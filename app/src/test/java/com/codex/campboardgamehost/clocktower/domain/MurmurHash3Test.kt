package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MurmurHash3Test {
    @Test
    fun `x64 128 matches published reference vectors`() {
        assertHash("", "0000000000000000", "0000000000000000")
        assertHash("foo", "e271865701f54561", "7eaf87e42bba7d87")
        assertHash("hello", "cbd8a7b341bd9b02", "5b1e906a48ae1d19")
    }

    private fun assertHash(input: String, expectedLow: String, expectedHigh: String) {
        val hash = MurmurHash3.x64_128(input.toByteArray(Charsets.UTF_8))
        assertEquals(expectedLow, unsignedHex(hash.low64))
        assertEquals(expectedHigh, unsignedHex(hash.high64))
    }

    private fun unsignedHex(value: Long): String = java.lang.Long
        .toUnsignedString(value, 16)
        .padStart(16, '0')

}
