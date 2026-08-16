# CampBoardGameHost 自动说书人玩家认知一致性算法改进方案

> 版本：1.1  
> 日期：2026-08-11  
> 状态：设计稿，尚未实施  
> 适用范围：优先覆盖《暗流涌动》，架构需支持后续剧本扩展  
> 与既有设计的关系：作为 V4 自动说书人算法的认知一致性补充与后续改进依据

---

## 1. 执行摘要

当前算法已经能够生成合法候选、区分稳健/平衡/激进风格并进行稳定随机，但仍缺少一个关键评价层：

> 系统没有判断玩家收到信息后，是否仍能相信自己的身份和能力处于正常状态。

因此，规则上允许的醉酒或中毒假信息，可能在玩家视角下明显不可能。例如：

- 8 人局中，酒鬼显示为图书管理员，却收到“没有外来者”；
- 8 人局中，酒鬼显示为调查员，却收到“没有爪牙”；
- 多夜信息单独看均合法，但合并后无法由任何正常能力世界共同解释；
- 酒鬼信息、恶魔伪装和真实邪恶目标组合后形成过强确认链。

本方案不为这些案例逐一增加角色或人数硬编码，而是新增统一的“玩家认知一致性评价”层：

1. 从信息接收者视角构建其合理知道的事实；
2. 假设该玩家相信自己的显示身份真实且能力正常；
3. 根据剧本、人数、设置修正、公开事实和历史私有信息构造合理世界；
4. 把候选信息加入这些世界；
5. 若正常能力假设下不存在任何合理世界，候选自动失去推荐资格；
6. 若候选虽然可解释，但明显迫使玩家怀疑自己醉酒或中毒，同样不得进入自动推荐池；
7. 只有通过认知一致性的完整候选，才进入阵营影响、确认链、历史压力、风格评分和稳定随机。

最终流水线调整为：

```text
生成完整候选
→ 官方合法性
→ 玩家认知一致性
→ 能力失效暴露风险
→ 全局叙事与确认链风险
→ 当前风格评分
→ 最高质量等级过滤
→ 分数容差过滤
→ 稳定加权随机
```

随机性只用于多个合格方案之间的多样性，不允许低质量或认知不一致方案重新获得概率。

---

## 2. 当前问题与根因

### 2.1 酒鬼显示身份和具体信息被分开评价

当前设置算法可以选择酒鬼显示为图书管理员、共情者等镇民，但除了调查员的部分特殊流程外，不会在选择显示身份时同时评价最终给出的信息。

这会形成以下断裂：

```text
设置阶段：图书管理员是高适配酒鬼身份
夜间阶段：“没有外来者”是一个合法的不可靠候选
最终结果：两个局部合法决定组合成明显自证酒鬼的完整方案
```

### 2.2 静态角色适配分替代了局面评价

当前 `drunkSuitability` 只是固定先验，无法判断：

- 当前人数是否允许某种零结果；
- 某个显示角色是否存在可信的具体信息；
- 多夜角色是否能持续产生前后一致的信息；
- 信息是否与公开事实、登记和此前信息冲突；
- 信息是否会快速暴露能力失效。

### 2.3 “合法假信息”被误认为“适合自动推荐”

醉酒或中毒允许说书人给出错误信息，但“规则允许”不等于“适合自动说书人默认采用”。

当前评分主要考虑角色暴露度、讨论价值、目标压力和信息真假。没有目标的零结果往往具有：

- 目标暴露度为 0；
- 历史目标压力为 0；
- 较低的数值误导压力。

因此，“没有外来者”之类的结果可能被反向评价为稳健方案。

### 2.4 两阶段选择破坏了风格边界

当前部分动态信息先分别选出稳健、平衡、激进代表，再由第二层选择器在这些代表之间随机。结果是平衡自动模式仍可能抽取稳健或激进代表。

第二层选择没有继承完整场景评分和质量等级，容易让第一层只作为“展示推荐”，而不是实际的自动选择约束。

### 2.5 当前测试缺少接收者视角的端到端组合

现有测试分别覆盖候选生成、静态评分、误导概率和稳定随机，但没有系统验证：

```text
酒鬼显示身份
+ 当前人数和设置结构
+ 最终展示信息
+ 历史信息
+ 最终自动选择
```

组合后是否仍具有玩家视角可信度。

---

## 3. 目标与非目标

### 3.1 目标

本次改进必须实现：

1. 统一评价首夜和后续夜晚的信息候选；
2. 自动识别在玩家正常能力假设下不存在合理世界的候选；
3. 自动识别明显暴露醉酒、中毒或其他能力失效状态的候选；
4. 将酒鬼显示身份与其首个信息结果联合评价；
5. 累积评价多夜信息的时间一致性；
6. 将玩家认知一致性与全局阵营平衡、恶魔伪装、确认链风险分开建模；
7. 移除针对具体人数、角色和结果的产品硬编码；
8. 保持同一决策可复现；
9. 给每次淘汰、降级和选择提供可审计理由；
10. 为后续剧本提供数据驱动的规则扩展点。

### 3.2 非目标

本次不要求：

- 精确预测阵营胜率；
- 模拟所有玩家的真实心理和发言；
- 把玩家声明当作绝对事实；
- 保证醉酒或中毒信息永远彼此一致；
- 让自动说书人取代熟练说书人的所有创造性裁定；
- 一次完成全部官方剧本的角色语义编码。

系统目标是排除明显不可信和自我暴露的自动推荐，同时保留多个合理但不同的叙事方向。

---

## 4. 核心定义

### 4.1 真实世界

系统掌握的实际局面，包括真实身份、醉酒、中毒、特殊登记、恶魔伪装及实际历史事件。

真实世界用于：

- 官方合法性判断；
- 判断信息语义是真或假；
- 阵营影响和确认链评价；
- 赛后复盘。

### 4.2 玩家认知世界

信息接收者根据公开规则、公开事实、自己认为的身份和自己收到的信息，可以合理相信的世界。

玩家认知世界不能直接包含说书人才知道的真实身份。

### 4.3 正常能力假设

评价候选时，暂时假设：

```text
接收者的显示身份就是其真实身份；
接收者当前健康且清醒；
该能力按其正常语义生效。
```

若候选在这个假设下没有任何合理世界，它就会直接提示玩家“我的身份或能力有问题”。

### 4.4 合理世界

同时满足以下约束的角色与状态分配：

- 当前剧本角色集合；
- 玩家人数对应的基础角色数量；
- 男爵等角色的设置修正；
- 角色唯一性和阵营约束；
- 已确认公开事件；
- 接收者的身份认知；
- 接收者此前收到的信息；
- 当前候选信息的正常能力语义。

### 4.5 自动推荐资格

自动推荐资格与官方合法性、质量等级分开：

```kotlin
enum class AutomaticEligibility {
    ELIGIBLE,
    MANUAL_ONLY,
    INELIGIBLE,
}
```

- `ELIGIBLE`：可进入自动推荐池；
- `MANUAL_ONLY`：规则允许，但自动模式不采用，可供熟练说书人手动裁定；
- `INELIGIBLE`：候选语义或规则不成立，不应展示为推荐。

