package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerUndertakerSquareTableWiringTest {
    @Test
    fun `undertaker uses specialized square table and suppresses generic result surfaces`() {
        val source = nightStepSource()

        assertTrue(source.contains("step.roleEnName == \"Undertaker\" && step.actor != null"))
        assertTrue(source.contains("clocktowerUndertakerResultChoices("))
        assertTrue(source.contains("ClocktowerUndertakerSquareTableDialog("))
        assertTrue(source.contains("val usesUndertakerSquareTable = undertakerResultChoices.isNotEmpty()"))
        assertTrue(source.contains("!usesUndertakerSquareTable &&\n                !usesNumericSquareTable &&\n                resultFirstRegistrationCandidates.isEmpty()"))
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
