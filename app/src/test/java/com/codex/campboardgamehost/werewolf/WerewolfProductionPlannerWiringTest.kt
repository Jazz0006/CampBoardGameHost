package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WerewolfProductionPlannerWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val stepProjection = source
        .substringAfter("internal fun WerewolfJudgeScreen(")
        .substringBefore("val currentIndex = stepIndex.coerceIn")

    @Test
    fun `production Werewolf steps are projected by the canonical planner`() {
        assertTrue(stepProjection.contains("val roleRegistry = WerewolfRoleRegistry.builtIn()"))
        assertTrue(stepProjection.contains("val productionBoard = WerewolfBoardDefinition.create("))
        assertTrue(stepProjection.contains("val steps = WerewolfFlowPlanner()"))
        assertTrue(stepProjection.contains(".plan(productionBoard, roleRegistry)"))
        assertTrue(stepProjection.contains(".map { interaction -> interaction.legacyStep }"))
        assertFalse(stepProjection.contains("val steps = buildList"))
    }

    @Test
    fun `production board is reconstructed mechanically from every dealt role`() {
        assertTrue(stepProjection.contains("val productionRoleDeck = cards"))
        assertTrue(stepProjection.contains("roleRegistry.roleIdFor(card.role)"))
        assertTrue(stepProjection.contains("Unknown production Werewolf role"))
        assertTrue(stepProjection.contains(".groupingBy"))
        assertTrue(stepProjection.contains(".eachCount()"))
        assertFalse(
            "Production planner eligibility must remain role-existence based, not alive-only.",
            stepProjection.contains("eliminatedRound"),
        )
    }
}
