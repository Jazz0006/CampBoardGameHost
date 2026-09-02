from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

old = """        playerDisplayStep?.let { displayStep ->
            ClocktowerPlayerDisplayCardLocalized(
                step = displayStep,
                onDismiss = { playerDisplayStep = null },
            )
            return
        }
"""
new = """        playerDisplayStep?.let { displayStep ->
            ClocktowerPlayerDisplayCardLocalized(
                step = displayStep,
                cards = cards,
                onDismiss = { playerDisplayStep = null },
            )
            return
        }
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one player-display wiring anchor, found {count}")

TARGET.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
