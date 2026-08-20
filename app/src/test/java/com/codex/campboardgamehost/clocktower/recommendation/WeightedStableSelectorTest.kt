package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightedStableSelectorTest {
    private val budget = FamilyProbabilityBudget(
        massByFamily = mapOf(
            "natural-truth" to 700_000,
            "registered-truth" to 300_000,
        ),
    )

    @Test
    fun `candidate input order cannot change probabilities or selection`() {
        val pool = listOf(
            evaluation("natural-b", "natural-truth", 1),
            evaluation("registered-a", "registered-truth", 1),
            evaluation("natural-a", "natural-truth", 3),
        )

        val forward = WeightedStableSelector.select(pool, budget, decisionSeed = Long.MIN_VALUE)!!
        val reversed = WeightedStableSelector.select(pool.reversed(), budget, decisionSeed = Long.MIN_VALUE)!!

        assertEquals(forward.finalProbabilityByCandidate, reversed.finalProbabilityByCandidate)
        assertEquals(forward.selected.candidate.candidateId, reversed.selected.candidate.candidateId)
    }

    @Test
    fun `adding candidates redistributes only within their family`() {
        val original = WeightedStableSelector.select(
            pool = listOf(
                evaluation("natural-a", "natural-truth", 1),
                evaluation("registered-a", "registered-truth", 1),
            ),
            familyBudget = budget,
            decisionSeed = 0,
        )!!
        val expanded = WeightedStableSelector.select(
            pool = listOf(
                evaluation("natural-a", "natural-truth", 1),
                evaluation("natural-b", "natural-truth", 1),
                evaluation("registered-a", "registered-truth", 1),
            ),
            familyBudget = budget,
            decisionSeed = 0,
        )!!

        assertEquals(700_000, original.familyTotal("natural"))
        assertEquals(700_000, expanded.familyTotal("natural"))
        assertEquals(300_000, original.finalProbabilityByCandidate.getValue("registered-a"))
        assertEquals(300_000, expanded.finalProbabilityByCandidate.getValue("registered-a"))
    }

    @Test
    fun `inactive family mass is normalized across active families`() {
        val selection = WeightedStableSelector.select(
            pool = listOf(evaluation("registered-a", "registered-truth", 1)),
            familyBudget = budget,
            decisionSeed = -1,
        )!!

        assertEquals(WeightedStableSelector.FIXED_POINT_SCALE, selection.finalProbabilityByCandidate.getValue("registered-a"))
        assertEquals("registered-a", selection.selected.candidate.candidateId)
    }

    private fun WeightedSelection<String>.familyTotal(prefix: String): Long =
        finalProbabilityByCandidate.filterKeys { it.startsWith(prefix) }.values.sum()

    private fun evaluation(
        id: String,
        familyId: String,
        weight: Long,
    ): DecisionEvaluation<String> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = id,
            candidateFamilyId = familyId,
            outcome = id,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
            metadata = CandidateMetadata("1", "test"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 10,
        withinFamilyWeightFixedPoint = weight,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = emptyList(),
        explanationCodes = emptyList(),
    )
}
