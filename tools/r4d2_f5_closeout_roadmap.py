#!/usr/bin/env python3
from pathlib import Path

TARGET = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")

REPLACEMENTS = [
    (
'''latest validated executable checkpoint:
UI-R4D-2F / F4 — source-agnostic resolved pair Player Reveal transition
branch: codex/ui-r4d2-seating-first-setup
F4 RED anchor: 4c8b4e6a21d4da3a3b440c73f973b8a716e3bc08
F4 resolved-display contract checkpoint: 959258033c572864afaa182941d575a3ab9cf168
F4 product checkpoint: 83374634f9246eb5556a26a2f6020ae9251d0c3e
F4 executable cleanup head: c5a6c3a2687e6cf2ba52c19d355ecd36a7da8984
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F4 state
PR #79: draft / open / mergeable / unmerged
''',
'''latest validated executable checkpoint:
UI-R4D-2F / F5 — dedicated typed pair Player Reveal / readable seat-number hierarchy
branch: codex/ui-r4d2-seating-first-setup
F5 RED contract checkpoint: 98622ab624401217282466829bc200e67782b25e
F5 RED run: 33702857698
F5 product checkpoint: f8cb06a2774821e9b1bc007e563c6b9f759f063f
F5 strengthened presentation/privacy contract checkpoint: 8bf1087396544633b52046ddfc670a8c1c75034b
F5 final validation run: 33703495351
F5 executable cleanup head: c4f106c7cddd4d3bd9c8215d33c4d503d279e57b
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F5 state
PR #79: draft / open / mergeable / unmerged
'''),
    (
'''active development target:
UI-R4D-2F / F5 — dedicated typed pair Player Reveal / readable seat-number hierarchy
''',
'''active development target:
UI-R4D-2F / F6 — high-contrast seat/state typography corrections
'''),
    (
'''UI-R4D-2F F4 Manual pair resolved-display transition                COMPLETE / VERIFIED
UI-R4D-2F F5 typed pair Player Reveal hierarchy                     ACTIVE NEXT
UI-R4D-2F F6-F7 remaining field-test closeout                       QUEUED
''',
'''UI-R4D-2F F4 Manual pair resolved-display transition                COMPLETE / VERIFIED
UI-R4D-2F F5 typed pair Player Reveal hierarchy                        COMPLETE / VERIFIED
UI-R4D-2F F6 high-contrast seat/state typography                       ACTIVE NEXT
UI-R4D-2F F7 real-device closeout                                      QUEUED
'''),
    (
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, or F4 resolved-display authority.
''',
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, F4 resolved-display authority, or F5 typed pair Player Reveal presentation.
'''),
    (
'''#### F5 — next active slice

Create the dedicated typed pair Player Reveal hierarchy with readable large seat numbers. Preserve the F4 source-agnostic resolved-display authority; F5 changes presentation, not Manual/recommendation semantics.

#### Remaining F6-F7

- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.

Only after F7 is clean should R4D-3 become active.
''',
'''#### F5 — dedicated typed pair Player Reveal — COMPLETE

Permanent presentation path:

```text
F4 source-agnostic resolved Player Reveal payload
        |
        v
clocktowerPairPlayerRevealPresentation(step, cards)
        |
        v
ClocktowerPairPlayerRevealPresentation
  - typed ClocktowerSeatId for each subject
  - player name from the canonical roster
  - display kind / title / primary result
  - player-visible displayFooter only
        |
        v
same Player Reveal renderer
        |
        X no square Host table
        X no Manual / Recommendation source distinction
        X no localized display-text parsing for seat identity
```

Validated behavior:

- `EitherOne` pair clues derive both seats only from typed `AnyOf(RoleAt, RoleAt)` semantics;
- two-subject `Number` and `YesNo` results derive both seats only from typed `subjectSeats`, including the production Chambermaid and Fortune Teller shapes;
- the canonical roster supplies player names for the typed physical seats;
- malformed/non-pair propositions and unknown seats do not fabricate pair identity and fall back to the existing generic Player Reveal;
- the dedicated pair reveal makes both seat numbers dominant and keeps player names/result hierarchy immediately readable on a phone;
- `displaySecondary` is not parsed or rendered as the pair-seat authority;
- pair reveal does not fall back to Storyteller-only `explanation` when no player-visible footer exists;
- Player Reveal remains sanitized information-only full screen and does not display the square Host table;
- F4 Manual/recommendation convergence remains untouched because the new presentation projection has no source parameter;
- `ClocktowerNightStepUi.kt`, `ClocktowerPlayerDisplayResolution.kt`, and `ClocktowerPairManualSelectionUi.kt` were not changed in F5;
- the obsolete no-op `clocktowerPlayerDisplayHighlightedSeats` presentation seam was retired and its tests were upgraded to the durable typed pair-reveal contract.

Evidence:

- F5 RED contract checkpoint `98622ab624401217282466829bc200e67782b25e`;
- RED run `33702857698`: focused Player Display test failed before the typed pair reveal seam existed;
- typed presentation owner checkpoint `e90554c513a78cb6b28d68844d07ac78aacb18ec`;
- dedicated renderer product checkpoint `f8cb06a2774821e9b1bc007e563c6b9f759f063f`;
- strengthened presentation/privacy test checkpoint `8bf1087396544633b52046ddfc670a8c1c75034b`;
- final validation run `33703495351`: focused F5 + F4 resolution contracts, `:app:testFast`, exact F5 changed-file audit and `git diff --check` GREEN;
- executable cleanup head `c4f106c7cddd4d3bd9c8215d33c4d503d279e57b`;
- temporary F5 workflows self-removed.

Permanent F5 diff vs F4 closeout is exactly:

- `ClocktowerPairPlayerRevealPresentation.kt`;
- `ClocktowerPlayerDisplayUi.kt`;
- `ClocktowerPlayerDisplayPresentationTest.kt`.

#### F6 — next active slice

Apply the queued high-contrast seat/state typography corrections without changing seat authority, legality, F5 Player Reveal identity, or F4 information-resolution semantics.

#### F7 — remaining field-test closeout

Real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.

Only after F7 is clean should R4D-3 become active.
'''),
]

text = TARGET.read_text(encoding="utf-8")
for old, new in REPLACEMENTS:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one roadmap anchor, found {count}: {old[:80]!r}")
    text = text.replace(old, new, 1)
TARGET.write_text(text, encoding="utf-8")
