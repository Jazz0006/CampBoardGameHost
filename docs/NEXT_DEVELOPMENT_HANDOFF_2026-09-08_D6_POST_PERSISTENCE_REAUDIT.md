# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Persistence Simplification merge commit: `1b502c75357a2de7c928c88668e6a9613521b4ac`
> Status: **D6.0 + D6.1a COMPLETE — D6.1b SESSION-CORE COMPATIBILITY RED/GREEN NEXT — NO D6 PRODUCTION APP-ROOT CUTOVER STARTED**

## Read first

Treat these as authority before continuing D6:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
5. `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
6. this handoff
7. `docs/archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` only when persistence-history context is needed

Before every write sequence, re-confirm live `main` and the D6 branch head.

## Completed predecessor

Persistence Simplification / PS5 is fully complete and merged in PR #112. Recovery remains a current-version-only, 4-hour emergency continuity contract; Archive remains separate. Recovery write-gate retry/deduplication, pause/stop forcing rules, ordinary Compose `SideEffect` persistence opportunity, A4 durability ordering and recovered seating ownership are frozen D6 constraints.

Do not reopen persistence design merely to simplify D6 wiring.

## D6.0 — COMPLETE

D6.0 established that the goal is ownership decomposition rather than file-size shuffling.

Confirmed existing owners that should not be re-extracted first:

- `HostSeatingSetupFlow` — seating-first state;
- PS5 Recovery/persistence coordinator/write-gate/snapshot/planner owners;
- Archive codec/history boundary separate from Recovery;
- existing recommendation/setup coordinators;
- A4 cache/durability lifecycle as a derived consumer.

The strongest remaining seam is the existing:

`app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt`

Do not create a generic session Manager/Controller parallel to it.

## D6.1a — COMPLETE

Detailed characterization:

`docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`

D6.1a did **not** modify production code.

### Current App-root authority confirmed

`CampBoardGameHostApp.kt` directly owns or mutates:

- Clocktower script/game ID/seed;
- nullable advanced `clocktowerRulesetRef`;
- `clocktowerGameStateRevision`;
- `clocktowerPlayerInputRevision`;
- semantic-history mode;
- `ActionFactTimeline`;
- epistemic observation list;
- next global timeline sequence;
- cards/mechanics from which canonical `GameState` is derived.

Global action/observation legality, idempotency and global cursor allocation already delegate to stateless `ClocktowerGameSession` companion transitions, then copy results back into root state. This is the core duplicated-ownership smell.

### Production creation/start boundary

`resetDealState()` establishes a new Clocktower game lifetime:

- new game ID / seed;
- revisions reset to zero;
- semantic history reset to `GLOBAL_V1` + empty timeline/log/cursor;
- Trouble Brewing resolves its advanced ruleset ref;
- No Greater Joy intentionally leaves the advanced ruleset ref null.

A final live owner must exist before recovery/A4/recommendation consumers observe canonical session state and must be replaced/cleared on restart/end.

### Recovery boundary

Recovery v2 persists identity/history plus cards/mechanics, not a serialized `GameSnapshot`.

`applyValidatedRecoveryPlan()` currently rehydrates root fields one-by-one. No live `ClocktowerGameSession` owner is restored.

This wiring gap is real, but D6 must keep Recovery v2 schema unchanged.

### Model mismatch discovered by D6.1a

A direct `ClocktowerGameSession(GameSnapshot)` production cutover is unsafe today:

1. `GameSnapshot` requires non-null `RulesetRef`.
2. production No Greater Joy intentionally has no advanced `RulesetRef`; `RecoveryRestorePlannerNoGreaterJoyTest` protects successful recovery with null.
3. the session companion comments explicitly explain that the stateless production path exists partly to avoid requiring a synthetic RulesetRef when advanced ruleset state is not loaded.

Do not solve this by inventing a fake NGJ ref or by making only Trouble Brewing session-owned.

### Revision mismatch discovered by D6.1a

`ClocktowerGameSession.updateGameState()` increments `gameStateRevision` only when the new `GameState` differs.

Production App root advances `gameStateRevision` explicitly at accepted event/decision boundaries, and some flows increment more than once during one larger user action.

D6 must preserve the **existing revision values/cadence exactly**, because those revisions feed recommendation/A4 request identity, invalidation and Recovery history.

Do not replace root revision increments mechanically with equality-based `updateGameState()`.

### Compose observability constraint

`ClocktowerGameSession.snapshot` is a plain mutable property. Mutating it does not by itself invalidate Compose if only the session object reference is held in `mutableStateOf`.

Correct dependency direction:

```text
ClocktowerGameSession = sole writable authority
-> one-way read-only observable projection at App/Compose boundary
-> UI / A4 / recommendation / Recovery consumers
```

Forbidden direction:

```text
ClocktowerGameSession writable state
<-> App writable duplicate canonical state
```

### Non-mutating preflight constraint

`preflightClocktowerPublicAliveObservation()` intentionally validates a proposed global observation without committing it. A live-session cutover must keep a non-mutating preflight path; do not replace it with the mutating instance commit API.

## D6.1b — NEXT

### Title

**`ClocktowerGameSession` production-core compatibility RED/GREEN**

### Goal

Before touching the 238 KB App root, make the existing session owner capable of representing the common production identity/revision/history contract for **both** Trouble Brewing and No Greater Joy without a synthetic advanced `RulesetRef`.

Prefer adapting `ClocktowerGameSession` internals/API over creating a parallel generic owner.

A strict `GameSnapshot` remains a projection for consumers that actually have a resolved `RulesetRef`; do not change Recovery schema to satisfy it.

### Required typed RED

Add the smallest stable pure-session test(s) proving:

1. NGJ production session create/restore does not require a synthetic advanced `RulesetRef`.
2. game ID/script/seed, game/player revisions, semantic-history mode, action timeline, observation log and global cursor restore exactly.
3. an explicit production game-state revision transition can preserve the existing cadence independently of `GameState` equality.
4. existing equality-based `updateGameState()` behavior remains available for operations that want state-difference semantics.
5. global observation preflight can validate without mutating session state.
6. when a real resolved `RulesetRef` exists, strict `GameSnapshot` projection preserves existing seed/script/ruleset invariants.

Do not use source-string tests or Compose internals to establish this contract.

### Expected D6.1b allowlist

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt
possibly one new typed session production-contract test
docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
this handoff
```

