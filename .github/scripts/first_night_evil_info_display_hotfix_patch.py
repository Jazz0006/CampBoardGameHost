from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

old = 'actor?.clocktowerRole?.enName == "Drunk" || actorIsUnreliable(displayStep.roleEnName.orEmpty(), actor)'
new = 'clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)'

count = text.count(old)
if count != 2:
    raise SystemExit(f"Expected exactly two display reliability anchors, found {count}")

text = text.replace(old, new)

if old in text:
    raise SystemExit("Original unsafe display reliability anchor remains after replacement")
if text.count(new) != 2:
    raise SystemExit("Expected exactly two guarded display reliability calls after replacement")

path.write_text(text, encoding="utf-8", newline="\n")
