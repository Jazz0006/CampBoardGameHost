# CampBoardGameHost — Current AI Development Workflow

> Date: 2026-09-27 Australia/Sydney  
> Status: **ACTIVE / NORMATIVE DEVELOPMENT WORKFLOW**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Parent authority: root `AGENTS.md`  
> Test-tier authority: `docs/TESTING_STRATEGY.md`

## 1. Decision

The project returns to a **GitHub Connector-first development workflow** for ordinary repository and remote-control work.

Mini MCP is retained because it solves one important limitation of the GitHub Connector: safely working with large files or operations that require a complete local file/worktree rather than truncated connector content.

The stable split is:

~~~text
ChatGPT / Chat
  = architecture authority
  = product semantics / ownership / scope
  = implementation plan
  = test strategy / acceptance criteria
  = final diff and result analysis

GitHub Connector
  = default repository / remote interface
  = normal small/medium-file work
  = branch / PR / CI / R2 / review inspection
  = canonical remote-state audit
  = normal post-implementation test/result analysis

Mini MCP + Codex CLI
  = supplemental complete-local-context execution path
  = primarily large/truncated-file editing
  = optional local analysis when complete-file context is materially required

GitHub
  = canonical remote repository / PR / CI / review state
  = normal Android validation / acceptance surface
~~~

Mini MCP is **not** the default GitHub control plane for this project anymore.

The N3150 Android execution-host route is **retired from the normal workflow** because measured end-to-end test time is too slow to justify its operational complexity. Do not block normal development on synchronizing or running N3150.

## 2. Decision authority

Architecture, ownership, product semantics, scope, testing strategy and acceptance remain in ChatGPT unless the user explicitly delegates them.

Local model access does not transfer design authority.

### L2 — ChatGPT architecture authority

Always active.

ChatGPT decides:

- the real production owner;
- the behavior/invariant being changed;
- file and symbol scope;
- what must not change;
- RED/characterization requirements where applicable;
- acceptance criteria;
- whether a local full-context analysis is actually necessary.

### L1 — strong Codex local analysis, only when needed

Use only when a design decision genuinely depends on complete local context that ChatGPT cannot safely reconstruct from bounded connector reads.

L1 is **read-only analysis by default**.

It should return:

- relevant ownership/fanout map;
- exact code paths and interactions;
- constraints/invariants found in the complete file/worktree;
- proposed patch plan;
- risks / unresolved questions.

It must not silently implement the proposal.

The analysis returns to ChatGPT, which decides the actual implementation plan.

Use a stronger Codex model for this mode when necessary. Do not use cheap Luna as the default architecture designer merely because it can read the whole file.

### L0 — Luna mechanical execution, default local implementation mode

When ChatGPT has already fixed the implementation plan, Luna is the preferred low-cost local executor.

ChatGPT supplies:

- exact target branch / expected HEAD;
- file allowlist;
- target owner/symbols;
- required replacements/insertions/deletions or an otherwise deterministic implementation specification;
- forbidden changes;
- acceptance criteria;
- required diff/commit result.

Luna may:

- read the complete target file/worktree;
- perform the specified mechanical edit;
- make only implementation-local adjustments necessary to apply the approved design;
- inspect its diff;
- create the authorized commit through the controlled local path.

Luna must stop and report if the actual code makes the approved design ambiguous or materially incompatible. It must not choose a new architecture itself.

## 3. Default path — GitHub Connector

Use the GitHub Connector for ordinary work when complete-file local context is not required.

Typical work includes:

- live branch / PR / exact-head inspection;
- repository search and bounded code reads;
- small and medium source/test/doc edits when the target can be handled safely;
- PR discovery and state;
- CI / R2 / review-thread inspection;
- workflow/run log analysis;
- post-push remote diff/parent/scope audit;
- test-result interpretation and checkpoint acceptance.

GitHub itself remains canonical remote truth.

After any local Mini MCP/Codex commit reaches the remote branch, return to the GitHub Connector for normal remote-state and test/acceptance work.

## 4. Large / truncated file path

Use this path when the GitHub Connector cannot safely expose or edit the complete target file, especially large files such as `CampBoardGameHostApp.kt`.

Default sequence:

~~~text
ChatGPT
  audit owner / fanout / invariants
  design exact change
  define acceptance + forbidden scope

-> Mini MCP starts/controls local Codex execution

-> Luna L0
  read complete file
  mechanically implement approved plan
  inspect local diff
  commit through controlled local path

-> make the commit visible on the remote branch through the controlled local Git path when required

-> return to GitHub Connector

-> audit exact remote head / parent / changed-file scope / semantic diff
-> inspect CI / R2 / review threads
-> analyze failures or accept checkpoint
~~~

The local path exists to solve the **complete-file editing problem**, not to replace the rest of the development workflow.

## 5. When ChatGPT cannot safely design from connector context

Do not force a patch plan from incomplete context.

Use:

