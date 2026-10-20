# 当前交接 — Codex 本地 D2（2026-10-20）

## 环境、分支与已验证基线

直接使用 Windows 本地 `C:/Users/Jazz_/source/repos/CampBoardGameHost` 和 PowerShell；不使用 MiniMCP。JDK 可用路径为 `C:/Program Files/Android/Android Studio/jbr`；实际 Gradle/SDK 能力每次核验。

本轮从 `35a2c880` 安全快进到同名远端纯文档提交 `cf3dd516`；未 merge 其他分支、rebase 或强推。已跟踪文件当时干净；原未跟踪 `node_modules/`、`package.json`、`package-lock.json` 不属于本批，不暂存。

#334 Draft/base main/HEAD `76be7e17`；#335 Draft/base #334/分支 `refactor/minimal-night-flow-20261019`。代码 `35a2c880` 的 [完整 Android CI #4479](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401685093) 与 [R2 #4055](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401684985) 成功；不能当作后续代码验证。两个 PR 描述已按当前架构和未完成项更新，未改 base/Draft。

## 唯一当前合同

一个 Session/GameState + 已确认顺序事件 + 稳定 stepId。行动本角色 Next 立即提交；D2 选择→全屏预览→返回同角色→可改选再看→本角色 Next 才提交最后唯一 typed Observation。预览和草稿不持久化，未确认中断可重做；已确认不能用跨角色返回修改。无 Pending/Previous/Dawn/重复公开死亡事务。

Host 执行确定性规则与流程，AI/人工只做真实非唯一裁量；真人玩家查询仍由真人选择。保持实际生产 AI、TB/NGJ、双防重复、事件隐私和原子/stale 安全。

## 已完成范围与本批任务

四角色机械链、即时确定性死亡/继任及夜末/Dawn 删除已有分批证据。D2 已有最终 Next/稳定 Observation、三模式/部分重试和查询绑定、数字草稿隔离、双人最后预览恢复。D2 仍不是全角色或真机验收。

先修复双人信息编辑身份/座位/无对应角色/恢复推荐时的旧预览失效，防止编辑 B 后未重新展示却提交预览 A；同时检查全屏返回和异步推荐不会重置人工草稿。数字类返回后的输入/推荐高亮仍需核查。用真实行为验证，避免只检测源码字符串。

随后核查 FT/侍女/送葬者/守鸦人/贤者等的最后预览、查询变化与 AI 缺失/过期/重试。保留共享确认提示和 typed 结果门禁，不新增预览管理框架、候选领域或持久 UI 状态。旧点目标自动提交路径已列为后续行动确认收敛的明确删除项，不应以“自动模式”代替显式 Next 合同。

每批报告：生产/测试删除与净体量、替换消费者、尚存旧链、超限文件、状态/事件风险、精确代码 SHA 的测试及 CI/R2。不得以构建通过声称设备交互已通过。

## 后续依次推进

1. D2 缺口验收后，统一全夜 `nextStep(Session)` 并同批删旧 UI index、nightStarted、动态 handoff 和过渡 FixedNightStepResolver，处理所有角色/触发/跳过。
2. B/C 白天 Host 结算：同方桌结束与 Death/Execution 派生 Klutz 真实选择。
3. 唯一 Session 原子持久化/安全恢复、全角色/人数回归、全树职责审计与零遗留、源码 <=50,000B 和真机验收。

两个 PR 继续 Draft。当前无 merge/retarget/rebase/force-push/删分支授权；不把 #334 的夜末人工确认中间版单独合入 main。P2 后可重新审查集成点，当前最终验收仍按完整重建/P5 合同。

## 历史证据

全部旧批次和 CI 记录保留于 [原交接快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_LOCAL_D2_2026-10-20.md)，其中的“下一步”均不是现行命令。更早 P0/P1 交接仅供历史取证。当前路线以 [路线图](CURRENT_DEVELOPMENT_ROADMAP.md)、最终架构、A–D2 决策和测试策略为准。
