# D6 — App / Host 状态与持久化所有权实施方案

> Role: **CURRENT ACTIVE HANDOFF / PLANNING CONTRACT**
> Date: 2026-09-07 Australia/Sydney
> 当前进度与优先级以 `CURRENT_DEVELOPMENT_ROADMAP.md` 为准。

## 1. 决策与范围

用户要求在 PR #106 合并及后续修复后重新规划 D6。本轮只更新方案，不改生产代码。
本方案依据最新 main `ac71cbe392fb542727dc0c2d69ac82c5fdc0435e` 重新审计；旧 S9 与 D1–D6
分析仅作参考。完整本地工作区可直接处理大文件，原 Chat/connector 大文件改写路径限制不适用；
规则、生命周期、验证与合并约束继续有效。实施前重新获取 main，禁止复用已合并的 #106 分支。

**推荐顺序：存档编码 → 恢复解析与应用 → 夜间运行时状态 → Host/App 命令副作用。**
先建立可验证的输入输出边界，再改变状态所有者。不要先搬进一个巨大 ViewModel，也不要为了行数
拆一批仅转发参数的 helper。每个检查点必须移除原所有者的一项完整职责，且删除其旧活动路径。

旧 S9 对持久化边界的判断仍有价值；其最低缩减字节数不作为验收门槛。当前新增 checkpoint/
revision/全局时序与崩溃诊断路径必须纳入新契约，不能直接重放旧补丁。

## 2. 当前代码审计

| 所有者 | 当前规模 | 实际边界问题 |
|---|---:|---|
| `CampBoardGameHostApp.kt` | 4557 行 / 261492 bytes | Compose 状态、存档编码、恢复解析、状态赋值、持久化触发及游戏命令混合 |
| `clocktower/ui/ClocktowerHostScreen.kt` | 5474 行 / 329172 bytes | 消费 App 的共享可变状态，同时承担派生时序、回调接线、交互与效果协调 |
| `persistence/ActiveGamePersistenceCoordinator.kt` | 已有稳定契约 | 负责存档版本、内容身份与兼容性；不是全存档 codec 或 SharedPreferences 仓库 |
| `clocktower/session/ClocktowerNightCheckpoint.kt` | 已有不可变投影 | 包含草稿/确认值，也投影 phase、round、revision 与全局 sequence；不能直接变成这些值的第二个运行时所有者 |

审计锚点：App 的 `activeGameSnapshotJson`、`restoreSavedGame`、
`currentClocktowerNightCheckpoint`、`persistAndReleaseA4ObservationRebuildIfDurable`、
`advanceClocktowerGameStateRevision`、`onConfirmDay`；Host 的 `effectivePoisonSourceAt`。
用符号定位实现，行数仅用于本次测量。

具体发现：

- 存档编码先写多项扁平字段，又用 checkpoint 的 `persistedValues()` 写同名字段；恢复也先从 JSON
  赋值，随后再用 checkpoint 覆盖。最终生效值必须先固定，不能因“去重”改变 fallback。
- 当前 schema v3 的 checkpoint 就使用这些顶层键，没有额外的嵌套 checkpoint 对象。
  不得将顶层键误当成可删除的 legacy 格式，也不新增 v1/v2 兼容支持。
- 恢复先校验身份、ruleset 与语义历史，但部分记录/投票/结果解码仍在 live state 赋值区域中。
  这是“完全解析后再应用”边界缺失的证据；尚未复现由此引起的异常，不宣称它就是已报告崩溃根因。
- 当前写盘使用 SharedPreferences 同步 `commit()`；A4 重建释放以写盘成功为前提。
  `SideEffect` 与 ON_PAUSE/ON_STOP 的调用时机属于现有行为，不能随编码抽取一起改成异步或防抖。
- #107/#108 增加诊断与 Application 早期安装；#110 修复可选投毒者步骤缺席时查询时序的问题。
  诊断日志不是游戏事实或恢复权威，时序缺席不是放宽全局 chronology 严格校验的理由。

## 3. 目标所有权

| 数据 / 行为 | 权威所有者与依赖方向 |
|---|---|
| Compose 生命周期、导航、持久化触发、恢复结果应用 | App 根先保留；逐步仅做捕获、应用和接线 |
| 活动游戏的存储结构与 JSON 编码 | 独立 persistence codec；消费不可变数据，无 Compose/Context/Host 引用 |
| 不可信 JSON 的解析与完整验证 | 独立 restore parser；复用现有版本、身份、ruleset、语义历史权威 |
| 夜间草稿/确认选择 | D6.3 的窄状态所有者；一份 live state，UI 观察并请求修改 |
| phase/round/revision 与 session 全局时序 | 保持现有 App/session 权威；checkpoint 只是投影，不独立递增 |
| 合法性、夜间重建、确认规则 | 现有 reducer / reconstructor / transaction / rules 模块 |
| Drawer、弹窗、展开与临时展示状态 | 保留最低层 UI 所有者与原 remember/reset key |
| 崩溃捕获、诊断导出 | 现有 Application/debug 所有者，不并入存档 codec |

