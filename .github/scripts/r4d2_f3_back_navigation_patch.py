from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

text = TARGET.read_text(encoding="utf-8")

helper_anchor = '''    fun promoteDemonSuccessorIfNeeded(
        impDeathWasSelfChosen: Boolean,
    ): String? {
        if (impDeathWasSelfChosen) return null
        return promoteScarletWomanIfNeeded()
    }

    CompositionLocalProvider(LocalContext provides context) {
'''
helper_replacement = '''    fun promoteDemonSuccessorIfNeeded(
        impDeathWasSelfChosen: Boolean,
    ): String? {
        if (impDeathWasSelfChosen) return null
        return promoteScarletWomanIfNeeded()
    }

    fun applyHostSeatingBack(origin: HostSeatingBackOrigin) {
        val transition = hostSeatingBackTransition(
            flow = hostSeatingSetupFlow,
            origin = origin,
        )
        hostSeatingSetupFlow = transition.flow
        screen = when (transition.destination) {
            HostSeatingSetupDestination.Seating -> Screen.Setup
            HostSeatingSetupDestination.GameSelection -> Screen.GameSelection
        }
    }

    val hostSeatingBackOrigin = when (screen) {
        Screen.GameSelection -> HostSeatingBackOrigin.GameSelection
        Screen.UndercoverSettings,
        Screen.WerewolfSettings,
        Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings
        else -> null
    }
    BackHandler(enabled = hostSeatingBackOrigin != null) {
        applyHostSeatingBack(requireNotNull(hostSeatingBackOrigin))
    }

    CompositionLocalProvider(LocalContext provides context) {
'''

selection_back = '''                    onBackToSeating = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.reopenSeating()
                        screen = Screen.Setup
                    },
'''
selection_back_replacement = '''                    onBackToSeating = {
                        applyHostSeatingBack(HostSeatingBackOrigin.GameSelection)
                    },
'''

settings_back = '''                        onBack = {
                            hostSeatingSetupFlow = hostSeatingSetupFlow.returnToGameSelection()
                            screen = Screen.GameSelection
                        },
'''
settings_back_replacement = '''                        onBack = {
                            applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
                        },
'''

checks = [
    (helper_anchor, 1, "host back helper insertion anchor"),
    (selection_back, 1, "game selection visible back anchor"),
    (settings_back, 3, "game settings visible back anchors"),
]
for anchor, expected, label in checks:
    actual = text.count(anchor)
    if actual != expected:
        raise SystemExit(f"{label}: expected {expected} occurrence(s), found {actual}")

text = text.replace(helper_anchor, helper_replacement, 1)
text = text.replace(selection_back, selection_back_replacement, 1)
text = text.replace(settings_back, settings_back_replacement)

TARGET.write_text(text, encoding="utf-8")
print("Patched Game Selection/settings visible Back and Android system Back onto one typed seating transition.")
