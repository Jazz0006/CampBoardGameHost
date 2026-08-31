package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TroubleBrewingSetupRecommendationRevealCoordinatorTest {
    @Test
    fun `committed deal enters reveal before setup recommendation prewarm is dispatched`() {
        val request = setupRequest()
        val expected = SetupRecommendationService.ConstrainedResult(plans = emptyList())
        val events = mutableListOf<String>()
        var queuedWork: (() -> Unit)? = null
        var buildCount = 0
        val prewarmer = TroubleBrewingSetupRecommendationPrewarmCoordinator {
            events += "build"
            buildCount += 1
            expected
        }
        val coordinator = TroubleBrewingSetupRecommendationRevealCoordinator(prewarmer)

        coordinator.onCommittedDeal(
            request = request,
            enterReveal = { events += "reveal" },
            launchBackground = { work ->
                events += "dispatch"
                queuedWork = work
            },
        )

        assertEquals(listOf("reveal", "dispatch"), events)
        assertEquals(0, buildCount)

        requireNotNull(queuedWork).invoke()

        assertEquals(listOf("reveal", "dispatch", "build"), events)
        assertSame(expected, prewarmer.readyFor(request))
        assertEquals(1, buildCount)
    }

    @Test
    fun `first consumer reuses only exact ready request and safely falls back on mismatch`() {
        val committed = setupRequest()
        val stale = committed.copy(
            game = committed.game.copy(seed = committed.game.seed + 1L),
        )
        var buildCount = 0
        val prewarmer = TroubleBrewingSetupRecommendationPrewarmCoordinator { request ->
            buildCount += 1
            SetupRecommendationService.ConstrainedResult(
                plans = emptyList(),
                failureCodes = listOf("seed-${request.game.seed}"),
            )
        }
        val coordinator = TroubleBrewingSetupRecommendationRevealCoordinator(prewarmer)

        val committedReady = prewarmer.prewarm(committed)

        assertSame(committedReady, coordinator.resultFor(committed))
        assertEquals(1, buildCount)

        val staleFallback = coordinator.resultFor(stale)

        assertEquals(listOf("seed-${stale.game.seed}"), staleFallback.failureCodes)
        assertSame(staleFallback, prewarmer.readyFor(stale))
        assertEquals(2, buildCount)
    }

    private fun setupRequest() = SetupCoordinationRequest(
        game = TroubleBrewingFixtures.eightPlayerExample(),
        roles = TroubleBrewingFixtures.fullRoleDefinitions(),
    )
}
