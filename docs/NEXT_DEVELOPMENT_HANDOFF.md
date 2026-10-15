# CampBoardGameHost — Next Development Handoff

> **2026-10-15（Australia/Sydney），唯一活动交接。** 不根据旧快照推断当前分支或 “NEXT”；每次先读 [AGENTS.md](../AGENTS.md)、[current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [AI 连续对话专项](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。被压缩的旧交接全文见 [2026-10-15 同步前交接快照](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md)。

## 下一会话应首先核实

1. 通过 GitHub Connector 核实 live `main`、Draft #304/#306/#307、#305 已关闭状态及分支依赖、CI/R2；通过 Mini MCP `git_state clocktower` 核实开发机 HEAD/索引/工作树。**最近核实但非永久状态**：开发分支 `feature/prod-global-3e3-whole-residual-20261012` 含 `56bc97e` / `409665d`；Android CI #4159、R2 #3776 对 `409665d` GREEN；#307 仍 Draft，不得凭历史 HEAD 推断当前验收。
2. **P0 已落地源代码并通过 GitHub Android CI/R2**：`56bc97e` 移除旧 per-step、Drunk-only、postcommit、one-shot 模型分支和 UI，抽离安全传输并保留 V2 Responses 测试；`409665d` 根据首轮 CI 日志修复手动 Drunk 页 `primary` 引用，第二轮 CI #4159、R2 #3776 全 GREEN。原 Mini MCP 隔离会话 `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 仍是过时未推广候选，**不可覆盖新分支**。本地 Android 构建主机 `photoprismsv` SSH timeout，属于运行基础设施问题，不应伪称应用测试失败。保留 V2 需要的 V1 后缀 Host 合法候选类型。
3. 对 #307 的原有 V2 首夜/剩余联合决策，不能把“已存在 Kotlin 类型/通过部分单元测试”说成完全支持多夜、已发布或无人工说书人的产品。之前 FIELD-1 Chef show 后退出、FIELD-2 首夜合同验证失败需以真实 ADB trace / Host 验证具体报错实证闭环。

## 本次新确定的产品路线（按顺序）

**P0 — V1-RETIRE-NOW（代码及 CI/R2 完成；真机待验）：** 旧独立模型路线已经从 #307 源码中删除，生产只保留 `InitialJointOpeningDirectV2`（同次首夜全局）及 `StorytellerStrategicContinuationV2`（当前合法剩余整包）。V2 无法覆盖或结果过期时必须安全人工接管，不可回退逐点付费 AI。2026-10-15 CI #4159 / R2 #3776 GREEN；真正手机 FIELD-1/FIELD-2 与多夜自动流程仍单独验收。

**P1 — AI-CONVERSATION-1（2026-10-15 实验分支实施中）：** 单局连续 Responses 分阶段交付。已获真实 Luna `store:true` + `previous_response_id` 的 Python/Mini MCP 两次调用证据（约 26.0s→7.8s，但第二次无新增行动，不证明多夜质量）。本轮在 #308 后继实验分支加入 Android 生产**原样完整 Host 输入 + 模型响应锚点**：只在 Host 验证后保留待采纳首夜锚点、在实际采纳后启用链；自动整包私人预选并通过复验后推进锚点，辅助模式未采纳推荐不推进 provider 链；过期/404/400 的链失败不自动付费重试、下一明确请求可从完整事实独立起步；换模型/新局/人工接管/恢复不能沿用旧锚点。顶层指令及 Schema 每轮重送。**当前并未改成事件差量**，仍是完整 Host 状态；事件差量和人工覆盖/持久链游标属于下一阶段。生产调用明确 `store:true`，响应由服务商留存（通常 30 日），与原先 `store:false` 不同，需用户了解留存与成本。当前只做源码/离线验证，须以 CI/R2/真机实证作为完成条件；不宣称生产多夜已验收。

**P2 — AI-DELTA-1 / HUMAN-OVERRIDE：** Host 持久化因果序号、真实动作/观察、原方案与最终采纳的区别；人工可以拒绝推荐 Drunk，也可确认 Drunk 后改掉第一条推荐信息。记录已确认约束后，一次重新规划**全部仍开放且受影响的联动决定**；已公布内容永不撤销。禁止无权限模型响应推进 canonical 对话头；并发/陈旧回答不得写事实或战略游标。事件只入账；真正有当前模型裁量时才调用，一夜多项改动可合并。

**P3 — PLAYER-CLAIMS-1：** 长按玩家座位→公开声称角色（`PUBLIC_CLAIM`）或私下掩饰意向（`PRIVATE_BLUFF_INTENT`）；改口/撤回追加事件，记录 seat/role/来源/公开范围/phase/round/序号。不将声称当真相、不向其他玩家泄漏私密意向。晚间后续决策将这些新增事实当成因果输入，而不是一录入就单独调用 AI。

**P4 — CROSS-GAME-STRATEGY-1：** 复用已有阵容轮换与历史存储，记录过去**实际执行**的 Drunk、线索组合、红鲱鱼、Demon bluffs、可证实登记、相关玩家体验和抽象战略关系。相关 3–5 局短摘要只在新局首夜注入；让 AI 软偏好结构上有差异、依然公平/可推理的方案，不能打分最小化、硬轮换酒鬼或破坏同局多夜一致性。目前 `recentGames` 首夜形参在生产调用中默认为空，`HistoricalClueSignature` 类型尚不等于完整生产去重。

**P5 — MULTI-NIGHT-QUALITY：** 以相同 8 人局首夜→提名/投票/处决/声明→第二夜共情者等可裁量信息做受控 API A/B：不续接/续接/有条件的 persisted reasoning，比战略延续、合法性、费用/延时。增加不同阵容与 Drunk/no-Drunk、Spy/Recluse 见证、manual override、自动保守执行和 Android Studio/ADB 手机演示。不得用合成 basic_checks 当 Host semantic verdict；真机发布链路、CI/R2 都必须通过。

## 技术/安全界限

- 官方 API 参考：https://developers.openai.com/api/docs/guides/conversation-state 及 https://developers.openai.com/api/docs/guides/reasoning 。先核实当前部署模型能力，再决定使用 `all_turns`、response 链还是 `store:false` 本地加密重放；**不要假定模型内部推理能被 APP 读出**。
- 单局 AI 上下文可以跨数小时，但并非免费或永久；以前的 token 在后续请求可能重复计费，须对账。Host 真实记录和较短战略检查点是服务端失效、拒绝方案、被覆盖时的恢复依据。
- 玩家经验按人持久、局内冻结，默认 BEGINNER；无额外真人说书人经验开关。MANUAL、AI_ASSISTED、AI_AUTOMATIC 共用一个 legality/strategy 机制，只有提交权限不同。
- 继续保留当前同版本/≤4 小时的“异常关闭恢复”产品范围，无旧版本迁移、历史档案回放扩张；跨局战略记录属于独立的小型历史索引。
- 文档与协议按**事实账本 / 可错战略假说 / 未执行建议 / 已确认采纳 / 玩家已知信息**隔离，避免模型建议被误记成已实际执行。不要修改用户已看过的信息或猜测 Spy/Recluse 唯一登记。
- 开发治理：本轮 P0 是代码清理及验证，不进行新的付费 API 调用、模型微调、PR 合并或旧路线复活。后续生产修改按 AGENTS 风险测试、GitHub diff 审计和 Android CI/R2；保留 #307 Draft 直到真实多夜验收。

## 历史证据和清理原则

- [3E3 原实现计划](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)、[MODEL-PROMPT-2 之前长 handoff](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md) 仅供历史证据，不构成新一轮“先做 Drunk 单点”或“等 parity 才删 V1”的指令。
- **下一步：** P0 代码及 CI/R2 已通过，先安排真实 Android/ADB 走通 Drunk/no-Drunk、手动接管、FIELD-1/2，多夜结果按证据验收；技术开发可进入 P1 AI-CONVERSATION-1 的非付费设计/离线对照，真实 Luna API 实验必须有单独的调用授权。绝不重新使用隔离旧补丁覆盖已更新的源码和文档。

