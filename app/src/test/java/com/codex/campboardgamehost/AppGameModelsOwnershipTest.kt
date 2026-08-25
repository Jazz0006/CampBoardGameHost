package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppGameModelsOwnershipTest {
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
    private val appGameModels = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt",
    )

    @Test
    fun `cross game value models have dedicated owner`() {
        assertTrue("CampBoardGameHostApp.kt must exist", root.isFile)
        assertTrue("AppGameModels.kt must exist", appGameModels.isFile)

        val rootText = root.readText()
        val appGameModelsText = appGameModels.readText()

        listOf(
            "internal enum class GameKind {",
            "internal enum class Role {",
            "internal data class PlayerCard(",
            "internal data class EliminationRecord(",
            "internal data class GameOutcome(",
            "internal data class SavedGamePreview(",
            "internal data class ArchivedGameReview(",
        ).forEach { declaration ->
            assertTrue("AppGameModels.kt must contain $declaration", appGameModelsText.contains(declaration))
            assertFalse("CampBoardGameHostApp.kt must not contain $declaration", rootText.contains(declaration))
        }
    }

    @Test
    fun `S8 one keeps non model responsibilities outside app game models`() {
        assertTrue("AppGameModels.kt must exist", appGameModels.isFile)
        val rootText = root.readText()
        val appGameModelsText = appGameModels.readText()

        listOf(
            "private enum class Screen {",
            "internal enum class LanguageMode(val prefsValue: String) {",
            "internal fun PlayerCard.abilitySubject(poisonTarget: String?): AbilitySubject",
        ).forEach { declaration ->
            assertTrue("CampBoardGameHostApp.kt must retain $declaration", rootText.contains(declaration))
            assertFalse("AppGameModels.kt must not contain $declaration", appGameModelsText.contains(declaration))
        }

        listOf(
            "internal enum class ClocktowerEventType {",
            "internal data class ClocktowerEvent(",
            "internal enum class ClocktowerTeam {",
        ).forEach { declaration ->
            assertFalse("AppGameModels.kt must not contain $declaration", appGameModelsText.contains(declaration))
        }

        listOf(
            "remember",
            "mutableStateOf",
            "LaunchedEffect",
            "DisposableEffect",
            "SideEffect",
            "JSONObject",
            "JSONArray",
            "getSharedPreferences",
            "ClocktowerGameSession",
        ).forEach { authority ->
            assertFalse("AppGameModels.kt must not contain $authority", appGameModelsText.contains(authority))
        }
    }
}
