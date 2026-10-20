# 当前开发路线 — Minimal Host（2026-10-20）

> **最新代码验收（2026-10-20）**：`b5a955e92e45a9ea4c03fff122c165de3bf8a929` 的 [完整 Android CI #4482](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39408336436) 与 [R2 #4058](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39408336439) 均 SUCCESS；Android 全回归/APK、ASP、Clingo 和总门禁通过。本地完整回归 162 suites / 670 tests、0 failure/error/skipped，调试 APK 构建成功。双人/送葬者/守鸦人/贤者的草稿改选失效、步骤隔离和手动输入不被 AI 重置，数字预览输入恢复与占卜师最后预览标记已落地；未作真机或付费 API 验收。

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 和最新用户指令为准。当前在 Codex 本地 Windows 环境直接读写仓库；不使用 MiniMCP。

## 当前事实与证据

- main：`0abc842e`，#330–#333 已合并。
- #334：Draft，`codex/minimal-host-full-scan-20261018`，HEAD `76be7e17`，base main。
- #335：Draft，`refactor/minimal-night-flow-20261019`，base #334。此次本地已安全快进到远端文档提交 `cf3dd516`，父提交即代码 `35a2c880`。
- 代码 `35a2c880` 的 [完整 Android CI #4479](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401685093) / [R2 #4055](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39401684985) 成功。后续代码必须取得独立证据；docs-only 成功不证明 Android。
- 已分批实现四角色即时 Session 机械链、即时 Death/继任边界、删除夜末自由判死和 Dawn/重复公开死亡事务；D2 已实现预览/返回/最终 Next 单条 Observation、三模式及部分 stale/query 防护、数字隔夜隔离和双人预览恢复。以上不是全夜/全角色验收。

## 当前执行顺序

1. 更新落后文档和 #334/#335 描述；当前状态与历史记录分开，旧记录归档保留。
2. 收尾 D2（本批代码已修复双人/角色/Sage 草稿失效、移除异步推荐重置人工草稿、数字预览恢复及 FT 最近结果高亮；本地全回归 670 tests/APK 通过，精确代码 b5a955e9 的 CI #4482/R2 #4058 通过）：改任何信息草稿（包括双人身份/座位）必须使旧预览失效；全屏返回重现最后预览；异步 AI 不覆盖人工编辑；缺失/失败/过期可重试，实际查询目标与结果一致。只在本角色 Next 写唯一 Observation。
3. 统一全夜 `nextStep(Session)`：替换并同批删除 UI `nightStepIndexState`、`nightStartedState`、`pendingNightAdvance`、旧 Dynamic/Fixed resolver 控制链。清理点目标自动提交的旧路径，恢复所有行动的显式 Next 确认；保留 AI 裁量/预选，不让 AI 代替玩家选择。
4. P3：B 同一白天方桌一次日结，C 从 Death/Execution 派生 Klutz 真实选择；确定性结算和胜负归 Host。
5. P4/P5：AI 三模式同一入口、唯一 Session 原子存储和安全续局、TB 5–15/NGJ 5–6 全角色与代表性交互、全树删除证明、全部源码 <=50,000 bytes、完整 CI/R2 和必要真机验收。

P0 当前树的完整语义归属审计仍未完结，已有枚举和关键链审查不算全树验收；随业务迁移补齐原台账。不回溯重建已删规则，不恢复通用候选/评分/世界引擎，不引入持久 Pending 或第二 GameState。

## 审计区分与集成边界

UI 游标/交接、Previous 空参数和超限文件属于已规划但未完成的清理。双人草稿未失效属于已要求行为的实现遗漏；点目标自动提交属于需要明确退役的旧行为。临时预览和普通输入草稿是必要 UI 状态，不以多一个变量就判定第二事实权威。

当前超限生产文件：App 150,261B、HostScreen 94,508B、Session 54,310B（本地代码基线实测；后续重测）。优先删旧职责，再按内聚职责整理。

每条链迁移完成即删旧，不等 PR 合并。P2 完成后可审查独立安全集成点，但当前合同未批准提前合并；最终 main 接受仍需剩余整局/恢复/P5 证据。#334 保留被 #335 替代的中间设计，不因 CI 绿单独先合入。保持两个 Draft；merge/retarget/rebase/force-push/删除分支须明确批准。合并后 main 验证通过且无依赖 PR，才清理相应分支。

保护现有 AI 全链、两内置剧本、跨局阵容和同玩家身份两类防重复，以及事实顺序、私密范围、去重、原子与 stale 安全。无真机或付费 API 验收声明。

## 历史记录

旧路线完整保留于 [本地 D2 接续前快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_LOCAL_D2_2026-10-20.md)。其中 Pending、Previous、Dawn、旧展示即提交和旧 MiniMCP NEXT 均不是现行命令。当前唯一接续动作见 [交接](NEXT_DEVELOPMENT_HANDOFF.md)。
