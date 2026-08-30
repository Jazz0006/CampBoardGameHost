package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingCompletionRotationHistoryWiringTest {
    @Test
    fun `only a completed Trouble Brewing game records the committed setup provenance`() {
        val helper = functionBlock(appSource(), "fun persistCompletedTroubleBrewingSetupIfNeeded()")

        assertTrue(helper.contains("if (currentGameKind != GameKind.Clocktower) return true"))
        assertTrue(helper.contains("if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return true"))
        assertTrue(
            "A generic mid-game Restart must not authorize rotation-history insertion.",
            helper.contains("if (gameOutcome == null) return true"),
        )
        assertTrue(helper.contains("val selection = committedTroubleBrewingSetupSelection ?: return true"))
        assertTrue(helper.contains("TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)"))
        assertTrue(helper.contains(".recordCompletedGame("))
        assertTrue(helper.contains("gameId = clocktowerGameId"))
        assertTrue(helper.contains("selection = selection"))
    }

    @Test
    fun `restart archives only after completed setup history persistence succeeds`() {
        val archive = functionBlock(appSource(), "fun archiveCurrentGameForRestart(): Boolean")

        val completionPersistence = archive.indexOf("if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false")
        val reviewArchive = archive.indexOf("baseContext.archiveGame(activeGameSnapshotJson())")
        val activeSaveClear = archive.indexOf("clearSavedGameState()")

        assertTrue(
            "Restart must consult completion persistence before review/archive history is written.",
            completionPersistence >= 0 && completionPersistence < reviewArchive,
        )
        assertTrue(
            "A failed completion-history write must not clear the active save needed for retry.",
            completionPersistence < activeSaveClear,
        )
        assertFalse(
            "Generic archive must delegate completion semantics instead of directly recording every restart.",
            archive.contains("recordCompletedGame("),
        )
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
