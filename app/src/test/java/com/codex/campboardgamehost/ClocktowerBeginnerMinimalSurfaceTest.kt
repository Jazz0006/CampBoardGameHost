package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// UI-INFO-1.4 regression coverage: Beginner surfaces show only immediate required actions.
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

    @Test
    fun `structured guidance remains the only compact-mode trigger`() {
        assertTrue(clocktowerUsesBeginnerCompactNightGuidance("唤醒 厨师\n2号 Jazz"))
        assertFalse(clocktowerUsesBeginnerCompactNightGuidance("厨师：查看邪恶相邻对"))
    }
}
