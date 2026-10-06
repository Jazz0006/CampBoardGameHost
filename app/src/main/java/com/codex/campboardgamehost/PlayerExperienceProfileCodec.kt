package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small persistent profile codec for cross-game player experience.
 *
 * NORMAL is the default and is intentionally omitted from storage. Claims and declared pressure are
 * current-game inputs and therefore never belong in this profile codec.
 */
internal object PlayerExperienceProfileCodec {
    fun encode(levelByPlayerName: Map<String, PlayerExperienceLevelV1>): String {
        val normalized = levelByPlayerName.entries
            .map { (name, level) -> name.trim() to level }
            .filter { (name, level) -> name.isNotEmpty() && level != PlayerExperienceLevelV1.NORMAL }
            .sortedBy { (name, _) -> name }
        require(normalized.map { it.first }.distinct().size == normalized.size) {
            "Player experience profile names must be unique after trimming."
        }
        return JSONArray().apply {
            normalized.forEach { (name, level) ->
                put(JSONObject().apply {
                    put("playerName", name)
                    put("experienceLevel", level.name)
                })
            }
        }.toString()
    }

    fun decode(raw: String?): Map<String, PlayerExperienceLevelV1> {
        if (raw.isNullOrBlank()) return emptyMap()
        val array = JSONArray(raw)
        return buildMap {
            for (index in 0 until array.length()) {
                val entry = array.optJSONObject(index)
                    ?: throw IllegalArgumentException("Player experience profile entry must be an object.")
                val name = entry.getString("playerName").trim()
                require(name.isNotEmpty()) { "Player experience profile name cannot be blank." }
                require(name !in this) { "Player experience profile names must be unique." }
                val levelName = entry.getString("experienceLevel")
                val level = PlayerExperienceLevelV1.entries.singleOrNull { it.name == levelName }
                    ?: throw IllegalArgumentException("Unknown player experience level '$levelName'.")
                require(level != PlayerExperienceLevelV1.NORMAL) {
                    "Default NORMAL experience must not be persisted."
                }
                put(name, level)
            }
        }
    }
}
