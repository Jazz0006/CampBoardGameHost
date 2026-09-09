# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-10 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 deep transaction-application viability audit   COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE

UI-R5 pair-information baseline                   ACCEPTED / T4 + device
UI-R5 wake-actor + compact navigation             IMPLEMENTED / automated green
UI-R5 Chef square-table                           COMPLETE / user device PASS
UI-R5 Empath square-table                         IMPLEMENTED / automated green / device pending
UI-R5 Undertaker square-table                     IMPLEMENTED / automated green / device pending
Dynamic night-trigger navigation bug              FIXED / T4 + R2 green / device retest pending
UI-R5 overall campaign                            ACTIVE
```

Historical D6/R3 evidence lives under `docs/archive/checkpoints/d6/`. Do not reopen D6 merely because a remaining source file is large.

## 2. Live UI-R5 branch / PR

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
latest Undertaker implementation: 4e8296600e2144a1c9790181f2b78c471e32adec
```

PR #117 is open, Draft, mergeable and unmerged. Do not merge without explicit user authorization.

Current full validation checkpoint:

```text
production fix: 0f66b78bbdc9a50e9f1c94db530e3aaaaf488880
verified head:  c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
CI #2058 / run 34408187526              PASS
- Android full unit tests + debug APK     PASS / executed
- ASP contract tests                      PASS / executed
- Real Clingo cross-validation            PASS / executed
- CI gate                                 PASS
R2 #1925 / run 34408187513                PASS
```

`c1ab5578...` is a no-tree-change `[full-ci]` verification commit on top of the production code at `0f66b78...`.

Latest per-role Undertaker checkpoint:

```text
implementation head: 4e8296600e2144a1c9790181f2b78c471e32adec
CI #2067 / run 34411490702               PASS
- Android FAST unit tests                 PASS / executed
- Android full unit tests + debug APK     skipped / not requested for this per-role slice
- ASP contract tests                      skipped / UI-only change
- Real Clingo cross-validation            skipped / UI-only change
- CI gate                                 PASS
R2 #1934 / run 34411490693                PASS
```

This per-role checkpoint does not replace the full-T4 checkpoint. Another logical full T4 remains required after the remaining role convergence and legacy retirement work.

## 3. Current priority

> **CURRENT: finish UI-R5 night-role square-table convergence and retire superseded legacy UI.**

Product target:

> All Storyteller night-role operations should use one coherent square-table interaction language where that interaction model is appropriate. Once a replacement path is accepted, retire the superseded text/list/manual-dialog path instead of keeping two parallel UIs.

Shared presentation hierarchy:

```text
1. current actor / player to wake        highest priority
2. role/action instruction
3. related target / information players  visually distinct from actor
4. result / recommendation
5. action controls
6. Previous / Next                       stable compact one-row navigation
```

Stable interaction rules:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- typed upstream domains own legality and registration witnesses;
- localized display text is never parsed back into semantic identity;
- Storyteller-only truth/reliability information never leaks to player-facing reveal;
- specialized thin center controls are preferred over one universal mega-picker.

## 4. Completed / implemented UI-R5 slices

### Pair-information: Washerwoman / Librarian / Investigator

Accepted semantic/table baseline:

```text
b960be22d217ed2caa06b49c0319eace23470b42
CI #2024 PASS — full Android + APK / ASP / Real Clingo / gate
R2 #1891 PASS
user-reported real-device PASS
```

Pair Manual legality, recommendation canonicalization and reveal semantics remain accepted. The later wake-actor/navigation presentation amendment is automated-green; historical device acceptance predates that amendment and is not silently extended to every later visual change.

### Chef

Status: **COMPLETE / user-reported real-device PASS**.

Chef now uses the square-table shell with:

- current actor wake highlight;
- actual evil Storyteller information hints;
- distinct Recluse marker;
- one-result single Show-information action;
- typed multi-result choice with effective contributing-seat highlighting;
- Spy/Recluse registration witnesses;
- fail-closed presentation for impaired arbitrary values.

### Empath

Status: **IMPLEMENTED / automated green / real-device acceptance pending**.

Empath now uses the square-table shell with:

- current actor wake highlight;
- authoritative living-neighbour scope from typed `NumericResult.subjectSeats`;
- no UI reconstruction of adjacency/dead-player skipping;
- independent neighbour scope, actual-evil hint, Recluse marker and selected-result contribution;
- typed Spy/Recluse registration witnesses;
- no fabricated contributor highlight for impaired arbitrary information.

### Undertaker

Status: **IMPLEMENTED / Android FAST + R2 green / real-device acceptance pending**.

Undertaker now uses a dedicated read-only square-table information surface with:

