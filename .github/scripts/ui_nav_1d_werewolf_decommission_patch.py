from pathlib import Path

ROOT = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
SEATING = Path("app/src/main/java/com/codex/campboardgamehost/SeatingFirstSetupUi.kt")
LEGACY_SETUP = Path("app/src/main/java/com/codex/campboardgamehost/AppPlayerSetupScreens.kt")
VALIDATOR = Path("app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameSaveInvariantValidators.kt")
VALIDATOR_TEST = Path("app/src/test/java/com/codex/campboardgamehost/persistence/ActiveGameSaveInvariantValidatorTest.kt")
LAST_WORDS = Path("app/src/main/java/com/codex/campboardgamehost/LastWordsMode.kt")

WEREWOLF_PRODUCTION = [
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfBoardRegistry.kt"),
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfFlowPlanner.kt"),
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfGameSupport.kt"),
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt"),
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfRoleRegistry.kt"),
    Path("app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfRuleOptions.kt"),
]
WEREWOLF_TESTS = [
    Path("app/src/test/java/com/codex/campboardgamehost/werewolf/WerewolfLegacyPlannerDifferentialTest.kt"),
    Path("app/src/test/java/com/codex/campboardgamehost/werewolf/WerewolfRegistryFlowPlannerTest.kt"),
]


