package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRegistrationUiOwnershipTest {
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
    private val registrationUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationUi.kt",
    )

    @Test
    fun `special registration presentation has dedicated owner`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue("Dedicated registration UI source must exist", registrationUi.isFile)

        val hostText = host.readText()
        val registrationUiText = registrationUi.readText()

        assertFalse(
            "SpyRegistrationPanel must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun SpyRegistrationPanel("),
        )
        assertFalse(
            "RecluseRegistrationPanel must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun RecluseRegistrationPanel("),
        )
        assertTrue(
            "SpyRegistrationPanel must be cross-file visible in the dedicated registration UI source",
            registrationUiText.contains("internal fun SpyRegistrationPanel("),
        )
        assertTrue(
            "RecluseRegistrationPanel must be cross-file visible in the dedicated registration UI source",
            registrationUiText.contains("internal fun RecluseRegistrationPanel("),
        )
    }
}
