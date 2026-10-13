package com.codex.campboardgamehost.clocktower.session

import android.util.Log
import com.codex.campboardgamehost.BuildConfig
import java.util.concurrent.atomic.AtomicInteger

/**
 * Opt-in, DEBUG-only live Storyteller AI wire evidence for Android Studio/ADB.
 *
 * Enable: adb shell setprop log.tag.BotcAiWire VERBOSE
 * View: adb logcat -v time -s BotcAiWire:V BotcAiTrace:I '*:S'
 * Disable: adb shell setprop log.tag.BotcAiWire INFO
 *
 * WARNING: When enabled, prompts/responses can contain player names, hidden
 * roles and strategic observations. Never emit credentials or HTTP headers.
 * This deliberately does not persist data or alter the existing ZIP exporter.
 */
internal object BotcAiWireTrace {
    private const val TAG = "BotcAiWire"
    private val nextId = AtomicInteger(0)

    private fun enabled(): Boolean =
        BuildConfig.DEBUG && Log.isLoggable(TAG, Log.VERBOSE)

    fun begin(hostPayload: String, modelRequest: String): Int {
        if (!enabled()) return 0
        val id = nextId.incrementAndGet()
        payload(id, "HOST_INPUT", hostPayload)
        payload(id, "MODEL_REQUEST", modelRequest)
        event(id, "SEND", "ready")
        return id
    }

    fun payload(id: Int, stage: String, body: String) {
        if (id <= 0 || !enabled()) return
        require(stage in setOf(
            "HOST_INPUT", "MODEL_REQUEST", "RAW_PROVIDER_RESPONSE",
            "PARSED_MODEL_OUTPUT", "HOST_VALIDATION",
        ))
        val safe = BotcAiWireTraceCodec.redactSecrets(body)
        val parts = BotcAiWireTraceCodec.split(safe)
        parts.forEachIndexed { index, chunk ->
            Log.v(TAG, "call=" + id + " stage=" + stage +
                " part=" + (index + 1) + "/" + parts.size + " data=" + chunk)
        }
    }

    fun event(id: Int, stage: String, detail: String) {
        if (id <= 0 || !enabled()) return
        require(stage in setOf("SEND", "HTTP", "DONE", "FAILURE"))
        // Do not permit arbitrary upstream exception strings in diagnostics.
        require(detail.matches(Regex("[a-zA-Z0-9_.:/= -]{1,100}")))
        Log.v(TAG, "call=" + id + " stage=" + stage + " detail=" + detail)
    }

    /** A game-level Host verdict can be correlated by the adjacent call event. */
    fun hostValidation(
        kind: String,
        valid: Boolean,
        decisionCount: Int,
        rejection: String? = null,
    ) {
        if (!enabled()) return
        require(kind in setOf("JOINT_OPENING", "STRATEGIC_CONTINUATION"))
        val reason = rejection?.let {
            " rejection=" + BotcAiWireTraceCodec.redactSecrets(it).take(600)
                .replace("\n", " ").replace("\r", " ")
        }.orEmpty()
        Log.v(TAG, "stage=HOST_VALIDATION kind=" + kind + " accepted=" +
            valid + " decisions=" + decisionCount.coerceIn(0, 256) + reason)
    }

    /** Safe 3E3 Host gate summary; never contains player-visible clues or secret role data. */
    fun residualGate(
        event: String,
        decisionCount: Int,
        memoryVersion: Int,
        phase: String,
        round: Int,
    ) {
        if (!enabled()) return
        require(event in setOf(
            "REQUEST", "MODEL_ACCEPTED", "HOST_REJECTED", "AUTO_PAUSED",
            "PRIVATE_ADOPTED", "MEMORY_ADVANCED", "HOST_PUBLISHED",
            "MODEL_UNAVAILABLE", "SKIPPED", "STALE"))
        require(phase in setOf("FIRST_NIGHT", "DAWN", "DAY", "NIGHT"))
        Log.v(TAG, "stage=RESIDUAL event=$event decisions=" +
            decisionCount.coerceIn(0, 256) + " memoryVersion=" +
            memoryVersion.coerceAtLeast(0) + " phase=$phase round=" +
            round.coerceAtLeast(1))
    }

    /** Host-side reveal lifecycle. No player names or observed clue text. */
    fun revealStage(event: String, phase: String, round: Int) {
        if (!enabled()) return
        require(event in setOf("CLICKED", "PREFLIGHT_OK", "FIRST_NIGHT_RECORDED",
            "OBSERVATION_RECORDED", "HISTORY_RECORDED", "REVEAL_OPENED", "BLOCKED"))
        Log.v(TAG, "stage=PLAYER_REVEAL event=$event phase=" +
            phase.takeIf { it in setOf("FIRST_NIGHT", "DAWN", "DAY", "NIGHT") }
                .orEmpty() + " round=" + round.coerceAtLeast(1))
    }

    /** Debug-only error location; player-facing text never includes the exception. */
    fun revealFailure(error: Exception) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "stage=PLAYER_REVEAL event=FAILED; check Host game history before retry", error)
        }
    }

    fun hostAdoption(drunkSeat: Int?, recommendedCount: Int) {
        if (!enabled()) return
        Log.v(TAG, "stage=HOST_ADOPTED kind=JOINT_OPENING drunkSeat=" +
            (drunkSeat ?: "none") + " recommendationCount=" +
            recommendedCount.coerceIn(0, 256))
    }
}

/** Pure chunking/redaction. No impact on the actual model request or answer. */
internal object BotcAiWireTraceCodec {
    private const val CHUNK_SIZE = 1800
    private val keyLike = Regex("sk-[A-Za-z0-9_-]{10,}")
    private val bearerLike = Regex("(?i)Bearer\\s+[^\\s\\\"']+")

    fun redactSecrets(text: String): String = text
        .replace(keyLike, "[REDACTED_KEY]")
        .replace(bearerLike, "Bearer [REDACTED]")

    fun split(text: String, size: Int = CHUNK_SIZE): List<String> {
        require(size in 8..3000)
        if (text.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        val fragment = StringBuilder()
        var fragmentBytes = 0
        var offset = 0
        while (offset < text.length) {
            val codePoint = Character.codePointAt(text, offset)
            val character = String(Character.toChars(codePoint))
            val bytes = character.toByteArray(Charsets.UTF_8).size
            if (fragmentBytes + bytes > size && fragment.isNotEmpty()) {
                result += fragment.toString()
                fragment.setLength(0)
                fragmentBytes = 0
            }
            fragment.append(character)
            fragmentBytes += bytes
            offset += Character.charCount(codePoint)
        }
        if (fragment.isNotEmpty()) result += fragment.toString()
        return result
    }
}
