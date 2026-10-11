# CampBoardGameHost — Current Development Roadmap

> Updated: **2026-10-10 Australia/Sydney**. **唯一当前状态与优先级权威**；不要从 archived docs 或旧 PR 的 `NEXT` 继续施工。
>
> 2026-10-09 收敛前的完整路线、PR/CI/R2 验收与逐步历史已完整归档为 [pre-MEM0 roadmap snapshot](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)。需要复核已完成 stage 的 exact-head 验收时查此快照及相应原始 PR，不把这些历史重写到本文件。

> **2026-10-10 PROD-GLOBAL-1D-3（Luna compact-on-live，开发分支待 Android 验收）：** 延迟实验在同一个真实合法 8 人 TB 调查员候选决策中取得完整 A 22.858s/2193 output tokens、简洁 B 7.516s/646 tokens、仅 ID C 8.957s/782 tokens。第 4 个请求 HTTP 503，后两个场景没有模型验证，故 **B 是单场景初步指标而非整局证明**。用户明确优先 Luna/成本与实战质量。当前 PR #292 分支实现针对 **AI_ASSISTED 的实时合法决策** opt-in `COMPACT_MEMO_V1`，返回唯一候选 + 一句可延续的暂定战略摘要；开局分析和酒鬼决策仍原完整协议。旧 Gateway 完整响应兼容；Host 校验事件游标、决策 ID、revision 和合法候选，助手不自动确认。此变更尚未经过整局 Android→Gateway→LLM E2E 验收，不能视为产品通过，也不能把 AI_AUTOMATIC 说成全自动完整 TB。**NEXT：实际 8 人 TB 一局贯穿首夜与白天/次夜并记录每次 Host 事实、玩家已见信息、模型候选、接受/人工覆盖、合法性与耗时；错误/503 人工接管；分析连贯性后决定扩大自动覆盖。**

> **2026-10-11 真机新阻塞 — 完整首夜方案不是可选附加：** 用户已通过 Android Studio 直连获得调查员 AI 建议，但未同时获得恶魔三个掩饰身份。经代码核查，当前 committed analysis 只包含 issue/relations/intentions，首夜仍按单个 pending role 请求建议，恶魔掩饰身份在独立手动确认路径。这**尚未实现用户要求的「首夜全局信息/恶魔伪装联合规划」**，不得将「调查员信息 AI 可用」视为完整首夜验收。**立即将全首夜 bundle typed Host legal domains + LLM 一次联合计划 + 事件前缀驱动的剩余计划重算置于生产最高优先级**，详细边界见 PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md 的 PROD-GLOBAL-2 节。2026-10-11 的独立 UI 缺陷是调查员阶段仍有低对比度字色；PR #293 的共享主题、全屏方桌 foreground contract 和 WCAG AA 4.5:1 自动测试为第一步，需真实手机截图复核。不使用单角色推荐器或角色特例补洞。

> **2026-10-11 新执行目标：单一推荐首夜配置 + 一键采纳。** 模型内部保留整局联合思考，前台不再输出大段 issues/intentions/alternatives，而是返回唯一推荐配置：恶魔三个合法掩饰身份及首夜各项可立即确定的 Host 合法候选；等待投毒、占卜师选人的项目须明确标为依赖待定。第一阶段假设说书人不手动覆盖，但不得预设玩家行动或改变游戏规则。首先完成 Host 全首夜合法域输入、单一结果校验、模型/Gateway 协议及 UI 一键采纳；已经确认玩家看到的信息不可追改。共享深色主题必须拥有背景和文字颜色，并在 CI 对真实 Material 调色板的全部文字/背景配对做 WCAG AA >=4.5:1 检查；禁止靠调查员专用补丁蒙混通过。
>
> **当前进度边界：** 已新增 FirstNightOneShotPlanV1 的纯合同/测试及全局 Surface/主题对比度加强（PR #293 的开发分支），**尚未接入可执行的模型首夜单包及一键采纳**；这是真实生产验收阻塞，不能称已完成。

## 1. 当前唯一执行顺序 — **PRODUCTION FIRST / AUTONOMOUS GOAL**

