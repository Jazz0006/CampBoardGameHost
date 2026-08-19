# GitHub Connector 大文件安全修改运行手册

> 文档角色：**REFERENCE / DEVELOPMENT OPERATIONS**  
> 适用环境：无法直接使用本地工作区，只能通过 ChatGPT GitHub connector + GitHub Actions 修改仓库时  
> 首次验证：2026-08-20，`Jazz0006/CampBoardGameHost`，分支 `codex/storyteller-algorithm-v4`

## 1. 背景与已确认限制

GitHub connector 的普通 `update_file` 写入路径基于 GitHub Contents API。该接口在修改已有 UTF-8 文本文件时，需要提交**完整的新文件内容**，而不是 unified diff / patch hunks。

因此：

- 小文件、小改动：直接使用 connector `update_file` 最简单；
- 大文件、小改动：如果文件达到数百 KB，而实际只修改几十行，整文件覆盖的风险和操作成本明显增加；
- connector 当前不能把 `git apply` / `apply_patch` 直接作用到远端 working tree；
- 不应为了几行改动重新生成并上传一个超大文件，尤其是文件内容接近 connector/tool payload 上限时。

本项目在 R2 MainActivity mechanical decomposition 中，以约 848 KB 的 `MainActivity.kt` 为验证对象，成功建立并多次复用了一条远端安全 patch 路径。

## 2. 核心方案

核心思想：

> **connector 只负责修改一个很小的 GitHub Actions workflow；真正的大文件局部修改在 GitHub Actions runner 的本地 git working tree 中完成。**

执行链路：

```text
ChatGPT
  -> GitHub connector
  -> 创建/更新临时 GitHub Actions executor workflow
  -> Actions runner checkout 目标分支
  -> Python / shell 在 runner 本地精确修改大文件
  -> marker/结构检查
  -> git diff --check
  -> commit
  -> push 回原开发分支
  -> connector 审计实际 source commit
  -> 正式 CI 验收
  -> executor 降权为 read-only verifier 或删除
```

这相当于把原来需要用户在本机执行的：

```bash
git apply ...
git diff --check
git commit
git push
```

迁移到一次性的 GitHub Actions runner 中执行。

## 3. 何时使用

优先使用该方案的典型情况：

- 单个源文件数百 KB 或更大；
- 需要从大文件中机械抽取连续代码块；
- 只需要替换少量精确文本，但整文件重写风险高；
- 多文件 move/extract 可以用确定 marker 表达；
- 当前环境没有本地 repo / `gh` / `git apply` 能力；
- GitHub connector 没有直接 patch API；
- 仓库允许 GitHub Actions 对开发分支写入。

不应使用的情况：

- 文件很小，connector `update_file` 足够安全；
- 修改依赖模糊语义、无法用稳定 marker 表达；
- 需要大量人工编辑而无法构造可验证的机械 transformation；
- fork PR 或不可信外部输入需要写仓库内容；
- branch protection 不允许 Actions bot push。

## 4. 推荐的标准流程

### Step 1 — 先审计目标文件与修改边界

通过 connector 读取/搜索目标文件，确认：

- start marker；
- end marker；
- marker 是否唯一；
- 需要跨文件提升的 visibility；
- 新文件需要的 imports；
- 哪些业务逻辑必须明确保持不变。

对于行为保持型重构，应提前写清禁止事项，例如：

```text
不改规则语义
不改 state ownership
不改 persistence ordering
不改 revision wiring
不改 cache/recommendation behavior
```

### Step 2 — 创建临时 executor workflow

workflow 应：

- checkout 明确的目标 branch；
- `fetch-depth: 0`；
- 只在明确开发 branch / 临时 PR 上运行；
- 仅在真正需要写 source 时使用：

```yaml
permissions:
  contents: write
```

不要长期保留这种写权限。

### Step 3 — transformation 必须 fail closed

推荐使用 Python 做精确文本 transformation。

示例：

```python
old = "private fun Example(...)"
new = "internal fun Example(...)"

if source.count(old) != 1:
    raise SystemExit("marker missing or not unique")

source = source.replace(old, new, 1)
```

连续代码块抽取：

```python
start = source.find(start_marker)
if start < 0 or source.find(start_marker, start + 1) >= 0:
    raise SystemExit("start marker missing or not unique")

end = source.find(end_marker, start)
if end < 0:
    raise SystemExit("end marker not found")

block = source[start:end]
source = source[:start] + source[end:]
```

原则：

- marker 不唯一 -> fail；
- marker 不存在 -> fail；
- target 已存在但结构不符合预期 -> fail；
- 不允许“尽量猜”或 silently continue。

### Step 4 — transformation 尽量幂等

executor 可能因为 PR synchronize / workflow retry 被再次运行，因此应先检测目标状态：

```python
if target_path.exists() and source_marker not in source:
    # 已经成功抽取，只验证结构后 exit 0
    ...
```

这样重复执行不会再次删除/复制代码。

### Step 5 — 写文件时统一 EOF

本次实验中实际遇到过：

```text
git diff --check
new blank line at EOF
```

推荐统一：

```python
path.write_text(text.rstrip() + "\n", encoding="utf-8")
```

避免额外空行和 EOF 格式噪音。

