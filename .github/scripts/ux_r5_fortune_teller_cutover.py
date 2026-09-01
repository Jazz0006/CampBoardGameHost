from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
text = PATH.read_text()


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    text = text.replace(old, new, 1)


replace_once(
    "import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric\n",
    "import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric\n",
    "BooleanMetric import",
)

replace_once(
    "    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel\n",
    """    val structuredFortuneTellerActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredFortuneTellerSelectedSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
        .mapNotNull { selectedName ->
            cards.indexOfFirst { it.name == selectedName }
                .takeIf { it >= 0 }
                ?.plus(1)
        }
        .takeIf { seats -> seats.size == 2 && seats.distinct().size == 2 }
    val structuredFortuneTellerProposition = (step.displayProposition as? InformationProposition.BooleanResult)
        ?.takeIf { proposition ->
            proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT &&
                proposition.sourceSeat == structuredFortuneTellerActorSeat &&
                proposition.subjectSeats == structuredFortuneTellerSelectedSeats
        }
    val structuredFortuneTellerRecommendedOption = if (automaticStorytellerInfo) {
        automaticDisplayOption
    } else {
        displayedInformationOptions.firstOrNull { it.isDefaultRecommendation }
            ?: step.displayOptions.firstOrNull { it.isDefaultRecommendation }
            ?: automaticDisplayOption
    }
    val structuredFortuneTellerRecommendedValue =
        (structuredFortuneTellerRecommendedOption?.proposition as? InformationProposition.BooleanResult)
            ?.takeIf { proposition ->
                proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT &&
                    proposition.sourceSeat == structuredFortuneTellerActorSeat &&
                    proposition.subjectSeats == structuredFortuneTellerSelectedSeats
            }
            ?.value
    val structuredFortuneTellerUiModel = if (
        step.action == ClocktowerNightAction.FortuneTeller &&
        step.roleEnName == "Fortune Teller" &&
        step.informationReliability != InformationReliability.RELIABLE &&
        structuredFortuneTellerActorSeat != null &&
        structuredFortuneTellerSelectedSeats != null &&
        structuredFortuneTellerProposition != null
    ) {
        prepareBooleanInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredFortuneTellerActorSeat,
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = structuredFortuneTellerSelectedSeats,
            trueValue = structuredFortuneTellerProposition.value,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredFortuneTellerRecommendedValue,
        )
    } else {
        null
    }
    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel
""",
    "structured Fortune Teller model",
)

replace_once(
    """            }

            pairRecommendationPresentation?.let { presentation ->
""",
    """            }

            if (showFortuneTellerDisplayOptions) {
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

            pairRecommendationPresentation?.let { presentation ->
""",
    "structured Fortune Teller panel",
)

replace_once(
    """                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
""",
    """                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
""",
    "recommended information suppression",
)

replace_once(
    """            if (
                structuredNumberUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
""",
    """            if (
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
""",
    "legacy display option suppression",
)

PATH.write_text(text)
