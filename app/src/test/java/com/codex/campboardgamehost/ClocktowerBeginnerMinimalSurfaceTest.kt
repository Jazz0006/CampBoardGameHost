package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// UI-INFO-1.4 regression coverage: Beginner reveal-only seats do not expose information accents.
class ClocktowerBeginnerMinimalSurfaceTest {
    @Test
    fun `beginner reveal-only seats never expose information highlights`() {
        val actor = clocktowerBeginnerNeutralSeatPresentation(seatNumber = 2, actorSeat = 2)
        val other = clocktowerBeginnerNeutralSeatPresentation(seatNumber = 5, actorSeat = 2)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertTrue(actor.isCurrentActor)
        assertFalse(other.isCurrentActor)
    }
}
