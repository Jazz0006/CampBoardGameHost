# 当前开发路线（2026-10-19，F12-K Sage直选配对完成及切换新对话）

## 当前状态

**最新F12-K（Sage子阶段完成，准备转入新对话）**：正式将Sage旧“枚举全部pair、真假筛选、手动候选下拉”改为现有方桌上人工直接点两个实际座位，并仍可直接采纳AI明确返回的两个座位；统一沿原onShowPlayerDisplay→唯一Session私密公布/事件记录。连带删除Host`storytellerPairHint`根据真恶魔自动拼出来的默认decoy，Sage夜步骤不再带伪默认信息。两个代码提交`9d962565b18e53b41713d639129e1ec8f6af253e`、最终`9c7c57a7369d9ee76324b009d26d088a66660923`，GitHub实际比对**7文件+163/-558，净删395行**。精确最终Android完整 **[CI #4396 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025505)**（full JVM tests+Debug APK、ASP/Clingo/gate成功）、**[R2 #3976 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39181025502)**。唯一台账**177/453（39.07%）已审、276待审、0审计中**；Sage已审生产/测试只复查、新全文审阅HostCoreSemantics+1，NightStepUi仍待全文件审计。阵容跨局防重复和同一玩家身份防重复、现有AI/唯一Session/安全保密全保留。**注意本轮只完成Sage子阶段，P1/P4及未审276文件并未完成**。无真机/付费API验证。

**最新F12-J**：用户批准的两类开局防重复（跨局阵容多样性、同一玩家身份轮换）及现有AI/唯一Session均保留。已整删Host夜间Monk/Ravenkeeper目标合法性包装器、singleton紧凑授权模块及两份专属测试（4文件）；Undertaker/Sage/Ravenkeeper取消仅有一个候选时的特别展示分支，改用原统一确认UI；切除已孤立的BeginnerSingleTargetRevealDialog。随后清掉Mayor方桌永不可达的automatic/Imp继任分支和无消费者DTO字段，保留真实Mayor选择目标、Mayor dies按钮与Session实际事件。代码从`2210130d`经`8582fe7a`到最终**`f822ef5b9174c2e5d38b4704dca68af1042c9c72`**，GitHub比较13文件+13/-243，**净删230行**。最终完整 **[CI #4393 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966455)**（Android全JVM单测+Debug APK、ASP/Clingo/gate成功）、**[R2 #3973 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39178966521)**。唯一审计账**176/453（38.85%）已审、277待审**。Monk界面现允许选择任意已存在座位，不再由Host按玩法过滤自己，规则裁量由说书人自行把握。Sage全配对仍供真实手工下拉使用，待直接座位Pair UI可用才整删。App/HostScreen仍>50KB，P1/P4未完成；未运行真机/付费API。

**最新F12-I**：所有者在清理期间明确要求永久保留两个独立开局体验：（1）连续局阵容/展示角色避免重复，（2）同一玩家跨局尽量不被分配相同身份。开发机先前**未提交**的SetupDiversitySelector/History、TemplateRepository及rotation history/role assignment和专属测试删除，已全部恢复，git_state再次确认没有变动；不删/改相关生产行为。其它确认可替代模块继续减法：退役PlayerHistoricalTimeline影子公开历史、Mayor singleton Host-legal自动结果及自动跳步（保留实际选择/Session确认）、FT Host唯一Boolean自动判定（保留人工YES/NO及AI推荐）；3生产+3专属旧测试整删，GitHub范围f8cdf8af..69f1bf31 **13文件+29/-610、净删581行**。实际代码经过两个独立检查点：eb702ff6 CI #4389/R2 #3969 SUCCESS，最终69f1bf31 **[CI #4390 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608692)**（Android全单测+Debug APK、ASP、Clingo、gate）及 **[R2 #3970 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39174608628)**。现行台账**171/457（37.42%）已审、286待审**；无真机或付费模型调用。下批清Sage等之前先保留结构化人工两座位决定与AI原有效行为，避免仅删除候选导致无法正常公布。P1/P4继续。

**最新F12-H（2026-10-19）**：所有者明确“已有生产调用不是保留理由；删除后游戏可按既有方桌/人工操作/Session/AI继续则删除整条旧链，只有不能替代的基本游戏、事实/安全和AI路径保留”。已实施首夜/后夜间谍只提示唤醒具体座位、说书人把当前方桌给间谍看后收回手机点击原下一步；正式取消“展示魔典”按钮、单独魔典视图、GrimoireState/ReminderToken DTO及JSON、独立中毒Spy gate、Spy→红鲱鱼旧依赖。两个生产文件及四个专属旧测试文件整删，还清除孤立PairInformationOutcome。GitHub代码范围 `fc80fd1a..f7f47d1e` **18文件+20/-869，净删849行**。第一完整CI #4386失败因旧FirstNightFlowTest强制红鲱鱼先于Spy；已退休该测试并保留占卜师前置事实测试，最终精确代码 `f7f47d1e28b5fd74db61179a3c22736f6ca04d3d` 的 **[CI #4387 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098629)**（Android full JVM+Debug APK、ASP/real Clingo/gate成功）及 **[R2 #3967 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39172098663)**。唯一台账移除6文件后**172/463（37.15%）已审、291待审**。App/HostScreen仍超50,000B，P1/P4未完成。

**历史F12-G**：实际删除无生产调用的FormalGameState、FormalPlayerState、PlayerKnowledgeSnapshot及配套JSON/旧Timeline绑定，专属旧测试文件1个退出，两个混合测试保留真实global chronology/私密观察合同。代码起点`4da03849`、最终代码SHA **`8644e7575b67d9f3bef6cc269c7c93bca4332bf1`**，GitHub精确比对7文件+8/-737（净删729行）。首轮CI #4383 Kotlin import误删失败，定位只补回`Collections`后最终完整Android **CI #4384 SUCCESS / R2 #3964 SUCCESS**，ASP/真实Clingo/gate通过。新全文审计7项、退役原待审测试1项：**177/469（37.74%）已审，292待审**。现用AI与Session事实/隐私均未修改。

**当前最新（F12-F，MiniMCP）**：在`56975956`基线完成32个原待审文件全文语义审计，整删旧PROD-GLOBAL 3B2/3B3独立实验2脚本/2专属测试及过时P1分支专用CI工作流、删掉主CI中的6行旧Python运行步骤。代码独立提交`e6faea27bb28a571b45669bf2357af0b11e4f863`并推送，GitHub实际对比6文件净删1087行（旧台账估计约52,165B）；精确代码完整Android（full JVM+Debug APK）CI #4381 SUCCESS / R2 #3961 SUCCESS，ASP、Clingo、CI gate均通过。本批后**170/470（36.17%）已审、300待审**，新改动不涉及Android应用源码或现用AI模型链。

F15已落实明确夜间实际结果确认：无人/多名死者、任意实际Imp继承座位、原中毒keep/clear；Dawn提交不再引用攻击/保护/Mayor/Soldier/SW或合法继承候选。删除NightDawnResolutionPlanner和TroubleBrewingDemonSuccessionResolver，旧候选UI/元数据/API一同退出；Attack与实际Death独立。多死亡通过原纯Action/Observation提交函数完整预检后一次更新唯一Session；NoNightDeaths为明确空结果事实。私密新身份仍沿实际RoleChange确认，Dawn名单读取实际Death。

本地完整 `:app:testFull :app:assembleDebug` SUCCESS：741 tests、0 failure/error/skipped。生产净删642行/30217B，测试净删1300行/47578B，共1942行/77795B；2生产+10专属旧测试文件退出，新确认界面4673B。当前883跟踪文件、241生产+178测试Kotlin；App159449B、HostScreen99682B仍超限，Session42993B。精确代码 **`0f904f7031770bd2f12acea4299aa9b1d5c8e5c0`** 的完整 Android **[CI #4377 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759682)**（full单测+Debug APK）、**[R2 #3957 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39121759696)**；ASP、真实Clingo、CI gate均SUCCESS。 后续docs-only HEAD不替代此代码验收。详细边界及风险只在原全源台账F15。

## 新对话 / 开发机接续

F12-H最终完整代码SHA `f7f47d1e`，CI #4387/R2 #3967 SUCCESS；本次文档还会另行提交，进入下一轮必须实时核对GitHub PR/head/base、MiniMCP干净同步及精确代码CI，不能将docs-only CI算Android编译通过。当前**172/463已审、291待审**。下一批应用产品必要性新标准：若已有简单直接路径继续游戏，先替换/取消真实生产调用再整组退出旧模块，优先PlayerHistoricalTimeline、Mayor/FT host singleton/法律候选、Sage菜单、SetupDiversity/角色轮换；不要以有消费者作为默认保留理由。现用AI/唯一Session事实安全不可丢。

## 最终职责

- Host：唯一 canonical Session/GameState、实际行动与公布信息、结构化事件、必要手工控制与继续游戏；只保护身份/结构、版本、去重/原子性、时序、隐私，不能以游戏规则否决确认事实或从信息反推未选登记。
- AI 推荐：保留开局/后夜、联合规划、真实 API/提示词/解析、战略记忆、预选、AI_ASSISTED/AI_AUTOMATIC；直接读取 canonical facts/events，不接受 Host 的合法候选菜单。
- Rules：以后独立只读重建，不能写事实或成为提交许可。已删除的旧 web/world/candidate 引擎不重建；当前不为清理重新创造规则包装器。

## 当前阶段：成组解耦与全树审计同步推进

**所有者本轮再次强调：全文件遍历约20%尚未完成，F15之后必须继续。横向审计已恢复，下一任务继续覆盖未审文件，不再仅沿夜间调用链实施。** 继续在原台账逐路径记录最终分类、消费者、证据和待审状态，不复制库存或NEXT。

F12-D/E/F/G完成的横向审计和旧Formal删除见唯一台账；F12-H正式取消间谍独立魔典与旧Reminder Token，连同完全孤立的pair DTO，当前范围 `469-6=463`，已审旧路径退役5项、原未审旧展示测试退役1项，现行**172/463（37.15%）已审、0审计中、291待审**；删除不当作新增审计。已删实例证明：生产调用是耦合，不代表功能有保留必要。继续横向逐文件与高价值旧生产链净删并进；Host Rules P1、超50KB治理和实际真机仍未完成。

下一审计横向覆盖未审epistemic/codec、catalog/setup、平台/transport、工具/CI与测试，按实际必要Host/现用AI、旧Rules、死实验或需所有者决策分类，再选择有证据的整组退出。F15没有完成P1：DemonAttackProductionAdapter仍为信息步骤提供只读预览，Sage/Ravenkeeper触发和毒展示、日间自动继承/终局、其它规则和旧Flow模板仍待删除/解耦。预览和最终实际死者可不同，需要后续UI职责审计及真机验收；不能声称跨项目新kind消费者均已验证。

现用AI首夜/后夜、pair/数字、prompt/IO/parser/memory/preselection及信息采纳的双模式保留。U1自定义导入范围未决前保留，不阻塞其余审计。两个超大根组件仍须职责净删至50,000B以下。

## 阶段验收与治理

R0（AI恢复）已完成；P1规则门禁删除/解耦持续推进，F11不是全P1完成。后续依据审计落实纯死代码/实验退出（P2）、唯一状态事件收敛（P3）、UI职责与全部源码<=50000B（P4）、全树/跨项目合同验收和独立只读Rules规划（P5）。

本地验证 testFast；完整检查点 testFull + assembleDebug 及精确代码HEAD的GitHub完整CI/R2。只退休旧规则正确性测试，保留实际AI、Session、时序、隐私、stale及原子性测试；未经实际执行不声称真机或付费API已验收。Draft #334继续堆叠，不合并/retarget/force push；原有未跟踪文件不动。
