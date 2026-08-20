# 状态版本驱动的动态决策引擎：Terra 实施规范

> 状态：BLOCKED BY CURRENT ROADMAP — R5 + R5.5  
> 当前解锁条件：Phase A R5 通过 + `多剧本多板子与动态游戏流程架构设计_v1.md` 的 R5.5 Foundation 退出  
> 日期：2026-08-15
> 主规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
> 本文取代：`archive/dynamic_storyteller_decision_recommendation_implementation.md` 中面向未来的实施计划
> 相关但不取代：`storyteller_a4_5_observation_cache_rebuild_spec.md`
> 第一实施范围：Trouble Brewing；先使用现有启发式评分，Possible Worlds 以 shadow 接入

## 1. 一句话目标

把“投毒后重算首夜信息”“醉酒/中毒信息”“市长弹刀”“特殊登记”“恶魔继承”等流程统一成：

> 从一个不可变、可重放的当前状态快照生成尚未提交的决策；任何影响合法性、真值或评分的状态变化都会使旧推荐过期；已经展示或确认的结果永不被后台重算改写。

本文不是要求一次性重写全部夜间 UI。Terra 必须按第 12 节的批次逐步实施，每批独立测试、独立验收。

## 2. 当前基线与已知问题

### 2.0 与 A4.5 的执行顺序

本计划不取消 A4.5。Terra 开始 Batch 0 前必须先做一次 A4.5 completion audit：

- 如果 `storyteller_a4_5_observation_cache_rebuild_spec.md` 的 Definition of done 尚未满足，先完成 A4.5；
- 如果已经满足，只记录测试、设备日志和 rollout 状态，不重复实现；
- A4.5 与本计划 Batch 1–3 不得在同一次任务中交错修改共享 cache/revision 代码；
- A4.5 始终保持 shadow，不能作为动态引擎正确性的前置依赖。

### 2.1 必须保留的现有能力

- `GameSnapshot` 已包含 `gameId`、`gameStateRevision`、`playerInputRevision`、规则版本、决策历史和 observation log。
- `StorytellerDecisionEvent` 已保存版本、状态摘要、候选审计和结果快照。
- `DecisionEventStore.appendAtomically` 已能拒绝 revision 不匹配的提交。
- `DynamicGameState`、`DynamicDecisionRequest` 和现有动态候选/评分器已覆盖部分市长、登记与继承场景。
- 酒鬼展示身份在发身份前由 setup 推荐选定；按具体展示角色分族，并使用跨局软冷却。
- A4/A4.5 仍是五人 Trouble Brewing 的 ZDD shadow；生产推荐不得依赖 shadow cache。

### 2.2 当前实现不能继续扩展的方式

当前 `MainActivity.kt` 中仍有以下结构性风险：

1. 大量 Compose 状态直接拼成推荐输入，各入口各自决定何时重算。
2. setup 推荐 key 主要由剧本、seed 和实际角色组成，没有统一包含 phase、round、poison、protection、alive、observation/history 等完整上下文。
3. `gameStateRevision` 会因部分 UI 事件增加，但“机械状态改变”“信息已展示”“纯 UI 输入”没有清晰区分。
4. 酒鬼调查员的预发牌信息目前可能随展示身份一起锁定；有投毒者时，这会阻止投毒目标确定后的正确重算。
5. 动态规则按角色散落在 UI 中，继续增加角色特例会造成旧结果写回新状态、已展示内容被覆盖、恢复后结果漂移等问题。

因此本轮首先建立状态、版本和提交边界，再迁移角色能力；禁止从“给投毒者多加一个 `LaunchedEffect`”开始。

## 3. 术语与不可违反的边界

### 3.1 Setup committed facts

Setup 只负责开局后必须稳定、且不应因夜间目标变化而改写的事实：

- 实际角色与座位；
- 酒鬼展示身份 `DrunkShownRole`；
- 红鲱鱼；
- 恶魔伪装身份；
- 其他剧本明确要求在 setup 时确定的隐藏事实。

酒鬼的“具体首夜信息”不是永久 setup fact。它可以在发身份前预计算用于挑选展示身份，但在真正展示前仍是 provisional recommendation。

特别修正规则：

```text
DrunkShownRole                    COMMITTED once identity is shown
DrunkInvestigatorInfo             PROVISIONAL until actually shown
other Drunk first-night clue      PROVISIONAL until actually shown
```

如果没有投毒者，预计算结果通常可直接复用；如果有投毒者，目标确认后必须让所有尚未展示的首夜信息过期并重算。

### 3.2 Dynamic decision

动态决策是依赖当前状态、尚未向玩家展示或尚未由说书人确认的结果，包括：

- 可靠或不可靠信息内容；
- Spy/Recluse 的 interaction-scoped registration；
- 市长死亡转移；
- 恶魔继承；
- 角色变化后的后续行动；
- 任何需要根据最新存活、中毒、保护、已用能力或历史压力选择的说书人裁定。

玩家自行选择的目标不是推荐候选，但它是动态状态输入。例如投毒者目标、僧侣保护目标和恶魔击杀目标由玩家决定；确认后必须触发相关推荐失效。

### 3.3 Commit

以下任一动作构成 commit：

- 信息已经在玩家安全展示界面显示；
- 说书人明确确认一个裁定；
- 玩家行动步骤确认并推进；
- 规则结算已经改变角色、存活或死亡状态。

COMMITTED/APPLIED 结果只能通过显式 correction event 纠正，不能被后台推荐覆盖。

## 4. 统一状态模型

### 4.1 不可变推荐快照

新增一个不依赖 Android/Compose 的领域对象，命名可等价：

```kotlin
data class DynamicDecisionSnapshot(
    val game: GameSnapshot,
    val phase: StorytellerPhase,
    val round: Int,
    val nightOrderPosition: Int,
    val poisonTargetSeat: Int?,
    val protectedSeats: Set<Int>,
    val pendingAttackSeat: Int?,
    val spentAbilitySeats: Set<Int>,
    val committedActionFacts: List<ActionFact>,
    val lockedSetupDecisions: List<StorytellerDecision>,
    val policy: StorytellerPolicySnapshot,
)
```

字段可以按现有模型调整，但必须满足：

- 构造后不可变；
- 不包含本地化 UI 文案；
- seat 使用 1-based 整数；
- 集合和 map 能规范化排序；
- 能生成稳定 `stateDigest`；
- 能从持久化快照与事件日志完全重建；
- 不从 Compose mutable state 延迟读取。

### 4.2 revision 的正式语义

继续使用现有两个 revision，但固定其职责：

| 字段 | 增加时机 | 示例 |
|---|---|---|
| `gameStateRevision` | 已确认的机械事实或时间线改变 | 投毒目标确认、保护确认、死亡、角色变化、phase/round 推进 |
| `playerInputRevision` | 尚未确认但会改变预览候选的输入改变 | 临时改选目标、编辑锁定项、修改待确认回答 |

仅切换 UI tab、展开卡片、滚动或打开弹窗不得增加任何 revision。

信息展示本身不一定改变机械世界，但会改变 decision history 和 observation log。推荐 key 必须包含它们的 digest；不得只依靠两个数字推断上下文相同。

### 4.3 推荐 generation key

新增稳定 key，至少包含：

```kotlin
data class DynamicRecommendationKey(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val phase: StorytellerPhase,
    val round: Int,
    val decisionPointId: String,
    val stateDigest: String,
    val decisionHistoryDigest: String,
    val observationLogDigest: String,
    val lockedDecisionDigest: String,
    val policyVersion: String,
    val style: RecommendationStyle,
    val algorithmConfigVersion: String,
)
```

同一 key 必须得到同一候选 ID、排序和选择。任意字段变化都产生新的 generation；旧 generation 可以结束计算，但不得发布或提交。

## 5. 决策生命周期

统一状态机：

```text
NOT_REQUESTED
    -> GENERATING
    -> READY
    -> CONFIRMED
    -> APPLIED

GENERATING/READY
    -> SUPERSEDED   state key changed
    -> FAILED       ordinary error
    -> RESOURCE_EXHAUSTED
    -> CANCELLED
```

领域历史仍只持久化有业务意义的 `PROPOSED / CONFIRMED / APPLIED / FAILED`。`GENERATING`、`SUPERSEDED`、`CANCELLED` 是运行时状态，不写成虚假的游戏事件。

发布结果必须执行原子校验：

```text
result.key == currentKey
and result.stateDigest == currentSnapshot.stateDigest
and request decision point is still open
and no committed event already owns the idempotency key
```

任一失败都丢弃结果并标记 stale/superseded，不能让 UI 短暂显示旧信息。

