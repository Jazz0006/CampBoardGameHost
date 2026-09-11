package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPlayerStartingIdentity
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingStartingRoleCategory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Active-game persistence boundary for the compact Trouble Brewing completion/diversity fact.
 *
 * Decoding this record never loads the preset dataset. It carries only the TB-specific metadata
 * required if the restored game later completes and must be written to rotation history.
 */
internal object TroubleBrewingSetupCompletionPersistence {
    const val ROOT_KEY = "troubleBrewingSetupCompletion"
    const val SCHEMA_VERSION = 2

    fun encode(record: TroubleBrewingSetupRotationRecord): JSONObject {
        TroubleBrewingSetupRotationRecordFactory.validate(record)
        return JSONObject().apply {
            put("schemaVersion", SCHEMA_VERSION)
            put("datasetId", record.datasetId)
            put("datasetSchemaVersion", record.schemaVersion)
            put("presetId", record.presetId)
            put("playerCount", record.playerCount)
            put("realNonDemonRoleIds", record.realNonDemonRoleIds.sorted().toCompletionJsonArray())
            put("minionRoleIds", record.minionRoleIds.sorted().toCompletionJsonArray())
            put("primaryStyleTag", record.primaryStyleTag ?: JSONObject.NULL)
            put("selectedDrunkShownRole", record.selectedDrunkShownRole ?: JSONObject.NULL)
            put(
                "playerStartingIdentities",
                record.playerStartingIdentities.toCompletionStartingIdentitiesJsonArray(),
            )
        }
    }

    fun decodeOrNull(root: JSONObject): TroubleBrewingSetupRotationRecord? {
        if (!root.has(ROOT_KEY)) return null
        require(!root.isNull(ROOT_KEY)) { "$ROOT_KEY cannot be null." }
        val json = root.optJSONObject(ROOT_KEY)
            ?: throw IllegalArgumentException("$ROOT_KEY must be an object.")
        val schemaVersion = json.requiredCompletionInt("schemaVersion")
        require(schemaVersion in LEGACY_SCHEMA_VERSION..SCHEMA_VERSION) {
            "Unsupported Trouble Brewing setup completion schema '$schemaVersion'."
        }

        return TroubleBrewingSetupRotationRecord(
            datasetId = json.requiredCompletionString("datasetId"),
            schemaVersion = json.requiredCompletionInt("datasetSchemaVersion"),
            presetId = json.requiredCompletionString("presetId"),
            playerCount = json.requiredCompletionInt("playerCount"),
            realNonDemonRoleIds = json.requiredCompletionStringSet("realNonDemonRoleIds"),
            minionRoleIds = json.requiredCompletionStringSet("minionRoleIds"),
            primaryStyleTag = json.requiredCompletionNullableString("primaryStyleTag"),
            selectedDrunkShownRole = json.requiredCompletionNullableString("selectedDrunkShownRole"),
            playerStartingIdentities = if (schemaVersion == LEGACY_SCHEMA_VERSION) {
                emptyList()
            } else {
                json.requiredCompletionStartingIdentities("playerStartingIdentities")
            },
        ).also(TroubleBrewingSetupRotationRecordFactory::validate)
    }

    private const val LEGACY_SCHEMA_VERSION = 1
}

private fun List<String>.toCompletionJsonArray(): JSONArray = JSONArray().apply { forEach(::put) }

private fun List<TroubleBrewingPlayerStartingIdentity>.toCompletionStartingIdentitiesJsonArray(): JSONArray =
    JSONArray().apply {
        forEach { identity ->
            put(JSONObject().apply {
                put("playerKey", identity.playerKey)
                put("actualRoleId", identity.actualRoleId)
                put("shownRoleId", identity.shownRoleId)
                put("actualRoleCategory", identity.actualRoleCategory.name)
            })
        }
    }

private fun JSONObject.requiredCompletionString(key: String): String {
    require(has(key) && !isNull(key)) { "Missing required Trouble Brewing completion string '$key'." }
    val value = opt(key) as? String
        ?: throw IllegalArgumentException("Trouble Brewing completion '$key' must be a string.")
    require(value.isNotBlank()) { "Trouble Brewing completion '$key' cannot be blank." }
    return value
}

private fun JSONObject.requiredCompletionInt(key: String): Int {
    require(has(key) && !isNull(key)) { "Missing required Trouble Brewing completion integer '$key'." }
    val raw = opt(key)
    require(raw is Byte || raw is Short || raw is Int || raw is Long) {
        "Trouble Brewing completion '$key' must be an integer."
    }
    val value = (raw as Number).toLong()
    require(value in Int.MIN_VALUE..Int.MAX_VALUE) {
        "Trouble Brewing completion '$key' is outside Int range."
    }
    return value.toInt()
}

private fun JSONObject.requiredCompletionStringSet(key: String): Set<String> {
    require(has(key) && !isNull(key)) { "Missing required Trouble Brewing completion array '$key'." }
    val array = optJSONArray(key)
        ?: throw IllegalArgumentException("Trouble Brewing completion '$key' must be an array.")
    val values = buildList {
        for (index in 0 until array.length()) {
            val value = array.opt(index) as? String
                ?: throw IllegalArgumentException("Trouble Brewing completion '$key' entry $index must be a string.")
            require(value.isNotBlank()) { "Trouble Brewing completion '$key' entries cannot be blank." }
            add(value)
        }
    }
    require(values.distinct().size == values.size) {
        "Trouble Brewing completion '$key' entries must be unique."
    }
    return values.toSet()
}

private fun JSONObject.requiredCompletionStartingIdentities(
    key: String,
): List<TroubleBrewingPlayerStartingIdentity> {
    require(has(key) && !isNull(key)) { "Missing required Trouble Brewing completion array '$key'." }
    val array = optJSONArray(key)
        ?: throw IllegalArgumentException("Trouble Brewing completion '$key' must be an array.")
    return buildList {
        for (index in 0 until array.length()) {
            val identity = array.optJSONObject(index)
                ?: throw IllegalArgumentException(
                    "Trouble Brewing completion '$key' entry $index must be an object.",
                )
            val categoryName = identity.requiredCompletionString("actualRoleCategory")
            val category = runCatching { TroubleBrewingStartingRoleCategory.valueOf(categoryName) }
                .getOrElse {
                    throw IllegalArgumentException(
                        "Trouble Brewing completion starting identity has unknown role category '$categoryName'.",
                    )
                }
            add(
                TroubleBrewingPlayerStartingIdentity(
                    playerKey = identity.requiredCompletionString("playerKey"),
                    actualRoleId = identity.requiredCompletionString("actualRoleId"),
                    shownRoleId = identity.requiredCompletionString("shownRoleId"),
                    actualRoleCategory = category,
                ),
            )
        }
    }
}

private fun JSONObject.requiredCompletionNullableString(key: String): String? {
    require(has(key)) { "Missing Trouble Brewing completion nullable string '$key'." }
    if (isNull(key)) return null
    return requiredCompletionString(key)
}
