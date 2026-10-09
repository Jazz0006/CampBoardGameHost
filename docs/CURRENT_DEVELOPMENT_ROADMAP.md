# CampBoardGameHost — Current Development Roadmap

> Updated: **2026-10-09 Australia/Sydney**. **唯一当前状态与优先级权威**；不要从 archived docs 或旧 PR 的 `NEXT` 继续施工。
>
> 2026-10-09 收敛前的完整路线、PR/CI/R2 验收与逐步历史已完整归档为 [pre-MEM0 roadmap snapshot](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)。需要复核已完成 stage 的 exact-head 验收时查此快照及相应原始 PR，不把这些历史重写到本文件。

## 1. 当前唯一执行顺序 — **PRODUCTION FIRST / AUTONOMOUS GOAL**

> **2026-10-09 最新产品纠偏 — PROD-GLOBAL-1 为 PROD-1 的强制补充/覆盖：** 先基于全局已定座位、完整显示角色、尚未确认的酒鬼合法域分析整局脆弱点、信息链路、邪恶压力及未来连锁裁量，形成可修订的全局策略，再推荐**当前**合法决定。发完牌后在说书人专用方桌界面展示整局局势、座位间关系与后续计划；后续动作必须引用同一策略并按新事实更新。MANUAL 不调用 LLM、不出现 AI 推荐；AI_AUTOMATIC 经过 Host 验证自动提交。详见 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。**PR #292 当前代码虽然 CI/R2 GREEN，但仍是单点推荐 SPIKE / Draft / 产品验收 NO-GO，不可直接合并。**



