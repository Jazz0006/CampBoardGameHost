package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerAppModelsOwnershipTest {
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
    private val clocktowerAppModels = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt",
    )
    private val appGameModels = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt",
    )

    private val declarations = listOf(
        "internal enum class ClocktowerEventType {",
        "internal data class ClocktowerEvent(",
        "internal enum class ClocktowerTeam {",
        "internal enum class ClocktowerPhase {",
        "internal enum class ClocktowerDayMode {",
        "internal enum class ClocktowerNightAction {",
        "internal enum class ClocktowerDisplayKind {",
        "internal enum class ClocktowerScript {",
        "internal data class ClocktowerRole(",
    )

    @Test
    fun `Clocktower app value models have dedicated owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", root.isFile)
        assertTrue("ClocktowerAppModels.kt must exist", clocktowerAppModels.isFile)

        val rootText = root.readText()
        val ownerText = clocktowerAppModels.readText()

        declarations.forEach { declaration ->
            assertTrue("ClocktowerAppModels.kt must contain $declaration", ownerText.contains(declaration))
            assertFalse("CampBoardGameHostApp.kt must not contain $declaration", rootText.contains(declaration))
        }
    }

    @Test
    fun `Clocktower app model owner contains no state or persistence authority`() {
        assertTrue("ClocktowerAppModels.kt must exist", clocktowerAppModels.isFile)
        val ownerText = clocktowerAppModels.readText()

        listOf(
            "remember(",
            "mutableStateOf(",
            "LaunchedEffect(",
            "DisposableEffect(",
            "SideEffect(",
            "JSONObject",
            "JSONArray",
            "SharedPreferences",
            "getSharedPreferences",
            "ClocktowerGameSession",
            "loadActiveGameState",
            "saveActiveGameState",
            "assignment",
            "catalog",
        ).forEach { forbidden ->
            assertFalse("ClocktowerAppModels.kt must not contain $forbidden", ownerText.contains(forbidden))
        }
    }

    @Test
    fun `Clocktower app models stay out of cross game app model owner`() {
        assertTrue("AppGameModels.kt must exist", appGameModels.isFile)
        val appGameModelsText = appGameModels.readText()

        declarations.forEach { declaration ->
            assertFalse("AppGameModels.kt must not contain $declaration", appGameModelsText.contains(declaration))
        }
    }
}
