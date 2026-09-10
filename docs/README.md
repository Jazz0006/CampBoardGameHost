# CampBoardGameHost 文档入口

> 最后整理：2026-09-11 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md) — **唯一 active handoff**；
4. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
5. 当前 slice 真正需要的 specialized semantic/product docs；
6. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / accepted / merge via PR #118

CURRENT after #118 merge:
ROLE-ROTATION-1 consecutive role repeat avoidance

QUEUED:
EPI-MQ / Productive Uncertainty
UX-R6 recommendation-provider replacement
```

ROLE-ROTATION-1 只优化“选定角色如何分配给玩家”，不是重做角色池或合法 setup 规则。目标是：能避免时避免同一玩家连续拿到同一身份；无法完全避免时最小化重复；同等最优方案之间仍保持随机。

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)

下一会话首先做 read-only ownership/history audit，不要直接改生产代码。

## Queued EPI-MQ references

EPI-MQ 仍然保留，但已经被本次用户优先级调整明确推迟到 ROLE-ROTATION-1 之后：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md)
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

When EPI-MQ resumes, re-audit against then-live `main`; its old baseline is not current execution authority.

## Historical UI evidence

UI-NAV-1 is closed for feature development. These files are historical evidence only and should not be loaded in a normal ROLE-ROTATION-1 session:

- [`UI_NAV_1_CLOSEOUT_2026-09-10.md`](UI_NAV_1_CLOSEOUT_2026-09-10.md)
- [`UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`](UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md)

UI-R5 and D6 historical evidence remains under `archive/` and likewise should be loaded only for a specific ownership/regression question.

## Long-lived product / architecture references

Load only when relevant:

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

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
