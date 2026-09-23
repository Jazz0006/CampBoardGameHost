package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import com.codex.campboardgamehost.clocktower.session.InformationResolutionRequest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredInformationShadowAdapterTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-1e-shadow-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `healthy structured numeric shadow preserves candidate confirmation and session inputs`() {
        val revision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        )
        val decisionContext = structuredEmpathContext(revision)
        val timeline = ActionFactTimeline(emptyList())
        val observationLog = EpistemicObservationLog()
        val exactContext = ExactConsequenceContext(
            validatedRuleset = validatedRuleset,
            exactContext = exactHistoricalContext(timeline, observationLog),
        )
        val snapshotBefore = decisionContext.snapshot
        val recommendedCandidateId = snapshotBefore.recommendedCandidateIds.single()
        val confirmationBefore = decisionContext.confirm(
            candidateId = recommendedCandidateId,
            source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
            currentRevision = revision,
        )
        val timelineBefore = timeline.reducerFacts()
        val observationsBefore = observationLog.records.toList()

        val shadow = StructuredInformationShadowAdapter.evaluate(
            decisionContext = decisionContext,
            exactContext = exactContext,
        )

        assertEquals(snapshotBefore, shadow.informationSnapshot)
        assertEquals(snapshotBefore.legalCandidateIds, shadow.plannedDecisions.map(PlannedDecisionRef::candidateId))
        assertTrue(shadow.plannedDecisions.all { it.isCurrentFor(snapshotBefore) })
        assertTrue(shadow.consequences is ExactConsequenceEvaluation.Ready)
        assertEquals(
            snapshotBefore.legalCandidateIds,
            (shadow.consequences as ExactConsequenceEvaluation.Ready).consequences.map(CandidateConsequence::candidateId),
        )
        assertEquals(
            confirmationBefore,
            decisionContext.confirm(
                candidateId = recommendedCandidateId,
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                currentRevision = revision,
            ),
        )
        assertEquals(timelineBefore, timeline.reducerFacts())
        assertEquals(observationsBefore, observationLog.records)
        assertEquals(revision, shadow.informationSnapshot.revision)
    }

    @Test
    fun `historical confirmation features are attached without changing v1 policy ordering`() {
        val revision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        )
        val decisionContext = structuredEmpathContext(revision)
        val shadow = StructuredInformationShadowAdapter.evaluate(
            decisionContext = decisionContext,
            exactContext = ExactConsequenceContext(
                validatedRuleset = validatedRuleset,
                exactContext = exactHistoricalContext(ActionFactTimeline(emptyList()), EpistemicObservationLog()),
            ),
        )

        val features = shadow.featureEvaluation as DecisionFeatureEvaluation.Ready
        features.candidates.forEach { candidate ->
            val confirmation = candidate.features.confirmationChainImpact
            assertTrue(confirmation is FeatureProjection.Projected)
            confirmation as FeatureProjection.Projected
            assertTrue(confirmation.value.historicalObservationImpacts.isEmpty())
        }

        val strategicOnly = DecisionFeatureEvaluation.Ready(
            candidates = features.candidates.map { candidate ->
                candidate.copy(
                    features = candidate.features.copy(
                        confirmationChainImpact =
                            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
                    ),
                )
            },
        )
        assertEquals(
            BeginnerConservativeV1Policy.evaluate(strategicOnly),
            shadow.policyEvaluation,
        )
    }

    @Test
    fun `exact deferral stays separate from structured recommendation authority`() {
        val revision = InformationDecisionRevision(
            gameStateRevision = snapshot.gameStateRevision,
            playerInputRevision = snapshot.playerInputRevision,
        )
        val decisionContext = structuredEmpathContext(revision)
        val unsupportedRuleset = validatedRuleset.copy(
            script = validatedRuleset.script.copy(source = ClocktowerScriptSource.IMPORTED_HOMEBREW),
        )
        val recommendedBefore = decisionContext.snapshot.recommendedCandidateIds

        val shadow = StructuredInformationShadowAdapter.evaluate(
            decisionContext = decisionContext,
            exactContext = ExactConsequenceContext(
                validatedRuleset = unsupportedRuleset,
                exactContext = exactHistoricalContext(ActionFactTimeline(emptyList()), EpistemicObservationLog()),
            ),
        )

        assertTrue(shadow.consequences is ExactConsequenceEvaluation.Deferred)
        assertEquals(recommendedBefore, shadow.informationSnapshot.recommendedCandidateIds)
        assertEquals(decisionContext.snapshot.legalCandidateIds, shadow.plannedDecisions.map(PlannedDecisionRef::candidateId))
        val recommendedCandidateId = recommendedBefore.single()
        assertTrue(
            decisionContext.confirm(
                candidateId = recommendedCandidateId,
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                currentRevision = revision,
            ).confirmed != null,
        )
    }

    private fun structuredEmpathContext(revision: InformationDecisionRevision) =
        ClocktowerRecommendationCoordinator().let { coordinator ->
            val evaluations = coordinator.resolveNumberInformation(
                InformationResolutionRequest.Number(
                    context = UnreliableNumberContext(
                        trueValue = 0,
                        minimumValue = 0,
                        maximumValue = 2,
                        pressureCostPerPoint = 1,
                    ),
                    generation = DynamicGenerationContext(
                        abilityRole = RoleId("Empath"),
                        recipientSeat = 2,
                        reliability = InformationReliability.RELIABLE,
                        style = RecommendationStyle.BALANCED,
                        targetSeats = setOf(1, 3),
                    ),
                ),
            )
            val recommendedCandidateIds = evaluations
                .filter { it.candidate.outcome.value == 0 }
                .mapTo(linkedSetOf()) { it.candidate.candidateId }
            coordinator.informationDecisionContext(
                evaluations = evaluations,
                recommendedCandidateIds = recommendedCandidateIds,
                revision = revision,
                semanticIdentity = "numeric|Empath|${snapshot.gameId}|FIRST_NIGHT|1|0|2|LIVING_EVIL_NEIGHBOURS",
                draftOf = { evaluation -> empathDraft(evaluation.candidate.outcome) },
            )
        }

    private fun empathDraft(outcome: DynamicInformationOutcome.Number) = EpistemicObservationDraft(
        recordId = "sde-1e-empath-${outcome.value}",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 0,
        sourceSeat = 2,
        sourceAbility = RoleId("Empath"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(2),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 2,
            subjectSeats = listOf(1, 3),
            value = outcome.value,
        ),
    )

    private fun exactHistoricalContext(
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
}
