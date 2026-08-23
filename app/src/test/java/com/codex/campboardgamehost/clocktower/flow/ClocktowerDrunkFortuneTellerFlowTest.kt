package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDrunkFortuneTellerFlowTest {
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
    fun `Drunk shown Fortune Teller wakes without creating the real Fortune Teller setup ability`() {
        val fortuneTeller = RoleId("Fortune Teller")
        val drunk = RoleId("Drunk")
        val wakingRoleIds = setOf(drunk, fortuneTeller)
        val actualRoleIds = setOf(drunk)
        val basePlan = ClocktowerFlowPlanner().planNight(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            context = ClocktowerFlowContext(
                playerCount = 5,
                inPlayRoleIds = wakingRoleIds,
            ),
        )

        val interactions = ClocktowerHostInteractionProjector().projectNight(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            basePlan = basePlan,
            actualRoleIds = actualRoleIds,
        )

        assertTrue(interactions.any {
            it.id == ClocktowerInteractionId("first_night:role:Fortune Teller")
        })
        assertFalse(interactions.any {
            it.id == ClocktowerInteractionId("first_night:fortune_teller:red_herring")
        })
    }

    @Test
    fun `actual Fortune Teller still creates red herring before the role interaction`() {
        val fortuneTeller = RoleId("Fortune Teller")
        val basePlan = ClocktowerFlowPlanner().planNight(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            context = ClocktowerFlowContext(
                playerCount = 5,
                inPlayRoleIds = setOf(fortuneTeller),
            ),
        )

        val interactions = ClocktowerHostInteractionProjector().projectNight(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            basePlan = basePlan,
            actualRoleIds = setOf(fortuneTeller),
        )
        val relevant = interactions.map { it.id.value }.filter {
            it == "first_night:fortune_teller:red_herring" ||
                it == "first_night:role:Fortune Teller"
        }

        assertEquals(
            listOf(
                "first_night:fortune_teller:red_herring",
                "first_night:role:Fortune Teller",
            ),
            relevant,
        )
    }

    @Test
    fun `production first-night seam carries actual roles separately from waking roles`() {
        val fortuneTeller = RoleId("Fortune Teller")
        val drunk = RoleId("Drunk")
        val ordered = ClocktowerProductionFirstNightFlow.order(
            ruleset = ruleset,
            playerCount = 5,
            inPlayRoleIds = setOf(drunk, fortuneTeller),
            actualRoleIds = setOf(drunk),
            productionSteps = listOf("drunk-as-fortune-teller"),
            identityOf = { ClocktowerProductionNightStepIdentity.role(fortuneTeller) },
        )

        assertEquals(listOf("drunk-as-fortune-teller"), ordered)
    }
}
