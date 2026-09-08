# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Starting point: merged `main`
> Persistence Simplification merge commit: `1b502c75357a2de7c928c88668e6a9613521b4ac`
> Status: **READY FOR FRESH D6 AUDIT / PLANNING — NO D6 IMPLEMENTATION STARTED**

## Read first

Treat these as authority before doing D6 work:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. this handoff
5. `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` only when persistence-history context is needed

Before any write, re-confirm live `main`. Documentation-only commits may exist after the PS5 merge commit above.

## Completed predecessor

Persistence Simplification / PS5 is fully complete:

- PR #112 merged / closed;
- automated acceptance complete;
- renewed post-hotfix full T4 complete;
- real-device acceptance tests 1–5 complete;
- recovered 6-player No Greater Joy -> quick restart crash fixed and retested successfully.

Latest PS5 production GREEN inside merged history:

```text
e622960a9c75c0b110d2c92e34b46828cb949e0a
fix: restore seating owner after recovery
```

Renewed post-hotfix T4:

```text
CI 34191738571 — PASS
R2 34191738649 — PASS
```

Final pre-merge docs checks:

```text
CI 34192745929 — PASS
R2 34192745940 — PASS
```

## Why D6 must be re-audited

Do not mechanically resume the old D6 plan. Persistence Simplification changed or clarified several ownership boundaries that directly affect decomposition decisions:

- Recovery is a current-version-only emergency continuity contract, not long-lived Save Game;
- Archive is separate from Recovery;
- `RecoveryWriteGate` owns semantic duplicate suppression / retry state;
- lifecycle persistence is `ON_PAUSE force=true`, `ON_STOP force=false`;
- Compose `SideEffect` remains the generic ordinary persistence opportunity by explicit architecture decision;
- A4 durability ordering must not be broken;
- Recovery restore reconstructs `HostSeatingSetupFlow` ownership for an active recovered game;
- session/restart/recovery ownership must remain consistent across Undercover, Werewolf and Clocktower.

Any D6 extraction that moves state, callbacks, persistence/recovery wiring or session ownership must preserve those contracts.

## D6 goal

Perform a **fresh ownership-first decomposition audit** of the merged application architecture, then produce the best new D6 plan.

The goal is not merely “make files smaller”. The plan should reduce coupling, improve ownership clarity and make future AI/human edits cheaper without creating cross-file hidden state or callback spaghetti.

## Required audit scope

At minimum inspect:

1. `CampBoardGameHostApp.kt`
   - current size / responsibility clusters;
   - mutable state ownership;
   - game-start / restart / recovery entry points;
   - persistence trigger/wiring;
   - Clocktower flow orchestration;
   - UI/composable ownership still mixed with domain/session logic.

2. Already extracted owners around:
   - Recovery / persistence;
   - Archive;
   - Host seating/setup flow;
   - Clocktower flow/session helpers;
   - A4 durability/cache lifecycle;
   - recommendation / algorithm coordination.

3. Cross-boundary coupling:
   - callbacks into the App root;
   - shared mutable collections/state;
   - hidden ordering constraints;
   - functions that are only file-local because they capture many App locals;
   - state clusters that should become explicit owner objects/state holders instead of utility functions.

4. Tests and CI ownership:
   - identify characterization tests that already protect extraction boundaries;
   - identify missing REDs required before moving a responsibility;
   - preserve FAST vs full-T4 strategy from `docs/TESTING_STRATEGY.md`.

## Audit principles

Use mature decomposition criteria rather than a raw line-count target:

- one reason to change per owner;
- explicit state ownership;
- minimal bidirectional callbacks;
- dependency direction should be obvious and preferably one-way;
- pure/domain logic should not depend on Compose/Android lifecycle when avoidable;
- persistence contracts should be injected/called through explicit boundaries rather than globally reachable state;
- prefer extracting cohesive state + behavior together over moving isolated functions;
- avoid “manager” objects that merely mirror the giant App with dozens of callbacks;
- preserve fail-closed invariants at production boundaries;
- do not change user-visible behavior during structural D6 slices unless separately authorized.

File size remains a useful signal, especially for AI edit cost, but it is secondary to ownership quality.

## Deliverable before implementation

Produce a D6 plan that includes:

1. current responsibility map of the remaining large App/root file;
2. proposed target owners/modules;
3. dependency graph / extraction order;
4. each slice's exact behavioral invariants;
5. tests-first RED/characterization strategy for each risky slice;
6. expected changed-file allowlist per slice where practical;
7. rollback/checkpoint strategy;
8. FAST / R2 / full-T4 cadence;
9. explicit non-goals to stop D6 expanding into unrelated product work;
10. whether the old D6 slices should be retained, reordered, replaced or dropped.

Do **not** begin production extraction until this audit/plan has been reviewed and accepted.

## Guardrails carried forward

- Reconfirm remote/live branch head before every write sequence.
- Behavior changes require tests-first RED.
- Structural extraction should preserve exact behavior; characterization tests are appropriate.
- Run focused tests first, then `:app:testFast` at logical GREEN checkpoints.
- Use R2 for main-thread / structural boundary validation where applicable.
- Reserve full T4 for campaign/slice acceptance according to `docs/TESTING_STRATEGY.md` rather than running it after every tiny edit.
- Run `git diff --check` and exact changed-file/reference audits.
- Treat giant-file edits fail-closed: use precise local/workflow patches rather than broad blind rewrites.
- Do not mix D6 with Recovery redesign, cross-version migration, Archive redesign, Werewolf removal, A4/ZDD feature rollout, unrelated UI changes or DataStore modernization.

## Suggested new-conversation prompt

```text
请读取根目录 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md。

先重新确认 live main 和当前 checks。Persistence Simplification / PR #112 已完成并合并，不要继续 PS5，也不要直接沿用旧 D6 方案。

请从合并后的真实架构重新审计 CampBoardGameHostApp.kt 及相关 owner，按 ownership / coupling / dependency direction / testability 重新设计 D6。先只做审计和实施路线，不要开始 production extraction。最后给出你认为最佳的 D6 拆分顺序、每一步的 tests-first 边界、风险和验收门。
```
