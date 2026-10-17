package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

/**
 * Read the accepted poison choice from the one Session, including a dead target whose
 * mechanical poisoned flag has already cleared. The last same-round expiry (null) wins.
 * No obsolete snapshot compatibility fallback: current games commit their choices as facts.
 */
internal fun ClocktowerSessionState.confirmedPoisonTargetSeat(round: Int): Int? {
    require(round > 0)
    return (actionTimeline.entries.asReversed().firstOrNull {
        it.point.round == round && it.fact is ActionFact.Poison
    }?.fact as? ActionFact.Poison)?.targetSeat
}

internal fun ClocktowerSessionState.confirmedPoisonTargetName(round: Int): String? =
    confirmedPoisonTargetSeat(round)?.let { seat ->
        requireNotNull(gameState.playerAt(seat)) {
            "Confirmed Session poison references unknown seat $seat."
        }.name
    }

/** Commit the rules planner's carry/expiry; it cannot choose a different poison victim. */
internal fun ClocktowerGameSession.commitDawnPoisonCarry(
    intent: DawnPoisonCarryIntent,
    expectedGameStateRevision: Long,
): Boolean {
    check(state.phase == StorytellerPhase.NIGHT) { "Dawn poison settles from Night." }
    require(intent.targetSeat == null || intent.targetSeat == intent.previousTargetSeat)
    return settlePoison(intent.targetSeat, intent.previousTargetSeat, expectedGameStateRevision)
}

/** The outgoing Day owns expiry, before the next Night phase is committed. */
internal fun ClocktowerGameSession.expirePoisonAtDusk(expectedGameStateRevision: Long): Boolean {
    check(state.phase == StorytellerPhase.DAY) { "Dusk poison settles from Day." }
    return settlePoison(null, state.confirmedPoisonTargetSeat(state.round), expectedGameStateRevision)
}

private fun ClocktowerGameSession.settlePoison(
    targetSeat: Int?,
    previousTargetSeat: Int?,
    expectedGameStateRevision: Long,
): Boolean {
    check(state.gameStateRevision == expectedGameStateRevision) { "Stale poison settlement." }
    val current = state.confirmedPoisonTargetSeat(state.round)
    check(state.gameState == gameStateWithPoisonTarget(current)) {
        "Confirmed poison and mechanical state disagree."
    }
    if (current == targetSeat) return false
    check(current == previousTargetSeat) { "Poison settlement no longer matches the confirmed choice." }
    commitConfirmedPoisonTransition(
        draft = ActionFactDraft.Poison(
            actionId = "poison-settlement-${state.gameId}-${state.round}-${state.phase}-$nextInputSequence",
            phase = state.phase,
            round = state.round,
            sequence = nextInputSequence,
            targetSeat = targetSeat,
        ),
        expectedGameStateRevision = expectedGameStateRevision,
        preserveNightChoices = true,
    )
    return true
}

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
    // Dawn carry/expiry resolves an existing choice; it must not revoke pending succession.
    preserveNightChoices: Boolean = false,
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
        preserveNightChoices = preserveNightChoices,
    )
}
