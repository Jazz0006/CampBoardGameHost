# 当前开发路线（2026-10-21，main 已合并清理）

> 唯一权威状态索引。先看本文件和 [当前交接](NEXT_DEVELOPMENT_HANDOFF.md)；旧阶段性 NEXT、Draft 禁令、局部失败记录仅是历史。最近独立只读复核基线为 `main` `6230b5959cf4b64e1c41f08bb48aa66460e13667`，下一轮须再次核对实时 HEAD。

## 已验收的两个阶段

1. **最小完整游戏：阶段一关闭。** [#334](https://github.com/Jazz0006/CampBoardGameHost/pull/334) 与 [#335](https://github.com/Jazz0006/CampBoardGameHost/pull/335) 已合并。TB / NGJ 代表性实际 App 入口→发牌→夜晚→白天提名/投票→后夜→终局→文件恢复→再开一局通过自动化回归。详见 [最小整局验收](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)；并非全部阵容穷举/真机杀进程验收。
2. **全仓遍历和大规模删除：阶段二关闭。** [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337) 已合并到 `main`。完成 430/430 非 docs 路径审计，0 待审，215 个生产 Kotlin 文件均 ≤50,000B；相对合并前基线净删约 7,635 行 / 376,286B，整删 30 文件。精确生产代码 `e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba` 的 [完整 CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) 与 [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) 成功；639 tests / 144 suites / 38 UI + Debug APK。合并后的 main 相比此代码检查点只有文档更改。详见 [收尾验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md) 和 [路径台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 当前第三阶段：规则正确性优先，然后细节

- **P1：确定性信息的 Host 责任缺口（高置信、未修复）。** 正常工作的 Chef / Empath 等数值能力在缺少预生成值时可经 `ClocktowerDirectNumericResultUi` 手工给出任意非负数；Fortune Teller 布尔信息的唯一结果也要核验。Host 应先依据唯一 Session、查询对象、功能状态及登记裁量求唯一答案；只把真实裁量交 AI/人工。醉酒/中毒信息仍可裁量，实际误播应单独明确记录而非悄悄覆盖。
- **P1：白天被提名目标范围（已复核，未修复）。** `ClocktowerDayNominationGesture.kt` 和 `ClocktowerDayHostScreen.kt` 把提名者、被提名者均限制为存活者。实际规则为**仅存活玩家可以提名，已死玩家仍可以被提名**；Session 白天确认允许此目标。修 UI 输入范围，保持 Session 事件/投票语义，增加真实交互回归。规则参见 [Rulebook](https://www.rulespal.com/blood-on-the-clocktower/rulebook)。
- **P2：功能与设备验收。** 复核 Slayer/Day 实际选择、各角色 D2 三模式展示/重选/唯一 Next、AI stale/错误重试、真实 Android 强退恢复和付费 API 行为（付费请求必须单独授权）。UI 排版细节后置，不无限重复大范围清理。

## 不可变架构合同

唯一 `ClocktowerGameSession` + `ClocktowerSessionState` 是角色/生死/阶段轮次/已确认顺序事件/目标/终局的**唯一可写权威**。开局前 `ClocktowerOpeningDraft`、UI 草稿、`GameSnapshot`、只读卡片、`ClocktowerAiConversation` 与传输/存档适配不构成另一局；不可凭名称删除有生产消费者的只读派生。Host 实施确定性游戏规则，AI 只做真正的说书人裁量；保留 TB 5–15、NGJ 5–6、现有 Android AI 三模式和 V3、两项跨局防重复、隐私、原子保存/幂等/stale 检查。D2 展示只是可反复预览，仅当前角色 Next 写**唯一最终 Observation**；无跨角色 Previous、Dawn/EndConfirm 独立页面或第二恢复器。

**分支提示：** #336 仍为独立 open Draft，内容与已合并的 #337 重叠；需按最新 main 审查差异后再决定关闭/保留，不得直接合并或覆盖。后续代码工作从最新 main 派生新分支；不得擅自强推、重写历史或触发付费 API。
