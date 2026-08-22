# CampBoardGameHost Permanent Trusted Patch Writer

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`

## 1. 为什么把 trusted writer 变成永久基础设施

PR #40 的最终 P1 修复只需要修改 `ClocktowerHostScreen.kt` 中很小的一段代码，但该文件约 433 KB。实践再次确认：GitHub connector 的 `update_file` 是完整文件 replacement，而不是远端 `git apply`；当 connector 无法可靠返回完整大文件时，为了几行修改重建整个文件是不安全的。

此前的 temporary trusted writer 已证明 runner 内机械 patch 是可行的，但临时 workflow 还引入了新的操作复杂度：workflow discovery 受 default/base branch 约束、临时 retarget PR、真实 tree-change trigger、采收后清理等。PR #40 最后一段工作因此仍退回到本地 patch，并额外暴露 Windows CRLF 与 PowerShell 文本编码问题。

结论是：

> **需要的不是“每次临时搭一个 writer”，而是在 default branch 中长期保留一个最小权限、fail-closed、可由 connector 触发的 patch primitive。**

永久 writer 位于：

```text
.github/workflows/trusted-patch-writer.yml
tools/trusted_patch_writer/prepare_request.py
```

## 2. 什么时候使用

选择顺序：

```text
目标文件完整内容可被 connector 可靠读取和重建
    -> update_file / Git Data API + exact diff audit

目标文件很大或 connector 已出现截断，改动是小范围、确定性的单文件 patch
    -> permanent trusted patch writer

改动跨很多文件、需要复杂语义重构、patch 很大或无法机械表达
    -> 完整本地/Codex Git worktree
