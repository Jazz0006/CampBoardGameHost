from pathlib import Path

NIGHT_UI = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")


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


night = read_lf(NIGHT_UI)

old_recommendation_mapping = '''    fun booleanOptionValue(option: ClocktowerDisplayOption?): Boolean? =
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
'''
new_recommendation_mapping = '''    fun booleanOptionValue(option: ClocktowerDisplayOption?): Boolean? =
        (option?.proposition as? InformationProposition.BooleanResult)?.value
    val structuredFortuneTellerActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredFortuneTellerProposition = (step.displayProposition as? InformationProposition.BooleanResult)
        ?.takeIf { it.metric == com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT }
    val structuredFortuneTellerTruthValue = structuredFortuneTellerProposition?.value
    val structuredFortuneTellerRecommendedCandidate = if (automaticStorytellerInfo) {
        automaticDisplayOption
    } else {
        displayedInformationOptions.firstOrNull { it.isDefaultRecommendation }
            ?: step.displayOptions.firstOrNull { it.isDefaultRecommendation }
            ?: automaticDisplayOption
    }
    val structuredFortuneTellerRecommendedCandidateValue =
        booleanOptionValue(structuredFortuneTellerRecommendedCandidate)
    val structuredFortuneTellerRecommendedOption = structuredFortuneTellerProposition?.let { proposition ->
        structuredFortuneTellerRecommendedCandidateValue?.let { recommendedValue ->
            findBooleanDisplayOption(
                options = listOfNotNull(structuredFortuneTellerRecommendedCandidate),
                metric = proposition.metric,
                sourceSeat = proposition.sourceSeat,
                subjectSeats = proposition.subjectSeats,
                value = recommendedValue,
            )
        }
    }
    val structuredFortuneTellerRecommendedValue = booleanOptionValue(structuredFortuneTellerRecommendedOption)
'''

night = replace_once(
    night,
    old_recommendation_mapping,
    new_recommendation_mapping,
    "typed Fortune Teller recommendation mapping",
)
night = replace_once(
    night,
    "truthful = value == structuredFortuneTellerProposition.value,",
    "truthful = value == structuredFortuneTellerTruthValue,",
    "selection audit truthful comparison",
)
night = replace_once(
    night,
    "selectedInformationTruthful = value == structuredFortuneTellerProposition.value,",
    "selectedInformationTruthful = value == structuredFortuneTellerTruthValue,",
    "display truthful comparison",
)

required = [
    "val structuredFortuneTellerTruthValue = structuredFortuneTellerProposition?.value",
    "findBooleanDisplayOption(",
    "sourceSeat = proposition.sourceSeat",
    "subjectSeats = proposition.subjectSeats",
    "truthful = value == structuredFortuneTellerTruthValue",
    "selectedInformationTruthful = value == structuredFortuneTellerTruthValue",
]
for token in required:
    if token not in night:
        raise SystemExit(f"Missing Fortune Teller fix postcondition: {token}")

if night.count("findBooleanDisplayOption(") != 1:
    raise SystemExit("Fortune Teller production mapping must call findBooleanDisplayOption exactly once")
if "structuredFortuneTellerProposition.value" in night:
    raise SystemExit("Nullable Fortune Teller proposition value access remains after fix")

NIGHT_UI.write_text(night, encoding="utf-8", newline="\n")
