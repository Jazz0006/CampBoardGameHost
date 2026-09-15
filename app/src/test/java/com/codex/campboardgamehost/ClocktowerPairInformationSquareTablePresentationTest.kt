package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPairInformationSquareTablePresentationTest {
    @Test
    fun `recommended preview gives both candidates the same highlight while leaving other seats neutral`() {
        val recommended = option("Chef", 1, 4)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(recommended, option("Chef", 2, 4)),
        )
        val selection = ClocktowerPairManualSelectionModel.from(presentation, recommended)

        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerPairInformationSeatState(selection, seatNumber = 1, editing = false),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerPairInformationSeatState(selection, seatNumber = 4, editing = false),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.Neutral,
            clocktowerPairInformationSeatState(selection, seatNumber = 2, editing = false),
        )
    }

    @Test
    fun `truth marker is driven only by selected candidate actual-role match`() {
        assertEquals(
            "✓",
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Empath",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = false,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = null,
                actualRoleId = "Chef",
            ),
        )
    }

    @Test
    fun `current actor highlight is independent from common information pair state`() {
        val recommended = option("Chef", 1, 4)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(listOf(recommended))
        val selection = ClocktowerPairManualSelectionModel.from(presentation, recommended)

        val actor = clocktowerPairInformationSeatPresentation(
            selection = selection,
            seatNumber = 6,
            editing = false,
            actorSeat = 6,
        )
        val firstInformationSeat = clocktowerPairInformationSeatPresentation(
            selection = selection,
            seatNumber = 1,
            editing = false,
            actorSeat = 6,
        )
        val secondInformationSeat = clocktowerPairInformationSeatPresentation(
            selection = selection,
            seatNumber = 4,
            editing = false,
            actorSeat = 6,
        )

        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertTrue(actor.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedHighlighted, firstInformationSeat.targetState)
        assertFalse(firstInformationSeat.isCurrentActor)
        assertEquals(ClocktowerSquareTableSeatState.SelectedHighlighted, secondInformationSeat.targetState)
        assertFalse(secondInformationSeat.isCurrentActor)
    }

    @Test
    fun `manual edit preserves legal continuation projection while selected candidates share one style`() {
        val recommended = option("Chef", 1, 4)
        val presentation = ClocktowerPairManualAuthority.selectionPresentation(
            listOf(recommended, option("Chef", 2, 4), option("Chef", 2, 8)),
        )
        val selection = ClocktowerPairManualSelectionModel.from(presentation, recommended)

        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
            clocktowerPairInformationSeatState(selection, seatNumber = 1, editing = true),
        )
        assertEquals(
            ClocktowerSquareTableSeatState.SelectedHighlighted,
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
            ClocktowerSquareTableSeatState.SelectedHighlighted,
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
