# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-21  
> 当前基线：`main`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> R5.5 merge commit：`7add8569e2484a350f6cf1512a730e9f4db469c5`  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> R5.5 最终收尾：`r5_5_stage_close_known_limitations_2026-08-21.md`  
> R6 P1.3 收尾：`r6_p1_3_closeout_2026-08-21.md`  
> 历史 R5.5 交接：`r5_5_multiscript_progress_handoff_2026-08-20.md`（**仅历史参考，不得按其中 S1.3 / Draft PR 指令恢复开发**）  
> Phase A 退出评审：`phase_a_exit_review_2026-08-20.md`

## 1. 当前结论

Phase A remediation 与 R5.5 已完成并正式合并：

```text
R1  A3 correctness hotfix
  ↓
R2  MainActivity mechanical decomposition
  ↓
R3  A2/A3 validation contract hardening
  ↓
R4  A4.5 lifecycle / durability / cache hardening
  ↓
R5  A3/A4/A4.5 correctness re-exit
  ↓
R5.5 Script & Dynamic Flow Foundation / production flow cutover
  ↓
PR #2 merged to main
  ↓
R6 P1 prerequisite hardening
  ↓
P1.3 PASS; P1.1 / P1.2 remain OPEN
```

**当前执行点（2026-08-21）：R6 / P1 IN PROGRESS — P1.3 PASS，P1.1 / P1.2 OPEN。**

R5.5 的 production 目标已经完成：

- Production Clocktower Host 的夜间步骤顺序与 eligibility 由 `ClocktowerFlowPlanner` 负责；
- Production Werewolf Judge 的步骤顺序与 eligibility 由 `WerewolfFlowPlanner` 负责；
- 旧的独立 flow-order authority 已移除；
- Trouble Brewing / No Greater Joy 已证明共用 catalog + registry + planner seam；
- Werewolf 已建立 typed board/role registry + planner；
- persistence / ruleset identity migration 已完成 R5.5 范围；
- R5.5 最终文档 head `aae5b5198c605bbd00fa064b703bb237b2f21bb9`：CI #222 SUCCESS、R2 #217 SUCCESS；
- PR #2 已合并，merge commit `7add8569e2484a350f6cf1512a730e9f4db469c5`。

R6 已完成的 P1 hardening 包括 TimelinePoint/global allocator foundation、observation timeline migration seam、knowledge chronology，以及 P1.3 knowledge-safe input boundary；这些基础工作**尚未授权 production multi-night Possible Worlds**。

**不要继续在 `codex/storyteller-algorithm-v4` 长分支上开发。下一次开发从最新 `main` 创建新 branch。**

## 2. 当前阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；正式多夜 player-world reasoning 前仍需处理 P1。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` schema-v2 与 fail-closed typed decoder 已验证。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 total；24 Clingo executable；`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| A3 EnumeratedWorldSet | PASS / R5 RE-EXIT VALIDATED | exact correctness baseline。 |
| MainActivity decomposition | PASS | Activity shell、三游戏边界、Clocktower setup/day/night/host/history 已机械拆分。 |
| A4 ZDD prototype | PASS AS EXACT SHADOW / NOT DEVICE-VALIDATED | correctness differential 通过；production selector 不读取 shadow。 |
| A4.5 observation cache rebuild | PASS / R4 VALIDATED | lifecycle/durability/cache correctness 已验证。 |
| R5 Phase A re-exit | PASS | exit review 已签署。 |
| R5.5 S0 Schema/Catalog | PASS | official/custom JSON normalization、typed validation、canonical assets。 |
| R5.5 S1 Clocktower FlowPlanner | PASS | resolved facts / conditional interactions / stable identities 已建立并接 production。 |
| R5.5 S2 NGJ second-script proof | PASS | real second-script structural proof。 |
| R5.5 S3 Werewolf registry/planner | PASS | typed registry/board/planner 已接 production。 |
| R5.5 S4 persistence/ruleset identity | PASS | schema v2 + explicit migration / fail-closed identity。 |
| R5.5 S5 production cutover/regression | PASS | planner cutover、legacy flow authority removal、persistence regression、CI/R2。 |
| **R5.5 release** | **CLOSED / MERGED** | PR #2 merged to `main`。 |
| R6 P1.2 Observation timeline identity | IN PROGRESS | Global timeline foundation、allocator/restore、observation binding、knowledge chronology 已建立；ActionFact shared authority 与 time-aware replay migration 仍需收口。 |
| **R6 P1.3 Actual truth vs safe input** | **PASS** | PR #7/#8 已将 world-builder 与 player-knowledge core 从完整 Formal truth 中隔离。 |
| R6 revision-driven production expansion | P1 IN PROGRESS | 正式多夜 possible-world reasoning 仍被 P1.1 / P1.2 阻塞。 |
| Recommendation-information UI migration | DEFERRED / KNOWN LIMITATION | 不属于 R5.5 blocker；后续单独完成。 |
| 2026-08-22 real-game field validation | PLANNED | 实战发现作为后续输入；只有核心 rules/flow/persistence/state defect 才重新打开 R5.5 correctness boundary。 |

