package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedWorldMayorNightDeathBranchingTest {
    @Test
    fun `Mayor may die or redirect to every other seat while Demon safety still applies`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Mayor"),
                2 to RoleId("Empath"),
                3 to RoleId("Soldier"),
                4 to RoleId("Monk"),
                5 to RoleId("Poisoner"),
                6 to RoleId("Imp"),
            ),
            aliveSeats = setOf(1, 3, 4, 5, 6),
        )
        val attackBranch = mayorAttackBranch(
            world = world,
            functioningMonkProtectedSeat = 5,
        )

        val branches = EnumeratedWorldMayorNightDeathBranching.branches(attackBranch)

        assertEquals(setOf<Int?>(null, 2, 3, 4, 5, 6), branches.map { it.redirectTargetSeat }.toSet())
        assertEquals(
            setOf(3, 4, 5, 6),
            branches.single { it.redirectTargetSeat == null }.world.aliveSeats,
        )
        assertEquals(
            setOf(2, 3, 5),
            branches.filter { it.world == world }.mapNotNull { it.redirectTargetSeat }.toSet(),
        )
        assertEquals(
            setOf(1, 3, 5, 6),
            branches.single { it.redirectTargetSeat == 4 }.world.aliveSeats,
        )

        val bounceToImp = branches.single { it.redirectTargetSeat == 6 }
        assertEquals(setOf(1, 3, 4, 5), bounceToImp.world.aliveSeats)
        assertEquals(RoleId("Imp"), bounceToImp.world.currentRolesBySeat.getValue(5))
        assertEquals(RoleId("Imp"), bounceToImp.world.currentRolesBySeat.getValue(6))
    }

    @Test
    fun `redirected Poisoner death clears active poison state`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Mayor"),
                2 to RoleId("Empath"),
                3 to RoleId("Chef"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            abilityStatesBySeat = mapOf(2 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val attackBranch = mayorAttackBranch(world = world)

        val poisonerDeath = EnumeratedWorldMayorNightDeathBranching.branches(attackBranch)
            .single { it.redirectTargetSeat == 4 }

        assertEquals(setOf(1, 2, 3, 5), poisonerDeath.world.aliveSeats)
        assertEquals(emptyMap<Int, AbilityState>(), poisonerDeath.world.abilityStatesBySeat)
    }

    private fun mayorAttackBranch(
        world: EnumeratedWorld,
        functioningMonkProtectedSeat: Int? = null,
    ): EnumeratedWorldOtherNightAttackBranch {
        val protectionBranch = EnumeratedWorldOtherNightProtectionBranching.branches(world)
            .single { it.functioningMonkProtectedSeat == functioningMonkProtectedSeat }
        return EnumeratedWorldOtherNightAttackBranching.branches(protectionBranch)
            .single { branch ->
                branch.possibleAttackTargetSeat == 1 &&
                    branch.outcome == DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
            }
    }
}
