# CampBoardGameHost 文档入口

> 最后整理：2026-09-04 Australia/Sydney  
> 目标：让新的开发会话只读取少量真正有权威性的文档，不再被历史 handoff / checkpoint 误导。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — 项目级 AI / Git / 测试执行规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前项目状态与执行顺序权威**；
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md) — **唯一当前 active handoff**，现覆盖 R4D-6 full integration prerequisite 与紧随其后的 UI-N1；
4. 当前任务需要的 specialized design / semantic reference；
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
6. 查询 live GitHub state 后再实施。

不要从旧 commit SHA、旧 PR、历史 branch、archive 文档或旧 handoff 推断当前状态。

## 2. 当前 active task

```text
R4D-6 FULL INTEGRATION / RECONCILIATION
-> UI-N1 Night persistent Host Table lifecycle
```

2026-09-04 的全局审计发现，PR #94 明确排除的一条 post-#92 R4D-6 lineage 仍保留有可用的 Host Table 完成工作。用户随后明确决定：趁设计意图仍清楚，先完整吸收这条 lineage 的有效最终功能，再开始 UI-N1。

当前 integration branch：

`codex/r4d6-full-integration`

当前 Draft PR：

`#99 — R4D-6: absorb preserved Host Table closeout lineage`

内部三方 merge PR #98 已将最终 salvage descendant 合入 integration branch；**这不是对 main 的 merge 授权**。

## 3. R4D-6 full integration contract

最终 salvage descendant：

```text
codex/ui-r4d6-closeout-postdeal-role-visibility
b0eabb24620a14ce704c6e3de5df9ec569e0c864
```

它与当前 main 的共同基线是 PR #92 head `5501fb02...`，历史上 44 commits ahead；但三方 merge 后相对当前 main 的最终净差异仅集中在 Host Table presentation / tests。

当前吸收目标包括：

- Demon Successor square-table migration；
- shared typed `HostSeatPresentation` consumption for Night / Fortune Teller / pair Manual；
- seat number badge；
- Storyteller actual/shown role presentation，包括 Drunk；
- adaptive seat density / bounded long-name readiness；
- post-deal Host role visibility；
- shared seat-number presentation in Player Reveal；
- corresponding typed tests。

吸收原则：

- 默认保留最终仍有效的产品能力；
- current `main` 的后续架构与规则 authority 优先；
- 不恢复已经自删除的 one-shot workflow / patch script；
- 不把旧 Night presentation shape 当成未来 UI-N1 lifecycle authority；
- 若三方 merge 后的净 patch 只是 typed seat/presentation wiring，不因其历史来源而重新实现；
- final `main` merge 仍需正常 CI/R2、exact diff audit 与用户明确授权。

## 4. 紧随其后的 UI-N1

R4D-6 integration 合并后，立即进入：

```text
UI-N1 — Night persistent Host Table wake/action lifecycle
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

UI-N1 继续复用现有 `HostTableShell` / stable `ClocktowerSeatId`，不创建第二套 table framework。

关键产品边界：

- WAKE 必须在 action selector 前成为明确阶段；
- actor / legal target / selected / disabled / dead presentation 语义分离；
- RESOLVE 保留 Storyteller recommendation / Manual authority；
- SHOW 仍是 sanitized Player Reveal boundary；
- lifecycle state 不接管 gameplay legality / rule truth；
- navigation / recomposition / restore 不得静默丢失、重复或提前推进阶段。

## 5. Execution order

```text
R4D-6 full integration / reconciliation
-> UI-N1 Night lifecycle
-> R4D-6 residual verification only
-> UI-R5 final real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

UI-N1 后的 residual 工作应只处理 full integration 与 UI-N1 都未覆盖的真实 active legacy path；不要重新打开已经吸收完成的 R4D-6 功能。

Public Claim History 与 Sequential Vote UX 继续按产品决定延期。

## 6. 当前 UI / architecture references

- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`UI_R4B_NIGHT_ACTION_SURFACE_PLAN_2026-09-02.md`](UI_R4B_NIGHT_ACTION_SURFACE_PLAN_2026-09-02.md)
- [`UI_STACK_CLOSEOUT_2026-09-04.md`](UI_STACK_CLOSEOUT_2026-09-04.md) — historical closeout evidence only.

Persistent Host Table principles:

```text
stable typed seatId
-> stable physical table position
-> Storyteller-private typed seat content
-> phase-specific center task
-> typed interaction state
-> sanitized Player Reveal boundary
```

## 7. Epistemic / rules references

Future misinformation-quality design：

- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)

Same-night / rules architecture：

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)

Frozen semantic ordering remains：

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

## 8. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md)
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md)
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md)

Historical handoffs live under `archive/handoffs/`; deferred work under `archive/deferred/`; superseded workflow guidance under `archive/workflows/`.

## 9. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state and priority;
4. the one active task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. archive documents, old Git branches and historical PR records are evidence, not current instructions.