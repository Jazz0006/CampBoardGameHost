# CampBoardGameHost 自动说书人线索与动态裁定算法改进设计

> 适用于 CampBoardGameHost · Blood on the Clocktower 自动说书人
> 版本：4.0（v3 评审意见修订版）
> 目标剧本：优先完成《暗流涌动》（Trouble Brewing）
> 文档用途：可直接交给 Codex 作为分阶段实施依据
> 修订重点：候选家族分类表、哈希算法规范、规则权威层级与失败类型绑定、PR 文件迁移映射、exp() 与定点整数矛盾修复、PR 2 拆分、RulesetRef 内容哈希规范

---

## 目录

1. [执行摘要](#1-执行摘要)
2. [设计目标与边界](#2-设计目标与边界)
3. [当前问题诊断](#3-当前问题诊断)
4. [最终选定的总体架构](#4-最终选定的总体架构)
5. [统一决策数据模型](#5-统一决策数据模型)
6. [统一决策流水线](#6-统一决策流水线)
7. [候选选择算法](#7-候选选择算法)
8. [初始线索生成改进](#8-初始线索生成改进)
9. [自然真实结果优先规则](#9-自然真实结果优先规则)
10. [动态线索与局内叙事](#10-动态线索与局内叙事)
11. [醉酒与中毒的能力失效模型](#11-醉酒与中毒的能力失效模型)
12. [隐士与间谍的特殊登记模型](#12-隐士与间谍的特殊登记模型)
13. [玩家信息压力与决策账本](#13-玩家信息压力与决策账本)
14. [后果评估与风险控制](#14-后果评估与风险控制)
15. [稳定随机与可复现性](#15-稳定随机与可复现性)
16. [跨局历史与模板冷却](#16-跨局历史与模板冷却)
17. [决策日志、解释与纠错](#17-决策日志解释与纠错)
18. [建议代码结构](#18-建议代码结构)
19. [实施阶段与 PR 拆分](#19-实施阶段与-pr-拆分)
20. [测试、模拟与验收指标](#20-测试模拟与验收指标)
21. [参数校准策略](#21-参数校准策略)
22. [暂不实施的内容](#22-暂不实施的内容)
23. [最终设计原则](#23-最终设计原则)

---

## 1. 执行摘要

当前自动说书人算法已经具备候选生成、评分、风格区分和部分历史控制能力，但仍存在三个根本问题：

1. **确定性最高分长期垄断**
   酒鬼容易固定显示为调查员；相似动态局面容易固定输出同一个数字、身份或目标。

2. **能力失效与特殊登记混用**
   醉酒/中毒产生的失效信息，与隐士/间谍的合法特殊登记，本质不同，却容易被简化为同一个"误导概率"。

3. **概率与结果生成分离**
   若先决定"是否误导"，再独立生成具体线索或特殊登记，容易出现概率失真、语义不一致和无法复盘的问题。

本设计最终选择以下方案：

> **生成完整候选结果 → 合法性过滤 → 场景评分 → 后果评估 → 建立优质候选池 → 使用稳定加权随机一次性选择。**

"概率"不再是独立掷骰步骤，而是完整候选结果的最终选择权重。

这样可以同时保证：

- 规则正确；
- 自然真实结果不会被无意义随机覆盖；
- 特殊登记与显示结果始终绑定；
- 同一局具有叙事连续性；
- 不同游戏不会形成固定程序习惯；
- 同一决策可复现；
- 所有结果可解释、可测试、可回放。

---

## 2. 设计目标与边界

### 2.1 产品目标

自动说书人应能够：

- 自动处理夜间顺序；
- 自动生成初始线索；
- 自动生成醉酒、中毒或特殊登记下的信息；
- 自动完成规则允许的动态裁定；
- 根据当前局势避免明显失衡；
- 记录每次重要决定；
- 支持存档恢复、赛后复盘和人工纠错。

### 2.2 三类结果必须分层

#### A. 确定性规则结果

由规则直接决定，不进入随机选择：

- 玩家能力是否生效；
- 目标是否合法；
- 玩家是否死亡；
- 保护是否成功；
- 圣女、杀手、恶魔传位等强制结果；
- 胜负条件；
- 唯一明确且规则要求的自然真实信息。

#### B. 说书人裁量结果

规则允许多个合法选项：

- 酒鬼被告知哪个角色；
- 醉酒或中毒时给真还是给假；
- 错误数字或错误角色是什么；
- 隐士、间谍是否特殊登记；
- 红鲱鱼；
- 恶魔伪装身份；
- 镇长死亡反弹目标；
- 多个合法自然真实结果中选哪一个。

#### C. AI 或高级顾问结果

未来 AI 可用于：

- 解释推荐理由；
- 对合法候选进行辅助排序；
- 生成复盘；
- 生成自然语言提示；
- 提醒潜在风险。

AI 不得绕过确定性规则引擎直接创建不合法状态。

### 2.3 官方规则、产品策略与风格偏好必须分层

每一条约束必须标明来源级别：

```kotlin
enum class ConstraintAuthority {
    OFFICIAL_RULE_REQUIRED,
    OFFICIAL_RULE_ALLOWED,
    PRODUCT_POLICY_REQUIRED,
    STYLE_PREFERENCE,
    HOUSE_RULE,
}
```

- 官方规则要求或禁止的内容进入合法性验证器，对应的 `LegalityFailure` 必须携带 `constraintAuthority = OFFICIAL_RULE_REQUIRED`；
- 官方允许、但本产品不希望默认采用的裁量，保留为合法候选，再由产品策略禁用、降级或降权，对应 `LegalityFailure` 携带 `constraintAuthority = PRODUCT_POLICY_REQUIRED`；
- 温和、平衡、激进只影响风格偏好，不能改变官方合法性；
- 每条规则事实必须保存来源、规则版本和最后核验时间；
- 未核验角色或相互作用不得显示为"规则已确认"，自动模式应降级为人工确认。

`LegalityFailure` 需携带权威级别字段，以便测试和日志能区分"官方规则禁止"与"产品策略拒绝"：

```kotlin
sealed interface LegalityFailure {
    val code: String
    val constraintAuthority: ConstraintAuthority
    // ... 具体子类同现有实现，补充 constraintAuthority 字段
}
```

例如，"健康图书管理员面对唯一实际隐士时默认显示隐士"是《暗流涌动》的产品策略，不应被误写成对所有剧本都成立的官方强制规则，其对应失败代码的 `constraintAuthority` 应为 `PRODUCT_POLICY_REQUIRED`。

---

## 3. 当前问题诊断

### 3.1 酒鬼显示角色存在结构性固定偏好

当前逻辑（`PlanEvaluator.kt` 第 119–126 行）对非调查员的酒鬼显示角色整体降级，导致调查员在大量配置中成为事实上的唯一最高等级候选。

结果是：

- 酒鬼频繁显示调查员；
- 调查员又频繁显示同一爪牙；
- 玩家熟悉系统后可以反向推断算法习惯。

### 3.2 随机只用于完全同分

当前排序（`RecommendationSearch.kt` 第 74–76 行）通常为：

1. 质量等级；
2. 总分；
3. 稳定哈希、候选 ID 或较小数字。

只要一个模板比其他模板高一分，随机种子便无法改变结果。

### 3.3 动态信息也会形成固定转换规律

可能出现：

- 中毒共情者真实值为 0 时总显示 1；
- 相同局面总选择相同玩家作为诱饵；
- 相同特殊登记场景总做同一裁定；
- 激进模式反复给同一名玩家增加怀疑。

### 3.4 `isTruthful` 表达能力不足

单一布尔值无法区分：

- 正常能力得到真信息；
- 特殊登记后信息成立；
- 能力失效但碰巧给真；
- 能力失效后给假。

这些结果在规则、解释、测试和历史控制中意义完全不同。

### 3.5 特殊登记可能与最终信息脱节

错误流程：

1. 生成"2号或6号中有一人是男爵"；
2. 再独立决定6号隐士是否登记为男爵。

正确流程：

- "隐士登记为男爵 + 显示男爵 + 两名候选"必须是同一个不可拆分候选。

### 3.6 缺少完整决策历史

仅保存最终显示内容不足以回答：

- 当时有哪些合法候选？
- 为什么没有选择另一个？
- 是否使用了特殊登记？
- 是否因中毒给假？
- 使用了什么随机种子？
- 对哪些玩家增加了信息压力？
- 恢复存档后应否重新抽取？

---

## 4. 最终选定的总体架构

自动说书人决策拆分为五层。

```text
┌───────────────────────────────┐
│ 1. Deterministic Rule Engine  │
│    确定性规则与合法状态        │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│ 2. Candidate Generator        │
│    生成完整合法候选结果        │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│ 3. Candidate Evaluator        │
│    质量、平衡、叙事与压力评分  │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│ 4. Consequence Guard          │
│    硬否决与高风险惩罚          │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│ 5. Stable Weighted Selector   │
│    优质候选池内稳定加权选择    │
└───────────────────────────────┘
```

### 4.1 为什么使用完整候选一次选择

不推荐：

```text
先以 70% 概率决定误导
→ 再选择错误结果
→ 再决定是否特殊登记
```

推荐：

```text
候选 A：自然真
候选 B：失效真
候选 C：失效假 1
候选 D：失效假 2
候选 E：特殊登记结果
→ 一次性评分并选择
```

优点：

- 候选概率总和自然一致；
- 特殊登记与最终信息不会脱节；
- 后果评估作用于完整结果；
- 决策日志可以完整复现；
- 不需要抽中后不断重掷。

---

## 5. 统一决策数据模型

### 5.1 结果必须按正交维度表达

```kotlin
enum class AbilityState {
    FUNCTIONING,
    MALFUNCTIONING_DRUNK,
    MALFUNCTIONING_POISONED,
}

enum class TruthRelation {
    TRUE_TO_ACTUAL_STATE,
    TRUE_TO_REGISTERED_STATE,
    FALSE_TO_ACTUAL_STATE,
    PARTIALLY_TRUE,
    NOT_APPLICABLE,
}
```

"能力是否生效""信息与实际状态的关系""是否发生特殊登记"不能合并为一个枚举。一个候选可能同时包含多个登记事实；能力失效时也可能碰巧给出与实际状态一致的信息。

### 5.2 决策请求

```kotlin
data class StorytellerDecisionRequest(
    val requestId: String,
    val idempotencyKey: String,
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val round: Int,
    val phase: StorytellerPhase,
    val sourceSeat: Int?,
    val actorActualRole: RoleId?,
    val abilityRole: RoleId,
    val abilityInstanceId: String,
    val abilityType: AbilityType,
    val decisionSequence: Int,
    val rulesetRef: RulesetRef,
    val algorithmConfigVersion: String,
    val gameState: DynamicGameState,
)
```

`abilityRole` 表示本次正在模拟或结算的能力。酒鬼实际角色为 `Drunk`，但可能以 `Investigator` 的能力流程接收信息，两者不得共用一个含糊的 `sourceRole`。

### 5.3 完整候选

```kotlin
data class DecisionCandidate<T>(
    val candidateId: String,
    val candidateFamilyId: String,
    val outcome: T,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact> = emptyList(),
    val effects: List<EffectDraft> = emptyList(),
    val metadata: CandidateMetadata,
)
```

`EffectDraft` 必须使用强类型对象区分：给玩家的信息、死亡、身份变化、阵营变化、状态变化和待办提醒。自由文本只能作为展示或日志草稿，不能替代权威状态变化。

### 5.4 评估结果

```kotlin
data class DecisionEvaluation<T>(
    val candidate: DecisionCandidate<T>,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val withinFamilyWeightFixedPoint: Long,
    val finalProbabilityFixedPoint: Long,
    val pressureDelta: Map<Int, Int>,
    val warnings: List<String>,
    val explanationCodes: List<String>,
)
```

### 5.5 特殊登记事实

```kotlin
data class RegistrationFact(
    val interactionId: String,
    val subjectSeat: Int,
    val registeredRole: RoleId?,
    val registeredType: CharacterType?,
    val registeredAlignment: Alignment?,
    val registrationQuestion: RegistrationQuestion,
    val reason: RegistrationReason,
)
```

必须使用列表而不是单个可空登记。厨师等数字能力可能在同一次结算中，对同一隐士的不同相邻关系采用不同登记；所有这些登记都必须与最终数字绑定在同一个候选中。

### 5.6 规则知识引用

```kotlin
data class RulesetRef(
    val scriptId: ScriptId,
    val scriptContentHash: String,
    val rulesetVersion: String,
    val sourceRevision: String,
    val coverage: RuleCoverage,
)
```

**`scriptContentHash` 规范：**

- 哈希内容包括：所有在场角色的文本描述、夜间顺序、Jinx 列表（按角色 ID 字典序排列后序列化为 UTF-8 JSON）；
- 哈希算法：SHA-256，取前 16 字节（128 位），以十六进制小写字符串表示；
- 内容来源：应用内嵌的规则资源文件（如 `assets/rules/trouble_brewing.json`），不依赖网络下载；
- 哈希不匹配时的处理策略：自动模式降级为人工确认，并在日志中记录 `RULESET_HASH_MISMATCH` 警告，不允许静默使用旧知识包。

候选、事件和模拟报告都必须引用相同的 `RulesetRef`。角色文本、夜序、Jinx 或相互作用发生变化后，不得继续静默使用旧知识包。

### 5.7 候选家族分类表（新增）

`candidateFamilyId` 是防止概率泄漏的核心机制。新增候选不能意外改变已有家族的总概率预算。以下为《暗流涌动》初始设置阶段和动态阶段的标准家族分类：

| 场景 | 候选家族 ID | 说明 |
|---|---|---|
| 酒鬼显示角色（非调查员） | `drunk-shown-role` | 每个显示角色为一个独立候选，同属一个家族 |
| 酒鬼显示调查员 + 线索 | `drunk-investigator-info` | 每个（显示角色, 爪牙, 候选对）三元组为一个候选 |
| 恶魔伪装身份组合 | `demon-bluffs` | 每个三角色组合为一个候选 |
| 占卜师红鲱鱼 | `red-herring` | 每个合法好人座位为一个候选 |
| 自然真实信息 | `natural-truth` | 规则唯一确定的真实结果 |
| 能力失效给真 | `malfunction-truth` | 失效但碰巧与实际状态一致 |
| 能力失效给假（数字） | `malfunction-falsehood-numeric` | 失效后显示错误数字，每个错误数字为一个候选 |
| 能力失效给假（角色/阵营） | `malfunction-falsehood-role` | 失效后显示错误角色或错误阵营 |
| 隐士特殊登记 | `registration-recluse` | 每种登记结果（登记为恶魔/爪牙/邪恶阵营）为一个候选 |
| 间谍特殊登记 | `registration-spy` | 每种登记结果（登记为善良/镇民/外来者）为一个候选 |

规则：

- 同一家族内新增候选只改变家族内部各候选的权重分配，不改变该家族的总概率预算；
- 不同家族之间的概率预算由 `FamilyPolicy` 根据场景和风格配置独立分配；
- 真信息只有一个候选、假信息有多个候选时，不会因候选数量差异导致概率失真。

---

## 6. 统一决策流水线

```text
1. 读取带版本号的不可变游戏状态快照
2. 使用幂等键查询是否已有已保存结果；若有则直接返回
3. 判断能力是否正常生效
4. 识别检测语义
5. 生成自然真实候选
6. 生成合法特殊登记候选
7. 若能力失效，生成失效真与失效假候选
8. 运行合法性验证
9. 计算候选特征与质量评分
10. 运行后果评估
11. 淘汰 REJECTED 候选
12. 选择最高可用质量等级并建立分数容差池
13. 按候选家族分配概率预算，再在家族内部归一化
14. 使用稳定种子选择一次
15. 提交前再次校验状态版本，拒绝过期结果
16. 原子追加决策事件；账本与历史只作为事件投影
17. 将强类型效果草稿交给自动执行层或人工确认层
```

### 6.1 检测语义

```kotlin
enum class DetectionSemantics {
    ACTUAL_ROLE,
    CHARACTER_TYPE,
    ALIGNMENT,
    SPECIFIC_MINION,
    DEMON_DETECTION,
    NUMERIC_INFORMATION,
    ABILITY_EFFECT,
}
```

每种能力必须明确使用哪种检测语义，避免"角色有特殊登记，所以所有检测都随机登记"。

---

## 7. 候选选择算法

### 7.1 硬合法性先于评分

任何不合法候选直接淘汰，不允许依靠低权重继续留在池中。

```kotlin
val legalCandidates = candidates.filter {
    legalityValidator.validate(it, state).isValid
}
```

### 7.2 质量等级

```kotlin
enum class QualityTier {
    RECOMMENDED,
    ACCEPTABLE_WITH_WARNING,
    EXPERT_ONLY,
    REJECTED,
}
```

最终选择只能在最高可用质量等级中进行。

### 7.3 分数容差池

```kotlin
val bestTier = legalEvaluations.maxBy {
    it.qualityTier.rankingPriority()
}.qualityTier

val sameTier = legalEvaluations.filter {
    it.qualityTier == bestTier
}

val bestScore = sameTier.maxOf {
    it.totalScore
}

val pool = sameTier.filter {
    it.totalScore >= bestScore - scoreTolerance
}
```

以下初始值为**临时预估值**，需在 PR 5 的 1000 局分布测试后校准，不应视为最终配置：

| 决策类型 | 分数容差（待校准） |
|---|---:|
| 初始整套线索 | 8 |
| 初始单项线索 | 6 |
| 普通动态信息 | 4 |
| 高冲击动态裁定 | 2 |
| 纯规则性选择 | 0 |

### 7.4 候选家族与最终选择权重

评分与概率权重分离：

- `totalScore` 表达候选质量；
- `withinFamilyWeightFixedPoint` 表达候选在所属家族内部的选择倾向；
- `finalProbabilityFixedPoint` 表达家族归一化后的最终概率。

禁止直接把所有候选放入同一个权重池。否则"真信息只有一个候选、假信息有多个候选"会使假结果仅因候选数量更多而获得额外概率。

先为候选家族分配总概率：

```kotlin
val familyMass = familyPolicy.normalizedMass(
    familyId = candidate.candidateFamilyId,
    context = context,
)
```

再在家族内部归一化。**权重计算使用定点整数，不使用浮点 `exp()`**，以保证跨设备和跨 JVM 版本的确定性：

```kotlin
// scoreDelta 为负整数或零（候选分 - 最佳分）
// temperature 为正整数，建议初始值 10，经模拟校准后调整
// 使用定点线性衰减替代 exp()，避免浮点边界差异改变选择结果
val scoreWeight: Long = maxOf(0L, temperature.toLong() + scoreDelta.toLong())

val withinFamilyWeight: Long =
    scoreWeight *
    historyMultiplierFixedPoint /
    FIXED_POINT_SCALE *
    diversityMultiplierFixedPoint /
    FIXED_POINT_SCALE *
    consequenceMultiplierFixedPoint /
    FIXED_POINT_SCALE

val finalProbabilityFixedPoint: Long =
    familyMassFixedPoint * withinFamilyWeight / familyWeightSum
```

其中：

- 分数越接近最佳值，权重越高；低于容差的候选不会进入池；
- 所有乘数必须定义上下限，防止溢出；
- 某个风险因素只能进入 `totalScore` 或进入概率乘数之一，不允许双重计算；
- `FIXED_POINT_SCALE` 建议为 `1_000_000L`（百万分之一精度）；
- 如后续模拟证明线性衰减对多样性不足，可改用分档映射（如 `scoreWeight = lookupTable[scoreDelta.coerceIn(-tableSize, 0)]`），但仍须使用定点整数，并升级 `selectorVersion`。

### 7.5 候选顺序独立

选择前必须按稳定 `candidateId` 排序。

```kotlin
val canonicalPool = pool.sortedBy {
    it.candidate.candidateId
}
```

这样即使候选生成顺序改变，相同输入仍得到相同结果。

---

## 8. 初始线索生成改进

### 8.1 酒鬼显示角色

取消"非调查员一律降级"（删除 `PlanEvaluator.kt` 中对非调查员触发 `ACCEPTABLE_WITH_WARNING` 的 `if (decision.role != investigator)` 代码块）。

推荐酒鬼角色池：

- 洗衣妇；
- 图书管理员；
- 调查员；
- 厨师；
- 共情者；
- 占卜师；
- 送葬者。

角色适配由以下特征决定：

- 是否能提供有意义的信息体验；
- 是否容易过快自证；
- 是否需要持续动态处理；
- 是否与当前配置重复；
- 最近几局是否重复使用；
- 是否与恶魔伪装身份产生过强重叠。

调查员可以保持较高基础适配度，但不能具有唯一 `RECOMMENDED` 等级。

### 8.2 红鲱鱼

候选评分应考虑：

- 目标角色暴露敏感度；
- 是否已经承受其他错误信息；
- 是否与酒鬼线索重叠；
- 是否容易被其他可靠能力直接确认；
- 最近几局是否重复选择相同角色类型。

### 8.3 恶魔伪装角色

避免只按"容易伪装"取固定最佳组合。

同时考虑：

- 当前不在场角色；
- 邪恶方讨论空间；
- 是否与酒鬼显示角色冲突；
- 最近游戏是否重复；
- 角色组合的难度梯度；
- 新手局是否至少提供一个容易伪装角色。

### 8.4 整套初始方案

完整初始方案应作为组合候选参与评分，而不只逐项独立选择。

需要检查：

- 多条线索是否集中攻击同一名玩家；
- 酒鬼身份与恶魔伪装是否重叠；
- 红鲱鱼是否与错误调查线索叠加；
- 是否存在过度确认链；
- 是否至少保留多个合理世界。

---

## 9. 自然真实结果优先规则

### 9.1 健康图书管理员与唯一隐士

条件：

- 图书管理员健康且清醒；
- 能力正常生效；
- 场上只有一个实际外来者；
- 该外来者是隐士。

《暗流涌动》默认产品策略（`constraintAuthority = PRODUCT_POLICY_REQUIRED`）：

- 必须显示隐士；
- 两名候选中必须包含真实隐士；
- 结果来源为 `NATURAL_TRUTH`；
- 只随机选择另一名诱饵玩家；
- 不调用隐士特殊登记策略；
- 不调用误导策略。

规则层仍应能表达隐士的官方特殊登记能力；本策略只是在当前剧本配置中将该候选禁用或降级，而不是把它错误标记为"官方规则不合法"。

```kotlin
if (
    librarian.isHealthy &&
    librarian.isSober &&
    actualOutsiders.size == 1
) {
    val outsider = actualOutsiders.single()

    return naturalCandidate(
        shownRole = outsider.actualRole,
        candidateSeats = listOf(
            outsider.seat,
            chooseBalancedDecoy(),
        ),
    )
}
```

### 9.2 健康调查员与唯一间谍

条件：

- 调查员健康且清醒；
- 能力正常生效；
- 间谍是唯一实际爪牙；
- 没有其他完整特殊登记候选被选择。

自然真实候选必须：

- 显示间谍；
- 两名候选中包含真实间谍；
- 结果来源为 `NATURAL_TRUTH`。

间谍"可以登记为善良、镇民或外来者"，不等于系统可以凭空显示一个不存在的男爵、投毒者或猩红女巫。

### 9.3 存在隐士时的调查员候选

若场上同时有唯一间谍与隐士，可生成两类完整候选：

#### 候选 A：自然真实

- 显示间谍；
- 包含真实间谍；
- `NATURAL_TRUTH`。

#### 候选 B：隐士特殊登记

- 隐士完整登记为某个具体爪牙；
- 显示该爪牙；
- 候选玩家中包含隐士；
- `SPECIAL_REGISTRATION`。

候选 B 必须在特殊登记系统中实现，不允许通过临时文案补丁实现。

---

## 10. 动态线索与局内叙事

### 10.1 两个时间尺度

| 时间尺度 | 主要目标 |
|---|---|
| 同一局内 | 连续、合理、可推理 |
| 不同游戏间 | 避免形成固定程序规律 |

例：

- 同一局中毒共情者连续得到 1，可以是合理叙事。
- 不同游戏中真实值为 0 时，不应总固定显示 1。

### 10.2 连续叙事与重复针对分离

```kotlin
data class NarrativeFactors(
    val continuityValue: Int,
    val repeatedTargetPressure: Int,
)
```

- `continuityValue`：奖励合理延续；
- `repeatedTargetPressure`：惩罚反复攻击同一玩家。

激进模式可以降低重复针对惩罚，但不能把它变为正向奖励。

### 10.3 数字型信息

评分应考虑：

- 与真实值距离；
- 与上一晚结果距离；
- 邻居或局面是否变化；
- 是否出现不自然的大跳；
- 玩家整局获得的真假比例；
- 数字变化是否会直接锁定某名玩家。

### 10.4 玩家主动选择目标

例如占卜师、守鸦人。

应考虑：

- 玩家是否正确命中恶魔或爪牙；
- 假信息是否完全抹杀正确判断；
- 是否为一次性或死亡触发能力；
- 当前是否处于终局；
- 好人或邪恶是否已经明显领先。

系统仍可给假，但高冲击假结果应受到更严格后果评估。

---

## 11. 醉酒与中毒的能力失效模型

### 11.1 与特殊登记完全分离

```kotlin
interface MalfunctionPolicy {
    fun generateCandidates(
        context: MalfunctionContext,
    ): List<DecisionCandidate<*>>
}
```

醉酒或中毒意味着能力失效，候选可以包括：

- `MALFUNCTION_TRUTH`；
- `MALFUNCTION_FALSEHOOD`。

能力失效不等于必须给假。

### 11.2 不再单独掷"误导概率"

推荐对完整候选赋予不同权重。

例如中毒共情者真实值为 0：

| 候选 | 来源 | 示例权重倾向 |
|---|---|---|
| 显示 0 | MALFUNCTION_TRUTH | 较低或中等 |
| 显示 1 | MALFUNCTION_FALSEHOOD | 通常较高 |
| 显示 2 | MALFUNCTION_FALSEHOOD | 高冲击，通常较低 |

最终概率由候选权重归一化产生。

### 11.3 能力类型基础倾向

以下仅作为初始校准范围，不是官方概率：

| 信息类型 | 温和 | 平衡 | 激进 |
|---|---:|---:|---:|
| 一次性开局信息：假结果总权重占比 | 55–65% | 65–75% | 75–85% |
| 每夜数字信息：假结果总权重占比 | 40–55% | 55–70% | 65–80% |
| 玩家主动选择目标 | 按结果冲击调整 | 设置合理上限 | 可更高但须后果评估 |
| 死亡触发一次性能力 | 35–50% | 50–65% | 60–75% |

这些范围只是候选家族的目标分布，不应写成每次独立固定概率。

### 11.4 场景修正因素

提高假结果权重：

- 邪恶方明显落后；
- 真信息会立即完全暴露恶魔；
- 最近几次均给真；
- 假结果可以增加多个合理世界；
- 假结果不会集中打击同一玩家。

降低假结果权重：

- 好人已经明显落后；
- 最近高冲击假信息过多；
- 同一玩家已承受大量怀疑；
- 一次性能力会完全失去体验；
- 终局一条假信息近乎自动决定胜负；
- 假结果与其他线索叠加后几乎锁死无辜玩家。

---

## 12. 隐士与间谍的特殊登记模型

### 12.1 独立策略入口

```kotlin
interface RegistrationPolicy {
    fun generateCandidates(
        context: RegistrationContext,
    ): List<DecisionCandidate<*>>
}
```

```kotlin
data class RegistrationContext(
    val registeringRole: RoleId,
    val detectingRole: RoleId,
    val registrationQuestion: RegistrationQuestion,
    val phase: StorytellerPhase,
    val gameBalance: GameBalanceSnapshot,
    val history: RegistrationLedger,
    val playerPressure: PlayerInformationPressure,
)
```

### 12.2 检测语义矩阵

| 检测能力 | 正常检测语义 | 隐士处理 |
|---|---|---|
| 图书管理员 | 实际外来者身份 | 隐士本来就是外来者，优先自然真实 |
| 调查员 | 具体爪牙身份 | 可生成隐士登记为具体爪牙的完整候选 |
| 共情者 | 邻近邪恶数量 | 可登记为邪恶 |
| 占卜师 | 两人中是否有恶魔 | 可登记为恶魔 |
| 送葬者 | 被处决者身份 | 可登记为邪恶角色 |
| 杀手 | 目标是否为恶魔 | 高冲击，必须严格控制 |

### 12.3 场景化初始倾向

| 场景 | 温和 | 平衡 | 激进 |
|---|---:|---:|---:|
| 占卜师查隐士，登记为恶魔 | 35–50% | 50–70% | 65–80% |
| 共情者邻近隐士，登记为邪恶 | 25–40% | 40–60% | 55–70% |
| 送葬者显示隐士为邪恶角色 | 30–45% | 45–65% | 60–75% |
| 杀手射隐士，登记为恶魔并死亡 | 0–5% | 5–15% | 10–25% |

这些数值必须经过以下因素修正：

- 隐士已承受的怀疑；
- 是否已是红鲱鱼；
- 此前登记次数；
- 是否连续登记为同一方向；
- 当前阵营优势；
- 是否处于终局；
- 是否会直接导致不公平失败。

### 12.4 间谍登记

间谍可在相应检测中登记为善良、镇民或外来者。

但必须注意：

- 不能总是完美躲避所有检测；
- 不能因间谍未登记为爪牙，就凭空制造不存在的其他爪牙；
- 每一次登记必须与具体检测问题绑定；
- 最近连续躲避检测后，应降低再次躲避的倾向。

---

## 13. 玩家信息压力与决策账本

### 13.1 玩家信息压力

```kotlin
data class PlayerInformationPressure(
    val seat: Int,
    val directSuspicion: Int,
    val indirectSuspicion: Int,
    val confirmation: Int,
    val recentTargetCount: Int,
    val highImpactTargetCount: Int,
)
```

现有 `DynamicGameState.informationPressureBySeat: Map<Int, Int>` 中的单整数对应迁移至 `directSuspicion`，原字段在 PR 8 完成后废弃。迁移期间两者并存，`PlayerInformationPressure` 为权威来源。

用途：

- 防止调查员、占卜师、共情者等信息叠加攻击同一名好人；
- 防止同一名玩家长期成为固定诱饵；
- 防止红鲱鱼、隐士登记和中毒假信息过度重叠；
- 控制终局中的信息公平性。

### 13.2 误导账本

```kotlin
data class MisinformationLedger(
    val totalOpportunities: Int,
    val falseInformationCount: Int,
    val highImpactFalseCount: Int,
    val consecutiveFalseCount: Int,
    val truthfulWhileImpairedCount: Int,
)
```

不能只看连续假信息次数。

"假、真、假、真、假"的连续假信息始终为 1，但总体假信息已经很多。

### 13.3 特殊登记账本

```kotlin
data class RegistrationLedger(
    val evilRegistrationCount: Int,
    val goodRegistrationCount: Int,
    val minionRegistrationCount: Int,
    val demonRegistrationCount: Int,
    val highImpactRegistrationCount: Int,
    val consecutiveSameRegistrationCount: Int,
)
```

账本属于软预算：

- 不是达到次数后禁止登记；
- 而是逐步改变候选权重；
- 合法候选很少时仍可重复。

---

## 14. 后果评估与风险控制

### 14.1 硬否决

以下候选直接 `REJECTED`：

- 违反角色规则；
- 目标不合法；
- 显示内容与特殊登记不一致；
- 健康图书管理员面对唯一隐士却不包含隐士；
- 健康调查员面对唯一间谍，在没有完整特殊登记基础时显示不存在的其他爪牙；
- 候选依赖未发生的登记；
- 结果造成内部状态矛盾。

### 14.2 软风险惩罚

以下情况降低分数、质量等级或选择权重：

- 同一玩家已承受过高怀疑；
- 高冲击假信息近期使用过多；
- 特殊登记近期使用过多；
- 终局一条假信息近乎自动决定胜负；
- 一次性能力核心体验被完全剥夺；
- 多条可靠信息已经锁定同一玩家；
- 候选使局面失去可推理空间；
- 邪恶方已经明显领先。

### 14.3 不使用抽中后无限重掷

正确方式：

1. 先对完整候选评估后果；
2. 将风险转换为淘汰、降级或权重；
3. 最后只选择一次。

---

## 15. 稳定随机与可复现性

### 15.1 决策种子

```kotlin
decisionSeed = hash(
    persistedGameSeed,
    gameId,
    idempotencyKey,
    gameStateRevision,
    playerInputRevision,
    historyDigest,
    rulesetVersion,
    algorithmConfigVersion,
    selectorVersion,
)
```

`persistedGameSeed` 必须随存档保存和迁移，不能依赖设备安装种子，否则换机恢复无法复现。

### 15.2 必须保证

- Compose 重组不改变结果；
- 返回同一页面不改变结果；
- 横竖屏切换不改变结果；
- 存档恢复不改变结果；
- 下一回合可以变化；
- 下一局可以变化；
- 算法版本升级后可以明确区分。

### 15.3 稳定候选 ID 与哈希算法规范

禁止使用对象默认 `hashCode()`、`toString()` 或依赖字段拼接顺序的哈希（现有 `RecommendationSearch.kt` 中的 `canonical.hashCode() xor foldedSeed` 也属于此类，需在 PR 4 中替换）。

**指定哈希算法：MurmurHash3-x64-128，取低 64 位作为 `Long`。**

理由：确定性跨平台、无需加密强度、速度快、碰撞率低、纯 Kotlin/Java 实现无需 NDK。

```kotlin
candidateId = murmurHash3_x64_128_low64(
    candidateSchemaVersion,
    abilityState.name,
    truthRelation.name,
    abilityRole.value,
    shownRole?.value ?: "",
    sortedCandidateSeats.joinToString(","),
    numericValue?.toString() ?: "",
    registrationDetails,   // 规范化序列化，见下文
)
```

规范化序列化规则：

- 所有字段按固定顺序拼接，使用 `|` 分隔；
- 集合类型（如 `sortedCandidateSeats`）必须排序后序列化；
- 可空字段缺失时写入空字符串占位，不省略；
- 字符串使用 UTF-8 编码；
- 数值使用十进制字符串，不使用本地化格式。

`decisionSeed` 同样使用 MurmurHash3-x64-128 对上述输入字段列表计算。

所有算法版本（随机数生成器、定点权重算法、候选 ID 算法）都必须有版本号及跨版本 golden test vectors，以便在升级后验证兼容性。

---

## 16. 跨局历史与模板冷却

### 16.1 历史指纹

```kotlin
data class HistoricalClueSignature(
    val decisionType: String,
    val drunkShownRole: RoleId?,
    val shownCharacter: RoleId?,
    val candidateAlignmentPattern: String?,
    val candidateSeatDistance: Int?,
    val redHerringRole: RoleId?,
    val demonBluffs: Set<RoleId>,
)
```

### 16.2 保存范围

保存最近 5–10 局。

### 16.3 冷却规则

- 上一局相同酒鬼显示角色：强惩罚；
- 最近三局重复显示同一爪牙或外来者：中等惩罚；
- 相同真假结构：轻度惩罚；
- 相同座位距离：轻度惩罚；
- 相同红鲱鱼角色：中等惩罚；
- 相同恶魔伪装组合：中等惩罚；
- 时间越久，惩罚越弱。

历史冷却不能把合法、明显更好的候选淘汰，只改变优质候选池中的权重。

---

## 17. 决策日志、解释与纠错

### 17.1 决策事件

```kotlin
data class StorytellerDecisionEvent(
    val eventId: String,
    val requestId: String,
    val idempotencyKey: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val rulesetRef: RulesetRef,
    val algorithmConfigVersion: String,
    val selectorVersion: String,
    val decisionSeed: Long,
    val stateDigest: String,
    val historyDigest: String,
    val selectedCandidateId: String,
    val selectedOutcomeSnapshot: DecisionOutcomeSnapshot,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact>,
    val qualityTier: QualityTier,
    val totalScore: Int,
    val finalProbabilityFixedPoint: Long,
    val pressureDelta: Map<Int, Int>,
    val candidatePoolFingerprint: String,
    val candidateAudit: List<CandidateAuditSummary>,
    val explanationCodes: List<String>,
    val status: DecisionEventStatus,
)

enum class DecisionEventStatus {
    PROPOSED,
    CONFIRMED,
    APPLIED,
    FAILED,
}
```

只保存候选池指纹不足以回答"当时还有哪些候选、为什么没有选择另一个"。事件必须保存选中结果快照和足够的候选审计摘要，使旧算法代码不再存在时仍能复盘。

**关于敏感数据可见性：** 在本地 Android 应用场景中，强制隔离说书人视图与玩家视图的技术成本较高，可作为产品决策延后处理。当前阶段只需确保 UI 层在玩家可见界面中不主动展示 `decisionSeed`、实际角色和候选审计字段，不要求底层存储加密。

### 17.2 推荐解释代码

```text
natural.only-outsider-recluse
natural.only-minion-spy
selection.high-quality-pool
selection.weighted-stable-random
selection.cross-game-cooldown
malfunction.truth-selected
malfunction.falsehood-selected
registration.recluse-as-demon
registration.spy-as-good
pressure.repeated-target-penalty
consequence.final-day-impact-penalty
consequence.one-shot-ability-protection
```

### 17.3 纠错事件

不直接覆盖旧结果。

```kotlin
data class DecisionCorrectionEvent(
    val eventId: String,
    val replacedEventId: String,
    val replacementEventId: String,
    val reasonCode: String,
)
```

### 17.4 账本由事件重建

事件日志是事实来源。

误导账本、登记账本和玩家压力可由事件重建，避免存档中出现两套不一致状态。允许保存带事件偏移量的可丢弃投影缓存，但不得把"保存事件"和"更新另一套权威账本"作为两个独立步骤。

事件追加必须满足：

- 同一幂等键最多一个有效决策事件；
- 提交时的游戏状态版本必须与请求一致；
- 更正链只允许线性追加，不允许从旧版本分叉；
- 自动执行失败不得推进夜序，事件需明确记录 `PROPOSED`、`CONFIRMED`、`APPLIED` 或 `FAILED` 状态。

---

## 18. 建议代码结构

```text
clocktower/
├── config/
│   ├── RecommendationProfile.kt
│   ├── UncertaintyProfile.kt
│   ├── RegistrationProfile.kt
│   └── TroubleBrewingRecommendationMetadata.kt
├── domain/
│   ├── GameState.kt
│   ├── DecisionRequest.kt
│   ├── DecisionCandidate.kt
│   ├── DecisionEvaluation.kt
│   ├── AbilityState.kt
│   ├── TruthRelation.kt
│   ├── RegistrationFact.kt
│   ├── DetectionSemantics.kt
│   ├── RulesetRef.kt
│   └── StorytellerDecisionEvent.kt
├── rules/
│   ├── DeterministicRuleEngine.kt
│   ├── InformationTruthResolver.kt
│   ├── RegistrationLegality.kt
│   └── TargetLegalityValidator.kt
├── recommendation/
│   ├── setup/
│   │   ├── SetupCandidateGenerator.kt       ← 替换现有 CandidateGenerator.kt
│   │   ├── SetupEvaluator.kt                ← 替换现有 PlanEvaluator.kt
│   │   └── SetupRecommendationService.kt    ← 替换现有 RecommendationSearch.kt
│   ├── dynamic/
│   │   ├── DynamicCandidateGenerator.kt     ← 替换 AutomaticInformationPolicy.kt 核心逻辑
│   │   ├── MalfunctionPolicy.kt             ← 拆分自现有 UnreliableNumberInformationRecommender.kt
│   │   │                                       和 UnreliableCategoricalInformationRecommender.kt
│   │   ├── RegistrationPolicy.kt            ← 拆分自现有 SpecialRegistrationRecommender.kt
│   │   │                                       和 PairInformationRecommender.kt
│   │   └── ConsequenceEvaluator.kt          ← 新增，无现有对应文件
│   ├── selection/
│   │   ├── WeightedStableSelector.kt        ← 替换现有 AutomaticStorytellerSelector.kt
│   │   ├── CandidatePoolBuilder.kt          ← 新增
│   │   └── DecisionSeedFactory.kt           ← 新增
│   └── pressure/
│       ├── PlayerPressureCalculator.kt      ← 新增
│       ├── MisinformationLedger.kt          ← 新增
│       └── RegistrationLedger.kt            ← 新增
├── history/
│   ├── StorytellerEventStore.kt             ← 新增
│   ├── DecisionHistoryRepository.kt         ← 替换现有 InformationReferenceExtractor.kt 部分职责
│   └── HistoricalClueSignature.kt           ← 新增
├── simulation/
│   ├── StorytellerSimulationRunner.kt       ← 新增
│   ├── DistributionReport.kt                ← 新增
│   └── RegressionScenarioCatalog.kt         ← 新增
└── session/
    ├── ClocktowerGameSession.kt             ← 新增（整合现有 MainActivity 中的会话状态）
    └── ClocktowerRecommendationCoordinator.kt ← 新增
```

**现有文件处置说明：**

| 现有文件 | 处置 | 目标 PR |
|---|---|---|
| `CandidateGenerator.kt` | 由 `SetupCandidateGenerator.kt` 替换后删除 | PR 5 |
| `PlanEvaluator.kt` | 由 `SetupEvaluator.kt` 替换后删除 | PR 5 |
| `RecommendationSearch.kt` | 由 `SetupRecommendationService.kt` 替换后删除 | PR 5 |
| `PlanDiversifier.kt` | 逻辑迁移至 `CandidatePoolBuilder.kt` 后删除 | PR 4 |
| `AutomaticStorytellerSelector.kt` | 由 `WeightedStableSelector.kt` 替换后删除 | PR 4 |
| `AutomaticInformationPolicy.kt` | 核心逻辑迁移至 `DynamicCandidateGenerator.kt` 后删除 | PR 6 |
| `UnreliableNumberInformationRecommender.kt` | 迁移至 `MalfunctionPolicy.kt` 后删除 | PR 7 |
| `UnreliableCategoricalInformationRecommender.kt` | 迁移至 `MalfunctionPolicy.kt` 后删除 | PR 7 |
| `PairInformationRecommender.kt` | 迁移至 `RegistrationPolicy.kt` 后删除 | PR 7 |
| `SpecialRegistrationRecommender.kt` | 迁移至 `RegistrationPolicy.kt` 后删除 | PR 7 |
| `InformationReferenceExtractor.kt` | 部分职责迁移至 `DecisionHistoryRepository.kt` 后删除 | PR 8 |
| `GameBalanceEvaluator.kt` | 保留，接口由协调层调用 | 不删除 |
| `MayorRedirectRecommender.kt` | 保留，接口由协调层调用 | 不删除 |
| `DemonSuccessorRecommender.kt` | 保留，接口由协调层调用 | 不删除 |

### 18.1 协调层

```kotlin
class ClocktowerRecommendationCoordinator {
    fun recommendSetup(...)
    fun resolveInformation(...)
    fun resolveRegistration(...)
    fun resolveDynamicDecision(...)
    fun explainDecision(...)
}
```

UI 只提交请求并显示结果，不直接负责：

- 候选生成；
- 随机；
- 评分；
- 特殊登记；
- 账本更新；
- 后果否决。

### 18.2 暂不全面重写 MainActivity

先建立稳定领域接口和协调层，再进行 UI 拆分。

---

## 19. 实施阶段与 PR 拆分

### 阶段 0：测试安全网

目标：锁定当前行为。

任务：

- 先审计角色文本、官方规则事实与现有行为，避免把已知错误固化成回归测试；
- 建立关键规则回归测试；
- 建立 1000 局模拟基线；
- 记录当前酒鬼显示角色分布；
- 记录当前动态数字分布；
- 记录当前模板连续重复率。

### PR 1：状态快照、版本与规则知识边界

- `GameSnapshot`、`gameStateRevision`、`playerInputRevision`；
- 持久化 `gameSeed`；
- `RulesetRef`、SHA-256 内容哈希（取前 16 字节）和规则核验状态；
- 明确哈希内容范围：在场角色文本、夜序、Jinx 列表，UTF-8 JSON 序列化；
- 从 MainActivity 抽出最小会话状态边界，不进行全面 UI 重写。

### PR 2a：酒鬼非调查员降级修复（独立热修复）

**不依赖 PR 3 的数据模型，可独立合并。**

- 删除 `PlanEvaluator.kt` 中 `if (decision.role != investigator)` 触发 `ACCEPTABLE_WITH_WARNING` 的代码块；
- 添加酒鬼显示角色分布单元测试，验证调查员不再垄断 `RECOMMENDED` 等级；
- 不引入 `TruthRelation`、`AbilityState` 或 `RegistrationFact`。

### PR 2b：自然真实候选（依赖 PR 3）

**必须在 PR 3 合并后进行。**

- 健康图书管理员面对唯一隐士时，100% 生成 `NATURAL_TRUTH` 候选；
- 健康调查员面对唯一间谍时，正确生成自然真实候选；
- 使用 PR 3 引入的 `TruthRelation` 和 `AbilityState` 建模；
- 添加单元测试。

### PR 3：统一决策模型

- `AbilityState` 与 `TruthRelation` 正交建模；
- `DetectionSemantics`；
- `DecisionRequest`；
- `DecisionCandidate`；
- `DecisionEvaluation`；
- `LegalityFailure` 补充 `constraintAuthority` 字段；
- 多登记事实与强类型效果草稿；
- 稳定候选 ID（MurmurHash3-x64-128 实现）。

### PR 4：幂等事件存储与稳定选择器

- `DecisionSeedFactory`（MurmurHash3-x64-128）；
- 幂等键、过期请求校验和原子事件追加；
- `CandidatePoolBuilder`（替换 `PlanDiversifier.kt`）；
- `WeightedStableSelector`（替换 `AutomaticStorytellerSelector.kt`，使用定点整数权重）；
- 候选家族概率预算与家族内归一化；
- 分数容差；
- 候选顺序独立测试；
- 删除 `PlanDiversifier.kt` 和 `AutomaticStorytellerSelector.kt`。

### PR 5：初始线索迁移

- 酒鬼显示角色（`SetupCandidateGenerator.kt` + `SetupEvaluator.kt`）；
- 图书管理员；
- 调查员；
- 红鲱鱼；
- 恶魔伪装；
- 整套初始方案；
- 1000 局分布测试，用于验证和校准 §7.3 分数容差初始值；
- 删除 `CandidateGenerator.kt`、`PlanEvaluator.kt`、`RecommendationSearch.kt`。

### PR 6：动态信息迁移

- 数字型信息；
- 二选一信息；
- 类别信息；
- 同一决策可复现；
- 跨局结果可变化；
- 迁移 `AutomaticInformationPolicy.kt` 核心逻辑至 `DynamicCandidateGenerator.kt`，完成后删除原文件。

### PR 7：能力失效与特殊登记拆分

- `MalfunctionPolicy`（整合 `UnreliableNumberInformationRecommender.kt` 和 `UnreliableCategoricalInformationRecommender.kt`）；
- `RegistrationPolicy`（整合 `SpecialRegistrationRecommender.kt` 和 `PairInformationRecommender.kt`）；
- 完整特殊登记候选；
- 取消二次独立登记掷骰；
- 删除上述四个旧文件。

### PR 8：事件审计与账本投影

- 决策事件（含 `DecisionEventStatus`）；
- `PlayerInformationPressure` 替换 `DynamicGameState.informationPressureBySeat`；
- 误导账本；
- 登记账本；
- 存档恢复；
- 纠错事件；
- 迁移 `InformationReferenceExtractor.kt` 部分职责至 `DecisionHistoryRepository.kt` 后删除原文件。

### PR 9：后果评估

- 重复针对惩罚；
- 一次性能力保护；
- 高冲击误导；
- 终局风险；
- 阵营优势修正。

### PR 10：跨局冷却与模拟校准

- 最近 5–10 局指纹；
- 衰减冷却；
- 统计报告；
- 参数校准工具；
- 使用 5 万局固定种子集验证 §7.3 容差最终值。

### PR 11：UI 与 MainActivity 剩余重构

- 协调层全面接管；
- 设置、夜间、白天、历史模块拆分；
- 显示推荐理由；
- 支持赛后复盘。

---

## 20. 测试、模拟与验收指标

### 20.1 自然真实规则测试

```kotlin
@Test
fun healthyLibrarianSeesTheOnlyRecluse()

@Test
fun healthyInvestigatorSeesTheOnlySpy()

@Test
fun spyRegistrationDoesNotInventAnotherMinion()

@Test
fun recluseRegistrationIsACompleteSeparateCandidate()
```

### 20.2 选择器测试

- 只从最高可用质量等级选择；
- 不选择超过分数容差的候选；
- 相同种子结果一致；
- 不同 `gameId` 可选择不同优质候选；
- 候选列表顺序改变不影响结果；
- 同一家族新增等价候选不改变家族总概率；
- 候选重复、ID 冲突、负权重、零总权重和溢出均安全失败；
- 同一幂等键重复调用只产生一个事件；
- 状态版本或玩家输入变化后拒绝应用旧结果；
- 高分候选更常出现但不垄断。

### 20.3 能力失效测试

- 中毒玩家可以获得失效真；
- 中毒玩家可以获得失效假；
- 高冲击错误结果权重较低；
- 连续高冲击误导会降低后续权重；
- 终局假信息受到额外惩罚。

### 20.4 特殊登记测试

- 隐士登记为男爵时，显示角色与登记一致；
- 占卜师"是"与隐士恶魔登记绑定；
- 共情者数字与隐士邪恶登记绑定；
- 杀手射隐士进入高冲击检查；
- 间谍不会永久躲避所有检测。

### 20.5 账本与恢复测试

- 事件可重建误导账本；
- 事件可重建登记账本；
- 事件可重建玩家压力；
- 存档恢复后同一决策不重新抽取；
- 纠错后保留原始事件。

### 20.6 批量模拟

1000 局用于快速 CI 基线；正式校准使用固定种子集运行至少 5 万局，并按人数、风格和关键角色组合分层。至少报告：

1. 1000 局酒鬼显示角色分布；
2. 1000 局调查员显示爪牙分布；
3. 1000 局图书管理员目标分布；
4. 中毒共情者连续 5 夜模拟；
5. 同一玩家连续被指向比例；
6. 隐士在不同检测能力下登记频率；
7. 间谍躲避检测频率；
8. 温和、平衡、激进模式对比；
9. 三人和四人终局高冲击决策；
10. 新旧算法模板重复率比较；
11. 每个候选家族的目标概率、实际概率及 95% 置信区间；
12. 角色选择熵、最大占比、连续重复长度和玩家压力 P95；
13. 新旧算法在相同场景、相同种子上的配对差异。

没有经过验证的玩家行为模型时，模拟胜率只作为观察项，不作为自动调参的主要目标。

### 20.7 验收指标

| 指标 | 目标 |
|---|---|
| 健康图书管理员面对唯一隐士 | 100% 自然真实 |
| 健康调查员面对唯一间谍 | 自然真实候选 100% 正确生成 |
| 酒鬼显示调查员比例 | 不再接近 100% |
| 相同完整初始模板连续出现 | 原则上不超过 2 局，除非候选极少 |
| 动态相同状态跨局输出 | 存在多个优质结果 |
| 同一玩家连续高压指向 | 显著低于旧算法 |
| 同一决策可复现性 | 100% |
| 特殊登记与显示语义一致 | 100% |
| 低质量候选被随机选中 | 0 |
| 事件日志可重建最终决策 | 100% |

---

## 21. 参数校准策略

### 21.1 所有概率配置必须可版本化

```kotlin
data class StorytellerAlgorithmConfig(
    val algorithmConfigVersion: String,
    val selectorVersion: String,
    val candidateSchemaVersion: String,
    val rulesetRef: RulesetRef,
    val profiles: Map<RecommendationStyle, RecommendationProfile>,
    val abilityPolicies: Map<RoleId, AbilityPolicy>,
    val registrationPolicies: Map<RegistrationKey, RegistrationProfile>,
)
```

### 21.2 先固定规则，再调体验参数

参数调整顺序：

1. 合法性；
2. 自然真实优先；
3. 特殊登记完整性；
4. 稳定随机；
5. 模板多样性；
6. 玩家压力；
7. 阵营平衡；
8. 温和、平衡、激进差异。

### 21.3 不以单场体验直接改参数

至少根据：

- 批量模拟；
- 多次真人测试；
- 决策日志；
- 模板重复率；
- 玩家压力；
- 胜率趋势；

共同调整。

---

## 22. 暂不实施的内容

### 22.1 不只修改固定百分比

把 82% 改成 70% 不能解决：

- 能力类型没有区分；
- 结果冲击没有区分；
- 特殊登记与失效混用；
- 玩家压力不足；
- 固定最高分垄断。

### 22.2 不先实现复杂跨局 AI

先使用明确、可测试的历史指纹和冷却算法。

### 22.3 不同时全面重写 UI、存档和算法

应按 PR 分层迁移，保持问题可定位。

### 22.4 不让 LLM 决定规则合法性

LLM 可解释和辅助排序，不替代规则引擎。

### 22.5 不立即扩展大量剧本

先把《暗流涌动》做到：

- 规则正确；
- 候选完整；
- 决策可解释；
- 存档可恢复；
- 模拟稳定；
- 真人体验合理。

---

## 23. 最终设计原则

> 自动说书人应当有内在逻辑，但不能让玩家通过熟悉程序习惯预测下一步。

最终系统流程：

```text
确定性规则引擎
        ↓
检测语义识别
        ↓
完整自然真实候选
        ↓
完整特殊登记候选
        ↓
完整能力失效候选
        ↓
合法性验证
        ↓
质量、平衡、叙事与压力评分
        ↓
后果评估
        ↓
最高质量等级候选池
        ↓
稳定加权随机一次选择（定点整数权重）
        ↓
自动执行或人工确认
        ↓
不可变决策日志
        ↓
恢复、解释、纠错、模拟与复盘
```

关键原则：

1. 唯一、明确、有价值的自然真实结果应坚定给出。
2. 健康图书管理员面对唯一隐士时，应显示隐士。
3. 健康调查员面对唯一间谍时，必须正确生成显示间谍的自然真实候选。
4. 特殊登记必须绑定完整候选，不能二次独立抽签。
5. 醉酒/中毒与隐士/间谍特殊登记必须分开建模。
6. 概率应来自完整候选权重，而不是独立"是否误导"掷骰。
7. 最高分应更常出现，但不能长期垄断。
8. 局内保持叙事连续，跨局避免固定模板。
9. 同一决策必须稳定、可复现；权重计算使用定点整数，不使用浮点 `exp()`。
10. 玩家压力、高冲击结果和终局风险必须进入评估。
11. 所有重要决定必须可追踪、可解释、可纠错和可复盘。
12. 先保证规则与架构正确，再通过模拟和真人测试校准概率。
13. 能力状态、真假关系与特殊登记必须正交建模，且一个候选可以包含多个登记事实。
14. 候选家族先获得总概率预算，再在家族内部归一化，候选数量不能偷走概率质量。
15. 同一幂等键只能产生一个权威事件；过期建议和过期自动结果不得应用。
16. 规则知识、算法配置、选择器和候选模式均必须版本化。
17. 事件必须保存结果快照与候选审计摘要，不能只依赖哈希和未来重算。
18. 自动模式与人工模式共享同一候选和事件合同；差别只在最终确认策略。
19. 候选家族分类必须在实施前明确定义，不得在 PR 中临时决定。
20. 哈希算法（MurmurHash3-x64-128）和定点整数精度（`FIXED_POINT_SCALE = 1_000_000L`）必须在版本升级时更新 `selectorVersion` 和 golden test vectors。
