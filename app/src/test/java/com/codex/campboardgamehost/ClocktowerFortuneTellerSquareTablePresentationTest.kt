package com.codex.campboardgamehost

// Durable UI-R4 RED contract: the square-table surface renders supplied target legality and Boolean choices only.
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
    fun `selected first seat stays selected while only supplied continuations remain selectable`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
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
    fun `completed pair keeps ordered first and second visual states`() {
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerFortuneTellerSeatState(
                seatNumber = 2,
                selectedSeats = listOf(2, 5),
                selectableSeats = emptySet(),
            ),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedSecond,
            clocktowerFortuneTellerSeatState(
                seatNumber = 5,
                selectedSeats = listOf(2, 5),
                selectableSeats = emptySet(),
            ),
        )
    }

    @Test
    fun `result actions expose exactly supplied legal values with recommendation first`() {
        assertEquals(
            listOf(true),
            clocktowerFortuneTellerResultActions(
                legalResults = setOf(true),
                recommendedResult = false,
            ),
        )
        assertEquals(
            listOf(false, true),
            clocktowerFortuneTellerResultActions(
                legalResults = setOf(true, false),
                recommendedResult = false,
            ),
        )
        assertEquals(
            emptyList<Boolean>(),
            clocktowerFortuneTellerResultActions(
                legalResults = emptySet(),
                recommendedResult = true,
            ),
        )
    }
}
