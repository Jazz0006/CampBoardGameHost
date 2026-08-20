package com.codex.campboardgamehost.clocktower.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.net.URI
import java.security.MessageDigest

internal data class ImportedClocktowerJinx(
    val externalId: String,
    val reason: String,
)

internal data class ImportedClocktowerSpecialFeature(
    val type: String,
    val name: String,
    val value: ClocktowerSpecialValue?,
    val time: String?,
    val global: String?,
)

internal sealed interface ImportedClocktowerCharacter {
    val externalId: String

    data class OfficialReference(override val externalId: String) : ImportedClocktowerCharacter

    data class CustomDefinition(
        override val externalId: String,
        val name: String,
        val team: String,
        val abilityText: String,
        val firstNightOrder: BigDecimal,
        val otherNightOrder: BigDecimal,
        val firstNightReminder: String,
        val otherNightReminder: String,
        val reminders: List<String>,
        val globalReminders: List<String>,
        val setup: Boolean,
        val jinxes: List<ImportedClocktowerJinx>,
        val specialFeatures: List<ImportedClocktowerSpecialFeature>,
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
    private val displayOnlyCharacterKeys = setOf("name", "image", "flavor", "edition")
    private val customCharacterKeys = setOf(
        "id", "name", "image", "team", "edition", "ability", "flavor",
        "firstNight", "firstNightReminder", "otherNight", "otherNightReminder",
        "reminders", "remindersGlobal", "setup", "jinxes", "special",
    )
    private val teamValues = setOf("townsfolk", "outsider", "minion", "demon", "traveller", "fabled", "loric")
    private val specialTypes = setOf("selection", "ability", "signal", "vote", "reveal", "player", "reminder")
    private val specialNames = setOf(
        "grimoire", "pointing", "ghost-votes", "distribute-roles", "bag-disabled", "bag-duplicate",
        "evil-duplicate", "good-duplicate", "multiplier", "hidden", "replace-character", "player", "card",
        "open-eyes", "public",
    )
    private val specialTimes = setOf("pregame", "day", "night", "firstNight", "firstDay", "otherNight", "otherDay")
    private val specialGlobals = setOf("townsfolk", "outsider", "minion", "demon", "traveller", "dead")

    fun parse(json: String): ParsedClocktowerScript {
        val root = try {
            JSONArray(json)
        } catch (error: Exception) {
            clocktowerValidationFailure(
                ClocktowerScriptValidationCode.INVALID_JSON,
                "Clocktower custom script JSON must be a top-level array.",
                cause = error,
            )
        }
        clocktowerValidate(root.length() in 5..201, ClocktowerScriptValidationCode.INVALID_SCRIPT_SIZE, {
            "Clocktower custom script must contain between 5 and 201 top-level entries."
        })

        var metadata: JSONObject? = null
        val characters = mutableListOf<ImportedClocktowerCharacter>()
        for (index in 0 until root.length()) {
            val path = "[$index]"
            when (val entry = root.get(index)) {
                is String -> characters += ImportedClocktowerCharacter.OfficialReference(
                    validateExternalId(entry, maxLength = 20, path = path),
                )
                is JSONObject -> {
                    val id = requiredString(entry, "id", 50, "$path.id")
                    if (id == "_meta") {
                        clocktowerValidate(metadata == null, ClocktowerScriptValidationCode.DUPLICATE_META, {
                            "Clocktower custom script may contain only one _meta object."
                        }, path)
                        validateMetadata(entry, path)
                        metadata = entry
                    } else if (entry.length() == 1) {
                        characters += ImportedClocktowerCharacter.OfficialReference(
                            validateExternalId(id, maxLength = 20, path = "$path.id"),
                        )
                    } else {
                        characters += parseCustomCharacter(entry, id, path)
                    }
                }
                else -> clocktowerValidationFailure(
                    ClocktowerScriptValidationCode.INVALID_ENTRY,
                    "Clocktower custom script entries must be official character IDs or objects.",
                    path,
                )
            }
        }
        clocktowerValidate(characters.isNotEmpty(), ClocktowerScriptValidationCode.INVALID_SCRIPT_SIZE, {
            "Clocktower custom script must contain characters."
        })

        val meta = metadata
        return ParsedClocktowerScript(
            name = meta?.let { requiredString(it, "name", 50, "_meta.name") },
            author = meta?.optionalNonBlankString("author", 50, "_meta.author"),
            characters = characters,
            firstNightOverride = meta?.optionalStringArray("firstNight", 200, null, "_meta.firstNight"),
            otherNightOverride = meta?.optionalStringArray("otherNight", 200, null, "_meta.otherNight"),
            bootleggerRules = meta?.optionalStringArray("bootlegger", 10, null, "_meta.bootlegger") ?: emptyList(),
        )
    }

    private fun validateMetadata(meta: JSONObject, path: String) {
        requiredString(meta, "name", 50, "$path.name")
        meta.optionalNonBlankString("author", 50, "$path.author")
        meta.optionalUri("logo", 250, "$path.logo")
        meta.optionalBoolean("hideTitle", "$path.hideTitle")
        meta.optionalUri("background", 250, "$path.background")
        meta.optionalUri("almanac", 250, "$path.almanac")
        meta.optionalStringArray("bootlegger", 10, null, "$path.bootlegger")
        meta.optionalStringArray("firstNight", 200, null, "$path.firstNight")
        meta.optionalStringArray("otherNight", 200, null, "$path.otherNight")
    }

    private fun parseCustomCharacter(
        entry: JSONObject,
        rawId: String,
        path: String,
    ): ImportedClocktowerCharacter.CustomDefinition {
        val unexpected = entry.keys().asSequence().filterNot { it in customCharacterKeys }.toList()
        clocktowerValidate(unexpected.isEmpty(), ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Unknown custom character field(s): ${unexpected.sorted().joinToString()}"
        }, path)

        val externalId = validateExternalId(rawId, maxLength = 50, path = "$path.id")
        val team = requiredString(entry, "team", 20, "$path.team")
        clocktowerValidate(team in teamValues, ClocktowerScriptValidationCode.UNKNOWN_CHARACTER_TEAM, {
            "Unknown Clocktower character team '$team'."
        }, "$path.team")
        entry.optionalImage("image", path)
        entry.optionalNonBlankString("edition", 50, "$path.edition")
        entry.optionalString("flavor", 500, "$path.flavor")

        return ImportedClocktowerCharacter.CustomDefinition(
            externalId = externalId,
            name = requiredString(entry, "name", 30, "$path.name"),
            team = team,
            abilityText = requiredString(entry, "ability", 250, "$path.ability"),
            firstNightOrder = entry.optionalNonNegativeNumber("firstNight", "$path.firstNight"),
            otherNightOrder = entry.optionalNonNegativeNumber("otherNight", "$path.otherNight"),
            firstNightReminder = entry.optionalString("firstNightReminder", 500, "$path.firstNightReminder"),
            otherNightReminder = entry.optionalString("otherNightReminder", 500, "$path.otherNightReminder"),
            reminders = entry.optionalStringArray("reminders", 20, 30, "$path.reminders") ?: emptyList(),
            globalReminders = entry.optionalStringArray("remindersGlobal", 20, 25, "$path.remindersGlobal") ?: emptyList(),
            setup = entry.optionalBoolean("setup", "$path.setup"),
            jinxes = entry.optionalJinxes(path),
            specialFeatures = entry.optionalSpecialFeatures(path),
            sourceSemanticHash = semanticHash(entry),
        )
    }

