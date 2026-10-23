package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerFortuneTellerSquareTablePresentationTest {
    @Test fun pairSelectionKeepsOrderAndAbilityToSelectSuppliedCandidates() {
        val first = clocktowerFortuneTellerSeatPresentation(2, listOf(2, 5), emptySet())
        val second = clocktowerFortuneTellerSeatPresentation(5, listOf(2, 5), emptySet())
        val candidate = clocktowerFortuneTellerSeatPresentation(4, listOf(2), setOf(4))
        val unavailable = clocktowerFortuneTellerSeatPresentation(3, listOf(2), setOf(4))
        assertEquals(1, first.selectionOrder)
        assertEquals(2, second.selectionOrder)
        assertTrue(first.isSelected)
        assertTrue(candidate.isInteractionEnabled)
        assertFalse(unavailable.isInteractionEnabled)
    }

    @Test fun fixedAnswersBypassAiWhileDiscretionRequiresCurrentAutomaticAdvice() {
        for (automatic in listOf(false, true)) {
            assertEquals(listOf(false), clocktowerFortuneTellerVisibleResults(listOf(false), true, automatic))
            assertEquals(listOf(true), clocktowerFortuneTellerVisibleResults(listOf(true), null, automatic))
        }
        assertEquals(emptyList<Boolean>(), clocktowerFortuneTellerVisibleResults(listOf(false, true), null, true))
        assertEquals(listOf(false), clocktowerFortuneTellerVisibleResults(listOf(false, true), false, true))
        assertEquals(listOf(true, false), clocktowerFortuneTellerVisibleResults(listOf(false, true), true, false))
    }
}
