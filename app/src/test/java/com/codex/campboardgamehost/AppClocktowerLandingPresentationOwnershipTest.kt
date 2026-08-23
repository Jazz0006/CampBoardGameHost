package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppClocktowerLandingPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val extractedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppClocktowerLandingScreen.kt",
    )

    @Test
    fun `clocktower landing screen has a dedicated presentation owner`() {
        assertTrue(extractedFile.exists())
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertTrue(extractedSource.contains("internal fun ClocktowerLandingScreen("))
        assertFalse(extractedSource.contains("savedGamePreview"))
        assertFalse(extractedSource.contains("fun restoreSavedGame()"))
        assertFalse(extractedSource.contains("loadActiveGameStateJson("))
        assertFalse(extractedSource.contains("Screen.Landing"))
        assertFalse(extractedSource.contains("Screen.Setup"))
    }

    @Test
    fun `app root routes to but no longer owns the clocktower landing screen`() {
        assertTrue(rootSource.contains("Screen.Landing -> ClocktowerLandingScreen("))
        assertTrue(rootSource.contains("hasSavedGame = savedGamePreview != null"))
        assertTrue(rootSource.contains("onStartGame = { screen = Screen.Setup }"))
        assertTrue(rootSource.contains("onContinueGame = ::restoreSavedGame"))
        assertFalse(rootSource.contains("private fun ClocktowerLandingScreen("))
        assertFalse(rootSource.contains("internal fun ClocktowerLandingScreen("))
    }

    @Test
    fun `app root retains landing state and persistence ownership`() {
        assertTrue(rootSource.contains("var screen by remember { mutableStateOf(Screen.Landing) }"))
        assertTrue(rootSource.contains("var savedGamePreview by remember(context) { mutableStateOf(baseContext.loadSavedGamePreview(context)) }"))
        assertTrue(rootSource.contains("fun restoreSavedGame()"))
        assertTrue(rootSource.contains("fun clearSavedGameState()"))
        assertTrue(rootSource.contains("baseContext.loadActiveGameStateJson()"))
    }

    @Test
    fun `landing extraction does not acquire clocktower flow or lifecycle ownership`() {
        if (!extractedFile.exists()) return
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertFalse(extractedSource.contains("ClocktowerFlowPlanner"))
        assertFalse(extractedSource.contains("ClocktowerHostInteractionProjector"))
        assertFalse(extractedSource.contains("ClocktowerNightStepMaterializerRegistry"))
        assertFalse(extractedSource.contains("ClocktowerGameSession"))
        assertFalse(extractedSource.contains("LaunchedEffect"))
        assertFalse(extractedSource.contains("DisposableEffect"))
        assertFalse(extractedSource.contains("SideEffect"))
    }
}
