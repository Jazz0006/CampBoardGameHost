# ARCH-CLEANUP-1 — 架构收敛与冗余清理（2026-10-15）

> 状态：**进行中；清理优先，AI-EVENT-JIT 暂停**。本文件记录已经由调用链审计证明的事实与本次实际改动，不等于整局历史完整性已经通过验收。
>
> 开发分支：`cleanup/arch-cleanup-1-20261015`；[Draft #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) 叠加在 #308（后者在 #307 → #306 → #304 之上）。**不得直接合并到当前 main**。主线 #309 的 evidence-first `AGENTS.md`/`TESTING_STRATEGY.md` 已同步。#310–#314 已经逐一审阅并全部关闭且**未合并**（过时的模型候选 ID、虚假的空历史增量和过滤提名/投票，以及审计文档迁移）。1E 实验分支仍待核对，以上均不视为生产契约。

## 1. 目标和保留边界

停止独立的模型提示词设计、付费 API 调用、AI-EVENT-JIT 新功能；只做架构/冗余清理与相应验证，直至 PR/文档/CI 收口。

- 权威当前 GameState 仍由 `ClocktowerGameSession` / `ClocktowerSessionState` 持有；`GameSnapshot`、TBGS V1 与历史前缀是只读投影，不新增第二套可写游戏对象。
- `ActionFactTimeline` + `EpistemicObservationLog` 在 `GLOBAL_V1` 下表示真实结构化机械动作及玩家收到的信息。保留全部提名和逐人投票；时间次序不得伪造。
- `StorytellerCausalDecisionJournalV1` 有真实生产写入与 Recovery 消费，保留；其中 `StorytellerProviderPriorDecisionV1` 与 `DecisionOutcomeSnapshot` 是实际使用的因果决策数据。
- `SdeHistoricalReplayInput`、`DynamicActionReducer`、`TroubleBrewingGameSnapshotProjector`、Recovery、Game Archive、规则合法域/Host 裁决/合法性检查继续保留。**不开发新通用时间点重放**。
- 不保留旧 APK/旧存档/过时策略的行为兼容；但任何正在消费的手动规则路径必须保持可用。EvidenceLab/TBGS 既有共享协议不随意更改。

## 2. 已实施的源码清理批次

### 第一批：死的推荐决策事件存储和评分

检查 `appendDecision`、`transitionDecision`、`correctDecision`、`explainDecision`、`postGameReview` 均无生产调用方，因此移除 `DecisionEventStore`、`HistoryReviewModule`、评分解释 `DecisionReviewModels` 与没有调用方的 `DecisionScoreTolerances`。保留 `ClocktowerRecommendationCoordinator` 的实际中性合法信息解析。删除仅服务被淘汰逻辑的测试，保留真实类别信息解析测试。

已提交 `330718d81c20`，第一批差异：7 文件、删除 532 行。对应 GitHub CI #4186、R2 #3802 **GREEN**；本地 FAST 无法连接 `photoprismsv:22`，属于独立构建机连通性问题。

### 第二批：从未写入的历史档案及双重投影

完成全生产代码调用链审计：`DecisionHistoryArchive` **没有真实的提交生产者**，Session 与 Snapshot 一直持有默认空实例；过去只有单元测试构造非空对象。因此：

- 从 `GameSnapshot`、`ClocktowerSessionState` 和快照转换删除该历史冗余字段；
- 从模型 continuation JSON 删除始终为空的 `unsequencedDecisionHistory`，去除基于空档案的变化检测；
- 删除 `DecisionHistoryRepository`、旧 `StorytellerDecisionEvent`、决策纠正链、误导计数/信息压力/登记统计和对应专属测试；
- 把实际有 UI 使用者的 `extractSeatNumbers` 提取到 `InformationReferenceExtractor`，保留展示解析行为，但绝不把文本数字反向解释为真实游戏历史；
- 单独保留并迁移 `DecisionOutcomeSnapshot`，因为真正的因果裁决日志、记录生产者和 Recovery 正在使用它；
- Provider `priorDecisions` 保留实际因果日志注入的接口，而非使用旧空档案生成“历史”；
- 保留当前结构化动作/观察记录、投票、版本绑定及 Host 合法性检查。

第二批提交 `ef8c86223c45`，GitHub Android CI #4187 与 R2 #3803 GREEN；整个架构重构仍未完成。

### 第三批：废弃的 rollout 切换门槛

生产引用搜索证明 `SelectionRolloutGate`、`SelectionRolloutStage` 与对应旧 `SelectionRolloutGateTest` 没有生产消费者，仍描述 `LEGACY_ONLY/SHADOW_COMPARE/LIMITED_AUTO` 旧推荐发布阶梯。本轮删除该独立旧策略门槛及专属测试，不触及当前 `StorytellerOperationMode`（MANUAL/AI_ASSISTED/AI_AUTOMATIC）与 Host 真实模式实现。第三批提交 `ea8f471`，GitHub Android CI #4189 与 R2 #3805 GREEN。

### 第四批：旧首夜角色独立整包评估与 D5 校准

经 `app/src/main/java` 全生产调用链和 `app/src/test` 闭包审计，以下不再具有任何运行时消费者，都是此前已经退役的基于 Drunk 角色和 Demon Bluff 的离线实验分析（不是规则或 Host 信息合法域）：

- 删除 `FirstNightDrunkWholeBundleExactEvaluator`、`FirstNightDrunkPairWholeBundleEvaluator`、`FirstNightDrunkNumericWholeBundleEvaluator`、`FirstNightDrunkFortuneTellerWholeBundleEvaluator`；
- 删除 `DemonBluffJointOutputEvaluator`（其中的 `SetupDemonBluffJointOutputAdapter` 亦无独立生产调用者；真正的 setup bluff 规则候选生成和 Host 合法性校验不在此文件）；
- 删除上述离线评估器的直接单元测试及只为它们服务的 D5 Drunk/Demon Bluff 校准辅助类与测试；
- 在 `Sde2D5CalibrationExperiment` 中移除这两个已弃用的实验场景与 lazy 构造，**保留**该文件其余的跨玩家人数基本拓扑验证与真人案例测试；`sde2D5Calibration` Gradle 任务因仍服务剩余真实测试而保留；
- 保留共享的 `FirstNightBundleExperimentContract`、`FirstNightBundleCandidateSpaceAudit`、规则拥有者和 `ExactHistoricalHypotheticalObservationBundleEvaluator`；当时先保留了仅由测试消费的 `FirstNightBundleHealthyHarness`，其后在第五批独立确认并清理。

第四批已在 `3dbb175ec9fa` 提交，经 GitHub CI #4190 与 R2 #3806 验证 GREEN。尚未证明全部旧实验无用；未触碰实际夜晚信息、登记、语义时间线或 Recovery。

### 第五批：FN-BUNDLE-3 离线穷举报告管线

继续按生产引用做闭包审计：`FirstNightBundleHealthyHarness`、FN-BUNDLE-3 初学者 corpus、低信息量校准和 Fortune Teller pilot 没有生产调用者；只用于自己生成历史报告的测试类。删除 1 个生产 source 文件和 6 个 test source 文件，连同 `FirstNightBundleHealthyHarnessAcceptanceTest`、`FirstNightBundleBeginnerCorpusExperiment`、其独立报告构造与校准类，保留 `FirstNightBundleCandidateSpaceAudit` 等确有其他 SDE 消费者的生产代码。

从 `app/build.gradle.kts` 删除已退役的 `fnBundle3Calibration` 专用任务、FAST/FULL 排除配置和耗时实验清单中的孤儿项，保留真实 `Sde2D4TopologyBundlePerformanceTest`、`sde2D5Calibration` 和其他仍存在的校准任务。同步 `docs/TESTING_STRATEGY.md` 中当前测试任务矩阵与过时类别清单；历史归档不改写。第五批提交 `88a51a5c8bc2`，GitHub Android CI #4191 与 R2 #3807 GREEN。

### 第六批：移除以界面文案反解析历史的死代码

`ClocktowerHostScreen.recentMisinformationStreak` 从 `ClocktowerEvent.title` 中寻找中文“误导”或英文 `misleading`，推断信息误导连击数；该值在 `ClocktowerInformationStepBuilder` 中仅赋值给 `ClocktowerNightStepUi.recentMisinformationStreak`，源码全局搜索证明**没有任何读取者**。因此删除解析函数、传参回调、Ui 模型字段及五个单元测试中不再需要的虚拟参数。保留其余结构化信息测试。这个切片既删除无效维护点，也杜绝未来把文案反向当作权威事件事实使用。第六批提交 `0a3a7976cece`，GitHub Android CI #4192 与 R2 #3808 GREEN。

### 第七批：时间线旧重载与首夜实验包评估薄包装器

- `ClocktowerSemanticHistoryMode.requireCompatible(observationLog, cursor)` 是旧版忽略 `ActionFactTimeline` 的兼容重载。所有生产调用方已传入 action timeline；删除此无人调用的重载，保留 `GLOBAL_V1` 的三参数真实校验，防止未来误从空动作记录验证。
- `FirstNightBundleExperimentEvaluator` 只被两个实验性测试调用，是对现存 `ExactHistoricalHypotheticalObservationBundleEvaluator` 的单层包装。删除 `FirstNightExperimentProfile`、诊断/响应 wrapper 与这层无生产使用的评估器；把原有两项测试改为直接调用底层精确评估器，保留善良公开线索投影、醉酒可信世界、双接收者排序、原始 bundle 不变等证据。
- 不删除 `FirstNightPublicGoodInfoProjection` 和 `FirstNightBundleProfileExposure`：虽然属于旧的实验语义，但其他仍保留的实验/影子模块有消费者；不影响 Host 当前联合法律裁量。
- 第七批源码提交 `f4c9468643e7`，GitHub CI #4194（含 Android tests、Real Clingo）与 R2 #3810 均 GREEN。

## 3. 审计结论：尚未清理的生产调用链

P1 PR 依赖核对：#304（首轮联合推荐/Host 验证）、#306（变更检查点）、#307（人工/自动采纳与旧 V1 移除、仍活跃的 whole-`remainingChoices`）、#308（已采纳响应作为 Responses continuation anchor）都有现存真实生产调用。故先保证这条联合作业功能健全，不能直接把 #315 merge into main，不能按文件名删除整个 P1；旧 whole-`remainingChoices` 只能在未来新语义合同完成替换之后退休，而不是被误判为无调用死代码。

逐个审计了 `toClocktowerGameState()` 的全部生产调用：Session 创建前、Recovery 重建、夜晚/破晓裁决、首夜信息请求及 Day table 的 UI 卡牌输入都是实际消费者；**现阶段没有证据确保 Session 中的 GameState 比实时传入的 PlayerCard 更新**。因此绝不通过机械地替换为 `session.state.gameState` 来减少调用，而冒险引用旧角色或存活状态。Day table 同一轮渲染中 6 处重复卡牌转换可考虑在同一作用域内只计算一次，但不得跨 Compose 输入变化缓存。整个系统要真正单状态，需要先界定 UI 临时编辑与 Session 确认事实的写入边界。

Recovery 与 Game Archive 具有不同合同：前者供继续游戏，后者供复盘，两者已经复用 `AppGameStateJsonCodec` 的低层卡牌/事件/结果映射。**不应把不同用途的归档结构硬合并**。

部分名字含 `sde`、`candidate`、`scoring`、`shadow` 的模块可能仅供历史实验，也可能提供仍活跃的规则候选/手动展示或因果重放。应先做 **生产消费者闭包** 审计，再成组删除，不能按文件名批量删除。

## 4. 未收口 / 待验证

1. #315 七批源码至 `f4c9468643e7` 的 Android CI、Real Clingo、R2 已通过；但仅覆盖自动化证据，不代表手动 Host/Recovery 的真实设备端验收。最终归并 HEAD 再跑相应检查；本地构建机 SSH 故障单独处理，不把构建机故障记作应用错误。
2. 清理其余没有生产入口的旧推荐评分/概率与历史实验类族（只删除真实死亡依赖图）。
3. 逐项核实当前权威状态转换点、Recovery 和 Archive 的真实边界与跨项目 TBGS 语义。
4. **已关闭 #310–#314（全部未合并）**：#310 的 Host 单项门控值得复用，但现有代码让模型输出 candidateId；#311 的事件增量使用已废弃的 `ClocktowerSessionState.decisionHistory`、`StorytellerDecisionEvent` 与 `DecisionCorrectionEvent`，当前 #315 已移除这套空档案，不能直接 cherry-pick。#312 的模型合同过时；#313 筛掉合法提名/逐人投票与完整历史不符；#314 的原始审计文档已引入本清理分支。可迁移的是 Host 单项合法性、前缀单调性和私有缓存原则，不是旧接口。已核对 1E 分支：独立 `StorytellerJitDirectResultV2` 对结果数字/真假/Pair/角色/座位执行严格结构化 JSON 解码、模型不接收 candidate ID 且明确 Host 验证为后置；`HostAiDecisionRelevantFactsV2` 对所有合法提名、逐人投票和阶段变化均保留。**但它们尚未接入实际 API/UI**，后者调用的 `HostCausalEventDeltaV2` 仍依赖 #311 已删除的空档案，故**绝不整体合并 1E**；仅标记两项可单独复用的设计/测试素材。关闭的 PR 远端分支暂不删除，以防依赖无法追踪。
5. 适度整合活动路线与交接文档，保留必要历史审计，但不再将旧项目计划列为当下 NEXT。

## 5. AI-EVENT-JIT 恢复门槛（本轮结束后）

只有在确切 PR HEAD CI/R2 和规则/手动/恢复重要路径证明通过、没有误删有效历史生产者、跨项目协议保留、待处理 PR 明确关闭或归并、工作树 clean，且主线规范与 handoff 同步后，才将本任务标记为 CLOSED。之后恢复“首轮整局战略、后续唯一单项语义结果、全量结构化事实增量及 Host 验证”的主路线；**不是**复活旧的候选 ID/整包 `remainingChoices` 模型输出。
