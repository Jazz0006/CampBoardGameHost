# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 live `main`：`efd63b360ca9aba8c7890594449aa5e21817f560`（PR #44 merge）  
> 当前工作：**PR #43 Clocktower host source decomposition — implementation complete, final review next**  
> 当前 validated implementation head：`b37f0067b674a0cd4bee5ff311840d1c52ce8c05`  
> 当前执行点：**A1–A13 完成；post-A13 audit 决定不实施 optional A14；下一步 PR #43 final review / merge-readiness audit**  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md`

> 新会话实施前必须重新查询 live `main`、PR #43、feature head 和 checks，不得把本文 SHA 当作永久 HEAD。

## 1. 当前状态

```text
Phase A correctness foundation                         PASS
R5.5 Script & Dynamic Flow Foundation                  CLOSED / MERGED
R6 semantic prerequisites                              CLOSED
PR #39 Storyteller Information Decision Foundation     CLOSED / MERGED
PR #40 Structured Manual UI — Empath numeric slice     CLOSED / MERGED
PR #41 developer workflow + LF policy                  CLOSED / MERGED
PR #42 Historical Action + Observation Capture         CLOSED / MERGED
PR #44 Drunk / Fortune Teller setup correctness hotfix CLOSED / MERGED
PR #43 Clocktower host source decomposition            DRAFT / A1–A13 GREEN
Current implementation point                           FINAL REVIEW / MERGE-READINESS AUDIT
Optional A14 day-routing extraction                    SKIPPED AFTER POST-A13 AUDIT
Next product source slice after PR #43                 A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

当前 live `main`：

```text
efd63b360ca9aba8c7890594449aa5e21817f560
```

PR #43 当前 validated implementation head：

```text
b37f0067b674a0cd4bee5ff311840d1c52ce8c05
```

A13 remote gates：

```text
CI #534:                  SUCCESS
R2 #473:                  SUCCESS
Android unit/build:       SUCCESS
ASP contract:             SUCCESS
Real Clingo:              SUCCESS
remote exact-diff audit:  PASS
```

PR #43 保持 Draft / Open / Not merged。未经用户明确授权不得 merge 或 mark ready。

## 2. PR #43 目标与完成标准

PR #43 是 structural refactor only：

- 保留 Blood on the Clocktower 规则语义和 precedence；
- 保留 recommendation ranking / selection ordering；
- 保留 registration 与 impairment semantics；
- 保留 persistence / history identity；
- 保留 `ClocktowerGameSession` global timeline authority；
- 保留 Compose state/effect lifetime；
- 保留 callback / audit / commit transaction ordering；
- 让 planner / projector 成为夜间 interaction existence/order authority；
- 让 `ClocktowerJudgeScreen` 收敛为 coordinator/orchestrator，而不是为了字节数机械拆分。

约 50 KiB 是 **soft maintainability guideline**，不是 merge gate。禁止为了达到数字目标制造巨型参数袋、弱 owner、额外 `internal` 泄漏或 lifetime/transaction 风险。

## 3. PR #43 已完成 slices

```text
A1   ClocktowerHostCoreSemantics.kt
A2   ClocktowerHostSelectionSemantics.kt
A3   ClocktowerHostPresentationModels.kt
A4   ClocktowerStorytellerRecommendationUi.kt — recommendation screen/reason
A5   ClocktowerStorytellerRecommendationUi.kt — card/editor
A6   ClocktowerPlayerDisplayUi.kt
A7   ClocktowerRegistrationUi.kt
A8   ClocktowerNightStepUi.kt
A9   unreachable legacy fallback cleanup
A10  ClocktowerInformationStepBuilder.kt
A11  ClocktowerNightStepMaterializerRegistry + canonical interactions() seams
A11.1 integrate PR #44 actual-role vs waking-role correctness baseline
A12  planner-first First Night lazy materialization
A13  planner-first Other Night lazy materialization
```

### Key evidence

```text
A9 RED                               3ecbcadbd728ac83f7ab1f8d1d40175795e44078
A9 GREEN                             00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
A10 RED                              3377fdbea83727a797afce28064b924a074df5c3
A10 GREEN                            363629ed45f0f044da021f77bb52c5c3ff3c9e20
A11 RED                              19bdaf5525c4979f36d44ed0213c0b3c60f4ff7d
A11 GREEN                            c893300b8d8dbc7ea845849b81416259da32d485
A11.1 integration                    4eaa9863070b1eee571169bde737b249379e28ee
A12 RED                              43f64fc6b2123a35bd9e89b3f6120a8adb7ec809
A12 legacy contract migration        3715c5428b52bcce87781fb48ab715338227e19f
A12 GREEN                            854c2464d8a742ba0438fa700bdd2848aa88f4cf
A13 RED                              4e638c345ed50a3bc65abdc22ac5487172bf9f32
A13 New Demon contract migration     bae29d5fccb988f641a95e743f899be56ae84299
A13 GREEN                            b37f0067b674a0cd4bee5ff311840d1c52ce8c05
A13 CI / R2                          #534 SUCCESS / #473 SUCCESS
```

## 4. Night-flow architecture after A13

Both First Night and Other Night now use the same authority shape:

```text
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteractionProjector
        ↓
