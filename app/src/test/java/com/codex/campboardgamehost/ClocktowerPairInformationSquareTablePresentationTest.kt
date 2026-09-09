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
    fun `manual edit keeps recommended pair selected and exposes every legal replacement seat`() {
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
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerPairInformationSeatState(selection, seatNumber = 2, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Selectable,
            clocktowerPairInformationSeatState(selection, seatNumber = 8, editing = true),
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
