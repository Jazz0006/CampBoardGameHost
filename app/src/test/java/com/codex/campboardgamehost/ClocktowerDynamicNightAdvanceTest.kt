package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerDynamicNightAdvanceTest {
    @Test
    fun `dynamic last step keeps the current renderable cursor while awaiting refreshed flow`() {
        val directive = clocktowerNightAdvanceDirective(
            currentStepIndex = 4,
            currentStepCount = 5,
            flowMayExpandAfterConfirmation = true,
        )

        assertEquals(ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4), directive)
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
    fun `refreshed dynamic flow moves only to a newly renderable next step`() {
        val directive = clocktowerRefreshedNightAdvanceDirective(
            pending = ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4),
            refreshedStepCount = 6,
        )

        assertEquals(ClocktowerNightAdvanceDirective.MoveTo(5), directive)
    }

    @Test
    fun `refreshed dynamic flow completes when no next step was inserted`() {
        val directive = clocktowerRefreshedNightAdvanceDirective(
            pending = ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(4),
            refreshedStepCount = 5,
        )

        assertEquals(ClocktowerNightAdvanceDirective.CompleteNight, directive)
    }
}