stable / conditional ClocktowerHostInteraction
        ↓
ClocktowerProductionFirstNightFlow.interactions(...)
        or
ClocktowerProductionOtherNightFlow.interactions(...)
        ↓
ClocktowerNightStepMaterializerRegistry(FIRST_NIGHT / OTHER_NIGHT)
        ↓
lazy materialize only projected actionable interactions
        ↓
ClocktowerNightStepUi
```

Planner / projector owns **what exists and canonical order**. Materializer registry owns **stable interaction identity -> lazy UI step representation**. Host owns **Compose lifetime, derived orchestration state and transaction ordering**.

### First Night protected identity split

```text
firstNightWakingRoleIds
  = actual roles + Drunk shown role

firstNightActualRoleIds
  = actual roles only
```

Projector only treats actual roles as setup ability identity while allowing Drunk to wake/act in the shown role slot.

### Other Night A13 stable materializers

Role identities:

```text
Poisoner
Butler
Empath
Chambermaid
Fortune Teller
Undertaker
Monk
Imp
Sage
Ravenkeeper
Spy
```

Event identities:

```text
new Demon identity
demon succession
Mayor redirect
```

Conditional/event existence remains projector/planner-authoritative through existing resolved facts. Lazy builders may use `requireNotNull` only as fail-closed invariant checks after an interaction has been projected; they do not form a second existence filter.

## 5. Protected Host ownership after A13

These stay in `ClocktowerJudgeScreen` unless a future product slice supplies a stronger natural owner:

- Compose `remember` state and `LaunchedEffect` lifetime；
- recommendation coordinator and telemetry recorder lifetime；
- Spy/Recluse mutable registration state/maps；
- derived recommendation/information facts tightly coupled to the current Compose snapshot；
- first-night information migration lifecycle；
- player display / information observation ordering；
- history/session authority integration；
- phase routing and state transitions；
- stateful day-action transactions；
- `advanceNightStep` transaction。

`advanceNightStep` remains Host-owned with the established ordering:

```text
confirm poison / monk / demon draft
-> Mayor redirect audit + confirm
-> Demon successor audit
-> Spy registration recording
-> Recluse registration recording
-> semantic night-step event recording
-> step index advance OR onConfirmNight()
```

A13 added a characterization contract specifically to prevent accidental relocation/reordering.

## 6. Post-A13 size and cohesion audit

At validated A13 GREEN head:

```text
ClocktowerHostScreen.kt     295,644 bytes
ClocktowerDayScreen.kt       63,135 bytes
ClocktowerNightStepUi.kt     45,251 bytes
ClocktowerHistoryScreen.kt   38,365 bytes
ClocktowerNightScreen.kt     17,833 bytes
ClocktowerSetupScreen.kt     17,362 bytes
```

Representative extracted owners outside `clocktower/ui`:

```text
ClocktowerHostCoreSemantics.kt              3,660 bytes
ClocktowerHostPresentationModels.kt         5,172 bytes
ClocktowerHostSelectionSemantics.kt         6,035 bytes
ClocktowerInformationStepBuilder.kt         9,095 bytes
ClocktowerNightStepMaterializerRegistry.kt  2,539 bytes
ClocktowerPlayerDisplayUi.kt                16,688 bytes
```

Host remains large because it still contains high-coupling orchestration, recommendation-derived facts, registration state and lazy night materializer closures. A13 intentionally changed authority rather than shrinking bytes.

### A14 decision — SKIP

Roadmap previously left A14 as optional clean day routing, prioritizing `Overview`, `Vote`, and `EndConfirm`.

Post-A13 source audit shows:

- `ClocktowerDayOverviewScreen`, `ClocktowerVoteScreen`, and `ClocktowerExecutionConfirmScreen` already own the presentation layer;
- remaining Host code for those paths is primarily state reset/transition and callback transaction wiring;
- Nomination/Virgin depends on registration semantics and execution ordering;
- Slayer depends on Recluse registration recommendation state and resolution callbacks;
- Artist depends on unreliable-information recommendation state and `LaunchedEffect` behavior;
- Klutz depends on Spy registration state and confirmation ordering.

Extracting the thin simple routes would save little and introduce another coordinator/context layer. Extracting the complex routes would require exposing many state/recommendation/registration internals or moving transaction ownership. Both are architecture-negative relative to current goals.

Therefore:

```text
A14 = NOT IMPLEMENTED
reason = no remaining high-value low-coupling owner
```

This is a positive decomposition stop condition, not an incomplete task. The 50 KiB guideline is intentionally not used as a reason to continue.

## 7. PR #43 final review / merge-readiness audit — NEXT

Before any merge authorization:

1. Re-query live `main`, PR #43/head, mergeability and latest checks.
2. Review complete changed-file list for scope drift.
3. Review unresolved PR review threads/comments if any.
4. Audit key protected invariants across the final PR diff:
   - rules / recommendation ordering;
   - registration + impairment semantics;
   - persistence/history identity;
   - Compose state/effect lifetime;
   - First/Other Night planner authority;
   - `advanceNightStep` and day transaction ordering.
5. Confirm no temporary debug/build artifacts or `.gradle-codex/` were committed.
6. Confirm docs accurately describe the final architecture and stop decision.
7. If final review is clean, report merge readiness to the user.
8. **Do not mark ready or merge until the user explicitly authorizes it.**

## 8. Product work after PR #43

Only after PR #43 has passed final review and is explicitly authorized/merged should product development continue to:

# A3 historical multi-night exact baseline

Target: use `EnumeratedWorldSet` to establish an exact multi-night historical reconstruction correctness baseline.

Do not mix into PR #43:

- A3 historical product behavior;
- B4 / ZDD production promotion;
- history UI redesign;
- misinformation expansion;
- broader manual Storyteller UI rollout.

## 9. Long-term architecture boundaries

### Registration and impairment

```text
actual world
  -> registration projection
  -> truthful result / legal information space
  -> impairment policy
  -> storyteller decision
