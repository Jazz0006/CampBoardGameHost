# CampBoardGameHost AI Development Instructions

> Role: **NORMATIVE / PROJECT-LEVEL AI WORKING AGREEMENT**  
> Effective: 2026-09-23  
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
  = implementation and review through the configured repository workspace
  = remote PR / CI / merge-gate review

Mini MCP
  = default repository workspace when configured and available
  = bounded repository reads and searches
  = revision-guarded targeted writes
  = local git status / diff inspection
  = allow-listed task execution and long-running job lifecycle

GitHub / GitHub connector
  = canonical remote state and history
  = PR / CI / merge-gate operations
  = independent remote acceptance surface
  = fallback repository writer when Mini MCP is unavailable or unsuitable

Codex / Luna
  = exceptional constrained fallback for work that genuinely requires a different execution environment or broader mechanical/local tooling
```

Architecture, decomposition boundaries, product semantics, test strategy, remote audit and checkpoint acceptance are made in Chat unless the user explicitly delegates a decision to Codex/Luna.

Mini MCP is a controlled repository workspace / execution substrate, not an independent architecture or product-decision owner. Its availability changes how repository work is performed; it does not transfer semantic, scope, acceptance, or merge authority away from Chat and the user.

Codex/Luna must not independently redesign a requested slice, broaden scope, substitute a different architecture, perform remote PR review, or choose a different semantic implementation merely because it is easier locally.

### Repository workspace rule

- When the configured Mini MCP repository alias is available, ChatGPT **MUST normally use Mini MCP as the default repository read/search/edit/diff/status path**.
- Prefer `create_file`, `apply_patch`, or `apply_patches` over whole-file replacement. For existing-file edits, use the returned expected revision, exact semantic anchors, unique-match requirements, and fail closed on stale or ambiguous state.
- After Mini MCP edits, inspect local `git_diff` and `git_status` before treating the slice as ready for validation or remote acceptance.
- **File size alone MUST NOT determine the writer or execution path.** A large file may remain on the primary Mini MCP path when the intended change can be represented safely through bounded exact edits.
- GitHub Connector remains the fallback writer when Mini MCP is unavailable or unsuitable and remains the normal GitHub-native control-plane surface.
- The GitHub Actions one-shot patch workflow remains a valid exceptional remote fallback, not the automatic first choice merely because a file is large or connector output would be truncated.
- Codex/Luna **MUST NOT** be used merely because it is already involved in the task or because a file is large. Use it only when the primary controlled workspace and GitHub fallbacks cannot safely or practically perform the work.
- Mini MCP **owns the guarded local Git write path** for this repository when the live runtime exposes the reviewed safe-Git tools: state review -> explicit-path stage -> staged diff review -> commit -> network-observed remote audit -> push. GitHub Connector remains the canonical PR / CI / review / merge control plane, and merge still requires explicit user authorization.

## 2. Execution-path priority

Choose the simplest safe path that preserves repository-state, architecture, testing, diff-review, and merge-governance invariants.

### Path A — Chat + Mini MCP repository workspace

Use this by default when the configured `clocktower` repository alias is available.

Workflow:

```text
Chat live-state / architecture / scope audit
-> Mini MCP repo_info / read / search
-> identify the true owner and required evidence
-> Mini MCP create_file / revision-guarded targeted patch
-> Mini MCP git_diff / git_status
-> configured focused validation when the execution target supports it
-> broader checkpoint validation according to TESTING_STRATEGY
-> Mini MCP guarded stage / staged-diff review / commit / remote audit / push
-> GitHub remote parent / diff / CI / PR audit
```

Use exact repository-relative paths and stable semantic anchors. Do not patch by absolute line number. Do not silently relax stale-revision, zero-match, or multiple-match failures into fuzzy edits.

Mini MCP task execution is evidence only when the configured execution target actually supports the required toolchain and the requested task completes successfully. The current Oracle ARM64 workspace must not be reported as having locally validated Android work when the required Android SDK/toolchain is unavailable. GitHub CI remains the independent Android acceptance path until a reviewed supported build host is configured.

When a genuine behavior gap requires test-first development, preserve real RED provenance when the current development plan requires a distinct RED. Do not manufacture a RED for a refactor or intermediate implementation step merely to satisfy process ceremony.

### Path B — GitHub Connector fallback / GitHub-native operations

Use GitHub Connector when:

- Mini MCP is unavailable in the current conversation/runtime;
- the configured repository alias is unavailable;
- GitHub-native remote state or control-plane operations are required;
- a direct remote edit is simpler while still preserving complete safe read/write, exact diff review, and stale-state protection;
- bootstrap or recovery specifically requires the remote repository rather than the local workspace.

GitHub remains the canonical source for remote branch/PR state, CI/checks, mergeability, reviews, and merge actions.

### Path C — GitHub Actions one-shot exceptional patch

Use `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` only as an exceptional remote fallback when:

- Mini MCP cannot safely or practically express the required edit;
- the Mini MCP runtime is unavailable;
- execution must occur in a GitHub-hosted environment;
- an exact-head/blob-locked remote mutation is specifically useful.

The one-shot path must retain its existing fail-closed safety invariants: exact branch HEAD, target-file blob SHA, stable unique anchors, changed-file allowlist, semantic assertions, required test/diff evidence, remote-head recheck, and cleanup of temporary workflow/script files.

Large-file handling is therefore **capability-based, not file-size-based**.

### Path D — Codex/Luna exceptional fallback

Use Codex/Luna only when the controlled Chat + Mini MCP / GitHub paths are insufficient, including genuinely broad mechanical work, a required different local environment, or tooling that cannot be exposed safely through the configured workspace.

In this path Chat must provide a deterministic implementation task containing, normally:

- target branch and exact expected live HEAD;
- file allowlist;
- exact replacements/insertions/deletions;
- the evidence required for the change: focused RED/GREEN when applicable, or baseline/characterization/compile/diff validation for non-behavioral changes;
- checkpoint-level broader test only when the slice is the logical checkpoint;
- `git diff --check`;
- exact commit message and push target when commit/push is authorized through that path;
- explicit stop/report conditions.

Every Luna instruction **MUST be one continuous fenced code block** suitable for one paste. Use exact language (`replace`, `insert`, `delete`, `run`, `commit`). Do not use implementation-choice language such as `推荐结构`, `建议`, `例如可以`, or `大致如下`.

If the specified patch cannot apply because the live API/signature differs materially, Luna must stop and report the conflict rather than invent an equivalent implementation.

The current Mini MCP capability boundary is defined by this root agreement and the live configured tool surface. Older connector/Luna workflow documents remain useful only where they do not conflict with this root agreement.

## 3. Behavior-first, risk-based development and validation cadence

### 3.1 Core rule: evidence first, not RED ceremony

The repository uses **risk-based test-first development**, not “a new RED test for every production edit.”

The objective is to protect stable behavior, regressions, invariants, and architectural boundaries with the cheapest reliable evidence. A production change does **not** automatically require a newly created failing test.

Before choosing tests, first classify both the **change type** and the **ownership boundary**. Test strategy follows architecture; it must not be chosen independently of it.

Use this implementation pre-flight:

1. What kind of change is this: bug fix, new/changed behavior, behavior-preserving refactor, UI/presentation-only change, architecture/ownership change, or mechanical move/rename?
2. Which module is the authoritative owner of the behavior or invariant being protected?
3. What is the narrowest durable callable seam that proves the intended contract?
4. Does useful existing coverage already protect that contract?
5. If no durable callable seam exists, is that because the architecture genuinely lacks one, or only because the current implementation is coupled?
6. What is the cheapest reliable evidence for this slice: RED/GREEN, existing baseline, typed characterization, integration test, compile/static check, architecture guard, or exact diff audit?

### 3.1.1 Shared-contract / fan-out gate

Before changing a shared model, presentation contract, renderer, projector, persistence DTO, domain result, or any other fan-out seam, the implementation pre-flight **MUST** also map the full production fan-out:

1. Find every production producer, constructor, adapter, mapper, and direct builder of the changed contract.
2. Find every production consumer of the changed field or behavior.
3. Classify each path as **must inherit** or **intentionally exempt**; every exemption needs an explicit reason.
4. Prefer fixing the common semantic / projection / ownership boundary over caller-specific patches.
5. If callers bypass the intended common projection, migrate them to it or explicitly document and test why separate construction remains necessary.
6. Verify every relevant product phase or mode (for example Setup / Day / Night, beginner / expert, restored / fresh) rather than only the screen that exposed the issue.
7. Re-run the producer/consumer search after implementation so no direct path silently relies on a default value, stale adapter, or incomplete projection.

A shared renderer does **not** imply shared ownership. **"Shared renderer without shared projection" is an architecture smell** when callers independently assemble incomplete semantic state. Move the derivation toward the authoritative shared projection/owner instead of teaching each screen how to reconstruct it.

For cross-cutting UI state, visual state and interaction eligibility are separate contracts unless the domain explicitly couples them. A shared visual marker must not silently become a rule-level disabled/selectable decision.

The default evidence mapping is:

```text
bug fix / new stable behavior / changed stable behavior
  -> smallest durable typed test first
  -> meaningful RED when executable
  -> implementation
  -> GREEN

