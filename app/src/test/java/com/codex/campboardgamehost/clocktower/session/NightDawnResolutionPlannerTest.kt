package com.codex.campboardgamehost.clocktower.session

import java.io.File
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
    fun `Imp self kill preserves exact confirmed successor only while identity confirmation is pending`() {
        val confirmNight = appSource
            .substringAfter("onConfirmNight = {")
            .substringBefore("onShowResults = {")

        val afterPendingSuccessor = confirmNight
            .substringAfter("clocktowerPendingNewDemonName = newDemonName")

        assertTrue(
            "Confirm Night may clear ordinary-night successor state, but an exact confirmed " +
                "successor must survive while pendingNewDemonName still awaits identity confirmation.",
            afterPendingSuccessor.contains("if (clocktowerPendingNewDemonName == null) {") &&
                afterPendingSuccessor.contains("clocktowerDemonSuccessorTarget = null") &&
                afterPendingSuccessor.contains("clearConfirmedDemonSuccessorTarget()"),
        )
    }

    @Test
    fun `new Demon confirmation materializes exact successor before clearing its transaction facts`() {
        val confirmNewDemon = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice")

        assertTrue(
            "A still-Minion successor can only materialize through the exact confirmed successor path.",
            confirmNewDemon.contains("clocktowerConfirmedDemonSuccessorTarget != null") &&
                confirmNewDemon.contains("materializeConfirmedNightDemonSuccessor()"),
        )
        assertTrue(
            "The fallback path only accepts a pending card that is already publicly a Demon, so it " +
                "cannot recover a cleared exact Minion successor confirmation.",
            confirmNewDemon.contains("pendingName != null") &&
                confirmNewDemon.contains("?.clocktowerTeam == ClocktowerTeam.Demon"),
        )
        val successfulConfirmation = confirmNewDemon.substringAfter("if (canEnterDawn) {")
        assertTrue(
            "Successful identity confirmation must clear pending, draft, and confirmed successor " +
                "transaction facts before entering Dawn.",
            successfulConfirmation.contains("clocktowerPendingNewDemonName = null") &&
                successfulConfirmation.contains("clocktowerDemonSuccessorTarget = null") &&
                successfulConfirmation.contains("clearConfirmedDemonSuccessorTarget()"),
        )
    }

    @Test
    fun `Poisoner successor becoming Demon must end poison before Dawn`() {
        val confirmNewDemon = appSource
            .substringAfter("onConfirmNewDemon = {")
            .substringBefore("onSelectKlutzChoice")

        val afterMaterializationDecision = confirmNewDemon
            .substringAfter("materializeConfirmedNightDemonSuccessor()")

        assertTrue(
            "After the exact successor is materialized, poison lifecycle must be re-evaluated using " +
                "the successor's new current role before Dawn. Otherwise a Poisoner promoted to Imp " +
                "can incorrectly carry their old poison effect into the following day.",
            afterMaterializationDecision.contains("PoisonEffectLifecycle.afterNight(") &&
                afterMaterializationDecision.contains("clocktowerConfirmedPoisonTarget =") &&
                afterMaterializationDecision.contains("clocktowerPoisonTarget ="),
        )
    }
}