```

### Session authority

UI, recommendation, manual selector and history adapters must not allocate Global timeline identity. `ClocktowerGameSession` remains the authority for global identity / sequence.

### Host / flow / materialization

```text
ClocktowerJudgeScreen
  -> Compose state/effect lifetime
  -> current snapshot / derived orchestration state
  -> setup recommendation lifecycle
  -> registration mutable state
  -> phase routing / state transitions
  -> commit / callback ordering

ClocktowerFlowPlanner + ClocktowerHostInteractionProjector
  -> interaction existence
  -> canonical order
  -> conditional/event interactions

ClocktowerNightStepMaterializerRegistry
  -> stable interaction identity -> lazy step representation
  -> no Compose state/effect ownership

ClocktowerInformationStepBuilder
  -> generic reliable/unreliable information-step packaging

Day UI owners
  -> presentation screens; Host retains stateful routing transactions
```

Key rule:

> Planner decides **what / when**; materializer decides **how the requested interaction becomes a production step**; Host retains **state lifetime / commit ordering**.

## 10. Development workflow and CI

Project-level collaboration rules live in root `AGENTS.md`.

```text
ChatGPT / Chat
  -> live-state audit
  -> architecture / scope / risk decisions
  -> tests / characterization strategy
  -> implementation specification
  -> remote diff / CI / merge-gate review

GitHub connector
  -> preferred writer for safe small/medium edits

Codex / Luna
  -> constrained local implementation / validation executor for large mechanical edits
```

Mac / Codex Gradle:

```bash
GRADLE_USER_HOME="$PWD/.gradle-codex"
```

Keep `.gradle-codex/` untracked.

A pushed commit is never merge authorization.

## 11. New-conversation startup order

1. Read root `AGENTS.md`.
2. Read `docs/README.md`.
3. Read `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`.
4. Read `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`.
5. Read this file.
6. Read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md`.
7. Re-query live `main`, PR #43/head and latest checks.
8. Treat `b37f0067...` as the last validated implementation baseline, not a permanent branch head.
9. Perform PR #43 final review / merge-readiness audit; do not start A14.
10. Do not let Luna independently redesign decomposition boundaries.
11. Do not merge or mark ready without explicit user authorization.

## 12. Documentation maintenance rule

- This file is the current execution-point authority.
- `AGENTS.md` is the long-term AI collaboration and execution agreement.
- Handoff files serve continuation context.
- Specialized design docs maintain semantic boundaries, not live branch state.
- Historical audits must not override this roadmap.
- If a later decision reopens decomposition, it must identify a concrete high-value owner and justify why it does not move protected state/transaction lifetime merely to reduce file size.
