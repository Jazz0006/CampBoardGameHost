# Host 信息角色重建 — 第二批代码验收与第三批交接（2026-10-22）

> **验收记录与后续主线集成：** Stage 1 #343 源代码 `1ea2c918` / main squash `78d2b115`，Android CI #4543 / R2 #4110 SUCCESS；Stage 2 #344 源生产代码 `bb024f42` / main squash `d3a75c99`，Android CI #4544 / R2 #4111 SUCCESS。此前记录的 Draft/分支状态属于合并前的历史，**两批现已合并 main**。权威五批合同见 [当前本地设计文件](HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md)；第三批 Numeric UI 尚未实施。另酒鬼选择已由 #346 兼容实现合并 main `b66f8fe3`（Android CI #4551 / R2 #4115 SUCCESS），原 #341 已关闭；#342 文档由 #345 替代，#336 旧冲突 Draft 关闭且未移植旧模块。真机/付费 API 未验收。

## 1. 合并前的真实分支和证据（历史留存）

| 项目 | 已核验状态 |
| --- | --- |
| 仓库/开发机 | `Jazz0006/CampBoardGameHost`；Oracle VM Mini MCP alias `clocktower` |
| Stage 1 卡片视觉 | [Draft #343](https://github.com/Jazz0006/CampBoardGameHost/pull/343)，HEAD `1ea2c9182cef31ebf0ede29cbfc8dd6545e0a80f`；Android CI #4543、R2 #4110 SUCCESS |
| Stage 2 信息 Resolver | [Draft #344](https://github.com/Jazz0006/CampBoardGameHost/pull/344)，分支 `feature/host-information-resolvers-20261022`，生产提交 `bb024f42442e464fe168e59af6ebd4a945840cb8`；Android CI #4544、R2 #4111 SUCCESS |
| 依赖 | #344 **base = #343 的代码分支**，#343 base = `main`；两者均未合并；#342 为独立 docs Draft，#341 为独立酒鬼方桌 Draft |
| 已审工作树 | 生产代码基线 `bb024f42`，此前 Clean、local/remote equal；本轮 docs-only 交接提交使最新 HEAD 前进，后续请实时核查 Git 状态 |
| 真机 / 实际进程强退 / 付费模型 | 本批未执行，不声明通过 |

此前主线的“最小完整游戏／全仓代码清理”两大阶段已经合并；**不要把它们与 2026-10-22 的新五批 UI/Resolver 重建混为一谈**。

## 2. 第二批已经落地的代码

仅新增 `app/src/main/java/com/codex/campboardgamehost/clocktower/session/HostInformationResolvers.kt` 和 `app/src/test/java/com/codex/campboardgamehost/clocktower/session/HostInformationResolversTest.kt`。本批**没有修改** Session 核心、Night/Day 事件、AI API 或任何数字/Pair 生产 UI，也未增任何第二游戏状态。

- `NumericInformationResolver.resolve(ClocktowerSessionState, NumericMetric, sourceSeat)`：首批只支持 Chef `ADJACENT_EVIL_PAIRS` 与 Empath `LIVING_EVIL_NEIGHBOURS`。返回可展示数字、从唯一 Session 提取的查询座位、源能力是否有效、局部有效的特殊登记座位、是否属产品用实用短名单。Chef 计算座位环的最后至第一条边，且采用局部动态计数，不创建登记假设世界；Empath 查找最近**存活**邻居。
- `PairInformationResolver.resolve(session, ruleset, role, sourceSeat, firstSeat, secondSeat)`：单次只处理当前两个座位（或尚未选完的空查询），支持 Washerwoman/Librarian/Investigator 的实际可支持角色、特殊 Spy/Recluse 登记、信息源 Drunk/poison。角色来源区分 `IN_PLAY`、`DEMON_BLUFF`、`SCRIPT_FALLBACK`；恶魔伪装严格依赖 Session 已确认 `demonBluffRoleIds`；Librarian 的没有外来者结果与中毒裁量单独处理；不生成 Investigator “0 爪牙”。
- `AbilityFunctioningSemantics` 只读重用：不能把信息源中毒视为 Spy/Recluse 登记生效，也不能把 Spy/Recluse 自身中毒误判为允许特殊登记；角色真身与酒鬼展示身份分开读取。
- `NumericInformationResolver.acceptsCurrentAdvice` 和 `PairInformationResolver.acceptsCurrentRoleAdvice` 只是**当前查询 AI 建议检查辅助接口**，不是对实际人工确认事实的规则合法性拒绝门禁。这些接口尚未在生产 UI 使用。
- `Clockmaker` 和 `Chambermaid` 的语义明确推迟 Stage 5；`Fortune Teller` 的固定布尔结果单独排期，不混入本批。

## 3. 已有测试与未验收范围

高价值测试使用真实 `ClocktowerGameSession` 构造，核对 Chef 首尾相邻 / 特殊登记与失效、Empath 跳过死亡座位 / 双特殊登记、源中毒与目标中毒独立、酒鬼 `shownRole` 与 `actualRole`、Pair 的选定两个座位、在场角色和 Session 恶魔伪装来源、Librarian 零外来者、Investigator Recluse 与不伪造零结果、TB/NGJ 剧本隔离、过期/错误 Numeric AI 建议、求值无 Session 写入。完整 Android CI #4544 与 R2 #4111 均为 SUCCESS。

**不能据此宣称生产 Host 已自动向厨师/共情者给出真值：** `ClocktowerDirectNumericResultUi.kt` 仍允许输入非负整数，`ClocktowerNightStepUi.kt` 和 `clocktower/ui/ClocktowerHostScreen.kt` 仍使用现有 step/recommendation/人工路径；Pair 方桌也尚未换成先选座位的交互。**这正是接下来的 UI 批次。**

## 4. 下一个对话：Stage 3（唯一待执行优先工作）

1. 从本分支最新实际 HEAD 开始，先核对 `git_state`、remote、#343/#344/#342/#341 的状态，保持其它 Draft 不变。若建立新 Stage 3 开发分支，应优先从本轮交接完成后的最新开发分支 HEAD 派生（包含已验证的 Stage 2 `bb024f42` 代码），而非直接从尚未集成该代码的 `main`。
2. 读 `AGENTS.md`、`docs/CURRENT_DEVELOPMENT_ROADMAP.md`、`docs/NEXT_DEVELOPMENT_HANDOFF.md`、`docs/TESTING_STRATEGY.md` 和 PR #342 的完整五批规划，按实际调用图审计 `ClocktowerDirectNumericResultUi.kt`、`ClocktowerNightStepUi.kt`、`ClocktowerSessionHost.kt`、`clocktower/ui/ClocktowerHostScreen.kt`、`clocktowerDeterminedNumericResult`、现有 D2 Next/预览提交与三 AI 模式。
3. 把 Chef、Empath 的数字结果从**当前** Session `NumericInformationResolver` 接入真实方桌：正常单值只有一个“展示给玩家：N”按钮；有真正登记/醉毒裁量时数字单选，高亮当前有效 AI 推荐；按钮直接打开既有全屏展示，无重复预览文字。不能把旧 `step.displayProposition` 或 AI 建议当作真实 Host 真值来源。
4. 全屏返回仍在同一角色步骤，保留已经展示的选择；修改选择令旧预览失效，必须再次展示才可 Next；Next 只提交最后一个实际展示的私密 Observation 一次。警惕 AI stale 的游戏修订、查询座位、metric、主体和角色；自动模式无当前合法建议不得臆造选项。人工真实误播记录仍按 Session 确认事实处理，不偷偷覆盖为机器认为的正确值。
5. **删旧换新须同时完成：** 退休任意非负数 TextField 和已被替代的判断/分支/测试，不保留长期双轨；保护 NGJ 钟表匠、侍女原有临时路径直到 Stage 5 正式迁移，不得本批误伤。保留唯一 Session、TB/NGJ、MANUAL/AI_ASSISTED/AI_AUTOMATIC、AI V3/提示词/战略记忆、跨局防重复、D2、AtomicFile、安全/隐私。
6. 添加少量真实行为回归、覆盖正确唯一数值/有裁量的单选/中毒解除/邻居死亡变化/迟到 AI/预览→改选→重预览→唯一 Next/360×520；完整提交/推送/Android CI/R2，遇到编译或测试失败实际修复。无授权不得 merge、rebase、force-push 或请求付费 API。

## 5. 后续批次与独立问题

- Stage 4：Washerwoman、Librarian、Investigator 方桌先手选两座位，再由 Resolver 产生动态角色短名单，Washerwoman 的在场/已确认恶魔伪装纵向两分区，单列下拉，无额外第二栏；只有本步 Next 提交最终 Observation。
- Stage 5：Clockmaker/Chambermaid 真正能力语义、共享 UI 扩展、实体设备与整局复核。
- 其它独立 P1：Day 存活提名人允许提名死人（当前 UI 仍过窄）；健康 Fortune Teller 固定布尔真值独立审查。
- #341 酒鬼方桌与 #343 共享 `ClocktowerHostTableUi.kt`、`ClocktowerSquareTableUi.kt` 两个路径，后续合并需要人工审查冲突。#342 合同文件与当前代码分支的路线图/交接变动同样有集成重叠；**不能自动合并**。