---

## 5. 总体架构

新增四个相互独立的评价层：

```text
┌──────────────────────────┐
│ CompleteCandidateGenerator│
└─────────────┬────────────┘
              ↓
┌──────────────────────────┐
│ OfficialLegalityValidator │
└─────────────┬────────────┘
              ↓
┌──────────────────────────────┐
│ EpistemicConsistencyEvaluator │
└─────────────┬────────────────┘
              ↓
┌──────────────────────────────┐
│ NarrativeConsequenceEvaluator │
└─────────────┬────────────────┘
              ↓
┌──────────────────────────┐
│ StyleEvaluator + Selector │
└──────────────────────────┘
```

职责边界：

| 模块 | 回答的问题 |
|---|---|
| 官方合法性 | 规则是否允许这个结果或裁定？ |
| 玩家认知一致性 | 玩家相信能力正常时，这条信息是否存在合理解释？ |
| 能力失效暴露 | 这条信息是否明显迫使玩家怀疑自己醉酒或中毒？ |
| 全局叙事后果 | 是否过度确认恶魔伪装、锁死玩家或形成不自然确认链？ |
| 风格评分 | 在所有合格结果中，当前风格偏好哪一种？ |
| 稳定随机 | 多个同质量方案中，本局选择哪一个？ |

---

## 6. 统一完整候选模型

### 6.1 信息命题

所有展示信息必须转换为可求解语义，而不能只保存界面字符串：

```kotlin
sealed interface InformationProposition {
    data class NoCharacterType(
        val type: CharacterType,
    ) : InformationProposition

    data class OneOfSeatsHasRole(
        val role: RoleId,
        val seats: Set<Int>,
    ) : InformationProposition

    data class NumberResult(
        val abilityRole: RoleId,
        val value: Int,
        val targetSeats: Set<Int> = emptySet(),
    ) : InformationProposition

    data class YesNoResult(
        val abilityRole: RoleId,
        val answer: Boolean,
        val targetSeats: Set<Int>,
    ) : InformationProposition

    data class RevealedRole(
        val subjectSeat: Int,
        val role: RoleId,
    ) : InformationProposition
}
```

后续可扩展死亡原因、阵营、相邻关系、距离等命题。

### 6.2 完整信息候选

```kotlin
data class CompleteInformationCandidate(
    val candidateId: String,
    val recipientSeat: Int,
    val actualRole: RoleId,
    val perceivedRole: RoleId,
    val abilityState: AbilityState,
    val proposition: InformationProposition,
    val targets: Set<Int>,
    val registrations: List<RegistrationFact>,
    val setupContext: SetupContext,
    val temporalContext: TemporalContext,
)
```

其中：

- `actualRole` 用于官方规则和复盘；
- `perceivedRole` 用于玩家认知求解；
- `proposition` 是最终展示给玩家的信息语义；
- `registrations` 与本次显示绑定，不能二次独立选择；
- `setupContext` 包含人数、剧本和设置修正；
- `temporalContext` 标明夜晚、轮次和当时状态快照。

### 6.3 酒鬼显示身份计划

酒鬼首夜信息角色应生成完整计划：

```kotlin
data class DrunkPerceivedRolePlan(
    val perceivedRole: RoleId,
    val initialInformation: CompleteInformationCandidate?,
    val futureFeasibility: FutureInformationFeasibility,
)
```

不再只保存：

```text
DrunkShownRole(Librarian)
```

而应保存：

```text
酒鬼显示为图书管理员
+ 展示“管家在 2/6 中”
+ 本次登记
+ 认知一致性评价摘要
```

对于没有首夜信息的角色，`initialInformation` 可以为空，但必须有相应能力执行计划或可行性评价。

---

## 7. 玩家认知快照

```kotlin
data class PlayerKnowledgeSnapshot(
    val recipientSeat: Int,
    val playerCount: Int,
    val script: ScriptId,
    val perceivedRole: RoleId,
    val publicFacts: List<PublicFact>,
    val privateObservations: List<EpistemicObservation>,
    val knownSetupConstraints: List<SetupConstraint>,
    val snapshotVersion: String,
)
```

### 7.1 可以进入的事实

- 玩家人数和剧本；
- 公开角色数量规则；
- 角色卡上公开可知的设置修正规则；
- 已发生并被所有玩家确认的公开能力触发；
- 公开死亡、处决和存活状态；
- 该接收者此前收到的私有信息；
- 该接收者当时选择的目标；
- 该接收者认为自己的身份。

### 7.2 不能作为硬事实的内容

- 说书人才知道的真实身份；
- 恶魔得到的三个伪装身份；
- 未公开的中毒目标；
- 其他玩家未经验证的口头声明；
- 说书人主观猜测玩家相信什么。

玩家声明如未来纳入系统，只能作为带置信度的软证据，不能直接让合理世界变为零。

### 7.3 认知快照必须按时间保存

后续夜晚不能使用当前状态重新解释首夜。每条信息必须关联当时的：

- 存活座位；
- 相邻关系；
- 目标选择；
- 公开事件；
- 适用规则版本。

---

## 8. 合理世界求解器

### 8.1 接口

```kotlin
interface EpistemicWorldSolver {
    fun solve(
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: AbilityHypothesis,
        additionalProposition: InformationProposition?,
        witnessLimit: Int,
    ): WorldSolveResult
}

sealed interface AbilityHypothesis {
    data object FunctioningAsPerceivedRole : AbilityHypothesis
    data object AbilityMayMalfunction : AbilityHypothesis
}
```

```kotlin
data class WorldSolveResult(
    val satisfiable: Boolean,
    val exactUnsat: Boolean,
    val witnessCount: Int,
    val truncated: Boolean,
    val setupProfiles: Set<SetupProfileSignature>,
    val targetExplanations: Set<TargetExplanationSignature>,
    val contradictionCore: List<ConstraintId>,
)
```

### 8.2 求解要求

“是否存在合理世界”必须是精确结论：

- `UNSAT` 必须经过完整约束证明；
- 不能因为枚举达到上限就误判为没有世界；
- 找到一个有效见证即可证明 `SAT`；
- 世界数量、覆盖率和多样性可以使用上限截断或近似统计。

### 8.3 规则数据化

人数结构和设置修正不得散落在评价器中：

```kotlin
data class SetupModifier(
    val sourceRole: RoleId,
    val teamDeltas: Map<CharacterType, Int>,
    val activationConstraint: ConstraintExpression,
)
```

男爵示例：

```text
sourceRole = Baron
OUTSIDER +2
TOWNSFOLK -2
```

求解器只理解通用约束，不理解“8 人图书管理员”这种产品特例。

### 8.4 性能策略

《暗流涌动》规模可采用约束传播和回溯：

1. 先求角色类型数量；
2. 再求必要角色是否在场；
3. 只对候选涉及的座位和角色做精确分配；
4. 未涉及座位使用计数变量而非全排列；
5. 对相同认知快照缓存基础求解结果；
6. 对每个候选只增量加入一个命题；
7. 合理世界见证达到上限后停止计数，但继续保证 `UNSAT` 判断正确。

