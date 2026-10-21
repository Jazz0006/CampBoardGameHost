package com.codex.campboardgamehost

// Durable UI contract: the square-table surface renders supplied target legality and Boolean choices only.
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerFortuneTellerSquareTablePresentationTest {
    @Test
    fun `before first target only supplied selectable seats are enabled`() {
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerFortuneTellerSeatState(
                seatNumber = 2,
                selectedSeats = emptyList(),
                selectableSeats = setOf(2, 4, 5),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerFortuneTellerSeatState(
                seatNumber = 3,
                selectedSeats = emptyList(),
                selectableSeats = setOf(2, 4, 5),
            ),
        )
    }

    @Test
    fun `selected first seat uses the common selected highlight while supplied continuations remain selectable`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerFortuneTellerSeatState(
                seatNumber = 2,
                selectedSeats = listOf(2),
                selectableSeats = setOf(4, 5),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerFortuneTellerSeatState(
                seatNumber = 5,
                selectedSeats = listOf(2),
                selectableSeats = setOf(4, 5),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerFortuneTellerSeatState(
                seatNumber = 6,
                selectedSeats = listOf(2),
                selectableSeats = setOf(4, 5),
            ),
        )
    }

    @Test
    fun `completed pair uses the same visual state for both selected players`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerFortuneTellerSeatState(
                seatNumber = 2,
                selectedSeats = listOf(2, 5),
                selectableSeats = emptySet(),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerFortuneTellerSeatState(
                seatNumber = 5,
                selectedSeats = listOf(2, 5),
                selectableSeats = emptySet(),
            ),
        )
    }

    @Test
    fun `result actions always allow both answers and highlight AI recommendation first`() {
        assertEquals(listOf(true, false), clocktowerFortuneTellerResultActions(null))
        assertEquals(listOf(false, true), clocktowerFortuneTellerResultActions(false))
        assertEquals(listOf(true, false), clocktowerFortuneTellerResultActions(true))
    }

    @Test
    fun `automatic Fortune Teller displays only the bound answer and never a manual fallback`() {
        assertEquals(emptyList<Boolean>(), clocktowerFortuneTellerVisibleResults(null, automaticOnly = true))
        assertEquals(listOf(false), clocktowerFortuneTellerVisibleResults(false, automaticOnly = true))
        assertEquals(listOf(true), clocktowerFortuneTellerVisibleResults(true, automaticOnly = true))
        assertEquals(listOf(false, true), clocktowerFortuneTellerVisibleResults(false, automaticOnly = false))
        assertEquals(listOf(true, false), clocktowerFortuneTellerVisibleResults(null, automaticOnly = false))
    }
}
