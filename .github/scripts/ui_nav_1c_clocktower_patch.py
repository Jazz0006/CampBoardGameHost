from pathlib import Path
import re

BASE = Path("app/src/main/java/com/codex/campboardgamehost")
PATHS = {
    "app": BASE / "CampBoardGameHostApp.kt",
    "host": BASE / "clocktower/ui/ClocktowerHostScreen.kt",
    "night_screen": BASE / "clocktower/ui/ClocktowerNightScreen.kt",
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
        raise SystemExit(f"{path}: unexpected CRLF/CR")
    return raw.decode("utf-8")


texts = {key: read_lf(path) for key, path in PATHS.items()}
originals = dict(texts)


def replace_exact(key: str, old: str, new: str, expected: int = 1) -> None:
    count = texts[key].count(old)
    if count != expected:
        raise SystemExit(f"{PATHS[key]}: anchor count {count}, expected {expected}\n{old}")
    texts[key] = texts[key].replace(old, new)


def replace_regex(key: str, pattern: str, repl, expected: int) -> None:
    matches = list(re.finditer(pattern, texts[key], flags=re.MULTILINE))
    if len(matches) != expected:
        raise SystemExit(f"{PATHS[key]}: regex count {len(matches)}, expected {expected}: {pattern}")
    texts[key] = re.sub(pattern, repl, texts[key], flags=re.MULTILINE)


# Shared square-table navigation delegates to the UI-NAV-1B three-slot primitive.
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

# Thread the utility callback through all square-table presentation signatures.
sig_old = '''    onPrevious: () -> Unit,
    onNext: () -> Unit,
'''
sig_new = '''    onPrevious: () -> Unit,
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

pair_pattern = r'^(\s*)onPrevious = onPrevious,\n\1onNext = onNext,$'
def pair_repl(match: re.Match) -> str:
    indent = match.group(1)
    return f"{indent}onPrevious = onPrevious,\n{indent}onHostTools = onHostTools,\n{indent}onNext = onNext,"

for key in ("chef", "empath", "undertaker", "clockmaker", "sage", "spy", "ravenkeeper"):
    replace_regex(key, pair_pattern, pair_repl, expected=1)
replace_regex("pair", pair_pattern, pair_repl, expected=2)
replace_regex("fortune", pair_pattern, pair_repl, expected=1)
replace_regex("chambermaid", pair_pattern, pair_repl, expected=1)

# Single-target/ruling wrappers carry Host Tools as UI utility, not as a gameplay event.
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
replace_exact(
    "single",
    '''        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
''',
    '''        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
''',
    expected=2,
)

# Replace the remaining local square-table Previous/Next variants.
replace_exact(
    "action",
    '''            Spacer(Modifier.height(6.dp))
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
''',
    '''            Spacer(Modifier.height(6.dp))
            ClocktowerSquareTableStepNavigation(
                language = language,
                canGoPrevious = canGoPrevious,
                nextEnabled = nextEnabled,
                onPrevious = onPrevious,
                onHostTools = onHostTools,
                onNext = onNext,
            )
''',
)
replace_exact(
    "fortune",
    '''        Spacer(Modifier.height(6.dp))
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
''',
    '''        Spacer(Modifier.height(6.dp))
        ClocktowerSquareTableStepNavigation(
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
        )
''',
)
replace_exact(
    "chambermaid",
    '''        Spacer(Modifier.height(6.dp))
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
''',
    '''        Spacer(Modifier.height(6.dp))
        ClocktowerSquareTableStepNavigation(
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
        )
''',
)

# Night-step orchestration carries Host Tools into every migrated square-table surface.
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

TRIPLE = '"' * 3

def find_call_close(text: str, start_paren: int) -> int:
    depth = 0
    i = start_paren
    state = "code"
    while i < len(text):
        c = text[i]
        n = text[i + 1] if i + 1 < len(text) else ""
        if state == "line_comment":
            if c == "\n": state = "code"
        elif state == "block_comment":
            if c == "*" and n == "/": state = "code"; i += 1
        elif state == "string":
            if c == "\\": i += 1
            elif c == '"': state = "code"
        elif state == "triple":
            if text.startswith(TRIPLE, i): state = "code"; i += 2
        elif state == "char":
            if c == "\\": i += 1
            elif c == "'": state = "code"
        else:
            if c == "/" and n == "/": state = "line_comment"; i += 1
            elif c == "/" and n == "*": state = "block_comment"; i += 1
            elif text.startswith(TRIPLE, i): state = "triple"; i += 2
            elif c == '"': state = "string"
            elif c == "'": state = "char"
            elif c == "(": depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0: return i
        i += 1
    raise SystemExit("unterminated Kotlin call")


def call_spans(text: str, callee: str):
    token = callee + "("
    spans = []
    for match in re.finditer(re.escape(token), text):
        start = match.start()
        line_start = text.rfind("\n", 0, start) + 1
        if re.search(r"\bfun\s+$", text[line_start:start]):
            continue
        close = find_call_close(text, start + len(callee))
        spans.append((start, close))
    return spans


def add_host_arg_to_unique_call(key: str, callee: str) -> None:
    text = texts[key]
    spans = call_spans(text, callee)
    if len(spans) != 1:
        raise SystemExit(f"{PATHS[key]}: {callee} invocation count {len(spans)}, expected 1")
    start, close = spans[0]
    block = text[start:close + 1]
    marker = re.compile(r"(?m)^(\s*)onPrevious = onPrevious,\n\1onNext = onNext,$")
    matches = list(marker.finditer(block))
    if len(matches) != 1:
        raise SystemExit(f"{PATHS[key]}: {callee} Previous/Next pair count {len(matches)}, expected 1")
    m = matches[0]
    indent = m.group(1)
    replacement = f"{indent}onPrevious = onPrevious,\n{indent}onHostTools = onHostTools,\n{indent}onNext = onNext,"
    block = block[:m.start()] + replacement + block[m.end():]
    texts[key] = text[:start] + block + text[close + 1:]


for callee in (
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
):
    add_host_arg_to_unique_call("night", callee)

# Ordinary night card fallback navigation uses the same three-slot presentation.
replace_exact(
    "night",
    '''            if (showNavigationActions) {
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
''',
    '''            if (showNavigationActions) {
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
''',
)

# D6 owner: NightActive owns the host-facing bottom strip.
replace_exact(
    "night_screen",
    '''    onPrevious: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit,
''',
    '''    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit,
''',
)
replace_exact(
    "night_screen",
    '''            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier
                            .weight(0.78f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.previous_step))
                    }
                    Button(
                        onClick = onNext,
                        enabled = nextEnabled,
                        modifier = Modifier
                            .weight(1.22f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_finish_next))
                    }
                }
            }
