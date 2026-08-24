package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerHistoricalTimelineTest {
    @Test
    fun `player history interleaves public actions and visible observations by global sequence`() {
        val actions = ActionFactTimeline(
            listOf(
                action(
                    ActionFact.Death("night-1-death", 20L, 2),
                    StorytellerPhase.DAWN,
                    round = 1,
                    localSequence = 0,
                    globalSequence = 20L,
                ),
                action(
                    ActionFact.PhaseAdvance("night-2", 40L, StorytellerPhase.NIGHT, 2),
                    StorytellerPhase.DAY,
                    round = 1,
                    localSequence = 2,
                    globalSequence = 40L,
                ),
            ),
        )
        val observations = EpistemicObservationLog(
            listOf(
                observation("public-10", ObservationVisibility.PUBLIC, emptySet(), 10L),
                observation("private-recipient-30", ObservationVisibility.PRIVATE, setOf(1), 30L),
                observation("private-other-35", ObservationVisibility.PRIVATE, setOf(2), 35L),
            ),
        )

        val events = PlayerHistoricalTimeline.project(
            recipientSeat = 1,
            actionTimeline = actions,
            observationLog = observations,
        )

        assertEquals(listOf(10L, 20L, 30L, 40L), events.map { it.point.globalSequence })
        assertTrue(events[0] is PlayerHistoricalEvent.Observation)
        assertTrue(events[1] is PlayerHistoricalEvent.PublicDeath)
        assertTrue(events[2] is PlayerHistoricalEvent.Observation)
        assertTrue(events[3] is PlayerHistoricalEvent.PhaseAdvance)
        assertEquals(
            listOf("public-10", "private-recipient-30"),
            events.filterIsInstance<PlayerHistoricalEvent.Observation>().map { it.record.recordId },
        )
    }

    @Test
    fun `storyteller-only mechanical actions never enter player historical timeline`() {
        val actions = ActionFactTimeline(
            listOf(
                action(ActionFact.Poison("poison", 1L, 2), StorytellerPhase.NIGHT, 1, 0, 1L),
                action(ActionFact.Protect("protect", 2L, 3), StorytellerPhase.NIGHT, 1, 1, 2L),
                action(ActionFact.Attack("attack", 3L, 4), StorytellerPhase.NIGHT, 1, 2, 3L),
                action(
                    ActionFact.RoleChange("starpass", 4L, 5, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
                    StorytellerPhase.NIGHT,
                    1,
                    3,
                    4L,
                ),
                action(ActionFact.Execution("execution", 5L, 3), StorytellerPhase.DAY, 1, 0, 5L),
                action(ActionFact.Death("death", 6L, 4), StorytellerPhase.DAWN, 2, 0, 6L),
                action(
                    ActionFact.PhaseAdvance("day-2", 7L, StorytellerPhase.DAY, 2),
                    StorytellerPhase.DAWN,
                    2,
                    1,
                    7L,
                ),
            ),
        )

        val events = PlayerHistoricalTimeline.project(
            recipientSeat = 1,
            actionTimeline = actions,
            observationLog = EpistemicObservationLog(),
        )

        assertEquals(listOf(5L, 6L, 7L), events.map { it.point.globalSequence })
        assertEquals(3, events.size)
        assertTrue(events[0] is PlayerHistoricalEvent.PublicExecution)
        assertTrue(events[1] is PlayerHistoricalEvent.PublicDeath)
        assertTrue(events[2] is PlayerHistoricalEvent.PhaseAdvance)
    }

    private fun action(
        fact: ActionFact,
        phase: StorytellerPhase,
        round: Int,
        localSequence: Int,
        globalSequence: Long,
    ) = TimelineBoundActionFact(
        fact = fact,
        point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = localSequence,
            globalSequence = globalSequence,
        ),
    )

    private fun observation(
        recordId: String,
        visibility: ObservationVisibility,
        recipientSeats: Set<Int>,
        globalSequence: Long,
    ) = RecordedEpistemicObservation(
        recordId = recordId,
        phase = StorytellerPhase.NIGHT,
        round = 2,
        sequence = globalSequence.toInt(),
        sourceSeat = 1,
        sourceAbility = RoleId("Empath"),
        visibility = visibility,
        recipientSeats = recipientSeats,
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 1,
            subjectSeats = listOf(5, 2),
            value = 0,
        ),
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(
                phase = StorytellerPhase.NIGHT,
                round = 2,
                sequence = globalSequence.toInt(),
                globalSequence = globalSequence,
            ),
        ),
    )
}
