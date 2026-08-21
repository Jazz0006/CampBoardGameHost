package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint

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
        val globalSequence = snapshot.nextTimelineGlobalSequence
        check(globalSequence != Long.MAX_VALUE) { "Timeline global sequence exhausted." }
        val point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = sequence,
            globalSequence = globalSequence,
        )
        snapshot = snapshot.copy(nextTimelineGlobalSequence = globalSequence + 1)
        return point
    }

    /** Records only information that has been shown to its recipient(s) or publicly established. */
    fun recordEpistemicObservation(record: RecordedEpistemicObservation): GameSnapshot {
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

    companion object {
        fun create(
            gameId: String,
            gameSeed: Long,
            rulesetRef: RulesetRef,
            initialState: GameState,
        ): ClocktowerGameSession = ClocktowerGameSession(
            GameSnapshot(
                gameId = gameId,
                gameStateRevision = 0,
                playerInputRevision = 0,
                gameSeed = gameSeed,
                rulesetRef = rulesetRef,
                gameState = initialState,
            ),
        )

        fun restore(snapshot: GameSnapshot): ClocktowerGameSession = ClocktowerGameSession(snapshot)
    }
}
