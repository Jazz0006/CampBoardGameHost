package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationRecommenderTest {
    @Test
    fun `styles select distinct complete information packages`() {
        val recommendations = PairInformationRecommender.recommend(
            listOf(
                candidate("safe", PairInformationRegistration.NONE, exposure = 1, discussion = 1, pressure = 0),
                candidate("balanced", PairInformationRegistration.NONE, exposure = 2, discussion = 5, pressure = 2),
                candidate("spy", PairInformationRegistration.SPY_AS_GOOD_ROLE, exposure = 3, discussion = 4, pressure = 5),
            ),
        ).associateBy { it.style }

        assertEquals("safe", recommendations.getValue(RecommendationStyle.GENTLE).candidateId)
        assertEquals("balanced", recommendations.getValue(RecommendationStyle.BALANCED).candidateId)
        assertEquals("spy", recommendations.getValue(RecommendationStyle.AGGRESSIVE).candidateId)
        assertTrue(recommendations.getValue(RecommendationStyle.AGGRESSIVE).warningIds.contains("special-registration"))
    }

    @Test
    fun `empty candidate set has no recommendation`() {
        assertTrue(PairInformationRecommender.recommend(emptyList()).isEmpty())
    }

    @Test
    fun `balanced misinformation avoids repeatedly pressured players`() {
        val recommendations = PairInformationRecommender.recommend(
            listOf(
                PairInformationCandidate(
                    id = "repeated",
                    registration = PairInformationRegistration.NONE,
                    isTruthful = false,
                    targetExposure = 2,
                    decoyExposure = 2,
                    discussionValue = 5,
                    misinformationPressure = 2,
                    historyPressure = 4,
                ),
                PairInformationCandidate(
                    id = "fresh",
                    registration = PairInformationRegistration.NONE,
                    isTruthful = false,
                    targetExposure = 2,
                    decoyExposure = 2,
                    discussionValue = 4,
                    misinformationPressure = 2,
                    historyPressure = 0,
                ),
                candidate("truth", PairInformationRegistration.NONE, exposure = 1, discussion = 1, pressure = 0),
            ),
        ).associateBy { it.style }

        assertEquals("fresh", recommendations.getValue(RecommendationStyle.BALANCED).candidateId)
    }

    private fun candidate(
        id: String,
        registration: PairInformationRegistration,
        exposure: Int,
        discussion: Int,
        pressure: Int,
    ) = PairInformationCandidate(
        id = id,
        registration = registration,
        targetExposure = exposure,
        decoyExposure = exposure,
        discussionValue = discussion,
        misinformationPressure = pressure,
    )
}
