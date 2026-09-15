package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class B4HistoricalExactShadowBridgeTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-b4-historical-exact-shadow-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `validated B4 shadow delegates to deterministic mutation-free neutral exact evaluation`() {
        val facts = listOf(
            ActionFact.Protect("actual-protect", 1L, 1),
            ActionFact.Attack("actual-attack", 2L, 2),
            ActionFact.RoleChange(
                actionId = "actual-role-change",
                sequence = 3L,
                targetSeat = 4,
                role = RoleId("Imp"),
                alignment = Alignment.EVIL,
                type = CharacterType.DEMON,
            ),
        )
        val timeline = timelineOf(facts)
        val observationLog = EpistemicObservationLog()
        val initialFormal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val setupKnowledge = A4PlayerKnowledgeFactory.createAll(
            formal = initialFormal,
            perceivedRolesBySeat = perceived,
            observationLog = observationLog,
        ).first { it.recipientSeat == 1 }
        val candidate = EpistemicObservation(
            observationId = "public-alive-candidate",
            snapshotId = initialFormal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 4,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, true),
        )
        val expected = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = rulesetRef,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timeline,
            observationLog = observationLog,
        ).worldSet
        val expectedWorlds = expected.enumeratedWorlds()
        val rolesById = roles.associateBy(RoleDefinition::id)
        val expectedAfterWorlds = expectedWorlds.filter { world ->
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = candidate,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches
        }
        val neutralContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timeline,
            perceivedRolesBySeat = perceived,
            observationLog = observationLog,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )
        val neutralQueries = listOf(
            ExactHypotheticalObservationQuery("alive-seat-2", 1, candidate),
        )
        val timelineBefore = timeline.reducerFacts()
        val observationLogBefore = observationLog.records.toList()

        val neutralFirst = ExactHistoricalHypotheticalObservationEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = neutralContext,
            queries = neutralQueries,
        )
        val neutralSecond = ExactHistoricalHypotheticalObservationEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = neutralContext,
            queries = neutralQueries,
        )

        assertEquals(neutralFirst, neutralSecond)
        assertTrue(neutralFirst is ExactHypotheticalObservationEvaluation.Ready)
        val neutralDiagnostic = (neutralFirst as ExactHypotheticalObservationEvaluation.Ready).diagnostics.single()
        assertEquals(exact(expectedWorlds.size), neutralDiagnostic.before)
        assertEquals(exact(expectedAfterWorlds.size), neutralDiagnostic.after)
        assertEquals(timelineBefore, timeline.reducerFacts())
        assertEquals(observationLogBefore, observationLog.records)

        val report = B4DynamicPlayerWorldSetShadow(
            validatedRuleset = validatedRuleset,
        ).evaluate(
            B4ShadowRequest(
                initialSnapshot = snapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = timeline,
                perceivedRolesBySeat = perceived,
                observationLog = observationLog,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
                candidates = listOf(B4ShadowCandidate("alive-seat-2", 1, candidate)),
            ),
        )

        assertEquals(B4ShadowOutcome.READY, report.outcome)
        assertEquals(neutralDiagnostic.before, report.queries.single().before)
        assertEquals(neutralDiagnostic.after, report.queries.single().after)
    }

    @Test
    fun `neutral exact evaluation and B4 both defer unsupported semantics before baseline evaluation`() {
        val unsupportedRuleset = validatedRuleset.copy(
            script = validatedRuleset.script.copy(
                source = ClocktowerScriptSource.IMPORTED_HOMEBREW,
            ),
        )
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timelineOf(emptyList()),
            perceivedRolesBySeat = perceived,
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )

        val neutral = ExactHistoricalHypotheticalObservationEvaluator.evaluate(
            validatedRuleset = unsupportedRuleset,
            context = context,
            queries = emptyList(),
        )

        assertTrue(neutral is ExactHypotheticalObservationEvaluation.Deferred)
        assertEquals(
            EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            (neutral as ExactHypotheticalObservationEvaluation.Deferred).missingCapabilities,
        )

        val report = B4DynamicPlayerWorldSetShadow(
            validatedRuleset = unsupportedRuleset,
        ).evaluate(
            B4ShadowRequest(
                initialSnapshot = snapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = context.actionTimeline,
                perceivedRolesBySeat = perceived,
                observationLog = context.observationLog,
                hypothesis = context.hypothesis,
                roleDefinitions = roles,
                candidates = emptyList(),
            ),
        )

        assertEquals(B4ShadowOutcome.DEFERRED_B4, report.outcome)
        assertEquals(emptyList<B4CandidateWorldQuery>(), report.queries)
    }

    private fun exact(count: Int): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(count.toLong()))

    private fun timelineOf(facts: List<ActionFact>): ActionFactTimeline =
        ActionFactTimeline(
            facts.map { fact ->
                TimelineBoundActionFact(
                    fact = fact,
                    point = TimelinePoint(
                        phase = StorytellerPhase.FIRST_NIGHT,
                        round = 1,
                        sequence = fact.sequence.toInt(),
                        globalSequence = fact.sequence,
                    ),
                )
            },
        )
}
