from __future__ import annotations

import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE_SCRIPT = ROOT / ".github/scripts/ux_mode_1_legacy_night_ui_retirement_patch.py"

spec = importlib.util.spec_from_file_location("legacy_retirement_patch", BASE_SCRIPT)
if spec is None or spec.loader is None:
    raise SystemExit("failed to load base patch script")
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

original_replace_once = module.replace_once
PAIR_FILE = "app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt"
AMBIGUOUS_SIGNATURE = '''    roleLabel: (String) -> String,\n    language: String,\n'''


def replace_once(rel: str, old: str, new: str) -> None:
    if rel == PAIR_FILE and old == AMBIGUOUS_SIGNATURE:
        path = ROOT / rel
        text = path.read_text(encoding="utf-8")
        count = text.count(old)
        if count == 2:
            path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
            return
    original_replace_once(rel, old, new)


module.replace_once = replace_once
module.main()
