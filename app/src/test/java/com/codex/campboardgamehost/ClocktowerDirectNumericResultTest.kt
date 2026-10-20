package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDirectNumericResultTest {
    @Test fun `manual number is actual published value, independent of calculated candidate set`() {
        val baseline = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), 1,
        )
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = PlayerCard("Alice"), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "1", explanation = "",
            informationQuestion = ClocktowerInformationQuestion.Number(baseline.metric, baseline.subjectSeats), roleEnName = "Chef",
            manualInformationCandidates = listOf(
                ClocktowerDisplayOption("1", ClocktowerDisplayKind.Number, "Chef", "1", null, null,
                    proposition = baseline),
            ),
        )
        val cards = listOf(requireNotNull(step.actor), PlayerCard("Bob"), PlayerCard("Carol"))
        val actual = requireNotNull(clocktowerDirectNumericResult(step, 1, 17, cards))
        assertEquals("17", actual.displayPrimary)
        assertEquals(17, (actual.displayProposition as InformationProposition.NumericResult).value)
        assertEquals(baseline.metric,
            (actual.displayProposition as InformationProposition.NumericResult).metric)
        assertNull(actual.selectedInformationTruthful)
        assertNull(clocktowerDirectNumericResult(step, 3, 17, cards))
        assertNull(clocktowerDirectNumericResult(step, 1, -1, cards))
        assertNull(clocktowerDirectNumericResult(step.copy(informationQuestion =
            ClocktowerInformationQuestion.Number(baseline.metric, listOf(99))), 1, 17, cards))
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
