# CampBoardGameHost — Mini MCP Development Workflow & Developer-Memory Adoption

> Date: 2026-09-25 Australia/Sydney  
> Status: **ACTIVE WORKFLOW AUTHORITY**  
> Repository alias: `clocktower`  
> Runtime: ChatGPT -> Secure MCP Tunnel -> always-on Oracle Mini MCP  
> Parent rules: root `AGENTS.md`

## 1. Decision

Mini MCP is now the default execution and GitHub-control interface for ordinary CampBoardGameHost development when its reviewed tool surface is available.

GitHub itself remains the canonical remote collaboration / PR / CI state. The normal interface used to inspect and mutate that state is now Mini MCP's guarded GitHub surface rather than the GitHub Connector.

The GitHub Connector remains a fallback for:

- Mini MCP runtime/tool exposure failure;
- a GitHub capability not yet represented by Mini MCP;
- bootstrap/recovery where the Oracle workspace itself is unavailable;
- an explicitly requested independent cross-check.

Do not switch to GitHub Connector merely because a task involves PRs, CI, reviews, Draft state or merge. Mini MCP now covers those ordinary control-plane operations.

## 2. Verified Mini MCP capability boundary

The current deployed tool surface includes:

### Repository read / search / mutation

- `repo_info`
- `read_file`
- `search_text`
- `create_file`
- `apply_patch`
- `apply_patches`
- `delete_file`

Edits are repository-alias bound and use server-enforced path and stale-revision safety. Prefer small exact edits over whole-file replacement.

### Git lifecycle

- `git_state`
- `git_status`
- `git_diff`
- `git_diff_cached`
- `git_fetch`
- `git_branches`
- `git_create_branch`
- `git_switch_branch`
- `git_track_remote_branch`
- `git_fast_forward`
- `git_stage`
- `git_commit`
- `git_remote_status`
- `git_push`

Normal synchronization is:

~~~text
fetch
-> inspect branch / remote relationship
-> guarded fast-forward when appropriate
~~~

Do not substitute an implicit `git pull` model.

Normal write path is:

~~~text
live state
-> bounded edit
-> git diff/status
-> validation
-> explicit-path stage
-> cached diff review
-> commit
-> remote-status audit
-> guarded push
~~~

### Controlled tasks / long-running validation

- `run_task`
- `start_task`
- `task_status`
- `task_result`

Use synchronous `run_task` for bounded tasks. Use the job lifecycle for validations whose expected duration/output makes a synchronous call unsuitable.

A task result is evidence only when the configured execution host actually supports the required toolchain. The Oracle ARM64 host must not be presented as Android-local acceptance when the Android SDK/toolchain is unavailable. GitHub CI/R2 remains the independent Android acceptance path unless a reviewed build host is added.

### GitHub PR / CI / review control plane

- `github_pr_discover`
- `github_pr_create_draft`
- `github_pr_audit`
- `github_ci_detail`
- `github_job_log`
- `github_pr_review_threads`
- `github_pr_mark_ready`
- `github_pr_convert_to_draft`
- `github_pr_merge`

These tools operate against configured repository identity and use server-owned credentials and stale-state guards.

For CampBoardGameHost:

- use Mini MCP GitHub tools as the normal PR/CI/review interface;
- keep the GitHub service itself as canonical remote truth;
- do not infer current remote state from local Git alone;
- merge still requires explicit user authorization;
- after an ambiguous remote write outcome, re-audit instead of blindly retrying.

## 3. Default high-efficiency development loop

For a substantive task, use this order unless the task clearly requires less:

~~~text
1. repo_info / git_state
2. bounded developer-memory search when prior durable context can materially change the work
3. read current project authorities
4. inspect exact production owner + fanout
5. fetch / remote / PR audit when live remote state matters
6. targeted read/search
7. tests-first or characterization pre-flight according to AGENTS / TESTING_STRATEGY
8. bounded patch/create/delete
9. diff/status
10. focused validation
11. broader checkpoint validation only when required
12. stage explicit paths
13. cached diff review
14. commit
15. remote-status audit
16. push
17. github_pr_audit / CI / review-thread drill-down
18. checkpoint memory decision: ADD / UPDATE / SUPERSEDE / NONE
~~~

Do not perform steps mechanically when they provide no value. A one-line read-only question does not need a full workflow, and documentation-only cleanup does not need invented Android RED tests.

