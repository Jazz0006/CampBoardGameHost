# 当前唯一 NEXT：P1 继续整链清除旧规则检查系统（2026-10-18）

**新对话必须实际实施代码、提交并验证 CI/R2**，不重新规划；必须从真实仓库当前 HEAD 核查开始，不继续旧 M1c 逐角色推荐功能。

## 权威阅读顺序

AGENTS.md；docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md；docs/CURRENT_DEVELOPMENT_ROADMAP.md；docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md；docs/TESTING_STRATEGY.md；docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。

## 当前远端/开发机基线（先重新核对）

- 仓库：Jazz0006/CampBoardGameHost。开发机 Mini MCP 别名 clocktower。
- PR #330→#334 均为依赖堆叠 Draft。当前 P1 开发分支为 `codex/minimal-host-full-scan-20261018`，最新已提交的 P1 代码检查点为 `4b1267e8e7d3277577e44c01dd7004cb04da6fb5`；后续文档提交会继续推进 HEAD，开始新批次必须重新核对 GitHub。
- P1 B1 首次 Android CI #4330 测试 1301 项 / 1 项失败（旧登记键）；修复代码 `4b1267e8...` 的完整 Android CI #4331 与 R2 #3911 是本批对应独立检验（最终状态需要重查）。更早 CI #4325/R2 #3905 与 docs-only #4326/R2 #3906 仅属清理前的历史基线。
- 开发机已经 `git_fetch` 后经 Mini MCP `git_track_remote_branch` 从旧 `codex/minimal-host-reset` 切换并同步到正确 #334 分支，且 B1 两次代码提交均已推送。继续时必须再次检查 branch/HEAD/working tree，不能依赖本段历史记录。
- 不自动合并 Draft / 强推 / 删除远端分支。真实付费模型调用没有授权。

## P1 B1 已提交，不等于 P1 完成

- 当前开发分支：`codex/minimal-host-full-scan-20261018`，本批最新代码 HEAD：`4b1267e8e7d3277577e44c01dd7004cb04da6fb5`；Draft PR #334 与原 base 堆叠关系不变。
- B1 删除 `RegistrationInteractionRules.kt`、`TroubleBrewingRecurringPlayerInformationRouteDomain.kt`、`RegistrationInteractionRulesTest.kt`；删除厨师方桌数字→登记见证及座位高亮反推，移除配对方桌 Spy/Recluse 登记勾选推断；调整调用及专属旧测试。GitHub 比较证据（包含修复后相对 P1 初始 `c9204ade...`）：9 个文件变化，+32/-410 行，净删 378 行。
- B1 初次 `[full-ci]` #4330：主程序和测试源码均编译成功，1301 项单元测试中 1 个旧登记键断言失败。R2 #3910 成功。修复提交 `4b1267e8e7d3277577e44c01dd7004cb04da6fb5` 关闭了旧夜间信息 builder 的全部隐式登记键：不复活旧可靠性过滤规则，也不推断未确认的 Spy/Recluse 裁定。最新完整 Android CI #4331 **SUCCESS**（`:app:testFull :app:assembleDebug`）、R2 #3911 **SUCCESS**，ASP/Clingo 和 CI gate 同样成功；这是当前有效的完整代码验收。未进行真机或收费模型调用。
- 保留原 Session 事件 writer、直接数字展示/确认、私密信息与方桌真实身份提示；但旧规则算法仍有生产消费者，**不得宣称人工结果全面不受限制或 P1 已完成**。
- 已按 B1 **代码 HEAD** 从 GitHub 读取并实测 4 个超 50,000B 的源码：`CampBoardGameHostApp.kt` 198706B、`ClocktowerHostScreen.kt` 150032B、`ClocktowerNightStepUi.kt` 60237B、`web/app.js` 143503B。其他文件以完整仓库再扫描为准。

## P1 下一批具体工作

1. 以完整 Git tree 而非现有局部流程为入口，遍历 clocktower/rules 下全部旧检查模块和外部生产消费者。重点 App/HostScreen/NightStepUi、Setup、Session、Flow、Recommendation、Provider 法域、角色候选 UI、R2 源码守卫及旧规则测试。
2. 旧规则检查**全部要删除**，不是重构或迁移。先解开消费者，再整链净删。若 Host/UI 要记录某个实际动作或展示值，保留其结构性录入/确认/显示/事件，不以规则合法候选阻拦。移除基于 Spy/Recluse 的见证推导、固定真值、自动挑合法结果、规则阻断和影子推演的辅助代码。
3. 此阶段优先保护基础手动游戏：阵容配置、魔典、隐私展示、玩家行动、投票/死亡、实际信息记录、夜/日阶段推进。旧自动裁定/旧 AI 可以明确短期不可用，但不能因删除旧功能阻塞正常事实写入。
4. 删除专用旧规则/候选测试和只为旧源码形状存在的 CI 守卫。核心事件顺序、唯一权威、去重、原子写入、座位/身份、私密收件人、过期响应以及必须的交互安全测试应保留。采用 evidence-first，无机械 test-first。
5. 每批逐项报告：已清除的生产消费者与旧源码/测试、净删除量、遗留绑定、当前超 50,000 B 的全部源文件、GitHub 真实 CI/R2。CI 失败查运行日志并修复，不为旧规则测试恢复删除路径。遇到不确定是否属于事实核心的字段，形成简短的用户审核包而不是重新设计整个路线。

## P2→P5 已批准顺序

P2 删除旧 AI 候选/评分、A4/B4/ZDD/世界推演；P3 一份 Host 状态与关键事件，最小安全续局；P4 精简 UI/所有源码 <=50,000 bytes；P5 全树再审、跨项目数据合同，然后重新规划独立 Rules 与 AI 模块。新规则交互（pair、厨师、共情者合法按钮）等清理完成后再做。

本轮仍未确认是否整包删除仓库旧 web/ 客户端及非 Trouble Brewing 剧本；这些是需要所有者审核的边界，不能因此拖延已明确授权的 Rules 删除。
