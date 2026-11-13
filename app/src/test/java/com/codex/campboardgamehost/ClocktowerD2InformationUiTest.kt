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

/** D2 private information: only the last preview is recorded on Next, across modes and information roles. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerD2InformationUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    /** Seat interaction must select the clickable square-table card, not its duplicated label. */
    private fun clickSeat(name: String) {
        ui.onNode(hasClickAction() and hasText(name)).performClick()
    }

    @Test fun manualChefPublishesOnlyFinalPreviewOnNext() = numericHost(StorytellerOperationMode.MANUAL)
    @Test fun assistedEmpathPublishesOnlyFinalPreviewOnNext() = numericHost(StorytellerOperationMode.AI_ASSISTED, "Empath")
    @Test fun automaticChefWaitsForActualPreviewAndExplicitNext() = numericHost(StorytellerOperationMode.AI_AUTOMATIC)

    @Test fun manualWasherwomanEditsInvalidatePreview() = pairHost("Washerwoman", StorytellerOperationMode.MANUAL)
    @Test fun manualLibrarianEditsInvalidatePreview() = pairHost("Librarian", StorytellerOperationMode.MANUAL)
    @Test fun manualInvestigatorEditsInvalidatePreview() = pairHost("Investigator", StorytellerOperationMode.MANUAL)
    @Test fun assistedPairRetainsManualEditWhenAdviceChanges() = pairHost("Investigator", StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticPairWaitsForDisplayAndNext() = pairHost("Washerwoman", StorytellerOperationMode.AI_AUTOMATIC)

    @Test fun firstNightSpyWakeKeepsTheOpeningWasherwomanAdviceWithoutAnotherRequest() {
        val host = D2TestHost("Washerwoman", StorytellerOperationMode.AI_AUTOMATIC,
            roles = listOf("Washerwoman", "Spy", "Soldier", "Imp", "Mayor"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.onNode(hasClickAction() and hasText("P2 + P3", substring = true)).assertIsEnabled().performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.runOnIdle {
            assertEquals(0, host.requests)
            assertEquals(1, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.NightWake })
            assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(1, host.session.state.epistemicObservationLog.records.size) }
    }

    @Test fun redHerringPreselectsOpeningAdviceAndOnlyConfirmsAnAllowedManualOverrideOnNext() {
        val host = D2TestHost("Fortune Teller", StorytellerOperationMode.AI_ASSISTED,
            roles = listOf("Fortune Teller", "Soldier", "Recluse", "Imp", "Mayor", "Scarlet Woman"))
        host.openingRedHerringSeat.value = 2
        val before = host.session.state.playerInputRevision
        ui.setContent { host.Content() }
        ui.onNodeWithText("Selected: P2").assertExists()
        ui.onNodeWithText("Next →").assertIsEnabled()
        listOf("P3", "P4", "P6").forEach { name ->
            ui.onNodeWithText(name).assertExists()
            ui.onNode(hasClickAction() and hasText(name)).assertDoesNotExist()
        }
        clickSeat("P2")
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P5")
        ui.onNodeWithText("Selected: P5").assertExists()
        ui.runOnIdle {
            assertEquals(null, host.session.state.redHerringSeat)
            assertEquals(before, host.session.state.playerInputRevision)
            assertEquals(0, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.RedHerringChoice })
        }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            assertEquals(5, host.session.state.redHerringSeat)
            assertEquals(1, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.RedHerringChoice })
        }
    }

    private fun pairHost(role: String, mode: StorytellerOperationMode) {
        val host = D2TestHost(role, mode)
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        val first = if (role == "Librarian") 3 else 2
        val second = if (role == "Librarian") 4 else 3
        val initialRole = when (role) {
            "Washerwoman" -> "Soldier"
            "Librarian" -> "Saint"
            else -> "Baron"
        }
        if (mode != StorytellerOperationMode.MANUAL) {
            ui.onNode(hasClickAction() and hasText("P$first + P$second", substring = true)).performClick()
        } else {
            // Stage 4: choose two physical seats BEFORE choosing the matching role.
            clickSeat("P$first")
            clickSeat("P$second")
            if (role == "Washerwoman") {
                ui.onNodeWithText("Choose character").performClick()
                ui.onAllNodesWithText(initialRole)[1].performClick()
            }
            ui.onNode(hasClickAction() and hasText("Show to player:", substring = true)).performClick()
        }
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        if (mode != StorytellerOperationMode.AI_AUTOMATIC) {
            if (mode == StorytellerOperationMode.AI_ASSISTED) {
                ui.onNodeWithText("Manual selection").performClick()
            }
            // Remove first seat, retain second, then select a new partner.
            clickSeat("P$first")
            val newPartner = if (role == "Librarian") 2 else 4
            clickSeat("P$newPartner")
            ui.onNodeWithText("Next →").assertIsNotEnabled()
            // An asynchronous changed recommendation must not clear the human draft.
            ui.runOnIdle { host.pairRole.value = "Mayor" }
            ui.onNode(hasClickAction() and hasText("Show to player:", substring = true)).performClick()
            ui.onNodeWithText("Close display card").performClick()
        }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            val observation = host.session.state.epistemicObservationLog.records.single()
            assertEquals(setOf(1), observation.recipientSeats)
            val pair = (observation.proposition as InformationProposition.AnyOf).alternatives
                .map { it as InformationProposition.RoleAt }
            assertEquals(if (mode == StorytellerOperationMode.AI_AUTOMATIC)
                listOf(first, second) else listOf(second, if (role == "Librarian") 2 else 4).sorted(),
                pair.map { it.seat })
            assertEquals(if (mode == StorytellerOperationMode.AI_AUTOMATIC) initialRole else
                when (role) { "Washerwoman" -> "Mayor"; "Librarian" -> "Saint"; else -> "Baron" },
                pair.first().role.value)
        }
    }

    @Test fun automaticFailureRetryAndLateResponseStayOnCurrentRevision() {
        val host = D2TestHost("Empath", StorytellerOperationMode.AI_AUTOMATIC)
        host.openingEnabled.value = false
        ui.setContent { host.Content() }
        ui.onNodeWithText("Request again").assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(2, host.requests) }
        val oldKey = host.adviceKey.value
        val requestId = requireNotNull(host.requestId)
        ui.runOnIdle { host.session.recordPlayerInput(); host.refresh() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { host.deliver(oldKey, requestId, "1") }
        ui.onNodeWithText("Show to player: 1").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Request again").performClick()
        ui.runOnIdle { host.deliver(host.adviceKey.value, requireNotNull(host.requestId), "1") }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performTouchInput { doubleClick() }
        ui.runOnIdle { assertEquals(1, (host.session.state.epistemicObservationLog.records.single()
            .proposition as InformationProposition.NumericResult).value) }
    }

    @Test fun manualUndertakerRestoresPreviewAndRejectsUnshownEdits() = roleReveal("Undertaker", StorytellerOperationMode.MANUAL)
    @Test fun daySuccessionShowsNewImpOnTableThenAllowsItsActualAttack() {
        val host = D2TestHost("Imp", StorytellerOperationMode.MANUAL,
            roles = listOf("Imp", "Scarlet Woman", "Chef", "Soldier", "Saint"))
        host.enterDay()
        host.session.confirmNomination(3, 1, null)
        host.session.confirmVote(3, 1, listOf(1, 2, 3))
        host.session.confirmEndDay(1)
        host.refresh()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Wake seat 2").assertExists()
        ui.onNodeWithText("Informed, continue").assertIsNotEnabled()
        ui.onNodeWithText("Show full screen to player").performClick()
        ui.onNodeWithText("You are now the Imp").assertExists()
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Informed, continue").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(InformationProposition.RoleAt(2, RoleId("Imp")),
                host.session.state.epistemicObservationLog.records.single().proposition)
            assertEquals(0, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
        }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P3")
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
        ui.runOnIdle {
            assertEquals(false, host.session.state.gameState.playerAt(3)!!.alive)
            assertEquals(true, host.session.state.gameState.playerAt(2)!!.alive)
            assertEquals(1, host.session.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
        }
    }

    @Test fun healthyUndertakerInAutomaticModeNeedsNoAiAndOnlyShowsTheExecutedRole() {
        val host = D2TestHost("Undertaker", StorytellerOperationMode.AI_AUTOMATIC,
            roles = listOf("Undertaker", "Soldier", "Mayor", "Baron", "Imp"))
        host.enterDay()
        host.session.confirmNomination(1, 2, null)
        host.session.confirmVote(1, 2, listOf(1, 2, 3), 1)
        host.session.confirmEndDay(1)
        val rules = com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog.fromContext(ui.activity)
            .ruleset(ClocktowerScript.TroubleBrewing)
        // The Imp acts before the Undertaker on later nights.
        host.session.confirmNightAttack(4, StorytellerPhase.NIGHT, 2, requireNotNull(host.session.state.nextNightInteraction(rules)).id.value)
        host.refresh()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Choose character").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Show to player: Soldier").performClick()
        ui.onNodeWithText("Soldier").assertExists()
        ui.onNodeWithText("Record the actual shown character.").assertDoesNotExist()
        ui.onNodeWithText("Close display card").performClick()
        ui.runOnIdle { assertEquals(0, host.requests); assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(InformationProposition.RoleAt(2, RoleId("Soldier")), host.session.state.epistemicObservationLog.records.single().proposition) }
    }

    @Test fun deadHealthyRavenkeeperChoosesTargetThenGetsOneHostRoleWithoutAi() {
        val host = D2TestHost("Ravenkeeper", StorytellerOperationMode.AI_AUTOMATIC,
            roles = listOf("Ravenkeeper", "Soldier", "Mayor", "Saint", "Imp"))
        host.enterOtherNight()
        val rules = com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog.fromContext(ui.activity)
            .ruleset(ClocktowerScript.TroubleBrewing)
        host.session.confirmNightAttack(1, StorytellerPhase.NIGHT, 2, requireNotNull(host.session.state.nextNightInteraction(rules)).id.value)
        host.refresh()
        ui.setContent { host.Content() }
        clickSeat("P2")
        ui.onNodeWithText("Choose character").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Show to player: Soldier").performClick()
        ui.onNodeWithText("Soldier").assertExists()
        ui.onNodeWithText("Close display card").performClick()
        ui.runOnIdle { assertEquals(0, host.requests); assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(InformationProposition.RoleAt(2, RoleId("Soldier")), host.session.state.epistemicObservationLog.records.single().proposition) }
    }
    @Test fun assistedUndertakerKeepsHumanInputWhenAdviceChanges() = roleReveal("Undertaker", StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticUndertakerRequiresPreviewBeforeNext() = roleReveal("Undertaker", StorytellerOperationMode.AI_AUTOMATIC)
    @Test fun manualRavenkeeperRestoresPreviewAndRejectsUnshownEdits() = roleReveal("Ravenkeeper", StorytellerOperationMode.MANUAL)
    @Test fun assistedRavenkeeperKeepsHumanInputWhenAdviceChanges() = roleReveal("Ravenkeeper", StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticRavenkeeperRequiresPreviewBeforeNext() = roleReveal("Ravenkeeper", StorytellerOperationMode.AI_AUTOMATIC)

    private fun roleReveal(role: String, mode: StorytellerOperationMode) {
        val f = D2TestRoleCard(role, mode)
        ui.setContent { f.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        if (mode == StorytellerOperationMode.MANUAL) {
            ui.onNodeWithText("Choose character").performClick()
            ui.onNodeWithText("Spy").performClick()
            ui.onNodeWithText("Show to player").performClick()
        } else ui.onNodeWithText("Show: Spy").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        if (mode != StorytellerOperationMode.AI_AUTOMATIC) {
            if (mode == StorytellerOperationMode.AI_ASSISTED) ui.onNodeWithText("Manual selection").performClick()
            ui.onNode(hasClickAction() and hasText("Spy")).performClick()
            ui.onNodeWithText("Chef").performClick()
            ui.onNodeWithText("Next →").assertIsNotEnabled()
            ui.runOnIdle { f.advice.value = f.roleOption("Baron") }
            ui.onNodeWithText("Chef").assertExists()
            ui.onNodeWithText("Show to player").performClick()
            ui.onNodeWithText("Close display card").performClick()
        }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            assertEquals(InformationProposition.RoleAt(2, RoleId(
                if (mode == StorytellerOperationMode.AI_AUTOMATIC) "Spy" else "Chef")), f.confirmed.single())
        }
    }

    @Test fun manualHealthyFortuneTellerUsesHostAnswerAndRecordsOnlyFinalPreview() = healthyFortuneTeller(StorytellerOperationMode.MANUAL)
    @Test fun assistedHealthyFortuneTellerUsesHostAnswerAndRecordsOnlyFinalPreview() = healthyFortuneTeller(StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticHealthyFortuneTellerNeedsNoAiAndRecordsOnlyFinalPreview() = healthyFortuneTeller(StorytellerOperationMode.AI_AUTOMATIC)
    private fun healthyFortuneTeller(mode: StorytellerOperationMode) {
        val host = D2TestHost("Fortune Teller", mode, roles = listOf("Fortune Teller", "Soldier", "Mayor", "Saint", "Imp"))
        host.session.commitRedHerringSeat(4, StorytellerPhase.FIRST_NIGHT, 1, host.session.nextInputSequence)
        host.refresh()
        ui.setContent { host.Content() }
        clickSeat("P2")
        clickSeat("P3")
        ui.onNodeWithText("YES").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("NO").performClick()
        ui.runOnIdle { assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        clickSeat("P2")
        clickSeat("P4") // Red herring, changing targets must invalidate the old NO preview.
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("NO").assertDoesNotExist()
        ui.onNodeWithText("YES").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            val record = host.session.state.epistemicObservationLog.records.single()
            assertEquals(setOf(1), record.recipientSeats)
            assertEquals(InformationProposition.BooleanResult(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                1, listOf(4, 3), true), record.proposition)
            assertEquals(0, host.requests)
        }
    }

    @Test fun manualFortuneTellerClearsPreviewWhenPlayerChangesQuery() = fortuneTeller(StorytellerOperationMode.MANUAL)
    @Test fun assistedFortuneTellerClearsPreviewWhenPlayerChangesQuery() = fortuneTeller(StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticFortuneTellerCannotReuseAnswerForOtherPlayers() = fortuneTeller(StorytellerOperationMode.AI_AUTOMATIC)
    private fun fortuneTeller(mode: StorytellerOperationMode) {
        val f = D2TestRoleCard("Fortune Teller", mode)
        ui.setContent { f.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText(if (mode == StorytellerOperationMode.AI_AUTOMATIC) "YES" else "NO").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        clickSeat("P2")
        clickSeat("P4")
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        if (mode == StorytellerOperationMode.AI_AUTOMATIC) {
            ui.onNodeWithText("YES").assertDoesNotExist()
            ui.runOnIdle { f.advice.value = f.booleanOption(false) }
        }
        ui.onNodeWithText("NO").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            assertEquals(listOf(4, 3), (f.confirmed.single() as InformationProposition.BooleanResult).subjectSeats)
        }
    }

    @Test fun manualChambermaidReturnsToSelectedNumberAndClearsChangedQuery() {
        val f = D2TestRoleCard("Chambermaid", StorytellerOperationMode.MANUAL)
        ui.setContent { f.Content() }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        clickSeat("P2")
        clickSeat("P4")
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            assertEquals(listOf(3, 4), (f.confirmed.single() as InformationProposition.NumericResult).subjectSeats)
        }
    }

    @Test fun manualSageRetainsPairAndInvalidatesChangedSeats() {
        val f = D2TestRoleCard("Sage", StorytellerOperationMode.MANUAL)
        ui.setContent { f.Content() }
        clickSeat("P2")
        clickSeat("P3")
        ui.onNodeWithText("Show this pair").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        clickSeat("P2")
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P4")
        ui.onNodeWithText("Show this pair").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(listOf(3, 4), clocktowerSagePreviewSeats(f.confirmed.single(), 5)) }
    }

    private fun numericHost(mode: StorytellerOperationMode, role: String = "Chef") {
        val host = D2TestHost(role, mode)
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        val discretionary = role == "Empath"
        val first = 0
        if (discretionary) ui.onNodeWithText("0").performClick()
        ui.onNode(hasSetTextAction()).assertDoesNotExist()
        ui.onNodeWithText("Show to player: $first").performClick()
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Next →").assertIsEnabled()
        if (discretionary) {
            ui.onNode(hasClickAction() and hasText("1", substring = true)).performClick()
            ui.onNodeWithText("Next →").assertIsNotEnabled()
            ui.onNodeWithText("Show to player: 1").performClick()
            ui.onNodeWithText("Close display card").performClick()
        }
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            val records = host.session.state.epistemicObservationLog.records
            assertEquals(1, records.size)
            assertEquals(setOf(1), records.single().recipientSeats)
            assertEquals(if (discretionary) 1 else 0,
                (records.single().proposition as InformationProposition.NumericResult).value)
        }
    }

}
