package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImpairedInformationSemanticsContractTest {
    @Test
    fun `poisoned information strongly prefers false regardless of game balance`() {
        val masses = RecommendationStyle.entries.flatMap { style ->
            listOf(-100, 0, 100).map { evilAdvantage ->
                DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = InformationReliability.POISONED,
                    style = style,
                    evilAdvantage = evilAdvantage,
                )
            }
        }

        assertTrue(
            "Poisoned information should keep false information in the intended near-certain policy band.",
            masses.all { it in 950_000L..990_000L },
        )
    }

    @Test
    fun `drunk information strongly prefers false regardless of game balance`() {
        val masses = RecommendationStyle.entries.flatMap { style ->
            listOf(-100, 0, 100).map { evilAdvantage ->
                DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = InformationReliability.DRUNK,
                    style = style,
                    evilAdvantage = evilAdvantage,
                )
            }
        }

        assertTrue(
            "Drunk information should keep false information in the intended near-certain policy band.",
            masses.all { it in 950_000L..990_000L },
        )
    }

    @Test
    fun `game balance cannot own the impaired truthful versus false boundary`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(InformationReliability.DRUNK, InformationReliability.POISONED).forEach { reliability ->
                val goodAhead = DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = reliability,
                    style = style,
                    evilAdvantage = -100,
                )
                val evilAhead = DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = reliability,
                    style = style,
                    evilAdvantage = 100,
                )

                assertEquals(
                    "Balance may rank legal false candidates, but must not flip the reliability family budget.",
                    goodAhead,
                    evilAhead,
                )
            }
        }
    }

    @Test
    fun `healthy information never receives malfunction false probability`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(-100, 0, 100).forEach { evilAdvantage ->
                assertEquals(
                    0L,
                    DynamicCandidateGenerator.misinformationMassFixedPoint(
                        reliability = InformationReliability.RELIABLE,
                        style = style,
                        evilAdvantage = evilAdvantage,
                    ),
                )
            }
        }
    }
}
