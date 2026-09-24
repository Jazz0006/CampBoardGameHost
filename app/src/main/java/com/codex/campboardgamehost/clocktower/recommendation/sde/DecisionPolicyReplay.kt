package com.codex.campboardgamehost.clocktower.recommendation.sde

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
    val policyVersion: PolicyVersion

    fun run(
        featureEvaluation: DecisionFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun
}

internal object BeginnerConservativeV1ReplayRunner : DecisionPolicyReplayRunner {
    override val policyVersion: PolicyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1

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
                listOf(BeginnerConservativeV1ReplayRunner),
            )
    }
}
