# ARCH-CLEANUP-2 — 单一权威 Session、逐步确认与实用恢复（2026-10-16）

> **CURRENT APPROVED IMPLEMENTATION CONTRACT / A1 首批实施中，其余尚未完成。** 此文件替代 ARCH-CLEANUP-1 中与本路线冲突的未来验收要求；ARCH-CLEANUP-1 是已完成批次与历史审计记录，不删除已发生事实。全局开发原则见根目录 AGENTS.md。
>
> 唯一活动实施分支：cleanup/arch-cleanup-1-20261015；唯一集成入口：Draft PR #315（base main，未合并）。起点代码 HEAD ec6596a56cd23981acdc3e30b2cfcf6b23820ca0，Android CI #4213 与 R2 #3829 SUCCESS，仅为起点证据。新实施必须重新确认 live HEAD / worktree / PR / CI。
>
> 优先 ARCH-CLEANUP-2 A1→A6；暂停 AI-EVENT-JIT/P2 新功能、付费模型实验和 main 合并。文档批准 ≠ 生产已实现，A 目前 PARTIAL。

## 1. 产品决策和根本原则

**只有一个始终可靠的当前游戏对象，加一份足够完整、可供 AI 理解的结构化游戏记录。**

1. ClocktowerGameSession / ClocktowerSessionState 的 GameState 是唯一当前机械事实权威；阶段、步骤及已确认设置也必须在同一 Session 权威下收敛。UI PlayerCard/Compose、GameSnapshot/TBGS、历史导出、AI 战略、Recovery/Archive 都只是临时状态或只读投影，不能反向作为第二个真实来源。Undercover 的独立游戏机制不受此路线影响。
2. ActionFactTimeline + EpistemicObservationLog 提供 Host 内部事实和玩家实际获得的信息；**唯一对 AI/最终玩家复盘有用的游戏记录**是从已确认事实提炼的最小日/夜记录。AI 连续会话通常只获取自上次接受的回复后新增的记录，会话丢失时使用完整精简记录；复盘读取完整精简记录。绝不暴露全量底层事件、技术序号、确认来源或多套日志。记录最终结果，不标记 AI 推荐或人工覆盖；同一实际数字/真假信息只记一次。已确认的 Drunk 选定必须作为第一夜游戏事件呈现，不能把 AI 推荐座位当成事实；可以从已确认 Session 角色生成事件，不必另外持久化第二份酒鬼身份。真人说书人录入的「某玩家公开声称自己是 XXX」是正式游戏记录，**声明不等于身份真相**。无须建立第三套 EventStore。
3. **迁移必须是替代与删除，不是叠加兼容：每引入新的统一接口，先列出所替代的旧接口、状态、调用路径、测试和恢复字段；迁移并验证后立即删除旧实现，不保留长期双轨。** 删除前确认所有实际生产消费者；禁止只新增同步层和长期 parity 校验来维持两份真相。
4. 一个正式 Host 决定以**该步骤确认**为提交边界：UI 上一般是“下一步”，但正式的处决、投毒目标、投票、信息公布可以有自身确认/公布动作。合法性验证→Session 当前状态+结构化事实一次性提交→刷新只读 UI；幂等防重复。未确认草稿和未采用的 AI 方案不得修改正式事实。
5. **实际公布的信息就是应记录的信息**：醉酒、中毒、虚假结果、成对结果等须保存玩家真实看到的结构化语义、接收者、相关日/夜与必要的能力有效性/来源；不可用正确规则值或界面文案替代。**不要求精确时钟、毒效起止事件或无意义的子步骤顺序**；但 Host 必须知道某次信息公布时能力是否受毒/醉影响，避免把失真结果解释为真值。公开/私密边界保留，已告知玩家的信息不得静默改写。
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

### A1 首批实施记录（2026-10-16；持续收敛，不代表 A1 完成）

- 起点真实本地/remote HEAD 为 `470b5dbe4a48d4788646cc7d2313e6131ed2470a`，worktree clean；`main=770ed32f446d03cf81f226901afa78cbdcf287bf`，Draft #315 仍由 GitHub 报告 `mergeable=false / dirty`。比较结果确认 main 额外修改 `AGENTS.md` 和 `docs/TESTING_STRATEGY.md`，不能将本地 clean 或旧文档里的 clean 声明当作 PR 冲突解除证明；不在 A1 阶段合并 main。
- 已审计 `ClocktowerGameSession` 的 revision / action / observation 写入口、Compose 的投毒确认、死亡/处决、身份变化、夜间/破晓 materialization、信息公布与 `PlayerCard` 衍生路径。初批选择正式投毒目标确认：原 `recordClocktowerAction(Poison)` 与 `commitPoisonTargetBoundary` 两次 Session 写入存在半提交窗口。
- 初批将 Host `NightCheckpointHostTransaction.confirmPoison` 审核后的提交改为 `commitConfirmedPoisonTarget` → `ClocktowerGameSession.commitConfirmedGameAction`，先无副作用地检查全局动作及预期 revision，再一次 `updateState` 更新机械投毒状态、`ActionFactTimeline`、`gameStateRevision`、`nextTimelineGlobalSequence`；同一 action ID 不重复提交，冲突 ID/过时 revision 拒绝且保持状态不变。保留 Klutz 死亡前状态取证共用准备逻辑。
- **替代且删除** `commitPoisonTargetBoundary` 的生产实现及全部引用；旧测试迁到确认行为、幂等、拒绝、游标的确定性证据，仍被夜间/破晓调用的同步路径暂保留，不能当已完成冗余清理。
- **A1 仍 PARTIAL**：`advanceGameStateRevision`、`commitGameStateBoundary`、`synchronizeGameStateWithinCurrentRevision`、`commitGlobalActionFact`、`commitGlobalEpistemicObservation` 及其真实夜晚/死亡/处决/身份/信息公布使用者仍须逐项收敛并删除失去消费者的旧入口。A2–A6 未开始。以该实现提交的准确 HEAD 独立 CI/R2 为准，不拿起点 GREEN 代替。

