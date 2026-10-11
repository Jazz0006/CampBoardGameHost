# CampBoardGameHost — 当前文档入口

> 同步日期：2026-10-10（Australia/Sydney）。这里是 **导航，不是第二份 roadmap**。旧索引原文已存于 [归档](archive/checkpoints/DOCS_INDEX_PRE_MEM0_CONVERGENCE_2026-10-09.md)。

## 新开发会话：按需阅读

1. 根目录 [AGENTS.md](../AGENTS.md)：执行规范、模块权威与 AI 协作方式。
2. [测试策略](TESTING_STRATEGY.md) 与 [当前工作流](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md)：风险分级测试、GitHub 独立验收。
3. **[当前开发路线](CURRENT_DEVELOPMENT_ROADMAP.md)**：唯一当前状态和优先级权威。
4. **[当前交接](NEXT_DEVELOPMENT_HANDOFF.md)**：唯一活动 handoff。
5. 仅当任务涉及具体模块时再查下面的专项规范；不把历史阶段 audit 全部加入默认阅读。

## 当前研究与产品约束

- [GSP-MEM0 / Recovery 范围决策](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md)：战略记忆可以参与建议，但 Host 规则/事实/候选仍是权威；Recovery 仅恢复短期意外中断。
- **[生产接入与全自动说书人路线（GSP-PROD-AUTO）](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)**：长期产品目标：真实 TB 中 LLM 推荐 → 自动裁量 → 完整无人类说书人；**当前执行优先级以 roadmap/handoff 和 PROD-GLOBAL-1D 为准**。 MEM0/MEM1 改为非阻塞实验。
- **[PROD-GLOBAL-1/1D 整局策略、展示身份后台预规划、全阶段事件重规划](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)**：当前 PROD-1 产品验收与首夜时序权威。2026-10-10 再确认 **先展示身份、再定酒鬼完全合法**；后台首次整局规划可与逐人展示重叠，Host 在投毒、人工覆盖等**真实确认事件**后按当前合法状态更新剩余建议。低于 5 秒尚未验证。PR #292 已含多信息家族代码、Keystore 凭据持久化，仍为 Draft，不能当完成的自动说书人。
- [MEM0 实验与评分协议](GSP_MEMORY_ABLATION_EXPERIMENT_2026-10-09.md) 与 [冻结合成 TB8 fixture](benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json)：B2 九份历史响应仅作为非阻塞研究证据，不替代 PROD-GLOBAL-1 的真实游戏验收。
- [API0 Responses benchmark 接口](GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)：历史纯开发者端 benchmark，不是当前 Android 入口。开发机现有 `tools/storyteller_gateway.py` 及 Android AI_ASSISTED 代码见 PR #292；真实 Android E2E 尚未验收。
- [通用说书人 Policy 规范](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)：推荐只对 Host 的合法选择 rank/recommend/explain，不继承旧自动特例。
- [GSP-R1 现有历史捕捉审计](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md) 与 [registration ambiguity 约束](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md)：已实现机械历史继续有效；C2C-2/3 可选扩展目前暂停，不能因旧 NEXT 文字擅自恢复。

## 本轮已验证的工具与待验收项

- **Mini MCP 直接读取开发机评测报告：已实际验收。** `read_botc_evaluation_report({report_id:"prod-global-1c"})` 已成功返回固定私有报告和 SHA-256，不再需要人工上传同类 JSON；Mini MCP `master@f40e202`，服务已重启。该报告是三次 Luna 真实 API、**合成**游戏状态，非 Android E2E。
- 首夜合成测试 A2 的“厨师已确认 → 调查员给信息”不是正式 Trouble Brewing 首夜顺序，**只能证明历史覆盖后 API 延续理解，不能算真正首夜时序验收**。
- [产品路线和待测项](CURRENT_DEVELOPMENT_ROADMAP.md)：先做 API 延迟分段、不同 `reasoning.effort` / 输出长度的**同一局面质量-时间**对比，之后实施展示身份阶段的后台联合规划和全局事件驱动增量刷新；最终验收仍需 Android 真实操作。

## 长期架构与独立证据

- [RES-0～5 分离与物理清理](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)：Game Engine / Manual 与 Provider 分离，已接受。
- [核心模块所有权目标](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md) 和 [TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)：共享只读上下文，不建立第二个可写 Game State。
- [HOST-ML1 中立 decision export](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)：为测试和未来模型保留可替换接口。
- [旧推荐系统退役审计](LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md)：仅作为已删除旧引擎的证据，不授权继续旧 policy。
- [历史归档入口](archive/README.md) 和 [2026-10-09 分支保留/清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md)。

## 判断冲突与维护规则

官方 BoTC 规则负责游戏规则真伪；`AGENTS.md` 负责协作执行与所有权；`TESTING_STRATEGY.md` 负责测试；**当前 roadmap + handoff + 最新 GSP-PROD-AUTO 授权（不取消先前 Recovery 范围边界）** 负责下一步任务。专项旧路线与 archived docs 仅作为历史/设计证据；Mini MCP developer memory 仅供导航，不覆盖 live 状态。

历史 SDE/C5/DLB/TBGS/RSR/LRE 完成状态不再占据活动入口，也不能从其旧 `NEXT` 推断可实施任务。迁移、归档和删除文件前应先核对实际引用，确保不损坏深链接；完成一次阶段验收应回收过时 handoff，而不是叠加新入口。
