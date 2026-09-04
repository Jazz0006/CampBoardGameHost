package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSlayerTableStateTest {
    @Test
    fun `claimant step exposes only unused claimant seats and locks the rest`() {
        val state = clocktowerSlayerTableState(
            seats = testSeats(),
            claimantCandidateNames = setOf("Player 2", "Player 5"),
            alivePlayerNames = setOf("Player 1", "Player 2", "Player 3", "Player 5"),
            claimantName = null,
            targetName = null,
        )

        assertTrue(state.choosingClaimant)
        assertEquals(
            setOf(ClocktowerSeatId(2), ClocktowerSeatId(5)),
            state.interaction.selectableSeatIds,
        )
        assertEquals(
            setOf(ClocktowerSeatId(1), ClocktowerSeatId(3), ClocktowerSeatId(4)),
            state.interaction.lockedSeatIds,
        )
        assertEquals(HostTableInteractionMode.Selection, state.interaction.mode)
    }

    @Test
    fun `target step keeps claimant first and allows only other living players`() {
        val state = clocktowerSlayerTableState(
            seats = testSeats(),
            claimantCandidateNames = setOf("Player 2", "Player 5"),
            alivePlayerNames = setOf("Player 1", "Player 2", "Player 3", "Player 5"),
            claimantName = "Player 2",
            targetName = null,
        )

        assertFalse(state.choosingClaimant)
        assertEquals(listOf(ClocktowerSeatId(2)), state.interaction.selectedSeatIds)
        assertEquals(
            setOf(ClocktowerSeatId(1), ClocktowerSeatId(3), ClocktowerSeatId(5)),
            state.interaction.selectableSeatIds,
        )
        assertEquals(setOf(ClocktowerSeatId(4)), state.interaction.lockedSeatIds)
        assertFalse(ClocktowerSeatId(2) in state.interaction.selectableSeatIds)
        assertEquals(HostTableInteractionMode.OrderedSelection, state.interaction.mode)
    }

    @Test
    fun `chosen target is second ordered selection while claimant remains stable`() {
        val state = clocktowerSlayerTableState(
            seats = testSeats(),
            claimantCandidateNames = setOf("Player 2", "Player 5"),
            alivePlayerNames = setOf("Player 1", "Player 2", "Player 3", "Player 5"),
            claimantName = "Player 2",
            targetName = "Player 5",
        )

        assertEquals(
            listOf(ClocktowerSeatId(2), ClocktowerSeatId(5)),
            state.interaction.selectedSeatIds,
        )
        assertEquals("Player 2", state.claimantName)
        assertEquals("Player 5", state.targetName)
        assertEquals("Player 5", state.playerNameForSeat(ClocktowerSeatId(5)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dead target is rejected instead of silently becoming selectable`() {
        clocktowerSlayerTableState(
            seats = testSeats(),
            claimantCandidateNames = setOf("Player 2"),
            alivePlayerNames = setOf("Player 1", "Player 2", "Player 3", "Player 5"),
            claimantName = "Player 2",
            targetName = "Player 4",
        )
    }

    private fun testSeats(): List<HostSeatPresentation> =
        (1..5).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = number != 4,
            )
        }
}
