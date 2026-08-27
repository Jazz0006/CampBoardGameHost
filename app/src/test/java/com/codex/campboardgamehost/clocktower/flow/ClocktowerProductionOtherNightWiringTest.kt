package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionOtherNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val planningInputs = source
        .substringAfter("val firstNightActualRoleIds = buildSet")
        .substringBefore("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")

    private val nightRouting = source
        .substringAfter("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")
        .substringBefore("playerDisplayStep?.let")

    private val otherNightRouting = nightRouting
        .substringAfter("firstNightMaterializers.materialize(firstNightInteractions)\n    } else {")

    @Test
    fun `production host imports canonical other-night planner seam and resolved facts`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts"))
    }

    @Test
    fun `other-night planner input uses only currently waking roles including living Drunk shown role`() {
        assertTrue(planningInputs.contains("val otherNightWakingRoleIds = buildSet"))
        assertTrue(planningInputs.contains("aliveCards.forEach { card ->"))
        assertTrue(planningInputs.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(planningInputs.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(planningInputs.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(otherNightRouting.contains("wakingRoleIds = otherNightWakingRoleIds"))
    }

    @Test
    fun `other-night planner receives existing resolved rule outcomes rather than deriving rules`() {
        assertTrue(planningInputs.contains("val otherNightResolvedFacts = ClocktowerResolvedFlowFacts("))
        assertTrue(planningInputs.contains("if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)"))
        assertTrue(planningInputs.contains("if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)"))
        assertTrue(planningInputs.contains("if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)"))
        assertTrue(planningInputs.contains("if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)"))
        assertTrue(planningInputs.contains("if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)"))
        assertTrue(planningInputs.contains("if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)"))
        assertTrue(otherNightRouting.contains("resolvedFacts = otherNightResolvedFacts"))
    }

    @Test
    fun `demon succession rule outcome is resolved before planner facts are projected`() {
        val resolution = source.indexOf("val impSelfKillNeedsSuccessor =")
        val facts = source.indexOf("val otherNightResolvedFacts =")
        assertTrue("Imp succession outcome must exist before planner facts are assembled", resolution >= 0 && facts > resolution)
    }

    @Test
    fun `production other night is planner-first and lazily materialized by stable identity`() {
        assertTrue(otherNightRouting.contains("ClocktowerProductionOtherNightFlow.interactions("))
        assertTrue(otherNightRouting.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(otherNightRouting.contains("playerCount = cards.size"))
        assertTrue(otherNightRouting.contains("wakingRoleIds = otherNightWakingRoleIds"))
        assertTrue(otherNightRouting.contains("resolvedFacts = otherNightResolvedFacts"))
        assertTrue(otherNightRouting.contains("ClocktowerNightStepMaterializerRegistry("))
        assertTrue(otherNightRouting.contains("phase = ClocktowerNightFlowPhase.OTHER_NIGHT"))
        assertTrue(otherNightRouting.contains(".materialize("))

        listOf(
            "Poisoner",
            "Butler",
            "Empath",
            "Chambermaid",
            "Fortune Teller",
            "Undertaker",
            "Monk",
            "Imp",
            "Sage",
            "Ravenkeeper",
            "Spy",
        ).forEach { roleName ->
            assertTrue(
                "Missing lazy Other Night materializer for $roleName",
                otherNightRouting.contains("ClocktowerProductionNightStepIdentity.role(RoleId(\"$roleName\"))"),
            )
        }
        assertTrue(otherNightRouting.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
        assertTrue(otherNightRouting.contains("ClocktowerProductionNightStepIdentity.demonSuccessor()"))
        assertTrue(otherNightRouting.contains("ClocktowerProductionNightStepIdentity.mayorRedirect()"))
    }

    @Test
    fun `production other night no longer constructs and reorders eager filtered steps`() {
        assertFalse(source.contains("val unfilteredNightSteps ="))
        assertFalse(source.contains("val filteredNightSteps ="))
        assertFalse(otherNightRouting.contains("ClocktowerProductionOtherNightFlow.order("))
        assertFalse(otherNightRouting.contains("productionSteps = filteredNightSteps"))
        assertFalse(otherNightRouting.contains("identityOf ="))
    }

    @Test
    fun `other-night legacy numeric order table remains removed from production`() {
        assertFalse(source.contains("fun legacyOtherNightOrder"))
        assertFalse(source.contains("step.roleEnName == \"Poisoner\" -> 0"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.DemonKill -> 3"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.DemonSuccessor -> 4"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.MayorRedirect -> 5"))
        assertFalse(source.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
    }

    @Test
    fun `daytime Scarlet Woman confirmation remains separate from next-night identity interaction`() {
        assertTrue(source.contains("pendingNewDemonName?.let { newDemonName ->"))
        assertTrue(source.contains("ClocktowerNewDemonConfirmationScreen("))
        assertTrue(otherNightRouting.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
    }
}
