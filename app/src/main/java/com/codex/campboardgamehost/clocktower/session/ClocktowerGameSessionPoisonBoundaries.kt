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
 * Confirmed poison choice: Host's accepted target, mechanical poison flags, revision and
 * globally sequenced ActionFact.Poison are committed together. No UI/fact-first write.
 */
internal fun ClocktowerGameSession.commitConfirmedPoisonTarget(
    draft: ActionFactDraft.Poison,
    expectedGameStateRevision: Long,
): TimelineBoundActionFact {
    require(draft.phase == StorytellerPhase.FIRST_NIGHT || draft.phase == StorytellerPhase.NIGHT) {
        "Poison target confirmation is a night action."
    }
    return commitConfirmedGameAction(
        draft = draft,
        nextGameState = gameStateWithPoisonTarget(draft.targetSeat),
        expectedGameStateRevision = expectedGameStateRevision,
    )
}

/** Synchronizes poison projection inside a boundary whose game-state revision already exists. */
internal fun ClocktowerGameSession.synchronizePoisonTargetWithinCurrentRevision(
    targetSeat: Int?,
): ClocktowerSessionState = synchronizeGameStateWithinCurrentRevision(
    gameStateWithPoisonTarget(targetSeat),
)
