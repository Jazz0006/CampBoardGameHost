# 最小游戏状态与记录清理

日期：2026-10-17，Australia/Sydney。依据：项目所有者本轮明确纠正。
状态：**本地源码审计与删除设计；尚未实施生产代码删除。**
审计基线：本地 `770ed32f`，起始工作区干净。未核对远端状态，不据此声明 PR/CI 验收。

> 后续远端审计修正：开放 PR #315 的 `06dbb954` 含 main 尚未合入的大量清理和 Session 收敛；本文对象/尺寸清单仅适用于 main，不能直接作为最新开发分支的删除清单。先按 [重构前分支建议](PRE_REFACTOR_BRANCH_AUDIT_2026-10-17.md) 收束开发基线，再复核具体删除范围。产品目标不变。

## 1. 产品目标与实施顺序

游戏中断后，恢复玩家身份和足以继续游戏的进度。无需独立游戏存档产品，无需精确恢复 UI、异步请求、候选缓存或每个中间步骤。

先删冗余，即使事件记录尚不完整。允许暂时没有中断恢复、完整文字回顾或部分历史辅助展示；不能把“先实现完整替代品”变成保留旧系统的前提。缺口须如实记录，不能用空实现、假成功或默认事实掩盖。

最终只维护一个当前游戏事实所有者和一份权威游戏记录。初始配置、已确认的说书人决定、玩家操作、实际发出的信息及阶段推进进入同一记录；AI 请求与文字展示按需读取或投影它。这里的“一份”指不重复维护事实，不要求所有事件成为无类型字符串，也不要求把所有类压成一个文件。

恢复必须由本地程序读取已确认事实；不得调用 LLM 猜测历史、重新抽身份、重新决定已发信息。仍需把这份记录可靠地存到本地；“不要独立存档”不等于“不要持久化”。持久化和最小继续游戏能力在减法完成后实现，不在本轮借机建设通用 event-sourcing 框架、快照平台或迁移系统。

## 2. 当前结构与实证

Gradle 当前只包含 `:app`；`player/`、`ui/`、`web/` 不是独立 Gradle 模块。

- `clocktower/setup`：开局生成、展示身份及酒鬼晚绑定相关输入。
- `clocktower/domain`、`rules`：类型、规则及合法性。
- `clocktower/session`：当前游戏事实/时间线、确认边界、推荐上下文和协调。
- `clocktower/epistemic`：玩家已获信息与推理/回放相关实现。
- `clocktower/recommendation`：推荐与既有实验、分析实现；不能成为游戏事实权威。
- `persistence`：混有应用设置、独立 Recovery、已结束游戏归档及历史序列化。
- App 根与 Host：Compose 生命周期、游戏流程、状态、回调和展示仍有较多混合责任。

| 现有对象/路径 | 源码观察 | 清理方向 |
| --- | --- | --- |
| `ClocktowerGameSession` / `ClocktowerSessionState` | 持有 `GameState`、动作时间线、观察记录、revision 等 | 保留游戏权威；随后把仍散落 App 的已确认机械状态归回真实所有者 |
| App `cards`、phase、round、确认目标、能力使用标记等 | 与 Session 并存；`GameState` 本身主要覆盖玩家身份、存活、中毒，尚不覆盖完整流程 | 区分 UI 草稿与重复事实，不能把全部变量机械装进一个大 Context |
| `RecoverySnapshot` 及读写/校验/恢复链 | 另存 cards、位置、机械目标/标记、文字事件、动作/观察、因果日志、SDE 导出 | 删除独立恢复产品和专用支持链，不继续缩减 DTO 后保留体系 |
| `ClocktowerEvent` / `clocktowerEvents` | 独立可变的本地化 title/detail 列表 | 删除独立事实维护与持久化；未来文字记录只做权威事件的展示 |
| `GameArchiveRecord` / store / codec | 另存 cards、records、events、outcome，最多 20 局 | 清除重复归档链；跨局统计若有独立有效需求须单独论证，不借其保留整份旧存档 |
| `ActionFactTimeline` + `EpistemicObservationLog` | 已有共同 global sequence；AI prefix 合并两者 | 保留已确认事实，作为后续统一记录的基础；不另造一套并行日志 |
| `StorytellerCausalDecisionJournalV1` | 不只存决定，还冻结请求时的 seats、historyPrefix、候选 ID 等 | 分离实际决定与精确历史请求重建需求；审计并删除后者的非必要副本，不能整类误删已确认登记事实 |
| `GameSnapshot` / TB snapshot / request DTO | 部分是不可变读取投影 | 不以名称含 State/Snapshot 就认定冗余；检查是否独立可写、重复持久化、存在实际消费者 |

### 会影响删除顺序的真实耦合

1. App `addClocktowerEvent` 不只是写文字：还推进 revision，并为部分死亡/处决生成 `ActionFactDraft` 和公开 `AliveAt` 观察。删除文字写入时必须保留这些确认事实及先后关系。
2. Host 从文字事件读取夜间动作去重、间谍变化摘要、误导连续次数；`previousClocktowerUnreliableNumber` 甚至从本地化 detail 用正则取数字。辅助历史展示可以删除/暂缺，不能让规则继续解析文字日志。涉及真实已发布信息或重复提交防护的部分必须保留其事实约束。
3. App `persistAndReleaseA4ObservationRebuildIfDurable` 把观察缓存重建和旧 Recovery 写盘绑定。删除恢复时要解除该依赖，不能简单返回“保存成功”维持旧门。
4. `NightDawnPoisonRecoveryAuthority` / `DuskPoisonExpiryRecoveryAuthority` 从动作时间线读取中毒事实；它们参与运行中的昼夜转换。名字含 Recovery 不是删除依据，需区分正常规则行为与退出恢复专用分支。
5. 独立恢复与已结束归档还共用于 Undercover。删除公共文件时必须处理其调用者；本轮可移除共同旧恢复入口，但不能误删 Undercover 的发牌、淘汰和胜负流程。
6. 现有 AI prefix 的 coverage 明确为 PARTIAL/UNKNOWN。当前代码已有 `Nomination`、`Vote` 及 App 写入，不能沿用 10-08 文档“只有七类动作”的过时结论；也不能因此宣称完整开局和全局恢复已具备。

