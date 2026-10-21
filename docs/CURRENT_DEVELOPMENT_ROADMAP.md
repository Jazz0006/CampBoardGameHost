# 当前路线 — 全仓清理进行中（2026-10-21）

代码 `130fe215592f7b4725917beb302323a75bbf46b2` 已推送 [Draft #336](https://github.com/Jazz0006/CampBoardGameHost/pull/336)，base 为已合并 main `40050941`。本地 fast **617** / full **649**（145 suites、38 UI，零失败/错误/跳过）、APK 和原 R2 脚本通过；[R2 #4082](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39579927285) **SUCCESS**，[完整 Android CI #4507](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39579927163) **SUCCESS**（full/APK 上传、ASP、真实 Clingo 和总门禁已独立核对）。

当前执行环境为 Codex 本地 Windows，直接读写文件，不使用 MiniMCP。#335、#334 已由远端其他操作合并；本批从最新 main 建新分支，没有重开旧 PR、合并或重写历史。

## 第一阶段：最小完整流程已关闭

保持 `e73e4807` / CI #4503 / R2 #4079 的两剧本真实完整 App 基线：首页/玩家/座位/剧本、生产发牌、首夜、真实提名/投票/同页日结、后夜死亡、私有文件重建、终局揭晓、恢复后原班再开，以及中断发牌。详见 [既有验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。本批原有 38 项界面测试全部保留通过；不重新开放第一阶段，也不冒称设备/实际杀进程/付费模型验收。

## 第二阶段：本批收敛与后续主任务

- App 从 **116000B → 48729B**，Night Host 从 **63787B → 42359B**；Session 保持 **47578B**。维护源码扫描无 >50000B，R2 增加跨生产/测试的硬门禁。
- App 中原有开局准备/Drunk 确认与 handoff、实时 AI 请求及建议记忆、对局草稿和 Session 回调、夜间只读角色文案分归明确职责。复用 main 的 `StorytellerAiSettings`，保留加载保护与实际 stale/finally；没有通用 Context/Manager、新 Session、永久 pending 或新恢复引擎。
- 删除已无生产用途的角色揭示 Anchor/两个 typed-result 包装、旧唤醒集合、evil helper、AI mode helper及两份独占旧测试。实际直接角色输入、D2 预览/最终 Next、Session 夜序、当前 AI 和安全测试保留。
- 对 main 的本批实量：生产 **+1921/-1756，净 +165 行/+3328B**；测试 **+1/-80，净 -79 行/-3271B**。生产 **219** / 测试 **149** 文件。拆分的签名和职责边界计入新增，不能称为大规模净删或全树语义验收。精确链见 [台账顶部](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。

**下一步是未截断全树按完整职责的语义审计和大规模删除**：生产源码、资源、测试和 CI 逐项核对最小可玩必要性、实际 producer/consumer 和唯一事实所有者，删除整项多余功能而非只查未使用符号。当前只证明本批责任迁移、几个孤立出口和文件大小，不代表全树零遗留。保留一个 Session、两个内置剧本、现有 AI 全链与模式范围、双跨局防重复、确定机械、实际信息/收件人、顺序/幂等/原子性。

## 第三阶段与授权边界

完成清理后统一优化 UI/流程细节，再做必要视觉/设备验收。保持新 PR 为 Draft；不自行 merge、retarget、rebase、force-push、删分支或调用付费 API。无新的所有者决策阻塞。交接见 [下一步](NEXT_DEVELOPMENT_HANDOFF.md)。
