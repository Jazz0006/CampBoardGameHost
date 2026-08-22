# ChatGPT ↔ Codex Luna 本地 Patch 工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`

## 1. 采用结论

对于 GitHub connector 无法可靠读取完整内容的大文件，尤其是 `ClocktowerHostScreen.kt` 这类数百 KB 文件，本项目不再优先尝试 remote writer / temporary writer。

正式采用：

```text
ChatGPT
  -> 审计 live main / PR / feature head
  -> 设计 tests-first 最小修改
  -> 输出最小 unified diff patch
  -> 输出精确本地测试命令
  -> 输出给 Codex Luna 的机械执行 prompt

Codex Luna（本地完整 Git worktree）
  -> 校验当前 branch/head
  -> git apply --check
  -> git apply
  -> git diff --check + exact local diff audit
  -> 运行指定测试
  -> 测试 GREEN 后 commit
  -> push 当前 feature branch
  -> 返回 commit SHA / diff / test result

ChatGPT
  -> 从 GitHub 重新读取 live PR/head
  -> 校验 parent / exact changed files / hunks
  -> 检查 CI / R2
  -> 处理 review threads / final Codex review
  -> 仅在用户明确授权后 merge
```

这个流程把“架构判断与代码设计”和“真实 worktree 机械执行”分开。

## 2. 为什么采用这个方案

PR #40 最终 P1 修复仅修改 `ClocktowerHostScreen.kt` 中不到十行，但该文件约 433 KB。GitHub connector 的普通 file update 是完整文件 replacement，不是 patch-level write；一旦读取内容出现截断，为几行修改重建整个文件风险过高。

2026-08-23 又验证了 permanent `issue_comment` writer 方案：

- workflow / parser 静态实现可通过 Android / ASP / Real Clingo / R2；
- 但 pre-merge canary 无法真正触发 writer；
- `issue_comment` workflow 只有在 workflow 已存在于 default branch 时才会响应；
- 因而必须先把尚未端到端验证的 write-enabled workflow 部署到 `main`，才能验证其完整写入链路。

本项目决定不承担这个 bootstrap 风险。remote writer 方案状态：

```text
EXPLORED
STATICALLY VALIDATED
NOT END-TO-END VALIDATED
NOT ADOPTED
```

相比之下，本地 Codex worktree 已经是完整 Git 环境，`git apply`、Gradle tests、commit、push 都是成熟路径，而且 Luna 只需机械执行小 patch，不需要重新阅读大文件或做架构推理。

## 3. 什么时候必须切换到 Luna patch 流程

满足任一条件即优先使用本流程：

- GitHub connector 返回大文件内容 truncated / incomplete；
- 无法证明获得的是目标 branch 当前完整文件；
- whole-file replacement 对一个很小的修改产生不合理风险；
- 修改可以表示成清晰、确定性的 unified diff；
- 目标文件很大，而 Codex 本地 worktree 可用。

不要因为“只有 5–10 行修改”就继续使用 connector whole-file replacement；API 风险由完整文件大小决定，而不是 hunk 大小。

## 4. ChatGPT 必须提供的三个交付物

每次需要 Luna 执行大文件 patch 时，ChatGPT 默认给用户三块内容。

### A. PATCH

- 最小 unified diff；
- 只包含当前任务允许的文件；
- 尽量一个或极少数 hunk；
- 不做无关格式化；
- 不夹带 docs/workflow/其他 R6 工作。

### B. TEST COMMANDS

优先给 focused tests，再给必要的 full validation。例如 Android production 修改通常至少包含：

```powershell
.\gradlew.bat :app:testDebugUnitTest `
  --tests "<focused-test-1>" `
  --tests "<focused-test-2>" `
  --no-daemon

.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --build-cache
```

如果当前 slice 不需要 full local suite，可以明确只要求 focused tests，并由 GitHub CI 承担 full gate。

### C. LUNA EXECUTION PROMPT

推荐固定模板：

```text
在当前 Git 工作区执行下面任务，不做任何额外修改。

1. 确认当前 branch 和 HEAD，不切换 branch。
2. 将提供的 patch 保存为临时 patch 文件。
3. 执行 git apply --check <patch-file>。
4. 如果成功，执行 git apply <patch-file>。
5. 执行：
   git diff --check
   git status --short
   git diff --stat
   git diff