### A1 第二批：死亡／处决确认收敛（2026-10-16；待新 HEAD CI/R2）

- 从此前确认的精确分支 HEAD `550ed797044d1864f45068566e45ae960dbe464d` 继续实施，不合并 main。生产入口逐项迁移：Slayer 命中死亡、Virgin 自动处决、白天正式处决、夜间破晓已裁决死亡；保留 Slayer 消耗、Nomination、Klutz learn-time 以及已有 Host 法律和行动规则。
- `ClocktowerGameSession.commitConfirmedGameAction` 在无副作用的纯事实预检后，允许同时预检一个实际公开 `EpistemicObservationDraft`；一次 Session `updateState` 写入 `GameState`、`gameStateRevision`、行动记录、观察记录、`playerInputRevision` 与统一 global sequence。重复相同动作和同一观察不新增记录；冲突/过时请求不能半写入。
- 新的窄边界 `commitConfirmedPlayerDeath` 在 **变更 alive/poisoned 前** 由 Session 捕获 Klutz 死亡证据；死亡和处决均由该真实规则确认入口持久化，UI `PlayerCard` 仅反映已经确认死亡的结果。公开存活状态 `AliveAt(seat,false)` 对应的 typed observation 与 action 同事务提交，并继续在 Session 成功后向 `A4ObservationDurabilityGate` 提交 pending 记录，保证保存后观察缓存可重新构建（明确已有观察记录的无变更恢复分支暂保留）。
- **迁移并删除** `synchronizePlayerDeathWithinCurrentRevision`（所有生产和测试消费者均已迁移）及 `addClocktowerEvent` 通过 PlayerCard、标题/事件类型反向生成死亡行动事实和公开观察的旧逻辑，移除其 `projectSemanticHistory` 形参。删除 Slayer 命中、Virgin 处决、正式白天处决中被 Session 单次提交替代的独立 revision 前置推进；无处决/未命中的独立步骤继续存在。
- 保留现役静态规则、Klutz/Spy 因果取证、真实 NGJ 流程与必要归档合同测试；死亡测试迁到一次确认、事实+公开观察 chronology、重复安全、被拒绝状态不变以及 Klutz 中毒前证据。Dawn planner 原来可补写历史的独立状态/事实标志尚有角色变化和 Recovery 消费，留待 A1 后续和 A5 精简，不声称已消除全局恢复双轨。
- **A1 仍 PARTIAL**：真实角色变更、Dawn 毒延续、阶段推进、无处决/票数以及各能力展示的事实和 revision 写入仍分散；UI/PlayerCard 第二权威属 A3，未开始 A2。不拿旧 HEAD CI/R2 当新批验收证据。

### A1 第三批：正式角色改变与旧机械边界删除（2026-10-16，待精确 HEAD 验证）

- 实际起点：`d6742aca56e164d9459be7d9a67591a3f45e883d`（本地/远端一致，clean）；Draft #315 仍 `mergeable=false / dirty`；尚无上一批新 HEAD 的 CI/R2 证明。
- 真正消费链：`promoteScarletWomanIfNeeded` → `setClocktowerActualRole` 以及 `NightDawnResolutionPlanner.confirmNewDemonIdentity` → `NightDawnDurableMaterializationPlanner.roleChanges` → `setClocktowerActualRole`。原链是 `recordClocktowerRoleChangeAction` 独立提交事实、`commitActualRoleBoundary` 独立改变机械状态与 revision，产生半提交窗口。
- 迁移成 `commitConfirmedActualRoleChange(ActionFactDraft.RoleChange, expectedGameStateRevision)` → 已有 `ClocktowerGameSession.commitConfirmedGameAction`；一次 Session 状态跃迁提交真实角色、阵营、类型、`ActionFactTimeline`、`gameStateRevision` 和 global sequence。校验未知座位、已无变化的角色、过期 revision；相同已确认 action ID 幂等。保留其他座位、alive/poisoned、shownRole 原样；UI PlayerCard 在确认后才投影更新。
- 破晓角色变化改由单个 action ID 执行；`NightDawnDurableMaterializationPlan` 如果出现状态/历史仅一侧已更新的旧式半提交恢复情形，现在明确拒绝，不继续生成第三份修复事实；该 planner 其他毒药/阶段/死亡恢复消费仍须在 A1/A5 继续收敛。
- **已删除** `recordClocktowerRoleChangeAction`、`commitActualRoleBoundary`、`recordSemanticHistory=false` 双轨参数；同时经全量调用搜索证实 `commitShownRoleBoundary` 只有自身测试、`commitGameStateBoundary` 只有前者与自测消费，已直接删除两者和 `ClocktowerGameSessionGameStateBoundaryTest`（纯旧 API 形状测试）。真正角色变更幂等、原子性、状态保持及拒绝测试已替换旧角色测试；不删除合法性检查或 TBGS/EvidenceLab 读接口。
- **A1 仍 PARTIAL**：破晓毒药 carry、phase advance、攻击、投票/无处决、实际信息公布仍有分散事实/状态/版本链。现场编译/CI/R2、玩家操作、恢复还待精确提交验收；A2–A6 未提前执行。