## 3. 删除顺序与验收

### A. 删除独立恢复

范围：Recovery 入口、预览、生命周期写入、快照/codec/strict decoder、恢复 planner/application coordinator/write gate、仅服务于旧恢复的校验和测试；继续沿调用链删除不再使用的序列化和还原代码。

直接候选：`persistence/Recovery*.kt` 九个文件，加 `ActiveGameSaveInvariantValidators.kt`，基线共 1,557 行 / 74,308 字节。该数值不包括 App 接线和其他共享支持文件，不能当作最终净删量。

验收：新局与正常手动流程继续运行；不再出现可用的旧恢复入口，不再写旧 active-game 快照；明确此阶段中断恢复暂缺。既有旧文件不迁移，不因本地审计主动擦除用户磁盘数据。缓存及规则确认不得依赖伪造持久化成功。

### B. 删除独立文字记录与重复归档

逐条检查生产写入/读取：保留它们承载的结构化确认事实，删除 title/detail 作为事实来源的路径、独立列表、序列化和仅服务该路径的 UI/测试。允许暂缺回顾、摘要等功能，不为保留旧显示重建另一份历史数据库。

验收：提名/投票、死亡/处决、身份变化、实际发出信息的已存在结构化记录仍正确；没有从本地化文字反推事实；正常规则不被改写。保留的事实约束用已有 typed tests 和针对性的流程证据验证。

### C. 清理多余状态与精确重建支架

对 App/Session/因果日志/推荐回放的生产调用图重新审计。删除重复可写状态、无生产消费者的契约、只为精确历史请求重建而保存的副本及已退役测试。已确认规则事实、实际收到的信息、合法性检查不得混同为重建垃圾。

不先增加通用 controller、统一 God Context、双写 adapter 或版本迁移桥。剩余确需的只读投影可以保留；每个需要保留的模块必须说明具体消费者和产品责任。

### D. 删除后重新审计，再补最小缺口

先量化净删除及剩余责任，再决定是否需要拆分大文件。随后补足同一份事件记录的初始配置、必要确认结果与进度覆盖及持久化，实现从最后已确认步骤继续。恢复不追求原页面、草稿、弹窗或请求现场；已向玩家发布的信息不能被丢弃后重新决定。

不以恢复能力尚缺为理由回滚减法、保留旧 Recovery 或实现第二份快照。

## 4. 大文件审计与架构 pre-flight

| 文件 | 当前行数 | 当前字节数 |
| --- | ---: | ---: |
| `CampBoardGameHostApp.kt` | 4,768 | 271,975 |
| `clocktower/ui/ClocktowerHostScreen.kt` | 2,966 | 175,951 |

当前是一个超过 200 KB，另一个约 176 KB；不能把历史尺寸当作现状。App 的直接恢复构造/应用/生命周期集中于约 1128–1470 行，但还存在外围接线。仅删这段不足以解决整个大文件问题。

Architecture pre-flight:
- current owner: App 持有 UI 生命周期、机械状态、日志接线和恢复；Host 混有夜间交互、信息准备、文字历史读取；Session 持有部分事实和全局序列。
- proposed responsibility: 移除独立恢复和重复日志，不增加新框架；实际确认事实仍由游戏边界提交。
- authoritative state owner(s): `ClocktowerGameSession` 是 Clocktower 事实目标所有者；App 中尚存的权威状态需逐项归属，不能假称已完成；UI 草稿由对应交互所有者持有。
- narrow typed input/output seam: 已有确认动作/信息输入及 Session 事实输出；不新增整个 App/Host 的代理对象。
- keep in current owner / extract: 先删责任，暂不机械搬文件；删除后按 AI 请求生命周期、阶段交互和规则提交的真实边界再评估。
- reason: 先移除功能负担才可能减少理解半径；搬走原封不动的巨型回调不是减法。

后续审计应同时报告：生产代码净变化、最大文件尺寸、重复事实写入口、独立存储副本、回调/参数面，以及修改一个常规动作所需理解的所有者数量。尺寸降低但依赖半径扩大不算完成。

## 5. 证据与当前完成范围

本次读取当前入口、架构、Recovery、历史覆盖、既有拆分审计和测试策略，并对照本地实现；做了生产引用搜索和尺寸统计。旧文档含多层过时 NEXT/PR 状态，本轮收敛当前入口，不复制它们成为新现状。

本次仅变更文档，未删生产代码、未运行 Android 测试、未进行付费模型调用、未提交/推送或声明远端验收。生产清理按 A → B → C → D 执行。每步重查生产 fan-out，删除已退役功能专属测试，保留真实规则/事实回归；以编译、相关 typed tests、T1/触发的 T2 及最终设备流程验证，不制造 RED。CI/R2 仍是后续合并门。

本地执行直接读写本工作区，遵循本轮用户覆盖指令；不经过 Mini MCP 或 GitHub 读写本地源文件。
