# GitHub Connector 大文件安全修改运行手册

> 文档角色：**REFERENCE / DEVELOPMENT OPERATIONS**  
> 适用环境：主要通过 ChatGPT GitHub connector 修改仓库；本地 working tree 不可用或不是首选  
> 首次验证：2026-08-20，`Jazz0006/CampBoardGameHost`，分支 `codex/storyteller-algorithm-v4`  
> 最近更新：2026-08-20，R4.2 trusted-writer / Git Data API 工作流审计

## 1. 背景与已确认限制

GitHub connector 的普通 `update_file` 写入路径基于 GitHub Contents API。修改已有 UTF-8 文件时，它提交的是**完整的新文件内容**，而不是 unified diff / patch hunk。

因此：

- 小文件、小改动：`update_file` 简单且安全；
- 大文件、小改动：整文件覆盖成本和误改风险明显增加；
- connector 不能直接把 `git apply` / `apply_patch` 作用到远端 working tree；
- 但 connector 已可使用 Git Data API 级别的 blob/tree/commit/ref 操作，从而把多个已准备好的文件组成**单一原子 commit**；
- GitHub Actions temporary writer 仍然有价值，但不再作为默认路径。

本项目在 R2 期间用约 848 KB 的 `MainActivity.kt` 验证了 Actions runner 的机械抽取方案；R4.1/R4.2 又验证了 Git Data API 原子提交，以及 `pull_request` workflow 的 base-branch 安全语义。

## 2. 两级操作策略

### 2.1 默认路径：原子 source commit + 正常 PR CI

只要目标改动可以在 connector 侧可靠构造完整的新文件内容，或可以通过 Git Data API 准备多个 blob，就优先使用：

```text
read-only audit
  -> 锁定目标 branch head SHA
  -> 生成目标文件新内容 / blob
  -> 基于当前 tree 创建单一 commit
  -> fast-forward feature branch
  -> connector 审计 exact diff / file scope
  -> 正常 PR CI
  -> clean-head 验收
```

优点：

- 不需要临时修改 PR base；
- 不需要临时 `contents: write` workflow；
- 不需要制造 trigger commit；
- source commit 本身就是正常 PR synchronize 事件；
- 代码 diff 与验证基础更直接；
- 多文件可以一次原子落地，避免 Contents API 逐文件提交产生中间破损状态。

使用 Git Data API 时必须：

1. 先读取当前 feature head；
2. source commit parent 必须等于刚确认的 head；
3. branch ref 只做 fast-forward；
4. commit 后立即用 compare/fetch commit 核对实际文件列表；
5. 不把 unrelated working changes 混入 commit。

### 2.2 备用路径：temporary trusted writer

只有以下情况才优先使用 Actions writer：

- 单个文件很大，完整内容无法安全通过 connector 传输；
- 修改是稳定 marker 驱动的机械 transformation；
- 需要 runner 本地 `git diff --check` / compile / focused test 后才决定是否写 source；
- Git Data API 无法在 connector 侧安全构造目标文件。

执行链路：

```text
connector
  -> trusted base 上的临时 writer
  -> runner checkout feature branch
  -> fail-closed deterministic patch
  -> scope guard
  -> compile / focused / full validation
  -> staged-only commit
  -> push feature branch
  -> connector 审计 source commit
  -> 删除/降权临时基础设施
```

**trusted writer 是备用工具，不是日常默认开发方式。**

## 3. `pull_request` workflow 的重要语义

R4.2 已实际确认以下行为边界。

### 3.1 PR workflow 采用 base branch 上的 workflow 定义

对于 `pull_request` event，不应假设 feature/head branch 新增的 workflow 会直接作为 trusted workflow 执行。需要执行有写权限的临时 validator 时，workflow 定义必须存在于 PR 的 trusted base 上。

因此下面这种假设不可靠：

```text
feature branch 新增 writer
  -> PR synchronize
  -> writer 自动以该 feature 定义运行
```

正确理解是：**PR workflow 的安全边界由 base branch 控制。**

### 3.2 修改 base 上已有 workflow 不会自动产生新的 PR event

更新 base branch 中已有 workflow 文件，并不等于生成新的 `pull_request` synchronize 事件。不能期待“改一下 base workflow”自动重新验证当前 head。

### 3.3 同 tree / no-op commit 不是可靠 trigger

