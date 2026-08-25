# CampBoardGameHost — App-root Decomposition Handoff (S7+)

> Refreshed: 2026-08-25 Australia/Sydney  
> Structural branch: `codex/source-decomposition-app-root-s7-4-game-settings-ui`
> Structural base: `main@0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Current structural status: **S0–S7.3 CLOSED; S7.4 shared game-settings presentation COMPLETE on the current local slice**
> PR #48 A3 historical exact/B4 shadow checkpoint is merged into `main`; future A3 setup-snapshot/authority work remains out of scope here.  
> Never merge, force-push, rebase, mark ready, or broaden scope without explicit user authorization.

## 1. Current branch lineage

The current lineage starts from stable main:

```text
0311c3bb54ea71be69bc60a4aae642e0f39cd900
```

The S7 planning checkpoint was:

```text
9d5499a278135e9ab642f76a565b52c2a74cd289
```

S7.1 then established a real RED/GREEN history:

```text
9d5499a278135e9ab642f76a565b52c2a74cd289
  -> 78b4a1dce9e4990d348354d900c9339f3047db95  test: redefine Clocktower theme ownership
  -> 4062a65355fedad5e13b5e874657db46cd0368ad  refactor: extract Clocktower presentation theme
```

S7.2 then established the selection-semantics ownership contract and behavior-preserving move:

```text
eb158077ce382526330071d8d44fab67817a472d
  -> 7829291c90af7d09b6b444eb561d34c935437319  test: define S7.2 selection semantics ownership
  -> eda75cd3bd7cb4177e13d1792fde17ca229aa644  refactor: move Clocktower selection semantics
  -> 5ca925ae38e69376d768bb73c1a2f0fe6e06fab5  test: make S7.2 ownership contract portable
```

The S7.2 remote exact-diff audit passed. S7.3 and S7.4 then continued as stacked structural slices after the test-workflow optimization merge:

```text
b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
  -> 4a46210c  test: define shared game settings UI ownership
  -> 843f61e5  test: retire stale root ownership assertions
  -> bcd8b9b2  refactor: extract shared game settings UI
```

S7.3 owner: `HostInteractionUi.kt`, with exactly `HostProgressCard`, `HostScriptCard`, `HostInstructionBlock`, `HostActionSection`, `SelectablePlayerChips`, `SelectableTwoPlayerChips`, and `SelectableSeatNumbers`. S7.3 Root size was 220,403 bytes and the new owner was 8,901 bytes; remote exact-diff audit passed and no GitHub workflow run was observed on that head.

S7.4 owner: `GameSettingsUi.kt`, with exactly `GameSettingsHeader` and `StepperRow`. The S7.4 focused tests, `:app:testFast`, `:app:assembleDebug`, and exact diff audit passed. No GitHub workflow run was observed on the S7.4 head.

## 2. Current app-root progress

Original Root at the start of the decomposition pass:

```text
CampBoardGameHostApp.kt = 325,556 bytes
```

Validated structural sizes:

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
After S7.4 = 218,086 bytes
```

S7.1 new owner:

```text
ClocktowerPresentationTheme.kt = 985 bytes
```

S7.2 moved the pure selection semantics into the existing owner:

```text
ClocktowerHostSelectionSemantics.kt
```

S7.3 moved shared host interaction presentation into:

```text
HostInteractionUi.kt = 8,901 bytes
```

S7.4 moved shared game-settings presentation into:

```text
GameSettingsUi.kt = 3,112 bytes
```

Net Root reduction through S7.4:

```text
107,470 bytes
~33.0% from the original Root
```

The 50 KiB target remains a **maintainability guideline, not a hard gate**.

Preferred interpretation:

```text
ordinary feature/model/semantics/presentation file:
    usually < 50 KiB

> 50 KiB:
    should have a clear ownership explanation

> 100 KiB:
    requires an architecture audit before accepting further growth
```

The objective is to prevent ordinary feature work from defaulting to a 200–300 KiB monolith, not to manufacture abstractions solely to satisfy a byte threshold.

## 3. Completed slices S0–S7.2

### S0 — dynamic-flow preservation guard — CLOSED

Protected planner/projector/materializer authority and stable interaction identities before App-root movement.

### S1 — player setup presentation — CLOSED

Owner:

```text
AppPlayerSetupScreens.kt = 35,163 bytes
```

Root retained player state, mutation, routing, restore/start ownership.

### S2 — settings presentation — CLOSED

Owner:

