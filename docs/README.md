# CampBoardGameHost 文档入口

> 最后整理：2026-08-23（永久 trusted patch writer 基础设施）  
> **任何新的开发或审计任务都应先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> **任何通过 ChatGPT / GitHub connector 修改代码的任务，还必须先读 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`。**  
> 不在 README 中硬编码“当前开发 branch”；所有 source work 都应从最新 live `main` 创建短生命周期 branch。

## 1. 文档权威与冲突处理

当文档之间出现冲突时：

1. **游戏规则正确性**：官方 Blood on the Clocktower 规则 / Almanac / published rulings 优先；项目 golden expectation 次之；外部 Oracle 只用于交叉验证。
2. **当前开发状态和“下一步做什么”**：`CURRENT_DEVELOPMENT_ROADMAP.md` 是唯一状态权威。
3. **当前 Storyteller Decision / impaired-information 边界**：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` 是专项权威。
4. **Possible Worlds / 玩家认知一致性总体架构**：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` 是主规范。
5. **多剧本 / 动态流程架构**：`多剧本多板子与动态游戏流程架构设计_v1.md` 是 R5.5 以后继续生效的专项规范。
6. **当前 handoff**：`NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md` 服务下一次开发，但不得覆盖 roadmap。
7. **历史 audit / closeout / handoff**：保存证据与局部上下文；如果正文仍写 `Impaired Information NEXT`、`#27 PAUSED` 等旧状态，视为历史，不得执行。
8. **本仓库 connector 默认开发策略**：`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 是项目级运行规范。完整内容可靠时可 whole-file / atomic write；一旦 connector 出现 truncation / incomplete content，大文件小 patch 应切换到永久 trusted patch writer。
9. **永久 patch writer 协议**：`TRUSTED_PATCH_WRITER.md` 是 default-branch writer 的规范；定义 PR comment 协议、head/blob 锁、单文件 scope、固定测试 profile、CI/R2 dispatch 与 LF policy。
10. **大文件历史机制参考**：`github_connector_large_file_editing_playbook.md` 保留 Git Data API / temporary writer / workflow trigger 的经验；其中 temporary-writer 默认策略若与第 8–9 项冲突，以当前规范为准。
11. **`archive/`**：仅历史追溯，不作为新代码实施入口。

## 2. 当前必须阅读

### A. 当前路线 / handoff

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  当前 source baseline 已包含 PR #28、#24、#29、#27。下一 source slice 是 **Storyteller Information Decision Foundation**。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md) — **CURRENT HANDOFF / POST PR #27**  
  给新会话的 tests-first 入口：建立 recommendation/manual 共用的 decision + validation + `EpistemicObservationDraft` authority seam；先做 Foundation，不直接做完整 manual UI。

- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md) — **CURRENT SPECIALIZED DESIGN**  
  定义 Drunk/Poison information semantics、balance authority 边界、registration 分层，以及 recommendation + manual Storyteller 的统一 decision authority。

### B. 主架构规范

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — **NORMATIVE**  
  Possible Worlds、玩家知识边界、registration、Oracle 权威与 recommendation 总体架构。

- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md) — **NORMATIVE / FOUNDATION IMPLEMENTED**  
  Catalog、registry、ruleset identity、Clocktower/Werewolf FlowPlanner 与多剧本扩展边界。R5.5 已完成 structural foundation；不要重建第二套 framework。

### C. 关键 rollout / correctness evidence

- [`R6_DRUNK_POISON_CORRECTNESS_HOTFIX_HANDOFF_2026-08-22.md`](R6_DRUNK_POISON_CORRECTNESS_HOTFIX_HANDOFF_2026-08-22.md) — PR #28 mechanical ability-functioning correctness。
- [`post_p1_production_rollout_entry_audit_2026-08-21.md`](post_p1_production_rollout_entry_audit_2026-08-21.md) — production authority / persistence / rollout dependency 入口审计。
- [`r6_p1_1_closeout_2026-08-21.md`](r6_p1_1_closeout_2026-08-21.md) — P1.1 Spy Grimoire truth boundary。
- [`r6_p1_2_closeout_2026-08-21.md`](r6_p1_2_closeout_2026-08-21.md) — P1.2 Global timeline prerequisite。
- [`r6_p1_3_closeout_2026-08-21.md`](r6_p1_3_closeout_2026-08-21.md) — P1.3 knowledge-safe boundary。

### D. correctness / oracle reference

- [`unified_semantic_model.md`](unified_semantic_model.md) — A1/A1.1 unified semantic model。
- [`external_solver_evaluation.md`](external_solver_evaluation.md) — external solver/research 使用边界。
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — Trouble Brewing semantic corpus / oracle applicability。
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) — ASP / real Clingo 交叉验证基线。
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md) — ZDD 仍为 shadow/prototype；未获 production promotion。

### E. 开发运行手册

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE / DEVELOPMENT OPERATIONS**  
  规定 connector 写入路径决策：完整内容可靠时 whole-file / Git Data API；出现 truncation 后停止 reconstruction；确定性大文件小 patch 使用 permanent trusted patch writer。

- [`TRUSTED_PATCH_WRITER.md`](TRUSTED_PATCH_WRITER.md) — **NORMATIVE / DEVELOPMENT OPERATIONS**  
  永久 default-branch patch primitive：owner-only PR comment trigger、exact head/blob lock、single-file patch scope、固定 Android validation、remote-head recheck、CI/R2 dispatch、禁止自修改和自动 merge。

- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — **REFERENCE / HISTORICAL MECHANICS**  
  Git Data API / temporary writer / workflow trigger 的详细机制与历史经验。普通大文件小 patch 不再新建 temporary writer；当前标准 fallback 看 `TRUSTED_PATCH_WRITER.md`。

## 3. 当前 rollout 顺序摘要

完整定义只看 roadmap；README 只保留导航摘要：

```text
Production Semantic-History Foundation       DONE / #24
        ↓
