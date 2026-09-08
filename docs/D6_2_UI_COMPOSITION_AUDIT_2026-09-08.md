# D6.2 UI Composition Boundary Audit

> Date: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`
> D6.2 branch base: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`
> Latest validated production-code checkpoint: `6a5723af0e9fb646d0c66e900a1c4d215d6a2fef`
> Status: **D6.2a/b/c/d COMPLETE + FULL CI PASS — D6.2e LEGACY STORYTELLER UI REACHABILITY AUDIT NEXT**

## Why D6.2 exists

D6.1 solved canonical Clocktower session and dynamic GameState writer ownership. The dominant residual debt is now the Compose/UI composition surface around `ClocktowerJudgeScreen`, not more state migration into `ClocktowerGameSession`.

D6.2 optimizes **responsibility ownership and modification radius**, not file size for its own sake.

## Product/UI direction that now constrains the architecture

Near-term Storyteller UI direction is explicit:

> **Operational Storyteller interactions should converge on the square-table/table-based UI. Older HostScriptCard-style interaction screens are being retired progressively.**

Consequences:

- surviving square-table/table components are the preferred future ownership seams;
- do not create State/Actions/Controller/ViewModel abstractions merely to preserve old HostScriptCard composition;
- do not add tests that freeze obsolete layout/composition structure unless required by a still-reachable behavior contract;
- legacy paths may share the same narrow state/action helper only for minimum migration compatibility;
- if an older UI block is already unreachable, prove and delete it rather than decomposing it.

This direction does not authorize gameplay/recovery/recommendation semantic changes inside D6.2.

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

File size is a signal, not the target. The important result was giant cross-phase fan-out through one Judge composition surface.

## What D6.2 must not do

Do **not**:

- replace callbacks with one giant `ClocktowerJudgeActions` bag;
- replace parameters with one giant Judge/Day state bag;
- introduce a broad Controller/ViewModel to hide the same coupling;
- move Compose/UI state into `ClocktowerGameSession` or domain code;
- mechanically replace `cards.toClocktowerGameState(...)` readers;
- reopen D6.1 canonical state, Recovery v2, PS5 lifecycle or recommendation semantics;
- invest architecture effort in legacy UI that is already scheduled for retirement and may be unreachable.

## D6.2a — COMPLETE: consumption / responsibility characterization

Complete matrix:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Exact baseline recount:

```text
103 total parameters
 39 on... callbacks
  3 additional function-valued providers
 10 MutableState<T> parameters
```

### Zero-consumer parameters

Two Judge inputs had no consumer beyond the signature:

- `records`
- `onPhaseChange`

They remain separate trivial cleanup candidates. They were deliberately not mixed into ownership slices.

### MutableState ownership result

The baseline 10 `MutableState<T>` parameters did not form one natural state object:

- `nightStartedState` / `nightStepIndexState`: checkpoint/recovery coupled;
- `dayModeState`: externally written by recovery/Klutz routing;
- `highestVoteNameState` / `highestVoteCountState`: recovery/mechanics coupled;
- nomination/vote state: cohesive but broader Day flow;
- Slayer claimant/target were the clean UI-local exception.

This rejected mechanical `DayState`, `NightState`, `ClocktowerJudgeState` or broad Controller extraction.

### Existing surviving child seams

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

These table/specialized screens align with the newly explicit square-table UI direction.

## D6.2b — COMPLETE / VALIDATED: Slayer active selection ownership

Validated production checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
refactor: localize Slayer selection ownership [full-ci]
```

Exact production diff:

```text
CampBoardGameHostApp.kt
  +0 / -8

ClocktowerHostScreen.kt
  +2 / -4
```

Judge now owns:

```kotlin
var slayerClaimantName by remember(gameId) { mutableStateOf<String?>(null) }
var slayerTargetName by remember(gameId) { mutableStateOf<String?>(null) }
```

The complete durable boundary stayed unchanged:

```text
onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)
```

Post-D6.2b metrics:

```text
ClocktowerJudgeScreen
  parameters:           101
  callbacks:             39
  providers:              3
  MutableState params:    8

App-root clocktower vars: 39
```

Acceptance:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS
  Android FULL unit tests — PASS
  debug APK — PASS
  ASP — PASS
  Real Clingo — PASS
  CI gate — PASS
```

## D6.2c — COMPLETE: Artist confirmation-contract characterization

Detailed audit:

- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

Unlike Slayer, Artist originally made a round trip through App transient state:

```text
Judge selection
-> selection callback
-> App transient state
-> value forwarded back to Judge
-> onConfirmArtistQuestion()
-> App rereads transient values
```

Selected durable boundary:

```text
onConfirmArtistQuestion(
  claimantName: String,
  truthfulAnswer: Boolean,
  shownAnswer: Boolean,
)
```

The three selections remain UI concerns; `artistUsed`, `artistClaimedNames`, records/events, Day routing and revision remain durable App orchestration.

## D6.2d — COMPLETE / VALIDATED: Artist selection ownership

Latest validated production checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
refactor: localize Artist selection ownership [full-ci]
```

Exact net production diff from D6.2c docs checkpoint `7a80d4c690e41190c5d70430733056caf05b1523`:

```text
CampBoardGameHostApp.kt
  +19 / -50

ClocktowerHostScreen.kt
  +64 / -46

