# CampBoardGameHost 文档入口

> 最后整理：2026-09-10 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md) — **唯一 active handoff**；
4. [`UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`](UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md) — UI-NAV-1 accepted visual/ownership baseline；
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
6. 当前 slice 所需的 specialized semantic/product docs；
7. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
Persistence Simplification — COMPLETE / merged
D6.1 — COMPLETE / merged
D6.2 R0–R2 — COMPLETE / FULL accepted / merged
R3 transaction viability — COMPLETE / NO-GO
D6 decomposition campaign — COMPLETE
UI-R5 square-table convergence — COMPLETE / merged (#117)

CURRENT:
UI-NAV-1 Global Navigation Visual Unification
  1A read-only audit — COMPLETE / GO
  1B shared stateless three-slot bottom primitive — NEXT

THEN:
EPI-MQ-0 baseline / ownership re-audit + behavior corpus
-> EPI-MQ-1 hypothetical observation evaluation seam (only if EPI-MQ-0 GO/MODIFY)
-> later EPI-MQ quality metrics/ranking
-> UX-R6 recommendation-provider replacement unless explicitly reprioritized
```

UI-NAV-1 accepted visual direction:

```text
No persistent global top bar.
Content owns title / instructions / square table / optional local progress.
Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

Progress remains screen-owned/optional. Identity reveal keeps its current useful progress; UI-NAV-1 does not impose one shared identity/night progress model.

Stable inherited UI-R5 baseline:

```text
UI-R5 merged production baseline b47b00fd0c727e048dcb1b57260b8dd6fff466a1
UI-R5 final T4 2958f334fc7cccd59ed2e75a3bdaa60684492292
CI #2143 PASS — Android full + APK / ASP / Real Clingo / gate
R2 #2010 PASS
post-merge main CI #2146 PASS
Field Test APK #34 PASS
```

UI-NAV-1 campaign-start main:

```text
9c19484c682044bb469d90fb7522810ca49ecac2
branch codex/ui-nav-1-global-navigation-visual-unification
```

Always query live `main` and the active branch before starting a new slice.

UI-R5 implementation is closed as a development campaign. Some role/device paths remain field-test follow-up; CI is not treated as proof of real-device acceptance.

## 当前 active references

Current status / execution:

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md)
- [`UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`](UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)

Queued next program — EPI-MQ / cognitive consistency:

- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md) — queued, not historical; resumes after UI-NAV-1 closeout
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

Long-lived UI / information product references remain active when relevant:

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)
- [`FIELD_TEST_APK_DISTRIBUTION.md`](FIELD_TEST_APK_DISTRIBUTION.md)

## UI-R5 historical evidence

Completed UI-R5 handoffs are archived under:

- [`archive/handoffs/`](archive/handoffs/)

Completed UI-R5 audits / acceptance / closeout evidence is grouped under:

- [`archive/checkpoints/ui-r5/`](archive/checkpoints/ui-r5/)
- [`archive/checkpoints/ui-r5/UI_R5_CLOSEOUT_INDEX_2026-09-10.md`](archive/checkpoints/ui-r5/UI_R5_CLOSEOUT_INDEX_2026-09-10.md)

Do not read the full UI-R5 archive in a normal UI-NAV/EPI-MQ session. Load a specific historical file only when a current ownership question requires its evidence.

## D6 / R3 historical evidence

Historical audit/progress/acceptance files live under:

- [`archive/checkpoints/d6/`](archive/checkpoints/d6/)
- [`archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`](archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md)

Closed D6 handoffs live under [`archive/handoffs/`](archive/handoffs/).

D6 结束后的架构结论继续有效：不要为了消灭大文件尺寸而引入第二 coordinator、mega context 或 callback bag。只有真实产品需求自然形成新的 application seam 时才重新审计。

## Same-night / rules architecture references

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) where applicable.

Older superseded workflow guidance remains under `archive/workflows/`.

## Archive / status authority

Historical evidence belongs under [`archive/`](archive/README.md). If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. the single active handoff linked above;
5. specialized domain/product docs where non-conflicting;
6. archive/old branches/old PRs as evidence only.