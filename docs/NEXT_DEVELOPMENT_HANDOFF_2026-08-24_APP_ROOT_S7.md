# CampBoardGameHost — App-root Decomposition Handoff (S7+)

> Refreshed: 2026-08-24 Australia/Sydney  
> Structural branch: `codex/source-decomposition-app-root-s7-main65c0ba`  
> Structural base: `main@65c0ba47a6f2c0b76263b3f3a1fbf2e26a427024`  
> Current structural status: **S0–S6 CLOSED; S7 re-audited and ready for tests-first implementation**  
> PR #48 A3 historical exact/B4 shadow checkpoint is merged into `main`; future A3 setup-snapshot/authority work remains out of scope here.  
> Never merge, force-push, rebase, or broaden scope without explicit user authorization.

## 1. Why this branch exists

The old structural branches were based on the pre-A3 `main` checkpoint and are no longer appropriate production continuation points.

The current branch starts from the post-PR #48 `main` merge:

```text
65c0ba47a6f2c0b76263b3f3a1fbf2e26a427024
```

This preserves the completed A3 historical exact/B4 shadow checkpoint while keeping future A3 setup-snapshot/authority promotion out of the structural workstream.

Do **not** restore the unfinished S7 RED that was removed by PR #47.

## 2. Current app-root progress

Original Root at the start of the decomposition pass:

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

The 50 KiB target is a **maintainability guideline, not a hard gate**.

Preferred interpretation:

```text
ordinary feature/model/semantics/presentation file:
    usually < 50 KiB

> 50 KiB:
    should have a clear ownership explanation

> 100 KiB:
    requires an architecture audit before accepting further growth
```

The objective is not byte minimization by itself. The objective is to prevent normal feature work from defaulting to a 200–300 KiB monolith.

## 3. Completed slices S0–S6

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

The validated S0–S6 checkpoint before PR #48 was:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

PR #48 was then merged on top without touching the App-root S1–S6 production extraction surfaces.

## 4. Fresh architecture audit

Current large Android handwritten production files of interest:

```text
ClocktowerHostScreen.kt    ~295,644 bytes
CampBoardGameHostApp.kt    ~229,822 bytes
ClocktowerDayScreen.kt      ~63,135 bytes
ClocktowerNightStepUi.kt    ~45,251 bytes
ClocktowerHistoryScreen.kt  ~38,365 bytes
WerewolfHostScreen.kt       ~29,673 bytes
```

### Key conclusion

`CampBoardGameHostApp.kt` remains the highest-value structural target.

`ClocktowerHostScreen.kt` is **not** a byte-target campaign. A1–A13 already extracted its strongest natural seams. The remaining file is largely a state/effect/transaction orchestrator and remains under new-responsibility growth freeze.

`ClocktowerDayScreen.kt` is only moderately above the guideline and appears to retain a stale import surface from earlier extraction. Clean/re-measure it after the Root pass before deciding whether another split is justified.

## 5. Current Root structure after S1–S6

The remaining `CampBoardGameHostApp.kt` has three broad zones.

### 5.1 Front — models / prefs / JSON / setup helpers

Contains candidates such as:

- app-level models and enums;
- preference/schema constants;
- active-game JSON encode/decode support;
- Clocktower setup/card helpers;
- runtime ruleset helpers;
- pure label/adapter helpers.

These are possible S8 candidates, but persistence/restore contracts make this section riskier than the remaining presentation seams.

### 5.2 Middle — real app orchestrator

Protected ownership includes:

- `remember` state;
- `LaunchedEffect` / `DisposableEffect` / `SideEffect` lifetimes;
- navigation state;
- active-game persistence and restore timing;
- `ClocktowerGameSession` ownership;
- action/observation/history ordering;
- revision state;
- async recommendation lifetime;
- cross-game reset behavior;
- day/night transaction callbacks;
- Demon transition/public-history ordering.

Do **not** move these merely to reduce byte count.

