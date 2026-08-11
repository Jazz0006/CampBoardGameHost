# CampBoardGameHost 自动说书人玩家认知一致性算法改进方案 v2

> **历史版本：已被 `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` 取代。后续实施以 v2.2 为唯一规范；本文仅保留为设计演进记录。**

> 版本：2.0  
> 日期：2026-08-11  
> 状态：实施前最终架构设计稿  
> 适用范围：优先覆盖《暗流涌动》（Trouble Brewing），架构支持后续剧本扩展  
> 基础文档：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v1.md` v1.1  
> 本版目的：整合 `botc-asp`、`olarozenfeld/botc`、`botc-zdd-`、ISES 及其后续 Social Deduction 研究，形成可直接交给 Codex 分阶段实施的路线

---

## 1. 执行结论

本项目不再把“玩家认知一致性”理解为推荐评分中的一个附加分数，而是建立一个独立、可验证、可查询的玩家视角 Possible-Worlds 基础层。

最终技术路线为：

```text
Official / Formal BotC Rules
          ↓
Legal Choice Layer
          ↓
Player-perspective World Engine
          ↓
PlayerWorldSet(P, t)
          ↓
Candidate Simulation
beforeWorlds → candidate → afterWorlds
          ↓
Epistemic Metrics
一致性 / 熵 / 模糊度 / 恶魔集中度 / 叙事分支
          ↓
Storyteller Recommendation Policy
信息节奏 / 邪恶伪装支持 / 确认链 / 历史压力 / 风格
          ↓
Quality Gate
          ↓
