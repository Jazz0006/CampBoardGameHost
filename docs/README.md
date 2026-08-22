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
8. 大文件本地 patch 流程：`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
9. 历史 audit / handoff / `archive/` 仅用于追溯，不得覆盖当前 roadmap。

## 2. 当前必须阅读

### 当前路线

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  当前 validated product baseline 已包含 PR #39 Decision Foundation 和 PR #40 Empath structured manual UI first production slice。下一产品 source slice 是 **Historical Action + Observation Capture**。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md) — **CURRENT HANDOFF**  
  记录 PR #39/#40 完成状态、PR #41 developer-workflow 决策，以及下一 Historical Capture slice 的审计入口和 stop condition。

### 当前开发运行规范

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE**  
  小/中等完整文件可由 connector 直接写；一旦大文件出现 truncation / incomplete content，停止 whole-file reconstruction。

- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — **NORMATIVE**  
  大文件默认执行路径：ChatGPT 生成 patch + tests + Luna prompt；Codex Luna 在本地 worktree apply/test/commit/push；ChatGPT 从 GitHub 接回 exact diff / CI / review。

### 当前专项架构

- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md)

### 历史 / fallback reference

- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — Git Data API / temporary writer 历史机制参考。remote writer **不是当前默认方案**。

## 3. 当前准确开发状态

```text
PR #24 Production Semantic-History Foundation          MERGED
PR #29 Impaired Information Semantics                  MERGED
PR #27 Global Observation Ownership                    MERGED
PR #39 Storyteller Information Decision Foundation     MERGED
PR #40 Structured Manual UI — Empath numeric slice     MERGED
PR #41 workflow / LF policy docs-infra                 DRAFT

validated live product main:
205473868b50e159977a8ad34e2cf239a711a79d

next product source slice:
Historical Action + Observation Capture
```

PR #40 并不表示所有信息角色 manual UI 已完成；当前 production rollout 是 Empath numeric first slice。

## 4. 大文件工作流当前结论

永久 remote `issue_comment` writer 已探索但**未采用**：静态 CI 可通过，但 pre-merge 无法端到端验证，因为此类 workflow 必须先存在于 default branch。

因此不要在新会话重新实现 permanent writer。

当前标准：

```text
complete small/medium file
  -> connector direct write

large file + truncation/incomplete content
  -> ChatGPT minimum patch
  -> Codex Luna local apply/test/commit/push
  -> ChatGPT remote audit
```

Luna 测试通过后必须 push 当前 feature branch；否则 ChatGPT 无法从 repo 继续验证。

## 5. 当前 rollout 摘要

```text
Semantic-History Foundation                         DONE
Impaired Information Semantics                     DONE
Global Observation Ownership                       DONE
Storyteller Information Decision Foundation        DONE
Structured Manual UI first slice (Empath)          DONE
Historical Action + Observation Capture            NEXT
A3 historical multi-night exact baseline           LATER
physical Grimoire / Spy VerifiedExact              LATER
B4 historical expansion                            LATER
revision-driven recommendation/history unification LATER
broader structured manual role rollout             AS PRIORITIZED
ZDD production reconsideration                     LATER
```

## 6. 新会话启动顺序

1. 读本文；
2. 读 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 读 `CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
4. 读 `CURRENT_DEVELOPMENT_ROADMAP.md`；
5. 读 `NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
6. 查询 live `main` 和 open PR，不相信旧 SHA；
7. 如果是产品 source work，从最新 main 新建 focused branch；
8. behavior change 先真实 RED；
9. 大文件若 connector 不完整，直接走 Luna patch，不再折腾 remote writer；
10. 未经用户明确授权不得 merge。

## 7. 文档维护规则

- 只有 `CURRENT_DEVELOPMENT_ROADMAP.md` 维护当前执行点；
- handoff 服务下一次开发；
- design 文档维护语义/架构，不维护 live branch；
- historical audit 保留证据但不覆盖 current roadmap；
- developer workflow 以 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 和 Luna patch 文档为准。
