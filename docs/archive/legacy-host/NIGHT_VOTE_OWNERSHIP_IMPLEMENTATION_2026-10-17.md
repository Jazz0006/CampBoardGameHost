# C2：夜间确认与投票结果归属

基线 #318 `fb663d6a`。验收：草稿不改变确认事实；不同的已确认攻击/中毒/保护撤销依赖决定，相同确认与重试不撤销；待确认新恶魔选择不提前改身份；投票依确认当时门槛更新最高票/平票，失败不改结果；跨轮清除相应当前状态。

Architecture pre-flight:
- current owner: App 可写夜间目标、Host 可写最高票；Session 只记录对应动作。checkpoint reducer/transaction 重复承担确认和失效。
- proposed responsibility: 两个小型当前规则值（夜间有效目标、当日投票结果）归 Session，随正式动作原子更新；UI 只读，草稿/导航仍在 UI。删除旧确认适配层与重复写入口。
- authoritative state owner(s): ClocktowerSessionState，现有 ActionFactTimeline/observation；规则 planner 继续决定合法结果。
- narrow typed input/output seam: 已确认 ActionFact + 提交前状态 -> NightTargets/VoteStanding；显式空目标和夜晚收尾使用窄 Session 方法，不复制整个 checkpoint。
- keep in current owner / extract: 纯更新规则放 session 下具名小文件；Session 现有提交点调用，App/Host 仅映射座位和草稿输入。
- reason: 记录不包含所有失效/门槛上下文，不能按最后事件粗糙回算。当前状态与正式历史各司其职，不新增存档或事件平台。

扇出：App 夜间确认/黎明/新恶魔/重开，Host vote/执行候选，Session 通用动作/poison/Mayor/phase 提交，NightCheckpointReducer/HostTransaction 与专属测试。保留现有 legality planner、信息发布顺序、死亡/身份原子提交。旧规则回归迁到 Session 或新的纯规则边界，不只删除测试。

证据：现有角色/夜间/投票回归，新 Session 确认失效、投票门槛/平票/失败/跨轮测试，完整 JVM/FAST、APK、diff 和远端 full CI/R2。无真机/模型证据则不声明。

## 实施结果与测试退休

- Session 原子提交同时更新正式事实与当前夜间目标/投票结果；App 只读 Session 的实时值，Host 只收投票结果值。
- 攻击、保护、手动投毒的不同确认撤销市长转移与继任选择；相同选择、重复 action ID 不撤销。黎明 carry/expiry 和日末清毒显式保留夜间选择，避免把规则结算错当玩家重选。
- Mayor 仍由已有合法性提交边界裁定；当前另一个支持脚本 No Greater Joy 无 Mayor，未增设无消费者 fallback。
- 新恶魔待选只增加 input revision，不改变角色；现有 New Demon 正式边界仍负责身份变化。
- 删除 NightCheckpointReducer、NightCheckpointHostTransaction、ClocktowerVoteTransaction 及三个专用测试文件。草稿/Previous 的机械 copy 不再需要生产适配器；投影忽略草稿、Previous 保留确认的 typed matrix 仍保留。
- 旧上游失效和最高票/平票测试迁入 ClocktowerNightVoteOwnershipTest 的真实 Session 提交；补充 stale/重试不变、空确认、跨轮、按确认时人数判门槛及 poison expiry 保留继任选择。保留僧侣改保护目标后的死亡规则、市长失效到 Dawn、新恶魔到 Dawn 的组合回归。
- 一份当前 Session + 已有结构化记录；没有新增存档、历史副本或通用协调器。纯 NightTargets 只接收动作和前一中毒座位，不依赖整个 Session。

App 4,065 行 / 234,187 bytes；Host 3,022 行 / 179,448 bytes。后续 D 仍需职责审计。事件记录仍不包含全部空确认/失效/门槛上下文，中断恢复和完整复盘继续缺失，不声明可从现有记录完整续局。

开发记忆：NONE；当前约束已写入仓库权威入口。

本地验证：`:app:testFull` 实际执行 1,342 项、`:app:testFast` 实际执行 1,335 项，均无失败/错误/跳过；`:app:assembleDebug` 成功，`git diff --check` 通过。远端 full CI/R2 待本检查点推送后核对。没有真机、真实模型或独立人工 review 声明。
