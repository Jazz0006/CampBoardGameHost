# CampBoardGameHost 自动说书人玩家认知一致性算法改进方案 v2.2

> 版本：2.2  
> 日期：2026-08-11  
> 状态：当前唯一实施规范  
> 适用范围：优先覆盖《暗流涌动》（Trouble Brewing），架构支持后续剧本扩展  
> 取代文档：v2.0、v2.1；旧文档仅保留为设计演进记录  
> 当前实施基线：A0、A1、A2、A1.1 已完成；下一项为 A2.1  

---

## 1. 本版结论

本项目继续采用“玩家视角 Possible Worlds + 信息论节奏控制 + 说书人策略”的总体方向，但在进入 `EnumeratedWorldSet` 前先加固规则语义和验证权威边界。

正式路线为：

```text
Official Rules / Almanac / Published Rulings
                    ↓
Formal Game State + Interaction-scoped Registration Semantics
                    ↓
Legal Choice Layer
                    ↓
PlayerKnowledgeSnapshot + EpistemicHypothesis
                    ↓
PlayerWorldSet(P, t, hypothesisMode)
                    ↓
Candidate Simulation: beforeWorlds → observation → afterWorlds
                    ↓
Epistemic Metrics + Structural Metrics + Narrative Metrics
                    ↓
Shared Quality Gates
                    ↓
Runtime Storyteller Policy
                    ↓
AUTO / ASSISTED Unified Selector
```

本版新增的关键决定：

1. 权威顺序固定为：官方规则 > 项目 golden expectation > 外部 Oracle。
2. 登记语义必须绑定具体互动，禁止把 Spy/Recluse 永久改写成另一个身份或阵营。
3. 红鲱鱼按实际好人身份选择，不使用互动登记结果；Spy 不可成为红鲱鱼，Recluse 可以。
4. 世界数量使用可表达精确大整数或下界的 `WorldCardinality`，不再以 `Long` 暗示永不溢出。
5. `hypothesisMode`、接收者视角和知识快照共同决定 `PlayerWorldSet` 身份与缓存键。
6. 候选类别和剩余世界的解释类别是两套 taxonomy，禁止混用。
7. A3 前插入 A1.1 与 A2.1；A3 只有在扩展 golden corpus 无未解释差异后才开始。
8. Phase C 增加 Selection Distribution & Withholding Audit，生产上线顺延为 C9。

本项目的产品目标仍然不是尽快消除不确定性，而是：

> **维持 Productive Uncertainty：信息持续推动推理，但不过早把合理世界、恶魔候选或叙事分支压缩到接近唯一答案。**

---

## 2. 规范与验证权威

### 2.1 单一实施规范

从本版开始：

- v2.2 是后续实现、审查和验收的唯一主规范；
- v2.0、v2.1 仅说明设计演进，不再作为并列要求来源；
- 专题文档可补充实现细节，但不得改变 v2.2 的规则权威、数据边界或退出条件；
- 如果专题文档与 v2.2 冲突，必须先修订 v2.2 或显式记录新的决策。

### 2.2 权威顺序

规则判断采用以下顺序：

```text
1. Official role text / Almanac / published official ruling
2. CampBoardGameHost reviewed golden expectation
3. External executable Oracle
4. External implementation or research model
5. Current production behavior
```

外部 Oracle 的价值是发现差异，不是覆盖官方规则。Oracle 与官方规则冲突时：

- 保留项目的官方期望；
- 将差异登记为 `KNOWN_ORACLE_VARIANCE`；
- 不为了让测试变绿而修改正式规则；
- 不把外部模型的结果展示为官方裁定。

### 2.3 Oracle 比较分类

统一使用：

```text
AGREE
EXPECTED_COVERAGE_GAP
KNOWN_ORACLE_VARIANCE
UNEXPLAINED_MISMATCH
NOT_RUN
```

解释：

- `AGREE`：项目期望与 Oracle 一致；
- `EXPECTED_COVERAGE_GAP`：冻结 Oracle 无法表达项目正式语义；
- `KNOWN_ORACLE_VARIANCE`：Oracle 可运行，但与官方规则存在已解释偏差；
- `UNEXPLAINED_MISMATCH`：尚未查明，阻断相关阶段退出；
- `NOT_RUN`：工具、超时或环境失败，绝不能被解释成 UNSAT。

---

## 3. 统一语义边界

### 3.1 三层事实必须分离

