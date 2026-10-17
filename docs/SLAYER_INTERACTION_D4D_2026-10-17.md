# 第四阶段 D4d：Slayer 白天交互

基线 #325 `7e00d5aa`（包含 D4c R2 所有权守卫修复）。

Architecture pre-flight:
- current owner: Host 内 Slayer 声称者/目标/隐士登记草稿、座位点击、重选、取消、确认和登记控件。
- proposed responsibility: SlayerInteraction 拥有临时选择与 UI；正式射击仍通过原 onSlayerShot 回调交由 App/Session。
- authoritative state owner(s): Session 拥有已使用能力、死亡及登记事实；UI 仅拥有三项临时草稿。共享 RegistrationProjection 继续调用规则域，不复制合法性规则。
- narrow typed input/output seam: cards、共享座位投影、候选姓名、registration、round/poison/可操作性 -> 原三参数射击请求；导航独立。
- keep in current owner / extract: 草稿放在 Host remember(gameId)，交互独立，不传 Host/Session/nightCheckpoint。无全局 Context。
- reason: 剩余白天 Slayer 分支不应混入夜间 materializer；先明确交互生存期，再进行阶段早分流。

生命周期：原 claimant/target 按 gameId，登记 Boolean 未加 key；新三项均按 gameId。该规范化不会丢失可用选择：新 gameId 原 claimant/target 已为空，而进入 Slayer、重选 claimant/target、取消或成功射击均清登记选择。新局不应继承前局草稿。失败回调前不清空，失败后不导航。

扇出：Overview 进入时 clear；Slayer 选择/重选/取消/成功后 clear；App onSlayerShot 唯一正式消费者保持不变。返回/重选和能力资格使用现有桌面投影。仅健康隐士目标转发显式登记 Boolean，其余传 null，保持原语义。

验收：草稿重选清理、健康隐士标记转发、确认成功/失败清理顺序；既有规则/幽灵票/死亡回归、完整编译。无真机或付费模型验收，D4 早分流尚未完成。

## 实施与下一步前置审计

SlayerInteraction 104 行 / 4,217 bytes；Host 从 2,772 → 2,708 行，166,290 → 162,937 bytes。生产总行数增加 40 行，本次是职责拆分。正式射击、死亡和裁定写入未移动。三个草稿回归覆盖重选失效、null/false/true 登记值转发、失败不清空。

同步迁移 R2 Slayer 控件归属守卫：Host 调用交互、交互持有隐士控件。D4c 旧守卫修复已单独提交回 #325，没有把已正确拆出的控件重新放回 Host。

白天/黎明早分流仍有明确前置：共享登记 remember、三个白天草稿、玩家展示与新恶魔确认的优先级必须保留；首夜信息展示去重、异步候选预计算及重试状态、手动恶魔伪装草稿不能因提前 return 被无意销毁。下一检查点应先保留这些状态的生命周期，再将纯夜间准备放到日间路由之后，不把全部状态装入 Context。

验证：本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 通过，完整 1,342 / 快速 1,335，失败、错误、跳过均为 0。R2 shell 除 macOS 不支持的 grep -P 多行检查外全部通过，该项用等价 Python 正则验证；远端 Ubuntu 原样执行。`git diff --check` 通过。D4c 修复 head 的 R2 已通过。无真机/付费模型验收，D4d 远端检查以当前 PR 为准。