Stable Weighted Selection
```

核心职责分工：

- `pnkfelix/botc-asp`：开发期 Formal Rule Oracle / Cross-validation Oracle；
- `olarozenfeld/botc`：Perspective、GameLog、Possible Worlds、Assumption 等领域建模参考；
- `pnkfelix/botc-zdd-`：移动端 Runtime World Engine / `PlayerWorldSet` 的重点技术参考；
- ISES：Information Set、Entropy、Information Gain、有限预算采样搜索的理论参考；
- CSP4SDG：未来多人社交推理中 Hard Constraint + Weighted Soft Evidence 的参考；
- CampBoardGameHost：负责上述基础之上的 Storyteller Decision / Recommendation Layer。

本项目最重要的产品目标不是：

> 尽快降低玩家的不确定性。

而是：

> **维持 Productive Uncertainty：让信息持续推动推理，但不过早把合理世界、恶魔候选或叙事分支压缩到接近唯一答案。**

本版将该策略命名为：

> **Epistemic Pacing（认知节奏控制）**

---

## 2. ISES 论文复核结论

参考论文：

- Fandi Meng, Simon Lucas, *Deduction Game Framework and Information Set Entropy Search*, IEEE Conference on Games 2024；
- arXiv:2407.21178；
- DOI: 10.1109/COG60054.2024.10645614。

### 2.1 论文中可以直接借鉴的定义

论文把 Information Set 定义为：

```text
在当前决策点，与 Agent 已知信息一致的全部可能游戏状态集合。
```

这与本项目的 `PlayerWorldSet(P, t)` 几乎一一对应。

对于等概率可能状态，论文使用：

```text
H(S) = log |S|
```

描述当前 Information Set 的不确定性。

因此，本项目可以把第一版机械世界熵定义为：

```kotlin
WorldEntropy = log2(worldCount)
```

并定义：

```kotlin
InformationGain = entropyBefore - entropyAfter
EntropyRetention = entropyAfter / entropyBefore
```

其中：

- `InformationGain` 回答“这条信息压缩了多少不确定性”；
- `EntropyRetention` 回答“给完这条信息以后还保留了多少推理空间”。

### 2.2 ISES 的核心搜索思想

论文中的 ISES 对候选 action：

1. 从当前 Information Set 取可能状态；
2. 模拟 action 在不同状态下产生的 observation；
3. 用 observation 更新 Information Set；
4. 计算新的 entropy；
5. 取平均后选择 expected entropy 最低的 action。

论文还给出状态采样和动作采样版本，使搜索可以在固定计算时间内返回近似较优决策。

这对本项目有两个重要启发：

1. `PlayerWorldSet` 不仅可以证明 SAT / UNSAT，还可以成为策略评价的数学基础；
2. 将来可以对“未来事件 / 下一夜 / 下一次玩家选择”做有限深度前瞻，而不需要穷举完整游戏树。

### 2.3 ISES 不能直接照搬的地方

论文当前实验主要针对 single-player deduction game，典型模型具有：

- hidden secret；
- Agent 主动 query；
- Oracle 通常诚实；
- observation 基本确定；
- state transition 基本确定；
- 玩家目标通常是用尽量少的步骤把 Information Set 压缩到唯一答案。

Blood on the Clocktower 与其不同：

- 玩家可能撒谎；
- 邪恶阵营会主动制造错误叙事；
- Drunk / Poisoned 会得到不可靠信息；
- Spy / Recluse 可以产生特殊登记；
- 说书人不是被动 Oracle，而是主动决定某些合法信息如何展示；
- 游戏目标不是尽快让所有玩家得到唯一真相。

因此绝对禁止将：

```text
maximize InformationGain
```

直接作为自动说书人的总目标。

对于说书人，更合理的是：

```text
keep information gain inside a productive band
```

即“熵要下降，但下降速度和方向必须适合当前游戏阶段”。

### 2.4 一个重要实施边界：即时评价不需要照搬 ISES Expected Search

在 ISES 中，Agent 选择 query 后不知道 Oracle 会返回哪个 observation，因此需要：

```text
action
→ 多种 possible observations
→ expected entropy
```

但在自动说书人的许多决策点中，说书人实际就在选择要给出的完整信息候选，例如：

```text
Librarian → 给哪两个座位 + 哪个 Outsider
Investigator → 给哪两个座位 + 哪个 Minion
Drunk / Poisoned → 给哪个合法但不可靠结果
```

此时当前候选的 `afterWorlds` 可以直接求出：

```text
candidate
→ afterWorlds
→ entropyAfter
```

无需为了“即时候选评分”再对 observation 取期望。

ISES 的 expected-entropy sampling 应主要保留给未来：

- 玩家主动选择不同目标后的未来分支；
- 白天可能发生的不同死亡 / 处决；
- Poisoner / Monk / Imp 等行动造成的下一夜状态；
- “如果现在这样给，未来一至两轮还有多少可维护的高质量 Storyteller choices”。

这是本项目避免过度实现 ISES 的关键边界。

---

## 3. ISES 后续方向：CSP4SDG 对本项目的意义

2026 年 AAAI 论文：

- Kaijie Xu, Fandi Meng, Clark Verbrugge, Simon Mark Lucas；
- *CSP4SDG: Constraint and Information-Theory Based Role Identification in Social Deduction Games with LLM-Enhanced Inference*。

该工作已经从单人 deduction game 进入 Avalon、Mafia、Werewolf 一类 Social Deduction Game。

其架构中最值得本项目长期参考的是：

```text
Hard Constraints
→ 删除 mechanically impossible role assignments

Weighted Soft Constraints
→ 对剩余角色世界做相对评分

Information Gain
→ 衡量假设 / 信息的价值

Posterior / Belief State
→ 持续更新
```

该方向验证了：

> `Constraint Reasoning + Possible Worlds + Information Theory` 可以继续扩展到多人 Hidden-Role Social Deduction，而不是只适用于 Mastermind / Wordle 类型问题。

但当前 CampBoardGameHost 第一阶段仍不应该实现完整 probabilistic player model。

理由：

- 当前自动说书人真正急需解决的是 mechanically credible / epistemically credible；
- 玩家声明具有欺骗性，权重模型需要真实数据校准；
- 若过早把 soft claims 转成概率，会让系统看起来“数学化”，但实际只是隐藏主观权重。

因此确定：

```text
Phase 1: Hard Possible Worlds
Phase 2: Entropy / Structural Metrics
Phase 3: Storyteller Pacing
Phase 4（未来）: Weighted Soft Beliefs / Claims
```

---

## 4. `PlayerWorldSet` 正式定义

`PlayerWorldSet` 是本算法的一等领域对象，而不只是某次评价的临时结果。

```kotlin
interface PlayerWorldSet {
    val recipientSeat: Int
    val snapshotId: String

