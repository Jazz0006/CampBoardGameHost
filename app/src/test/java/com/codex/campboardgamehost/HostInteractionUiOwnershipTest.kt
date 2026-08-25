package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostInteractionUiOwnershipTest {
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
    private val host = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    )
    private val interactionUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/HostInteractionUi.kt",
    )

    private val declarations = listOf(
        "HostProgressCard",
        "HostScriptCard",
        "HostInstructionBlock",
        "HostActionSection",
        "SelectablePlayerChips",
        "SelectableTwoPlayerChips",
        "SelectableSeatNumbers",
    )

    @Test
    fun `host interaction UI has dedicated owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", host.isFile)
        assertTrue("Dedicated host interaction UI source must exist", interactionUi.isFile)

        val hostText = host.readText()
        val interactionUiText = interactionUi.readText()

        declarations.forEach { declaration ->
            assertTrue(
                "$declaration must be declared in HostInteractionUi.kt",
                interactionUiText.contains("internal fun $declaration("),
            )
            assertFalse(
                "$declaration must no longer be declared in CampBoardGameHostApp.kt",
                hostText.contains("internal fun $declaration("),
            )
        }

        listOf(
            "remember(",
            "mutableStateOf(",
            "LaunchedEffect(",
            "DisposableEffect(",
            "SideEffect(",
        ).forEach { forbidden ->
            assertFalse(
                "HostInteractionUi.kt must not contain $forbidden",
                interactionUiText.contains(forbidden),
            )
        }
    }
}
