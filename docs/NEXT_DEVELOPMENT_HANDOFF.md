# CampBoardGameHost — Next Development Handoff

> **2026-10-15（Australia/Sydney），唯一活动交接。** 不根据旧快照推断当前分支或 “NEXT”；每次先读 [AGENTS.md](../AGENTS.md)、[current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [AI 连续对话专项](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。被压缩的旧交接全文见 [2026-10-15 同步前交接快照](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md)。

## 下一会话应首先核实

1. 通过 GitHub Connector 核实 live `main`、Draft #304/#306/#307 及并行 #305 的状态、review/CI/R2 与分支堆叠；通过 Mini MCP `git_state clocktower` 核实开发机 HEAD/索引/工作树。**最近已核实但非永久状态**：开发分支 `feature/prod-global-3e3-whole-residual-20261012 @ 7910e1be3d7924a1f5bb2f43a86499b92e3110ee`、#307 Draft、工作树在本轮文档修改之前 clean。
2. 核实 Mini MCP 隔离会话 `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 状态 REVIEW_REQUIRED：删除 V1 per-step、Drunk-only、postcommit 和 one-shot 模型分支的补丁**在隔离工作树，没有合入当前本地开发分支**。`git diff --check` 已通过，但上次 Oracle 本地 Gradle 由于环境阻断而没有编译；严格审查所有删除文件及 V2 依赖后，通过 GitHub CI/R2 和真实机型闭环再接纳/推广。保留仍供 V2 使用的 V1 后缀 Host 类型；不因标签 V1 就删规则合同。
3. 对 #307 的原有 V2 首夜/剩余联合决策，不能把“已存在 Kotlin 类型/通过部分单元测试”说成完全支持多夜、已发布或无人工说书人的产品。之前 FIELD-1 Chef show 后退出、FIELD-2 首夜合同验证失败需以真实 ADB trace / Host 验证具体报错实证闭环。

## 本次新确定的产品路线（按顺序）

**P0 — V1-RETIRE-NOW：** 旧模型路线不再因功能尚未覆盖而等待退役。生产应只有 `InitialJointOpeningDirectV2`（同次首夜全局）和 `StorytellerStrategicContinuationV2`（当前合法剩余整包），不支持的决定进入安全 Manual/assisted takeover，不能回退逐点 AI。审查、CI、真机后才算完成。

**P1 — AI-CONVERSATION-1：** 首夜建立**同局连续 Responses 对话**。首轮全局状态 + 联合配置 + 可错战略摘要；后续仅增量事件 + 当前 phase/round + 全部当前 Host-open legal domains。优先试 `previous_response_id` 和可确认 response_id 锚点；每轮继续发送核心顶层指令与严格输出 Schema。必须用真正 `gpt-6-luna` 实验 `reasoning.context` 的 effective 支持；官方对别的模型支持 `all_turns` 不可直接推断本模型支持。当前 `store:false` 独立请求没有这样的连续记忆。跟踪完整 usage、延时、数据留存风险/费用及 context 累积。

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
- 开发治理：本轮为文档同步，不授权新的付费 API、模型微调、PR 合并或将旧路线复活。后续生产改动按 AGENTS 风险测试、GitHub Connector 远端 diff 审计和 Android CI/R2；保留 #307 Draft 直到真正可玩的多夜验收。

## 历史证据和清理原则

- [3E3 原实现计划](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)、[MODEL-PROMPT-2 之前长 handoff](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md) 仅供历史证据，不构成新一轮“先做 Drunk 单点”或“等 parity 才删 V1”的指令。
- **下次首先完成 P0 隔离补丁审查和验收**，再做连续对话方案的 Python 真实 API 原型；不重复建设独立推荐器或大规模存档系统。

