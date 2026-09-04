from pathlib import Path


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")
    return raw.decode("utf-8")


def replace_exact(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


def write_lf(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


night_action = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt")
text = read_lf(night_action)
text = replace_exact(
    text,
    """internal fun clocktowerTwoTargetSeatState(
""",
    """internal data class ClocktowerNightActionSeatPresentation(
    val targetState: ClocktowerSquareTableSeatState,
    val isCurrentActor: Boolean,
)

internal fun clocktowerSingleTargetSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = clocktowerSingleTargetSeatState(
        seatNumber = seatNumber,
        selectedSeat = selectedSeat,
        selectableSeats = selectableSeats,
    ),
    isCurrentActor = seatNumber == actorSeat,
)

internal fun clocktowerTwoTargetSeatState(
""",
    "night action presentation contract",
)
text = replace_exact(
    text,
    """    else -> ClocktowerSquareTableSeatState.Disabled
}

@Composable
internal fun ClocktowerSingleTargetSquareTableDialog(
""",
    """    else -> ClocktowerSquareTableSeatState.Disabled
}

internal fun clocktowerTwoTargetSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = clocktowerTwoTargetSeatState(
        seatNumber = seatNumber,
        selectedSeats = selectedSeats,
        selectableSeats = selectableSeats,
    ),
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerNightActionWakeInstruction(instruction: String?) {
    instruction?.takeIf { it.isNotBlank() }?.let { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
internal fun ClocktowerSingleTargetSquareTableDialog(
""",
    "two-target presentation and wake instruction",
)
text = replace_exact(
    text,
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    title: String,
""",
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    title: String,
""",
    "single-target dialog parameters",
)
text = replace_exact(
    text,
    """        seatState = { seatNumber ->
            clocktowerSingleTargetSeatState(
                seatNumber = seatNumber,
                selectedSeat = selectedSeat,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
""",
    """        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeat = selectedSeat,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
""",
    "single-target seat presentation",
)
text = replace_exact(
    text,
    """        ) {
            Text(
                text = title,
""",
    """        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            Text(
                text = title,
""",
    "single-target center wake instruction",
)
text = replace_exact(
    text,
    """    language: String,
    seatState: (Int) -> ClocktowerSquareTableSeatState,
    onSeatSelected: (Int) -> Unit,
""",
    """    language: String,
    seatPresentation: (Int) -> ClocktowerNightActionSeatPresentation,
    onSeatSelected: (Int) -> Unit,
""",
    "generic dialog presentation parameter",
)
text = replace_exact(
    text,
    """                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = seatState(seat.seatId.number),
                    )
                },
""",
    """                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    val presentation = seatPresentation(seat.seatId.number)
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = presentation.targetState,
                        isCurrentActor = presentation.isCurrentActor,
                    )
                },
""",
    "generic dialog seat model",
)
assert "clocktowerSingleTargetSeatPresentation" in text
assert "ClocktowerNightActionWakeInstruction(wakeInstruction)" in text
assert "isCurrentActor = presentation.isCurrentActor" in text
write_lf(night_action, text)


square_table = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt")
text = read_lf(square_table)
text = replace_exact(
    text,
    """    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,
    val isInteractionEnabled: Boolean = state in setOf(
""",
    """    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,
    val isCurrentActor: Boolean = false,
    val isInteractionEnabled: Boolean = state in setOf(
""",
    "square-table actor flag",
)
text = replace_exact(
    text,
    """        border = BorderStroke(palette.borderWidth, palette.border),
        tonalElevation = if (canSelect && seat.state != ClocktowerSquareTableSeatState.Neutral) 2.dp else 0.dp,
""",
    """        border = if (seat.isCurrentActor) {
            BorderStroke(4.dp, MaterialTheme.colorScheme.tertiary)
        } else {
            BorderStroke(palette.borderWidth, palette.border)
        },
        tonalElevation = when {
            seat.isCurrentActor -> 4.dp
            canSelect && seat.state != ClocktowerSquareTableSeatState.Neutral -> 2.dp
            else -> 0.dp
        },
""",
    "square-table actor border",
)
text = replace_exact(
    text,
    """            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
""",
    """            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (seat.isCurrentActor) {
                    Text(
                        text = "➤",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 2.dp),
                    )
                }
                clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
""",
    "square-table actor arrow",
)
text = replace_exact(
    text,
    """                fontWeight = if (seat.state in setOf(
""",
    """                fontWeight = if (seat.isCurrentActor || seat.state in setOf(
""",
    "square-table actor label emphasis",
)
assert 'text = "➤"' in text
assert "val isCurrentActor: Boolean = false" in text
write_lf(square_table, text)


fortune = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt")
text = read_lf(fortune)
text = replace_exact(
    text,
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    legalResults: Set<Boolean>,
""",
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    legalResults: Set<Boolean>,
""",
    "fortune dialog actor parameters",
)
text = replace_exact(
    text,
    """                        state = clocktowerFortuneTellerSeatState(
                            seatNumber = seat.seatId.number,
                            selectedSeats = selectedSeats,
                            selectableSeats = if (enabled) selectableSeats else emptySet(),
                        ),
                    )
""",
    """                        state = clocktowerFortuneTellerSeatState(
                            seatNumber = seat.seatId.number,
                            selectedSeats = selectedSeats,
                            selectableSeats = if (enabled) selectableSeats else emptySet(),
                        ),
                        isCurrentActor = seat.seatId.number == actorSeat,
                    )
""",
    "fortune actor seat model",
)
text = replace_exact(
    text,
    """                ClocktowerFortuneTellerCenterControls(
                    selectedSeats = selectedSeats,
""",
    """                ClocktowerFortuneTellerCenterControls(
                    wakeInstruction = wakeInstruction,
                    selectedSeats = selectedSeats,
""",
    "fortune center wake wiring",
)
text = replace_exact(
    text,
    """private fun ClocktowerFortuneTellerCenterControls(
    selectedSeats: List<Int>,
""",
    """private fun ClocktowerFortuneTellerCenterControls(
    wakeInstruction: String?,
    selectedSeats: List<Int>,
""",
    "fortune center parameter",
)
text = replace_exact(
    text,
    """    ) {
        Text(
            text = if (language == "en") "Fortune Teller" else "占卜师",
""",
    """    ) {
        ClocktowerNightActionWakeInstruction(wakeInstruction)
        Text(
            text = if (language == "en") "Fortune Teller" else "占卜师",
""",
    "fortune center wake instruction",
)
assert "isCurrentActor = seat.seatId.number == actorSeat" in text
write_lf(fortune, text)


chambermaid = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidSquareTableUi.kt")
text = read_lf(chambermaid)
text = replace_exact(
    text,
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    resultOptions: List<ClocktowerDisplayOption>,
""",
    """    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    resultOptions: List<ClocktowerDisplayOption>,
""",
    "chambermaid dialog actor parameters",
)
text = replace_exact(
    text,
    """        seatState = { seatNumber ->
            clocktowerTwoTargetSeatState(
                seatNumber = seatNumber,
                selectedSeats = selectedSeats,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
""",
    """        seatPresentation = { seatNumber ->
            clocktowerTwoTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeats = selectedSeats,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
""",
    "chambermaid seat presentation",
)
text = replace_exact(
    text,
    """        ClocktowerChambermaidCenterControls(
            selectedSeats = selectedSeats,
""",
    """        ClocktowerChambermaidCenterControls(
            wakeInstruction = wakeInstruction,
            selectedSeats = selectedSeats,
""",
    "chambermaid center wake wiring",
)
text = replace_exact(
    text,
    """private fun ClocktowerChambermaidCenterControls(
    selectedSeats: List<Int>,
""",
    """private fun ClocktowerChambermaidCenterControls(
    wakeInstruction: String?,
    selectedSeats: List<Int>,
""",
    "chambermaid center parameter",
)
text = replace_exact(
    text,
    """    ) {
        Text(
            text = if (language == "en") "Chambermaid" else "侍女",
""",
    """    ) {
        ClocktowerNightActionWakeInstruction(wakeInstruction)
        Text(
            text = if (language == "en") "Chambermaid" else "侍女",
""",
    "chambermaid center wake instruction",
)
assert "clocktowerTwoTargetSeatPresentation" in text
write_lf(chambermaid, text)


night_step = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
text = read_lf(night_step)
text = replace_exact(
    text,
    """        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
            if (language == "en") {
                "Ask ${step.actor.seatLabel(cards)} to choose two players to check"
            } else {
                "让 ${step.actor.seatLabel(cards)} 选择两名想要查验的玩家"
            }
        }
""",
    """        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
            if (language == "en") {
                "Wake ${step.actor.seatLabel(cards)} and ask them to choose two players to check"
            } else {
                "唤醒 ${step.actor.seatLabel(cards)}，让他选择两名想要查验的玩家"
            }
        }
""",
    "fortune wake command",
)
text = replace_exact(
    text,
    """        fun selectableSeatNumbers(candidates: List<PlayerCard>): Set<Int> = candidates
            .mapNotNull { candidate -> seatNumberForName(candidate.name) }
            .toSet()

        when (step.action) {
""",
    """        fun selectableSeatNumbers(candidates: List<PlayerCard>): Set<Int> = candidates
            .mapNotNull { candidate -> seatNumberForName(candidate.name) }
            .toSet()
        val actionActorSeat = seatNumberForName(step.actor?.name)

        when (step.action) {
""",
    "night-step actor seat projection",
)
for label, old, new in [
    (
        "poison inline wake",
        """                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_poison_target),
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    title = stringResource(R.string.clocktower_host_choose_poison_target),
""",
    ),
    (
        "butler inline wake",
        """                    enabled = step.isRealAction,
                    title = if (language == "en") "Choose the Butler's master" else "选择管家的主人",
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    title = if (language == "en") "Choose the Butler's master" else "选择管家的主人",
""",
    ),
    (
        "monk inline wake",
        """                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
""",
    ),
    (
        "fortune inline wake",
        """                    enabled = step.isRealAction,
                    legalResults = fortuneTellerLegalResults,
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    legalResults = fortuneTellerLegalResults,
""",
    ),
    (
        "chambermaid inline wake",
        """                    enabled = step.isRealAction,
                    resultOptions = resultOptions,
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    resultOptions = resultOptions,
""",
    ),
    (
        "demon inline wake",
        """                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_night_death),
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    title = stringResource(R.string.clocktower_host_choose_night_death),
""",
    ),
    (
        "ravenkeeper inline wake",
        """                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
""",
        """                    enabled = step.isRealAction,
                    actorSeat = actionActorSeat,
                    wakeInstruction = command,
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
""",
    ),
]:
    text = replace_exact(text, old, new, label)

for required in [
    "val actionActorSeat = seatNumberForName(step.actor?.name)",
    "actorSeat = actionActorSeat",
    "wakeInstruction = command",
    "唤醒 ${step.actor.seatLabel(cards)}，让他选择两名想要查验的玩家",
]:
    assert required in text
write_lf(night_step, text)
