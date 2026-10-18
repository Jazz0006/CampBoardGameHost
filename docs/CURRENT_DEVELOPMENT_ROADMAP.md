# 当前开发路线（2026-10-18，F12 审计进行中）

## 当前状态

所有者已授权按完整职责链加速实施。F13在起点 `d76968168eb02e37cd87084bb51eabc60f789d9c` 补齐8个Session全文审计，并实际删除日间裁定producer、独立causal journal及DecisionOutcomeSnapshot：3生产+3旧测试文件退出，其中Mayor安全用例迁出保留。明确登记并入SlayerShot/Nomination，与公开动作共享一次Session事件提交，不推造角色/阵营。保留现用AI及实际死亡/执行操作。

本地完整 `:app:testFull :app:assembleDebug` SUCCESS：807 tests，0 failure/error/skipped。生产净删800行/43958B、测试净删338行/16469B，共1138行/60427B；900跟踪文件，243生产+193测试Kotlin。当前App172872B、HostScreen101994B仍超限。精确代码 **`27d02f723340ff88c466d170655898be768d3ae3`** 的完整 Android **[CI #4373 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071885)**（full单测+Debug APK）、**[R2 #3953 SUCCESS](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39119071868)**；ASP、真实Clingo、CI gate均SUCCESS。 后续docs-only提交不改变此代码验收基线。完整边界/证据只在全源台账F13记录。

## 最终职责

- Host：唯一 canonical Session/GameState、实际行动与公布信息、结构化事件、必要手工控制与继续游戏；只保护身份/结构、版本、去重/原子性、时序、隐私，不能以游戏规则否决确认事实或从信息反推未选登记。
- AI 推荐：保留开局/后夜、联合规划、真实 API/提示词/解析、战略记忆、预选、AI_ASSISTED/AI_AUTOMATIC；直接读取 canonical facts/events，不接受 Host 的合法候选菜单。
- Rules：以后独立只读重建，不能写事实或成为提交许可。已删除的旧 web/world/candidate 引擎不重建；当前不为清理重新创造规则包装器。

## 当前阶段：成组解耦与全树审计同步推进

原F12范围497项，A/B完成105项；F13补齐8个Session审计并落实整组删除，6个删除路径退出当前表、1个迁移测试入表。当前范围 **492**，已审计 **111（22.56%）**，0审计中、381待审。真实消费者、每路径状态/字节、证据和历史分母只在原全源台账F12/F13维护；扫描或代码净删不算语义审计进度。

本批已移除日间provider候选/frozen snapshot/PriorDecision/correction archive整条重复历史链。登记的null/false/true、JSON旧历史兼容、canonical身份、AI delta及各玩家回放隐私已纳入完整回归；不再存放或生成独立裁定历史。信息录入与AI的游戏规则门禁尚未全部删除，P1没有完成。

下一组优先处理实际死亡→继承→新身份/Dawn：从NightDawnResolutionPlanner、RestoreComposition和DemonSuccessionResolver混合链中保留真正结果确认及Session应用，删除预测confirmedEvents和强制/候选规则推导，再处理其专属测试。横向审计继续覆盖epistemic/codec、setup、工具/CI和测试，避免只沿调用链。剩余Sage/Spy、日间命中/后果、毒/终局、发牌门禁及两个超大根组件仍列待解耦。U1自定义导入未决前保留，不阻塞其他组。

## 阶段验收与治理

R0（AI恢复）已完成；P1规则门禁删除/解耦持续推进，F11不是全P1完成。后续依据审计落实纯死代码/实验退出（P2）、唯一状态事件收敛（P3）、UI职责与全部源码<=50000B（P4）、全树/跨项目合同验收和独立只读Rules规划（P5）。

本地验证 testFast；完整检查点 testFull + assembleDebug 及精确代码HEAD的GitHub完整CI/R2。只退休旧规则正确性测试，保留实际AI、Session、时序、隐私、stale及原子性测试；未经实际执行不声称真机或付费API已验收。Draft #334继续堆叠，不合并/retarget/force push；原有未跟踪文件不动。
