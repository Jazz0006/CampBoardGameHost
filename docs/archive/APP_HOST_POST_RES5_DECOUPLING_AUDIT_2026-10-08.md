# App / Host：移除旧推荐算法后的解耦审计

> 日期：2026-10-08 Australia/Sydney
>
> 本地基线：`050f9ac72be5510a85f35ec75c89672bf935a6bf`，RES-5 / PR #242 合入提交。审计开始时工作区干净，处于 detached HEAD。
>
> 状态：**H-R / H-M / A-C / A-S / H-P / H-I 均已合并并经完整 CI/R2 验收。** PR #244 (CI #3837 / R2 #3512)、#245 (CI #3839 / R2 #3513)、#246 (CI #3841 / R2 #3514) 均 GREEN。当前更新后的独立再审计：`GSP_R1_POST_APP_HOST_SPLIT_REAUDIT_2026-10-08.md`。
>
> 原始审计范围：直接读取本地文件；当时未使用 Mini MCP、未查询远端。此文档后半部记录的实施现已通过 PR #244–246 完成；旧行号与「候选/待验收」描述属于历史过程，不得优先于本页当前状态及新 GSP-R1 再审计。

## 1. 结论

**还能继续拆，但两个文件应采用不同策略。**

- Host 的主要机会是：登记结果投影、首夜/其他夜晚重复的角色步骤构造、玩家展示的记录准备。这些有机会真正减少一个功能需要理解的代码范围。
- App 的主要机会是：把仍留在回调中的规则判断收回已有规则边界，以及独立移出角色目录。App 大量剩余代码是状态生命周期与跨模块事务应用，整体搬走不会降低耦合。
- 不恢复旧推荐算法的拆分路线；不新增通用 `AppContext`、`HostActions`、事务 executor 或第二个 session authority；不把 Host 内容转移到已经 1,169 行的 `ClocktowerNightStepUi.kt`。
- 第一轮优先做 **H-R 登记结果投影 → H-M 角色步骤构造**；App 可以独立做 **A-C 目录搬迁**，随后做 **A-S 非自杀恶魔死亡接任规则收敛**。每个切片单独验证，不开一个覆盖所有生命周期的大重构。

这是静态架构审计，不是全项目玩法正确性认证或性能测量。完整阅读了两个目标文件，并检查相关 rules/session/flow/presentation/persistence、生产引用、测试和 R2 工作流；未逐行审查所有其他源文件。

## 2. 当前尺寸与职责集中度

计数口径：物理行数、UTF-8 字节；包含 import、注释及空行，不等同于逻辑 LOC。

| 生产文件 | 行数 | UTF-8 字节 | 判断 |
| --- | ---: | ---: | --- |
| `CampBoardGameHostApp.kt` | 3,587 | 204,014 | 最大；状态、恢复、事务应用和少量规则/目录混合 |
| `clocktower/ui/ClocktowerHostScreen.kt` | 2,953 | 179,138 | 第二；角色准备、登记交互、流程组装和路由混合 |
| `ClocktowerNightStepUi.kt` | 1,169 | 57,214 | 第三；应避免成为下一轮承接所有逻辑的文件 |
| `clocktower/epistemic/EnumeratedWorldSet.kt` | 744 | 36,760 | 算法文件；本轮没有仅因尺寸建议拆分 |
| `ClocktowerSquareTableUi.kt` | 789 | 34,284 | 共享渲染；本轮不改 |

`ClocktowerJudgeScreen` 仍有 **72 个参数，其中 36 个 `on…` 回调、5 个 `MutableState` 参数**。App 有 80 个 `remember` 调用，Host 有 27 个。这些是所有权/生命周期审查信号，不是要求合并成大对象的理由。

### App 当前分布

