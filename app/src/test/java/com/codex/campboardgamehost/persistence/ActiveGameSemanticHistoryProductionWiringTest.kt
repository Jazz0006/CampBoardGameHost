package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ActiveGameSemanticHistoryProductionWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production save persists explicit semantic history mode and reuses existing cursor key`() {
        assertTrue(
            source.contains(
                "var clocktowerSemanticHistoryMode by remember { mutableStateOf(ClocktowerSemanticHistoryMode.LEGACY_LOCAL) }",
            ),
        )
        assertTrue(
            source.contains(
                "var clocktowerNextTimelineGlobalSequence by remember { mutableStateOf(0L) }",
            ),
        )

        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameStateIfNeeded()")

        assertTrue(snapshot.contains("ClocktowerSemanticHistoryPersistence.MODE_KEY"))
        assertTrue(snapshot.contains("ClocktowerSemanticHistoryPersistence.encode(clocktowerSemanticHistoryMode)"))
        assertTrue(snapshot.contains("nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence"))
        assertFalse(snapshot.contains("semanticTimelineCursor"))
    }

    @Test
    fun `restore validates mode cursor and observation history before mutating live state`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(restore.contains("val restoredSemanticHistoryMode ="))
        assertTrue(restore.contains("ClocktowerSemanticHistoryPersistence.decodeMode(json)"))
        assertTrue(restore.contains("val restoredClocktowerEpistemicObservations ="))
        assertTrue(restore.contains("\"clocktowerNextTimelineGlobalSequence\""))
        assertTrue(restore.contains("restoredSemanticHistoryMode.requireCompatible("))

        val modeIndex = restore.indexOf("val restoredSemanticHistoryMode =")
        val observationIndex = restore.indexOf("val restoredClocktowerEpistemicObservations =")
        val checkpointIndex = restore.indexOf("val restoredNightCheckpoint =")
        val compatibilityIndex = restore.indexOf("restoredSemanticHistoryMode.requireCompatible(")
        val mutationIndex = restore.indexOf("playerNames.clear()")

        assertTrue(modeIndex >= 0)
        assertTrue(observationIndex >= 0)
        assertTrue(checkpointIndex >= 0)
        assertTrue(compatibilityIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue(modeIndex < mutationIndex)
        assertTrue(observationIndex < mutationIndex)
        assertTrue(checkpointIndex < mutationIndex)
        assertTrue(compatibilityIndex < mutationIndex)

        assertTrue(
            restore.contains(
                "clocktowerSemanticHistoryMode = restoredSemanticHistoryMode",
            ),
        )
        assertTrue(
            restore.contains(
                "clocktowerNextTimelineGlobalSequence = restoredNightCheckpoint.nextTimelineGlobalSequence",
            ),
        )
    }

    @Test
    fun `new and non Clocktower games stay legacy until the later producer cutover`() {
        val reset = source
            .substringAfter("fun resetDealState(")
            .substringBefore("fun startUndercoverGame()")

        assertTrue(
            reset.contains(
                "clocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL",
            ),
        )
        assertTrue(reset.contains("clocktowerNextTimelineGlobalSequence = 0L"))
        assertFalse(reset.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1"))
    }
}
