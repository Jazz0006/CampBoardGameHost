package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerClockmakerSquareTableWiringTest {
    @Test
    fun `Clockmaker owns result presentation inside square table`() {
        val source = nightStepSource()

        assertTrue(source.contains("val clockmakerResultChoices = clocktowerClockmakerResultChoices("))
        assertTrue(source.contains("val usesClockmakerSquareTable = clockmakerResultChoices.isNotEmpty()"))
        assertTrue(source.contains("ClocktowerClockmakerSquareTableDialog("))
        assertTrue(source.contains("fun showClockmakerChoice(choice: ClocktowerClockmakerResultChoice)"))
        assertTrue(source.contains("!usesClockmakerSquareTable &&"))
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
