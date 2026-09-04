package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerSeatNumberLabelTest {
    @Test
    fun `seat numbers localize to Chinese suffix and English hash`() {
        assertEquals("2号", clocktowerSeatNumberLabel(2, "zh"))
        assertEquals("#10", clocktowerSeatNumberLabel(10, "en"))
    }

    @Test
    fun `seat number labels fail closed for invalid physical identity`() {
        assertThrows(IllegalArgumentException::class.java) {
            clocktowerSeatNumberLabel(0, "zh")
        }
    }
}
