package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedSelectionPoolDeviceBenchmarkTest {
    @Test
    fun `benchmarks build auto selection and aggregate commit without exposing payload`() {
        val report = UnifiedSelectionPoolDeviceBenchmark.run(
            poolFactory = {
                UnifiedSelectionPool(
                    listOf(
                        candidate("balanced", RecommendationStyle.BALANCED, 10),
                        candidate("aggressive", RecommendationStyle.AGGRESSIVE, 9),
                    ),
                )
            },
            playerCount = 5,
            phase = StorytellerPhase.FIRST_NIGHT,
            style = RecommendationStyle.BALANCED,
            styleOf = { it.second },
            samples = 3,
        )

        assertEquals(2, report.candidateCount)
        assertTrue(report.poolBuild.p50Micros >= 0)
        assertTrue(report.autoSelect.p95Micros >= 0)
        assertTrue(report.telemetryCommit.p95Micros >= 0)
        assertTrue(report.toLogLine("first-night-information").contains("family=first-night-information"))
        assertTrue(!report.toLogLine("first-night-information").contains("balanced"))
    }

    private fun candidate(id: String, style: RecommendationStyle, rank: Long) = UnifiedSelectionCandidate(
        candidateId = id,
        familyId = "first-night-information",
        legality = UnifiedCandidateLegality.LEGAL,
        epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
        qualityTier = QualityTier.RECOMMENDED,
        rankFixedPoint = rank,
        payload = id to style,
    )
}
