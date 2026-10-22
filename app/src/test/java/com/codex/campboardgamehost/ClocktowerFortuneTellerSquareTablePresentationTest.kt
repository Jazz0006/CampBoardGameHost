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

    @Test fun resultActionsKeepBothManualAnswersAndRespectAutomaticRecommendation() {
        assertEquals(listOf(true, false), clocktowerFortuneTellerResultActions(null))
        assertEquals(listOf(false, true), clocktowerFortuneTellerResultActions(false))
        assertEquals(emptyList<Boolean>(), clocktowerFortuneTellerVisibleResults(null, automaticOnly = true))
        assertEquals(listOf(false), clocktowerFortuneTellerVisibleResults(false, automaticOnly = true))
        assertEquals(listOf(false, true), clocktowerFortuneTellerVisibleResults(false, automaticOnly = false))
    }
}

