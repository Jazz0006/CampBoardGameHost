package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class CandidatePoolBuilderTest {
    @Test
    fun `pool keeps only best non-rejected tier inside score tolerance`() {
        val pool = CandidatePoolBuilder.build(
            evaluations = listOf(
                evaluation("recommended-low", QualityTier.RECOMMENDED, 94),
                evaluation("acceptable-high", QualityTier.ACCEPTABLE_WITH_WARNING, 200),
                evaluation("recommended-best", QualityTier.RECOMMENDED, 100),
                evaluation("recommended-near", QualityTier.RECOMMENDED, 96),
                evaluation("rejected", QualityTier.REJECTED, 500),
            ),
            scoreTolerance = 5,
        )

        assertEquals(listOf("recommended-best", "recommended-near"), pool.map { it.candidate.candidateId })
    }

    @Test
    fun `candidate order does not affect canonical pool order`() {
        val evaluations = listOf(
            evaluation("candidate-c", QualityTier.RECOMMENDED, 10),
            evaluation("candidate-a", QualityTier.RECOMMENDED, 10),
            evaluation("candidate-b", QualityTier.RECOMMENDED, 10),
        )

        val forward = CandidatePoolBuilder.build(evaluations, scoreTolerance = 0)
        val reversed = CandidatePoolBuilder.build(evaluations.reversed(), scoreTolerance = 0)

        assertEquals(listOf("candidate-a", "candidate-b", "candidate-c"), forward.map { it.candidate.candidateId })
        assertEquals(forward, reversed)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate candidate ids fail closed`() {
        CandidatePoolBuilder.build(
            evaluations = listOf(
                evaluation("duplicate", QualityTier.RECOMMENDED, 10),
                evaluation("duplicate", QualityTier.RECOMMENDED, 9),
            ),
            scoreTolerance = 1,
        )
    }

    private fun evaluation(
        id: String,
        tier: QualityTier,
        score: Int,
    ): DecisionEvaluation<String> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = id,
            candidateFamilyId = "family",
            outcome = id,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            metadata = CandidateMetadata(
                candidateSchemaVersion = "1",
                decisionType = "test",
            ),
        ),
        qualityTier = tier,
        totalScore = score,
        withinFamilyWeightFixedPoint = 1_000_000,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = emptyList(),
        explanationCodes = emptyList(),
    )
}
