# CampBoardGameHost 自动说书人玩家认知一致性算法改进方案 v2.1

> 版本：2.1  
> 日期：2026-08-11  
> 状态：实施前主设计修订稿  
> 基础文档：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2.md` v2.0  
> 本版目的：在 v2.0 的 Formal Rules / PlayerWorldSet / Entropy / Epistemic Pacing 架构上，正式加入 **Narrative Fairness / Earned Advantage** 与 **运行时可变 Storyteller Policy / Assisted Selection**。  
> 覆盖关系：若本文件与 v2.0 在推荐策略、自动/手动模式或实施顺序上冲突，以 v2.1 为准；v2.0 的 World Engine、ISES、Entropy、Cross-validation 等技术定义继续有效。

---

## 1. 本次修订的核心结论

经过进一步讨论，自动说书人的“好线索”不能只看信息量和平衡，还必须区分三个层级：

```text
Level 1 — Must Be Valid
规则合法、能力语义正确、玩家视角存在合理解释

Level 2 — Mechanically Good
不是毫无用处、不过强、不直接锁死、结构上保持适度平衡

Level 3 — Human-story Appropriate
尊重玩家通过发言、伪装、配合和判断自己挣得的优势，避免粗暴破坏现场已经形成的优秀故事
```

其中：

- **Level 1**：应由 Formal Rules + PlayerWorldSet 精确保证；
- **Level 2**：应由 Entropy / Epistemic Pacing / Structural Balance / Narrative Risk 自动评价；
- **Level 3**：当前版本只做“可计算风险提示 + 说书人判断”，不得假装算法已经理解现场玩家表现。

因此，系统正式采用两个互补输出层：

```text
Mechanical Recommendation
        +
Storyteller Judgment Hints
```

同时，自动/手动与稳健/平衡/激进不再属于“开局后冻结的 Setup”，而属于 **运行时可变的 Storyteller Policy**。

说书人可以在游戏中随时切换：

```text
AUTO + CONSERVATIVE
AUTO + BALANCED
AUTO + AGGRESSIVE
ASSISTED + 任意风格
```

已经执行的游戏事实和已发送线索不会被重新计算；新策略只影响尚未提交的当前决策和未来决策。

---

## 2. “好线索”的正式质量模型

### 2.1 Level 1：规则与认知硬门槛

候选首先必须满足：

1. 官方规则允许；
2. 信息格式和角色能力语义成立；
3. 对需要“正常能力可信”的自动候选，`afterWorlds != empty`；
4. 不出现近乎必然的能力失效自证；
5. 不依赖 sampling 或软分数证明 legality / SAT / UNSAT。

对应现有架构：

```text
Formal Rule Oracle
        ↓
Legal Choice Layer
        ↓
PlayerWorldSet
        ↓
