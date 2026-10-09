# CampBoardGameHost — Current Development Roadmap

> Updated: **2026-10-09 Australia/Sydney**. **唯一当前状态与优先级权威**；不要从 archived docs 或旧 PR 的 `NEXT` 继续施工。
>
> 2026-10-09 收敛前的完整路线、PR/CI/R2 验收与逐步历史已完整归档为 [pre-MEM0 roadmap snapshot](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)。需要复核已完成 stage 的 exact-head 验收时查此快照及相应原始 PR，不把这些历史重写到本文件。

## 1. 当前唯一执行顺序

| 顺位 | 工作 | 状态 / 边界 |
| --- | --- | --- |
| **NOW** | **GSP-MEM0**：冻结 TB 8 人合成多决策例子的 A/B/C memory ablation，采集独立模型回答并盲评 | Fixture / rubric **已冻结；模型运行和盲评仍待进行**。不预设 C 优于 A/B |
| **PARALLEL (bounded)** | **DEV-EXEC1 / GSP-API0**：开发机 Mini MCP 远程离线任务 | [执行与隔离规范](DEV_EXEC1_ORACLE_REMOTE_EXPERIMENT_WORKFLOW_2026-10-09.md)；Mini MCP PR #8 代码本地测试已过，**实际服务重启/新任务执行待验收**；API0 单次付费烟雾测试已成功，不代表九组 MEM0 或模型质量 |
| AFTER MEM0 | **GSP-MEM1**：比较全局质量、连续性、矛盾/泄漏、成本及可移植性；用 EvidenceLab 可重建全局真人局进行复核 | 根据实验结果决定最小必需事实/记忆形态 |
| CONDITIONAL | **GSP-R1C2C-2/3** 信息生产者、setup/裁量历史扩展 | **PAUSED**；仅对有具体真实用途、真实可达缺口、可量化推荐价值的事实重新开启；不再把严格历史重演/Recovery 完备作为每个 family 的先决条件 |
| LATER, gated | GSP-R2 玩家信息、R3 跨局多样性、R4 生产格式化 provider request/response、API-1 Android 可选联网 | 尚未全面实施；必须先过语义/安全/质量/隐私门；无网络游戏完整可用 |
| INDEPENDENT | 局部 App/Host 维护与真实规则缺陷 | 仅在可证实的维护需要出现时独立处理，不阻塞 MEM0，也不借机再拆大模块 |

**不可误读：** 旧文档中“C2C-2 NEXT”“GSP-R1 后立刻做 R2/R3/R4”“API 必须等整个历史收敛完成”“旧 SDE/特例推荐等待 cutover”等语句现均非当期指令。

## 2. 冻结的架构与产品原则

1. **规则与游戏权威独立：** Host/Game Engine 拥有 canonical current game/session、rules、legal candidates、committed facts 和游戏状态写入。任何 LLM/provider 只返回合法候选的推荐、备选和理由，不得自动提交或伪造事实。
2. **离线优先：** 在没有模型、网络、额度、有效响应时，Manual 主持、规则与合法选择仍可运行。唯一合法结果可由 rules 自动确定；多个未获合格推荐的合法结果为 `MANUAL_REQUIRED`。
3. **旧引擎退役：** RES-0～5 的 heuristic/style/weighted/named special-policy 自动推荐权限已移除或物理删除；GSP-1 已撤销 Q04 Drunk、functioning Librarian V2、INV1-A 的多候选特例自动权威。旧案例/算法只能供证据、benchmark 或审计。
4. **Strategic memory 允许且需检验：** Host 真实事实和 legal domain 优先；LLM 短期推理/战略摘要、跨局经验与相同局面多样性是 **advisory**，明确区分 FACT / INTENT / HYPOTHESIS / UNCERTAINTY；模型记忆不等于可写规则事实。
5. **Recovery 的产品范围严格限定：** 仅同一当前版本格式、精确兼容令牌、**4 小时以内**的当前游戏突发关闭恢复（restore game, not App）。不新增跨版本迁移、长期未完局存档、任意历史 cut-off 复演、无可达风险的重复 setup 校验；既有已验证的真实机械正确性与短期恢复保持不变。
6. **结果优先的登记语义：** Spy/Recluse 可具有多种合法见证解释（如 Chef/Empath 数字），显示结果不唯一指定登记；仅记录真实明确选择，不把推导的第一个 witness 当成裁定。
7. **训练/数据边界：** EvidenceLab 收集来源可信、可追溯的完整对局，HOST-ML1 提供中立合法候选导出；Host 不依赖训练模型，也不重回单案例 if/else 排序。

