package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNewDemonPresentationOwnershipTest {
    private fun findRepositoryRoot(): File {
        val knownHostSource = "app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt"
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
    private val root = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    )
    private val nightScreen = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt",
    )
    private val hostScreen = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    )

    @Test
    fun `new Demon confirmation presentation has night screen owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", root.isFile)
        assertTrue("ClocktowerNightScreen.kt must exist", nightScreen.isFile)
        assertTrue("ClocktowerHostScreen.kt must exist", hostScreen.isFile)

        val rootText = root.readText()
        val nightScreenText = nightScreen.readText()

        assertTrue(
            "ClocktowerNewDemonConfirmationScreen must be declared in ClocktowerNightScreen.kt",
            nightScreenText.contains("internal fun ClocktowerNewDemonConfirmationScreen("),
        )
        assertFalse(
            "ClocktowerNewDemonConfirmationScreen must no longer be declared in CampBoardGameHostApp.kt",
            rootText.contains("internal fun ClocktowerNewDemonConfirmationScreen("),
        )
    }

    @Test
    fun `host retains new Demon state construction and callback wiring`() {
        val hostText = hostScreen.readText()
        val nightScreenText = nightScreen.readText()

        listOf(
            "pendingNewDemonName?.let { newDemonName ->",
            "val newDemon = cards.firstOrNull { it.name == newDemonName }",
            "val newDemonStep = ClocktowerNightStepUi(",
            "newDemonLabel = newDemon?.seatLabel(cards).orEmpty()",
            "hasNewDemon = newDemon != null",
            "onShowPlayerDisplay = { playerDisplayStep = newDemonStep }",
            "onConfirm = onConfirmNewDemon",
        ).forEach { wiring ->
            assertTrue("ClocktowerHostScreen.kt must retain $wiring", hostText.contains(wiring))
        }

        listOf(
            "pendingNewDemonName",
            "playerDisplayStep",
            "onConfirmNewDemon",
        ).forEach { authority ->
            assertFalse("ClocktowerNightScreen.kt must not own $authority", nightScreenText.contains(authority))
        }
    }
}
