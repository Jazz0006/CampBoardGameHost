from pathlib import Path
import re

ROOT = Path(".")
BASE = Path("app/src/main/java/com/codex/campboardgamehost")

PATHS = {
    "app": BASE / "CampBoardGameHostApp.kt",
    "night": BASE / "ClocktowerNightStepUi.kt",
    "action": BASE / "ClocktowerNightActionSquareTableUi.kt",
    "chef": BASE / "ClocktowerChefSquareTableUi.kt",
    "empath": BASE / "ClocktowerEmpathSquareTableUi.kt",
    "undertaker": BASE / "ClocktowerUndertakerSquareTableUi.kt",
    "clockmaker": BASE / "ClocktowerClockmakerSquareTableUi.kt",
    "sage": BASE / "ClocktowerSageSquareTableUi.kt",
    "spy": BASE / "ClocktowerSpySquareTableUi.kt",
    "ravenkeeper": BASE / "ClocktowerRavenkeeperSquareTableUi.kt",
    "fortune": BASE / "ClocktowerFortuneTellerSquareTableUi.kt",
    "chambermaid": BASE / "ClocktowerChambermaidSquareTableUi.kt",
    "pair": BASE / "ClocktowerPairInformationSquareTableUi.kt",
    "single": BASE / "ClocktowerSingleTargetInteractionUi.kt",
}

def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"{path}: unexpected CRLF/CR; refusing normalization")
    return raw.decode("utf-8")

texts = {key: read_lf(path) for key, path in PATHS.items()}
originals = dict(texts)

def replace_exact(key: str, old: str, new: str, expected: int = 1) -> None:
    text = texts[key]
    count = text.count(old)
    if count != expected:
        raise SystemExit(
            f"{PATHS[key]}: exact anchor count {count}, expected {expected}\nANCHOR:\n{old}"
        )
    texts[key] = text.replace(old, new)

def replace_regex(key: str, pattern: str, repl, expected: int) -> None:
    text = texts[key]
    matches = list(re.finditer(pattern, text, flags=re.MULTILINE))
    if len(matches) != expected:
        raise SystemExit(
            f"{PATHS[key]}: regex anchor count {len(matches)}, expected {expected}: {pattern}"
        )
    texts[key] = re.sub(pattern, repl, text, flags=re.MULTILINE)

# 1. Shared square-table navigation delegates to the UI-NAV-1B primitive.
old_shared = '''@Composable
internal fun ClocktowerSquareTableStepNavigation(
    language: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean = true,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canGoPrevious,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (language == "en") "← Previous" else "← 上一步",
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Button(
            onClick = onNext,
            enabled = nextEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (language == "en") "Next →" else "下一步 →",
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
'''
new_shared = '''@Composable
internal fun ClocktowerSquareTableStepNavigation(
    language: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean = true,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
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
    )
}
'''
replace_exact("action", old_shared, new_shared)

# 2. Add the presentation callback to dialog/center signatures.
sig_old = '''    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
'''
sig_new = '''    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
'''
for key in ("chef", "empath", "undertaker", "clockmaker", "sage", "spy", "ravenkeeper"):
    replace_exact(key, sig_old, sig_new, expected=1)
replace_exact("pair", sig_old, sig_new, expected=2)
replace_exact("fortune", sig_old, sig_new, expected=2)
replace_exact("chambermaid", sig_old, sig_new, expected=2)
replace_exact("action", sig_old, sig_new, expected=1)
replace_exact("night", sig_old, sig_new, expected=1)

# 3. Existing shared-helper consumers pass through Host Tools.
pair_pattern = r'^(\s*)onPrevious = onPrevious,\n\1onNext = onNext,$'
def pair_repl(match: re.Match) -> str:
    indent = match.group(1)
    return (
        f"{indent}onPrevious = onPrevious,\n"
        f"{indent}onHostTools = onHostTools,\n"
        f"{indent}onNext = onNext,"
    )

for key in ("chef", "empath", "undertaker", "clockmaker", "sage", "spy", "ravenkeeper"):
    replace_regex(key, pair_pattern, pair_repl, expected=1)
replace_regex("pair", pair_pattern, pair_repl, expected=2)
replace_regex("fortune", pair_pattern, pair_repl, expected=1)
replace_regex("chambermaid", pair_pattern, pair_repl, expected=1)

# 4. Single-target / ruling presentation sections pass Host Tools without adding a gameplay event.
single_sig_old = '''    language: String,
    canGoPrevious: Boolean,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
'''
single_sig_new = '''    language: String,
    canGoPrevious: Boolean,
    onHostTools: () -> Unit,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
'''
replace_exact("single", single_sig_old, single_sig_new, expected=2)

