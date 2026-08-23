# ChatGPT ↔ Codex Luna 本地实现与验证工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`

## 1. 采用结论

对于 GitHub connector 无法可靠读取完整内容的大文件，尤其是 `ClocktowerHostScreen.kt` 这类数百 KB 文件，本项目不再把“ChatGPT 生成 patch、Luna 只负责 apply”作为唯一默认路径。

正式采用更完整的本地执行模型：

```text
ChatGPT
  -> 审计 live main / PR / feature head
  -> 划定当前 slice 的架构边界与不变量
  -> 设计 tests-first / characterization-first 验证矩阵
  -> 给出精确机械编辑指令，必要时才提供已验证 patch
  -> 定义 focused / full local validation

Codex Luna（本地完整 Git worktree）
  -> 校验当前 branch/head
  -> 在允许范围内直接机械编辑完整文件
  -> 或 apply 已验证 patch
  -> git diff --check + exact local diff audit
  -> 先跑 focused tests
  -> 再跑本地 full Android / build / 可执行 contract checks
  -> 全部 GREEN 后 commit
  -> push 当前 feature branch
  -> 返回 commit SHA / diff / test result

ChatGPT
  -> 从 GitHub 重新读取 live PR/head
  -> 校验 parent / exact changed files / hunks
  -> 检查 GitHub CI / R2
  -> 处理 review threads / final review
  -> 仅在用户明确授权后 merge
```

核心分工：

```text
ChatGPT = architecture / scope / invariants / review authority
Luna    = local implementation / validation executor
GitHub  = independent clean-environment merge gate
```

## 2. 为什么升级工作流

最初采用 Luna patch 流程，是因为 GitHub connector 对大文件采用 whole-file replacement；当读取内容被截断时，为几行修改重建数百 KB 文件风险过高。

在 Source Decomposition A1 中又暴露了第二类风险：人工拼写 unified diff 的 hunk header 可能产生结构错误。即使逻辑修改正确，只要行号、上下文或 hunk 计数不匹配，`git apply` 就会报 `corrupt patch`。

因此以后：

- 不把人工手写 unified-diff hunk header 作为默认路径；
- 大文件重构优先让 Luna 在完整 worktree 中按精确声明边界直接移动/编辑；
- 如确实需要 patch，patch producer 必须用真实旧/新文件自动生成，并在可行时先做 `git apply --check`；
- Luna 本地承担主要开发验证，避免每个小 slice 都依赖远端 CI 周期。

Remote writer 方案仍保持：

```text
EXPLORED
STATICALLY VALIDATED
NOT END-TO-END VALIDATED
NOT ADOPTED
```

## 3. 什么时候优先使用 Luna

满足任一条件即优先使用本流程：

- GitHub connector 返回大文件内容 truncated / incomplete；
- whole-file replacement 对小改动产生不合理风险；
- 修改涉及大文件 declaration extraction / 多处机械移动；
- 修改需要完整 Git worktree 才能安全重构；
- 本地 Gradle / Android / Clingo / Python 验证比反复等待远端 CI 更适合作为开发循环；
- 需要在提交前看到真实 Git diff、编译结果和完整测试结果。

不要因为逻辑改动只有几行就继续使用 connector whole-file replacement；API 风险由目标文件大小决定，不由 hunk 大小决定。

## 4. ChatGPT 必须提供的执行规格

每次交给 Luna 的任务至少包含以下四部分。

### A. BASELINE

必须明确：

- 预期 branch；
- 预期 HEAD；
- 允许修改的文件范围；
- 禁止修改的相邻功能范围。

如果 branch/head 不匹配，Luna 应停止，不自行切换、rebase 或猜测。

### B. IMPLEMENTATION INSTRUCTIONS

优先描述可机械执行的声明级操作，例如：

```text
move exact top-level declarations A/B/C
from file X
to file Y
preserve package/signatures/bodies/call sites
no import cleanup beyond compile necessity
no unrelated formatting
```

对于大文件 decomposition，这通常优于 ChatGPT 手写 patch。

如果提供 patch：

- 必须来自真实旧/新文件自动生成；
- 不允许人工伪造 hunk 行号；
- Luna 必须先运行 `git apply --check`；
- check 失败即停止，不自行重写 patch。

### C. LOCAL VALIDATION MATRIX

默认顺序：

```text
1. git diff --check
2. focused characterization / unit tests
3. :app:testDebugUnitTest
4. :app:assembleDebug
5. 任务相关的 ASP / Real Clingo / Python contract checks（若本地可执行）
6. exact local diff audit
```

