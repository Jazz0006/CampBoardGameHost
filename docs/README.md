# CampBoardGameHost 文档入口

> 最后整理：2026-08-28 Australia/Sydney  
> 目标：让新的开发会话只读少量真正有权威性的文档，不再被历史 handoff / checkpoint 淹没。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — 项目级 AI / Git / 测试执行规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态权威**；
3. 当前 active handoff；
4. 当前任务需要的 specialized design / semantic reference；
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
6. 查询 live GitHub state 后再实施。

不要从旧 commit SHA、旧 PR 状态、archive 文档或历史 handoff 推断当前状态。

## 2. 当前 active task

当前最高优先级是：

```text
GCR — Global Correctness Review Follow-up
```

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-28_GLOBAL_CORRECTNESS_FOLLOWUP.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-28_GLOBAL_CORRECTNESS_FOLLOWUP.md) — **CURRENT HANDOFF**

当前 merge-blocking correctness items：

```text
GCR-1  Current Demon authority / cross-night Imp succession
GCR-2  Poisoned Spy information integrity / no impairment side channel
```

随后才处理：

```text
GCR-3  typed production acceptance + source-string retirement
GCR-4  Chambermaid actual wake-history authority
GCR-5  durable target identity / reconstruction API hardening
```

PR #54 仍应保持 draft/open/unmerged，除非明确授权改变状态。

## 3. Recently closed — SNE-7

Same-Night Effective Mechanical State / SNE-7 已经：

```text
CLOSED / BROAD GREEN
```

Latest accepted full checkpoint：

```text
70935644daf5c06985420f19833dbda3a160bbfa
```

Later docs-only SNE closeout：

```text
83bafdeef2e8445ee6ef92a3e247d63fdf4b58ce
```

SNE-7 的旧执行 handoff 与 micro-checkpoint 已从 active docs root 移除/收口。历史索引见：

- [`archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md`](archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md)

不要为了 GCR 新问题而重新执行旧 SNE handoff。GCR 是新的 follow-up campaign。

## 4. Active long-lived architecture / semantic references

这些文档保留在 active docs root，因为它们定义长期 contract，而不是临时分支状态：

### Same-night / night transaction

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md) — same-night cursor/effective-state architecture；
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md) — accepted design decisions；
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md) — authoritative transaction boundary；
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md) — reusable engineering lessons；
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md) — active test-debt / retirement policy.

### Information / epistemic architecture

- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md) — impaired information + Storyteller decision authority；
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — Possible Worlds / epistemic architecture；
- [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md) — chronology / knowledge timeline；
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — semantic reference matrix；
- [`unified_semantic_model.md`](unified_semantic_model.md) — unified semantic model.

### Future architecture references

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md) — dynamic decision engine；
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md) and [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md) — A4/ZDD reference；
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md) — multi-script / dynamic-flow architecture；
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md), [`external_solver_evaluation.md`](external_solver_evaluation.md) — solver/reference validation.

These documents may describe older milestones, but remain because their primary role is semantic/design authority, not current status.

## 5. Deferred unfinished work

Old `NEXT_DEVELOPMENT_HANDOFF_*` files that contain unfinished future work are no longer allowed to sit beside the active handoff in the docs root.

They are archived under `archive/deferred/` and may only be consulted after the roadmap explicitly reactivates that scope:

- [`archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md`](archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md) — immutable setup-snapshot ownership/persistence；
- [`archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md`](archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md) — S9.2 Active Game Persistence Boundary.

A deferred handoff is context only. Before resuming it, re-audit live `main` and current architecture.

## 6. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 test strategy；
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md) — current AI development workflow；
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — GitHub connector workflow；
- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — large-file local/Luna workflow；
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — connector large-file constraints/playbook.

## 7. Documentation lifecycle policy

Active docs root should contain only:

```text
CURRENT_DEVELOPMENT_ROADMAP.md
one active NEXT_DEVELOPMENT_HANDOFF_*.md
long-lived architecture / semantic references
normative test / workflow references
```

When a task closes:

- completed execution handoffs and micro-checkpoints should leave the active root;
- if only historical traceability remains, consolidate them into an archive closeout/index and rely on Git/PR history for exact old content;
- if the document carries genuinely unfinished future work, move it intact under `archive/deferred/`;
- do not create a new handoff for every GREEN micro-step;
- update the existing active handoff and roadmap instead.

## 8. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state;
4. the one active task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. archive / historical Git / old PR records are evidence, not current instructions.
