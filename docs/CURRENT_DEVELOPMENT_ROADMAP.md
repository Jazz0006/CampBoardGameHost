# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
live main: 2c495ee547e8327b0d3c3a811f5c891ba34fd863
active branch: codex/d6-root-reaudit
active draft PR: #113 D6: Clocktower session authority cutover
latest tested D6 production checkpoint: 4373c0225deb733c305e233e07bf7078577de05d
latest pre-roadmap docs-only checkpoint: 3907da0a4fb9b0873df30ef8b4c50e280c77c617
```

Validation on the latest pre-roadmap docs-only checkpoint:

```text
CI 34198706043 — PASS
R2 34198705999 — PASS
```

PR #113 remains **Draft / open / do not merge yet**.

## Current priority

> **D6.0 COMPLETE → D6.1a COMPLETE → D6.1b COMPLETE → D6.1c App-root identity/revision/semantic-chronology authority cutover NEXT.**

The goal is ownership decomposition, not line-count shuffling. `CampBoardGameHostApp.kt` is still very large, but the primary acceptance criterion is that canonical state and policy move to coherent owners rather than merely moving code into helper files.

The strongest existing owner is `ClocktowerGameSession`. D6.1b has now made it production-compatible for common Clocktower identity/revision/history ownership without requiring a synthetic advanced `RulesetRef`, including No Greater Joy.

Authoritative D6 docs:

- `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
- `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
- `docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

Historical PS5 completion evidence remains in:

- `docs/archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`
- `docs/archive/checkpoints/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

## Persistence Simplification / PS5 — COMPLETE and frozen for D6

PR #112 is merged. Recovery remains:

- current-version-only;
- 4-hour emergency continuity;
- separate from Archive;
- fail-closed for unsupported/old recovery data;
- retry-capable after failed persistence;
- ordered so A4 rebuild cannot release before durable persistence succeeds;
- responsible for restoring the game, not every transient UI gesture.

Final persistence trigger topology remains:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

Do not redesign Recovery, Archive, seating ownership or this persistence topology merely to simplify D6 wiring.

## D6.0 — COMPLETE: post-persistence ownership re-audit

D6.0 established:

1. `HostSeatingSetupFlow` already owns seating-first state; do not re-extract it.
2. PS5 Recovery/persistence owners remain authoritative.
3. Archive remains a separate durable/history contract.
4. A4 is a derived cache/durability consumer, not session authority.
5. existing recommendation/setup coordinators remain owners; do not create a broad recommendation manager.
6. `ClocktowerGameSession` is the strongest existing seam for Clocktower session authority.
7. file size is a smell/budget metric, not the main architecture acceptance test.

Desired dependency direction:

```text
Clocktower domain/session authority
-> recovery / recommendation / A4 projections and consumers
-> App root / Compose composition and presentation orchestration
```

## D6.1a — COMPLETE: production-wiring characterization

D6.1a mapped the actual post-PS5 wiring before production edits.

Confirmed App-root writable authority included:

- current Clocktower script/game ID/seed;
- both revisions;
- semantic-history mode;
- action timeline;
- epistemic observation history;
- one shared global chronology cursor;
- ruleset projection;
- cards/mechanics from which `GameState` is currently derived.

Important constraints discovered:

1. production No Greater Joy legitimately has no advanced `RulesetRef`; do not invent one.
2. `GameSnapshot` remains a strict ruleset-backed projection, not the universal production owner.
3. production `gameStateRevision` cadence is broader than equality-based `updateGameState()` semantics and must be preserved exactly.
4. global observation preflight is intentionally non-mutating.
5. A4/recommendation/Judge revisions are observable cache/request identity and therefore behavioral contracts.
6. a plain mutable session object is not automatically Compose-observable.

## D6.1b — COMPLETE: production-compatible session core

D6.1b added a ruleset-independent production session state/API while preserving strict `GameSnapshot` projection.

Typed RED/GREEN evidence:

```text
RED: 97ff03989c57bb6e5337380cf14d080164685b64
final tested production/test checkpoint:
4373c0225deb733c305e233e07bf7078577de05d

