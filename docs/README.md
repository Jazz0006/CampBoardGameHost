# CampBoardGameHost 文档入口

> 最后整理：2026-09-08 Australia/Sydney  
> 目标：新的开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不参与当前决策。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — **项目级 AI / architecture / test / Git 执行规范**；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行优先级权威**；
3. [`D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`](D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md) — **D6 ownership 基线与 D6.1 边界**；
4. [`D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`](D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md) — **最新完成的 D6.1c production ownership checkpoint**；
5. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md) — **唯一当前 active handoff，D6.1d continuation contract**；
6. 当前任务需要的 specialized semantic / product design；
7. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
8. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或旧文档中的 `PASS / COMPLETE / READY / NEXT` 字样推断当前状态。

## 2. 当前状态与执行顺序

当前 live `main` 为 Persistence Simplification / PR #112 合并后的 docs/CI descendant。PS0–PS5 已完成；D6.0、D6.1a、D6.1b、D6.1c 已完成。当前优先级：

```text
D6.1d canonical GameState ownership re-audit / bounded cutover
-> D6.1e D6.1 cleanup + acceptance
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

D6.1c 已将 identity/revision/semantic-history writable authority 切到唯一 `ClocktowerGameSession`；D6.1d 现在只处理仍留在 App-root `cards`/mechanics 的 canonical `GameState` ownership，不做 size-first 或全量 day/night mechanics 重写。

## 3. 当前 D6 references

- [`D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`](D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md) — responsibility map、owner ranking、D6.1 初始 contract；
- [`D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`](D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md) — production wiring / writer characterization；
- [`D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md`](D6_1B_SESSION_CORE_PROGRESS_2026-09-08.md) — production-compatible session core checkpoint；
- [`D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`](D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md) — latest completed identity/revision/history authority cutover and validation；
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md) — current D6.1d continuation contract；
- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — current priority / guardrails；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — risk-based T0–T4 validation；
- live production code — App/root 与 extracted owners 的最终事实来源。

Persistence Simplification 的 audit、handoff、PS1–PS5 checkpoint 已完成，统一保留在：

- [`archive/handoffs/`](archive/handoffs/)
- [`archive/checkpoints/`](archive/checkpoints/)

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
4. `D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md` controls the D6 ownership baseline；
5. latest D6 progress checkpoint refines completed-slice evidence；
6. the one active D6 handoff controls the approved narrow continuation plan；
7. specialized design docs control their own semantic/product domain where non-conflicting；
8. archive documents, old Git branches and historical PR records are evidence only。
