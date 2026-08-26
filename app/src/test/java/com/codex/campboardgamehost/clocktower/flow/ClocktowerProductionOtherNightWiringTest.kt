package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionOtherNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val plannerCall = "ClocktowerProductionOtherNightFlow.interactions("
    private val materializerRouting = source
        .substringAfter("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")
        .substringBefore("playerDisplayStep?.let")

    @Test
    fun `production host imports canonical other-night planner seam and resolved facts`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts"))
    }

    @Test
    fun `other-night canonical plan starts from public night-start waking roles including living Drunk shown role`() {
        assertTrue(source.contains("val publicAliveCards = cards.filter { it.eliminatedRound == null }"))
        assertFalse(source.contains("val aliveCards = cards.filter { it.eliminatedRound == null }"))
        assertTrue(source.contains("val otherNightWakingRoleIds = buildSet"))
        assertTrue(source.contains("publicAliveCards.forEach { card ->"))
        assertTrue(source.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(source.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(source.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(source.contains("wakingRoleIds = otherNightWakingRoleIds"))
    }

    @Test
    fun `other-night planner receives existing resolved rule outcomes rather than deriving rules`() {
        assertTrue(source.contains("val otherNightResolvedFacts = ClocktowerResolvedFlowFacts("))
        assertTrue(source.contains("if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)"))
        assertTrue(source.contains("if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)"))
        assertTrue(source.contains("if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)"))
        assertTrue(source.contains("if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)"))
        assertTrue(source.contains("if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)"))
        assertTrue(source.contains("if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)"))
        assertTrue(source.contains("resolvedFacts = otherNightResolvedFacts"))
    }

    @Test
    fun `demon succession rule outcome is resolved before planner facts are projected`() {
        val resolution = source.indexOf("val impSelfKillNeedsSuccessor =")
        val facts = source.indexOf("val otherNightResolvedFacts =")
        assertTrue("Imp succession outcome must exist before planner facts are assembled", resolution >= 0 && facts > resolution)
    }

    @Test
    fun `canonical other-night plan is built once before UI routing and lazily materialized by stable identity`() {
        val wakingRoles = source.indexOf("val otherNightWakingRoleIds =")
        val resolvedFacts = source.indexOf("val otherNightResolvedFacts =")
        val planner = source.indexOf(plannerCall)
        val nightSteps = source.indexOf("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")
        assertTrue(wakingRoles >= 0 && resolvedFacts > wakingRoles && planner > resolvedFacts && nightSteps > planner)
        assertTrue(Regex("""ClocktowerProductionOtherNightFlow\.interactions\(""").findAll(source).count() == 1)
        assertTrue(source.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(source.contains("playerCount = cards.size"))
        assertTrue(source.contains("wakingRoleIds = otherNightWakingRoleIds"))
        assertTrue(source.contains("resolvedFacts = otherNightResolvedFacts"))
        assertTrue(materializerRouting.contains("ClocktowerNightStepMaterializerRegistry("))
        assertTrue(materializerRouting.contains("phase = ClocktowerNightFlowPhase.OTHER_NIGHT"))
        assertTrue(materializerRouting.contains(".materialize(otherNightInteractions)"))

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
                materializerRouting.contains("ClocktowerProductionNightStepIdentity.role(RoleId(\"$roleName\"))"),
            )
        }
        assertTrue(materializerRouting.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
        assertTrue(materializerRouting.contains("ClocktowerProductionNightStepIdentity.demonSuccessor()"))
        assertTrue(materializerRouting.contains("ClocktowerProductionNightStepIdentity.mayorRedirect()"))
    }

    @Test
    fun `production other night no longer constructs and reorders eager filtered steps`() {
        assertFalse(source.contains("val unfilteredNightSteps ="))
        assertFalse(source.contains("val filteredNightSteps ="))
        assertFalse(source.contains("ClocktowerProductionOtherNightFlow.order("))
        assertFalse(source.contains("productionSteps = filteredNightSteps"))
        assertFalse(source.contains("identityOf ="))
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
        assertTrue(materializerRouting.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
    }
}
