package com.codex.campboardgamehost.clocktower.epistemic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class A4ShadowLifecycleInvalidatorTest {
    @Test fun `revision supersede invalidates game and cancels rebuild without clearing pending durability`() {
        val events = mutableListOf<String>()
        val invalidator = A4ShadowLifecycleInvalidator(
            invalidateGame = { events += "invalidate:$it" },
            clearPendingObservation = { events += "clear-pending" },
            cancelObservationRebuild = { events += "cancel-rebuild" },
        )

        invalidator.revisionSuperseded("game-1")

        assertEquals(listOf("invalidate:game-1", "cancel-rebuild"), events)
    }

    @Test fun `session boundary invalidates cache clears pending work and cancels rebuild`() {
        val events = mutableListOf<String>()
        val invalidator = A4ShadowLifecycleInvalidator(
            invalidateGame = { events += "invalidate:$it" },
            clearPendingObservation = { events += "clear-pending" },
            cancelObservationRebuild = { events += "cancel-rebuild" },
        )

        invalidator.sessionBoundary("game-1")

        assertEquals(listOf("invalidate:game-1", "clear-pending", "cancel-rebuild"), events)
    }

    @Test fun `blank session still clears pending work and cancels rebuild without cache lookup`() {
        val events = mutableListOf<String>()
        val invalidator = A4ShadowLifecycleInvalidator(
            invalidateGame = { events += "invalidate:$it" },
            clearPendingObservation = { events += "clear-pending" },
            cancelObservationRebuild = { events += "cancel-rebuild" },
        )

        invalidator.sessionBoundary("")

        assertTrue(events.none { it.startsWith("invalidate:") })
        assertEquals(listOf("clear-pending", "cancel-rebuild"), events)
    }
}
