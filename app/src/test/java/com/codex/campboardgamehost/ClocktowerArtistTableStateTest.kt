package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerArtistTableStateTest {
    @Test
    fun `artist table keeps stable physical seats while exposing only eligible claimants`() {
        val seats = testSeats(8)
        val state = clocktowerArtistTableState(
            seats = seats,
            claimantCandidateNames = setOf("Player 2", "Player 5", "Player 8"),
            claimantName = null,
        )

        assertEquals((1..8).map(::ClocktowerSeatId), state.seats.map(HostSeatPresentation::seatId))
        assertEquals(
            setOf(ClocktowerSeatId(2), ClocktowerSeatId(5), ClocktowerSeatId(8)),
            state.interaction.selectableSeatIds,
        )
        assertEquals(HostTableInteractionMode.Selection, state.interaction.mode)
        assertTrue(state.interaction.selectedSeatIds.isEmpty())
    }

    @Test
    fun `selected artist claimant remains typed and highlighted without changing eligibility`() {
        val state = clocktowerArtistTableState(
            seats = testSeats(7),
            claimantCandidateNames = setOf("Player 1", "Player 4", "Player 6"),
            claimantName = "Player 4",
        )

        assertEquals(ClocktowerSeatId(4), state.claimantSeatId)
        assertEquals("Player 4", state.claimantName)
        assertEquals(listOf(ClocktowerSeatId(4)), state.interaction.selectedSeatIds)
        assertEquals(
            setOf(ClocktowerSeatId(1), ClocktowerSeatId(4), ClocktowerSeatId(6)),
            state.interaction.selectableSeatIds,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown artist claimant candidate is rejected instead of being reindexed`() {
        clocktowerArtistTableState(
            seats = testSeats(6),
            claimantCandidateNames = setOf("Player 2", "Missing"),
            claimantName = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selected artist claimant must remain eligible`() {
        clocktowerArtistTableState(
            seats = testSeats(6),
            claimantCandidateNames = setOf("Player 2", "Player 5"),
            claimantName = "Player 3",
        )
    }

    private fun testSeats(count: Int): List<HostSeatPresentation> =
        (1..count).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = true,
            )
        }
}
