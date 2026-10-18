# 新对话交接：F15 夜间实际结果确认已接通（2026-10-18）

## 先读与现行授权

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F15/F14/F13/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- 分支 `codex/minimal-host-full-scan-20261018`，Draft PR #334，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F15起点本地/远端 `9f6c4af245b3aa7424e1134ad1789be6520181bc`，docs-only CI #4376 / R2 #3956 SUCCESS；本批代码将提交同一分支并检查精确HEAD完整CI/R2。旧F14 `f776151e` CI #4375 / R2 #3955不能代替本次代码验收；后续docs-only HEAD也不能冒充Android验证。
- F15本地 `:app:testFull :app:assembleDebug` SUCCESS：175 suites、741 tests，0 failure/error/skipped；没有真机或付费API验收。
- Windows工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`，PowerShell，`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr`，Gradle使用gradlew.bat。Mini MCP clocktower是独立旧Linux `/home/opc/repos/CampBoardGameHost`、同名分支但HEAD `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，不得覆盖本地或假设同步。
- 原未跟踪文件原样保留：artifacts/branch-audit-2026-10-04/、artifacts/test-audit-2026-10-04/；docs下2026-10-04的BRANCH_CLEANUP_AUDIT/RESULT、MAIN_SYNC_VALIDATION、TEST_RETIREMENT_AUDIT/CLEANUP；node_modules、package.json、package-lock.json。未创建工作树，未真机/付费API验收，不merge/retarget/force push或删除分支。

## F15 已落实：实际结果独立确认

- 增加NightResultConfirmation界面：明确无人/多名死者、任意座位实际成为Imp、原已确认中毒keep/clear；取消/复核、gameId绑定及双revision过期及防双击。最终实际结果不从攻击/保护/Mayor/Soldier/SW重算，不因合法候选拒绝；新身份沿原私密展示与实际RoleChange确认，Dawn从实际Death名单播报多名。
- Session.commitConfirmedNightDeaths用现有纯Action/Observation函数暂存全批预检后一次publish state，未知/重复/stale/后续记录冲突不留半份结果；原Attack和实际Death分开，不再Death回写attackSeat。NoNightDeaths只表示用户明确无人死亡，JSON/canonical/public/AI delta完整支持，旧历史缺失不反推空结果。角色/毒/保护等不进入公开死亡播报。
- 整删2生产：NightDawnResolutionPlanner、TroubleBrewingDemonSuccessionResolver；旧DemonSuccessor action/合法候选UI和Session/checkpoint候选元数据退出。DawnPoisonCarryIntent移入原PoisonBoundaries，只保留明确keep/clear输入，夜间不按Poisoner死亡/转职自动推导清除。10个仅保护旧规划/DTO的测试文件删除，真实Session/AI/保密/stale/幂等测试保留，新增5个实际合同用例。
- 生产净删642行/30217B、测试净删1300行/47578B，共1942行/77795B。当前883跟踪文件，241生产+178测试Kotlin；源码/测试/工具配置475项、116已审计（24.42%）、0审计中、359待审。每路径状态及删除历史只维护在原台账F12/F15。
- App159449B、HostScreen99682B仍超50,000B；Session42993B、新界面4673B未新增超限。P1/P4未完成。U1无生产入口的自定义导入仍待所有者决定，本批无新增决策阻塞。

## 下一任务：恢复全文件横向语义审计

**所有者再次明确：全文件遍历仍约20%进度，后续必须继续完成。F15已完成本步后，下一批优先处理现有台账的359个未审项，不能继续只沿夜间局部链或拿代码净删当审计完成。** 保持唯一现行台账，横向覆盖epistemic/codec、catalog/setup、平台/transport、tools/CI和测试，全文读取并验证真实消费者后再给最终分类；不重读已审文件冒充新进度。再依据横向结果选规模大、证据闭合的一组退休。

F15未清空所有Host Rules：DemonAttackProductionAdapter仍仅给现有信息步骤作只读死亡预览，Sage/Ravenkeeper触发/毒展示、日间自动继承/终局、Klutz/日落清除及旧Flow模板仍有消费者。最终实际死者可与早前预览不同，后续UI补充信息与触发流程仍需审计及真机验收。新NoNightDeaths的跨项目消费者仍需完整合同复核；不要声称已做外部服务/付费/真机验证。夜间已退出旧规则顺便自动判终局，实际结果提交后允许继续；日间终局规则尚在。

现有AI开局/pair/数字/后夜、prompt/真实IO/parser、strategyMemory/preselection及双模式信息采纳必须保护；不恢复已删除web/world引擎或独立causal journal。每批报告实际删除、移除链、净体量、超限、Host事实/事件风险、精确HEAD CI/R2和未知所有者项，保持Draft堆叠、不merge/retarget/force push，原未跟踪文件不动。
