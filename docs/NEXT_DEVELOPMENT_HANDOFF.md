# 新对话交接：Minimal Host Architecture Rebuild（2026-10-20 A–D2 最新裁定）

> **最新决定：D2 信息展示（正式设计，分批实施中）**：说书人进入洗衣妇、厨师、共情者、送葬者等信息角色时，UI 展示选择按钮（自动模式：唯一 AI 已决定内容的一键全屏展示；辅助模式：当前一条、未来可三条推荐+手选；手动模式：仅手选；确定唯一则只有一个展示按钮）。点击展示进入全屏；返回只回到同一角色的方桌，保留并高亮最后预览信息，允许重新挑选并预览；**该过程完全不写 Session 正式事件、不自动进入下一角色**。点击本角色 **“下一步”** 才原子提交**最后确认的一条** Observation，由 Host 进入下一角色；中间预览均不记日志，Next 后无 Previous。若 Next 之前意外中断，只需重新进入原角色重新展示，不保证恢复草稿、预览/高亮；Next 之后唯一事件幂等。不得新增 Pending 缓存/第二状态，AI 不可替玩家做真实玩家决定，也不能自动视为已经展示。此前所有“打开即写 Observation/关闭即推进/不需 Next”的 D 文案已被撤销，详情见 `MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md` 与 `AGENTS.md`。

> **当前实施顺序与状态（2026-10-20）**：① 四角色机械步骤的 Session 事实推进已在 `2a92181` 验收；② D2 展示/Next 事务已在 `a9d2bf0` 验收，当前批次继续统一具体角色三模式展示按钮；③ 随后重建全夜 `nextStep(Session)` 并删除剩余 UI 业务游标，④ 白天 B/C，⑤ 全角色/跨人数与恢复验收。上述阶段完成状况仅按实际提交与 CI 判断，旧“此刻只记录 D2”说明已过时。



> **本条优先于下方全部历史步骤和阶段文本：** 用户在 2026-10-20 审核最小 Host 全流程后明确批准 A–D，参见 [专项审计与实施边界](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md)。**A** 删除首夜/后夜通用“开始夜晚/准备”页，直接呈现第一个实际可执行步骤；**B** 白天同一方桌一次确认当日结束/处决/胜负，删除 EndConfirm 路由；**C** NGJ Klutz 只由已确认 Death/Execution 与实际角色触发待处理真实玩家选择，删除独立 Learned/得知死亡事件、ReturnToDawn 状态/无用日志；**D2（覆盖旧 D）** 信息按钮仅进入全屏预览，关闭回到当前角色可修改；唯一“下一步”确认最后预览的一条 Observation，并由 Host 推进。夜间无 Previous/pending，Night→Day 无 Dawn 或二次公开死亡事务。
>
> **立即实施边界**：优先比对现行代码而非从旧设计增删；完成剩余全树职责/消费者审计，按照完整业务链把流程/确定性结算统一到 `nextStep(Session)` / 唯一 Session；完成替代和行为验证的**同一批**物理删除旧页面、UI 游标/流程状态、预测和候选的多余包装、producer/consumer、独占测试与失效 CI 门禁。生产仍有调用不是保留理由；AI 首夜/后夜全链、TB 5–15/NGJ 5–6、两类跨局防重复、真实信息/私密与 Session 安全必须保留。**未完成**：彻底撤出旧投影/多层流转、白天由 UI 规则判断、活跃 Session 安全持久化恢复、全源码/角色验证。此条仅文档定稿，**不得报告为已删除 A–D 代码**。

> **唯一现行实施合同**：[MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)。以下早期“最新” F12、局部夜间和重新追查已删规则的历史记录不可作为新一轮执行命令。
>
> **2026-10-20 最小纵向流程实施第 1 批（本提交后按实际 SHA/CI 更新）**：不扩展全角色；生产 UI 取消后夜通用 StartNight/ReadyCard 以及无真实首夜配置时的通用准备页，唯独实际恶魔伪装确认保留为真实决策操作；删除无生产消费者的 `ClocktowerNightReadyCard`，添加 Poisoner→Monk（中毒后仍可选目标但保护无效）→Imp（即时 Death）→Butler（幂等）的 Session 事实时序回归。**未实现/绝不宣称完成**：唯一 `nextStep(Session)` 和替换 UI `nightStepIndex`/`AwaitRefreshedFlow`，退出旧 NightHostProjection 攻击死亡预测，首夜真实伪装决策持久恢复，D 展示关闭自动下一角色，B/C 白天调整，完整 TB/NGJ 全角色覆盖。测试需以新代码 SHA 对应 Android CI/R2 为准；独立隔离工作区的 Gradle 曾因文件锁/端口 bind 失败，非代码通过证据。

## 2026-10-20 D2 双人线索返回高亮恢复（第六批，以新 SHA/CI 为准）

- 洗衣妇/图书管理员/调查员双人信息界面从**本步骤最近预览的结构化命题**恢复选中的身份/两个座位；即使打开全屏导致子树销毁，返回后的方桌也应高亮最后预览的选项，而非重置回默认 AI 推荐。
- 该 UI 仅使用 Session 之外已有的临时预览草稿；由既有 `ClocktowerDirectPairSelection` 解析结构化 pair 命题，不新增持久状态、评分、合法候选领域或流程游标。手动编辑过程不因新的异步 AI 推荐被覆写；自动未在编辑时仍能展示当前有效推荐。
- 完成逻辑回归不等于设备上的最终可视验收；占卜师/侍女/守鸦人等其他界面高亮与 AI 过期时序仍需持续审查，之后才能推进统一 `nextStep(Session)`。

## 2026-10-20 D2 跨夜数字草稿隔离与免 AI 边界收紧（第五批，以新 SHA/CI 为准）

- 数字输入草稿的 Compose `remember` 现在包含当前步骤身份 `informationDecisionKey`，避免同一角色与相同对象在后续夜晚错误复用前夜数字。
- 免 AI 的请求屏蔽由“任何非空 typed 命题”改为严格要求已有可靠、来源/指标/座位一致的 **Host 数字结果**。其他 typed 声明可能仍涉及人工/AI 裁量，不可仅凭非空 `displayProposition` 跳过 AI。本批仍不重建独立合法候选引擎。
- D2 仍有同角色专用方桌高亮与真机交互待验收，不能把此批当作统一 `nextStep(Session)` 已完成。

## 2026-10-20 D2 模式边界与唯一数字信息直显（第四批；以新 SHA/CI 为准）

- 首夜 AI 推荐的有效性严格以实际操作模式为边界：`MANUAL` 不复用 `jointOpeningSemantics` 或残余 AI 推荐；自动模式当前失败时不能降级沿用旧结果。
- 对已经带有可靠且源座位、指标、查询座位完全匹配的 Host 数字型结构化结果，三模式都直接给一个展示入口，不再显示无意义手动输入或等待 AI。醉酒、中毒、指标/座位不匹配及缺少 typed 结果不能误认成唯一结果。当前这只是消费已有 Host typed 结果，不等于已经实现所有角色的确定性线索计算。
- 已有 typed 结果不再进入通用首夜/后夜信息 AI 请求规划；当前游戏状态/玩家输入版本变化，之前的私密信息预览不再能点 Next 确认。新增真实数字信息结构测试。
- 剩余：角色方桌内部预览选项/座位高亮和人工可用性检查、查询目标变化跨异步请求 UI、完整 TB/NGJ 组合、全夜 `nextStep(Session)` / 旧 UI 游标及缓存清理。当前切片未宣告 D2 完结。

## 2026-10-20 D2 数字草稿与信息占位安全收尾（第三批：以新代码提交及 CI 为准）

- 修正数字信息组件：说书人修改数字草稿即失效旧预览，避免未展示新数字却以旧结果点击 Next；异步 AI 推荐变更不覆盖正在输入的手动数字。预览仍仅在 UI 临时状态保存，下一步才正式写入 Observation。
- 普通/回退信息方桌只有持有**结构化 `displayProposition`** 的结果才提供展示按钮；全自动模式缺结果时显示等待信息而不是将原步骤占位文本伪装成 AI 已选答案。Host 已给出的确定性 typed 信息无需 AI 即可展示。Spy 的方桌交接与新恶魔身份展示仍使用其专用路径，不改变本批规则。
- 删除侍女没有真实数字结果时仍提供的“查询并展示”空操作按钮及 `onShowDeterminedResult` 生产参数/调用；手动模式由真实数字输入组件展示，自动模式仍等待有效 AI 结果。增加 typed 信息显示门禁行为测试。
- **未完成**：Host 完整确定性线索计算、全角色方桌真机视觉/交互验证、AI 缺失/过期/重新请求全链与复盘/恢复、统一 `nextStep(Session)`；以上不可用仅有编译通过替代。

## 2026-10-20 D2 生产界面生命周期与查询绑定修复（首批已通过完整 CI/R2）

- 提交 `0d92b85cba2b4f3e5a506ac6e524bfd6a94dedbe` 已推送 PR #335；真实 Android CI #4473 与 R2 #4049 均 SUCCESS，包含完整 JVM 单测和 Debug APK。之前 `bf4a146` CI #4472/R2 #4048 是基线，不能代替新的代码验收。
- `ClocktowerHostScreen` 的最后预览草稿提升到全屏提前返回路径之上，只是当前 game/phase/round 的 UI 临时内存，并按稳定 `interactionId` 匹配当前角色；全屏 Back 回到同角色仍可 Next，不创建 Observation。真人改变占卜师、侍女、守鸦人查询目标时清空旧预览、需再次预览。
- 点击 Next 先检查当前操作目标，再写入最后预览一条 Observation；无效的/旧角色预览不能提交。自动模式没有本次推荐时允许明确重新请求 AI，不默默转手动。侍女手动数字的结构化 `subjectSeats` 使用玩家当前选中的两人，不沿用旧问题的查询目标。
- **尚未完成：** 所有专用方桌的选项内高亮/真机可视验收、唯一结果由 Host 直接计算免 AI、所有角色与跨人数的完整 D2 回归；因此**不可宣称 D2 完全收尾**。下一核心任务仍是全夜 `nextStep(Session)`，清掉 `nightStepIndex`/`pendingNightAdvance`/旧步骤解析。此批未删除任何尚有活跃消费者的旧步进组件。

## 2026-10-20 D2 方桌预览确认提示与无上一角色入口清理（第二批已通过完整 CI/R2：`15ed25b`，CI #4474、R2 #4050）

