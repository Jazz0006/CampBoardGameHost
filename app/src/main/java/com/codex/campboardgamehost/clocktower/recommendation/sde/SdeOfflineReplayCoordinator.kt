package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext

internal data class SdeOfflineReplayEvaluation(
    val shadow: StructuredInformationShadowEvaluation,
    val pendingTrace: DecisionTrace,
    val recomputedInput: MultiPolicyReplayInput,
)

/** Composes existing owners for one offline evaluation; owns no game state or policy semantics. */
internal object SdeOfflineReplayCoordinator {
    fun <T : DynamicInformationOutcome> evaluate(
        replayInput: SdeHistoricalReplayInput,
        decisionContext: InformationDecisionContext<T>,
        validatedRuleset: ValidatedClocktowerRuleset,
        roleDefinitions: Collection<RoleDefinition>,
        inputBindings: SdeDecisionInputBindings = SdeDecisionInputBindings.NotCaptured,
        hypothesis: EpistemicHypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    ): SdeOfflineReplayEvaluation {
        require(decisionContext.requestIdentity.gameId == replayInput.gameId) {
            "Replay input belongs to a different game than the information decision request."
        }
        require(replayInput.rulesetRef.scriptId == validatedRuleset.script.id) {
            "Replay input belongs to a different ruleset script."
        }
        require(replayInput.rulesetRef.scriptContentHash == validatedRuleset.script.contentHash) {
            "Replay input ruleset content does not match the validated ruleset."
        }
        require(replayInput.rulesetRef.coverage == validatedRuleset.coverage) {
            "Replay input ruleset coverage does not match the validated ruleset."
        }
        val shadow = StructuredInformationProductionShadow.evaluateHistorical(
            decisionContext = decisionContext,
            validatedRuleset = validatedRuleset,
            committedSetup = replayInput.toCommittedSetup(),
            currentSnapshot = replayInput.toGameSnapshot(roleDefinitions),
            roleDefinitions = roleDefinitions,
            inputBindings = inputBindings,
            hypothesis = hypothesis,
        )
        return SdeOfflineReplayEvaluation(
            shadow = shadow,
            pendingTrace = DecisionTraceFactory.fromStructuredShadow(shadow),
            recomputedInput = MultiPolicyReplayInput.fromStructuredShadow(shadow),
        )
    }
}
