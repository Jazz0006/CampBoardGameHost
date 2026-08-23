package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppRootDynamicFlowDecompositionGuardTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val firstNightFlowSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionFirstNightFlow.kt",
    ).readText(Charsets.UTF_8)
    private val otherNightFlowSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionOtherNightFlow.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `app root does not reclaim Clocktower night ordering authority`() {
        assertFalse(appSource.contains("ClocktowerProductionFirstNightFlow"))
        assertFalse(appSource.contains("ClocktowerProductionOtherNightFlow"))
        assertFalse(appSource.contains("ClocktowerNightStepMaterializerRegistry"))
    }

    @Test
    fun `production night seams remain planner and projector backed`() {
        assertTrue(firstNightFlowSource.contains("planner.planNight("))
        assertTrue(firstNightFlowSource.contains("projector.projectNight("))
        assertTrue(otherNightFlowSource.contains("planner.planNight("))
        assertTrue(otherNightFlowSource.contains("projector.projectNight("))

        assertTrue(firstNightFlowSource.contains("ruleset: ValidatedClocktowerRuleset"))
        assertTrue(otherNightFlowSource.contains("ruleset: ValidatedClocktowerRuleset"))
        assertFalse(firstNightFlowSource.contains("ClocktowerScript"))
        assertFalse(otherNightFlowSource.contains("ClocktowerScript"))
    }

    @Test
    fun `host consumes planner interactions before lazily materializing night steps`() {
        val firstPlannerIndex = hostSource.indexOf("ClocktowerProductionFirstNightFlow.interactions(")
        val firstMaterializeIndex = hostSource.indexOf(
            "firstNightMaterializers.materialize(firstNightInteractions)",
        )
        val otherPlannerIndex = hostSource.indexOf("ClocktowerProductionOtherNightFlow.interactions(")
        val otherMaterializeIndex = hostSource.indexOf(
            "otherNightMaterializers.materialize(otherNightInteractions)",
        )

        assertTrue(firstPlannerIndex >= 0)
        assertTrue(firstMaterializeIndex > firstPlannerIndex)
        assertTrue(otherPlannerIndex >= 0)
        assertTrue(otherMaterializeIndex > otherPlannerIndex)
        assertTrue(hostSource.contains("ClocktowerProductionNightStepIdentity.role(RoleId("))
    }

    @Test
    fun `host resolves active script through the shared built-in ruleset catalog`() {
        val catalogResolution = "BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"
        assertTrue(hostSource.contains(catalogResolution))
    }
}
