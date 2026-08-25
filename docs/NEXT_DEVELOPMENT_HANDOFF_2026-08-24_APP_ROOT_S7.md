# CampBoardGameHost — App-root Decomposition Handoff (S7 closure / S8+)

> Refreshed: 2026-08-25 Australia/Sydney
> Stable main: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Latest closed structural code checkpoint: `7b52eab31fb7c0e78156c5f3487cb062de03edfe` (S8.1 GREEN)
> Current structural status: **S0–S8.1 CLOSED; S8.2 Clocktower app value models NEXT**
> Future A3 setup-snapshot/authority work remains out of scope here.
> Never merge, force-push, rebase, mark ready or broaden scope without explicit user authorization.

## 1. Current structural lineage

Stable integration base after PR #50:

```text
0311c3bb54ea71be69bc60a4aae642e0f39cd900
```

Recent stacked structural checkpoints:

```text
S7.3 GREEN
b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7

S7.4
b6d5d407...
  -> 4a46210cf11a0c45ceffef7bfaa647e53e99747f  RED
  -> 843f61e5106b704417b1cd347c47393264215bb8  stale structural-test repair
  -> bcd8b9b2dad4f3cde42bef2cdb6d1eea7839dd7e  GREEN
  -> 7c36d7d1fb95d9bae66439acd3523e86ab9b6e72  clean docs checkpoint

S7.5
  -> ea940199dd768aa6f35a418ab333c40401af658c  docs/preflight base
  -> 5ea3829631b33a13d4245b3932fc21662767a3a8  RED
  -> 91ce3da44ec4b824c4e3e750ec9f6c5b99b618a3  GREEN

S7.6
  -> 6e72afe4372a0ed7d329354a3bce018cb5799614  RED
  -> c2ab6dcba6e1d0fe862255160b782a8c6dab5fda  GREEN

S8.1
  -> 9b9d9c411a5d9c6c96cdf3315146338002123da7  RED
  -> c58d8bda8dc04144d039f828918f1edafd8bc983  stale structural-test repair
  -> 7b52eab31fb7c0e78156c5f3487cb062de03edfe  GREEN
```

The separate S7.4 documentation-recovery history is intentionally not part of the clean S7.5/S7.6 lineage.

## 2. Root-size progress

```text
Original   = 325,556 bytes
After S1   = 293,536 bytes
After S2   = 283,470 bytes
After S3   = 277,453 bytes
After S4   = 261,232 bytes
After S5   = 235,741 bytes
After S6   = 229,822 bytes
After S7.1 = 229,007 bytes
After S7.2 = 228,172 bytes
After S7.3 = 220,403 bytes
After S7.4 = 218,086 bytes
After S7.5 = 210,856 bytes
After S7.6 = 209,336 bytes
After S8.1 = 208,126 bytes
```

Current dedicated owners of note:

```text
ClocktowerPresentationTheme.kt       985 bytes
HostInteractionUi.kt               8,901 bytes
GameSettingsUi.kt                  3,112 bytes
clocktower/ui/ClocktowerNightScreen.kt 25,063 bytes
werewolf/WerewolfHostScreen.kt     31,193 bytes
AppGameModels.kt                    1,245 bytes
```

The 50 KiB target remains a maintainability guideline. Do not manufacture abstractions solely to reach a byte threshold.

## 3. S7 closeout

### S7.3 — shared host interaction UI — CLOSED

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

Owner: `HostInteractionUi.kt`. Remote exact-diff audit passed. No GitHub Actions run was observed on the branch head.

### S7.4 — shared game-settings presentation — CLOSED

Moved exactly:

```text
GameSettingsHeader
StepperRow
```

Owner: `GameSettingsUi.kt`.

The initial T1 exposed stale location assertions in `AppDealPresentationOwnershipTest` and `ClocktowerHostSelectionSemanticsOwnershipTest`. Those were repaired to protect their real S4/S7.2 boundaries instead of freezing temporary Root ownership.

Remote exact-diff audit passed. No GitHub Actions run was observed on the branch head.

### S7.5 — New Demon presentation — CLOSED

Moved exactly:

```text
ClocktowerNewDemonConfirmationScreen
CampBoardGameHostApp.kt
  -> clocktower/ui/ClocktowerNightScreen.kt
```

Remote audit found exactly three BASE->GREEN files: Root, NightScreen and the new ownership test. The production move was `-159/+159` lines. `ClocktowerHostScreen.kt` had identical blob SHA before and after the move, proving byte-for-byte preservation of:

- `pendingNewDemonName` ownership;
- `newDemon` lookup;
- `newDemonStep` construction;
- `playerDisplayStep` mutation;
- `onConfirmNewDemon` wiring;
- adjacent night transaction authority.

Root after S7.5: 210,856 bytes. NightScreen: 25,063 bytes. No GitHub Actions run was observed.

### S7.6 — Werewolf presentation leftovers — CLOSED

Moved exactly:

```text
WerewolfRoleLine
WerewolfPlayerStatusRow
CampBoardGameHostApp.kt
  -> werewolf/WerewolfHostScreen.kt
```

Tests-first lineage:

```text
RED   6e72afe4372a0ed7d329354a3bce018cb5799614
GREEN c2ab6dcba6e1d0fe862255160b782a8c6dab5fda
```

Remote audit found exactly three BASE->GREEN files: Root, WerewolfHostScreen and the new ownership test. Production move was `-36/+36` lines with unchanged signatures, visibility and bodies; no imports or consumers were changed. `AppDealPresentationOwnershipTest.kt` remained zero-diff.

Root after S7.6: 209,336 bytes. WerewolfHostScreen: 31,193 bytes. No GitHub Actions run was observed.