    fun isEmpty(): Boolean
    fun worldCount(): Long

    fun require(observation: EpistemicObservation): PlayerWorldSet
    fun exclude(observation: EpistemicObservation): PlayerWorldSet

    fun possibleRoles(seat: Int): Set<RoleId>
    fun possibleDemonSeats(): Set<Int>
    fun possibleMinionSeats(): Set<Int>

    fun roleWorldCount(seat: Int, role: RoleId): Long
    fun demonWorldCount(seat: Int): Long
    fun explanationClusters(): ExplanationClusterSummary
}
```

实现层可替换：

```text
PlayerWorldSet
├── EnumeratedWorldSet       // 正确性基线 / 调试
├── ZddPlayerWorldSet        // 移动端重点候选
└── SolverBackedWorldSet     // Oracle / 测试适配
```

Recommendation Engine 只能依赖 `PlayerWorldSet` 接口，禁止直接依赖 Clingo atom 或 ZDD node。

---

## 5. Entropy Metrics 设计

### 5.1 World Entropy

机械世界暂时等权时：

```text
H_world = log2(|W|)
```

用途：

- 测量整体不确定性规模；
- 测量候选造成的压缩程度；
- 比较同一个 snapshot 下多个候选。

禁止把它解释为真实玩家概率。

### 5.2 Information Gain

```text
IG_world = H_world(before) - H_world(after)
```

意义：这条信息提供了多少机械可区分信息。

### 5.3 Entropy Retention

```text
ER_world = H_world(after) / H_world(before)
```

当 `H_before > 0` 时使用。

意义：候选给出后还保留多少推理空间。

### 5.4 Demon Structural Entropy

只看 `worldCount` 不够。

假设候选前后世界数量都很大，但几乎所有剩余世界都把同一座位视为 Demon，那么游戏实际上已经非常接近 Hard Solve。

因此需要单独统计：

```text
mechanicalShareDemon(seat)
= worldsWhereSeatIsDemon / totalWorlds
```

然后计算结构性 Demon entropy：

```text
H_demon = -Σ q_i log2(q_i)
```

其中 `q_i` 是机械世界中的 Demon seat share。

注意：

> `q_i` 是 structural share，不是“玩家真实认为该座位是恶魔的概率”。

代码和 UI 均不得把该指标命名为 `demonProbability`。

建议命名：

```kotlin
MechanicalDemonDistribution
DemonStructuralEntropy
```

### 5.5 Evil / Role Structural Entropy

同理可以计算：

```text
H_evil
H_role(seat)
H_setup
```

第一阶段不需要对所有维度都计算；优先：

1. `H_world`
2. `H_demon`
3. possible Demon seat count
4. possible Minion seat count
5. setup profile count

### 5.6 Narrative Branch Entropy

后期可以将剩余世界按“解释类型”聚类，例如：

```text
TRUE_INFO
DRUNK_EXPLANATION
POISONED_EXPLANATION
SPY_REGISTRATION
RECLUSE_REGISTRATION
BARON_SETUP
STARPASS_PATH
```

得到：

```text
ExplanationClusterSummary
```

然后评价：

- 是否仍有多个具有实际规模的解释分支；
- 是否所有世界虽然数量很多，却只是同一个叙事解释的座位排列变化；
- 是否只剩一个极端脆弱解释。

该指标比纯 world count 更接近玩家实际讨论价值。

第一版仅生成 cluster count / dominant cluster share，不要求直接计算 Shannon entropy。

---

## 6. Epistemic Pacing：自动说书人的核心信息论策略

### 6.1 与 ISES 的目标区别

ISES：

```text
目标：尽快降低 entropy
```

CampBoardGameHost：

```text
目标：让 entropy 以适合当前局面的速度下降
```

因此定义：

```text
Too Little Information
        ↓
候选没有推动讨论

Productive Band
        ↓
有方向、有讨论、有多个合理世界

Too Much Information
        ↓
