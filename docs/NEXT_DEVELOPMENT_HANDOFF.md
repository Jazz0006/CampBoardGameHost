# CampBoardGameHost — Next Development Handoff

> Updated: **2026-10-09 Australia/Sydney**. **唯一活动 handoff**，会话开始必须重新查 live GitHub / 本地状态；旧 PR/head 数值不代表现状。完整的旧交接与逐步验收历史在 [归档快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_MEM0_CONVERGENCE_2026-10-09.md)，其中旧 NEXT 均无执行权威。

## 1. 进入工作前

1. 阅读根目录 `AGENTS.md`、[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [当前文档入口](README.md)。行为变更另查 [测试策略](TESTING_STRATEGY.md)。
2. 实时核对 GitHub `main`、open PR、branch HEAD 和本地 working tree。不要丢弃本地未提交改动。
3. 本次全局文档收敛开始时的基线为 `main@2886dcca876ef81a164ef700d748408223c029d3`（PR #288 已合并），仅作为历史记录，不是下一轮必然的 HEAD。

## 2. 下一项：PROD-1 全局策略优先（真实游戏 AI 推荐接入）

> **2026-10-09 最新纠偏：** 先全局分析阵容压力/线索网络/后续裁量的互动，产生可修订的整局策略，再输出当前酒鬼等合法选择。**绝不接受酒鬼单点推荐作为 PROD-1 完成。** 模型建议与主观关系只是战略意图，不是规则事实。详细：[PROD-GLOBAL-1 全局优先设计](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。
>
> UX：MANUAL 完全无 LLM 调用及 AI 推荐；AI_ASSISTED 发牌后在 Host 私有方桌显示全局分析/座位关系/未来策略，后续每次决策显示与整局策略关联的建议；AI_AUTOMATIC 验证后自动推进、失败暂停可接管。酒鬼晚绑定要求首次分析发生在“可见阵容/座位已定，酒鬼尚未确定”时，正式发牌展示后再展示校对过的分析。个人使用 Key 的快捷部署可考虑，但不要认为编译期加密能保证 APK 密钥安全；优先利用现有开发机 Gateway + 私有 TLS 通道简化配置。

> **2026-10-10 全局设计约束：** 不要把先前的 `Poisoner target` 误解成需要单独实现的重算触发器。产品目标是尽可能模拟真人说书人的整个认知—判断—裁量循环：Host 记录并按时间顺序解释所有确实发生的事件；LLM 结合真实机械效果、玩家实际收到/公开的信息、尚不确定的合理世界、玩家水平与可修订的整局意图，对**尚未执行**的裁量持续重新规划。无效/假杀手声称依然可能影响公共认知；处女提名的触发与否是 Host 权威裁定；这些白天事件不倒灌首夜。实现和验收一套通用事件/认知机制，不写投毒者/杀手/处女各自专用的推荐排序。详见 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。

**2026-10-10 PROD-GLOBAL-1A 代码 checkpoint：** PR #292 Draft 分支 `prod-1-live-drunk-ai-assisted` 新增首次首夜 Host 真实 pair decision 的连续策略通道（`d72faee0c64fd099d056167ca4e668bd3d1d4148`）：利用原有 Session GLOBAL_V1 行动/观察因果前缀、完整角色/玩家输入/合法候选及先前整体战略，经同一个 LLM gateway 更新；AI_ASSISTED 的真实首夜页面展示合法建议，实际结果仍由主持人依现有 Host 操作确认，后续裁量读取已确认动作和信息，不再使用推荐草稿作为事实。Gateway 离线测试与独立 CI/R2 需按最新 HEAD 查验。**尚缺跨白天/后续夜晚通用裁量、全部首夜信息家族、AUTO 自动合法提交和实际 Android→TLS→LLM 端到端实测；PR #292 保持 Draft，不能合并。** 完整设计/遗留验收见 [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md)。

**2026-10-10 PROD-GLOBAL-1B 续航 checkpoint：** PR #292 Draft `1c278ffbbb8c4e1292be67f7089564e8c589b2c8`：同一事件驱动全局战略和中立合法候选验证已跨越首夜到次夜，接入现有 Host pending SeatTarget 决策；测试使用私有首夜观察、公开白天提名/无效射击和后续夜间行动。没有角色独立的 AI 策略。当前真实 Android↔TLS Gateway↔LLM 行程、Foundation 数值/布尔信息族、完整首夜和 AUTO 自动确认仍待验收；继续 Draft。后续优先用 `StructuredNumberInformationUiModel` / `StructuredBooleanInformationUiModel` 现有 Foundation 候选作为真实 Host 法定域，延伸统一 planner；拒绝另建角色专用 AI selector。独立 CI/R2 必须对照当前 HEAD 重新确认。

**2026-10-10 PROD-GLOBAL-1C 首夜信息推荐 checkpoint：** 同一事件驱动全局 LLM 推荐已接入 TB 的 Washerwoman/Librarian/Investigator 双人信息及 Chef/Empath 数值、Fortune Teller 选定两名查询对象后的布尔结果。数值/布尔使用现有 Foundation 合法候选或 Host result-first 登记歧义域；不新增角色专用推荐策略；实际显示/采纳依然由 Host 确认，改变未确认的占卜师查询对象会使原候选域无效。实现提交 `57982dcd37e172bd2140593848d8d57be069e493`，后续修订以 live HEAD 为准。**立即目标：在最新 CI/R2/Gateway 通过后开展真人 Android + 私有 HTTPS + Oracle Gateway + 强模型的首夜真实推荐质量 smoke，记录交叉信息连贯性、玩家部分覆写、邪恶方压力、真实效应和成本。不是等待 MEM0 盲测。** 需要先确认 TLS/Gateway 可达、实际 API model ID/预算及安卓运行；PR #292 仍为 Draft，AUTO 与完整游戏支持不在本 checkpoint 验收范围。

**最新产品授权（2026-10-09）：不再以 MEM0/MEM1 质量盲评作为接入生产前置条件。** 强通用 LLM 的现实建议质量已经足以支持立即做可玩的原型。产品特色长期目标是 **AI_AUTOMATIC / 无人工说书人**，而不是让人类永远确认每个裁量。

完整路线：[GSP-PROD-AUTO — 生产接入与自动说书人](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。

### 当前立即执行的 PROD-0 / PROD-1

1. 用 live GitHub main 复核已有 `StorytellerProviderRequestV1`、`StorytellerProviderResponseV1`、`StorytellerProviderRequestFactoryV1`、`StorytellerProviderResponseValidatorV1`、`StorytellerProviderGameContextBuilderV1`、真实 pending decision 与确认 owner；不要再建第二套推荐引擎。
2. 选择 **一个真实 TB 角色裁量**（优先酒鬼 late-binding），做完整链路：运行中当前游戏状态/所有显示角色/合法候选/必要已确认历史 → 格式化上下文 → 安全后端服务可配置 LLM → Host 校验 ID/来源 revision/当前可达动作 → UI 展示主方案和备选 → 调用现有 Host confirm 路径。
3. 用真实 Host 游戏态测试合法候选、缺角色信息、stale revision、网络错误/超时/无额度、无 API key 入 APK、同一决策不重复提交，以及原有离线 Manual 流程；**不要求 27 次 MEM0 复测或 MEM1 完成**。
4. PROD-1 完成后立即实现 **PROD-2 的可选自动模式**：Host 验证同一合法选择后自动确认，失败明确暂停/支持接管。随后推进 TB 首夜自动流程，最终扩展到完整无人类说书人局（但玩家实际选择/提名/投票仍需由玩家交互输入）。

**模型策略：** 用户已在直接对话中观察到较强模型（Sol）输出好于本次 Luna API 模型，不把单个较弱模型的表现当成 LLM 能力天花板。真实部署采用可配置模型、质量优先、记录费用与延迟，不假设聊天名称=实际 API model ID。

## 3. MEM0/API0/开发机状态 — 留证但不得阻塞生产

- API0 由 Host PR #288 提供 Python 开发者端 Responses script；**尚未**提供 Android 生产网关。
- Oracle Mini MCP DEV-EXEC1/2 已完成服务自重启、固定离线任务与安全结果读取。B1 预执行因输出目录非空未发出请求且永久封存；B2 私有只读审计：**9 attempted / 9 succeeded / 9 valid outputs**，累计输入 5,187、输出 12,667 tokens。B2 的一次重复运行被单批次保护拒绝，不代表又发送九次；还不能据此证明 LLM 质量或统计意义上的记忆效果。
- MEM0 现有 9 份响应可供将来审阅，D0 合成 prompt 对备选候选的显示角色信息不足、少量回答有酒鬼规则断言问题，应反馈给 **生产 context completeness**，而不是要求马上重新扩大盲测。完整 MEM0 盲评转 **NON-BLOCKING**，EvidenceLab 完整真人局检验随后按实际产品缺陷开展。
- 原 GSP-R1C2C-2/3 历史工程继续暂停；Recovery 只为同当前格式、≤4h 突发中断续局。不要又将 Recovery 当记忆或生产接入的先决条件。

## 4. 施工禁区与保留项

- `Host truth > provider memory`：Model 的连续对话/长短期战略记忆可以帮助推理，但不能作为规则、角色、真实登记、游戏状态的最终事实来源。Provider 可替换，手动流程必须离线完整。
- Recovery 仅同当前格式/令牌、**≤4 小时紧急续局**；不扩展长期存档、历史版本迁移、任意 cut-off strict replay。PR #286 已关闭且未合并，**不得重复添加已由合法 setup 保证的酒鬼 shown-role collision 校验**，除非发现可达输入边界缺陷。
- 已有 Drunk shown Virgin 的首次提名兼容真实修复（#284）、真实 TB/NGJ 公共/裁定行动记录（#282 等）要保留。Spy/Recluse 结果若有多个合法 witness，只将 Storyteller 真正明确裁定的注册记成明确事实。
- Legacy heuristic/style/special-policy 已退休。不得重新引入 named Drunk/Mayor/Investigator if/else 自动排序。
- GSP-R2/R3 玩家资料与跨局经验按照真实使用需要渐进接入，**R4 的必要部分已进入 PROD-1 的生产上下文/格式化与验证工作**；旧关闭未合并的 PR #232 分支 `gsp-2b3b-player-context-edit-surface` 有参考代码，**保留其远端分支**，日后需重新按 live 架构审计，不能照搬。

## 5. 文档/分支清理的独立交接

参见 [分支清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md)。已把收敛前的 roadmap、handoff 和 docs index 作为保留原文的 archive snapshot；活动入口不再包含 DLB/C5/GSP-R1C 的逐提交流水账。Git 分支删除必须基于已经 merge/closed 的精确 PR、独立内容审查和显式保留白名单。没有得到完整证明前不删除未合并的唯一证据分支；分支清理不得覆盖 MEM0 工作。

## 6. 本轮验收

本轮为生产优先级文档路线决策，不是已完成 Android 接入。文档-only 更改不需要伪造 Android RED 测试；需检查 diff、文档相互引用、无业务代码变更、GitHub 独立 PR/checks。后续任何功能行为改动按 `AGENTS.md` 和 `TESTING_STRATEGY.md` 进行风险分级验证。
