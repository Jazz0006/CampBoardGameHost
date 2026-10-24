# GameSession 简明共享说明

适用：Android Host、未来 WebHost、EvidenceLab。沿用已确认的最小事实合同；本说明不创建新的存档格式或实现承诺。核对代码：Host main `17949e72`、Batch 5 Draft #361 `05f6a21f`；下述记录结构在两者一致。

## 一局游戏记录什么

最小记录链：**开局布局 → 酒鬼等开局配置 → 恶魔伪装身份/红鲱鱼 → 首夜行动与信息 → 后续行动与信息 → 结束**。发生顺序以实际记录为准，不把这行示意当作所有剧本的固定夜序。

| 内容 | 当前 Host 的表达 |
|---|---|
| 对局、剧本、座位、玩家、真实/展示角色 | `ClocktowerSessionState.gameState`；玩家状态含阵营、生死、中毒 |
| 酒鬼 | 创建 Session 前确认，结果在真实/展示角色中；目前无独立分配事件 |
| 恶魔伪装身份、红鲱鱼 | `DemonBluffChoice`、`RedHerringChoice` 已确认事件 |
| 玩家行动与变化 | `ActionFact`：投毒、保护、攻击、死亡、角色变化、提名、投票、处决、阶段推进、结束等 |
| 实际告知信息 | `RecordedEpistemicObservation`：内容、能力来源、接收座位、可见范围及阶段/轮次 |
| 两类记录的共同顺序 | 行动和信息分开存放，但共享 `TimelinePoint.globalSequence`；按此合并读取，不能分别排完再拼接 |

Session 是当前游戏事实的唯一写入者。展示预览、尚未确认的选择、AI 建议不算已发生记录。信息只有最终 Next 确认才保存一次；中断后未确认步骤可以重选。

**信息记录的是“玩家实际收到什么”，不保证内容为真。** 中毒占卜师收到“是”，应记录实际的“是”和接收者，而不是偷偷替换为真实查询答案。没有明确记录的 Spy/Recluse 登记不能从结果反推为已发生事实。

## 三个项目如何使用

- Android Host / WebHost：主持游戏，由确认操作产生记录；UI 和网络会话不成为游戏事实来源。
- EvidenceLab：从外部来源整理同样的游戏过程；缺失事实保留未知，不猜角色、目标或告知内容，也不把它伪装成可以直接运行的完整 Host Session。
- 为什么选择 A 而不是 B、来源链接/时间点：作为相关步骤的可选附加说明，不是创建一局记录的必填条件，也不参与机械结算。当前 Host 尚无通用的逐记录备注存储，不能把这条约定说成已实现。

不要求为了记录一局游戏先建立候选评分、训练样本或复杂证据审批体系。需要判断史料真伪时可保留来源与不确定性，但“信息角色失效”和“资料尚未经核实”是不同含义。

## 保存与恢复：以现有实现为准

`ClocktowerSessionJson` v1 保存**当前玩家状态 + 已确认行动/信息 + 序号/上下文**。恢复直接加载当前状态，并从记录重建阶段、轮次、夜间目标、投票等；不是从开局重新执行所有事件。

当前没有独立持久化的、不可变的完整初始布局副本。开局配置确实用于创建 Session，之后 `gameState` 随游戏更新。现有实现支持续局，但完整开局回放尚不能视为已保证。初始布局留存和酒鬼独立步骤记录以后按实际需要补齐，不阻塞续局。

三种 JSON 不要混用：

1. **Session JSON**：顶层 `version/gameId/players/actions/observations/...`，保存当前局。
2. **Android 活动局文件**：外层 `version/session/metadata`；metadata 保存当前模式、AI 对话 ID/模型、策略记忆、游标等适配信息。
3. **旧 TBGS 快照**：`schemaId=botc.tb.game-snapshot`，只是有限的只读 TB 投影，不是完整 Session，也不是新的共享主模型。

已有 Session 编码/解码见 [ClocktowerSessionJson.kt](../app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerSessionJson.kt)，外层见 [ClocktowerActiveGameStore.kt](../app/src/main/java/com/codex/campboardgamehost/persistence/ClocktowerActiveGameStore.kt)。角色 ID 使用现有 `RoleId.value`，不能和旧 TBGS 外部 ID、证据资料的大写角色名直接混用；座位在当前 Host 为从 1 开始的连续整数。WebHost 尚未因本说明获得完整兼容实现。

## AI 续接与断网目标

当前 AI 续规划输入含当前状态、当前决策、策略记忆及游标之后的已确认事件。对话 ID 和游标配套保存/恢复；ID 不可用时，显式重试可用当前状态及从头记录重新提供上下文。不能用最后一次“发起请求”的位置代替已接受的同步位置。过期响应不准写入当前步骤。

产品目标是：AI 不可用也能继续本局。允许人工接管；人工明确触发的随机只适用于当前可裁量结果，仍需展示和 Next 确认。规则唯一结果由 Host 计算，玩家目标由玩家决定。**统一夜间接管入口和可选随机尚待实现**；不能仅凭错误提示“请手动继续”就声称已完整支持。

## 使用边界

本说明供未来实现共同参考，不要求搬运 Android UI、另造第二个游戏对象或恢复旧评分引擎。下一步优先补齐离线续局与恢复行为；如未来确需文件交换，再围绕具体消费者添加少量共享样本和明确的字段校验。无需先建跨语言 SDK 或大规模兼容平台。
