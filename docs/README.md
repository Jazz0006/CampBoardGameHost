# 文档入口

## 当前阅读顺序

1. [GameSession 简明共享说明](GAME_SESSION_SHARED_GUIDE.md)：三个项目共用的最小记录链、当前保存/恢复与 AI 续接，以及尚未实现的部分。
2. [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md)、[交接](NEXT_DEVELOPMENT_HANDOFF.md)：当前代码与下一项工作。
3. [最小事实合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)、[已批准重构合同](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)：唯一 Session、确认顺序、隐私、原子保存等约束。
4. [Host UI 职责分批计划](HOST_UI_RESPONSIBILITY_REFACTOR_PLAN_2026-10-23.md)、[测试策略](TESTING_STRATEGY.md)、[方桌视觉合同](HOST_TABLE_VISUAL_CONTRACT_2026-10-22.md)。

最新用户决定及 AGENTS 当前段优先；共享说明沿用已有合同，不另建事实模型。设计目标、已实现能力与验收证据必须分开。文件名包含 CURRENT、CONTRACT、NEXT 不代表其内容仍适用，尤其是归档文件。

## 当前验收与工具参考

- [Batch 3](HOST_UI_BATCH3_ACCEPTANCE.md)、[Batch 4](HOST_UI_BATCH4_ACCEPTANCE.md)、[Batch 5](HOST_UI_BATCH5_ACCEPTANCE.md)：Batches 3/4 已合并，Batch 5 仍是 Draft #361。
- [实际整局验收](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)、[全仓清理验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md)：历史代码检查点，不替代最新 SHA。
- [测试 APK 分发](FIELD_TEST_APK_DISTRIBUTION.md)、[日志诊断参考](AI_DIAG1_LIVE_ADB_LOGCAT_ROUTE_2026-10-12.md)、[docs-only CI 策略](CI_DOCS_ONLY_MAIN_PUSH_POLICY_2026-09-08.md)。

## 历史资料

[归档入口](archive/README.md) · [本次清理清单](DOCS_CLEANUP_2026-10-24.md)。旧策略/评分/世界枚举、独立 Recovery、旧迁移与实验路线均只供追溯，不是重新开发或恢复旧代码的指令。史料、JSON/TSV 数据和验收证据不能仅因日期较早就删除。
