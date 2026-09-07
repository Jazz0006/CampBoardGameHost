from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CR/CRLF source; refusing implicit large-file normalization")

text = raw.decode("utf-8")
start_marker = "    fun activeGameSnapshotJson(): JSONObject = JSONObject().apply {\n"
end_marker = "    fun activeGameRecoverySnapshot(): RecoverySnapshot {\n"

if text.count(start_marker) != 1:
    raise SystemExit(f"Expected exactly one activeGameSnapshotJson start marker, found {text.count(start_marker)}")
if text.count(end_marker) != 1:
    raise SystemExit(f"Expected exactly one activeGameRecoverySnapshot end marker, found {text.count(end_marker)}")

start = text.index(start_marker)
end = text.index(end_marker)
if start >= end:
    raise SystemExit("active snapshot markers are out of order")

removed = text[start:end]
required_removed_tokens = (
    'put("version", ACTIVE_GAME_STATE_VERSION)',
    "PersistedActiveGameIdentityJsonCodec.ROOT_KEY",
    "CommittedClocktowerSetupPersistence.ROOT_KEY",
    "TroubleBrewingSetupCompletionPersistence.ROOT_KEY",
    "ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY",
    'put("clocktowerRulesetRoleIds"',
    "ClocktowerNightCheckpoint(",
    "ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY",
    'put("cards", playerCardsToJsonArray(cards))',
    'put("clocktowerEvents", clocktowerEventsToJsonArray(clocktowerEvents))',
    'put("clocktowerEpistemicObservations", recordedEpistemicObservationsToJsonArray(clocktowerEpistemicObservations))',
)
missing = [token for token in required_removed_tokens if token not in removed]
if missing:
    raise SystemExit(f"Active snapshot body no longer matches audited shape; missing tokens: {missing}")

# These dependencies are known to be shared with surviving production code. If any
# becomes exclusive to the removed block, stop so Chat can re-audit instead of
# silently broadening PS4.1.
shared_tokens = (
    "ACTIVE_GAME_STATE_VERSION",
    "activeGamePersistenceCoordinator",
    "ActiveGamePersistenceInputs",
    "PersistedActiveGameIdentityJsonCodec",
    "CommittedClocktowerSetupPersistence",
    "TroubleBrewingSetupCompletionPersistence",
    "stringsToJsonArray",
    "putNullableString",
    "ClocktowerSemanticHistoryPersistence",
    "ClocktowerRulesetPersistenceBasisJsonCodec",
    "ClocktowerNightCheckpoint",
    "ClocktowerGhostVoteAuthorityPersistence",
    "putNullableBoolean",
    "playerCardsToJsonArray",
    "eliminationRecordsToJsonArray",
    "clocktowerEventsToJsonArray",
    "recordedEpistemicObservationsToJsonArray",
)
outside = text[:start] + text[end:]
newly_exclusive = [token for token in shared_tokens if token not in outside]
if newly_exclusive:
    raise SystemExit(
        "Audited shared dependency became exclusive to activeGameSnapshotJson; "
        f"stop for explicit dependency review: {newly_exclusive}"
    )

patched = outside
if "activeGameSnapshotJson(" in patched:
    raise SystemExit("activeGameSnapshotJson reference remains in target source after deletion")
if "fun activeGameRecoverySnapshot(): RecoverySnapshot" not in patched:
    raise SystemExit("typed Recovery writer seam was damaged")
if "LegacyRestoreCompatibility(" not in patched:
    raise SystemExit("PS4.1 must not remove LegacyRestoreCompatibility")
if "RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())" not in patched:
    raise SystemExit("typed Recovery persistence writer call was damaged")

TARGET.write_text(patched, encoding="utf-8", newline="\n")
print(f"Removed activeGameSnapshotJson block: {removed.count(chr(10))} lines")
