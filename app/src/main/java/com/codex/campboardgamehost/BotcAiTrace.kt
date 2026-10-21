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
        "BUILDING_JOINT_OPENING_CONTEXT", "VALIDATING_COMPLETE_HOST_OPENING",
        "FROZEN_WHOLE_GAME_PREFIX", "VALIDATING_STRATEGIC_CONTINUATION",
    )
    private fun write(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    /** Dedicated minimal first-night one-shot lifecycle in Android Studio Logcat. */
    fun firstNightStage(serial: Int, stage: String) {
        write("oneshot STAGE seq=$serial stage=${stage.takeIf { it in safeStages } ?: "OTHER"}")
    }

    fun firstNightEvent(
        serial: Int,
        event: String,
        elapsedMs: Long = 0L,
        recommendationCount: Int = 0,
    ) {
        val safeEvent = event.takeIf {
            it in setOf("START", "READY", "FAILED", "STALE", "ADOPTED",
                "JOINT_PRECOMMIT_START", "JOINT_PRECOMMIT_READY",
                "JOINT_PRECOMMIT_FAILED")
        } ?: "OTHER"
        write("oneshot $safeEvent seq=$serial elapsedMs=$elapsedMs " +
            "recommendations=${recommendationCount.coerceIn(0, 64)}")
    }

}
