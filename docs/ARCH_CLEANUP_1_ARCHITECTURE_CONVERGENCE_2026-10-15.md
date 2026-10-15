# ARCH-CLEANUP-1 — 架构收敛与冗余清理（2026-10-15）

> 状态：**进行中；清理优先，AI-EVENT-JIT 暂停**。本文件记录已经由调用链审计证明的事实与本次实际改动，不等于整局历史完整性已经通过验收。
>
> 开发分支：`cleanup/arch-cleanup-1-20261015`；**[唯一开放的整合候选 Draft #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) 已直接改以 live `main` 为 base，GitHub mergeable/clean**，但尚未合并或完成真机端验收。原 P1 #304→#306→#307→#308 的全部 HEAD 均是 #315 的 Git 祖先，四个 PR 已关闭未合并，远端分支保留以供追溯；#310–#314 已先前逐一关闭未合并。#309 evidence-first 的关键规范在清理分支同步，后续所有源码与文档以 #315 为单一审阅入口。1E 是不直接合并的独立实验分支。

## 第十批：2026-10-16 最终复审新增清理（尚待本批精确 HEAD CI/R2）

- 复核 live `main=770ed32f446d03cf81f226901afa78cbdcf287bf`、开发机 clean 起点 `50a1c05fcb2df01d5869729b229f412dd934272a`；该起点 CI #4206 / R2 #3822 均 SUCCESS；ASP 和 Real Clingo 两项按分类 SKIPPED。PR #315 仍 Draft，不合并。
- **A = PARTIAL / NOT CLOSED**：`ClocktowerGameSession.state.gameState` 是已确认机械事实的预期权威；Compose `PlayerCard` 仍可被实际游戏操作修改且多处投影独立重建 `GameState`；不要用可能过时的 Session 机械替换。信息公布、具体 Drunk/Red Herring/bluffs 采纳、人工覆盖、Claim 来源及任意时间点完整回放不能宣称完整。首夜 UI 临时门禁不再保存第二份 `AbilityObservation` 内容，但发布与 Session durable observation 是先后两道动作，必须审计中途失败和恢复。需完整单写入接入后方可给 A=YES。
- **B = YES（当前外发源码合同）**：首夜 `InitialJointOpeningDirectV2` 不带合法候选菜单；后续 `StorytellerStrategicContinuationV2` 只外发 `currentDecision` 并收 `currentResult.semantic`。内部 `legalCandidates` 和单项 `remainingChoices` 保留用于 Host 验证及现役 UI 采纳，均非模型端菜单。真实付费 API 调用/设备行为未验收。
- **C = YES（确认已退役的旧策略推荐）**：原 SDE 评分/归一化一致性/Shadow 规划不存在生产策略调用。进一步清除首夜 `FirstNightInformationMigration` 的旧/新候选池 Shadow 比对、旧路线回退和专属 `SelectionPoolParityRecorder`/统计，保留单一合法发布、已展示不可覆盖和 Session 独立持久观察；`UnifiedSelectionPool` 的合法性/用户可选状态是手动展示机制，不是模型评分。Recovery 的两个 `SdeHistoricalReplayInput` 文件与 epistemic 世界检验、setup diversity 不能按文件名误删。
- 历史事实外发由 `ActionFact.toString()` / `InformationProposition.toString()` 改为复用 `ClocktowerSemanticHistoryPersistence.encodeActionFact` 与 `EpistemicSemanticJson.encode` 的稳定结构化 JSON，避免让 Kotlin 调试字符串成为跨层历史协议。仍需补齐真正缺失的生产事实事件源。
- 本轮针对旧 Shadow 的测试改为首夜已选语义发布、候选有效性、重开幂等；保留角色合法性和 Recluse 登记见证测试。进一步查出仅供旧候选策略特征的 `Sde2D5FExpertObservedPolicyFeatures.kt`、`Sde2D5FExpertObservedPolicyFeatureReport.kt`，删除两者及同一实验类的专属 policy-features 测试；保留有独立价值的真人观察实证、注册见证与精确世界拓扑实验，其本质不是旧效用/评分推荐。代码 HEAD `7ffe236d` CI #4207（Android FAST / verify-boundary）与 R2 #3823 均 SUCCESS；完整 Android 构建、ASP、Real Clingo 当次 SKIPPED。此处再删除特征后须复验新 HEAD。真机 MANUAL/AI 辅助/自动、Recovery 的验收状态以最新 SHA 实证为准。**严禁将本批标记 CLOSED，直至 A、B、C 全部真正通过**。

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

### 第八批：旧 SDE 评分 / 特征 / 一致性推荐框架完全退役（2026-10-16）

按照用户最新明确的范围，沿真实 **production entrypoint → SDE module → tests** 依赖闭包审计，确认没有生产调用者使用旧 SDE 分值、NormalizedStrategicDiagnostics、效用、信息锁定/一致性特征或 Shadow 来挑选推荐。由此删除旧 SDE **29 个主源码文件**，包括 `RecommendationDecisionExportV1`、`StorytellerDecisionEngine`、`DecisionFeatures` / `DecisionFeatureEvaluation`、`DrunkAssignmentFeatures`、`NormalizedStrategicDiagnostics`、`HealthyInformationUtilityFeatures`、全部相关历史 feature projector、Drunk/Structured Shadow 适配器和旧候选计划引用；同步删除这些旧链路的独立单元测试以及试验性 SDE-2D5 标准/B4F SILVER 校准。撤销已无类可运行的两个 Gradle 专用任务 `sde2D5Calibration`、`sde2D5FB4FSilverGeneralization`，修订现行 `TESTING_STRATEGY.md`。

此外确认旧 providerPayload 不在生产实际请求路径，单独删除并保留真实 Host 内部合法域哈希与校验。

**不删除** 仍被 App 捕捉/Recovery 读写的 `SdeHistoricalReplayInput.kt`、`SdeHistoricalReplayInputJsonCodec.kt`。首轮 CI #4197 揭示了三个只供旧 SDE 实验使用的红鲱鱼类型适配器的残留依赖；进一步审计确认无应用生产调用者，已经一并删除 `RedHerringSetupPrecommitAdapter.kt`、`RedHerringCommittedInputBindingAdapter.kt`、`SdeDecisionCandidate.kt` 及其专属测试。真正的 Host 红鲱鱼合法域仍在 `HostOpeningRulesDomainV2` 等规则入口，不受影响。该 SDE 目录现仅保留 2 个 Recovery 历史重放文件。其他 Host 合法域、真实登记见证、Epistemic 世界与因果时序一致性不是旧评分算法，必须保持。这一批须通过后续精确 HEAD Android CI/R2 才算通过验证；不能只因代码搜索归零就宣称编译成功。

### 第九批：对外候选菜单退役及单项语义请求（2026-10-16）

- `InitialJointOpeningDirectV2` 的模型请求删除 `redHerringAllowedSeats`、`legalDemonBluffRoleIds`、`subjectModes`、`observationKinds`，Host 原本的完整合法分支、候选、红鲱鱼/恶魔掩饰和登记见证仍在内存中供**响应后** `HostOpeningSemanticGateV2` 严格验证。保留条件首夜依赖/等待事件和 Drunk 分支结构，避免提前编造未来玩家行动。提交 `268bc128`。
- `StorytellerStrategicContinuationV2` 在提供给模型的 JSON 中只发送真实当前 `currentDecision`，不发送 `openDecisions[].legalCandidates`。响应只包含 `currentResult.semantic`，模型不返回 `candidateId` 或整包 `remainingChoices`；Host 使用完整私有候选集合匹配语义，匹配不到则拒绝。内部单项 `remainingChoices` 只是现有 UI/采纳代码过渡展示句柄，**不是**外发 JSON；多登记见证同一公开结果时仍由 Host 的 witness-free 展示门槛把关。提交 `eac18827`。
- 非兼容 wire contract 已升级为 `botc.whole-game-strategy-continuation-v3`，OpenAI Responses strict format 为 `botc_whole_game_strategic_continuation_v3`；真实旧 V2 model JSON 不再接受。提交 `63b5d111`，其 Android CI #4202 / R2 #3818 SUCCESS；以上三项均无付费 API 实测，真机尚未通过。
- Session 单状态工作采取实证式增量收口：现役标量信息的 `pendingGlobalNightScalarDecision` 通过当前 Session `gameState` 获取角色座位、占卜师已确认查询的目标座位，并对照 UI 卡牌名单/顺序，发生漂移时 fail-closed；不引入新 GameState，不误把 UI 草稿当作事实。提交 `ef3438c`，最终 Android CI/R2 需另验。
- **第十批（`4cb657e`）**：移除首夜模型输入的 `conditionalDecisionContracts` 完整合法酒鬼座位分支枚举。保留 `decisionInventory.information`、`decisionInventory.events`、`state.unknownEvents`，让模型联合选择当前 Drunk / 红鲱鱼 / bluffs / 信息；原始 `HostOpeningRulesDomainV2` 仍对每个分支保留 Host 独立规则验证，错误分支/未来行动信息 fail-closed。源测试确认输入不再出现 `drunkSeat` 或 `conditionalDecisionContracts` 之类候选菜单，Host 原内部合法分支数保持不变。Android CI #4205 / R2 #3821 需要新 HEAD 结果；无真实付费 API 策略质量证明。
- 当前事实历史与因果回放是否覆盖**全部提名、逐人投票、每个已公布线索**，仍需再验，不把“目前已转发存在的事件”当作全量性证明。

### 最新产品目标实况（审计，不是完工声明）

1. **游戏对象唯一可写权威：未达成**。Session 的 `GameState` 和 Compose 的 `PlayerCard` 仍分别可以写入，游戏动作有多处从实时 cards 生成 `GameState`。TBGS/GameSnapshot 本身是只读投影，保留合理，但写入所有权尚未完全统一。
2. **活跃个人直连模型请求不再附合法候选菜单：源码已达成、真实 API 未验收**。首夜不再发送任何合法座位、身份、合法信息类型或酒鬼分支菜单；只发送展示阵容、逐项待决信息和实际未知事件标识，由模型一次性联合规划。续接 V3 只发送当前一项决策的语义请求和响应。Host 用内部完整合法域与当前游标后验校验，内部候选句柄不外发；原 `InitialGlobalOpeningScopeV1.providerPayload` 已删除。
3. **旧评分 / 一致性启发式推荐代码：SDE 子模块已按调用图清除**；任何剩余的世界一致性/合法性/注册见证及实际历史校验仍属 Host 正确性，不能为了满足命名字面要求删除。
4. **返回结果的 Host 后验合法性校验：保留**。不因停止发送合法备选而取消 Host 拒绝非法/过时/歧义结果的能力。

最终验收必须由以上真实消费者的生产重构、回归证明及独立 CI/R2 完成，不以“删除了大量文件”替代。

## 3. 审计结论：尚未清理的生产调用链

原 P1 #304/#306/#307/#308 的联合开局、Host 审核和 Responses 续接仍有生产消费者，均已纳入 Draft #315。**模型外发整包 `remainingChoices` 已被 V3 唯一语义结果替换**；但 Host 内部因旧展示/采纳代码保留同名 Map 句柄，必须在实际 UI 验收后再选择是否进一步重命名或删除。

逐个审计了 `toClocktowerGameState()` 的全部生产调用：Session 创建前、Recovery 重建、夜晚/破晓裁决、首夜信息请求及 Day table 的 UI 卡牌输入都是实际消费者；**现阶段没有证据确保 Session 中的 GameState 比实时传入的 PlayerCard 更新**。因此绝不通过机械地替换为 `session.state.gameState` 来减少调用，而冒险引用旧角色或存活状态。Day table 同一轮渲染中 6 处重复卡牌转换可考虑在同一作用域内只计算一次，但不得跨 Compose 输入变化缓存。整个系统要真正单状态，需要先界定 UI 临时编辑与 Session 确认事实的写入边界。

Recovery 与 Game Archive 具有不同合同：前者供继续游戏，后者供复盘，两者已经复用 `AppGameStateJsonCodec` 的低层卡牌/事件/结果映射。**不应把不同用途的归档结构硬合并**。

部分名字含 `sde`、`candidate`、`scoring`、`shadow` 的模块可能仅供历史实验，也可能提供仍活跃的规则候选/手动展示或因果重放。应先做 **生产消费者闭包** 审计，再成组删除，不能按文件名批量删除。


## 3.1 PR 依赖正式收敛（2026-10-15）

本轮确认原 P1 四个 Draft 的 HEAD 严格在 #315 祖先链内：#304 `df058ee5`、#306 `d0e704c9`、#307 `df0f701d`、#308 `486f2734`。GitHub 对 #315 改 base 为 `main` 后给出 `mergeable: true` 与 `merge state: clean`，**没有发生 main 合并**；#304/#306/#307/#308 都关闭为未合并，保留分支与审计证据。开放 PR 仅 #315。

统一候选中的真实 P1 功能必须保留：同次联合酒鬼/信息/红鲱鱼/掩饰规划、Host 确认、`store:true` 与已采纳响应的 `previous_response_id` 续接仍有调用。续接模型输出已按 V3 更改为当前唯一语义；内部 `remainingChoices` 映射暂时用于当前 Host UI，而非模型端协议。

**统一候选进入 main 的验收条件**：

- 证明精确最后代码 HEAD 的 Android 构建/单测、Host 合法性、规则验证、R2 和跨项目 TBGS/Recovery 相关合同无失败。第七批代码 HEAD `f4c9468643e7` 的 GitHub CI #4194（Android 与 Real Clingo SUCCESS，ASP SKIPPED）和 R2 #3810 已通过；之后仅变更文档须分别记录 Android 被跳过的事实。
- 校验合并前 PR 的 base/head、diff、未解决审阅和 conflict；尤其检查 `main` 后续是否变化。不能将旧 stacked PR 全关闭等同于运行时验收。
- **真机未完成**：8 人 TB setup 手动无 AI 路径；AI_ASSISTED 联合酒鬼/红鲱鱼/掩饰一键采纳；AI_AUTOMATIC 仅在 Host 合法后私有预采纳；Chef 点击信息后不退出（FIELD-1）、首夜战略 schema 合法（FIELD-2）、首夜/次夜至少一个真实事件与人工覆盖、因果记录、恢复流程。实机请求须用户单独批准真实付费 API 预算；不在清理过程中擅自收费。
- 真机验证前仍是 Draft，不宣称全自动说书人已经达成，也不启动 P2 新功能。即使完成源码结构收口，任何无法通过 CI 覆盖的实机风险继续独立列出。

## 4. 未收口 / 待验证

1. #315 的旧算法清理 HEAD `0e563911` Android CI #4199 / R2 #3815 SUCCESS；V3 wire HEAD `63b5d111` Android CI #4202 / R2 #3818 SUCCESS；最新 Session 座位收口 HEAD `ef3438c` 仍待 CI/R2。自动化不能代替真实 Android Host/Recovery 的设备端验收。最终归并 HEAD 再跑相应检查；本地构建机 SSH 故障单独处理，不把构建机故障记作应用错误。
2. 第八批旧 SDE 分值/特征/Shadow 与实验校准已按实际无外部生产调用者删除；需 Android CI/R2 验证，仍保留 Recovery replay 与 Host 合法域。另需核查非 SDE 的历史实验与模型请求旧序列化路径。
3. 逐项核实当前权威状态转换点、Recovery 和 Archive 的真实边界与跨项目 TBGS 语义。
4. **已关闭 #310–#314（全部未合并）**：#310 的 Host 单项门控值得复用，但现有代码让模型输出 candidateId；#311 的事件增量使用已废弃的 `ClocktowerSessionState.decisionHistory`、`StorytellerDecisionEvent` 与 `DecisionCorrectionEvent`，当前 #315 已移除这套空档案，不能直接 cherry-pick。#312 的模型合同过时；#313 筛掉合法提名/逐人投票与完整历史不符；#314 的原始审计文档已引入本清理分支。可迁移的是 Host 单项合法性、前缀单调性和私有缓存原则，不是旧接口。已核对 1E 分支：独立 `StorytellerJitDirectResultV2` 对结果数字/真假/Pair/角色/座位执行严格结构化 JSON 解码、模型不接收 candidate ID 且明确 Host 验证为后置；`HostAiDecisionRelevantFactsV2` 对所有合法提名、逐人投票和阶段变化均保留。**但它们尚未接入实际 API/UI**，后者调用的 `HostCausalEventDeltaV2` 仍依赖 #311 已删除的空档案，故**绝不整体合并 1E**；仅标记两项可单独复用的设计/测试素材。关闭的 PR 远端分支暂不删除，以防依赖无法追踪。
5. 适度整合活动路线与交接文档，保留必要历史审计，但不再将旧项目计划列为当下 NEXT。

## 5. AI-EVENT-JIT 恢复门槛（本轮结束后）

只有在确切 PR HEAD CI/R2 和规则/手动/恢复重要路径证明通过、没有误删有效历史生产者、跨项目协议保留、待处理 PR 明确关闭或归并、工作树 clean，且主线规范与 handoff 同步后，才将本任务标记为 CLOSED。之后恢复“首轮整局战略、后续唯一单项语义结果、全量结构化事实增量及 Host 验证”的主路线；**不是**复活旧的候选 ID/整包 `remainingChoices` 模型输出。