```text
AppSettingsScreen.kt = 11,456 bytes
```

Root retained settings state and preference writes.

### S3 — Clocktower landing presentation — CLOSED

Owner:

```text
AppClocktowerLandingScreen.kt = 7,522 bytes
```

Root retained navigation, restore and persistence ownership.

### S4 — deal / reveal presentation — CLOSED

Owner:

```text
AppDealScreens.kt = 17,749 bytes
```

Root retained `currentDealIndex`, navigation mutation, persistence and A4 identity-reveal effect lifetime.

### S5 — results / host tools / history presentation — CLOSED

Owner:

```text
AppGameReviewScreens.kt = 27,332 bytes
```

Root retained game-history state, archive/restart transactions, outcome evaluation and routing.

### S6 — legacy generic GameScreen presentation — CLOSED

Owner:

```text
AppGameScreen.kt = 7,285 bytes
```

Root retained the generic game mutation transaction and `evaluateGameOutcome`.

### S7.1 — shared Clocktower theme — CLOSED

Ownership moved:

```text
ClocktowerDarkTheme

CampBoardGameHostApp.kt
  -> ClocktowerPresentationTheme.kt
```

RED:

```text
78b4a1dce9e4990d348354d900c9339f3047db95
```

GREEN:

```text
4062a65355fedad5e13b5e874657db46cd0368ad
```

Remote exact-diff audit from the S7.1 planning checkpoint found exactly four changed files:

```text
app/src/test/java/com/codex/campboardgamehost/AppDealPresentationOwnershipTest.kt
app/src/test/java/com/codex/campboardgamehost/AppClocktowerPresentationThemeOwnershipTest.kt
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationTheme.kt
```

The RED correctly migrated two stale S4 file-location assertions: the preserved invariant is that `AppDealScreens.kt` does not own the shared Clocktower theme; S7.1 replaces the historical temporary fact that Root owned it with a dedicated unique-owner contract.

The GREEN production diff is declaration-only:

- Root deletes only `ClocktowerDarkTheme`;
- the new owner contains the same `@Composable internal` signature and the same theme body/color values;
- existing consumers are unchanged;
- no visibility widening;
- no state, effect, lifecycle, session, persistence, A3/B4, Host transaction, or recommendation semantics moved.

The branch push for `4062a653...` did **not** trigger a GitHub Actions run or combined status. Therefore do not describe that head as remotely CI-green. The remote closeout proves lineage, ownership and exact diff; GitHub CI/R2 remains an independent merge gate when a PR/head is later prepared for merge.

### S7.2 — Clocktower selection semantics — CLOSED

Ownership moved:

```text
TwoPlayerSelectionAction
twoPlayerSelectionAction(...)
shouldAutoAdvanceRedHerring(...)

CampBoardGameHostApp.kt
  -> ClocktowerHostSelectionSemantics.kt
```

Provenance:

```text
RED:
7829291c90af7d09b6b444eb561d34c935437319

GREEN:
eda75cd3bd7cb4177e13d1792fde17ca229aa644

portable ownership-test repair:
5ca925ae38e69376d768bb73c1a2f0fe6e06fab5
```

Remote exact-diff audit passed:

- RED added only `ClocktowerHostSelectionSemanticsOwnershipTest.kt`;
- GREEN deleted exactly the 27-line semantic block from Root and added the same block to `ClocktowerHostSelectionSemantics.kt`;
- `ClocktowerHostScreen.kt` remained zero-diff;
- `SelectablePlayerChips`, `SelectableTwoPlayerChips`, and `SelectableSeatNumbers` remained Root-owned for future S7.3;
- no visibility widening, behavioral change, Compose lifetime change, persistence/session/history change, or A3/B4/A4 authority change occurred;
- the follow-up repair changed only the ownership test, replacing a machine-specific absolute path with the repository-relative source path and using `CampBoardGameHostApp.kt` directly.

Branch pushes through the S7.2 closeout still did **not** produce GitHub Actions workflow runs/statuses. Local focused/full unit tests, assemble and diff-check are execution evidence only; GitHub CI/R2 remains the independent checkpoint-PR merge gate.

## 4. Current large-file audit after S7.2

Current handwritten Android production files of interest include:

```text
ClocktowerHostScreen.kt     295,644 bytes
CampBoardGameHostApp.kt     228,172 bytes
ClocktowerDayScreen.kt       63,135 bytes
ClocktowerNightStepUi.kt     45,251 bytes
ClocktowerHistoryScreen.kt   38,365 bytes
WerewolfHostScreen.kt        29,673 bytes
```

