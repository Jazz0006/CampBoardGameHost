package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

// Seat-based night facts read only from the Session. UI maps seats to cards and labels.

internal fun ClocktowerSessionState.allSeats(): Set<Int> = gameState.players.mapTo(sortedSetOf()) { it.seat }

internal fun ClocktowerSessionState.aliveSeats(): Set<Int> =
    gameState.players.filter(PlayerState::alive).mapTo(sortedSetOf()) { it.seat }

internal fun ClocktowerSessionState.actualRoleSeats(role: String): List<Int> =
    gameState.players.filter { it.actualRole.value == role }.map { it.seat }.sorted()

internal fun ClocktowerSessionState.actualTypeSeats(type: CharacterType): List<Int> =
    gameState.players.filter { it.actualType == type }.map { it.seat }.sorted()

/** The single living Imp, if any. */
internal fun ClocktowerSessionState.livingDemonSeat(): Int? =
    gameState.players.singleOrNull { it.alive && it.actualRole.value == "Imp" }?.seat

internal fun ClocktowerSessionState.livingDemonPoisoned(): Boolean =
    gameState.players.any { it.alive && it.actualRole.value == "Imp" && it.poisoned }

internal fun ClocktowerSessionState.livingNeighbourSeats(seat: Int): List<Int> =
    NumericInformationResolver.livingNeighbours(gameState.players.sortedBy(PlayerState::seat), seat).map(PlayerState::seat)

/** Every seat except an attacked Mayor awaiting the redirect ruling. */
internal fun ClocktowerSessionState.mayorRedirectEligibleSeats(): Set<Int> = mayorRulingSeat().let { mayor ->
    allSeats().filterTo(sortedSetOf()) { it != mayor }
}

private fun ClocktowerSessionState.currentNightDeaths(): List<TimelineBoundActionFact> = actionTimeline.entries.filter {
    it.point.phase == phase && it.point.round == round && it.fact is ActionFact.Death
}

/** A player whose waking role died this night, e.g. a Ravenkeeper or Sage death trigger. */
internal fun ClocktowerSessionState.currentNightDeathTriggerSeat(role: String): Int? =
    currentNightDeaths().firstNotNullOfOrNull { entry ->
        gameState.playerAt((entry.fact as ActionFact.Death).targetSeat)?.takeIf { it.wakingRole() == RoleId(role) }?.seat
    }

/** The waking role's seat if its ability is poisoned now; a death trigger uses the poison in force before that death. */
internal fun ClocktowerSessionState.poisonedSeatForRole(role: String): Int? {
    val player = gameState.players.firstOrNull { it.wakingRole() == RoleId(role) } ?: return null
    if (role in setOf("Ravenkeeper", "Sage") && !player.alive) {
        val death = currentNightDeaths().firstOrNull { (it.fact as ActionFact.Death).targetSeat == player.seat } ?: return null
        val poison = actionTimeline.entries.lastOrNull {
            it.point.globalSequence < death.point.globalSequence && it.fact is ActionFact.Poison
        }?.fact as? ActionFact.Poison
        return player.seat.takeIf { poison?.targetSeat == player.seat }
    }
    return player.seat.takeIf { player.poisoned }
}

internal fun ClocktowerSessionState.abilitySubjectForRole(role: String, seat: Int?): AbilitySubject? {
    val player = seat?.let(gameState::playerAt) ?: return null
    return AbilitySubject(player.actualRole.value, player.shownRole?.value, poisonedSeatForRole(role) == player.seat, player.alive)
}

internal fun ClocktowerSessionState.deathTriggerAbilityState(role: String): AbilityFunctioningState? =
    abilitySubjectForRole(role, currentNightDeathTriggerSeat(role))
        ?.let { AbilityFunctioningSemantics.stateForEstablishedInteraction(it, role) }
