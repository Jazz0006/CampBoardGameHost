from pathlib import Path

path = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = path.read_text(encoding="utf-8")

replacements = [
    (
        "Immediate next slice:\n\n> **PS4.1 — proven-dead `activeGameSnapshotJson()` removal**\n\nDo not begin PS5, Werewolf module deletion or D6 decomposition as part of PS4.1.",
        "Immediate next slice:\n\n> **PS4.3 — typed Recovery wire cleanup / format bump**\n\nPS4.1 and PS4.2 are complete. Do not begin PS5, Werewolf module deletion or D6 decomposition as part of PS4.3.",
    ),
    (
        "PS4  retire superseded active-save infrastructure   AUTHORIZED / NEXT",
        "PS4  retire superseded active-save infrastructure   IN PROGRESS (PS4.1–PS4.2 COMPLETE)",
    ),
    (
        "## 6. PS4 — Retire superseded active-save infrastructure\n\nStatus: **authorized and next**.",
        "## 6. PS4 — Retire superseded active-save infrastructure\n\nStatus: **in progress — PS4.1 and PS4.2 complete; PS4.3 next**.",
    ),
    (
        "#### PS4.1 — Proven-dead active snapshot removal\n\nDelete:",
        "#### PS4.1 — Proven-dead active snapshot removal\n\nStatus: **COMPLETE**. Production checkpoint: `721c7394115cdc839afb189f88eb9234cd5ea204`.\n\nDelete:",
    ),
    (
        "#### PS4.2 — Recovery owns compatibility identity\n\nCut dependency on `ActiveGamePersistenceCoordinator.CURRENT_VERSION` and give Recovery its own current-format/current-contract token authority.",
        "#### PS4.2 — Recovery owns compatibility identity\n\nStatus: **COMPLETE**. RED checkpoint: `64c9866fccffa501ae1e1e3889c478b33764231e`; production GREEN checkpoint: `9fa0e3f86832cd847fb71126e4de54524e0326d2`.\n\nRecovery compatibility is now derived from `RecoverySnapshot.CURRENT_FORMAT_VERSION`, producing `recovery-v1:<GameKind>` for format v1. The token no longer depends on `ActiveGamePersistenceCoordinator.CURRENT_VERSION`. No migration support was added, and the 4-hour/fail-closed policy is unchanged.\n\nCut dependency on `ActiveGamePersistenceCoordinator.CURRENT_VERSION` and give Recovery its own current-format/current-contract token authority.",
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one roadmap anchor, found {count}: {old[:80]!r}")
    text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
