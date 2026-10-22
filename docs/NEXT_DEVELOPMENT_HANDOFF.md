# 当前开发交接（2026-10-22，Stage 4 Pair UI 已实施 / 最终验收中）

> **Stage 3 方桌接线复审（2026-10-22）：** 开发分支 `feature/host-numeric-chef-empath-stage3-20261022` / Draft [#348](https://github.com/Jazz0006/CampBoardGameHost/pull/348)。原 `2799930c` 完整 Android CI #4560 / R2 #4122 成功，但复审确认 Chef/Empath 仍由独立数字全屏页面承载，且存在未提交的导航删改。现将 Chef/Empath 合入统一 `ClocktowerNightFullScreenSurface` 方桌分发，复用 `ClocktowerHostSquareTableScaffold`、唯一座位投影及底部 Host/Next，取消数字组件中的重复导航；新增 360×520 共享方桌 Compose 回归；保留 Session `NumericInformationResolver` / AI 当前建议校验 / D2 改选重预览及下一步唯一 Observation，Stage 5 旧文本输入不动。**最终代码 `44fb05c769735826187a66008749432e25368ea4` 的完整 Android CI #4562 / R2 #4124 已全部 SUCCESS，PR 检查 4/4 成功。** 首次 UI 接线代码 CI #4561 因完整对局测试仍点击旧 `Next` 而失败；下一提交将该测试改为共享方桌 `Next →`（钟表匠 Stage 5 路径保留），重新通过全量检查。见 [Stage 3 验收记录](HOST_INFORMATION_STAGE3_ACCEPTANCE_2026-10-22.md)。真机、付费模型未执行；Stage 4/5 不在本批范围。

**当前 Stage 4 交接：** Stage 3 [#348](https://github.com/Jazz0006/CampBoardGameHost/pull/348) 已 squash 合并 main `e241d03b9a7e344f422937cdb6c8419ad091981b`，开发机由此建 `feature/host-pair-information-stage4-20261022`、[Draft #349](https://github.com/Jazz0006/CampBoardGameHost/pull/349)。第一版生产代码 `ff96496a783e5e7ab9635081deeb4e6a9e073afb` 完整 Android CI #4565 / R2 #4126 SUCCESS。已经实装：先选两物理座位、只读当前 Session 的 `PairInformationResolver.resolve` 角色短名单、正常/特殊座位虚线、分来源单列下拉、有效的 Librarian 零外来者、AI 当前 pair 建议拦截、全屏预览与 Next 唯一 Observation；不改 Stage 5 钟表匠/侍女。正在增加 360×520/15 人回归并待最终 CI/R2；真机/付费 API 未执行。详见 [Stage 4 验收记录](HOST_INFORMATION_STAGE4_ACCEPTANCE_2026-10-22.md)。

## 历史 Stage 3 开工交接（完成后以本页顶部状态为准）

## 下一轮必须直接执行（当前权威）

**开发仓库** `Jazz0006/CampBoardGameHost`；Oracle VM Mini MCP alias `clocktower`。**所有先前开放 PR 已完成处置，第三批从最新 main 派生**：Stage 1 [#343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) squash `78d2b115`；Stage 2 [#344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) squash `d3a75c99`；Stage 1–5 权威规划文档通过 [#345](https://github.com/Jazz0006/CampBoardGameHost/pull/345) squash `0ee82171` 集成；酒鬼镇民专用方桌已在新共享 Renderer 上重写，通过 [#346](https://github.com/Jazz0006/CampBoardGameHost/pull/346) squash `b66f8fe3` 合并（Android CI #4551 / R2 #4115 SUCCESS）。原 #341/#342 因被替代关闭；#336 冲突旧拆分单独审计后关闭，未回引旧模块；GitHub **0 个开放 PR**。详见 [PR 整理记录](HOST_PR_CONSOLIDATION_2026-10-22.md)。新一轮须基于**最新 main（至少 `b66f8fe3`，再加本轮 docs-only 提交）**，不再使用旧 Stage1/2 堆叠分支。

**实际代码 / 验收：** Stage 1 `1ea2c918` Android CI #4543 / R2 #4110 SUCCESS；Stage 2 `bb024f42` Android CI #4544 / R2 #4111 SUCCESS。第二批只新增 `clocktower/session/HostInformationResolvers.kt` 和 `HostInformationResolversTest.kt`，支持 Chef `ADJACENT_EVIL_PAIRS`（环形首尾）、Empath `LIVING_EVIL_NEIGHBOURS`（最近存活邻座）及 Washerwoman/Librarian/Investigator 当前 Pair 座位查询；查询时区分信息源 Drunk/poisoned、被检查 Spy/Recluse 特殊登记及是否自身中毒；Washerwoman 划分实际在场与 Session 已确认恶魔伪装、Librarian 有零外来者、NGJ 与 TB 剧本隔离。接口 `NumericInformationResolver.resolve(sessionState, metric, sourceSeat)` 返回 `values`、`subjectSeats`、`sourceAbility`、`relevantSpecialSeats`、`productShortlist`；当前建议核验 `acceptsCurrentAdvice`。**未接入任何生产数字或 Pair 页面，UI 任意文本输入仍在，不能说玩家已经获得唯一 Host 数字。**

**立即实施 Stage 3：厨师 / 共情者统一数字信息 UI（不要只做规划）。**
1. 先读本交接、[Roadmap](CURRENT_DEVELOPMENT_ROADMAP.md)、[测试](TESTING_STRATEGY.md)、[本批验收](HOST_INFORMATION_STAGE2_ACCEPTANCE_2026-10-22.md) 与上述五批合同；审计 `ClocktowerDirectNumericResultUi.kt`、`ClocktowerNightStepUi.kt`、`ClocktowerSessionHost.kt`、`clocktower/ui/ClocktowerHostScreen.kt`、`clocktowerDeterminedNumericResult` 和最新预览/Next/AI 消费者，确认真实的数据如何沿现有 Session 流经页面，不增第二份可写状态。
2. 当前 Session 的 Chef/Empath 查询正常且单一数字时，只显示**一个**可点击的“展示给玩家：N”按钮，不显示原任意数字输入框、额外“预览文本”和冗余列表。当前真实登记裁量或源 Drunk/poisoned 时呈现 Resolver 的数字单选，当前有效 AI 推荐仅高亮，不接纳过期/错 seat/错 metric/错 subjects 的推荐。真正常规唯一答案不得由 AI/人工随意替换；若需记录实际误播，走现有实际展示确认事实边界，不静默伪装为正确答案。
3. 全屏展示是已有私密预览而非立即提交。返回同一步保留上次展示值；改选即使上一 preview 失效，需重新点击展示；本步 Next 才唯一提交最后一条实际展示的 `Observation`，不得跨角色 Previous。三模式 MANUAL / AI_ASSISTED / AI_AUTOMATIC 行为保留，自动模式缺少有效当前建议时等待/重试，不替玩家选择交互对象。
4. 在真实生产调用中切换后，**同批删除** `ClocktowerDirectNumericResultUi.kt` 中旧的任意非负数文本输入和不再使用的双轨计算/测试。保留现有 `clocktowerDirectNumericResult` 的仅用于格式化有效结果的部分（如实际仍有调用），不能误删 NGJ Clockmaker/Chambermaid 仍使用的能力；后者真值计算留 Stage 5。不得在此阶段重建旧 Rules/合法候选/评分/假设世界。
5. 加少量高价值真实 Session/Compose 回归：无特殊登记的唯一答案、健康 Spy/Recluse 才有裁量、源酒鬼/中毒可选数字、不同目标/已更改 Session 后 stale AI 无效、返回改选/重新预览/唯一 Next、360×520 界面与三模式。推送明确 SHA，核实完整 Android CI/R2。真机、物理强退、付费 API 分开报告，付费模型需用户明确同意。

**之后：** Stage 4 Pair UI 座位先选后角色单列分组；Stage 5 Clockmaker/Chambermaid 精确求值及真实设备验收。别提前混进 Stage 3。独立待修 P1：活人可以提名死者的 Day UI；健康 Fortune Teller 固定布尔结果审计。保留唯一 `ClocktowerGameSession/ClocktowerSessionState`、两 builtin 规模与 AI V3/战略记忆/两项跨局防重复、隐私、AtomicFile、D2 显示后 Next 只写一次事件；严禁未授权合并、rebase、force push。

## 2026-10-21 main 审计交接归档（后续以文首为准）

> **旧版 2026-10-22 UI 视觉合同描述（以下为历史分类）**：[HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md](HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md)。方桌稳定；Session Day Light、FirstNight/Night Dark；Session actualAlignment 善蓝恶红；交互仅边框/标记，文字高对比。第一/第二批只修改主题与阵营卡；第三批酒鬼/数字手选回方桌尚未实施。共享 Theme/Seat Renderer 承担唯一实现；提交与 CI 以实际产物为准。

> **最终测试清理合并验收**：#338/#339 均已合并，最新已验证测试代码 main `4b9852cf5828c445b532550115aafbf2ddc8c4bf`，后续 #339 [Android CI #4528](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39604089426) / R2 #4098 成功，保留两项独有 AI 阻断和 5–15 人界面布局真实风险回归。最终静态 124 类/566 个 `@Test`，从 144 类/639 项净减 20 类/73 项。下一步仍应修复确定性信息和死者被提名的实际生产 Bug。下文各分支/旧 HEAD/待 CI 的信息视为审计过程记录。

> **已合并测试清理更新**：PR [#338](https://github.com/Jazz0006/CampBoardGameHost/pull/338) 经 CI #4524/#4525 和 R2 #4097 成功后已 squash merge 到 main `2dcf8a9745f487c3b4a0e30188fc67e3d58c5dfd`。高价值回归优先保留，低中价值的第一轮 22 类/75 项及 1 无用 helper 已删除；静态余 122 类/564 项。下文 `1cfacda8` 是原审计/工作起点、测试结果是历史基线，不是当前 main HEAD。生产 Host 固定信息和死者被提名两个缺陷仍待修正。

## 立即使用的真实基线

- 主线：`main` `6230b5959cf4b64e1c41f08bb48aa66460e13667`（独立审计起点；执行前重新检查远端）。
- #334/#335：已合并，最小整局阶段关闭；#337：已合并，全仓清理阶段关闭；#336：**仍 open Draft，内容与已合并 #337 重叠，不直接合并**。
- 最新经过全量代码验收的检查点：`e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba`， [Android CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) / [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) SUCCESS；Cloud full 639 / 144 suites / 38 UI、Debug APK。main 上的后续文档提交不等于新 Android 代码测试。
- 开发机：Oracle VM 上 Mini MCP 仓库别名 `clocktower`；以实时 `git_state` 和远端为准，目标工作树是干净、与远端一致的 `main`。旧 Cloud / Windows 路径与临时日志都不是当前交接的环境要求。

## 独立审计结果与下一步

1. **先补最小 Host 确定性信息**：`ClocktowerDirectNumericResultUi.kt:26-56` 允许在缺少 Host typed 结果时直接选择任意数值；`clocktower/ui/ClocktowerHostScreen.kt:450-474` 只在已经有匹配 numeric 结果时绕过 AI。对 Chef/Empath 及有固定真值的其他角色，以 Session 唯一事实、能力是否正常、真实查询目标和登记裁量求结果，禁止把“常规唯一结果”留给 AI。Drunk/poisoned 与登记存在裁量时保留选择；人工误播可明确留痕，不能抹掉实际展示。
2. **修复提名目标 UI**：`ClocktowerDayNominationGesture.kt:19-31` 和 `clocktower/ui/ClocktowerDayHostScreen.kt:143-153` 将死者从被提名目标排除；规则允许存活玩家提名死者，只有发起人必须活着。Session `clocktower/session/ClocktowerDayFlow.kt:144-159` 未限制被提名者存活。修正 UI 和真实交互测试，切勿重建独立 Day 状态。
3. **之后有证据地验收**：D2 各角色/三模式、Slayer 目标、强制退出后的真机恢复、AI 提交/重试/过期，实测后才声明通过；所有付费 API 调用需单独明确授权。重点验证新旧调用链替代后同批清掉旧实现。

## 单一对象与不变量

`ClocktowerGameSession` 拥有一个 `ClocktowerSessionState`；全部阶段、轮次、角色、死亡、中毒、当前夜间步骤、白天票数/执行、已确认 ActionFact/Observation、终局由此读取、提交、原子落盘。UI draft、开局前 `ClocktowerOpeningDraft`、静态剧本、`GameSnapshot`（只读快照）、展示卡片和 `ClocktowerAiConversation`（建议/会话记忆）不能独立推进或持久化另一场游戏。D2 全屏预览可以返回修改，只有本步 Next 写唯一最终 Observation；已确认动作立即生效、不得回到上一角色。保留 TB/NGJ、AI 三模式/当前 V3/receipt/战略记忆、两项跨局轮换、隐私/stale/幂等/AtomicFile 的现行代码和行为测试。

## 文档与历史入口

- [路线图](CURRENT_DEVELOPMENT_ROADMAP.md) 是进度和下一步唯一入口；[完整设计](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md) 是产品合同。
- [独立审计](MINIMAL_HOST_POST_MERGE_CODE_AUDIT_2026-10-21.md) 列证据/缺口；[清理验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md) 和 [原路径台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 保留可复核历史，不把旧 Draft/CI/阶段状态当现行命令。
- 只读审计不等于重新跑 CI。禁止任意合并旧 Draft、强推或让文档修改假装业务测试。

## 本轮测试清理（2026-10-21）

批准标准：只留保护可感知产品行为与真实状态安全的高价值测试，中低价值及以前保护错误行为的测试均可删除。本轮从 `1cfacda8` 派生 `cleanup/high-value-test-suite-20261021`，静态删去 22 个测试类、75 个 `@Test` 和无用 helper `A4RuntimeFixtures.kt`，另按测试级清理厨师任意值 `17`、只允许活人作为被提名者的错误断言。静态余 122 类、564 个 `@Test`（正式以精确 SHA CI/R2 实测为准）。保留 TB/NGJ 整局、Session 夜/日规则、原子保存、隐私、D2 唯一 Next、AI 请求/stale/回执/记忆及两项跨局防重复。没有修改生产业务代码，因此 Host 固定线索求值和死者被提名 UI Bug 仍需修复。Oracle VM 上原生 Gradle 的锁与 aarch64 运行库受限，不能冒充为本地测试通过；必须用 GitHub 全量 Android CI/R2 验收新提交。

**合并后复审补充（仅恢复两项高价值）：** #338 的自动代码复审发现两条独有缺失边界：① 真实 Session 当前玩家行动未确认，不得让残余 AI Scope 发明其后续目标与请求；② 360×520 手机竖屏 5–15 人方桌必须全部能完成容量布局。只恢复这两项小型行为测试（`ClocktowerResidualUnconfirmedActionBarrierTest`、`ClocktowerSupportedPlayerLayoutTest`），没有恢复以前 22 个低中价值类。静态总数修正为 124 类/566 项；准确验收待修复提交的 CI/R2，第一轮历史删减仍为 22 类/75 项。
