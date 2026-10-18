# 当前开发路线（2026-10-18）

## F9 当前交接：四模块候选解耦，完整 CI/R2 成功（覆盖下方历史状态）

- Pair 真值/见证/合法候选生成、Numeric/Boolean Foundation/Coordinator/适配与重复确认、Drunk 假设投影/候选确认、后夜语义回匹配候选 ID 已整链退出；整删 19 生产 + 20 专属测试，源码净删 5289 行。真实首夜/后夜 AI transport/提示词/响应、战略记忆、AI_ASSISTED 预选和 AI_AUTOMATIC 原发布回调保留；建议不写事实。
- setup 接受任意已发牌 Drunk 座位、已有邪恶红鲱鱼座位及已在场 bluff；只验证结构身份/去重/时序。直接数字接受非负 Int，不受旧 gameplay range 约束。Session 保留单 writer、私密 recipient、当前 revision 与实际确认边界。
- 本地完整 `:app:testFull :app:assembleDebug` SUCCESS：898 项，0 失败/错误/跳过。代码 `4113974d840f1c201d6b9aa7686193435c710f9b` 的完整 Android **CI #4363 / R2 #3943 SUCCESS**，ASP/真实 Clingo/CI gate 成功；后续 docs-only HEAD 不是新的编译证明。实际 Git tree 936 文件，263 生产 + 209 测试 Kotlin。NightStepUi 49650B，余超限 App 180482B、HostScreen 144543B。
- 下一批优先审计 HostScreen/InformationStepBuilder 的真值/登记预览、ResultFirstRegistrationDomain/FixedInformationEvaluator、Mayor/NewDemon 等行动候选与旧 Provider 通用候选合同；确认消费者后继续整组删除。四块已取得实质进展，不等于全树或所有规则已清完。完整清单、消费证据、风险只维护全源台账 F9。无新产品决策，无真机/付费 API，Draft #334 不合并/retarget。

唯一详细权威：docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md。**所有者纠偏（2026-10-18）**：仅清除 Host 过度工程化的规则合法性门禁与真值推断；已有 AI 说书人推荐是保留的产品功能，包括首夜整局规划、配对/数字推荐、提示词、API、响应合同和长期战略记忆。B3 意外删除 AI 配对推荐链，应先恢复并验证，再继续 P1；未来 Rules 为独立只读查询，不成为事实提交门禁。Rules 和 AI 均不得写入 Host 事实，人工结果在清理期间不受游戏规则候选限制。

## F8 当前批次：无效首夜预计算整链退出

本地已删除首夜 natural pair 全局预计算缓存/后台调度/等待/重试及重复 snapshot→GameState context，共整删 3 生产 + 2 专属测试；保留 current-step AI 配对请求并直接使用 canonical Session 事实与版本。源码净删 760 行，无新增 Context/状态/算法；本地完整 testFull + Debug APK SUCCESS（973 项，0 失败/错误/跳过），代码 `119de9496e52088a0fde164aeafd24973743bfda` 的完整 Android **CI #4361 / R2 #3941 SUCCESS**，ASP/真实 Clingo/CI gate 全部成功。实际剩余 pair/UI/setup/后夜候选复核另行解耦，P1 未完成，详细证据见全源台账 F8。

## F7 当前执行：先集中整删有把握的部分，再审计剩余

所有者已调整顺序：横向比较规模与消费者证据，优先清除可完整退出的大块/死代码组，随后再对剩余混合文件逐项审计。F7 已在本地整删 11 个无生产消费者的旧规则/评分/重复状态/传输/界面及兼容文件、5 个专属测试、3 条 F5 遗留的失效实验工作流；代码/配置净删 917 行，保留实际 AI、唯一 Session、事实/隐私及现用 UI。本地 `:app:testFull :app:assembleDebug` SUCCESS（980 项、0 失败/错误/跳过）；代码 `6320a735a9ee13a7c57dcd6a259361d40de591c3` 的完整 Android **CI #4359 / R2 #3939 SUCCESS**，ASP/真实 Clingo/CI gate 均成功；详见唯一全源台账 F7。不再默认从酒鬼链逐函数推进，P1 仍未完成。

## F6 当前本地批次：首夜候选域已删除，完整 CI/R2 已通过

