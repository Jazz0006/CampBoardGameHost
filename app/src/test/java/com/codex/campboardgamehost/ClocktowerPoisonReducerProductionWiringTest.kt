package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4A temporary production-wiring RED.
 *
 * The App/Compose callback boundary is not directly callable from the JVM test layer yet, so this
 * narrow ownership guard proves only the cut-over: checkpoint-local Poison transition semantics
 * must be delegated to NightCheckpointReducer while durable action/timeline side effects remain
 * at the existing App transaction boundary. Retire this test once a callable integration seam
 * supersedes it.
 */
class ClocktowerPoisonReducerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val poisonDraftBlock = appSource
        .substringAfter("onSelectPoisonTarget = {")
        .substringBefore("onConfirmPoisonTarget = {")

    private val poisonConfirmBlock = appSource
        .substringAfter("onConfirmPoisonTarget = {")
        .substringBefore("onSelectFortuneTellerFirst = {")

    @Test
    fun `Poison callbacks delegate checkpoint transitions to NightCheckpointReducer`() {
        assertTrue(
            "SNE-7.4A requires the production App to consume the typed reducer seam.",
            appSource.contains(
                "import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer",
            ),
        )
        assertTrue(
            "SNE-7.4A requires typed transient NightResolutionEvent commands.",
            appSource.contains(
                "import com.codex.campboardgamehost.clocktower.session.NightResolutionEvent",
            ),
        )
        assertTrue(poisonDraftBlock.contains("NightResolutionEvent.EditPoisonDraft"))
        assertTrue(poisonDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertTrue(poisonConfirmBlock.contains("NightResolutionEvent.ConfirmPoison"))
        assertTrue(poisonConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `Compose no longer owns Poison confirmation or successor invalidation semantics`() {
        assertFalse(
            "Draft selection must be projected through the reducer rather than directly assigning " +
                "the raw callback value as checkpoint authority.",
            poisonDraftBlock.contains("clocktowerPoisonTarget = it"),
        )
        assertFalse(
            "Confirmed Poison must come from the reduced checkpoint, not a parallel direct commit.",
            poisonConfirmBlock.contains("clocktowerConfirmedPoisonTarget = clocktowerPoisonTarget"),
        )
        assertFalse(
            "Dependent successor confirmation invalidation belongs to NightCheckpointReducer.",
            poisonConfirmBlock.contains("clocktowerConfirmedDemonSuccessorTarget = null"),
        )
        assertFalse(
            "A changed upstream confirmation invalidates the dependent confirmation, not the editable " +
                "successor draft; the reducer preserves that draft.",
            poisonConfirmBlock.contains("clocktowerDemonSuccessorTarget = null"),
        )
    }

    @Test
    fun `durable Poison action commit remains at the App transaction boundary`() {
        assertTrue(
            "Reducer cut-over must not move durable ActionFact/timeline ownership into the reducer.",
            poisonConfirmBlock.contains("recordClocktowerAction(ActionFactDraft.Poison("),
        )
    }
}
