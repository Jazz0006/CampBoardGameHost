# 当前唯一 NEXT：全树遍历优先、按职责直接清除（2026-10-18）

**用户再次确认：不要再以旧代码调用栈为主线逐段探索。** 优先枚举当前 Git HEAD 的全部源码模块，按 Host Core、现有 AI、旧 Host Rules、无生产消费者的实验、需要产品决策的外部客户端五类做证据分类。审计及确认/标记删除均记录到现有 `docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`，不复制第二份全树文件索引。已确认无效的整块实现与专属测试直接删除并独立验证；仅有风险或实际消费者的标记 `REMOVE_AFTER_DISCONNECT`，先保护 AI 生产调用。P1 持续清除 Host 规则门禁，不重写 AI。

## F1 当前进展（2026-10-18）

- Git 树 SHA `8c1a4dc114060806c33d5f78102b6b3c6457cf71`：1135 文件；含 Kotlin/DSL、Java、JS/TS、HTML/CSS、Python、Shell、XML、YAML、JSON 的宽口径源码/配置 723 文件；Android 337 生产 Kotlin、320 测试 Kotlin（当前删除前基线）。不同统计扩展名口径不要直接比较。
- `clocktower/recommendation/DecisionSeedFactory.kt`、`TroubleBrewingSetupRecommendationDecisionContext.kt` 的唯一消费者均为各自专属测试；已直接删除这 2 个生产文件和对应 2 个独占测试。保留真正首夜 AI `HostOpeningRulesDomainV2`、`InitialJointOpeningDirectV2`、Provider、配对候选、战略记忆及请求合同。
- A4/B4 世界影子、FN-BUNDLE 旧实验、HostScreen/UI 规则门禁等按审计台账标记，尚未完成逐文件全语义核查；不要凭目录名整删。与 web/ 和非 TB 剧本有关的产品范围仍需用户决定。
- 代码提交 SHA、净删行数、GitHub 全量 CI/R2 尚待执行，不得把此前 B4 #4344 当作 F1 验收。


## B4 实施交接（已完成代码及完整 CI/R2 验收）

- 从起始 HEAD 15a27e9cce0a41190b9704c148a4dd3321d1f30e、工作树 Clean、远端一致、PR #334 Draft 开始。
- 实际审计确认：DayAbilityRegistrationRulingProducerV1 只由 App 白天两处（Slayer 对 Recluse 明确裁定、Virgin 首提名的 Spy 明确裁定）及 CausalDecisionJournal 验证消费；旧 TroubleBrewingRegistrationDomain.resolve 的再校验阻止明确确认事实。删除这段 Host 规则校验与规则健康/真值拒绝，保留真实登记身份、座位、时间阶段、版本、重复/篡改/事件合同；注册事实取自显式选择。删除 legalRoles 重复传参、调整专属旧测试。
- **AI 保护**：NaturalPairInformationCandidateGenerator 仍直接依赖 TroubleBrewingRegistrationDomain；FirstNightNumericInformationSemantics、FixedInformationEvaluator 仍被首夜数值/推荐消费；HostOpeningRulesDomainV2/InitialJointOpeningDirectV2 仍用于首夜联合请求；这些、生产 API、提示词、战略记忆、辅助/自动模式及 Provider 未删。
- **验收**：代码 HEAD `3ad502ac34cb3e93a2a1c843752e8c3b63d93f54`（6 文件，+73/-150，净删 77 行，生产净删 42 行）；GitHub 完整 Android CI **#4344 SUCCESS**（测试和 Debug APK），R2 **#3924 SUCCESS**，ASP、Clingo、CI gate SUCCESS。重新按 GitHub 目录元数据核对已知超 50,000B 源码：`CampBoardGameHostApp.kt` 198482B，`ClocktowerHostScreen.kt` 149006B，`ClocktowerNightStepUi.kt` 60237B，`web/app.js` 143503B。没有付费 API/真机测试。
- **未解决**：App/HostScreen/Night 仍有其他旧候选门禁和规则自动结果；本批不宣称 P1 完成。下一批继续以生产消费者为依据清除 Host 人工信息拒绝，尤其避免破坏 AI 提前预选。

**R0 已完成并通过真实完整 CI/R2。继续执行 P1：只删 Host 旧合法性门禁、真值推断、过度工程及无效专属测试，不删除 AI 推荐的调用、Provider、提示词、战略记忆或必要候选依赖。**

## 权威阅读顺序

AGENTS.md；docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md；docs/CURRENT_DEVELOPMENT_ROADMAP.md；docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md；docs/TESTING_STRATEGY.md；docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。

## 当前远端/开发机基线（先重新核对）

