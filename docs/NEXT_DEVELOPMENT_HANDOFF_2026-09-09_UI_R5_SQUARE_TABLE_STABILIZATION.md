# NEXT DEVELOPMENT HANDOFF — UI-R5 Square-table Storyteller Consolidation / Real-device Stabilization

> Date: 2026-09-09 Australia/Sydney  
> Status: **ACTIVE HANDOFF — PAIR-INFORMATION SLICE IMPLEMENTED; T4/DEVICE ACCEPTANCE PENDING**  
> Scope: presentation / interaction architecture + real-device stabilization

## 1. Read first

For the next clean development session, read in this order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live code for the square-table surfaces selected by the audit.

Do **not** load the D6 archive by default. If historical ownership evidence is needed, start with:

- `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`;
- `docs/archive/checkpoints/d6/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md` only if a square-table ownership question specifically requires it.

## 2. Baseline to preserve

D6 decomposition is closed.

```text
D6.1                            COMPLETE / merged
D6.2 R0–R2                     COMPLETE / FULL accepted / merged
R3 transaction application     COMPLETE / NO-GO
```

D6.2 final production-equivalent code/test head:

```text
b2263cd08bc2ce223598698324bf2b22243c91f2
```

D6.2 merge:

```text
PR #115
c75e0f0bc4635ef42ffbece41470c3437a205910
```

The final D6.2 FULL gate passed, but real-device critical-path testing was explicitly waived for that merge. UI-R5 exists partly to close that practical validation gap.

## 3. Live UI-R5 checkpoint

Active branch / PR:

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
```

Latest validated production/test checkpoint before this documentation-only T4 trigger:

```text
ecfacb7b11d39e65febef89fb3e058390f9eea5f
```

Validation at that head:

```text
CI #2022 / 34339511298             PASS
Android FAST unit tests            PASS / executed
full Android unit tests + APK       SKIPPED by ordinary PR policy
CI gate                             PASS
R2 #1889 / 34339511280              PASS
```

The first `[full-ci]` documentation trigger was intentionally superseded after a behavior-level audit found one presentation mismatch: the new pair-information Manual state exposed every role-valid seat instead of preserving the existing pair picker's legal second-seat continuation projection. That issue was fixed before this final checkpoint by reusing `clocktowerPairManualSeatState`, and a regression test now protects the behavior.

This final documentation checkpoint intentionally uses `[full-ci]`. Treat the pair-information slice as T4 accepted **only after** the workflows attached to the final branch head complete with the full Android unit suite + debug assemble and all other selected full gates green.

Do not confuse that T4 checkpoint with UI-R5 campaign acceptance: real-device validation is still required.

## 4. Implemented pair-information flow

Washerwoman / Librarian / Investigator now use the square-table interaction language directly.

Default recommended state:

```text
night step
-> full-screen square table
-> recommended two seats already highlighted
-> center shows the information role / proposition preview
-> [Show this information / 展示此信息]
-> [Choose manually / 手动选择]
```

Manual state:

```text
same square table
-> recommendation remains selected initially
-> player selection uses the existing two-seat selection semantics
-> center role control becomes editable
-> selectable/selected/disabled seats use the existing pair Manual projection
-> after a first seat is retained, only legal second-seat continuations are selectable
-> [Restore recommendation / 恢复推荐] is available when a recommendation exists
-> [Show this information / 展示此信息] uses the same final handoff
```

Player-facing display remains the existing display/confirmation path. This slice did **not** create a second observation commit or reveal pipeline.

### Ownership retained

The implementation deliberately reuses the existing owners:

```text
ClocktowerPairManualAuthority
  -> complete typed Manual legal projection
  -> recommendation-to-Manual canonicalization by structured proposition key

ClocktowerPairManualSelectionModel
  -> two-player draft state
  -> select/cancel/replace behavior

clocktowerPairManualSeatState
  -> legal selectable/selected/disabled seat projection

ClocktowerSquareTableSeatSurface
  -> existing square-table geometry and seat rendering

existing showRecommendedDisplayOption / player-display resolution
  -> final display and existing structured confirmation/observation path
