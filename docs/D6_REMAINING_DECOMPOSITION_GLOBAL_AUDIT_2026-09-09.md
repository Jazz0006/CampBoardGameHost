# D6 剩余部分整体拆分与解耦审计

> 日期：2026-09-09 Australia/Sydney
> 范围：剩余 Android App/Clocktower 组合、展示、准备与事务边界；仅审计和路线更新。
> 审计代码：5e0891e7611787300b01d83b889f27a903c0768b
> 审计分支头：918230a95df0dcfae35a62d4e8a70158792b28f0（其后本次提交只更新文档）
> main：d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
> 分支：codex/d6-2-ui-composition；PR #115 OPEN / DRAFT，未授权合并。

## 实施进度更新

D6.2k 已在 `c3a25f640e7e6c9ef2537d7d9387eb27b28b1ece` 完成：三个生产文件净删除 257 行，移除 13 个闲置诊断状态和四个不可触发 effect；Judge 参数 89 → 87，NightStep 参数 49 → 48。CI 34293746781（Android compile + FAST / CI gate）及 R2 34293746779 通过。详见 `D6_2K_DORMANT_DIAGNOSTIC_CLEANUP_PROGRESS_2026-09-09.md`。

下方体积、行号和估算保留为原始审计基线。当前 Host 为 4545 行 / 268260 字节，NightStep 为 868 行 / 45697 字节，App 为 4236 行 / 236956 字节。下一步按 R0 顺序退役旧 Day/History UI 及对应过时 R2 断言，使用 full CI 验证；私有 App decoder 清理另行切片。

## 1. 结论与审计边界

**仍有明显且值得实施的解耦空间，但主要收益已经从“下放几个 UI 状态”转向“分开准备、渲染与事务编排”。**

推荐在当前代码上渐进切割，不重写架构，不更换语言，不把现有会话、规则、恢复系统推倒重建。先利用已经存在的 typed seam，再针对仍缺失的职责建立少量边界。

两轮推进：

1. 第一轮：删除无入口代码，拆开 Host 中的候选/步骤准备和交互展示，降低日常 UI/推荐修改的理解范围。
2. 第二轮：按实际收益选择 App 事务编排、Recovery 适配和 A4 生命周期边界；涉及持久化和顺序，风险与验证成本高于第一轮。

本次盘点了 296 个 Kotlin 生产文件的体积，对 App、Judge、NightStep、Day/History 组件及相关 session/persistence/planner、测试和 R2 约束做了职责与引用检查。不是对全部 296 个文件逐行进行规则正确性证明，也没有新的运行时性能测量。行号用于定位当前证据，不能作为以后补丁的定位方式。

遵循 AGENTS.md 与 TESTING_STRATEGY.md：单一可写权威、局部推理、窄输入/结果、行为保留、风险分层验证、允许退役过时测试。文件约 50 KiB 是软目标；不建立 God State/Actions/Context，不为跨文件访问盲目提升可见性，不把存储或规则决定塞进渲染层。

## 2. 实测现状

| 文件 | 字节 | 行数 | 主要问题 / 判断 |
|---|---:|---:|---|
| ClocktowerHostScreen.kt | 281354 | 4755 | 规则读取、推荐准备、局部状态、发布副作用、Day/Night 渲染混合 |
| CampBoardGameHostApp.kt | 237112 | 4238 | 应用壳、偏好/存储、会话桥接、恢复与大量事务回调混合 |
| ClocktowerDayScreen.kt | 50927 | 1033 | 部分旧界面已无入口；先删除再看是否需要拆分 |
| ClocktowerNightStepUi.kt | 47970 | 913 | 候选投影、推荐/确认、选择状态、注册面板与内容渲染混合 |
| ClocktowerHistoryScreen.kt | 38365 | 725 | 存在旧面板孤岛；结果展示仍有效 |
| ClocktowerStorytellerRecommendationUi.kt | 33459 | 594 | 相对明确的 setup 推荐 UI；不优先继续拆 |
| ClocktowerGameSession.kt | 22708 | 506 | 已有明确规范权威；本轮不拆其核心状态权威 |
| RecoveryRestorePlanner.kt | 19123 | 409 | 已有独立恢复规划职责；优先使用，不重新发明 |

