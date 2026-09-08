# CampBoardGameHost 文档入口

> 最后整理：2026-09-08 Australia/Sydney  
> 目标：新的开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不参与当前决策。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — **项目级 AI / architecture / test / Git 执行规范**；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行优先级权威**；
3. [`D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`](D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md) — **当前 D6 ownership 决策与 D6.1 边界**；
4. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md) — **唯一当前 active handoff**；
5. 当前任务需要的 specialized semantic / product design；
6. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
7. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或旧文档中的 `PASS / COMPLETE / READY / NEXT` 字样推断当前状态。

## 2. 当前状态与执行顺序

当前 live `main` 为 Persistence Simplification / PR #112 合并后的 docs/CI descendant。PS0–PS5 已全部完成；D6.0 post-persistence App/root responsibility audit 也已完成，当前优先级是：

```text
D6.1 ClocktowerGameSession production authority cutover
-> remaining D6 ownership decomposition after re-audit
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

A4/ZDD remains non-production。

当前 D6 原则：

```text
ownership first
-> complete existing owners before inventing new managers
-> cohesive state + behavior
-> explicit dependency direction
-> smaller App/root as a consequence
```

D6.0 已决定：第一优先不是继续机械拆 UI/大文件，而是完成现有 `ClocktowerGameSession` 对 `GameSnapshot` canonical session/history state 的 production ownership cutover。

## 3. 当前 D6 references

- [`D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`](D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md) — 当前 responsibility map、owner ranking、D6.1 contract；
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md) — 当前 D6 continuation contract；
- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — 当前优先级、前置完成状态和 guardrails；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — risk-based T0–T4 validation；
- live production code — App/root 与 extracted owners 的最终事实来源。

Persistence Simplification 的 audit、handoff、PS1–PS5 checkpoint 已完成，统一移入：

- [`archive/handoffs/`](archive/handoffs/)
- [`archive/checkpoints/`](archive/checkpoints/)

其中 persistence 历史审计保留为：

`archive/checkpoints/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

PS5 历史完成记录保留为：

`archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

长期 completed-campaign 汇总历史现保留为：

`archive/COMPLETED_DEVELOPMENT_HISTORY.md`

## 4. Product / UI / epistemic 长期参考

UI / information design：

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)

Future misinformation-quality / epistemic design：

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)
- [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md)
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md)

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

## 5. Normative engineering workflow

Current engineering authorities：

- root `AGENTS.md` — first execution/architecture authority；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md)；
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md)。

The older single-developer connector workflow and Git Data large-file playbook are archived under `archive/workflows/` because their writer-priority rules are superseded by current `AGENTS.md`.

## 6. Other long-lived / future architecture references

These remain in docs root because they describe reusable contracts or intentionally deferred future architecture, not completed execution checkpoints：

- `TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`
- `TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`
- `storyteller_revision_driven_dynamic_decision_engine_plan.md`
- `unified_semantic_model.md`
- `多剧本多板子与动态游戏流程架构设计_v1.md`
- `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`
- `FIELD_TEST_APK_DISTRIBUTION.md`
- `CI_DOCS_ONLY_MAIN_PUSH_POLICY_2026-09-08.md`

Their presence in root does not make them current execution priority；roadmap + active D6 documents control current work。

## 7. Archive layout

Historical evidence belongs under [`archive/`](archive/README.md)：

```text
archive/handoffs/     closed/superseded NEXT handoffs
archive/checkpoints/  completed implementation/test/audit/checkpoint records
archive/ui/           superseded UI plans / closeout evidence
archive/deferred/     unfinished but explicitly deferred future work
archive/workflows/    superseded workflow guidance
archive/              consolidated historical reports / campaign history / lessons
```

Files under `archive/` are evidence and traceability sources, not current execution authority。

## 8. Status authority rule

If documents disagree：

1. official Blood on the Clocktower rules/rulings control gameplay correctness；
2. root `AGENTS.md` controls project execution and architecture/test rules；
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state and priority；
4. `D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md` controls the current D6 ownership decision；
5. the one active D6 handoff controls the approved narrow continuation plan；
6. specialized design docs control their own semantic/product domain where non-conflicting；
7. archive documents, old Git branches and historical PR records are evidence only。
