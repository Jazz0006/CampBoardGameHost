package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerRecommendationPresentationTest {
    @Test
    fun `ranked recommendations expose top one and at most two alternatives without reordering`() {
        val top = displayOption("top")
        val second = displayOption("second")
        val third = displayOption("third")
        val fourth = displayOption("fourth")

        val presentation = clocktowerRecommendationPresentation(
            rankedCandidates = listOf(top, second, third, fourth),
        )

        assertSame(top, presentation.primary)
        assertEquals(listOf(second, third), presentation.alternatives)
    }

    @Test
    fun `short recommendation lists are not padded with synthetic alternatives`() {
        val only = displayOption("only")
        val second = displayOption("second")

        val empty = clocktowerRecommendationPresentation<ClocktowerDisplayOption>(emptyList())
        val single = clocktowerRecommendationPresentation(listOf(only))
        val pair = clocktowerRecommendationPresentation(listOf(only, second))

        assertNull(empty.primary)
        assertEquals(emptyList<ClocktowerDisplayOption>(), empty.alternatives)

        assertSame(only, single.primary)
        assertEquals(emptyList<ClocktowerDisplayOption>(), single.alternatives)

        assertSame(only, pair.primary)
        assertEquals(listOf(second), pair.alternatives)
    }

    private fun displayOption(label: String) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.Plain,
        displayTitle = "$label title",
        displayPrimary = label,
        displaySecondary = null,
        displayFooter = null,
    )
}
