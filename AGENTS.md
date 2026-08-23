# CampBoardGameHost AI Development Instructions

> Role: **NORMATIVE / PROJECT-LEVEL AI WORKING AGREEMENT**  
> Effective: 2026-08-23  
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
  = remote diff / CI / merge-gate review

GitHub connector
  = preferred direct writer when the target files can be read and updated safely

Codex / Luna
  = constrained implementation and local-validation executor for edits that are unsafe or inefficient through the connector

GitHub
  = source of truth and independent remote validation surface
```

Architecture, decomposition boundaries, product semantics, and risk decisions are made in Chat unless the user explicitly delegates a decision to Codex/Luna.

Codex/Luna should not independently redesign a requested slice, broaden scope, or substitute a different architecture merely because it is easier to implement locally.

## 2. Execution-path priority

Choose the simplest safe path for each task.

### Path A — GitHub connector direct implementation

Use this by default when:

- the target files are small/medium and can be read completely and reliably;
- the required edit is localized or can be expressed safely as a complete-file update;
- exact diff review remains practical;
- local-only tooling is not required to establish correctness.

Workflow:

```text
Chat audit / decision
-> connector edit
-> exact remote diff audit
-> focused GitHub checks / CI as appropriate
-> continue only after the expected gate is understood
```

For tests-first work, preserve real RED provenance when required. Do not silently combine a behavior-changing test and its GREEN implementation if the current development plan requires a distinct RED.

### Path B — Codex/Luna local implementation

Use this when:

- connector output is truncated or incomplete;
- a large source file makes whole-file replacement risky;
- declaration extraction or large mechanical movement needs a complete worktree;
- several strongly coupled files must be edited together;
- Android/Gradle/Clingo/Python local validation is materially safer or faster;
- the task needs a real local diff before commit.

In this path, Chat must provide a precise implementation task including:

- repository / branch / expected live head;
- allowed files;
- exact declarations or source region to change;
- invariants and forbidden adjacent changes;
- required RED/GREEN behavior where applicable;
- focused and full validation commands;
- commit/push contract;
- explicit stop point.

Codex/Luna executes that specification, runs the requested local validation, commits/pushes only when permitted, and stops. Chat then re-reads GitHub and performs the remote audit.

Detailed local-worktree rules are in `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`.

## 3. Source-decomposition principle

File size is a maintainability signal, not an architecture by itself.

For Clocktower decomposition work:

- prefer handwritten production files at or below roughly 50 KiB when a natural ownership boundary supports it;
- do **not** introduce poor abstractions, giant parameter bags, state-lifetime changes, or unnecessary `internal` exposure merely to satisfy a byte threshold;
- cohesion, stable ownership, transaction ordering, and future feature isolation outrank the numeric size target;
- decomposition should stop when further extraction would increase coupling or regression risk more than it improves maintainability.

### Post-PR #43 Host growth rule

`ClocktowerHostScreen.kt` is now a protected orchestration owner. It may remain substantially larger than 50 KiB, but it must not become the default destination for new feature implementation.

For new Clocktower work:

- new algorithms belong in domain / epistemic / history / recommendation / session owners as appropriate;
- new role/interaction presentation should prefer dedicated materializer/UI owners when a cohesive seam exists;
- new persistence/history/session behavior must not be embedded into Host merely because current state is available there;
- Host changes should normally be limited to derived orchestration state, phase routing, wiring and protected transaction/callback boundaries;
- if a feature would add hundreds of lines of new policy/UI/algorithm code to Host, stop and identify a natural owner before implementation.

This is a **growth freeze on new responsibility**, not a byte freeze and not a mandate to mechanically shrink the current file.

### Next large-file priority after PR #43

The next structural task is **App-root decomposition of `CampBoardGameHostApp.kt`**, before A3 historical multi-night product development. Current large-file order of concern is:

```text
CampBoardGameHostApp.kt   ~325 KiB   next structural priority
ClocktowerHostScreen.kt   ~296 KiB   first high-value decomposition complete; growth-frozen
ClocktowerDayScreen.kt     ~63 KiB   audit only; split only if a natural low-coupling owner exists
```

The detailed current plan and stop criteria live in `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

## 4. Protected architectural invariants

Unless a task explicitly changes product behavior, preserve:

- Blood on the Clocktower rule semantics and precedence;
- recommendation ranking and selection ordering;
- registration semantics and impairment ordering;
- information-decision lifecycle;
- persistence/history identity;
- `ClocktowerGameSession` authority for global timeline identity/sequence;
- Compose state lifetime and effect lifetime;
- callback / audit / commit ordering in stateful transactions.

A structural refactor must not become a hidden product change.

## 5. Validation and merge governance

- Re-check live `main`, PR head, and target branch before implementation.
- Use characterization/ownership contracts for structural moves where useful.
- Use tests-first with a real RED for behavior changes when the roadmap requires it.
- Require exact diff audit; unrelated formatting churn is not acceptable.
- Local validation does not replace GitHub CI/R2 before merge.
- A pushed commit is not merge authorization.
- **Never merge, mark ready, force-push, rebase, or broaden the active PR without explicit user authorization.**

## 6. Current project documents

Read these when relevant:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — current execution authority;
2. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md` — next task after PR #43 merge;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23_POST_A13.md` — PR #43 completion/final-review evidence;
4. `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` — connector workflow;
5. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` — large-file / local execution workflow.

If these documents disagree about current state, re-query GitHub and prefer the most recent explicit user decision plus the current roadmap; then correct stale documentation before proceeding.