仅 App 和 Host 超过 51200 bytes。其余较大文件（枚举器、语义 JSON、方桌布局等）不能仅因行数触发就纳入拆分；本次未发现足以优先于 App/Host 的结构收益证据。

Judge：89 参数、34 个 on 回调、3 个函数 provider、5 个 MutableState 参数。
NightStepCardLocalized：49 参数、13 个 on 回调。
App：44 个直接 remember 的 Clocktower 标量状态声明，包含 38 个 delegated var 和 6 个显式 state val；不包含派生值与 state list。

### Host 的规模集中在哪里

以下是不重叠源码区段的测量，不意味着整个区段可原样抽走：

| 当前区段 | 行数 | 字节 | 说明 |
|---|---:|---:|---|
| 1253–2217 | 965 | 50288 | actor、动态历史/压力、注册/数字/角色/二人信息候选准备 |
| 2218–2721 | 504 | 28870 | 信息构建输入、真值/登记派生、发布逻辑、首夜展示准备 |
| 2722–3738 | 1017 | 79999 | 首夜与其他夜晚角色步骤构建 |
| 3739–4351 | 613 | 30292 | Dawn 和 Day 各模式渲染 |
| 4352–4755 | 404 | 23340 | 夜间路由、前进/确认与 NightStep 接线 |

可见最大收益在角色/信息准备边界，不是继续追求几个参数的减少。

App 的 Judge 绑定及回调所在大区段（约 2695–4008）有约 1314 行、93156 bytes；Recovery 组合区段 1437–1784 有 348 行。它们包含实际提交与路由顺序，不能当作纯渲染搬运。

## 3. 可以直接删除的残留：先清除错误参照

### A. D6.2k 已冻结的清理合同

继续遵循 D6_2J_RESIDUAL_COMPOSITION_AUDIT_2026-09-09.md：

- Host 三个 permanently-zero 诊断计数及效果；NightStep 一个同类效果。
- Host/子组件之间没有消费者的 debugDiagnosticsExpanded 接线。
- Judge 的 confirmedDemonSuccessorTarget 参数与 diagnostics-only rulesetRef 参数。
- Host 八个未使用的纯角色查询局部值。

实际 benchmark harness、typed tests、A4 prewarm/rebuild、App confirmed state 和 ruleset authority 全部保留。预期 Judge 89 → 87，NightStep 49 → 48；回调数及 App 的 44 个状态不变。

### B. 本次新增确认的旧 UI 孤岛（单独切片）

全仓代码引用搜索显示：

| 定义 | 证据 | 处理 |
|---|---|---|
| ClocktowerNominationScreen | 仅定义，无调用或函数引用 | 删除旧界面，保留方桌 PendingNomination |
| ClocktowerVoteScreen | 仅定义 | 删除，保留 VoteTableScreen 与 typed vote state |
| ClocktowerSpecialDayActionScreen | 仅定义 | 删除，保留 Slayer/Artist/Klutz 方桌路径 |
| ClocktowerGameRecordPanel | 仅定义 | 删除旧记录面板 |
| ClocktowerTimelineRow | 只有旧 GameRecordPanel 调用 | 随不可达调用子图删除 |
| ClocktowerPlayerStatusRow | 仅定义 | 删除 |

保留：ClocktowerDawnSummaryScreen、ClocktowerExecutionConfirmScreen、其仍被调用的 DayActionHeader、ClocktowerResultsDialog、ResultPlayerRow，以及被 AppGameReviewScreens 调用的 clocktowerEventPhaseLabel。

Day 旧定义区段约 417 行/19006 bytes；History 旧定义区段约 190 行/8892 bytes。删除后 Day 约 616 行，History 约 535 行（未计 import 清理），没有必要先把这些死界面拆成更多文件。

**R2 约束冲突已查实：** .github/workflows/r2-write-probe.yml 当前强制上述旧函数存在。删除应在同一切片退役它们的 existence assertions，保留“仍有效的职责不得回到 root/Host”的约束及活跃定义检查。不要保留空壳函数迎合测试。修改 workflow 会触发 full-strength CI，不能按普通 UI 删除只跑 FAST。新 allowlist 必须显式包含这个 workflow；这不是 D6.2k 三文件合同的暗中扩展。

### C. App 旧解码孤岛（单独切片）

App 483–567 的私有旧解码链没有外部入口：