### 5.3 Tail — declarations with better owners

Current low-risk candidates include:

```text
ClocktowerDarkTheme
ClocktowerNewDemonConfirmationScreen

GameSettingsHeader
EmptyStateCard
StepperRow
HostProgressCard
HostScriptCard
HostInstructionBlock
HostActionSection

SelectablePlayerChips
SelectableTwoPlayerChips
SelectableSeatNumbers

TwoPlayerSelectionAction
twoPlayerSelectionAction
shouldAutoAdvanceRedHerring

WerewolfRoleLine
WerewolfPlayerStatusRow

PlayerCard host/seat display helpers and similar pure labels
```

Several are already `internal` because extracted screens call them cross-file. That is evidence that Root is no longer their natural owner.

## 6. S7 plan — shared presentation / selection ownership cleanup

Implement S7 as small tests-first slices, not one large move.

### S7.1 — Clocktower shared theme

Move ownership of:

```kotlin
ClocktowerDarkTheme
```

from `CampBoardGameHostApp.kt` to a dedicated owner such as:

```text
ClocktowerPresentationTheme.kt
```

Requirements:

- declaration body unchanged except visibility only if required;
- existing consumers unchanged semantically;
- Root no longer owns the declaration;
- no state/effect/lifecycle movement.

This is the preferred first slice because it should have a very small dependency surface.

### S7.2 — Clocktower selection semantics

Move:

```kotlin
TwoPlayerSelectionAction
twoPlayerSelectionAction(...)
shouldAutoAdvanceRedHerring(...)
```

into:

```text
ClocktowerHostSelectionSemantics.kt
```

Use existing selection characterization/tests. Do not alter selection behavior.

### S7.3 — Clocktower selection UI

Move:

```kotlin
SelectablePlayerChips
SelectableTwoPlayerChips
SelectableSeatNumbers
```

into a dedicated reusable owner such as:

```text
ClocktowerSelectionUi.kt
```

Do not create a generic `Utils.kt` bucket.

### S7.4 — New Demon presentation

Move:

```kotlin
ClocktowerNewDemonConfirmationScreen
```

out of Root.

Preferred owner after exact dependency audit:

```text
clocktower/ui/ClocktowerNightScreen.kt
```

### S7.5 — Werewolf presentation leftovers

Move after exact usage/visibility audit:

```kotlin
WerewolfRoleLine
WerewolfPlayerStatusRow
```

preferably into:

```text
werewolf/WerewolfHostScreen.kt
```

### S7.6 — generic app host presentation primitives

Audit:

```text
GameSettingsHeader
EmptyStateCard
StepperRow
HostProgressCard
HostScriptCard
HostInstructionBlock
HostActionSection
```

If genuinely shared, use a narrow owner such as:

```text
AppHostPresentationPrimitives.kt
```

If usage proves a single feature owner, move them there instead.

## 7. S8 — pure models / setup / catalog helpers

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

Map actual call sites and choose domain-specific owners before moving anything.

## 8. S9 — persistence codec/schema extraction, conditional only

Persistence is higher risk because it touches:

- active-game restore;
- persisted game/ruleset identity;
- Clocktower semantic history;
- action timeline;
- observation timeline;
- GLOBAL sequence identity;
- old saved-game compatibility.

If S9 is still justified after S7/S8, only extract **pure codec/schema/serialization** responsibilities first.

Root should continue to own restore timing and Compose lifecycle/effect orchestration unless an independently tested coordinator boundary already exists.

Required approach:

```text
characterization RED
-> pure codec/schema extraction
-> preserve old JSON compatibility
-> preserve restore ordering/lifetime
-> full persistence + production wiring regression suite
```

Stop if extraction requires broad lifecycle movement merely to reduce file size.

## 9. ClocktowerDayScreen follow-up

### D1 — unused-import cleanup + remeasure

Before splitting `ClocktowerDayScreen.kt`, clean stale imports left by earlier mechanical extraction and remeasure.

