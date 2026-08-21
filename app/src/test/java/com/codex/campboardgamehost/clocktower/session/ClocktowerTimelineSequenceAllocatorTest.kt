package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ClocktowerTimelineSequenceAllocatorTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = initialState.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test fun `timeline allocation stays globally monotonic across phase and round boundaries`() {
        val session = newSession()

        val firstNight = session.allocateTimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 8)
        val day = session.allocateTimelinePoint(StorytellerPhase.DAY, round = 1, sequence = 0)
        val secondNight = session.allocateTimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0)

        assertEquals(listOf(0L, 1L, 2L), listOf(firstNight.globalSequence, day.globalSequence, secondNight.globalSequence))
        assertEquals(3L, session.snapshot.nextTimelineGlobalSequence)
        assertEquals(0L, session.snapshot.gameStateRevision)
        assertEquals(0L, session.snapshot.playerInputRevision)
    }

    @Test fun `restored session continues from persisted global timeline cursor`() {
        val original = newSession()
        original.allocateTimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 3)
        original.allocateTimelinePoint(StorytellerPhase.DAY, round = 1, sequence = 7)

        val restored = ClocktowerGameSession.restore(original.snapshot)
        val next = restored.allocateTimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0)

        assertEquals(2L, next.globalSequence)
        assertEquals(3L, restored.snapshot.nextTimelineGlobalSequence)
    }

    @Test fun `exhausted global timeline cursor fails closed without wrapping`() {
        val exhausted = ClocktowerGameSession.restore(
            newSession().snapshot.copy(nextTimelineGlobalSequence = Long.MAX_VALUE),
        )

        try {
            exhausted.allocateTimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 0)
            fail("timeline allocation must fail instead of wrapping Long.MAX_VALUE")
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("exhausted", ignoreCase = true))
        }

        assertEquals(Long.MAX_VALUE, exhausted.snapshot.nextTimelineGlobalSequence)
    }

    @Test fun `negative persisted global timeline cursor is rejected`() {
        try {
            newSession().snapshot.copy(nextTimelineGlobalSequence = -1L)
            fail("negative timeline cursor must be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: corrupted persisted cursor must not be normalized into a valid identity.
        }
    }

    private fun newSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-21-r6-allocator",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
    )
}
