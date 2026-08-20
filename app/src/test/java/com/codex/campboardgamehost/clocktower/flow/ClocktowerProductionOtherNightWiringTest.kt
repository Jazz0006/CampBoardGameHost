package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionOtherNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val ordering = source
        .substringAfter("val filteredNightSteps = unfilteredNightSteps.filter")
        .substringBefore("playerDisplayStep?.let")

    @Test
    fun `production host imports canonical other-night planner seam and resolved facts`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts"))
    }

    @Test
    fun `other-night planner input uses only currently waking roles including living Drunk shown role`() {
        assertTrue(ordering.contains("val otherNightWakingRoleIds = buildSet"))
        assertTrue(ordering.contains("aliveCards.forEach { card ->"))
        assertTrue(ordering.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(ordering.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(ordering.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(ordering.contains("wakingRoleIds = otherNightWakingRoleIds"))
    }

    @Test
    fun `other-night planner receives existing resolved rule outcomes rather than deriving rules`() {
        assertTrue(ordering.contains("val otherNightResolvedFacts = ClocktowerResolvedFlowFacts("))
        assertTrue(ordering.contains("if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)"))
        assertTrue(ordering.contains("if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)"))
        assertTrue(ordering.contains("if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)"))
        assertTrue(ordering.contains("if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)"))
        assertTrue(ordering.contains("if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)"))
        assertFalse(ordering.contains("ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON"))
        assertTrue(ordering.contains("resolvedFacts = otherNightResolvedFacts"))
    }

    @Test
    fun `demon succession rule outcome is resolved before night UI composition`() {
        val resolution = source.indexOf("val impSelfKillNeedsSuccessor =")
        val composition = source.indexOf("val unfilteredNightSteps =")
        assertTrue("Imp succession outcome must exist before UI composition", resolution >= 0 && composition > resolution)
    }

    @Test
    fun `production other night is ordered by canonical planner with exact event identities`() {
        assertTrue(ordering.contains("ClocktowerProductionOtherNightFlow.order("))
        assertTrue(ordering.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(ordering.contains("playerCount = cards.size"))
        assertTrue(ordering.contains("productionSteps = filteredNightSteps"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.demonSuccessor()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.mayorRedirect()"))
        assertTrue(ordering.contains("step.action == ClocktowerNightAction.DemonKill"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.role(RoleId(\"Imp\"))"))
        assertTrue(ordering.contains("RoleId(requireNotNull(step.roleEnName)"))
    }

    @Test
    fun `other-night legacy numeric order table is removed from production`() {
        assertFalse(ordering.contains("fun legacyOtherNightOrder"))
        assertFalse(ordering.contains("step.roleEnName == \"Poisoner\" -> 0"))
        assertFalse(ordering.contains("step.action == ClocktowerNightAction.DemonKill -> 3"))
        assertFalse(ordering.contains("step.action == ClocktowerNightAction.DemonSuccessor -> 4"))
        assertFalse(ordering.contains("step.action == ClocktowerNightAction.MayorRedirect -> 5"))
        assertFalse(ordering.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
    }

    @Test
    fun `new Demon identity confirmation remains a separate legacy screen in this stage`() {
        assertTrue(source.contains("pendingNewDemonName?.let { newDemonName ->"))
        assertTrue(source.contains("ClocktowerNewDemonConfirmationScreen("))
        assertFalse(ordering.contains("new_demon_identity"))
    }
}
