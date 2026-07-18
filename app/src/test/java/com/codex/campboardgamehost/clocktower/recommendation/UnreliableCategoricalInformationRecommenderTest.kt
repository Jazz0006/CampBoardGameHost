package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnreliableCategoricalInformationRecommenderTest {
    @Test
    fun `gentle retains truth while other styles can mislead`() {
        val recommendations = UnreliableCategoricalInformationRecommender.recommend(
            listOf(
                UnreliableCategoricalCandidate("truth", isTruthful = true),
                UnreliableCategoricalCandidate("limited", isTruthful = false, misinformationPressure = 2),
                UnreliableCategoricalCandidate("extreme", isTruthful = false, misinformationPressure = 5),
            ),
        ).associateBy { it.style }

        assertEquals("truth", recommendations.getValue(RecommendationStyle.GENTLE).candidateId)
        assertEquals("limited", recommendations.getValue(RecommendationStyle.BALANCED).candidateId)
        assertEquals("extreme", recommendations.getValue(RecommendationStyle.AGGRESSIVE).candidateId)
        assertTrue(recommendations.getValue(RecommendationStyle.AGGRESSIVE).warningIds.isNotEmpty())
    }

    @Test
    fun `binary information returns two distinct legal outcomes`() {
        val recommendations = UnreliableCategoricalInformationRecommender.recommend(
            listOf(
                UnreliableCategoricalCandidate("yes", isTruthful = true),
                UnreliableCategoricalCandidate("no", isTruthful = false, misinformationPressure = 3),
            ),
        )

        assertEquals(2, recommendations.size)
        assertEquals(setOf("yes", "no"), recommendations.map { it.candidateId }.toSet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty candidates are rejected`() {
        UnreliableCategoricalInformationRecommender.recommend(emptyList())
    }
}
