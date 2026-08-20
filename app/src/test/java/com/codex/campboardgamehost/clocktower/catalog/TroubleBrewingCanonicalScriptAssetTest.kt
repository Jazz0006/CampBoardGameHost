package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingCanonicalScriptAssetTest {
    private val legacyKnowledge by lazy {
        RulesetJsonLoader.parse(
            File("src/main/assets/rules/trouble_brewing.json").readText(Charsets.UTF_8),
        )
    }

    private val registry by lazy {
        LegacyRulesetCatalogAdapter.characterRegistry(
            knowledge = legacyKnowledge,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            coverage = RuleCoverage.PARTIAL,
        )
    }

    private val normalized by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/trouble_brewing.json").readText(Charsets.UTF_8),
            requestedScriptId = ScriptId("trouble_brewing"),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }

    @Test
    fun `canonical asset preserves legacy Trouble Brewing composition and role night order`() {
        assertEquals(
            legacyKnowledge.characters.map { it.roleId }.toSet(),
            normalized.script.characterIds.toSet(),
        )
        assertEquals(
            legacyKnowledge.firstNightOrder,
            requireNotNull(normalized.script.firstNightOverride).characterRoleIds(),
        )
        assertEquals(
            legacyKnowledge.otherNightOrder,
            requireNotNull(normalized.script.otherNightOverride).characterRoleIds(),
        )
        assertEquals(RuleCoverage.PARTIAL, normalized.coverage)
        assertEquals(ClocktowerScriptSource.BUILTIN_OFFICIAL, normalized.script.source)
    }

    @Test
    fun `canonical asset pins required system night tokens and stable ruleset identity shape`() {
        val firstNight = requireNotNull(normalized.script.firstNightOverride)
        val otherNight = requireNotNull(normalized.script.otherNightOverride)

        assertEquals(NightOrderToken.System.DUSK, firstNight.first())
        assertEquals(NightOrderToken.System.MINION_INFO, firstNight[1])
        assertEquals(NightOrderToken.System.DEMON_INFO, firstNight[2])
        assertEquals(NightOrderToken.System.DAWN, firstNight.last())
        assertEquals(NightOrderToken.System.DUSK, otherNight.first())
        assertEquals(NightOrderToken.System.DAWN, otherNight.last())
        assertTrue(NightOrderToken.System.MINION_INFO !in otherNight)
        assertTrue(NightOrderToken.System.DEMON_INFO !in otherNight)

        assertEquals(ScriptId("trouble_brewing"), normalized.script.id)
        assertEquals("Trouble Brewing", normalized.script.name)
        assertEquals("The Pandemonium Institute", normalized.script.author)
        assertTrue(normalized.script.contentHash.matches(Regex("[0-9a-f]{32}")))
    }

    private fun List<NightOrderToken>.characterRoleIds(): List<RoleId> =
        mapNotNull { token -> (token as? NightOrderToken.Character)?.roleId }
}
