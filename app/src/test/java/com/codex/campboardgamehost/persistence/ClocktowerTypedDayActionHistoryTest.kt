package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Actions are player/mechanical facts: no fabricated storyteller decision or private observation. */
class ClocktowerTypedDayActionHistoryTest {
    private fun entry(fact: ActionFact, local: Int): TimelineBoundActionFact =
        TimelineBoundActionFact(fact, TimelinePoint(StorytellerPhase.DAY, 1, local, fact.sequence))

    private fun roundTrip(timeline: ActionFactTimeline): ActionFactTimeline =
        ClocktowerSemanticHistoryPersistence.decodeActionTimeline(JSONObject().put(
            ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
            ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline),
        ))

    @Test fun `hit and ineffective shot, first Virgin nomination and vote are separate globally ordered facts`() {
        val facts = listOf(
            ActionFact.SlayerShot("attempt-failed", 0L, 1, 4, abilityConsumed = true, hit = false),
            ActionFact.SlayerShot("attempt-other", 1L, 2, 3, abilityConsumed = false, hit = false),
            ActionFact.Nomination("first-virgin", 2L, 5, 6, firstVirginNomination = true),
            ActionFact.Vote("public-vote", 3L, 5, 6, listOf(1, 3, 5), listOf(3)),
        )
        val timeline = ActionFactTimeline(facts.mapIndexed { index, fact -> entry(fact, index + 1) })
        assertEquals(timeline, roundTrip(timeline))
        assertEquals(listOf(0L, 1L, 2L, 3L), roundTrip(timeline).entries.map { it.fact.sequence })
        assertTrue(roundTrip(timeline).entries.none { it.fact is ActionFact.Death })
    }

    @Test fun `strict current-format recovery action decoder rejects forged hit and ghost vote`() {
        val timeline = ActionFactTimeline(listOf(
            entry(ActionFact.SlayerShot("miss", 0L, 1, 4, abilityConsumed = false, hit = false), 1),
            entry(ActionFact.Vote("vote", 1L, 2, 4, listOf(1, 3), listOf(3)), 2),
        ))
        val base = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline)

        base.getJSONObject(0).getJSONObject("fact").put("hit", true)
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, base,
            ))
        }

        base.getJSONObject(0).getJSONObject("fact").put("hit", false)
        base.getJSONObject(1).getJSONObject("fact").put("ghostVoterSeats", org.json.JSONArray().put(5))
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, base,
            ))
        }
    }

    @Test fun `public replay keeps player actions but hides private functioning and virgin-role flags`() {
        val timeline = ActionFactTimeline(listOf(
            entry(ActionFact.SlayerShot("shot", 0L, 1, 4, abilityConsumed = true, hit = false), 1),
            entry(ActionFact.Nomination("nom", 1L, 2, 3, firstVirginNomination = true), 2),
            entry(ActionFact.Vote("vote", 2L, 2, 3, listOf(1, 2), listOf(1)), 3),
        ))
        val public = com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalTimeline.project(
            recipientSeat = 5,
            actionTimeline = timeline,
            observationLog = com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog(),
        )
        assertEquals(3, public.size)
        assertTrue(public[0] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicSlayerShot)
        assertTrue(public[1] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicNomination)
        assertTrue(public[2] is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicVote)
        assertEquals(listOf(0L, 1L, 2L), public.map { it.point.globalSequence })
    }

    @Test fun `explicit no execution is canonical and publicly visible not an inferred missing death`() {
        val actions = listOf(
            ActionFact.Nomination("nominee", 0L, 2, 3, firstVirginNomination = false),
            ActionFact.Vote("voting", 1L, 2, 3, listOf(1, 2, 4), emptyList()),
            ActionFact.NoExecution("confirmed-nobody", 2L),
        )
        val timeline = ActionFactTimeline(actions.mapIndexed { index, fact ->
            entry(fact, index + 1)
        })
        val restored = roundTrip(timeline)
        assertEquals(timeline, restored)
        val public = com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalTimeline.project(
            recipientSeat = 5,
            actionTimeline = restored,
            observationLog = com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog(),
        )
        assertEquals(3, public.size)
        assertTrue(public.last() is
            com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicNoExecution)
        assertTrue(restored.entries.none { it.fact is ActionFact.Execution })
    }

    @Test fun `typed action fields cannot be silently omitted from current format`() {
        val timeline = ActionFactTimeline(listOf(
            entry(ActionFact.Nomination("nomination", 0L, 1, 2, firstVirginNomination = true), 1),
        ))
        val payload = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline)
        payload.getJSONObject(0).getJSONObject("fact").remove("firstVirginNomination")
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, payload,
            ))
        }
    }
}
