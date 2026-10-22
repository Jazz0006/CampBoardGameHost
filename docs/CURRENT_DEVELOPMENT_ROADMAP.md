# 当前开发路线（2026-10-22，Host 方桌 / 信息角色五批重建）

## 当前有效开发进度与下一步（优先于下方旧审计）

**权威五批合同：** [PR #342 的 `HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md`](https://github.com/Jazz0006/CampBoardGameHost/blob/docs/host-table-information-rebuild-20261022/docs/HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md)。合同仍在独立 Draft #342，当前代码分支尚无此文件；不要把未合并文档错当作已在 `main`。

| 批次 | 已验证事实 | 当前状态 |
| --- | --- | --- |
| Stage 1a/1b — 统一卡片视觉 | [Draft #343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) HEAD `1ea2c9182cef31ebf0ede29cbfc8dd6545e0a80f`；Android CI #4543 / R2 #4110 SUCCESS；死亡/鬼票/醉毒、普通/特殊虚线与选中次序分层，旧 `ClocktowerSquareTableSeatState` 及其消费者已清理 | **代码已实现并自动验证，未合并；真机视觉未验收** |
| Stage 2 — 只读 Numeric / Pair Resolver | [Draft #344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) Stage 2 已验证**生产代码提交** `bb024f42442e464fe168e59af6ebd4a945840cb8`（后续 docs-only commit 会推进 PR HEAD）；Android CI #4544 / R2 #4111 SUCCESS。新增 `clocktower/session/HostInformationResolvers.kt` 与 `HostInformationResolversTest.kt`，只读 Session 支持 Chef、Empath、Washerwoman、Librarian、Investigator；独立判断信息源醉毒与 Spy/Recluse 特殊登记 | **求值模块已实现并自动验证；尚未接入 UI 或 AI 生产消费者，未合并** |
| Stage 3 — 厨师 / 共情者数字 UI | 需把当前 Session Resolver 接进 `ClocktowerNightStepUi` / HostScreen 与当前信息步进 | **下一步：实际实现，不是重新规划** |
| Stage 4 — 洗衣妇 / 图书管理员 / 调查员 Pair UI | 先选两个座位、当前查询角色短名单与来源分区、AI 三模式 | 待实施 |
| Stage 5 — 钟表匠 / 侍女及全局验收 | 后续补精确能力语义与真实设备/交互验收 | 待实施 |

**立即实施 Stage 3：** 从 `feature/host-information-resolvers-20261022` 的已验证 HEAD `bb024f42` 开始（或检查实时 HEAD 后建立其子分支），切勿直接从未合并 Stage 1/2 的 `main` 创建不含 Resolver 的工作树。先逐个审计 `ClocktowerDirectNumericResultUi.kt`、`ClocktowerNightStepUi.kt`、`clocktower/ui/ClocktowerHostScreen.kt`、`ClocktowerSessionHost.kt`、`clocktowerDeterminedNumericResult` 与 D2 唯一 Observation。让正常确定结果只出现一个“展示给玩家：N”，多种真裁量提供单选并高亮当前可靠的 AI 建议，严守当前查询和预览失效/重预览/Next。正式完成迁移同批删除任意非负数文本输入与废弃分支/测试。只增高价值行为回归，提交推送运行准确 SHA 的完整 Android CI/R2，继续 Draft，不擅自合并/改写历史。

**分支关系：** #344 堆叠在 #343（#343 以 `main` 为 base），#342 文档合同独立、#341 酒鬼方桌独立。两者共享的 Renderer 等文件后续集成必须核对冲突；当前绝不自动合并/覆盖。独立未修复 P1：活人可以提名死人（当前 UI 错误限制）、占卜师等固定布尔信息求值另行审计；这些不属于 Stage 3 的随意扩 scope。CI ≠ 真机；任何付费 API 测试单独授权。

**证据与完整交接：** [第二批验收与第三批入口](HOST_INFORMATION_STAGE2_ACCEPTANCE_2026-10-22.md)、[下一轮交接](NEXT_DEVELOPMENT_HANDOFF.md)、[测试策略](TESTING_STRATEGY.md)。

## 以下为 2026-10-21 清理 / P1 独立审计历史基线（与上方新状态冲突时以上方为准）

> **最终高价值测试验收（2026-10-21）**：#338 清理后，#339 按代码复审补回两个独有的高价值保护（当前行动未确认不生成后续 AI 请求；360×520 竖屏支持 5–15 人）。两 PR 已合并；最新代码合并点 `4b9852cf5828c445b532550115aafbf2ddc8c4bf`，#339 [CI #4528](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39604089426) / R2 #4098 SUCCESS；静态最终 124 测试类/566 个 `@Test`，相较初始 144/639 净删 20 类/73 项。下文 #338 后的 122/564 是中间检查点，不是最终数量。

> **测试清理新进度（2026-10-21）**：仅保留高价值行为测试的第一轮清理 [#338](https://github.com/Jazz0006/CampBoardGameHost/pull/338) 已合并入 main `2dcf8a9745f487c3b4a0e30188fc67e3d58c5dfd`；共删除 22 个低中价值测试类、1 无用 helper 和 75 项 `@Test`，静态保留 122 类、564 项。精确清理代码 `b050033b` [CI #4524](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39602538249) / [CI #4525](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39602587292) / R2 #4097 均 SUCCESS。只清理测试，未修复下述两个生产 Bug。其余尚未逐条证实属中低价值的测试不作盲删。

> 唯一权威状态索引。先看本文件和 [当前交接](NEXT_DEVELOPMENT_HANDOFF.md)；旧阶段性 NEXT、Draft 禁令、局部失败记录仅是历史。最近独立只读复核基线为 `main` `6230b5959cf4b64e1c41f08bb48aa66460e13667`，下一轮须再次核对实时 HEAD。

## 已验收的两个阶段

1. **最小完整游戏：阶段一关闭。** [#334](https://github.com/Jazz0006/CampBoardGameHost/pull/334) 与 [#335](https://github.com/Jazz0006/CampBoardGameHost/pull/335) 已合并。TB / NGJ 代表性实际 App 入口→发牌→夜晚→白天提名/投票→后夜→终局→文件恢复→再开一局通过自动化回归。详见 [最小整局验收](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)；并非全部阵容穷举/真机杀进程验收。
2. **全仓遍历和大规模删除：阶段二关闭。** [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337) 已合并到 `main`。完成 430/430 非 docs 路径审计，0 待审，215 个生产 Kotlin 文件均 ≤50,000B；相对合并前基线净删约 7,635 行 / 376,286B，整删 30 文件。精确生产代码 `e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba` 的 [完整 CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) 与 [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) 成功；639 tests / 144 suites / 38 UI + Debug APK。合并后的 main 相比此代码检查点只有文档更改。详见 [收尾验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md) 和 [路径台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

## 历史视觉路线说明（被上方五批新合同取代）

长期视觉合同见 [Host 方桌视觉合同](HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md)：方桌保持稳定；Day 白底浅色主题、FirstNight/Night 黑底深色主题；真实阵营善蓝恶红；点击/唤醒/死亡/中毒只改变边框和标记，文字必须可读。只使用唯一 Session 已确认状态的只读展示投影，不保留平行配色状态或主题实现。这是早期按“主题/阵营卡”划分批次的记录，**不再表示五批方案中的 Stage 1/2**。共享视觉 Stage 1 和只读信息求值 Stage 2 的真实代码 SHA、CI/R2、未合并状态已在文首列出，数字 UI 为下一步 Stage 3；真机验收仍未执行。

## 2026-10-21 独立审计遗留问题（以文首新五批状态为准）

- **P1：确定性信息的 UI 消费缺口（Stage 2 只读 Resolver 已实现，UI 尚未接入）。** 正常工作的 Chef / Empath 等数值能力在缺少预生成值时可经 `ClocktowerDirectNumericResultUi` 手工给出任意非负数；Fortune Teller 布尔信息的唯一结果也要核验。Host 应先依据唯一 Session、查询对象、功能状态及登记裁量求唯一答案；只把真实裁量交 AI/人工。醉酒/中毒信息仍可裁量，实际误播应单独明确记录而非悄悄覆盖。
- **P1：白天被提名目标范围（已复核，未修复）。** `ClocktowerDayNominationGesture.kt` 和 `ClocktowerDayHostScreen.kt` 把提名者、被提名者均限制为存活者。实际规则为**仅存活玩家可以提名，已死玩家仍可以被提名**；Session 白天确认允许此目标。修 UI 输入范围，保持 Session 事件/投票语义，增加真实交互回归。规则参见 [Rulebook](https://www.rulespal.com/blood-on-the-clocktower/rulebook)。
- **P2：功能与设备验收。** 复核 Slayer/Day 实际选择、各角色 D2 三模式展示/重选/唯一 Next、AI stale/错误重试、真实 Android 强退恢复和付费 API 行为（付费请求必须单独授权）。UI 排版细节后置，不无限重复大范围清理。

## 不可变架构合同

唯一 `ClocktowerGameSession` + `ClocktowerSessionState` 是角色/生死/阶段轮次/已确认顺序事件/目标/终局的**唯一可写权威**。开局前 `ClocktowerOpeningDraft`、UI 草稿、`GameSnapshot`、只读卡片、`ClocktowerAiConversation` 与传输/存档适配不构成另一局；不可凭名称删除有生产消费者的只读派生。Host 实施确定性游戏规则，AI 只做真正的说书人裁量；保留 TB 5–15、NGJ 5–6、现有 Android AI 三模式和 V3、两项跨局防重复、隐私、原子保存/幂等/stale 检查。D2 展示只是可反复预览，仅当前角色 Next 写**唯一最终 Observation**；无跨角色 Previous、Dawn/EndConfirm 独立页面或第二恢复器。

**分支提示：** #336 仍为独立 open Draft，内容与已合并的 #337 重叠；需按最新 main 审查差异后再决定关闭/保留，不得直接合并或覆盖。这段是旧 main-only 基线；当前 Stage 3 必须从尚未合并的 Stage 2 已验证分支派生，避免丢失 Resolver；不得擅自强推、重写历史、合并 Draft 或触发付费 API。
