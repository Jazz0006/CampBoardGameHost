# 当前开发交接

> **2026-10-26 更新：** 重构 Batch 1–5、周末真机修复，以及 2026-10-26 审查的修复（#369、#370 与审查小项修复批）已合并。详见 [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 AGENTS.md 当前权威一节。

1. 先核对 live main、工作区、开放 PR 与精确 SHA 的 CI/R2；Android tests 日志须确实运行 `./gradlew :app:testFull :app:assembleDebug`（PR 默认只跑 `testFast`，代码提交需带 `[full-ci]`），合并后再看 main push 运行。
2. 夜间 Host 事实由 `ClocktowerNightFacts`（Session 快照一次读取）与 `ClocktowerNightDraftSubjects`（草稿座位路由）统一提供；`ClocktowerNightPresentation` 只把座位映射为卡片与文字。新增夜间事实放在这里，不在 UI 中重复计算。
3. 规则改动须在 PR 中说明改了什么、为什么；同一条规则只在 Session 侧实现一次，AI 解析、UI 草稿与确认共用（如红鲱鱼规则）。存档/metadata 格式改动须说明旧存档如何恢复。
4. 下一项功能：AI 不可用或恢复后仍能完成本局（统一夜间手动接管、人工触发的可选随机、恢复后过期响应校验）。
5. Session 唯一写事实；无持久 UI pending、第二游戏对象或独立恢复系统；D2 预览不入账，Next 记录最后一次展示。保留两套内置剧本、三种模式、两类跨局防重复、隐私与原子保存。
6. 测试按 2026-10-25 测试政策：只为有明确缺口的重要失败加小型行为回归。真机与付费模型未验收；付费调用需授权。

[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) · [旧累计交接](archive/legacy-host/PRE_SHARED_GUIDE_NEXT_DEVELOPMENT_HANDOFF.md) · [归档清理清单](DOCS_CLEANUP_2026-10-24.md)。
