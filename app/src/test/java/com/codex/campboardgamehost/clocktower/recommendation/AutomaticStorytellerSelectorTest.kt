package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeightedStableSelectorFallbackTest {
    @Test
    fun `selectStyle applies the configured automatic style`() {
        val options = RecommendationStyle.entries.toList()

        assertEquals(
            RecommendationStyle.AGGRESSIVE,
            WeightedStableSelector.selectStyle(
                options,
                RecommendationStyle.AGGRESSIVE,
            ) { it },
        )
    }

    @Test
    fun `select uses balanced option even when it is not first`() {
        val options = listOf("gentle", "balanced", "aggressive")

        assertEquals(
            "balanced",
            WeightedStableSelector.selectPreferred(options) { it == "balanced" },
        )
    }

    @Test
    fun `select falls back to first legal option when balanced was deduplicated`() {
        val options = listOf("gentle", "aggressive")

        assertEquals(
            "gentle",
            WeightedStableSelector.selectPreferred(options) { it == "balanced" },
        )
    }

    @Test
    fun `select returns null when no legal information exists`() {
        assertNull(
            WeightedStableSelector.selectPreferred(emptyList<String>()) { it == "balanced" },
        )
    }
}
