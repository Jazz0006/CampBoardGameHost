package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDynamicNightAdvanceTest {
    @Test
    fun `dynamic last step cannot expose a cursor outside the current renderable flow`() {
        val currentStepCount = 5
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = currentStepCount,
            flowMayExpandAfterConfirmation = true,
        )

        val exposedStepIndex = (directive as? ClocktowerNightAdvanceDirective.MoveTo)?.stepIndex
        assertTrue(
            "Pending dynamic refresh must not expose an out-of-range cursor: $directive",
            exposedStepIndex == null || exposedStepIndex in 0 until currentStepCount,
        )
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