## 6. 失效与重算规则

### 6.1 基本规则

状态变化后只处理尚未提交的决策：

```text
COMMITTED/APPLIED          preserve
READY but not displayed   invalidate and regenerate
GENERATING                cancel generation; late result is stale
future unopened step      generate lazily from new snapshot
```

### 6.2 必须实现的失效矩阵

| 事件 | revision | 必须失效的内容 | 必须保留 |
|---|---|---|---|
| 酒鬼展示身份完成 | game state | 依赖另一展示身份的候选 | 已展示 `DrunkShownRole` |
| 投毒者临时改选目标 | player input | 当前预览及所有下游未提交信息 | 已展示身份、已提交 setup |
| 投毒者确认目标 | game state | 本夜所有尚未展示的信息与动态裁定 | 此前已经展示的信息 |
| 僧侣确认保护 | game state | 恶魔击杀结果、市长/士兵相关死亡候选 | 已提交的早先夜间信息 |
| 恶魔确认攻击 | game state | 死亡解析、Ravenkeeper、市长、继承等下游步骤 | 已提交的上游行动 |
| 玩家死亡/处决 | game state | 邻居、存活目标、继承、胜负及以后信息 | 历史 observation/event |
| 角色变化 | game state | 所有依赖身份/能力状态的未来步骤 | 变化前已经展示的信息 |
| phase/round 推进 | game state | 前一阶段所有未提交草稿 | 已提交完整时间线 |
| 推荐风格变化 | 不改机械 revision；key 改变 | 尚未提交的排序/选择 | 合法候选与历史事实 |
| 新 observation 提交 | history/log digest | 叙事压力与认知评分依赖项 | observation 本身 |

禁止手写“投毒时只刷新被投毒者”。投毒改变全局信息压力、登记选择、候选重叠和叙事评分，因此本夜所有未提交信息都需要新 generation；生成器可以按 dependency set 做性能优化，但语义上必须等价于完整失效。

## 7. 首夜正式流程

### 7.1 没有酒鬼、没有投毒者

```text
commit role assignment
-> identity reveal
-> create first-night snapshot
-> lazily/eagerly generate unopened information decisions
-> display one item
-> commit observation
-> regenerate only if its history effect changes remaining ranking
```

### 7.2 有酒鬼、没有投毒者

```text
generate setup-level Drunk perceived-role candidates
-> verify each role has at least one viable initial ability path
-> select and commit only DrunkShownRole before identity reveal
-> create dynamic first-night snapshot with that role locked
-> generate concrete Drunk clue and all other unopened clues
```

预发牌计算可以携带 provisional clue 用于联合评分，但不得因为它与身份一起算出就把它当作已展示信息。

### 7.3 有投毒者

```text
commit setup facts and DrunkShownRole
-> reveal identities
-> Poisoner acts first
-> storyteller confirms poison target
-> gameStateRevision increments
-> discard every unopened first-night recommendation from older key
-> regenerate poisoned target information and all remaining first-night decisions
```

被投毒者可以收到真或假信息；其他健康角色仍需重算，是因为全局信息压力、登记选择、候选重叠和叙事评分可能改变，而不是因为他们的规则真值一定改变。

## 8. 统一生成管线

每个动态 decision point 必须走同一顺序：

```text
1. SnapshotFactory captures immutable current state.
2. LegalChoiceGenerator enumerates rule-legal choices.
3. OutcomeSimulator predicts direct mechanical/information outcome.
4. EpistemicGate checks recipient-world credibility when available.
5. NarrativeEvaluator scores pressure, overlap and future flexibility.
6. QualityGate classifies RECOMMENDED / WARNING / MANUAL_ONLY / INELIGIBLE.
7. WeightedStableSelector selects reproducibly within family budgets.
8. Coordinator publishes only if generation key is current.
9. UI displays/asks for confirmation.
10. Committer atomically appends event/observation and applies outcome.
```

当 B4 Possible Worlds 尚未覆盖某个 timeline 时：

- 规则合法性和现有启发式评分可以运行；
- epistemic 结果必须标记 `DEFERRED_B4`/`UNKNOWN`；
- 不得把缺少 world result 当作 UNSAT；
- AUTO 是否允许该类候选必须由显式 rollout policy 决定，不能静默放行。

## 9. 依赖关系与模块边界

建议结构；命名可以调整，职责不可合并回 UI：

```text
clocktower/domain/
  DynamicDecisionSnapshot.kt
  DynamicRecommendationKey.kt
  ActionFact.kt
  StorytellerDecisionEvent.kt

clocktower/session/
  DynamicDecisionSnapshotFactory.kt
  DynamicRecommendationCoordinator.kt
  DynamicRecommendationGenerationStore.kt
  DecisionEventStore.kt

clocktower/recommendation/dynamic/
  LegalChoiceGenerator.kt
  DynamicOutcomeSimulator.kt
  DynamicDecisionEvaluator.kt
  DynamicDecisionDependencyCatalog.kt

clocktower/epistemic/
  PlayerWorldSet.kt
  A4... shadow components
  B4 timeline components (later batch)
```

依赖方向固定为：

```text
UI -> session coordinator -> domain/recommendation interfaces
recommendation -> PlayerWorldSet interface
recommendation -X-> ZDD concrete classes
domain -X-> Android/Compose/localized strings
```

## 10. 提交、持久化与恢复

### 10.1 推荐结果不是事实

`READY` recommendation 不写 observation log，也不改变角色状态。只有用户展示/确认才提交。

### 10.2 原子提交顺序

```text
1. Re-capture current key.
2. Reject if selected result key is stale.
3. Append PROPOSED/CONFIRMED event with idempotency key.
4. If information was displayed, append exactly one EpistemicObservation.
5. Apply mechanical outcome if any.
6. Transition event to APPLIED.
7. Persist snapshot and increment the appropriate revision/digest.
8. Start the next generation.
```

失败恢复必须保证不会出现“结果已展示但 observation 没保存”或“死亡已应用但 event 仍只是 READY”。若当前存储无法提供真正事务，使用幂等 key 和可重放的 ordered reducer，恢复时补齐未完成 transition。

### 10.3 恢复要求

进程重启后：

- 从 setup facts、action facts、decision events 和 observation log 重建快照；
- 不恢复旧协程或旧 `READY` 对象；
- 使用当前 key 重新生成未提交内容；
- 已展示的酒鬼身份和信息保持原样；
- 相同 seed/config/key 的结果必须可重放。

## 11. 测试策略

### 11.1 领域单元测试

| ID | 场景 | 必须断言 |
|---|---|---|
| R1 | 相同快照重复构造 | key、digest、候选和选择完全相同 |
| R2 | 仅 UI 展开状态改变 | revision/key 不变 |
| R3 | poison target draft 改变 | input revision/key 改变，state revision 不变 |
| R4 | poison target confirm | state revision/key 改变，旧结果 stale |
| R5 | style 改变 | mechanical digest 不变，recommendation key 改变 |
| R6 | private observation 提交 | observation digest 改变；其他玩家秘密不泄漏 |
| C1 | 旧 generation 晚完成 | 不发布、不提交、不覆盖 UI |
| C2 | 同一 idempotency key 双击确认 | 只产生一个有效 event/observation |
| C3 | commit 时 revision 已变 | `StaleRequest`，无部分应用 |
| P1 | 投毒者确认首夜目标 | 所有未展示首夜推荐失效 |
| P2 | 一条信息已展示后改状态 | 已展示 observation 保留，只重算剩余项 |
| P3 | 酒鬼调查员 provisional clue | poison 后可改变；shown role 不变 |
| M1 | 市长转移到死亡/保护/士兵 | outcome 合法并可解析为无人死亡 |
| T1 | death/role change/phase advance | 下游 decision point 集合正确变化 |
| E1 | 普通异常/OOM/取消 | 不产生 UNSAT，不提交空结果 |

### 11.2 场景集成测试

至少覆盖：

1. 首夜无投毒者，无酒鬼；
2. 首夜酒鬼展示调查员，无投毒者；
3. 首夜酒鬼展示图书管理员，无投毒者；
4. 首夜有投毒者，中毒酒鬼；
5. 首夜有投毒者，中毒健康信息角色；
6. 首夜投毒目标改选两次后确认；
7. 后续夜投毒 + 共情者邻居死亡；
8. 僧侣保护 + 恶魔攻击 + 市长弹刀；
9. 小恶魔自杀 + 猩红女巫/其他爪牙继承；
10. 进程恢复后继续未完成夜晚；
11. 自动与辅助模式共享同一候选池；
12. 旧推荐计算在新 revision 后完成。