不建议直接穷举所有角色到所有座位的完整排列。

---

## 9. 认知一致性评价

### 9.1 基础求解

对每个候选执行：

```text
before = 玩家知识 + 正常能力假设
after  = 玩家知识 + 正常能力假设 + 当前候选信息
```

### 9.2 评价结果

```kotlin
data class EpistemicConsistencyEvaluation(
    val functioningWorldExists: Boolean,
    val functioningWitnessCount: Int,
    val setupProfileCount: Int,
    val targetExplanationCount: Int,
    val survivalRatioFixedPoint: Long,
    val malfunctionExposure: MalfunctionExposure,
    val automaticEligibility: AutomaticEligibility,
    val explanationCodes: List<String>,
)
```

### 9.3 合理世界压缩到零

若：

```text
before.satisfiable == true
after.satisfiable == false
```

则：

```text
automaticEligibility = INELIGIBLE
qualityTier = REJECTED
explanation = epistemic.no-functioning-world
```

这是一条通用语义规则，不是针对具体角色的硬门槛。

### 9.4 既有认知已经矛盾

若 `before` 已经无解，说明玩家此前的信息或公开事实已经暴露能力异常。此时：

- 不应把全部责任记到当前候选；
- 当前候选应以“是否进一步增加明显矛盾”为标准；
- 自动模式优先选择能够恢复多个局部解释、避免继续强化自证的信息；
- 记录 `epistemic.preexisting-contradiction` 供复盘。

### 9.5 明显暴露能力失效

```kotlin
enum class MalfunctionExposure {
    NONE,
    LOW,
    HIGH,
    NEAR_CERTAIN,
}
```

评价依据不绑定具体角色，综合考虑：

- 正常能力世界是否只剩极少数见证；
- 是否所有见证都依赖同一个罕见且未被支持的设置结构；
- 是否只剩一个极端脆弱的目标解释；
- 新候选相对信息前压缩了多少正常世界；
- “能力可能失效”假设是否远比“能力正常”更容易解释信息时间线；
- 新候选是否与接收者此前多条信息共同形成矛盾核心。

资格映射：

| 暴露等级 | 自动资格 | 说明 |
|---|---|---|
| `NONE` | `ELIGIBLE` | 正常参与推荐 |
| `LOW` | `ELIGIBLE` | 可轻度扣分 |
| `HIGH` | `MANUAL_ONLY` | 自动模式不采用 |
| `NEAR_CERTAIN` | `INELIGIBLE` | 不作为推荐展示 |

阈值必须通过专家标注和模拟校准，不能由多个无关小额加分抵消。

---

## 10. 首夜与酒鬼显示身份联合评价

### 10.1 候选生成方式

对每个不在场的镇民显示身份：

1. 生成该身份所有格式合法的首夜信息；
2. 将显示身份与每条具体信息组合；
3. 运行认知一致性评价；
4. 运行全局叙事评价；
5. 只有至少一个 `ELIGIBLE` 完整候选时，才允许该显示身份进入自动设置推荐。

伪代码：

```kotlin
for (perceivedRole in outOfPlayTownsfolk) {
    val informationCandidates = initialInformationGenerator.generate(
        game = game,
        recipient = drunkPlayer,
        perceivedRole = perceivedRole,
    )

    val evaluated = informationCandidates.map { candidate ->
        unifiedEvaluator.evaluate(candidate)
    }

    val eligible = evaluated.filter {
        it.automaticEligibility == AutomaticEligibility.ELIGIBLE
    }

    if (eligible.isNotEmpty() || perceivedRole.hasNoInitialInformation()) {
        addDrunkRolePlans(perceivedRole, eligible)
    }
}
```

### 10.2 静态适配度的新定位

原有 `drunkSuitability` 可以保留为弱先验，只在两个完整候选的认知质量和叙事质量接近时参与排序。

建议优先级：

```text
认知一致性
> 能力失效暴露
> 全局确认链风险
> 首夜/多夜可持续性
> 具体信息质量
> 静态角色先验
```

静态先验不得使认知不一致候选恢复自动资格。

### 10.3 首夜零结果示例

#### 8 人酒鬼图书管理员：“没有外来者”

玩家视角约束：

```text
8 人基础外来者数量为 1
玩家相信自己是镇民图书管理员
男爵只增加外来者
候选声称外来者数量为 0
```

正常能力世界无解，因此自动不合格。

#### 允许零外来者的配置

如果当前人数和所有可能设置修正允许零外来者，则“没有外来者”仍可存在正常能力世界，不会被特殊禁用。

这说明结果来自统一规则求解，而不是人数硬编码。

### 10.4 调查员展示男爵示例

8 人酒鬼调查员收到“2/7 中有男爵”：

- 男爵在场会把外来者数量增加 2；
- 这是玩家根据剧本规则能够相信的合法设置世界；
- 只要存在一个合理世界让 2 号或 7 号成为男爵，认知一致性评价通过；
- 不能因为真实场上没有男爵而拒绝，因为酒鬼允许收到可信的错误信息。

如果后续公开事实已经使所有男爵世界都无法成立，该信息才会自动失去资格。

---

## 11. 后续夜晚与认知时间线

### 11.1 统一覆盖整局

认知一致性评价必须用于：

- 首夜信息；
- 每夜数字信息；
- 玩家主动选择目标后的结果；
- 死亡触发能力；
- 身份展示；
- 特殊登记相关信息；
- 酒鬼和中毒玩家的所有不可靠信息。

### 11.2 时间线模型

```kotlin
data class PlayerEpistemicTimeline(
    val recipientSeat: Int,
    val perceivedRole: RoleId,
    val observations: List<EpistemicObservation>,
)

data class EpistemicObservation(
    val phase: StorytellerPhase,
    val round: Int,
    val snapshotRef: String,
    val proposition: InformationProposition,
    val selectedTargets: Set<Int>,
    val publicFactsAtTime: Set<PublicFact>,
)
```

每次推荐时求解：

```text
截至当前的全部认知时间线
+ 当前候选
+ 各时间点对应的状态转换
```

### 11.3 状态变化必须允许解释前后差异

前后信息不同不能直接视为矛盾。求解器必须允许：

- 玩家死亡导致相邻关系变化；
- 小恶魔换人；
- 本夜中毒状态变化；
- 隐士或间谍在不同交互中采用不同合法登记；
- 目标选择变化；
- 一次性能力触发条件变化。

只有不存在任何合法状态路径时，才判定认知不一致。

### 11.4 持续信息角色不做角色专用处理

共情者、占卜师等角色不需要单独增加“优先”或“禁止”条件。统一算法评价：

- 当前信息是否可信；
- 历史信息能否共同解释；
- 未来若干常见状态变化下是否仍存在可维护路径；
- 是否形成明显能力失效暴露。

角色的首夜/每夜属性只作为能力语义和维护成本输入。

---

## 12. 全局叙事与确认链评价

