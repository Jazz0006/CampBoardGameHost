package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDealPresentationOwnershipTest {
    private val rootSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val extractedFile = File(
        "src/main/java/com/codex/campboardgamehost/AppDealScreens.kt",
    )

    @Test
    fun `deal and reveal presentation cluster has a dedicated owner`() {
        assertTrue(extractedFile.exists())
        val extractedSource = extractedFile.readText(Charsets.UTF_8)

        assertTrue(extractedSource.contains("internal fun PassPhoneScreen("))
        assertTrue(extractedSource.contains("internal fun RevealCardScreen("))
        assertTrue(extractedSource.contains("private fun ClocktowerDealHandoffScreen("))
        assertTrue(extractedSource.contains("private fun ClocktowerPlayerRoleRevealScreen("))
        assertTrue(extractedSource.contains("private fun FullScreenColumn("))

        assertFalse(extractedSource.contains("var currentDealIndex"))
        assertFalse(extractedSource.contains("Screen.PassPhone"))
        assertFalse(extractedSource.contains("Screen.RevealCard"))
        assertFalse(extractedSource.contains("LaunchedEffect"))
        assertFalse(extractedSource.contains("DisposableEffect"))
        assertFalse(extractedSource.contains("SideEffect"))
        assertFalse(extractedSource.contains("ClocktowerGameSession"))
        assertFalse(extractedSource.contains("ClocktowerFlowPlanner"))
        assertFalse(extractedSource.contains("ClocktowerNightStepMaterializerRegistry"))
        assertFalse(extractedSource.contains("fun Role.labelResId()"))
        assertFalse(extractedSource.contains("fun ClocktowerRole.descriptionFor("))
    }

    @Test
    fun `app root routes to but no longer owns deal presentation cluster`() {
        assertTrue(rootSource.contains("Screen.PassPhone -> PassPhoneScreen("))
        assertTrue(rootSource.contains("Screen.RevealCard -> RevealCardScreen("))

        assertFalse(rootSource.contains("private fun PassPhoneScreen("))
        assertFalse(rootSource.contains("internal fun PassPhoneScreen("))
        assertFalse(rootSource.contains("private fun RevealCardScreen("))
        assertFalse(rootSource.contains("internal fun RevealCardScreen("))
        assertFalse(rootSource.contains("private fun ClocktowerDealHandoffScreen("))
        assertFalse(rootSource.contains("internal fun ClocktowerDealHandoffScreen("))
        assertFalse(rootSource.contains("private fun ClocktowerPlayerRoleRevealScreen("))
        assertFalse(rootSource.contains("internal fun ClocktowerPlayerRoleRevealScreen("))
        assertFalse(rootSource.contains("private fun FullScreenColumn("))
    }

    @Test
    fun `app root retains deal navigation and index ownership`() {
        assertTrue(rootSource.contains("var currentDealIndex by remember { mutableStateOf(0) }"))
        assertTrue(rootSource.contains("playerName = cards[currentDealIndex].name"))
        assertTrue(rootSource.contains("card = cards[currentDealIndex]"))
        assertTrue(rootSource.contains("onReveal = { screen = Screen.RevealCard }"))
        assertTrue(rootSource.contains("if (currentDealIndex == cards.lastIndex)"))
        assertTrue(rootSource.contains("GameKind.Werewolf -> Screen.WerewolfJudge"))
        assertTrue(rootSource.contains("GameKind.Clocktower -> Screen.ClocktowerJudge"))
        assertTrue(rootSource.contains("GameKind.Undercover -> Screen.Game"))
        assertTrue(rootSource.contains("currentDealIndex += 1"))
        assertTrue(rootSource.contains("screen = Screen.PassPhone"))
    }

    @Test
    fun `deal display helpers remain root owned and cross file visible`() {
        assertTrue(rootSource.contains("internal fun Role.labelResId(): Int = when (this)"))
        assertFalse(rootSource.contains("private fun Role.labelResId(): Int = when (this)"))

        assertTrue(rootSource.contains("internal fun ClocktowerRole.nameFor(language: String): String"))
        assertTrue(rootSource.contains("internal fun ClocktowerRole.descriptionFor(language: String): String"))
        assertFalse(rootSource.contains("private fun ClocktowerRole.descriptionFor(language: String): String"))

        assertTrue(rootSource.contains("internal fun ClocktowerTeam.label(context: Context): String"))
    }

    @Test
    fun `shared root presentation primitives remain outside S4 deal slice`() {
        assertTrue(rootSource.contains("internal fun GameSettingsHeader("))
        assertTrue(rootSource.contains("internal fun EmptyStateCard("))
        assertTrue(rootSource.contains("internal fun StepperRow("))
        assertTrue(rootSource.contains("internal fun HostProgressCard("))
        assertFalse(
            extractedFile.readText(Charsets.UTF_8)
                .contains("fun ClocktowerDarkTheme("),
        )
        assertTrue(rootSource.contains("internal fun SelectableSeatNumbers("))
        assertTrue(rootSource.contains("internal fun WerewolfPlayerStatusRow("))
    }
}
