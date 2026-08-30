package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production boundary guard for semantic-history persistence.
 *
 * Mode/cursor compatibility and history semantics are proved by typed persistence/session tests.
 * These checks retain only App save/restore/reset ownership that is not directly callable on JVM.
 */
class ActiveGameSemanticHistoryProductionWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App persists and validates semantic-history mode before restoring live state`() {
        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameStateIfNeeded()")
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(snapshot.contains("ClocktowerSemanticHistoryPersistence.encode(clocktowerSemanticHistoryMode)"))
        assertTrue(snapshot.contains("nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence"))

        val modeIndex = restore.indexOf("ClocktowerSemanticHistoryPersistence.decodeMode(json)")
        val compatibilityIndex = restore.indexOf("restoredSemanticHistoryMode.requireCompatible(")
        val mutationIndex = restore.indexOf("playerNames.clear()")
        assertTrue(modeIndex >= 0)
        assertTrue(compatibilityIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue(modeIndex < mutationIndex)
        assertTrue(compatibilityIndex < mutationIndex)
    }

    @Test
    fun `new Clocktower games enter global semantic history mode`() {
        val reset = source
            .substringAfter("fun resetDealState(")
            .substringBefore("fun startUndercoverGame()")

        assertTrue(reset.contains("nextGameKind == GameKind.Clocktower"))
        assertTrue(reset.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1"))
        assertTrue(reset.contains("clocktowerNextTimelineGlobalSequence = 0L"))
    }
}