玩家认知一致性只回答“玩家是否能相信这条信息”，不能代替上帝视角的全局质量评价。

### 12.1 独立评价指标

```kotlin
data class NarrativeRiskEvaluation(
    val demonBluffConfirmationRisk: Int,
    val realEvilExposureRisk: Int,
    val innocentLockRisk: Int,
    val crossClueConfirmationRisk: Int,
    val repeatedTargetPressure: Int,
    val discussionValue: Int,
    val explanationCodes: List<String>,
)
```

### 12.2 恶魔伪装与酒鬼信息

例如：

```text
酒鬼显示为图书管理员
展示“管家在 3/7 中”
7 号是真实恶魔
管家是恶魔得到的伪装身份
```

从酒鬼玩家视角，这条信息可以完全合理，因此认知一致性应通过。

但全局评价必须识别：

- 展示身份与恶魔伪装重合；
- 真实恶魔被放入候选对；
- 两者组合可能给恶魔声明提供强确认；
- 若还有其他线索支持该声明，可能形成近似锁死确认链。

处理方式：

- 轻度伪装支持可以保留并参与平衡评分；
- 过强双向确认降为 `MANUAL_ONLY`；
- 近似锁死且缺乏替代世界时自动不合格。

这不是认知矛盾，而是全局叙事风险，必须保留独立解释代码。

---

## 13. 质量分层与选择算法

### 13.1 评价结果

```kotlin
data class UnifiedCandidateEvaluation<T>(
    val candidate: DecisionCandidate<T>,
    val legality: LegalityStatus,
    val automaticEligibility: AutomaticEligibility,
    val qualityTier: QualityTier,
    val epistemic: EpistemicConsistencyEvaluation,
    val narrative: NarrativeRiskEvaluation,
    val styleScore: Int,
    val warnings: List<String>,
    val explanations: List<String>,
)
```

### 13.2 选择顺序

自动模式严格执行：

```kotlin
val eligible = evaluations.filter {
    it.legality == LEGAL &&
        it.automaticEligibility == ELIGIBLE
}

val bestTier = eligible.maxOf { it.qualityTier }
val sameTier = eligible.filter { it.qualityTier == bestTier }
val bestScore = sameTier.maxOf { it.styleScore }
val withinTolerance = sameTier.filter {
    it.styleScore >= bestScore - tolerance
}

return stableWeightedSelect(withinTolerance)
```

### 13.3 禁止跨风格二次随机

用户选择平衡模式时：

- 所有候选使用平衡评价参数；
- 只在平衡候选池中选择；
- 稳健和激进结果可以用于人工比较，但不得重新混入平衡自动池。

### 13.4 随机权重不得绕过资格和等级

以下字段只能在完成资格过滤后影响权重：

- 分数差；
- 历史冷却；
- 多样性；
- 稳定随机种子。

任何随机权重都不能让 `MANUAL_ONLY`、`INELIGIBLE` 或较低质量等级候选重新出现。

---

## 14. 解释代码与审计

建议新增标准解释代码：

```text
epistemic.functioning-world-exists
epistemic.no-functioning-world
epistemic.preexisting-contradiction
epistemic.low-world-survival
epistemic.single-fragile-setup
epistemic.single-target-explanation
epistemic.high-malfunction-exposure
epistemic.near-certain-malfunction-exposure
narrative.demon-bluff-confirmation
narrative.real-evil-in-false-pair
narrative.cross-clue-lock
narrative.innocent-lock-risk
selection.current-style-only
selection.highest-quality-tier
selection.within-score-tolerance
selection.stable-weighted-random
```

每次自动决定至少记录：

- 候选语义；
- 认知快照版本；
- 求解结果摘要；
- 自动资格；
- 质量等级；
- 风格分数；
- 淘汰或选择原因；
- 规则内容哈希；
- 稳定随机种子派生键。

赛后复盘应能回答：

> 为什么这条假信息仍被认为可信？为什么另一条合法信息没有进入自动推荐池？

---

## 15. 建议代码结构

```text
clocktower/
  epistemic/
    PlayerKnowledgeSnapshot.kt
    PlayerEpistemicTimeline.kt
    InformationProposition.kt
    EpistemicWorld.kt
    EpistemicWorldSolver.kt
    EpistemicConstraintCompiler.kt
    EpistemicConsistencyEvaluator.kt
    MalfunctionExposureEvaluator.kt

  recommendation/
    CompleteInformationCandidateGenerator.kt
    UnifiedCandidateEvaluator.kt
    NarrativeConsequenceEvaluator.kt
    UnifiedCandidateSelector.kt

  rules/
    SetupConstraint.kt
    SetupModifier.kt
    AbilitySemanticRule.kt
    InformationPropositionEvaluator.kt

  history/
    EpistemicObservationRepository.kt
    PublicFactProjection.kt
```

既有模块调整方向：

| 现有模块 | 调整 |
|---|---|
| `SetupCandidateGenerator` | 生成酒鬼显示身份与首夜信息完整组合 |
| `SetupEvaluator` | 接入认知一致性和完整候选质量 |
| `DynamicCandidateGenerator` | 不再把所有动态选项统一标记为 `RECOMMENDED` |
| `RegistrationPolicy` | 只负责登记语义，不承担玩家认知求解 |
| `CandidatePoolBuilder` | 增加自动资格过滤，继续负责等级和容差 |
| `WeightedStableSelector` | 只接收已通过资格和等级过滤的候选 |
| `MainActivity` | 只做输入适配和展示，不在 UI 层构造认知规则 |

---

## 16. 分阶段实施方案

### PR 1：语义模型与认知快照

- 新增 `InformationProposition`；
- 新增玩家认知快照和时间线模型；
- 将图书管理员、调查员、洗衣妇、厨师、共情者的展示信息转换为结构化命题；
- 保持现有选择结果不变；
- 增加序列化、稳定 ID 和规则版本测试。

### PR 2：基础设置约束求解器

- 编码人数角色数量；
- 编码角色唯一性；
- 编码男爵等设置修正；
- 支持“是否存在合理世界”的精确 `SAT/UNSAT`；
- 支持有限见证枚举和矛盾核心；
- 不接入生产选择器。

### PR 3：首夜认知一致性评价

- 接入图书管理员、调查员、洗衣妇；
- 证明零结果和二选一角色信息；
- 增加 `AutomaticEligibility`；
- 先以影子模式记录评价，不改变推荐结果；
- 对历史固定种子进行差异报告。

### PR 4：酒鬼显示身份与首夜信息联合候选

- 泛化现有调查员联合结构；
- 图书管理员、洗衣妇等不再先选身份后独立选信息；
- 若某显示身份没有合格信息，则不进入自动设置候选；
- 静态 `drunkSuitability` 降为弱先验。

### PR 5：统一自动选择器

- 删除跨风格二次随机；
- 当前风格直接评价所有完整候选；
- 按自动资格、质量等级、容差、稳定权重依次选择；
- 移除 UI 层重复选择逻辑。

### PR 6：多夜认知时间线

