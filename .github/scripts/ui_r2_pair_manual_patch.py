from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

state_start = """    fun pairManualKey(option: ClocktowerDisplayOption): Pair<String?, List<Int>>? = when (val structured = option.proposition) {
"""
state_end = """    val dynamicDecisionFamily = when (step.action) {
"""
state_replacement = """    val manualPairCandidates = if (
        !automaticStorytellerInfo &&
        phase == ClocktowerPhase.FirstNight &&
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        step.manualInformationCandidates
    } else {
        emptyList()
    }
    var showManualPairSelection by remember(
        informationDecisionKey,
        step.roleEnName,
    ) { mutableStateOf(false) }
"""

manual_ui_start = """            if (!automaticStorytellerInfo && manualPairEntries.isNotEmpty()) {
"""
manual_ui_end = """            if (
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
"""
manual_ui_replacement = """            if (manualPairCandidates.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showManualPairSelection = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(if (language == "en") "Manually choose clue" else "手动选择展示信息")
                }
                if (showManualPairSelection) {
                    ClocktowerPairManualSelectionDialog(
                        interactionKey = informationDecisionKey,
                        candidates = manualPairCandidates,
                        seats = cards.mapIndexed { index, card ->
                            ClocktowerPairManualSeatUiModel(
                                seatId = "seat-${index + 1}",
                                seatNumber = index + 1,
                                label = card.name,
                            )
                        },
                        roleLabel = { roleId -> roleId },
                        onDismiss = { showManualPairSelection = false },
                        onConfirm = { manualOption ->
                            showRecommendedDisplayOption(manualOption)
                            showManualPairSelection = false
                        },
                    )
                }
            }

"""

reason_line = """                        RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
"""

for name, anchor in (
    ("state_start", state_start),
    ("state_end", state_end),
    ("manual_ui_start", manual_ui_start),
    ("manual_ui_end", manual_ui_end),
):
    count = text.count(anchor)
    if count != 1:
        raise SystemExit(f"Expected exactly one {name} anchor, found {count}")

reason_count = text.count(reason_line)
if reason_count != 2:
    raise SystemExit(f"Expected exactly two normal-product reason lines, found {reason_count}")

state_begin = text.index(state_start)
state_finish = text.index(state_end)
if state_begin >= state_finish:
    raise SystemExit("State anchors out of order")
text = text[:state_begin] + state_replacement + text[state_finish:]

manual_begin = text.index(manual_ui_start)
manual_finish = text.index(manual_ui_end)
if manual_begin >= manual_finish:
    raise SystemExit("Manual UI anchors out of order")
text = text[:manual_begin] + manual_ui_replacement + text[manual_finish:]

text = text.replace(reason_line, "")
TARGET.write_text(text, encoding="utf-8", newline="\n")