```text
FormalGameState
    说书人真实世界：实际角色、实际状态、隐藏 setup 选择、真实时间线

EpistemicObservation
    实际给出的信息：接收者、时间、命题、玩家可见的可靠性

PlayerKnowledgeSnapshot
    某一玩家在某时间点真正可用的公开与私有事实
```

禁止将以下事实泄漏进普通好人玩家的知识快照：

- 实际角色表；
- Poisoner 当前目标；
- 红鲱鱼身份；
- 恶魔 bluff；
- 未向该玩家公开的 registration choice；
- 说书人内部评分、随机种子或候选集合。

### 3.2 A1 语义根对象

A1 已建立并继续保留：

```text
FormalGameState
InformationProposition
EpistemicObservation
StorytellerDecisionPoint
LegalChoiceSet
PlayerKnowledgeSnapshot
```

A1.1 在该边界上补充：

```text
EpistemicHypothesis
RegistrationQuery
RegistrationProfile
RegistrationSemantics
WorldCardinality
PlayerWorldSetIdentity
CandidateFamilyId
WorldExplanationClusterId
```

由于 A1 尚未接入生产路径，A1.1 直接将 epistemic JSON schema 升级到 v2，并提供显式迁移或拒绝旧 fixture 的清晰错误；禁止静默误读。

### 3.3 `EpistemicHypothesis`

同一知识快照可在不同分析假设下产生不同世界集合：

```kotlin
enum class EpistemicHypothesis {
    MECHANICALLY_CREDIBLE,
    FUNCTIONING_ONLY,
    MALFUNCTION_ALLOWED,
}
```

语义：

- `MECHANICALLY_CREDIBLE`：允许所有与该玩家所知相容的官方机制解释；
- `FUNCTIONING_ONLY`：假设相关能力健康且正常生效；
- `MALFUNCTION_ALLOWED`：显式允许 Drunk/Poisoned 等失能解释，用于比较 malfunction exposure。

实现可以在 A1.1 最终命名时微调枚举，但必须满足：

- mode 是 `PlayerWorldSet` 构造参数；
- mode 是实例属性；
- mode 进入 snapshot identity 与所有缓存键；
- 不同 mode 的结果禁止共享缓存值。

---

## 4. Interaction-scoped Registration Semantics

### 4.1 为什么不能只提供 `mayRegisterAs(seat, role, phase)`

Spy/Recluse 的登记是“每次能力互动中可能发生的规则选择”，而不是对玩家真实身份的永久替换。同一夜里，同一玩家对不同能力可以采用不同登记结果。

仅使用 `phase` 无法区分：

- 哪个检测能力正在查询；
- 查询的是角色、角色类型、阵营还是 Demon 身份；
- 哪个候选输出绑定了该登记；
- 同一夜多次互动是否独立。

### 4.2 正式接口方向

```kotlin
data class RegistrationQuery(
    val subjectSeat: Int,
    val interactionId: String,
    val timelinePoint: TimelinePoint,
    val detectingAbility: AbilityId,
    val question: RegistrationQuestion,
)

interface RegistrationSemantics {
    fun possibleRegistrations(
        state: FormalGameState,
        query: RegistrationQuery,
    ): Set<RegistrationProfile>
}
```

两层职责严格分开：

- `possibleRegistrations(...)`：规则允许哪些登记；
- `RegistrationFact`：某个具体合法候选或世界实际选择了哪个登记。

所有受登记影响的信息角色和能力必须查询统一语义层。禁止在各角色 evaluator 中散落 `actualRole == ...` 特判。

### 4.3 登记事实的绑定规则

登记结果必须绑定完整候选：

```text
displayed information
+ interactionId
+ selected RegistrationFact(s)
= one LegalChoice
```

禁止：

```text
先选择显示信息
→ 再独立随机登记
```

否则会产生候选 ID 不稳定、回放不一致和局部合法性丢失。

### 4.4 红鲱鱼是明确例外

Fortune Teller 的 red herring setup 选择依据是实际好人玩家，而不是某次互动的登记结果：

```text
eligibleRedHerring(player) = actualAlignment(player) == GOOD
```

因此：

- Spy 实际为邪恶，不能成为红鲱鱼，即使其能力允许在互动中登记为好人；
- Recluse 实际为好人，可以成为红鲱鱼，即使某次互动中登记为邪恶或 Demon；
- Fortune Teller 检查 Recluse 是否得到 YES，仍属于该次 FT 互动中的可选 Demon 登记；
- red herring 身份属于 storyteller-only setup fact，不进入普通玩家的知识快照。