Epistemic Gate
```

这部分不允许 Storyteller style 绕过。

### 2.2 Level 2：机械意义上的好线索

在所有 Level 1 合格候选中评价：

- `InformationUsefulness`：不能完全没有任何实际作用；
- `EpistemicPacing`：信息量处于合适区间；
- `HardSolveRisk`：不能过早锁定 Demon / Evil；
- `Ambiguity`：保留多个有意义的解释；
- `StructuralBalance`：避免系统自身造成明显单边压制；
- `ConfirmationChainRisk`：避免多条线索联合形成非自然锁死；
- `MalfunctionExposure`：避免明显告诉玩家“你醉了 / 你被毒了”；
- `BluffSupport`：允许合理帮助邪恶叙事，但不能形成不自然的机械认证；
- `HistoricalPressure`：避免持续把信息压力集中到同一玩家。

这一层是第一版自动推荐的主要优化范围。

### 2.3 Level 3：现场叙事适配

典型问题：

> 邪恶方通过优秀发言、身份构造、互相配合和持续一致的故事取得了优势。此时，即使好人机械上落后，说书人通常也不应该突然给出一条极强、几乎直接推翻该故事的线索。

这类判断依赖：

- 谁声称了什么；
- 谁支持谁；
- 谁怀疑谁；
- 声明是否持续一致；
- 哪个故事由谁主动建立；
- 玩家为什么相信；
- 当前优势来自 Setup / 随机性还是来自实际玩家表现。

当前 App 并不可靠掌握这些信息，因此：

> **Level 3 目前不得直接变成自动评分中的“玩家表现分”。**

算法可以提示“这条线索可能高度破坏当前故事”，但最终是否应避免，由现场说书人决定。

---

## 3. Narrative Fairness 与 Earned Advantage

### 3.1 核心原则

正式加入以下设计原则：

> **Correct structural imbalance, but do not automatically erase player-earned advantage.**

中文：

> **可以修正系统造成的结构性失衡，但不应自动抹掉玩家通过优秀表现赢得的优势。**

### 3.2 为什么 `BalanceScore` 不能等于“把胜率拉回 50:50”

必须区分：

```text
Structural Imbalance
≠
Current Winning Position
```

例如同样表现为邪恶方当前占优：

#### 场景 A：结构性优势

```text
首夜多个信息天然偏向邪恶
+ 关键好人被连续中毒
+ 随机事件继续偏向邪恶
```

这是较适合由说书人通过后续信息节奏适度修正的情况。

#### 场景 B：玩家挣来的优势

```text
恶魔伪装选择优秀
+ 爪牙配合自然
+ 多轮公开发言前后一致
+ 好人因为社交判断而主动相信错误故事
```

即使机械局势同样落后，也不应简单执行：

```text
evil ahead
→ give stronger good clue
```

否则系统会惩罚成功的玩家行为。

### 3.3 自动算法当前能做什么

当前机械层可以识别：

- 一条新线索是否直接否定某个已有机械可支持的 bluff story；
- 是否让某个 Demon bluff 从“有多个解释”突然变为机械上极难成立；
- 是否与此前线索形成强确认 / 强反确认；
- 是否造成 explanation cluster 的突然塌缩；
- 是否从多个 plausible evil stories 压缩为几乎唯一故事。

这些都属于可计算的 **Story Disruption Risk**。

### 3.4 自动算法当前不能可靠做什么

当前不得自动断言：

```text
“邪恶方这一局玩得很好”
“这个优势是他们自己挣来的”
“好人是因为发言而不是机械信息被骗”
```

这些结论需要现场软信息和玩家行为理解。

在 Phase E 的 Soft Evidence / Social Deduction Belief Model 尚未成熟前，算法只能：

```text
计算 Story Disruption
+
提示说书人结合现场判断
```

而不能：

```text
估算 PlayerSkillScore
→ 自动修改候选权重
```

---

## 4. `StoryDisruptionRisk` 设计

### 4.1 定位

`StoryDisruptionRisk` 是 **Narrative Metric**，不是 legality gate，也不默认是负分。

```kotlin
enum class StoryDisruptionRisk {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME,
}
```

建议同时保存原因：

```kotlin
data class StoryDisruptionEvaluation(
    val risk: StoryDisruptionRisk,
    val bluffStoryCollapse: Boolean,
    val demonStoryCollapse: Boolean,
    val explanationClusterDrop: Int,
    val affectedSeats: Set<Int>,
    val reasonCodes: List<String>,
)
```

### 4.2 可计算特征

第一版可以使用：

- candidate 前后 `explanationClusterCount`；
- dominant explanation share 的变化；
- `H_demon` 的突然下降；
- 某 Demon bluff 是否由“机械上有解释”变为几乎无法维持；
- 多条 observation 联合后是否突然反证同一故事；
- candidate 是否直接针对真实 Demon / bluff role 形成高强度交叉确认。

### 4.3 不直接解释为“坏线索”

`HIGH StoryDisruption` 可能代表：

```text
A. 很好的结构修正
```

也可能代表：

```text
B. 粗暴抹掉玩家自己打出来的优秀故事
```

因此第一版处理：

```text
AUTO：作为 Narrative Risk 的软特征，只有在同时出现 HardSolve / structural anomaly 等机械证据时才显著影响自动排序。

