from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in ClocktowerNightStepUi.kt")
text = raw.decode("utf-8")

old = '''    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
        if (automaticStorytellerInfo && automaticDecisionTargetName != null && selectedName != automaticDecisionTargetName) {
            onSelectName(automaticDecisionTargetName)
        }
    }
    val command = when {
'''
new = '''    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
        if (automaticStorytellerInfo && automaticDecisionTargetName != null && selectedName != automaticDecisionTargetName) {
            onSelectName(automaticDecisionTargetName)
        }
    }
    LaunchedEffect(
        automaticStorytellerInfo,
        step.action,
        step.isRealAction,
        selectedName,
    ) {
        if (
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = automaticStorytellerInfo,
                isRedHerringStep = step.action == ClocktowerNightAction.RedHerring,
                isRealAction = step.isRealAction,
                hasSelectedRedHerring = selectedName != null,
            )
        ) {
            onNext()
        }
    }
    val command = when {
'''

count = text.count(old)
if count != 1:
    raise SystemExit(f"Red Herring auto-advance anchor: expected 1, found {count}")
text = text.replace(old, new)

required = (
    "shouldAutoAdvanceRedHerring(",
    "isRedHerringStep = step.action == ClocktowerNightAction.RedHerring",
    "isRealAction = step.isRealAction",
    "hasSelectedRedHerring = selectedName != null",
    "onNext()",
)
for token in required:
    if token not in text:
        raise SystemExit(f"Missing Red Herring auto-advance token: {token}")
if text.count("shouldAutoAdvanceRedHerring(") != 1:
    raise SystemExit("Expected exactly one Red Herring auto-advance call in night step UI")

TARGET.write_text(text, encoding="utf-8", newline="\n")
