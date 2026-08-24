package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumeratedWorldOtherNightAttackBranchingTest {
    @Test
    fun `functioning Imp branches every target through Monk protection and self kill precedence`() {
        val protection = EnumeratedWorldOtherNightProtectionBranch(
            world = world("Empath", "Chef", "Monk", "Poisoner", "Imp"),
            functioningMonkProtectedSeat = 1,
        )

        val branches = EnumeratedWorldOtherNightAttackBranching.branches(protection)

        assertEquals(5, branches.size)
        assertEquals(
            mapOf(
                1 to DemonNightAttackOutcome.NO_DEATH,
                2 to DemonNightAttackOutcome.TARGET_DIES,
                3 to DemonNightAttackOutcome.TARGET_DIES,
                4 to DemonNightAttackOutcome.TARGET_DIES,
                5 to DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
            ),
            branches.associate { it.possibleAttackTargetSeat to it.outcome },
        )
    }

    @Test
    fun `dead player remains a legal attack choice but cannot die again`() {
        val protection = EnumeratedWorldOtherNightProtectionBranch(
            world = world(
                "Empath", "Chef", "Monk", "Poisoner", "Imp",
                aliveSeats = setOf(1, 3, 4, 5),
            ),
            functioningMonkProtectedSeat = null,
        )

        val branches = EnumeratedWorldOtherNightAttackBranching.branches(protection)

        assertEquals(5, branches.size)
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            branches.single { it.possibleAttackTargetSeat == 2 }.outcome,
        )
    }

    @Test
    fun `alive poisoned Imp still branches hidden choices but every direct outcome is no death`() {
        val protection = EnumeratedWorldOtherNightProtectionBranch(
            world = world(
                "Empath", "Chef", "Monk", "Poisoner", "Imp",
                abilityStatesBySeat = mapOf(5 to AbilityState.MALFUNCTIONING_POISONED),
            ),
            functioningMonkProtectedSeat = null,
        )

        val branches = EnumeratedWorldOtherNightAttackBranching.branches(protection)

        assertEquals(5, branches.size)
        assertEquals(setOf(DemonNightAttackOutcome.NO_DEATH), branches.map { it.outcome }.toSet())
    }

    @Test
    fun `no living Imp produces one no choice no death branch`() {
        val protection = EnumeratedWorldOtherNightProtectionBranch(
            world = world(
                "Empath", "Chef", "Monk", "Poisoner", "Imp",
                aliveSeats = setOf(1, 2, 3, 4),
            ),
            functioningMonkProtectedSeat = null,
        )

        val branch = EnumeratedWorldOtherNightAttackBranching.branches(protection).single()

        assertNull(branch.possibleAttackTargetSeat)
        assertEquals(DemonNightAttackOutcome.NO_DEATH, branch.outcome)
    }

    @Test
    fun `target role and poison state are carried into shared Demon attack semantics`() {
        val functioningTargets = EnumeratedWorldOtherNightProtectionBranch(
            world = world("Soldier", "Mayor", "Monk", "Poisoner", "Imp"),
            functioningMonkProtectedSeat = null,
        )
        val poisonedSoldier = EnumeratedWorldOtherNightProtectionBranch(
            world = world(
                "Soldier", "Mayor", "Monk", "Poisoner", "Imp",
                abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_POISONED),
            ),
            functioningMonkProtectedSeat = null,
        )

        val functioning = EnumeratedWorldOtherNightAttackBranching.branches(functioningTargets)
        val poisoned = EnumeratedWorldOtherNightAttackBranching.branches(poisonedSoldier)

        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            functioning.single { it.possibleAttackTargetSeat == 1 }.outcome,
        )
        assertEquals(
            DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
            functioning.single { it.possibleAttackTargetSeat == 2 }.outcome,
        )
        assertEquals(
            DemonNightAttackOutcome.TARGET_DIES,
            poisoned.single { it.possibleAttackTargetSeat == 1 }.outcome,
        )
    }

    private fun world(
        vararg roles: String,
        aliveSeats: Set<Int> = roles.indices.map { it + 1 }.toSet(),
        abilityStatesBySeat: Map<Int, AbilityState> = emptyMap(),
    ): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = roles.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(),
        aliveSeats = aliveSeats,
        abilityStatesBySeat = abilityStatesBySeat,
    )
}