## 3. R5.5 形成的长期架构边界

### 3.1 Clocktower

```text
script asset / imported JSON
        ↓
parser + normalizer
        ↓
ClocktowerCharacterRegistry + ClocktowerScriptCatalog
        ↓
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteraction
        ↓
existing Host UI adapters
```

新增剧本不应要求：

- 为 script name 在 `ClocktowerFlowPlanner` 主干添加 `if/when`；
- 新建第二套 catalog / loader / planner；
- 让 Compose UI 决定“下一个角色是谁”。

复杂规则继续留在 typed Kotlin rule/handler layer，不构建通用 JSON rules DSL，也不从 ability text 猜 behavior。

### 3.2 Werewolf

```text
all dealt cards
→ WerewolfRoleRegistry fail-closed mapping
→ normalized role deck / WerewolfBoardDefinition
→ WerewolfFlowPlanner
→ existing WerewolfJudgeStep UI adapter
```

不得重新引入：

- `cards.any(Role.X)` + manual `buildList` 作为 production step authority；
- fixed JudgeStep list 作为未来多板子扩展方式。

### 3.3 Legacy UI adapter 与 legacy flow authority 必须区分

R5.5 删除的是**能独立决定 flow order / eligibility 的旧 authority**，不是机械删除所有 legacy-named adapter。

当前仍可存在：

- `ClocktowerNightAction` UI/action adapter；
- `WerewolfJudgeStep` UI adapter；
- same-night Imp succession confirmation lifecycle；
- display/record compatibility paths。

这些 adapter **不得重新拥有顺序或 eligibility authority**。

## 4. 已知限制：Clocktower 信息推荐 / 展示 UI

2026-08-21 实机审计确认，信息 recommendation/presentation 仍是过渡态：

- unified candidate-pool projection 当前主要覆盖 first-night information；
- later-night information family 仍可能走 legacy/fallback display path；
- automatic mode 下后续夜可能只看到既有“展示给玩家”动作，而不是最终预期的 recommendation UX；
- manual mode 下可能出现 migration-oriented / `legacy` 类选项与“展示给玩家”并存。

这不是 FlowPlanner 顺序错误，也不是第二套 flow-order authority；它属于 information-decision / presentation migration 尚未完成。

### 4.1 当前处理决定

**暂不在 R5.5 收尾阶段修复。**

原因：

- recommendation-information 算法本身仍在继续开发；
- 临近 2026-08-22 实战，不应为了 UI 完整度扩大 production Host 改动；
- 隐藏 `legacy` label 不能解决两套 information presentation path 的根因。

后续修复应以一个统一 semantic model 收口：

```text
information interaction
→ candidate generation / fixed observation
→ AUTO or MANUAL selection policy
→ one committed player-visible statement
→ one display lifecycle
```

不要只做按钮层 cosmetic patch。

详见：`r5_5_stage_close_known_limitations_2026-08-21.md`。

## 5. P1 — 正式多夜 Possible Worlds / R6 深化前必须解决

这些语义债务不重新打开 Phase A 或 R5.5，但在 production 化多夜 player-world reasoning 前必须处理。

