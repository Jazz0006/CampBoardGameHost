from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

raw = TARGET.read_bytes()
if b"\r" in raw:
    raise SystemExit("Refusing to patch CampBoardGameHostApp.kt with CR/CRLF line endings")
text = raw.decode("utf-8")

old_reader = '''private fun archivedGameReviewFromJson(entry: JSONObject): ArchivedGameReview? {
    val snapshot = entry.optJSONObject("snapshot") ?: return null
    val gameKind = enumByName<GameKind>(snapshot.optNullableString("currentGameKind")) ?: return null
    val cards = snapshot.optJSONArray("cards")?.toPlayerCards().orEmpty()
    if (cards.isEmpty()) return null
    return ArchivedGameReview(
        id = entry.optLong("id", entry.optLong("archivedAtMillis", 0L)),
        archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
        gameKind = gameKind,
        round = snapshot.optInt("round", 1).coerceAtLeast(1),
        cards = cards,
        records = snapshot.optJSONArray("records")?.toEliminationRecords().orEmpty(),
        events = snapshot.optJSONArray("clocktowerEvents")?.toClocktowerEvents().orEmpty(),
        outcome = gameOutcomeFromJson(snapshot.optJSONObject("gameOutcome")),
    )
}
'''
new_reader = '''private fun archivedGameReviewFromJson(entry: JSONObject): ArchivedGameReview? =
    GameArchiveJsonCodec.decodeEntry(entry, ::clocktowerRoleByName)
'''

old_store = '''private fun Context.archiveGame(snapshot: JSONObject): List<ArchivedGameReview> {
    if (snapshot.optJSONArray("cards")?.length() == 0) return loadGameHistory()
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = runCatching { JSONArray(prefs.getString(GAME_HISTORY_KEY, "[]")) }.getOrDefault(JSONArray())
    val archivedAt = System.currentTimeMillis()
    val next = JSONArray().apply {
        put(JSONObject().apply {
            put("id", archivedAt)
            put("archivedAtMillis", archivedAt)
            put("snapshot", JSONObject(snapshot.toString()))
        })
        for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
            existing.optJSONObject(index)?.let(::put)
        }
    }
    prefs.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
    return loadGameHistory()
}
'''
new_store = '''private fun Context.archiveGame(record: GameArchiveRecord): List<ArchivedGameReview> {
    if (record.cards.isEmpty()) return loadGameHistory()
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = runCatching { JSONArray(prefs.getString(GAME_HISTORY_KEY, "[]")) }.getOrDefault(JSONArray())
    val archivedAt = System.currentTimeMillis()
    val next = JSONArray().apply {
        put(
            GameArchiveJsonCodec.encodeEntry(
                record = record,
                id = archivedAt,
                archivedAtMillis = archivedAt,
            ),
        )
        for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
            existing.optJSONObject(index)?.let(::put)
        }
    }
    prefs.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
    return loadGameHistory()
}
'''

old_restart = '''    fun archiveCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
        if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false
        invalidateA4SessionBoundary()
        gameHistory = baseContext.archiveGame(activeGameSnapshotJson())
        clearSavedGameState()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
    }
'''
new_restart = '''    fun archiveCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
        if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false
        invalidateA4SessionBoundary()
        gameHistory = baseContext.archiveGame(
            GameArchiveRecord(
                gameKind = currentGameKind,
                round = round,
                cards = cards.toList(),
                records = records.toList(),
                events = clocktowerEvents.toList(),
                outcome = gameOutcome,
            ),
        )
        clearSavedGameState()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
    }
'''

replacements = [
    ("archive reader", old_reader, new_reader),
    ("archive store", old_store, new_store),
    ("restart archive wiring", old_restart, new_restart),
]

for label, old, _ in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")

recovery_save = '''    fun persistActiveGameStateIfNeeded(): Boolean {
        if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
        return baseContext.saveActiveGameState(activeGameSnapshotJson())
    }
'''
lifecycle_trigger = '''            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                latestPersistActiveGameState()
            }
'''
if text.count(recovery_save) != 1:
    raise SystemExit("Active recovery save anchor drifted before patch")
if text.count(lifecycle_trigger) != 1:
    raise SystemExit("Lifecycle recovery trigger anchor drifted before patch")

for _, old, new in replacements:
    text = text.replace(old, new, 1)

if text.count(new_reader) != 1:
    raise SystemExit("Archive reader postcondition failed")
if text.count("private fun Context.archiveGame(record: GameArchiveRecord)") != 1:
    raise SystemExit("Archive store postcondition failed")
if text.count(recovery_save) != 1:
    raise SystemExit("Active recovery save changed unexpectedly")
if text.count(lifecycle_trigger) != 1:
    raise SystemExit("Lifecycle recovery trigger changed unexpectedly")

restart_start = text.index("    fun archiveCurrentGameForRestart(): Boolean {")
restart_end = text.index("\n    fun archiveAndReturnToPlayerManagement()", restart_start)
restart_block = text[restart_start:restart_end]
if "activeGameSnapshotJson()" in restart_block:
    raise SystemExit("Archive restart still consumes the active recovery snapshot")
if "GameArchiveRecord(" not in restart_block:
    raise SystemExit("Archive restart does not construct the independent archive record")

TARGET.write_text(text, encoding="utf-8", newline="\n")
