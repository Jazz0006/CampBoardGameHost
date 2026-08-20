package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import java.math.BigDecimal

/**
 * Official character metadata needed by No Greater Joy that is not already present in Trouble Brewing.
 *
 * This extends the existing central ClocktowerCharacterRegistry; it is not a second script registry or
 * a rules DSL. Complex behavior remains bound in Kotlin by role/behavior handlers.
 */
internal object NoGreaterJoyOfficialCharacterMetadata {
    val definitions: List<ClocktowerCharacterDefinition> = listOf(
        ClocktowerCharacterDefinition(
            id = RoleId("Clockmaker"),
            externalId = "clockmaker",
            name = "Clockmaker",
            team = ClocktowerCatalogTeam.TOWNSFOLK,
            abilityText = "You start knowing how many steps from the Demon to its nearest Minion.",
            firstNightOrder = BigDecimal("2.5"),
            behaviorKey = "clockmaker",
            automationCoverage = RuleCoverage.PARTIAL,
        ),
        ClocktowerCharacterDefinition(
            id = RoleId("Chambermaid"),
            externalId = "chambermaid",
            name = "Chambermaid",
            team = ClocktowerCatalogTeam.TOWNSFOLK,
            abilityText = "Each night, choose 2 alive players (not yourself): you learn how many woke tonight due to their ability.",
            firstNightOrder = BigDecimal("10"),
            otherNightOrder = BigDecimal("12"),
            behaviorKey = "chambermaid",
            automationCoverage = RuleCoverage.PARTIAL,
        ),
        ClocktowerCharacterDefinition(
            id = RoleId("Artist"),
            externalId = "artist",
            name = "Artist",
            team = ClocktowerCatalogTeam.TOWNSFOLK,
            abilityText = "Once per game, during the day, privately ask the Storyteller any yes/no question.",
            behaviorKey = "artist",
            automationCoverage = RuleCoverage.PARTIAL,
        ),
        ClocktowerCharacterDefinition(
            id = RoleId("Sage"),
            externalId = "sage",
            name = "Sage",
            team = ClocktowerCatalogTeam.TOWNSFOLK,
            abilityText = "If the Demon kills you, you learn that it is 1 of 2 players.",
            otherNightOrder = BigDecimal("6.5"),
            behaviorKey = "sage",
            automationCoverage = RuleCoverage.PARTIAL,
        ),
        ClocktowerCharacterDefinition(
            id = RoleId("Klutz"),
            externalId = "klutz",
            name = "Klutz",
            team = ClocktowerCatalogTeam.OUTSIDER,
            abilityText = "When you learn that you died, publicly choose 1 alive player: if they are evil, your team loses.",
            behaviorKey = "klutz",
            automationCoverage = RuleCoverage.PARTIAL,
        ),
    )

    fun extend(base: ClocktowerCharacterRegistry): ClocktowerCharacterRegistry {
        definitions.forEach { definition ->
            require(base.findByRoleId(definition.id) == null) {
                "No Greater Joy extension character '${definition.id.value}' already exists in the base registry."
            }
            require(base.findByExternalId(definition.externalId) == null) {
                "No Greater Joy extension external id '${definition.externalId}' already exists in the base registry."
            }
        }
        return ClocktowerCharacterRegistry(base.definitions + definitions)
    }
}