Windows 本地直接开发，已删除首夜 HostOpeningRulesDomainV2 / HostOpeningSemanticGateV2 / InitialGlobalOpeningScopeV1 和失去生产消费者的 Numeric/Setup 生成器（5 个生产、4 个专属测试文件）。保留真实首夜请求/响应、结构化预选内容、战略记忆及异步安全；当前本地 testFast 与完整 testFull/Debug APK 通过，代码 `f3fae3f2e5bfde9083c919f9a5ae34f4e10fb90c` 的完整 Android **CI #4357 / R2 #3937 SUCCESS**，ASP/Clingo/CI gate 均成功。净删 **1596 行代码**，受控树 998 文件，Android 295 生产 + 236 测试 Kotlin；详情和剩余 UI/setup/后夜候选绑定见全源台账 F6。P1 继续进行，不宣称全面移除规则门禁。

## F5 大模块优先（2026-10-18）

用户现行顺序是 **整删旧 web 客户端 → 整组删除 epistemic 非核心世界推演/实验 → 再拆 Host 首夜合法候选引擎**；优先清除大型无产品价值模块，不再沿旧路径逐函数减法。已删除 web/ 全部 8 文件（GitHub 二进制提交 `5be8d37b` + Mini MCP `bb96b0c5`）；已删除 epistemic 旧世界引擎生产 26 文件、独占测试 48 文件、review 17 个测试/3 个 manifest、推荐实验 2 个生产与 3 个测试、Gradle 旧实验任务。保留 9 个承担真实事件/私密观察/序列化/公共历史安全的 epistemic 生产文件和 12 个验证文件。**已验收**：F5 实验与引擎整删代码提交 `3be99bbc40a099a18c8f1bfc13b1a5d7ed978bc7`，103 文件 +27/-17731，净删 **17,704 行**；GitHub 完整 Android **CI #4355 SUCCESS**（单测/Debug APK），**R2 #3935 SUCCESS**，ASP/Clingo/CI gate SUCCESS。另有 web/ 全部 8 文件由 `5be8d37b` / `bb96b0c5` 提交。完整树 1112→1005 文件，Android 生产 326→298 Kotlin、测试 308→240；旧网页源码超限已消失，余三个超 50,000B Android 源码。详见唯一 `MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`。

## 严格阶段顺序

| 阶段 | 任务 | 状态 |
|---|---|---|
| R0 | 恢复 B3 误伤的 AI 配对推荐、Provider/残余范围/方桌预选，同时清除无消费的 Host 规则确认接口 | **已完成：代码 CI #4342 / R2 #3922 SUCCESS** |
| P1 | 删除 Host 旧规则合法性否决、真值/登记见证、重复投影和专属测试；**先保持 AI 真实生产行为，再拆除旧候选适配，不把 Host 候选当成 AI 产品需求** | **F5 已验收；F6 首夜候选域已删并验收，UI/setup/后夜残留待清理** |
| P2 | 仅清理已证明无 AI 生产消费者的旧实验/重复算法；禁止整包删除 AI 推荐、提示词、API、联合规划与战略记忆 | 待 P1 |
| P3 | 收敛唯一 GameState/Session 事实及结构化事件记录，最小继续游戏、通用合同 | 待 P2 |
| P4 | 净删后的 UI 和源码职责拆分；全部源文件 <=50,000 bytes | 待 P3 |
| P5 | 全树验收、跨项目数据合同；规划独立只读 Rules 与**保留中的 AI**的咨询接口 | 待 P4 |

以真实生产消费者审计为依据，有把握的整条删除；不确定的给用户一份简短的保留/删除风险报告，倾向先删、以后按正确边界重建。允许旧 Host 自动规则门禁退休，但**不得主动停用有效 AI 推荐**，**必须保留 MANUAL 最小游戏操作、真实已确认事实、安全和隐私**。每批报告净删除、源文件超限、唯一事实风险和真实 CI/R2。

## 全树优先（2026-10-18 再次确认）

