package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import org.json.JSONObject

/** Active-game JSON contract for explicit Clocktower semantic-history mode. */
internal object ClocktowerSemanticHistoryPersistence {
    const val MODE_KEY = "clocktowerSemanticHistoryMode"

    fun encode(mode: ClocktowerSemanticHistoryMode): String = mode.name

    fun decodeMode(json: JSONObject): ClocktowerSemanticHistoryMode {
        if (!json.has(MODE_KEY)) return ClocktowerSemanticHistoryMode.LEGACY_LOCAL
        require(!json.isNull(MODE_KEY)) { "$MODE_KEY cannot be null when present." }
        val raw = json.opt(MODE_KEY)
        require(raw is String && raw.isNotBlank()) {
            "$MODE_KEY must be a non-blank string when present."
        }
        return ClocktowerSemanticHistoryMode.values().firstOrNull { it.name == raw }
            ?: throw IllegalArgumentException("Unknown $MODE_KEY '$raw'.")
    }
}
