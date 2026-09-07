from pathlib import Path

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
PRIMITIVES = Path("app/src/main/java/com/codex/campboardgamehost/persistence/AppJsonPrimitives.kt")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected CR/CRLF in {path}; refusing implicit normalization")
    return raw.decode("utf-8")


def require_once(text: str, marker: str, label: str) -> None:
    count = text.count(marker)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label}, found {count}")


def remove_between(text: str, start_marker: str, end_marker: str, label: str) -> tuple[str, str]:
    require_once(text, start_marker, f"{label} start marker")
    require_once(text, end_marker, f"{label} end marker")
    start = text.index(start_marker)
    end = text.index(end_marker)
    if start >= end:
        raise SystemExit(f"{label} markers are out of order")
    return text[:start] + text[end:], text[start:end]


app = read_lf(APP)
primitives = read_lf(PRIMITIVES)

# PS4.1 is deliberately narrow: the dead broad active snapshot writer plus
# serialization helpers that are consumed only by that writer. Decoder-side
# legacy helpers are retained for later PS4 slices rather than broadened here.
active_start = "    fun activeGameSnapshotJson(): JSONObject = JSONObject().apply {\n"
active_end = "    fun activeGameRecoverySnapshot(): RecoverySnapshot {\n"
require_once(app, active_start, "activeGameSnapshotJson definition")
require_once(app, active_end, "activeGameRecoverySnapshot definition")
active_start_index = app.index(active_start)
active_end_index = app.index(active_end)
if active_start_index >= active_end_index:
    raise SystemExit("active snapshot markers are out of order")
active_block = app[active_start_index:active_end_index]

required_active_tokens = (
    'put("version", ACTIVE_GAME_STATE_VERSION)',
    "PersistedActiveGameIdentityJsonCodec.ROOT_KEY",
    "CommittedClocktowerSetupPersistence.ROOT_KEY",
    "TroubleBrewingSetupCompletionPersistence.ROOT_KEY",
    "ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY",
    'put("clocktowerRulesetRoleIds"',
    "ClocktowerNightCheckpoint(",
    "ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY",
    'putNullableBoolean("clocktowerArtistTruthfulAnswer"',
    'putNullableBoolean("clocktowerArtistShownAnswer"',
    'put("cards", playerCardsToJsonArray(cards))',
    'put("records", eliminationRecordsToJsonArray(records))',
    'put("clocktowerEvents", clocktowerEventsToJsonArray(clocktowerEvents))',
    'put("clocktowerEpistemicObservations", recordedEpistemicObservationsToJsonArray(clocktowerEpistemicObservations))',
)
missing = [token for token in required_active_tokens if token not in active_block]
if missing:
    raise SystemExit(f"Active snapshot body no longer matches audited shape; missing: {missing}")

# Every .toJson() occurrence in this App file belongs to the dead writer's
# private encode-helper chain: four private encoder definitions/calls plus the
# active snapshot's GameOutcome call. If that set changes, stop for re-audit.
if app.count(".toJson()") != 5:
    raise SystemExit(f"Expected exactly five App .toJson() occurrences, found {app.count('.toJson()')}")

