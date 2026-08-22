package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerActionTimelinePersistenceTest {
    @Test
    fun `active game action timeline round trips every supported action fact`() {
        val timeline = ActionFactTimeline(
            listOf(
                entry(ActionFact.Poison("poison", 0L, 2), StorytellerPhase.FIRST_NIGHT, 1, 10, 0L),
                entry(ActionFact.Protect("protect", 1L, 3), StorytellerPhase.NIGHT, 2, 11, 1L),
                entry(ActionFact.Attack("attack", 2L, 4), StorytellerPhase.NIGHT, 2, 12, 2L),
                entry(ActionFact.Execution("execution", 3L, 5), StorytellerPhase.DAY, 2, 13, 3L),
                entry(ActionFact.Death("death", 4L, 6), StorytellerPhase.DAWN, 2, 14, 4L),
                entry(
                    ActionFact.RoleChange(
                        actionId = "role-change",
                        sequence = 5L,
                        targetSeat = 7,
                        role = RoleId("Imp"),
                        alignment = Alignment.EVIL,
                        type = CharacterType.DEMON,
                    ),
                    StorytellerPhase.DAWN,
                    2,
                    15,
                    5L,
                ),
                entry(
                    ActionFact.PhaseAdvance("phase", 6L, StorytellerPhase.NIGHT, 3),
                    StorytellerPhase.DAY,
                    2,
                    16,
                    6L,
                ),
            ),
        )
        val json = JSONObject().put(
            ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
            ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline),
        )

        assertEquals(timeline, ClocktowerSemanticHistoryPersistence.decodeActionTimeline(json))
    }

    @Test
    fun `missing additive action history restores empty without inventing legacy chronology`() {
        val restored = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(JSONObject())

        assertTrue(restored.entries.isEmpty())
    }

    @Test
    fun `present null or malformed action history fails closed`() {
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, JSONObject.NULL),
            )
        }
        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
                JSONObject().put(
                    ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
                    org.json.JSONArray().put(JSONObject().put("kind", "not-an-entry")),
                ),
            )
        }
    }

    @Test
    fun `persisted fact sequence must still equal bound global timeline identity`() {
        val timeline = ActionFactTimeline(
            listOf(entry(ActionFact.Death("death", 4L, 2), StorytellerPhase.DAWN, 2, 1, 4L)),
        )
        val payload = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline)
        payload.getJSONObject(0).getJSONObject("fact").put("sequence", 3L)

        assertFails {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
                JSONObject().put(ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, payload),
            )
        }
    }

    private fun entry(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
        globalSequence: Long,
    ): TimelineBoundActionFact = TimelineBoundActionFact(
        fact = fact,
        point = TimelinePoint(phase, round, localSequence, globalSequence),
    )

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        } catch (_: org.json.JSONException) {
            failed = true
        }
        assertTrue("Expected active-game action history persistence to fail closed.", failed)
    }
}
