package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/**
 * One knowledge-safe mechanical branch at the Monk slot of an Other Night.
 *
 * [functioningMonkProtectedSeat] records only an effective protection. It is deliberately not the
 * Storyteller-selected Protect target: a dead, poisoned, or Drunk-shown-as-Monk actor collapses to
 * the same null mechanical branch because their hidden choice cannot protect anyone.
 */
internal data class EnumeratedWorldOtherNightProtectionBranch(
    val world: EnumeratedWorld,
    val functioningMonkProtectedSeat: Int?,
)

internal object EnumeratedWorldOtherNightProtectionBranching {
    fun branches(world: EnumeratedWorld): List<EnumeratedWorldOtherNightProtectionBranch> {
        val monkSeat = world.rolesBySeat.entries.singleOrNull { (_, role) ->
            role.value.equals("Monk", ignoreCase = true)
        }?.key ?: return listOf(branch(world, null))

        val monk = subject(world, monkSeat)
        if (!AbilityFunctioningSemantics.functionsAs(monk, "Monk")) {
            return listOf(branch(world, null))
        }

        return world.rolesBySeat.keys
            .filter { seat -> seat != monkSeat }
            .map { protectedSeat -> branch(world, protectedSeat) }
    }

    private fun branch(
        world: EnumeratedWorld,
        protectedSeat: Int?,
    ) = EnumeratedWorldOtherNightProtectionBranch(
        world = world,
        functioningMonkProtectedSeat = protectedSeat,
    )

    private fun subject(world: EnumeratedWorld, seat: Int) = AbilitySubject(
        actualRole = world.rolesBySeat.getValue(seat).value,
        shownRole = world.shownRolesBySeat[seat]?.value,
        isPoisoned = world.abilityStatesBySeat[seat] == AbilityState.MALFUNCTIONING_POISONED,
        isAlive = seat in world.aliveSeats,
    )
}