实施策略由 B1–B4 的“沿旧调用路径小批清理”切换为**Git 全树横向遍历，按最小 Host / 现有 AI / 旧 Host Rules / 实验冗余 / 待审核产品范围逐模块分类**。分类与证据更新在已有 `docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`（沿用旧文件，不并建台账）。`REMOVE_CONFIRMED` 可带专属测试直接整删；`REMOVE_AFTER_DISCONNECT` 列完整消费者并在断开后整删；AI 生产依赖明确 `KEEP_AI`，不因目录有 Rules/推荐/旧版本名而删。现行全树 1135 文件，按较宽代码/配置扩展名口径 723 个、Android 337 主 Kotlin + 320 测试 Kotlin，当前本次 F1 以前的 Git HEAD 8c1a4dc1。

F1 首批横向筛选确认 `DecisionSeedFactory` 与 `TroubleBrewingSetupRecommendationDecisionContext` 均仅有自身和专属测试调用，无实际 AI/Host 生产消费者；四文件（生产两件、专属测试两件）整删，净删 **478 行代码**（210 生产、268 测试）。代码及台账 HEAD `24995d37041bdf24a5bd6ad8da50a51ad693c8b4`，GitHub **完整 Android CI #4346 SUCCESS**（单测+Debug APK）、**R2 #3926 SUCCESS**，ASP、Clingo 和 CI gate 成功；PR #334 Draft 仍堆叠，未做真机/收费 API。后续按**全树分类**集中安排删除，不再用旧调用链决定审计顺序；未知与混合文件继续核查，不虚称全部源码已语义审核。

F2 横向筛选到 `epistemic/B4DynamicPlayerWorldSetShadow` 的孤立测试影子接口：无 App/AI 生产消费者，整删接口与两份独占实验测试；两份混合时间线/Exact 测试脱离旧 B4 包装，继续核验共享记录和 Exact 评估不变量。A4 ZDD 预热有实际 App 消费，本轮暂保留且标记断开后可删。F2 代码提交 `bdf5dbcdccdb9ad7024580a2283ad4d704f496a6`，完整 Android **CI #4348 SUCCESS**、**R2 #3928 SUCCESS**，ASP/Clingo/CI gate 成功；整删生产 180 行、专属测试 391 行，混合测试另净删 26 行。

F3 基于全树模块分类进一步确认：A4 身份展示预热和观察缓存重建仅在 App Debug TB 5 人诊断中消费，不参与现有 AI 推荐。已从 App 消除整个影子缓存调度/失效/日志链，并连同五个 A4 生产源码和五个独占实验测试整删；真实 Session 日志、事实提交及 AI 规划代码未动。A4/ZDD 通用推演基底暂由其他测试使用，留待后续生产消费者审计。F3 代码提交 `6ea36666ceb0b2d3d7a919a25c2e0b04841f7dce`：14 文件，生产净删 **977 行**（App -187 + 5 服务 -790）、旧测试净删 **735 行**，合计净删 **1712 行代码**（文档 +22/-6），完整 Android **CI #4349 SUCCESS** / **R2 #3929 SUCCESS**，ASP/Clingo/CI gate 全部成功。F2+F3 连续净删 **2309 行代码**；P1 全树审计未完成，AI 保留不变。

F4 用户再确认：AI 直接根据 Host 的真实游戏状态与已确认事件规划，不需要 Host 枚举候选，也不应被旧 Host 游戏合法域否决。代码现状与目标有差距：`InitialJointOpeningDirectV2.encode` 已不传合法候选列表，但 `decode` 仍依赖 `HostOpeningSemanticGateV2.accept` 和 `HostOpeningRulesDomainV2` 对推荐进行合法域复核；列为 `REMOVE_AFTER_DISCONNECT`，不再误称为长期 AI 依赖。F4 已删除无生产消费者的 A4 世界集合 runtime、ZDD 原型、rollout policy 及 3 份专属测试；保留正式结构/时间线/枚举金标准测试，清理 ZDD 双轨和 Gradle 排除。F4 代码 SHA `52ef98ee4c406fc49af21e1592a954da7e4246ff`，6 个整文件删除（3 生产 740 行，3 专属测试 477 行）；另缩减混合测试净 78 行与 Gradle 1 行，**代码净删 1296 行**。15 文件总改动 +27/-1309，净删 1282 行（含文档）。GitHub 完整 Android **CI #4351 SUCCESS** / **R2 #3931 SUCCESS**，ASP/Clingo/CI gate 均成功。新完整 Git tree 1112 文件，宽口径代码/配置 700，Android 326 生产 + 308 测试 Kotlin；四个 >50000B 源文件未变。
## P1 实施记录（只记录真实提交）