当前 `PlanLegalityValidator` 使用实际阵营判断的方向保持不变。

### 4.5 Spy grimoire observation

Spy 的知识在“每夜醒来查看魔典”时形成 observation，不笼统归入 setup：

```text
Spy wakes
→ sees current grimoire state
→ private EpistemicObservation addressed to Spy
```

第一版至少表达当时可见的：

- 玩家位置；
- 当前显示角色/token；
- reminder tokens；
- 已发生且在魔典中可见的状态。

不得在没有官方依据时自动加入“恶魔收到的三张 bluff 列表”。若产品 UI 额外展示某项信息，必须由独立正式规则说明。

---

## 5. `PlayerWorldSet` 正式契约

### 5.1 一等领域对象

```kotlin
interface PlayerWorldSet {
    val recipientSeat: Int
    val knowledgeSnapshotId: String
    val hypothesis: EpistemicHypothesis
    val identity: PlayerWorldSetIdentity

    fun isEmpty(): Boolean
    fun cardinality(): WorldCardinality

    fun require(observation: EpistemicObservation): PlayerWorldSet
    fun exclude(observation: EpistemicObservation): PlayerWorldSet

    fun possibleRoles(seat: Int): Set<RoleId>
    fun possibleDemonSeats(): Set<Int>
    fun possibleMinionSeats(): Set<Int>

    fun roleWorldCount(seat: Int, role: RoleId): WorldCardinality
    fun demonWorldCount(seat: Int): WorldCardinality
    fun explanationClusters(): ExplanationClusterSummary
}
```

可替换实现：

```text
EnumeratedWorldSet       correctness baseline / debugging
ZddPlayerWorldSet        mobile runtime candidate
SolverBackedWorldSet     development Oracle adapter
```

Recommendation Engine 只依赖接口，不依赖 Clingo atom 或 ZDD node。

### 5.2 `WorldCardinality`

```kotlin
sealed interface WorldCardinality {
    data class Exact(val value: BigInteger) : WorldCardinality
    data class AtLeast(val lowerBound: BigInteger) : WorldCardinality
}
```

约束：

- 可精确计算时必须返回 `Exact`；
- 因调试枚举上限或预算只能得到下界时返回 `AtLeast`；
- `isLarge` 属于性能路由判断，不属于数值正确性；
- 不允许溢出、截断或达到 cap 后继续伪装为 exact；
- SAT/UNSAT 必须由精确存在性判断得出，不得由有限枚举“没找到”推断 UNSAT。

### 5.3 snapshot identity

`PlayerWorldSetIdentity` 必须来自：

```text
ruleset identity
+ recipient seat
+ canonical PlayerKnowledgeSnapshot
+ EpistemicHypothesis
+ semantic schema version
```

稳定 ID 继续采用 canonical payload + SHA-256，不引入 MurmurHash 等第二套身份规则。

禁止使用包含说书人秘密的 `FormalGameState.snapshotId` 作为玩家世界缓存身份。实现内部可以关联 actual snapshot 进行审计，但不能让秘密事实改变玩家视角对象的公开语义身份。

---

## 6. Entropy 与结构指标

### 6.1 基本指标

等权机械世界第一版使用：

```text
H_world = log2(|W|)
IG_world = H_before - H_after
ER_world = H_after / H_before
```

这些值描述机械世界结构，不是玩家真实主观概率。

优先结构指标：

```text
World entropy
Demon structural entropy
Possible Demon seat count
Possible Minion seat count
Setup profile count
Explanation cluster count
Dominant explanation share
```

### 6.2 边界条件

指标计算必须遵守：

1. `afterWorlds` 为空：候选先标为 `INELIGIBLE`，entropy/retention 为 undefined，不进入评分。
2. `H_before == 0 && H_after == 0`：singleton 到 singleton，retention 定义为 1。
3. `H_before == 0 && H_after > 0`：违反过滤单调性，记录 invariant violation 并使该次评价失败；禁止 clamp。
4. `H_after > H_before`：同样是 invariant violation，不能靠舍入或截断静默修正。
5. `WorldCardinality.AtLeast`：不得伪造精确 entropy；可以输出带界限或 `UNKNOWN` 的指标状态。

建议显式建模：

```kotlin
sealed interface MetricValue {
    data class Exact(val value: Double) : MetricValue
    data class Bounded(val lower: Double?, val upper: Double?) : MetricValue
    data object Undefined : MetricValue
}
```

