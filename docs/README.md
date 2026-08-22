# CampBoardGameHost 文档入口

> 最后整理：2026-08-22  
> **任何新的开发或审计任务都应先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> **任何通过 ChatGPT / GitHub connector 修改代码的任务，还必须先读 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`。**  
> 不在 README 中硬编码“当前开发 branch”；所有 source work 都应从最新 `main` 创建短生命周期 branch。

## 1. 文档权威与冲突处理

当文档之间出现冲突时：

1. **游戏规则正确性**：官方 Blood on the Clocktower 规则 / Almanac / published rulings 优先；项目 golden expectation 次之；外部 Oracle 只用于交叉验证。
2. **当前开发状态和“下一步做什么”**：`CURRENT_DEVELOPMENT_ROADMAP.md` 是唯一状态权威。
3. **本轮 impaired-information / manual-storyteller 边界**：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` 是专项权威。
4. **Possible Worlds / 玩家认知一致性总体架构**：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` 是主规范。
5. **多剧本 / 动态流程架构**：`多剧本多板子与动态游戏流程架构设计_v1.md` 是 R5.5 以后继续生效的专项规范。
6. **专项 audit / closeout / handoff**：保存证据与局部上下文，不覆盖 roadmap 的当前状态。
7. **历史 handoff**：如果顶部标记 `SUPERSEDED` / `HISTORICAL ONLY`，不得执行正文中的旧“下一步”指令。
8. **本仓库 connector 默认开发策略**：`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 是项目级运行规范；它明确本仓库按单开发者模式优先 whole-file replace + SHA guard + exact diff audit。
9. **大文件 / trusted writer 详细机制**：`github_connector_large_file_editing_playbook.md` 保存 Git Data API / Actions writer 的详细 fallback 知识；若其默认路径与第 8 项冲突，以第 8 项为准。
10. **`archive/`**：仅历史追溯，不作为新代码实施入口。

## 2. 当前必须阅读

### A. 当前路线

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  当前状态：PR #24 semantic-history foundation 已合并；仓库 Public 且 GitHub Actions 已全绿；下一 source slice 是 **Impaired Information Semantics**。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md) — **CURRENT HANDOFF**  
  把实战反馈转换成下一次 tests-first impaired-information PR 的边界、non-goals、CI gate，以及 #27 的恢复条件。

- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md) — **CURRENT SPECIALIZED DESIGN**  
  定义 Drunk/Poison information semantics、balance authority 边界、registration 分层，以及 #27 后 manual storyteller + recommendation 统一 decision authority。

- [`post_p1_production_rollout_entry_audit_2026-08-21.md`](post_p1_production_rollout_entry_audit_2026-08-21.md) — **AUDIT EVIDENCE**  
  记录 production timeline/session ownership、Spy truth、A3/ZDD/B4 readiness、persistence/migration 与 rollout dependency 的入口审计证据。

### B. 主架构规范

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — **NORMATIVE**  
  Possible Worlds、玩家知识边界、registration、Oracle 权威和 recommendation policy 总体架构。新增 impaired/manual 语义由 2026-08-22 专项设计补充。

- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md) — **NORMATIVE / FOUNDATION IMPLEMENTED**  
  Catalog、registry、ruleset identity、Clocktower/Werewolf FlowPlanner 与多剧本/多板子扩展边界。R5.5 已完成 production flow foundation；不要重新建设第二套 framework。

### C. 当前 rollout / recommendation 设计

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md) — **NORMATIVE FOLLOW-UP PLAN**  
  较早的 `BLOCKED BY R5 + R5.5` header 是历史 gate；R5/R5.5 已完成。当前必须服从 roadmap：先完成 impaired-information semantics、#27 Global ownership，再进入 Storyteller Information Decision Unification。

- [`r5_5_stage_close_known_limitations_2026-08-21.md`](r5_5_stage_close_known_limitations_2026-08-21.md) — **REFERENCE / RELEASE LIMITATIONS**  
  R5.5 closeout 与旧 recommendation UI transition。其 “Recommendation Entry-Point Unification” 概念现已被更完整的 **Storyteller Information Decision Unification** 吸收。

### D. R6 correctness / closeout evidence

- [`R6_DRUNK_POISON_CORRECTNESS_HOTFIX_HANDOFF_2026-08-22.md`](R6_DRUNK_POISON_CORRECTNESS_HOTFIX_HANDOFF_2026-08-22.md) — PR #28 mechanical ability-functioning correctness 交接/证据。
- [`r6_p1_1_closeout_2026-08-21.md`](r6_p1_1_closeout_2026-08-21.md) — P1.1 Spy Grimoire truth boundary。
- [`r6_p1_2_closeout_2026-08-21.md`](r6_p1_2_closeout_2026-08-21.md) — P1.2 Global timeline semantic prerequisite。
- [`r6_p1_3_closeout_2026-08-21.md`](r6_p1_3_closeout_2026-08-21.md) — P1.3 knowledge-safe boundary；旧 OPEN/next-step 只作为历史记录。

