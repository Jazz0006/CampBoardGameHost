> **历史库存保留，现行审计结论见下文最新补记。** 2026-10-18 所有者纠正了早期方案：Host Core 保留唯一事实及安全提交；旧 Host 规则合法性门禁要删除，未来 Rules 独立只读重建；**现有 AI 说书人推荐系统必须完整保留**，不再执行“AI 清空后重建”。旧库存列表含先前已删除文件，不能视为现行 Git tree。

# 最小 Host 全仓源码遍历 / 删除边界台账（2026-10-18）

## 2026-10-18 全树重启审计（替代沿调用路径逐段清理；最新当前方向）

- 全量 Git tree 快照 SHA `8c1a4dc114060806c33d5f78102b6b3c6457cf71`：未截断、**1135 个文件**（树条目除目录）、按扩展名初筛 **723 个代码/配置类文件**（含 Kotlin/DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML、JSON 等）；Android Kotlin **337 生产 + 320 测试**。不同扩展名口径下的源代码数量不能与上方历史的 717 或窄口径 703 直接比作增删。
- 审计必须**横向遍历全部真实代码目录及构建/工具资产**：Host UI 与根组件、Session、domain、epistemic、rules、recommendation、setup、flow、catalog、persistence、debug、test、Web、工具/CI；每个文件先按职责分类，而不是沿某个旧规则调用栈寻找下一步。
- **决策标签**：`KEEP_CORE` 唯一事实、事件、安全、隐私、必要输入/展示；`KEEP_AI` 实际 AI 规划、建议、提示词、Provider、响应/战略与必须的规则候选依赖；`REMOVE_CONFIRMED` 仅自身或专属测试使用的旧实验/适配和过度 Host 规则门禁；`REMOVE_AFTER_DISCONNECT` 已判断应删但存在实际消费者，整条拆除后删；`REVIEW_SCOPE` 需要产品边界决策；`AUDIT_PENDING` 只有路径/体积证据，尚不能声称消费者审计完成。不得以目录名推定去留，也不得保留原代码仅因为旧测试要求。
- 全仓源码索引 `MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv` 是**旧基线库存**，不是另一套可写状态；新增审计结果统一记入本文件。任何新 HEAD 对照均重新拉完整 Git 树，不以静态清单判定实际删除完成。

| 目录/链路 | 当前判定 | 可核实依据与操作 |
|---|---|---|
| domain / ClocktowerGameSession / canonical ActionFact / UI 事实提交 | KEEP_CORE + AUDIT_PENDING（混合文件） | 保留唯一状态、已确认事实、安全、私密/顺序/去重/过期防护；不按旧 Rules 正确答案驳回已发生信息 |
| 首夜 HostOpeningRulesDomainV2 + InitialJointOpeningDirectV2、配对候选与 Provider、战略续规划 | KEEP_AI | 生产 App、Session、推荐路径直接消费；既有 AI 不重建不降级 |
| FixedInformationEvaluator、FirstNightNumericInformationSemantics、TroubleBrewingRegistrationDomain | KEEP_AI（混合旧 Rules） | 数值与配对 AI 候选实际使用；先分离 Host 规则 veto 后再清理旧 Host 消费者 |
| A4 identity-reveal prewarm / A4 Observation ZDD shadow | F3 REMOVE_CONFIRMED；A4 runtime/ZDD 剩余 AUDIT_PENDING | App 的调用全部限定 BuildConfig.DEBUG、TB 5 人局；只对旧影子缓存生成诊断日志，不供 AI/事实提交。现已连消费者整链删除；通用世界推演运行时在其他测试中仍有引用，单独审计 |
| B4DynamicPlayerWorldSetShadow / ExactHistorical 和 Topology bundle 旧实验 | B4 独立接口 REMOVE_CONFIRMED（F2）；Exact/Topology AUDIT_PENDING | B4 全部生产引用仅本文件，无 AI 消费；时间线测试改测共享 API，Exact 评估测试独立保留；其余大型推演不能据此一起删 |
| FN-BUNDLE PublicGoodInfoProjection 等压力实验 | REMOVE_AFTER_DISCONNECT 待核 | 旧实验/测试消费者存在；与正式联合规划 API 不是同一合同；先完整检查引用，再批量删除 |
| ClocktowerHostSelectionSemantics / HostScreen / NightStepUi | AUDIT_PENDING 混合 | 夜间人工候选过滤和规则否决需要删，但显示、事实与 AI 自动/辅助流程要保留 |
| legacy web/ 独立网页客户端及多剧本 | REVIEW_SCOPE | 非 Android Host，是否保留 Undercover/狼人及旧网页，由产品范围决定 |
| tools、CI、assets、测试 | AUDIT_PENDING | 以当前 CI/R2/运行时消费者实际使用为依据，专属废弃实验测试可与生产实现一起删除 |

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

