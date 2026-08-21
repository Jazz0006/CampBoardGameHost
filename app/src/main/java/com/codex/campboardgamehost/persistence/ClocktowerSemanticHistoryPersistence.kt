package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.json.JSONObject

/** Active-game JSON contract for explicit Clocktower semantic-history metadata. */
internal object ClocktowerSemanticHistoryPersistence {
    const val MODE_KEY = "clocktowerSemanticHistoryMode"
    const val CURSOR_KEY = "clocktowerNextTimelineGlobalSequence"

    fun encode(mode: ClocktowerSemanticHistoryMode): String = mode.name

    fun decodeMode(json: JSONObject): ClocktowerSemanticHistoryMode {
        require(json.has(MODE_KEY)) { "$MODE_KEY is required in active-game schema v3." }
        require(!json.isNull(MODE_KEY)) { "$MODE_KEY cannot be null." }
        val rawMode = json.opt(MODE_KEY)
        require(rawMode is String && rawMode.isNotBlank()) {
            "$MODE_KEY must be a non-blank string."
        }

        require(json.has(CURSOR_KEY)) { "$CURSOR_KEY is required in active-game schema v3." }
        require(!json.isNull(CURSOR_KEY)) { "$CURSOR_KEY cannot be null." }
        val rawCursor = json.opt(CURSOR_KEY)
        require(rawCursor is Byte || rawCursor is Short || rawCursor is Int || rawCursor is Long) {
            "$CURSOR_KEY must be an integer."
        }
        require((rawCursor as Number).toLong() >= 0L) {
            "$CURSOR_KEY cannot be negative."
        }

        return ClocktowerSemanticHistoryMode.values().firstOrNull { it.name == rawMode }
            ?: throw IllegalArgumentException("Unknown $MODE_KEY '$rawMode'.")
    }
}
