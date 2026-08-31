package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Coarse App-root provenance guard; codec semantics are owned by typed persistence tests. */
class TroubleBrewingActiveGameProvenanceWiringTest {
    @Test
    fun `curated Trouble Brewing start commits selected provenance and snapshot uses canonical codec`() {
        val start = functionBlock(appSource(), "fun startTroubleBrewingGame()")
        val snapshot = functionBlock(appSource(), "fun activeGameSnapshotJson()")

        assertTrue(start.contains("committedTroubleBrewingSetupSelection"))
        assertTrue(start.contains("preparedSetup.selection"))
        assertTrue(snapshot.contains("TroubleBrewingSetupProvenancePersistence.encode("))
    }

    @Test
    fun `curated Trouble Brewing start durably saves provenance after committing selection`() {
        val start = functionBlock(appSource(), "fun startTroubleBrewingGame()")
        val commitIndex = start.indexOf(
            "committedTroubleBrewingSetupSelection = preparedSetup.selection",
        )
        val persistIndex = start.indexOf(
            "persistActiveGameStateIfNeeded()",
            startIndex = commitIndex.coerceAtLeast(0),
        )

        assertTrue(commitIndex >= 0)
        assertTrue(persistIndex > commitIndex)
    }

    @Test
    fun `restore consumes canonical provenance without rerunning setup selection`() {
        val restore = functionBlock(appSource(), "fun restoreSavedGame()")

        assertTrue(restore.contains("TroubleBrewingSetupProvenancePersistence.decodeOrNull("))
        assertTrue(restore.contains("committedTroubleBrewingSetupSelection"))
        assertFalse(restore.contains("TroubleBrewingProductionSetupPreparer.prepare("))
        assertFalse(restore.contains("TroubleBrewingSetupPresetSelector"))
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
