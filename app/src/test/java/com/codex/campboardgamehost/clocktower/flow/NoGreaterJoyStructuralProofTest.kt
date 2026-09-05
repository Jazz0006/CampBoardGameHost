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
    private val planner = ClocktowerFlowPlanner()
    private val projector = ClocktowerHostInteractionProjector()

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

    @Test
    fun `five-to-six player NGJ first night suppresses evil info and uses the same planner`() {
        val inPlay = setOf(
            "Investigator", "Empath", "Clockmaker", "Chambermaid", "Scarlet Woman", "Imp",
        ).map(::RoleId).toSet()

        val plan = planner.planNight(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            context = ClocktowerFlowContext(playerCount = 6, inPlayRoleIds = inPlay),
        )

        assertEquals(
            listOf(
                NightOrderToken.System.DUSK,
                NightOrderToken.Character(RoleId("Investigator")),
                NightOrderToken.Character(RoleId("Empath")),
                NightOrderToken.Character(RoleId("Clockmaker")),
                NightOrderToken.Character(RoleId("Chambermaid")),
                NightOrderToken.System.DAWN,
            ),
            plan,
        )
        assertFalse(NightOrderToken.System.MINION_INFO in plan)
        assertFalse(NightOrderToken.System.DEMON_INFO in plan)
    }

    @Test
    fun `promoted Scarlet Woman is represented by current Imp role plus explicit identity event`() {
        // Production changes the player's actual role from Scarlet Woman to Imp immediately.
        // The planner must therefore consume the current role set, not preserve a historical
        // Scarlet Woman token merely as an ordering anchor.
        val currentInPlay = setOf(
            "Imp", "Sage", "Empath", "Chambermaid", "Artist",
        ).map(::RoleId).toSet()
        val basePlan = planner.planNight(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            context = ClocktowerFlowContext(playerCount = 5, inPlayRoleIds = currentInPlay),
        )

        val quiet = projector.projectNight(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            basePlan = basePlan,
        )
        assertEquals(
            listOf(
                "other_night:role:Imp",
                "other_night:role:Empath",
                "other_night:role:Chambermaid",
            ),
            quiet.actionableIds(),
        )

        val triggered = projector.projectNight(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            basePlan = basePlan,
            resolvedFacts = ClocktowerResolvedFlowFacts(
                setOf(
                    ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON,
                    ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON,
                ),
            ),
        )
        assertEquals(
            listOf(
                "other_night:event:imp:new_demon_identity",
                "other_night:role:Imp",
                "other_night:role:Sage",
                "other_night:role:Empath",
                "other_night:role:Chambermaid",
            ),
            triggered.actionableIds(),
        )
    }

    @Test
    fun `NGJ day-only roles never leak into night flow`() {
        val inPlay = setOf("Artist", "Klutz", "Scarlet Woman", "Imp", "Empath", "Chambermaid").map(::RoleId).toSet()

        ClocktowerNightFlowPhase.entries.forEach { phase ->
            val plan = planner.planNight(
                ruleset = ruleset,
                phase = phase,
                context = ClocktowerFlowContext(playerCount = 6, inPlayRoleIds = inPlay),
            )
            val roleIds = plan.mapNotNull { token -> (token as? NightOrderToken.Character)?.roleId }
            assertTrue(RoleId("Artist") !in roleIds)
            assertTrue(RoleId("Klutz") !in roleIds)
        }
    }

    private fun List<NightOrderToken>.characterRoleIds(): List<RoleId> =
        mapNotNull { token -> (token as? NightOrderToken.Character)?.roleId }

    private fun List<ClocktowerHostInteraction>.actionableIds(): List<String> =
        filterNot { it.kind == ClocktowerHostInteractionKind.SYSTEM_BOUNDARY }
            .map { it.id.value }
}
