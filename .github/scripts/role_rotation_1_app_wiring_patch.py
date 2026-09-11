from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = TARGET.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

replacements = [
    (
        """        val rotationHistory = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .historyFor(
                datasetId = dataset.datasetId,
                schemaVersion = dataset.schemaVersion,
                playerCount = playerNames.size,
            )
""",
        """        val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
        val rotationHistory = rotationHistoryStore.historyFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            playerCount = playerNames.size,
        )
        val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
        )
""",
    ),
    (
        """            recentSetupRotationHistory = rotationHistory,
        )
""",
        """            recentSetupRotationHistory = rotationHistory,
            recentPlayerStartingIdentityHistory = playerRotationHistory,
        )
""",
    ),
    (
        """                committedTroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecordFactory.fromSelection(
                    preparedSetup.selection,
                )
""",
        """                committedTroubleBrewingSetupRotationRecord =
                    TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(preparedSetup)
""",
    ),
]

counts = [text.count(old) for old, _ in replacements]
for index, count in enumerate(counts, start=1):
    if count != 1:
        raise SystemExit(f"Expected exactly one occurrence of patch anchor {index}, found {count}")

for old, new in replacements:
    text = text.replace(old, new, 1)

required = [
    "val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)",
    "val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(",
    "recentPlayerStartingIdentityHistory = playerRotationHistory,",
    "TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(preparedSetup)",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Required post-patch semantic token missing: {token}")

for old, _ in replacements:
    if old in text:
        raise SystemExit("Original patch anchor remains after replacement")

TARGET.write_text(text, encoding="utf-8", newline="\n")