single_call_old = '''        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
'''
single_call_new = '''        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
'''
replace_exact("single", single_call_old, single_call_new, expected=2)

# 5. Replace the remaining local square-table flow navigation variants only.
single_nav_old = '''            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
            }

            if (canGoPrevious) {
                TextButton(onClick = onPrevious) {
                    Text(if (language == "en") "Previous step" else "上一步")
                }
            }
'''
single_nav_new = '''            Spacer(Modifier.height(6.dp))
            ClocktowerSquareTableStepNavigation(
                language = language,
                canGoPrevious = canGoPrevious,
                nextEnabled = nextEnabled,
                onPrevious = onPrevious,
                onHostTools = onHostTools,
                onNext = onNext,
            )
'''
replace_exact("action", single_nav_old, single_nav_new)

fortune_nav_old = '''        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
        }

        if (canGoPrevious) {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
'''
fortune_nav_new = '''        Spacer(Modifier.height(6.dp))
        ClocktowerSquareTableStepNavigation(
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
        )
'''
replace_exact("fortune", fortune_nav_old, fortune_nav_new)

chamber_nav_old = '''        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
        }

        if (canGoPrevious) {
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
'''
chamber_nav_new = '''        Spacer(Modifier.height(6.dp))
        ClocktowerSquareTableStepNavigation(
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
        )
'''
replace_exact("chambermaid", chamber_nav_old, chamber_nav_new)

# 6. Night-step orchestration threads the UI utility callback to every migrated square-table surface.
replace_exact(
    "night",
    "ClocktowerSingleTargetAbilitySection(nightActionSeats, it, language, canGoPrevious, onSingleTargetEvent)",
    "ClocktowerSingleTargetAbilitySection(nightActionSeats, it, language, canGoPrevious, onHostTools, onSingleTargetEvent)",
)
replace_exact(
    "night",
    "ClocktowerNightRulingSection(nightActionSeats, it, language, canGoPrevious, onSingleTargetEvent)",
    "ClocktowerNightRulingSection(nightActionSeats, it, language, canGoPrevious, onHostTools, onSingleTargetEvent)",
)

named_square_calls = (
    "ClocktowerRavenkeeperSquareTableDialog",
    "ClocktowerFortuneTellerSquareTableDialog",
    "ClocktowerChambermaidSquareTableDialog",
    "ClocktowerChefSquareTableDialog",
    "ClocktowerEmpathSquareTableDialog",
    "ClocktowerUndertakerSquareTableDialog",
    "ClocktowerSpySquareTableDialog",
    "ClocktowerClockmakerSquareTableDialog",
    "ClocktowerSageSquareTableDialog",
    "ClocktowerPairInformationSquareTableDialog",
)

TRIPLE = '"' * 3

def find_call_close(text: str, start_paren: int) -> int:
    depth = 0
    i = start_paren
    state = "code"
    while i < len(text):
        c = text[i]
        n = text[i + 1] if i + 1 < len(text) else ""
        if state == "line_comment":
            if c == "\n":
                state = "code"
        elif state == "block_comment":
            if c == "*" and n == "/":
                state = "code"
                i += 1
        elif state == "string":
            if c == "\\":
                i += 1
            elif c == '"':
                state = "code"
        elif state == "triple":
            if text.startswith(TRIPLE, i):
                state = "code"
                i += 2
        elif state == "char":
            if c == "\\":
                i += 1
            elif c == "'":
                state = "code"
        else:
            if c == "/" and n == "/":
                state = "line_comment"
                i += 1
            elif c == "/" and n == "*":
                state = "block_comment"
                i += 1
            elif text.startswith(TRIPLE, i):
                state = "triple"
                i += 2
            elif c == '"':
                state = "string"
            elif c == "'":
                state = "char"
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    raise SystemExit("unterminated Kotlin call")

def call_spans(text: str, callee: str):
    token = callee + "("
    starts = [m.start() for m in re.finditer(re.escape(token), text)]
    spans = []
    for start in starts:
        line_start = text.rfind("\n", 0, start) + 1
        prefix = text[line_start:start]
        if re.search(r"\bfun\s+$", prefix):
            continue
        paren = start + len(callee)
        close = find_call_close(text, paren)
        spans.append((start, close))
    return spans

