# CampBoardGameHost AI Development Instructions

> Role: **NORMATIVE / PROJECT-LEVEL AI WORKING AGREEMENT**  
> Effective: 2026-08-27  
> Applies to: ChatGPT, Codex/Luna, and other AI development agents working on this repository.

## 1. Decision authority and division of work

The default collaboration model is:

```text
ChatGPT / Chat
  = live-state audit
  = architecture and design decisions
  = scope and slice boundaries
  = invariant / regression-risk analysis
  = tests-first or characterization strategy
  = implementation specification
  = small/medium-file implementation through GitHub connector
  = remote diff / CI / merge-gate review

GitHub connector
  = default direct writer whenever the target file can be read and updated safely

Codex / Luna
  = constrained file-level implementation and local-validation executor for edits that are unsafe or inefficient through the connector

GitHub
  = source of truth and independent remote validation surface
```

Architecture, decomposition boundaries, product semantics, test strategy, remote audit and checkpoint acceptance are made in Chat unless the user explicitly delegates a decision to Codex/Luna.

Codex/Luna must not independently redesign a requested slice, broaden scope, substitute a different architecture, perform remote PR review, or choose a different semantic implementation merely because it is easier locally.

### Mandatory writer rule

- Tests, docs, and small/medium source files **MUST** be edited by ChatGPT through the GitHub connector when complete safe read/write is available.
- Codex/Luna **MUST NOT** be used merely because it is already involved in the task.
- Codex/Luna is the fallback for large/truncated files, complete-worktree mechanical edits, or required local-only validation.

## 2. Execution-path priority

Choose the simplest safe path for each file.

### Path A — GitHub connector direct implementation

Use this by default when:

- the target file is small/medium and can be read completely and reliably;
- the required edit is localized or can be expressed safely as a complete-file update;
- exact diff review remains practical;
- local-only tooling is not required to perform the edit safely.

Workflow:

```text
Chat audit / decision
-> connector RED/test or production edit
-> exact remote diff audit
-> local validation only if execution is required and unavailable in Chat
-> checkpoint CI only when the current test cadence says it is a gate
```

For tests-first work, preserve real RED provenance when required. Do not silently combine a behavior-changing test and its GREEN implementation if the current development plan requires a distinct RED.

### Path B — Codex/Luna local implementation

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
- required focused RED/GREEN command(s);
- checkpoint-level broader test only when the slice is the logical checkpoint;
- `git diff --check`;
- exact commit message and push target;
- explicit stop/report conditions.

Every Luna instruction **MUST be one continuous fenced code block** suitable for one paste. Use exact language (`replace`, `insert`, `delete`, `run`, `commit`). Do not use implementation-choice language such as `推荐结构`, `建议`, `例如可以`, or `大致如下`.

If the specified patch cannot apply because the live API/signature differs materially, Luna must stop and report the conflict rather than invent an equivalent implementation.

Detailed current operations are in `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`. The older `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` is historical guidance only where it does not conflict with V2.

## 3. Tests-first micro-cycle and validation cadence

### Micro-slice default

```text
ChatGPT creates RED
-> exact T0 RED is run when required
-> expected JUnit/assertion failure = RED PASS, continue
-> exact production patch
-> exact T0 GREEN with --rerun-tasks
-> git diff --check
-> commit + push
-> ChatGPT remote parent/diff/scope audit
-> continue next micro-slice
```

Requirements:

- Luna instructions must explicitly say when an expected JUnit/assertion failure counts as RED PASS and execution should continue.
- If Luna already ran the exact requested focused test with `--rerun-tasks` and reported `BUILD SUCCESSFUL`, ChatGPT **MUST NOT** rerun the identical command merely to duplicate evidence.
- Do not treat `UP-TO-DATE` or `FROM-CACHE` as proof that a required test executed.

### Broad-test and CI cadence

- T0 exact focused tests are the normal RED/GREEN feedback loop.
- `:app:testFast` is T1 and belongs at a logical checkpoint, not automatically after every small commit.
- At logical checkpoints, run T1 plus triggered T2/T3 validation as defined by `docs/TESTING_STRATEGY.md`.
- Related micro-slices may continue after focused GREEN + remote diff audit; **do not wait for old-head GitHub CI after every small push**.
- The latest logical checkpoint head gets the GitHub CI/R2 gate.
- Persistence/schema, transaction boundaries, shared projector/chronology, build/Gradle/CI configuration, or insufficient T0 coverage may justify earlier escalation.
- PR/full validation uses T4; `:app:testFull` is the complete Android JVM entry point and must preserve `:app:testDebugUnitTest` coverage.
- Local validation never replaces GitHub CI/R2 before merge.

The detailed tier and subsystem mapping is authoritative in `docs/TESTING_STRATEGY.md`.

In Codex/Luna sandboxed local worktrees, use task-local Gradle state when needed, e.g. `GRADLE_USER_HOME="$PWD/.gradle-codex"`.

## 4. Source-wiring test policy

Source-level wiring tests are temporary migration tools when a production boundary cannot yet be exercised through a callable typed seam. They are **not** the preferred long-lived proof of gameplay/rules behavior.

### Mandatory preference

When a callable typed seam exists or can reasonably be introduced, business/rules behavior **MUST** be tested through that seam instead of by reading production `.kt` source text.

Preferred proof order:

```text
typed pure/domain behavior
-> typed reducer/planner/session behavior
-> typed adapter/integration behavior
-> minimal architecture/ownership source guard only where runtime proof is impractical
```

