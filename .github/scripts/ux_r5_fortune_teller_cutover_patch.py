from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
NIGHT_UI = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")
    return raw.decode("utf-8")


def require_count(text: str, needle: str, expected: int, label: str) -> None:
    actual = text.count(needle)
    if actual != expected:
        raise SystemExit(f"{label}: expected {expected} anchor occurrence(s), found {actual}")


host = read_lf(HOST)
night = read_lf(NIGHT_UI)

host_first_old = '''                                action = ClocktowerNightAction.FortuneTeller,
                                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
'''
host_first_new = '''                                action = ClocktowerNightAction.FortuneTeller,
                                displayProposition = fortuneTellerMatched?.let { matched ->
                                    roleActor("Fortune Teller")?.let { actor ->
                                        InformationProposition.BooleanResult(
                                            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                                            cards.indexOf(actor) + 1,
                                            listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                                                cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                                            },
                                            matched,
                                        )
                                    }
                                },
                                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
'''

host_other_old = '''                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
'''
host_other_new = '''                action = ClocktowerNightAction.FortuneTeller,
                displayProposition = fortuneTellerMatched?.let { matched ->
                    roleActor("Fortune Teller")?.let { actor ->
                        InformationProposition.BooleanResult(
                            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                            cards.indexOf(actor) + 1,
                            listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                                cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                            },
                            matched,
                        )
                    }
                },
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
'''

night_model_old = '''    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel

    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
'''
night_model_new = '''    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel
    fun booleanOptionValue(option: ClocktowerDisplayOption?): Boolean? =
        (option?.proposition as? InformationProposition.BooleanResult)?.value
    val structuredFortuneTellerActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredFortuneTellerProposition = (step.displayProposition as? InformationProposition.BooleanResult)
        ?.takeIf { it.metric == com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT }
    val structuredFortuneTellerRecommendedOption = if (automaticStorytellerInfo) {
        automaticDisplayOption
    } else {
        displayedInformationOptions.firstOrNull { it.isDefaultRecommendation }
            ?: step.displayOptions.firstOrNull { it.isDefaultRecommendation }
            ?: automaticDisplayOption
    }
    val structuredFortuneTellerRecommendedValue = booleanOptionValue(structuredFortuneTellerRecommendedOption)
    val structuredFortuneTellerUiModel = if (
        step.action == ClocktowerNightAction.FortuneTeller &&
        step.roleEnName == "Fortune Teller" &&
        step.spyRegistrationKey == null &&
        step.recluseRegistrationKey == null &&
        structuredFortuneTellerActorSeat != null &&
        structuredFortuneTellerProposition != null &&
        structuredFortuneTellerProposition.subjectSeats.size == 2
    ) {
        prepareBooleanInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredFortuneTellerActorSeat,
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Fortune Teller"),
            metric = structuredFortuneTellerProposition.metric,
            subjectSeats = structuredFortuneTellerProposition.subjectSeats,
            trueValue = structuredFortuneTellerProposition.value,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredFortuneTellerRecommendedValue,
            falseMisinformationPressure = 3,
        )
    } else {
        null
    }
    val fortuneTellerYesLabel = stringResource(R.string.clocktower_yes)
    val fortuneTellerNoLabel = stringResource(R.string.clocktower_no)

    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
'''

fortune_button_old = '''                        onClick = {
                            if (automaticStorytellerInfo && automaticDisplayOption != null) {
                                showRecommendedDisplayOption(automaticDisplayOption)
                            } else if (step.displayOptions.isNotEmpty() || displayedInformationOptions.isNotEmpty()) {
                                showFortuneTellerDisplayOptions = true
                            } else {
                                onShowPlayerDisplay(step)
                            }
                        },
'''
fortune_button_new = '''                        onClick = {
                            if (structuredFortuneTellerUiModel != null) {
                                showFortuneTellerDisplayOptions = true
                            } else if (automaticStorytellerInfo && automaticDisplayOption != null) {
                                showRecommendedDisplayOption(automaticDisplayOption)
                            } else if (step.displayOptions.isNotEmpty() || displayedInformationOptions.isNotEmpty()) {
                                showFortuneTellerDisplayOptions = true
                            } else {
                                onShowPlayerDisplay(step)
                            }
                        },
'''

