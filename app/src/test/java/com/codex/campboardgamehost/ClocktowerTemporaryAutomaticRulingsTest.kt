package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClocktowerTemporaryAutomaticRulingsTest {
    @Test
    fun `mayor automatic redirect candidates keep only other living townsfolk`() {
        val candidates = listOf(
            ClocktowerTemporaryMayorCandidate(seat = 1, team = ClocktowerTeam.Townsfolk, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 2, team = ClocktowerTeam.Townsfolk, alive = false),
            ClocktowerTemporaryMayorCandidate(seat = 3, team = ClocktowerTeam.Outsider, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 4, team = ClocktowerTeam.Minion, alive = true),
            ClocktowerTemporaryMayorCandidate(seat = 9, team = ClocktowerTeam.Townsfolk, alive = true),
        )

        assertEquals(
            listOf(1),
            clocktowerTemporaryMayorEligibleTownsfolkSeats(
                candidates = candidates,
                mayorSeat = 9,
            ),
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
            clocktowerTemporaryMayorEligibleTownsfolkSeats(
                candidates = candidates.reversed(),
                mayorSeat = 4,
            ),
        )
    }

    @Test
    fun `night ruling decision key excludes mutable revision and separates decision families`() {
        val mayorKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "mayor-redirect",
        )
        val sameMayorKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "mayor-redirect",
        )
        val successionKey = clocktowerTemporaryNightDecisionKey(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 11,
            family = "demon-succession",
        )

        assertEquals(mayorKey, sameMayorKey)
        assertNotEquals(mayorKey, successionKey)
        assertEquals(
            clocktowerTemporaryAutomaticDecisionSeed(mayorKey),
            clocktowerTemporaryAutomaticDecisionSeed(sameMayorKey),
        )
        assertNotEquals(
            clocktowerTemporaryAutomaticDecisionSeed(mayorKey),
            clocktowerTemporaryAutomaticDecisionSeed(successionKey),
        )
    }
}
