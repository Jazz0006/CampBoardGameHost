# ChatGPT ↔ Codex Luna 本地实现与验证工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`

## 1. 定位：这是按需启用的备用执行路径，不是所有任务的强制流程

本工作流解决的是 GitHub connector 对超大源文件、完整 worktree 重构、本地 Android/Gradle 验证等场景的能力缺口。

它不是项目永久的唯一开发路径。随着 `ClocktowerHostScreen.kt`、`CampBoardGameHostApp.kt` 等 monolith 被拆分到正常规模，很多日常修改应重新优先采用更轻量的 GitHub connector 路径。

默认选择应是：

```text
任务开始
  -> connector 是否能可靠取得完整目标文件？
  -> whole-file update 风险是否合理？
  -> 是否需要完整 Git worktree / 本地重型验证？

若普通 connector 足够安全
  -> connector direct update + exact diff + GitHub CI

若存在大文件、截断、复杂机械重构或本地验证优势
  -> Luna complete-worktree workflow
```

因此本工作流应理解为 **capability fallback / heavy-task path**，不是为了使用 Luna 而使用 Luna。

## 2. 核心分工

```text
ChatGPT
  = architecture / scope / invariants / review authority

Codex Luna
  = constrained local implementation / validation executor

GitHub
  = source of truth + independent clean-environment merge gate
```

典型流程：

```text
ChatGPT
  -> 审计 live main / PR / feature head
  -> 划定当前 slice 边界与不变量
  -> 设计 tests-first / characterization-first 验证矩阵
  -> 给出声明级机械编辑要求

Luna（完整本地 Git worktree）
  -> 安全收敛 workspace 到目标 branch/head
  -> 按 allowlist 实施修改
  -> focused tests
  -> full local Android / build / contract validation
  -> exact local diff audit
  -> GREEN 后 commit + push

ChatGPT
  -> 从 GitHub 重新读取 live head
  -> exact remote diff audit
  -> 检查 CI / R2 / review
  -> 仅在用户明确授权后 merge
```

## 3. 为什么升级自最初的 patch-only 流程

最初采用 Luna patch 流程，是因为 GitHub connector 对大文件采用 whole-file replacement；当内容读取被截断时，为几行修改重建数百 KB 文件风险过高。

Source Decomposition A1 又暴露出第二类风险：人工拼写 unified diff hunk header 可能产生结构错误。即使逻辑修改正确，只要 hunk 行号、上下文或计数不匹配，`git apply` 就会失败。

因此：

- 不把人工手写 unified-diff hunk header 作为默认路径；
- 大文件 declaration extraction 优先让 Luna 在真实 worktree 中机械移动；
- 如需要 patch，必须由真实旧/新文件自动生成并尽可能先验证；
- Luna 本地承担主要开发验证；
- GitHub CI/R2 保留为独立 merge gate。

Remote writer 方案仍保持：

```text
EXPLORED
STATICALLY VALIDATED
NOT END-TO-END VALIDATED
NOT ADOPTED
```

## 4. 何时优先使用 Luna

满足任一条件时优先考虑本流程：

- connector 返回大文件 truncated / incomplete；
- whole-file replacement 对小改动产生不合理风险；
- declaration extraction / 多处机械移动需要完整 worktree；
- 修改跨多个强关联生产文件；
- 本地 Gradle / Android / Clingo / Python 验证更适合作为开发循环；
- 需要提交前看到真实 Git diff、编译和完整测试结果。

反之，若文件已经拆小、connector 可可靠读写、修改局部且风险可控，则不必使用 Luna。

## 5. Workspace Reconciliation：Luna 应主动安全收敛环境，而不是因 branch 不对就立即停止

旧规则“branch/head 不匹配即停止”过于机械。新的目标是：**能无损自动恢复的环境问题由 Luna 自行解决；只有存在覆盖风险时才停止。**

ChatGPT 应声明目标状态，例如：

```text
repository: Jazz0006/CampBoardGameHost
remote: origin
target branch: codex/source-decomposition-clocktower-host
expected remote head: <sha or explicitly allowed live head>
workspace policy:
  preserve all existing uncommitted work
  prefer existing clean target worktree
  otherwise safely switch or create isolated worktree
  fetch before execution
  fast-forward only
  never reset --hard
  never force push
  never auto-merge
```