- 仓库：Jazz0006/CampBoardGameHost。开发机 Mini MCP 别名 clocktower。
- PR #330→#334 均为依赖堆叠 Draft。当前 P1 开发分支为 `codex/minimal-host-full-scan-20261018`，已验收的 R0 最新**代码**检查点为 `e60a3ef9d6b7a3598ddfc59451ba1d84fb3644bb`；后续文档 HEAD 不等于代码验收 HEAD；后续文档提交会继续推进 HEAD，开始新批次必须重新核对 GitHub。
- P1 B1 首次 Android CI #4330 测试 1301 项 / 1 项失败（旧登记键）；修复代码 `4b1267e8...` 的完整 Android CI #4331 与 R2 #3911 是本批对应独立检验（最终状态需要重查）。更早 CI #4325/R2 #3905 与 docs-only #4326/R2 #3906 仅属清理前的历史基线。
- 开发机已经 `git_fetch` 后经 Mini MCP `git_track_remote_branch` 从旧 `codex/minimal-host-reset` 切换并同步到正确 #334 分支，且 B1 两次代码提交均已推送。继续时必须再次检查 branch/HEAD/working tree，不能依赖本段历史记录。
- 不自动合并 Draft / 强推 / 删除远端分支。真实付费模型调用没有授权。

## P1 B1 已提交，不等于 P1 完成

- 当前开发分支：`codex/minimal-host-full-scan-20261018`，本批最新代码 HEAD：`4b1267e8e7d3277577e44c01dd7004cb04da6fb5`；Draft PR #334 与原 base 堆叠关系不变。
- B1 删除 `RegistrationInteractionRules.kt`、`TroubleBrewingRecurringPlayerInformationRouteDomain.kt`、`RegistrationInteractionRulesTest.kt`；删除厨师方桌数字→登记见证及座位高亮反推，移除配对方桌 Spy/Recluse 登记勾选推断；调整调用及专属旧测试。GitHub 比较证据（包含修复后相对 P1 初始 `c9204ade...`）：9 个文件变化，+32/-410 行，净删 378 行。
- B1 初次 `[full-ci]` #4330：主程序和测试源码均编译成功，1301 项单元测试中 1 个旧登记键断言失败。R2 #3910 成功。修复提交 `4b1267e8e7d3277577e44c01dd7004cb04da6fb5` 关闭了旧夜间信息 builder 的全部隐式登记键：不复活旧可靠性过滤规则，也不推断未确认的 Spy/Recluse 裁定。最新完整 Android CI #4331 **SUCCESS**（`:app:testFull :app:assembleDebug`）、R2 #3911 **SUCCESS**，ASP/Clingo 和 CI gate 同样成功；这是当前有效的完整代码验收。未进行真机或收费模型调用。
- 保留原 Session 事件 writer、直接数字展示/确认、私密信息与方桌真实身份提示；但旧规则算法仍有生产消费者，**不得宣称人工结果全面不受限制或 P1 已完成**。
- 已按 B1 **代码 HEAD** 从 GitHub 读取并实测 4 个超 50,000B 的源码：`CampBoardGameHostApp.kt` 198706B、`ClocktowerHostScreen.kt` 150032B、`ClocktowerNightStepUi.kt` 60237B、`web/app.js` 143503B。其他文件以完整仓库再扫描为准。

## P1 B2 结果与下一批边界

- B2 提交 `4e87bee2...`、`f2869f0f...`，相对 B1 文档 HEAD `19d67c...`：12 文件，+123/-2012，净删 **1889 行**；整删 `HostOpeningResidualEventGateV2.kt`、`FirstNightPairInformationProductionSemanticsTest.kt`、`HostOpeningRulesDomainV2Test.kt`。
- 人工 Washerwoman/Librarian/Investigator 配对选项现在从脚本角色和座位构建，不经旧 Pair Rules 真值/注册见证筛选；首夜发布使用确认的结构化显示事实，不再使用 Rules 候选 ID 或推导登记。`ClocktowerPairManualAuthority` 已删除旧合法域重算/判定；`ClocktowerHostSelectionSemantics` 删除旧配对真值投影。
- B2 第一次全量 CI #4333 编译通过，1292 个测试中一项旧候选顺序断言失败，已修正；第二个代码 HEAD 的完整 Android CI **#4334 SUCCESS**（`:app:testFull :app:assembleDebug`）、R2 **#3914 SUCCESS** 才是本批有效验收；ASP/Clingo/CI gate 均 SUCCESS。
- B2 HEAD 源码超 50KB 实测：`CampBoardGameHostApp.kt` 198706B，`ClocktowerHostScreen.kt` 149006B，`ClocktowerNightStepUi.kt` 60237B，`web/app.js` 143503B。
- P1 **未完成**：`clocktower/rules` 还保留支持 AI 推荐的候选与部分 Host 规则逻辑；`HostOpeningRulesDomainV2` 是正在使用的首夜 AI 输入，**不得因目录名而删除**。B3 误删的 `PairInformationDecisionBoundary` AI 链先恢复；以后只把 Host 人工提交门禁与 AI 候选提供分开。

## P1 B3 实际代码清理及交接

