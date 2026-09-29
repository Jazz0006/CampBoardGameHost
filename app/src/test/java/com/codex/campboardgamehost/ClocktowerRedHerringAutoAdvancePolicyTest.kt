package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `automatic red herring advance waits for a real recommendation but skips non-actions`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `automatic selection commits only a current legal recommendation at the barrier`() {
        assertEquals(
            "Alice",
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = false,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = false,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = false,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = "Bob",
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = null,
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Stale",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
    }
}
