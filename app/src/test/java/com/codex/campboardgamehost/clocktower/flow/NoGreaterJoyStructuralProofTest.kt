package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.NoGreaterJoyOfficialCharacterMetadata
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.math.BigDecimal

class NoGreaterJoyStructuralProofTest {

    private val baseRegistry by lazy {
        val knowledge = RulesetJsonLoader.parse(
            File("src/main/assets/rules/trouble_brewing.json").readText(Charsets.UTF_8),
        )
        LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = knowledge,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            coverage = RuleCoverage.PARTIAL,
        )
    }

    private val registry by lazy {
        NoGreaterJoyOfficialCharacterMetadata.extend(baseRegistry)
    }

    private val ruleset: ValidatedClocktowerRuleset by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/no_greater_joy.json").readText(Charsets.UTF_8),
            requestedScriptId = ScriptId("no_greater_joy"),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }

    @Test
    fun `canonical No Greater Joy asset uses the existing catalog and official night order`() {
        assertEquals(
            setOf(
                "Clockmaker", "Investigator", "Empath", "Chambermaid", "Artist", "Sage",
                "Drunk", "Klutz", "Baron", "Scarlet Woman", "Imp",
            ).map(::RoleId).toSet(),
            ruleset.script.characterIds.toSet(),
        )
        assertEquals(
            listOf("Investigator", "Empath", "Clockmaker", "Chambermaid").map(::RoleId),
            requireNotNull(ruleset.script.firstNightOverride).characterRoleIds(),
        )
        assertEquals(
            listOf("Scarlet Woman", "Imp", "Sage", "Empath", "Chambermaid").map(::RoleId),
            requireNotNull(ruleset.script.otherNightOverride).characterRoleIds(),
        )
        assertEquals(RuleCoverage.PARTIAL, ruleset.coverage)
        assertEquals(ClocktowerScriptSource.BUILTIN_OFFICIAL, ruleset.script.source)
    }

    @Test
    fun `NGJ-only official metadata binds behavior without inventing night actions for day-only roles`() {
        val expectedBehaviorKeys = mapOf(
            "Clockmaker" to "clockmaker",
            "Chambermaid" to "chambermaid",
            "Artist" to "artist",
            "Sage" to "sage",
            "Klutz" to "klutz",
        )

        expectedBehaviorKeys.forEach { (roleName, behaviorKey) ->
            val definition = requireNotNull(registry.findByRoleId(RoleId(roleName)))
            assertEquals(behaviorKey, definition.behaviorKey)
        }

        listOf("Artist", "Klutz").forEach { roleName ->
            val definition = requireNotNull(registry.findByRoleId(RoleId(roleName)))
            assertEquals(BigDecimal.ZERO, definition.firstNightOrder)
            assertEquals(BigDecimal.ZERO, definition.otherNightOrder)
        }
    }

    private fun List<NightOrderToken>.characterRoleIds(): List<RoleId> =
        filterIsInstance<NightOrderToken.Character>().map { it.roleId }
}
