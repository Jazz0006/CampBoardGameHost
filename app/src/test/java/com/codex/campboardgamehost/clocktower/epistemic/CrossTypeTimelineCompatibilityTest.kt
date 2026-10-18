package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CrossTypeTimelineCompatibilityTest {
    @Test fun `Shared timeline rejects action and observation sharing one global sequence`() {
        assertThrows(IllegalArgumentException::class.java) {
            validatedPair(
                actionTimeline = actionTimeline(10L),
                observationLog = globalObservationLog(10L),
            )
        }
    }

    @Test fun `Shared timeline accepts distinct action and observation global sequences`() {
        val request = validatedPair(
            actionTimeline = actionTimeline(10L),
            observationLog = globalObservationLog(11L),
        )

        assertEquals(10L, request.first.entries.single().point.globalSequence)
        assertEquals(
            11L,
            (request.second.records.single().timelineBinding as ObservationTimelineBinding.Global)
                .point.globalSequence,
        )
    }

    @Test fun `Shared timeline rejects globally bound actions combined with legacy local observations`() {
        assertThrows(IllegalArgumentException::class.java) {
            validatedPair(
                actionTimeline = actionTimeline(10L),
                observationLog = legacyObservationLog(),
            )
        }
    }

    @Test fun `Shared timeline keeps observation-only legacy compatibility when action timeline is empty`() {
        val request = validatedPair(
            actionTimeline = ActionFactTimeline(),
            observationLog = legacyObservationLog(),
        )

        assertEquals(1, request.second.records.size)
        assertEquals(ObservationTimelineBinding.LegacyLocal, request.second.records.single().timelineBinding)
    }

    @Test fun `validated observation log cannot be rewritten through caller owned mutable list`() {
        val callerOwned = mutableListOf(globalObservation(11L))
        val log = EpistemicObservationLog(callerOwned)
        val request = validatedPair(actionTimeline(10L), log)

        callerOwned.clear()
        callerOwned += globalObservation(10L)

        assertEquals(
            11L,
            (request.second.records.single().timelineBinding as ObservationTimelineBinding.Global)
                .point.globalSequence,
        )
    }

    private fun validatedPair(
        actionTimeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): Pair<ActionFactTimeline, EpistemicObservationLog> {
        // Same shared chronology boundary formerly wrapped by the test-only B4 request.
        actionTimeline.requireCompatibleWith(observationLog)
        return actionTimeline to observationLog
    }

    private fun actionTimeline(globalSequence: Long): ActionFactTimeline = ActionFactTimeline(
        listOf(
            TimelineBoundActionFact(
                fact = ActionFact.Poison("poison-$globalSequence", globalSequence, targetSeat = 2),
                point = TimelinePoint(
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    sequence = globalSequence.toInt(),
                    globalSequence = globalSequence,
                ),
            ),
        ),
    )

    private fun globalObservationLog(globalSequence: Long): EpistemicObservationLog =
        EpistemicObservationLog(listOf(globalObservation(globalSequence)))

    private fun globalObservation(globalSequence: Long): RecordedEpistemicObservation =
        RecordedEpistemicObservation(
            recordId = "observation-$globalSequence",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = globalSequence.toInt(),
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.PlayerCount(5),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    sequence = globalSequence.toInt(),
                    globalSequence = globalSequence,
                ),
            ),
        )

    private fun legacyObservationLog(): EpistemicObservationLog = EpistemicObservationLog(
        listOf(
            RecordedEpistemicObservation(
                recordId = "legacy-observation",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 3,
                sourceSeat = 1,
                sourceAbility = RoleId("Chef"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.PlayerCount(5),
            ),
        ),
    )
}
