package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline

/** Current ghost-vote use is scoped to a life, not forever to a physical seat. */
internal fun ActionFactTimeline.spentGhostVoterSeats(): Set<Int> = buildSet {
    entries.forEach { entry -> when (val fact = entry.fact) {
        is ActionFact.Vote -> addAll(fact.ghostVoterSeats)
        is ActionFact.Revival -> remove(fact.targetSeat)
        else -> Unit
    } }
}

internal fun ClocktowerGameSession.confirmRevival(actionId: String, targetSeat: Int) {
    check(state.gameState.script == BadMoonRisingOpening.script)
    val previous = state.actionTimeline.entries.firstOrNull { it.fact.actionId == actionId }?.fact
    if (previous != null) { check(previous is ActionFact.Revival && previous.targetSeat == targetSeat); return }
    val player = requireNotNull(state.gameState.playerAt(targetSeat))
    require(!player.alive) { "Only an actually dead player can be resurrected." }
    commitConfirmedGameAction(ActionFactDraft.Revival(actionId, state.phase, state.round, nextInputSequence, targetSeat),
        state.gameState.copy(players = state.gameState.players.map {
            if (it.seat == targetSeat) it.copy(alive = true, registersAsDead = false) else it
        }), state.gameStateRevision)
}

internal fun ClocktowerGameSession.confirmDeathRegistration(actionId: String, targetSeat: Int) {
    check(state.gameState.script == BadMoonRisingOpening.script)
    val previous = state.actionTimeline.entries.firstOrNull { it.fact.actionId == actionId }?.fact
    if (previous != null) { check(previous is ActionFact.DeathRegistration && previous.targetSeat == targetSeat); return }
    val player = requireNotNull(state.gameState.playerAt(targetSeat))
    require(player.alive && !player.impaired && !player.registersAsDead && player.actualRole.value == "Zombuul")
    commitConfirmedGameAction(ActionFactDraft.DeathRegistration(actionId, state.phase, state.round, nextInputSequence, targetSeat),
        state.gameState.copy(players = state.gameState.players.map {
            if (it.seat == targetSeat) it.copy(registersAsDead = true) else it
        }), state.gameStateRevision)
}

/** Public changes read from life facts; a Zombuul's later real death is private, not a second announcement. */
internal data class ClocktowerPublicLifeChange(val seat: Int, val revived: Boolean)

internal fun ClocktowerSessionState.publicLifeChangesAtNight(round: Int): List<ClocktowerPublicLifeChange> {
    val publiclyDead = mutableSetOf<Int>()
    return buildList {
        actionTimeline.entries.forEach { entry ->
            val night = entry.point.round == round && entry.point.phase != com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY
            when(val fact = entry.fact) {
                is ActionFact.Revival -> if(publiclyDead.remove(fact.targetSeat) && night) add(ClocktowerPublicLifeChange(fact.targetSeat, true))
                else -> {
                    val seat = when(fact) {
                        is ActionFact.Death -> fact.targetSeat
                        is ActionFact.DeathRegistration -> fact.targetSeat
                        is ActionFact.Execution -> fact.targetSeat.takeIf { fact.causedDeath }
                        else -> null
                    }
                    if(seat != null && publiclyDead.add(seat) && night) add(ClocktowerPublicLifeChange(seat, false))
                }
            }
        }
    }
}
