package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberRecommendation
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberScoreItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNumericInformationOptionPreparationTest {
    @Test
    fun `previous unreliable number uses latest matching actor title and localized separator`() {
        val events = listOf(
            event(1, "共情者信息", "共情者信息：1", listOf("Alice")),
            event(2, "Empath information", "Empath information: 2", listOf("Alice")),
            event(3, "Empath information", "Empath information: 0", listOf("Bob")),
            event(4, "Empath information", "Empath information: 7", listOf("Alice"), ClocktowerEventType.Information),
        )

        assertEquals(1, previousClocktowerUnreliableNumber(events, "共情者信息", "Alice"))
        assertEquals(2, previousClocktowerUnreliableNumber(events, "Empath information", "Alice"))
        assertNull(previousClocktowerUnreliableNumber(events, "Chef information", "Alice"))
    }

    @Test
    fun `previous unreliable number preserves digit extraction and malformed fallback`() {
        val events = listOf(
            event(1, "Empath information", "shown value 2 of 3", listOf("Alice")),
            event(2, "Empath information", "no numeric result", listOf("Alice")),
        )

        assertNull(previousClocktowerUnreliableNumber(events, "Empath information", "Alice"))
        assertEquals(2, previousClocktowerUnreliableNumber(events.dropLast(1), "Empath information", "Alice"))
    }

    @Test
    fun `number display projection preserves recommendation metadata and clamps pressure`() {
        val recommendations = listOf(
            recommendation(2, RecommendationStyle.BALANCED, listOf("history-continuity"), listOf("large-history-jump")),
            recommendation(9, RecommendationStyle.AGGRESSIVE, listOf("truth-distance"), emptyList()),
        )

        val options = clocktowerUnreliableNumberDisplayOptions(
            recommendations = recommendations,
            title = "Empath information",
            trueValue = 1,
            secondary = "2   4",
            footer = "Evil living neighbors",
            styleLabel = { style -> "style-${style.name.lowercase()}" },
            highPressureSuffix = " warning",
            propositionForValue = { value ->
                InformationProposition.NumericResult(
                    NumericMetric.LIVING_EVIL_NEIGHBOURS,
                    sourceSeat = 3,
                    subjectSeats = listOf(2, 4),
                    value = value,
                )
            },
        )

        val balanced = options[0]
        assertEquals("style-balanced：2 warning", balanced.label)
        assertEquals(ClocktowerDisplayKind.Number, balanced.displayKind)
        assertEquals("Empath information", balanced.displayTitle)
        assertEquals("2", balanced.displayPrimary)
        assertEquals("2   4", balanced.displaySecondary)
        assertEquals("Evil living neighbors", balanced.displayFooter)
        assertEquals(RecommendationStyle.BALANCED, balanced.recommendationStyle)
        assertFalse(balanced.isTruthful)
        assertEquals(1, balanced.misinformationPressure)
        assertTrue(balanced.isDefaultRecommendation)
        assertEquals(listOf("history-continuity"), balanced.reasonCodes)
        assertEquals(listOf("large-history-jump"), balanced.warningCodes)
        assertEquals(2, (balanced.proposition as InformationProposition.NumericResult).value)

        val aggressive = options[1]
        assertEquals("style-aggressive：9", aggressive.label)
        assertEquals(5, aggressive.misinformationPressure)
        assertFalse(aggressive.isDefaultRecommendation)
    }

    private fun recommendation(
        value: Int,
        style: RecommendationStyle,
        rules: List<String>,
        warnings: List<String>,
    ) = UnreliableNumberRecommendation(
        value = value,
        style = style,
        totalScore = 0,
        scoreItems = rules.map { UnreliableNumberScoreItem(it, 1) },
        warningIds = warnings,
    )

    private fun event(
        sequence: Int,
        title: String,
        detail: String,
        players: List<String>,
        type: ClocktowerEventType = ClocktowerEventType.UnreliableInformation,
    ) = ClocktowerEvent(
        sequence = sequence,
        type = type,
        title = title,
        detail = detail,
        playerNames = players,
        phase = ClocktowerPhase.Night,
        round = 2,
    )
}
