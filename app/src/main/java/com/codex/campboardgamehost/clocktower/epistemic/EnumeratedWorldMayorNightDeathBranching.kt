package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome

/**
 * One rule-derived mechanical resolution of a functioning Mayor being hit by the current Imp.
 *
 * [redirectTargetSeat] is null when the Mayor is allowed to die. A non-null value is a hypothetical
 * Storyteller redirect generated from the possible world itself, never the persisted hidden target.
 */
internal data class EnumeratedWorldMayorNightDeathBranch(
    val redirectTargetSeat: Int?,
    val world: EnumeratedWorld,
)

/** Trouble Brewing Mayor night-death branching before materializer integration. */
internal object EnumeratedWorldMayorNightDeathBranching {
    fun branches(
        attackBranch: EnumeratedWorldOtherNightAttackBranch,
    ): List<EnumeratedWorldMayorNightDeathBranch> {
        require(attackBranch.outcome == DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED) {
            "Mayor night-death branching requires a Mayor redirect choice outcome."
        }

        val world = attackBranch.protectionBranch.world
        val mayorSeat = requireNotNull(attackBranch.possibleAttackTargetSeat) {
            "Mayor redirect choice must identify the attacked Mayor seat."
        }
        require(
            world.currentRolesBySeat.getValue(mayorSeat).value.equals("Mayor", ignoreCase = true) &&
                mayorSeat in world.aliveSeats &&
                AbilityFunctioningSemantics.functionsAs(subject(world, mayorSeat), "Mayor"),
        ) { "Mayor redirect choice requires a living functioning current Mayor." }

        val impSeat = requireNotNull(
            world.currentRolesBySeat.entries.singleOrNull { (seat, role) ->
                seat in world.aliveSeats && role.value.equals("Imp", ignoreCase = true)
            }?.key,
        ) { "Mayor redirect choice requires exactly one living current Imp." }

        val mayorDies = EnumeratedWorldMayorNightDeathBranch(
            redirectTargetSeat = null,
            world = materializeDeath(world, mayorSeat),
        )
        val redirects = world.rolesBySeat.keys
            .filter { seat -> seat != mayorSeat }
            .flatMap { redirectTargetSeat ->
                redirectWorlds(
                    world = world,
                    impSeat = impSeat,
                    redirectTargetSeat = redirectTargetSeat,
                    functioningMonkProtectedSeat =
                        attackBranch.protectionBranch.functioningMonkProtectedSeat,
                ).map { redirectedWorld ->
                    EnumeratedWorldMayorNightDeathBranch(
                        redirectTargetSeat = redirectTargetSeat,
                        world = redirectedWorld,
                    )
                }
            }

        return listOf(mayorDies) + redirects
    }

    private fun redirectWorlds(
        world: EnumeratedWorld,
        impSeat: Int,
        redirectTargetSeat: Int,
        functioningMonkProtectedSeat: Int?,
    ): List<EnumeratedWorld> {
        if (redirectTargetSeat !in world.aliveSeats) return listOf(world)
        if (redirectTargetSeat == functioningMonkProtectedSeat) return listOf(world)
        if (AbilityFunctioningSemantics.functionsAs(subject(world, redirectTargetSeat), "Soldier")) {
            return listOf(world)
        }
        if (redirectTargetSeat == impSeat) {
            return EnumeratedWorldImpSelfKillSuccessionBranching.branches(world).map { it.world }
        }
        return listOf(materializeDeath(world, redirectTargetSeat))
    }

    private fun materializeDeath(
        world: EnumeratedWorld,
        targetSeat: Int,
    ): EnumeratedWorld {
        val abilityStates = if (
            world.currentRolesBySeat.getValue(targetSeat).value.equals("Poisoner", ignoreCase = true)
        ) {
            world.abilityStatesBySeat.filterValues { it != AbilityState.MALFUNCTIONING_POISONED }
        } else {
            world.abilityStatesBySeat
        }
        return world.copy(
            aliveSeats = world.aliveSeats - targetSeat,
            abilityStatesBySeat = abilityStates,
        )
    }

    private fun subject(world: EnumeratedWorld, seat: Int) = AbilitySubject(
        actualRole = world.currentRolesBySeat.getValue(seat).value,
        shownRole = world.shownRolesBySeat[seat]?.value,
        isPoisoned = world.abilityStatesBySeat[seat] == AbilityState.MALFUNCTIONING_POISONED,
        isAlive = seat in world.aliveSeats,
    )
}
