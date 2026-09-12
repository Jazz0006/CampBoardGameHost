package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerBeginnerCompactNightPresentationTest {
    @Test
    fun `structured beginner wake guidance uses compact surface`() {
        assertTrue(clocktowerUsesBeginnerCompactNightGuidance("唤醒 投毒者\n3号 张三\n让他选择一名玩家作为中毒目标"))
        assertFalse(clocktowerUsesBeginnerCompactNightGuidance("Wake P3 张三 and choose a target"))
    }

    @Test
    fun `beginner pair surface hides clue target accents but keeps actor cue`() {
        val actor = clocktowerBeginnerPairSeatPresentation(seatNumber = 3, actorSeat = 3)
        val other = clocktowerBeginnerPairSeatPresentation(seatNumber = 5, actorSeat = 3)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, actor.targetState)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, other.targetState)
        assertTrue(actor.isCurrentActor)
        assertFalse(other.isCurrentActor)
    }
}
