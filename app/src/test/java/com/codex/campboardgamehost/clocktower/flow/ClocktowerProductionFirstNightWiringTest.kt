package com.codex.campboardgamehost.clocktower.flow

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerProductionFirstNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val ordering = source
        .substringAfter("val filteredNightSteps = unfilteredNightSteps.filter")
        .substringBefore("playerDisplayStep?.let")

    private val firstNightRouting = source
        .substringAfter("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")
        .substringBefore("ClocktowerProductionOtherNightFlow.order(")

    @Test
    fun `production host uses canonical first-night planner seam`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity"))
    }

    @Test
    fun `production first night is planner-first and lazily materialized`() {
        assertTrue(ordering.contains("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {"))
        assertTrue(firstNightRouting.contains("ClocktowerProductionFirstNightFlow.interactions("))
        assertTrue(firstNightRouting.contains("BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)"))
        assertTrue(firstNightRouting.contains("playerCount = cards.size"))
        assertTrue(firstNightRouting.contains("inPlayRoleIds = firstNightWakingRoleIds"))
        assertTrue(firstNightRouting.contains("actualRoleIds = firstNightActualRoleIds"))
        assertTrue(firstNightRouting.contains("ClocktowerNightStepMaterializerRegistry("))
        assertTrue(firstNightRouting.contains("ClocktowerNightFlowPhase.FIRST_NIGHT"))
        assertTrue(firstNightRouting.contains(".materialize("))
    }

    @Test
    fun `production first night no longer routes eager filtered steps through the orderer`() {
        assertFalse(firstNightRouting.contains("ClocktowerProductionFirstNightFlow.order("))
        assertFalse(firstNightRouting.contains("productionSteps = filteredNightSteps"))
        assertFalse(firstNightRouting.contains("identityOf ="))
    }

    @Test
    fun `first-night materializer registry binds stable production identities`() {
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.minionInfo()"))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.demonInfo()"))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()"))
        assertTrue(source.contains("ClocktowerProductionNightStepIdentity.role("))
    }

    @Test
    fun `production first-night planner input preserves actual and Drunk waking identities separately`() {
        assertTrue(ordering.contains("val firstNightActualRoleIds = buildSet"))
        assertTrue(ordering.contains("val firstNightWakingRoleIds = buildSet"))
        assertTrue(ordering.contains("cards.forEach { card ->"))
        assertTrue(ordering.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(ordering.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(ordering.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(firstNightRouting.contains("inPlayRoleIds = firstNightWakingRoleIds"))
        assertTrue(firstNightRouting.contains("actualRoleIds = firstNightActualRoleIds"))

        val actualRolesBlock = ordering
            .substringAfter("val firstNightActualRoleIds = buildSet")
            .substringBefore("val firstNightWakingRoleIds = buildSet")
        assertFalse(actualRolesBlock.contains("clocktowerShownRole"))
    }

    @Test
    fun `other night remains on its dedicated event-aware orderer until A13`() {
        assertTrue(ordering.contains("ClocktowerProductionOtherNightFlow.order("))
        assertTrue(ordering.contains("productionSteps = filteredNightSteps"))
        assertFalse(ordering.contains("fun legacyOtherNightOrder(step: ClocktowerNightStepUi): Int"))
        assertFalse(ordering.contains("filteredNightSteps.sortedBy(::legacyOtherNightOrder)"))
    }

    @Test
    fun `first-night legacy numeric order table remains absent`() {
        assertFalse(ordering.contains("fun officialNightOrder"))
        assertFalse(ordering.contains("step.title == minionInfoTitle -> 0"))
        assertFalse(ordering.contains("step.title == demonInfoTitle -> 1"))
        assertFalse(ordering.contains("step.roleEnName == \"Clockmaker\" -> 4"))
        assertFalse(ordering.contains("step.action == ClocktowerNightAction.RedHerring -> 10"))
    }
}