6. 运行提供的测试命令。
7. 只有测试全部通过且 diff 只包含预期修改时：
   git add <明确指定文件>
   git diff --cached --check
   git diff --cached --stat
   git diff --cached
8. 提交：
   git commit -m "<指定 commit message>"
9. push 当前 branch：
   git push origin HEAD
10. 不要 merge，不要修改任何其他文件。

最后返回：
- branch 名
- apply 前原 HEAD
- 新 commit SHA
- git status --short
- changed files / diff stat
- 测试结果
- push 是否成功
```

## 5. Luna 的职责边界

Luna 用于**机械执行**，不是重新设计修改。

允许：

- 保存 patch；
- `git apply --check` / `git apply`；
- 运行明确指定的测试；
- 检查 diff；
- 测试 GREEN 后 commit；
- push 当前 feature branch；
- 返回结果。

默认禁止：

- 自己扩大修改范围；
- 自己重写 patch；
- 顺手修 unrelated failures；
- 切换到另一个 branch；
- force push；
- merge PR；
- 修改用户未授权的文件。

如果 patch 不适用或测试失败，Luna 应停止并返回错误，不自行探索大范围修复。

## 6. 为什么 Luna 可以省 token

大文件任务中，昂贵步骤通常是：

```text
读取数百 KB 源码
-> 理解架构
-> 定位修改点
-> 设计改法
-> 编辑
```

在本流程中这些由 ChatGPT / 当前开发会话完成。Luna 只处理：

```text
小 patch
-> apply
-> test
-> commit
-> push
```

因此 Luna 不需要把完整大文件重新放入模型上下文，适合使用较低成本模型执行。Gradle 编译/测试主要消耗 runner/本机计算时间，不需要大量模型推理 token。

## 7. Push 是流程必需步骤

只做本地 apply/test 不足以让 ChatGPT 后续审计远端仓库。

因此只要测试 GREEN 且 diff 正确，Luna 必须：

```text
commit
-> git push origin HEAD
```

然后把新 commit SHA 返回给用户/ChatGPT。

ChatGPT 收到 SHA 后必须重新从 GitHub 获取数据，不根据 Luna 的文字报告直接假设远端成功。

## 8. ChatGPT 收回后的远端 gate

Luna push 后，ChatGPT 至少重新验证：

```text
live PR head == Luna returned commit SHA
new commit parent == expected previous head
changed files == expected allowlist
exact hunk == expected patch semantics
no unrelated churn
CI / R2 result
review findings / threads
live main has not unexpectedly moved
```

需要 tests-first 的任务还要保留真实 RED provenance，不能因为执行者换成 Luna 就跳过 RED。

## 9. Windows / line-ending 规则

仓库根目录 `.gitattributes` 是文本行尾权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等使用 LF；
- `.bat` / `.cmd` 保持 CRLF。

不要通过 PowerShell `>` 手工生成 patch 作为默认路径；Windows PowerShell 版本可能产生 UTF-16 或 CRLF 差异。优先让 Codex/Luna 在完整 worktree 中直接保存用户提供的 patch，再运行 `git apply --check`。

如果 working tree 已有真实未提交修改，不要使用 destructive `git restore .` / reset 来处理行尾问题。

## 10. 与其他写入方式的优先级

```text
A. 小/中等文本文件，connector 能可靠得到完整内容
   -> GitHub connector direct update + exact diff audit

B. 大文件但完整内容仍可安全获得，且 whole-file replacement 风险可证明可控
   -> direct / Git Data API 可考虑

C. 大文件出现 truncation / incomplete content，且修改是确定性 patch
   -> ChatGPT patch + Codex Luna apply/test/commit/push（默认）

D. 大范围、多文件复杂重构
   -> 完整 Codex/local worktree，让执行模型拥有必要上下文
```

Temporary/permanent remote writer 不再是本项目常规路径。

## 11. 不改变的治理规则

- live main/head 必须重新确认；
- tests-first 工作仍需真实 RED；
- exact diff audit 强制；
- full CI / R2 / review gate 仍生效；
- unfinished stacked work 不得混入当前 patch；
- Luna push 不等于批准 merge；
- **未经用户明确授权，ChatGPT 和 Codex 都不得 merge。**
