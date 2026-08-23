package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppGameReviewPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val extractedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppGameReviewScreens.kt",
    )

    @Test
    fun `results host tools and history presentation cluster has a dedicated owner`() {
        assertTrue(extractedFile.exists())
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertTrue(extractedSource.contains("internal enum class HostToolTab"))
        assertTrue(extractedSource.contains("internal fun ResultsDialog("))
        assertTrue(extractedSource.contains("internal fun HostToolsTopBar("))
        assertTrue(extractedSource.contains("internal fun NewGameConfirmationDialog("))
        assertTrue(extractedSource.contains("internal fun HostGameToolsScreen("))
        assertTrue(extractedSource.contains("private fun HostRolesList("))
        assertTrue(extractedSource.contains("private fun HostRecordsList("))
        assertTrue(extractedSource.contains("private fun GameRecordRow("))
        assertTrue(extractedSource.contains("private fun ArchivedGameCard("))
        assertTrue(extractedSource.contains("private fun ArchivedGameReviewContent("))

        assertFalse(extractedSource.contains("data class ArchivedGameReview("))
        assertFalse(extractedSource.contains("fun evaluateGameOutcome("))
        assertFalse(extractedSource.contains("fun archiveCurrentGameForRestart("))
        assertFalse(extractedSource.contains("fun archiveAndReturnToPlayerManagement("))
        assertFalse(extractedSource.contains("fun archiveAndStartNewGame("))
        assertFalse(extractedSource.contains("var showResults by remember"))
        assertFalse(extractedSource.contains("var showHostTools by remember"))
        assertFalse(extractedSource.contains("var showNewGameConfirmation by remember"))
        assertFalse(extractedSource.contains("var gameHistory by remember"))
        assertFalse(extractedSource.contains("LaunchedEffect"))
        assertFalse(extractedSource.contains("DisposableEffect"))
        assertFalse(extractedSource.contains("SideEffect"))
        assertFalse(extractedSource.contains("JSONObject"))
        assertFalse(extractedSource.contains("ActiveGamePersistenceCoordinator"))
        assertFalse(extractedSource.contains("ClocktowerGameSession"))
        assertFalse(extractedSource.contains("ClocktowerFlowPlanner"))
        assertFalse(extractedSource.contains("ClocktowerNightStepMaterializerRegistry"))
        assertFalse(extractedSource.contains("fun ClocktowerResultsDialog("))
    }

    @Test
    fun `app root routes to but no longer owns review presentation cluster`() {
        assertTrue(rootSource.contains("HostToolsTopBar("))
        assertTrue(rootSource.contains("ResultsDialog("))
        assertTrue(rootSource.contains("HostGameToolsScreen("))
        assertTrue(rootSource.contains("NewGameConfirmationDialog("))

        assertFalse(rootSource.contains("private enum class HostToolTab"))
        assertFalse(rootSource.contains("internal enum class HostToolTab"))
        assertFalse(rootSource.contains("private fun ResultsDialog("))
        assertFalse(rootSource.contains("internal fun ResultsDialog("))
        assertFalse(rootSource.contains("private fun HostToolsTopBar("))
        assertFalse(rootSource.contains("internal fun HostToolsTopBar("))
        assertFalse(rootSource.contains("private fun NewGameConfirmationDialog("))
        assertFalse(rootSource.contains("internal fun NewGameConfirmationDialog("))
        assertFalse(rootSource.contains("private fun HostGameToolsScreen("))
        assertFalse(rootSource.contains("internal fun HostGameToolsScreen("))
        assertFalse(rootSource.contains("private fun HostRolesList("))
        assertFalse(rootSource.contains("private fun HostRecordsList("))
        assertFalse(rootSource.contains("private fun GameRecordRow("))
        assertFalse(rootSource.contains("private fun ArchivedGameCard("))
        assertFalse(rootSource.contains("private fun ArchivedGameReviewContent("))
    }

    @Test
    fun `app root retains review state archive and outcome transaction ownership`() {
        assertTrue(rootSource.contains("var gameHistory by remember"))
        assertTrue(rootSource.contains("var showHostTools by remember"))
        assertTrue(rootSource.contains("var hostToolTab by remember"))
        assertTrue(rootSource.contains("var showNewGameConfirmation by remember"))
        assertTrue(rootSource.contains("var showResults by remember"))
        assertTrue(rootSource.contains("var gameOutcome by remember"))

        assertTrue(rootSource.contains("internal data class ArchivedGameReview("))
        assertFalse(rootSource.contains("private data class ArchivedGameReview("))

        assertTrue(rootSource.contains("fun archiveCurrentGameForRestart(): Boolean"))
        assertTrue(rootSource.contains("fun archiveAndReturnToPlayerManagement()"))
        assertTrue(rootSource.contains("fun archiveAndStartNewGame()"))
        assertTrue(rootSource.contains("private fun evaluateGameOutcome("))
        assertTrue(rootSource.contains("gameHistory = baseContext.archiveGame(activeGameSnapshotJson())"))
        assertTrue(rootSource.contains("onManagePlayers = ::archiveAndReturnToPlayerManagement"))
        assertTrue(rootSource.contains("onQuickRestart = ::archiveAndStartNewGame"))

        assertTrue(rootSource.contains("records.add(EliminationRecord(round, name))"))
        assertTrue(rootSource.contains("gameOutcome = evaluateGameOutcome(context, cards, currentGameKind)"))
        assertTrue(rootSource.contains("round += 1"))
    }

    @Test
    fun `shared review dependencies remain outside S5 owner`() {
        assertTrue(rootSource.contains("internal fun PlayerCard.hostRoleLabel("))
        assertTrue(rootSource.contains("internal fun Role.labelResId(): Int = when (this)"))
        assertTrue(rootSource.contains("internal fun ClocktowerRole.nameFor(language: String): String"))
        assertTrue(rootSource.contains("internal fun ClocktowerDarkTheme("))
        assertTrue(rootSource.contains("private fun GameScreen("))
        assertTrue(rootSource.contains("private fun PlayerStatusRow("))
        assertTrue(rootSource.contains("ClocktowerResultsDialog("))
    }
}
