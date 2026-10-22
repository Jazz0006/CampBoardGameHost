# 第四阶段 D4e：白天/黎明提前分流

基线 D4d #326 `351b32c1`。

Architecture pre-flight:
- current owner: ClocktowerJudgeScreen 同时持有跨阶段 UI 状态、阶段路由及夜间信息准备。
- proposed responsibility: 在执行夜间投影和 materializer 前完成优先展示、新恶魔确认、黎明及全部白天分支；本步只调整编排顺序。
- authoritative state owner(s): Session 继续拥有正式身份/进度；原 remember 草稿、展示令牌和首夜异步预计算状态继续由 Host 拥有，key 不变。
- narrow typed input/output seam: Day 登记使用现有 RegistrationProjection，输入已确认 poisonTarget 和 Session 按座位的 actualRole；Night 保留原时序投影函数。原交互回调不变。
- keep in current owner / extract: 保留现有路由所有权，不新增 Context、状态副本或大参数组件。本步为后续夜间边界拆分解除日间依赖。
- reason: 白天不应经过夜间死亡解析、继任、唤醒流程及信息 materializer；跨阶段记忆不能随提前 return 被销毁。

扇出核对：Day 仅提名/Virgin 和 Slayer 消费登记投影；它们原先通过 NightHostProjection 的非 Night 分支取得 poisonTarget 和 gameState.playerAt(cards.indexOf(card)+1).actualRole。新日间适配保持完全相同取值。FirstNight 和 Night 的登记仍使用原投影。黎明与新恶魔确认不消费登记。

生命周期：跨阶段保留 recommendationCoordinator、首夜信息去重/中毒观察、registrationState、pendingNightAdvance、三个白天草稿、playerDisplayStep、pendingAutoAdvanceAfterDisplay、首夜候选/重试及启动状态、manualDemonBluffDraft。首夜预计算两个 LaunchedEffect 仍在早返回前，保持取消、清理和启动行为。手动伪装合法候选仍在早返回前计算，以保持原 remember key 更新。玩家展示优先于新恶魔确认，后者优先于 Dawn/Day。夜间页面自身效果仍位于路由之后。

验收：所有日间模式与黎明不经过 NightHostProjection/materializer；原回调及顺序、草稿 key 和效果 key 不变；日间登记仍由规则域判定，身份来自 Session。证据选用移动差异审计、既有登记/草稿/规则测试、完整 JVM 与 APK 编译、R2。此编排调整不新增为源码顺序制造的行为测试；无真机 Compose 生命周期或付费模型验收。

## 实施与验证

七个 Day 条件改为 Day 边界内的穷尽 when；日间输入只在 Day 准备。优先展示、确认与黎明路由已移到夜间投影前。FirstNight/Night 仍使用原登记与夜间时序规则。没有新增可写游戏状态、测试支架或通用上下文。

逐分支比较确认七个 Day body 仅去掉分支 return；夜间渲染及其效果完整不变。规范化行差异仅包含 Day 分流结构和当前事实登记适配。全部已有 remember 与 LaunchedEffect 的 key 保持不变，原跨阶段状态/效果仍位于早返回之前。

本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 通过，完整 1,342 / 快速 1,335，失败、错误、跳过均 0；测试任务实际执行。R2 shell 其余检查通过，macOS 不支持的 grep -P 项由等价 Python 正则核对；远端 Ubuntu 仍需原样 CI/R2 验证。`git diff --check` 通过。无真机/Compose 运行时验收。

Host 2,708 → 2,713 行，162,937 → 164,567 bytes。本步是阶段依赖隔离，不是文件缩减；大文件问题仍在。下一步审计夜间 materializer 与推荐生命周期的最小独立输入，优先删除不消费的准备路径，再按职责提取，不搬成另一个巨型 Context。
