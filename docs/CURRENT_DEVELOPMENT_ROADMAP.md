# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
live main: 2c495ee547e8327b0d3c3a811f5c891ba34fd863
active branch: codex/d6-root-reaudit
active draft PR: #113 D6: Clocktower session authority cutover
latest tested D6 production checkpoint: bd0161c50a7e4d2c94187c553546696bf6e81aee
pre-closeout cleanup head: 68e8d8c9458a9e9e192c711199e352c9623db712
```

D6.1c validation evidence:

```text
D6.1c one-shot 34203877479 — PASS
CI 34203881890 — PASS
R2 34203881911 — PASS
```

PR #113 remains **Draft / open / do not merge yet**.

## Current priority

> **D6.0 COMPLETE → D6.1a COMPLETE → D6.1b COMPLETE → D6.1c COMPLETE → D6.1d canonical GameState ownership re-audit/cutover NEXT.**

The goal is ownership decomposition, not line-count shuffling. `CampBoardGameHostApp.kt` remains very large, but the primary acceptance criterion is that canonical state and policy move to coherent owners rather than merely moving code into helper files.

The strongest existing owner is `ClocktowerGameSession`. D6.1c has now made it the sole writer for common Clocktower identity/revision/semantic-history authority. The remaining major session-authority gap is canonical mechanical `GameState` ownership.

Authoritative D6 docs:

- `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
- `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
- `docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`
- `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

Historical PS5 completion evidence remains under `docs/archive/checkpoints/`.

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

D6.1a mapped the actual post-PS5 wiring before production edits and identified App-root writable identity/revision/history plus App-root cards/mechanics as the main remaining session authority.

Important constraints remain:

1. production No Greater Joy legitimately has no advanced `RulesetRef`;
2. `GameSnapshot` is a strict ruleset-backed projection, not the universal production owner;
3. production `gameStateRevision` cadence is broader than equality-based `updateGameState()` semantics;
4. global observation preflight is intentionally non-mutating;
5. A4/recommendation/Judge revisions are observable cache/request identity;
6. a plain mutable session object is not automatically Compose-observable.

## D6.1b — COMPLETE: production-compatible session core

D6.1b added the ruleset-independent production session state/API while preserving strict `GameSnapshot` projection.

```text
RED: 97ff03989c57bb6e5337380cf14d080164685b64
final tested checkpoint: 4373c0225deb733c305e233e07bf7078577de05d
CI 34198474844 — PASS
R2 34198474815 — PASS
```

It proved NGJ create/restore without a synthetic ruleset, exact identity/revision/history restore, one global chronology, explicit revision advancement independent of `GameState` equality, equality-based update semantics, and non-mutating observation preflight.

## D6.1c — COMPLETE: App-root identity/revision/semantic-chronology authority cutover

Production checkpoint:

```text
bd0161c50a7e4d2c94187c553546696bf6e81aee
refactor: cut over Clocktower session authority
```

Validation:

```text
D6.1c one-shot 34203877479 — PASS
CI 34203881890 — PASS
R2 34203881911 — PASS
exact ownership audit — PASS
immutable projection audit — PASS
archive/reset/recovery session-boundary audit — PASS
```

D6.1c now enforces:

```text
ClocktowerGameSession = sole writable identity/revision/history owner
-> immutable ClocktowerSessionView projection
-> Compose / A4 / recommendation / Recovery / Judge readers
```

App root no longer independently writes game ID/seed/script projection, both revisions, semantic-history mode, action timeline, observation log or global cursor.

The projection is safe because timeline/log value types snapshot lists and expose unmodifiable storage; append operations return new value objects.

Recovery v2 schema/planner/codec and PS5 persistence topology were not redesigned.

### Deliberate D6.1c limit

D6.1c did **not** make `ClocktowerSessionState.gameState` the live canonical production mechanical state. App-root `cards` and related mechanics still mutate independently between projections. Production consumers must not start treating session `gameState` as canonical until D6.1d proves and performs the relevant cutover.

Detailed evidence: `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`.

## D6.1d — NEXT: canonical GameState ownership in cohesive pieces

### First action: source ownership audit, not implementation by assumption

Map the current post-D6.1c production paths for:

- App-root mechanical state holders;
- every `cards` mutation path;
- every `cards.toClocktowerGameState(...)` projection;
- every `advanceClocktowerGameStateRevision()` callsite;
- session `gameState` reads/writes;
- A4/recommendation/Judge/Recovery dependencies on revision identity versus actual mechanics.

Rank candidate slices by:

1. ownership cohesion;
2. ability to move state + complete mutation authority together;
3. actual reduction in App-root authority;
4. existing behavioral evidence / testability;
5. Compose/Android coupling;
6. blast radius.

Then implement **one smallest high-confidence complete owner slice**. Do not choose the largest code block merely because it reduces the most lines.

### D6.1d guardrails

- do not rewrite all day/night mechanics at once;
- preserve exact revision cadence/order;
- do not change gameplay semantics;
- do not change Recovery v2/current-version-only policy;
- do not reopen RecoveryWriteGate/lifecycle topology;
- do not create synthetic NGJ `RulesetRef`;
- do not make Compose a session/domain dependency;
- do not introduce a generic Manager/Controller;
- do not touch Undercover/Werewolf;
- do not merge PR #113.

Testing is risk-based: existing focused characterization first for behavior-preserving ownership movement; add a typed RED only when the audit exposes a real missing invariant. Run focused + `:app:testFast` + affected T2/R2 according to changed surface.

## D6.1e — D6.1 cleanup and acceptance

After D6.1d ownership is stable:

- remove obsolete compatibility paths only when no production caller remains;
- re-audit Recovery/session projections and root writer count;
- focused + FAST + affected T2;
- one reserved `[full-ci]` T4 at the D6.1 logical acceptance checkpoint.

Real Clingo is not selected merely for structural ownership movement.

## D6.1 invariants

Preserve all:

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