- 接入共情者、占卜师、送葬者、守鸦人等后续信息；
- 保存每条信息发生时的状态快照；
- 支持合法状态转换；
- 对前后无法共同解释的信息自动降级。

### PR 7：全局确认链和恶魔伪装

- 识别酒鬼线索与恶魔伪装重合；
- 识别真实恶魔进入假信息候选对；
- 评价多线索确认链、锁死风险和替代解释数量；
- 与认知一致性保持独立评分和解释。

### PR 8：灰度、校准与旧逻辑清理

- 固定种子差异回放；
- 专家标注 `HIGH`/`NEAR_CERTAIN` 暴露案例；
- 校准世界覆盖和暴露阈值；
- 删除被统一语义替代的角色/人数硬门槛；
- 删除旧的不可靠信息二次选择路径。

---

## 17. 测试方案

### 17.1 求解器单元测试

```text
8 人基础配置存在且外来者数量为 1
8 人男爵配置存在且外来者数量按规则增加
角色唯一性冲突返回 UNSAT
正常能力身份与公开事实冲突返回 UNSAT
枚举上限不会被误判为 UNSAT
```

### 17.2 首夜认知一致性测试

```text
8 人酒鬼图书管理员 + 没有外来者
→ 无正常能力世界
→ 自动资格 INELIGIBLE

允许零外来者的公开配置 + 没有外来者
→ 存在正常能力世界

8 人酒鬼调查员 + 没有爪牙
→ 无正常能力世界
→ 自动资格 INELIGIBLE

8 人酒鬼调查员 + 男爵在 A/B 中
→ 存在带男爵设置修正的合理世界
→ 不因真实场上无男爵而自动拒绝
```

### 17.3 全局叙事测试

```text
酒鬼图书管理员展示管家
+ 候选对包含真实恶魔
+ 管家属于恶魔伪装
→ 认知一致性通过
→ 产生 demon-bluff-confirmation 风险

多个独立线索共同确认真实恶魔伪装
→ 确认链风险升级

假信息锁死单一无辜玩家且不存在替代解释
→ 自动资格 MANUAL_ONLY 或 INELIGIBLE
```

### 17.4 多夜测试

```text
共情者邻居不变时，历史数字和新数字存在共同正常世界
共情者邻居死亡后，允许数字随相邻关系变化
占卜师目标成为新恶魔后，允许结果变化
隐士在不同交互中采用不同合法登记时仍可解释
多夜信息合并后正常能力世界为零
→ 当前候选不得自动推荐
```

### 17.5 选择器测试

```text
INELIGIBLE 候选在 100,000 个种子中命中次数为 0
MANUAL_ONLY 候选在自动模式中命中次数为 0
平衡模式不会抽取只由稳健或激进评价产生的代表
只从最高可用质量等级和分数容差内选择
同一完整输入和历史得到完全相同结果
不同种子在多个同质量候选间保持分布多样性
```

### 17.6 回归测试

- 健康图书管理员面对唯一自然外来者仍生成自然真实候选；
- 健康调查员面对唯一自然爪牙仍生成自然真实候选；
- 间谍和隐士登记不凭空创造未绑定的角色事实；
- 酒鬼和中毒可以收到错误但可信的信息；
- 熟练说书人仍能通过手动裁定选择规则允许的非自动候选；
- 旧存档缺少认知时间线时可以安全降级，不产生伪造历史。

---

## 18. 验收指标

| 指标 | 目标 |
|---|---:|
| 正常能力假设下 `UNSAT` 的候选被自动选中 | 0 |
| `NEAR_CERTAIN` 能力失效暴露候选被自动选中 | 0 |
| `MANUAL_ONLY` 候选被自动选中 | 0 |
| 同一决策重放一致性 | 100% |
| 平衡模式跨风格选择 | 0 |
| 认知求解 `UNSAT` 误判 | 0 |
| 首夜信息评价交互延迟 P95 | 待基线测量后确定，目标不影响正常夜间操作 |
| 多夜增量评价交互延迟 P95 | 待基线测量后确定 |
| 推荐结果具有可解释代码 | 100% |
| 固定种子回放可重建认知快照和候选选择 | 100% |

产品验收还应包含熟练说书人盲测：

- 信息是否看起来像健康角色可能收到的结果；
- 是否仍保留多个可讨论世界；
- 是否出现明显“系统在告诉玩家自己醉酒”的结果；
- 是否过度确认恶魔伪装；
- 多夜信息是否自然、可维护。

---

## 19. 迁移与兼容策略

### 19.1 影子模式优先

认知评价器首次接入时，只记录：

- 当前旧算法选择；
- 新算法自动资格；
- 合理世界摘要；
- 是否产生结果差异。

在固定种子回放和专家评审完成前，不立即改变所有生产选择。

### 19.2 旧存档

旧存档可能没有结构化信息命题和当时状态快照：

- 已有结构化事件正常进入认知时间线；
- 只能解析出文本的旧事件标记为 `LEGACY_UNCERTAIN`；
- 不用不完整旧数据证明 `UNSAT`；
- 新产生的信息从新模型开始完整记录。

### 19.3 清理硬门槛

只有在对应语义约束、求解测试和端到端测试完成后，才能删除旧硬门槛。

删除依据不是“案例目前通过”，而是：

```text
所有同类候选都由统一命题语义表达；
求解器能精确证明是否存在正常能力世界；
自动资格过滤发生在随机选择之前；
大量种子验证自动命中次数为 0。
```

---

## 20. 风险与待校准项目

### 20.1 世界数量偏差

不同世界建模粒度可能影响“剩余世界比例”。因此：

- `SAT/UNSAT` 使用精确约束；
- 数量指标只作为排序和暴露风险特征；
- 不直接把原始世界数量解释为真实概率。

### 20.2 玩家声明不可靠

若未来记录角色声明，应使用软证据。不能因为所有人声称镇民，就证明男爵世界不存在。

### 20.3 规则扩展复杂度

后续剧本包含角色变化、角色创建、阵营变化和复杂设置修正。规则必须通过版本化语义接口接入，不能在求解器内部增加角色名称分支。

### 20.4 过度一致会让酒鬼信息太真

认知一致性不要求信息符合真实世界，只要求存在一个玩家可相信的正常能力世界。

因此算法仍然可以：

- 给出完全错误的角色；
- 指向两名错误玩家；
- 在不同合理世界之间制造冲突；
- 支持邪恶阵营伪装。

不能把“可信”错误实现成“总是接近真实”。

### 20.5 阈值不能隐藏在总分中

`HIGH` 和 `NEAR_CERTAIN` 的暴露判断必须输出独立等级。严重风险不得被讨论价值、历史多样性或恶魔支持等加分抵消。

---

## 21. 最终设计原则

1. **规则合法不等于适合自动推荐。**
2. **从接收者视角评价可信度，从真实局面评价全局后果。**
3. **酒鬼显示身份和具体信息是一个不可拆分的完整候选。**
4. **首夜与后续夜晚使用同一认知一致性框架。**
5. **合理世界压缩到零的候选自动失去资格。**
6. **明显暴露能力失效的候选只允许人工裁定或完全不推荐。**
7. **恶魔伪装支持属于叙事风险，不应与认知矛盾混淆。**
8. **随机性只在同质量合格候选之间工作。**
9. **静态角色适配度只是弱先验，不能替代具体局面求解。**
10. **所有淘汰、降级和选择结果必须可解释、可复现、可回放。**

