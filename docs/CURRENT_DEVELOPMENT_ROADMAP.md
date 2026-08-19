# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-19  
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

## 1. 当前结论

2026-08-19 对 A0–A4.5 做了重新审计。审计推翻了“可以继续进入 Phase B”的前提：A3 存在真实规则语义缺口，A4.5 也有未满足的生命周期/持久化合同。

同时，当前 `MainActivity.kt` 已承担过多职责：Android Activity 生命周期、Compose 根界面、多游戏模式 UI、Clocktower 夜间/白天流程、持久化、revision wiring、A4/A4.5 shadow wiring 等均集中在同一大文件。继续直接在其中实施 A4.5 lifecycle 修复会提高误改和回归风险。

因此当前唯一正确的实施方向是：

```text
Phase A remediation
    ↓
A3 correctness hotfix
    ↓
MainActivity mechanical decomposition
    ↓
A2/A3 validation contract hardening
    ↓
A4.5 lifecycle hardening
    ↓
A3/A4/A4.5 regression + device gate review
    ↓
Phase A final exit review
    ↓
ONLY THEN unlock revision-driven dynamic decision plan / Phase B
```

**在 Phase A 重新退出前，不开始新的 B1/B2/B3 功能扩展，也不把 B4 shadow 变成生产依赖。**

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考仍有效；旧文档中的权威顺序文字需要以后统一，但不阻塞修复。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 基本成立；B4 前需补时间线身份细节。 |
| A2 ASP Oracle harness | CONDITIONAL PASS | Oracle 权威边界正确，但 golden catalog 内嵌 `FormalGameState` 仍使用旧 schema shape。 |
| A2.1 Golden corpus | CONDITIONAL PASS | 48 个合同仍是重要基线，但缺 poisoned Spy/Recluse + numeric registration 组合覆盖。 |
| **A3 EnumeratedWorldSet** | **REOPEN** | 已发现真实 correctness bug；旧 exit PASS 已归档。 |
| **MainActivity decomposition** | **PLANNED** | A3 hotfix 后进行纯结构拆分；禁止与状态架构/规则语义修改混做。 |
| **A4 ZDD prototype** | **IN PROGRESS** | 仍为 exact shadow/prototype；设备性能门槛未完成，而且共享 A3 evaluator bug。 |
| **A4.5 observation cache rebuild** | **REOPEN** | 核心 rebuild 架构可保留，但 durability、cancellation/invalidation、cache invariant 未完全满足原 spec。 |
| B1+ | BLOCKED | 等 Phase A final exit。 |

## 3. P0 — 必须先修的 correctness 问题

### P0.1 Poisoned Spy/Recluse 在 Chef/Empath 数字信息中仍可特殊登记

当前 `TroubleBrewingWorldObservationEvaluator.registrationMatch()` 会在 Role/Type/Alignment 类查询中检查被登记角色自身的 ability state；但是 `evaluateNumeric()` 使用的 `alignmentOptions()` 没有做同样检查。

这会允许：

```text
poisoned Spy     -> Chef / Empath 仍可按 GOOD 登记
poisoned Recluse -> Chef / Empath 仍可按 EVIL 登记
```

修复要求：

1. `alignmentOptions()` 只有在 Spy/Recluse 自身 `AbilityState.FUNCTIONING` 时才能增加 special-registration branch。
2. 保留实际 alignment 分支；中毒只关闭角色能力产生的可选登记，不改变真实身份。
3. 增加 Chef + poisoned Spy、Chef + poisoned Recluse、Empath + poisoned Spy、Empath + poisoned Recluse 回归测试。
4. 将这些组合加入 official golden corpus；可被冻结外部实现忠实表达的部分必须做独立 cross-validation。
5. 修复后重新跑 Enumerated/ZDD differential；“A3 == A4”不能替代 official/golden 独立验证。

### P0.2 A2 fixture schema 与当前 A1 schema-v2 不一致

当前 catalog envelope 是 schema v2，但其中 `formalStates[*].schemaVersion` 仍是旧 v1 shape，Python harness 甚至显式要求该值。

修复要求：

1. 将 A2/A2.1 nested FormalGameState fixture 迁移到当前 schema-v2。
2. fixture 必须能通过当前 typed semantic decoder/adapter，而不是由 Python 维护一套“近似 A1”结构。
3. schema-v1 必须继续 fail closed，不允许为了兼容测试重新静默接受。
4. 更新 canonical hash 和 Oracle baseline report。

