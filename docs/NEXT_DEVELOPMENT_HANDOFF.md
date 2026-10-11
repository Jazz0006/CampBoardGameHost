# CampBoardGameHost — Next Development Handoff

> Updated: **2026-10-10 Australia/Sydney**. **唯一活动 handoff**，会话开始必须重新查 live GitHub / 本地状态；旧 PR/head 数值不代表现状。完整的旧交接与逐步验收历史在 [归档快照](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_MEM0_CONVERGENCE_2026-10-09.md)，其中旧 NEXT 均无执行权威。

> **2026-10-10 新方向 — 实际整局游戏优先：** 用户选择继续采用 `gpt-6-luna`，把已进行的 B 极简推荐试验（首次样本 7.52s，相对 A 22.86s；第 4 次 503）接入当前 `prod-1-live-drunk-ai-assisted` Draft PR #292 的 **AI_ASSISTED 实时决策**。Gateway opt-in `responseProfile=COMPACT_MEMO_V1` 返回唯一合法候选及 ≤200 字的临时 planMemo，Android 兼容旧完整格式。完整开局全局分析/酒鬼选择不变。前一次模型 memo 仅同局咨询上下文，并非 Host truth，不替代完整因果记录；仍由 Host 校验合法性/版本和真人确认。尚未做真机 Android E2E 与完整 TB 对局，PR 保持 Draft。**先实测整局连续首夜→白天→次夜→结束，记录可追溯人机冲突和一致性；不能用孤立 9-call 合成实验宣布整局已通过。**

> **2026-10-11 新执行目标：单一推荐首夜配置 + 一键采纳。** 模型内部保留整局联合思考，前台不再输出大段 issues/intentions/alternatives，而是返回唯一推荐配置：恶魔三个合法掩饰身份及首夜各项可立即确定的 Host 合法候选；等待投毒、占卜师选人的项目须明确标为依赖待定。第一阶段假设说书人不手动覆盖，但不得预设玩家行动或改变游戏规则。首先完成 Host 全首夜合法域输入、单一结果校验、模型/Gateway 协议及 UI 一键采纳；已经确认玩家看到的信息不可追改。共享深色主题必须拥有背景和文字颜色，并在 CI 对真实 Material 调色板的全部文字/背景配对做 WCAG AA >=4.5:1 检查；禁止靠调查员专用补丁蒙混通过。
>
> **当前进度边界：** 已新增 FirstNightOneShotPlanV1 的纯合同/测试及全局 Surface/主题对比度加强（PR #293 的开发分支），**尚未接入可执行的模型首夜单包及一键采纳**；这是真实生产验收阻塞，不能称已完成。

## 1. 进入工作前

