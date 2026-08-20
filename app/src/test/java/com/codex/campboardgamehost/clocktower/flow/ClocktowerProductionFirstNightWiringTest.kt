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
        assertTrue(ordering.contains("productionSteps = filteredNightSteps"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.minionInfo()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.demonInfo()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()"))
        assertTrue(ordering.contains("ClocktowerProductionNightStepIdentity.role("))
        assertTrue(ordering.contains("RoleId(requireNotNull(step.roleEnName)"))
    }

    @Test
    fun `production first-night planner input preserves Drunk waking identity separately from functioning identity`() {
        assertTrue(ordering.contains("val firstNightWakingRoleIds = buildSet"))
        assertTrue(ordering.contains("cards.forEach { card ->"))
        assertTrue(ordering.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(ordering.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(ordering.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(ordering.contains("inPlayRoleIds = firstNightWakingRoleIds"))
    }

    @Test
    fun `other night cutover is owned by its dedicated event-aware planner guard`() {
        assertTrue(ordering.contains("ClocktowerProductionOtherNightFlow.order("))
        assertFalse(ordering.contains("fun legacyOtherNightOrder(step: ClocktowerNightStepUi): Int"))
        assertFalse(ordering.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
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
