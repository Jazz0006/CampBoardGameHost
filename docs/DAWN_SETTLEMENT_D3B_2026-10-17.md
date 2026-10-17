# 第四阶段 D3b：黎明死亡、身份与阶段结算

基线：D3a `1a3e34c7`（#321，完整 CI/R2 已通过）。

Architecture pre-flight:
- current owner: App 三处 Dawn materialization 调用复制存活/角色/阶段及全部事实、观察 ID；Session 已拥有死亡+公开观察、身份与阶段的原子提交和判重。
- proposed responsibility: 删除中间状态/计划，直接调用现有 Session 边界；只保留死亡和角色的稳定提交标识。阶段重复推进继续由现有 App 投影检查与 Session 合法性边界处理。
- authoritative state owner(s): Session 当前状态和结构化时间线；规则 planner 决定死亡与继任；App 拥有界面导航和 AI 失效通知。
- narrow typed input/output seam: 已有 ActionFactDraft.Death + observation ID、RoleChange、PhaseAdvance -> TimelineBoundActionFact；无需增加覆盖全部结算的容器或扩大 Session 主类。
- keep in current owner / extract: 删除 NightDawnDurableMaterializationPlanner 及五个 DTO；仅保留稳定 ID 的具名 DawnCommitIdentity。已有 callable Session 边界足够，不新增整局协调器。
- reason: 重复 ID 检索和半写入修复分支已无必要，死亡及公开观察必须同一原子提交，不能补造历史。

扇出：App onConfirmNewDemon 的身份/阶段，onConfirmNight 的死亡/公开观察与正常 Dawn 阶段；commitClocktowerActualRole 的另一消费者为白天猩红女郎晋升，保持现状；commitClocktowerPhaseAdvance 所有调用继承原有阶段投影与合法性验证，移除仅 Dawn 使用的可选 stableActionId。其余处决、Slayer、圣女及日间公开观察 preflight 不变。

验收：同轮同死亡/身份再次提交不增加事实、观察或修订；旧半写入不补写；死亡观察紧跟死亡且早于后续阶段；Klutz 死亡前证据与继任/市长规则保留；错误和过期的新提交失败不变。已有 AliveBoundary、RoleIdentityBoundary、phase/chronology 与 NightDawnResolutionPlanner 测试保护语义；用真实 Session 集成回归替代已删除 planner 的模拟重试测试。无真机或付费模型验收。

## 完成范围与证据映射

- NightDawnDurableMaterializationPlanner 及五个中间 DTO 删除，只留下 24 行 DawnCommitIdentity；死亡、公开观察与角色 ID 沿用原格式。阶段使用既有通用推进入口，当前 phase/round 由 Session 投影，重复推进无操作。
- App 直接提交已通过规则 planner 的结果。仅在重试时找单个动作的原 sequence，内容校验和实际判重仍由 Session 负责；不复制全量 ID 集。死亡观察由同一原子提交发布，新增事实后才通知 AI。原先专属 Dawn 的 preflight 不再需要，因为 Session 在变更前完成事实与观察校验；日间 preflight 保留。
- 退休 NightDawnDurableMaterializationPlannerTest 的三个测试：首次/重复计划由 DawnCommitBoundaryTest 真实死亡、观察、身份、阶段提交顺序与重试代替；半写入信号模拟改为真实 Session 拒绝补造观察。新增过期提交状态不变用例。既有 AliveBoundary、RoleIdentityBoundary、PhaseAdvance 以及市长/继任/中毒/Klutz 规则回归保留。
- App 3,887 → 3,768 行，221,785 → 212,701 bytes；生产源码净减少 243 行。Host 不变。没有扩张 Session 主类或新增全局 Context。

下一步 D4：Host 白天/黎明分流与草稿归属。D3 的重复结算计划清理完成，不代表 App/Host 大文件治理完成，更不代表事件覆盖或最小续局已完成。

验证：本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 全部通过，完整 1,332 / 快速 1,325，失败、错误、跳过均为 0；三个旧 planner 测试由三个真实 Session 回归替代。测试与 APK 编译打包实际执行，`git diff --check` 通过。远端检查以当前 PR head 为准，保持 Draft 未合并。