boolean_panel_anchor = '''            }

            pairRecommendationPresentation?.let { presentation ->
'''
boolean_panel_insert = '''            }

            if (showFortuneTellerDisplayOptions) {
                structuredFortuneTellerUiModel?.let { model ->
                    val template = structuredFortuneTellerRecommendedOption
                        ?: step.displayOptions.firstOrNull()
                        ?: step.legacyInformationCandidates.firstOrNull()
                    StructuredBooleanInformationDecisionPanel(
                        model = model,
                        currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        language = language,
                        roleLabel = step.title,
                        onConfirmed = { confirmed, value ->
                            if (automaticDisplayOption != null) {
                                selectionAudit?.let { audit ->
                                    audit.recorder.recordCommittedSelection(
                                        SelectionAuditCommit(
                                            selectionId = audit.selectionId,
                                            dimensions = audit.dimensions,
                                            selectedFamilyId = DynamicCandidateGenerator.selectionAuditFamilyId(
                                                reliability = step.informationReliability,
                                                truthful = value == structuredFortuneTellerProposition.value,
                                            ),
                                        ),
                                    )
                                }
                            }
                            val displayValue = if (value) fortuneTellerYesLabel else fortuneTellerNoLabel
                            onShowPlayerDisplay(
                                step.copy(
                                    tellPlayer = displayValue,
                                    displayKind = ClocktowerDisplayKind.YesNo,
                                    displayTitle = template?.displayTitle ?: step.displayTitle,
                                    displayPrimary = displayValue,
                                    displaySecondary = step.displaySecondary ?: template?.displaySecondary,
                                    displayFooter = template?.displayFooter ?: step.displayFooter ?: step.explanation,
                                    displayProposition = confirmed.draft.proposition,
                                    selectedInformationTruthful = value == structuredFortuneTellerProposition.value,
                                    informationDecisionConfirmation = confirmed,
                                    informationDecisionExpectedSnapshot = model.contextSnapshot,
                                    displayOptions = emptyList(),
                                    recommendedDisplayOptions = emptyList(),
                                ),
                            )
                        },
                    )
                }
            }

            pairRecommendationPresentation?.let { presentation ->
'''

legacy_recommended_old = '''                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
'''
legacy_recommended_new = '''                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
'''

legacy_manual_old = '''            if (
                structuredNumberUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
'''
legacy_manual_new = '''            if (
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
'''

require_count(host, host_first_old, 1, "first-night Fortune Teller proposition anchor")
require_count(host, host_other_old, 1, "other-night Fortune Teller proposition anchor")
require_count(night, night_model_old, 1, "Fortune Teller structured-model insertion")
require_count(night, fortune_button_old, 1, "Fortune Teller result button")
require_count(night, boolean_panel_anchor, 1, "Boolean panel insertion anchor")
require_count(night, legacy_recommended_old, 1, "legacy recommended fallback guard")
require_count(night, legacy_manual_old, 1, "legacy manual fallback guard")

host = host.replace(host_first_old, host_first_new, 1)
host = host.replace(host_other_old, host_other_new, 1)
night = night.replace(night_model_old, night_model_new, 1)
night = night.replace(fortune_button_old, fortune_button_new, 1)
night = night.replace(boolean_panel_anchor, boolean_panel_insert, 1)
night = night.replace(legacy_recommended_old, legacy_recommended_new, 1)
night = night.replace(legacy_manual_old, legacy_manual_new, 1)

required_host = [
    'displayProposition = fortuneTellerMatched?.let { matched ->',
    'BooleanMetric.DEMON_OR_RED_HERRING_PRESENT',
]
required_night = [
    'val structuredFortuneTellerUiModel = if (',
    'prepareBooleanInformationUiModel(',
    'StructuredBooleanInformationDecisionPanel(',
    'informationDecisionExpectedSnapshot = model.contextSnapshot',
    'structuredFortuneTellerUiModel == null',
]
for token in required_host:
    if token not in host:
        raise SystemExit(f"Missing Host postcondition: {token}")
for token in required_night:
    if token not in night:
        raise SystemExit(f"Missing Night UI postcondition: {token}")
if host.count('displayProposition = fortuneTellerMatched?.let { matched ->') != 2:
    raise SystemExit("Fortune Teller truthful proposition must be wired in exactly two night materializers")
if night.count('StructuredBooleanInformationDecisionPanel(') != 1:
    raise SystemExit("Structured Fortune Teller panel must have exactly one production call")

HOST.write_text(host, encoding="utf-8", newline="\n")
NIGHT_UI.write_text(night, encoding="utf-8", newline="\n")
