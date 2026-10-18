# 新对话交接：F12-G Formal世界镜像清除及继续全源审计（2026-10-19）

## 先读与现行授权

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F12-G/F12-F/F12-E/F15/F12-D/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- **F12-G最新代码证据**：在已同步的MiniMCP `clocktower` 分支`codex/minimal-host-full-scan-20261018`实施，起点`4da03849`，第一代码提交`0d8f145b`删旧Formal世界模型/JSON/时间线绑定及专属测试。首轮CI #4383因误删Grimoire immutable-list使用的`java.util.Collections` import编译失败；精准修复提交最终代码 HEAD **`8644e7575b67d9f3bef6cc269c7c93bca4332bf1`** 已推送。GitHub独立比较7文件**+8/-737（净删729行）**。完整 **[CI #4384 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056035)**（full Android单测+debug APK、ASP、真实Clingo、CI gate均通过）和 **[R2 #3964 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39166056032)**。该精确代码之后本交接文档另单独提交，下一轮按GitHub PR实时head/base/CI核对，不把docs-only CI冒充代码验收。
- **F12-G进度**：唯一台账 **177/469（37.74%）已审、292待审、0审计中**，7个原待审epistemic测试全文审计并分类，删1个原待审Formal旧测试，原有4个已审生产文件只做复审/代码缩减不重计。事实/全球事件/recipient保密、唯一Session、现用AI开局/后夜/策略记忆与模式未改。
- **下一组明确任务**：首先核对并迁移`PlayerHistoricalTimeline.project`中独有的公开历史/recipient隐私断言到现用生产历史结构，再完整删除该只在测试消费的旧投影与旧专属测试。继续横向剩余292文件，随后攻Mayor及FT单候选Host规则门禁、Sage/Ravenkeeper预览规则、TB setup rotation评分与App/HostScreen超50000B清理。保留跨项目真实合同，未知功能U1在所有者定夺前不能删除；不merge、retarget、force-push Draft #334。

- **本轮F12-F代码已推送**：分支`codex/minimal-host-full-scan-20261018`，Draft #334（base SHA 64be3090），代码提交**`e6faea27bb28a571b45669bf2357af0b11e4f863`**（从56975956前进）。实际净删2个3B2/3B3 Python实验脚本、2个专属测试、1个过时P1分支工作流及主CI中调用旧实验测试的6行；GitHub compare 6文件0新增/1087删除行，旧台账文件大小估计共52,165B。未改Android生产、正式AI请求/传输/解析/战略记忆或Session事实。代码级[full-ci] **[CI #4381 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379709)**（full Android JVM+Debug APK、ASP、Clingo、gate均SUCCESS）、**[R2 #3961 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39164379711)**，已核精确HEAD和Android job steps。后续docs-only HEAD不可冒充代码编译证明；新对话仍应重新核对GitHub实时状态。
- **全源审计**：F12-F新增32项保留路径的首次全文语义结论，连同旧5个已退休路径从现行目录表移除。当前**170/470（36.17%）已审、300待审、0审计中**；470路径每项唯一。审核结论只维护在原台账F12-F与原表，不另建库存。当前代码后还将另有本文档提交，最新Git HEAD必须重新读取。
- **下一组重点**：epistemic内部旧FormalGameState/PlayerKnowledge与相关JSON可优先隔离，现用InformationProposition、RecordedObservation、TimelinePoint、global AI history/隐私必须保留；孤立PlayerHistoricalTimeline只在迁移高价值公开历史测试后删。setup真实TB/NGJ发牌、Drunk已选身份、AI模式/密钥入口不能误删，跨局rotation/候选评分宜整组缩减；Mayor旧合法单选、Sage/Ravenkeeper触发和日间自动终局仍有生产消费者，继续P1。TBGS快照JsonCodec在Android生产无调用但跨EvidenceLab/WebHost契约未核，不贸然删除。