| 位置（本地基线行号） | 责任 | 审计判断 |
| --- | --- | --- |
| 232–331、3574–3587 | 角色查询、角色目录、标签和座位显示 | 可分离；角色目录有规则/恢复扇出 |
| 334–581 | Compose 状态、session view、规则/决策上下文 | 保留权威和生命周期；不整体状态化 |
| 582–709 | A4 请求准备、预热/重建 effect | 可读性机会存在，但无新证据支持本轮搬动 effect |
| 710–1018 | checkpoint、action/observation、事件和局部重置 | 跨边界顺序敏感；避免通用化 |
| 1019–1337 | Recovery capture/apply、durability、生命周期写入 | 维持 R7 单独设计 |
| 1388–1762 | 新局、发牌、Drunk 提交、archive/restart | Setup/session entry 的应用所有权保留 |
| 1763–1857 | 角色变化应用、非自杀恶魔死亡接任 | 只收敛纯规则判断；提交/记录保留 |
| 2373–2541 | `onConfirmNewDemon` | 169 行；角色/毒/黎明续接应用 |
| 2605–2717 | `onSlayerShot` | 113 行；能力判断与死亡副作用混合 |
| 2801–2934 | `onConfirmDay` | 134 行；白天结果、Dusk、续接 |
| 2935–3387 | `onConfirmNight` | 453 行；最不适合机械搬迁的区域 |
| 3508–3554 | `evaluateGameOutcome` | 玩法判断和本地化混合；可作为后续独立规则切片 |

### Host 当前分布

| 位置 | 责任 | 审计判断 |
| --- | --- | --- |
| 187–227 | 首夜信息 publication migration | 保留 game/round 生命周期与毒变化失效语义 |
| 228–363 | Spy/Recluse 选择、合法登记适配和登记事件去重 | 真正内聚的交互候选；先拆纯投影，再考虑状态 owner |
| 364–473、675–896 | 同夜投影适配、actor、信息真值/参考值准备 | 不能统一当作“呈现代码”搬迁 |
| 474–548 | 非信息夜间动作记录 | 独立记录准备机会；幂等状态暂留 Host |
| 590–642 | 首夜候选预计算等待/重试 | 体量有限，effect 不宜随 phase 条件迁移 |
| 898–970 | 私密信息 observation 准备与提交分流 | 有旧路径 fallback，需分阶段收敛 |
| 981–1007 | Spy 历史差异显示 | 纯投影，但当前依赖本地化标题，不宜顺手改成新历史协议 |
| 1011–1047 | bluff 手动草稿、合法域、邪恶信息 materializer | 上轮已建立边界，维持 |
| 1061–1210 | 三种 result-first registration options | 本轮最清楚的纯投影拆分候选 |
| 1212–2008 | 首夜/其他夜 materializer registry 组装 | 按重复角色或结果家族拆，不整体搬到大 builder |
| 2057–2563 | Day 交互/路由 | 仍夹有 Virgin/Artist 规则判断，不能只改路由文件 |
| 2636–2953 | 夜间推进、登记操作、玩家展示 handoff | 保留推进/发布顺序；可先拆记录格式投影 |

## 3. 第一优先级：H-R 登记结果投影

证据：Host 的 `resultFirstNumericRegistrationOptions`、`resultFirstFortuneTellerRegistrationOptions`、`resultFirstRoleRevealRegistrationOptions`（1061–1210）仍由大闭包捕获 cards、角色状态、登记字典、语言和 effective-state 方法。

已有稳定边界：

- `TroubleBrewingRegistrationDomain` 决定合法登记；
- `ClocktowerRegistrationResultDomain.kt` 已拥有 witness 组合与最终结果去重；
- Host 拥有当前临时选择与已记录标记；
- `ClocktowerNightStepUi` 消费结果并把选中的 witness 交回 Host。

建议增加一个明确命名的只读呈现 owner，例如 `ClocktowerRegistrationResultPresentation`。输入为已求得的合法登记候选/当前 witness、稳定 seat、最终 proposition 及显示文案；输出 `List<ClocktowerDisplayOption>`。不让它捕获 Host、解析 session、修改登记或发布信息。数值/布尔/角色结果可以是同文件的少量具体函数，不做跨所有信息类型的万能工厂。

**必须继承的扇出：** Chef、Empath 首夜与其他夜、Fortune Teller 两种夜晚、Undertaker、Ravenkeeper。Pair 信息继续使用 `PairInformationLegalDomain`/`ClocktowerPairManualAuthority`，因为它拥有成对角色候选的独立合法域；Virgin/Klutz/Slayer 的登记操作不属于本次“信息结果投影”，暂时保留。

保留约束：

1. 当前 witness 优先；相同最终结果去重后保留第一个合法 witness。
2. 稳定 seat、候选身份、排序和 registration provenance 不变。
3. 不可靠信息继续跳过不需要的登记步骤。
4. 其他夜晚使用 interaction 前的有效角色/毒状态；死亡触发角色保留死亡前能力状态。
5. `ClocktowerNightStepUi` 模型不加默认字段来掩盖缺失输入。