本方案完成后，“8 人酒鬼图书管理员得到没有外来者”不再依赖专门条件被禁止；它会因为在玩家正常能力假设下不存在任何合理世界，而被统一认知一致性评价自动淘汰。调查员零爪牙、多夜信息矛盾以及其他尚未发现的同类问题，也由同一机制处理。

---

## 22. 外部参考项目与架构修订（2026-08-11）

本节记录实施前新增的三项高价值参考，并据此修订本方案的底层实现方向。结论不是用外部求解器替代自动说书人的推荐算法，而是尽量复用成熟的“规则形式化 / Possible Worlds / 高效集合运算”思想，把本项目的开发重点集中在真正具有产品差异化的 Storyteller Recommendation Layer。

### 22.1 `pnkfelix/botc-asp`：Formal Rule Oracle

项目定位：使用 Answer Set Programming（Clingo）形式化 Blood on the Clocktower 游戏规则和角色能力，能够寻找与观察信息一致的所有合法模型，并验证场景是 SAT 还是 UNSAT。

对本项目最有价值的部分：

- 角色和能力采用声明式约束，而不是大量命令式 `if/else`；
- 说书人的“可选信息”本身被建模为规则允许的选择空间；
- 正常角色与醉酒/中毒角色的信息约束可以在同一语义下表达；
- Spy / Recluse 的登记、Fortune Teller 红鲱鱼、Poisoner、Imp 传位和多夜状态都可以进入统一规则世界；
- 可用 SAT / UNSAT 测试作为独立规则真值机。

本项目对它的定位：

> **ASP 主要负责回答“这个世界 / 这条信息在规则上是否可能”，而不是直接回答“说书人应该选哪一个”。**

因此优先把 `botc-asp` 作为开发期的 **Formal Rule Oracle / Cross-validation Oracle**，而不是直接成为 Android 生产运行时的核心依赖。

实施原则：

1. 对《暗流涌动》规则建立本项目语义与 ASP predicate 的映射表；
2. 对关键角色和交互建立双实现测试；
3. 本项目 World Engine 输出的 SAT / UNSAT、候选集合或世界数量应在可比范围内与 ASP 交叉验证；
4. 外部模型发现的边界案例优先加入本项目回归测试；
5. 在确认外部项目许可证和可复用范围前，不直接复制其源代码进入产品仓库。

### 22.2 `olarozenfeld/botc`：Possible Worlds 与 Perspective 模型

项目定位：输入从某一视角记录的游戏日志，枚举所有 mechanically possible worlds，并允许通过额外假设继续过滤世界，例如某玩家为邪恶、某角色不在场、某玩家某夜未中毒等。

对本项目最有价值的部分不是具体 SAT 实现，而是领域建模：

- 明确区分 `STORYTELLER`、`PLAYER`、`OBSERVER` 视角；
- Game Log 不只记录真实角色，还能记录角色行动、角色信息、声明和事件；
- Claim 可以区分 hard claim / soft claim；
- Audience 能表达公开信息、私聊信息和只有部分玩家知道的信息；
- Possible Worlds 可以在不断加入新观察后持续过滤；
- 项目自身已经预留未来从“机械可能”扩展到 world likelihood / player strategy 的方向。

这与本方案的核心目标直接一致：

> **不能从说书人的全知视角判断一条信息是否“明显”，必须从收到信息的玩家视角判断该玩家还剩哪些合理世界。**

因此本项目的数据模型应吸收 `Perspective + GameLog + Observation + Audience + Assumption + World` 的思想，但继续保持适合 Android/Kotlin 和现有游戏状态架构的实现。

### 22.3 `pnkfelix/botc-zdd-`：移动端 Runtime World Engine 的重点候选

该项目是研究 `botc-asp` 后发现的进一步参考。它使用 Zero-suppressed Decision Diagram（ZDD）表示仍然可能的角色分布和说书人选择，目标本身就是移动端友好地跟踪“哪些游戏世界仍与玩家得到的信息一致”。

它对本项目的意义尤其直接：

- ZDD 天然适合表示大量稀疏的角色组合 / 世界集合；
- `require / exclude / union / intersection / difference` 等集合运算直接对应“加入一个新观察后过滤 Possible Worlds”；
- 不需要每次把所有可能性展开成完整对象列表；
- 可以保存 world count、possible values 和增量过滤结果；
- 项目采用分阶段状态：Distribution → Seat Assignment → Night Info → Day / Night Action，而不是一次构造整局的巨大笛卡尔积；
- 已经和 `botc-asp` 使用独立实现做 cross-validation，并通过这种方式发现双方真实 bug。

外部项目自己的 benchmark 显示，在其覆盖的 Night 1 场景中 ZDD 相比 Clingo 有数量级的性能优势。该结果不能直接作为本项目性能承诺，但足以支持我们把 ZDD / Decision Diagram 方案列为移动端运行时的优先技术路线，并在真实 Android 设备上重新基准测试。

### 22.4 三个项目在本方案中的明确职责

| 参考项目 | 本项目借鉴职责 | 不直接承担的职责 |
|---|---|---|
| `pnkfelix/botc-asp` | 规则形式化、合法性 Oracle、Cross-validation | 自动说书人的最终推荐排序 |
| `olarozenfeld/botc` | Perspective、GameLog、Possible Worlds、Assumption 领域模型 | 移动端生产实现的直接依赖 |
| `pnkfelix/botc-zdd-` | 高性能 World Set 表示、增量过滤、Runtime 架构 | 叙事平衡和说书人风格判断 |

本项目自己的核心价值继续是：

```text
Formal Rules / Legal Worlds
            ↓
Player-perspective Possible Worlds
            ↓
Candidate Simulation
            ↓
Storyteller Recommendation
            ↓
Balance / Ambiguity / Bluff Support / Narrative / Style
```

即：**外部项目解决“什么可能”，CampBoardGameHost 重点解决“在所有可能中，自动说书人现在最好选择什么”。**

---

## 23. 修订后的目标架构

第 5 节描述的评价层仍然成立，但底层实现进一步拆成三个明确层次。

```text
                   Official / Formal BotC Rules
                              │
                              ↓
                 ┌────────────────────────┐
                 │   Legal Choice Layer   │
                 │  什么在规则上允许？     │
                 └───────────┬────────────┘
                             ↓
                 ┌────────────────────────┐
                 │      World Engine      │
                 │ Possible World / ZDD   │
                 └───────────┬────────────┘
                             ↓
                    PlayerWorldSet(P, t)
                             │
              ┌──────────────┼──────────────┐
              ↓              ↓              ↓
          Candidate A    Candidate B    Candidate C
              │              │              │
              ↓              ↓              ↓
          WorldSet A     WorldSet B     WorldSet C
              └──────────────┼──────────────┘
                             ↓
                 ┌────────────────────────┐
                 │ Recommendation Engine  │
                 │  认知 / 平衡 / 叙事评分 │
                 └───────────┬────────────┘
                             ↓
                         Top N / Auto
```

