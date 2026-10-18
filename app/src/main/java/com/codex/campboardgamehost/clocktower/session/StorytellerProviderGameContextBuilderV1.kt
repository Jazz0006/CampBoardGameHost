package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPlayerContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1

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

        return StorytellerProviderGameContextV1(
            players = players,
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
        // Confirmed choices belong to the shared action timeline. Capture their exact
        // action/observation cutoff from this frozen Session, with no parallel decision store.
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
