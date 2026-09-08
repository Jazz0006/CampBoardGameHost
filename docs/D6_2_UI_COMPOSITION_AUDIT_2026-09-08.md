# D6.2 UI Composition Boundary Audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`
> D6.2 branch base: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`
> Status: **D6.2a CHARACTERIZATION COMPLETE — SLAYER ACTIVE INTERACTION SELECTED FOR D6.2b**

## Why D6.2 exists

D6.1 solved canonical Clocktower session and dynamic GameState writer ownership. The remaining highest-value architecture debt is now the Compose/UI composition surface around `ClocktowerJudgeScreen`, not more state migration into `ClocktowerGameSession`.

## Residual audit evidence

Read-only residual-root audit:

```text
34223904849 — PASS
```

Measured hotspots:

```text
ClocktowerHostScreen.kt     329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt     241,986 bytes / 4,315 lines
ClocktowerDayScreen.kt       50,927 bytes
ClocktowerNightStepUi.kt     47,970 bytes
ClocktowerHistoryScreen.kt   38,365 bytes
ClocktowerNightScreen.kt     25,063 bytes
ClocktowerGameSession.kt     22,708 bytes

ClocktowerJudgeScreen
  parameters: 103
  callbacks:   39

App-root clocktower vars: 41
```

File size is only a signal. The more important result is the boundary fan-out: unrelated concerns are routed through one giant Judge composition surface.

The callbacks span recommendation, phase/night navigation, semantic event/observation recording, demon attack, execution, poison, Fortune Teller, Chambermaid, Ravenkeeper, Red Herring, Butler, Monk, Mayor redirect, demon succession, Klutz, Artist, Slayer, Virgin, day/night confirmation and result display.

## What D6.2 must not do

Do **not**:

- replace 39 callback parameters with one 39-function `ClocktowerJudgeActions` bag;
- replace 103 parameters with one giant state bag;
- introduce a broad Controller/ViewModel just to hide the same dependencies;
- move Compose/UI state into `ClocktowerGameSession` or domain code;
- mechanically replace `cards.toClocktowerGameState(...)` readers;
- reopen D6.1 canonical state, Recovery v2, PS5 persistence lifecycle or recommendation semantics.

A successful extraction must reduce cross-phase knowledge and change amplification, not merely reduce visible argument count.

## Existing seams to build on

The repository already has real child surfaces such as Day, Night and History screens. D6.2 should characterize how Judge currently consumes and forwards data into these children before inventing new abstractions.

Preferred architectural direction:

```text
stable shared Judge read context
+ small phase-specific transient UI state contracts
+ small phase/ability action contracts
+ existing domain/session owners beneath them
```

The exact first slice was deliberately not predetermined before D6.2a.

## D6.2a — COMPLETE: parameter/callback consumption characterization

D6.2a was performed read-only against the fresh branch `codex/d6-2-ui-composition`, based exactly on live `main` `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

The complete 103-row consumption/responsibility matrix is recorded in:

`docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Exact recount:

```text
103 total parameters
 39 on... callbacks
  3 additional function-valued providers
 10 MutableState<T> parameters
```

Therefore the 42 function-valued dependencies reconcile as 39 callbacks + 3 providers.

### Zero-consumer parameters

Two inputs are present in the Judge signature but have no Judge consumer:

- `records`
- `onPhaseChange`

They are legitimate dead-parameter cleanup candidates, but should remain separate from the first production ownership slice unless a focused diff proves combining them is still smaller and clearer.

### MutableState ownership result

The 10 `MutableState<T>` parameters do **not** share one natural owner:

- `nightStartedState` and `nightStepIndexState` participate in checkpoint/recovery restore and cannot safely become child-local state;
- `dayModeState` is externally written by recovery/Klutz routing;
- `highestVoteNameState` and `highestVoteCountState` are recovery/mechanics-coupled;
- nomination/vote selection state is a later cohesive Day candidate;
- `slayerClaimantNameState` and `slayerTargetNameState` are the clean exception: App only declares, resets and forwards them, with no recovery/session/business read found.

This explicitly rejects a mechanical `DayState`, `NightState`, `ClocktowerJudgeState` or broad Controller extraction.

### Actual child seams confirmed

Current real child owners include:

```text
ClocktowerDawnSummaryScreen
ClocktowerDayOverviewScreen
ClocktowerPendingNominationTableScreen
ClocktowerVoteTableScreen
ClocktowerSlayerTableScreen
ClocktowerArtistTableScreen
ClocktowerKlutzTableScreen
ClocktowerNightActiveScreen
  -> ClocktowerNightStepCardLocalized
