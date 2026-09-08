# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Active branch: `codex/d6-root-reaudit`
> Draft PR: #113 `D6: Clocktower session authority cutover`
> Status: **D6.0 + D6.1a + D6.1b + D6.1c COMPLETE — D6.1d CANONICAL GAMESTATE OWNERSHIP RE-AUDIT/CUTOVER NEXT — DO NOT MERGE YET**

## Read first

Treat these as authority before continuing D6:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
5. `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
6. `docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`
7. `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`
8. this handoff
9. persistence archive docs only when historical persistence context is needed

Before every write sequence, re-confirm live `main`, branch head, PR state and current checks.

## Frozen predecessor: PS5 Persistence Simplification

PR #112 is merged. Recovery/persistence remains a D6 constraint, not a refactor target:

- current-version-only, 4-hour emergency continuity;
- Archive separate;
- unsupported/old Recovery fails closed;
- failed persistence retains retry opportunity;
- A4 rebuild cannot release before persistence succeeds;
- recovered seating ownership already fixed and accepted;
- ordinary `SideEffect`, forced `ON_PAUSE`, ordinary `ON_STOP` topology unchanged.

Do not reopen persistence design merely to simplify D6.

## D6.0 — COMPLETE

D6.0 changed the decomposition goal from size-first splitting to ownership decomposition. Existing seating, Recovery/persistence, Archive, recommendation/setup and A4-derived-cache owners should not be re-extracted first. `ClocktowerGameSession` was selected as the strongest existing seam for Clocktower session authority.

## D6.1a — COMPLETE

Production wiring characterization established the real App-root writable identity/revision/history subset and the separate remaining `cards`/mechanics authority. It also established NGJ null-ruleset, revision-cadence, non-mutating-preflight, A4 identity and Compose-observability constraints.

## D6.1b — COMPLETE

Production-compatible session core checkpoint:

```text
4373c0225deb733c305e233e07bf7078577de05d
CI 34198474844 — PASS
R2 34198474815 — PASS
```

D6.1b supplied ruleset-independent production create/restore, explicit revision advancement, global chronology ownership and non-mutating preflight without changing App root or Recovery schema.

## D6.1c — COMPLETE: App-root identity/revision/semantic-chronology authority cutover

### Product checkpoint

```text
bd0161c50a7e4d2c94187c553546696bf6e81aee
refactor: cut over Clocktower session authority
```

Temporary tooling cleanup head before this docs closeout:

```text
68e8d8c9458a9e9e192c711199e352c9623db712
chore: remove D6.1c one-shot tooling
```

### Validation

```text
D6.1c one-shot 34203877479 — PASS
CI 34203881890 — PASS
R2 main-thread boundary 34203881911 — PASS
exact ownership audit — PASS
immutable projection audit — PASS
archive/reset/recovery boundary audit — PASS
```

The successful product cutover changed only `CampBoardGameHostApp.kt`. Temporary one-shot workflow/script were self-cleaned afterward.

The cleanup head's own CI/R2 records were `action_required` with no jobs because that bot-generated cleanup did not provide a normal runnable validation checkpoint; do not treat those empty records as a product failure. The user-authored trigger/product sequence already has normal green CI/R2 evidence above.

### D6.1c ownership contract now live

```text
ClocktowerGameSession = sole writable authority
-> immutable/read-only ClocktowerSessionView
-> Compose / A4 / recommendation / Recovery / Judge readers
```

The sole-writer subset is:

- game ID;
- game seed;
- session script identity;
- game-state revision;
- player-input revision;
- semantic-history mode;
- action timeline;
- epistemic observation log;
- next global timeline sequence.

App-root no longer keeps separately writable mirrors for these fields.

`ActionFactTimeline` and `EpistemicObservationLog` use immutable snapshot semantics, so the session view does not expose a collection write back door.

Archive/reset/recovery replace or clear the session owner and republish the projection; they do not mutate projected collections directly.

### Critical limit

D6.1c intentionally did **not** make session `gameState` production-canonical.

Mechanics still mutate App-root `cards` and related state. `ClocktowerSessionState.gameState` is therefore not safe to consume as live canonical mechanics until D6.1d audits and moves the relevant mutation authority.

## D6.1d — NEXT

### Title

**Canonical GameState ownership re-audit and bounded cutover**

### First step — mandatory source audit

Do not begin by mechanically replacing `cards.toClocktowerGameState(...)` with `session.state.gameState`.

First map the current post-D6.1c paths for:

1. App-root mechanical state holders;
2. every `cards` mutation (`add/remove/clear`, player-status/mechanical field mutations, replacements);
3. every `cards.toClocktowerGameState(...)` projection;
4. every `advanceClocktowerGameStateRevision()` callsite and its exact ordering relative to mutations/A4/persistence;
5. every session `gameState` read/write/update path;
6. Recovery restore/serialization mechanics;
7. A4/recommendation/Judge consumers that need actual mechanical state versus revision identity only.

Build a responsibility matrix:

```text
mechanical responsibility
-> owned state
-> mutation entry points
-> revision boundary
-> side effects / durability
-> downstream readers
-> current behavioral tests
-> candidate session API / owner
```

### Candidate ranking

Rank each candidate by:

- ownership cohesion;
- ability to move complete state + writer authority together;
- real reduction of root authority;
- test coverage / characterization quality;
- Android/Compose coupling;
- semantic blast radius.

Choose the **smallest high-confidence complete ownership slice**, not the largest block or easiest line-count reduction.

### Implementation rule

For a behavior-preserving cutover:

1. identify owning focused tests and baseline them;
2. add a typed RED only if the source audit exposes a real uncovered invariant;
3. add/adjust the smallest session-side API required by the chosen slice;
4. use fail-closed large-file patching for the giant App root when needed;
5. no intermediate committed state may have two writable canonical owners;
6. preserve exact revision and side-effect ordering;
7. rerun focused evidence + `:app:testFast` + affected T2/R2 as required;
8. exact diff and writer/ownership audit before declaring the slice complete.

### D6.1d non-goals

Do not:

- rewrite every day/night mechanic together;
- claim session `gameState` is globally canonical before all audited writers for the chosen slice are moved;
- change gameplay semantics;
- change Recovery v2 schema/current-version-only policy;
- reopen persistence trigger topology;
- create synthetic NGJ `RulesetRef`;
- add Compose dependency to session/domain;
- introduce a broad Manager/Controller abstraction;
- touch Undercover/Werewolf;
- do opportunistic large-file cleanup;
- optimize primarily for the 50 KiB target;
- merge PR #113.

## D6.1e — later acceptance/cleanup

After canonical mechanical ownership is stable enough:

- remove obsolete compatibility APIs only if no production caller remains;
- re-audit remaining App-root writers;
- focused + FAST + affected T2;
- one reserved `[full-ci]` T4 at the D6.1 logical acceptance checkpoint.

## Invariants

Preserve all:

1. same game ID/seed/script behavior;
2. no fake NGJ `RulesetRef`;
3. exact revision cadence/order;
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
14. no intended gameplay/user-visible semantic change.

## Suggested continuation prompt

```text
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md、docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md、docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md、docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md 和当前 D6 handoff。

重新确认 live main、codex/d6-root-reaudit、draft PR #113 和最新 checks。D6.1c 已完成；继续 D6.1d 时先做 canonical GameState/source writer 审计：映射 App-root mechanical state、所有 cards mutation、cards.toClocktowerGameState、advanceClocktowerGameStateRevision 调用、session gameState update/read，以及 Recovery/A4/recommendation/Judge 消费关系。先排名最小高置信完整 ownership slice，再决定实现，不要机械替换或一次重写 day/night mechanics。保持 Recovery v2、NGJ null RulesetRef、revision cadence、A4/persistence ordering 和 gameplay semantics 不变。按 risk-based tests-first/characterization + fail-closed large-file workflow 实施，完成 focused + FAST + affected T2/R2 + exact ownership audit 后停止，不要 merge PR。
```