### 11.3 不变量测试

- 所有 COMMITTED/APPLIED event 的 key 在提交瞬间与当前 key 相符；
- 每次玩家信息展示对应且只对应一条结构化 observation；
- recommendation cache 的存在与否不改变确定性输出；
- sealed decision point 不再接收新 commit；
- 角色实际身份、poison target、red herring 不进入无权玩家的 knowledge snapshot；
- `FAILED`、`CANCELLED`、`RESOURCE_EXHAUSTED` 永远不等于 `UNSAT`。

## 12. Terra 实施批次

Terra 每次只执行一个 batch。完成 focused tests 和该 batch 的验收记录后才能进入下一批。

### Batch 0 — 契约测试与现状锁定

目标：不改变生产行为，先用测试暴露当前 key/revision/commit 缺口。

前置：完成并记录第 2.0 节的 A4.5 completion audit。

工作：

- 为 `GameSnapshot`、`DecisionRevision`、`DecisionEventStore` 增加 R1、C2、C3；
- 给现有首夜 recommendation key 写 characterization test；
- 记录当前哪些 UI 动作增加哪一种 revision；
- 不修改推荐评分。

建议文件：

```text
clocktower/session/*Test.kt
clocktower/domain/*Test.kt
docs/storyteller_revision_driven_dynamic_decision_engine_plan.md
```

退出条件：测试明确证明旧 key 的缺失字段；完整 JVM 测试仍通过。

#### Batch 0 execution record — 2026-08-15

Completed without changing production scoring or UI flow. Characterization tests establish the
following current baseline:

| Current action / model boundary | `gameStateRevision` | `playerInputRevision` | Batch 0 finding |
|---|---:|---:|---|
| `ClocktowerGameSession.updateGameState` with a changed `GameState` | +1 | unchanged | Correct session-path mechanical increment. |
| `ClocktowerGameSession.recordPlayerInput` | unchanged | +1 | Correct provisional-input increment. |
| `ClocktowerGameSession.recordEpistemicObservation` | unchanged | +1 | Observation is durable, but its digest is not yet part of the recommendation key. |
| Direct `GameSnapshot.copy(gameState = ...)` | unchanged | unchanged | R1 gap: the immutable value itself cannot prove a mechanical change was revisioned. |
| `DecisionEventStore.appendAtomically` | exact match required | exact match required | New commits reject either stale revision; existing idempotency keys still replay. |
| Legacy `DecisionSeedMaterial` / setup seed | included | included | Key cannot express phase, round, state digest, observation-log digest, or locked-decision digest. |

The gaps above are intentional evidence for Batch 1. Batch 1 must introduce a normalized immutable
dynamic snapshot and `DynamicRecommendationKey`; it must not reinterpret this legacy setup seed as a
complete dynamic generation key.

### Batch 1 — 不可变 snapshot、digest 与 generation store

目标：建立纯 Kotlin 基础，不接 UI。

工作：

- 新增 `DynamicDecisionSnapshot` 与规范化 digest；
- 新增 `DynamicRecommendationKey`；
- 新增 generation store，提供 `begin/current/publishIfCurrent/cancelGame`；
- ordinary failure、OOM、cancel、stale 使用不同终态；
- 所有操作 JVM 可测，不依赖 Compose。

退出条件：R1–R6、C1、E1 通过；不得改 `MainActivity` 的角色流程。

#### Batch 1 execution record — 2026-08-15

Completed as a pure Kotlin/domain slice. `DynamicDecisionSnapshot` freezes the formal game snapshot,
phase/round, night-order position, poison/protection/attack inputs, spent abilities, locked decisions
and policy configuration. Canonical state, history, observation and lock digests feed the complete
`DynamicRecommendationKey`; no localized UI text or Compose state is referenced.

`DynamicRecommendationGenerationStore` provides atomic `begin/current/publishIfCurrent/cancelGame`
semantics. Starting a newer generation marks the previous one `SUPERSEDED`; ordinary exceptions,
OOM and cancellation remain distinct `FAILED`, `RESOURCE_EXHAUSTED` and `CANCELLED` terminals.
The store retains no recommendation value and is not wired into production UI or scoring.

`DynamicRecommendationKeyTest` covers R1–R6, C1 and E1, including canonical set/lock ordering,
private-observation digest invalidation and redacted keys. Focused tests, full JVM
`testDebugUnitTest`, the 11-test ASP Oracle harness and `git diff --check` all pass.

### Batch 2 — reducer 与提交事务边界

目标：让 action/event/observation 能重建当前状态。

工作：

- 定义 typed `ActionFact`：poison、protect、attack、execution、death、role change、phase advance；
- 实现 ordered reducer；
- 将原子 stale check 与 event append、observation append、outcome apply 收敛到 committer；
- 保证幂等恢复。

退出条件：C2、C3、P2 和恢复测试通过；仍不改变生产推荐选择。

### Batch 3 — 投毒者驱动的首夜动态重算

目标：完成第一个真实纵向切片。

工作：

- Poisoner 目标选择保持“玩家行动”，不进入说书人候选；
- 临时选择只更新 input revision；步骤确认后提交 poison action 并更新 state revision；
- 首夜所有未展示信息使用新 snapshot/key 重新生成；
- 移除 `DrunkInvestigatorInfo` 的永久 identity lock，只保留 `DrunkShownRole`；
- 如果旧 provisional clue 与新状态仍相同，可以 cache hit，但必须由 exact key 证明；
- UI 显示 Loading/Ready/Error，不短暂回退到旧信息。

退出条件：P1–P3 全部通过，并完成一次无投毒者与一次有投毒者的端到端测试。

#### Batch 3 execution record — 2026-08-15

Completed as the first first-night lifecycle slice.

- Added `FirstNightPoisonLifecycle`: draft poison selection increments only `playerInputRevision`; confirmation increments `gameStateRevision`; both invalidate the outstanding generation token, and repeated confirmation is idempotent.
- The Poisoner UI now has an explicit confirmation boundary when the night step advances. A changed confirmed target increments the persisted game-state revision, while target picking remains provisional input.
- Setup/recommendation and per-information stable keys now include phase, round, state revision, input revision and poison target. Cancelled Compose generations check cancellation before publishing, so an older calculation cannot overwrite the later state.
- `DrunkShownRole` remains the only committed Drunk setup decision. `DrunkInvestigatorInfo` is cleared at deal/apply/Poisoner-confirm boundaries and is no longer locked into later setup reruns.
- Validation passed: focused P1–P3 lifecycle/transaction tests, full `./gradlew testDebugUnitTest --no-daemon`, Debug APK assembly, 11-test ASP Oracle, and `git diff --check`. End-to-end clicks without and with a Poisoner still require an attached device/emulator. Batch 3 still does not migrate every first-night information family; that is Batch 4.

### Batch 4 — 统一不可靠信息与首夜其余角色

目标：所有首夜信息不再由互相独立的 UI helper 决定生命周期。

迁移顺序：

1. 酒鬼 Washerwoman/Librarian/Investigator；
2. 酒鬼 Chef/Empath；
3. 健康或中毒的 WW/Librarian/Investigator；
4. Chef/Empath/Fortune Teller；
5. Spy/Recluse registration 与信息联合候选。

每迁移一类必须先保留旧路径做 shadow diff；候选合法集合不一致时停止，不得直接删除旧实现。

退出条件：12 个场景集成测试中的 1–6 通过；所有展示均产生 typed observation。

#### Batch 4 execution record — 2026-08-15

In progress. The shared pure-Kotlin `FirstNightInformationMigration` boundary now covers
Washerwoman, Librarian, Investigator, Chef, Empath and Fortune Teller. Each migrated candidate
set must shadow-match the legacy candidate IDs before it can become READY; any mismatch keeps the
legacy path available and prevents publication. A displayed choice is the only commit boundary and
creates one typed `AbilityObservation`; poison invalidation keeps displayed observations but drops
every unshown candidate family. Focused JVM tests cover all six families, mismatch refusal, and
Drunk/poison invalidation. The first-night UI adapter now commits these six families through the
shared lifecycle before appending its event/epistemic record, ignores a repeat display of a sealed
decision point, and invalidates unshown candidates when the Poisoner target changes. Legacy UI
candidate helpers remain in place as the shadow source. End-to-end scenarios remain to be completed
before this batch can be marked finished.

#### Batch 4 continuation record — 2026-08-16

Completed in this continuation:

- Wired the six first-night information families through `FirstNightInformationMigration` at the
  player-display boundary in `MainActivity`. A typed `AbilityObservation` is prepared before the
  existing event and epistemic-record append path runs.
