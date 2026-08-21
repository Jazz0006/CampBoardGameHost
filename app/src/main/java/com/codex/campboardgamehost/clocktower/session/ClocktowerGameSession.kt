package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature

internal class ClocktowerGameSession private constructor(
    initialSnapshot: GameSnapshot,
) {
    var snapshot: GameSnapshot = initialSnapshot
        private set

    fun updateGameState(nextState: GameState): GameSnapshot {
        require(nextState.seed == snapshot.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        if (nextState == snapshot.gameState) return snapshot
        snapshot = snapshot.copy(
            gameStateRevision = snapshot.gameStateRevision + 1,
            gameState = nextState,
        )
        return snapshot
    }

    fun recordPlayerInput(): GameSnapshot {
        snapshot = snapshot.copy(playerInputRevision = snapshot.playerInputRevision + 1)
        return snapshot
    }

    /**
     * Allocates the next game-wide timeline identity without changing semantic game/input revisions.
     * Local [sequence] remains phase-specific replay/display context and never resets global ordering.
     */
    fun allocateTimelinePoint(
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): TimelinePoint {
        val point = nextTimelinePoint(phase, round, sequence)
        snapshot = snapshot.copy(nextTimelineGlobalSequence = point.globalSequence + 1)
        return point
    }

    /**
     * Atomic authority for newly produced Global observations.
     *
     * The caller supplies an unbound draft. Timeline identity, durable binding, cursor advancement,
     * and the player-input revision are committed together from the session snapshot.
     */
    fun commitGlobalEpistemicObservation(draft: EpistemicObservationDraft): RecordedEpistemicObservation {
        require(snapshot.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "Global observation commit requires GLOBAL_V1 semantic history."
        }

        snapshot.epistemicObservationLog.records.firstOrNull { it.recordId == draft.recordId }?.let { existing ->
            require(draft.matches(existing)) {
                "Observation record ID '${draft.recordId}' is already committed with different content."
            }
            require(existing.timelineBinding is ObservationTimelineBinding.Global) {
                "GLOBAL_V1 history cannot reuse a LegacyLocal observation record ID."
            }
            return existing
        }

        val point = nextTimelinePoint(draft.phase, draft.round, draft.sequence)
        val record = draft.bindGlobal(point)
        val nextLog = snapshot.epistemicObservationLog.append(record)
        snapshot = snapshot.copy(
            playerInputRevision = snapshot.playerInputRevision + 1,
            epistemicObservationLog = nextLog,
            nextTimelineGlobalSequence = point.globalSequence + 1,
        )
        return record
    }

    /** Records pre-cutover LegacyLocal information only; Global producers must use the draft API. */
    fun recordEpistemicObservation(record: RecordedEpistemicObservation): GameSnapshot {
        require(snapshot.semanticHistoryMode == ClocktowerSemanticHistoryMode.LEGACY_LOCAL) {
            "Direct durable observation recording is reserved for LEGACY_LOCAL history."
        }
        require(record.timelineBinding === ObservationTimelineBinding.LegacyLocal) {
            "LEGACY_LOCAL session cannot accept a pre-bound Global observation."
        }
        snapshot = snapshot.copy(
            playerInputRevision = snapshot.playerInputRevision + 1,
            epistemicObservationLog = snapshot.epistemicObservationLog.append(record),
        )
        return snapshot
    }

    fun recordCompletedGameSignature(signature: HistoricalClueSignature): GameSnapshot {
        snapshot = snapshot.copy(crossGameHistory = snapshot.crossGameHistory.append(signature))
        return snapshot
    }

    private fun nextTimelinePoint(
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): TimelinePoint {
        val globalSequence = snapshot.nextTimelineGlobalSequence
        check(globalSequence != Long.MAX_VALUE) { "Timeline global sequence exhausted." }
        return TimelinePoint(
            phase = phase,
            round = round,
            sequence = sequence,
            globalSequence = globalSequence,
        )
    }

    companion object {
        fun create(
            gameId: String,
            gameSeed: Long,
            rulesetRef: RulesetRef,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        ): ClocktowerGameSession = ClocktowerGameSession(
            GameSnapshot(
                gameId = gameId,
                gameStateRevision = 0,
                playerInputRevision = 0,
                gameSeed = gameSeed,
                rulesetRef = rulesetRef,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
            ),
        )

        fun restore(snapshot: GameSnapshot): ClocktowerGameSession = ClocktowerGameSession(snapshot)
    }
}