只改变 commit identity、但 tree 内容没有实质变化的 commit，不能作为稳定的 Actions 重新调度手段。GitHub 可能不会创建期望的 workflow run。

**不要把 no-op commit 当成正式触发机制。**

### 3.4 新 workflow identity + 真实 head tree change 最可靠

R4.2 最终稳定触发 trusted writer 的方式是：

1. trusted base 上创建新的 workflow 文件名/identity；
2. PR base 明确指向包含该 workflow 的 commit；
3. feature branch 发生真实 tree change；
4. 观察新 workflow run 实际关联到该 head/base。

这只应在确实需要 trusted writer 时使用。

### 3.5 Actions bot push 不等于普通用户 push

`GITHUB_TOKEN` 产生的 push 不应被假定会递归触发所有普通 push/PR workflow。source writer push 后必须显式检查对应 commit 的 CI；没有 run 时不能把“writer 已绿”冒充成正常 CI 已绿。

## 4. Temporary writer 的标准安全流程

### Step 1 — 先审计目标边界

明确：

- start/end marker；
- marker 唯一性；
- 目标文件；
- 新文件 imports/visibility；
- 禁止改变的语义。

例如：

```text
不改规则语义
不改 state ownership
不改 persistence / revision 时机（除非本 batch 就是在修该合同）
不改 production recommendation
不扩 rollout
```

### Step 2 — trusted base 与权限最小化

写 source 的 job 可以临时：

```yaml
permissions:
  contents: write
```

但 workflow 顶层和其他 job 仍应 `contents: read`。必须限制为同仓库、明确 feature branch，不允许 fork PR 获得写权限。

### Step 3 — transformation fail closed

示例：

```python
old = "private fun Example(...)"
new = "internal fun Example(...)"

if source.count(old) != 1:
    raise SystemExit("marker missing or not unique")

source = source.replace(old, new, 1)
```

原则：

- marker 不存在 -> fail；
- marker 不唯一 -> fail；
- 当前 source SHA 不符合预期 -> fail；
- 目标已部分修改但结构不完整 -> fail；
- 不允许“猜一下继续”。

### Step 4 — 优先结构边界替换，不依赖脆弱缩进

R4.2 第一次 trusted writer 曾因为 YAML heredoc / Python `.replace(...)` 对 Kotlin 缩进的假设不成立而报：

```text
unexpected rebuild worker marker
```

该失败没有产生 source commit，这是正确的 fail-closed 行为。

后续优先：

- 用稳定函数/块起止 marker；
- 用索引切片替换完整结构块；
- 避免为了消除 YAML 缩进而对代码正文做全局空格 `.replace(...)`。

### Step 5 — transformation 尽量幂等

workflow retry / PR synchronize 可能重复运行。应先检测目标状态，若 source 已处于完成状态，则只做结构验证并退出，不应再次修改。

### Step 6 — 统一 EOF

写文本时：

```python
path.write_text(text.rstrip() + "\n", encoding="utf-8")
```

避免 `git diff --check` 的 EOF 噪音。

### Step 7 — transformation-time scope 必须包含 untracked

不能只用：

```bash
git diff --name-only
```

因为新文件尚未 `git add` 时属于 untracked。

应使用：

```bash
actual="$( {
  git diff --name-only
  git ls-files --others --exclude-standard
} | sort -u )"
```

### Step 8 — 验证工具本身不能污染 tracked tree

不要无意执行：

```bash
chmod +x ./gradlew
```

导致 tracked file mode 进入 diff。优先：

```bash
bash ./gradlew ...
```

### Step 9 — staged-only commit

禁止：

```bash
git add .
```

必须显式：

```bash
git add path/to/a.kt path/to/b.kt
```

然后：

```bash
git diff --cached --name-only
git diff --cached --check
```

staged scope 必须与允许列表完全一致。

### Step 10 — source commit 之后再次独立审计

writer success 不等于 source 正确。必须通过 connector：

- fetch/compare source commit；
- 核对 parent；
- 核对文件列表；
- 核对没有 workflow / unrelated source 混入；
- 再观察正式 CI。

### Step 11 — 立即清理

结束后：

- PR base 恢复正常目标（本项目为 `main`）；
- 临时 trigger 删除；
- 临时 writer 删除或恢复 read-only verifier；
- PR 保持既定 Draft/merge 策略；
- 再确认标准 workflow blob/权限未被遗留修改。

