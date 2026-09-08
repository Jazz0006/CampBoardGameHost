# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Active branch: `codex/d6-root-reaudit`
> Draft PR: #113 `D6: Clocktower session authority cutover`
> Status: **D6.0 + D6.1a + D6.1b COMPLETE — D6.1c APP-ROOT IDENTITY/REVISION/SEMANTIC-CHRONOLOGY AUTHORITY CUTOVER NEXT — DO NOT MERGE YET**

## Read first

Treat these as authority before continuing D6:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
5. `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
6. `docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`
7. this handoff
8. `docs/archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` only when persistence-history context is needed

Before every write sequence, re-confirm live `main`, branch head, PR state and current checks.

## Completed predecessor: PS5 Persistence Simplification

PR #112 is merged and PS5 is complete. Recovery remains a frozen D6 constraint:

- current-version-only, 4-hour emergency continuity;
- Archive is separate;
- unsupported/old Recovery fails closed;
- failed persistence retains retry opportunity;
- A4 rebuild cannot release before persistence succeeds;
- recovered seating ownership is already fixed and accepted;
- ordinary `SideEffect`, forced `ON_PAUSE`, ordinary `ON_STOP` persistence topology remains unchanged.

Do not reopen persistence design merely to simplify D6.

## D6.0 — COMPLETE

D6.0 changed the decomposition goal from size-first splitting to ownership decomposition.

Confirmed existing owners that should not be re-extracted first:

- `HostSeatingSetupFlow`;
- PS5 Recovery/persistence owners;
- Archive codec/history boundary;
- existing recommendation/setup coordinators;
- A4 cache/durability lifecycle as a derived consumer.

The strongest remaining session seam is:

`app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt`

Do not create a parallel generic Manager/Controller.

## D6.1a — COMPLETE: production wiring characterization

D6.1a confirmed that `CampBoardGameHostApp.kt` directly owned/mutated:

- current Clocktower script/game ID/seed;
- both revisions;
- semantic-history mode;
- action timeline;
- epistemic observation history;
- next global timeline sequence;
- nullable advanced ruleset projection;
- cards/mechanics from which current `GameState` is derived.

Important constraints:

1. No Greater Joy intentionally has no advanced `RulesetRef` in current production/Recovery.
2. `GameSnapshot` requires a real `RulesetRef` and therefore cannot be the universal production owner.
3. App-root `gameStateRevision` cadence is broader than equality-based `ClocktowerGameSession.updateGameState()`.
4. revision values feed A4/recommendation identity and must not drift.
5. observation preflight is deliberately non-mutating.
6. a plain mutable session object is not Compose-observable.

## D6.1b — COMPLETE: production-compatible session core

D6.1b established the missing owner API before touching the giant App root.

Evidence:

```text
RED
97ff03989c57bb6e5337380cf14d080164685b64
CI 34197821003 — expected FAIL

final tested production/test checkpoint
4373c0225deb733c305e233e07bf7078577de05d
CI 34198474844 — PASS
R2 34198474815 — PASS

