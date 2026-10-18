# 当前唯一 NEXT：P1 旧规则检查系统整链清除（2026-10-18）

**新对话必须实际实施代码、提交并验证 CI/R2**，不重新规划；必须从真实仓库当前 HEAD 核查开始，不继续旧 M1c 逐角色推荐功能。

## 权威阅读顺序

AGENTS.md；docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md；docs/CURRENT_DEVELOPMENT_ROADMAP.md；docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md；docs/TESTING_STRATEGY.md；docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv。

## 当前远端/开发机基线（先重新核对）

- 仓库：Jazz0006/CampBoardGameHost。开发机 Mini MCP 别名 clocktower。
- PR #330→#334 是依赖堆叠 Draft；最新待开发的分支为 codex/minimal-host-full-scan-20261018，原代码/文档 HEAD 为 55508645a11dc9277d0af7260bca4968c211e31c，本次文档更新会继续推进 HEAD，**必须再次查 GitHub**。
- 先前代码 commit 2bab8d5469a9e80cc6d193e6746e8142f65a56f0 的 CI #4325 Android tests、R2 #3905 SUCCESS；之后 docs-only #4326/R2 #3906 SUCCESS，不等于未来代码修改已过测试。
- 开发机先前 clean 分支 codex/minimal-host-reset、HEAD c9802fe61d342af3d6dbafe1d6275209817e7572，并非远端 #334。不要在未同步的本地目录直接改错分支；可 GitHub Connector 基于 #334 当前 HEAD 开堆叠分支，或用准确的 Mini MCP Git 工作流创建隔离本地分支。
- 不自动合并 Draft / 强推 / 删除远端分支。真实付费模型调用没有授权。

## P1 第一批具体工作

1. 以完整 Git tree 而非现有局部流程为入口，遍历 clocktower/rules 下全部旧检查模块和外部生产消费者。重点 App/HostScreen/NightStepUi、Setup、Session、Flow、Recommendation、Provider 法域、角色候选 UI、R2 源码守卫及旧规则测试。
2. 旧规则检查**全部要删除**，不是重构或迁移。先解开消费者，再整链净删。若 Host/UI 要记录某个实际动作或展示值，保留其结构性录入/确认/显示/事件，不以规则合法候选阻拦。移除基于 Spy/Recluse 的见证推导、固定真值、自动挑合法结果、规则阻断和影子推演的辅助代码。
3. 此阶段优先保护基础手动游戏：阵容配置、魔典、隐私展示、玩家行动、投票/死亡、实际信息记录、夜/日阶段推进。旧自动裁定/旧 AI 可以明确短期不可用，但不能因删除旧功能阻塞正常事实写入。
4. 删除专用旧规则/候选测试和只为旧源码形状存在的 CI 守卫。核心事件顺序、唯一权威、去重、原子写入、座位/身份、私密收件人、过期响应以及必须的交互安全测试应保留。采用 evidence-first，无机械 test-first。
5. 每批逐项报告：已清除的生产消费者与旧源码/测试、净删除量、遗留绑定、当前超 50,000 B 的全部源文件、GitHub 真实 CI/R2。CI 失败查运行日志并修复，不为旧规则测试恢复删除路径。遇到不确定是否属于事实核心的字段，形成简短的用户审核包而不是重新设计整个路线。

## P2→P5 已批准顺序

P2 删除旧 AI 候选/评分、A4/B4/ZDD/世界推演；P3 一份 Host 状态与关键事件，最小安全续局；P4 精简 UI/所有源码 <=50,000 bytes；P5 全树再审、跨项目数据合同，然后重新规划独立 Rules 与 AI 模块。新规则交互（pair、厨师、共情者合法按钮）等清理完成后再做。

本轮仍未确认是否整包删除仓库旧 web/ 客户端及非 Trouble Brewing 剧本；这些是需要所有者审核的边界，不能因此拖延已明确授权的 Rules 删除。