## 5. 已确认问题与经验

### 5.1 Workflow YAML 可能因 heredoc 缩进失效

避免内嵌脚本输出落到 YAML column 0。复杂 patcher 最好独立成脚本文件，workflow 只负责调用。

### 5.2 EOF 空行会让 `git diff --check` 失败

统一 `rstrip() + "\n"`。

### 5.3 Actions bot push 不保证递归触发 CI

必须观察实际 workflow run；没有 run 就没有正常 CI 证据。

### 5.4 Draft PR 可以作为 workflow 可观察通道

如确有必要，应：

- 保持 Draft；
- body 明确 `DO NOT MERGE`；
- 不把它误当成正常 merge vehicle；
- 临时改 base 后必须恢复。

### 5.5 跨文件 `private` visibility 容易遗漏

只提升真正需要跨文件访问的声明，Android compiler 是最终兜底。

### 5.6 `git diff --name-only` 不包含 untracked

创建新文件的 transformation 必须使用 tracked + untracked 联合 scope guard。

### 5.7 验证阶段不要修改 tracked executable bit

直接 `bash ./gradlew`，避免 `chmod` 噪音。

### 5.8 Base-workflow discovery 是安全模型，不是 GitHub 故障

R4.2 中多次“workflow 文件已提交但没有期望 run”的根因，不是仓库损坏，而是对 `pull_request` workflow discovery/trigger 语义理解不完整。

后续遇到类似现象，先检查：

```text
PR base 是谁？
目标 workflow 是否存在于该 base commit？
head 是否发生真实 tree change？
本次 event 是否真的匹配 workflow 的 on: 条件？
```

不要用连续无语义 commit 猜测性重触发。

## 6. 正式 CI 与 source audit

无论默认路径还是备用 writer，最终都必须拥有两类独立证据：

1. **source evidence**
   - exact parent/head；
   - exact changed files；
   - diff 审计；
   - workflow 未混入产品 source commit。

2. **validation evidence**
   - focused tests（高风险 batch）；
   - full Android unit regression；
   - debug APK build；
   - ASP contract tests；
   - Real Clingo cross-validation；
   - 必要的 structural verifier。

任何 writer 自身的“script success”都不能替代上述项目级验证。

## 7. 选择策略（当前推荐）

```text
小文件 / 完整内容安全可控
  -> connector update_file

多文件中等修改 / 可以可靠生成完整目标内容
  -> Git Data API atomic source commit
  -> normal PR CI
  -> exact diff audit

超大文件 + 小范围稳定机械变换，connector 无法安全构造完整内容
  -> temporary trusted writer
  -> deterministic patch + guarded validation
  -> staged-only source commit
  -> restore temporary infrastructure

修改复杂、无法安全机械表达
  -> 优先本地 git / Codex workspace
  -> 或生成 patch 由用户本地应用
```

**默认不要为了能够修改 source 而先设计一个 Actions writer。先判断 Git Data API 原子提交能否直接完成任务。**

## 8. 已验证实例

### R2 — large-file mechanical extraction

通过 temporary Actions executor 完成：

- 从超大 `MainActivity.kt` 抽取 Werewolf support；
- 分离 Android Activity shell 与 app root；
- 抽取 Undercover / Werewolf / Clocktower setup/day/night/host/history；
- 最终恢复 read-only structural verifier。

### R4.1 — atomic cache contract source commit

验证了：

- Git Data API 可把多个 source/test blob 组成单一原子 commit；
- source diff 可在 commit 后精确审计；
- 无需逐文件 Contents API 制造中间状态。

### R4.2 — trusted-writer trigger semantics

验证了：

- `pull_request` trusted workflow 受 base branch 定义控制；
- 修改已有 base workflow 不自动产生新的 PR event；
- no-op/same-tree commit 不是可靠触发器；
- 新 workflow identity + 真实 head tree change 可以稳定触发；
- deterministic patcher fail-closed 后，compile / focused / full Android / ASP / Clingo / staged-only source commit 全部可在同一 trusted run 中完成；
- 完成后必须恢复 PR base 和 read-only workflow。

因此以后应优先走**原子 source commit + normal PR CI**，只有 connector 无法安全表达大文件局部修改时才回退到 trusted writer。
