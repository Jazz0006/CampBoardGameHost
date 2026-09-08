# CampBoardGameHost 单开发者 GitHub / Codex 工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`

## 1. 基本原则

本项目按单开发者模式运行。真正需要防范的是：

- 写错 branch；
- 使用过期 head/blob；
- 大文件被 connector 截断后仍做 whole-file replacement；
- Codex 为机械修改重新读取大量无关上下文；
- 本地修改测试通过但没有 push，导致 ChatGPT 无法从远端继续审计；
- unrelated stacked work 混入当前 PR。

所有修改仍遵守：

```text
live main/head recheck
-> tests-first（behavior change）
-> smallest change
-> exact diff audit
-> focused/full tests
-> CI + R2
-> final review
-> explicit user authorization before merge
```

## 2. 文件写入路径选择

### A. 小/中等文件：GitHub connector 直接修改

当 connector 能可靠得到目标 branch 当前文件的**完整内容**时，可使用 direct `update_file` / Git Data API。

必须：

- 从实际目标 branch live head 获取文件；
- 使用对应 blob SHA；
- 写前确认 head 未漂移；
- 写后立即 exact diff audit；
- 如果出现异常大 churn，停止，不靠编译通过掩盖错误。

### B. 大文件 + 内容截断：ChatGPT → Codex Luna 本地 patch

一旦出现 truncated / incomplete content，就停止 whole-file reconstruction。

正式默认流程见：

- `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`

摘要：

```text
ChatGPT 负责
  分析 / 定位 / tests-first / 生成最小 patch / 指定测试

Codex Luna 负责
  git apply --check
  git apply
  local diff audit
  run tests
  commit
  git push origin HEAD

ChatGPT 再负责
  fetch remote head/commit
  verify parent + exact diff
  CI / R2 / review / PR gate
```

**Luna 测试 GREEN 后必须 commit + push。** 只在本地修改而不 push，ChatGPT 无法继续基于 GitHub repo 做可靠审计。

### C. 大范围复杂重构

如果修改跨很多文件、patch 很大、需要执行模型理解完整上下文，则使用完整 Codex/local Git worktree，不把任务强行压成小 patch。

## 3. Permanent / temporary remote writer 的状态

2026-08-23 对 permanent `issue_comment` writer 做过实现和静态验证：Android / ASP / Real Clingo / R2 可 GREEN，但 pre-merge canary 不能真正触发，因为 `issue_comment` workflow 必须先存在于 default branch。

这意味着必须先将一个尚未端到端验证的 write-enabled workflow 部署到 `main`，才能验证它本身。

项目决定：

```text
remote trusted writer
  EXPLORED
  STATICALLY VALIDATED
  NOT END-TO-END VALIDATED
  NOT ADOPTED
```

因此：

- 不在 `main` 长期保留 permanent writer；
- 不为普通大文件小修改临时搭 writer；
- 不为触发 writer 临时 retarget 产品 PR；
- `github_connector_large_file_editing_playbook.md` 仅保留历史机制参考。

## 4. ChatGPT 输出给 Luna 的标准交付

大文件 patch 默认包含三块：

```text
1. PATCH
2. TEST COMMANDS
3. LUNA EXECUTION PROMPT
```

Luna prompt 至少要求：

```text
- 不切 branch
- 不做额外修改
- git apply --check
- git apply
- git diff --check
- 显示 status/stat/diff
- 跑指定测试
- 仅测试 GREEN + diff 正确时 git add 指定文件
- staged diff audit
- commit 指定 message
- git push origin HEAD
- 不 merge / 不 force push
- 返回 old HEAD / new SHA / tests / push result
```

## 5. Luna 失败时的规则

如果：

- patch 不适用；
- `git diff --check` 失败；
- 测试失败；
- 出现额外 modified files；

Luna 应停止并返回结果，不自行扩大修复范围。

ChatGPT 根据结果重新设计 patch 或测试，不要求 Luna 自己探索整个大文件。

## 6. Push 后的远端审计

收到 Luna 新 commit SHA 后，ChatGPT 必须重新从 GitHub 验证：

```text
PR live head == returned SHA
parent == expected previous head
changed files == expected allowlist
hunks == intended patch
no unrelated churn
live main status
CI / R2 status
review threads / final review
```

不得仅根据 Luna 的文字报告假定 push 成功。

## 7. Line-ending policy

根目录 `.gitattributes` 是仓库行尾权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等文本使用 LF；
- `.bat` / `.cmd` 使用 CRLF。

不要依赖全局 `core.autocrlf` 来保证 source-contract tests。

在 Windows 上不要默认用 PowerShell `>` 生成 patch；可能引入 UTF-16 或 CRLF 差异。优先让 Codex/Luna 在 worktree 中保存 patch 并执行 `git apply --check`。

## 8. Tests-first 和 commit 边界

Behavior-changing source work：

```text
RED tests commit
-> real CI RED evidence
-> GREEN implementation
-> exact diff
-> final CI / review
```

若 GREEN implementation 需要修改 connector 无法安全处理的大文件，则 RED 可以先通过 connector 提交，随后把 GREEN patch + tests 交给 Luna；Luna 测试通过后 commit/push。

## 9. Merge discipline

无论修改由 connector、Luna 或完整 Codex worktree 完成：

- push != merge authorization；
- Ready for review != merge authorization；
- CI GREEN != merge authorization；
- **只有用户明确授权 merge 后才执行 merge。**

## 10. 新会话决策树

```text
确认 live main / active PR
        ↓
目标文件完整内容可靠？
  YES -> connector direct / Git Data API
  NO  -> 是否可表达为小型确定性 patch？
           YES -> ChatGPT patch + Codex Luna apply/test/commit/push
           NO  -> 完整 Codex/local worktree
        ↓
exact diff + CI/R2 + review
        ↓
用户明确授权后 merge
```
