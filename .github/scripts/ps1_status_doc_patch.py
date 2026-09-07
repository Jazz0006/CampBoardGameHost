from pathlib import Path

ROADMAP = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
HANDOFF = Path("docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


roadmap = ROADMAP.read_text(encoding="utf-8")
roadmap = replace_once(
    roadmap,
    "Current code archives `activeGameSnapshotJson()` directly. This coupling must end.\n",
    "PS1 now enforces this separation in production: new archive writes project live game state into a narrow "
    "`GameArchiveRecord` and encode it with `GameArchiveJsonCodec`; they no longer consume "
    "`activeGameSnapshotJson()`. Legacy `{\\\"snapshot\\\": ...}` archive entries remain readable through an "
    "archive-only compatibility fallback, while active Recovery save/restore remains on its existing path.\n",
    "roadmap archive separation status",
)
roadmap = replace_once(
    roadmap,
    "### PS1 — Separate Archive from Recovery\n\nGoal: remove the product/data-model assumption that the active recovery snapshot is also the archive payload.\n",
    "### PS1 — Separate Archive from Recovery\n\nStatus: **complete on draft PR #112**.\n\n"
    "Goal: remove the product/data-model assumption that the active recovery snapshot is also the archive payload.\n",
    "roadmap PS1 status",
)
roadmap = replace_once(
    roadmap,
    "Do not yet change lifecycle persistence timing.\n\n### PS2 — Introduce minimal typed `RecoverySnapshot`\n",
    "Do not yet change lifecycle persistence timing.\n\n"
    "PS1 validation completed with the focused `GameArchiveJsonCodecTest`, `:app:testFast`, `git diff --check`, "
    "and an exact App wiring diff audit. The App wiring change is limited to archive decode/store/restart anchors; "
    "active Recovery snapshot generation, restore semantics and lifecycle save triggers were not changed.\n\n"
    "### PS2 — Introduce minimal typed `RecoverySnapshot`\n\nStatus: **next slice; not started**.\n",
    "roadmap PS2 status",
)
ROADMAP.write_text(roadmap, encoding="utf-8", newline="\n")

handoff = HANDOFF.read_text(encoding="utf-8")
handoff = replace_once(
    handoff,
    "### PS1 — Archive / Recovery separation\n\n**This is the next implementation slice.**\n",
    "### PS1 — Archive / Recovery separation\n\n**Status: complete on draft PR #112.**\n\n"
    "Production now owns an independent `GameArchiveRecord` / `GameArchiveJsonCodec` boundary. New archive writes "
    "no longer call or consume `activeGameSnapshotJson()`, while legacy snapshot-shaped archive records remain "
    "reviewable without active-Recovery version/identity validation. Active Recovery behavior and lifecycle triggers "
    "were intentionally left unchanged.\n",
    "handoff PS1 status",
)
handoff = replace_once(
    handoff,
    "Stop after PS1 validation before beginning PS2 unless the user explicitly asks to continue.\n\n### PS2 — Minimal typed RecoverySnapshot\n",
    "PS1 validation evidence: focused `GameArchiveJsonCodecTest` GREEN after the typed seam was introduced; "
    "`:app:testFast` GREEN; exact diff audit confirmed only the intended App archive anchors changed. The temporary "
    "one-shot patch workflow/script were removed after use.\n\n"
    "Stop after PS1 validation before beginning PS2 unless the user explicitly asks to continue.\n\n"
    "### PS2 — Minimal typed RecoverySnapshot\n\n**Status: next slice; not started.**\n",
    "handoff PS2 status",
)
old_immediate = """## 10. Immediate next action\n\nStart **PS1 only**:\n\n1. re-query branch/main heads;\n2. inspect the current archive write/read/review path and its tests;\n3. establish missing behavior characterization where needed;\n4. introduce the narrow archive projection;\n5. switch new archive writes away from active snapshot;\n6. validate old archive reading + new archive review parity;\n7. stop and report the PS1 checkpoint before PS2.\n\nNo production implementation has been performed by this documentation checkpoint."""
new_immediate = """## 10. Immediate next action\n\nPS1 is complete and this handoff now stops at that checkpoint.\n\nThe next implementation slice is **PS2 — Minimal typed RecoverySnapshot**, but it has **not** started. Before PS2 production edits:\n\n1. re-query live `main`, PR #112 and branch head;\n2. re-read the recovery classification and current active-save consumers;\n3. design the minimal common envelope plus game-specific payloads;\n4. identify real behavior contracts that deserve tests-first coverage;\n5. do not manufacture RED tests for purely mechanical ownership moves or rewiring;\n6. keep current persistence trigger timing stable during PS2 unless a behavior defect requires otherwise.\n\nDo not begin PS2 without explicit user authorization."""
handoff = replace_once(handoff, old_immediate, new_immediate, "handoff immediate next action")
HANDOFF.write_text(handoff, encoding="utf-8", newline="\n")