## 4. Startup memory retrieval

Mini MCP developer memory is now part of the normal workflow, but only for substantive work where prior durable context can materially change navigation, architecture or execution.

At task start:

1. identify the repository and task intent;
2. run **one small bounded `memory_search`** using `repo: "clocktower"`;
3. use natural operational terms: owner/module/invariant/failure/workflow name, not generic "project context";
4. inspect returned summaries/triggers first;
5. call `memory_get` only for promising candidates;
6. verify mutable facts against live code, Git state, project documentation or GitHub state before acting.

With `repo: "clocktower"`, default search semantics intentionally include:

- project-scoped `clocktower` memories;
- global workflow memories;
- global developer engineering lessons.

Do not inject the whole memory corpus into context. Do not search memory for trivial one-step operations.

## 5. Authority precedence for memory

Memory is advisory navigation / experience, never project truth.

For mutable or normative facts, precedence is:

~~~text
explicit current user instruction
-> root AGENTS.md
-> current authoritative project docs
-> live code / Git / runtime / GitHub state as applicable
-> developer memory
-> historical/archive docs
~~~

A remembered branch, PR head, CI result, implementation detail or policy status must be rechecked live.

If an active memory conflicts with newer authoritative evidence:

- do not silently follow the memory;
- finish the immediate task from the authoritative source;
- then UPDATE or SUPERSEDE the stale memory at the checkpoint.

## 6. What is worth remembering

A normal substantive development session should produce **zero to three** durable memories. `NONE` is a healthy and common outcome.

Capture only facts that are likely to prevent meaningful repeated work later.

High-value project memories include:

### `repository_map`

Use for stable ownership/navigation facts.

Examples:

- which module owns a difficult invariant;
- where canonical history or persistence boundaries live;
- a non-obvious producer/consumer fanout that would otherwise require broad search next time.

Do not store a simple filename that is cheap to search unless the ownership relationship itself is non-obvious and durable.

### `architecture_decision`

Use for accepted decisions that materially constrain future implementations.

Good examples:

- one canonical owner was chosen over an attractive duplicate path;
- a tempting design was rejected for a durable reason;
- a versioning/compatibility boundary future work must preserve.

The authoritative decision should still live in repository documentation when it is important enough to be normative. Memory exists to make the decision easy to recover and to point to its evidence.

### `engineering_lesson`

Use for a hard-won lesson that is likely to recur.

Examples:

- a specific architecture smell repeatedly caused incomplete fanout;
- a test strategy repeatedly produced low-value source-shape tests;
- a failure only appeared because a particular owner or lifecycle boundary was misunderstood.

Prefer causal lessons over "command X failed once".

### `technical_debt`

Use only when the debt has:

- a concrete location/owner;
- why it is intentionally retained;
- a future trigger for removal/revisit.

A vague TODO is not a durable memory.

### `workflow_rule`

Normally global `workflow` scope, not project scope.

Examples:

- Mini MCP is the normal GitHub control plane; connector is fallback;
- schema/tool-manifest changes require runtime restart / client refresh, while ordinary project aliases/config-only changes may not;
- Android acceptance remains remote while the configured Oracle host lacks the toolchain.

### `user_correction`

Use for explicit durable engineering corrections from the project owner that would materially change future work and are not already better represented as a normative repository rule.

Do not use developer memory as a personal-profile store.

## 7. Project vs developer vs workflow scope

Use `project` + `repo: "clocktower"` when the fact belongs specifically to CampBoardGameHost.

Use `developer` only for cross-project engineering lessons that genuinely generalize.

Use `workflow` only for durable tool/environment/process facts independent of a single repository.

Do not copy the same fact into all three scopes. Prefer the narrowest correct scope.

## 8. Capture quality contract

Every memory should be concise and operational.

A good record contains:

- a short summary stating the durable fact;
- detail explaining the reason/consequence, not a transcript;
- 1–12 searchable operational triggers;
- evidence references where useful;
- a confidence appropriate to the evidence.

Useful evidence types include:

- repository path;
- commit;
- pull request;
- runtime observation;
- user statement;
- external reference.

Evidence should point to **why the memory exists**. Do not embed full logs, files or conversation transcripts.

Prefer triggers such as:

- `DecisionTrace replay owner`
- `role-function exposure severity`
- `source-string test retirement`
- `Android Oracle toolchain`
- `Red Herring truth danger`

