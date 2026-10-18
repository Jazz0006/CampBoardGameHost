# 当前开发路线（2026-10-18）

唯一详细权威：docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md。**所有者纠偏（2026-10-18）**：仅清除 Host 过度工程化的规则合法性门禁与真值推断；已有 AI 说书人推荐是保留的产品功能，包括首夜整局规划、配对/数字推荐、提示词、API、响应合同和长期战略记忆。B3 意外删除 AI 配对推荐链，应先恢复并验证，再继续 P1；未来 Rules 为独立只读查询，不成为事实提交门禁。Rules 和 AI 均不得写入 Host 事实，人工结果在清理期间不受游戏规则候选限制。

## 严格阶段顺序

| 阶段 | 任务 | 状态 |
|---|---|---|
| R0 | **优先恢复 B3 误伤的 AI 配对推荐及残余推荐路径，保持 Host 人工发布不受规则门禁** | **立即执行** |
| P1 | 删除 Host 旧规则合法性否决、真值/登记见证、重复投影和专属测试；**保留 AI 正在使用的候选生产/适配路径直至安全替换** | **R0 验收后继续** |
| P2 | 仅清理已证明无 AI 生产消费者的旧实验/重复算法；禁止整包删除 AI 推荐、提示词、API、联合规划与战略记忆 | 待 P1 |
| P3 | 收敛唯一 GameState/Session 事实及结构化事件记录，最小继续游戏、通用合同 | 待 P2 |
| P4 | 净删后的 UI 和源码职责拆分；全部源文件 <=50,000 bytes | 待 P3 |
| P5 | 全树验收、跨项目数据合同；规划独立只读 Rules 与**保留中的 AI**的咨询接口 | 待 P4 |

以真实生产消费者审计为依据，有把握的整条删除；不确定的给用户一份简短的保留/删除风险报告，倾向先删、以后按正确边界重建。允许旧 Host 自动规则门禁退休，但**不得主动停用有效 AI 推荐**，**必须保留 MANUAL 最小游戏操作、真实已确认事实、安全和隐私**。每批报告净删除、源文件超限、唯一事实风险和真实 CI/R2。

## P1 实施记录（只记录真实提交）