### 6.3 Demon structural entropy

```text
mechanicalShareDemon(seat)
= worldsWhereSeatIsDemon / totalWorlds

H_demon = -Σ q_i log2(q_i)
```

命名必须使用 `MechanicalDemonDistribution` / `DemonStructuralEntropy`，不得称为 `demonProbability`。

### 6.4 两套 taxonomy

以下概念必须独立：

```text
CandidateFamilyId
    说书人正在选择哪一类决策/线索

WorldExplanationClusterId
    剩余世界通过哪种机制解释已有观察
```

候选 family 例如：

```text
FIRST_NIGHT_PAIR_INFO
NUMERIC_INFO
MALFUNCTION_INFO
REGISTRATION_DEPENDENT_INFO
DEATH_REDIRECTION
```

世界解释 cluster 例如：

```text
TRUE_INFO
DRUNK_EXPLANATION
POISONED_EXPLANATION
SPY_REGISTRATION
RECLUSE_REGISTRATION
BARON_SETUP
STARPASS_PATH
```

二者是多对多关系，禁止共用一个枚举或用 candidate family 代替世界聚类。

### 6.5 fixed-point selector contract

原始 entropy 和研究报告可使用确定性 `Double`。凡进入稳定 selector 的派生分数必须：

- 明确 scale；
- 明确 rounding mode；
- 转成 fixed-point `Long`；
- 在 replay/debug 输出中保存原值和量化值；
- 相同 snapshot、policy、候选集合与 seed 必须得到相同排序和选择。

`FutureStorySpace` 中进入 selector 的任何浮点值遵守同一规则。

---

## 7. Cache 与性能契约

### 7.1 两级缓存

```text
Level 1: before-world cache
key = PlayerWorldSetIdentity

Level 2: candidate-after cache
key = Level1 key + canonical candidate observation ID
```

要求：

- Level 1 可复用同一玩家快照的基础世界集合；
- Level 2 必须是有界 LRU 或等价有界结构；
- Level 2 不应长期持有大量 materialized after-world sets；
- policy 权重变化不应失效世界缓存；
- knowledge、hypothesis、ruleset 或 semantic schema 变化必须形成不同 key；
- 缓存命中与否不得改变结果。

### 7.2 Exact 与 approximate 分离

```text
Exact path:
legality / SAT / UNSAT / registration / knowledge filtering

Approximate path:
future search / sampling / quality estimation under time budget
```

sampling failure、time budget 到期或 enumeration cap 只能影响近似质量，不得改变 exact legality。

---

## 8. “好线索”的三级质量模型

### 8.1 Level 1：规则与认知硬门槛

候选必须同时满足：

```text
Official legality
Recipient after-worlds non-empty
No private-information leakage
Registration facts complete and interaction-bound
Deterministic transition invariants
```

失败即 `INELIGIBLE`，任何策略权重不能救回。

### 8.2 Level 2：机械意义上的质量

评价：

- 信息是否推动推理；
- 是否过度压缩世界；
- Demon 位置是否过早集中；
- 是否仍有多个有规模的解释 cluster；
- 是否形成 confirmation lock；
- 是否暴露 Drunk/Poisoned 等 malfunction；
- 是否保留合理的未来说书空间。

核心策略为 `Epistemic Pacing`：让信息落在 Productive Band，而不是最大化 Information Gain。

### 8.3 Level 3：现场叙事适配

评价：

- 当前游戏阶段；
- 人数和存活结构；
- 邪恶 bluff 的合理支持；
- 历史线索分布；
- 当前 style/policy；
- `StoryDisruptionRisk`；
- Narrative Fairness 与 Earned Advantage。

Level 3 不能绕过 Level 1，也不能把玩家通过推理挣来的优势自动“平衡掉”。

---

## 9. Narrative Fairness、Earned Advantage 与 StoryDisruption

### 9.1 Narrative Fairness

自动说书人的目标不是动态维持 50:50 胜率，而是维护：

```text
规则公平
+ 信息质量
+ 可理解的叙事因果
+ 对玩家已取得成果的尊重
```

结构性偶然优势可以通过合法且节奏合理的信息适度缓和；玩家靠发言、逻辑和风险判断赢得的优势不应被系统自动惩罚。

### 9.2 当前可计算与不可计算边界

第一版可以可靠计算：