### A1 第四批：黄昏清毒与破晓投毒延续（2026-10-16；待新 HEAD 验证）

- 起点已核对：`094c3337a0553b4af3c19820f32beef14c453781`，开发机 clean 且与 origin 一致；Draft PR #315 仍 dirty，无精确 HEAD CI/R2 证明。
- 原三条真实生产链：`materializeClocktowerPoisonExpiryAtDusk`、恶魔继承后的 `NightDawnDurableMaterializationPlanner.poison`、普通 `DawnCommitIntent.poisonCarry`，都可能先 `recordClocktowerAction(Poison)`、再 `synchronizePoisonTargetWithinCurrentRevision`。现在同样与首夜确认共用 `commitConfirmedPoisonTransition` → Session `commitConfirmedGameAction`，在一个受 expected revision 保护的 Session 转移中存储投毒机械状态、行动事实、统一序号和 revision。
- 黄昏仅允许 `StorytellerPhase.DAY + targetSeat=null` 清除，夜间仍允许确认/延续；当受毒者已经死亡，机械旗标已清，仍可记录真正发生的黄昏失效行动（不可误删历史），且幂等重试不重复写入。已接受的完整稳定 ID 不重复执行；旧时态/历史只一侧已提交的情况显式拒绝，而非另开一个补丁式同步写入口。
- **删除/替代** `commitConfirmedPoisonTarget` 旧独立名称、`synchronizePoisonTargetWithinCurrentRevision`、无其余消费者的 `synchronizeGameStateWithinCurrentRevision` 与仅检查同步辅助函数的测试。新高价值测试覆盖夜间选择、破晓延续、黄昏在死亡后机械空变化仍写入真实失效事实、全局顺序、幂等与冲突拒绝。
- 保留现有 Dusk/Dawn 纯物化规划的稳定事件身份与当前消费，但 `stateMutationRequired` 与 `actionIdToCommit` 不能再被当作允许独立写状态或补写历史的许可证。其过去的 state-first/history-first 测试数据可用于定位旧半持久化风险，未来 A5 在移除旧式 Recovery repair 逻辑时再压缩。
- **A1 仍 PARTIAL**：PhaseAdvance、叙事 UI 事件产生的额外 revision、其他行动、信息公布及投票/无处决等生产接口仍需迁移；A3 的 PlayerCard 第二事实来源与 A5 恢复简化尚未开始。禁止将此前旧 green 冒充本批运行证据。

### A1 第五批：阶段确认与叙事事件隔离（2026-10-16；待新 HEAD CI/R2）

- 起点确认为 `20e5a99dfcf64caa37519e235c6faeb9c2a424a3`、工作树 clean 且与远端一致；PR #315 保持 Draft/dirty，对当前实现尚无精确 Android CI/R2 实证。
- 旧 `recordClocktowerPhaseAdvance` 仅写 `ActionFact.PhaseAdvance`，调用者有时另调 `advanceClocktowerGameStateRevision`；普通/继承破晓仍分别写历史、更新 Compose phase 和 advance revision，存在事实与版本的间隙。已替换为 Session `commitConfirmedPhaseAdvance` → `commitConfirmedGameAction`，把结构化 PhaseAdvance、globalSequence 与机械版本作为**一个接受步骤**保存。检查合法阶段相邻、轮次增进、历史最后确认阶段以及过期请求与重复 ID。
- `CampBoardGameHostApp.commitClocktowerPhaseAdvance` 为唯一 Host 阶段入口：首夜→白天、白天→下一夜、Klutz 临时白天→破晓、夜晚→破晓（包括恶魔继承后的稳定 action ID）和 NGJ Klutz 白天流程均迁入。删除所有 `recordClocktowerPhaseAdvance` 调用和在首夜→白天、Klutz 返回、继承破晓处被此单次提交替代的单独 revision，拒绝 Dawn 事实和已更新 UI 阶段一侧缺失的历史修复分支。
- **叙事写入不再隐式修改 Session：** `addClocktowerEvent` 仅追加 UI 文案事件和事件显示序号，不再自动 `advanceClocktowerGameStateRevision`。已确认角色变化、死亡、处决和胜负结果的文字展示不会再重复推进机械版本。当前未完成 typed A1/A4 迁移的 `ClocktowerHostScreen.onRecordEvent`（登记、投票、信息历史等）在实际回调点明确保留版本变更；Artist 与 Klutz Choice 的确认入口也显式保留必要的版本推进，等待对应正式逐步提交路径收敛。
- 新增 Session 阶段有序提交测试，覆盖 FIRST_NIGHT→DAY→NIGHT→DAWN、黄昏 Poison 失效后同回调立即转 Night、重复 ID、非法越级/跳轮、历史阶段不匹配和过时版本。**发现并修正复合回调的 stale revision 风险**：Compose 的 `clocktowerGameStateRevision` 是回调开始的快照；Dusk 清毒后转 Night、死亡后 Dawn 继续物化、继承角色后 poison 等连续已确认动作必须使用各自提交时的实时 `ClocktowerGameSession.state.gameStateRevision`，不能继续把旧快照传给下一次提交。Host 的实际步骤合法性与 Action ID 幂等仍不变。阶段进程证据是已有的 `ActionFact.PhaseAdvance`，尚未新增第二权威阶段状态；UI 层 `clocktowerPhase/round` 仍待 A3/恢复阶段完全收敛。
- **A1 仍 PARTIAL**：Night 结算前置 revision、部分 UI callback 的 version/typed fact 分离、NoExecution/Slayer/Nomination/Vote 等行动、实际信息公布的原子性未完成；主线继续 A1，暂不开始 A2–A6。Android 构建和设备观察没有运行证据，不得声称验收通过。