Hard Solve / Confirmation Lock / Malfunction Exposure
```

### 6.2 不采用固定全局 Target Entropy

不能写成：

```text
所有 Night 1 保留 70% entropy
所有 Night 2 保留 50%
```

原因：

- 不同角色天然信息强度不同；
- 不同玩家人数世界规模不同；
- 不同实际局面可用解释数量差异巨大；
- 有些合法真信息本身就非常强。

正确方式是：

1. 收集已有候选的 entropy metrics；
2. 对专家认为“好 / 太弱 / 太强”的固定场景做标注；
3. 根据 role family + game phase + player count 校准合理 band；
4. 先用宽区间，仅对明显异常候选产生 hard penalty；
5. 后续再缩小区间。

### 6.3 Pacing Score

第一版建议不用复杂机器学习：

```kotlin
data class EntropyMetrics(
    val worldEntropyBefore: Double,
    val worldEntropyAfter: Double,
    val worldInformationGain: Double,
    val worldEntropyRetention: Double,
    val demonEntropyBefore: Double,
    val demonEntropyAfter: Double,
    val possibleDemonSeatsBefore: Int,
    val possibleDemonSeatsAfter: Int,
    val possibleMinionSeatsAfter: Int,
    val explanationClusterCountAfter: Int,
    val dominantExplanationShare: Double?,
)
```

然后：

```text
pacingScore
= informationUsefulness
- overCompressionPenalty
- demonCollapsePenalty
- narrativeCollapsePenalty
```

其中严重问题仍然使用独立 gate，而不是依赖总分抵消。

### 6.4 三种风格与 Pacing Band

稳健 / 平衡 / 激进模式仍保留，但不再通过完全不同的候选池实现。

所有模式共享：

```text
Official Legality
Epistemic Consistency
Hard Quality Gates
```

只调整 Productive Band：

```text
稳健：偏高 Entropy Retention
平衡：中间区域
激进：允许更大的 Information Gain，但仍禁止 Hard Solve / 自证失能
```

风格不能绕过：

- `afterWorlds == empty`；
- `NEAR_CERTAIN malfunction exposure`；
- 明显 single-demon lock；
- 规则不合法。

---

## 7. 推荐评分重构

现有经验评分保留，但逐步从“人工猜测一个数”升级成“World Metrics + Policy Weight”。

### 7.1 InformationScore

旧：

```text
按角色、真假、目标类型人工加减
```

新：

```text
主要来自 IG_world
辅以角色能力的固有解释
```

但 `IG_world` 不是越大越好，而是用于计算 Pacing Band 适配度。

### 7.2 AmbiguityScore

来源：

- `H_demon_after`；
- possible Demon seats；
- possible Minion seats；
- explanation cluster count；
- dominant explanation share。

### 7.3 HardSolvePenalty

触发特征包括：

- possible Demon seats 接近 1；
- `H_demon` 急剧塌缩；
- 某一 Demon seat 的 structural share 极端集中；
- 多条线索联合后只剩同一个邪恶故事。

### 7.4 MalfunctionExposure

比较两种假设：

```text
W_functioning
W_mayMalfunction
```

若：

```text
W_functioning = 0
```

则直接 `INELIGIBLE`。

若 functioning worlds 极少且高度脆弱，而 malfunction worlds 规模和解释明显更自然，则提高 exposure level。

### 7.5 ConfirmationChainPenalty

不只统计“同一玩家被指向几次”。

应观察：

```text
连续 observations
→ PlayerWorldSet 联合过滤
→ Demon / Evil / Bluff story 是否发生非线性塌缩
```

即两条单独看都合理的信息，如果联合后几乎锁死同一恶魔伪装，也应被识别。

### 7.6 EvilBluffSupportScore

该指标仍然从 Actual World / Storyteller View 计算。

禁止把恶魔伪装身份、真实恶魔身份等秘密事实加入接收者的 `PlayerWorldSet`。

架构必须保持：

```text
Player Epistemic Metrics
            +
Actual-world Narrative Metrics
            ↓