~~~text
ChatGPT defines the question and boundaries
-> Mini MCP / strong Codex L1 reads complete local context
-> L1 returns analysis only
-> ChatGPT reviews and chooses the design
-> Luna L0 implements mechanically
-> GitHub Connector resumes normal audit / CI / result analysis
~~~

This is preferred over allowing Luna to combine incomplete architectural reasoning and implementation in one step.

## 6. Testing and validation

Follow `docs/TESTING_STRATEGY.md` for T0–T4 definitions and subsystem triggers.

Current execution policy:

- **N3150 is not a normal validation target.**
- Do not spend development time synchronizing its checkout or waiting on it unless the user explicitly reopens that experiment.
- Oracle Mini MCP task execution is not Android acceptance when the required Android toolchain is unavailable.
- GitHub CI/R2 is the normal Android execution and independent acceptance surface.
- Connector-visible CI evidence remains the standard source for result analysis.
- User-run compatible local tests may be supplemental evidence, but are not required merely to satisfy the retired N3150 path.
- Do not weaken coverage because local Android execution is unavailable.

For a real behavior bug, preserve RED/GREEN evidence when the active slice requires it. The execution location may be GitHub CI/workflow rather than a local Android host.

Do not manufacture RED for documentation-only, behavior-preserving or purely structural edits.

## 7. Commit / push / remote handoff

For ordinary connector edits, use the established GitHub Connector workflow.

For Mini MCP/Codex large-file work:

1. verify exact local branch/HEAD before execution;
2. keep a strict changed-file allowlist;
3. inspect the complete local diff;
4. commit only approved paths;
5. use the controlled local Git path only as needed to make that exact commit visible remotely;
6. immediately return remote authority to GitHub / GitHub Connector;
7. independently verify exact remote head, parent, changed-file scope and semantic diff.

A local Codex/Mini MCP report is implementation evidence, not canonical remote truth.

## 8. Merge governance

No workflow path changes the acceptance requirements before merge.

Standing authorization effective 2026-09-29:

- once a task/PR is complete;
- its exact head has passed all required validation for that slice;
- GitHub reports the PR mergeable;
- the changed-file allowlist and semantic diff match the approved scope;
- required documentation is converged;
- and there are no unresolved review or correctness issues;

ChatGPT may mark the PR ready and merge it directly without asking for a separate per-PR authorization message.

This standing authorization does not waive acceptance gates and does not authorize merging incomplete, failing, conflicted, scope-drifted, or intentionally paused work.

Still require explicit user authorization for:

- rebase of an active integration branch when it rewrites history or changes the reviewed base materially;
- force-push;
- destructive history changes;
- broadening the approved PR integration strategy or scope.

## 9. Mini MCP capability boundary

Mini MCP remains valuable for:

- complete-file/local-worktree access;
- large-file mechanical execution through Codex CLI;
- controlled local diff / commit / push required by that execution;
- optional durable developer memory;
- exceptional local tooling unavailable through the connector.

Its available GitHub/CI tools may be used for recovery or a deliberate cross-check, but they are no longer the default project control plane.

Developer memory is advisory and optional. Use it only when durable prior context materially reduces repeated work; verify mutable facts against current code/docs/GitHub.

## 10. Deprecated paths

The following are no longer current defaults:

- Mini MCP-first repository + GitHub control plane;
- N3150 as the normal Android execution host;
- GitHub Actions + Python one-shot as the first response to every large connector-truncated file;
- Luna independently designing and implementing architecture-sensitive changes.

The GitHub Actions one-shot path remains an exceptional fallback when a local Mini MCP/Codex edit is unavailable or a locked remote mutation is uniquely safer.

## 11. Workflow selection summary

~~~text
Need ordinary repo / PR / CI work?
  -> GitHub Connector

Need a small/medium edit the connector can safely handle?
  -> GitHub Connector

Need to edit a large/truncated file but design is already clear?
  -> ChatGPT design
  -> Mini MCP + Luna L0 mechanical implementation
  -> controlled local commit/remote handoff
  -> GitHub Connector audit / CI / analysis

Need full local context before design is safe?
  -> ChatGPT defines question
  -> strong Codex L1 read-only analysis
  -> ChatGPT decides
  -> Luna L0 implements
  -> GitHub Connector resumes

Need Android validation?
  -> TESTING_STRATEGY tiers
  -> GitHub CI/R2 by default
  -> do not wait on N3150

Need merge?
  -> verify exact-head acceptance / scope / mergeability / docs
  -> merge directly when complete under standing authorization
  -> ask only if acceptance is incomplete, scope changed, or history-rewriting action is required
~~~

## 12. Supersession

This document supersedes conflicting execution-path defaults in:

- `archive/workflows/MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md`;
- `archive/workflows/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
- `LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`;
- archived connector/Luna workflow documents.

Those documents remain historical/reference material only where they do not conflict with this workflow or root `AGENTS.md`.
