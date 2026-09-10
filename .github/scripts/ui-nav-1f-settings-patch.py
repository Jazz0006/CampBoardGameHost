from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise AssertionError(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


settings_path = Path("app/src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt")
settings = settings_path.read_text()

settings = replace_once(
    settings,
    "internal fun SettingsScreen(\n",
    "internal fun SettingsContent(\n",
    "rename reusable settings content",
)
settings = replace_once(
    settings,
    "    onRemoveCommonPlayer: (String) -> Unit,\n    onBack: () -> Unit,\n) {",
    "    onRemoveCommonPlayer: (String) -> Unit,\n    modifier: Modifier = Modifier,\n    onBack: (() -> Unit)? = null,\n) {",
    "settings content optional wrapper controls",
)
settings = replace_once(
    settings,
    "        modifier = Modifier\n            .fillMaxSize()",
    "        modifier = modifier\n            .fillMaxSize()",
    "settings content modifier",
)
settings = replace_once(
    settings,
    "                TextButton(onClick = onBack) {\n                    Text(stringResource(R.string.back))\n                }",
    "                onBack?.let { back ->\n                    TextButton(onClick = back) {\n                        Text(stringResource(R.string.back))\n                    }\n                }",
    "optional legacy settings back action",
)

wrapper = '''@Composable
internal fun SettingsScreen(
    languageMode: LanguageMode,
    storytellerAutomationMode: StorytellerAutomationMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onStorytellerAutomationModeChange: (StorytellerAutomationMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    onBack: () -> Unit,
) {
    SettingsContent(
        languageMode = languageMode,
        storytellerAutomationMode = storytellerAutomationMode,
        commonPlayers = commonPlayers,
        newCommonPlayerName = newCommonPlayerName,
        onLanguageModeChange = onLanguageModeChange,
        onStorytellerAutomationModeChange = onStorytellerAutomationModeChange,
        onNewCommonPlayerNameChange = onNewCommonPlayerNameChange,
        onAddCommonPlayer = onAddCommonPlayer,
        onRemoveCommonPlayer = onRemoveCommonPlayer,
        onBack = onBack,
    )
}

'''
marker = "@OptIn(ExperimentalLayoutApi::class)\n@Composable\ninternal fun SettingsContent("
if marker not in settings:
    raise AssertionError("settings content marker missing")
settings = settings.replace(marker, wrapper + marker, 1)
settings_path.write_text(settings)

review_path = Path("app/src/main/java/com/codex/campboardgamehost/AppGameReviewScreens.kt")
review = review_path.read_text()
review = replace_once(
    review,
    "internal enum class HostToolTab {\n    Roles,\n    Records,\n    History,\n}",
    "internal enum class HostToolTab {\n    Roles,\n    Records,\n    History,\n    Settings,\n}",
    "HostToolTab Settings entry",
)
review = replace_once(
    review,
    "    history: List<ArchivedGameReview>,\n    initialTab: HostToolTab,\n    onDismiss: () -> Unit,",
    "    history: List<ArchivedGameReview>,\n    initialTab: HostToolTab,\n    settingsContent: @Composable () -> Unit,\n    onDismiss: () -> Unit,",
    "HostGameTools settings slot",
)
review = replace_once(
    review,
    "                            HostToolTab.History -> text(\"历史复盘 ${history.size}\", \"History ${history.size}\")\n                        }",
    "                            HostToolTab.History -> text(\"历史复盘 ${history.size}\", \"History ${history.size}\")\n                            HostToolTab.Settings -> text(\"设置\", \"Settings\")\n                        }",
    "Host Tools Settings tab label",
)
review = replace_once(
    review,
    "                    HostToolTab.History -> {\n                        if (selectedHistory == null) {",
    "                    HostToolTab.History -> {\n                        if (selectedHistory == null) {",
    "Host Tools History anchor",
)
# Insert the Settings branch immediately after the complete History branch and before the when closes.
history_start = review.index("                    HostToolTab.History -> {")
footer_marker = "                Surface(\n                    modifier = Modifier\n                        .fillMaxWidth(),"
footer = review.index(footer_marker, history_start)
segment = review[history_start:footer]
needle = "                    }\n                }\n"
if not segment.endswith(needle):
    raise AssertionError("Host Tools selectedTab when shape changed")
segment = segment[:-len(needle)] + "                    }\n                    HostToolTab.Settings -> {\n                        Box(\n                            modifier = Modifier\n                                .weight(1f)\n                                .fillMaxWidth(),\n                        ) {\n                            settingsContent()\n                        }\n                    }\n                }\n"
review = review[:history_start] + segment + review[footer:]
review_path.write_text(review)

root_path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
root = root_path.read_text()
anchor = """                        history = gameHistory,
                        initialTab = hostToolTab,
                        onDismiss = { showHostTools = false },
"""
settings_slot = """                        history = gameHistory,
                        initialTab = hostToolTab,
                        settingsContent = {
                            SettingsContent(
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
                                onNewCommonPlayerNameChange = { newCommonPlayerName = it },
                                onAddCommonPlayer = ::addCommonPlayer,
                                onRemoveCommonPlayer = ::removeCommonPlayer,
                            )
                        },
                        onDismiss = { showHostTools = false },
"""
root = replace_once(root, anchor, settings_slot, "root SettingsContent composition")
root_path.write_text(root)

# Ownership/composition audit before tests.
settings = settings_path.read_text()
assert settings.count("internal fun SettingsScreen(") == 1
assert settings.count("internal fun SettingsContent(") == 1
assert "onBack: (() -> Unit)? = null" in settings
assert "onBack?.let" in settings

review = review_path.read_text()
assert "HostToolTab.Settings" in review
assert "settingsContent: @Composable () -> Unit" in review
assert "settingsContent()" in review
assert "mutableStateOf" in review  # existing local selectedTab only

root = root_path.read_text()
assert root.count("var languageMode by") == 1
assert root.count("var storytellerAutomationMode by") == 1
assert root.count("var newCommonPlayerName by") == 1
assert "Screen.Settings -> SettingsScreen(" in root
assert "settingsContent = {\n                            SettingsContent(" in root
assert root.count("saveLanguageMode(nextMode)") == 2  # legacy route + Host Tools composition
assert root.count("saveStorytellerAutomationMode(mode)") == 2

print("UI-NAV-1F Settings ownership/composition audit PASS")
