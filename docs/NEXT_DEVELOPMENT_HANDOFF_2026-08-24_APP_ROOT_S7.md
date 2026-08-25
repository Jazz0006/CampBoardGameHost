# CampBoardGameHost — App-root Decomposition Handoff (S7+)

> Refreshed: 2026-08-25 Australia/Sydney
> Stable main: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Current structural branch: `codex/source-decomposition-app-root-s7-5-new-demon-presentation`
> Latest closed structural code checkpoint: `bcd8b9b2dad4f3cde42bef2cdb6d1eea7839dd7e` (S7.4 GREEN)
> Current structural status: **S0–S7.4 CLOSED; S7.5 New Demon presentation ACTIVE**
> Future A3 setup-snapshot/authority work remains out of scope here.
> Never merge, force-push, rebase, mark ready or broaden scope without explicit user authorization.

## 1. Branch lineage and checkpoints

Stable integration base after PR #50:

```text
0311c3bb54ea71be69bc60a4aae642e0f39cd900
```

S7.3 checkpoint:

```text
b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
refactor: extract shared host interaction UI
```

S7.4 tests-first lineage:

```text
b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
  -> 4a46210cf11a0c45ceffef7bfaa647e53e99747f  test: define shared game settings UI ownership
  -> 843f61e5106b704417b1cd347c47393264215bb8  test: retire stale root ownership assertions
  -> bcd8b9b2dad4f3cde42bef2cdb6d1eea7839dd7e  refactor: extract shared game settings UI
  -> 7c36d7d1fb95d9bae66439acd3523e86ab9b6e72  docs: sync post-S7.4 decomposition plan
```

S7.5 is stacked from the clean S7.4 docs checkpoint `7c36d7d1...`; the separate S7.4 documentation-recovery history is deliberately not part of this branch lineage.

## 2. Root-size progress

Original Root:

```text
CampBoardGameHostApp.kt = 325,556 bytes
```

Validated progression:

```text
After S1   = 293,536 bytes
After S2   = 283,470 bytes
After S3   = 277,453 bytes
After S4   = 261,232 bytes
After S5   = 235,741 bytes
After S6   = 229,822 bytes
After S7.1 = 229,007 bytes
After S7.2 = 228,172 bytes
After S7.3 = 220,403 bytes
After S7.4 = 218,086 bytes (~213 KiB)
```

Current dedicated owners of note:

```text
ClocktowerPresentationTheme.kt =   985 bytes
HostInteractionUi.kt           = 8,901 bytes
GameSettingsUi.kt              = 3,112 bytes
```

The 50 KiB target remains a maintainability guideline, not a hard correctness gate. Do not manufacture artificial abstractions just to reach a byte target.

## 3. Closed slices relevant to current work

### S7.1 — shared Clocktower theme — CLOSED

Moved `ClocktowerDarkTheme` from Root to `ClocktowerPresentationTheme.kt`.

### S7.2 — Clocktower selection semantics — CLOSED

Moved:

```text
TwoPlayerSelectionAction
twoPlayerSelectionAction(...)
shouldAutoAdvanceRedHerring(...)
```

into `ClocktowerHostSelectionSemantics.kt`.

The permanent invariant is semantics ownership; UI helper location is not part of the S7.2 contract.

### S7.3 — shared host interaction UI — CLOSED

Owner:

```text
HostInteractionUi.kt
```

Moved exactly:

```text
HostProgressCard
HostScriptCard
HostInstructionBlock
HostActionSection
SelectablePlayerChips
SelectableTwoPlayerChips
SelectableSeatNumbers
```

Root after S7.3: 220,403 bytes. Owner size: 8,901 bytes. Remote exact-diff audit passed. No GitHub Actions run was observed on that head, so do not describe it as remotely CI-green.

### S7.4 — shared game-settings presentation — CLOSED

Owner:

```text
GameSettingsUi.kt
```

Moved exactly:

```text
GameSettingsHeader
StepperRow
```

The first T1 run exposed two stale structural assertions. Their real invariants were migrated:

- `AppDealPresentationOwnershipTest` now protects that shared primitives remain outside the S4 deal owner rather than requiring permanent Root ownership.
- `ClocktowerHostSelectionSemanticsOwnershipTest` now protects that selection UI remains outside the semantics owner rather than requiring permanent Root ownership.