over vague tags such as:

- `SDE`
- `Kotlin`
- `important`

## 9. ADD / UPDATE / SUPERSEDE / NONE

At a meaningful checkpoint, make exactly one decision per candidate fact.

### ADD

Use when a durable fact is new and not already represented.

Search first when duplication is plausible.

### UPDATE

Use when the identity/scope/kind is still correct but the durable content has evolved.

Read the current record and use its exact version. Stale-version writes must fail closed.

### SUPERSEDE

Use when the old memory is conceptually replaced, especially when:

- its meaning has materially changed;
- its scope/repository/kind identity is no longer correct;
- a prior architecture decision has been replaced by a new one.

Supersession preserves history and removes the old row from normal active search.

### NONE

Use when:

- the information is transient;
- authoritative docs already make it cheap to recover;
- it is ordinary commit/test/CI output;
- it is a one-off debugging detail;
- it has no likely future trigger;
- recording it would mostly duplicate prose.

## 10. Never store these as developer memory

Do not persist:

- passwords, PATs, tokens, private keys, cookies or raw env files;
- personal/private information unrelated to engineering;
- full tool logs or conversation transcripts;
- ordinary branch HEAD / current PR status / CI run numbers as durable memory;
- every commit, every test result, or every file touched;
- speculative conclusions not yet accepted;
- large copied sections of authoritative docs;
- facts that can be recovered more safely with one cheap live command.

## 11. Recommended CampBoardGameHost memory classes

During normal SDE work, prioritize these durable classes:

1. **ownership maps** — non-obvious canonical owners and fanout;
2. **architecture decisions** — accepted cross-slice boundaries and rejected alternatives;
3. **future cleanup triggers** — intentionally retained compatibility code and the exact condition for retiring it;
4. **repeated failure lessons** — mistakes that caused multiple rounds of rework;
5. **user corrections** — explicit corrections to algorithm/architecture assumptions that future work could otherwise repeat;
6. **workflow lessons** — cross-project Mini MCP / CI / build-host behavior.

Do not record current SDE phase status as memory merely because it is important today. Roadmap/handoff own current phase status.

## 12. Memory and documentation division

Use documentation for:

- normative architecture;
- current roadmap;
- active handoff;
- acceptance criteria;
- evidence contracts;
- decisions future contributors must read even without the memory service.

Use developer memory for:

- fast recall of hard-won facts;
- locating the right owner quickly;
- preserving causal engineering lessons;
- remembering technical-debt triggers;
- carrying explicit corrections between conversations;
- avoiding repeated broad repository archaeology.

A useful rule:

> If the project would become incorrect when the memory database disappeared, the fact belongs in repository documentation/code/tests first.

## 13. GitHub / merge governance after Mini MCP adoption

Mini MCP's GitHub tools do not change human authority.

Before a merge decision:

- audit exact PR identity/head/base;
- inspect required current checks/workflows;
- inspect unresolved review threads when relevant;
- drill into failed CI only when bound to the exact current PR head;
- re-audit after any potentially ambiguous remote operation.

For this project:

> **Never merge, mark ready, force-push, rebase, or broaden the active PR without explicit project-owner authorization.**

The existence of `github_pr_merge` does not imply permission to call it.

## 14. Adoption target for this repository

CampBoardGameHost participates in Mini MCP M9D's real developer-memory canary.

Use memory naturally during real work; do not manufacture rows to hit a quota.

Desired evidence from this project includes:

- useful recalls that avoid repeated repository archaeology;
- stale memories detected and corrected;
- durable user corrections carried into later work;
- cross-project developer/workflow lessons reused;
- lexical misses that show whether SQLite/FTS retrieval is insufficient;
- memories that were never useful, which should inform stricter capture.

Do not optimize for memory count. Optimize for fewer repeated mistakes and faster recovery of durable context.

## 15. Current recommended new-session pattern

For a new CampBoardGameHost development conversation:

~~~text
repo_info(clocktower)
+ git_state(clocktower)
+ one bounded memory_search(clocktower, task-specific query)
+ current AGENTS / roadmap / handoff
+ live PR audit when a PR is active
-> continue the exact current slice
~~~

This replaces the older pattern of loading a large stack of historical slice documents at startup.

Historical audits should be read only when the live task identifies a specific ownership/evidence question that needs them.
