package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef

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