Impaired Information Semantics               DONE / #29
        ↓
new-game Global observation ownership        DONE / #27
        ↓
Storyteller Information Decision Foundation  NEXT
        ↓
Structured Manual Storyteller Information UI
        ↓
historical action + observation capture
        ↓
A3 historical multi-night exact baseline
        ↓
authoritative physical Grimoire ledger + Spy VerifiedExact
        ↓
B4 historical expansion
        ↓
revision-driven recommendation/history unification
        ↓
ZDD reconsideration
```

**当前下一 slice 不直接做完整 manual UI，也不提前进入 historical capture。**

## 4. 当前信息决策架构摘要

```text
actual world
    ↓
registration projection
    ↓
truthful result + legal information space
    ↓
impairment policy (healthy/drunk/poisoned)
    ↓
InformationDecisionContext
   ├── recommendation candidate(s)
   └── manual legal candidate(s)
    ↓
Storyteller chooses / confirms
    ↓
shared validation
    ↓
EpistemicObservationDraft
    ↓
ClocktowerGameSession / Global timeline authority
```

关键规则：

- Drunk/Poison mechanical effect：不生效；
- Drunk/Poison information：strongly prefer legal false information，balance 不主导真假；
- Spy/Recluse registration 与 impairment policy 分层；
- manual 是结构化 authority input，不是 free-text bypass；
- recommendation 是建议，不是 durable fact authority；
- manual 与 recommendation 最终都必须经过同一 validation / observation pipeline；
- UI / Host 不自行分配 Global sequence。

## 5. 下一 PR 的最小边界

下一份代码 PR：**Storyteller Information Decision Foundation**。

优先建立纯语义 seam：

```text
InformationDecisionContext
InformationDecisionSource = MANUAL | RECOMMENDATION_ACCEPTED
shared legal-result validation
hard block / soft warning
stale decision rejection
confirmed result → EpistemicObservationDraft
```

Tests-first 至少覆盖：

- manual / recommendation 选择同一合法结果 → 等价 semantic output；
- healthy illegal false → hard block；
- impaired legal unreliable choice → allowed；
- role-format / target-count invalid → hard block；
- legal-but-discouraged → soft warning；
- stale context → reject；
- 两条路径都不得绕过 impairment/registration validation；
- 两条路径都生产同一种 unbound draft；
- regression test 阻止 legacy direct recommendation path 回归。

Non-goals：完整 manual UI、history UI、Historical Action Capture、Spy/Recluse rewrite、Investigator balance tuning、broad evil-side balance、A3/B4/ZDD promotion、ML。

## 6. Multi-script 当前准确边界

```text
catalog / normalization / registry / ruleset identity / flow
    → MULTI-SCRIPT VERIFIED

advanced recommendation / Possible Worlds / role-specific epistemic semantics
    → TB-FIRST / future expansion
```

未来剧本能力分级：

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

Storyteller Decision Foundation 是未来达到 Level 2 的关键基础。

## 7. 新会话启动顺序

新会话继续开发时，请按这个顺序：

1. 读本文；
2. 读 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 如果目标涉及 connector 大文件写入，读 `TRUSTED_PATCH_WRITER.md`；
4. 读 `CURRENT_DEVELOPMENT_ROADMAP.md`；
5. 读当前 handoff；
6. 必要时读专项设计；
7. 查询 live `main`，不要假设文档中的 source baseline 就是当前 HEAD；
8. 从最新 `main` 创建新的 focused branch；
9. 按当前阶段 tests-first 开始，不把基础设施工作和产品 scope 混在一起。

## 8. 历史文档处理

历史文档可以继续保留，但：

- 任何仍写 `Impaired Information Semantics = NEXT` 的段落已经过时；
- 任何仍写 `PR #27 = PAUSED / OPEN` 的段落已经过时；
- `NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md` 已 superseded；
- 旧 recommendation-entry 设计已被 **Storyteller Information Decision Unification** 吸收；
- 历史 temporary-writer 默认路径已被 permanent writer 规范 supersede；
- 历史 `PASS / OPEN / NEXT / BLOCKED` 只代表当时状态，不得覆盖 current roadmap。

## 9. 文档维护规则

- **只有一份当前路线**：`CURRENT_DEVELOPMENT_ROADMAP.md`。
- Handoff 只服务下一次开发；阶段完成后立即更新。
- Audit 记录“发现了什么”；roadmap 记录“现在决定怎么做”。
- Specialized design 记录本阶段语义/产品边界，不维护 live branch 状态。
- 不在 README 中写当前工作 branch。
- Connector 操作遵守 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；永久 writer 的具体协议遵守 `TRUSTED_PATCH_WRITER.md`。
- 不要在新对话中重新假设“数千行文件必须用 temporary writer”，也不要在 connector 已明确截断目标文件后继续 whole-file reconstruction。
