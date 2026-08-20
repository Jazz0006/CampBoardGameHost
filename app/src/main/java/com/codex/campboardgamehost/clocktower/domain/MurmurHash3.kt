package com.codex.campboardgamehost.clocktower.domain

internal data class Hash128(
    val low64: Long,
    val high64: Long,
)

internal object MurmurHash3 {
    private val c1 = 0x87c37b91114253d5UL.toLong()
    private const val c2 = 0x4cf5ad432745937fL

    fun x64_128(
        input: ByteArray,
        seed: Int = 0,
    ): Hash128 {
        var h1 = seed.toLong() and 0xffffffffL
        var h2 = h1
        val blockCount = input.size / 16

        repeat(blockCount) { blockIndex ->
            val offset = blockIndex * 16
            var k1 = littleEndianLong(input, offset)
            var k2 = littleEndianLong(input, offset + 8)

            k1 *= c1
            k1 = java.lang.Long.rotateLeft(k1, 31)
            k1 *= c2
            h1 = h1 xor k1
            h1 = java.lang.Long.rotateLeft(h1, 27)
            h1 += h2
            h1 = h1 * 5 + 0x52dce729L

            k2 *= c2
            k2 = java.lang.Long.rotateLeft(k2, 33)
            k2 *= c1
            h2 = h2 xor k2
            h2 = java.lang.Long.rotateLeft(h2, 31)
            h2 += h1
            h2 = h2 * 5 + 0x38495ab5L
        }

        val tailOffset = blockCount * 16
        val tailSize = input.size and 15
        var k1 = 0L
        var k2 = 0L

        if (tailSize >= 15) k2 = k2 xor unsignedByte(input[tailOffset + 14]).shl(48)
        if (tailSize >= 14) k2 = k2 xor unsignedByte(input[tailOffset + 13]).shl(40)
        if (tailSize >= 13) k2 = k2 xor unsignedByte(input[tailOffset + 12]).shl(32)
        if (tailSize >= 12) k2 = k2 xor unsignedByte(input[tailOffset + 11]).shl(24)
        if (tailSize >= 11) k2 = k2 xor unsignedByte(input[tailOffset + 10]).shl(16)
        if (tailSize >= 10) k2 = k2 xor unsignedByte(input[tailOffset + 9]).shl(8)
        if (tailSize >= 9) {
            k2 = k2 xor unsignedByte(input[tailOffset + 8])
            k2 *= c2
            k2 = java.lang.Long.rotateLeft(k2, 33)
            k2 *= c1
            h2 = h2 xor k2
        }

        if (tailSize >= 8) k1 = k1 xor unsignedByte(input[tailOffset + 7]).shl(56)
        if (tailSize >= 7) k1 = k1 xor unsignedByte(input[tailOffset + 6]).shl(48)
        if (tailSize >= 6) k1 = k1 xor unsignedByte(input[tailOffset + 5]).shl(40)
        if (tailSize >= 5) k1 = k1 xor unsignedByte(input[tailOffset + 4]).shl(32)
        if (tailSize >= 4) k1 = k1 xor unsignedByte(input[tailOffset + 3]).shl(24)
        if (tailSize >= 3) k1 = k1 xor unsignedByte(input[tailOffset + 2]).shl(16)
        if (tailSize >= 2) k1 = k1 xor unsignedByte(input[tailOffset + 1]).shl(8)
        if (tailSize >= 1) {
            k1 = k1 xor unsignedByte(input[tailOffset])
            k1 *= c1
            k1 = java.lang.Long.rotateLeft(k1, 31)
            k1 *= c2
            h1 = h1 xor k1
        }

        val length = input.size.toLong()
        h1 = h1 xor length
        h2 = h2 xor length
        h1 += h2
        h2 += h1
        h1 = fmix64(h1)
        h2 = fmix64(h2)
        h1 += h2
        h2 += h1
        return Hash128(low64 = h1, high64 = h2)
    }

    fun low64Utf8(value: String): Long = x64_128(value.toByteArray(Charsets.UTF_8)).low64

    private fun littleEndianLong(input: ByteArray, offset: Int): Long {
        var result = 0L
        repeat(8) { byteIndex ->
            result = result or unsignedByte(input[offset + byteIndex]).shl(byteIndex * 8)
        }
        return result
    }

    private fun unsignedByte(value: Byte): Long = value.toLong() and 0xffL

    private fun fmix64(input: Long): Long {
        var value = input
        value = value xor (value ushr 33)
        value *= 0xff51afd7ed558ccdUL.toLong()
        value = value xor (value ushr 33)
        value *= 0xc4ceb9fe1a85ec53UL.toLong()
        value = value xor (value ushr 33)
        return value
    }
}
