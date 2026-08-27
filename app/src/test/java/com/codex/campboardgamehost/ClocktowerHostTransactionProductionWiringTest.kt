package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.6A temporary production-wiring guard.
 *
 * Typed behavior belongs to NightCheckpointHostTransactionTest. This source guard exists only to
 * prove that the real App/Host callbacks consume that callable seam. Retire or narrow it during
 * SNE-7.7 once the integration boundary is fully established.
 */
class ClocktowerHostTransactionProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val successorDraftBlock = appSource
        .substringAfter("onSelectDemonSuccessor = {")
        .substringBefore("onConfirmDemonSuccessorTarget = {")

    private val successorConfirmBlock = appSource
        .substringAfter("onConfirmDemonSuccessorTarget = {")
        .substringBefore("onConfirmNewDemon = {")

    private val judgeCallBlock = appSource
        .substringAfter("Screen.ClocktowerJudge -> ClocktowerJudgeScreen(")
        .substringBefore("onRecordEvent =")

    @Test
    fun `App successor callbacks consume callable host transaction seam`() {
        assertTrue(successorDraftBlock.contains("NightCheckpointHostTransaction.editDemonSuccessor("))
        assertTrue(successorConfirmBlock.contains("NightCheckpointHostTransaction.confirmDemonSuccessor("))
        assertFalse(successorDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertFalse(successorConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `App supplies Previous navigation through callable host transaction seam`() {
        assertTrue(judgeCallBlock.contains("onMovePreviousNightStep = {"))
        assertTrue(judgeCallBlock.contains("NightCheckpointHostTransaction.movePrevious("))
    }

    @Test
    fun `Host Previous callbacks no longer mutate nightStepIndex directly`() {
        assertTrue(hostSource.contains("onMovePreviousNightStep: () -> Unit"))
        assertFalse(hostSource.contains("nightStepIndex = currentStepIndex - 1"))
        assertTrue(hostSource.contains("onPrevious = onMovePreviousNightStep"))
    }
}
