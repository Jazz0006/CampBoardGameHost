from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
NIGHT = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")
    return raw.decode("utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


host = read_lf(HOST)
night = read_lf(NIGHT)

host = replace_once(
    host,
    '''                                action = ClocktowerNightAction.FortuneTeller,
                                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
''',
    '''                                action = ClocktowerNightAction.FortuneTeller,
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
''',
    "first-night Fortune Teller typed proposition",
)

host = replace_once(
    host,
    '''                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
''',
    '''                action = ClocktowerNightAction.FortuneTeller,
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
''',
    "other-night Fortune Teller typed proposition",
)

night = replace_once(
    night,
    "import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric\n",
    "import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition\n"
    "import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric\n",
    "BooleanMetric import",
)

night = replace_once(
    night,
    "    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel\n",
    '''    val structuredFortuneTellerActorSeat = step.actor
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
''',
    "structured Fortune Teller model",
)

night = replace_once(
    night,
    '''            }

            pairRecommendationPresentation?.let { presentation ->
''',
    '''            }

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
''',
    "structured Fortune Teller panel",
)

night = replace_once(
    night,
    '''                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
''',
    '''                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
''',
    "recommended information fallback suppression",
)

night = replace_once(
    night,
    '''            if (
                structuredNumberUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
''',
    '''            if (
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
''',
    "legacy display-option fallback suppression",
)

if host.count('displayProposition = fortuneTellerMatched?.let { matched ->') != 2:
    raise SystemExit("Fortune Teller typed truth must be wired in exactly two night materializers")
if night.count('StructuredBooleanInformationDecisionPanel(') != 1:
    raise SystemExit("Structured Fortune Teller panel must have exactly one production call")
if night.count('findBooleanDisplayOption(') != 1:
    raise SystemExit("Fortune Teller confirmation must map through exactly one typed option seam")

HOST.write_text(host, encoding="utf-8", newline="\n")
NIGHT.write_text(night, encoding="utf-8", newline="\n")
