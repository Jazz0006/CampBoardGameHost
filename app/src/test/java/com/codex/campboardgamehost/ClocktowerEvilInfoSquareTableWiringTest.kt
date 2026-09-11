package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEvilInfoSquareTableWiringTest {
    @Test
    fun `first-night EvilInfo exclusively owns its square-table routing`() {
        val source = nightStepSource()

        assertTrue(
            source.contains(
                "val evilInfoSquareTablePresentation = clocktowerEvilInfoSquareTablePresentation(",
            ),
        )
        assertTrue(source.contains("val usesEvilInfoSquareTable = evilInfoSquareTablePresentation != null"))
        assertTrue(source.contains("ClocktowerEvilInfoSquareTableDialog("))
        assertTrue(source.contains("onShowPlayerDisplay = { onShowPlayerDisplay(step) }"))

        val plainFallback = source.substringAfter("val plainInformationDisplayStep =")
            .substringBefore("val plainInformationSquareTablePresentation =")
        assertTrue(plainFallback.contains("!usesEvilInfoSquareTable"))
        assertFalse(source.contains("推荐给说书人的完整信息"))
        assertFalse(source.contains("This ability is unreliable. Choose a result to show."))
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
