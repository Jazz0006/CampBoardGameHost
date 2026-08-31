package com.codex.campboardgamehost.clocktower.setup

import org.json.JSONArray
import org.json.JSONObject

internal object TroubleBrewingSetupPresetJson {
    fun parse(json: String): TroubleBrewingSetupPresetDataset = try {
        val root = JSONObject(json)
        TroubleBrewingSetupPresetDataset(
            schemaVersion = root.requiredInt("schema_version"),
            datasetId = root.requiredString("dataset_id"),
            status = root.requiredString("status"),
            declaredPoolSizes = parsePoolSizes(root.requiredObject("pool_sizes")),
            runtimeSelectionPolicy = parseRuntimeSelectionPolicy(root.requiredObject("runtime_selection_policy")),
            pools = parsePools(root.requiredObject("pools")),
        )
    } catch (error: IllegalArgumentException) {
        throw error
    } catch (error: Exception) {
        throw IllegalArgumentException("Invalid Trouble Brewing setup preset JSON.", error)
    }

    private fun parsePoolSizes(json: JSONObject): Map<Int, Int> =
        json.keys().asSequence().associate { rawPlayerCount ->
            rawPlayerCount.requiredPlayerCountKey() to json.requiredInt(rawPlayerCount)
        }

    private fun parseRuntimeSelectionPolicy(json: JSONObject): TroubleBrewingRuntimeSelectionPolicy =
        TroubleBrewingRuntimeSelectionPolicy(
            exactRepeat = json.requiredString("exact_repeat"),
            similarityScope = json.requiredString("similarity_scope"),
            roleOverlapFormula = json.requiredString("role_overlap_formula"),
            lastGameMaxOverlap = parsePlayerCountDoubles(json.requiredObject("last_game_max_overlap")),
            historyWeights = json.requiredDoubleList("history_weights"),
            extraSoftPenalties = json.requiredStringList("extra_soft_penalties"),
            fallback = json.requiredString("fallback"),
        )

    private fun parsePlayerCountDoubles(json: JSONObject): Map<Int, Double> =
        json.keys().asSequence().associate { rawPlayerCount ->
            rawPlayerCount.requiredPlayerCountKey() to json.requiredDouble(rawPlayerCount)
        }

    private fun parsePools(json: JSONObject): Map<Int, List<TroubleBrewingSetupPreset>> =
        json.keys().asSequence().associate { rawPlayerCount ->
            val playerCount = rawPlayerCount.requiredPlayerCountKey()
            val presets = json.requiredArray(rawPlayerCount).mapObjects { preset ->
                TroubleBrewingSetupPreset(
                    id = preset.requiredString("id"),
                    playerCount = preset.requiredInt("player_count"),
                    townsfolk = preset.requiredStringList("townsfolk"),
                    outsiders = preset.requiredStringList("outsiders"),
                    minions = preset.requiredStringList("minions"),
                    demons = preset.requiredStringList("demons"),
                    source = preset.requiredString("source"),
                    complexity = preset.requiredString("complexity"),
                    styleTags = preset.requiredStringList("style_tags"),
                    drunkAsOptions = preset.requiredStringList("drunk_as_options"),
                )
            }
            playerCount to presets
        }

    private fun String.requiredPlayerCountKey(): Int =
        toIntOrNull() ?: throw IllegalArgumentException("Player-count pool key '$this' must be an integer.")

    private fun JSONObject.requiredString(key: String): String {
        val value = get(key)
        require(value is String) { "Field '$key' must be a string." }
        return value
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = get(key)
        require(value is Number) { "Field '$key' must be an integer." }
        return value.toString().toIntOrNull()
            ?: throw IllegalArgumentException("Field '$key' must be an integer.")
    }

    private fun JSONObject.requiredDouble(key: String): Double {
        val value = get(key)
        require(value is Number) { "Field '$key' must be a number." }
        return value.toDouble()
    }

    private fun JSONObject.requiredObject(key: String): JSONObject {
        val value = get(key)
        require(value is JSONObject) { "Field '$key' must be an object." }
        return value
    }

    private fun JSONObject.requiredArray(key: String): JSONArray {
        val value = get(key)
        require(value is JSONArray) { "Field '$key' must be an array." }
        return value
    }

    private fun JSONObject.requiredStringList(key: String): List<String> =
        requiredArray(key).mapValues { index, value ->
            require(value is String) { "Field '$key' item $index must be a string." }
            value
        }

    private fun JSONObject.requiredDoubleList(key: String): List<Double> =
        requiredArray(key).mapValues { index, value ->
            require(value is Number) { "Field '$key' item $index must be a number." }
            value.toDouble()
        }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        mapValues { index, value ->
            require(value is JSONObject) { "Array item $index must be an object." }
            transform(value)
        }

    private fun <T> JSONArray.mapValues(transform: (Int, Any) -> T): List<T> =
        (0 until length()).map { index -> transform(index, get(index)) }
}