CI 34198474844 — PASS
R2 34198474815 — PASS
```

The final D6.1b contract proves:

- No Greater Joy can create/restore a production session without synthetic `RulesetRef`;
- identity, revisions, semantic mode, action timeline, observation log and global cursor restore exactly;
- action + observation share one monotonic global chronology;
- explicit production game-state revision advancement is independent of `GameState` equality;
- equality-based `updateGameState()` still exists for state-difference semantics;
- global observation preflight does not mutate session state;
- strict `GameSnapshot` projection remains available when a real `RulesetRef` exists.

D6.1b did **not** modify `CampBoardGameHostApp.kt` or Recovery schema/planner/codec.

## D6.1c — NEXT: App-root identity/revision/semantic-chronology authority cutover

### Goal

Make exactly one live `ClocktowerGameSession` the writable authority for the subset already proven safe in D6.1b:

- game ID;
- game seed;
- script identity as represented by the session core;
- game-state revision;
- player-input revision;
- semantic-history mode;
- action timeline;
- epistemic observation log;
- next global timeline sequence.

### Compose boundary

Compose may hold an **immutable/read-only observable projection** of session-owned values solely to trigger recomposition.

Allowed:

```text
ClocktowerGameSession = sole writer
-> immutable observable projection
-> Compose / A4 / recommendation / Recovery / Judge reads
```

Forbidden:

```text
ClocktowerGameSession writable state
<-> independently writable App-root mirror
```

Every mutation helper must update the session first, then publish a fresh read-only projection. App code must not mutate individual projected fields.

### Critical GameState boundary

D6.1c must **not** claim that `ClocktowerSessionState.gameState` is already the live production canonical `GameState`.

Today the actual game mechanics still mutate `cards` and related state in App root, and the session's `GameState` will not be synchronized at every mechanic boundary until D6.1d.

Therefore D6.1c consumers may use the session's identity/revision/history subset, but must not start reading its `gameState` as authoritative merely because the session now exists.

### Exact cutover anchors already identified

Creation / reset:

- `resetDealState()`

Recovery:

- `applyValidatedRecoveryPlan()`
- `activeGameRecoverySnapshot()`

Mutation wrappers:

- `advanceClocktowerGameStateRevision()`
- `advanceClocktowerPlayerInputRevision()`
- `recordClocktowerAction()`
- `recordEpistemicObservation()`
- `preflightClocktowerPublicAliveObservation()`

Read consumers:

- A4 initial prewarm/rebuild requests;
- `currentClocktowerNightCheckpoint()`;
- `clocktowerActionId()`;
- `ClocktowerJudgeScreen(...)` parameters;
- debug revision fields;
- Recovery history projection.

### D6.1c implementation rules

1. create exactly one new production session on Clocktower game start;
2. restore exactly one session from validated Recovery identity/history plus a temporary current `GameState` projection, without changing Recovery v2 schema;
3. clear/replace the owner at session boundary/restart so stale session state cannot leak;
4. route all revision and global semantic-history mutations through instance session APIs;
5. keep preflight non-mutating;
6. preserve A4 invalidation and durability ordering exactly;
7. preserve current revision increment count/order exactly;
8. do not introduce a generic Manager/Controller or Compose dependency into session/domain code;
9. do not modify Undercover/Werewolf;
10. use the large-file fail-closed patch workflow for `CampBoardGameHostApp.kt`.

### D6.1c validation

This is a structural ownership cutover with already-characterized behavior, so do not manufacture a RED solely for code movement. Add a typed characterization only if the implementation exposes a real uncovered invariant.

Required evidence:

```text
focused ClocktowerGameSession/global-history tests
+ affected Recovery tests
+ affected A4/recommendation tests where revision identity is touched
+ :app:testFast
+ R2 because App-root / main-thread structure changes
+ git diff --check
+ exact changed-file / ownership audit
```

No T4 yet unless semantic blast radius unexpectedly expands.

## D6.1d — after D6.1c GREEN: canonical GameState cutover

Only after identity/revision/history authority is stable, route canonical `GameState` updates/projections through the session where behavior equivalence is proven.

Do not rewrite all day/night mechanics at once. Prefer cohesive mechanical mutation boundaries and keep revision cadence unchanged.

## D6.1e — D6.1 cleanup and acceptance

- Recovery reads session-owned identity/history while v2 schema remains unchanged;
- recovery restore constructs the owner before dependent consumers run;
- A4 consumes strict ruleset-backed projection where appropriate;
- recommendation/Judge use session-derived revision identity;
- remove obsolete stateless production compatibility APIs only when no production caller remains;
- focused + FAST + affected T2;
- one reserved `[full-ci]` T4 at the D6.1 logical acceptance checkpoint.

Real Clingo is not selected merely for structural ownership movement.

## D6.1 invariants

Preserve all of these:

1. same game ID/seed/script behavior;
2. NGJ never acquires a fake/synthetic advanced `RulesetRef`;
3. no lost/double revision increments or cadence drift;
4. one monotonic collision-free semantic global cursor;
5. action/observation idempotency unchanged;
6. observation preflight remains non-mutating;
7. no hidden storyteller target leaks into player-visible durable history;
8. Recovery v2 schema/current-version-only policy unchanged;
9. RecoveryWriteGate/lifecycle/SideEffect topology unchanged;
10. A4 durability/invalidation/prewarm ordering unchanged;
11. Compose does not become a session/domain dependency;
12. restart/end/recovery cannot leak a stale session;
13. Undercover/Werewolf untouched;
14. no gameplay semantic or user-visible behavior change.

## Post-D6.1 provisional route

```text
D6.1 session authority cutover
-> D6.2 cohesive Clocktower durable day/night mutation boundaries
-> D6.3 shrink ClocktowerJudgeScreen state/callback surface
-> D6.4 simplify root Recovery composition after per-game authority moves down
-> D6.5 re-audit remaining root responsibilities / file size / callback fan-out
```

Re-audit after every completed ownership cutover instead of following a rigid size-first sequence.

## Later priority after D6

```text
D6 ownership decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