`CampBoardGameHostApp.kt` remains the future structural target because it still contains declarations with clearer semantic/presentation owners.

`ClocktowerHostScreen.kt` is **not** a byte-target campaign. A1–A13 already extracted its strongest natural seams; it remains under new-responsibility growth freeze.

`ClocktowerDayScreen.kt` should receive only stale-import cleanup/re-measure after the Root pass before any further split is considered.

## 5. Current Root ownership model

The remaining `CampBoardGameHostApp.kt` has three broad zones.

### 5.1 Front — models / prefs / JSON / setup helpers

Potential later S8 candidates include app-level models/enums, pure setup/card helpers, runtime-ruleset adapters and pure labels. Persistence/restore contracts make this area riskier than the remaining S7 seams.

### 5.2 Middle — protected real app orchestrator

Do not move merely for size:

- `remember` state;
- `LaunchedEffect` / `DisposableEffect` / `SideEffect` / `rememberUpdatedState` lifetimes;
- navigation state;
- active-game persistence and restore timing;
- `ClocktowerGameSession` ownership;
- action/observation/history ordering;
- revision state;
- async recommendation lifetime;
- cross-game reset behavior;
- day/night transaction callbacks;
- Demon transition/public-history ordering.

### 5.3 Tail — declarations with better owners

Remaining S7 candidates after S7.4 include:

```text
ClocktowerNewDemonConfirmationScreen

WerewolfRoleLine
WerewolfPlayerStatusRow

EmptyStateCard
```

Several are already `internal` because extracted files call them cross-file. That is evidence that Root is no longer their natural owner.

## 6. S7.3/S7.4 structural closeout

S7.3 and S7.4 are closed on the stacked local branch. The exact production moves were behavior-preserving and did not change visibility, Compose lifetime, persistence/session/history authority, transaction callbacks, recommendation logic, or A3/B4 semantics.

S7.4 stale structural-contract migration changed only:

```text
AppDealPresentationOwnershipTest.kt
ClocktowerHostSelectionSemanticsOwnershipTest.kt
```

Those tests now protect their actual S4/S7.2 boundaries rather than historical temporary Root ownership. `GameSettingsUiOwnershipTest.kt` independently protects the S7.4 owner contract.

## 7. Remaining S7 route

### S7.3 — shared host interaction UI — CLOSED

Owner:

```text
HostInteractionUi.kt
```

### S7.4 — shared game-settings presentation — CLOSED

Owner:

```text
GameSettingsUi.kt
```

Declarations:

```text
GameSettingsHeader
StepperRow
```

### S7.5 — New Demon presentation

Move only after exact dependency/transaction guard:

```kotlin
ClocktowerNewDemonConfirmationScreen
```

Preferred owner candidate:

```text
clocktower/ui/ClocktowerNightScreen.kt
```

### S7.6 — Werewolf presentation leftovers

Audit:

```kotlin
WerewolfRoleLine
WerewolfPlayerStatusRow
```

Preferred owner candidate:

```text
werewolf/WerewolfHostScreen.kt
```

### Deferred generic app presentation

`EmptyStateCard` remains deferred. Do not create a generic `Utils.kt` or `CommonUi.kt` bucket merely to move it.

The completed S7.3 owner contains:

```text
HostProgressCard
HostScriptCard
HostInstructionBlock
HostActionSection
SelectablePlayerChips
SelectableTwoPlayerChips
SelectableSeatNumbers
```

## 8. S8 — pure models / setup / catalog helpers

After S7, remeasure Root and audit its front section.

Potential candidates include:

```text
RulesetRef.matchesRuleset(...)
buildClocktowerRuntimeRulesetRef(...)
adjustRoleCount(...)
buildClocktowerPlayerCards(...)
buildPlayerKnowledgeSnapshots(...)
pure display/seat label adapters
```

Map actual call sites and select domain-specific owners before moving anything.

## 9. S9 — persistence codec/schema extraction, conditional only

Persistence is higher risk because it touches active-game restore, game/ruleset identity, semantic history, action/observation timelines, GLOBAL sequence identity and saved-game compatibility.

If S9 is still justified after S7/S8, extract only pure codec/schema/serialization responsibilities first. Root should continue to own restore timing and Compose lifecycle/effect orchestration unless an independently tested coordinator boundary already exists.

Stop S9 if the only way to reduce Root is broad lifecycle movement.

## 10. ClocktowerDayScreen follow-up

