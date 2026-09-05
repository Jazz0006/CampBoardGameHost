# CampBoardGameHost 文档入口

> 最后整理：2026-09-05 Australia/Sydney  
> 目标：新的开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认进入 archive，不参与当前决策。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — **项目级 AI / architecture / test-first / Git 执行规范**；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行优先级权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md) — **唯一当前 active handoff**；
4. [`CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`](CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md) — 当前 Night Step ownership decomposition reference；
5. 当前任务需要的 specialized semantic / product design；
6. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
7. 查询 live GitHub state 后再实施。

不要从 archive 中的旧 SHA、旧 PR、旧 branch、`PASS/READY/NEXT` 字样推断当前状态。

## 2. 当前状态

2026-09-05 已集成的重要节点：

```text
PR #99  R4D-6 Host Table preserved-lineage integration
PR #100 UI-N1 inline actor/wake cue + square-table readability
PR #101 same-night dead-role wake-step fix
PR #102 Manual Demon bluff consistency fix
```

创建本次文档 checkpoint 时 live `main`：

```text
6d787172d4084e0af9ab74cb35e06f492cbb19fd
```

PR #100 的最终产品决定取代旧 UI-N1 handoff 中“独立 WAKE acknowledgement state”的设计：当前 actor/wake cue 与 target selection 共存于同一 persistent table。

## 3. 当前执行顺序

```text
architecture/test policy + docs hardening
-> fresh Night Step UI cluster ownership audit
-> first behavior-preserving decomposition slice
-> UI-R5 real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

Night Step decomposition 不是机械降文件大小。主要指标是 **ownership clarity + change context radius**。

## 4. 当前 architecture / UI references

- [`CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`](CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md) — pre-decomposition ownership map、existing seams、candidate slices、test implications；
- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md) — 信息展示 / Manual full-screen 产品方向；
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md) — recommendation / Manual UX 产品决策；
- persistent Host Table / inline actor-cue 的当前实现以 live code + PR #99/#100 后 `main` 为准。

## 5. Epistemic / rules 长期参考

Future misinformation-quality design：

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)

Same-night / rules architecture：

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md)

Frozen high-level semantic ordering remains：

```text
actual identity
-> committed shown identity
-> perceived ability
-> complete legal/truth semantic domain
-> reliability / impairment
-> recommendation/manual decision
-> AbilityObservation
-> durable player-visible history
-> UI
```

## 6. Normative engineering workflow

- root `AGENTS.md` — first authority;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md);
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md);
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md).

`AGENTS.md` now treats architecture pre-flight and test-first as one decision process: classify the change, identify ownership/durable contract, then choose the cheapest reliable evidence. A new production edit does not automatically imply a new RED.

## 7. Other active/future architecture references

These remain in docs root because they describe long-lived contracts, future designs, or reusable engineering decisions rather than a completed checkpoint:

- `TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`
- `TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`
- `storyteller_revision_driven_dynamic_decision_engine_plan.md`
- `unified_semantic_model.md`
- `epistemic_reference_matrix.md`
- `asp_oracle_cross_validation.md`
- `external_solver_evaluation.md`
- `多剧本多板子与动态游戏流程架构设计_v1.md`
- `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`

Their presence in root does not make them the current execution priority; current priority is controlled by the roadmap + active handoff.

## 8. Archive layout

Historical evidence belongs under [`archive/`](archive/README.md):

```text
archive/handoffs/     closed/superseded NEXT handoffs
archive/checkpoints/  completed implementation/test/checkpoint records
archive/ui/           superseded UI plans / closeout evidence
archive/deferred/     unfinished but explicitly deferred future work
archive/workflows/    superseded workflow guidance
```

The repository may retain consolidated history/index docs in root where they provide navigation value, but individual completed checkpoint files should not crowd the active-doc surface.

## 9. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution and architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state and priority;
4. the one active handoff controls the approved narrow next-campaign plan;
5. specialized design docs control their own semantic/product domain where non-conflicting;
6. archive documents, old Git branches and historical PR records are evidence only.
