package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSettingsUiOwnershipTest {
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
    private val settingsUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/GameSettingsUi.kt",
    )
    private val undercoverSupport = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/undercover/UndercoverSupport.kt",
    )
    private val werewolfHost = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt",
    )

    @Test
    fun `shared game settings UI has dedicated owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", host.isFile)
        assertTrue("GameSettingsUi.kt must exist", settingsUi.isFile)
        assertTrue("UndercoverSupport.kt must exist", undercoverSupport.isFile)
        assertTrue("WerewolfHostScreen.kt must exist", werewolfHost.isFile)

        val hostText = host.readText()
        val settingsUiText = settingsUi.readText()
        val undercoverText = undercoverSupport.readText()
        val werewolfText = werewolfHost.readText()

        listOf("GameSettingsHeader", "StepperRow").forEach { declaration ->
            assertTrue(
                "$declaration must be declared in GameSettingsUi.kt",
                settingsUiText.contains("internal fun $declaration("),
            )
            assertFalse(
                "$declaration must no longer be declared in CampBoardGameHostApp.kt",
                hostText.contains("internal fun $declaration("),
            )
        }

        assertTrue("UndercoverSupport.kt must call GameSettingsHeader", undercoverText.contains("GameSettingsHeader("))
        assertTrue("UndercoverSupport.kt must call StepperRow", undercoverText.contains("StepperRow("))
        assertTrue("WerewolfHostScreen.kt must call GameSettingsHeader", werewolfText.contains("GameSettingsHeader("))
        assertTrue("WerewolfHostScreen.kt must call StepperRow", werewolfText.contains("StepperRow("))

        assertTrue(
            "EmptyStateCard must remain declared in CampBoardGameHostApp.kt",
            hostText.contains("internal fun EmptyStateCard("),
        )
        assertFalse("EmptyStateCard must not be declared in GameSettingsUi.kt", settingsUiText.contains("fun EmptyStateCard("))

        listOf(
            "remember(",
            "mutableStateOf(",
            "LaunchedEffect(",
            "DisposableEffect(",
            "SideEffect(",
            "rememberUpdatedState(",
        ).forEach { forbidden ->
            assertFalse(
                "GameSettingsUi.kt must not contain $forbidden",
                settingsUiText.contains(forbidden),
            )
        }
    }
}
