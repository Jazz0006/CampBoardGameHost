# CampBoardGameHost 文档入口

> 最后整理：2026-09-13 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
4. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md) — **唯一 active handoff**；
5. [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md) — 当前 EPI-MQ 架构/所有权基线；
6. 查询 live GitHub `main` 后再实施。

不要从 archive、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
UI-R5 square-table convergence                    COMPLETE / PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / PR #118
ROLE-ROTATION-1 consecutive-role avoidance        COMPLETE / PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / PR #120
UI-INFO-1 information filtering & layout          COMPLETE / PR #121 merge-ready

CURRENT:
EPI-MQ-0.5 dynamic-script extensibility guard

QUEUED:
EPI-MQ-1+ neutral evaluator / productive uncertainty
UX-R6 recommendation-provider replacement
```

UI-INFO-1 已完成产品/真机验收。旧 UI-INFO-1 handoff 不再具有执行权，收口后移入 `archive/`。

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md)
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)

## EPI-MQ supporting references

按需加载：

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)

## Long-lived product / architecture references

按需加载，不作为默认执行状态：

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

Completed campaign handoffs/audits belong under `archive/` and should be loaded only for a specific ownership/regression question.

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) only where applicable.

## Status authority

If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. the single active handoff linked above;
5. specialized domain/product docs where non-conflicting;
6. archive/old branches/old PRs as evidence only.
