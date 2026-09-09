package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSpySquareTableWiringTest {
    @Test
    fun `Spy owns a read-only square table while keeping legacy reveal handoff`() {
        val source = nightStepSource()

        assertTrue(source.contains("val spySquareTablePresentation = clocktowerSpySquareTablePresentation(step)"))
        assertTrue(source.contains("val usesSpySquareTable = spySquareTablePresentation != null"))
        assertTrue(source.contains("ClocktowerSpySquareTableDialog("))
        assertTrue(source.contains("onShowLegacyReveal = { onShowPlayerDisplay(step) }"))
        assertTrue(source.contains("!usesSpySquareTable &&"))
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
}