### P0.3 A3 official golden runner 需要真正覆盖 enumerator

当前 A3 golden runner 的多数场景是从 JSON actual assignment 直接创建单个 `EnumeratedWorld`，然后验证 evaluator。它不能证明：

```text
PlayerKnowledgeSnapshot
  -> TroubleBrewingWorldEnumerator
  -> complete possible worlds
  -> official observation contract
```

修复要求：

- 保留 evaluator-level tests；
- 新增至少一条 typed end-to-end golden path，直接经过 `TroubleBrewingWorldEnumerator`；
- setup、Drunk identity alternative、hidden Baron、Poisoner target、red herring、registration-sensitive info 至少有代表性场景贯穿完整路径；
- 只有通过该路径后才能重新签署 A3 exit review。

## 4. P0 — MainActivity 结构性拆分

### P0.S1 为什么在 A4.5 前拆分

当前 `MainActivity.kt` 已同时承担过多不同层次的职责。A4.5 下一步恰好需要修改 persistence、lifecycle cancellation、revision invalidation 和 coroutine wiring，如果继续直接叠加在大文件中，会显著增加以下风险：

- 小范围修改需要整体重写超大文件；
- Clocktower 改动误伤狼人杀/谁是卧底 UI；
- Compose UI 与领域状态/持久化逻辑难以区分；
- lifecycle/persistence 修复与界面重构混杂后难以定位回归来源；
- GitHub connector 对大单体文件的小差异修改不够理想。

因此在 A3 最小 correctness hotfix 之后、A4.5 lifecycle hardening 之前，安排一次**纯结构性 mechanical extraction**。

### P0.S2 第一轮目标结构

第一轮只要求形成清晰职责边界，不追求一次完成最终架构。目标可等价于：

```text
MainActivity.kt
    Android Activity / window / setContent only

CampBoardGameHostApp.kt
    app root / top-level game navigation

undercover/
    UndercoverScreen.kt

werewolf/
    WerewolfHostScreen.kt

clocktower/ui/
    ClocktowerHostScreen.kt
    ClocktowerSetupScreen.kt
    ClocktowerNightScreen.kt
    ClocktowerDayScreen.kt
    ClocktowerHistoryScreen.kt

clocktower/persistence/
    ClocktowerGamePersistence.kt   (仅在可机械抽取且不改变语义时)
```

实际文件名可按现有代码边界调整。第一轮目标约为 8–12 个职责明确的文件，而不是拆成大量几十行的小文件。

### P0.S3 第一轮严格禁止事项

本阶段是 **behavior-preserving structural refactor**。禁止同时做以下改动：

- 不修 Clocktower 规则行为；
- 不改变 `gameStateRevision` / `playerInputRevision` 增加时机；
- 不改变 observation append/persistence 顺序；
- 不改变 recommendation key、cache key 或 hash；
- 不改变 A4/A4.5 rollout；
- 不引入新的 ViewModel/Redux/MVI 状态架构；
- 不把 Compose mutable state 迁移为新的领域状态模型；
- 不改持久化 schema；
- 不顺便清理业务逻辑或重命名大批领域概念；
- 不改变任何用户可见流程、文案或推荐结果。

允许的操作主要是：

- 移动顶层/Composable/helper function 到职责明确的新 `.kt` 文件；
- 机械调整 visibility/import；
- 为了拆文件添加最小参数传递或轻量参数容器，但不得改变状态拥有者；
- 抽离无状态 UI helper；
- 抽离已经是纯函数的 formatting/mapping helper。

### P0.S4 为什么暂时不同时引入 ViewModel

长期目标仍然应该从：

```text
Compose mutable state + callbacks
```

演进为：

```text
Compose UI
  -> controller / ViewModel
  -> immutable session/domain state
  -> rules / recommendation / epistemic engine
```

但这属于第二阶段架构重构，不与本次文件拆分混做。先完成零行为变化的 mechanical extraction，后续才能更安全地判断哪些状态真正应该进入 `ClocktowerSessionController` / ViewModel。

## 5. P0 — A4.5 合同修复

A4.5 的 complete-log rebuild、recipient isolation、sequential build、stale commit、OOM != UNSAT 等设计保留，但以下合同必须补齐。

### P0.4 Durable-before-build

原 spec 要求 observation 已经 durable append 后才能捕获 rebuild request。当前 UI 先更新 Compose 内存状态并捕获 request，SharedPreferences persistence 随后由 `SideEffect` 完成。

