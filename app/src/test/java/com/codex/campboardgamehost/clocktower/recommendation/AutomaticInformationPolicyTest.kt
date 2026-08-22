package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicInformationSelectionTest {
    @Test
    fun `drunk and poisoned information share the same strong false preference`() {
        RecommendationStyle.entries.forEach { style ->
            val drunk = probability(InformationReliability.DRUNK, style)
            val poisoned = probability(InformationReliability.POISONED, style)

            assertEquals("$style", drunk, poisoned, 0.0)
            assertTrue("$style: impaired=$drunk", drunk in 0.95..0.99)
        }
    }

    @Test
    fun `global balance does not move impaired truthful versus false probability`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(InformationReliability.DRUNK, InformationReliability.POISONED).forEach { reliability ->
                val evilAhead = probability(reliability, style, evilAdvantage = 100)
                val balanced = probability(reliability, style, evilAdvantage = 0)
                val goodAhead = probability(reliability, style, evilAdvantage = -100)

                assertEquals("$reliability $style good-v-balanced", balanced, goodAhead, 0.0)
                assertEquals("$reliability $style evil-v-balanced", balanced, evilAhead, 0.0)
            }
        }
    }

    @Test
    fun `history streak and candidate pressure do not own the reliability family budget`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(InformationReliability.DRUNK, InformationReliability.POISONED).forEach { reliability ->
                val baseline = probability(reliability, style)
                val pressured = probability(
                    reliability = reliability,
                    style = style,
                    evilAdvantage = 100,
                    streak = 4,
                    pressure = 5,
                )

                assertEquals("$reliability $style", baseline, pressured, 0.0)
            }
        }
    }

    @Test
    fun `reliable information has no malfunction false probability`() {
        RecommendationStyle.entries.forEach { style ->
            assertEquals(0.0, probability(InformationReliability.RELIABLE, style), 0.0)
        }
    }

    @Test
    fun `selection is reproducible for the same event key`() {
        data class Option(
            val id: String,
            val truthful: Boolean,
            val pressure: Int,
            val style: RecommendationStyle,
        )
        val options = listOf(
            Option("truth", true, 0, RecommendationStyle.GENTLE),
            Option("soft-lie", false, 2, RecommendationStyle.BALANCED),
            Option("hard-lie", false, 4, RecommendationStyle.AGGRESSIVE),
        )
        fun select() = DynamicCandidateGenerator.select(
            options = options,
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.BALANCED,
            evilAdvantage = 0,
            stableKey = "game:night:2:empath:Alice",
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = Option::pressure,
            styleOf = Option::style,
        )

        assertEquals(select(), select())
    }

    @Test
    fun `impaired selection falls back to truth when no legal false candidate exists`() {
        data class Option(val id: String, val truthful: Boolean)
        val onlyTruth = Option("truth", true)

        val selected = DynamicCandidateGenerator.select(
            options = listOf(onlyTruth),
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.AGGRESSIVE,
            evilAdvantage = -100,
            stableKey = "game:night:2:no-false-candidate",
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = { 0 },
            styleOf = { RecommendationStyle.BALANCED },
        )

        assertEquals(onlyTruth, selected)
    }

    @Test
    fun `stable event keys approximate the shared impaired false preference`() {
        data class Option(val truthful: Boolean, val pressure: Int)
        val options = listOf(Option(true, 0), Option(false, 2))
        fun observed(reliability: InformationReliability): Double {
            val misleading = (0 until 5_000).count { index ->
                DynamicCandidateGenerator.select(
                    options = options,
                    reliability = reliability,
                    style = RecommendationStyle.BALANCED,
                    evilAdvantage = if (index % 2 == 0) 100 else -100,
                    stableKey = "game:event:$index",
                    recentMisinformationStreak = index % 5,
                    stableIdOf = { if (it.truthful) "truth" else "lie" },
                    isTruthful = Option::truthful,
                    misinformationPressure = Option::pressure,
                    styleOf = { RecommendationStyle.BALANCED },
                )?.truthful == false
            }
            return misleading / 5_000.0
        }

        val drunk = observed(InformationReliability.DRUNK)
        val poisoned = observed(InformationReliability.POISONED)
        assertTrue("drunk=$drunk", drunk in 0.95..0.99)
        assertTrue("poisoned=$poisoned", poisoned in 0.95..0.99)
    }

    private fun probability(
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int = 0,
        streak: Int = 0,
        pressure: Int = 0,
    ) = DynamicCandidateGenerator.misinformationMassFixedPoint(
        reliability = reliability,
        style = style,
        evilAdvantage = evilAdvantage,
        recentMisinformationStreak = streak,
        minimumMisinformationPressure = pressure,
    ) / 1_000_000.0
}
