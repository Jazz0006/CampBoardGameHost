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

host_old = '''                                displayOptions = { actor -> recommendedNumberOptions(text("厨师信息", "Chef information"), actor, chefReferenceValue, chefMaximumValue, text("邪恶玩家相邻对数", "Adjacent evil pairs"), pressureCostPerPoint = 1, propositionForValue = { value -> InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indexOf(actor) + 1, cards.indices.map { it + 1 }, value) }) },
'''
host_new = '''                                displayProposition = chefActor?.let { actor ->
                                    InformationProposition.NumericResult(
                                        NumericMetric.ADJACENT_EVIL_PAIRS,
                                        cards.indexOf(actor) + 1,
                                        cards.indices.map { it + 1 },
                                        chefReferenceValue,
                                    )
                                },
                                numericMinimumValue = 0,
                                numericMaximumValue = chefMaximumValue,
                                displayOptions = { actor -> recommendedNumberOptions(text("厨师信息", "Chef information"), actor, chefReferenceValue, chefMaximumValue, text("邪恶玩家相邻对数", "Adjacent evil pairs"), pressureCostPerPoint = 1, propositionForValue = { value -> InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indexOf(actor) + 1, cards.indices.map { it + 1 }, value) }) },
'''

night_insert_old = '''            pressureCostPerPoint = 1,
        )
    } else {
        null
    }

    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
'''
night_insert_new = '''            pressureCostPerPoint = 1,
        )
    } else {
        null
    }
    val structuredChefActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredChefProposition = (step.displayProposition as? InformationProposition.NumericResult)
        ?.takeIf { it.metric == NumericMetric.ADJACENT_EVIL_PAIRS }
    val structuredChefUiModel = if (
        step.roleEnName == "Chef" &&
        step.informationReliability != InformationReliability.RELIABLE &&
        step.spyRegistrationKey == null &&
        step.recluseRegistrationKey == null &&
        structuredChefActorSeat != null &&
        structuredChefProposition != null &&
        step.numericMinimumValue != null &&
        step.numericMaximumValue != null
    ) {
        prepareNumericInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredChefActorSeat,
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = structuredChefProposition.subjectSeats,
            trueValue = structuredChefProposition.value,
            minimumValue = step.numericMinimumValue,
            maximumValue = step.numericMaximumValue,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredEmpathRecommendedValue,
            previousShownValue = step.previousShownNumber,
            pressureCostPerPoint = 1,
        )
    } else {
        null
    }
    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel

    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
'''

panel_call_old = '''                StructuredNumberInformationDecisionPanel(
                    model = model,
                    currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    onConfirmed = { confirmed, value ->
'''
panel_call_new = '''                StructuredNumberInformationDecisionPanel(
                    model = model,
                    currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    roleLabel = step.title,
                    onConfirmed = { confirmed, value ->
'''

require_count(host, host_old, 1, "Chef Host wiring")
require_count(night, night_insert_old, 1, "Chef structured-model insertion")
require_count(night, "            structuredEmpathUiModel?.let { model ->", 1, "numeric panel model")
require_count(night, panel_call_old, 1, "numeric panel call")
require_count(night, "informationDecisionExpectedSnapshot = structuredEmpathUiModel.contextSnapshot", 1, "numeric snapshot authority")
require_count(night, "structuredEmpathUiModel == null", 3, "legacy numeric fallback guards")

host = host.replace(host_old, host_new, 1)
night = night.replace(night_insert_old, night_insert_new, 1)
night = night.replace(
    "            structuredEmpathUiModel?.let { model ->",
    "            structuredNumberUiModel?.let { model ->",
    1,
)
night = night.replace(panel_call_old, panel_call_new, 1)
night = night.replace(
    "informationDecisionExpectedSnapshot = structuredEmpathUiModel.contextSnapshot",
    "informationDecisionExpectedSnapshot = structuredNumberUiModel.contextSnapshot",
    1,
)
night = night.replace("structuredEmpathUiModel == null", "structuredNumberUiModel == null")

required_host = [
    "numericMaximumValue = chefMaximumValue",
    "displayProposition = chefActor?.let { actor ->",
]
required_night = [
    "val structuredChefUiModel = if (",
    "val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel",
    "roleLabel = step.title",
    "informationDecisionExpectedSnapshot = structuredNumberUiModel.contextSnapshot",
]
for token in required_host:
    if token not in host:
        raise SystemExit(f"Missing Host postcondition: {token}")
for token in required_night:
    if token not in night:
        raise SystemExit(f"Missing Night UI postcondition: {token}")
if "structuredEmpathUiModel == null" in night:
    raise SystemExit("Legacy numeric fallback guards still reference Empath-only model")

HOST.write_text(host, encoding="utf-8", newline="\n")
NIGHT_UI.write_text(night, encoding="utf-8", newline="\n")