def read(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"{path}: non-LF line endings")
    return raw.decode("utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


def replace_exact(text: str, old: str, new: str, *, label: str, expected: int = 1) -> str:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact matches, got {count}")
    return text.replace(old, new)


def remove_between(text: str, start: str, end: str, *, label: str) -> str:
    if text.count(start) != 1:
        raise SystemExit(f"{label}: start marker count {text.count(start)}, expected 1")
    if text.count(end) != 1:
        raise SystemExit(f"{label}: end marker count {text.count(end)}, expected 1")
    start_index = text.index(start)
    end_index = text.index(end, start_index)
    if end_index <= start_index:
        raise SystemExit(f"{label}: invalid marker ordering")
    return text[:start_index] + text[end_index:]


# --- App root: remove reachable Werewolf runtime wiring, preserve legacy schema discriminators. ---
root = read(ROOT)
for line, label in [
    ("    WerewolfSettings,\n", "Screen.WerewolfSettings enum"),
    ("    WerewolfJudge,\n", "Screen.WerewolfJudge enum"),
    ("internal const val MIN_WEREWOLF_PLAYERS = 4\n", "MIN_WEREWOLF_PLAYERS"),
]:
    root = replace_exact(root, line, "", label=label)

root = replace_exact(
    root,
    """    Screen.WerewolfJudge,\n""",
    "",
    label="active screen WerewolfJudge",
)
root = replace_exact(
    root,
    """    Screen.WerewolfSettings,\n""",
    "",
    label="inactive screen WerewolfSettings",
)
root = replace_exact(
    root,
    """    val activeGameWerewolfSaveValidator = remember {\n        WerewolfActiveGameSaveValidator(WerewolfRoleRegistry.builtIn())\n    }\n""",
    "",
    label="Werewolf save validator remember",
)

root = replace_exact(
    root,
    """    var werewolfCount by remember { mutableStateOf(1) }\n    var includeSeer by remember { mutableStateOf(true) }\n    var includeWitch by remember { mutableStateOf(false) }\n    var includeHunter by remember { mutableStateOf(false) }\n    var lastWordsMode by remember { mutableStateOf(LastWordsMode.FirstDay) }\n    var lastWordsPromptNames by remember { mutableStateOf<List<String>>(emptyList()) }\n    var currentDealIndex by remember { mutableStateOf(0) }\n    var round by remember { mutableStateOf(1) }\n    var selectedElimination by remember { mutableStateOf<String?>(null) }\n    var werewolfJudgeStepIndex by remember { mutableStateOf(0) }\n    var pendingNightDeath by remember { mutableStateOf<String?>(null) }\n    var seerCheckTarget by remember { mutableStateOf<String?>(null) }\n    var witchSaveUsed by remember { mutableStateOf(false) }\n    var witchPoisonUsed by remember { mutableStateOf(false) }\n    var witchSavedTonight by remember { mutableStateOf(false) }\n    var witchPoisonTarget by remember { mutableStateOf<String?>(null) }\n    var hunterShotTarget by remember { mutableStateOf<String?>(null) }\n    var selectedDayExile by remember { mutableStateOf<String?>(null) }\n    var clocktowerPhase by remember { mutableStateOf(ClocktowerPhase.FirstNight) }\n""",
    """    var lastWordsMode by remember { mutableStateOf(LastWordsMode.FirstDay) }\n    var currentDealIndex by remember { mutableStateOf(0) }\n    var round by remember { mutableStateOf(1) }\n    var selectedElimination by remember { mutableStateOf<String?>(null) }\n    var clocktowerPhase by remember { mutableStateOf(ClocktowerPhase.FirstNight) }\n""",
    label="Werewolf runtime state",
)

root = replace_exact(
    root,
    """            GameKind.Werewolf -> {\n                activeGameWerewolfSaveValidator.validate(\n                    assignedRoles = cards.map { card -> card.role },\n                    werewolfCount = werewolfCount,\n                    includeSeer = includeSeer,\n                    includeWitch = includeWitch,\n                    includeHunter = includeHunter,\n                )\n            }\n""",
    """            GameKind.Werewolf -> error("Werewolf runtime has been removed.")\n""",
    label="Werewolf recovery save validation branch",
)
root = remove_between(
    root,
    "            GameKind.Werewolf -> WerewolfRecovery(\n",
    "            GameKind.Clocktower -> ClocktowerRecovery(\n",
    label="Werewolf recovery snapshot branch",
)
root = replace_exact(
    root,
    "            GameKind.Clocktower -> ClocktowerRecovery(\n",
    "            GameKind.Werewolf -> error(\"Werewolf runtime has been removed.\")\n            GameKind.Clocktower -> ClocktowerRecovery(\n",
    label="fail-closed legacy Werewolf recovery snapshot branch",
)

root = replace_exact(
    root,
    """        undercoverCount = 1\n        includeBlank = false\n        lastWordsMode = LastWordsMode.FirstDay\n        lastWordsPromptNames = emptyList()\n        selectedElimination = null\n\n        werewolfCount = 1\n        includeSeer = true\n        includeWitch = false\n        includeHunter = false\n        werewolfJudgeStepIndex = 0\n        pendingNightDeath = null\n        seerCheckTarget = null\n        witchSaveUsed = false\n        witchPoisonUsed = false\n        witchSavedTonight = false\n        witchPoisonTarget = null\n        hunterShotTarget = null\n        selectedDayExile = null\n\n        selectedClocktowerScript = null\n""",
    """        undercoverCount = 1\n        includeBlank = false\n        lastWordsMode = LastWordsMode.FirstDay\n        selectedElimination = null\n\n        selectedClocktowerScript = null\n""",
    label="recovery runtime reset",
)

root = remove_between(
    root,
    "    fun maxWerewolfFor(count: Int): Int {\n",
    "    fun addCurrentPlayer(name: String) {\n",
    label="Werewolf setup helper functions",
)
root = replace_exact(root, "            clampWerewolfSettings()\n", "", label="Werewolf clamp calls", expected=2)
root = replace_exact(
    root,
    """        selectedElimination = null\n        werewolfJudgeStepIndex = 0\n        lastWordsPromptNames = emptyList()\n        pendingNightDeath = null\n        seerCheckTarget = null\n        witchSaveUsed = false\n        witchPoisonUsed = false\n        witchSavedTonight = false\n        witchPoisonTarget = null\n        hunterShotTarget = null\n        selectedDayExile = null\n        clocktowerPhase = ClocktowerPhase.FirstNight\n""",
    """        selectedElimination = null\n        clocktowerPhase = ClocktowerPhase.FirstNight\n""",
    label="resetDealState Werewolf reset",
)

root = remove_between(
    root,
    "    fun startWerewolfGame() {\n",
    "    fun startTroubleBrewingGame() {\n",
    label="startWerewolfGame",
)
root = replace_exact(root, "        lastWordsPromptNames = emptyList()\n", "", label="archive Werewolf prompt reset")
root = replace_exact(
    root,
    "            GameKind.Werewolf -> startWerewolfGame()\n",
    "            GameKind.Werewolf -> error(\"Werewolf runtime has been removed.\")\n",
    label="restart Werewolf branch",
)
root = replace_exact(
    root,
    """        Screen.UndercoverSettings,\n        Screen.WerewolfSettings,\n        Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings\n""",
    """        Screen.UndercoverSettings,\n        Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings\n""",
    label="host seating Werewolf settings back route",
)
root = replace_exact(
    root,
    """                    onOpenWerewolfSettings = {\n                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Werewolf)\n                        screen = Screen.WerewolfSettings\n                    },\n""",
    "",
    label="game selection Werewolf callback",
)
root = remove_between(
    root,
    "                    Screen.WerewolfSettings -> WerewolfSettingsScreen(\n",
    "                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(\n",
    label="Werewolf settings route",
)
root = replace_exact(
    root,
    "                                GameKind.Werewolf -> Screen.WerewolfJudge\n",
    "                                GameKind.Werewolf -> error(\"Werewolf runtime has been removed.\")\n",
    label="identity completion Werewolf route",
)
root = remove_between(
    root,
    "                    Screen.WerewolfJudge -> WerewolfJudgeScreen(\n",
    "                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(\n",
    label="Werewolf judge route",
)
write(ROOT, root)

# --- Current seating-first game selection: remove the reachable Werewolf entry. ---
seating = read(SEATING)
seating = replace_exact(
    seating,
    "    onOpenWerewolfSettings: () -> Unit,\n",
    "",
    label="seating-first Werewolf callback parameter",
)
seating = replace_exact(
    seating,
    """                    OutlinedButton(\n                        onClick = onOpenWerewolfSettings,\n                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("狼人杀", "Werewolf"))\n                    }\n""",
    "",
    label="seating-first Werewolf button",
)
write(SEATING, seating)

# --- Legacy setup surface: remove its dormant Werewolf entry too, preventing accidental resurrection. ---
legacy_setup = read(LEGACY_SETUP)
legacy_setup = replace_exact(
    legacy_setup,
    "    onOpenWerewolfSettings: () -> Unit,\n",
    "",
    label="legacy setup Werewolf callback parameter",
)
legacy_setup = replace_exact(
    legacy_setup,
    "    val canStartWerewolf = playerCount >= MIN_WEREWOLF_PLAYERS\n",
    "",
    label="legacy setup Werewolf availability",
)
legacy_setup = replace_exact(
    legacy_setup,
    """                    OutlinedButton(\n                        onClick = onOpenWerewolfSettings,\n                        enabled = canStartWerewolf,\n                        modifier = Modifier\n                            .fillMaxWidth()\n                            .height(52.dp),\n                        shape = RoundedCornerShape(14.dp),\n                    ) {\n                        Text(\n                            if (canStartWerewolf) {\n                                stringResource(R.string.game_werewolf)\n                            } else {\n                                stringResource(R.string.need_werewolf_min_players, MIN_WEREWOLF_PLAYERS)\n                            }\n                        )\n                    }\n""",
    "",
    label="legacy setup Werewolf button",
)
write(LEGACY_SETUP, legacy_setup)

# --- Active-save runtime validator was only for a runtime that can no longer start. ---
validator = read(VALIDATOR)
marker = "\ninternal class WerewolfActiveGameSaveValidator(\n"
if validator.count(marker) != 1:
    raise SystemExit(f"Werewolf validator marker count {validator.count(marker)}, expected 1")
validator = validator.split(marker, 1)[0].rstrip() + "\n"
write(VALIDATOR, validator)

validator_test = read(VALIDATOR_TEST)
validator_test = replace_exact(
    validator_test,
    "    private val roleRegistry = WerewolfRoleRegistry.builtIn()\n",
    "",
    label="Werewolf validator test registry",
)
validator_test = remove_between(
    validator_test,
    "    @Test\n    fun `Werewolf save validation accepts a mechanically exact assigned deck`() {\n",
    "    private fun assertFails(block: () -> Unit) {\n",
    label="Werewolf active-save validator tests",
)
write(VALIDATOR_TEST, validator_test)

# --- Keep only the shared legacy/Undercover last-words option in neutral ownership. ---
if LAST_WORDS.exists():
    raise SystemExit(f"{LAST_WORDS}: already exists")
LAST_WORDS.write_text(
    """package com.codex.campboardgamehost\n\ninternal enum class LastWordsMode {\n    None,\n    FirstDay,\n    FirstTwoDays,\n    Always,\n}\n\ninternal fun LastWordsMode.labelResId(): Int = when (this) {\n    LastWordsMode.None -> R.string.last_words_none\n    LastWordsMode.FirstDay -> R.string.last_words_first_day\n    LastWordsMode.FirstTwoDays -> R.string.last_words_first_two_days\n    LastWordsMode.Always -> R.string.last_words_always\n}\n""",
    encoding="utf-8",
    newline="\n",
)

# --- Delete the actual Werewolf runtime and its runtime-only planner/registry tests. ---
for path in WEREWOLF_PRODUCTION + WEREWOLF_TESTS:
    if not path.is_file():
        raise SystemExit(f"{path}: expected runtime file is missing")
    path.unlink()

# Fail closed if any reachable/runtime symbol survived in production/test Kotlin.
main_and_test = list(Path("app/src/main").rglob("*.kt")) + list(Path("app/src/test").rglob("*.kt"))
for symbol in [
    "WerewolfSettingsScreen",
    "WerewolfJudgeScreen",
    "WerewolfJudgeStep",
    "WerewolfFlowPlanner",
    "WerewolfBoardRegistry",
    "WerewolfRoleRegistry",
    "WerewolfRuleOptions",
    "WerewolfActiveGameSaveValidator",
    "werewolfRolesFor",
    "startWerewolfGame",
    "MIN_WEREWOLF_PLAYERS",
    "onOpenWerewolfSettings",
]:
    hits = [str(path) for path in main_and_test if symbol in read(path)]
    if hits:
        raise SystemExit(f"runtime symbol {symbol!r} survived in: {hits}")

root_after = read(ROOT)
for symbol in [
    "Screen.WerewolfSettings",
    "Screen.WerewolfJudge",
    "werewolfCount",
    "includeSeer",
    "includeWitch",
    "includeHunter",
    "lastWordsPromptNames",
    "werewolfJudgeStepIndex",
    "pendingNightDeath",
    "seerCheckTarget",
    "witchSaveUsed",
    "witchPoisonUsed",
    "witchSavedTonight",
    "witchPoisonTarget",
    "hunterShotTarget",
    "selectedDayExile",
]:
    if symbol in root_after:
        raise SystemExit(f"App root Werewolf runtime state survived: {symbol}")

# Legacy compatibility is intentional: old archive/recovery JSON still parses the discriminator.
if "GameKind.Werewolf" not in root_after:
    raise SystemExit("Expected fail-closed GameKind.Werewolf compatibility branches to remain")
recovery_source = read(Path("app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryRestorePlanner.kt"))
if "is WerewolfRecovery -> RecoveryPreparationResult.Invalid" not in recovery_source:
    raise SystemExit("Werewolf legacy recovery rejection contract drifted")

print("Werewolf runtime decommission patch applied")
