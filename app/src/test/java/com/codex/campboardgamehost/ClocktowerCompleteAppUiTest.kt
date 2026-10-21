package com.codex.campboardgamehost

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real App router, production setup, one Session and actual private AtomicFile. No fake game callbacks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerCompleteAppUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val mounted = mutableStateOf(true)
    private val players = (1..5).map { "P$it" }
    private val store get() = ClocktowerActiveGameStore.fromContext(ui.activity)
    private fun state() = requireNotNull(store.load()).state
    private fun click(label: String) = ui.onNodeWithText(label).performClick()
    private fun waitFor(label: String) = ui.waitUntil(10000) {
        ui.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
    }

    private fun launch(script: ClocktowerScript, seed: Long) {
        ui.runOnIdle {
            ui.activity.getSharedPreferences("camp_board_game_host", Context.MODE_PRIVATE).edit().clear().commit()
            AppPreferencesStore(ui.activity).apply {
                saveLanguageMode(LanguageMode.English)
                saveStorytellerOperationMode(StorytellerOperationMode.MANUAL)
                saveCommonPlayers(emptyList())
            }
            store.clear()
        }
        // Randomness is fixed; all assignment, gameplay, persistence and UI callbacks are production.
        ui.setContent { if (mounted.value) CampBoardGameHostApp(newClocktowerSeed = { seed }) }
        waitFor("Start game")
        click("Start game")
        players.forEach { name ->
            ui.onNode(hasSetTextAction()).performTextReplacement(name)
            ui.onNodeWithText("Add player").performScrollTo().performClick()
        }
        click("Confirm seats")
        ui.onNodeWithText("Continue").performScrollTo().performClick()
        ui.onNodeWithText(script.nameFor("en")).performScrollTo().performClick()
        ui.onNodeWithText("Continue").performScrollTo().performClick()
        ui.onNodeWithText("Start Dealing").performScrollTo().assertIsEnabled().performClick()
        waitFor("Show identity")
    }

    private fun deal() {
        repeat(5) {
            click("Show identity")
            click("Hide role and return to Storyteller")
            click("Next")
        }
        ui.waitUntil(10000) { runCatching { store.load() != null }.getOrDefault(false) }
    }

    private fun remount() {
        ui.runOnIdle { mounted.value = false }
        ui.waitForIdle()
        ui.runOnIdle { mounted.value = true }
    }

    private fun numericInformation() {
        ui.onNode(hasSetTextAction()).performTextReplacement("1")
        click("Show manually selected result")
        click("Close display card")
        click("Next")
    }

    private fun nominateAndExecute(nominator: PlayerState, target: PlayerState) {
        val source = ui.onNodeWithText(nominator.name).fetchSemanticsNode().boundsInRoot.center
        val destination = ui.onNodeWithText(target.name).fetchSemanticsNode().boundsInRoot.center
        ui.onRoot().performTouchInput {
            down(source)
            advanceEventTime(700)
            moveTo(destination, delayMillis = 600)
            up()
        }
        waitFor("Start voting")
        click("Start voting")
        state().gameState.players.filter { it.alive }.forEach { click(it.name) }
        click("Confirm vote")
        click("End day")
    }

    @Test fun troubleBrewingRunsFromLandingThroughDealToOutcomeAndRestoredQuickRestart() = complete(ClocktowerScript.TroubleBrewing, 34L)
    @Test fun noGreaterJoyRunsFromLandingThroughDealToOutcomeAndRestoredQuickRestart() = complete(ClocktowerScript.NoGreaterJoy, 26L)

    private fun complete(script: ClocktowerScript, seed: Long) {
        launch(script, seed)
        deal()
        val first = state()
        assertEquals(script.toRecommendationScriptId(), first.gameState.script)
        assertEquals(players, first.gameState.players.map { it.name })
        // These seeds select real legal production setups with only numeric first-night information.
        assertEquals(if (script == ClocktowerScript.TroubleBrewing)
            setOf("Chef", "Empath", "Slayer", "Scarlet Woman", "Imp") else
            setOf("Clockmaker", "Artist", "Sage", "Scarlet Woman", "Imp"), first.gameState.players.map { it.actualRole.value }.toSet())
        repeat(if (script == ClocktowerScript.TroubleBrewing) 2 else 1) { numericInformation() }
        waitFor("End day")
        val day = state()
        val source = day.gameState.players.first { it.actualRole.value == if (script == ClocktowerScript.TroubleBrewing) "Chef" else "Clockmaker" }
        nominateAndExecute(source, day.gameState.players.single { it.actualRole.value == "Scarlet Woman" })
        assertEquals(StorytellerPhase.NIGHT, state().phase)
        val victim = state().gameState.players.single { it.actualRole.value == if (script == ClocktowerScript.TroubleBrewing) "Empath" else "Artist" }
        click(victim.name)
        click("Next →")
        waitFor("End day")
        val beforeReload = state()
        assertEquals(false, beforeReload.gameState.playerAt(victim.seat)!!.alive)
        remount()
        waitFor("End day")
        assertEquals(beforeReload, state())
        val alive = state().gameState.players
        nominateAndExecute(alive.single { it.seat == source.seat }, alive.single { it.actualRole.value == "Imp" })
        waitFor("Confirm and reveal all roles")
        ui.onNodeWithText("All players and actual roles").assertDoesNotExist()
        val ended = state()
        assertEquals(first.gameId, ended.gameId)
        assertEquals(GameEndReason.NO_DEMON, ended.outcome?.reason)
        assertEquals(1, ended.actionTimeline.reducerFacts().filterIsInstance<ActionFact.GameEnded>().size)
        // Restore the actual ended save, then start another game with the same canonical participants.
        remount()
        waitFor("Confirm and reveal all roles")
        assertEquals(ended, state())
        click("Confirm and reveal all roles")
        waitFor("Finish and prepare next game")
        click("Finish and prepare next game")
        click("Quick restart with same players")
        waitFor("Show identity")
        deal()
        assertNotEquals(ended.gameId, state().gameId)
        assertEquals(ended.gameState.script, state().gameState.script)
        assertEquals(players, state().gameState.players.map { it.name })
        if (script == ClocktowerScript.TroubleBrewing) assertNotEquals(
            ended.gameState.players.map { it.actualRole }.toSet(), state().gameState.players.map { it.actualRole }.toSet())
    }

    @Test fun interruptedDealingRestoresTheSameSessionAndSafelyRepeatsIdentityDisplay() {
        launch(ClocktowerScript.NoGreaterJoy, 26L)
        click("Show identity")
        click("Hide role and return to Storyteller")
        ui.waitUntil(10000) { runCatching { store.load() != null }.getOrDefault(false) }
        val accepted = state()
        remount()
        waitFor("Show identity")
        assertEquals(accepted, state())
        deal()
        assertEquals(accepted.gameId, state().gameId)
    }
}
