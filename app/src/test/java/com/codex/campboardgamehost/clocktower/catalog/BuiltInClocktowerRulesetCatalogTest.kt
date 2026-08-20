package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuiltInClocktowerRulesetCatalogTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `shared catalog exposes canonical validated Trouble Brewing and No Greater Joy rulesets`() {
        val troubleBrewing = catalog.ruleset(ClocktowerScript.TroubleBrewing)
        val noGreaterJoy = catalog.ruleset(ClocktowerScript.NoGreaterJoy)

        assertEquals(ScriptId("trouble_brewing"), troubleBrewing.script.id)
        assertEquals(ClocktowerScriptSource.BUILTIN_OFFICIAL, troubleBrewing.script.source)
        assertEquals(RuleCoverage.PARTIAL, troubleBrewing.coverage)
        assertTrue(RoleId("Imp") in troubleBrewing.script.characterIds)
        assertTrue(RoleId("Fortune Teller") in troubleBrewing.script.characterIds)

        assertEquals(ScriptId("no_greater_joy"), noGreaterJoy.script.id)
        assertEquals(ClocktowerScriptSource.BUILTIN_OFFICIAL, noGreaterJoy.script.source)
        assertEquals(RuleCoverage.PARTIAL, noGreaterJoy.coverage)
        assertEquals(
            setOf(
                "Clockmaker", "Investigator", "Empath", "Chambermaid", "Artist", "Sage",
                "Drunk", "Klutz", "Baron", "Scarlet Woman", "Imp",
            ).map(::RoleId).toSet(),
            noGreaterJoy.script.characterIds.toSet(),
        )
    }

    @Test
    fun `shared catalog caches normalized rulesets instead of reparsing assets per request`() {
        assertSame(
            catalog.ruleset(ClocktowerScript.TroubleBrewing),
            catalog.ruleset(ClocktowerScript.TroubleBrewing),
        )
        assertSame(
            catalog.ruleset(ClocktowerScript.NoGreaterJoy),
            catalog.ruleset(ClocktowerScript.NoGreaterJoy),
        )
    }

    @Test
    fun `persistence coordinator consumes the shared catalog and owns no duplicate built-in loader`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/persistence/ActiveGamePersistenceCoordinator.kt",
        ).readText(Charsets.UTF_8)

        assertTrue(source.contains("BuiltInClocktowerRulesetCatalog.fromContext(context)"))
        assertTrue(source.contains("catalog.ruleset(script).script"))
        assertFalse(source.contains("private object BuiltInClocktowerPersistenceCatalog"))
        assertFalse(source.contains("context.assets.open(\"scripts/"))
    }
}