Architecture pre-flight:
- current owner: Host 内三个 result-first 函数；合法性已由 rules owner 提供。
- proposed responsibility: 已有合法结果的呈现投影。
- authoritative state owner(s): session 为机械真相；Host 为登记交互临时选择。
- narrow typed input/output seam: 合法 witness/result + seat + 文案 -> display options。
- keep in current owner / extract: 提取纯投影；选择、去重记录状态、effect 保留。
- reason: 同一登记结果功能不再依赖整个 Judge 闭包。

证据：沿用 `ClocktowerRegistrationResultDomainTest`、`ClocktowerRegistrationAuthorityTest`、`RegistrationInteractionRulesTest`；补上缺失的投影行为 characterization，覆盖三个结果形态及候选顺序。现有 tests 证明了 witness/去重原语，尚不能证明这三个 Host 闭包的全部集成。

## 4. 第一优先级：H-M 重复角色步骤 materializer

两份 registry 中重复构造 Poisoner、Butler、Empath、Fortune Teller、Spy。侍女已通过 `clocktowerChambermaidStepMaterializer` 在两处复用；Sage 也有独立 materializer。结构上已有可行先例。

建议顺序：

1. 先做 Fortune Teller / Empath 这种准备量较大、跨两个 phase 使用的步骤；输入已准备好的结果和选项，不接收登记字典或整个 `ClocktowerNightHostProjection`。
2. 再判断 Undertaker / Ravenkeeper 是否适合共用“角色查验结果呈现”边界；Ravenkeeper 的 trigger actor、死亡前能力状态必须显式保留，不能被共用模板覆盖。
3. Poisoner / Butler 等较小步骤可以在内聚的行动呈现文件中处理，不必每个角色一个微型文件。
4. Host 继续负责选哪个 registry、把 production flow 给 registry，以及阶段/interaction routing。规则顺序仍来自 `ClocktowerProductionFirstNightFlow` / `ClocktowerProductionOtherNightFlow`。

**不能直接复制合并两份代码。** 已观察到差异：

- Empath 首夜不可靠数字选项在 Host 1438 带 `propositionForValue`，其他夜 1614 附近没有；之后还经过 structured presentation。静态差异不等于已确认玩法 bug，但首轮重构必须保留或通过独立行为证据解释。
- Spy 首夜读取直接 poison target，其他夜读取 `effectivePoisonForRole("Spy")`。简单合并可能破坏同夜毒源死亡/角色变化后的时序。
- Butler 的首夜与其他夜文案不同。
- `ClocktowerInformationStepBuilder` 仅在相应 actor/reliability 条件下调用候选 lambda；不能把候选计算全部提前为 eager evaluation。

Architecture pre-flight:
- current owner: Host 两套 registry 内的角色构造；通用显示决策由 InformationStepBuilder 提供。
- proposed responsibility: 单角色或同一结果家族的 typed step materialization。
- authoritative state owner(s): rules/flow/session 保持不变；materializer 无可变状态。
- narrow typed input/output seam: 准备后的角色事实、结果、必要文案/惰性选项 -> registry entry / night step。
- keep in current owner / extract: 按角色/家族提取；registry/flow 选择保留 Host。
- reason: 修改同一角色不再同时编辑首夜、其他夜两套大块代码。

证据：`ClocktowerNightStepMaterializerRegistryTest`、`ClocktowerChambermaidStepMaterializerTest` 可作测试形态参考；每个新边界验证 actor 缺席、不可靠信息、惰性候选、phase 差异。联动 `ClocktowerFortuneTellerPhaseAuthorityTest`、structured numeric/boolean tests、first/other-night flow 和 `ClocktowerNightHostProjectionTest`。最后一个目前只有一个集成场景，不能宣称已经覆盖完整同夜矩阵。

估计 H-R + H-M 可从 Host 移走数百行，具体净减少量取决于新契约；不设置行数验收阈值。若新函数仍需十几个 Host 闭包或一个大 Context，应停止该切片并重新缩小责任。

## 5. App 最值得做的语义收敛：A-S 非自杀恶魔死亡接任

App 1832–1857 自己判断“死亡后至少四人存活、存活且未中毒的 Scarlet Woman 接任”。而 `DemonSuccessionSemantics.resolve(DemonSuccessionContext)` 已有五人阈值、functioning Scarlet Woman、self/non-self death 的共同规则，并有 typed tests。

