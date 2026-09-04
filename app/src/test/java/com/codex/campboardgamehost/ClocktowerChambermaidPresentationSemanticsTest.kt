package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
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
}
