package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Player gesture structure, never Slayer ability eligibility or hidden rules. */
class ClocktowerSlayerTableStateTest {
    @Test fun `claimant step exposes every known physical seat regardless of a legacy role menu`() {
        val state = clocktowerSlayerTableState(testSeats(), null, null)
        assertTrue(state.choosingClaimant)
        assertEquals((1..5).map(::ClocktowerSeatId).toSet(), state.interaction.selectableSeatIds)
        assertTrue(state.interaction.lockedSeatIds.isEmpty())
        assertEquals(HostTableInteractionMode.Selection, state.interaction.mode)
    }

    @Test fun `target follows claimant in stable ordered selection`() {
        val state = clocktowerSlayerTableState(testSeats(), "Player 2", "Player 5")
        assertFalse(state.choosingClaimant)
        assertEquals(listOf(ClocktowerSeatId(2), ClocktowerSeatId(5)),
            state.interaction.selectedSeatIds)
        assertEquals("Player 2", state.claimantName)
        assertEquals("Player 5", state.targetName)
        assertEquals("Player 5", state.playerNameForSeat(ClocktowerSeatId(5)))
        assertFalse(ClocktowerSeatId(2) in state.interaction.selectableSeatIds)
        assertEquals(HostTableInteractionMode.OrderedSelection, state.interaction.mode)
    }

    @Test fun `dead seat can be recorded as a public target without Host legality rejection`() {
        val state = clocktowerSlayerTableState(testSeats(), "Player 2", "Player 4")
        assertEquals("Player 4", state.targetName)
        assertEquals(ClocktowerSeatId(4), state.targetSeatId)
        assertTrue(ClocktowerSeatId(4) in state.interaction.selectedSeatIds)
    }

    @Test fun `only structural unknown and duplicate seats are rejected`() {
        for (pair in listOf("Unknown" to null, "Player 2" to "Unknown",
            "Player 2" to "Player 2", null to "Player 3")) {
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                clocktowerSlayerTableState(testSeats(), pair.first, pair.second)
            }
        }
    }

    private fun testSeats(): List<HostSeatPresentation> = (1..5).map { number ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(number),
            playerName = "Player $number",
            isAlive = number != 4,
        )
    }
}