```

New composition owner:

```text
ClocktowerPairInformationSquareTableUi.kt
```

Its responsibility is intentionally thin: compose recommendation preview + in-place Manual editing on the square-table surface. It does not own recommendation ranking, legal-domain generation, durable history, Recovery, or GameState.

### Recommendation canonicalization contract

Recommendation candidates and complete Manual-domain candidates can carry different presentation metadata. The implementation therefore does not rely on object equality or localized label parsing.

`ClocktowerPairManualAuthority` canonicalizes the recommendation back onto the complete Manual legal candidate by structured pair semantics. If the recommendation cannot be represented in the current Manual domain, the flow fails closed into Manual editing rather than inventing an illegal candidate.

### Characterization evidence added

Tests now protect:

- seeding a Manual selection model from a valid recommendation;
- fail-closed behavior for an invalid/stale recommendation;
- structured recommendation-to-Manual canonicalization despite presentation metadata differences;
- recommended read-only seat highlighting;
- Manual selectable/selected/disabled seat-state projection;
- legal second-seat availability after entering Manual edit from a recommendation.

## 5. Exact scope already audited

Relative to base `main`, the production flow change is localized.

`ClocktowerNightStepUi.kt` only removes the old local `showManualPairSelection` state / separate Manual dialog wiring and replaces the pair recommendation section with the unified square-table composition. Existing Fortune Teller, Chambermaid, numeric information, registration/result-first, dynamic-decision and player-display semantics remain outside this change.

`ClocktowerPairManualSelectionUi.kt` changes only one visibility boundary: `clocktowerPairManualSeatState` becomes `internal` so the new pair-information composition can reuse the already-existing legal seat-state projection. The old helper's behavior is unchanged.

No square-table geometry algorithm was rewritten. No domain/session/persistence module acquired Compose dependencies. No recommendation scoring or gameplay rule changed.

PR #117 remains Draft and must not be merged solely because CI is green.

## 6. Next mandatory step after T4

After the final `[full-ci]` checkpoint is green, move to **UI-R5.4 real-device stabilization**, not EPI-MQ.

Portrait phone is primary. Record exact device / Android version where practical and exercise at minimum:

1. Washerwoman recommended flow;
2. Washerwoman Manual flow and selected-seat correction behavior;
3. Librarian normal pair flow;
4. Librarian legal zero-Outsider flow if the chosen setup exposes it;
5. Investigator recommended + Manual flow;
6. player-facing reveal then return to the correct night step;
7. dense 8–15 player layouts, especially 15 players;
8. long player names;
9. selected-first / selected-second / selectable / disabled state readability;
10. center role dropdown and action buttons without seat collision;
11. status/navigation inset safety;
12. no first-night display-crash regression;
13. no Storyteller-only hidden state on the player-facing display.

If a real-device defect is found, fix it within UI-R5 with focused evidence. Do not waive it silently.

## 7. Product constraints

Preserve the active product decisions in `BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`:

- full-screen rectangular/square-table visual language;
- stable seat identity independent of filtered-list position;
- player selection happens on the table, not via player dropdowns;
- Manual editing stays on the same table surface for pair-information roles;
- shared visual language between selection and player display;
- Fortune Teller directly selects two targets then resolves the legal result domain;
- Storyteller-only reliability/discretion indicators never leak to player-facing display;
- Manual legality comes from the complete typed legal semantic domain, not the recommendation shortlist;
- localized text is never parsed back into semantic identity.

UI-R5 may refine geometry and presentation, but it must not redefine those semantic contracts.

## 8. Architecture constraints

UI-R5 must preserve the post-D6 ownership model:

- `ClocktowerGameSession` remains canonical writable state;
- rule/recommendation/legal-domain semantics remain upstream of presentation;
- Planner/Reducer responsibilities are not pulled into UI;
- square-table components may own layout, local draft selection presentation and visual-state rendering only when appropriate;
- durable observation/history/Recovery ordering remains in existing owners;
- App choreography is not moved merely to make a file smaller;
- no `UiContext`, `Actions`, callback mega-bag or second session owner;
- no Compose dependency enters session/domain modules;
- Undercover/Werewolf remain isolated.

## 9. Validation strategy

Use the repository's risk-based evidence model.

For UI/presentation-only work:

- do not manufacture domain REDs;
- use typed interaction/legality tests for stable contracts;
- run focused evidence during iteration;
- run `:app:testFast` at the logical checkpoint when Android production code changes;
- use `[full-ci]` for the T4 acceptance checkpoint;
- distinguish an actually executed full suite from a skipped/cached step;
- real-device validation remains a separate required UI-R5 acceptance artifact.

A green FAST run is not evidence that `testFull` or `assembleDebug` executed. Record the exact workflow step result.

## 10. Scope fence

Do not broaden UI-R5 into:

- EPI-MQ scoring or explanation;
- recommendation-provider replacement;
- generalized architecture rewrite of `CampBoardGameHostApp.kt`;
- Persistence Simplification follow-up;
- rules fixes unrelated to a UI-R5 defect;
- new scripts/characters merely to test layout;
- A4/ZDD production activation.

If a real-device bug exposes a genuine semantic defect, isolate it as a separate tested bug-fix slice rather than silently changing rules inside a visual refactor.

## 11. Completion condition

UI-R5 is ready to close only when:

```text
shared square-table ownership audited
+ approved consolidation implemented
+ focused/FAST affected validation green
+ T4 logical acceptance checkpoint green
+ exact diff / scope audit green
+ real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ roadmap/handoff updated
```

Only then change the active roadmap priority to EPI-MQ / Productive Uncertainty.
