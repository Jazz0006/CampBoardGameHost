package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.json.JSONObject

/** Active-game JSON contract for explicit Clocktower semantic-history mode. */
internal object ClocktowerSemanticHistoryPersistence {
    const val MODE_KEY = "clocktowerSemanticHistoryMode"

    fun encode(mode: ClocktowerSemanticHistoryMode): String = mode.name

    fun decodeMode(json: JSONObject): ClocktowerSemanticHistoryMode {
        require(json.has(MODE_KEY)) { "$MODE_KEY is required in active-game schema v3." }
        require(!json.isNull(MODE_KEY)) { "$MODE_KEY cannot be null." }
        val raw = json.opt(MODE_KEY)
        require(raw is String && raw.isNotBlank()) {
            "$MODE_KEY must be a non-blank string."
        }
        return ClocktowerSemanticHistoryMode.values().firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown $MODE_KEY '$raw'.")
    }
}
