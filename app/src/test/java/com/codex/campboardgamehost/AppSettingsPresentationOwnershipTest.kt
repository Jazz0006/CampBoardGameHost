package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppSettingsPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val extractedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt",
    )

    @Test
    fun `app settings screen has a dedicated presentation owner`() {
        assertTrue(extractedFile.exists())
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertTrue(extractedSource.contains("internal fun SettingsScreen("))
        assertFalse(extractedSource.contains("enum class LanguageMode"))
        assertFalse(extractedSource.contains("fun LanguageMode.labelResId"))
        assertFalse(extractedSource.contains("fun addCommonPlayer("))
        assertFalse(extractedSource.contains("fun removeCommonPlayer("))
        assertFalse(extractedSource.contains("saveLanguageMode("))
        assertFalse(extractedSource.contains("saveStorytellerAutomationMode("))
        assertFalse(extractedSource.contains("saveCommonPlayers("))
    }

    @Test
    fun `app root routes to but no longer owns the settings screen`() {
        assertTrue(rootSource.contains("Screen.Settings -> SettingsScreen("))
        assertFalse(rootSource.contains("private fun SettingsScreen("))
        assertFalse(rootSource.contains("internal fun SettingsScreen("))
    }

    @Test
    fun `app root retains settings state mutation persistence and language ownership`() {
        assertTrue(rootSource.contains("var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }"))
        assertTrue(rootSource.contains("var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }"))
        assertTrue(rootSource.contains("var newCommonPlayerName by remember { mutableStateOf(\"\") }"))
        assertTrue(rootSource.contains("val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }"))
        assertTrue(rootSource.contains("fun addCommonPlayer()"))
        assertTrue(rootSource.contains("fun removeCommonPlayer(name: String)"))
        assertTrue(rootSource.contains("baseContext.saveLanguageMode(nextMode)"))
        assertTrue(rootSource.contains("baseContext.saveStorytellerAutomationMode(mode)"))
        assertTrue(rootSource.contains("baseContext.saveCommonPlayers(commonPlayers)"))
        assertTrue(rootSource.contains("internal enum class LanguageMode"))
        assertTrue(rootSource.contains("internal fun LanguageMode.labelResId(): Int"))
    }

    @Test
    fun `shared settings primitives remain outside S2 owner`() {
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertFalse(extractedSource.contains("fun GameSettingsHeader("))
        assertFalse(extractedSource.contains("fun EmptyStateCard("))
        assertFalse(extractedSource.contains("fun StepperRow("))
    }
}