- A decision point already displayed in the current first night is sealed: re-entering the step
  cannot append another information event or replace the player-visible result.
- The adapter observes Poisoner target changes during the first night and invalidates only unshown
  migration drafts. Displayed observations remain committed.
- Added the idempotent-display contract test; focused migration coverage is now four tests:
  six-family parity, mismatch refusal, Drunk/poison invalidation, and duplicate-display idempotency.

Validation completed:

```zsh
./gradlew testDebugUnitTest --no-daemon \
  --tests 'com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigrationTest' \
  --tests 'com.codex.campboardgamehost.clocktower.session.FirstNightInformationLifecycleTest'
git diff --check
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

The focused JVM run and diff check passed on 2026-08-16; the independent ASP Oracle suite passed
11/11 on 2026-08-15. Batch 4 remains **in progress**. Its remaining exit work is to feed each
legacy helper's complete legal candidate set into the UI adapter's shadow comparison and to pass
integration scenarios 1–6 (including device/emulator interaction coverage). Do not remove the
legacy helpers before those parity checks pass.

### Batch 5 — 后续夜晚、死亡与市长裁定

目标：将同一引擎扩展到状态结算，而不是增加第二套夜晚框架。

迁移顺序：

1. poison duration/换目标；
2. Monk/Soldier protection；
3. Demon attack；
4. Mayor redirect；
5. Ravenkeeper/Undertaker；
6. Imp starpass/Scarlet Woman succession；
7. role change、death、phase/round transition。

退出条件：场景 7–10 和 M1/T1 通过；重启恢复结果与连续运行一致。

#### Batch 5 execution record — 2026-08-16

Completed as a state-settlement and recovery slice.

- Poison, Monk protection, Demon attack and Mayor redirect now keep draft input separate from the confirmed mechanical fact; only confirmation advances `gameStateRevision`.
- Ravenkeeper and Undertaker player-visible role information commits through the durable epistemic observation path, so later state changes cannot rewrite what was shown.
- Death, role change, phase/round transition and Imp/Scarlet Woman succession now close a committed timeline boundary; downstream protection and pending-attack state is invalidated on phase advance.
- `ClocktowerNightCheckpoint` persists an unfinished night’s phase, revisions, step position, confirmed facts and drafts. Restore is backward compatible with the former flat save fields.
- Automated evidence: M1 Mayor outcome cases, Imp successor recommendations, T1 ordered death/role/phase reduction, subsequent-night poison/death replay, checkpoint round-trip and legacy-save compatibility. Focused regression plus full `./gradlew testDebugUnitTest --no-daemon` passed; `git diff --check` passed.

### Batch 6 — B4 PlayerWorldSet shadow 接入

目标：在动态 timeline 上增加玩家视角正确性，暂不改变生产选择。

工作：

- 扩展 FormalGameState timeline；
- observation replay 覆盖 death、poison duration、role transition；
- 对动态候选执行 before/after world 查询；
- 与 A3 baseline/golden/Oracle 做适用范围内交叉验证；
- 不支持场景返回明确 `DEFERRED_B4`。

退出条件：B4 golden matrix 通过；shadow diff 无秘密泄漏和伪 UNSAT。

#### Batch 6 execution record — 2026-08-16

Completed as a domain-only shadow integration; production recommendation selection and the A4 cache consumer remain unchanged.

- `FormalGameState` now persists a canonical ordered `ActionFact` timeline, including death, poison duration/replacement and role-transition facts; schema-v2 JSON round-trips that timeline.
- `B4DynamicPlayerWorldSetShadow` reduces the dynamic timeline, projects only public deaths into recipient knowledge, replays durable observations in timeline order, and runs exact before/after candidate world queries. Its report exposes only recipient, candidate ID and cardinalities, never a formal snapshot or secret target.
- Attack/protect resolution and role-transition world semantics are not yet exact possible-world operations. They explicitly return `DEFERRED_B4`, rather than treating an unsupported query as `UNSAT`.
- Added the B4 golden-matrix regression slice for public death replay, secret poison-target replacement, poison-target report invariance, explicit deferred role transition, and canonical JSON timeline replay for every typed action shape. Focused B4 JVM tests pass (5/5), A3 golden/A4 runtime cross-check tests pass, the ASP Oracle suite passes (11/11), and `git diff --check` passes. Recommendation's 1,000-selection reproducibility/distribution test and 200×2 history-cooldown distribution test also pass. The aggregate full-JVM command exceeds this environment's reporting window after entering the test task, so the complete 62-class JVM suite was run as deterministic package/class batches instead; every batch passed.

### Batch 7 — 生产门槛与清理

目标：在正确性、性能和分布验证后逐步替换旧路径。

工作：

- AUTO/ASSISTED 使用同一候选池；
- 加入 selection distribution/withholding telemetry；
- POCO X5/X8 测 P50/P95、heap、帧间隔和取消响应；
- staged rollout；
- 只有新旧 parity 和所有 gates 通过后删除旧 UI helper。

退出条件：主规范 C9 gates 全部通过。

#### Batch 7 execution record — 2026-08-16 (C8 telemetry foundation)

In progress. The first C8 slice is deliberately audit-only and does not alter production selection:

- Added a thread-safe, aggregate-only `SelectionDistributionTelemetryRecorder`. It records the full candidate opportunity pool and aggregates the required opportunity, AUTO-eligible, highest eligible tier, and selected counters by family × player count × phase × style. The live first-night and later-night AUTO information selector now supplies that audit at its stable-selection boundary; its decision key makes recomposition idempotent.
- `EXPERT_ONLY` candidates remain available to ASSISTED but are excluded from the AUTO denominator. `REJECTED` candidates remain opportunities only. A selected family must have an AUTO-eligible candidate, so the audit cannot accidentally validate a manual-only selection.
- The retained snapshot contains no candidate IDs, player names, propositions, or hidden game facts; it supports replayable withholding analysis through `selectionRateGivenEligibility` without creating a second selector.
- The existing debug A4 device probe already emits cold/warm P50/P95, coarse heap deltas and main-thread frame intervals. Identity-reveal cancellation now additionally logs the synchronous acknowledgement latency and cancelled-entry count; an in-flight exact build remains discard-only after that acknowledgement.
- **POCO X5 measurement — 2026-08-16:** Xiaomi 22101320G (Android 14), 5-player Trouble Brewing live structural fixture, 11 samples: 2,160 worlds / 496 ZDD nodes; build P50/P95 **125.613 / 131.026 ms**; generation **91.475 / 95.093 ms**; prefix insertion **24.091 / 25.295 ms**; canonicalization **3.437 / 3.715 ms**; coarse maximum build-heap delta **13,205,504 bytes**. Native `alive-seat-2` filter P50/P95 was **2.407 / 2.478 ms**, and native `spy-absent` was **1.981 / 2.843 ms**. No ANR was observed during the 11-sample run. This particular legal role draw contained neither Chef nor Empath, so the numeric decode/rebuild fallback probe was not emitted; fallback, frame-interval and cancellation-latency measurements remain explicitly pending.
- **POCO X5 repeatable fallback measurement — 2026-08-16:** the Debug-only synthetic numeric observation was added so every legal 5-player draw exercises decode/rebuild without changing a game or recommendation. Across 11 samples: build P50/P95 **172.901 / 256.340 ms**; generation **117.264 / 163.363 ms**; prefix insertion **33.718 / 58.005 ms**; canonicalization **14.998 / 27.735 ms**; coarse maximum build-heap delta **13,221,888 bytes**. Native `alive-seat-2` was **2.652 / 3.198 ms**, native `spy-absent` was **1.779 / 3.422 ms**, while `numeric-synthetic-fallback` decode/rebuild was **244.723 / 346.330 ms**. The repeated fallback P95 is over `A4WorldEngineRuntimePolicy`'s 50 ms provisional maximum; ZDD must remain shadow-only and is not eligible for `ZDD_DEVICE_VALIDATED` or a C9 production switch. Frame-interval and cancellation-latency samples remain pending.
- **POCO X5 fallback decomposition — 2026-08-16:** repeatable synthetic numeric fallback, 11 samples: total P50/P95 **247.325 / 345.840 ms**; the exact fallback retained **1,440** worlds, with decode-plus-observation evaluation P50/P95 **190.830 / 258.324 ms** and ZDD rebuild **56.459 / 87.481 ms**. The dominant cost is therefore per-world decoding/rule evaluation, not canonical diagram rebuild. Do not attempt a rebuild-only micro-optimization as a production gate fix; any path toward `ZDD_DEVICE_VALIDATED` requires an exact symbolic compiler for numeric observations plus golden/Oracle parity, otherwise the rollout remains shadow-only.
- Focused `SelectionDistributionTelemetryTest` (4/4), A4 identity-prewarm cancellation regression, existing automatic-selector regression, and `git diff --check` passed.

Still required for C9: wire this audit at every unified selector boundary, complete AUTO/ASSISTED shared-pool parity, perform POCO X5/X8 performance/cancellation measurements and staged shadow rollout, then remove legacy UI helpers only after the gates pass.

#### Batch 7 progress review and next implementation plan — 2026-08-16

结论：Batch 7 仍为 **IN PROGRESS / production rollout blocked**。Batch 6 的 B4 动态视角接入已经完成其 shadow 范围，但 Batch 7 目前只完成了 C8 遥测骨架、AUTO 夜间信息选择的一处接线，以及 POCO X5 的部分 A4 shadow 性能测量。不得把这些成果解释为 C6、C8 或 C9 已退出，也不得删除 legacy UI helper。

##### 当前完成度

| 范围 | 状态 | 证据与边界 |
|---|---|---|
| B4 dynamic PlayerWorldSet | 完成其既定 shadow 范围 | golden matrix 通过；不支持的 attack/protect/role transition 显式 `DEFERRED_B4`；不驱动生产选择 |
| C6 AUTO/ASSISTED unified pipeline | **未完成** | 持久化模式仍是 `MANUAL` 与三个 `AUTO_*`；UI 仍分别使用 `displayOptions`、`recommendedDisplayOptions` 和 `legacyInformationCandidates`，尚未证明同一候选池、gate、ranking 与解释 |
| C8 telemetry domain model | 部分完成 | 有线程安全、去重、无私密内容的内存聚合器及 4 个 focused tests |
| C8 production coverage | **未完成** | 仅 AUTO 的首夜/后续夜晚信息 selector 接线；setup、registration、Mayor、Demon succession、ASSISTED 人工最终选择和 game-end/export 均未接线 |
| C8 denominator correctness | **需修正** | 当前 `familyOpportunityCount`/`familyEligibleCount` 按候选数累加；规范要求按 decision opportunity 计数，即每次 selector invocation 中某 family 至多各加 1。现实现会让候选较多的 family 人为降低 `selectionRateGivenEligibility` |
| POCO X5 correctness/performance evidence | 部分完成且 ZDD promotion 失败 | native filter P95 约 3 ms；numeric decode/rebuild P95 345.840 ms，超过 50 ms 暂定上限；ZDD 必须保持 shadow-only |
| POCO frame/cancel/X8 evidence | **未完成** | frame interval 与 cancellation acknowledgement 尚未形成设备样本；POCO X8 尚未测；当前 heap 只是 JVM coarse delta，不等于 Android Profiler peak memory |
| C9 staged rollout | **未开始** | 尚无统一 selector rollout state、shadow diff 汇总、跨局 distribution 审查或生产切换证据 |
| Legacy helper removal | **禁止** | parity、distribution、X5/X8 和 staged rollout gates 均未完成 |

##### 优先级修正

POCO X5 已证明 ZDD numeric fallback 不具备生产资格，但本实施计划的 PlayerWorldSet 本来就是 shadow 接入。数值观察的 exact symbolic compiler 归入后续 A4/B4 性能工作，不是 Batch 7 当前关键路径。除非统一 selector 的 correctness gate 明确要求该操作，否则不得用继续优化 shadow ZDD 取代 C6/C8/C9 主线工作。

##### 下一步实施顺序

**B7.1 — 修正 C8 统计口径并建立可导出审计（下一项且唯一优先实施项）**

1. 将 opportunity/eligible/highest-tier/selected 改为 invocation-level family counters：
   - family 在完整候选池出现：`opportunity += 1`；
   - family 至少有一个 AUTO-eligible 候选：`eligible += 1`；
   - family 至少有一个候选处于本次全池最高 eligible tier：`highestTier += 1`；
   - 最终选择属于该 family：`selected += 1`。
2. 保留候选数量为独立字段（如确有分析价值），不得再混入 opportunity 分母。
3. 区分 `recommended/previewed` 与 `committed selection`；AUTO 在 exact key 发布并采用时记录 selected，ASSISTED 只在说书人实际确认时记录 selected，单纯渲染候选不得算 selected。
4. 增加 bounded、aggregate-only export/log snapshot；不得输出玩家名、poison target、实际角色表、候选 proposition 或未匿名化 game ID。
5. 必测：多候选同 family 只增加一次 opportunity；无 eligible family 的 rate 为 null；重组/恢复/重复确认不重复计数；同一 decision 的不同 revision 可分别计数；AUTO 与 ASSISTED commit 的 selected 语义一致。

退出条件：统计口径测试通过，聚合快照可按 family × player count × phase × style 重放，且日志隐私测试通过。

##### B7.1 execution record — 2026-08-16

Completed for the currently migrated AUTO night-information selector.

- `SelectionDistributionTelemetryRecorder` now counts opportunity, AUTO eligibility and highest eligible tier once per family per exact selector invocation. Candidate multiplicity is no longer part of the withholding denominator.
- Preview publication and committed selection are separate operations. Generating or recomposing an AUTO recommendation records a preview only; the selected counter advances only when the player display action adopts it. Both operations are idempotent for the exact decision key, while a later state revision is a distinct opportunity.
- The recorder retains only the AUTO-eligible family set needed to validate a later commit. Its bounded export contains aggregate family × player-count × phase × style totals only, with no candidate IDs, player names, propositions or game identifiers.
- Regression coverage now proves invocation-level counting, null rate for an ineligible family, preview-versus-commit separation, duplicate preview/commit idempotency, separate state revisions and bounded aggregate export. Focused telemetry and selector regression tests, ASP Oracle tests and `git diff --check` pass.

The next permitted implementation entry is **B7.2 — unified candidate pool and execution-policy boundary**. Do not broaden C8 coverage or delete legacy helpers until B7.2 parity is in place.

**B7.2 — 建立统一候选池与执行策略边界**

1. 新增纯 Kotlin `UnifiedSelectionPool`（命名可等价），一次性保存 candidate ID/family、legality、epistemic status、quality tier、fixed-point rank、reason/warning codes。
2. 将模式差异限制在最终动作：
   - `AUTO` 从同一 pool 的 AUTO-eligible 候选稳定选择；
   - `ASSISTED` 展示同一 pool、排序、解释及 `MANUAL_ONLY` 候选，由说书人确认；
   - `INELIGIBLE` 对两种模式都不可选择。
3. 明确现有 `MANUAL` 设置与 ASSISTED 产品语义的迁移：优先保留 prefs 兼容，在领域层引入 execution policy；不要直接破坏已有存档枚举值。
4. 首先在 first-night information family 上 shadow 接入，使用 `legacyInformationCandidates` 的完整集合做 ID parity；mismatch 继续旧路径并记录 aggregate diff。

退出条件：每个已迁移 family 的 AUTO/ASSISTED candidate ID 集合、tier、rank 完全相同；差异只剩最终执行方式。

##### B7.2 execution record — 2026-08-16

Completed as a first-night shadow slice; production rollout remains blocked on the later B7.3–B7.5 gates.

- Added pure Kotlin `UnifiedSelectionPool`, carrying candidate ID/family, legality, epistemic status, quality tier, fixed-point rank and reason/warning codes. AUTO can act on recommended/warning candidates only; ASSISTED uses the same order and additionally exposes expert candidates. Ineligible, rejected and unverified/deferred candidates are selectable by neither policy.
- `StorytellerAutomationMode.MANUAL` remains the persisted preference value and maps to the new domain-level `ASSISTED` execution policy, preserving existing preferences and saves.
- First-night shadow conversion now derives a complete pool from `legacyInformationCandidates` and compares ID, tier and rank rather than IDs alone. A mismatch records aggregate-only parity telemetry and continues down the legacy event/display path; it cannot call `display()` on an unpublished migrated draft.
- Added pool-policy, persisted-mode, aggregate-parity and tier/rank-mismatch regressions. Focused B7.1/B7.2 JVM tests and `git diff --check` pass.

The next permitted implementation entry is **B7.3 — expand the unified selector and telemetry coverage**, beginning with completing first-night production-pool wiring and parity tests before setup or registration migration.

**B7.3 — 扩展 selector 与 telemetry 覆盖**

按风险从低到高迁移：first-night information → setup → special registration → Mayor redirect → Demon succession。每个入口必须同时具备：exact key、完整 pool parity、AUTO/ASSISTED 共池测试、selected-at-commit 遥测和恢复幂等测试。未迁移入口保留 legacy，不允许半接线后静默 fallback。

退出条件：C6 所列入口全部共池，C8 audit 不再只覆盖 AUTO 夜间信息。

##### B7.3 execution record — 2026-08-16 (first-night production slice)

In progress. The first-night information card now builds its AUTO and ASSISTED views from the same `UnifiedSelectionPool` constructed from the complete `legacyInformationCandidates` set. It uses the canonical semantic candidate ID also used by the first-night shadow boundary; AUTO is projected to AUTO-eligible candidates and ASSISTED to the full assisted-eligible ordering. Later-night information, setup, special registration, Mayor redirect and Demon succession deliberately remain on their existing paths pending their own parity and recovery tests.

The setup AUTO path now also projects its existing constrained plans through `UnifiedSelectionPool` before style selection. The same complete plan set remains available to ASSISTED, and automatic adoption records preview/commit C8 telemetry by setup family. Spy and Recluse special-registration panels likewise project their recommendations from a shared pool, use distinct registration decision keys, and record AUTO adoption as preview then commit. Mayor redirect and Demon succession now use the same candidate projection for AUTO and ASSISTED; their AUTO telemetry commits only when the selected outcome is confirmed at the night-step boundary. Focused unified-pool, first-night migration, setup coordinator, special-registration, Mayor redirect, Demon succession, dynamic selector and C8 telemetry JVM tests pass. This is not C6 completion and must not be used to remove any legacy helper.

#### B7.3 exit-validation attempt — 2026-08-16

- The full `testDebugUnitTest assembleDebug` invocation compiled successfully and produced `app-debug.apk` (13,717,186 bytes). As with the earlier recorded aggregate suite, this environment did not return a complete test-task summary inside its reporting window; the migrated focused test classes remain the deterministic automated evidence.
- ASP Oracle tests pass 11/11 and `git diff --check` passes.
- The Debug APK was installed on the attached POCO X5 (22101320G) without clearing app data. After user unlock, the existing five-player saved game loaded and the Debug-only A4 benchmark completed: 11 samples, 2,160 worlds / 496 nodes; build P50/P95 **178.874 / 256.006 ms**; generation P50/P95 **123.654 / 156.540 ms**; prefix insertion P50/P95 **34.625 / 64.960 ms**; canonicalization P50/P95 **13.496 / 27.206 ms**; coarse maximum build heap delta **13,221,888 bytes**. Native `alive-seat-2` P50/P95 was **2.325 / 3.175 ms** and native `spy-absent` was **1.854 / 3.496 ms**. Synthetic numeric decode/rebuild P50/P95 was **239.874 / 339.061 ms** (decode/evaluation **184.798 / 252.823 ms**, rebuild **55.092 / 86.208 ms**), still above the provisional 50 ms maximum; ZDD remains `ZDD_SHADOW`.
- After the user provided a new disposable game and unlocked the device, the normal end-of-day transition was clicked once. The app advanced from Day 1 through the intervening night to Day 2 with all five players alive; there was no stuck confirmation state and no `com.codex.campboardgamehost` `AndroidRuntime` crash. This is a later-night automatic-transition smoke test only: because the supplied game was already at Day 1, it does not prove the first-night card or a Mayor/Demon decision branch end-to-end.
- A real restore uncovered two B7.3 setup-pool defects before any recommendation was displayed: a legal no-op plan produced a blank candidate ID, and style variants of the same no-op plan produced duplicate IDs. `unifiedSetupPool` now hashes the full stable plan variant (canonical decisions, style, tier, score and deterministic source index); a regression covers both blank and duplicate no-op variants. Focused coordinator/A4 tests and a POCO X5 restore into the first-night recommendation screen pass after the fix.

**B7.4 — 设备门槛补齐**

1. POCO X5 采集 identity-prewarm 的 frame P50/P95、>32 ms/>50 ms 帧数、coarse heap、cancel acknowledgement；验证取消后的 in-flight 结果只能为 stale。
2. 在 POCO X8 重复相同脚本、样本数和 thermal/charging 条件，记录 device/build 标签。
3. 另行基准统一 production selector 的 build/select/commit latency；不得用 A4 shadow benchmark 代替生产 selector 性能。
4. ZDD 维持 `ZDD_SHADOW`；只有 exact symbolic compiler 的 golden/Oracle parity 与两台设备门槛通过后，才重新评估 `ZDD_DEVICE_VALIDATED`。

退出条件：X5/X8 均有可复核的 P50/P95、peak/coarse memory、frame 与 cancellation evidence；无 ANR/OOM/stale publish。

##### B7.4 execution record — 2026-08-16 (POCO X5 partial)

- Natural identity-reveal prewarm on the POCO X5 completed 5/5 recipients: total build **9,773 ms**, coarse maximum/end heap delta **62,797,000 / 62,797,000 bytes**, main-thread frame P50/P95 **16 / 16 ms**, 0 frames over 32/50 ms, maximum interval **23 ms**.
- The Debug-only isolated cancellation probe reads only the current structural snapshot and uses a fresh shadow cache; it does not change the current night step, event log, recommendation or persisted game. On the same device, after two frame boundaries it reported: 5 recipients, `1:STALE, 2–5:CANCELLED`, 0 ready, total worker build **2,113 ms**, coarse max/end heap delta **9,472,488 / 9,472,488 bytes**, frame P50/P95 **8 / 8 ms**, 0 frames over 32/50 ms, maximum interval **19 ms**, synchronous cancellation acknowledgement **0 ms**, 5 cancelled entries, and `verification=stale-not-published`.
- A Debug-only, aggregate-only unified setup-selector benchmark uses the current ready setup plans without writing a decision or save. On the POCO X5, 11 samples / 3 candidates: pool build P50/P95 **155 / 217 µs**, AUTO selection **24 / 32 µs**, and isolated C8 preview/commit recording **75 / 87 µs**. This covers the setup selector boundary only; it must not be generalized to first-night information, registration, Mayor or Demon decision latency.
- **POCO X8 measurement — 2026-08-16:** Xiaomi POCO X8 (`2511FPC34G` / `klee_global`, Android 16, build `OS3.0.306.0.WPJMIXM`), 5-player Trouble Brewing test game. The user ran the same Debug-only isolated cancellation probe: 5 recipients, `1:STALE, 2–5:CANCELLED`, 0 ready, worker build **1,290 ms**, coarse max/end heap delta **12,132,112 / 12,132,112 bytes**, frame P50/P95 **8 / 8 ms**, no frames over 32/50 ms, maximum interval **8 ms**, cancellation acknowledgement **0 ms**, 5 cancelled entries and `verification=stale-not-published`. The 11-sample setup-selector slice (3 candidates) measured pool build **116 / 152 µs**, AUTO selection **14 / 15 µs**, and C8 preview/commit **39 / 44 µs**. Android 16 forbids ADB input injection on this device, so the user tapped the diagnostic controls manually；log retrieval remains ADB-verifiable. No `com.codex.campboardgamehost` crash appeared in the captured log.
- **POCO X8 first-night information-pool slice — 2026-08-16:** the Debug-only benchmark recreated the complete Investigator first-night `UnifiedSelectionPool` for each of 11 samples without showing, committing or persisting information. With 4 candidates, pool build P50/P95 was **157 / 320 µs**, AUTO projection/style selection **30 / 41 µs**, and isolated C8 preview/commit **50 / 84 µs**. No `com.codex.campboardgamehost` crash appeared in the captured log.
- This completes the POCO X5/X8 coarse/frame/cancellation evidence and the setup-selector latency slice, but does not complete B7.4: Android Profiler peak-memory data and the remaining unified decision-family latency measurements are pending. The prior numeric fallback gate failure still keeps ZDD at `ZDD_SHADOW`.

**B7.5 — Distribution review 与 staged rollout**

1. rollout 状态至少为 `LEGACY_ONLY → SHADOW_COMPARE → ASSISTED → LIMITED_AUTO → AUTO`，默认保持 `LEGACY_ONLY` 或 `SHADOW_COMPARE`。
2. 每阶段记录 candidate parity、selected parity、stale discard、failure/degradation 和 withholding strata；任何 unexplained mismatch 自动回退且禁止扩大 rollout。
3. 在足够的 replay/simulation 与真实匿名聚合样本上审查稳定 withholding signal；representation floor 只能作用于已经通过 legality、epistemic 和 quality gates 的候选。
4. 只有 correctness、epistemic、metric、policy、performance、distribution gates 全部通过后，才允许 production switch；legacy helper 删除必须是最后一个独立变更。

##### B7.5 execution record — 2026-08-16 (in progress)

- Added a pure-domain `SelectionRolloutGate` with the required ladder: `LEGACY_ONLY → SHADOW_COMPARE → ASSISTED → LIMITED_AUTO → AUTO`. It is intentionally decoupled from persisted storyteller automation preferences, so this change does not expand any live automatic decision path.
- Any candidate/selected parity mismatch, selection failure/degradation, or unexplained withholding stratum forces `LEGACY_ONLY` and blocks expansion. Stale discards remain separately measurable and do not by themselves create a false rollback.
- `ASSISTED` and higher require an explicit external distribution-review approval. Wiring aggregate parity, stale-discard, failure/degradation and withholding evidence from every migrated decision family into this gate remains pending; until then each family stays at its existing conservative behavior.
- Added `SelectionDistributionReviewer`: it consumes only C8 aggregate exports, compares AUTO-eligible family selection rates within the same player-count/phase/style cohort after a configurable minimum sample, and emits review signals rather than a self-authorizing rollout decision. Cross-game persistence/aggregation and reviewer approval UI remain pending.
- Verification on 2026-08-16: full `testDebugUnitTest`, ASP Oracle suite (11 tests), `git diff --check`, and Debug APK assembly passed; the APK was installed on the connected POCO X8 without automated input.
- The remaining B7.4 Debug-only controls are now behind one collapsed developer-diagnostics disclosure per host view；the first-night pool control follows the same disclosure. They remain available only until the outstanding device evidence is collected, and are excluded from Release builds.

##### 进入下一项前的验证基线

当前工作区同时包含 Batch 6、Batch 7 与设备诊断在途修改。开始 B7.1 前先运行并记录：

```zsh
./gradlew testDebugUnitTest --no-daemon \
  --tests 'com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryTest' \
  --tests 'com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigrationTest' \
  --tests 'com.codex.campboardgamehost.clocktower.epistemic.B4DynamicPlayerWorldSetShadowTest' \
  --tests 'com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkHarnessTest' \
  --tests 'com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmCoordinatorTest'
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
git diff --check
```

完成 B7.1–B7.3 后再运行 full JVM、Debug APK build 和人工点击冒烟。若出现秘密泄漏、旧 generation 发布、真实 OOM/ANR、无法区分 preview 与 committed selection，立即按第 17 节停止条件报告。

## 13. 每批验证命令

Terra 应优先运行该批 focused tests，然后运行：

```zsh
./gradlew testDebugUnitTest --no-daemon
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
git diff --check
```

涉及 Android UI wiring 的批次额外要求 Debug APK 编译和人工点击冒烟，但不得把 APK 构建成功替代领域测试。

## 14. 性能与并发要求

- 推荐计算不得在主线程运行；
- 同一 game/decision point 同时只保留最新 generation 可发布；
- cancel 是优化，stale commit rejection 才是正确性保证；
- 不以 enumeration cap 或 sampling 改变 SAT/UNSAT；
- OOM/timeout 返回显式不可用状态，ASSISTED 可提示人工裁定；
- 首夜可以预热未来 decision point，但发布前仍必须校验 exact key；
- 只有 dependency catalog 证明不受影响时才可复用缓存，不能凭角色名称猜测。

## 15. 遥测与审计

每次 generation 至少记录：

```text
gameId (可匿名化)
decisionPointId
gameStateRevision
playerInputRevision
stateDigest
historyDigest
observationDigest
generationStatus
candidateCount / eligibleCount
selectedCandidateId (若有)
buildMs
staleDiscarded
```

日志不得包含完整实际角色表、poison target、私密 proposition 文本或玩家姓名。

每个 committed event 必须保存结果快照和候选审计摘要；不能依赖未来版本重新计算历史结果。

## 16. Definition of done

只有满足以下全部条件，动态决策引擎第一版才完成：

1. setup fact、provisional recommendation 和 committed result 有类型边界；
2. 投毒目标确认能使所有未展示首夜信息失效并重算；
3. 已展示信息在任何后续状态变化后保持不变；
4. 酒鬼展示身份不会改变，但其未展示的具体信息可随状态重算；
5. 市长、保护、死亡、继承和角色变化使用同一 snapshot/generation/commit 框架；
6. 旧 generation 永远不能覆盖新状态；
7. 保存/恢复可重放，双击确认幂等；
8. AUTO/ASSISTED 共用候选与规则事实；
9. 不支持的 B4 场景显式 deferred，不伪造 UNSAT；
10. focused、full JVM、Oracle、设备性能和端到端测试全部通过。

## 17. Terra 停止条件

遇到以下任一情况，Terra 必须停止当前 batch 并报告，不得自行扩大范围：

- 为通过测试必须改写官方规则语义；
- 必须让生产推荐读取 A4 shadow cache；
- 无法区分已展示结果和 provisional recommendation；
- 无法以一个不可变 snapshot 重放当前决策；
- 需要一次性重写整个 `MainActivity.kt` 才能继续；
- 出现真实 OOM、ANR、旧 generation 成功写入新 key或私密信息泄漏；
- 工作区存在无法保留的重叠用户修改。

报告必须包含：触发条件、最小复现、已完成批次、未执行内容以及建议的设计变更。

## 18. Terra 新对话执行交接 — 2026-08-15

### 18.1 当前权威进度

- A4.5 observation cache rebuild executor 已完成并通过领域测试、全量 JVM 测试和真机日志验证；不要在本计划中重做 A4.5。
- 动态决策引擎 Batch 0 已完成：现有行为 characterization、legacy seed 边界与回归基线已经建立。
- 动态决策引擎 Batch 1 已完成：`DynamicDecisionSnapshot`、`DynamicRecommendationKey`、canonical digests、generation store 及 R1–R6/C1/E1 测试已经落地。
- Batch 1 完成时，全量 JVM 测试、ASP Oracle 11 项测试和 `git diff --check` 均通过。
- **下一项且唯一获准主动实施的工作是 Batch 2：reducer 与提交事务边界。** 不得跳到 Batch 3，也不得重新实现 Batch 0/1。

新对话开始后，Terra 先执行只读检查：

```zsh
git status --short
./gradlew testDebugUnitTest --no-daemon --tests "com.codex.campboardgamehost.clocktower.domain.DynamicRecommendationKeyTest"
```

当前工作区包含本计划之前各批次的已修改及未跟踪文件。它们都是需要保留的在途成果；不得 clean、reset、checkout、删除、暂存或提交，也不得用批量格式化覆盖无关改动。

### 18.2 Batch 2 入口文件

开始设计前至少阅读以下文件及其对应测试：

- `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DynamicRecommendationKey.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/GameSnapshot.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerDecisionEvent.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/DecisionEventStore.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicObservationLog.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/DynamicRecommendationKeyTest.kt`

优先新增小型、纯 Kotlin 的 reducer/transaction aggregate 文件，不要把所有职责直接塞进 `DecisionEventStore`，也不要为了接线改写 `MainActivity.kt`。

### 18.3 Batch 2 最小实施契约

1. 定义不可变 typed `ActionFact`，至少覆盖 poison、protect、attack、execution、death、role change、phase advance。每个 fact 必须有稳定 action ID 或 idempotency key，以及可重放的时间线顺序信息。
2. 实现确定性的 ordered reducer。相同的初始持久化状态和相同的有序 facts 必须产生完全相同的 `GameSnapshot`、revision 和相关 digest；不得读取时钟、随机数、UI 或可变全局状态。
3. 建立单一 committer/transaction aggregate，在一个临界区内完成：
   - 重新校验 exact `DynamicRecommendationKey` 和相关 revisions；
   - 追加 committed `StorytellerDecisionEvent`；
   - 仅当结果实际向玩家展示时追加且只追加一条 observation；
   - 应用 action outcome/fact 并生成新状态；
   - 任一步失败时不得留下部分 event、observation 或 state mutation。
4. commit 结果至少显式区分 `APPLIED`、`ALREADY_APPLIED`、`STALE`、`CONFLICT` 和内部失败；不能把 stale 当作普通失败，也不能静默覆盖新状态。
5. 同一 action/confirmation 重复提交必须幂等：第二次返回已有结果，不得重复 event、observation 或状态效果。
6. 恢复路径必须只依赖持久化初态和有序 facts/events/observations 重放；不得依赖未来版本重新计算已经展示的结果。

如果当前存储层没有数据库事务，可先实现同步、内存、纯 Kotlin 的 transaction aggregate 与 copy-on-write 提交模型来证明原子性和恢复语义；不得假装多个独立可变 store 的顺序写入等价于事务。

### 18.4 Batch 2 必测场景

- C2：双击/重复确认只产生一个 event、至多一个 displayed observation 和一次 action effect。
- C3：generation 后、commit 前任一 key/revision 改变，commit 返回 `STALE`，且所有 store 保持零部分写入。
- P2：已展示 observation 在后续 action 后保持不变；未展示 recommendation 可失效，但 Batch 2 不负责生成新推荐。
- 相同输入重放两次得到相同 snapshot、revision 与 digests。
- facts 无论从恢复载入还是在线追加，都按稳定顺序规约；重复 fact 不重复生效。
- role change、death、phase advance 等类型不能退化为自由文本或 UI callback。
- 注入 event/observation/state 写入失败时，事务前后的可观察状态一致。

完成 focused tests 后必须运行第 13 节的全量 JVM、Oracle 和 diff-check。若 Batch 2 未涉及 Android UI，不要求构建 APK 或真机点击。

### 18.5 明确禁止与 Batch 2 非目标

- 不接 Poisoner 真实 UI；那是 Batch 3。
- 不改变现有推荐评分、候选选择或 AUTO/ASSISTED 行为。
- 不让生产推荐读取 A4 shadow cache。
- 不实现 B4 timeline、Mayor/保护/死亡的完整生产行为；Batch 2 只建立可承载这些 action 的类型与 reducer/commit 基础。
- 不改 `MainActivity` 角色流程，不做大规模重构，不生成或发布 APK。
- 不因发现后续需求而越过 Batch 2；触发第 17 节任一条件时立即停下并按规定报告。

### 18.6 Batch 2 完成交付格式

Terra 完成后必须在本文件追加 `Batch 2 execution record`，记录：新增/修改文件、事务模型、测试映射、完整验证命令与结果、尚未接入的生产路径、已知风险，以及下一批明确入口。只有 C2、C3、P2、恢复与失败原子性测试全部通过，才可声明 Batch 2 完成。

#### Batch 2 execution record — 2026-08-15

Completed as a pure Kotlin transaction slice; no UI, recommendation scoring, or A4 shadow consumer was changed.

- Added `ActionFact.kt`: immutable typed poison, protect, attack, execution, death, role-change and phase-advance facts, plus a deterministic ordered reducer. Facts are uniquely identified and sequence ordered; replay is independent of time, randomness and UI state.
- Added `DynamicDecisionTransactionAggregate.kt`: a synchronized copy-on-write aggregate. It validates the exact generation key and revisions before creating a candidate state, then atomically appends an APPLIED event, optionally one durable displayed observation, and the reduced action facts. Failed candidate construction leaves the prior observable state unchanged.
- Added `DynamicDecisionTransactionAggregateTest.kt`: C2 idempotency; C3 stale rejection with no writes; P2 preservation of displayed observations; stable replay; typed role/death/phase handling; duplicate-fact and observation-append failure atomicity.
- Validation passed: focused `DynamicDecisionTransactionAggregateTest`; full `./gradlew testDebugUnitTest --no-daemon`; 11-test `python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'`; and `git diff --check`.

