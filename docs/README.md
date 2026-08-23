# CampBoardGameHost 文档入口

> 最后整理：2026-08-23  
> **任何新的开发或审计任务先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> 通过 ChatGPT / GitHub connector / Codex 修改代码时，还必须读根目录 `AGENTS.md` 和对应工作流文档。

## 1. 当前权威顺序

出现冲突时：

1. 游戏规则正确性：官方 Blood on the Clocktower 规则 / Almanac / published rulings；
2. 项目级 AI 执行规范：根目录 `AGENTS.md`；
3. 当前开发状态 / 下一步：`CURRENT_DEVELOPMENT_ROADMAP.md`；
4. 下一任务 handoff：`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`；
5. PR #43 completion evidence：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md`；
6. impaired information / Storyteller decision：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`；
7. Possible Worlds 总体架构：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`；
8. 多剧本 / 动态流程：`多剧本多板子与动态游戏流程架构设计_v1.md`；
9. GitHub / Codex 写入流程：`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
10. 大文件本地实现流程：`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
11. 历史 audit / handoff / `archive/` 仅用于追溯，不得覆盖当前 roadmap。

## 2. 当前必须阅读

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  PR #43 A1–A13 已完成并通过 final merge-readiness audit；用户已授权 merge。下一 structural task 改为 App-root decomposition，A3 顺延。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md) — **NEXT TASK HANDOFF**  
  PR #43 merge 后，在新对话 / fresh branch 中审计并拆分 `CampBoardGameHostApp.kt`；先做 ownership inventory 和 slice plan，不直接 mass extraction。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md) — **PR #43 COMPLETION EVIDENCE**  
  记录 A13 planner-first Other Night materialization、post-A13 audit、Host protected transaction boundary 和 PR #43 completion evidence。

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE**

- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — **NORMATIVE**  
  大文件 declaration move / mechanical cleanup 默认由 Luna 在完整本地 worktree 中实施、测试、commit、push；ChatGPT 负责远端 exact diff 与 CI/R2。

## 3. 当前准确开发状态

```text
PR #39 Storyteller Information Decision Foundation     MERGED
PR #40 Structured Manual UI — Empath numeric slice     MERGED
PR #41 workflow / LF policy docs-infra                 MERGED
PR #42 Historical Action + Observation Capture         MERGED
PR #44 Drunk / Fortune Teller correctness hotfix       MERGED
PR #43 Clocktower host source decomposition            MERGE-READY / A1–A13 GREEN

PR #43 validated implementation:
b37f0067b674a0cd4bee5ff311840d1c52ce8c05

current execution point:
finish docs + merge PR #43

next structural task:
CampBoardGameHostApp.kt APP-ROOT DECOMPOSITION

next product task after structural pass:
A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

PR #43 merge 前必须重新确认 live head / mergeability / latest CI。用户已经在 2026-08-23 明确授权本次 merge。

## 4. Large-file state

当前主要 handwritten production 大文件约为：

```text
CampBoardGameHostApp.kt      325,556 bytes   next structural priority
ClocktowerHostScreen.kt      295,644 bytes   PR #43 high-value pass complete
ClocktowerDayScreen.kt        63,135 bytes   audit later; split only on natural seam
ClocktowerNightStepUi.kt      45,251 bytes
MainActivity.kt                1,102 bytes   earlier R2 decomposition complete
```

重要区别：早先 R2 已经完成 `MainActivity.kt` 拆分；当前最大的 root monolith 是 `CampBoardGameHostApp.kt`。

`ClocktowerHostScreen.kt` 从 PR #43 后进入 **new-responsibility growth freeze**：允许继续作为较大的 orchestrator，但新算法、policy、history/session behavior 或大块角色 UI 不应默认继续堆入 Host。

## 5. Revised development sequence

```text
PR #43 docs + final guard
-> merge PR #43
-> stop this conversation
-> new conversation / fresh branch from merged main
-> audit + decompose CampBoardGameHostApp.kt
-> remeasure large source files
-> audit ClocktowerDayScreen.kt only if a natural split exists
-> resume A3 historical multi-night exact baseline
```

不得把 A3 product behavior 混进 App-root structural PR。

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

## 7. 下一新会话启动顺序

1. 读根目录 `AGENTS.md`；
2. 读本文；
3. 读 `CURRENT_DEVELOPMENT_ROADMAP.md`；
4. 读 `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`；
5. 查询 live `main` 并确认 PR #43 已 merge；
6. 记录实际 merge SHA；
7. 从 live `main` 创建 fresh structural branch；
8. 审计 `CampBoardGameHostApp.kt` 当前大小与 responsibility inventory；
9. 返回候选 slices、风险排序、protected state/effect boundaries 和 first characterization plan；
10. 之后才开始 implementation；
11. 不提前进入 A3 product work。

## 8. 文档维护规则

- `AGENTS.md` 维护长期 AI 执行规范；
- 只有 `CURRENT_DEVELOPMENT_ROADMAP.md` 维护当前执行点；
- handoff 服务下一次开发；
- design 文档维护语义/架构，不维护 live branch；
- 每个 structural slice 完成后更新 head、gate、剩余规模和 next boundary；
- historical audit 不得覆盖 current roadmap。