要求调整为明确顺序：

```text
append / deduplicate observation
  -> persist active game state successfully
  -> bump/capture revisions consistently
  -> capture immutable rebuild request
  -> Dispatchers.Default execute
```

如果持久化失败，不得把该 observation 当作可重放的 cache rebuild 基础。

### P0.5 Coroutine/lifecycle cancellation 必须进入 executor

`A4ObservationCacheRebuildExecutor.execute()` 已支持 `isCancelled`，但真实 UI 调用没有传入 coroutine/lifecycle cancellation。

要求：

- UI worker 将 coroutine active/cancel state 映射到 `isCancelled`；
- 离开 game、restart、role reassignment、player-count 改变或 revision supersede 时主动使旧 generation 失效；
- 已经运行的 exact build 可以结束，但结果只能成为 `STALE/CANCELLED`，不得继续写为 current cache；
- 不依赖“恰好会启动下一次 rebuild”来使旧 generation 失效。

### P0.6 Cache generation invariant 加固

`commitIfCurrent()` 除了 gameId/current generation，还应验证 key 与 scope 的结构一致性，至少包括：

- `gameStateRevision`；
- `formalSnapshotId`；
- `rollout`；
- 其他属于 generation scope 且能从 key 验证的字段。

builder 返回的 recipient、knowledge ID、hypothesis、world-set identity 校验继续保留。

### P0.7 Heap telemetry 命名/实现一致

A4.5 report 当前名为 `coarseMaxHeapDeltaBytes`，但 executor 只计算结束时 heap 与开始时 heap 的差值。

二选一：

- 真正在阶段间采样并维护 coarse maximum；或
- 改名为 `coarseEndHeapDeltaBytes`，避免把 end delta 当 peak。

性能数据不得作为 retained-size 结论。

### P0.8 补齐直接 acceptance tests

需要确保至少有直接 executor/integration 证据覆盖：

- wrong recipient / knowledge / hypothesis / identity -> FAILED, no write；
- production recommendation 在 shadow cache ready 与 absent 两种情况下结果完全一致；
- leave/restart/revision cancellation 的真实 wiring；
- durable-before-build 顺序。

## 6. P1 — Phase B/B4 前必须解决的语义债务

这些问题不阻止当前 P0 修复，但在正式多夜 Possible Worlds 前必须完成。

### P1.1 Spy Grimoire reminder tokens

`GrimoireState` schema 已包含 reminder tokens，但 world evaluator 当前主要验证 displayed role/alive。正式使用 Spy perspective 前必须决定哪些 reminder token 属于机械 truth，并让 world filtering 与 schema 承诺一致。

### P1.2 Observation timeline identity

当前多处 canonical order 主要按：

```text
round -> sequence -> id
```

进入多 phase/multi-night 后必须有唯一明确的全局顺序；不能让不同 phase 的相同 sequence 最后由 recordId 决定历史顺序。

在 B4 前统一：

- phase order；
- round；
- global/monotonic sequence 或等价 TimelinePoint；
- knowledge identity 中哪些时间字段必须参与 hash。

### P1.3 FormalGameState actual truth 与 structural world-builder seam

`FormalGameState` 规范上代表说书人真实世界，包含真实 poisoned status；但 A4 UI seam 为避免泄露 player-world secret，目前构造 structural snapshot 时会隐藏实际 poison target。

正式 Phase B 前需要把这两个概念拆清楚：

```text
Actual FormalGameState (storyteller truth)
vs
Player-world construction input (knowledge-safe structural facts)
```

不能靠把真实字段传 `null` 来长期表达两种不同语义。

## 7. 实施批次

### R1 — A3 registration correctness

范围：P0.1 + golden regression。

说明：这是最小 correctness hotfix，应在 MainActivity 拆分前完成，因为它位于 epistemic evaluator，不依赖 UI 大文件重构。

退出条件：

- poisoned Spy/Recluse numeric registration 回归全部通过；
- official expectation 明确；
- A3/A4 differential 无新差异；
- 无 cap/OOM 被翻译为 UNSAT。

### R2 — MainActivity mechanical decomposition

范围：P0.S1–P0.S4。

目标：降低后续 A4.5 lifecycle/persistence 修改风险，并改善 GitHub connector 对日常小范围代码修改的可维护性。

实施原则：

