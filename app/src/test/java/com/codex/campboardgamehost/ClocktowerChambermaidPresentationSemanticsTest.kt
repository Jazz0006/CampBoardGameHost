package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerChambermaidPresentationSemanticsTest {
    private fun card(name: String) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
    )

    @Test
    fun `Chambermaid result binds actor value and exact selected subject seats`() {
        val actor = card("Maid")
        val first = card("Alice")
        val second = card("Bob")
        val cards = listOf(actor, first, second)

        assertEquals(
            InformationProposition.NumericResult(
                metric = NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
                sourceSeat = 1,
                subjectSeats = listOf(2, 3),
                value = 1,
            ),
            clocktowerChambermaidDisplayProposition(
                cards = cards,
                actor = actor,
                firstTargetName = first.name,
                secondTargetName = second.name,
                value = 1,
            ),
        )
    }

    @Test
    fun `selection presentation preserves ordered seats and partial display text`() {
        val actor = card("Maid")
        val first = card("Alice")
        val second = card("Bob")
        val cards = listOf(actor, first, second)

        val complete = clocktowerChambermaidSelectionPresentation(
            cards,
            RevalidatedTwoPlayerSelection(second.name, first.name),
        )
        assertEquals(listOf("Bob", "Alice"), complete.selectedTargetNames)
        assertEquals(listOf(3, 2), complete.subjectSeats)
        assertEquals("3   2", complete.displaySecondary)

        val partial = clocktowerChambermaidSelectionPresentation(
            cards,
            RevalidatedTwoPlayerSelection(first.name, null),
        )
        assertEquals(listOf(2), partial.subjectSeats)
        assertEquals("2", partial.displaySecondary)

        val empty = clocktowerChambermaidSelectionPresentation(
            cards,
            RevalidatedTwoPlayerSelection(null, null),
        )
        assertNull(empty.displaySecondary)
    }

    @Test
    fun `prepared proposition validates source targets and displayed value`() {
        val presentation = ClocktowerChambermaidSelectionPresentation(
            selectedTargetNames = listOf("Alice", "Bob"),
            subjectSeats = listOf(2, 3),
        )
        assertEquals(
            InformationProposition.NumericResult(
                metric = NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
                sourceSeat = 1,
                subjectSeats = listOf(2, 3),
                value = 2,
            ),
            presentation.proposition(sourceSeat = 1, value = 2),
        )
        assertThrows(IllegalArgumentException::class.java) { presentation.proposition(0, 1) }
        assertThrows(IllegalArgumentException::class.java) { presentation.proposition(1, 3) }
        assertThrows(IllegalArgumentException::class.java) {
            presentation.copy(subjectSeats = listOf(2, 2)).proposition(1, 1)
        }
    }
}
