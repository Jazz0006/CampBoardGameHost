#!/usr/bin/env python3
from pathlib import Path

TARGET = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")

REPLACEMENTS = [
    (
'''latest validated executable checkpoint:
UI-R4D-2F / F3 — explicit seating return + Android Back navigation
branch: codex/ui-r4d2-seating-first-setup
F3 typed contract checkpoint: 7792a799874f8a6fdec1e89f8b08d7f4fc7c8c19
F3 App product checkpoint: bbf75d6eee1a1759e8795b4418ea39c08f551cfc
F3 executable cleanup head: 5c0027eb180ee5c95ca52ee8cca03d7204258c61
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F3 state
PR #79: draft / open / mergeable / unmerged
''',
'''latest validated executable checkpoint:
UI-R4D-2F / F4 — source-agnostic resolved pair Player Reveal transition
branch: codex/ui-r4d2-seating-first-setup
F4 RED anchor: 4c8b4e6a21d4da3a3b440c73f973b8a716e3bc08
F4 resolved-display contract checkpoint: 959258033c572864afaa182941d575a3ab9cf168
F4 product checkpoint: 83374634f9246eb5556a26a2f6020ae9251d0c3e
F4 executable cleanup head: c5a6c3a2687e6cf2ba52c19d355ecd36a7da8984
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F4 state
PR #79: draft / open / mergeable / unmerged
'''),
    (
'''active development target:
UI-R4D-2F / F4 — Manual pair resolved-display transition RED -> GREEN
''',
'''active development target:
UI-R4D-2F / F5 — dedicated typed pair Player Reveal / readable seat-number hierarchy
'''),
    (
'''UI-R4D-2F F3 seating return / Android Back                       COMPLETE / VERIFIED
UI-R4D-2F F4 Manual pair resolved-display transition             ACTIVE NEXT
UI-R4D-2F F5-F7 remaining field-test closeout                    QUEUED
''',
'''UI-R4D-2F F3 seating return / Android Back                       COMPLETE / VERIFIED
UI-R4D-2F F4 Manual pair resolved-display transition                COMPLETE / VERIFIED
UI-R4D-2F F5 typed pair Player Reveal hierarchy                     ACTIVE NEXT
UI-R4D-2F F6-F7 remaining field-test closeout                       QUEUED
'''),
    (
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, or F3 seating navigation.
''',
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, or F4 resolved-display authority.
'''),
    (
'''#### F4 — next active slice

Establish the resolved Manual pair display transition RED -> GREEN without changing Player Reveal visual hierarchy yet.

#### Remaining F5-F7

- F5 dedicated typed pair Player Reveal hierarchy;
- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.
''',
'''#### F4 — resolved Manual/recommended pair display transition — COMPLETE

Permanent architecture:

```text
Manual pair selection -----\\
                            -> resolved ClocktowerDisplayOption
Recommendation selection --/          |
                                       v
                         resolveClocktowerPlayerDisplay(step, option)
                                       |
                                       v
                         SAME sanitized Player Reveal payload
                                       |
                                       v
                         SAME Player Reveal renderer
```

Validated behavior:

- Manual and recommendation paths already converge on the same `showRecommendedDisplayOption(option)` commit path;
- final Player Reveal projection is now owned by source-agnostic `resolveClocktowerPlayerDisplay(step, option)` with no Manual/recommended source parameter;
- the resolved payload copies the exact player-visible display fields, typed proposition and truth-selection value from the chosen option;
- Storyteller candidate lists are cleared before Player Reveal, preserving the phone-handoff privacy boundary;
- `ClocktowerNightStepUi.kt` keeps callback/audit ordering unchanged and delegates only the final projection;
- no Player Reveal visual hierarchy was changed in F4; readable pair seat-number hierarchy remains F5.

Evidence:

- F4 RED anchor `4c8b4e6a21d4da3a3b440c73f973b8a716e3bc08`;
- RED run `33700178383`: failed only on missing `resolveClocktowerPlayerDisplay` references;
- resolved-display contract checkpoint `959258033c572864afaa182941d575a3ab9cf168`;
- focused GREEN run `33700330132`;
- exact large-file wiring product checkpoint `83374634f9246eb5556a26a2f6020ae9251d0c3e`;
- final one-shot run `33700851236`: exact branch/blob/anchor audit + exact `ClocktowerNightStepUi.kt` diff (`+1/-14`) + `ClocktowerPlayerDisplayResolutionTest` + pair Manual/presentation focused contracts + `:app:testFast` + `git diff --check` GREEN;
- F4 executable cleanup head `c5a6c3a2687e6cf2ba52c19d355ecd36a7da8984`;
- all temporary F4 workflows/scripts self-removed.

Permanent F4 product/test diff relative to the F3/docs baseline touches only:

- `ClocktowerNightStepUi.kt`;
- `ClocktowerPlayerDisplayResolution.kt`;
- `ClocktowerPlayerDisplayResolutionTest.kt`.

#### F5 — next active slice

Create the dedicated typed pair Player Reveal hierarchy with readable large seat numbers. Preserve the F4 source-agnostic resolved-display authority; F5 changes presentation, not Manual/recommendation semantics.

#### Remaining F6-F7

- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.
'''),
    (
'''Note: PR #79 is intentionally stacked on PR #78 rather than based on `main`, while the repository's ordinary CI/R2 pull-request triggers target `main`. Therefore normal PR CI/R2 does not automatically execute for this stacked PR. F1/F2/F3 executable evidence comes from the dedicated checkpoint workflows above; do not misreport absence of main-target PR CI as a failure.
''',
'''Note: PR #79 is intentionally stacked on PR #78 rather than based on `main`, while the repository's ordinary CI/R2 pull-request triggers target `main`. Therefore normal PR CI/R2 does not automatically execute for this stacked PR. F1/F2/F3/F4 executable evidence comes from the dedicated checkpoint workflows above; do not misreport absence of main-target PR CI as a failure.
'''),
]


def main() -> None:
    text = TARGET.read_text(encoding="utf-8")
    for old, new in REPLACEMENTS:
        count = text.count(old)
        if count != 1:
            raise SystemExit(f"Refusing roadmap patch: expected one exact anchor, found {count}: {old[:80]!r}")
        text = text.replace(old, new, 1)
    TARGET.write_text(text, encoding="utf-8")
    print(f"Applied {len(REPLACEMENTS)} exact F4 closeout replacements.")


if __name__ == "__main__":
    main()