DTO 可以按共同会话、各游戏持久化部分、既有 night checkpoint 等真实概念分组；它代表存储契约，
不是一个传给所有功能的 AppState/Context。不可包含 MutableState、SnapshotStateList、服务或回调。
集合捕获为稳定快照，保留原有顺序。已有 domain 值可直接复用，不重复定义事实模型。

## 4. 检查点路线

| 步骤 | 实施边界 | 验收与停止点 |
|---|---|---|
| D6.0 | 实施启动检查：重新核对 main/修复/验证，列出现有 schema v3 字段及最终覆盖值 | 明确同一基线；不另造一批生产抽象或仅为 RED 的测试 |
| D6.1 | 完整活动存档编码边界；迁移相关编码子职责，App 捕获当前状态并调用 codec | 三种游戏的 JSON 契约等价；Root 不再解释活动存档键；存储调用与恢复流程不动 |
| D6.2 | JSON → 完整验证的恢复结果 → App 应用；将记录/投票/结果等解析移出赋值区 | 校验失败不调用应用；有效存档恢复结果与基线一致；screen 最后赋值；闭合存档/恢复边界后可停 |
| D6.3 | 夜间草稿/确认选择状态所有权；一个交互族完成全部读写迁移后再扩大 | 没有新旧双写；confirm/edit/back/reset/restore 一致；共享 revision 与全局 sequence 不复制 |
| D6.4 | 按命令提取 App/Host 效果协调；先夜间确认，再独立审计执行/日夜推进 | 保留每条路径 preflight、commit、记录、revision/invalidation、持久化及失败顺序；不宣称新增原子性 |

D6.1–D6.2 是首个完整持久化里程碑，分开审阅编码等价与恢复边界的风险。D6.3–D6.4 是后续路线，
不是一次 PR 的固定工作量；完成前一里程碑后复审，不预先搬全部状态或全部 callback。
setup/start/reset/archive 暂不设必做 D6.5：D6.3 必须迁移其对所选夜间字段的写入，但整个设置与
归档事务只在前述边界落地后重新判断收益。SharedPreferences store、DataStore、全局 ViewModel、
A4 运行时重构与新的通用 AppIntent 均不属于当前方案。

## 5. 第一实施步 D6.1 的具体合同

建议新增 `persistence/ActiveGameSnapshot.kt` 与 `persistence/ActiveGameSnapshotCodec.kt`。
命名可按实际类型冲突微调，责任边界固定：

1. App 在原调用点捕获不可变存档数据；私有 `Screen` 用现有持久化名称投影，不扩大可见性。
2. codec 消费数据并构造 JSONObject，调用 `ActiveGamePersistenceCoordinator` 的既有身份契约，
   复用 setup/completion、ruleset、semantic-history、epistemic、ghost-vote 与 checkpoint 编码。
   依赖采用已有权威或窄输入，不把整个 App/Host 注入。
3. 与活动编码内聚的 PlayerCard、EliminationRecord、GameOutcome、ClocktowerEvent 编码迁到同一
   persistence 责任下。若 archive 等共享这些编码，调用点只改为委托；归档内容和副作用时序保持原样。
   解码及本地化暂留 D6.2，不为了“共用”扩大旧 catalog/role resolver 的可见性。
4. checkpoint 键由 checkpoint 编码一个入口负责；先以最终输出等价证明消除重复写入安全。
   不改变 absent/null、默认值、数组次序、实际/展示身份、版本或规则身份。
5. 删除 Root 被替代的编码正文，保留捕获及原 `saveActiveGameState` 调用路径。
   不引入新的 remember/effect，不改生命周期监听、commit 返回值或 A4 durable gate。

预计文件范围：App 根、上述两个新 persistence 文件、对应 codec 契约测试/必要的固定 fixture、
当前 roadmap/handoff。已有 codecs 原则上只调用；若签名确需变化，先解释该拥有者的真实契约。
Host、Application/debug、Gradle/CI、规则/推荐实现不在 D6.1 生产改动范围。

验收检查：

- 原 v3 Undercover / Werewolf / Clocktower 固定样本的键、值与数组顺序一致；JSON 对象键顺序不作契约。
- Clocktower 样本覆盖未完成夜间、草稿与确认值不同、实际/展示身份、语义历史与全局 sequence、鬼票。
- 使用冻结的基线输出/字段断言；不能只用新 encoder 与新 decoder 的 round-trip 互相证明。
- 编码器没有 Compose/Android Context 或可变状态依赖；Root 保留的代码能解释为“捕获 + 调用”。
- 现有 owning tests、Kotlin 编译、完整检查点 CI/R2 与 diff 审计通过；无新源码字符串行为测试。

