package com.codex.campboardgamehost

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GatewayTokenEnvelopeV1Test {
    @Test
    fun roundTripKeepsIVAndCiphertextSeparateAndNeverAddsPlaintext() {
        val iv = ByteArray(12) { it.toByte() }
        val encrypted = ByteArray(48) { (it * 3).toByte() }
        val encoded = GatewayTokenEnvelopeV1.encode(iv, encrypted)
        assertEquals(2 + iv.size + encrypted.size, encoded.size)
        val (restoredIv, restoredEncrypted) = GatewayTokenEnvelopeV1.decode(encoded)
        assertArrayEquals(iv, restoredIv)
        assertArrayEquals(encrypted, restoredEncrypted)
    }

    @Test
    fun unknownFormatAndIncorrectSizesFailClosed() {
        val valid = GatewayTokenEnvelopeV1.encode(ByteArray(12), ByteArray(16))
        for (invalid in listOf(
            byteArrayOf(),
            valid.copyOf().also { it[0] = 2 },
            valid.copyOf().also { it[1] = 11 },
            valid.dropLast(1).toByteArray(),
            ByteArray(2 + 12 + 4096 + 17),
        )) {
            assertThrows(IllegalArgumentException::class.java) {
                GatewayTokenEnvelopeV1.decode(invalid)
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            GatewayTokenEnvelopeV1.encode(ByteArray(11), ByteArray(16))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GatewayTokenEnvelopeV1.encode(ByteArray(12), ByteArray(15))
        }
    }
}
