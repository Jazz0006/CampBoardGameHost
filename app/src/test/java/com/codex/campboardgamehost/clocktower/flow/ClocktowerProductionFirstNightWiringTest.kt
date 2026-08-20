package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerProductionFirstNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val ordering = source
        .substringAfter("val filteredNightSteps = unfilteredNightSteps.filter")
        .substringBefore("playerDisplayStep?.let")

    @Test
    fun `production host imports canonical first-night planner seam`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity"))
    }

    @Test
    fun `production first night is ordered by canonical planner with exact production identities`() {
        assertTrue(ordering.contains("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {"))
        assertTrue(ordering.contains("ClocktowerProductionFirstNightFlow.order("))
        assertTrue(ordering.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(ordering.contains("playerCount = cards.size"))
        assertTrue(ordering.contains("inPlayRoleIds = cards.mapNotNull { it.clocktowerRole?.enName }.map(::RoleId).toSet()"))
        assertTrue(ordering.contains("productionSteps = filteredNightSteps"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.minionInfo()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.demonInfo()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.role("))
        assertTrue(ordering.contains("RoleId(requireNotNull(step.roleEnName)"))
    }

    @Test
    fun `other night remains legacy until its event projection cutover`() {
        assertTrue(ordering.contains("fun legacyOtherNightOrder(step: ClocktowerNightStepUi): Int"))
        assertTrue(ordering.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
        assertTrue(ordering.contains("step.action == ClocktowerNightAction.DemonSuccessor"))
        assertTrue(ordering.contains("step.action == ClocktowerNightAction.MayorRedirect"))
    }

    @Test
    fun `first-night legacy numeric order table is removed from production`() {
        assertFalse(ordering.contains("fun officialNightOrder"))
        assertFalse(ordering.contains("step.title == minionInfoTitle -> 0"))
        assertFalse(ordering.contains("step.title == demonInfoTitle -> 1"))
        assertFalse(ordering.contains("step.roleEnName == \"Clockmaker\" -> 4"))
        assertFalse(ordering.contains("step.action == ClocktowerNightAction.RedHerring -> 10"))
    }
}