### A1 第六批：行动确认事实与版本同一提交（2026-10-16；待新 HEAD 独立验证）

- 起点经 Mini MCP 与 GitHub 对证：`05b292ebabde8b1dbe940f67ed8604e39a64a152`，本地 clean、远端相同，Draft PR #315 对 `main` 仍 `dirty` 且没有该新 HEAD 的 CI/R2。
- 真实生产引用审计：`recordClocktowerAction` 是 Attack、Protect、SlayerShot、Nomination、Vote、NoExecution 和 NGJ Klutz Learned/Choice 的共同事实写入口，原本调用 `session.commitGlobalActionFact` **只追加事实**，而真正确认回调在另处 `advanceClocktowerGameStateRevision`；因此语义记录与接受步骤可能分离。
- 将旧入口**直接替换并删除旧名字**：`commitClocktowerConfirmedAction` 统一通过现有 `ClocktowerGameSession.commitConfirmedGameAction(draft, session.state.gameState, session.state.gameStateRevision)`，一次 Session 转移写入确认的行动事实、全局序号和 gameStateRevision。只有已被 Host 正式接受的行动进入该函数；Attack/Protect 的确认目标仍由现役 `NightCheckpointHostTransaction` 合法检查，Vote 仍使用真实逐席投票、ghost 子集及 Virgin 首次提名标志，SlayerShot 保留命中/未命中和能力消耗语义。没有新增攻击/保护专属并行状态。
- 删除 Attack/Protect **有目标且 Global 模式**后的第二次版本推进；无目标或非 Global 模式仍沿用明确的 revision 失效通知。SlayerShot 未命中不再把版本推进交给额外 callback；命中后公开死亡仍是另一个独立实际事件，一次死亡加公开 alive 观察的原子提交。NGJ Klutz choice 此前由“事实+手动 revision”组合，已删除重复 revision。
- `ClocktowerHostScreen.onRecordEvent` 仅当 Nomination/Vote 已由 Global 正式行动提交时，不再为了同一条叙事文字增加第二次 revision；Spy/Recluse 登记、私有线索公布及未迁移回调暂继续显式版本通知，留待后续 A1/A4。Virgin 未执行且 Global 已确认提名时，不再推进多余版本。NoExecution 现在在 `onConfirmDay` 通过确认的结构化事实一并推进 revision，非 Global 或无真实动作的旧行为有受控回退；保持 Mayor 胜负检查之前完成明确的无处决记录。
- 重要并发顺序：SlayerShot → 命中 Death、Nomination → Virgin Execution、Vote/其他确认 → Day Execution 可能在同一 Compose 回调发生。后续死亡使用实时 `ClocktowerGameSession.state.gameStateRevision`，而非回调开始捕获的旧 `clocktowerGameStateRevision`，以免被错误判断为 stale。
- 新增 `ClocktowerGameSessionConfirmedActionBoundaryTest`：Attack/Protect 共同保护与攻击、Nomination/Vote/Slayer miss/NoExecution 的版本及事实精确一致，重复 ID 幂等、冲突/过期拒绝。**源码与测试已修改，但未执行 Android/JUnit/CI/R2**，不声称运行验收。现存 `ClocktowerGameSession.commitGlobalActionFact` 的 instance 方法已无生产消费者，但仍被多个历史/恢复/战略种子测试调用；待 A1 完成后按真实测试保留价值清理，避免误删造成测试失编。
- **A1 仍 PARTIAL**：夜间结算前置 revision、部分登记/信息公布回调、PlayerCard 可写状态、阶段进度的 UI 投影、Recovery 历史双轨等仍需后续步骤。继续 A1，不提前 A2、不合并 main、不进行付费模型调用。

### A1 第七批：夜晚结算 revision 与已公布信息的时间线所有权（2026-10-16；待独立验收）

