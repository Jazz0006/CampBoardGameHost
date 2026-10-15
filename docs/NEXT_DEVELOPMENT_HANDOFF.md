# CampBoardGameHost — Next Development Handoff

> **唯一当前目标（2026-10-15）：把 ARCH-CLEANUP-1 的统一 [Draft PR #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315) 完成独立验收，再决定合并 `main`，之后才恢复 P2。** 开发机仓库别名 `clocktower`；活动分支 `cleanup/arch-cleanup-1-20261015`，PR 现以 `main` 为 base、mergeable/clean，尚未合并。#304/#306/#307/#308 全部代码已随 Git 祖先关系纳入 #315，原四个 Draft 现均 CLOSED UNMERGED；#310–#314 同样 CLOSED UNMERGED。只剩 #315 一个开放 PR。不要继续按老交接的五层堆叠执行合并，也不要为旧事件式 candidateId 合同写功能。

> 2026-10-16 最终复审：起点 HEAD `50a1c05f` CI #4206 / R2 #3822 SUCCESS（ASP/Real Clingo SKIPPED）。新清理删除首夜迁移 Shadow 候选对比、旧展示回退、私有 parity 统计以及 UI 保存的冗余观察值；续接 `causalHistory` 现在序列化类型化 JSON 事实/观察，而不是 Kotlin `toString()`。A=PARTIAL（仍有 Compose PlayerCard 独立可写状态、部分确定事实记录不全、任意时间点重放缺口），B=YES 源码合同，C=YES 已清理的历史策略路径；本轮源码精确 HEAD/CI 尚需重新验证。**仍保留 Draft #315，ARCH-CLEANUP-1 不关闭、不合并 main。**

> 2026-10-16 A 专项：根 UI 独立 `clocktowerCausalJournals` 已删除；同局因果日志归 Session。确认的 Red Herring / Demon bluffs 及其按全局因果游标排序的类型化提交，由 Session 原子承接且按源调用写入；Recovery、模型延续和只读 UI 投影同源。已确认身份/死亡写入卡片的回调增加 Session 前置事实校验。**A 尚 PARTIAL，不能宣布闭环**：继续收敛完整 UI 写入口、夜间能力/公开信息的记录源、Recovery 跨状态一致性、任意时点重建。当前清理 PR #315 Draft，不合并 main；最新代码须精确 CI/R2 验收。

## 已完成与保留证据

- ARCH-CLEANUP-1 多批源码清理：删除重复历史档案/Session 空字段、整个旧 SDE 评分/一致性偏好/Shadow 推荐实验族（仅保留真实 Recovery replay 两个文件）、角色独立 whole-bundle、FN-BUNDLE-3 耗时实验、旧 rollout、无消费的 UI 文案推断和旧 V1 模型合法域外发序列化；移除仅供测试的整包 wrapper，但相关测试改用真正精确评估器。每批提交与保留边界详见 [审计文档](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)。
- 已确认源码 HEAD `0e563911` 的 Android CI #4199 / R2 #3815 SUCCESS，以及 V3 语义输出 HEAD `63b5d111` 的 Android CI #4202 / R2 #3818 SUCCESS（Real Clingo、ASP 按 CI 分类 SKIPPED）。Session-seat 调整 HEAD `ef3438c` Android CI #4203 仍待确认、R2 #3819 GREEN；最新去首夜条件合法分支菜单 HEAD `4cb657e` 的 Android CI #4205 和 R2 #3821 待独立确认；不得把旧 GREEN 当新 HEAD 的检查。
- P1 实际现存功能：确定阵容即尝试 `InitialJointOpeningDirectV2` 联合首夜计划，外发仅阵容、当前逐项需求、尚未发生事件与玩家经验，**没有候选 Drunk 分支清单或其他合法备选菜单**；Host 合法性检查并确认 Drunk/红鲱鱼/掩饰身份与可选信息；在确认采纳后使用 `store:true` / `previous_response_id` 保存同局上下文。**已实现 V3 外发当前唯一 `currentDecision` / 直接 `currentResult.semantic`**，请求不包含 `legalCandidates` / `candidateId`；Host 用本地完整合法域验证后，将唯一结果转换成仅供旧 UI 采纳的私有 `remainingChoices` 键值。P2 全量因果增量、有效缓存和真实设备 JIT 验收仍未完成；模型还没有进行这份 V3 付费实验。玩家熟练度默认 BEGINNER。
- 已审核独立 1E 实验分支：`StorytellerJitDirectResultV2` 的直接语义 JSON 设计值得 P2 单独参考；其余增量代码仍依赖已退役的 #311 决策档案，**不能整支合并**。关闭的 PR 和远端分支均保留供追溯。

## 本轮收口的必做工作

1. **先校验真正 live SHA**：GitHub `main`、#315 最新 base/head、所有检查、未解决审阅、实际差异和合并状态；开发机 Mini MCP `git_state clocktower` 需无非预期改动。不得使用旧 `experiment/ai-conversation-1-probe` 当活动分支。
2. **最小独立自动化证据**：最终实际代码 HEAD 必须通过 Android CI、真实 Clingo/规则路径按分类执行、R2；关注 Host 合法/首夜联合/手动操作/因果账本/Recovery/Provider 测试。文档-only CI 的 Android SKIPPED 不能替代最近代码 HEAD 实际执行的证据。没有真实 API 预算批准就不调用付费模型。
3. **真实 Android Studio/ADB 验收待用户设备**：从 8 人 TB 局配置开始：MANUAL 完整身份展示与首夜信息；AI_ASSISTED/AI_AUTOMATIC 首夜联合建议和 Host 接受、红鲱鱼不再重复选择、候选对信息和恶魔掩饰身份；Chef 显示信息是否退出（FIELD-1）；首夜计划 schema/Host verdict 是否报 `whole-game AI plan failed strategy contract validation`（FIELD-2）；人工覆盖、下一夜、确认提名/逐人投票全量记录、恢复同版本游戏。记录 ADB 崩溃堆栈、Host trace 和实际请求/返回；不得凭 CI 或策略示例擅自声明完成。
4. **PR #315 是否 Ready / merge**：现为合并候选但仍 Draft；上述风险验收或用户明确接受剩余风险前，不自动合并到 `main`。完成验收后再次检查最新 base/head 与 CI/R2，独立审阅真实 diff，方可转 Ready、合并并关闭 ARCH-CLEANUP-1。不要重新打开已关闭的 #304–#314。
5. **治理与文件**：AGENTS evidence-first，按风险用既有验证/日志/真机而非机械 test-first；不制造旧版本兼容，不引入第二套可写 GameState。异常关闭 Recovery 与 Game Archive 不强行合并；EvidenceLab/TBGS 语义映射保持。

## 待恢复的 P2 准确产品合同（当前禁止开始新生产实现）

首夜整局联合规划只有一次；后续只在有必要时让资深 AI 考虑全局并返回**当前唯一一条语义信息**，数字/真假/角色/Pair 等由模型直接给出，而**不是让模型选择 Host 枚举的 candidateId**。Host 自主校验结果、登记多解、时序与合法性。真实事实包括投毒、死亡、角色变化、信息公布、所有确认提名和逐人投票、手工覆盖及玩家声明；用 `ActionFactTimeline` / `EpistemicObservationLog` / `StorytellerCausalDecisionJournalV1`，不能再使用已删除的 `DecisionHistoryArchive`。同局确认过的 Responses 锚点、合法缓存和可错战略记忆均要安全延续；没有完整增量证据时重送事实快照。已公布结果不可改写，未采纳的模型响应不能推进锚点。其中当前单项语义输出 V3 已实施，旧模型外发整包被废止；剩余真实历史全量性、事件增量、缓存与恢复锚点需继续审计。Host 内部为现役采纳 UI 暂存的 `remainingChoices` 映射不是模型输出格式，不能按名称机械删掉。

## 来源与历史

阅读 [roadmap](CURRENT_DEVELOPMENT_ROADMAP.md)、[ARCH-CLEANUP-1 审计](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)、[TBGS/UGSM 时间点审计](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md)、[连续对话长期设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)、[AGENTS.md](../AGENTS.md) 与 [TESTING_STRATEGY.md](TESTING_STRATEGY.md)。先前长交接只用于历史证据，见 [归档快照](archive/checkpoints/NEXT_DEVELOPMENT_HANDOFF_PRE_CONTINUITY_SYNC_2026-10-15.md)，其中“下一个对话立即启动 P2”、旧 `candidateId` 模型字段和旧 Draft 堆叠合并均**不再有效**。