- **B1 代码提交 `892e0252eb89d9c4bf99f69754f1984fe5bbba98`**，PR #334 Draft 堆叠关系不变。删除旧 `RegistrationInteractionRules` 及专属测试、无消费者的 `TroubleBrewingRecurringPlayerInformationRouteDomain`，以及厨师方桌由数值推导登记见证/有效邪恶座位/相邻计数高亮、配对方桌对 Spy/Recluse 登记见证推断勾选。另清除旧步骤构建器中依可靠性过滤登记键的调用。初始净减少 **379 行（+31/-410）**，修复后 B1 合计净减少 **378 行（+32/-410）**，生产源码删除 2 文件、旧测试删除 1 文件，其余按职责精简。
- B1 首个代码 HEAD 的完整 Android CI **#4330**：编译通过，1301 项测试中 1 项旧规则键断言失败；R2 **#3910 SUCCESS**。已根据批准的退役方向修复：夜间信息 Builder 不再开启旧登记见证交互（无自动见证、不可凭信息展示补记登记），修复提交 **`4b1267e8e7d3277577e44c01dd7004cb04da6fb5`**；修复后的完整 Android CI **#4331 SUCCESS**（`testFull` 和 Debug APK 构建），R2 **#3911 SUCCESS**；ASP/Clingo 和 CI gate 均成功。保留原 Session 事实提交、直接数字选择和方桌实际身份提示；未进行真机手动通局验收。
- **B2 代码提交 `4e87bee2adedbe4ad2542e87426b3449d7893a19` 和 `f2869f0fd3965939adc131b55f22ef7d3e25179c`**：洗衣妇/图书管理员/调查员人工选项改为纯结构性角色与座位组合，不再调用 `PairInformationLegalDomain`；首夜确认结果不再经过 `PairInformationDecisionBoundary` 合法候选 ID 或配对见证推断，而由选定的结构化显示事实生成 Session 观察请求；废除 `projectFirstNightPairInformationOptions` 真值/登记推导和选项池按剧本答案过滤。完整删除无人调用的 `HostOpeningResidualEventGateV2` 旧规则复核器，以及 `FirstNightPairInformationProductionSemanticsTest`、`HostOpeningRulesDomainV2Test`；将混合测试精简为结构选项与实际确认保护。**B2 合计 12 文件，+123/-2012，净删除 1889 行**。初次完整 CI #4333 主程序编译成功，1292 项测试中 1 项因旧顺序断言失败，已修正；第二代码提交的完整 Android CI **#4334 SUCCESS**（包含 `:app:testFull :app:assembleDebug`）、R2 **#3914 SUCCESS**，ASP、Clingo、CI gate 均 SUCCESS。
- **B3 代码提交 `d5f21d431ea3334d0bee05ff8e450e08c6ca7661` 和 `39860f40011729f5c5a148a0fc39d5f8e8895450`**：沿旧 `PairInformationDecisionBoundary` 的消费者真正删除生产调用，包括 App 实时残余 AI 请求、残余范围批次、Host UI 旧配对匹配、Provider 的 `fromPairInformation` 与 Matcher 的 `matchPair`。完整删除 `ClocktowerGlobalFirstNightDecision.kt`、`PairInformationDecisionBoundary.kt`、专属旧测试 `PairInformationDecisionBoundaryTest.kt`；另外已实证零生产消费者的 `FirstNightBundleCandidateSpaceAudit.kt` 和它的专属测试一起删除。保留 MANUAL 的结构化配对录入/发布，旧配对 AI 推荐暂停，不生成临时兼容合法域。**B3 修复后 13 文件，+17/-993，净删 976 行**，初次全量 CI #4337 编译失败：独占测试的旧位置参数遗留 `null`，不是 Rules 生产链失败；R2 #3917 SUCCESS。已删除旧位置参数，修复代码提交 `2b751f4d7719a241540516e176349305d18fe8d9`，修复后的完整 Android CI **#4338 SUCCESS**（单测与 Debug APK 构建）、R2 **#3918 SUCCESS**，ASP/Clingo 与 CI gate 均通过；未做真机或收费 API 测试。
- **P1 仍未完成**：需继续识别 Host 规则门禁的生产消费者；Pair/Numeric 候选计算若供首夜 AI/后夜 AI 使用必须先保留，否则无法恢复 AI。`HostOpeningRulesDomainV2` 是首夜联合 AI 输入，不得作为无用 Host 门禁直接删除。后续批次复核 CI/R2 旧源码守卫。

## 当前历史基线（不是 P1 完成证明）

GitHub 堆叠 Draft PR #330→#334 未合并。开发机已从旧 `codex/minimal-host-reset` 显式切换到 #334 正确分支 `codex/minimal-host-full-scan-20261018`。P1 B1 原始代码提交 `892e0252...`，后续修复代码提交 `4b1267e8...`；当前 HEAD 后续可能被文档提交推进，始终以最新 GitHub 和本地 Git 实测为准。更早 #4325/#3905 是 P1 之前的历史基线，不得当作新 P1 验收。

原全树扫描约 717 个代码类文件，4 个源文件超过 50,000 bytes：CampBoardGameHostApp.kt、ClocktowerHostScreen.kt、ClocktowerNightStepUi.kt、web/app.js。web/ 为旧独立网页客户端，含 Undercover/狼人杀与独立 localStorage；是否整个退休属待用户审核，不应误当成未来独立 WebHost。其余 Host Rules 门禁已获准退休，生产 AI 依赖须先保护再局部清理，不需重复征求方向批准。

当前开发实施入口：docs/NEXT_DEVELOPMENT_HANDOFF.md；全部源码原始索引：docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。旧 M1b/M1c、D5/D6/Recovery/规则维护 NEXT 均为历史，不得继续执行。
