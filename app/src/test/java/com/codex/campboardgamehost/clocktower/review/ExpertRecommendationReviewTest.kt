package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpertRecommendationReviewTest {
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `expert scenarios return nonempty legal recommendations`() {
        ExpertReviewFixtures.scenarios.forEach { scenario ->
            val plans = SetupRecommendationService.recommend(scenario.game, roleDefinitions)
            assertTrue(scenario.id, plans.isNotEmpty())
            plans.forEach { plan ->
                assertTrue(scenario.id, PlanLegalityValidator.validate(
                    scenario.game, roleDefinitions, CandidatePlan(plan.decisions),
                ).isEmpty())
            }
        }
    }

    @Test
    fun `review fixtures are deterministic and independent of player names`() {
        ExpertReviewFixtures.scenarios.take(4).forEach { scenario ->
            val first = SetupRecommendationService.recommend(scenario.game, roleDefinitions)
            val renamedGame = scenario.game.copy(
                players = scenario.game.players.map { it.copy(name = "Renamed ${it.seat}") },
            )
            val second = SetupRecommendationService.recommend(renamedGame, roleDefinitions)

            assertEquals(first.map { it.decisions }, second.map { it.decisions })
        }
    }

}
