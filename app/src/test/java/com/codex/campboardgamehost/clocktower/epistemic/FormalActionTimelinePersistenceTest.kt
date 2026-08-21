package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FormalActionTimelinePersistenceTest {
    private val snapshot = A4RuntimeFixtures.snapshot()

    @Test fun `legacy schema v2 formal timeline stays explicitly legacy without inferred timeline points`() {
        val formal = FormalGameState.from(
            snapshot = snapshot,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            timeline = listOf(ActionFact.Poison("legacy-poison", 7L, 2)),
        )

        val json = EpistemicSemanticJson.encode(formal)
        val decoded = EpistemicSemanticJson.decodeFormalGameState(json)

        assertFalse(json.contains("actionTimelineBinding"))
        assertEquals(FormalActionTimelineBinding.Legacy, decoded.actionTimelineBinding)
        assertEquals(listOf(7L), decoded.timeline.map(ActionFact::sequence))
    }

    @Test fun `global formal action timeline round trips explicit timeline points`() {
        val timeline = globalTimeline()
        val formal = FormalGameState.from(
            snapshot = snapshot,
            phase = StorytellerPhase.NIGHT,
            round = 2,
            timeline = timeline.reducerFacts(),
            actionTimelineBinding = FormalActionTimelineBinding.Global(timeline),
        )

        val json = EpistemicSemanticJson.encode(formal)
        val decoded = EpistemicSemanticJson.decodeFormalGameState(json)

        assertTrue(json.contains("actionTimelineBinding"))
        assertEquals(formal, decoded)
        val decodedTimeline = (decoded.actionTimelineBinding as FormalActionTimelineBinding.Global).timeline
        assertEquals(listOf(40L, 41L), decodedTimeline.entries.map { it.point.globalSequence })
        assertEquals(listOf(99, 0), decodedTimeline.entries.map { it.point.sequence })
    }

    @Test fun `formal state defensively snapshots caller owned action list`() {
        val expected = globalFormal()
        val callerOwned = expected.timeline.toMutableList()
        val formal = expected.copy(timeline = callerOwned)

        callerOwned.clear()

        assertEquals(expected.timeline, formal.timeline)
        assertEquals(formal, EpistemicSemanticJson.decodeFormalGameState(EpistemicSemanticJson.encode(formal)))
    }

    @Test fun `global formal binding rejects action ids absent from the persisted action payload`() {
        val root = JSONObject(EpistemicSemanticJson.encode(globalFormal()))
        root.getJSONObject("actionTimelineBinding")
            .getJSONArray("entries")
            .getJSONObject(0)
            .put("actionId", "not-in-formal-timeline")

        assertThrows(IllegalArgumentException::class.java) {
            EpistemicSemanticJson.decodeFormalGameState(root.toString())
        }
    }

    @Test fun `global formal binding rejects timeline point whose global sequence disagrees with action fact`() {
        val root = JSONObject(EpistemicSemanticJson.encode(globalFormal()))
        root.getJSONObject("actionTimelineBinding")
            .getJSONArray("entries")
            .getJSONObject(0)
            .getJSONObject("point")
            .put("globalSequence", 999L)

        assertThrows(IllegalArgumentException::class.java) {
            EpistemicSemanticJson.decodeFormalGameState(root.toString())
        }
    }

    @Test fun `global formal binding rejects null instead of downgrading to legacy`() {
        val root = JSONObject(EpistemicSemanticJson.encode(globalFormal()))
        root.put("actionTimelineBinding", JSONObject.NULL)

        assertThrows(IllegalArgumentException::class.java) {
            EpistemicSemanticJson.decodeFormalGameState(root.toString())
        }
    }

    private fun globalFormal(): FormalGameState {
        val timeline = globalTimeline()
        return FormalGameState.from(
            snapshot = snapshot,
            phase = StorytellerPhase.NIGHT,
            round = 2,
            timeline = timeline.reducerFacts(),
            actionTimelineBinding = FormalActionTimelineBinding.Global(timeline),
        )
    }

    private fun globalTimeline(): ActionFactTimeline = ActionFactTimeline()
        .append(
            TimelineBoundActionFact(
                ActionFact.Poison("night-one-poison", 40L, 2),
                TimelinePoint(StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 99, globalSequence = 40L),
            ),
        )
        .append(
            TimelineBoundActionFact(
                ActionFact.PhaseAdvance("dawn-two", 41L, StorytellerPhase.DAWN, 2),
                TimelinePoint(StorytellerPhase.DAWN, round = 2, sequence = 0, globalSequence = 41L),
            ),
        )
}
