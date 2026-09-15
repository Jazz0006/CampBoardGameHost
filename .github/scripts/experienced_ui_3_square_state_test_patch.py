from pathlib import Path

path = Path('app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatLayoutTest.kt')
text = path.read_text(encoding='utf-8')
old = '''                ClocktowerSquareTableSeatState.SelectedHighlighted,
                ClocktowerSquareTableSeatState.HighlightedInformation,
                ClocktowerSquareTableSeatState.Disabled,
'''
new = '''                ClocktowerSquareTableSeatState.SelectedHighlighted,
                ClocktowerSquareTableSeatState.HighlightedInformation,
                ClocktowerSquareTableSeatState.RegistrationHint,
                ClocktowerSquareTableSeatState.Disabled,
'''
if text.count(old) != 1:
    raise SystemExit(f'square-table state contract anchor count = {text.count(old)}')
path.write_text(text.replace(old, new, 1), encoding='utf-8')
print('Square-table RegistrationHint state contract patched successfully')
