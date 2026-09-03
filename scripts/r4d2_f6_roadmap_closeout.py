from pathlib import Path

path = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = path.read_text(encoding="utf-8")

replacements = [
    (
        """latest validated executable checkpoint:\nUI-R4D-2F / F5 — dedicated typed pair Player Reveal / readable seat-number hierarchy\nbranch: codex/ui-r4d2-seating-first-setup\nF5 RED contract checkpoint: 98622ab624401217282466829bc200e67782b25e\nF5 RED run: 33702857698\nF5 product checkpoint: f8cb06a2774821e9b1bc007e563c6b9f759f063f\nF5 strengthened presentation/privacy contract checkpoint: 8bf1087396544633b52046ddfc670a8c1c75034b\nF5 final validation run: 33703495351\nF5 executable cleanup head: c4f106c7cddd4d3bd9c8215d33c4d503d279e57b\nlater branch heads after that checkpoint may be docs-only; always distinguish them from executable F5 state\nPR #79: draft / open / mergeable / unmerged""",
        """latest validated executable checkpoint:\nUI-R4D-2F / F6 — high-contrast Host seat/state typography\nbranch: codex/ui-r4d2-seating-first-setup\nF6 product checkpoint: ae3fa44063e6d8ad6457d0b5b06a48f4eccb579a\nF6 validation run: 33704315219\nF6 executable cleanup head: e5109cc95ebb18fdb51a336c24fd4d96e388a0c4\nlater branch heads after that checkpoint may be docs-only; always distinguish them from executable F6 state\nPR #79: draft / open / mergeable / unmerged""",
    ),
    (
        """active development target:\nUI-R4D-2F / F6 — high-contrast seat/state typography corrections""",
        """active development target:\nUI-R4D-2F / F7 — real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal""",
    ),
    (
        """UI-R4D-2F F6 high-contrast seat/state typography                       ACTIVE NEXT\nUI-R4D-2F F7 real-device closeout                                      QUEUED""",
        """UI-R4D-2F F6 high-contrast seat/state typography                       COMPLETE / VERIFIED\nUI-R4D-2F F7 real-device closeout                                      ACTIVE NEXT""",
    ),
    (
        "Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, F4 resolved-display authority, or F5 typed pair Player Reveal presentation.",
        "Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, F4 resolved-display authority, F5 typed pair Player Reveal presentation, or F6 Host seat/state typography.",
    ),
    (
        """#### F6 — next active slice\n\nApply the queued high-contrast seat/state typography corrections without changing seat authority, legality, F5 Player Reveal identity, or F4 information-resolution semantics.\n\n#### F7 — remaining field-test closeout""",
        """#### F6 — COMPLETE / VERIFIED\n\nF6 strengthened the shared Storyteller Host-table seat readability without changing table geometry or interaction semantics:\n\n- seat number typography increased from 12sp/Bold to 15sp/Black;\n- player-name typography increased from 11sp to 12sp with stronger SemiBold/Black emphasis;\n- typed state markers increased from 13sp/Bold to 14sp/Black;\n- neutral/selectable/selected/highlighted borders were strengthened so state is not conveyed by fill color alone;\n- disabled content/border contrast was raised while preserving an unmistakably disabled appearance;\n- 64x50 seat-card geometry, stable seat identity, layout capacity, click legality, drag ordering and state mapping were unchanged.\n\nTesting followed the risk-based visual-adjustment rule: no ceremonial pixel/source-shape RED was added. Existing semantic/layout contracts plus regression coverage remained authoritative.\n\nEvidence:\n\n- product checkpoint: `ae3fa44063e6d8ad6457d0b5b06a48f4eccb579a`;\n- validation run `33704315219`: exact-head guard + focused square-table/layout/reorder contracts + `:app:testFast` + exact diff audit + `git diff --check` GREEN;\n- executable cleanup head: `e5109cc95ebb18fdb51a336c24fd4d96e388a0c4`;\n- temporary F6 workflow self-removed;\n- permanent F6 executable diff is only `ClocktowerSquareTableUi.kt` (`+21/-19`).\n\n#### F7 — ACTIVE NEXT / remaining field-test closeout""",
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one roadmap anchor, found {count}: {old[:80]!r}")
    text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