ASSISTED：明确展示给说书人，让人结合现场判断。
```

不得仅因为 `StoryDisruption == HIGH` 就自动 `INELIGIBLE`。

### 4.4 Assisted 模式建议提示

例如：

```text
候选 A
信息强度：中高
认知一致性：高
结构平衡：改善好人劣势
Story Disruption：高

提示：该线索可能明显削弱当前邪恶身份故事。
如果该故事主要来自邪恶玩家自己的优秀发言与配合，可考虑保留其已获得的优势并选择较温和候选。
```

系统表达必须保持条件式，不声称已经知道现场真实原因。

---

## 5. Structural Balance 的重新定义

### 5.1 自动说书人不追求动态 50:50

正式禁止以下简单逻辑：

```text
teamAhead(EVIL)
→ boost(GOOD)
```

自动平衡应主要针对：

- Setup 结构造成的异常强弱；
- 说书人此前信息选择导致的累积失衡；
- 多条自动线索造成的单边机械确认；
- 能力失效 / 登记组合造成的异常不可解状态；
- 系统自身历史选择产生的信息压力集中。

### 5.2 新的概念分层

建议：

```kotlin
data class BalanceEvaluation(
    val structuralBalanceRisk: Int,
    val informationPressureImbalance: Int,
    val confirmationImbalance: Int,
    val currentPositionEstimate: Int?,
)
```

其中：

- `structuralBalanceRisk` 可以进入自动评分；
- `currentPositionEstimate` 即使未来存在，也不得单独驱动“帮助落后方”；
- “该优势是否由玩家挣得”在当前版本保持人工判断。

### 5.3 与 Epistemic Pacing 的关系

Pacing 目标仍然是：

> 给足够的信息推动游戏，但保留足够的不确定性让玩家继续推理。

Balance 负责避免系统造成的不公平；Pacing 负责控制信息速度；Narrative Fairness 负责提醒人尊重玩家自己的故事建设。

三者不能合并成一个不可解释的总分。

---

## 6. 运行时可变 `StorytellerPolicyState`

### 6.1 核心架构边界

必须分开：

```text
GameSetupState
GameRuntimeState
StorytellerPolicyState
```

#### `GameSetupState`

开局后基本冻结：

- script；
- player seats；
- actual roles；
- Drunk actual / perceived role；
- Demon bluffs；
- FT red herring；
- Setup modifiers；
- 其他必须在开局阶段确定的事实。

#### `GameRuntimeState`

随游戏合法演进：

- day / night / round；
- alive / dead；
- poison / protection；
- current Demon；
- starpass；
- nominations / executions（如有记录）；
- 已执行的能力和历史 observation。

#### `StorytellerPolicyState`

说书人可以随时修改：

```kotlin
data class StorytellerPolicyState(
    val selectionMode: SelectionMode,
    val style: RecommendationStyle,
    val recommendationCount: Int = 3,
    val showAdvancedHints: Boolean = true,
    val revision: Long,
)

enum class SelectionMode {
    AUTO,
    ASSISTED,
}

