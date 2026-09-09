package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRavenkeeperSquareTableWiringTest {
    @Test
    fun `ravenkeeper owns target and result flow on one specialized square table`() {
        val source = nightStepSource()

        assertTrue(source.contains("val usesRavenkeeperSquareTable = step.action == ClocktowerNightAction.Ravenkeeper && step.actor != null"))
        assertTrue(source.contains("clocktowerRavenkeeperResultChoices("))
        assertTrue(source.contains("ClocktowerRavenkeeperSquareTableDialog("))
        assertTrue(source.contains("val candidates = clocktowerRavenkeeperTargetCards(cards)"))
        assertTrue(source.contains("onConfirm = ::showRavenkeeperChoice"))
        assertFalse(source.contains("ClocktowerNightAction.DemonKill, ClocktowerNightAction.Ravenkeeper ->"))
        assertTrue(source.contains("!usesRavenkeeperSquareTable && !usesUndertakerSquareTable && !usesNumericSquareTable && nonPairResultFirstCandidates.isNotEmpty()"))
        assertTrue(source.contains("!usesRavenkeeperSquareTable &&\n                !usesUndertakerSquareTable &&\n                !usesNumericSquareTable &&\n                resultFirstRegistrationCandidates.isEmpty()"))
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
