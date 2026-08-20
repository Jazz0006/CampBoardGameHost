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
              "imp", "chef", "poisoner"
            ]
        """.trimIndent()
        val second = """[{"firstNight":["dusk","poisoner","dawn"],"author":"Author B","name":"Display B","id":"_meta"},"poisoner","imp","chef"]"""

        val firstNormalized = normalize(first)
        val secondNormalized = normalize(second)

        assertEquals(firstNormalized.script.contentHash, secondNormalized.script.contentHash)
        assertNotEquals(firstNormalized.script.name, secondNormalized.script.name)
        assertNotEquals(firstNormalized.script.author, secondNormalized.script.author)
    }

    @Test
    fun `unknown duplicate and invalid night tokens fail closed`() {
        val unknown = runCatching { normalize("""["imp","notarealcharacter"]""") }.exceptionOrNull()
        val duplicate = runCatching { normalize("""["imp","imp"]""") }.exceptionOrNull()
        val invalidNight = runCatching {
            normalize(
                """[{"id":"_meta","name":"Bad Night","firstNight":["dusk","notarealcharacter","dawn"]},"imp"]""",
            )
        }.exceptionOrNull()
        val invalidOtherNightSystemToken = runCatching {
            normalize(
                """[{"id":"_meta","name":"Bad Other Night","otherNight":["dusk","minioninfo","imp","dawn"]},"imp"]""",
            )
        }.exceptionOrNull()

        assertTrue(unknown is IllegalArgumentException)
        assertTrue(duplicate is IllegalArgumentException)
        assertTrue(invalidNight is IllegalArgumentException)
        assertTrue(invalidOtherNightSystemToken is IllegalArgumentException)
    }

    @Test
    fun `homebrew character stays unverified and opaque semantic metadata participates in identity`() {
        val first = homebrewJson("alpha")
        val second = homebrewJson("beta")

        val firstNormalized = normalize(first)
        val secondNormalized = normalize(second)
        val custom = firstNormalized.characters.single { it.externalId == "customseer" }

        assertEquals(RoleId("homebrew:customseer"), custom.id)
        assertEquals(RuleCoverage.UNVERIFIED, custom.automationCoverage)
        assertEquals(RuleCoverage.UNVERIFIED, firstNormalized.coverage)
        assertEquals(ClocktowerScriptSource.IMPORTED_HOMEBREW, firstNormalized.script.source)
        assertNotEquals(firstNormalized.script.contentHash, secondNormalized.script.contentHash)
    }

    @Test
    fun `bootlegger rule downgrades otherwise known official script to unverified`() {
        val normalized = normalize(
            """[{"id":"_meta","name":"Bootleg","bootlegger":["Imp attacks twice."]},"imp","chef"]""",
        )

        assertEquals(RuleCoverage.UNVERIFIED, normalized.coverage)
        assertEquals(ClocktowerScriptSource.IMPORTED_HOMEBREW, normalized.script.source)
    }

    @Test
    fun `script catalog rejects duplicate script identities`() {
        val script = normalize("""["imp","chef"]""").script
        val failure = runCatching { ClocktowerScriptCatalog(listOf(script, script)) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private fun normalize(json: String): ValidatedClocktowerRuleset = RulesetJsonLoader.parseScript(
        json = json,
        requestedScriptId = ScriptId("test_script"),
        registry = registry,
        source = ClocktowerScriptSource.IMPORTED_OFFICIAL,
    )

    private fun List<NightOrderToken>?.characterRoleIds(): List<RoleId> = requireNotNull(this)
        .mapNotNull { token -> (token as? NightOrderToken.Character)?.roleId }

    private fun homebrewJson(specialValue: String): String = """
        [
          {"id":"_meta","name":"Homebrew"},
          "imp",
          {
            "id":"customseer",
            "name":"Custom Seer",
            "team":"townsfolk",
            "ability":"You learn something.",
            "firstNight":1,
            "firstNightReminder":"Give information.",
            "otherNight":0,
            "reminders":["Seen"],
            "setup":false,
            "special":[{"type":"signal","name":"card","value":"$specialValue"}]
          }
        ]
    """.trimIndent()

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