`TroubleBrewingDemonSuccessionResolver.kt` 的注释明确说明它当前仅接管 Imp 自杀，普通恶魔死亡留在该切片之外。因此这里是**有历史原因、现在值得收敛的重复规则所有权**，不是发现旧切片错误，也不应强行把普通死亡送进自杀专用 resolver。

建议只把非自杀死亡的上下文准备和接任判定导向共同规则 owner，返回 `DemonSuccessionResolution`。App 保留 `setClocktowerActualRole`、session view 发布、A4 失效、records/events 及待确认新恶魔的赋值。

完整调用范围：

- Slayer 杀死恶魔（App 2678 附近）；
- 白天处决恶魔（2893 附近）；
- 夜间非自杀恶魔死亡（3182 附近）。

三者必须共同继承，TB 与 No Greater Joy 都要覆盖。Imp 自杀的特殊攻击/重建路径继续经过当前专用 resolver，它已经委托共同规则；不能因为统一调用外观而丢掉重建前死亡状态处理。

Architecture pre-flight:
- current owner: App 的 promoteScarletWomanIfNeeded + rules 的 DemonSuccessionSemantics。
- proposed responsibility: 非自杀死亡的纯接任判定回归共同 rules 契约。
- authoritative state owner(s): ClocktowerGameSession；App cards 仍为既有呈现镜像，本切片不迁移整套状态来源。
- narrow typed input/output seam: 死亡前人数/死亡事实/有效候选 -> DemonSuccessionResolution。
- keep in current owner / extract: 判定收敛；所有应用副作用保持 App 原位置和顺序。
- reason: 同一个规则不应由 App 与 rules 分别维护。

注意死亡前/后人数：当前 App helper 在死亡已经 materialize 后调用，规则输入叫 `aliveCountBeforeDemonDeath`。不能直接把当前 alive count 传入。也不能把 App 当前首个候选行为无解释地改成其他选择规则。

T0 沿用 `DemonSuccessionSemanticsTest`，补非自杀适配的四/五人、死亡/中毒 Scarlet Woman、无接任者场景；继承夜间 succession/reconstruction/restore tests。该切片的价值是消除规则重复，预计 App 行数降幅很小。

## 6. App 独立维护切片：A-C 角色目录

移动 `troubleBrewingRoles`、完整角色列表、NGJ membership、`clocktowerRolesForScript` 与内聚查询/本地化函数到明确的角色呈现目录文件。复用现有 `ClocktowerRoleLocalization.kt`，不要再创建与其竞争的同名定位责任。列表继续 private，只保留已需要的 internal API。

**修正旧审计的轻描淡写：这里不只有 UI fan-out。** `clocktower/domain/RoleCatalogAdapter.kt` 从 `clocktowerRolesForScript` 转换 `RoleDefinition`，参与规则侧合法域。对这几个目录符号的直接生产引用分布在七个文件（包括定义本身）：

| 文件 | 关系 | 本次要求 |
| --- | --- | --- |
| App | 定义、发牌角色解析、archive lookup | 同行为迁移 |
| Host | 脚本角色、登记角色和显示 | 同行为迁移 |
| `ClocktowerFirstNightInformationRequest.kt` | 首夜语义/候选准备 | 同行为迁移 |
| `ClocktowerRoleLocalization.kt` | RoleId -> 标签 | 同行为迁移 |
| `clocktower/domain/RoleCatalogAdapter.kt` | 目录 -> RoleDefinition | 同行为迁移，不能当纯文案调用忽略 |
| `persistence/RecoveryAppEnvironment.kt` | 恢复角色 lookup | 同行为迁移 |
| `persistence/RecoveryRestorePlanner.kt` | 恢复角色定义/已提交 setup | 同行为迁移 |

标签 extension 还服务发牌、复盘、设置、邪恶信息显示等 UI。任何列表成员、顺序、enName、team 或缺失查找语义变化都属于额外行为变化，不应混入搬迁。

Architecture pre-flight:
- current owner: App 顶层目录 + 现有 RoleLocalization/RoleCatalogAdapter。
- proposed responsibility: 把内聚只读目录从 Compose 应用文件隔离。
- authoritative state owner(s): 无可变状态；validated ruleset authority 仍为 BuiltInClocktowerRulesetCatalog。
- narrow typed input/output seam: script / role ID / language -> 既有角色列表或标签。
- keep in current owner / extract: 同包机械迁移；不合并 ruleset asset 与显示目录。
- reason: 编辑角色数据不再需要打开应用事务代码。

