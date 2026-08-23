# CampBoardGameHost 文档入口

> 最后整理：2026-08-23  
> **任何新的开发或审计任务先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> 通过 ChatGPT / GitHub connector / Codex 修改代码时，还必须读开发运行规范。

## 1. 当前权威顺序

出现冲突时：

1. 游戏规则正确性：官方 Blood on the Clocktower 规则 / Almanac / published rulings；
2. 当前开发状态 / 下一步：`CURRENT_DEVELOPMENT_ROADMAP.md`；
3. 当前 handoff：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
4. impaired information / Storyteller decision：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`；
5. Possible Worlds 总体架构：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`；
6. 多剧本 / 动态流程：`多剧本多板子与动态游戏流程架构设计_v1.md`；
7. GitHub / Codex 写入流程：`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
8. 大文件本地实现流程：`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
9. 历史 audit / handoff / `archive/` 仅用于追溯，不得覆盖当前 roadmap。

## 2. 当前必须阅读

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  PR #42 Historical Action + Observation Capture 已合并；当前正在 PR #43 中拆分 `ClocktowerHostScreen.kt`，A1–A9 已完成，A10 边界重新审计中。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md) — **CURRENT HANDOFF**  
  记录 PR #43 live head、A1–A9 证据、A9 final gates、剩余规模和 A10 re-audit stop conditions。

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE**

- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — **NORMATIVE**  
  大文件 declaration move / mechanical cleanup 默认由 Luna 在完整本地 worktree 中实施、测试、commit、push；ChatGPT 负责远端 exact diff 与 CI/R2。

## 3. 当前准确开发状态

```text
PR #39 Storyteller Information Decision Foundation     MERGED
PR #40 Structured Manual UI — Empath numeric slice     MERGED
PR #41 workflow / LF policy docs-infra                 MERGED
PR #42 Historical Action + Observation Capture         MERGED
PR #43 Clocktower host source decomposition            DRAFT / A1–A9 GREEN

live main:
88164a5bba1fa80695a0247538e632d127e5cfa1

PR #43 validated head:
00a2d19e45415614fbd8e93e83a53ba4d2cf9d35

current execution point:
A10 BOUNDARY RE-AUDIT — no implementation yet

next product slice after decomposition:
A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

PR #43 仍为 draft、未 merge。未经用户明确授权不得 merge。

## 4. PR #43 摘要

A1–A9 已完成以下 owner extraction / cleanup：

- `ClocktowerHostCoreSemantics.kt`
- `ClocktowerHostSelectionSemantics.kt`
- `ClocktowerHostPresentationModels.kt`
- `ClocktowerStorytellerRecommendationUi.kt`
- `ClocktowerPlayerDisplayUi.kt`
- `ClocktowerRegistrationUi.kt`
- `ClocktowerNightStepUi.kt`
- A9 unreachable legacy fallback / `ClocktowerInfoCard` cleanup

A8 final gate：

```text
ClocktowerNightStepUi.kt   45,251 bytes
CI #503                    SUCCESS
R2 #443                    SUCCESS
```

A9 final gate：

```text
RED commit                  3ecbcadbd728ac83f7ab1f8d1d40175795e44078
GREEN commit                00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
exact deletion audit        PASS
CI #506                     SUCCESS
R2 #446                     SUCCESS
```

A9 后 Host 为 294,769 bytes / 4,818 lines；共删除 25,068 bytes / 484 lines。50 KiB 目标尚未完成。

## 5. 当前 A10 边界审计

A10 尚未开始。下一步只审计 active `ClocktowerJudgeScreen` 内可独立拥有的 state/model construction 或 phase-presentation 边界，再建立真实 tests-first contract。

不得直接移动 stateful Compose block；不得改变 active UI、day/night flow、callback ordering、recommendation、registration、information decision、history/persistence 或 session authority。

## 6. 大文件工作流

```text
complete small/medium file
  -> connector direct update

large file / declaration move / mechanical cleanup
  -> ChatGPT scope + tests
  -> Luna local implementation + focused/full validation
  -> commit + push
  -> ChatGPT remote exact diff + CI/R2
```

Luna push 后必须返回 commit SHA；ChatGPT 必须从 GitHub 重新确认 live head，不能只依赖本地文字报告。

## 7. 新会话启动顺序

1. 读本文；
2. 读 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 读 `CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
4. 读 `CURRENT_DEVELOPMENT_ROADMAP.md`；
5. 读 `NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
6. 查询 live `main`、PR #43 和 feature head；
7. 若 PR #43 仍 open，从其 live head 继续；
8. 当前从 A10 boundary re-audit 开始，尚不实施；
9. 不提前进入 A3 product work；
10. 未经用户明确授权不得 merge。

## 8. 文档维护规则

- 只有 `CURRENT_DEVELOPMENT_ROADMAP.md` 维护当前执行点；
- handoff 服务下一次开发；
- design 文档维护语义/架构，不维护 live branch；
- 每个 decomposition slice 完成后更新 head、gate、剩余规模和 next boundary；
- historical audit 不得覆盖 current roadmap。
