package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Coarse App-root completion-transaction guard; store semantics are owned by typed persistence tests. */
class TroubleBrewingCompletionRotationHistoryWiringTest {
    @Test
    fun `only completed Trouble Brewing with committed completion record reaches rotation history`() {
        val helper = functionBlock(appSource(), "fun persistCompletedTroubleBrewingSetupIfNeeded()")

        assertTrue(helper.contains("if (currentGameKind != GameKind.Clocktower) return true"))
        assertTrue(helper.contains("if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return true"))
        assertTrue(helper.contains("if (gameOutcome == null) return true"))
        assertTrue(helper.contains("committedTroubleBrewingSetupRotationRecord ?: return true"))
        assertTrue(helper.contains(".recordCompletedGame("))
        assertTrue(helper.contains("record = record"))
    }

    @Test
    fun `restart persists completion history before archive or active-save clearing`() {
        val archive = functionBlock(appSource(), "fun archiveCurrentGameForRestart(): Boolean")

        val completionPersistence = archive.indexOf("persistCompletedTroubleBrewingSetupIfNeeded()")
        val reviewArchive = archive.indexOf("baseContext.archiveGame(activeGameSnapshotJson())")
        val activeSaveClear = archive.indexOf("clearSavedGameState()")

        assertTrue(completionPersistence >= 0 && completionPersistence < reviewArchive)
        assertTrue(completionPersistence < activeSaveClear)
        assertFalse(archive.contains("recordCompletedGame("))
    }

    private fun appSource(): String = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private fun functionBlock(source: String, signature: String): String {
        val start = source.indexOf(signature)
        require(start >= 0) { "Missing source function '$signature'." }
        val openingBrace = source.indexOf('{', start)
        require(openingBrace >= 0) { "Missing opening brace for '$signature'." }
        var depth = 0
        for (index in openingBrace until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) return source.substring(start, index + 1)
                }
            }
        }
        error("Unclosed source function '$signature'.")
    }
}
