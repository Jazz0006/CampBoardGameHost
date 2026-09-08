# D6.2e Legacy Storyteller UI Reachability Audit

> Date: 2026-09-09 Australia/Sydney
> Branch: `codex/d6-2-ui-composition`
> Latest validated production checkpoint: `6a5723af0e9fb646d0c66e900a1c4d215d6a2fef`
> Draft PR: #115
> Status: **REACHABILITY PROVED — D6.2f DEAD LEGACY UI DELETION AUTHORIZED**

## Question

Can the trailing HostScriptCard/HostProgressCard storyteller UI inside `ClocktowerJudgeScreen` still be reached in current production, or is it dead after the newer table/square-table screens were introduced?

## Product direction constraint

The near-term Storyteller UI target is the square-table/table-based interaction surface. Older HostScriptCard-style operational screens are being retired progressively.

Therefore an unreachable legacy block should be deleted rather than decomposed or wrapped in new compatibility abstractions.

## Authoritative DayMode set

`ClocktowerDayMode` currently contains exactly eight values:

```text
Overview
Slayer
Artist
Klutz
Nomination
Vote
EndConfirm
ExecutionResult
```

Seven have explicit modern Day branches in `ClocktowerJudgeScreen`, each ending in `return`:

```text
Overview       -> ClocktowerDayOverviewScreen -> return
Nomination     -> ClocktowerPendingNominationTableScreen -> return
Vote           -> ClocktowerVoteTableScreen -> return
EndConfirm     -> ClocktowerExecutionConfirmScreen -> return
Slayer         -> ClocktowerSlayerTableScreen -> return
Artist         -> ClocktowerArtistTableScreen -> return
Klutz          -> ClocktowerKlutzTableScreen -> return
```

The remaining value, `ExecutionResult`, has no production writer.

## ExecutionResult writer proof

`clocktowerDayModeState` is created locally in `CampBoardGameHostApp.kt` and only that App scope plus `ClocktowerJudgeScreen`, which receives the exact `MutableState`, can mutate it.

Complete App references show only:

```text
initialization -> Overview
reset day flow -> Overview
reset whole flow -> Overview
Recovery -> Klutz or Overview
pass same state into Judge
Artist durable confirmation -> Overview
Day execution of Klutz -> Klutz
Night death of Klutz -> Klutz
```

There is no `ExecutionResult` occurrence in App.

The complete current Host source contains one `ExecutionResult` occurrence only:

```kotlin
ClocktowerDayMode.ExecutionResult -> Unit
```

inside the trailing legacy UI itself. It is a read-only `when` branch, not a writer.

Repository code search finds no other `ExecutionResult` reference.

Conclusion: `ExecutionResult` is a dead enum value in the current production topology and cannot provide a real entry into the legacy tail.

## Phase reachability matrix

| Phase | Relevant substate | Behavior before legacy tail | Tail reachable? |
| --- | --- | --- | --- |
| FirstNight | `nightStarted == false` | modern first-night recommendation/ready path returns | No |
| FirstNight | `nightStarted == true` | `ClocktowerNightActiveScreen` path returns; invalid empty step list fails invariant | No |
| Night | `nightStarted == false` | modern night-ready path returns | No |
| Night | `nightStarted == true` | `ClocktowerNightActiveScreen` path returns; invalid empty step list fails invariant | No |
| Dawn | any | `ClocktowerDawnSummaryScreen` returns | No |
| Day | Overview | modern table path returns | No |
| Day | Nomination | modern table path returns | No |
| Day | Vote | modern table path returns | No |
| Day | EndConfirm | dedicated confirmation path returns | No |
| Day | Slayer | modern square-table path returns | No |
| Day | Artist | modern square-table path returns | No |
| Day | Klutz | modern square-table path returns | No |
| Day | ExecutionResult | no production writer | No legal production state |

This exhausts the current `ClocktowerPhase × nightStarted × ClocktowerDayMode` state space that can be produced by App/Recovery/Judge.

## Exact legacy block boundary

In the current `ClocktowerHostScreen.kt` blob `6c403a951babdbf74b2fae00c2352bbe6c657cce`:

- the modern active-night branch ends with `return` around line 4804;
- the legacy block starts at approximately line 4807 with:

```kotlin
ClocktowerDarkTheme {
    LazyColumn(
```

- the trailing legacy `return` ends immediately before the `ClocktowerJudgeScreen` closing brace around line 5489;
- the file ends around line 5490.

Deletion slice: approximately **683 lines** (`4807..5489`), preserving the final function-closing brace.

Current file size is **330,257 bytes**. Expected reduction is roughly 40–60 KiB; exact byte reduction should be measured from the D6.2f diff rather than estimated further here.

## Legacy-only calls inside the tail

Within current Judge, these call sites occur only in the legacy tail:

```text
HostProgressCard              1
HostScriptCard                8
HostInstructionBlock          4
SelectablePlayerChips         5
StepperRow                    1
ClocktowerGameRecordPanel     1
```

D6.2f should delete the unreachable call block first. It should **not** speculatively delete helper definitions/imports in the same production change. Compile after deletion and let actual orphan usage determine any later cleanup.

## Test coupling audit

Existing relevant tests are centered on surviving behavior/table contracts rather than the legacy HostScriptCard composition:

- `ClocktowerHostTableContractTest` verifies stable seat identity, spatial slots and interaction-state projection;
- square-table/table tests cover table layout, density, vote state, nomination gesture and night/table presentation;
- `ClocktowerHostDecompositionCharacterizationTest` and `ClocktowerHostSelectionSemanticsCharacterizationTest` test pure semantics/helpers, not the trailing Compose layout.

No test/reference to `ExecutionResult` was found. No new test should be added merely to preserve an unreachable legacy UI structure.

## Decision

D6.2f is authorized as a narrow dead-code deletion:

1. remove the unreachable `ClocktowerDarkTheme { LazyColumn { ... } }` legacy storyteller tail from `ClocktowerJudgeScreen`;
2. remove dead `ClocktowerDayMode.ExecutionResult` from `ClocktowerAppModels.kt`;
3. do not modify gameplay/session/recovery/recommendation semantics;
4. do not combine `records`, `onPhaseChange`, nomination/vote ownership, Night navigation, helper-definition cleanup or import cleanup unless required to compile;
5. run `git diff --check`, Kotlin compile/focused tests, then full checkpoint CI/R2 because this is a large source deletion despite being behaviorally unreachable.

## Why deletion is preferable to decomposition

The old block has no legal production entry, duplicates modern Day/Dawn interaction flows, and points in the opposite direction from the planned square-table UI consolidation. Refactoring it would increase maintenance surface and freeze obsolete architecture. Deleting it reduces both file size and cognitive/modification radius with essentially no intended runtime behavior change.