S7 is now complete. The mandatory STOP / remeasure / fresh S8 architecture audit has been performed.

## 4. Current Root ownership model after S7

### 4.1 Front — mixed responsibilities requiring selective S8 work

The front now contains:

```text
A. pure app-level value models
B. preferences / JSON / persistence / restore / archive code
C. legacy Clocktower role/setup/catalog/assignment code
```

These must not be treated as one extraction simply because they are adjacent.

### 4.2 Middle — protected application orchestrator

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
- Demon transition and public-history ordering;
- A4 cache/prewarm/rebuild lifetime.

### 4.3 Tail

`EmptyStateCard` remains deferred. Do not create a generic `Utils.kt`, `Common.kt` or `CommonUi.kt` bucket solely to relocate it.

## 5. S8.1 closeout and S8.2 implementation contract

### S8.1 — pure app game models — CLOSED

```text
BASE:   a8e17af78cda13ee6f504f24142d02f3dcfdacb3
RED:    9b9d9c411a5d9c6c96cdf3315146338002123da7
repair: c58d8bda8dc04144d039f828918f1edafd8bc983
GREEN:  7b52eab31fb7c0e78156c5f3487cb062de03edfe
root:   208,126 bytes
owner:  AppGameModels.kt = 1,245 bytes
remote exact-diff audit: PASS
GitHub Actions: none observed; do not claim remote CI green
```

S8.1 moved exactly `GameKind`, `Role`, `PlayerCard`, `EliminationRecord`, `GameOutcome`, `SavedGamePreview`, and `ArchivedGameReview`. It caused no production visibility widening.

### S8.2 — Clocktower app value models — NEXT

Remote branch:

```text
codex/source-decomposition-app-root-s8-2-clocktower-app-models
```

Expected base/head before implementation:

```text
7b52eab31fb7c0e78156c5f3487cb062de03edfe
```

Create `app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt` and move exactly these nine `internal` declarations from `CampBoardGameHostApp.kt`:

```text
ClocktowerEventType
ClocktowerEvent
ClocktowerTeam
ClocktowerPhase
ClocktowerDayMode
ClocktowerNightAction
ClocktowerDisplayKind
ClocktowerScript
ClocktowerRole
```

Defer prefs/JSON/persistence/restore, legacy role lists/catalog/helpers, distribution/assignment, and all state/effects/session/recommendation/A3/B4 logic. Do not change consumers or widen visibility.

## 6. S8.2 tests-first contract

Add `ClocktowerAppModelsOwnershipTest.kt` and run only its exact test for RED. It must prove the new owner exists, owns all nine declarations, Root no longer owns them, and the new owner contains no state/effect/JSON/prefs/session/loader/assignment/catalog strings. It must also prove `AppGameModels.kt` does not contain any of the nine declarations. Do not add byte-size assertions or UI/implementation-detail assertions.

Expected RED commit:

```text
test: define Clocktower app model ownership
```

The existing `AppGameModelsOwnershipTest.kt` has stale temporary Root-location assertions for three Clocktower declarations. Repair only those assertions so the Clocktower group checks `AppGameModels.kt` exclusion without requiring Root ownership, then commit:

```text
test: decouple shared models from Clocktower model location
```

The repair must leave its actual app-model ownership assertions unchanged. The new Clocktower ownership test must remain RED after the repair.

GREEN is a mechanical move only. Allowed code/test files are exactly:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt
app/src/test/java/com/codex/campboardgamehost/ClocktowerAppModelsOwnershipTest.kt
app/src/test/java/com/codex/campboardgamehost/AppGameModelsOwnershipTest.kt
```

Run the specified T0 ownership/characterization tests, then T1 `:app:testFast`, then T2 `:app:assembleDebug`. Do not run `testFull`, ASP, Clingo, benchmarks, or unrelated suites. Commit `refactor: extract Clocktower app models`, push the named branch, and stop.

## 7. Large-file execution workflow — HARD RULE

`CampBoardGameHostApp.kt` is 209,336 bytes after S7.6. Do **not** use the GitHub Connector for production rewrites/moves in this file.

### ChatGPT owns

- live-state audit;
- architecture/ownership decision;
- production/test symbol inventory;
- dependency/visibility audit;
- stale structural-test classification;
- RED design;
- changed-file allowlist;
- validation commands;
- exact-diff acceptance criteria;
- STOP conditions;
- post-push remote audit.

### Luna owns in the user's full local Git worktree

- writing/running the approved RED;
- mechanical Root declaration movement;
- minimal compile-required import changes;
- T0/T1/T2 execution;
- `git diff --check`;
- exact local diff review;
- commit and push after approved gates pass.

## 8. Global protected boundaries

Do not weaken or relocate without dedicated design/tests:

- Clocktower live transaction ordering;
- Compose effect lifetime;
- persistence / restore / archive ownership;
- A4 lifecycle/cache/prewarm/rebuild;
- `ClocktowerGameSession` global timeline identity/sequence;
- action/observation commit ordering;
- planner/projector/materializer authority;
- merged A3/B4 historical-exact semantics;
- Host recommendation/registration transaction semantics.

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze.

## 9. Current planned route

```text
S7.1–S7.6                         CLOSED
S8.1 pure app game models         CLOSED
-> S8.2 Clocktower app models     NEXT
-> remeasure + fresh audit
-> possible Clocktower legacy model/catalog slice only if natural
-> persistence/codec S9 only if independently justified
-> D1 ClocktowerDayScreen stale-import cleanup + remeasure
-> D2 split DayScreen only if still architecturally justified
-> structural exit audit
```

Future A3 setup-snapshot/authority work remains separate from this structural branch.
