package com.codex.campboardgamehost
import org.junit.Assert.*
import org.junit.Test
class ClocktowerChambermaidPresentationSemanticsTest {
    @Test fun `query presentation preserves actual ordered targets and partial selection`() {
        val cards = listOf(PlayerCard("Maid"), PlayerCard("Alice"), PlayerCard("Bob"))
        val complete = clocktowerChambermaidSelectionPresentation(cards, RevalidatedTwoPlayerSelection("Bob", "Alice"))
        assertEquals(listOf("Bob", "Alice"), complete.selectedTargetNames)
        assertEquals(listOf(3, 2), complete.subjectSeats)
        assertEquals("3   2", complete.displaySecondary)
        val partial = clocktowerChambermaidSelectionPresentation(cards, RevalidatedTwoPlayerSelection("Alice", null))
        assertEquals(listOf(2), partial.subjectSeats)
        assertEquals("2", partial.displaySecondary)
        assertNull(clocktowerChambermaidSelectionPresentation(cards, RevalidatedTwoPlayerSelection(null, null)).displaySecondary)
    }
}
