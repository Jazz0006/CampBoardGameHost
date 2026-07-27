package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticStorytellerSelectorTest {
    @Test
    fun `selectStyle applies the configured automatic style`() {
        val options = RecommendationStyle.entries.toList()

        assertEquals(
            RecommendationStyle.AGGRESSIVE,
            AutomaticStorytellerSelector.selectStyle(
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
            AutomaticStorytellerSelector.select(options) { it == "balanced" },
        )
    }

    @Test
    fun `select falls back to first legal option when balanced was deduplicated`() {
        val options = listOf("gentle", "aggressive")

        assertEquals(
            "gentle",
            AutomaticStorytellerSelector.select(options) { it == "balanced" },
        )
    }

    @Test
    fun `select returns null when no legal information exists`() {
        assertNull(
            AutomaticStorytellerSelector.select(emptyList<String>()) { it == "balanced" },
        )
    }
}