```

**文件“大”本身不是唯一判据。真正的升级条件是：无法可靠得到完整目标内容，或 whole-file reconstruction 无法被安全证明。** 一旦已经观察到截断，就不要继续尝试整文件 replacement。

## 3. Connector 触发协议

writer 由 PR Conversation 中的新评论触发。第一行必须是：

```text
/trusted-patch-writer
```

完整请求格式：

```text
/trusted-patch-writer
expected_head_sha=<40-char lowercase SHA>
target_path=<one existing repository-relative text file>
expected_blob_sha=<40-char lowercase blob SHA>
test_profile=android|none
commit_message=<single-line commit message>
patch_base64=<single-line base64 encoded unified diff>
```

`patch_base64` 是 PR 评论正文的一部分；本仓库是公开仓库，因此**绝不能在 patch 或评论中放 secrets、tokens、credentials 或其他敏感内容**。

## 4. Fail-closed 安全边界

writer 在任何条件不满足时都必须停止，不能猜测继续。

### 身份与 PR 边界

- 只接受 repository owner 创建的评论；
- 必须是在 Pull Request 上，不接受普通 Issue；
- 只接受 same-repository PR；fork PR 拒绝；
- 目标 branch 不能是 default branch；
- writer 永不 merge PR。

### Head / blob 锁

请求必须同时提供：

```text
expected_head_sha
expected_blob_sha
```

writer 会：

1. 从 GitHub API 重新读取 PR live head；
2. 要求 live head 与 `expected_head_sha` 完全一致；
3. checkout exact SHA，而不是模糊 branch tip；
4. 用 `git rev-parse "$expected_head_sha:$target_path"` 验证目标 blob；
5. commit 前再次用 `git ls-remote origin "refs/heads/$target_branch"` 确认远端 head 没有漂移。

任何一步不一致都 fail。

### Patch 边界

当前永久 writer 故意只支持窄能力：

- 单个已有 UTF-8 文本文件；
- decoded patch 最大 **40 KiB**，为 PR comment 的 base64 膨胀和协议字段留出余量；
- `target_path` 只接受项目常规安全字符并要求已经规范化；
- 必须恰好只有一个 `diff --git`；
- diff header 必须与 `target_path` 一致；
- 不允许 binary patch；
- 不允许 create/delete；
- 不允许修改 `.github/`、`.gitattributes`、`.git/` 或 `tools/trusted_patch_writer/`，避免 writer 自修改；
- `git apply --check --whitespace=error-all` 必须先成功；
- apply 后 `git diff --check` 必须成功；
- `git diff --name-only` 必须精确等于唯一 `target_path`；
- staging 后再重复 cached scope / whitespace audit；
- 不使用 `git add .`。

如果任务超出这些边界，应升级到正常 feature commit / worktree，而不是扩宽评论协议去执行任意 shell。

## 5. 测试策略

请求提供固定的 `test_profile`，而不是任意命令：

- `android`：运行
  ```text
  bash ./gradlew :app:testDebugUnitTest :app:assembleDebug --no-daemon --build-cache
  ```
- `none`：只适用于不要求 Android/build validation 的窄文本修改，例如普通 docs。

以下路径强制 `test_profile=android`：

- `app/**`；
- `gradle/**`；
- `build.gradle.kts`；
- `settings.gradle.kts`；
- `gradle.properties`。

writer 自身 GREEN 仍然不是最终验证。由 `GITHUB_TOKEN` 产生的 push 不应被假设会正常递归触发 PR CI，所以 writer push 后会显式 dispatch：

```text
ci.yml
r2-write-probe.yml
```

随后 connector 仍必须：

- fetch 新 source commit；
- 核对 parent；
- 核对 exact changed file / hunk；
- 核对 dispatched CI / R2 的实际 head；
- 再经过正常 review gate。

## 6. 标准操作步骤

```text
1. 确认 live main / PR / feature head
2. 从 exact feature head 获取目标 file blob SHA
3. 生成最小 unified diff
4. 本地/模型侧验证 patch 只含一个目标文件
5. base64 编码 patch（单行）
6. 在目标 PR 发布 /trusted-patch-writer 请求
7. 检查 writer run 是否 GREEN
8. fetch writer 生成的新 commit
9. exact diff / parent / scope audit
10. 检查显式 dispatch 的 CI + R2
11. 正常 Codex review / merge gate
```

如果第 1–5 步中的 head/blob 已经变化，重新生成请求，不复用旧 patch 锁。

## 7. Line-ending policy

仓库根目录 `.gitattributes` 是跨平台文本行尾的权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等文本统一 LF；
- Windows `.bat` / `.cmd` 保持 CRLF。

这解决了 Windows `core.autocrlf=true` 下 source-contract tests 因 working-tree CRLF 而出现假失败的问题。新 clone / checkout 应以 `.gitattributes` 为准，而不是要求每个开发环境手工修改全局 Git 配置。

已有 working tree 如果包含未提交修改，不要直接使用 destructive `git restore .` / reset 来“规范化”行尾；先保存/提交真实改动，再按 Git 正常 renormalize 流程处理。

## 8. 与旧 temporary writer 的关系

`docs/github_connector_large_file_editing_playbook.md` 中 temporary trusted writer 的内容保留为历史与机制参考，尤其是 workflow trigger、安全审计和 runner patch 经验。

自本文件生效后：

- **不要为了普通大文件小 patch 新建 temporary writer；**
- **不要为了 writer 临时 retarget 产品 PR base；**
- default branch 上的 permanent trusted writer 是此类任务的标准 fallback；
- 只有永久 writer 的窄协议无法覆盖、而完整 worktree 又确实不可用时，才考虑一次性特殊 writer，并明确记录原因。

## 9. 不改变的工程规则

永久 writer 只是“安全写入 primitive”，不改变项目已有治理：

- tests-first / RED provenance 仍然适用；
- exact diff audit 仍然必需；
- full CI / R2 / review gate 仍然必需；
- writer 不自动 merge；
- 未经用户明确授权不得 merge PR；
- writer 不扩大产品 scope，也不替代架构判断。