- toPlayerCards → playerCardFromJson
- toEliminationRecords → eliminationRecordFromJson
- toClocktowerEvents → clocktowerEventFromJson
- toRecordedEpistemicObservations
- gameOutcomeFromJson

现行 archive 路径通过 GameArchiveJsonCodec / AppGameStateJsonCodec；Recovery 有独立 v2 codec/planner。这里应删除无入口旧实现，而不是搬到新的 LegacyHelpers。clocktowerRolesFor(playerCount) 也仅有定义，约 12 行；generateClocktowerAssignments 仍被启动路径调用，不能一起删除。

这组约 97 行，不改变 JSON 格式/兼容策略。删除前再次完整查引用并保留现行 codec/恢复测试。

A/B/C 合计有约 900–1000 行已识别的死代码清理空间（包括 A 中诊断效果，未计 imports；范围估算，不是精确最终 diff）。这些收益不能归因于新增抽象。

## 4. 值得实施的解耦边界

### D. 信息准备与角色步骤构建：高收益，中高风险

当前 Host 同时知道历史压力、登记角色、同夜生效状态、候选合法域、推荐排序、展示文本和步骤数组。将其拆成两种不同责任：

1. **信息/登记准备适配**：输入当前只读规则状态、actor/目标、相关历史摘要和配置；输出合法结果、推荐候选、登记选项及必要解释数据。复用 PairInformationLegalDomain、RegistrationPolicy、FixedInformationEvaluator、推荐器及 ClocktowerStructuredInformationPreparation。
2. **角色族步骤 materializer**：消费已准备的数据和本角色交互，输出 ClocktowerNightStepUi；保留 ClocktowerNightStepMaterializerRegistry 的 canonical interaction 顺序权威。

分族切割：数字信息、二人/角色揭示、目标型动作。不要一角色一个极小文件，也不要一个上千行 NightContext 包含所有值/函数。初期可以保留 UI adapter 层的 PlayerCard；不要为了本次拆分全面迁移所有 readers。

先移纯准备函数，调用位置保持不变；再让 materializer 不捕获完整 Host 局部作用域。不能只是把 1000 行闭包列表移到另一个文件，继续接收几十个任意回调。

关键注意：`recommendationCoordinator.selectInformation` 可以记录审计；structured prepareUiModel 可以进入 decision lifecycle。**名称叫 prepare 不等于纯函数**。先把候选计算与 decision/telemetry 操作分开，后者保留在明确生命周期中。

成果：调整某个信息族的展示/候选适配时，主要阅读该族 adapter、既有规则 owner 与相应测试；不再必须打开整个 Judge 的 Day/Klutz/Recovery 接线。参数总数初期可以不降，准备职责移出才是实质收益。

### E. NightStep 展示交互：高收益，中风险

49 参数的 NightStep 仍混合：候选来源投影、AUTO/ASSISTED/MANUAL 策略、结构化数字/布尔确认、登记控件、目标选择、展示按钮与导航。

复用既有 StructuredNumberInformationUiModel / Boolean preparation、InformationDecisionFoundation、typed table state，将其组织为“当前交互准备 → 对应展示分支 → 确认结果”。渲染器只收到其交互需要的模型和少量动作；不能把 49 个字段装入一个 NightStepArgs。

优先数字/布尔信息确认，再目标选择，再登记面板接线。展示前确认保持上层；纯 UI 组件不拿 ClocktowerGameSession 或恢复 writer。manual overlay 的 remember key、切换角色后的 reset 和选中目标变化必须保持。

成果：未来改手动信息选择/数字按钮，不需要理解恶魔继承或夜晚结算。可将 913 行集中实现收敛到约 350–600 行组合代码加内聚组件；这是规划目标，不是允许机械分文件的硬指标。

### F. Day 组合：中等收益，按互动拆，不能整体搬

Vote 是最好的试点：已有 ClocktowerTableVoteState 与 commitClocktowerVoteTransaction。将准备好的座位与 standing 输入、投票确认意图交给一个明确互动边界；当前权威回调只负责提交投票结果和日志。不要给子组件 ghost/highest 的多个 setter，以免分散事务顺序。

