package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactHistoricalHypotheticalObservationBundleEvaluatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "fn-bundle-exact-evaluator-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)

    @Test
    fun `singleton bundle preserves validated B4 exact consequence and does not mutate history`() {
        val timeline = timelineOf(emptyList())
        val observationLog = EpistemicObservationLog()
        val observation = publicObservation(
            id = "singleton-poisoner",
            sequence = 1,
            proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        )
        val context = context(timeline, observationLog)
        val query = ExactHypotheticalObservationBundleQuery(
            bundleId = "singleton",
            recipientSeat = 1,
            observations = listOf(observation),
        )
        val timelineBefore = timeline.reducerFacts()
        val logBefore = observationLog.records.toList()
        val observationsBefore = query.observations.toList()

        val first = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            queries = listOf(query),
        )
        val second = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            queries = listOf(query),
        )
        assertEquals(first, second)
        assertTrue(first is ExactHypotheticalObservationBundleEvaluation.Ready)
        val diagnostic = (first as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()

        val b4 = B4DynamicPlayerWorldSetShadow(validatedRuleset).evaluate(
            B4ShadowRequest(
                initialSnapshot = snapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = timeline,
                perceivedRolesBySeat = perceived,
                observationLog = observationLog,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
                candidates = listOf(B4ShadowCandidate("singleton", 1, observation)),
            ),
        )

        assertEquals(B4ShadowOutcome.READY, b4.outcome)
        assertEquals(diagnostic.before, b4.queries.single().before)
        assertEquals(diagnostic.after, b4.queries.single().after)
        assertEquals(timelineBefore, timeline.reducerFacts())
        assertEquals(logBefore, observationLog.records)
        assertEquals(observationsBefore, query.observations)
    }

    @Test
    fun `bundle observations are conjoined on the same baseline world and order is irrelevant`() {
        val timeline = timelineOf(emptyList())
        val observationLog = EpistemicObservationLog()
        val poisoner = publicObservation(
            id = "poisoner-at-4",
            sequence = 1,
            proposition = InformationProposition.RoleAt(4, RoleId("Poisoner")),
        )
        val imp = publicObservation(
            id = "imp-at-5",
            sequence = 2,
            proposition = InformationProposition.RoleAt(5, RoleId("Imp")),
        )
        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(timeline, observationLog),
            queries = listOf(
                ExactHypotheticalObservationBundleQuery("poisoner-only", 1, listOf(poisoner)),
                ExactHypotheticalObservationBundleQuery("imp-only", 1, listOf(imp)),
                ExactHypotheticalObservationBundleQuery("forward", 1, listOf(poisoner, imp)),
                ExactHypotheticalObservationBundleQuery("reverse", 1, listOf(imp, poisoner)),
            ),
        )

        assertTrue(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready)
        val diagnostics = (evaluation as ExactHypotheticalObservationBundleEvaluation.Ready)
            .diagnostics.associateBy(ExactHypotheticalObservationBundleDiagnostics::bundleId)
        val baselineWorlds = baselineWorlds(timeline, observationLog)
        val rolesById = roles.associateBy(RoleDefinition::id)
        val expectedIntersection = baselineWorlds.count { world ->
            listOf(poisoner, imp).all { observation ->
                TroubleBrewingWorldObservationEvaluator.evaluate(
                    world = world,
                    roles = rolesById,
                    observation = observation,
                    hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                ).matches
            }
        }

        assertEquals(exact(baselineWorlds.size), diagnostics.getValue("forward").before)
        assertEquals(exact(expectedIntersection), diagnostics.getValue("forward").after)
        assertEquals(diagnostics.getValue("forward").after, diagnostics.getValue("reverse").after)
        assertTrue(
            diagnostics.getValue("forward").after.value <=
                diagnostics.getValue("poisoner-only").after.value,
        )
        assertTrue(
            diagnostics.getValue("forward").after.value <=
                diagnostics.getValue("imp-only").after.value,
        )
    }

    @Test
    fun `supported contradictory bundle is exact zero rather than deferred`() {
        val poisonerAtFour = InformationProposition.RoleAt(4, RoleId("Poisoner"))
        val positive = publicObservation("poisoner-positive", 1, poisonerAtFour)
        val negative = publicObservation(
            "poisoner-negative",
            2,
            InformationProposition.Not(poisonerAtFour),
        )

        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context(timelineOf(emptyList()), EpistemicObservationLog()),
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "contradiction",
                    recipientSeat = 1,
                    observations = listOf(positive, negative),
                ),
            ),
        )

        assertTrue(evaluation is ExactHypotheticalObservationBundleEvaluation.Ready)
        val diagnostic = (evaluation as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()
        assertEquals(exact(0), diagnostic.after)
    }

    @Test
    fun `unsupported exact semantics defer with explicit missing capabilities`() {
        val unsupportedRuleset = validatedRuleset.copy(
            script = validatedRuleset.script.copy(
                source = ClocktowerScriptSource.IMPORTED_HOMEBREW,
            ),
        )

        val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = unsupportedRuleset,
            context = context(timelineOf(emptyList()), EpistemicObservationLog()),
            queries = emptyList(),
        )

        assertTrue(evaluation is ExactHypotheticalObservationBundleEvaluation.Deferred)
        assertEquals(
            EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            (evaluation as ExactHypotheticalObservationBundleEvaluation.Deferred).missingCapabilities,
        )
    }

    private fun context(
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): ExactHistoricalHypotheticalContext = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = timeline,
        perceivedRolesBySeat = perceived,
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun baselineWorlds(
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): List<EnumeratedWorld> {
        val knowledge = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceived,
            observationLog = observationLog,
        ).first { it.recipientSeat == 1 }
        return EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = rulesetRef,
            setupKnowledge = knowledge,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timeline,
            observationLog = observationLog,
        ).worldSet.enumeratedWorlds()
    }

    private fun publicObservation(
        id: String,
        sequence: Int,
        proposition: InformationProposition,
    ): EpistemicObservation = EpistemicObservation(
        observationId = id,
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = proposition,
    )

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
