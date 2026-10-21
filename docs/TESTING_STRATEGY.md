> **真实 App 最小整局（2026-10-21）**：新增 `ClocktowerCompleteAppUiTest` 3 项，使用生产 App、准备器、真实长按提名/逐座投票、同一 Session 和真实私有 AtomicFile，从首页/发牌到终局/恢复/下一局；另一项覆盖发牌中断重做。只固定 seed，不替换游戏回调；修复前 3 失败、修复后通过。Cloud full 654（147 suites / 38 UI）、APK 与原 R2 shell 通过，生产代码 `e73e4807691ab974dddfa86532a30efe89973753` 的[完整 Android CI #4503](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618033) 与 [R2 #4079](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618124) 均 SUCCESS，已独立核对全量 Android/APK 上传、ASP、Clingo 和 gate，范围和下一阶段见 [验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。实际设备被杀进程/付费 API 未运行，不能冒称已验收。

> **单一对象整局验证（2026-10-21）**：本批删除 SessionView/NightCheckpoint 投影依赖，所有真实 Host UI 测试都以空 setupCards 从 Session 显示玩家；新增两剧本从首夜到正式终局和重读的代表轨迹，以及 3 项开局镜像无关读取回归。整局测试使用真实 Session 和 JudgeScreen，提名输入调用真实 Session，未涵盖整个 App 发牌页面/设备/全部阵容；最终 Cloud full 651（146 suites / 35 UI）、APK 和原 R2 shell 已通过；精确生产代码 `3268440fd312e1bdf9ad3278013207b4f8af24af` 的[完整 Android CI #4501](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39561245755) 与 [R2 #4077](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39561245733) 均 SUCCESS（full tests / Debug APK 上传、ASP、Clingo、gate 均已核对），不能冒称第一阶段全部完成。

> **2026-10-21 最新验证优先级**：先证明两剧本真实入口可达、从创建到终局的最小完整游戏与核心安全续局，针对阻断/机械结果/事实/隐私/原子性保留高价值回归；再为全仓大规模删除验证保留行为；UI 细节与全面视觉/交互精修验收放在清理后。每个受支持角色仍须有最小可执行路径，但不要求先穷举所有阵容或每个按钮的细节。原 646 / CI #4498 / R2 #4074 只是已验收代码基线，不是整局闭环已完成证明。详见 [路线](CURRENT_DEVELOPMENT_ROADMAP.md)。

> **夜间职责接续验证**：新增 3 项真实 JudgeScreen 继任交互与 3 项 Session 继任/保护/过期状态回归；本批完整 Cloud 646（146 suites / 33 UI）、APK、原 R2 shell 与 diff 检查通过；生产代码 `9b3cee4c5ba8fa405080cd0929726e66ffd6c324` 的[完整 Android CI #4498](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39557618164) 与 [R2 #4074](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39557618174) 均 SUCCESS（包含 full tests / Debug APK、ASP、Clingo 和 gate）。旧 640/4496/4072 仅证明前一 F2。继任 query 不产生事实，确认仍重新读取 Session；共同 materializer 不决定夜序。没有新增专属测试文件或忽略测试，未运行真机/杀进程/付费 API。

> **F2 接续验证**：投毒来源死亡/身份退出问题已迁入实际 Session 的耐久回归并修复；新增来源 9 项、真实 AtomicFile 1 项，旧时间线回归拆分为两种执行语义。Cloud full 640（146 suites，30 UI）/APK、原 R2 shell 通过，精确代码 `6a76a57c` 完整 Android CI #4496 / R2 #4072 SUCCESS。此前临时来源死亡失败证据是已修复前历史，不再是当前已知未修复项。

> **独立审计修正**：新增设置 UI 回归 3 项，修正前 2 失败、修正后 full 629（30 UI）/APK 成功。另一个临时真实 Session 诊断反例证明 Poisoner 死亡后的中毒清除缺陷；它是单列的失败证据，不属于 629 通过数量。该反例已由首段 F2 批次迁入 Session 回归并修复；629 是此前数量，当前完整数量为 640。详见审计报告。

> **2026-10-21 Codex Cloud 本批**：最新设置职责收敛与无生产调用规则退休已实跑 full 626（27 UI），0 failure/error/skipped，Debug APK 和原 R2 shell 成功。13 项孤立旧规则测试与生产实现一起退休，真实 Session 规则/私密信息/原子性测试保留。当前工具链先 source `/workspace/.cloud-setup/activate.sh`；只读 home 的 Robolectric 测试使用工作区 user.home 与原代理/CA。下方 Windows/旧数量均为历史，最新远端精确 SHA 验收见当前交接。

> **2026-10-21 当前验证**：代码 `f4d78d5c70c55be988c1daf29558fde2e0c82305` 已提交/推送，Artist 测试旧接口编译失败已修复。本地 fast 612/full 639（27 UI＝原 23＋4 Day）、Debug APK 和原始 R2 脚本成功；[完整 Android CI #4490](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956510) **SUCCESS**；[R2 #4066](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956507) **SUCCESS**。新增 DayFlow 15 项、真实文件 Store 8 项、两剧本支持人数的两夜轨迹，保护 B/C、确定白天效果、稳定轮次幂等、保存失败回滚、私密收件人和恢复后的事实进度。真机/杀进程/付费 API 未运行，全部源码大小和全树零遗留尚未完成。下方“在途编译失败/未提交”段是历史检查点，当前下一步见交接。

> **2026-10-21 在途验证状态**：P3/原子保存改动仅在本地未提交工作树。最近 `:app:testFast` 在 `:app:compileDebugUnitTestKotlin` 失败（ClocktowerArtistDraftTest 旧回调签名）；测试尚未运行。新增 Session DayFlow、GameEnded、Artist 私密事实、JSON/AtomicFile/恢复入口均未取得本轮 full/APK、故障恢复或精确代码 CI/R2 验收；下方 `311191a3` 的通过只属于上一批。当前接续见 [交接](NEXT_DEVELOPMENT_HANDOFF.md)。退休 Slayer 任意判死 UI 的专属测试和 R2 旧字符串合同应改由真实 Session 机械/裁量行为保护，不能仅修改字符串过门禁。

> **后续净删/全夜控制检查点（2026-10-20）**：代码 `311191a30d6b0d83b5cf2d6de2a67905b526e596` 已将全夜步骤切换到 Session 事实，物理删除旧 UI 游标、Dynamic/Fixed 与 Planner/Projector 链，并执行无用途净删；当前说明见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 和 [路线](CURRENT_DEVELOPMENT_ROADMAP.md)。本地 fast 593/full 616（含 23 UI）及 APK 通过；精确代码 [CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS / [R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS。下方 af35cee9/21 UI 及“随后才替换全夜路由”的文字是此前检查点；当前合同不变，剩余 P2 全轨迹、P3 B/C、持久恢复、大小/P5 尚未完成。用户先前真机反馈不等于本批新 APK 验收。

# CampBoardGameHost Testing Strategy（2026-10-19，Minimal Host Rebuild）

> **最新代码验收（2026-10-20）**：`af35cee951539df689552fb3c3ff97b7b2b5861e` 的 [完整 Android CI #4484](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558605) 与 [R2 #4060](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39411558519) 均 SUCCESS，Android full/APK、ASP、Clingo 与总门禁通过。本地 fast 671 / full 692 tests（含 21 个真实 Compose 交互用例）及 APK 通过。D2 系统 Back、全屏 Next、显式重试及真实查询的战略检查缺口已修复；详见 [D2 验收矩阵](D2_INTERACTION_ACCEPTANCE_2026-10-20.md)。这是代码/自动交互检查点，不是设备/像素视觉/现场或付费 API 验收。

> **现行验收合同**：[Minimal Host Architecture Rebuild](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md) 覆盖下文旧三模块/F12/P1-P5 流程描述；下方通用证据优先、不强制机械 TDD、CI/Android、安全/隐私和 AI 回归原则仍有效。不要为了让旧实现测试继续通过而保留过时组件。
>
> **现在的阶段 P0→P5（用户 A–D 裁定后的唯一当前执行标准）**：P0 全树职责归属、角色输入/触发/结算及退役矩阵；P1 唯一 Session + 本步 Next 即时提交 + 稳定步骤身份 + 原子落盘 + 安全续局，**无 pendingOperation、无夜间 Previous**；P2 全夜间唤醒/跳过/即时死亡/中毒保护时序/继任以及 **A 无 StartNight/ReadyCard，D2 信息可多次全屏预览/返回且仅 Next 记录最终一条 Observation**；P3 白天提名、投票、鬼票、处决及胜负，**B 方桌同页一次结束，C Klutz 从 Death/Execution 推导，不记录独立 Learned**；P4 UI 无规则写入、原 AI 人工/辅助/自动同提交入口、AI stale 版本指纹；P5 全树删除证明、5–15 人两内置剧本完整游戏与真机验收。详见 [2026-10-20 决策与审计](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md)。
>
> **强制行为测试（已取代旧 stage/back/dawn 测试）**：夜间目标在**当前页面确认前**可改，点击当前 Next 立即提交；点击后无上一角色也无法改写已确认事实；夜间没有准备页、Next 后等待阶段交接、Dawn 页面或额外死亡公开观察；D2 信息全屏打开/返回只更新当前 UI 预览，不写 Observation、不自动跳角色；改选可再次展示，**唯独本步 Next** 原子记录最后预览的一条信息且稳定步骤 ID 幂等；真人改变查询目标使旧预览/旧 AI 结果无效；白天方桌一次确认日结，不再进入 EndConfirm；夜死/日间死按已确认 Death/Execution 在正确时点触发 NGJ Klutz 实际选座位，**不写独立 Learned**；死亡使后续普通角色不醒、守鸦人/贤者按原因触发；中毒失效/保护按事件发生时结算；投毒/市长/恶魔继任；两类跨局防重复；5/6/7/9/10/12/13/15 人代表性全流程及其余人数配置；NGJ 女仆、艺术家、呆瓜、贤者；重复提交、进程恢复、版本过期、AI stale、隐私及实际误播记录。
>
> **D2 追加关键验证**：在数字或双人信息已经全屏预览之后修改数字、身份、座位、无角色选项或恢复推荐草稿，Next 不得直接提交旧预览；后续 AI 推荐到达不得覆盖当前人工输入；普通方桌无 typed `displayProposition` 时不得提供误导性展示按钮，全自动必须等待或允许重试，已确定 typed 结果可直接展示。侍女无选定数字不应出现“查询并展示”的空操作。以上纯逻辑及真实 UI 路径都要验收，安卓完整 CI 通过不等于视觉/现场操作已验收。
>
> **前一代码基线证据（2026-10-20，最新见本文首段）**：PR #335 `35a2c880646b3b2d1af8d3bd69a2636e57db9001` 完整 Android CI #4479 / R2 #4055 SUCCESS；前一 SHA `e0426d5` Android CI #4478 因 Kotlin 编译失败，后以 `35a2c88` 修复，只有成功 SHA 是当前基线。该证据涵盖源码已有相关单测与 Debug APK 构建，**不等于真机手工 D2 全角色行为验收**。当前执行环境为 Codex Windows 本地，已同步到其纯文档子提交 cf3dd516，不使用 MiniMCP。Docs-only 后续 CI 可能跳过 Android，不能替代真实代码验收。
>
> **D2 下一批定向验收**：分别在 MANUAL/AI_ASSISTED/AI_AUTOMATIC 模式走一次洗衣妇/图书管理员/调查员、厨师/共情者、占卜师、侍女、送葬者、守鸦人、贤者等的“选择→全屏预览→返回原角色高亮→修改再预览→Next 唯一结构化 Observation”完整链；AI 缺失/调用失败/版本过期必须可见并重试，查询目标变更后旧结果绝不可提交；唯一确定线索无需多余 AI 请求；中断后能回未完成安全步骤。然后全夜 `nextStep(Session)` 验证剧本夜序、已确认 ActionFact/Observation、死亡/保护/毒及条件唤醒、无跳步/重复行动/持久游标，迁移一条业务链同批删除旧 UI 步进路径。避免只按源字符串或机械 TDD 编造不必要断言。
>
> **每批代码迁移退出准则**：精确列出旧→新入口及全部消费者，迁移并验证新业务路径后**同一批整删**被替换的页面、流程枚举/游标、状态/事件、兼容层、独占旧测试和 CI 守卫；不能仅隐藏按钮、改名或保留双轨；对未全查清职责的生产调用先追踪，不凭类名暴力删除关键 Host/AI 能力。代码 SHA 关联完整 Android `:app:testFull :app:assembleDebug`（或 GitHub 工作流）与 R2；docs-only SUCCESS/Android skipped 不能冒充代码验收。所有源码 <=50,000 bytes；保存可复用的真实行为测试。

## 目的与产品合同

**历史路线（已取代）**：原先曾计划 Host / 新 Rules / 新 AI 三模块及旧 P1–P5 删除次序；现行业务是 **一个执行确定性规则的最小 Host + 既有 AI 推荐**，不得建立第三个新 Rules 权威，当前 P0–P5 采用本文件顶部重构路线。旧文档可用于已发生改动与 CI 取证。

**Host 规则合法性门禁与未确认 Spy/Recluse 见证不再是产品合同；AI 提示词、候选通信、开局/后夜推荐、战略记忆和请求/响应机制仍是必须保护的产品合同。** 仅已证实没有 AI 生产消费者的旧算法及其专属测试/CI 守卫随代码退休。测试通过不能成为保留旧 Rules 的理由，也不需要开发新 Rules 替代之后才能删旧 Rules。P1/P2 期间不能因为删 Host 规则而停用正常 AI 推荐；恢复 B3 误伤并保留必要的 AI 生产适配，日后按可验证等价替换清理。

## 必须保护的 Host 基本行为

- 唯一会话/玩家/座位/真实与展示身份、当前阶段/轮次与真实存活状态。最终实际确认的信息/事件拥有一个事实权威。
- 初始配置、真实玩家动作、所给私密线索、投票/处决/死亡、角色/状态变化、阶段推进等关键事件的顺序、收件人、重复身份、原子提交与基本继续游戏。
- 重复提交与过期 AI 响应的隔离；私密信息可见范围；敏感凭据安全；在删除规则后 MANUAL 模式最小操作可用，手动输入只被数据形状/安全检查而非游戏规则合法候选约束。
- 删除规则评估后不能把过去已显示的数值重新计算成“正确答案”覆盖实际记录，也不得凭一条观察补写未发生的登记裁定。
- 未来跨 Android / 独立 WebHost / EvidenceLab 的核心数据合同应有最少且有效的结构化状态与事件测试，不要求测试旧平台细节或重复状态的巧妙兼容。

## Evidence-first，不强制 RED/GREEN

对每一删除批先界定新合同中需要保留的操作与可接受缺口，审计真正的生产调用和废弃测试，再依据风险选择验证。机械代码删除/移动和文档修订不为完成“test-first”而写新的 RED。只有可复用、确实缺失的稳定行为才增补耐久测试。

**产品必要性优先**：旧功能在生产仍被调用，只证明存在耦合，不证明产品需要保留；当现有方桌、明确用户动作或基础 Session/AI 能替代，测试旧专属 UI、候选规则、令牌编码或高成本自动判定，应与旧链一并退休。例如 Spy 不再有单独魔典页、不维护镜像 GrimoireState 或「中毒间谍不准打开该页」测试；仍保留真正的玩家身份/公开信息/Session 保密和通用夜间前进验收。不要增加只保护已取消按钮的机械测试。

仅旧 Host 规则合法性门禁和确认无用的实验测试：可以整类/整文件删除；独立查看是否意外覆盖 Host 事实/隐私不变量，必要时把**真实不变量**迁到 Session/事件测试，不能复制已退休的规则逻辑为“对照”。不要为已停用的 Host 门禁调整事实输出；但保留现有生产 AI 的请求合同、响应解析、开局与后夜推荐语义的有效回归测试。

**已确认的跨局开局体验也必须保护**：保留“连续局避免相同角色阵容/展示阵容”和“相同玩家连续局尽量避免同一身份/角色”两条独立的测试及真实生产路径，继续核验history store、seed可重复、Drunk真实/展示身份一致性。不能把开局仍然可运行当作删除这两项产品需求的理由。与此不同，Mayor Host合法候选singleton自动推进、FT规则唯一Boolean自动判定和只供测试消费的旧PlayerHistoricalTimeline可按现行真实确认/事件投影替代并退休。

## 验证层与执行

- T0 直接的最小独立证据：新旧调用链/符号搜索、编译检查、现存类型/事务测试、按目标操作复核、确切 diff。局部可运行断言仅在有效的稳定合同上添加。
- T1 Android 快速现存单元测试：./gradlew :app:testFast。具体被排除的旧实验名单应依当前 Gradle 配置而非历史测试快照确认。
- T2 涉及跨状态、事件、身份、隐私、异步、共享 DTO 或持久化时，增加受影响范围的现存回归与完整构建。
- T3 性能/实验/外部 ASP/Clingo 等只在保留的合同需要时运行，旧规则枚举性能不是新的最小 Host 验收义务。
- T4 代码里程碑完整 Android JVM 与调试 APK：./gradlew :app:testFull :app:assembleDebug；按 GitHub CI/R2 当前实际工作流选择外部检验。GitHub commit message 可用 [full-ci] 强制完整检查。Docs-only CI 成功时 Android 等重型 jobs 可能被跳过，不能当作新代码的编译证明。

旧代码删除若导致过时 CI 规则守卫失败，应审计其保护意图并删除/更新守卫；但发布权限、Session 写入/原子性、隐私、安全、基本构建检查不可因旧规则退役而被一起取消。CI/R2 不是全部质量证明，真机/完整手动游戏验收也要在合适的阶段补充，未进行不可假称通过。

## 执行与证明要求

每批从 GitHub 最新明确 SHA/PR base 开始，检查开发机真实分支、完整生产消费者及当前测试清单。优先净删。完成后检查 Git diff、编译/现存测试，核对 GitHub 对应代码 SHA 的 CI/R2。CI 失败直接取该 job log，按新产品合同修复；禁止恢复旧代码只为让旧规则测试通过。不要对每个微步骤重复不必要的全量验收，但每个可信提交检查点应有独立远端证明。

验收报告必须包含：删除文件/测试与净删体量、唯一事实状态/事件风险、Host 旧规则/生产 AI 真实剩余调用、所有超限源文件、Android/CI/R2 结论和有待用户决策的边界。代码来源和执行环境以最新 repository Gradle/.github workflow 为真值；历史 S1/SDE/A4 统计与遗留测试数量是归档而非现行法定门槛。

## 工作流与合并

GitHub Connector 默认为远端审计与变更工具，Mini MCP 仅在大文件完整上下文/本地操作时补充。所有相关 PR 保持 Draft；未授权不得擅自合并堆叠分支、强推、删除远端分支或进行收费 API 测试。

## D2 Compose 交互回归（2026-10-20，已由 af35cee9 的 CI #4484/R2 #4060 验证）

`:app:testFull` 包含 `ClocktowerD2UiTest` 的真实 Compose + Robolectric 回归，当前 21 个 UI 用例；`:app:testFast` 排除这一个较重的 UI 类，保留所有原快速合同与新增查询审查回归。必须以 full 的结果接受 UI 改动，不能以 fast 代替。全部 UI 用例使用离线输入/响应 fixture，部分直接走真实 JudgeScreen 与 GLOBAL_V1 Session，组件级和整链证明范围见 [D2 验收记录](D2_INTERACTION_ACCEPTANCE_2026-10-20.md)。此记录不等于设备/真实网络/像素视觉验收。
