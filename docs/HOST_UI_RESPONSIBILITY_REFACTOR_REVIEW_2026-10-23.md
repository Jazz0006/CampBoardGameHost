# Host UI 职责重构规划独立评审与改进建议（2026-10-23）

> **文档性质：** 对 [原始重构规划](HOST_UI_RESPONSIBILITY_REFACTOR_PLAN_2026-10-23.md) 的独立架构评审与修改建议；不是已经批准的实施合同，也不代表任何生产代码已修改。
>
> **审阅基线：** `refactor/host-ui-responsibility-plan-20261023` / [PR #351](https://github.com/Jazz0006/CampBoardGameHost/pull/351)，源自 `main` `5dd880f95997ff476bffc5ef1aa3fc13f374e21b`。原方案为 docs-only，Stage 1–5 的 Host 方桌与信息角色开发已合并主线，实机验收仍待执行。
>
> **建议结论：** **接受分批重构的总体方向，调整 Batch 1、2、4、5 的边界及任务顺序后再实施**。保留原计划独立文档；本评审不直接改写或替代原规划。实施需通过所有者批准，并遵守 `AGENTS.md`、当前 Roadmap/Handoff、测试策略。

## 1. 总体判断

既有文件拆分减小了源文件体积，但没有充分解决真实状态所有权与调用链问题：`CampBoardGameHostApp.kt` 仍持有导航、偏好、局生命周期、保存和大量临时 UI 状态；`ClocktowerSessionHost.kt` 中存在 11 个按整局生命周期保存的夜间角色草稿；`ClocktowerJudgeScreen` 同时计算 Host 事实、AI 有效性和 UI 就绪状态，通过大量参数与回调转发。

原方案从“职责边界”和“状态生命周期”而不是机械拆文件入手，是正确方向。其优先成果应是**不再有第二个可写游戏事实来源、没有业务规则由 UI 裁判、AI 推荐不影响确定性规则、每步交互草稿不会泄露到下一步**。50,000 B/文件是工程维护约束，不能取代这些产品级验收标准。

## 2. 必须守住的体系结构边界

1. **唯一事实写者：** `ClocktowerGameSession/ClocktowerSessionState` 保存真实角色、座位、阵营、生死、状态、阶段、已确认行动和有序 Observation。所有真正的动作与游戏事实只经 Session 的正式确认入口修改。
2. **依赖方向单向：** 游戏规则计算只读取 Session、剧本及必要的当前输入；`PlayerCard` / Compose UI 展示模型不能反向成为规则真相或决定生死、唤醒、胜负。
3. **草稿无权威：** 当前交互的选座位、预览、候选高亮和按钮状态可丢弃，不是持久 GameState、Pending 事件或另一份步骤游标。
4. **规则与建议分离：** Host 计算确定值、必要结构约束及真实存在的说书人裁量边界；AI 只给裁量建议，不能替玩家作出玩家自己的目标选择或绕开 Session 的统一提交入口。
5. **候选提示不等于非法性拒绝：** 金色/青色虚线、首选项及产品缩略名单用于帮助说书人选择，不能变回旧 Host 泛用合法域、候选评分、世界枚举或普遍 veto 机制。真正需要的结构约束、确定真值和允许裁量应明确区分。
6. **D2 不变：** 可选择→全屏预览→返回修改→再次预览；只有本角色 **Next** 提交最后确认展示的一条 Observation，不记录中间预览、不跨角色 Previous；意外退出可重做未确认步骤。
7. **已有能力不能丢：** TB 5–15、NGJ 5–6、三模式、首夜联合规划/后夜 AI 增量与战略记忆、两类跨局防重复、隐私、AtomicFile、安全续局、幂等和 stale 检查。
8. **替换即删除：** 一条生产调用链迁移完成并验证后，同一批移除被替代接口、旧 UI 游标、重复状态与独占低价值测试，不保留永久双轨。

## 3. Batch 1 —— 交互级 NightStepDraft：建议采纳并加强输入标识

### 3.1 认可之处

把 11 个在 `remember(gameId)` 中长期存活的角色草稿收敛成当前 `(gameId, interactionId)` 作用域，能够显著减少不同角色间草稿残留、手工重置和跨三层参数转发。这是最合适的第一批结构改造。

### 3.2 对原接口的建议

原计划示例：

```kotlin
NightStepDraft(interactionId, targets: List<String>)
```

建议优先使用稳定的座位编号，避免用玩家姓名充当业务标识：

```kotlin
data class NightStepDraft(
    val interactionId: String,
    val selectedSeats: List<Int> = emptyList(),
)
```

此处仅为最小结构示例，最终应复用代码库已有的 `interactionId` / 座位类型，不另造第二套 ID。若已有专用选择模型可复用，不强制引入新包装类型。**不允许**把 `List<Int>` 本身当作可以解释任意角色规则的“通用候选域”；当前 `ClocktowerNightAction` / 查询类型负责解释需要几个座位、有无先后顺序、当前输入是否齐备。避免为此增加一套策略注册表或工厂。

- `Red Herring` 可以是当前步骤的未确认草稿，但其正式座位始终从 Session 读取；草稿改变不调用 `recordPlayerInput()`，只有正式步骤确认才写入 Session。
- Fortune Teller、Chambermaid、Ravenkeeper 的相关目标变化必须使原信息预览失效；需要重新展示才可 Next。
- Fortune Teller 两座位仍用于 AI 请求指纹、`fortuneTellerSubjectSeats` 及当前请求过期判定。
- Chambermaid 必须根据本轮现存目标约束重新验证两个不同且合适的目标；其他角色沿用各自已批准的选择行为。
- Imp 自杀后的继任裁量和 Demon bluffs 不应被误塞入夜间目标列表；各自独立输入和确认职责保留。
- 改变普通玩家目标时保留当前 `recordPlayerInput()` 的修订号语义。该 revision 是识别过期请求的技术机制，**不代表已发生/已公开的游戏事实或一条行动事件**。不要趁本次重构更改其时序。

### 3.3 本批退出标准

在不改变任何正式 Session 确认 API 的前提下，逐一审计 11 个旧草稿的生产读写者，确保跨角色读取一律从正式 Session 事件获得，而不是依赖上一个草稿。以真实 Compose 交互验证：进入下一步骤清空旧草稿；目标变化使预览/AI 过期；红鲱鱼草稿不变更 input revision；D2 Next 只写一条最终 Observation。迁移后实际删除旧参数/回调及重置代码。

## 4. Batch 2 —— HostNightStep 与 AI Overlay：最重要的边界修订

### 4.1 Host 输入不依赖 PlayerCard

原计划：

```kotlin
hostNightStep(sessionState, ruleset, cards, draft): HostNightStep
```

推荐收窄为规则层可复用的只读签名（示意）：

```kotlin
hostNightStep(
    sessionState: ClocktowerSessionState,
    ruleset: BuiltInRuleset,
    draft: NightStepDraft,
): HostNightStep
```

`BuiltInRuleset` 是抽象占位，实施时使用现有剧本/规则类型，**不得为这段示例创建新框架**。名字、文案、`PlayerCard`、阵营卡颜色、虚线、按钮和布局属于 UI 显示投影，不能流入纯规则计算。若 `HostNightStep` 与现有 Session 夜间步骤查询已有重复职责，应复用后者而不是新造第二个 `nextStep` 引擎。

### 4.2 必须区分四类信息，不能合并为“合法目标候选列表”

| 分类 | 正确归属 | 示例及约束 |
| --- | --- | --- |
| 确定性游戏结果 | Host/Session 只读求值 | 健康 Chef 数值、固定 Fortune Teller 布尔结果；AI 不猜测唯一答案 |
| 必要结构约束 | 当前步骤/Session 确认边界 | 存在的座位、两个不同目标、正确的当前交互身份和幂等修订 |
| 真实说书人裁量 | Host 返回窄范围，AI 或真人决定 | 有效 Spy/Recluse 登记、醉毒不可靠信息、市长转移 |
| 视觉候选/建议提示 | UI 投影与 AI Overlay | 金色/青色虚线、推荐高亮、Pair 首选短名单；**不是全部合法域，更不能自动禁止点击** |

对未唯一确定的信息，允许说书人在已批准的实际流程里作出有意义的选择；实际误播不能被 Session 静默伪装为计算真值。仅有必要时才通过明确的已显示信息/纠错机制记录事实，不为普通选择增添一套审计事件。

### 4.3 避免规则与 UI 就绪混装

`HostNightStep` 最好只表达当前 Session 已确定的步骤、需要的输入、机械结果及真实裁量边界。`nextEnabled` 在实现上可由 Host 必要条件和当前 UI 是否已完成预览共同派生，但**不应让纯规则函数依赖 Compose 预览状态或 AI busy/error**。

`nightAiOverlay` 保持纯只读：验证 request identity、game/input revision、剧本/角色/seat/metric/subjects，判断当前建议是否仍可用并提供高亮；不调用 Session 修改方法、不得随意扩展 Host 裁量范围。MANUAL 完全绕过 AI，AI_AUTOMATIC 仍须等待真实玩家输入或点击现场展示按钮。

### 4.4 本批退出标准

删除 Compose 内重复的规则判断、英文角色名→metric 硬编码映射和重复的 proposition 生成；以少量 Session/JVM 用例覆盖确定真值、Spy/Recluse、醉毒、Mayor/Imp 继任、错误目标及过期 AI。AI 模块移除时 MANUAL 整局仍可进行。不得借此恢复旧 `Rules` 泛用合法性引擎、评分或世界枚举。

## 5. Batch 3 —— Judge Screen 变成路由器：建议采纳

将 `ClocktowerJudgeScreen` 收敛为当前完整 UI 状态的轻量路由器，把夜间展示和 Next 处理放在唯一 `ClocktowerNightHostScreen`，职责清晰。

- 夜间页面只消费 Host 步骤投影、AI 推荐和当前交互草稿，不能自行维护业务权威步骤索引。
- `lastInformationPreview` / `playerDisplayStep` 必须与当前交互 ID 绑定；返回同一步可以改选并重预览，跨步骤不得重用旧预览。
- 同批删除旧 Judge 原有的夜间选择/提交/AI 决策路径，不建立两套 Next 回调链。
- 约 3KB/15–20KB 等文件体积目标是参考而不是产品通过标准；优先保证实际 D2、夜间死亡影响后续唤醒和三模式行为不变。

## 6. Batch 4 —— Observable Session：建议采纳，但不得重写事务语义

现有 `ClocktowerGameSession` 已有 `persistWith`、`confirmAtomically` 和 `updateState`：确认批次失败会恢复原先内存状态，非批次状态更新在对外生效前尝试持久化。本批**仅替换通知 UI 更新的方式，不改变这些原子写入职责**。

建议 `ClocktowerSessionUiState` 只持有“供 Compose 观察的最新已接受 Session 快照”及同一 Session 实例引用；不拥有另一套可修改的 GameState，也不对外开放直接赋值的规则结果。事务成功后发布 `session.state`；遇到 `IOException`，保留旧快照并显示保存错误，不能先刷新 UI 成功状态再回滚。

- 需要覆盖原 Session 替换/恢复/重新开局时，旧异步回调不得把状态发布到新对局。
- 统一两处 `confirmClocktowerStep` 时不丢失不同调用者的返回值、错误处理、序列检查。
- 所有原 `refreshClocktowerUi()` 生产调用者迁移并有行为测试后，才删除 `clocktowerUiTick` / `onChanged`。
- 确保所有纯展示投影确实在新的 Session 快照变更时重算，不能因旧 `remember` 键只包含语言等条件而显示过时事实。
- 回归：保存失败不推进；双击 Next 幂等；D2 确认只写一次；连续提交、加载旧局后恢复以及中断后继续均保持正确。

## 7. Batch 5 —— App 根组件职责收敛：有条件认可 Lifecycle Owner

**对 PR #351 请求的所有者裁定建议：可以允许一个具体的 `ClocktowerGameLifecycle` 协调者，但不能批准通用 Controller/Manager。**

可管理：新建/切换/结束活动局、恢复活动局、绑定持久化、与已存在的跨局轮换配置协调。它只编排这些已经存在的入口，不重新封装一套 Host 规则 API、缓存当前角色/阶段/死亡/观察事件，也不生成第二份独立夜间游标或恢复器。

建议明确以下所有权边界：

| 状态/职责 | 建议所有者 |
| --- | --- |
| 角色、夜序、生死、轮次、已确认事件、胜负 | 唯一 Session |
| App 屏幕路由、对话框可见性 | App UI/导航层 |
| MANUAL / AI_ASSISTED / AI_AUTOMATIC 模式设置 | 已有模式偏好/活动局设置合同，不在 Lifecycle 中另设相互竞争副本 |
| 玩家名单及经验偏好 | `AppPreferencesState` 与现有偏好存储 |
| 活动 Session 创建/恢复/结束的调用编排 | 具体 `ClocktowerGameLifecycle` |
| AI 开局计划、战略 cursor、response ID | 既有 `ClocktowerActiveGameMetadata` / AI 模块 |

`ClocktowerOpeningDraft` 如需调用 Lifecycle，可使用少量清晰的操作接口，避免从多个 getter/setter lambda 变为一个可随意读写所有字段的“大杂烩”持有者。

对 TB/NGJ 共用 `PlayerCard` builder、规则集查找、主题及姓名→座位转换的去重予以认可；最终以 App 根组件不再承担大量业务操作为验收，不能只看 `CampBoardGameHostApp.kt` 是否降至约 20 KB。

## 8. Batch 6 —— 可移植 Session JSON 数据合同：保留但独立实施

这批有利于未来 TypeScript WebHost / EvidenceLab 共享统一游戏事实语义。现在无需建设跨语言 SDK、第二套 Session 实现或改变持久化格式。

- 从现有 `ClocktowerSessionJson.kt` 精确记录 v1 版本/字段、座位及角色标识、顺序 ActionFact、按接收者区分的 Observation、特殊状态和可选字段含义。
- 增加少量匿名无真实个人信息的 golden fixtures：TB 中局、NGJ、Klutz/Slayer 与信息展示。
- 用 Kotlin decode→encode→decode 验证语义往返、事件顺序和接收者私密边界；仅在编码器已保证规范化输出时才要求字节完全相同。
- 不把 Android 界面、AI 供应商私密凭据或 UI 草稿塞进共享格式；正式跨语言实现等实际 WebHost 工作启动后再安排。

## 9. 建议实施顺序（原分批保留，独立 P1 前插）

| 次序 | 工作 | 退出条件 |
| --- | --- | --- |
| 0 | 所有者审阅并定稿本评审提出的 Batch 1/2/4/5 边界 | 不新增第二权威、候选引擎或泛用 Lifecycle facade |
| 1 | 独立 P1：活人可以提名已死目标 | Day UI + 正式 Session 事件/票务行为回归通过；不要因重构而延后已知真实玩法 Bug |
| 2 | Batch 1：按互动 ID 统一草稿 | 角色选择/preview/revision 完整迁移并实际删除旧草稿路径 |
| 3 | Batch 2：纯 Host Step + AI Overlay | 无 PlayerCard 规则依赖；确定真值、裁量和提示正确分离 |
| 4 | 独立 P1：健康 Fortune Teller 唯一布尔结果 | 在新 Host 边界修复并覆盖醉毒/登记及目标变动 |
| 5 | Batch 3：Judge → Night UI | 单一夜间 Next；D2 与整局真实 App 回归 |
| 6 | Batch 4：可观察 Session 适配 | 原子事务、错误回滚、替换 Session/恢复及刷新全部验证，旧 tick 移除 |
| 7 | Batch 5：App 根组件按所有权收敛 | Lifecycle 只协调，偏好/导航/Session 各归其位 |
| 独立 | Batch 6：JSON Schema 与 golden fixtures | 不改现有格式，不阻塞 Android |

**真机验收不应被新的纯架构重构长期推迟：** 先保留五批完成后的设备基线；在 D2/Session UI/Lifecycle 相关批次后抽测实际 Android 手机的 5/15 人方桌、醉毒/死亡/鬼票叠加、三模式全屏交接、来电/切后台/杀进程后实用续局。不能以 Robolectric/CI 代替真机结果，付费 API 仍需单独授权。

## 10. 每个实施 PR 的验收清单

- [ ] 从最新 `main` 创建有界分支；核对真实 HEAD、开放 PR、工作树和本批生产消费者。
- [ ] 写明本批的新职责所有者、删除的原职责/参数/回调/文件、实际净增减字节和超过 50,000 B 的文件。
- [ ] 列出有无新增可写状态、额外步骤游标、通用 Rules/候选域、影子恢复器；均应为**无**。
- [ ] 明确不变的 Session API/JSON、D2 当前步骤 preview→Next、AI stale/请求身份、现实玩家输入/三模式的用例。
- [ ] 对高风险 Session/Compose 交互增加少量高价值测试，不恢复机械结构断言或旧规则引擎专属测试。
- [ ] 在**精确生产代码 SHA** 上验证完整 Android CI + R2；docs-only CI 不充当构建或设备验收。
- [ ] 报告真机、强退/恢复、付费 LLM 哪些真实执行，哪些明确未执行。
- [ ] 新消费者切换后同批删除旧调用与独占测试，禁止永久新旧双轨；未经授权不合并 PR 或执行付费请求。

## 11. 待所有者批准的关键裁定

1. **Batch 1**：同意“一个当前交互草稿”，优先用稳定座位 ID，并保持现有 red-herring/input revision 特例；不引入通用规则解释器。
2. **Batch 2**：同意 Host/AI 解耦，但 `hostNightStep` 不读 `PlayerCard`；视觉提示与真实许可范围分离，避免重建泛用候选或规则 veto 引擎。
3. **Batch 4**：同意由 UI Adapter 观察 Session；必须完全沿用现有事务原子性及失败回滚，观察者不成为新事实持有者。
4. **Batch 5**：**有条件批准**具体 `ClocktowerGameLifecycle`，只做局生命周期编排；偏好、导航、运行模式与游戏状态保留各自明确所有者。
5. **顺序**：独立的白天提名已死玩家 P1 不需等待这次重构；健康 Fortune Teller 的 P1 可在 Batch 2 边界稳定后落地；实机验收仍作为独立风险检查点。

**最终建议：** 批准整体技术方向，先定稿上述边界，再按小批次实际实施、验证并删除被替代路径。架构改造的成功应以单一事实来源、职责清晰、真实玩法不回退及代码净复杂度下降衡量，而不是以新文件数量或单文件大小独立衡量。
