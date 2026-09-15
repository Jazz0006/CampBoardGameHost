# CampBoardGameHost 文档入口

> 最后整理：2026-09-15 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / dated handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
4. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**；
5. 查询 live GitHub `main` / open PR / checks 后再实施。

不要从 `archive/`、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
GLOBAL-OWNERSHIP-CLEANUP steps 1-6                COMPLETE / PRs #124-#129
EXPERIENCED-UI-1                                  COMPLETE / PR #130
EXPERIENCED-UI-2                                  COMPLETE / PR #131
EXPERIENCED-UI-3 device-test refinements          COMPLETE / PR #132

CURRENT:
Real-device UI polish + small bug fixes

NEXT:
ADB evidence collection for Pair-display latency / old OPPO abnormal exit

PAUSED / QUEUED:
EPI-MQ-0.5 and later productive-uncertainty work
UX-R6 recommendation-provider replacement
```

Product-code baseline after PR #132 merge: `ce1925c5ab70416c6b46981538ab91726e5f8a4d`. Documentation closeout commits may advance `main`; always re-query the live ref.

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable

## Current execution order

1. Finish concrete UI refinements and small bugs found during real-device testing.
2. Keep those changes focused and preserve the shared Beginner/Experienced gameplay pipeline.
3. After that pass is complete, reproduce the deferred Pair-information display stall / old-device abnormal exit with ADB evidence.
4. Add debug-only timing instrumentation only if raw ADB/logcat/bugreport evidence is insufficient.
5. Resume EPI-MQ only after explicit reprioritization.

## Long-lived product / architecture references

Load only when relevant:

- [`GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`](GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md)
- [`EXPERIENCED_NIGHT_FLOW_S2_SURFACE_TOTALITY_AUDIT_2026-09-14.md`](EXPERIENCED_NIGHT_FLOW_S2_SURFACE_TOTALITY_AUDIT_2026-09-14.md)
- [`EXPERIENCED_NIGHT_FLOW_S3_TARGET_ELIGIBILITY_AUDIT_2026-09-14.md`](EXPERIENCED_NIGHT_FLOW_S3_TARGET_ELIGIBILITY_AUDIT_2026-09-14.md)
- [`EXPERIENCED_NIGHT_FLOW_S4_REGRESSION_MATRIX_AUDIT_2026-09-14.md`](EXPERIENCED_NIGHT_FLOW_S4_REGRESSION_MATRIX_AUDIT_2026-09-14.md)
- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

## Paused EPI-MQ references

Load only when that program is explicitly resumed:

- [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md)
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)

## Historical handoffs

Completed/superseded dated handoffs belong under `archive/` and are evidence only. They must not be used as current execution authority.

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable.

## Status authority

If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. `NEXT_DEVELOPMENT_HANDOFF.md` for the current continuation point;
5. specialized domain/product docs where non-conflicting;
6. archive/old branches/old PRs as evidence only.