A tightly related domain projection type may be changed only if the RED proves it is unavoidable. Do not modify Recovery schema/planner/codec in D6.1b.

Prefer **no `CampBoardGameHostApp.kt` change in D6.1b**. Establish and test the owner API first.

### D6.1b validation

```text
session production-contract RED
-> focused session tests
-> minimal session-core GREEN
-> focused ClocktowerGameSession/global-history tests
-> :app:testFast
-> git diff --check
-> exact changed-file/API audit
```

No full T4 is required for the isolated session-core API GREEN unless the change unexpectedly expands semantic blast radius.

## D6.1c — after D6.1b GREEN

App-root authority cutover for:

- session identity;
- both revisions;
- semantic-history mode;
- action timeline;
- epistemic observation log;
- global cursor.

Create/restore/end exactly one session owner, preserve a one-way Compose projection if needed, and remove corresponding writable root duplicates atomically where practical.

Use the large-file fail-closed workflow for `CampBoardGameHostApp.kt`.

## D6.1d — after revision/history authority is stable

Cut over canonical `GameState` projection/mutation only where behavior equivalence is proven. Do not rewrite every Clocktower day/night mechanic in this slice.

## D6.1e — acceptance

- Recovery reads session-owned identity/history without changing v2 schema;
- recovery restore constructs the owner before dependent consumers read it;
- A4 consumes strict ruleset-backed projection when available;
- recommendation/Judge revisions are session-derived;
- obsolete stateless production compatibility wiring is removed only when safe;
- focused + FAST + affected T2;
- R2 only if structural/main-thread risk selects it;
- one reserved `[full-ci]` T4 at D6.1 logical acceptance.

Real Clingo is not selected unless exact epistemic semantics change, which is outside D6.1 scope.

## D6.1 invariants

Preserve all of these:

1. same game ID/seed/script authority;
2. NGJ does not acquire a fake/synthetic advanced `RulesetRef`;
3. no lost/double revision increments and no revision-cadence drift;
4. one monotonic collision-free semantic global cursor;
5. action/observation idempotency unchanged;
6. observation preflight remains non-mutating;
7. no hidden storyteller target leaks into player-visible durable history;
8. Recovery v2 schema/current-version-only policy unchanged;
9. RecoveryWriteGate/lifecycle/SideEffect topology unchanged;
10. A4 durability/invalidation/prewarm ordering unchanged;
11. Compose does not become a dependency of domain/session code;
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

Re-audit after every completed ownership cutover rather than treating this as a rigid size-first sequence.

## Guardrails

- re-confirm remote heads before writes;
- behavior changes require RED;
- structural changes use existing characterization unless a real stable gap exists;
- no fake ruleset metadata to satisfy architecture;
- no TB-only/NGJ-only split authority;
- no Recovery redesign;
- no generic Manager/Controller mirror of App root;
- no intermediate committed state with two writable canonical Clocktower authorities;
- giant App edits are fail-closed and localized;
- `git diff --check` + exact changed-file/ownership audit at every logical GREEN.

## Suggested continuation prompt

```text
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md、docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md 和当前 D6 handoff。

重新确认 live main 与 codex/d6-root-reaudit head，然后执行 D6.1b：先建立 ClocktowerGameSession production-core typed RED，覆盖 NGJ 不需要 synthetic RulesetRef、恢复 identity/revisions/history、显式 revision cadence 与非 mutating observation preflight。先不要修改 CampBoardGameHostApp.kt 或 Recovery schema。RED 证明真实 gap 后做最小 GREEN、focused tests、testFast 和 exact diff audit，然后停止并报告。
```
