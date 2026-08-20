package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerFlowPlannerTest {
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

    private val planner = ClocktowerFlowPlanner()

    @Test
    fun `canonical Trouble Brewing full-role shadow plan matches legacy night metadata`() {
        val allRoles = ruleset.script.characterIds.toSet()
        val context = ClocktowerFlowContext(playerCount = 7, inPlayRoleIds = allRoles)

        assertEquals(
            expectedFirstNight(includeEvilInfo = true),
            planner.planNight(ruleset, ClocktowerNightFlowPhase.FIRST_NIGHT, context),
        )
        assertEquals(
            expectedOtherNight(),
            planner.planNight(ruleset, ClocktowerNightFlowPhase.OTHER_NIGHT, context),
        )
    }

    @Test
    fun `five player first night omits evil info without a script-name branch`() {
        val allRoles = ruleset.script.characterIds.toSet()
        val context = ClocktowerFlowContext(playerCount = 5, inPlayRoleIds = allRoles)
        val plan = planner.planNight(ruleset, ClocktowerNightFlowPhase.FIRST_NIGHT, context)

        assertEquals(expectedFirstNight(includeEvilInfo = false), plan)
        assertTrue(NightOrderToken.System.MINION_INFO !in plan)
        assertTrue(NightOrderToken.System.DEMON_INFO !in plan)
    }

    @Test
    fun `planner filters base night plan to roles actually in play`() {
        val inPlay = setOf("Poisoner", "Imp", "Chef", "Empath", "Butler").map(::RoleId).toSet()
        val context = ClocktowerFlowContext(playerCount = 7, inPlayRoleIds = inPlay)

        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.System.MINION_INFO,
                NightOrderToken.System.DEMON_INFO,
                NightOrderToken.Character(RoleId("Poisoner")),
                NightOrderToken.Character(RoleId("Chef")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.Character(RoleId("Butler")),
                NightOrderToken.System.DAWN,
            ),
            planner.planNight(ruleset, ClocktowerNightFlowPhase.FIRST_NIGHT, context),
        )
        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.Character(RoleId("Poisoner")),
                NightOrderToken.Character(RoleId("Imp")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.Character(RoleId("Butler")),
                NightOrderToken.System.DAWN,
            ),
            planner.planNight(ruleset, ClocktowerNightFlowPhase.OTHER_NIGHT, context),
        )
    }

    @Test
    fun `registry priorities reproduce canonical night order when script overrides are absent`() {
        val withoutOverrides = ruleset.copy(
            script = ruleset.script.copy(
                firstNightOverride = null,
                otherNightOverride = null,
            ),
        )
        val context = ClocktowerFlowContext(
            playerCount = 7,
            inPlayRoleIds = ruleset.script.characterIds.toSet(),
        )

        assertEquals(
            planner.planNight(ruleset, ClocktowerNightFlowPhase.FIRST_NIGHT, context),
            planner.planNight(withoutOverrides, ClocktowerNightFlowPhase.FIRST_NIGHT, context),
        )
        assertEquals(
            planner.planNight(ruleset, ClocktowerNightFlowPhase.OTHER_NIGHT, context),
            planner.planNight(withoutOverrides, ClocktowerNightFlowPhase.OTHER_NIGHT, context),
        )
    }

    @Test
    fun `off-script in-play role fails closed`() {
        val failure = runCatching {
            planner.planNight(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
                context = ClocktowerFlowContext(
                    playerCount = 7,
                    inPlayRoleIds = setOf(RoleId("Imp"), RoleId("NotOnScript")),
                ),
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private fun expectedFirstNight(includeEvilInfo: Boolean): List<NightOrderToken> = buildList {
        add(NightOrderToken.System.DUSK)
        if (includeEvilInfo) {
            add(NightOrderToken.System.MINION_INFO)
            add(NightOrderToken.System.DEMON_INFO)
        }
        addAll(legacyKnowledge.firstNightOrder.map(NightOrderToken::Character))
        add(NightOrderToken.System.DAWN)
    }

    private fun expectedOtherNight(): List<NightOrderToken> = buildList {
        add(NightOrderToken.System.DUSK)
        addAll(legacyKnowledge.otherNightOrder.map(NightOrderToken::Character))
        add(NightOrderToken.System.DAWN)
    }
}
