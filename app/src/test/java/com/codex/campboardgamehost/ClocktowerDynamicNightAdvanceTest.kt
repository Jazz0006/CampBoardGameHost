package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDynamicNightAdvanceTest {
    @Test
    fun `dynamic last step requests the next slot instead of finishing from the stale list`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(5), directive)
    }

    @Test
    fun `ordinary last step still completes immediately`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = false,
        )

        assertEquals(ClocktowerNightAdvanceDirective.CompleteNight, directive)
    }

    @Test
    fun `existing next step advances normally`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 2,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(3), directive)
    }

    @Test
    fun `deferred advance keeps newly inserted trigger step`() {
        assertFalse(
            clocktowerDeferredNightAdvanceShouldComplete(
                requestedStepIndex = 5,
                refreshedStepCount = 6,
            ),
        )
    }

    @Test
    fun `deferred advance completes only after refreshed flow proves no step was inserted`() {
        assertTrue(
            clocktowerDeferredNightAdvanceShouldComplete(
                requestedStepIndex = 5,
                refreshedStepCount = 5,
            ),
        )
    }
}
