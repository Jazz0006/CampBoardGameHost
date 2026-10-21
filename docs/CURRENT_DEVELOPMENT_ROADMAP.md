# 当前开发路线 — Minimal Host（2026-10-21）

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 和最新用户指令为准：唯一 Session/GameState 与有序确认事实，Host 执行确定性规则，AI/真人负责实际裁量，不恢复通用候选/世界枚举或第二游戏状态。

## 当前批次：修复 F2 投毒来源退出

在 `2dea23bc` 基线上接续原 Draft #335，仍堆叠于 #334 `76be7e17`；main 为 `0abc842e`。前一已验收生产代码 `bc263581` 的 CI #4494/R2 #4070 不替代本批验收。

- 已确认投毒者 Death/Execution/RoleChange 使其死亡或不再拥有实际 Poisoner 身份时，在 Session 唯一状态发布入口立即解除持续毒，并复用既有 Poison(null) 结束事实。
- 夜间攻击、白天结算、确认死亡和身份变化共用这一边界；没有新持久字段、第二状态/存档、规则引擎或 UI 补写器。没有活动毒、非来源死亡或保护成功不会制造毒结束；已确认的玩家信息保持原样。
- 状态与毒结束事实一次确认保存，失败整步回滚；真实文件中断/重读后恢复正确下一步。重复/过期确认不重复结束，也不复活旧目标。
- 旧时间线回归中的“处决投毒者仍中毒到黄昏”预期已纠正，保留普通玩家被处决时中毒持续到黄昏的覆盖。耐久新增来源退出 9 项、真实文件 1 项，并把旧单场景拆成两场景；不恢复已删除的孤立旧规则测试。

Cloud `testFull + assembleDebug` **640 tests / 146 suites / 30 UI，0 failure/error/skipped**，原 R2 shell 与 diff 检查通过。首批 8 个定向场景在修复前有 5 个真实失败。精确代码 `6a76a57cfa8034363abbf884d1a0326edc1c15a4` 的完整 Android CI #4496、R2 #4072 均 SUCCESS（包含 full tests / Debug APK、ASP / Clingo / gate）；未做真机/杀进程或付费 API。

本批生产 **+33 行 / +1904B**（必要 bug 修复，不伪称净删），删除生产/专属测试文件均为 0；测试净 +187 行。App **118544B**、Night HostScreen **68496B** 仍超 50000B；Session **49453B** 达标。所有入口、保留消费者和证据见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 与 [审计 F2](MINIMAL_HOST_P4_REVIEW_2026-10-21.md)。

## 已有基线与边界

Session 拥有夜间步骤/确定性机械、白天提名/投票/处决/胜负、Death 派生 Klutz 和单一 AtomicFile 活动局。D2 信息可预览重选，仅当前角色 Next 记录最终一条 Observation；预览不是事实。AI 配置职责已收敛，继承的读取竞态及本批暴露的 stale AI busy 问题已修复验证。

## 下一步

1. 核实本批精确代码 CI/R2；随后继续 Night Host 的角色展示/机械判断、AI/UI 确认边界及保存/恢复入口职责审计。
2. 两个超限文件先净删，再按真实内聚职责收敛至全部源码 <=50000B，不用机械切块、泛用 Context/Manager 或影子 Session。
3. 完成 P0/P5 全树语义归属与零遗留证明、两剧本逐角色合同、更多强制线索及代表性交互证据。
4. 新 APK 真机操作与杀进程安全续局。TB 5–15/NGJ 5–6 显式两夜 fixture 不等于所有合法阵容或设备验收。

Codex Cloud 直接读写/测试并使用 Git/GitHub 同步分支及 PR，不依赖 MiniMCP 或开发机。保留生产 AI 全链、两剧本、双跨局防重复、隐私/顺序/去重/stale。#334/#335 保持 Draft，不合并/改 base/重写历史/删分支，不调用付费 API。环境与接续见 [交接](NEXT_DEVELOPMENT_HANDOFF.md)。
