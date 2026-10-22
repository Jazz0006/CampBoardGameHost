package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * A prior AI result cannot be rebound to different Chambermaid query subjects.
 * Query seats come from live player input, not AI; AI owns only the result value.
 */
internal fun clocktowerCurrentChambermaidRecommendation(
    option: ClocktowerDisplayOption?,
    actorSeat: Int?,
    selectedSeats: List<Int>,
): ClocktowerDisplayOption? = option?.takeIf { candidate ->
    val answer = candidate.proposition as? InformationProposition.NumericResult
    candidate.displayKind == ClocktowerDisplayKind.Number && answer != null &&
        answer.metric == com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.PLAYERS_WAKING_FOR_ABILITY &&
        answer.sourceSeat == actorSeat &&
        selectedSeats.size == 2 && selectedSeats.distinct().size == 2 &&
        answer.subjectSeats.size == 2 && answer.subjectSeats.toSet() == selectedSeats.toSet()
}

@Composable
internal fun ClocktowerNightStepCardLocalized(
    scriptRoles: List<ClocktowerRole>,
    allowRuleDeterminedAutoUi: Boolean,
    phase: ClocktowerPhase,
    gameId: String,
    round: Int,
    sequence: Int,
    gameStateRevision: Long,
    playerInputRevision: Long,
    informationDecisionKey: String,
    cards: List<PlayerCard>,
    /** Current Session alignment is the only source for night card colors. */
    actualAlignmentBySeat: Map<Int, com.codex.campboardgamehost.clocktower.domain.Alignment>,
    ghostVoteAuthority: ClocktowerGhostVoteAuthority,
    poisonedPlayerName: String?,
    aliveCards: List<PlayerCard>,
    chambermaidTargetCards: List<PlayerCard>,
    mayorRedirectTargetCards: List<PlayerCard>,
    step: ClocktowerNightStepUi,
    /** Suggested current-step option only. Never narrows the Host's legal domain. */
    aiSuggestedInformationOption: ClocktowerDisplayOption? = null,
    numericResolution: com.codex.campboardgamehost.clocktower.session.HostNumericResolution? = null,
    surfacePlan: ClocktowerNightSurfacePlan,
    selectedName: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    redHerring: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    onSelectName: (String) -> Unit,
    onSelectFortuneTellerFirst: (String) -> Unit,
    onSelectFortuneTellerSecond: (String) -> Unit,
    onSelectChambermaidFirst: (String) -> Unit,
    onSelectChambermaidSecond: (String) -> Unit,
    onShowPlayerDisplay: (ClocktowerNightStepUi) -> Unit,
    onInformationDraftChanged: () -> Unit = {},
    lastPreviewProposition: InformationProposition? = null,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    val hostContext = LocalContext.current
    val language = hostContext.resources.configuration.locales[0].language
    val plannedFullScreenSurface =
        (surfacePlan as? ClocktowerNightSurfacePlan.FullScreen)?.surface
    val presentationRoleEnName = clocktowerNightPresentationRoleEnName(
        stepRoleEnName = step.roleEnName,
        actor = step.actor,
    )
    val directNumericActorSeat = step.actor?.let { actor ->
        cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
    }
    if (step.isRealAction &&
        step.action == ClocktowerNightAction.None &&
        directNumericActorSeat != null &&
        clocktowerHasDirectNumericInput(step, directNumericActorSeat, cards)
    ) {
        if (step.roleEnName == "Chef" || step.roleEnName == "Empath") {
            val metric = (step.informationQuestion as? ClocktowerInformationQuestion.Number)?.metric
            val expectedMetric = if (step.roleEnName == "Chef")
                com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.ADJACENT_EVIL_PAIRS
            else com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.LIVING_EVIL_NEIGHBOURS
            val current = numericResolution?.takeIf { metric == expectedMetric }
            if (current != null && current.values.isNotEmpty()) {
                val advised = clocktowerMatchingNumericResult(
                    step, directNumericActorSeat, aiSuggestedInformationOption?.proposition, current.subjectSeats,
                )?.takeIf { it in current.values }
                ClocktowerResolvedNumericResultUi(
                    values = current.values,
                    recommendedValue = advised,
                    lastPreviewValue = clocktowerMatchingNumericResult(
                        step, directNumericActorSeat, lastPreviewProposition, current.subjectSeats,
                    ),
                    automaticOnly = allowRuleDeterminedAutoUi,
                    language = language,
                    draftKey = informationDecisionKey,
                    onConfirm = { value ->
                        if (value in current.values) {
                            clocktowerDirectNumericResult(
                                step, directNumericActorSeat, value, cards, subjectSeats = current.subjectSeats,
                            )?.let(onShowPlayerDisplay)
                        }
                    },
                    onDraftChanged = onInformationDraftChanged,
                )
            } else {
                Text(if (language == "en") "Current Host numeric query unavailable."
                    else "当前 Host 数字信息尚不可用。")
            }
            return
        }
        // Stage 5 roles retain their established path pending the separate semantic cutover.
        val determinedNumber = if (step.informationReliability ==
            com.codex.campboardgamehost.clocktower.domain.ReliabilityState.RELIABLE)
            clocktowerMatchingNumericResult(step, directNumericActorSeat, step.displayProposition) else null
        val advised = clocktowerMatchingNumericResult(step, directNumericActorSeat, aiSuggestedInformationOption?.proposition)
        ClocktowerDirectNumericResultUi(
            title = step.displayTitle,
            initialValue = determinedNumber ?: advised,
            lastPreviewValue = clocktowerMatchingNumericResult(step, directNumericActorSeat, lastPreviewProposition),
            automaticOnly = allowRuleDeterminedAutoUi || determinedNumber != null,
            subjectSeats = requireNotNull(step.informationQuestion).subjectSeats,
            actorLabel = requireNotNull(step.actor).seatLabel(cards),
            wakeInstruction = step.storytellerAction,
            language = language,
            onHostTools = onHostTools,
            onNext = onNext,
            onConfirm = { value ->
                clocktowerDirectNumericResult(step, directNumericActorSeat, value, cards)
                    ?.let(onShowPlayerDisplay)
            },
            onDraftChanged = onInformationDraftChanged,
            draftKey = informationDecisionKey,
        )
        return
    }
    val currentAiSuggestedOption = aiSuggestedInformationOption
    // AI_AUTOMATIC can only display a present, current AI result. Do not
    // silently substitute an arbitrary manual option when its advice is absent.
    val automaticDisplayOption = currentAiSuggestedOption.takeIf { allowRuleDeterminedAutoUi }
    val structuredActorSeat = step.actor
        ?.let { actor -> cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1) }
    val undertakerTargetSeat = (step.informationQuestion as? ClocktowerInformationQuestion.Role)
        ?.subjectSeat?.takeIf { it in 1..cards.size }
    val usesUndertakerSquareTable =
        plannedFullScreenSurface == ClocktowerNightFullScreenSurface.Undertaker &&
            step.actor != null && undertakerTargetSeat != null
    val sageSuggestedOption = currentAiSuggestedOption?.takeIf { option ->
        option.displayKind == ClocktowerDisplayKind.EitherOne &&
            clocktowerSageValidPair(option.presentationSubjectSeats, cards.size)
    }
    val usesSageSquareTable =
        plannedFullScreenSurface == ClocktowerNightFullScreenSurface.Sage &&
            step.actor != null && step.isRealAction && cards.size >= 2
    val usesRavenkeeperSquareTable = plannedFullScreenSurface == ClocktowerNightFullScreenSurface.Ravenkeeper
    val ravenkeeperSelectedSeat = if (usesRavenkeeperSquareTable) {
        selectedName
            ?.let { selected -> cards.indexOfFirst { card -> card.name == selected } }
            ?.takeIf { index -> index >= 0 }
            ?.plus(1)
    } else {
        null
    }
    val recommendedRoleRevealOption = currentAiSuggestedOption
    val previewedRole = (lastPreviewProposition as? InformationProposition.RoleAt)?.let { result ->
        clocktowerDirectRoleRevealChoice(step, result.seat, cards.size, result.role.value, scriptRoles) { it }
    }?.roleId?.value
    val ravenkeeperRecommendedChoice = if (usesRavenkeeperSquareTable) {
        clocktowerDirectRecommendedRoleReveal(
            step, ravenkeeperSelectedSeat, cards.size, scriptRoles, recommendedRoleRevealOption,
        )
    } else null
    val fortuneTellerSelectedSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
        .mapNotNull { selectedName ->
            cards.indexOfFirst { it.name == selectedName }
                .takeIf { it >= 0 }
                ?.plus(1)
        }
    val structuredFortuneTellerSelectedSeats = fortuneTellerSelectedSeats
        .takeIf { seats -> seats.size == 2 && seats.distinct().size == 2 }
    val fortuneTellerSelectableSeats = when (fortuneTellerSelectedSeats.size) {
        0 -> cards.indices.mapTo(linkedSetOf()) { index -> index + 1 }
        1 -> cards.indices
            .map { index -> index + 1 }
            .filterTo(linkedSetOf()) { seat -> seat !in fortuneTellerSelectedSeats }
        else -> emptySet()
    }
    val fortuneTellerRecommendedResult = (currentAiSuggestedOption?.proposition as?
        InformationProposition.BooleanResult)?.takeIf {
            it.sourceSeat == structuredActorSeat && it.subjectSeats == structuredFortuneTellerSelectedSeats &&
                it.metric == (step.informationQuestion as? ClocktowerInformationQuestion.Boolean)?.metric
        }?.value

    fun showRecommendedDisplayOption(
        option: ClocktowerDisplayOption,
        transformDisplayStep: (ClocktowerNightStepUi) -> ClocktowerNightStepUi = { it },
    ) {
        onShowPlayerDisplay(transformDisplayStep(resolveClocktowerPlayerDisplay(step, option)))
    }

    // A suggestion may be preselected, but opening the player's fullscreen
    // is always an explicit live Storyteller action, including AI_AUTOMATIC.

    fun showUndertakerChoice(choice: ClocktowerDirectRoleRevealChoice) {
        fun undertakerDisplay(displayStep: ClocktowerNightStepUi): ClocktowerNightStepUi =
            clocktowerUndertakerPlayerDisplayStep(displayStep, choice, cards, language)

        showRecommendedDisplayOption(choice.displayOption, ::undertakerDisplay)
    }

    fun showRavenkeeperChoice(choice: ClocktowerDirectRoleRevealChoice) {
        showRecommendedDisplayOption(choice.displayOption)
    }


    fun showSageManualPair(seats: List<Int>) {
        clocktowerSageManualPlayerDisplay(step, seats, cards.size)?.let(onShowPlayerDisplay)
    }

    fun showStructuredFortuneTellerResult(value: Boolean) {
        val actorSeat = structuredActorSeat ?: return
        val subjects = structuredFortuneTellerSelectedSeats ?: return
        clocktowerDirectBooleanResult(step, actorSeat, subjects, value, language)
            ?.let(onShowPlayerDisplay)
    }

    // Fortune Teller, Chambermaid, and Ravenkeeper show the recommended
    // result only after the player actually selects their query targets and
    // the Storyteller explicitly taps a display button. No automatic reveal.

    val compactNightGuidance = clocktowerBeginnerNightGuidance(
        action = step.action,
        actorIsPoisoned = poisonedPlayerName != null && step.actor?.name == poisonedPlayerName,
        actor = step.actor,
        cards = cards,
        language = language,
        groupTeam = step.actor
            ?.clocktowerRole
            ?.team
            ?.takeIf { team ->
                step.displayKind == ClocktowerDisplayKind.EvilInfo &&
                    team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)
            },
    )
    val command = compactNightGuidance?.asWakeInstruction(
        // Wake instruction formatting is a presentation choice, never AI authority.
        expandedFormat = true,
    ) ?: when {
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
            if (language == "en") {
                "Wake ${step.actor.seatLabel(cards)} and ask them to choose two players to check"
            } else {
                "唤醒 ${step.actor.seatLabel(cards)}，让他选择两名想要查验的玩家"
            }
        }
        step.wakeText != null -> step.wakeText
        step.actor != null -> if (language == "en") "Wake ${step.actor.seatLabel(cards)}" else "唤醒 ${step.actor.seatLabel(cards)}"
        else -> step.title
    }
    val helper = when {
        step.action == ClocktowerNightAction.Chambermaid -> if (language == "en") "Have them choose two players, then show only the number." else "让她选择两名玩家，点查询后直接展示数字。"
        step.action == ClocktowerNightAction.FortuneTeller -> if (language == "en") "Have them choose two players, then show Yes or No." else "让他选择两名玩家，点查询后展示“有”或“没有”。"
        step.action == ClocktowerNightAction.RedHerring -> if (language == "en") "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon." else "选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。"
        step.action == ClocktowerNightAction.Poison -> if (language == "en") "Record the poisoned player." else "记录中毒的玩家。"
        step.action == ClocktowerNightAction.ButlerMaster -> if (language == "en") "Have the Butler choose a master. During voting, the Butler may vote only if their master votes." else "让他选择主人。白天计票时，只有主人投票时，管家才能投票，需要管家自律。"
        step.action == ClocktowerNightAction.MonkProtect -> if (language == "en") "Record the protected player." else "记录被保护的玩家。"
        step.action == ClocktowerNightAction.DemonKill -> if (language == "en") "Record the selected kill target." else "记录被击杀的玩家。"
        step.action == ClocktowerNightAction.MayorRedirect -> if (language == "en") "Let the Mayor die or redirect the death to another player." else "选择让市长死亡，或将死亡转移给另一名玩家。"
        step.action == ClocktowerNightAction.Ravenkeeper -> if (language == "en") "After choosing a target, show that character only to the Ravenkeeper." else "选目标后，把该玩家角色只给他看。"
        step.displayKind != ClocktowerDisplayKind.None -> if (language == "en") "Show the information to the player." else "展示信息给玩家。"
        else -> step.explanation
    }
    val ownsFullScreenSurface = surfacePlan.ownsFullScreenHostSurface
    Card(
        modifier = if (ownsFullScreenSurface) Modifier.fillMaxSize() else Modifier,
        shape = RoundedCornerShape(if (ownsFullScreenSurface) 0.dp else 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = if (ownsFullScreenSurface) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            },
            verticalArrangement = if (ownsFullScreenSurface) Arrangement.Top else Arrangement.spacedBy(14.dp),
        ) {
            if (!ownsFullScreenSurface) {
                Text(
                    if (step.actor != null) {
                        if (language == "en") "CURRENT PLAYER" else "当前玩家"
                    } else {
                        if (language == "en") "CURRENT STEP" else "当前步骤"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    command.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Black,
                )
            }

        val nightActionSeats = cards.mapIndexed { index, card ->
            card.toStorytellerHostSeatPresentation(
                seatNumber = index + 1,
                language = language,
                ghostVoteAuthority = ghostVoteAuthority,
                isPoisoned = card.name == poisonedPlayerName,
            ).copy(actualAlignment = actualAlignmentBySeat[index + 1])
        }
        fun seatNumberForName(name: String?): Int? = name
            ?.let { selected -> cards.indexOfFirst { card -> card.name == selected } }
            ?.takeIf { index -> index >= 0 }
            ?.plus(1)
        fun selectableSeatNumbers(candidates: List<PlayerCard>): Set<Int> = candidates
            .mapNotNull { candidate -> seatNumberForName(candidate.name) }
            .toSet()
        val actionActorSeat = seatNumberForName(step.actor?.name)
        val redHerringSeat = seatNumberForName(redHerring)
        val evilInfoTeam = step.actor
            ?.clocktowerRole
            ?.team
            ?.takeIf { team ->
                step.displayKind == ClocktowerDisplayKind.EvilInfo &&
                    team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)
            }
        val evilInfoTeamCards = evilInfoTeam
            ?.let { team -> cards.filter { card -> card.clocktowerRole?.team == team } }
            .orEmpty()
        val evilInfoHighlightedSeats = if (evilInfoTeam == ClocktowerTeam.Minion) {
            evilInfoTeamCards.mapNotNull { card -> seatNumberForName(card.name) }.toSet()
        } else {
            emptySet()
        }
        val evilInfoGroupLines = if (evilInfoTeam == ClocktowerTeam.Minion) {
            evilInfoTeamCards.mapNotNull { card ->
                seatNumberForName(card.name)?.let { seat ->
                    if (language == "en") "P$seat ${card.name}" else "${seat}号 ${card.name}"
                }
            }
        } else {
            emptyList()
        }
        val evilInfoSquareTablePresentation = if (
            plannedFullScreenSurface == ClocktowerNightFullScreenSurface.EvilInformation
        ) {
            clocktowerEvilInfoSquareTablePresentation(
                step = step,
                actorSeat = actionActorSeat,
                wakeInstruction = command,
                compactTeamWake = evilInfoTeam != null,
                highlightedSeats = evilInfoHighlightedSeats,
                groupLines = evilInfoGroupLines,
            )
        } else {
            null
        }
        val plannedSpecialistUnavailable = when (plannedFullScreenSurface) {
            ClocktowerNightFullScreenSurface.EvilInformation -> evilInfoSquareTablePresentation == null
            ClocktowerNightFullScreenSurface.PairInformation -> false
            ClocktowerNightFullScreenSurface.Chef -> true
            ClocktowerNightFullScreenSurface.Empath -> true
            ClocktowerNightFullScreenSurface.Undertaker -> !usesUndertakerSquareTable
            ClocktowerNightFullScreenSurface.Spy -> false
            ClocktowerNightFullScreenSurface.Clockmaker -> true
            ClocktowerNightFullScreenSurface.Sage -> !usesSageSquareTable
            else -> false
        }
        val plainInformationDisplayStep = automaticDisplayOption
            ?.let { option -> resolveClocktowerPlayerDisplay(step, option) }
            ?: step
        val plainInformationSquareTablePresentation = if (
            plannedFullScreenSurface == ClocktowerNightFullScreenSurface.PlainInformation ||
            plannedFullScreenSurface == ClocktowerNightFullScreenSurface.Spy ||
            plannedSpecialistUnavailable
        ) {
            clocktowerPlainInformationSquareTablePresentation(
                step = plainInformationDisplayStep,
                actorSeat = actionActorSeat,
                wakeInstruction = command,
                automaticOnly = allowRuleDeterminedAutoUi,
            ) ?: ClocktowerPlainInformationSquareTablePresentation(
                actorSeat = actionActorSeat,
                wakeInstruction = command,
                displayStep = plainInformationDisplayStep,
                showPlayerDisplayAction = false,
            )
        } else {
            null
        }
        val onSingleTargetEvent: (ClocktowerSingleTargetEvent) -> Unit = { event ->
            when (event) {
                is ClocktowerSingleTargetEvent.SelectSeat -> cards.getOrNull(event.seat - 1)?.name?.let(onSelectName)
                ClocktowerSingleTargetEvent.ShowResult -> onShowPlayerDisplay(step)
                ClocktowerSingleTargetEvent.Next -> onNext()
            }
        }
        @Composable
        fun renderPlainInformationFallback() {
            val presentation = checkNotNull(plainInformationSquareTablePresentation) {
                "A planned plain/fallback night surface requires a concrete presentation."
            }
            ClocktowerPlainInformationSquareTableDialog(
                seats = nightActionSeats,
                presentation = presentation,
                language = language,

                onHostTools = onHostTools,
                onNext = onNext,
                onShowPlayerDisplay = { onShowPlayerDisplay(presentation.displayStep) },
            )
        }

        when (plannedFullScreenSurface) {
            ClocktowerNightFullScreenSurface.SingleTarget -> {
                val candidates = when (step.action) {
                    ClocktowerNightAction.RedHerring -> cards
                    ClocktowerNightAction.ButlerMaster -> cards.filter { it.name != step.actor?.name }
                    ClocktowerNightAction.MonkProtect -> cards
                    else -> aliveCards
                }
                val presentation = checkNotNull(
                    clocktowerSingleTargetAbilityPresentation(
                        action = step.action,
                        selection = ClocktowerSingleTargetSelection(
                            seatNumberForName(selectedName), selectableSeatNumbers(candidates), step.isRealAction,
                        ),
                        actorSeat = actionActorSeat,
                        wakeInstruction = command,
                        canShowResult =
                            step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None,
                    ),
                ) { "SingleTarget surface plan requires a supported real target action." }
                key(step.action) {
                    ClocktowerSingleTargetAbilitySection(nightActionSeats, presentation, language, onHostTools, onSingleTargetEvent)
                }
            }

            ClocktowerNightFullScreenSurface.Ravenkeeper -> {
                val candidates = cards
                ClocktowerRavenkeeperSquareTableDialog(
                    interactionKey = informationDecisionKey,
                    seats = nightActionSeats,
                    actorSeat = actionActorSeat,
                    selectedSeat = ravenkeeperSelectedSeat,
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    wakeInstruction = command,
                    step = step,
                    scriptRoles = scriptRoles,
                    recommendedOption = recommendedRoleRevealOption,
                    lastPreviewRoleId = previewedRole,
                    automaticOnly = allowRuleDeterminedAutoUi,
                    roleLabel = { roleId ->
                        clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language)
                    },
                    language = language,

                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },

                    onHostTools = onHostTools,
                    onNext = onNext,
                    onConfirm = ::showRavenkeeperChoice,
                    onDraftChanged = onInformationDraftChanged,
                )
            }

            ClocktowerNightFullScreenSurface.FortuneTeller -> {
                ClocktowerFortuneTellerSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeats = fortuneTellerSelectedSeats,
                    selectableSeats = fortuneTellerSelectableSeats,
                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    redHerringSeat = redHerringSeat,
                    recommendedResult = fortuneTellerRecommendedResult,
                    lastPreviewResult = (lastPreviewProposition as? InformationProposition.BooleanResult)?.takeIf {
                        it.sourceSeat == structuredActorSeat && it.subjectSeats == structuredFortuneTellerSelectedSeats &&
                            it.metric == (step.informationQuestion as? ClocktowerInformationQuestion.Boolean)?.metric
                    }?.value,
                    automaticOnly = allowRuleDeterminedAutoUi,
                    language = language,

                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let { name ->
                            when (twoPlayerSelectionAction(fortuneTellerFirst, fortuneTellerSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> onSelectFortuneTellerFirst(name)
                                TwoPlayerSelectionAction.ToggleSecond -> onSelectFortuneTellerSecond(name)
                                TwoPlayerSelectionAction.RejectLimit -> Unit
                            }
                        }
                    },
                    onResultSelected = ::showStructuredFortuneTellerResult,

                    onHostTools = onHostTools,
                    onNext = onNext,
                )
            }

            ClocktowerNightFullScreenSurface.Chambermaid -> {
                val candidates = chambermaidTargetCards.filter { it.name != step.actor?.name }
                val selectedSeats = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull(::seatNumberForName)
                val candidateSeats = selectableSeatNumbers(candidates)
                val selectableSeats = when (selectedSeats.size) {
                    0 -> candidateSeats
                    1 -> candidateSeats - selectedSeats.first()
                    else -> emptySet()
                }
                // Chambermaid's actual query targets belong to the live player.
                // Advice prepared for a different pair must not be displayed
                // as though it answered the newly selected pair.
                val currentChambermaidRecommendation = clocktowerCurrentChambermaidRecommendation(
                    option = currentAiSuggestedOption,
                    actorSeat = structuredActorSeat,
                    selectedSeats = selectedSeats,
                )
                val resultOptions = if (allowRuleDeterminedAutoUi) {
                    listOfNotNull(currentChambermaidRecommendation)
                } else {
                    step.displayOptions
                }
                val recommendedResultOptions = listOfNotNull(currentChambermaidRecommendation)
                ClocktowerChambermaidSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeats = selectedSeats,
                    selectableSeats = selectableSeats,
                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    recommendedResultOptions = recommendedResultOptions,
                    resultOptions = resultOptions,
                    automaticOnly = allowRuleDeterminedAutoUi,
                    language = language,

                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let { name ->
                            when (twoPlayerSelectionAction(chambermaidFirst, chambermaidSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> onSelectChambermaidFirst(name)
                                TwoPlayerSelectionAction.ToggleSecond -> onSelectChambermaidSecond(name)
                                TwoPlayerSelectionAction.RejectLimit -> Unit
                            }
                        }
                    },
                    onResultSelected = ::showRecommendedDisplayOption,
                    numericInput = if (!allowRuleDeterminedAutoUi &&
                        selectedSeats.size == 2 && structuredActorSeat != null) ({
                        ClocktowerDirectNumericResultUi(
                            title = step.displayTitle,
                            initialValue = (currentChambermaidRecommendation?.proposition as? InformationProposition.NumericResult)?.value,
                            lastPreviewValue = clocktowerMatchingNumericResult(
                                step, structuredActorSeat, lastPreviewProposition, selectedSeats,
                            ),
                            subjectSeats = selectedSeats,
                            actorLabel = requireNotNull(step.actor).seatLabel(cards), wakeInstruction = null,
                            language = language, onHostTools = onHostTools,
                            onNext = onNext, onConfirm = { value ->
                                clocktowerDirectNumericResult(
                                    step, structuredActorSeat, value, cards, subjectSeats = selectedSeats,
                                )?.let(onShowPlayerDisplay)
                            },
                            onDraftChanged = onInformationDraftChanged,
                            draftKey = informationDecisionKey,
                        )
                    }) else null,

                    onHostTools = onHostTools,
                    onNext = onNext,
                )
            }

            ClocktowerNightFullScreenSurface.Ruling -> {
                val candidates = mayorRedirectTargetCards
                val mayor = aliveCards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                val presentation = checkNotNull(
                    clocktowerNightRulingPresentation(
                        action = step.action,
                        selection = ClocktowerSingleTargetSelection(
                            seatNumberForName(selectedName), selectableSeatNumbers(candidates), step.isRealAction,
                        ),
                        mayorSeat = seatNumberForName(mayor?.name),
                    ),
                ) { "Ruling surface plan requires Mayor redirect or Demon succession." }
                key(step.action) {
                    ClocktowerNightRulingSection(nightActionSeats, presentation, language, onHostTools, onSingleTargetEvent)
                }
            }

            ClocktowerNightFullScreenSurface.EvilInformation -> {
                val presentation = evilInfoSquareTablePresentation
                if (presentation == null) {
                    renderPlainInformationFallback()
                } else {
                    ClocktowerEvilInfoSquareTableDialog(
                        seats = nightActionSeats,
                        presentation = presentation,
                        language = language,

                        onHostTools = onHostTools,
                        onNext = onNext,
                        onShowPlayerDisplay = { onShowPlayerDisplay(step) },
                    )
                }
            }

            ClocktowerNightFullScreenSurface.PairInformation -> {
                ClocktowerPairInformationSquareTableDialog(
                    interactionKey = informationDecisionKey,
                    recommendedOption = currentAiSuggestedOption,
                    lastPreviewProposition = lastPreviewProposition,
                    automaticOnly = allowRuleDeterminedAutoUi,
                    scriptRoles = scriptRoles,
                    abilityRoleEnName = requireNotNull(presentationRoleEnName),
                    seats = nightActionSeats,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    abilityLabel = presentationRoleEnName
                        ?.let { roleId -> clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language) }
                        ?: step.title,
                    roleLabel = { roleId ->
                        clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language)
                    },
                    language = language,

                    onHostTools = onHostTools,
                    onNext = onNext,
                    onConfirm = ::showRecommendedDisplayOption,
                    onDraftChanged = onInformationDraftChanged,
                )
            }

            ClocktowerNightFullScreenSurface.Chef,
            ClocktowerNightFullScreenSurface.Empath -> renderPlainInformationFallback()

            ClocktowerNightFullScreenSurface.Undertaker -> {
                if (!usesUndertakerSquareTable) {
                    renderPlainInformationFallback()
                } else ClocktowerUndertakerSquareTableDialog(
                    interactionKey = informationDecisionKey,
                    seats = nightActionSeats,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    step = step,
                    scriptRoles = scriptRoles,
                    recommendedOption = recommendedRoleRevealOption,
                    lastPreviewRoleId = previewedRole,
                    automaticOnly = allowRuleDeterminedAutoUi,
                    roleLabel = { roleId ->
                        clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language)
                    },
                    language = language,

                    onHostTools = onHostTools,
                    onNext = onNext,
                    onConfirm = ::showUndertakerChoice,
                    onDraftChanged = onInformationDraftChanged,
                )
            }

            ClocktowerNightFullScreenSurface.Spy -> renderPlainInformationFallback()

            ClocktowerNightFullScreenSurface.Clockmaker -> renderPlainInformationFallback()

            ClocktowerNightFullScreenSurface.Sage -> {
                if (!usesSageSquareTable) {
                    renderPlainInformationFallback()
                } else ClocktowerSageSquareTableDialog(
                    interactionKey = informationDecisionKey,
                    seats = nightActionSeats,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    recommendedSeats = sageSuggestedOption?.presentationSubjectSeats,
                    lastPreviewSeats = clocktowerSagePreviewSeats(lastPreviewProposition, cards.size),
                    automaticOnly = allowRuleDeterminedAutoUi,
                    language = language,

                    onHostTools = onHostTools,
                    onNext = onNext,
                    onConfirmRecommended = { sageSuggestedOption?.let(::showRecommendedDisplayOption) },
                    onConfirmManual = ::showSageManualPair,
                    onDraftChanged = onInformationDraftChanged,
                )
            }

            ClocktowerNightFullScreenSurface.PlainInformation -> renderPlainInformationFallback()
            null -> Unit
        }

            if (!ownsFullScreenSurface) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (language == "en") "STEP NOTE" else "步骤提示",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            helper,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