''',
    '''            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
''',
)

# D6 composition owner receives and distributes the root utility callback.
replace_exact(
    "host",
    '''    onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit,
    onMovePreviousNightStep: () -> Unit,
''',
    '''    onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit,
    onHostTools: () -> Unit,
    onMovePreviousNightStep: () -> Unit,
''',
)
replace_exact(
    "host",
    '''            onPrevious = onMovePreviousNightStep,
            onNext = advanceNightStep,
''',
    '''            onPrevious = onMovePreviousNightStep,
            onHostTools = onHostTools,
            onNext = advanceNightStep,
''',
    expected=2,
)

# App root remains the sole Host Tools state owner.
replace_exact(
    "app",
    '''                        onRecordEpistemicObservation = ::recordEpistemicObservation,
                        onSelectNightDeath = { selected ->
''',
    '''                        onRecordEpistemicObservation = ::recordEpistemicObservation,
                        onHostTools = {
                            hostToolTab = HostToolTab.Roles
                            showHostTools = true
                        },
                        onSelectNightDeath = { selected ->
''',
)

# 1C.1 deliberately keeps the legacy ClocktowerJudge top bar until Day/Dawn/ready are migrated.
if "screen == Screen.ClocktowerJudge ||" not in texts["app"]:
    raise SystemExit("UI-NAV-1C.1 must not remove the ClocktowerJudge top bar")
if "HostBottomActionBar(" not in texts["action"] or "HostBottomActionBar(" not in texts["night_screen"]:
    raise SystemExit("Night/square-table navigation was not delegated to HostBottomActionBar")
if "onHostTools: () -> Unit" not in texts["host"]:
    raise SystemExit("ClocktowerHostScreen did not receive onHostTools")

unchanged = [str(PATHS[key]) for key in PATHS if texts[key] == originals[key]]
if unchanged:
    raise SystemExit("Expected every 1C.1 target to change; unchanged: " + ", ".join(unchanged))

for key, path in PATHS.items():
    path.write_text(texts[key], encoding="utf-8", newline="\n")

print("UI-NAV-1C.1 D6-aware Night/square-table patch applied")
for path in sorted(PATHS.values(), key=str):
    print(path)
