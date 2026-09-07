from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line endings; refusing implicit large-file normalization")

text = raw.decode("utf-8")

old_load_preview = '''private fun Context.loadSavedGamePreview(localizedContext: Context): SavedGamePreview? {
    val json = loadActiveGameStateJson() ?: return null
    val preview = runCatching { savedGamePreviewFromJson(localizedContext, json) }
        .getOrElse {
            clearActiveGameState()
            null
        }
    if (preview == null) {
        clearActiveGameState()
    }
    return preview
}'''
new_load_preview = '''private fun Context.loadSavedGamePreview(localizedContext: Context): SavedGamePreview? =
    RecoveryPreviewLoader.load(
        raw = loadActiveGameStateJson(),
        prepare = { raw -> prepareCurrentRecoveryPlan(raw) },
        clearRejected = { clearActiveGameState() },
    )?.toSavedGamePreview(localizedContext)'''

old_raw_preview = '''private fun savedGamePreviewFromJson(context: Context, json: JSONObject): SavedGamePreview? {
    if (!ActiveGamePersistenceCoordinator.isSupportedVersion(json.optInt("version", 0))) return null
    val gameKind = enumByName<GameKind>(json.optNullableString("currentGameKind")) ?: return null
    val screen = enumByName<Screen>(json.optNullableString("screen")) ?: return null
    val playerCount = json.optJSONArray("cards")?.length() ?: 0
    if (playerCount == 0) return null
    if (
        gameKind == GameKind.Clocktower &&
        enumByName<ClocktowerScript>(json.optNullableString("currentClocktowerScript")) == ClocktowerScript.TroubleBrewing &&
        (!json.has(CommittedClocktowerSetupPersistence.ROOT_KEY) ||
            !json.has(TroubleBrewingSetupCompletionPersistence.ROOT_KEY))
    ) return null
    val round = json.optInt("round", 1)
    val gameName = when (gameKind) {
        GameKind.Undercover -> context.getString(R.string.game_who_is_undercover)
        GameKind.Werewolf -> context.getString(R.string.game_werewolf)
        GameKind.Clocktower -> context.getString(R.string.game_clocktower)
    }
    val stage = when {
        json.optBoolean("showResults", false) || json.optJSONObject("gameOutcome") != null ->
            context.getString(R.string.saved_game_stage_results)
        screen == Screen.PassPhone || screen == Screen.RevealCard ->
            context.getString(R.string.saved_game_stage_dealing)
        gameKind == GameKind.Clocktower -> {
            when (enumByName<ClocktowerPhase>(json.optNullableString("clocktowerPhase")) ?: ClocktowerPhase.FirstNight) {
                ClocktowerPhase.FirstNight -> context.getString(R.string.clocktower_phase_first_night)
                ClocktowerPhase.Dawn -> context.getString(R.string.saved_game_stage_dawn)
                ClocktowerPhase.Day -> context.getString(R.string.clocktower_phase_day, round)
                ClocktowerPhase.Night -> context.getString(R.string.clocktower_phase_night, round)
            }
        }
        else -> context.getString(R.string.round_format, round)
    }
    return SavedGamePreview(
        title = context.getString(R.string.resume_saved_game),
        subtitle = context.getString(R.string.saved_game_summary_format, gameName, stage, playerCount),
        savedAtLabel = json.optLong("savedAtMillis", 0L)
            .takeIf { it > 0L }
            ?.let { savedAtMillis ->
                val locale = context.resources.configuration.locales[0]
                val pattern = if (locale.language == "en") "MMM d, HH:mm" else "M月d日 HH:mm"
                java.text.SimpleDateFormat(pattern, locale).format(java.util.Date(savedAtMillis))
            },
    )
}'''

token_old = 'compatibilityToken = "active-v${ACTIVE_GAME_STATE_VERSION}:${currentGameKind.name}",'
token_new = 'compatibilityToken = RecoveryCompatibilityToken.currentFor(currentGameKind),'

anchors = (
    ("loadSavedGamePreview", old_load_preview, new_load_preview),
    ("legacy raw preview", old_raw_preview, ""),
    ("compatibility token", token_old, token_new),
)

for label, old, _ in anchors:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")

for _, old, new in anchors:
    text = text.replace(old, new, 1)

if "savedGamePreviewFromJson" in text:
    raise SystemExit("Legacy raw preview reader remains after patch")
if "RecoveryPreviewLoader.load(" not in text:
    raise SystemExit("Typed preview loader wiring is missing")
if "prepare = { raw -> prepareCurrentRecoveryPlan(raw) }" not in text:
    raise SystemExit("Preview does not share the current typed recovery preparation path")
if token_new not in text:
    raise SystemExit("Recovery writer does not use the shared compatibility token")
if "fun restoreSavedGame()" not in text:
    raise SystemExit("PS3.2 must not remove or rewrite the restore entry point")

path.write_text(text, encoding="utf-8", newline="\n")
