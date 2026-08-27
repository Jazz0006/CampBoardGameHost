# SNE-7.4F-2 new Demon checkpoint ownership checkpoint — 2026-08-27

Status: focused accepted; broad CI/R2 validation triggered by this docs checkpoint.

## RED

- Initial RED: `eecfe561f0ab6fe03c5450e5055795fbfe3bda72`.
- The first GREEN attempt exposed an over-broad source assertion: `ClocktowerNightCheckpoint(` also matched the canonical helper name `currentClocktowerNightCheckpoint()`.
- The guard was narrowed without changing production at `ce15d84d819d400ae481d47c9a36c4cefac43962` so it forbids only direct `val checkpoint = ClocktowerNightCheckpoint(` construction.
- CI #837 on the refined RED: 899 tests, exactly 2 intended failures, 4 skipped.
- Real Clingo: success.
- R2 #764: success.

The RED proves that `onConfirmNewDemon` still rebuilt the full unfinished-night checkpoint field list instead of reusing the canonical App snapshot projection.

## GREEN

- Production commit: `6188978b96059d176fe1647f7bd8d068237a0d6f`.
- RED→GREEN compare from `ce15d84d...`: exactly one commit and one production file (`CampBoardGameHostApp.kt`), 1 addition / 21 deletions.
- Production change only:
  - removed the duplicated `ClocktowerNightCheckpoint(...)` field-by-field constructor inside `onConfirmNewDemon`;
  - replaced it with `val checkpoint = currentClocktowerNightCheckpoint()`.
- Exact target-head check, exact patch, `git diff --check`, one-production-file scope audit, focused `--rerun-tasks`, and remote-head recheck all passed.
- Focused GREEN suite:
  - `ClocktowerNewDemonCheckpointProductionWiringTest`
  - `ClocktowerDawnExactDemonSuccessorWiringTest`
  - `ClocktowerNewDemonPresentationOwnershipTest`
  - `ClocktowerDemonSuccessorReducerProductionWiringTest`
  - `NightDawnResolutionPlannerContractTest`
  - `SNE7NightTransactionBehaviorMatrixTest`

## Ownership after F-2

`currentClocktowerNightCheckpoint()` remains the sole App projection for unfinished-night checkpoint state. `onConfirmNewDemon` consumes that snapshot when calling `NightDawnResolutionPlanner.confirmNewDemonIdentity()` instead of becoming a second checkpoint owner.

No poison carry, role-change materialization, outcome evaluation, Dawn phase advancement, Klutz/Ravenkeeper behavior, or reconstruction behavior was changed in this slice.

## Next after broad validation

Continue SNE-7.4F authority closeout. Audit the remaining `onConfirmNewDemon` Dawn/poison commit path and cut only the smallest demonstrable duplicate planner authority. Do not enter SNE-7.5 reconstruction until SNE-7.4F is explicitly closed.