- 本批起点 `d83655f17c1c462a21c3c09d5776b7cd02acad60`，本地/远端一致且工作树 clean；PR #315 仍 Draft/dirty，未合并 main。
- **删去 `onConfirmNight` 入口无条件的 `advanceClocktowerGameStateRevision()`。** 夜晚结算中确认死亡、角色变化、毒药 carry、PhaseAdvance 已经各自通过 Session 事务拥有事实+revision；在 callback 入口先记实时 Session revision，完成结算后，仅当此次没有任何 Session 版本变更时才进行一次兜底失效，覆盖没有死亡/阶段结果、等待进一步交互等安全场景。不得把“每段 UI 回调”误作一个独立 Host 行动而提前推进版本。
- **实际信息公布与 UI 文案分工：** `ClocktowerHostScreen` 的 `Information`/`UnreliableInformation` narrative event 不再额外推进机械 gameStateRevision；玩家实际收到的 `EpistemicObservationDraft` 通过 `commitGlobalEpistemicObservation` 写入观察记录、同一全局序号及 playerInputRevision。Nomination/Vote 的现有免重复 revision 继续有效；其他尚未迁移的注册/角色动作仍在原确认回调保留失效通知。
- 修复 `recordEpistemicObservation` 在同一 Compose 回调连续公布/重试时读取旧捕获 `clocktowerPlayerInputRevision` 的判重问题：改在提交前直接读取当前 `session.state.playerInputRevision`，只给真正新增的公布记录触发 pending 及后续依赖通知。与 `commitConfirmedInformationDecision` 的实时 Session 判重保持一致。
- 增加针对性 Session 合同测试：已实际展示的不可靠 Chef 数字（示例 0）以 **KNOWN_MALFUNCTIONING + 私有接收席位 + 精确 NumericResult** 保存；只增加 playerInputRevision/globalSequence、不伪造 gameStateRevision；同 ID 重复不更新；下一合法确认 Attack 使用连续的全局顺序。这是写入所有权验证，不代表 UI 的所有 Drunk/Poisoned 情况已被完整采集。
- **仍待 A1/A4 消除的真实缺口：** `ClocktowerHostScreen.recordReliablePrivateInformation` 当前对缺少 `displayProposition` 的不可靠展示会直接返回，无法证明记录了真正展示内容；`performClocktowerPlayerRevealHandoff` 依次 first-night publication → observation → narrative → reveal，而不是整体事务，存在中途失败的状态差异；`ClocktowerConfirmedRegistrationHostWriterV1` 的 journal 写入与 observation Session 写入也尚分离。下一批必须沿**实际显示的 proposition/接收者/可靠性**修正，不能用本应显示的真值回填或把日志当权威观察事实。
- **独立验证阻塞（真实检查）**：使用 Mini MCP `execution_session_start` 只读检查精确起点 HEAD：Java 21 / Gradle 9.5.0 存在，`:app:testFast` 已配置；开发机找不到 Android SDK 35，且 `ANDROID_HOME`、`ANDROID_SDK_ROOT` 均未设置。未执行 Gradle/JUnit/Android 编译；Mini MCP `run_task(:app:testFast)` 也因仓库未列入 task allow-list 被拒。没有源码改动由 Codex 产生。必须经能用 SDK 的构建机或 GitHub 精确 HEAD CI/R2 执行才可称 GREEN。
- **A1 仍 PARTIAL，A2–A6 未开始。** 不新增通用 EventStore、平行状态或兼容桥；PR #315 保持 Draft，尚有 main 合并冲突，禁止未经许可付费模型测试或合并 main。

### A1 第八批：实际展示信息与首夜观察先提交（2026-10-16；等待 Android 验证）

- 起始仓库经 Mini MCP 检查为 `796b6d38263d4b032bc16c1de1f00b0e4d588f5c`，本地工作树 clean 且等于远端，PR #315 仍 Draft/dirty/无当前 HEAD 检查。
- **不可靠信息不得被漏记或用真值补写：** 新增 `clocktowerShownUnreliableProposition`，优先使用确定的 `ConfirmedInformationDecision.draft.proposition` 或当前被选中的 `displayProposition`；若遗留数字展示无命题，只在可从**玩家实际看到的数字**加上既有显式角色/查询席位准确确定语义时解析为 NumericResult（Chef、Empath、Clockmaker、Chambermaid）；Fortune Teller 的 YesNo 仅在已确认两名查询席位和屏幕显示的“有/没有、DEMON/NO DEMON、Yes/No”等确定字面值可识别时补全 BooleanResult。已有 NumericResult/BooleanResult 与屏幕值冲突时均拒绝，而非假装所见等于真值。角色/配对/魔典信息不尝试解析本地化文字；必须携带确定的结构化命题。
- `ClocktowerHostScreen.onShowPlayerDisplay` 在首夜展示标记/观察写入之前加 `unreliableInformationCanBeRecorded` 前置检查。对需要私有语义观察的不可靠 Number/YesNo/EitherOne/RoleReveal/Grimoire，缺少合法 typed evidence 时拒绝打开展示并提示重新选择；`recordReliablePrivateInformation` 不再对缺少 proposition 的醉酒/中毒结果静默 return，使用真正展示数值写入 `EpistemicObservationDraft`，可靠性仍标为 `KNOWN_MALFUNCTIONING`，且不使用原本的真实信息 fallback。
- **删除旧“首夜显示标记先提交，观察后提交”的顺序。** `performClocktowerPlayerRevealHandoff` 同一现役入口改为纯检查是否首次公布 → 持久化实际私有观察及已确认登记 → 写入临时 first-night displayed marker → 可读的叙事事件 → 玩家展示页面。若观察或登记失败，不会提前消耗已展示标记；重试依靠 Session 已有观察 recordId 幂等。保留同一决定再次打开展示、不重复公布的语义，替换旧的 `publishFirstNight` 回调，不增加第二条写入链。
- 针对旧 handoff 合同测试更新阶段顺序、拒绝/异常传播和重复公布，并新增“首次观察失败不消耗标记、下一次成功”的覆盖；新增纯语义测试确认 Chef=0（虽可不是实情）、Empath 数字/查询邻座、Chambermaid 查询席位、Fortune Teller 明确显示的 Yes/No 和真假冲突、旧角色/配对无结构化命题时不许伪造、真值数字与屏幕不同必须拒绝。尚未取得 Android 编译或 JVM 测试执行证据。
- **仍有真实边界**：当前 `FirstNightInformationMigration` 是易失的 UI displayed-ID gate，而非 Session 单步事务；`ClocktowerConfirmedRegistrationHostWriterV1` 仍追加 Session-owned journal 但与观察不是同一个事务；历史 `recordHistory` 仍是显示投影；未列举的可靠/NGJ 非数值信息的缺命题情况仍需审计。此次改变的是失败发生时的写入顺序和失效保护，**不宣称整个观察+裁定+UI 展示已实现数据库级原子性**。A1 仍 PARTIAL、PR 不合并，A2–A6 未开始。
- Android SDK 35 在开发机缺失，`:app:testFast` 不是 Mini MCP allowlisted task；后续必须由 SDK 可用的安卓构建机或精确 HEAD GitHub CI/R2 证明编译和实际流程。

