package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableSeatLayoutTest {
    @Test
    fun `trouble brewing player counts preserve every stable seat exactly once in clockwise order`() {
        for (playerCount in 5..15) {
            val seats = (1..playerCount).map { seatNumber ->
                ClocktowerSquareTableSeatUiModel(
                    seatId = "seat-$seatNumber",
                    seatNumber = seatNumber,
                    label = "Player $seatNumber",
                )
            }
            val layout = layoutFor(playerCount)

            val placements = clocktowerSquareTablePlacements(
                seats = seats,
                layout = layout,
            )

            assertEquals(seats.map { it.seatId }, placements.map { it.seat.seatId })
            assertEquals(playerCount, placements.map { it.seat.seatId }.distinct().size)
            assertEquals(playerCount, placements.size)
            assertEquals((0 until playerCount).toList(), placements.map { it.spatialSlot.ringIndex })

            val edgeCounts = ClocktowerSquareTableEdge.values().map { edge ->
                placements.count { it.edge == edge }
            }
            assertTrue(edgeCounts.all { it > 0 })
        }
    }

    @Test
    fun `seat visual state remains attached to stable identity after layout assignment`() {
        val seats = (1..10).map { seatNumber ->
            val state = when (seatNumber) {
                2 -> ClocktowerSquareTableSeatState.SelectedFirst
                7 -> ClocktowerSquareTableSeatState.SelectedSecond
                9 -> ClocktowerSquareTableSeatState.HighlightedInformation
                10 -> ClocktowerSquareTableSeatState.Disabled
                else -> ClocktowerSquareTableSeatState.Selectable
            }
            ClocktowerSquareTableSeatUiModel(
                seatId = "stable-$seatNumber",
                seatNumber = seatNumber,
                label = "P$seatNumber",
                state = state,
            )
        }

        val statesById = clocktowerSquareTablePlacements(
            seats = seats,
            layout = layoutFor(seats.size),
        ).associate { it.seat.seatId to it.seat.state }

        assertEquals(ClocktowerSquareTableSeatState.SelectedFirst, statesById["stable-2"])
        assertEquals(ClocktowerSquareTableSeatState.SelectedSecond, statesById["stable-7"])
        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, statesById["stable-9"])
        assertEquals(ClocktowerSquareTableSeatState.Disabled, statesById["stable-10"])
    }

    @Test
    fun `surface exposes the six product seat states`() {
        assertEquals(
            setOf(
                ClocktowerSquareTableSeatState.Neutral,
                ClocktowerSquareTableSeatState.Selectable,
                ClocktowerSquareTableSeatState.SelectedFirst,
                ClocktowerSquareTableSeatState.SelectedSecond,
                ClocktowerSquareTableSeatState.HighlightedInformation,
                ClocktowerSquareTableSeatState.Disabled,
            ),
            ClocktowerSquareTableSeatState.values().toSet(),
        )
    }

    @Test
    fun `duplicate stable seat identity fails closed`() {
        val seats = listOf(
            ClocktowerSquareTableSeatUiModel("same", 1, "P1"),
            ClocktowerSquareTableSeatUiModel("same", 2, "P2"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            clocktowerSquareTablePlacements(
                seats = seats,
                layout = layoutFor(seats.size),
            )
        }
    }

    private fun layoutFor(playerCount: Int): HostTableLayout = hostTableLayout(
        playerCount = playerCount,
        constraints = HostTableLayoutConstraints(
            availableWidth = 360f,
            availableHeight = 600f,
            seatCardWidth = 64f,
            seatCardHeight = 50f,
            minimumSafeSeparation = 4f,
            centerWorkspaceWidth = 200f,
            centerWorkspaceHeight = 312f,
        ),
    )
}
