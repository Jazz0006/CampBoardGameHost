# 当前开发路线（2026-10-18，F12 审计进行中）

## 当前状态

所有者最新顺序：先解耦信息真值/登记投影与 Mayor 候选链，再在新对话做全模块逐文件审计。F11 已实施；本地完整 testFull + Debug APK SUCCESS（819 tests，0 failure/error/skipped），代码 **`d9fb47f2e805339736f6a70cd3b347090810bea9`** 的完整 Android **[CI #4369 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071137)**（完整单测+Debug APK）、**[R2 #3949 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39116071147)**，ASP/真实 Clingo/CI gate SUCCESS。现用 AI 保留，P1 未全面完成。

F11 整删 10 生产+11 专属旧测试；生产净删2827行、测试净删1851行，共4678行。当前905跟踪文件，246生产+195测试Kotlin。App178170B、HostScreen101971B仍超限；NightStepUi44505B。详细消费者、实际删除、风险与验收只见现有全源台账F11，下一步入口只见NEXT_DEVELOPMENT_HANDOFF。

## 最终职责

- Host：唯一 canonical Session/GameState、实际行动与公布信息、结构化事件、必要手工控制与继续游戏；只保护身份/结构、版本、去重/原子性、时序、隐私，不能以游戏规则否决确认事实或从信息反推未选登记。
- AI 推荐：保留开局/后夜、联合规划、真实 API/提示词/解析、战略记忆、预选、AI_ASSISTED/AI_AUTOMATIC；直接读取 canonical facts/events，不接受 Host 的合法候选菜单。
- Rules：以后独立只读重建，不能写事实或成为提交许可。已删除的旧 web/world/candidate 引擎不重建；当前不为清理重新创造规则包装器。

## 当前阶段：全模块逐文件审计

新对话先重新核对远端、本地、PR/CI，再以最新完整 Git tree 定义源码/测试/配置/工具范围，按四类出具证据结论：必须保留（Host 控制/记录/AI 推荐/必要平台安全支撑）、必须删除、需要解耦、需要人工审核。未审查文件另记待审，不强行归类。只在原全源台账维护明细、证据 HEAD、消费者及验证，不能另建重复库存。

F12 已从当前完整905文件 Git tree 明确497个源码/测试/配置/工具文件范围，在原全源台账逐路径记录状态；A批71项，B批新增34项（30生产+4测试、478849B），累计 **105/497（21.13%）**，0审计中、392待审。App/HostScreen两个超限根组件已完成整文件职责审计，Session目录25个文件（含GameSession）的当前职责和消费者已核查；8个Session文件仍待审，不以引用扫描代替全文审计。当前仅改审计文档，未删产品代码；B批16项已有离线AI wire、Session原子提交/角色变更及只读cards测试通过，完整代码基线仍为F11。全树清点100%和生产文件净减率都不是语义审计完成率。

首批发现：孤立 PairInformationOutcome 可删；攻击/继承/毒/Klutz、flow eligibility、Sage默认真值/pair、Provider候选及causal元数据须解耦；文本/内容身份、canonical事实、时序和密钥边界保留。U1人工审核项是无生产用户入口的custom/homebrew导入分支，建议退休未上线分支而保留两内置脚本解析，未决前不删除。明细和证据仅在台账F12维护。

B批进一步确认：App/HostScreen混合真实操作与Slayer/Virgin/继承/Dawn/终局计算，UI同夜同recipient拒绝不同信息与同事件ID去重必须区分；NightTargets清除/毒生命周期及UI实际语义仍需唯一事件收敛。现用开局/续规划及pair/scalar/Mayor请求工厂不发送Host合法候选，保留其prompt/transport/schema/memory与stale边界。下一批补齐8个待审Session文件，再横向epistemic/codec、setup、工具与测试。

剩余重点：Sage真值/pair与truth标签、Spy魔典/poison分支、Virgin/Slayer日间后果、其它行动/死亡/继承/发牌门禁、剩余展示候选/Provider DTO、因果日志/重复状态历史、跨项目codec/工具CI。含规则的混合文件不能整包删除必要操作和状态应用。人工审核项必须列具体决策证据与风险，不以不确定为由随意删除，也不把所有当前消费者默认为最终保留。

## 阶段验收与治理

R0（AI恢复）已完成；P1规则门禁删除/解耦持续推进，F11不是全P1完成。后续依据审计落实纯死代码/实验退出（P2）、唯一状态事件收敛（P3）、UI职责与全部源码<=50000B（P4）、全树/跨项目合同验收和独立只读Rules规划（P5）。

本地验证 testFast；完整检查点 testFull + assembleDebug 及精确代码HEAD的GitHub完整CI/R2。只退休旧规则正确性测试，保留实际AI、Session、时序、隐私、stale及原子性测试；未经实际执行不声称真机或付费API已验收。Draft #334继续堆叠，不合并/retarget/force push；原有未跟踪文件不动。
