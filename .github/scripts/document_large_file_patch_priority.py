from pathlib import Path

AGENTS = Path("AGENTS.md")
WORKFLOW = Path("docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")
    return raw.decode("utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


agents = read_lf(AGENTS)

agents = replace_once(
    agents,
    """- Tests, docs, and small/medium source files **MUST** be edited by ChatGPT through the GitHub connector when complete safe read/write is available.
- Codex/Luna **MUST NOT** be used merely because it is already involved in the task.
- Codex/Luna is the fallback for large/truncated files, complete-worktree mechanical edits, or required local-only validation.
""",
    """- Tests, docs, and small/medium source files **MUST** be edited by ChatGPT through the GitHub connector when complete safe read/write is available.
- For a large/truncated source file where whole-file connector replacement is unsafe but the change can be expressed as stable unique anchors, the **first fallback MUST be a GitHub Actions one-shot workflow + separate Python patch script** following `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.
- The one-shot path must fail closed on exact branch HEAD, target-file blob SHA, anchor occurrence count, changed-file allowlist, and required test/diff evidence; temporary workflow/script files must self-remove after the product commit.
- Codex/Luna **MUST NOT** be used merely because it is already involved in the task or because a file is large.
- Codex/Luna is the next fallback only when the one-shot remote patch cannot be made safe, repeatedly fails because of the execution environment, or the work genuinely requires a complete local worktree / broad mechanical edit / local-only tooling.
""",
    "AGENTS mandatory writer rule",
)

old_path_b = """### Path B — Codex/Luna local implementation

Use this when:

- connector output is truncated or incomplete;
- a large source file makes whole-file replacement risky;
- declaration extraction or large mechanical movement needs a complete worktree;
- several strongly coupled large files must be edited together;
- Android/Gradle/Clingo/Python local execution is required;
- the task needs a real local diff before commit.

In this path Chat must provide a deterministic implementation task containing, normally:

- target branch and exact expected live HEAD;
- file allowlist;
- exact replacements/insertions/deletions;
- the evidence required for the change: focused RED/GREEN when applicable, or baseline/characterization/compile/diff validation for non-behavioral changes;
- checkpoint-level broader test only when the slice is the logical checkpoint;
- `git diff --check`;
- exact commit message and push target;
- explicit stop/report conditions.

Every Luna instruction **MUST be one continuous fenced code block** suitable for one paste. Use exact language (`replace`, `insert`, `delete`, `run`, `commit`). Do not use implementation-choice language such as `推荐结构`, `建议`, `例如可以`, or `大致如下`.

If the specified patch cannot apply because the live API/signature differs materially, Luna must stop and report the conflict rather than invent an equivalent implementation.
"""

new_path_b = """### Path B — GitHub Actions one-shot large-file patch

Use this as the **first fallback for large/truncated files** when:

- connector output is truncated or whole-file replacement is risky;
- the intended edit is localized and can be expressed with stable unique semantic anchors;
- a complete GitHub runner checkout can safely compile/test/audit the result;
- the patch can be locked to exact branch HEAD and target-file blob SHA.

Required pattern:

```text
connector writes/updates small test if needed
-> connector writes temporary .github/scripts/<slice>_patch.py
-> connector writes temporary .github/workflows/<slice>-one-shot.yml
-> workflow checks exact HEAD + remote HEAD + target/test blob SHA
-> focused RED/baseline when applicable
-> separate Python script applies exact count==1 anchor replacement
-> focused GREEN / checkpoint tests
-> git diff --check + exact changed-file allowlist + semantic assertions
-> remote-head recheck
-> product commit/push
-> workflow removes its own temporary workflow/script in cleanup commit
-> connector/user-authored checkpoint commit supplies normal PR CI/R2, and [full-ci] when T4 is required
```

Do not patch by absolute line number. Do not silently relax a zero/multiple-match anchor to fuzzy matching. Non-trivial Python patch bodies must live in the separate `.py` file rather than being embedded inside YAML.

Detailed quoting, LF/newline, anchor-count, failure-triage, bot-push and cleanup rules are normative in `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.

### Path C — Codex/Luna local implementation

Use this only when Path B cannot be made safe or genuinely cannot perform the work, including when:

- the large-file change cannot be expressed as stable unique anchors;
- declaration extraction or broad mechanical movement needs a complete worktree;
- several strongly coupled large files must be edited together;
- local-only tooling/input is required;
- a complete local diff is necessary before the patch can be specified deterministically.

In this path Chat must provide a deterministic implementation task containing, normally:

- target branch and exact expected live HEAD;
- file allowlist;
- exact replacements/insertions/deletions;
- the evidence required for the change: focused RED/GREEN when applicable, or baseline/characterization/compile/diff validation for non-behavioral changes;
- checkpoint-level broader test only when the slice is the logical checkpoint;
- `git diff --check`;
- exact commit message and push target;
- explicit stop/report conditions.

Every Luna instruction **MUST be one continuous fenced code block** suitable for one paste. Use exact language (`replace`, `insert`, `delete`, `run`, `commit`). Do not use implementation-choice language such as `推荐结构`, `建议`, `例如可以`, or `大致如下`.

If the specified patch cannot apply because the live API/signature differs materially, Luna must stop and report the conflict rather than invent an equivalent implementation.
"""

agents = replace_once(agents, old_path_b, new_path_b, "AGENTS Path B/C")

agents = replace_once(
    agents,
    """3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` — current Chat/connector/Luna execution contract;
4. `docs/TESTING_STRATEGY.md` — authoritative test tiers, evidence model, and subsystem mapping;
5. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md` — known failure patterns and proven improvements;
6. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md` — current same-night product/architecture decisions;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` — source-string debt and retirement triggers;
8. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` — connector workflow;
9. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — older local-worktree guidance, subordinate where it conflicts with V2.
""",
    """3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` — current Chat/connector/Luna execution contract;
4. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` — normative large/truncated-file one-shot patch SOP;
5. `docs/TESTING_STRATEGY.md` — authoritative test tiers, evidence model, and subsystem mapping;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md` — known failure patterns and proven improvements;
7. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md` — current same-night product/architecture decisions;
8. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` — source-string debt and retirement triggers;
9. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` — connector workflow;
10. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — older local-worktree guidance, subordinate where it conflicts with V2.
""",
    "AGENTS document list",
)

AGENTS.write_text(agents, encoding="utf-8", newline="\n")

workflow = read_lf(WORKFLOW)

workflow = replace_once(
    workflow,
    """GitHub connector
  = default writer whenever the complete target file can be read and safely replaced

Codex / Luna
  = file-level implementation executor only when connector editing is unsafe or inefficient
  = primarily large/truncated files and local Gradle execution
""",
    """GitHub connector
  = default writer whenever the complete target file can be read and safely replaced

GitHub Actions one-shot + separate Python patch script
  = first fallback for large/truncated files when an exact localized remote patch is possible
  = complete checkout + exact HEAD/blob/anchor locking + tests/diff audit + self-cleanup

Codex / Luna
  = next fallback only when the one-shot remote patch cannot be made safe or the task genuinely requires a local worktree/tooling
""",
    "V2 responsibility block",
)

workflow = replace_once(
    workflow,
    """Use Luna only when at least one of these is true:

- the file is large/truncated and whole-file replacement is unsafe;
- the change requires a complete local worktree or mechanical multi-region edit;
- local-only build/test tooling is needed to implement the specified patch safely.

Do not send small-file work to Luna merely because Luna is already involved in the slice.
""",
    """For a large/truncated file where whole-file connector replacement is unsafe, first ask whether the edit can be expressed as stable unique semantic anchors against an exact file blob. If yes, **use the GitHub Actions one-shot + separate Python patch script path before Luna**.

Use Luna only when at least one of these is true:

- the one-shot patch cannot be made fail-closed with stable unique anchors;
- the change requires a complete local worktree or broad mechanical multi-region edit;
- local-only build/test inputs or tooling are required;
- repeated failures demonstrate that the GitHub runner environment cannot perform the required operation safely.

Do not send work to Luna merely because Luna is already involved in the slice or because the target file is large.

The normative large-file procedure, including YAML/Python quoting, LF/newline preservation, exact anchor counts, workflow startup failures, GitHub Actions bot-push behavior and self-cleanup, is `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.
""",
    "V2 writer selection",
)

workflow = replace_once(
    workflow,
    """## 5. Luna instruction contract

Every Luna instruction must be one continuous fenced code block and should normally contain only:
""",
    """## 5. Luna instruction contract

Luna is the fallback after the large-file one-shot path, not the default large-file writer. Before assigning a large/truncated-file edit to Luna, record why `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` cannot safely perform the edit.

Every Luna instruction must be one continuous fenced code block and should normally contain only:
""",
    "V2 Luna fallback rule",
)

WORKFLOW.write_text(workflow, encoding="utf-8", newline="\n")
