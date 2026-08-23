package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPlayerDisplayUiOwnershipTest {
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
    private val playerDisplayUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt",
    )

    @Test
    fun `player display presentation has dedicated owner`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue("Dedicated player display UI source must exist", playerDisplayUi.isFile)

        val hostText = host.readText()
        val playerDisplayUiText = playerDisplayUi.readText()

        assertFalse(
            "ClocktowerPlayerDisplayCardLocalized must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun ClocktowerPlayerDisplayCardLocalized("),
        )
        assertFalse(
            "ClocktowerPlayerDisplayCard must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun ClocktowerPlayerDisplayCard("),
        )
        assertFalse(
            "EvilInfoDisplay must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun EvilInfoDisplay("),
        )
        assertTrue(
            "ClocktowerPlayerDisplayCardLocalized must be cross-file visible in the dedicated player display UI source",
            playerDisplayUiText.contains("internal fun ClocktowerPlayerDisplayCardLocalized("),
        )
        assertTrue(
            "ClocktowerPlayerDisplayCard must live in the dedicated player display UI source",
            playerDisplayUiText.contains("fun ClocktowerPlayerDisplayCard("),
        )
        assertTrue(
            "EvilInfoDisplay must live in the dedicated player display UI source",
            playerDisplayUiText.contains("fun EvilInfoDisplay("),
        )
    }
}