### 23.1 Legal Choice Layer

职责：只判断规则语义和生成规则允许的完整选择，不夹带“这个选择好不好”的策略判断。

建议接口：

```kotlin
interface LegalChoiceProvider {
    fun legalChoices(
        state: FormalGameState,
        decision: StorytellerDecisionPoint,
    ): LegalChoiceSet
}
```

在开发和测试环境中，Legal Choice Layer 可以与 ASP Oracle 对照；生产环境可以使用经过交叉验证的 Kotlin / ZDD 实现。

### 23.2 `PlayerWorldSet` 升级为一等领域对象

这是本次外部项目研究后最重要的架构变化。

此前“玩家认知一致性”更接近 Candidate 的一个评分结果。修订后，每个相关玩家在每个时间点都应拥有一个可查询的认知世界集合：

```kotlin
interface PlayerWorldSet {
    val recipientSeat: Int
    val snapshotId: String

    fun isEmpty(): Boolean
    fun worldCount(): WorldCount
    fun require(observation: EpistemicObservation): PlayerWorldSet
    fun exclude(observation: EpistemicObservation): PlayerWorldSet
    fun possibleRoles(seat: Int): Set<RoleId>
    fun possibleDemonSeats(): Set<Int>
    fun possibleMinionSeats(): Set<Int>
    fun possibleValues(query: EpistemicQuery): PossibleValueSummary
}
```

具体实现可以是：

```text
PlayerWorldSet
├── EnumeratedWorldSet       // 第一版、调试与小规模基线
├── ZddPlayerWorldSet        // 重点性能候选
└── SolverBackedWorldSet     // 测试 / Oracle 适配
```

推荐层依赖接口，不绑定 ZDD、SAT 或 ASP。

### 23.3 Candidate 不再只“评分”，而是先模拟世界变化

统一流程改为：

```text
beforeWorlds = PlayerWorldSet(recipient, currentTime)

for candidate in legalCandidates:
    afterWorlds = simulate(beforeWorlds, candidate)

    if afterWorlds.empty:
        INELIGIBLE
    else:
        epistemicMetrics = compare(beforeWorlds, afterWorlds)
        narrativeMetrics = evaluateAgainstActualWorld(candidate, afterWorlds)
        score = recommendationPolicy(...)
```

这使“为什么一条信息太强 / 太弱 / 自证醉酒”有可审计的世界变化依据，而不是只有人工设定的加减分。

### 23.4 评分指标重新建立在 World Set 上

现有评分项保留，但逐步替换为有世界集合支撑的特征。

#### Information Value

概念上可由：

```text
uncertainty(before) - uncertainty(after)
```

衡量候选减少了多少合理解释。

第一版不要求严格使用 Shannon entropy；可以先使用：

- 世界集合缩减比例；
- Demon candidate 数变化；
- Minion candidate 数变化；
- setup profile 数变化；
- target explanation 数变化。

#### Hard Solve Risk

不再只依靠“指向真实邪恶次数”，还应观察候选之后是否出现：

```text
几乎所有剩余世界都把同一玩家解释为 Demon / Evil
```

或者：

```text
所有替代解释都依赖极端、脆弱的单一状态
```

#### Ambiguity / Discussion Value

可结合：

- 剩余 Demon 候选数量；
- 各候选是否仍有多个 setup profile；
- 信息是否同时支持多个可讨论方向；
- 是否避免单一解释锁死。

#### Malfunction Exposure

比较：

```text
FunctioningAsPerceivedRole 的可解释世界
vs
AbilityMayMalfunction 的可解释世界
```

若正常能力世界消失或只剩极少脆弱解释，而失能世界大量存在，则能力失效暴露风险上升。

### 23.5 World Count 不等于真实概率

即使 ZDD / 枚举器可以精确统计 mechanically possible worlds，也不能直接解释为：

```text
某玩家有 72% 概率是恶魔
```

原因包括：

- 世界建模粒度会影响数量；
- 玩家真实策略、声明可信度和说书人选择并非均匀分布；
- 不同合法登记或说书人选择可能产生不等权的机械世界。

因此第一阶段只把 world count 当作：

- 一致性证明；
- 信息压缩程度；
- 多样性特征；
- 候选之间的相对启发式指标。

未来如要加入 likelihood，必须单独设计概率 / 行为模型，不能直接把枚举数量归一化。

---

## 24. 修订后的实施顺序

> **本节从 2026-08-11 起覆盖第 16 节原有的实施顺序。**  
> 第 16 节保留作为原始设计演进记录，但 Codex 后续实施应以本节为准。

核心变化是：**实施认知评分前，先确定并验证 Formal Oracle、World Engine 与 PlayerWorldSet 的边界。**

### PR 0：外部模型验证与技术 Spike

目标：在改生产算法之前确认参考项目真正能覆盖哪些问题。

- 固定三个参考仓库的具体 commit / tag，避免研究基线漂移；
- 整理 Trouble Brewing 角色能力和本项目已有规则到外部模型的映射表；
- 建立至少 20 个代表性场景：WW / Librarian / Investigator / Chef / Empath / FT / Drunk / Poisoner / Spy / Recluse / Baron；
- 用 `botc-asp` 验证 SAT / UNSAT 和合法信息集合；
- 用 `olarozenfeld/botc` 对可覆盖场景观察 Possible Worlds 行为；
- 跑 `botc-zdd-` 的现有测试和 benchmark，并在本项目目标 Android 设备上设计等价基准；
- 明确许可证、代码复用和第三方 NOTICE 要求；
- 输出 `external_solver_evaluation.md`，再决定 Runtime 是否采用 ZDD、枚举器或混合实现。

**退出条件：**不允许只因为外部 benchmark 很好就直接引入依赖；必须先验证规则覆盖、正确性、包体积、内存、冷启动和 Android 集成成本。

### PR 1：统一语义模型 + Oracle Adapter

- 新增 / 完善 `InformationProposition`、`EpistemicObservation`、`FormalGameState`；
- 定义 `LegalChoiceProvider` 和 `WorldEngine` 接口；
- 将现有 WW / Librarian / Investigator / Chef / Empath 信息转换为统一命题；
- 建立测试侧 ASP Oracle Adapter；
- 生产结果保持完全不变；
- 所有规则边界测试同时比较当前实现与 Oracle。

### PR 2：可枚举 World Engine 基线

先实现一个简单、透明、易调试的基线，而不是直接优化：

- 支持 Trouble Brewing setup distribution；
- 支持 seat assignment 和基础 registration；
- 支持 `SAT / UNSAT`；
- 支持有限 `enumerate`；
- 支持 Player Perspective 过滤；
- 对同一场景与 ASP Oracle cross-validation；
- 作为后续 ZDD 实现的正确性基线。

该版本不要求成为最终移动端生产实现。

### PR 3：ZDD / Decision Diagram Runtime Prototype

