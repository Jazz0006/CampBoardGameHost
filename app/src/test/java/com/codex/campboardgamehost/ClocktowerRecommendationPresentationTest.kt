package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerRecommendationPresentationTest {
    @Test
    fun `the advisory surface exposes only one candidate even from a larger ranked set`() {
        val top = displayOption("top")
        val second = displayOption("second")
        val third = displayOption("third")
        val fourth = displayOption("fourth")

        val presentation = clocktowerRecommendationPresentation(
            rankedCandidates = listOf(top, second, third, fourth),
        )

        assertSame(top, presentation.primary)
        assertEquals(listOf(top), presentation.recommendations)
    }

    @Test
    fun `short recommendation lists are not padded with synthetic recommendations`() {
        val only = displayOption("only")
        val second = displayOption("second")

        val empty = clocktowerRecommendationPresentation<ClocktowerDisplayOption>(emptyList())
        val single = clocktowerRecommendationPresentation(listOf(only))
        val pair = clocktowerRecommendationPresentation(listOf(only, second))

        assertNull(empty.primary)
        assertEquals(emptyList<ClocktowerDisplayOption>(), empty.recommendations)

        assertSame(only, single.primary)
        assertEquals(listOf(only), single.recommendations)

        assertSame(only, pair.primary)
        assertEquals(listOf(only), pair.recommendations)
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
