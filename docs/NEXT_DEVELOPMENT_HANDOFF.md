# CampBoardGameHost — Next Development Handoff

> Updated: **2026-10-09 Australia/Sydney**. **唯一活动 handoff**，会话开始必须重新查 live GitHub / 本地状态；旧 PR/head 数值不代表现状。完整的旧交接与逐步验收历史在 [归档快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_MEM0_CONVERGENCE_2026-10-09.md)，其中旧 NEXT 均无执行权威。

## 1. 进入工作前

1. 阅读根目录 `AGENTS.md`、[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [当前文档入口](README.md)。行为变更另查 [测试策略](TESTING_STRATEGY.md)。
2. 实时核对 GitHub `main`、open PR、branch HEAD 和本地 working tree。不要丢弃本地未提交改动。
3. 本次全局文档收敛开始时的基线为 `main@2886dcca876ef81a164ef700d748408223c029d3`（PR #288 已合并），仅作为历史记录，不是下一轮必然的 HEAD。

## 2. 下一项：GSP-MEM0

**NOW：运行冻结的独立模型 A/B/C 记忆消融盲测，而不是继续扩展存档。**

- 产品授权：[2026-10-09 范围决定](GSP_MEMORY_RECOVERY_SCOPE_DECISION_2026-10-09.md)；协议：[MEM0 ablation](GSP_MEMORY_ABLATION_EXPERIMENT_2026-10-09.md)；fixture：[合成 TB8 多步局](benchmarks/GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json)。
- A 只给当前 Host 权威事实/合法候选；B 加当前决策前冻结的真实历史；C 加显式区分假设、动机、计划与不确定性的战略摘要。必须同一合法 as-of 时点，不携带未来信息，不用“是否猜中作者预选答案”当唯一分数。
- 独立记录整局信息生态、公平性、多种合理世界、记忆一致性、玩家压力、可解释备选、错误、成本与延迟；独立模型调用和盲评 **仍待完成**。实验数据不得声称已验证模型效果。
- 后续 GSP-MEM1 审查结果，优先使用 EvidenceLab 可重建完整真人局复核；只为真实改进补充最小信息 producer。C2C-2/3 的旧历史覆盖计划 **PAUSED**，并未取消已有规则/真实语义的正确性。

## 3. 并行的 API0（已实现，不是 Android 生产）

[API0 transport 使用说明](GSP_API0_OPTIONAL_RESPONSES_BENCHMARK_TRANSPORT_2026-10-09.md)：PR #288 已将 Python `tools/gsp_api0_responses.py` 合入 main；可生成 9 个冻结 A/B/C×D0/D1/D2 请求指纹，默认不发网络请求。开发者须自备 `OPENAI_API_KEY`、指定可用 `OPENAI_MODEL`，显式 `--live` 并设置 `--max-requests` 和 repo 外私有输出目录后才能产生计费调用。先做 1 次有界实际请求，核查 schema、合法 ID 和费用，再考虑整组 9 次。不要把 key 写入源码或 APK，不要自动发送任务或重试。

此功能 **没有**验证真实 OpenAI API 模型质量，没有上线 Android；后续 API-1 需要服务端保管密钥的认证网关和 Host 本地合法性/状态新鲜度复验，仍只展示建议，由说书人确认。

## 4. 施工禁区与保留项

- `Host truth > provider memory`：Model 的连续对话/长短期战略记忆可以帮助推理，但不能作为规则、角色、真实登记、游戏状态的最终事实来源。Provider 可替换，手动流程必须离线完整。
- Recovery 仅同当前格式/令牌、**≤4 小时紧急续局**；不扩展长期存档、历史版本迁移、任意 cut-off strict replay。PR #286 已关闭且未合并，**不得重复添加已由合法 setup 保证的酒鬼 shown-role collision 校验**，除非发现可达输入边界缺陷。
- 已有 Drunk shown Virgin 的首次提名兼容真实修复（#284）、真实 TB/NGJ 公共/裁定行动记录（#282 等）要保留。Spy/Recluse 结果若有多个合法 witness，只将 Storyteller 真正明确裁定的注册记成明确事实。
- Legacy heuristic/style/special-policy 已退休。不得重新引入 named Drunk/Mayor/Investigator if/else 自动排序。
- GSP-R2 玩家资料、R3 跨局经验与推荐多样性、R4 production materializer 未作当前选择；旧关闭未合并的 PR #232 分支 `gsp-2b3b-player-context-edit-surface` 有参考代码，**保留其远端分支**，日后需重新按 live 架构审计，不能照搬。

## 5. 文档/分支清理的独立交接

参见 [分支清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md)。已把收敛前的 roadmap、handoff 和 docs index 作为保留原文的 archive snapshot；活动入口不再包含 DLB/C5/GSP-R1C 的逐提交流水账。Git 分支删除必须基于已经 merge/closed 的精确 PR、独立内容审查和显式保留白名单。没有得到完整证明前不删除未合并的唯一证据分支；分支清理不得覆盖 MEM0 工作。

## 6. 本轮验收

文档-only 更改不需要伪造 Android RED 测试；需检查 diff、文档相互引用、无业务代码变更、GitHub 独立 PR/checks。后续任何功能行为改动按 `AGENTS.md` 和 `TESTING_STRATEGY.md` 进行风险分级验证。
