package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDirectNumericResultTest {
    @Test fun `last preview restores actual manual number only for the same actor metric and query`() {
        val step = ClocktowerNightStepUi(
            title = "Chambermaid", actor = PlayerCard("Alice"), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "", explanation = "",
            roleEnName = "Chambermaid",
            informationQuestion = ClocktowerInformationQuestion.Number(
                NumericMetric.PLAYERS_WAKING_FOR_ABILITY, listOf(2, 3),
            ),
        )
        val preview = InformationProposition.NumericResult(
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 1, listOf(2, 3), 2,
        )
        // An unrelated current AI answer does not become the value that was previewed.
        val current = step.copy(displayProposition = preview.copy(value = 0))
        assertEquals(2, clocktowerMatchingNumericResult(current, 1, preview))
        assertEquals(0, clocktowerMatchingNumericResult(current, 1, current.displayProposition))
        assertNull(clocktowerMatchingNumericResult(current, 2, preview))
        assertNull(clocktowerMatchingNumericResult(current, 1, preview.copy(metric = NumericMetric.ADJACENT_EVIL_PAIRS)))
        assertNull(clocktowerMatchingNumericResult(current, 1, preview, listOf(2, 4)))
        assertNull(clocktowerMatchingNumericResult(current, 1, null))
        // Current player-selected subjects may differ from the original prepared question.
        assertEquals(1, clocktowerMatchingNumericResult(
            current, 1, preview.copy(subjectSeats = listOf(2, 4), value = 1), listOf(2, 4),
        ))
    }

    @Test fun `only reliable matching Host number is determined without AI`() {
        val fact = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), 0,
        )
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = PlayerCard("Alice"), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "0", explanation = "",
            roleEnName = "Chef", displayKind = ClocktowerDisplayKind.Number,
            displayProposition = fact,
            informationQuestion = ClocktowerInformationQuestion.Number(fact.metric, fact.subjectSeats),
            informationReliability = ReliabilityState.RELIABLE,
        )
        assertEquals(0, clocktowerDeterminedNumericResult(step, 1))
        assertNull(clocktowerDeterminedNumericResult(step, 2))
        assertNull(clocktowerDeterminedNumericResult(step.copy(informationReliability = ReliabilityState.DRUNK), 1))
        assertNull(clocktowerDeterminedNumericResult(step.copy(informationReliability = ReliabilityState.POISONED), 1))
        assertNull(clocktowerDeterminedNumericResult(step.copy(displayProposition = null), 1))
        assertNull(clocktowerDeterminedNumericResult(step.copy(informationQuestion =
            ClocktowerInformationQuestion.Number(fact.metric, listOf(1, 3))), 1))
    }

    @Test fun `manual number records actual selected query subjects rather than stale step subjects`() {
        val step = ClocktowerNightStepUi(
            title = "Chambermaid", actor = PlayerCard("Alice"), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "", explanation = "",
            roleEnName = "Chambermaid",
            informationQuestion = ClocktowerInformationQuestion.Number(
                NumericMetric.ADJACENT_EVIL_PAIRS, listOf(1, 2),
            ),
        )
        val cards = listOf(PlayerCard("Alice"), PlayerCard("Bob"), PlayerCard("Carol"))
        val result = requireNotNull(clocktowerDirectNumericResult(
            step, 1, 0, cards, subjectSeats = listOf(2, 3),
        ))
        assertEquals(listOf(2, 3),
            (result.displayProposition as InformationProposition.NumericResult).subjectSeats)
        assertNull(clocktowerDirectNumericResult(step, 1, 0, cards, subjectSeats = listOf(2, 2)))
        assertNull(clocktowerDirectNumericResult(step, 1, 0, cards, subjectSeats = listOf(2, 4)))
    }
}