The aggregate is intentionally not wired to `MainActivity`, the legacy `DecisionEventStore`, or production recommendation generation. A durable persistence adapter will need to store the aggregate's initial snapshot, ordered facts, events and observation log as one transaction. The next permitted implementation entry is Batch 3, Poisoner-driven first-night dynamic regeneration, after the required full validation succeeds.

## 19. Current handoff for a new development session — 2026-08-16

This section supersedes the stale “next permitted implementation” wording in section 18 for the current worktree. Preserve all existing modified and untracked files: they are in-progress Batch 6/7 work. Do not clean, reset, checkout, stage, commit, or broadly reformat the worktree.

### 19.1 Current state

- B7.1–B7.3 migrated the setup selector, first-night information, special registrations, Mayor redirect and Demon succession to shared `UnifiedSelectionPool` boundaries with aggregate-only C8 preview/commit telemetry and focused parity coverage.
- B7.4 has verified A4 cancellation/coarse/frame evidence on POCO X5 and POCO X8, plus setup-selector and Investigator first-night pool latency on X8. ZDD remains `ZDD_SHADOW`; the numeric decode/rebuild threshold is still not met.
- B7.5 has a pure-domain rollout ladder (`SelectionRolloutGate`) and aggregate-only withholding reviewer (`SelectionDistributionReviewer`). Neither expands live automation；cross-game aggregate persistence, all-family evidence wiring, and an explicit reviewer approval path are still absent.
- Debug diagnostic controls remain necessary only for the outstanding device gates. They are compiled only in Debug and are now hidden behind a single “Show developer diagnostics” disclosure. The first-night pool control follows that disclosure.

