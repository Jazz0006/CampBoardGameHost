# 重构前 PR 与分支清理建议

2026-10-17，Australia/Sydney。只读审计与建议，未执行合并、关闭 PR、删除分支或清理 worktree。

## 当前真实状态

- GitHub main 与本 worktree HEAD：`770ed32f446d03cf81f226901afa78cbdcf287bf`。本 worktree 为 detached HEAD，上一轮六份文档改动尚未提交。
- 唯一开放 PR：[#315](https://github.com/Jazz0006/CampBoardGameHost/pull/315)，Draft，head `06dbb95401708dd83090f5f8d88b1c632f0ea4f5`，base main。GitHub 当前 mergeable=false / dirty。
- PR 累计 163 个独有提交、301 个改动文件、+12,391 / -28,631 行。它包含 main 没有的 AI 接入、Session 收敛、旧推荐/回放退役及 Recovery 瘦身，不能按“旧存档 PR”全部丢弃。
- main 一侧有 6 个独有提交，但从共同祖先到 main 的实际内容变化仅 AGENTS.md 与 TESTING_STRATEGY.md。只读 legacy merge-tree 预览报告 AGENTS.md 冲突；这不是一次实际合并或完整语义验收。
- PR 描述仍停在 A2 in progress、A3 未开始，已落后于分支代码和路线。分支最新记录已有 A2 收尾、多个 A3 消费者迁移和 A5 部分删除。
- 当前远端 34 分支；本地 22 分支、3 个 worktree。其余两个 worktree 状态干净；本 worktree 有上述文档修改。未发现 stash。
- #315 最新文档 HEAD 的 [CI #4283](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39005683853) gate 成功，但 Android/ASP/Clingo 跳过。前一提交 `d3d58ea6` 的 [CI #4282](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39005403923) 五个 job 均成功；R2 #3865 成功属于 `64fd5983`，不是最新 HEAD。PR reviews 列表为空；没有据此认定完整独立评审通过。

## 对上一轮审计的修正

上一轮的对象和尺寸只适用于 main，不代表最新开发状态。#315 已删 SDE 重复恢复出口及部分重复字段/校验，已将若干 PlayerCard 和夜间规则消费者迁向 Session。应继承这些成果，再审计剩余删除范围，不能重复施工。

| 文件 | main 字节 / 行 | #315 字节 / 行 |
| --- | --- | --- |
| App | 271,975 / 4,768 | 280,654 / 4,846 |
| Host | 175,951 / 2,966 | 192,424 / 3,225 |

全局净删不代表核心大文件已经缩小。后续必须同时检查剩余责任与尺寸。

## 推荐顺序

1. **冻结 #315 范围并保留基线。** 不再向它加入新的大规模删除。执行清理时先为 main 与 #315 创建可验证的基线 tag（或 Git bundle），保存未提交文档到命名分支。保存原始证据不等于继续维护旧架构。
2. **将 #315 收束为已有工作的检查点。** 重写过时 PR 描述，列出实际已完成、未完成和已改变的恢复目标；保留有效的 Session 收敛，明确不承诺原 A1–A6 全部完成。不为满足旧路线补做已被放弃的恢复功能。
3. **将 main 合入 #315 的工作分支。** 采用普通 merge，不 rebase、不 force-push。合并 AGENTS 时保留 evidence-first，并采用本轮“先删除、允许暂缺”的明确覆盖；不能直接 ours/theirs 丢一边规范。此操作本轮未执行。
4. **评审组合代码并验证，再决定是否合入 main。** 对 Session 权威、已发信息不可重复修改、关键手动流程及已删测试的剩余覆盖做差异审查；运行最终组合 HEAD 的适用 full CI/R2，做必要的 Android 手动流程冒烟。无需付费模型调用，不能把旧 CI 或文档 gate 当成新代码已验收。这里合并的是现有实现检查点，不代表 AI 全自动、恢复或原路线全部产品验收。若发现较大未解决问题，保持 Draft，转用第 7 步，不能为整理分支强行合并。
5. **清理枝条与 worktree。** 已合并 PR 且当前分支 head 与记录 head 相同的 10 条远端分支是优先删除候选；删除前再查 head 未移动。4 条已被 #315 祖先历史包含的旧链分支在 #315 有持久引用后也可删除。其他未合并分支先保留唯一提交到归档 tag/bundle，核查无需继续开发后删除活动分支；禁止直接批量 `-D`。本地远端跟踪引用过期，先 fetch --prune 再分类，不能仅凭旧 upstream 状态删除。
6. **新重构从验收后的 main 开短分支。** 建议依次 `codex/remove-independent-recovery`、`codex/unify-game-record`、`codex/remove-state-mirrors`。每次完成一项可编译、缺口明确的删除后合并，再开始下一项；避免再堆叠一个跨十余阶段的长期巨型 PR。删除后再独立审计 App/Host，按真实责任拆分。
7. **备选：#315 无法低成本成为可接受基线时。** 从它的已保存 head 新建重构分支并合入 main，继续必要删除，建立一个明确替代 #315 的 Draft PR；新 PR 和基线引用均存在后再关闭 #315。不要从旧 main 重做 163 个提交，也不默认拆成大量 cherry-pick。该路径会继续承载大的差异，因此只作备选。

## worktree 处理建议

- `/Users/hlin5616/Jazz/repo/CampBoardGameHost`：主 checkout，干净，位于 #315 分支；保留作检查点收尾。
- `/Users/hlin5616/.codex/worktrees/35d2/CampBoardGameHost`：本会话，detached main，有六份未提交文档；先保存，再按 #315 现状修正文档，不能把上一轮 main-only handoff 整份覆盖最新开发交接。
- `/Users/hlin5616/.codex/worktrees/6a41/CampBoardGameHost`：干净，旧 registration-interaction 分支；对应 [#246](https://github.com/Jazz0006/CampBoardGameHost/pull/246) 已合并且 head 相同，是归档 worktree 和删除本地分支的候选。
- 本会话 artifacts 列表为空；未来归档旧托管 worktree 时需在其所属会话核对归属，用可恢复归档流程处理，不能假定可由本会话直接删除目录。

## 远端分支逐项清单

“已合并”依据 GitHub PR 记录和相同 head；squash merge 的原提交可能不在 main 祖先链中。“#315 已包含”依据对 GitHub 返回 SHA 的本地祖先校验；不等于已进入 main。其余为待归档/审查，未声称内容已吸收。

| 分支 | HEAD | 证据 / 建议 |
| --- | --- | --- |
| `audit/tbgs-ugsm-decision-time-completeness-20261015` | `99150eb6` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `c5e-librarian-production-cutover` | `f85535b2` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `c5e-librarian-production-cutover-r2` | `1fd6fccd` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `cleanup/arch-cleanup-1-20261015` | `06dbb954` | #315，保留并收束检查点 |
| `codex/app-host-decomposition-audit` | `a45eea8f` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `codex/d6-ownership-plan` | `f6c6d944` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `codex/drunk-late-binding-sequencing-audit` | `5abac479` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `codex/execution-restore-crash-repro` | `b9a0565f` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `codex/ps4-4-validation-red-final` | `d0ca5d38` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `codex/sde-rhe-validation-20260927` | `bf663633` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `docs/beginner-storyteller-mode-policy` | `ce656369` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `docs/dev-exec1-remote-experiment-handoff` | `4e218fb9` | #290 已合并；复核 head 后可删活动分支 |
| `docs/gsp-production-autopilot-priority-20261009` | `ed8d51bc` | #291 已合并；复核 head 后可删活动分支 |
| `docs/lre-p-inv1a-closure-20261004` | `86591469` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `docs/prod-global-3-expert-storyteller-reorientation-20261012` | `e47a2c1b` | #298 已合并；复核 head 后可删活动分支 |
| `experiment/ai-conversation-1-probe-20261015` | `486f2734` | #315 已包含；保留基线引用后可删 |
| `experiment/personal-openai-direct-20261011` | `c1991478` | #293 已合并；复核 head 后可删活动分支 |
| `feature/ai-event-jit-1-20261015` | `d21854e8` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `feature/ai-event-jit-1b-causal-delta-20261015` | `7bebf7ee` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `feature/ai-event-jit-1c-single-wire-20261015` | `6c69ae25` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `feature/ai-event-jit-1d-relevant-facts-20261015` | `8b115ac9` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `feature/ai-event-jit-1e-real-history-audit-20261015` | `0c97c809` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `feature/prod-global-3c3d-single-precommit-opening-20261012` | `df058ee5` | #315 已包含；保留基线引用后可删 |
| `feature/prod-global-3e3-whole-residual-20261012` | `df0f701d` | #315 已包含；保留基线引用后可删 |
| `feature/prod-global-3e-generic-host-checkpoint-20261012` | `d0e704c9` | #315 已包含；保留基线引用后可删 |
| `governance/evidence-first-20261015` | `05974bc4` | #309 已合并；复核 head 后可删活动分支 |
| `gsp-2b3b-player-context-edit-surface` | `75e1943d` | #232 明示保留参考；先审计/归档，不直接删除 |
| `legacy-experience-retirement-20261011` | `3263b91e` | #295 已合并；复核 head 后可删活动分支 |
| `main` | `770ed32f` | 保留主线 |
| `prod-1-live-drunk-ai-assisted` | `b21a2cdb` | #292 已合并；复核 head 后可删活动分支 |
| `prod-global-3b1-precommit-contract-20261012` | `af87577c` | #297 已合并；复核 head 后可删活动分支 |
| `sde-2d4-d4e-validation-20260919` | `bf4f14cb` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `sde-2d4-player-count-performance-audit` | `f9b99779` | 未证明被 main/#315 包含；先归档唯一提交，再决定删除 |
| `tb-setup-simplification-20261011` | `691bb5fa` | #294 已合并；复核 head 后可删活动分支 |

本轮未执行任何远端或 Git 状态变更。清理建议不构成当前 #315 可立即合并的结论。

