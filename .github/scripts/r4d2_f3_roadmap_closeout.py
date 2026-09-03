from pathlib import Path

TARGET = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = TARGET.read_text(encoding="utf-8")

replacements = [
    (
'''latest validated executable checkpoint:
UI-R4D-2F / F2 — drag-to-reorder on shared computed Host-table slots
branch: codex/ui-r4d2-seating-first-setup
F2 product checkpoint: 5857e2324fc8bf1fd6526adc95710751735dd3b6
F2 final cross-corner contract commit: bfb91ff9746ac0da2f4d182c5c319270da62ebdd
F2 executable cleanup head: 0e3b23dd96c164affd8540e1926ffdcada0fc3c6
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F2 state
PR #79: draft / open / mergeable / unmerged
''',
'''latest validated executable checkpoint:
UI-R4D-2F / F3 — explicit seating return + Android Back navigation
branch: codex/ui-r4d2-seating-first-setup
F3 typed contract checkpoint: 7792a799874f8a6fdec1e89f8b08d7f4fc7c8c19
F3 App product checkpoint: bbf75d6eee1a1759e8795b4418ea39c08f551cfc
F3 executable cleanup head: 5c0027eb180ee5c95ca52ee8cca03d7204258c61
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F3 state
PR #79: draft / open / mergeable / unmerged
''',
    ),
    (
'''active development target:
UI-R4D-2F / F3 — explicit GameSelection/Edit-seating + Android Back navigation
''',
'''active development target:
UI-R4D-2F / F4 — Manual pair resolved-display transition RED -> GREEN
''',
    ),
    (
'''UI-R4D-2F F2 shared-slot drag-to-reorder                       COMPLETE / VERIFIED
UI-R4D-2F F3 seating return / Android Back                     ACTIVE NEXT
UI-R4D-2F F4-F7 remaining field-test closeout                  QUEUED
''',
'''UI-R4D-2F F2 shared-slot drag-to-reorder                       COMPLETE / VERIFIED
UI-R4D-2F F3 seating return / Android Back                       COMPLETE / VERIFIED
UI-R4D-2F F4 Manual pair resolved-display transition             ACTIVE NEXT
UI-R4D-2F F5-F7 remaining field-test closeout                    QUEUED
''',
    ),
    (
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, or F2 drag ordering.
''',
'''Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, or F3 seating navigation.
''',
    ),
    (
'''#### F3 — next active slice

Make return from Game Selection to editable seating explicit and support Android system Back.

Required invariant:

```text
visible Edit/Back action
          \\
           -> SAME reopenSeating transition -> Screen.Setup
          /
Android system Back
```

Game-specific Settings -> Back must continue to return to Game Selection **without** releasing confirmed seating.

#### Remaining F4-F7

- F4 Manual pair resolved-display RED -> GREEN;
- F5 dedicated typed pair Player Reveal hierarchy;
- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.
''',
'''#### F3 — seating return / Android Back — COMPLETE

Permanent architecture:

```text
Game Selection visible Edit seats
            \\
             -> hostSeatingBackTransition(GameSelection)
             -> reopenSeating()
             -> Screen.Setup
            /
Android system Back

Game-specific Settings visible/system Back
-> hostSeatingBackTransition(GameSettings)
-> returnToGameSelection()
-> Screen.GameSelection
-> confirmed seating preserved
```

Validated behavior:

- Game Selection visible `Edit seats` and Android system Back consume the same typed transition;
- both explicitly release the old confirmation and return to editable seating;
- Undercover, Werewolf and Clocktower settings visible/system Back consume the same typed settings transition;
- settings Back preserves confirmed seating while clearing only the selected game and returning to Game Selection;
- no second direct App-level `reopenSeating()` / `returnToGameSelection()` navigation path remains for these surfaces.

Evidence:

- F3 RED anchor `8cf3eb5babd2d6075b872004c6dd2b8dc060f788`;
- RED run `33698824258`: failed only because the new typed back-navigation contract did not yet exist;
- typed contract checkpoint `7792a799874f8a6fdec1e89f8b08d7f4fc7c8c19`;
- typed focused GREEN run `33698991966`;
- exact large-file App product checkpoint `bbf75d6eee1a1759e8795b4418ea39c08f551cfc`;
- final one-shot run `33699228036`: exact head/blob/anchor audit + `HostSeatingRosterTest` + `:app:testFast` + `git diff --check` GREEN;
- F3 executable cleanup head `5c0027eb180ee5c95ca52ee8cca03d7204258c61`;
- all temporary F3 workflows/scripts self-removed.

Permanent F3 product/test diff relative to the F2/docs baseline touches only:

- `CampBoardGameHostApp.kt`;
- `HostSeatingBackNavigation.kt`;
- `HostSeatingRosterTest.kt`.

#### F4 — next active slice

Establish the resolved Manual pair display transition RED -> GREEN without changing Player Reveal visual hierarchy yet.

#### Remaining F5-F7

- F5 dedicated typed pair Player Reveal hierarchy;
- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.
''',
    ),
    (
'''Note: PR #79 is intentionally stacked on PR #78 rather than based on `main`, while the repository's ordinary CI/R2 pull-request triggers target `main`. Therefore normal PR CI/R2 does not automatically execute for this stacked PR. F1/F2 executable evidence comes from the dedicated checkpoint workflows above; do not misreport absence of main-target PR CI as a failure.
''',
'''Note: PR #79 is intentionally stacked on PR #78 rather than based on `main`, while the repository's ordinary CI/R2 pull-request triggers target `main`. Therefore normal PR CI/R2 does not automatically execute for this stacked PR. F1/F2/F3 executable evidence comes from the dedicated checkpoint workflows above; do not misreport absence of main-target PR CI as a failure.
''',
    ),
]

for old, new in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one roadmap anchor, found {count}: {old[:80]!r}")
    text = text.replace(old, new, 1)

TARGET.write_text(text, encoding="utf-8")
print("Closed F3 in roadmap and activated F4.")
