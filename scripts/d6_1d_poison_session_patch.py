from pathlib import Path

SESSION_PATH = Path(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt"
)
POISON_PATH = Path(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPoisonBoundaries.kt"
)

session_text = SESSION_PATH.read_text()
marker = (
    "    /** Production event/decision boundary whose accepted revision cadence is independent of state equality. */\n"
)
if session_text.count(marker) != 1:
    raise SystemExit("Expected exactly one advanceGameStateRevision marker.")

insert = '''    /**
     * Synchronizes canonical mechanical state inside a production boundary whose revision was
     * already accepted by the caller. This never creates an additional game-state revision.
     */
    fun synchronizeGameStateWithinCurrentRevision(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        if (nextState == state.gameState) return state
        return updateState(state.copy(gameState = nextState))
    }

'''
SESSION_PATH.write_text(session_text.replace(marker, insert + marker, 1))

if POISON_PATH.exists():
    raise SystemExit("Poison boundary source already exists.")

POISON_PATH.write_text('''package com.codex.campboardgamehost.clocktower.session

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
''')
