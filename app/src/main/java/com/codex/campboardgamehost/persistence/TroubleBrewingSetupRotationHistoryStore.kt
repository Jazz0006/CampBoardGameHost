package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationHistory
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import org.json.JSONArray
import org.json.JSONObject

internal class TroubleBrewingSetupRotationHistoryStore(
    private val readRaw: () -> String?,
    private val writeRaw: (String) -> Boolean,
) {
    fun recordCompletedGame(
        gameId: String,
        selection: TroubleBrewingSetupPresetSelection,
    ): Boolean = recordCompletedGame(
        gameId = gameId,
        record = TroubleBrewingSetupRotationRecordFactory.fromSelection(selection),
    )

    fun recordCompletedGame(
        gameId: String,
        record: TroubleBrewingSetupRotationRecord,
    ): Boolean {
        require(gameId.isNotBlank()) { "Trouble Brewing rotation-history game ID cannot be blank." }
        TroubleBrewingSetupRotationRecordFactory.validate(record)
        val existing = decodeOrEmpty(readRaw())
        val existingForGame = existing.firstOrNull { it.gameId == gameId }
        if (existingForGame != null) {
            require(existingForGame.record == record) {
                "Trouble Brewing rotation history already contains a different setup for game '$gameId'."
            }
            return true
        }

        val updated = trimPerPlayerCount(
            listOf(PersistedRotationEntry(gameId = gameId, record = record)) + existing,
        )
        return writeRaw(encode(updated))
    }

    fun historyFor(
        datasetId: String,
        schemaVersion: Int,
        playerCount: Int,
    ): TroubleBrewingSetupRotationHistory {
        require(datasetId.isNotBlank()) { "Trouble Brewing rotation-history dataset ID cannot be blank." }
        require(schemaVersion > 0) { "Trouble Brewing rotation-history schema version must be positive." }
        require(playerCount > 0) { "Trouble Brewing rotation-history player count must be positive." }

        val records = decodeOrEmpty(readRaw())
            .asSequence()
            .map { it.record }
            .filter {
                it.datasetId == datasetId &&
                    it.schemaVersion == schemaVersion &&
                    it.playerCount == playerCount
            }
            .take(MAX_GAMES_PER_PLAYER_COUNT)
            .toList()
        return TroubleBrewingSetupRotationHistory(recentGames = records)
    }

    private fun trimPerPlayerCount(entries: List<PersistedRotationEntry>): List<PersistedRotationEntry> {
        val retainedCounts = mutableMapOf<Int, Int>()
        return entries.filter { entry ->
            val playerCount = entry.record.playerCount
            val retained = retainedCounts.getOrDefault(playerCount, 0)
            if (retained >= MAX_GAMES_PER_PLAYER_COUNT) {
                false
            } else {
                retainedCounts[playerCount] = retained + 1
                true
            }
        }
    }

    private fun encode(entries: List<PersistedRotationEntry>): String = JSONObject().apply {
        put("version", CURRENT_VERSION)
        put("entries", JSONArray().apply {
            entries.forEach { entry ->
                put(JSONObject().apply {
                    put("gameId", entry.gameId)
                    put("datasetId", entry.record.datasetId)
                    put("schemaVersion", entry.record.schemaVersion)
                    put("presetId", entry.record.presetId)
                    put("playerCount", entry.record.playerCount)
                    put("realNonDemonRoleIds", entry.record.realNonDemonRoleIds.sorted().toJsonArray())
                    put("minionRoleIds", entry.record.minionRoleIds.sorted().toJsonArray())
                    put("primaryStyleTag", entry.record.primaryStyleTag ?: JSONObject.NULL)
                    put("selectedDrunkShownRole", entry.record.selectedDrunkShownRole ?: JSONObject.NULL)
                })
            }
        })
    }.toString()

    private fun decodeOrEmpty(raw: String?): List<PersistedRotationEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { decode(raw) }.getOrDefault(emptyList())
    }

    private fun decode(raw: String): List<PersistedRotationEntry> {
        val root = JSONObject(raw)
        require(root.requiredInt("version") == CURRENT_VERSION) {
            "Unsupported Trouble Brewing rotation-history version."
        }
        val entriesJson = root.optJSONArray("entries")
            ?: throw IllegalArgumentException("Trouble Brewing rotation-history entries must be an array.")
        val entries = buildList {
            for (index in 0 until entriesJson.length()) {
                val entry = entriesJson.optJSONObject(index)
                    ?: throw IllegalArgumentException("Trouble Brewing rotation-history entry $index must be an object.")
                val record = TroubleBrewingSetupRotationRecord(
                    datasetId = entry.requiredString("datasetId"),
                    schemaVersion = entry.requiredInt("schemaVersion"),
                    presetId = entry.requiredString("presetId"),
                    playerCount = entry.requiredInt("playerCount"),
                    realNonDemonRoleIds = entry.requiredStringSet("realNonDemonRoleIds"),
                    minionRoleIds = entry.requiredStringSet("minionRoleIds"),
                    primaryStyleTag = entry.nullableString("primaryStyleTag"),
                    selectedDrunkShownRole = entry.nullableString("selectedDrunkShownRole"),
                ).also(TroubleBrewingSetupRotationRecordFactory::validate)
                add(
                    PersistedRotationEntry(
                        gameId = entry.requiredString("gameId").also {
                            require(it.isNotBlank()) { "Persisted Trouble Brewing game ID cannot be blank." }
                        },
                        record = record,
                    ),
                )
            }
        }
        require(entries.map { it.gameId }.distinct().size == entries.size) {
            "Persisted Trouble Brewing rotation-history game IDs must be unique."
        }
        return trimPerPlayerCount(entries)
    }

    private data class PersistedRotationEntry(
        val gameId: String,
        val record: TroubleBrewingSetupRotationRecord,
    )

    companion object {
        const val CURRENT_VERSION = 1
        const val MAX_GAMES_PER_PLAYER_COUNT = 5
        private const val PREFS_NAME = "camp_board_game_host"
        private const val STORAGE_KEY = "tb_setup_rotation_history_v1"

        fun fromContext(context: Context): TroubleBrewingSetupRotationHistoryStore {
            val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return TroubleBrewingSetupRotationHistoryStore(
                readRaw = { preferences.getString(STORAGE_KEY, null) },
                writeRaw = { raw -> preferences.edit().putString(STORAGE_KEY, raw).commit() },
            )
        }
    }
}

