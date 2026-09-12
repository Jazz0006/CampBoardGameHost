from pathlib import Path

files = {
    "rulings": Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerTemporaryAutomaticRulings.kt"),
    "night_ui": Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt"),
}

texts = {}
for key, path in files.items():
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")
    texts[key] = raw.decode("utf-8")

rulings_old = '''internal fun clocktowerTemporaryMayorSelection(
    mayorSeat: Int,
    livingTownsfolkSeats: List<Int>,
    decisionKey: String,
): TemporaryAutomaticSelection<Int> = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
    mayorSeat = mayorSeat,
    livingTownsfolkSeats = livingTownsfolkSeats.distinct().sorted(),
    decisionSeed = clocktowerTemporaryAutomaticDecisionSeed(decisionKey),
)

internal fun clocktowerTemporaryDemonSuccessorSelection(
'''
rulings_new = '''internal fun clocktowerTemporaryMayorSelection(
    mayorSeat: Int,
    livingTownsfolkSeats: List<Int>,
    decisionKey: String,
): TemporaryAutomaticSelection<Int> = TemporaryAutomaticStorytellerPolicy.selectMayorRedirect(
    mayorSeat = mayorSeat,
    livingTownsfolkSeats = livingTownsfolkSeats.distinct().sorted(),
    decisionSeed = clocktowerTemporaryAutomaticDecisionSeed(decisionKey),
)

internal fun clocktowerAutomaticMayorRulingShouldAdvance(
    automaticStorytellerInfo: Boolean,
    action: ClocktowerNightAction,
    selectedName: String?,
    automaticTargetName: String?,
): Boolean =
    automaticStorytellerInfo &&
        action == ClocktowerNightAction.MayorRedirect &&
        automaticTargetName != null &&
        selectedName == automaticTargetName

internal fun clocktowerTemporaryDemonSuccessorSelection(
'''

night_old = '''    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
        if (automaticStorytellerInfo && automaticDecisionTargetName != null && selectedName != automaticDecisionTargetName) {
            onSelectName(automaticDecisionTargetName)
        }
    }
    val beginnerGuidance = if (automaticStorytellerInfo) {
'''
night_new = '''    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
        if (automaticStorytellerInfo && automaticDecisionTargetName != null && selectedName != automaticDecisionTargetName) {
            onSelectName(automaticDecisionTargetName)
        }
    }
    LaunchedEffect(
        automaticStorytellerInfo,
        step.title,
        step.action,
        selectedName,
        automaticDecisionTargetName,
    ) {
        if (
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = automaticStorytellerInfo,
                action = step.action,
                selectedName = selectedName,
                automaticTargetName = automaticDecisionTargetName,
            )
        ) {
            onNext()
        }
    }
    val beginnerGuidance = if (automaticStorytellerInfo) {
'''

for key, old, new, label in [
    ("rulings", rulings_old, rulings_new, "Mayor auto-advance policy"),
    ("night_ui", night_old, night_new, "Mayor auto-advance wiring"),
]:
    text = texts[key]
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
    texts[key] = text.replace(old, new, 1)

if texts["rulings"].count("clocktowerAutomaticMayorRulingShouldAdvance(") != 1:
    raise SystemExit("Unexpected Mayor auto-advance policy count")
if texts["night_ui"].count("clocktowerAutomaticMayorRulingShouldAdvance(") != 1:
    raise SystemExit("Unexpected Mayor auto-advance wiring count")

for key, path in files.items():
    path.write_text(texts[key], encoding="utf-8", newline="\n")
