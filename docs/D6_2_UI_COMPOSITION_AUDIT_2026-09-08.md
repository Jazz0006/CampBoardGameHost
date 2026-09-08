# D6.2 UI Composition Boundary Audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`
> D6.2 branch base: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`
> Latest validated production-code checkpoint: `58bc1e51440d44f36e14d1a9d5a45cfe9c235955`
> Status: **D6.2b SLAYER OWNERSHIP COMPLETE + FULL CI PASS — D6.2c ARTIST CONTRACT CHARACTERIZATION NEXT**

## Why D6.2 exists

D6.1 solved canonical Clocktower session and dynamic GameState writer ownership. The remaining highest-value architecture debt is the Compose/UI composition surface around `ClocktowerJudgeScreen`, not more state migration into `ClocktowerGameSession`.

## D6.2 baseline evidence

Post-D6.1 residual audit `34223904849` measured:

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

File size is only a signal. The more important result is boundary fan-out: unrelated concerns are routed through one giant Judge composition surface.

## What D6.2 must not do

Do **not**:

- replace the callback surface with one giant `ClocktowerJudgeActions` bag;
- replace the parameter surface with one giant state bag;
- introduce a broad Controller/ViewModel merely to hide the same dependencies;
- move Compose/UI state into `ClocktowerGameSession` or domain code;
- mechanically replace `cards.toClocktowerGameState(...)` readers;
- reopen D6.1 canonical state, Recovery v2, PS5 persistence lifecycle or recommendation semantics.

A successful extraction must reduce cross-phase knowledge and change amplification, not merely reduce visible argument count.

## D6.2a — COMPLETE: consumption / responsibility characterization

D6.2a was performed read-only on fresh branch `codex/d6-2-ui-composition`, based exactly on live `main` `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

Complete matrix:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Exact baseline recount:

```text
103 total parameters
 39 on... callbacks
  3 additional function-valued providers
 10 MutableState<T> parameters
```

The 42 function-valued dependencies therefore reconcile as **39 callbacks + 3 providers**.

### Zero-consumer parameters

Two Judge inputs were found to have no consumer beyond the signature:

- `records`
- `onPhaseChange`

They remain legitimate dead-parameter cleanup candidates, but were deliberately excluded from D6.2b so the first ownership slice stayed exact.

### MutableState ownership result

The 10 `MutableState<T>` parameters do not share one natural owner:

- `nightStartedState` and `nightStepIndexState` participate in checkpoint/recovery restore;
- `dayModeState` is externally written by recovery/Klutz routing;
- `highestVoteNameState` and `highestVoteCountState` are recovery/mechanics-coupled;
- nomination/vote selection state is a later cohesive Day candidate;
- `slayerClaimantNameState` and `slayerTargetNameState` were the clean exception: App only declared, reset and forwarded them, with no recovery/session/business read.

This rejects a mechanical `DayState`, `NightState`, `ClocktowerJudgeState` or broad Controller extraction.

### Existing child seams confirmed

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

Night callbacks are frequently adapted according to `ClocktowerNightAction`, rather than forming one homogeneous action surface. That is additional evidence against a mega actions bag.

## D6.2b — COMPLETE: Slayer active selection ownership

### Selected boundary

D6.2b moved only the two tightly cohesive transient Slayer selections:

```text
slayerClaimantName
slayerTargetName
```

The durable boundary remained unchanged:

```text
onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)
```

### Production implementation

Validated production checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
refactor: localize Slayer selection ownership [full-ci]
```

Exact production diff from the D6.2a checkpoint:

```text
CampBoardGameHostApp.kt
  +0 / -8

ClocktowerHostScreen.kt
  +2 / -4
