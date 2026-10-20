package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerChambermaidAiChoiceBindingTest {
    private val suggestion = ClocktowerDisplayOption(
        label = "1",
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chambermaid",
        displayPrimary = "1",
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
            sourceSeat = 2,
            subjectSeats = listOf(3, 5),
            value = 1,
        ),
    )

    @Test fun `same live query retains approved recommendation regardless of seat selection order`() {
        assertEquals(suggestion, clocktowerCurrentChambermaidRecommendation(suggestion, 2, listOf(3, 5)))
        assertEquals(suggestion, clocktowerCurrentChambermaidRecommendation(suggestion, 2, listOf(5, 3)))
    }

    @Test fun `different real player query never uses stale AI result`() {
        assertNull(clocktowerCurrentChambermaidRecommendation(suggestion, 2, listOf(3, 6)))
        assertNull(clocktowerCurrentChambermaidRecommendation(suggestion, 1, listOf(3, 5)))
        assertNull(clocktowerCurrentChambermaidRecommendation(suggestion, 2, listOf(3)))
        assertNull(clocktowerCurrentChambermaidRecommendation(suggestion, 2, listOf(3, 3)))
        assertNull(clocktowerCurrentChambermaidRecommendation(null, 2, listOf(3, 5)))
    }
}
