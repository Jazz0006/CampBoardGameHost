package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.LegacyRulesetCatalogAdapter
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

class ClocktowerLegacyPlannerDifferentialTest {
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

    private val legacyHostSource by lazy {
        File("src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
            .readText(Charsets.UTF_8)
    }

    private val planner = ClocktowerFlowPlanner()
    private val projector = ClocktowerHostInteractionProjector()

    @Test
    fun `production night ordering is owned by canonical planner seams after cutover`() {
        assertFalse(
            "Combined legacy night-order table must remain removed.",
            legacyHostSource.contains("fun officialNightOrder(step: ClocktowerNightStepUi): Int"),
        )
        assertFalse(
            "Other-night legacy numeric order table must be removed after cutover.",
            legacyHostSource.contains("fun legacyOtherNightOrder(step: ClocktowerNightStepUi): Int"),
        )
        assertFalse(
            "Production must not sort night steps through the legacy numeric table.",
            legacyHostSource.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"),
        )
        assertTrue(
            "Production first night must project canonical planner interactions before materialization.",
            legacyHostSource.contains("ClocktowerProductionFirstNightFlow.interactions("),
        )
        assertFalse(
            "Production first night must no longer route eager UI steps through the legacy-compatible orderer.",
            legacyHostSource.contains("ClocktowerProductionFirstNightFlow.order("),
        )
        assertTrue(
            "Production other night must be owned by the event-aware canonical planner seam.",
            legacyHostSource.contains("ClocktowerProductionOtherNightFlow.order("),
        )
    }

    @Test
    fun `first-night shadow projection matches the canonical production order`() {
        val inPlay = setOf(
            "Poisoner",
            "Spy",
            "Washerwoman",
            "Librarian",
            "Investigator",
            "Chef",
            "Empath",
            "Fortune Teller",
            "Butler",
            "Imp",
        ).map(::RoleId).toSet()

        assertEquals(
            listOf(
                "MINION_INFO",
                "DEMON_INFO",
                "Poisoner",
                "Spy",
                "Washerwoman",
                "Librarian",
                "Investigator",
                "Chef",
                "Empath",
                "FortuneTellerRedHerring",
                "Fortune Teller",
                "Butler",
            ),
            normalizedPlannerOrder(
                phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
                playerCount = 10,
                inPlay = inPlay,
            ),
        )
    }

    @Test
    fun `other-night shadow projection is legacy-order equivalent with resolved events`() {
        val inPlay = setOf(
            "Poisoner",
            "Monk",
            "Spy",
            "Imp",
            "Scarlet Woman",
            "Mayor",
            "Ravenkeeper",
            "Undertaker",
            "Empath",
            "Fortune Teller",
            "Butler",
        ).map(::RoleId).toSet()
        val facts = ClocktowerResolvedFlowFacts(
            setOf(
                ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED,
                ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE,
                ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT,
                ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY,
            ),
        )

        assertEquals(
            listOf(
                "Poisoner",
                "Monk",
                "Spy",
                "DemonKill",
                "DemonSuccessor",
                "MayorRedirect",
                "Ravenkeeper",
                "Undertaker",
                "Empath",
                "Fortune Teller",
                "Butler",
            ),
            normalizedPlannerOrder(
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                playerCount = 11,
                inPlay = inPlay,
                resolvedFacts = facts,
            ),
        )
    }

    @Test
    fun `day execution transition changes only the conditional Undertaker slot`() {
        val inPlay = setOf(
            "Poisoner",
            "Monk",
            "Spy",
            "Imp",
            "Undertaker",
            "Empath",
            "Fortune Teller",
            "Butler",
        ).map(::RoleId).toSet()

        val withoutExecution = normalizedPlannerOrder(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 8,
            inPlay = inPlay,
        )
        val withExecution = normalizedPlannerOrder(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 8,
            inPlay = inPlay,
            resolvedFacts = ClocktowerResolvedFlowFacts(
                setOf(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY),
            ),
        )

        assertEquals(
            listOf("Poisoner", "Monk", "Spy", "DemonKill", "Empath", "Fortune Teller", "Butler"),
            withoutExecution,
        )
        assertEquals(
            listOf("Poisoner", "Monk", "Spy", "DemonKill", "Undertaker", "Empath", "Fortune Teller", "Butler"),
            withExecution,
        )
    }

    @Test
    fun `shadow interaction identity is deterministic across equivalent fact sets`() {
        val inPlay = setOf(
            "Poisoner",
            "Monk",
            "Spy",
            "Imp",
            "Scarlet Woman",
            "Mayor",
            "Ravenkeeper",
            "Undertaker",
            "Empath",
            "Fortune Teller",
            "Butler",
        ).map(::RoleId).toSet()
        val forwardFacts = ClocktowerResolvedFlowFacts(
            ClocktowerResolvedFlowFact.entries.toSet(),
        )
        val reverseFacts = ClocktowerResolvedFlowFacts(
            ClocktowerResolvedFlowFact.entries.reversed().toSet(),
        )

        val forward = project(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 11,
            inPlay = inPlay,
            resolvedFacts = forwardFacts,
        )
        val reverse = project(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            playerCount = 11,
            inPlay = inPlay,
            resolvedFacts = reverseFacts,
        )

        assertEquals(forward.map { it.id }, reverse.map { it.id })
        assertEquals(forward.size, forward.map { it.id }.distinct().size)
    }

    private fun normalizedPlannerOrder(
        phase: ClocktowerNightFlowPhase,
        playerCount: Int,
        inPlay: Set<RoleId>,
        resolvedFacts: ClocktowerResolvedFlowFacts = ClocktowerResolvedFlowFacts.EMPTY,
    ): List<String> = project(phase, playerCount, inPlay, resolvedFacts).mapNotNull { interaction ->
        when (interaction.id.value) {
            "first_night:system:dusk",
            "first_night:system:dawn",
            "other_night:system:dusk",
            "other_night:system:dawn" -> null
            "first_night:system:minion_info" -> "MINION_INFO"
            "first_night:system:demon_info" -> "DEMON_INFO"
            "first_night:fortune_teller:red_herring" -> "FortuneTellerRedHerring"
            "other_night:role:Imp" -> "DemonKill"
            "other_night:event:imp:demon_successor" -> "DemonSuccessor"
            "other_night:event:mayor:death_resolution" -> "MayorRedirect"
            else -> interaction.roleId?.value
        }
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
        return projector.projectNight(
            phase = phase,
            basePlan = basePlan,
            resolvedFacts = resolvedFacts,
        )
    }
}
