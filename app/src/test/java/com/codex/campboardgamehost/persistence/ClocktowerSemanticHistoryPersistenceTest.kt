package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSemanticHistoryPersistenceTest {
    @Test
    fun `empty committed history derives zero cursor`() {
        assertEquals(
            0L,
            ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
                actionTimeline = ActionFactTimeline(),
                observations = emptyList(),
            ),
        )
    }

    @Test
    fun `cursor derives from highest committed action or observation`() {
        val actions = actionTimeline(globalSequence = 4L)
        val observations = listOf(globalObservation(globalSequence = 7L))

        assertEquals(
            5L,
            ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
                actionTimeline = actions,
                observations = emptyList(),
            ),
        )
        assertEquals(
            8L,
            ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
                actionTimeline = ActionFactTimeline(),
                observations = observations,
            ),
        )
        assertEquals(
            8L,
            ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
                actionTimeline = actions,
                observations = observations,
            ),
        )
    }

    @Test
    fun `exhausted committed global sequence fails closed`() {
        assertFails {
            ClocktowerSemanticHistoryPersistence.deriveNextTimelineGlobalSequence(
                actionTimeline = actionTimeline(globalSequence = Long.MAX_VALUE),
                observations = emptyList(),
            )
        }
    }

    private fun actionTimeline(globalSequence: Long): ActionFactTimeline = ActionFactTimeline(
        listOf(
            TimelineBoundActionFact(
                fact = ActionFact.Death(
                    actionId = "death-$globalSequence",
                    sequence = globalSequence,
                    targetSeat = 1,
                ),
                point = TimelinePoint(
                    phase = StorytellerPhase.DAWN,
                    round = 1,
                    sequence = 0,
                    globalSequence = globalSequence,
                ),
            ),
        ),
    )

    private fun globalObservation(globalSequence: Long): RecordedEpistemicObservation =
        RecordedEpistemicObservation(
            recordId = "observation-$globalSequence",
            phase = StorytellerPhase.DAWN,
            round = 1,
            sequence = 0,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(1, false),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(
                    phase = StorytellerPhase.DAWN,
                    round = 1,
                    sequence = 0,
                    globalSequence = globalSequence,
                ),
            ),
        )

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected derived timeline cursor to fail closed.", failed)
    }
}
