from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
NIGHT_STEP = ROOT / "ClocktowerNightStepUi.kt"
HOST_SCREEN = ROOT / "clocktower/ui/ClocktowerHostScreen.kt"


def replace_exact(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")
    return text.replace(old, new, 1)


night = NIGHT_STEP.read_text(encoding="utf-8")
night = replace_exact(
    night,
    "    onNext: () -> Unit,\n    showNavigationActions: Boolean = true,\n) {",
    "    onNext: () -> Unit,\n) {",
    "NightStep navigation parameter",
)
legacy_nav = '''                if (showNavigationActions) {
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

                    Surface(
'''
night = replace_exact(
    night,
    legacy_nav,
    "                Surface(\n",
    "NightStep dormant bottom navigation",
)
if "stringResource(" not in night:
    night = replace_exact(
        night,
        "import androidx.compose.ui.res.stringResource\n",
        "",
        "unused stringResource import",
    )
if "showNavigationActions" in night:
    raise SystemExit("showNavigationActions survived NightStep cleanup")
if "HostBottomActionBar(" in night:
    raise SystemExit("NightStep still owns HostBottomActionBar")
NIGHT_STEP.write_text(night, encoding="utf-8", newline="\n")

host = HOST_SCREEN.read_text(encoding="utf-8")
host = replace_exact(
    host,
    "                onNext = advanceNightStep,\n                showNavigationActions = false,\n",
    "                onNext = advanceNightStep,\n",
    "HostScreen dormant navigation disable",
)
if "showNavigationActions" in host:
    raise SystemExit("showNavigationActions survived HostScreen cleanup")
HOST_SCREEN.write_text(host, encoding="utf-8", newline="\n")

print("Dormant NightStep bottom navigation ownership retired")
