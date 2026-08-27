package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4E temporary production-wiring RED.
 *
 * Successor legality, required-selection semantics, same-night RoleChanged projection, and Dawn
 * materialization remain owned by their existing typed seams. This narrow guard proves only that
 * checkpoint-local successor draft/confirmation transitions are delegated to NightCheckpointReducer
 * while App revision side effects remain at the existing boundary.
 * Retire this test once a callable integration seam supersedes it.
 */
class ClocktowerDemonSuccessorReducerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val successorDraftBlock = appSource
        .substringAfter("onSelectDemonSuccessor = {")
        .substringBefore("onConfirmDemonSuccessorTarget = {")

    private val successorConfirmBlock = appSource
        .substringAfter("onConfirmDemonSuccessorTarget = {")
        .substringBefore("onConfirmNewDemon = {")

    @Test
    fun `Demon successor callbacks delegate checkpoint transitions to NightCheckpointReducer`() {
        assertTrue(successorDraftBlock.contains("NightResolutionEvent.EditDemonSuccessorDraft"))
        assertTrue(successorDraftBlock.contains("NightCheckpointReducer.reduce("))
        assertTrue(successorConfirmBlock.contains("NightResolutionEvent.ConfirmDemonSuccessor"))
        assertTrue(successorConfirmBlock.contains("NightCheckpointReducer.reduce("))
    }

    @Test
    fun `Compose no longer owns Demon successor draft or confirmation semantics`() {
        assertFalse(
            "Successor draft must be projected through the reducer rather than directly assigning " +
                "the raw callback value as checkpoint authority.",
            successorDraftBlock.contains("clocktowerDemonSuccessorTarget = it"),
        )
        assertFalse(
            "Confirmed successor must come from the reducer's checkpoint draft, not the transient " +
                "confirm callback argument.",
            successorConfirmBlock.contains("clocktowerConfirmedDemonSuccessorTarget = selectedTarget"),
        )
    }

    @Test
    fun `App revision side effects remain at the Demon successor callback boundary`() {
        assertTrue(
            "Successor draft editing must continue advancing player-input revision at the App boundary.",
            successorDraftBlock.contains("advanceClocktowerPlayerInputRevision()"),
        )
        assertTrue(
            "Changed successor confirmation must continue advancing game-state revision at the App boundary.",
            successorConfirmBlock.contains("advanceClocktowerGameStateRevision()"),
        )
    }
}