- **当前F12-E状态**：MiniMCP `clocktower` 干净分支从`a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`精确快进到远端`2ce0e897898e8f040c3bef8dcbda20186e2e4689`，随后全文审计原待审16项，唯一台账 **138/475（29.05%）已审，337待审**；本批只修改文档、不改生产代码。当前HEAD之后的本交接/台账文档提交号需下次实时核对。GitHub PR #334保持Draft，base仍64be3090。同步后CI #4379/R2 #3959成功但Android跳过；代码完整Android证据仍是0f904f70 CI #4377/R2 #3957。
- **历史注意**：下文旧Windows/Linux不同步状态是F12-D交接时的历史现场；本轮已在Linux开发机快进，不可误用旧a2cbcac8作为当前HEAD。

- 分支 `codex/minimal-host-full-scan-20261018`，Draft PR #334，base `64be30905b88f639a464204ed1db89b7b62bc910`；main `61cd6796569ed8dfdea360caefeb5b9328745090`。
- F15起点本地/远端 `9f6c4af245b3aa7424e1134ad1789be6520181bc`，docs-only CI #4376 / R2 #3956 SUCCESS；精确代码 **`0f904f7031770bd2f12acea4299aa9b1d5c8e5c0`** 的完整 Android **[CI #4377 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759682)**（full单测+Debug APK）、**[R2 #3957 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759696)**；ASP、真实Clingo、CI gate均SUCCESS。 远端compare核实51文件差异，PR仍Draft/base未变；后续docs-only HEAD也不能冒充Android验证。
- F15本地 `:app:testFull :app:assembleDebug` SUCCESS：175 suites、741 tests，0 failure/error/skipped；没有真机或付费API验收。
- Windows工作区 `C:/Users/Jazz_/source/repos/CampBoardGameHost`，PowerShell，`JAVA_HOME=C:/Program Files/Android/Android Studio/jbr`，Gradle使用gradlew.bat。Mini MCP clocktower是独立旧Linux `/home/opc/repos/CampBoardGameHost`、同名分支但HEAD `a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`，不得覆盖本地或假设同步。
- 原未跟踪文件原样保留：artifacts/branch-audit-2026-10-04/、artifacts/test-audit-2026-10-04/；docs下2026-10-04的BRANCH_CLEANUP_AUDIT/RESULT、MAIN_SYNC_VALIDATION、TEST_RETIREMENT_AUDIT/CLEANUP；node_modules、package.json、package-lock.json。未创建工作树，未真机/付费API验收，不merge/retarget/force push或删除分支。

## 转入开发机 MiniMCP 的接续说明（2026-10-18 同步）

本轮所有者要求同步文档并转入新对话，后续在开发机通过 MiniMCP 继续；本次只整理交接，不新增生产改动，也不提前启动下一批审计。

