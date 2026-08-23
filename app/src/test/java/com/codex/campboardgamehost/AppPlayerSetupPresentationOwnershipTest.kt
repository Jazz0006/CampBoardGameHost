package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppPlayerSetupPresentationOwnershipTest {
    private val appFile = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    )
    private val setupPresentationFile = File(
        "src/main/java/com/codex/campboardgamehost/AppPlayerSetupScreens.kt",
    )

    @Test
    fun `player setup presentation has a dedicated source owner`() {
        assertTrue(
            "S1 requires AppPlayerSetupScreens.kt before production extraction can be GREEN.",
            setupPresentationFile.exists(),
        )

        val source = setupPresentationFile.readText(Charsets.UTF_8)
        assertTrue(source.contains("internal fun SetupScreen("))
        assertTrue(source.contains("private fun RoundTableSetupEditor("))
        assertTrue(source.contains("private sealed class DraggedPlayer"))
        assertTrue(source.contains("private data class PlayerDragState("))
    }

    @Test
    fun `app root no longer owns player setup leaf implementations`() {
        val source = appFile.readText(Charsets.UTF_8)

        assertFalse(source.contains("private fun SetupScreen("))
        assertFalse(source.contains("private fun RoundTableSetupEditor("))
        assertFalse(source.contains("private sealed class DraggedPlayer"))
        assertFalse(source.contains("private data class PlayerDragState("))
    }

    @Test
    fun `app root retains session navigation and player mutation ownership`() {
        val source = appFile.readText(Charsets.UTF_8)

        assertTrue(source.contains("val playerNames = remember { mutableStateListOf<String>() }"))
        assertTrue(source.contains("fun addCurrentPlayer(name: String)"))
        assertTrue(source.contains("fun removeCurrentPlayer(index: Int)"))
        assertTrue(source.contains("fun moveCurrentPlayerTo(index: Int, insertIndex: Int)"))
        assertTrue(source.contains("fun restoreSavedGame()"))
        assertTrue(source.contains("fun startClocktowerGame()"))
        assertTrue(source.contains("Screen.Setup -> SetupScreen("))
    }
}
