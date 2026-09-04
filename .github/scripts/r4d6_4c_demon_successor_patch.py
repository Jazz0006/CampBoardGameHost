from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit source normalization")

text = raw.decode("utf-8")

old = '''            ClocktowerNightAction.DemonSuccessor -> {
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "Choose the new Imp" else "选择新小恶魔",
                    helper = step.explanation,
                ) {
                    SelectablePlayerChips(
                        cards = demonSuccessorTargetCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
                }
            }
'''

new = '''            ClocktowerNightAction.DemonSuccessor -> {
                if (!automaticStorytellerInfo) {
                    ClocktowerSingleTargetSquareTableDialog(
                        seats = nightActionSeats,
                        selectedSeat = seatNumberForName(selectedName),
                        selectableSeats = selectableSeatNumbers(demonSuccessorTargetCards),
                        enabled = step.isRealAction,
                        title = if (language == "en") "Choose the new Imp" else "选择新小恶魔",
                        helper = step.explanation,
                        language = language,
                        canGoPrevious = canGoPrevious,
                        onSeatSelected = { seatNumber ->
                            cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                        },
                        onPrevious = onPrevious,
                        onNext = onNext,
                    )
                }
            }
'''

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one DemonSuccessor UI patch anchor, found {count}")

text = text.replace(old, new, 1)

if old in text:
    raise SystemExit("Original DemonSuccessor UI anchor remains after replacement")

required = [
    "ClocktowerNightAction.DemonSuccessor -> {",
    "selectableSeats = selectableSeatNumbers(demonSuccessorTargetCards)",
    "selectedSeat = seatNumberForName(selectedName)",
    "ClocktowerSingleTargetSquareTableDialog(",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required post-patch token: {token}")

path.write_text(text, encoding="utf-8", newline="\n")
