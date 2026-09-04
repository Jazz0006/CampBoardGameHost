from pathlib import Path

path = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = path.read_text(encoding="utf-8")


def replace_exact(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    text = text.replace(old, new, 1)


replace_exact(
    "> Updated: 2026-09-03 Australia/Sydney",
    "> Updated: 2026-09-04 Australia/Sydney",
    "updated date",
)

replace_exact(
    """live main at roadmap refresh:\nbf37bbb (re-confirm live before implementation/merge)\n\nactive campaign:\nUI Information / Storyteller Workspace Campaign\n\nlatest validated executable checkpoint:\nUI-R4D-2F / F7 — real-device correction closeout\nbranch: codex/ui-r4d2-seating-first-setup\nfinal product checkpoint: 40b604eae7ea489347357f88fd3d07be83ce5a78\nF7.6 field APK validation run: 33722208538\nreal-device acceptance: PASS (user-confirmed 2026-09-03)\nlater #79 heads are CI/docs cleanup only; permanent product files match the checkpoint\nPR #79: draft / open / mergeable / unmerged\n\nvalidated F1 immediately below it:\nUI-R4D-2F / F1 — constraint/capacity-aware HostTableLayout\nvalidated executable checkpoint: f49e9f6a4be5109cd16fe724e24071179310004c\ncleanup head: 37ea5e9b3b1283c6f1f5fc71e35603ff9e88aaad\n\nvalidated foundation below R4D-2:\nUI-R4D-1 — Persistent Host Table Foundation\nbranch: codex/ui-r4d-persistent-table-foundation\ncleanup head: 524f55bac945f1be8ee9d9ec77e4e4ca6935781e\nPR #78: draft / open / mergeable / unmerged\n\nactive development target:\nUI-R4D-3.1 — Day Overview migration into the persistent Host table workspace\n\nunblocked by completed R4D-2F:\nUI-R4D-3 — Day Storyteller Workspace\n\nstabilization after UI-R4D:\nUI-R5 — Real-Device Stabilization / Feature Freeze\n\nalgorithm campaign after UI stabilization:\nEPI-MQ / Productive Uncertainty / PlayerWorldSet""",
    """live main at roadmap refresh:\nbf37bbbced8b1ec71a1ffe209954d328de453c95 (re-confirm live before merge)\n\nactive campaign:\nUI Information / Storyteller Workspace Campaign\n\ncloseout integration candidate:\nbranch: codex/ui-stack-closeout-2026-09-04\nbase product lineage: PR #92 head 5501fb02cf37fa2da9ad63bbef7d78608784d787\nmain field-test APK infrastructure: carried forward unchanged into the closeout candidate\n\nlatest stacked product checkpoint:\nUI-R4D-6.4B — Mayor redirect selection migrated to shared square table\nPR #92: draft / open / mergeable / unmerged at closeout start\nvalidated checkpoint: 5501fb02cf37fa2da9ad63bbef7d78608784d787\n\ncloseout status:\nFINAL MERGE GATE ACTIVE — closeout PR + final [full-ci] + R2 required before merge\n\nnext independent UI slice after closeout merge:\nNight persistent Host Table wake/action lifecycle\nWAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE\n\nalgorithm campaign after UI stabilization:\nEPI-MQ / Productive Uncertainty / PlayerWorldSet""",
    "current development context",
)

replace_exact(
    "UI-R1 through UI-R4D-2 are stacked draft work and are **not yet on main**. Do not create the next UI branch from `main` or the stack will be lost. Do not start R4D-3 until the R4D-2F field-test correction gate below is closed.",
    "UI-R1 through UI-R4D-6.4B are accumulated stacked draft work and are **not yet on main**. The active task is stack closeout, not new feature development. Do not branch new UI work from `main` until the closeout merge lands. The known Night wake/action UX gap is explicitly deferred to the next independent branch from the post-closeout `main`.",
    "stack closeout paragraph",
)

replace_exact(
    """UI-R4D-3 day Storyteller workspace                             ACTIVE — R4D-3.1 DAY OVERVIEW\nUI-R4D-4 public claim history                                  QUEUED\nUI-R4D-5 nomination / vote state machine                       QUEUED\nUI-R4D-6 unified Host seat presentation migration              QUEUED\nUI-R5   real-device stabilization / feature freeze             QUEUED AFTER UI-R4D""",
    """UI-R4D-3.1 Day Overview persistent workspace                    COMPLETE / VERIFIED / DRAFT #82\nUI-R4D-4 public claim history                                  DEFERRED / NOT IN CLOSEOUT\nUI-R4D-5.1 nomination gesture                                  COMPLETE / VERIFIED / DRAFT #83\nUI-R4D-5.2A individual table voting                            COMPLETE / VERIFIED / DRAFT #84\nUI-R4D-5.2B dead-player ghost-vote authority                   COMPLETE / VERIFIED / DRAFT #85\nUI-R4D-5.3 detailed voter history                              COMPLETE / VERIFIED / DRAFT #86\nUI-R4D-5.2C confirmed vote transaction                         COMPLETE / VERIFIED / DRAFT #87\nUI-R4D-6.1 Slayer table migration                              COMPLETE / VERIFIED / DRAFT #88\nUI-R4D-6.2 Artist table migration                              COMPLETE / VERIFIED / DRAFT #89\nUI-R4D-6.3 Klutz table migration                               COMPLETE / VERIFIED / DRAFT #90\nUI-R4D-6.4A Red Herring table migration                        COMPLETE / VERIFIED / DRAFT #91\nUI-R4D-6.4B Mayor redirect table migration                     COMPLETE / VERIFIED / DRAFT #92\nUI-R4D-N Night wake/action lifecycle                            NEXT AFTER CLOSEOUT MERGE\nUI-R5   real-device stabilization / feature freeze             CLOSEOUT GATE ACTIVE""",
    "campaign status",
)

replace_exact(
    """Current next-development handoff:\n\n`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_UI_R4D3_DAY_WORKSPACE.md`""",
    """Current closeout authority:\n\n`docs/UI_STACK_CLOSEOUT_2026-09-04.md`\n\nHistorical Day-workspace handoff:\n\n`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_UI_R4D3_DAY_WORKSPACE.md`""",
    "active authority",
)

path.write_text(text, encoding="utf-8")