- B3 两次代码提交 `d5f21d431ea3334d0bee05ff8e450e08c6ca7661`、`39860f40011729f5c5a148a0fc39d5f8e8895450`：旧全局配对 AI 决策创建/Provider 映射/残余集合/UI 推荐匹配整链停止，删去 `ClocktowerGlobalFirstNightDecision.kt`、`clocktower/session/PairInformationDecisionBoundary.kt` 和专属测试。没有新建 Rules 代用品，MANUAL 配对结构输入及 Host 信息记录继续保留。
- 清除零生产消费者的 `clocktower/recommendation/FirstNightBundleCandidateSpaceAudit.kt` 与专属 `FirstNightBundleCandidateSpaceAuditTest.kt`，不再保留旧首夜法域笛卡尔积实验。**B3 修复后相对于 `91663dd34...` 共 13 个文件，+17/-993，净删 976 行，整删 3 个生产源码及 2 个专属测试文件**。
- B3 代码 HEAD `39860f40...` 的 CI #4337 Kotlin 测试编译失败：旧 Scope 测试有一个位置参数 `null`（原 `pairContext` 参数已退出）。R2 #3917 SUCCESS。已直接删除过时调用参数，修复提交 **`2b751f4d7719a241540516e176349305d18fe8d9`**；修复后完整 Android **CI #4338 SUCCESS**（单测+Debug APK）、R2 **#3918 SUCCESS**；ASP/Clingo/CI gate 均 SUCCESS，这是 B3 最新有效完整验收。B3 首个代码 HEAD 对应 CI #4336/R2 #3916，不替代修复验证。
- B3 HEAD 从 GitHub 实测超 50,000B 文件：`CampBoardGameHostApp.kt` **198219B**，`ClocktowerHostScreen.kt` **148322B**，`ClocktowerNightStepUi.kt` **60237B**，`web/app.js` **143503B**；P4 以前不预先拆大文件。
- **R0 已修复 B3 AI 误删。接下来不得直接删除 `HostOpeningRulesDomainV2` / `InitialJointOpeningDirectV2` 或其首夜 AI 消费者。** 继续审计其规则候选只服务 AI 输入还是阻止 Host 事实提交；仅剥离后者。保留 MANUAL 与 AI_ASSISTED/AI_AUTOMATIC、开局/配对/后夜建议、真实事件、私密交付。

## 纠偏后的实施顺序

0. **R0（已通过）**：恢复 B3 前的 AI 配对决策、Provider 请求、全局残余规划与方桌预选；恢复提交 `0774711c...` 完整 CI #4341 / R2 #3921 成功。删除没有生产消费者的 `PendingPairInformationDecision.confirm` 规则确认到 Host 观察转换接口，保留 AI-only 候选，新增推荐匹配与自由手工公布回归测试；修复代码 `e60a3ef9...` 完整 CI #4342 / R2 #3922 成功。未做真实收费 API/真机验收。
1. **P1**：将人工线索/动作的结构与事实记录同旧规则合法性判定拆开，清除 Host 真值、见证、过度合法性门禁和它们的专属测试。AI 正在消费的候选/策略模块可暂时保留并标注迁移边界，不得为净删行数关停 AI。
2. **P2**：只删除确认为废弃的实验、重复层、影子缓存，避免影响现有 AI 推荐。P3–P5 继续遵循修订权威文档。

## P1 下一批具体工作

1. 以完整 Git tree 而非现有局部流程为入口，遍历 clocktower/rules 下全部旧检查模块和外部生产消费者。重点 App/HostScreen/NightStepUi、Setup、Session、Flow、Recommendation、Provider 法域、角色候选 UI、R2 源码守卫及旧规则测试。
2. Host 中旧规则合法性门禁**全部要删除**，不是重构或迁移。先解开消费者，再整链净删。若 Host/UI 要记录某个实际动作或展示值，保留其结构性录入/确认/显示/事件，不以规则合法候选阻拦。移除基于 Spy/Recluse 的见证推导、固定真值、自动挑合法结果、规则阻断和影子推演的辅助代码。
3. 此阶段优先保护基础手动游戏：阵容配置、魔典、隐私展示、玩家行动、投票/死亡、实际信息记录、夜/日阶段推进。旧 Host 自动裁定可以临时退出，但不能使现有 AI 推荐失效，但不能因删除旧功能阻塞正常事实写入。
4. 删除专用旧规则/候选测试和只为旧源码形状存在的 CI 守卫。核心事件顺序、唯一权威、去重、原子写入、座位/身份、私密收件人、过期响应以及必须的交互安全测试应保留。采用 evidence-first，无机械 test-first。
5. 每批逐项报告：已清除的生产消费者与旧源码/测试、净删除量、遗留绑定、当前超 50,000 B 的全部源文件、GitHub 真实 CI/R2。CI 失败查运行日志并修复，不为旧规则测试恢复删除路径。遇到不确定是否属于事实核心的字段，形成简短的用户审核包而不是重新设计整个路线。

## P2→P5 已批准顺序

P2 审计并清除无生产 AI 消费者的旧实验/重复推演（现有 AI 功能一律保留）；P3 一份 Host 状态与关键事件，最小安全续局；P4 精简 UI/所有源码 <=50,000 bytes；P5 全树再审、跨项目数据合同，然后重新规划独立只读 Rules 并对接继续维护的 AI 模块。新规则交互（pair、厨师、共情者合法按钮）等清理完成后再做。

本轮仍未确认是否整包删除仓库旧 web/ 客户端及非 Trouble Brewing 剧本；这些是需要所有者审核的边界，不能因此拖延已明确授权的 Rules 删除。
