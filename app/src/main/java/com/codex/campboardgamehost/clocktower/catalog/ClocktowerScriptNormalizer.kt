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
        val resolved = parsed.characters.map { entry ->
            when (entry) {
                is ImportedClocktowerCharacter.OfficialReference ->
                    registry.findByExternalId(entry.externalId)
                        ?: throw IllegalArgumentException("Unknown official Clocktower character id '${entry.externalId}'.")

                is ImportedClocktowerCharacter.CustomDefinition -> customDefinition(entry, registry)
            }
        }
        require(resolved.map { it.id }.distinct().size == resolved.size) {
            "Clocktower script contains duplicate character IDs."
        }
        require(resolved.map { it.externalId }.distinct().size == resolved.size) {
            "Clocktower script contains duplicate external character IDs."
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
        require(registry.findByExternalId(imported.externalId) == null) {
            "Custom character id '${imported.externalId}' collides with a known official character; use an official ID reference instead."
        }
        val team = when (imported.team) {
            "townsfolk" -> ClocktowerCatalogTeam.TOWNSFOLK
            "outsider" -> ClocktowerCatalogTeam.OUTSIDER
            "minion" -> ClocktowerCatalogTeam.MINION
            "demon" -> ClocktowerCatalogTeam.DEMON
            "traveller" -> ClocktowerCatalogTeam.TRAVELLER
            "fabled" -> ClocktowerCatalogTeam.FABLED
            "loric" -> ClocktowerCatalogTeam.LORIC
            else -> throw IllegalArgumentException("Unknown Clocktower character team '${imported.team}'.")
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
            setup = imported.setup,
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
        require(rawTokens.distinct().size == rawTokens.size) { "Clocktower night-order tokens must be unique." }
        return rawTokens.map { rawToken ->
            require(rawToken == rawToken.lowercase(Locale.ROOT)) {
                "Clocktower night-order tokens must use lowercase official IDs."
            }
            when (rawToken) {
                "dusk" -> NightOrderToken.System.DUSK
                "dawn" -> NightOrderToken.System.DAWN
                "minioninfo" -> {
                    require(firstNight) { "minioninfo is valid only in firstNight metadata." }
                    NightOrderToken.System.MINION_INFO
                }
                "demoninfo" -> {
                    require(firstNight) { "demoninfo is valid only in firstNight metadata." }
                    NightOrderToken.System.DEMON_INFO
                }
                else -> {
                    val character = charactersByExternalId[rawToken]
                        ?: throw IllegalArgumentException("Unknown or off-script night-order token '$rawToken'.")
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
                    append(",\"firstNight\":${character.firstNightOrder}")
                    append(",\"firstNightReminder\":${quoted(character.firstNightReminder)}")
                    append(",\"id\":${quoted(character.id.value)}")
                    append(",\"otherNight\":${character.otherNightOrder}")
                    append(",\"otherNightReminder\":${quoted(character.otherNightReminder)}")
                    append(",\"reminders\":[${character.reminders.sorted().joinToString(",") { quoted(it) }}]")
                    append(",\"semanticSourceHash\":${quoted(character.sourceSemanticHash ?: "")}")
                    append(",\"setup\":${character.setup}")
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
