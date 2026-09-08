package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState

private fun ClocktowerGameSession.gameStateWithPlayerDeath(targetSeat: Int): GameState {
    val currentGameState = state.gameState
    require(currentGameState.playerAt(targetSeat) != null) {
        "Unknown Clocktower seat $targetSeat."
    }
    return currentGameState.copy(
        players = currentGameState.players.map { player ->
            if (player.seat == targetSeat) {
                player.copy(alive = false, poisoned = false)
            } else {
                player
            }
        },
    )
}

/** Synchronizes a player death inside a boundary whose game-state revision already exists. */
internal fun ClocktowerGameSession.synchronizePlayerDeathWithinCurrentRevision(
    targetSeat: Int,
): ClocktowerSessionState = synchronizeGameStateWithinCurrentRevision(
    gameStateWithPlayerDeath(targetSeat),
)