### 5.1 Level 1：可自动修复

Luna 可以直接执行：

```text
git fetch origin

workspace clean + 当前 branch 不正确
  -> git switch <target>

本地不存在 target，但 origin/target 存在
  -> 创建 tracking branch

本地 target 落后 origin 且可 fast-forward
  -> git pull --ff-only origin <target>
```

这些不应成为需要用户手工介入的常规阻塞点。

### 5.2 Level 2：自动隔离，不触碰用户现有工作

如果当前工作目录 dirty，默认不要自动 stash、reset 或覆盖。

优先策略：

```text
current workspace dirty
  -> 保留原工作区完全不动
  -> 如条件允许，为目标 branch 建立 isolated git worktree
  -> 在 isolated worktree 中继续任务
```

推荐优先级：

```text
已有 clean target worktree
  -> 使用它

否则当前 workspace clean
  -> 安全 switch + ff-only sync

否则当前 workspace dirty
  -> 创建 isolated worktree

否则发现分叉/覆盖风险
  -> 停止并报告
```

专用 worktree 尤其适合长期 AI 协同开发，因为主工作区可以继续停在 `main` 或用户自己的工作 branch，Luna 任务与 feature branch 天然隔离。

### 5.3 Level 3：必须停止

以下情况不得擅自解决：

- 本地与远端 target branch 已 diverged；
- 存在未 push commit，自动操作可能覆盖；
- 远端目标 branch 不存在且任务未授权创建；
- expected remote head 与实际 live head 发生不可解释变化；
- worktree 路径被另一 active task 占用；
- 需要 `reset --hard`、force push、自动 rebase/merge 才能继续。

此时 Luna 应返回精确状态，而不是破坏性“修复”。

## 6. ChatGPT 必须提供的执行规格

每次交给 Luna 的任务至少包含四部分。

### A. TARGET STATE

- repository / remote；
- target branch；
- expected live head 或允许的同步规则；
- workspace reconciliation policy；
- 允许修改的文件 allowlist；
- 禁止修改的相邻功能范围。

### B. IMPLEMENTATION INSTRUCTIONS

优先描述声明级机械操作，例如：

```text
move exact top-level declarations A/B/C
from file X
to file Y
preserve package/signatures/bodies/call sites
no semantic change
no unrelated formatting
```

如提供 patch：

- 必须来自真实旧/新文件自动生成；
- 不允许人工伪造 hunk 行号；
- 先 `git apply --check`；
- check 失败不得自行重写 patch。

### C. LOCAL VALIDATION MATRIX

默认顺序：

```text
1. git diff --check
2. focused characterization / unit tests
3. :app:testDebugUnitTest
4. :app:assembleDebug
5. task-specific ASP / Real Clingo / Python contract checks（若本地可执行）
6. exact local diff audit
```

Android production 修改通常至少要求：

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon --build-cache
```

### D. COMMIT / PUSH CONTRACT

仅当：

- 指定 tests 全部 GREEN；
- `git diff --check` GREEN；
- changed files 全部在 allowlist；
- exact diff 没有无关 churn；

才允许 commit + push。

默认永远禁止 merge。

## 7. Luna 的职责边界

Luna 是受约束的本地开发代理，不是架构决策者。

允许：

- 安全 fetch / switch / ff-only sync；
- 在必要时创建 isolated worktree；
- 按精确指令编辑完整 worktree；
- declaration extraction / file move / compile-required import adjustment；
- apply 已验证 patch；
- 运行 focused/full Android tests 和 build；
- 运行任务指定 contract tests；
- 检查 diff / stat / status；
- GREEN 后 commit + push。

默认禁止：

- 扩大 scope；
- 顺手修 unrelated failure；
- 改变架构目标或游戏规则语义；
- destructive stash/reset/clean；
- 自动 rebase / merge；
- force push；
- merge PR；
- 修改 allowlist 外文件，除非任务规格明确允许结构测试/文档同步。

若测试失败，先分类：

```text
A. 当前修改真实回归
B. stale structural boundary test
C. environment/tooling failure
D. unrelated pre-existing failure
```

未经授权不得扩大修复范围。

## 8. 本地测试与 GitHub CI 的职责分工

Luna 本地测试是 **primary development gate**：验证真实工作区中的修改能否编译、通过 focused/full tests，且 diff 精确。

GitHub CI / R2 是 **independent merge gate**：验证 clean checkout、Linux runner、仓库标准 workflow 下仍然成立。

因此：

- 不取消 GitHub CI；
- 也不要求每个小 extraction 都等待远端 CI 才能继续；
- 本地 full validation GREEN 后可继续同 PR 内下一安全 slice；
- merge 前最新 head 的 GitHub CI / R2 必须全部 GREEN。

GitHub CI 仍负责发现：

- 未提交文件依赖；
- 本地缓存掩盖问题；
- Windows/Linux 差异；
- 大小写问题；
- 环境变量/工具依赖；
- stale repository boundary contract；
- clean checkout 才出现的问题。

## 9. Structural boundary tests 必须随架构演进

R2 一类结构测试属于架构契约，不是永久固定的文件位置断言。

当 declaration 被有意迁出原文件：

1. 确认 extraction 符合 refactor plan；
2. 保留“旧位置不应再存在”的断言；
3. 更新“新位置必须存在”的断言到新的 owner；
4. 不为让旧 R2 GREEN 而把代码搬回错误位置。

Source Decomposition A1：

```text
clocktowerShownAsDifferentRole
  old owner: ClocktowerHostScreen.kt
  new owner: ClocktowerHostCoreSemantics.kt
