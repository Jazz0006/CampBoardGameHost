package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.9D coarse App ownership guard; succession gameplay semantics stay in typed tests. */
class ClocktowerDemonSuccessionProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Imp self kill Dawn consumes canonical succession resolution and planner`() {
        val dawnBlock = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onConfirmNewDemon = {")
        val demonDeathBlock = dawnBlock
            .substringAfter("if (demonDied) {")
            .substringBefore("if (nightDeathCard.clocktowerRole?.enName == \"Klutz\")")

        assertTrue(
            "Imp self-kill Dawn must derive current succession through the shared Trouble Brewing resolver.",
            demonDeathBlock.contains("resolveTroubleBrewingImpSelfKillSuccession("),
        )
        assertTrue(
            "Imp self-kill Dawn must consume NightDawnResolutionPlanner.planDemonSuccession.",
            demonDeathBlock.contains("NightDawnResolutionPlanner.planDemonSuccession("),
        )
        assertTrue(
            "App must project planner-owned pending new-Demon state back to its checkpoint fields.",
            demonDeathBlock.contains("successionTransition.checkpoint.pendingNewDemonName"),
        )
        assertFalse(
            "A confirmed UI successor name must not remain an independent Dawn succession authority.",
            demonDeathBlock.contains("newDemonName = clocktowerConfirmedDemonSuccessorTarget"),
        )
        assertFalse(
            "Dawn must not decide unresolved succession merely from the presence of any living Minion.",
            demonDeathBlock.contains("it.clocktowerTeam == ClocktowerTeam.Minion"),
        )
    }

    @Test
    fun `new Demon identity confirmation follows planner pending authority`() {
        val confirmationBlock = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice = {")

        assertTrue(confirmationBlock.contains("val pendingName = clocktowerPendingNewDemonName"))
        assertTrue(confirmationBlock.contains("NightDawnResolutionPlanner.confirmNewDemonIdentity("))
        assertFalse(
            "Forced succession intentionally has no confirmed choice, so identity confirmation cannot be gated by one.",
            confirmationBlock.contains("if (clocktowerConfirmedDemonSuccessorTarget != null)"),
        )
    }
}
