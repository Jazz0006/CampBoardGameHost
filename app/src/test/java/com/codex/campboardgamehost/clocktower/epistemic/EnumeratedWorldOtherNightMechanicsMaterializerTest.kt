package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedWorldOtherNightMechanicsMaterializerTest {
    @Test
    fun `self kill succession materializes and converges with direct outcomes`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Monk"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
        )

        val result = EnumeratedWorldOtherNightMechanicsMaterializer.materialize(world)

        assertEquals(
            setOf(
                setOf(1, 2, 3, 4, 5),
                setOf(2, 3, 4, 5),
                setOf(1, 3, 4, 5),
                setOf(1, 2, 4, 5),
                setOf(1, 2, 3, 5),
                setOf(1, 2, 3, 4),
            ),
            result.resolvedWorlds.map { it.aliveSeats }.toSet(),
        )
        assertEquals(emptyList<EnumeratedWorldOtherNightAttackBranch>(), result.unresolvedBranches)

        val selfKillWorld = result.resolvedWorlds.single { 5 !in it.aliveSeats }
        assertEquals(RoleId("Imp"), selfKillWorld.currentRolesBySeat.getValue(4))
        assertEquals(RoleId("Imp"), selfKillWorld.currentRolesBySeat.getValue(5))
    }

    @Test
    fun `Mayor redirect materializes and converges with direct attack and self kill outcomes`() {
        val world = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Chef"),
                3 to RoleId("Mayor"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
        )

        val result = EnumeratedWorldOtherNightMechanicsMaterializer.materialize(world)

        assertEquals(emptyList<EnumeratedWorldOtherNightAttackBranch>(), result.unresolvedBranches)
        assertEquals(
            setOf(
                setOf(2, 3, 4, 5),
                setOf(1, 3, 4, 5),
                setOf(1, 2, 4, 5),
                setOf(1, 2, 3, 5),
                setOf(1, 2, 3, 4),
            ),
            result.resolvedWorlds.map { it.aliveSeats }.toSet(),
        )
        assertEquals(5, result.resolvedWorlds.size)

        val selfKillWorld = result.resolvedWorlds.single { 5 !in it.aliveSeats }
        assertEquals(RoleId("Imp"), selfKillWorld.currentRolesBySeat.getValue(4))
        assertEquals(RoleId("Imp"), selfKillWorld.currentRolesBySeat.getValue(5))
    }
}
