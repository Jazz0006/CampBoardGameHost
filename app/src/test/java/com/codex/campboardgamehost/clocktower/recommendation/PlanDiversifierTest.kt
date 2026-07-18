package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanDiversifierTest {
    @Test
    fun `identical effect signatures have maximum similarity`() {
        val signature = PlanEffectSignature(
            redHerringSeat = 5,
            drunkShownRole = RoleId("Investigator"),
            drunkInvestigatorShownMinion = RoleId("Poisoner"),
            suspectedSeats = setOf(1, 4),
            demonBluffs = setOf(RoleId("Monk"), RoleId("Soldier"), RoleId("Butler")),
        )

        assertEquals(100, PlanDiversifier.similarityPercent(signature, signature))
    }

    @Test
    fun `different decisions reduce similarity`() {
        val first = PlanEffectSignature(
            redHerringSeat = 5,
            drunkShownRole = RoleId("Investigator"),
            drunkInvestigatorShownMinion = RoleId("Poisoner"),
            suspectedSeats = setOf(1, 4),
            demonBluffs = setOf(RoleId("Monk"), RoleId("Soldier"), RoleId("Butler")),
        )
        val second = PlanEffectSignature(
            redHerringSeat = 2,
            drunkShownRole = RoleId("Monk"),
            suspectedSeats = emptySet(),
            demonBluffs = setOf(RoleId("Investigator"), RoleId("Soldier"), RoleId("Butler")),
        )

        assertTrue(PlanDiversifier.similarityPercent(first, second) < 50)
    }
}