subsequent docs-only checkpoint before roadmap/handoff refresh
3907da0a4fb9b0873df30ef8b4c50e280c77c617
CI 34198706043 — PASS
R2 34198705999 — PASS
```

D6.1b contract now proves:

- production session create/restore works for NGJ without synthetic `RulesetRef`;
- identity/revisions/history/global chronology restore exactly;
- action and observation share one global cursor;
- explicit production revision advancement can preserve current cadence independently of `GameState` equality;
- equality-based `updateGameState()` remains available;
- observation preflight is non-mutating;
- strict `GameSnapshot` remains a projection when a real ruleset ref exists.

D6.1b changed only session production/test code plus D6 docs. It did not change `CampBoardGameHostApp.kt` or Recovery schema/planner/codec.

## D6.1c — NEXT

### Title

**App-root identity/revision/semantic-chronology authority cutover**

### Goal

Create/restore/end exactly one live `ClocktowerGameSession` and make it the sole writer for the already-proven common production subset:

- game ID;
- game seed;
- script identity represented by session state;
- `gameStateRevision`;
- `playerInputRevision`;
- semantic-history mode;
- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- next global timeline sequence.

### Required ownership direction

```text
ClocktowerGameSession = sole writable authority
-> immutable/read-only observable projection
-> Compose / A4 / recommendation / Recovery / Judge readers
```

Forbidden:

```text
ClocktowerGameSession writable state
<-> separately writable App-root canonical mirror
```

The App may hold one immutable projection in Compose state to trigger recomposition. All writes must go through session APIs first, then publish a fresh projection. Projected fields must not have independent setters.

### Do not prematurely cut over GameState

`ClocktowerSessionState` contains a `gameState`, but D6.1c must not start treating that field as production-canonical.

Current Clocktower mechanics still mutate `cards` and related App-root state. Until D6.1d explicitly synchronizes/proves those mechanical mutations, session `gameState` is only the seed/script-bearing state used to establish the owner contract.

D6.1c consumers should read the session identity/revision/history subset only.

### Exact App-root anchors already audited

Session creation/reset:

- `resetDealState()`

Recovery reconstruction:

- `applyValidatedRecoveryPlan()`

Recovery serialization:

- `activeGameRecoverySnapshot()`

Revision mutation wrappers:

- `advanceClocktowerGameStateRevision()`
- `advanceClocktowerPlayerInputRevision()`

Semantic chronology mutation:

- `recordClocktowerAction()`
- `recordEpistemicObservation()`
- `preflightClocktowerPublicAliveObservation()`

Read consumers that must become projection/session-derived:

- A4 initial identity prewarm request;
- A4 observation cache rebuild request;
- A4 `LaunchedEffect` revision identity;
- `currentClocktowerNightCheckpoint()`;
- `clocktowerActionId()`;
- `ClocktowerJudgeScreen(...)` revision/identity inputs;
- debug revision fields;
- Recovery identity/history projection;
- durable night/dawn recovery-authority reads from action timeline/history.

### Start/recovery construction rule

New game:

1. cards/setup are already prepared;
2. derive the temporary current `GameState` exactly as production already does;
3. call `ClocktowerGameSession.createProduction(...)` once with the real game ID/seed and `GLOBAL_V1`;
4. publish its immutable read projection;
5. resolve/store TB advanced ruleset projection separately as today; NGJ remains null.

Recovery:

1. validated Recovery remains source of persisted identity/history/cards/mechanics;
2. reconstruct the temporary `GameState` from restored cards/script/seed using existing production conversion;
3. build one `ClocktowerSessionState` from Recovery identity/history plus that temporary state;
4. call `ClocktowerGameSession.restoreProduction(...)` once;
5. publish the read-only projection before downstream Clocktower consumers run;
6. keep Recovery v2 schema unchanged.

### Mutation rule

Revision wrappers become session mutation adapters:

```text
session.advanceGameStateRevision()/recordPlayerInput()
-> publish projection
-> preserve existing A4 invalidation timing
```

Global action/observation functions must use the **instance** session authority instead of stateless companion transitions plus copied-back root fields.

Observation preflight must call the instance non-mutating preflight and must not publish a projection.

### Compose observability rule

Do not rely on `mutableStateOf(session)` plus internal mutation; session identity is stable and Compose would not observe internal state changes.

Use one immutable projection value, for example conceptually:

```text
var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
```

The projection must be a read model only. Prefer a session/domain type that excludes or clearly prevents use of stale `gameState` during D6.1c.

Do not introduce Compose imports into `clocktower/session`.

### Recommended D6.1c preparation before App patch

Before modifying the giant App root, add the smallest session-side read projection/API needed to expose only the cutover-safe subset.

A good projection contains:

- gameId;
- script ID;
- gameSeed;
- both revisions;
- semantic-history mode;
- action timeline;
- observation log;
- global cursor.

It should not expose mutable setters and should not encourage production consumers to read session `gameState` before D6.1d.

If this projection is a trivial immutable mapping with no new behavior, characterization tests are sufficient; do not manufacture a meaningless RED.

### Large-file App patch requirements

`CampBoardGameHostApp.kt` remains a giant source file. Follow `AGENTS.md` large-file fail-closed rules:

- use stable audited anchors;
- patch only the planned ownership blocks;
- fail if an anchor count differs from expectation;
- do not opportunistically reformat unrelated code;
- exact diff audit after the patch;
- no intermediate committed state with two writable canonical owners.

### D6.1c production acceptance

At the end of the slice, App root must no longer independently write the cutover subset.

A shorter file alone is not acceptance. The slice fails if App still owns revision/history values and merely forwards copies to a helper/session.

### D6.1c validation

Use existing characterization for behavior-preserving structural movement unless a real gap appears.

Required:

```text
focused ClocktowerGameSession/global-history tests
focused affected Recovery tests
focused affected A4/recommendation tests where revision identity is touched
:app:testFast
R2 main-thread boundary
exact changed-file/ownership audit
git diff --check
```

No T4 yet unless semantics unexpectedly expand. One reserved `[full-ci]` T4 belongs at D6.1 acceptance after later cleanup.

## D6.1d — after D6.1c GREEN

Cut over canonical `GameState` projection/mutation in cohesive pieces only where behavior equivalence is proven.

Do not rewrite every Clocktower day/night mechanic at once.

## D6.1e — acceptance/cleanup

- Recovery reads session-owned identity/history without schema change;
- strict ruleset-backed projection feeds A4 where appropriate;
- recommendation/Judge revision identity is session-derived;
- obsolete stateless session compatibility APIs are removed only when no production callers remain;
- run focused + FAST + affected T2;
- run one reserved `[full-ci]` T4 at logical D6.1 acceptance.

## D6.1 invariants

Preserve all:

1. same game ID/seed/script behavior;
2. no fake NGJ `RulesetRef`;
3. exact revision cadence;
4. one monotonic collision-free global cursor;
5. action/observation idempotency unchanged;
6. preflight non-mutating;
7. no storyteller-hidden target leak;
8. Recovery v2/current-version-only policy unchanged;
9. persistence topology unchanged;
10. A4 durability/invalidation/prewarm ordering unchanged;
11. no Compose dependency in session/domain;
12. restart/end/recovery cannot leak stale session;
13. Undercover/Werewolf untouched;
14. no gameplay/user-visible semantic change.

## Post-D6.1 provisional route

```text
D6.1 session authority cutover
-> D6.2 cohesive Clocktower durable day/night mutation boundaries
-> D6.3 shrink ClocktowerJudgeScreen state/callback surface
-> D6.4 simplify root Recovery composition after per-game authority moves down
-> D6.5 re-audit remaining root responsibilities / file size / callback fan-out
```

Re-audit after every completed ownership cutover.

## Suggested continuation prompt

```text
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md、docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md、docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md 和当前 D6 handoff。

重新确认 live main、codex/d6-root-reaudit 和 draft PR #113。继续 D6.1c：先建立只读 session projection/API，明确排除尚未 canonical 的 GameState；然后用 fail-closed 大文件 patch 将 CampBoardGameHostApp.kt 的 identity/revisions/semantic-history writable authority 切到唯一 ClocktowerGameSession。保持 Recovery v2、NGJ null RulesetRef、revision cadence、A4 invalidation/durability 和 non-mutating preflight 完全不变。完成 focused + FAST + R2 + exact ownership/diff audit 后停止，不要 merge PR。
```
