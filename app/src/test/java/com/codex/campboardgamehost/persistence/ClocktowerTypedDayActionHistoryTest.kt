package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Actions are player/mechanical facts: no fabricated storyteller decision or private observation. */
class ClocktowerTypedDayActionHistoryTest {
    private fun entry(fact: ActionFact, local: Int): TimelineBoundActionFact =
        TimelineBoundActionFact(fact, TimelinePoint(StorytellerPhase.DAY, 1, local, fact.sequence))

    @Test fun `hit and ineffective shot, first Virgin nomination and vote are separate globally ordered facts`() {
        val facts = listOf(
            ActionFact.SlayerShot("attempt-failed", 0L, 1, 4, abilityConsumed = true, hit = false),
            ActionFact.SlayerShot("attempt-other", 1L, 2, 3, abilityConsumed = false, hit = false),
            ActionFact.Nomination("first-virgin", 2L, 5, 6, firstVirginNomination = true),
            ActionFact.Vote("public-vote", 3L, 5, 6, listOf(1, 3, 5), listOf(3)),
        )
        val timeline = ActionFactTimeline(facts.mapIndexed { index, fact -> entry(fact, index + 1) })
        assertEquals(listOf(0L, 1L, 2L, 3L), timeline.entries.map { it.fact.sequence })
        assertTrue(timeline.entries.none { it.fact is ActionFact.Death })
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
        assertEquals(listOf("nominee", "voting", "confirmed-nobody"), timeline.entries.map { it.fact.actionId })
        assertTrue(timeline.entries.last().fact is ActionFact.NoExecution)
        assertTrue(timeline.entries.none { it.fact is ActionFact.Execution })
    }

}