- 实现或适配 `ZddPlayerWorldSet`；
- 支持 `require / exclude / count / possibleValues`；
- 对 Distribution、Seat Assignment、Night 1 信息做分阶段表示；
- 与 PR 2 枚举实现和 ASP Oracle 三方交叉验证；
- 测量真实设备 P50 / P95、峰值内存、节点数和缓存命中；
- 只有明显优于基线且维护成本可接受时，才确定为生产 Runtime。

### PR 4：`PlayerWorldSet` 与 Candidate Simulation

- 将 `PlayerWorldSet` 作为正式领域对象；
- 建立 `beforeWorlds → candidate → afterWorlds` 模拟；
- 产出 `worldSurvival`、possible Demon / Minion、setup profile 等指标；
- `afterWorlds == empty` 时产生 `epistemic.no-functioning-world`；
- 先影子记录，不改变自动选择。

### PR 5：首夜认知一致性接入

- 接入 WW / Librarian / Investigator / Chef / Empath；
- 对健康角色、Drunk perceived role、Poisoned role 使用相同 World Engine；
- 验证“8 人酒鬼图书管理员 + 没有外来者”等典型案例由统一世界约束自然淘汰；
- 不增加针对人数 + 角色 + 输出值的特殊禁用分支；
- 与 Oracle 做固定场景 cross-validation。

### PR 6：酒鬼显示身份 + 首个信息联合规划

- `DrunkShownRole` 升级为 `DrunkPerceivedRolePlan`；
- 每个 perceived role 必须先证明至少存在一个可自动使用的首夜信息路径；
- `drunkSuitability` 降为同质量候选的弱先验；
- 候选选择以完整计划而不是孤立身份为单位。

### PR 7：多夜 PlayerWorldSet

- 加入 Day / Night 状态转换；
- 支持 death、neighbor change、Poisoner retarget、Monk protection、Imp kill / starpass；
- 接入 Empath N2、FT N2、Undertaker、Ravenkeeper 等；
- 每个 observation 增量过滤当前 PlayerWorldSet；
- 保存 snapshot / undo / replay；
- 与可覆盖的 ASP / ZDD 外部模型持续 cross-validation。

### PR 8：叙事与推荐评分建立在 World Metrics 上

保留现有人工经验评分，但逐步替换底层特征：

- `InformationScore` ← world reduction / candidate reduction；
- `AmbiguityScore` ← remaining alternative explanations；
- `HardSolvePenalty` ← evil / demon concentration；
- `MalfunctionExposure` ← functioning worlds vs malfunction worlds；
- `ConfirmationChainPenalty` ← 多个 observation 对同一 actual / bluff story 的联合压缩；
- `EvilBluffSupportScore` 继续从真实世界视角计算，不能混入玩家认知事实。

每个最终分数必须能追溯到 World Metrics + 策略权重。

### PR 9：统一选择器、灰度与旧逻辑清理

- 删除跨风格二次随机；
- 自动池严格执行 `LEGAL → ELIGIBLE → QualityTier → StyleScore → tolerance → stable random`；
- 固定种子 replay 对比旧算法 / 新算法；
- 专家标注高风险场景；
- 清理已被 Formal Rules / World Engine 替代的硬编码；
- 保留手动说书人 override；
- 输出最终性能、正确性和推荐质量验收报告。

---

## 25. 新增 Cross-validation 与性能验收要求

### 25.1 三层正确性验证

对于外部模型覆盖的核心 Trouble Brewing 场景，建立：

```text
Formal ASP Oracle
       │
       ├──── compare ──── Enumerated Baseline
       │
       └──── compare ──── ZDD / Runtime World Engine
```

重点比较：

- SAT / UNSAT；
- 合法角色分布数量；
- 给定 observation 后的剩余世界；
- 信息角色可产生的合法输出；
- Spy / Recluse registration；
- Drunk / Poisoned malfunction；
- FT red herring；
- 多夜状态变化。

发现差异时，不能默认“我们的实现错”或“外部实现错”，必须缩小到最小场景，确认官方规则语义后加入永久回归测试。

### 25.2 Runtime 性能

性能目标以真实 Android 设备测量为准，而不是直接采用外部项目 benchmark。

至少记录：

- 5 / 7 / 8 / 10 / 12 人 Trouble Brewing；
- 首次构建 WorldSet 时间；
- 加入单个 observation 的增量过滤时间；
- Top N candidate simulation 总时间；
- P50 / P95；
- 峰值内存；
- ZDD node / internal state size；
- snapshot / undo 成本。

交互目标：推荐求解不得让正常夜间操作产生可感知卡顿。具体毫秒阈值在 PR 0 / PR 3 真实设备基线后冻结。

### 25.3 新的架构验收条件

正式启用新推荐算法前必须满足：

1. 核心 Trouble Brewing 规则场景与独立 Oracle 无已知未解释差异；
2. `PlayerWorldSet` 可重放且同输入结果确定；
3. `afterWorlds == empty` 的候选自动命中次数为 0；
4. 推荐层不直接依赖 ASP / ZDD 的具体数据结构；
5. World Engine 可替换并有基线实现；
6. world count 不被 UI 或评分代码错误展示为真实概率；
7. 外部许可证和第三方依赖要求已明确；
8. 固定场景能输出“为何合法、为何可信、为何被推荐/淘汰”的完整解释链。

---

## 26. 2026-08-11 修订后的最终原则

在第 21 节原则基础上，新增以下架构原则：

11. **不要重复发明已经有成熟形式化工作的规则真值层。** 优先用独立 Formal Oracle 验证自己的实现。
12. **ASP 适合做规则 Oracle，不默认等于移动端 Runtime。**
13. **Possible Worlds 必须是实际领域状态，而不只是一个评分过程中的临时统计。**
14. **`PlayerWorldSet` 是玩家认知一致性算法的一等对象。**
15. **候选评价先模拟世界变化，再进行叙事和风格打分。**
16. **Runtime World Engine 与 Recommendation Engine 必须解耦。** 未来可以从枚举器换成 ZDD、SAT 或其他表示而不重写推荐策略。
17. **机械世界数量不是玩家真实概率。** 概率模型属于未来独立层。
18. **外部项目最重要的价值是规则模型、数据结构和验证方法，不是复制最终产品。**
19. **本项目的差异化重点是 Storyteller Decision / Recommendation Layer。**
20. **在实施认知评分之前先完成外部模型验证和 World Engine Spike。**

修订后的总体方向可以概括为：

```text
botc-asp 思想 / Oracle
        ↓
Formal Legality
        ↓
Possible Worlds / PlayerWorldSet
        ↓
ZDD 或其他高效 Runtime
        ↓
CampBoardGameHost 独有的 Recommendation Layer
        ↓
可信但不自证、信息量适当、保留讨论空间、支持合理邪恶叙事
```

这使本方案从“为推荐算法增加认知一致性评分”进一步升级为：

> **建立一个经过形式化规则交叉验证的玩家视角 Possible-Worlds 基础层，再让自动说书人的策略评分在这个基础上做选择。**
