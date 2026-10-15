# CampBoardGameHost — Current Development Roadmap

> **2026-10-15（Australia/Sydney）当前唯一优先级权威。** 详细架构：[AI-STORYTELLER-CONTINUITY-1](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)；下一步执行：[handoff](NEXT_DEVELOPMENT_HANDOFF.md)。原有逐 PR 长篇历史已完整归档至 [同步前 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)，不再执行其中的旧“CURRENT/NEXT”。

## 当前核实状态

- 开发机 repo alias `clocktower`；本轮开始时本地 `feature/prod-global-3e3-whole-residual-20261012 @ 7910e1be3d7924a1f5bb2f43a86499b92e3110ee`（clean）。GitHub [Draft #307](https://github.com/Jazz0006/CampBoardGameHost/pull/307) stacked 在 #306 → #304 上，#305 为并行诊断分支；每次开始重新审计 HEAD、main、PR、CI/R2。
- **已在代码中**：V2 同次初始整局联合规划；Host 严格合法候选/全包校验；V2 当前剩余规划及辅助/自动的安全私有预采纳；逐玩家 BEGINNER 默认经验；部分真实 ADB 诊断。尚未证明所有决策域的真正全自动公布或多夜手机验收。
- **尚未在生产完成**：响应续接（当前 API 请求 `store:false` 且互相独立）、增量对话/内部推理复用、玩家长按声明、跨局 AI 战略多样性。
- **V1-RETIRE-NOW 仅在隔离工作树**：Mini MCP session `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 已生成清理旧单点、独立酒鬼、旧分析和 one-shot 的补丁，状态 REVIEW_REQUIRED。静态 diff 检查通过；Gradle 编译被环境阻断、CI 和设备测试未进行；当前开发分支还未应用该补丁。产品决定是**立即退役**，不等新引擎覆盖完，但绝不能写成已经上线。
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
| **P0** | **V1-RETIRE-NOW** | 审查隔离清理补丁，完全移除旧有付费单点路径及无用 UI；仅 V2 整体开局/续规划或安全人工接管。需 Android 编译、焦点回归、GitHub CI/R2/差异审查 |
| **P1** | **AI-CONVERSATION-1** | 真实 `gpt-6-luna` 首夜→白天→第二夜同局续接实验；`previous_response_id`、响应锚点、隐性推理 effective 支持、tokens/延时/战略质量，和无续接基线对比 |
| **P2** | **AI-DELTA-1** | Host 增量因果事件、当前合法完整剩余决策包、并发/失效/人工覆盖管理、补偿/恢复；有真实模型裁量才发付费请求 |
| **P3** | **PLAYER-CLAIMS-1** | 长按玩家、公开角色声明/私下掩饰意向/更改撤回，带时间来源与范围，接入下一次因果增量 |
| **P4** | **CROSS-GAME-STRATEGY-1** | 基于 Host 真实已执行历史生成战略签名，选 3–5 条相关历史初夜注入，AI 执行非强制结构去重 |
| **P5** | **MULTI-NIGHT-FIELD-EVAL** | E01/E02/E03 多样局面，覆盖/投毒/声明/醉酒一致性，自动模式、费用、真实 Android ADB 与 CI/R2 双重验收 |

**活动入口**：[AGENTS.md](../AGENTS.md) · [TESTING_STRATEGY.md](TESTING_STRATEGY.md) · [当前 handoff](NEXT_DEVELOPMENT_HANDOFF.md) · [连续对话新设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。旧实现证据：[3E3](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)；历史正文中的先后状态/旧 NEXT 不再有效。