1. 阅读根目录 `AGENTS.md`、[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 和 [当前文档入口](README.md)。行为变更另查 [测试策略](TESTING_STRATEGY.md)。
2. 实时核对 GitHub `main`、open PR、branch HEAD 和本地 working tree。不要丢弃本地未提交改动。
3. 本次全局文档收敛开始时的基线为 `main@2886dcca876ef81a164ef700d748408223c029d3`（PR #288 已合并），仅作为历史记录，不是下一轮必然的 HEAD。

## 2. 当前唯一接棒：PROD-GLOBAL-2B — 首夜唯一方案 / 一键采纳 / 全局色板

**本轮真实基线（2026-10-10）：** [PR #292](https://github.com/Jazz0006/CampBoardGameHost/pull/292) `prod-1-live-drunk-ai-assisted` 保持 Draft。既有 Host/Engine 独占规则、真实状态、合法候选与确认。已接入 TB 首夜 Washerwoman/Librarian/Investigator pair、Chef/Empath numeric、Fortune Teller 已选查询对象的 Boolean、全局战略与历史因果续航；已有次夜 SeatTarget 和跨阶段观察的测试。MANUAL 零 LLM，AI_ASSISTED 真人确认，AI_AUTOMATIC 现阶段仅有限覆盖，不能称完整自动说书人。

**设备与代码验证：** Android 调用现有 Oracle 开发机 Gateway（Bearer + 配额 + HTTPS Funnel；OpenAI API key 仅服务端）。APK 的 Gateway 地址已写入偏好设置，Gateway 令牌 Android Keystore AES-GCM 加密存储于非备份目录（`13b31d0`；Android CI #4049、R2 #3677、Gateway #59 GREEN）。需要手机实际检查重启及同签名、同 applicationId 的覆盖安装保留设置；**真正 Android→HTTPS→Gateway→LLM→Host 首夜游戏 E2E 仍未完成**。

**真实模型实验：** Oracle 上 `tools/prod_global_live_smoke.py --live` 已完成 3 次 `gpt-6-luna` 付费请求（22.83/25.92/25.32 秒）；返回结构化有效、A2 能识别人工覆盖，但质量不足以评价整局产品。报告 `~/.local/share/botc-evaluation/prod-global-1c-llm-smoke-report.json` 属于合成局，A2 的 Chef→Investigator 事件顺序 **不符合官方首夜次序**。**新的 Mini MCP `read_botc_evaluation_report({report_id:"prod-global-1c"})` 已实际读取该报告 + SHA-256**（Mini MCP `master@f40e202`，服务已重启）；不需要人工再次上传本固定报告。

**酒鬼晚绑定 / 身份展示修正（明确产品授权）：** setup 预留酒鬼并固定全体展示角色后，**可以先向所有玩家逐人展示身份，展示完再从合法的“展示为镇民”玩家中选实际酒鬼**；选择只需早于受到影响的首夜能力裁定。现有 App 把 Drunk 选择挡在角色卡生成/展示之前只是代码时序，不是 BoTC 规则。PROD-GLOBAL-1D 要新增安全的 shown-only 身份展示边界，同时后台启动**整局联合策略**，包含酒鬼、Pair、首夜条件信息规划；酒鬼与投毒、占卜师查询对象等未确认事实不得预填进 Host 真实状态。

**下轮马上做：** 对同一个规则合法、首夜真实顺序的固定局面，增加不含密钥的 Gateway/API 延迟分段和 token usage 观测；对照首轮 vs 连续请求、默认 vs 较低推理强度、完整 vs 精简全局报告，记录结果质量、合法率与总耗时。**5 秒后续重算仅体验目标，尚无任何测得证据**。基于测量再落地 `PROD-GLOBAL-1D-1` 身份展示后台全局预规划，`1D-2` Host 因果事件驱动、依剩余合法域更新战略与未执行建议；无角色专用投毒/酒鬼 if/else 推荐。未达到目标应支持旧有效建议/等待提示及人工接管，不能使用过期模型决定。

**未变约束：** 无 MEM0/MEM1、EvidenceLab、训练或扩 Recovery 的发布前置门；旧推荐 heuristic/style/special policy 均退休。模型只输出意图/推荐，Host 对 revision、legal ID 和真实顺序享有最终权威。Recovery 仅用于当前格式、≤4 小时意外中断。Spy/Recluse 多 witness 的结果优先歧义继续保留。

**查阅：** [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) → [PROD-GLOBAL-1/1D 设计与事实边界](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md) → [GSP-PROD-AUTO 长期目标](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。旧 PROD-0/1A/1B 逐提交行动列表是 Git/PR 历史，不是本轮 NEXT；仅查真实代码和 CI。

## 3. 工程/分支边界与暂停事项

- PR #292 **保持 Draft**；即使此次 docs-only CI GREEN，也不能代替正式 E2E 产品质量/安全验收。
- `Host truth > provider memory`；不虚构未记录的玩家私聊/声称，不把模型推断变成规则事实；确认过的信息不可因后续事件回写。
- 保留已经通过验收的 Drunk-shown Virgin、真实 TB/NGJ 裁定历史、短期 Recovery；**不要再次添加由合法 setup 已保证的酒鬼身份冲突特判**。
- 仍按 [分支清理审计](BRANCH_RETENTION_AND_PRUNING_AUDIT_2026-10-09.md) 保留未合并唯一证据分支，特别是 `gsp-2b3b-player-context-edit-surface`；本次是文档同步，不处理 Git 分支。
- 10 月 9 日已将 pre-MEM0 旧 roadmap/handoff/docs index 的原文归档至 `docs/archive/`；此后归档信息只供历史追溯，**不能从其旧 NEXT 复活已经退出的任务**。

## 4. 验收纪律

文档同步只需检查引用、diff 与 GitHub 独立 CI/R2；行为改动应按 `AGENTS.md` 和 `TESTING_STRATEGY.md` 选择实际测试。开新对话时先核对 **live remote main、PR #292、当前 branch/HEAD、本地工作树、CI**，绝不假设本交接记载的 SHA 仍是最新状态。

> **Full CI required:** PROD-GLOBAL compact adapter changes include Kotlin production source; use `[full-ci]` on this checkpoint so Android compilation/unit tests and APK assembly are mandatory even if prior docs-only commit caused synchronize diff classification to skip them.


### 2026-10-11 integration checkpoint — Android one-tap single bundle

Product-owner phone feedback: readable theme acceptable; do not spend this iteration on new color layouts. Experimental PR #293 now has new personal-**direct** AI_ASSISTED first-night path: setup commit stores snapshot but skips redundant verbose postcommit analysis, reveal leads to private first-night preflight; one joint Host-legal opening request yields a single Demon bluff triple + one recommendation per currently known decision. FT unknown query and not-yet-chosen Poisoner target are kept as dependencies, not imagined observations. Explicit one-tap acceptance commits bluff preselection/red herring only and retains per-information candidate IDs as advice; when night steps arrive, only current Host manual-legal matching IDs are displayed, otherwise existing legal/manual fallback applies. Full first-night Host facts and observations remain step-committed. The Gateway AI_ASSISTED path intentionally remains unchanged until its protocol is extended.

Implemented vs pending: code checkpoint currently requires full Android CI and device acceptance, including one actual 8-player first-night test with Investigator, Demon bluffs, Chef/Empath, Poisoner and FT target dependency. User specifically does not want multi-paragraph analysis page. No auto-claim that the old full analysis already delivered a coordinated plan.
