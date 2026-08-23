package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightStepUiOwnershipTest {
    private fun findRepositoryRoot(): File {
        val knownHostSource = "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
        val workingDirectory = System.getProperty("user.dir") ?: error("Working directory is unavailable")
        var directory = File(workingDirectory).absoluteFile
        while (true) {
            if (File(directory, knownHostSource).isFile) return directory
            val parent = directory.parentFile ?: error("Repository root not found from ${directory.path}")
            if (parent == directory) error("Repository root not found from ${directory.path}")
            directory = parent
        }
    }

    private val repoRoot = findRepositoryRoot()
    private val host = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    )
    private val nightStepUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    )

    @Test
    fun `night step presentation has dedicated owner`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue("Dedicated night-step UI source must exist", nightStepUi.isFile)

        val hostText = host.readText()
        val nightStepUiText = nightStepUi.readText()

        assertFalse(
            "ClocktowerNightStepCardLocalized must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun ClocktowerNightStepCardLocalized("),
        )
        assertFalse(
            "ClocktowerInfoCard must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun ClocktowerInfoCard("),
        )
        assertTrue(
            "ClocktowerNightStepCardLocalized must be cross-file visible in the dedicated night-step UI source",
            nightStepUiText.contains("internal fun ClocktowerNightStepCardLocalized("),
        )
        assertTrue(
            "ClocktowerInfoCard must remain a private helper in the dedicated night-step UI source",
            nightStepUiText.contains("private fun ClocktowerInfoCard("),
        )
    }
}
