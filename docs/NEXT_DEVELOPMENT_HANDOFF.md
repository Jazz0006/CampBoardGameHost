# 新对话交接：F14 夜间影子重建已退休（2026-10-18）

## 先读与现行授权

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F13/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- 分支 `codex/minimal-host-full-scan-20261018`，Draft PR #334，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F14起点本地/远端 `ae54b58e28b7abe180c98dadf3abdd3630af0bcb`，docs-only CI #4374 / R2 #3954 SUCCESS；本轮代码将提交同一分支，精确代码HEAD完整CI/R2须重新核实。上一完整代码基线F13 `27d02f723340ff88c466d170655898be768d3ae3` 的CI #4373 / R2 #3953 SUCCESS不能代替F14。后续docs-only HEAD也不能冒充Android验证。
- F14本地 `:app:testFull :app:assembleDebug` SUCCESS：185 suites、761 tests，0 failure/error/skipped。没有真机/付费API验收。
- Windows工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`，PowerShell，`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr`，Gradle使用gradlew.bat。Mini MCP clocktower是独立旧Linux `/home/opc/repos/CampBoardGameHost`、同名分支但HEAD `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，不得覆盖本地或假设同步。
- 原未跟踪文件原样保留：artifacts/branch-audit-2026-10-04/、artifacts/test-audit-2026-10-04/；docs下2026-10-04的BRANCH_CLEANUP_AUDIT/RESULT、MAIN_SYNC_VALIDATION、TEST_RETIREMENT_AUDIT/CLEANUP；node_modules、package.json、package-lock.json。未创建工作树，未真机/付费API验收，不merge/retarget/force push或删除分支。

## F14 已落实：选择不再投影成死亡或角色变化

- 删除NightTransactionRestoreComposition生产文件；混合EffectiveNightState文件删除伪MechanicalDeath/RoleChanged和StateProjector，只留Dawn所用窄只读值类型及尚有OptionalNightSourceChronology消费者的cursor排序。
- NightHostProjection删除confirmedEvents/canonicalNightReconstruction、重复alive/role映射与无消费者字段；Chambermaid、Empath和能力身份/存活读取canonical Session状态。没有新增重建器、事件store或第二可写状态。
- 5个影子重建专属测试文件退出；混合EffectiveNightStateTest中4个实际cursor排序/身份用例保留并重命名NightChronologyTest。NightHostProjectionTest保留Ravenkeeper入口与旧cards边界，并新增选择/返回/草稿不造死亡角色、实际Death/RoleChange及幂等贯穿用例。既有AI/Session/安全、独立Demon规则测试保留。
- 生产净删318行/15571B、测试净删1238行/51061B，共1556行/66632B。当前894跟踪文件、242生产+188测试Kotlin；审计486项、112已审计（23.05%）、0审计中、374待审。完整逐路径及删除历史仍在原台账，不把本批重读已审生产文件算新增进度。
- App172872B、HostScreen101202B仍超50,000B；P1/P4未完成。U1自定义导入范围待所有者决定，本批无新增待决项。
- F13已并入SlayerShot.targetRegistersAsDemon / Nomination.nominatorRegistersAsTownsfolk的日间明确登记，不重建独立causal journal。F11信息/Mayor候选解耦仍保留。现有AI未停用；已删除的web/world引擎不重建。

## 下一组：实际夜间结果 / 继承确认，退出提交端规则

当前App.onConfirmNight仍用DawnDeathFacts→NightDawnResolutionPlanner.planValidatedNightDeath决定死亡，继承仍有Forced/Choice、App自动promote及合法候选确认限制。下一批先接明确实际结果和继承输入，保留Session Death/RoleChange、公开告知与私密新身份的原子性和事件顺序，再删除这条规则决策链及专属测试。不要把F14删除影子投影误认为这些提交门禁已全部移除，也不要未经公开告知就提前泄漏尚未宣布的夜间结果。

真实消费者仍有App、DemonAttackProductionAdapter、CurrentDemonAuthority、NightHostProjection。PoisonEffectLifecycle/planPoisonCarry、Ravenkeeper/Sage预览和eligibility、Slayer/Virgin后果/登记展示条件及终局仍待处理；继续横向epistemic/codec、setup、工具/CI和测试，不沿一条调用链冒充全树审计。

每批报告真实生产/测试删除、移除链、净体量、超限、Host事实/事件风险、精确HEAD的CI/R2及未知所有者项。保留必要继续游戏与真实AI，不重建大而全facade、兼容archive、旧合法性验证器或第二写入状态；不merge/retarget/force push。
