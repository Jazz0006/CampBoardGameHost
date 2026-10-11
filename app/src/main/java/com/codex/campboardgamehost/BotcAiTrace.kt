package com.codex.campboardgamehost

import android.util.Log

/**
 * ADB/Android Studio lifecycle trace for the personal experimental client.
 * ONLY enum-like stages, fixed Host rejection codes, durations and revision counters.
 * Never pass prompts, grimoire state, player names, access tokens or provider bodies.
 *
 * View with: adb logcat -v time -s BotcAiTrace:I
 */
internal object BotcAiTrace {
    private const val TAG = "BotcAiTrace"
    private val safeStages = setOf(
        "BUILDING_HOST_CONTEXT", "PREPARING", "CONNECTING", "AWAITING_MODEL",
        "HTTP_RESPONSE", "PARSING_RESPONSE", "VALIDATING_HOST_RESPONSE",
    )
    private val safeReasons = setOf(
        "GAME_CHANGED", "MANUAL_TAKEOVER", "GAME_KIND_CHANGED", "PHASE_CHANGED",
        "ROUND_CHANGED", "SCREEN_CHANGED", "GAME_REVISION_CHANGED",
        "PLAYER_REVISION_CHANGED", "SNAPSHOT_CHANGED", "NEWER_REQUEST",
        "MISSING_SESSION", "MISSING_CREDENTIALS",
    )
    private val safeCategories = setOf(
        "TLS certificate", "DNS", "timeout", "network connection",
        "JSON response shape", "incomplete model response", "missing structured output",
        "strategy contract validation", "response or configuration validation",
    )
    private fun write(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    fun started(serial: Int, direct: Boolean, gameRevision: Long, inputRevision: Long) {
        write("postcommit START seq=$serial transport=${if (direct) "DIRECT" else "GATEWAY"} " +
            "sentGameRev=$gameRevision sentInputRev=$inputRevision")
    }

    fun stage(serial: Int, stage: String) {
        write("postcommit STAGE seq=$serial stage=${stage.takeIf { it in safeStages } ?: "OTHER"}")
    }

    fun dropped(
        serial: Int,
        reason: String,
        elapsedMs: Long,
        sentGameRev: Long,
        currentGameRev: Long?,
        sentInputRev: Long,
        currentInputRev: Long?,
    ) {
        write(
            "postcommit DISCARDED seq=$serial " +
                "reason=${reason.takeIf { it in safeReasons } ?: "OTHER"} " +
                "elapsedMs=$elapsedMs sentGameRev=$sentGameRev " +
                "currentGameRev=${currentGameRev ?: -1L} sentInputRev=$sentInputRev " +
                "currentInputRev=${currentInputRev ?: -1L}",
        )
    }

    fun finished(serial: Int, elapsedMs: Long) {
        write("postcommit ACCEPTED seq=$serial elapsedMs=$elapsedMs")
    }

    fun failed(serial: Int, elapsedMs: Long, category: String) {
        // Never print an upstream exception message. safeFailure categories are bounded;
        // 3-digit HTTP codes are the only dynamic class admitted.
        val sanitized = category.takeIf { it in safeCategories ||
            Regex("HTTP [1-5][0-9]{2}").matches(it)
        } ?: "other"
        write("postcommit FAILED seq=$serial elapsedMs=$elapsedMs category=$sanitized")
    }

    fun blocked(reason: String) {
        write("postcommit BLOCKED reason=${reason.takeIf { it in safeReasons } ?: "OTHER"}")
    }
}
