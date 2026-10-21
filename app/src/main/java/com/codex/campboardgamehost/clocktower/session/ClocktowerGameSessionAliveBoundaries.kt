package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

/**
 * Confirmed death/execution from a Host-validated current step. The Session captures
 * pre-death Klutz evidence, changes alive/poison flags and binds ONE action fact.
 * An already confirmed death is never duplicated as a separate public observation.
 */
internal fun ClocktowerGameSession.commitConfirmedPlayerDeath(
    draft: ActionFactDraft,
    expectedGameStateRevision: Long,
): TimelineBoundActionFact {
    val targetSeat = when (draft) {
        is ActionFactDraft.Death -> draft.targetSeat
        is ActionFactDraft.Execution -> draft.targetSeat
        else -> error("Only a confirmed death or execution can eliminate a player.")
    }
    val current = state.gameState
    val target = requireNotNull(current.playerAt(targetSeat)) { "Unknown Clocktower seat $targetSeat." }
    check(target.alive || state.actionTimeline.entries.any { it.fact.actionId == draft.actionId }) {
        "A dead player cannot be killed again by a new action."
    }
    val next = current.copy(
        players = current.players.map { player ->
            if (player.seat == targetSeat) player.copy(alive = false, poisoned = false) else player
        },
    )
    return commitConfirmedGameAction(
        draft = draft,
        nextGameState = next,
        expectedGameStateRevision = expectedGameStateRevision,
    )
}

