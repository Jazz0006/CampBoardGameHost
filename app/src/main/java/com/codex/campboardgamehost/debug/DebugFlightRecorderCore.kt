package com.codex.campboardgamehost.debug

import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayDeque

data class DebugEvent(
    val timestampMillis: Long,
    val event: String,
    val fields: Map<String, String> = emptyMap(),
)

data class DebugCrashSnapshot(
    val capturedAtMillis: Long,
    val threadName: String,
    val throwable: Throwable,
    val state: Map<String, String> = emptyMap(),
)

class DebugEventBuffer(
    private val maxEvents: Int,
) {
    init {
        require(maxEvents > 0) { "maxEvents must be positive" }
    }

    private val events = ArrayDeque<DebugEvent>(maxEvents)

    @Synchronized
    fun record(event: DebugEvent) {
        if (events.size == maxEvents) {
            events.removeFirst()
        }
        events.addLast(event)
    }

    @Synchronized
    fun snapshot(): List<DebugEvent> = events.toList()
}

object DebugFlightRecorderCodec {
    fun encodeEvents(events: List<DebugEvent>): String = buildString {
        events.forEach { event ->
            append(
                JSONObject()
                    .put("timestampMillis", event.timestampMillis)
                    .put("event", event.event)
                    .put("fields", JSONObject(event.fields))
                    .toString(),
            )
            append('\n')
        }
    }

    fun decodeEvents(jsonLines: String): List<DebugEvent> = jsonLines
        .lineSequence()
        .filter { it.isNotBlank() }
        .map { line ->
            val json = JSONObject(line)
            val fieldsJson = json.optJSONObject("fields") ?: JSONObject()
            val fields = buildMap {
                fieldsJson.keys().forEach { key ->
                    put(key, fieldsJson.optString(key, ""))
                }
            }
            DebugEvent(
                timestampMillis = json.getLong("timestampMillis"),
                event = json.getString("event"),
                fields = fields,
            )
        }
        .toList()

    fun encodeState(state: Map<String, String>): String =
        JSONObject(state).toString(2)

    fun encodeCrash(snapshot: DebugCrashSnapshot): String {
        val stackTrace = StringWriter().also { writer ->
            snapshot.throwable.printStackTrace(PrintWriter(writer))
        }.toString()
        return buildString {
            appendLine("capturedAtMillis=${snapshot.capturedAtMillis}")
            appendLine("thread=${snapshot.threadName}")
            appendLine("throwable=${snapshot.throwable::class.java.name}")
            appendLine("message=${snapshot.throwable.message.orEmpty()}")
            appendLine()
            append(stackTrace)
        }
    }
}
