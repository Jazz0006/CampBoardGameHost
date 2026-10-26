package com.codex.campboardgamehost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.widget.Toast
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility

/** Owns the current night interaction draft, private preview, and the only night Next intent. */
@Composable
internal fun ClocktowerNightHostScreen(
    allowRuleDeterminedAutoUi: Boolean,
    cards: List<PlayerCard>,
    // Confirmed mechanical facts come from the one Session, never rebuilt from UI cards.
    sessionState: ClocktowerSessionState,
    globalAiAssisted: Boolean = false,
    /** A verified joint plan is advisory; only confirmed current steps affect Session. */
    globalAiAutomatic: Boolean = false,
    jointOpeningAvailable: Boolean = false,
    jointOpeningReason: String? = null,
    recommendedRedHerringSeat: Int? = null,
    jointOpeningSemantics: Map<Int, com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2> = emptyMap(),
    globalNightPendingDecision: PendingMayorRedirectDecision? = null,
    globalAiAdviceKey: String? = null,
    globalAiAdviceText: String? = null,
    globalAiAdviceBusy: Boolean = false,
    globalAiAdviceError: String? = null,
    globalResidualRecommendedChoices: Map<String, com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult> = emptyMap(),
    onRequestGlobalAiAdvice: (List<ClocktowerNightStepUi>, Int, List<String>) -> Unit = { _, _, _ -> },
    recommendedDemonBluffRoleNames: List<String>,
    onHostTools: () -> Unit,
    /** Advances only the AI-staleness input revision; never a game fact or event. */
    onRecordPlayerInput: () -> Unit,
    onConfirmNightStep: (ClocktowerNightStepConfirmation) -> Boolean,
    rulesetCatalog: BuiltInClocktowerRulesetCatalog = BuiltInClocktowerRulesetCatalog.fromContext(LocalContext.current),
    onAdvanceFromFirstNight: () -> Unit,
    onCompleteNight: () -> Unit,
) {
    if (sessionState.outcome != null) return
    val sessionGameState = sessionState.gameState
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameId = sessionState.gameId
    val round = sessionState.round
    val phase = sessionState.phase.toClocktowerPhase()
    val script = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionGameState.script }
    val gameStateRevision = sessionState.gameStateRevision
    val playerInputRevision = sessionState.playerInputRevision
    val ghostVoteAuthority = ClocktowerGhostVoteAuthority.from(sessionState.actionTimeline)
    fun text(zh: String, en: String) = if (language == "en") en else zh
    val nightRuleset = rulesetCatalog.ruleset(script)
    val flowPhase = when {
        phase == ClocktowerPhase.FirstNight -> ClocktowerNightFlowPhase.FIRST_NIGHT
        phase == ClocktowerPhase.Night -> ClocktowerNightFlowPhase.OTHER_NIGHT
        else -> null
    }
    val nextNightInteraction = flowPhase?.let { sessionState.nextNightInteraction(nightRuleset) }
    // Each night fact is read once from this Session snapshot and shared below.
    val facts = remember(sessionState, nightRuleset) { ClocktowerNightFacts(sessionState, nightRuleset) }
    val redHerringSelectableSeats = facts.redHerringSelectableSeats
    val redHerringInteractionId = ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()
        .interactionId(ClocktowerNightFlowPhase.FIRST_NIGHT)
    // UI-only, keyed to this exact interaction (IDs carry no round); outlives the fullscreen preview.
    var nightDraft by remember(gameId, phase, round, nextNightInteraction?.id) {
        mutableStateOf(nextNightInteraction?.let {
            val initialSeat = recommendedRedHerringSeat?.takeIf { seat ->
                it.id == redHerringInteractionId && globalAiAssisted && jointOpeningAvailable &&
                    seat in redHerringSelectableSeats
            }
            ClocktowerNightStepDraft(gameId, sessionState.phase, round, it.id, listOfNotNull(initialSeat))
        })
    }
    fun nameAt(seat: Int?): String? = seat?.let { cards.getOrNull(it - 1)?.name }
    val subjects = ClocktowerNightDraftSubjects(nightDraft, sessionState.phase, facts.aliveSeats)
    val fortuneTellerSubjectSeats = subjects.fortuneTellerSeats
    val fortuneTellerSelectedNames = fortuneTellerSubjectSeats.mapNotNull(::nameAt)
    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        facts.abilityState(enName, cards.seatOf(actor?.name)) in setOf(AbilityFunctioningState.DRUNK, AbilityFunctioningState.POISONED)

    var playerDisplayStep by remember(gameId, phase, round, nextNightInteraction?.id) { mutableStateOf<ClocktowerNightStepUi?>(null) }
    // UI-only D2 draft must outlive the fullscreen early return below.
    // Match the interaction identity before reuse: no persisted night cursor or Session event.
    var lastInformationPreview by remember(gameId, phase, round, nextNightInteraction?.id) {
        mutableStateOf<ClocktowerNightStepUi?>(null)
    }
    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            cards = cards,
            // D2: closing a preview returns to this role without any Session fact.
            onDismiss = { playerDisplayStep = null },
        )
        return
    }

    fun informationDecisionPublicationAllowed(displayStep: ClocktowerNightStepUi): Boolean =
        clocktowerInformationPublicationAllowed(
            sourceRevision = displayStep.informationSourceRevision,
            currentRevision = StorytellerDecisionRevision(gameStateRevision, playerInputRevision),
        )
    fun displayIsUnreliable(displayStep: ClocktowerNightStepUi) =
        clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
    val recordableUnreliableKinds = setOf(
        ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo, ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
    )
    // The exact proposition Next records for this preview. Misinformation is never silently
    // omitted; the true display proposition may only serve a reliable ability.
    fun shownPropositionFor(displayStep: ClocktowerNightStepUi): InformationProposition? {
        if (!displayIsUnreliable(displayStep)) return displayStep.displayProposition
        val actorSeat = cards.seatOf(displayStep.actor?.name) ?: return null
        if (displayStep.displayKind !in recordableUnreliableKinds) return null
        return clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = facts.empathNeighbourSeats,
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        )
    }
    // Previews of unsupported unreliable kinds stay viewable; their Next is refused.
    fun unreliableInformationCanBeRecorded(displayStep: ClocktowerNightStepUi): Boolean =
        !displayIsUnreliable(displayStep) || displayStep.displayKind !in recordableUnreliableKinds ||
            shownPropositionFor(displayStep) != null
    fun previewObservation(displayStep: ClocktowerNightStepUi, stepId: String): EpistemicObservationDraft? {
        val actorSeat = cards.seatOf(displayStep.actor?.name) ?: return null
        if (!informationDecisionPublicationAllowed(displayStep)) return null
        val proposition = shownPropositionFor(displayStep) ?: return null
        return EpistemicObservationDraft(
            recordId = clocktowerPrivateObservationRecordId(
                gameId = gameId,
                phase = phase,
                round = round,
                roleEnName = requireNotNull(displayStep.roleEnName),
                actorSeat = actorSeat,
                stepId = stepId,
            ),
            phase = phase.toStorytellerPhase(),
            round = round, sequence = (sessionState.nextTimelineGlobalSequence + 1).toInt(), sourceSeat = actorSeat,
            sourceAbility = RoleId(requireNotNull(displayStep.roleEnName)), visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(actorSeat),
            reliability = if (displayIsUnreliable(displayStep)) {
                ObservationReliability.KNOWN_MALFUNCTIONING
            } else {
                ObservationReliability.RECEIVED_AS_FUNCTIONING
            },
            proposition = proposition,
        )
    }
    val nightSteps = clocktowerNightPresentation(facts, subjects, script, cards, recommendedDemonBluffRoleNames, language)
    // Later nights and first nights without an outstanding real setup decision
    // enter their first actionable step directly, regardless of the old UI start flag.
    if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
        val currentStepIndex = nightSteps.indexOfFirst { it.interactionId == nextNightInteraction?.id }
        LaunchedEffect(gameId, phase, round, nextNightInteraction?.id, gameStateRevision, playerInputRevision) {
            if (nextNightInteraction == null && sessionState.outcome == null) {
                if (phase == ClocktowerPhase.FirstNight) onAdvanceFromFirstNight() else onCompleteNight()
            }
        }
        if (nextNightInteraction == null) return
        check(currentStepIndex >= 0) { "Missing presentation for current Session night interaction." }
        val currentStep = nightSteps[currentStepIndex]
        // A fullscreen preview temporarily replaces this role's composable subtree.
        // Only a draft for this exact Host interaction can enable final Next.
        val currentInformationPreview = lastInformationPreview?.takeIf {
            it.interactionId == currentStep.interactionId && informationDecisionPublicationAllowed(it)
        }
        val host = hostNightStep(sessionState, nightRuleset, nightDraft, facts)
        val redHerringReady = currentStep.action != ClocktowerNightAction.RedHerring ||
            host.selectedSeat in redHerringSelectableSeats
        // Verify the UI materializer matches the actual pending Session interaction.
        // Display projections may not assign role, action, actor or numeric query authority.
        check(currentStep.interactionId == host.facts.interactionId &&
            currentStep.action == host.facts.action &&
            currentStep.isRealAction == host.facts.isRealAction &&
            currentStep.informationQuestion == host.facts.question
        ) { "Night display differs from the authoritative Session interaction." }
        val ai = nightAiOverlay(host, NightAiInputs(
            globalAiAssisted, globalAiAutomatic, globalAiAdviceKey, globalAiAdviceBusy, globalAiAdviceError,
            globalResidualRecommendedChoices, jointOpeningSemantics, globalNightPendingDecision, fortuneTellerSelectedNames,
        ), sessionState, nightRuleset, currentStep, currentStepIndex, phase, cards, language, clocktowerRolesForScript(script))
        LaunchedEffect(ai.currentKey, globalAiAssisted, ai.validatedOption, jointOpeningAvailable, phase) {
            if (ai.shouldRequest) onRequestGlobalAiAdvice(nightSteps, currentStepIndex, fortuneTellerSelectedNames)
        }
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        var recommendationSelectionVersion by remember(gameId, currentStep.interactionId) { mutableStateOf(0) }
        var waitingForRequestedRecommendation by remember(gameId, currentStep.interactionId) { mutableStateOf(false) }
        fun updateNightDraft(next: ClocktowerNightStepDraft) {
            waitingForRequestedRecommendation = false
            if (currentStep.action in setOf(
                    ClocktowerNightAction.Ravenkeeper, ClocktowerNightAction.FortuneTeller, ClocktowerNightAction.Chambermaid,
                )) lastInformationPreview = null
            // Red Herring stays a pure draft until its Next; it does not advance the AI input revision.
            if (currentStep.action != ClocktowerNightAction.RedHerring) onRecordPlayerInput()
            nightDraft = next
        }
        var selectedSelfKillSuccessor by remember(
            gameId, phase, round, currentStep.interactionId, gameStateRevision,
            host.selectedSeat, host.successorSeats,
        ) { mutableStateOf<Int?>(null) }
        val successorSeat = host.successorSeat(selectedSelfKillSuccessor)
        val successorReady = host.successorReady(selectedSelfKillSuccessor)
        val advanceNightStep = advance@{
            // Check readiness before the only irreversible Observation write.
            if (!host.targetReady || !successorReady || !redHerringReady) {
                Toast.makeText(context, text(
                    "请先完成当前行动目标及必要的恶魔继任选择。",
                    "Select the current target and any required Imp successor before continuing.",
                ), Toast.LENGTH_LONG).show()
                return@advance
            }
            var observation: EpistemicObservationDraft? = null
            if (host.isPrivateInformation) {
                val preview = currentInformationPreview
                if (preview == null ||
                    preview.actor?.name != currentStep.actor?.name ||
                    preview.roleEnName != currentStep.roleEnName
                ) {
                    Toast.makeText(context, text(
                        "请先点击展示按钮，再点击下一步确认最后展示的信息。",
                        "Preview the selected information before confirming Next.",
                    ), Toast.LENGTH_LONG).show()
                    return@advance
                }
                // Only the final preview is recorded. The previous previews
                // have no Session footprint and may be discarded on interruption.
                observation = previewObservation(preview, currentStep.interactionId.toString())
                if (observation == null) {
                    Toast.makeText(context, text(
                        "当前展示信息无效或已经过期，请重新选择并展示。",
                        "The preview is invalid or stale; select and preview again.",
                    ), Toast.LENGTH_LONG).show()
                    return@advance
                }
            }
            // Every step's Next goes through the one Session dispatcher; steps without an action are no-ops.
            if (onConfirmNightStep(ClocktowerNightStepConfirmation(
                step = currentStep,
                draft = requireNotNull(nightDraft) { "A current night step always has its draft." },
                successorSeat = successorSeat,
                previewRevision = currentInformationPreview?.informationSourceRevision,
                observation = observation,
            ))) lastInformationPreview = null
        }
        LaunchedEffect(ai.recommendedMayorSeat, currentStep.interactionId, host.selectedSeat, globalAiAssisted) {
            if (globalAiAssisted && currentStep.action == ClocktowerNightAction.MayorRedirect && host.selectedSeat == null) {
                ai.recommendedMayorSeat?.let { seat ->
                    nightDraft?.let { draft -> updateNightDraft(draft.copy(seatSlots = listOf(seat))) }
                }
            }
        }

        val currentAiRequest = globalAiAdviceKey == ai.currentKey && ai.currentKey != null
        val hasCurrentRecommendation = currentAiRequest && !ai.requestBlocked &&
            globalResidualRecommendedChoices.isNotEmpty() &&
            (ai.suggestedOption != null || ai.recommendedMayorSeat != null)
        LaunchedEffect(waitingForRequestedRecommendation, currentAiRequest, ai.suggestedOption,
            ai.recommendedMayorSeat, globalAiAdviceBusy, globalAiAdviceError) {
            if (waitingForRequestedRecommendation && currentAiRequest && !globalAiAdviceBusy) {
                if (!ai.requestBlocked && (ai.suggestedOption != null || ai.recommendedMayorSeat != null)) {
                    recommendationSelectionVersion += 1
                }
                waitingForRequestedRecommendation = false
            }
        }
        val aiActions = if (globalAiAssisted &&
            (ai.currentKey != null || (jointOpeningAvailable &&
                (host.isEvilInformation || currentStep.action == ClocktowerNightAction.RedHerring)))) {
            ClocktowerAiRecommendationActions(
                identity = listOf(gameId, currentStep.interactionId, ai.currentKey, gameStateRevision, playerInputRevision),
                reason = if (hasCurrentRecommendation) globalAiAdviceText else jointOpeningReason.takeIf {
                    phase == ClocktowerPhase.FirstNight && jointOpeningAvailable
                },
                openingReason = !hasCurrentRecommendation && phase == ClocktowerPhase.FirstNight && jointOpeningAvailable,
                busy = currentAiRequest && globalAiAdviceBusy,
                error = globalAiAdviceError.takeIf { currentAiRequest },
                onRetry = ai.currentKey?.let { {
                    lastInformationPreview = null
                    waitingForRequestedRecommendation = true
                    onRequestGlobalAiAdvice(nightSteps, currentStepIndex, fortuneTellerSelectedNames)
                } },
            )
        } else null
        ClocktowerAiRecommendationPage(aiActions) {
            ClocktowerNightActiveScreen(
                progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
                nextEnabled = (!(host.isPrivateInformation || host.isEvilInformation) || currentInformationPreview != null) &&
                    host.targetReady && successorReady && redHerringReady,
                onHostTools = onHostTools,
                onNext = advanceNightStep,
                contentOwnsFullScreen = currentSurfacePlan.ownsFullScreenHostSurface,
            ) {
                if (host.successorSeats.size > 1) {
                    ClocktowerSingleTargetSquareTableDialog(
                        seats = sessionGameState.toHostSeatPresentations { roleId ->
                            clocktowerRoleByName(roleId.value)?.nameFor(language) ?: roleId.value
                        }.map { seat ->
                            seat.copy(hasUnspentGhostVote = hostSeatHasUnspentGhostVote(
                                seat.seatId, seat.isAlive, ghostVoteAuthority,
                            ))
                        },
                        selectedSeat = successorSeat,
                        selectableSeats = host.successorSeats.toSet(),
                        enabled = true,
                        actorSeat = host.facts.actorSeat,
                        title = text("小恶魔自杀 · 选择继任者", "Imp self-kill · successor"),
                        helper = text("在方桌上选择接任小恶魔的存活爪牙。", "Select a living Minion on the table to become the Imp."),
                        language = language,
                        onSeatSelected = { seat ->
                            if (seat in host.successorSeats) {
                                onRecordPlayerInput()
                                selectedSelfKillSuccessor = seat.takeUnless { it == selectedSelfKillSuccessor }
                            }
                        },
                        onHostTools = onHostTools,
                        onNext = advanceNightStep,
                        nextEnabled = host.targetReady && successorReady,
                        secondaryActionLabel = text("修改攻击目标", "Change attack target"),
                        secondaryActionEnabled = true,
                        onSecondaryAction = {
                            nightDraft?.let { updateNightDraft(it.copy(seatSlots = emptyList())) }
                        },
                    )
                    return@ClocktowerNightActiveScreen
                }
                key(recommendationSelectionVersion) {
                    ClocktowerNightStepCardLocalized(
                    scriptRoles = clocktowerRolesForScript(script),
                    allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                    phase = phase,
                    gameId = gameId,
                    round = round,
                    sequence = currentStepIndex,
                    gameStateRevision = gameStateRevision,
                    playerInputRevision = playerInputRevision,
                    informationDecisionKey = "$gameId:$gameStateRevision:$playerInputRevision:${currentStep.interactionId}",
                    cards = cards,
                    actualAlignmentBySeat = sessionGameState.players.associate { it.seat to it.actualAlignment },
                    ghostVoteAuthority = ghostVoteAuthority,
                    poisonedPlayerName = nameAt(currentStep.roleEnName?.let(facts::poisonedSeat)),
                    aliveSeats = facts.aliveSeats,
                    chambermaidTargetSeats = facts.aliveSeats,
                    mayorRedirectTargetSeats = facts.mayorRedirectSeats,
                    livingMayorSeat = facts.livingMayorSeat,
                    // One optional current-step suggestion for both AI modes.
                    // The card retains the complete Host legal domain for manual edits.
                    // Automatic mode can use the same verified choice at this step only.
                    aiSuggestedInformationOption = ai.suggestedOption,
                    numericResolution = host.numericResolution,
                    booleanResolution = host.booleanResolution,
                    roleResolution = host.roleResolution,
                    pairResolver = { role, seat, first, second ->
                        PairInformationResolver.resolve(
                            sessionState, nightRuleset, RoleId(role), seat, first, second,
                        )
                    },
                    // AI may select the result but NEVER open the player fullscreen
                    // automatically. Showing private information requires a live tap.
                    step = currentStep,
                    surfacePlan = currentSurfacePlan,
                    selectedSeatSlots = when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> listOfNotNull(host.selectedSeat)
                        ClocktowerNightAction.Chambermaid ->
                            listOf(subjects.chambermaid.first, subjects.chambermaid.second)
                        else -> nightDraft?.seatSlots.orEmpty()
                    },
                    redHerringSeat = sessionState.redHerringSeat,
                    redHerringSelectableSeats = redHerringSelectableSeats,
                    onSelectSeat = { seat ->
                        if (currentStep.action in clocktowerNightSeatDraftActions &&
                            (currentStep.action != ClocktowerNightAction.RedHerring || seat in redHerringSelectableSeats)) {
                            nightDraft?.withTappedSeat(currentStep.action, seat)?.let(::updateNightDraft)
                        }
                    },
                    onShowPlayerDisplay = { selectedStep ->
                        val preview = selectedStep.copy(informationSourceRevision =
                            StorytellerDecisionRevision(gameStateRevision, playerInputRevision))
                        if (!informationDecisionPublicationAllowed(preview) ||
                            !unreliableInformationCanBeRecorded(preview)
                        ) {
                            Toast.makeText(context, text(
                                "当前信息缺少准确的结构化展示值，不能预览。请重新选择。",
                                "The selected information cannot be previewed safely. Choose a concrete result.",
                            ), Toast.LENGTH_LONG).show()
                        } else {
                            // D2: these are UI-only previews. Returning to this role
                            // permits replacing the selection. The final typed
                            // observation is written only by this role's Next.
                            waitingForRequestedRecommendation = false
                            lastInformationPreview = preview
                            playerDisplayStep = preview
                        }
                    },
                    onInformationDraftChanged = {
                        waitingForRequestedRecommendation = false
                        lastInformationPreview = null
                    },
                    lastPreviewProposition = currentInformationPreview?.displayProposition,
                            onHostTools = onHostTools,
                    onNext = advanceNightStep,
                    )
                }
            }
        }
        return
    }
}
