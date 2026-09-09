package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerPairInformationSquareTablePresentationTest {
    @Test
    fun `recommended preview highlights pair while leaving other seats visually neutral`() {
        val recommended = option("Chef", 1, 4)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(recommended, option("Chef", 2, 4)),
        )
        val selection = ClocktowerPairManualSelectionModel.from(presentation, recommended)

        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerPairInformationSeatState(selection, seatNumber = 1, editing = false),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedSecond,
            clocktowerPairInformationSeatState(selection, seatNumber = 4, editing = false),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Neutral,
            clocktowerPairInformationSeatState(selection, seatNumber = 2, editing = false),
        )
    }

    @Test
    fun `manual edit reuses legal second-seat projection from the existing pair picker`() {
        val recommended = option("Chef", 1, 4)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(recommended, option("Chef", 2, 4), option("Chef", 2, 8)),
        )
        val selection = ClocktowerPairManualSelectionModel.from(presentation, recommended)

        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerPairInformationSeatState(selection, seatNumber = 1, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedSecond,
            clocktowerPairInformationSeatState(selection, seatNumber = 4, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerPairInformationSeatState(selection, seatNumber = 2, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerPairInformationSeatState(selection, seatNumber = 8, editing = true),
        )

        val awaitingSecondSeat = selection.selectSeat(4)
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedFirst,
            clocktowerPairInformationSeatState(awaitingSecondSeat, seatNumber = 1, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerPairInformationSeatState(awaitingSecondSeat, seatNumber = 4, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerPairInformationSeatState(awaitingSecondSeat, seatNumber = 2, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Disabled,
            clocktowerPairInformationSeatState(awaitingSecondSeat, seatNumber = 8, editing = true),
        )
    }

    private fun option(role: String, first: Int, second: Int) = ClocktowerDisplayOption(
        label = "$role $first/$second",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = "info",
        displayPrimary = role,
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(first, RoleId(role)),
                InformationProposition.RoleAt(second, RoleId(role)),
            ),
        ),
    )
}
