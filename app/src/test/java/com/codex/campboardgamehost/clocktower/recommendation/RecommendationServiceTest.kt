package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationServiceTest {
    @Test
    fun `service returns one usable plan for every recommendation style`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()

        val result = RecommendationService.recommend(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.roleDefinitions(),
        )

        assertEquals(
            setOf(RecommendationStyle.GENTLE, RecommendationStyle.BALANCED, RecommendationStyle.AGGRESSIVE),
            result.map { it.style }.toSet(),
        )
        assertTrue(result.all { it.decisions.isNotEmpty() })
    }
}