这一步能缩小文件阅读范围，但**不会自动修复 RoleCatalogAdapter 依赖呈现目录的历史方向**。若以后要将所有元数据统一到 validated catalog，应作为另一个 fan-out migration，不能宣称本次搬文件已经实现 engine/UI 解耦。

## 7. 第二轮已完成：H-P 发布记录准备、H-I 登记交互状态

### H-P：先拆展示历史 payload，后审查私密 proposition fallback

Architecture pre-flight（H-P 实施，2026-10-08）：
- current owner: Host 的 `onShowPlayerDisplay.recordHistory` 闭包负责构造事件字段并调用 `onRecordEvent`；`ClocktowerPlayerRevealHandoff` 负责发布效果顺序。
- proposed responsibility: 独立的纯投影函数从已确定的展示步骤生成历史事件的类型、标题、详情和关联玩家名。
- authoritative state owner(s): `ClocktowerNightStepUi` 是本次展示快照，`cards` 提供座位到玩家名的当前映射；事件存储与序号仍由既有回调/仓储拥有。
- narrow typed input/output seam: `displayStep + ordered player names + unreliable flag + language text function -> typed event payload`。
- keep in current owner / extract: Host 保留授权、首夜发布、私密观察、`onRecordEvent` 和打开展示的调用时序；只抽离无副作用的字段投影。
- reason: 修改历史文案或关联座位无需进入 Host 的事务闭包，也不引入新的事件或游戏状态权威。

本切片不修改 `ClocktowerNightStepUi` 共享模型、不调整 `DecisionHistoryRepository.extractSeatNumbers` 的现有正则与集合遍历语义，也不触碰 role-specific 私密 proposition fallback。新增 typed 测试只覆盖投影契约；既有 handoff 测试保护调用顺序。

Host 2900 附近的 `recordHistory` 按 displayKind 生成文案、提取座位并选择事件类型。可提取纯 `display step + cards + reliability + language -> event payload`，Host 继续调用 `onRecordEvent`，保留既有 `performClocktowerPlayerRevealHandoff` 顺序：

```text
authorize -> first-night publication -> private observation -> history -> open reveal
```

现有 `ClocktowerFirstNightPlayerRevealHandoffTest` 已覆盖拒绝、重复打开、顺序和异常中止，可直接继承。新投影测试覆盖中文/英文、EitherOne/Number/YesNo/RoleReveal/Grimoire、actor 与被引用 seat 去重。

`recordReliablePrivateInformation` 的后半段仍从 role-specific 局部变量构造 legacy proposition。应先审计 `displayProposition` 和 structured confirmation 的全部生产路径，再逐步让 materializer 提供完整语义。**不能现在直接删除 fallback**，也不能传一个装着所有角色结果的大 context 给新 helper。

H-P 本地实施：新增 `ClocktowerInformationHistoryPayload.kt`，只投影事件类型、标题、详情和玩家名；Host 仍在 `recordHistory` 回调内调用 `onRecordEvent`。英文六种展示类型、中文主要格式、关联座位去重及误导/不可靠标题由 `ClocktowerInformationHistoryPayloadTest` 覆盖；既有 `ClocktowerFirstNightPlayerRevealHandoffTest` 继续保护副作用顺序。私密 proposition fallback 与共享 `ClocktowerNightStepUi` 均未改动。

对 `ClocktowerNightStepUi` 的文本引用已分布在 24 个生产文件（不是精确调用图计数）。若改变该 shared model，需要重新列全 producer/consumer；本轮建议优先保持模型不变，缩小实际迁移面。

### H-I：登记交互 owner 有价值，但不应成为第一刀

