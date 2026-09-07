from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

replacements = [
    (
        """            committedClocktowerSetup = committedClocktowerSetup,
            troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,
            clocktowerRulesetRoleIds = clocktowerRulesetRoleIds.toSet(),
""",
        """            committedClocktowerSetup = committedClocktowerSetup,
            clocktowerRulesetRoleIds = clocktowerRulesetRoleIds.toSet(),
""",
        "legacy shell rotation field",
    ),
    (
        """                identity = ClocktowerRecoveryIdentity(
                    script = currentClocktowerScript,
                    gameId = clocktowerGameId,
                    gameSeed = clocktowerGameSeed,
                ),
                position = ClocktowerRecoveryPosition(
""",
        """                identity = ClocktowerRecoveryIdentity(
                    script = currentClocktowerScript,
                    gameId = clocktowerGameId,
                    gameSeed = clocktowerGameSeed,
                ),
                troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,
                position = ClocktowerRecoveryPosition(
""",
        "typed Clocktower recovery rotation ownership",
    ),
    (
        """                committedTroubleBrewingSetupRotationRecord =
                    plan.snapshot.legacyRestoreCompatibility.troubleBrewingSetupRotationRecord
""",
        """                committedTroubleBrewingSetupRotationRecord =
                    game.troubleBrewingSetupRotationRecord
""",
        "typed recovery apply rotation source",
    ),
]

for old, _new, label in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")

for old, new, _label in replacements:
    text = text.replace(old, new, 1)

for old, _new, label in replacements:
    if old in text:
        raise SystemExit(f"Original {label} anchor remains after replacement")

required = [
    "troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,",
    "committedTroubleBrewingSetupRotationRecord =\n                    game.troubleBrewingSetupRotationRecord",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Missing required post-patch token: {token}")

forbidden = [
    "plan.snapshot.legacyRestoreCompatibility.troubleBrewingSetupRotationRecord",
    "            troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,\n            clocktowerRulesetRoleIds",
]
for token in forbidden:
    if token in text:
        raise SystemExit(f"Forbidden legacy rotation token remains: {token}")

PATH.write_text(text, encoding="utf-8", newline="\n")
