package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ClocktowerHostTableContractTest {
    @Test
    fun `same seat ids keep the same spatial slots across phase input orderings`() {
        val setupSeats = seats(8)
        val daySeats = setupSeats.reversed()
        val nightSeats = setupSeats.drop(3) + setupSeats.take(3)

        val setupPlacements = placementsBySeat(setupSeats)
        val dayPlacements = placementsBySeat(daySeats)
        val nightPlacements = placementsBySeat(nightSeats)

        assertEquals(setupPlacements, dayPlacements)
        assertEquals(setupPlacements, nightPlacements)
    }

    @Test
    fun `interaction state never filters or repositions the physical seats`() {
        val allSeats = seats(7)
        val passiveFrames = hostTableSeatFrames(
            seats = allSeats,
            interaction = HostTableInteractionState(),
        )
        val activeFrames = hostTableSeatFrames(
            seats = allSeats,
            interaction = HostTableInteractionState(
                mode = HostTableInteractionMode.Selection,
                selectableSeatIds = setOf(ClocktowerSeatId(2), ClocktowerSeatId(5)),
                selectedSeatIds = listOf(ClocktowerSeatId(5)),
                highlightedSeatIds = setOf(ClocktowerSeatId(3)),
                currentSeatId = ClocktowerSeatId(2),
                lockedSeatIds = setOf(ClocktowerSeatId(7)),
            ),
        )

        assertEquals(
            passiveFrames.associate { it.seat.seatId to it.placement },
            activeFrames.associate { it.seat.seatId to it.placement },
        )
        assertEquals(allSeats.size, activeFrames.size)

        val byId = activeFrames.associateBy { it.seat.seatId }
        assertTrue(byId.getValue(ClocktowerSeatId(5)).isSelected)
        assertTrue(byId.getValue(ClocktowerSeatId(3)).isHighlighted)
        assertTrue(byId.getValue(ClocktowerSeatId(2)).isCurrent)
        assertTrue(byId.getValue(ClocktowerSeatId(7)).isLocked)
    }

    @Test
    fun `seat identity is independent from duplicate player names`() {
        val duplicateNames = listOf(
            HostSeatPresentation(ClocktowerSeatId(1), playerName = "Alex", isAlive = true),
            HostSeatPresentation(ClocktowerSeatId(2), playerName = "Alex", isAlive = true),
            HostSeatPresentation(ClocktowerSeatId(3), playerName = "Casey", isAlive = true),
        )

        val frames = hostTableSeatFrames(
            seats = duplicateNames,
            interaction = HostTableInteractionState(
                mode = HostTableInteractionMode.Selection,
                selectedSeatIds = listOf(ClocktowerSeatId(2)),
            ),
        )
        val byId = frames.associateBy { it.seat.seatId }

        assertFalse(byId.getValue(ClocktowerSeatId(1)).isSelected)
        assertTrue(byId.getValue(ClocktowerSeatId(2)).isSelected)
    }

    @Test
    fun `ordered selection projects explicit typed selection order`() {
        val frames = hostTableSeatFrames(
            seats = seats(5),
            interaction = HostTableInteractionState(
                mode = HostTableInteractionMode.OrderedSelection,
                selectedSeatIds = listOf(ClocktowerSeatId(4), ClocktowerSeatId(1)),
            ),
        )
        val byId = frames.associateBy { it.seat.seatId }

        assertEquals(1, byId.getValue(ClocktowerSeatId(4)).selectionOrder)
        assertEquals(2, byId.getValue(ClocktowerSeatId(1)).selectionOrder)
        assertEquals(null, byId.getValue(ClocktowerSeatId(2)).selectionOrder)
    }

    @Test
    fun `invalid physical seat topology fails closed`() {
        val missingSeat = listOf(
            HostSeatPresentation(ClocktowerSeatId(1), playerName = "A", isAlive = true),
            HostSeatPresentation(ClocktowerSeatId(3), playerName = "C", isAlive = true),
        )

        try {
            hostTableSeatFrames(missingSeat, HostTableInteractionState())
            fail("Expected a non-contiguous physical seat topology to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: the shell must not invent a new seat order from a malformed list.
        }
    }

    private fun seats(count: Int): List<HostSeatPresentation> =
        (1..count).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = true,
            )
        }

    private fun placementsBySeat(
        seats: List<HostSeatPresentation>,
    ): Map<ClocktowerSeatId, ClocktowerSquareTableSeatPlacement> =
        hostTableSeatFrames(seats, HostTableInteractionState())
            .associate { it.seat.seatId to it.placement }
}