- 世界和 Demon 结构塌缩；
- confirmation chain；
- 同一 family 的历史机会与选择分布；
- 线索是否突然反转先前叙事；
- 说书人决策是否连续偏向某种机械效果。

第一版不能可靠断言：

- 玩家是否真正相信某个说法；
- 某种优势是否完全来自高水平社交推理；
- 桌上心理关系、语气和承诺；
- 真实胜率应被修正多少。

因此相关指标在第一版是解释和提示，不是自动篡改规则的许可。

### 9.3 `StoryDisruptionRisk`

它表达“某候选是否会突然破坏桌上已形成且仍有价值的叙事”，不是“这条线索一定坏”。

可计算特征包括：

- 与此前观察联合后 explanation cluster 的突变；
- 某种 malfunction 解释从边缘变为唯一解释；
- 某玩家或角色族被连续重复指向；
- 当前候选与历史候选形成非线性 hard solve；
- 恶魔 bluff 叙事被无必要地直接摧毁。

AUTO 可将高风险作为 penalty 或 gate；ASSISTED 应显示原因，让说书人决定现场价值。

---

## 10. Drunk / malfunction admission

`worldCount > 0` 只说明候选 mechanically credible，不等于适合自动采用。

候选至少经过两级门槛：

```text
Mechanically credible:
after-worlds is SAT

Auto-setup eligible:
通过保守的结构健康检查和质量 gate
```

Drunk 等感知身份规划的第一版保守条件可以包含：

- 至少两个 possible Demon seats；
- 非单一脆弱 explanation cluster；
- 不立即证明自身失能；
- 与其他首夜信息联合后不形成明显 hard solve；
- 有足够的后续合法信息空间。

这些阈值在 C2 专家标注阶段校准。在此之前宁可进入 `MANUAL_ONLY`，不能把“SAT”误当“健康”。

---

## 11. Runtime Policy 与 AUTO / ASSISTED

### 11.1 状态分层

```text
GameSetupState
    开局后不可随意改变的真实 setup

GameRuntimeState
    夜晚、死亡、提名、处决、状态和观察时间线

StorytellerPolicyState
    可在运行时修改的风格、节奏和辅助策略
```

Policy 变化不能改写历史事实或已经提交的决定。

### 11.2 精确生效规则

1. 已经执行的决定永久冻结，并保存当时的 policy snapshot。
2. 当前尚未提交的 decision 可以在 policy 变化后重新排序。
3. 决定一旦提交，后续 policy 变化从下一 decision 生效。
4. replay 必须能恢复 decision、候选、policy snapshot、score breakdown 和 seed。

### 11.3 统一 pipeline

AUTO 与 ASSISTED 共用：

```text
Legal candidates
→ Player-world evaluation
→ Shared hard gates
→ Mechanical and narrative scoring
→ Runtime policy weighting
→ Stable ranking
```

差别仅在最终执行：

- `AUTO`：从合格候选中按稳定策略自动选择；
- `ASSISTED`：向说书人展示候选、解释、风险和建议；
- `MANUAL_ONLY`：合法但风险或模型覆盖不足，不自动选择；
- `INELIGIBLE`：规则或认知硬门槛失败，不得选择。

---

## 12. Selection Distribution 与 withholding audit

### 12.1 产品风险

如果某类合法线索经常出现却几乎从不被系统选择，资深玩家可能从“系统长期不提供什么”反推隐藏状态。这是需要实测的产品风险，不是已被论文直接证明的 BotC 定律。

### 12.2 正确统计口径

至少记录：

```text
familyOpportunityCount
familyEligibleCount
familyHighestTierCount
familySelectedCount
selectionRateGivenEligibility
```

按以下维度分层：

```text
role family × player count × phase × style
```

只看总选择次数没有意义。核心问题是：某类候选在合格出现时，被选择的比例是否异常低。

### 12.3 representation floor 的边界

任何 minimum representation floor 只能在已经通过：

```text
legality
epistemic consistency
quality gates
```

的候选之间生效。禁止为了分布好看选择规则不合法、认知矛盾或明显破坏游戏的线索。

---

## 13. 性能策略与 ZDD 退出条件

### 13.1 设备基线

移动端实测至少使用：

- POCO X5：最低性能基线；
- POCO X8：参考设备。

记录：

```text
cold/warm P50
cold/warm P95
peak memory
cache hit rate
world cardinality type
fallback count
no-ANR result
```

### 13.2 目标与硬门槛分开

在取得设备数据前：

