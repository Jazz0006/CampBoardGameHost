# CampBoardGameHost — Current Development Roadmap

> **2026-10-15（Australia/Sydney）当前唯一优先级权威。** 详细架构：[AI-STORYTELLER-CONTINUITY-1](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)；下一步执行：[handoff](NEXT_DEVELOPMENT_HANDOFF.md)。原有逐 PR 长篇历史已完整归档至 [同步前 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)，不再执行其中的旧“CURRENT/NEXT”。

## 当前核实状态

- 开发机 repo alias `clocktower`；活动分支 `feature/prod-global-3e3-whole-residual-20261012`，截至 P0 实现已含 `56bc97e` / `409665d`，本地与 GitHub 曾同步且 clean。GitHub [Draft #307](https://github.com/Jazz0006/CampBoardGameHost/pull/307) stacked 在 #306 → #304 上，#305 已关闭为 superseded；每次开始仍需重新核对 HEAD、main、PR、CI/R2。
- **已在代码中**：V2 同次初始整局联合规划；Host 严格合法候选/全包校验；V2 当前剩余规划及辅助/自动的安全私有预采纳；逐玩家 BEGINNER 默认经验；部分真实 ADB 诊断。尚未证明所有决策域的真正全自动公布或多夜手机验收。
- **尚未在生产完成**：响应续接（当前 API 请求 `store:false` 且互相独立）、增量对话/内部推理复用、玩家长按声明、跨局 AI 战略多样性。
- **V1-RETIRE-NOW — 代码及 CI/R2 GREEN，真机待验**：2026-10-15 在活动 #307 分支以 `56bc97e`（8 文件修改、16 文件删除）和 `409665d`（修复手动酒鬼选择残留的 `primary` 引用）实现旧 Drunk-only、postcommit analysis、one-shot、逐项付费回退的退役。旧分析 UI 已移除；V2 唯一两条付费请求使用独立安全 HTTPS 传输，旧响应 schema 拒绝进入 adapter。GitHub CI #4159（含 Android unit/build）与 R2 #3776 于 `409665d` GREEN；本地 `test:fast` 因构建主机 `photoprismsv` SSH 超时未执行。原 Mini MCP 隔离 session `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 仅为**被新实现取代的历史候选**，不得重复覆盖当前分支或新版文档；后续源码微调需要重新 CI。真机 ADB / FIELD-1、FIELD-2 / 多夜游戏未获验收。
- FIELD-1 Chef 点击展示退出与 FIELD-2 首夜合同校验故障仍须依赖实际 ADB/Host 证据闭环。真实 Luna 输出/基础检查、单个 CI GREEN 不等于多夜质量验收。

## 不可违背的产品原则

1. **Host 为唯一 Game State、事实、规则、合法候选和发布权威**；AI 只是会修订假设的整体战略说书人。无 AI/联网时 MANUAL 独立可玩；多义/非法/过期自动结果暂停并人工接管。
2. **唯一整体战略引擎**：首夜联合选择 Drunk（若有）、信息、Red Herring 与 Demon bluffs；后续所有角色与事件都经“真实事件→战略修订→当前全部合法剩余决定联合推荐”。不再保留 V1 单点 fallback、独立酒鬼模型调用、专用投毒者/占卜师重规划器或旧评分排名。
3. **同局连续对话优先**：首夜给完整局面；此后只传新增 Host 事件与当前开放的整包合法裁量，战略/假说沿同一对话链修订；兼容模型的跨轮内部推理复用是**待实测能力**。Host 事实不可仅寄存在 provider 对话中。
4. **人工覆盖可随时成为已确认约束**：酒鬼或后续线索可部分采纳、部分修改；未执行建议不能成为事实；已公布内容不可改写；AI 只联合重算尚开放且受影响的剩余配置。
5. **玩家声明记录来源和可见性**：公开声称 ≠ 真实身份，私下掩饰意向 ≠ 已向所有人宣布；更改/撤回追加事件。不因录入一条声明就发一次付费 API。
6. **跨局经验帮助软去重**：Host 长期保留过往实际采用的联合配置、结构性信息交叉、玩家体验；首夜提供相关少量摘要，AI 优先在同样合理的方案之间提供新体验。多样性不得损害公平、因果一致性，不能强制角色轮换或单点相似度评分。
7. 玩家熟练度是人的稳定档案，默认 BEGINNER；MANUAL / AI_ASSISTED / AI_AUTOMATIC 同一规则源。当前短期异常退出 Recovery 范围（同版本、≤4 小时）不因 AI 持久对话而无端扩张；Spy/Recluse 多解见证不自动变成确定登记。

## 当前按顺序开发

| 优先 | 任务 | 最小可验收交付 |
| --- | --- | --- |
| **P0** | **V1-RETIRE-NOW · 代码/CI 已完成** | 旧付费单点路径与过时 UI 已退役；仅 V2 整体开局/续规划或安全人工接管。GitHub Android CI/R2 已通过；仍待真机 ADB 和 FIELD-1/2 多夜体验验收，不得写成正式发布 |
| **P1** | **AI-CONVERSATION-1** | 真实 `gpt-6-luna` 首夜→白天→第二夜同局续接实验；`previous_response_id`、响应锚点、隐性推理 effective 支持、tokens/延时/战略质量，和无续接基线对比 |
| **P2** | **AI-DELTA-1** | Host 增量因果事件、当前合法完整剩余决策包、并发/失效/人工覆盖管理、补偿/恢复；有真实模型裁量才发付费请求 |
| **P3** | **PLAYER-CLAIMS-1** | 长按玩家、公开角色声明/私下掩饰意向/更改撤回，带时间来源与范围，接入下一次因果增量 |
| **P4** | **CROSS-GAME-STRATEGY-1** | 基于 Host 真实已执行历史生成战略签名，选 3–5 条相关历史初夜注入，AI 执行非强制结构去重 |
| **P5** | **MULTI-NIGHT-FIELD-EVAL** | E01/E02/E03 多样局面，覆盖/投毒/声明/醉酒一致性，自动模式、费用、真实 Android ADB 与 CI/R2 双重验收 |

**活动入口**：[AGENTS.md](../AGENTS.md) · [TESTING_STRATEGY.md](TESTING_STRATEGY.md) · [当前 handoff](NEXT_DEVELOPMENT_HANDOFF.md) · [连续对话新设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。旧实现证据：[3E3](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)；历史正文中的先后状态/旧 NEXT 不再有效。

