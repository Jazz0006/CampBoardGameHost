from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

old_projection = """    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions
    fun pairManualKey(option: ClocktowerDisplayOption): Pair<String?, List<Int>>? = when (val structured = option.proposition) {
"""
new_projection = """    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions
    val pairRecommendationPresentation = if (
        !automaticStorytellerInfo &&
        phase == ClocktowerPhase.FirstNight &&
        step.roleEnName in setOf(\"Washerwoman\", \"Librarian\", \"Investigator\")
    ) {
        clocktowerRecommendationPresentation(displayedInformationOptions)
    } else {
        null
    }
    fun pairManualKey(option: ClocktowerDisplayOption): Pair<String?, List<Int>>? = when (val structured = option.proposition) {
"""

old_surface = """            if (
                structuredEmpathUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
                (step.action != ClocktowerNightAction.FortuneTeller || showFortuneTellerDisplayOptions)
            ) {
"""
new_surface = """            pairRecommendationPresentation?.let { presentation ->
                ClocktowerPairRecommendationPresentationSection(
                    presentation = presentation,
                    language = language,
                    onSelect = ::showRecommendedDisplayOption,
                )
            }

            if (
                pairRecommendationPresentation == null &&
                structuredEmpathUiModel == null &&
                displayedInformationOptions.isNotEmpty() &&
                (step.action != ClocktowerNightAction.FortuneTeller || showFortuneTellerDisplayOptions)
            ) {
"""

anchors = {
    "projection": (old_projection, new_projection),
    "surface": (old_surface, new_surface),
}

for name, (old, _) in anchors.items():
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {name} anchor, found {count}")

for old, new in anchors.values():
    text = text.replace(old, new, 1)

for name, (old, _) in anchors.items():
    if old in text:
        raise SystemExit(f"Original {name} anchor remains after replacement")

required_tokens = [
    "val pairRecommendationPresentation = if (",
    "clocktowerRecommendationPresentation(displayedInformationOptions)",
    "ClocktowerPairRecommendationPresentationSection(",
    "pairRecommendationPresentation == null &&",
    "if (!automaticStorytellerInfo && manualPairEntries.isNotEmpty()) {",
    "onClick = { showRecommendedDisplayOption(option) }",
]
for token in required_tokens:
    if token not in text:
        raise SystemExit(f"Required semantic token missing after patch: {token}")

if text.count("ClocktowerPairRecommendationPresentationSection(") != 1:
    raise SystemExit("Expected exactly one pair recommendation presentation call")
if text.count("if (!automaticStorytellerInfo && manualPairEntries.isNotEmpty()) {") != 1:
    raise SystemExit("Manual pair surface anchor changed unexpectedly")

path.write_text(text, encoding="utf-8", newline="\n")