- 15 ms 单次操作、8 MB world-engine memory 可作为工程目标；
- 不把它们直接写成未验证的绝对淘汰线；
- A4 必须根据真实场景分布提出“目标值、可接受上限、降级策略”三层结论；
- 正确性优先于速度，近似只能用于未来质量估计。

### 13.3 降级策略

若 ZDD 在某场景无法满足预算：

- 不降级规则合法性；
- 可降级解释 cluster 细节或 forward-search 预算；
- 可进入 ASSISTED / MANUAL_ONLY；
- 必须记录 telemetry，不得静默返回错误世界数。

---

## 14. 分阶段实施路线

### Phase A：Formal Rules + World Engine Foundation

#### A0：Reference Freeze & Evaluation — 已完成

产物：

```text
docs/external_solver_evaluation.md
docs/epistemic_reference_matrix.md
```

已冻结外部参考、许可证边界和 33 个语义 golden scenarios。

#### A1：Unified Semantic Model — 已完成

已建立六个语义根对象、canonical JSON、稳定 SHA-256 ID 和接收者边界校验；尚未接入生产 selector。

#### A2：ASP Oracle Cross-validation Harness — 已完成

已建立开发期 harness 和 18 个可执行优先场景。冻结基线：

```text
AGREE                     17
EXPECTED_COVERAGE_GAP      1
KNOWN_ORACLE_VARIANCE      0
UNEXPLAINED_MISMATCH       0
NOT_RUN                    0
```

#### A1.1：Semantic Contract Hardening — 已完成

任务：

- 新增 interaction-scoped `RegistrationSemantics`；
- 区分 registration capability 与 selected `RegistrationFact`；
- 新增 `EpistemicHypothesis`；
- 新增 `WorldCardinality`；
- 固定 knowledge-based `PlayerWorldSetIdentity`；
- 建模 Spy grimoire observation；
- 分离 candidate family 与 world explanation taxonomy；
- 将 epistemic schema 升级到 v2；
- 保持生产选择结果不变。

退出条件：

- registration 受 interaction/query 约束；
- red herring 使用实际阵营且有回归测试；
- snapshot/cache identity 不含玩家未知秘密；
- 所有 A1/A1.1 序列化、迁移和防泄漏测试通过。

#### A2.1：Golden Corpus & Oracle Authority

任务：

- 把权威顺序写入 harness/report；
- 新增 Spy/Recluse red-herring 场景；
- 记录 frozen Oracle 的 Spy red-herring 偏差为 `KNOWN_ORACLE_VARIANCE`；
- 将 A0 的 33 个场景尽量全部转为 executable fixtures；
- 按 coverage 扩展到约 45–60 个场景；
- 补齐多夜 poison、starpass、registration interaction 和每个 TB 核心能力；
- 为 CHOICE 场景断言完整候选与 bound registration facts。

场景数量是 coverage 目标，不为凑数量制造无意义 UNSAT。

退出条件：

```text
All official golden scenarios pass
+ UNEXPLAINED_MISMATCH == 0
+ NOT_RUN == 0 in the release baseline
+ every Oracle conflict is documented as coverage gap or known variance
```

#### A3：EnumeratedWorldSet Baseline

实现透明、可调试的 exact baseline：

- setup distribution；
- seat assignments；
- registration semantics；
- player-perspective filtering；
- exact SAT/UNSAT；
- exact/lower-bound cardinality；
- explanation cluster baseline。

退出条件：

```text
所有 official executable golden scenarios 通过
+ 无未解释 ASP 差异
+ SAT/UNSAT 不受 enumeration cap 影响
+ registration choices 与 candidate 稳定绑定
```

#### A4：ZddPlayerWorldSet Prototype

实现或适配：

```text
require / exclude / count
possibleRoles / possibleDemonSeats / possibleValues
snapshot / undo
WorldCardinality
```

三方验证：

```text
Official golden expectation
vs EnumeratedWorldSet
vs ZddPlayerWorldSet
with ASP as external cross-check
```

在 POCO X5/X8 完成正确性、P50/P95、内存和降级策略评估后，才决定是否作为 runtime 实现。

### Phase B：Player Epistemic Correctness

#### B1：PlayerWorldSet Domain Integration

- 将 A3/A4 接入真实 game snapshot；
- 每个接收者构造独立 knowledge snapshot；
- hypothesisMode 贯穿构造、查询与缓存；
- 保持 actual-world narrative metrics 与 player-world metrics 分离。

