# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 deep transaction-application viability audit   COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE
UI-R5 pair-information square-table slice         COMPLETE / T4 + real-device accepted
UI-R5 overall campaign                            ACTIVE / final closeout audit remaining
```

D6.2 was merged through PR #115. Historical D6/R3 evidence lives under:

- `docs/archive/checkpoints/d6/`
- `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`

## 2. UI-R5 accepted pair-information checkpoint

Active branch / PR:

```text
branch codex/ui-r5-square-table-stabilization
Draft PR #117
base main 60842381dcbc3709ad453209846f66e9b4a7a777
```

Production-equivalent automated acceptance checkpoint:

```text
b960be22d217ed2caa06b49c0319eace23470b42
```

Automated evidence:

```text
CI #2024 / run 34339861281             PASS
- Android full unit tests + debug APK   PASS / executed
- ASP contract tests                    PASS / executed
- Real Clingo cross-validation          PASS / executed
- CI gate                               PASS
R2 #1891 / run 34339861261              PASS
```

The immediately preceding production/test head `ecfacb7b11d39e65febef89fb3e058390f9eea5f` also passed ordinary FAST CI #2022 and R2 #1889.

Real-device acceptance was reported PASS by the user on 2026-09-09 Australia/Sydney and is recorded in:

- `docs/UI_R5_PAIR_INFORMATION_REAL_DEVICE_ACCEPTANCE_2026-09-09.md`

Exact device model / Android build were not supplied in this chat and are intentionally not invented in the acceptance record.

The accepted pair-information flow covers Washerwoman / Librarian / Investigator:

```text
recommended square-table preview
-> in-place Manual edit on the same table
-> existing pair legality / seat-state projection
-> existing show/confirm/player-display path
```

The accepted implementation did not change square-table geometry, recommendation scoring, gameplay semantics, session ownership, persistence or player-display commit authority.

## 3. Current priority

> **CURRENT: UI-R5.5 final read-only closeout audit.**

The pair-information slice itself is fully accepted and must not be reopened without a concrete defect or new requirement.

Before changing project priority to EPI-MQ, perform one final read-only audit of the remaining active Storyteller square-table surfaces and answer one question:

> Is there any remaining UI-R5 consolidation / device-stabilization work that is materially required, or is the campaign ready to close?

The global execution order remains:

```text
UI-R5 final closeout audit
-> if no remaining material work: merge/close UI-R5 and move to EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

Do not start EPI-MQ implementation before UI-R5 closeout. Do not reopen D6 merely because `CampBoardGameHostApp.kt` remains large.

Current active handoff:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`

Primary product/UI reference:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 4. UI-R5 pair-information acceptance summary

### UI-R5.0 / ownership

For the pair-information slice, the reusable owners are:

```text
ClocktowerPairManualAuthority          complete typed Manual legality
ClocktowerPairManualSelectionModel     two-seat draft interaction state
clocktowerPairManualSeatState          legal selectable/selected/disabled projection
ClocktowerSquareTableSeatSurface       shared geometry/rendering
existing player-display resolution     final commit/reveal handoff
```

No generic callback/state mega-bag was introduced.

### UI-R5.1 / characterization

Durable evidence protects:

- recommendation-to-Manual canonicalization by structured pair semantics;
- recommendation seeding into Manual mode;
- fail-closed invalid/stale recommendation behavior;
- recommended read-only seat highlighting;
- Manual selectable/selected/disabled states;
- legal second-seat continuation after entering Manual from a recommendation.

### UI-R5.2 / consolidation

`ClocktowerPairInformationSquareTableUi.kt` is the thin composition owner for:

```text
recommended read-only preview
-> in-place Manual edit
-> existing show/confirm handoff
```

It does not own recommendation ranking, legal-domain generation, durable observation/history, Recovery, or canonical GameState.

### UI-R5.3 / flow convergence

Washerwoman / Librarian / Investigator now use the same square-table interaction language rather than the old text/list + separate Manual dialog flow.

### UI-R5.4 / real-device stabilization

**Status: PASS for the pair-information slice.**

User-reported device validation passed, including the required pair-information interaction and dense-layout acceptance surface. See the dedicated acceptance record for scope and evidence wording.

### UI-R5.5 / acceptance closeout

**Status: NEXT.**

The only remaining UI-R5 action is a read-only campaign closeout audit. Do not add code merely to produce activity. If the audit finds no remaining material gap, UI-R5 is ready to close and PR #117 can move to merge readiness.

## 5. Frozen architecture after D6/R3

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- pure planner/reducer ownership of rules/transaction semantics and exactly-once intent planning;
- existing App ownership of remaining cross-boundary application choreography and Compose-facing projection;
- exact revision cadence and semantic chronology;
- action/observation idempotency and non-mutating preflight;
- Recovery v2 current-version-only policy and existing trigger topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- no storyteller-hidden target leak;
- Undercover/Werewolf isolation.

A future product requirement may justify a new application seam, but file size alone does not.

## 6. Scope fence

UI-R5 does **not** authorize:

- EPI-MQ scoring or explanation;
- recommendation-provider replacement;
- generalized architecture rewrite of `CampBoardGameHostApp.kt`;
- Persistence Simplification follow-up;
- gameplay/rules fixes unrelated to a UI-R5 defect;
- new scripts/characters merely to test layout;
- A4/ZDD production activation.

If the final audit exposes a real defect, isolate it as a focused tested slice. Otherwise close the campaign rather than continuing speculative refactoring.

## 7. Testing / validation authority

Read and follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`.

The pair-information production-equivalent T4 checkpoint is accepted at `b960be22d217ed2caa06b49c0319eace23470b42`; later status/acceptance records are documentation-only and do not invalidate that evidence.

A read-only closeout audit requires no new Android regression. Any new production change discovered by that audit must re-enter the risk-based T0–T4 model according to its actual semantic risk.

## 8. UI-R5 completion condition

UI-R5 can close when the final read-only audit confirms:

```text
shared square-table ownership audited
+ approved consolidation implemented
+ focused/FAST affected validation green
+ T4 logical acceptance checkpoint green
+ exact diff / scope audit green
+ real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ no remaining material UI-R5 surface gap
```

All conditions except the final cross-surface closeout confirmation are now satisfied for the pair-information work.

## 9. Current authoritative reading order

For a new development session read, in order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`;
5. `docs/UI_R5_PAIR_INFORMATION_REAL_DEVICE_ACCEPTANCE_2026-09-09.md`;
6. `docs/TESTING_STRATEGY.md`;
7. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
8. specialized semantic/product docs only as required by the closeout audit.