Android production 修改通常至少要求：

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon --build-cache
```

如果 slice 有明确 focused tests，必须先跑 focused，再跑 full local suite。

### D. COMMIT / PUSH CONTRACT

只有当：

- 预期测试全部 GREEN；
- `git diff --check` GREEN；
- changed files 全部在 allowlist；
- exact diff 没有无关 churn；

Luna 才可以 commit + push 当前 feature branch。

默认禁止 merge。

## 5. Luna 的职责边界

Luna 是**本地实现与验证执行器**，不是架构决策者。

允许：

- 按精确指令直接编辑完整 worktree；
- declaration extraction / file move / import adjustment；
- apply 已验证 patch；
- 运行 focused tests；
- 运行 full Android unit tests / debug build；
- 运行任务指定的本地 contract tests；
- 检查 diff / stat / status；
- 测试 GREEN 后 commit；
- push 当前 feature branch；
- 返回完整结果摘要。

默认禁止：

- 自己扩大 scope；
- 顺手修 unrelated failure；
- 自行改变架构目标；
- 自行改变规则/推荐/持久化语义；
- 切换 branch；
- rebase / reset / force push；
- merge PR；
- 修改未授权文件。

若测试失败，允许做的第一件事是定位失败属于：

```text
A. 当前修改真实回归
B. stale structural boundary test
C. environment/tooling failure
D. unrelated pre-existing failure
```

除非 ChatGPT 事先授权了对应修复范围，否则 Luna 应停止并报告，不自行扩大修复。

## 6. 本地测试与 GitHub CI 的职责分工

本地 Luna 是 **primary development gate**。

它回答：

> 当前真实工作区中的修改能否编译、通过 focused/full tests，并且 diff 是否精确？

GitHub CI / R2 是 **independent merge gate**。

它回答：

> 在 clean checkout、独立 runner 和仓库标准 workflow 中是否仍然成立？

因此：

- 不取消 GitHub CI；
- 但不要求每个小 extraction 都等待远端 CI 才能继续下一刀；
- 本地 full validation GREEN 后，可以继续同一 PR 内的下一小步；
- PR 准备合并前，最新 head 的 GitHub CI / R2 必须全部 GREEN。

GitHub CI 仍用于发现：

- 未提交文件依赖；
- 本地缓存掩盖的问题；
- Windows / Linux 差异；
- 文件名大小写问题；
- 环境变量或工具依赖；
- stale repository boundary contract；
- clean checkout 才会出现的问题。

## 7. 大型重构的推荐节奏

对于 Source Decomposition 这类工作，不要采用：

```text
改一点
-> push
-> 等完整 CI
-> 再改一点
```

推荐：

```text
A1 mechanical extraction
-> focused local GREEN
-> full local GREEN
-> local exact diff

A2 mechanical extraction
-> focused local GREEN
-> full local GREEN
-> local exact diff

A3 ...

阶段性 push / 或每个安全 commit push
-> ChatGPT remote exact diff audit
-> GitHub CI / R2 independent validation
```

如果 push 会自动触发 CI，可以让 CI 并行运行；除非失败揭示当前 slice 的真实问题，否则不需要把远端 runner 当成每一步的同步阻塞点。

## 8. Structural boundary tests 必须随架构演进

R2 一类结构测试属于架构契约，不是永远固定的文件位置断言。

当一个 declaration 被**有意、经批准地**迁出原文件时：

1. 先确认 extraction 本身符合当前 refactor plan；
2. 保留“旧位置不应再存在”的断言；
3. 把“新位置必须存在”的断言更新到新的 owner；
4. 不应为了让旧 R2 GREEN 而把 declaration 搬回错误位置。

Source Decomposition A1 的例子：

```text
clocktowerShownAsDifferentRole
  old owner: ClocktowerHostScreen.kt
  new owner: ClocktowerHostCoreSemantics.kt
```

R2 应验证迁移完成，而不是要求它永远留在 host monolith。

## 9. Patch 使用规则

Patch 仍然是可用工具，但不再是大文件任务默认媒介。

适合 patch：

- 小而稳定的文本修改；
- 上下文已完整取得；
- patch 可由真实文件自动生成；
- 可以可靠 `git apply --check`。

不适合 patch：

- 数百 KB monolith 的大范围 declaration extraction；
- 需要大量人工 hunk header；
- 文件持续变化；
- patch producer 无法验证完整原文件。

严禁把“手写 hunk + 希望 git apply 接受”作为标准工作流。

## 10. Windows / line-ending 规则

仓库根目录 `.gitattributes` 是文本行尾权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等使用 LF；
- `.bat` / `.cmd` 保持 CRLF。

不要通过 Windows PowerShell `>` 手工拼 patch 作为默认路径；不同 PowerShell 版本可能产生编码或 CRLF 差异。

如果 working tree 已有真实未提交修改，不要使用 destructive `git restore .` / reset 处理行尾或测试问题。

## 11. Push 后 ChatGPT 的远端 gate

Luna / 用户 push 后，ChatGPT 必须重新验证真实 GitHub 状态，而不是信任文字报告：

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

如果 CI 失败，先判断是：

```text
behavior regression
compile failure
stale structural contract
environment-only failure
unrelated failure
```

再决定是否允许下一步修复。

## 12. 与其他写入方式的优先级

```text
A. 小/中等文本文件，connector 能可靠得到完整内容
   -> GitHub connector direct update + exact diff audit

B. 大文件、小范围但完整内容安全可得
   -> direct edit / automatically generated patch 可考虑

C. 大文件出现 truncation，或 declaration extraction / 多处机械移动
   -> Luna complete-worktree direct implementation（默认）

D. 多文件复杂重构
   -> Luna/local complete worktree + ChatGPT architecture/review
```

Temporary/permanent remote writer 不再是本项目常规路径。

## 13. 不改变的治理规则

- live main/head 必须重新确认；
- tests-first 工作仍需真实 RED；纯重构使用 characterization-first；
- exact diff audit 强制；
- 本地 full validation 不能替代 merge 前 GitHub CI / R2；
- unfinished stacked work 不得混入当前 task；
- Luna push 不等于批准 merge；
- **未经用户明确授权，ChatGPT 和 Codex 都不得 merge。**
