# ARCH-CLEANUP-2 — 单一权威 Session、逐步确认与实用恢复（2026-10-16）

> **CURRENT APPROVED IMPLEMENTATION CONTRACT / 尚未实施。** 此文件替代 ARCH-CLEANUP-1 中与本路线冲突的未来验收要求；ARCH-CLEANUP-1 是已完成批次与历史审计记录，不删除已发生事实。全局开发原则见根目录 AGENTS.md。
>
> 唯一活动实施分支：cleanup/arch-cleanup-1-20261015；唯一集成入口：Draft PR #315（base main，未合并）。起点代码 HEAD ec6596a56cd23981acdc3e30b2cfcf6b23820ca0，Android CI #4213 与 R2 #3829 SUCCESS，仅为起点证据。新实施必须重新确认 live HEAD / worktree / PR / CI。
>
> 优先 ARCH-CLEANUP-2 A1→A6；暂停 AI-EVENT-JIT/P2 新功能、付费模型实验和 main 合并。文档批准 ≠ 生产已实现，A 目前 PARTIAL。

## 1. 产品决策和根本原则

**只有一个始终可靠的当前游戏对象，加一份足够完整、可供 AI 理解的结构化游戏记录。**

1. ClocktowerGameSession / ClocktowerSessionState 的 GameState 是唯一当前机械事实权威；阶段、步骤及已确认设置也必须在同一 Session 权威下收敛。UI PlayerCard/Compose、GameSnapshot/TBGS、历史导出、AI 战略、Recovery/Archive 都只是临时状态或只读投影，不能反向作为第二个真实来源。Undercover 的独立游戏机制不受此路线影响。
2. ActionFactTimeline + EpistemicObservationLog 共用严格全局顺序，是**逻辑上的一份结构化历史**。无需强行物理合并成第三套通用 EventStore。因果日志中保留真实需要的裁量来源/证据，删除与正式历史重复的可变事实。
3. **迁移必须是替代与删除，不是叠加兼容：每引入新的统一接口，先列出所替代的旧接口、状态、调用路径、测试和恢复字段；迁移并验证后立即删除旧实现，不保留长期双轨。** 删除前确认所有实际生产消费者；禁止只新增同步层和长期 parity 校验来维持两份真相。
4. 一个正式 Host 决定以**该步骤确认**为提交边界：UI 上一般是“下一步”，但正式的处决、投毒目标、投票、信息公布可以有自身确认/公布动作。合法性验证→Session 当前状态+结构化事实一次性提交→刷新只读 UI；幂等防重复。未确认草稿和未采用的 AI 方案不得修改正式事实。
5. **实际公布的信息就是应记录的信息**：醉酒、中毒、虚假结果、成对结果等须保存玩家真实看到的结构化语义、接收者、时间、可靠性/来源；不可从正确规则值或界面文案推断替代。公开/私密边界保留；已经告知玩家的信息不得静默改写。
6. 初始联合 AI 全局规划保留，**首夜/后续“一键采纳整包”及专门的整包采纳状态机退役**。AI_ASSISTED 在当前步骤预选、可修改并确认；AI_AUTOMATIC 在当前合法可执行步骤自动确认（必要的展示/交接仍执行），MANUAL 同一个 Session 提交入口。已有计划的未执行项只是带适用前提的缓存；人工覆盖或真实事件改变依赖时，受影响的缓存失效，按需重规划。绝不再单独规划 Drunk，或因整包获批而提前确认后续信息。
7. **异常恢复的验收标准是可安全继续游戏，不是逐像素还原 UI 或任意历史时点重建**：重新进入后显示正确魔典、保留已经公布的事实和信息、从一个安全步骤继续；允许丢弃当前未确认草稿、重新确认尚未完成的动作。不能恢复出错误的身份/死亡/已公布信息。存档以最后可靠 Session 和必要的最小进度投影为基础，写盘必须避免“状态已变而历史未落盘”的半提交。
8. 暂**不开发通用 Undo/Redo 或任意历史回滚**。未确认的 UI 草稿可以退回修改；正式确认或已公布内容不可静默回退。仅修正最近一次、且尚未造成玩家已知或后续 AI 影响的操作属于未来可选便捷功能，不得阻塞架构收敛。
9. 无旧 APK/旧存档兼容负担；允许明确升级 Recovery 格式而删除遗留适配。保留真实 Host 规则、合法性/登记见证、TBGS 与 EvidenceLab 已验证的只读协议边界；Recovery 与游戏完成后的 Archive 不是两个权威。