### Step 6 — commit 前必须做结构和 diff 检查

至少：

```bash
git diff --check
git diff --stat
git diff --cached --check
```

并使用 `grep` / Python assertion 检查：

- 新文件存在；
- 应移动的声明已进入目标文件；
- app root 不再保留旧声明；
- 必要的 `internal` visibility 已存在；
- 禁止修改的关键 marker 仍存在。

### Step 7 — Actions bot 只提交明确文件

不要直接：

```bash
git add .
```

应显式：

```bash
git add path/to/source.kt path/to/target.kt
```

然后：

```bash
git commit -m "refactor: ..."
git push origin HEAD:<target-branch>
```

### Step 8 — connector 审计真正的 source commit

executor success **不能替代代码审计**。

Actions push 完成后，通过 GitHub connector：

- fetch commit；
- 看实际 diff；
- 核对文件列表；
- 确认只有预计的 mechanical movement / visibility change；
- 如果发现遗漏，仅做最小 mechanical follow-up。

### Step 9 — 使用正式 CI 做验收

source commit 写入后必须重新经过项目正式 CI。

不要把 executor 自己的：

```text
script completed successfully
```

当成产品构建/测试通过。

本项目 R2 使用的正式 gate 包括：

- Android unit tests；
- debug APK build；
- ASP contract tests；
- Real Clingo cross-validation。

### Step 10 — 完成后立即降权

完成写入后，应把临时 executor：

```yaml
permissions:
  contents: write
```

改成：

```yaml
permissions:
  contents: read
```

并变成只读 structural verifier，或直接删除。

不要在仓库中长期保留可被普通 push/PR 触发、同时可以任意修改 source 的 workflow。

## 5. 本次实验中已经遇到并解决的问题

### 5.1 Workflow YAML 本身无效

最初 Python triple-quoted string 的内容破坏了 YAML `run: |` 的缩进，导致 workflow 没有正常注册。

经验：

- 避免在 YAML 内嵌 Python 时生成会跑到 YAML column 0 的文本；
- 构造多行 header 时优先：

```python
header = "\n".join([
    "package ...",
    "",
    "import ...",
])
```

### 5.2 EOF 空行导致 `git diff --check` 失败

解决方式：

```python
text.rstrip() + "\n"
```

### 5.3 Actions bot push 不等于普通用户 push

GitHub Actions 的 `GITHUB_TOKEN` push 可能不会像普通 connector/user commit 一样递归触发所有 workflow。

因此：

- source executor push 后，不要假定正式 CI 一定自动重新触发；
- 必须显式观察对应 commit/run；
- 必要时通过普通 connector commit、PR event 或其他明确事件触发正式 CI。

### 5.4 PR 用作 workflow 可观察通道

当 connector 只能方便读取 PR-triggered workflow runs 时，可创建临时 draft PR 作为 Actions 可观察通道。

但必须：

- 标记 draft；
- body 明确 `DO NOT MERGE`；
- 不把这个 PR 当作正常 merge vehicle；
- 任务结束后关闭/清理。

### 5.5 跨文件 `private` visibility 遗漏

机械抽取后，新文件可能仍引用 app root 中的 `private` helper。

处理原则：

- 只把确实需要跨文件调用的 declaration 提升到 `internal`；
- 不顺便重构其实现；
- 在正式 CI 前尽量通过依赖盘点提前发现；
- Android compiler 仍是最终兜底。

## 6. 安全边界

使用该方案时必须坚持：

1. **只对已知同仓库开发分支开放 `contents: write`。**
2. **不要让不可信 fork PR 获得 source write 权限。**
3. transformation 应由固定 marker 驱动，不执行来自 issue/comment/PR 文本的任意 shell。
4. commit 前显式检查 staged files。
5. executor 完成后立即降权或删除。
6. 正式 CI 与 source diff 审计缺一不可。
7. 对规则/状态/persistence 等高风险代码，仍然按小 batch 拆分并逐批验收。

## 7. 选择策略

以后在 GitHub connector 环境修改代码时，按以下顺序判断：

```text
目标文件小、完整内容安全可控
  -> connector update_file

目标文件大，但修改可精确表达为 marker/patch
  -> GitHub Actions temporary executor（本手册）

修改复杂、无法安全机械表达
  -> 优先本地 git/Codex workspace；必要时生成 patch 由用户本地应用
```

本手册的目的不是用 Actions 替代正常开发环境，而是在**只有 GitHub connector、没有本地 working tree**时，为大文件局部修改提供一条已经实际验证过的安全路径。

## 8. 已验证实例

R2 MainActivity decomposition 已用该模式完成并通过 CI 的代表性操作包括：

- 从约 848 KB `MainActivity.kt` 抽取 Werewolf support；
- 将 Android Activity shell 与 `CampBoardGameHostApp()` 分离；
- 抽取 Undercover UI/helpers；
- 抽取 Werewolf host UI；
- 抽取 Clocktower pre-game setup UI；
- 机械修复抽取后的共享 helper visibility；
- 最终把临时 executor 恢复为 `contents: read` structural verifier。

因此后续遇到类似“大文件 + 小范围结构修改”的 connector-only 场景，应直接复用本手册，不再重新尝试整文件覆盖、手工 patch 下载或无保护的 workflow 写入方案。
