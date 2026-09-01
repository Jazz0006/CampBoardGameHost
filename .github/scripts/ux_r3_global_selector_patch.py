from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
EXPECTED_BLOB = "d53437fa7a9760dd460cc5600470163cf35fa4fd"

text = TARGET.read_text(encoding="utf-8")
if "\r" in text:
    raise SystemExit("Refusing to patch CampBoardGameHostApp.kt with CRLF/mixed newlines")

replacements = [
    (
        "import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode\n",
        "import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode\n"
        "import com.codex.campboardgamehost.clocktower.domain.StorytellerRecommendationUxPolicy\n",
    ),
    (
        "    var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }\n"
        "    val automaticStorytellerInfo = storytellerAutomationMode.isAutomatic\n"
        "    val context = remember(languageMode) { baseContext.localized(languageMode) }\n",
        "    var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }\n"
        "    val storytellerRecommendationUxPolicy =\n"
        "        StorytellerRecommendationUxPolicy.fromLegacyMode(storytellerAutomationMode)\n"
        "    val automaticStorytellerInfo = storytellerRecommendationUxPolicy.automaticExecution\n"
        "    val context = remember(languageMode) { baseContext.localized(languageMode) }\n",
    ),
    (
        "                        automaticStorytellerInfo = automaticStorytellerInfo,\n"
        "                        automaticStorytellerStyle = storytellerAutomationMode.style ?: RecommendationStyle.BALANCED,\n",
        "                        automaticStorytellerInfo = automaticStorytellerInfo,\n"
        "                        automaticStorytellerStyle = storytellerRecommendationUxPolicy.recommendationStyle,\n",
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exact anchor once, found {count}: {old[:120]!r}")
    text = text.replace(old, new, 1)

TARGET.write_text(text, encoding="utf-8", newline="\n")
print("UX-R3 exact-anchor App root patch applied")
