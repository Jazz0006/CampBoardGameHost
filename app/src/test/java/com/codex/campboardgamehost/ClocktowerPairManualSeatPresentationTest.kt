package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerPairManualSeatPresentationTest {
    @Test
    fun `pair manual selector consumes shared storyteller seat semantics including drunk shown role`() {
        val seat = HostSeatPresentation(
            seatId = ClocktowerSeatId(4),
            playerName = "Dana",
            isAlive = false,
            actualRole = HostRolePresentation(roleId = "Drunk", displayName = "Drunk"),
            shownRole = HostRolePresentation(roleId = "Empath", displayName = "Empath"),
            contentMode = HostSeatContentMode.StorytellerRoleDetail,
        )

        val ui = clocktowerPairManualSquareTableSeat(
            seat = seat,
            language = "en",
            state = ClocktowerSquareTableSeatState.Selectable,
        )

        assertEquals("seat-4", ui.seatId)
        assertEquals(4, ui.seatNumber)
        assertEquals("Dana ☠", ui.label)
        assertEquals(listOf("Actual: Drunk", "Shown: Empath"), ui.detailLabels)
        assertEquals(ClocktowerSquareTableSeatState.Selectable, ui.state)
    }
}