# Validate all private encoder boundaries before mutating anything.
encoder_boundaries = (
    (
        "private fun PlayerCard.toJson(): JSONObject = JSONObject().apply {\n",
        "private fun playerCardFromJson(json: JSONObject): PlayerCard? {\n",
        "PlayerCard encoder",
    ),
    (
        "private fun playerCardsToJsonArray(cards: List<PlayerCard>): JSONArray {\n",
        "private fun JSONArray.toPlayerCards(): List<PlayerCard> = buildList {\n",
        "PlayerCard array encoder",
    ),
    (
        "private fun EliminationRecord.toJson(): JSONObject = JSONObject().apply {\n",
        "private fun eliminationRecordFromJson(json: JSONObject): EliminationRecord? {\n",
        "EliminationRecord encoder",
    ),
    (
        "private fun eliminationRecordsToJsonArray(records: List<EliminationRecord>): JSONArray {\n",
        "private fun JSONArray.toEliminationRecords(): List<EliminationRecord> = buildList {\n",
        "EliminationRecord array encoder",
    ),
    (
        "private fun GameOutcome.toJson(): JSONObject = JSONObject().apply {\n",
        "private fun ClocktowerEvent.toJson(): JSONObject = JSONObject().apply {\n",
        "GameOutcome encoder",
    ),
    (
        "private fun ClocktowerEvent.toJson(): JSONObject = JSONObject().apply {\n",
        "private fun clocktowerEventFromJson(json: JSONObject): ClocktowerEvent? {\n",
        "ClocktowerEvent encoder",
    ),
    (
        "private fun clocktowerEventsToJsonArray(events: List<ClocktowerEvent>): JSONArray {\n",
        "private fun JSONArray.toClocktowerEvents(): List<ClocktowerEvent> = buildList {\n",
        "ClocktowerEvent array encoder",
    ),
    (
        "private fun recordedEpistemicObservationsToJsonArray(records: List<RecordedEpistemicObservation>): JSONArray = JSONArray().apply {\n",
        "private fun JSONArray.toRecordedEpistemicObservations(): List<RecordedEpistemicObservation> = buildList {\n",
        "epistemic observation encoder",
    ),
)
for start, end, label in encoder_boundaries:
    require_once(app, start, f"{label} start")
    require_once(app, end, f"{label} end")
    if app.index(start) >= app.index(end):
        raise SystemExit(f"{label} markers are out of order")

nullable_boolean_block = """internal fun JSONObject.putNullableBoolean(key: String, value: Boolean?) {
    put(key, value ?: JSONObject.NULL)
}

"""
require_once(primitives, nullable_boolean_block, "putNullableBoolean helper")

# First remove the broad writer. Then remove only its proven-exclusive private
# encode helpers. Deletion uses semantic function boundaries, not line numbers.
app, removed_active = remove_between(app, active_start, active_end, "active snapshot")
for start, end, label in encoder_boundaries:
    app, _ = remove_between(app, start, end, label)
primitives = primitives.replace(nullable_boolean_block, "", 1)

# Required absence after cleanup.
for token in (
    "activeGameSnapshotJson(",
    "private fun PlayerCard.toJson()",
    "playerCardsToJsonArray(",
    "private fun EliminationRecord.toJson()",
    "eliminationRecordsToJsonArray(",
    "private fun GameOutcome.toJson()",
    "private fun ClocktowerEvent.toJson()",
    "clocktowerEventsToJsonArray(",
    "recordedEpistemicObservationsToJsonArray(",
):
    if token in app:
        raise SystemExit(f"PS4.1 dead encoder token remains in App: {token}")
if "putNullableBoolean(" in primitives:
    raise SystemExit("putNullableBoolean helper remains after cleanup")

# Explicit non-goal / retained-boundary assertions.
for token in (
    "fun activeGameRecoverySnapshot(): RecoverySnapshot",
    "LegacyRestoreCompatibility(",
    "RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())",
    "playerCardFromJson(",
    "toPlayerCards()",
    "eliminationRecordFromJson(",
    "toEliminationRecords()",
    "clocktowerEventFromJson(",
    "toClocktowerEvents()",
    "toRecordedEpistemicObservations()",
    "gameOutcomeFromJson(",
):
    if token not in app:
        raise SystemExit(f"PS4.1 unexpectedly damaged retained boundary: {token}")

APP.write_text(app, encoding="utf-8", newline="\n")
PRIMITIVES.write_text(primitives, encoding="utf-8", newline="\n")
print(f"Removed activeGameSnapshotJson block: {removed_active.count(chr(10))} lines")
print("Removed only its private encode-helper chain and putNullableBoolean primitive")
