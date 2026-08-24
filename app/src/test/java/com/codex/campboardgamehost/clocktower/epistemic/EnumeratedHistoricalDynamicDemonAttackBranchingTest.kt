package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedHistoricalDynamicDemonAttackBranchingTest {
    @Test
    fun `H7 hidden attack branching uses the living current Demon rather than setup Imp identity`() {
        val setupRoles = linkedMapOf(
            1 to RoleId("Empath"),
            2 to RoleId("Chef"),
            3 to RoleId("Scarlet Woman"),
            4 to RoleId("Imp"),
            5 to RoleId("Poisoner"),
        )
        val world = EnumeratedWorld(
            rolesBySeat = setupRoles,
            currentRolesBySeat = setupRoles +
                (3 to RoleId("Imp")) +
                (4 to RoleId("Imp")),
            aliveSeats = setOf(1, 2, 3, 5),
        )
        val protection = EnumeratedWorldOtherNightProtectionBranch(
            world = world,
            functioningMonkProtectedSeat = null,
        )

        val branches = EnumeratedWorldOtherNightAttackBranching.branches(protection)

        assertEquals(5, branches.size)
        assertEquals(
            DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
            branches.single { it.possibleAttackTargetSeat == 3 }.outcome,
        )
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            branches.single { it.possibleAttackTargetSeat == 4 }.outcome,
        )
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            branches.single { it.possibleAttackTargetSeat == 1 }.outcome,
        )
    }
}
