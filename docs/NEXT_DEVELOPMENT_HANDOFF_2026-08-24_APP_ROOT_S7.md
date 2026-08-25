# CampBoardGameHost — App-root Decomposition Handoff (S7 closure / S8+)

> Refreshed: 2026-08-25 Australia/Sydney
> Stable main: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Latest closed structural code checkpoint: `c2ab6dcba6e1d0fe862255160b782a8c6dab5fda` (S7.6 GREEN)
> Current structural status: **S0–S7.6 CLOSED; STOP/remeasure completed; S8.1 pure app game models NEXT**
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
```

Current dedicated owners of note:

```text
ClocktowerPresentationTheme.kt       985 bytes
HostInteractionUi.kt               8,901 bytes
GameSettingsUi.kt                  3,112 bytes
clocktower/ui/ClocktowerNightScreen.kt 25,063 bytes
werewolf/WerewolfHostScreen.kt     31,193 bytes
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

## 5. Fresh S8 audit result

### S8.1 — pure app game models — NEXT

Preferred new owner:

```text
app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt
```

Move exactly these seven already-`internal` declarations:

```text
GameKind
Role
PlayerCard
EliminationRecord
GameOutcome
SavedGamePreview
ArchivedGameReview
```

These are cross-game app-level value types, have no state/effects, and can remain in the same Kotlin package with unchanged visibility.

Explicitly keep in Root for S8.1:

```text
private enum class Screen
internal enum class LanguageMode
internal fun PlayerCard.abilitySubject(...)
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

Why `PlayerCard.abilitySubject` stays out: it is a rules adapter to `clocktower.rules.AbilitySubject`, not a value-model declaration.

Why `LanguageMode` stays out: it is settings/preferences-facing (`prefsValue`) rather than a game value model.

Why Clocktower-specific types stay out: they deserve a separate domain-specific audit instead of turning `AppGameModels.kt` into a mixed bucket.

### Persistence / JSON — DEFER / CONDITIONAL S9

Do not include in S8.1:

- prefs keys or SharedPreferences access;
- active-game save/load/clear;
- saved-game preview restore behavior;
- game archive/history persistence;
- nullable JSON helpers/codecs;
- `PlayerCard`/event/outcome JSON codecs;
- epistemic observation JSON serialization;
- saved-game version/schema handling.

These touch restore compatibility and history/schema semantics.

### Legacy Clocktower role/setup/catalog — AUDIT LATER

Root still owns legacy role lists, script mapping, player-count distribution and random assignment helpers. Although cohesive, several are `private`; moving them would widen visibility and must be reconciled with the newer `clocktower/catalog` architecture. Do not combine this work with S8.1.

## 6. S8.1 tests-first contract

Add a focused ownership RED that proves:

1. `AppGameModels.kt` exists.
2. It declares exactly the approved app game model symbols needed for this slice.
3. Root no longer declares those seven symbols.
4. Root still declares:
   - `private enum class Screen`
   - `internal enum class LanguageMode`
   - `internal fun PlayerCard.abilitySubject(`
   - `internal enum class ClocktowerEventType`
   - `internal enum class ClocktowerTeam`
5. The new owner contains none of:
   - `remember`
   - `mutableStateOf`
   - `LaunchedEffect`
   - `DisposableEffect`
   - `SideEffect`
   - `JSONObject`
   - `JSONArray`
   - `SharedPreferences`
   - `ClocktowerGameSession`
6. Do not add byte-size assertions.
7. Do not modify consumers simply because declarations move within the same package.

Expected code/test allowlist:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt
app/src/test/java/com/codex/campboardgamehost/AppGameModelsOwnershipTest.kt
```

No fourth code/test file unless T1 exposes a genuinely stale structural location assertion; if so, STOP and report before editing it.

Validation:

```text
T0 AppGameModelsOwnershipTest RED/GREEN
-> T1 :app:testFast
-> T2 :app:assembleDebug
-> no T3 expected
-> applicable PR T4 later
-> git diff --check + exact declaration-only diff audit
```

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
-> STOP / remeasure / S8 audit    COMPLETE
-> S8.1 pure app game models      NEXT
-> remeasure + fresh audit
-> possible Clocktower legacy model/catalog slice only if natural
-> persistence/codec S9 only if independently justified
-> D1 ClocktowerDayScreen stale-import cleanup + remeasure
-> D2 split DayScreen only if still architecturally justified
-> structural exit audit
```

Future A3 setup-snapshot/authority work remains separate from this structural branch.
