# 当前开发路线（2026-10-18，F15 已实施）

## 当前状态

F15已落实明确夜间实际结果确认：无人/多名死者、任意实际Imp继承座位、原中毒keep/clear；Dawn提交不再引用攻击/保护/Mayor/Soldier/SW或合法继承候选。删除NightDawnResolutionPlanner和TroubleBrewingDemonSuccessionResolver，旧候选UI/元数据/API一同退出；Attack与实际Death独立。多死亡通过原纯Action/Observation提交函数完整预检后一次更新唯一Session；NoNightDeaths为明确空结果事实。私密新身份仍沿实际RoleChange确认，Dawn名单读取实际Death。

本地完整 `:app:testFull :app:assembleDebug` SUCCESS：741 tests、0 failure/error/skipped。生产净删642行/30217B，测试净删1300行/47578B，共1942行/77795B；2生产+10专属旧测试文件退出，新确认界面4673B。当前883跟踪文件、241生产+178测试Kotlin；App159449B、HostScreen99682B仍超限，Session42993B。F15精确代码HEAD的完整远端CI/R2提交后核实；旧F14 `f776151e` CI #4375 / R2 #3955不能替代本次验收。详细边界及风险只在原全源台账F15。

## 最终职责

- Host：唯一 canonical Session/GameState、实际行动与公布信息、结构化事件、必要手工控制与继续游戏；只保护身份/结构、版本、去重/原子性、时序、隐私，不能以游戏规则否决确认事实或从信息反推未选登记。
- AI 推荐：保留开局/后夜、联合规划、真实 API/提示词/解析、战略记忆、预选、AI_ASSISTED/AI_AUTOMATIC；直接读取 canonical facts/events，不接受 Host 的合法候选菜单。
- Rules：以后独立只读重建，不能写事实或成为提交许可。已删除的旧 web/world/candidate 引擎不重建；当前不为清理重新创造规则包装器。

## 当前阶段：成组解耦与全树审计同步推进

**所有者本轮再次强调：全文件遍历约20%尚未完成，F15之后必须继续。下一任务优先恢复未审文件的横向语义审计，不再仅沿夜间调用链实施。** 继续在原台账逐路径记录最终分类、消费者、证据和待审状态，不复制库存或NEXT。

当前范围 **475**，已审计 **116（24.42%）**，0审计中、**359待审**。F12原497项、A/B完成105；F13/F14/F15删除历史和分母变化记录在原台账。重读已审App/Session不计新增，变更/扫描/净删不等于全树语义审计完成。

下一审计横向覆盖未审epistemic/codec、catalog/setup、平台/transport、工具/CI与测试，按实际必要Host/现用AI、旧Rules、死实验或需所有者决策分类，再选择有证据的整组退出。F15没有完成P1：DemonAttackProductionAdapter仍为信息步骤提供只读预览，Sage/Ravenkeeper触发和毒展示、日间自动继承/终局、其它规则和旧Flow模板仍待删除/解耦。预览和最终实际死者可不同，需要后续UI职责审计及真机验收；不能声称跨项目新kind消费者均已验证。

现用AI首夜/后夜、pair/数字、prompt/IO/parser/memory/preselection及信息采纳的双模式保留。U1自定义导入范围未决前保留，不阻塞其余审计。两个超大根组件仍须职责净删至50,000B以下。

## 阶段验收与治理

R0（AI恢复）已完成；P1规则门禁删除/解耦持续推进，F11不是全P1完成。后续依据审计落实纯死代码/实验退出（P2）、唯一状态事件收敛（P3）、UI职责与全部源码<=50000B（P4）、全树/跨项目合同验收和独立只读Rules规划（P5）。

本地验证 testFast；完整检查点 testFull + assembleDebug 及精确代码HEAD的GitHub完整CI/R2。只退休旧规则正确性测试，保留实际AI、Session、时序、隐私、stale及原子性测试；未经实际执行不声称真机或付费API已验收。Draft #334继续堆叠，不合并/retarget/force push；原有未跟踪文件不动。
