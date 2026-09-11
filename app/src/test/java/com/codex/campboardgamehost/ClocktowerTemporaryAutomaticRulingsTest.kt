package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClocktowerTemporaryAutomaticRulingsTest {
    @Test
    fun `mayor automatic redirect candidates keep only living townsfolk`() {
        val candidates = listOf(
            ClocktowerTemporaryMayorCandidate(seat = 1, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 2, team = ClocktowerTeam.Townsfolk, alive = false),
            ClocktowerTemporaryMayorCandidate(seat = 3, team = ClocktowerTeam.Outsider, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 4, team = ClocktowerTeam.Minion, alive = true),
        )

        assertEquals(
            listOf(1),
            clocktowerTemporaryMayorEligibleTownsfolkSeats(candidates),
        )
    }

    @Test
    fun `mayor automatic redirect candidate projection is order independent`() {
        val candidates = listOf(
            ClocktowerTemporaryMayorCandidate(seat = 7, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 2, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 5, team = ClocktowerTeam.Outsider, alive = true),
        )

        assertEquals(
            listOf(2, 7),
            clocktowerTemporaryMayorEligibleTownsfolkSeats(candidates.reversed()),
        )
    }

    @Test
    fun `temporary automatic decision key is stable but decision families remain independent`() {
        val base = "game-1|Night|2|11|5|3"

        assertEquals(
            clocktowerTemporaryAutomaticDecisionSeed("$base|mayor-redirect"),
            clocktowerTemporaryAutomaticDecisionSeed("$base|mayor-redirect"),
        )
        assertNotEquals(
            clocktowerTemporaryAutomaticDecisionSeed("$base|mayor-redirect"),
            clocktowerTemporaryAutomaticDecisionSeed("$base|demon-succession"),
        )
    }
}