enum class RecommendationStyle {
    CONSERVATIVE,
    BALANCED,
    AGGRESSIVE,
}
```

UI 可继续使用更容易理解的中文：

```text
自动选择
手动选择（显示推荐）
```

底层建议命名为 `AUTO / ASSISTED`，避免把手动模式误解成“关闭推荐算法”。

### 6.2 为什么 Policy 可以运行时修改

这些设置只决定：

- 候选如何排序；
- productive band 偏稳健还是偏激进；
- 是否由系统自动提交最终候选；
- UI 显示多少建议和高级提示。

它们不改变：

- 真实身份；
- 已发生事件；
- 已经告诉玩家的信息；
- Formal Rules；
- PlayerWorldSet 的历史事实。

因此没有理由在游戏开始后锁死。

---

## 7. Policy Change 的精确生效规则

### 7.1 已经执行的决定永久冻结

例如：

```text
Night 1 Empath
policy = BALANCED
result = 1
```

之后改成：

```text
AUTO + AGGRESSIVE
```

不能重新生成或重新解释 Night 1 的结果。

### 7.2 当前尚未提交的 Decision 可以重新排序

若说书人正在查看当前候选，但尚未向玩家提交：

```text
Decision status = PENDING
```

此时修改 style / mode 后，可以：

```text
相同 FormalGameState
+ 相同 PlayerWorldSet
+ 新 StorytellerPolicyState
→ 重新评价 / 排序候选
```

这只是重新跑 Recommendation Policy，不改变世界状态。

### 7.3 已提交后从下一 Decision 生效

若：

```text
Decision status = COMMITTED
```

则新 Policy 从下一个决策点开始生效。

### 7.4 每个决定保存 Policy Snapshot

```kotlin
data class DecisionAuditRecord(
    val decisionId: String,
    val policyRevision: Long,
    val selectionMode: SelectionMode,
    val style: RecommendationStyle,
    val candidateId: String,
    val selectionSource: SelectionSource,
    val committedAtPhase: StorytellerPhase,
)

enum class SelectionSource {
    AUTO_SELECTOR,
    STORYTELLER_MANUAL,
}
```

这样赛后可以准确回答：

> “为什么 Night 2 这条信息比 Night 1 更激进？”

因为日志明确知道当时 Policy 已经改变，而不是重放时使用当前最新设置。

---

## 8. AUTO 与 ASSISTED 的统一 Recommendation Pipeline

### 8.1 两种模式共享完整算法

正式禁止：

```text
Manual mode
→ skip recommendation engine
```

两种模式都执行：

```text
Legal Candidate Generation
→ PlayerWorldSet Simulation
→ Epistemic Gates
→ Entropy / Pacing
→ Narrative / StoryDisruption
→ Style Policy
→ Ranked Candidates
```

差别只发生在最后一步。

### 8.2 AUTO

```text
ranked candidates
→ highest eligible quality pool
→ tolerance
→ stable weighted select
→ system commits selected candidate
```

仍需满足所有 hard gates。

### 8.3 ASSISTED

```text
ranked candidates
→ Top N + explanation + warnings
→ storyteller chooses
→ commit selected candidate
```

推荐显示至少包括：

- 推荐等级；
- 信息强度；
- Pacing；
- HardSolve risk；
- Malfunction exposure；
- Structural Balance；
- Story Disruption；
- 主要推荐理由。

### 8.4 `MANUAL_ONLY` 与 `INELIGIBLE`

建议：

- `ELIGIBLE`：AUTO / ASSISTED 都正常显示；
- `MANUAL_ONLY`：ASSISTED 可以显示，但必须带明显警告，AUTO 永远不选；
- `INELIGIBLE`：默认不作为推荐显示；开发/诊断模式可以查看淘汰原因。

这延续现有 AutomaticEligibility 设计，不让人工模式绕过规则合法性。

---

## 9. 运行时 UI 修改要求

### 9.1 不再只允许从“开局设置页”修改

当前问题：

```text
游戏开始前选择 Manual / Auto
Auto 再选择 Balanced / Aggressive 等
↓
进入游戏后无法修改
```

修改后：这些选项必须从正常游戏界面随时访问。

### 9.2 推荐入口

主界面长期显示一个轻量状态入口，例如：

```text
AUTO · BALANCED
```

或：

```text
说书策略：自动 · 平衡
```

点击打开 Bottom Sheet / Dialog：

```text
选择方式
● 自动选择
○ 手动选择（显示推荐）

推荐风格
○ 稳健
● 平衡
○ 激进

高级推荐提示
● 开启

