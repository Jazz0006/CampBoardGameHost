package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse App ownership guard for Dawn durable materialization.
 *
 * Exactly-once, stable-ID and retry semantics are proved by typed planner/session tests. These
 * checks only ensure the two non-callable App callbacks continue consuming the canonical planner.
 */
class ClocktowerDawnDurableMaterializationProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `night confirmation consumes canonical Dawn durable materialization planner`() {
        val block = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")

        assertTrue(block.contains("NightDawnDurableMaterializationPlanner.plan("))
        assertTrue(block.contains("DawnDurableMaterializationState("))
    }

    @Test
    fun `new Demon confirmation consumes canonical Dawn durable materialization planner`() {
        val block = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice = {")

        assertTrue(block.contains("NightDawnDurableMaterializationPlanner.plan("))
        assertTrue(block.contains("DawnDurableMaterializationState("))
    }
}
