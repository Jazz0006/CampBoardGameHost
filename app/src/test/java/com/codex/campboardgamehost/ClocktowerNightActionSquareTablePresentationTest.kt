package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightActionSquareTablePresentationTest {
    @Test fun selectedActorRemainsDistinctFromTargetEligibility() {
        val actor = clocktowerSingleTargetSeatPresentation(3, 3, 3, setOf(2, 3))
        assertTrue(actor.isCurrentActor)
        assertTrue(actor.isSelected)
        assertEquals(1, actor.selectionOrder)
        assertTrue(actor.isInteractionEnabled)
        val eligible = clocktowerSingleTargetSeatPresentation(2, 3, 3, setOf(2, 3))
        assertFalse(eligible.isCurrentActor)
        assertTrue(eligible.isCandidate)
        val blocked = clocktowerSingleTargetSeatPresentation(4, 3, 3, setOf(2, 3))
        assertFalse(blocked.isInteractionEnabled)
    }

    @Test fun twoTargetsKeepTheirPhysicalSelectionOrderEvenIfNoLongerCandidates() {
        val first = clocktowerTwoTargetSeatPresentation(2, 4, listOf(2, 6), emptySet())
        val second = clocktowerTwoTargetSeatPresentation(6, 4, listOf(2, 6), emptySet())
        assertEquals(1, first.selectionOrder)
        assertEquals(2, second.selectionOrder)
        assertTrue(first.isSelected)
        assertTrue(second.isSelected)
        assertFalse(first.isCandidate)
        assertFalse(second.isCandidate)
    }
}

