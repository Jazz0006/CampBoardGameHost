package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState

private fun ClocktowerGameSession.gameStateWithPoisonTarget(targetSeat: Int?): GameState {
    val currentGameState = state.gameState
    if (targetSeat != null) {
        require(currentGameState.playerAt(targetSeat) != null) {
            "Unknown Clocktower seat $targetSeat."
        }
    }
    return currentGameState.copy(
        players = currentGameState.players.map { player ->
            player.copy(poisoned = player.alive && player.seat == targetSeat)
        },
    )
}

/** Commits an accepted poison-target boundary and owns its one game-state revision. */
internal fun ClocktowerGameSession.commitPoisonTargetBoundary(
    targetSeat: Int?,
): ClocktowerSessionState = commitGameStateBoundary(
    gameStateWithPoisonTarget(targetSeat),
)

/** Synchronizes poison projection inside a boundary whose game-state revision already exists. */
internal fun ClocktowerGameSession.synchronizePoisonTargetWithinCurrentRevision(
    targetSeat: Int?,
): ClocktowerSessionState = synchronizeGameStateWithinCurrentRevision(
    gameStateWithPoisonTarget(targetSeat),
)
