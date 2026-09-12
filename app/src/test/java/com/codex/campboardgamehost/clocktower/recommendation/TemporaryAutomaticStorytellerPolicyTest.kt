package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TemporaryAutomaticStorytellerPolicyTest {
    @Test
    fun `registration uses ninety percent special and ten percent actual with equal special identities`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectRegistration(
            actual = TemporaryAutomaticChoice("actual", "actual"),
            special = listOf(
                TemporaryAutomaticChoice("fake-chef", "Chef"),
                TemporaryAutomaticChoice("fake-empath", "Empath"),
            ),
            decisionSeed = 17L,
        )

        assertEquals(
            mapOf(
                "actual" to 100_000L,
                "fake-chef" to 450_000L,
                "fake-empath" to 450_000L,
            ),
            result.finalProbabilityByCandidate,
        )
    }

    @Test
    fun `registration falls back to actual when no special registration is legal`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectRegistration(
            actual = TemporaryAutomaticChoice("actual", "actual"),
            special = emptyList(),
            decisionSeed = 17L,
        )

        assertEquals("actual", result.selected.candidateId)
        assertEquals(mapOf("actual" to 1_000_000L), result.finalProbabilityByCandidate)
    }

    @Test
    fun `mayor redirects ninety percent across living townsfolk and dies ten percent`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
            mayorSeat = 4,
            livingTownsfolkSeats = listOf(1, 2, 7),
            decisionSeed = 99L,
        )

        assertEquals(
            mapOf(
                "mayor-dies:4" to 100_000L,
                "townsfolk-redirect:1" to 300_000L,
                "townsfolk-redirect:2" to 300_000L,
                "townsfolk-redirect:7" to 300_000L,
            ),
            result.finalProbabilityByCandidate,
        )
    }

    @Test
    fun `mayor dies when no living townsfolk redirect exists`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
            mayorSeat = 4,
            livingTownsfolkSeats = emptyList(),
            decisionSeed = 99L,
        )

        assertEquals(4, result.selected.payload)
        assertEquals(mapOf("mayor-dies:4" to 1_000_000L), result.finalProbabilityByCandidate)
    }

    @Test
    fun `demon successor weights baron scarlet woman spy poisoner four three two one`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectDemonSuccessor(
            eligible = listOf(
                TemporaryDemonSuccessorChoice(1, RoleId("Baron")),
                TemporaryDemonSuccessorChoice(2, RoleId("Scarlet Woman")),
                TemporaryDemonSuccessorChoice(3, RoleId("Spy")),
                TemporaryDemonSuccessorChoice(4, RoleId("Poisoner")),
            ),
            decisionSeed = 123L,
        )

        assertNotNull(result)
        assertEquals(
            mapOf(
                "successor:1:Baron" to 400_000L,
                "successor:2:Scarlet Woman" to 300_000L,
                "successor:3:Spy" to 200_000L,
                "successor:4:Poisoner" to 100_000L,
            ),
            result!!.finalProbabilityByCandidate,
        )
    }

    @Test
    fun `demon successor renormalizes weights when only some legal minions remain`() {
        val result = TemporaryAutomaticStorytellerPolicy.selectDemonSuccessor(
            eligible = listOf(
                TemporaryDemonSuccessorChoice(1, RoleId("Baron")),
                TemporaryDemonSuccessorChoice(4, RoleId("Poisoner")),
            ),
            decisionSeed = 123L,
        )

        assertNotNull(result)
        assertEquals(
            mapOf(
                "successor:1:Baron" to 800_000L,
                "successor:4:Poisoner" to 200_000L,
            ),
            result!!.finalProbabilityByCandidate,
        )
    }

    @Test
    fun `same decision seed gives the same automatic ruling`() {
        fun select() = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
            mayorSeat = 4,
            livingTownsfolkSeats = listOf(1, 2, 7),
            decisionSeed = 555_123L,
        )

        val first = select()
        val second = select()

        assertEquals(first.selected, second.selected)
        assertEquals(first.finalProbabilityByCandidate, second.finalProbabilityByCandidate)
        assertEquals(first.decisionSeed, second.decisionSeed)
    }
}
