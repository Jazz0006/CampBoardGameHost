# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — CONTINUE REMAINING NIGHT-ROLE MIGRATIONS**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

For a new development conversation, read in this order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live code for the next role-specific migration.

Do not reload the D6 archive by default. D6/R3 are closed.

## 2. Live branch / PR checkpoint

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
latest Undertaker implementation: 4e8296600e2144a1c9790181f2b78c471e32adec
```

PR #117 remains open, Draft, mergeable, and unmerged. Do not merge without explicit user authorization.

The latest full-T4 verified head is a no-tree-change `[full-ci]` verification commit on top of production fix:

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

The later Undertaker per-role implementation checkpoint is:

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

Do not treat `4e829660...` as a replacement for the full-T4 checkpoint. A new logical full T4 remains due after remaining role convergence and legacy retirement.

## 3. Product target

UI-R5 remains active:

> All Storyteller night-role operations should use one coherent square-table interaction language. Once a replacement path is accepted, retire the superseded text/list/manual-dialog surface rather than keep parallel UI.

Shared hierarchy:

```text
1. player to wake / current actor        highest visual priority
2. role/action instruction
3. related target or information seats  distinct from actor
4. recommendation / result content
5. action controls
6. Previous / Next                      stable compact single row
```

Preserve these rules:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- target/result legality comes from typed upstream semantics;
- never recover identity or legality by parsing localized text;
- Storyteller-only information must never leak to player-facing display;
- keep role-specific center controls thin rather than force every role through one generic picker.

## 4. Completed UI-R5 slices

### 4.1 Pair-information — Washerwoman / Librarian / Investigator

Accepted semantic/table baseline:

```text
b960be22d217ed2caa06b49c0319eace23470b42
CI #2024 PASS — full Android + APK / ASP / Real Clingo / gate
R2 #1891 PASS
user-reported real-device PASS
```

The later wake-actor / compact-navigation amendment is implemented and automated-green. Historical device PASS predates that presentation amendment, so do not silently treat it as acceptance of every later visual change.

### 4.2 Chef — COMPLETE / device accepted

Chef has been migrated to the square-table UI.

Implemented behavior:

- current Chef actor uses the shared wake highlight;
- actual evil players are Storyteller-only information hints;
- Recluse uses a distinct marker;
- one legal result shows one `展示信息：N` action;
- multiple legal results allow number choice and highlight only the typed witness seats contributing to the selected result;
- Spy/Recluse registration uses typed witnesses;
- poisoned/drunk arbitrary information does not fabricate truthful contributor-seat highlights.

The user reported real-device PASS for Chef on 2026-09-10 conversation context. Treat Chef as device accepted unless a new concrete defect is reported.

### 4.3 Empath — IMPLEMENTED / automated green / device acceptance pending

Empath has been migrated to the square-table UI.

Implemented behavior:

- current Empath actor uses the shared wake highlight;
- the two effective living neighbours come directly from authoritative typed `NumericResult.subjectSeats`;
- UI does not recompute adjacency and therefore does not own dead-player skipping;
- neighbour scope, actual evil hint, Recluse marker and selected-result contribution are separate visual dimensions;
- one legal result does not imply result selection;
- multiple legal results highlight only effective evil neighbours contributing under the chosen typed registration witness;
- impaired arbitrary values fail closed to no fabricated contributor-seat witness.

Do not mark Empath real-device PASS until the user explicitly reports it.

### 4.4 Undertaker — IMPLEMENTED / automated green / device acceptance pending

Undertaker has been migrated to a dedicated read-only square-table information surface.

Implemented behavior:

- current Undertaker actor uses the shared wake highlight and remains visually independent from the information-context seat;
- the player executed today comes directly from typed `InformationProposition.RoleAt.seat`;
- the shown character comes from the same typed `RoleAt` proposition;
- the UI does not infer execution identity from day state, `lastExecutedName`, labels, or localized text;
- the executed player is highlighted as read-only information context and is never presented as a selectable target;
- one legal final display gives one Show-information action;
- multiple legal final displays preserve the existing typed candidate domain and use a compact role menu rather than a long center-button grid;
- result candidates with missing/non-`RoleAt` propositions, invalid seats, or conflicting executed seats fail closed rather than fabricating context;
- recommended, unreliable and direct player-facing reveal/observation/history paths remain the existing owners;
- generic recommendation/result-first/unreliable/direct result controls are suppressed while the specialized Undertaker surface has a valid typed domain, so the migration does not leave a parallel reachable UI.

Checkpoint:

```text
4e8296600e2144a1c9790181f2b78c471e32adec
CI #2067 / run 34411490702  Android FAST + CI gate PASS
R2 #1934 / run 34411490693   PASS
exact implementation diff from a309dbcb...: 4 Undertaker-related files only
```

Do not mark Undertaker real-device PASS until the user explicitly reports it.

## 5. Intervening Ravenkeeper trigger bug — FIXED, separate from UI migration

A real-device report exposed this scenario:

```text
Monk initially protects Ravenkeeper
-> Storyteller goes back
-> Monk is reconfirmed protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies, but Ravenkeeper ability step was skipped
```

Tests proved Monk retarget semantics were already correct: the reconfirmed target replaces the old protection, and the Demon attack correctly resolves Ravenkeeper as dead.

Root cause was stale night-flow navigation: after `DemonKill` dynamically generated a Ravenkeeper/Sage-style trigger step, the same Compose callback could still inspect the pre-confirmation `nightSteps` and immediately complete the night if DemonKill had been the old last step.

Production fix `0f66b78...` introduces deferred flow reconciliation:

- dynamic actions do not finish the night from the stale list;
- request the next slot first;
- after checkpoint-derived `nightSteps` refresh:
  - enter the inserted trigger step when it exists;
  - complete the night only when refreshed flow proves no new step exists.

The fix currently marks `DemonKill` and `MayorRedirect` as flow-expanding actions and is protected by:

- `ClocktowerDynamicNightAdvanceTest`;
- `ClocktowerDynamicNightAdvanceWiringTest`;
- `ClocktowerMonkRetargetRavenkeeperDeathTest`.

Automated T4 + R2 are green at `c1ab5578...`. The exact original real-device scenario still requires user retest before claiming device acceptance of the bug fix.

Important: this correctness fix does **not** mean Ravenkeeper's Storyteller interaction UI has been migrated. Ravenkeeper remains the next UI-R5 migration target below.

## 6. Immediate next migration sequence

Start the next development slice directly here:

```text
Ravenkeeper
-> read-only inventory of every remaining active night-role Storyteller surface
-> migrate any surviving appropriate legacy surface
-> retire unreachable superseded text/list/manual-dialog UI
-> final UI-R5 T4 checkpoint
-> cross-role real-device acceptance
-> UI-R5 closeout
```

### 6.1 Undertaker — IMPLEMENTED / DEVICE PENDING

The Undertaker model planned in the previous handoff state has been implemented at `4e829660...` and is Android FAST + R2 green.

The critical ownership result is now explicit:

- typed `InformationProposition.RoleAt` owns both the executed seat and the role shown for that candidate;
- the square table consumes that authority read-only;
- no day-UI reconstruction or localized parsing is used;
- malformed or internally inconsistent typed candidate sets fall back rather than letting presentation invent semantics.

Real-device acceptance is still pending.

### 6.2 Ravenkeeper — NEXT

Ravenkeeper is a different interaction model:

- the Ravenkeeper actor is already dead but must be highlighted as the current actor/trigger owner;
- Storyteller selects one target player;
- then resolves/displays that player's role according to existing typed registration/reliability semantics;
- reuse square-table target selection rather than text/dropdown selection;
- preserve Spy/Recluse registration options and impaired misinformation legality from the rule layer;
- do not conflate the recently fixed dynamic trigger/navigation bug with the role's presentation migration.

The original Monk-retarget real-device reproduction is a useful Ravenkeeper trigger regression path after the UI migration.

## 7. Final surviving-surface audit

After Ravenkeeper, do not assume UI-R5 is complete. Perform a fresh read-only inventory of all active night actions.

For every role/action classify the Storyteller surface as:

```text
ACCEPTED SQUARE TABLE
MIGRATE
KEEP SPECIALIZED — square-table conversion would reduce clarity or violate semantics
UNREACHABLE LEGACY — delete/retire
```

The audit must explicitly find duplicated legacy text/list/manual-dialog surfaces that remain reachable after a square-table replacement.

Do not migrate a surface only for visual uniformity if its current specialized composition is the better interaction owner.

## 8. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

Per role slice:

- tests-first when a stable semantic/presentation contract needs characterization;
- no artificial domain RED for purely visual geometry;
- run focused tests during implementation;
- Android FAST at each production-code checkpoint;
- keep R2 green;
- exact diff audit, especially for large owner files;
- no full T4 after every small role unless risk requires it.

A full T4 checkpoint already passed at `c1ab5578...` after the dynamic-night fix. Take another final logical T4 after the remaining role-convergence/legacy-retirement work, before PR #117 is considered merge-ready.

Real-device acceptance remains separate from automation.

## 9. Architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable state;
- Planner/Reducer rule ownership;
- typed upstream legal/recommendation domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused change.

Do not broaden this handoff into:

- EPI-MQ scoring/explanation;
- recommendation-provider replacement;
- generalized App-root rewrite;
- Persistence follow-up;
- unrelated gameplay changes;
- A4/ZDD activation;
- D6 decomposition reopening because of source-file size.

## 10. Stop / completion condition

Do not merge PR #117 automatically.

UI-R5 becomes merge-ready only after:

```text
Undertaker accounted for
+ Ravenkeeper accounted for
+ all remaining active night-role Storyteller surfaces audited
+ appropriate square-table migrations complete
+ superseded reachable legacy surfaces retired
+ affected focused/FAST/R2 green
+ final logical T4 green
+ exact scope/diff audit green
+ cross-role real-device critical paths recorded
+ roadmap/handoff synchronized
```