### D1 — unused-import cleanup + remeasure

Clean stale imports after the Root pass and remeasure.

### D2 — split only if still justified

Potential natural screen seams include:

```text
ClocktowerDayOverviewScreen
ClocktowerDawnSummaryScreen
ClocktowerNominationScreen
ClocktowerVoteScreen
ClocktowerExecutionConfirmScreen
ClocktowerSpecialDayActionScreen
```

Do not split solely because the file once measured ~63 KiB.

## 11. ClocktowerHostScreen policy

`ClocktowerHostScreen.kt` remains under **new-responsibility growth freeze**.

Further extraction is allowed only when a cohesive owner is obvious and behavior can be characterized without weakening:

- Compose state/effect lifetime;
- callback/audit/commit ordering;
- recommendation/session ownership;
- persistence/history identity;
- planner/projector/materializer authority.

## 12. Protected boundaries for S7+

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

Future A3 setup-snapshot/authority work remains out of scope on this branch.

## 13. Large-file execution workflow — HARD RULE

`CampBoardGameHostApp.kt` remains approximately 228 KiB. Do **not** use the GitHub Connector for production rewrites/moves in this file.

For any future S7/S8 slice touching Root:

### ChatGPT owns

- live-state audit;
- architecture/ownership decision;
- complete production + test symbol inventory;
- dependency and visibility audit;
- stale structural-contract classification;
- tests-first RED design;
- exact declaration move specification;
- allowed/forbidden changed files;
- validation commands;
- exact-diff acceptance criteria;
- STOP conditions;
- post-push remote audit.

### Codex Luna in the user's full local Git worktree owns

- writing/running the approved RED;
- mechanical declaration move;
- minimal compile-required imports/visibility edits;
- focused T0 tests, T1 `:app:testFast`, and affected T2 checks;
- assemble;
- `git diff --check`;
- exact local diff review;
- commit and push when the approved gates pass.

ChatGPT must not directly edit Root production code through the connector for these slices.

## 14. Tests-first contract for every structural slice

For each future S7/S8/D2 extraction:

1. re-check live `main`, structural branch head and parent chain;
2. inventory every production declaration/caller for all target symbols;
3. inventory every test reference/location assertion for all target symbols;
4. classify stale structural boundary tests before RED design;
5. design a focused ownership/characterization RED;
6. confirm RED fails for the intended ownership reason only;
7. perform the smallest behavior-preserving move;
8. minimize visibility widening;
9. run focused T0 ownership/characterization RED/GREEN;
10. run T1 `:app:testFast`;
11. run T2 affected regression/compile checks, including `:app:assembleDebug` when production Compose declarations move;
12. trigger T3 only when the semantic area requires it;
13. run the applicable PR T4 FULL gate in CI;
14. run `git diff --check` and perform exact diff audit against the pre-move baseline;
15. stop on any unexpected dependency/visibility/lifecycle requirement.

Structural refactor workflow is therefore:

```text
T0 focused ownership/characterization RED/GREEN
-> T1 :app:testFast
-> T2 affected regression/compile checks
-> triggered T3 only when semantic area requires
-> PR T4 applicable FULL
```

If impact is uncertain, escalate validation upward. Do not weaken the PR T4 gate.

A pushed local commit is not merge authorization. GitHub CI/R2 remains an independent merge gate before eventual integration.

## 15. Overall stop condition

After S7 and S8, remeasure and re-audit.

Stop the Root decomposition campaign when the remaining large Root content is primarily:

- app-scoped state;
- Compose effect lifetime;
- navigation/orchestration;
- persistence/restore timing;
- game transactions;
- cross-feature session coordination.

A remaining Root above 50 KiB is acceptable when those responsibilities are coherent and further movement would create artificial parameter plumbing or weaken lifecycle/transaction ownership.

## 16. Planned route

```text
S7.1 shared Clocktower theme — CLOSED
-> S7.2 selection semantics — CLOSED
-> S7.3 shared host interaction UI — CLOSED
-> S7.4 shared game settings UI — CLOSED
-> S7.5 New Demon presentation
-> S7.6 Werewolf presentation leftovers
-> STOP / remeasure Root / fresh S8 audit
-> S8 pure models/setup/catalog helpers where natural
-> remeasure and decide whether S9 is justified
-> D1 DayScreen import cleanup + remeasure
-> D2 DayScreen split only if still justified
-> structural exit audit
-> docs reconciliation from then-current main
```

Do not resume future A3 product implementation on this branch.