Nomination 的 Virgin preflight → registration record → execution callback 顺序保留。Artist 自动答案还依赖动态优势、历史和 effect；Slayer/Klutz 也使用共享登记策略，因此需等待 D 的窄准备接口，不能拿完整 recommendation context 下沉。

提名 pair、Slayer/Artist transient 状态可以最终跟随互动 owner，但先保留现有 remember 寿命；“移到分支内”会改变卸载后保留/重建行为，必须单独证明。dayMode、highest vote、ghost vote、Night cursor 仍保留既有外部权威。

成果：Day UI 修改在相应互动文件完成，Judge 只分派已准备的分支。Dawn 和 EndConfirm 已相对薄，无需为了行数再抽一层。

### G. setup 推荐效果：有条件拆，保持挂载位置

Host 1080/1112/1162/1187 的效果管理 readiness、取消、重试、推荐加载与自动应用。它们不是只在 setup screen 出现时才运行的普通渲染代码。

可以形成专门的 Compose lifecycle owner，暴露 loading/ready/failed 和启动/重试操作；但第一步在原位置无条件调用，保留 recommendationKey、gameId/gameSeed、locks 和 isActive 防旧结果发布逻辑。复用已有 setup precompute/reveal/prewarm coordinator。

不得将其与全局 screen state、投票、Recovery 装入同一个 controller。只有证明效果的完整依赖后，才考虑按阶段挂载；性能优化作为独立目标验证。

### H. App 事务编排：最大深层收益，高风险，第二轮

现有 NightCheckpointHostTransaction、NightDawnResolutionPlanner、NightDawnDurableMaterializationPlanner 和 GameSession 已提供关键权威，不需要新增第二套游戏引擎。

App 残留任务是把它们的结果应用到 session、revision、历史、PlayerCard 镜像、continuation 与 A4 invalidation。按事务种类拆，不按“所有 callbacks”拆：

1. 夜间 draft edit/confirm：复用 reducer 与 revision intent；攻击/中毒/保护保留各自精确提交次序，不能默认共享同一 revision 模板。
2. 信息发布/history：先抽纯历史文本与 ObservationDraft 准备，再保留一次发布入口。
3. Day execution/Slayer/Virgin/Klutz：明确命令输入和 continuation；规则决定用已有规则 owner，UI 只接结果。
4. Dawn/death/succession：最后处理；保留非突变 preflight、idempotency、role identity、public-alive、phase advance 的原顺序。

可以增加专用事务 adapter，但只依赖本事务的状态快照和必要提交能力；若接口需要几十个 getter/setter 或直接收到 App 对象，说明还未形成真正边界，应停止而非接受 God Controller。

不能为减小 App，把 UI/Android Context/persistence 放进 ClocktowerGameSession。也不应让 adapter 另存一份 canonical GameState。先抽准备/决策，再逐个切换应用入口；整体事务模型重写不属于本次结构重构。

成果：普通事务的 correctness 能在 typed seam 验证，App 保留短接线与镜像发布。原有全局代码仍需理解的场景缩小为真正跨阶段/跨会话的操作。

### I. Recovery / A4：边界明确，收益较小但风险高，按需第二轮

Recovery 已有 planner、application coordinator、write gate、codec。优先保持 PS5 成果。App 348 行 capture/apply/lifecycle 并不意味着应创建一个 44 字段的 RecoveryUiState 或逐字段 setter 接口。

可先拆纯 capture/restore projection 和按游戏适配，使用已有 RecoveryGame/ValidatedRecoveryPlan 数据；根仍控制 `prepare 成功 → crossSessionBoundary → apply` 与最后的 screen re-entry。没有稳定的状态应用边界时，保留局部代码比增加大参数包更好。

A4 live lifecycle 可以独立 Compose owner 管理 request/job/cancel，但保留原挂载位置和 cache 实例寿命。revision/session invalidation 以及 `persist success → durability release → rebuild` 不拆成独立可乱序事件。DEBUG/5-player 运行条件本身不意味着 live A4 功能可删除。

成果主要是隔离持久化/后台工作知识；不承诺改变 schema、降低写频率、降低启动时间或扩大 A4 产品覆盖。

### J. 目录/偏好/历史访问：低风险，适合作为收尾

App 的双语角色表示/选择 facade、SharedPreferences 偏好和 archive I/O 可分别归属现有 catalog/persistence 与呈现适配。它们不需要读取整个 Compose root。

