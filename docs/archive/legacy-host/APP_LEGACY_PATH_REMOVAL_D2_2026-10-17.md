# 第四阶段 D2：App 无用请求和旧模式分支删除

基线 C2 `9769f8a4` + D1 审计 `ea959cd8`。架构 pre-flight 见 `APP_HOST_STAGE_D_AUDIT_2026-10-17.md`；本批保持现役行为，不增加状态 owner/adapter。

验收：纯手动开局、联合建议后的手动选择、AI 开局手动接管三条路径继续使用 Host 合法候选与 stale 检查；TB/NGJ 正式信息与动作只提交到现有 Global Session，观察→锚点→视图/缓存的顺序不变。

## 实施与调用审计

- 删除 App 私有 PendingTroubleBrewingDrunkSelection.providerRequest 及三个构造路径的请求/上下文。三个入口均仍创建 DrunkAssignmentDecisionBoundary；选择确认仍检查候选属于当前列表、decision 合法域、pending 身份和当前页面。
- 全生产搜索确认 fromDrunkAssignment 不再有消费者，删除该工厂方法。保留 pair/scalar/Mayor 工厂和联合开局实现。退休其专属 1 个请求映射测试与只为该测试存在的 fixture；其测试目标是已删除的旧请求，而非酒鬼选择的合法性。保留 DrunkSelectionRouter、DrunkCandidateProjection、OpeningRulesDomain/SemanticGate 和其他工厂回归。
- 生产 Session 唯一创建点为 App resetDealState，TB 和 NGJ 都显式 GLOBAL_V1。其他 App 写入只有清空 Session，当前无 restore/import 路径。删除 App 中 LEGACY_LOCAL 观察绑定、private publication fallback、旧模式条件与 Slayer/Virgin 的旧修订号补写。
- 受影响消费者为 ClocktowerJudge 的正式动作/公开与私有观察、注册裁定、Klutz、Slayer、提名/投票/圣女/无处决。它们位于已创建 Session 的 Clocktower 活跃流程；开局未建 Session 的手动酒鬼页面仅提交 setup，不调用这些入口。无 Session 的注册 preflight 仍返回 false；非 TB 的注册裁定豁免保留，因为当前该裁定仅支持 TB。
- 没有改变 Session/公共认识模型中的 Legacy 数据类型和兼容消费者，没有更改规则、显示授权、AI 锚点条件或实际请求协议。

## 规模和下一步

生产净删 94 行，测试净删 80 行。App 从 4,065 行 / 234,187 bytes 降至 4,006 行 / 229,866 bytes。未新增生产抽象，Host 仍 3,022 行 / 179,448 bytes。

下一步 D3：先锁定 Dawn/Dusk 的提交责任与完整调用图，收缩重复 materialization 契约和 App 分支；保留正式动作的幂等、失败原子性和已公布信息顺序，不把整个结算闭包搬入 Session。第四阶段大文件治理尚未完成；事件覆盖、持久化与中断继续游戏缺口不变。

本次不新增测试形状约束。完整 JVM、FAST、APK 与远端证据在当前 PR 核对；没有真机或真实模型验证。开发记忆：NONE。

本地最终验证：`:app:testFull` 1,341 项、`:app:testFast` 1,334 项实际执行，失败/错误/跳过均 0；`:app:assembleDebug` 成功；`git diff --check` 通过。
