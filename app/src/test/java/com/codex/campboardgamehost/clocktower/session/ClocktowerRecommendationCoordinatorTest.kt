package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateAuditSummary
import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRecommendationCoordinatorTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `coordinator is the single entry point for setup and information`() {
        val coordinator = ClocktowerRecommendationCoordinator()
        val setup = coordinator.recommendSetup(
            SetupCoordinationRequest(game, TroubleBrewingFixtures.roleDefinitions()),
        )
        val information = coordinator.resolveInformation(
            InformationResolutionRequest.Category(
                candidates = listOf(
                    UnreliableCategoricalCandidate("truth", true),
                    UnreliableCategoricalCandidate("lie", false, 2),
                ),
                generation = DynamicGenerationContext(
                    abilityRole = RoleId("Empath"),
                    recipientSeat = 2,
                    reliability = InformationReliability.POISONED,
                    style = RecommendationStyle.BALANCED,
                    state = DynamicGameState(game, StorytellerPhase.NIGHT, 2),
                ),
            ),
        )

        assertEquals(3, setup.plans.size)
        assertEquals(
            setOf("truth", "lie"),
            information.map { (it.candidate.outcome as DynamicInformationOutcome.Category).id }.toSet(),
        )
    }

    @Test
    fun `setup selection consumes committed Drunk identity without recommending it`() {
        val coordinator = ClocktowerRecommendationCoordinator()

        val selected = requireNotNull(
            coordinator.selectSetupPlan(
                SetupCoordinationRequest(game, TroubleBrewingFixtures.roleDefinitions()),
                RecommendationStyle.BALANCED,
            ),
        )

        assertTrue(selected.decisions.none {
            it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
        })
        assertTrue(selected.observations.any { observation ->
            observation.sourceSeat == 6 &&
                observation.perceivedRole == RoleId("Investigator") &&
                observation.reliability == ReliabilityState.DRUNK
        })
        assertEquals(RoleId("Investigator"), game.players.single { it.actualRole == RoleId("Drunk") }.shownRole)
    }

    @Test
    fun `setup AUTO selection is projected from the same unified pool exposed to assisted`() {
        val coordinator = ClocktowerRecommendationCoordinator()
        val plans = coordinator.recommendSetup(
            SetupCoordinationRequest(game, TroubleBrewingFixtures.roleDefinitions()),
        ).plans

        val pool = requireNotNull(coordinator.unifiedSetupPool(plans))
        val auto = requireNotNull(coordinator.selectSetupPlan(plans, RecommendationStyle.BALANCED))

        assertEquals(
            plans.map { it.decisions }.toSet(),
            pool.candidatesFor(com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy.ASSISTED)
                .map { it.payload.decisions }.toSet(),
        )
        assertTrue(
            pool.candidatesFor(com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy.AUTO)
                .map { it.payload.decisions }
                .contains(auto.decisions),
        )
    }

    @Test
    fun `unified setup pool assigns a stable ID to a legal no-op plan`() {
        val noOp = RecommendationPlan(
            decisions = emptyList(),
            observations = emptyList(),
            qualityTier = QualityTier.RECOMMENDED,
            style = RecommendationStyle.BALANCED,
            totalScore = 0,
            scoreItems = emptyList(),
            warnings = emptyList(),
            effectSignature = PlanEffectSignature(),
        )

        val aggressiveNoOp = noOp.copy(style = RecommendationStyle.AGGRESSIVE, totalScore = 1)
        val coordinator = ClocktowerRecommendationCoordinator()
        val pool = requireNotNull(coordinator.unifiedSetupPool(listOf(noOp, aggressiveNoOp)))

        assertEquals(2, pool.rankedCandidates.map { it.candidateId }.toSet().size)
        assertTrue(pool.rankedCandidates.all { it.candidateId.isNotBlank() })
        assertEquals(noOp, coordinator.selectSetupPlan(listOf(noOp, aggressiveNoOp), RecommendationStyle.BALANCED))
    }

    @Test
    fun `coordinator explanation and post game review preserve alternatives and corrections`() {
        val first = event("event-1", "candidate-1")
        val replacement = event("event-2", "candidate-2")
        val coordinator = ClocktowerRecommendationCoordinator(
            DecisionHistoryArchive(
                events = listOf(first, replacement),
                corrections = listOf(DecisionCorrectionEvent("correction-1", "event-1", "event-2", "operator-fix")),
            ),
        )

        val explanation = requireNotNull(coordinator.explainDecision("event-2"))
        val review = coordinator.postGameReview()

        assertEquals(listOf("alternative-candidate"), explanation.alternativeCandidateIds)
        assertEquals(1, review.effectiveDecisionCount)
        assertEquals(1, review.correctionCount)
        assertTrue(review.decisions.single { it.eventId == "event-1" }.corrected)
        assertTrue(!review.decisions.single { it.eventId == "event-2" }.corrected)
    }

    private fun event(eventId: String, selectedCandidateId: String) = StorytellerDecisionEvent(
        eventId = eventId,
        requestId = "request-$eventId",
        idempotencyKey = "game-1:$eventId",
        gameStateRevision = 1,
        playerInputRevision = 1,
        rulesetRef = RulesetRef(
            scriptId = game.script,
            scriptContentHash = "e12f6425ece137da02477a642235c797",
            rulesetVersion = "trouble-brewing-v1",
            sourceRevision = "official-wiki-2026-08-06",
            coverage = RuleCoverage.PARTIAL,
        ),
        algorithmConfigVersion = "v4-pr11",
        selectorVersion = "weighted-stable-v1",
        decisionSeed = 42,
        stateDigest = "state",
        historyDigest = "history",
        selectedCandidateId = selectedCandidateId,
        selectedOutcomeSnapshot = DecisionOutcomeSnapshot("test", emptyMap()),
        abilityState = AbilityState.FUNCTIONING,
        truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
        registrations = emptyList(),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 10,
        finalProbabilityFixedPoint = 600_000,
        pressureDelta = mapOf(2 to 1),
        candidatePoolFingerprint = "pool",
        candidateAudit = listOf(
            CandidateAuditSummary(selectedCandidateId, "natural-truth", QualityTier.RECOMMENDED, 10, 600_000, listOf("selected")),
            CandidateAuditSummary("alternative-candidate", "natural-truth", QualityTier.RECOMMENDED, 9, 400_000, listOf("alternative")),
        ),
        explanationCodes = listOf("selection.weighted-stable-random"),
        status = DecisionEventStatus.APPLIED,
    )
}
