package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPlayerContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository

/**
 * Host-owned reconstruction of stateless provider enrichment.
 *
 * Provider conversation memory is never required: player context and prior committed decisions are
 * rebuilt from canonical session/history for each request.
 */
internal object StorytellerProviderGameContextBuilderV1 {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        sourceRevision: StorytellerProviderRevisionV1,
        playerInputsBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
        decisionHistory: DecisionHistoryArchive = DecisionHistoryArchive(),
    ): StorytellerProviderGameContextV1 {
        val snapshotSeats = snapshot.grimoireSeats.map { it.seat }
        require(playerInputsBySeat.keys.all { it in snapshotSeats }) {
            "Provider player-context overrides must reference current snapshot seats."
        }

        val players = snapshotSeats.map { seat ->
            val input = playerInputsBySeat[seat] ?: StorytellerPlayerContextInputV1.DEFAULT
            StorytellerProviderPlayerContextV1(
                seat = seat,
                experienceLevel = input.experienceLevel,
                claimedRoleIds = input.claimedRoleIds.toList(),
                pressureLevel = input.pressureLevel,
            )
        }

        val priorDecisions = DecisionHistoryRepository(decisionHistory)
            .project()
            .effectiveEvents
            .asSequence()
            .filter { event ->
                event.gameStateRevision <= sourceRevision.gameStateRevision &&
                    event.playerInputRevision <= sourceRevision.playerInputRevision
            }
            .map { event ->
                StorytellerProviderPriorDecisionV1(
                    eventId = event.eventId,
                    gameStateRevision = event.gameStateRevision,
                    playerInputRevision = event.playerInputRevision,
                    selectedCandidateId = event.selectedCandidateId,
                    selectedOutcome = event.selectedOutcomeSnapshot,
                    abilityState = event.abilityState,
                    truthRelation = event.truthRelation,
                    registrations = event.registrations.toList(),
                )
            }
            .toList()

        return StorytellerProviderGameContextV1(
            players = players,
            priorDecisions = priorDecisions,
        )
    }

    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        sourceRevision: StorytellerProviderRevisionV1,
        sessionState: ClocktowerSessionState,
    ): StorytellerProviderGameContextV1 {
        require(sessionState.gameId == snapshot.gameId) {
            "Provider context must use the current session game."
        }
        require(sessionState.gameStateRevision == sourceRevision.gameStateRevision) {
            "Provider context game-state revision must match the request revision."
        }
        require(sessionState.playerInputRevision == sourceRevision.playerInputRevision) {
            "Provider context player-input revision must match the request revision."
        }
        // Current effective decision events cannot be causally interleaved with GLOBAL_V1 facts.
        // Until R1C persists exact decision cutoffs and corrections, keep them out of the
        // historical provider prefix. Coverage explicitly marks PRIOR_DECISIONS unknown.
        val base = build(
            snapshot = snapshot,
            sourceRevision = sourceRevision,
            playerInputsBySeat = sessionState.storytellerPlayerContextBySeat,
        )
        return base.copy(
            historyPrefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
                sessionState = sessionState,
                sourceRevision = sourceRevision,
            ),
        )
    }
}
