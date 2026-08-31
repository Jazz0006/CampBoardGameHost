# AI Development Workflow V2 — ChatGPT / GitHub Connector / Codex Luna

> Role: **NORMATIVE / DEVELOPMENT OPERATIONS**  
> Effective: 2026-08-27  
> Applies to: `Jazz0006/CampBoardGameHost`  
> Precedence: this document supersedes conflicting execution/testing defaults in `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`. `docs/TESTING_STRATEGY.md` remains authoritative for test-tier definitions.

## 1. Fixed division of responsibility

```text
ChatGPT / Chat
  = live GitHub audit
  = architecture / product semantics / slice boundaries
  = test design and RED ownership
  = small/medium-file implementation through GitHub connector
  = exact remote diff / parent / scope audit
  = CI and checkpoint acceptance

GitHub connector
  = default writer whenever the complete target file can be read and safely replaced

GitHub Actions one-shot + separate Python patch script
  = first fallback for large/truncated files when an exact localized remote patch is possible
  = complete checkout + exact HEAD/blob/anchor locking + tests/diff audit + self-cleanup

Codex / Luna
  = next fallback only when the one-shot remote patch cannot be made safe or the task genuinely requires a local worktree/tooling

GitHub
  = source of truth and independent remote validation surface
```

Luna must not be assigned architecture design, scope expansion, remote PR audit, merge decisions, or independent alternative implementations unless the user explicitly asks for that delegation.

## 2. Writer selection is mandatory, not advisory

Use the connector for tests, docs, and small/medium source files whenever it can read/write them safely.

For a large/truncated file where whole-file connector replacement is unsafe, first ask whether the edit can be expressed as stable unique semantic anchors against an exact file blob. If yes, **use the GitHub Actions one-shot + separate Python patch script path before Luna**.

Use Luna only when at least one of these is true:

- the one-shot patch cannot be made fail-closed with stable unique anchors;
- the change requires a complete local worktree or broad mechanical multi-region edit;
- local-only build/test inputs or tooling are required;
- repeated failures demonstrate that the GitHub runner environment cannot perform the required operation safely.

Do not send work to Luna merely because Luna is already involved in the slice or because the target file is large.

The normative large-file procedure, including YAML/Python quoting, LF/newline preservation, exact anchor counts, workflow startup failures, GitHub Actions bot-push behavior and self-cleanup, is `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.

## 3. Tests-first micro-cycle

For a normal narrow behavior slice:

```text
ChatGPT creates RED
→ Luna runs the exact T0 RED only when local execution is required
→ expected assertion/JUnit failure = RED PASS
→ implement exact production patch
→ run exact T0 GREEN with --rerun-tasks
→ git diff --check
→ commit + push
→ ChatGPT audits canonical remote parent/diff/scope
→ continue next micro-slice
```

Rules:

- A RED instruction must state that the expected JUnit/assertion failure is success for the RED phase and execution should continue.
- Do not treat `UP-TO-DATE` or `FROM-CACHE` as proof of execution when a real test run is required.
- If Luna already reports the requested focused test with `--rerun-tasks` and `BUILD SUCCESSFUL`, ChatGPT does **not** rerun the identical command merely to duplicate evidence.
- Independent verification comes from remote parent/diff/scope audit and checkpoint CI, not repeated identical local runs.

## 4. Broad-test and CI cadence

`docs/TESTING_STRATEGY.md` defines T0/T1/T2/T3/T4. Operational cadence is:

```text
micro-slice RED/GREEN      → T0 only
several related slices     → continue after remote diff audit; do not wait old-head CI
logical checkpoint         → T1 + triggered T2/T3 as required
latest checkpoint head     → GitHub CI/R2 gate
merge                       → required full PR/merge gates
```

Do **not** automatically run `testFast`, `testDebugUnitTest`, `assembleDebug`, or wait for full GitHub CI after every tiny behavior patch.

### GitHub CI execution policy

GitHub CI now separates ordinary PR iteration from full acceptance checkpoints:

```text
ordinary PR synchronize commit
  → classify only previous PR head .. current PR head
  → Android-relevant change: :app:testFast
  → exact/oracle semantic change: additionally run the selected semantic gate

logical checkpoint
  → put [full-ci] in the checkpoint commit message
  → :app:testFull + :app:assembleDebug
  → ASP contracts + Real Clingo
  → CI gate

workflow_dispatch
  → full gate

push to main
  → full gate

.github/workflows/** or Gradle/build configuration change
  → full Android validation immediately; workflow routing changes also select every gate
```

The `[full-ci]` marker is an explicit acceptance escalation, not a replacement for focused RED/GREEN proof. Once a `[full-ci]` checkpoint is pushed, do not immediately push another micro-commit while its acceptance run is still needed: PR concurrency intentionally cancels older runs.

For `pull_request` `synchronize` events, CI must not classify the accumulated `main → PR head` diff, because that makes every later micro-commit inherit all earlier expensive validation triggers. Initial/opened/reopened PR validation may use the PR base because no prior PR head exists.

Documentation-only commits remain lightweight and do not invalidate a previously accepted code checkpoint merely by creating a newer docs head.

Escalate a single slice earlier when it changes persistence/schema, transaction boundaries, shared projector/chronology, build/Gradle/CI configuration, or when focused tests cannot establish sufficient confidence.

## 5. Luna instruction contract

Luna is the fallback after the large-file one-shot path, not the default large-file writer. Before assigning a large/truncated-file edit to Luna, record why `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` cannot safely perform the edit.

Every Luna instruction must be one continuous fenced code block and should normally contain only:

1. target branch and exact expected HEAD;
2. exact file allowlist;
3. exact replacements/insertions;
4. required focused test(s), plus checkpoint test only if this is the checkpoint;
5. `git diff --check`;
6. exact commit message and push target;
7. concise result fields to return.

Use deterministic language: `replace`, `insert`, `delete`, `run`, `commit`.

Do not use implementation-choice language such as `推荐结构`, `建议`, `例如可以`, or `大致如下`.

If the specified code cannot mechanically apply because the live API/signature differs materially, Luna must stop and report the conflict rather than invent an equivalent design.

## 6. Source-wiring test quality

Source-level wiring tests are allowed when runtime seams are impractical, but they must verify semantics rather than formatting.

Prefer:

- unique function/block anchors;
- several independent structural tokens;
- explicit absence checks for forbidden legacy paths.

Avoid:

- ambiguous first-occurrence `substringAfter` anchors;
- exact whitespace/line-break matching;
- whole formatted call strings;
- changing correct production formatting or adding meaningless comments solely to satisfy a brittle string test.

If one source-wiring test contains several instances of the same brittle assumption, fix the whole test once rather than waiting for sequential failures.

## 7. Remote acceptance

After every Luna push, ChatGPT independently verifies:

```text
expected parent
branch/PR head
changed-file allowlist
exact semantic diff
no unrelated churn
relevant test evidence
CI only when the current cadence says CI is a gate
```

A pushed commit is never merge authorization. Never merge, mark ready, force-push, rebase, or broaden the PR without explicit user authorization.

## 8. Precedence rule

When workflow documents disagree:

1. newest explicit user instruction;
2. root `AGENTS.md`;
3. this V2 workflow;
4. `docs/TESTING_STRATEGY.md` for tier definitions and subsystem mapping;
5. current roadmap/handoff for the active slice;
6. older workflow documents only where they do not conflict.

The disagreement must then be corrected in the repository instead of being silently carried forward.
