package com.codex.campboardgamehost
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.*
import org.junit.Test

class ClocktowerChambermaidStepMaterializerTest {
    private val cards = listOf(PlayerCard("Alice"), PlayerCard("Bob"), PlayerCard("Carol"))
    private val builder = ClocktowerInformationStepBuilder(cards, "en", { _, en -> en }, { cards[0] },
        { "missing" }, { _, _ -> AbilityFunctioningState.FUNCTIONING })
    @Test fun `query targets have no precomputed answer and accept actual nonnegative number`() {
        val content = ClocktowerChambermaidStepContent("explanation", "footer", "wake")
        val step = builder.buildChambermaidStep(content,
            ClocktowerChambermaidSelectionPresentation(listOf("Bob", "Carol"), listOf(2, 3)))
        assertNull(step.tellPlayer)
        assertNull(step.displayProposition)
        assertTrue(step.displayOptions.isEmpty())
        assertEquals(listOf(2, 3), step.informationQuestion!!.subjectSeats)
        val shown = clocktowerDirectNumericResult(step, 1, 99, cards)!!
        assertEquals(InformationProposition.NumericResult(NumericMetric.PLAYERS_WAKING_FOR_ABILITY, 1, listOf(2, 3), 99), shown.displayProposition)
        val incomplete = builder.buildChambermaidStep(content, ClocktowerChambermaidSelectionPresentation(listOf("Bob"), listOf(2)))
        assertNull(incomplete.informationQuestion)
        assertNull(clocktowerDirectNumericResult(incomplete, 1, 1, cards))
    }
}