Recommendation Policy
```

而不是污染玩家认知事实。

---

## 8. ISES-style Forward Search 的正确定位

### 8.1 第一阶段不实现多步搜索

在 `PlayerWorldSet`、熵指标、Pacing Score 尚未经过专家标注验证前，禁止实现复杂前瞻搜索。

否则会出现：

```text
底层世界模型略错
×
熵指标未校准
×
未来分支假设错误
=
一个非常复杂但难以解释的错误推荐器
```

### 8.2 后期只做有限深度 Storyteller Search

第一版 forward search 建议限制：

```text
Depth = 1 future decision
或
Depth = 2 storyteller-relevant transitions
```

不构建完整 Game Tree。

可评价：

```text
当前 candidate
→ 一组合理 future events / choices
→ 下一次 Storyteller decision 的可用高质量候选数量
```

定义：

```kotlin
FutureStorySpace(
    feasibleBranchCount,
    healthyBranchCount,
    expectedFutureEntropyBandScore,
    worstCaseMalfunctionExposure,
    futureHardSolveRisk,
)
```

### 8.3 何时使用 sampling

当未来分支过多时吸收 ISES sampling 思想：

- sample possible world states；
- sample future player choices；
- sample plausible day outcomes；
- 设置严格 time budget；
- 时间到立即返回当前最佳近似。

但必须：

- 固定随机种子以保证 replay；
- 记录 sample count；
- 记录 search budget；
- 不允许 sampling failure 导致 `UNSAT` 误判。

精确合法性和认知一致性仍由 Hard World Engine 保证；sampling 只用于未来质量估计。

---

## 9. 清晰实施路线（本版为准）

> 本节覆盖 v1.1 第 16 节与第 24 节的实施排序。  
> 原章节保留为设计演进记录；后续 Codex 开发以本节为最终路线。

整体分为四个阶段：

```text
Phase A  正确的世界
Phase B  正确的玩家认知
Phase C  正确的信息节奏
Phase D  有限未来前瞻
```

---

### Phase A：Formal Rules + World Engine Foundation

#### PR A0：Reference Freeze & Evaluation

任务：

- 固定 `botc-asp`、`olarozenfeld/botc`、`botc-zdd-` 的研究 commit；
- 记录 ISES 论文和 CSP4SDG 论文版本；
- 建立 reference matrix；
- 检查外部项目许可证与可复用范围；
- 建立 20+ Trouble Brewing golden scenarios；
- 明确每个场景由哪个外部项目可以验证。

产物：

```text
docs/external_solver_evaluation.md
docs/epistemic_reference_matrix.md
```

退出条件：

- 外部项目定位明确；
- 不存在“把某个外部实现直接当官方规则”的误解；
- 关键边界案例都有独立验证来源。

#### PR A1：Unified Semantic Model

新增：

```text
FormalGameState
InformationProposition
EpistemicObservation
StorytellerDecisionPoint
LegalChoiceSet
PlayerKnowledgeSnapshot
```

要求：

- UI 字符串与规则语义分离；
- stable ID / serialization 完成；
- 生产选择结果不改变。

#### PR A2：ASP Oracle Cross-validation Harness

目标：测试环境中可以把本项目场景转成 ASP Oracle 输入并比较：

```text
SAT / UNSAT
legal outputs
registration behavior
```

优先覆盖：

```text
WW / Librarian / Investigator
Chef / Empath / FT
Drunk / Poisoner
Spy / Recluse
Baron
```

Oracle 不进入正式 Android gameplay dependency。

#### PR A3：EnumeratedWorldSet Baseline

实现简单透明的 baseline：

- setup distribution；
- seat assignment；
- basic registration；
- Player Perspective filtering；
- exact SAT / UNSAT；
- limited enumeration；
- world count。

要求：

- 与 ASP Oracle golden scenarios 一致；
- 代码以正确和可调试为第一目标，不追求最终性能。

#### PR A4：ZddPlayerWorldSet Prototype

实现或适配 Decision Diagram Runtime：

- `require`；
- `exclude`；
- `count`；
- `possibleRoles`；
- `possibleDemonSeats`；
- `possibleValues`；
- snapshot / undo。

三方验证：

```text
ASP Oracle
vs EnumeratedWorldSet
vs ZddPlayerWorldSet
```

真实 Android 设备测：

- P50 / P95；
- peak memory；
- node count；
- first build；
- incremental observation；
- snapshot / restore。

Gate：只有 ZDD 明显值得时才作为生产实现；推荐层必须允许继续使用其他 World Engine。

---

### Phase B：Player Epistemic Correctness

#### PR B1：PlayerWorldSet Domain Integration

将 `PlayerWorldSet` 接入正式领域层。

建立：

```text
beforeWorlds
→ candidate
→ afterWorlds
```

但只 shadow log，不改变推荐结果。

记录：

- before / after count；
- possible Demon seats；
- setup profile；
- contradiction reason。

#### PR B2：First-night Epistemic Gate

接入：

```text
WW
Librarian
Investigator
Chef
Empath
```

规则：

```text
before SAT && after UNSAT
→ INELIGIBLE
```

典型案例必须自然通过统一求解得到结论：

```text
8 人 Drunk-Librarian + No Outsiders
8 人 Drunk-Investigator + No Minions
Spy / Recluse registration
Baron setup modification
```

禁止针对这些案例写角色 + 人数硬编码。

#### PR B3：Drunk Perceived Role Joint Planning

将：

```text
DrunkShownRole
```

升级为：

```text
DrunkPerceivedRolePlan
= perceived role
+ initial info
+ epistemic result
+ future maintainability placeholder
```

只有存在至少一个健康可信信息路径的 perceived role 才可进入自动设置池。

#### PR B4：Multi-night PlayerWorldSet

加入：

- Day / Night timeline；
- death；
- changing neighbors；
- Poisoner retarget；
- Monk protection；
- Imp kill / starpass；
- Empath N2；
- FT N2；
- Undertaker；
- Ravenkeeper。

要求：

- snapshot based replay；
- undo；
- 不用当前状态错误回溯解释过去；
- 多夜联合 UNSAT 精确。

Phase B 完成时，系统已经解决“玩家是否还能相信自己正常工作”这个原始问题，即使尚未使用 entropy 排序。

---

### Phase C：Entropy Metrics + Epistemic Pacing

#### PR C1：Entropy Metrics Shadow Mode

新增只读：

```text
H_world
IG_world
ER_world
H_demon
possible Demon seats
possible Minion seats
explanation cluster count
```

要求：

- 不改变任何推荐；
- 固定 seed 回放所有历史测试；
- 输出 candidate metric comparison report。

目标：先验证指标是否与熟练说书人的直觉相关。

#### PR C2：Expert-labelled Pacing Dataset

建立小型内部 golden dataset。

每个场景由人工标注：

```text
TOO_WEAK
PRODUCTIVE
TOO_STRONG
MALFUNCTION_EXPOSING
HARD_SOLVE
NARRATIVE_LOCK
```

并记录：

- role family；
- player count；
- phase；
- before metrics；
- after metrics；
- 人类理由。

第一轮不需要大量数据，重点是覆盖边界场景。

#### PR C3：Pacing Policy v1

使用规则化 band，不使用 ML。

```text
Official Legality Gate
↓
Epistemic Consistency Gate
↓
Malfunction Exposure Gate
↓
Pacing Band Score
↓
Narrative Risk
↓
Style Score
```

三个 style 仅调整 productive band 和软权重。

Gate 不随 style 改变。

#### PR C4：Replace Heuristic Information Metrics

逐步替换：

```text
InformationScore
AmbiguityScore
HardSolvePenalty
ConfirmationChainPenalty
```

为 WorldSet-derived feature。

不是一次删除全部旧分数，而是：

1. shadow compare；
2. feature-by-feature 替换；
3. fixed-seed diff；
4. 专家 review；
5. 才删除旧路径。

#### PR C5：Unified Selector Production Rollout

自动模式统一：

```text
LEGAL
→ ELIGIBLE
→ highest QualityTier
→ current Style policy
→ score tolerance
→ stable weighted random
```

确保：

- `INELIGIBLE` 命中率 0；
- `MANUAL_ONLY` 自动命中率 0；
- 平衡模式不跨风格二次随机；
- 同一输入可完全 replay。

Phase C 完成后，系统进入第一版正式“认知节奏控制”。

---

### Phase D：ISES-style Limited Forward Search

> Phase D 明确不是第一轮实施依赖。只有 Phase C 稳定后再开始。

#### PR D1：FutureStorySpace Model

定义：

```kotlin
data class FutureStorySpace(
    val sampledBranchCount: Int,
    val healthyBranchCount: Int,
    val futurePacingScore: Double,
    val futureHardSolveRisk: Double,
    val futureMalfunctionExposure: MalfunctionExposure,
)
```

先只做 deterministic / small branch scenarios。

#### PR D2：Depth-1 Future Search

评价：

```text
现在给 candidate A
→ 下一次 Storyteller decision
→ 还有多少高质量选择？
```

主要用于：

- Drunk perceived role；
- persistent info roles；
- 很容易在下一夜自证失能的候选。

#### PR D3：Budgeted Sampling Search

只在 branch explosion 时启用。

参考 ISES：

```text
sample worlds
sample future actions / outcomes
fixed computation budget
```

要求：

- deterministic seed；
- anytime return；
- exact legality 与 UNSAT 不使用 sampling；
- sampling 只影响 soft future score。

#### PR D4：Depth-2 Experimental Evaluation

仅实验，不默认生产打开。

比较：

```text
No Search
Depth 1
Depth 2
```

观察：

- 推荐质量提升是否真实存在；
- 性能成本；
- 解释复杂度；
- 是否产生过度优化 / 不自然说书模式。

没有显著收益则停止在 Depth 1。

---

## 10. Phase E（未来）：Soft Evidence / Social Deduction Belief Model

该阶段不是当前一致性算法实施范围，仅预留架构。

参考 CSP4SDG：

```text
Hard Facts
→ prune impossible worlds

