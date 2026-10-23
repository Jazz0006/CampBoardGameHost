# 当前开发路线（2026-10-23，Host UI 职责重构 Batch 2 已合并）

> **P1 修复分支进度（2026-10-23，尚未合并）**：健康 Fortune Teller 现在从唯一 Session 对当前双目标求布尔结果；确定值在三模式直接展示，不再等待 AI 或提供相反值。醉毒与有效 Recluse 登记保留裁量；Spy 不登记为 Demon，恶魔/红鲱鱼的确定阳性优先。目标变更仍清空预览，Next 只提交最后一条私密 Observation。已补求值、AI stale 和三模式真实 Host UI 回归；验收结果见本修复 PR。P1 合并后下一结构批次为 Batch 3；本批不混做 Batch 3 或 AI 重建。


## 当前权威进度：Host UI 重构（优先于历史 Stage 1–5 说明）

**本次文档更新前主线：** `main` `1fe855e80efb006717275341b17b8a9d9e805fa7`。Host 方桌与信息角色 Stage 1–5 已全部合并；随后独立 Day P1 #352 已合并（`edcbf5a4`）。Host UI v3 职责重构依次完成：

| 批次 | 实际代码 / 合并 | 状态 |
| --- | --- | --- |
| Batch 1 — 唯一当前夜间交互草稿和确认分发 | [#355](https://github.com/Jazz0006/CampBoardGameHost/pull/355) squash `7486d05c` | 已合并 |
| Batch 2a — Session 座位事实，不经 PlayerCard 裁判 | [#356](https://github.com/Jazz0006/CampBoardGameHost/pull/356) squash `e7ac9481`；完整 Android CI #4593 / R2 #4148 | 已合并 |
| Batch 2 — Session 派生 HostNightStep + 只读 AI→UI Overlay | [#357](https://github.com/Jazz0006/CampBoardGameHost/pull/357) squash `1fe855e8`；reviewed head `5871c355` 完整 Android CI #4597 / R2 #4151 | 已合并 |
| 独立 P1 — 健康 Fortune Teller 确定布尔结果 | 需另立有界 PR | 未完成；不得混入 Batch 3 |
| Batch 3 / 4 / 5 — 夜间 UI 路由与唯一 Next / 可观察 Session UI / App 根职责 | 以 v3 文档逐批实施 | 未开始 |

**权威合同：** [Host UI FINAL v3](HOST_UI_RESPONSIBILITY_REFACTOR_PLAN_2026-10-23.md) 和 [独立 Review（§13 AI 例外，§14 最新进度）](HOST_UI_RESPONSIBILITY_REFACTOR_REVIEW_2026-10-23.md)，原在 docs-only PR #351。用户已批准：`NightAiOverlay` 对旧 `ClocktowerNightStepUi`、`PlayerCard`、语言与 AI matcher 的依赖可作为**临时 AI→UI 适配层**保留；`hostNightStep` 和 Session/规则代码不得读取这些展示类型。不要提前重写/删除 AI 生产推荐，也不要为旧 AI 增建兼容框架。独立 AI 推荐重建时，再迁移调用并同批删去旧适配器。

**下一步：** 先按已批准顺序独立处理健康 Fortune Teller P1 或明确安排；再开始 Batch 3（`ClocktowerJudgeScreen` 路由瘦身，`ClocktowerNightHostScreen` 拥有夜间 UI/预览与唯一 Next 意图，唯一 Session 确认分发仍归 `ClocktowerSessionHost`）。代码批次在精确 HEAD 上执行完整 `:app:testFull :app:assembleDebug` 和 R2。Stage 5 真机可读性、三模式、来电/后台/进程终止后安全继续仍待设备验收；付费 AI API 未获新授权时不得执行。

## 已完成方桌 Stage 1–5 的历史快照（保留原有验收资料）



**已合并主线：** [Stage 5 PR #350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) 通过 squash merge 合并 `main`，第五批合并提交为 **`b551d7f2efb074e45f2722c7bcd712d5f8bd56e8`**。2026-10-23 开发机 `clocktower` 已从干净的 feature 分支切回 `main` 并 fast-forward 至同一 SHA；最终文档更新另行提交推送。详细设计合同：[五批重建](HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md)。

| 批次 | 生产实现与代码证据 | 状态 |
| --- | --- | --- |
| Stage 1 — 共享座位视觉 | [PR #343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) squash `78d2b115`；CI #4543 / R2 #4110 SUCCESS。善蓝恶红、死亡/鬼票/醉毒和普通/特殊候选边框统一到同一 renderer | **已合并 main；真机视觉待验收** |
| Stage 2 — Session 只读求值 | [PR #344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) squash `d3a75c99`；CI #4544 / R2 #4111 SUCCESS。当前 Session 的 Numeric/Pair Resolver，不创建第二套状态 | **已合并 main；已由 Stage 3–5 UI 消费** |
| Stage 3 — 厨师 / 共情者 | [PR #348](https://github.com/Jazz0006/CampBoardGameHost/pull/348) squash `e241d03b`；代码 `44fb05c` CI #4562 / R2 #4124 SUCCESS。统一方桌数字信息、单值按钮/裁量单选和 Next 唯一 Observation | **已合并 main** |
| Stage 4 — 洗衣妇 / 图书管理员 / 调查员 | [PR #349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) squash `97a4179d`；最终代码 `18a64bc8` CI #4569 / R2 #4130 SUCCESS。先物理双座位再动态角色短名单、两种候选虚线、来源单列分区、三模式/预览门禁 | **已合并 main** |
| Stage 5 — 钟表匠 / 侍女 | [PR #350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) squash `b551d7f2`；生产代码 `7c2950aa` 完整 [Android CI #4572](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39768833600) / R2 #4132 SUCCESS。NGJ 当前夜唤醒和最近爪牙数字只读求值、共享数字 UI、删除旧任意数字文本输入；后续 docs-only CI #4573/R2 #4133 SUCCESS（Android tests skipped） | **已合并 main；自动化通过；真机/打断恢复待验收** |

**接下来只处理剩余风险，不重新实现已合并五批。** [下一轮交接](NEXT_DEVELOPMENT_HANDOFF.md) / [Stage 5 验收](HOST_INFORMATION_STAGE5_ACCEPTANCE_2026-10-22.md) 明确：在真实 Android 手机验证 15 人/360×520 可读性、状态叠加、三工作模式 D2 全屏预览→Next、来电/切后台/被杀进程后的安全继续。现有 TB 5–15、NGJ 5–6、小屏/全局行为自动化验证不等同真机或付费 API 验收。Oracle VM 当前不具备本地 Android 真机条件时，记录设备测试未执行，不杜撰结果。

**原 Stage 5 时点独立待办（历史描述）：** 白天存活提名者提名已死目标的问题现已由 #352 合并解决；正常占卜师（Fortune Teller）可靠固定布尔结果仍待独立修复。后续以本文件顶部进度为准，不回引旧 Rules 引擎、候选评分或额外 GameState。

**工程原则：** 唯一可写 `ClocktowerGameSession/ClocktowerSessionState` 与结构化已确认事件；信息展示可多次预览，只有 Next 形成最后一条 Observation；AI 仅建议当前有效裁量；TB/NGJ 两 builtin 与安全恢复、隐私、AtomicFile 保留。测试遵守 [当前测试策略](TESTING_STRATEGY.md)，付费 API 须另行批准。

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