`GameSettingsUiOwnershipTest` independently owns the current S7.4 location contract.

Reported local gates after repair:

```text
T0 focused ownership tests     GREEN
T1 :app:testFast               GREEN
T2 :app:assembleDebug          GREEN
git diff --check               PASS
exact local diff audit         PASS
remote exact-diff audit        PASS
```

No GitHub Actions run was observed on the S7.4 branch. Remote CI GREEN is therefore not claimed.

`EmptyStateCard` remains Root-owned and intentionally deferred.

## 4. Current Root ownership model

The remaining Root has three broad zones.

### Front — models / prefs / JSON / setup helpers

Potential future S8 candidates exist here, but this area is materially riskier because of setup identity, saved-game compatibility, persistence and A3 semantics. Do not enter it during remaining S7 work.

### Middle — protected application orchestrator

Do not move merely for size:

- `remember` and mutable Compose state;
- `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberUpdatedState` lifetimes;
- navigation state;
- active-game persistence and restore timing;
- `ClocktowerGameSession` ownership;
- action / observation / history ordering;
- revision state;
- async recommendation lifetime;
- cross-game reset behavior;
- day/night transaction callbacks;
- Demon transition and public-history ordering.

### Tail — remaining presentation candidates

After S7.4, the only planned S7 candidates are:

```text
ClocktowerNewDemonConfirmationScreen
WerewolfRoleLine
WerewolfPlayerStatusRow
```

`EmptyStateCard` is deferred; do not create a generic `Utils.kt` or `CommonUi.kt` bucket for it.

## 5. S7.5 — New Demon presentation — ACTIVE

Fresh preflight confirms the move candidate:

```text
ClocktowerNewDemonConfirmationScreen

CampBoardGameHostApp.kt
  -> clocktower/ui/ClocktowerNightScreen.kt
```

Current declaration is a presentation-only composable taking only:

```text
newDemonLabel: String
hasNewDemon: Boolean
onShowPlayerDisplay: () -> Unit
onConfirm: () -> Unit
```

It owns no `remember`, mutable state, effect, session, persistence, planner or transaction authority.

Current Host wiring is:

```kotlin
pendingNewDemonName?.let { newDemonName ->
    val newDemon = cards.firstOrNull { it.name == newDemonName }
    val newDemonStep = ClocktowerNightStepUi(...)
    ClocktowerNewDemonConfirmationScreen(
        newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
        hasNewDemon = newDemon != null,
        onShowPlayerDisplay = { playerDisplayStep = newDemonStep },
        onConfirm = onConfirmNewDemon,
    )
    return
}
```

Protected boundaries for S7.5:

- Host keeps `pendingNewDemonName` handling;
- Host keeps `newDemon` lookup;
- Host keeps `newDemonStep` construction;
- Host keeps `playerDisplayStep` mutation;
- Host/session keeps Imp self-kill succession transaction authority;
- do not change `onConfirmNewDemon` behavior;
- do not change actual successor selection;
- do not change identity-display routing;
- do not change public history/audit ordering;
- do not change night-step advancement;
- do not move or edit planner/projector/materializer/session authority;
- do not touch A3/B4/A4, persistence or recommendation authority.

The preferred owner is confirmed as `clocktower/ui/ClocktowerNightScreen.kt`: it already owns stateless Clocktower night presentation (`ClocktowerNightActiveScreen`, `ClocktowerNightReadyCard`), uses the same Kotlin package, already imports the Compose/theme dependencies required by the moved function, and currently measures 17,833 bytes.

No visibility widening or consumer edit should be required.

## 6. S7.5 tests-first guard

Add a focused ownership/characterization RED that proves:

1. Root, NightScreen and HostScreen sources exist.
2. `ClocktowerNightScreen.kt` declares `internal fun ClocktowerNewDemonConfirmationScreen(`.
3. Root no longer declares the function.
4. Host still retains the `pendingNewDemonName?.let` branch, new-Demon lookup and `ClocktowerNightStepUi` construction.
5. Host wiring still contains exactly the protected values/callbacks:
   - `newDemonLabel = newDemon?.seatLabel(cards).orEmpty()`
   - `hasNewDemon = newDemon != null`
   - `onShowPlayerDisplay = { playerDisplayStep = newDemonStep }`
   - `onConfirm = onConfirmNewDemon`
