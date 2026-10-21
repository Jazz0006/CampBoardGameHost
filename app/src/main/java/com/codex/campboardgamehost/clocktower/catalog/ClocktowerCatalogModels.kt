package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import java.math.BigDecimal

internal enum class ClocktowerCatalogTeam {
    TOWNSFOLK,
    OUTSIDER,
    MINION,
    DEMON,
    TRAVELLER,
    FABLED,
    LORIC,
}

internal enum class ClocktowerScriptSource {
    BUILTIN_OFFICIAL,
}

internal sealed interface NightOrderToken {
    data class Character(val roleId: RoleId) : NightOrderToken

    enum class System : NightOrderToken {
        DUSK,
        MINION_INFO,
        DEMON_INFO,
        DAWN,
    }
}

internal data class ClocktowerCharacterDefinition(
    val id: RoleId,
    val externalId: String,
    val name: String,
    val team: ClocktowerCatalogTeam,
    val abilityText: String,
    val firstNightOrder: BigDecimal = BigDecimal.ZERO,
    val otherNightOrder: BigDecimal = BigDecimal.ZERO,
    val firstNightReminder: String = "",
    val otherNightReminder: String = "",
    val reminders: List<String> = emptyList(),
    val globalReminders: List<String> = emptyList(),
    val setup: Boolean = false,
    val behaviorKey: String? = null,
    val automationCoverage: RuleCoverage,
) {
    init {
        require(EXTERNAL_ID_PATTERN.matches(externalId)) {
            "Clocktower external character id must be lowercase alphanumeric."
        }
        require(name.isNotBlank()) { "Clocktower character name cannot be blank." }
        require(abilityText.isNotBlank()) { "Clocktower character ability text cannot be blank." }
        require(firstNightOrder.signum() >= 0) { "firstNightOrder cannot be negative." }
        require(otherNightOrder.signum() >= 0) { "otherNightOrder cannot be negative." }
        require(behaviorKey == null || behaviorKey.isNotBlank()) { "behaviorKey cannot be blank." }
    }

    private companion object {
        val EXTERNAL_ID_PATTERN = Regex("[a-z0-9]{1,50}")
    }
}

internal data class ClocktowerScriptDefinition(
    val id: ScriptId,
    val name: String,
    val author: String?,
    val characterIds: List<RoleId>,
    val firstNightOverride: List<NightOrderToken>?,
    val otherNightOverride: List<NightOrderToken>?,
    val source: ClocktowerScriptSource,
    val contentHash: String,
) {
    init {
        require(name.isNotBlank()) { "Clocktower script name cannot be blank." }
        require(author == null || author.isNotBlank()) { "Clocktower script author cannot be blank." }
        require(characterIds.isNotEmpty()) { "Clocktower script must contain at least one character." }
        require(characterIds.distinct().size == characterIds.size) {
            "Clocktower script character IDs must be unique."
        }
        require(CONTENT_HASH_PATTERN.matches(contentHash)) {
            "Clocktower script contentHash must be a 128-bit lowercase hexadecimal SHA-256 prefix."
        }
    }

    private companion object {
        val CONTENT_HASH_PATTERN = Regex("[0-9a-f]{32}")
    }
}

internal class ClocktowerCharacterRegistry(definitions: Iterable<ClocktowerCharacterDefinition>) {
    val definitions: List<ClocktowerCharacterDefinition> = definitions.toList()
    private val byRoleId: Map<RoleId, ClocktowerCharacterDefinition>
    private val byExternalId: Map<String, ClocktowerCharacterDefinition>

    init {
        require(this.definitions.isNotEmpty()) { "Clocktower character registry cannot be empty." }
        require(this.definitions.map { it.id }.distinct().size == this.definitions.size) {
            "Clocktower character registry RoleIds must be unique."
        }
        require(this.definitions.map { it.externalId }.distinct().size == this.definitions.size) {
            "Clocktower character registry external IDs must be unique."
        }
        byRoleId = this.definitions.associateBy { it.id }
        byExternalId = this.definitions.associateBy { it.externalId }
    }

    fun findByRoleId(roleId: RoleId): ClocktowerCharacterDefinition? = byRoleId[roleId]

    fun findByExternalId(externalId: String): ClocktowerCharacterDefinition? = byExternalId[externalId]
}

internal data class ValidatedClocktowerRuleset(
    val script: ClocktowerScriptDefinition,
    val characters: List<ClocktowerCharacterDefinition>,
    val coverage: RuleCoverage,
) {
    val characterRegistry = ClocktowerCharacterRegistry(characters)

    init {
        require(characters.map { it.id }.toSet() == script.characterIds.toSet()) {
            "Validated ruleset characters must exactly match the script composition."
        }
    }

    fun toRulesetRef(rulesetVersion: String, sourceRevision: String): RulesetRef = RulesetRef(
        scriptId = script.id,
        scriptContentHash = script.contentHash,
        rulesetVersion = rulesetVersion,
        sourceRevision = sourceRevision,
        coverage = coverage,
    )
}
