# CampBoardGameHost — 当前文档入口

> 同步日期：2026-10-09（Australia/Sydney）。这里是 **导航，不是第二份 roadmap**。旧索引原文已存于 [归档](archive/checkpoints/DOCS_INDEX_PRE_MEM0_CONVERGENCE_2026-10-09.md)。

## 新开发会话：按需阅读

1. 根目录 [AGENTS.md](../AGENTS.md)：执行规范、模块权威与 AI 协作方式。
2. [测试策略](TESTING_STRATEGY.md) 与 [当前工作流](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md)：风险分级测试、GitHub 独立验收。
3. **[当前开发路线](CURRENT_DEVELOPMENT_ROADMAP.md)**：唯一当前状态和优先级权威。
4. **[当前交接](NEXT_DEVELOPMENT_HANDOFF.md)**：唯一活动 handoff。
5. 仅当任务涉及具体模块时再查下面的专项规范；不把历史阶段 audit 全部加入默认阅读。

## 当前研究与产品约束

- [GSP-MEM0 / Recovery 范围决策](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md)：战略记忆可以参与建议，但 Host 规则/事实/候选仍是权威；Recovery 仅恢复短期意外中断。
- **[生产接入与全自动说书人路线（GSP-PROD-AUTO）](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)**：当前产品首要路线；真实 TB 中 LLM 推荐 → 自动裁量 → 完整无人类说书人。MEM0/MEM1 改为非阻塞实验。
- **[PROD-GLOBAL-1 整局全局分析、可修订策略与方桌 UI](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)**：2026-10-09 用户纠偏后的 PROD-1 产品验收权威。先全局诊断/交互/未来计划，再当前合法推荐；MANUAL 无 AI，ASSISTED 方桌图形分析，AUTO 验证后自动推进。PR #292 原独立酒鬼推荐为 Draft SPIKE，尚不可合并。
- [MEM0 实验与评分协议](GSP_MEMORY_ABLATION_EXPERIMENT_2026-10-09.md) 与 [冻结合成 TB8 fixture](benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json)：B2 已有九份真实模型响应，**未做正式 A/B/C 盲评**，用于辅助生产上下文检查，不阻塞接入。
- [API0 Responses benchmark 接口](GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)：**已验证开发机真实结构化 API 实验；尚无 Android 生产集成/安全网关**。
- [通用说书人 Policy 规范](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)：推荐只对 Host 的合法选择 rank/recommend/explain，不继承旧自动特例。
- [GSP-R1 现有历史捕捉审计](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md) 与 [registration ambiguity 约束](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md)：已实现机械历史继续有效；C2C-2/3 可选扩展目前暂停，不能因旧 NEXT 文字擅自恢复。

## 长期架构与独立证据

- [RES-0～5 分离与物理清理](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)：Game Engine / Manual 与 Provider 分离，已接受。
- [核心模块所有权目标](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md) 和 [TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)：共享只读上下文，不建立第二个可写 Game State。
- [HOST-ML1 中立 decision export](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)：为测试和未来模型保留可替换接口。
- [旧推荐系统退役审计](LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md)：仅作为已删除旧引擎的证据，不授权继续旧 policy。
- [历史归档入口](archive/README.md) 和 [2026-10-09 分支保留/清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md)。

## 判断冲突与维护规则

官方 BoTC 规则负责游戏规则真伪；`AGENTS.md` 负责协作执行与所有权；`TESTING_STRATEGY.md` 负责测试；**当前 roadmap + handoff + 最新 GSP-PROD-AUTO 授权（不取消先前 Recovery 范围边界）** 负责下一步任务。专项旧路线与 archived docs 仅作为历史/设计证据；Mini MCP developer memory 仅供导航，不覆盖 live 状态。

历史 SDE/C5/DLB/TBGS/RSR/LRE 完成状态不再占据活动入口，也不能从其旧 `NEXT` 推断可实施任务。迁移、归档和删除文件前应先核对实际引用，确保不损坏深链接；完成一次阶段验收应回收过时 handoff，而不是叠加新入口。
