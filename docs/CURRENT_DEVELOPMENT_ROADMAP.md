# 当前开发路线（2026-10-18，F14 已实施）

## 当前状态

F14已先退休日间登记之后的夜间影子重建：攻击/继承选择不再通过伪MechanicalDeath/RoleChanged改变界面的实际alive/role。删除NightTransactionRestoreComposition，删除EffectiveNightStateProjector及其伪事件类型，Chambermaid/Empath和身份读取直接使用canonical Session状态。保留Ravenkeeper信息入口、现用AI、真实Death/RoleChange和Dawn应用。实际死亡/继承/毒等旧规则尚未整体退出，P1未完成。

本地完整 `:app:testFull :app:assembleDebug` SUCCESS：761 tests、0 failure/error/skipped，Debug APK成功。生产净删318行/15571B、测试净删1238行/51061B，共1556行/66632B；894跟踪文件、242生产+188测试Kotlin。当前App172872B、HostScreen101202B仍超限。精确代码 **`f776151ed8a3e778ddb088a78a4313cfc7a74dfe`** 的完整 Android **[CI #4375 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119919096)**（full单测+Debug APK）、**[R2 #3955 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119919080)**；ASP、真实Clingo、CI gate均SUCCESS。 后续docs-only HEAD不能替代本代码验收。完整边界/证据只在原全源台账F14记录。

## 最终职责

- Host：唯一 canonical Session/GameState、实际行动与公布信息、结构化事件、必要手工控制与继续游戏；只保护身份/结构、版本、去重/原子性、时序、隐私，不能以游戏规则否决确认事实或从信息反推未选登记。
- AI 推荐：保留开局/后夜、联合规划、真实 API/提示词/解析、战略记忆、预选、AI_ASSISTED/AI_AUTOMATIC；直接读取 canonical facts/events，不接受 Host 的合法候选菜单。
- Rules：以后独立只读重建，不能写事实或成为提交许可。已删除的旧 web/world/candidate 引擎不重建；当前不为清理重新创造规则包装器。

## 当前阶段：成组解耦与全树审计同步推进

原F12范围497项、A/B完成105；F13并入实际日间登记并删除独立causal journal，F14继续删影子重建。当前范围 **486**，已审计 **112（23.05%）**，0审计中、374待审；已从真实Git tree核对每路径库存，删除历史/分母变化只见原台账F12/F13/F14。扫描或净删率不算语义审计完成率。

下一组先接实际夜间结果与继承确认：App.onConfirmNight仍用DawnDeathFacts→NightDawnResolutionPlanner.planValidatedNightDeath，继承仍有Forced/Choice、App自动promote和合法候选限制。NightTargets.after(Death)还会回写attackSeat，下一组须把实际死亡与原攻击事实分开。需要保留实际Death/RoleChange的Session原子提交、公开告知及私密新身份，再成组删除预测/候选门禁，避免改变尚未宣布信息的recipient/时间。F14只删除把这些预测二次投影成事实的分支，没有称它们已从提交端移除。

横向审计继续覆盖epistemic/codec、setup、工具/CI及测试。Ravenkeeper/Sage预览与eligibility、Spy、日间命中/后果、毒/终局、发牌门禁及两个超大根组件仍待职责处理；U1自定义导入范围未决前保留，不阻塞其他组。现用AI的prompt/IO/parser/memory/preselection及身份/版本/隐私边界必须保留。

## 阶段验收与治理

R0（AI恢复）已完成；P1规则门禁删除/解耦持续推进，F11不是全P1完成。后续依据审计落实纯死代码/实验退出（P2）、唯一状态事件收敛（P3）、UI职责与全部源码<=50000B（P4）、全树/跨项目合同验收和独立只读Rules规划（P5）。

本地验证 testFast；完整检查点 testFull + assembleDebug 及精确代码HEAD的GitHub完整CI/R2。只退休旧规则正确性测试，保留实际AI、Session、时序、隐私、stale及原子性测试；未经实际执行不声称真机或付费API已验收。Draft #334继续堆叠，不合并/retarget/force push；原有未跟踪文件不动。
