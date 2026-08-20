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

    @Test
    fun `custom character schema rejects unknown fields and invalid special metadata`() {
        assertEquals(
            ClocktowerScriptValidationCode.INVALID_FIELD,
            failureCode(
                homebrewJson("alpha").replace(
                    "\"setup\":false,",
                    "\"setup\":false,\"unexpected\":true,",
                ),
            ),
        )
        assertEquals(
            ClocktowerScriptValidationCode.INVALID_FIELD,
            failureCode(
                homebrewJson("alpha").replace("\"name\":\"card\"", "\"name\":\"invented-feature\""),
            ),
        )
    }

    @Test
    fun `homebrew metadata is preserved as unverified without interpreting its behavior`() {
        val firstNormalized = normalize(homebrewJson("alpha"))
        val secondNormalized = normalize(homebrewJson("beta"))
        val custom = firstNormalized.characters.single { it.externalId == "customseer" }

        assertEquals(RoleId("homebrew:customseer"), custom.id)
        assertEquals(BigDecimal("1.5"), custom.firstNightOrder)
        assertEquals(listOf("Knows"), custom.globalReminders)
        assertEquals(RoleId("Imp"), custom.jinxes.single().targetRoleId)
        assertEquals("Custom interaction.", custom.jinxes.single().reason)
        assertEquals("signal", custom.specialFeatures.single().type)
        assertEquals("card", custom.specialFeatures.single().name)
        assertEquals(ClocktowerSpecialValueKind.TEXT, custom.specialFeatures.single().value?.kind)
        assertEquals("alpha", custom.specialFeatures.single().value?.canonicalValue)
        assertEquals(RuleCoverage.UNVERIFIED, custom.automationCoverage)
        assertEquals(RuleCoverage.UNVERIFIED, firstNormalized.coverage)
        assertEquals(ClocktowerScriptSource.IMPORTED_HOMEBREW, firstNormalized.script.source)
        assertNotEquals(firstNormalized.script.contentHash, secondNormalized.script.contentHash)
    }

    @Test
    fun `equivalent decimal encodings produce the same normalized identity`() {
        val first = normalize(numericHomebrewJson("1.50", "3.0"))
        val second = normalize(numericHomebrewJson("1.5", "3"))

        assertEquals(first.script.contentHash, second.script.contentHash)
        assertEquals(BigDecimal("1.5"), first.characters.single { it.externalId == "customseer" }.firstNightOrder)
        assertEquals("3", first.characters.single { it.externalId == "customseer" }.specialFeatures.single().value?.canonicalValue)
    }

    @Test
    fun `off script homebrew jinx target fails closed`() {
        val invalid = homebrewJson("alpha").replace("\"id\":\"imp\",\"reason\":\"Custom interaction.\"", "\"id\":\"spy\",\"reason\":\"Custom interaction.\"")

        assertEquals(ClocktowerScriptValidationCode.INVALID_JINX_TARGET, failureCode(invalid))
    }

    @Test
    fun `bootlegger rule downgrades otherwise known official script to unverified`() {
        val normalized = normalize(
            """[{"id":"_meta","name":"Bootleg","bootlegger":["Imp attacks twice."]},"imp","chef","empath","poisoner"]""",
        )

        assertEquals(RuleCoverage.UNVERIFIED, normalized.coverage)
        assertEquals(ClocktowerScriptSource.IMPORTED_HOMEBREW, normalized.script.source)
    }

    @Test
    fun `script catalog rejects duplicate script identities`() {
        val script = normalize("""["imp","chef","empath","poisoner","spy"]""").script
        val failure = runCatching { ClocktowerScriptCatalog(listOf(script, script)) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private fun failureCode(json: String): ClocktowerScriptValidationCode? =
        (runCatching { normalize(json) }.exceptionOrNull() as? ClocktowerScriptValidationException)?.code

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
          "chef",
          "empath",
          {
            "id":"customseer",
            "name":"Custom Seer",
            "team":"townsfolk",
            "ability":"You learn something.",
            "firstNight":1.5,
            "firstNightReminder":"Give information.",
            "otherNight":0,
            "reminders":["Seen"],
            "remindersGlobal":["Knows"],
            "setup":false,
            "jinxes":[{"id":"imp","reason":"Custom interaction."}],
            "special":[{"type":"signal","name":"card","value":"$specialValue","time":"firstNight"}]
          }
        ]
    """.trimIndent()

    private fun numericHomebrewJson(priority: String, specialValue: String): String = """
        [
          {"id":"_meta","name":"Homebrew"},
          "imp",
          "chef",
          "empath",
          {
            "id":"customseer",
            "name":"Custom Seer",
            "team":"townsfolk",
            "ability":"You learn something.",
            "firstNight":$priority,
            "otherNight":0,
            "setup":false,
            "special":[{"type":"vote","name":"multiplier","value":$specialValue}]
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
