# 当前开发交接

> **2026-11-13：** BMR UI 规则条件已收归 Session（分支 `claude/modest-einstein-os4fez`，详见[职责审查](SESSION_UI_AUTHORITY_AUDIT_2026-11-02.md) 的 2026-11-13 跟进）。所有者裁定：醉酒能力不产生实际效果，刺客选中莽夫同样击杀失败。本地 testFull 662 项/0 失败、APK 通过；以推送提交的远端 CI 为准，尚未合并。仍待：拆分超限的 `ClocktowerD2UiTest.kt`、真机检查、TB/NGJ AI 不可用接管。

> **合并后基线（2026-11-02）：** PR #372 已合并，main `d10444b7d2f09a924f06676229c3edab46311605`；本地 main 已快进同步。PR head `7fe2b345` 的完整 CI #4642/R2 #4179、合并后 [main 完整 CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41444310189) 及 [Field Test APK 发布](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41444310152) 均通过。Android 日志确实运行 `testFull + assembleDebug`，现场安装包来源为该合并提交。下一步继续下述真机反馈和职责/尺寸遗留，不再尝试合并已关闭的 PR #372；开工仍读取 live main，不将本 SHA 作为永久基线。

> **2026-11-02 最新交接：** BMR MANUAL 及首批真机反馈修正统一交付于 [PR #372](https://github.com/Jazz0006/CampBoardGameHost/pull/372)，所有者已授权合并；先核对实时 PR/main 和精确 SHA 检查，不继续按旧 Draft 状态工作。爪牙/疯子/恶魔/教父/侍女页面、水手单选裁定、侍臣“选角色即使用、清除即跳过”和全屏行高修正均已实施；本地完整 testFull（661 项，0 失败）和 APK 构建通过。原子存档、最后展示才确认、Session 独占事实写入未改；Session JSON 格式不变。具体见[交付进展](BMR_MANUAL_GAME_PROGRESS_2026-11-02.md)和[职责审查](SESSION_UI_AUTHORITY_AUDIT_2026-11-02.md)。
>
> **保留的下一步：** 真机继续核对页面/交接；将 BMR 日夜及状态控件中的规则条件组合收归 Session 事实接口；按交互职责拆分 57,536 B 的 `ClocktowerD2UiTest.kt`，不增加低价值测试。生产源码未超限（Session 47,116 B，BMR 夜间 UI 36,333 B），但须防止后续集中膨胀。不要把这些审查项标记已修复，也不要误报已完成真机/物理强杀/付费 API 验收。TB/NGJ AI 不可用及恢复后的人工接管完善仍独立未完成。

> **2026-10-26 更新：** 重构 Batch 1–5、周末真机修复，以及 2026-10-26 审查的修复（#369、#370 与审查小项修复批）已合并。详见 [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) 与 AGENTS.md 当前权威一节。

1. 先核对 live main、工作区、开放 PR 与精确 SHA 的 CI/R2；Android tests 日志须确实运行 `./gradlew :app:testFull :app:assembleDebug`（PR 默认只跑 `testFast`，代码提交需带 `[full-ci]`），合并后再看 main push 运行。
2. 夜间 Host 事实由 `ClocktowerNightFacts`（Session 快照一次读取）与 `ClocktowerNightDraftSubjects`（草稿座位路由）统一提供；`ClocktowerNightPresentation` 只把座位映射为卡片与文字。新增夜间事实放在这里，不在 UI 中重复计算。
3. 规则改动须在 PR 中说明改了什么、为什么；同一条规则只在 Session 侧实现一次，AI 解析、UI 草稿与确认共用（如红鲱鱼规则）。存档/metadata 格式改动须说明旧存档如何恢复。
4. 下一项功能：AI 不可用或恢复后仍能完成本局（统一夜间手动接管、人工触发的可选随机、恢复后过期响应校验）。
5. Session 唯一写事实；无持久 UI pending、第二游戏对象或独立恢复系统；D2 预览不入账，Next 记录最后一次展示。保留两套内置剧本、三种模式、两类跨局防重复、隐私与原子保存。
6. 测试按 2026-10-25 测试政策：只为有明确缺口的重要失败加小型行为回归。真机与付费模型未验收；付费调用需授权。

BMR 新功能线现已在 PR #372 接通完整 MANUAL 主持；当前入口/25 角色审计及验证见[本次交付](BMR_MANUAL_GAME_PROGRESS_2026-11-02.md)。[A 批实施细化](BMR_A_IMPLEMENTATION_PLAN_2026-11-02.md)保留开局阶段的历史证据，原首夜只读预览已删除。BMR 不调用 AI，也不以此替代上文原剧本的 AI 续局任务。

[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) · [旧累计交接](archive/legacy-host/PRE_SHARED_GUIDE_NEXT_DEVELOPMENT_HANDOFF.md) · [归档清理清单](DOCS_CLEANUP_2026-10-24.md)。

2026-11-02 用户再次强调：存档只保证异常退出后继续当前局。复用既有活动局保存/加载；未确认步骤可重做，不恢复 UI 细节，不扩展历史档案、回放、初始快照或通用恢复框架。BMR 实施遵守此限制。

最新 BMR 进展：[完整人工主持交付](BMR_MANUAL_GAME_PROGRESS_2026-11-02.md)；[B 生死基础](BMR_B_LIFE_PROGRESS_2026-11-02.md)为早期检查点。首夜/后夜、白天提名投票处决、复活/登记死亡/跨夜醉毒、特殊结算和人工终局均已接通。三项普卡裁定已实现：自毒使旧目标解毒不死且后续选择无效；连续同目标只结算旧施毒、保留新毒；行动后同夜侍臣死亡使普卡立即恢复旧毒并补结算，无重复选择。无待用户裁定问题。

本地完整 testFull/APK 与后续定向回归已通过；发布时仍须核对最新完整 SHA 的远端 testFull/APK 和 R2。下一步是所有者本地/真机试玩及实际问题修复；跨来源醉酒的暂停/恢复和特殊胜负按第一阶段范围由说书人确认，不扩建循环求解器。未做真机、物理强杀或付费 API，不把这些记为已验收。推送/PR 发布已有项目级授权，合并仍遵守原边界。