- **B4（2026-10-18，代码 HEAD `3ad502ac34cb3e93a2a1c843752e8c3b63d93f54`）**：审计 DayAbilityRegistrationRulingProducerV1（App 白天 Slayer/Virgin → StorytellerCausalDecisionJournalV1）并移除它对 TroubleBrewingRegistrationDomain 的重复合法性查询，以及对健康/存活、Virgin 公开执行结果等的规则否决；显式登记直接构建事实，不从旧合法域反推。移除 App 和测试中的无用 legalRoles 参数。保留主体身份、座位、DAY 阶段、快照版本、决定 ID 去重、事件字段/登记事实完整性；清理旧规则拒绝测试，保留事实重放与防篡改测试。AI 的 TroubleBrewingRegistrationDomain/NaturalPairInformationCandidateGenerator、HostOpeningRulesDomainV2、InitialJointOpeningDirectV2、Provider、提示词和续规划保持原生产调用。P1 尚未完成：HostScreen/Night 的其他门禁待清理。GitHub 对比 6 文件 +73/-150，净删 77 行（生产净删 42、测试净删 44，文档新增 9）。**该代码 SHA 完整 Android CI #4344 SUCCESS**（单测+Debug APK）、**R2 #3924 SUCCESS**，ASP/Clingo/CI gate SUCCESS；尚未执行真机或收费 API。PR #334 继续 Draft 堆叠、不合并。