说明：修改只影响尚未提交和未来的决定。
```

不要求说书人离开当前夜间流程返回 Setup 页面。

### 9.3 关键 UX 原则

- 一到两次点击完成切换；
- 修改 Policy 不触发 Setup 重建；
- 修改 style 后，当前 PENDING 候选可以即时重新排序；
- 明确显示当前 active policy；
- COMMITTED 决策不因设置变化而改变；
- App 重启 / 恢复存档后恢复当前 Policy；
- 历史 DecisionAuditRecord 保留当时 Policy snapshot。

### 9.4 Assisted 模式的产品价值

ASSISTED 不应被设计成“功能较少的自动模式”，而应成为熟练说书人的高级工作方式：

```text
机器负责：
规则
可能世界
信息强度
风险
候选排序

人负责：
现场发言
谁在演什么故事
玩家表现
戏剧节奏
是否尊重 earned advantage
最终选择
```

这与 Narrative Fairness 的边界完全一致。

---

## 10. 修订后的推荐解释模型

候选解释建议扩展为：

```text
1. Rule Legality
2. Functioning-world Existence
3. Information Usefulness
4. World Entropy Before / After
5. Demon Structural Diversity
6. Malfunction Exposure
7. Structural Balance
8. Confirmation-chain Risk
9. Story Disruption Risk
10. Style / Pacing Fit
11. Automatic Eligibility
12. Human Judgment Hint（如适用）
```

示例：

```text
候选 B：ELIGIBLE / STRONG

机械评价
- 正常能力世界仍存在
- 信息量中等
- 恶魔候选仍保持 4 个座位
- 无明显自证醉酒风险
- 结构平衡正常

叙事提示
- Story Disruption：HIGH
- 该信息可能明显削弱当前邪恶身份故事
- 如果该故事主要由邪恶玩家优秀发言与配合建立，可考虑较温和候选