> 2026-10-09 **最新用户产品授权覆盖**：不再要求 MEM0 盲评、MEM1 真人局研究或进一步 Recovery 覆盖完成后，才准生产接入。直接推进可玩的 LLM 推荐与自动说书人。唯一执行设计权威：[GSP-PROD-AUTO](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。

| 顺位 | 工作 | 状态 / 边界 |
| --- | --- | --- |
| **NOW** | **PROD-0：真实运行入口/所有权审计** | 找到现有 Host pending decision、RES provider contract、上下文与确认路径；选择一个真实 TB 决策为最小生产切入。不要再建新的推荐算法或全面扩展历史 |
| **NEXT** | **PROD-1：整局全局策略分析先行 + AI_ASSISTED** | 真实完整阵容/关系 → 先诊断最重要的局势问题、交叉信息链与未来计划 → 当前合法候选推荐并链接全局策略 → Host 校验 → Host-only 方桌图形化分析/关系/备选 → 人工确认；下一决策增量更新策略。不可用独立酒鬼推荐作为验收。 |
| **THEN** | **PROD-2：AI_AUTOMATIC 自动裁量** | 用户明确开启自动模式后，由 Host 验证并调用**现有**规则确认路径自动提交；网络失败、非法或过期响应则暂停/人工接管，不静默恢复旧 heuristic |
| **FOLLOW-ON** | **PROD-3/4：TB 首夜自动主持 → 完整 TB 无人工说书人** | 扩大真实夜间/白天裁量家族、输入采集、顺序与终局，逐段验收完整可玩 |
| **NON-BLOCKING** | GSP-MEM0/MEM1、EvidenceLab、模型对比与训练 | B2 已有 9/9 真实结构化结果；质量与记忆优势未完成正式盲评。按实际产品问题选择性验证；不阻塞 PROD-1 |
| **PAUSED** | GSP-R1C2C-2/3 及 Recovery 历史扩展 | 只有真实游戏决策的不可替代缺口才可重新进入；Recovery 仍限当前格式、≤4h 紧急续局 |

**模型策略：** 产品所有者报告强模型 Sol 的直接测试质量高于近期 Luna API 试验；这是主观质量观察，不宣称对应公开 API 模型 ID 或统计显著性。生产模型可配置且质量优先，避免弱模型实验结果阻塞部署。

**不可误读：** 旧文档中“MEM0 NOW”“盲评或 R1 历史补齐是生产 API 前置条件”“永远必须人工确认”等均由本次新授权覆盖；人工确认仅为 AI_ASSISTED 模式的契约。旧算法不复活。

> **2026-10-10 再次纠偏 — 真人说书人持续判断，不做角色特例：** PROD-GLOBAL-1/2 的战略连续性以**全游戏、全阶段的已确认因果事件流**驱动，不是单独的投毒者或酒鬼重算器。既要考虑真实生效/无效的能力行动，也要考虑公开但无机械效果的声称、杀手射击、处女提名、已展示信息及其对玩家认知的影响；明确区分真实机制、玩家看见或声称的内容、未证实的推测和说书人可修订的战略意图。按时点重新评估**未来仍待决定**的动作，绝不因白天新事件回写首夜历史，绝不以技能名 if/else 复活旧推荐算法。完整设计与多阶段验收场景见 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。

## 2. 冻结的架构与产品原则

1. **规则与游戏权威独立：** Host/Game Engine 拥有 canonical current game/session、rules、legal candidates、committed facts 和游戏状态写入。任何 LLM/provider 只返回合法候选的推荐、备选和理由，**provider 本身不得写入事实**；AI_ASSISTED 由人确认，显式启用 AI_AUTOMATIC 后可由 Host 在即时合法性/版本核验通过后调用现有确认路径自动提交。
2. **离线优先：** 在没有模型、网络、额度、有效响应时，Manual 主持、规则与合法选择仍可运行。唯一合法结果可由 rules 自动确定；多个未获合格推荐的合法结果：手动模式为 `MANUAL_REQUIRED`，自动模式须暂停并提示/支持人工接管；**不得**擅自选择任意结果。
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
| GSP-MEM0 | B2 经只读批次审计：9 attempted / 9 succeeded / 9 response JSON；正式盲评仍未完成，转为 NON-BLOCKING |
| GSP-API0 / DEV-EXEC1/2 | #288 API0 及 Mini MCP Oracle 远程离线/付费实验已验收：B2 9/9，5,187 input / 12,667 output tokens（私有批次审计）；**不等于 Android 生产接入或模型质量验收** |

## 4. 执行与验收门

- **先执行 PROD-0/1。** 每轮核对 live GitHub main、所有开放 PR、本地工作树，按 `AGENTS.md` 选择最小变更；在可运行的真实 TB 决策上验收。参考 [生产自动说书人路线](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。
- PROD-1 的 **必要**测试：真实全座位/显示身份与合法候选上下文、模型结构化有效返回、非法 ID、已变更 decision/revision、超时离线/额度失败、密钥不进入 APK、手动流程可用；无 MEM0 大样本质量研究前置门。
- PROD-2 的 **必要**测试：自动模式必须显式选择、单次合法确认、取消/去重/过期拒绝/状态重复进入、无提供方时暂停；所有状态提交属于 Host。整个完整自动 TB 游戏是 PROD-4 的独立目标，不能由单决策成功宣称完成。
- Oracle VM 当前是 **开发机**，其已完成的 Mini MCP 执行/结果返回不构成安全的 Android 生产网关。生产需要有鉴权、额度限制、TLS、服务端密钥保管及私有数据最小化的实际部署。
- MEM0 旧冻结合成 fixture/评分协议继续作为证据但 **不阻塞** 产品；若研发时遇到具体质量退化才开展针对性评估。Host 缺什么字段就对着真实用例补，不扩展任意历史 Replay/Recovery。
- 当前新方案不创建旧 heuristic/special-policy 推荐，不改变规则权威、Spy/Recluse 多 witness 结果优先语义、现有 Recovery 范围。无关文档和分支清理不可阻塞 PROD-0/1。

## 5. 权威索引

- [最新产品路线：生产 LLM 接入与自动说书人](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。
- [唯一当期 handoff](NEXT_DEVELOPMENT_HANDOFF.md)、[文档导航](README.md) 与根目录 [AGENTS.md](../AGENTS.md)。
- [GSP 通用 policy 设计](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)，以本路线、2026-10-09 memory/recovery 决定作为后续优先级覆盖。
- [RES 架构解耦和旧引擎退役](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)、[核心所有权](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)、[TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)、[HOST-ML1](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)。
- 2026-10-09 之前全部工程阶段详情与 exact-head 验收：参阅 [旧 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)；不再在唯一执行计划里堆叠几百行历史。
