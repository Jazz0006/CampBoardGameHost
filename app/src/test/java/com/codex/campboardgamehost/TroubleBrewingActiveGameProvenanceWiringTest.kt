package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TroubleBrewingActiveGameProvenanceWiringTest {
    @Test
    fun `curated Trouble Brewing start locks the selected setup provenance`() {
        val helper = functionBlock(appSource(), "fun startTroubleBrewingGame()")

        assertTrue(
            "The exact selector-owned setup must become active-game state when the curated deal commits.",
            helper.contains("committedTroubleBrewingSetupSelection = preparedSetup.selection"),
        )
    }

    @Test
    fun `active game snapshot persists the committed Trouble Brewing setup provenance`() {
        val snapshot = functionBlock(appSource(), "fun activeGameSnapshotJson()")

        assertTrue(snapshot.contains("committedTroubleBrewingSetupSelection"))
        assertTrue(snapshot.contains("TroubleBrewingSetupProvenancePersistence.ROOT_KEY"))
        assertTrue(snapshot.contains("TroubleBrewingSetupProvenancePersistence.encode("))
    }

    @Test
    fun `saved Trouble Brewing game restores exact setup provenance without rerunning selection`() {
        val restore = functionBlock(appSource(), "fun restoreSavedGame()")

        assertTrue(
            "Restore must decode the persisted preset identity against the frozen dataset.",
            restore.contains("TroubleBrewingSetupProvenancePersistence.decodeOrNull("),
        )
        assertTrue(
            "Restore must rebind the decoded selection into active-game state.",
            restore.contains("committedTroubleBrewingSetupSelection = restoredTroubleBrewingSetupSelection"),
        )
        assertFalse(
            "Restoring an existing game must not select or materialize a fresh preset.",
            restore.contains("TroubleBrewingProductionSetupPreparer.prepare("),
        )
        assertFalse(
            "Restoring an existing game must not invoke the preset selector directly.",
            restore.contains("TroubleBrewingSetupPresetSelector"),
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