当前策略
- ASSISTED
- BALANCED
```

注意最后一条是条件式 Storyteller hint，不是系统对玩家表现的事实判断。

---

## 11. 修订后的实施路线

v2.0 的 Phase A / B 保持不变。

```text
Phase A  正确的世界
Phase B  正确的玩家认知
Phase C  正确的信息节奏 + 运行时说书策略
Phase D  有限未来前瞻
Phase E  Soft Evidence / Social Story Understanding（未来）
```

### Phase A：保持 v2.0

```text
A0 Reference Freeze
A1 Semantic Model
A2 ASP Oracle Harness
A3 Enumerated World Baseline
A4 ZDD Runtime Prototype
```

### Phase B：保持 v2.0

```text
B1 PlayerWorldSet Integration
B2 First-night Epistemic Gate
B3 Drunk Joint Planning
B4 Multi-night WorldSet
```

### Phase C：修订后的第一版生产路线

#### PR C1：Entropy Metrics Shadow Mode

保持 v2.0：

- `H_world`；
- `IG_world`；
- `ER_world`；
- `H_demon`；
- possible Demon / Minion；
- explanation clusters。

不改变生产推荐。

#### PR C2：Expert-labelled Pacing + Narrative Dataset

在 v2.0 标注基础上增加：

```text
TOO_WEAK
PRODUCTIVE
TOO_STRONG
MALFUNCTION_EXPOSING
HARD_SOLVE
NARRATIVE_LOCK
STORY_DISRUPTION_HIGH
STRUCTURAL_IMBALANCE
```

人工 review 时额外记录：

```text
是否属于系统结构失衡？
Story Disruption 是否机械上高？
若知道现场故事，该候选是否可能破坏 earned advantage？
```

最后一项仅作为人工备注，不训练当前自动评分。

#### PR C3：Pacing Policy + Structural Balance v1

实现：

```text
Official Legality Gate
↓
Epistemic Gate
↓
Malfunction Gate
↓
Information Usefulness
↓
Pacing Band
↓
Structural Balance
↓
Narrative Risk
↓
Style Score
```

禁止实现：

```text
current team ahead
→ automatically boost other team
```

#### PR C4：StoryDisruptionEvaluation Shadow / Assisted Hint

新增：

- Story disruption reason codes；
- explanation-cluster collapse；
- bluff story mechanical disruption；
- candidate explanation 文案。

第一阶段：

- AUTO 只作为软辅助特征；
- ASSISTED 明确展示；
- 不自动判断 earned advantage。

#### PR C5：Runtime Storyteller Policy State

将原来开局设置中的：

```text
Manual / Auto
Conservative / Balanced / Aggressive
```

迁移到可持久化的 `StorytellerPolicyState`。

完成：

- 游戏中随时修改；
- PENDING decision 重新排序；
- COMMITTED decision 不变；
- DecisionAuditRecord 保存 policy snapshot；
- save / restore；
- UI 快速入口。

#### PR C6：AUTO / ASSISTED Unified Pipeline

统一两种模式：

```text
same candidate engine
same world simulation
same metrics
same gates
same ranking
```

仅最后 selection source 不同。

完成 Assisted Top N UI 和 `MANUAL_ONLY` warning。

#### PR C7：Replace Heuristic Metrics

逐步替换旧：

```text
InformationScore
AmbiguityScore
HardSolvePenalty
ConfirmationChainPenalty
```

加入：

```text
StructuralBalance
StoryDisruption
```

但 `EarnedAdvantage` 仍不进入自动分数。

#### PR C8：Unified Selector Production Rollout

AUTO：

```text
LEGAL
→ ELIGIBLE
→ QualityTier
→ active runtime Style
→ tolerance
→ stable weighted random
```

ASSISTED：

```text
LEGAL / MANUAL_ONLY partition
→ ranked Top N
→ hints
→ human selection
```

第一版正式目标更新为 **Phase C8**。

### Phase D：保持有限前瞻定位

在 C8 稳定后再实现：

```text
D1 FutureStorySpace
D2 Depth-1
D3 Budgeted Sampling
D4 Depth-2 Experiment
```

Story Disruption 可成为 FutureStorySpace 的软指标之一，但 earned advantage 仍不自动推断。

### Phase E：未来 Soft Evidence / Social Story Model

未来若 App 能可靠记录：

- role claims；
- claim changes；
- whisper summaries；
- nomination / vote；
- 玩家公开发言结构化摘要；
- 谁支持谁 / 谁怀疑谁；

才考虑建立：

```text
ExactPlayerWorldSet
        ↓
WeightedBeliefOverlay
        ↓
SocialNarrativeState
        ↓
