package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.widget.Toast
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.ClocktowerResidualDisplayMatcherV2
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.NightOperationKind
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightPublicationResolution
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerJudgeScreen(
    allowRuleDeterminedAutoUi: Boolean,
    cards: List<PlayerCard>,
    // Confirmed mechanical facts come from the one Session, never rebuilt from UI cards.
    sessionGameState: GameState,
    actualInformationHistory: List<RecordedEpistemicObservation>,
    confirmedNightDeathSeats: List<Int>,
    script: ClocktowerScript,
    gameId: String,
    gameSeed: Long,
    globalAiAssisted: Boolean = false,
    /** A verified joint plan is advisory; only confirmed current steps affect Session. */
    globalAiAutomatic: Boolean = false,
    jointOpeningAvailable: Boolean = false,
    jointOpeningSemantics: Map<Int, com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2> = emptyMap(),
    globalNightPendingDecision: PendingMayorRedirectDecision? = null,
    globalAiAdviceKey: String? = null,
    globalAiAdviceText: String? = null,
    globalAiAdviceBusy: Boolean = false,
    globalAiAdviceError: String? = null,
    globalResidualRecommendedChoices: Map<String, com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult> = emptyMap(),
    onRequestGlobalAiAdvice: (List<ClocktowerNightStepUi>, Int) -> Unit = { _, _ -> },
    phase: ClocktowerPhase,
    round: Int,
    nightCheckpoint: ClocktowerNightCheckpoint,
    selectedExecution: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    ravenkeeperTarget: String?,
    redHerring: String?,
    openingRecommendedRedHerring: String? = null,
    recommendedDemonBluffRoleNames: List<String>,
    butlerMaster: String?,
    virginUsed: Boolean,
    artistUsed: Boolean,
    artistClaimedNames: List<String>,
    lastExecutedName: String?,
    pendingKlutzName: String?,
    klutzChoiceName: String?,
    nightStartedState: MutableState<Boolean>,
    nightStepIndexState: MutableState<Int>,
    dayModeState: MutableState<ClocktowerDayMode>,
    ghostVoteAuthority: ClocktowerGhostVoteAuthority,
    highestVoteName: String?,
    highestVoteCount: Int,
    gameOutcome: GameOutcome?,
    onCommitConfirmedPrivateInformationPublication: (EpistemicObservationDraft, StorytellerDecisionRevision) -> Unit,
    onHostTools: () -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onConfirmDemonAttack: (String, String?) -> Unit,
    onSelectExecution: (String?) -> Unit,
    onSelectPoisonTarget: (String?) -> Unit,
    onConfirmPoisonTarget: (String) -> Unit,
    onSelectFortuneTellerFirst: (String?) -> Unit,
    onSelectFortuneTellerSecond: (String?) -> Unit,
    onSelectChambermaidFirst: (String?) -> Unit,
    onSelectChambermaidSecond: (String?) -> Unit,
    onSelectRavenkeeperTarget: (String?) -> Unit,
    onSelectRedHerring: (String?) -> Unit,
    onConfirmRedHerring: (String) -> Unit,
    onCommitDemonBluffs: (List<String>) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onConfirmButlerMaster: (String, String, String) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onConfirmMonkProtectedTarget: (String) -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onConfirmMayorRedirectTarget: () -> Unit,
    onConfirmNewDemon: () -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: () -> Unit,
    onConfirmArtistQuestion: (String) -> Unit,
    onSlayerShot: (String, String, Boolean) -> Unit,
    onPreflightVirginExecution: (String) -> Unit,
    onVirginNomination: (String, String, Boolean) -> Unit,
    onConfirmedNomination: (String, String, Boolean?) -> Unit,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onCompleteNight: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameStateRevision = nightCheckpoint.gameStateRevision
    val playerInputRevision = nightCheckpoint.playerInputRevision
    val pendingNightDeath = nightCheckpoint.confirmedAttackTarget
    val demonAttackDraftTarget = nightCheckpoint.attackDraftTarget
    val poisonTarget = nightCheckpoint.confirmedPoisonTarget
    val poisonDraftTarget = nightCheckpoint.poisonDraftTarget
    val monkProtectedTarget = nightCheckpoint.confirmedMonkTarget
    val monkProtectedDraftTarget = nightCheckpoint.monkDraftTarget
    val mayorRedirectTarget = nightCheckpoint.confirmedMayorRedirectTarget
    val mayorRedirectDraftTarget = nightCheckpoint.mayorRedirectDraftTarget
    val pendingNewDemonName = nightCheckpoint.pendingNewDemonName
    val pendingNightNewDemonIdentityName = nightCheckpoint.pendingNightNewDemonIdentityName
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val publicAliveCards = cards.filter { it.eliminatedRound == null }
    // The UI still owns rendering, but first-night information now crosses one
    // common lifecycle boundary before it is committed to the event log.
    var firstNightInformationMigration by remember(gameId, round) {
        mutableStateOf(FirstNightInformationMigration())
    }
    var observedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf<String?>(null) }
    var hasObservedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf(false) }
    LaunchedEffect(phase, poisonTarget) {
        if (phase != ClocktowerPhase.FirstNight) return@LaunchedEffect
        if (hasObservedFirstNightPoisonTarget && observedFirstNightPoisonTarget != poisonTarget) {
            firstNightInformationMigration = firstNightInformationMigration.invalidateUnshown()
        }
        observedFirstNightPoisonTarget = poisonTarget
        hasObservedFirstNightPoisonTarget = true
    }

    val registrationState = remember { ClocktowerRegistrationInteractionState() }
    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var pendingNightAdvance by remember(gameId, round, phase) {
        mutableStateOf<ClocktowerNightAdvanceDirective.AwaitRefreshedFlow?>(null)
    }
    val nominationDraft = remember(gameId, round) { ClocktowerNominationDraft() }
    val slayerDraft = remember(gameId) { ClocktowerSlayerDraft() }
    val artistDraft = remember(gameId) { ClocktowerArtistDraft() }
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    // Transient UI handoff only; Session publication is the lasting fact.
    // Advancing from a confirmed player display must wait for its dismissal.
    var pendingAutoAdvanceAfterDisplay by remember(gameId, phase, round) {
        mutableStateOf<Pair<Int, String>?>(null)
    }
    val legalDemonBluffs = clocktowerRolesForScript(script).distinctBy { it.enName }
    val legalDemonBluffRoleNames = legalDemonBluffs.mapTo(linkedSetOf()) { it.enName }
    var manualDemonBluffDraft by remember(
        gameId,
        recommendedDemonBluffRoleNames,
        legalDemonBluffRoleNames,
    ) {
        mutableStateOf(
            recommendedDemonBluffRoleNames
                .filter { it in legalDemonBluffRoleNames }
                .distinct()
                .take(3),
        )
    }

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            cards = cards,
            // The current reveal's publication callback owns the one-shot
            // advance token. Dismissing an older/reopened reveal cannot create it.
            onDismiss = { playerDisplayStep = null },
        )
        return
    }

    pendingNewDemonName?.let { newDemonName ->
        val newDemon = cards.firstOrNull { it.name == newDemonName }
        val newDemonStep = ClocktowerNightStepUi(
            title = text("新恶魔", "New Demon"),
            actor = newDemon,
            isRealAction = newDemon != null,
            reason = "",
            storytellerAction = text("唤醒新的小恶魔。", "Wake the new Imp."),
            tellPlayer = text("你现在是小恶魔", "You are now the Imp"),
            explanation = "",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = text("新身份", "New role"),
            displayPrimary = text("你现在是小恶魔", "You are now the Imp"),
            displayFooter = "",
        )
        ClocktowerNewDemonConfirmationScreen(
            newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
            hasNewDemon = newDemon != null,
            compact = false,
            onHostTools = onHostTools,
            onShowPlayerDisplay = { playerDisplayStep = newDemonStep },
            onConfirm = onConfirmNewDemon,
        )
        return
    }

    if (phase == ClocktowerPhase.Day) {
        val executionThreshold = (publicAliveCards.size + 1) / 2
        val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
        val scriptHasSlayer = "Slayer" in scriptRoleNames
        val scriptHasArtist = "Artist" in scriptRoleNames
        val artistClaimantCandidates = publicAliveCards.filter { card ->
            card.name !in artistClaimedNames && !(artistUsed && card.clocktowerRole?.enName == "Artist")
        }
        when (dayMode) {
            ClocktowerDayMode.Overview -> {
                val highestVoteText = when {
                    highestVoteName != null -> text(
                        "最高票 · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                        "Highest · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
                    )
                    highestVoteCount >= executionThreshold -> text(
                        "最高票 · 平票 $highestVoteCount 票",
                        "Highest · tie at $highestVoteCount",
                    )
                    else -> text("最高票 · 无", "Highest · none")
                }
                val dayTableState = clocktowerDayOverviewTableState(
                    sessionGameState,
                    roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                    ghostVoteAuthority = ghostVoteAuthority,
                )
                ClocktowerDayOverviewScreen(
                    round = round,
                    tableState = dayTableState,
                    aliveCount = publicAliveCards.size,
                    executionThreshold = executionThreshold,
                    highestVoteText = highestVoteText,
                    lastNightDeathLabels = if (round > 1) confirmedNightDeathSeats.mapNotNull { seat ->
                        sessionGameState.playerAt(seat)?.name?.let { playerSeatLabel(cards, it) }
                    } else null,
                    showSlayerAction = scriptHasSlayer,
                    slayerActionEnabled = cards.isNotEmpty(),
                    showArtistAction = scriptHasArtist,
                    artistActionEnabled = artistClaimantCandidates.isNotEmpty(),
                    actionsEnabled = gameOutcome == null,
                    diagnosticContent = null,
                    onHostTools = onHostTools,
                    onNominationGesture = { sourceSeatId, targetSeatId ->
                        val sourceName = dayTableState.seats
                            .firstOrNull { seat -> seat.seatId == sourceSeatId && seat.isAlive }
                            ?.playerName
                        val targetName = dayTableState.seats
                            .firstOrNull { seat -> seat.seatId == targetSeatId && seat.isAlive }
                            ?.playerName
                        if (sourceName != null && targetName != null && sourceName != targetName) {
                            nominationDraft.select(sourceName, targetName)
                            dayMode = ClocktowerDayMode.Nomination
                        }
                    },
                    onOpenSlayer = {
                        slayerDraft.clear()
                        dayMode = ClocktowerDayMode.Slayer
                    },
                    onOpenArtist = {
                        artistDraft.selectClaimant(null)
                        dayMode = ClocktowerDayMode.Artist
                    },
                    onEndDay = {
                        onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                        dayMode = ClocktowerDayMode.EndConfirm
                    },
                )
            }

            ClocktowerDayMode.Nomination -> {
                ClocktowerNominationInteraction(
                    draft = nominationDraft,
                    cards = cards,
                    tableState = clocktowerDayOverviewTableState(
                        sessionGameState,
                        roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                        ghostVoteAuthority = ghostVoteAuthority,
                    ),
                    registrationState = registrationState,
                    round = round,
                    executionThreshold = executionThreshold,
                    poisonTarget = poisonTarget,
                    virginUsed = virginUsed,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onConfirm = { confirmation ->
                        if (confirmation.firstVirginNomination && confirmation.virginExecutes) {
                            onPreflightVirginExecution(confirmation.nominator)
                        }
                        onConfirmedNomination(
                            confirmation.nominator, confirmation.nominee, confirmation.explicitSpyRegistration,
                        )
                        if (confirmation.firstVirginNomination) {
                            onVirginNomination(
                                confirmation.nominator, confirmation.nominee, confirmation.virginExecutes,
                            )
                        }
                        if (!confirmation.virginExecutes) dayMode = ClocktowerDayMode.Vote
                    },
                    onCancel = { dayMode = ClocktowerDayMode.Overview },
                )
            }

            ClocktowerDayMode.Vote -> {
                val highestVoteText = when {
                    highestVoteName != null -> text(
                        "当前最高：${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                        "Current highest: ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
                    )
                    highestVoteCount >= executionThreshold -> text(
                        "当前最高为平票：$highestVoteCount 票；暂时无人被处决。",
                        "Current high vote is tied at $highestVoteCount; nobody is set for execution.",
                    )
                    else -> text("当前还没有达到门槛的最高票。", "No qualifying high vote has been recorded yet.")
                }
                ClocktowerVoteTableScreen(
                    round = round,
                    cards = cards,
                    ghostVoteAuthority = ghostVoteAuthority,
                    tableState = clocktowerDayOverviewTableState(
                        sessionGameState,
                        roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                        ghostVoteAuthority = ghostVoteAuthority,
                    ),
                    executionThreshold = executionThreshold,
                    nominatorName = nominationDraft.nominatorName,
                    nomineeName = nominationDraft.nomineeName,
                    highestVoteText = highestVoteText,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onConfirm = { voteState ->
                        nominationDraft.confirmVote(voteState.confirmedVoteRecord(), onConfirmedVote)
                        dayMode = ClocktowerDayMode.Overview
                    },
                    onCancel = {
                        dayMode = ClocktowerDayMode.Nomination
                    },
                )
            }

            ClocktowerDayMode.EndConfirm -> {
                ClocktowerExecutionConfirmScreen(
                    round = round,
                    cards = cards,
                    executionThreshold = executionThreshold,
                    selectedExecution = selectedExecution,
                    highestVoteCount = highestVoteCount,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onConfirm = onConfirmDay,
                    onBack = { dayMode = ClocktowerDayMode.Overview },
                )
            }

            ClocktowerDayMode.Slayer -> {
                ClocktowerSlayerInteraction(
                    draft = slayerDraft,
                    seats = clocktowerDayOverviewTableState(
                        sessionGameState,
                        roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                        ghostVoteAuthority = ghostVoteAuthority,
                    ).seats,
                    round = round,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onSlayerShot = onSlayerShot,
                    onBack = { dayMode = ClocktowerDayMode.Overview },
                )
            }

            ClocktowerDayMode.Artist -> {
                ClocktowerArtistInteraction(
                    draft = artistDraft,
                    cards = cards,
                    seats = clocktowerDayOverviewTableState(
                        sessionGameState,
                        roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                        ghostVoteAuthority = ghostVoteAuthority,
                    ).seats,
                    claimantCandidateNames = artistClaimantCandidates.mapTo(mutableSetOf()) { it.name },
                    round = round,
                    poisonTarget = poisonTarget,
                    allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onConfirm = onConfirmArtistQuestion,
                    onBack = { dayMode = ClocktowerDayMode.Overview },
                )
            }

            ClocktowerDayMode.Klutz -> {
                val klutzTableState = clocktowerKlutzTableState(
                    seats = clocktowerDayOverviewTableState(
                        sessionGameState,
                        roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                        ghostVoteAuthority = ghostVoteAuthority,
                    ).seats,
                    klutzName = pendingKlutzName,
                    alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name },
                    choiceName = klutzChoiceName,
                )
                ClocktowerKlutzTableScreen(
                    round = round,
                    tableState = klutzTableState,
                    actionsEnabled = gameOutcome == null,
                    onHostTools = onHostTools,
                    onSeatClick = { seatId ->
                        val playerName = klutzTableState.playerNameForSeat(seatId)
                        onSelectKlutzChoice(if (klutzChoiceName == playerName) null else playerName)
                    },
                    onConfirm = onConfirmKlutzChoice,
                )
            }
        }
        return
    }

    fun firstNightPublicationNeeded(displayStep: ClocktowerNightStepUi): Boolean {
        val request = clocktowerFirstNightInformationRequest(
            displayStep, phase, round, cards,
        ) ?: return true
        return !firstNightInformationMigration.isDisplayed(request.decisionId)
    }
    fun publishFirstNightInformation(displayStep: ClocktowerNightStepUi): Boolean {
        val request = clocktowerFirstNightInformationRequest(
            displayStep,
            phase,
            round,
            cards,
        ) ?: return true
        return when (val result = firstNightInformationMigration.resolvePublication(request)) {
            is FirstNightPublicationResolution.Published -> {
                firstNightInformationMigration = result.migration
                true
            }
            FirstNightPublicationResolution.AlreadyDisplayed -> false
        }
    }
    val spySeatLabel = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }?.seatLabel(cards)
    val nightHostProjection = ClocktowerNightHostProjectionFactory.project(
        cards = cards,
        gameState = sessionGameState,
        ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
        phase = phase,
        poisonTarget = poisonTarget,
        checkpoint = nightCheckpoint,
        pendingNightNewDemonIdentityName = pendingNightNewDemonIdentityName,
        lastExecutedName = lastExecutedName,
        confirmedNightDeathSeats = confirmedNightDeathSeats,
    )
    val mayorCanRedirect = nightHostProjection.mayorCanRedirect
    val mayorTarget = nightHostProjection.mayorTarget
    val mayorRedirectTargetCards = nightHostProjection.mayorRedirectTargetCards
    val ravenkeeperTrigger = nightHostProjection.ravenkeeperTrigger
    val demonCard = nightHostProjection.demonCard
    val demonPoisonedForActionExplanation = nightHostProjection.demonPoisonedForActionExplanation
    val sageNightDeath = nightHostProjection.sageNightDeath
    val otherNightInteractions = nightHostProjection.otherNightInteractions
    val otherNightCanonicalInteractionIds = nightHostProjection.otherNightCanonicalInteractionIds
    val chambermaidTargetCards = nightHostProjection.chambermaidTargetCards
    val ravenkeeperDeathTriggerAbilityState = nightHostProjection.ravenkeeperDeathTriggerAbilityState
    val sageDeathTriggerAbilityState = nightHostProjection.sageDeathTriggerAbilityState

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? =
        nightHostProjection.effectiveAbilitySubjectForRole(enName, actor)

    val effectivePoisonForRole = nightHostProjection::effectivePoisonForRole
    val chambermaidSelection = revalidateTwoPlayerSelection(
        first = chambermaidFirst, second = chambermaidSecond,
        eligibleNames = chambermaidTargetCards.mapTo(mutableSetOf()) { it.name },
    )
    val chambermaidPresentation = clocktowerChambermaidSelectionPresentation(cards, chambermaidSelection)
    val recommendationKey = buildString {
        append(script.name)
        append("|seed:")
        append(gameSeed)
        append("|state:")
        append(gameStateRevision)
        append("|input:")
        append(playerInputRevision)
        append("|phase:")
        append(phase.name)
        append("|round:")
        append(round)
        append("|poison:")
        append(poisonTarget.orEmpty())
        cards.forEachIndexed { index, card ->
            append('|')
            append(index + 1)
            append(':')
            append(card.clocktowerRole?.enName.orEmpty())
        }
    }
    fun roleActor(enName: String): PlayerCard? {
        if (phase != ClocktowerPhase.Night) {
            return cards.firstOrNull {
                AbilityFunctioningSemantics.interactsAs(
                    it.abilitySubject(null),
                    enName,
                )
            }
        }
        val interactionId = ClocktowerProductionNightStepIdentity
            .role(RoleId(enName))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)

        if (interactionId !in otherNightCanonicalInteractionIds) {
            return null
        }

        return cards.firstOrNull { candidate ->
            val effectiveSubject =
                effectiveAbilitySubjectForRole(enName, candidate)
                    ?: return@firstOrNull false

            AbilityFunctioningSemantics.interactsAs(
                effectiveSubject,
                enName,
            )
        }
    }

    fun roleMissingReason(enName: String): String {
        val roleCard = actualClocktowerRoleCards(cards, enName).firstOrNull()
        val drunkShownAsRole = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName }
        return when {
            roleCard == null && drunkShownAsRole != null -> ""
            roleCard == null -> text("本局没有这个角色。", "This character is not in play.")
            roleCard.eliminatedRound != null -> text(
                "${roleCard.seatLabel(cards)} 已经死亡，死亡后不再执行这个能力。",
                "${roleCard.seatLabel(cards)} is dead and no longer uses this ability.",
            )
            else -> ""
        }
    }

    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        (effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
            AbilityFunctioningSemantics.stateFor(subject, enName)
        } in
            setOf(AbilityFunctioningState.DRUNK, AbilityFunctioningState.POISONED)
        )
    val informationStepBuilder = ClocktowerInformationStepBuilder(
        cards = cards,
        language = language,
        text = ::text,
        roleActor = ::roleActor,
        roleMissingReason = ::roleMissingReason,
        abilityStateFor = { enName, actor ->
            effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
                AbilityFunctioningSemantics.stateFor(subject, enName)
            }
        },
    )

    val empathActor = roleActor("Empath")
    val effectiveEmpathCards = cards.filterIndexed { index, _ ->
        sessionGameState.playerAt(index + 1)?.alive == true
    }
    val empathNeighbors = empathActor?.let { livingNeighbors(effectiveEmpathCards, it.name) }.orEmpty()
    // Capture query seats before the information-publication callbacks that record the
    // actual Fortune Teller result; the materializer below reuses this same seat list.
    val fortuneTellerSelectedNames = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
    val fortuneTellerSubjectSeats = fortuneTellerSelectedNames.mapNotNull { name ->
        cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    }
    fun informationDecisionPublicationAllowed(displayStep: ClocktowerNightStepUi): Boolean =
        clocktowerInformationPublicationAllowed(
            sourceRevision = displayStep.informationSourceRevision,
            currentRevision = StorytellerDecisionRevision(gameStateRevision, playerInputRevision),
        )
    fun unreliableInformationCanBeRecorded(displayStep: ClocktowerNightStepUi): Boolean {
        if (!clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) return true
        if (displayStep.displayKind !in setOf(
                ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo,
                ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
            )) return true
        val actorSeat = displayStep.actor?.let { cards.indexOf(it) + 1 }
            ?.takeIf { it > 0 } ?: return false
        return clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        ) != null
    }
    fun recordReliablePrivateInformation(displayStep: ClocktowerNightStepUi) {
        val actor = displayStep.actor ?: return
        val actorSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return
        if (!informationDecisionPublicationAllowed(displayStep)) return
        val unreliable = clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
        val shownUnreliable = if (unreliable) clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        ) else null
        // Never silently omit misinformation after the first-night marker was accepted.
        // In particular, these fallbacks are TRUE answers and may only serve reliable abilities.
        if (unreliable) {
            if (displayStep.displayKind !in setOf(
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo,
                    ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
                )) return
            requireNotNull(shownUnreliable) { "Actual unreliable displayed information lacks a typed proposition." }
        }
        val proposition = shownUnreliable ?: displayStep.displayProposition ?: return
        onCommitConfirmedPrivateInformationPublication(EpistemicObservationDraft(
            recordId = clocktowerPrivateObservationRecordId(
                gameId = gameId,
                phase = phase,
                round = round,
                roleEnName = requireNotNull(displayStep.roleEnName),
                actorSeat = actorSeat,
                proposition = proposition,
            ),
            phase = phase.toStorytellerPhase(),
            round = round, sequence = nightStepIndex, sourceSeat = actorSeat,
            sourceAbility = RoleId(requireNotNull(displayStep.roleEnName)), visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(actorSeat),
            reliability = if (clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) {
                ObservationReliability.KNOWN_MALFUNCTIONING
            } else {
                ObservationReliability.RECEIVED_AS_FUNCTIONING
            },
            proposition = proposition,
        ), displayStep.informationSourceRevision ?: StorytellerDecisionRevision(gameStateRevision, playerInputRevision))
    }
    val undertakerTarget = lastExecutedName?.let { name -> cards.firstOrNull { it.name == name } }
    val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val minionCards = cards.filter { it.clocktowerRole?.team == ClocktowerTeam.Minion }
    fun seatNumberText(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
    val manualDemonBluffsRequired = shouldGiveFirstNightEvilInfo && demonCard != null
    val manualDemonBluffsReady = !manualDemonBluffsRequired ||
        manualDemonBluffSelectionReady(
            selectedRoleNames = manualDemonBluffDraft,
            legalRoles = legalDemonBluffs,
        )
    val demonBluffRoleNames = recommendedDemonBluffRoleNames.takeIf { it.isNotEmpty() }
    val demonBluffPresentation = resolveDemonBluffPresentation(
        recommendedRoleNames = demonBluffRoleNames,
        legalRoles = legalDemonBluffs,
    )
    val firstNightEvilInformationSteps = clocktowerFirstNightEvilInformationSteps(
        minionActor = minionCards.firstOrNull(),
        demonActor = demonCard,
        minionSeatLabels = minionCards.map { it.seatLabel(cards) },
        demonSeatLabel = demonCard?.seatLabel(cards),
        shouldGiveInformation = shouldGiveFirstNightEvilInfo,
        demonBluffPresentation = demonBluffPresentation,
        language = language,
    )
    // Session owns true and shown roles; player cards are only a presentation surface.
    val firstNightActualRoleIds = sessionGameState.players.mapTo(linkedSetOf()) { it.actualRole }
    val firstNightWakingRoleIds = clocktowerNightWakingRoleIds(
        gameState = sessionGameState,
        includeDead = true,
    )
    val chambermaidMaterializer = clocktowerChambermaidStepMaterializer(
        builder = informationStepBuilder,
        content = ClocktowerChambermaidStepContent(
            explanation = text("记录侍女实际查询的目标和展示数字。", "Record the Chambermaid's actual targets and shown number."),
            displayFooter = text("查询这两名玩家", "Checking these two players"),
            hostInstruction = text("唤醒侍女，记录她选择的两名玩家，再输入展示数字。", "Wake the Chambermaid, record the two chosen players, then enter the shown number."),
        ),
        presentation = chambermaidPresentation,
    )
    val empathStepMaterializer = clocktowerEmpathStepMaterializer(
        informationStepBuilder, empathNeighbors.map { cards.indexOf(it) + 1 }, ::text,
    )
    val fortuneTellerStepMaterializer = clocktowerFortuneTellerStepMaterializer(
        informationStepBuilder, fortuneTellerSubjectSeats, ::text,
    )

    val nightSteps = if (phase == ClocktowerPhase.FirstNight) {
        val firstNightInteractions =
            ClocktowerProductionFirstNightFlow.interactions(
                ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
                playerCount = cards.size,
                inPlayRoleIds = firstNightWakingRoleIds,
                actualRoleIds = firstNightActualRoleIds,
            )
        val firstNightMaterializers = ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = listOf(
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.minionInfo(),
            build = { firstNightEvilInformationSteps.minion },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.demonInfo(),
            build = { firstNightEvilInformationSteps.demon },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner")),
            build = {
                informationStepBuilder.build(
                                roleName = "投毒者",
                                enName = "Poisoner",
                                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                                action = ClocktowerNightAction.Poison,
                                displayKind = ClocktowerDisplayKind.None,
                                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring(),
            build = {
                ClocktowerNightStepUi(
                                title = text("占卜师红鲱鱼", "Fortune Teller red herring"),
                                actor = null,
                                isRealAction = actualClocktowerRoleCards(cards, "Fortune Teller").isNotEmpty(),
                                reason = if (actualClocktowerRoleCards(cards, "Fortune Teller").isEmpty()) text("本局没有占卜师，此步骤只用于首夜配置。", "No Fortune Teller is in play; this is only a first-night setup step.") else "",
                                storytellerAction = text("不要公开说明这个选择。请选择一名善良玩家作为红鲱鱼；可以选择占卜师本人。", "Keep this choice private. Choose a good player as the red herring; the Fortune Teller may be chosen."),
                                tellPlayer = redHerring?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                                explanation = text("选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。", "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon."),
                                action = ClocktowerNightAction.RedHerring,
                                roleEnName = "Fortune Teller",
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Clockmaker")),
            build = {
                informationStepBuilder.build(
                    roleName = "钟表匠", enName = "Clockmaker", tellPlayer = null,
                    explanation = text("输入实际展示的数字。", "Enter the actual shown number."),
                    informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.STEPS_TO_NEAREST_MINION, cards.indices.map { it + 1 }),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Washerwoman")),
            build = {
                informationStepBuilder.build(
                    roleName = "洗衣妇", enName = "Washerwoman", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Librarian")),
            build = {
                informationStepBuilder.build(
                    roleName = "图书管理员", enName = "Librarian", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Investigator")),
            build = {
                informationStepBuilder.build(
                    roleName = "调查员", enName = "Investigator", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Chef")),
            build = {
                informationStepBuilder.build(
                    roleName = "厨师", enName = "Chef", tellPlayer = null,
                    explanation = text("输入实际展示的数字。", "Enter the actual shown number."),
                    informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indices.map { it + 1 }),
                )
            },
        ),
        empathStepMaterializer,
        chambermaidMaterializer,
        fortuneTellerStepMaterializer,
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Butler")),
            build = {
                informationStepBuilder.build(
                                roleName = "管家",
                                enName = "Butler",
                                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                                explanation = text("管家每天选择一名主人，白天只能在主人投票时投票。", "The Butler chooses a master each day and may vote only when that master votes."),
                                action = ClocktowerNightAction.ButlerMaster,
                                displayKind = ClocktowerDisplayKind.None,
                                hostInstruction = text("轻拍管家，示意睁眼。让他指一名玩家作为今天的主人；记在心里，白天投票时提醒自己核对。", "Tap the Butler to wake them. Have them point to today's master and keep the choice available for checking during voting."),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Spy")),
            build = {
                informationStepBuilder.build(
                                roleName = "间谍",
                                enName = "Spy",
                                tellPlayer = null,
                                explanation = text("让间谍查看当前说书人方桌；收回手机后点击下一步。", "Let the Spy view the current Storyteller table, then take back the phone and tap Next."),
                                displayKind = ClocktowerDisplayKind.None,
                                hostInstruction = text("唤醒 ${spySeatLabel ?: "间谍"}，让他查看当前方桌；收回手机，示意闭眼，然后点击下一步。", "Wake ${spySeatLabel ?: "the Spy"}, show them the current table, take back the phone, signal them to close their eyes, then tap Next."),
                            )
            },
        )
            ),
        )
        firstNightMaterializers.materialize(firstNightInteractions)
    } else {
    val otherNightMaterializers = ClocktowerNightStepMaterializerRegistry(
        phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
        entries = listOf(
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner")),
            build = {
            informationStepBuilder.build(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Butler")),
            build = {
            informationStepBuilder.build(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                explanation = text("管家每天选择一名主人。", "The Butler chooses a master each day."),
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍管家，示意睁眼。让他指今天的主人；白天投票时用这个记录提醒自己。", "Tap the Butler to wake them and have them point to today's master. Keep the choice available for checking during voting."),
            )
            },
        ),
        empathStepMaterializer,
        chambermaidMaterializer,
        fortuneTellerStepMaterializer,
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker")),
            build = {
                informationStepBuilder.build(
                    roleName = "送葬者", enName = "Undertaker", tellPlayer = null,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = undertakerTarget?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Monk")),
            build = {
            informationStepBuilder.build(
                roleName = "僧侣",
                enName = "Monk",
                tellPlayer = monkProtectedTarget?.let { text("已选择保护：${playerSeatLabel(cards, it)}。如果恶魔今晚选择该玩家，他不会死亡。", "Protected: ${playerSeatLabel(cards, it)}. If the Demon chooses this player tonight, they will not die.") },
                explanation = text("僧侣每晚选择除自己以外的一名玩家。若恶魔今晚攻击被保护的玩家，天亮时宣布无人死亡；不要透露是僧侣保护导致。", "Each night, the Monk protects one other player from the Demon. If that player is attacked, announce no death at dawn without revealing the protection."),
                action = ClocktowerNightAction.MonkProtect,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍僧侣，示意睁眼。让他指一名除自己以外的玩家，在下面记录为今晚保护目标。", "Tap the Monk to wake them. Have them point to another player and record that player as tonight's protected target."),
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.newDemonIdentity(),
            build = {
            val newDemon = requireNotNull(
                publicAliveCards.firstOrNull {
                    it.name == pendingNightNewDemonIdentityName &&
                        it.clocktowerRole?.enName == "Imp"
                },
            ) {
                "Pending next-night new-Demon identity must reference the current living Imp."
            }
                    ClocktowerNightStepUi(
                        title = text("新恶魔身份", "New Demon identity"),
                        actor = newDemon,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text(
                            "轻拍 ${newDemon.seatLabel(cards)}，示意睁眼。把手机交给他，确认他现在是小恶魔；看完后收回手机并示意闭眼。",
                            "Tap ${newDemon.seatLabel(cards)} to wake them. Hand them the phone to confirm they are now the Imp, then take it back and signal them to close their eyes.",
                        ),
                        tellPlayer = text("你现在是小恶魔。", "You are now the Imp."),
                        explanation = text(
                            "猩红女巫在白天因恶魔死亡而继任。必须在新的小恶魔本夜行动前私下确认身份。",
                            "The Scarlet Woman became the Demon during the day. Confirm the new identity privately before the Imp acts tonight.",
                        ),
                        action = ClocktowerNightAction.NewDemonIdentity,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("新身份", "New role"),
                        displayPrimary = text("小恶魔", "Imp"),
                        displayFooter = "",
                        roleEnName = "Imp",
                    )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Imp")),
            build = {
            ClocktowerNightStepUi(
                title = text("恶魔行动", "Demon action"),
                actor = demonCard,
                isRealAction = demonCard != null,
                reason = if (demonCard == null) text("当前没有存活恶魔。", "There is no living Demon.") else "",
                storytellerAction = demonCard?.let {
                    text("轻拍 ${it.seatLabel(cards)}，示意睁眼。让他指今晚要杀死的玩家，在下面记录；记录后示意闭眼。", "Tap ${it.seatLabel(cards)} to wake them. Have them point to tonight's kill target, record it, then signal them to close their eyes.")
                } ?: text("不要唤醒任何玩家，停顿 2-3 秒后继续。", "Do not wake anyone. Pause for 2–3 seconds, then continue."),
                tellPlayer = if (demonCard != null) {
                    if (demonPoisonedForActionExplanation) {
                        text("恶魔已中毒，今晚杀人会失效。", "The Demon is poisoned, so tonight's kill will fail.")
                    } else {
                        pendingNightDeath?.let { text("已记录：今晚恶魔选择杀死 ${playerSeatLabel(cards, it)}。现在不要宣布死亡，等天亮统一宣布。", "Recorded: the Demon chose ${playerSeatLabel(cards, it)}. Do not announce the death until dawn.") }
                    }
                } else {
                    null
                },
                explanation = if (demonPoisonedForActionExplanation) text("可以记录恶魔选择，但天亮不会因此死亡。", "Record the Demon's choice, but it will not cause a death at dawn.") else text("恶魔选择的死亡目标会在天亮时统一公布。", "The Demon's chosen target is announced at dawn."),
                action = ClocktowerNightAction.DemonKill,
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.mayorRedirect(),
            build = {
            val targetedMayor = requireNotNull(mayorTarget)
                    ClocktowerNightStepUi(
                        title = text("市长死亡裁定", "Mayor death ruling"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text("市长被恶魔击杀。选择让市长死亡，或将死亡转移给另一名玩家。", "The Demon attacked the Mayor. Let the Mayor die or redirect the death to another player."),
                        tellPlayer = mayorRedirectTarget?.let { target ->
                            if (target == targetedMayor.name) {
                                text("市长死亡", "Mayor dies")
                            } else {
                                text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
                            }
                        },
                        explanation = text("市长保持存活时，可以让另一名玩家代替死亡。选择死亡或受保护的玩家，可能导致今夜无人死亡。", "To keep the Mayor alive, another player may die instead. Choosing a dead or protected player can result in no death tonight."),
                        action = ClocktowerNightAction.MayorRedirect,
                        displayKind = ClocktowerDisplayKind.None,
                        roleEnName = "Mayor",
                    )
            },
        ),
        clocktowerSageStepMaterializer(
            builder = informationStepBuilder,
            triggerActor = sageNightDeath,
            abilityState = sageDeathTriggerAbilityState,
            content = ClocktowerSageStepContent(
                explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                displayTitle = text("贤者信息", "Sage information"),
                displayPrimary = text("恶魔", "Demon"),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
            ),
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper")),
            build = {
                informationStepBuilder.build(
                    roleName = "守鸦人", enName = "Ravenkeeper", tellPlayer = null,
                    actorOverride = requireNotNull(ravenkeeperTrigger), abilityStateOverride = ravenkeeperDeathTriggerAbilityState,
                    action = ClocktowerNightAction.Ravenkeeper,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = ravenkeeperTargetCard?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Spy")),
            build = {
                informationStepBuilder.build(
                    roleName = "间谍",
                    enName = "Spy",
                    tellPlayer = null,
                    explanation = text("让间谍查看当前说书人方桌；收回手机后点击下一步。", "Let the Spy view the current Storyteller table, then take back the phone and tap Next."),
                    displayKind = ClocktowerDisplayKind.None,
                    hostInstruction = text("唤醒 ${spySeatLabel ?: "间谍"}，让他查看当前方桌；收回手机，示意闭眼，然后点击下一步。", "Wake ${spySeatLabel ?: "the Spy"}, show them the current table, take back the phone, signal them to close their eyes, then tap Next."),
                )
            },
        ),
        ),
    )
        otherNightMaterializers.materialize(
            otherNightInteractions,
            confirmedAttackDeathName = nightHostProjection.resolvedDeathForRemainingWakeups,
        )
    }

    // The first night has no generic preparation page. The only possible interruption
    // before its first actionable role is a REAL uncommitted Demon-bluff decision.
    if (phase == ClocktowerPhase.FirstNight && !nightStarted && manualDemonBluffsRequired) {
        // Automatic mode may accept an already verified joint-opening recommendation.
        // It must not fast-forward any player information display.
        LaunchedEffect(
            globalAiAutomatic, jointOpeningAvailable, manualDemonBluffsReady,
            manualDemonBluffDraft,
        ) {
            if (globalAiAutomatic && jointOpeningAvailable && manualDemonBluffsReady && !nightStarted) {
                onCommitDemonBluffs(manualDemonBluffDraft)
                nightStarted = true
            }
        }
        ClocktowerStorytellerRecommendationScreen(
            title = text("恶魔伪装身份", "DEMON BLUFFS"),
            subtitle = text("确认实际恶魔伪装", "Confirm the actual Demon bluffs"),
            description = text(
                "请确认本局恶魔实际获得的三项伪装身份。这个真实配置决定确认后，直接进入首夜角色。",
                "Confirm the three actual Demon bluffs. This is a real setup decision; the first night starts directly afterward.",
            ),
            buttonLabel = text("确认恶魔伪装", "Confirm Demon bluffs"),
            onHostTools = onHostTools,
            startEnabled = manualDemonBluffsReady,
            onStartNight = {
                if (manualDemonBluffsReady) {
                    onCommitDemonBluffs(manualDemonBluffDraft)
                    nightStarted = true
                }
            },
        ) {
            ClocktowerDemonBluffManualPicker(
                legalRoles = legalDemonBluffs,
                selectedRoleNames = manualDemonBluffDraft,
                language = language,
                onSelectionChange = { nextSelection ->
                    manualDemonBluffDraft = nextSelection
                },
            )
        }
        return
    }

    // Later nights and first nights without an outstanding real setup decision
    // enter their first actionable step directly, regardless of the old UI start flag.
    if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
        require(nightSteps.isNotEmpty()) { "Night flow must contain an actionable step." }
        val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
        val currentStep = nightSteps[currentStepIndex]
        val globalAiPending = if (globalAiAssisted && phase == ClocktowerPhase.FirstNight) {
            pendingGlobalFirstNightPairDecision(
                currentStep, phase, round, cards, sessionGameState, gameId,
                com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision(
                    gameStateRevision, playerInputRevision),
            )
        } else null
        val globalAiScalarPending = if (globalAiAssisted &&
            phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
            globalAiPending == null
        ) pendingGlobalNightScalarDecision(
            currentStep, phase, round, currentStepIndex, cards, gameId,
            StorytellerDecisionRevision(gameStateRevision, playerInputRevision),
            listOfNotNull(fortuneTellerFirst, fortuneTellerSecond),
        ) else null
        val globalAiTargetPending = globalNightPendingDecision?.takeIf {
            globalAiAssisted && phase == ClocktowerPhase.Night &&
                currentStep.action == ClocktowerNightAction.MayorRedirect &&
                currentStep.isRealAction
        }
        val globalAiDecisionIdentity = globalAiPending?.requestIdentity
            ?: globalAiScalarPending?.requestIdentity
            ?: globalAiTargetPending?.requestIdentity
        val globalAiCurrentKey = globalAiDecisionIdentity?.let { identity ->
            ClocktowerResidualRequestIdentityV2.build(
                gameId, round, currentStepIndex, identity.requestId,
                gameStateRevision, playerInputRevision, nightCheckpoint.nextTimelineGlobalSequence,
                if (globalAiAutomatic) StorytellerOperationMode.AI_AUTOMATIC
                else StorytellerOperationMode.AI_ASSISTED,
            )
        }
        // Host revalidates each currently recommended option against the live
        // legal display domain; no package-level confirmation grants authority.
        val preplannedSeat = currentStep.actor?.let { actor ->
            cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
        }
        // An opening information choice is NEVER a valid default for a later
        // night, even when the same role wakes again and the number matches.
        val residualRecommendedOption = if (globalAiCurrentKey != null &&
            globalAiAdviceKey == globalAiCurrentKey
        ) when {
            globalAiPending != null -> ClocktowerResidualDisplayMatcherV2.matchPair(
                globalAiPending,
                globalResidualRecommendedChoices[globalAiPending.requestIdentity.requestId],
                currentStep.manualInformationCandidates, cards, language)
            globalAiScalarPending != null -> ClocktowerResidualDisplayMatcherV2.matchScalar(
                globalAiScalarPending,
                globalResidualRecommendedChoices[globalAiScalarPending.requestIdentity.requestId],
                if (globalAiScalarPending.kind ==
                    com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.ROLE
                ) currentStep.manualInformationCandidates.ifEmpty {
                    currentStep.automaticInformationCandidates
                } else currentStep.manualInformationCandidates)
            else -> null
        } else null
        // Busy/error belong to their exact Host-current request. A stale
        // failed/in-flight earlier step must not block a new deterministic step.
        val currentAiRequestBlocked = clocktowerCurrentAiRequestBlocked(
            globalAiCurrentKey, globalAiAdviceKey, globalAiAdviceBusy, globalAiAdviceError,
        )
        // Match direct AI target advice to the current question, revision and canonical seats.
        val recommendedMayorTargetName = globalAiTargetPending?.takeIf {
            globalAiCurrentKey != null &&
                globalAiAdviceKey == globalAiCurrentKey &&
                !currentAiRequestBlocked
        }?.let { decision ->
            val visibleEligibleNames = mayorRedirectTargetCards.mapTo(mutableSetOf()) { it.name }
            cards.getOrNull(decision.mayorSeat - 1)?.name?.let(visibleEligibleNames::add)
            val eligibleSeats = cards.mapIndexedNotNull { index, card ->
                (index + 1).takeIf { card.name in visibleEligibleNames }
            }.toSet()
            ClocktowerResidualDisplayMatcherV2.matchMayorRedirect(
                decision = decision,
                selected =
                    globalResidualRecommendedChoices[decision.requestIdentity.requestId],
                currentRevision = com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision(
                    gameStateRevision, playerInputRevision,
                ),
                currentSeats = eligibleSeats,
            )?.let { seat -> cards.getOrNull(seat - 1)?.name }
        }
        val approvedOpeningOption = if (globalAiAutomatic &&
            currentAiRequestBlocked) null else
            residualRecommendedOption ?: preplannedSeat?.takeIf {
                phase == ClocktowerPhase.FirstNight
            }?.let { seat ->
            jointOpeningSemantics[seat]?.let { planned ->
                ClocktowerJointOpeningDisplayMatcherV2.match(
                    question = currentStep.informationQuestion,
                    plan = planned,
                    currentSourceSeat = seat,
                    currentOptions = currentStep.manualInformationCandidates,
                )
            }
        }
        LaunchedEffect(globalAiCurrentKey, globalAiAssisted, approvedOpeningOption,
            jointOpeningAvailable, phase) {
            if (globalAiAssisted &&
                globalAiCurrentKey != null &&
                globalAiAdviceKey != globalAiCurrentKey &&
                approvedOpeningOption == null &&
                !(currentStep.action == ClocktowerNightAction.MayorRedirect &&
                    mayorRedirectDraftTarget != null)
            ) {
                onRequestGlobalAiAdvice(nightSteps, currentStepIndex)
            }
        }
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        val selectedNightName = when (currentStep.action) {
            ClocktowerNightAction.RedHerring -> redHerring
            ClocktowerNightAction.Poison -> poisonDraftTarget
            ClocktowerNightAction.ButlerMaster -> butlerMaster
            ClocktowerNightAction.MonkProtect -> monkProtectedDraftTarget
            ClocktowerNightAction.DemonKill -> demonAttackDraftTarget
            ClocktowerNightAction.MayorRedirect -> mayorRedirectDraftTarget
            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
            else -> null
        }
        // Reuse exactly the current square-table's Host target domain. Player
        // actions remain player choices, never AI-selected defaults.
        val legalPlayerTargetNames = when (currentStep.action) {
            ClocktowerNightAction.Poison, ClocktowerNightAction.DemonKill ->
                publicAliveCards.mapTo(mutableSetOf()) { it.name }
            ClocktowerNightAction.ButlerMaster ->
                cards.filter { it.name != currentStep.actor?.name }.mapTo(mutableSetOf()) { it.name }
            ClocktowerNightAction.MonkProtect ->
                cards.mapTo(mutableSetOf()) { it.name }
            else -> emptySet()
        }
        val playerTargetReady = clocktowerLivePlayerTargetReady(
            currentStep.action, currentStep.isRealAction,
            selectedNightName, legalPlayerTargetNames,
        )
        val impPlayer = sessionGameState.players.singleOrNull {
            it.alive && it.actualRole == RoleId("Imp")
        }
        val isSelfKillChoice = currentStep.isRealAction &&
            currentStep.action == ClocktowerNightAction.DemonKill &&
            impPlayer != null && impPlayer.name == selectedNightName
        val monkProtectsImp = impPlayer != null &&
            nightCheckpoint.confirmedMonkTarget == impPlayer.name &&
            sessionGameState.players.any {
                it.alive && !it.poisoned && it.actualRole == RoleId("Monk")
            }
        val eligibleMinions = if (isSelfKillChoice && impPlayer?.poisoned == false &&
            !monkProtectsImp) sessionGameState.players.filter {
            it.alive && it.actualType == com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION
        } else emptyList()
        val forcedScarlet = eligibleMinions.singleOrNull {
            it.actualRole == RoleId("Scarlet Woman") && !it.poisoned
        }?.takeIf { sessionGameState.players.count { player -> player.alive } >= 5 }
        val eligibleSuccessors = if (forcedScarlet != null) listOf(forcedScarlet)
            else eligibleMinions
        var selectedSelfKillSuccessor by remember(
            gameId, phase, round, currentStep.interactionId,
        ) { mutableStateOf<String?>(null) }
        val successorName = when (eligibleSuccessors.size) {
            0 -> null
            1 -> eligibleSuccessors.single().name
            else -> selectedSelfKillSuccessor
        }
        val successorReady = !isSelfKillChoice || eligibleSuccessors.size <= 1 ||
            successorName in eligibleSuccessors.map { it.name }
        // UI-only token: only a FRESH seat tap during this exact step may auto-confirm.
        // Re-entering a step with a retained draft must not commit it silently.
        var freshAutomaticPlayerTarget by remember(
            gameId, phase, round, currentStepIndex, currentStep.interactionId,
        ) { mutableStateOf<String?>(null) }
        val advanceNightStep = advance@{
            if (!playerTargetReady || !successorReady) {
                Toast.makeText(context, text(
                    "请先完成当前行动目标及必要的恶魔继任选择。",
                    "Select the current target and any required Imp successor before continuing.",
                ), Toast.LENGTH_LONG).show()
                return@advance
            }
            if (currentStep.action == ClocktowerNightAction.RedHerring &&
                currentStep.isRealAction
            ) {
                onConfirmRedHerring(requireNotNull(redHerring) {
                    "Red Herring requires a selected rules-legal player."
                })
            }
            // Non-real night placeholders never commit an action or a
            // synthetic revision. Only the active player/ruling has authority.
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.Poison) {
                onConfirmPoisonTarget(currentStep.interactionId.toString())
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.MonkProtect) {
                onConfirmMonkProtectedTarget(currentStep.interactionId.toString())
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.DemonKill) {
                onConfirmDemonAttack(
                    currentStep.interactionId.toString(),
                    if (isSelfKillChoice) successorName else null,
                )
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.MayorRedirect) {
                onConfirmMayorRedirectTarget()
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.ButlerMaster) {
                onConfirmButlerMaster(
                    requireNotNull(currentStep.actor).name,
                    requireNotNull(butlerMaster) { "Butler must choose a master before proceeding." },
                    currentStep.interactionId.toString(),
                )
            }
            val flowMayExpandAfterConfirmation = currentStep.action in setOf(
                ClocktowerNightAction.DemonKill,
                ClocktowerNightAction.MayorRedirect,
            )
            when (
                val directive = clocktowerNightAdvanceDirective(
                    currentStepIndex = currentStepIndex,
                    currentStepCount = nightSteps.size,
                    flowMayExpandAfterConfirmation = flowMayExpandAfterConfirmation,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> {
                    nightStepIndex = directive.stepIndex
                }
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow -> pendingNightAdvance = directive
                ClocktowerNightAdvanceDirective.CompleteNight -> {
                    if (phase == ClocktowerPhase.FirstNight) onAdvanceFromFirstNight()
                    else onCompleteNight()
                }
            }
        }

        LaunchedEffect(
            recommendedMayorTargetName, currentStepIndex, currentStep.interactionId,
            selectedNightName, globalAiAssisted, globalAiAutomatic,
        ) {
            val choice = recommendedMayorTargetName ?: return@LaunchedEffect
            if (globalAiAssisted && currentStep.isRealAction &&
                currentStep.action == ClocktowerNightAction.MayorRedirect &&
                selectedNightName == null
            ) {
                // Assisted: visibly preselect but leave the Next button to the
                // Storyteller. Automatic: same Host draft, then safely commit.
                onSelectMayorRedirectTarget(choice)
                if (globalAiAutomatic && allowRuleDeterminedAutoUi) {
                    freshAutomaticPlayerTarget = choice
                }
            }
        }

        LaunchedEffect(
            freshAutomaticPlayerTarget, selectedNightName, currentStepIndex,
            currentStep.interactionId, playerTargetReady, globalAiAutomatic,
            allowRuleDeterminedAutoUi, playerDisplayStep,
        ) {
            val freshlySelected = freshAutomaticPlayerTarget ?: return@LaunchedEffect
            val verifiedMayorTarget = globalAiAutomatic && allowRuleDeterminedAutoUi &&
                currentStep.isRealAction &&
                currentStep.action == ClocktowerNightAction.MayorRedirect &&
                selectedNightName == freshlySelected &&
                freshlySelected in (
                    mayorRedirectTargetCards.map { it.name } +
                        listOfNotNull(cards.getOrNull(globalAiTargetPending?.mayorSeat?.minus(1) ?: -1)?.name)
                ) && playerDisplayStep == null
            if (verifiedMayorTarget || clocktowerShouldAutoConfirmFreshPlayerTarget(
                    automaticMode = globalAiAutomatic && allowRuleDeterminedAutoUi,
                    action = currentStep.action,
                    isRealAction = currentStep.isRealAction,
                    freshTarget = freshlySelected,
                    selectedName = selectedNightName,
                    legalNames = legalPlayerTargetNames,
                    playerDisplayOpen = playerDisplayStep != null,
                )
            ) {
                freshAutomaticPlayerTarget = null
                advanceNightStep()
            }
        }

        LaunchedEffect(pendingAutoAdvanceAfterDisplay, playerDisplayStep,
            currentStepIndex, currentStep.interactionId) {
            val pending = pendingAutoAdvanceAfterDisplay ?: return@LaunchedEffect
            // Never advance while the player is still viewing private information.
            if (playerDisplayStep != null) return@LaunchedEffect
            if (pending.first != currentStepIndex || pending.second != currentStep.interactionId.toString()) {
                pendingAutoAdvanceAfterDisplay = null
                return@LaunchedEffect
            }
            pendingAutoAdvanceAfterDisplay = null
            advanceNightStep()
        }

        LaunchedEffect(nightSteps.size, pendingNightAdvance) {
            val pending = pendingNightAdvance ?: return@LaunchedEffect
            when (
                val directive = clocktowerRefreshedNightAdvanceDirective(
                    pending = pending,
                    refreshedStepCount = nightSteps.size,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> {
                    pendingNightAdvance = null
                    nightStepIndex = directive.stepIndex
                }
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow ->
                    error("Refreshed night advance cannot remain pending.")
                ClocktowerNightAdvanceDirective.CompleteNight -> {
                    pendingNightAdvance = null
                    if (phase == ClocktowerPhase.FirstNight) onAdvanceFromFirstNight()
                    else onCompleteNight()
                }
            }
        }

        LaunchedEffect(
            phase, currentStepIndex, currentStep.action,
            redHerring, openingRecommendedRedHerring, globalAiAssisted,
        ) {
            if (globalAiAssisted && phase == ClocktowerPhase.FirstNight &&
                currentStep.action == ClocktowerNightAction.RedHerring &&
                currentStep.isRealAction && redHerring == null &&
                openingRecommendedRedHerring != null &&
                publicAliveCards.any { it.name == openingRecommendedRedHerring }
            ) {
                onSelectRedHerring(openingRecommendedRedHerring)
            }
        }
        LaunchedEffect(
            allowRuleDeterminedAutoUi,
            currentStepIndex,
            currentStep.action,
            currentStep.isRealAction,
            redHerring,
            jointOpeningAvailable,
            globalAiAutomatic,
        ) {
            if (
                shouldAutoAdvanceRedHerring(
                    allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                    isRedHerringStep = currentStep.action == ClocktowerNightAction.RedHerring,
                    isRealAction = currentStep.isRealAction,
                    hasSelectedRedHerring = redHerring != null,
                    acceptedJointOpening = jointOpeningAvailable && globalAiAutomatic &&
                        openingRecommendedRedHerring != null &&
                        redHerring == openingRecommendedRedHerring,
                )
            ) {
                advanceNightStep()
            }
        }

        ClocktowerNightActiveScreen(
            title = if (phase == ClocktowerPhase.FirstNight) {
                text("第 1 夜", "Night 1")
            } else {
                text("第 $round 夜", "Night $round")
            },
            subtitle = text("当前阶段：${currentStep.title}", "Current: ${currentStep.title}"),
            progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
            canGoPrevious = false,
            nextEnabled = playerTargetReady && successorReady && (currentStep.action !in setOf(
                ClocktowerNightAction.MayorRedirect,
            ) || selectedNightName != null) &&
                (currentStep.action != ClocktowerNightAction.RedHerring ||
                    !currentStep.isRealAction || redHerring != null),
            onPrevious = {},
            onHostTools = onHostTools,
            onNext = advanceNightStep,
            contentOwnsFullScreen = currentSurfacePlan.ownsFullScreenHostSurface,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (globalAiAssisted && globalAiDecisionIdentity != null &&
                    globalAiCurrentKey != null
                ) {
                    if (!globalAiAutomatic) {
                        OutlinedButton(
                            onClick = { onRequestGlobalAiAdvice(nightSteps, currentStepIndex) },
                            enabled = !globalAiAdviceBusy || globalAiAdviceKey != globalAiCurrentKey,
                        ) {
                            Text(text("AI 全局局势与当前合法裁量", "Global AI advice"))
                        }
                    }
                    if (globalAiAdviceKey == globalAiCurrentKey) {
                        if (globalAiAdviceBusy) Text(
                            text("正在重新评估已确认的游戏事件…", "Analysing committed game history…"),
                        )
                        globalAiAdviceError?.let { error -> Text(text = error) }
                        globalAiAdviceText?.let { advice ->
                            Text(text = if (globalAiAutomatic && globalAiAdviceError == null)
                                text("AI 已更新整局规划，合法结果将在相应夜晚步骤使用。",
                                    "Whole-game strategy updated; legal results are used in their night steps.")
                            else advice)
                        }
                    }
                }
                ClocktowerNightStepCardLocalized(
                scriptRoles = clocktowerRolesForScript(script),
                allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                phase = phase,
                gameId = gameId,
                round = round,
                sequence = currentStepIndex,
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                informationDecisionKey = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}",
                cards = cards,
                ghostVoteAuthority = ghostVoteAuthority,
                poisonedPlayerName = currentStep.roleEnName?.let(effectivePoisonForRole),
                aliveCards = publicAliveCards,
                chambermaidTargetCards = chambermaidTargetCards,
                mayorRedirectTargetCards = mayorRedirectTargetCards,
                // One optional current-step suggestion for both AI modes.
                // The card retains the complete Host legal domain for manual edits.
                // Automatic mode can use the same verified choice at this step only.
                aiSuggestedInformationOption = approvedOpeningOption.takeUnless {
                    currentAiRequestBlocked
                },
                // Only a player-independent current information step may automatically
                // enter the SAME confirmation/publication/reveal handoff as a manual tap.
                // Returning from player display must not re-publish/re-open it.
                autoPresentAiInformation = clocktowerShouldAutoPresentCurrentInformation(
                    automaticMode = globalAiAutomatic && allowRuleDeterminedAutoUi &&
                        !currentAiRequestBlocked,
                    step = currentStep,
                    phase = phase,
                    round = round,
                    actorSeat = preplannedSeat,
                    hasCurrentLegalResult = approvedOpeningOption != null ||
                        false,
                    playerQueryResolved = when (currentStep.action) {
                        ClocktowerNightAction.FortuneTeller -> fortuneTellerFirst != null &&
                            fortuneTellerSecond != null && fortuneTellerFirst != fortuneTellerSecond
                        ClocktowerNightAction.Chambermaid -> chambermaidSelection.isComplete
                        ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget != null &&
                            cards.any { it.name == ravenkeeperTarget }
                        else -> false
                    },
                    confirmedObservations = actualInformationHistory,
                ),
                step = currentStep,
                surfacePlan = currentSurfacePlan,
                selectedName = selectedNightName,
                fortuneTellerFirst = fortuneTellerFirst,
                fortuneTellerSecond = fortuneTellerSecond,
                redHerring = redHerring,
                chambermaidFirst = chambermaidSelection.first,
                chambermaidSecond = chambermaidSelection.second,
                onSelectName = { name ->
                    val committedKind = when (currentStep.action) {
                        ClocktowerNightAction.Poison -> NightOperationKind.POISON
                        ClocktowerNightAction.MonkProtect -> NightOperationKind.PROTECT
                        ClocktowerNightAction.ButlerMaster -> NightOperationKind.BUTLER
                        ClocktowerNightAction.DemonKill -> NightOperationKind.ATTACK
                        else -> null
                    }
                    if (committedKind != null &&
                        committedKind in nightCheckpoint.acceptedOperationKinds
                    ) {
                        freshAutomaticPlayerTarget = null
                        Toast.makeText(context, text(
                            "本步骤行动已经正式记录，不能通过返回修改。",
                            "This action has already started and cannot be amended.",
                        ), Toast.LENGTH_LONG).show()
                    } else {
                        val nextSelection = clocktowerToggledSingleTargetSelection(
                            currentSelection = selectedNightName,
                            tappedSelection = name,
                        )
                        when (currentStep.action) {
                            ClocktowerNightAction.RedHerring -> onSelectRedHerring(nextSelection)
                            ClocktowerNightAction.Poison -> onSelectPoisonTarget(nextSelection)
                            ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(nextSelection)
                            ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(nextSelection)
                            ClocktowerNightAction.DemonKill -> onSelectNightDeath(nextSelection)
                            ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(nextSelection)
                            ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(nextSelection)
                            else -> Unit
                        }
                        freshAutomaticPlayerTarget = nextSelection?.takeIf { target ->
                            globalAiAutomatic && allowRuleDeterminedAutoUi &&
                                currentStep.isRealAction &&
                                currentStep.action in setOf(
                                    ClocktowerNightAction.Poison,
                                    ClocktowerNightAction.ButlerMaster,
                                    ClocktowerNightAction.MonkProtect,
                                    ClocktowerNightAction.DemonKill,
                                ) && target in legalPlayerTargetNames
                        }
                    }
                },
                onSelectFortuneTellerFirst = {
                    onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it)
                },
                onSelectFortuneTellerSecond = {
                    onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it)
                },
                onSelectChambermaidFirst = {
                    onSelectChambermaidFirst(if (chambermaidFirst == it) null else it)
                },
                onSelectChambermaidSecond = {
                    onSelectChambermaidSecond(if (chambermaidSecond == it) null else it)
                },
                onShowPlayerDisplay = { selectedStep ->
                    val displayStep = selectedStep.copy(informationSourceRevision =
                        StorytellerDecisionRevision(gameStateRevision, playerInputRevision))
                    // UI handoff token is fresh only for newly confirmed
                    // information; a prior displayed observation is not a new turn.
                    pendingAutoAdvanceAfterDisplay = null
                    val displayActorSeat = displayStep.actor?.let { actor ->
                        cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
                    }
                    val alreadyPublishedForThisActor = displayActorSeat != null &&
                        actualInformationHistory.any { observation ->
                            observation.phase == phase.toStorytellerPhase() &&
                                observation.round == round &&
                                observation.sourceSeat == displayActorSeat &&
                                observation.sourceAbility?.value == displayStep.roleEnName &&
                                observation.visibility == ObservationVisibility.PRIVATE
                        }
                    // Use the SAME typed precedence as the actual Session writer;
                    // unreliable information must never compare against a stale true answer.
                    val shownQueryResult = if (clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) {
                            displayActorSeat?.let { actorSeat ->
                                clocktowerShownUnreliableProposition(
                                    step = displayStep,
                                    actorSeat = actorSeat,
                                    allSeats = cards.indices.map { it + 1 },
                                    empathNeighborSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
                                    fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
                                )
                            }
                        } else displayStep.displayProposition
                    val queryRevealDisposition = clocktowerLiveQueryRevealDisposition(
                        action = currentStep.action,
                        phase = phase,
                        round = round,
                        actorSeat = displayActorSeat,
                        roleEnName = displayStep.roleEnName,
                        shown = shownQueryResult,
                        confirmedObservations = actualInformationHistory,
                    )
                    try {
                    com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
                        .revealStage("CLICKED", phase.toStorytellerPhase().name, round)
                        val handoff = performClocktowerPlayerRevealHandoff(
                            authorize = {
                                queryRevealDisposition != ClocktowerLiveQueryRevealDisposition.CONFLICT &&
                                    informationDecisionPublicationAllowed(displayStep) &&
                                    unreliableInformationCanBeRecorded(displayStep)
                            },
                            publicationNeeded = {
                                queryRevealDisposition != ClocktowerLiveQueryRevealDisposition.REOPEN &&
                                    firstNightPublicationNeeded(displayStep)
                            },
                            recordPrivateInformation = {
                                recordReliablePrivateInformation(displayStep)
                                com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
                                    .revealStage("OBSERVATION_RECORDED", phase.toStorytellerPhase().name, round)
                            },
                            markFirstNightPublished = {
                                check(publishFirstNightInformation(displayStep)) {
                                    "First-night decision became displayed before its confirmed observation."
                                }
                                com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
                                    .revealStage("FIRST_NIGHT_RECORDED", phase.toStorytellerPhase().name, round)
                            },
                            openReveal = {
                                playerDisplayStep = displayStep
                                com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
                                    .revealStage("REVEAL_OPENED", phase.toStorytellerPhase().name, round)
                            },
                        )
                        if (clocktowerShouldAutoAdvanceAfterFreshReveal(
                                automaticMode = globalAiAutomatic,
                                phase = phase,
                                currentStep = currentStep,
                                revealedStep = displayStep,
                                actorSeat = displayActorSeat,
                                alreadyPublished = alreadyPublishedForThisActor,
                                handoff = handoff,
                            )) {
                            pendingAutoAdvanceAfterDisplay =
                                currentStepIndex to currentStep.interactionId.toString()
                        }
                        if (!handoff.openReveal &&
                            queryRevealDisposition == ClocktowerLiveQueryRevealDisposition.CONFLICT
                        ) {
                            Toast.makeText(
                                context,
                                text(
                                    "本夜该角色已收到信息，不能修改查询目标或公布另一条结果；只能重新查看原信息。",
                                    "This role has already received information tonight. Only the original result may be reopened.",
                                ),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        if (!handoff.openReveal && !unreliableInformationCanBeRecorded(displayStep)) {
                            Toast.makeText(
                                context,
                                text(
                                    "当前信息缺少准确的结构化展示值，不能公布。请重新选择具体显示结果。",
                                    "The chosen display has no exact structured result. Select a concrete result before revealing.",
                                ),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    } catch (failure: Exception) {
                        // A Host write may already have succeeded. Never retry or
                        // continue the reveal silently after a partial failure.
                        com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
                            .revealFailure(failure)
                        Toast.makeText(context, text(
                            "展示失败；部分游戏记录可能已经保存。请先查看调试日志，不要重复点击。",
                            "Reveal failed; some Host records may already be saved. Check debug logs before retrying."
                        ), Toast.LENGTH_LONG).show()
                    }
                },
                canGoPrevious = false,
                onPrevious = {},
                onHostTools = onHostTools,
                onNext = advanceNightStep,
                )
                if (isSelfKillChoice && eligibleSuccessors.size > 1 &&
                    !successorReady) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(text("小恶魔自杀 · 选择继任者", "Imp self-kill · successor")) },
                        text = {
                            Column {
                                eligibleSuccessors.forEach { candidate ->
                                    TextButton(onClick = {
                                        selectedSelfKillSuccessor = candidate.name
                                    }) {
                                        Text(playerSeatLabel(cards, candidate.name))
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                    )
                }
            }
        }
        return
    }
}
