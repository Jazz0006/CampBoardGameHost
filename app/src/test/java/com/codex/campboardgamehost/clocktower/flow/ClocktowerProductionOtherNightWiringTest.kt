package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Coarse production ownership guards for the Other Night flow.
 *
 * Canonical ordering/rule behavior is covered through typed flow tests. These checks intentionally
 * avoid chained substring anchors and exact formatting so they protect only the remaining Host
 * wiring boundary while SNE-7 migrates production toward callable typed seams.
 */
class ClocktowerProductionOtherNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production host imports canonical other-night planner seam and resolved facts`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts"))
    }

    @Test
    fun `other-night planner input includes current public waking roles and living Drunk shown role`() {
        assertTrue(source.contains("val otherNightWakingRoleIds = buildSet"))
        assertTrue(source.contains("publicAliveCards.forEach { card ->"))
        assertTrue(source.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(source.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(source.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(source.contains("wakingRoleIds = otherNightWakingRoleIds"))
    }

    @Test
    fun `other-night planner receives already-resolved rule facts`() {
        assertTrue(source.contains("val otherNightResolvedFacts = ClocktowerResolvedFlowFacts("))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON"))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY"))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT"))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE"))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED"))
        assertTrue(source.contains("ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON"))
        assertTrue(source.contains("resolvedFacts = otherNightResolvedFacts"))
    }

    @Test
    fun `demon succession rule outcome is resolved before planner facts are projected`() {
        val resolution = source.indexOf("val impSelfKillNeedsSuccessor =")
        val facts = source.indexOf("val otherNightResolvedFacts =")
        assertTrue("Imp succession outcome must exist before planner facts are assembled", resolution >= 0 && facts > resolution)
    }

    @Test
    fun `production other night is planner-first and materializes by stable identity`() {
        assertTrue(source.contains("ClocktowerProductionOtherNightFlow.interactions("))
        assertTrue(source.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(source.contains("playerCount = cards.size"))
        assertTrue(source.contains("wakingRoleIds = otherNightWakingRoleIds"))
        assertTrue(source.contains("resolvedFacts = otherNightResolvedFacts"))
        assertTrue(source.contains("ClocktowerNightStepMaterializerRegistry("))
        assertTrue(source.contains("ClocktowerNightFlowPhase.OTHER_NIGHT"))
        assertTrue(source.contains(".materialize("))

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
                "Missing stable Other Night identity for $roleName",
                source.contains("ClocktowerProductionNightStepIdentity.role(RoleId(\"$roleName\"))"),
            )
        }
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.demonSuccessor()"))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.mayorRedirect()"))
    }

    @Test
    fun `production other night no longer constructs and reorders eager filtered steps`() {
        assertFalse(source.contains("val unfilteredNightSteps ="))
        assertFalse(source.contains("val filteredNightSteps ="))
        assertFalse(source.contains("ClocktowerProductionOtherNightFlow.order("))
        assertFalse(source.contains("productionSteps = filteredNightSteps"))
        assertFalse(source.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
    }

    @Test
    fun `other-night legacy numeric order table remains removed from production`() {
        assertFalse(source.contains("fun legacyOtherNightOrder"))
        assertFalse(source.contains("step.roleEnName == \"Poisoner\" -> 0"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.DemonKill -> 3"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.DemonSuccessor -> 4"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.MayorRedirect -> 5"))
    }

    @Test
    fun `daytime Scarlet Woman confirmation remains separate from next-night identity interaction`() {
        assertTrue(source.contains("pendingNewDemonName?.let { newDemonName ->"))
        assertTrue(source.contains("ClocktowerNewDemonConfirmationScreen("))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.newDemonIdentity()"))
    }
}
