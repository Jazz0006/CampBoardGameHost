package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDemonSuccessorConfirmationWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App owns successor draft and confirmed state separately and persists confirmed checkpoint`() {
        assertTrue(
            appSource.contains("var clocktowerDemonSuccessorTarget by remember") &&
                appSource.contains("var clocktowerConfirmedDemonSuccessorTarget by remember"),
        )
        assertTrue(
            appSource.contains(
                "putNullableString(\"clocktowerConfirmedDemonSuccessorTarget\", clocktowerConfirmedDemonSuccessorTarget)",
            ),
        )
        assertTrue(
            appSource.contains(
                "confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget",
            ),
        )
        assertTrue(
            appSource.contains(
                "clocktowerConfirmedDemonSuccessorTarget = restoredNightCheckpoint.confirmedDemonSuccessorTarget",
            ),
        )
    }

    @Test
    fun `successor confirmation callback commits the exact resolved target`() {
        val successorBlock = appSource
            .substringAfter("onSelectDemonSuccessor = {")
            .substringBefore("onConfirmNewDemon = {")

        assertTrue(
            successorBlock.contains("onConfirmDemonSuccessorTarget = { selectedTarget ->") &&
                successorBlock.contains("clocktowerConfirmedDemonSuccessorTarget != selectedTarget") &&
                successorBlock.contains("clocktowerConfirmedDemonSuccessorTarget = selectedTarget"),
        )
    }

    @Test
    fun `reconfirming upstream Poison Monk or Demon attack invalidates successor confirmation`() {
        val poisonConfirmBlock = appSource
            .substringAfter("onConfirmPoisonTarget = {")
            .substringBefore("onSelectFortuneTellerFirst = {")
        val monkConfirmBlock = appSource
            .substringAfter("onConfirmMonkProtectedTarget = {")
            .substringBefore("onSelectMayorRedirectTarget = {")
        val attackConfirmBlock = appSource
            .substringAfter("onConfirmDemonAttack = {")
            .substringBefore("onSelectExecution = {")

        listOf(poisonConfirmBlock, monkConfirmBlock, attackConfirmBlock).forEach { block ->
            assertTrue(
                "Changing a confirmed upstream mechanical fact must invalidate the downstream successor confirmation.",
                block.contains("clocktowerConfirmedDemonSuccessorTarget = null"),
            )
        }
    }

    @Test
    fun `editing upstream drafts does not invalidate confirmed successor mechanics`() {
        val poisonDraftBlock = appSource
            .substringAfter("onSelectPoisonTarget = {")
            .substringBefore("onConfirmPoisonTarget = {")
        val monkDraftBlock = appSource
            .substringAfter("onSelectMonkProtectedTarget = {")
            .substringBefore("onConfirmMonkProtectedTarget = {")
        val attackDraftBlock = appSource
            .substringAfter("onSelectNightDeath = {")
            .substringBefore("onConfirmDemonAttack = {")

        listOf(poisonDraftBlock, monkDraftBlock, attackDraftBlock).forEach { block ->
            assertFalse(
                "Draft edits are not mechanical authority and must not clear a confirmed successor.",
                block.contains("clocktowerConfirmedDemonSuccessorTarget = null"),
            )
        }
    }
}
