# CampBoardGameHost 单开发者 GitHub Connector 工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-23  
> 适用仓库：`Jazz0006/CampBoardGameHost`  
> **本文件覆盖本项目其他运行手册中与 connector 写入路径选择冲突的旧建议。**

## 1. 项目现实前提

CampBoardGameHost 当前按**单一开发者项目**运行：

- 基本不存在多人同时修改同一文件的常态竞争；
- branch / stacked PR 通常由同一开发者明确安排；
- 真正需要优先防范的是**拿错 branch、使用过期 head/blob、整文件重建时意外改动无关内容、以及大文件内容被 connector 截断后仍继续 whole-file replacement**；
- GitHub connector 的 `update_file` 使用当前 blob SHA，提供 optimistic concurrency guard，但它写的是**完整文件内容**，不是 patch hunk。

因此本项目不把“文件很大”本身当作禁止 whole-file replacement 的理由；但一旦 connector 无法可靠提供完整目标文件，必须立即停止整文件重建，切换到 patch-level 写入路径。

## 2. 默认策略：完整内容可靠时 whole-file replace

只要 connector 能够可靠读取并构造**目标 branch 当前版本的完整 UTF-8 文件内容**，可以优先考虑 direct `update_file` 或 Git Data API atomic commit。

标准路径：

```text
明确目标 branch / PR
  -> 读取 live head SHA
  -> 从 exact head 读取完整目标文件 + blob SHA
  -> 基于该完整版本生成新内容
  -> 写入前再次确认 head 未漂移
  -> update_file / atomic Git Data commit
  -> 立即 exact diff audit
  -> focused tests（需要时）
  -> full CI / R2 / review gate
```

### Whole-file 硬性安全规则

1. **绝不能拿 `main` 的文件去覆盖 feature branch，反之亦然。**
2. 完整文件必须从真正要写入的目标 branch 当前 head 读取。
3. 写入使用的 blob SHA 必须属于刚读取的目标版本。
4. 从读取到写入之间若 branch head 已变化，停止；重新 fetch head/file/blob。
5. whole-file replace 后必须立即做 exact diff audit。
6. 如果计划只改少量代码，却出现大量无关 churn、异常删除、全文件 reformat/import 变化，拒绝该 commit。
7. tests-first / RED provenance、focused tests、full CI、review/merge gate 不因写入方式而降低。

## 3. 明确的升级条件：看到截断就不要重建整文件

以下任一条件成立时，不再尝试 direct whole-file replacement：

- connector 返回明确 truncated 内容；
- 无法证明读取的是完整目标文件；
- 文件/请求体大到无法安全构造完整 replacement；
- exact diff 无法证明 reconstruction 没有无关 churn；
- 修改本质上只是一个小范围、稳定、可机械表达的单文件 patch，而完整目标文件不可安全获得。

特别是：

> **“文件只有几行需要修改”并不能降低 whole-file API 的风险。** Contents API 仍然要求完整 replacement。

PR #40 的 `ClocktowerHostScreen.kt`（约 433 KB）最终修复证明：当 connector 已无法可靠返回完整文件时，为几行修改继续尝试 whole-file 路径只会增加风险和人工成本。

## 4. Permanent trusted patch writer：大文件小 patch 的标准 fallback

仓库 default branch 长期维护：

```text
.github/workflows/trusted-patch-writer.yml
tools/trusted_patch_writer/prepare_request.py
```

完整协议、安全边界和操作步骤见：

- `docs/TRUSTED_PATCH_WRITER.md`

它用于：

```text
大文件 / connector 内容不完整
+ 单文件
+ 小范围确定性 unified diff
+ 可以锁定 exact head + target blob
```

writer 由 repository owner 在目标 PR Conversation 中发布结构化 `/trusted-patch-writer` 评论触发。它不会接受 arbitrary shell command，只允许固定字段，并在 runner 内执行：

```text
live PR head verification
-> exact SHA checkout
-> exact target blob verification
-> git apply --check
-> git apply
-> git diff --check
-> exact single-file scope guard
-> fixed validation profile
-> staged exact-scope audit
-> git ls-remote remote-head recheck
-> commit + non-force push
-> explicit CI / R2 dispatch
```

任何锁、scope 或测试失败都 fail closed。

## 5. 当前写入路径优先级

