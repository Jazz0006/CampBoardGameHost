# CampBoardGameHost — App-root Decomposition Handoff (S7)

> Prepared: 2026-08-24 Australia/Sydney  
> Branch: `codex/source-decomposition-app-root`  
> Current structural status: **S0–S6 CLOSED; next = S7 fresh architecture audit**  
> Product A3 remains deferred until the structural pass is complete.  
> Never merge without explicit user authorization.

## 1. Current app-root progress

Original root at the start of this pass:

```text
CampBoardGameHostApp.kt = 325,556 bytes
```

Validated sizes after completed slices:

```text
After S1 = 293,536 bytes
After S2 = 283,470 bytes
After S3 = 277,453 bytes
After S4 = 261,232 bytes
After S5 = 235,741 bytes
After S6 = 229,822 bytes
```

Net reduction through S6:

```text
95,734 bytes
~29.4%
```

The 50 KiB target remains a maintainability guideline, not a hard gate.

## 2. Completed slices

### S0 — dynamic-flow preservation guard — CLOSED

Protected planner/projector/materializer authority and stable interaction identities before app-root movement.

### S1 — player setup presentation — CLOSED

New owner:

```text
AppPlayerSetupScreens.kt = 35,163 bytes
```

Root retained player state, mutation, routing, restore/start ownership.

### S2 — settings presentation — CLOSED

New owner:

```text
AppSettingsScreen.kt = 11,456 bytes
```

Root retained settings state and preference writes.

### S3 — Clocktower landing presentation — CLOSED

New owner:

```text
AppClocktowerLandingScreen.kt = 7,522 bytes
```

Root retained navigation, restore and persistence ownership.

### S4 — deal / reveal presentation — CLOSED

New owner:

```text
AppDealScreens.kt = 17,749 bytes
```

Root retained `currentDealIndex`, navigation mutation, persistence and A4 identity-reveal effect lifetime.

### S5 — results / host tools / history presentation — CLOSED

RED:

```text
48d67e607dd6af12d5a03684b86bc81ea7e39f9e
```

GREEN:

```text
8e255ff72d733c475c15f5980756c297968a89af
refactor: extract game review presentation
```

New owner:

```text
AppGameReviewScreens.kt = 27,332 bytes
```

Moved presentation ownership:

- `HostToolTab`
- `ResultsDialog`
- `HostToolsTopBar`
- `NewGameConfirmationDialog`
- `HostGameToolsScreen`
- private history/record presentation helpers

Root retained:

- `ArchivedGameReview` data/persistence contract (`private -> internal` only)
- game history state
- archive/restart transactions
- `evaluateGameOutcome`
- results/host-tools routing

Remote exact-diff audit: PASS.

### S6 — legacy generic GameScreen presentation — CLOSED

Initial RED:

```text
d5a7dddde00de528b34b1abbba368f547011d881
```

Characterization corrections:

```text
bd5c1aec540fcb3eb40150c578cff63b232816c2
  tighten substring assignment matcher

e92591b5960234c096c83252c17712fa8d1cabd1
  assert Root state ownership rather than ambiguous `=` substrings

04bc128ef8425308bad59fe7917d0894795c1268
  decouple stale S5 ownership assertions from S6
```

GREEN is the single production commit immediately after `04bc128...` on the live branch; the remote connector exposed the exact compare/diff and final blobs but did not expose the head SHA in its compact compare response. Re-query live Git before using a SHA operationally.

New owner:

```text
AppGameScreen.kt = 7,285 bytes
```

Moved exactly:

- `GameScreen`: `private -> internal`
- `PlayerStatusRow`: remains private

Root still owns and routes the generic game transaction:

```text
Screen.Game -> GameScreen(...)

cards[index] = cards[index].copy(eliminatedRound = round)
records.add(EliminationRecord(round, name))
selectedElimination = null
gameOutcome = evaluateGameOutcome(context, cards, currentGameKind)
showResults = true
round += 1
```

`evaluateGameOutcome` remains Root-owned.

Remote exact-diff audit: PASS.

## 3. Validation gate used for S1–S6

For structural GREENs, accepted gate remains:

```text
focused characterization/ownership test GREEN
+ full :app:testDebugUnitTest GREEN
+ :app:assembleDebug GREEN
+ git diff --check GREEN
+ Chat remote exact-diff audit PASS
```

GitHub Actions status may be absent on these structural branch commits; do not silently weaken the local+remote gate.

## 4. Working model

### Chat owns

- live-state audit
- candidate selection
- dependency and visibility analysis
- slice boundary
- characterization RED design
- protected Root ownership
- forbidden scope
- validation commands
- exact expected diff
- remote audit / close decision

### Luna owns only constrained mechanical execution

Luna must not choose architecture or broaden visibility/movement. If an unplanned private dependency blocks compilation, STOP and report the exact symbol.

## 5. Protected boundaries for S7+

Do not move merely for byte reduction:

- Clocktower live transaction ordering
- Compose effect lifetime (`LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberUpdatedState`)
- persistence / restore / archive ownership
- A4 lifecycle/cache/prewarm effects
- Clocktower planner/projector/materializer/session authority
- product A3 behavior

Do not turn transitional Clocktower hardcode into a new permanent-looking owner without a natural architecture seam.

## 6. Exact next task — S7 fresh architecture audit

Do **not** automatically reuse an earlier ranking.

Audit the live `CampBoardGameHostApp.kt` (~229.8 KiB) and identify the next highest-value, lowest-risk cohesive slice.

Current visible remaining categories include, but are not limited to:

- shared app presentation primitives
- Werewolf presentation helpers still Root-owned
- Clocktower-specific presentation helpers still Root-owned
- pure app labels/models/helpers
- app shell/routing-adjacent presentation

Before selecting S7, determine:

1. exact new owner filename;
2. exact declarations to move;
3. every direct cross-file dependency;
4. every required visibility change;
5. state/effect/transaction ownership that must remain in Root;
6. stale previous characterization tests that would need semantic updating;
7. focused RED failure reasons;
8. production allowlist;
9. full validation commands;
10. exact diff audit criteria and STOP conditions.

If no natural low-risk slice remains, stop the app-root pass rather than manufacturing an artificial owner.

## 7. Structural route after S7

```text
continue small characterized App-root slices while ownership stays natural
-> remeasure handwritten production files
-> audit ClocktowerDayScreen.kt (~63 KiB) only for natural seams
-> complete structural pass
-> resume A3 historical multi-night exact baseline
```

A3 remains out of scope on this branch.
