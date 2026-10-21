# 合并后独立代码审计（2026-10-21，基线 main 6230b595）

> 这是在第一阶段最小完整游戏和第二阶段全仓清理合并后，对**真实 main 生产数据流的额外只读复核**，并非新的完整构建、全量逐行重审、真机或付费 AI 实验。后续判断以实时 Git 状态、[路线图](CURRENT_DEVELOPMENT_ROADMAP.md)及可复核代码为准。

## 已核对的状态与证据

- 合并状态：#334、#335、#337 已 merged；#336 独立 open Draft（与已合并清理路径重叠，需单独差异审查）。
- `main` 观察时 HEAD `6230b5959cf4b64e1c41f08bb48aa66460e13667`，对应已验收生产代码 `e5ffc4e5e3ccdac5a5d4d3d7845f9754b998dbba` 之后仅文档变化。后者 [Android CI #4520](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977318) 和 [R2 #4095](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39591977316) 成功，Cloud full 639 / 144 suites / 38 UI、APK 成功。
- 从 main 的完整 Git 树核对 215 个 `app/src/main` Kotlin 文件，**没有 >50,000B** 的生产 Kotlin；清理台账已覆盖 430/430 非 docs 路径。此为已有审计与检查点的核对，不是本轮逐行验证 215 文件或重新执行 639 项测试。

## 游戏对象与数据权威审计

| 职责 | 现行生产所有者 | 独立复核判断 |
| --- | --- | --- |
| 座位/身份/生死/毒/轮次/阶段/历史/目标/终局 | `ClocktowerGameSession` 内的 `ClocktowerSessionState` | **唯一事实写入权**；阶段从 `PhaseAdvance` 推导，状态迁移与有序 ActionFact/Observation 由 Session 拥有 |
| 夜序、触发和跳过 | `clocktower/session/ClocktowerNightFlow.kt` 读 Session + 内置剧本 | 没有发现独立持久 UI 夜间游标 |
| 白天结算/投票/提名/胜负 | `clocktower/session/ClocktowerDayFlow.kt` | Session 决定并确认；局部 UI 模式/草稿并非提交事实 |
| D2 信息展示与方桌 | `clocktower/ui/ClocktowerHostScreen.kt` 的临时状态 + 当前 Session | 预览可修改；仅当前角色 Next 向 Session 提交唯一最终观察；不持久化预览 |
| 保存/恢复 | `ClocktowerActiveGameStore`、`ClocktowerSessionJson`、App | 同一个 Session 的 JSON 在私有 AtomicFile 保存并重载；没有另一个可写游戏存档 |
| AI/开局辅助 | `ClocktowerAiConversation` / `ClocktowerOpeningDraft` / `GameSnapshot` | 请求快照、推荐、回执、开局前未确认草稿；没有发现独立推进游戏的写权。只读副本**不等于**第二个游戏对象 |

具体参考：`clocktower/session/ClocktowerSessionState.kt:38-89`、`clocktower/session/ClocktowerGameSession.kt:31-83,724-755`、`clocktower/session/ClocktowerNightFlow.kt:9-99`、`clocktower/session/ClocktowerDayFlow.kt:6-48,101-142`、`CampBoardGameHostApp.kt:144-205,552-600`、`persistence/ClocktowerActiveGameStore.kt:9-39`（以上源码均位于 `app/src/main/java/com/codex/campboardgamehost/`）。

## 优先修复项（仍未修改代码）

1. **P1：Host 确定性信息缺口；置信度高。** `ClocktowerDirectNumericResultUi.kt:26-56` 构造任意非负手工数值；`clocktower/ui/ClocktowerHostScreen.kt:450-474` 只在当前已有匹配的 Host typed numeric 时绕开 AI。对正常工作的 Chef / Empath 等纯确定值，若未预先生成结果，可手工记录与 GameState 不符的数值。Fortune Teller 查询在确定登记/红鲱鱼/能力状态后也应由 Host 确认唯一布尔真值。**建议**：基于 Session 的窄角色计算与现实登记裁量，在 Host/显示确认边界区分唯一值与自由裁量；Drunk/poisoned 可给假信息，明确的人工误播另行记录实际结果，不能用“规则正确”重写已经给玩家看的线索。不要复活旧 Host 候选域/评分引擎。
2. **P1：死者不能成为 UI 提名目标；代码复核和规则文本一致。** `ClocktowerDayNominationGesture.kt:19-31` 的 `sourceSeatIds` / `targetSeatIds` 都只取活人，`clocktower/ui/ClocktowerDayHostScreen.kt:143-153` 双向检查仍限制 `seat.isAlive`；但 `clocktower/session/ClocktowerDayFlow.kt:144-159` 仅要求 **发起提名者**存活，允许被提名者死亡。正式 [Rulebook](https://www.rulespal.com/blood-on-the-clocktower/rulebook) 明确：only alive players may nominate; dead players can be nominated。**建议**：UI 目标集合包含全部座位，发起人仅活人，继续限制每天次数、候选当前步骤和已确认状态，增加真实手势→确认→投票与重载回归。
3. **P2：范围未验收而非已确认缺陷。** 真机 Android 被系统杀进程后恢复、所有角色/模式的 D2 视觉操作和异常流程、当前外部模型的真实付费响应/延迟尚未执行，不能把 Robolectric/CI 通过说成实机/线上通过。

## 结论与下一轮约束

**最小 Host 的单一状态权威和代表性完整流程有充分代码与自动化证据；但全部规则确定性信息尚未归 Host 决定，因此产品合同还未全部完成。** 本次没有找到第二套独立可写 GameState 或恢复进度，更没有足够证据证明所有残余只读 adapter 属冗余；不要为了追求“零同名结构”破坏实际 AI、两个内置剧本与原子性。先定向修复上述 P1、补高价值测试，再验收实机/AI。文档状态落后于 Git 的项目应以远端事实纠正，不改写过去真实 CI/PR 历史。
