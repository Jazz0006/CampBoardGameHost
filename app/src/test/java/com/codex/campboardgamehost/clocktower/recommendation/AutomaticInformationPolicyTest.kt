package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticInformationPolicyTest {
    @Test
    fun `poisoned information is more likely to mislead than drunk information`() {
        RecommendationStyle.entries.forEach { style ->
            val drunk = probability(InformationReliability.DRUNK, style)
            val poisoned = probability(InformationReliability.POISONED, style)

            assertTrue("$style: poison=$poisoned drunk=$drunk", poisoned > drunk)
        }
    }

    @Test
    fun `drunk misinformation remains above half when evil is far ahead`() {
        RecommendationStyle.entries.forEach { style ->
            assertTrue(
                probability(
                    reliability = InformationReliability.DRUNK,
                    style = style,
                    evilAdvantage = 100,
                    streak = 4,
                    pressure = 5,
                ) > 0.5,
            )
        }
    }

    @Test
    fun `global balance moves misinformation toward the trailing team`() {
        val evilAhead = probability(
            InformationReliability.POISONED,
            RecommendationStyle.BALANCED,
            evilAdvantage = 70,
        )
        val goodAhead = probability(
            InformationReliability.POISONED,
            RecommendationStyle.BALANCED,
            evilAdvantage = -70,
        )

        assertTrue(goodAhead > evilAhead)
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
        fun select() = AutomaticInformationPolicy.select(
            options = options,
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.BALANCED,
            evilAdvantage = 0,
            stableKey = "game:night:2:empath:Alice",
            recentMisinformationStreak = 0,
            isTruthful = Option::truthful,
            misinformationPressure = Option::pressure,
            styleOf = Option::style,
        )

        assertEquals(select(), select())
    }

    @Test
    fun `stable event keys approximate configured misinformation rates`() {
        data class Option(val truthful: Boolean, val pressure: Int)
        val options = listOf(Option(true, 0), Option(false, 2))
        fun observed(reliability: InformationReliability): Double {
            val misleading = (0 until 5_000).count { index ->
                AutomaticInformationPolicy.select(
                    options = options,
                    reliability = reliability,
                    style = RecommendationStyle.BALANCED,
                    evilAdvantage = 0,
                    stableKey = "game:event:$index",
                    recentMisinformationStreak = 0,
                    isTruthful = Option::truthful,
                    misinformationPressure = Option::pressure,
                    styleOf = { RecommendationStyle.BALANCED },
                )?.truthful == false
            }
            return misleading / 5_000.0
        }

        val drunk = observed(InformationReliability.DRUNK)
        val poisoned = observed(InformationReliability.POISONED)
        assertTrue("drunk=$drunk", drunk in 0.60..0.70)
        assertTrue("poisoned=$poisoned", poisoned in 0.77..0.87)
        assertTrue(poisoned > drunk)
    }

    private fun probability(
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int = 0,
        streak: Int = 0,
        pressure: Int = 0,
    ) = AutomaticInformationPolicy.misinformationProbability(
        reliability = reliability,
        style = style,
        evilAdvantage = evilAdvantage,
        recentMisinformationStreak = streak,
        minimumMisinformationPressure = pressure,
    )
}
