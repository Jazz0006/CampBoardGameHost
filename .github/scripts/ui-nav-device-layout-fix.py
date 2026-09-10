from pathlib import Path
import re
import textwrap

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise AssertionError(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


def match_delimiter(text: str, start: int, opener: str, closer: str) -> int:
    if text[start] != opener:
        raise AssertionError(f"expected {opener!r} at {start}")
    depth = 0
    i = start
    in_string = False
    in_char = False
    triple = False
    line_comment = False
    block_comment = 0
    escaped = False
    while i < len(text):
        if line_comment:
            if text[i] == "\n":
                line_comment = False
            i += 1
            continue
        if block_comment:
            if text.startswith("/*", i):
                block_comment += 1
                i += 2
                continue
            if text.startswith("*/", i):
                block_comment -= 1
                i += 2
                continue
            i += 1
            continue
        if triple:
            if text.startswith('"""', i):
                triple = False
                i += 3
            else:
                i += 1
            continue
        if in_string:
            if escaped:
                escaped = False
            elif text[i] == "\\":
                escaped = True
            elif text[i] == '"':
                in_string = False
            i += 1
            continue
        if in_char:
            if escaped:
                escaped = False
            elif text[i] == "\\":
                escaped = True
            elif text[i] == "'":
                in_char = False
            i += 1
            continue
        if text.startswith("//", i):
            line_comment = True
            i += 2
            continue
        if text.startswith("/*", i):
            block_comment = 1
            i += 2
            continue
        if text.startswith('"""', i):
            triple = True
            i += 3
            continue
        if text[i] == '"':
            in_string = True
            i += 1
            continue
        if text[i] == "'":
            in_char = True
            i += 1
            continue
        if text[i] == opener:
            depth += 1
        elif text[i] == closer:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise AssertionError(f"unmatched delimiter {opener}{closer} at {start}")


def replace_function(text: str, name: str, replacement: str) -> str:
    marker = f"@Composable\ninternal fun {name}("
    start = text.index(marker)
    paren = text.index("(", start)
    paren_end = match_delimiter(text, paren, "(", ")")
    brace = text.index("{", paren_end)
    brace_end = match_delimiter(text, brace, "{", "}")
    return text[:start] + replacement.rstrip() + text[brace_end + 1:]


def remove_nav_call(text: str, label: str) -> str:
    matches = list(re.finditer(r"(?m)^(?P<indent>[ \t]*)ClocktowerSquareTableStepNavigation\(", text))
    if len(matches) != 1:
        raise AssertionError(f"{label}: expected one nested navigation call, got {len(matches)}")
    match = matches[0]
    start = match.start()
    open_paren = text.index("(", match.start())
    close_paren = match_delimiter(text, open_paren, "(", ")")
    end = close_paren + 1
    while end < len(text) and text[end] in " \t":
        end += 1
    if end < len(text) and text[end] == "\n":
        end += 1
    # Remove one preceding blank line as well, but never consume non-whitespace content.
    prev_line_start = text.rfind("\n", 0, start - 1) + 1
    if text[prev_line_start:start].strip() == "":
        start = prev_line_start
    return text[:start] + text[end:]


def inject_shared_dialog_args(text: str, args: list[str], label: str) -> str:
    marker = "ClocktowerNightActionSquareTableDialog("
    candidates = [m.start() for m in re.finditer(re.escape(marker), text)]
    call_start = None
    for candidate in candidates:
        line_start = text.rfind("\n", 0, candidate) + 1
        prefix = text[line_start:candidate]
        if "fun " not in prefix:
            call_start = candidate
            break
    if call_start is None:
        raise AssertionError(f"{label}: shared dialog call not found")
    open_paren = text.index("(", call_start)
    close_paren = match_delimiter(text, open_paren, "(", ")")
    line_start = text.rfind("\n", 0, close_paren) + 1
    close_indent = text[line_start:close_paren]
    if close_indent.strip():
        raise AssertionError(f"{label}: expected closing parenthesis on its own line")
    arg_indent = close_indent + "    "
    insertion = "".join(f"{arg_indent}{arg},\n" for arg in args)
    return text[:line_start] + insertion + text[line_start:]


def wrap_raw_square_table_surface(text: str, function_name: str, bottom_args: list[str]) -> str:
    function_marker = f"internal fun {function_name}("
    fn = text.index(function_marker)
    fn_paren = text.index("(", fn)
    fn_paren_end = match_delimiter(text, fn_paren, "(", ")")
    fn_brace = text.index("{", fn_paren_end)
    fn_end = match_delimiter(text, fn_brace, "{", "}")

    dialog = text.index("Dialog(", fn_brace, fn_end)
    surface = text.index("Surface(", dialog, fn_end)
    surface_paren = text.index("(", surface)
    surface_paren_end = match_delimiter(text, surface_paren, "(", ")")
    surface_brace = text.index("{", surface_paren_end, fn_end)
    surface_end = match_delimiter(text, surface_brace, "{", "}")
    body = text[surface_brace + 1:surface_end]
    if body.count("ClocktowerSquareTableSeatSurface(") != 1:
        raise AssertionError(f"{function_name}: expected one direct square-table surface")

    body = remove_nav_call(body, function_name)
    body = textwrap.dedent(body).strip("\n")
    body = replace_once(
        body,
        "modifier = Modifier.fillMaxSize(),",
        "modifier = Modifier\n        .weight(1f)\n        .fillMaxWidth(),",
        f"{function_name} weighted table",
    )
    indented_body = textwrap.indent(body, "                ")
    bottom = "\n".join(f"                    {arg}" for arg in bottom_args)
    new_body = (
        "\n            Column(modifier = Modifier.fillMaxSize()) {\n"
        f"{indented_body}\n\n"
        "                ClocktowerNightBottomActionBar(\n"
        f"{bottom}\n"
        "                )\n"
        "            }\n        "
    )
    return text[:surface_brace + 1] + new_body + text[surface_end:]


# 1) Real-device identity controller: the Host table already constrains the center workspace.
# Do not consume that safe workspace again with 68dp padding on each side.
path = ROOT / "AppDealScreens.kt"
text = path.read_text()
text = replace_once(
    text,
    """        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 68.dp),
""",
    """        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(),
""",
    "identity center panel width",
)
path.write_text(text)

# 2) Shared night dialog owns the screen-level bottom bar, outside the square-table surface.
path = ROOT / "ClocktowerNightActionSquareTableUi.kt"
text = path.read_text()
text = remove_nav_call(text, "single-target center navigation")
text = inject_shared_dialog_args(
    text,
    [
        "onHostTools = onHostTools",
        "onNext = onNext",
        "nextEnabled = nextEnabled",
    ],
    "single-target shared dialog",
)
text = text.replace(
    "Compact shared navigation for square-table night actions.",
    "Shared screen-bottom navigation for square-table night actions.",
    1,
)
text = replace_function(
    text,
    "ClocktowerSquareTableStepNavigation",
    '''@Composable
internal fun ClocktowerNightBottomActionBar(
    language: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean = true,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
    ) {
        HostBottomActionBar(
            previousLabel = if (language == "en") "← Previous" else "← 上一步",
            hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
            nextLabel = if (language == "en") "Next →" else "下一步 →",
            previousEnabled = canGoPrevious,
            nextEnabled = nextEnabled,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}''',
)
text = replace_function(
    text,
    "ClocktowerNightActionSquareTableDialog",
    '''@Composable
internal fun ClocktowerNightActionSquareTableDialog(
    seats: List<HostSeatPresentation>,
    enabled: Boolean,
    language: String,
    seatPresentation: (Int) -> ClocktowerNightActionSeatPresentation,
    onSeatSelected: (Int) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    centerContent: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (canGoPrevious) onPrevious()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ClocktowerSquareTableSeatSurface(
                    seats = seats.map { seat ->
                        val content = hostSeatContentPresentation(seat, language)
                        val presentation = seatPresentation(seat.seatId.number)
                        ClocktowerSquareTableSeatUiModel(
                            seatId = seat.seatId.renderKey(),
                            seatNumber = seat.seatId.number,
                            label = content.primaryLabel,
                            detailLabels = content.detailLabels,
                            state = presentation.targetState,
                            isCurrentActor = presentation.isCurrentActor,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    interactionMode = if (enabled) {
                        ClocktowerSquareTableInteractionMode.Selectable
                    } else {
                        ClocktowerSquareTableInteractionMode.ReadOnly
                    },
                    onSeatClick = { renderKey ->
                        seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }
                            ?.seatId
                            ?.number
                            ?.let(onSeatSelected)
                    },
                ) {
                    centerContent()
                }
                ClocktowerNightBottomActionBar(
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                    nextEnabled = nextEnabled,
                )
            }
        }
    }
}''',
)
path.write_text(text)

# Shared-dialog night roles: remove the center-owned nav and pass existing callbacks to the shared
# screen-bottom owner. No gameplay or enabled semantics change.
for filename in [
    "ClocktowerSageSquareTableUi.kt",
    "ClocktowerSpySquareTableUi.kt",
    "ClocktowerRavenkeeperSquareTableUi.kt",
    "ClocktowerChambermaidSquareTableUi.kt",
    "ClocktowerClockmakerSquareTableUi.kt",
]:
    path = ROOT / filename
    text = path.read_text()
    text = remove_nav_call(text, filename)
    text = inject_shared_dialog_args(
        text,
        ["onHostTools = onHostTools", "onNext = onNext"],
        filename,
    )
    path.write_text(text)

# Custom square-table dialogs keep their specialized seat projection but move the same callbacks to
# a screen-bottom sibling below the weighted table surface.
for filename, function_name in [
    ("ClocktowerChefSquareTableUi.kt", "ClocktowerChefSquareTableDialog"),
    ("ClocktowerEmpathSquareTableUi.kt", "ClocktowerEmpathSquareTableDialog"),
    ("ClocktowerUndertakerSquareTableUi.kt", "ClocktowerUndertakerSquareTableDialog"),
    ("ClocktowerPairInformationSquareTableUi.kt", "ClocktowerPairInformationSquareTableDialog"),
]:
    path = ROOT / filename
    text = path.read_text()
    if filename == "ClocktowerPairInformationSquareTableUi.kt":
        text = replace_once(
            text,
            """                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSelectionChange = { selection = it },
                    onStartEditing = { editing = true },
                    onRestoreRecommendation = ::restoreRecommendation,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                    onConfirm = onConfirm,
""",
            """                    language = language,
                    onSelectionChange = { selection = it },
                    onStartEditing = { editing = true },
                    onRestoreRecommendation = ::restoreRecommendation,
                    onConfirm = onConfirm,
""",
            "pair center call navigation params",
        )
        text = replace_once(
            text,
            """    roleLabel: (String) -> String,
    language: String,
    canGoPrevious: Boolean,
    onSelectionChange: (ClocktowerPairManualSelectionModel) -> Unit,
    onStartEditing: () -> Unit,
    onRestoreRecommendation: () -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
""",
            """    roleLabel: (String) -> String,
    language: String,
    onSelectionChange: (ClocktowerPairManualSelectionModel) -> Unit,
    onStartEditing: () -> Unit,
    onRestoreRecommendation: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
""",
            "pair center signature navigation params",
        )
    text = wrap_raw_square_table_surface(
        text,
        function_name,
        [
            "language = language,",
            "canGoPrevious = canGoPrevious,",
            "onPrevious = onPrevious,",
            "onHostTools = onHostTools,",
            "onNext = onNext,",
        ],
    )
    path.write_text(text)

# Fortune Teller already has a screen-level Column. Move its nav out of HostTableShell and append it
# as the last child of that Column.
path = ROOT / "ClocktowerFortuneTellerSquareTableUi.kt"
text = path.read_text()
text = remove_nav_call(text, "Fortune Teller center navigation")
marker = "HostTableShell("
host = text.index(marker)
open_paren = text.index("(", host)
close_paren = match_delimiter(text, open_paren, "(", ")")
host_brace = text.index("{", close_paren)
host_end = match_delimiter(text, host_brace, "{", "}")
insertion = '''

        ClocktowerNightBottomActionBar(
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onSubmit,
            nextEnabled = canSubmit,
        )'''
text = text[:host_end + 1] + insertion + text[host_end + 1:]
path.write_text(text)

# Final structural assertions.
app_deal = (ROOT / "AppDealScreens.kt").read_text()
assert ".padding(horizontal = 68.dp)" not in app_deal
assert ".align(Alignment.Center)\n            .fillMaxWidth()," in app_deal

all_kotlin = "\n".join(path.read_text() for path in ROOT.glob("*.kt"))
assert "ClocktowerSquareTableStepNavigation(" not in all_kotlin
assert all_kotlin.count("ClocktowerNightBottomActionBar(") == 7  # one definition + six screen-level uses

night = (ROOT / "ClocktowerNightActionSquareTableUi.kt").read_text()
assert "modifier = Modifier\n                        .weight(1f)\n                        .fillMaxWidth()," in night
assert "onHostTools: () -> Unit,\n    onNext: () -> Unit,\n    nextEnabled: Boolean = true," in night

print("UI-NAV real-device layout patch PASS")
