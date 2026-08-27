package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4B temporary production-wiring RED.
 *
 * The App/Compose callback boundary is not directly callable from the JVM test layer yet, so this
 * narrow ownership guard proves only the Monk cut-over: checkpoint-local protection transition
 * semantics must be delegated to NightCheckpointReducer while durable action/timeline side effects
 * remain at the existing App transaction boundary. Retire this test once a callable integration
 * seam supersedes it.
 */
class ClocktowerMonkReducerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val monkDraftBlock = appSource
        .substringAfter("onSelectMonkProtectedTarget = {")
        .substringBefore("onConfirmMonkProtectedTarget = {")

    private val monkConfirmBlock = appSource
        .substringAfter("onConfirmMonkProtectedTarget = {")
        .substringBefore("onSelectMayorRedirectTarget = {")

    @Test
    fun `Monk callbacks delegate checkpoint transitions to NightCheckpointReducer`() {
        assertTrue(monkDraftBlock.contains("NightResolutionEvent.EditMonkProtectionDraft"))
        assertTrue(monkDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertTrue(monkConfirmBlock.contains("NightResolutionEvent.ConfirmMonkProtection"))
        assertTrue(monkConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `Compose no longer owns Monk confirmation or successor invalidation semantics`() {
        assertFalse(
            "Draft selection must be projected through the reducer rather than directly assigning " +
                "the raw callback value as checkpoint authority.",
            monkDraftBlock.contains("clocktowerMonkProtectedTarget = it"),
        )
        assertFalse(
            "Confirmed Monk protection must come from the reduced checkpoint, not a parallel direct commit.",
            monkConfirmBlock.contains(
                "clocktowerConfirmedMonkProtectedTarget = clocktowerMonkProtectedTarget",
            ),
        )
        assertFalse(
            "Dependent successor confirmation invalidation belongs to NightCheckpointReducer.",
            monkConfirmBlock.contains("clocktowerConfirmedDemonSuccessorTarget = null"),
        )
        assertFalse(
            "Changed upstream confirmation invalidates only the dependent confirmation; the editable " +
                "successor draft must remain available.",
            monkConfirmBlock.contains("clocktowerDemonSuccessorTarget = null"),
        )
    }

    @Test
    fun `durable Monk protection action commit remains at the App transaction boundary`() {
        assertTrue(
            "Reducer cut-over must not move durable Protect action/timeline ownership into the reducer.",
            monkConfirmBlock.contains("recordClocktowerAction(ActionFactDraft.Protect("),
        )
    }
}
