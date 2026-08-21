# CampBoardGameHost 文档入口

> 最后整理：2026-08-21  
> **任何新的开发或审计任务都应先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> 不在 README 中硬编码“当前开发 branch”；所有 source work 都应从最新 `main` 创建短生命周期 branch。

## 1. 文档权威与冲突处理

当文档之间出现冲突时：

1. **游戏规则正确性**：官方 Blood on the Clocktower 规则 / Almanac / published rulings 优先；项目 golden expectation 次之；外部 Oracle 只用于交叉验证。
2. **当前开发状态和“下一步做什么”**：`CURRENT_DEVELOPMENT_ROADMAP.md` 是唯一状态权威。
3. **Possible Worlds / 玩家认知一致性总体架构**：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` 是主规范。
4. **多剧本 / 动态流程架构**：`多剧本多板子与动态游戏流程架构设计_v1.md` 是 R5.5 以后继续生效的专项规范；R5.5 已验证 structural/flow foundation。
5. **专项 audit / closeout / handoff**：保存证据与局部上下文，不覆盖 roadmap 的当前状态。
6. **历史 handoff**：如果顶部标记 `SUPERSEDED` / `HISTORICAL ONLY`，不得执行正文中的旧“下一步”指令。
7. **开发运行手册**：说明可靠的开发/connector 操作方式，不改变产品架构。
8. **`archive/`**：仅历史追溯，不作为新代码实施入口。

## 2. 当前必须阅读

### A. 当前路线

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  当前状态：R6 P1 CLOSED；post-P1 production-rollout entry audit 已完成；下一 source slice 是 **Production Semantic-History Foundation**。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md) — **CURRENT HANDOFF**  
  把当前 audit 结论转换成下一次 tests-first source implementation 的边界、non-goals 与 gate。

- [`post_p1_production_rollout_entry_audit_2026-08-21.md`](post_p1_production_rollout_entry_audit_2026-08-21.md) — **CURRENT AUDIT EVIDENCE**  
  记录 production timeline/session ownership、Spy truth、A3/ZDD/B4 readiness、persistence/migration 与 rollout dependency。

### B. 主架构规范

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — **NORMATIVE**  
  Possible Worlds、玩家知识边界、registration、Oracle 权威和 recommendation policy 总体架构。

- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md) — **NORMATIVE / FOUNDATION IMPLEMENTED**  
  Catalog、registry、ruleset identity、Clocktower/Werewolf FlowPlanner 与多剧本/多板子扩展边界。R5.5 已完成 production flow foundation；不要重新建设第二套 framework。

### C. 当前 rollout / recommendation 设计

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md) — **NORMATIVE FOLLOW-UP PLAN**  
  其中顶部较早的 `BLOCKED BY R5 + R5.5` 状态是历史 gate；R5/R5.5 已完成。该计划现在必须服从当前 roadmap 的 rollout dependency：先完成 semantic-history/session ownership，再进行更广泛的 revision-driven recommendation unification。不要按旧 header 重新打开 R5/R5.5。

- [`r5_5_stage_close_known_limitations_2026-08-21.md`](r5_5_stage_close_known_limitations_2026-08-21.md) — **REFERENCE / RELEASE LIMITATIONS**  
  R5.5 closeout 与 recommendation UI transition。2026-08-21 已补充：legacy direct recommendation button/path 是 **Production Recommendation Entry-Point Unification** 的 authority debt，不是 cosmetic label 问题。

### D. R6 P1 closeout evidence

- [`r6_p1_1_closeout_2026-08-21.md`](r6_p1_1_closeout_2026-08-21.md) — P1.1 Spy Grimoire truth boundary。
- [`r6_p1_2_closeout_2026-08-21.md`](r6_p1_2_closeout_2026-08-21.md) — P1.2 Global timeline semantic prerequisite。
- [`r6_p1_3_closeout_2026-08-21.md`](r6_p1_3_closeout_2026-08-21.md) — P1.3 knowledge-safe boundary；§7–§8 的旧 OPEN/next-step 内容已明确标成历史状态。

### E. correctness / oracle reference

- [`unified_semantic_model.md`](unified_semantic_model.md) — A1/A1.1 unified semantic model。
- [`external_solver_evaluation.md`](external_solver_evaluation.md) — external solver/research 冻结与使用边界。
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md) — 52-contract Trouble Brewing corpus；当前 **24 oracle/A3 executable + 28 deferred/`ORACLE_NOT_APPLICABLE`**，两者不是简单“验证百分比”。
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) — ASP / real Clingo 交叉验证基线。
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md) — ZDD 工程证据；当前仍 shadow/prototype，device gate 未通过。

### F. design review / audit

- [`design_plan_audit_2026-08-21.md`](design_plan_audit_2026-08-21.md) — 全设计/开发计划审计与后续 disposition；原始 finding 保留，review clarification 记录在文末。

### G. 开发运行手册

- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — **REFERENCE / DEVELOPMENT OPERATIONS**。

## 3. 当前 rollout 顺序摘要

完整定义只看 roadmap；README 只保留导航摘要：

```text
Production Semantic-History Foundation
        ↓
new-game Global observation ownership
        ↓
Production Recommendation Entry-Point Unification
        ↓
historical action + observation capture
        ↓
A3 historical multi-night exact baseline
        ↓
authoritative physical Grimoire ledger + Spy VerifiedExact
        ↓
B4 historical expansion
        ↓
revision-driven recommendation unification
        ↓
ZDD reconsideration
```

当前第一 slice **不改 production Host/Compose behavior**。

## 4. Multi-script 当前准确边界

不要再用“multi-script 仍只是 scaffold”描述当前状态。

```text
catalog / normalization / registry / ruleset identity / flow
    → MULTI-SCRIPT VERIFIED (TB + NGJ structural proof)

advanced recommendation metadata / Possible Worlds / role-specific epistemic semantics
    → TB-FIRST / future expansion
```

这意味着未来扩展 NGJ/其他剧本时，应复用现有 framework，而不是创建 `MultiScriptManager`、第二套 catalog 或 script-specific FlowPlanner 主干。

## 5. 历史文档

历史过程文档可以保留在 `docs/` 或 `archive/` 供追溯，但有两个规则：

- 已失效的 handoff 必须在顶部显示 `SUPERSEDED / HISTORICAL ONLY`；
- 历史正文中的 `PASS / OPEN / NEXT / BLOCKED` 只代表当时状态，不得覆盖 roadmap。

特别是：

- `r5_5_multiscript_progress_handoff_2026-08-20.md` 已 superseded；不要继续旧 `codex/storyteller-algorithm-v4` 或 PR #2 Draft 指令。
- `r6_p1_3_closeout_2026-08-21.md` 的旧 P1.1/P1.2 OPEN 段落是 P1.3 close 当时的历史记录；当前 P1 已 CLOSED。

## 6. 文档维护规则

- **只有一份当前路线**：`CURRENT_DEVELOPMENT_ROADMAP.md`。
- 主规范只在架构/语义发生变化时升级版本，不记录每日进度。
- Audit 记录“发现了什么”；roadmap 记录“现在决定怎么做”。
- Closeout 保留退出证据；不要为了更新状态改写当时历史，使用 banner 指向当前 roadmap。
- Handoff 只服务下一次开发；一旦失效必须标记 superseded。
- Deferred decision 必须写清 **current decision + reopen trigger**，避免“以后再说”被永久遗忘。
- 不再把当前开发 branch 写进 README；每次 source work 都从最新 `main` 创建短生命周期 branch。
