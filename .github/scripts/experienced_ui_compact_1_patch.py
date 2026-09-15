from pathlib import Path

TARGET = Path(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
)

raw = TARGET.read_bytes()
if b"\r\n" in raw:
    raise SystemExit("Unexpected CRLF source; refusing to normalize large file")
if b"\r" in raw:
    raise SystemExit("Unexpected CR/mixed line endings")

text = raw.decode("utf-8")

replacements = [
    (
        """        ClocktowerNewDemonConfirmationScreen(
            newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
            hasNewDemon = newDemon != null,
            onHostTools = onHostTools,
""",
        """        ClocktowerNewDemonConfirmationScreen(
            newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
            hasNewDemon = newDemon != null,
            compact = !automaticStorytellerInfo,
            onHostTools = onHostTools,
""",
    ),
    (
        """        ClocktowerSlayerTableScreen(
            round = round,
            tableState = slayerTableState,
            actionsEnabled = gameOutcome == null,
""",
        """        ClocktowerSlayerTableScreen(
            round = round,
            tableState = slayerTableState,
            actionsEnabled = gameOutcome == null,
            compact = !automaticStorytellerInfo,
""",
    ),
    (
        """        ClocktowerPendingNominationTableScreen(
            round = round,
            cards = cards,
""",
        """        ClocktowerPendingNominationTableScreen(
            round = round,
            compact = !automaticStorytellerInfo,
            cards = cards,
""",
    ),
]

for index, (old, new) in enumerate(replacements, start=1):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Patch anchor {index} expected once, found {count}")
    text = text.replace(old, new, 1)

TARGET.write_text(text, encoding="utf-8", newline="\n")
print(f"Patched {TARGET} with {len(replacements)} exact replacements")