### P1.1 Spy Grimoire reminder tokens — OPEN

`GrimoireState` 已包含 reminder tokens；正式使用 Spy perspective 前要确定哪些 token 属于 mechanical truth，并与 filtering/schema 承诺一致。

### P1.2 Observation timeline identity — IN PROGRESS

原始问题是部分 canonical order 依赖：

```text
round -> sequence -> id
```

R6 已完成：

- `TimelinePoint.globalSequence` 类型、ordering 与 JSON identity contract；
- schema-v2 legacy TimelinePoint fail-closed compatibility hardening；
- per-game global timeline allocator + restore contract；
- durable observation 的 `LegacyLocal / Global(TimelinePoint)` migration seam；
- Global observation log 的全局排序、duplicate/mixed-mode fail-closed；
- player-knowledge chronology 保留 global ordering；
- 当前 evaluator 仍 time-insensitive，因此 `globalSequence` 暂不进入 `PlayerWorldSetIdentity`，避免制造假的 time-aware cache contract。

P1.2 **仍为 OPEN**，下一步重点不是重写 reducer，而是把已经全局唯一的 `ActionFact.sequence` 明确绑定到共享 TimelinePoint/global authority，并在未来引入 historically time-aware evaluation 前迁移 A3/ZDD/B4 的 legacy replay sort。

入口与阶段文档见：

- `r6_p1_entry_audit_2026-08-21.md`；
- `r6_p1_2_observation_timeline_handoff_2026-08-21.md`；
- `r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`。

### P1.3 Actual truth vs knowledge-safe world-builder input — PASS

已建立明确类型边界：

```text
Actual FormalGameState
        ↓ one-way compatibility projection
KnowledgeSafeWorldInput
        ↓
A3/A4 world-construction core

Actual FormalGameState
        ↓ one-way compatibility projection
KnowledgeConstructionInput
        ↓
A4PlayerKnowledgeFactory core
```

安全输入不暴露 actual role / alignment / type、poison state、shown role 或 storyteller-only propositions；knowledge construction 额外只接收明确 public 的 propositions。Durable observation replay 也可仅通过 opaque formal snapshot ID 绑定，不需要把完整 Formal truth 传入 safe core。

R6.5 PR #7 merge：`19b91887344655285ec8bd93ca5bdb51bcfff445`。  
R6.6 PR #8 merge：`8f5ccc551948fea085caf8df3eb100ef67eae438`。  
完整退出证据：`r6_p1_3_closeout_2026-08-21.md`。

## 6. 下一阶段开发入口

默认软件路线仍是：

```text
post-merge main
→ new branch
→ audit R6 plan + remaining P1 prerequisites
→ tests-first / contract-first smallest vertical slice
→ exact diff audit
→ normal CI + R2
```

**当前默认下一目标：继续 P1.2，tests-first 建立 ActionFact 与 shared global timeline authority 的绑定契约；仍不要提前接 production Host。**

R6 设计入口：

`docs/storyteller_revision_driven_dynamic_decision_engine_plan.md`

### 6.1 明天白天继续开发时的第一步

新会话直接执行：

1. 确认 `main` head 包含 R5.5 merge `7add8569...`、P1.3 merge `19b91887...` / `8f5ccc55...` 以及后续 documentation commits；
2. 读取本文件；
3. 读取 `r5_5_stage_close_known_limitations_2026-08-21.md`；
4. 如果继续 R6，优先读取 P1.2 阶段文档并从 ActionFact/shared timeline 最小 contract slice 继续；
5. 创建新的 development branch；
6. 不再恢复 `codex/storyteller-algorithm-v4` 的 S1/S5 工作；
7. 行为变更继续 tests-first / contract-first。

如果明天临时决定优先改善实战 UX，也可以单独开启 recommendation-information migration branch；但不要把它伪装成 R5.5 hotfix，也不要仅隐藏 `legacy` 文案。

## 7. 2026-08-22 实战验证

实战的最高价值不是重复验证已知 `legacy/全屏展示` UI，而是验证 R5.5 真正改变的 production flow：