behavior-preserving refactor / ownership extraction
  -> identify existing owning tests
  -> establish GREEN baseline when useful
  -> add durable characterization only if a real behavior gap exists
  -> refactor
  -> rerun focused evidence + compile/static/diff checks

UI/presentation-only change
  -> test durable interaction/presentation behavior when valuable
  -> otherwise use focused compile/static/UI evidence and exact diff review
  -> do not invent domain REDs for visual restructuring

mechanical move / rename / formatting
  -> existing baseline + compile/static/diff evidence
  -> no manufactured RED

new architectural seam that becomes a durable contract
  -> test the seam at its true ownership boundary
  -> do not require every intermediate extraction step to have an independent RED
```

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

### 3.2 Test-value and ownership rule

Before adding a test, ask:

> If the implementation is substantially refactored later but the intended behavior remains correct, should this test still pass and still be valuable?

Also ask:

> Is this test attached to the module that should own the contract, or is it reaching across layers because the current architecture is coupled?

If the first answer is no, the proposed test is probably protecting implementation shape rather than a durable contract. If the second answer reveals cross-layer reach-through, prefer moving proof toward the true owner rather than cementing the accidental dependency.

Prefer a higher-value typed behavior/integration test, an explicit architecture guard, compile/static validation, or exact diff audit over incidental source-shape assertions.

**Do not introduce a production seam, helper, adapter, visibility expansion, or abstraction solely to satisfy a process requirement for a new RED.** Introduce seams because they improve ownership/testability of a durable contract, not because every intermediate edit needs an independently failing test.

A seam introduced for architectural reasons may become a useful test boundary. In that case, test the resulting stable contract once it exists; do not redesign production code around an artificial test seam that has no ownership value.

### 3.3 Existing coverage can be test-first evidence

“Test first” does not mean “new test first.” If an existing test already protects the intended behavior, that test predates the production change and is valid test-first evidence.

For a behavior-preserving refactor the preferred cycle is:

```text
identify existing owning tests / characterization
-> identify the intended post-refactor owner
-> confirm baseline when needed
-> add durable characterization only for uncovered stable behavior
-> refactor toward the intended owner/seam
-> run affected tests / compile checks
-> retire or narrow superseded source-shape tests
-> exact diff and invariant audit
```

Do not deliberately break or rewrite a correct test just to manufacture RED provenance.

For decomposition work, the owning test surface should normally move **downward toward the extracted responsibility**, not upward into a broader Host/screen source test. A successful extraction should reduce the amount of production source shape that tests need to know.

### 3.4 Test retirement is allowed and expected

The suite is a maintained engineering asset, not an append-only archive. Tests **MAY and SHOULD** be deleted or narrowed when they are demonstrably low-value, superseded, duplicated, or coupled only to an obsolete implementation path.

A test retirement must satisfy all applicable conditions:

- identify what behavior/invariant the test originally protected;
- confirm that the behavior is no longer required **or** is protected by a more stable test/evidence layer;
- ensure the removed assertion is not the only regression proof for a real product contract;
- delete obsolete production scaffolding that existed only to satisfy that test when safe and in scope;
- run the affected suite after retirement.

When a refactor creates a typed seam that proves the same behavior more directly, migrate coverage to that seam and retire source-string or cross-layer assertions that only protect the old ownership shape.

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

### AI-first architecture guardrails

The primary optimization target is **local reasoning**: a developer or agent should be able to make a normal feature change by understanding a small, cohesive set of files rather than loading a broad application surface. File count and line count are secondary signals. Track the practical **change context radius**: how much code and how many owners must be understood to safely change one feature.

Before adding substantial production behavior, perform a short architecture pre-flight:

1. Which existing module owns this behavior?
2. Which mutable state does it read or change, and who is the authoritative owner of each state value?
3. Does it perform domain/rules decisions, presentation preparation, rendering, persistence/I/O, telemetry, or another side effect?
4. What is the narrowest stable input/result boundary for the new behavior?
5. Does the proposed change add a new responsibility to an already broad file, screen, Host, controller, ViewModel, session object, or repository?
6. Can the change be implemented while preserving a clear dependency direction and keeping the normal change context radius small?

If the answers expose mixed ownership or a new responsibility, establish the boundary before expanding the feature implementation.

#### Recorded architecture pre-flight gate

For any substantial production edit that touches a protected/core handwritten file or a handwritten production source above roughly 1000 LOC, the architecture pre-flight **MUST be recorded before production editing begins**. The active implementation specification, audit, roadmap, or handoff must contain at least:

```text
Architecture pre-flight:
- current owner:
- proposed responsibility:
- authoritative state owner(s):
- narrow typed input/output seam:
- keep in current owner / extract:
- reason:
```

If this record is absent, substantial production editing is a stop condition until the pre-flight is completed. This is an execution gate, not a requirement to create a permanent standalone document for every micro-slice. Small bug fixes, mechanical edits, and already-approved implementation continuations do not require a new record unless they introduce or move responsibility.

#### Soft complexity triggers

These are **audit triggers, not automatic failure thresholds**:

- roughly 400–700 LOC in a handwritten production file: check whether responsibilities are still cohesive;
- above roughly 700 LOC: before adding a substantial feature, perform a decomposition/ownership check;
- above roughly 1000 LOC: do not add a new responsibility by default; justify why the existing owner is still correct;
- a function around 80–120+ LOC: check whether it mixes state read, decision, effects, and rendering;
- a function/composable with roughly 8–10+ parameters: check whether ownership or the input contract is unclear;
- an interaction surface with roughly 5–6+ callbacks: check whether an interaction/event boundary is missing;
- a core file growing by about 30% over a short feature campaign: perform a fresh architecture audit before further growth.

Do not mechanically split code to satisfy these numbers. A cohesive 900-line owner may be healthier than ten 90-line files that must all be opened for one change.

#### Ownership and dependency rules

- Every mutable state value must have one authoritative owner. Other layers may observe or request changes; they must not become competing sources of truth.
- Prefer the conceptual flow `state/input -> domain or decision operation -> result -> side effects -> rendering` over callbacks that directly mutate several states, call services, update revisions, persist data, and render results in one block.
- UI may decide **how** to render a legal result. UI must not become the authority for domain legality, rules semantics, candidate legality, transaction validity, or persistence identity.
- New modules should depend on narrow data/interfaces or typed inputs/results, not on the whole Host, screen, controller, ViewModel, session object, or a giant shared context.
- A parameter object is acceptable only when it represents a real cohesive concept. Replacing 30 parameters with a 30-field `Context`, `State`, `Args`, or `Environment` object is not decomposition.
- Do not widen `private -> internal -> public` solely because extracted code cannot otherwise reach an implementation detail. First ask whether a narrower input/result boundary should exist.

#### Forbidden false-decomposition patterns

Agents must not treat any of the following as architectural success by themselves:

- moving code into `Utils`, `Helpers`, `Manager`, `Common`, `Misc`, or similarly vague dumping-ground modules;
- replacing a long parameter list with a God Context/God State object;
- moving a function to another file while it still depends on the entire former owner;
- creating one file per tiny function/model when a feature then requires understanding many files at once;
- extracting UI components that still receive the same broad domain/service dependency surface as the original screen;
- increasing visibility or adding adapters whose only purpose is to satisfy a source-shape test or a file-size target.

New `Utils`, `Helpers`, `Manager`, `Common`, or broad `Context` modules require explicit ownership justification.

#### Abstraction discipline

Avoid both duplication panic and premature generalization.

- First similar implementation: note the pattern.
- Second similar implementation: compare semantics and lifecycle; duplication may still be cheaper than a wrong abstraction.
- Extract a shared abstraction when the common ownership/contract is stable, not merely because two blocks look syntactically similar.
- Prefer domain- or responsibility-named abstractions such as `PairInformationLegalDomain`, `NightCheckpointReducer`, or `InformationDecisionCoordinator` over generic helpers.

#### Generalization / anti-fixture policy rule

When a product rule or policy is conceptually shared across roles, scenarios, player counts, phases, or scripts, implementation **MUST** target the shared semantic/ownership boundary rather than encode the currently discussed example.

Before accepting a branch keyed to a named role, exact seat, fixture ID, known setup, or one regression scenario, ask:

1. Does this condition represent a genuine game-rule distinction?
2. Or is it compensating for a missing shared abstraction/policy owner?
3. Would another role/scenario with the same semantic shape inherit the behavior automatically?
4. Is the test proving a durable generic contract, or only the exact example that motivated the change?

If the behavior is policy-level rather than rules-level, prefer a shared typed model over named-role conditionals.

Examples in design discussions are **evidence and acceptance probes**, not permission to implement example-specific branches.

A change is incomplete if the implementation passes the motivating example but semantically equivalent cases still require separate patches.

#### Interaction and callback discipline

A growing callback list is an ownership signal. If an interaction needs many independent `onSelectX`, `onConfirmX`, `onChangeY`, and `onCancelZ` callbacks, check whether a cohesive interaction owner and a typed intent/result contract should exist.

Do not overcorrect by creating one application-wide `AppIntent` or event object containing every possible action. Intent/event types should follow the smallest cohesive interaction owner.

UI-local transient state should remain with the lowest cohesive UI owner. Do not hoist drawer/overlay/expanded/temporary selection state into application/session state merely to centralize state.

#### Change-scope discipline

For risky or architecture-sensitive work, keep the main dimension of change explicit:

- behavior/product change;
- architecture/ownership change;
- mechanical move/rename/formatting.

Do not casually combine all three in one slice. A feature PR may contain necessary structural work, but unrelated cleanup, broad renaming, and speculative abstraction should be split out when they enlarge the review or regression surface.

#### Architecture maintenance cadence

After roughly 5–10 feature PRs in the same subsystem, or when a protected/core file grows materially, perform a lightweight architecture audit. Check at least:

- largest handwritten production files;
- functions with unusually large parameter/callback surfaces;
- high fan-in/fan-out or broad dependency surfaces;
- shared mutable state and unclear authoritative owners;
- cross-layer dependencies and UI-owned domain decisions;
- duplicated semantic interpretation in different layers;
- source-string tests that may now protect obsolete implementation shape;
- visibility expansion caused by extraction;
- God Context / Utils / Helpers / Manager growth;
- practical change context radius for common feature changes.

The audit may conclude that no decomposition is needed. Its purpose is to catch ownership drift before a file becomes the default destination for unrelated future work.

#### AI precedent and architecture-document drift

AI agents tend to copy existing repository patterns. A large or poorly-owned file is **not** automatically an approved pattern merely because similar code already exists. When extending an unhealthy legacy area, prefer the intended architectural direction documented here and in current architecture/handoff documents instead of cloning the legacy shape.

Keep architecture documentation lightweight and current. If a current `ARCHITECTURE.md`, roadmap, handoff, or ownership document disagrees materially with live code, report the drift and resolve it rather than silently following a stale document or treating the current code accident as the intended architecture.

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

After every push through any authorized path, ChatGPT must independently verify GitHub actual state:

```text
expected parent
branch / PR head
changed-file allowlist
exact semantic diff
no unrelated churn
relevant test evidence
CI only when current cadence says CI is a gate
```

Mini MCP local `git_status` / `git_diff`, Codex/Luna reports, workflow logs, and user-reported local results are implementation evidence; none is the canonical remote source of truth. GitHub actual branch/PR state and required CI/R2 remain the independent remote acceptance surface.

A pushed commit is not merge authorization.

**Never merge, mark ready, force-push, rebase, or broaden the active PR without explicit user authorization.**

## 8. Current project documents and precedence

Read these when relevant:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — current execution authority;
2. newest `docs/NEXT_DEVELOPMENT_HANDOFF_*.md` for the active campaign;
3. `docs/TESTING_STRATEGY.md` — authoritative test tiers, evidence model, and subsystem mapping;
4. `docs/MINI_MCP_DEVELOPMENT_WORKFLOW_ADOPTION_AUDIT_2026-09-23.md` — Mini MCP migration rationale and current capability boundary;
5. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` — older Chat/connector/Luna workflow guidance, subordinate where it conflicts with this root agreement;
6. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` — exceptional remote one-shot patch SOP;
7. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md` — known failure patterns and proven improvements;
8. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md` — current same-night product/architecture decisions;
9. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` — source-string debt and retirement triggers;
10. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` and `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — historical/specialized guidance only where non-conflicting.

If documents disagree, apply this precedence:

1. newest explicit user instruction;
2. this root `AGENTS.md`;
3. `docs/TESTING_STRATEGY.md` for test-tier and evidence definitions;
4. current roadmap/handoff for active-state specifics;
5. `docs/MINI_MCP_DEVELOPMENT_WORKFLOW_ADOPTION_AUDIT_2026-09-23.md` for the current Mini MCP capability boundary where not already incorporated here;
6. older workflow and campaign documents only where non-conflicting.

For repository work, query the configured Mini MCP workspace for current local state when available and re-query GitHub whenever canonical remote branch/PR/CI state matters. Correct stale or conflicting documentation instead of silently carrying the conflict forward.