6. Do not add UI-string/body-copy assertions.
7. Do not add a file-size assertion.

Affected regression protection includes:

- `ClocktowerAdvanceNightStepTransactionOwnershipTest`
- `ClocktowerPlayerDisplayUiOwnershipTest`
- `ClocktowerNightStepUiOwnershipTest`
- `AppRootDynamicFlowDecompositionGuardTest`

The existing advance-night-step test protects confirmation/audit/registration/semantic-record/finalization ordering in Host. The dynamic-flow guard protects planner/projector/materializer authority from returning to Root.

## 7. S7.6 — Werewolf presentation leftovers

Only after S7.5 closes, audit:

```text
WerewolfRoleLine
WerewolfPlayerStatusRow
-> werewolf/WerewolfHostScreen.kt
```

After S7.6: STOP, remeasure Root and run a fresh S8 architecture audit. Do not automatically continue into models, setup or persistence.

## 8. Protected boundaries for all remaining S7 work

Do not move merely for byte reduction:

- Clocktower live transaction ordering;
- Compose effect lifetime;
- persistence / restore / archive ownership;
- A4 cache/prewarm/runtime lifecycle;
- `ClocktowerGameSession` global timeline identity/sequence;
- action/observation commit ordering;
- planner/projector/materializer authority;
- merged A3/B4 historical-exact semantics;
- Host recommendation/registration transaction semantics.

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze. Further extraction is allowed only when a cohesive owner is obvious and behavior can be characterized without weakening lifecycle, callback ordering, recommendation/session ownership or timeline identity.

## 9. Large-file execution workflow — HARD RULE

`CampBoardGameHostApp.kt` is 218,086 bytes (~213 KiB) after S7.4. Do **not** use the GitHub Connector for production rewrites/moves in this file.

### ChatGPT owns

- live-state audit;
- architecture and ownership decision;
- production/test symbol inventory;
- dependency and visibility audit;
- stale structural-contract classification;
- tests-first RED design;
- exact declaration-move specification;
- changed-file allowlist;
- validation commands;
- exact-diff acceptance criteria;
- STOP conditions;
- post-push remote audit.

### Luna in the user's full local Git worktree owns

- writing/running the approved RED;
- mechanical declaration move;
- minimal compile-required imports/visibility edits;
- focused T0 tests;
- T1 `:app:testFast`;
- affected T2 checks;
- `:app:assembleDebug` for moved Compose production declarations;
- `git diff --check`;
- exact local diff review;
- commit and push after approved gates pass.

ChatGPT must not directly rewrite Root production code through the connector for these slices.

## 10. Tests-first contract

For every remaining S7 extraction:

1. re-check live `main`, structural head and parent chain;
2. inventory every declaration/caller/test reference for target symbols;
3. classify stale structural tests before RED design;
4. add focused ownership/characterization RED;
5. observe RED for the intended ownership reason;
6. perform the smallest behavior-preserving move;
7. minimize visibility widening;
8. run T0 focused GREEN;
9. run T1 `:app:testFast`;
10. run T2 affected regression/compile checks;
11. trigger T3 only if the semantic area requires it;
12. leave applicable FULL coverage to the PR T4 gate unless uncertainty requires earlier escalation;
13. run `git diff --check` and exact diff audit;
14. STOP on unexpected dependency, visibility, lifecycle or transaction requirements.

Structural flow:

```text
T0 focused RED/GREEN
-> T1 :app:testFast
-> T2 affected checks / assemble
-> triggered T3 only when required
-> PR T4 applicable FULL
```

If impact is uncertain, escalate upward. Do not weaken T4.

## 11. Planned route

```text
S7.1 shared Clocktower theme             CLOSED
-> S7.2 selection semantics              CLOSED
-> S7.3 shared host interaction UI       CLOSED
-> S7.4 shared game settings UI          CLOSED
-> S7.5 New Demon presentation           ACTIVE
-> S7.6 Werewolf presentation leftovers
-> STOP / remeasure Root / fresh S8 audit
```

Future A3 setup-snapshot/authority work remains separate. Do not resume it on the structural branch.