### 19.2 Latest verified baseline

- `./gradlew testDebugUnitTest --no-daemon` passed on 2026-08-16.
- `python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'` passed 11 tests.
- `git diff --check` passed.
- Debug APK compiled and was installed without clearing data on POCO X8: serial `YXSCJNTSIFT4W8Z9`, model `2511FPC34G` / `klee_global`, Android 16 build `OS3.0.306.0.WPJMIXM`.
- Android 16 on this device rejects ADB input injection (`INJECT_EVENTS`); the user must tap diagnostics manually. ADB log retrieval remains usable. Never claim a device measurement was run unless its log line has been retrieved.

### 19.3 Next permitted work

1. Complete B7.4 by adding/using real, non-persisting selector benchmarks for the remaining migrated families: special registration, Mayor redirect and Demon succession. Do not substitute a synthetic generic pool benchmark for their real candidate construction. Record X8 P50/P95 build/select/C8-commit timings and check for app crashes after each manual run.
2. Collect Android Profiler peak-memory evidence under the existing specified fixture；coarse heap metrics do not satisfy this remaining gate.
3. Then implement B7.5 evidence wiring and an aggregate-only cross-game persistence/review boundary. It must contain no names, role sheets, poison targets, private propositions, candidate IDs, or decision IDs. Do not allow the reviewer or rollout gate to promote a live family without explicit human approval and all required gates.
4. Do not remove legacy helpers or the Debug diagnostics until B7.4/B7.5 evidence is complete in a separate reviewable change.

