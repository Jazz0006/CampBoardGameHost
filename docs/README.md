# CampBoardGameHost 文档入口

> 最后整理：2026-09-04 Australia/Sydney  
> 目标：让新的开发会话只读取少量真正有权威性的文档，不再被历史 handoff / checkpoint 误导。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — 项目级 AI / Git / 测试执行规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行顺序权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md) — **唯一当前 active handoff**；
4. 当前任务需要的 specialized design / semantic reference；
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
6. 查询 live GitHub state 后再实施。

不要从旧 commit SHA、旧 PR、历史 branch、archive 文档或旧 handoff 推断当前状态。

## 2. 当前 active task

```text
UI-N1 — Night persistent Host Table wake/action lifecycle

WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

当前目标：让夜间唤醒、行动、裁定/信息选择、玩家展示和完成推进共享同一个 persistent Host Table 生命周期，避免 action selector 在 Storyteller 尚未明确确认唤醒对象前覆盖 wake instruction。

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md)

当前 roadmap：

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)

## 3. 当前 UI 设计/架构参考

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`UI_R4B_NIGHT_ACTION_SURFACE_PLAN_2026-09-02.md`](UI_R4B_NIGHT_ACTION_SURFACE_PLAN_2026-09-02.md)
- [`UI_STACK_CLOSEOUT_2026-09-04.md`](UI_STACK_CLOSEOUT_2026-09-04.md) — historical closeout evidence; not current next-step authority.

Persistent Host Table current principles:

```text
stable typed seatId
-> stable physical table position
-> phase-specific center task
-> typed actor / legal target / selected target presentation
-> sanitized Player Reveal boundary
```

## 4. Preserved post-#92 R4D-6 salvage lineage

A post-#92 implementation chain was intentionally excluded from PR #94 and remains outside `main`.

Preserve these branches until the `UI-R4D residual migration audit` is complete:

```text
codex/ui-r4d6-4c-demon-successor-table
-> codex/ui-r4d6-closeout-seat-number-badge
-> codex/ui-r4d6-closeout-host-seat-role-presentation
-> codex/ui-r4d6-closeout-adaptive-seat-presentation
-> codex/ui-r4d6-closeout-postdeal-role-visibility
```

Furthest audited descendant:

```text
b0eabb24620a14ce704c6e3de5df9ec569e0c864
```

Do not bulk merge/cherry-pick this lineage into UI-N1. It is a historical implementation reference to be reconciled after UI-N1 as `REUSE / REIMPLEMENT / SUPERSEDED / DEFER`.

## 5. Execution order after UI-N1

```text
UI-N1 Night lifecycle
-> UI-R4D residual migration audit
-> UI-R5 final real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

Public Claim History and Sequential Vote UX remain deferred by product decision unless explicitly reopened in the roadmap.

## 6. Epistemic / misinformation quality references

Future long-lived design reference:

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)

Foundational algorithm reference:

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)

Frozen semantic ordering:

```text
actual identity
-> committed shown identity
-> perceived ability
-> complete legal/truth semantic domain
-> RELIABLE / POISONED / DRUNK
-> recommendation/manual decision
-> AbilityObservation
-> durable player-visible history
-> UI
```

Future quality ranking may only operate downstream of legal semantic authority.

## 7. Same-night / rules architecture references

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md)
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md)

## 8. Future architecture references

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md)
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md)
- [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md)
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)

These are not current implementation permission unless reactivated by the roadmap.

## 9. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md)
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md)
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md)

The older `CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` has been archived under `archive/workflows/` and is historical only.

## 10. Documentation lifecycle policy

Active docs root should contain:

```text
CURRENT_DEVELOPMENT_ROADMAP.md
one active NEXT_DEVELOPMENT_HANDOFF_*.md
long-lived architecture / semantic references
normative test / workflow references
limited historical checkpoint evidence pending later consolidation
```

Historical handoffs are under `archive/handoffs/`. Deferred unfinished work belongs under `archive/deferred/`.

When an active task closes:

- retire the old active handoff immediately;
- update the existing roadmap and create/activate only the next required handoff;
- archive completed micro-handoffs/checkpoints in batches rather than allowing them to remain active-looking;
- do not delete historical implementation branches that are explicitly registered as salvage sources until their reconciliation audit is complete.

## 11. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state and priority;
4. the one active task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. archive documents, old Git branches and historical PR records are evidence, not current instructions.
