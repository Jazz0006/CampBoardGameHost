package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1

/**
 * An all-or-nothing *proposal* handoff. It never writes observations/actions.
 * Callers must regenerate the current Host question immediately before applying
 * any decision, and then use that decision's existing Host confirmation route.
 */
internal object StorytellerResidualPackageGateV2 {
    internal data class Accepted(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val choices: Map<String, StorytellerStrategicContinuationV2.SemanticResult>,
    )

    fun verify(
        frozen: ClocktowerSessionState,
        phase: StorytellerPhase,
        round: Int,
        originalOpen: List<StorytellerStrategicContinuationV2.OpenDecision>,
        currentRequests: List<StorytellerProviderRequestV1>,
        result: StorytellerStrategicContinuationV2.Result,
        cursor: StorytellerStrategicContinuationV2.MemoryCursor,
    ): Accepted {
        require(cursor.gameId == frozen.gameId && cursor.version == result.memoryVersion)
        require(result.gameId == frozen.gameId &&
            result.gameStateRevision == frozen.gameStateRevision &&
            result.playerInputRevision == frozen.playerInputRevision &&
            result.exclusiveGlobalSequence == frozen.nextTimelineGlobalSequence) {
            "Model response targets an obsolete Host state or observation prefix."
        }
        require(originalOpen.isNotEmpty()) {
            "Never present an executable model package without Host-owned decisions."
        }
        val fresh = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            frozen, phase, round, currentRequests)
        // Includes recipient, ability, observed subjects and revision, not just an opaque ID.
        require(originalOpen == fresh) {
            "Any newly opened, resolved or changed current question invalidates the whole package."
        }
        require(result.remainingChoices.keys == fresh.map { it.decisionId }.toSet()) {
            "All and only current Host decisions must have a response."
        }
        fresh.forEach { decision ->
            requireNotNull(result.remainingChoices[decision.decisionId]).validate(
                decision.currentDecisionContext, frozen.gameState.players.size)
        }
        // No implicit default, witness selection, game mutation or partial commit.
        return Accepted(frozen.gameId, frozen.gameStateRevision,
            frozen.playerInputRevision, frozen.nextTimelineGlobalSequence,
            result.remainingChoices.toMap())
    }
}