```

## 10. Patch 使用规则

Patch 仍可使用，但不再是大文件任务默认媒介。

适合 patch：

- 小而稳定的文本修改；
- 上下文完整；
- 可由真实文件自动生成；
- 可可靠 `git apply --check`。

不适合 patch：

- 数百 KB monolith 的大范围 extraction；
- 需要大量人工 hunk header；
- 文件持续变化；
- producer 无法验证完整原文件。

严禁把“手写 hunk + 希望 Git 接受”作为标准流程。

## 11. Windows / line-ending 规则

仓库根目录 `.gitattributes` 是文本行尾权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等使用 LF；
- `.bat` / `.cmd` 保持 CRLF。

不要通过 Windows PowerShell `>` 手工拼 patch 作为默认路径。

若已有真实未提交修改，不使用 destructive `git restore .`、`reset --hard` 或 `git clean -fd` 来“恢复环境”。

## 12. Push 后 ChatGPT 的远端 gate

Luna / 用户 push 后，ChatGPT 必须重新验证 GitHub 实际状态：

```text
live PR head
parent chain
changed-file allowlist
exact diff semantics
no unrelated churn
CI / R2 status
review findings / threads
live main movement
```

Luna 的文字报告不是远端事实来源。

## 13. Worktree 生命周期

若 Luna 为任务创建专用 worktree：

- PR 未 merge 前保留，便于继续同一 feature branch；
- 不自动删除存在未提交修改的 worktree；
- merge 后可以提示用户清理；
- 除非用户明确授权，不自动删除可能承载工作的目录/branch。

## 14. 写入方式优先级

```text
A. 小/中等文件，connector 能可靠完整读写
   -> GitHub connector direct update + exact diff audit

B. 较大文件但完整内容可安全获得，小范围修改
   -> direct edit / automatically generated patch

C. 大文件出现 truncation，或 declaration extraction / 多处机械移动
   -> Luna complete-worktree direct implementation

D. 多文件复杂重构 / 重型本地验证
   -> Luna isolated worktree + ChatGPT architecture/review
```

重要目标不是让更多任务进入 Luna，而是让每个任务使用**最简单且足够安全的路径**。

如果 source decomposition 成功消除了超大文件和 whole-file replacement 风险，本工作流完全可能在一段时间内很少被使用；这是架构改善后的正常结果，不代表工作流失去价值。

## 15. 不改变的治理规则

- live main/head 必须重新确认；
- tests-first 任务保留真实 RED；纯重构使用 characterization-first；
- exact diff audit 强制；
- 本地 full validation 不能替代 merge 前 GitHub CI / R2；
- unfinished stacked work 不得混入当前 task；
- Luna push 不等于批准 merge；
- 所有自动 workspace reconciliation 都以“不丢用户工作”为最高优先级；
- **未经用户明确授权，ChatGPT 和 Codex 都不得 merge。**
