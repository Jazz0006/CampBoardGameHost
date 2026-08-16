# 状态版本驱动的动态决策引擎：Terra 实施规范

> 状态：READY FOR IMPLEMENTATION
> 日期：2026-08-15
> 主规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`
> 本文取代：`dynamic_storyteller_decision_recommendation_implementation.md` 中面向未来的实施计划
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

禁止手写“投毒时只刷新被投毒者”。投毒改变全局叙事与评分，因此本夜所有未提交信息都需要新 generation；生成器可以按 dependency set 做性能优化，但语义上必须等价于完整失效。

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

### Batch 7 — 生产门槛与清理

目标：在正确性、性能和分布验证后逐步替换旧路径。

工作：

- AUTO/ASSISTED 使用同一候选池；
- 加入 selection distribution/withholding telemetry；
- POCO X5/X8 测 P50/P95、heap、帧间隔和取消响应；
- staged rollout；
- 只有新旧 parity 和所有 gates 通过后删除旧 UI helper。

退出条件：主规范 C9 gates 全部通过。

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
