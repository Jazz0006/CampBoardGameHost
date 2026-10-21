package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import java.security.MessageDigest
import java.util.Locale

internal object ClocktowerScriptNormalizer {
    fun normalize(
        parsed: ParsedClocktowerScript,
        requestedScriptId: ScriptId,
        registry: ClocktowerCharacterRegistry,
        requestedSource: ClocktowerScriptSource,
    ): ValidatedClocktowerRuleset {
        val ids = parsed.characterIds
        clocktowerValidate(ids.distinct().size == ids.size, ClocktowerScriptValidationCode.DUPLICATE_CHARACTER_ID, { "Duplicate official character ID." })
        val characters = ids.map { id -> registry.findByExternalId(id)
            ?: clocktowerValidationFailure(ClocktowerScriptValidationCode.UNKNOWN_CHARACTER_ID, "Unknown official character '$id'.") }
        val byExternalId = characters.associateBy { it.externalId }
        val first = parsed.firstNightOverride?.let { normalizeNightOrder(it, byExternalId, true) }
        val other = parsed.otherNightOverride?.let { normalizeNightOrder(it, byExternalId, false) }
        val hash = ClocktowerCatalogContentHasher.hash(characters, first, other)
        return ValidatedClocktowerRuleset(ClocktowerScriptDefinition(
            requestedScriptId, parsed.name ?: requestedScriptId.value, parsed.author, characters.map { it.id },
            first, other, requestedSource, hash,
        ), characters, aggregateCoverage(characters))
    }

    private fun normalizeNightOrder(
        rawTokens: List<String>,
        charactersByExternalId: Map<String, ClocktowerCharacterDefinition>,
        firstNight: Boolean,
    ): List<NightOrderToken> {
        clocktowerValidate(rawTokens.distinct().size == rawTokens.size, ClocktowerScriptValidationCode.DUPLICATE_NIGHT_TOKEN, {
            "Clocktower night-order tokens must be unique."
        })
        return rawTokens.mapIndexed { index, rawToken ->
            val path = if (firstNight) "_meta.firstNight[$index]" else "_meta.otherNight[$index]"
            clocktowerValidate(rawToken == rawToken.lowercase(Locale.ROOT), ClocktowerScriptValidationCode.INVALID_NIGHT_TOKEN, {
                "Clocktower night-order tokens must use lowercase official IDs."
            }, path)
            when (rawToken) {
                "dusk" -> NightOrderToken.System.DUSK
                "dawn" -> NightOrderToken.System.DAWN
                "minioninfo" -> {
                    clocktowerValidate(firstNight, ClocktowerScriptValidationCode.INVALID_NIGHT_SYSTEM_TOKEN, {
                        "minioninfo is valid only in firstNight metadata."
                    }, path)
                    NightOrderToken.System.MINION_INFO
                }
                "demoninfo" -> {
                    clocktowerValidate(firstNight, ClocktowerScriptValidationCode.INVALID_NIGHT_SYSTEM_TOKEN, {
                        "demoninfo is valid only in firstNight metadata."
                    }, path)
                    NightOrderToken.System.DEMON_INFO
                }
                else -> {
                    val character = charactersByExternalId[rawToken]
                        ?: clocktowerValidationFailure(
                            ClocktowerScriptValidationCode.INVALID_NIGHT_TOKEN,
                            "Unknown or off-script night-order token '$rawToken'.",
                            path,
                        )
                    NightOrderToken.Character(character.id)
                }
            }
        }
    }

    private fun aggregateCoverage(characters: List<ClocktowerCharacterDefinition>): RuleCoverage = when {
        characters.any { it.automationCoverage == RuleCoverage.UNVERIFIED } -> RuleCoverage.UNVERIFIED
        characters.any { it.automationCoverage == RuleCoverage.PARTIAL } -> RuleCoverage.PARTIAL
        else -> RuleCoverage.VERIFIED
    }
}

internal object ClocktowerCatalogContentHasher {
    fun hash(
        characters: List<ClocktowerCharacterDefinition>,
        firstNightOverride: List<NightOrderToken>?,
        otherNightOverride: List<NightOrderToken>?,
    ): String {
        val canonical = buildString {
            append("{\"bootlegger\":[")
            append("],\"characters\":[")
            append(characters.sortedBy { it.externalId }.joinToString(",") { character ->
                buildString {
                    append("{\"ability\":${quoted(character.abilityText)}")
                    append(",\"externalId\":${quoted(character.externalId)}")
                    append(",\"firstNight\":${canonicalNumber(character.firstNightOrder)}")
                    append(",\"firstNightReminder\":${quoted(character.firstNightReminder)}")
                    append(",\"globalReminders\":[${character.globalReminders.joinToString(",") { quoted(it) }}]")
                    append(",\"id\":${quoted(character.id.value)}")
                    append(",\"jinxes\":[]")
                    append(",\"otherNight\":${canonicalNumber(character.otherNightOrder)}")
                    append(",\"otherNightReminder\":${quoted(character.otherNightReminder)}")
                    append(",\"reminders\":[${character.reminders.joinToString(",") { quoted(it) }}]")
                    append(",\"semanticSourceHash\":${quoted("")}")
                    append(",\"setup\":${character.setup}")
                    append(",\"special\":[]")
                    append(",\"team\":${quoted(character.team.name)}")
                    append('}')
                }
            })
            append("],\"firstNightOverride\":")
            append(canonicalOrder(firstNightOverride))
            append(",\"otherNightOverride\":")
            append(canonicalOrder(otherNightOverride))
            append('}')
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .take(16)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    private fun canonicalNumber(value: java.math.BigDecimal): String =
        value.stripTrailingZeros().toPlainString()

    private fun canonicalOrder(tokens: List<NightOrderToken>?): String {
        if (tokens == null) return "null"
        return tokens.joinToString(prefix = "[", postfix = "]") { token ->
            quoted(
                when (token) {
                    is NightOrderToken.Character -> "character:${token.roleId.value}"
                    NightOrderToken.System.DUSK -> "system:dusk"
                    NightOrderToken.System.MINION_INFO -> "system:minioninfo"
                    NightOrderToken.System.DEMON_INFO -> "system:demoninfo"
                    NightOrderToken.System.DAWN -> "system:dawn"
                },
            )
        }
    }

    private fun quoted(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u%04x".format(character.code))
                } else {
                    append(character)
                }
            }
        }
        append('"')
    }
}
