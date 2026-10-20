> **后续净删/全夜控制检查点（2026-10-20）**：代码 `311191a30d6b0d83b5cf2d6de2a67905b526e596` 已将全夜步骤切换到 Session 事实，物理删除旧 UI 游标、Dynamic/Fixed 与 Planner/Projector 链，并执行无用途净删；当前说明见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 和 [路线](CURRENT_DEVELOPMENT_ROADMAP.md)。本地 fast 593/full 616（含 23 UI）及 APK 通过；精确代码 [CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS / [R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS。下方 af35cee9/21 UI 及“随后才替换全夜路由”的文字是此前检查点；当前合同不变，剩余 P2 全轨迹、P3 B/C、持久恢复、大小/P5 尚未完成。用户先前真机反馈不等于本批新 APK 验收。

# Minimal Host — 主持流程零基线审计与人工裁定（2026-10-20）

> **最新代码验收（2026-10-20）**：`af35cee951539df689552fb3c3ff97b7b2b5861e` 的 [完整 Android CI #4484](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558605) 与 [R2 #4060](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558519) 均 SUCCESS，Android full/APK、ASP、Clingo 与总门禁通过。本地 fast 671 / full 692 tests（含 21 个真实 Compose 交互用例）及 APK 通过。D2 系统 Back、全屏 Next、显式重试及真实查询的战略检查缺口已修复；详见 [D2 验收矩阵](D2_INTERACTION_ACCEPTANCE_2026-10-20.md)。这是代码/自动交互检查点，不是设备/像素视觉/现场或付费 API 验收。

> **2026-10-20 最新信息展示裁决 D2（优先于本文所有历史 D 文案；已批准，D2 已分批实施但未完成全部验收）**：信息角色的单步流程为 **信息候选/手动选择 → 点击“向玩家展示”打开全屏 → 点击全屏返回当前角色方桌（不推进）→ 可改选再预览 → 点击当前角色“下一步”才正式把最后一次确认展示的信息写入唯一 Session Observation，并让 Host 进入下一步骤**。单次步骤只记录最终确认的一条结果；先前预览的选项不是正式信息事件，无需逐条记日志。Next 前不允许进入下一角色；Next 后不可回上一夜间角色。可以只在 UI 临时状态保留上次预览及高亮，不得创建持久 Pending、已展示事实或第二权威对象。异常退出在 Next 之前：恢复同一未完成角色，重新选择/展示即可，不追求此前全屏、草稿和预览历史精确恢复。Next 提交必须按稳定 stepId 幂等、记录实际最终内容并保证已确认进度可靠恢复。唯一候选只给一个展示按钮；手动模式仅手选，辅助模式当前一条、未来可三条推荐加手选，自动模式仅 AI 已选信息展示按钮。**注意这是预览而不是确认玩家已经获得信息，旧“打开即写 Observation、关闭即下一位”一律作废。**

> **前一 D2 验证基线（最新验收见本文首段）**：最新通过完整 Android CI #4479 / R2 #4055 的代码 HEAD 为 `35a2c880646b3b2d1af8d3bd69a2636e57db9001`。相继完成 fullscreen preview/back 不写事件、唯一 Next Observation、真实查询座位绑定与过期失效、自动模式缺 AI 的等待/重试、手动不复用 AI、数字编辑草稿隔离、可靠匹配 typed 数字不额外求 AI、双人信息从最后预览恢复角色及座位高亮，且物理删除部分失效按钮/回调。原 `e0426d5` CI #4478 的 Kotlin 编译失败已在 `35a2c88` 修复并重新验证。尚未经过所有信息角色的真机视觉/互动验收，也未完成普遍确定性线索生成、统一 `nextStep(Session)`、完整 TB/NGJ 组合和中断恢复。**代码 CI 通过不等于 D2 或 Host 重建总体完成。**
>
> **2026-10-20 D2 信息安全收尾（对应代码已被后续 CI #4479 / R2 #4055 覆盖验证）**：数字草稿手动改值立即使上次预览失效；AI 异步更新不会重置人工正在编辑的数字。普通信息方桌只向玩家展示已生成 typed `displayProposition` 的确定结果/受验证推荐，不把空白占位当信息；自动模式未有有效答案时展示等待。侍女空结果时旧“查询并展示”没有可写 Observation，相关按钮及多余生产回调已删除，继续用当前两名玩家实际查询目标的数字输入。此批保持 D2 Next 正式发布边界，未开始 `nextStep(Session)` 和全角色真机验收。
>
> **实施顺序（当前）**：代表性 Poisoner→Monk→Imp→Butler Session 机械闭环已完成；D2 私密信息显示链多批生产代码已上线开发分支、尚待剩余角色和真机验收。随后重建 Host 唯一 `nextStep(Session)` 并同批移除已迁移 UI 游标/解析器，最后推进白天 B/C、完整角色/人数和恢复。不得以历史 D 方案替换 D2。

> **权威产品裁定，实施边界；不是代码已完成声明。** 依照用户 A–D 四项明确批准，优先级高于此前任何与准备页、日末确认页、Klutz Learned 事件、信息展示关闭后动作有关的历史文件、测试和代码注释。总合同见 [MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)。审计的生产源码检查基线包括 `ccaef940`、`0e63d9c`；制定文档时最新开发机 HEAD 为 `4fc60df`。每批实施前必须重新扫描当前真实 HEAD/工作树及消费者。
>
> 本审计枚举约 226 个 Kotlin 生产文件，并阅读/追踪关键 Activity→Compose App→Host UI→Session、夜间、白天、AI/事件及持久化入口；**并非 226 文件逐一完整语义验收**，不能宣称全仓旧代码已清零。已定位项目详见下面矩阵；未确认代码按 REVIEW_REQUIRED，不可假设已删除。

> **本地接续更新（2026-10-20）**：不使用 MiniMCP。同步基线 `cf3dd516`（代码 `35a2c880` 完整 CI #4479/R2 #4055 成功）。双人身份/座位编辑未统一使旧预览失效是本批 D2 缺口；UI index/handoff/Previous 空参数属后续已规划退役，自动点目标提交须明确删除。具体当前任务见路线图和交接，以下审计表为实施时需复核的职责清单，不能把全部行当成今日仍未修改。

## 1. 先从游戏规则独立定义最小流程

```text
创建游戏（TB 5–15 / NGJ 5–6、座位、配置、保留双跨局防重复）
  -> 分发身份、已确定的实际配置与红鲱鱼/恶魔伪装等真实裁量
  -> 首夜第一个实际可执行步骤
     -> 当前角色 UI 草稿/必要玩家操作、Host 唯一决定固定规则和是否唤醒
     -> 行动类本步 Next 确认时原子记录 Action；信息类 Next 时仅记录最终确认的最后一条预览信息 Observation
     -> 私密全屏返回仅回本角色供重选、重看；本角色 Next 后才完成并由 Host 进入下一步
     -> 已完成不允许回到上一个夜间角色；无需独立 Begin/Start/Previous/交接页
  -> 最后一夜间步骤完成直接进入 Day（绝无 Dawn/死亡播报/公开确认步骤）
  -> 白天同一方桌直接只读展示当夜已确认 Death
     -> 讨论/提名/投票/鬼票/技能/正式处决，按发生时记录必要事件
     -> 单个“结束白天”确认在**同一方桌**完成结算/胜负；不出现 EndConfirm 页面
  -> 已结束：展示 Session 结算结果；未结束：直接下一夜的第一个实际可执行步骤
  -> 每一次非唯一裁量由 AI/人工，说书人以外不由 UI/AI 决定流程本身
```

**状态权威**：唯一可写 Session/GameState + 按发生顺序确认事件；Script 提供固定角色次序/有限剧本例外；Host 是机械效果、下一步、触发/跳过、胜负和原子事务的唯一业务决策者。UI 保存**当前未确认输入草稿/可见性**，不能拥有另一份实际进度。AI 保留原有开局/后夜建议、战略记忆、人工/辅助/自动模式，通过 Host 一条提交路径；不得恢复通用候选枚举、评分、世界搜索、平行 Rules 权威。

## 2. 人工裁决 A–D：已批准、无需反复征询

| ID | 已批准行为 | 必须删除/收敛 | 不得误删的能力 |
|---|---|---|---|
| **A 夜晚开始** | 夜与首夜直接进入 Host 计算的第一个可执行步骤；无“开始第N夜”“首夜准备完毕”页面 | `nightStarted` 布尔流程分支、`ClocktowerNightReadyCard`、专用 StartNight 屏和仅支撑它们的按钮/测试；不允许改为隐藏的自动点击/空步骤 | 确实需要的开局裁量（恶魔伪装、红鲱鱼、酒鬼选定）作为独立真实配置输入；必须记录 |
| **B 白天结束** | 同一白天方桌上一键确认结束，进行本日处决/无人处决、特殊结果与胜负结算；无需离开方桌再点击 | `ClocktowerDayMode.EndConfirm`、`ClocktowerExecutionConfirmScreen` 和跳转到独立确认页的接口/状态/旧测试 | 日间提名/投票/处决动作及票数、胜负规则仍在 Host 确认，不可绕过投票权限/鬼票校验 |
| **C NGJ Klutz** | 已发生 `Death` + 身份/已完成角色行动推导出应执行 Klutz 玩家选择；不再单独记录“获知死亡”事件 | `KlutzLearnedDeath` 类型/producer/记录入口/单独发布标志、`clocktowerKlutzReturnToDawn` 等过渡状态以及其独占测试/AI历史映射（在安全迁移后） | 必须保留 Klutz 真正选座位及其实际结算；夜死在新 Day 才显示给玩家；白天死亡当时触发流程；不新增独立 Dawn |
| **D2 展示确认（最新）** | 可多次打开全屏预览、返回当前角色重选；**本角色 Next 才把最后一次确认展示的那条信息写为唯一 Observation，然后 Host 前进** | `pendingAutoAdvanceAfterDisplay`、展示即提交、关闭即导航、双重展示完成标记及其失效测试；迁移后删旧 reveal lifecycle | Next 前不记预览历史；崩溃可重做本步。Next 后稳定 stepId 幂等、无上一角色。AI 仅供信息选择，不跳过真人展示 |

**边界澄清**：D2 以本文件开头最新裁决为准：打开展示与返回均是可丢弃的 UI 预览，Next 才记录最后确认的一条信息并前进；之前的“展示即记录、关闭即推进”方案已撤销。C 的“由 Death 事实触发”不免除真实 Klutz 人工选座位的事件记录。

## 3. 已查明的旧流程/隐藏控制：改造与删除台账

| 优先 | 现存/历史路径（实施时核对最新代码） | 结论 | 所需验证 |
|---|---|---|---|
| P0 | `ClocktowerHostScreen.kt` 夜前 `nightStarted`、`ClocktowerStorytellerRecommendationScreen`、`ClocktowerNightReadyCard` | **DELETE A**：取消实际 StartNight 分支；开局真实裁量改为角色配置步骤 | 首夜及后夜无准备页，立刻呈现合法第一步 |
| P0 | `ClocktowerHostScreen.kt` `ClocktowerDayMode.EndConfirm`、`ClocktowerDayScreen.kt` `ClocktowerExecutionConfirmScreen` | **DELETE B**：把 onConfirmDay 结算入白天方桌按钮 | 不重复处决/NoExecution、选票与胜负一致 |
| P0 | `CampBoardGameHostApp.kt` `captureKlutzLearnedDeathOnPublicAnnouncement`、`NoGreaterJoyKlutzHistoryProducerV1.kt` 的 learned、`clocktowerPendingKlutzName`、`clocktowerKlutzReturnToDawn` | **REPLACE/DELETE C**：改为 Session Death/Execution + Klutz 实际选择推导待处理 | 夜死及日间死亡两入口、真实玩家选择、无伪造 Learned |
| P0 | `ClocktowerHostScreen.kt` `playerDisplayStep`、`pendingAutoAdvanceAfterDisplay`、`FirstNightInformationMigration`、`ClocktowerPlayerRevealHandoff` | **REPLACE/DELETE D2**：全屏返回不推进；Next 唯一正式确认 Observation 与后继，删除旧展示即提交及重复显示去重 | 重预览可改选；Next 只提交最终一条；中断可重做未确认步骤，已确认幂等 |
| P0 | `ClocktowerNightHostProjection.kt` + `ClocktowerDemonAttackProductionAdapter.kt` 死亡预览驱动唤醒 | **REPLACE**：仅从已确认 Session Attack/Death/Poison/Protection/Mayor 分支决定实际夜序，然后删除整条失效预测路径 | 杀死尚未行动角色跳过；Ravenkeeper/Sage 真触发；受保护不死 |
| P0 | `ClocktowerNightCheckpoint.kt` + `ClocktowerDynamicNightAdvance.kt` + UI `nightStepIndex`、`AwaitRefreshedFlow` | **REPLACE**：由 Host 根据稳定步骤 identity、Session 已完成事实选择下一步骤；不保留 UI 第二流程游标 | 角色因死亡消失、死后触发插入、没有重复确认/跳过 |
| P0 | `CampBoardGameHostApp.kt` `evaluateGameOutcome(context, cards)` 和 `gameOutcome` 写入 | **MOVE**：确定性胜负、Saint/Mayor/Demon succession 统一进入 Host；UI 可保留只读展示开关 | Host 真实状态和显示状态一致，多结果优先级准确 |
| P1 | `ClocktowerNominationInteraction.kt` 中 Virgin/Spy 判定、`ClocktowerArtistInteraction.kt` 中回答真值 | **MOVE**：确定结果到 Host，真正角色裁量/玩家问题可从 UI 输入 | Virgin/Spy/醉毒、艺术家独立真值与实际播报 |
| P1 | `ClocktowerFirstNightInformationRequest.kt`、`FirstNightInformationMigration.kt` 单候选发布域和临时已显示 lifecycle | **REPLACE**：Session Observation + 单条推荐直接绑定，不为单候选重建旧域引擎 | 已展示信息严格一次；手动及自动、AI预选不中断 |
| P1 | `ClocktowerFlowPlanner.kt`、`ClocktowerHostInteractionProjector.kt`、`ClocktowerScriptNormalizer.kt` 的 `DUSK/DAWN` token | **REVIEW THEN DELETE** 非必要 system token/UI；既有 Dawn UI 在 `0e63d9c` 已删除，后续 token 不能再成为步骤 | 内置剧本夜序、6 人开场信息、正确日夜轮换 |
| P1 | 旧 `DawnCommitIdentity`、后夜死者显示 `PUBLIC AliveAt` 构造 | **CLEAN** 非必要重复公开事件/旧命名；已有稳定 ID 若是事件历史 key，重命名不得破坏去重 | Night→Day 无第二次 Death、无隐私泄露；其他日间公众事实仍可记录 |
| P1 | 多处 `PlayerCard.eliminatedRound` 与 Session `alive` 双状态读写 | **REPLACE** 所有规则判断；展示数据只能由 Session 单向派生 | 已死角色/鬼票/票数/胜负不因视图过时错判 |
| P1 | 夜间 `onPrevious` 禁用参数与专门 UI scaffold | **DELETE** 无用途参数、空 callback/旧测试 | 任何夜间角色不可回上一角色；配置/白天取消草稿仍允许 |
| P1 | App 中 `LaunchedEffect` 自动确认选中信息与自动下一步 | **MOVE** AI 自动选择到统一 Host 事务入口；UI 可执行显示关闭等真正展示动作 | 无重复提交；没有在玩家看到信息前自动跳过 |
| P1 | 活动对局只在 Compose `remember` 持有，缺乏证实的持久化 Session 恢复入口 | **BUILD MINIMAL** 单 Session 原子保存、安全恢复，而非旧平行存档引擎 | 进程被杀后能安全继续，宁可重复未完成操作也不篡改已公布事实 |

**审计注意**：以上为已查明的活跃旧路径和有待迁移职责，并非所有行都证明功能完全错误。特殊角色必须以真实合同判定“必需／非唯一裁量／冗余”，不能仅因发生在 UI 或叫 `candidate` 就暴力删掉当前有用的 AI 路径。不要为了未来 Homebrew、独立 Web、实验性评分保留复杂度。

## 4. 实施顺序、权限与每批清理退出标准

1. **P0 继续补全当前全树职责地图**：枚举所有源码/资源/入口/生产消费者（前轮只深入核查关键调用链），每项分 KEEP_MINIMAL、REPLACE_AND_DELETE、DELETE、OWNER_REVIEW；包含角色逐项输入/触发/输出；不以“有调用/CI 仍要求”作为保留理由。
2. **P1/P2 Host nextStep(Session) 唯一路由**：先以投毒者→僧侣→Imp→死亡/条件唤醒覆盖确定性结算，同时直接切换 A；以证据删除 `nightStarted`、UI 业务 index、预测 adapter 及交接/旧等待链，确保身份展示时序。每迁移一条业务责任同批实际删除整条被替代控制链、辅助类、页面、独占测试/CI 守卫。
3. **P3 白天 Host 结算与 B/C**：白天方桌一次确认；Klutz 死亡事实触发真实选座位，不新增“得知死亡”事件；UI 不再自行判断胜负。处理夜死/日间死、胜负优先级和幽灵票。
4. **P4 D2 与全部 AI/UI 汇合**：全屏预览不提交、返回同角色可改选；仅本角色 Next 提交最后唯一 Observation 并推进；保留开局/后夜推荐、战略记忆、三模式和预选，所有模式走同一个接受接口。
5. **P1/P5 恢复及全树验收**：唯一 Session 原子存储、崩溃时安全续局；重复点击、重组、恢复、旧 ID 兼容、玩家私密资料；TB 5–15/NGJ 5–6 每角色合同与代表性全局组合；Android testFull/assembleDebug、R2、必要真机及大于 50KB 文件净化。

**物理删除原则**：功能不必需即删除；有必要但与旧实现耦合，先让最小新路径承接并验证同等实际游戏行为，再在**同一批**切断和删除旧 producer/consumer、数据字段、测试及门禁。禁止永久新旧双轨、影子 GameState、泛用 Rules 引擎、保留只读但无人调用的兼容封装、伪造旧事件保持测试通过。每批报告准确代码 SHA、物理删除量、实测 CI/R2、剩余调用和风险。**只读审计不等于完成迁移**。

## 5. 需要保护且不得误删的合同

- 两个内置剧本 TB 5–15 / NGJ 5–6 的阵容、首夜/后夜/白天流程与每个真实角色动作；脚本元数据和 5/6 人的特殊信息行为。
- 连续局阵容防重复、同一玩家连续角色避免重复，两者独立保留。
- 实际展示给玩家的消息、公开或私密范围、已发生事件的有序记录；红鲱鱼等实际开局决策；事件日期轮次/位置/座位。
- AI 首夜联合策略、后夜增量、提示词/API/解析、策略记忆、MANUAL/AI_ASSISTED/AI_AUTOMATIC、过时版本判定；付费 API 必须另行批准。
- 确定机械效果：保护/中毒时点、死亡、跳过/触发、继任、票数/鬼票、处决及胜负；必要结构和身份/隐私/原子/去重安全。
- 真正人工误播只能添加显式纠正事实，不允许普通返回覆盖历史；意外退出后可从安全位置继续（持久化尚待实现证明）。

## 6. 测试策略纠正

删除旧测试中的 “Next 先 stage、下一步开始才 commit、允许 Previous 改前一步、黎明再公布死亡、独立 Klutz learned 事件、仅 StartNight/EndConfirm 页面渲染” 期望，替换为：
- A：首夜开局设置完成/后夜上日结束，第一可执行动作直接出现；有真正开局选择时先处理选择并记录，没有空转页。
- B：白天方桌一次结束，已投最高票及处决/无人处决严格提交一次；无第二确认页。
- C：夜死进入 Day 或当日现场死亡可准确触发 Klutz 选座位，一次性记录真实玩家选择，但无 Learned 单独事件。
- D2：全屏预览/返回不写事件，任何输入变更使旧预览失效，重新展示后本角色 Next 才写最终唯一 Observation；双击幂等、未确认中断可重做、不泄露下家信息。
- 基础：夜间无上一角色、Next 即刻原子提交；确定死亡即时影响之后的唤醒；Dawn 不成为阶段/单独死亡公告；Session 一直唯一；AI stale/安全边界不退化。

> **实施声明**：本文件记录 2026-10-20 新的权威设计决策和审计整改清单；它本身不证明 A–D 已经写入正式 Android 产品、不证明 226 个 Kotlin 文件已全部彻底审完，也不替代源代码、CI/R2 和真机验收。