保留原 prefs key、读失败 fallback、提交方式、archive 顺序与容量。角色展示目录搬迁不等于改由另一个规则源重新推导角色；避免在拆分里改变 NGJ/TB 实际身份或重复引入新的规则权威。

只移动完整责任和调用者需要的 API。私有实现保持在该责任内部；不要为文件拆分将每个 helper 都变成 internal。不要同时改 UI 文案或规则。

## 5. 必须冻结的顺序与生命周期

| 路径 | 必须保留的契约 |
|---|---|
| 信息展示 | apply selected registration → selection commit telemetry → reveal handoff；handoff 的 authorize/first-night publish/private observation/history/display 顺序不改 |
| 准备/推荐 | 合法域先于排名；稳定 candidate ID/枚举顺序/随机 seed/key 保留；prepare 中的状态操作不得当纯计算搬动 |
| 同夜状态 | canonical interaction 顺序与 cursor-relative alive/role/poison 解释；不能用最终 GameState 替换时间点状态 |
| 游戏会话 | GameSession 仍是唯一 canonical writable owner；revision +0/+1 与 chronology 分配保持 |
| UI selection | 原 remember key、分支重建、取消/完成清理语义；day/night/restore 特殊路径保留 |
| Recovery | v2 only、先验证后写入、SideEffect 与 ON_PAUSE/ON_STOP、WriteGate 重试/force 语义 |
| A4 | revision/session invalidation；持久化成功后才释放 observation rebuild |
| UI 边界 | 渲染器不判断 rules legality、不写 session、不接收不必要的隐藏事实 |

## 6. 建议实施路线与工作量

工作量用“逻辑切片”而非提交数量：一次切片包括接口/范围确认、实现、相关验证、diff 审计和文档；同一切片可有多个小提交。下面是规划量级，不是已证实工期。环境配置、CI 排队、真机反馈和新发现的行为缺口都会影响耗时。

| 阶段 | 内容 | 估计切片 | 风险 / 验收 |
|---|---|---:|---|
| R0 清理 | D6.2k + 旧 UI/R2 + App dead decoder | 2–3 | 低；旧 UI 的 workflow 变化需 FULL |
| R1 准备边界 | 信息准备按族、角色步骤 materializer、纯 history presentation | 3–5 | 中高；typed 行为和 registry/顺序证据 |
| R2 交互边界 | NightStep 数字/布尔/目标展示，Day vote 试点，必要的 setup effect owner | 3–5 | 中；UI 生命周期/确认接线证据 |
| 第一轮验收 | 重新量测修改范围与收益，FULL + 真机关键路径 | 1 | 到此可停止并回到产品任务 |
| R3 App 深层事务 | night confirm、Day execution、Dawn/succession；按实际收益选 | 4–7 | 高；preflight/retry/order/revision 集成证据 |
| R4 可选边界 | Recovery/A4 适配、preferences/catalog/archive | 2–4 | 中高；不改变持久化或身份权威 |

推荐先承诺 R0–R2 的方向，每阶段结束后决定下一阶段的精确 allowlist。第二轮不是完成第一轮的前置条件。不要将所有切片塞进正在验收的 PR；在用户授权合并当前 PR 后，用新分支承载后续自然检查点。未授权合并时保持当前草稿，不自动 merge/mark ready。

R1/R2 可有交错，例如先用 typed Vote 做小试点；但不先搬所有 Artist/Klutz，也不先改所有效果挂载。大规模事务变化不能混进 UI-only 切片。

## 7. 预期可以达到的成果

### 已有证据支持的短期成果

- 约 900–1000 行死代码清理，减少错误的旧实现参照；不引入新抽象。
- Judge 89 → 87、NightStep 49 → 48（仅 D6.2k 的可靠预期）。
- Day/History 文件删掉旧 UI 后约 616/535 行，优先无需再拆。
- R2 不再强迫保留已经退休的 UI 定义，架构约束与活跃代码一致。

### 第一轮规划目标（需要实施后测量）

