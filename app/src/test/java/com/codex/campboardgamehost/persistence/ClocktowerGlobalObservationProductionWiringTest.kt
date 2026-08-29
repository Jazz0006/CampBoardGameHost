package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production ownership guard for semantic observation history.
 *
 * Commit, deduplication, revision, durability, and replay semantics belong to typed session and
 * persistence tests. This file protects only the remaining non-callable App/Host wiring boundary.
 */
class ClocktowerGlobalObservationProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `new games use global history while restore preserves persisted history mode`() {
        assertTrue(appSource.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1"))
        assertTrue(appSource.contains("clocktowerSemanticHistoryMode = restoredSemanticHistoryMode"))
    }

    @Test
    fun `App routes observation drafts through canonical session authority while retaining legacy mode`() {
        assertTrue(appSource.contains("fun recordEpistemicObservation("))
        assertTrue(appSource.contains("draft: EpistemicObservationDraft"))
        assertTrue(appSource.contains("ClocktowerGameSession.commitGlobalEpistemicObservation("))
        assertTrue(appSource.contains("ClocktowerSemanticHistoryMode.LEGACY_LOCAL ->"))
        assertTrue(appSource.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1 ->"))
    }

    @Test
    fun `Host emits unbound observation drafts instead of assigning durable global identity`() {
        assertTrue(hostSource.contains("onRecordEpistemicObservation(EpistemicObservationDraft("))
        assertTrue(hostSource.contains("onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit"))
        assertFalse(hostSource.contains("onRecordEpistemicObservation: (RecordedEpistemicObservation) -> Unit"))
    }
}
