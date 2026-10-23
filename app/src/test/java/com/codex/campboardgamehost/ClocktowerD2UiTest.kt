package com.codex.campboardgamehost

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
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

/** Exercises real Compose event handlers and unmount/recomposition, without a paid provider. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerD2UiTest {
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

    private fun pairHost(role: String, mode: StorytellerOperationMode) {
        val host = Host(role, mode)
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
        val host = Host("Empath", StorytellerOperationMode.AI_AUTOMATIC)
        host.openingEnabled.value = false
        ui.setContent { host.Content() }
        ui.onNodeWithText("Retry AI information").assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(2, host.requests) }
        val oldKey = host.adviceKey.value
        val requestId = requireNotNull(host.requestId)
        ui.runOnIdle { host.session.recordPlayerInput(); host.refresh() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { host.deliver(oldKey, requestId, "1") }
        ui.onNodeWithText("Show to player: 1").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Retry AI information").performClick()
        ui.runOnIdle { host.deliver(host.adviceKey.value, requireNotNull(host.requestId), "1") }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performTouchInput { doubleClick() }
        ui.runOnIdle { assertEquals(1, (host.session.state.epistemicObservationLog.records.single()
            .proposition as InformationProposition.NumericResult).value) }
    }

    @Test fun manualUndertakerRestoresPreviewAndRejectsUnshownEdits() = roleReveal("Undertaker", StorytellerOperationMode.MANUAL)
    @Test fun assistedUndertakerKeepsHumanInputWhenAdviceChanges() = roleReveal("Undertaker", StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticUndertakerRequiresPreviewBeforeNext() = roleReveal("Undertaker", StorytellerOperationMode.AI_AUTOMATIC)
    @Test fun manualRavenkeeperRestoresPreviewAndRejectsUnshownEdits() = roleReveal("Ravenkeeper", StorytellerOperationMode.MANUAL)
    @Test fun assistedRavenkeeperKeepsHumanInputWhenAdviceChanges() = roleReveal("Ravenkeeper", StorytellerOperationMode.AI_ASSISTED)
    @Test fun automaticRavenkeeperRequiresPreviewBeforeNext() = roleReveal("Ravenkeeper", StorytellerOperationMode.AI_AUTOMATIC)

    private fun roleReveal(role: String, mode: StorytellerOperationMode) {
        val f = RoleCard(role, mode)
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
        val host = Host("Fortune Teller", mode, roles = listOf("Fortune Teller", "Soldier", "Mayor", "Saint", "Imp"))
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
        val f = RoleCard("Fortune Teller", mode)
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
        val f = RoleCard("Chambermaid", StorytellerOperationMode.MANUAL)
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
        val f = RoleCard("Sage", StorytellerOperationMode.MANUAL)
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

    /** Real role card + real fullscreen; callbacks expose the chosen result for the shared Host tests above. */
    private class RoleCard(val role: String, val mode: StorytellerOperationMode) {
        val cards = listOf(role, "Soldier", "Mayor", "Saint", "Recluse").mapIndexed { index, name ->
            val identity = requireNotNull(clocktowerRoleByName(name))
            PlayerCard("P${index + 1}", clocktowerRole = identity, clocktowerShownRole = identity, clocktowerTeam = identity.team)
        }
        val first = mutableStateOf<String?>("P2")
        val second = mutableStateOf<String?>("P3")
        val preview = mutableStateOf<ClocktowerNightStepUi?>(null)
        val fullscreen = mutableStateOf(false)
        val confirmed = mutableListOf<InformationProposition>()
        private fun subjects() = listOfNotNull(first.value, second.value).map { name -> cards.indexOfFirst { it.name == name } + 1 }
        private val twoSeatQuery = role in listOf("Fortune Teller", "Chambermaid")
        private fun slots() = listOf(first.value, second.value).map { name -> name?.let { n -> cards.indexOfFirst { it.name == n } + 1 } }
        private fun step() = ClocktowerNightStepUi(
            title = role, actor = cards[0], isRealAction = true, reason = "", storytellerAction = "Wake P1",
            tellPlayer = role, explanation = "", roleEnName = role,
            action = when (role) {
                "Fortune Teller" -> ClocktowerNightAction.FortuneTeller
                "Chambermaid" -> ClocktowerNightAction.Chambermaid
                "Ravenkeeper" -> ClocktowerNightAction.Ravenkeeper
                else -> ClocktowerNightAction.None
            },
            informationQuestion = when (role) {
                "Fortune Teller" -> ClocktowerInformationQuestion.Boolean(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, subjects())
                "Chambermaid" -> ClocktowerInformationQuestion.Number(NumericMetric.PLAYERS_WAKING_FOR_ABILITY, subjects())
                "Sage" -> null
                else -> ClocktowerInformationQuestion.Role(2)
            },
            displayKind = when(role) {
                "Fortune Teller" -> ClocktowerDisplayKind.YesNo
                "Chambermaid" -> ClocktowerDisplayKind.Number
                "Sage" -> ClocktowerDisplayKind.EitherOne
                else -> ClocktowerDisplayKind.RoleReveal
            },
        )
        fun roleOption(identity: String) = requireNotNull(clocktowerDirectRoleRevealChoice(
            step(), 2, cards.size, identity, clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)) { it }).displayOption
        fun booleanOption(value: Boolean) = ClocktowerDisplayOption("answer", ClocktowerDisplayKind.YesNo,
            role, if(value) "YES" else "NO", null, null,
            proposition = InformationProposition.BooleanResult(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, subjects(), value))
        val advice = mutableStateOf<ClocktowerDisplayOption?>(when(role) {
            "Fortune Teller" -> booleanOption(true)
            "Undertaker", "Ravenkeeper" -> roleOption("Spy")
            else -> null
        })
        @Composable fun Content() {
            val current = step()
            ClocktowerDarkTheme {
                if (fullscreen.value) {
                    ClocktowerPlayerDisplayCardLocalized(requireNotNull(preview.value), cards, onDismiss = { fullscreen.value = false })
                } else CompositionLocalProvider(LocalClocktowerNightNextEnabled provides (preview.value != null)) {
                    ClocktowerNightStepCardLocalized(
                        scriptRoles = clocktowerRolesForScript(if(role in listOf("Sage", "Chambermaid")) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing),
                        allowRuleDeterminedAutoUi = mode == StorytellerOperationMode.AI_AUTOMATIC,
                        phase = ClocktowerPhase.Night, gameId = "role-d2", round = 2, sequence = 0,
                        gameStateRevision = 0, playerInputRevision = 0, informationDecisionKey = "role-d2:$role:2",
                        cards = cards,
                        actualAlignmentBySeat = cards.mapIndexed { index, card ->
                            (index + 1) to if (card.clocktowerTeam in setOf(ClocktowerTeam.Demon, ClocktowerTeam.Minion))
                                Alignment.EVIL else Alignment.GOOD
                        }.toMap(),
                        ghostVoteAuthority = ClocktowerGhostVoteAuthority(), poisonedPlayerName = null,
                        aliveSeats = (1..cards.size).toSet(), chambermaidTargetSeats = (1..cards.size).toSet(),
                        mayorRedirectTargetSeats = emptySet(), livingMayorSeat = 3,
                        step = current, surfacePlan = clocktowerNightSurfacePlan(current, ClocktowerPhase.Night),
                        numericResolution = if (role == "Chambermaid" && subjects().size == 2)
                            HostNumericResolution(
                                listOf(1), subjects().sorted(),
                                com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState.FUNCTIONING,
                                emptySet(), false,
                            ) else null,
                        // This card-only fixture exercises discretionary (impaired) Boolean input.
                        booleanResolution = if (role == "Fortune Teller" && subjects().size == 2)
                            HostBooleanResolution(listOf(false, true), 1, subjects(),
                                com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState.POISONED) else null,
                        aiSuggestedInformationOption = advice.value.takeIf { mode != StorytellerOperationMode.MANUAL },
                        selectedSeatSlots = if (twoSeatQuery) slots() else listOf(2), redHerringSeat = null,
                        onSelectSeat = { seat ->
                            if (twoSeatQuery) ClocktowerNightStepDraft("role-d2", StorytellerPhase.NIGHT, 2,
                                com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId("role-d2"), slots(),
                            ).withTappedSeat(current.action, seat)?.let { next ->
                                first.value = next.seatSlots.getOrNull(0)?.let { cards[it - 1].name }
                                second.value = next.seatSlots.getOrNull(1)?.let { cards[it - 1].name }
                                preview.value = null
                            }
                        },
                        onShowPlayerDisplay = { preview.value = it; fullscreen.value = true },
                        onInformationDraftChanged = { preview.value = null }, lastPreviewProposition = preview.value?.displayProposition,
                        onHostTools = {},
                        onNext = { preview.value?.displayProposition?.let(confirmed::add) },
                    )
                }
            }
        }
    }

    private fun numericHost(mode: StorytellerOperationMode, role: String = "Chef") {
        val host = Host(role, mode)
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

    @Test fun automaticButlerTargetRequiresExplicitNext() = playerTargetRequiresNext("Butler")
    @Test fun automaticPoisonerTargetRequiresExplicitNext() = playerTargetRequiresNext("Poisoner")

    private fun playerTargetRequiresNext(role: String) {
        val host = Host(role, StorytellerOperationMode.AI_AUTOMATIC)
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL)
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL)
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL)
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL, ClocktowerScript.NoGreaterJoy,
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
        val host = Host("Chef", StorytellerOperationMode.MANUAL, roles =
            listOf("Chef", "Soldier", "Mayor", "Saint", "Empath", "Baron", "Imp"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Confirm Demon bluffs").assertIsNotEnabled()
        listOf("Monk", "Washerwoman", "Investigator").forEach { ui.onNodeWithText(it).performScrollTo().performClick() }
        ui.onNodeWithText("Confirm Demon bluffs").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("Show to player").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(setOf(6), host.session.state.epistemicObservationLog.records.single().recipientSeats) }
        ui.onNodeWithText("Show to player").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle { assertEquals(listOf(setOf(6), setOf(7)),
            host.session.state.epistemicObservationLog.records.map { it.recipientSeats }) }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
    }

    @Test fun selfKillWithMultipleMinionsWaitsForChoiceThenCommitsOnNext() {
        val host = Host("Imp", StorytellerOperationMode.MANUAL, roles =
            listOf("Imp", "Spy", "Baron", "Chef", "Soldier"))
        host.enterOtherNight()
        host.session.confirmSpyWake(
            com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog.fromContext(ui.activity)
                .ruleset(ClocktowerScript.TroubleBrewing), "other_night:role:Spy")
        host.refresh()
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        clickSeat("P1")
        ui.onNodeWithText("Imp self-kill · successor").assertExists()
        ui.runOnIdle { assertEquals(0, host.session.state.actionTimeline.entries.map { it.fact }.filterIsInstance<ActionFact.Attack>().size) }
        ui.onNodeWithText("#2 P2").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled().performClick()
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL, roles =
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL, roles =
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
        val host = Host("Chef", StorytellerOperationMode.MANUAL, roles =
            listOf("Chef", "Baron", "Imp", "Soldier", "Saint"))
        ui.setContent { host.Content() }
        ui.onNodeWithText("Show to player: 1").performClick()
        ui.runOnIdle { host.enterOtherNight() }
        ui.onNodeWithText("Close display card").assertDoesNotExist()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(0, host.session.state.epistemicObservationLog.records.size) }
    }

    @Test fun confirmingOneInformationRoleCannotEnableTheNextRolesNext() {
        val host = Host("Chef", StorytellerOperationMode.MANUAL, roles =
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
        val host = Host("Chef", StorytellerOperationMode.MANUAL)
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
        val host = Host("Imp", StorytellerOperationMode.MANUAL, roles =
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
        val host = Host(role, StorytellerOperationMode.MANUAL, script, roles)
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
    private class Host(role: String, val mode: StorytellerOperationMode,
        private val script: ClocktowerScript = ClocktowerScript.TroubleBrewing,
        roles: List<String> = listOf(role, "Soldier",
            if (role == "Investigator") "Baron" else "Mayor", "Saint", "Recluse")) {
        val openingEnabled = mutableStateOf(true)
        val pairRole = mutableStateOf(when (role) {
            "Washerwoman" -> "Soldier"
            "Librarian" -> "Saint"
            "Investigator" -> "Baron"
            else -> "Chef"
        })
        val adviceKey = mutableStateOf<String?>(null)
        private val adviceError = mutableStateOf<String?>(null)
        private val choices = mutableStateOf<Map<String, SemanticResult>>(emptyMap())
        var saveErrors = 0
        var requests = 0
        var requestId: String? = null
        val cards = roles.mapIndexed { index, name ->
            val identity = requireNotNull(clocktowerRoleByName(name))
            PlayerCard("P${index + 1}", clocktowerRole = identity, clocktowerShownRole = identity,
                clocktowerTeam = identity.team)
        }
        private val game = GameState(script.toRecommendationScriptId(), cards.mapIndexed { index, card ->
            PlayerState(index + 1, card.name, RoleId(requireNotNull(card.clocktowerRole).enName),
                if(card.clocktowerTeam in setOf(ClocktowerTeam.Demon, ClocktowerTeam.Minion)) Alignment.EVIL else Alignment.GOOD,
                when(card.clocktowerTeam) {
                    ClocktowerTeam.Demon -> CharacterType.DEMON
                    ClocktowerTeam.Minion -> CharacterType.MINION
                    ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                    else -> CharacterType.TOWNSFOLK
                })
        }, 19L)
        var session = ClocktowerGameSession.createProduction("d2-ui", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        private val holderState = mutableStateOf(ClocktowerSessionUiState(session) { saveErrors++ })
        // Test-only externally injected Session changes publish through the production holder.
        fun refresh() { holderState.value.commit { } }
        fun enterDay() {
            session.commitConfirmedPhaseAdvance(com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.PhaseAdvance(
                "ui-day-${session.state.round}", session.state.phase, session.state.round, session.nextInputSequence, StorytellerPhase.DAY, session.state.round), session.state.gameStateRevision)
            refresh()
        }
        fun enterOtherNight() {
            enterDay()
            session.commitConfirmedPhaseAdvance(com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.PhaseAdvance(
                "ui-night", StorytellerPhase.DAY, 1, session.nextInputSequence, StorytellerPhase.NIGHT, 2), session.state.gameStateRevision)
            refresh()
        }
        fun advanceToNextNight() {
            enterDay()
            session.commitConfirmedPhaseAdvance(com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.PhaseAdvance(
                "ui-night-${session.state.round + 1}", StorytellerPhase.DAY, session.state.round, session.nextInputSequence,
                StorytellerPhase.NIGHT, session.state.round + 1), session.state.gameStateRevision)
            refresh()
        }
        fun reload() {
            holderState.value.deactivate()
            session = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(session.state)))
            holderState.value = ClocktowerSessionUiState(session) { saveErrors++ }
        }
        fun deliver(key: String?, id: String, value: String) {
            adviceKey.value = key
            adviceError.value = null
            choices.value = mapOf(id to SemanticResult(kind = "SCALAR_RESULT", scalarValue = value))
        }
        private fun request(steps: List<ClocktowerNightStepUi>, i: Int, @Suppress("UNUSED_PARAMETER") fortuneTellerNames: List<String>) {
            requests++
            val current = pendingGlobalNightScalarDecision(steps[i], ClocktowerPhase.FirstNight, 1, i,
                cards, session.state.gameId, StorytellerDecisionRevision(session.state.gameStateRevision, session.state.playerInputRevision))
                ?: return
            requestId = current.requestIdentity.requestId
            adviceKey.value = ClocktowerResidualRequestIdentityV2.build(session.state.gameId, 1, i,
                requireNotNull(requestId), session.state.gameStateRevision, session.state.playerInputRevision,
                session.state.nextTimelineGlobalSequence, mode)
            choices.value = emptyMap()
            adviceError.value = "Unavailable fixture"
        }

        @Composable fun Content() {
            val holder = holderState.value
            val state = holder.snapshot
            val ruleset = com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
                .fromContext(androidx.compose.ui.platform.LocalContext.current).ruleset(script)
            ClocktowerJudgeScreen(
                allowRuleDeterminedAutoUi = mode == StorytellerOperationMode.AI_AUTOMATIC,
                cards = ClocktowerSessionPlayerCards(emptyList(), { holder.snapshot }, "en"),
                sessionState = state,
                recommendedDemonBluffRoleNames = state.demonBluffRoleIds.map(RoleId::value),
                onHostTools = {},
                onCommitDemonBluffs = { names ->
                    session.commitDemonBluffs(names.map(::RoleId), state.phase, state.round, session.nextInputSequence)
                    refresh()
                },
                onConfirmNewDemon = { session.confirmImpNotification(); refresh() },
                onConfirmKlutzChoice = { chosenName ->
                    session.confirmKlutzChoice(requireNotNull(session.state.pendingKlutzDeath()).actionId,
                        cards.indexOfFirst { it.name == chosenName } + 1)
                    refresh()
                },
                onConfirmArtistQuestion = { _, _, _ -> },
                onSlayerShot = { _, _, _ -> },
                onConfirmedNomination = { _, _, _, _ -> false },
                onConfirmedVote = { a, b, vote, expectedRound ->
                    session.confirmVote(cards.indexOfFirst { it.name == a } + 1, cards.indexOfFirst { it.name == b } + 1,
                        vote.voters.map { v -> cards.indexOfFirst { it.name == v.playerName } + 1 }, expectedRound)
                    refresh()
                },
                onConfirmDay = { expectedRound -> session.confirmEndDay(expectedRound); refresh() },
                nightContent = {
                    ClocktowerNightHostScreen(
                        allowRuleDeterminedAutoUi = mode == StorytellerOperationMode.AI_AUTOMATIC,
                        cards = ClocktowerSessionPlayerCards(emptyList(), { holder.snapshot }, "en"),
                        sessionState = state,
                        globalAiAssisted = mode != StorytellerOperationMode.MANUAL,
                        globalAiAutomatic = mode == StorytellerOperationMode.AI_AUTOMATIC,
                        globalAiAdviceKey = adviceKey.value,
                        globalAiAdviceError = adviceError.value,
                        globalResidualRecommendedChoices = choices.value,
                        onRequestGlobalAiAdvice = ::request,
                        jointOpeningSemantics = if (!openingEnabled.value) emptyMap() else mapOf(1 to
                            if (cards[0].clocktowerRole?.enName in listOf("Washerwoman", "Librarian", "Investigator"))
                                HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                                    "FUNCTIONING", "EVIDENCE_PAIR",
                                    if (cards[0].clocktowerRole?.enName == "Librarian") listOf(3, 4)
                                    else listOf(2, 3), "ROLE", roleId = pairRole.value)
                            else HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                                "FUNCTIONING", "NONE", emptyList(), "NUMBER", number = 1)),
                        recommendedDemonBluffRoleNames = state.demonBluffRoleIds.map(RoleId::value),
                        onHostTools = {},
                        onRecordPlayerInput = { session.recordPlayerInput(); refresh() },
                        onConfirmNightStep = { confirmation ->
                            holder.commit { it.confirmNightStep(confirmation, ruleset, confirmMayorRedirect = {}) } ?: false
                        },
                        onAdvanceFromFirstNight = ::enterDay,
                        onCompleteNight = ::enterDay,
                    )
                },
            )
        }
    }
}
