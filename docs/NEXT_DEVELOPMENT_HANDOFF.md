# 当前交接 — Codex 本地 D2（2026-10-20）

> **最新代码验收（2026-10-20）**：`b5a955e92e45a9ea4c03fff122c165de3bf8a929` 的 [完整 Android CI #4482](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39408336436) 与 [R2 #4058](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39408336439) 均 SUCCESS；Android 全回归/APK、ASP、Clingo 和总门禁通过。本地完整回归 162 suites / 670 tests、0 failure/error/skipped，调试 APK 构建成功。双人/送葬者/守鸦人/贤者的草稿改选失效、步骤隔离和手动输入不被 AI 重置，数字预览输入恢复与占卜师最后预览标记已落地；未作真机或付费 API 验收。

## 环境、分支与已验证基线

直接使用 Windows 本地 `C:/Users/Jazz_/source/repos/CampBoardGameHost` 和 PowerShell；不使用 MiniMCP。JDK 可用路径为 `C:/Program Files/Android/Android Studio/jbr`；实际 Gradle/SDK 能力每次核验。

本轮从 `35a2c880` 安全快进到同名远端纯文档提交 `cf3dd516`；未 merge 其他分支、rebase 或强推。已跟踪文件当时干净；原未跟踪 `node_modules/`、`package.json`、`package-lock.json` 不属于本批，不暂存。

#334 Draft/base main/HEAD `76be7e17`；#335 Draft/base #334/分支 `refactor/minimal-night-flow-20261019`。代码 `35a2c880` 的 [完整 Android CI #4479](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401685093) 与 [R2 #4055](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401684985) 成功；不能当作后续代码验证。两个 PR 描述已按当前架构和未完成项更新，未改 base/Draft。

## 唯一当前合同

一个 Session/GameState + 已确认顺序事件 + 稳定 stepId。行动本角色 Next 立即提交；D2 选择→全屏预览→返回同角色→可改选再看→本角色 Next 才提交最后唯一 typed Observation。预览和草稿不持久化，未确认中断可重做；已确认不能用跨角色返回修改。无 Pending/Previous/Dawn/重复公开死亡事务。

Host 执行确定性规则与流程，AI/人工只做真实非唯一裁量；真人玩家查询仍由真人选择。保持实际生产 AI、TB/NGJ、双防重复、事件隐私和原子/stale 安全。

## 已完成范围与最新 D2 批次（本地及精确代码 CI/R2 已通过）

四角色机械链、即时确定性死亡/继任及夜末/Dawn 删除已有分批证据。D2 已有最终 Next/稳定 Observation、三模式/部分重试和查询绑定、数字草稿隔离、双人最后预览恢复。D2 仍不是全角色或真机验收。

本批已修复双人信息编辑身份/座位/无对应角色/恢复推荐的旧预览失效；去掉以 lastPreview 为重建条件的 remember key，避免失效回调反过来清空正在编辑的选择。送葬者、守鸦人、贤者也接入旧预览失效，并以步骤/实际查询身份隔离草稿，去掉 AI 推荐变更导致的人工编辑重置。数字组件从当前查询匹配的最后预览恢复输入；占卜师结果按钮突出最近预览。所有这些仍是可丢弃 UI 草稿，沿原 Next→Observation 提交入口，不修改 Session 事实模型或引入新状态框架。

下一步核查三模式的实际 UI 路径、AI 缺失/过期/重试，以及查询变化的端到端行为。保留共享确认提示和 typed 结果门禁，不新增预览管理框架、候选领域或持久 UI 状态。旧点目标自动提交路径已列为后续行动确认收敛的明确删除项，不应以“自动模式”代替显式 Next 合同。

本批验证与净体量：本地 `:app:testFast` 通过；最终 `:app:testFull :app:assembleDebug` 通过，162 suites / 670 tests，0 failure/error/skipped。新增两个查询/结构绑定行为用例；首次运行新增用例尝试构造负数，被既有 NumericResult 构造器直接拒绝，去掉这条无效测试数据后完整重跑成功。没有设备或付费 API 验收。

本批 8 个生产文件 +101 行/+4,620B、2 个测试文件 +41 行/+2,476B，未整删生产/测试文件；这是 D2 合同修复，不能冒称净删。替换的是各角色的推荐驱动 remember 重置/不通知父级的局部改选路径，统一复用原 onInformationDraftChanged→清空最后预览。无新 Pending/Session/流程游标/候选引擎。现有 UI index/dynamic handoff 尚存，下一阶段必须整体迁移删除。超限源码仍是 App 150,261B、HostScreen 94,508B、Session 54,310B，三者本批未增长。远端代码 b5a955e9 的完整 CI #4482/R2 #4058 通过，remote compare 与本地 12 文件变更一致。

每批报告生产/测试删除与净体量、替换消费者、尚存旧链、超限文件、状态/事件风险、精确代码 SHA 的测试及 CI/R2。不得以构建通过声称设备交互已通过。

## 后续依次推进

1. D2 缺口验收后，统一全夜 `nextStep(Session)` 并同批删旧 UI index、nightStarted、动态 handoff 和过渡 FixedNightStepResolver，处理所有角色/触发/跳过。
2. B/C 白天 Host 结算：同方桌结束与 Death/Execution 派生 Klutz 真实选择。
3. 唯一 Session 原子持久化/安全恢复、全角色/人数回归、全树职责审计与零遗留、源码 <=50,000B 和真机验收。

两个 PR 继续 Draft。当前无 merge/retarget/rebase/force-push/删分支授权；不把 #334 的夜末人工确认中间版单独合入 main。P2 后可重新审查集成点，当前最终验收仍按完整重建/P5 合同。

## 历史证据

全部旧批次和 CI 记录保留于 [原交接快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_LOCAL_D2_2026-10-20.md)，其中的“下一步”均不是现行命令。更早 P0/P1 交接仅供历史取证。当前路线以 [路线图](CURRENT_DEVELOPMENT_ROADMAP.md)、最终架构、A–D2 决策和测试策略为准。
