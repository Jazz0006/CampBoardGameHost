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
        val observation = healthyEmpathObservation()
        val request = StorytellerDecisionRequest(
            decisionId = "first-night-empath",
            candidates = listOf(
                StorytellerDecisionCandidate(
                    candidateId = "empath-0",
                    recipientSeat = 2,
                    observation = observation,
                ),
            ),
        )
        val decisionContext = StorytellerDecisionContext(
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

        val first = StorytellerDecisionEngine.evaluate(request, decisionContext)
        val second = StorytellerDecisionEngine.evaluate(request, decisionContext)

        assertEquals(first, second)
        assertTrue(first is StorytellerDecisionEvaluation.Ready)
        val consequence = (first as StorytellerDecisionEvaluation.Ready).consequences.single()
        assertEquals("empath-0", consequence.candidateId)
        assertEquals(expected.before, consequence.diagnostics.before)
        assertEquals(expected.after, consequence.diagnostics.after)
        assertEquals(expected.beforeStructure, consequence.diagnostics.beforeStructure)
        assertEquals(expected.afterStructure, consequence.diagnostics.afterStructure)
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
        val request = StorytellerDecisionRequest(
            decisionId = "unsupported",
            candidates = listOf(
                StorytellerDecisionCandidate(
                    candidateId = "empath-0",
                    recipientSeat = 2,
                    observation = healthyEmpathObservation(),
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
                    observations = listOf(healthyEmpathObservation()),
                ),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Deferred)

        val evaluation = StorytellerDecisionEngine.evaluate(
            request = request,
            context = StorytellerDecisionContext(
                validatedRuleset = unsupportedRuleset,
                exactContext = exactContext,
            ),
        )

        assertTrue(evaluation is StorytellerDecisionEvaluation.Deferred)
        assertEquals(
            (direct as ExactHypotheticalObservationBundleEvaluation.Deferred).missingCapabilities,
            (evaluation as StorytellerDecisionEvaluation.Deferred).missingCapabilities,
        )
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

    private fun healthyEmpathObservation(): EpistemicObservation {
        val information = EffectDraft.PlayerInformation(
            recipientSeat = 2,
            sourceAbility = RoleId("Empath"),
            value = InformationValue.Number(0),
        )
        return EpistemicObservation(
            observationId = "sde-private-empath-2",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 0,
            sourceSeat = 2,
            sourceAbility = RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(2),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                game = snapshot.gameState,
                information = information,
                roleDefinitions = roles,
            ),
        )
    }
}
