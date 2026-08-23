package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppGameScreenPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val extractedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppGameScreen.kt",
    )

    @Test
    fun `legacy game screen presentation has a dedicated owner`() {
        assertTrue(extractedFile.exists())
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertTrue(extractedSource.contains("internal fun GameScreen("))
        assertTrue(extractedSource.contains("private fun PlayerStatusRow("))

        assertFalse(extractedSource.contains("fun evaluateGameOutcome("))
        assertFalse(extractedSource.contains("records.add("))
        assertFalse(extractedSource.contains("cards[index] ="))
        assertFalse(extractedSource.contains("round += 1"))
        assertFalse(extractedSource.contains("selectedElimination ="))
        assertFalse(extractedSource.contains("gameOutcome ="))
        assertFalse(extractedSource.contains("showResults ="))
        assertFalse(extractedSource.contains("Screen.Game ->"))
        assertFalse(extractedSource.contains("LaunchedEffect"))
        assertFalse(extractedSource.contains("DisposableEffect"))
        assertFalse(extractedSource.contains("SideEffect"))
        assertFalse(extractedSource.contains("JSONObject"))
        assertFalse(extractedSource.contains("fun archiveCurrentGameForRestart("))
        assertFalse(extractedSource.contains("fun archiveAndReturnToPlayerManagement("))
        assertFalse(extractedSource.contains("fun archiveAndStartNewGame("))
    }

    @Test
    fun `app root routes to but no longer owns legacy game screen presentation`() {
        assertTrue(rootSource.contains("Screen.Game -> GameScreen("))

        assertFalse(rootSource.contains("private fun GameScreen("))
        assertFalse(rootSource.contains("internal fun GameScreen("))
        assertFalse(rootSource.contains("private fun PlayerStatusRow("))
        assertFalse(rootSource.contains("internal fun PlayerStatusRow("))
    }

    @Test
    fun `app root retains elimination outcome and round transaction ownership`() {
        assertTrue(rootSource.contains("var round by remember { mutableStateOf(1) }"))
        assertTrue(rootSource.contains("var selectedElimination by remember { mutableStateOf<String?>(null) }"))
        assertTrue(rootSource.contains("var showResults by remember { mutableStateOf(false) }"))
        assertTrue(rootSource.contains("var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }"))
        assertTrue(rootSource.contains("val cards = remember { mutableStateListOf<PlayerCard>() }"))
        assertTrue(rootSource.contains("val records = remember { mutableStateListOf<EliminationRecord>() }"))

        assertTrue(rootSource.contains("private fun evaluateGameOutcome("))
        assertTrue(rootSource.contains("cards[index] = cards[index].copy(eliminatedRound = round)"))
        assertTrue(rootSource.contains("records.add(EliminationRecord(round, name))"))
        assertTrue(rootSource.contains("selectedElimination = null"))
        assertTrue(rootSource.contains("gameOutcome = evaluateGameOutcome(context, cards, currentGameKind)"))
        assertTrue(rootSource.contains("showResults = true"))
        assertTrue(rootSource.contains("round += 1"))
    }

    @Test
    fun `S6 does not absorb shared app presentation or review ownership`() {
        assertTrue(rootSource.contains("internal fun GameSettingsHeader("))
        assertTrue(rootSource.contains("internal fun EmptyStateCard("))
        assertTrue(rootSource.contains("internal fun StepperRow("))
        assertTrue(rootSource.contains("internal fun HostProgressCard("))
        assertTrue(rootSource.contains("internal fun ClocktowerDarkTheme("))
        assertTrue(rootSource.contains("HostToolsTopBar("))
        assertTrue(rootSource.contains("ResultsDialog("))
        assertTrue(rootSource.contains("HostGameToolsScreen("))
    }
}
