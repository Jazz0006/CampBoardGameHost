# CampBoardGameHost 文档入口

> 最后整理：2026-09-12 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
4. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md) — **唯一 active handoff**；
5. [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md) — 当前 UI-INFO-1 产品参考；
6. 查询 live GitHub `main` 后再实施。

不要从 archive、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
UI-R5 square-table convergence                    COMPLETE / PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / PR #118
ROLE-ROTATION-1 consecutive-role avoidance        COMPLETE / PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / accepted / PR #120 closeout

CURRENT:
UI-INFO-1 information filtering & layout

QUEUED:
EPI-MQ / Productive Uncertainty
UX-R6 recommendation-provider replacement
```

UI-INFO-1 的第一步是 **read-only audit**：先判断每个界面应该保留、弱化、隐藏或删除哪些信息，再设计排版层级。不要一开始就直接修改 Compose。

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)

## Queued EPI-MQ references

EPI-MQ 继续排在 UI-INFO-1 之后；恢复时必须基于当时 live `main` 重新确认 baseline：

- [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md)
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

## Long-lived product / architecture references

按需加载，不作为默认执行状态：

- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`UI_NAV_1_CLOSEOUT_2026-09-10.md`](UI_NAV_1_CLOSEOUT_2026-09-10.md)
- [`UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`](UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md)

Historical D6/UI-R5 execution evidence remains under `archive/` and should be loaded only for a specific ownership/regression question.

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
