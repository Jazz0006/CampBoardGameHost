# D6.2 UI Composition Boundary Audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`
> Status: **ROUTE SELECTED — D6.2a CHARACTERIZATION NEXT — START FROM FRESH MAIN-BASED BRANCH**

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

The exact first slice is deliberately not predetermined.

## D6.2a — NEXT: parameter/callback consumption characterization

D6.2a is **read/design-first**. Before any production edit:

1. re-confirm live `main` and the merged #113 state;
2. create a fresh branch from current `main` (recommended name: `codex/d6-2-ui-composition`);
3. parse all 103 `ClocktowerJudgeScreen` parameters;
4. for each parameter record responsibility group, phase scope, actual consumer(s), forwarding path and whether it is shared/read-only/transient UI/durable-domain data;
5. classify all 39 callbacks by cohesive phase/ability responsibility;
6. identify callbacks that are selection-only versus callbacks crossing durable/session/history boundaries;
7. inventory all `MutableState<T>` parameters and decide which component naturally owns each transient state;
8. map existing Day/Night/ability child owners and unchanged forwarding chains;
9. identify groups that are always consumed together and groups that currently cross unrelated phases;
10. baseline focused UI/flow tests around the best candidate groups;
11. rank the smallest cohesive extraction by real fan-out reduction, behavior risk and test coverage;
12. add a typed RED only if characterization reveals a genuine stable coverage gap.

Suggested matrix columns:

```text
name
kind (value / MutableState / callback)
responsibility
phase scope
actual consumer(s)
forwarded unchanged?
transient UI or durable/domain?
current owner
existing tests
candidate cohesive contract
```

## Candidate ranking after characterization

Prefer the smallest group that:

- removes unrelated concepts from both App and HostScreen;
- maps to one real flow or child owner;
- does not own canonical domain state;
- preserves current behavior exactly;
- has focused characterization coverage;
- makes subsequent extractions easier rather than creating a new monolith.

Potential categories to evaluate, not pre-authorized implementations:

```text
Night-flow transient UI state/actions
Day-flow transient UI state/actions
shared Judge read-only context
ability-specific contracts where a real child owner already exists
```

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

D6.1 is merged and closed. D6.2 must use a fresh branch from current `main` and a separate PR.

```text
re-confirm current main
-> create codex/d6-2-ui-composition (or equivalent) from main
-> D6.2a characterization only
-> choose smallest cohesive boundary
-> tests/characterization-first implementation
-> re-audit after each completed slice
```

Do not reuse `codex/d6-root-reaudit` or PR #113.
