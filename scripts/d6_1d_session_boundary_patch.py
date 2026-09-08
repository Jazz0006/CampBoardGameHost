from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in ClocktowerGameSession.kt")
text = raw.decode("utf-8")

old = '''    fun updateGameState(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        if (nextState == state.gameState) return state
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /** Production event/decision boundary whose accepted revision cadence is independent of state equality. */
'''

new = '''    fun updateGameState(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        if (nextState == state.gameState) return state
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /**
     * Commits one accepted production mechanical boundary atomically.
     *
     * Unlike [updateGameState], accepted production boundaries preserve the established revision
     * cadence even when their projected [GameState] is value-equal to the previous state.
     */
    fun commitGameStateBoundary(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /** Production event/decision boundary whose accepted revision cadence is independent of state equality. */
'''

count = text.count(old)
if count != 1:
    raise SystemExit(f"Session boundary anchor count was {count}, expected exactly 1")
if "fun commitGameStateBoundary(nextState: GameState)" in text:
    raise SystemExit("commitGameStateBoundary already exists")
text = text.replace(old, new, 1)
if text.count("fun commitGameStateBoundary(nextState: GameState)") != 1:
    raise SystemExit("Expected exactly one commitGameStateBoundary after patch")
TARGET.write_text(text, encoding="utf-8", newline="\n")