## 6. 后续高风险合同

D6.2：复用 `ActiveGamePersistenceCoordinator` 的支持版本与兼容性门禁；解析完成后返回不可变的
`ValidatedActiveGameRestore`。所有可能失败的解析、身份校验、默认值决策与显示数据准备在 live
赋值前完成。Android 本地化留在薄适配层；旧角色查询按窄 resolver 注入，禁止复制 catalog。

保持现有 A4 session invalidation 时机、失败后的 clear-save 策略及 screen 最后赋值。
UUID/seed 缺失时的生成方式与每次恢复的调用次数保持一致，不把随机生成藏进可重复执行的纯 parser。
“解析失败不进入 apply”是目标合同；若能复现当前部分恢复缺陷，单列行为加固测试与修复证据，
不要把异常策略变更夹进机械搬迁。此边界不承诺 Compose 原子提交或任意副作用失败后的回滚。

D6.3：复用 `NightCheckpointReducer`、`NightCheckpointHostTransaction` 的 revision intent 与
`NightTransactionRestoreComposition`。新 night action state 只持有其夜间选择职责；phase、round、
revision、next global sequence 仍来自既有权威。修改 revision 必须走现有 invalidation 路径。
恢复与新局 reset 同步切到唯一 owner；不保留两个可写状态之间的同步桥。

D6.4：复用已完成的 D5 publication owners；不重新合并 registration、telemetry 与 reveal 生命周期。
每个命令先列出现有读写和异常短路顺序，再抽取 typed input/result 与窄 effects；主线程、协程作用域、
捕获的新旧值语义保持一致。#107 execution breadcrumbs 的位置、#108 early install 和 #110
optional-source gating 均受保护，不能在回调缩短时丢失。

## 7. 验证与回归矩阵

先用现有测试；只为真实缺口新增 durable typed contract，不制造 RED 或逐微步跑全量：

| 风险 | 现有起点 / 所需补充 |
|---|---|
| 跨游戏版本/身份及 schema v3 | `ActiveGamePersistenceCoordinatorTest`；补活动存档固定样本等价 |
| setup/completion/ruleset/semantic history | 对应 persistence owning tests；校验不迁成 UI 自行解释 |
| checkpoint 草稿/确认、回退和恢复 | `ClocktowerNightCheckpointTest`、`NightCheckpointReducerTest`、`NightCheckpointHostTransactionTest`、`NightTransactionRestoreCompositionTest` |
| 日落清毒、恢复重试、全局 chronology | `DuskPoisonExpiryRecoveryAuthorityTest`、`NightDawnRestoreRetryConvergenceAcceptanceTest`、`PoisonExecutionDuskTimelineRegressionTest` |
| 可选投毒者缺席 | `ClocktowerOptionalNightSourceChronologyTest`；执行/死亡后下一夜及存档恢复场景 |
| 解析拒绝与应用隔离 | D6.2 补有效/无效输入的 typed restore 契约；不按源码行序证明 |
| 生命周期与真实持久化触发 | 里程碑真机暂停/继续、杀进程后恢复、新局/归档；JVM 不能替代 |
| 诊断与玩家信息边界 | `DebugFlightRecorderCoreTest` + 有改动时的启动/导出检查；不主动外发诊断 |

T0 为当前边界的最小证据；T1 在逻辑检查点；持久化/恢复与中央事务检查点使用 T4 完整 CI（按仓库
`[full-ci]` 机制）及 R2。本次文档规划不重新运行 Android 测试，不把旧测试称作新执行。
既有合并后 main 验证在 roadmap 记录；实施时基线若发生语义变化，重新选择受影响证据。

真机重点：夜间 edit/confirm/back 后恢复；处决投毒者 → 日落清毒 → 下一夜 → 恢复；无投毒者
步骤的 canonical plan；草稿不提前成为机械事实；鬼票不恢复成可重复使用；三种游戏继续游戏。
每条记录 APK SHA、设备、Android 版本与预期/实际。已合并 bug 修复不等于真机验收通过。

## 8. 成功标准与停止条件

成功是：存档字段变更主要理解 codec 与其数据合同；恢复兼容性变更主要理解 parser 与验证权威；
夜间选择变更只理解其状态/reducer 与窄接线。Root/Host 仍可较大，但不再重新承接已移出的职责。
记录每步移除的责任、状态写入点与典型改动需阅读的所有者，不以减少多少行作为唯一成果。

遇到现存修复回退、schema 不等价、必须双写状态、必须依赖整个 App/Host、必须改变生命周期或
扩大公共 API 才能继续时，停止该切片并报告具体冲突。可在 D6.2 后结束本轮；后续拆分收益不足时
保留现有协调代码。UI-R5 仍是稳定性验收要求；EPI-MQ/UX-R6 不与 D6 混做。
