package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerLegacyFallbackOwnershipTest {
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

    private val host = File(
        findRepositoryRoot(),
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    )

    @Test
    fun `active themed host UI remains present`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue(
            "Active ClocktowerDarkTheme UI must remain in ClocktowerHostScreen.kt",
            host.readText().contains("ClocktowerDarkTheme {"),
        )
    }

    @Test
    fun `unreachable legacy fallback is absent`() {
        assertTrue("Host source must exist", host.isFile)
        val hostText = host.readText()
        val returnThenLegacyLazyColumn = Regex("""(?s)\n    return\s*\n\s*LazyColumn\(""")

        assertFalse(
            "Unreachable legacy LazyColumn must not remain after the active UI return",
            returnThenLegacyLazyColumn.containsMatchIn(hostText),
        )
    }

    @Test
    fun `legacy info card is absent`() {
        assertTrue("Host source must exist", host.isFile)
        assertFalse(
            "ClocktowerInfoCard must be removed with its unreachable call sites",
            host.readText().contains("fun ClocktowerInfoCard("),
        )
    }
}
