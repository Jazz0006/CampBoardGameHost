package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.CandidatePoolBuilder
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryCooldownTest {
    @Test
    fun `history retains newest ten games in canonical order`() {
        val history = (0..10).fold(CrossGameHistory()) { current, index ->
            current.append(signature(shownCharacter = "Role-$index"))
        }

        assertEquals(10, history.recentSignatures.size)
        assertEquals(RoleId("Role-10"), history.recentSignatures.first().shownCharacter)
        assertEquals(RoleId("Role-1"), history.recentSignatures.last().shownCharacter)
        assertEquals(history.digest(), CrossGameHistory(history.recentSignatures).digest())
    }

    @Test
    fun `committed impaired ability identity does not enter recommendation history fingerprint`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val librarian = HistoricalClueSignature.fromSetupPlan(
            game,
            planWithObservation(candidateSeats = listOf(2, 4), perceivedRole = "Librarian"),
        )
        val investigator = HistoricalClueSignature.fromSetupPlan(
            game,
            planWithObservation(candidateSeats = listOf(2, 4), perceivedRole = "Investigator"),
        )

        assertEquals(librarian, investigator)
        assertEquals(librarian.canonical(), investigator.canonical())
    }

    @Test
    fun `persistent fingerprint penalties decay with age`() {
        val candidate = signature("Investigator").copy(candidateAlignmentPattern = "EVIL,GOOD")
        val recent = HistoryCooldown.multiplierFixedPoint(candidate, CrossGameHistory(listOf(candidate)))
        val older = HistoryCooldown.multiplierFixedPoint(
            candidate,
            CrossGameHistory(List(5) { signature("Role-$it") } + candidate),
        )

        assertTrue(recent < older)
        assertTrue(older < WeightedStableSelector.FIXED_POINT_SCALE)
    }

    @Test
    fun `generic impaired pair observation participates in setup cooldown by clue content`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val repeated = HistoricalClueSignature.fromSetupPlan(
            game,
            planWithObservation(candidateSeats = listOf(2, 4), perceivedRole = "Librarian"),
        )
        val withMatchingHistory = HistoryCooldown.multiplierFixedPoint(
            repeated,
            CrossGameHistory(listOf(repeated)),
        )
        val withoutHistory = HistoryCooldown.multiplierFixedPoint(
            repeated,
            CrossGameHistory(),
        )

        assertEquals(RoleId("Drunk"), repeated.shownCharacter)
        assertTrue(withMatchingHistory < withoutHistory)
    }

    @Test
    fun `cooldown changes only weights inside the already selected quality pool`() {
        val evaluations = listOf(
            evaluation("best", score = 20),
            evaluation("inside", score = 17),
            evaluation("outside", score = 10),
        )
        val pool = CandidatePoolBuilder.build(evaluations, scoreTolerance = 4)
        val history = CrossGameHistory(listOf(signature(shownCharacter = "Investigator")))
        val cooled = HistoryCooldown.apply(pool, history) { item ->
            signature(shownCharacter = if (item.candidate.candidateId == "best") "Investigator" else "Chef")
        }

        assertEquals(listOf("best", "inside"), cooled.map { it.candidate.candidateId })
        assertTrue(cooled.first { it.candidate.candidateId == "best" }.withinFamilyWeightFixedPoint < 100)
        assertEquals(100, cooled.first { it.candidate.candidateId == "inside" }.withinFamilyWeightFixedPoint)
        assertTrue(cooled.all { it.qualityTier == QualityTier.RECOMMENDED && it.totalScore in setOf(20, 17) })
    }

    @Test
    fun `canonical signature sorts demon bluffs`() {
        val first = signature("Chef").copy(demonBluffs = setOf(RoleId("Monk"), RoleId("Slayer")))
        val second = signature("Chef").copy(demonBluffs = setOf(RoleId("Slayer"), RoleId("Monk")))

        assertEquals(first.canonical(), second.canonical())
    }

    private fun planWithObservation(
        candidateSeats: List<Int>,
        perceivedRole: String,
    ) = RecommendationPlan(
        decisions = emptyList(),
        observations = listOf(
            AbilityObservation(
                sourceSeat = 6,
                perceivedRole = RoleId(perceivedRole),
                shownRole = RoleId("Drunk"),
                candidateSeats = candidateSeats,
                reliability = ReliabilityState.DRUNK,
                semanticTruth = SemanticTruth.FALSE,
            ),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        style = RecommendationStyle.BALANCED,
        totalScore = 0,
        scoreItems = emptyList(),
        warnings = emptyList(),
        effectSignature = PlanEffectSignature(),
    )

    private fun signature(shownCharacter: String) = HistoricalClueSignature(
        decisionType = "setup-plan",
        shownCharacter = RoleId(shownCharacter),
    )

    private fun evaluation(id: String, score: Int): DecisionEvaluation<String> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = id,
            candidateFamilyId = "setup-plan",
            outcome = id,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            metadata = CandidateMetadata("setup-v1", "setup-plan"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = score,
        withinFamilyWeightFixedPoint = 100,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = emptyList(),
        explanationCodes = emptyList(),
    )
}
