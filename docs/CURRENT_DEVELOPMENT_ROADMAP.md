# 当前开发路线 — Minimal Host（2026-10-20）

**最新代码检查点：`311191a30d6b0d83b5cf2d6de2a67905b526e596`。** 已执行无用途净删，并把全夜当前步骤切换到 Session 事实；详见 [全源码台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。本地 fast 593 / full 616 tests（含 23 个真实 UI 用例）及 Debug APK 通过。精确代码 [完整 CI #4486](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032184) SUCCESS，[R2 #4062](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39419032113) SUCCESS；不能用旧代码或后续 docs-only 的绿灯代替。

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 和最新用户指令为准。当前直接使用 Codex 本地 Windows 文件，不使用 MiniMCP。用户已报告前一测试版本真机流程可运行、细节待优化，并要求优先清理；不视为本次新代码全部真机验收。

## 当前事实

- main `0abc842e`；#330–#333 已合并。#334 基线 `76be7e17` / base main；#335 仍基于 #334 的 `codex/minimal-host-full-scan-20261018`，保持 Draft。
- 已有代表性 Poisoner→Monk→Imp→Butler 即时 Session 机械链、确定死亡/继任、夜末自由判死和独立 Dawn/重复公开死亡事务删除。
- D2 保持预览→返回同角色/改选→显式 Next 唯一 Observation；无 UI 持久草稿或跨角色 Previous。
- **本批已完成**：移除 UI night index/start/handoff/自动提交，NightFlow 从 Script+Session 已确认事实决定步骤；旧 Planner/Projector/First/Other flow、Fixed/Dynamic resolver、毒游标 chronology、攻击预测适配整链退出。无用途首夜生命周期、旧登记/投影 helper、Homebrew 分支也已净删；codec 移入测试。AI 全链、两内置剧本、两种跨局防重复保留。
- 生产净删 2278 行/101108B，测试净删 1961 行/77387B；18 个专属旧测试文件退休，真实不变量移至 Session/实际 UI 回归。清理详情、最小 Spy wake 事实及 Imp 通知完成证据见台账。

## 接续顺序

1. 接受本批精确代码的独立完整 Android CI/R2，完成当前 P2 切换后的跨夜/角色/人数和必要新 APK 真机回归；不要再恢复旧游标或无限补非阻塞 D2 细节。
2. P3：B 同一白天方桌一次日结；C 从 Death/Execution 派生 Klutz 真实选择，删 EndConfirm / KlutzLearnedDeath 整链；白天与胜负的必要确定性结算归 Host，并同批删除界面层旧控制。
3. P4/P5：唯一 Session 原子存储与真实安全续局、AI/展示剩余职责收敛、TB 5–15 / NGJ 5–6 完整轨迹、全树删除证明、全部源码 <=50000B 和完整 CI/R2/必要真机验收。

P0 全树语义归属审计仍未完结；当前 212 生产 Kotlin/150 测试文件的库存不等于语义验收。不回溯重建旧世界/合法域引擎，不引入通用管理框架或第二可写 GameState。每条职责迁移当批删旧，不等合并后再清。

超限生产文件仍为 App **148697B**、HostScreen **80726B**、Session **53414B**。先净删职责，再按必要内聚职责整理；最终大小门槛未满足。

## 集成边界

两个堆叠 PR 保持 Draft；不单独合入 #334 的夜末人工确认中间设计。成功 CI 不等于合并授权；merge/retarget/rebase/force-push/删分支仍需明确批准。必要事实/隐私/身份/去重/原子/stale、AI 首夜/后夜/transport/parser/memory/modes、两内置剧本及双防重复必须保留。

历史证据保留于 [原路线快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_LOCAL_D2_2026-10-20.md)、[D2 验收](D2_INTERACTION_ACCEPTANCE_2026-10-20.md) 与完整台账；其中旧 NEXT 不是当前命令。
