# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-21  
> 当前基线：`main`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> R5.5 merge commit：`7add8569e2484a350f6cf1512a730e9f4db469c5`  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> R5.5 最终收尾：`r5_5_stage_close_known_limitations_2026-08-21.md`  
> R6 P1.2 收尾：`r6_p1_2_closeout_2026-08-21.md`  
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
P1.2 / P1.3 PASS; P1.1 remains OPEN
```

**当前执行点（2026-08-21）：R6 / P1 IN PROGRESS — P1.2 / P1.3 PASS，仅 P1.1 OPEN。**

R5.5 的 production 目标已经完成：

- Production Clocktower Host 的夜间步骤顺序与 eligibility 由 `ClocktowerFlowPlanner` 负责；
- Production Werewolf Judge 的步骤顺序与 eligibility 由 `WerewolfFlowPlanner` 负责；
- 旧的独立 flow-order authority 已移除；
- Trouble Brewing / No Greater Joy 已证明共用 catalog + registry + planner seam；
- Werewolf 已建立 typed board/role registry + planner；
- persistence / ruleset identity migration 已完成 R5.5 范围；
- R5.5 最终文档 head `aae5b5198c605bbd00fa064b703bb237b2f21bb9`：CI #222 SUCCESS、R2 #217 SUCCESS；
- PR #2 已合并，merge commit `7add8569e2484a350f6cf1512a730e9f4db469c5`。

R6 已完成 P1.2 timeline semantic prerequisite 与 P1.3 knowledge-safe input boundary；这些 PASS **仍不等于 production multi-night Possible Worlds 已获授权**。P1.1 Spy reminder-token truth boundary 仍 OPEN，且 production Host/Compose 尚未切换为从游戏开始即统一分配 Global timeline positions。

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
| **R6 P1.2 Timeline identity** | **PASS** | Global allocator/binding/persistence、ActionFact authority、A3/ZDD/B4 shared replay chronology 与 cross-type global uniqueness 已建立；production cutover 仍单独受保护。 |
| **R6 P1.3 Actual truth vs safe input** | **PASS** | PR #7/#8 已将 world-builder 与 player-knowledge core 从完整 Formal truth 中隔离。 |
| R6 revision-driven production expansion | P1 IN PROGRESS | 正式多夜 possible-world reasoning 仍被 P1.1 与 production Global timeline cutover guardline 阻塞。 |
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

### P1.2 Timeline identity — PASS

P1.2 已完成 semantic prerequisite 收口。核心模型现在是：

```text
TimelinePoint(globalSequence)
        ├── ActionFactTimeline
        └── Global observation history
                 ↓
     shared chronology / uniqueness
                 ↓
       knowledge + A3/ZDD/B4
```

已成立的契约包括：

- `TimelinePoint.globalSequence` 是跨 phase / round 的唯一 ordering identity；
- per-game allocator + restore cursor 已有持久化契约；
- durable observations 显式区分 `LegacyLocal / Global(TimelinePoint)`；
- Global observation log、player knowledge、A3、ZDD、B4 共用 chronology contract；
- `ActionFact.sequence` 通过 `TimelineBoundActionFact` 显式绑定 `TimelinePoint.globalSequence`；
- `FormalGameState` 显式区分 Legacy action payload 与 `Global(ActionFactTimeline)` persistence binding；
- B4 materialized Formal state 保留 Global action binding；
- action 与 observation histories 在 combined consumer 中不能共享同一 global position；
- 非空 Global actions 与非空 LegacyLocal observations 不允许被偷偷拼接，因为没有可证明的 cross-type ordering；
- timeline containers 防御 caller-owned mutable-list mutation；
- A3/ZDD/B4 已移除会覆盖 Global chronology 的本地 `round -> sequence` replay sort。

当前 evaluator 仍 time-insensitive，因此 `globalSequence` **不进入** `PlayerWorldSetIdentity`，也不被无条件塞入 recommendation stable digests。未来一旦 historical timeline position 会改变 world constraints，必须 tests-first 重新审计 identity/cache/digest contract。

#### Production cutover guardline

P1.2 PASS **不是 production rollout claim**：

- Production Host/Compose 尚未从游戏开始为所有 action/observation 使用 per-game Global allocator；
- legacy/running games 继续保持 LegacyLocal，不允许猜测历史 global positions；
- B4 仍是 isolated shadow；
- A3 仍是 setup/first-night constructor，不是 historical-state engine；
- 当前 evaluator 尚未 historically time-aware。

完整退出证据：`r6_p1_2_closeout_2026-08-21.md`。

入口与阶段历史文档仍保留：

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

**当前默认下一目标：P1.1 Spy Grimoire reminder-token truth boundary。先定义哪些 reminder tokens 属于 mechanical truth、哪些只是 storyteller/UI annotation，再决定 Spy perspective/filtering/schema 承诺；仍不要提前接 production multi-night Possible Worlds。**

R6 设计入口：

`docs/storyteller_revision_driven_dynamic_decision_engine_plan.md`

### 6.1 明天白天继续开发时的第一步

新会话直接执行：

1. 确认 `main` head 包含 R5.5 merge `7add8569...`、P1.2 source close merge `ea78b8f4...`、P1.3 merge `19b91887...` / `8f5ccc55...` 以及后续 documentation commits；
2. 读取本文件；
3. 读取 `r5_5_stage_close_known_limitations_2026-08-21.md`；
4. 如果继续 R6，读取 `r6_p1_2_closeout_2026-08-21.md` 后从 P1.1 reminder-token truth boundary 做最小 tests-first audit；
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
R6 prerequisite: P1.2 / P1.3 PASS; P1.1 remains required before multi-night production reasoning
Production Global timeline cutover: NOT AUTHORIZED / NOT YET WIRED
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
- 静默把旧 save 解释为最新同名 content；
- 从 legacy local sequence 猜测或补造 global timeline identity。

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
- `r6_p1_2_closeout_2026-08-21.md` — P1.2 timeline identity semantic exit evidence；
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

### R6 P1.2 关键验证基线

```text
R6.7 ActionFact timeline contract / PR #10:
6c254edb7c5cd5d3c429eb59e4a9623a2e5397e5

R6.8 B4 bound action timeline / PR #11:
c7b53a8bc7f7d52a0940f8b148655f67c52d6e61

R6.9 B4 observation chronology / PR #12:
b1161470df6bca30dd2801511d72c75e090cdda1

R6.10 Formal action timeline persistence / PR #13:
45861ff6ca4ce4171fddfd88bb765aec82a4d2cb

R6.11 B4 Formal Global binding / PR #14:
32e136565096dc2bf92c1bdc1ab040aae03a931f

R6.12 A3/ZDD shared replay chronology / PR #15:
06122e45e894811b19f7e110a7654c765c2314df

R6.13 cross-type timeline uniqueness / PR #16:
ea78b8f4e31e56c10d21eb62a0fc6e6c2dbb5d0a
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