### A1 第九批：首次观察 + 登记裁定的 Session 确认及角色信息命题（2026-10-16；待独立编译验收）

- 本批从经 Mini MCP 核实的 `e235ac75818b66b7cde5ccc43b39420adeea0421` 开始，开发机工作树 clean、origin 相同；PR #315 Draft，对 main 仍有冲突、没有当前源码的 Android CI/R2。
- **正式首次信息公布单一写入入口**：原 `ClocktowerHostScreen` 同时持有 `onRecordEpistemicObservation` 和 `onCommitConfirmedInformationDecision`，再另调用 `onCommitConfirmedRegistrationResult` 写特殊登记；现用统一的 `onCommitConfirmedPrivateInformationPublication(draft, publication?)`。删除原两个 UI 参数、旧 `commitConfirmedInformationDecision` 生产函数和原先“先写观察、后另行提交登记”的首次公布调用链。已公布后再次主动修改特殊登记仍是合法的**独立纠正动作**，继续使用现有 Host writer。
- **Session 接受前整体预演**：新增 `ClocktowerGameSession.commitConfirmedPrivateInformationPublication`，对实际观察先进行不变式 preflight，检查登记引用的观察 ID、来源、内容、私有及能力可靠性；在仅用于检验的临时 Session 状态及从现有因果记录恢复的临时 journal 上调用**既有** `ClocktowerConfirmedRegistrationHostWriterV1` 的完整合法候选/witness 与规则检查。只有完整成功后，才一次性更新正式 Session 观察、playerInputRevision、全局游标和 Session-owned journal；任何登记裁定冲突都不会留下半条正式观察或不完整裁定。未引入持续存在的并行 GameState、第二 EventStore 或新的注册裁定算法。
- **角色信息按玩家实际看到的身份写入**：`ClocktowerPlayerDisplayResolution.withDisplayOption` 对已明确选中的角色揭示，使用该候选元数据 `presentationRoleId` 和目标座位构造 `RoleAt`；座位与之前锚点冲突时拒绝，不把原本真实身份偷偷写成展示身份。更新 Ravenkeeper/Undertaker 选项投影与送葬者展示结果：保留已选 `RoleId`，不解析本地化角色文本，不把空命题视为已记录。
- **贤者的两座位线索**：候选/直接展示现在附带 `AnyOf(RoleAt(seatA, 当前恶魔角色 ID), RoleAt(seatB, 同角色 ID))` 的准确玩家所见命题；可靠与不可靠的不同配对按同一真实展示语义记录，不以“是否真实”替代“告诉玩家什么”。
- 合同测试：新增 Session 首次观察+两项登记合法确认、重复幂等、非法完整 witness 导致全部拒绝；角色选项显示身份覆盖真实锚点、冲突座位失败；修正贤者原来要求“不许有结构化命题”的过时测试，并采用真正的 Imp 测试阵容。
- **纠正真实 APP 与测试快照之间的集成差异**：TB runtime projector 的 `position.playerInputRevision` 是公布前版本，且 `grimoireSeats` 使用注册表合法的 external role ID（如 `chef`、`fortuneteller`）；Session 在临时裁定预演中生成*同一观察提交后 revision* 的快照，不改变原外部角色 ID；既有 `StorytellerDecisionPrefixCaptureV1` 的角色一致性校验现支持该 external ID 与 Session `RoleId`（如 `Chef`、`Fortune Teller`）的大小写/分隔符规范等价，同时仍拒绝不同角色。新测试从真实 external-ID 格式的公布前快照出发验证成功、重复与拒绝，不能依赖仅使用内部命名的夹具通过。
- **独立只读审阅后修正**：对 `cc1ee7d1135760bb09b02e8fc47352b2d971c2fb` 的源码审阅发现，`StorytellerRegistrationRulingProducerV1.validateCommitted` 对注册主体仅匹配 `"Spy"`/`"Recluse"`，而合法 TB 快照是 `"spy"`/`"recluse"`；原先只有 `UNRESOLVED` 测试没有触发。已在注册事实验证阶段使用与 frozen role 校验一致的正规化 ID 判定，并加入 **Spy 和 Recluse 同时显式特殊登记、外部 ID、重复提交无改写** 的实义回归。只读审阅未发现确定 Kotlin 编译错误，但无 SDK 仍不能声称编译通过。
- **边界/剩余工作**：事务是一次同步 Session 接受操作，不是磁盘持久化原子事务；未被选为结构化 role/pair 的遗留无元数据展示仍应 fail-closed；NGJ 其他非数字线索/公开信息、旧 callback 的独立 revision、完整 Recovery 双轨尚待最后 A1/A4 审计。原 `ClocktowerConfirmedRegistrationHostWriterV1` 仍服务于**已经公布后的显式更正**，不得盲删。尚未运行 Android 构建/JUnit：开发机无 SDK35，GitHub 新 HEAD CI/R2 未提供独立证据。A1 仍 PARTIAL，A2–A6 未开始；不合并 main、不执行付费模型请求。

### A1 第十批：逐入口版本所有权审计、叙事去重与死分支清理（2026-10-16；仍待构建）

