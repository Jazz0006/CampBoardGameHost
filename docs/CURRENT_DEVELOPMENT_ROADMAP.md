> **独立审计跟进**：已修正继承的配置加载覆盖输入竞态，以及本批 live getter 导致 stale AI 请求退出后 busy 不清除的回归；新增 3 项设置 UI 测试；读取门槛版本 `23590475` 的完整回归 629（30 UI）/APK、CI #4493/R2 #4069 均通过，finally 跟进修正以最新精确 SHA 验收。另通过真实 Session 反例确认基线既有“投毒者死亡未解除目标中毒”缺陷，需作为下一独立 Session 批次优先修复。不得据此前绿灯宣称无 BUG。见 [审计报告](MINIMAL_HOST_P4_REVIEW_2026-10-21.md)。

# 当前开发路线 — Minimal Host（2026-10-21）

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 与最新用户指令为准：唯一 Session/GameState 与有序确认事实，Host 执行确定性规则，AI/真人负责实际裁量。不恢复旧通用候选/世界枚举或第二可写游戏状态。

## 当前基线

最新生产代码 **`f852b04f81f13fa35b08ecade8f6ccce4018e48f`** 已推送 [Draft #335](https://github.com/Jazz0006/CampBoardGameHost/pull/335)，仍堆叠于 #334 `76be7e17`；main 为 `0abc842e`。本批基线 `ce1c314c`，无合并、改 base 或历史重写。

- 夜间进度由 Session 确认事实与内置夜序派生，旧 UI 游标/Start/交接推进链已退役；行动显式 Next 提交机械结果。
- D2 信息可全屏预览、返回同角色重选，仅 Next 正式记录最终一条 Observation。预览不是事实，不持久保存。
- 白天同方桌一次日结；Session 拥有提名、票/鬼票、技能、处决、继任、胜负；Klutz 从 Death/Execution 派生真实选择。独立 EndConfirm 和 Learned 事件链已删除。
- 唯一活动 Session 通过私有 noBackup AtomicFile 原子保存，确认失败回滚；恢复已确认事实、AI 已接受记忆/会话与开局轮换元数据，未确认预览/AI 请求丢弃。

## 本批：P4 配置职责及孤立旧规则清理

AI 连接配置、加载、700ms 安全延迟保存与设置表单归 `StorytellerAiSettings`/原设置面板；生命周期仍在 App 根部，本局模式与持久默认分开。开局/后夜请求、stale 校验及 Session 保留原职责。原表单 16 参数及 App 重复接线退休。

确认无生产消费者后，物理删除 `DemonNightAttackSemantics`、`PoisonEffectLifecycle` 及两项专属测试文件，另删 Session 无调用的死亡 roster helper。真实攻击、保护、中毒、死亡、市长及继任的 Session 实现与行为测试保留。

计入新增文件，生产 Kotlin **净 -84 行 / -3381B**；专属旧测试 **-146 行 / 13 项**。当前 App **118275B**、Night HostScreen **68496B** 仍超过 50000B；Session **49379B**，新设置文件 **3507B**。完整消费者/删除证据见 [审计台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 验证

Codex Cloud 实跑 full **626 tests / 144 suites，含 27 UI**，0 failure/error/skipped；Debug APK、原始 R2 shell、diff 检查通过。精确代码 [CI #4492](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39549387016) **SUCCESS**；[R2 #4068](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39549387006) **SUCCESS**。旧 #4490/#4066 属于前一代码，不替代本批证明。

## 下一步

1. 优先修复审计 F2 的投毒来源死亡/身份退出后失效、事实及恢复一致性，然后继续夜间 Host 的角色展示/机械判断和 AI/UI 提交边界审计，核对每个保留状态、生产消费者、保存入口与错误路径；不是只按零引用或名字删除代码。
2. 两个超限文件先净删，再按真实内聚职责收敛至全部源码 <=50000B，禁止机械切块、通用 Context/Manager 或影子 Session。
3. 完成 P0/P5 全树语义归属与零遗留证明；按两剧本逐角色核对触发、真人输入、确定/裁量输出及代表性组合，尤其更广泛强制线索计算。
4. 新 APK 真机操作、杀进程及安全续局验收。已有 TB 5–15 / NGJ 5–6 显式 fixture 两夜证据，不等于全部合法阵容或真机证明。

当前在 Codex Cloud `/workspace/CampBoardGameHost` 直接读写/测试并用 Git/GitHub 处理分支及 PR，不依赖 MiniMCP 或开发机。工具链与接续见 [交接](NEXT_DEVELOPMENT_HANDOFF.md)。保持 #334/#335 Draft；保护生产 AI 全链、两剧本、双跨局防重复、隐私/有序事实/去重/stale。不进行付费 API、合并、retarget、rebase、force-push 或删分支。
