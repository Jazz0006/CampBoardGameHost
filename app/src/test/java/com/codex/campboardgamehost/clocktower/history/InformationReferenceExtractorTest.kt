package com.codex.campboardgamehost.clocktower.history

import org.junit.Assert.assertEquals
import org.junit.Test

class InformationReferenceExtractorTest {
    @Test
    fun `extracts distinct valid seats from secondary information and footer`() {
        assertEquals(
            setOf(2, 7, 8),
            InformationReferenceExtractor.extractSeatNumbers(
                values = listOf("2   7", "查询目标：8号玩家", null, "99号无效"),
                maximumSeat = 12,
            ),
        )
    }

    @Test
    fun `empty game has no references`() {
        assertEquals(emptySet<Int>(), InformationReferenceExtractor.extractSeatNumbers(listOf("1 2"), maximumSeat = 0))
    }
}
