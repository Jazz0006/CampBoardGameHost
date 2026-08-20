package com.codex.campboardgamehost.clocktower.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.security.MessageDigest

internal sealed interface ImportedClocktowerCharacter {
    data class OfficialReference(val externalId: String) : ImportedClocktowerCharacter

    data class CustomDefinition(
        val externalId: String,
        val name: String,
        val team: String,
        val abilityText: String,
        val firstNightOrder: Int,
        val otherNightOrder: Int,
        val firstNightReminder: String,
        val otherNightReminder: String,
        val reminders: List<String>,
        val setup: Boolean,
        val sourceSemanticHash: String,
    ) : ImportedClocktowerCharacter
}

internal data class ParsedClocktowerScript(
    val name: String?,
    val author: String?,
    val characters: List<ImportedClocktowerCharacter>,
    val firstNightOverride: List<String>?,
    val otherNightOverride: List<String>?,
    val bootleggerRules: List<String>,
)

internal object ClocktowerScriptJsonParser {
    private val displayOnlyCharacterKeys = setOf("name", "image", "flavor")

    fun parse(json: String): ParsedClocktowerScript {
        val root = runCatching { JSONArray(json) }
            .getOrElse { throw IllegalArgumentException("Clocktower custom script JSON must be a top-level array.", it) }

        var metadata: JSONObject? = null
        val characters = mutableListOf<ImportedClocktowerCharacter>()
        for (index in 0 until root.length()) {
            when (val entry = root.get(index)) {
                is String -> characters += ImportedClocktowerCharacter.OfficialReference(validateExternalId(entry))
                is JSONObject -> {
                    val id = requiredString(entry, "id")
                    if (id == "_meta") {
                        require(metadata == null) { "Clocktower custom script may contain only one _meta object." }
                        metadata = entry
                    } else if (entry.length() == 1) {
                        characters += ImportedClocktowerCharacter.OfficialReference(validateExternalId(id))
                    } else {
                        characters += parseCustomCharacter(entry, id)
                    }
                }
                else -> throw IllegalArgumentException(
                    "Clocktower custom script entries must be official character IDs or objects.",
                )
            }
        }
        require(characters.isNotEmpty()) { "Clocktower custom script must contain characters." }

        val meta = metadata
        return ParsedClocktowerScript(
            name = meta?.let { requiredString(it, "name") },
            author = meta?.optionalNonBlankString("author"),
            characters = characters,
            firstNightOverride = meta?.optionalStringArray("firstNight"),
            otherNightOverride = meta?.optionalStringArray("otherNight"),
            bootleggerRules = meta?.optionalStringArray("bootlegger") ?: emptyList(),
        )
    }

    private fun parseCustomCharacter(entry: JSONObject, rawId: String): ImportedClocktowerCharacter.CustomDefinition {
        val externalId = validateExternalId(rawId)
        return ImportedClocktowerCharacter.CustomDefinition(
            externalId = externalId,
            name = requiredString(entry, "name"),
            team = requiredString(entry, "team"),
            abilityText = requiredString(entry, "ability"),
            firstNightOrder = entry.optionalNonNegativeInt("firstNight"),
            otherNightOrder = entry.optionalNonNegativeInt("otherNight"),
            firstNightReminder = entry.optionalString("firstNightReminder"),
            otherNightReminder = entry.optionalString("otherNightReminder"),
            reminders = entry.optionalStringArray("reminders") ?: emptyList(),
            setup = entry.optionalBoolean("setup"),
            sourceSemanticHash = semanticHash(entry),
        )
    }

    private fun validateExternalId(raw: String): String {
        require(EXTERNAL_ID_PATTERN.matches(raw)) {
            "Official/custom character IDs must be lowercase alphanumeric strings."
        }
        return raw
    }

    private fun requiredString(json: JSONObject, key: String): String {
        require(json.has(key) && json.get(key) is String) { "Clocktower script field '$key' must be a string." }
        return json.getString(key).also { value ->
            require(value.isNotBlank()) { "Clocktower script field '$key' cannot be blank." }
        }
    }

    private fun JSONObject.optionalNonBlankString(key: String): String? {
        if (!has(key)) return null
        require(get(key) is String) { "Clocktower script field '$key' must be a string." }
        return getString(key).also { value ->
            require(value.isNotBlank()) { "Clocktower script field '$key' cannot be blank." }
        }
    }

    private fun JSONObject.optionalString(key: String): String {
        if (!has(key)) return ""
        require(get(key) is String) { "Clocktower script field '$key' must be a string." }
        return getString(key)
    }

    private fun JSONObject.optionalStringArray(key: String): List<String>? {
        if (!has(key)) return null
        require(get(key) is JSONArray) { "Clocktower script field '$key' must be an array." }
        val array = getJSONArray(key)
        return (0 until array.length()).map { index ->
            require(array.get(index) is String) { "Clocktower script field '$key' must contain only strings." }
            array.getString(index).also { value ->
                require(value.isNotBlank()) { "Clocktower script field '$key' cannot contain blank strings." }
            }
        }
    }

    private fun JSONObject.optionalNonNegativeInt(key: String): Int {
        if (!has(key)) return 0
        val raw = get(key)
        require(raw is Number) { "Clocktower script field '$key' must be numeric." }
        val decimal = BigDecimal(raw.toString()).stripTrailingZeros()
        require(decimal.scale() <= 0 && decimal.signum() >= 0) {
            "Clocktower script field '$key' must be a non-negative integer."
        }
        return runCatching { decimal.intValueExact() }
            .getOrElse { throw IllegalArgumentException("Clocktower script field '$key' is out of range.", it) }
    }

    private fun JSONObject.optionalBoolean(key: String): Boolean {
        if (!has(key)) return false
        require(get(key) is Boolean) { "Clocktower script field '$key' must be boolean." }
        return getBoolean(key)
    }

    private fun semanticHash(character: JSONObject): String {
        val keys = character.keys().asSequence()
            .filterNot { it in displayOnlyCharacterKeys }
            .sorted()
            .toList()
        val canonical = keys.joinToString(prefix = "{", postfix = "}") { key ->
            "${JSONObject.quote(key)}:${canonicalJsonValue(character.get(key))}"
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    private fun canonicalJsonValue(value: Any?): String = when (value) {
        null, JSONObject.NULL -> "null"
        is String -> JSONObject.quote(value)
        is Boolean -> value.toString()
        is Number -> BigDecimal(value.toString()).stripTrailingZeros().toPlainString()
        is JSONArray -> (0 until value.length()).joinToString(prefix = "[", postfix = "]") { index ->
            canonicalJsonValue(value.get(index))
        }
        is JSONObject -> value.keys().asSequence().sorted().joinToString(prefix = "{", postfix = "}") { key ->
            "${JSONObject.quote(key)}:${canonicalJsonValue(value.get(key))}"
        }
        else -> throw IllegalArgumentException("Unsupported JSON value in Clocktower character metadata: ${value::class.java.simpleName}")
    }

    private val EXTERNAL_ID_PATTERN = Regex("[a-z0-9]{1,50}")
}
