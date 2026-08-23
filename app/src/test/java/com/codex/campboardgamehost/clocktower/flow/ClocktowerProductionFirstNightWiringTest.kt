package com.codex.campboardgamehost.clocktower.flow

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerProductionFirstNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val nightRouting = source
        .substringAfter("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {")
        .substringBefore("playerDisplayStep?.let")

    private val firstNightMaterializeCall =
        "firstNightMaterializers.materialize(firstNightInteractions)"

    private val firstNightRouting = nightRouting
        .substringBefore(firstNightMaterializeCall) + firstNightMaterializeCall

    private val firstNightPlannerInputs = source
        .substringAfter("val firstNightActualRoleIds = buildSet")
        .substringBefore("val otherNightWakingRoleIds = buildSet")

    @Test
    fun `production host uses canonical first-night planner seam`() {
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow"))
        assertTrue(source.contains("import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity"))
    }

    @Test
    fun `production first night is planner-first and lazily materialized`() {
        assertTrue(nightRouting.contains("val nightSteps = if (phase == ClocktowerPhase.FirstNight) {").not())
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
        assertTrue(firstNightRouting.contains("ClocktowerProductionNightStepIdentity.minionInfo()"))
        assertTrue(firstNightRouting.contains("ClocktowerProductionNightStepIdentity.demonInfo()"))
        assertTrue(firstNightRouting.contains("ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()"))
        assertTrue(firstNightRouting.contains("ClocktowerProductionNightStepIdentity.role("))
    }

    @Test
    fun `production first-night planner input preserves actual and Drunk waking identities separately`() {
        assertTrue(firstNightPlannerInputs.contains("val firstNightWakingRoleIds = buildSet"))
        assertTrue(firstNightPlannerInputs.contains("cards.forEach { card ->"))
        assertTrue(firstNightPlannerInputs.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(firstNightPlannerInputs.contains("if (card.clocktowerRole?.enName == \"Drunk\") {"))
        assertTrue(firstNightPlannerInputs.contains("card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(firstNightRouting.contains("inPlayRoleIds = firstNightWakingRoleIds"))
        assertTrue(firstNightRouting.contains("actualRoleIds = firstNightActualRoleIds"))

        val actualRolesBlock = source
            .substringAfter("val firstNightActualRoleIds = buildSet")
            .substringBefore("val firstNightWakingRoleIds = buildSet")
        assertFalse(actualRolesBlock.contains("clocktowerShownRole"))
    }

    @Test
    fun `first-night legacy numeric order table remains absent`() {
        assertFalse(source.contains("fun officialNightOrder"))
        assertFalse(source.contains("step.title == minionInfoTitle -> 0"))
        assertFalse(source.contains("step.title == demonInfoTitle -> 1"))
        assertFalse(source.contains("step.roleEnName == \"Clockmaker\" -> 4"))
        assertFalse(source.contains("step.action == ClocktowerNightAction.RedHerring -> 10"))
    }
}