> **2026-10-10 当前状态：PROD-GLOBAL-1C 代码就绪；PROD-GLOBAL-1D 正式接棒。** Draft [PR #292](https://github.com/Jazz0006/CampBoardGameHost/pull/292) 已在真实 Host 信息决策接入统一全局推荐：首夜 Washerwoman/Librarian/Investigator pair，Chef/Empath number，Fortune Teller 选定对象后 Boolean，以及跨白天/次夜的通用因果续航；Gateway 服务端认证/限额与 Android HTTPS 客户端已接通代码边界。Android HTTPS 地址现在会持久保存，Bearer 令牌经 Android Keystore AES-GCM 加密保存，`13b31d0` 的 Android CI #4049、R2 #3677、Gateway #59 GREEN。**尚无真实 Android 整局 E2E 产品验收，不合并 Draft。**
>
> **真实模型 / 开发机工具验收已发生：** Oracle Gateway 对 `gpt-6-luna` 完成三次**付费、结构化、合成 TB** 请求：22.83 / 25.92 / 25.32 秒，最后请求完成不代表续航更快。合成 A2 先 Chef 再 Investigator 与 TB 正式首夜顺序不符，只能作为 API continuation smoke，须修正真实 Host 时序。Mini MCP `master@f40e202` 的 `read_botc_evaluation_report(report_id="prod-global-1c")` 已实际返回真实报告与 SHA-256；以后不必手动上传这一固定报告。**这些都不是 Android E2E 证据。**
>
> **酒鬼时序用户纠偏：** 若 setup 已预留 Drunk 并固定全体 shown roles，玩家**可以先逐人看到身份**，说书人**在展示结束后才选定哪位展示为镇民的玩家实际为 Drunk**；只须在受影响的首夜能力/信息裁定前绑定。当前 App 阻塞在 Drunk 选择再造正式卡牌是实现顺序，不是规则强制。PROD-GLOBAL-1D 将分离安全的 shown-only 展示与待确认实际身份，在逐人展示时后台全局预规划 Drunk/Pair/首夜信息；投毒与 Fortune Teller 选对象等未发生行动只能条件规划，发生后 Host 校验和按全局事件重规划**未执行**裁量。不制造角色特例策略。
>
> **下个会话首先验证延迟瓶颈，不空等重构：** 当前 Gateway 不记录完整的 API 推理/输出 token、首字时间、HTTP 上传下载等分段，因此 20–26 秒不能判定为网络延迟或推理延迟。固定同一个真实合法 TB 上下文，加入无密钥的计时/usage 采集，有限预算对比 API 推理强度、结构化输出长度、首次 vs 续航对结果质量与耗时；交互刷新 **< 5 秒**是体验目标，尚未实现或证明。然后实施预取和 event-driven delta，MANUAL 完全离线，AI_ASSISTED 仍由真人确认，AI_AUTOMATIC 由 Host 校验后自动确认。
>
> 原 GSP-PROD-AUTO 的 `PROD-0 NOW`、独立单角色入口与“尚无 Gateway”均是 **2026-10-09 阶段历史**，不是现在的待办。旧路线只提供长期自动主持目标；当前详细方案以 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md) 为准。

| 顺位 | 工作 | 当前状态 / 退出条件 |
| --- | --- | --- |
| **NOW** | **PROD-GLOBAL-2B：完整首夜唯一推荐包与一键采纳** | 同一合法上下文实测 HTTP 总/上游耗时、model API usage、输出长度、推理强度和首次/续航；拒绝以聊天界面体感替代 API 证据；下一轮优先实施 |
| **NEXT** | **PROD-GLOBAL-1D-1：展示身份期间的首次全局预规划** | shown roles 全部固定 → 前台逐人展示 → 后台规划 Drunk 和首夜条件信息；发牌后绑定实际 Drunk，再按事件有效性验证剩余合法域 |
| **THEN** | **PROD-GLOBAL-1D-2：全局事件驱动增量重算** | 投毒、人工部分覆盖、夜间/白天其他已确认事件统一进 Host causal prefix，撤销过时建议，≤5 秒作为实验目标；未达到时明确等待/人工接管 |
| **PRODUCT GATE** | **PROD-1 Android 真实 E2E / PR #292** | 真机→HTTPS Funnel→Gateway→LLM→Host 法定决策、时序、部分覆盖、失效拒绝和手动回退验收后才评估 Draft 合并 |
| **FOLLOW-ON** | **PROD-2/3/4：自主裁量 → 首夜 → 完整 TB** | 完整 Auto 决策覆盖、出错暂停、人类玩家正常交互和整局验收；无旧 heuristic |
| **NON-BLOCKING** | MEM0/MEM1、EvidenceLab、训练、旧 GSP-R1 历史扩展 | 仅针对当前质量证据缺口，不作为生产前置项；Recovery 仅当前短期意外关闭 |

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
| GSP-API0 / DEV-EXEC1/2 / PROD-GLOBAL-1C | 历史 API0、B2 9/9 与当前 Gateway 三次真实 Luna 合成测试均有独立记录；Mini MCP 新固定报告读取器已实读验收。Android Keystore/Gateway 客户端本轮 CI GREEN；**全局预规划、低于 5 秒响应及真机全流程均待验收** |

