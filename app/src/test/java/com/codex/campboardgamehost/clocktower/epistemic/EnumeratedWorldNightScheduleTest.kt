package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumeratedWorldNightScheduleTest {
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

    private val ruleset by lazy {
        RulesetJsonLoader.parseScript(
            json = File("src/main/assets/scripts/trouble_brewing.json").readText(Charsets.UTF_8),
            requestedScriptId = ScriptId("trouble_brewing"),
            registry = registry,
            source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        )
    }

    @Test
    fun `possible world uses canonical other-night order without actual action history`() {
        val world = world(
            "Empath",
            "Chef",
            "Monk",
            "Poisoner",
            "Imp",
        )

        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.Character(RoleId("Poisoner")),
                NightOrderToken.Character(RoleId("Monk")),
                NightOrderToken.Character(RoleId("Imp")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.System.DAWN,
            ),
            EnumeratedWorldNightSchedule.plan(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
            ),
        )
    }

    @Test
    fun `different possible world omits absent hidden role while preserving remaining order`() {
        val withoutMonk = world(
            "Empath",
            "Chef",
            "Soldier",
            "Poisoner",
            "Imp",
        )

        val plan = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            world = withoutMonk,
        )

        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.Character(RoleId("Poisoner")),
                NightOrderToken.Character(RoleId("Imp")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.System.DAWN,
            ),
            plan,
        )
        assertFalse(NightOrderToken.Character(RoleId("Monk")) in plan)
    }

    @Test
    fun `Drunk shown role participates in waking schedule without replacing actual identity`() {
        val drunkShownFortuneTeller = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Drunk"),
                2 to RoleId("Chef"),
                3 to RoleId("Empath"),
                4 to RoleId("Poisoner"),
                5 to RoleId("Imp"),
            ),
            shownRolesBySeat = mapOf(1 to RoleId("Fortune Teller")),
        )

        val plan = EnumeratedWorldNightSchedule.plan(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            world = drunkShownFortuneTeller,
        )

        assertTrue(NightOrderToken.Character(RoleId("Fortune Teller")) in plan)
        assertFalse(NightOrderToken.Character(RoleId("Drunk")) in plan)
        assertEquals(RoleId("Drunk"), drunkShownFortuneTeller.rolesBySeat.getValue(1))
    }

    private fun world(vararg roles: String): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = roles.mapIndexed { index, role -> index + 1 to RoleId(role) }.toMap(),
    )
}