Soft Claims / Dialogue / Behavior
→ weight surviving worlds
```

可能数据：

- 公开 role claim；
- whisper audience；
- claim change；
- nomination / vote；
- 信息传播关系。

必须遵守：

- 玩家声明永远不能默认成为 hard truth；
- weights 必须有数据校准依据；
- LLM 可以把自然语言转换为 structured evidence，但不能直接决定规则合法性；
- weighted belief layer 不得破坏底层 exact `PlayerWorldSet`。

建议未来结构：

```text
ExactPlayerWorldSet
        ↓
WeightedBeliefOverlay
        ↓
Posterior-like structural belief
        ↓
Recommendation Layer
```

---

## 11. 测试与验收

### 11.1 Correctness Gate

必须满足：

- 核心 Trouble Brewing golden scenarios 与 ASP Oracle 无未解释差异；
- `EnumeratedWorldSet` 与生产 World Engine 在测试范围内一致；
- exact UNSAT 不依赖 enumeration limit 或 sampling；
- Spy / Recluse / Drunk / Poisoned / FT red herring 等边界有永久回归测试。

### 11.2 Epistemic Gate

必须满足：

```text
afterWorlds == empty
→ 自动命中次数 0
```

以及：

```text
NEAR_CERTAIN malfunction exposure
→ 自动命中次数 0
```

### 11.3 Entropy Metric Gate

测试：

- world count 1 → `H_world = 0`；
- identical before/after → `IG = 0`；
- after 是 before 子集时 `H_after <= H_before`；
- demon structural shares sum to 1；
- candidate replay 得到完全一致 metrics；
- UI / log 不把 structural share 标记为真实 probability。

### 11.4 Pacing Gate

通过 expert-labelled dataset 验证：

- 明显无信息候选不会因“安全”而总是排名最高；
- 强但仍健康的信息可以在激进模式提高权重；
- 明显 hard solve 在所有 style 下仍被强惩罚 / gate；
- productive candidates 在平衡模式稳定进入最高质量区间。

### 11.5 Forward Search Gate

- Depth 1 在相同 seed / budget 下完全可复现；
- sampling 不改变 legality；
- sampling 不产生 false UNSAT；
- time budget 达到时可以返回当前最佳结果；
- 若 forward search 没有显著提升专家评分，则不进入默认生产路径。

---

## 12. 性能策略

### 12.1 Runtime 第一原则

UI 夜间操作不能等待一个“理论上最优”的搜索。

优先顺序：

```text
规则正确
> 认知正确
> 实时响应
> 信息节奏优化
> 多步搜索
```

### 12.2 Cache Key

建议：

```text
WorldSetCacheKey = hash(
    rulesVersion,
    script,
    playerCount,
    perspectiveSeat,
    timelineSnapshot,
    hypothesisMode
)
```

Candidate simulation 只追加单个 proposition 时优先增量过滤。

### 12.3 Exact 与 Approximate 分离

Exact：

- legality；
- SAT / UNSAT；
- candidate 是否存在 functioning world。

Approximate / budgeted：

- 大规模 world count；
- future branch expected score；
- Depth 1 / Depth 2 sampling；
- future feasibility。

禁止用 approximate layer 证明“没有合理世界”。

---

## 13. 可解释性要求

每个 Candidate 最终至少能输出：

```text
1. Rule legality
2. Functioning-world existence
3. World entropy before / after
4. Information gain
5. Demon structural diversity before / after
6. Malfunction exposure
7. Narrative / bluff consequence
8. Style pacing score
9. Quality tier
10. Final selection reason
```

示例解释格式：

```text
候选 B：ELIGIBLE / STRONG
- 正常能力世界仍存在
- 合理世界从 384 降到 96
- 信息量中等
- 恶魔候选仍有 4 个座位
- 无明显自证醉酒风险
- 保留 3 类主要解释分支
- 轻度支持恶魔伪装，但未形成确认锁
- 符合 Balanced productive band
```

开发日志可以显示数字；最终 UI 可只显示简化自然语言。

---

## 14. 最终架构原则

1. **Official Legality 与 Recommendation Quality 永远分层。**
2. **规则层回答“能不能”，推荐层回答“该不该”。**
3. **`PlayerWorldSet` 是玩家认知的一等领域对象。**
4. **候选必须先通过 `beforeWorlds → afterWorlds` 模拟，再评分。**
5. **正常能力世界归零的候选自动失去资格。**
6. **Entropy 是测量工具，不是说书人的唯一目标函数。**
7. **说书人优化的是 Productive Uncertainty / Epistemic Pacing，而不是最大 Information Gain。**
8. **World count 与 structural share 不是玩家真实概率。**
9. **Demon entropy 比单纯 world entropy 更能识别 Hard Solve。**
10. **叙事分支多样性必须与机械世界数量分开观察。**
11. **严重认知风险使用 gate，不允许被其他加分抵消。**
12. **Style 只调整 Productive Band，不改变 legality / epistemic gates。**
13. **ASP 优先作为开发期 Oracle，而不是移动端强依赖。**
14. **ZDD 是重点 Runtime 候选，但必须通过真实设备验证后再确定。**
15. **Recommendation Engine 不能依赖具体 World Engine 数据结构。**
16. **ISES sampling 只用于未来质量估计，不用于 exact legality / UNSAT。**
17. **Forward Search 在单步指标稳定前禁止实施。**
18. **玩家声明和对话未来只能先作为 Soft Evidence。**
19. **LLM 可以解释 / 结构化信息，但不能绕过 Formal Rules。**
20. **本项目的核心差异化是 Storyteller Decision / Information Pacing，而不是重新实现一个通用 BotC solver。**

---

## 15. 实施总览

```text
A0 Reference Freeze
 ↓
