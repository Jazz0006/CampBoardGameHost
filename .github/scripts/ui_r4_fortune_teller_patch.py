from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

selected_seats_old = '''    val structuredFortuneTellerSelectedSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
        .mapNotNull { selectedName ->
            cards.indexOfFirst { it.name == selectedName }
                .takeIf { it >= 0 }
                ?.plus(1)
        }
        .takeIf { seats -> seats.size == 2 && seats.distinct().size == 2 }
'''
selected_seats_new = '''    val fortuneTellerSelectedSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
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
'''

model_condition_old = '''    val structuredFortuneTellerUiModel = if (
        step.action == ClocktowerNightAction.FortuneTeller &&
        step.roleEnName == "Fortune Teller" &&
        step.informationReliability != InformationReliability.RELIABLE &&
        structuredFortuneTellerActorSeat != null &&
        structuredFortuneTellerSelectedSeats != null &&
        structuredFortuneTellerProposition != null
    ) {
'''
model_condition_new = '''    val structuredFortuneTellerUiModel = if (
        step.action == ClocktowerNightAction.FortuneTeller &&
        step.roleEnName == "Fortune Teller" &&
        structuredFortuneTellerActorSeat != null &&
        structuredFortuneTellerSelectedSeats != null &&
        structuredFortuneTellerProposition != null
    ) {
'''

model_tail_old = '''    } else {
        null
    }
    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel
'''
model_tail_new = '''    } else {
        null
    }
    val fortuneTellerLegalResults = structuredFortuneTellerUiModel
        ?.choices
        ?.mapTo(linkedSetOf()) { choice -> choice.value }
        .orEmpty()
    val fortuneTellerRecommendedResult = structuredFortuneTellerUiModel
        ?.choices
        ?.firstOrNull { choice -> choice.recommended }
        ?.value
    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel
'''

helper_anchor_old = '''    }
    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
'''
helper_anchor_new = '''    }

    fun showStructuredFortuneTellerResult(value: Boolean) {
        val model = structuredFortuneTellerUiModel ?: return
        val actorSeat = structuredFortuneTellerActorSeat ?: return
        val subjectSeats = structuredFortuneTellerSelectedSeats ?: return
        val choice = model.choices.firstOrNull { it.value == value } ?: return
        val currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision)
        val confirmation = if (choice.recommended) {
            model.acceptRecommendation(choice.candidateId, currentRevision)
        } else {
            model.chooseManually(choice.candidateId, currentRevision)
        }
        val confirmed = confirmation.confirmed ?: return
        val selectedOption = findBooleanDisplayOption(
            options = (
                displayedInformationOptions +
                    step.displayOptions +
                    step.legacyInformationCandidates
                ).distinctBy(::optionId),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = actorSeat,
            subjectSeats = subjectSeats,
            value = value,
        )
        val displayStep = selectedOption?.let { option ->
            step.copy(
                tellPlayer = option.displayPrimary,
                displayKind = option.displayKind,
                displayTitle = option.displayTitle,
                displayPrimary = option.displayPrimary,
                displaySecondary = option.displaySecondary,
                displayFooter = option.displayFooter,
                displayProposition = confirmed.draft.proposition,
                selectedInformationTruthful = option.isTruthful,
                informationDecisionConfirmation = confirmed,
                informationDecisionExpectedSnapshot = model.contextSnapshot,
                displayOptions = emptyList(),
                recommendedDisplayOptions = emptyList(),
            )
        } ?: step.copy(
            displayProposition = confirmed.draft.proposition,
            informationDecisionConfirmation = confirmed,
            informationDecisionExpectedSnapshot = model.contextSnapshot,
            displayOptions = emptyList(),
            recommendedDisplayOptions = emptyList(),
        )
        onShowPlayerDisplay(displayStep)
    }

    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
'''

legacy_state_old = '''    var fortuneTellerLimitExceeded by remember(step.actor?.name, step.title) { mutableStateOf(false) }
    var showFortuneTellerDisplayOptions by remember(step.actor?.name, step.title) { mutableStateOf(false) }
'''
legacy_state_new = ''''''

