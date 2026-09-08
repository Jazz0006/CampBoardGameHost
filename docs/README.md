# CampBoardGameHost 文档入口

> 最后整理：2026-09-08 Australia/Sydney
> 目标：新开发会话优先读取少量当前权威文档；历史 checkpoint / handoff 默认只作为证据。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
3. [`D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`](D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md) — D6.1 T4 acceptance；
4. [`D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`](D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md) — post-D6.1 residual audit 与 D6.2 路线；
5. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md) — 当前 narrow continuation contract；
6. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
7. 当前任务需要的 specialized semantic/product docs；
8. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或旧文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
PS5 Persistence Simplification — COMPLETE / merged
D6.0 ownership re-audit — COMPLETE
D6.1 Clocktower session authority — COMPLETE / T4 GREEN
PR #113 — Draft/open, waiting for explicit merge decision
D6.2 — UI composition boundary characterization after D6.1 merge
```

D6.1 已将 canonical writable authority 收敛到现有 `ClocktowerGameSession`，包括 identity、revisions、semantic chronology 和 dynamic GameState mechanics。App-root cards/flow variables 是 presentation/orchestration mirrors，不再是第二 canonical mechanical owner。

post-D6.1 re-audit 显示下一主要架构债已经转为 `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI composition fan-out：103 parameters / 39 callbacks。D6.2 不再继续 broad GameState/read-side migration。

## 当前 D6 references

Current authority/evidence:

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`](D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md)
- [`D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`](D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)

Historical D6 evidence remains available in D6.0–D6.1d progress/audit docs but does not control the next priority.

## Long-lived product / semantic references

UI / information design:

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)

Epistemic/future misinformation quality:

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

Same-night/rules architecture:

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md).

Older superseded workflow guidance remains under `archive/workflows/`.

## Archive

Historical evidence belongs under [`archive/`](archive/README.md):

```text
archive/handoffs/
archive/checkpoints/
archive/ui/
archive/deferred/
archive/workflows/
```

If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. latest active D6 acceptance/audit/handoff docs;
5. specialized domain docs where non-conflicting;
6. archive/old branches/old PRs as evidence only.
