from pathlib import Path
import re
import textwrap

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
SHARED = ROOT / "ClocktowerHostFullScreenScaffold.kt"
COMMON = ROOT / "ClocktowerNightActionSquareTableUi.kt"
NIGHT_STEP = ROOT / "ClocktowerNightStepUi.kt"
CUSTOM = [
    ROOT / "ClocktowerChefSquareTableUi.kt",
    ROOT / "ClocktowerEmpathSquareTableUi.kt",
    ROOT / "ClocktowerFortuneTellerSquareTableUi.kt",
    ROOT / "ClocktowerPairInformationSquareTableUi.kt",
    ROOT / "ClocktowerUndertakerSquareTableUi.kt",
]
PAIR = ROOT / "ClocktowerPairInformationSquareTableUi.kt"


def replace_exact(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
    return text.replace(old, new, 1)


def matching_function_end(text: str, dialog_start: int) -> int:
    function_start = text.rfind("@Composable\ninternal fun ", 0, dialog_start)
    if function_start < 0:
        raise SystemExit("Could not locate owning composable")
    open_brace = text.find("{", function_start, dialog_start)
    if open_brace < 0:
        raise SystemExit("Could not locate owning function brace")
    depth = 0
    for index in range(open_brace, len(text)):
        char = text[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return index
    raise SystemExit("Could not locate owning function end")


# The shared Activity-root shell also owns Back semantics for surfaces that used to be Dialogs.
shared = SHARED.read_text(encoding="utf-8")
shared = replace_exact(
    shared,
    "package com.codex.campboardgamehost\n\n",
    "package com.codex.campboardgamehost\n\nimport androidx.activity.compose.BackHandler\n",
    "shared BackHandler import",
)
shared = replace_exact(
    shared,
    "    nextEnabled: Boolean = true,\n    body: @Composable () -> Unit,\n) {\n    Surface(\n",
    "    nextEnabled: Boolean = true,\n    onBack: (() -> Unit)? = null,\n    body: @Composable () -> Unit,\n) {\n    if (onBack != null) {\n        BackHandler(onBack = onBack)\n    }\n    Surface(\n",
    "shared BackHandler ownership",
)
SHARED.write_text(shared, encoding="utf-8", newline="\n")

# Preserve the old full-screen Dialog back behavior after moving the common square table inline.
common = COMMON.read_text(encoding="utf-8")
common = replace_exact(
    common,
    "        onPrevious = onPrevious,\n        onHostTools = onHostTools,\n        onNext = onNext,\n    ) {\n        ClocktowerSquareTableSeatSurface(\n",
    "        onPrevious = onPrevious,\n        onHostTools = onHostTools,\n        onNext = onNext,\n        onBack = { if (canGoPrevious) onPrevious() },\n    ) {\n        ClocktowerSquareTableSeatSurface(\n",
    "common square-table back handler",
)
COMMON.write_text(common, encoding="utf-8", newline="\n")

# The first ownership patch promotes NightStep to fillMaxSize for full-screen content.
night_step = NIGHT_STEP.read_text(encoding="utf-8")
if "import androidx.compose.foundation.layout.fillMaxSize\n" not in night_step:
    night_step = replace_exact(
        night_step,
        "import androidx.compose.foundation.layout.fillMaxWidth\n",
        "import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\n",
        "NightStep fillMaxSize import",
    )
NIGHT_STEP.write_text(night_step, encoding="utf-8", newline="\n")


def migrate_custom(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    for import_line in (
        "import androidx.compose.ui.window.Dialog\n",
        "import androidx.compose.ui.window.DialogProperties\n",
    ):
        if text.count(import_line) != 1:
            raise SystemExit(f"{path.name}: expected one {import_line.strip()}")
        text = text.replace(import_line, "", 1)

    dialog_marker = "\n    Dialog("
    if text.count(dialog_marker) != 1:
        raise SystemExit(f"{path.name}: expected one platform Dialog root, found {text.count(dialog_marker)}")
    dialog_start = text.index(dialog_marker)
    function_end = matching_function_end(text, dialog_start)

    table_marker = "ClocktowerSquareTableSeatSurface("
    table_start = text.find(table_marker, dialog_start, function_end)
    if table_start < 0:
        raise SystemExit(f"{path.name}: missing square-table body")
    line_start = text.rfind("\n", dialog_start, table_start) + 1
    table_start = line_start

    bottom_marker = "\n\n                ClocktowerNightBottomActionBar("
    bottom_start = text.find(bottom_marker, table_start, function_end)
    if bottom_start < 0:
        raise SystemExit(f"{path.name}: missing legacy night bottom bar")

    table_block = textwrap.dedent(text[table_start:bottom_start]).rstrip()
    weight_pattern = re.compile(
        r"modifier = Modifier\s*\n\s*\.weight\(1f\)\s*\n\s*\.fillMaxWidth\(\),"
    )
    table_block, replacements = weight_pattern.subn(
        "modifier = Modifier.fillMaxSize(),",
        table_block,
        count=1,
    )
    if replacements != 1:
        raise SystemExit(f"{path.name}: expected one weighted table modifier, found {replacements}")

    if path == PAIR:
        on_back = '''        onBack = {
            if (editing && recommendedSelection.resolvedOption != null) {
                restoreRecommendation()
            } else if (canGoPrevious) {
                onPrevious()
            }
        },
'''
    else:
        on_back = "        onBack = { if (canGoPrevious) onPrevious() },\n"

    replacement = (
        "\n    ClocktowerHostFullScreenScaffold(\n"
        "        previousLabel = if (language == \"en\") \"← Previous\" else \"← 上一步\",\n"
        "        hostToolsLabel = if (language == \"en\") \"Host Tools\" else \"主持工具\",\n"
        "        nextLabel = if (language == \"en\") \"Next →\" else \"下一步 →\",\n"
        "        previousEnabled = canGoPrevious,\n"
        "        onPrevious = onPrevious,\n"
        "        onHostTools = onHostTools,\n"
        "        onNext = onNext,\n"
        + on_back
        + "    ) {\n"
        + textwrap.indent(table_block, "        ")
        + "\n    }\n"
    )
    text = text[:dialog_start] + replacement + text[function_end:]

    if "import androidx.compose.ui.window.Dialog" in text:
        raise SystemExit(f"{path.name}: Dialog import survived")
    if "DialogProperties(" in text or "\n    Dialog(" in text:
        raise SystemExit(f"{path.name}: platform Dialog survived")
    if "ClocktowerNightBottomActionBar(" in text:
        raise SystemExit(f"{path.name}: legacy night bottom bar survived")
    if text.count("ClocktowerHostFullScreenScaffold(") < 1:
        raise SystemExit(f"{path.name}: shared scaffold missing")

    path.write_text(text, encoding="utf-8", newline="\n")


for custom_path in CUSTOM:
    migrate_custom(custom_path)

# Fail closed over every role-specific square-table source, not only the files discovered by compile.
for path in sorted(ROOT.glob("Clocktower*SquareTableUi.kt")):
    source = path.read_text(encoding="utf-8")
    if "import androidx.compose.ui.window.Dialog" in source:
        raise SystemExit(f"{path.name}: platform Dialog import remains")
    if "DialogProperties(" in source or "\n    Dialog(" in source:
        raise SystemExit(f"{path.name}: platform Dialog root remains")
    if "ClocktowerNightBottomActionBar(" in source:
        raise SystemExit(f"{path.name}: retired night bottom bar remains")

print("Residual Night square-table owners migrated to Activity-root scaffold")
