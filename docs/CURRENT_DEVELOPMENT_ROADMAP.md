# 当前开发路线 — Minimal Host（2026-10-21）

**最新代码检查点：`311191a30d6b0d83b5cf2d6de2a67905b526e596`。** 已执行无用途净删，并把全夜当前步骤切换到 Session 事实；详见 [全源码台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。本地 fast 593 / full 616 tests（含 23 个真实 UI 用例）及 Debug APK 通过。精确代码 [完整 CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS，[R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS；不能用旧代码或后续 docs-only 的绿灯代替。

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 和最新用户指令为准。当前直接使用 Codex 本地 Windows 文件，不使用 MiniMCP。用户已报告前一测试版本真机流程可运行、细节待优化，并要求优先清理；不视为本次新代码全部真机验收。

## 2026-10-21 当前工作进度（未验收工作树）

用户已授权继续收尾 P2、推进 P3 和最终收敛。当前分支 `refactor/minimal-night-flow-20261019`，本轮起点为 docs-only `8024613ead6935d0baf4dea0d07638f82c921153`。**本节记录本地未提交代码，不表示代码已经推送、可发布或通过验收；本次同步仅提交进度文档。** 最近已验收代码仍为上面的 `311191a3` / CI #4486 / R2 #4062。

| 范围 | 当前进度 | 验收状态 |
|---|---|---|
| P2 | 保留已通过的 Session 全夜路由；本轮追加夜间攻击/市长结果与胜负事务衔接 | 跨夜、全部角色/人数及新 APK 真机验收尚未补齐，不能宣布 P2 收尾 |
| P3 B/C | 本地已切断 EndConfirm/独立日结页；Klutz 从实际 Death/Execution 派生真实选择，移除 Learned 类型和 producer；App 处决/继任/胜负逻辑迁入 Session DayFlow | 仍在实施，尚未通过测试和独立 CI/R2 |
| P3 白天动作 | 本地新增 Session 提名/投票/鬼票、Virgin/Spy 裁量、Slayer/Recluse 裁量、Saint/Mayor/Klutz/普通胜负；Artist 实际答案和能力消耗改为正式事实/私密 Observation | 需专项行为测试、幂等/原子/隐私与 UI 回归；不把源码存在当作规则正确性证明 |
| 原子保存/恢复 | 本地新增单一 Session JSON、应用私有 noBackup AtomicFile、恢复入口；AI 建议记忆/会话与开局轮换元数据随存档保存；预览与未确认请求不保存 | 尚无完整落盘、故障注入、真实进程恢复证明；错误呈现和恢复时流程还需核验 |
| 最终收敛 | 白天旧控制开始退出，代码仍有 3 个超限文件 | P0/P5 全树语义审计、余留 UI/AI 职责与文件大小目标未完成 |

**最近本地验证：** `:app:testFast` 在 `:app:compileDebugUnitTestKotlin` 失败；`ClocktowerArtistDraftTest` 仍按旧单参数回调调用，生产接口已改为 `(claimant, truthful, shown)`。该次生产 Kotlin 编译成功，但测试没有执行，不能报 fast/full/APK 通过。本轮未提交代码没有新的远端 CI/R2 ID。R2 仍检查旧 Slayer `targetDied` UI 字符串，须在确认真实规则/裁量行为后同步退休旧守卫，不能恢复人工任意判死来过门禁。

**本地物理变更快照（含尚未跟踪的新 Kotlin，非最终净删成绩）：** 删除生产 `ClocktowerDayScreen.kt`、`NoGreaterJoyKlutzHistoryProducerV1.kt`；删除旧人工判死 UI 专属 `ClocktowerSlayerDraftTest.kt`；混合 Klutz/UI 测试在迁移。新增 6 个生产文件（DayFlow、GameEndReason、OutcomePresentation、SessionJson、ActiveGameStore、ActiveGameMetadata）；生产 Kotlin 212→216，字节净变化 **-3B**；测试 150→149，净 **-2846B**。这些临时数量不代表功能或清理验收。

当前超限源码：App **125487B**、HostScreen **79274B**、Session **54513B**。仍须先删除不必要职责，再按内聚职责整理到每文件 <=50000B，禁止仅为达标增加通用 Context/Manager 或第二游戏状态。

**立即接续：** 修复/迁移真实测试合同 → 补 DayFlow 机械行为、重复提交/回滚/保存失败及恢复测试 → 核验提名后未完成投票的事实派生、白天裁量和胜负优先级 → P2 跨夜/角色/人数轨迹 → 完整 testFast/testFull/APK/R2 → 通过后提交代码、推送并核验精确 SHA 独立 CI/R2 → 完成余留 UI/AI、大小与全树证明。两内置剧本、现有 AI 和双跨局防重复继续保护。

## 已验收的上一批事实（2026-10-20）

- main `0abc842e`；#330–#333 已合并。#334 基线 `76be7e17` / base main；#335 仍基于 #334 的 `codex/minimal-host-full-scan-20261018`，保持 Draft。
- 已有代表性 Poisoner→Monk→Imp→Butler 即时 Session 机械链、确定死亡/继任、夜末自由判死和独立 Dawn/重复公开死亡事务删除。
- D2 保持预览→返回同角色/改选→显式 Next 唯一 Observation；无 UI 持久草稿或跨角色 Previous。
- **本批已完成**：移除 UI night index/start/handoff/自动提交，NightFlow 从 Script+Session 已确认事实决定步骤；旧 Planner/Projector/First/Other flow、Fixed/Dynamic resolver、毒游标 chronology、攻击预测适配整链退出。无用途首夜生命周期、旧登记/投影 helper、Homebrew 分支也已净删；codec 移入测试。AI 全链、两内置剧本、两种跨局防重复保留。
- 生产净删 2278 行/101108B，测试净删 1961 行/77387B；18 个专属旧测试文件退休，真实不变量移至 Session/实际 UI 回归。清理详情、最小 Spy wake 事实及 Imp 通知完成证据见台账。

## 已批准的阶段顺序（当前具体 NEXT 见上节）

1. 接受本批精确代码的独立完整 Android CI/R2，完成当前 P2 切换后的跨夜/角色/人数和必要新 APK 真机回归；不要再恢复旧游标或无限补非阻塞 D2 细节。
2. P3：B 同一白天方桌一次日结；C 从 Death/Execution 派生 Klutz 真实选择，删 EndConfirm / KlutzLearnedDeath 整链；白天与胜负的必要确定性结算归 Host，并同批删除界面层旧控制。
3. P4/P5：唯一 Session 原子存储与真实安全续局、AI/展示剩余职责收敛、TB 5–15 / NGJ 5–6 完整轨迹、全树删除证明、全部源码 <=50000B 和完整 CI/R2/必要真机验收。

P0 全树语义归属审计仍未完结；当前 212 生产 Kotlin/150 测试文件的库存不等于语义验收。不回溯重建旧世界/合法域引擎，不引入通用管理框架或第二可写 GameState。每条职责迁移当批删旧，不等合并后再清。

已验收 `311191a3` 的超限文件为 App **148697B**、HostScreen **80726B**、Session **53414B**；当前工作树大小见上节。先净删职责，再按必要内聚职责整理；最终大小门槛未满足。

## 集成边界

两个堆叠 PR 保持 Draft；不单独合入 #334 的夜末人工确认中间设计。成功 CI 不等于合并授权；merge/retarget/rebase/force-push/删分支仍需明确批准。必要事实/隐私/身份/去重/原子/stale、AI 首夜/后夜/transport/parser/memory/modes、两内置剧本及双防重复必须保留。

历史证据保留于 [原路线快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_LOCAL_D2_2026-10-20.md)、[D2 验收](D2_INTERACTION_ACCEPTANCE_2026-10-20.md) 与完整台账；其中旧 NEXT 不是当前命令。
