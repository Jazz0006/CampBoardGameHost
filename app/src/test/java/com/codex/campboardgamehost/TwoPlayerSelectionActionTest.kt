package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class TwoPlayerSelectionActionTest {
    @Test
    fun `empty selection assigns the first slot`() {
        assertEquals(
            TwoPlayerSelectionAction.ToggleFirst,
            twoPlayerSelectionAction(null, null, "Alice"),
        )
    }

    @Test
    fun `one selected player assigns the empty second slot`() {
        assertEquals(
            TwoPlayerSelectionAction.ToggleSecond,
            twoPlayerSelectionAction("Alice", null, "Bob"),
        )
    }

    @Test
    fun `tapping a selected player toggles that slot`() {
        assertEquals(
            TwoPlayerSelectionAction.ToggleFirst,
            twoPlayerSelectionAction("Alice", "Bob", "Alice"),
        )
        assertEquals(
            TwoPlayerSelectionAction.ToggleSecond,
            twoPlayerSelectionAction("Alice", "Bob", "Bob"),
        )
    }

    @Test
    fun `third player is rejected when two are selected`() {
        assertEquals(
            TwoPlayerSelectionAction.RejectLimit,
            twoPlayerSelectionAction("Alice", "Bob", "Carol"),
        )
    }

}
