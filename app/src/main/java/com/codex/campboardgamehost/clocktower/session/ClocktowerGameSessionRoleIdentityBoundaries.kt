package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics
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

/** Read only confirmed mechanics; selecting an attack target never makes it a fact. */
internal fun ClocktowerSessionState.impSelfKillSucceeds(targetSeat: Int?): Boolean {
    if (phase != StorytellerPhase.NIGHT) return false
    val imp = gameState.players.singleOrNull { it.alive && it.actualRole == RoleId("Imp") } ?: return false
    return imp.seat == targetSeat && !imp.poisoned && !(nightTargets.monkSeat == targetSeat &&
        gameState.players.any { it.alive && !it.poisoned && it.actualRole == RoleId("Monk") })
}

/** The live UI and final attack confirmation share this one succession decision. */
internal fun ClocktowerSessionState.impSelfKillSuccession(targetSeat: Int?): DemonSuccessionResolution {
    if (!impSelfKillSucceeds(targetSeat)) return DemonSuccessionResolution.None
    val roster = gameState.players
    return DemonSuccessionSemantics.resolve(DemonSuccessionContext(
        demonActuallyDied = true,
        demonDeathWasImpSelfKill = true,
        aliveCountBeforeDemonDeath = roster.count { it.alive },
        functioningScarletWomanSeat = roster.singleOrNull {
            it.alive && !it.poisoned && it.actualRole == RoleId("Scarlet Woman")
        }?.seat,
        livingMinionSeats = roster.filter {
            it.alive && it.actualType == CharacterType.MINION
        }.mapTo(linkedSetOf()) { it.seat },
    ))
}
