package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedWorldOtherNightProtectionBranchingTest {
    @Test
    fun `functioning living Monk branches effective protection across every other player`() {
        val world = world(
            roles = listOf("Empath", "Chef", "Monk", "Poisoner", "Imp"),
            aliveSeats = setOf(1, 3, 4, 5),
        )

        val branches = EnumeratedWorldOtherNightProtectionBranching.branches(world)

        assertEquals(
            setOf(1, 2, 4, 5),
            branches.mapNotNull { it.functioningMonkProtectedSeat }.toSet(),
        )
        assertEquals(4, branches.size)
    }

    @Test
    fun `former setup Monk that is no longer current Monk has no effective protection branch`() {
        val setupWorld = world(
            roles = listOf("Empath", "Chef", "Monk", "Poisoner", "Imp"),
        )
        val world = setupWorld.withCurrentRoles(
            setupWorld.currentRolesBySeat + (3 to RoleId("Soldier")),
        )

        assertEquals(
            listOf(null),
            EnumeratedWorldOtherNightProtectionBranching.branches(world)
                .map { it.functioningMonkProtectedSeat },
        )
    }

    @Test
    fun `poisoned or dead Monk has no effective protection branch`() {
        val poisoned = world(
            roles = listOf("Empath", "Chef", "Monk", "Poisoner", "Imp"),
            abilityStatesBySeat = mapOf(3 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val dead = world(
            roles = listOf("Empath", "Chef", "Monk", "Poisoner", "Imp"),
            aliveSeats = setOf(1, 2, 4, 5),
        )

        assertEquals(
            listOf(null),
            EnumeratedWorldOtherNightProtectionBranching.branches(poisoned)
                .map { it.functioningMonkProtectedSeat },
        )
        assertEquals(
            listOf(null),
            EnumeratedWorldOtherNightProtectionBranching.branches(dead)
                .map { it.functioningMonkProtectedSeat },
        )
    }

    @Test
    fun `Drunk shown as Monk never creates functioning protection`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Drunk"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            shownRolesBySeat = mapOf(3 to RoleId("Monk")),
            abilityStatesBySeat = mapOf(3 to AbilityState.MALFUNCTIONING_DRUNK),
        )

        assertEquals(
            listOf(null),
            EnumeratedWorldOtherNightProtectionBranching.branches(world)
                .map { it.functioningMonkProtectedSeat },
        )
    }

    @Test
    fun `world without Monk has one no protection branch`() {
        val world = world(
            roles = listOf("Empath", "Chef", "Fortune Teller", "Poisoner", "Imp"),
        )

        assertEquals(
            listOf(null),
            EnumeratedWorldOtherNightProtectionBranching.branches(world)
                .map { it.functioningMonkProtectedSeat },
        )
    }

    private fun world(
        roles: List<String>,
        aliveSeats: Set<Int> = roles.indices.map { it + 1 }.toSet(),
        abilityStatesBySeat: Map<Int, AbilityState> = emptyMap(),
    ): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = roles.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(),
        aliveSeats = aliveSeats,
        abilityStatesBySeat = abilityStatesBySeat,
    )
}
