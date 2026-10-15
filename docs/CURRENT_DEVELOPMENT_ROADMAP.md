# CampBoardGameHost — Current Development Roadmap

> **ARCH-CLEANUP-1 最新（2026-10-15）**：Draft #315 已完成七批清理，代码 HEAD `f4c9468643e7`；七批 Android CI/R2 GREEN，最新代码 CI #4194、Real Clingo 与 R2 #3810 均成功。#310–#314 已关闭且未合并；当前只剩 #304/#306/#307/#308/#315 五个开放 PR。P1 上游四项仍有真实生产调用，#315 继续叠加其上，不得直接合并 main。1E 实验分支只用于后续参考语义直出合同，不整体合并。


> **当前执行优先级（2026-10-15）：ARCH-CLEANUP-1 进行中。** 先完成架构收敛、死代码清理、实证验收与 PR 收口；**暂时停止 AI-EVENT-JIT 提示词开发、新功能和付费 API 实验**。以 [ARCH-CLEANUP-1 审计/进展](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md) 和 [PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) 为活动工作。下文原 AI 续接 P2 计划仅为恢复开发时的背景，不代表当前 NEXT。


> **2026-10-15（Australia/Sydney）当前唯一优先级权威。** 详细架构：[AI-STORYTELLER-CONTINUITY-1](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)；下一步执行：[handoff](NEXT_DEVELOPMENT_HANDOFF.md)。原有逐 PR 长篇历史已完整归档至 [同步前 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)，不再执行其中的旧“CURRENT/NEXT”。

## 当前核实状态