fortune_action_old = '''            ClocktowerNightAction.FortuneTeller -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectableTwoPlayerChips(
                        cards = cards,
                        firstSelectedName = fortuneTellerFirst,
                        secondSelectedName = fortuneTellerSecond,
                        enabled = step.isRealAction,
                        onSelect = { name ->
                            showFortuneTellerDisplayOptions = false
                            when (twoPlayerSelectionAction(fortuneTellerFirst, fortuneTellerSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> {
                                    fortuneTellerLimitExceeded = false
                                    onSelectFortuneTellerFirst(name)
                                }
                                TwoPlayerSelectionAction.ToggleSecond -> {
                                    fortuneTellerLimitExceeded = false
                                    onSelectFortuneTellerSecond(name)
                                }
                                TwoPlayerSelectionAction.RejectLimit -> {
                                    fortuneTellerLimitExceeded = true
                                }
                            }
                        },
                    )
                    if (fortuneTellerLimitExceeded) {
                        Text(
                            stringResource(R.string.clocktower_fortune_cannot_select_more_than_two),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else if (fortuneTellerFirst == null || fortuneTellerSecond == null) {
                        Text(
                            stringResource(R.string.clocktower_fortune_must_select_two),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(
                        onClick = {
                            if (automaticStorytellerInfo && automaticDisplayOption != null) {
                                showRecommendedDisplayOption(automaticDisplayOption)
                            } else if (step.displayOptions.isNotEmpty() || displayedInformationOptions.isNotEmpty()) {
                                showFortuneTellerDisplayOptions = true
                            } else {
                                onShowPlayerDisplay(step)
                            }
                        },
                        enabled = step.isRealAction && fortuneTellerFirst != null && fortuneTellerSecond != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_fortune_show_result))
                    }
                    if (showFortuneTellerDisplayOptions) {
                        Text(
                            stringResource(R.string.clocktower_fortune_unreliable_choose_result),
                            color = Color(0xFF8C4B20),
                        )
                    }
                }
            }
'''
fortune_action_new = '''            ClocktowerNightAction.FortuneTeller -> {
                ClocktowerFortuneTellerSquareTableDialog(
                    seats = cards.mapIndexed { index, card ->
                        ClocktowerFortuneTellerSeatUiModel(
                            seatId = "seat-${index + 1}",
                            seatNumber = index + 1,
                            label = card.name,
                        )
                    },
                    selectedSeats = fortuneTellerSelectedSeats,
                    selectableSeats = fortuneTellerSelectableSeats,
                    enabled = step.isRealAction,
                    legalResults = fortuneTellerLegalResults,
                    recommendedResult = fortuneTellerRecommendedResult,
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    canGoPrevious = canGoPrevious,
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
                    onAutomaticResultSelected = ::showStructuredFortuneTellerResult,
                    onPrevious = onPrevious,
                )
            }
'''

legacy_boolean_panel_old = '''            if (showFortuneTellerDisplayOptions) {
                structuredFortuneTellerUiModel?.let { model ->
                    val actorSeat = requireNotNull(structuredFortuneTellerActorSeat)
                    val subjectSeats = requireNotNull(structuredFortuneTellerSelectedSeats)
                    StructuredBooleanInformationDecisionPanel(
                        model = model,
                        currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        language = language,
                        roleLabel = step.title,
                        onConfirmed = { confirmed, value ->
                            val selectedOption = findBooleanDisplayOption(
                                options = (
                                    displayedInformationOptions +
                                        step.displayOptions +
                                        step.legacyInformationCandidates
                                    ).distinctBy(::optionId),
                                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                                sourceSeat = actorSeat,
                                subjectSeats = subjectSeats,
                                value = value,
                            )
                            if (selectedOption != null) {
                                onShowPlayerDisplay(
                                    step.copy(
                                        tellPlayer = selectedOption.displayPrimary,
                                        displayKind = selectedOption.displayKind,
                                        displayTitle = selectedOption.displayTitle,
                                        displayPrimary = selectedOption.displayPrimary,
                                        displaySecondary = selectedOption.displaySecondary,
                                        displayFooter = selectedOption.displayFooter,
                                        displayProposition = confirmed.draft.proposition,
                                        selectedInformationTruthful = selectedOption.isTruthful,
                                        informationDecisionConfirmation = confirmed,
                                        informationDecisionExpectedSnapshot = model.contextSnapshot,
                                        displayOptions = emptyList(),
                                        recommendedDisplayOptions = emptyList(),
                                    ),
                                )
                            }
                        },
                    )
                }
            }

'''
legacy_boolean_panel_new = ''''''

legacy_generic_gate = '''(step.action != ClocktowerNightAction.FortuneTeller || showFortuneTellerDisplayOptions)'''
new_generic_gate = '''step.action != ClocktowerNightAction.FortuneTeller'''

replacements = [
    ("selected seats", selected_seats_old, selected_seats_new, 1),
    ("Foundation model condition", model_condition_old, model_condition_new, 1),
    ("Foundation model projections", model_tail_old, model_tail_new, 1),
    ("confirmed result helper", helper_anchor_old, helper_anchor_new, 1),
    ("legacy local state", legacy_state_old, legacy_state_new, 1),
    ("Fortune Teller action UI", fortune_action_old, fortune_action_new, 1),
    ("legacy Boolean panel", legacy_boolean_panel_old, legacy_boolean_panel_new, 1),
    ("legacy generic Fortune Teller gate", legacy_generic_gate, new_generic_gate, 2),
]

for label, old, _new, expected in replacements:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact anchor occurrence(s), found {count}")

for _label, old, new, expected in replacements:
    text = text.replace(old, new, expected)

required = [
    "ClocktowerFortuneTellerSquareTableDialog(",
    "model.acceptRecommendation(choice.candidateId, currentRevision)",
    "model.chooseManually(choice.candidateId, currentRevision)",
    "val confirmed = confirmation.confirmed ?: return",
    "informationDecisionConfirmation = confirmed",
    "val fortuneTellerLegalResults = structuredFortuneTellerUiModel",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Required postcondition missing: {token}")

for token in ["showFortuneTellerDisplayOptions", "fortuneTellerLimitExceeded"]:
    if token in text:
        raise SystemExit(f"Legacy Fortune Teller UI token remains: {token}")

if text.count("ClocktowerFortuneTellerSquareTableDialog(") != 1:
    raise SystemExit("Expected exactly one Fortune Teller square-table dialog wiring")

path.write_text(text, encoding="utf-8", newline="\n")
