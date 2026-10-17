package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BotcAiWireTraceCodecTest {
    @Test fun chunksReconstructFullDebugPayloadWithoutTruncatingJson() {
        val modelData = "{\"input\":\"" + "中文whole-game".repeat(2000) + "\"}"
        val chunks = BotcAiWireTraceCodec.split(modelData)
        assertTrue(chunks.size > 2)
        assertTrue(chunks.all { it.toByteArray(Charsets.UTF_8).size <= 1800 })
        assertEquals(modelData, chunks.joinToString(""))
        assertEquals(listOf(""), BotcAiWireTraceCodec.split(""))
        val unicode = "\uD83D\uDE00".repeat(100) + "中".repeat(300)
        val segments = BotcAiWireTraceCodec.split(unicode, 12)
        assertTrue(segments.all { it.toByteArray(Charsets.UTF_8).size <= 12 })
        assertEquals(unicode, segments.joinToString(""))
    }

    @Test fun removesCredentialPatternsBeforeAnyLogcatChunk() {
        val raw = """{"input":"testing","key":"sk-proj-AbCdEfG12345","auth":"Bearer demo-secret-token","game":"隐士"}"""
        val sanitized = BotcAiWireTraceCodec.redactSecrets(raw)
        assertFalse(sanitized.contains("sk-proj-AbCdEfG12345"))
        assertFalse(sanitized.contains("demo-secret-token"))
        assertTrue(sanitized.contains("[REDACTED_KEY]"))
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
        assertTrue(sanitized.contains("隐士"))
        assertTrue(runCatching { BotcAiWireTraceCodec.split("abc", 2) }.isFailure)
    }
}
