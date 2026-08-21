package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class B4DynamicPlayerWorldSetShadowTest {
    private val snapshot = A4RuntimeFixtures.snapshot()
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { it.seat to (it.shownRole ?: it.actualRole) }

    @Test fun `public death is replayed before and after candidate world queries`() {
        val facts = listOf(ActionFact.Death("night-2-death", 1, 2))
        val formal = formalFor(facts)
        val candidate = EpistemicObservation(
            "candidate-empath", formal.snapshotId, StorytellerPhase.NIGHT, 2, 2, 1, RoleId("Chef"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 1, listOf(5, 3), 0),
        )

        val report = B4DynamicPlayerWorldSetShadow().evaluate(request(facts, B4ShadowCandidate("empath-0", 1, candidate)))

        assertEquals(B4ShadowOutcome.READY, report.outcome)
        assertEquals(1, report.queries.size)
        assertTrue(report.queries.single().before is WorldCardinality.Exact)
        assertTrue(report.queries.single().after is WorldCardinality.Exact)
    }

    @Test fun `poison replacement remains a secret and does not become pseudo unsat`() {
        val facts = listOf(ActionFact.Poison("poison-1", 1, 1), ActionFact.Poison("poison-2", 2, 2))
        val formal = formalFor(facts)
        val candidate = EpistemicObservation(
            "candidate-chef", formal.snapshotId, StorytellerPhase.NIGHT, 2, 3, 1, RoleId("Chef"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 0),
        )
        val report = B4DynamicPlayerWorldSetShadow().evaluate(request(facts, B4ShadowCandidate("chef-0", 1, candidate)))

        assertEquals(B4ShadowOutcome.READY, report.outcome)
        assertTrue(report.toString().contains("poison-2").not())
        assertTrue(report.toString().contains("targetSeat").not())
    }

    @Test fun `changing only the hidden poison target does not change the shadow report`() {
        val firstFacts = listOf(ActionFact.Poison("poison", 1, 1))
        val secondFacts = listOf(ActionFact.Poison("poison", 1, 2))
        val firstCandidate = candidateFor(formalFor(firstFacts))
        val secondCandidate = candidateFor(formalFor(secondFacts))

        val first = B4DynamicPlayerWorldSetShadow().evaluate(request(firstFacts, firstCandidate))
        val second = B4DynamicPlayerWorldSetShadow().evaluate(request(secondFacts, secondCandidate))

        assertEquals(first, second)
    }

    @Test fun `b4 request consumes shared action timeline canonical order`() {
        val facts = listOf(
            ActionFact.Poison("poison-1", 1, 1),
            ActionFact.Poison("poison-2", 2, 2),
        )
        val formal = formalFor(facts)
        val candidate = candidateFor(formal)
        val forward = request(timelineOf(facts), candidate)
        val reverse = request(timelineOf(facts.reversed()), candidate)

        assertEquals(listOf(1L, 2L), forward.actionTimeline.reducerFacts().map(ActionFact::sequence))
        assertEquals(forward.actionTimeline, reverse.actionTimeline)
        assertEquals(
            B4DynamicPlayerWorldSetShadow().evaluate(forward),
            B4DynamicPlayerWorldSetShadow().evaluate(reverse),
        )
    }

    @Test fun `b4 observation replay keeps shared global chronology across public private split`() {
        val formalSnapshotId = "b4-global-observation-snapshot"
        val earlierPrivate = EpistemicObservation(
            observationId = "earlier-private",
            snapshotId = formalSnapshotId,
            phase = StorytellerPhase.DAY,
            round = 1,
            sequence = 99,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, true),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(StorytellerPhase.DAY, round = 1, sequence = 99, globalSequence = 10L),
            ),
        )
        val laterPublic = EpistemicObservation(
            observationId = "later-public",
            snapshotId = formalSnapshotId,
            phase = StorytellerPhase.NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(3, true),
            timelineBinding = ObservationTimelineBinding.Global(
                TimelinePoint(StorytellerPhase.NIGHT, round = 1, sequence = 0, globalSequence = 20L),
            ),
        )
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "b4-global-replay",
            formalSnapshotId = formalSnapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            publicObservations = listOf(laterPublic),
            privateObservations = listOf(earlierPrivate),
        )

        assertEquals(
            listOf(10L, 20L),
            knowledge.b4ReplayObservationsInTimelineOrder().map {
                (it.timelineBinding as ObservationTimelineBinding.Global).point.globalSequence
            },
        )
    }

    @Test fun `unmodelled transition is explicitly deferred rather than reported unsat`() {
        val facts = listOf(ActionFact.RoleChange("starpass", 1, 4, RoleId("Imp"),
            com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
            com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON))

        val report = B4DynamicPlayerWorldSetShadow().evaluate(request(facts, placeholderCandidate()))

        assertEquals(B4ShadowOutcome.DEFERRED_B4, report.outcome)
        assertTrue(report.queries.isEmpty())
    }

    @Test fun `timeline is canonical and persists every B4 action shape`() {
        val facts = listOf(
            ActionFact.PhaseAdvance("dawn-2", 7, StorytellerPhase.DAWN, 2),
            ActionFact.RoleChange("starpass", 6, 4, RoleId("Imp"),
                com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON),
            ActionFact.Death("death", 5, 5),
            ActionFact.Execution("execution", 4, 3),
            ActionFact.Attack("attack", 3, 5),
            ActionFact.Protect("protect", 2, 2),
            ActionFact.Poison("poison", 1, 1),
        )

        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1, timeline = facts.reversed())

        assertEquals((1L..7L).toList(), formal.timeline.map(ActionFact::sequence))
        assertEquals(formal, EpistemicSemanticJson.decodeFormalGameState(EpistemicSemanticJson.encode(formal)))
    }

    private fun request(facts: List<ActionFact>, candidate: B4ShadowCandidate): B4ShadowRequest =
        request(timelineOf(facts), candidate)

    private fun request(actionTimeline: ActionFactTimeline, candidate: B4ShadowCandidate) = B4ShadowRequest(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = actionTimeline,
        perceivedRolesBySeat = perceived,
        observationLog = EpistemicObservationLog(),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
        candidates = listOf(candidate),
    )

    private fun timelineOf(facts: List<ActionFact>): ActionFactTimeline =
        facts.fold(ActionFactTimeline()) { timeline, fact ->
            timeline.append(
                TimelineBoundActionFact(
                    fact = fact,
                    point = TimelinePoint(
                        phase = StorytellerPhase.FIRST_NIGHT,
                        round = 1,
                        sequence = fact.sequence.toInt(),
                        globalSequence = fact.sequence,
                    ),
                ),
            )
        }

    private fun formalFor(facts: List<ActionFact>): FormalGameState {
        val reduced = DynamicActionReducer.reduce(snapshot, StorytellerPhase.FIRST_NIGHT, 1, facts)
        val deaths = facts.filter { it is ActionFact.Death || it is ActionFact.Execution }.map {
            InformationProposition.AliveAt((it as? ActionFact.Death)?.targetSeat ?: (it as ActionFact.Execution).targetSeat, false)
        }
        return FormalGameState.from(reduced.snapshot, reduced.phase, reduced.round, deaths, timeline = facts)
    }

    private fun placeholderCandidate(): B4ShadowCandidate = B4ShadowCandidate(
        "unused", 1, EpistemicObservation("unused", "not-used", StorytellerPhase.NIGHT, 2, 0, 1,
            RoleId("Chef"), ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.RoleAt(2, RoleId("Imp"))),
    )

    private fun candidateFor(formal: FormalGameState): B4ShadowCandidate = B4ShadowCandidate(
        "chef-0", 1, EpistemicObservation("candidate-chef", formal.snapshotId, StorytellerPhase.NIGHT, 2, 3,
            1, RoleId("Chef"), ObservationVisibility.PRIVATE, setOf(1),
            ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 0)),
    )
}