- 共享 `ClocktowerNightActiveScreen` 直接在角色方桌的表面显示高对比度最后预览信息提示，正常布局嵌入滚动容器，全屏角色 UI 采用不更改 Session 的临时叠层。替代原先放在角色内容之后、全屏模式可能不可见的文字。此提示只代表预览，不代表已向 Session 提交事实。
- 直接数字信息 UI 已完全删除失效的“上一角色”按钮和 `onPrevious`/`canGoPrevious` 参数；两个生产调用同步迁移。没有新增第二份游戏流程状态，也没有提前改造下一步 Host 调度。仍需真机确认方桌不同角色上的视觉位置和按钮可用性。

## 2026-10-20 D2 角色界面三模式收敛（按本批提交及 CI 验收）

- 对双人线索（洗衣妇/图书管理员/调查员）、数字线索（厨师/共情者/钟表匠）、占卜师、侍女、送葬者、守鸦人、贤者等角色的实际 UI 补全 `AI_AUTOMATIC` **只能按当前已绑定推荐展示**的约束：没有推荐时显示等待信息，不能悄悄转成手选或提供多个结果按钮。仍允许占卜师/侍女/守鸦人的**真人玩家选择查询目标**；AI 结果必须重新对应实际目标座位，尤其侍女换查询对象后旧推荐不可沿用。
- `AI_ASSISTED` 保留现阶段一条推荐和手动输入，`MANUAL` 保留手动输入；数字结果提供显式推荐展示按钮，且不因展示按钮被点击而在 Next 前提交 Session。专用 UI 的 `automaticOnly` 只是派生的只读显示模式，不是新增可写流程状态。
- 增加占卜师结果模式和侍女真人查询绑定回归测试。**仍待验证**：各专用界面的真机可见性/预览高亮位置、AI 错误及重试入口、唯一确定信息直接显示的 Host 计算、全夜无 UI `nightStepIndex`、白天 B/C。不要以 CI 代替现场可用性验收。

## 2026-10-20 D2 信息全屏预览 / Next 最终事件（已提交，历史批次）

- **已修改生产入口**：`ClocktowerHostScreen` 中 `onShowPlayerDisplay` 仅更新当前步骤的临时 `lastInformationPreview` 和打开全屏；全屏关闭只回同一步，不记录/不推进；本步骤 Next 缺乏预览时不允许，已有预览时从最后预览的真实 typed proposition 原子提交唯一 Observation，再沿现有稳定交互 ID 走后续步骤。不再保留多次中间预览作为任何事实。
- **已删除旧流程**：自动模式下 `ClocktowerNightStepUi` 用 `LaunchedEffect` 自动打开 Numeric/Pair/FT/Chambermaid/Ravenkeeper 信息的副作用；`autoPresentAiInformation` 参数；`pendingAutoAdvanceAfterDisplay`；生产 UI 的 `FirstNightInformationMigration` 已展示标记；`ClocktowerPlayerRevealHandoff` 的展示即发布交接文件及其独占测试，改为最小的 Session 版本确认工具。自动模式仍按现有推荐选值，但真人现场点击展示。
- **进度与恢复**：从 Session 同期 Observation 判断信息步骤是否已 Next 确认，UI 游标归零时无需重复该角色；未 Next 的预览丢失即可重新开始。正式 Observation 的 recordId 由 game/phase/round/role/seat/稳定 interaction stepId 生成，**不含预览结果值**：已经 Next 后再尝试不同结果会因同一事件身份冲突而拒绝，不会另记第二条。直接动作提交既有路径不改变。保留原 AI/候选显示组件，仍需后续实际安卓端到端验证三模式的全部角色按钮/高亮样式及多夜条件唤醒。
- **尚未完成**：全夜纯 `nextStep(Session)`、`nightStepIndex` 全面退出、Mayor/Imp 动态 handoff 清理、NGJ Klutz C 与 Day B、旧 FirstNightInformationRequest/生命周期测试辅助文件的最终依赖剔除。此处代码提交后必须以对应完整 CI/R2 检查，不可把本批视为全角色验收。

## 2026-10-20 四角色机械路径收尾新批次（验收以本批最新提交/CI 为准）

- 正式代码的 `ClocktowerFixedNightStepResolver.kt` 现在**直接解析唯一 Session 的 ActionFactTimeline** 并按 phase/round 取得已确认 Poison/Protect/Attack/Butler 操作；生产 UI 使用该不可写时间线，而不是 `ClocktowerNightCheckpoint.acceptedOperationKinds`。同步删除后者、`CampBoardGameHostApp` 中用于组装它的重复数据计算，夜间死亡投影仅从 Session 已确认袭击目标判断是否发生过 Attack。
- 为四角色增加**阻止跳过未确认机械行动**的最小保证：如果临时 UI 游标误越过投毒者/僧侣/Imp/管家任一尚未由 Session 确认且仍活着的实际行动，Host 的步骤解析会返回更早的那个强制行动，不能凭 UI 光标跳过；依赖 UI 的还只是尚未迁移的信息角色展示，不能据此宣称全夜已 Host-only。
- 投毒者、僧侣、管家的 Next 正式提交后不再建立 `ClocktowerConfirmedNightAdvance` 暂存 UI 跳转：由 Session 事实驱动下一可执行步骤；对 Imp Attack、Mayor 转移等可能动态重排夜序的场景，仍保留稳定 stepId 的临时重排衔接，以防漏掉死亡触发角色；其替代/删除待全夜统一 `nextStep(Session)`。
- 新增/更新回归：UI 游标越过强制行动也不能漏掉、阶段轮次严格隔离、真实 Session 的 Poisoner→Monk→Imp→Butler 事实递进、信息角色 barrier 不能伪造完成。**未完成**：信息角色 D2（按用户最新批准规范单独实施）、所有夜间步骤可恢复的 `nextStep(Session)`、其余 UI 游标与动态 handoff 全清、白天 B/C、全部角色真实完整对局。此批按具体 Android CI/R2 验收，勿把纯函数回归等同真机端到端验收。

## 2026-10-20 最小流程第 3 批：机械行动由 Session 事实恢复当前步骤（验收以当前 SHA/CI 为准）

- 范围仅含 TB/NGJ 夜晚现有脚本流中的 **Poisoner、Monk、Imp、Butler**。使用已按 phase+round 从唯一 Session 事件时间线派生的 `acceptedOperationKinds` 和唯一 GameState 的实际存活信息，识别这四类已执行/不应唤醒的机械步骤；UI 不能因为 `nightStepIndex` 重新初始化而重复执行这些步骤。步骤完成仍即时提交，不添加独立持久化标志。
- 新 `ClocktowerFixedNightStepResolver` 是纯读取的最小步骤状态判断，当前仅跳过这四类机械动作；遇到其他真实信息角色与 Ravenkeeper/Sage 夜间触发时必须保留原展示步骤，不能推断其已完成。为避免未经确认的刷新泄露下一角色，旧短暂 `pendingNightAdvance` handoff 尚在；`nightStepIndex` 暂保留为显示提示而非提交事实。
- 物理删除 `ClocktowerNightCheckpoint` 中无生产消费者的 `nightStarted`、`nightStepIndex` 字段和独占测试输入，代之用 GameState + Session 已确认事件。新增跨角色、游标复位、死亡跳过、不跳过信息角色、夜末无待操作的纯函数回归测试。
- **未完成**：通用持久化 `nextStep(Session)`、实际信息公开/展示的逐步完成记录与关闭后自动推进（D）、旧 Compose 显示游标退出、按所有角色/夜序的完整动态触发与 Day B/C；不得误称所有夜晚已实现 Session-only 游标。提交后须以该 SHA 的 GitHub Android CI/R2 真验收更新结果。

## 2026-10-20 未提交文件专项审计：死亡只保留一条确认事实（提交后确认 CI）

- 审计 `ClocktowerGameSessionAliveBoundaries.kt` 的遗留未提交更改：**保留意图，并补齐消费者，而非抛弃**。现行产品约定已经确认 Session 的 `Death/Execution` 即死亡唯一事实，不在同一次提交中再建 `PUBLIC AliveAt(false)`。原未提交改动只删了 `publishedObservationId` 参数，尚有三条生产调用和对应单元测试使用，若原样提交将无法编译。
- 一并迁移杀手射击致死、处女提名直接处决、白天正式处决的生产调用与测试；删除 `preflightClocktowerPublicAliveObservation`、用于该冗余观察的调试字段和仅为其存在的 Virgin preflight UI 回调，取消旧 `publishedObservationId`、`public-alive-` producer。仍保留一般观察数据结构 `AliveAt` 解析以理解历史记录，保留 Klutz 死亡时真实身份/醉毒证据及真正选择行动。
- 这不意味着日间所有 Host 业务已重构完成：`EndConfirm` UI、`KlutzLearnedDeath` 独立事件及规则/胜负 UI 控制仍依 A–D 后续批次清理。需要本次 Android CI/R2 真实验证通过才可报告代码验收；目的是清理此遗留未提交工作，维护干净的开发分支。

## 2026-10-20 第二批：夜间稳定步骤身份与真实死亡触发（代码批次，验收按最新 SHA）

- 后夜确认步骤以后不再用旧 `ClocktowerNightAdvanceDirective.AwaitRefreshedFlow`、旧列表长度或原索引猜下一角色。改为短暂 UI handoff 储存刚**已确认**步骤的稳定 interactionId / 已完成前缀，由刷新后的 Host 可执行步骤重新定位下一项；对 Imp 攻击、市长转移必须等 Session gameStateRevision 增长才允许导航，重复 Next 在待刷新期间禁用。该 handoff 不是待提交业务操作或第二份 GameState，**但 Compose 的 `nightStepIndex` 仍存在，仅作为显示位置；还没实现 `nextStep(Session)` 的完整可恢复业务游标**。
- `ClocktowerNightHostProjection` 不再使用尚未确认的袭击草稿/死亡预测驱动 Ravenkeeper/Sage 唤醒和后续角色跳过。只有当前夜在唯一 Session 已接受的 Death 才提供死亡触发和正常角色跳过。Mayor 判定是否可选择转移的旧预测仍保留等待拆除；有意不扩大到全部角色。
- 删除旧索引式动态推进函数和专属测试；新增死亡使先前角色消失、插入守鸦人步骤、当前 actor 消失、夜末直接完成、重复步骤 ID 拒绝等回归，以及“未确认袭击不得触发守鸦人”与“已确认死亡触发守鸦人”测试。现行 AI、私密已展示事实、双剧本/跨局轮换未删。
- **未完成**：彻底 Host-owned `nextStep(Session)`、去除 `nightStepIndex` 和其 UI/Checkpoint 双轨、删除整个死亡预测 Adapter/Planner 重叠、真机中断安全恢复、A 中尚存恶魔真实配置步骤的持久判定、D 展示关闭与 Session 步骤身份统一、B/C 日间与 Klutz，以及全角色覆盖。此处不允许把批次性修复表述为完整最小 Host 完成。要以本次实际代码 SHA 的 Android full CI/R2 验收结论为准。