A1 Semantic Model
 ↓
A2 ASP Oracle Harness
 ↓
A3 Enumerated World Baseline
 ↓
A4 ZDD Runtime Prototype
 ↓
B1 PlayerWorldSet Integration
 ↓
B2 First-night Epistemic Gate
 ↓
B3 Drunk Joint Planning
 ↓
B4 Multi-night WorldSet
 ↓
C1 Entropy Metrics Shadow Mode
 ↓
C2 Expert Pacing Dataset
 ↓
C3 Pacing Policy v1
 ↓
C4 Replace Heuristic Metrics
 ↓
C5 Unified Selector Rollout
 ───────────── 第一版正式目标到此 ─────────────
 ↓
D1 FutureStorySpace
 ↓
D2 Depth-1 Search
 ↓
D3 Budgeted Sampling
 ↓
D4 Depth-2 Experiment
 ───────────── 可选高级功能 ─────────────
 ↓
E Soft Evidence / Weighted Belief（未来）
```

### 第一版正式完成定义

本次算法改进的“完成”建议定义在 **Phase C5**，而不是要求完成 ISES-style Forward Search。

即第一版正式目标是：

> **已经有准确的 PlayerWorldSet；能够排除自证异常候选；能够用 WorldSet / Entropy 指标控制信息节奏；能够在不同说书风格下从高质量候选中稳定选择。**

Depth-1 / Depth-2 ISES-style search 属于后续增益项，不应阻塞第一版上线。