- 本次同步前远端/Windows HEAD 均为 **`8f8f9573d452b92cdd727fc7fd98906a1446cb26`**（F15验收记录及F12-D横向审计）。该文档提交的 [CI #4378 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39122159425)、[R2 #3958 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39122159339) 已复核；它们是docs-only检查，完整Android代码证明仍是上节 `0f904f70` 的 CI #4377 / R2 #3957。本节所在提交是后续交接文档提交，进入新对话必须从GitHub重新取得最新HEAD及其检查，不能把此处基线当最新HEAD。
- 本次只读实查 MiniMCP `clocktower`：目录 `/home/opc/repos/CampBoardGameHost`，分支 `codex/minimal-host-full-scan-20261018`，HEAD **`a2cbcac82b457b5a56abf3833b604d9c4fdd74f7`**，index tree `4d2cdd8b643f23520222a1d9ce540941d3c6b370`，工作区干净。此状态尚未快进到本轮远端；所有expected HEAD/index/worktree版本令牌进入新对话后都必须重取，不复用本次快照。
- 接续顺序：先用GitHub检查main、Draft #334的实时head/base和对应CI/R2；再用MiniMCP `git_state`、`git_remote_status`核对开发机。确认无本地改动及未发布提交后，`git_fetch`获取配置远端，重新读取refs/state，使用精确预期HEAD与目标SHA执行同名分支 `git_fast_forward`。若不是可安全快进，先检查分叉/本地差异；不要reset、强推或覆盖工作区。同步后再次 `git_state`，确认开发机HEAD等于GitHub的最新远端HEAD，再全文读取本交接、权威架构、路线、测试策略和原审计台账。
- 本次未切换或修改MiniMCP开发机分支/文件；Windows原有未跟踪文件没有提交或迁移。开发机Android/JDK/Gradle能力需实查，不能套用Windows JAVA_HOME或声称开发机已运行741项测试；当前独立Android接受证据来自精确代码HEAD的GitHub完整CI。
- F12-D历史交接建议当时是**继续原台账的横向全文件语义审计**：当时 **122/475（25.68%）已审，353待审**，先从未审epistemic/codec、catalog/setup、平台/transport、tools/CI及测试选一组，全文读取并验证真实消费者，更新原表；不是重复已审夜间链，也不是重做F15。下一批再依据审计证据选择完整删除职责链。全部现用AI必须保留，两个超大文件和剩余Host Rules问题见下文。

## F15 已落实：实际结果独立确认

- 增加NightResultConfirmation界面：明确无人/多名死者、任意座位实际成为Imp、原已确认中毒keep/clear；取消/复核、gameId绑定及双revision过期及防双击。最终实际结果不从攻击/保护/Mayor/Soldier/SW重算，不因合法候选拒绝；新身份沿原私密展示与实际RoleChange确认，Dawn从实际Death名单播报多名。
- Session.commitConfirmedNightDeaths用现有纯Action/Observation函数暂存全批预检后一次publish state，未知/重复/stale/后续记录冲突不留半份结果；原Attack和实际Death分开，不再Death回写attackSeat。NoNightDeaths只表示用户明确无人死亡，JSON/canonical/public/AI delta完整支持，旧历史缺失不反推空结果。角色/毒/保护等不进入公开死亡播报。
- 整删2生产：NightDawnResolutionPlanner、TroubleBrewingDemonSuccessionResolver；旧DemonSuccessor action/合法候选UI和Session/checkpoint候选元数据退出。DawnPoisonCarryIntent移入原PoisonBoundaries，只保留明确keep/clear输入，夜间不按Poisoner死亡/转职自动推导清除。10个仅保护旧规划/DTO的测试文件删除，真实Session/AI/保密/stale/幂等测试保留，新增5个实际合同用例。
- 生产净删642行/30217B、测试净删1300行/47578B，共1942行/77795B。当前883跟踪文件，241生产+178测试Kotlin；F15完工时475项、116已审计；之后F12-D已新增横向6项，当前122已审计（25.68%）、0审计中、353待审。每路径状态及删除历史只维护在原台账F12/F15。
- App159449B、HostScreen99682B仍超50,000B；Session42993B、新界面4673B未新增超限。P1/P4未完成。U1无生产入口的自定义导入仍待所有者决定，本批无新增决策阻塞。

## 下一任务：从F12-G继续未审292项横向语义审计

**所有者再次明确：全文件审计必须继续。F12-D/E/F分别新增6/16/32项，F12-G本轮又完整审阅7项测试并退役1项原待审文件；现有台账177/469、292待审。下一批从292项继续，不以局部夜间路径和净删行数充当全仓审计。** 保持唯一现行台账，横向覆盖epistemic/codec、catalog/setup、平台/transport、tools/CI和测试，全文读取并验证真实消费者后再给最终分类；不重读已审文件冒充新进度。再依据横向结果选规模大、证据闭合的一组退休。

F15未清空所有Host Rules：DemonAttackProductionAdapter仍仅给现有信息步骤作只读死亡预览，Sage/Ravenkeeper触发/毒展示、日间自动继承/终局、Klutz/日落清除及旧Flow模板仍有消费者。最终实际死者可与早前预览不同，后续UI补充信息与触发流程仍需审计及真机验收。新NoNightDeaths的跨项目消费者仍需完整合同复核；不要声称已做外部服务/付费/真机验证。夜间已退出旧规则顺便自动判终局，实际结果提交后允许继续；日间终局规则尚在。

现有AI开局/pair/数字/后夜、prompt/真实IO/parser、strategyMemory/preselection及双模式信息采纳必须保护；不恢复已删除web/world引擎或独立causal journal。每批报告实际删除、移除链、净体量、超限、Host事实/事件风险、精确HEAD CI/R2和未知所有者项，保持Draft堆叠、不merge/retarget/force push，原未跟踪文件不动。
