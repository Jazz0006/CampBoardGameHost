# 第四阶段 D1：删除后的 App / Host 审计

> 历史检查点：后续执行方向已由 [最小 Host 产品合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md) 覆盖。旧规则保留要求及 NEXT 不再适用；本文件保留实施证据。

基线：`9769f8a4`（C2，#319）。本次只读生产源码，更新审计与路线文档；没有拆分生产文件。阶段编号：A/B/C 分别为第一/二/三阶段，C1/C2 是第三阶段内部检查点；审计与拆分明确属于第四阶段 D。D1 不是整个 D 完成。

## 结论

可以开始第四阶段，不需要先补恢复。没有发现必须恢复旧存档才能推进的阻碍。主要风险是：把大闭包搬走却保留所有依赖；拆分 Compose 后改变草稿生命周期；删除旧重建支架时连同真实提交幂等与信息发布顺序一起删掉。

应先做一批有证据的残留删除，然后沿夜间结算、白天交互、夜间信息、AI 请求生命周期逐个收敛责任。不得新增覆盖全局状态的 Controller/Context，也不能把所有 App 回调塞进 Session。

## 当前量化

| 文件 | A 前 `704f7b29` | C2 后 `9769f8a4` |
| --- | --- | --- |
| CampBoardGameHostApp.kt | 4,846 行 / 280,654 bytes | 4,065 行 / 234,187 bytes |
| ClocktowerHostScreen.kt | 3,225 行 / 192,424 bytes | 3,022 行 / 179,448 bytes |
| ClocktowerNightStepUi.kt | 本次不作历史比较 | 1,232 行 / 60,803 bytes |
| ClocktowerGameSession.kt | 本次不作历史比较 | 937 行 / 45,765 bytes |

A–C 累计生产源码增加 443、删除 4,946 行，净删 **4,503 行**；测试增加 352、删除 4,730 行，净删 **4,378 行**（`app/src/main` / `app/src/test`，不含文档）。App/Host 合计只减少 59,443 bytes，说明大部分删除发生于外围支持链，两个核心闭包仍需治理。

Host `ClocktowerJudgeScreen` 有 **90 个参数、40 个 on… 回调**，另有两个 first-night provider 参数。参数数量是耦合信号，不是应创建一个 90 字段参数包的理由。

## 责任与具体证据

以下行号均对应审计基线，后续编辑需重新搜索。

| 范围 | 当前职责 / 耦合 | 处置 |
| --- | --- | --- |
| App 253–340、2313 起 | 配置存储、异步凭据加载、设置 UI | 后置独立配置边界；不是优先搬走少量代码以粉饰尺寸 |
| App 341–374、952–990、1322–1643 | AI 开局建议、续规划、已确认锚点、请求去重、迟到响应检查 | 独立请求生命周期候选；Session 仍拥有事实，接受锚点只在真实 Host 确认后推进 |
| App 1644–2094、2495–2560 | 准备开局、联合规划、可见身份先发、手动接管、酒鬼确认 | 先删无用 provider request；之后开局流程自有小状态，不复用对局事实存储 |
| App 2953–3088、3420–3880 | 新恶魔确认、日末执行、黎明死亡/中毒/继承/阶段推进，以及 UI 效果 | 高优先级：先收缩旧 materialization 中间契约，再建立窄 Session 结算入口；App 只处理导航与展示效果 |
| Host 189–228、483–535 | 首夜信息展示去重、中毒变化失效、候选异步预计算 | 必须保留已展示信息与取消/重试语义；拆分前固定生命周期归属 |
| Host 237–438、568–1188 | 登记合法域、有效夜间状态、各信息角色数值与候选准备 | 接入已有规则/projector，不在新屏幕再实现一套合法性 |
| Host 1189–1759 | 首夜/后续夜晚 materializer registry 和闭包 | 按信息职责拆解，不能把整个 registry 搬走并携带原闭包 |
| Host 1789–2207 | 黎明、白天提名/投票/执行、Slayer/Artist/Klutz | 白天交互应先于夜间准备分流；合法性由规则/Session，界面只拥有草稿和导航 |
| Host 2280–3022 | 当前步骤 AI 匹配、登记选择、玩家信息发布、自动推进、夜间显示 | 保留授权→正式观察→玩家展示→推进的顺序；适合作为后续夜间交互边界 |

当前白天/Dawn 的早返回在夜间候选和 `nightSteps` 构造之后。这里是可见的计算与依赖混合，不等同于已测得性能故障；没有做设备计时。

## 删除候选与保留边界

