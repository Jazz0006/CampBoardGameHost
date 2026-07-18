package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnreliableNumberInformationRecommenderTest {
    @Test
    fun `all legal values remain available including the truth`() {
        val recommendations = UnreliableNumberInformationRecommender.recommend(
            UnreliableNumberContext(trueValue = 1, minimumValue = 0, maximumValue = 2),
        )

        assertEquals(setOf(0, 1, 2), recommendations.map { it.value }.toSet())
        assertTrue(recommendations.any { it.value == 1 })
    }

    @Test
    fun `gentle keeps truth while balanced creates limited misinformation`() {
        val recommendations = UnreliableNumberInformationRecommender.recommend(
            UnreliableNumberContext(trueValue = 0, minimumValue = 0, maximumValue = 2, pressureCostPerPoint = 2),
        ).associateBy { it.style }

        assertEquals(0, recommendations.getValue(RecommendationStyle.GENTLE).value)
        assertEquals(1, recommendations.getValue(RecommendationStyle.BALANCED).value)
        assertEquals(2, recommendations.getValue(RecommendationStyle.AGGRESSIVE).value)
    }

    @Test
    fun `history continuity influences gentle recommendation`() {
        val recommendation = UnreliableNumberInformationRecommender.recommend(
            UnreliableNumberContext(
                trueValue = 1,
                minimumValue = 0,
                maximumValue = 2,
                previousShownValue = 1,
            ),
        ).first { it.style == RecommendationStyle.GENTLE }

        assertEquals(1, recommendation.value)
        assertTrue(recommendation.scoreItems.any { it.ruleId == "history-continuity" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid true value is rejected`() {
        UnreliableNumberContext(trueValue = 3, minimumValue = 0, maximumValue = 2)
    }
}