#### B2：First-night Epistemic Gate

- WW/Librarian/Investigator；
- Chef/Empath/Fortune Teller；
- Spy/Recluse local registration；
- 所有候选执行 `before → observation → after`；
- `after == empty` 直接 `INELIGIBLE`。

#### B3：Drunk Perceived Role Joint Planning

- shown role 与相关 observations 联合规划；
- 区分 mechanically credible 与 auto-setup eligible；
- 不证明自身失能；
- 保留健康世界、Demon 分散度和叙事分支。

#### B4：Multi-night PlayerWorldSet

- death/execution/nominations timeline；
- poison duration 与换目标；
- Empath living-neighbour；
- Undertaker/Ravenkeeper；
- Imp starpass、Scarlet Woman、Mayor、Monk/Soldier；
- snapshot/undo/replay。

### Phase C：Quality, Policy and Production

#### C1：Entropy Metrics Shadow Mode

- 实现世界、Demon 和基本 narrative metrics；
- 实现边界条件与 invariant violation；
- 不改变生产选择。

#### C2：Expert-labelled Pacing + Narrative Dataset

- 对固定场景标注 `too weak / productive / too strong`；
- 标注 structural advantage 与 earned advantage；
- 校准 role family × phase × player count 的宽 Productive Band；
- 校准 Drunk auto-setup eligibility。

#### C3：Pacing Policy + Structural Balance v1

- information usefulness；
- over-compression penalty；
- Demon collapse penalty；
- narrative collapse penalty；
- 不实施动态 50:50 胜率修正。

#### C4：StoryDisruptionEvaluation Shadow / Assisted Hint

- 输出风险特征和解释；
- 先服务 ASSISTED，不直接作为未经验证的 AUTO hard gate。

#### C5：Runtime Storyteller Policy State

- 可运行时修改 style/pacing；
- 精确提交边界；
- policy snapshot 与 replay。

#### C6：AUTO / ASSISTED Unified Pipeline

- 共用候选、gate、metrics、ranking；
- 明确 `INELIGIBLE` / `MANUAL_ONLY` / eligible；
- ASSISTED 展示原因而不是另一套算法。

#### C7：Replace Heuristic Metrics

- 逐项替换旧 Information/Ambiguity/HardSolve 等 heuristic；
- 每项替换必须有 shadow comparison 与回归；
- 保留不能由 world metrics 表达的实际世界叙事指标。

#### C8：Selection Distribution & Withholding Audit

- 记录 opportunity/eligible/highest-tier/selected；
- 计算 selection rate given eligibility；
- 按 family × player count × phase × style 分层；
- 只在合格候选内评估 representation floor；
- 未经数据验证不自动强制分布。

#### C9：Unified Selector Production Rollout

- stable fixed-point ranking；
- staged rollout / shadow diff；
- 设备性能与 replay 验证；
- 生产 selector 只有在 correctness、quality、distribution gates 全部满足后切换。

### Phase D：ISES-style Limited Forward Search

仅在 C9 稳定后实施：

```text
D1 FutureStorySpace model
D2 Depth-1 exact/limited future evaluation
D3 Budgeted sampling with fixed seed
D4 Depth-2 experimental evaluation
```

限制：

- 不构建完整 game tree；
- sampling 只评估未来质量；
- exact legality 不依赖 sampling；
- 进入 selector 的 Double 必须 fixed-point 量化；
- 时间到返回可解释的当前最佳近似，不返回伪 UNSAT。

### Phase E：未来 Soft Evidence / Social Story Model

未来可参考 CSP4SDG：

```text
Hard mechanically possible worlds
+ weighted soft claims/evidence
+ posterior belief model
```

在获得真实数据与可解释权重前，不将玩家声明直接转成伪精确概率。

---

## 15. Golden corpus coverage 要求

A2.1 的 45–60 个 executable scenarios 应由 coverage 驱动，至少包含：

| 领域 | 必须覆盖 |
|---|---|
| Setup | 标准人数、Baron、Drunk、Teensyville/标准邪恶知识 |
| Pair info | WW、Librarian、Investigator 的 SAT/UNSAT/zero 分支 |
| Numeric info | Chef 邻接、Recluse 登记、Empath living neighbours |
| Fortune Teller | Demon、red herring、Recluse、Spy red-herring prohibition |
| Malfunction | Drunk、Poisoned truth/false、跨夜 poison duration |
| Registration | interaction-local Spy/Recluse、CHOICE 候选绑定 |
| Night protection/death | Imp、Soldier、Monk、Mayor redirect |
| Role transition | starpass、Scarlet Woman |
| Death information | Undertaker、Ravenkeeper |
| Day/game end | Virgin、Slayer/Recluse、Saint、Demon death |
| Knowledge boundary | Spy grimoire、恶魔/爪牙知识、禁止私密泄漏 |