def add_named_host_arg_to_unique_call(key: str, callee: str) -> None:
    text = texts[key]
    spans = call_spans(text, callee)
    if len(spans) != 1:
        raise SystemExit(f"{PATHS[key]}: {callee} invocation count {len(spans)}, expected 1")
    start, close = spans[0]
    block = text[start:close + 1]
    if "onHostTools =" in block:
        raise SystemExit(f"{PATHS[key]}: {callee} already has onHostTools")
    marker = re.compile(r"(?m)^(\s*)onPrevious = onPrevious,\n\1onNext = onNext,$")
    matches = list(marker.finditer(block))
    if len(matches) != 1:
        raise SystemExit(
            f"{PATHS[key]}: {callee} onPrevious/onNext pair count {len(matches)}, expected 1"
        )
    m = matches[0]
    indent = m.group(1)
    replacement = (
        f"{indent}onPrevious = onPrevious,\n"
        f"{indent}onHostTools = onHostTools,\n"
        f"{indent}onNext = onNext,"
    )
    block2 = block[:m.start()] + replacement + block[m.end():]
    texts[key] = text[:start] + block2 + text[close + 1:]

for callee in named_square_calls:
    add_named_host_arg_to_unique_call("night", callee)

# 7. Ordinary night-step navigation uses the same three-slot primitive and preserves enabled semantics.
night_nav_old = '''            if (showNavigationActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.previous_step))
                    }
                    Button(
                        onClick = onNext,
                        enabled = step.action !in setOf(
                            ClocktowerNightAction.MayorRedirect,
                            ClocktowerNightAction.DemonSuccessor,
                        ) || selectedName != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_finish_next))
                    }
                }
            }
'''
night_nav_new = '''            if (showNavigationActions) {
                HostBottomActionBar(
                    previousLabel = stringResource(R.string.previous_step),
                    hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
                    nextLabel = if (language == "en") "Next →" else "下一步 →",
                    previousEnabled = canGoPrevious,
                    nextEnabled = step.action !in setOf(
                        ClocktowerNightAction.MayorRedirect,
                        ClocktowerNightAction.DemonSuccessor,
                    ) || selectedName != null,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                )
            }
'''
replace_exact("night", night_nav_old, night_nav_new)

# 8. App root remains the only owner of Host Tools overlay state.
app_text = texts["app"]
spans = call_spans(app_text, "ClocktowerNightStepCardLocalized")
if len(spans) != 1:
    raise SystemExit(
        f"{PATHS['app']}: ClocktowerNightStepCardLocalized invocation count {len(spans)}, expected 1"
    )
start, close = spans[0]
block = app_text[start:close + 1]
if "onHostTools =" in block:
    raise SystemExit("App NightStep call already has onHostTools")
if "onPrevious =" not in block or "onNext =" not in block:
    raise SystemExit("App NightStep call does not expose expected existing navigation callbacks")
before_close = block[:-1]
if not before_close.rstrip().endswith(","):
    raise SystemExit("App NightStep call last existing argument lacks trailing comma")
close_line_start = app_text.rfind("\n", 0, close) + 1
close_indent = app_text[close_line_start:close]
arg_indent = close_indent + "    "
insert = (
    f"{arg_indent}onHostTools = {{\n"
    f"{arg_indent}    hostToolTab = HostToolTab.Roles\n"
    f"{arg_indent}    showHostTools = true\n"
    f"{arg_indent}}},\n"
)
texts["app"] = app_text[:close_line_start] + insert + app_text[close_line_start:]

topbar_old = '''        screen == Screen.WerewolfJudge ||
        screen == Screen.ClocktowerJudge ||
        screen == Screen.Game
'''
topbar_new = '''        screen == Screen.WerewolfJudge ||
        screen == Screen.Game
'''
replace_exact("app", topbar_old, topbar_new)

# 9. Postconditions.
if "HostBottomActionBar(" not in texts["action"]:
    raise SystemExit("shared square-table navigation is not delegated to HostBottomActionBar")
if "HostBottomActionBar(" not in texts["night"]:
    raise SystemExit("ordinary night navigation is not delegated to HostBottomActionBar")
if "screen == Screen.ClocktowerJudge ||" in texts["app"]:
    raise SystemExit("ClocktowerJudge still owns the legacy persistent Host Tools top bar")
if texts["app"].count("hostToolTab = HostToolTab.Roles") < originals["app"].count("hostToolTab = HostToolTab.Roles") + 1:
    raise SystemExit("App root Host Tools callback was not added")
if texts["app"].count("showHostTools = true") < originals["app"].count("showHostTools = true") + 1:
    raise SystemExit("App root Host Tools visibility callback was not added")

unchanged = [str(PATHS[k]) for k in PATHS if texts[k] == originals[k]]
if unchanged:
    raise SystemExit("Expected every target to change, unchanged: " + ", ".join(unchanged))

for key, path in PATHS.items():
    path.write_text(texts[key], encoding="utf-8", newline="\n")

print("UI-NAV-1C Clocktower navigation patch applied to:")
for path in sorted(PATHS.values(), key=str):
    print(path)
