package com.codex.campboardgamehost.debug

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugFlightRecorderCoreTest {
    @Test
    fun rollingBuffer_keepsOnlyMostRecentEventsInOrder() {
        val buffer = DebugEventBuffer(maxEvents = 3)

        buffer.record(DebugEvent(1L, "one", mapOf("seat" to "1")))
        buffer.record(DebugEvent(2L, "two", mapOf("seat" to "2")))
        buffer.record(DebugEvent(3L, "three", mapOf("seat" to "3")))
        buffer.record(DebugEvent(4L, "four", mapOf("seat" to "4")))

        assertEquals(listOf("two", "three", "four"), buffer.snapshot().map { it.event })
    }

    @Test
    fun jsonLines_roundTripsStructuredBreadcrumbs() {
        val events = listOf(
            DebugEvent(
                timestampMillis = 10L,
                event = "CONFIRM_EXECUTION_CLICKED",
                fields = mapOf("round" to "1", "targetSeat" to "5"),
            ),
            DebugEvent(
                timestampMillis = 11L,
                event = "EXECUTION_PREFLIGHT",
                fields = mapOf("eventCounter" to "7", "observationAlreadyExists" to "true"),
            ),
        )

        val encoded = DebugFlightRecorderCodec.encodeEvents(events)
        val decoded = DebugFlightRecorderCodec.decodeEvents(encoded)

        assertEquals(events, decoded)
        assertEquals(2, encoded.lineSequence().count { it.isNotBlank() })
    }

    @Test
    fun crashSnapshot_serializesStateAndStackWithoutPlayerNames() {
        val exception = IllegalStateException("duplicate observation")
        val snapshot = DebugCrashSnapshot(
            capturedAtMillis = 123L,
            threadName = "main",
            throwable = exception,
            state = mapOf(
                "screen" to "ClocktowerJudge",
                "phase" to "DAY",
                "round" to "1",
                "selectedSeat" to "5",
                "eventCounter" to "7",
            ),
        )

        val stateJson = DebugFlightRecorderCodec.encodeState(snapshot.state)
        val decodedState = JSONObject(stateJson)
        val crashText = DebugFlightRecorderCodec.encodeCrash(snapshot)

        assertEquals("5", decodedState.getString("selectedSeat"))
        assertTrue(crashText.contains("IllegalStateException"))
        assertTrue(crashText.contains("duplicate observation"))
        assertFalse(decodedState.has("playerName"))
    }
}