1. Trouble Brewing：第一夜 → 白天 → 第二夜 → 天亮，确认 planner order 与 interaction UI 连续；
2. Scarlet Woman：白天 Demon 死亡后的 next-night new Demon identity ordering；
3. Imp self-kill：same-night succession lifecycle；
4. Mayor / Ravenkeeper / Undertaker conditional interaction；
5. No Greater Joy：第一夜 → 白天 → later-night，尤其 Sage / Scarlet Woman conditional flow；
6. navigation/back/step index：无跳步、重复步、返回错位；
7. persistence/restart/restore（若当晚实际触发）；
8. 信息推荐 UX 的额外摩擦点。

分类规则：

- rules correctness / flow order / persistence / game-state defect → 高优先级 correctness follow-up；
- recommendation/presentation UX → recommendation-information migration backlog；
- 单纯已知 `legacy` label → 已记录，不重复当 blocker。

## 8. 生产保护线

在后续路线明确修改前：

```text
Production Clocktower Host flow order: Clocktower planner-backed
Production Werewolf Judge flow order: Werewolf planner-backed
Legacy UI adapters: allowed only when they do not own flow order/eligibility
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R5.5: CLOSED / MERGED
R6 prerequisite: P1.3 PASS; P1.1 / P1.2 still required before multi-night production reasoning
```

任何后续优化或重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已展示/提交决定；
- 以“多剧本准备”为名改写 production recommendation correctness；
- 以 JSON 内容化为名把复杂规则变成未经验证的通用 DSL；
- 为 No Greater Joy 新建第二套 catalog/flow framework；
- 重新引入第二套 production flow-order authority；
- 静默把旧 save 解释为最新同名 content。

## 9. GitHub / CI 开发策略

默认：

```text
read-only audit
→ tests-first / contract-first where behavior changes
→ smallest coherent source change
→ exact diff audit
→ normal PR CI
```

正常 gate：

```text
Android unit tests + debug APK
ASP contract tests
real Clingo cross-validation
R2 structural verifier
```

不要为了临时验证长期保留 one-shot workflow；不要在 `main` 上直接开展下一阶段 source work。

## 10. 历史证据与参考

R5.5 详细过程保留在：

- `r5_5_multiscript_progress_handoff_2026-08-20.md` — **历史过程文档**；其中“当前 S1.3 / PR Draft / do not merge”等措辞已经过期；
- `r5_5_stage_close_known_limitations_2026-08-21.md` — **R5.5 最终收尾与已知限制**；
- `多剧本多板子与动态游戏流程架构设计_v1.md` — 长期架构规范；
- `phase_a_exit_review_2026-08-20.md` — Phase A correctness exit；
- `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` — recommendation / knowledge 主设计；
- `r6_p1_3_closeout_2026-08-21.md` — P1.3 knowledge-safe world-builder / knowledge-construction exit evidence。

### R5.5 关键验证基线

```text
Clocktower cutover clean baseline:
948c45fd2b7cad9cf7b562aa96ceda9db886dbe6

Werewolf production cutover:
74b3efc3baca70186b982b6b1e035da870dcbc02

S5 clean source baseline:
cf324b3794feea15fcb5e7f7551d91bf1e7f181c

Pre-close green production-code baseline:
7d06bde318d91e8ad29454b63d254cf5525cbec7
CI #219 SUCCESS / R2 #214 SUCCESS

Final documentation head before merge:
aae5b5198c605bbd00fa064b703bb237b2f21bb9
CI #222 SUCCESS / R2 #217 SUCCESS

R5.5 merge commit:
7add8569e2484a350f6cf1512a730e9f4db469c5
```

### R6 P1.3 关键验证基线

```text
R6.5 world-input boundary merge:
19b91887344655285ec8bd93ca5bdb51bcfff445

R6.6 knowledge-input boundary merge:
8f5ccc551948fea085caf8df3eb100ef67eae438
```

## 11. 文档维护规则

后续只在本文更新“当前执行点”。

专项设计、退出证据、历史 handoff 保持独立，但不得创建另一个与本文并列的“当前路线”。如果历史 handoff 与本文冲突，以本文为准。