- Host 从 4755 行降到约 **2200–3000 行**：重点迁出约 1000–2000 行内聚的准备/materializer/互动责任，扣除新接线后预留余量。
- NightStep 从 913 行集中实现收敛到约 **350–600 行**组合代码，复杂交互转给内聚 owner。
- App 第一轮仅 dead code/纯适配有小幅变化，可能仍有 **3600–4100 行**；不应为了让两个大文件一起变小而提前迁移高风险事务。
- 对一个数字/二人信息展示或 vote UI 修改，目标主要理解 **2–4 个责任文件及其测试**，而非加载两个大根文件。这个数量是验收目标，不是当前已测得的工时下降。

### 第二轮可选目标（不作为交付承诺）

- Host 约 **1500–2200 行**，保留真实的阶段/注册/效果协调。
- App 约 **1800–2600 行**，保留应用壳、跨游戏导航和少量有序边界接线。
- 会话/恢复/规则 owner 不再被 UI 布局改动频繁触及；复杂事务由专用 typed contract 测试，而不是依赖大文件源码片段。

这些区间来自当前职责块体积与预留接线量，不是已实现节省。迁移的代码仍存在于仓库，总行数可能不降，甚至因明确模型/测试略增。也不承诺整体 Judge 参数一定降到 10–20；如果它仍是合法的总调度边界，保留较大签名可能比隐藏为宽对象更诚实。

不承诺：所有文件 <50 KiB、测试时间按比例下降、内存/运行速度提高、零回归、产品算法质量提升。性能需另有 profiling；算法语义改进留给后续产品/一致性工作。

## 8. 验证与测试退役

- 死代码：完整调用/写入路径证明 + compile/diff + FAST；workflow 改动按规范 FULL。
- 准备/materializer：复用 PairInformationLegalDomain/FixedInformation/Registration 测试、ClocktowerNightStepMaterializerRegistryTest、InformationStepBuilderManualAuthorityTest、StructuredInformationPreparationTest；只有覆盖不到的稳定契约才补 characterization。
- 显示/确认：复用 StructuredNumberInformationUiModelTest、InformationDecisionCoordinatorIntegrationTest、相关数字/布尔 adapter 和 player reveal handoff 测试；核对手动/自动、醉酒/中毒及重复展示。
- 投票：DayNominationGesture、TableVoteState、VoteTransaction、GhostVoteAuthority；UI 取消/返回生命周期增加必要人工证据。
- 事务：NightCheckpointHostTransaction/Reducer、NightDawn planner/materialization、GameSession boundary、restore-retry convergence；出现新的共享提交 seam 时补 typed 顺序/不突变 preflight 测试。
- Recovery/A4：现有 v2 schema/strict decode、ApplicationCoordinator、WriteGate/Lifecycle、A4 cancellation/durability tests；保持实际执行与 cache 命中区分。

已发现 StructuredEmpathInformationAdapterTest 和 ClocktowerChambermaidSelectionAuthorityTest 等仍读 Host/NightStep 源码，R2 有文件/函数存在约束。对应责任迁出时，先识别原契约，尽量迁到 typed owning tests，再删/缩旧 source assertions；不为保持旧源码形状放回 helper。

每个逻辑 checkpoint 采用 T1 + 触发的 T2/T3；FULL 放在架构验收或规则要求的 gate。不要每个微提交重复全套，也不要用旧头的 FULL 证明新跨边界改动。实际设备验收至少覆盖：提名取消/投票返回与鬼票、Virgin 即时处决、Artist 自动/手动、信息展示往返、Night previous/confirm、Klutz/继承、后台恢复。

## 9. 停止条件与验收标准

只有同时满足这些问题，拆分才算有效：

1. 责任名能说明它拥有的规则/准备/渲染/事务之一，正常修改能在该边界完成。
2. 下层未拿到整个 Host/session/通用 Context；上层不再知道已迁出的内部步骤。
3. 没有新增 canonical 状态副本，没有同一数据两处可写。
4. 接口字段/回调属于同一互动或事务，而非为了参数计数包装。
5. 原行为、候选顺序、effect/remember 寿命、revision/history 顺序有可靠证据。
6. 相应 source-string 债务减少或明确保留理由。

若继续拆会产生几十个 getter/setter、第二套状态权威、只能改一件事却要穿越更多文件、或必须改变规则/生命周期才能成立，应停止该边界。完成第一轮后如果普通 UI/信息修改已有清晰 owner，就可以暂停 D6，返回真机稳定化和一致性算法目标；不以把每个文件压到固定体积为结束条件。