1. 先盘点 `MainActivity.kt` 内所有顶层函数、Composable、remember state、SideEffect/LaunchedEffect、持久化 helper 和各游戏模式入口。
2. 先抽**无状态 UI / helper**，再抽各游戏模式 screen；不优先移动 state ownership。
3. Clocktower UI 按 setup / night / day / history 等自然边界拆分，但避免一次创建过多文件。
4. 每一个小批次只做移动、visibility/import/参数机械调整，并立即运行相关编译/单测。
5. 结构拆分完成后再执行全量回归；只有行为零变化才能退出 R2。

退出条件：

- `MainActivity.kt` 只保留 Activity/window/setContent 和必要顶层 app bootstrap，或接近这一目标；
- 三种游戏模式仍能从同一路径进入，用户可见流程不变；
- Clocktower setup/night/day/history 的主要 UI 已不再全部堆在 `MainActivity.kt`；
- revision、persistence、recommendation、A4/A4.5 行为与拆分前一致；
- 不新增/删除任何正式规则分支；
- focused tests 通过；
- full `testDebugUnitTest` 通过；
- `git diff --check` 通过；
- 若 CI 可用，CI 必须通过；
- 对关键 Clocktower happy path 做至少一次编译/运行级 smoke 验证。

**R2 退出不代表最终架构完成。** ViewModel/SessionController/immutable state ownership 重构应在后续单独规划，不得偷偷并入 R2。

### R3 — A2/A3 validation contract

范围：P0.2 + P0.3。

退出条件：

- fixture schema-v2 一致；
- typed end-to-end enumerator golden path 存在；
- Oracle mismatch 分类重新生成；
- `UNEXPLAINED_MISMATCH = 0`、`NOT_RUN = 0` 才能申请 A3 re-exit。

### R4 — A4.5 lifecycle hardening

范围：P0.4–P0.8。

前置条件：R2 已完成，避免 persistence/lifecycle 修复继续堆入超大 `MainActivity.kt`。

退出条件：

- durable-before-build 可测试；
- leave/restart/revision 可立即失效旧 generation；
- cache scope invariant fail closed；
- OOM/failure/cancel/stale 均不产生 empty logical result/UNSAT；
- production recommendation 完全不读取 shadow cache。

### R5 — Phase A re-exit

执行：

1. focused epistemic tests；
2. full `testDebugUnitTest`；
3. Python ASP Oracle tests + real Clingo baseline；
4. `git diff --check`；
5. A4 target-device measurements（仅在 correctness 通过后）；
6. 新建一份 Phase A exit review，旧 A3 PASS 不恢复原文件。

A4 仍只有在目标设备 correctness、latency、memory 和 degradation gates 全部满足后，才可以讨论 `ZDD_DEVICE_VALIDATED`。

### R6 — Unlock dynamic decision engine

只有 R5 通过后，`storyteller_revision_driven_dynamic_decision_engine_plan.md` 才从 `BLOCKED` 变为 `READY`。

开始前重新核对该计划的 Batch 0 输入是否仍与最新 `GameSnapshot`、revision、observation log 和 PlayerWorldSet seam 一致；若 Phase A 修复改变公共语义，先更新计划再写代码。

## 8. Phase A 最终退出条件

必须同时满足：

- A3 known correctness defect 已修复且有 official/golden regression；
- MainActivity 第一轮 mechanical decomposition 已完成且行为零变化；
- A2 fixture 使用当前 schema-v2；
- A3 至少有代表性 official golden 端到端经过真实 enumerator；
- 所有已运行 Oracle 差异已分类，无 unexplained mismatch；
- A4 ZDD 与修复后的 A3 在适用 exact 场景保持一致；
- A4.5 durability/cancellation/cache identity contract 通过；
- shadow cache 不影响 production recommendation；
- 资源失败不被解释为 UNSAT；
- 多夜尚未支持的边界明确 defer，而不是 false UNSAT；
- 完整回归通过；
- 若要推广 ZDD，目标设备 gate 另外通过。

## 9. 生产保护线

在本路线另行更新前：

```text
Production recommendation engine: existing production path
A3 enumerator: correctness/debug baseline only
A4 ZDD: shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
```

任何性能优化或结构重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 的规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已经展示/提交的决定；
- 借“拆文件”名义改变 revision、persistence 或 recommendation 语义。

## 10. 文档状态维护

后续只在本文更新阶段状态。完成 R1/R2/R3/R4 时不要再创建新的并列“最终路线”。专项实现细节可以追加到对应 spec/status log，但“下一步做什么”始终回到本文。
