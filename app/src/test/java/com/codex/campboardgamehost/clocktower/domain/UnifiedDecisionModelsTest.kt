package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedDecisionModelsTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val dynamicState = DynamicGameState(
        game = game,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
    )
    private val rulesetRef = RulesetRef(
        scriptId = game.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.PARTIAL,
    )
    private val metadata = CandidateMetadata(
        candidateSchemaVersion = "1",
        decisionType = "numeric-information",
    )

    @Test
    fun `ability malfunction and truth relation remain orthogonal`() {
        val candidate = DecisionCandidate(
            candidateId = "candidate-1",
            candidateFamilyId = "malfunction-truth",
            outcome = 0,
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            metadata = metadata,
        )

        assertEquals(AbilityState.MALFUNCTIONING_POISONED, candidate.abilityState)
        assertEquals(TruthRelation.TRUE_TO_ACTUAL_STATE, candidate.truthRelation)
    }

    @Test
    fun `one candidate can bind multiple registration facts to its final outcome`() {
        val registrations = listOf(
            registration("chef-left", RegistrationQuestion.ALIGNMENT),
            registration("chef-right", RegistrationQuestion.ALIGNMENT),
        )
        val candidate = DecisionCandidate(
            candidateId = "candidate-2",
            candidateFamilyId = "registration-recluse",
            outcome = 1,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            registrations = registrations,
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = 2,
                    sourceAbility = RoleId("Chef"),
                    value = InformationValue.Number(1),
                ),
            ),
            metadata = metadata,
        )

        assertEquals(registrations, candidate.registrations)
        assertEquals(1, candidate.effects.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `registered truth cannot exist without a registration fact`() {
        DecisionCandidate(
            candidateId = "candidate-3",
            candidateFamilyId = "registration-recluse",
            outcome = 1,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_REGISTERED_STATE,
            metadata = metadata,
        )
    }

    @Test
    fun `request separates actual role from simulated ability role`() {
        val request = StorytellerDecisionRequest(
            requestId = "request-1",
            idempotencyKey = "game-1:first-night:seat-6:investigator",
            gameId = "game-1",
            gameStateRevision = 0,
            playerInputRevision = 0,
            round = 1,
            phase = StorytellerPhase.FIRST_NIGHT,
            sourceSeat = 6,
            actorActualRole = RoleId("Drunk"),
            abilityRole = RoleId("Investigator"),
            abilityInstanceId = "seat-6:investigator",
            abilityType = AbilityType.PAIR_INFORMATION,
            detectionSemantics = DetectionSemantics.SPECIFIC_MINION,
            decisionSequence = 0,
            rulesetRef = rulesetRef,
            algorithmConfigVersion = "v4-pr3",
            gameState = dynamicState,
        )

        assertNotEquals(request.actorActualRole, request.abilityRole)
        assertEquals(RoleId("Investigator"), request.abilityRole)
    }

    @Test
    fun `evaluation keeps fixed point weights separate from quality score`() {
        val candidate = DecisionCandidate(
            candidateId = "candidate-4",
            candidateFamilyId = "malfunction-falsehood-numeric",
            outcome = 1,
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = TruthRelation.FALSE_TO_ACTUAL_STATE,
            metadata = metadata,
        )
        val evaluation = DecisionEvaluation(
            candidate = candidate,
            qualityTier = QualityTier.RECOMMENDED,
            totalScore = 42,
            withinFamilyWeightFixedPoint = 750_000,
            finalProbabilityFixedPoint = 300_000,
            pressureDelta = mapOf(4 to 1),
            warnings = emptyList(),
            explanationCodes = listOf("malfunction.falsehood-selected"),
        )

        assertEquals(42, evaluation.totalScore)
        assertTrue(evaluation.withinFamilyWeightFixedPoint > evaluation.finalProbabilityFixedPoint)
    }

    private fun registration(
        interactionId: String,
        question: RegistrationQuestion,
    ) = RegistrationFact(
        interactionId = interactionId,
        subjectSeat = 3,
        registeredAlignment = Alignment.EVIL,
        registrationQuestion = question,
        reason = RegistrationReason.RECLUSE_ABILITY,
    )
}