每个适用 fixture 至少断言：

```text
actual-world legality
recipient knowledge boundary
before-world SAT
after-world SAT/UNSAT
legal outputs or transition
bound registration facts
hypothesisMode
stable canonical serialization and ID
```

---

## 16. 测试与验收总门槛

### 16.1 Correctness Gate

- 官方 golden expectation 全部通过；
- 无 unexplained mismatch；
- `NOT_RUN` 不得冒充通过；
- SAT/UNSAT 不受枚举上限、cache 或 sampling 影响；
- red herring 和 registration 有独立回归。

### 16.2 Epistemic Gate

- 玩家只看到公开事实和发给自己的私有事实；
- actual role、poison target、red herring、bluff 不泄漏；
- Drunk shown token 与 actual role 分离；
- Spy grimoire 只进入 Spy 对应时间点的 observation；
- 不同 hypothesisMode 的世界和缓存严格分离。

### 16.3 Metric Gate

- singleton、empty after、invariant violation 有明确测试；
- `Exact` 与 `AtLeast` 不混淆；
- structural share 不命名为概率；
- selector fixed-point 可稳定 replay；
- cache hit/miss 输出一致。

### 16.4 Narrative / Policy Gate

- earned advantage 不被简单 50:50 平衡抵消；
- policy 变化不改写已提交决定；
- AUTO/ASSISTED 共用完整 pipeline；
- `MANUAL_ONLY` 与 `INELIGIBLE` 行为不同且 UI 清晰；
- StoryDisruption 第一阶段有解释、可审计。

### 16.5 Performance Gate

- POCO X5/X8 有真实 P50/P95 和 peak memory；
- 无 ANR；
- 超时或资源不足采用显式降级；
- correctness path 不因性能预算变成 approximate。

### 16.6 Distribution Gate

- opportunity 与 selection 分母正确；
- 可按 family/player count/phase/style 重放分析；
- representation 策略只作用于合格候选；
- 上线前审查是否存在稳定 withholding signal。

---

## 17. 第一版正式完成定义

第一版生产完成不再是“有一个 entropy 分数”，而是同时满足：

```text
1. Official-rule correctness corpus 通过
2. PlayerWorldSet exact correctness 通过
3. Enumerated 与 runtime 实现一致
4. Player knowledge 无秘密泄漏
5. Drunk/Poisoned/Spy/Recluse 等关键机制可解释
6. Entropy 与 structural metrics 在 shadow mode 校准
7. Narrative Fairness 和 StoryDisruption 有明确边界
8. Runtime policy 可回放
9. AUTO/ASSISTED 共用 selector
10. Withholding/distribution audit 通过
11. POCO X5/X8 性能与降级策略通过
12. C9 完成 staged production rollout
```

Phase D forward search 和 Phase E soft belief 不属于第一版生产完成的必要条件。

---

## 18. 最终设计原则

1. 官方规则高于外部 Oracle。
2. Possible Worlds 是可验证基础层，不是装饰性分数。
3. 玩家视角与说书人真相永远分离。
4. Registration 是 interaction-scoped choice，不是永久角色改写。
5. Red herring 使用实际好人资格，不使用互动登记。
6. SAT 只代表可信，不代表优质或适合 AUTO。
7. exact correctness 不依赖枚举 cap 或 sampling。
8. 世界数量必须表达 exact、lower bound 或 unknown，不能溢出伪装。
9. Entropy 不是玩家真实概率，也不是越低越好。
10. 候选 family 与世界 explanation taxonomy 分离。
11. 玩家挣来的优势应被尊重，不做动态 50:50 操纵。
12. AUTO 与 ASSISTED 共用同一套规则、世界和评分事实。
13. Policy 可以运行时调整，但不能改写已经提交的历史。
14. 稳定选择必须 fixed-point、可回放、可解释。
15. 分布优化只能在合格候选之间进行。
16. 移动端性能目标必须由真实设备数据校准。
17. Forward search 后置，底层正确性优先。
18. 每个阶段都以明确 golden gates 退出，不以“代码已写完”退出。

