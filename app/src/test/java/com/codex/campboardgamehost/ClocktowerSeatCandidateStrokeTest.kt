package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Selected targets must never be obscured by an unrelated ordinary/special hint. */
class ClocktowerSeatCandidateStrokeTest {
    private fun seat(
        candidate: Boolean = false,
        special: Boolean = false,
        selected: Boolean = false,
        order: Int? = null,
    ) = ClocktowerSquareTableSeatUiModel(
        seatId = "4", seatNumber = 4, label = "Player 4",
        isCandidate = candidate, isSpecialCandidate = special,
        isSelected = selected, selectionOrder = order,
    )

    @Test fun ordinaryAndSpecialCandidateUseDistinctDashPatterns() {
        assertEquals(listOf(12f, 6f), clocktowerSquareTableCandidateDashDp(seat(candidate = true)))
        assertEquals(listOf(5f, 4f), clocktowerSquareTableCandidateDashDp(seat(special = true)))
        assertNull(clocktowerSquareTableCandidateDashDp(seat()))
    }

    @Test fun selectionAlwaysOverridesEitherCandidateWithoutChangingTheirSourceFacts() {
        assertNull(clocktowerSquareTableCandidateDashDp(seat(candidate = true, selected = true)))
        assertNull(clocktowerSquareTableCandidateDashDp(seat(candidate = true, special = true, order = 2)))
    }
}
