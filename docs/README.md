# CampBoardGameHost 文档入口

> 最后整理：2026-09-08 Australia/Sydney
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / handoff 默认只作为证据。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
3. [`D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`](D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md) — D6.2 路线与 residual audit；
4. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md) — **当前 active handoff**；
5. [`D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`](D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md) — D6.1 已合并验收证据；
6. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
7. 当前任务需要的 specialized semantic/product docs；
8. 查询 live GitHub state 后再实施。

不要从 archive、旧 branch、旧 PR 或旧文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
PS5 Persistence Simplification — COMPLETE / merged
D6.1 Clocktower session authority — COMPLETE / T4 GREEN / merged
PR #113 — merged
merge commit — 112572cbd3d990737a412cc4b8ead766d00867e8
merge CI 34225075948 — PASS
Field Test APK 34225075992 — PASS
D6.2a — NEXT: Judge UI composition characterization on a fresh main-based branch
```

D6.1 已将 canonical writable authority 收敛到现有 `ClocktowerGameSession`，包括 identity、revisions、semantic chronology 和 dynamic GameState mechanics。App-root cards/flow variables 是 presentation/orchestration mirrors，不再是第二 canonical mechanical owner。

post-D6.1 re-audit 显示下一主要架构债已经转为 `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI composition fan-out：103 parameters / 39 callbacks。D6.2 不继续 broad GameState/read-side migration。

## 当前 D6 references

Current authority/evidence:

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`](D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md)
- [`NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md)
- [`D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`](D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)

Historical D6.0–D6.1d docs remain evidence but do not control the next priority. The previous `NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md` is closed and superseded by the D6.2 handoff above.

## D6.2 execution principle

```text
characterize consumption first
-> identify smallest cohesive UI boundary
-> preserve canonical session/domain ownership
-> structural behavior-preserving extraction
-> focused validation
-> re-audit fan-out before the next slice
```

Do not create a mega Actions/State bag or broad Controller/ViewModel merely to reduce parameter count.

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

## Archive / status authority

Historical evidence belongs under [`archive/`](archive/README.md). If documents disagree, priority is:

1. official BoTC rules/rulings for gameplay correctness;
2. root `AGENTS.md` for execution/architecture/test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` for current state/priority;
4. current active D6.2 audit/handoff;
5. specialized domain docs where non-conflicting;
6. completed D6.1 evidence and archive/old branches/old PRs as evidence only.
