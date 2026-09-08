# D6.2 UI Composition Boundary Audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: accepted D6.1 tree `dfe4ad99f331361697ea8976c7d72fb529106d54`
> Status: **ROUTE SELECTED — D6.2a CHARACTERIZATION NEXT — PRODUCTION UNCHANGED**

## Purpose

D6.1 solved canonical Clocktower session and dynamic GameState writer ownership. This audit asks what remaining decomposition work now yields the highest architectural value without reopening accepted persistence/rules/session semantics.

The answer is no longer “move more state into `ClocktowerGameSession`.” The largest remaining problem is the Compose/UI composition surface around `ClocktowerJudgeScreen`.

## Residual audit evidence

Read-only residual-root audit:

```text
34223904849 — PASS
```

The audit workflow self-cleaned. Its cleanup returned the branch to exactly the accepted D6.1 tree.

### File-size signal

```text
ClocktowerHostScreen.kt     329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt     241,986 bytes / 4,315 lines
ClocktowerDayScreen.kt       50,927 bytes
ClocktowerNightStepUi.kt     47,970 bytes
ClocktowerHistoryScreen.kt   38,365 bytes
ClocktowerNightScreen.kt     25,063 bytes
ClocktowerGameSession.kt     22,708 bytes
```

File size is not itself the acceptance criterion, but it shows that the dominant residual monolith has shifted from canonical session ownership to UI composition.

### JudgeScreen boundary signal

`ClocktowerJudgeScreen` currently exposes:

```text
function parameters       103
callback parameters        39
App invocation span   ~97,718 characters
```

The raw App invocation contains nested named arguments, so the audit's 470 named-argument count is not a semantic parameter count; the function definition's 103 parameters is the reliable boundary metric.

The callbacks span unrelated concerns including:

- recommendation demand/application;
- phase/night-step movement;
- semantic event/observation recording;
- demon attack and execution;
- poison;
- Fortune Teller / Chambermaid / Ravenkeeper / Red Herring;
- Butler / Monk / Mayor redirect;
- demon succession/new Demon;
- Klutz / Artist / Slayer / Virgin;
- day/night confirmation and result display.

This is composition fan-out, not one cohesive UI contract.

### App-root residual state

The audit found 41 `clocktower*` root variables. They now fall mostly into:

- transient selection/draft UI state;
- day/night flow orchestration;
- role-action UI state;
- recommendation/display state;
- the session owner/view/ruleset references;
- event/UI sequencing.

This is materially different from the pre-D6.1 problem: these variables are not a second canonical mechanical GameState owner.

### Recovery composition

The App root still contains a substantial Recovery adapter block around snapshot build/apply, but PS5 already established mature Recovery owners and accepted lifecycle/write-gate semantics. It is now a lower-priority decomposition target than the Judge UI boundary.

### Derived GameState readers

Production `toClocktowerGameState` call counts include:

```text
ClocktowerHostScreen.kt                    16
CampBoardGameHostApp.kt                    11
ClocktowerFirstNightInformationRequest.kt   2
ClocktowerDemonAttackProductionAdapter.kt   2
ClocktowerNightScreen.kt                     1
ClocktowerHistoryScreen.kt                   1
ClocktowerDayScreen.kt                       1
ClocktowerGameStateAdapter.kt                1
```

These are mostly derived read/pre-session/recommendation/UI projections. D6.1e already proved they are not competing writers. Replacing them mechanically is not selected work.

## Candidate ranking

### 1. D6.2 — Judge UI composition boundary decomposition — SELECTED

Why first:

- highest current fan-out;
- largest production file is now `ClocktowerHostScreen.kt`, not App root;
- 103 parameters / 39 callbacks create high change amplification and AI/human editing cost;
- existing Day/Night/History screens provide real seams to build on;
- can reduce composition coupling without changing canonical session semantics.

### 2. Cohesive day/night durable orchestration extraction — DEFER

Still valuable, but D6.1 has already moved canonical mechanical writers. Starting another broad day/night mutation rewrite now would create more semantic risk than UI-boundary cleanup and could duplicate existing session/planner owners.

Re-audit after D6.2.

### 3. Root Recovery composition cleanup — DEFER

The App adapter is large, but Recovery has mature PS5 owners and frozen behavior. Decomposition should follow clearer per-game/composition boundaries later, not reopen schema/lifecycle design.

### 4. Broad `cards.toClocktowerGameState` reader replacement — REJECT

This would optimize source shape rather than ownership and would increase coupling between presentation and canonical domain state.

## D6.2 design rule

Do **not** solve 39 callbacks by creating one `ClocktowerJudgeActions` object containing the same 39 functions. That only hides fan-out behind one parameter.

Do **not** create a new broad Controller/ViewModel merely because `ClocktowerJudgeScreen` is large. Canonical domain/session ownership is already established.

Instead decompose by **cohesive UI responsibility**:

```text
stable shared Judge context
+ phase-specific UI state
+ small phase/ability action contracts
+ existing domain/session owners beneath them
```

A useful boundary must reduce the number of unrelated concepts a caller or child screen must know, not merely reduce the visible argument count.

## D6.2a — NEXT: parameter-consumption characterization

Before production edits:

1. classify all 103 `ClocktowerJudgeScreen` parameters by responsibility and actual consumer;
2. classify the 39 callbacks into cohesive phase/ability groups;
3. map which values are shared shell context versus FirstNight/Night/Day/Dawn-only;
4. map existing child screens/components and which parameters are forwarded unchanged;
5. identify UI state currently passed as `MutableState<T>` and decide which component should own each transient state;
6. distinguish callbacks that are pure UI selection from callbacks that cross a domain/session/durable boundary;
7. identify the smallest group whose extraction removes real cross-phase knowledge from both App and HostScreen;
8. baseline existing focused UI/flow tests; add a new typed characterization only if a genuine stable gap exists.

D6.2a is read/design-first. Do not edit production until the responsibility matrix and first slice are explicit.

## Likely extraction shape, not yet authorized implementation

Prefer several small contracts aligned with real flows, for example:

```text
Judge shared read context
Night flow UI state/actions
Day flow UI state/actions
ability-specific contracts only where a real child owner exists
```

The exact grouping must come from D6.2a consumption data. Avoid pre-committing to one giant state bag or action bag.

## D6.2 invariants

Preserve:

- `ClocktowerGameSession` as canonical writable domain/session owner;
- exact game/input revision cadence;
- semantic chronology and idempotency;
- Recovery v2 and PS5 write/lifecycle topology;
- A4 durability/invalidation ordering;
- existing recommendation semantics;
- all gameplay/rule behavior;
- current user-visible UI behavior during structural slices;
- Undercover/Werewolf isolation.

No Compose type may move into session/domain code.

## Branch/PR strategy

D6.1 is a complete accepted logical checkpoint. Do not continue D6.2 production work inside PR #113.

Preferred sequence:

```text
user authorizes merge of PR #113
-> re-confirm merged main
-> new D6.2 branch from main
-> D6.2a characterization
-> smallest cohesive UI boundary slice
```

This keeps session-authority and UI-composition refactors independently reviewable.
