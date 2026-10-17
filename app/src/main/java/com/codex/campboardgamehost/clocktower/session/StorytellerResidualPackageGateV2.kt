package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1

/**
 * An all-or-nothing *proposal* handoff. It never writes observations/actions.
 * Callers must regenerate every pending Host domain immediately before applying
 * any decision, and then use that decision's existing Host confirmation route.
 */
internal object StorytellerResidualPackageGateV2 {
    internal data class Accepted(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val choices: Map<String, String>,
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
        // Includes candidate payload semantics, not just a matching opaque ID.
        require(originalOpen == fresh) {
            "Any newly opened, resolved or changed legal decision invalidates the whole package."
        }
        val expected = fresh.associate { decision ->
            decision.decisionId to decision.legalCandidates.map { it.candidateId }.toSet()
        }
        require(expected.values.all { it.isNotEmpty() })
        require(result.remainingChoices.keys == expected.keys &&
            result.remainingChoices.all { (id, choice) ->
                choice in requireNotNull(expected[id])
            }) {
            "All and only currently Host-legal residual decisions must be selected."
        }
        // No implicit default, witness selection, game mutation or partial commit.
        return Accepted(frozen.gameId, frozen.gameStateRevision,
            frozen.playerInputRevision, frozen.nextTimelineGlobalSequence,
            result.remainingChoices.toMap())
    }
}