- 起点经核对的本地/远端共同 clean HEAD `f1d2c471a444fcad2224564ee642a530b1b59a5e`；PR #315 Draft、与 `main` 冲突、无当前 HEAD CI/R2。此批专注 A1，不进入 A2/A3/A5。
- **只对确定有权威写入的 UI 叙事去重**：`ClocktowerJudgeScreen.onRecordEvent` 的现有同一个回调增加显式 `confirmedRoleNarrationOnly` 参数，配合 `clocktowerNarrationNeedsGameRevision`。Global 情况下，提名/投票早已有 Session fact，信息早已有 observation，不应靠另记 UI 文案再次推进机械版本；Spy/Recluse UI 登记文字纯展示也不增加机械版本。夜晚 `recordNightStep` 只有在 RedHerring / Poison / MonkProtect 这几类已经有现役独立 Host Session 提交的动作才标为文字投影。**ButlerMaster 等尚未迁移的确认，继续推进原来的 revision**，防止把“无结构化 fact”的旧操作静默消掉。LEGACY_LOCAL 的 RoleAction/提名/投票仍维持旧失效语义。
- **删除艺术家问答的重复版本**：同一次 `onConfirmArtistQuestion` 原先在明确确认答复后以及 UI 回到总览时连续调用两次 `advanceClocktowerGameStateRevision()`；第二次没有新确认事实，直接删除。第一次暂留作为唯一已确认日间决定的版本通知；其真值/展示值目前只有文字事件，尚缺准确结构化的完整问答，应在 A4 迁移，不得把无问题文本的 Yes/No 硬套 Fortune Teller 的 BooleanResult。
- **移除不可能的 Host revision 分支**：`NightCheckpointHostTransaction.movePrevious` 在当前 reducer 永远返回 `NONE`，上一夜步骤导航只更新 index，不再无意义地提供 GAME_STATE/PLAYER_INPUT 分流；`editDemonSuccessor` 永远属于 PLAYER_INPUT，正式选择仍按原路径单独确认，删除其编辑回调中的不可能 GAME_STATE 分支。这两处不删除正确的真实输入版本推进。
- 逐点审计 `advanceClocktowerGameStateRevision` 余项：空攻击、空保护、非法/空处决、无 typed night-close 仍属明确兜底；Slayer miss、Virgin/NoExecution 在 Legacy 模式仍需旧回调；**真实尚未收敛**的是 Mayor redirect（已有 causal journal 与单独机械 revision）、DemonSuccessor confirmed draft（先玩家选择、后确定接班）及 Artist question（未有 typed fact）等；它们不能凭删 revision 模拟已经完成 Session 事务。夜晚回退及后继者草稿中两条不可达 GAME_STATE 分支已清理。
- `ClocktowerGameSession.commitGlobalActionFact(draft)` 实例 fact-only 生产入口无正式生产消费者，但仍被多个真实历史/恢复合同测试直接使用。删除前应把这些测试迁到具有明确 Session 接受语义的测试 fixture/新接口（并保留因果/历史合同），不能在缺 Android 构建时批量把测试改成会自动推进游戏版本的不同语义。静态 companion 函数仍是 `commitConfirmedGameAction` 等 Session 事务内部的纯转换器，**不是同一类废弃接口**。
- 增加定向策略测试覆盖 Global“已有接受事实只记一次”、未迁移 RoleAction 仍有 revision 及 Legacy 语义；保留原 NightCheckpointHostTransaction 行为测试。开发机无 Android SDK 35，且 `:app:testFast` 不在 Mini MCP 允许列表，尚无实际 Android/JUnit 编译/测试结果。A1 仍 PARTIAL；接下来应先取得具 SDK 的独立编译证据并针对现有九/十批累计改动修复，再决定是否转入 A1 余下真实决策提交。不得提前宣称 A1 完成。

### A1 GitHub CI 全量编译/测试验收证据（2026-10-16）

