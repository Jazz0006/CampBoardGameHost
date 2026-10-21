package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

/**
 * Confirm one Host-accepted actual role transition. Unlike the old role-only boundary,
 * actual identity, globally ordered RoleChange fact, and revision publish together.
 * Shown identity and alive status stay unchanged. Session also settles any ongoing
 * poison effect whose source loses its actual Poisoner role in this same step.
 */
internal fun ClocktowerGameSession.commitConfirmedActualRoleChange(
    draft: ActionFactDraft.RoleChange,
    expectedGameStateRevision: Long,
): TimelineBoundActionFact {
    val previous = requireNotNull(state.gameState.playerAt(draft.targetSeat)) {
        "Unknown Clocktower seat ${draft.targetSeat}."
    }
    check(
        previous.actualRole != draft.role ||
            previous.actualAlignment != draft.alignment ||
            previous.actualType != draft.type ||
            state.actionTimeline.entries.any { it.fact.actionId == draft.actionId }
    ) { "An unchanged actual role cannot generate a new RoleChange fact." }
    val nextGameState = state.gameState.copy(
        players = state.gameState.players.map { player ->
            if (player.seat != draft.targetSeat) player else player.copy(
                actualRole = draft.role,
                actualAlignment = draft.alignment,
                actualType = draft.type,
            )
        },
    )
    return commitConfirmedGameAction(
        draft = draft,
        nextGameState = nextGameState,
        expectedGameStateRevision = expectedGameStateRevision,
    )
}

