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

    @Test
    fun `automatic storyteller skips a completed red herring step`() {
        assertEquals(
            true,
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
    }

    @Test
    fun `automatic storyteller waits when a required red herring is missing`() {
        assertEquals(
            false,
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `automatic storyteller skips a red herring placeholder`() {
        assertEquals(
            true,
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `manual storyteller never skips the red herring step`() {
        assertEquals(
            false,
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
    }
}
