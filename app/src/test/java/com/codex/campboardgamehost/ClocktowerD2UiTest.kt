package com.codex.campboardgamehost

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
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
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
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

    @Test fun numericNextUsesCurrentHostPreviewGate() {
        ui.setContent {
            ClocktowerDarkTheme {
                CompositionLocalProvider(LocalClocktowerNightNextEnabled provides false) {
                    ClocktowerDirectNumericResultUi(
                        title = "Chef", subjectSeats = listOf(1, 2), actorLabel = "P1",
                        wakeInstruction = null, language = "en", onHostTools = {}, onNext = {},
                        onConfirm = {}, draftKey = "g:first:Chef",
                    )
                }
            }
        }
        ui.onNodeWithText("Next").assertIsNotEnabled()
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
        if (mode != StorytellerOperationMode.MANUAL) {
            ui.onNode(hasClickAction() and hasText("P2 + P3", substring = true)).performClick()
        } else {
            ui.onNode(hasClickAction() and hasText("Choose character")).performClick()
            ui.onNodeWithText("Chef").performClick()
            ui.onNodeWithText("P2").performClick()
            ui.onNodeWithText("P3").performClick()
            ui.onNodeWithText("Show to player").performClick()
        }
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        if (mode != StorytellerOperationMode.AI_AUTOMATIC) {
            if (mode != StorytellerOperationMode.MANUAL) {
                ui.onNodeWithText("Manual selection").performClick()
            }
            ui.onNodeWithText("Chef").performClick()
            ui.onNodeWithText("Empath").performClick()
            ui.onNodeWithText("Next →").assertIsNotEnabled()
            // A late recommendation must not turn this manual edit back into the AI choice.
            ui.runOnIdle { host.pairRole.value = "Saint" }
            ui.onNodeWithText("Empath").assertExists()
            ui.onNodeWithText("P3").performClick()
            ui.onNodeWithText("P4").performClick()
            ui.onNodeWithText("Show to player").performClick()
            ui.onNodeWithText("Close display card").performClick()
        }
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            val observation = host.session.state.epistemicObservationLog.records.single()
            assertEquals(setOf(1), observation.recipientSeats)
            val pair = (observation.proposition as InformationProposition.AnyOf).alternatives
                .map { it as InformationProposition.RoleAt }
            assertEquals(if (mode == StorytellerOperationMode.AI_AUTOMATIC) listOf(2, 3) else listOf(3, 4), pair.map { it.seat })
            assertEquals(if (mode == StorytellerOperationMode.AI_AUTOMATIC) "Chef" else "Empath", pair.first().role.value)
        }
    }

    @Test fun automaticFailureRetryAndLateResponseStayOnCurrentRevision() {
        val host = Host("Chef", StorytellerOperationMode.AI_AUTOMATIC)
        host.openingEnabled.value = false
        ui.setContent { host.Content() }
        ui.onNodeWithText("Retry AI information").assertIsEnabled().performClick()
        ui.runOnIdle { assertEquals(2, host.requests) }
        val oldKey = host.adviceKey.value
        val requestId = requireNotNull(host.requestId)
        ui.runOnIdle { host.session.recordPlayerInput(); host.refresh() }
        ui.onNodeWithText("Next").assertIsNotEnabled()
        ui.runOnIdle { host.deliver(oldKey, requestId, "1") }
        ui.onNodeWithText("Show recommended: 1").assertDoesNotExist()
        ui.onNodeWithText("Next").assertIsNotEnabled()
        ui.onNodeWithText("Retry AI information").performClick()
        ui.runOnIdle { host.deliver(host.adviceKey.value, requireNotNull(host.requestId), "2") }
        ui.onNodeWithText("Show recommended: 2").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next").performTouchInput { doubleClick() }
        ui.runOnIdle { assertEquals(2, (host.session.state.epistemicObservationLog.records.single()
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
        ui.onNodeWithText("P2").performClick()
        ui.onNodeWithText("P4").performClick()
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
        ui.onNode(hasSetTextAction()).performTextReplacement("2")
        ui.onNodeWithText("Show manually selected result").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("P2").performClick()
        ui.onNodeWithText("P4").performClick()
        ui.onAllNodesWithText("Next →")[0].assertIsNotEnabled()
        ui.onNode(hasSetTextAction()).performTextReplacement("1")
        ui.onNodeWithText("Show manually selected result").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").performClick()
        ui.runOnIdle {
            assertEquals(listOf(4, 3), (f.confirmed.single() as InformationProposition.NumericResult).subjectSeats)
        }
    }

    @Test fun manualSageRetainsPairAndInvalidatesChangedSeats() {
        val f = RoleCard("Sage", StorytellerOperationMode.MANUAL)
        ui.setContent { f.Content() }
        ui.onNodeWithText("P2").performClick()
        ui.onNodeWithText("P3").performClick()
        ui.onNodeWithText("Show this pair").performClick()
        ui.onNodeWithText("Close display card").performClick()
        ui.onNodeWithText("Next →").assertIsEnabled()
        ui.onNodeWithText("P2").performClick()
        ui.onNodeWithText("Next →").assertIsNotEnabled()
        ui.onNodeWithText("P4").performClick()
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
                        cards = cards, ghostVoteAuthority = ClocktowerGhostVoteAuthority(), poisonedPlayerName = null,
                        aliveCards = cards, chambermaidTargetCards = cards, mayorRedirectTargetCards = emptyList(),
                        step = current, surfacePlan = clocktowerNightSurfacePlan(current, ClocktowerPhase.Night),
                        aiSuggestedInformationOption = advice.value.takeIf { mode != StorytellerOperationMode.MANUAL },
                        selectedName = "P2", fortuneTellerFirst = first.value, fortuneTellerSecond = second.value,
                        chambermaidFirst = first.value, chambermaidSecond = second.value, redHerring = null,
                        onSelectName = {}, onSelectFortuneTellerFirst = { first.value = it.takeUnless { it == first.value }; preview.value = null },
                        onSelectFortuneTellerSecond = { second.value = it.takeUnless { it == second.value }; preview.value = null },
                        onSelectChambermaidFirst = { first.value = it.takeUnless { it == first.value }; preview.value = null },
                        onSelectChambermaidSecond = { second.value = it.takeUnless { it == second.value }; preview.value = null },
                        onShowPlayerDisplay = { preview.value = it; fullscreen.value = true },
                        onInformationDraftChanged = { preview.value = null }, lastPreviewProposition = preview.value?.displayProposition,
                        canGoPrevious = false, onPrevious = {}, onHostTools = {},
                        onNext = { preview.value?.displayProposition?.let(confirmed::add) },
                    )
                }
            }
        }
    }

    private fun numericHost(mode: StorytellerOperationMode, role: String = "Chef") {
        val host = Host(role, mode)
        ui.setContent { host.Content() }
        ui.onNodeWithText("Next").assertIsNotEnabled()
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        if (mode == StorytellerOperationMode.AI_AUTOMATIC) {
            ui.onNode(hasSetTextAction()).assertDoesNotExist()
            ui.onNodeWithText("Show recommended: 1").performClick()
        } else {
            ui.onNode(hasSetTextAction()).performTextReplacement("2")
            ui.onNodeWithText("Show manually selected result").performClick()
        }
        // The real player display unmounts the host content. Hardware Back must close it.
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Next").assertIsEnabled()
        if (mode != StorytellerOperationMode.AI_AUTOMATIC) {
            ui.onNode(hasSetTextAction()).performTextReplacement("3")
            ui.onNodeWithText("Next").assertIsNotEnabled()
            ui.onNodeWithText("Show manually selected result").performClick()
            ui.onNodeWithText("Close display card").performClick()
        }
        assertEquals(0, host.session.state.epistemicObservationLog.records.size)
        ui.onNodeWithText("Next").performClick()
        ui.runOnIdle {
            val records = host.session.state.epistemicObservationLog.records
            assertEquals(1, records.size)
            assertEquals(setOf(1), records.single().recipientSeats)
            assertEquals(if (mode == StorytellerOperationMode.AI_AUTOMATIC) 1 else 3,
                (records.single().proposition as InformationProposition.NumericResult).value)
        }
    }

    /** Uses the production JudgeScreen and real Session; only external input/provider callbacks are fixtures. */
    private class Host(role: String, val mode: StorytellerOperationMode) {
        val openingEnabled = mutableStateOf(true)
        val pairRole = mutableStateOf("Chef")
        val adviceKey = mutableStateOf<String?>(null)
        private val adviceError = mutableStateOf<String?>(null)
        private val choices = mutableStateOf<Map<String, SemanticResult>>(emptyMap())
        var requests = 0
        var requestId: String? = null
        val cards = listOf(role, "Soldier", "Mayor", "Saint", "Recluse").mapIndexed { index, name ->
            val identity = requireNotNull(clocktowerRoleByName(name))
            PlayerCard("P${index + 1}", clocktowerRole = identity, clocktowerShownRole = identity,
                clocktowerTeam = identity.team)
        }
        private val game = GameState(ScriptId("trouble_brewing"), cards.mapIndexed { index, card ->
            PlayerState(index + 1, card.name, RoleId(requireNotNull(card.clocktowerRole).enName),
                Alignment.GOOD, if (card.clocktowerTeam == ClocktowerTeam.Outsider) CharacterType.OUTSIDER
                else CharacterType.TOWNSFOLK)
        }, 19L)
        val session = ClocktowerGameSession.createProduction("d2-ui", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        private val tick = mutableStateOf(0)
        private val started = mutableStateOf(false)
        private val index = mutableStateOf(0)
        private val dayMode = mutableStateOf(ClocktowerDayMode.Overview)
        fun refresh() { tick.value++ }
        fun deliver(key: String?, id: String, value: String) {
            adviceKey.value = key
            adviceError.value = null
            choices.value = mapOf(id to SemanticResult(kind = "SCALAR_RESULT", scalarValue = value))
        }
        private fun request(steps: List<ClocktowerNightStepUi>, i: Int) {
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
            tick.value
            val state = session.state
            ClocktowerJudgeScreen(
                allowRuleDeterminedAutoUi = mode == StorytellerOperationMode.AI_AUTOMATIC,
                cards = cards, sessionGameState = state.gameState, sessionActionTimeline = state.actionTimeline,
                actualInformationHistory = state.epistemicObservationLog.records, confirmedNightDeathSeats = emptyList(),
                script = ClocktowerScript.TroubleBrewing, gameId = state.gameId, gameSeed = state.gameSeed,
                globalAiAssisted = mode != StorytellerOperationMode.MANUAL,
                globalAiAutomatic = mode == StorytellerOperationMode.AI_AUTOMATIC,
                globalAiAdviceKey = adviceKey.value, globalAiAdviceError = adviceError.value,
                globalResidualRecommendedChoices = choices.value, onRequestGlobalAiAdvice = ::request,
                jointOpeningSemantics = if (!openingEnabled.value) emptyMap() else mapOf(1 to
                    if (cards[0].clocktowerRole?.enName in listOf("Washerwoman", "Librarian", "Investigator"))
                        HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                            "FUNCTIONING", "EVIDENCE_PAIR", listOf(2, 3), "ROLE", roleId = pairRole.value)
                    else HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                        "FUNCTIONING", "NONE", emptyList(), "NUMBER", number = 1)),
                phase = ClocktowerPhase.FirstNight, round = 1,
                nightCheckpoint = ClocktowerNightCheckpoint("FIRST_NIGHT", 1, state.gameStateRevision, state.playerInputRevision,
                    null, null, null, null, null, null, null, null, null, nextTimelineGlobalSequence = state.nextTimelineGlobalSequence),
                selectedExecution = null, fortuneTellerFirst = null, fortuneTellerSecond = null,
                chambermaidFirst = null, chambermaidSecond = null, ravenkeeperTarget = null, redHerring = null,
                recommendedDemonBluffRoleNames = emptyList(), butlerMaster = null,
                virginUsed = false, artistUsed = false, artistClaimedNames = emptyList(), lastExecutedName = null,
                pendingKlutzName = null, klutzChoiceName = null, nightStartedState = started, nightStepIndexState = index,
                dayModeState = dayMode, ghostVoteAuthority = ClocktowerGhostVoteAuthority(), highestVoteName = null,
                highestVoteCount = 0, gameOutcome = null,
                onCommitConfirmedPrivateInformationPublication = { draft, revision ->
                    session.commitGlobalEpistemicObservation(draft, revision)
                    tick.value++
                },
                onHostTools = {}, onSelectNightDeath = {}, onConfirmDemonAttack = { _, _ -> },
                onSelectExecution = {}, onSelectPoisonTarget = {}, onConfirmPoisonTarget = {},
                onSelectFortuneTellerFirst = {}, onSelectFortuneTellerSecond = {},
                onSelectChambermaidFirst = {}, onSelectChambermaidSecond = {}, onSelectRavenkeeperTarget = {},
                onSelectRedHerring = {}, onConfirmRedHerring = {}, onCommitDemonBluffs = {},
                onSelectButlerMaster = {}, onConfirmButlerMaster = { _, _, _ -> },
                onSelectMonkProtectedTarget = {}, onConfirmMonkProtectedTarget = {},
                onSelectMayorRedirectTarget = {}, onConfirmMayorRedirectTarget = {}, onConfirmNewDemon = {},
                onSelectKlutzChoice = {}, onConfirmKlutzChoice = {}, onConfirmArtistQuestion = {},
                onSlayerShot = { _, _, _ -> }, onVirginNomination = { _, _, _ -> },
                onConfirmedNomination = { _, _, _ -> }, onConfirmedVote = { _, _, _ -> },
                onAdvanceFromFirstNight = {}, onConfirmDay = {}, onCompleteNight = {},
            )
        }
    }
}
