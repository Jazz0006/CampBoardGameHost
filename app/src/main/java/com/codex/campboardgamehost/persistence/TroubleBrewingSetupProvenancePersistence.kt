package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPreset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetDataset
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetSelection
import org.json.JSONObject

/**
 * Additive active-game persistence seam for the selector-owned Trouble Brewing setup provenance.
 *
 * This codec never selects, draws, shuffles, or generates a seed. It can only rebind an already
 * committed preset identity to the exact compatible dataset supplied by the caller.
 */
internal object TroubleBrewingSetupProvenancePersistence {
    const val ROOT_KEY = "troubleBrewingSetupProvenance"

    fun encode(selection: TroubleBrewingSetupPresetSelection): JSONObject {
        validateSelection(selection)
        return JSONObject().apply {
            put("datasetId", selection.datasetId)
            put("schemaVersion", selection.schemaVersion)
            put("presetId", selection.presetId)
            put("playerCount", selection.playerCount)
            put("gameSeed", selection.gameSeed)
            put("selectedDrunkShownRole", selection.selectedDrunkShownRole ?: JSONObject.NULL)
        }
    }

    fun decodeOrNull(
        root: JSONObject,
        dataset: TroubleBrewingSetupPresetDataset,
    ): TroubleBrewingSetupPresetSelection? {
        if (!root.has(ROOT_KEY)) return null
        require(!root.isNull(ROOT_KEY)) { "$ROOT_KEY cannot be null." }
        val json = root.optJSONObject(ROOT_KEY)
            ?: throw IllegalArgumentException("$ROOT_KEY must be an object.")

        val persistedDatasetId = json.requiredString("datasetId")
        val persistedSchemaVersion = json.requiredInt("schemaVersion")
        require(persistedDatasetId == dataset.datasetId) {
            "Persisted Trouble Brewing setup dataset '$persistedDatasetId' does not match '${dataset.datasetId}'."
        }
        require(persistedSchemaVersion == dataset.schemaVersion) {
            "Persisted Trouble Brewing setup schema $persistedSchemaVersion does not match ${dataset.schemaVersion}."
        }

        val presetId = json.requiredString("presetId")
        val playerCount = json.requiredInt("playerCount")
        val matches = dataset.pools[playerCount].orEmpty().filter { it.id == presetId }
        require(matches.size == 1) {
            "Persisted Trouble Brewing preset '$presetId' cannot be resolved uniquely for $playerCount players."
        }
        val preset = matches.single()
        val selection = TroubleBrewingSetupPresetSelection(
            datasetId = persistedDatasetId,
            schemaVersion = persistedSchemaVersion,
            presetId = presetId,
            playerCount = playerCount,
            gameSeed = json.requiredLong("gameSeed"),
            preset = preset,
            selectedDrunkShownRole = json.nullableString("selectedDrunkShownRole"),
        )
        validateSelection(selection)
        return selection
    }

    private fun validateSelection(selection: TroubleBrewingSetupPresetSelection) {
        require(selection.datasetId.isNotBlank()) { "Trouble Brewing setup dataset ID cannot be blank." }
        require(selection.schemaVersion > 0) { "Trouble Brewing setup schema version must be positive." }
        require(selection.presetId == selection.preset.id) {
            "Trouble Brewing setup preset provenance is inconsistent."
        }
        require(selection.playerCount == selection.preset.playerCount) {
            "Trouble Brewing setup player count is inconsistent."
        }
        validateDrunkShownRole(selection.preset, selection.selectedDrunkShownRole)
    }

    private fun validateDrunkShownRole(
        preset: TroubleBrewingSetupPreset,
        selectedDrunkShownRole: String?,
    ) {
        val hasDrunk = DRUNK_EXTERNAL_ID in preset.outsiders
        if (!hasDrunk) {
            require(selectedDrunkShownRole == null) {
                "Non-Drunk Trouble Brewing setup cannot carry a Drunk shown role."
            }
            return
        }

        require(!selectedDrunkShownRole.isNullOrBlank()) {
            "Trouble Brewing Drunk setup requires its committed shown role."
        }
        require(selectedDrunkShownRole in preset.drunkAsOptions) {
            "Committed Trouble Brewing Drunk shown role must belong to the selected preset options."
        }
        val actualRoleIds = (preset.townsfolk + preset.outsiders + preset.minions + preset.demons).toSet()
        require(selectedDrunkShownRole !in actualRoleIds) {
            "Committed Trouble Brewing Drunk shown role must not be an actual in-play role."
        }
    }

    private fun JSONObject.requiredString(key: String): String {
        require(has(key) && !isNull(key)) { "Missing required Trouble Brewing setup string '$key'." }
        val value = opt(key) as? String
            ?: throw IllegalArgumentException("Trouble Brewing setup '$key' must be a string.")
        require(value.isNotBlank()) { "Trouble Brewing setup '$key' cannot be blank." }
        return value
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredIntegralNumber(key).toLong()
        require(value in Int.MIN_VALUE..Int.MAX_VALUE) {
            "Trouble Brewing setup '$key' is outside Int range."
        }
        return value.toInt()
    }

    private fun JSONObject.requiredLong(key: String): Long = requiredIntegralNumber(key).toLong()

    private fun JSONObject.requiredIntegralNumber(key: String): Number {
        require(has(key) && !isNull(key)) { "Missing required Trouble Brewing setup integer '$key'." }
        val raw = opt(key)
        require(raw is Byte || raw is Short || raw is Int || raw is Long) {
            "Trouble Brewing setup '$key' must be an integer."
        }
        return raw as Number
    }

    private fun JSONObject.nullableString(key: String): String? {
        require(has(key)) { "Missing Trouble Brewing setup nullable string '$key'." }
        if (isNull(key)) return null
        val value = opt(key) as? String
            ?: throw IllegalArgumentException("Trouble Brewing setup '$key' must be a string or null.")
        require(value.isNotBlank()) { "Trouble Brewing setup '$key' cannot be blank." }
        return value
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
