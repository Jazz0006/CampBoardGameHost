package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.MisinformationLedger
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsequenceEvaluatorTest {
    @Test
    fun `repeated targeting is always a penalty and aggressive only reduces it`() {
        val state = state(
            pressureBySeat = mapOf(
                2 to PlayerInformationPressure(
                    seat = 2,
                    directSuspicion = 3,
                    recentTargetCount = 2,
                ),
            ),
        )
        val base = evaluation(pressureDelta = mapOf(2 to 2))

        val gentle = ConsequenceEvaluator.evaluate(base, ConsequenceContext(state, RecommendationStyle.GENTLE))
        val aggressive = ConsequenceEvaluator.evaluate(base, ConsequenceContext(state, RecommendationStyle.AGGRESSIVE))

        assertTrue(gentle.totalScore < aggressive.totalScore)
        assertTrue(aggressive.totalScore < base.totalScore)
        assertTrue("pressure.repeated-target-penalty" in aggressive.explanationCodes)
    }

    @Test
    fun `final day selected target misinformation protects a one shot ability`() {
        val finalState = state(alivePlayers = 3)
        val result = ConsequenceEvaluator.evaluate(
            evaluation(pressureDelta = mapOf(4 to 4)),
            ConsequenceContext(
                state = finalState,
                style = RecommendationStyle.BALANCED,
                isOneShotAbility = true,
                playerSelectedTarget = true,
            ),
        )

        assertEquals(QualityTier.EXPERT_ONLY, result.qualityTier)
        assertTrue("consequence.one-shot-ability-protection" in result.warnings)
        assertTrue("consequence.high-impact-misinformation-penalty" in result.warnings)
        assertTrue("consequence.final-day-impact-penalty" in result.warnings)
    }

    @Test
    fun `recent high impact misinformation lowers later false candidate weight`() {
        val state = state(
            misinformationLedger = MisinformationLedger(
                totalOpportunities = 4,
                falseInformationCount = 3,
                highImpactFalseCount = 2,
                consecutiveFalseCount = 2,
            ),
        )
        val base = evaluation(pressureDelta = mapOf(3 to 1))

        val result = ConsequenceEvaluator.evaluate(base, ConsequenceContext(state, RecommendationStyle.BALANCED))

        assertTrue(result.withinFamilyWeightFixedPoint < base.withinFamilyWeightFixedPoint)
        assertTrue("consequence.high-impact-misinformation-penalty" in result.explanationCodes)
    }

    @Test
    fun `alignment correction opposes the side that is already ahead`() {
        val evilAhead = state(evilAdvantage = 40)
        val base = evaluation(pressureDelta = emptyMap())

        val helpsEvil = ConsequenceEvaluator.evaluate(
            base,
            ConsequenceContext(evilAhead, RecommendationStyle.BALANCED, alignmentImpact = 1),
        )
        val helpsGood = ConsequenceEvaluator.evaluate(
            base,
            ConsequenceContext(evilAhead, RecommendationStyle.BALANCED, alignmentImpact = -1),
        )

        assertTrue(helpsEvil.totalScore < base.totalScore)
        assertTrue(helpsGood.totalScore > base.totalScore)
        assertTrue("consequence.alignment-advantage-adjustment" in helpsGood.explanationCodes)
    }

    @Test
    fun `truthful result is not penalized merely for being one shot or final day`() {
        val base = evaluation(truth = TruthRelation.TRUE_TO_ACTUAL_STATE, pressureDelta = emptyMap())

        val result = ConsequenceEvaluator.evaluate(
            base,
            ConsequenceContext(
                state = state(alivePlayers = 3),
                style = RecommendationStyle.GENTLE,
                isOneShotAbility = true,
                playerSelectedTarget = true,
            ),
        )

        assertEquals(base, result)
    }

    @Test
    fun `dynamic generation applies consequences to explicit target seats`() {
        val context = DynamicGenerationContext(
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Ravenkeeper"),
            recipientSeat = 1,
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.BALANCED,
            state = state(alivePlayers = 3),
            targetSeats = setOf(4),
            isOneShotAbility = true,
            playerSelectedTarget = true,
        )

        val falseCandidate = DynamicCandidateGenerator.generateCategorical(
            listOf(UnreliableCategoricalCandidate("wrong-role", isTruthful = false, misinformationPressure = 4)),
            context,
        ).single()

        assertEquals(setOf(4), falseCandidate.pressureDelta.keys)
        assertEquals(QualityTier.EXPERT_ONLY, falseCandidate.qualityTier)
        assertTrue("consequence.final-day-impact-penalty" in falseCandidate.explanationCodes)
    }

    private fun state(
        alivePlayers: Int = TroubleBrewingFixtures.eightPlayerExample().players.size,
        pressureBySeat: Map<Int, PlayerInformationPressure> = emptyMap(),
        misinformationLedger: MisinformationLedger = MisinformationLedger(),
        evilAdvantage: Int = 0,
    ): DynamicGameState {
        val base = TroubleBrewingFixtures.eightPlayerExample()
        val game = base.copy(players = base.players.mapIndexed { index, player ->
            player.copy(alive = index < alivePlayers)
        })
        return DynamicGameState(
            game = game,
            phase = StorytellerPhase.NIGHT,
            round = 3,
            playerInformationPressureBySeat = pressureBySeat,
            misinformationLedger = misinformationLedger,
            evilAdvantage = evilAdvantage,
        )
    }

    private fun evaluation(
        truth: TruthRelation = TruthRelation.FALSE_TO_ACTUAL_STATE,
        pressureDelta: Map<Int, Int>,
    ): DecisionEvaluation<String> {
        val candidate = DecisionCandidate(
            candidateId = "candidate-1",
            candidateFamilyId = "malfunction-falsehood-role",
            outcome = "result",
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = truth,
            metadata = CandidateMetadata("dynamic-v1", "test"),
        )
        return DecisionEvaluation(
            candidate = candidate,
            qualityTier = QualityTier.RECOMMENDED,
            totalScore = 20,
            withinFamilyWeightFixedPoint = 200,
            finalProbabilityFixedPoint = 0,
            pressureDelta = pressureDelta,
            warnings = emptyList(),
            explanationCodes = emptyList(),
        )
    }
}