### 19.4 New-session first checks

```zsh
git status --short
./gradlew testDebugUnitTest --no-daemon \
  --tests 'com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolTest' \
  --tests 'com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryTest' \
  --tests 'com.codex.campboardgamehost.clocktower.recommendation.SelectionRolloutGateTest' \
  --tests 'com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionReviewTest'
git diff --check
adb devices -l
```

## 20. 2026-08-20 R5.5 FlowPlanner 前置约束

本节覆盖第 18/19 节历史 handoff 中任何“下一项可直接继续 production implementation”的旧状态文字。当前实际状态由 `CURRENT_DEVELOPMENT_ROADMAP.md` 决定：**本文的后续 production implementation 在 Phase A R5 与 R5.5 Script & Dynamic Flow Foundation 完成前均为 BLOCKED。**

R5.5 的专项规范是：

`docs/多剧本多板子与动态游戏流程架构设计_v1.md`

新的上游边界固定为：

```text
Script / Character Catalog
        ↓
ClocktowerFlowPlanner
        ↓
HostInteraction / StorytellerDecisionPoint
        ↓
DynamicDecisionSnapshotFactory
        ↓
本文定义的 generation / recommendation / commit lifecycle
```

因此本文负责“一个已经打开的 decision point 如何生成、失效、评分、确认和提交”，**不负责决定本阶段有哪些角色应该行动、谁应该醒、下一个流程步骤是什么**。这些职责属于 FlowPlanner / Role Handler。

R5.5 后恢复本文 implementation 时必须满足：

- decision point 来自 script-aware `ClocktowerFlowPlanner` 或等价正式 seam；
- generation key/snapshot 能携带稳定 `ScriptId / RulesetRef / RoleId / interaction identity`；
- `nightOrderPosition` 只可用于定位/replay，不得作为定义流程的唯一事实；
- 禁止按 `TroubleBrewing` enum、角色显示名或 Compose `when` 重新生成流程；
- dynamic engine 不自行维护第二份 first/other-night order；
- FlowPlanner 的 waking/interaction eligibility 与 role ability functioning 分离；
- state/action/event commit 完成后由 FlowPlanner 基于新状态重新计算开放 interaction；
- custom/homebrew `PARTIAL / UNVERIFIED` interaction 必须遵守 R5.5 的 MANUAL_ONLY/安全降级，不能仅因本文能生成候选就提升为 AUTO。

R5.5 不废弃本文已经实现并验证的 revision、stale rejection、transaction、UnifiedSelectionPool 或 telemetry 机制；目标是让这些机制从“TB hardcoded flow 的下游”迁移为“script-aware FlowPlanner 的下游”。
