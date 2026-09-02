from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/mixed line endings in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")


def replace_exact(old: str, new: str, expected_count: int = 1) -> None:
    global text
    count = text.count(old)
    if count != expected_count:
        raise SystemExit(
            f"Exact anchor mismatch: expected {expected_count}, found {count}\n--- anchor ---\n{old}"
        )
    text = text.replace(old, new)


replace_exact(
    """private enum class Screen {
    Landing,
    Setup,
    UndercoverSettings,
""",
    """private enum class Screen {
    Landing,
    Setup,
    GameSelection,
    UndercoverSettings,
""",
)

replace_exact(
    """    val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
    val playerNames = remember { mutableStateListOf<String>() }
    val cards = remember { mutableStateListOf<PlayerCard>() }
""",
    """    val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
    val playerNames = remember { mutableStateListOf<String>() }
    var hostSeatingSetupFlow by remember { mutableStateOf(HostSeatingSetupFlow()) }
    val cards = remember { mutableStateListOf<PlayerCard>() }
""",
)

replace_exact(
    """                        onStartGame = { screen = Screen.Setup },
""",
    """                        onStartGame = {
                            hostSeatingSetupFlow = HostSeatingSetupFlow()
                            screen = Screen.Setup
                        },
""",
)

old_setup = """                    Screen.Setup -> SetupScreen(
                    playerCount = playerCount,
                    savedGamePreview = savedGamePreview,
                    commonPlayers = commonPlayers,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onAddTemporaryPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onResumeSavedGame = ::restoreSavedGame,
                    onDiscardSavedGame = ::clearSavedGameState,
                    onOpenSettings = { screen = Screen.Settings },
                    onOpenUndercoverSettings = { screen = Screen.UndercoverSettings },
                    onOpenWerewolfSettings = { screen = Screen.WerewolfSettings },
                    onOpenClocktowerSettings = { screen = Screen.ClocktowerSettings },
                )
"""
new_setup = """                    Screen.Setup -> SeatingFirstSetupScreen(
                    savedGamePreview = savedGamePreview,
                    commonPlayers = commonPlayers,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onResumeSavedGame = ::restoreSavedGame,
                    onDiscardSavedGame = ::clearSavedGameState,
                    onOpenSettings = { screen = Screen.Settings },
                    onConfirmSeats = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.confirmSeats(playerNames)
                        screen = Screen.GameSelection
                    },
                )

                    Screen.GameSelection -> SeatingFirstGameSelectionScreen(
                    seating = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
                        "Game selection requires confirmed seating"
                    },
                    onBackToSeating = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.reopenSeating()
                        screen = Screen.Setup
                    },
                    onOpenUndercoverSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Undercover)
                        screen = Screen.UndercoverSettings
                    },
                    onOpenWerewolfSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Werewolf)
                        screen = Screen.WerewolfSettings
                    },
                    onOpenClocktowerSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Clocktower)
                        screen = Screen.ClocktowerSettings
                    },
                )
"""
replace_exact(old_setup, new_setup)

replace_exact(
    """                        onBack = { screen = Screen.Setup },
""",
    """                        onBack = {
                            hostSeatingSetupFlow = hostSeatingSetupFlow.returnToGameSelection()
                            screen = Screen.GameSelection
                        },
""",
    expected_count=3,
)

replace_exact(
    """                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
                        playerCount = playerCount,
                        playerNames = playerNames,
""",
    """                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
                        playerCount = playerCount,
                        playerNames = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
                            "Clocktower settings require confirmed seating"
                        }.playerNames,
""",
)

PATH.write_text(text, encoding="utf-8", newline="\n")