production files changed: exactly 2
production commits ahead: exactly 1
```

### Ownership result

App lost:

```text
clocktowerArtistClaimantName
clocktowerArtistTruthfulAnswer
clocktowerArtistShownAnswer
```

plus their reset/forwarding plumbing and three selection callbacks.

Judge now owns the transient selection with `remember(gameId)` and narrow helpers enforcing the existing transition semantics:

```text
claimant change -> clear truthful + shown
truthful change -> clear shown
shown change -> update shown only
confirm -> pass all three values to durable callback, then clear local selection
```

Durable App behavior is unchanged in responsibility:

- update `artistClaimedNames`;
- update `artistUsed` for a real unused Artist;
- add record;
- add RoleAction event containing exact truthful/shown values;
- route Day back to Overview;
- advance game-state revision.

### Square-table versus legacy compatibility

The modern square-table Artist path is the intended surviving UI.

A second older HostScriptCard Artist consumer was discovered during implementation. D6.2d did **not** introduce a special legacy abstraction. It temporarily reuses the same Judge-local selection helpers solely so the old block continues compiling while reachability is audited.

### Compile correction

Moving `artistTruthfulAnswer` from a stable parameter to a Compose delegated property exposed Kotlin smart-cast restrictions. The first full-CI attempt failed only at `compileDebugKotlin`.

The correction was deliberately narrow:

- square-table Artist path snapshots the nullable delegated truth value into an immutable local value for Boolean calculations;
- legacy compatibility path does the same;
- no durable/gameplay/recommendation semantics changed.

The branch history was rebuilt afterward so failed/bootstrap commits do not remain in the final production line.

### Post-D6.2d metrics

```text
ClocktowerJudgeScreen
  parameters:            95   (103 -> 95)
  callbacks:             36   (39 -> 36)
  providers:              3
  MutableState params:    8   (10 -> 8)

App-root clocktower vars: 36   (41 -> 36)
```

### Final acceptance

```text
R2 34286858464 — PASS

CI 34286858453 — PASS
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contract tests — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS
```

CI head SHA is exactly `6a5723af0e9fb646d0c66e900a1c4d215d6a2fef`.

## D6.2e — NEXT: legacy Storyteller fallback reachability audit

A preliminary control-flow read found a potentially higher-value next boundary than another local state migration.

Before the old trailing storyteller UI, modern paths already return for:

```text
Dawn -> ClocktowerDawnSummaryScreen -> return

Day Overview -> table UI -> return
Day Nomination -> table UI -> return
Day Vote -> table UI -> return
Day EndConfirm -> dedicated screen -> return
Day Slayer -> square-table UI -> return
Day Artist -> square-table UI -> return
Day Klutz -> square-table UI -> return

FirstNight && !nightStarted -> setup recommendation UI -> return
Night && !nightStarted -> night-ready UI -> return
(FirstNight || Night) && nightStarted -> ClocktowerNightActiveScreen -> return
```

After these branches the file still contains a large old:

```text
ClocktowerDarkTheme {
  LazyColumn {
    HostProgressCard / HostScriptCard based Dawn/Day interaction flows
    ...
  }
}
```

This tail now looks potentially **entirely unreachable**, not merely deprecated. That has not yet been proven exhaustively.

### Required D6.2e proof

Read-only first:

1. locate authoritative `ClocktowerDayMode` definition;
2. enumerate every DayMode value;
3. build exhaustive `ClocktowerPhase × nightStarted × dayMode` reachability coverage;
4. prove whether every legal state returns before the legacy tail;
5. identify exact old block start/end range;
6. inventory helpers/imports/functions that become dead only after tail removal;
7. audit tests for legacy-only presentation dependencies;
8. estimate byte/line reduction and risk;
9. authorize a deletion slice only if proof is complete.

If proven unreachable, D6.2f should **delete** the dead legacy UI instead of decomposing it.

Do not mix the reachability proof with dead-parameter cleanup, nomination/vote ownership, Night checkpoint/navigation, Recovery or recommendation semantics.

## Candidate order after D6.2d

1. **D6.2e legacy storyteller reachability proof**;
2. **D6.2f dead legacy UI deletion if proven unreachable**;
3. `records` / `onPhaseChange` cleanup as a separate tiny slice if still useful;
4. nomination/vote subsets with Recovery coupling explicitly preserved;
5. whole Day dispatcher only around surviving table UI seams;
6. Night navigation only after checkpoint/recovery-safe ownership characterization.

## D6.2 invariants

Preserve:

- `ClocktowerGameSession` as canonical writable session/domain owner;
- exact game/player revision cadence;
- semantic chronology and idempotency;
- Recovery v2 + PS5 lifecycle/write-gate topology;
- A4 durability/invalidation ordering;
- recommendation/gameplay semantics;
- no Compose dependency in session/domain;
- Undercover/Werewolf isolation.

## Branch / PR route

```text
main d76b0854...
-> codex/d6-2-ui-composition
-> D6.2a characterization COMPLETE
-> D6.2b Slayer ownership VALIDATED @ 58bc1e5...
-> D6.2c Artist contract characterization COMPLETE
-> D6.2d Artist ownership VALIDATED @ 6a5723a...
-> R2 34286858464 PASS
-> FULL CI 34286858453 PASS
-> draft PR #115 OPEN / DO NOT AUTO-MERGE
-> D6.2e legacy Storyteller reachability audit NEXT
```

Do not reuse `codex/d6-root-reaudit` or PR #113.