## 3. 阶段进度（聚合状态，不复制逐 PR 历史）

| 轨道 | 最新认可状态 |
| --- | --- |
| D6 / App-Host ownership / EPI / SDE foundations / C4 / IF-D / RH-E | 已完成；部分旧 SDE policy 历史身份冻结，无生产推荐权威 |
| DLB-0～7 / first-night dependency、TBGS-0/1 | **COMPLETE / ACCEPTED** |
| TBGS-2A～2E | **COMPLETE / ACCEPTED**，2F 未选择 |
| C5-A～E / old Investigator / Q04 special policies | 实验和历史验收保留，**GSP-1 后自动裁量权已撤销** |
| HOST-ML0 / HOST-ML1 | **COMPLETE / ACCEPTED** 中立模型/benchmark 边界 |
| RSR、LRE-1、GSP-0/1/2 基础、RES-0～5 | **COMPLETE / ACCEPTED**；旧 LRE family-by-family 路线已替换 |
| GSP-R1A / R1B / R1C1 / R1C2A / R1C2B | **COMPLETE / ACCEPTED**，但不意味着任意历史时点可恢复 |
| GSP-R1C2C-1 已限定的真实脚本裁定/白天公共动作 | **已验收**（PR #263、#265、#267、#269、#276、#278、#280、#282、#284）；Drunk-shown Virgin P1 已修复；非真实脚本的 synthetic Klutz/Spy writer 已撤销 |
| GSP-R1C2C-2/3、R1C overall | **未完成且当前暂停**，只保持有用的现有事实与安全功能 |
| GSP-MEM0 | 设计、合成 fixture 冻结（#287）；独立实验 **PENDING** |
| GSP-API0 / DEV-EXEC1 | #288 已合入；开发者单次 S001 真实 API 调用 **1 attempted / 1 succeeded**，结构及合法候选已审；**九组 MEM0、模型质量、Mini MCP 远程任务执行均未验收** |

## 4. 执行与验收门

- 每轮先查 **live GitHub main、全部开放 PR、本地工作区**，再按 `AGENTS.md` 选择最小安全执行路径。不要引用固定 HEAD 当现状。已通过验证的完整 PR 在符合规范后可以直接合并；失败、不完整或扩大范围的工作不得以清理为名合并。
- MEM0：[范围决定](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md) → [ablation 设计](GSP_MEMORY_ABLATION_EXPERIMENT_2026-10-09.md) → [冻结 fixture](benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json)。A＝当前事实与合法候选；B＝A 加事实历史；C＝B 加清晰标注的战略记忆。在同一合法 as-of 时点独立比较；不得偷看未来。真实局证据明确区分合成演示。
- DEV-EXEC1：[开发机远程实验执行与结果隔离](DEV_EXEC1_ORACLE_REMOTE_EXPERIMENT_WORKFLOW_2026-10-09.md)，当前仅离线、无额外计费授权；Mini MCP systemd 重启状态须经实际 instance ID 验证，旧任务白名单仍阻塞生产 canary。
- API0：[开发与密钥操作说明](GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)。`tools/gsp_api0_responses.py` 默认 offline、严格 schema、显式 `--live`、私有输出且 `store:false`；API 账单和 ChatGPT 订阅独立。不将密钥植入 Android/APK；未来 API-1 需要 authenticated secret-owning gateway、权限、限流、额度、Host freshness/legality 校验和人工确认。
- 新历史事实入库之前，需要真实支持的游戏场景、实际消费者、当前 snapshot/事件/战略摘要不能替代的原因、明确排除的非目标以及比例合适的测试。不要为填满历史日志而继续实现 Recovery。
- **无关文档、历史分支与代码重构**不得混入 MEM0 实验；文档整理遵循 [清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md)。

## 5. 权威索引

- [唯一当期 handoff](NEXT_DEVELOPMENT_HANDOFF.md)、[文档导航](README.md) 与根目录 [AGENTS.md](../AGENTS.md)。
- [GSP 通用 policy 设计](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)，以本路线、2026-10-09 memory/recovery 决定作为后续优先级覆盖。
- [RES 架构解耦和旧引擎退役](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)、[核心所有权](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)、[TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)、[HOST-ML1](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)。
- 2026-10-09 之前全部工程阶段详情与 exact-head 验收：参阅 [旧 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)；不再在唯一执行计划里堆叠几百行历史。
