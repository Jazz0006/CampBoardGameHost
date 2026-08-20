package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerHostInteractionProjectorTest {
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
    private val projector = ClocktowerHostInteractionProjector()

    @Test
    fun `first-night projection inserts Fortune Teller red herring before its role action`() {
        val inPlay = setOf(
            "Poisoner",
            "Spy",
            "Chef",
            "Empath",
            "Fortune Teller",
            "Butler",
            "Imp",
        ).map(::RoleId).toSet()
        val basePlan = planner.planNight(
            ruleset = ruleset,
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            context = ClocktowerFlowContext(playerCount = 7, inPlayRoleIds = inPlay),
        )
        val interactions = projector.projectNight(ClocktowerNightFlowPhase.FIRST_NIGHT, basePlan)

        val empathIndex = interactions.indexOfFirst { it.roleId == RoleId("Empath") }
        val redHerringIndex = interactions.indexOfFirst {
            it.id == ClocktowerInteractionId("first_night:fortune_teller:red_herring")
        }
        val fortuneTellerIndex = interactions.indexOfFirst {
            it.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION &&
                it.roleId == RoleId("Fortune Teller")
        }

        assertTrue(empathIndex >= 0)
        assertEquals(empathIndex + 1, redHerringIndex)
        assertEquals(redHerringIndex + 1, fortuneTellerIndex)

        val redHerring = interactions[redHerringIndex]
        assertEquals(RoleId("Fortune Teller"), redHerring.roleId)
        assertEquals(ClocktowerHostInteractionKind.STORYTELLER_SETUP, redHerring.kind)
        assertEquals(
            ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION,
            redHerring.completionPolicy,
        )
        assertNull(redHerring.decisionPointId)
    }

    @Test
    fun `first night without Fortune Teller does not invent red herring interaction`() {
        val inPlay = setOf("Poisoner", "Spy", "Chef", "Empath", "Butler", "Imp").map(::RoleId).toSet()
        val interactions = project(ClocktowerNightFlowPhase.FIRST_NIGHT, playerCount = 7, inPlay = inPlay)

        assertTrue(interactions.none {
            it.id == ClocktowerInteractionId("first_night:fortune_teller:red_herring")
        })
    }

    @Test
    fun `other night does not repeat Fortune Teller red herring setup`() {
        val inPlay = setOf("Poisoner", "Spy", "Empath", "Fortune Teller", "Butler", "Imp").map(::RoleId).toSet()
        val interactions = project(ClocktowerNightFlowPhase.OTHER_NIGHT, playerCount = 7, inPlay = inPlay)

        assertTrue(interactions.none { it.kind == ClocktowerHostInteractionKind.STORYTELLER_SETUP })
        assertTrue(interactions.any {
            it.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION &&
                it.roleId == RoleId("Fortune Teller")
        })
    }

    @Test
    fun `conditional other-night anchors stay silent without resolved facts`() {
        val inPlay = setOf(
            "Imp",
            "Scarlet Woman",
            "Mayor",
            "Ravenkeeper",
            "Undertaker",
            "Empath",
        ).map(::RoleId).toSet()
        val interactions = project(ClocktowerNightFlowPhase.OTHER_NIGHT, playerCount = 8, inPlay = inPlay)

        assertTrue(interactions.any { it.id == ClocktowerInteractionId("other_night:role:Imp") })
        assertTrue(interactions.any { it.id == ClocktowerInteractionId("other_night:role:Empath") })
        assertTrue(interactions.none { it.id == ClocktowerInteractionId("other_night:role:Scarlet Woman") })
        assertTrue(interactions.none { it.id == ClocktowerInteractionId("other_night:event:mayor:death_resolution") })
        assertTrue(interactions.none { it.id == ClocktowerInteractionId("other_night:role:Ravenkeeper") })
        assertTrue(interactions.none { it.id == ClocktowerInteractionId("other_night:role:Undertaker") })
    }

    @Test
    fun `resolved facts project legacy conditional and event order`() {
        val inPlay = setOf(
            "Imp",
            "Scarlet Woman",
            "Mayor",
            "Ravenkeeper",
            "Undertaker",
            "Empath",
        ).map(::RoleId).toSet()
        val facts = ClocktowerResolvedFlowFacts(
            setOf(
                ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED,
                ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE,
                ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT,
                ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY,
            ),
        )
        val interactions = project(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 8,
            inPlay = inPlay,
            resolvedFacts = facts,
        )

        val expectedOrderedIds = listOf(
            "other_night:role:Imp",
            "other_night:event:imp:demon_successor",
            "other_night:event:mayor:death_resolution",
            "other_night:role:Ravenkeeper",
            "other_night:role:Undertaker",
        )
        assertEquals(
            expectedOrderedIds,
            interactions.map { it.id.value }.filter { it in expectedOrderedIds.toSet() },
        )
        assertTrue(interactions.none { it.id == ClocktowerInteractionId("other_night:role:Scarlet Woman") })

        val successor = interactions.single {
            it.id == ClocktowerInteractionId("other_night:event:imp:demon_successor")
        }
        assertEquals(ClocktowerHostInteractionKind.EVENT_RESOLUTION, successor.kind)
        assertEquals(ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION, successor.completionPolicy)
        assertEquals(RoleId("Imp"), successor.roleId)

        val mayor = interactions.single {
            it.id == ClocktowerInteractionId("other_night:event:mayor:death_resolution")
        }
        assertEquals(ClocktowerHostInteractionKind.EVENT_RESOLUTION, mayor.kind)
        assertEquals(ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION, mayor.completionPolicy)
        assertEquals(RoleId("Mayor"), mayor.roleId)
    }

    @Test
    fun `resolved facts do not invent interactions for roles that are not in play`() {
        val facts = ClocktowerResolvedFlowFacts(
            ClocktowerResolvedFlowFact.entries.toSet(),
        )
        val inPlay = setOf("Poisoner", "Empath", "Fortune Teller", "Butler").map(::RoleId).toSet()
        val interactions = project(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 8,
            inPlay = inPlay,
            resolvedFacts = facts,
        )

        assertTrue(interactions.none { it.id.value.contains("demon_successor") })
        assertTrue(interactions.none { it.id.value.contains("mayor:death_resolution") })
        assertTrue(interactions.none { it.roleId == RoleId("Ravenkeeper") })
        assertTrue(interactions.none { it.roleId == RoleId("Undertaker") })
    }

    @Test
    fun `system and role tokens project to stable unique interaction identities`() {
        val inPlay = setOf("Poisoner", "Empath", "Fortune Teller", "Imp").map(::RoleId).toSet()
        val interactions = project(ClocktowerNightFlowPhase.FIRST_NIGHT, playerCount = 7, inPlay = inPlay)

        assertEquals(interactions.size, interactions.map { it.id }.distinct().size)
        assertEquals(
            ClocktowerInteractionId("first_night:system:dusk"),
            interactions.first().id,
        )
        assertEquals(
            ClocktowerInteractionId("first_night:system:dawn"),
            interactions.last().id,
        )
        assertTrue(interactions.any { it.id == ClocktowerInteractionId("first_night:system:minion_info") })
        assertTrue(interactions.any { it.id == ClocktowerInteractionId("first_night:system:demon_info") })
        assertTrue(interactions.any { it.id == ClocktowerInteractionId("first_night:role:Poisoner") })
    }

    @Test
    fun `duplicate role handlers fail closed`() {
        val handler = object : ClocktowerCharacterInteractionHandler {
            override val roleId: RoleId = RoleId("Fortune Teller")
        }
        val failure = runCatching {
            ClocktowerCharacterInteractionRegistry(listOf(handler, handler))
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private fun project(
        phase: ClocktowerNightFlowPhase,
        playerCount: Int,
        inPlay: Set<RoleId>,
        resolvedFacts: ClocktowerResolvedFlowFacts = ClocktowerResolvedFlowFacts.EMPTY,
    ): List<ClocktowerHostInteraction> {
        val basePlan = planner.planNight(
            ruleset = ruleset,
            phase = phase,
            context = ClocktowerFlowContext(playerCount = playerCount, inPlayRoleIds = inPlay),
        )
        return projector.projectNight(phase, basePlan, resolvedFacts)
    }
}