Architecture pre-flight（H-I 实施，2026-10-08）：
- current owner: Host 顶层六个无 key `remember` map，加上 Day 的 Virgin/Klutz 与 Night 的 Spy/Recluse 回调；规则域由 `TroubleBrewingRegistrationDomain` 拥有。
- proposed responsibility: 一个 UI-local `ClocktowerRegistrationInteractionState` 保存登记选择、所选角色和每个 key 的已记录标记，并集中默认角色选择及推荐选项 witness 写入。
- authoritative state owner(s): 新 owner 仅拥有六个暂态 UI map；phase/round、玩家身份、规则合法性、事件序号和存储仍由原 owner 提供。
- narrow typed input/output seam: `registration key + special flag/role/default or display option -> UI selection state`；查询与“首次记录”返回布尔值，Host 决定是否调用 `onRecordEvent`。
- keep in current owner / extract: 在 Host 原来的无条件位置使用无 key `remember` 创建 owner；保留 key 字符串构造、Spy/Recluse 合法性、Virgin 预检、Day/Night 回调顺序和事件文案写入。
- reason: 减少正常登记交互修改所需理解的 Host 范围，同时避免另建游戏事实源或改变 Compose 状态寿命。

本切片不把 Slayer 的局部登记开关、publication migration 的 keyed `remember`、规则裁定或六个 map 之外的暂态状态并入 owner；不更改 R2 对 Host 调用登记控件的边界 guard。

Spy/Recluse 的六个 mutable maps、默认选择、已记录标记、option witness 应用散布在 Host 的初始化、Day controls 和 Night controls。可以形成 `ClocktowerRegistrationInteractionState` 这类责任明确的 UI-local owner；领域合法性继续委托 rules。

需要先冻结以下真实行为：

- 六个 map 当前是无 key 的 `remember`；publication migration 则是 `remember(gameId, round)`。不能抽取时顺手统一生命周期。
- key 当前由 phase/round/ability/subject 字符串组成；typed key 或 seat identity 迁移须与纯搬迁分开。
- 同一 owner 横跨 Chef/Empath/FT/角色查验、Virgin、Klutz；Slayer 另有局部 boolean，不应未经分析强行归并。
- 首次登记记录的判据不相同：Spy 可记录实际身份，Recluse 当前仅特殊登记时记录；保持事件顺序和去重行为。
- `onApplyRecommendedDisplayOption` 会先更新 witness、再记录登记；信息展示还走单独 handoff。不能把这几步无证据地变成一个“自动提交”。

纯 reducer/typed characterization 可降低风险；实际 Compose 保留/重进/切阶段仍需集成/UI 证据。抽取函数应先在 Host 原来的无条件位置调用，避免把交互状态随分支销毁。

H-I 本地实施：新增 `ClocktowerRegistrationInteractionState.kt`，在 Host 原六个无 key `remember` 的位置以一个无 key `remember` 创建 UI-local owner。六个 map、手动默认角色、推荐结果 witness 和每 key 首次记录标记转入 owner；Host 仍判断 `spyCanRegister`/`recluseCanRegister`、保留 Virgin 预检、Red Herring 清理以及所有事件写入时机。`ClocktowerRegistrationInteractionStateTest` 覆盖跨 key 保留、切换默认值、推荐 witness 和记录去重；`RegistrationInteractionRulesTest` 及现有 Host/流程测试继续验证规则门槛。Slayer 局部选择和 R2 登记控件调用 guard 未变。本地 `:app:testFast` 执行 1,444 项、`:app:testFull` 执行 1,451 项，均 0 失败/跳过；`:app:assembleDebug` 成功。远端 CI/R2 仍须独立验收。

## 8. 需要单独规则审查，不能伪装成文件搬迁的部分

| 当前内嵌判断 | 为什么不能简单搬到 presentation 文件 | 建议 |
| --- | --- | --- |
| Host `fortuneTellerMatches` | 同夜有效角色 + Red Herring + Recluse 登记；现有 `healthyResult` 契约要求真实存活 FT，且不直接包含该登记选择 | 未来收敛到共同规则前，先明确 drunk/poison/registration/phase 差异 |
| Host `clockmakerNumber`、`chambermaidWakeRoles` | 后者是角色名单推断“因自身能力醒来”，不是单纯显示布局 | 以规则事实/事件为输入设计 seam；不能直接用“所有显示步骤”替代 waking fact |
| Host Virgin 处决、Artist 可展示答案集合 | 决定合法行为，而非 UI 风格 | 分别建立小型规则契约，typed characterization；不要做大 DayIntent |
| App Slayer 命中、Saint/Klutz/Mayor 与一般胜负判断 | 决定死亡/胜负和续接优先级 | 按能力/终局契约分切片；本轮不修改规则语义 |

