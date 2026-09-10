from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt")


def read() -> str:
    return PATH.read_text(encoding="utf-8")


def write(text: str) -> None:
    PATH.write_text(text, encoding="utf-8", newline="\n")


def replace_exact(text: str, old: str, new: str, *, label: str, expected: int = 1) -> str:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact matches, got {count}")
    return text.replace(old, new)


text = read()

# Make the judge content scroll independently while navigation remains viewport-stable below it.
text = replace_exact(
    text,
    """    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
""",
    """    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
""",
    label="wrap WerewolfJudge scroll content",
)

bar = """                    HostBottomActionBar(
                        previousLabel = stringResource(R.string.previous_step),
                        hostToolsLabel = text(\"主持工具\", \"Host Tools\"),
                        nextLabel = stringResource(R.string.next_step),
                        onPrevious = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
                        onHostTools = onHostTools,
                        onNext = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
                        previousEnabled = currentIndex > 0,
                        nextEnabled = currentIndex < steps.lastIndex,
                    )
"""
text = replace_exact(text, bar, "", label="remove in-card judge bottom bar")

old_suffix = """        }
    }
}
"""
new_suffix = """        }
    }

        HostBottomActionBar(
            previousLabel = stringResource(R.string.previous_step),
            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),
            nextLabel = stringResource(R.string.next_step),
            onPrevious = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
            onHostTools = onHostTools,
            onNext = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            previousEnabled = currentIndex > 0,
            nextEnabled = currentIndex < steps.lastIndex,
        )
    }
}
"""
if not text.endswith(old_suffix):
    raise SystemExit("WerewolfJudge file suffix drifted")
text = text[: -len(old_suffix)] + new_suffix
write(text)

after = read()
judge_start = after.index("internal fun WerewolfJudgeScreen(")
bar_index = after.index("HostBottomActionBar(", judge_start)
results_index = after.index("onClick = onShowResults", judge_start)
assert bar_index > results_index
assert after.count("HostBottomActionBar(") == 1
assert ".weight(1f)\n                .fillMaxWidth()\n                .padding(20.dp)" in after[judge_start:]
assert "previousEnabled = currentIndex > 0" in after[bar_index:]
assert "nextEnabled = currentIndex < steps.lastIndex" in after[bar_index:]
assert "onHostTools = onHostTools" in after[bar_index:]
assert "onConfirmDawn(nightDeathEvents)" in after[judge_start:]
assert "onConfirmDayExile" in after[judge_start:]

print("UI-NAV-1D.1b WerewolfJudge viewport-bottom placement applied")
print(PATH)