### E. correctness / oracle reference

- [`unified_semantic_model.md`](unified_semantic_model.md) — A1/A1.1 unified semantic model。
- [`external_solver_evaluation.md`](external_solver_evaluation.md) — external solver/research 冻结与使用边界。
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — Trouble Brewing semantic corpus / oracle applicability reference。
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) — ASP / real Clingo 交叉验证基线。
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md) — ZDD 工程证据；当前仍 shadow/prototype，device gate 未通过。

### F. design review / audit

- [`design_plan_audit_2026-08-21.md`](design_plan_audit_2026-08-21.md) — 全设计/开发计划审计与后续 disposition；原始 finding 保留。

### G. 开发运行手册

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE / DEVELOPMENT OPERATIONS**。  
  本仓库当前按单开发者模式运行：只要能从目标 branch live head 可靠取得完整文件与 blob SHA，**大文件也优先 whole-file replace**；写入后用 exact diff audit 作为主要保护层。不要仅因文件大就先搭 Actions writer。

- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — **REFERENCE / FALLBACK DEVELOPMENT OPERATIONS**。  
  保存 Git Data API、temporary trusted writer、workflow trigger/base 安全语义。仅在完整文件无法安全读取/构造、存在真实同文件并发、必须 runner 先验证等例外情况下升级使用。

## 3. 当前 rollout 顺序摘要

完整定义只看 roadmap；README 只保留导航摘要：

```text
Production Semantic-History Foundation       DONE / #24
        ↓
Impaired Information Semantics               NEXT
        ↓
new-game Global observation ownership        PR #27
        ↓
Storyteller Information Decision Unification
  recommendation + manual storyteller choice
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

当前下一 slice **不做 manual UI、不恢复 #27 production wiring、不改 Spy/Recluse registration**。

## 4. 新的信息决策架构摘要

```text
actual world
    ↓
registration projection
    ↓
truthful result + legal information space
    ↓
impairment policy (healthy/drunk/poisoned)
    ↓
Storyteller Information Decision
   ├── recommendation
   └── manual legal choice
    ↓
shared validation
    ↓
EpistemicObservationDraft
    ↓
ClocktowerGameSession / global timeline
```

关键规则：

- Drunk/Poison mechanical ability effect：不生效；
- Drunk/Poison information：strongly prefer legal false information，而非由 balance 主导真假；
- balance/style 主要选择“哪一个合法 false candidate”；
- Spy/Recluse registration 与 impairment policy 分层；
- manual 是正式 authority input，不是绕过规则的 free-text bypass；
- recommendation 是建议，不是 durable fact authority。

## 5. Multi-script 当前准确边界

```text
catalog / normalization / registry / ruleset identity / flow
    → MULTI-SCRIPT VERIFIED (TB + NGJ structural proof)

advanced recommendation metadata / Possible Worlds / role-specific epistemic semantics
    → TB-FIRST / future expansion
```

未来剧本能力分级：

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

Level 2 是重要的实用里程碑：有经验的真人说书人可以在 AI recommendation 尚不成熟时正常使用新剧本。

## 6. 历史文档

历史过程文档可以保留在 `docs/` 或 `archive/` 供追溯，但：

- 已失效 handoff 必须显示 `SUPERSEDED / HISTORICAL ONLY`；
- 历史正文中的 `PASS / OPEN / NEXT / BLOCKED` 只代表当时状态，不得覆盖 roadmap。

特别是：

- `NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md` 已被 2026-08-22 handoff supersede；
- `r5_5_multiscript_progress_handoff_2026-08-20.md` 已 superseded；
- `r6_p1_3_closeout_2026-08-21.md` 的旧 P1.1/P1.2 OPEN 段落是历史记录；当前 P1 已 CLOSED。

## 7. 文档维护规则

- **只有一份当前路线**：`CURRENT_DEVELOPMENT_ROADMAP.md`。
- 主规范只在架构/语义发生大版本变化时升级版本，不记录每日进度。
- 新的专项语义可以用 dated specialized design 补充主规范，并由 roadmap 明确其 authority。
- Audit 记录“发现了什么”；roadmap 记录“现在决定怎么做”。
- Closeout 保留退出证据；不要为了更新状态改写当时历史，使用 banner 指向当前 roadmap。
- Handoff 只服务下一次开发；一旦失效必须标记 superseded。
- Deferred decision 必须写清 current decision + reopen trigger。
- 不再把当前开发 branch 写进 README；每次 source work 都从最新 `main` 创建短生命周期 branch。
- Connector 操作策略必须按 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 执行；不要在新对话中重新假设“数千行文件必须通过 temporary writer”。
