package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4C temporary production-wiring RED.
 *
 * The App/Compose callback boundary is not directly callable from the JVM test layer yet, so this
 * narrow ownership guard proves only the Demon attack cut-over: checkpoint-local attack draft,
 * confirmation, and dependent successor invalidation semantics must be delegated to
 * NightCheckpointReducer while durable action/timeline side effects remain at the existing App
 * transaction boundary. Retire this test once a callable integration seam supersedes it.
 */
class ClocktowerDemonAttackReducerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val attackDraftBlock = appSource
        .substringAfter("onSelectNightDeath = { selected ->")
        .substringBefore("onConfirmDemonAttack = {")

    private val attackConfirmBlock = appSource
        .substringAfter("onConfirmDemonAttack = {")
        .substringBefore("onSelectExecution = {")

    @Test
    fun `Demon attack callbacks delegate checkpoint transitions to NightCheckpointReducer`() {
        assertTrue(attackDraftBlock.contains("NightResolutionEvent.EditDemonAttackDraft"))
        assertTrue(attackDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertTrue(attackConfirmBlock.contains("NightResolutionEvent.ConfirmDemonAttack"))
        assertTrue(attackConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `Compose no longer owns Demon attack confirmation or successor invalidation semantics`() {
        assertFalse(
            "Attack draft selection must be projected through the reducer rather than directly assigning " +
                "the raw callback value as checkpoint authority.",
            attackDraftBlock.contains("clocktowerDemonAttackDraftTarget = selected"),
        )
        assertFalse(
            "Editing the attack draft must not erase the editable successor draft outside the reducer.",
            attackDraftBlock.contains("clocktowerDemonSuccessorTarget = null"),
        )
        assertFalse(
            "Confirmed attack must come from the reduced checkpoint, not a parallel direct commit.",
            attackConfirmBlock.contains(
                "clocktowerPendingNightDeath = clocktowerDemonAttackDraftTarget",
            ),
        )
        assertFalse(
            "Dependent successor confirmation invalidation belongs to NightCheckpointReducer.",
            attackConfirmBlock.contains("clocktowerConfirmedDemonSuccessorTarget = null"),
        )
        assertFalse(
            "Changed upstream attack confirmation invalidates only the dependent confirmation; the editable " +
                "successor draft must remain available.",
            attackConfirmBlock.contains("clocktowerDemonSuccessorTarget = null"),
        )
    }

    @Test
    fun `durable Demon attack action commit remains at the App transaction boundary`() {
        assertTrue(
            "Reducer cut-over must not move durable Attack action/timeline ownership into the reducer.",
            attackConfirmBlock.contains("recordClocktowerAction(ActionFactDraft.Attack("),
        )
    }
}
