package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumeratedWorldOtherNightMechanicsMaterializerTest {
    @Test
    fun `resolved direct outcomes converge while unresolved self kill branches remain explicit`() {
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
            ),
            result.resolvedWorlds.map { it.aliveSeats }.toSet(),
        )
        assertEquals(
            setOf(DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED),
            result.unresolvedBranches.map { it.outcome }.toSet(),
        )
        assertEquals(
            setOf(1, 2, 4),
            result.unresolvedBranches.map {
                it.protectionBranch.functioningMonkProtectedSeat
            }.toSet(),
        )
        assertEquals(
            setOf(5),
            result.unresolvedBranches.mapNotNull { it.possibleAttackTargetSeat }.toSet(),
        )
    }
}
