package com.codex.campboardgamehost

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import androidx.compose.ui.test.onAllNodesWithText
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D2 actions, succession, day table, recovery and complete games on the only Session. Real Compose handlers, no paid provider. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerD2NightDayFlowUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    /** Seat interaction must select the clickable square-table card, not its duplicated label. */
    private fun clickSeat(name: String) {
        ui.onNode(hasClickAction() and hasText(name)).performClick()
    }

    @Test fun automaticButlerTargetRequiresExplicitNext() = playerTargetRequiresNext("Butler")
    @Test fun automaticPoisonerTargetRequiresExplicitNext() = playerTargetRequiresNext("Poisoner")

    private fun playerTargetRequiresNext(role: String) {
        val host = D2TestHost(role, StorytellerOperationMode.AI_AUTOMATIC)
        ui.setContent { host.Content() }
        clickSeat("P2")
        ui.runOnIdle { org.junit.Assert.assertTrue(host.session.state.actionTimeline.entries.isEmpty()) }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            val actions = host.session.state.actionTimeline.entries.map { it.fact }.filterNot { it is ActionFact.PhaseAdvance }
            assertEquals(1, actions.size)
            assertEquals(StorytellerPhase.DAY, host.session.state.phase)
            val fact = actions.single()
            if (role == "Butler") assertEquals(2, (fact as com.codex.campboardgamehost.clocktower.domain.ActionFact.ButlerMasterChoice).masterSeat)
            else assertEquals(2, (fact as com.codex.campboardgamehost.clocktower.domain.ActionFact.Poison).targetSeat)
        }
    }

    @Test fun dayTableSingleClickRecordsNoExecutionAndEntersNight() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL)
        host.enterDay()
        ui.setContent { host.Content() }
        ui.onNodeWithText("End day").performClick()
        ui.runOnIdle {
            assertEquals(StorytellerPhase.NIGHT, host.session.state.phase)
            assertEquals(2, host.session.state.round)
            assertEquals(1, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.NoExecution>().size)
        }
    }

    @Test fun dayTableSingleClickExecutesConfirmedHighestVoteWithoutAnotherScreen() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL)
        host.enterDay()
        host.session.confirmNomination(3, 2, null)
        host.session.confirmVote(3, 2, listOf(1, 2, 3))
        ui.setContent { host.Content() }
        ui.onNodeWithText("End day").performClick()
        ui.runOnIdle {
            assertEquals(StorytellerPhase.NIGHT, host.session.state.phase)
            assertEquals(false, host.session.state.gameState.playerAt(2)!!.alive)
            assertEquals(1, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Execution>().size)
        }
    }

    @Test fun restoredConfirmedNominationOpensItsRealVoteWithoutTransientDraft() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL)
        host.enterDay()
        host.session.confirmNomination(3, 2, null)
        host.reload()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Confirm vote").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Vote>().single().voterSeats.size)
            assertEquals(null, host.session.state.pendingVote())
        }
        ui.onNodeWithText("End day").assertIsEnabled()
    }

    @Test fun realDeathDirectlyOpensKlutzChoiceAndOnlyConfirmCommitsIt() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL, ClocktowerScript.NoGreaterJoy,
            listOf("Imp", "Klutz", "Artist", "Empath", "Clockmaker"))
        host.enterDay()
        host.session.commitConfirmedPlayerDeath(com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Death(
            "ui-klutz-death", StorytellerPhase.DAY, 1, host.session.nextInputSequence, 2), host.session.state.gameStateRevision)
        host.reload()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Confirm Klutz choice").assertIsNotEnabled()
        clickSeat("P3")
        ui.runOnIdle { assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.KlutzChoice>().size) }
        ui.onNodeWithText("Confirm Klutz choice").performClick()
        ui.runOnIdle {
            val choice = host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.KlutzChoice>().single()
            assertEquals("ui-klutz-death", choice.deathActionId)
            assertEquals(3, choice.chosenSeat)
            assertEquals(StorytellerPhase.DAY, host.session.state.phase)
        }
    }

    @Test fun demonBluffsRouteIntoSeparatePrivateEvilInformationPreviews() {
        val host = D2TestHost("Chef", StorytellerOperationMode.MANUAL, roles =
            listOf("Chef", "Soldier", "Mayor", "Saint", "Empath", "Baron", "Imp"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Confirm Demon bluffs").assertIsNotEnabled()
        listOf("Monk", "Washerwoman", "Investigator").forEach { ui.onNodeWithText(it).performScrollTo().performClick() }
        ui.onNodeWithText("Confirm Demon bluffs").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Show to player").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        ui.runOnIdle { assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Next →").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(setOf(6), host.session.state.epistemicObservationLog.records.single().recipientSeats) }
        ui.onNodeWithText("Show to player").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        ui.runOnIdle { assertEquals(1, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(listOf(setOf(6), setOf(7)),
            host.session.state.epistemicObservationLog.records.map { it.recipientSeats }) }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
    }

    @Config(sdk = [35], qualifiers = "en-rUS-w360dp-h520dp-mdpi")
    @Test fun selfKillWithMultipleMinionsWaitsForChoiceThenCommitsOnNext() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL,
            roles = listOf("Imp", "Spy", "Baron", "Chef", "Soldier"))
        host.enterOtherNight()
        host.session.confirmSpyWake(
            com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog.fromContext(ui.activity)
                .ruleset(ClocktowerScript.TroubleBrewing), "other_night:role:Spy")
        host.refresh()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P1")
        ui.onNodeWithText("Imp self-kill · successor").assertExists()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("P4").assertExists()
        ui.onNode(hasClickAction() and hasText("P4")).assertDoesNotExist()
        ui.runOnIdle { assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Attack>().size) }
        clickSeat("P3")
        ui.onNodeWithText("Next →").assertIsEnabled()
        clickSeat("P3")
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P2")
        ui.onNodeWithText("Change attack target").assertIsDisplayed().performClick()
        ui.onNodeWithText("Imp self-kill · successor").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P1")
        // Returning to self-kill must require a fresh choice, not reuse the cancelled successor.
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P3")
        clickSeat("P2")
        ui.runOnIdle { host.session.advanceGameStateRevision(); host.refresh() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P2")
        ui.runOnIdle {
            assertEquals(true, host.session.state.gameState.playerAt(1)!!.alive)
            assertEquals(0, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.RoleChange })
        }
        ui.onNodeWithText("Next →").assertIsDisplayed().assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(false, host.session.state.gameState.playerAt(1)!!.alive)
            assertEquals(RoleId("Imp"), host.session.state.gameState.playerAt(2)!!.actualRole)
            assertEquals(1, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.RoleChange>().size)
        }
        ui.onNodeWithText("Informed, continue").assertIsNotEnabled()
        ui.onNodeWithText("Show full screen to player").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Informed, continue").assertIsEnabled().performClick()
        ui.runOnIdle {
            val notification = host.session.state.epistemicObservationLog.records.single { it.recordId.startsWith("role-notification:") }
            assertEquals(setOf(2), notification.recipientSeats)
        }
    }

    @Test fun selfKillUsesForcedScarletWithoutOfferingOtherMinion() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL, roles =
            listOf("Imp", "Spy", "Scarlet Woman", "Chef", "Soldier"))
        host.enterOtherNight()
        host.session.confirmSpyWake(
            com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog.fromContext(ui.activity)
                .ruleset(ClocktowerScript.TroubleBrewing), "other_night:role:Spy")
        host.refresh()
        ui.setContent { host.Content() }
        clickSeat("P1")
        ui.onNodeWithText("Imp self-kill · successor").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(RoleId("Imp"), host.session.state.gameState.playerAt(3)!!.actualRole)
            assertEquals(RoleId("Spy"), host.session.state.gameState.playerAt(2)!!.actualRole)
        }
    }

    @Test fun poisonedImpSelfTargetDoesNotOfferOrCommitSuccession() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL, roles =
            listOf("Imp", "Poisoner", "Baron", "Chef", "Soldier"))
        host.enterOtherNight()
        host.session.confirmNightPoison(1, StorytellerPhase.NIGHT, 2, "other_night:role:Poisoner")
        host.refresh()
        ui.setContent { host.Content() }
        clickSeat("P1")
        ui.onNodeWithText("Imp self-kill · successor").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(true, host.session.state.gameState.playerAt(1)!!.alive)
            assertEquals(1, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Attack>().size)
            assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.RoleChange>().size)
        }
    }

    @Test fun openNightPreviewIsDiscardedWhenSessionMovesToAnotherNight() {
        val host = D2TestHost("Chef", StorytellerOperationMode.MANUAL, roles =
            listOf("Chef", "Baron", "Imp", "Soldier", "Saint"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.runOnIdle { host.enterOtherNight() }
        ui.onNodeWithText("Close display card").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
    }

    @Test fun confirmingOneInformationRoleCannotEnableTheNextRolesNext() {
        val host = D2TestHost("Chef", StorytellerOperationMode.MANUAL, roles =
            listOf("Chef", "Empath", "Baron", "Imp", "Saint"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(1, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(listOf(1, 2), host.session.state.epistemicObservationLog.records.map { it.sourceSeat }) }
    }

    @Test fun failedNightObservationSaveRetainsPreviewAndRetryWritesOnce() {
        val host = D2TestHost("Chef", StorytellerOperationMode.MANUAL)
        var failSave = false
        host.session.persistWith { if (failSave) throw java.io.IOException("fixture disk failure") }
        ui.setContent { host.Content() }
        ui.onNodeWithText("Show to player: 0").performClick()
        ui.onNodeWithText("Close display card").performClick()
        val before = host.session.state
        ui.runOnIdle { failSave = true }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(before, host.session.state); assertEquals(1, host.saveErrors); failSave = false }
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(1, host.session.state.epistemicObservationLog.records.size) }
    }

    @Test fun unconfirmedImpTargetDoesNotCarryIntoTheNextNight() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL, roles =
            listOf("Imp", "Baron", "Chef", "Soldier", "Saint"))
        host.enterOtherNight()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("P4").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        ui.runOnIdle { host.advanceToNextNight() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle {
            assertEquals(3, host.session.state.round)
            assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Attack>().size)
        }
    }

    @Test fun troubleBrewingCompletesFirstNightDayNightAndEndingFromOnlySession() = completeGame(ClocktowerScript.TroubleBrewing)
    @Test fun noGreaterJoyCompletesFirstNightDayNightAndEndingFromOnlySession() = completeGame(ClocktowerScript.NoGreaterJoy)

    private fun completeGame(script: ClocktowerScript) {
        val role = if (script == ClocktowerScript.TroubleBrewing) "Chef" else "Clockmaker"
        val roles = if (script == ClocktowerScript.TroubleBrewing)
            listOf(role, "Slayer", "Soldier", "Poisoner", "Imp") else
            listOf(role, "Artist", "Sage", "Baron", "Imp")
        val host = D2TestHost(role, StorytellerOperationMode.MANUAL, script, roles)
        val original = host.session
        ui.setContent { host.Content() }
        if (script == ClocktowerScript.TroubleBrewing) {
            clickSeat("P3")
            ui.onNodeWithText("Next →").performClick()
        }
        if (script == ClocktowerScript.TroubleBrewing) {
            // Poisoner and Imp occupy neighbouring seats 4/5: Chef's Host value is 1.
            ui.onNodeWithText("Show to player: 1").performClick()
        } else {
            ui.onNodeWithText("Show to player: 1").performClick()
        }
        ui.runOnIdle { assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.onNodeWithText("End day").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(original, host.session)
            assertEquals(StorytellerPhase.NIGHT, host.session.state.phase)
            assertEquals(2, host.session.state.round)
        }
        if (script == ClocktowerScript.TroubleBrewing) {
            clickSeat("P3")
            ui.onNodeWithText("Next →").performClick()
        }
        clickSeat("P2")
        ui.onNodeWithText("Next →").performClick()
        ui.onNodeWithText("End day").assertIsEnabled()
        ui.runOnIdle {
            assertEquals(original, host.session)
            assertEquals(false, host.session.state.gameState.playerAt(2)!!.alive)
            host.reload()
            assertEquals("d2-ui", host.session.state.gameId)
            host.session.confirmNomination(1, 5, null)
            host.refresh()
        }
        listOf("P1", "P3", "P4").forEach(::clickSeat)
        ui.onNodeWithText("Confirm vote").performClick()
        ui.onNodeWithText("End day").performClick()
        ui.runOnIdle {
            val ended = host.session.state
            assertEquals(GameEndReason.NO_DEMON, ended.outcome?.reason)
            assertEquals(1, ended.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.GameEnded>().size)
            assertEquals(1, ended.epistemicObservationLog.records.size)
            host.reload()
            assertEquals(ended, host.session.state)
            host.session.confirmEndDay(2)
            assertEquals(ended, host.session.state)
        }
    }

    /** Uses the production JudgeScreen and real Session; only external input/provider callbacks are fixtures. */
}
