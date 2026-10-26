# 当前开发路线

> **2026-10-26 更新：** Host UI 职责重构 Batch 1–5（#355–#361）与周末真机修复（#358、#363–#367）均已合并。2026-10-26 代码审查的修复：#369（夜间事实只计算一次、AI 红鲱鱼校验、快速重开不崩溃）、#370（厨师按官方规则：间谍/隐士每次读取只登记一次）、审查小项修复批（元数据去重保存、Host 侧解码错误不再标为 AI 拒绝、恢复存档失败不采用 AI 状态、对局记录清理、厨师冗余计算）。每次开工前核对 live main、PR 与精确 SHA 的 CI（日志须确实运行 `testFull`）。

- 已完成：重构批次与审查修复。[FINAL v3 计划](HOST_UI_RESPONSIBILITY_REFACTOR_PLAN_2026-10-23.md)、[Batch 5 验收](HOST_UI_BATCH5_ACCEPTANCE.md) 现为历史记录。
- 已定规则（2026-10-26）：厨师计数时，正常运作的间谍/隐士每次厨师读取只登记一次（官方规则）；红鲱鱼为“真实善良且不是隐士”的玩家，这一条规则在 Session 侧唯一实现，AI 开局解析、草稿与确认共用。
- 下一项功能优先级：AI 不可用或恢复后仍能完成本局——统一夜间手动接管入口、人工触发的可选随机、恢复后的请求 ID/游标与过期响应校验。尚未实现，不能用“请手动继续”的提示代替可用入口。
- 真机验收仍待执行：360×520 / 15 座位可读性、醉毒/死亡/鬼票标记叠加、三种模式的全屏交接与 Next、来电/后台/杀进程后继续。付费模型调用需单独授权。
- 跨项目 JSON 仅保留[简明共享说明](GAME_SESSION_SHARED_GUIDE.md)；跨语言 SDK 与 EvidenceLab 评分体系不是当前前提。

约束：AGENTS.md 当前权威一节；[最小事实合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)、[测试策略](TESTING_STRATEGY.md)。[旧累计路线](archive/legacy-host/PRE_SHARED_GUIDE_CURRENT_DEVELOPMENT_ROADMAP.md) 仅供追溯，不能作为当前 NEXT。