- 开发机 repo alias `clocktower`；**2026-10-15 最近确认活动分支** `experiment/ai-conversation-1-probe-20261015`、HEAD `f09a257375e5631fe0750d6b83982b4e118aa876`、本地/remote 同步且工作树 clean；[Draft #308](https://github.com/Jazz0006/CampBoardGameHost/pull/308) stacked 在 Draft #307 → #306 → #304 上。对该 HEAD 的 Android CI #4175、R2 #3791、P1 Kotlin fixture #29 均 GREEN；不得假定随后没有更新，每次新会话先检查 live state。#305 已关闭为 superseded。
- **已在代码中**：V2 同次初始整局联合规划；Host 严格合法候选/全包校验；V2 当前剩余规划及辅助/自动的安全私有预采纳；逐玩家 BEGINNER 默认经验；部分真实 ADB 诊断。尚未证明所有决策域的真正全自动公布或多夜手机验收。
- **P1 已作为首个独立源码切片通过 Android CI/R2/Kotlin fixture，仍待 Android 真机实证**：Draft #308 的已采纳开局保存 Responses anchor，后续 `previous_response_id` + `store:true`，目前每次仍发送**完整 Host 状态**；辅助模式未采纳的建议不推进对话头或战略记忆，自动私有预选须 Host 核验。API 留存行为不同于旧 `store:false`。**新批准的 P2 产品方向**：将后续“完整 `remainingChoices` 联合整包”改为“有效预推荐复用；必要时仅请求**当前单项合法结果**”，同时保持模型的整局战略。事件增量、推荐失效依赖、人工覆盖/fork 与持久游标仍待实施。
- **V1-RETIRE-NOW — 代码及 CI/R2 GREEN，真机待验**：2026-10-15 在活动 #307 分支以 `56bc97e`（8 文件修改、16 文件删除）和 `409665d`（修复手动酒鬼选择残留的 `primary` 引用）实现旧 Drunk-only、postcommit analysis、one-shot、逐项付费回退的退役。旧分析 UI 已移除；V2 唯一两条付费请求使用独立安全 HTTPS 传输，旧响应 schema 拒绝进入 adapter。GitHub CI #4159（含 Android unit/build）与 R2 #3776 于 `409665d` GREEN；本地 `test:fast` 因构建主机 `photoprismsv` SSH 超时未执行。原 Mini MCP 隔离 session `a8e87ce2-334f-47ca-8ea0-06d7180a4fde` 仅为**被新实现取代的历史候选**，不得重复覆盖当前分支或新版文档；后续源码微调需要重新 CI。真机 ADB / FIELD-1、FIELD-2 / 多夜游戏未获验收。
- FIELD-1 Chef 点击展示退出与 FIELD-2 首夜合同校验故障仍须依赖实际 ADB/Host 证据闭环。真实 Luna 输出/基础检查、单个 CI GREEN 不等于多夜质量验收。

## 不可违背的产品原则

1. **Host 为唯一 Game State、事实、规则、合法候选和发布权威**；AI 只是会修订假设的整体战略说书人。无 AI/联网时 MANUAL 独立可玩；多义/非法/过期自动结果暂停并人工接管。
2. **唯一整体战略引擎，不等于每次整包输出**：人员/阵容确定后马上并行展示身份发起一次联合开局规划，统一权衡 Drunk（若有）、能预定的信息、Red Herring 与 Demon bluffs。**后续每次只需要当前这一个 Host 合法结果**，但基于持续全局战略和实际游戏事件选择；已有有效预推荐直接复用。2026-10-15 批准**退役“后续整包 `remainingChoices` 为必需输出”的旧合同**。绝不恢复 V1 孤立单点调用、酒鬼独立模型调用、投毒者/占卜师专用规划器或旧评分排序。
3. **同局连续对话 + 魔典增量记录**：首轮完整输入；后续只有需要 AI 时，提交从**最后一个 Host 已确认 provider 锚点**以来的全部新增事实/手动决定/玩家行动记录，以及当前阶段、版本和**当前唯一决策的合法候选**。未采纳、过期的回应不能推进锚点；旧锚点不可用则以完整 Host 快照恢复。兼容模型的跨轮内部推理复用仍待实测；Host 事实不得只寄存在 provider。
4. **实际决定一律写入魔典/游戏记录**：无论 Drunk 或下一条线索是 AI 推荐还是人类指定，真实提交与来源都记录，模型提议与拒绝另列，不能冒充已发生。修改、状态变化、玩家行动都促使 Host 通用检查**后续缓存推荐合法性及战略前提**；受影响才失效。下一个必要裁量点若无有效缓存，再按需请求一个结果，而不是每改一条就立即重算剩余整包。已公布结果不可改写。
5. **玩家声明记录来源和可见性**：公开声称 ≠ 真实身份，私下掩饰意向 ≠ 已向所有人宣布；更改/撤回追加事件。不因录入一条声明就发一次付费 API。
6. **跨局经验帮助软去重**：Host 长期保留过往实际采用的联合配置、结构性信息交叉、玩家体验；首夜提供相关少量摘要，AI 优先在同样合理的方案之间提供新体验。多样性不得损害公平、因果一致性，不能强制角色轮换或单点相似度评分。
7. 玩家熟练度是人的稳定档案，默认 BEGINNER；MANUAL / AI_ASSISTED / AI_AUTOMATIC 同一规则源。Host 唯一可区分结果直接确定、无须模型；可预知下一项需要裁量且前提已确认时允许后台预取，但不得猜测未来玩家行动、不得使用过期推荐。当前短期异常退出 Recovery 范围（同版本、≤4 小时）不因 AI 持久对话而扩张；Spy/Recluse 多解见证不自动变成确定登记。

## 当前按顺序开发

| 优先 | 任务 | 最小可验收交付 |
| --- | --- | --- |
| **P0** | **V1-RETIRE-NOW · 代码/CI 已完成** | 旧付费单点路径与过时 UI 已退役；仅 V2 整体开局/续规划或安全人工接管。GitHub Android CI/R2 已通过；仍待真机 ADB 和 FIELD-1/2 多夜体验验收，不得写成正式发布 |
| **P1** | **AI-CONVERSATION-1 · Kotlin 首版 CI/R2 GREEN；真机待验** | #308 已实现 Host 采纳后锚点、`previous_response_id`、严格 schema + 全量 Host 真相；Luna 已有两次付费续接实验，但没有真实跨夜新事件，需 ADB 验收；保持 Draft |
| **P2 — 下一实施任务** | **AI-EVENT-JIT-1 · 游戏记录驱动的按需单项裁量** | 阵容确认后即时联合开局；实际 Drunk/线索/人工覆盖均入魔典；有效预推荐直接复用；发生实质变化或无缓存时，按最后确认的锚点补齐事件，只让 AI 返回**当前一个** Host 合法候选 ID；Host 唯一结果不调用、可安全预取；失效/并发/fork/人工接管安全处理，替换旧续规划整包合同 |
| **P3** | **PLAYER-CLAIMS-1** | 长按玩家、公开角色声明/私下掩饰意向/更改撤回，带时间来源与范围，接入下一次因果增量 |
| **P4** | **CROSS-GAME-STRATEGY-1** | 基于 Host 真实已执行历史生成战略签名，选 3–5 条相关历史初夜注入，AI 执行非强制结构去重 |
| **P5** | **MULTI-NIGHT-FIELD-EVAL** | E01/E02/E03 多样局面，既测缓存命中无 API 又测失效后单次当前线索返回；覆盖/投毒/声明/醉酒一致性、自动模式、费用/延迟、真实 Android ADB 与 CI/R2 双重验收。15 人局所有首夜信息提前确定的旧缺口暂缓，不阻断 P2 |

**活动入口**：[AGENTS.md](../AGENTS.md) · [TESTING_STRATEGY.md](TESTING_STRATEGY.md) · [当前 handoff](NEXT_DEVELOPMENT_HANDOFF.md) · [连续对话新设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)。旧实现证据：[3E3](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md)、[3E2](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md)、[3E](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md)；历史正文中的先后状态/旧 NEXT 不再有效。