## P1 2026-10-20 用户最终裁定：取消黎明阶段和机械死亡公开复制（历史已实施）

- **唯一现行合同**：夜间确认的 `Death` 是即时生效的机械事实；后续夜间能力从同一 Session 的已确认存活状态计算，不维护为了玩家认知一致性而生成的第二次公开死亡事实。不额外生成 `PUBLIC AliveAt`、不设置 `DAWN` 阶段或播报确认页。首夜/后夜最后一项操作直接进入 Day，白天方桌读取本轮 Night 已确认死者并显示；讨论、提名、投票正常继续。NGJ 呆瓜在得知死亡后的实际白天选择保留，已给角色展示的私密观察及现有 AI 推荐整链保留。
- `0e63d9c6c7f126b5a383bf5cfecb914b1489e192`：生产移除 `StorytellerPhase.DAWN`、`ClocktowerPhase.Dawn`、`ClocktowerDawnSummaryScreen`、夜→日合成 `PUBLIC AliveAt` 的代码及专用 Dawn 测试，Day Overview 显示“昨夜死亡/无人死亡”。官方剧本 JSON 内的 dawn **仅是夜序终止标记**，Host Projector 不再将其生成为游戏操作或 UI 步骤。[Android CI #39355476807](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39355476807) SUCCESS。旧 [R2 #39355476845](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39355476845) 失败仅因自动化守卫仍要求存在已删除 Dawn UI。
- `64c57df66df527c65aeb5b9099d998fd23fa158d`：R2 守卫改为确保 Dawn UI/阶段**不得重返**；删除新恶魔身份确认时第二次写 `RoleChange` 的旧分支；`KlutzReturnToDawn` 更名并保持 Day 真正行动；`DawnCommitIdentity` 更名为 `ConfirmedNightEffectIdentity`（出于历史幂等兼容保留旧稳定 ID 值，不代表 Dawn 阶段）。[完整 Android CI #39356416753](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39356416753)/[R2 #39356416743](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39356416743) 均 SUCCESS。
- **未完成**：白天杀手/处决仍有早期 `PUBLIC AliveAt` 重复观察，应按是否实际必要进一步删除；旧死亡预测 Adapter/多套 night flow、单 Session 持久化恢复及 TB 5–15 / NGJ 5–6 完整角色流程仍待审核。#334/#335 仍是 Draft，不能宣称项目完成。

## P1 2026-10-20 夜末双轨正式删除（历史检查点）

- #335 已推送 `b040fb66`：Mayor 转移杀死 Imp 的同时，若符合 Scarlet Woman 条件就在同一 Session 事务提交被动继任的 RoleChange；此前 `d38c50d` 已完成 Imp 自杀的即时 Attack + Death + RoleChange（完整 Android CI/R2 成功）。
- #335 提交 `712d46e6`：物理删除旧夜末死者/继任者/中毒延续选择界面 `ClocktowerNightResultConfirmation.kt`、只服务该旧界面的 `commitConfirmedNightDeaths` 与 `commitDawnPoisonCarry`/`DawnPoisonCarryIntent`、重复 `NoNightDeaths` 专用事件类型及其独占旧测试；从已确定 Death 直接发布 Dawn 公告，毒保持到离开 Day 时过期。`ae0e3447` 修复动态夜间刷新后的最后一个旧 callback，同时 FIRST_NIGHT 末尾直接进入 Day。`71f54a7e` 清除旧的 `finishNightResolution` 测试残留。`ccaef940bb2de0b068b26ba8f844d1a2dc562dad` 修复 `ClocktowerNightTargets.after(PhaseAdvance)` 原有的阶段时点错误：以前是进入 Night 清理而进入 Dawn 仍残留临时目标，现在任何正式 PhaseAdvance 都清空当夜临时攻击/保护/市长选择，死亡事实与毒机械状态不受影响。[完整 Android CI #39349210480](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39349210480) / [R2 #39349210477](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39349210477) 均 SUCCESS。
- **验收边界**：代码已提交推送，`ccaef940` 精确完整 CI/R2 已通过，但 CI 不能替代完整夜间真机对局和 P1–P5 全模块验收。双剧本条件唤醒、恶魔继任后的顺序、NGJ 呆瓜、预览适配器清理及实用中断恢复仍需下一批审计和实测。

## P1 2026-10-20 市长死亡即时结算检查点（以下为历史检查点）

- 实际提交 `19c7c9a1861e2d9c53be0d5a2a1dba84e3b74f68` 已推送 #335：`ClocktowerGameSession.commitConfirmedMayorRedirect` 不再仅记录 `MayorRedirectChoice`，而是在同一个 `state.copy` 原子记录选定的裁量与可能发生的私密 `Death`，即时更新唯一 GameState、死亡 Timeline、版本和已接受市长目标；自己的死亡、转移给健康玩家、转移给已死/受有效保护/有免疫的玩家均按实际状态结算。同夜重选会被拒绝。原允许独立记市长事实、多次改选的 `MayorRedirectBoundaryTest` 被新事件边界用例替代。精确 Android [CI #39340843586](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39340843586) / [R2 #39340843687](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39340843687) SUCCESS。
- 第二批 `c178e8f31987bada5621b099ac91542e36265621` 防止已确认恶魔攻击市长、但裁量尚未完成时直接在夜末记录“无人死亡”或进入黎明，回归测试同步提交。[完整 Android CI #39341689671](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39341689671) / [R2 #39341689637](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39341689637) 均 SUCCESS。
- **下一个未闭合业务链**：小恶魔自杀后应在袭击结算时记录 Imp Death，并按真实 Minion/Scarlet Woman 继任裁量记录 RoleChange，通知新恶魔后继续余下夜间步骤；随后才能安全删除旧夜末自由勾选 Death/继任/毒延续的确认页面及其重复适配器，不能为了删除而令自杀局无法继续。另需审查市长转移杀死恶魔时是否触发继任、夜间终局以及单 Session 恢复。P1 尚未完成；PR #334/#335 继续 Draft。

## P1 2026-10-20 最终简化：所有夜间步骤只向前、不设 Pending（以下为上批次事实）

- 用户继前一条“恶魔袭击禁止返回”后，**进一步明确整个夜间流程一律不允许“上一步”**，包括信息展示类角色；不采用“信息类可回退重看”的中间建议。各夜间角色在本步骤点 Next 时立即接受机械事件，展示信息则在实际私密公布时接受 Observation。用户不需要为任何角色多点“开始当前步骤”。
- 真实代码 `2eb56b2fb71d13965ff8832ccacddfe3445603f8` 已推送 #335：删除 `ClocktowerSessionState.pendingOperation`、`PendingNightOperation`、`stageNightPoison/Protection/ButlerMaster`、`startNextNightStep`、`acceptPendingOperation` 与 Checkpoint 的 Pending 字段；改成各自的 `confirmNightPoison/Protection/ButlerMaster` 直接写入同一 Session，保留 `confirmNightAttack` 原子私密死亡。夜间 UI 移除了“开始当前步骤/开始夜间结算”页面与后退回调，通常夜间 Previous 被禁用并隐藏；新增立即确认/幂等测试取代旧 Pending 测试。[完整 Android CI #39339115698](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39339115698) / [R2 #39339115692](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39339115692) 均 SUCCESS。继续保留 AI 全链、跨局两类防重复、TB/NGJ。
- **仍须完成**：市长转移与恶魔自杀继任即时真实选择后的 Death/RoleChange、删除夜末自由决定死者/中毒/继任的旧 UI、去除旧攻击预览依赖、信息已展示原值复看一致性、持久化恢复与 TB 5–15 / NGJ 5–6 回归。#334/#335 保持 Draft，不要将文档测试当成完整代码验收。

## P1 2026-10-20 第二次裁定与实际变更（本节已被上方最终简化覆盖）

- **明确例外**：除恶魔以外的 Poison/Monk/Butler 仍遵循唯一 Session `pendingOperation`，允许下一步骤未执行前退一步更改；**Imp 袭击在自己的“确认并继续”动作当刻不可逆地接受**，与确定性的死亡及 GameState 一次原子提交。此前 10-20 记录“Imp 延迟到下一步骤提交”已由该新产品决定取代。
- 新代码 `683ad97aead2b4cd6fb2d1f7047d609cb8ca11a2` 已推送 #335、[全 Android CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39332646726)/[R2](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39332646724) 均通过：删除生产 `stageNightAttack`，改 `confirmNightAttack`；单次 Session 写入 Attack + 唯一 Death；UI Previous 无法跨已接受的 Imp，Ravenkeeper/Sage/后续角色从单一 History/GameState 已确认 Death 派生；以新立即确认语义测试代替旧暂存攻击测试，覆盖 Monk、Soldier、投毒、Mayor、自杀边界。
- 后续安全收尾 `5ffbdbec6455569e268d833a48775d2ff2217947` 预选并锁定旧夜末界面已接受 Death、Session 防止夜末增加/移除这批确定死者；[完整 Android CI #39333202349](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39333202349)/[R2 #39333202449](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39333202449) 均 SUCCESS。**下一项必须仍实际做**：Mayor 真实转移及 Imp 自杀继任不能用 `null` 死亡代替“待裁量”，在继续后续角色前完成选择并原子写 Death/继任；彻底移除旧夜末自由决定死亡、重复攻击/预测适配器，保证已确认死亡与黎明公告不重复；补持久化恢复和 TB/NGJ 人数/角色测试。保持 #334/#335 Draft，绝不声称 P1 完成。

## P1 2026-10-20 更新（优先于下方 10-19 检查点）

- 已完成单一 `ClocktowerSessionState.pendingOperation` 的夜间玩家行动首批整链替换：Poisoner、Monk、Butler、Imp **四种行动**统一暂存、修改、按下一实际步骤开场原子提交；正确同步投毒机械状态，提交前不修改效果/历史。真实角色间的占位 UI 不可提前触发提交；Session 拒绝两个并行 pending、跨轮/未前进提交、不相关事实消费 pending。
- 实际代码提交：`c0a1394` 首次统一（659 tests 中夹具 Butler 不符，已修）；`be91f77` 修测试，Android CI/R2 SUCCESS；`8f7058e` 删除旧 `confirmButlerMaster` 并把首夜/后夜重复验证迁入真实 Pending，Android CI/R2 SUCCESS；`c29a319b7e4d29423fb4c5534060924ab2cc6f8b` 对已提交四类行动统一阻止回退编辑、删除旧 `clearConfirmedNightAttack`/`clearConfirmedNightProtection` 和独占错误测试、确认 Session 原子接受身份（[完整 Android CI #39327637102](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39327637102) / [R2 #39327637052](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39327637052) 均 SUCCESS）。
- **不要误报 P1 完成**：目前未统一所有角色/推荐/显示事件与持久化安全恢复；`ClocktowerNightResultConfirmation` 仍在夜末自由确认最终死者，Ravenkeeper/Sage/Mayor 仍依赖旧纯死亡预览，**未完成 Imp 确认后确定性死亡即时生效/条件唤醒/旧路径彻底退出**。下一批先推进这条真实闭环，再删旧 Adapter、重复 UI 和测试，不恢复旧候选/评分/控制器。#334/#335 均保持 Draft；保留双内置剧本与现有 AI 全链。

## 最新 P1 实际实施检查点（2026-10-19，取代下面历史基线）

- 已完成 PR 整理：#330–#333 全部按顺序合并 `main`，四次合并及最终 `main` 的完整 Android CI 均通过；#334 已改 base=`main`，仍 Draft（大规模清理审计未完成）；#335 保持 Draft、base=#334 的分支，不做 merge/rebase/force。
- `clocktower` 工作分支 `refactor/minimal-night-flow-20261019`。P1 本轮代码已实际推送：`baa777d`（Session 内暂存 Imp 攻击，下一实际步骤开始才提交）、`0a5dcbf`（阶段安全与预览测试）、`6a91f59`（已提交目标无法回退更改）、`f91184f`（夜间机械死亡私密记录，跨 NIGHT→DAWN/DAY 阶段时才原子公开死亡公告）。最后源码 HEAD `f91184fd24ed394b4880330bccc56bc18796eacf`，GitHub [完整 CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39254476810) 与 [R2](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39254476803) 均 SUCCESS，含 Android tests/APK。没有真机或收费 API。
- **必须如实保留缺口**：`pendingNightAttack` 目前只是首个垂直切片，尚未统一为整个夜间所有角色共享的**最多一个 pendingOperation**；Poison/Monk/Butler 仍存在旧立即确认/多重 UI 控制。`commitConfirmedNightDeaths` 虽已把事实与黎明公告分离，但**真实死亡仍在夜末通过人工 NightResultConfirmation 才提交**，不是恶魔行动后的及时确定性结算；当前 Ravenkeeper/Sage 触发仍使用旧死亡纯预览，而非最终已确认机械状态。旧 Adapter/Projection/Materializer 尚有生产消费者，不可声称已删除。无稳定持久化恢复的完整验收，也未遍历 TB 5–15/NGJ 5–6 所有角色组合。
- **下一实施顺序**：先以稳定步骤身份把 Poison→Monk→Imp 和其后续条件动作统一为一个 Session-owned pending 操作/按需纯预览，真正在后续步骤开始时原子接受；把无需裁量的死亡结算移到恶魔行动被接受时，确有裁量的 Mayor/继任等显式处理，改变 Session 存活状态再计算 Ravenkeeper/Sage/后续跳过；最后移除被替换的旧 Checkpoint/Adapter/夜末自由判死/多份可写 UI，补齐事件恢复、隐私、整局验收，任何未达成的子链继续 Draft。

## 下一轮必须实际执行的首批工作

1. **先通过 GitHub Connector + Mini MCP `clocktower` 核对**：main / PR #334 / Draft #335 的真实 HEAD、base、clean、远端同步、准确最新代码 CI/R2 和任何并发改动。此前基线 `refactor/minimal-night-flow-20261019`, HEAD `011baa1f49683c42b1eef65acc5a50f669c373ab`，#335 基于 `codex/minimal-host-full-scan-20261018`，不是直接 main。原代码 `47ce60da` Android CI #4422 / R2 #4002 SUCCESS，docs-only `011baa1f` CI #4423 / R2 #4003 SUCCESS 但 Android skipped。新对话核对本次文档提交后更新的 HEAD。
2. **P0 是一次面向新架构的全仓横向审计，不是复刻旧 F12 逐角色/逐文件随机删除**。以当前实际 Git tree 罗列所有生产代码、UI、AI、规则算法、持久化、测试/CI，按“Host 必需 / AI 必需 / 两内置剧本与跨局防重复 / 整链替换 / 直接删除 / 人工审核”归属；审查真实消费者但不以存在调用为保留理由。交付一个可执行的替换-退出映射：旧文件/函数、对应新边界、调用者、删除后的实际用户流程、风险、验收测试。未扫描的文件如实标记，不假称100%。
3. 同步建立 **TB 5–15、NGJ 5–6** 角色行为合同：脚本夜序，触发条件、玩家输入、确定结果、裁量节点、时间性毒/保护、死者正常动作跳过、守鸦人/贤者死亡触发、Dawn 公开、处女/投票/杀手/胜负；核对人数边界与 NGJ 剧本特例。该合同是 P1/P2 测试的行为标准，避免为旧算法补“合法域”。
4. 根据真实堆叠 PR/改动选择最干净的实施分支策略，**保持 #334/#335 Draft，不自行 merge/rebase/force-push**。P0 归属与架构证据形成后即开始首个真实重构批次（如状态安全允许），**以完整的投毒→僧侣→恶魔→死亡/条件唤醒路径联动 P1 暂存提交**；重建和删除被替代旧调用一起完成。不要只提交另一份宏观计划。
5. **单步事务硬合同**：Next 后最多一项 pending，进入下一个 UI 但尚未实际开始即可 Previous 修改；下一个玩家操作/私密显示/公布之前原子提交旧 pending、状态、顺序事实并安全落盘。失败恢复最近安全步骤，不要双 GameState 或通用 Undo。即时机械死亡但黎明才公开；旧 AI 请求需要匹配正式 revision 和 pending 指纹；真实误播只能另行显式更正。
6. 实施批次报告包含真实被删路径、净减少/新增、所有旧控制剩余消费者、唯一 Session 风险、AI 及两类开局多样性回归、全源码 <= 50KB、GitHub 对应代码 SHA 的 Android CI/R2。项目不以减少行数或旧结构测试数量当作完成度；必须证明每条已迁移业务链的旧代码彻底退役。

**绝不恢复**已删除的通用 Host 规则/候选/评分/世界枚举；**不删除**必须的确定性规则执行和原 AI 提示词/API/战略记忆/模式；**不新增**独立恢复状态、第二 GameState、夜末自由选死者或动态脚本通用框架；**不调用收费 API**（除非另获授权）。

## 旧开发交接及历史审计记录（以下皆为历史，不再决定工作顺序）

## 先读与现行授权

**2026-10-19 最新用户指令，优先于下述此前交接**：**立即停止旧 Rules 删除回溯审计；已删的判定不恢复，旧多层候选引擎可继续删除。** 唯一必须本阶段正确实现的是 **Host 游戏流程**：官方剧本角色顺序、从唯一 Session/GameState 和已确认行动读取真实状态、保护/恶魔袭击后自动跳过已死的普通角色、条件触发的守鸦人/贤者/送葬者、不打乱已完成步骤、保留酒鬼展示唤醒，市长/恶魔继承等真正选择才请 AI/说书人裁量；绝不能问玩家要不要叫醒占卜师或送葬者。其他固定信息真值（pair、厨师、共情者、占卜师等）以后再简洁重建，不是当前补回对象。PR #335 代码提交 `47ce60da9522c0cebf1613ccd58581c3035b31a0` 已推送并等待真实 CI/R2；它删除通用角色Handler注册表，令 Host 自动剔除已确认攻击结算所致、发生在 Imp 之后的普通角色后续唤醒，保留早期已执行步骤和死亡触发，并在攻击/Mayor后重算游标。**未完成**：`ClocktowerNightResultConfirmation` 仍到夜末才能提交 Session death；不得宣称新规则/状态生效时序完成。优先让夜间确定性死亡及时进入唯一 Session，保持死亡在黎明前对玩家保密，并做条件唤醒/保护/继任测试。不启动额外全仓清理、历史规则删除审计或付费 API。

**2026-10-19 最新最高优先级（覆盖下面早期冲突路线）**：本段是**旧指令，现已撤回**：不再只读回溯审计 F12-L/F12-M–R 旧规则删除：Host 不是被动存储器，必须从唯一 Session 状态自动执行规则确定的死亡、保护、夜间跳过和无歧义信息；AI/说书人只对真正有多个规则许可值的选择做决策。醉毒错误信息、Recluse/Spy 可选登记、Mayor 重定向和开局策略可有裁量。人工显式纠错记录真实已发生误播，与常规自由选择唯一结果区分。已批准保留现有 AI 和两种开局多样性；不重建旧通用合法域/世界枚举，也不引入第三份事实状态。旧规则判定功能现阶段不恢复；仅本轮游戏流程必须即时正确，其他唯一答案稍后重新设计。其它全仓删除仍暂停，Draft #335 保留此前真实 CI 通过的流程减法。**优先追查** `ClocktowerDirectBooleanResult`/占卜师唯一布尔、`ClocktowerNightResultConfirmation` 夜末全人工确认、Imp 保护/死亡/唤醒、Chef/Empath/Sage/Ravenkeeper/Slayer/Virgin/Artist 的唯一结果是否被替换成自由人工输入，区分确定/可选/冗余后报告。

**2026-10-19 新的即时交接（覆盖下述 F12-R/F12-L 任务优先级）**：**暂停其它全仓清理**，只推进最小化 **夜间游戏流程**。在独立 Draft PR #335（`refactor/minimal-night-flow-20261019`，base=`codex/minimal-host-full-scan-20261018`）继续，原清理 Draft PR #334 不动。已实际推送三次源码改动：`651704ce5ef0fd050071f5a2f23b8e35aad0ad56`（删除 `FirstNightSetupDependencyPlanner`/`Projection` 和专属测试）、`bf350b928a60323d84b46b274af6870fcff3812f`（唯一 GameState 提供真实/展示身份及存活唤醒集合）、`0b9f83b2477a2991fbdfac3b09f62e28a603e250`（退役测试专用 `ProductionInteractionOrderer` / `.order`，保留稳定 UI StepIdentity，测试实际 `interactions`）。三个精确源码提交的 Android **CI #4417/#4418/#4419 SUCCESS，R2 #3997/#3998/#3999 SUCCESS**。开发机隔离 Gradle 不可用，不能声称本地跑通；无真机、无付费模型调用。**最大未完工问题**：`ClocktowerNightHostProjection` 仍经 `resolveTroubleBrewingDawnDeathResolution` 使用旧 DemonAttack 规则推演出的死亡结果来决定 Mayor/守鸦人/贤者条件步骤，`ClocktowerNightResultConfirmationScreen` 却只在夜末确认真实死亡。下一步改为：恶魔行动后 Host **直接结算规则唯一结果**；只有 Mayor 等有效可选分支才需私密 AI/人工确认，再把死亡与唤醒及时写入唯一 Session；黎明才公开。不得让说书人常规重复判断规则唯一的死亡。实现确认事件驱动条件步骤之后删除旧预测规则与专属测试；继续合并重复 Planner/Projector/Registry/Materializer 封装，而不要建等价新规则引擎。必须保留 TB/NGJ、酒鬼展示身份、开场恶魔信息、红鲱鱼、夜间目标/私密公布、已有 AI 全链和唯一 Session 历史。最终完成前 Draft/不合并。不要按下方老路线去审计剩余221个不相干源文件。

**2026-10-19 早期所有者裁定，其“旧 Rules 全删、Host 不计算”部分已由本页更晚决定修正**：当前只保留最小唯一Host状态/事件/必要UI安全与现有AI推荐，**现在必须全部清除旧Rules实现和其不必需生产消费者/独占测试，不能只删入口留下旧Rules影子控制**。**旧Rules完全退出之后**才可能另行批准从零重建独立只读Rules，本轮不开发、不复用旧实现；这与已经工作的AI必须保留形成明确区别。动态/custom/homebrew剧本导入为未来可选，不必为其维持当前复杂架构，允许对custom角色、Homebrew检测、扩展解析/normalizer/测试整链删减；当前`ClocktowerScriptJsonParser`和`ClocktowerScriptNormalizer`被`RulesetJsonLoader`→`BuiltInClocktowerRulesetCatalog`用于两内置剧本，需先确保官方TB/NGJ继续正确加载，不能误删内置支持。已有生产调用**不能**单独成为保留理由，反而应核对完整调用链是否属于最小Host/AI。

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F12-K/F12-J/F12-I/F12-H/F12-G/F12-F/F12-E/F15/F12-D/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- **本轮文档边界更新基线**：起点`65a15960c5fe2c9bdaa2d2438972d2dbdc8996ba`，分支`codex/minimal-host-full-scan-20261018`、PR #334 Draft/base SHA`64be3090`；本次只更新权威约束与两条已审计的可选动态导入归属，**没有实际删生产代码或测试**。后续先完成剩221项全文件审计并对91项需要解耦、1项明确必删、仍需人工确认的TBGS快照codec按产品必要性重新排序；保持AI完整调用、单一Session、跨局两种防重复，不新增并行状态/规则adapter。任何docs-only CI Android skipped都不能冒充F12-R代码CI #4414。


- **最新F12-R完整代码检查点**：开发机MiniMCP`clocktower`原Draft #334分支`codex/minimal-host-full-scan-20261018`，干净远端一致起点`fcaecb54b75d59a7d5b2f247207e27fef2b9c6ed`；代码`e0cd90be62e00d52895e730ff05163de49da3e7a`（`[full-ci]`）整条SlayerTableState/UI/HostScreen/App层不再使用按被声称/存活/真实Slayer已使用规则的claimant/target候选集合：真人直接选实际存在且唯一的两个座位，保持Session真实Shot记录、明确实际死亡/继任，保留App来自Session的历史`clocktowerSlayerUsed`仅用于事件事实，旧UI生成声明人列表已删。真实GitHub diff**5文件+41/-114、净删73行**，完整[Android CI #4414 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39204226346)/[R2 #3994 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39204226503)，Android tests+Debug APK/ASP/真实Clingo/CI gate均全绿；docs-only提交之后Android skipped不能替代此真实编译。
- **全仓唯一审计台账**：原`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-R 横向6个原待审全文审阅：NominationInteraction/DayOverviewTableUi/SlayerTableUi/ArtistInteraction/ArtistTableState/NominationDraftTest。原SlayerTableState及其Test此前已审本次改后复核不重复计。现行**223/444（50.23%）已审、221待审、0审计中**，AI首夜/后夜/pair/numeric/strategy/prompt/transport/schema/解析、双模式、阵容跨局与玩家身份跨局双防重复、唯一Session/私密原子提交都保留。
- **下一步任务**：继续全树剩221项，特别`ClocktowerNominationInteraction.kt`现在仍据Virgin真角色、poison和Spy登记自己计算立即处决结果，App又重复计算`firstVirginNomination`；应改成说书人先确认真实Virgin能力消耗及是否处决再唯一Session提交Nomination/Execution，并移除旧Spy专属选择和规则推演（注意保留可用的投票/处决流程与事实顺序）；`ClocktowerArtistInteraction/ArtistTableState`仍有真值自动菜单与claimant名单门禁，待真人直选和只记录真实公开答案。旧`DemonAttackProductionAdapter`→NightHostProjection中的死亡预览仍决定Sage/Ravenkeeper实际唤醒，夜末Session才确认真实死亡，不能把攻击目标当死者硬删，先建立先于唤醒且可恢复的真实确认入口后连同独占测试整体退休。继续App/HostScreen/NightStepUi三个>50KB源码，未授权不得merge/retarget/force push/付费API/真机。PR #334仍Draft/base SHA`64be3090`。

- **最新F12-Q实际代码验收**：开发机`clocktower`、原Draft #334分支`codex/minimal-host-full-scan-20261018`，起点`0a500c2f6d3438e9fa91611e2c31caef8c28ae4d` clean/equal。生产提交`3de38e6e53a841bd4c866ce1f2daa1fa5a739228`（`[full-ci]`）Slayer射击从根据中毒/Drunk/Recluse/恶魔自动命中和自动死亡，换为说书人勾选**真实目标死亡**（默认无死亡、换座位重置），继续用原Session提交真实Shot、已确认Death和后续继任；移除旧oneShotDecision/OneShotAbilityDecision/selectedMechanicalEffectApplies和专属规则测试，7文件+45/-165**净删120行**。现有`SlayerShot.abilityConsumed`标识真实Slayer使用历史，`targetRegistersAsDemon`旧事件字段保留向后兼容但新界面不臆造。旧R2 #3991因硬依赖退休登记控件FAILED，后续`8b1d07b983de190ed0a61ec994fb01704a6e3cca`更新门禁断言新实际结果而不恢复旧控件，真正最新完整[Android CI #4412 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39202867302)/[R2 #3992 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39202867257)，Android tests+APK/ASP/Clingo/CI gate全成功。无真机/付费API。
- **唯一台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-Q真实横向5原待审源码/测试：SlayerInteraction、SlayerDraftTest、AbilityFunctioningSemanticsTest、SlayerTableState+Test首次全文并追真实引用；现库存**444项、217（48.87%）已审、227待审、0审计中**。其余原已审App/Screen/ActionFactDraft/AbilityFunctioningSemantics和docs只复核，不双计。
- **下一任务优先**：`DemonAttackProductionAdapter`仍是Sage/Ravenkeeper/Mayor实时夜间预览消费者，而`ClocktowerNightResultConfirmation`只在夜末确认真正死者，直接删除会破坏可玩的夜间唤醒与自动AI，不能把攻击目标假冒死者、增影子GameState；需要先设计并实施真实可恢复的先于唤醒确认/继续入口再移除完整attack语义及独占测试。并继续SlayerTableState里实际没有必要的alive/claimant规则候选门禁、Virgin/Mayor自动裁定和三个大于50KB源码的减法，以及227待审横向遍历。现有AI开局/后夜/pair/numeric/策略memory/API transport/schema/多模式、两类跨局防重复、唯一Session事实/保密与提交安全都要保留。PR #334继续Draft/base SHA`64be3090`，未merge/retarget/force push，不授权真机/付费API。此后docs-only Android skipped不能取代#4412完整代码验收。

- **最新F12-P真实验收检查点**：MiniMCP`clocktower`原Draft PR #334工作分支`codex/minimal-host-full-scan-20261018`，起点`d7c9484f804f882485ba7257c12952f82b2147bd` clean/equal，实际代码`ea6d9a03c968077299c0aea3aac043ab7514d6bf`（`[full-ci]`）完整退役`ClocktowerNeutralInformationPreparation.kt`与2份独占Undertaker/Ravenkeeper旧角色菜单测试；同时移除HostScreen调用、InformationStepBuilder三个重复候选域赋值、旧ResultChoices转换，UI走直接RoleAt和AI typed预选，保留已确认目标seat/Session事实/原玩家展示。GitHub精确比较**10文件+269/-431，净删162行**，3整删/2新增。首次CI #4408 Android编译失败旧`choice.key`字段，随后修复`37f7c63bcc0df93dd47918153fcfce4f04db5c7a`（`[full-ci]`），用原`clocktowerInformationCandidateId`去重，最终真完整[Android CI #4409 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39192215275)/[R2 #3989 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39192215497)，Android tests+APK/ASP/Clingo/CI gate全部SUCCESS，无付费API或真机。
- **现行唯一源审计库存**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-P：445退役3项、增加2项→**444项**；211已审删1、加新2→**212/444（47.75%）已审、232待审、0进行中**。原两份旧测试原待审但已退役，不假装新审。原已审UI/builder复审不双计。守鸦人玩家先选座位；送葬者仅用实际昨日处决座位；不会因AI预测或旧候选规则改变座位/事实。
- **下一任务**：不要回头补旧菜单与测试，继续横向剩余232文件；优先拆断`NightHostProjection→DemonAttackProductionAdapter`旧恶魔攻击预测与Sage/Ravenkeeper唤醒及Mayor redirect的依赖；真正`ClocktowerNightResultConfirmation`在夜末，**不能把攻击目标代替实际死亡或没有触发步骤就简单删适配器**，必须先保留可玩的实际确认/信息公布入口，然后连同独占攻击规则及专属测试整退。其次逐步真实减小CampBoardGameHostApp/ClocktowerHostScreen/ClocktowerNightStepUi的>50KB职责、Slayer/Virgin旧自动规则/事实事件混合。唯一Session事实/保密/幂等与现有全链AI首夜/后夜/pair/numeric/prompt/transport/parser/memory/preselection/辅助及自动模式、阵容跨局与玩家身份跨局防重复必须保留。PR #334继续Draft/base`codex/minimal-host-m1c-direct-number-20261018` SHA`64be3090`，不merge/retarget/force push，不授权付费API/真机。docs-only HEAD CI Android skipped绝不等同代码#4409全验收。

- **最新F12-O已验收代码**：开发机MiniMCP别名`clocktower`；起点`0513123ef51d82766327908cbd412ad9aaf66b3d` clean/equal，代码提交`de091e2480373b271325d679c87c10bbb3093e10`（`[full-ci]`）17文件+372/-1166（净删794行）、整删3个生产旧PairManualAuthority/SelectionModel/SelectionUi和4个旧依赖测试；新`ClocktowerDirectPairInformation.kt`+测试保留人工角色/双座位/缺席输入、真实Session信息、AI typed preselection；Role×座位全组合从HostScreen/NeutralPrep/夜间入口全部移除。CI修复提交`526190030926eb5e52e8a2e9f85ec73e4aef148d`同步R2老enum必须存在的错误架构门禁，真正完整[Android CI #4406 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39189973397)与[R2 #3986 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39189973230)，Android tests+APK/ASP/Clingo/CI gate/R2全成功（老R2 #3985失败已修，旧CI #4405取消）。**如果后续HEAD由docs-only提交向前，不得把其Android skipped等同#4406真实编译。**
- **唯一审计台账**：原`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`新增F12-O。现行范围从450整退7、增加2=>**445项**；从209已审退出2旧已审、增加2新增已审及2原待审首审=>**211/445（47.42%）已审、234待审、0审计中**。原已审UI/AI代码只复核不重计。AI首夜/后夜/pair/numeric/strategy/context/解析/transport/preselection/辅助/自动模式、两种跨局防重复、唯一GameState/Session、实际死亡确认/秘密展示和安全原子提交不改，未付费API、未真机。
- **下一任务**：继续横向234项全文件遍历，优先Undertaker/Ravenkeeper当前仍从全脚本roleRevealOptions生成的旧角色候选菜单，考虑像Sage一样按角色直接选/接AI；随后解决夜间Sage/Ravenkeeper和Mayor还依赖DemonAttackProductionAdapter只读预测：真正死亡由夜末ClocktowerNightResultConfirmation确认，不能把恶魔攻击目标当实际死者直接删除adapter/规则，须保持正确真人唤醒与信息收到。继续App/HostScreen>50KB实际无用职责，以及Slayer/Virgin旧自动规则/终局/人工作用路径，保留AI和双轮换。不重建旧Host规则以取悦旧测试。PR #334仍Draft、base `codex/minimal-host-m1c-direct-number-20261018` SHA `64be3090`，不merge/retarget/force push，不做未经授权的付费API/真机。

- **最新F12-N代码检查点**：MiniMCP别名`clocktower`，代码起点前一docs HEAD`9f11548825903330d76700c08d7ccb041e933707` clean/equal；推送`e49972e1268b483dc273301f4287f760772d0ffd`（`[full-ci]`），删除无人用于生产的Host红鲱鱼singleton伪裁定函数和测试、删除旧夜死亡预览的死getter/引用/重复判断及未使用的全配对临时ID；精确GitHub比较**6文件+2/-103，净删101行**，0整文件删除。正式代码Android [CI #4403 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39188307015)与[R2 #3983 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39188307113)，Android tests/ASP/Clingo/gate全通过，无付费API/真机。
- **审计量与严格限制**：唯一原台账`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`的F12-N新增11个原待审生产/测试全文+consumer结论；**209/450（46.44%）已审、241待审、0审计中**，不把历史子阶段累加成当前库存。PR #334依然Draft、原base`codex/minimal-host-m1c-direct-number-20261018` / SHA`64be3090`，不得merge/retarget/force push。旧`DemonAttackProductionAdapter`仍是真实Sage/Ravenkeeper触发的唯一预览生产链，真正`ClocktowerNightResultConfirmation`在最后才确认实际死亡；**先建早于唤醒的真实Host选择/事件/安全步骤，再移除旧推演及其专属测试**，不能把攻击目标当死者或破坏AI实时依赖。
- **下一批**：横向余241文件重点night result的真正触发重连、first-night 3角色全枚举pair手工菜单及对应旧domain，继续审计/缩减App/HostScreen>50KB与其它Slayer/Virgin/Mayor旧Host自动规则。AI首夜/后夜、gpt请求API/prompt/schema/解析/策略memory与预选/辅助/自动模式、两种跨局防重复、唯一GameState/事件、隐私/幂等/原子安全继续保留。允许实删代码、全CI/R2；不能为旧测试恢复规则候选。以下后续docs-only HEAD不能冒充代码CI检查点。

- **最新F12-M完整代码检查点**：先核实`clocktower`与远端在`codex/minimal-host-full-scan-20261018` clean/equal，起点`c3ea51af96a2c44429bd8f4782e0c6eb61d6c2c1`。真实代码提交推送`ba8ea494acc9aada4e4ead4168734de4f60d8894`，在App中Klutz只提交实际选择并统一继续游戏；NoGreaterJoyKlutzHistoryProducer去掉旧Host阵营/候选计算，整删`NoGreaterJoyKlutzChoiceRuleV1.kt`及混合LiveKlutzScriptReachabilityTest的旧专属裁定断言。GitHub独立对比**4文件+30/-162，净删132行、整删1生产文件**。精确代码[full-ci] **[CI #4401 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39185744458)**（Android tests+Debug APK、ASP/Clingo/CI gate全绿）与**[R2 #3981 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39185744492)**。未真机、未付费API，所有活跃AI/唯一GameState事件和两类跨局防重复不改。
- **唯一台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-M新增13个原待审生产/测试首次全文语义结论，退休原已审KlutzRule 1项并移除当前索引，故**198/450（44.00%）已审、252待审**，0审计中。保留现有DemonBluff三身份、开局AI匹配及review、FT直接YES/NO、Drunk可见角色、Host魔典与事实投影；DayNominationGesture的alive-only和InformationStepBuilder的manual/auto候选仍标待解耦。Klutz选中邪恶玩家后如今不会自动宣告胜负；说书人裁量，不得误称已实现单独显式终局入口。
- **下一步任务**：继续横向252项；优先真实App/HostScreen>50KB源职责删除、拆断`DemonAttackProductionAdapter`旧Imp/Monk/Soldier/Mayor死亡预览→NightHostProjection→Sage/Ravenkeeper触发，但必须先保证这两角色仍有真人唤醒、座位选择、AI和实际信息公布途径；审日间Slayer/Virgin/Mayor/旧自动终局并尽可能移除不可替代性低的门禁。保留当前阶段推进、Session事实/隐私/幂等、真实AI首夜/后夜/pair/numeric/prompt/transport/parser/memory/preselection/辅助自动模式，以及两类setup防重复。不为旧测试恢复退休规则。每批代码提交推送及完整Android CI/R2修失败，PR #334仍Draft/base`64be3090`，无授权不得merge/retarget/force push/付费API/真机。此后docs-only HEAD不能替代`ba8ea494`实际Android编译基线。

- **最新F12-L代码事实**：起点MiniMCP `clocktower`与远端clean/equal `b4c979ac143d25c455e0d7011705f0800cd1690f`；分支`codex/minimal-host-full-scan-20261018`，PR #334 Draft且base `codex/minimal-host-m1c-direct-number-20261018`（SHA `64be3090`）不变。两次实际代码提交并推送：`c420bd9e2dd4b0e7ebaa8436aa956a55f4f101e9`清Undertaker/Ravenkeeper Night UI不可达LegacyUnreliable/Direct来源/compat，完整 **CI #4398/R2 #3978 SUCCESS**；`4e1c9d40e72acf6a08abb3f6a843c3d0be9d091b`整删无生产调用的旧不可靠数字历史选择函数及唯一专属测试，完整 **CI #4399/R2 #3979 SUCCESS**（真实Android tests、ASP、Clingo、gate全绿）。GitHub精确比较7文件+8/-155、净删147行，其中整删1生产+1专属旧测试。后续本handoff/docs-only提交会令HEAD前进，**不能把docs-only CI当作比此代码更新的Android编译证据**。
- **唯一台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`新增F12-L及9个全文+消费者审阅结论，现**186/451（41.24%）已审、265待审、审计中0**；删除路径退出逐文件库存，历史仅在F12-L。NightStepUi、DemonAttackAdapter本轮已全文件审阅并标待解耦；PlayerDisplayResolution+测试、Drunk展示身份、phase mapper保留；Klutz真正方桌选择保留但判输赢尚需退出规则门禁。Sage直选、两类跨局防重复、现有AI首夜/后夜/配对/数字/提示词/真实API/解析/战略记忆/预选/辅助和自动模式、唯一Session及隐私不改。未进行付费API/真机。
- **下一优先序**：从台账剩余265路径继续横向首审epistemic/codec、setup、平台、transport、测试与tools/CI，不能重复算已审项。清Host日间Klutz规则自动胜负（保留公开选择实际Session事件与继续步骤的可用替代）、夜间DemonAttackProductionAdapter预测链（先保证Sage/Ravenkeeper有可用实际唤醒/信息展示入口，再删触发规则推演）、NightStepUi中仍被使用的manual/automatic候选域及其它可替代UI/DTO，和App/HostScreen>50KB大文件真正无用职责。不为通过旧测试恢复已退休Host Rules。每代码批提交推送、跑精确完整GitHub CI/R2并修失败，不merge/retarget/force push，未经授权不做收费API/真机。此处未宣称P1/P4/P5完成。

- **F12-K Sage子阶段实际完成**：从clean+sync `4b29f45117119299f6ed3c395b3f8a4e9556fa35`出发，提交`9d962565b18e53b41713d639129e1ec8f6af253e`将旧全pair真/假枚举、LegacyUnreliable候选菜单替换成Sage方桌直接轻点两名实际玩家，保留有效AI两座位建议的一键确认、现有onShowPlayerDisplay/私密观察原子记录；Sage手工结果使用typed two-seat AnyOf并不强行标truthful。进一步提交最终真实代码`9c7c57a7369d9ee76324b009d26d088a66660923`，彻底删掉`storytellerPairHint`旧默认恶魔+第一人配对、删Sage初始化虚构已选命题和无用传参。真实GitHub对比7文件**+163/-558、净删395行**（本轮无整文件删除）。代码两次完整验收：首CI #4395/R2 #3975 SUCCESS，最终 **[CI #4396 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025505)**（Android full JVM unit tests、debug APK、ASP、real Clingo、CI gate）、**[R2 #3976 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025502)**。无真实付费OpenAI API/真机测试。
- **保护项重申**：User明确要求保留两种跨局体验：①阵容/展示角色尽量避免连局重复（SetupDiversitySelector/History/TemplateRepository）；②同一玩家不同对局尽量避免分到相同身份角色（TB DealPlanner、PlayerStartingIdentityHistory、rotation record/store）。禁止重新以“游戏仍能启动”为理由删除它们。仍须保留现有首夜/pair/numeric/后夜AI推荐、真正的调用/提示词/返回parser/战略记忆、所有MANUAL/AI_ASSISTED/AI_AUTOMATIC模式、唯一Session/GameState、结构化实际行动/公布/角色/存活历史、stale/privacy/idempotent/atomic safety。
- **最新唯一横向审计台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-K，**177/453（39.07%）已审、276待审、0审计中**。F12-K新全文首次审`ClocktowerHostCoreSemantics.kt`1项；已经审过的4项Sage生产/测试与HostScreen不重计；`ClocktowerNightStepUi.kt`虽然被窄修改但整文件仍待审。代码没有删文件，分母不变。未来要继续覆盖真正尚未审过的276项，而不是拿净删行数代替全仓审计。两个>50KB文件App/HostScreen未完成减法，P1/P4/P5仍有剩余，不能宣称整个P1完结。
- **下一轮必须实际实施（F12-L起）**：先用GitHub Connector+MiniMCP `clocktower`检查最新PR #334/head/base、branch/index/clean、remote sync及最精确CI/R2，不重用本段SHA当docs最新HEAD。横向对全仓剩余未审文件审计（优先`ClocktowerNightStepUi.kt` ~39KB及配套legacyInformationCandidates/旧automatic/manual-domain语义、`DemonAttackProductionAdapter`预览链、Sage/Ravenkeeper触发推导、日间Klutz自动判胜负、测试/工具）。对“删除后已有方桌、说书人手工、唯一Session及AI仍能继续游戏”的旧模块，先移除调用再整删与独占测试；不可仅因生产调用而保留。持续检查50KB源文件上限，针对真实废弃职责减法，不靠机械拆文件达标。**确实改代码、提交推送、精确完整Android CI+R2失败即修**，之后更新本唯一台账/roadmap/handoff。不合并、不retarget、不force push PR #334或其它堆叠PR；不调用付费API/不做真机除非后续明确批准。
- **切换注意**：本交接/审计文档另有docs-only提交，GitHub新的文档CI/R2即使成功，Android/ASP/Clingo通常跳过，不可冒充精确代码`9c7c57a`的完整验收。要核对当前真实HEAD与同步关系。

- **F12-J正式代码验收**：原分支/远端Clean、HEAD `2210130d9f89fc03de0f0345d809135748ef7718`。代码提交 `8582fe7a435c2f4c01c84b3a9468f25bc63c4541` 完整Android CI #4392/R2 #3972 SUCCESS；接续提交 **`f822ef5b9174c2e5d38b4704dca68af1042c9c72`** 完整 **[CI #4393 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966455)**（Android全套单元测试、Debug APK、ASP、真实Clingo和gate）及 **[R2 #3973 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966521)**。GitHub比对13文件**+13/-243，净删230行**。2生产文件（ClocktowerNightTargetLegality、ClocktowerRuleOnlyCompactChoice）与2项只保护旧实现的专属测试整删，3个信息角色single-choice专用compact UI与已孤立BeginnerSingleTargetRevealDialog切除；Mayor仅false的automatic/旧ImpSuccession展示退役。所有现行AI建议/真实Session提交、Mayor事件、两种跨局防重复、夜间按Next继续及手工/AI信息确认保留。无付费API或真机验证。
- **最新全源台账**：在F12-I `171/457`上删4路径，其中1已审、3待审；另6个此前未审UI/Presentation/测试已实际全文+consumer复核，故**176/453（38.85%）已审，277待审、0审计中**，单一路径库存详见F12-J段。HostScreen/SageSquareTable以前已审本轮不重复计，NightStepUi的大规模源码继续待全量横向复核。
- **继续清理的边界**：两类开局体验为正式硬保留：阵容防重复+同人连续身份轮换（SetupDiversitySelector/History与旋转planner/history），不可当多余规则校验删掉。SageDisplayOptions全合法pairs仍为手工下拉的当前生产来源，必须先接通可手工选择任意两座位的真实可提交信息路径，再整删其枚举器/专属旧测试；其它旧夜晚规则preview/手工目标拒绝、Klutz旧自动终局与两个超50KB源文件仍待推进。Monk本轮直接选择真实座位，已不阻止选自己的情况，说书人自行判断游戏规则；这里是职责迁移而非正式改变游戏规则。Draft #334 base为`64be3090`，绝不合并/retarget/force push。
- **注意**：后续提交本交接的docs-only CI不等于完整Android二次构建，精确产品代码通过证据须引用f822ef5b的CI #4393。

- **所有者新增硬保留要求（2026-10-19）**：开局前的**角色阵容多样性**（不要连续重复相同角色/展示阵容）与**同一玩家跨局角色轮换**（尽量不要连续获得相同身份）必须共同保留，属于实际产品体验，不能再以“删除也可开局”为理由删除。曾在本轮未提交工作树中尝试删SetupDiversitySelector/History、TemplateRepository及其测试、断开TB历史调用；接到纠正后通过远端真实原HEAD逐字恢复，已验证工作树clean，没有commit/push这些误删。必须保留TB开局prep中的history、近期PlayerStartingIdentityHistory、rotation record/store、DealPlanner的minimumRotationCostAssignment及对应测试。不要擅自对其改成单纯随机。
- **本轮F12-I生产代码已验收**：基线f8cdf8af，提交`eb702ff6387c8b4e28425986e0e089401028130a`（旧PlayerHistoricalTimeline完全没有生产consumer，移除并缩窄混合测试；Mayor仅singleton合法结果自动选定/自动Next及Authority整体退休），**CI #4389/R2 #3969 SUCCESS**。随后提交`69f1bf31df067e3c0d586f6e4a5f93d77d3781cc`（FT Boolean始终YES/NO，删旧Host唯一合法项auto分支/测试，保留AI建议优先和人工确认），**[CI #4390 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608692)**（full Android JVM+Debug APK，ASP/real Clingo/gate均通过）、**[R2 #3970 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608628)**。从f8cdf8..69f1bf合计13文件+29/-610（**净删581行、3生产+3专属旧测试退出**）。没有付费API或真机；现用开局/后夜AI、Session实际Mayor/FT结果、私密事实/版本安全均保留。
- **台账**：原463源路径减6个删除文件、净新增3个曾待审的真实合同全文审计，现行**171/457（37.42%）已审、286待审、0审计中**，逐路径见唯一MINIMAL_HOST_FULL_SOURCE_AUDIT的F12-I；文档将有独立提交导致HEAD前进，勿把docs-only CI当成上述代码验收。
- **后续**：在两类防重复保留的前提下，继续审已标需要解耦的旧规则生产调用。Sage全pair枚举仍被真实Sage UI消费且当前用户无法从该UI直接结构化选择任意两座位，**应先迁移到直接手工pair/AI确认再整删候选生成**，不应直接砍掉还能公布信息的入口；随后Ravenkeeper/Monk目标门禁、Klutz自动结算、日间规则判输赢、老夜间预览及超50KB组件。Draft #334维持原base，未批准不merge/retarget/force-push。

- **新产品删减准则（所有者2026-10-19确认）**：旧模块即使有生产调用，若取消后仍能按现有方桌/人工确认/Session事实/AI推荐正常游戏，就先移除/替换调用再整删该旧模块及专属测试；绝不能单凭当前有调用判保留，低概率特例暂交给说书人处理。真正无替代且关系到继续游戏、唯一事实/隐私/原子性与正在运行的AI链才保留。此项已写入AGENTS.md、HOST_RULES_AI_REBUILD路线和TESTING_STRATEGY。
- **F12-H已经实际实施并全验收**：从clean`fc80fd1a`开始，代码提交`7982b654`移除Spy独立Show Grimoire页面与按钮、首夜/后夜生成的GrimoireState/ReminderToken镜像、独立中毒Spy规则界面、Spy观察导致红鲱鱼提前的过度依赖、相应JSON/历史copy，以及零引用PairInformationOutcome。Spy现在在原方桌上显示唤醒指定座位、拿手机给玩家看、收回再按原Next；未新增确认按钮，普通Spy身份/登记/其他信息记录保留。2个生产文件+4个专属旧测试文件整删。最初CI #4386旧测试要求Spy前确定红鲱鱼失败，修正测试而非恢复旧依赖，最终代码HEAD **`f7f47d1e28b5fd74db61179a3c22736f6ca04d3d`**，GitHub compare**18文件+20/-869、净删849行**，精确 **[CI #4387 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098629)**（Android full JVM+Debug APK、ASP、Clingo、gate）、**[R2 #3967 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098663)**。无真机、无收费API。
- **台账现况**：唯一源路径 `469-6=463`；172已审、291待审、0审计中，无重复；6个已删除原路径只保留在台账F12-H历史说明。App和HostScreen仍超过50KB；P1未完全退出Host Rules。本文档提交会让PR head前进，后续必须现场复核远端/本地SHA、分支干净和CI，docs-only检查不能替代上述代码CI。
- **后续批次建议**：按照新标准审PlayerHistoricalTimeline孤立公开历史投影（迁移需要的真正保密测试）、Mayor/FT“合法结果singleton自动裁决”、Sage pair全候选生成、跨局SetupDiversitySelector+角色轮换DP、NGJ Klutz自动gameOutcome和剩余旧夜间规则preview；按功能是否中断真实游戏判保留必要。仍需继续横向未审291项、清理两个超大UI、核跨项目TBGS合同。Draft PR #334保持不合并、不变base、不强推。

- **F12-G最新代码证据**：在已同步的MiniMCP `clocktower` 分支`codex/minimal-host-full-scan-20261018`实施，起点`4da03849`，第一代码提交`0d8f145b`删旧Formal世界模型/JSON/时间线绑定及专属测试。首轮CI #4383因误删Grimoire immutable-list使用的`java.util.Collections` import编译失败；精准修复提交最终代码 HEAD **`8644e7575b67d9f3bef6cc269c7c93bca4332bf1`** 已推送。GitHub独立比较7文件**+8/-737（净删729行）**。完整 **[CI #4384 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056035)**（full Android单测+debug APK、ASP、真实Clingo、CI gate均通过）和 **[R2 #3964 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056032)**。该精确代码之后本交接文档另单独提交，下一轮按GitHub PR实时head/base/CI核对，不把docs-only CI冒充代码验收。
- **F12-G进度**：唯一台账 **177/469（37.74%）已审、292待审、0审计中**，7个原待审epistemic测试全文审计并分类，删1个原待审Formal旧测试，原有4个已审生产文件只做复审/代码缩减不重计。事实/全球事件/recipient保密、唯一Session、现用AI开局/后夜/策略记忆与模式未改。
- **下一组明确任务**：首先核对并迁移`PlayerHistoricalTimeline.project`中独有的公开历史/recipient隐私断言到现用生产历史结构，再完整删除该只在测试消费的旧投影与旧专属测试。继续横向剩余292文件，随后攻Mayor及FT单候选Host规则门禁、Sage/Ravenkeeper预览规则、TB setup rotation评分与App/HostScreen超50000B清理。保留跨项目真实合同，未知功能U1在所有者定夺前不能删除；不merge、retarget、force-push Draft #334。

- **本轮F12-F代码已推送**：分支`codex/minimal-host-full-scan-20261018`，Draft #334（base SHA 64be3090），代码提交**`e6faea27bb28a571b45669bf2357af0b11e4f863`**（从56975956前进）。实际净删2个3B2/3B3 Python实验脚本、2个专属测试、1个过时P1分支工作流及主CI中调用旧实验测试的6行；GitHub compare 6文件0新增/1087删除行，旧台账文件大小估计共52,165B。未改Android生产、正式AI请求/传输/解析/战略记忆或Session事实。代码级[full-ci] **[CI #4381 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379709)**（full Android JVM+Debug APK、ASP、Clingo、gate均SUCCESS）、**[R2 #3961 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379711)**，已核精确HEAD和Android job steps。后续docs-only HEAD不可冒充代码编译证明；新对话仍应重新核对GitHub实时状态。
- **全源审计**：F12-F新增32项保留路径的首次全文语义结论，连同旧5个已退休路径从现行目录表移除。当前**170/470（36.17%）已审、300待审、0审计中**；470路径每项唯一。审核结论只维护在原台账F12-F与原表，不另建库存。当前代码后还将另有本文档提交，最新Git HEAD必须重新读取。
- **下一组重点**：epistemic内部旧FormalGameState/PlayerKnowledge与相关JSON可优先隔离，现用InformationProposition、RecordedObservation、TimelinePoint、global AI history/隐私必须保留；孤立PlayerHistoricalTimeline只在迁移高价值公开历史测试后删。setup真实TB/NGJ发牌、Drunk已选身份、AI模式/密钥入口不能误删，跨局rotation/候选评分宜整组缩减；Mayor旧合法单选、Sage/Ravenkeeper触发和日间自动终局仍有生产消费者，继续P1。TBGS快照JsonCodec在Android生产无调用但跨EvidenceLab/WebHost契约未核，不贸然删除。

- **当前F12-E状态**：MiniMCP `clocktower` 干净分支从`a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`精确快进到远端`2ce0e897898e8f040c3bef8dcbda20186e2e4689`，随后全文审计原待审16项，唯一台账 **138/475（29.05%）已审，337待审**；本批只修改文档、不改生产代码。当前HEAD之后的本交接/台账文档提交号需下次实时核对。GitHub PR #334保持Draft，base仍64be3090。同步后CI #4379/R2 #3959成功但Android跳过；代码完整Android证据仍是0f904f70 CI #4377/R2 #3957。
- **历史注意**：下文旧Windows/Linux不同步状态是F12-D交接时的历史现场；本轮已在Linux开发机快进，不可误用旧a2cbcac8作为当前HEAD。

- 分支 `codex/minimal-host-full-scan-20261018`，Draft PR #334，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F15起点本地/远端 `9f6c4af245b3aa7424e1134ad1789be6520181bc`，docs-only CI #4376 / R2 #3956 SUCCESS；精确代码 **`0f904f7031770bd2f12acea4299aa9b1d5c8e5c0`** 的完整 Android **[CI #4377 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759682)**（full单测+Debug APK）、**[R2 #3957 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759696)**；ASP、真实Clingo、CI gate均SUCCESS。 远端compare核实51文件差异，PR仍Draft/base未变；后续docs-only HEAD也不能冒充Android验证。
- F15本地 `:app:testFull :app:assembleDebug` SUCCESS：175 suites、741 tests，0 failure/error/skipped；没有真机或付费API验收。
- Windows工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`，PowerShell，`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr`，Gradle使用gradlew.bat。Mini MCP clocktower是独立旧Linux `/home/opc/repos/CampBoardGameHost`、同名分支但HEAD `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，不得覆盖本地或假设同步。
- 原未跟踪文件原样保留：artifacts/branch-audit-2026-10-04/、artifacts/test-audit-2026-10-04/；docs下2026-10-04的BRANCH_CLEANUP_AUDIT/RESULT、MAIN_SYNC_VALIDATION、TEST_RETIREMENT_AUDIT/CLEANUP；node_modules、package.json、package-lock.json。未创建工作树，未真机/付费API验收，不merge/retarget/force push或删除分支。

## 转入开发机 MiniMCP 的接续说明（2026-10-18 同步）

本轮所有者要求同步文档并转入新对话，后续在开发机通过 MiniMCP 继续；本次只整理交接，不新增生产改动，也不提前启动下一批审计。

- 本次同步前远端/Windows HEAD 均为 **`8f8f9573d452b92cdd727fc7fd98906a1446cb26`**（F15验收记录及F12-D横向审计）。该文档提交的 [CI #4378 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39122159425)、[R2 #3958 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39122159339) 已复核；它们是docs-only检查，完整Android代码证明仍是上节 `0f904f70` 的 CI #4377 / R2 #3957。本节所在提交是后续交接文档提交，进入新对话必须从GitHub重新取得最新HEAD及其检查，不能把此处基线当最新HEAD。
- 本次只读实查 MiniMCP `clocktower`：目录 `/home/opc/repos/CampBoardGameHost`，分支 `codex/minimal-host-full-scan-20261018`，HEAD **`a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`**，index tree `4d2cdd8b643f23520222a1d9ce540941d3c6b370`，工作区干净。此状态尚未快进到本轮远端；所有expected HEAD/index/worktree版本令牌进入新对话后都必须重取，不复用本次快照。
- 接续顺序：先用GitHub检查main、Draft #334的实时head/base和对应CI/R2；再用MiniMCP `git_state`、`git_remote_status`核对开发机。确认无本地改动及未发布提交后，`git_fetch`获取配置远端，重新读取refs/state，使用精确预期HEAD与目标SHA执行同名分支 `git_fast_forward`。若不是可安全快进，先检查分叉/本地差异；不要reset、强推或覆盖工作区。同步后再次 `git_state`，确认开发机HEAD等于GitHub的最新远端HEAD，再全文读取本交接、权威架构、路线、测试策略和原审计台账。
- 本次未切换或修改MiniMCP开发机分支/文件；Windows原有未跟踪文件没有提交或迁移。开发机Android/JDK/Gradle能力需实查，不能套用Windows JAVA_HOME或声称开发机已运行741项测试；当前独立Android接受证据来自精确代码HEAD的GitHub完整CI。
- F12-D历史交接建议当时是**继续原台账的横向全文件语义审计**：当时 **122/475（25.68%）已审，353待审**，先从未审epistemic/codec、catalog/setup、平台/transport、tools/CI及测试选一组，全文读取并验证真实消费者，更新原表；不是重复已审夜间链，也不是重做F15。下一批再依据审计证据选择完整删除职责链。全部现用AI必须保留，两个超大文件和剩余Host Rules问题见下文。

## F15 已落实：实际结果独立确认

- 增加NightResultConfirmation界面：明确无人/多名死者、任意座位实际成为Imp、原已确认中毒keep/clear；取消/复核、gameId绑定及双revision过期及防双击。最终实际结果不从攻击/保护/Mayor/Soldier/SW重算，不因合法候选拒绝；新身份沿原私密展示与实际RoleChange确认，Dawn从实际Death名单播报多名。
- Session.commitConfirmedNightDeaths用现有纯Action/Observation函数暂存全批预检后一次publish state，未知/重复/stale/后续记录冲突不留半份结果；原Attack和实际Death分开，不再Death回写attackSeat。NoNightDeaths只表示用户明确无人死亡，JSON/canonical/public/AI delta完整支持，旧历史缺失不反推空结果。角色/毒/保护等不进入公开死亡播报。
- 整删2生产：NightDawnResolutionPlanner、TroubleBrewingDemonSuccessionResolver；旧DemonSuccessor action/合法候选UI和Session/checkpoint候选元数据退出。DawnPoisonCarryIntent移入原PoisonBoundaries，只保留明确keep/clear输入，夜间不按Poisoner死亡/转职自动推导清除。10个仅保护旧规划/DTO的测试文件删除，真实Session/AI/保密/stale/幂等测试保留，新增5个实际合同用例。
- 生产净删642行/30217B、测试净删1300行/47578B，共1942行/77795B。当前883跟踪文件，241生产+178测试Kotlin；F15完工时475项、116已审计；之后F12-D已新增横向6项，当前122已审计（25.68%）、0审计中、353待审。每路径状态及删除历史只维护在原台账F12/F15。
- App159449B、HostScreen99682B仍超50,000B；Session42993B、新界面4673B未新增超限。P1/P4未完成。U1无生产入口的自定义导入仍待所有者决定，本批无新增决策阻塞。

## 下一任务：从F12-K继续未审276项，进入F12-L横向清理及超大模块减法

**当前两条并行准则**：旧功能生产有调用不构成保留理由、可以用真实Session/人工或现用AI等价继续游戏则删除；但开局角色阵容防重复与同一玩家跨局身份防重复已被所有者明确认定必须保留，绝不再次擅删。F12-I退役public影子历史与Mayor/FT旧单例真值门禁，现行171/457已审、286待审；下一轮从286项继续横向审计并处理有等价可用入口的旧模块，不能用净删行数冒充完成全仓审计。** 保持唯一现行台账，横向覆盖epistemic/codec、catalog/setup、平台/transport、tools/CI和测试，全文读取并验证真实消费者后再给最终分类；不重读已审文件冒充新进度。再依据横向结果选规模大、证据闭合的一组退休。

F15未清空所有Host Rules：DemonAttackProductionAdapter仍仅给现有信息步骤作只读死亡预览，Sage/Ravenkeeper触发/毒展示、日间自动继承/终局、Klutz/日落清除及旧Flow模板仍有消费者。最终实际死者可与早前预览不同，后续UI补充信息与触发流程仍需审计及真机验收。新NoNightDeaths的跨项目消费者仍需完整合同复核；不要声称已做外部服务/付费/真机验证。夜间已退出旧规则顺便自动判终局，实际结果提交后允许继续；日间终局规则尚在。

现有AI开局/pair/数字/后夜、prompt/真实IO/parser、strategyMemory/preselection及双模式信息采纳必须保护；不恢复已删除web/world引擎或独立causal journal。每批报告实际删除、移除链、净体量、超限、Host事实/事件风险、精确HEAD CI/R2和未知所有者项，保持Draft堆叠、不merge/retarget/force push，原未跟踪文件不动。
