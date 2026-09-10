# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — ROLE MIGRATIONS + LEGACY RETIREMENT COMPLETE / FINAL T4 IN PROGRESS**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/TESTING_STRATEGY.md`;
5. `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`;
6. `docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`.

D6/R3 are closed; do not reopen them by default.

## 2. Live branch / checkpoints

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest prior full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
Spy checkpoint: d45f96ddcdb1142a422d64ee87cf61c5475121f9
Clockmaker verified head: 9119ec83f036432ec9b5f0f3a920690ab9719e16
Sage materializer owner: d82e1254bdb413445adb1fc2e66bb4401eea719e
Sage Host delegation: b9a67a4dc7a336d1a8693f76aa383646e3c063d7
legacy pair-manual retirement: 9e67fd53b21089a84c897e73ca9abb4c0f4ce17d
generic result cleanup production: 5cf762c81c333a52e7d744499ff1f1dd28e20c61
post-cleanup production tree head: e84e01c2a8f5064814077d0583e684d2f42b09d7
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

## 3. Sage ownership extraction — implemented

The Sage slice is split by ownership rather than left inside the protected Host.

Final dependency direction:

```text
ClocktowerHostScreen
  - owns current Demon / Sage death trigger / effective ability state / card order
  - passes prepared facts only
        |
        v
ClocktowerSageStepMaterializer
  - owns Sage pair candidate projection
  - owns Sage recommendation/display metadata
  - owns presentationSubjectSeats
  - builds the Sage information step through ClocktowerInformationStepBuilder
        |
        v
ClocktowerNightStepUi / ClocktowerSageSquareTableUi
  - owns result projection and rendering
```

Preserved invariants:

- Host/session/canonical flow remain authoritative for whether Sage was actually killed by the Demon;
- Host remains authoritative for current Demon and effective death-trigger ability state;
- `ClocktowerInformationStepBuilder` still owns generic reliable/unreliable/manual/automatic information-step mechanics;
- unreliable Sage choices remain `proposition = null`;
- typed `presentationSubjectSeats` are presentation-only and are not converted into epistemic truth;
- no parsing of `displaySecondary` is used to recover seat identity;
- global `clocktowerInformationCandidateId(...)` remains unchanged;
- no other role materializer was extracted as part of this slice.

## 4. Sage validation evidence

Sage one-shot run:

```text
run: 34429902491
job: 102722989741
result: PASS
```

It executed focused Sage tests, Android FAST, exact trigger/source checks, `git diff --check`, production changed-file allowlisting, Host delegation checks, and one-shot self-cleanup.

## 5. Legacy pair-manual retirement — complete

Production checkpoint:

```text
9e67fd53b21089a84c897e73ca9abb4c0f4ce17d
refactor: retire legacy pair manual dialog
```

Removed only:

- `ClocktowerPairManualSelectionDialog`;
- `ClocktowerPairManualCenterControls`;
- their now-unused Compose imports.

Retained:

- `clocktowerPairManualSquareTableSeat(...)`;
- `clocktowerPairManualSeatState(...)`;
- `ClocktowerPairManualSelectionModel`;
- valid typed/ownership tests used by the accepted pair-information square table.

Automated validation:

```text
CI #2133 / run 34433654157
- Android FAST PASS
- CI gate PASS
R2 #2000 / run 34433654190 PASS
```

No full T4 was required for that isolated UI dead-code retirement.

## 6. Generic result reachability cleanup — complete

Architecture pre-flight and reachability proof are recorded in:

`docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`

The pre-flight decision was **KEEP routing ownership in `ClocktowerNightStepUi`**. This slice removed duplicate/unreachable presentation routing; it did not add domain, registration, recommendation, or session authority.

Production checkpoint:

```text
5cf762c81c333a52e7d744499ff1f1dd28e20c61
refactor: retire superseded generic result surfaces
```

Persistent production changes are limited to `ClocktowerNightStepUi.kt`:

1. remove `nonPairResultFirstCandidates` derivation;
2. remove its superseded generic result-first render block;
3. exclude Chambermaid from generic recommendation rendering;
4. exclude Chambermaid from generic unreliable-result rendering;
5. retain generic direct reveal and valid generic recommendation/unreliable fallbacks for remaining owners.

The focused ownership RED was real and explicit:

```text
ClocktowerNightStepResultSurfaceOwnershipTest
2 tests executed
2 expected assertion failures before production cleanup
```

After the production patch, the same focused class passed.

The first FAST run then exposed two obsolete source-shape assertions in Ravenkeeper/Undertaker wiring tests that simultaneously claimed specialized ownership while requiring the superseded generic result-first surface to exist. Only those obsolete assertions were retired; all specialized owner and suppression assertions remain.

Final one-shot run:

```text
run: 34434862249
job: 102737709222
result: PASS
```

The run completed all of the following successfully:

```text
exact trigger/head/blob gate
focused ownership RED proof
exact production patch
production diff audit
focused ownership GREEN
Android FAST --rerun-tasks
production commit + push
one-shot self-cleanup
```

The post-cleanup head is:

```text
e84e01c2a8f5064814077d0583e684d2f42b09d7
```

Its normal PR CI/R2 entries show `action_required` with zero jobs because that head was authored by `github-actions[bot]`; this is not accepted as validation and is not treated as a test failure.

## 7. Final logical UI-R5 validation

All planned role migrations and planned legacy/reachability cleanup are now implemented. The next required checkpoint is the final logical UI-R5 T4.

This handoff update is intentionally committed by the normal connector identity with `[full-ci]` so CI runs against the same production tree plus documentation only.

Required T4 evidence:

```text
Android full unit suite
assemble/debug APK gate selected by full checkpoint
ASP contract validation
Real Clingo cross-validation
aggregate CI gate
R2 main-thread boundary
```

Do not downgrade a selected T4 component to FAST. If a T4 component fails, investigate the concrete failure before proceeding to device acceptance.

## 8. Scope fence

Preserve:

- `ClocktowerGameSession` writable authority;
- Planner/Reducer rule ownership;
- current recommendation semantics and ranking;
- revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- current Spy Grimoire reveal behavior;
- current dynamic Ravenkeeper-trigger correctness fix;
- New Demon identity specialized confirmation surface.

Do not broaden into EPI-MQ, recommendation-provider redesign, Persistence, unrelated gameplay changes, A4/ZDD feature work, generalized App-root rewrite, or renewed D6 decomposition.

## 9. Remaining execution sequence

```text
1. complete final logical UI-R5 T4
2. perform exact final scope/diff audit if T4 is green
3. cross-role real-device acceptance / pending device retests
4. synchronize roadmap/handoff with final T4 evidence
5. UI-R5 closeout
6. merge only with explicit user authorization
```
