package com.codex.campboardgamehost

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.onNodeWithText
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

/** Real role card + real fullscreen; callbacks expose the chosen result to the D2 tests. */
internal class D2TestRoleCard(val role: String, val mode: StorytellerOperationMode) {
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
                    // Card-only discretion fixture: the source is impaired, so arbitrary shown roles are deliberate.
                    roleResolution = if (role in setOf("Undertaker", "Ravenkeeper")) HostRoleResolution(
                        clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).map { RoleId(it.enName) }, 1, 2,
                        com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState.POISONED,
                    ) else null,
                    aiSuggestedInformationOption = advice.value.takeIf { mode != StorytellerOperationMode.MANUAL },
                    selectedSeatSlots = if (twoSeatQuery) slots() else listOf(2), redHerringSeat = null,
                    redHerringSelectableSeats = emptySet(),
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


internal class D2TestHost(role: String, val mode: StorytellerOperationMode,
    private val script: ClocktowerScript = ClocktowerScript.TroubleBrewing,
    roles: List<String> = listOf(role, "Soldier",
        if (role == "Investigator") "Baron" else "Mayor", "Saint", "Recluse")) {
    val openingEnabled = mutableStateOf(true)
    val openingRedHerringSeat = mutableStateOf<Int?>(null)
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
        val opening = if (!openingEnabled.value) emptyMap() else mapOf(1 to
            if (cards[0].clocktowerRole?.enName in setOf("Washerwoman", "Librarian", "Investigator"))
                HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                    "FUNCTIONING", "EVIDENCE_PAIR",
                    if (cards[0].clocktowerRole?.enName == "Librarian") listOf(3, 4) else listOf(2, 3),
                    "ROLE", roleId = pairRole.value)
            else HostOpeningSemanticValueV2(1, requireNotNull(cards[0].clocktowerRole).enName, null,
                "FUNCTIONING", "NONE", emptyList(), "NUMBER", number = 1))
        val openingUsable = openingEnabled.value && ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
            state, null, opening, openingRedHerringSeat.value, state.demonBluffRoleIds.map(RoleId::value))
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
                    jointOpeningAvailable = openingUsable,
                    recommendedRedHerringSeat = openingRedHerringSeat.value.takeIf { openingUsable },
                    jointOpeningSemantics = opening.takeIf { openingUsable }.orEmpty(),
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
