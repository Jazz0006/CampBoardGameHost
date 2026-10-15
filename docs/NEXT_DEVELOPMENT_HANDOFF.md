# CampBoardGameHost — Next Development Handoff

> **唯一当前目标（2026-10-15）：把 ARCH-CLEANUP-1 的统一 [Draft PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) 完成独立验收，再决定合并 `main`，之后才恢复 P2。** 开发机仓库别名 `clocktower`；活动分支 `cleanup/arch-cleanup-1-20261015`，PR 现以 `main` 为 base、mergeable/clean，尚未合并。#304/#306/#307/#308 全部代码已随 Git 祖先关系纳入 #315，原四个 Draft 现均 CLOSED UNMERGED；#310–#314 同样 CLOSED UNMERGED。只剩 #315 一个开放 PR。不要继续按老交接的五层堆叠执行合并，也不要为旧事件式 candidateId 合同写功能。

## 已完成与保留证据

- ARCH-CLEANUP-1 七批源码清理：去掉从未被写入的重复历史档案/Session 空字段、旧评分与审阅、闲置 role-specific whole-bundle 算法、FN-BUNDLE-3 耗时实验、旧 rollout 和未消费的 UI 文案推断；移除仅供测试的整包 wrapper，但相关测试改用真正精确评估器。每批提交与保留边界详见 [审计文档](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)。
- 最后源码提交 `f4c9468643e7` 对应 GitHub CI #4194：Android tests SUCCESS、Real Clingo SUCCESS、R2 #3810 SUCCESS，ASP 检查按分类 SKIPPED。仅文档 HEAD 的 CI/R2 通过不代表 Android 重新执行。
- P1 实际现存功能：确定阵容即尝试 `InitialJointOpeningDirectV2` 联合首夜计划；Host 合法性检查并确认 Drunk/红鲱鱼/掩饰身份与可选信息；在确认采纳后使用 `store:true` / `previous_response_id` 保存同局上下文。生产现仍使用 `StorytellerStrategicContinuationV2.remainingChoices`；未实现替代它的 P2 语义直出 JIT。玩家熟练度默认 BEGINNER。
- 已审核独立 1E 实验分支：`StorytellerJitDirectResultV2` 的直接语义 JSON 设计值得 P2 单独参考；其余增量代码仍依赖已退役的 #311 决策档案，**不能整支合并**。关闭的 PR 和远端分支均保留供追溯。

## 本轮收口的必做工作

1. **先校验真正 live SHA**：GitHub `main`、#315 最新 base/head、所有检查、未解决审阅、实际差异和合并状态；开发机 Mini MCP `git_state clocktower` 需无非预期改动。不得使用旧 `experiment/ai-conversation-1-probe` 当活动分支。
2. **最小独立自动化证据**：最终实际代码 HEAD 必须通过 Android CI、真实 Clingo/规则路径按分类执行、R2；关注 Host 合法/首夜联合/手动操作/因果账本/Recovery/Provider 测试。文档-only CI 的 Android SKIPPED 不能替代最近代码 HEAD 实际执行的证据。没有真实 API 预算批准就不调用付费模型。
3. **真实 Android Studio/ADB 验收待用户设备**：从 8 人 TB 局配置开始：MANUAL 完整身份展示与首夜信息；AI_ASSISTED/AI_AUTOMATIC 首夜联合建议和 Host 接受、红鲱鱼不再重复选择、候选对信息和恶魔掩饰身份；Chef 显示信息是否退出（FIELD-1）；首夜计划 schema/Host verdict 是否报 `whole-game AI plan failed strategy contract validation`（FIELD-2）；人工覆盖、下一夜、确认提名/逐人投票全量记录、恢复同版本游戏。记录 ADB 崩溃堆栈、Host trace 和实际请求/返回；不得凭 CI 或策略示例擅自声明完成。
4. **PR #315 是否 Ready / merge**：现为合并候选但仍 Draft；上述风险验收或用户明确接受剩余风险前，不自动合并到 `main`。完成验收后再次检查最新 base/head 与 CI/R2，独立审阅真实 diff，方可转 Ready、合并并关闭 ARCH-CLEANUP-1。不要重新打开已关闭的 #304–#314。
5. **治理与文件**：AGENTS evidence-first，按风险用既有验证/日志/真机而非机械 test-first；不制造旧版本兼容，不引入第二套可写 GameState。异常关闭 Recovery 与 Game Archive 不强行合并；EvidenceLab/TBGS 语义映射保持。

## 待恢复的 P2 准确产品合同（当前禁止开始新生产实现）

首夜整局联合规划只有一次；后续只在有必要时让资深 AI 考虑全局并返回**当前唯一一条语义信息**，数字/真假/角色/Pair 等由模型直接给出，而**不是让模型选择 Host 枚举的 candidateId**。Host 自主校验结果、登记多解、时序与合法性。真实事实包括投毒、死亡、角色变化、信息公布、所有确认提名和逐人投票、手工覆盖及玩家声明；用 `ActionFactTimeline` / `EpistemicObservationLog` / `StorytellerCausalDecisionJournalV1`，不能再使用已删除的 `DecisionHistoryArchive`。同局确认过的 Responses 锚点、合法缓存和可错战略记忆均要安全延续；没有完整增量证据时重送事实快照。已公布结果不可改写，未采纳的模型响应不能推进锚点。先实现这一合同，再退役活跃的旧 `remainingChoices`，不能为了清理立刻把它删掉。

## 来源与历史

阅读 [roadmap](CURRENT_DEVELOPMENT_ROADMAP.md)、[ARCH-CLEANUP-1 审计](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)、[TBGS/UGSM 时间点审计](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md)、[连续对话长期设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)、[AGENTS.md](../AGENTS.md) 与 [TESTING_STRATEGY.md](TESTING_STRATEGY.md)。先前长交接只用于历史证据，见 [归档快照](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md)，其中“下一个对话立即启动 P2”、旧 `candidateId` 模型字段和旧 Draft 堆叠合并均**不再有效**。