## 2. 当前起点的真实源码债务（非已完成声明）

- ClocktowerSessionView 仍故意不暴露 GameState；多处生产路径从 cards.toClocktowerGameState() 重新构造，UI PlayerCard 事实仍可单独修改。
- Session 的 advanceGameStateRevision、commitGameStateBoundary、synchronizeGameStateWithinCurrentRevision、commitGlobalActionFact、commitGlobalEpistemicObservation 等仍有不同调用时序；正式动作与状态同步、公布与结构化记录尚非处处一个事务。
- ClocktowerJointOpeningReviewScreen 有“一键采纳完整首夜方案”；ClocktowerHostScreen 有“一键采纳剩余方案”；CampBoardGameHostApp 的 adoptJointOpeningAfterReveal/adoptWholeResidualProposal、residualProposed*/residualAdopted* 存在整包采纳分叉。**仅删除按钮是不够的**：必须同时安全迁移使用者并删除失去存在理由的状态/规则层。
- recordReliablePrivateInformation 对不可靠信息且没有 displayProposition 时会直接返回，不能证明所有已显示的醉酒/中毒线索都进入 EpistemicObservationLog。
- RecoverySnapshot/RecoveryRestorePlanner 仍保存或从 cards 重建部分正式机械状态，并携带可审计的旧 replay 投影。必须按生产消费链收敛，不凭 Sde 文件名盲删；Archive 只读显示逻辑单独评估。
- 已有 Session-owned 红鲱鱼/恶魔掩饰选择及事件、Session-owned causal journal、Virgin/Slayer 派生去重等 A 阶段成果保留；本轮重点是**完成收敛并删除被替代路径**，不要重建一套相似对象。

## 3. 分批实施次序（每批提交真实代码与删除结果）

### A1 — 单一正式提交边界

- 逐个列举 Session 状态改变、结构化行动、观察、UI 操作和版本推进的现役调用图；选定最窄的 Session 确认/公布提交接口，不开发通用命令总线。
- 机械事实+对应行动记录和 revision / globalSequence 在一个已验证的 Session 状态跃迁中提交；信息实际公布边界需预检并保证可持久记录；任何重复确认幂等。
- 列表式注销所替代的分散写入口、冗余版本更新和跨 UI 同步路径；只保留规则引擎必需的纯函数与验证。
- 验收：正式确认后 Session 与记录无半提交，重复点不会多一条事实；异常/拒绝不会写入局内真相。

### A2 — 取消所有整包采纳

- 删除两处按钮及 ClocktowerJointOpeningReviewScreen 的人工整包采纳门禁；把 Host 已验证的联合方案放入未执行推荐缓存，不提前写入 Drunk、red herring、bluffs 或将来信息（只有实际步骤确认后才写入）。
- 三模式同一个逐项提交与信息公布流程；对自动模式特别检验无需“采纳”按钮和对话锚点不得由未执行推荐错误推进。
- 沿用现有合法性/时序/真实条件防护；迁移实际使用者后删除整包 adoption 状态机和专属测试。不能把首夜一次联合规划拆成多次规划。
- 验收：AI_ASSISTED 逐步预选/覆盖，AI_AUTOMATIC 按合法当前步骤自动确认；已公布信息不可重新写入。

### A3 — 移除 Clocktower PlayerCard 第二写入源

- Session 对游戏中的真实角色/展示身份/存活/投毒/阶段/确认选择和必要能力状态提供只读 view；PlayerCard 只保留 UI 名称/视觉元素等非权威展示属性。
- 消除游戏进行中的 cards.toClocktowerGameState() 读取和由 UI 卡片反向覆盖 Session 的写入；仅允许开局初始化及必要的旧边界适配（迁移完成即移除不必要适配）。
- 逐项统一攻击、保护、处决、死亡、角色变化、夜间动作，保留 Undercover 原行为。
- 验收：真实规则、AI、恢复均由 Session 单源读取；没有第二份可写机械事实及为了维持它们的一致性而存在的永久同步。

### A4 — AI 可用结构化记录完整性

