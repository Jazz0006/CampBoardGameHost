## 2026-10-21 在途实施：P3 白天与原子保存（未验收、未推送代码）

最新所有者要求：收尾 P2、推进 P3 并完成最终收敛；随后要求同步当前进度文档。本次仅提交文档，工作树 Kotlin 改动仍未提交。完整状态/下一步见 [路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 [交接](NEXT_DEVELOPMENT_HANDOFF.md)。已验收代码仍是下面的 `311191a3`，本轮不能借用其 CI #4486/R2 #4062 作为新代码验收。

| 在途职责 | 原链退出 | 本地替代及尚缺证据 |
|---|---|---|
| B 日结 | EndConfirm → ClocktowerExecutionConfirmScreen；ClocktowerDayScreen.kt 实删 | 方桌直接调用 Session.confirmEndDay，尚待日结/重复点击/UI 验证 |
| C Klutz | App pending/ResumeSameDay/Learned 捕获 → NoGreaterJoyKlutzHistoryProducerV1；producer 实删 | pendingKlutzDeath 从真实 Death/Execution 派生，实际 KlutzChoice 引用 deathActionId；需夜死/日死/善恶目标/重复与恢复证明 |
| 白天确定性效果/胜负 | App evaluateGameOutcome、旧处决/继任控制；Slayer 任意手工判死 UI | Session DayFlow + GameEnded；仅保留真实 Spy/Recluse 裁量。规则优先级、全部消费者、UI 路由及幂等未验收 |
| Artist | App 可写 used/claimed 标记 | 正式 ArtistAnswer + 实际私密 Observation；旧草稿测试回调未迁完，当前测试编译失败 |
| 唯一存档 | 仅 remember 的活动局缺口开始补齐 | SessionJson + noBackup AtomicFile + 元数据/恢复入口；无新旧双存档引擎，但落盘、故障回滚、真实进程恢复和错误 UI 未验证 |

当前工作树删除生产文件 2 个：`clocktower/ui/ClocktowerDayScreen.kt`、`clocktower/session/NoGreaterJoyKlutzHistoryProducerV1.kt`；删除旧人工判死 UI 专属测试 `ClocktowerSlayerDraftTest.kt` 1 个。混合 `LiveKlutzScriptReachabilityTest` 与 D2 UI fixture 正在迁移。新增生产 6 文件：`ClocktowerActiveGameMetadata.kt`、`ClocktowerOutcomePresentation.kt`、`clocktower/domain/GameEndReason.kt`、`clocktower/session/ClocktowerDayFlow.kt`、`clocktower/session/ClocktowerSessionJson.kt`、`persistence/ClocktowerActiveGameStore.kt`。

计入尚未跟踪新源码、按当前磁盘 UTF-8 字节对比 docs-only `8024613e`：生产 Kotlin 212→216，净 **-3B**；测试 150→149，净 **-2846B**。这是在途快照，不是最终净删成绩。超限仍为 App **125487B**、HostScreen **79274B**、Session **54513B**，未达到 <=50000B。

最近 `:app:testFast` 在 `compileDebugUnitTestKotlin` 失败，Artist 草稿测试仍调用旧接口；未执行测试。本轮无新的远端 Android CI/R2、无新 APK 真机/付费 API 验收。R2 旧 Slayer 字符串守卫尚需随真实行为迁移。P2 全轨迹、P0/P5 全树语义审计、余留 UI/AI 职责及大小收敛均仍未完成。

## 2026-10-20 已实施：无用途净删与全夜 Session 步骤切换

代码 **`311191a30d6b0d83b5cf2d6de2a67905b526e596`** 已推送原 Draft #335。本节是实际实施状态；下方同日候选表保留为迁移前审计证据，不再把已经完成的项目作为 NEXT。

### 实际退出的生产调用链

- **夜间唯一当前步骤**：`ClocktowerSessionState.nightInteractions/nextNightInteraction` 由两内置剧本固定夜序、当前真实/展示身份、生死与已确认 ActionFact/Observation 产生步骤。HostScreen 只按稳定 ID 取展示组件，进度数字只是派生展示；App 的异步 AI 接受改为检查当前 Session 步骤身份和原有 state/revision/mode/request 绑定。删除 `nightStepIndexState`、`nightStartedState`、`pendingNightAdvance`、`freshAutomaticPlayerTarget`、重置游标和 UI 接力逻辑；没有持久 Pending 或新可写 GameState。
- **夜序控制整链删除**：原 First/OtherNightFlow → FlowPlanner → InteractionProjector → ResolvedFlowFacts，以及 Fixed resolver / Dynamic handoff 与旧毒游标 chronology，均无生产消费者后整删。MaterializerRegistry 现在只渲染 Host 已决定的步骤，不再过滤死者或决定唤醒。原 NightHostProjection 缩成只读座位/能力展示，死亡触发读真实 Death，中毒读 Session 及死亡前事件；不再调用旧夜序和攻击预测。
- **行动必须显式 Next**：删除点座位自动提交、Mayor AI 自动确认、红鲱鱼 UI 自动推进 helper/效应。玩家真实选目标与 AI 的合法裁量预选保留，实际提交沿既有 Session 机械入口。夜间 Previous 参数/事件传播清除；共享白天取消、设置返回、D2 全屏返回同角色保留。
- **全部无用途候选已处理**：旧首夜 Request/Lifecycle/Migration 发布控制和专属测试退休，仅保留 AI 当前使用的 FirstNightInformationFamily；无调用的 live-query reveal disposition / role-choice projection 退出；完整 Spy/Recluse 登记控件、角色列表和多余 UI 映射退出，处女提名中的显式 Spy 镇民类型裁量保留。
- **旧死亡适配链整删**：攻击预测、安全座位全表枚举和无消费者结果不再存在。单一存活 Imp 的旧通用 Card 适配/规则包装亦因最后消费者退出而删除，唯一 Session 中的多 Imp 歧义失败和真实继任行为由新行为测试保护；确定性攻击、保护、投毒、死亡和继任实现仍保留。
- **两内置剧本收窄**：删除 CustomDefinition、jinx/special/bootlegger 解析/归一化/数据字段与专属测试/辅助 fixture。保留 TB/NGJ 资产、官方角色/提醒元数据、固定夜序及原两内置内容 hash 的规范输出，不增加动态导入兼容层。候选/多样性代码没有因名称相似而误删；两种跨局防重复及其既有测试保留。
- **测试专用交换 codec**：7985B 的 TroubleBrewingGameSnapshotJsonCodec 从 main 移到 test，保留 golden/round-trip 和跨项目交换合同；实际开局/AI 使用的只读 Snapshot/Projector 没有被删掉。

### 正式步骤完成证据与最小新增

- 行动完成直接读同夜真实 Poison/Protect/Attack/Butler/Mayor 事实；普通信息完成读同夜、同接收者能力的正式私密 Observation。没有额外机械“完成事件”。
- Spy 只在真实方桌交接后的 Next 写一条 **NightWake**，记录实际唤醒座位、角色和稳定步骤 ID；不复制 Grimoire、角色状态或信息快照，不制造另一条观察。此事实解决已无 UI 游标后“Spy 是否实际唤醒”的唯一完成证据。
- 开场邪恶信息在最终 Next 写实际私密接收范围和内容，预览中间版本不记事件，且校验 preview revision。角色变化后的 Imp 私密通知从 RoleChange 与实际确认 Observation 派生，删掉两个 UI 可写待通知标记；实际 RoleChange 不被通知二次写入。
- 同一信息确认的重复提交先核对已记录内容并幂等返回；新内容仍检查 revision，冲突不改事实。死亡触发信息使用已建立交互的醉毒标记，避免死后生死检查丢掉中毒属性。

### 文件、测试与净缩减（40909dfe → 311191a3）

- 生产目录退出 **16 个旧路径**：其中 **15 个文件删除**（含原混合 Migration 文件），另 **1 个 codec 移入测试**；新增 Session NightFlow 10732B 和保留下来的 Family 分类文件。实际删除名单可直接查该提交；不是“隐藏按钮”或留下旧实现。
- 专属旧测试整删 **18 文件**；混合 AI/数值/登记/目标选择/NGJ 元数据测试仅移除旧职责，保留或迁移真实行为。新增 `ClocktowerNightFlowTest` **14 个** Session 行为用例，D2 UI 从 21 增到 **23 个**，包括自动模式 Butler/Poisoner 点目标不提交、显式 Next 才提交的真实 JudgeScreen→Session 链。
- 按 Git blob 和关闭 rename 折叠的 numstat：生产 **+427/-2705，净删 2278 行 / 101108B**；测试 **+454/-2415，净删 1961 行 / 77387B**；生产+测试合计 **净删 4239 行 / 178495B**。codec 移动在生产/测试各自计出入，在总量中抵消。另 R2 工作流净 +3 行，更新旧控件必须存在的过时守卫。
- 当前 Git 跟踪 **828 文件**，生产 Kotlin **212**、测试 Kotlin **150**。全树完整语义审计仍未完结；本批只对整条已迁移职责负责，不宣称所有遗留模块清空。
- 全部仍超限生产源码：App **148697B**、HostScreen **80726B**、Session **53414B**；相比基线分别缩小 2002B、13782B、896B。没有新增超限文件，<=50000B 的最终门槛尚未达到。

### 验证与尚存边界

本地 `:app:testFast :app:testFull :app:assembleDebug` 全部通过：fast **593 tests / 145 suites**，full **616 tests / 146 suites**，0 failure/error/skipped，含 23 个 Compose+Robolectric 实际交互。直接运行原 R2 工作流脚本通过。精确代码的 [完整 CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) **SUCCESS**；[R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) **SUCCESS**。已核对 CI 的 full Android 单测/APK、APK 附件、ASP、真实 Clingo 和总门禁均 SUCCESS。没有本批新 APK 真机/像素视觉或付费 API 验收；用户先前真机反馈只覆盖其实际测试版本与范围。

本批完成的是全夜当前步骤/推进控制切换及相应净删。P2 全人数/全角色与跨夜完整轨迹、原子落盘/真实进程恢复仍需独立证据；P3 EndConfirm、KlutzLearnedDeath、App 白天/胜负控制仍未迁移，本批没有扩大到白天。P4/P5 还需 AI/展示剩余职责、文件大小和全树零遗留证明。没有新增需要所有者决策的阻塞；不 merge/retarget/rebase/force-push/删分支。

---

## 2026-10-20 当前代码清理审计：D2 检查点之后

本节是当前候选及迁移边界；下方旧 F 批次保留历史原文，不重新执行。用户已报告真机流程可运行、细节待优化，并要求优先审计代码清理；这不是全角色、完整对局、中断恢复或像素视觉的覆盖证明。本次只审计并更新记录，没有删除生产代码或测试。

### 基线与审计范围

- 本地分支 `refactor/minimal-night-flow-20261019`，HEAD `40909dfe05c3b43742f8d5ced98a3333bc6771bb`；GitHub #335 实时核对为同一 HEAD、open/Draft、base `codex/minimal-host-full-scan-20261018` / `76be7e176650d4b7852a8151ff577db718929fad`。main 仍为 `0abc842e5468e3ba41eb32b5fae15264c6f5e221`。
- 当前 Git 跟踪 858 文件；生产 Kotlin 226 文件、测试 Kotlin 166 文件。已横向枚举全部生产 Kotlin 的目录、大小和顶层符号引用，再对下述候选逐项读取、核对生产/测试消费者；不以符号零引用自动判死，也不声称 226 文件均完成语义审计。Android Manifest 的 Application 入口就是不可误删的零 Kotlin 调用示例。
- 目录库存：根包 102；catalog 8、domain 16、epistemic 7、flow 7、rules 10、session 29、setup 21、clocktower/ui 17、clocktower/persistence 1、clocktower 根 1；debug 2、根 persistence 5。计数不等于需要保留或删除的结论。
- 源码/测试引用检查同时覆盖 `app/src/main`、`app/src/test`、`tools` 和 `.github`，并对孤立候选用跟踪树检索复核。现有未跟踪 npm 文件与 node_modules 不属于本次审计删除范围。

### 可以近期清理，不依赖全夜 nextStep 重建

“可以清理”表示已经明确代码边界；删除批仍须切换混合测试、编译并取得相应行为验证，不能直接删除整份混合文件。

| 项目 | 当前证据、生产边界 | 建议实际删除与保留 |
| --- | --- | --- |
| 旧首夜发布生命周期 | `ClocktowerFirstNightInformationRequest.kt:16` 在生产没有调用；`FirstNightInformationMigration.kt:63` 的迁移器和 publication resolution 只被测试实例化；Lifecycle 只服务此迁移器。当前真实展示走 HostScreen 的 typed preview → `recordReliablePrivateInformation` → `onCommitConfirmedPrivateInformationPublication`，没有接入这套 ready/displayed 集合 | 整删 `ClocktowerFirstNightInformationRequest.kt`（3696B）和 `session/FirstNightInformationLifecycle.kt`（911B）；删除 Migration 文件中的 Candidate/Request/Migration/PublicationResolution。**保留** `FirstNightInformationFamily` / `usesAuthoritativePairDomain`，它们仍被 `ClocktowerGlobalFirstNightDecision` 使用，不能整删该混合文件 |
| 旧信息重新打开裁定 helper | `ClocktowerHostSelectionSemantics.kt:94` 的 `ClocktowerLiveQueryRevealDisposition` 和同名函数只被 `ClocktowerRedHerringAutoAdvancePolicyTest` 调用，没有 Host 生产调用 | 删除此枚举/函数及专属旧测试方法；保留同文件真实目标校验、当前 AI busy/error 绑定及红鲱鱼真实开局逻辑。单次正式 Observation、stale、幂等仍由当前 Session/D2 行为测试保护 |
| 未使用的角色选项投影 | `ClocktowerRoleRevealChoiceProjection.kt:37` 的函数与返回 DTO 没有生产或测试调用；同文件 `clocktowerRoleRevealAnchor` 仍被 Ravenkeeper typed result 使用 | 删 ChoiceProjection 函数/DTO；保留 Anchor 类型/函数，不能整删文件或破坏守鸦人的真实查询绑定 |
| 不再使用的完整登记 UI/状态 | `ClocktowerSpyRegistrationDecisionControls`、`ClocktowerRecluseRegistrationDecisionControls` 没有生产调用；Spy 角色列表及 Recluse 状态编辑也只在旧测试中使用。真实调用是 NominationInteraction 的 `ClocktowerSpyTownsfolkTypeDecisionControls` 和 explicit Spy 类型选择 | 删除两个废弃控件、角色选择分支、Spy 角色映射及 Recluse UI 映射/方法；把共享控件缩为实际使用的布尔类型选择。保留处女提名中 Spy 是否登记为镇民的真实裁量、显式选择与未选择的区别、对应 Session 事实。不是取消 Spy/Recluse 规则上的裁量能力 |
| 旧死亡预测的无消费者结果 | `ClocktowerDemonAttackProductionAdapter.kt:78` 仍遍历所有座位求 `demonSafeSeats`，并计算 `resolvedDeathSeat/Name`。生产只在 NightHostProjection 使用 `mayorRedirectEligible` 和 `facts.mayorSeat`；预测死者与安全座位没有外部生产消费者。后续唤醒已经读取已提交 Death | 裁掉整表安全座位和预测死者的计算/DTO字段，缩为窄市长边界输入，清除旧 Dawn 规划注释。保留攻击、保护、中毒的必要确定性规则及市长触发；NightHostProjection/adapter 整体退役另见下节，不因可裁剪部分就整删 |
| 夜间无效 Previous 传播 | HostScreen 两个夜间入口固定传 `canGoPrevious=false` / `onPrevious={}`，NightStepUi、各角色卡和 NightActiveScreen 仍传播这套参数 | 从仅服务跨角色后退的夜间接口/事件/分支清除参数链。共享 scaffold 仍用于白天取消、设置返回；D2 全屏返回同角色必须保留，不可全仓删除 onBack/onPrevious |
| 自定义/Homebrew 解析分支 | 两内置脚本仅有 `_meta`、官方角色引用及固定夜序。Parser 仍支持 CustomDefinition、jinx/special、bootlegger，Normalizer 构造自定义角色并计算 Homebrew source；没有产品导入入口需求 | 裁剪 Parser/Normalizer/CatalogModels 的自定义角色和专属校验/数据分支，删除其专属测试。**保留或直接简化**两内置脚本的解析、官方角色映射、固定夜序和必要内容身份；不能整删 Loader/Catalog 或资产。三份混合文件当前共 41815B，不能把总大小当预计净删量 |

第一项可整删的两个生产文件合计 **4607B**；其余为混合文件的局部职责，净删行数/字节必须在实施后测量，不在审计阶段编造总收益。

### 测试如何随上述清理处理

- 首夜旧链的专属 `ClocktowerFirstNightInformationRequestTest`、`FirstNightInformationLifecycleTest`、`FirstNightInformationMigrationTest` 可随被测实现退休。`ClocktowerChefAlternativePublicationTest` 和 `ClocktowerAiPairRecommendationRecoveryTest` 是混合行为证据：数值可真实记录、AI 预选及人工覆盖不得丢失，应改走现有 typed 信息/Session 入口，而不是整删有价值的 AI 回归。
- `ClocktowerRedHerringAutoAdvancePolicyTest` 只删除旧 live-query reveal disposition 的方法，保留真实红鲱鱼开局及 AI key 隔离用例。
- `ClocktowerRegistrationInteractionStateTest` 缩到真实处女/Spy 类型裁量，不继续为已无消费者的角色列表/Recluse UI 状态留代码。
- `ClocktowerDemonAttackDawnFactsTest`、`ClocktowerMonkRetargetRavenkeeperDeathTest` 中针对全表安全集合/旧 DTO 的断言需退休或收敛；Session 实际投毒/保护/攻击/Death、死亡触发和市长真实分支的测试必须保留。不能因为删预测而删规则行为证据。
- 任何只绑定字符串、类名或旧界面结构的 CI 守卫在实际迁移时复核；Session 写入、隐私、身份、stale、安全和完整构建门禁不属于清理目标。

### 必须先迁移，然后同批删除的生产控制链

| 优先级/阶段 | 仍活跃的链 | 迁移与整删边界 |
| --- | --- | --- |
| 最高，P2 | App 的 UI night 状态 → HostScreen `nightStepIndexState/nightStartedState/pendingNightAdvance` → Fixed resolver → Dynamic handoff → 再写 UI cursor / Night→Day | 用 Script + Session 已确认 ActionFact/Observation + 稳定步骤身份产生唯一当前步骤，覆盖所有信息/机械/死亡触发/跳过及必要实际开局配置。然后同批删 `ClocktowerDynamicNightAdvance.kt`、`session/ClocktowerFixedNightStepResolver.kt`、对应专属测试和 App/HostScreen 游标逻辑。两 helper 文件合计仅 5399B，主要工作在大文件中的控制职责，不以删两个文件冒称整夜已收敛 |
| 最高，与 P2 行动确认同批 | HostScreen `freshAutomaticPlayerTarget` → `clocktowerShouldAutoConfirmFreshPlayerTarget` / Mayor 自动分支 → `advanceNightStep()`（约 1399–1423 行） | 去掉无显式 Next 的提交入口，保留真实玩家输入、AI 裁量/预选和同一个确认函数；调整旧自动提交测试。它是仍执行的行为，不是可以仅凭零引用删除的死代码 |
| P2 | NightHostProjection → PlayerCard/checkpoint 攻击预测适配 → flow facts → planner/projector → UI materializer | Host 统一接收事实和决定步骤后删除被替代的计算/协调层；保留必要的纯展示 materializer 和稳定身份。当前 Planner/Projector 仍是内置夜序/触发的生产实现，不能按类名一刀切。`NightOrderToken.System.DAWN` 当前是脚本元数据边界，Projector 对其执行 Unit，不是已删除的 Dawn 页面/事务复活 |
| P3/B | DayMode.EndConfirm → HostScreen 路由 → `ClocktowerExecutionConfirmScreen` → App 日结 | 把同方桌一次确认与真实处决/无处决/胜负接到唯一 Host 提交；同批删枚举、独立页面职责、专属路由/测试。`ClocktowerDayScreen.kt` 仍含其他职责，不整文件误删 |
| P3/C | App `captureKlutzLearnedDeathOnPublicAnnouncement` → Producer.learned → `KlutzLearnedDeath` ActionFact/Draft/JSON/reducer → Producer.choice 依赖 Learned | 从已确认 Death/Execution、真实身份及未完成选择推出实际玩家动作，重接 choice 后同批移除 Learned 生产者/事件/编码/消费者。保留真实 KlutzChoice、顺序和对应胜负；不能只删事件声明或直接整删 producer 文件 |
| P3/P4 | App 内多处 `evaluateGameOutcome` / 可写 `gameOutcome`，UI 负责结算调度 | 将必要确定性终局与持续流程合入 Host；界面只读结果。迁移前不能删除仍使对局结束的分支；保留圣徒、市长、呆瓜、恶魔继任等真实规则行为 |

### 生产外移候选与必须保留的边界

- `clocktower/TroubleBrewingGameSnapshotJsonCodec.kt`（7985B）仅被 SnapshotProjectorTest 的 golden/round-trip 用例使用，没有 Android 生产或工具消费者；可从 main 移至测试支持代码。是否连同 golden fixture 整删须核对跨项目交换合同，不能把“当前未使用”自动等同“无合同价值”。Snapshot DTO/Projector 本身仍服务实际开局和 AI，不能一起删；只读快照也不等于第二可写 GameState。
- 保留唯一 Session、顺序 ActionFact/Observation、接收者隐私、去重/身份/stale、防凭据泄漏；`ClocktowerSessionPlayerCards` 是实时只读投影，不是第二份卡牌事实缓存。
- 保留现有实际 AI 首夜/后夜、prompt/transport/parser/memory、三模式及真实推荐；例如 `PendingPairInformationDecision` / `PendingMayorRedirectDecision` 是当前请求 DTO，不等于已经禁止的持久待提交机械操作，不能按 Pending 名字误删。
- 保留两内置剧本及两种跨局防重复。SetupDiversitySelector 的多样性评分属于明确保留的产品要求，不因名称含 Score/Candidate 就归入退休世界/合法域引擎。
- 旧 web、世界枚举、通用合法域的大块删除已发生，不回溯重建。全树还未完成语义分类的源码/工具/实验应继续横向审查，不能用本节局部候选当作 P0/P5 全部完成。

### 建议实施顺序与验收状态

1. 先做已经无生产用途的首夜生命周期、孤立 helper、旧登记分支和无消费者死亡预测结果的净删除，处理混合测试；无效夜间 Previous 参数可同一夜间 UI 整理批收掉。
2. 随即完成 P2 唯一全夜步骤与显式 Next，替换并整删仍活跃的旧推进控制。自定义脚本分支可作独立明确边界的净删批，避免同全夜语义重建混为一个不可审查的大提交。
3. 完成 P3 白天 B/C 与胜负归 Host，再做 P4/P5 全树复核；每条已迁移职责当批删除旧实现，不等最后阶段或 PR 合并才动手。

本次生产文件删除 **0**、测试删除 **0**、代码净缩减 **0**；仅记录审计。所有超限生产源码仍为 App **150699B**、HostScreen **94508B**、Session **54310B**，必须先净删多余职责，再按必要内聚职责整理到每文件 <=50000B。

实时复核原代码 `af35cee951539df689552fb3c3ff97b7b2b5861e` 的 [完整 CI #4484](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558605) / [R2 #4060](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558519) 均 success；本次无代码变化，未重跑 Android 或付费 API。上述成功不是未来删除批的验证。剩余风险是仍由 UI 控制部分步进和结算、原子持久化/安全续局尚无完整证明；本次明确净删项没有新增产品决策阻塞，跨项目 codec 整删仅为待核合同候选。PR 继续 Draft，不 merge/rebase/force-push/删分支。

---

> **2026-10-20 当前使用方式**：当前审计结论见上节；以下是原逐路径审计/删除证据台账，保留历史原文，不是今日实施状态。旧 F15 人工夜末判死、Future Rules、Pending/Previous/Dawn 与 MiniMCP NEXT 都不再执行。当前采用唯一 Session、显式 Next 与 D2，阶段与实测证据只读当前路线/交接；全树语义审计仍未全部完成。

> **2026-10-19 改变审计组织方式**：当前全树审计与退出验收以 [Minimal Host Architecture Rebuild](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md) 为准：P0 按完整目标职责横向遍历当下真实源码，P1–P4 每条业务链重建同时删除旧调用，P5 再全树严格复核。旧 F12-L/R 日期、审计覆盖分子/分母、F15 夜末自由选择死者均仅为当时历史，不是现在的交付目标。不得因为一个旧生产调用保留旧功能，也不得用历史台账漏审量代替真实当前代码树的审查。
>
> **历史库存保留，现行审计结论见上述新合同。** 2026-10-18 所有者纠正了早期方案：Host Core 保留唯一事实及安全提交；旧 Host 规则合法性门禁要删除，未来 Rules 独立只读重建；**现有 AI 说书人推荐系统必须完整保留**，不再执行“AI 清空后重建”。旧库存列表含先前已删除文件，不能视为现行 Git tree。

# 最小 Host 全仓源码遍历 / 删除边界台账（2026-10-18）

## F15：明确夜间实际结果与继承确认，移除 Dawn 提交端规则规划（2026-10-18）

- **最新任务顺序**：所有者要求先实施本步，并再次强调全文件遍历约20%尚未完成、后续必须继续。F15完成后下一任务回到现有逐路径表中的未审文件横向覆盖，不再仅沿夜间调用链推进，也不把局部实现或净删率称全仓审计完成。
- **基线**：本地/远端Draft #334一致 `9f6c4af245b3aa7424e1134ad1789be6520181bc`，base `64be3090`、main `61cd6796`；基线docs-only CI #4376 / R2 #3956 SUCCESS。上一完整代码F14 `f776151e` CI #4375 / R2 #3955不能替代本轮代码验收。Mini MCP clocktower仍为独立旧Linux `a2cbcac8`，不覆盖本地；原未跟踪文件原样保留。
- **实际入口**：新增ClocktowerNightResultConfirmation界面及窄输入：明确无人/多名死者、实际成为Imp的任意座位、原已确认中毒是否延续。默认不从攻击、Mayor、Monk、Soldier或SW计算死者/继承人；界面保留复核/取消，gameId绑定及双revision过期禁止确认，提交按钮防双击。结束夜间的两条入口均进入此确认；新身份沿原私密展示→实际RoleChange确认，随后Dawn/必要Klutz步骤衔接。日间自动继承不是本批删除范围。
- **唯一事实/原子性**：Session新增窄commitConfirmedNightDeaths，全部座位、重复输入、版本以及Action/Observation身份先经现有纯提交函数暂存预检，再一次发布同一个Session状态；没有第二Session/writable GameState/恢复器。每名真实新死者仍只有一个Death及其公开AliveAt，实际继承仍为原RoleChange；先前已死亡的座位不制造第二次死亡。NightTargets.after(Death)回写attackSeat以及clearResolvedNightDeath已删除，Attack与实际Death永久区分，Dawn名单从本轮实际Death事件读取，支持多名。
- **明确空结果**：增加结构化NoNightDeaths及Draft、JSON、canonical payload、公开历史和AI delta的必要合同；用户明确无人死亡才提交该事件，不把历史缺失推成无人死亡。现有v2缺失旧历史仍保持原样，新kind可以round-trip。死亡公布不包含角色、保护、毒或继承人身份；角色变化继续只在Host/AI私密事实中出现。
- **生产整删2文件**：`clocktower/session/NightDawnResolutionPlanner.kt`、`clocktower/session/TroubleBrewingDemonSuccessionResolver.kt`。删除planner death/role/continuation/poison-resolution DTO与Forced/Choice晋升规划、临时effective alive/role值类型、重建当前Imp fallback、旧DemonSuccessor action/Materializer/UI合法候选与自动单候选选择、Session/checkpoint的旧继承候选字段/API。DawnPoisonCarryIntent只保留已确认keep/clear的窄输入，归于原SessionPoisonBoundaries；不再按Poisoner活着/转职推导清除。
- **旧链退出**：App.onConfirmNight → DawnDeathFacts/planValidatedNightDeath → 候选继承/planDemonSuccession → confirmNewDemonIdentity/planPoisonCarry 的提交链，及UI候选→Session pending successor→规则合法域再次否决均退出。既有DemonAttackProductionAdapter仍仅供信息步骤的只读预览，原公式在既有adapter中直接收敛，不参与最终确认。其死亡/保护计算、Sage/Ravenkeeper预览、毒效果展示与旧Flow模板仍待后续职责删除，不能称全部Host Rules已移除。
- **专属旧测试整删10文件**：NightDawnResolutionPlannerContractTest、NightDawnResolutionPlannerPoisonContractTest、NightDawnResolutionPlannerMayorContractTest、NightDawnResolutionPlannerAttackOutcomeContractTest、NightDawnResolutionPlannerSuccessionResolutionTest、NightDawnPoisonClearIntentTest、NightDawnOrdinaryPoisonOwnershipTest、MayorRedirectDependencyInvalidationTest、TroubleBrewingCurrentDemonRegressionTest、ClocktowerPendingSuccessionFlowAuthorityTest。核实这些专属文件只消费退休pure producer/DTO，没有真实commitConfirmedPlayerDeath/ActualRoleChange/GlobalObservation调用；历史Imp识别的真实断言保留在CurrentDemonAuthorityTest。混合UI/current-Demon/poison/vote测试只去旧继承分支及旧checkpoint参数，真实identity/stale/幂等/独立Mayor/票数/机械状态断言保留。
- **本地验证**：完整 `:app:testFull :app:assembleDebug` **BUILD SUCCESSFUL**；175 suites、**741 tests，0 failure/error/skipped**（原761，退休25旧用例，新增5实际合同用例）。新增多死亡与原Attack/Protect独立、任意已知座位可接受实际角色变化、未知/重复/双revision stale不变、后续公布冲突整个批次不变、公开无角色泄漏、明确无人死亡的AI/public/JSON往返；既有开局/后夜/传输/战略/Session回归一起运行。无真机/付费API调用。精确代码 **`0f904f7031770bd2f12acea4299aa9b1d5c8e5c0`** 的完整 Android **[CI #4377 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759682)**（full单测+Debug APK）、**[R2 #3957 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759696)**；ASP、真实Clingo、CI gate均SUCCESS。 远端compare与本地51文件提交一致；883文件树/475审计路径无缺失、额外或重复。
- **净缩减/体积**：含新确认界面4673B，生产净删 **642行 / 30217B**；测试净删 **1300行 / 47578B**；共 **1942行 / 77795B**，不含文档。当前App **159449B**、HostScreen **99682B**仍超50000；Session42993B、新界面4673B，未新增超限文件。P1/P4未完成。
- **审计进度**：当前树 **883** 跟踪文件、**241**生产+**178**测试Kotlin；原486范围删除2生产+10测试、增1界面→**475**。已审计 **116/475（24.42%）**，0审计中、**359待审**。本批2个已审生产文件删除退出当前表，新界面、缩窄后的CurrentDemonAuthority及4个全文核查的实际边界/混合测试计新增结论；重读已审App/Session等不计新增。所有现行路径和字节维护在下方同一F12表，历史删除仅在本节留证。
- **风险/未决**：全树审计仍约四分之一，不能停在局部夜间链。只读信息预览还可能与最后确认的实际死者不同，其触发/手工补充流程仍待UI职责审计及真机验收；夜间不再由旧死亡预测顺便自动判终局，保留实际结果提交后的继续流程；日间自动继承/终局、Klutz、毒机械标记/日落清除与其它Host规则仍有真实消费者。所有现用AI首夜/pair/数字/后夜、prompt/IO/parser/memory/preselection及AI模式信息采纳保留；没有为退休Host规则停用AI。U1自定义导入仍待所有者决定，未删除；本批无新增所有者决策阻塞。保持Draft堆叠，不merge/retarget/force push。

## F14：退休夜间死亡/继承影子重建（2026-10-18）

- **基线/选批**：所有者要求继续死亡→继承→新身份/Dawn整组清理。本地/远端Draft #334起点 `ae54b58e28b7abe180c98dadf3abdd3630af0bcb`，base `64be3090`、main `61cd6796`；起点docs-only CI #4374 / R2 #3954 SUCCESS，完整旧代码验收仍为F13 `27d02f72` CI #4373 / R2 #3953。Mini clocktower仍独立旧Linux `a2cbcac8`，未用于编辑。原未跟踪文件保留。
- **本批明确范围**：先整块移除把攻击/继承选择推成死亡/角色事实的影子状态，保留实际Session Death/RoleChange及Dawn应用；不是宣称整条夜间规则链已清空。经生产消费者复核，NightTransactionRestoreComposition只有NightHostProjection消费；投影器/伪事件只有这两者消费，没有模型API/prompt/parser/strategic-memory消费者。旧台账把EffectiveNightState整文件列保留过宽：窄只读类型及source/cursor排序仍有用，MechanicalDeath/RoleChanged重放器无必要，现已修正拆除。
- **删除生产文件1个**：`clocktower/session/NightTransactionRestoreComposition.kt`（121行/5660B）。从`rules/ClocktowerEffectiveNightState.kt`删除`ResolvedNightMechanicalEvent`和`ClocktowerEffectiveNightStateProjector`；从NightHostProjection删除预测confirmedEvents、canonicalNightReconstruction、按interaction重放存活/角色的入口及无消费者暴露字段；HostScreen的Empath读取也直接用Session事实。
- **移除链**：confirmedAttack/confirmedDemonSuccessor选择 → 重新跑Imp自杀/强制或候选继承 → 合成MechanicalDeath/RoleChanged → before/after重放出第二份alive/role → Chambermaid目标、Empath存活上下文、Poisoner身份与信息步骤。现在身份/存活均读传入的canonical GameState；选择、草稿、旧cursor、返回上一步不能把尚未提交的候选变成实际角色/死亡。不创建兼容重建器或另一份可写状态。必要的source/cursor顺序仍用于尚存毒效果展示。
- **测试处理**：整删5个专属文件：NightTransactionHostIntegrationSmokeTest、NightTransactionReconstructionContractTest、NightTransactionReconstructorSuccessionLegalityTest、NightTransactionRestoreCompositionTest、SNE7NightTransactionBehaviorMatrixTest（均已全文核查）。matrix中纯Monk/Imp/SW断言与现存DemonNightAttackSemanticsTest/DemonSuccessionSemanticsTest重复，其独立测试保留，规则实现也尚未删除。混合ClocktowerEffectiveNightStateTest保留4个真实cursor排序/未知/重复身份用例并重命名为ClocktowerNightChronologyTest；影子事件排序/身份推演用例退休。Session真实死亡/角色/原子性/保密测试保留。
- **功能证明**：更新ClocktowerNightHostProjectionTest为canonical alive/role语义，保留Ravenkeeper信息步骤预览；新增一个贯穿Session→投影的用例，证明攻击/继承选择及2→1→0/越界cursor、草稿修改不造Death/RoleChange；实际Session提交后，即使UI cards/cursor旧，也不复活或回滚角色，重复实际提交不增事件。当前Ravenkeeper/Sage触发提示仍是旧UI预览，不声称它们已成为用户明确事实；没有删除AI推荐路径。
- **净缩减/当前树**：生产净删 **318行 / 15571B**，测试净删 **1238行 / 51061B**，总 **1556行 / 66632B**，含保留/重命名的4个cursor测试。当前 **894** 跟踪文件、**242**生产+**188**测试Kotlin；审计范围492→**486**（1生产+5测试净退出），已审计 **112/486（23.05%）**、0审计中、374待审。删除路径只在本节保留历史，不留当前表充当未审项；两个当前新增结论为投影测试和缩窄的chronology测试。
- **验证**：本地 `:app:testFull :app:assembleDebug` **BUILD SUCCESSFUL**；185 suites、**761 tests，0 failure/error/skipped**。原807，退休47旧用例、保留4个排序用例、新增1个真实Session/投影回归；测试fixture已显式使用生产GLOBAL_V1模式。精确代码 **`f776151ed8a3e778ddb088a78a4313cfc7a74dfe`** 的完整 Android **[CI #4375 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119919096)**（full单测+Debug APK）、**[R2 #3955 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119919080)**；ASP、真实Clingo、CI gate均SUCCESS。 远端compare核实15文件差异与本地提交一致；完整树894/486与当前台账逐路径核对无缺漏、额外项或重复。无真机/付费API验收。
- **仍需解耦/风险**：App.onConfirmNight仍调用DawnDeathFacts→NightDawnResolutionPlanner.planValidatedNightDeath；继承Forced/Choice和App自动promote、PoisonEffectLifecycle/planPoisonCarry、Ravenkeeper/Sage步骤eligibility仍存在。补查NightTargets.after(Death)回写attackSeat，实际死亡不能覆盖原攻击事实，留下一组一起断开。下一批优先实际夜间结果与继承确认输入，再成组删除这些门禁及专属测试；不能从删除影子投影推称人工确认已完全不受规则约束。App **172872B**、HostScreen **101202B**仍超50000，P1/P4未完成。U1自定义导入仍待所有者决定、未删除，本批无新增所有者决策。PR保持堆叠Draft，不merge/retarget/force push。

## F13：日间明确登记并入唯一事件，整组删除旧裁定日志（2026-10-18）

- **授权/基线**：所有者同意按完整职责链加速实施；本地/远端起点 `d76968168eb02e37cd87084bb51eabc60f789d9c`，Draft #334/base `64be3090`、main `61cd6796`；基线 docs-only CI #4372 / R2 #3952 SUCCESS，不作本轮代码证明。Mini clocktower 仍为独立旧 Linux `a2cbcac8`，没有覆盖本地。保留全部原未跟踪文件。
- **补齐8个Session全文审计**：DayAbilityRegistrationRulingProducer、CausalDecisionJournal、GlobalEventProjection、NightDawnPlanner、NightTransactionRestoreComposition、KlutzHistoryProducer、DemonSuccessionResolver、RuntimeGameProjector。前两项随本批退休，其余6项结论写入下方原逐文件表；不是把符号扫描算成完成。删除的三个旧测试已全文核查：Slayer/Virgin的候选、类型/阵营推导与archive restore专属合同退出；journal中实际Mayor安全用例单独迁入MayorRedirectBoundaryTest，其余捕获/校正/快照用例随第二历史删除。
- **实际删除3生产文件**：`DayAbilityRegistrationRulingProducerV1.kt`、`StorytellerCausalDecisionJournalV1.kt`、`DecisionOutcomeSnapshot.kt`。删除3旧测试文件：`DayAbilityRegistrationRulingProducerV1Test.kt`、`DayVirginSpyRegistrationProducerV1Test.kt`、`StorytellerCausalDecisionJournalV1Test.kt`，保留其中Mayor身份/未知座位/stale/幂等安全覆盖（新测试文件1个，未重建journal）。Session旧journal owner测试及空priorDecisions断言一并退出。
- **移除完整链**：App日间明确选择 → DayAbilityRegistration provider候选/旧规则快照 → frozen cutoff/candidate membership → PriorDecision的ability/truth/RegistrationFact推导 → 独立Captured/Committed/Corrected archive；连同Provider DayAbilityRegistration context、priorDecisions字段和DecisionOutcomeSnapshot DTO删除。Mayor的context改从既有Session/history builder读取。生产及测试/工具再查旧符号无引用，不给活跃AI增加兼容包装器。
- **保留与新边界**：SlayerShot增加nullable `targetRegistersAsDemon`；Nomination增加nullable `nominatorRegistersAsTownsfolk`，直接保存UI已经明确确认的Boolean，null是未确认/旧历史未知，false是明确否。与公开动作共享同一actionId/revision/globalSequence，提名后执行的顺序不变。不会把“登记恶魔”捏成Imp角色/邪恶阵营，也不会把“镇民类型”捏成好人阵营。不会从hit/处决反推出登记；实际死亡/执行/角色变化仍为各自确认事实。
- **序列化/AI/保密**：JSON兼容缺失字段，未知值编码仍省略字段；canonical payload只在明确登记存在时增加值，保护历史UNKNOWN标识。AI delta直接读取共享事件中的明确值；公开PlayerHistoricalTimeline继续只暴露动作/座位，不携带登记或隐藏身份。开局、pair/数字/后夜推荐、prompt/IO/parser/memory/preselection及AI模式未删除，无付费API/真机调用。
- **实际净缩减**：生产净删 **800行 / 43958B**，测试净删 **338行 / 16469B**，共 **1138行 / 60427B**（以起点Git blob与实际文件计算，含迁出的Mayor测试；不含文档）。现行跟踪树 **900** 文件：生产Kotlin **243**、测试Kotlin **193**；原497审计范围删除6项、增加1项→当前 **492**。当前已审计 **111/492（22.56%）**，0审计中、381待审；删除文件在此保留完成证据，不留在当前表中当未审项。与旧105/497不能只比较分子，分母变化已明确。
- **本地完整验收**：`:app:testFull :app:assembleDebug` **BUILD SUCCESSFUL**；JUnit XML核对190 suites、**807 tests，0 failure/error/skipped**（原819，退休15旧用例、新增3实际合同用例；Mayor1例保留迁移）。新增覆盖null/false/true编解码、canonical身份、单事件顺序、重复/变更ID/过期提交原子性、AI delta、各玩家回放保密；现有开局/后夜/传输等完整回归同时运行。精确代码 **`27d02f723340ff88c466d170655898be768d3ae3`** 的完整 Android **[CI #4373 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071885)**（full单测+Debug APK）、**[R2 #3953 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071868)**；ASP、真实Clingo、CI gate均SUCCESS。 远端compare与本地26文件提交一致；后续docs-only HEAD单独查询，不能替代此代码验收。
- **风险/下一组**：日间UI选择的展示条件、Slayer命中、Virgin后果、Dawn攻击/继承/毒及终局仍有旧规则，P1没有完成。下一组优先把实际死亡/继承/角色确认从NightDawn/RestoreComposition的预测与候选域中解耦，再删整组旧计算。App **172872B**、HostScreen **101994B**仍超限，P4未完成。U1自定义导入仍待所有者决定，本批无新增决策阻塞。PR #334保持Draft，不merge/retarget/force push。

## F12：当前 Git 树逐文件语义审计（进行中，A/B/C批）

- **证据 HEAD**：`cfe258b17ed1c8abb248f0ada4ee1c871593d961`。2026-10-18 重新核实本地/远端 Draft #334 一致，base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`。Mini MCP 独立旧 Linux HEAD `a2cbcac8` 不用作编辑工作副本。原未跟踪文件保持不动。
- **准确范围**：完整 `git -c core.quotepath=false ls-tree -r --name-only HEAD` 共 **905** 跟踪文件；审计分母 **497**：246 生产 Kotlin、195 测试 Kotlin + 1 测试 fixture、14 Android XML/JSON资源配置、20 tools 源码/数据、21 构建/CI/仓库配置及 player/ui 网页源码。纳入非 docs/artifacts 目录的 `.kt/.kts/.java/.py/.sh/.ps1/.bat/.xml/.yml/.yaml/.html/.css/.js/.ts/.tsx/.jsx/.json/.properties` 及 `gradlew/.gitignore/.gitattributes`。`docs/`、`artifacts/` 的历史文档/报告/数据，以及 README/AGENTS 说明、两个二进制 PNG/JAR 排除语义源码分母；AGENTS 是已读治理权威而非产品源码。构建输出与未跟踪 node_modules/package 文件不在 Git tree，不计入。完整树由程序读取并核对数量，终端大段输出曾被截断，不用该输出冒充完整清单。
- **逐项状态**：本节每条只有在全文读取并核查真实生产消费者后才给最终分类；A/B历史累计105/497；F13/F14之后F15继续删除/实施，当前已审计 **122/475（25.68%）**，审计中 **0**，其余 **353** 待审。扫描、符号匹配、旧台账曾提及和代码净删率不计语义完成率。测试/工具未读全文的维持待审，不能随生产文件结论自动标成已审。
- **验证口径**：下表最终结论已做源码静态审计、声明/实际引用交叉检索（app/src/main、app/src/test、tools、.github）；这不是新运行的行为测试。现有完整代码验收仍是 `d9fb47f2` 的 Android CI #4369 / R2 #3949 SUCCESS；文档 HEAD 的 CI #4370 / R2 #3950 SUCCESS，Android skipped。A/B审计批尚未改产品/测试/CI；F13已实施并完整验证，见上节。A批本地已运行 `:app:testFast --tests *GatewayTokenEnvelopeV1Test --tests *MurmurHash3Test --no-daemon`，BUILD SUCCESSFUL，3用例0失败；其余表项仅静态审计，未运行真机或付费 API。
- **首批整组建议**：① `PairInformationOutcome.kt` 为独立孤立叶子，列必须删除；此审计批先记录、不靠删一个叶子替代全树审计。② 攻击/继承/毒/Klutz 规则计算及 flow eligibility 为混合生产链，必须先保留实际结果输入和 Session 状态应用再退休。③ Provider/日间 causal DTO 仍有合法候选/真值标签和重复决策历史，需联合实际消费者审计；不能删活跃 AI。④ 目录、文本、时序、密钥、canonical state 与只读 AI 快照不是旧规则真值引擎，保留必要责任。
- **风险与待办**：所有手工事实应能绕过规则生成步骤/合法菜单，事实 seat/权限/时序/去重/原子性仍保留。App **178170B**、HostScreen **101971B** 仍超限；F12-B已完成整文件职责审计，两个都列需要解耦，尚未净删/缩至硬上限。人工审核项 U1：是否退出无生产用户入口的 custom/homebrew 导入分支；两内置脚本均只有 official references。推荐删除未上线分支，保留两剧本解析/目录；未决前不删。详见 parser/normalizer 两行。A/B纯审计批生产删除 0、专属测试删除 0、移除链 0、净代码缩减 0；F13实际删除见上节；不宣称 P1/P4 或全仓审计完成。

### F12-B：两根组件、Session 与现用 AI 全文审计（本批已记录，产品代码未改）

- **基线与范围**：2026-10-18核实远端/本地 `22716fa4d6bdbe92edeb0091b4c9483a733a7370`、Draft #334/base `64be3090`、main `61cd6796`；起点文档 CI #4371 / R2 #3951 SUCCESS（Android skipped）。完整905文件树/497审计范围没有变化。新增 **34** 项最终结论（30生产+4测试），其中根App3121行/178170B、HostScreen1795行/101971B、Session816行/39575B已分段核查全体主体与引用；本批审查源码合计 **478849B**。尚未全文读完的8个Session文件仍待审，不把本批符号引用扫描算作其结论。
- **需要整组处理**：A. App/HostScreen的Slayer/Virgin/攻击→Dawn/继承/终局规则及实际后果确认混合，保留真实death/role/poison/phase原子提交后退出计算；Artist/lastExecuted/pending/终局的事实语义收敛唯一Session/event，不能只为压体积移动巨型文件。B. UI同角色同夜不同结果拒绝、FirstNight生命周期和Inventory/Checkpoint按recipient已公布判关闭，需与同事件ID幂等和旧信息只读重开严格区分；新增已确认实际信息不得被能力次数规则拒绝，也不得改写旧事实。C. NightTargets清除、票数自动处决候选、Dawn毒推导、Mayor默认living-role定位仍是尚未退休链，不宣称F11已清空。D. BotcAiWireTrace原始私密payload和异常输出应缩减/核对安全导出边界，不增第二历史。
- **明确保留的现用AI**：JointOpening/InitialRequest/OpeningRecommendation、ProviderRequestFactory、冻结history/context、PersonalDirect及StrategicContinuation均真实消费；pair/scalar/Mayor factory均不填legalCandidates，开局/续规划encode/decode没有旧候选合法域重算。`pendingNeedsJudgment`只是非空检查，App中HOST_DETERMINED/合法domain的旧注释不能当仍有该算法的证据。保留prompt、IO、schema、receipt/response anchor、跨夜memory、preselection、stale隔离与实际publication才提升策略；未执行付费请求。
- **本地验证**：既有 `testFast` 定向PersonalDirectOpenAiV1Test、ConfirmedActionBoundaryTest、RoleIdentityBoundaryTest、SessionPlayerCardsTest，**16 tests、0 failure/error/skipped，BUILD SUCCESSFUL**；XML各6/6/1/3已核实。覆盖离线AI wire/retained链、Session事件/版本幂等、role原子性和同frame只读展示。通过的旧Butler规则断言仍列待解耦，不把绿灯当保留规则理由；没有新增形状测试。
- **变更/风险/下一批**：本批删除生产0、专属测试0、移除生产链0、净代码缩减0，只维护本台账与路线/交接；两个超限仍如上。实际Session/AI产品行为未改变，不等于上列旧门禁已修复。无新增所有者阻塞决策；U1自定义导入范围保持待决但不阻塞其余审计。下一批先补齐DayAbilityRegistrationRulingProducer、CausalDecisionJournal、GlobalEventProjection、NightDawnPlanner/RestoreComposition、KlutzHistoryProducer、DemonSuccessionResolver、RuntimeGameProjector这8个待审Session文件，再横向epistemic/codec、setup与工具/测试。完整代码验收仍为F11 `d9fb47f2` CI #4369 / R2 #3949，审计文档新HEAD需另查CI，不冒充Android编译。

当前范围已按F15变动重核：883跟踪文件、475源码/测试/工具配置；每个现行路径仍恰好一次。

### 2026-10-19 Owner scope clarification：旧Rules先完全清除，未来Rules再考虑；自定义剧本可删

- **本轮是已授权范围/归属纠正，不是代码清理完成证据。** 当前应保留最小Host的**唯一可写GameState/Session和结构化真实事件/原子提交**及**现有AI完整推荐**；所有旧Rules规则/候选/裁定、重复控制及不必要消费者必须先全部清除，不能因为生产还在调用就持续保留，也不能因未来可能重建独立Rules就在本期过渡保留旧算法。旧Rules完全清零、Host事实所有权收敛后才可能另行从零开始实现新的只读Rules；不包含当前P1–P5。
- **动态/自定义剧本**是未来可选、当前复杂则可删的分支；核实两现有`ClocktowerScriptJsonParser`/`ClocktowerScriptNormalizer`仍被内置TB/NGJ通过RulesetJsonLoader→BuiltInClocktowerRulesetCatalog真实调用，**授权拆除自定义角色/Homebrew扩展及其测试**，但保留/更小替换内置两剧本JSON/身份解析。将上述两行已审计状态`需要人工审核`→`需要解耦`，不再被U1产品范围决策阻塞；仅剩`TroubleBrewingGameSnapshotJsonCodec`跨项目消费是否必需的人工审核。全部现行台账仍为**223/444已审（50.23%），221待审**；已审128必须保留、**93需要解耦**、1必须删除（SetupDistribution）、1人工审核。不因改分类冒充新全文审计或代码已删。
- **下一批任务排序**：先横向逐文件确定实际Host事实/事件/安全、AI、平台必要UI或删除，优先完整归并同一个GameState/Session，集中清除大旧Rules模块和绑定的调用、独占测试，不再只为日间单个角色小规则沿链追逐；既有setup阵容及同人跨局角色防重复两个功能不得误删。所有删改仍必须精确代码HEAD完整CI/R2，文档检查Android skipped不是生产验证。

### F12-R：Slayer方桌彻底退出规则候选门禁、补审Virgin/Artist真实链（2026-10-19）

- **精确起点与PR**：开发机MiniMCP`clocktower`原分支`codex/minimal-host-full-scan-20261018`，clean且远端一致`fcaecb54b75d59a7d5b2f247207e27fef2b9c6ed`；PR #334始终Draft、base`codex/minimal-host-m1c-direct-number-20261018`/SHA`64be3090`不变，无merge/retarget/force push。
- **真实实删代码**：提交`e0cd90be62e00d52895e730ff05163de49da3e7a`（`[full-ci]`）从`ClocktowerSlayerTableState`删除按claimantCandidateNames/alivePlayerNames计算的旧规则资格候选及相应runtime拒绝检查；方桌现按唯一Session展示的所有**实际已知座位**先选射击者再选另一名目标，结构上仅核唯一已知名字、先选者与目标不同。沉淀删除`ClocktowerJudgeScreen`的`slayerUsed/slayerClaimedNames`白名单参数及GameApp专为UI生成的旧声明者列表，也删除`ClocktowerSlayerInteraction`旧alive卡过滤和多余cards参数；仍保留Session已用能力历史用于事实字段、公开Shot、说书人确认的实际hit/death以及Session原子提交、恶魔继任，不以真角色/醉毒/重复声明阻止录入真人选择。旧`ClocktowerSlayerTableStateTest`替换为实体座位、双座位次序、包含死亡座位的**公共射击对象记录**（真实死亡仍按原Session结构校验）和未知/同人拒绝，不为旧Host规则保留测试。精确GitHub比较**5文件+41/-114，净删73行**，无整文件删除。
- **真实代码验收**：完整 [Android CI #4414 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39204226346) / [R2 #3994 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39204226503)，Android tests+APK、ASP、真实Clingo、CI gate均绿色，不以之后docs-only skipped冒充代码验收；没有付费OpenAI API/真机。
- **横向6个首次源码/测试语义审计**：`ClocktowerNominationInteraction`与`ClocktowerNominationDraftTest`、`ClocktowerDayOverviewTableUi`、`ClocktowerSlayerTableUi`、`ClocktowerArtistInteraction`、`ClocktowerArtistTableState`均已全文+真实消费者核查，逐行分类在当前台账。两位旧自动规则仍待改：Virgin仍从能力身份/中毒/Spy登记算自动处决并记录firstVirgin；Artist仍用醉毒条件过滤真实可选公布信息与声称者候选。保留Day/UI/Session事实路径，不因当前Slayer优化误伤；原已审App/SlayerState/SlayerStateTest/HostScreen/SlayerInteraction只复核不双计。当前台账库存**444项、已审223（50.23%）、待审221、审计中0**。
- **下一批目标**：优先删除Virgin的自动触发/处决推导并以真实说书人确认的事件驱动（保留唯一Session Nomination+Execution）、Artist的candidate/真值门禁及当前发表信息；再处理`DemonAttackProductionAdapter`旧Imp/Monk/Soldier/Mayor预测→Sage/Ravenkeeper触发，必须在实际早期可恢复结果确认/唤醒入口就位后才能整链退役，攻击选择不等于真实死者。继续App/HostScreen/NightStepUi三个>50KB旧职责，保留全部AI首夜/后夜/提示词/传输/策略记忆/双模式以及跨局阵容与玩家身份两种防重复、Session事实/隐私/原子安全。P1/P4/P5尚未结束。

### F12-Q：Slayer射击改为说书人实际结果确认并删除旧one-shot自动裁定（2026-10-19）

- **仓库基线**：MiniMCP`clocktower`于同一Draft PR #334分支`codex/minimal-host-full-scan-20261018`从clean/equal`0a500c2f6d3438e9fa91611e2c31caef8c28ae4d`开始，base SHA`64be3090`不变；未合并、未retarget、未force push、无付费API/真机。
- **真实代码提交**：`3de38e6e53a841bd4c866ce1f2daa1fa5a739228`（`[full-ci]`）用`ClocktowerSlayerInteraction`中的显式“说书人确认：目标因射击死亡”勾选替代AI/Host对中毒、Drunk、真恶魔及Recluse登记的自动命中判断；真人仍选claimant和target，默认**没有死亡**，变更座位时清空临时确认；App按此**确认事实**记`SlayerShot`事件并仅在确认death时调用原`commitConfirmedPlayerDeath`和后续实际恶魔身份/终局流程，不悄悄用静态规则推断。已有`abilityConsumed`保留为本局真实Slayer已使用的事实字段，不再阻止说书人确认其他射击结果；`targetRegistersAsDemon`旧字段对已有Session历史与codec保留但新的现场动作不臆造私密登记。删除生产完全零消费者`AbilityFunctioningSemantics.oneShotDecision`、`OneShotAbilityDecision`、`selectedMechanicalEffectApplies`，以及旧Slayer专属醉毒/登记测试，保留其仍被其他角色生产使用的functionsAs/stateFor和真正的Session合约。**7文件+45/-165，净删120行**，无整文件删除。
- **R2旧门禁修订**：旧R2 #3991因硬要求已退休的`ClocktowerRecluseRegistrationDecisionControls`存在而FAILED；没有恢复控件，而提交`8b1d07b983de190ed0a61ec994fb01704a6e3cca`（`[full-ci]`）改为断言新显式`targetDied`和退休控件禁入。第一笔代码Android CI #4411 SUCCESS、R2失败属架构门禁与当前产品目标不符；**最终最新完整CI #4412及R2 #3992**以GitHub实际结果为准；不能把过时失败检查或docs-only skipped冒充验收。
- **重要夜间阻塞**：完整复读DemonAttackProductionAdapter→NightHostProjection→Mayor/Sage/Ravenkeeper真实调用，以及`ClocktowerNightResultConfirmation`/App夜末实际Death提交。结论不变：攻击选择不等于死亡，未经早期真实确认不能整删当前trigger并仍保证角色实际唤醒/自动AI可靠；不以临时shadow-GameState模仿替代。优先后续完整阶段重连。另SlayerTableState仍有旧alive/claimant候选门禁、其它Virgin/Mayor日间自动结果，待继续P1删除。
- **审计进度**：本批全文首次审阅现行5文件：SlayerInteraction、SlayerDraftTest、AbilityFunctioningSemanticsTest、SlayerTableState及其Test；7其他涉及文件原台账已审只复核不重计。库存不变**444项；已审217（48.87%）、待审227、审计中0**。两类跨局setup/玩家身份防重复、现行首夜/后夜AI、prompt/transport/parser/整局策略记忆/辅助自动与Session安全都保留。
- **下一步**：继续剩227源码/测试横向实际删除，重点移出旧NightDemonAttack规则前先提供真实的death-trigger行动确认入口；并审Virgin/Mayor白天自动终局、App/HostScreen/NightStepUi三个>50KB文件真实无关代码。每个代码提交必须完整Android CI/R2。

### F12-P：守鸦人/送葬者角色直选，退休最后的Host全脚本角色候选器（2026-10-19）

- **环境**：MiniMCP `clocktower`从clean/equal的`d7c9484f804f882485ba7257c12952f82b2147bd`起步，仍在`codex/minimal-host-full-scan-20261018`，PR #334仍Draft，base SHA`64be3090`不动；无merge/retarget/force push。
- **生产整删**：提交`ea6d9a03c968077299c0aea3aac043ab7514d6bf`（`[full-ci]`）整删`ClocktowerNeutralInformationPreparation.kt`（F12-O已删配对全网格，F12-P又退役其最后`roleRevealOptions`全脚本枚举消费者）及**2份独占菜单测试**`ClocktowerRavenkeeperSquareTablePresentationTest.kt`、`ClocktowerUndertakerSquareTablePresentationTest.kt`。从HostScreen真正删除Undertaker/Ravenkeeper的`displayOptions`原调用；`ClocktowerInformationStepBuilder`删除生成选项lambda与同一list分装manual/automatic/display三套的重复职责；两张实际方桌转为直接选脚本身份或AI已匹配RoleAt，保留查询seat/昨夜处决seat、真实人工覆盖、角色私密显示、原Session事件、现有AI与两类跨局轮换；新增窄`ClocktowerDirectRoleReveal`纯结构合同和合同测试，不另设可写状态与Host合法真值检查。
- **CI真实修复**：最初代码`ea6d9a03` R2 #3988 SUCCESS、CI #4408 Android编译失败：旧`automaticRavenkeeperChoice.key`字段已退役但LaunchedEffect的去重键尚未换。修复`37f7c63bcc0df93dd47918153fcfce4f04db5c7a`（`[full-ci]`）改为已存在的结构化`clocktowerInformationCandidateId`，不恢复旧DTO。最终真实代码[Android CI #4409 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39192215275)、[R2 #3989 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39192215497)，Android tests/Debug APK、ASP/Clingo/CI gate、R2 verify-boundary全绿；不能以之前失败的代码/后续docs-only skipped冒充。
- **真实比较**：GitHub从`d7c9484f`到`ea6d9a03`共10文件 **+269/-431，净删162行**（3旧文件整删、2个新的结构直接输入/测试文件；5原有文件改动），后续修复只是1行键替换。全仓本批由445删除3增加2→**444项**；原211已审删除1、两个新文件均全文审核计2，两个删掉的旧测试原待审，故**212/444（47.75%）已审、232待审、0审计中**。原已审的UI/builder仅复审不重复计。
- **仍待清理**：NightHostProjection→DemonAttackProductionAdapter预测死亡仍决定Ravenkeeper/Sage额外唤醒；真正死者在夜末另行确认，切不可把攻击目标当死者或直接删除当前触发路径；优先建立真实早期触发/确认入口再整删旧攻击规则和专属测试。App/HostScreen及NightStepUi仍超50KB、Slayer/Virgin旧自动规则、剩余232文件横向审计及P1/P4/P5未完。无真机与付费OpenAI API。

### F12-O：洗衣妇/图书管理员/调查员直接配对输入替换全候选域（2026-10-19）

- **真实环境**：MiniMCP `clocktower`、`codex/minimal-host-full-scan-20261018`、PR #334 Draft与base SHA `64be3090`。起点`0513123ef51d82766327908cbd412ad9aaf66b3d`本地远端equal+clean；非merge、非retarget、非force push。
- **实删链（不是补充facade）**：生产代码提交`de091e2480373b271325d679c87c10bbb3093e10`：完整删除旧`ClocktowerPairManualAuthority.kt`、`ClocktowerPairManualSelectionModel.kt`、`ClocktowerPairManualSelectionUi.kt`及四个直接依赖旧候选表的专属测试`ClocktowerPairManualAuthorityTest.kt`、`ClocktowerPairManualSelectionModelTest.kt`、`ClocktowerPairManualSeatPresentationTest.kt`、`ClocktowerPairInformationSquareTablePresentationTest.kt`；整个`ClocktowerNeutralInformationPreparation.pairInformationOptions`从Role×全seat二组合的预枚举路径退出；HostScreen 3个角色的displayOptions生成闭包删除、`ClocktowerPairInformationAbility`旧enum删除。现有方桌改接`ClocktowerDirectPairSelection`：说书人直接选一身份及两个实际玩家或None→只在确认时生成typed AnyOf(RoleAt) / AllOf(RoleInPlay false)。AI首夜/后夜实际pair建议可被结构验证后直接预选；无任何基于Host全候选表的反向匹配或真实性门禁，保留人工覆盖/角色truth-cue不虚构Spy/Recluse登记。保留同一onShowPlayerDisplay→Session真实收到的信息/事件及辅助/自动模式。
- **真实比较**：代码提交17文件 **+372/-1166，净删794行**（删除7文件、增加2文件；其余8文件修改）。另外CI护栏提交`526190030926eb5e52e8a2e9f85ec73e4aef148d`：R2 #3985失败因过期字符串门禁仍要求废弃PairInformationAbility enum存在；**未恢复废弃代码**，改为核新的直接输入model存在与旧PairManual三模块不回归；后续同HEAD R2 #3986 SUCCESS。
- **真实完整验收**：代码与CI门禁同树`52619003`真正完整[Android CI #4406 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39189973397)、[R2 #3986 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39189973230)，Android tests、ASP、真实Clingo、CI gate、R2 verify-boundary全部成功；首次过期门禁R2 #3985确实失败且已修正，代码提交#4405因后续推送取消不冒充通过。未付费API或真机。
- **横向补审/现行目录**：本批从原库存450项整退7个旧文件，添加2个新的直接输入生产/测试→**445项**。退役的2项原已审，另2项原待审的`ClocktowerHostPresentationModels.kt`及`ClocktowerNightFullScreenOwnershipTest.kt`首次全文并追查真实生产/测试调用且标已审，2个新文件全文审核且算新已审；原已审其它文件只复审。故现行**已审211/445（47.42%）、待审234、审计中0**。项目内两类跨局轮换、全部现有AI建议链及保密原子提交都未删除。
- **下一批优先**：继续剩余234未审全树源码，优先还存于`ClocktowerNeutralInformationPreparation.roleRevealOptions`及Ravenkeeper/Undertaker的script角色全菜单+旧候选域，夜末真实死亡确认之前的Sage/Ravenkeeper旧DemonAttack推演/Mayor预览仍不得直接整删；App/HostScreen>50KB、日间Slayer/Virgin旧自动规则与P1/P4/P5仍未完。

### F12-N：死分支、旧红鲱鱼Host单候选及夜间死亡预览只读裁剪（2026-10-19）

- **环境**：MiniMCP `clocktower`起点代码/docs HEAD`9f11548825903330d76700c08d7ccb041e933707`本地远端equal+Clean，PR #334保持Draft与base SHA`64be3090`，未合并、retarget或force push。
- **完整代码更改**：`e49972e1268b483dc273301f4287f760772d0ffd`（含`[full-ci]`）在`ClocktowerHostSelectionSemantics`直接删掉全仓仅旧测试使用的`automaticRedHerringSelectionAtBarrier`（按Host唯一legal target自动给红鲱鱼），旧测试的整块门禁断言同步删，真实联合开局AI red-herring采纳和确认自动步骤不变；`NeutralInformationPreparation`删全配对临时对象`PairInformationEffect.id`旧字符串生成，真正pair typed选项和实际AI输入仍在；`NightHostProjection`删HostScreen完全未使用的`resolvedNightDeathName`镜像、重复`nightDeathWillOccur`检查，Sage/Ravenkeeper展示仍基于同一个旧preview，无事实变化。远端代码对比**6文件+2/-103，净删101行、无整文件删除**。
- **关于不能直接整删的根因**：已全文核实夜末`ClocktowerNightResultConfirmation`供说书人确认实际死亡，发生时间**晚于**夜间Sage/Ravenkeeper的触发和展示。旧`DemonAttackProductionAdapter`的Imp/Monk/Soldier/Mayor推断仍经`NightHostProjection`筛选额外唤醒/Mayor入口；把confirmedAttackTarget直接冒充实际死亡会错误唤醒受保护/存活玩家。因此本批**未删除该生产adapter/专属测试**，下一批必须改夜间实际已确定结果在角色唤醒之前的确认/可继续流程，再整体拆出旧rule和测试。不中断实际游戏，不制造影子Session死亡。
- **源码横向首次审计**：审阅并核对消费者11原待审文件：`ClocktowerCurrentResidualScopeV2`、`GlobalFirstNightDecision`、`GlobalFirstNightScalarDecision`、`NeutralInformationPreparation`、`HostSelectionSemantics`、`PairManualAuthority`、`PairInformationSquareTableUi`、`RedHerringAutoAdvancePolicyTest`、`DemonAttackDawnFactsTest`、`MonkRetargetRavenkeeperDeathTest`、`DemonAttackProductionAdapterTest`。其中特别标注前/后夜AI真实requests和人工信息实际消费者，不删除生产AI；其余全枚举pair domain和预览攻击旧门禁待解耦。原审完的NightHostProjection/HostScreen/Test只复审不重复计数。现库存**450、已审209（46.44%）、待审241、审计中0**，逐文件结论沿用同一台账。
- **真实代码验收**：真正完整[Android CI #4403 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39188307015)、[R2 #3983 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39188307113)，Android tests/ASP/真实Clingo/CI gate 全部completed/success；不把后续docs-only检查看作代码重新编译。未使用付费API或真机。现有跨局阵容/玩家身份双重轮换、联合开局/续战略AI/API/prompt/schema/preselection/双模式、Session私密展示与幂等均未改。
- **下一步**：优先真正消除HostScreen/App内旧预览/Slayer/Virgin/Mayor胜负逻辑及双文件>50KB负担，同时保留Sage/Ravenkeeper/AI可玩入口；继续剩余241条横向遍历。F12-N不是整个P1/P4/P5完结。

### F12-M：Klutz规则自动结算整删与横向UI/AI合同复核（2026-10-19）

- **真实环境**：`clocktower`与远端同一clean分支`codex/minimal-host-full-scan-20261018`，基线`c3ea51af96a2c44429bd8f4782e0c6eb61d6c2c1`，PR #334继续Draft、原base`64be3090`，无合并/retarget/force push。
- **实际删除**：代码提交`ba8ea494acc9aada4e4ead4168734de4f60d8894`完整整删`clocktower/rules/NoGreaterJoyKlutzChoiceRuleV1.kt`，并改App确认KlutzChoice只提交已选结构化座位事件，**不再读取真实阵营自动判邪恶获胜/弹终局**；然后统一继续原Dawn/下一夜，保留方桌选择、real Death、learned and chosen事件顺序、Session事实及重复ID保护。NoGreaterJoyKlutzHistoryProducer的choice不再使用旧Rule对阵营、存活和脚本枚举合法性否决实际选择，但仍要求已知座位与已记录learned关联，缺失旧史不捏造事件。真实玩法由说书人裁量；没有新建Rules包装器或影子状态。
- **测试与代码证据**：LiveKlutzScriptReachabilityTest删旧AI无关的阵营胜负、手工动作合法候选与模拟脚本异常专属断言，保留现用脚本roster、实际Death证据及真实learn→choice chronology/重复拒绝，并检验选择曾死亡的真实已知座位作为事实也不被旧Host规则拒绝，未知座位不进入事件。GitHub真实比较**4文件+30/-162，净删132行，其中1生产规则文件整删**。真正完整Android **[CI #4401 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39185744458)** / **[R2 #3981 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39185744492)**，Android tests、ASP、Clingo、gate全绿；无真机、无付费API。
- **本批横向新审**：原待审7个生产、6个测试全文并查真实引用（DayNominationGesture+Test、DayOverviewTableState+Test、DemonBluffPresentation+Test、JointOpeningDisplayMatcherV2+Test、JointOpeningReviewScreen、DirectBooleanResult+Test、InformationStepBuilder、LiveKlutzScriptReachabilityTest）。AI配对/数字、开局review、恶魔掩饰三选和真实Drunk显示、Host事实投影的现用入口全部保留；只把旧手势alive-only与信息manual/automatic候选域标待解耦。原已审的App/NoGreaterJoyKlutzHistoryProducer复审不重计；退役的规则文件原已审从现行目录退出。当前库存 **450项、198（44.00%）已审、252待审、审计中0**，逐条在同一原台账修改，不把失效旧库存当源码。
- **下一批**：必须继续横向剩余252项，优先App/HostScreen>50KB真正无用职责与DemonAttackProductionAdapter→Sage/Ravenkeeper夜间预览依赖；不能在未确保现有真实唤醒/信息确认入口下整体删预览。另核Day/Slayer/Virgin及其它自动规则终局，保留事实、AI、跨局阵容和个人身份轮换。P1/P4/P5未完。

### F12-L：全仓横向补审和不可达夜间信息选择分支减法（2026-10-19）

- **真实环境**：开发机 `clocktower` 分支 `codex/minimal-host-full-scan-20261018` 起点 `b4c979ac143d25c455e0d7011705f0800cd1690f`、远端一致且Clean；Draft PR #334 base `64be3090` 原样，未merge/retarget/force push。F12-K上一个完整Android代码基线 `9c7c57a` CI #4396/R2 #3976 SUCCESS，起点文档CI #4397/R2 #3977 Android skipped不冒充编译。
- **本批真实代码**：提交 `c420bd9e2dd4b0e7ebaa8436aa956a55f4f101e9` 删Ravenkeeper/Undertaker chooser从不生成的`LegacyUnreliable`/`Direct`来源、无效null分支及仅服务它们的legacy display resolver；保留真正`DisplayOption`及同一`onShowPlayerDisplay`向Session公布。提交 `4e1c9d40e72acf6a08abb3f6a843c3d0be9d091b` 整删`ClocktowerNumericInformationOptionPreparation.kt`及唯一专属`ClocktowerNumericInformationOptionPreparationTest.kt`：旧`previousClocktowerUnreliableNumber`仅测试自引用、无一处生产调用；当前真正的AI战略记忆/记录观察与数字手工/自动输入在别的现用路径，未修改。
- **经GitHub远端实际比较** `b4c979ac..4e1c9d40`：7文件 +8/-155，**净删147行**，其中**1生产文件与1专属测试文件整删**；另外四个生产文件/一个保留测试修改。不新建兼容逻辑或第二GameState。
- **横向全文复核**：原待审9项首次读完全文件并核对真实生产消费者，分别为`ClocktowerDemonAttackProductionAdapter`、`ClocktowerNightStepUi`、`ClocktowerPlayerDisplayResolution`及其Test、`ClocktowerNightFullScreenOwnership`、`ClocktowerNightPresentationRole`、`ClocktowerPhaseStorytellerAdapter`、`ClocktowerKlutzTableState`和`ClocktowerNightStepMaterializerRegistry`。额外2个原待审且现已整删的生产/测试从当前库存剔除，已审UI辅助与测试仅复核不重计。现行唯一源码库存 **451项、已审186（41.24%）、待审265、审计中0**。相应逐文件结论直接替换下表原路径，不开新台账。
- **剩余真实风险与工作**：DemonAttackProductionAdapter的攻击/Monk/Soldier/Mayor规则死亡预览仍透过NightHostProjection决定Sage/Ravenkeeper唤醒与MayorRedirect入口，尚**不可无替代删除**，不得将预测当实际Session Death；Klutz日间规则终局及NightStepUi manual/automatic候选域仍需剥离。两个>50KB App/HostScreen仍超限，P1/P4未完。现有开局阵容和个人身份防重复、首夜/后夜所有AI/解析/预选/模式、唯一Session与事件和隐私/版本安全不改。未付费API/未真机。
- **真实代码验收已完成**：两次代码提交均含`[full-ci]`。GitHub确证`c420bd9e`完整 **CI #4398 SUCCESS/R2 #3978 SUCCESS**；最终代码`4e1c9d40`完整 **CI #4399 SUCCESS/R2 #3979 SUCCESS**，PR精确HEAD的Android tests、ASP contract tests、Real Clingo cross-validation、CI gate全部completed/success。两次是真正代码级检查，后续docs-only CI不能替代。

### F12-K：Sage人工直选两座位替代全配对枚举，完成本阶段并切新对话（2026-10-19）

- **产品决策严格保留**：跨局角色/展示阵容尽量不重复，以及同一玩家跨局尽量不拿重复角色，两项均保留完整正式算法与持久化历史；现有AI的首夜/后夜、prompt/API/JSON/parser/strategy memory/预选及AI_ASSISTED/AI_AUTOMATIC、唯一Session与结构化实际公布事件均不删除。
- **真实Sage生产改造**：原Host为每个Sage枚举全部候选pair（排actor、计算真假、分真/假来源）→Sage UI旧候选下拉→全配对菜单中确认已退休。现在NightStepUI接收实际AI建议的已选结构化two-seat pair（如果有），SageSquareTable用同一方桌高亮两个座位、一键采纳；没有有效AI建议或说书人切换人工时，直接点击两名实际座位、必要时重新轻点更改，只有两不同且范围内的座位可确认；走现有`onShowPlayerDisplay`和Session实际私密观察流程。Sage的真人`actor`醒夜信息、受毒/Drunk标志与隐私仍保留。
- **连锁移除旧Host伪推荐**：从SageStepMaterializer删除`clocktowerSageDisplayOptions`全pairs生成、isTruthful TruthfulOnly分支及无用HighPressureSuffix；从HostCoreSemantics整删唯一服务Sage的`storytellerPairHint`，HostScreen不再依据真实恶魔自动拼“恶魔＋第一个其他人”作为预设线索。初始Sage step不再有虚构已选pair命题；只有明确采纳的AI配对或确认的真人pair作为实际展示，人工结果使用typed two-seat AnyOf，selectedInformationTruthful保持未知，不从真相推说书人选择；首夜显示/历史基础设施不受影响。
- **测试收敛**：SageMaterializerTest删Host合法/真实配对枚举专项用例，新增真人实际两座位的命题、原始步骤无预设配对、重复/范围错误拒绝；SageSquareTablePresentationTest删LegacyUnreliable候选菜单断言，保留展示typed seat、防文案解析、actor独立高亮/座位点选及纠错测试。没有新增专属“合法结果”系统或新的Shadow GameState。
- **真实代码提交/验证**：起点`4b29f45117119299f6ed3c395b3f8a4e9556fa35`。先代码`9d962565b18e53b41713d639129e1ec8f6af253e`，**CI #4395 / R2 #3975 SUCCESS**；后代码`9c7c57a7369d9ee76324b009d26d088a66660923`，**[CI #4396 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025505)**（Android JVM所有单元测试、Debug APK、ASP、真实Clingo、CI gate均成功）、**[R2 #3976 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025502)**。GitHub比对共7文件**+163/-558，净删395行**，0文件整删；没有真机/真实付费AI验证。
- **唯一台账进度**：代码文件数453未变；新增全面首次审阅`ClocktowerHostCoreSemantics.kt`1项，原4项Sage已审生产/测试和已审HostScreen只复审不计新增；NightStepUi约40KB虽改Sage调用但仍保持整文件待审。当前**177/453（39.07%）已审，276待审、0审计中**。后续仍须真正逐文件遍历，不把F12-K称整个P1/P4/P5完成。
- **下一轮清理顺序**：先从276项横向继续，优先审NightStepUi其它manual/automatic domain与legacy unreliable角色候选、DemonAttackProductionAdapter只读预览、Sage/Ravenkeeper夜间触发推导与日间Klutz结算；保留主流程，按照“删除不会妨碍实际游戏继续”移除旧规则；两个>50KB App/HostScreen需继续实际净删。Audit ledger是唯一库存；PR #334保持Draft原base64be3090，不merge/retarget/force push。后续文档独立提交后精确完整Android代码证明仍为9c7c57a，docs-only CI不能替代。

### F12-J：去除Host目标规则候选包装器、singleton专用UI与Mayor死分支（2026-10-19）

- **最新保留硬边界**：阵容跨局多样性（SetupDiversitySelector/History/TemplateRepository）及同一玩家角色轮换（DealPlanner/RotationHistory Store）全部保留，无改动；同样保留唯一Session确认/事实、现有AI开局/后夜、API/parser/strategy memory/预选、真实手工公布和身份保密。
- **代码提交和完全独立验收**：从洁净一致的 `2210130d9f89fc03de0f0345d809135748ef7718` 开始；首提交 `8582fe7a435c2f4c01c84b3a9468f25bc63c4541` 完整Android **CI #4392 SUCCESS** 和 **R2 #3972 SUCCESS**；第二提交 `f822ef5b9174c2e5d38b4704dca68af1042c9c72` 的 **CI #4393 SUCCESS**、**R2 #3973 SUCCESS**（Android JVM所有单测+debug APK，ASP、real Clingo及CI gate）。GitHub compare `2210130d..f822ef5b` **13文件+13/-243，净删230行**。无本地真机/付费API测试、未对原stacked Draft PR #334改base或merge。
- **真实整删4文件**：`ClocktowerNightTargetLegality.kt`（Monk actor过滤包装器、Ravenkeeper仅透传当前卡片的虚假helper）、`ClocktowerRuleOnlyCompactChoice.kt`（基于legalChoices.singleOrNull的特殊展示授权）及两份只验证上述旧实现的单独测试。生产UI直接基于当前cards完整座位执行结构选择：Ravenkeeper任何实际座位仍可选；Monk当前可手动选择所有实际座位（不再由Host预过滤自己，遵守官方规则由说书人负责）；HostScreen对应的已选择目标验证以真实存在的座位为限，不引入新的规则校验。真实玩家目标提交、NightStep前进及AI仍在。
- **死UI同步清理**：Undertaker、Sage、Ravenkeeper 3个专用方桌删除“只有一项可选时跳到compact只读展示”的分支，统一复用现有人工确认/AI建议界面；`ClocktowerBeginnerCompactNightUi.kt`整删随之失去唯一消费者的`ClocktowerBeginnerSingleTargetRevealDialog`，保留实际被调用的其它几个紧凑界面。Sage旧全pair生成和ResultChoices下拉仍真实使用，**本批不直接删Sage整组**以免失去人工指定/公布能力；后续先接通直接座位pair输入再退役组合枚举。
- **Mayor独立后续清理**：`ClocktowerNightRulingPresentation`生产只有`automatic=false`，删除永不可达的automatic展示/Imp继任UI解释文案/冗余explanation，保留Mayor当前方桌、`Mayor dies`按钮、选择重定向目标/实际事件及确认，不恢复此前的singleton自动裁量。同步更新旧专属自动UI断言。
- **逐文件审计账**：基线 **171/457**，退役四路径中1已审+3待审，新增6条真正全文审阅/现行consumer分类，现行**176/453（38.85%）已审，277待审，0审计中**。HostScreen/SageSquareTable已审只复查不重计，NightStepUi43KB等尚未完整横向审阅继续保持待审。真实文档提交另计；docs-only CI不能替代上述最终代码SHA的完整Android结果。
- **后续优先**：Ravenkeeper/Monk现有目标提交仍有其它旧规则门禁（如其它角色目标/死亡预览），Sage全枚举/角色候选需先可用人工pair路径，按整体游戏可继续性删减。超50,000B的App/HostScreen及剩余Host Rules P1/P4未完成。全部功能不以存在生产引用直接判必要，也不因要删规则而破坏唯一Session或两类防重复。

### F12-I：按用户纠正保留开局两类防重复，继续旧规则门禁减法（2026-10-19）

- **2026-10-19 用户明确保留两条产品功能，优先级高于“能开局就删”通用规则**：①连续局尽量避免相同开局身份/角色阵容（SetupDiversitySelector、History、TemplateRepository、TB旋转历史到可见Roster的投影）；②同一玩家跨局尽量避免重复分到相同身份/角色（TroubleBrewingSetupDealPlanner中minimumRotationCostAssignment、近期PlayerStartingIdentityHistory及持久化Store/Record）。两者并非Host的游戏规则合法候选门禁，而是实际体验，不以开局技术上仍能启动为删除理由。早前本轮临时改掉的TB preset selector、删除的6个setup生产/测试文件、TB历史投影/引用和相关测试，**全部按原HEAD精确恢复**，经`git_state`验证**工作区clean、没有形成commit或push**；不因此重计新增审计。
- **清理实际完成（一）**：PlayerHistoricalTimeline.kt整个旧player-facing public world replay仅有测试使用，app/src/main无消费者；其4份混合测试删shadow replay断言，保留实际ActionFactTimeline、RecordedObservation、Session已确认事件、NoExecution/NoNightDeaths/RoleChange、Stale/atomic及真实AI结构化事件检查。删除1生产文件169行，没有建立第二影子投影。
- **清理实际完成（二）**：StorytellerDecisionAuthority.kt的“合法候选只有一项→Mayor自动裁量”及ClocktowerAutomaticRulings.kt中自动target与onNext UI副作用只有ClocktowerNightStepUi消费者；已取消自动选择/跳步，真实Mayor选择、确认、Session事件与现有AI推荐入口保留，用户可以正常选择并确认。连同2专属测试整删，未退役Mayor实际事件。
- **清理实际完成（三）**：ClocktowerFortuneTellerSquareTableUi已有生产调用传入`setOf(true,false)`，旧ruleDeterminedFortuneTellerResult仅测试单元素自动判定；已删该单独host rule分支和1份专属旧测试，界面持续允许人工YES/NO两值、AI建议置顶但不阻止实际确认。原方桌、座位选取、红鲱鱼标记与AI信息记录未删除；保留/修订真实选择UI测试。
- **验收**：起点`f8cdf8af065e0795d9147f87e2b56d236f66eccd`。先提交`eb702ff6387c8b4e28425986e0e089401028130a`，**CI #4389 / R2 #3969 SUCCESS**；再提交`69f1bf31df067e3c0d586f6e4a5f93d77d3781cc`，**[CI #4390 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608692)**（完整Android JVM单测+Debug APK、ASP、真实Clingo、gate全成功），**[R2 #3970 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608628)**。GitHub compare首→终共13文件，**+29/-610、净删581行**；3生产文件+3专属测试完全删除；未调用付费API/真机。删除原因与实际消费者逐条核查后，唯一台账463-6=**457**路径，旧已审路径退役4、旧待审退役2，新增完整审阅3，现**171/457（37.42%）已审、286待审、0审计中**。
- **剩余待清理**：Sage仍由生产UI消费多对候选，尚无等价的直接两座位人工输入，因此**不在当前批直接删Sage以致丧失实际公布/事件**；下一步先提供/复用结构化人工Pair确认再整组清退全组合筛选。另审Mayor实际target menu的合法性拒绝、Ravenkeeper/Monk选座位门禁、Klutz自动结算与夜间信息preview。继续逐文件横向审计，不误删TB两个开局防重复、AI策略/传输、唯一Session安全事实。超50,000B App及HostScreen仍未完成P4，Draft #334维持原stack不合并/force push。
- **文档提交另计**：此处代码基线`69f1bf31`已有完整Android CI；后续docs-only的CI不能冒充新代码编译。

### F12-H：以游戏可继续性优先，退休间谍独立魔典与关联冗余（2026-10-19）

- **所有者最新删除准则**：旧功能仍有生产调用，并不证明它需要存在。如果删除以后游戏依然可以正常进行（有共享方桌、说书人人工操作、现有Host事实与AI功能路径可以取代），整条旧调用、DTO/JSON/规则/专属测试一同删除；只有不保留就无法继续玩、无法维持唯一事实/安全/私密信息或破坏现行AI推荐的功能才保留。避免小概率特例的过度工程、避免为了通过旧测试重建已废弃界面。
- **正式间谍交互**：首夜及后夜Spy现在都由原NightStep生成纯文本“唤醒X号，给他查看当前方桌；收回手机、闭眼、点击下一步”，`displayKind=None`，不打开专用player reveal，也不经Spy特有观测构造+保存整个GrimoireState。沿原`ClocktowerNightFullScreenSurface.Spy`→只读方桌NightActionSquareTableDialog（`enabled=false`）→原`onNext`继续，未增加新的展示/确认按钮。中毒间谍由说书人自己处理，不专门构造魔典可见性规则/停止界面；通用状态事实仍在。清除Spy触发“红鲱鱼必须提前确定”的旧依赖，Fortune Teller自身前置设置仍要先于它的线索步骤。实际游戏角色、间谍登记裁量和普通玩家私密公布未删。
- **本批实删两生产文件**：`clocktower/epistemic/GrimoireReminderToken.kt`（独立physical/reminder-occurrence Resolver/Refs）及`clocktower/domain/PairInformationOutcome.kt`（唯一引用就是自身的废弃pair DTO）。另外清除旧`InformationProposition.GrimoireState`、`GrimoireSeatView`、`GrimoireTruthBinding`、专用Proposition JSON编码解码/defensiveCopy、`ClocktowerDisplayKind.Grimoire`与专用Spy player page/按钮/Host参数、首夜Spy-红鲱鱼额外观察依赖和UI中的中毒Spy专例。没有删除通用InformationProposition/Observation/Session或真正方桌，也未删除Spy在日间的必要手工登记裁量。
- **本批整删四个只保护已取消功能的专属测试**：`ClocktowerGrimoireRevealGateTest.kt`、`ActiveRulesetGrimoireReminderTokenTest.kt`、`GrimoireReminderTokenBoundaryTest.kt`、`GrimoireTruthBindingPersistenceTest.kt`。原先标记“必须保留”的TruthBinding在所有者正式取消产品后不再构成保留理由，现行台账移除该路径；有效通用Observation schema/私密性测试保持。两个混合first-night/semantic-model测试去已退役分支；第一次完整CI #4386暴露`ClocktowerProductionFirstNightFlowTest.kt`还强制“红鲱鱼在Spy前”旧合同，删除专属中毒Spy测试并修正普通FT前置顺序，而非恢复Spy依赖。
- **GitHub代码差异**：起点 `fc80fd1ace64dbf6725e40d92bef23664f5bbde7`，首提交 `7982b6541978cfaf2bd341f116f377e2edee292e`，修正历史测试提交 **`f7f47d1e28b5fd74db61179a3c22736f6ca04d3d`**，17+1文件共18文件，**+20/-869，代码净删849行**；2生产+4旧测试文件移除。**[CI #4387 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098629)**（Android full JVM unit tests + debug APK、ASP、real Clingo、CI gate全部通过）、**[R2 #3967 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098663)**（均已核精确代码SHA）；无真机或付费API验证。最外层App和HostScreen仍>50KB，P1/P4未全部完成。
- **进度与历史纠正**：此前台账 `177/469` 中5个已审文件及1个未审文件本次删除并退出当前路径表，因此现行 **172/463（37.15%）已审、291待审、0审计中**。不把已删除专属代码算成剩余源文件或将重读已审生产路径算作新审计；历史删改证据仅保留在本节。下批按同一产品必要性标准优先清退PlayerHistoricalTimeline仅测试消费的影子投影、Mayor/FT旧候选自动授权、Sage旧pair候选和TB跨局组局评分，先核必要Session/AI事实出口，不因生产调用自动保留。
- **运行约束**：Linux MiniMCP `clocktower`同名Draft PR #334，base仍 `64be3090`；不合并、不retarget、不force push，先看精确CI/R2。之后文档单独提交，代码验收SHA应指 `f7f47d1e` 而非docs-only的最新HEAD。

### F12-G：退休Formal世界镜像与专属测试，保留唯一事件/私密观察（2026-10-19）

- **实际代码提交**：从干净且远端同步的 `4da038498452a932717bd3a19c066d2d6e8fdb1c` 实施，经MiniMCP修改、提交、推送至Draft PR #334。第一版 `0d8f145bb07b2f01593b0f9b66d0d068a24255ef` 的CI #4383因误删仍由Spy魔典不可变容器使用的`java.util.Collections` import导致Kotlin编译失败；只补回该import，最终可执行代码SHA **`8644e7575b67d9f3bef6cc269c7c93bca4332bf1`**。精确代码 **[CI #4384 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056035)**（完整Android单测+Debug APK、ASP、真实Clingo、gate SUCCESS），**[R2 #3964 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056032)**；不以失败中的SHA冒充通过，无本地/真机/付费API测试。
- **整组真实退役**：`EpistemicSemanticModel.kt`删除FormalPlayerState/FormalGameState/PlayerKnowledgeSnapshot与只为旧Formal世界维护的全ActionFact影子canonical fingerprint/旧seats验证；`EpistemicSemanticJson.kt`同步删除该双镜像及Shadow ActionFact、Ruleset、Formal global timeline的编码与解码；`ActionFactTimeline.kt`删除没有当前消费者的FormalActionTimelineBinding；`EpistemicObservationLog.kt`移除已无消费者的worldReplay及Formal重载bindTo。完整删除旧`FormalActionTimelinePersistenceTest.kt`，收窄两份混合测试只保真实命题、recipient保密、时间线、历史迁移及Spy魔典。
- **实际规模**：GitHub对比`4da03849..8644e757`为**7文件、+8/-737，净删729行**（不计文档）；删除1个旧测试文件，生产Kotlin 241不变，测试Kotlin 178→177，原当前树总文件数878→877。App159449B、HostScreen99682B仍超50000B，不借搬迁文件假称P4完成。
- **保留合同**：`ActionFactTimeline`及`TimelineBoundActionFact`仍消费于Session和跨Action/Observation global chronology验证；`EpistemicObservationLog`、`RecordedEpistemicObservation`、`InformationProposition`、`EpistemicSemanticJson`真实私密/公开信息、proposition hash和snapshot绑定保留。Host唯一事实/原子事件、AI首夜/后夜prompt/transport/parser/strategyMemory/preselection/操作模式未动。ASP冻结fixture中旧FormalGameState字样属文档/离线oracle数据，不依赖Android Formal类型。
- **本批完整新审计7路径**：ModelTest、ObservationTimelineBindingTest、CrossTypeTimelineCompatibilityTest、ActiveRulesetGrimoireReminderTokenTest、GrimoireReminderTokenBoundaryTest、GrimoireTruthBindingPersistenceTest、ShownRoleAtSemanticContractTest 均全文读完、核对真实消费者与代码变化，并逐路径给分类；四个已审生产epistemic文件本轮复审/缩减不重复计数。删除一个原待审专属测试，范围470→**469**，审计进度170+7=**177/469（37.74%）**、0审计中、**292待审**。历史删除文件只保留在本节，不留在当前库存表冒充待审。
- **继续**：先清理已确认只有测试调用的`PlayerHistoricalTimeline`及其专属旧公开投影（迁移实际保密回归至生产读取路径），再审Mayor/FT仅存Rules singleton门禁、TB跨局rotation评分与UI Rule-only校验；但不能误删真实Session、AI及角色身份/隐私。跨项目TBGS JSON合同尚须外部核查，U1自定义脚本入口范围仍需所有者裁定。Draft #334保持原base，不merge/retarget/force push。

### F12-F：跨模块全源审计 + 旧AI实验整组退出（2026-10-19）

- **最新代码**：MiniMCP clocktower独立开发机与origin基线`569759560837591695168244b1a4eeeca21fb574`一致、工作树原本干净；本轮先移除历史AI实验完整无生产消费者链，单独提交并推送**`e6faea27bb28a571b45669bf2357af0b11e4f863`**（`[full-ci]`）。保留同名Draft PR #334，base `64be3090`，main `61cd6796`，不merge/retarget/force-push；文档在后续独立提交。
- **已执行整组删除**：`tools/prod_global_3b2_manual_probe.py`、`tools/prod_global_3b3_semantic_v2.py`、两个同名`tools/tests/test_*.py`专属测试与只绑定旧experiment/feature分支的`.github/workflows/p1-wire-fixture.yml`（2工具脚本+2专属测试+1过时工作流）。`.github/workflows/ci.yml`仅移除调用这两个旧Python unittest的6行步骤；后续再检app/src、tools、.github目录无任何引用。其结构/角色/候选约束均为与现行App不相连的冻结实验，不是生产AI请求/解析/strategic memory。历史fixtures/docs保留可追溯，未发付费API请求。
- **GitHub精确代码差异**：对比 `56975956..e6faea27` 得到**6文件、0新增/1087删除行**（2实验脚本627行、2测试369行、旧工作流85行、ci步骤6行），旧台账对应删除文件共51,928B、CI删除约237B，净约**52,165B**（未计文档）。未改Android生产/测试Kotlin、canonical Session、事实日志、模型API、UI或权限边界。当前预计878跟踪文件、241生产+178测试Kotlin，App159449B、HostScreen99682B仍超50,000B。
- **跨目录新增完整语义审计32项**（不计已删5项及旧已审条目）：epistemic ActionDraft/Model/Json、TB快照codec、推荐候选authority、TB/NGJ组局/Shown/Drunk/模板/跨局rotation、AI三模式与设置、Android诊断/Manifest FileProvider、离线工具/CI与关键AI/隐私测试；逐路径分类、生产/测试引用和删改安全边界详见下表。范围`475-5=470`，本轮已审`138+32=170`：**170/470（36.17%）已审、300待审、0审计中**；路径已去重。
- **下一步**：先从活跃InformationProposition/RecordedObservation/JSON剥离已经无人生产消费的FormalGameState/PlayerKnowledge旧世界模型，而不是删除核心观察/隐私/codec；再审证据完备的孤立PlayerHistoricalTimeline及其测试迁移、Mayor/Sage/Ravenkeeper/FT old host-rule-only门禁、TB setup preset候选与跨局rotation算法。共享TBGS JSON codec当前只有测试生产树零调用，EvidenceLab/WebHost跨项目契约尚需核对，未贸然删除。U1无生产入口的homebrew导入仍待所有者决定，不阻塞主任务。
- **验证**：源码非付费调用/无真机/无本地完整Android执行；远端代码`e6faea27`触发完整Android **[CI #4381 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379709)**（完整Android JVM单测+Debug APK、ASP与真实Clingo、CI gate全部SUCCESS）及独立 **[R2 #3961 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379711)**（已核精确代码HEAD/Job steps）；其它现行AI/Host不变量仍须以后真机和生产响应验证。

### F12-E：开发机同步后下一批全文件横向语义审计（2026-10-18）

- 在干净工作树上用MiniMCP fetch/精确快进从旧本地a2cbcac82b457b5a56abf3833b604d9c4fdd74f7到远端当前2ce0e897898e8f040c3bef8dcbda20186e2e4689。GitHub PR #334仍Draft/base 64be3090，main 61cd6796。同步后CI #4379/R2 #3959 SUCCESS，但Android tests skipped；完整Android最近可证明的代码仍0f904f70 CI #4377/R2 #3957。
- 新增 **16条** 首次全文阅读+实际消费者核查结论，分布于epistemic 4、setup 5、Android平台模型2、独立CI 2、专项测试3；3个catalog文件原台账已审，不重复计数。现 **138/475（29.05%）已审，337待审**，未审与重复读严格区分。本批生产删除0、专属测试删除0、旧链移除0、净源码体积缩减0；App159449B和HostScreen99682B仍超50000B。仅文档审计，未运行Gradle/真机/付费API。
- 生产实证：EpistemicObservationDraft/Log仍是Session与AI所消费的唯一已确认观察记录/隐私合同，不整删；PlayerHistoricalTimeline.project没有生产调用，迁移高价值公开历史保密测试后作为整删目标；FortuneTeller singleton rule-only测试只保旧Host合法性门禁。setup自动生成/历史评分/模板/醉汉选择仍有真实生产开局消费者，解耦后才整块退役；Android Application为manifest反射入口，不能被无Kotlin引用误删。catalog先前审计结论不改。
- 后续继续横向未审epistemic codec、setup/catalog、平台transport、tools/CI/测试，再组织孤立public replay和旧Rules-only UI/测试完整退出。U1自定义脚本导入范围仍待所有者决定，但不阻塞其余工作。

### F12-D：恢复跨模块横向审计（F15实现后的独立只读批）

- 按所有者最新提醒恢复全文件遍历，不沿夜间链：新增全文核查6项，跨epistemic合同/唯一时序、setup比例/来源、独立开发工具及其测试；实际消费者和保留/退休边界见原表对应路径。证据代码HEAD `0f904f7031770bd2f12acea4299aa9b1d5c8e5c0`，与F15通过完整CI的代码相同，本批只修改文档。
- 新增2项必须保留、1项必须删除（有真实开局消费者，先断开才能执行）、3项需要解耦。不是把名称含candidate/实验就直接删，也不从当前测试存在反推生产必要。该批生产/测试删除0、净代码缩减0；F15实际删除和体积单独见上节。
- 累计当前 **122/475（25.68%）**，0审计中、**353待审**；F15完工时为116/475，本只读批新增6。完整遍历仍未完成，下一批继续横向未审epistemic/codec、setup、平台/transport、tools/CI和测试。U1仍未决，无新增所有者阻塞。
- 本地明确运行 `python -m unittest discover -s tools -p test_ai_conversation_probe.py`，5 tests OK；先全文确认仅纯prepare/check调用，并用PYTHONDONTWRITEBYTECODE禁写缓存。没有运行CLI send、访问凭据、发起付费调用或把历史实验文档当现行指令。Kotlin代码验收见F15精确SHA，未新增设备验收。

表内路径以仓库根为基准。首批71条的证据 HEAD 为上列 `cfe258b1`；F12-B新增条目在行内明确标注 `22716fa4` 完整SHA（两者之间仅文档变化，产品代码相同）；“待审”的 `—` 表示没有最终结论，“审计中”不计完成；“静态”验证同上，后续运行结果另补，不能从历史 CI 推断新改动已编译。

| 文件（当前完整范围，每路径恰好一次） | 字节 | 状态 | 最终分类 | 真实消费者 / 证据与处理边界 |
|---|---:|---|---|---|
| `.gitattributes` | 296 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/api0-offline.yml` | 835 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；独立PR路径及手动离线触发gsp_api0_responses与unittest；与当前Android/Session/AI生产执行分离，后续核工具后决定是否实验整组退休；未运行。（静态，未运行新增测试） |
| `.github/workflows/ci.yml` | 11704 | 已审计 | 需要解耦 | GitHub push/PR/dispatch→Android JVM/APK、ASP、Clingo、gate；保留 exact-head 分类、full-ci、Android和独立gate；ASP/Clingo 仍被广泛路由但旧引擎已删，须读oracle工具实际合同再决定退役哪些job/触发路径，不能依据绿灯维持旧Rules。（静态，运行结论仅明确列出者） |
| `.github/workflows/field-test-apk.yml` | 13182 | 待审 | — | 尚未形成有证据结论 |
| `.github/workflows/prod1-gateway-offline.yml` | 990 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；独立PR路径/手动离线跑gateway/latency工具和unittest，密钥为空；不等于App直连模型产品验收，需与工具一起判断保留必要性；未运行。（静态，未运行新增测试） |
| `.github/workflows/r2-write-probe.yml` | 14336 | 已审计 | 需要解耦 | F12-O解决R2 #3985旧enum存在性门禁冲突：不恢复废弃ClocktowerPairInformationAbility，而核新的DirectPairSelection存在且旧PairManualAuthority/SelectionModel/Ability不再出现在生产。新R2 #3986成功；其余结构门禁仍后续按实际职责更新。 |
| `.gitignore` | 233 | 待审 | — | 尚未形成有证据结论 |
| `app/build.gradle.kts` | 2215 | 已审计 | 必须保留 | settings.gradle.kts include(:app)、CI Android/FieldTest jobs；实际唯一 Android 应用构建、依赖、版本、17字节码及 JVM测试入口。testFast 当前无 exclude，沿用 debugUnitTest 的 classes/classpath；testFull dependsOn testDebugUnitTest，不用历史排除清单推执行范围。（静态，运行结论仅明确列出者） |
| `app/src/main/AndroidManifest.xml` | 1369 | 已审计 | 必须保留 | Android打包/启动器→Application/MainActivity/FileProvider；真实平台入口、INTERNET与私有debug FileProvider，反射声明消费者不能靠Kotlin零引用判死；密钥另在noBackup/Keystore，allowBackup 的游戏隐私范围待对应存储消费者核查。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/rules/trouble_brewing.json` | 3031 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/assets/scripts/no_greater_joy.json` | 548 | 已审计 | 必须保留 | BuiltInCatalog.loadScript→Parser/Normalizer→Flow/Setup/AI；12条：1 _meta+11 official role引用，包含首/后夜顺序；无custom definition，NGJ活跃产品不因清理门禁退出。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/scripts/trouble_brewing.json` | 894 | 已审计 | 必须保留 | BuiltInCatalog.loadScript→Parser/Normalizer→Flow/Setup/AI；23条：1 _meta+22 official role引用，包含首/后夜顺序；无custom definition，不能随旧Host Rules引擎删当前剧本内容。（静态，运行结论仅明确列出者） |
| `app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json` | 331689 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/assets/setup/trouble_brewing_setup_presets_v3.schema.json` | 1650 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppClocktowerLandingScreen.kt` | 6026 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppDealScreens.kt` | 13556 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppGameModels.kt` | 464 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppGameReviewScreens.kt` | 12093 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt` | 10085 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/BotcAiTrace.kt` | 3982 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App开局联合模型请求调用firstNightStage/Event，ADB只写allowlist stage/原因/计数，保护在线调试；剩余未被调用的postcommit旧事件helper可独立裁剪。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt` | 159449 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；MainActivity.onCreate→CampBoardGameHostApp→JudgeScreen；Session/开局/续规划/密钥 stores；已按3121行覆盖根组件。保留设备配置、setup交付、Session单writer回调、联合开局及续规划/记忆/预选/异步key。L1353/1967醉汉候选回匹配、1747自动继承、2445Slayer效果、2606Virgin、2691处决/毒到期、2851Dawn推导、3075自动终局应退出Host规则责任。ArtistUsed/Claimed、lastExecutedName/pending身份/终局仍有Compose实际语义，需回到唯一事件；UI草稿不误当第二canonical状态。先成组断开规则计算和重复事实，再按真实职责缩至<=50000B，不机械抽facade。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApplication.kt` | 367 | 已审计 | 必须保留 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；AndroidManifest.xml经android:name反射实例化Application，onCreate安装DebugFlightRecorder；无Kotlin直接调用不能判死。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt` | 987 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；Team/Phase/NightAction/DisplayKind/Script在Host UI/Flow、AI matcher、catalog广泛使用；部分类别与Domain重复，但不可整文件删除。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerArtistTableState.kt` | 2237 | 已审计 | 需要解耦 | F12-R全文+ArtistInteraction：玩家实体seat映射有效，但claimantCandidateNames和已使用真实Artist禁止UI选择是Host规则候选门禁，F12-R作为下一批目标，以已知物理座位替代。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightUi.kt` | 5518 | 已审计 | 必须保留 | F12-J全文核查现用consumer：已切除无人调用的BeginnerSingleTargetRevealDialog；现用AbilityDialog、ReadOnlyRevealDialog、TwoTargetRevealDialog在生产仍有真实UI消费者，应逐用途保留。 完整代码CI #4393 / R2 #3973均成功。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerBeginnerNightGuidance.kt` | 4873 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemantics.kt` | 882 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidSquareTableUi.kt` | 8699 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializer.kt` | 1647 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerContrastPolicy.kt` | 1565 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerCurrentDemonAuthority.kt` | 801 | 已审计 | 必须保留 | F15删去已死Imp反向重建与继承Resolver依赖，只留当前活跃Demon展示来源查询和poison flag显示；不决定实际死亡或继承结果。两个真实当前身份用例保留；无新可写状态。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerCurrentResidualScopeV2.kt` | 7419 | 已审计 | 必须保留 | F12-N全文/现用链：从当前Session与实际nightSteps生成首夜/后夜AI scalar/pair/Mayor API请求，跨未完成玩家行动设置barrier；留真实AI能力；末尾manualInformationCandidates非空才判UNSUPPORTED的旧Host domain依赖下一轮替代。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDayNominationGesture.kt` | 2043 | 已审计 | 需要解耦 | F12-M全文件/引用：ClocktowerDayOverviewTableUi经HostTableUi消费方向手势，实体座位映射需留；只允许alive的指向候选是旧交互约束，后续按实际提名操作能力复核，不与Session规则有效性捆绑。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDayOverviewTableState.kt` | 1280 | 已审计 | 必须保留 | F12-M全文件/引用：HostScreen多处日间/投票/Klutz直接从canonical GameState投影真实座位、存活/Drunk展示身份和幽灵票；只读方桌的身份与保密入口。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonAttackProductionAdapter.kt` | 5559 | 已审计 | 需要解耦 | F12-L全文：仅NightHostProjection生产消费，只读推导Imp攻击、Monk/Soldier/Mayor与Sage/Ravenkeeper预览；Dawn实际Death已经由Session明确确认，先重接必要夜间唤醒入口再整链删除，不得误删AI/真实死亡。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentation.kt` | 2251 | 已审计 | 必须保留 | F12-M全文件/引用：HostScreen实际恶魔掩饰展示及StorytellerRecommendationUi人工选择均消费，三身份结构化确认/已选AI输出保留，不属于Host规则裁定。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectRoleReveal.kt` | 2766 | 已审计 | 必须保留 | F12-P新增全文：只允许真人已确认Undertaker/Ravenkeeper问题seat与已知脚本Role构造一条RoleAt公开展示，不读取Host答案候选域；验证AI typed RoleAt/座位/角色匹配且不重算真值或Spy/Recluse登记。最终实测CI见F12-P。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectPairInformation.kt` | 5554 | 已审计 | 必须保留 | F12-O新增并全文：说书人不枚举全合法role×seat×seat，直接角色/两真实座位/None输入，typed AnyOf(RoleAt)与AllOf(RoleInPlay false)结构，不替代AI判断、不推导角色真值；可从AI typed option安全预选，任何确定选择都从原私密publication写唯一Session。完整CI #4406/R2 #3986成功。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectBooleanResult.kt` | 1495 | 已审计 | 必须保留 | F12-M全文件/引用：NightStepUi的占卜师真人YES/NO typed信息，座位结构检查，不用Host计算真值；保持辅助/自动和实际Session私密公布。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDirectNumericResultUi.kt` | 5398 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvance.kt` | 2118 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerEvilInfoSquareTableUi.kt` | 8274 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFirstNightEvilInformationPresentation.kt` | 8559 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFirstNightInformationRequest.kt` | 3696 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；HostScreen.firstNightPublicationNeeded/publishFirstNightInformation；实际只包装当前已选公布为单一host-observed项，semanticTruth=NOT_APPLICABLE，无旧全合法域重算。可随Migration去掉candidate/selectedCandidateId/AbilityObservation中间包装与按role/night键；保留实际pair/number/Boolean值和recipient，不能误称它还枚举Host合法pair或删AIproducer。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt` | 6402 | 已审计 | 必须保留 | F12-I实际全文+引用审查：FT现有方桌和手工选择正常生产消费；移除僵死legalResults singleton、onAutomaticResultSelected及Host唯一结果自动裁量。保留任意YES/NO确认，AI仅影响排序。 精确代码69f1bf31，CI #4390通过。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGhostVoteAuthority.kt` | 913 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGhostVotePresentation.kt` | 304 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightDecision.kt` | 2158 | 已审计 | 必须保留 | F12-N全文：首夜washerwoman/librarian/investigator真实actor绑定唯一GameState及AI请求identity，不枚举合法配对答案；当前AI首夜真实consumer。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightScalarDecision.kt` | 3678 | 已审计 | 必须保留 | F12-N全文：按当前问题/selected FT两座位构建NUMBER/BOOLEAN/ROLE AI合同和Session revision，不生成答案；防止过期和两个影子身份。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostBottomInsetPolicy.kt` | 1233 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostCoreSemantics.kt` | 6435 | 已审计 | 需要解耦 | F12-K全文/生产消费者复核：全文件审阅实际引用：现用私密命题ID稳定hash、Drunk/Poison显示命题、活着的邻居/真实角色判别和夜间角色wake仍消费；仅老storytellerPairHint单一Sage consumer已整删，余helpers混合真实场景与规则语义，未来逐项解耦，不整删。 代码SHA9c7c57a；完整Android CI #4396 / R2 #3976通过。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostFullScreenScaffold.kt` | 2726 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostPresentationModels.kt` | 3747 | 已审计 | 必须保留 | F12-O全文审+生产引用：NightStepUi是宿主夜信息actor/phase/typed proposition/显示和提示词AI上下文的结构合同；退役PairInformationAbility旧枚举；其他role的候选字段仍按用途后续削减，不应整删live DTO。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSeatContentPresentation.kt` | 2600 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSelectionSemantics.kt` | 10583 | 已审计 | 需要解耦 | F12-N全文/调用：真实目标、夜间手工动作、私密重复展示、自动模式fresh ID/防误跳、DisplayOption结构仍由HostScreen/NightStep实际消费；删除无生产调用automaticRedHerringSelectionAtBarrier的旧singleton伪决定，不改已采纳AI红鲱鱼自动推进。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostSquareTableScaffold.kt` | 2788 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableContract.kt` | 5651 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableGameStateProjection.kt` | 1916 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableLayout.kt` | 12909 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableReorder.kt` | 2043 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableSurfaceLayout.kt` | 3596 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt` | 7643 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt` | 5970 | 已审计 | 需要解耦 | F12-P全文/生产使用：彻底删生成options回调及displayOptions/manualInformationCandidates/automaticInformationCandidates三份同源复制，仍保留真实actor、Drunk/Poison可靠性提示与角色当前信息问题结构。现其它旧默认UI路径待统一。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerJointOpeningDisplayMatcherV2.kt` | 7042 | 已审计 | 必须保留 | F12-M全文件/引用：App核首夜战略方案仍可用、HostScreen将AI typed配对/数字绑定实际座位及展示；不以手工候选membership拒绝AI。保留新鲜度与实际已公布信息边界。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerJointOpeningReviewScreen.kt` | 4438 | 已审计 | 必须保留 | F12-M全文件/引用：App开局全局AI规划的真实review/自动继续/失败重试/手动入口，保留当前生产AI的可见流程。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerKlutzTableState.kt` | 2629 | 已审计 | 需要解耦 | F12-L全文：HostScreen真实Klutz方桌选择与座位映射、姓名结构检查要保留；living/candidate UI选择不能继续扩大为自动阵营胜负门禁，后续与Klutz App决策分离。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt` | 10146 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightFullScreenOwnership.kt` | 3710 | 已审计 | 需要解耦 | F12-O直接按first-night+实际展示的Washerwoman/Librarian/Investigator分配PairInformation屏，不依赖manualInformationCandidates非空；真实其它动作与特殊screen分派保留。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightHostProjection.kt` | 9561 | 已审计 | 需要解耦 | F14证据ae54b58e28b7abe180c98dadf3abdd3630af0bcb。已删除伪confirmedEvents/role与alive重建，目标/身份/存活直接读Session；但DawnDeathResolution、Ravenkeeper/Sage预览、succession候选及毒效果仍调用旧规则，下一组先接真实确认结果再删，不能整删活跃信息/AI界面。 代码f776151e完整CI #4375 / R2 #3955通过，见F14。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightPresentationRole.kt` | 710 | 已审计 | 必须保留 | F12-L全文：NightStepUi+NightFullScreenOwnership实际调用，Drunk夜间显示遵循展示镇民身份，不读取规则候选。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepMaterializerRegistry.kt` | 3241 | 已审计 | 需要解耦 | F12-L全文：HostScreen first/other-night有实时消费者；顺序身份/去重必要，但角色死亡引起的额外流程筛选及旧Flow门禁待审，保护正常步骤和AI。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightResultConfirmation.kt` | 4673 | 已审计 | 必须保留 | F15新增实际结果复核UI与窄输入，任意座位/多死亡/无死亡/新Imp/原中毒keep-clear，不枚举合法候选；gameId绑定及双revision过期与防双击、取消后可复核；实际事实只由Session原子提交，私密身份另确认。完整741测试/APK；真机未执行。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt` | 39613 | 已审计 | 需要解耦 | F12-P已删Undertaker/Ravenkeeper脚本Role全候选ResultChoices、自动singleton fallback与旧key。两类真实界面接DirectRoleReveal，AI预选必须当前target+RoleAt一致；Ravenkeeper自动公布使用现存clocktowerInformationCandidateId去重。其余>50KB UI及规则手工候选仍待进一步减少。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt` | 14493 | 已审计 | 必须保留 | F12-O彻底迁移production：不再接完整候选网格；按实际脚本身份/座位数及AI typed建议初始化，坐标点击两席或None人工直选，在确认时构造一个ClocktowerDisplayOption→原onShowPlayerDisplay/唯一Session; 仍保持隐私与自动/辅助模式。代码de091e24，Android CI #4406/R2 #3986成功。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPairPlayerRevealPresentation.kt` | 3074 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPhaseStorytellerAdapter.kt` | 407 | 已审计 | 必须保留 | F12-L全文：App/HostScreen/HostSelectionSemantics多处真实事件与AI/时间线消费者，阶段映射属统一Session结构语义。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlainInformationSquareTableUi.kt` | 6219 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayResolution.kt` | 3001 | 已审计 | 必须保留 | F12-L全文：实际所选信息在同一player display中投影typed RoleAt与实际可见payload；清除孤立LegacyUnreliable兼容函数，普通和AI选择统一，保留敏感选项清空。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt` | 15450 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerRevealHandoff.kt` | 2149 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationRoleCatalog.kt` | 7722 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerPresentationTheme.kt` | 3168 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt` | 10438 | 已审计 | 必须保留 | F12-P全文新版+真正consumer：守鸦人方桌仍先由玩家确认查询seat，再直选脚本身份或AI typed RoleAt预选；不再从Host候选菜单读ResultChoices；实际公布沿onShowPlayerDisplay/Session和同一action revision，保留当前角色/目标高亮。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRecurringInformationStepMaterializers.kt` | 1897 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationDecisionControls.kt` | 7221 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationInteractionState.kt` | 4023 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerResidualDisplayMatcherV2.kt` | 12977 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerResidualRequestIdentityV2.kt` | 947 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRoleLocalization.kt` | 583 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerRoleRevealChoiceProjection.kt` | 2263 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSageSquareTableUi.kt` | 6088 | 已审计 | 必须保留 | F12-K全文/生产消费者复核：Sage UI已改为AI仅一条预选和现有方桌轻点任意两个真实座位；仅结构检查不同/存在座位，不从全部可能的pair/真假列表筛选。原真人确认与Sage actor高亮保留。 代码SHA9c7c57a；完整Android CI #4396 / R2 #3976通过。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSageStepMaterializer.kt` | 2798 | 已审计 | 必须保留 | F12-K全文/生产消费者复核：Sage实际被杀唤醒的NightStep构建仍真实消费；删除全Sage合法/真假配对枚举及直接恶魔默认Pair、highPressure旧字段；保留从说书人手工两seat构造typed AnyOf和原PlayerDisplay确认/事件。 代码SHA9c7c57a；完整Android CI #4396 / R2 #3976通过。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberBadge.kt` | 2796 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabel.kt` | 390 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSessionPlayerCards.kt` | 2544 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.cards→JudgeScreen及所有展示读者；AbstractList每次get读取当前Session而非镜像可写cards，保留seat/name绑定、已确认role/death round投影；setupCards仅开局展示。复活/多次死亡的first death round与未知role渲染扩展是后续状态合同核查项，不据此发明恢复状态。对应现有3测试本轮通过。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleNumericChoiceUi.kt` | 4197 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleRecommendationChoiceUi.kt` | 2362 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionPresentation.kt` | 3360 | 已审计 | 需要解耦 | F12-J全文核查现用consumer：Mayor夜间展示签名移除无消费者automatic/explanation，现有single-target合约仍兼容受权Session用户提交，但upstream selectableSeats来自旧Host候选，未来需仅保留结构输入。 完整代码CI #4393 / R2 #3973均成功。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionUi.kt` | 5600 | 已审计 | 必须保留 | F12-J全文核查现用consumer：现唯一MayorRedirect方桌确认路径，删不可达automatic/ImpSuccession分支，仍支持手选重定向及Mayor dies。 完整代码CI #4393 / R2 #3973均成功。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSlayerTableState.kt` | 3543 | 已审计 | 需要解耦 | F12-Q全文与SlayerInteraction消费者：物理座位稳定/玩家身份选择、UI互斥高亮仍需；现在仍把claimant/target alive-only及曾用过候选门禁混入投影，后续按最小结构/存在座位改造，不能为旧测试还原机械命中推导。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableLifeMarkers.kt` | 434 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatDensityPolicy.kt` | 3360 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt` | 35812 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt` | 7356 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerStructuredInformationPreparation.kt` | 756 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerTableVoteState.kt` | 3141 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt` | 10895 | 已审计 | 必须保留 | F12-P全文新版+真实consumer：昨夜已确认处决seat决定投影，用户只在角色菜单选择展示的角色或采纳当前AI RoleAt；展示文案/序列化只从实际选择生成，不依赖Host全脚本Role候选表，仍沿单一Session记录。 |
| `app/src/main/java/com/codex/campboardgamehost/ClocktowerVoteHistoryRecord.kt` | 1024 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/CommonPlayersDefaultPolicy.kt` | 415 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostBottomActionBar.kt` | 3802 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostInteractionUi.kt` | 844 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/HostSeatingRoster.kt` | 2101 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；confirmHostSeating冻结连续seatId/唯一playerName，App、SetupFlow、方桌和AI唯一座位引用有真实消费者，是Host身份结构边界。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/HostSeatingSetupFlow.kt` | 742 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App导航初始确认/重新开放入座序列使用ConfirmedHostSeating，不是另一份正式GameState；保留开局前临时状态。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/MainActivity.kt` | 1103 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；AndroidManifest launcher activity→setContent(CampBoardGameHostApp)；平台生命周期与沉浸模式、唯一Compose入口；无规则真值/候选计算。manifest反射消费者已核对，不能仅按Kotlin零引用删入口。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/PlayerExperiencePicker.kt` | 1921 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/SeatingFirstSetupUi.kt` | 10527 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/StorytellerAiSettingsPanel.kt` | 8104 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App真实渲染模式/direct API key/gateway设置，安全存储在上游Keystore store；保留用户配置入口，未来废弃Gateway应单独确认，不因Rules清理误删AI设置。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/StorytellerOperationMode.kt` | 2026 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App/AI RequestIdentity/Settings真实用MANUAL、AI_ASSISTED、AI_AUTOMATIC；新局密钥可用性与异步mode一致性是权限和过期安全，不属于旧Rules。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/TroubleBrewingGameSnapshotJsonCodec.kt` | 7985 | 已审计 | 需要人工审核 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；编码TBGS UNKNOWN/UNCOMMITTED/KNOWN状态且文档称EvidenceLab共享；app/src/main没有调用codec，需确认跨项目真实消费再定退役/跨项目独立合同，禁止误删由AI使用的快照投影。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/BuiltInClocktowerRulesetCatalog.kt` | 3528 | 已审计 | 必须保留 | App、HostScreen；生产调用 fromContext.ruleset；现用两内置剧本资产/元数据入口，延迟缓存是静态内容；parse/adapter 均真实消费，不能按 legacy 历史注释退休。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerCatalogModels.kt` | 6756 | 已审计 | 需要解耦 | Flow、Setup、InitialOpening、GrimoireReminderToken、Catalog；保留角色文本/token/ValidatedRuleset；ClocktowerScriptCatalog 仅 catalog 专属测试调用，homebrew/extra team 扩展须与生产内置数据模型分清再减，不能整文件删除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptJsonParser.kt` | 21423 | 已审计 | 需要解耦 | 2026-10-19所有者已批准动态/自定义剧本作为未来可选项：Homebrew/custom角色定义、jinx/special/URI扩展导入与专属测试若增加复杂度，优先整组删除，**无需再就功能是否必须保留进行人工审批**。但实际生产调用RulesetJsonLoader.parseScript→BuiltInCatalog读内置TB/NGJ，必须保留或替换official ID/_meta/顺序解析和结构/角色存在性；不能直接整删当前parser破坏内置剧本。尚未实际删除，下一批按完整消费者链动手。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptNormalizer.kt` | 13636 | 已审计 | 需要解耦 | 2026-10-19所有者确定Homebrew/custom/extra teams动态导入仅未来可选，当前增加复杂度可连同Parser相应部分及专属测试删除，无需为未上线功能保留完整Normalizer。现有RulesetJsonLoader→BuiltInCatalog必须维持内置TB/NGJ角色文本、身份和顺序的最小规范化；按真实消费者缩窄实现后清理旧customDefinition等分支。未实际删代码。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptValidation.kt` | 1161 | 已审计 | 必须保留 | ScriptJsonParser、ScriptNormalizer→BuiltInCatalog；JSON/字段/ID/重复等格式错误类型，非已确认信息规则门禁；随 parser 未来瘦身缩减枚举即可。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/LegacyRulesetCatalogAdapter.kt` | 2594 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog.load；将真实 rules asset 文本和顺序组装 registry；无候选/事实否决，独占生产消费者尚在，需等目录来源替换才可退役。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/NoGreaterJoyOfficialCharacterMetadata.kt` | 3502 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog.load；现用 NGJ 角色文本/顺序/提醒元数据来源，无 truth 计算；不能借 Host Rules 清理删除支持剧本。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/catalog/OfficialReminderTokenMetadata.kt` | 3220 | 已审计 | 必须保留 | BuiltInClocktowerRulesetCatalog→GrimoireReminderToken；真实魔典 reminder token 字典及来源版本；结构完整性检查，已确认标记/私密展示消费者须保留。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ActionFact.kt` | 11583 | 已审计 | 需要解耦 | Session、ActionFactDraft/Timeline、SemanticJson、GlobalEventProjection、ProviderHistoryPrefix；ActionFact 和有序 reducer 必须保留；no-self nomination/Butler/Klutz 与 hit⇒consumed 仍是规则约束，不能把已确认事实拒绝为不合法；保留 seat/ID/sequence/原子性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerGameStateAdapter.kt` | 1817 | 已审计 | 需要解耦 | App、TroubleBrewingSetupCommitter；是旧 PlayerCard→canonical immutable 输入适配，非第二写入者；以 name 匹配毒和死亡自动清毒需改为稳定 seat/明确事实，不能直接删除开局适配。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerSemanticHistoryMode.kt` | 2302 | 已审计 | 必须保留 | GameSnapshot、Session、ProviderHistoryPrefixMaterializer、StrategicContinuation；显式 legacy/global 模式、防未来或冲突序列，避免从缺失历史捏造事实；迁移策略不等于独立恢复产品。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt` | 2462 | 已审计 | 必须保留 | SetupCommitter/Provider/ShownIdentity/Template、GameSnapshotProjector、RotationRecordFactory；确切已确认初始身份不可由 provenance 重算；只作不可变输入/事实，结构检查为座位身份和非空，不算线索合法性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DecisionTypes.kt` | 1115 | 已审计 | 需要解耦 | DayAbilityRegistrationRulingProducer、ProviderContract、SemanticModel/Json、CausalJournal；保留明确 RegistrationFact/Question；truthRelation 与 AbilityState 标签须退出 Host 规则裁决，现用类型/codec/AI prior 有真实消费者，不能整删。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/DynamicDecisionModels.kt` | 134 | 已审计 | 必须保留 | Session、ActionFact、Snapshot、Timeline、StrategicContinuation；实际仅 StorytellerPhase 枚举；不是旧动态候选引擎，保留阶段语义，暂不机械一文件合并。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/GameSnapshot.kt` | 2000 | 已审计 | 必须保留 | Session、DynamicActionReducer、GameSnapshotProjector、SemanticModel；唯一 canonical state 的不可变包络+两类全局绑定事实视图及 revision，不能按 Snapshot 名称当第二写入状态；仍须后续核对唯一日志物理收敛。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/GameState.kt` | 1305 | 已审计 | 必须保留 | Session、SetupCommitter、Snapshot、AI/RoleCatalog、UI projections；唯一玩家/seat/角色/阵营/存活/毒事实类型，seat 唯一与正数为结构安全；无线索真值计算。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/InformationModels.kt` | 1003 | 已审计 | 需要解耦 | FirstNightInformationRequest/Migration、GlobalFirstNightScalar/Pair、ProviderContract、StepBuilder；Reliability/YesNo/AbilityObservation 仍供现用 AI；请求已用 NOT_APPLICABLE，清旧 truth/registration 候选元数据要先迁消费者，不能删配对推荐。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/MurmurHash3.kt` | 3775 | 已审计 | 必须保留 | SetupDiversitySelector、SetupShownIdentityCommitment、SetupDealPlanner、NoGreaterJoySetupPreparer；实际种子/稳定身份哈希仍被生产发牌使用；非 orphan candidate ID，保留 hash vectors 验证。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/RoleCatalogAdapter.kt` | 1337 | 已审计 | 必须保留 | App、BuiltInCatalog、SetupCommitter/Rotation、StrategicContinuation；内置角色与 script ID 的平台适配，服务实际身份与 AI；不计算候选或信息真值。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/RulesetRef.kt` | 756 | 已审计 | 必须保留 | GameSnapshot、Session、Catalog、Persistence、SemanticJson、Oracle fixtures；内容版本/来源/覆盖标签身份；只校验格式不产生规则 verdict。coverage 后续用途单独审，不以未验证标签否决事实。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerPlayerContext.kt` | 1439 | 已审计 | 必须保留 | AppSettings、Seating、AppPreferences、Session、ProviderGameContextBuilder、StrategicContinuation；现用 AI 经验/claims/压力输入；独立于 actual role 的声明，默认和去重为结构，保护战略产品能力。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderContractV1.kt` | 14652 | 已审计 | 需要解耦 | RequestFactory、Initial/Scalar/Pair/Mayor consumers、CausalJournal、StrategicContinuation；身份/revision/seat/history cutoff 保留；legalCandidates、candidate payload 与 prior truth/ability 是剩余旧耦合，先验证各真实 AI context 不依赖菜单再删；不得停用请求/解析。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderHistoryPrefixV1.kt` | 9442 | 已审计 | 必须保留 | ProviderHistoryPrefixMaterializer、GlobalEventProjection、StrategicContinuation、ProviderContract；不可变冻结事实前缀、明确 UNKNOWN/PARTIAL、recipient 和 cutoff；供 AI 连续策略，非独立文本复盘。Action.copy 对 list 字段浅拷贝是后续不可变性核查项，未断言 bug。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/TroubleBrewingGameSnapshot.kt` | 5165 | 已审计 | 必须保留 | RuntimeGameProjector、GameSnapshotProjector、InitialOpeningRequest/Joint、MayorBoundary、JsonCodec；只读真实座位/状态投影及 known/uncommitted/unknown，不自算信息真值；保留现用 AI 请求；不是另一份可写 GameState。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactDraft.kt` | 12877 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App、Session与NightResultConfirmed形成真实ActionFactDraft→bindGlobal→同一Action timeline；identity/seat/sequence保护有效，某些角色级phase/ability约束未来缩窄。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactTimeline.kt` | 4575 | 已审计 | 必须保留 | F12-G全文+真实引用核查：F12-G删除只有Formal世界消费的FormalActionTimelineBinding，保留ActionFactTimeline/TimelineBoundActionFact由Session/Temporal History真实消费和fact.sequence/globalSequence一致性。（代码8644e757，待精确CI结果） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicContracts.kt` | 848 | 已审计 | 必须保留 | F12-D全文/真实引用证据0f904f7031770bd2f12acea4299aa9b1d5c8e5c0；TimelinePoint由Session.nextTimelinePoint→绑定Action/Observation、公开历史/AI prefix/JSON消费；globalSequence唯一排序权威，round/本地sequence仅结构和诊断，正数/非负形状检查不涉及游戏规则。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicObservationDraft.kt` | 3378 | 已审计 | 必须保留 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；Session/HostScreen/App信息公布与AI residual matcher使用；仅处理记录ID、收件人可见范围、global timeline结构绑定与匹配，不推导线索真值。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicObservationLog.kt` | 7851 | 已审计 | 必须保留 | F12-G全文+真实引用核查：F12-G删除worldReplay观察排序与Formal重载bindTo；真实Session日志的Global/Legacy chronology、recipient隐私、去重、按snapshotId绑定保留。（代码8644e757，待精确CI结果） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticJson.kt` | 14727 | 已审计 | 必须保留 | F12-G全文+真实引用核查：F12-G删除FormalState、PlayerKnowledge的旧ActionFact/Ruleset影子JSON，现仅有GlobalEventProjection/HostCoreSemantics使用的InformationProposition、实际观察/私密日志及TimelinePoint codec。（代码8644e757，待精确CI结果） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticModel.kt` | 8515 | 已审计 | 必须保留 | F12-G全文+真实引用核查：F12-G已删除FormalGameState/FormalPlayerState/PlayerKnowledgeSnapshot及b4CanonicalPayload/referencedSeats，现保留实际InformationProposition、GrimoireState、EpistemicObservation、StableId和不可变列表。（代码8644e757，待精确CI结果） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerCharacterInteractionRegistry.kt` | 8465 | 已审计 | 需要解耦 | ClocktowerHostInteractionProjector→ProductionFirst/OtherNightFlow；保留私密角色变更通知及真实步骤身份；Ravenkeeper/Sage/Undertaker 等 eligibility 决定步骤是否存在，需改为实际交互可录入而非规则唯一通道。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerFlowPlanner.kt` | 4731 | 已审计 | 需要解耦 | ProductionFirstNightFlow、ProductionOtherNightFlow；保留规范夜间顺序/系统边界；off-script require、>=7 邪恶信息和 inPlay 过滤是规则筛选，不能成为实际信息/动作的唯一可表示范围。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteraction.kt` | 1576 | 已审计 | 必须保留 | HostPresentationModels、NightHostProjection、NightStepMaterializerRegistry、Flow/EffectiveNightState；稳定 interaction ID、phase、kind 与完成类型，只有形状约束，无候选/真值推理；不新增可写 Context。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteractionProjector.kt` | 5245 | 已审计 | 需要解耦 | ProductionFirstNightFlow、ProductionOtherNightFlow；保留稳定 token→interaction 身份；actualRole setup 过滤和 registry eligibility 属于规则耦合，手工操作不能只允许这些生成步骤。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionFirstNightFlow.kt` | 2855 | 已审计 | 需要解耦 | ClocktowerHostScreen、NightStepMaterializerRegistryTest；当前真实首夜顺序消费者，不能按 shadow 注释判死；保留顺序/身份，去掉 functioningRole→依赖筛选和 exact-set 对手工入口的限制。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionInteractionOrderer.kt` | 6257 | 已审计 | 需要解耦 | ProductionFirst/OtherNightFlow、HostScreen、NightHostProjection、StepMaterializers；保留重复 ID/phase 不混合；projectedSet==productionSet 将规则生成集合升为硬边界，需与手工实际交互登记解耦。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionOtherNightFlow.kt` | 2198 | 已审计 | 必须保留 | ClocktowerNightHostProjection；当前纯组合已给出的 resolvedFacts/顺序，不自行判死亡或候选；保留窄职责，依赖的 planner/registry 门禁在其所有者处拆除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerResolvedFlowFacts.kt` | 877 | 已审计 | 需要解耦 | NightHostProjection→CharacterInteractionRegistry；混合实际 EXECUTION/角色变化与 MAYOR_REDIRECT_ELIGIBLE/DEMON_SUCCESSION_REQUIRED 等推导标签；改从实际事件/未完成操作读取，不能存第二历史。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyPlanner.kt` | 3658 | 已审计 | 必须保留 | FirstNightSetupDependencyProjection；仅按 interaction 顺序找已声明事实首次消费点；防止展示未确认 setup，保留 identity/顺序检查，角色规则在 projector 端清理。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyProjection.kt` | 11867 | 已审计 | 需要解耦 | ProductionFirstNightFlow；保留实际 bluff/红鲱鱼公布前确认；FortuneTeller RESULT_SEMANTICS_REQUIRED、functioning Spy 观察依赖属于旧真值/醉毒规则耦合，删除后不能影响真实魔典记录。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/persistence/ClocktowerRulesetPersistence.kt` | 1848 | 已审计 | 需要解耦 | App.troubleBrewingRulesetRefFor、开局 rulesetBasis；现用 setup role-set 哈希与 catalog script hash 两种身份并行；先核对 Snapshot/历史消费者选一种内容身份，不能直接废弃现用 RulesetRef 或引入兼容恢复壳。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/AbilityFunctioningSemantics.kt` | 2689 | 已审计 | 需要解耦 | DemonAttackProductionAdapter、NightHostProjection、Sage/InformationStepBuilder、NominationInteraction；保留实际角色/所示角色读取；functionsAs/oneShotDecision 的醉毒、存活与效果裁决退出提交/结果选择，先给现用夜间和日间操作保留明确结果入口。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightState.kt` | 1260 | 已审计 | 必须保留 | F15 orphan alive/role类型已随planner删除；当前仅保留OptionalNightSourceChronology消费者使用的cursor/boundary/排序身份合同。没有机械事件投影或第二GameState。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerOptionalNightSourceChronology.kt` | 1387 | 已审计 | 必须保留 | ClocktowerNightHostProjection.effectivePoisonTarget；仅检查 source/cursor 在当夜计划中并比较时序；保留未知 source 不伪造已行动，不承担毒效果合法性。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/CurrentDemonAuthority.kt` | 655 | 已审计 | 需要解耦 | ClocktowerCurrentDemonAuthority、TroubleBrewingDemonSuccessionResolver；singleOrNull(living Demon) 仍决定行动资格；保留身份不歧义边界，取消以角色/存活规则替代用户确认来源的依赖后再缩减。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/DemonNightAttackSemantics.kt` | 1752 | 已审计 | 需要解耦 | ClocktowerDemonAttackProductionAdapter、NightDawnResolutionPlanner、TroubleBrewingDemonSuccessionResolver；当前自行算 Soldier/Monk/醉毒/Mayor/自杀后果；先把实际死亡、转移和继承结果改为确认输入，保留 Session 原子应用后再整删计算器及规则专属测试。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/DemonSuccessionSemantics.kt` | 1186 | 已审计 | 需要解耦 | App、CurrentDemonAuthority、NightHostProjection、NightDawnResolutionPlanner、SuccessionResolver；5人阈值/红唇强制与爪牙候选属于旧规则计算；保留已确认 RoleChange/私密身份通知，退出强制/候选生产链后删实现。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/PoisonEffectLifecycle.kt` | 480 | 已审计 | 需要解耦 | NightHostProjection、NightDawnResolutionPlanner；effectiveTarget 受 functioning、afterNight 受 poisonerAlive 控制；保留明确毒状态变化和阶段事件，退出规则推导；先核对 Dawn 清毒的原子应用。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetContentHasher.kt` | 2875 | 已审计 | 必须保留 | TroubleBrewingRulesetPersistence.refFor；现用开局 RulesetRef 内容身份哈希，非信息真值或行动许可；有第二 catalog 哈希但用途/输入不同，不能按算法相似直接删除。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetJsonLoader.kt` | 3108 | 已审计 | 必须保留 | App、BuiltInClocktowerRulesetCatalog；parse 的旧 rules asset 仍是文本/顺序 registry 来源，parseScript 仍加载两内置脚本；没有据此判信息正确，命名 legacy 不等于孤立。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/RulesetKnowledge.kt` | 995 | 已审计 | 必须保留 | RulesetJsonLoader、LegacyRulesetCatalogAdapter、ContentHasher、RulesetPersistence；角色文本/顺序/jinx 数据及结构检查；服务现用目录和内容身份，不能随旧 Rules 门禁整包删。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/BotcAiWireTrace.kt` | 6748 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；PersonalDirect、JointOpening、StrategicContinuation、App/HostScreen；保留best-effort诊断、密钥redaction、UTF8分块与stage枚举；DEBUG+显式VERBOSE可输出完整私密角色/响应，revealFailure直接Log.e异常不受opt-in控制，需核对可导出的最小安全字段并缩减raw payload/异常文本；调试日志不能变成第二事实日志，不能宣称只记录无敏感元数据。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt` | 42993 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App、Role/Alive/Poison扩展、AI冻结context/history/continuation、PlayerCards；816行确认单writer/revision/ID去重/同一cursor行动+观测原子更新必须保留。L367 Butler同夜首次目标固定、存活/role/非自身、L601继承alive、L231/265 setup限定须区分事实许可与基本时序；L480phase规则表不与lastaccepted源时序混删。clearAttack/Protection仅改nightTargets无清除事件，voteStanding自动阈值，causal journal并行历史，须P3联合收敛；不另建Session或Context。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionAliveBoundaries.kt` | 2512 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App Slayer/Virgin/普通处决/Dawn→commitConfirmedPlayerDeath；保留已知seat、death事实+可选public observation原子性/expected revision；新actionID再次确认死者被拒、死亡自动清毒属现有规则/结果联动，须与实际确认语义拆开，不以删除规则破坏现有单次死亡提交。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPoisonBoundaries.kt` | 5149 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App投毒确认/阶段切换/Dawn；NightDawnResolutionPlanner；保留按实际Poison事实读取、seat、expected revision与状态/事件一次提交。gameStateWithPoisonTarget过滤alive、Dawn只carry/clear、Day只expire及强制phase是规则生命周期；改为明确确认的毒变化后退休推导，不混删stale/一致性保护。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionRoleIdentityBoundaries.kt` | 1629 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.commitClocktowerActualRole→commitConfirmedActualRoleChange；纯已确认role/alignment/type变更和有序RoleChange/版本原子写入，不计算继承对象；保留seat、同ID幂等、stale和未改变状态不伪造变更事件。不要因调用方是自动继承而删真正角色修改操作。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightCheckpoint.kt` | 1221 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App→HostScreen/NightHostProjection/AttackAdapter/DawnPlanner/NightRestore/SuccessionResolver；只读瞬态输入非持久化或第二writer；混放Session已确认目标、name草稿、UI游标与pending继承状态。保留最小顺序/版本/实际输入，去掉规则planner专属内容并用seat引用，不把Checkpoint名字当独立Recovery。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightTargets.kt` | 1641 | 已审计 | 需要解耦 | F15删除Death回写attackSeat及旧successor状态；实际攻击与实际死亡已独立。当前Attack/Protect/Mayor选择仍由Session动作更新，withAttack/Protection/Poison依赖失效与phase清除还需全文件职责复核；不得复活第二角色/死亡历史。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerVoteStanding.kt` | 633 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；Session.commitConfirmedGameAction(Vote)→App/HostScreen；confirm按threshold/最高票/平票自动选待处决者，不能作实际处决唯一许可；保留完整真实voter/nominee事实，从事实导出便捷汇总或显式选择，不能创建第二投票日志。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/DawnCommitIdentity.kt` | 992 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App夜间death/roleChange/publication→Session幂等；当前按game/round/seat构造稳定重复确认ID；必要去重，不计算游戏结果。gameId压缩normalization存在不同原始字符串同token的可能，当前App UUID来源；跨项目开放任意ID时应保留完整身份而非宣称无碰撞。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationLifecycle.kt` | 911 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；FirstNightInformationMigration→HostScreen UI reveal；只含ready/displayed ID和generation的瞬态，不是另一观测日志；Migration实际publish后立即display，去掉无独立消费的ready/generation层时保留Session已公布事实和fresh reveal防重放，不能把UI生命周期当第二事实权威。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationMigration.kt` | 3609 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；HostScreen、FirstNightInformationRequest、GlobalFirstNightDecision；FirstNightInformationRequest仍有selectedCandidate/candidates壳但实际producer只装当前确认值；resolvePublication依据decisionId标已展示，需收敛Session实际记录和UIhandoff，不保留旧候选菜单或虚构truth；保护真实首夜AI及显示后不可静默重写。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/HostCurrentOpenDecisionInventoryV2.kt` | 4321 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice、ResidualPackageGate；保留同game/script/revision、真实sourceSeat与当前问题身份；按phase/round/source任一已收信息拒新问题、每人一项限制超出同ID去重。没有生成legalCandidates，不因变量叫legalOpen就恢复规则引擎；收敛真实待处理交互身份而非角色能力次数门禁。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/InitialJointOpeningDirectV2.kt` | 21911 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestJointOpening→PersonalDirectOpenAiV1；transport schema/instructions；完整encode/decode/strict schema/中文战略prompt及IO调用，输入canonical预提交事实+当前问题，无Host合法候选或旧semantic规则复核；仅结构、引用、revision/fingerprint与输出完整性。MODEL的规则建议不等于Host算真值；保留working联合规划、conditional intent和memory，不因旧命名SemanticAcceptance删除。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/InitialOpeningRequestV2.kt` | 4144 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.startTroubleBrewingGame→InitialJointOpeningDirectV2/OpeningRecommendation；已知/未确认实际与所示角色、hasDrunk、经验、fingerprint、待确认问题/事件ID；没有合法输出枚举或第二写入状态，保留跨局/过期身份保护。当前内置TB六信息角色范围是现用AI合同，扩展剧本应独立安排。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/MayorRedirectDecisionBoundary.kt` | 1561 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.currentMayorPending、ResidualScope、RequestFactory、ResidualDisplayMatcher；DTO不含候选，显式mayorSeat只查存在；但production snapshot重载走默认single living actual Mayor，仍限制问题建立。保留实际sourceSeat/revision，去默认规则定位的唯一入口；F11已去掉Session commit合法域，不能误报所有Mayor耦合均已清。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/NoGreaterJoyKlutzHistoryProducerV1.kt` | 3449 | 已审计 | 需要解耦 | F12-M移除NoGreaterJoyKlutzChoiceRuleV1规则计算调用及functioning候选门禁；继续保留现用App learned/choice→唯一Session实际动作、死亡和获知先后关联、重复防护与未知历史不凭空补写；后续可按产品必要性继续简化额外功能状态字段。精确Android CI #4401/R2 #3981成功。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/OpeningRecommendationV2.kt` | 3975 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；InitialJointOpeningDirectV2→App、JointDisplayMatcher/Review、StrategicContinuation；可错AI建议/conditional/memory及OpeningSelection.validFor，非未来事实整包提交；字段互斥、seat形状、长度和fingerprint为响应合同。basis/resolution等模型标记不是Host真值计算，不借清理停用现用DTO。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/PendingPairInformationDecision.kt` | 530 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；GlobalFirstNightDecision→RequestFactory→ResidualDisplayMatcher；当前recipient/source、ability、reliability和请求版本的窄DTO，无枚举结果；是活跃AI配对路径，应保留到直接事实接口同等验证后再调整，不能与孤立PairInformationOutcome混淆。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/PersonalDirectOpenAiV1.kt` | 10171 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；InitialJointOpeningDirectV2、StrategicContinuation、App/SettingsStore；固定Responses endpoint、schema路由、每次instructions、strict JSON、retained链ID、无redirect、TLS/超时/响应上限及safeFailure；保留真实API，不执行付费测试。实际公开方法post与postRetained应分别按消费者审，文件整体非死实验；payload诊断由BotcAiWireTrace另审。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerDecisionFoundation.kt` | 975 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App、Session、Pair/Scalar/Mayor pending、RequestFactory、RevealHandoff、ResidualMatcher；仅game/request身份与两类非负revision，必要stale/跨局边界；不包含规则合法域或AI政策，不按Engine注释删除。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerGlobalEventProjectionV1.kt` | 9476 | 已审计 | 必须保留 | 全文/消费者证据 d76968168eb02e37cd87084bb51eabc60f789d9c；StrategicContinuation encode→Session historyPrefix只读投影，动作/公布信息按globalSequence派生AI delta；本批直接传递明确登记，未知不补造，玩家回放另有安全投影；保留死亡/处决去重与公开/私密信息语义。 F13本地807测试/APK及代码27d02f72完整CI #4373 / R2 #3953通过。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderGameContextBuilderV1.kt` | 3090 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice、CausalJournal.contextForRequest；只读玩家经验/claims/压力和同一Session冻结prefix；priorDecisions明确空，不假造durable causal历史。保留game/revision/seat校验、UNKNOWN覆盖和AI上下文，不把纯重建read projection当重复可写状态。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt` | 3345 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；GameContextBuilder、CausalJournal、StrategicContinuation；合并action与recipient observation的同一global cursor只读前缀，明确legacy不可重构/冻结cutoff；没有第二持久历史或规则推演。保留单调顺序/版本与隐私元数据。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderRequestFactoryV1.kt` | 5331 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice、CurrentResidualScope→ProviderContract；完整三入口pair/scalar/Mayor当前都未填legalCandidates，直接装实际问题、snapshot、source revision与history context；注释legacy scalar domain不是行为。保留真实AI请求，候选DTO清理在ProviderContract本体及旧消费者处处理。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerResidualPackageGateV2.kt` | 2795 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice→PendingCurrentStepAdvice；只验证游戏/版本/cursor/memory/current问题集合和SemanticResult结构，返回未发布建议，无状态写入/候选合法域。库存内部按人同夜限制另在Inventory解耦，不能整删这个stale/原子建议handoff。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicChangeCheckpointV2.kt` | 8667 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice/reviewConfirmedCurrentStepAdvice；保留已确认日志append-only、单调revision、策略memory版本/真实状态变化与提案验证；pendingNeedsJudgment实际仅isNotEmpty，不再有Host唯一答案计算，App的HOST_DETERMINED说明不能当代码证据。同夜published source集合拒新问题仍需改为交互ID；历史accept只推进advisory memory，不是Session写入。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicContinuationV2.kt` | 29879 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.requestLiveGlobalAdvice、PersonalDirectOpenAiV1、CurrentInventory/ResidualGate/Matchers；完整encode/decode/prompt/strict schema/IO/MemoryCursor：从canonical状态+实际event delta规划当前问题，输出直接scalar/pair/seat，验证结构/已知引用，不重算合法候选。保留afterConfirmedPublication才升级记忆/response anchor、无anchor重发完整事实、state!==frozen丢弃，未来条件意图不写事实。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingRuntimeGameProjector.kt` | 3512 | 已审计 | 需要解耦 | 全文/消费者证据 d76968168eb02e37cd87084bb51eabc60f789d9c；唯一生产消费者MayorRedirectDecisionBoundary snapshot重载；重复从快照+catalog造GameState/name用于默认living actual Mayor定位。应传实际来源seat与canonical state后删除此反向投影，保留Mayor实际redirect事实和现用AI。 F13本地807测试/APK及代码27d02f72完整CI #4373 / R2 #3953通过。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupDistribution.kt` | 1018 | 已审计 | 必须删除 | F12-D全文/真实引用证据0f904f7031770bd2f12acea4299aa9b1d5c8e5c0；纯5–15人数的规则角色比例表及DTO；消费者为SetupScreen显示、GeneratedSetupCandidateSource与TroubleBrewingSetupPresetValidator。不是AI请求/推荐合同；须先让真实角色配置/发牌确认脱离比例菜单和验证器，再整文件退休，不能直接断掉正常开局。本批仅给退出结论，未删除。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProvider.kt` | 4186 | 已审计 | 需要解耦 | F12-D全文/真实引用证据0f904f7031770bd2f12acea4299aa9b1d5c8e5c0；TB/NGJ ProductionSetupPreparer经Provider/Registry→GeneratedSetupCandidateSource/TemplateRepository；保留script/playerCount/provenance身份、实际角色多重集的窄输入和结构检查（本文件不计算规则真值）。legal-candidate source/provider抽象随setup生成/预设校验链解开后再缩窄，不能按legal注释误删必要已选配置。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt` | 5567 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；NoGreaterJoyProductionSetupPreparer真实消费；人数比例、Baron outsiders增量与seed角色生成是规则组局便利逻辑，不是Host事实门禁；先保证实际开局后再缩减。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyProductionSetupPreparer.kt` | 3562 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App对NGJ 5/6人真实开局调用；生成候选→ShownIdentity→ConfirmedSetup；不能整删。Provider registry与合法shown选项属可收敛中间层，保留角色身份及固定Seed配置。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversityHistory.kt` | 1550 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；TroubleBrewingSetupRotationHistory→ProductionSetupPreparer→Selector真实消费；多局组局经验不是canonical Session事实，随历史/评分减法评估保留范围。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelector.kt` | 11312 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；TroubleBrewingProductionSetupPreparer直接select；weighted overlap、exact repeat判退和评分复杂度可简化，但先保护当前真实自动开局。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt` | 4807 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；NGJ ProductionPreparer实际消费commit结果并生成展示角色；现有重复验证候选/随机选择与candidate provenance绑定，未来收敛为一次实际角色/展示身份确认但不可先断NGJ开局。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicy.kt` | 8649 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；NGJ生成setup遇Drunk需展示镇民，当前Resolver→Committer真实使用；template-policy分支无该生产调用但同文件共存，先缩窄旧合法候选枚举且保留真实shown role。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepository.kt` | 2996 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；TroubleBrewingProductionSetupPreparer实际用模板bucket/候选池；需保存模板来源与最终已选角色后才能去掉多余候选/评分。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDealRoleResolver.kt` | 2756 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；已确认GameState→App ClocktowerRole翻译桥，确保已提交actual/shown引用有效且唯一；是现行UI实际发牌消费者，长远收敛统一RoleId后可删旧App映射。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkSelectionRouter.kt` | 2488 | 已审计 | 需要解耦 | F12-E全文及实际生产/测试调用；代码HEAD 2ce0e897898e8f040c3bef8dcbda20186e2e4689；App 4处route消费者，醉汉实际座位确认仍需；保留已发牌座位结构，recommendedCandidate旧候选归属门禁须与AI直接规划解开。（静态，未运行新增测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGameSnapshotProjector.kt` | 7788 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App在预提交/当前局面/Mayor作用前多处生成结构化TBGS，AI初始/后续context与UNCOMMITTED区分有意义；只是只读投影，不能与无消费者的Snapshot JSON codec混淆。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetup.kt` | 5166 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；TB VisibleRoster与ShownSeatAssignment是先发身份再明确Drunk的真实生产中间步骤，App/Committer依赖；后续压缩重复身份载体时必须保留未确认与已确认区分。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionSetupPreparer.kt` | 4782 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；TB真实生产开局遍历curated preset→TemplateRepository→DiversitySelector→visible role seating；现用生成/分布数据不可直接打断，但重复候选池/相似度筛选是可净删旧体验层。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupCommitter.kt` | 5650 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；实际确认Drunk Seat并唯一materialize GameState/CommittedSetup供App与AI，且确认发牌座位引用未变；保留原子实际事实边界，名称Candidate与旧UI桥后续可缩窄。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupDealPlanner.kt` | 13250 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；TB ProductionSetupPreparer实际将shown角色按座位分配；包含旧3局player rotation动态规划和固定Seed排序，保留真实座位/展示身份、可简化跨局最优匹配算法。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetJson.kt` | 5548 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App加载当前大模板JSON，parse池/角色/醉汉字段真实消费；冗余旧runtimeSelectionPolicy解析可随Models/Validator收敛，不能删活跃preset读取。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetModels.kt` | 1083 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；TB preset roster/drunk现由JSON/Preparer消费，需保留；RuntimeSelectionPolicy的大量参数只作为旧资产元数据解析/验证，没有成为统一Host提交许可，应检查后删除过度配置。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetValidator.kt` | 8574 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；ProductionSetupPreparer在读取curated pool时调用；角色身份/数量/外部ID结构安全保留，Baron/Drunk按规则比例拒绝模板是旧setup规则验证而非已确认事实门禁，后续简化。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationHistory.kt` | 2896 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；跨局完成记录→DiversityHistory供TB组局避免重复，额外player identity历史用于座位轮换；不是唯一Game Session事件源但独立跨局偏好，是否保留需按真实体验评估，不可误认并行本局存档。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationRecordFactory.kt` | 11970 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；App在一局完成创建跨局rotation record，persistence store也调用validate；当前重复比较canonical seat、Drunk和role集合有过度规则断言，保留必要已确认身份记录后收敛。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerArtistInteraction.kt` | 6603 | 已审计 | 需要解耦 | F12-R全文+HostScreen引用：艺术家声称者选择/Yes-No发布仍现用，然而旧trueAnswer/ShownAnswer和poison/functioning自动菜单以及claimant白名单混在UI；先保留实际玩家操作，下一批改直选实际公布并送入Session后清除旧候选。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerArtistTableUi.kt` | 4130 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayOverviewTableUi.kt` | 6871 | 已审计 | 必须保留 | F12-R全文+真实HostScreen引用：共享方桌白天当前投票/存活和提名手势、主持工具、Slayer/Artist入口、结束白天按钮；仅是真实现场流程/UI，不删核心Day。现Slayer按钮入口不再依旧候选门禁。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayScreen.kt` | 13585 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayTableScaffoldUi.kt` | 1002 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDrunkSelectionScreen.kt` | 8884 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt` | 96487 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；App.Screen.ClocktowerJudge→ClocktowerJudgeScreen→Day/Night/PlayerDisplay；R2位置守卫；已按1795行覆盖。保留私密显隐、确认后展示、current revision、AI预选及fresh-step自动handoff。L257起投票阈值/claimant筛选、558角色资格、721 Sage默认、896/1125 Spy醉毒、1335行动合法目标、1387继承候选需退出。L1680-1761按同phase/round/source阻止不同公布并非只去重同事件；后续区分新确认事实与重复ID/重开既有显示，不能删除防重放/隐私保护。FirstNightMigration、display DTO是需缩减的混合边界，非整UI可删。（静态；运行仅上述明确测试） |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerKlutzTableUi.kt` | 5712 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt` | 15696 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNominationInteraction.kt` | 5956 | 已审计 | 需要解耦 | F12-R全文+真实HostScreen/App引用：此处仍自动根据Virgin actualRole/poison/Spy registration算是否跳票处决，并控制Nomination special UI和Session事件。须改为说书人实际确认是否消耗圣女能力和执行提名者后，才退出真值规则及Spy旧专用控件；保留提名事实和真实死亡提交。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerPendingNominationTableUi.kt` | 7382 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerResultsScreen.kt` | 17211 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSetupScreen.kt` | 20901 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSlayerInteraction.kt` | 3693 | 已审计 | 需要解耦 | F12-Q全文与HostScreen真实调用：保留公共claimant→target现场操作、方桌座位投影、真实结果确认勾选；去掉仅旧Host规则使用的Recluse/poisoned私密登记和one-shot自动命中。手工目标死亡默认false，每次换目标重置；App仅把确认结果提交Session。后续可精简候选座位UI规则。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSlayerTableUi.kt` | 8133 | 已审计 | 必须保留 | F12-R全文+SlayerInteraction调用：公共声称者及目标高亮、选中顺序、确认实际结果/退回/重选的真实方桌操作，行动人角色及真假命中不由本UI计算；无真实座位时无按钮，保留。 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerVoteTableUi.kt` | 7854 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/StorytellerAutoPauseScreen.kt` | 2721 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/java/com/codex/campboardgamehost/debug/DebugFlightRecorder.kt` | 8403 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；Manifest Application注册崩溃捕捉、App/Settings分享ZIP、noBackup私有保存与FileProvider真实消费；有诊断价值，检查crash state以及可分享trace是否包含私密game事实后再缩减。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/debug/DebugFlightRecorderCore.kt` | 2706 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；DebugFlightRecorder使用bounded event buffer、crash/trace序列化，ADB/ZIP故障诊断合同有真实消费者；需检查fields/exception message泄露范围，避免向公开日志输出敏感事实。（静态审计） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/AppPreferencesStore.kt` | 4026 | 已审计 | 必须保留 | CampBoardGameHostApp；语言/常用玩家/AI操作模式/经验偏好；非 canonical 游戏状态或重复历史。保留 MANUAL 默认与 AI 模式开关。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/GatewayTokenEnvelopeV1.kt` | 1294 | 已审计 | 必须保留 | StorytellerGatewayConnectionStore；AES-GCM envelope 长度/IV/version parser，保护设备密钥传输；无 token 输出或规则计算。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/PersonalOpenAiSettingsStore.kt` | 1273 | 已审计 | 必须保留 | App→PersonalDirectOpenAiV1；真实 direct 模型/启用/密钥配置，分离 gateway scope；无授权不发付费请求，不能当实验删现用 AI。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/StorytellerGatewayConnectionStore.kt` | 4649 | 已审计 | 必须保留 | App、PersonalOpenAiSettingsStore；AndroidKeyStore AES-GCM、scope/AAD 隔离、AtomicFile/noBackup 原子密钥持久化，必要安全支撑；不能与普通偏好或游戏历史合并。（静态） |
| `app/src/main/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationHistoryStore.kt` | 12917 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/drawable/ic_launcher.xml` | 768 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-en/debug_strings.xml` | 472 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-en/strings.xml` | 6858 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values-v27/themes.xml` | 479 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/debug_strings.xml` | 426 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/strings.xml` | 6616 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/values/themes.xml` | 412 | 待审 | — | 尚未形成有证据结论 |
| `app/src/main/res/xml/debug_file_paths.xml` | 208 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；AndroidManifest FileProvider resource引用该cache-path，DebugFlightRecorder.shareDebugBundle依赖；限制仅debug-flight-recorder-export，不应按无Kotlin字符串引用判死。（静态审计） |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerAiPairRecommendationRecoveryTest.kt` | 4932 | 已审计 | 必须保留 | F12-O改为实际AI结构配对建议→DirectPairSelection无全枚举的预选＋人工覆盖→Private已公布观测NOT_APPLICABLE；护AI误删/无额外登记。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerArtistDraftTest.kt` | 2172 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerCompactNightPresentationTest.kt` | 1155 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerBeginnerNightGuidanceTest.kt` | 4298 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemanticsTest.kt` | 992 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidSelectionAuthorityTest.kt` | 853 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializerTest.kt` | 1566 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerChefAlternativePublicationTest.kt` | 2457 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerContrastPolicyTest.kt` | 2991 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerCurrentDemonAuthorityTest.kt` | 2318 | 已审计 | 需要解耦 | F15全文核查，保留已确认live Imp识别及多Imp歧义不擅选的2用例；死Imp恢复候选推理已删。剩余1例仍断言read-only attack preview的毒规则，随预览退休再缩窄，不能充当实际死亡提交门禁；完整741测试通过。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerCurrentResidualScopeV2Test.kt` | 19070 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDayNominationGestureTest.kt` | 2643 | 已审计 | 需要解耦 | F12-M全文件审阅：实际座位手势ring index/self映射是UI有效测试；alive-only nominator/target规则候选断言属于待解耦交互限制。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDayOverviewTableStateTest.kt` | 2519 | 已审计 | 必须保留 | F12-M全文件审阅：canonical GameState实际Role/ShownRole、座位排序、存活和只读状态的必要Storyteller魔典显示回归。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonAttackDawnFactsTest.kt` | 4312 | 已审计 | 需要解耦 | F12-N全文：所测均为过期Host攻击预测/Mayor安全候选，当前NightHostProjection仍真实使用，不误当Session death；完成独立确认触发改造后应连规则断言整删。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonAttackProductionAdapterTest.kt` | 3325 | 已审计 | 需要解耦 | F12-N全文：只测试旧Imp/Monk/Soldier/Mayor DirectAttackSemantics，当前生产仅用于Ravenkeeper/Sage夜间流和Mayor预测；最后拔出后整删，不能为旧CI保留Host规则裁定。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentationTest.kt` | 3571 | 已审计 | 必须保留 | F12-M全文件审阅：当前AI三掩饰身份不静默补齐、准确展示选择顺序及真人角色三选的结构/脚本域测试；不是Host线索真值门禁。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectRoleRevealTest.kt` | 4046 | 已审计 | 必须保留 | F12-P新增全文：执行座位/守鸦查询座位事实、AI当前RoleAt无全候选预选、异常/无效身份座位拒绝、显示不虚构registration，替换旧ResultChoices专属菜单测试。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectPairInformationTest.kt` | 3720 | 已审计 | 必须保留 | F12-O新增并全文：结构化直选顺序/未知座位/缺席typed事实/AI预选及Spy-Recluse不臆造登记，不依赖旧Host枚举；Android CI #4406通过。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectBooleanResultTest.kt` | 1687 | 已审计 | 必须保留 | F12-M全文件审阅：验证即使Host旧候选仅YES也能人工公布NO，并记录typed Boolean、主体两座位与无虚构truthful；重要P1/AI回归。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDirectNumericResultTest.kt` | 1960 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDisplayedInformationReliabilityTest.kt` | 2112 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerDynamicNightAdvanceTest.kt` | 1930 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerEvilInfoSquareTablePresentationTest.kt` | 4197 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightEvilInformationPresentationTest.kt` | 4882 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightInformationRequestTest.kt` | 3674 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFirstNightPlayerRevealHandoffTest.kt` | 6649 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFortuneTellerPhaseAuthorityTest.kt` | 2658 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTablePresentationTest.kt` | 2887 | 已审计 | 必须保留 | F12-I实际全文+引用审查：FT座位选择结构/高亮与两个Boolean确认语义测试，现确认AI建议不构成合法值拒绝。 精确代码69f1bf31，CI #4390通过。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGhostVoteAuthorityTest.kt` | 2641 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGhostVotePresentationTest.kt` | 1930 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerGlobalFirstNightScalarDecisionTest.kt` | 10699 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostSeatContentPresentationTest.kt` | 4526 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableContractTest.kt` | 5504 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableGameStateProjectionTest.kt` | 6009 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableLayoutTest.kt` | 6317 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableReorderTest.kt` | 6461 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerHostTableSurfaceLayoutTest.kt` | 4245 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerJointOpeningDisplayMatcherV2Test.kt` | 7413 | 已审计 | 必须保留 | F12-M全文件审阅：当前AI数字无需旧Host合法菜单、typed身份pair、与Session事实不符时撤销预选的真实合同，应保留。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerMonkRetargetRavenkeeperDeathTest.kt` | 2659 | 已审计 | 需要解耦 | F12-N全文：NightTargets重新选Monk座位与DawnAttackFacts旧preview桥接测试；非玩家实际死亡记录，待旧attack规则链退役后整删特定门禁断言。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTablePresentationTest.kt` | 3116 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightFullScreenOwnershipTest.kt` | 6661 | 已审计 | 必须保留 | F12-O全文核：真实夜行动至方桌surface dispatch，3首夜pair在无Host候选域时仍必须有完整可编辑UI，Ravenkeeper/FT/Mayor/newDemon保留。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightHostProjectionTest.kt` | 9222 | 已审计 | 必须保留 | F14已全文审计；保留待处理Ravenkeeper信息入口及canonical Session状态/旧UI cards边界，新增选择/返回/编辑不造死亡角色、实际提交及幂等贯穿回归。本地3用例在完整761测试/APK中通过；不保留旧预测即事实的断言。 代码f776151e完整CI #4375 / R2 #3955通过，见F14。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNightStepMaterializerRegistryTest.kt` | 7629 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerNominationDraftTest.kt` | 1419 | 已审计 | 必须保留 | F12-R全文：纯真实提名nominator/nominee暂存、投票提交、失败不清草稿和可复核原子动作回归，不参与Virgin身份真值自动裁定，需保留。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerOtherNightWakingRoleAuthorityTest.kt` | 1692 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayPresentationTest.kt` | 6681 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayResolutionTest.kt` | 5458 | 已审计 | 必须保留 | F12-L全文：实际私密显示typed RoleAt/座位与文本不反推安全断言；旧兼容调用替换为生产统一resolve，保留真实回归。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRecurringInformationStepMaterializersTest.kt` | 1457 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRedHerringAutoAdvancePolicyTest.kt` | 17783 | 已审计 | 必须保留 | F12-N全文：删除只验证无生产调用的singleton自动红鲱鱼合法域判决约75行；保留accepted joint opening、安全auto reveal、真人query一次公布/只读重开、AI stale隔离等现用测试。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRegistrationInteractionStateTest.kt` | 4372 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerResidualDisplayMatcherV2Test.kt` | 3455 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerResidualRequestIdentityV2Test.kt` | 1594 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRoleLocalizationTest.kt` | 620 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerRoundedRectanglePerimeterLayoutTest.kt` | 6857 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSageSquareTablePresentationTest.kt` | 2888 | 已审计 | 必须保留 | F12-K全文/生产消费者复核：保留现用Sage typed two-seat信息展示、防文本反推座位、实际手工两seat toggle及actor独立高亮、结构拒绝越界/重复座位；删除旧候选dropdown/LegacyUnreliable路径专项测试。 代码SHA9c7c57a；完整Android CI #4396 / R2 #3976通过。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSageStepMaterializerTest.kt` | 3562 | 已审计 | 必须保留 | F12-K全文/生产消费者复核：原Sage全合法pair真值/默认恶魔decoy断言退役；现验证Host步骤初始无伪默认信息，Storyteller实际两seat记录为AnyOf、非法/重复座位不提交。 代码SHA9c7c57a；完整Android CI #4396 / R2 #3976通过。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabelTest.kt` | 601 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSessionPlayerCardsTest.kt` | 3910 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；testFast/testFull→ClocktowerSessionPlayerCards；3项验证同frame读取Session死亡/role变化无需镜像写cards、纯setup仍可编辑；本轮通过，保护唯一事实来源而非UI表象字符串。（静态；运行仅上述明确测试） |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerShownUnreliablePropositionTest.kt` | 4641 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionPresentationTest.kt` | 6316 | 已审计 | 必须保留 | F12-J全文核查现用consumer：复核现行target selection/seat identity/首夜动作人工与auto确认防旧selection自动提交；删除只测已退役Mayor automatic旧呈现的断言。 完整代码CI #4393 / R2 #3973均成功。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSlayerDraftTest.kt` | 2344 | 已审计 | 必须保留 | F12-Q全文重写：当前草稿只记录真实claimant/target及直接确认布尔结果，取消旧Recluse登记矩阵断言；测试不完整不得提交、Session拒绝不清掉草稿、实际确认布尔值原样传递。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSlayerTableStateTest.kt` | 3471 | 已审计 | 需要解耦 | F12-Q全文：实体座位稳定、选中先后结构需要；待删claimant候选合法域及dead-target硬门禁旧玩法裁定断言，随生产UI变更调整。 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableLifeMarkersTest.kt` | 1151 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatDensityPolicyTest.kt` | 3003 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerSquareTableSeatLayoutTest.kt` | 4397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerTableVoteStateTest.kt` | 3843 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/ClocktowerVoteHistoryRecordTest.kt` | 2045 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/CommonPlayersDefaultPolicyTest.kt` | 1062 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/GatewayTokenEnvelopeV1Test.kt` | 1557 | 已审计 | 必须保留 | Gradle testFast/testFull→JVM JUnit；GatewayTokenEnvelopeV1；round-trip、版本/IV/长度 fail closed 为实际密钥 envelope 合同；本轮定向运行2用例通过，无明文/网络/付费调用。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/HostRoleLocalizationTest.kt` | 1497 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/HostSeatingRosterTest.kt` | 4070 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/LiveKlutzScriptReachabilityTest.kt` | 9913 | 已审计 | 必须保留 | F12-M全文件审阅并缩窄：退役KlutzRule阵营胜负/脚本白名单门禁用例，保留真实Death证据、learned/choice顺序与重复/未知保护；新增不按阵营、死亡合法性拒绝实际座位事件的合同。 |
| `app/src/test/java/com/codex/campboardgamehost/NoGreaterJoySetupRegressionTest.kt` | 1100 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/StorytellerNewGameConfigurationTest.kt` | 3716 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/StorytellerOperationModeContractTest.kt` | 1313 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/TwoPlayerSelectionActionTest.kt` | 2645 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/TwoPlayerSelectionRevalidationTest.kt` | 2397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/BuiltInClocktowerRulesetCatalogTest.kt` | 2247 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/ClocktowerScriptCatalogTest.kt` | 10823 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/OfficialReminderTokenMetadataTest.kt` | 5307 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/catalog/TroubleBrewingCanonicalScriptAssetTest.kt` | 3369 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerGameStateAdapterTest.kt` | 2151 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/ClocktowerSemanticHistoryModeTest.kt` | 5985 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt` | 2547 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/GameSnapshotTest.kt` | 1545 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/GameStateTest.kt` | 635 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/MurmurHash3Test.kt` | 872 | 已审计 | 必须保留 | Gradle testFast/testFull→JVM JUnit；生产 setup hash 消费者；保留 reference vectors，已不含退休 candidate ID 测试；本轮定向运行1用例通过。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/domain/StorytellerProviderContractV1Test.kt` | 4996 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/A4RuntimeFixtures.kt` | 1520 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ActionFactTimelineContractTest.kt` | 5514 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ClocktowerPrivateObservationRecordIdTest.kt` | 2045 | 已审计 | 必须保留 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；真实已公布私人线索稳定ID受game/phase/role/subject/value影响，记录重复与更正辨别；保留Session信息去重安全合同。（静态审计） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/CrossTypeTimelineCompatibilityTest.kt` | 5262 | 已审计 | 必须保留 | F12-G全文+真实引用核查：Action/Observation全局序列碰撞、Legacy混用拒绝、mutable输入防御；生产SemanticHistoryMode实际消费兼容性检查。（代码8644e757，待精确CI结果） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/EpistemicSemanticModelTest.kt` | 5052 | 已审计 | 必须保留 | F12-G全文+真实引用核查：删除旧FormalState/PlayerKnowledge与shadow ActionFact测试；保留InformationProposition、RecordedObservation、Spy私密Grimoire、schema迁移、recipient与稳定ID。（代码8644e757，待精确CI结果） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ObservationTimelineBindingTest.kt` | 6850 | 已审计 | 必须保留 | F12-G全文+真实引用核查：以真正的snapshotId替代Formal对象；保留Global排序、重复序列/混用拒绝、旧Local兼容、TimelinePoint往返及bindTo。（代码8644e757，待精确CI结果） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/ShownRoleAtSemanticContractTest.kt` | 898 | 已审计 | 必须保留 | F12-G全文+真实引用核查：ShownRoleAt角色公开/实际展示身份语义及稳定JSON往返对Session/AI结构化信息有价值。（代码8644e757，待精确CI结果） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/fixtures/TroubleBrewingFixtures.kt` | 3370 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/fixtures/g10-game2-precommit-tbgs-v1.json` | 2116 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerDrunkFortuneTellerFlowTest.kt` | 4367 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerFlowPlannerTest.kt` | 6150 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerHostInteractionProjectorTest.kt` | 10341 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerNewDemonIdentityContractTest.kt` | 1979 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionFirstNightFlowTest.kt` | 4489 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionInteractionOrdererTest.kt` | 9550 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/ClocktowerProductionOtherNightFlowTest.kt` | 4667 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyPlannerTest.kt` | 7875 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/FirstNightSetupDependencyProjectionTest.kt` | 8894 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/flow/NoGreaterJoyStructuralProofTest.kt` | 7788 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/AbilityFunctioningSemanticsTest.kt` | 7212 | 已审计 | 需要解耦 | F12-Q全文：原oneShotDecision与selectedMechanicalEffectApplies全无生产消费者，连旧Poison/Drunk Slayer效果计算测试整删；仍有角色身份/Drunk可靠性和functioning语义等其他生产夜间规则消费者，后续逐路径拆分。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerNightChronologyTest.kt` | 2022 | 已审计 | 必须保留 | F14从旧EffectiveNightState混合测试保留4个source/cursor排序、未知/重复interaction身份用例；OptionalNightSourceChronology仍真实消费。只测试当前排序责任，不重建MechanicalDeath/RoleChanged；完整761测试/APK通过。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerOptionalNightSourceChronologyTest.kt` | 2131 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/DemonNightAttackSemanticsTest.kt` | 3221 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/DemonSuccessionSemanticsTest.kt` | 4187 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/PoisonEffectLifecycleTest.kt` | 1271 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/RulesetContentHasherTest.kt` | 2515 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/AcceptedSessionActionTestFixture.kt` | 520 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/BotcAiWireTraceCodecTest.kt` | 1579 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionAliveBoundaryTest.kt` | 6135 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionConfirmedActionBoundaryTest.kt` | 15475 | 已审计 | 需要解耦 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；testFast/testFull→Session confirmedAction/observation；保留live cursor、action+revision、同ID内容冲突/幂等、stale、实际信息原值；Butler自选拒绝/同夜不许改目标的规则断言随职责退休，不整删原子性验证。当前6用例通过不代表这些旧规则须永久保留。（静态；运行仅上述明确测试） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPhaseBoundaryTest.kt` | 5343 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionPoisonBoundaryTest.kt` | 8150 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionRoleIdentityBoundaryTest.kt` | 3371 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；testFast/testFull→Session RoleIdentity boundary；1项验证RoleChange事实/状态/版本原子、shown/alive/poison/其它seat不变、幂等与stale无半写；本轮通过，不依赖候选继承算法。（静态；运行仅上述明确测试） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt` | 15547 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGlobalObservationCommitTest.kt` | 9807 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerHistoricalActionObservationCaptureTest.kt` | 6397 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightVoteOwnershipTest.kt` | 7003 | 已审计 | 需要解耦 | F15全文核查，保留Session真实Attack/Protect/Poison/Mayor、stale/幂等与实际Vote顺序/重试/phase reset；旧successor字段断言删除。票数门槛/最高者与依赖失效策略仍有旧规则预期，需随真实生产所有者缩窄，不能因本批绿灯永远保留。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerPoisonSettlementTest.kt` | 5410 | 已审计 | 需要解耦 | F15全文核查，保留显式keep/clear、旧事实不修复、死亡后毒事实、双写一致性、stale/ID幂等的6用例；旧successor元数据断言退出。部分phase/日间只清除策略仍待与实际手工状态输入合同核对；没有角色健康/转职合法域判断。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/DawnCommitBoundaryTest.kt` | 9598 | 已审计 | 必须保留 | F15全文核查；实际多死亡/公开AliveAt批次原子性、独立Attack/Protect、角色变化保密、未知/重复/stale和后续公布冲突不留半份状态、明确NoNightDeaths的AI/公开事实，以及原死亡/role/phase时序与重试全部保留，完整741测试通过。 证据9f6c4af245b3aa7424e1134ad1789be6520181bc；本轮验收见F15。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationLifecycleTest.kt` | 613 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/FirstNightInformationMigrationTest.kt` | 4195 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/InitialJointOpeningDirectV2Test.kt` | 24971 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/MayorRedirectBoundaryTest.kt` | 8478 | 已审计 | 必须保留 | F13从旧journal测试迁出唯一实际Mayor边界用例：普通/恶魔/自身target、未知seat、source身份、stale及重复ID原子性；无journal/candidate规则依赖，本地完整测试通过。 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/PersonalDirectOpenAiV1Test.kt` | 6575 | 已审计 | 必须保留 | 证据HEAD `22716fa4d6bdbe92edeb0091b4c9483a733a7370`；testFast/testFull→PersonalDirectOpenAiV1 buildRequest/adaptResponse；6项离线schema/retained response链ID/未完成输出/invalid model与退休wire路由回归，未调用post或网络；本轮6用例通过，保护现用AI而非旧规则门禁。（静态；运行仅上述明确测试） |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/PoisonExecutionDuskTimelineRegressionTest.kt` | 3627 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1Test.kt` | 13013 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerProviderRequestFactoryV1Test.kt` | 9321 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicChangeCheckpointV2Test.kt` | 23299 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/session/StorytellerStrategicContinuationV2Test.kt` | 24035 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProviderRegistryTest.kt` | 6378 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/Dlb7TroubleBrewingAcceptanceTest.kt` | 6939 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt` | 9487 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyGenericArchitectureAcceptanceTest.kt` | 6503 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/NoGreaterJoyProductionSetupPreparerTest.kt` | 4826 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelectorTest.kt` | 9107 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt` | 9218 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicyResolverTest.kt` | 10934 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepositoryTest.kt` | 6538 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDealRoleResolverTest.kt` | 2710 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkSelectionRouterTest.kt` | 5076 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGameSnapshotProjectorTest.kt` | 11556 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingGenericProductionAuthorityTest.kt` | 5097 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetupTest.kt` | 4130 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionSetupPreparerTest.kt` | 6322 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingProductionVisibleRosterCutoverTest.kt` | 2603 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupCommitterTest.kt` | 9018 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetJsonTest.kt` | 5031 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupPresetValidatorTest.kt` | 10158 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingSetupRotationRecordFactoryTest.kt` | 10100 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingVisibleRosterSeatPlannerTest.kt` | 7656 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/debug/DebugFlightRecorderCoreTest.kt` | 2500 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerRulesetPersistenceBasisTest.kt` | 2123 | 已审计 | 需要解耦 | Gradle testFast/testFull；RulesetPersistence/ContentHasher；保留角色变化不改写已冻结内容身份的真实不变量；若统一 catalog 内容哈希则迁移该断言并退休 role-set basis 形状，不能当新恢复系统依据。（静态，运行结论仅明确列出者） |
| `app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerTypedDayActionHistoryTest.kt` | 2386 | 已审计 | 必须保留 | F12-I实际全文+引用审查：保留实际ActionFact Slayer/Nomination/Vote/NoExecution按全局Sequence的结构化历史测试，删仅依赖假public replay的投影用例。 精确代码69f1bf31，CI #4390通过。 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingRecentPlayerRotationHistoryStoreTest.kt` | 5634 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationHistoryStoreTest.kt` | 12710 | 待审 | — | 尚未形成有证据结论 |
| `app/src/test/java/com/codex/campboardgamehost/persistence/TroubleBrewingSetupRotationRecordStoreTest.kt` | 2165 | 待审 | — | 尚未形成有证据结论 |
| `build.gradle.kts` | 217 | 已审计 | 必须保留 | Gradle wrapper/settings→:app；实际 AGP/Kotlin/Compose 插件版本定义；非旧规则模块，不为审计更新版本。（静态，运行结论仅明确列出者） |
| `gradle.properties` | 553 | 已审计 | 必须保留 | Gradle/AGP→:app；现用 JVM/AndroidX/Kotlin/AGP开关；部分deprecated不等于删除许可，需独立构建验证后才能改，审计不混入工具链升级。（静态，运行结论仅明确列出者） |
| `gradle/gradle-daemon-jvm.properties` | 1138 | 已审计 | 必须保留 | Gradle wrapper Daemon JVM criteria；toolchainVersion=25，故 CI setup-java17 不代表 daemon一定17；本地25 Gradle启动及本轮定向JVM测试成功，保留真实构建所需配置。（静态，运行结论仅明确列出者） |
| `gradle/wrapper/gradle-wrapper.properties` | 252 | 已审计 | 必须保留 | gradlew/gradlew.bat→wrapper JAR；固定Gradle9.5.0下载/缓存和超时配置；现用本地/CI构建入口，不是规则计算或重复状态。（静态，运行结论仅明确列出者） |
| `gradlew` | 9014 | 待审 | — | 尚未形成有证据结论 |
| `gradlew.bat` | 2966 | 待审 | — | 尚未形成有证据结论 |
| `player/handbook.html` | 48372 | 待审 | — | 尚未形成有证据结论 |
| `settings.gradle.kts` | 336 | 已审计 | 必须保留 | Gradle构建入口；仅 include(:app)，Google/MavenCentral/插件仓库和项目身份；无被删 web/独立 Rules module，后续不能据历史路径虚构消费者。（静态，运行结论仅明确列出者） |
| `tools/ai_conversation_probe.py` | 8245 | 已审计 | 需要解耦 | F12-D全文/真实引用证据0f904f7031770bd2f12acea4299aa9b1d5c8e5c0；仅手动CLI及test_ai_conversation_probe消费，历史AI_CONVERSATION_1文档标记prepared-only；未接Android生产/Gradle/当前CI该工具调用。保留必要离线APP请求结构核对、显式单次付费gate、无retry/私密输出边界；store=true/previous_response_id/reasoning/delta是独立实验，不是现用stateless Host合同。--host-verified仅operator断言不能代替事实安全或预算授权；后续按工具用途缩窄，不凭5个离线绿灯声称供应商支持或生产验收。本轮未运行send/写出私密请求。 |
| `tools/asp_oracle/oracle_harness.py` | 22933 | 待审 | — | 尚未形成有证据结论 |
| `tools/asp_oracle/scenarios/trouble_brewing_a2.json` | 44315 | 待审 | — | 尚未形成有证据结论 |
| `tools/asp_oracle/test_oracle_harness.py` | 6979 | 待审 | — | 尚未形成有证据结论 |
| `tools/gsp_api0_responses.py` | 12988 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；仅独立API0离线bench workflow及专属测试，模型输入仍带旧合法candidate ids，与现行App直接AI规划不同；保留有价值benchmark前先核对工具需求，旧运行器可退休且不得无预算运行--live。（静态审计） |
| `tools/prod_global_latency_profile.py` | 14080 | 已审计 | 需要解耦 | F12-F全文+真实生产/测试引用，代码基线569759560837591695168244b1a4eeeca21fb574；prod1-gateway-offline workflow/tests使用历史Gateway→Responses耗时比较；不是当前App请求/解析合约，潜在保留性能研究价值，模型调用live选项不得在本轮启用。（静态审计） |
| `tools/prod_global_live_smoke.py` | 13173 | 待审 | — | 尚未形成有证据结论 |
| `tools/setup/migrate_tb_drunk_templates.py` | 4626 | 待审 | — | 尚未形成有证据结论 |
| `tools/setup_field_test_keystore.sh` | 1965 | 待审 | — | 尚未形成有证据结论 |
| `tools/storyteller_gateway.py` | 40604 | 待审 | — | 尚未形成有证据结论 |
| `tools/test_ai_conversation_probe.py` | 2268 | 已审计 | 需要解耦 | F12-D全文/真实引用证据0f904f7031770bd2f12acea4299aa9b1d5c8e5c0；全文5个unittest只测prepare/check_app_request：opening变store、chain operator anchor、baseline stateless、旧schema/模型排除、实验delta；不调用cmd_send，不覆盖真实网络/模型支持/私密文件权限。随实验分支退休相应用例，若保留工具则保留必要离线baseline/结构/安全验证。当前CI没有引用该test，不能拿其它CI冒充它通过；本轮单独离线运行5 tests OK，未写pycache或调用网络。 |
| `tools/testing/summarize_junit.py` | 2979 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_gsp_api0_responses.py` | 5431 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_prod_global_latency_profile.py` | 4194 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_storyteller_gateway.py` | 22183 | 待审 | — | 尚未形成有证据结论 |
| `tools/tests/test_storyteller_gateway_compact.py` | 4932 | 待审 | — | 尚未形成有证据结论 |
| `ui/components.css` | 22652 | 待审 | — | 尚未形成有证据结论 |
| `ui/design-tokens.css` | 4351 | 待审 | — | 尚未形成有证据结论 |
| `ui/ui-showcase.html` | 21113 | 待审 | — | 尚未形成有证据结论 |
| `ui/ui-showcase.js` | 1810 | 待审 | — | 尚未形成有证据结论 |

## F11：信息真值/登记投影与 Mayor 候选链已解耦（完整 CI/R2 已通过）

- **核对基线**：本地/远端 `f2c09e175e8e70e2394594806ee7e969d285a996`、PR #334 Draft/base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`；其 docs-only CI #4367 / R2 #3947 SUCCESS，代码基线仍为 `800f1d97` 的完整 CI #4366 / R2 #3946。Mini MCP 是独立旧 checkout `a2cbcac8`、clean，不在那里修改。本批按用户明确指示先完成 B4/B5 解耦，再交接新对话做逐文件分类审计。
- **B4 信息链**：现有 presentation model 内的窄 `ClocktowerInformationQuestion` 仅携带 Number/Boolean 指标和实际查询 subjects 或 Role 查询座位；source 仍由当前 actor 与 canonical Session roster 定位，没有新增可写游戏状态。Builder → current scalar request、joint opening numeric matcher、direct numeric input 不再从真值/候选菜单抽取问题。Chef/Empath/Clockmaker/Chambermaid 输入任意非负 Int；FT/Chambermaid/Ravenkeeper 等须先有实际目标；Role 揭示可选择已知角色。保留原私密展示、手工/AI_ASSISTED 预选、AI_AUTOMATIC 发布回调、实际 API/提示词/解析/记忆及 revision/recipient 边界。直接输入绑定真实 actor 和 targets；改变数字查询目标会重置输入。
- **B4 删除链**：HostScreen 的 WW/Librarian/Investigator 默认真角色/自动 decoy、Chef/Empath/FT/Clockmaker/Chambermaid 真值计算、角色结果默认真值/登记假设及发布时真值回退已退出。RegistrationOptionPreparation → ResultPresentation/ResultDomain → RegistrationProjection → TroubleBrewingRegistrationDomain 整链删除；日间 Nomination/Slayer 直接保留明确选择，退出该规则投影的 poison/role 合法域门禁，未删除真实登记事实生产者。FixedInformationEvaluator 整删，livingNeighbors 只保留现有查询座位定位，不算阵营数字。旧 Chef/Empath/Clockmaker 方桌候选 renderer、Chambermaid wake-count/0..2 旧 helper 及无人消费的夜间登记 metadata 同时删除。
- **B5 Mayor 链**：pending 只保留 identity/revision/source，AI Provider 不再发 legalCandidates，返回的 SEAT_TARGET 按当前座位/版本直接预选。App/Session 删除 candidate ID 回匹配、living-actual-Mayor 事实门禁和排除 Demon 座位的合法域；Session 保留 request game/source/revision、已知座位、去重与单 writer 原子 ActionFact。一次确认只有实际 MayorRedirectChoice，不再为同一动作追加候选 causal capture/decision/correction；AI 历史仍从实际 ActionFact 读取。Dawn planner 使用已确认目标，不因目标后来成为 Demon 改杀 Mayor；保留原死亡、角色改变、继承和基本续局应用。
- **整删生产 10 文件**：
  - `ClocktowerChefSquareTableUi.kt`
  - `ClocktowerClockmakerSquareTableUi.kt`
  - `ClocktowerEmpathSquareTableUi.kt`
  - `ClocktowerRegistrationOptionPreparation.kt`
  - `ClocktowerRegistrationProjection.kt`
  - `ClocktowerRegistrationResultDomain.kt`
  - `ClocktowerRegistrationResultPresentation.kt`
  - `clocktower/rules/FixedInformationEvaluator.kt`
  - `clocktower/rules/MayorRedirectLegality.kt`
  - `clocktower/rules/TroubleBrewingRegistrationDomain.kt`
- **整删专属旧测试 11 文件**：
  - `ClocktowerChefSquareTablePresentationTest.kt`
  - `ClocktowerClockmakerSquareTablePresentationTest.kt`
  - `ClocktowerEmpathSquareTablePresentationTest.kt`
  - `ClocktowerRegistrationOptionPreparationTest.kt`
  - `ClocktowerRegistrationProjectionTest.kt`
  - `ClocktowerRegistrationResultDomainTest.kt`
  - `ClocktowerRegistrationResultPresentationTest.kt`
  - `FirstNightPairInformationSelectionDomainTest.kt`
  - `clocktower/rules/FixedInformationEvaluatorTest.kt`
  - `clocktower/session/MayorRedirectDecisionBoundaryTest.kt`
  - `clocktower/session/StorytellerDecisionFoundationTest.kt`
- **保留/迁移的验证**：当前无答案/无候选菜单时的首夜/后夜请求与已发表不可重开、首夜 joint 数字直接建议、查询 targets 与 actor 身份、任意 99 数字及阴性结构检查、Role 查询无真值锚仍接受手工/AI、actual information 不反推 Spy/Recluse 登记、Mayor Demon 目标接受/未知座位拒绝/源不匹配/过期版本/失败无半写/重复 no-op/改选事件顺序、Dawn/角色变化重建不改写已确认目标。保持实际 AI transport/模型响应/战略与隐私测试；退役旧 truth/候选/witness-only 断言，不恢复旧规则为测试兜底。
- **实际规模/本地验收**：生产 +258/-3085（净删 **2827**），测试 +169/-2020（净删 **1851**），合计净删 **4678 行代码**。当前存量 **905 跟踪文件、246 生产 + 195 测试 Kotlin**。`testFull + assembleDebug` SUCCESS，**819 tests，0 failure/error/skipped**；diff whitespace check 通过。超限源码：App **178170B**、HostScreen **101971B**；NightStepUi **44505B** 已低于 50000B。代码 **`d9fb47f2e805339736f6a70cd3b347090810bea9`** 的完整 Android **[CI #4369 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071137)**（完整单测+Debug APK）、**[R2 #3949 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071147)**，ASP/真实 Clingo/CI gate SUCCESS；本地 JVM/APK 和 CI 不等于实际真机或付费模型验收。
- **明确保留与剩余**：本批完成上述两条指定链，不宣称 P1 或全仓语义审计完成。Sage 仍有默认真值/pair 与 truth 标签、Spy 魔典/poison 分支、日间 Virgin/Slayer 后果、其它行动/继承/发牌门禁、剩余 candidate presentation/Provider DTO、因果日志及重复状态/历史仍须逐文件分类审计；必要死亡/角色变化/继承应用不能因含规则就整模块删除。跨项目 codec、安全/平台、tools/CI 必须读真实消费者。无新增必须由用户决策的产品阻塞项；潜在范围问题在下轮用证据列人工审核，不能假称已经裁定。
- **远端初次检查与修正**：代码 `56a927979fad03402d9a3de6e611db5ce2eb7840` 的 GitHub diff 独立核对为68文件+506/-5301，生产净删2827、测试1851，无未授权目录变更；R2 #3948 因要求已退休 ClocktowerRegistrationDetail 存在的旧源码守卫失败。仅移除该存在性断言，保留其余边界检查；从当前 workflow 提取的实际 R2 Bash 脚本在本地执行 SUCCESS。修正提交 `d9fb47f2e805339736f6a70cd3b347090810bea9` 带 [full-ci] 重新验收，完整 CI #4369 / R2 #3949 均 SUCCESS，旧失败已解决；本批没有通过复活旧 enum 满足源码字符串守卫。
- **交接与边界**：NEXT_DEVELOPMENT_HANDOFF 和 CURRENT_DEVELOPMENT_ROADMAP 已改为单一当前入口，旧批次历史只留本台账；新对话先核实最新远端/head/CI，再按“必须保留（Host 控制/记录/AI 推荐/平台安全支撑）、必须删除、需要解耦、需要人工审核”逐文件审计。待审状态不是已审核类，混合文件需分职责；完成比例按有证据的最终结论数/明确范围统计，不以扫描或净删除率冒充审计进度。Draft #334 不合并/retarget，不动既有未跟踪文件，无付费 API/真机操作。

## F10-B：孤立候选与旧世界合同整组删除（完整 CI/R2 已通过）

- **起点与范围**：远端/本地 `968261005f0003a137745eedbdaafbe9d4c40040`，Draft #334；沿 F10-A 的全树分类落实 A1–A3。重新核实无 App、Session、现用 AI、tools/CI 消费者后删除封闭旧类型岛，不把混合文件整体判死。
- **整删生产 7 文件**：`ClocktowerBooleanDisplayOptionSelection.kt`、`clocktower/domain/DecisionCandidate.kt`、`DynamicInformationOutcome.kt`、`EffectDraft.kt`、`RecommendationModels.kt`、`SetupClueOutcome.kt`、`StableCandidateIdFactory.kt`（后六个均在 domain/）。移除链为旧 candidate/effect/plan/ID → 孤立辅助/专属测试；实际事实仍用 ActionFactDraft/EpistemicObservationDraft。
- **混合职责删除**：StorytellerProviderContractV1 删除旧 response/outcome/recommendation/confidence/validator 尾部，保留现用 request/context/prior-decision；DecisionTypes 删除无人使用的 DetectionSemantics/AbilityType。EpistemicContracts/SemanticModel/Json 删除 F10-A 列出的 13 个旧世界、合法选择、登记推演类型及专属 codec/identity helper，并移除无人使用的 GOOD 红鲱鱼 eligible helper；保留 TimelinePoint、真实 ActionFact/Observation、Grimoire、PlayerKnowledge、安全稳定 ID 和现用 JSON。
- **测试**：整删 `ClocktowerBooleanDisplayOptionSelectionTest.kt`、`clocktower/domain/UnifiedDecisionModelsTest.kt`、`clocktower/epistemic/TimelinePointJsonCompatibilityTest.kt`（最后一项仅调用退休 RegistrationQuery codec）；混合 MurmurHash3/Provider/SemanticModel/ObservationTimelineBinding 测试只移除旧候选/世界段。现用观察日志仍验证缺失 globalSequence 拒绝、旧 local 显式兼容、全局序列一致性、隐私和 round-trip；保留实际 hash vectors、AI request/stale/隐私测试。
- **实际验证/规模**：本地 `:app:testFull :app:assembleDebug` SUCCESS，**883 tests，0 failure/error/skipped**。生产 +2/-816（净删 814），测试 +0/-432，合计净删 **1246 行代码**。Git 跟踪存量预计 **926 文件，256 生产 + 206 测试 Kotlin**。仍有两个 >50000B 源码：App **180424B**、HostScreen **144485B**；NightStepUi **49650B**。`git diff --check` 通过。代码 **`800f1d9797ecb0154c970a8a7cd8c58f5352d588`** 已经 GitHub Connector 独立比较远端 24 文件 +25/-1248 diff；完整 Android **[CI #4366 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39113685085)**（完整单测+Debug APK）、**[R2 #3946 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39113685103)**，ASP/真实 Clingo/CI gate SUCCESS。退役符号在现用 app/src、tools、CI 无残留；跟踪工作树干净，未跟踪用户文件保留。
- **事实/AI 风险及剩余**：本批没有改 Session 写入、事件身份/顺序、实际信息、recipient/revision、AI transport/parser/策略记忆/预选/模式；没有 API 调用或真机验收。B4 真实问题/真值/登记投影链、B5 Mayor 候选与实际死亡/继承链仍待先解耦后删除，不能称 Host Rules 已清空；无新增所有者决策，跨项目核心 codec 和现用工具保留。PR #334 保持 Draft，不 merge/retarget；用户未跟踪文件不动。

## F10-A 审计：F9 后剩余高置信清理对象（仅审计，未删除源码）

- **核对基线**：GitHub PR #334 仍为 Draft，远端/本地 HEAD `7552918161508522c2a0e677f38df6a49bf1a9ca`、base `64be30905b88f639a464204ed1db89b7b62bc910`，main `61cd6796569ed8dfdea360caefeb5b9328745090`；Mini MCP 独立旧 checkout `a2cbcac8`，不能当成本地工作副本。代码基线 `4113974d` 完整 Android CI #4363 / R2 #3943 SUCCESS；当前 docs-only HEAD CI #4364 / R2 #3944 SUCCESS，不能作为新 Android 编译证明。
- **覆盖口径**：读取未截断的全部 936 文件 Git tree，按目录/体积横向比较 263 个 Android 生产 Kotlin、209 个测试 Kotlin，以及 tools/CI；对下述候选检查声明、真实调用、manifest、工具/工作流和混合测试。它是全树库存加候选语义审计，**不等于 263 个生产文件均已逐函数完整审计**。初筛发现“零 Kotlin 引用”的 Application 实际由 manifest 加载，已排除；没有仅按名称、正则扫描或测试数量判死代码。

### A：可以直接清除的高置信对象

1. **F9 删除后孤立的候选模型/辅助岛：7 个整文件，274 生产行 / 9277B。**
   - `clocktower/domain/DecisionCandidate.kt`：CandidateMetadata/DecisionCandidate/DecisionEvaluation；只有旧模型专属测试，已无生产生成器或消费者。
   - `clocktower/domain/DynamicInformationOutcome.kt`：旧 Number/Category 候选包装，生产/工具/工作流无外部消费者。
   - `clocktower/domain/EffectDraft.kt`：InformationValue/PlayerStatus/EffectDraft 只互相引用或被上述孤立 DecisionCandidate 引用；真正事实仍是 ActionFactDraft/EpistemicObservationDraft，不是这个候选 effect。
   - `clocktower/domain/RecommendationModels.kt`：CandidatePlan、StorytellerDecision、QualityTier 等只由本岛的 SetupClueOutcome/DecisionCandidate 引用，不是现用 OpeningRecommendationV2 或 StrategicContinuation。
   - `clocktower/domain/SetupClueOutcome.kt`：只剩自身声明；旧开局生成器已退役。
   - `clocktower/domain/StableCandidateIdFactory.kt`：无生产消费者，仅 MurmurHash3Test 的旧候选 ID 用例。保留实际 setup/seed 使用的 MurmurHash3 和 hash golden vectors，删除候选 ID 专属用例即可。
   - `ClocktowerBooleanDisplayOptionSelection.kt`：findBooleanDisplayOption 仅独占测试调用；F9 已删除其真实调用方，现用真假直接结果不需要反查菜单。
   - 测试：可整删 UnifiedDecisionModelsTest、ClocktowerBooleanDisplayOptionSelectionTest；MurmurHash3Test 只删候选部分，不连同活跃 hash 测试一起删。DecisionTypes.kt 的 DetectionSemantics/AbilityType 也没有生产/工具消费者，可以连带精简；保留仍服务 Session/观察/登记事实的 AbilityState、TruthRelation、RegistrationFact 等。
2. **旧 V1 Provider 响应/候选验证器：144 生产行 / 5706B，混合文件中的完整尾部。**
   - `clocktower/domain/StorytellerProviderContractV1.kt:420` 起的 Response/Outcome/Recommendation/Confidence/Validation/ResponseValidator 七个类型只在本段和 StorytellerProviderContractV1Test 的旧 response 用例中引用。其职责是 primary/alternative candidate ID 回匹配和 deferred response，不在真实 OpenAI 网络/解析/记忆路径。
   - 可直接删除该尾部和专属旧断言；**不能整删文件**：Request、DecisionContext、Revision、GameContext、PriorDecision 等仍由 App、Session、AI 续规划与实际因果日志消费。也不能据此整删剩余 Candidate DTO：Mayor 和日间登记事实生产者仍在使用它们。
3. **旧世界/合法选择合同残留：三个混合文件中的封闭分支。**
   - `epistemic/EpistemicContracts.kt`：EpistemicHypothesis、RegistrationQuery/Basis/Profile/Semantics、TroubleBrewingRegistrationSemantics、WorldCardinality、CandidateFamilyId、WorldExplanationClusterId、PlayerWorldSetIdentity。
   - `epistemic/EpistemicSemanticModel.kt`：StorytellerDecisionPoint、LegalEpistemicChoice、LegalChoiceSet；与上一项合计 13 个顶层旧类型。
   - 上述对象的外部“生产”引用仅为 EpistemicSemanticJson 的对应 overload/codec 和对象内部 ID 构造，没有 App/实际 AI/Session 入口；tools/CI 无这些类型或旧 schema kind 消费者。旧 RegistrationSemantics 算法只有 EpistemicSemanticModelTest 调用。可连同 Json 中相应 encode/decode/private helper 和混合测试的旧世界/合法候选段一起删除。
   - 三个文件总库存为 1281 行 / 72296B，**这不是可删除量**，不把整文件体积当收益。必须保留 TimelinePoint、InformationProposition、Recorded/EpistemicObservation、可见范围/Grimoire、安全稳定 ID、实际事件 JSON，以及仍与观察日志/历史兼容有关的部分。保留 World identity 测试中真正的隐私/顺序性质时，应在现用日志合同验证，不为保旧 wrapper 继续供养世界合同。

A1+A2 确定整段生产体量为 **418 行 / 14983B**，另有 A3 数百行混合职责及专属测试可退役；精确净减少量应在真实删除后由 Git diff 统计，不能把“关联文件体量”算成净删除。建议把 A1–A3 合为一个封闭删除批：优先退出旧世界/合法选择合同，再一次收走孤立类型与旧响应验证器。

### B：删除方向置信高，但先断开当前事实/交互消费者

4. **Host 信息真值与登记见证投影（下一大批优先）**：RegistrationOptionPreparation、RegistrationResultPresentation、RegistrationResultDomain、RegistrationProjection、rules/FixedInformationEvaluator 五个关联文件合计 463 行 / 21878B，另有 HostScreen 中大段 Chef/Empath/FT/角色结果真值/枚举和 InformationStepBuilder 的旧候选字段。真实链是 HostScreen → RegistrationOptionPreparation → ResultPresentation/Domain，并经 RegistrationProjection → TroubleBrewingRegistrationDomain；FixedInformationEvaluator 由 HostCoreSemantics → HostScreen 使用。
   - Host 不应继续算线索真假或枚举 Spy/Recluse 见证，因此方向明确。先让当前问题显式持有 source/metric/实际查询 subjects，再移除旧数值/角色菜单及回退真值；保留实际输入、AI advice、Session 发布、recipient/revision 和必要座位展示。livingNeighbors 与其它必要查询/座位投影须单独判断，不能以删数字计数为由抹掉实际目标语义。
   - Builder 已把 night step 的 spy/recluseRegistrationKey 强制为 null，部分旧分支实际不会进入；但相同登记 State/Controls 仍被白天 Nomination/Virgin/Slayer 的明确登记事实操作消费，**不整包删除全部登记 UI**。日间实际确认不得被夜间“见证推导”连带删除。
5. **Mayor 合法候选/确认及通用 pending 候选底座**：MayorRedirectLegality、MayorRedirectDecisionBoundary、StorytellerDecisionFoundation 三个文件总库存 258 行 / 9935B。Mayor 边界是 PendingStorytellerDecision 的唯一生产消费者；规则域又被 Session.commitConfirmedMayorRedirect、NightDawnResolutionPlanner、NightHostProjection 使用。当前排除恶魔座位的产品限制与最新 Host 事实职责不符，应删除规则限制和候选 ID 中转。
   - 先保留已有 request/revision、实际重定向 seat 与后续死亡/继承事实应用，再退出域与确认包装。Foundation 前段 RequestIdentity/Revision 仍被活跃 AI/其他事实大量引用，不能连根整删。相关旧合法性测试可退役，去重/过期/原子事件/基本续局测试保留。

### 本轮不列为“可直接整删”的对象

- **旧 Chef/Empath/Clockmaker 方桌 UI**：3 文件库存 816 行 / 32332B。Chef 的真实 typed numeric 路径会在 NightStepUi 前段转直接数字输入；部分旧 UI 可以跟随 B4 化简，但 **Clockmaker materializer 仍未提供 typed NumericResult，旧数值展示兜底仍是真实路径**。不能因看到 F9 的 early return 就宣布三文件全无消费者。先补齐当前问题/输入合同再删除旧重复 renderer；不把源码 shape 测试当实际产品证据。
- **Epistemic 时间线/隐私、PlayerHistoricalTimeline 与 TB snapshot Json codec**：部分无 Android live 调用，但属于明确保留的观察/公共历史安全及跨 EvidenceLab 合同，不能仅凭零调用列为死实验；当前保留。后续合同收敛另议，避免误删 core serialization/security。
- **Python gateway / API0 / prod_global 工具**：不是已经证明无用的孤岛。prod1-gateway-offline、api0-offline、ci.yml 仍实际执行对应脚本/测试；这只证明有开发验收消费者，不自动证明长期产品必需，但本轮证据不足以批准整包删除。需要先判断现用连接/验收职责，不能为腾空间删掉所有 AI 工具。
- **Dawn、死亡、角色变化、继承、投毒等**：含规则和实际状态应用的混合模块；应去规则否决，保留必要事实提交和安全继续。此次不认定为整模块垃圾，也不扩大成删除现有 AI/基本 Host 操作。

- **本轮变更/验收**：仅形成审计结论与更新交接，生产删除 0、测试删除 0、净代码减少 0；未改状态/事件/AI 代码，因此不重复运行 Android 测试。最近完整代码验收仍为 `4113974d` 的 CI #4363/R2 #3943（898 tests + Debug APK）；未运行付费 API/真机。现仍有 App 180482B、HostScreen 144543B 两个超限源码，NightStepUi 49650B。未新增必须由所有者决策的阻塞项，工具/跨项目范围保守保留。PR #334 保持 Draft，不 merge/retarget，原有未跟踪文件不动。

## F9：四模块整组解耦与删除（完整 Android CI/R2 已通过）

- 起点：GitHub/本地 `a3008b33ff9fa4d8f8fc11389ef1f972fb529323`，main `61cd6796569ed8dfdea360caefeb5b9328745090`，Draft #334/base `64be30905b88f639a464204ed1db89b7b62bc910`；fetch 后一致。Mini MCP 仍为独立旧 checkout `a2cbcac8`，本批直接改 Windows 本地文件。完整未截断 Git tree 974 文件；本批工作树预计 936，生产 263 + 测试 209 Kotlin。历史版本库存不代表逐文件语义审计完成。
- **Pair**：App/current residual scope/HostScreen 的真实 AI 请求保留，只发送当前 recipient/ability/revision/problem。删除 PairInformationDecisionBoundary → PairInformationLegalDomain → NaturalPair/DisplaySemantics 真值、见证与合法候选枚举链；保留窄 PendingPairInformationDecision 身份 DTO。直接建议生成 witness-free typed display，不匹配合法候选 ID；手工 pair 允许任意已有座位（含 actor）及已知剧本角色，零结果仍表达本角色类别的缺席陈述，不从显示反推登记。
- **Numeric/Boolean**：删除 Coordinator → NightRecommendationModule → DynamicCandidateGenerator → InformationDecisionFoundation → StructuredNumber/Boolean UI model/adapter 的生成、值域/真假分类、候选包装与确认链；删除 FirstNightNumericInformationSemantics 真值枚举、UI truth projection 与 UnifiedSelectionPool。统一使用已有 ReliabilityState，退出重复枚举/版本 aliases。Chef/Empath 等直接数字输入保留非负 Int 的结构限制，AI_ASSISTED 预填、AI_AUTOMATIC 通过同一实际发布回调；FT 等只要求真实已选查询座位，直接接收真假结果，不以旧真值域二次否决。
- **Setup**：删除 Drunk 候选域/假设 GameState projector 和第二候选确认边界；原 pending object/screen/game identity 仍隔离旧点击，最终 commit 一次构造实际状态。任意已发牌座位都能被明确确认为 Drunk，保留 shown role 和实际 Drunk 元数据；未知/改名/换牌引用仍拒绝。红鲱鱼选座和 Session/SessionState 均退出善良阵营门禁；恶魔伪装使用所有已知剧本角色，Session/SessionState 不再拒绝已在场角色，去重、事件时序与单 writer 保留。
- **后夜接收**：StorytellerStrategicContinuationV2 的真实 transport、结构化 JSON、提示词、原模型响应 anchor、历史增量和多夜战略记忆保留；直接返回 SemanticResult，删除“语义 → 当前 legalCandidates 匹配 → 内部候选 ID → UI 再匹配”。保留严格字段/整数、当前问题/实际目标、已知角色与座位、game/revisions/global-prefix/memory-version、完整响应和已发布不可重开校验。记忆只在同一私密 recipient 实际收到相同结果后晋升；不凭建议写事实。Session 发布端增加当前 revision 校验，替代退役候选 snapshot 的过期安全职责。
- **整删生产 19 文件**：
  - `ClocktowerRecommendationRules.kt`
  - `StructuredBooleanInformationAdapter.kt`
  - `StructuredNumericInformationAdapter.kt`
  - `clocktower/recommendation/UnifiedSelectionPool.kt`
  - `clocktower/recommendation/dynamic/DynamicCandidateGenerator.kt`
  - `clocktower/recommendation/dynamic/UnreliableInformationModels.kt`
  - `clocktower/rules/FirstNightNumericInformationSemantics.kt`
  - `clocktower/rules/NaturalPairInformationCandidateGenerator.kt`
  - `clocktower/rules/PairInformationDisplaySemantics.kt`
  - `clocktower/rules/PairInformationLegalDomain.kt`
  - `clocktower/session/ClocktowerRecommendationCoordinator.kt`
  - `clocktower/session/DrunkAssignmentDecisionBoundary.kt`
  - `clocktower/session/InformationDecisionFoundation.kt`
  - `clocktower/session/PairInformationDecisionBoundary.kt`
  - `clocktower/session/RecommendationModules.kt`
  - `clocktower/session/StructuredBooleanInformationUiModel.kt`
  - `clocktower/session/StructuredNumberInformationUiModel.kt`
  - `clocktower/setup/TroubleBrewingDrunkCandidateProjection.kt`
  - `clocktower/ui/StructuredStorytellerInformationUi.kt`
- **整删专属测试 20 文件**（旧真值/合法候选、包装/假设投影/来源形状断言；真实 AI 与事实安全测试保留并改接直接语义）：
  - `ClocktowerRecommendationRulesTest.kt`
  - `ClocktowerStructuredInformationPreparationTest.kt`
  - `FirstNightPairInformationPoolLegalityTest.kt`
  - `StructuredChefInformationAdapterTest.kt`
  - `StructuredEmpathInformationAdapterTest.kt`
  - `StructuredFortuneTellerInformationAdapterTest.kt`
  - `clocktower/recommendation/NaturalPairInformationCandidateGeneratorPerceivedAbilityTest.kt`
  - `clocktower/recommendation/NaturalPairInformationCandidateGeneratorTest.kt`
  - `clocktower/recommendation/PairInformationLegalDomainTest.kt`
  - `clocktower/recommendation/UnifiedSelectionPoolTest.kt`
  - `clocktower/recommendation/dynamic/DynamicCandidateGeneratorTest.kt`
  - `clocktower/rules/FirstNightNumericInformationSemanticsTest.kt`
  - `clocktower/rules/PairInformationDisplaySemanticsTest.kt`
  - `clocktower/session/ClocktowerRecommendationCoordinatorTest.kt`
  - `clocktower/session/InformationDecisionCoordinatorIntegrationTest.kt`
  - `clocktower/session/InformationDecisionFoundationTest.kt`
  - `clocktower/session/PairInformationDecisionArchitectureTest.kt`
  - `clocktower/session/PairInformationDecisionBoundaryTest.kt`
  - `clocktower/session/StructuredNumberInformationUiModelTest.kt`
  - `clocktower/setup/TroubleBrewingDrunkCandidateProjectionTest.kt`
- **验证**：本地 `:app:testFull :app:assembleDebug` SUCCESS，898 项、0 失败/错误/跳过。旧候选测试/fixture 编译及旧断言曾失败，已按新职责退休/修改；过程中删 import 误去掉 Compose 委托运算符，编译发现后已恢复。重点回归：直接 99 数字与不在旧菜单的 pair/role、未知/重复/小数/越界座位拒绝、真实已存首夜模型响应、后夜记忆/历史/收件人、非镇民 Drunk、evil 红鲱鱼/in-play bluff 的单 writer 事件及重放、过期回复不改 Session。CI routing 只移除 R2 对已退役 unifiedFirstNightInformationPool 的存在性守卫；其他安全/构建/R2 验收保留。没有付费 API 或真机验收。
- **规模**：生产 +410/-3215，测试 +165/-2649，源码净删 **5289 行**（以本地 Git diff 加新 DTO 实测；另 R2 -1 行）。NightStepUi **49650B** 已低于 50,000；仍超限 App **180482B**、HostScreen **144543B**，源码按 canonical UTF-8/LF，修改的生产文件本地亦保存 LF。
- **明确剩余/风险**：这不是全部 Host Rules 清空。HostScreen/InformationStepBuilder 的角色信息真值/登记预览、ResultFirstRegistrationDomain 与 FixedInformationEvaluator、Mayor/NewDemon/其他行动候选及旧 Provider 通用候选 DTO 仍需横向审计；其中部分服务当前 Host flow，要先断开必要事实/交互。Pair 手工 UI 当前仍把结构化组合物化为展示列表，未来可继续化简。App/HostScreen 两个大混合文件须继续删职责并收敛到 <=50,000B。跨项目状态合同/真机/实际收费模型品质未验收；无新所有者决策。保留唯一 Session、既有结构化事实与观察日志、私密交付/事件顺序；PR #334 保持 Draft，不 merge/retarget。

- **Exact code HEAD 验收**：`4113974d840f1c201d6b9aa7686193435c710f9b`，GitHub 完整 Android **[CI #4363](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39111596271) SUCCESS**（Android tests/Debug APK）、**[R2 #3943](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39111596284) SUCCESS**，ASP、真实 Clingo、CI gate 均成功。GitHub 独立 compare 为 96 文件 +641/-5865（含文档），生产 +410/-3215、测试 +165/-2649，**源码净删 5289 行**；另 R2 净删 1 行。实际提交 tree 936 文件、263 生产 + 209 测试 Kotlin，两个超限文件及 NightStepUi 49650B 已以完整 Git tree 复核。后续 docs-only HEAD 不替代此代码的 Android 验收。未改变用户原有未跟踪文件，未合并/retarget Draft #334。

## F8：首夜无效候选预计算、等待门禁与重复上下文整链删除（本地完整构建与 exact-SHA CI/R2 已通过）

- 起点：远端/本地 `0e7e4a4e6c1c51c3522456393349d51240e109b3`，main `61cd6796`，PR #334 Draft/base `64be3090`；Mini MCP 仍为 `a2cbcac8`，本批继续本地直接读写。读取完整 979 文件树，横向比较大文件/消费者后选中整个首夜预计算链，而非沿某个规则小函数逐段清理。
- **消费证据**：App 在 setup commit 后预热整局 natural pair 候选；HostScreen 通过 ready/result provider 拉取并保存在 `firstNightNaturalPairCandidates`。该结果没有送入任何信息 Builder、AI 请求或确认回调，唯一读取用途是 `firstNightNaturalPairPrecomputeReady`，进而阻塞手动/自动开始首夜并保留失败重试 UI 状态。实际 AI 配对由另一个 current-step producer 独立生成，信息展示已有结构化手工选项。
- **整删 3 个生产文件**：`clocktower/session/TroubleBrewingFirstNightPrecomputeCoordinator.kt`（缓存/后台任务/BUSY/READY/fallback）、`clocktower/recommendation/TroubleBrewingFirstNightPairDecisionContext.kt`（snapshot 重建 poison-neutral GameState、角色定义拷贝及忽略版本/投毒的缓存相等性）、`clocktower/recommendation/NaturalPairInformationCandidateGenerator.kt`（兼容转发层）。删除 App 的 prewarm scope/调度/context builder/双 provider，HostScreen 的候选缓存/异步加载/失败重试/待开始状态以及纯预计算等待门禁；确认当前必要 setup 信息后直接进入实际首夜步骤。
- `ClocktowerRecommendationCoordinator` 删除只为预计算存在的 naturalPairCandidates 和 SetupClueOutcome/EffectDraft 包装；实际 rules/NaturalPairInformationCandidateGenerator 删去已经没有生产消费者的整局 perceived 汇总、其角色识别 helper 和旧 functioning 入口。**保留**仍被 PairInformationLegalDomain 消费的 generateHealthyInformationSpace，不重建或扩展规则算法。
- **AI 等价接续**：`pendingGlobalFirstNightPairDecision` 直接接受当前 canonical Session 的 GameState/gameId/revision，调用现存 AI-only pair boundary；App、HostScreen 和残余 scope 全部改用同一事实/版本输入。保留座位顺序/来源身份引用检查及真实请求/响应 key、战略记忆、已确认事件历史和过期异步隔离；不再为配对建议复制另一份 roster 或把毒状态归零。角色定义使用已有 TB 元数据函数（旧 context 测试已证明它与 registry 投影相同），没有引入新 Context/Manager 或第二份 writer。
- **测试处理**：整删 `TroubleBrewingFirstNightPrecomputeCoordinatorTest.kt`、`TroubleBrewingFirstNightPairDecisionContextTest.kt` 两个专属旧测试；去掉只验证已退役 functioning 入口拒绝 poison 的一项旧测试。保留并改为直测实际 rules owner 的自然配对、Drunk perceived Investigator、NoGreaterJoy 通用配置测试；保留首夜真实已存模型响应、配对 Provider/预选和战略续规划测试。补充真实 Session 投毒事件前后配对请求版本更新/残余 Provider 接续、来源身份/座位漂移拒绝、非首夜/非实际步骤退出及事实不被生产建议改写的回归。
- **本地验收**：首次 testFast 在测试编译发现遗漏的旧包名消费者，已迁移到实际 owner；补充测试中的 Provider revision 字段名修正后，最终 `:app:testFull :app:assembleDebug` SUCCESS，**973 项、0 失败/错误/跳过**。没有为旧测试恢复预计算或规则入口，没有执行付费 API/真机。
- **规模/风险**：生产 +31/-554，测试 +76/-313，源码净删 **760 行**；整删 3 个生产、2 个测试，无新增文件。树 979→974，Android 生产 284→281、测试 231→229。三个超限源码按 UTF-8/LF 实测：App **184919B**、HostScreen **145615B**、NightStepUi **60237B**。唯一 Session、事实/观察日志、收件人和原子提交未修改；变化只退出无用缓存与等待，并将活跃 AI producer 绑定当前 canonical facts。代码 `119de9496e52088a0fde164aeafd24973743bfda` 的 GitHub 完整 Android **CI #4361 SUCCESS**（单测/Debug APK）、**R2 #3941 SUCCESS**，ASP、真实 Clingo、CI gate 均成功。独立 GitHub diff：18 文件 +130/-867（含文档），源码净删 760 行；完整非截断 Git tree 实测 974 文件、281 生产 + 229 测试 Kotlin及上述三个超限源码。此 SHA 为本批代码验收基线，后续 docs-only HEAD 不是新的 Android 验收。
- **明确剩余**：当前 pair AI-only 旧候选边界、Numeric/UI/setup/目标合法域及后夜 response candidate matching 仍需解耦；不能把本批删除说成所有 Host 规则门禁已清空。其余大块按实际职责继续审计，没有新增所有者决策，PR 保持堆叠 Draft。

## F7：优先集中删除已证实无生产消费者的旧实现（本地完整构建及 exact-SHA CI/R2 已通过）

- **执行顺序按所有者最新指示调整**：先横向比较全树的体量、职责和消费者证据，集中整删规模较大且有把握的旧实现；清完明确可删部分后，再逐文件审计剩余混合模块。不再默认沿酒鬼确认链逐函数推进，也不先建立另一套文件台账。
- 起点：GitHub/local `13228a7e7dbe994d1eeebe6fa108389e27401bdd`，PR #334 Draft、base `64be3090`；Mini MCP 仍停在 `a2cbcac8`（本批不通过它改代码或提交）。已读取完整 998 文件树，横向比较全部 Kotlin 模块；词法引用扫描只作候选筛选，最终删除逐项以完整受控源码/XML/工具/CI 的符号和路径引用复核为依据。普通本地未跟踪文件不动。
- **整删 11 个生产文件（444 行）**：
  - `clocktower/rules/FortuneTellerInformationSemantics.kt`（占卜师目标枚举/真值）和 `FirstNightNumericInformationDisplayDomain.kt`（厨师/共情者旧规则范围）：仅自身和各自测试引用，当前 App/AI/UI 均未调用。
  - `clocktower/config/TroubleBrewingRecommendationMetadata.kt`：旧角色评分表与 metadata 类型没有任何消费者；不是模型提示词或战略记忆。
  - `clocktower/session/FirstNightPoisonLifecycle.kt`：仅专属测试使用的另一份投毒状态/版本生命周期；保留真正 App/Session 的 poison draft/确认、canonical 事件和安全回归。`AuthenticatedModelTransportV1.kt`：没有任何消费者的重复 HTTPS/旧网关传输；真正 `PersonalDirectOpenAiV1`、首夜/后夜 API、认证和对应测试不动。
  - `clocktower/history/InformationReferenceExtractor.kt`：未使用的展示文本座位提取器；实际结构化历史/收件人投影不动。`ClocktowerSetupRecommendationSemantics.kt`：仅专属测试使用的旧 mutable setup lock 合并函数。
  - `clocktower/domain/RegistrationOutcome.kt`、`clocktower/recommendation/dynamic/PairInformationPresentationModels.kt`：只剩定义，无生产或测试消费者的旧登记 DTO/enum。
  - `GameSettingsUi.kt`：只含未调用的 StepperRow，不是当前设置页面。`clocktower/recommendation/PairInformationLegalDomain.kt`：无生产调用的兼容转发/typealias；实际 `clocktower/rules/PairInformationLegalDomain` 仍支撑后续 AI，保留原实现和有意义的测试，测试改为直接导入实际 owner。
- **整删 5 个专属测试（281 行）**：`clocktower/rules/FortuneTellerInformationSemanticsTest.kt`、`FirstNightNumericInformationDisplayDomainTest.kt`、`clocktower/session/FirstNightPoisonLifecycleTest.kt`、`clocktower/history/InformationReferenceExtractorTest.kt`、`ClocktowerCommittedSetupRecommendationLockTest.kt`。这些均只验证被退休实现；没有删除实际 Session 去重、poison commit、AI Provider/请求时效或隐私测试。
- **整删 3 条失效实验工作流（193 行）**：`.github/workflows/fn-bundle-3-calibration.yml`、`sde-2d5f-expert-observed-calibration.yml`、`sde-2d5f-b4f-silver-generalization.yml`。各自仅调用 F5 已退休的 Gradle 实验任务/测试；当前 app/build.gradle.kts 无这些 task，工作流不是复用 workflow，无正式 CI/生产依赖。保留完整 Android CI、R2、离线 API/网关检查和 field-test 构建。
- **明确保留/剩余**：厨师/共情者仍调用 ClocktowerSingleNumericChoiceRows，其他现用信息 UI 仍调用 SingleRecommendationButtons 和 compact helper，因此不因粗筛结果删除。Snapshot JSON codec、PlayerHistoricalTimeline 和其余 epistemic 核心继续保留。当前有真实消费者的大块（Session、NightTransaction、Setup/Flow、规则生成器、UI/后夜候选）不是本批整删对象，留待剩余审计和可验证解耦；不宣称 P1 完成。
- 代码/配置总净删 **917 行**（生产 -444、测试 +1/-281、实验 CI -193），没有新算法/新兼容层/新状态。实际 Git tree 998→979，Android 生产 295→284、测试 236→231。三个超限源码仍为 App 188197B、HostScreen 149006B、NightStepUi 60237B。唯一事实/事件/安全和活跃 AI 未修改；无付费 API/真机测试，无新增所有者决策；本地 `:app:testFull :app:assembleDebug` SUCCESS（980 项、0 失败/错误/跳过）；代码 `6320a735a9ee13a7c57dcd6a259361d40de591c3` 的完整 Android **CI #4359 SUCCESS**（单测/Debug APK）、**R2 #3939 SUCCESS**，ASP/真实 Clingo/CI gate 均成功。GitHub 独立 diff 核对：24 文件 +33/-920（含文档）；源码净减 724 行、另删 CI 193 行，合计代码/配置净删 917 行。该 SHA 是本批代码验收基线；后续 docs-only HEAD 不是新的编译证明。PR 保持堆叠 Draft、不合并。

## F6：首夜候选合法域整链删除（本地完整构建及独立 CI/R2 已通过）

- 本批按所有者最新指示在 Windows 本地直接读写；起点为远端/本地 `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，main `61cd6796`，PR #334 仍堆叠 Draft、base `64be3090`。读取完整 1,005 文件 Git tree 并复核当前模块分类；未声称全部文件已完成语义审计。旧 web/world 引擎不重建。
- 删除生产 `HostOpeningRulesDomainV2.kt`、`HostOpeningSemanticGateV2.kt`、`InitialGlobalOpeningScopeV1.kt`：退出「每个酒鬼假设构造 GameState → Pair/Numeric/Setup 合法候选 → 语义匹配候选 ID → V1 选择复核 → App 反解推荐」整链。真实 AI 网络调用不再生成这些合法域；首夜提示词去掉 Host 后验规则校验承诺，要求模型提供具体观察值而非依赖 Host 计算唯一答案。
- 断开最后生产消费者后，删除 `recommendation/FirstNightNumericLegalDomain.kt` 和 `recommendation/setup/SetupCandidateGenerator.kt`。删除四个专属测试文件：`HostOpeningSemanticGateV2Test.kt`、`InitialGlobalOpeningScopeV1Test.kt`、`FirstNightNumericLegalDomainTest.kt`、`CandidateGeneratorTest.kt`（该文件只含 SetupCandidateGeneratorTest）。删除前已检索所有源码、工具和 CI 消费者；PairInformationLegalDomain 仍有真实后续 AI 消费者，未误删。
- `InitialOpeningRequestV2` 只承载已有 precommit snapshot、请求身份/版本、经验和信息/待输入事件标识；`OpeningRecommendationV2` 保留实际 AI 结构化建议、待决意图和战略记忆 DTO。没有候选枚举、第二份 GameState、规则 verdict、登记见证或 Session writer。App 直接保存模型的 seat/value/role 信息、Red Herring 座位和 bluffs；移除候选 ID 中转。保留 serial、pending object、模式/页面边界、response receipt、记忆续规划和逐步事实确认。
- 保留/补充有价值的原生产测试：不发送候选、真实已存 P15 模型响应解析（离线）、战略续规划；验证具体数值/角色不再按旧合法域拒绝，并验证跨游戏/版本/roster、信息清单遗漏/重复、收件来源身份、未知角色/座位、待决行动身份、整数/DTO 结构及战略记忆上限。第一轮本地 testFast、testFull + assembleDebug 均通过（993 项、0 失败、0 跳过）；最终代码变动后的复验与 GitHub CI/R2 见下方验收补记。
- **明确剩余绑定**：`TroubleBrewingDrunkSelectionRouter` / setup commit、HostScreen/NightStepUi 的既有菜单匹配/数值/目标/掩饰身份确认，以及后夜 `StorytellerStrategicContinuationV2` 对候选的复核仍需继续解耦。本批只删除首夜响应规则域；尚不能声称所有 AI/UI 和 Host 事实提交均已无规则门禁。新解析器接受、旧酒鬼选择器尚不能应用的建议会显示重试/手动接管，避免 UI 抛异常；后续应从结构化 setup 确认端清除该限制。
- Host 状态/事件风险：未修改唯一 Session、ActionFact、ObservationLog、收件权限、事件顺序和原子提交；建议、条件意图、战略记忆都不是已发生事实。未执行真机或新付费 API 调用，无新增所有者决策，PR 不合并/不 retarget。

- **本批代码规模与最终本地复验**：含两个保留 AI 合同新文件，生产 +356/-1268，测试 +100/-784，合计净删 **1596 行代码**。删除 5 个生产 + 4 个测试、增加 2 个窄职责 AI 合同文件，实际受控树 1,005→998、Android 生产 298→295、测试 240→236。最终本地 `:app:testFull :app:assembleDebug` SUCCESS（993 项，0 失败/错误/跳过）；APK 为 `app/build/outputs/apk/debug/app-debug.apk`。当前超 50,000B 源码：App 188197B、HostScreen 149006B、NightStepUi 60237B（按 UTF-8/LF 源码实测），其余受控源码无新增超限。代码提交 `f3fae3f2e5bfde9083c919f9a5ae34f4e10fb90c` 的 GitHub 完整 Android **CI #4357 SUCCESS**、**R2 #3937 SUCCESS**，ASP、真实 Clingo 和 CI gate 全部成功。该 SHA 是本批代码验收基线；后续 docs-only HEAD 不等于新的 Android 构建证据。

## F5：大型整块删除已完成（Android CI/R2 全通过）

**顺序更新（所有者 2026-10-18）**：先整删旧 `web/` 客户端，再横向删除 `epistemic` 旧世界推演系统及专属实验，**最后**处理仍依赖 Host 候选 ID 的旧首夜合法域引擎；不要把小函数手术当主进度。

- `web/` 旧独立网页客户端 8 个受控文件整体移除（其中 PNG 由 GitHub 内容 API 单独提交 `5be8d37bad28e6055b89611de7be22388a7fffd1`，7 个文本文件由 Mini MCP 提交 `bb96b0c525b1550875d5e0ae22e9c68e62b0e308`）。Android、CI、工具源码无任何引用；这个网页不是未来新设计的独立 WebHost。
- `epistemic/` 不能整目录删：原 35 个生产 Kotlin 中，**26 个**旧 A3/A4/B4 枚举、历史世界重放、Exact、Topology、玩家知识计算、登记见证推演、Spy 旧投影等已整文件删除；保留 9 个核心文件：`ActionFactDraft`、`ActionFactTimeline`、`EpistemicObservationDraft`、`EpistemicObservationLog`、`EpistemicSemanticJson`、`EpistemicSemanticModel`、`EpistemicContracts`、`GrimoireReminderToken`、`PlayerHistoricalTimeline`。原因：App、唯一 Session、历史事实、可见/私密信息、结构化 JSON、两个公共历史隐私测试仍引用其类型；之后可按统一合同重命名/迁目录，但不需为删目录制造新工程。
- `epistemic` 原 60 测试 Kotlin 删除 **48 个**世界推演专属测试；保留事件时间线、私密观察、JSON 兼容、魔典令牌和历史投影相关 12 个测试及测试 fixture。整删独立 `clocktower/review/` 的 17 个旧专家世界后验实验 Kotlin 测试、其 3 份离线 manifest，及仅这些实验/测试使用的两份 `recommendation/` 源文件与三份专属测试。
- `app/build.gradle.kts` 移除已退役 SDE-2D4/SDE-2D5F 规模/费用实验的 Gradle 路由及排除规则，保留原 `:app:testFast`、`:app:testFull` 正式验证任务。
- **AI 保证**：`InitialJointOpeningDirectV2`、Provider/OpenAI API、模型提示词、战略记忆、后夜增量规划、AI_ASSISTED/AI_AUTOMATIC、结构化结果与自动/辅助选择均未触及。Host 保存实际 ActionFact/ObservationLog 的路径和安全/身份/时间戳校验保留。原 `epistemic` 中保留的 `EpistemicContracts` 仍含旧登记语义，下一步 Host candidate cleanup 才核查删除。
- 当前仍有未审计的旧 `HostOpeningRulesDomainV2` → `HostOpeningSemanticGateV2` → `InitialGlobalOpeningScopeV1` 候选 ID 采纳链，列为**下一大块 REMOVE_AFTER_DISCONNECT**。不可把“当前生产调用”写成最终 AI 策略需 Host 候选；保留 AI 请求/返回及 Session 安全，改为直接结构化字段后删除。
- **F5 实际验收**：旧 Web PNG 删除提交 `5be8d37bad28e6055b89611de7be22388a7fffd1`，其余 7 文件删除 `bb96b0c525b1550875d5e0ae22e9c68e62b0e308`；世界引擎/测试整删及 Gradle 提交 `3be99bbc40a099a18c8f1bfc13b1a5d7ed978bc7`，该代码提交 **103 文件 +27/-17731，净删 17,704 行**。完整 Android **CI #4355 SUCCESS**（单测、Debug APK），**R2 #3935 SUCCESS**，ASP/Clingo/CI gate SUCCESS。最新完整非截断 Git tree **1005 个文件**（此前 1112），Android 生产 Kotlin **298 个**、测试 Kotlin **240 个**；epistemic 生产 9/test 12，web 文件 0。超 50,000 字节源码 3 件：App 188473B、HostScreen 149006B、NightStepUi 60237B；旧 web/app.js 已删。无真实收费 API/真机测试，PR #334 仍 Draft。



## 2026-10-18 全树重启审计（替代沿调用路径逐段清理；最新当前方向）

- 全量 Git tree 快照 SHA `8c1a4dc114060806c33d5f78102b6b3c6457cf71`：未截断、**1135 个文件**（树条目除目录）、按扩展名初筛 **723 个代码/配置类文件**（含 Kotlin/DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML、JSON 等）；Android Kotlin **337 生产 + 320 测试**。不同扩展名口径下的源代码数量不能与上方历史的 717 或窄口径 703 直接比作增删。
- 审计必须**横向遍历全部真实代码目录及构建/工具资产**：Host UI 与根组件、Session、domain、epistemic、rules、recommendation、setup、flow、catalog、persistence、debug、test、Web、工具/CI；每个文件先按职责分类，而不是沿某个旧规则调用栈寻找下一步。
- **决策标签**：`KEEP_CORE` 唯一事实、事件、安全、隐私、必要输入/展示；`KEEP_AI` 实际 AI 规划、建议、提示词、Provider、响应/战略与必须的规则候选依赖；`REMOVE_CONFIRMED` 仅自身或专属测试使用的旧实验/适配和过度 Host 规则门禁；`REMOVE_AFTER_DISCONNECT` 已判断应删但存在实际消费者，整条拆除后删；`REVIEW_SCOPE` 需要产品边界决策；`AUDIT_PENDING` 只有路径/体积证据，尚不能声称消费者审计完成。不得以目录名推定去留，也不得保留原代码仅因为旧测试要求。
- 全仓源码索引 `MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv` 是**旧基线库存**，不是另一套可写状态；新增审计结果统一记入本文件。任何新 HEAD 对照均重新拉完整 Git 树，不以静态清单判定实际删除完成。

| 目录/链路 | 当前判定 | 可核实依据与操作 |
|---|---|---|
| domain / ClocktowerGameSession / canonical ActionFact / UI 事实提交 | KEEP_CORE + AUDIT_PENDING（混合文件） | 保留唯一状态、已确认事实、安全、私密/顺序/去重/过期防护；不按旧 Rules 正确答案驳回已发生信息 |
| 首夜 InitialJointOpeningDirectV2、Provider、战略续规划与 UI 预选 | KEEP_AI | `encode` 已不把候选列表交给模型。保留真实模型请求、响应合同、上下文/记忆和 UI | 
| 首夜旧合法域 / 语义匹配 / V1 scope | F6 REMOVED | 已断开首夜 Provider/App 消费并整删；改为结构化 AI 建议和请求身份，UI/setup 与后夜残留仍另行审计 |
| FixedInformationEvaluator、FirstNightNumericInformationSemantics、TroubleBrewingRegistrationDomain | REMOVE_AFTER_DISCONNECT（混合） | 当前仍被 Host/UI 候选与 AI 辅助旧路径引用，**不代表 AI 需要 Host 给出候选**；优先断开合法域复核及候选消费，不牺牲真实 AI 功能。结构/必要手动操作可独立保留 |
| A4 identity reveal prewarm / A4 runtime/ZDD rollout | F3 + F4 REMOVE_CONFIRMED | Debug-only 缓存已在 F3 删除；F4 确认 A4 world runtime、rollout policy、ZDD 原型无生产消费者，仅测试自持，连专属测试和双轨契约整删 |
| B4DynamicPlayerWorldSetShadow / ExactHistorical 和 Topology bundle 旧实验 | F2 + F5 REMOVE_CONFIRMED | B4 查询接口在 F2 已删；独立 Exact/Topology/枚举世界实验及专属测试在 F5 一并删除；事件/观察核心类型未删 |
| FN-BUNDLE PublicGoodInfoProjection 等压力实验 | REMOVE_AFTER_DISCONNECT 待核 | 旧实验/测试消费者存在；与正式联合规划 API 不是同一合同；先完整检查引用，再批量删除 |
| ClocktowerHostSelectionSemantics / HostScreen / NightStepUi | AUDIT_PENDING 混合 | 夜间人工候选过滤和规则否决需要删，但显示、事实与 AI 自动/辅助流程要保留 |
| legacy web/ 独立网页客户端及多剧本 | F5 REMOVE_CONFIRMED | 产品所有者批准整删：Git tree 中 web/ 已零文件；不是新 WebHost |
| tools、CI、assets、测试 | AUDIT_PENDING | 以当前 CI/R2/运行时消费者实际使用为依据，专属废弃实验测试可与生产实现一起删除 |

### F4：A4/ZDD 原型整链退役；AI 无 Host 合法候选原则（CI/R2 已完成）

- **最终设计**：AI 不从 Host 获取候选表，只接收真实 GameState 和已确认事件，自行规划。源码核实：`InitialJointOpeningDirectV2.encode` 没有输出候选列表，但 `decode` 仍走 `HostOpeningSemanticGateV2.accept(raw, domain.forDrunkSeat(drunk))`，由 `HostOpeningRulesDomainV2` 的规则候选域重验模型方案。这部分标记 `REMOVE_AFTER_DISCONNECT`，不是长期 `KEEP_AI`；安全合同的 identity/版本/座位/隐私与 JSON shape、strategic memory 必须保留。
- **真实删除**：`A4PlayerWorldSetRuntime.kt`、`A4WorldEngineRuntimePolicy.kt`、`ZddPlayerWorldSet.kt` 及三份同名专属测试整删；`KnowledgeSafeWorldInputTest` 保留结构输入/隐藏真相验证，退役仅对 A4 Runtime 的测试；`KnowledgeTimelineSemanticsTest` 保留共享时间线检查，退役孤立 ZDD 双轨；`A3GoldenContractCatalogTest` 金标准改用已存在的枚举引擎，不再生成第二份 ZDD 结果；Gradle 移除过期 `testFast` 的 ZDD 排除。
- 实测删除后 `app/src` 不再含 `A4PlayerWorldSetRuntime` / `A4WorldEngineRuntimePolicy` / `ZddPlayerWorldSet` / `ZddFilterStrategy` / `useZdd` 调用。本批不动 Session、ActionFact、模型 API、AI 请求、提示词、战略记忆或 UI。**代码 SHA `52ef98ee4c406fc49af21e1592a954da7e4246ff`，15 文件 +27/-1309，净删 1282 行；代码净删 1296 行（生产 740 行、旧测试及混合测试 555 行、Gradle 1 行）。完整 Android CI #4351 SUCCESS，R2 #3931 SUCCESS，ASP/Clingo/CI gate 成功。** 当前未截断树 1112 文件、宽口径代码/配置 700、Android 326 生产 Kotlin/308 测试 Kotlin。超 50000B 四源码仍是 App 188473B、HostScreen 149006B、NightStepUi 60237B、web/app.js 143503B。未进行真实付费 API/真机验收。
### 全树横向删除批次 F3（已通过完整 GitHub CI/R2）

- 跨目录整体删除 `CampBoardGameHostApp.kt` 中**仅适用于 Debug 5 人局**的 A4 identity-reveal prewarm、观察缓存重建、worker 和框架耗时诊断，以及伴随每个 Session 写入/修订触发的旧 A4 cache-invalidations 和临时记录 ID，不改动原 Session 状态写入、公开/私密 EpistemicObservationLog、结构化事件或真实首夜 AI 预计算。
- 完整删除 5 个已无生产消费者的 A4 影子缓存源码：`A4IdentityRevealPrewarmCoordinator.kt`、`A4ObservationCacheRebuildExecutor.kt`、`A4ObservationCacheUpdateCoordinator.kt`、`A4ShadowWorldSetCache.kt`、`A4ShadowLifecycleInvalidator.kt`，及它们 5 个独占单测/实验验证类（含 `A4ShadowProductionIsolationTest.kt`，已无对象可测）。
- **保留** `A4PlayerKnowledgeFactory`、`A4PlayerWorldSetRuntime`、`A4WorldEngineRuntimePolicy`、`ZddPlayerWorldSet` 等被其他精确推演测试使用的类型，本批没有整包误删；下一次仍需判定这些类有无真实 AI 消费者，不能以旧测试为长期保留理由。
- 对 App 的所有 A4 调用和源文件互引用进行实际符号检索，删除后无 A4 预热/缓存/失效器残余；生产 AI Provider、整局首夜与后夜推荐、战略记忆、预选和模式路径保持原样。**代码 SHA `6ea36666ceb0b2d3d7a919a25c2e0b04841f7dce`，14 文件、生产源码 -977 行（App -187，5 服务 -790）、专属测试 -735 行，净删 1712 行代码**；文档 +22/-6，全提交 +22/-1718，净删 1696 行。完整 Android **CI #4349 SUCCESS**，R2 **#3929 SUCCESS**，ASP/Clingo/CI gate 全部成功。最新 Git tree 1118 文件、宽口径代码/配置 706、Android 329 主 Kotlin + 311 测试 Kotlin；源码超 50000B 仍为 App 188473B、HostScreen 149006B、NightStepUi 60237B、web/app.js 143503B。无付费 API 或真机验收。

### 全树横向删除批次 F2（已通过 CI/R2）

- 生产 `epistemic/B4DynamicPlayerWorldSetShadow.kt`（B4ShadowRequest、B4ShadowCandidate、B4ShadowReport、`b4FormalGameState`、`b4ReplayObservationsInTimelineOrder`）在 `app/src/main` 无任何外部使用：B4 旧影子查询适配只给专属实验提供接口，**不是 App 首夜/后夜 AI 推荐、Host 状态或隐私写入依赖**。
- 整删该生产文件以及独占的 `B4DynamicPlayerWorldSetShadowTest.kt`、`B4HistoricalExactShadowBridgeTest.kt`；`CrossTypeTimelineCompatibilityTest` 改为直接测试原始 `ActionFactTimeline.requireCompatibleWith(observationLog)` 和防外部列表篡改，`ExactHistoricalHypotheticalObservationBundleEvaluatorTest` 保留真实 Exact 评估稳定性/不变更记录断言，删 B4 对照包装要求。
- `A4IdentityRevealPrewarmCoordinator` / `A4ObservationCacheRebuildExecutor` **仍由 App 生产引用**，故仍标记 `REMOVE_AFTER_DISCONNECT` 而不误删；`ExactHistoricalHypotheticalObservationBundleEvaluator` 和 Topology 分支当前仅旧测试消费者，但仍在关联实验系统中，待整组独立审计，未动。
- F2 代码提交 `bdf5dbcdccdb9ad7024580a2283ad4d704f496a6`；完整 Android CI **#4348 SUCCESS**、R2 **#3928 SUCCESS**（ASP/Clingo/CI gate 均成功）。整文件删除生产 180 行与专属测试 391 行，混合测试另净删 26 行；整个 F2 无 Canonical GameState、ActionFact、AI Provider、提示词、战略记忆、OpenAI 传输或 Android UI 生产改动。

### 全树横向删除批次 F1（源码修改与 CI 以提交为准）

- **整删 `clocktower/recommendation/DecisionSeedFactory.kt` 及 `DecisionSeedFactoryTest.kt`**：旧 `DecisionSeedFactory`/`DecisionSeedMaterial` 在全部生产 Kotlin 中只有自身文件，无实际 AI 推荐调用，专属测试只测旧决定种子和旧回放假设。
- **整删 `clocktower/recommendation/TroubleBrewingSetupRecommendationDecisionContext.kt` 及 `TroubleBrewingSetupRecommendationDecisionContextTest.kt`**：旧 Builder/上下文在完整仓库的源码检索中只有自身和专属测试引用，当前真正首夜联合规划依赖的是 `HostOpeningRulesDomainV2` 等，而不是这个旧快照转换器。
- 两条完整孤立生产/测试链均与 Host 核心事实、AI Provider、生产提示词和战略记忆无生产调用关系。此批是**按全树角色筛选而非沿 B4 规则调用栈的删除**。已提交 `24995d37041bdf24a5bd6ad8da50a51ad693c8b4`；4 个文件全删 478 行（生产 210 + 专属测试 268），包含文档共 8 文件 +44/-481，净删 437 行。GitHub **完整 Android CI #4346 SUCCESS**、**R2 #3926 SUCCESS**，ASP/Clingo/CI gate 成功。新 Git 树文件 1131、宽口径代码/配置 719，Android 335 主 Kotlin + 318 测试 Kotlin；4 个超 50,000B 实际源文件仍为 App/HostScreen/NightStepUi/web.app.js。未完成的遍历项目保留 `AUDIT_PENDING`，不宣称 337 个起始生产 Kotlin 已逐文件完成语义审计。

**审计基线：** 64be30905b88f639a464204ed1db89b7b62bc910（PR #333 当前 HEAD）。本文件属于当前开发路线的源码库存与分批清理台账，不把“列出文件”混同“已完成逐项语义审计”。

## 两条硬约束

1. 产品只保留继续真实游戏的**最小当前状态 + 一条按实际发生顺序的权威关键事件记录 + 必要的人机操作**。废弃 Host 的规则求值、合法候选枚举、登记推断和历史世界搜索；保留身份/隐私、单次原子确认、防重复、过期 AI 隔离等数据安全防护。
2. **每个源文件严格小于或等于 50,000 字节**（十进制 KB；同时满足 50 KiB）。优先净删无效职责，再拆分仍需要的组件；不可为达到行数而创建委托壳或重复事实对象。

**分阶段交互：** 当前清理期人工信息输入先不加任何游戏规则合法性限制，可选择数值、真假、角色或两座位等结构上表达得出的内容；清理完成后才增加 pair 的座位/角色动态选项、厨师开局状态上限、共情者每次状态相关候选按钮。第二阶段是 UI 建议，不可复活多份事实对象或旧规则引擎。

## 全树元数据检查

- GitHub tree 为完整遍历：文件 1150；匹配源代码扩展名 698；Android 生产 Kotlin 348；Android 测试 Kotlin 327。其余源代码位于 web、tools、ui、artifacts 等目录。
- 基线 >50,000 字节源文件 4 个：
  - `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt` — **198,706 B**
  - `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt` — **150,032 B**
  - `web/app.js` — **143,503 B**
  - `app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt` — **60,272 B**

## 模块级决定

| 模块 | 生产 Kotlin 数量 | 判断 | 处理说明 |
|---|---:|---|---|
| Android 根组件、方桌和夜间交互 | 122 | 逐文件审计，生产消费者迁移后判定 | 分离最小导航/确认/展示与规则候选；删除多余的夜间手动规则助手，保留最小操作 |
| Session / AI | 46 | 逐文件审计，生产消费者迁移后判定 | 保留单一状态和提交边界、过期请求防护、结构化事实；消除候选语义协议及冗余策略控制 |
| 状态 / 实际角色 / 事件 | 26 | 逐文件审计，生产消费者迁移后判定 | 保留 GameState 与 ActionFact；其他类型按真实消费者删减 |
| 观察记录 / 历史推理 | 45 | 逐文件审计，生产消费者迁移后判定 | 保留观察日志、时间线、序列化；A4/B4/ZDD/世界枚举推理链为重点删除候选，必须先核查生产使用 |
| 规则 / 线索计算 | 23 | 逐文件审计，生产消费者迁移后判定 | 拟删除游戏规则判断、合法候选、自动真值推导；先隔离残留生产消费者 |
| 推荐 / 合法域 | 15 | 逐文件审计，生产消费者迁移后判定 | 迁移最后生产消费者后删除 Host 候选域与旧策略评分；保留 AI 传输必须的最小字段 |
| 发牌与初始配置 | 22 | 逐文件审计，生产消费者迁移后判定 | 保留身份发放与实际初始事实记录，删除额外合法性/评分/候选附属路径 |
| 流程顺序 | 10 | 逐文件审计，生产消费者迁移后判定 | 保留阶段/行动操作所需最小路由，拆除规则推导前置步骤 |
| 方桌 UI | 18 | 逐文件审计，生产消费者迁移后判定 | 确认与展示职责保留，其余按迁移后净删，ClocktowerHostScreen 为超限关键 |
| 脚本与官方素材 | 8 | 逐文件审计，生产消费者迁移后判定 | 角色名字及展示元数据按 UI 实际需要保留；规则校验型 Catalog 删除 |
| 其他 Clocktower 支持 | 0 | 逐文件审计，生产消费者迁移后判定 | 核对实际 Session 使用，避免重复快照 |
| 偏好/凭据持久化 | 5 | 逐文件审计，生产消费者迁移后判定 | 仅保留必要配置及安全凭据，不复活独立游戏存档 |
| 调试记录 | 2 | 逐文件审计，生产消费者迁移后判定 | 仅保留用户明确需要的诊断能力，否则列入后续删除 |
| 旧模拟模块 | 1 | 确认删除 | 无生产使用的模拟报表整模块删除 |

### 本轮可立即删除（已验证源代码消费者）

- `app/src/main/java/com/codex/campboardgamehost/clocktower/simulation/DistributionReport.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/A4DeviceBenchmarkHarness.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerB4HistoricalShadowCoordinator.kt` — 无生产调用/仅独立旧基准或影子对照职责
- `app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/A4DeviceBenchmarkHarnessTest.kt` — 独立旧测试，仅测试被删除功能
- `app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerB4HistoricalShadowCoordinatorTest.kt` — 独立旧测试，仅测试被删除功能

### 需所有者审核的范围（不可先假设删除）

- **`web/` 独立离线网页产品**：`web/app.js` 已达 143,503 B，无法仅通过 Android 运行确认它可直接删。需决定整套 web 是否仍属于本仓产品；若保留则须模块拆分到 <=50,000 B/源文件。
- **A4/B4 活跃影子推理链**：`CampBoardGameHostApp.kt` 存在 A4 identity-reveal prewarm 调用，存在与历史推理联动的消费者；按当前最小合同高度疑似不必要，但必须完成状态、隐私、并发消耗读写审计后整链删除。
- **两个剧本及旧推荐路径**：实际角色/展示元数据仍在调用规则/候选组件；必须先迁移操作路径再删，不能只按目录名删除。
- **调试回放、旧实验/脚本及工具目录**：需要识别 CI/R2 的真实调用再决定删除，不删除仍用于独立验收的工具。

## 源文件逐项库存（当前阶段是体积及路径扫描，非全文件行为审计）


### Android 根组件、方桌和夜间交互 (122)

- `AppClocktowerLandingScreen.kt` — 6026 B
- `AppDealScreens.kt` — 13556 B
- `AppGameModels.kt` — 464 B
- `AppGameReviewScreens.kt` — 12093 B
- `AppSettingsScreen.kt` — 10085 B
- `BotcAiTrace.kt` — 3982 B
- `CampBoardGameHostApp.kt` — 198706 B；超过 50 KB：必须净删/拆分
- `CampBoardGameHostApplication.kt` — 367 B
- `ClocktowerAppModels.kt` — 1007 B
- `ClocktowerArtistTableState.kt` — 2237 B
- `ClocktowerAutomaticRulings.kt` — 605 B
- `ClocktowerBeginnerCompactNightUi.kt` — 7186 B
- `ClocktowerBeginnerNightGuidance.kt` — 4873 B
- `ClocktowerBooleanDisplayOptionSelection.kt` — 934 B
- `ClocktowerChambermaidPresentationSemantics.kt` — 2273 B
- `ClocktowerChambermaidSquareTableUi.kt` — 8577 B
- `ClocktowerChambermaidStepMaterializer.kt` — 1981 B
- `ClocktowerChefSquareTableUi.kt` — 13911 B
- `ClocktowerClockmakerSquareTableUi.kt` — 10904 B
- `ClocktowerContrastPolicy.kt` — 1565 B
- `ClocktowerCurrentDemonAuthority.kt` — 2751 B
- `ClocktowerCurrentResidualScopeV2.kt` — 7658 B
- `ClocktowerDayNominationGesture.kt` — 2043 B
- `ClocktowerDayOverviewTableState.kt` — 1280 B
- `ClocktowerDemonAttackProductionAdapter.kt` — 6684 B
- `ClocktowerDemonBluffPresentation.kt` — 2251 B
- `ClocktowerDirectBooleanResult.kt` — 1495 B
- `ClocktowerDirectNumericResultUi.kt` — 5204 B
- `ClocktowerDynamicNightAdvance.kt` — 2118 B
- `ClocktowerEmpathSquareTableUi.kt` — 14281 B
- `ClocktowerEvilInfoSquareTableUi.kt` — 8274 B
- `ClocktowerFirstNightEvilInformationPresentation.kt` — 8559 B
- `ClocktowerFirstNightInformationRequest.kt` — 8620 B
- `ClocktowerFortuneTellerSquareTableUi.kt` — 8796 B
- `ClocktowerGhostVoteAuthority.kt` — 913 B
- `ClocktowerGhostVotePresentation.kt` — 304 B
- `ClocktowerGlobalFirstNightDecision.kt` — 2783 B
- `ClocktowerGlobalFirstNightScalarDecision.kt` — 11002 B
- `ClocktowerHostBottomInsetPolicy.kt` — 1233 B
- `ClocktowerHostCoreSemantics.kt` — 8233 B
- `ClocktowerHostFullScreenScaffold.kt` — 2726 B
- `ClocktowerHostPresentationModels.kt` — 3677 B
- `ClocktowerHostSeatContentPresentation.kt` — 2600 B
- `ClocktowerHostSelectionSemantics.kt` — 19123 B
- `ClocktowerHostSquareTableScaffold.kt` — 2788 B
- `ClocktowerHostTableContract.kt` — 5651 B
- `ClocktowerHostTableGameStateProjection.kt` — 1916 B
- `ClocktowerHostTableLayout.kt` — 12909 B
- `ClocktowerHostTableReorder.kt` — 2043 B
- `ClocktowerHostTableSurfaceLayout.kt` — 3596 B
- `ClocktowerHostTableUi.kt` — 7643 B
- `ClocktowerInformationStepBuilder.kt` — 9871 B
- `ClocktowerJointOpeningDisplayMatcherV2.kt` — 5261 B
- `ClocktowerJointOpeningReviewScreen.kt` — 4369 B
- `ClocktowerKlutzTableState.kt` — 2629 B
- `ClocktowerNeutralInformationPreparation.kt` — 12286 B
- `ClocktowerNightActionSquareTableUi.kt` — 10146 B
- `ClocktowerNightFullScreenOwnership.kt` — 3756 B
- `ClocktowerNightHostProjection.kt` — 18412 B
- `ClocktowerNightPresentationRole.kt` — 710 B
- `ClocktowerNightStepMaterializerRegistry.kt` — 3241 B
- `ClocktowerNightStepUi.kt` — 60272 B；超过 50 KB：必须净删/拆分
- `ClocktowerNightTargetLegality.kt` — 305 B
- `ClocktowerNumericInformationOptionPreparation.kt` — 1317 B
- `ClocktowerPairInformationSquareTableUi.kt` — 15082 B
- `ClocktowerPairManualAuthority.kt` — 11611 B
- `ClocktowerPairManualSelectionModel.kt` — 5862 B
- `ClocktowerPairManualSelectionUi.kt` — 1683 B
- `ClocktowerPairPlayerRevealPresentation.kt` — 3074 B
- `ClocktowerPhaseStorytellerAdapter.kt` — 407 B
- `ClocktowerPlainInformationSquareTableUi.kt` — 6219 B
- `ClocktowerPlayerDisplayResolution.kt` — 5007 B
- `ClocktowerPlayerDisplayUi.kt` — 15450 B
- `ClocktowerPlayerRevealHandoff.kt` — 2463 B
- `ClocktowerPresentationRoleCatalog.kt` — 7722 B
- `ClocktowerPresentationTheme.kt` — 3168 B
- `ClocktowerRavenkeeperSquareTableUi.kt` — 13933 B
- `ClocktowerRecommendationRules.kt` — 875 B
- `ClocktowerRecurringInformationStepMaterializers.kt` — 5233 B
- `ClocktowerRegistrationDecisionControls.kt` — 7221 B
- `ClocktowerRegistrationInteractionState.kt` — 4023 B
- `ClocktowerRegistrationOptionPreparation.kt` — 5574 B
- `ClocktowerRegistrationProjection.kt` — 5990 B
- `ClocktowerRegistrationResultDomain.kt` — 3809 B
- `ClocktowerRegistrationResultPresentation.kt` — 4927 B
- `ClocktowerResidualDisplayMatcherV2.kt` — 14862 B
- `ClocktowerResidualRequestIdentityV2.kt` — 1054 B
- `ClocktowerRoleLocalization.kt` — 583 B
- `ClocktowerRoleRevealChoiceProjection.kt` — 2263 B
- `ClocktowerRuleOnlyCompactChoice.kt` — 352 B
- `ClocktowerSageSquareTableUi.kt` — 11900 B
- `ClocktowerSageStepMaterializer.kt` — 5652 B
- `ClocktowerSeatNumberBadge.kt` — 2796 B
- `ClocktowerSeatNumberLabel.kt` — 390 B
- `ClocktowerSessionPlayerCards.kt` — 2544 B
- `ClocktowerSetupRecommendationSemantics.kt` — 705 B
- `ClocktowerSingleNumericChoiceUi.kt` — 4197 B
- `ClocktowerSingleRecommendationChoiceUi.kt` — 2362 B
- `ClocktowerSingleTargetInteractionPresentation.kt` — 3915 B
- `ClocktowerSingleTargetInteractionUi.kt` — 6760 B
- `ClocktowerSlayerTableState.kt` — 3543 B
- `ClocktowerSquareTableLifeMarkers.kt` — 434 B
- `ClocktowerSquareTableSeatDensityPolicy.kt` — 3360 B
- `ClocktowerSquareTableUi.kt` — 35812 B
- `ClocktowerStorytellerRecommendationUi.kt` — 7356 B
- `ClocktowerStructuredInformationPreparation.kt` — 9138 B
- `ClocktowerTableVoteState.kt` — 3141 B
- `ClocktowerUndertakerSquareTableUi.kt` — 13044 B
- `ClocktowerVoteHistoryRecord.kt` — 1024 B
- `CommonPlayersDefaultPolicy.kt` — 415 B
- `GameSettingsUi.kt` — 2269 B
- `HostBottomActionBar.kt` — 3802 B
- `HostInteractionUi.kt` — 844 B
- `HostSeatingRoster.kt` — 2101 B
- `HostSeatingSetupFlow.kt` — 742 B
- `MainActivity.kt` — 1103 B
- `PlayerExperiencePicker.kt` — 1921 B
- `SeatingFirstSetupUi.kt` — 10527 B
- `StorytellerAiSettingsPanel.kt` — 8104 B
- `StorytellerOperationMode.kt` — 2026 B
- `StructuredBooleanInformationAdapter.kt` — 5523 B
- `StructuredNumericInformationAdapter.kt` — 5336 B

### 其他源代码 (5)

- `clocktower/config/TroubleBrewingRecommendationMetadata.kt` — 2178 B
- `clocktower/history/HistoricalClueSignature.kt` — 915 B
- `clocktower/history/InformationReferenceExtractor.kt` — 645 B
- `clocktower/persistence/ClocktowerRulesetPersistence.kt` — 1848 B
- `clocktower/TroubleBrewingGameSnapshotJsonCodec.kt` — 7985 B

### 脚本与官方素材 (8)

- `clocktower/catalog/BuiltInClocktowerRulesetCatalog.kt` — 3528 B
- `clocktower/catalog/ClocktowerCatalogModels.kt` — 6756 B
- `clocktower/catalog/ClocktowerScriptJsonParser.kt` — 21423 B
- `clocktower/catalog/ClocktowerScriptNormalizer.kt` — 13636 B
- `clocktower/catalog/ClocktowerScriptValidation.kt` — 1161 B
- `clocktower/catalog/LegacyRulesetCatalogAdapter.kt` — 2594 B
- `clocktower/catalog/NoGreaterJoyOfficialCharacterMetadata.kt` — 3502 B
- `clocktower/catalog/OfficialReminderTokenMetadata.kt` — 3220 B

### 状态 / 实际角色 / 事件 (26)

- `clocktower/domain/ActionFact.kt` — 11069 B
- `clocktower/domain/ClocktowerGameStateAdapter.kt` — 1817 B
- `clocktower/domain/ClocktowerSemanticHistoryMode.kt` — 2302 B
- `clocktower/domain/CommittedClocktowerSetup.kt` — 2462 B
- `clocktower/domain/ConstraintAuthority.kt` — 212 B
- `clocktower/domain/DecisionCandidate.kt` — 1653 B
- `clocktower/domain/DecisionOutcomeSnapshot.kt` — 597 B
- `clocktower/domain/DecisionTypes.kt` — 1489 B
- `clocktower/domain/DynamicDecisionModels.kt` — 134 B
- `clocktower/domain/DynamicInformationOutcome.kt` — 349 B
- `clocktower/domain/EffectDraft.kt` — 3038 B
- `clocktower/domain/GameSnapshot.kt` — 2000 B
- `clocktower/domain/GameState.kt` — 1305 B
- `clocktower/domain/InformationModels.kt` — 1003 B
- `clocktower/domain/MurmurHash3.kt` — 3775 B
- `clocktower/domain/PairInformationOutcome.kt` — 869 B
- `clocktower/domain/RecommendationModels.kt` — 1105 B
- `clocktower/domain/RegistrationOutcome.kt` — 353 B
- `clocktower/domain/RoleCatalogAdapter.kt` — 1337 B
- `clocktower/domain/RulesetRef.kt` — 756 B
- `clocktower/domain/SetupClueOutcome.kt` — 451 B
- `clocktower/domain/StableCandidateIdFactory.kt` — 1747 B
- `clocktower/domain/StorytellerPlayerContext.kt` — 1439 B
- `clocktower/domain/StorytellerProviderContractV1.kt` — 23003 B
- `clocktower/domain/StorytellerProviderHistoryPrefixV1.kt` — 9442 B
- `clocktower/domain/TroubleBrewingGameSnapshot.kt` — 5165 B

### 观察记录 / 历史推理 (45)

- `clocktower/epistemic/A4DeviceBenchmarkHarness.kt` — 8954 B；此批确认删除
- `clocktower/epistemic/A4IdentityRevealPrewarmCoordinator.kt` — 19443 B
- `clocktower/epistemic/A4ObservationCacheRebuildExecutor.kt` — 10141 B
- `clocktower/epistemic/A4ObservationCacheUpdateCoordinator.kt` — 1888 B
- `clocktower/epistemic/A4PlayerKnowledgeFactory.kt` — 4467 B
- `clocktower/epistemic/A4PlayerWorldSetRuntime.kt` — 6031 B
- `clocktower/epistemic/A4ShadowLifecycleInvalidator.kt` — 970 B
- `clocktower/epistemic/A4ShadowWorldSetCache.kt` — 2432 B
- `clocktower/epistemic/A4WorldEngineRuntimePolicy.kt` — 3208 B
- `clocktower/epistemic/ActionFactDraft.kt` — 12179 B
- `clocktower/epistemic/ActionFactTimeline.kt` — 5094 B
- `clocktower/epistemic/B4DynamicPlayerWorldSetShadow.kt` — 8920 B
- `clocktower/epistemic/EnumeratedHistoricalExactBaseline.kt` — 6030 B
- `clocktower/epistemic/EnumeratedHistoricalWorldReplay.kt` — 19075 B
- `clocktower/epistemic/EnumeratedWorldImpSelfKillSuccessionBranching.kt` — 3437 B
- `clocktower/epistemic/EnumeratedWorldMayorNightDeathBranching.kt` — 4707 B
- `clocktower/epistemic/EnumeratedWorldNightObservationAnchor.kt` — 3639 B
- `clocktower/epistemic/EnumeratedWorldNightReplayPlan.kt` — 2668 B
- `clocktower/epistemic/EnumeratedWorldNightSchedule.kt` — 1432 B
- `clocktower/epistemic/EnumeratedWorldOtherNightAttackBranching.kt` — 3055 B
- `clocktower/epistemic/EnumeratedWorldOtherNightMechanicsMaterializer.kt` — 3734 B
- `clocktower/epistemic/EnumeratedWorldOtherNightProtectionBranching.kt` — 2068 B
- `clocktower/epistemic/EnumeratedWorldSet.kt` — 36760 B
- `clocktower/epistemic/EpistemicContracts.kt` — 8761 B
- `clocktower/epistemic/EpistemicEvaluationCapability.kt` — 2975 B
- `clocktower/epistemic/EpistemicObservationDraft.kt` — 3378 B
- `clocktower/epistemic/EpistemicObservationLog.kt` — 9783 B
- `clocktower/epistemic/EpistemicSemanticJson.kt` — 38851 B
- `clocktower/epistemic/EpistemicSemanticModel.kt` — 24684 B
- `clocktower/epistemic/ExactHistoricalHypotheticalObservationBundleEvaluator.kt` — 28136 B
- `clocktower/epistemic/GrimoireReminderToken.kt` — 3201 B
- `clocktower/epistemic/KnowledgeConstructionInput.kt` — 2734 B
- `clocktower/epistemic/KnowledgeSafeWorldInput.kt` — 1706 B
- `clocktower/epistemic/PlayerHistoricalTimeline.kt` — 7026 B
- `clocktower/epistemic/PlayerWorldSet.kt` — 2890 B
- `clocktower/epistemic/RegistrationWitnessSemanticMatcher.kt` — 2313 B
- `clocktower/epistemic/SpyGrimoireTruthProjection.kt` — 5574 B
- `clocktower/epistemic/StrategicWorldKey.kt` — 2182 B
- `clocktower/epistemic/TroubleBrewingGrimoireReminderSemantics.kt` — 2352 B
- `clocktower/epistemic/TroubleBrewingStrategicTopologyDomain.kt` — 2404 B
- `clocktower/epistemic/TroubleBrewingTopologyHypotheticalBundleEvaluator.kt` — 10056 B
- `clocktower/epistemic/TroubleBrewingTopologyObservationWitnessEvaluator.kt` — 28837 B
- `clocktower/epistemic/TroubleBrewingTopologySetupWitnessEvaluator.kt` — 24302 B
- `clocktower/epistemic/TroubleBrewingWorldEnumerator.kt` — 11627 B
- `clocktower/epistemic/ZddPlayerWorldSet.kt` — 24866 B

### 流程顺序 (10)

- `clocktower/flow/ClocktowerCharacterInteractionRegistry.kt` — 8465 B
- `clocktower/flow/ClocktowerFlowPlanner.kt` — 4731 B
- `clocktower/flow/ClocktowerHostInteraction.kt` — 1576 B
- `clocktower/flow/ClocktowerHostInteractionProjector.kt` — 5245 B
- `clocktower/flow/ClocktowerProductionFirstNightFlow.kt` — 2855 B
- `clocktower/flow/ClocktowerProductionInteractionOrderer.kt` — 6257 B
- `clocktower/flow/ClocktowerProductionOtherNightFlow.kt` — 2198 B
- `clocktower/flow/ClocktowerResolvedFlowFacts.kt` — 877 B
- `clocktower/flow/FirstNightSetupDependencyPlanner.kt` — 3658 B
- `clocktower/flow/FirstNightSetupDependencyProjection.kt` — 11867 B

### 推荐 / 合法域 (15)

- `clocktower/recommendation/DecisionSeedFactory.kt` — 1866 B
- `clocktower/recommendation/dynamic/DynamicCandidateGenerator.kt` — 5350 B
- `clocktower/recommendation/dynamic/PairInformationPresentationModels.kt` — 380 B
- `clocktower/recommendation/dynamic/UnreliableInformationModels.kt` — 1577 B
- `clocktower/recommendation/FirstNightBundleCandidateSpaceAudit.kt` — 16640 B
- `clocktower/recommendation/FirstNightBundleExperimentContract.kt` — 8207 B
- `clocktower/recommendation/FirstNightInformationPropositionMaterializer.kt` — 6870 B
- `clocktower/recommendation/FirstNightNumericLegalDomain.kt` — 2143 B
- `clocktower/recommendation/NaturalPairInformationCandidateGenerator.kt` — 1999 B
- `clocktower/recommendation/PairInformationLegalDomain.kt` — 1188 B
- `clocktower/recommendation/setup/SetupCandidateGenerator.kt` — 10175 B
- `clocktower/recommendation/StorytellerDecisionAuthority.kt` — 1711 B
- `clocktower/recommendation/TroubleBrewingFirstNightPairDecisionContext.kt` — 6666 B
- `clocktower/recommendation/TroubleBrewingSetupRecommendationDecisionContext.kt` — 7410 B
- `clocktower/recommendation/UnifiedSelectionPool.kt` — 2465 B

### 规则 / 线索计算 (23)

- `clocktower/rules/AbilityFunctioningSemantics.kt` — 2689 B
- `clocktower/rules/ClocktowerEffectiveNightState.kt` — 3936 B
- `clocktower/rules/ClocktowerOptionalNightSourceChronology.kt` — 1387 B
- `clocktower/rules/CurrentDemonAuthority.kt` — 655 B
- `clocktower/rules/DemonNightAttackSemantics.kt` — 1752 B
- `clocktower/rules/DemonSuccessionSemantics.kt` — 1186 B
- `clocktower/rules/FirstNightNumericInformationDisplayDomain.kt` — 2885 B
- `clocktower/rules/FirstNightNumericInformationSemantics.kt` — 4964 B
- `clocktower/rules/FixedInformationEvaluator.kt` — 1578 B
- `clocktower/rules/FortuneTellerInformationSemantics.kt` — 3137 B
- `clocktower/rules/MayorRedirectLegality.kt` — 2068 B
- `clocktower/rules/NaturalPairInformationCandidateGenerator.kt` — 16610 B
- `clocktower/rules/NoGreaterJoyKlutzChoiceRuleV1.kt` — 2569 B
- `clocktower/rules/PairInformationDisplaySemantics.kt` — 2901 B
- `clocktower/rules/PairInformationLegalDomain.kt` — 4078 B
- `clocktower/rules/PlanLegalityValidator.kt` — 8310 B
- `clocktower/rules/PoisonEffectLifecycle.kt` — 480 B
- `clocktower/rules/RegistrationInteractionRules.kt` — 347 B
- `clocktower/rules/RulesetContentHasher.kt` — 2875 B
- `clocktower/rules/RulesetJsonLoader.kt` — 3108 B
- `clocktower/rules/RulesetKnowledge.kt` — 995 B
- `clocktower/rules/TroubleBrewingRecurringPlayerInformationRouteDomain.kt` — 1394 B
- `clocktower/rules/TroubleBrewingRegistrationDomain.kt` — 5578 B

### Session / AI (46)

- `clocktower/session/AuthenticatedModelTransportV1.kt` — 2770 B
- `clocktower/session/BotcAiWireTrace.kt` — 6748 B
- `clocktower/session/ClocktowerB4HistoricalShadowCoordinator.kt` — 3468 B；此批确认删除
- `clocktower/session/ClocktowerGameSession.kt` — 41763 B
- `clocktower/session/ClocktowerGameSessionAliveBoundaries.kt` — 2512 B
- `clocktower/session/ClocktowerGameSessionPoisonBoundaries.kt` — 4846 B
- `clocktower/session/ClocktowerGameSessionRoleIdentityBoundaries.kt` — 1629 B
- `clocktower/session/ClocktowerNightCheckpoint.kt` — 1320 B
- `clocktower/session/ClocktowerNightTargets.kt` — 1844 B
- `clocktower/session/ClocktowerRecommendationCoordinator.kt` — 3497 B
- `clocktower/session/ClocktowerVoteStanding.kt` — 633 B
- `clocktower/session/DawnCommitIdentity.kt` — 992 B
- `clocktower/session/DayAbilityRegistrationRulingProducerV1.kt` — 20375 B
- `clocktower/session/DrunkAssignmentDecisionBoundary.kt` — 2472 B
- `clocktower/session/FirstNightInformationLifecycle.kt` — 911 B
- `clocktower/session/FirstNightInformationMigration.kt` — 3609 B
- `clocktower/session/FirstNightPoisonLifecycle.kt` — 1257 B
- `clocktower/session/HostCurrentOpenDecisionInventoryV2.kt` — 4612 B
- `clocktower/session/HostOpeningResidualEventGateV2.kt` — 19089 B
- `clocktower/session/HostOpeningRulesDomainV2.kt` — 14527 B
- `clocktower/session/HostOpeningSemanticGateV2.kt` — 18263 B
- `clocktower/session/InformationDecisionFoundation.kt` — 11251 B
- `clocktower/session/InitialGlobalOpeningScopeV1.kt` — 14446 B
- `clocktower/session/InitialJointOpeningDirectV2.kt` — 14756 B
- `clocktower/session/MayorRedirectDecisionBoundary.kt` — 4496 B
- `clocktower/session/NightDawnResolutionPlanner.kt` — 12201 B
- `clocktower/session/NightTransactionRestoreComposition.kt` — 5660 B
- `clocktower/session/NoGreaterJoyKlutzHistoryProducerV1.kt` — 3449 B
- `clocktower/session/PairInformationDecisionBoundary.kt` — 5263 B
- `clocktower/session/PersonalDirectOpenAiV1.kt` — 10171 B
- `clocktower/session/RecommendationModules.kt` — 1741 B
- `clocktower/session/SmallDomainPresentation.kt` — 1608 B
- `clocktower/session/StorytellerCausalDecisionJournalV1.kt` — 17613 B
- `clocktower/session/StorytellerDecisionFoundation.kt` — 3371 B
- `clocktower/session/StorytellerGlobalEventProjectionV1.kt` — 9133 B
- `clocktower/session/StorytellerProviderGameContextBuilderV1.kt` — 3306 B
- `clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt` — 3345 B
- `clocktower/session/StorytellerProviderRequestFactoryV1.kt` — 6606 B
- `clocktower/session/StorytellerResidualPackageGateV2.kt` — 2858 B
- `clocktower/session/StorytellerStrategicChangeCheckpointV2.kt` — 10135 B
- `clocktower/session/StorytellerStrategicContinuationV2.kt` — 28761 B
- `clocktower/session/StructuredBooleanInformationUiModel.kt` — 3290 B
- `clocktower/session/StructuredNumberInformationUiModel.kt` — 3272 B
- `clocktower/session/TroubleBrewingDemonSuccessionResolver.kt` — 6161 B
- `clocktower/session/TroubleBrewingFirstNightPrecomputeCoordinator.kt` — 4153 B
- `clocktower/session/TroubleBrewingRuntimeGameProjector.kt` — 3512 B

### 发牌与初始配置 (22)

- `clocktower/setup/ClocktowerSetupDistribution.kt` — 1018 B
- `clocktower/setup/ClocktowerSetupProvider.kt` — 4186 B
- `clocktower/setup/GeneratedSetupCandidateSource.kt` — 5567 B
- `clocktower/setup/NoGreaterJoyProductionSetupPreparer.kt` — 3562 B
- `clocktower/setup/SetupDiversityHistory.kt` — 1550 B
- `clocktower/setup/SetupDiversitySelector.kt` — 11312 B
- `clocktower/setup/SetupShownIdentityCommitment.kt` — 4807 B
- `clocktower/setup/SetupShownIdentityPolicy.kt` — 8649 B
- `clocktower/setup/TemplateRepository.kt` — 2996 B
- `clocktower/setup/TroubleBrewingDealRoleResolver.kt` — 2756 B
- `clocktower/setup/TroubleBrewingDrunkCandidateProjection.kt` — 9104 B
- `clocktower/setup/TroubleBrewingDrunkSelectionRouter.kt` — 2505 B
- `clocktower/setup/TroubleBrewingGameSnapshotProjector.kt` — 7788 B
- `clocktower/setup/TroubleBrewingIntermediateSetup.kt` — 5166 B
- `clocktower/setup/TroubleBrewingProductionSetupPreparer.kt` — 4782 B
- `clocktower/setup/TroubleBrewingSetupCommitter.kt` — 7659 B
- `clocktower/setup/TroubleBrewingSetupDealPlanner.kt` — 13250 B
- `clocktower/setup/TroubleBrewingSetupPresetJson.kt` — 5548 B
- `clocktower/setup/TroubleBrewingSetupPresetModels.kt` — 1083 B
- `clocktower/setup/TroubleBrewingSetupPresetValidator.kt` — 8574 B
- `clocktower/setup/TroubleBrewingSetupRotationHistory.kt` — 2896 B
- `clocktower/setup/TroubleBrewingSetupRotationRecordFactory.kt` — 11970 B

### 旧模拟模块 (1)

- `clocktower/simulation/DistributionReport.kt` — 4605 B；此批确认删除

### 方桌 UI (18)

- `clocktower/ui/ClocktowerArtistInteraction.kt` — 6603 B
- `clocktower/ui/ClocktowerArtistTableUi.kt` — 4130 B
- `clocktower/ui/ClocktowerDayOverviewTableUi.kt` — 6871 B
- `clocktower/ui/ClocktowerDayScreen.kt` — 13540 B
- `clocktower/ui/ClocktowerDayTableScaffoldUi.kt` — 1002 B
- `clocktower/ui/ClocktowerDrunkSelectionScreen.kt` — 8884 B
- `clocktower/ui/ClocktowerHostScreen.kt` — 150032 B；超过 50 KB：必须净删/拆分
- `clocktower/ui/ClocktowerKlutzTableUi.kt` — 5712 B
- `clocktower/ui/ClocktowerNightScreen.kt` — 15696 B
- `clocktower/ui/ClocktowerNominationInteraction.kt` — 6127 B
- `clocktower/ui/ClocktowerPendingNominationTableUi.kt` — 7382 B
- `clocktower/ui/ClocktowerResultsScreen.kt` — 17211 B
- `clocktower/ui/ClocktowerSetupScreen.kt` — 20901 B
- `clocktower/ui/ClocktowerSlayerInteraction.kt` — 4217 B
- `clocktower/ui/ClocktowerSlayerTableUi.kt` — 8133 B
- `clocktower/ui/ClocktowerVoteTableUi.kt` — 7854 B
- `clocktower/ui/StorytellerAutoPauseScreen.kt` — 2721 B
- `clocktower/ui/StructuredStorytellerInformationUi.kt` — 1753 B

### 调试记录 (2)

- `debug/DebugFlightRecorder.kt` — 8403 B
- `debug/DebugFlightRecorderCore.kt` — 2706 B

### 偏好/凭据持久化 (5)

- `persistence/AppPreferencesStore.kt` — 4026 B
- `persistence/GatewayTokenEnvelopeV1.kt` — 1294 B
- `persistence/PersonalOpenAiSettingsStore.kt` — 1273 B
- `persistence/StorytellerGatewayConnectionStore.kt` — 4649 B
- `persistence/TroubleBrewingSetupRotationHistoryStore.kt` — 12917 B

## 变更验收方法

每批从当前正确 HEAD 起步，定位生产读写者（包括 UI、Session、AI、CI），标记“删除文件、同步删除旧测试、保留的数据完整性”，实际修改后跑 GitHub CI/R2；不要让旧规则校验回归测试阻止明确批准的行为退休。每批给出净减少、超限文件数和留下的待确认风险。PR 在未授权合并前保持 Draft。

## 实施检查点 S0b（2026-10-18）

经过独立生产引用检索，下列原孤立规则/候选模型没有 Android 生产消费者；已随测试删除，而不是为过时测试保留规则引擎：
- `app/src/main/java/com/codex/campboardgamehost/clocktower/session/SmallDomainPresentation.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/history/HistoricalClueSignature.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/rules/PlanLegalityValidator.kt`
- `app/src/main/java/com/codex/campboardgamehost/clocktower/domain/ConstraintAuthority.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/session/SmallDomainPresentationTest.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/rules/PlanLegalityValidatorTest.kt`

`CandidateGeneratorTest.kt` 中只依赖 `PlanLegalityValidator` 的旧规则断言已移除，保留该文件其余断言暂待整个旧 setup candidate 生成器迁移审计。**本台账上方库存为基线目录树**，包含清理前的文件名；实际最新余量以当前 PR 的 Git 树为准。

## 全类型源码清单和网页残留复核（2026-10-18）

使用最新 HEAD 重新扫描 Kotlin、Kotlin DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML 及 Gradle 代码文件：**717 个**。完整路径/字节数/逐文件初步审计队列见 [全源码文件索引](MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv)，所有条目来源于未截断的 Git 树。该索引中的待处理状态只是分流，不宣称所有文件已完成源代码行为审计。

### 新发现：旧 web 产品边界

- `web/app.js` 143,503 B，是独立的离线网页主持台，不依赖 Android Host 当前 Session；包含 `freshUndercover`、`renderUndercover`、`freshWerewolf`、`renderWerewolf`、网页独立 `localStorage` / 事件逻辑。
- 所以“Undercover 已完全清除”严格只对 Android 源代码成立；**整个仓库 web 文件仍包含 Undercover**。是否整个废弃该嵌入式 PWA、是否同时废弃狼人杀，**需用户明确审核**。如果保留 web，就必须先剔除已废弃玩法和独立重复事实状态，再把 `web/app.js` 拆分至 <=50,000 B；不可通过单纯分文件假装业务已经精简。
- 历史 `app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json` 为 331,689 B，属于配置数据而非本轮“源码 <=50 KB”硬限制，但其必要性需要单独审计消费者与模板内容。

