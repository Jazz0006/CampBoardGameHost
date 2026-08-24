package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EnumeratedWorldImpSelfKillSuccessionBranchingTest {
    @Test
    fun `functioning Scarlet Woman is the forced successor when five or more players are alive`() {
        val world = tenPlayerWorld()

        val branches = EnumeratedWorldImpSelfKillSuccessionBranching.branches(world)

        val branch = branches.single()
        assertEquals(9, branch.successorSeat)
        assertFalse(10 in branch.world.aliveSeats)
        assertEquals(RoleId("Imp"), branch.world.currentRolesBySeat.getValue(9))
        assertEquals(RoleId("Imp"), branch.world.currentRolesBySeat.getValue(10))
    }

    @Test
    fun `poisoned Scarlet Woman loses forced priority and every living current Minion can inherit`() {
        val world = tenPlayerWorld(
            abilityStatesBySeat = mapOf(9 to AbilityState.MALFUNCTIONING_POISONED),
        )

        val branches = EnumeratedWorldImpSelfKillSuccessionBranching.branches(world)

        assertEquals(setOf(8, 9), branches.mapNotNull { it.successorSeat }.toSet())
        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7, 8, 9), branches.flatMap { it.world.aliveSeats }.toSet())
        assertEquals(
            emptyMap<Int, AbilityState>(),
            branches.single { it.successorSeat == 8 }.world.abilityStatesBySeat,
        )
        assertEquals(
            mapOf(9 to AbilityState.MALFUNCTIONING_POISONED),
            branches.single { it.successorSeat == 9 }.world.abilityStatesBySeat,
        )
    }

    @Test
    fun `self kill with no living Minion has one no successor branch with the Imp dead`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Fortune Teller"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            aliveSeats = setOf(1, 2, 3, 5),
        )

        val branch = EnumeratedWorldImpSelfKillSuccessionBranching.branches(world).single()

        assertEquals(null, branch.successorSeat)
        assertEquals(setOf(1, 2, 3), branch.world.aliveSeats)
        assertEquals(world.currentRolesBySeat, branch.world.currentRolesBySeat)
    }

    private fun tenPlayerWorld(
        abilityStatesBySeat: Map<Int, AbilityState> = emptyMap(),
    ) = EnumeratedWorld(
        rolesBySeat = linkedMapOf(
            1 to RoleId("Empath"),
            2 to RoleId("Chef"),
            3 to RoleId("Fortune Teller"),
            4 to RoleId("Monk"),
            5 to RoleId("Soldier"),
            6 to RoleId("Undertaker"),
            7 to RoleId("Ravenkeeper"),
            8 to RoleId("Poisoner"),
            9 to RoleId("Scarlet Woman"),
            10 to RoleId("Imp"),
        ),
        abilityStatesBySeat = abilityStatesBySeat,
    )
}
