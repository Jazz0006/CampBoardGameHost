package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics

/**
 * One knowledge-safe hypothetical Imp choice after a possible-world Monk protection branch.
 *
 * [possibleAttackTargetSeat] is a branch variable, never the Storyteller's actual hidden target.
 * Mayor redirection and Imp succession remain explicit unresolved outcomes for later A3 slices.
 */
internal data class EnumeratedWorldOtherNightAttackBranch(
    val protectionBranch: EnumeratedWorldOtherNightProtectionBranch,
    val possibleAttackTargetSeat: Int?,
    val outcome: DemonNightAttackOutcome,
)

internal object EnumeratedWorldOtherNightAttackBranching {
    fun branches(
        protectionBranch: EnumeratedWorldOtherNightProtectionBranch,
    ): List<EnumeratedWorldOtherNightAttackBranch> {
        val world = protectionBranch.world
        val impSeat = world.rolesBySeat.entries.singleOrNull { (_, role) ->
            role.value.equals("Imp", ignoreCase = true)
        }?.key ?: return listOf(noChoice(protectionBranch))

        val attacker = subject(world, impSeat)
        if (!AbilityFunctioningSemantics.interactsAs(attacker, "Imp")) {
            return listOf(noChoice(protectionBranch))
        }

        return world.rolesBySeat.keys.map { targetSeat ->
            val target = subject(world, targetSeat)
            EnumeratedWorldOtherNightAttackBranch(
                protectionBranch = protectionBranch,
                possibleAttackTargetSeat = targetSeat,
                outcome = DemonNightAttackSemantics.resolve(
                    DemonNightAttackContext(
                        attacker = attacker,
                        target = target,
                        targetIsAttacker = targetSeat == impSeat,
                        targetProtectedByFunctioningMonk =
                            protectionBranch.functioningMonkProtectedSeat == targetSeat,
                    ),
                ),
            )
        }
    }

    private fun noChoice(
        protectionBranch: EnumeratedWorldOtherNightProtectionBranch,
    ) = EnumeratedWorldOtherNightAttackBranch(
        protectionBranch = protectionBranch,
        possibleAttackTargetSeat = null,
        outcome = DemonNightAttackOutcome.NO_DEATH,
    )

    private fun subject(world: EnumeratedWorld, seat: Int) = AbilitySubject(
        actualRole = world.rolesBySeat.getValue(seat).value,
        shownRole = world.shownRolesBySeat[seat]?.value,
        isPoisoned = world.abilityStatesBySeat[seat] == AbilityState.MALFUNCTIONING_POISONED,
        isAlive = seat in world.aliveSeats,
    )
}
