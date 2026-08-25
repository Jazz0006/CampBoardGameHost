# CampBoardGameHost 文档入口

> 最后整理：2026-08-25
> 目标：让新的开发会话只读少量真正有权威性的文档，不再被历史 handoff/closeout 淹没。

## 1. 新任务的默认阅读顺序

1. 根目录 `AGENTS.md` — 项目级 AI / Git / 测试执行规范；
2. `CURRENT_DEVELOPMENT_ROADMAP.md` — **唯一当前状态权威**；
3. 当前任务对应的 handoff；
4. 当前任务需要的 specialized design / reference；
5. `TESTING_STRATEGY.md`；
6. 查询 live GitHub state 后再实施。

不要从旧 commit SHA、旧 PR 状态或历史 handoff 推断当前状态。

## 2. 当前任务

当前最高优先级是 Clocktower **Same-Night Effective Mechanical State correctness**。

核心问题不是单个 Empath 公式，而是：一个夜间行动已经确认并产生机械结果后，后续角色是否读取**同一夜当前时间点的有效机械状态**。

当前缺失的统一边界会影响：

```text
Imp 后的机械死亡
later actor eligibility
Empath living neighbours
Chambermaid target legality
Poisoner persistent-effect lifetime
death-trigger exceptions
role changes / Imp succession
information truth / InformationDecisionContext input
restore/recomposition determinism
```

当前 handoff / specialized design：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-25_SAME_NIGHT_EFFECTIVE_STATE_CORRECTNESS.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-25_SAME_NIGHT_EFFECTIVE_STATE_CORRECTNESS.md) — **CURRENT CORRECTNESS HANDOFF**
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md) — external-project research + official semantics + effective-state / dynamic-script / dynamic-character architecture authority
- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md) — protected information legality / impairment / Storyteller decision authority

Current stable baseline at task creation:

```text
main = c8985cb4991f6c7e5ea02adedb932d2d86452da1
PR #53 = MERGED / Information Decision confirmation authority fixed
branch = codex/clocktower-same-night-effective-state-correctness
```

Always re-query before implementation.

The next checkpoint is **RED-only**: establish pure/cursor-relative same-night state failures before writing GREEN production code.

## 3. Recently closed correctness work

The previous Information Decision production authority bug is closed by PR #53.

Its protected result remains:

```text
rules-owned legal candidates
-> InformationDecisionContext
-> MANUAL / RECOMMENDATION_ACCEPTED confirmation
-> ConfirmedInformationDecision
-> authorized player display / durable observation
```

The old handoff:

- `NEXT_DEVELOPMENT_HANDOFF_2026-08-25_INFORMATION_DECISION_CORRECTNESS_BUG.md`

is historical after PR #53 and must not override the current same-night handoff.

## 4. Intentionally deferred handoffs

These are **not current work**, but still contain unfinished future architecture and therefore remain in the repository.

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md) — S9.2 Active Game Persistence Boundary; architecture audit complete, implementation deferred until same-night correctness is resolved.
- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md) — historical exact H1–H7 is complete; only immutable setup-snapshot ownership/persistence remains deferred/not started.

A deferred handoff must never override `CURRENT_DEVELOPMENT_ROADMAP.md`.

## 5. Long-lived design / semantic references

Keep these because they define architecture or semantics rather than a temporary branch state:

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md) — same-night time/cursor state, mechanical-vs-public truth, persistent-effect lifetime and dynamic-role/script design;
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — Possible Worlds / epistemic architecture;
- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md) — impaired information + Storyteller decision authority;
- [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md) — chronology / knowledge timeline semantics;
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — reference matrix;
- [`unified_semantic_model.md`](unified_semantic_model.md) — unified semantic model;
- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md) — revision-driven dynamic-decision architecture;
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md) and [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md) — A4/ZDD design/spec;
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md) — multi-script / dynamic-flow architecture;
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md), [`external_solver_evaluation.md`](external_solver_evaluation.md) — solver/reference validation.

These documents may describe earlier milestones, but they are kept because their primary role is semantic/design reference, not “what branch do I work on next?”.

## 6. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 test strategy;
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — GitHub connector workflow;
- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — large-file local/Luna workflow;
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — connector large-file constraints/playbook.

## 7. Historical documentation policy

The repository previously accumulated many date-stamped files after each slice:

```text
NEXT_DEVELOPMENT_HANDOFF_...
*_closeout_...
*_entry_audit_...
merge-preflight notes
```

Once a task is merged/closed and no unfinished future decision depends on that handoff, those files should be removed from the active docs root. Their history remains available through Git and the merged PR.

Do not create a new handoff for every GREEN micro-step. Prefer updating:

```text
CURRENT_DEVELOPMENT_ROADMAP.md
+ one active task handoff
+ one specialized design document when a durable architecture decision is needed
```

Keep a deferred handoff only when it carries genuinely unfinished future work that would be expensive or risky to reconstruct.

## 8. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state;
4. the current task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. historical Git/PR records are evidence, not current instructions.
