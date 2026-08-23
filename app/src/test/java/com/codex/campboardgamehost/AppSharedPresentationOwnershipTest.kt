package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppSharedPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val sharedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppSharedPresentation.kt",
    )
    private val settingsSource = File(
        "src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt",
    ).readText(Charsets.UTF_8)
    private val setupSource = File(
        "src/main/java/com/codex/campboardgamehost/AppPlayerSetupScreens.kt",
    ).readText(Charsets.UTF_8)
    private val reviewSource = File(
        "src/main/java/com/codex/campboardgamehost/AppGameReviewScreens.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `shared stateless app presentation has a dedicated owner`() {
        assertTrue(sharedFile.exists())
        val sharedSource = sharedFile.readText(Charsets.UTF_8)

        assertTrue(sharedSource.contains("internal fun GameSettingsHeader("))
        assertTrue(sharedSource.contains("internal fun EmptyStateCard("))
        assertTrue(sharedSource.contains("internal fun StepperRow("))
        assertTrue(sharedSource.contains("internal fun HostProgressCard("))
        assertTrue(sharedSource.contains("internal fun ClocktowerDarkTheme("))

        assertFalse(sharedSource.contains("remember {"))
        assertFalse(sharedSource.contains("mutableStateOf("))
        assertFalse(sharedSource.contains("MutableState"))
        assertFalse(sharedSource.contains("LaunchedEffect"))
        assertFalse(sharedSource.contains("DisposableEffect"))
        assertFalse(sharedSource.contains("SideEffect"))
        assertFalse(sharedSource.contains("JSONObject"))
        assertFalse(sharedSource.contains("Screen."))
        assertFalse(sharedSource.contains("fun evaluateGameOutcome("))
        assertFalse(sharedSource.contains("fun archiveCurrentGameForRestart("))
        assertFalse(sharedSource.contains("ClocktowerGameSession"))
        assertFalse(sharedSource.contains("ClocktowerFlowPlanner"))
        assertFalse(sharedSource.contains("ClocktowerNightStepMaterializerRegistry"))

        assertFalse(sharedSource.contains("fun HostScriptCard("))
        assertFalse(sharedSource.contains("fun HostInstructionBlock("))
        assertFalse(sharedSource.contains("fun HostActionSection("))
        assertFalse(sharedSource.contains("fun SelectablePlayerChips("))
        assertFalse(sharedSource.contains("fun SelectableSeatNumbers("))
        assertFalse(sharedSource.contains("fun WerewolfPlayerStatusRow("))
    }

    @Test
    fun `app root no longer declares shared presentation primitives`() {
        assertFalse(rootSource.contains("internal fun GameSettingsHeader("))
        assertFalse(rootSource.contains("internal fun EmptyStateCard("))
        assertFalse(rootSource.contains("internal fun StepperRow("))
        assertFalse(rootSource.contains("internal fun HostProgressCard("))
        assertFalse(rootSource.contains("internal fun ClocktowerDarkTheme("))
    }

    @Test
    fun `existing presentation owners continue consuming shared primitives`() {
        assertTrue(settingsSource.contains("EmptyStateCard("))
        assertTrue(setupSource.contains("EmptyStateCard("))
        assertTrue(setupSource.contains("ClocktowerDarkTheme("))
        assertTrue(reviewSource.contains("ClocktowerDarkTheme("))
    }

    @Test
    fun `S7 does not acquire app transaction effect persistence or host-specific ownership`() {
        assertTrue(rootSource.contains("private fun evaluateGameOutcome("))
        assertTrue(rootSource.contains("fun archiveCurrentGameForRestart(): Boolean"))
        assertTrue(rootSource.contains("DisposableEffect(lifecycleOwner)"))
        assertTrue(rootSource.contains("SideEffect {"))
        assertTrue(rootSource.contains("Screen.Game -> GameScreen("))
        assertTrue(rootSource.contains("fun startClocktowerGame()"))

        assertTrue(rootSource.contains("internal fun HostScriptCard("))
        assertTrue(rootSource.contains("internal fun HostInstructionBlock("))
        assertTrue(rootSource.contains("internal fun HostActionSection("))
        assertTrue(rootSource.contains("internal fun SelectablePlayerChips("))
        assertTrue(rootSource.contains("internal fun SelectableSeatNumbers("))
        assertTrue(rootSource.contains("internal fun WerewolfPlayerStatusRow("))
    }
}
