package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.math.BigDecimal

class ClocktowerScriptCatalogTest {
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

    @Test
    fun `official style Trouble Brewing normalizes to the legacy composition and night metadata`() {
        val normalized = RulesetJsonLoader.parseScript(
            json = troubleBrewingOfficialStyleJson(),
            requestedScriptId = ScriptId("trouble_brewing"),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )

        assertEquals(legacyKnowledge.characters.map { it.roleId }.toSet(), normalized.script.characterIds.toSet())
        assertEquals(legacyKnowledge.firstNightOrder, normalized.script.firstNightOverride.characterRoleIds())
        assertEquals(legacyKnowledge.otherNightOrder, normalized.script.otherNightOverride.characterRoleIds())
        assertEquals(RuleCoverage.PARTIAL, normalized.coverage)
        assertEquals(ClocktowerScriptSource.BUILTIN_OFFICIAL, normalized.script.source)
        assertTrue(normalized.script.contentHash.matches(Regex("[0-9a-f]{32}")))

        val ref = normalized.toRulesetRef("catalog-v1", "s0-fixture")
        assertEquals(ScriptId("trouble_brewing"), ref.scriptId)
        assertEquals(normalized.script.contentHash, ref.scriptContentHash)
        assertEquals(RuleCoverage.PARTIAL, ref.coverage)
    }

    @Test
    fun `whitespace metadata key order display metadata and character order do not change normalized hash`() {
        val first = """
            [
              {"id":"_meta","name":"Display A","author":"Author A","firstNight":["dusk","poisoner","dawn"]},
              "imp", "chef", "poisoner", "empath"
            ]
        """.trimIndent()
        val second = """[{"firstNight":["dusk","poisoner","dawn"],"author":"Author B","name":"Display B","id":"_meta"},"poisoner","empath","imp","chef"]"""

        val firstNormalized = normalize(first)
        val secondNormalized = normalize(second)

        assertEquals(firstNormalized.script.contentHash, secondNormalized.script.contentHash)
        assertNotEquals(firstNormalized.script.name, secondNormalized.script.name)
        assertNotEquals(firstNormalized.script.author, secondNormalized.script.author)
    }

    @Test
    fun `typed validation rejects undersized unknown duplicate and invalid night tokens`() {
        assertEquals(
            ClocktowerScriptValidationCode.INVALID_SCRIPT_SIZE,
            failureCode("""["imp","chef","empath","poisoner"]"""),
        )
        assertEquals(
            ClocktowerScriptValidationCode.UNKNOWN_CHARACTER_ID,
            failureCode("""["imp","chef","empath","poisoner","notarealcharacter"]"""),
        )
        assertEquals(
            ClocktowerScriptValidationCode.DUPLICATE_CHARACTER_ID,
            failureCode("""["imp","imp","chef","empath","poisoner"]"""),
        )
        assertEquals(
            ClocktowerScriptValidationCode.INVALID_NIGHT_TOKEN,
            failureCode(
                """[{"id":"_meta","name":"Bad Night","firstNight":["dusk","notarealcharacter","dawn"]},"imp","chef","empath","poisoner"]""",
            ),
        )
        assertEquals(
            ClocktowerScriptValidationCode.INVALID_NIGHT_SYSTEM_TOKEN,
            failureCode(
                """[{"id":"_meta","name":"Bad Other Night","otherNight":["dusk","minioninfo","imp","dawn"]},"imp","chef","empath","poisoner"]""",
            ),
        )
    }

    private fun failureCode(json: String): ClocktowerScriptValidationCode? =
        (runCatching { normalize(json) }.exceptionOrNull() as? ClocktowerScriptValidationException)?.code

    private fun normalize(json: String): ValidatedClocktowerRuleset = RulesetJsonLoader.parseScript(
        json = json,
        requestedScriptId = ScriptId("test_script"),
        registry = registry,
        source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
    )

    private fun List<NightOrderToken>?.characterRoleIds(): List<RoleId> = requireNotNull(this)
        .mapNotNull { token -> (token as? NightOrderToken.Character)?.roleId }

    private fun troubleBrewingOfficialStyleJson(): String = """
        [
          {
            "id":"_meta",
            "name":"Trouble Brewing",
            "author":"The Pandemonium Institute",
            "firstNight":[
              "dusk","minioninfo","demoninfo","poisoner","spy","washerwoman","librarian",
              "investigator","chef","empath","fortuneteller","butler","dawn"
            ],
            "otherNight":[
              "dusk","poisoner","monk","spy","imp","scarletwoman","mayor","ravenkeeper",
              "undertaker","empath","fortuneteller","butler","dawn"
            ]
          },
          "washerwoman","librarian","investigator","chef","empath","fortuneteller",
          "undertaker","monk","ravenkeeper","virgin","slayer","soldier","mayor","butler",
          "drunk","recluse","saint","poisoner","spy","scarletwoman","baron","imp"
        ]
    """.trimIndent()
}