1. **已确认可删：手动酒鬼选择的 provider request。** App 私有 `PendingTroubleBrewingDrunkSelection.providerRequest` 无任何读取者，无解构消费；三个构造路径分别位于 `beginJointOpeningAtDrunkStep`、`takeOverJointOpeningManually`、`startTroubleBrewingGame`。确认只消费 request 的候选、decision 的合法域与 revision。删字段、三个构造及仅为它建立的上下文；保留 snapshot/decision，因为它们仍验证手动候选。
2. **App 旧模式分支可收缩，不能连带删全仓 Legacy 类型。** 唯一生产 `ClocktowerGameSession.createProduction` 位于 resetDealState，显式 GLOBAL_V1；无旧恢复入口。App `recordEpistemicObservation` 的 LEGACY_LOCAL 分支及旧模式 fallback 已无活跃局来源。先查所有模式读取/无 Session 路由，再移除对局分支；公共认识模型中的 Legacy 测试、序列化与 oracle 消费单独审计，不由此推断全仓无用。
3. **黎明重建支架需要窄化，不准整层无证据删除。** `NightDawnDurableMaterializationPlanner` 的四个 App 调用每次复制存活座位、角色、全量 action/observation ID 集，再返回 stateMutation/actionId 两套信号。当前死亡/身份/中毒/阶段已各自原子提交，App 大量分支只是在拒绝旧部分写入。把实际判重放到 Session 边界后，可退休这些重复中间模型；但稳定 ID、已发布信息不重复、日末清毒早于下一夜等是现役契约。
4. **中毒“RecoveryAuthority”是收缩候选，不是按名称直接删除。** 当前 checkpoint 中毒已经来自 Session 同轮最后 Poison；该 fallback 再读同一时间线（仅 NIGHT）。需证明手动确认、死亡清标记、新恶魔接替、重复夜末、日末 expiry 的等价性，再删除重复参数和 helper。
5. **必须保留：** `NightTransactionRestoreComposition` 被 `ClocktowerNightHostProjection` 实际调用，是同夜有效状态投影；不是磁盘恢复。NGJ 的 Artist/Clockmaker/Chambermaid/Sage/Klutz 有正式脚本来源，不是 TB 无此角色就能删。手动接管与晚到 AI 回应的 stale 检查也不是精确恢复负担。

## 执行路线

| 检查点 | 工作与边界 | 验收证据 |
| --- | --- | --- |
| D2 | 删手动酒鬼 provider request 与 App 无来源的 Legacy 分支；不扩大为全仓格式迁移 | 三种酒鬼候选路径、两种脚本新局/手动流程的调用审计；现有开局/信息发布回归、编译、T1 |
| D3 | 收缩 Dawn/Dusk materialization，Session 窄结算入口统一实际判重；保留既有规则 planner | 已有 Dawn death/Mayor/succession/poison、Dusk chronology、重复确认/原子失败回归；必要时增加真正提交边界集成用例；full CI/R2 |
| D4 | Host 白天/黎明与夜间分流，白天草稿归白天交互；移除相应 App 草稿传递与夜间计算依赖 | 提名、圣女、幽灵票、Slayer、Artist、Klutz 回归；设备验证来回切换/取消/同日重复进入；不能仅用 JVM 声称 UI 生命周期正确 |
| D5 | 夜间信息准备与当前交互分开；AI 请求生命周期随后独立 | 当前有效角色/中毒、信息发布只一次、Previous/重选、新恶魔、手动/辅助/自动同一确认边界；模型替身验证 stale，真实付费验证另需预算 |
| D6 | 再量化所有权、最大文件和理解半径；据结果决定是否继续拆分 | 一个白天投票变更无需读夜间 materializer；一个夜间结算变更无需读 AI HTTP/设置；没有新巨型容器 |

完成结构治理后，才单独列同一份事件记录的覆盖和最小续局工作：初始配置、空确认/失效、投票门槛上下文、阶段进度、已公布信息及可靠本地写入。不得顺手重建精确 checkpoint 存档。D 的原总目标包括这些缺口；上述 D2–D6 是结构治理子步骤，并不代表恢复完成。

## 下一实施前置记录（D2）

Architecture pre-flight:
- current owner: App 私有开局 pending 结构与对局信息写入口。
- proposed responsibility: 删除无消费的请求字段/构造与不可达的旧对局模式分支，不增加职责。
- authoritative state owner(s): 已确认事实仍为 Session；手动候选 decision、开局 pending 生命周期维持原归属。
- narrow typed input/output seam: 保留已有 DrunkAssignmentDecision 确认、EpistemicObservationDraft -> Session 全局提交。
- keep in current owner / extract: 先删除，不为这批小残留另造 owner。
- reason: 这些对象和分支没有现役消费理由；删除先缩小后续开局和发布流程的理解范围。

D3–D5 必须在各自实施前补当时的完整 producer/consumer 清单和窄接口，不把此表当作可以搬运任意闭包的授权。Session 已 937 行，后续行为应通过具名责任边界组织，不能继续默认塞入该文件。

本次证据为本地静态调用/声明/构造搜索、关键流程与现有测试阅读、Git 尺寸和净变化统计。没有运行新测试（未改生产/测试），没有设备或付费模型验证；#319 的既有 CI 不能当作未来拆分的证据。审计不是全仓死代码证明。开发记忆：NONE。
