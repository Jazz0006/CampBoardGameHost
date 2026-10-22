# 第四阶段 D3a：中毒结算收敛

基线 `b3c0f248`。D3a 先完成普通黎明/新恶魔确认/日末的中毒结算；死亡、身份和阶段 materialization 留待 D3b，不宣称整个 D3 完成。

Architecture pre-flight:
- current owner: App 在三个结算入口读确认目标、复制 timeline ID 集、构造 materialization、分支写事实；Session 已原子提交中毒。
- proposed responsibility: 已有 PoisonBoundaries 承担窄的黎明 carry/日末 expiry 提交，App 只发布视图/清草稿；删除重复 recovery 查询、poison materialization DTO/分支。
- authoritative state owner(s): Session 当前 GameState 与 ActionFactTimeline；NightDawnResolutionPlanner 保留规则决定。
- narrow typed input/output seam: DawnPoisonCarryIntent + expected revision -> changed；日末从 Session 当前 phase/round 取确认目标 -> changed。
- keep in current owner / extract: 扩展已有中毒边界文件；不扩张 GameSession 主类，不加入全局 Context。
- reason: 原子提交后无需 UI 推断“状态先写/历史先写”的修复计划；必须保留真实重试幂等、死目标清标记后的语义 expiry 和信息顺序。

扇出：App materializeClocktowerPoisonExpiryAtDusk、onConfirmNewDemon、onConfirmNight；NightDawnResolutionPlanner 两个 poison 入口；Dawn materialization poison 字段/三处 App state 构造；旧 Dusk authority/planner、NightDawnPoisonRecoveryAuthority 和对应测试。NightTransactionRestoreComposition 的同夜规则投影保留。Dawn death/role/phase materialization 暂保留。

验收：首夜与普通夜确认都可日末清毒；死亡或身份变化不丢失已确认中毒语义；同目标 carry 不制造事件；不同或 stale intent 拒绝且不变；重复结算不重复事实/修订号；结算不撤销已确认继任选择；日末 Poison(null) 在下一夜 PhaseAdvance 之前。保留正常规则测试，将旧部分写入恢复模拟退休，以真实 Session 提交回归替代。

## 实施与测试退休

已删除 DuskPoisonExpiryMaterializationPlanner、DuskPoisonExpiryRecoveryAuthority、NightDawnPoisonRecoveryAuthority；删除 Dawn poison materialization DTO/字段。Session 在原有原子动作边界上处理 carry/expiry，App 只调用并发布视图。结算事实 ID 使用当前输入游标；已结算结果直接无操作，后续新确认目标仍可正常结算。不扩张 Session 主类。

| 退休内容 | 保留/替代证据 |
| --- | --- |
| Dusk recovery 查询和中间计划测试 | ClocktowerPoisonSettlementTest 实际 Session 的空目标、死亡目标、重复确认与非法请求边界；首夜见下列集成回归 |
| NightDawnRestoreRetryConvergenceAcceptanceTest 的两侧部分写入模拟 | 旧半写入修复不再是产品合同；真实 Session 提交原子性回归保留，不一致直接拒绝 |
| Dawn materialization poison 断言、ordinary poison fallback 模拟 | 规则 planner 的投毒者死亡清毒断言保留；实际 Session carry/expiry 六项回归替代 |
| 原执行/日末模拟 | PoisonExecutionDuskTimelineRegressionTest 直接运行 Session：投毒者处决、真实公开观察、清毒、下一夜全局顺序 |

生产源码净减少 241 行；App 从 4,006 行 / 229,866 bytes 降至 3,887 行 / 221,785 bytes。Host 不变。已复查三条生产调用路径及删除符号引用；保留继任身份提交后的中毒结算顺序，规则 planner 仍拥有规则决定。

D3b 尚未实施：death/role/phase materialization 及对应旧分支继续保留。本轮不增加存档、持久化、事件补全或真机/付费模型调用。

## 验证

本地 `:app:testFull :app:testFast :app:assembleDebug --offline` 通过：完整 1,332、快速 1,325，失败/错误/跳过均为 0。测试任务实际执行；APK 在本轮首次完整验证已重新编译打包，最终检查为最新。`git diff --check` 通过。远端 CI/R2 和独立审查仍以 PR 当前 head 状态为准，未合并。
