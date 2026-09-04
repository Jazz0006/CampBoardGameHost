from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatDensityPolicy.kt")
text = path.read_text(encoding="utf-8")
old = '    require(playerCount > 0) { "Square-table seat density requires at least one player" }\n'
new = '    require(playerCount >= 0) { "Square-table player count cannot be negative" }\n'
if text.count(old) != 1:
    raise SystemExit("Expected generated positive-player density guard exactly once")
path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
