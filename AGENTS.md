# CampBoardGameHost AI Development Instructions

> Role: **NORMATIVE / PROJECT-LEVEL AI WORKING AGREEMENT**  
> Effective: 2026-08-30  
> Applies to: ChatGPT, Codex/Luna, and other AI development agents working on this repository.

## 1. Decision authority and division of work

The default collaboration model is:

```text
ChatGPT / Chat
  = live-state audit
  = architecture and design decisions
  = scope and slice boundaries
  = invariant / regression-risk analysis
  = behavior-first / characterization strategy
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
- For a large/truncated source file where whole-file connector replacement is unsafe but the change can be expressed as stable unique anchors, the **first fallback MUST be a GitHub Actions one-shot workflow + separate Python patch script** following `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.
- The one-shot path must fail closed on exact branch HEAD, target-file blob SHA, anchor occurrence count, changed-file allowlist, and required test/diff evidence; temporary workflow/script files must self-remove after the product commit.
- Codex/Luna **MUST NOT** be used merely because it is already involved in the task or because a file is large.
- Codex/Luna is the next fallback only when the one-shot remote patch cannot be made safe, repeatedly fails because of the execution environment, or the work genuinely requires a complete local worktree / broad mechanical edit / local-only tooling.

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
-> identify required evidence
-> connector test and/or production edit
-> exact remote diff audit
-> local validation only if execution is required and unavailable in Chat
-> checkpoint CI only when the current test cadence says it is a gate
```

When a genuine behavior gap requires test-first development, preserve real RED provenance when the current development plan requires a distinct RED. Do not manufacture a RED for a refactor or intermediate implementation step merely to satisfy process ceremony.

### Path B — GitHub Actions one-shot large-file patch

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

Detailed current operations are in `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`. The older `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` is historical guidance only where it does not conflict with V2.

## 3. Behavior-first, risk-based development and validation cadence

### 3.1 Core rule: evidence first, not RED ceremony

The repository uses **risk-based test-first development**, not “a new RED test for every production edit.”

The objective is to protect stable behavior, regressions, invariants, and architectural boundaries with the cheapest reliable evidence. A production change does **not** automatically require a newly created failing test.

A new test should normally be added before production implementation when the change introduces or modifies a stable contract, including:

- a bug fix that can be reproduced deterministically;
- new or changed gameplay/rules behavior;
- a new externally observable feature behavior;
- an algorithm, persistence, transaction, history, identity, concurrency, or recommendation invariant that is not already covered;
- a regression gap where existing tests would have allowed the defect.

For these cases the normal cycle remains:

```text
define stable behavior / invariant
-> create or modify the smallest durable typed test
-> exact T0 RED when the RED is meaningful and executable
-> production implementation
-> exact T0 GREEN
-> git diff --check
-> commit + push
-> ChatGPT remote parent/diff/scope audit
```

A **new RED is not required** merely because production source will change. In particular, do not create a new test solely for:

- internal refactoring with intentionally unchanged behavior;
- function/file extraction, movement, renaming, visibility adjustment, or decomposition;
- mechanical rewrites or dependency-neutral cleanup;
- an intermediate wiring step whose final behavior is already protected by a stable typed/integration test;
- temporary construction order or local implementation shape;
- making one implementation micro-step independently “test-first.”

For such changes, establish the relevant existing GREEN baseline when useful, make the change, then re-run the smallest affected evidence plus compile/static/diff checks as appropriate.

### 3.2 Test-value rule

Before adding a test, ask:

> If the implementation is substantially refactored later but the intended behavior remains correct, should this test still pass and still be valuable?

If the answer is no, the proposed test is probably protecting implementation shape rather than a durable contract. Prefer a higher-value typed behavior/integration test, an explicit architecture guard, compile/static validation, or exact diff audit instead.

**Do not introduce a production seam, helper, adapter, visibility expansion, or abstraction solely to satisfy a process requirement for a new RED.** Introduce seams because they improve ownership/testability of a durable contract, not because every intermediate edit needs an independently failing test.

### 3.3 Existing coverage can be test-first evidence

“Test first” does not mean “new test first.” If an existing test already protects the intended behavior, that test predates the production change and is valid test-first evidence.

For a behavior-preserving refactor the preferred cycle is:

```text
identify existing owning tests / characterization
-> confirm baseline when needed
-> refactor
-> run affected tests / compile checks
-> exact diff and invariant audit
```

Do not deliberately break or rewrite a correct test just to manufacture RED provenance.

### 3.4 Test retirement is allowed and expected

The suite is a maintained engineering asset, not an append-only archive. Tests **MAY and SHOULD** be deleted or narrowed when they are demonstrably low-value, superseded, duplicated, or coupled only to an obsolete implementation path.

A test retirement must satisfy all applicable conditions:

- identify what behavior/invariant the test originally protected;
- confirm that the behavior is no longer required **or** is protected by a more stable test/evidence layer;
- ensure the removed assertion is not the only regression proof for a real product contract;
- delete obsolete production scaffolding that existed only to satisfy that test when safe and in scope;
- run the affected suite after retirement.

Do not retain a test merely because it already exists, because historical test counts are expected to monotonically increase, or because deleting it would lower a numeric coverage count.

### 3.5 Broad-test and CI cadence

- T0 is the smallest directly relevant evidence. For genuine behavior gaps it is normally the RED/GREEN loop; for refactors it may be an existing characterization/contract test or compile/static check.
- `:app:testFast` is T1 and belongs at a logical checkpoint, not automatically after every small commit.
- At logical checkpoints, run T1 plus triggered T2/T3 validation as defined by `docs/TESTING_STRATEGY.md`.
- Related micro-slices may continue after focused GREEN/evidence + remote diff audit; **do not wait for old-head GitHub CI after every small push**.
- The latest logical checkpoint head gets the GitHub CI/R2 gate.
- Persistence/schema, transaction boundaries, shared projector/chronology, build/Gradle/CI configuration, or insufficient focused coverage may justify earlier escalation.
- PR/full validation uses T4; `:app:testFull` is the complete Android JVM entry point and must preserve all **currently intentional** Android JVM tests.
- Local validation never replaces GitHub CI/R2 before merge.

Luna instructions must explicitly say when an expected JUnit/assertion failure counts as RED PASS and execution should continue **only when a RED is actually required**. If Luna already ran the exact requested focused test with `--rerun-tasks` and reported `BUILD SUCCESSFUL`, ChatGPT **MUST NOT** rerun the identical command merely to duplicate evidence. Do not treat `UP-TO-DATE` or `FROM-CACHE` as proof that a required test executed.

The detailed tier and subsystem mapping is authoritative in `docs/TESTING_STRATEGY.md`.

In Codex/Luna sandboxed local worktrees, use task-local Gradle state when needed, e.g. `GRADLE_USER_HOME="$PWD/.gradle-codex"`.

## 4. Source-wiring test policy

Source-level tests are **not an ordinary test-first mechanism**. They are allowed only as explicit architecture/ownership guards or temporary migration tools when a production boundary cannot reasonably be exercised through a callable typed seam.

### Mandatory preference

When a callable typed seam exists or can reasonably be introduced for a durable contract, business/rules behavior **MUST** be tested through that seam instead of by reading production `.kt` source text.

Preferred proof order:

```text
typed pure/domain behavior
-> typed reducer/planner/session behavior
-> typed adapter/integration behavior
-> minimal architecture/ownership source guard only where runtime proof is impractical
```

### Temporary wiring tests

A source-string behavior/wiring test may remain only while it protects a unique production wiring gap that typed lower-layer tests cannot prove. It must have a clear retirement trigger.

A source-string test **MUST NOT** be created solely to force an intermediate production-wiring step through RED before GREEN.

When the corresponding production path is cut over to a typed/integration seam, the superseded source-string assertion **MUST** be deleted or narrowed in the same campaign. Do not preserve a legacy helper, local variable name, inline expression, formatting, or call spelling merely to keep an obsolete source-string test GREEN.

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

Current retirement inventory and SNE-specific triggers are tracked in `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`. The inventory should be periodically re-audited for tests that protect only obsolete intermediate source paths.

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
4. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` — normative large/truncated-file one-shot patch SOP;
5. `docs/TESTING_STRATEGY.md` — authoritative test tiers, evidence model, and subsystem mapping;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md` — known failure patterns and proven improvements;
7. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md` — current same-night product/architecture decisions;
8. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` — source-string debt and retirement triggers;
9. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` — connector workflow;
10. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — older local-worktree guidance, subordinate where it conflicts with V2.

If documents disagree, apply this precedence:

1. newest explicit user instruction;
2. this root `AGENTS.md`;
3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
4. `docs/TESTING_STRATEGY.md` for test-tier and evidence definitions;
5. current roadmap/handoff for active-state specifics;
6. older documents only where non-conflicting.

Re-query GitHub for live state, then correct stale/conflicting documentation instead of silently carrying the conflict forward.
