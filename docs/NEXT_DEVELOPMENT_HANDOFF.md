# 当前交接 — P2/P3 与最终收敛实施中（2026-10-21）

## 本地未提交代码：先修复验证，再继续迁移

本次远端同步为**文档专用检查点**，不包含本轮未验收 Kotlin。工作分支 `refactor/minimal-night-flow-20261019`；起点 `8024613ead6935d0baf4dea0d07638f82c921153`。接续必须保留当前脏工作树，不能直接 checkout/reset 丢弃改动，也不能把远端文档当成已推送的 P3 实现。`node_modules/`、`package.json`、`package-lock.json` 是原未跟踪文件，不得纳入本批。

已在本地修改：

- 删除独立日结页及 EndConfirm 路由；删除 KlutzLearnedDeath/producer，KlutzChoice 改为引用真实 deathActionId；待选择从 Session Death/Execution 派生。
- 新增 `clocktower/session/ClocktowerDayFlow.kt`：日结、真实 Klutz 选择、提名/投票/鬼票、Virgin/Spy、Slayer/Recluse、Artist 私密答案/消耗、确定继任与 GameEnded 事实。App 的旧胜负/日结控制已开始物理退出。
- 新增 SessionJson、私有 AtomicFile ActiveGameStore、ActiveGameMetadata 和 App 恢复入口。Session 确认批尝试先持久化再发布、失败回滚；AI 建议记忆和开局轮换身份元数据保留。尚未完成故障/恢复验收。
- 当前物理删除生产 2 文件、测试 1 文件；新增生产 6 文件。包含新文件的生产 Kotlin 字节仅净 -3B，测试净 -2846B，不能把只看已跟踪 diff 的大幅删除当作最终清理量。

**最近失败必须先处理：** 本地 `:app:testFast` 停在 `:app:compileDebugUnitTestKotlin`；`ClocktowerArtistDraftTest` 旧回调需迁为三个实际确认参数。生产 Kotlin 编译成功不等于测试/APK通过。最后日志 `.codex-fast.log` 与 `.codex-compile.log` 为本地临时诊断文件，不提交。`ClocktowerSlayerDraftTest` 的旧人工任意判死合同已退休，真实杀手/陌客机械与裁量行为需要新的 Session 回归。

接续事项按依赖顺序：

1. 修复当前测试编译，保留 Artist 草稿修改/失败不清空等真实不变量；补 P3 的 Session 行为证明，不恢复旧 Learned/EndConfirm/任意判死 UI。
2. 核验提名确认后未完成投票的恢复及去重、鬼票/管家、日结幂等、Saint/Mayor/Klutz/普通胜负优先级、Scarlet Woman 继任。检查新边界及 UI 全部生产消费者，不凭编译通过判定完成。
3. 验证 SessionJson 真实保存/重新读取、事件/观察顺序与收件人、毒/保护/继任恢复、原子写失败回滚、未确认预览重做、AI 旧响应隔离与建议记忆、双跨局防重复元数据。核验保存失败的 UI 处理和恢复入口；同一内存对象 copy 不算进程恢复。
4. P2 仍需跨夜/全角色/人数轨迹；P0/P5 全树语义审计未完。当前超限 App 125487B、HostScreen 79274B、Session 54513B，最终均须 <=50000B；不要机械拆分或引入通用外观/第二可写 GameState。
5. 更新 `.github/workflows/r2-write-probe.yml` 内已退休 Slayer `targetDied` 守卫，使其保护实际确定性结算和窄裁量；再完整 `:app:testFast :app:testFull :app:assembleDebug`、R2。通过后按同批迁移/删旧原则提交代码并推送，独立验证精确 SHA 的 Android CI/R2，更新 PR 描述与台账。

当前本轮没有通过的新 CI/R2、没有新 APK 真机或付费 API 验收。#334/#335 继续 Draft，不 merge/retarget/rebase/force-push/删分支。用户当前要求是同步进度文档，未授权将失败代码当作已验收成果发布。

## 上一批已验收基线与历史交接

**代码基线 `311191a30d6b0d83b5cf2d6de2a67905b526e596`，已推送原 Draft #335。** [完整 CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS；[R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS。本地 fast 593/full 616 tests（145/146 suites，0 failure/error/skipped，含 23 个真实 UI 用例）及 APK 通过。docs-only 后续提交不改变此代码验收归属。

## 已完成的责任与删除边界

直接使用本地 Windows `C:/Users/Jazz_/source/repos/CampBoardGameHost`，不使用 MiniMCP。JDK `C:/Program Files/Android/Android Studio/jbr`。原未跟踪 node_modules/package 文件保留，不属于本批。

一个 Session/GameState + 有序事实 + 稳定 stepId；信息预览不记录，当前角色显式 Next 才写最后唯一 Observation。夜间不再依赖 App/HostScreen 的 index/start/pendingAdvance，所有目标动作显式 Next。旧 Planner/Projector/First/OtherFlow、Fixed/Dynamic resolver、毒游标 chronology 和攻击预测链已实删。NightHostProjection/Materializer 只保留读取当前事实并渲染的职责，不再决定流程。

无用途首夜发布生命周期、旧 live-query/role projection、完整登记 UI/映射和自定义/Homebrew 分支已删除；保留处女/Spy 真实裁量、实际 AI、TB/NGJ 和两类跨局多样性。测试专用 snapshot codec 已移入 test，交换 fixture 保留。Spy 以实际 NightWake 事实完成真实方桌交接，不建 Grimoire 镜像；Imp 待通知从 RoleChange 与已确认私密信息推导，不再有两个可写 UI 通知标记。

生产目录 16 旧路径退出（15 删除+1 codec 移到测试）；新增 Session NightFlow 与 AI 仍需的 Family 分类。18 个专属旧测试文件退休；混合真实行为保留/迁移。生产净删 **2278 行/101108B**，测试净删 **1961 行/77387B**。当前超限 App **148697B**、HostScreen **80726B**、Session **53414B**。具体旧→新消费者、完整删除名单及风险见 [当前台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 和代码 diff。

## 下一批

1. 核实此代码精确 Android CI/R2 完整成功，并做 P2 新切换的跨夜/全角色/人数与必要新 APK 真机检查。用户已报告前一版本流程可运行；不冒称本批新 APK 真机/全角色/中断恢复已通过。无付费 API 实验。
2. 然后推进 P3 B/C：白天共享方桌一次日结，删除 EndConfirm；Death/Execution 派生 Klutz 真实选择，删除 Learned 事件/producer/编码/消费者；必要白天/胜负规则归 Host，同批退役旧 UI 控制。
3. 原子持久化/真实进程恢复、AI/展示剩余收敛、源码 <=50000B 与 P5 全树证明仍未完成。不要把同一已确认状态的纯读取重建测试当作落盘/进程恢复验证。

每批列删除生产/专属测试、消费者迁移、净量、超限文件、状态/隐私风险与精确代码 CI/R2。保持 #334/#335 Draft，不 merge/retarget/rebase/force-push/删分支；没有新增所有者决策阻塞。

历史原记录保留于 [交接快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_LOCAL_D2_2026-10-20.md)、[D2 交互矩阵](D2_INTERACTION_ACCEPTANCE_2026-10-20.md) 和台账。旧游标/Pending/Previous/Dawn/展示即提交/MiniMCP NEXT 不恢复。