### D2 — split only if still justified

Natural screen seams include:

```text
ClocktowerDayOverviewScreen
ClocktowerDawnSummaryScreen
ClocktowerNominationScreen
ClocktowerVoteScreen
ClocktowerExecutionConfirmScreen
ClocktowerSpecialDayActionScreen
```

A likely cluster is nomination/vote/execution confirmation, but choose only after a fresh dependency audit.

## 10. ClocktowerHostScreen policy

`ClocktowerHostScreen.kt` remains under **new-responsibility growth freeze**.

Do not set a target such as:

```text
295 KiB -> 50 KiB
```

Further extraction is allowed only when a cohesive owner is obvious and behavior can be characterized without weakening:

- Compose state/effect lifetime;
- callback/audit/commit ordering;
- recommendation/session ownership;
- persistence/history identity;
- planner/projector/materializer authority.

## 11. Protected boundaries for S7+

Do not move merely for byte reduction:

- Clocktower live transaction ordering;
- `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberUpdatedState` lifetime;
- persistence / restore / archive ownership;
- A4 cache/prewarm/runtime lifecycle;
- `ClocktowerGameSession` global timeline identity/sequence;
- action/observation commit ordering;
- planner/projector/materializer authority;
- merged A3/B4 historical-exact semantics;
- Host recommendation/registration transaction semantics.

Future A3 setup-snapshot/authority work is out of scope on this branch.

## 12. Large-file execution workflow — HARD RULE

`CampBoardGameHostApp.kt` is approximately 230 KiB. Do **not** use the GitHub Connector for production rewrites/moves in this file.

For any S7/S8 slice touching Root:

### ChatGPT owns

- live-state audit;
- architecture/ownership decision;
- dependency and visibility audit;
- tests-first RED design;
- exact declaration move specification;
- allowed/forbidden changed files;
- validation commands;
- exact-diff acceptance criteria;
- STOP conditions;
- post-push remote audit.

### Codex Luna in the user's full local Git worktree owns

- writing/running the RED;
- mechanical declaration move;
- minimal imports/visibility edits;
- focused and full local tests;
- assemble;
- `git diff --check`;
- commit and push.

ChatGPT must not directly edit Root production code through the connector for these slices.

## 13. Tests-first contract for every structural slice

For each S7/S8/D2 extraction:

1. audit exact current owner and every call site;
2. design a focused ownership/characterization RED;
3. confirm RED fails for the intended ownership reason only;
4. perform the smallest behavior-preserving move;
5. minimize visibility widening;
6. run focused test;
7. run full `:app:testDebugUnitTest`;
8. run `:app:assembleDebug`;
9. run `git diff --check`;
10. perform exact diff audit against the pre-move baseline;
11. stop on any unexpected dependency/visibility/lifecycle requirement.

## 14. Exact next task — S7.1

Start with `ClocktowerDarkTheme` only.

Before GREEN, establish a focused ownership RED proving at minimum:

```text
CampBoardGameHostApp.kt no longer owns ClocktowerDarkTheme
ClocktowerPresentationTheme.kt is the sole declaration owner
existing consumers continue to reference the same symbol
no Root state/effect/persistence/transaction code moves
```

Expected production allowlist:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationTheme.kt   # new
```

plus one focused ownership test.

If compilation requires unrelated production changes, stop and report rather than expanding the slice.

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
S7.1 shared Clocktower theme
-> S7.2 selection semantics
-> S7.3 selection UI
-> S7.4 New Demon presentation
-> S7.5 Werewolf presentation leftovers
-> S7.6 generic host presentation primitives
-> remeasure Root
-> S8 pure models/setup/catalog helpers where natural
-> remeasure and decide whether S9 is justified
-> D1 DayScreen import cleanup + remeasure
-> D2 DayScreen split only if still justified
-> structural exit audit
-> docs reconciliation from then-current main
```

Do not resume future A3 product implementation on this branch.
