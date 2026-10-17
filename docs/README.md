# 文档入口

更新：2026-10-17（Australia/Sydney）。导航不另行决定优先级。

## 当前必读

1. [开发规范](../AGENTS.md)：所有权、证据和合并规则。
2. [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md)：唯一执行优先级。
3. [当前交接](NEXT_DEVELOPMENT_HANDOFF.md)：已做与未做。
4. [最小游戏状态与记录清理](MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md)：本轮产品纠正、代码审计与删除顺序。
5. [测试策略](TESTING_STRATEGY.md)：证据分级，不要求机械 RED/GREEN。

- [第四阶段 App/Host 审计与拆分路线](APP_HOST_STAGE_D_AUDIT_2026-10-17.md)
- [分支清理结果与恢复入口](BRANCH_CLEANUP_RESULT_2026-10-17.md)

## 按需参考

- [核心模块所有权目标](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)：Setup、Game Engine、推荐分离；旧多份历史/恢复保留要求由本轮清理规格覆盖。
- [既有 App/Host 拆分审计](APP_HOST_POST_RES5_DECOUPLING_AUDIT_2026-10-08.md)：历史边界与经验；尺寸、候选和完成状态须以代码重查。
- [专家说书人规划原则](PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md)、[联合开局规划](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md)、[Host 语义校验](PROD_GLOBAL_3B3D_HOST_SEMANTIC_GATE_ROUTE_2026-10-12.md)：AI 方向保留，当前不优先扩展。
- [历史捕捉审计](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md)：当时的覆盖证据，不是当前实现清单。
- [旧 Recovery 范围决策](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md) 和 [旧最小恢复审计](CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md)：历史方案，不再授权保留独立恢复体系。
- [工作流](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md)：本轮本地读写按用户明确覆盖执行；独立验收规则仍有效。
- [历史归档](archive/README.md)：既有记录，不从旧 NEXT 推导新任务。

当前入口不保留滚动叠加的旧 PR/CI/handoff 快照；需要历史时查 Git 历史或对应专项文档。不得将旧验收结果当作当前工作区的验证。
