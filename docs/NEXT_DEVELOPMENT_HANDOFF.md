# 新对话交接：F13 日间裁定重复历史已退休（2026-10-18）

## 先读与现行授权

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F13/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- 分支 `codex/minimal-host-full-scan-20261018`，Draft PR #334，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F13实现代码 **`27d02f723340ff88c466d170655898be768d3ae3`**；远端compare确认相对起点`d76968168eb02e37cd87084bb51eabc60f789d9c`只有本批26文件差异。精确代码 **`27d02f723340ff88c466d170655898be768d3ae3`** 的完整 Android **[CI #4373 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071885)**（full单测+Debug APK）、**[R2 #3953 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071868)**；ASP、真实Clingo、CI gate均SUCCESS。 后续docs-only HEAD不能替代Android编译证明。
- 本地 `:app:testFull :app:assembleDebug` SUCCESS：190 suites、807 tests，0 failure/error/skipped。F11旧完整远端验收是`d9fb47f2` CI #4369 / R2 #3949；F12-A/B仅审计文档，不用其CI证明F13。
- Windows工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`，PowerShell，`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr`，Gradle使用gradlew.bat。Mini MCP clocktower是独立旧Linux `/home/opc/repos/CampBoardGameHost`、同名分支但HEAD `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，不得覆盖本地或假设同步。
- 原未跟踪文件原样保留：artifacts/branch-audit-2026-10-04/、artifacts/test-audit-2026-10-04/；docs下2026-10-04的BRANCH_CLEANUP_AUDIT/RESULT、MAIN_SYNC_VALIDATION、TEST_RETIREMENT_AUDIT/CLEANUP；node_modules、package.json、package-lock.json。未创建工作树，未真机/付费API验收，不merge/retarget/force push或删除分支。

## F13 已落实的完整链

- 整删3生产文件：DayAbilityRegistrationRulingProducerV1、StorytellerCausalDecisionJournalV1、DecisionOutcomeSnapshot；3旧测试路径退出，其中Mayor安全用例迁至MayorRedirectBoundaryTest保留。生产净删800行/43958B、测试净删338行/16469B，共1138行/60427B。
- 删除日间provider候选/frozen snapshot/PriorDecision/ability-truth推导及Captured/Committed/Corrected第二历史；Provider的DayAbilityRegistration context与priorDecisions字段退出。Mayor context由既有Session/history builder读取。
- SlayerShot.targetRegistersAsDemon / Nomination.nominatorRegistersAsTownsfolk直接记录已明确Boolean，和公开动作共享一次Session提交。null未知、false明确否；不把恶魔登记推成Imp/邪恶阵营，不把镇民类型推成好人阵营，不由命中/处决反推登记。
- JSON兼容缺失字段且未知值仍省略，canonical payload只为明确值扩展；AI delta读取唯一事件，玩家回放继续隐藏登记。完整回归覆盖编解码、重复/变更ID/stale原子性、delta及每玩家隐私；现用AI与实际死亡/执行操作仍保留。
- 补齐8个Session全文审计，其中2项随实现删除、6项保留/待解耦；不要重读当新增进度。当前900跟踪文件，243生产+193测试Kotlin；源码/测试/工具配置审计范围492项、111已审计（22.56%）、0审计中、381待审。全Git tree与台账逐路径核对无缺漏/重复。删除历史及分母497→492证据见原台账F13。
- App172872B、HostScreen101994B仍超50,000B；P1/P4未完成。U1无生产用户入口的custom/homebrew导入分支待所有者决定，推荐退休；未决前保留，不阻塞其他组。本批无新增待决项。

## 下一组：实际死亡 → 继承 → 新身份 / Dawn

先明确实际结果确认输入及canonical Death/RoleChange/Session单writer，再删除NightDawnResolutionPlanner中的attackOutcome/demonSafeSeats/继承候选/毒生命周期推导、RestoreComposition预测confirmedEvents和SuccessionResolver旧计算。真实消费者有App、DemonAttackProductionAdapter、CurrentDemonAuthority、NightHostProjection，逐项核查专属测试。保留必要状态应用、事件关联、隐私、时序及继续游戏；不重建大而全facade或兼容archive。

Slayer命中/Virgin后果及UI登记展示条件仍含旧规则，F13只退休登记裁定/重复历史链。然后横向继续epistemic/codec、setup、工具/CI和测试；Sage/Spy、毒/终局、发牌门禁仍需完整职责处理。两个超限根组件已全文审计，但还没有完成职责净删和体积要求。

每组必须报告实际生产/测试删除、断开链、净代码体量、超限文件、Host事实/事件风险、精确HEAD的CI/R2及未决所有者项。测试随职责退役，不能用旧绿灯要求恢复Host合法性引擎；真实AI与Session/security测试保留。
