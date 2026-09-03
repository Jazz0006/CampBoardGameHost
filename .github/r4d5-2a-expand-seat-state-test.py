from pathlib import Path

path = Path("app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatLayoutTest.kt")
text = path.read_text()
old = '''    @Test\n    fun `surface exposes the six product seat states`() {\n        assertEquals(\n            setOf(\n                ClocktowerSquareTableSeatState.Neutral,\n                ClocktowerSquareTableSeatState.Selectable,\n                ClocktowerSquareTableSeatState.SelectedFirst,\n                ClocktowerSquareTableSeatState.SelectedSecond,\n                ClocktowerSquareTableSeatState.HighlightedInformation,\n                ClocktowerSquareTableSeatState.Disabled,\n            ),\n            ClocktowerSquareTableSeatState.values().toSet(),\n        )\n    }\n'''
new = '''    @Test\n    fun `surface exposes ordered and arbitrary multi-selection seat states`() {\n        assertEquals(\n            setOf(\n                ClocktowerSquareTableSeatState.Neutral,\n                ClocktowerSquareTableSeatState.Selectable,\n                ClocktowerSquareTableSeatState.SelectedFirst,\n                ClocktowerSquareTableSeatState.SelectedSecond,\n                ClocktowerSquareTableSeatState.Selected,\n                ClocktowerSquareTableSeatState.SelectedHighlighted,\n                ClocktowerSquareTableSeatState.HighlightedInformation,\n                ClocktowerSquareTableSeatState.Disabled,\n            ),\n            ClocktowerSquareTableSeatState.values().toSet(),\n        )\n    }\n'''
if text.count(old) != 1:
    raise SystemExit("Expected exactly one legacy six-state contract")
path.write_text(text.replace(old, new, 1))
