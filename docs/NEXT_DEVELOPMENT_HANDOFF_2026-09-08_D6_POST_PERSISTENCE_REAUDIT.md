# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Active branch: `codex/d6-root-reaudit`
> Draft PR: #113 `D6: Clocktower session authority cutover`
> Status: **D6.0–D6.1d COMPLETE — D6.1e T4 ACCEPTANCE NEXT — DO NOT MERGE YET**

## Read first

Treat these as authority before continuing D6:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
5. `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
6. `docs/D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`
7. `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`
8. `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`
9. `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`
10. this handoff
11. persistence archive docs only when historical persistence context is needed

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

## D6.1d — COMPLETE: canonical dynamic GameState writer ownership

`ClocktowerGameSession` is now the canonical writable owner for the dynamic domain mechanics already represented by `GameState`:

- actual/shown role identity;
- alive/death state;
- poison state;
- along with the D6.1c identity/revision/history/global-chronology subset.

App-root `PlayerCard` and flow variables remain necessary presentation/orchestration mirrors. Session mutation occurs first for canonical mechanics; UI mirrors follow.

Global writer/dependency audit:

```text
34221212685 — PASS
```

The audit also reran combined focused D6.1d contracts and `:app:testFast`, both PASS, and proved Recovery/rules/A4-epistemic/Werewolf production packages were not changed by the ownership cutover.

D6.1e compatibility/read-side callsite audit:

```text
34222474745 — PASS
```

It found no external production use of stateless session companion transitions or `updateGameState()`. Those APIs remain valid pure/session test contracts and should not be deleted merely for cleanup. It also found many legitimate `cards.toClocktowerGameState(...)` read/pre-session/recommendation/UI projections; do not mechanically replace them with session reads.

## D6.1e — NEXT: T4 acceptance

Before any new architecture slice:

1. finish comment/docs closeout only;
2. create a user-authored `[full-ci]` checkpoint commit;
3. verify full CI routing and all selected full-strength gates;
4. record exact T4 evidence;
5. re-confirm `main`, branch head and PR #113 checks;
6. stop for merge/readiness review — do not merge automatically.

No additional GameState production refactor is authorized before this acceptance gate.

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
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md、docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md 和当前 D6 handoff。重新确认 live main、codex/d6-root-reaudit、draft PR #113 和最新 checks。D6.1d 已完成并通过全局 writer/dependency audit；不要继续扩大 GameState/read-side 重构。继续 D6.1e：确认 acceptance closeout diff 只包含 ownership 注释/docs，然后创建用户侧 `[full-ci]` checkpoint，验证完整 T4（Android full + assemble + ASP + Real Clingo 等所有被 classifier 选中的 gate）。全部 GREEN 后记录 exact commit/run/checks，做 PR merge-readiness 审计，但不要自动 merge。
```
