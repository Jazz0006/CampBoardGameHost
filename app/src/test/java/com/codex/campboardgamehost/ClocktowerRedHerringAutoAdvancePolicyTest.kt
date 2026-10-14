package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `accepted joint red herring may be reused only by automatic Host`() {
        assertTrue(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = true,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = false,
            acceptedJointOpening = true,
        ))
        assertFalse(shouldAutoAdvanceRedHerring(
            allowRuleDeterminedAutoUi = false,
            isRedHerringStep = true,
            isRealAction = true,
            hasSelectedRedHerring = true,
            acceptedJointOpening = true,
        ))
    }

    @Test
    fun `red herring auto advance skips only non-actions and never silently confirms real selection`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `legacy red herring recommendation cannot bypass Host singleton gate`() {
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = false,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = false,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = false,
                currentSelection = null,
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = "Bob",
                recommendedSelection = "Alice",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
        assertEquals(
            "Alice",
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = null,
                legalSelections = setOf("Alice"),
            ),
        )
        assertNull(
            automaticRedHerringSelectionAtBarrier(
                allowRuleDeterminedAutoUi = true,
                isRedHerringStep = true,
                isRealAction = true,
                currentSelection = null,
                recommendedSelection = "Stale",
                legalSelections = setOf("Alice", "Bob"),
            ),
        )
    }
}
