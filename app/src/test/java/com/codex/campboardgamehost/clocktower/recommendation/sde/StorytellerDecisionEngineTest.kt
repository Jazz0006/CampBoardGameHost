package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerDecisionEngineTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-1-exact-seam-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `healthy structured information consequence delegates exactly without mutating context`() {
        val timeline = ActionFactTimeline(emptyList())
        val observationLog = EpistemicObservationLog()
        val context = exactContext(timeline, observationLog)
        val observation = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 0,
            observationId = "sde-private-empath-2",
        )
        val request = ExactConsequenceRequest(
            decisionId = "first-night-empath",
            candidates = listOf(
                ExactConsequenceCandidate(
                    candidateId = "empath-0",
                    recipientSeat = 2,
                    observations = listOf(observation),
                ),
            ),
        )
        val decisionContext = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = context,
        )
        val timelineBefore = timeline.reducerFacts()
        val logBefore = observationLog.records.toList()

        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = context,
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "direct-empath-0",
                    recipientSeat = 2,
                    observations = listOf(observation),
                ),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Ready)
        val expected = (direct as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()

        val first = StorytellerDecisionEngine.evaluateExactConsequences(request, decisionContext)
        val second = StorytellerDecisionEngine.evaluateExactConsequences(request, decisionContext)

        assertEquals(first, second)
        assertTrue(first is ExactConsequenceEvaluation.Ready)
        val consequence = (first as ExactConsequenceEvaluation.Ready).consequences.single()
        assertEquals("empath-0", consequence.candidateId)
        assertEquals(expected.before, consequence.diagnostics.before)
        assertEquals(expected.after, consequence.diagnostics.after)
        assertEquals(expected.beforeStructure, consequence.diagnostics.beforeStructure)
        assertEquals(expected.afterStructure, consequence.diagnostics.afterStructure)
        assertEquals(timelineBefore, timeline.reducerFacts())
        assertEquals(logBefore, observationLog.records)
    }

    @Test
    fun `observation bundle is forwarded as one candidate consequence`() {
        val exactContext = exactContext(ActionFactTimeline(emptyList()), EpistemicObservationLog())
        val firstObservation = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 0,
            observationId = "sde-bundle-empath-0",
        )
        val secondObservation = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 1,
            observationId = "sde-bundle-empath-1",
        )
        val observations = listOf(firstObservation, secondObservation)
        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = exactContext,
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "direct-bundle",
                    recipientSeat = 2,
                    observations = observations,
                ),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Ready)
        val expected = (direct as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()

        val evaluation = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest(
                decisionId = "observation-bundle",
                candidates = listOf(
                    ExactConsequenceCandidate(
                        candidateId = "empath-bundle",
                        recipientSeat = 2,
                        observations = observations,
                    ),
                ),
            ),
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        assertTrue(evaluation is ExactConsequenceEvaluation.Ready)
        val consequence = (evaluation as ExactConsequenceEvaluation.Ready).consequences.single()
        assertEquals("empath-bundle", consequence.candidateId)
        assertEquals(expected.before, consequence.diagnostics.before)
        assertEquals(expected.after, consequence.diagnostics.after)
        assertEquals(expected.beforeStructure, consequence.diagnostics.beforeStructure)
        assertEquals(expected.afterStructure, consequence.diagnostics.afterStructure)
    }

    @Test
    fun `multiple candidates are evaluated from one immutable exact context`() {
        val timeline = ActionFactTimeline(emptyList())
        val observationLog = EpistemicObservationLog()
        val exactContext = exactContext(timeline, observationLog)
        val empath = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 0,
            observationId = "sde-multi-empath-2",
        )
        val chef = healthyNumericObservation(
            recipientSeat = 1,
            ability = "Chef",
            number = 1,
            observationId = "sde-multi-chef-1",
        )
        val request = ExactConsequenceRequest(
            decisionId = "healthy-numeric-multi",
            candidates = listOf(
                ExactConsequenceCandidate("empath-0", 2, listOf(empath)),
                ExactConsequenceCandidate("chef-1", 1, listOf(chef)),
            ),
        )
        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = exactContext,
            queries = listOf(
                ExactHypotheticalObservationBundleQuery("direct-empath", 2, listOf(empath)),
                ExactHypotheticalObservationBundleQuery("direct-chef", 1, listOf(chef)),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Ready)
        val expected = (direct as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics
        val timelineBefore = timeline.reducerFacts()
        val logBefore = observationLog.records.toList()

        val evaluation = StorytellerDecisionEngine.evaluateExactConsequences(
            request = request,
            context = ExactConsequenceContext(validatedRuleset, exactContext),
        )

        assertTrue(evaluation is ExactConsequenceEvaluation.Ready)
        val consequences = (evaluation as ExactConsequenceEvaluation.Ready).consequences
        assertEquals(listOf("empath-0", "chef-1"), consequences.map(CandidateConsequence::candidateId))
        assertEquals(expected[0].before, consequences[0].diagnostics.before)
        assertEquals(expected[0].after, consequences[0].diagnostics.after)
        assertEquals(expected[0].beforeStructure, consequences[0].diagnostics.beforeStructure)
        assertEquals(expected[0].afterStructure, consequences[0].diagnostics.afterStructure)
        assertEquals(expected[1].before, consequences[1].diagnostics.before)
        assertEquals(expected[1].after, consequences[1].diagnostics.after)
        assertEquals(expected[1].beforeStructure, consequences[1].diagnostics.beforeStructure)
        assertEquals(expected[1].afterStructure, consequences[1].diagnostics.afterStructure)
        assertEquals(timelineBefore, timeline.reducerFacts())
        assertEquals(logBefore, observationLog.records)
    }

    @Test
    fun `exact capability deferral is surfaced without heuristic fallback`() {
        val unsupportedRuleset = validatedRuleset.copy(
            script = validatedRuleset.script.copy(
                source = ClocktowerScriptSource.IMPORTED_HOMEBREW,
            ),
        )
        val observation = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 0,
            observationId = "sde-unsupported-empath-2",
        )
        val request = ExactConsequenceRequest(
            decisionId = "unsupported",
            candidates = listOf(
                ExactConsequenceCandidate(
                    candidateId = "empath-0",
                    recipientSeat = 2,
                    observations = listOf(observation),
                ),
            ),
        )
        val exactContext = exactContext(ActionFactTimeline(emptyList()), EpistemicObservationLog())
        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = unsupportedRuleset,
            context = exactContext,
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "direct-unsupported",
                    recipientSeat = 2,
                    observations = listOf(observation),
                ),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Deferred)

        val evaluation = StorytellerDecisionEngine.evaluateExactConsequences(
            request = request,
            context = ExactConsequenceContext(
                validatedRuleset = unsupportedRuleset,
                exactContext = exactContext,
            ),
        )

        assertTrue(evaluation is ExactConsequenceEvaluation.Deferred)
        assertEquals(
            (direct as ExactHypotheticalObservationBundleEvaluation.Deferred).missingCapabilities,
            (evaluation as ExactConsequenceEvaluation.Deferred).missingCapabilities,
        )
    }

    @Test
    fun `malformed candidate identity fails before exact evaluation`() {
        val observation = healthyNumericObservation(
            recipientSeat = 2,
            ability = "Empath",
            number = 0,
            observationId = "sde-malformed-empath-2",
        )
        var failed = false

        try {
            ExactConsequenceRequest(
                decisionId = "duplicate-candidate-ids",
                candidates = listOf(
                    ExactConsequenceCandidate("duplicate", 2, listOf(observation)),
                    ExactConsequenceCandidate("duplicate", 2, listOf(observation)),
                ),
            )
        } catch (_: IllegalArgumentException) {
            failed = true
        }

        assertTrue("Duplicate candidate IDs must fail at the SDE request boundary.", failed)
    }

    private fun exactContext(
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ) = ExactHistoricalHypotheticalContext(
        initialSnapshot = snapshot,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = timeline,
        perceivedRolesBySeat = perceived,
        observationLog = observationLog,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
    )

    private fun healthyNumericObservation(
        recipientSeat: Int,
        ability: String,
        number: Int,
        observationId: String,
    ): EpistemicObservation {
        val roleId = RoleId(ability)
        val information = EffectDraft.PlayerInformation(
            recipientSeat = recipientSeat,
            sourceAbility = roleId,
            value = InformationValue.Number(number),
        )
        return EpistemicObservation(
            observationId = observationId,
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = recipientSeat,
            sourceAbility = roleId,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(recipientSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = snapshot.gameState,
                information = information,
                roleDefinitions = roles,
            ),
        )
    }
}
