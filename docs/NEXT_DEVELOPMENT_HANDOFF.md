# 新对话交接：解耦完成，下一步全模块逐文件审计（2026-10-18）

## 已获所有者授权的顺序

1. 先完成信息真值/登记投影、Mayor 候选链解耦。本轮 F11 已实施并通过本地完整回归；远端精确代码验收待更新。
2. 切换新对话后，遍历全仓代码并逐文件形成有证据的分类；此轮不以新功能、重建 Rules 或删除现有 AI 替代审计。

## 读取顺序与唯一记录位置

先读 AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，再读现有 MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md 的 F11。分类明细继续维护在这份台账中，不复制第二份库存或 NEXT。历史 TSV 不是已完成的语义审计清单。

## 精确基线（新对话必须重新核实）

- 分支 `codex/minimal-host-full-scan-20261018`，PR #334 保持 Draft，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F11 起点 `f2c09e175e8e70e2394594806ee7e969d285a996`，代码提交与完整 CI/R2 待更新。最近旧代码基线 `800f1d97` 的完整 CI #4366/R2 #3946 SUCCESS；docs-only 成功不能代替真实编译。
- F11 本地 `:app:testFull :app:assembleDebug` SUCCESS：819 tests、0 failure/error/skipped。存量 905 跟踪文件，246 生产 +195 测试 Kotlin；净删 4678 行代码，整删 10 生产+11 专属旧测试。
- 超限 App 178170B、HostScreen 101971B；NightStepUi 44505B。两个超限仍须在后续职责清理中解决，不得宣布 P4 完成。
- Windows 工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`。JDK 为 `C:/Program Files/Android/Android Studio/jbr`，本地 Gradle 用 gradlew.bat。Mini MCP clocktower 是独立旧 Linux checkout `a2cbcac8`，不拿其状态覆盖本地。
- 用户未跟踪文件需原样保留：artifacts 下 2026-10-04 审计目录、docs 下同日审计/同步记录、node_modules、package.json、package-lock.json。没有创建工作树，没有真机/付费 API 验收，不 merge/retarget/force push。

## F11 已完成的职责边界

- 当前信息问题只携带指标/实际目标，不携带 Host 算出的答案。Chef/Empath/Clockmaker/Chambermaid 走直接数字，FT 保留实际已选 pair，Undertaker/Ravenkeeper 保留实际查询目标和所给角色。
- 删除 Host 真值/登记 witness producer → renderer/domain 的完整旧链，保留日间明确登记选择。原私密公布、Session 单 writer、实际事件顺序、去重/原子性、stale-response 与 recipient 保护保留。
- Mayor AI 请求不提供合法候选；直接 seat 建议通过身份/版本边界，实际 ActionFact 写一次；退出候选确认和重复候选裁定日志，确认到 Demon 也不被旧门禁重定向回 Mayor。实际死亡/继承/角色变化继续沿原 Session/Dawn 路径应用。
- 真实 AI 请求/提示词/模型通信/解析、首夜联合与后夜推荐、记忆、预选和辅助/自动模式保留。没有调用收费模型。

## 下一轮逐文件审计标准

范围先从最新、未截断 Git tree 明确列出所有生产源码、测试、构建配置、脚本和工具；文档/生成物/素材单独说明纳入或排除，不能用全部文件数冒充源码审计分母。已删除文件作为完成历史，不反复作为当前未审项。

| 最终分类 | 必填证据 |
|---|---|
| 必须保留 | 对应 Host 控制、记录或 AI 推荐；必要平台/传输/安全支撑注明服务对象，核实真实消费者 |
| 必须删除 | 职责为何退休、全部消费者、专属测试与删除风险；无消费者证据不能只靠名称判断 |
| 需要解耦 | 混合文件应保留/应删除职责、调用与事实边界、具体断开方案 |
| 需要人工审核 | 具体产品决策、现状证据、删除影响、推荐选项 |

每个文件另记待审/审计中/已审计、证据 HEAD、消费者和验证情况。未检查的维持待审；混合文件允许多功能归属。完成率只统计有证据最终结论的文件，不把扫描100%、代码净删率或曾碰过文件当语义审计完成率。

优先横向覆盖尚未完整审核的 Host UI/根组件、Session/记录、活跃 AI、setup、flow/rules、catalog/persistence/debug、工具/CI和跨项目 codec。特别核查 Sage、Spy 显示分支、日间后果、其它行动/继承/发牌门禁、剩余展示候选/Provider DTO、因果日志/重复历史；F11 不等于这些都已完成或可整删。不要重建已删除 web/world 引擎，不要误删工作中的 AI。

## 可直接粘贴给新对话的指令

读取 AGENTS.md 和当前交接/路线/测试策略，核对远端 PR #334 最新 HEAD、CI/R2 与本地状态。F11 的信息真值/登记投影和 Mayor 候选链已解耦；现在按全 Git 树逐文件审计，在现有全源台账分类为必须保留（Host 控制/记录/AI 推荐/必要支撑）、必须删除、需要解耦、需要人工审核，并记录待审状态和真实完成率。保护现有 AI、唯一 Session、事实/隐私/时序，先形成有证据的分类与整组处理建议，不沿单条旧调用链代替全仓遍历。
