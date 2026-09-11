from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")


def replace_exact(label: str, old: str, new: str, expected_count: int = 1) -> None:
    global text
    count = text.count(old)
    if count != expected_count:
        raise SystemExit(f"{label}: expected {expected_count} anchor(s), found {count}")
    text = text.replace(old, new)


replace_exact(
    "experience-mode import",
    """import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerRecommendationUxPolicy
""",
    """import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerRecommendationUxPolicy
""",
)

replace_exact(
    "experience-mode prefs keys",
    """private const val AUTOMATIC_STORYTELLER_INFO_KEY = \"automatic_storyteller_info\"
private const val STORYTELLER_AUTOMATION_MODE_KEY = \"storyteller_automation_mode\"
""",
    """private const val STORYTELLER_EXPERIENCE_MODE_KEY = \"storyteller_experience_mode\"
""",
)

replace_exact(
    "experience-mode persistence",
    """private fun Context.loadStorytellerAutomationMode(): StorytellerAutomationMode {
    val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val stored = preferences.getString(STORYTELLER_AUTOMATION_MODE_KEY, null)
    return StorytellerAutomationMode.entries.firstOrNull { it.prefsValue == stored }
        ?: if (preferences.getBoolean(AUTOMATIC_STORYTELLER_INFO_KEY, false)) {
            StorytellerAutomationMode.AUTO_BALANCED
        } else {
            StorytellerAutomationMode.MANUAL
        }
}

private fun Context.saveStorytellerAutomationMode(mode: StorytellerAutomationMode) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(STORYTELLER_AUTOMATION_MODE_KEY, mode.prefsValue)
        .remove(AUTOMATIC_STORYTELLER_INFO_KEY)
        .apply()
}
""",
    """private fun Context.loadStorytellerExperienceMode(): StorytellerExperienceMode =
    StorytellerExperienceMode.fromPrefsValue(
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(STORYTELLER_EXPERIENCE_MODE_KEY, null),
    )

private fun Context.saveStorytellerExperienceMode(mode: StorytellerExperienceMode) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(STORYTELLER_EXPERIENCE_MODE_KEY, mode.prefsValue)
        .apply()
}
""",
)

replace_exact(
    "experience-mode compose state",
    """    var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
    var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }
    val storytellerRecommendationUxPolicy =
        StorytellerRecommendationUxPolicy.fromLegacyMode(storytellerAutomationMode)
""",
    """    var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
    var storytellerExperienceMode by remember { mutableStateOf(baseContext.loadStorytellerExperienceMode()) }
    val storytellerRecommendationUxPolicy =
        StorytellerRecommendationUxPolicy.fromExperienceMode(storytellerExperienceMode)
""",
)

replace_exact(
    "standalone settings wiring",
    """                    Screen.Settings -> SettingsScreen(
                        languageMode = languageMode,
                        storytellerAutomationMode = storytellerAutomationMode,
                        commonPlayers = commonPlayers,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            baseContext.saveLanguageMode(nextMode)
                        },
                        onStorytellerAutomationModeChange = { mode ->
                            storytellerAutomationMode = mode
                            baseContext.saveStorytellerAutomationMode(mode)
                        },
""",
    """                    Screen.Settings -> SettingsScreen(
                        languageMode = languageMode,
                        storytellerExperienceMode = storytellerExperienceMode,
                        commonPlayers = commonPlayers,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            baseContext.saveLanguageMode(nextMode)
                        },
                        onStorytellerExperienceModeChange = { mode ->
                            storytellerExperienceMode = mode
                            baseContext.saveStorytellerExperienceMode(mode)
                        },
""",
)

replace_exact(
    "host-tools settings wiring",
    """                            SettingsContent(
                                languageMode = languageMode,
                                storytellerAutomationMode = storytellerAutomationMode,
                                commonPlayers = commonPlayers,
                                newCommonPlayerName = newCommonPlayerName,
                                onLanguageModeChange = { nextMode ->
                                    languageMode = nextMode
                                    baseContext.saveLanguageMode(nextMode)
                                },
                                onStorytellerAutomationModeChange = { mode ->
                                    storytellerAutomationMode = mode
                                    baseContext.saveStorytellerAutomationMode(mode)
                                },
""",
    """                            SettingsContent(
                                languageMode = languageMode,
                                storytellerExperienceMode = storytellerExperienceMode,
                                commonPlayers = commonPlayers,
                                newCommonPlayerName = newCommonPlayerName,
                                onLanguageModeChange = { nextMode ->
                                    languageMode = nextMode
                                    baseContext.saveLanguageMode(nextMode)
                                },
                                onStorytellerExperienceModeChange = { mode ->
                                    storytellerExperienceMode = mode
                                    baseContext.saveStorytellerExperienceMode(mode)
                                },
""",
)

for forbidden in (
    "StorytellerAutomationMode",
    "AUTOMATIC_STORYTELLER_INFO_KEY",
    "STORYTELLER_AUTOMATION_MODE_KEY",
    "loadStorytellerAutomationMode",
    "saveStorytellerAutomationMode",
):
    if forbidden in text:
        raise SystemExit(f"legacy storyteller automation ownership remains in App root: {forbidden}")

required = (
    "StorytellerExperienceMode",
    "STORYTELLER_EXPERIENCE_MODE_KEY",
    "StorytellerRecommendationUxPolicy.fromExperienceMode(storytellerExperienceMode)",
    "storytellerExperienceMode = storytellerExperienceMode",
    "onStorytellerExperienceModeChange = { mode ->",
)
for token in required:
    if token not in text:
        raise SystemExit(f"missing required UX-MODE-1B token: {token}")

TARGET.write_text(text, encoding="utf-8", newline="\n")
