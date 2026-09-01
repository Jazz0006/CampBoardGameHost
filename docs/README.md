# CampBoardGameHost 文档入口

> 最后整理：2026-09-01 Australia/Sydney  
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

```text
MS-SETUP — Generic Multi-Script Setup Architecture
MS-S6D — First-night Perceived-Ability Semantic Completion
当前阶段：S6D-6 production consistency / authority closeout
```

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md) — **CURRENT HANDOFF**

Current roadmap：

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — current branch/PR/checkpoint/stage authority.

Current next behavior target：

> 修复 pair-family 在 generic selector 之前被旧 `recommendPair` 预裁剪的问题，同时把完整 semantic domain 与 ASSISTED UI 的可见推荐列表分离，避免 UI 候选爆炸。

S7 remains blocked until S6D full acceptance.

## 3. Current S6D references

- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md) — current execution boundary and next RED；
- [`MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`](MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md) — historical S6D-0 audit evidence, **not current next-step authority**；
- [`MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`](MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md) — historical accepted S6C checkpoint evidence；
- [`MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`](MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md) — historical S6C planning evidence, superseded for current execution by the roadmap/handoff above.

Do not resume from the old 2026-08-31 MS-SETUP handoff. It has been retired because its “S6D-1 RED NEXT” instruction is no longer true.

## 4. Future epistemic / misinformation-quality design

The new long-lived design reference is:

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)

This is **DESIGN / DEFERRED**. It must not be implemented in PR #61.

It extends the player-cognition consistency direction from:

```text
Is the displayed answer true/false?
```

to:

```text
What mistaken player world does a legal false answer create?
Is it credible, sustainable, interactive, eventually breakable, and fair?
```

Its core principles include credibility, false-world persistence, breakability, cross-role interaction, social impact, Productive Uncertainty, avoiding direct Drunk exposure, avoiding confirmation locks, narrative value over degree of falsity, and player agency/fairness.

Related long-lived foundations:

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md)
- [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`unified_semantic_model.md`](unified_semantic_model.md)

## 5. Frozen information architecture

Current semantic authority:

```text
actual identity
-> committed shown identity
-> perceived ability
-> role semantic legal/truth space
-> RELIABLE / POISONED / DRUNK
-> generic selection
-> AbilityObservation
-> UI
```

Future quality ranking may only sit **after** legal semantic candidates exist. It must never mutate committed identity or replace official role/registration semantics.

## 6. Other active long-lived architecture references

### Same-night / night transaction

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md)
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md)

### Future architecture references

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md)
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md)
- [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md)
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)

## 7. Deferred unfinished work

Unfinished future work that is not currently active belongs under `archive/deferred/` and may only be resumed after the roadmap explicitly reactivates it.

Examples:

- `archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md`；
- `archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md`.

A deferred handoff is context only. Before resuming it, re-audit live `main` and current architecture.

## 8. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 test strategy；
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md) — current AI development workflow；
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) — normative large/truncated-file one-shot workflow + Python patch SOP；
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — GitHub connector workflow；
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — connector large-file constraints/playbook.

## 9. Documentation lifecycle policy

Active docs root should contain only:

```text
CURRENT_DEVELOPMENT_ROADMAP.md
one active NEXT_DEVELOPMENT_HANDOFF_*.md
long-lived architecture / semantic references
normative test / workflow references
limited historical checkpoint evidence that has not yet been consolidated
```

When a task closes:

- retire the old active handoff immediately when it gives a stale next step;
- preserve useful audit/checkpoint history as evidence, but mark it historical;
- consolidate or archive batches of micro-checkpoints at campaign/PR closeout instead of creating large documentation churn mid-PR;
- if a document carries genuinely unfinished future work, move it under `archive/deferred/`;
- update the existing roadmap + active handoff rather than creating a new handoff for every micro-GREEN.

For the current S6D closeout, only the stale 2026-08-31 active handoff is removed. Historical S6C/S6D audit evidence remains until PR #61 closeout, when a broader MS-SETUP archival cleanup can be done safely in one docs-only batch.

## 10. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state;
4. the one active task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. archive / historical Git / old PR records are evidence, not current instructions.
