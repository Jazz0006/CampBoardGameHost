package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightActionSquareTablePresentationTest {
    @Test
    fun `single-target state keeps selected seat distinct and disables illegal seats`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerSingleTargetSeatState(
                seatNumber = 3,
                selectedSeat = 3,
                selectableSeats = setOf(2, 3, 5),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerSingleTargetSeatState(
                seatNumber = 5,
                selectedSeat = 3,
                selectableSeats = setOf(2, 3, 5),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerSingleTargetSeatState(
                seatNumber = 4,
                selectedSeat = 3,
                selectableSeats = setOf(2, 3, 5),
            ),
        )
    }

    @Test
    fun `single-target presentation keeps acting seat cue independent from target state`() {
        val actingSeat = clocktowerSingleTargetSeatPresentation(
            seatNumber = 3,
            actorSeat = 3,
            selectedSeat = 3,
            selectableSeats = setOf(2, 3, 5),
        )
        assertEquals(ClocktowerSquareTableSeatState.SelectedFirst, actingSeat.targetState)
        assertTrue(actingSeat.isCurrentActor)

        val selectedTarget = clocktowerSingleTargetSeatPresentation(
            seatNumber = 5,
            actorSeat = 3,
            selectedSeat = 5,
            selectableSeats = setOf(2, 3, 5),
        )
        assertEquals(ClocktowerSquareTableSeatState.SelectedFirst, selectedTarget.targetState)
        assertFalse(selectedTarget.isCurrentActor)
    }

    @Test
    fun `two-target state preserves ordered selections and supplied continuations`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerTwoTargetSeatState(
                seatNumber = 2,
                selectedSeats = listOf(2, 6),
                selectableSeats = emptySet(),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedSecond,
            clocktowerTwoTargetSeatState(
                seatNumber = 6,
                selectedSeats = listOf(2, 6),
                selectableSeats = emptySet(),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerTwoTargetSeatState(
                seatNumber = 5,
                selectedSeats = listOf(2),
                selectableSeats = setOf(4, 5, 6),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerTwoTargetSeatState(
                seatNumber = 3,
                selectedSeats = listOf(2),
                selectableSeats = setOf(4, 5, 6),
            ),
        )
    }
}
