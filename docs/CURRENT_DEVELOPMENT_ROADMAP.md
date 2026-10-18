# 当前开发路线（2026-10-18）

唯一详细权威：docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md。**所有者纠偏（2026-10-18）**：仅清除 Host 过度工程化的规则合法性门禁与真值推断；已有 AI 说书人推荐是保留的产品功能，包括首夜整局规划、配对/数字推荐、提示词、API、响应合同和长期战略记忆。B3 意外删除 AI 配对推荐链，应先恢复并验证，再继续 P1；未来 Rules 为独立只读查询，不成为事实提交门禁。Rules 和 AI 均不得写入 Host 事实，人工结果在清理期间不受游戏规则候选限制。

## 严格阶段顺序

| 阶段 | 任务 | 状态 |
|---|---|---|
| R0 | 恢复 B3 误伤的 AI 配对推荐、Provider/残余范围/方桌预选，同时清除无消费的 Host 规则确认接口 | **已完成：代码 CI #4342 / R2 #3922 SUCCESS** |
| P1 | 删除 Host 旧规则合法性否决、真值/登记见证、重复投影和专属测试；**保留 AI 正在使用的候选生产/适配路径直至安全替换** | **继续实施，严禁误删 AI** |
| P2 | 仅清理已证明无 AI 生产消费者的旧实验/重复算法；禁止整包删除 AI 推荐、提示词、API、联合规划与战略记忆 | 待 P1 |
| P3 | 收敛唯一 GameState/Session 事实及结构化事件记录，最小继续游戏、通用合同 | 待 P2 |
| P4 | 净删后的 UI 和源码职责拆分；全部源文件 <=50,000 bytes | 待 P3 |
| P5 | 全树验收、跨项目数据合同；规划独立只读 Rules 与**保留中的 AI**的咨询接口 | 待 P4 |

以真实生产消费者审计为依据，有把握的整条删除；不确定的给用户一份简短的保留/删除风险报告，倾向先删、以后按正确边界重建。允许旧 Host 自动规则门禁退休，但**不得主动停用有效 AI 推荐**，**必须保留 MANUAL 最小游戏操作、真实已确认事实、安全和隐私**。每批报告净删除、源文件超限、唯一事实风险和真实 CI/R2。

## 全树优先（2026-10-18 再次确认）

实施策略由 B1–B4 的“沿旧调用路径小批清理”切换为**Git 全树横向遍历，按最小 Host / 现有 AI / 旧 Host Rules / 实验冗余 / 待审核产品范围逐模块分类**。分类与证据更新在已有 `docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`（沿用旧文件，不并建台账）。`REMOVE_CONFIRMED` 可带专属测试直接整删；`REMOVE_AFTER_DISCONNECT` 列完整消费者并在断开后整删；AI 生产依赖明确 `KEEP_AI`，不因目录有 Rules/推荐/旧版本名而删。现行全树 1135 文件，按较宽代码/配置扩展名口径 723 个、Android 337 主 Kotlin + 320 测试 Kotlin，当前本次 F1 以前的 Git HEAD 8c1a4dc1。

F1 首批横向筛选确认 `DecisionSeedFactory` 与 `TroubleBrewingSetupRecommendationDecisionContext` 均仅有自身和专属测试调用，无实际 AI/Host 生产消费者；四文件（生产两件、专属测试两件）整删，净删 **478 行代码**（210 生产、268 测试）。代码及台账 HEAD `24995d37041bdf24a5bd6ad8da50a51ad693c8b4`，GitHub **完整 Android CI #4346 SUCCESS**（单测+Debug APK）、**R2 #3926 SUCCESS**，ASP、Clingo 和 CI gate 成功；PR #334 Draft 仍堆叠，未做真机/收费 API。后续按**全树分类**集中安排删除，不再用旧调用链决定审计顺序；未知与混合文件继续核查，不虚称全部源码已语义审核。

F2 横向筛选到 `epistemic/B4DynamicPlayerWorldSetShadow` 的孤立测试影子接口：无 App/AI 生产消费者，整删接口与两份独占实验测试；两份混合时间线/Exact 测试脱离旧 B4 包装，继续核验共享记录和 Exact 评估不变量。A4 ZDD 预热有实际 App 消费，本轮暂保留且标记断开后可删。F2 代码提交 `bdf5dbcdccdb9ad7024580a2283ad4d704f496a6`，完整 Android **CI #4348 SUCCESS**、**R2 #3928 SUCCESS**，ASP/Clingo/CI gate 成功；整删生产 180 行、专属测试 391 行，混合测试另净删 26 行。

F3 基于全树模块分类进一步确认：A4 身份展示预热和观察缓存重建仅在 App Debug TB 5 人诊断中消费，不参与现有 AI 推荐。已从 App 消除整个影子缓存调度/失效/日志链，并连同五个 A4 生产源码和五个独占实验测试整删；真实 Session 日志、事实提交及 AI 规划代码未动。A4/ZDD 通用推演基底暂由其他测试使用，留待后续生产消费者审计。提交 SHA、净删和 CI/R2 待本批真实验证。

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