- current Undertaker actor highlighted independently from the information-context player;
- the executed player taken only from typed `InformationProposition.RoleAt.seat` and shown as read-only context;
- the displayed character taken from the same typed `RoleAt` proposition rather than reconstructed from localized text or day UI state;
- one legal final result rendered as one Show-information action;
- multiple legal final results kept inside the existing typed result domain and selected with a compact role menu suitable for small screens;
- conflicting/malformed/missing typed executed-seat candidates failing closed instead of inventing context;
- existing recommended, unreliable and direct player-display/history paths preserved;
- superseded generic result/recommendation/unreliable controls gated off whenever the specialized Undertaker surface has a valid typed domain.

Implementation checkpoint:

```text
4e8296600e2144a1c9790181f2b78c471e32adec
CI #2067 / run 34411490702  Android FAST + gate PASS
R2 #1934 / run 34411490693   PASS
exact implementation diff: 4 Undertaker-related files from pre-slice docs head a309dbcb...
```

Do not mark Undertaker device accepted until a real-device run is explicitly reported.

## 5. Dynamic night-trigger navigation bug

A real-device report exposed:

```text
Monk initially protects Ravenkeeper
-> go back
-> reconfirm Monk protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies but ability step is skipped
```

Characterization proved Monk retarget semantics and Demon death resolution were already correct. The defect was navigation: `DemonKill` could dynamically extend checkpoint-derived `nightSteps`, while the same Compose callback still used the pre-confirmation list to decide that the night was complete.

Production fix `0f66b78...` now defers completion for flow-expanding actions until refreshed `nightSteps` prove whether a new trigger step exists. Tests cover:

- dynamic-last-step deferred advance;
- Host wiring for `DemonKill` / `MayorRedirect`;
- Monk retarget followed by Ravenkeeper death.

Automated T4 and R2 are green at `c1ab5578...`.

The original user reproduction still needs real-device retest before the bug-fix device gate is marked PASS.

This correctness fix is separate from Ravenkeeper's remaining UI migration.

## 6. Immediate execution sequence

The next development slice starts here:

```text
Ravenkeeper
-> read-only inventory of all remaining active night-role Storyteller surfaces
-> migrate any remaining appropriate legacy surface
-> retire superseded reachable text/list/manual-dialog UI
-> final UI-R5 T4
-> cross-role real-device acceptance
-> UI-R5 closeout
```

### Undertaker — IMPLEMENTED / DEVICE PENDING

The planned model has been implemented at `4e829660...`. The executed player is read-only typed information context, the result remains typed, player-facing reveal/history semantics are preserved, and generic parallel result surfaces are suppressed while the specialized surface is valid.

No real-device acceptance is recorded yet.

### Ravenkeeper — NEXT

Expected model:

- dead Ravenkeeper remains current actor/trigger owner;
- select one target player on the square table;
- resolve/display that player's role using existing typed registration/reliability semantics;
- preserve Spy/Recluse and impaired misinformation legality;
- reuse square-table target selection rather than dropdown/text selection;
- keep the dynamic-trigger correctness fix separate from presentation migration.

## 7. Final surviving-surface audit

After Ravenkeeper, perform a fresh complete inventory of every active night action. Classify each Storyteller surface:

```text
ACCEPTED SQUARE TABLE
MIGRATE
KEEP SPECIALIZED
UNREACHABLE LEGACY — RETIRE
```

Do not declare UI-R5 complete from the known-role list alone.

Do not migrate a specialized interaction merely for visual uniformity if doing so reduces clarity or violates semantic ownership.

## 8. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

For each remaining role slice:

- tests-first where a stable semantic/presentation contract is uncovered;
- no artificial domain RED for visual-only layout changes;
- focused tests during implementation;
- Android FAST at production checkpoints;
- keep R2 green;
- exact diff audit, especially for large owner files;
- real-device validation remains separate from automation.

The latest full T4 is already green at `c1ab5578...`. Take another final logical T4 after the remaining migrations and legacy retirement before PR #117 becomes merge-ready.

## 9. Frozen architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- Planner/Reducer rule ownership;
- typed legal/recommendation/registration domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused fix.

UI-R5 does **not** authorize:

- EPI-MQ scoring/explanation;
- recommendation-provider replacement;
- generalized App-root architecture rewrite;
- Persistence follow-up;
- unrelated game-rule changes;
- A4/ZDD activation;
- renewed D6 decomposition based on file size.

## 10. Active handoff

The single active handoff is now:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`

The previous 2026-09-09 UI-R5 handoff is superseded and should be treated as historical context only.

Primary product/UI reference:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 11. UI-R5 completion condition

UI-R5 can close only when:

```text
Undertaker accounted for
+ Ravenkeeper accounted for
+ every active night-role Storyteller surface audited
+ appropriate square-table migrations complete
+ superseded reachable legacy UI retired
+ affected focused/FAST/R2 green
+ final logical T4 green
+ exact diff / scope audit green
+ cross-role real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ roadmap/handoff synchronized
```

The project is **not yet at this condition**.

## 12. Current authoritative reading order

For a new development conversation:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live role-specific code for the current slice.
