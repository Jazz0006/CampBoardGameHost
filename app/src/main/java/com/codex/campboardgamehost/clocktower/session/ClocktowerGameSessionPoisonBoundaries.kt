package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

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

/**
 * One confirmed Host poison transition: explicit night choice, Dawn carry/expiry or outgoing
 * Day expiry. Mechanical flags, revision and globally sequenced fact commit together.
 * A semantic expiry can be a fact even if the poisoned seat was already killed.
 */
internal fun ClocktowerGameSession.commitConfirmedPoisonTransition(
    draft: ActionFactDraft.Poison,
    expectedGameStateRevision: Long,
): TimelineBoundActionFact {
    require(draft.phase in setOf(StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT, StorytellerPhase.DAY)) {
        "Poison transition must be confirmed at night or at outgoing Day expiry."
    }
    require(draft.phase != StorytellerPhase.DAY || draft.targetSeat == null) {
        "Outgoing Day poison expiry can only clear a poison effect."
    }
    return commitConfirmedGameAction(
        draft = draft,
        nextGameState = gameStateWithPoisonTarget(draft.targetSeat),
        expectedGameStateRevision = expectedGameStateRevision,
    )
}
