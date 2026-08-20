package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedSetupSelectorDeviceBenchmarkTest {
    @Test
    fun `benchmarks complete unified setup build select and aggregate commit without exposing plans`() {
        val coordinator = ClocktowerRecommendationCoordinator()
        val plans = coordinator.recommendSetup(
            SetupCoordinationRequest(TroubleBrewingFixtures.eightPlayerExample(), TroubleBrewingFixtures.roleDefinitions()),
        ).plans

        val report = UnifiedSetupSelectorDeviceBenchmark.run(
            coordinator = coordinator,
            plans = plans,
            playerCount = 8,
            style = RecommendationStyle.BALANCED,
            samples = 3,
        )

        assertEquals(3, report.sampleCount)
        assertEquals(plans.size, report.candidateCount)
        assertTrue(report.poolBuild.p50Micros >= 0)
        assertTrue(report.autoSelect.p95Micros >= 0)
        assertTrue(report.telemetryCommit.p95Micros >= 0)
        assertTrue(report.toLogLine().contains("UNIFIED_SETUP_SELECTOR_DEVICE_BENCHMARK"))
        assertTrue(!report.toLogLine().contains("DrunkShownRole"))
    }
}