- 按真实玩家流程查全量动作/裁定/信息公布生产入口：Drunk 绑定、红鲱鱼、掩饰、投毒/保护/击杀、已展示数字/真假/角色/配对、毒/醉非真实信息、提名与逐人投票、人工覆盖及注册裁定。
- 记录实际宣布/告知的值、来源、目标与可见性；建议、确认、公布不可混为一谈。保留共用全局游标，不新造 EventStore，不依赖 UI 中文文案解析历史。
- 验收：Host 可由**当前 Session 状态+有序历史**构造后续 AI 输入，真实历史没有明显缺口；允许当前只读快照，不要求从任意历史游标完全还原一个 GameState。

### A5 — 恢复瘦身和冗余代码删除

- 基于当前 Session 的状态、结构化历史、最小安全进度，简化 Recovery 数据布局和恢复验证。中断时放弃未确认输入，落到当前/前一个安全主持步骤。
- 审计 SdeHistoricalReplayInput、DynamicActionReducer、RecoveryMechanics 与重复卡片投影等完整引用关系：不被规则/AI/必要 Archive 消费、且仅服务已经放弃的任意历史重建或严格 UI 重放的，连同孤儿测试和配置删除；仍具有真实信息判定/规则价值的纯函数保留。
- 不无限扩大恢复范围，不追求恢复暂态对话推理、UI 页索引或动画；失去模型会话时以正式事实 + 简短可复核战略重建。
- 验收：强制终止→重启→正确魔典/已公布信息→从安全步骤继续；不出现双事实来源。

### A6 — 真实验收与最终清理

- AGENTS evidence-first：选少量稳定高价值的 Session 提交、序列化、缺失醉酒信息保护测试；以当前源码审计、现有 CI/R2 及 Android Studio/ADB 真实 8 人 TB 从首夜到白天/次夜的 MANUAL/AI_ASSISTED/AI_AUTOMATIC 行为为主要证据。
- 检查 FIELD-1/2、人工覆盖、红鲱鱼、掩饰、醉酒线索、真实提名/投票和恢复。没有用户另行批准，不做付费模型调用；可使用固定 Host 合法建议做 UI 验收。
- 每批报告：新增/删除的生产入口和状态清单、删掉的重复实现、仍在的债务、精确 HEAD、CI/R2。最终回查不存在第二套 Clocktower 权威写入；Doc/CI GREEN 不能冒充设备验收。
- PR #315 始终 Draft 且不合并 main，直到真正完成并独立复核或用户明确改变要求。不得重开旧堆叠 PR。

## 4. 执行治理、禁止事项、退出条件

- 按 A1→A6 顺序；可为防止短期破坏保留有限迁移桥，但每批明确截止删除条件，不长期维持双轨。任何新增持久字段都必须回答“为什么不能从 Session 和正式记录获得？”。
- 不构建任意时刻完整历史重建、Undo/Redo、老存档兼容层、第二套 Game Object、通用 EventStore、先 Drunk 后联合规划、旧评分/一致性推荐、模型端合法候选菜单或整包采纳中转。
- A = YES 的必要条件：单源真实状态；正式逐步确认写入；真实公布线索全部可类型化记录；AI 基于当前状态+真实有序历史；恢复能安全继续；冗余旧路径已经实际删除；规则/模式与真实设备验收有证据。
- B（模型外发单项语义/无候选）与 C（废弃的评分/Shadow 推荐）此前已达源码路径结论，但仍需最终真实行为核查；不要把历史证据混写成新 HEAD 已验证。

## 5. 资料权威与同步关系

- 根目录 AGENTS.md：长期工程原则与流程权威。
- 本文件：最新批准的 ARCH-CLEANUP-2 产品/实施/验收合同。
- ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md：历史批次、已实现源码及 A=PARTIAL 证据；旧“任意时间点完整重建才能 A=YES”验收**不再生效**。
- CURRENT_DEVELOPMENT_ROADMAP.md：工作顺序；NEXT_DEVELOPMENT_HANDOFF.md：新对话起点；TESTING_STRATEGY.md：如何验证。
- AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md：未来模型续接目标，不规定当前 Recovery 精度，也不提供独立游戏真相。TBGS/UGSM 2026-10-15 审计为时间截面证据；历史 archive 不回写。
