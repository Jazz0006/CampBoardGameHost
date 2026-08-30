package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class TroubleBrewingSetupRecommendationPrewarmCoordinatorTest {
    @Test
    fun `same committed setup request is built once and reused`() {
        val request = setupRequest()
        val expected = SetupRecommendationService.ConstrainedResult(plans = emptyList())
        var buildCount = 0
        val coordinator = TroubleBrewingSetupRecommendationPrewarmCoordinator { actualRequest ->
            assertEquals(request, actualRequest)
            buildCount += 1
            expected
        }

        assertSame(expected, coordinator.prewarm(request))
        assertSame(expected, coordinator.prewarm(request))
        assertSame(expected, coordinator.readyFor(request))
        assertEquals(1, buildCount)
    }

    @Test
    fun `changed committed setup request misses ready result and rebuilds`() {
        val first = setupRequest()
        val changed = first.copy(
            game = first.game.copy(seed = first.game.seed + 1L),
        )
        val coordinator = TroubleBrewingSetupRecommendationPrewarmCoordinator { request ->
            SetupRecommendationService.ConstrainedResult(
                plans = emptyList(),
                failureCodes = listOf("seed-${request.game.seed}"),
            )
        }

        val firstResult = coordinator.prewarm(first)

        assertEquals(listOf("seed-${first.game.seed}"), firstResult.failureCodes)
        assertNull(coordinator.readyFor(changed))

        val changedResult = coordinator.prewarm(changed)

        assertEquals(listOf("seed-${changed.game.seed}"), changedResult.failureCodes)
        assertSame(changedResult, coordinator.readyFor(changed))
        assertNull(coordinator.readyFor(first))
    }

    private fun setupRequest() = SetupCoordinationRequest(
        game = TroubleBrewingFixtures.eightPlayerExample(),
        roles = TroubleBrewingFixtures.fullRoleDefinitions(),
    )
}