### Temporary wiring tests

A source-string behavior/wiring test may remain only while it protects a unique production wiring gap that typed lower-layer tests cannot prove. It must have a clear retirement trigger.

When the corresponding production path is cut over to the typed seam, the superseded source-string assertion **MUST** be deleted or narrowed in the same campaign. Do not preserve a legacy helper, local variable name, inline expression, formatting, or call spelling merely to keep an obsolete source-string test GREEN.

If a deliberate typed-seam refactor makes a source-string test fail while the owning typed behavior tests remain GREEN, first assess whether the string assertion has been superseded. Do not automatically change correct production code to restore the old source shape.

### Long-lived architecture guards

Source inspection remains acceptable for explicit coarse architecture/ownership invariants, such as preventing App root from reclaiming an extracted responsibility. These tests should protect ownership boundaries rather than local variable names, exact whitespace, exact formatted calls, or incidental implementation order.

For any retained source-based guard:

Prefer:

- unique function/block anchors;
- multiple independent structural tokens;
- explicit absence checks for forbidden legacy ownership paths.

Avoid:

- ambiguous first textual occurrences;
- exact whitespace/line-break matching;
- complete formatted call strings;
- exact local variable names when a coarser ownership assertion is possible;
- changing correct production formatting or inserting meaningless comments solely to satisfy a source-string test.

If several assertions in one source-wiring test share the same brittle assumption, repair or retire the whole affected test section rather than discovering the same defect one assertion at a time.

Current retirement inventory and SNE-specific triggers are tracked in `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`.

## 5. Source-decomposition principle

File size is a maintainability signal, not an architecture by itself.

For Clocktower decomposition work:

- prefer handwritten production files at or below roughly 50 KiB when a natural ownership boundary supports it;
- do **not** introduce poor abstractions, giant parameter bags, state-lifetime changes, or unnecessary `internal` exposure merely to satisfy a byte threshold;
- cohesion, stable ownership, transaction ordering, and future feature isolation outrank the numeric size target;
- decomposition should stop when further extraction would increase coupling or regression risk more than it improves maintainability.

### Post-PR #43 Host growth rule

`ClocktowerHostScreen.kt` is a protected orchestration owner. It may remain substantially larger than 50 KiB, but it must not become the default destination for new feature implementation.

For new Clocktower work:

- new algorithms belong in domain / epistemic / history / recommendation / session owners as appropriate;
- new role/interaction presentation should prefer dedicated materializer/UI owners when a cohesive seam exists;
- new persistence/history/session behavior must not be embedded into Host merely because current state is available there;
- Host changes should normally be limited to derived orchestration state, phase routing, wiring and protected transaction/callback boundaries;
- if a feature would add hundreds of lines of new policy/UI/algorithm code to Host, stop and identify a natural owner before implementation.

This is a **growth freeze on new responsibility**, not a byte freeze and not a mandate to mechanically shrink the current file.

### App-root decomposition status

App-root decomposition S7.1/S7.2 is merged and the campaign is paused before later decomposition work while current rules-correctness work is active. Resume only after a fresh live-state audit and the current roadmap/handoff says to do so.

## 6. Protected architectural invariants

Unless a task explicitly changes product behavior, preserve:

- Blood on the Clocktower rule semantics and precedence;
- explicit documentation of any intentional product/house-rule deviation from official rules;
- recommendation ranking and selection ordering;
- rules legality as an upstream authority separate from recommendations;
- stable seat/interaction identity independent of filtered views;
- draft selection vs confirmed mechanical fact boundaries;
- registration semantics and impairment ordering;
- information-decision lifecycle;
- persistence/history identity;
- `ClocktowerGameSession` authority for global timeline identity/sequence;
- Compose state lifetime and effect lifetime;
- callback / audit / commit ordering in stateful transactions.

A structural refactor must not become a hidden product change.

## 7. Remote acceptance and merge governance

Before implementation, re-check live `main`, PR head, and target branch when the slice depends on live state.

After every Luna/user push, ChatGPT must independently verify GitHub actual state:

```text
expected parent
branch / PR head
changed-file allowlist
exact semantic diff
no unrelated churn
relevant test evidence
CI only when current cadence says CI is a gate
```

Luna's textual report is evidence of local execution, not the remote source of truth.

A pushed commit is not merge authorization.

**Never merge, mark ready, force-push, rebase, or broaden the active PR without explicit user authorization.**

## 8. Current project documents and precedence

Read these when relevant:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — current execution authority;
2. newest `docs/NEXT_DEVELOPMENT_HANDOFF_*.md` for the active campaign;
3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` — current Chat/connector/Luna execution contract;
4. `docs/TESTING_STRATEGY.md` — authoritative test tiers and subsystem mapping;
5. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md` — known failure patterns and proven improvements;
6. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md` — current same-night product/architecture decisions;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` — source-string debt and retirement triggers;
8. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` — connector workflow;
9. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — older local-worktree guidance, subordinate where it conflicts with V2.

If documents disagree, apply this precedence:

1. newest explicit user instruction;
2. this root `AGENTS.md`;
3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
4. `docs/TESTING_STRATEGY.md` for test-tier definitions;
5. current roadmap/handoff for active-state specifics;
6. older documents only where non-conflicting.

Re-query GitHub for live state, then correct stale/conflicting documentation instead of silently carrying the conflict forward.