```text
A. connector update_file
   条件：目标文件完整内容可靠可得，单文件 replacement 可安全构造
        ↓ 不适用
B. Git Data API atomic commit
   条件：一个或多个完整目标 blob 可可靠构造，需要原子提交
        ↓ 不适用
C. permanent trusted patch writer
   条件：完整大文件不可安全获得，但改动是 <=40 KiB 的确定性单文件 patch
        ↓ 不适用
D. 完整本地/Codex Git worktree
   条件：复杂重构、多文件语义编辑、大 patch、writer 窄协议无法表达
```

不要再为普通大文件小 patch 临时创建 writer、临时 retarget 产品 PR base、制造 trigger commit 或采收 source blob。

## 6. Temporary writer 的新定位

Temporary GitHub Actions writer 从“常规 fallback”降级为**历史/紧急例外**。

只有 permanent writer 无法覆盖、完整 worktree 又确实不可用，而且必须依赖 runner 机械 transformation 时才考虑。若必须使用：

- 明确记录 permanent writer 为什么不适用；
- workflow 极简；复杂逻辑放独立脚本；
- transformation fail closed；
- runner 验证 exact scope；
- writer GREEN 不替代正式 CI；
- 不把临时基础设施混入产品 source commit；
- 用完立即清理。

`docs/github_connector_large_file_editing_playbook.md` 中 temporary writer 内容继续作为历史机制参考；与本文件冲突时，以本文件和 `TRUSTED_PATCH_WRITER.md` 为准。

## 7. Exact diff audit 对所有路径都强制

无论使用 whole-file、Git Data API、permanent writer 还是 worktree，写后至少核对：

```text
expected parent/head
expected changed file list
expected file count
expected hunks / semantic locations
reasonable additions/deletions
no unrelated workflow/docs/source changes
no accidental full-file reformatting
```

例如计划只改约 15 行，却得到：

```text
+420 / -380
```

除非 churn 本来就在计划内，否则视为失败，而不是继续靠编译通过来证明安全。

## 8. Stacked PR / 未完成工作

存在 stacked work 不改变核心规则：

- 所有读取和写入都针对**实际目标 branch live head**；
- 不从 main 猜 feature 文件内容；
- patch writer 必须锁目标 PR head 和 target blob；
- unrelated stacked work 不进入当前 diff；
- 如果目标 branch head 已漂移，旧请求/旧 patch 失效，重新生成。

## 9. Line-ending policy

根目录 `.gitattributes` 是本仓库文本行尾权威：

- Kotlin/KTS/Java/Markdown/YAML/Python/JSON/XML/shell/properties 等统一 LF；
- Windows `.bat` / `.cmd` 保持 CRLF。

不要依赖开发者全局 `core.autocrlf` 来维持 source-contract tests 的稳定性。尤其 Windows working tree 中，仓库 policy 应优先于机器个人习惯。

已有未提交修改时，不要为了行尾规范化直接 destructive restore/reset；先保护真实修改。

## 10. 新对话默认决策规则

新的 ChatGPT / Codex 开发对话在写代码前按以下判断：

```text
1. 这是 CampBoardGameHost 吗？
   -> 是：按单开发者 + live-head discipline

2. 能否从目标 branch exact head 可靠取得完整目标文件？
   -> 能：update_file / Git Data API 候选

3. 是否已经出现 truncation / incomplete content？
   -> 是：停止 whole-file reconstruction

4. 是否是小范围确定性单文件 patch？
   -> 是：permanent trusted patch writer

5. 是否是复杂、多文件或大范围语义修改？
   -> 是：完整 Git worktree

6. 所有路径完成后
   -> exact diff + focused/full CI + R2 + review + explicit merge authorization
```

新对话不得因为“大文件”机械地使用 writer，也不得在已经确认内容截断后继续尝试整文件 replacement。

## 11. 与其他文档的关系

- `docs/README.md`：新开发任务文档入口。
- `docs/TRUSTED_PATCH_WRITER.md`：permanent writer 的协议与安全规范。
- `docs/github_connector_large_file_editing_playbook.md`：Git Data API / temporary writer / workflow trigger 的历史和机制参考。
- 本文件对本仓库的**写入路径选择**具有最高项目级运行优先级。

当前原则：

> **complete content + exact head/blob => whole-file / atomic write 可接受；incomplete/truncated large file + small deterministic patch => permanent trusted patch writer。**

产品架构、R6 rollout、tests-first 要求和 merge 授权规则不由本文件改变。