    private fun JSONObject.optionalJinxes(path: String): List<ImportedClocktowerJinx> {
        if (!has("jinxes")) return emptyList()
        val array = requireArray("jinxes", "$path.jinxes")
        return (0 until array.length()).map { index ->
            val itemPath = "$path.jinxes[$index]"
            val raw = array.get(index)
            clocktowerValidate(raw is JSONObject, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower jinx entries must be objects."
            }, itemPath)
            val jinx = raw as JSONObject
            val allowedKeys = setOf("id", "reason")
            val unexpected = jinx.keys().asSequence().filterNot { it in allowedKeys }.toList()
            clocktowerValidate(unexpected.isEmpty(), ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Unknown Clocktower jinx field(s): ${unexpected.sorted().joinToString()}"
            }, itemPath)
            ImportedClocktowerJinx(
                externalId = validateExternalId(requiredString(jinx, "id", 50, "$itemPath.id"), 50, "$itemPath.id"),
                reason = requiredString(jinx, "reason", 500, "$itemPath.reason"),
            )
        }
    }

    private fun JSONObject.optionalSpecialFeatures(path: String): List<ImportedClocktowerSpecialFeature> {
        if (!has("special")) return emptyList()
        val array = requireArray("special", "$path.special")
        return (0 until array.length()).map { index ->
            val itemPath = "$path.special[$index]"
            val raw = array.get(index)
            clocktowerValidate(raw is JSONObject, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower special entries must be objects."
            }, itemPath)
            val feature = raw as JSONObject
            val type = requiredString(feature, "type", 30, "$itemPath.type")
            val name = requiredString(feature, "name", 40, "$itemPath.name")
            clocktowerValidate(type in specialTypes, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Unsupported Clocktower special type '$type'."
            }, "$itemPath.type")
            clocktowerValidate(name in specialNames, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Unsupported Clocktower special name '$name'."
            }, "$itemPath.name")
            val time = feature.optionalNonBlankString("time", 20, "$itemPath.time")
            if (time != null) {
                clocktowerValidate(time in specialTimes, ClocktowerScriptValidationCode.INVALID_FIELD, {
                    "Unsupported Clocktower special time '$time'."
                }, "$itemPath.time")
            }
            val global = feature.optionalNonBlankString("global", 20, "$itemPath.global")
            if (global != null) {
                clocktowerValidate(global in specialGlobals, ClocktowerScriptValidationCode.INVALID_FIELD, {
                    "Unsupported Clocktower special global scope '$global'."
                }, "$itemPath.global")
            }
            ImportedClocktowerSpecialFeature(
                type = type,
                name = name,
                value = feature.optionalSpecialValue("value", "$itemPath.value"),
                time = time,
                global = global,
            )
        }
    }

    private fun validateExternalId(raw: String, maxLength: Int, path: String): String {
        clocktowerValidate(raw.length <= maxLength && EXTERNAL_ID_PATTERN.matches(raw), ClocktowerScriptValidationCode.INVALID_CHARACTER_ID, {
            "Character IDs must be lowercase alphanumeric strings no longer than $maxLength characters."
        }, path)
        return raw
    }

    private fun requiredString(json: JSONObject, key: String, maxLength: Int, path: String): String {
        clocktowerValidate(json.has(key) && json.get(key) is String, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be a string."
        }, path)
        return json.getString(key).also { value ->
            clocktowerValidate(value.isNotBlank() && value.length <= maxLength, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower script field '$key' must be non-blank and no longer than $maxLength characters."
            }, path)
        }
    }

    private fun JSONObject.optionalNonBlankString(key: String, maxLength: Int, path: String): String? {
        if (!has(key)) return null
        clocktowerValidate(get(key) is String, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be a string."
        }, path)
        return getString(key).also { value ->
            clocktowerValidate(value.isNotBlank() && value.length <= maxLength, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower script field '$key' must be non-blank and no longer than $maxLength characters."
            }, path)
        }
    }

    private fun JSONObject.optionalString(key: String, maxLength: Int, path: String): String {
        if (!has(key)) return ""
        clocktowerValidate(get(key) is String, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be a string."
        }, path)
        return getString(key).also { value ->
            clocktowerValidate(value.length <= maxLength, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower script field '$key' cannot exceed $maxLength characters."
            }, path)
        }
    }

    private fun JSONObject.optionalStringArray(
        key: String,
        maxItems: Int,
        maxItemLength: Int?,
        path: String,
    ): List<String>? {
        if (!has(key)) return null
        val array = requireArray(key, path)
        clocktowerValidate(array.length() <= maxItems, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' cannot contain more than $maxItems items."
        }, path)
        return (0 until array.length()).map { index ->
            val itemPath = "$path[$index]"
            clocktowerValidate(array.get(index) is String, ClocktowerScriptValidationCode.INVALID_FIELD, {
                "Clocktower script field '$key' must contain only strings."
            }, itemPath)
            array.getString(index).also { value ->
                clocktowerValidate(value.isNotBlank(), ClocktowerScriptValidationCode.INVALID_FIELD, {
                    "Clocktower script field '$key' cannot contain blank strings."
                }, itemPath)
                if (maxItemLength != null) {
                    clocktowerValidate(value.length <= maxItemLength, ClocktowerScriptValidationCode.INVALID_FIELD, {
                        "Clocktower script field '$key' items cannot exceed $maxItemLength characters."
                    }, itemPath)
                }
            }
        }
    }

    private fun JSONObject.optionalNonNegativeNumber(key: String, path: String): BigDecimal {
        if (!has(key)) return BigDecimal.ZERO
        val raw = get(key)
        clocktowerValidate(raw is Number, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be numeric."
        }, path)
        val decimal = runCatching { BigDecimal(raw.toString()).stripTrailingZeros() }
            .getOrElse {
                clocktowerValidationFailure(
                    ClocktowerScriptValidationCode.INVALID_FIELD,
                    "Clocktower script field '$key' must be a finite number.",
                    path,
                    it,
                )
            }
        clocktowerValidate(decimal.signum() >= 0, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' cannot be negative."
        }, path)
        return decimal
    }

    private fun JSONObject.optionalBoolean(key: String, path: String): Boolean {
        if (!has(key)) return false
        clocktowerValidate(get(key) is Boolean, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be boolean."
        }, path)
        return getBoolean(key)
    }

    private fun JSONObject.optionalSpecialValue(key: String, path: String): ClocktowerSpecialValue? {
        if (!has(key)) return null
        return when (val raw = get(key)) {
            is String -> {
                clocktowerValidate(raw.length <= 50, ClocktowerScriptValidationCode.INVALID_FIELD, {
                    "Clocktower special string values cannot exceed 50 characters."
                }, path)
                ClocktowerSpecialValue(ClocktowerSpecialValueKind.TEXT, raw)
            }
            is Number -> {
                val canonical = runCatching { BigDecimal(raw.toString()).stripTrailingZeros().toPlainString() }
                    .getOrElse {
                        clocktowerValidationFailure(
                            ClocktowerScriptValidationCode.INVALID_FIELD,
                            "Clocktower special numeric values must be finite.",
                            path,
                            it,
                        )
                    }
                ClocktowerSpecialValue(ClocktowerSpecialValueKind.NUMBER, canonical)
            }
            else -> clocktowerValidationFailure(
                ClocktowerScriptValidationCode.INVALID_FIELD,
                "Clocktower special values must be strings or numbers.",
                path,
            )
        }
    }

    private fun JSONObject.optionalImage(key: String, path: String) {
        if (!has(key)) return
        when (val raw = get(key)) {
            is String -> validateUri(raw, "$path.$key")
            is JSONArray -> {
                clocktowerValidate(raw.length() in 1..3, ClocktowerScriptValidationCode.INVALID_FIELD, {
                    "Clocktower character image arrays must contain between 1 and 3 URLs."
                }, "$path.$key")
                repeat(raw.length()) { index ->
                    clocktowerValidate(raw.get(index) is String, ClocktowerScriptValidationCode.INVALID_FIELD, {
                        "Clocktower character image arrays must contain only URL strings."
                    }, "$path.$key[$index]")
                    validateUri(raw.getString(index), "$path.$key[$index]")
                }
            }
            else -> clocktowerValidationFailure(
                ClocktowerScriptValidationCode.INVALID_FIELD,
                "Clocktower character image must be a URL string or URL array.",
                "$path.$key",
            )
        }
    }

    private fun JSONObject.optionalUri(key: String, maxLength: Int, path: String) {
        if (!has(key)) return
        val value = optionalNonBlankString(key, maxLength, path) ?: return
        validateUri(value, path)
    }

    private fun validateUri(value: String, path: String) {
        val valid = runCatching { URI(value).isAbsolute }.getOrDefault(false)
        clocktowerValidate(valid, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower URI field must contain an absolute URI."
        }, path)
    }

    private fun JSONObject.requireArray(key: String, path: String): JSONArray {
        clocktowerValidate(has(key) && get(key) is JSONArray, ClocktowerScriptValidationCode.INVALID_FIELD, {
            "Clocktower script field '$key' must be an array."
        }, path)
        return getJSONArray(key)
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
        else -> clocktowerValidationFailure(
            ClocktowerScriptValidationCode.INVALID_FIELD,
            "Unsupported JSON value in Clocktower character metadata: ${value::class.java.simpleName}",
        )
    }

    private val EXTERNAL_ID_PATTERN = Regex("[a-z0-9]+")
}
