# 新对话交接：F12-L 已审186/451，继续剩余旧规则与 UI 清理（2026-10-19）

## 先读与现行授权

先读AGENTS.md、HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md、CURRENT_DEVELOPMENT_ROADMAP.md、TESTING_STRATEGY.md，然后读原MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md的F12-K/F12-J/F12-I/F12-H/F12-G/F12-F/F12-E/F15/F12-D/F12。唯一逐路径状态维护在原台账，不另建库存或NEXT。

所有者授权按完整职责链加速实际删除，同时继续全树横向审计。保留Host唯一事实/事件/安全提交及现有开局、pair、数字、后夜AI、prompt/transport/parser/memory/preselection和AI模式。不能让Host规则候选阻断确认事实，不能以退休适配器为由关闭AI。

## 精确代码与环境基线（每次进入必须重查）

- **最新F12-L代码事实**：起点MiniMCP `clocktower`与远端clean/equal `b4c979ac143d25c455e0d7011705f0800cd1690f`；分支`codex/minimal-host-full-scan-20261018`，PR #334 Draft且base `codex/minimal-host-m1c-direct-number-20261018`（SHA `64be3090`）不变。两次实际代码提交并推送：`c420bd9e2dd4b0e7ebaa8436aa956a55f4f101e9`清Undertaker/Ravenkeeper Night UI不可达LegacyUnreliable/Direct来源/compat，完整 **CI #4398/R2 #3978 SUCCESS**；`4e1c9d40e72acf6a08abb3f6a843c3d0be9d091b`整删无生产调用的旧不可靠数字历史选择函数及唯一专属测试，完整 **CI #4399/R2 #3979 SUCCESS**（真实Android tests、ASP、Clingo、gate全绿）。GitHub精确比较7文件+8/-155、净删147行，其中整删1生产+1专属旧测试。后续本handoff/docs-only提交会令HEAD前进，**不能把docs-only CI当作比此代码更新的Android编译证据**。
- **唯一台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md`新增F12-L及9个全文+消费者审阅结论，现**186/451（41.24%）已审、265待审、审计中0**；删除路径退出逐文件库存，历史仅在F12-L。NightStepUi、DemonAttackAdapter本轮已全文件审阅并标待解耦；PlayerDisplayResolution+测试、Drunk展示身份、phase mapper保留；Klutz真正方桌选择保留但判输赢尚需退出规则门禁。Sage直选、两类跨局防重复、现有AI首夜/后夜/配对/数字/提示词/真实API/解析/战略记忆/预选/辅助和自动模式、唯一Session及隐私不改。未进行付费API/真机。
- **下一优先序**：从台账剩余265路径继续横向首审epistemic/codec、setup、平台、transport、测试与tools/CI，不能重复算已审项。清Host日间Klutz规则自动胜负（保留公开选择实际Session事件与继续步骤的可用替代）、夜间DemonAttackProductionAdapter预测链（先保证Sage/Ravenkeeper有可用实际唤醒/信息展示入口，再删触发规则推演）、NightStepUi中仍被使用的manual/automatic候选域及其它可替代UI/DTO，和App/HostScreen>50KB大文件真正无用职责。不为通过旧测试恢复已退休Host Rules。每代码批提交推送、跑精确完整GitHub CI/R2并修失败，不merge/retarget/force push，未经授权不做收费API/真机。此处未宣称P1/P4/P5完成。

- **F12-K Sage子阶段实际完成**：从clean+sync `4b29f45117119299f6ed3c395b3f8a4e9556fa35`出发，提交`9d962565b18e53b41713d639129e1ec8f6af253e`将旧全pair真/假枚举、LegacyUnreliable候选菜单替换成Sage方桌直接轻点两名实际玩家，保留有效AI两座位建议的一键确认、现有onShowPlayerDisplay/私密观察原子记录；Sage手工结果使用typed two-seat AnyOf并不强行标truthful。进一步提交最终真实代码`9c7c57a7369d9ee76324b009d26d088a66660923`，彻底删掉`storytellerPairHint`旧默认恶魔+第一人配对、删Sage初始化虚构已选命题和无用传参。真实GitHub对比7文件**+163/-558、净删395行**（本轮无整文件删除）。代码两次完整验收：首CI #4395/R2 #3975 SUCCESS，最终 **[CI #4396 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025505)**（Android full JVM unit tests、debug APK、ASP、real Clingo、CI gate）、**[R2 #3976 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025502)**。无真实付费OpenAI API/真机测试。
- **保护项重申**：User明确要求保留两种跨局体验：①阵容/展示角色尽量避免连局重复（SetupDiversitySelector/History/TemplateRepository）；②同一玩家不同对局尽量避免分到相同身份角色（TB DealPlanner、PlayerStartingIdentityHistory、rotation record/store）。禁止重新以“游戏仍能启动”为理由删除它们。仍须保留现有首夜/pair/numeric/后夜AI推荐、真正的调用/提示词/返回parser/战略记忆、所有MANUAL/AI_ASSISTED/AI_AUTOMATIC模式、唯一Session/GameState、结构化实际行动/公布/角色/存活历史、stale/privacy/idempotent/atomic safety。
- **最新唯一横向审计台账**：`docs/MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md` F12-K，**177/453（39.07%）已审、276待审、0审计中**。F12-K新全文首次审`ClocktowerHostCoreSemantics.kt`1项；已经审过的4项Sage生产/测试与HostScreen不重计；`ClocktowerNightStepUi.kt`虽然被窄修改但整文件仍待审。代码没有删文件，分母不变。未来要继续覆盖真正尚未审过的276项，而不是拿净删行数代替全仓审计。两个>50KB文件App/HostScreen未完成减法，P1/P4/P5仍有剩余，不能宣称整个P1完结。
- **下一轮必须实际实施（F12-L起）**：先用GitHub Connector+MiniMCP `clocktower`检查最新PR #334/head/base、branch/index/clean、remote sync及最精确CI/R2，不重用本段SHA当docs最新HEAD。横向对全仓剩余未审文件审计（优先`ClocktowerNightStepUi.kt` ~39KB及配套legacyInformationCandidates/旧automatic/manual-domain语义、`DemonAttackProductionAdapter`预览链、Sage/Ravenkeeper触发推导、日间Klutz自动判胜负、测试/工具）。对“删除后已有方桌、说书人手工、唯一Session及AI仍能继续游戏”的旧模块，先移除调用再整删与独占测试；不可仅因生产调用而保留。持续检查50KB源文件上限，针对真实废弃职责减法，不靠机械拆文件达标。**确实改代码、提交推送、精确完整Android CI+R2失败即修**，之后更新本唯一台账/roadmap/handoff。不合并、不retarget、不force push PR #334或其它堆叠PR；不调用付费API/不做真机除非后续明确批准。
- **切换注意**：本交接/审计文档另有docs-only提交，GitHub新的文档CI/R2即使成功，Android/ASP/Clingo通常跳过，不可冒充精确代码`9c7c57a`的完整验收。要核对当前真实HEAD与同步关系。

- **F12-J正式代码验收**：原分支/远端Clean、HEAD `2210130d9f89fc03de0f0345d809135748ef7718`。代码提交 `8582fe7a435c2f4c01c84b3a9468f25bc63c4541` 完整Android CI #4392/R2 #3972 SUCCESS；接续提交 **`f822ef5b9174c2e5d38b4704dca68af1042c9c72`** 完整 **[CI #4393 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966455)**（Android全套单元测试、Debug APK、ASP、真实Clingo和gate）及 **[R2 #3973 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966521)**。GitHub比对13文件**+13/-243，净删230行**。2生产文件（ClocktowerNightTargetLegality、ClocktowerRuleOnlyCompactChoice）与2项只保护旧实现的专属测试整删，3个信息角色single-choice专用compact UI与已孤立BeginnerSingleTargetRevealDialog切除；Mayor仅false的automatic/旧ImpSuccession展示退役。所有现行AI建议/真实Session提交、Mayor事件、两种跨局防重复、夜间按Next继续及手工/AI信息确认保留。无付费API或真机验证。
- **最新全源台账**：在F12-I `171/457`上删4路径，其中1已审、3待审；另6个此前未审UI/Presentation/测试已实际全文+consumer复核，故**176/453（38.85%）已审，277待审、0审计中**，单一路径库存详见F12-J段。HostScreen/SageSquareTable以前已审本轮不重复计，NightStepUi的大规模源码继续待全量横向复核。
- **继续清理的边界**：两类开局体验为正式硬保留：阵容防重复+同人连续身份轮换（SetupDiversitySelector/History与旋转planner/history），不可当多余规则校验删掉。SageDisplayOptions全合法pairs仍为手工下拉的当前生产来源，必须先接通可手工选择任意两座位的真实可提交信息路径，再整删其枚举器/专属旧测试；其它旧夜晚规则preview/手工目标拒绝、Klutz旧自动终局与两个超50KB源文件仍待推进。Monk本轮直接选择真实座位，已不阻止选自己的情况，说书人自行判断游戏规则；这里是职责迁移而非正式改变游戏规则。Draft #334 base为`64be3090`，绝不合并/retarget/force push。
- **注意**：后续提交本交接的docs-only CI不等于完整Android二次构建，精确产品代码通过证据须引用f822ef5b的CI #4393。

- **所有者新增硬保留要求（2026-10-19）**：开局前的**角色阵容多样性**（不要连续重复相同角色/展示阵容）与**同一玩家跨局角色轮换**（尽量不要连续获得相同身份）必须共同保留，属于实际产品体验，不能再以“删除也可开局”为理由删除。曾在本轮未提交工作树中尝试删SetupDiversitySelector/History、TemplateRepository及其测试、断开TB历史调用；接到纠正后通过远端真实原HEAD逐字恢复，已验证工作树clean，没有commit/push这些误删。必须保留TB开局prep中的history、近期PlayerStartingIdentityHistory、rotation record/store、DealPlanner的minimumRotationCostAssignment及对应测试。不要擅自对其改成单纯随机。
- **本轮F12-I生产代码已验收**：基线f8cdf8af，提交`eb702ff6387c8b4e28425986e0e089401028130a`（旧PlayerHistoricalTimeline完全没有生产consumer，移除并缩窄混合测试；Mayor仅singleton合法结果自动选定/自动Next及Authority整体退休），**CI #4389/R2 #3969 SUCCESS**。随后提交`69f1bf31df067e3c0d586f6e4a5f93d77d3781cc`（FT Boolean始终YES/NO，删旧Host唯一合法项auto分支/测试，保留AI建议优先和人工确认），**[CI #4390 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608692)**（full Android JVM+Debug APK，ASP/real Clingo/gate均通过）、**[R2 #3970 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608628)**。从f8cdf8..69f1bf合计13文件+29/-610（**净删581行、3生产+3专属旧测试退出**）。没有付费API或真机；现用开局/后夜AI、Session实际Mayor/FT结果、私密事实/版本安全均保留。
- **台账**：原463源路径减6个删除文件、净新增3个曾待审的真实合同全文审计，现行**171/457（37.42%）已审、286待审、0审计中**，逐路径见唯一MINIMAL_HOST_FULL_SOURCE_AUDIT的F12-I；文档将有独立提交导致HEAD前进，勿把docs-only CI当成上述代码验收。
- **后续**：在两类防重复保留的前提下，继续审已标需要解耦的旧规则生产调用。Sage全pair枚举仍被真实Sage UI消费且当前用户无法从该UI直接结构化选择任意两座位，**应先迁移到直接手工pair/AI确认再整删候选生成**，不应直接砍掉还能公布信息的入口；随后Ravenkeeper/Monk目标门禁、Klutz自动结算、日间规则判输赢、老夜间预览及超50KB组件。Draft #334维持原base，未批准不merge/retarget/force-push。

- **新产品删减准则（所有者2026-10-19确认）**：旧模块即使有生产调用，若取消后仍能按现有方桌/人工确认/Session事实/AI推荐正常游戏，就先移除/替换调用再整删该旧模块及专属测试；绝不能单凭当前有调用判保留，低概率特例暂交给说书人处理。真正无替代且关系到继续游戏、唯一事实/隐私/原子性与正在运行的AI链才保留。此项已写入AGENTS.md、HOST_RULES_AI_REBUILD路线和TESTING_STRATEGY。
- **F12-H已经实际实施并全验收**：从clean`fc80fd1a`开始，代码提交`7982b654`移除Spy独立Show Grimoire页面与按钮、首夜/后夜生成的GrimoireState/ReminderToken镜像、独立中毒Spy规则界面、Spy观察导致红鲱鱼提前的过度依赖、相应JSON/历史copy，以及零引用PairInformationOutcome。Spy现在在原方桌上显示唤醒指定座位、拿手机给玩家看、收回再按原Next；未新增确认按钮，普通Spy身份/登记/其他信息记录保留。2个生产文件+4个专属旧测试文件整删。最初CI #4386旧测试要求Spy前确定红鲱鱼失败，修正测试而非恢复旧依赖，最终代码HEAD **`f7f47d1e28b5fd74db61179a3c22736f6ca04d3d`**，GitHub compare**18文件+20/-869、净删849行**，精确 **[CI #4387 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098629)**（Android full JVM+Debug APK、ASP、Clingo、gate）、**[R2 #3967 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098663)**。无真机、无收费API。
- **台账现况**：唯一源路径 `469-6=463`；172已审、291待审、0审计中，无重复；6个已删除原路径只保留在台账F12-H历史说明。App和HostScreen仍超过50KB；P1未完全退出Host Rules。本文档提交会让PR head前进，后续必须现场复核远端/本地SHA、分支干净和CI，docs-only检查不能替代上述代码CI。
- **后续批次建议**：按照新标准审PlayerHistoricalTimeline孤立公开历史投影（迁移需要的真正保密测试）、Mayor/FT“合法结果singleton自动裁决”、Sage pair全候选生成、跨局SetupDiversitySelector+角色轮换DP、NGJ Klutz自动gameOutcome和剩余旧夜间规则preview；按功能是否中断真实游戏判保留必要。仍需继续横向未审291项、清理两个超大UI、核跨项目TBGS合同。Draft PR #334保持不合并、不变base、不强推。

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

## 下一任务：从F12-K继续未审276项，进入F12-L横向清理及超大模块减法

**当前两条并行准则**：旧功能生产有调用不构成保留理由、可以用真实Session/人工或现用AI等价继续游戏则删除；但开局角色阵容防重复与同一玩家跨局身份防重复已被所有者明确认定必须保留，绝不再次擅删。F12-I退役public影子历史与Mayor/FT旧单例真值门禁，现行171/457已审、286待审；下一轮从286项继续横向审计并处理有等价可用入口的旧模块，不能用净删行数冒充完成全仓审计。** 保持唯一现行台账，横向覆盖epistemic/codec、catalog/setup、平台/transport、tools/CI和测试，全文读取并验证真实消费者后再给最终分类；不重读已审文件冒充新进度。再依据横向结果选规模大、证据闭合的一组退休。

F15未清空所有Host Rules：DemonAttackProductionAdapter仍仅给现有信息步骤作只读死亡预览，Sage/Ravenkeeper触发/毒展示、日间自动继承/终局、Klutz/日落清除及旧Flow模板仍有消费者。最终实际死者可与早前预览不同，后续UI补充信息与触发流程仍需审计及真机验收。新NoNightDeaths的跨项目消费者仍需完整合同复核；不要声称已做外部服务/付费/真机验证。夜间已退出旧规则顺便自动判终局，实际结果提交后允许继续；日间终局规则尚在。

现有AI开局/pair/数字/后夜、prompt/真实IO/parser、strategyMemory/preselection及双模式信息采纳必须保护；不恢复已删除web/world引擎或独立causal journal。每批报告实际删除、移除链、净体量、超限、Host事实/事件风险、精确HEAD CI/R2和未知所有者项，保持Draft堆叠、不merge/retarget/force push，原未跟踪文件不动。
