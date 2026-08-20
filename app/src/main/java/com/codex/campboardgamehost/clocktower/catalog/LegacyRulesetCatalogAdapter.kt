package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.rules.RulesetKnowledge
import java.math.BigDecimal
import java.util.Locale

internal object LegacyRulesetCatalogAdapter {
    fun characterRegistry(
        knowledge: RulesetKnowledge,
        roleDefinitions: List<RoleDefinition>,
        coverage: RuleCoverage,
    ): ClocktowerCharacterRegistry {
        val definitionsById = roleDefinitions.associateBy { it.id }
        val firstNightPositions = knowledge.firstNightOrder.withIndex().associate { (index, roleId) ->
            roleId to BigDecimal.valueOf((index + 1).toLong())
        }
        val otherNightPositions = knowledge.otherNightOrder.withIndex().associate { (index, roleId) ->
            roleId to BigDecimal.valueOf((index + 1).toLong())
        }
        val characters = knowledge.characters.map { character ->
            val role = definitionsById[character.roleId]
                ?: throw IllegalArgumentException("Legacy ruleset role '${character.roleId.value}' has no RoleDefinition.")
            ClocktowerCharacterDefinition(
                id = character.roleId,
                externalId = externalIdFor(character.roleId),
                name = character.roleId.value,
                team = role.type.toCatalogTeam(),
                abilityText = character.abilityText,
                firstNightOrder = firstNightPositions[character.roleId] ?: BigDecimal.ZERO,
                otherNightOrder = otherNightPositions[character.roleId] ?: BigDecimal.ZERO,
                automationCoverage = coverage,
            )
        }
        return ClocktowerCharacterRegistry(characters)
    }

    private fun CharacterType.toCatalogTeam(): ClocktowerCatalogTeam = when (this) {
        CharacterType.TOWNSFOLK -> ClocktowerCatalogTeam.TOWNSFOLK
        CharacterType.OUTSIDER -> ClocktowerCatalogTeam.OUTSIDER
        CharacterType.MINION -> ClocktowerCatalogTeam.MINION
        CharacterType.DEMON -> ClocktowerCatalogTeam.DEMON
    }

    private fun externalIdFor(roleId: RoleId): String = roleId.value
        .lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }
        .also { externalId ->
            require(externalId.isNotBlank()) { "Legacy RoleId '${roleId.value}' cannot produce an external character ID." }
        }
}
