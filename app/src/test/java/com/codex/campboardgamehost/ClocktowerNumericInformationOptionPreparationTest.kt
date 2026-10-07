package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