这些是代码责任泄漏的证据，**不是本次已证明的玩法缺陷**。若后续发现现有行为错误，先建立真实回归证据，再单独修复；不要在行为保持的 extraction 中悄悄更正。

## 9. 明确维持 NO-GO / 暂缓

- **整个 `onConfirmDay` / `onConfirmNight` / `onConfirmNewDemon` 搬入 executor：NO-GO。** 仍会需要 session、序号、A4、history、cards、文案、outcome、navigation 多路 port，复刻原闭包。旧 R3 的理由仍成立。
- **批量提取 DawnDurableMaterializationState 工厂：本轮不做。** 即使出现五处构造，调用点位于不同 mutation 时刻，字段使用也不同。一个合成前 snapshot 不能复用于整个事务；旧 R3 已评估过同一候选，当前没有足够的新收益推翻它。
- **整体 Recovery state owner / setup-effect owner：暂缓。** 不能把 session state、checkpoint、cards 和 recovery 再缓存到新对象里；保持 `prepare -> reject/cross-session -> apply` 和 `persist success -> A4 release -> rebuild`。
- **只移动 Day 路由：不推荐。** 没有减少参数、规则与状态责任时，只会让读代码多跳一个文件。
- **整套 registry 移入 NightStepBuilder：不推荐。** 近八百行仍需同样大闭包；只按内聚角色/家族移动。
- **把所有 night state 合并为一个可变 checkpoint：不在本轮。** H0 已去掉 13 个重复参数；剩下 `nightStartedState`/`nightStepIndexState` 是写通路，不能当重复只读参数直接删除。
- **把 precompute/A4 effects 放入只在夜晚显示的 composable：不推荐。** 当前初始化位置、取消语义、旧结果防发布和恢复路径都有生命周期约束；收益不足以作为第一轮。
- **删除带 recommendation/legacy 名字的全部字段：禁止按名字操作。** `legacyInformationCandidates`、misinformation 元数据和 precompute readiness 仍有有效候选/展示消费者；RES 删除的是策略权威，不是所有中性候选或兼容呈现。

## 10. 测试、验收与文档校正

这是审计文档改动，未运行 Gradle/Android tests；不冒充新的 GREEN，也不沿用历史 CI 状态声称当前审计经过远端验收。

后续 implementation 采用行为保持路线：现有 tests baseline（需要时）→ 新 seam 仅补有价值的 characterization → focused tests/compile → diff/fan-out 重查。无真实行为缺口时不制造 RED。T1 在逻辑 checkpoint 执行；共享编排变更按 `TESTING_STRATEGY.md` 选择 T2/T3，验收运行 T4 与 GitHub CI/R2。

重点测试面：

| 切片 | 必保留/扩展的证据 |
| --- | --- |
| H-R | RegistrationResultDomain / RegistrationAuthority / RegistrationInteractionRules；新增纯投影行为覆盖 |
| H-M | MaterializerRegistry / Chambermaid materializer、FT phase authority、StructuredInformationPreparation / StructuredFortuneTellerInformationAdapter、production flows |
| A-S | DemonSuccessionSemantics、非自杀适配 characterization、NightTransaction reconstruction/host integration、NightDawn restore/retry |
| A-C | BuiltInClocktowerRulesetCatalog、RecoveryRestorePlanner（TB/NGJ）、角色定义与 lookup 消费者、编译和原值/顺序 diff |
| H-P | FirstNightPlayerRevealHandoff、publication authorization、纯记录 payload characterization |
| H-I | 登记 reducer、阶段/重进/新局状态生命周期、登记与信息记录顺序 |

完整场景维度：FirstNight/OtherNight/Day、TB/NGJ、Beginner/Experienced、healthy/drunk/poisoned、Spy/Recluse、死亡触发、fresh/restored；按各切片实际扇出选择，不要求所有切片重复运行所有昂贵测试。

R2 工作流仍包含要求 `ClocktowerSpyRegistrationDecisionControls` / `ClocktowerRecluseRegistrationDecisionControls` 调用文本出现在 Host 的 assertion。如果 H-I 将交互组件合理搬走，应同步将 guard 改为保护真实 ownership，不能为旧字符串测试留无意义调用。`BuiltInClocktowerRulesetCatalogTest` 还检查 App 内 recovery catalog 的调用拼写；A-C 不需改变该路径，未来 R7 才需要重审。

