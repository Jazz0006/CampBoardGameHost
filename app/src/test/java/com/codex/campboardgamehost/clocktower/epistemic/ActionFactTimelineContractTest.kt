package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ActionFactTimelineContractTest {
    private val snapshot = A4RuntimeFixtures.snapshot()

    @Test fun `action sequence must equal shared global timeline sequence`() {
        val fact = ActionFact.Death("death-seat-two", sequence = 7L, targetSeat = 2)
        val point = TimelinePoint(StorytellerPhase.NIGHT, round = 2, sequence = 7, globalSequence = 8L)

        assertThrows(IllegalArgumentException::class.java) {
            TimelineBoundActionFact(fact, point)
        }
    }

    @Test fun `local sequence cannot stand in for global action identity`() {
        val fact = ActionFact.Poison("poison-seat-two", sequence = 15L, targetSeat = 2)
        val point = TimelinePoint(
            StorytellerPhase.NIGHT,
            round = 3,
            sequence = 15,
            globalSequence = 42L,
        )

        assertThrows(IllegalArgumentException::class.java) {
            TimelineBoundActionFact(fact, point)
        }
    }

    @Test fun `action timeline rejects duplicate shared global positions`() {
        val first = bound(
            ActionFact.Poison("poison", 4L, 2),
            StorytellerPhase.FIRST_NIGHT,
            round = 1,
            localSequence = 8,
        )
        val duplicate = bound(
            ActionFact.Death("death", 4L, 3),
            StorytellerPhase.DAY,
            round = 1,
            localSequence = 0,
        )

        assertThrows(IllegalArgumentException::class.java) {
            ActionFactTimeline(listOf(first, duplicate))
        }
    }

    @Test fun `action timeline rejects duplicate action ids even at distinct global positions`() {
        val first = bound(ActionFact.Poison("same-id", 4L, 2), StorytellerPhase.FIRST_NIGHT, 1, 8)
        val second = bound(ActionFact.Death("same-id", 5L, 3), StorytellerPhase.DAY, 1, 0)

        assertThrows(IllegalArgumentException::class.java) {
            ActionFactTimeline(listOf(first, second))
        }
    }

    @Test fun `append canonicalizes by global timeline across phase and local resets`() {
        val firstNight = bound(
            ActionFact.Poison("first-night-poison", 40L, 2),
            StorytellerPhase.FIRST_NIGHT,
            round = 1,
            localSequence = 99,
        )
        val day = bound(
            ActionFact.Execution("day-execution", 41L, 3),
            StorytellerPhase.DAY,
            round = 1,
            localSequence = 0,
        )
        val secondNight = bound(
            ActionFact.Death("second-night-death", 42L, 4),
            StorytellerPhase.NIGHT,
            round = 2,
            localSequence = 0,
        )

        val timeline = ActionFactTimeline()
            .append(secondNight)
            .append(firstNight)
            .append(day)

        assertEquals(listOf(40L, 41L, 42L), timeline.entries.map { it.point.globalSequence })
        assertEquals(
            listOf("first-night-poison", "day-execution", "second-night-death"),
            timeline.reducerFacts().map(ActionFact::actionId),
        )
    }

    @Test fun `bound timeline preserves existing dynamic reducer semantics`() {
        val facts = listOf(
            ActionFact.Poison("poison", 10L, 2),
            ActionFact.Execution("execution", 11L, 3),
            ActionFact.PhaseAdvance("advance", 12L, StorytellerPhase.DAY, 1),
        )
        val timeline = facts.fold(ActionFactTimeline()) { current, fact ->
            val phase = if (fact is ActionFact.PhaseAdvance) StorytellerPhase.DAY else StorytellerPhase.FIRST_NIGHT
            val local = if (phase == StorytellerPhase.DAY) 0 else fact.sequence.toInt()
            current.append(
                TimelineBoundActionFact(
                    fact,
                    TimelinePoint(phase, round = 1, sequence = local, globalSequence = fact.sequence),
                ),
            )
        }

        val raw = DynamicActionReducer.reduce(snapshot, StorytellerPhase.FIRST_NIGHT, 1, facts.reversed())
        val bound = DynamicActionReducer.reduce(
            snapshot,
            StorytellerPhase.FIRST_NIGHT,
            1,
            timeline.reducerFacts().reversed(),
        )

        assertEquals(raw, bound)
        assertEquals(listOf(10L, 11L, 12L), timeline.reducerFacts().map(ActionFact::sequence))
    }

    private fun bound(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
    ): TimelineBoundActionFact = TimelineBoundActionFact(
        fact,
        TimelinePoint(phase, round, localSequence, fact.sequence),
    )
}
