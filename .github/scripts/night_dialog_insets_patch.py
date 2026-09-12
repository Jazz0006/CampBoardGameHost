from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = ROOT / "app/src/main/java/com/codex/campboardgamehost"
ACTION_FILE = SOURCE_ROOT / "ClocktowerNightActionSquareTableUi.kt"
EXPECTED_FILES_OUTPUT = Path("/tmp/night-dialog-inset-files.txt")

OLD_DIALOG = "DialogProperties(usePlatformDefaultWidth = false)"


def replace_dialog_properties(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    if OLD_DIALOG not in text:
        return False

    updated = text
    while OLD_DIALOG in updated:
        index = updated.index(OLD_DIALOG)
        line_start = updated.rfind("\n", 0, index) + 1
        column = index - line_start
        indent = " " * column
        replacement = (
            "DialogProperties(\n"
            f"{indent}    usePlatformDefaultWidth = false,\n"
            f"{indent}    decorFitsSystemWindows = false,\n"
            f"{indent})"
        )
        updated = updated[:index] + replacement + updated[index + len(OLD_DIALOG):]

    path.write_text(updated, encoding="utf-8")
    return True


def patch_night_bottom_bar() -> None:
    text = ACTION_FILE.read_text(encoding="utf-8")

    import_anchor = "import androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\n"
    import_replacement = (
        "import androidx.compose.foundation.layout.ExperimentalLayoutApi\n"
        "import androidx.compose.foundation.layout.Row\n"
        "import androidx.compose.foundation.layout.Spacer\n"
        "import androidx.compose.foundation.layout.WindowInsets\n"
        "import androidx.compose.foundation.layout.WindowInsetsSides\n"
        "import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility\n"
        "import androidx.compose.foundation.layout.only\n"
        "import androidx.compose.foundation.layout.windowInsetsPadding\n"
    )
    if text.count(import_anchor) != 1:
        raise SystemExit(f"expected one import anchor, found {text.count(import_anchor)}")
    text = text.replace(import_anchor, import_replacement, 1)

    function_anchor = "@Composable\ninternal fun ClocktowerNightBottomActionBar("
    function_replacement = "@OptIn(ExperimentalLayoutApi::class)\n@Composable\ninternal fun ClocktowerNightBottomActionBar("
    if text.count(function_anchor) != 1:
        raise SystemExit(f"expected one bottom-bar function anchor, found {text.count(function_anchor)}")
    text = text.replace(function_anchor, function_replacement, 1)

    modifier_anchor = "            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),\n"
    modifier_replacement = (
        "            modifier = Modifier\n"
        "                .windowInsetsPadding(\n"
        "                    WindowInsets.navigationBarsIgnoringVisibility.only(WindowInsetsSides.Bottom),\n"
        "                )\n"
        "                .padding(horizontal = 16.dp, vertical = 10.dp),\n"
    )
    if text.count(modifier_anchor) != 1:
        raise SystemExit(f"expected one bottom-bar modifier anchor, found {text.count(modifier_anchor)}")
    text = text.replace(modifier_anchor, modifier_replacement, 1)

    ACTION_FILE.write_text(text, encoding="utf-8")


def main() -> None:
    candidate_files = []
    for path in sorted(SOURCE_ROOT.rglob("*.kt")):
        text = path.read_text(encoding="utf-8")
        if OLD_DIALOG in text:
            candidate_files.append(path)

    if not candidate_files:
        raise SystemExit("no unsafe full-width DialogProperties calls found")
    if ACTION_FILE not in candidate_files:
        raise SystemExit("shared night action dialog must be part of the unsafe dialog set")

    changed = []
    for path in candidate_files:
        if replace_dialog_properties(path):
            changed.append(path)

    patch_night_bottom_bar()

    relative = sorted(str(path.relative_to(ROOT)) for path in set(changed + [ACTION_FILE]))
    EXPECTED_FILES_OUTPUT.write_text("\n".join(relative) + "\n", encoding="utf-8")
    print(f"patched {len(relative)} production files")
    for path in relative:
        print(path)


if __name__ == "__main__":
    main()
