from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
APP = ROOT / "CampBoardGameHostApp.kt"
WEREWOLF = ROOT / "werewolf/WerewolfHostScreen.kt"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


def replace_exact(text: str, old: str, new: str, *, label: str, expected: int = 1) -> str:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact matches, got {count}")
    return text.replace(old, new)


# Werewolf judge: reuse existing step cursor owners; add only the root-owned Host Tools callback.
werewolf = read(WEREWOLF)
werewolf = replace_exact(
    werewolf,
    "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.res.stringResource\n",
    "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.res.stringResource\n",
    label="LocalContext import",
)
werewolf = replace_exact(
    werewolf,
    """    onConfirmDayExile: () -> Unit,
    onDismissLastWordsPrompt: () -> Unit,
    onShowResults: () -> Unit,
) {
    val roleRegistry = WerewolfRoleRegistry.builtIn()
""",
    """    onConfirmDayExile: () -> Unit,
    onDismissLastWordsPrompt: () -> Unit,
    onHostTools: () -> Unit,
    onShowResults: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == \"en\") en else zh
    val roleRegistry = WerewolfRoleRegistry.builtIn()
""",
    label="WerewolfJudge Host Tools parameter",
)
werewolf = replace_exact(
    werewolf,
    """                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
                            enabled = currentIndex > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.previous_step))
                        }
                        Button(
                            onClick = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
                            enabled = currentIndex < steps.lastIndex,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.next_step))
                        }
                    }
""",
    """                    HostBottomActionBar(
                        previousLabel = stringResource(R.string.previous_step),
                        hostToolsLabel = text(\"主持工具\", \"Host Tools\"),
                        nextLabel = stringResource(R.string.next_step),
                        onPrevious = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
                        onHostTools = onHostTools,
                        onNext = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
                        previousEnabled = currentIndex > 0,
                        nextEnabled = currentIndex < steps.lastIndex,
                    )
""",
    label="WerewolfJudge step navigation",
)
write(WEREWOLF, werewolf)

# Root: distribute existing Host Tools owner and retire only WerewolfJudge's old top-chrome dependency.
app = read(APP)
app = replace_exact(
    app,
    """                            screen == Screen.WerewolfJudge ||
                            screen == Screen.Game
""",
    """                            screen == Screen.Game
""",
    label="remove WerewolfJudge top-bar condition",
)
app = replace_exact(
    app,
    """                        onDismissLastWordsPrompt = { lastWordsPromptNames = emptyList() },
                        onShowResults = {
""",
    """                        onDismissLastWordsPrompt = { lastWordsPromptNames = emptyList() },
                        onHostTools = {
                            hostToolTab = HostToolTab.Roles
                            showHostTools = true
                        },
                        onShowResults = {
""",
    label="root WerewolfJudge Host Tools wiring",
)
write(APP, app)

# Postconditions: exact ownership/presentation intent only.
werewolf_after = read(WEREWOLF)
assert werewolf_after.count("internal fun WerewolfJudgeScreen(") == 1
assert "onHostTools: () -> Unit" in werewolf_after
assert "HostBottomActionBar(" in werewolf_after
assert "onHostTools = onHostTools" in werewolf_after
assert "previousEnabled = currentIndex > 0" in werewolf_after
assert "nextEnabled = currentIndex < steps.lastIndex" in werewolf_after
assert "onConfirmDawn(nightDeathEvents)" in werewolf_after
assert "onConfirmDayExile" in werewolf_after
assert "onShowResults" in werewolf_after

app_after = read(APP)
assert "screen == Screen.WerewolfJudge ||" not in app_after
assert "screen == Screen.Game" in app_after
assert app_after.count("onHostTools = {") >= 2

print("UI-NAV-1D.1 WerewolfJudge patch applied")
print(APP)
print(WEREWOLF)
