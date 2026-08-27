package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4D temporary production-wiring RED.
 *
 * Mayor redirect legality remains owned by the existing typed Host/rules seam. This narrow guard
 * proves only that checkpoint-local redirect draft/confirmation transitions are delegated to
 * NightCheckpointReducer while App revision side effects remain at the existing boundary.
 * Retire this test once a callable integration seam supersedes it.
 */
class ClocktowerMayorRedirectReducerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val mayorDraftBlock = appSource
        .substringAfter("onSelectMayorRedirectTarget = {")
        .substringBefore("onConfirmMayorRedirectTarget = {")

    private val mayorConfirmBlock = appSource
        .substringAfter("onConfirmMayorRedirectTarget = {")
        .substringBefore("onSelectDemonSuccessor = {")

    @Test
    fun `Mayor redirect callbacks delegate checkpoint transitions to NightCheckpointReducer`() {
        assertTrue(mayorDraftBlock.contains("NightResolutionEvent.EditMayorRedirectDraft"))
        assertTrue(mayorDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertTrue(mayorConfirmBlock.contains("NightResolutionEvent.ConfirmMayorRedirect"))
        assertTrue(mayorConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `Compose no longer owns Mayor redirect draft or confirmation semantics`() {
        assertFalse(
            "Mayor redirect draft must be projected through the reducer rather than directly assigning " +
                "the raw callback value as checkpoint authority.",
            mayorDraftBlock.contains("clocktowerMayorRedirectTarget = it"),
        )
        assertFalse(
            "Confirmed Mayor redirect must come from the reduced checkpoint, not a parallel direct commit.",
            mayorConfirmBlock.contains(
                "clocktowerConfirmedMayorRedirectTarget = clocktowerMayorRedirectTarget",
            ),
        )
    }

    @Test
    fun `App revision side effects remain at the Mayor redirect callback boundary`() {
        assertTrue(
            "Mayor draft editing must continue advancing player-input revision at the App boundary.",
            mayorDraftBlock.contains("advanceClocktowerPlayerInputRevision()"),
        )
        assertTrue(
            "Changed Mayor confirmation must continue advancing game-state revision at the App boundary.",
            mayorConfirmBlock.contains("advanceClocktowerGameStateRevision()"),
        )
    }
}
