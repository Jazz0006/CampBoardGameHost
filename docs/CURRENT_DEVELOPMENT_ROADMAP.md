# 当前开发路线 — Minimal Host（2026-10-21）

**当前代码：`f4d78d5c70c55be988c1daf29558fde2e0c82305`，已推送原 Draft #335。** 本批完成 P3 B/C 和主要白天/胜负消费者迁移，补入单一 Session 的原子保存/安全重读。精确代码：[完整 Android CI #4490](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956510) **SUCCESS**；[R2 #4066](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39496956507) **SUCCESS**。本地 fast **612** / full **639**（含 **27** 个真实 UI 用例），0 failure/error/skipped；Debug APK 和工作流原始 R2 脚本通过。尚未有本批新 APK 的真机杀进程/现场验收，不能宣布 P2/P3/P5 全部完成。

以 [最终架构](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)、[A–D2 决策](MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) 和最新用户指令为准。Codex 本地直接读写，不使用 MiniMCP。

## 本批已实施且本地验证的职责

- **B 同页日结**：共享白天方桌一次确认，Session 接受已确认最高票/平票结果，执行处决/无人处决、特殊能力、继任、胜负或下一夜；独立 EndConfirm/ExecutionConfirm 页面、枚举和整链删除。
- **C 真实 Klutz 选择**：从实际 Death/Execution 派生，夜死到 Day 才出现；KlutzChoice 引用 deathActionId，保留真人选存活座位与善恶结果。删除 Learned 事件/producer、App pending/resume 标志。
- **白天与胜负归 Session**：提名、投票/鬼票/管家、Virgin/Spy、Slayer/Recluse、Saint/Mayor/普通终局/确定继任及 Artist 实际私密答案/消耗均通过 Session。App 不再计算 evaluateGameOutcome 或另写 gameOutcome；一个 GameEnded 正式事实提供只读结果。确定结果不让人任意判死；真实登记裁量保留。
- **待投票事实派生**：提名已确认但票未确认时，重启后直接继续该投票，未完成不允许结束 Day/改提名。日结、提名、投票携带显示时轮次；夜间确认冻结显示阶段/轮次，防止旧按钮把动作写进下一阶段。
- **原子活动局**：仅保存一个 Session 到应用私有 noBackup AtomicFile；确认失败不发布新状态，整批日结/死亡/继任/终局回滚。保存失败可重试，坏存档保留并拒绝猜测恢复；预览/未确认 AI 请求不保存。保存并恢复已确认事实/收件人、AI 接受后的建议记忆/会话及原始开局轮换元数据。
- **职责与删旧**：DayHostScreen 自持白天展示草稿，App 的 day-mode/reset/Artist/Klutz 临时控制退出；不可变 SessionState 与提交机制分开，Session 文件 **49577B**，已符合上限。生产删除 2 文件、专属旧测试删除 2 文件；真实不变量迁到 Session/文件存储/Compose 回归。
- **P2 轨迹补证**：TB 5–15、NGJ 5–6 的显式角色 fixture 各跑两夜，覆盖固定夜序、确定动作、确认信息和读盘重建下一步；既有毒/保护/死亡触发/继任行为测试保留。这不是全部合法阵容组合或真机证明，未宣称全部信息函数已自动生成。

## 清理实量与剩余工作

对文档检查点 `25c0aca0` 的本批代码比较（计入全部新增文件、Git blob LF 字节）：生产 **+1265/-1373，净 -108 行、+1775B**；测试 **+588/-176，净 +412 行、+27595B**。本批新增必要的白天结算/存储及行为证明，不伪称生产字节净删。生产 Kotlin **217** / 测试 **150** 文件，库存不等于全树语义验收。完整删旧/消费者台账见 [审计顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。上一批 `311191a3` 的净删 2278 生产行/101108B 与本批统计分别归属，不混加冒称本批成果。

**下一步**：上述精确代码 Android CI/R2 已核实成功；对照两内置角色合同检查余留 UI 机械判断/所有提交与恢复入口 → 清理 App **122572B**、Night HostScreen **68496B** 两个超限文件（先净删，再内聚整理，全部 <=50000B）→ P0/P5 全树语义归属/零遗留证明 → 新 APK 必要真机及杀进程续局验收。不能用任意拆文件、通用 Context/Manager 或第二可写 GameState 达标。Chef/Empath/FT 等更广泛强制信息计算仍需按合同单独证明，不恢复旧通用候选引擎。

main 仍为 `0abc842e`；#334 基线 `76be7e17`，#335 保持其堆叠 Draft。保留 AI 全链、两剧本、双跨局防重复、隐私/顺序/去重/stale；不进行付费 API 实验。未 merge/retarget/rebase/force-push/删分支，没有新所有者决策阻塞。
