> **历史扫描快照，不是现行产品保留决策。** 2026-10-18 项目所有者已批准三个独立职责 Host Core / Rules（旧实现全删后重建）/ AI Clues（Host 清理后重建），以及 P1→P5 净删路线。请以 HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md 为准。本文件保留原始全树数据与实际删除批次，仅作为审计证据。下方“旧规则模块不再存在”等措辞是当时阶段表述，不应理解为永远不允许独立 Rules 模块。

# 最小 Host 全仓源码遍历 / 删除边界台账（2026-10-18）

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