- **B1 代码提交 `892e0252eb89d9c4bf99f69754f1984fe5bbba98`**，PR #334 Draft 堆叠关系不变。删除旧 `RegistrationInteractionRules` 及专属测试、无消费者的 `TroubleBrewingRecurringPlayerInformationRouteDomain`，以及厨师方桌由数值推导登记见证/有效邪恶座位/相邻计数高亮、配对方桌对 Spy/Recluse 登记见证推断勾选。另清除旧步骤构建器中依可靠性过滤登记键的调用。初始净减少 **379 行（+31/-410）**，修复后 B1 合计净减少 **378 行（+32/-410）**，生产源码删除 2 文件、旧测试删除 1 文件，其余按职责精简。
- B1 首个代码 HEAD 的完整 Android CI **#4330**：编译通过，1301 项测试中 1 项旧规则键断言失败；R2 **#3910 SUCCESS**。已根据批准的退役方向修复：夜间信息 Builder 不再开启旧登记见证交互（无自动见证、不可凭信息展示补记登记），修复提交 **`4b1267e8e7d3277577e44c01dd7004cb04da6fb5`**；修复后的完整 Android CI **#4331 SUCCESS**（`testFull` 和 Debug APK 构建），R2 **#3911 SUCCESS**；ASP/Clingo 和 CI gate 均成功。保留原 Session 事实提交、直接数字选择和方桌实际身份提示；未进行真机手动通局验收。
- **B2 代码提交 `4e87bee2adedbe4ad2542e87426b3449d7893a19` 和 `f2869f0fd3965939adc131b55f22ef7d3e25179c`**：洗衣妇/图书管理员/调查员人工选项改为纯结构性角色与座位组合，不再调用 `PairInformationLegalDomain`；首夜确认结果不再经过 `PairInformationDecisionBoundary` 合法候选 ID 或配对见证推断，而由选定的结构化显示事实生成 Session 观察请求；废除 `projectFirstNightPairInformationOptions` 真值/登记推导和选项池按剧本答案过滤。完整删除无人调用的 `HostOpeningResidualEventGateV2` 旧规则复核器，以及 `FirstNightPairInformationProductionSemanticsTest`、`HostOpeningRulesDomainV2Test`；将混合测试精简为结构选项与实际确认保护。**B2 合计 12 文件，+123/-2012，净删除 1889 行**。初次完整 CI #4333 主程序编译成功，1292 项测试中 1 项因旧顺序断言失败，已修正；第二代码提交的完整 Android CI **#4334 SUCCESS**（包含 `:app:testFull :app:assembleDebug`）、R2 **#3914 SUCCESS**，ASP、Clingo、CI gate 均 SUCCESS。
- **B3 代码提交 `d5f21d431ea3334d0bee05ff8e450e08c6ca7661` 和 `39860f40011729f5c5a148a0fc39d5f8e8895450`**：沿旧 `PairInformationDecisionBoundary` 的消费者真正删除生产调用，包括 App 实时残余 AI 请求、残余范围批次、Host UI 旧配对匹配、Provider 的 `fromPairInformation` 与 Matcher 的 `matchPair`。完整删除 `ClocktowerGlobalFirstNightDecision.kt`、`PairInformationDecisionBoundary.kt`、专属旧测试 `PairInformationDecisionBoundaryTest.kt`；另外已实证零生产消费者的 `FirstNightBundleCandidateSpaceAudit.kt` 和它的专属测试一起删除。保留 MANUAL 的结构化配对录入/发布，旧配对 AI 推荐暂停，不生成临时兼容合法域。**B3 修复后 13 文件，+17/-993，净删 976 行**，初次全量 CI #4337 编译失败：独占测试的旧位置参数遗留 `null`，不是 Rules 生产链失败；R2 #3917 SUCCESS。已删除旧位置参数，修复代码提交 `2b751f4d7719a241540516e176349305d18fe8d9`，修复后的完整 Android CI **#4338 SUCCESS**（单测与 Debug APK 构建）、R2 **#3918 SUCCESS**，ASP/Clingo 与 CI gate 均通过；未做真机或收费 API 测试。
- **R0 纠偏与恢复完成**：权威范围修订提交 `838297d9602735eff82dafffbe57f22ef1710fc0`；AI 配对生产调用恢复提交 `0774711c5899a193bffc36d3b2f376f8bfaf99b6`，完整 Android CI **#4341 SUCCESS** / R2 **#3921 SUCCESS**。随后在保留 AI provider 候选请求的前提下，移除旧 `PendingPairInformationDecision.confirm` 规则确认/观察生成接口和相关死 DTO，新增 AI 配对候选匹配结构化方桌选项、人工公布不依赖 AI 候选的回归测试；代码提交 `e60a3ef9d6b7a3598ddfc59451ba1d84fb3644bb`，完整 Android CI **#4342 SUCCESS**、R2 **#3922 SUCCESS**，ASP/Clingo/CI gate SUCCESS。未执行真机与收费 API，因此验收是源码/模拟数据/CI 层，而非真实模型响应质量验收。
- **P1 仍未完成**：需继续识别 Host 规则门禁的生产消费者；Pair/Numeric 候选计算若供首夜 AI/后夜 AI 使用必须先保留，否则无法恢复 AI。`HostOpeningRulesDomainV2` 是首夜联合 AI 输入，不得作为无用 Host 门禁直接删除。后续批次复核 CI/R2 旧源码守卫。

## 当前历史基线（不是 P1 完成证明）

GitHub 堆叠 Draft PR #330→#334 未合并。开发机已从旧 `codex/minimal-host-reset` 显式切换到 #334 正确分支 `codex/minimal-host-full-scan-20261018`。P1 B1 原始代码提交 `892e0252...`，后续修复代码提交 `4b1267e8...`；当前 HEAD 后续可能被文档提交推进，始终以最新 GitHub 和本地 Git 实测为准。更早 #4325/#3905 是 P1 之前的历史基线，不得当作新 P1 验收。

原全树扫描约 717 个代码类文件，4 个源文件超过 50,000 bytes：CampBoardGameHostApp.kt、ClocktowerHostScreen.kt、ClocktowerNightStepUi.kt、web/app.js。web/ 为旧独立网页客户端，含 Undercover/狼人杀与独立 localStorage；是否整个退休属待用户审核，不应误当成未来独立 WebHost。其余 Host Rules 门禁已获准退休，生产 AI 依赖须先保护再局部清理，不需重复征求方向批准。

当前开发实施入口：docs/NEXT_DEVELOPMENT_HANDOFF.md；全部源码原始索引：docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。旧 M1b/M1c、D5/D6/Recovery/规则维护 NEXT 均为历史，不得继续执行。
