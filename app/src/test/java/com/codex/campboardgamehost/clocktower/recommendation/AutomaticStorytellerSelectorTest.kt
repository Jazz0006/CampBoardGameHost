package com.codex.campboardgamehost.clocktower.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticStorytellerSelectorTest {
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
