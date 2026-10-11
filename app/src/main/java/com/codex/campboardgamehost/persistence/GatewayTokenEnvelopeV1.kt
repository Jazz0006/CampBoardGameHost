package com.codex.campboardgamehost

/**
 * Device-local encrypted token envelope; the ciphertext is never a reusable key.
 * Pure parser/encoder so the wire format and size boundaries are unit-testable
 * without mocking Android Keystore. Actual AES/GCM belongs to the Android owner.
 */
internal object GatewayTokenEnvelopeV1 {
    private const val VERSION: Byte = 1
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 16
    private const val MAX_TOKEN_BYTES = 4096
    private const val HEADER_LENGTH = 2

    fun encode(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        require(iv.size == IV_LENGTH)
        require(ciphertext.size in TAG_LENGTH..(MAX_TOKEN_BYTES + TAG_LENGTH))
        return byteArrayOf(VERSION, IV_LENGTH.toByte()) + iv + ciphertext
    }

    fun decode(envelope: ByteArray): Pair<ByteArray, ByteArray> {
        require(envelope.size in (HEADER_LENGTH + IV_LENGTH + TAG_LENGTH)..
            (HEADER_LENGTH + IV_LENGTH + MAX_TOKEN_BYTES + TAG_LENGTH))
        require(envelope[0] == VERSION && envelope[1] == IV_LENGTH.toByte())
        val iv = envelope.copyOfRange(HEADER_LENGTH, HEADER_LENGTH + IV_LENGTH)
        val ciphertext = envelope.copyOfRange(HEADER_LENGTH + IV_LENGTH, envelope.size)
        return iv to ciphertext
    }
}
