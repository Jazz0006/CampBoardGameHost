package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
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
    fun `Mayor redirect remains the only unresolved outcome after self kill succession materializes`() {
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

        assertEquals(
            setOf(DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED),
            result.unresolvedBranches.map { it.outcome }.toSet(),
        )
        assertEquals(
            setOf(3),
            result.unresolvedBranches.mapNotNull { it.possibleAttackTargetSeat }.toSet(),
        )
        assertEquals(
            setOf(
                setOf(2, 3, 4, 5),
                setOf(1, 3, 4, 5),
                setOf(1, 2, 3, 5),
                setOf(1, 2, 3, 4),
            ),
            result.resolvedWorlds.map { it.aliveSeats }.toSet(),
        )
    }
}