## 4. 执行与验收门

- **下轮先执行 PROD-GLOBAL-1D-0（测量）再实施 1D-1/2（预规划 / 重算）**。每轮核对 live GitHub main、开放 PR、本地 working tree 和 CI；仍须用真实 TB Host 合法候选、真实首夜顺序及 Android E2E 验收产品。参考 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。
- PROD-1 的 **必要**测试：真实全座位/显示身份与合法候选上下文、模型结构化有效返回、非法 ID、已变更 decision/revision、超时离线/额度失败、密钥不进入 APK、手动流程可用；无 MEM0 大样本质量研究前置门。
- PROD-2 的 **必要**测试：自动模式必须显式选择、单次合法确认、取消/去重/过期拒绝/状态重复进入、无提供方时暂停；所有状态提交属于 Host。整个完整自动 TB 游戏是 PROD-4 的独立目标，不能由单决策成功宣称完成。
- Oracle VM 是 **开发机**：Gateway 已有本地服务端密钥、Bearer、配额与 Tailscale Funnel HTTPS；开发机/本机 smoke 已实测，**仍须手机真实调用及运行时安全性、时序、配额测试**。Mini MCP 工具权限不等于 Android Gateway 权限。
- MEM0 旧冻结合成 fixture/评分协议继续作为证据但 **不阻塞** 产品；若研发时遇到具体质量退化才开展针对性评估。Host 缺什么字段就对着真实用例补，不扩展任意历史 Replay/Recovery。
- 当前新方案不创建旧 heuristic/special-policy 推荐，不改变规则权威、Spy/Recluse 多 witness 结果优先语义、现有 Recovery 范围。无关文档和分支清理不可阻塞 PROD-0/1。

## 5. 权威索引

- [最新产品路线：生产 LLM 接入与自动说书人](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。
- [唯一当期 handoff](NEXT_DEVELOPMENT_HANDOFF.md)、[文档导航](README.md) 与根目录 [AGENTS.md](../AGENTS.md)。
- [GSP 通用 policy 设计](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)，以本路线、2026-10-09 memory/recovery 决定作为后续优先级覆盖。
- [RES 架构解耦和旧引擎退役](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)、[核心所有权](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)、[TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)、[HOST-ML1](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)。
- 2026-10-09 之前全部工程阶段详情与 exact-head 验收：参阅 [旧 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)；不再在唯一执行计划里堆叠几百行历史。


### 2026-10-11 PROD-GLOBAL-2B implementation checkpoint — one-shot first-night actionability

The owner accepted revised cross-screen color readability on real Android and requested implementation priority on a SINGLE recommended whole-night configuration. In experimental personal direct AI_ASSISTED mode only, code now constructs one Host-supplied legal information scope from current first-night materialized Manual domains, legal off-board Demon bluff role IDs and actual Good red herring seats. It provides compact hashed candidate IDs with Host-authored result descriptions so the model can analyze the whole roster in ONE structured Responses call. The model output contains exactly one eligible candidate per available decision, one distinct legal bluff trio if applicable, and exactly all deferred choice/dependency IDs — without prose issue essays or alternatives. All are fail-closed validated and gated on exact game/revisions.

The private first-night preflight can display that one recommended package and a one-tap accept action, after which only Demon bluff preselection and FT red herring are stored. Other candidate IDs remain advisory and are re-matched to the exact step's current Host Manual-legal candidates before use. A Poisoner choice may invalidate a previously recommended candidate; the normal step-level live advice/manual control remains fallback; no old proposed observation is forcibly published. Unknown FT player query is deferred. The old verbose whole-board checkpoint is skipped ONLY for personal direct assisted first-night path; Gateway and automatic remain unchanged until independently upgraded.

**Acceptance not yet granted** until current-head full Android CI and device end-to-end: one full actual opening including Demon's triple, Investigator, Chef or Empath, Poisoner and deferred FT, plus change/staleness, offline manual fallback, latency and cost. Never treat unexecuted accepted plan IDs as Host observations or Recovery facts. Real-phone readable-color feedback applies to current visual baseline.
