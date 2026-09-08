package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Commits one accepted actual-role identity boundary without synchronizing unrelated live mechanics.
 * Alive/poisoned/shown identity and every other seat remain session-owned and unchanged.
 */
internal fun ClocktowerGameSession.commitActualRoleBoundary(
    seat: Int,
    actualRole: RoleId,
    actualAlignment: Alignment,
    actualType: CharacterType,
): ClocktowerSessionState {
    val currentGameState = state.gameState
    require(currentGameState.playerAt(seat) != null) {
        "Unknown Clocktower seat $seat."
    }
    val nextGameState = currentGameState.copy(
        players = currentGameState.players.map { player ->
            if (player.seat != seat) {
                player
            } else {
                player.copy(
                    actualRole = actualRole,
                    actualAlignment = actualAlignment,
                    actualType = actualType,
                )
            }
        },
    )
    return commitGameStateBoundary(nextGameState)
}

/**
 * Commits one accepted shown-role identity boundary without synchronizing unrelated live mechanics.
 * Actual identity/alive/poisoned and every other seat remain session-owned and unchanged.
 */
internal fun ClocktowerGameSession.commitShownRoleBoundary(
    seat: Int,
    shownRole: RoleId,
): ClocktowerSessionState {
    val currentGameState = state.gameState
    require(currentGameState.playerAt(seat) != null) {
        "Unknown Clocktower seat $seat."
    }
    val nextGameState = currentGameState.copy(
        players = currentGameState.players.map { player ->
            if (player.seat != seat) {
                player
            } else {
                player.copy(shownRole = shownRole)
            }
        },
    )
    return commitGameStateBoundary(nextGameState)
}
