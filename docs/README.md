# CampBoardGameHost 文档入口

> 最后整理：2026-09-07 Australia/Sydney  
> 目标：新的开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不参与当前决策。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — **项目级 AI / architecture / test / Git 执行规范**；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行优先级权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md) — **唯一当前 active handoff**；
4. [`PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`](PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md) — 当前 persistence requirement / field-classification audit；
5. 当前任务需要的 specialized semantic / product design；
6. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
7. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或旧文档中的 `PASS / COMPLETE / READY / NEXT` 字样推断当前状态。

## 2. 当前状态

当前 live-main campaign baseline：

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge PR #110 — Audit Poisoner execution dusk crash path
```

近期已集成的重要节点：

```text
PR #99   R4D-6 Host Table preserved-lineage integration
PR #100  UI-N1 inline actor/wake cue + square-table readability
PR #101  same-night dead-role wake-step fix
PR #102  Manual Demon bluff consistency fix
PR #104  obsolete source-wiring guard retirement
PR #105  Drunk shown-identity ownership repair
PR #106  Night Step D1–D5 ownership/decomposition campaign
PR #107  crash flight-recorder follow-up
PR #108  early Application install follow-up
PR #110  optional Poisoner chronology / execution dusk crash-path fix
```

The docs-only D6 planning PR #111 was closed without merge after a fresh persistence audit invalidated its core
assumption. Its branch remains historical evidence only.

## 3. 当前执行顺序

```text
Persistence Simplification / Recent Emergency Recovery
  PS0 product contract freeze
  -> PS1 archive/recovery separation
  -> PS2 minimal typed RecoverySnapshot
  -> PS3 typed safe restore
  -> PS4 old active-save infrastructure retirement
  -> PS5 persistence-trigger simplification
-> fresh D6 App/Host ownership audit + decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

A4/ZDD remains non-production.

Persistence Simplification is **not** a large-file-decomposition task. The goal is to remove product and state
responsibility the app does not need. File-size reduction is only a consequence.

## 4. 当前 persistence references

- [`PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`](PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md) — why full Save/Restore is over-scoped; five-category field audit; Clocktower/Werewolf exceptions; target recovery model；
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md) — PS0–PS5 implementation contract; PS1 is the next production slice；
- active production behavior remains authoritative where the audit marks a field/consumer for re-check before deletion.

Frozen product principle:

```text
Restore the game, not the App.
```

Active recovery is short-horizon emergency recovery, not a long-lived Save Game product.

## 5. Prior decomposition / UI references

The Night Step D1–D5 campaign is complete and integrated. Its documents remain useful architecture/history
references but are not the current execution route:

- [`CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`](CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md)
- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)

The old D6 branch/PR #111 must not be resumed as the implementation route. After Persistence Simplification is
merged, perform a new ownership audit against the reduced App/Host code.

## 6. Epistemic / rules 长期参考

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

## 7. Normative engineering workflow

- root `AGENTS.md` — first execution/architecture authority;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md);
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md);
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md).

`AGENTS.md` treats architecture pre-flight and test-first as one decision process: classify the change, identify
ownership/durable contract, then choose the cheapest reliable evidence. A new production edit does not
automatically imply a manufactured RED.

For this campaign, persistence/schema/restore boundaries are real durable contracts and therefore justify focused
behavior coverage. Source-string tests remain inappropriate for ordinary implementation movement/deletion.

## 8. Other active/future architecture references

These remain in docs root because they describe long-lived contracts, future designs, or reusable engineering
decisions rather than the current campaign:

- `TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`
- `TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`
- `storyteller_revision_driven_dynamic_decision_engine_plan.md`
- `unified_semantic_model.md`
- `epistemic_reference_matrix.md`
- `asp_oracle_cross_validation.md`
- `external_solver_evaluation.md`
- `多剧本多板子与动态游戏流程架构设计_v1.md`
- `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`

Their presence in root does not make them the current execution priority; current priority is controlled by the
roadmap + active handoff.

## 9. Archive layout

Historical evidence belongs under [`archive/`](archive/README.md):

```text
archive/handoffs/     closed/superseded NEXT handoffs
archive/checkpoints/  completed implementation/test/checkpoint records
archive/ui/           superseded UI plans / closeout evidence
archive/deferred/     unfinished but explicitly deferred future work
archive/workflows/    superseded workflow guidance
```

Historical branches/PRs may also remain as provenance. Their existence does not make their plan current.

## 10. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution and architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state and priority;
4. the one active Persistence Simplification handoff controls the approved narrow campaign plan;
5. specialized design docs control their own semantic/product domain where non-conflicting;
6. archive documents, old Git branches and historical PR records are evidence only.