```

The App-root declarations, reset plumbing and Judge forwarding for the two Slayer `MutableState<String?>` values were removed.

`ClocktowerJudgeScreen` now owns those transient selections directly:

```kotlin
var slayerClaimantName by remember(gameId) { mutableStateOf<String?>(null) }
var slayerTargetName by remember(gameId) { mutableStateOf<String?>(null) }
```

Using `gameId` keeps the selection UI-local while preventing stale selection reuse across games/sessions.

### Ownership deliberately left above

D6.2b did **not** move or redesign:

- `slayerUsed`;
- `slayerClaimedNames`;
- `gameOutcome`;
- `onSlayerShot`;
- durable session/domain/history mutation;
- Recovery mechanics;
- game/player revision semantics;
- day-mode routing.

No `SlayerState`, `SlayerActions`, Controller or ViewModel was introduced.

### Post-D6.2b boundary metrics

```text
ClocktowerJudgeScreen
  parameters:           101   (103 -> 101)
  callbacks:             39   (unchanged)
  providers:              3   (unchanged)
  MutableState params:    8   (10 -> 8)

App-root clocktower vars: 39   (41 -> 39)
```

This is intentionally a small numeric reduction. Its value is that ownership genuinely moved to the UI component that consumes it rather than being hidden in another container.

### Test / acceptance evidence

Existing focused characterization:

- `ClocktowerSlayerTableStateTest`
  - claimant eligibility;
  - claimant -> target ordered selection;
  - living-target restriction;
  - dead-target rejection.

Final clean production checkpoint acceptance:

```text
R2 34283098478 — PASS

CI 34283098477 — PASS
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contract tests — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS
```

The CI run head SHA is exactly `58bc1e51440d44f36e14d1a9d5a45cfe9c235955`.

## Repository hygiene

A temporary bootstrap PR was used only because the GitHub contents API is a poor fit for safely patching a 329 KB source file. It was closed without merge, and all temporary workflow/bootstrap commits were removed from the final branch history.

The clean production history after the D6.2a docs checkpoint contains one D6.2b production commit:

```text
7de2b0741923654586ec083e298070fa31fa9d0c
  -> 58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Formal draft PR:

```text
#115 — D6.2: localize Slayer UI selection ownership
```

Do not merge it automatically.

## D6.2c — NEXT: Artist confirmation-contract characterization

The next best candidate remains Artist active selection, but it is structurally different from Slayer.

Current App-owned Artist transient values are:

```text
clocktowerArtistClaimantName
clocktowerArtistTruthfulAnswer
clocktowerArtistShownAnswer
```

Unlike Slayer, current `onConfirmArtistQuestion` reads those values from App-owned state. Therefore simply moving the three values down would break the durable confirmation boundary.

D6.2c should be **read/design-first**:

1. map the exact `ClocktowerArtistTableScreen` selection and confirmation flow;
2. inventory all reads/writes/reset paths for the three Artist transient values;
3. inspect existing Artist-focused tests and identify whether a real contract gap exists;
4. determine whether confirmation can become a narrow value-carrying callback such as claimant/truthful/shown values without moving durable behavior;
5. preserve `artistUsed`, `artistClaimedNames`, records/events, revisions and day routing above;
6. add a typed RED only if that value-carrying boundary is not already protected;
7. do not start nomination/vote or Night work in the same slice.

Do not mechanically create `ArtistState` / `ArtistActions` containers unless a tiny typed UI contract demonstrably reduces coupling.

## Candidate order after D6.2b

1. **Artist confirmation-contract characterization / selection ownership**;
2. `records` / `onPhaseChange` dead-parameter cleanup as a separate trivial slice if still useful;
3. nomination/vote subsets, respecting Recovery coupling;
4. whole Day dispatcher only after smaller seams are cleaner;
5. Night navigation only after a checkpoint/recovery-safe contract is characterized.

The exact ordering of #2 and Artist implementation may be adjusted after D6.2c evidence; do not combine unrelated cleanup merely to improve parameter counts.

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

## Branch / PR route

```text
main d76b0854...
-> codex/d6-2-ui-composition
-> D6.2a characterization COMPLETE
-> D6.2b Slayer ownership COMPLETE @ 58bc1e51440d44f36e14d1a9d5a45cfe9c235955
-> R2 34283098478 PASS
-> FULL CI 34283098477 PASS
-> draft PR #115 OPEN
-> D6.2c Artist confirmation-contract characterization NEXT
```

Do not reuse `codex/d6-root-reaudit` or PR #113.