```

Night callbacks are frequently adapted according to `ClocktowerNightAction`, rather than being a homogeneous action surface. That is further evidence against a mega actions bag.

### Selected smallest cohesive boundary: Slayer active interaction

D6.2b should first move only the transient Slayer selection ownership:

```text
slayerClaimantNameState
slayerTargetNameState
```

Why this boundary wins:

- exactly two tightly cohesive transient UI selections;
- current App ownership is plumbing-only;
- no Recovery/Checkpoint coupling was found;
- an existing `ClocktowerSlayerTableScreen` seam already owns the interaction presentation;
- `onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)` already carries complete selected values across the durable boundary;
- `slayerUsed`, `slayerClaimedNames`, `gameOutcome` and session/domain mutation can remain above unchanged;
- it removes real App + Judge fan-out without creating a replacement state/actions bag.

### Focused test baseline

`ClocktowerSlayerTableStateTest` already characterizes claimant eligibility, ordered claimant→target selection, living-target restriction and invalid dead-target rejection.

Therefore D6.2a does not justify adding a generic snapshot RED. Before D6.2b production edits, add a new typed RED only if the exact ownership move reveals a stable contract gap not covered by the existing Slayer table-state characterization plus compile/focused tests.

### Candidate order after Slayer

Current ranking after the audit:

1. Slayer active selection ownership;
2. Artist active selection, after redesigning confirmation to carry selected values rather than read App state;
3. nomination/vote subsets, respecting Recovery coupling;
4. whole Day dispatcher only after smaller seams are cleaner;
5. Night navigation only after a checkpoint/recovery-safe contract is characterized.

## D6.2b — NEXT: focused Slayer ownership extraction

Expected narrow production scope:

```text
move Slayer claimant/target transient selection ownership down near Slayer UI
remove App-root Slayer MutableState declarations/reset plumbing
remove those two MutableState parameters from ClocktowerJudgeScreen
preserve slayerUsed/slayerClaimedNames/gameOutcome ownership
preserve onSlayerShot as the durable value-complete action boundary
retain dayMode routing above for this first slice
preserve visible behavior exactly
```

Do not introduce `SlayerState`, `SlayerActions`, a controller or a ViewModel merely to reduce argument count.

`records` / `onPhaseChange` dead-parameter cleanup remains a separately scoped follow-up candidate rather than an automatic part of D6.2b.

## D6.2 invariants

Preserve:

- `ClocktowerGameSession` as canonical writable session/domain owner;
- exact game/player revision cadence;
- semantic chronology and idempotency;
- Recovery v2 + PS5 lifecycle/write-gate topology;
- A4 durability/invalidation ordering;
- recommendation/gameplay semantics;
- current user-visible UI behavior for structural slices;
- no Compose dependency in session/domain;
- Undercover/Werewolf isolation.

## Branch / PR strategy

D6.1 is merged and closed. D6.2 uses the fresh branch `codex/d6-2-ui-composition` from live `main` and must use a separate PR when production work is ready.

```text
main d76b0854...
-> codex/d6-2-ui-composition
-> D6.2a characterization COMPLETE
-> Slayer active interaction selected
-> D6.2b tests/characterization-first ownership extraction NEXT
-> re-audit after each completed slice
```

Do not reuse `codex/d6-root-reaudit` or PR #113.
