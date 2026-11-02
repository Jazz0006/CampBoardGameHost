# 当前开发交接

> **2026-10-26 更新：** 重构 Batch 1–5、周末真机修复，以及 2026-10-26 审查的修复（#369、#370 与审查小项修复批）已合并。详见 [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 AGENTS.md 当前权威一节。

1. 先核对 live main、工作区、开放 PR 与精确 SHA 的 CI/R2；Android tests 日志须确实运行 `./gradlew :app:testFull :app:assembleDebug`（PR 默认只跑 `testFast`，代码提交需带 `[full-ci]`），合并后再看 main push 运行。
2. 夜间 Host 事实由 `ClocktowerNightFacts`（Session 快照一次读取）与 `ClocktowerNightDraftSubjects`（草稿座位路由）统一提供；`ClocktowerNightPresentation` 只把座位映射为卡片与文字。新增夜间事实放在这里，不在 UI 中重复计算。
3. 规则改动须在 PR 中说明改了什么、为什么；同一条规则只在 Session 侧实现一次，AI 解析、UI 草稿与确认共用（如红鲱鱼规则）。存档/metadata 格式改动须说明旧存档如何恢复。
4. 下一项功能：AI 不可用或恢复后仍能完成本局（统一夜间手动接管、人工触发的可选随机、恢复后过期响应校验）。
5. Session 唯一写事实；无持久 UI pending、第二游戏对象或独立恢复系统；D2 预览不入账，Next 记录最后一次展示。保留两套内置剧本、三种模式、两类跨局防重复、隐私与原子保存。
6. 测试按 2026-10-25 测试政策：只为有明确缺口的重要失败加小型行为回归。真机与付费模型未验收；付费调用需授权。

BMR 新功能线已完成 [A 批实施细化](BMR_A_IMPLEMENTATION_PLAN_2026-11-02.md)：人工配置/发牌/恢复与首夜方桌预览；官方资料核对和实际 A 实现见该方案文末。A 开局开发预览已落地，完整 BMR 仍不可玩；下一 B 批处理实际结算基础，不以此替代上文 AI 续局优先项。

[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) · [旧累计交接](archive/legacy-host/PRE_SHARED_GUIDE_NEXT_DEVELOPMENT_HANDOFF.md) · [归档清理清单](DOCS_CLEANUP_2026-10-24.md)。

2026-11-02 用户再次强调：存档只保证异常退出后继续当前局。复用既有活动局保存/加载；未确认步骤可重做，不恢复 UI 细节，不扩展历史档案、回放、初始快照或通用恢复框架。BMR 实施遵守此限制。

最新 BMR 进展：[B 批生死基础与审核项](BMR_B_LIFE_PROGRESS_2026-11-02.md)。已开始实际生死/登记死亡/复活及其消费者接入，完整 B/C/D/E 尚未完成；普卡自毒时序已向用户请求审核，收到答复前暂停相关醉毒和角色流程实现。
