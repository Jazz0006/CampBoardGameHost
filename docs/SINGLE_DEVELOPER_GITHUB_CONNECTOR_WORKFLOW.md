# CampBoardGameHost 单开发者 GitHub Connector 工作流

> 文档角色：**NORMATIVE / DEVELOPMENT OPERATIONS**  
> 生效日期：2026-08-22  
> 适用仓库：`Jazz0006/CampBoardGameHost`  
> **本文件覆盖本项目其他运行手册中与“大文件默认编辑策略”冲突的旧建议。**

## 1. 项目现实前提

CampBoardGameHost 当前按**单一开发者项目**运行：

- 基本不存在多人同时修改同一文件的常态竞争；
- branch / stacked PR 通常由同一开发者明确安排；
- 真正需要优先防范的是**拿错 branch、使用过期 head/blob、整文件生成时意外改动无关内容**，而不是多人 merge conflict；
- GitHub connector 的 `update_file` 使用当前 blob SHA，天然提供一层 optimistic concurrency guard：旧 SHA 不应被当成可以静默覆盖新文件的许可。

因此，本项目不应仅因为目标文件很大，就自动设计临时 GitHub Actions writer、trigger PR 或 blob-harvest 流程。

## 2. 默认策略：whole-file replace 优先

只要 connector 能够可靠读取并构造**目标 branch 当前版本的完整 UTF-8 文件内容**，即使文件很大，也优先考虑直接 whole-file replacement。

标准路径：

```text
明确目标 branch / PR
  -> 读取该 branch 的 live head SHA
  -> 从该 exact head 读取目标文件完整内容 + blob SHA
  -> 基于这份内容生成完整新文件
  -> 写入前再次确认 branch head 未漂移
  -> update_file(current blob SHA, complete new content)
  -> 立即 exact diff audit
  -> focused tests（需要时）
  -> full CI / review gate
```

### 硬性安全规则

1. **绝不能拿 `main` 的文件去覆盖 feature branch，反之亦然。**
2. 完整文件必须从**真正要写入的目标 branch 当前 head**读取。
3. 写入使用的 blob SHA 必须属于刚读取的那个目标文件版本。
4. 从读取到写入之间若 branch head 已变化，停止写入；重新 fetch head/file/blob，再重新构造修改。
5. whole-file replace 后必须立即做 exact diff audit；不能因为 API 返回 success 就认为修改正确。
6. 如果计划只是小范围修改，而 diff 出现大量无关 churn、异常删除、格式化全文件、import 大规模变化等，**拒绝该 commit，不继续往 CI 推**。
7. tests-first / RED provenance、focused tests、full CI、review/merge gate 等工程要求不因 whole-file replace 而降低。

## 3. Exact diff audit 是 whole-file replace 的主要保护层

单开发者模式下，whole-file replace 的主要风险不是 merge conflict，而是模型在重建完整文本时意外改变了无关内容。

因此写后至少核对：

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

除非该 churn 本来就在计划内，否则应视为失败并回滚/重做，而不是继续测试来“证明它也能编译”。

## 4. Stacked PR / 未完成工作仍可使用 whole-file replace

存在 stacked work 并不自动意味着必须使用 temporary writer。

只要满足：

- 从**该 stacked branch 的 live head**读取完整目标文件；
- 修改基于该版本构造；
- 写入前 head 未漂移；
- 写后 exact diff 只包含预期变化；

whole-file replacement 仍是可接受且通常更省资源的方案。

只有当**同一文件确实存在独立并发修改流**，或无法可靠拿到完整目标版本时，才需要升级到更复杂路径。

## 5. 何时不要直接 whole-file replace

以下情况优先不要直接整文件覆盖：

- connector 无法可靠返回完整目标文件，内容被截断或上下文不足；
- 文件/请求体大到无法安全构造或传输；
- 同一目标文件存在真实的独立并发开发；
- 目标是生成文件、二进制文件或不适合文本 replacement 的资源；
- 修改必须依赖 runner 本地工具做机械 transformation，且无法安全在 connector 侧表达；
- 必须在**落正式 source 前**先通过 compile/focused test 才允许生成目标 blob；
- exact diff 无法证明 whole-file reconstruction 没有无关 churn。

## 6. 备用路径顺序

遇到上面的例外时，按以下顺序升级复杂度，而不是直接跳到临时 Actions 基础设施：

```text
A. whole-file update_file + SHA guard + exact diff
        ↓ 不适用
B. Git Data API：create_blob / create_tree / create_commit / fast-forward ref
        ↓ 仍无法安全构造
C. temporary trusted writer
   - 简单 workflow
   - 复杂 patch logic 放独立脚本，不内嵌大量 YAML heredoc
   - fail-closed anchors
   - focused validation
   - 只采收目标 source blob/commit
        ↓ 仍不适用
D. 完整本地/Codex Git worktree + normal git patch workflow
```

**不要仅因为文件超过数千行就从 A 直接跳到 C。**

## 7. Temporary writer 的新定位

Temporary GitHub Actions writer 是**例外工具**，不是“大文件默认工具”。

如果必须使用：

- workflow 自身保持极简；
- Python/Kotlin transformation 放独立脚本；
- 不把大量 Python heredoc 嵌入 YAML；
- transformation 必须 fail closed；
- runner 必须验证 scope；
- writer GREEN 不能替代正式 PR CI；
- 临时 PR / workflow 不 merge 到产品 branch；
- 采收后立即关闭临时 PR，并用 compare audit 证明没有基础设施泄漏。

2026-08-22 的 R6 Global observation work 已实际证明：复杂内嵌 YAML writer 会带来 workflow parsing / trigger 排查成本，而将 patcher 独立成脚本后才恢复稳定。因此未来不要重复走这条弯路。

## 8. 新对话的默认决策规则

任何新的 ChatGPT / Codex 开发对话，在处理 GitHub connector 修改前，应按以下判断：

```text
这是 CampBoardGameHost 吗？
  -> 是：默认按单开发者模式

目标是普通 UTF-8 source/doc 文件吗？
  -> 是

能否从目标 branch 当前 head 可靠读取完整文件？
  -> 能：优先 whole-file replace

写入后能否做 exact diff audit？
  -> 能：继续正常 tests/CI/review

只有上述任一项不成立
  -> 才升级到 Git Data API / temporary writer / full worktree
```

新对话**不得因为历史上曾经使用过 trusted writer，就推断本项目的大文件必须通过 writer 修改**。

## 9. 与其他文档的关系

- `docs/README.md`：所有新开发任务的文档入口；应优先指向本文件。
- `docs/github_connector_large_file_editing_playbook.md`：保留 Git Data API / trusted writer 的详细安全知识，尤其适用于 fallback 路径。
- 本文件对本仓库的**默认路径选择**具有更高优先级：

> **single developer + exact target head/blob + reliable full content + exact diff audit => whole-file replacement is acceptable and normally preferred.**

产品架构、R6 rollout、tests-first 要求和 merge 授权规则不由本文件改变。