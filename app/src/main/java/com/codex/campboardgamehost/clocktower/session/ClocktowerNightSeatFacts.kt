package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction
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

/** Old dead Imps retain their role after succession; only the living Imp can act.
 * Other roles retain their established wake/death-trigger handling in nightInteractions. */
internal fun ClocktowerSessionState.wakingPlayerForRole(role: RoleId): PlayerState? =
    if (role.value == "Imp") livingDemonSeat()?.let(gameState::playerAt)
    else gameState.players.firstOrNull { it.wakingRole() == role }

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
    val player = wakingPlayerForRole(RoleId(role)) ?: return null
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

/** First-night Minion/Demon information and Demon bluffs need seven or more players. */
internal fun ClocktowerSessionState.firstNightEvilInformationRequired(): Boolean = gameState.players.size >= 7

/** Owner rule: the Red Herring is an actually good player who is not the Recluse. */
internal fun redHerringEligible(isGood: Boolean, roleId: String): Boolean = isGood && roleId != "Recluse"

internal fun ClocktowerSessionState.redHerringSelectableSeats(): Set<Int> = gameState.players
    .filter { redHerringEligible(it.actualAlignment == Alignment.GOOD, it.actualRole.value) }
    .mapTo(sortedSetOf()) { it.seat }

internal enum class ClocktowerRoleAvailability { ACTIVE, DEAD, DRUNK_SHOWN_ONLY, NOT_IN_PLAY }

/** How a role stands now; [seat] is the actual role holder when one is seated. */
internal data class ClocktowerRoleStatus(val availability: ClocktowerRoleAvailability, val seat: Int?)

internal fun ClocktowerSessionState.roleStatus(role: String): ClocktowerRoleStatus {
    val holder = gameState.players.firstOrNull { it.actualRole.value == role }
    return when {
        holder != null -> ClocktowerRoleStatus(
            if (holder.alive) ClocktowerRoleAvailability.ACTIVE else ClocktowerRoleAvailability.DEAD, holder.seat)
        gameState.players.any { it.actualRole.value == "Drunk" && it.shownRole?.value == role } ->
            ClocktowerRoleStatus(ClocktowerRoleAvailability.DRUNK_SHOWN_ONLY, null)
        else -> ClocktowerRoleStatus(ClocktowerRoleAvailability.NOT_IN_PLAY, null)
    }
}

/**
 * Night facts read once per Session snapshot and shared by the night screen, the Host step and
 * the display builders, so each fact has one computation. UI maps these seats to cards and labels.
 */
internal class ClocktowerNightFacts(private val state: ClocktowerSessionState, ruleset: ValidatedClocktowerRuleset) {
    val phase: StorytellerPhase get() = state.phase
    val interactions: List<ClocktowerHostInteraction> by lazy { state.nightInteractions(ruleset) }
    val allSeats: Set<Int> by lazy { state.allSeats() }
    val aliveSeats: Set<Int> by lazy { state.aliveSeats() }
    val redHerringSeat: Int? get() = state.redHerringSeat
    val redHerringSelectableSeats: Set<Int> by lazy { state.redHerringSelectableSeats() }
    val mayorRulingSeat: Int? by lazy { state.mayorRulingSeat() }
    val mayorRedirectSeats: Set<Int> by lazy { state.mayorRedirectEligibleSeats() }
    val livingMayorSeat: Int? by lazy { state.gameState.players.firstOrNull { it.alive && it.actualRole.value == "Mayor" }?.seat }
    val livingDemonSeat: Int? by lazy { state.livingDemonSeat() }
    val livingDemonPoisoned: Boolean by lazy { state.livingDemonPoisoned() }
    val minionSeats: List<Int> by lazy { state.actualTypeSeats(CharacterType.MINION) }
    val spySeat: Int? by lazy { state.actualRoleSeats("Spy").firstOrNull() }
    val fortuneTellerInPlay: Boolean by lazy { state.actualRoleSeats("Fortune Teller").isNotEmpty() }
    val evilInformationRequired: Boolean get() = state.firstNightEvilInformationRequired()
    val confirmedPoisonTargetSeat: Int? by lazy { state.confirmedPoisonTargetSeat(state.round) }
    val monkSeat: Int? get() = state.nightTargets.monkSeat
    val attackSeat: Int? get() = state.nightTargets.attackSeat
    val mayorRedirectSeat: Int? get() = state.nightTargets.mayorRedirectSeat
    /** The Undertaker's query: the most recent execution of a seated player. */
    val lastExecutedSeat: Int? by lazy {
        state.actionTimeline.entries.asReversed().firstNotNullOfOrNull { (it.fact as? ActionFact.Execution)?.targetSeat }
            ?.takeIf { state.gameState.playerAt(it) != null }
    }
    /** The living player who wakes for [role]; a Drunk wakes as their shown role. */
    fun wakingSeat(role: String): Int? =
        state.gameState.players.firstOrNull { it.alive && it.wakingRole() == RoleId(role) }?.seat
    fun livingNeighbourSeats(seat: Int): List<Int> = state.livingNeighbourSeats(seat)
    val empathNeighbourSeats: List<Int> by lazy { wakingSeat("Empath")?.let(::livingNeighbourSeats).orEmpty() }
    fun abilityState(role: String, seat: Int?): AbilityFunctioningState? =
        state.abilitySubjectForRole(role, seat)?.let { AbilityFunctioningSemantics.stateFor(it, role) }
    fun poisonedSeat(role: String): Int? = state.poisonedSeatForRole(role)
    fun roleStatus(role: String): ClocktowerRoleStatus = state.roleStatus(role)
    fun deathTriggerSeat(role: String): Int? = state.currentNightDeathTriggerSeat(role)
    fun deathTriggerAbilityState(role: String): AbilityFunctioningState? = state.deathTriggerAbilityState(role)
}
