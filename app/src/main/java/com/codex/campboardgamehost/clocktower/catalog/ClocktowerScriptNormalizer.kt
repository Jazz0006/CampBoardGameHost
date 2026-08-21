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
        val duplicateExternalId = parsed.characters
            .groupBy { it.externalId }
            .entries
            .firstOrNull { it.value.size > 1 }
            ?.key
        clocktowerValidate(duplicateExternalId == null, ClocktowerScriptValidationCode.DUPLICATE_CHARACTER_ID, {
            "Clocktower script contains duplicate character id '$duplicateExternalId'."
        })

        val preliminary = parsed.characters.map { entry ->
            when (entry) {
                is ImportedClocktowerCharacter.OfficialReference ->
                    registry.findByExternalId(entry.externalId)
                        ?: clocktowerValidationFailure(
                            ClocktowerScriptValidationCode.UNKNOWN_CHARACTER_ID,
                            "Unknown official Clocktower character id '${entry.externalId}'.",
                        )

                is ImportedClocktowerCharacter.CustomDefinition -> customDefinition(entry, registry)
            }
        }
        clocktowerValidate(
            preliminary.map { it.id }.distinct().size == preliminary.size,
            ClocktowerScriptValidationCode.DUPLICATE_CHARACTER_ID,
            { "Clocktower script contains duplicate character RoleIds." },
        )

        val byExternalId = preliminary.associateBy { it.externalId }
        val resolved = preliminary.zip(parsed.characters).map { (definition, imported) ->
            if (imported !is ImportedClocktowerCharacter.CustomDefinition) return@map definition
            definition.copy(
                jinxes = imported.jinxes.map { jinx ->
                    val target = byExternalId[jinx.externalId]
                        ?: clocktowerValidationFailure(
                            ClocktowerScriptValidationCode.INVALID_JINX_TARGET,
                            "Clocktower jinx target '${jinx.externalId}' is not present on this script.",
                        )
                    ClocktowerJinxDefinition(target.id, jinx.reason)
                },
            )
        }

        val scriptCharactersByExternalId = resolved.associateBy { it.externalId }
        val firstNightOverride = parsed.firstNightOverride?.let { raw ->
            normalizeNightOrder(raw, scriptCharactersByExternalId, firstNight = true)
        }
        val otherNightOverride = parsed.otherNightOverride?.let { raw ->
            normalizeNightOrder(raw, scriptCharactersByExternalId, firstNight = false)
        }
        val hasHomebrewContent = parsed.characters.any { it is ImportedClocktowerCharacter.CustomDefinition } ||
            parsed.bootleggerRules.isNotEmpty()
        val source = if (hasHomebrewContent) ClocktowerScriptSource.IMPORTED_HOMEBREW else requestedSource
        val coverage = if (parsed.bootleggerRules.isNotEmpty()) {
            RuleCoverage.UNVERIFIED
        } else {
            aggregateCoverage(resolved)
        }
        val contentHash = ClocktowerCatalogContentHasher.hash(
            characters = resolved,
            firstNightOverride = firstNightOverride,
            otherNightOverride = otherNightOverride,
            bootleggerRules = parsed.bootleggerRules,
        )
        val script = ClocktowerScriptDefinition(
            id = requestedScriptId,
            name = parsed.name ?: requestedScriptId.value,
            author = parsed.author,
            characterIds = resolved.map { it.id },
            firstNightOverride = firstNightOverride,
            otherNightOverride = otherNightOverride,
            bootleggerRules = parsed.bootleggerRules,
            source = source,
            contentHash = contentHash,
        )
        return ValidatedClocktowerRuleset(script, resolved, coverage)
    }

    private fun customDefinition(
        imported: ImportedClocktowerCharacter.CustomDefinition,
        registry: ClocktowerCharacterRegistry,
    ): ClocktowerCharacterDefinition {
        clocktowerValidate(
            registry.findByExternalId(imported.externalId) == null,
            ClocktowerScriptValidationCode.CUSTOM_ID_COLLISION,
            {
                "Custom character id '${imported.externalId}' collides with a known official character; " +
                    "use an official ID reference instead."
            },
        )
        val team = when (imported.team) {
            "townsfolk" -> ClocktowerCatalogTeam.TOWNSFOLK
            "outsider" -> ClocktowerCatalogTeam.OUTSIDER
            "minion" -> ClocktowerCatalogTeam.MINION
            "demon" -> ClocktowerCatalogTeam.DEMON
            "traveller" -> ClocktowerCatalogTeam.TRAVELLER
            "fabled" -> ClocktowerCatalogTeam.FABLED
            "loric" -> ClocktowerCatalogTeam.LORIC
            else -> clocktowerValidationFailure(
                ClocktowerScriptValidationCode.UNKNOWN_CHARACTER_TEAM,
                "Unknown Clocktower character team '${imported.team}'.",
            )
        }
        return ClocktowerCharacterDefinition(
            id = RoleId("homebrew:${imported.externalId}"),
            externalId = imported.externalId,
            name = imported.name,
            team = team,
            abilityText = imported.abilityText,
            firstNightOrder = imported.firstNightOrder,
            otherNightOrder = imported.otherNightOrder,
            firstNightReminder = imported.firstNightReminder,
            otherNightReminder = imported.otherNightReminder,
            reminders = imported.reminders,
            globalReminders = imported.globalReminders,
            setup = imported.setup,
            jinxes = emptyList(),
            specialFeatures = imported.specialFeatures.map { feature ->
                ClocktowerSpecialFeature(
                    type = feature.type,
                    name = feature.name,
                    value = feature.value,
                    time = feature.time,
                    global = feature.global,
                )
            },
            behaviorKey = null,
            automationCoverage = RuleCoverage.UNVERIFIED,
            sourceSemanticHash = imported.sourceSemanticHash,
        )
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
        bootleggerRules: List<String>,
    ): String {
        val canonical = buildString {
            append("{\"bootlegger\":[")
            append(bootleggerRules.sorted().joinToString(",") { quoted(it) })
            append("],\"characters\":[")
            append(characters.sortedBy { it.externalId }.joinToString(",") { character ->
                buildString {
                    append("{\"ability\":${quoted(character.abilityText)}")
                    append(",\"externalId\":${quoted(character.externalId)}")
                    append(",\"firstNight\":${canonicalNumber(character.firstNightOrder)}")
                    append(",\"firstNightReminder\":${quoted(character.firstNightReminder)}")
                    append(",\"globalReminders\":[${character.globalReminders.joinToString(",") { quoted(it) }}]")
                    append(",\"id\":${quoted(character.id.value)}")
                    append(",\"jinxes\":[${character.jinxes.sortedBy { it.targetRoleId.value }.joinToString(",") { jinx ->
                        "{\"reason\":${quoted(jinx.reason)},\"target\":${quoted(jinx.targetRoleId.value)}}"
                    }}]")
                    append(",\"otherNight\":${canonicalNumber(character.otherNightOrder)}")
                    append(",\"otherNightReminder\":${quoted(character.otherNightReminder)}")
                    append(",\"reminders\":[${character.reminders.joinToString(",") { quoted(it) }}]")
                    append(",\"semanticSourceHash\":${quoted(character.sourceSemanticHash ?: "")}")
                    append(",\"setup\":${character.setup}")
                    append(",\"special\":[${character.specialFeatures.sortedWith(compareBy<ClocktowerSpecialFeature> { it.type }.thenBy { it.name }.thenBy { it.time ?: "" }.thenBy { it.global ?: "" }.thenBy { it.value?.canonicalValue ?: "" }).joinToString(",") { feature ->
                        "{\"global\":${quoted(feature.global ?: "")},\"name\":${quoted(feature.name)}," +
                            "\"time\":${quoted(feature.time ?: "")},\"type\":${quoted(feature.type)}," +
                            "\"valueKind\":${quoted(feature.value?.kind?.name ?: "")}," +
                            "\"value\":${quoted(feature.value?.canonicalValue ?: "")}}"
                    }}]")
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
