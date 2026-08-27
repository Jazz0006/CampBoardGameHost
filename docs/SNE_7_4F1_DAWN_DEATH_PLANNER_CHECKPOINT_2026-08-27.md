# SNE-7.4F-1 Dawn death planner checkpoint — 2026-08-27

Status: focused accepted; broad CI/R2 validation triggered by this docs checkpoint.

## RED

- Commit: `c7b76ca4ca131da36f49634a081bbd9f47ab12bd`
- Test: `ClocktowerDawnDeathPlannerProductionWiringTest`
- CI #833: 897 tests, exactly 2 intended failures, 4 skipped.
- Real Clingo: success.
- The RED proved that `onConfirmNight` still owned duplicate handwritten Mayor redirect / validated night-death planning instead of delegating to `NightDawnResolutionPlanner.planValidatedNightDeath()`.

## GREEN

- Production commit: `508b82a29054c2a89b402bce2605734bea307c7b`
- RED→GREEN compare: exactly one commit, one production file (`CampBoardGameHostApp.kt`), 39 additions / 21 deletions.
- Exact patch, `git diff --check`, single-production-file scope audit, focused `--rerun-tasks`, and remote-head recheck all passed in the complete GitHub Actions worktree.
- Focused suite passed:
  - `ClocktowerDawnDeathPlannerProductionWiringTest`
  - `ClocktowerMayorDemonExclusionWiringTest`
  - `ClocktowerSameNightEffectiveStateProductionWiringTest`
  - `NightDawnResolutionPlannerMayorContractTest`
  - `SNE7NightTransactionBehaviorMatrixTest`

## Accepted ownership after 7.4F-1

`NightDawnResolutionPlanner.planValidatedNightDeath()` now owns pure validated Mayor redirect / resolved night-death intent for the `onConfirmNight` production path.

App remains the durable commit authority for actual public death materialization, death/event recording, revisions, and later Dawn transaction side effects. This slice intentionally does not migrate Monk/Soldier protection, Demon succession, outcome evaluation, poison carry, Klutz/Ravenkeeper behavior, or Dawn phase commit.

## Next slice after broad validation

SNE-7.4F-2: audit and migrate the smallest remaining duplicate Dawn authority, with `onConfirmNewDemon` poison carry / Dawn completion as the leading candidate. Do not enter SNE-7.5 reconstruction until SNE-7.4F is explicitly closed.
