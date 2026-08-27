package com.codex.campboardgamehost.clocktower.session

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary executable regression proof for the pre-planner production lifecycle.
 *
 * This test deliberately targets the existing App transaction wiring so SNE-7 can distinguish
 * a real lifecycle bug from the earlier compile-only RED caused by a not-yet-created planner seam.
 * Replace this source-level proof with planner/transaction behavior coverage once that seam exists.
 */
class NightDawnResolutionPlannerTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Imp self kill must preserve confirmed successor while new Demon identity is pending`() {
        val confirmNight = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")

        val afterPendingSuccessor = confirmNight
            .substringAfter("clocktowerPendingNewDemonName = newDemonName")

        assertFalse(
            "BUG: after Confirm Night has created pendingNewDemonName, the exact confirmed " +
                "successor must survive until Confirm New Demon materializes that identity.",
            afterPendingSuccessor.contains("clearConfirmedDemonSuccessorTarget()"),
        )
    }

    @Test
    fun `new Demon confirmation requires exact confirmed successor to materialize a Minion`() {
        val confirmNewDemon = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice")

        assertTrue(
            "A still-Minon successor can only materialize through the exact confirmed successor path.",
            confirmNewDemon.contains("clocktowerConfirmedDemonSuccessorTarget != null") &&
                confirmNewDemon.contains("materializeConfirmedNightDemonSuccessor()"),
        )
        assertTrue(
            "The fallback path only accepts a pending card that is already publicly a Demon, so it " +
                "cannot recover a cleared exact Minion successor confirmation.",
            confirmNewDemon.contains("pendingName != null") &&
                confirmNewDemon.contains("?.clocktowerTeam == ClocktowerTeam.Demon"),
        )
    }
}
