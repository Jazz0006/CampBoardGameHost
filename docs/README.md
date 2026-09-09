# CampBoardGameHost 文档入口

> 最后整理：2026-09-09 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md) — **唯一 active handoff**；
4. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
5. [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md) — 当前 square-table / Manual / player-display 产品参考；
6. 当前 slice 所需的 specialized semantic/product docs；
7. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
Persistence Simplification — COMPLETE / merged
D6.1 — COMPLETE / merged
D6.2 R0–R2 — COMPLETE / FULL accepted / merged (#115)
R3 transaction-application viability audit — COMPLETE / NO-GO
D6 decomposition campaign — COMPLETE

NEXT:
square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

D6 结束后的架构结论是：不要再为了消灭 `CampBoardGameHostApp.kt` 的大文件尺寸而引入第二 coordinator、mega context 或 callback bag。只有未来真实产品需求自然形成新的 application seam 时，才重新审计。

## 当前 active references

Current status / execution:

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)

UI / information design:

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)

## D6 / R3 historical evidence

The completed D6 decomposition campaign has been removed from the active docs root.

Historical audit/progress/acceptance files now live under:

- [`archive/checkpoints/d6/`](archive/checkpoints/d6/)
- [`archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`](archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md)

Closed D6 handoffs live under [`archive/handoffs/`](archive/handoffs/).

Do not read the full D6 archive in a normal UI-R5 session. Load a specific historical file only when a current ownership question requires its evidence.

## Long-lived semantic / algorithm references

Epistemic / future misinformation quality:

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

Same-night / rules architecture:

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

Other long-lived architecture/reference docs remain in the docs root when they are still intentionally active or future-facing.

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) where its execution path is applicable.

Older superseded workflow guidance remains under `archive/workflows/`.

## Archive / status authority

Historical evidence belongs under [`archive/`](archive/README.md). If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. the single active handoff linked above;
5. specialized domain/product docs where non-conflicting;
6. archive/old branches/old PRs as evidence only.