EarnedAdvantageEstimate（实验）
```

即使未来实现，也必须：

- 保持与 Formal Rules 分层；
- 明确不确定性；
- 不把 LLM 判断当 hard truth；
- 有足够真实游戏数据验证后才允许影响 AUTO。

---

## 12. 新增测试与验收

### 12.1 Policy Runtime 测试

必须覆盖：

```text
开局 AUTO + BALANCED
→ Night 1 commit
→ 改 AUTO + AGGRESSIVE
→ Night 1 不变
→ Night 2 使用 AGGRESSIVE
```

```text
PENDING recommendation
→ BALANCED 改 CONSERVATIVE
→ candidate world facts 不变
→ ranking 可以改变
```

```text
ASSISTED 选择候选 B
→ audit source = STORYTELLER_MANUAL
→ 保存并恢复后仍可 replay
```

```text
AUTO 模式永远不选择 MANUAL_ONLY
INELIGIBLE 默认不进入 Assisted 推荐
```

### 12.2 Narrative Fairness 测试

Golden scenarios 至少覆盖：

- 高 StoryDisruption 但用于修正明显结构失衡；
- 高 StoryDisruption 但没有其他机械失衡证据；
- 单条线索正常、多线索联合导致 bluff story collapse；
- explanation clusters 大量减少但 Demon entropy 仍健康；
- Demon entropy 急剧下降且 bluff story 同时被推翻。

验收重点不是“算法猜中谁玩得好”，而是：

> **算法能准确展示机械 Story Disruption，并把“是否尊重 earned advantage”的最终判断留给人。**

### 12.3 UI 验收

- 游戏进行中无需退出当前流程即可修改 mode/style；
- 当前 active policy 清晰可见；
- 一到两次交互完成切换；
- 修改 Policy 不触发 Setup 变化；
- Assisted 模式清楚区分推荐、警告和人工判断提示；
- 赛后 replay 能显示每个 decision 当时采用的 Policy。

---

## 13. 更新后的第一版完成定义

第一版正式完成目标由 v2.0 的 `C5` 修订为 v2.1 的 **`C8`**。

完成时系统必须具备：

1. 正确且经过 Oracle cross-validation 的 World Engine；
2. `PlayerWorldSet` 与多夜认知一致性；
3. Entropy / Demon Structural / Explanation Cluster 指标；
4. Productive Uncertainty / Epistemic Pacing；
5. Structural Balance，而非简单“帮助落后方”；
6. StoryDisruptionRisk 与 Narrative Fairness 人工提示；
7. 游戏中随时切换 AUTO / ASSISTED；
8. 游戏中随时切换 Conservative / Balanced / Aggressive；
9. PENDING decision 可按新 Policy 重排；
10. COMMITTED 历史不被修改；
11. 每个 decision 保存 Policy snapshot；
12. AUTO 与 ASSISTED 共享同一 Recommendation Engine；
13. Assisted 模式能够让熟练说书人利用现场信息覆盖纯机械推荐。

第一版仍**不要求**：

- 自动理解完整玩家发言；
- 自动判断哪一方“玩得更好”；
- 自动估计 earned advantage；
- ISES Depth-1 / Depth-2 搜索；
- Soft Evidence probabilistic model。

这些属于后续增益项，不阻塞核心算法上线。

---

## 14. v2.1 最终设计原则

在 v2.0 原有原则基础上新增：

21. **好线索分为规则正确、机械质量和现场叙事适配三个层级，不应混为一个分数。**
22. **自动算法负责 Level 1 + Level 2；Level 3 当前主要由现场说书人判断。**
23. **平衡不是把双方实时拉回 50:50，而是避免系统自身制造不公平。**
24. **可以修正结构性失衡，但不应自动抹掉玩家通过优秀表现赢得的优势。**
25. **Story Disruption 是风险描述，不天然等于坏线索。**
26. **在没有可靠现场 Soft Evidence 前，禁止自动计算“玩家表现分”或 EarnedAdvantageScore。**
27. **AUTO 与 ASSISTED 使用同一个 Recommendation Engine，只在最终提交权上不同。**
28. **手动模式应是 Assisted Selection，而不是关闭算法。**
29. **Storyteller Policy 是运行时策略，不是开局后冻结的游戏事实。**
30. **已经 COMMITTED 的决定永远不因后续 Policy 修改而重算。**
31. **尚未提交的当前决定可以在 Policy 修改后重新排序。**
32. **每个 Decision 必须保存当时的 Policy Snapshot，以保证 audit / replay。**
33. **Conservative / Balanced / Aggressive 只改变软策略与 Productive Band，不改变 legality / epistemic hard gates。**
34. **熟练说书人的现场判断是产品能力的一部分，而不是算法尚未消除的缺陷。**

最终产品定位进一步明确为：

```text
机器负责把“能算清楚的部分”算清楚：
规则、可能世界、认知一致性、信息强度、结构风险、候选排序

说书人负责“现场才知道的部分”：
发言质量、身份故事、玩家投入、earned advantage、戏剧节奏和最终取舍
```

因此 CampBoardGameHost 的目标不是完全取代说书人，而是：

> **让自动模式足够可靠，让 Assisted 模式把熟练说书人的判断力放大。**
