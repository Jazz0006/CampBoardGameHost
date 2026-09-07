from pathlib import Path

path = Path('.github/scripts/ps4_3b_recovery_v2_patch.py')
text = path.read_text(encoding='utf-8')
old = '''    if end >= len(text) or text[end] != ',':
        raise SystemExit(f'{path}: expected trailing comma after LegacyRestoreCompatibility call')
    end += 1
    if end < len(text) and text[end] == '\\r':
        end += 1
'''
new = '''    if end < len(text) and text[end] == ',':
        end += 1
    elif end >= len(text) or text[end] not in '\\r\\n':
        raise SystemExit(f'{path}: expected comma or statement newline after LegacyRestoreCompatibility call')
    if end < len(text) and text[end] == '\\r':
        end += 1
'''
if text.count(old) != 1:
    raise SystemExit('PS4.3b parser-fix anchor drifted')
path.write_text(text.replace(old, new, 1), encoding='utf-8')
