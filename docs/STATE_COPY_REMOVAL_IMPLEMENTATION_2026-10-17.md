# 第三阶段：重复状态与历史请求支架清理

基线 #317 `60f61c3b`，分支 `codex/reduce-runtime-state-copies`。

验收：幽灵票消费只来自 Session 已确认 Vote，草稿和失败提交不消费；当前 AI 请求仍获得完整的已有动作/观察前缀，登记裁定仍核验真实观察、身份和合法候选；不再为历史请求保留玩家资料和全量历史副本。允许无历史请求精确重建，不补替代存档。

Architecture pre-flight:
- current owner: App 双写幽灵票集合与 Session Vote；journal 每个 capture 复制完整 historyPrefix 和玩家资料；Session 保留旧 snapshot 缓存/restore API。
- proposed responsibility: 删除重复写入口和历史请求副本，保留实际裁定及提交所需最小证据；移除仅供旧恢复测试使用的生产契约。
- authoritative state owner(s): Session 动作/观察及当前 GameState；journal 仅保留确认裁定与必要的提交校验上下文；UI 草稿仍归 UI。
- narrow typed input/output seam: ActionFactTimeline -> 幽灵票只读投影；capture -> 单条实际观察引用和裁定校验所需身份/候选；当前请求仍按 Session 即时投影。
- keep in current owner / extract: 在现有 owner 减法，不增加 controller、God Context、双写桥或持久化机制。
- reason: 已确认 Vote 足以决定已用幽灵票；登记校验只消费刚发布的一条观察，无需每个裁定复制全部历史。旧 Klutz+Spy 历史兼容是不可生产的旧 fixture，不是当前 Klutz 规则。

扇出：App/Host 投票提交、table/presentation、vote transaction；journal capture/atomic trial restore、registration/day producers、测试。保留 Session 原子 trial journal，保留当前角色/存活/中毒和观察一致性检查。删除 legacy Klutz archive 专属 validator/fixture/tests，当前 Klutz learned-death/choice 与 death-time evidence 覆盖保留。

风险：Host 必须先成功提交正式 Vote 再改变 UI standing；不得把 alive voter 当 ghost voter。旧历史复制测试退休，保留 stale request、伪造观察/登记、候选、修正顺序、原子失败测试并运行完整 JVM/assemble。App confirmed night targets 涉及上游改变后的失效与尚未执行的继任，不可简单从最后一条事件推导；继续按真实语义审计，不能把安全草稿误删。

阶段/轮数扇出复核：四种 ClocktowerPhase 与 StorytellerPhase 一一对应，所有生产推进都已先提交 PhaseAdvance 再赋值 Compose phase/round。删除后两次赋值，以 Session 最后 PhaseAdvance（初始 FirstNight/1）作为只读投影。Undercover 独立轮数保留。采用 Compose derivedStateOf 委托，确保同一回调提交后立即读取新阶段，不等待重组。不会把 nightStep/dayMode 等 UI 子步骤混入正式游戏记录。

## 本检查点删除与证据

- 幽灵票只读投影读取 Session Vote.ghostVoterSeats，删除 Compose 已用票集合、变更回调、transaction 第二份消费结果及旧 JSON codec。Host 先正式提交 Vote 再更新最高票 UI。
- Clocktower phase/round 从已确认 PhaseAdvance 投影；删除 App 的可写副本及逐处同步赋值。Undercover 轮数独立保留。
- journal 不再保存 historyPrefix、玩家资料、setup、seed、无关座位和整份 position；只保留登记校验实际使用的 1–2 个当时角色/状态、阶段、候选约束及（仅信息登记）刚发布的一条观察。当前 AI 请求仍从 Session 动作/观察即时投影，未删事实。
- 删除仅测试消费的 frozenAt/effectiveAt、无历史 cutoff API、Session.restore(GameSnapshot)、独立 allocateTimelinePoint、snapshotCache、CrossGameHistory 及记录入口。GameSnapshot 仍是规则/推荐的只读适配，不是独立持久化。
- journal 内部 Entry 和外部 record 合为一种不可变记录；保留隔离 trial journal 的原子提交校验，删除历史前缀精确重建。不能整类删除 journal，因为登记裁定是实际确认事实。
- 删除不可生产的旧 TB+Klutz+Spy 存档 validator、fixture 与专属测试。真实 NGJ Klutz 流程仍由 LiveKlutzScriptReachabilityTest、ClocktowerGameSessionAliveBoundaryTest 等覆盖死亡前状态/已知死亡/实际选择。
- 旧恢复、历史请求复制测试退休；观察连续性改测当前 Session，cursor overflow 改在已有纯提交边界证明，幂等/失败无副作用比较权威 state 而非缓存对象地址。新增/扩展现有幽灵票和 phase 测试，覆盖草稿、失败、重试、跨轮和当前阶段输出。
- 完整 JVM 1,362 项通过，0 failures/errors/skipped；assembleDebug 成功。最终 FAST 1,355 项通过，0 failures/errors/skipped；远端 full CI/R2 状态在 PR 记录；无真机和付费模型声明。

## 剩余状态审计（不伪称全部收敛）

| 剩余对象 | 当前用途与决定 |
| --- | --- |
| App confirmedMonk / confirmedMayor / pendingAttack | 仍服务同夜规则、上游重新确认后的失效、无死亡清理；现有动作只记正向选择，不能从最后一条动作准确推导当前有效值。保留正常规则，下一检查点把这个小型夜间机械责任归 Session 并退休 App 写入口。不要重建大 checkpoint 存档。 |
| confirmedDemonSuccessor / pending new demon | 是尚待 New Demon 步骤执行的选择，不是已变更角色的第二事实。必须保持选择与角色生效的区别，再收敛夜间边界。 |
| nightStep/dayMode、未确认角色目标 | 当前 UI 导航/草稿，正常生命周期所需；未来恢复不要求还原它们。 |
| highestVoteName/count | 当日票数领先/平票状态，涉及当时门槛，尚未成为 Session 的确认结果；不能用当前存活人数回算每次历史投票。与夜间归属分开处理。 |
| GameSnapshot / TB snapshot / request | 实际规则和 AI 消费者需要的临时只读投影，不独立可写或保存。 |
| journal.validation、typed trial restore | 当前正式裁定校验及原子失败隔离需要；已没有精确历史请求 API 或整份历史副本。 |

本轮是第三阶段 C1 检查点。C2 继续夜间机械确认及当日投票结果的所有权收敛，之后进入 D 的 App/Host 职责审计。App/Host 仍约 240 KB/180 KB，没有借搬运巨型回调声称大文件问题已解决。当前继续没有中断恢复、完整复盘与持久化；事件覆盖仍不完整。

规模：本检查点生产净删 313 行、测试净删 721 行。App 4,137 行 / 240,194 bytes；Host 3,033 行 / 180,119 bytes。主要收益是删除历史副本与写入口，尚不是大文件拆分。