前一份 `APP_HOST_DECOMPOSITION_REAUDIT_2026-10-07.md` 的状态/尺寸已更新到 RES-5，但若干区段仍引用旧行号、86 参数及已删除 named-policy hooks；A1/A2/H0 的说明也包含“仍待拆”的历史措辞。本报告用当前代码重新定位，**不能按旧区段把已完成项目再做一次**。两个文件的字符数仍与 RES-5 完成报告一致；旧文档行数各多一行是计数口径差异，不是新的代码增长。

本报告不改写已完成 RES-5 的验收结论。第一轮 H-R/H-M/A-C/A-S 随 PR #244 合并；H-P 随 PR #245 合并；H-I 随 PR #246 合并。PR #245 的未解决 P2 文档 review 指出的「H-P 已做却仍写候选」矛盾由本次文档状态修正处理；不是已发现的运行时玩法缺陷。

## 11. 第一轮本地实施记录（2026-10-08）

- **H-R**：新增 `ClocktowerRegistrationResultPresentation.kt`，从 Host 搬出数字、占卜师布尔和角色查验的最终选项投影；Host 仍准备合法 witness 和当前交互选择。增加三项 typed 测试，覆盖重复结果保留当前 witness、proposition seat 和 Spy 特殊登记字段。
- **H-M**：新增 `ClocktowerRecurringInformationStepMaterializers.kt`，Empath 与 Fortune Teller 在首夜和其他夜使用同一 materializer entry；Host 显式保留 Empath 首夜 proposition 与其他夜无 proposition 的原有差异。增加针对可靠信息惰性候选及不可靠 FT 选中座位的 typed 测试。
- **A-C**：将只读角色列表、脚本成员关系、查找及内聚本地化函数迁到 `ClocktowerPresentationRoleCatalog.kt`；目录元素与顺序未变。`RoleCatalogAdapter` 仍消费相同入口。
- **A-S**：App 的非自杀恶魔死亡接任判定调用 `DemonSuccessionSemantics`，保留死亡后人数转换、原角色提交和记录顺序；Imp 自杀专用 resolver 未改。

本地 Android SDK 存在，但项目 Gradle 9.5.0 wrapper 分发包未缓存；已将校验过 SHA-256 的 Gradle/JDK 下载到临时目录，并以临时 truststore 与本地 Maven 缓存绕过本机代理的 Java TLS 问题，不改项目构建配置。新增定向测试及既有 `DemonSuccessionSemanticsTest` 通过；`:app:testFast` 执行 1,435 项测试，0 失败/跳过，Kotlin 主代码和测试代码均编译通过。以上是**本地验证**，尚非远端 CI/R2 或合入验收。

历史注：记录此段时 H-P/H-I 尚未实施；截至 PR #245/#246 合并，两项均已完成并通过完整 CI/R2。不要再次按旧候选路线重复执行。

## 12. 关键本地证据

路径均相对于本文件；具体行号以上述基线为准。

- [App root](../app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt)
- [Host](../app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt)
- [登记规则](../app/src/main/java/com/codex/campboardgamehost/clocktower/rules/TroubleBrewingRegistrationDomain.kt)、[结果去重](../app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationResultDomain.kt)
- [InformationStepBuilder](../app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt)、[侍女 materializer](../app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializer.kt)
- [夜间投影](../app/src/main/java/com/codex/campboardgamehost/ClocktowerNightHostProjection.kt)、[玩家展示 handoff](../app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerRevealHandoff.kt)
- [接任共同规则](../app/src/main/java/com/codex/campboardgamehost/clocktower/rules/DemonSuccessionSemantics.kt)、[自杀 resolver](../app/src/main/java/com/codex/campboardgamehost/clocktower/session/TroubleBrewingDemonSuccessionResolver.kt)
- [RoleCatalogAdapter](../app/src/main/java/com/codex/campboardgamehost/clocktower/domain/RoleCatalogAdapter.kt)
- [RES-5 已完成边界](RES_5_PHYSICAL_MODULE_DEPENDENCY_CONVERGENCE_ACCEPTANCE_2026-10-07.md)、[前次审计](APP_HOST_DECOMPOSITION_REAUDIT_2026-10-07.md)、[R3 NO-GO 原因](archive/checkpoints/d6/R3_TRANSACTION_APPLICATION_VIABILITY_AUDIT_2026-09-09.md)

Developer-memory checkpoint：NONE；不保存可直接从代码验证的当前尺寸或提交状态。
