# Stage 5 验收记录：NGJ 钟表匠、侍女数字语义与统一方桌（2026-10-23 已合并主线）

## 基线与范围

- Stage 4 [#349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) 经完整 Android CI #4569 / R2 #4130 成功后 squash 合并 main `97a4179d8461a96e1d38f0fc169b1f5386a4c730`。
- 第五批 [PR #350](https://github.com/Jazz0006/CampBoardGameHost/pull/350) 已于 2026-10-23 squash merge 到 `main`，合并 SHA **`b551d7f2efb074e45f2722c7bcd712d5f8bd56e8`**。合并后开发机 `main` 曾同步至该 SHA、工作树 Clean；后续纯文档更新会生成更晚的 main 提交。首批代码 `e64bbb3f` 完整 Android CI #4571/R2 #4131 SUCCESS；最终生产代码 **`7c2950aa06b668d13230349957a3d844ce10e8a9`** 完整 Android CI **#4572/R2 #4132 SUCCESS**；后续 docs-only `4d3ce214` CI #4573/R2 #4133 SUCCESS（Android tests SKIPPED）。本次没有真机/付费 API 验收。

## 生产实现与边界

1. `NumericInformationResolver.resolveLaterNight` 只读当前唯一 `ClocktowerGameSession/ClocktowerSessionState`，根据 NGJ 剧本、当前 actor、能力源实际生效状态求有限可能展示数字。不建立新 Rules 引擎或第二份游戏对象。
2. Clockmaker 仅首夜：恶魔至最近爪牙沿物理环形桌座位两方向的最短步数；可靠结果仅一个展示按钮，醉毒源给出有限候选，源角色未唤醒、跨剧本或缺少爪牙则不伪造答案。
3. Chambermaid 先确认两名不同、存活、非自身目标；从 `session.nightInteractions(ruleset)` 查询本夜按能力唤醒的真实角色步骤，排除说书人系统恶人介绍以及没有真实能力的 Drunk 虚假唤醒。该数字依赖当前夜间动态条件（如送葬者前夜是否处决、死亡顺序），不能靠静态剧本表或 AI 猜测。醉毒源可展示 0/1/2 有限备选。**需补边界验证：已在同夜醒过后来死亡者、角色变化与登记例外不能以本轮 CI 代替真机实测**。
4. Clockmaker 复用 `ClocktowerNumericInformationSquareTableDialog`，Chambermaid 保留共享物理座位双选方桌并复用 `ClocktowerResolvedNumericResultUi`；两者统一全屏展示/返回改选/Next 最终一次 Observation 语义。MANUAL、AI_ASSISTED、AI_AUTOMATIC 当前建议仅绑定实际 metric、source、subject seats 与当前 Resolver 值；确定值不需要 AI 自选。
5. **已删** `ClocktowerDirectNumericResultUi` 遗留的任意非负整数编辑 UI 及相关不用的 helper，保留 `clocktowerDirectNumericResult` 仅用于实际展示格式化。D2/完整 App 高价值回归已切换到新数字组件；此前 Stage 1–4 共用 Seat renderer/显示合同保持不变。

## 已完成的自动化与仍需实机验收的工作

- **已完成：** 最终生产代码 `7c2950aa` 完整 Android CI #4572、R2 #4132 SUCCESS；包括 NGJ 5–6 人数字语义、酒鬼假唤醒、恢复与真实 App 整局回归。后续文档提交如跳过 Android 测试，不得替代这一代码 SHA。
- 现有自动化覆盖 5–15 人 TB、5–6 人 NGJ、360×520 共享卡片/数字/Pair、完整 D2、死亡/鬼票、Session 序列化恢复与 NGJ 整局闭环。需在真实 Android 设备上抽测 15 人文本与状态叠加、来电中断和杀进程后实用恢复。当前 NGJ 官方角色表不包含 Recluse/Spy，不新增跨剧本不可达的 Clockmaker 登记算法。
- 全仓审计确认正式方桌仍通过共享 `ClocktowerSquareTableSeatSurface` 渲染；旧 `ClocktowerDirectNumericResultUi` 无调用，其余 `OutlinedTextField` 仅用于设置或玩家编辑。真机 Android/ADB 未执行，真实付费 LLM API 未授权、未执行；不能以 CI SUCCESS 代替现场测试。
- 独立旧 P1：死亡玩家被提名、健康占卜师固定布尔真值审计不包含在第五批，需单独规划。
