# CampBoardGameHost — Next Development Handoff

> **本轮执行结果更新**：Draft #315 的六批源代码清理已推送；当前 `cleanup/arch-cleanup-1-20261015` HEAD `0a3a7976cece`。六批 Android CI/R2 均 GREEN；归并前仍须检查最新合并候选 HEAD 和受影响的规则、Recovery 契约。#312/#313/#314 已关闭且未合并，#314 的审计已纳入本清理分支；剩余 #310/#311、#304–#308 与 1E 实验分支按实际依赖进一步复审。不要将这条记录理解为整轮 ARCH-CLEANUP-1 已结束。


> **当前唯一 NEXT（2026-10-15）：完成 ARCH-CLEANUP-1 并在 CI/R2 与依赖收口后恢复 AI-EVENT-JIT。** 不要按以下旧段落启动任何提示词实验、真实 API 调用或新功能。先读 [架构清理进展与保留边界](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)、[Draft #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315)、[TBGS/UGSM 审计](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md)。确保 #304/#306/#307/#308/#310-#314 与当前 main 的实际 live 依赖，不合并已过时的候选 ID 模型合同或过滤投票语义。


> **2026-10-15（Australia/Sydney），唯一活动交接。** 不根据旧快照推断当前分支或 “NEXT”；每次先读 [AGENTS.md](../AGENTS.md)、[current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [AI 连续对话专项](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。被压缩的旧交接全文见 [2026-10-15 同步前交接快照](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md)。

## 下一会话应首先核实

1. 用 GitHub Connector 核实 live `main`、Draft #304/#306/#307/**#308**、分支依赖和 CI/R2；用 Mini MCP `git_state clocktower` 核实 HEAD/索引/工作树。**2026-10-15 最近核实，非永久状态**：活动开发分支 `experiment/ai-conversation-1-probe-20261015`，HEAD `f09a257375e5631fe0750d6b83982b4e118aa876`，本地/remote 同步且 clean；该提交 Android CI #4175、R2 #3791、P1 Kotlin fixture #29 GREEN。#308 stacked 在 #307 → #306 → #304 上，均暂不自动合并。
2. **P0 已落地源代码并通过 GitHub Android CI/R2**：`56bc97e` 移除旧 per-step、Drunk-only、postcommit、one-shot 模型分支和 UI，抽离安全传输并保留 V2 Responses 测试；`409665d` 根据首轮 CI 日志修复手动 Drunk 页 `primary` 引用，第二轮 CI #4159、R2 #3776 全 GREEN。原 Mini MCP 隔离会话 `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 仍是过时未推广候选，**不可覆盖新分支**。本地 Android 构建主机 `photoprismsv` SSH timeout，属于运行基础设施问题，不应伪称应用测试失败。保留 V2 需要的 V1 后缀 Host 合法候选类型。
3. 对 #307 的原有 V2 首夜/剩余联合决策，不能把“已存在 Kotlin 类型/通过部分单元测试”说成完全支持多夜、已发布或无人工说书人的产品。之前 FIELD-1 Chef show 后退出、FIELD-2 首夜合同验证失败需以真实 ADB trace / Host 验证具体报错实证闭环。

## 本次新确定的产品路线（按顺序）

**P0 — V1-RETIRE-NOW（代码及 CI/R2 完成；真机待验）：** #307 已删除旧独立模型路线，生产保留 `InitialJointOpeningDirectV2`（开局全局规划）及 `StorytellerStrategicContinuationV2`（现有后续整包规划；**即将按新产品决策重构为按需当前单项**）。绝不能因为后续只输出一项就复活 V1 角色孤立分析/旧单项付费 fallback。当前出错安全人工接管，手机 FIELD-1/FIELD-2 与多夜自动流程仍待验。

**P1 — AI-CONVERSATION-1（Kotlin 首版 CI/R2 GREEN，真机待验）：** #308 在 Android 生产 V2 实现 `store:true`、采纳后锚点、`previous_response_id`、Host 合法校验及失败安全恢复；`f09a2573` 已通过 Android CI #4175、R2 #3791 和独立 Kotlin fixture #29。现仍每轮重送完整 Host 状态及 JSON schema，而非增量事实。开发机真实 Luna 两次续接 26.0s → 7.8s 仅证明 API 链可用，**第二次没有真实新事件，不能证明多夜提速/质量**。留存/token 费用和 Android ADB 待实证；本轮不修改此基线生产代码。

**P2 — AI-EVENT-JIT-1（下一对话立即实施；正式取代 AI-DELTA-1 的后续整包合同）：** 人员/展示阵容确认即启动首次联合全局请求，与身份展示并行；初始联合考虑 Drunk/红鲱鱼/恶魔掩饰身份及当时合法可预选的线索，未知未来事件不得编造。Host 的真实 Drunk 和每条信息/行动/覆盖等**不论是否采纳 AI 建议**均进入同一魔典/游戏记录，并区别提议、否决、实际采纳和对玩家公布。下一项需要线索时：唯一玩家可见合法结果 Host 直接定；若已有合法、条件仍满足、全局战略未受相关改变的缓存建议就直接复用且不调用 API；否则将最后 Host 确认的响应锚点之后所有尚未发送的因果事件一次提交，延续整局战略并**只返回当前一项 Host 合法候选 ID**（由 Host 映射为 `0/1/2`、`有/没有`、角色名、合法成对信息等）。覆盖和玩家行动影响推荐有效性但不要机械清空无关推荐；有理由预测下一项需要裁量时可在展示期间提前请求，不能假设未发生的玩家行动。确保严格 Host/revision/候选/因果游标校验、拒绝/失效响应不推进 canonical 链、过期/并发/手工覆盖 fork、安全人工接管与恢复。**绝不把单项输出理解为孤立角色 AI 思考。**

**P3 — PLAYER-CLAIMS-1：** 长按玩家座位→公开声称角色（`PUBLIC_CLAIM`）或私下掩饰意向（`PRIVATE_BLUFF_INTENT`）；改口/撤回追加事件，记录 seat/role/来源/公开范围/phase/round/序号。不将声称当真相、不向其他玩家泄漏私密意向。晚间后续决策将这些新增事实当成因果输入，而不是一录入就单独调用 AI。

**P4 — CROSS-GAME-STRATEGY-1：** 复用已有阵容轮换与历史存储，记录过去**实际执行**的 Drunk、线索组合、红鲱鱼、Demon bluffs、可证实登记、相关玩家体验和抽象战略关系。相关 3–5 局短摘要只在新局首夜注入；让 AI 软偏好结构上有差异、依然公平/可推理的方案，不能打分最小化、硬轮换酒鬼或破坏同局多夜一致性。目前 `recentGames` 首夜形参在生产调用中默认为空，`HistoricalClueSignature` 类型尚不等于完整生产去重。

**P5 — MULTI-NIGHT-QUALITY：** 8 人及 15 人不同阵容，验证开局背景请求、采纳/否决 Drunk、下一条已缓存且仍有效时零网络请求、人工覆盖导致缓存失效时单项按需请求、Host 唯一结果零调用、模型只返回当次候选但理解全局战略、连续对话无缺漏事件、长局成本/延迟。包括毒/醉/Spy/Recluse 多义/红鲱鱼、自动与辅助模式及 Android Studio/ADB。不得把机械 basic_checks 当 Host 语义验收；真实 15 人局首夜六项全部延期问题已获批准**暂缓**，不是当前 P2 阻塞项。

## 技术/安全界限

- 官方 API 参考：https://developers.openai.com/api/docs/guides/conversation-state 及 https://developers.openai.com/api/docs/guides/reasoning 。先核实当前部署模型能力，再决定使用 `all_turns`、response 链还是 `store:false` 本地加密重放；**不要假定模型内部推理能被 APP 读出**。
- 单局 AI 上下文可以跨数小时，但并非免费或永久；以前的 token 在后续请求可能重复计费，须对账。Host 真实记录和较短战略检查点是服务端失效、拒绝方案、被覆盖时的恢复依据。
- 玩家经验按人持久、局内冻结，默认 BEGINNER；无额外真人说书人经验开关。MANUAL、AI_ASSISTED、AI_AUTOMATIC 共用一个 legality/strategy 机制，只有提交权限不同。
- 继续保留当前同版本/≤4 小时的“异常关闭恢复”产品范围，无旧版本迁移、历史档案回放扩张；跨局战略记录属于独立的小型历史索引。
- 文档与协议按**事实账本 / 可错战略假说 / 未执行建议 / 已确认采纳 / 玩家已知信息**隔离，避免模型建议被误记成已实际执行。不要修改用户已看过的信息或猜测 Spy/Recluse 唯一登记。
- 开发治理：本次**只更新产品设计、roadmap 与 handoff，不修改生产代码、不付费调用、不合并 PR**；下一对话进入 P2 实施。按 AGENTS evidence-first 风险测试、GitHub diff 审计和 Android CI/R2；#307/#308 保留 Draft 直到真实多夜/ADB 验收。

## 历史证据和清理原则

- [3E3 原实现计划](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)、[MODEL-PROMPT-2 之前长 handoff](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md) 仅供历史证据，不构成新一轮“先做 Drunk 单点”或“等 parity 才删 V1”的指令。
- **下一步（新对话工作票）：** 正式进入 **AI-EVENT-JIT-1**。从 live Draft #308 及已批准设计开始：①审计现有魔典/事件账本和 `remainingChoices` 消费者；②规划并实现缓存推荐关联事实依赖/过期判断、全局战略与当前单项候选的接口，维护已采用与拒绝建议的 provider fork；③合并从最后确认锚点后的事件增量，不丢失人工覆盖；④针对缓存命中零调用、确有必要的一次调用、Host 唯一结果、重复/过期事件、已公布事实不改写、真实不同来源的 Drunk/后续结果做重点 Kotlin/Android CI 验证；⑤通过独立 CI/R2 才可进行真机/单独授权 API 实验。P15 全部预规划延期问题暂缓，禁止特例修补。**所有真实付费调用均需新的明确授权**。

