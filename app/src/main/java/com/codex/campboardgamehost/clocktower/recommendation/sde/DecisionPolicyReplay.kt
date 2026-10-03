package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

internal sealed interface DecisionPolicyReplayScope {
    object Unspecified : DecisionPolicyReplayScope

    data class PairInformation(
        val script: ScriptId,
        val phase: StorytellerPhase,
        val round: Int,
        val targetType: CharacterType,
        val reliability: ReliabilityState,
        val truthfulLegalOutcomes: Boolean,
    ) : DecisionPolicyReplayScope {
        init {
            require(round > 0) { "Pair replay scope round must be positive." }
        }
    }
}

internal data class DecisionPolicyReplayRun(
    val policySnapshot: DecisionTracePolicySnapshot,
    val policySelection: PolicySelection?,
) {
    init {
        when (policySnapshot) {
            is DecisionTracePolicySnapshot.Ready -> {
                val selection = requireNotNull(policySelection) {
                    "Ready policy replay requires a selection."
                }
                require(selection.policyVersion == policySnapshot.policyVersion) {
                    "Policy replay selection must use the replayed policy version."
                }
                val selected = policySnapshot.evaluations.singleOrNull {
                    it.candidateId == selection.candidateId
                }
                require(selected?.disposition == PolicyDisposition.SURVIVOR) {
                    "Policy replay selection must choose a policy survivor."
                }
            }

            is DecisionTracePolicySnapshot.Deferred ->
                require(policySelection == null) {
                    "Deferred policy replay cannot carry a selection."
                }
        }
    }
}

internal interface DecisionPolicyReplayRunner {
    val definition: StorytellerPolicyDefinition

    val policyVersion: PolicyVersion
        get() = definition.policyVersion

    fun run(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun

    fun runScoped(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
        policyScope: DecisionPolicyReplayScope,
    ): DecisionPolicyReplayRun = run(
        featureEvaluation = featureEvaluation,
        decisionId = decisionId,
        selectionSeed = selectionSeed,
    )
}

internal object BeginnerConservativeV1ReplayRunner : DecisionPolicyReplayRunner {
    override val definition: StorytellerPolicyDefinition =
        StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1

    override fun run(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun {
        val evaluation = BeginnerConservativeV1Policy.evaluate(featureEvaluation)
        val selection = BeginnerConservativeV1Selector.select(
            evaluation = evaluation,
            decisionId = decisionId,
            selectionSeed = selectionSeed,
        )
        return DecisionPolicyReplayRun(
            policySnapshot = evaluation.toDecisionTracePolicySnapshot(),
            policySelection = selection,
        )
    }
}

internal object BeginnerConservativeV2ReplayRunner : DecisionPolicyReplayRunner {
    override val definition: StorytellerPolicyDefinition =
        StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V2

    override fun run(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun = runScoped(
        featureEvaluation = featureEvaluation,
        decisionId = decisionId,
        selectionSeed = selectionSeed,
        policyScope = DecisionPolicyReplayScope.Unspecified,
    )

    override fun runScoped(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
        policyScope: DecisionPolicyReplayScope,
    ): DecisionPolicyReplayRun {
        val evaluation = BeginnerConservativeV2Policy.evaluate(
            featureEvaluation = featureEvaluation,
            scope = policyScope,
        )
        val selection = BeginnerConservativeV2Selector.select(
            evaluation = evaluation,
            decisionId = decisionId,
            selectionSeed = selectionSeed,
        )
        return DecisionPolicyReplayRun(
            policySnapshot = evaluation.toDecisionTracePolicySnapshot(),
            policySelection = selection,
        )
    }
}

/**
 * Explicit policy-version registry for offline replay.
 *
 * Production contains only policy versions that actually exist. Unknown versions fail closed rather
 * than silently reusing the current policy implementation.
 */
internal class DecisionPolicyReplayRegistry(
    runners: List<DecisionPolicyReplayRunner>,
) {
    private val runnersByVersion: Map<PolicyVersion, DecisionPolicyReplayRunner>

    val supportedVersions: Set<PolicyVersion>

    init {
        require(runners.isNotEmpty()) { "Policy replay registry requires at least one runner." }
        require(runners.map(DecisionPolicyReplayRunner::policyVersion).distinct().size == runners.size) {
            "Policy replay registry cannot contain duplicate policy versions."
        }
        runnersByVersion = runners.associateBy(DecisionPolicyReplayRunner::policyVersion)
        supportedVersions = runners.map(DecisionPolicyReplayRunner::policyVersion).toSet()
    }

    fun requireRunner(policyVersion: PolicyVersion): DecisionPolicyReplayRunner =
        requireNotNull(runnersByVersion[policyVersion]) {
            "Unsupported policy replay version '" + policyVersion.value + "'."
        }

    companion object {
        fun production(): DecisionPolicyReplayRegistry =
            DecisionPolicyReplayRegistry(
                listOf(
                    BeginnerConservativeV1ReplayRunner,
                    BeginnerConservativeV2ReplayRunner,
                ),
            )
    }
}
