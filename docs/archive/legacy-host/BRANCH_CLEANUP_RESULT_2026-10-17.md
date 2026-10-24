# 重构前分支清理结果

日期：2026-10-17（Australia/Sydney）。用户已授权按审计建议执行。

## 已完成

- 清理前 main `770ed32f446d03cf81f226901afa78cbdcf287bf`、PR #315 `06dbb95401708dd83090f5f8d88b1c632f0ea4f5` 均已保存。
- 所有 34 条原远端分支建立 `archive/pre-refactor-20261017/remote/<原分支名>` 标签并推回同一仓库；GitHub Connector 核验每个标签与原 SHA 完全一致后，以精确 SHA 租约原子删除 32 条旧活动分支。
- 远端保留 `main` 和 `cleanup/arch-cleanup-1-20261015`（#315）。未重新打开旧 PR。
- 本地原有分支及本会话准备分支都已建立 `archive/pre-refactor-20261017/local/<原分支名>` 标签，删除 20 条旧本地分支。保留 main、#315 和 `codex/pre-refactor-preparation`。
- 已移除无未提交修改的旧 `6a41` worktree（#246 已合并），忽略文件只有 Gradle/build 缓存。主 checkout 和本会话 worktree 保留。
- 完整本地 bundle 已验证且存于主仓库 `.git/archives/pre-refactor-20261017/CampBoardGameHost-pre-refactor-20261017.bundle`；同目录 `camp-branch-cleanup-20261017.json` 保存全部原分支、完整 SHA 和归档标签映射。本地独有提交未作为归档标签上传。
- 本会话七份原文档在 `4baed837` 保存；随后以普通 merge 整合 #315。整合相对 #315 原 head 仅改变文档，`app/`、`tools/`、`.github/` 和构建配置没有新增变化；main 的 evidence-first 与 #315 的目的优先/单一事实所有者原则均保留。
- 收敛当前路线和交接，保留 #315 已完成工作的证据；旧“完善独立 Recovery”的执行要求由本轮减法目标覆盖。

## 恢复方式

远端归档标签已经存在于 origin，本地可直接从标签开恢复分支。例如：

```bash
git switch -c codex/recover-old-jit archive/pre-refactor-20261017/remote/feature/ai-event-jit-1-20261015
```

本地独有旧分支使用 `archive/pre-refactor-20261017/local/<原分支名>`。若需恢复整个清理前仓库，可从上述 bundle 克隆至另一个目录。不要为了查阅历史重新打开所有活动分支。

## PR 与重构边界

#315 保持 Draft。此检查点仅准备开发基线，不代表其 301 文件的既有生产变更已完成新的独立语义评审或 Android 真机验收。合并 main 必须满足适用 CI/R2 与流程验证，不能用文档 gate 代替。

新的生产删除尚未开始；继续开发应继承整合后的 #315 成果，不能回到旧 main 重复删除。优先完成基线检查，再以短分支删除独立 Recovery、统一记录和清理重复状态。当前状态、检查运行及 PR head 以 GitHub 实时结果为准。

## 审批处理

最初尝试推送全部本地/远端归档标签被自动审批拒绝，原因是可能外发本地未上传提交。已改用远端原有 SHA 只在原仓库归档、本地独有内容仅保留本地 bundle/tag 的方式，并成功完成；没有绕过外发限制。
