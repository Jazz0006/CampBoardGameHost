from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt")

raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit normalization")

text = raw.decode("utf-8")
old = """    onReevaluate: (List<StorytellerDecision>) -> Unit,
    onClearLocks: () -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == \"en\") en else zh
"""
new = """    onReevaluate: (List<StorytellerDecision>) -> Unit,
    onClearLocks: () -> Unit,
) {
    if (automaticStorytellerInfo && appliedStyle == selectedStyle) return

    fun text(zh: String, en: String): String = if (language == \"en\") en else zh
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected StorytellerRecommendationCard entry anchor exactly once, found {count}")
if "if (automaticStorytellerInfo && appliedStyle == selectedStyle) return" in text:
    raise SystemExit("Beginner applied-plan hide guard already exists")

patched = text.replace(old, new, 1)
if patched.count("if (automaticStorytellerInfo && appliedStyle == selectedStyle) return") != 1:
    raise SystemExit("Expected exactly one Beginner applied-plan hide guard")
for required in (
    "RecommendationUiState.Loading",
    "RecommendationUiState.Empty",
    "is RecommendationUiState.InvalidLocks",
    "is RecommendationUiState.Error",
    "if (!automaticStorytellerInfo && editingDecisions)",
):
    if required not in patched:
        raise SystemExit(f"Required recommendation UI contract disappeared: {required}")

TARGET.write_text(patched, encoding="utf-8", newline="\n")
