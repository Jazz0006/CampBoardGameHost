package com.codex.campboardgamehost.clocktower.catalog

import org.json.JSONArray
import org.json.JSONObject

internal data class ParsedClocktowerScript(
    val name: String?,
    val author: String?,
    val characterIds: List<String>,
    val firstNightOverride: List<String>?,
    val otherNightOverride: List<String>?,
)

/** The two bundled official scripts: metadata, official IDs and fixed night orders only. */
internal object ClocktowerScriptJsonParser {
    fun parse(json: String): ParsedClocktowerScript {
        val root = try { JSONArray(json) } catch (error: Exception) {
            clocktowerValidationFailure(ClocktowerScriptValidationCode.INVALID_JSON, "Script must be an array.", cause = error)
        }
        clocktowerValidate(root.length() in 5..201, ClocktowerScriptValidationCode.INVALID_SCRIPT_SIZE, { "Invalid script size." })
        var metadata: JSONObject? = null
        val ids = mutableListOf<String>()
        for (index in 0 until root.length()) {
            val entry = root.get(index)
            val id = when (entry) {
                is String -> entry
                is JSONObject -> {
                    val value = requiredString(entry, "id")
                    if (value == "_meta") {
                        clocktowerValidate(metadata == null, ClocktowerScriptValidationCode.DUPLICATE_META, { "Duplicate script metadata." })
                        metadata = entry
                        continue
                    }
                    clocktowerValidate(entry.length() == 1, ClocktowerScriptValidationCode.INVALID_ENTRY, { "Only official character references are supported." })
                    value
                }
                else -> clocktowerValidationFailure(ClocktowerScriptValidationCode.INVALID_ENTRY, "Expected official character ID.")
            }
            clocktowerValidate(id.matches(Regex("[a-z0-9]{1,50}")), ClocktowerScriptValidationCode.INVALID_CHARACTER_ID, { "Invalid official character ID." })
            ids += id
        }
        val meta = metadata
        return ParsedClocktowerScript(
            meta?.let { requiredString(it, "name") },
            meta?.takeIf { it.has("author") }?.let { requiredString(it, "author") }, ids,
            meta?.order("firstNight"), meta?.order("otherNight"),
        )
    }

    private fun requiredString(json: JSONObject, key: String): String {
        clocktowerValidate(json.has(key) && json.get(key) is String, ClocktowerScriptValidationCode.INVALID_FIELD, { "Expected string '$key'." })
        return json.getString(key).also {
            clocktowerValidate(it.isNotBlank() && it.length <= 50, ClocktowerScriptValidationCode.INVALID_FIELD, { "Invalid '$key'." })
        }
    }

    private fun JSONObject.order(key: String): List<String>? {
        if (!has(key)) return null
        clocktowerValidate(get(key) is JSONArray, ClocktowerScriptValidationCode.INVALID_FIELD, { "Expected order array." })
        val array = getJSONArray(key)
        return (0 until array.length()).map { index ->
            clocktowerValidate(array.get(index) is String, ClocktowerScriptValidationCode.INVALID_FIELD, { "Expected order token." })
            array.getString(index)
        }
    }
}
