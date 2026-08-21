package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CrossTypeTimelineCompatibilityTest {
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val perceived = snapshot.gameState.players.associate { it.seat to (it.shownRole ?: it.actualRole) }
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test fun `B4 rejects action and observation sharing one global sequence`() {
        assertThrows(IllegalArgumentException::class.java) {
            request(
                actionTimeline = actionTimeline(10L),
                observationLog = globalObservationLog(10L),
            )
        }
    }

    @Test fun `B4 accepts distinct action and observation global sequences`() {
        val request = request(
            actionTimeline = actionTimeline(10L),
            observationLog = globalObservationLog(11L),
        )

        assertEquals(10L, request.actionTimeline.entries.single().point.globalSequence)
        assertEquals(
            11L,
            (request.observationLog.records.single().timelineBinding as ObservationTimelineBinding.Global)
                .point.globalSequence,
        )
    }

    @Test fun `B4 rejects globally bound actions combined with legacy local observations`() {
        assertThrows(IllegalArgumentException::class.java) {
            request(
                actionTimeline = actionTimeline(10L),
                observationLog = legacyObservationLog(),
            )
        }
    }

    @Test fun `B4 keeps observation-only legacy compatibility when action timeline is empty`() {
        val request = request(
            actionTimeline = ActionFactTimeline(),
            observationLog = legacyObservationLog(),
        )

        assertEquals(1, request.observationLog.records.size)
        assertEquals(ObservationTimelineBinding.LegacyLocal, request.observationLog.records.single().timelineBinding)
    }

    private fun request(
        actionTimeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): B4ShadowRequest = B4ShadowRequest(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = actionTimeline,
        perceivedRolesBySeat = perceived,
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
        candidates = emptyList(),
    )

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

    private fun globalObservationLog(globalSequence: Long): EpistemicObservationLog = EpistemicObservationLog(
        listOf(
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