private fun List<String>.toJsonArray(): JSONArray = JSONArray().apply { forEach(::put) }

private fun JSONObject.requiredString(key: String): String {
    require(has(key) && !isNull(key)) { "Missing required persisted string '$key'." }
    return opt(key) as? String ?: throw IllegalArgumentException("Persisted '$key' must be a string.")
}

private fun JSONObject.requiredInt(key: String): Int {
    require(has(key) && !isNull(key)) { "Missing required persisted integer '$key'." }
    val raw = opt(key)
    require(raw is Byte || raw is Short || raw is Int || raw is Long) {
        "Persisted '$key' must be an integer."
    }
    val value = (raw as Number).toLong()
    require(value in Int.MIN_VALUE..Int.MAX_VALUE) { "Persisted '$key' is outside Int range." }
    return value.toInt()
}

private fun JSONObject.requiredStringSet(key: String): Set<String> {
    val array = optJSONArray(key)
        ?: throw IllegalArgumentException("Persisted '$key' must be an array.")
    return buildList {
        for (index in 0 until array.length()) {
            val value = array.opt(index) as? String
                ?: throw IllegalArgumentException("Persisted '$key' entry $index must be a string.")
            require(value.isNotBlank()) { "Persisted '$key' entries cannot be blank." }
            add(value)
        }
    }.also { values ->
        require(values.distinct().size == values.size) { "Persisted '$key' entries must be unique." }
    }.toSet()
}

private fun JSONObject.nullableString(key: String): String? {
    require(has(key)) { "Missing persisted nullable string '$key'." }
    if (isNull(key)) return null
    return (opt(key) as? String)
        ?.also { require(it.isNotBlank()) { "Persisted '$key' cannot be blank." } }
        ?: throw IllegalArgumentException("Persisted '$key' must be a string or null.")
}
