package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEvilInfoSquareTableWiringTest {
    @Test
    fun `first-night EvilInfo owns square-table routing instead of generic legacy result surface`() {
        val source = nightStepSource()

        assertTrue(
            source.contains(
                "val evilInfoSquareTablePresentation = clocktowerEvilInfoSquareTablePresentation(",
            ),
        )
        assertTrue(source.contains("val usesEvilInfoSquareTable = evilInfoSquareTablePresentation != null"))
        assertTrue(source.contains("ClocktowerEvilInfoSquareTableDialog("))
        assertTrue(source.contains("onShowPlayerDisplay = { onShowPlayerDisplay(step) }"))
        assertTrue(source.countOccurrences("!usesEvilInfoSquareTable &&") >= 3)
    }

    private fun nightStepSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerNightStepUi.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }

    private fun String.countOccurrences(needle: String): Int =
        windowed(size = needle.length, step = 1, partialWindows = false).count { it == needle }
}