- PR #315 为 Draft 且存在对 main 的 merge conflict；已有 `pull_request` CI 因而未产生新的执行。当前 `.github/workflows/ci.yml` 与 `r2-write-probe.yml` 对 `cleanup/**` 分支启用 push 检查，不需合并 main 或调用付费 API。
- [CI #4214](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38852123720) 运行于 `322be05b700a1427ca67baa9f43ed5d8137deb80`：ASP、Clingo、R2 成功，Android `compileDebugKotlin` 失败：`ClocktowerHostScreen.kt` 第 922、942 行在局部变量 `fortuneTellerSubjectSeats` 声明前引用。已把**原有唯一的座位列表声明**提前至实际公布回调之前，不另造数据源。
- 经修复的准确**可执行代码**提交 `16f2413f33e2151e5676298162aae850bae52b10`：[CI #4215](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38852526023) Android `assembleDebug`、`testDebugUnitTest`、`testFull` 及 CI gate 全部 SUCCESS（`BUILD SUCCESSFUL`），ASP 合同和 Real Clingo 也 SUCCESS；[R2](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38852526096) SUCCESS。同一 source SHA 验证前十批 A1 的累计应用改动；不把后续纯文档 SHA 伪称再次编译过，也不把 CI 当作真机交互验收。
- A1 仍 PARTIAL：市长转移、恶魔后继者、艺术家及剩余事实确认路径还需后续精简；A3/A4/A5 分别负责多份 UI 机械状态、全部信息补全和安全恢复，不扩大 A1 范围。

### A1 第十一批：市长转移正式确认收敛与真实 Android 回归（2026-10-16；本批完成）

- 开始前经 Mini MCP / GitHub 重新确认 `cleanup/arch-cleanup-1-20261015` 为本地/远端一致且 clean 的 `c8a7f530924bf9f0ba69b633ba6b2aa4b9c2d8df`，`main=770ed32f446d03cf81f226901afa78cbdcf287bf`；唯一 PR #315 仍 Draft、dirty、未合并。全部五份权威文档已读取。
- **替换原 Mayor 双轨写入口**：原 `CampBoardGameHostApp.onConfirmMayorRedirectTarget` 在已验证合法候选后，直接先写 `causalDecisionJournal.captureBeforeDecision/commit/correct`，后向 Compose `clocktowerConfirmedMayorRedirectTarget` 写入，再独立执行 `advanceClocktowerGameStateRevision()`，留下日志与 Session 事实不一致窗口。现移除该 callback 中的直接 journal 三次写入及 Trouble Brewing 的独立 mechanical revision 调用，将合法确认委托给 `ClocktowerGameSession.commitConfirmedMayorRedirect`，使用 `ActionFactDraft.MayorRedirectChoice`。
- 新路径在正式 Session 更新前校验当前版本、请求来源、规则域以及目标席位，离线暂存已有 Session-owned causal journal 的前缀、决定和必要的更正，再调用现役 `commitConfirmedGameAction` 一次写入 `MayorRedirectChoice(mayorSeat, targetSeat)`、`gameStateRevision`、`ActionFactTimeline`、`nextTimelineGlobalSequence`，成功后才替换 journal。若接受前任何检查失败，不改变权威 Session 事实/正式因果日志；重复相同 Action ID 幂等，冲突禁止。当前 UI 夜间未完成步骤的 `confirmedMayorRedirectTarget` 仍承担临时流程投影，尚不是已清理的 A3 PlayerCard/Recovery 阶段。
- 新增事实沿用**既有**行动事实、序列及存档模型，没有创建第二 GameState、并行事件仓库或替代规则算法；补齐 `ActionFactDraft` 绑定、纯状态 reducer、持久化 codec、Recovery 的合法席位检验、全局 AI 事件投影及不泄漏身份的玩家可见历史视图。特别保留既有规则域允许的 `mayorSeat == targetSeat`（市长未转移、死亡照常处理），不能凭个人推测禁止。
- 针对性的 `StorytellerCausalDecisionJournalV1Test` 验证提交后事实/版本/序号、重复确认、非法恶魔目标拒绝无半写、同夜正式更正后有效因果记录、Mayor 自身合法目标与历史 JSON 编解码一致；不机械扩增 UI 字符串测试。
- 源码批次 `dd7392cdba097509130e3641b8b7830752c8adc5` [CI #4217](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854159448) SUCCESS（Android tests、ASP、Real Clingo、CI Gate），同一 SHA [R2 #3832](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854159617) SUCCESS。随后 Mayor 同席规则和持久化回归修复的**最新可执行源码 SHA** `f010720493e3f79a5db145f3b1da4fb655276b30` [CI #4218](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/38854537664) 完成，Android tests、ASP、Real Clingo、CI Gate 均 SUCCESS。R2 的 push 路径仅监控主屏等 UI 文件，后一提交只有 domain/epistemic/persistence/test 修改，因此 **R2 在 f0107204 SHA 未单独触发**；不把前一 SHA 的 R2 假称是后一 SHA 验收。
- **A1 仍 PARTIAL，但收尾范围应收缩**：DemonSuccessor 的草稿只属 UI 输入，最终的继承结果/实际 RoleChange 只需成为一项有效的说书人历史事实；如 Host 在 Dawn 前需要已确认候选来继续规则流程，可将其作为待处理状态而非额外永久 AI 事件。审计其重复 revision 与事实写入口后删除。Artist AI 问答界面与问题文本采集尚未开发，作为**后续独立功能**，不能阻塞 A1；现有手动答复路径保留到功能替换。剩余 `advanceClocktowerGameStateRevision` 只移除无真正状态意义的调用；实例 `commitGlobalActionFact` 的测试消费者先迁移再删除。A2/A3/A4/A5/A6 均未开始，PR 保持 Draft/dirty，不合并 main、不调用付费 API。

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

- 按真实玩家流程审计各生产入口，判断是否漏掉**会影响 AI 下一次说书人裁量**的事项：Host 最终选定的 Drunk（第一夜正式事件，不信 AI 预案）、重要投毒/保护/袭击/死亡结果、最终恶魔继承、实际获得的数字/真假/角色/配对信息、白天提名投票及处决，以及说书人录入的玩家公开身份声明（标明是声称，不混同真实身份）。不区分 AI 推荐与人工覆盖，只输出实际发生的决定。Host 可保留规则运行所需的更细事实，但 AI 历史不得成为完整底层动作转储。艺术家 AI 问答另行设计。
- 记录实际宣布/告知的值、来源、对象、可见性与必要日/夜背景；**不要求精确毒效起止时间、草稿/点击/确认中间过程、所有机械性阶段过渡和不影响裁量的行动详情**。相关能力是否受毒、醉影响由 Host 规则状态与信息公布当时的有效性判定，不能丢失。内部 global cursor 用于确定性与幂等，AI 展示可按日夜汇总并去重，不能将独立的机械日志与因果日志各当一份决策重复输出。
- 验收：Host 可由**当前 Session 状态 + 最小去重日/夜游戏记录**构造后续 AI 差量输入，并用同一份记录做最终玩家复盘。真正选定酒鬼、角色收到的信息及人工输入的公开身份声明均可见，且不包含「AI 推荐/人工操作」来源标签。同一 Session 原始事实只生成一份只读游戏记录，不增加第二持久化事件层；不要求从任意历史游标完全还原 GameState。

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
