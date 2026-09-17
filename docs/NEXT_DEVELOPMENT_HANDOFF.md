# NEXT DEVELOPMENT HANDOFF — SDE-1 Unified StorytellerDecisionEngine Orchestration

> Updated: 2026-09-17 Australia/Sydney  
> Status: **CURRENT / begin SDE-1**  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. query live `main`, open PRs and current checks before editing.

Do not restart completed FN-BUNDLE/SDE-0 work.

## 1. Live continuation point

Completed immediately before this handoff:

- PR #143 — SDE-0 / BEGINNER strategic-robustness corpus + policy contract;
- squash-merged to `main` as `5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`;
- final validated PR head `7f5b0574b25fff52612cd8cbe74a26933987dae1`;
- final observed validation: R2 SUCCESS, CI / Android FAST / CI gate SUCCESS, FN-BUNDLE-3 calibration SUCCESS;
- final test-only classifier intentionally skipped full Android + debug APK.

After merge, roadmap status advanced to SDE-1 in docs-only commit `6f96837cdb122f05a8ee2e18ec5f784b2eb1fde9`.

Always query live `main` because this handoff may itself be followed by another docs-only commit.

## 2. Current objective

**SDE-1 — unified `StorytellerDecisionEngine` orchestration seam**

The goal is not to rewrite recommendation logic. The goal is to establish one thin typed orchestration owner that composes the authorities already present:

```text
canonical actual/effective state
→ rules-owned legal candidates
→ typed hypothetical propositions/effects
→ existing exact epistemic evaluator
→ strategic structural diagnostics
→ profile/phase policy boundary
→ selected result
→ existing session/flow commit path
```

The engine must not own role legality, canonical mutation, night ordering, or world solving.

## 3. First task — SDE-1A fanout audit

Before creating `StorytellerDecisionEngine`, inspect the live code and map the real call graph for:

1. legal candidate generation;
2. `InformationProposition` / observation construction;
3. exact hypothetical evaluation entry points;
4. first-night bundle recommendation/coordinator callers;
5. dynamic recommendation callers;
6. session decision identity/revision/commit ownership;
7. flow ordering and UI invocation;
8. `ConsequenceEvaluator` callers and the context signals they consume.

For each node classify it as:

```text
REUSE AS AUTHORITY
ADAPT INTO SDE SEAM
LEGACY CALLER TO MIGRATE LATER
DUPLICATE / RETIRE AFTER CUTOVER
OUT OF SCOPE
```

Do not delete production paths during this audit unless they are independently proven dead.

## 4. Audit questions that must be answered

The next implementation plan should answer, with concrete symbols/files:

- Which current type should provide canonical actual/effective state to the engine?
- Which existing candidate producers already expose legal outcomes in typed form?
- Which decisions still require legacy text/display-option adaptation?
- What is the narrowest exact evaluator API the orchestrator can consume?
- Which exact diagnostics are already sufficient to carry SDE-0 structural evidence forward?
- What existing session identity/revision type should be reused for decision freshness?
- Where should lifecycle metadata live without shadowing session state?
- Which callers currently depend on `ConsequenceEvaluator` conclusions versus genuine context inputs?
- What is the smallest vertical slice that proves the new orchestration seam without production cutover?

## 5. Expected SDE-1 typed seam

Names are provisional; reuse existing types wherever possible.

Likely conceptual boundary:

```text
StorytellerDecisionEngine
    decide(request, context)

DecisionContext
    canonical/effective state reference
    phase / round / alive-state context
    table/player policy profile
    committed decision history reference
    lifecycle metadata

DecisionRequest
    decision kind
    legal candidates or legal-candidate provider
    recipient / interaction identity

CandidateConsequence
    exact BEFORE / AFTER consequence
    possible Demon seats
    evil-team configurations
    forced-good / forced-evil seats
    evil cover
    leave-one-out / recovery evidence where relevant

StorytellerPolicyResult
    acceptable/rejected/uncertain classification
    selected candidate when appropriate
    reason/provenance for diagnostics and manual Experienced-mode review
```

Do not freeze these exact class names before auditing existing types.

## 6. Lifecycle contract to establish in SDE-1

SDE-1 must make the ownership distinction expressible:

```text
PERSISTENT
    setup-level decisions that survive replanning

COMMITTED
    already shown/executed decisions; immutable

PLANNED / UNCOMMITTED
    recommendations that may be invalidated/re-evaluated
```

SDE-1 only establishes the seam and ownership semantics.

Actual Poisoner-triggered invalidation/replanning belongs to SDE-2.

## 7. Recommended first vertical slice

Choose one already-typed, bounded decision surface with:

- canonical legal candidates;
- an existing exact hypothetical evaluation path;
- no need to add Drunk/Spy/Recluse/Poisoner uncertainty;
- easy comparison against existing behavior;
- limited production fanout.

The slice should prove:

```text
existing legal candidates
→ SDE orchestration
→ exact diagnostics
→ policy result
```

without making the new engine the global production authority yet.

The exact slice must be chosen from the fanout audit, not guessed from role names in advance.

## 8. Testing expectations

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For SDE-1:

- audit documentation does not require manufactured RED tests;
- new stable orchestration types require focused typed tests;
- any semantic change to exact evaluation requires epistemic/oracle validation;
- first integration slice should have differential/contract evidence against the current authoritative path where useful;
- central caller cutovers require broader T2/T4 validation;
- expensive calibration experiments remain explicit experiments, not default FAST workload.

Do not resurrect low-value source-grep / reflection / field-echo tests removed in the SDE-0 cleanup.

## 9. SDE-1 non-goals

Do not implement yet:

- final BEGINNER numeric Badness gates unless supported by reviewed corpus evidence;
- Drunk hidden-role uncertainty;
- Spy/Recluse per-interaction policy selection;
- Poisoner re-planning;
- later-night information pacing;
- full production recommendation cutover;
- deletion of `ConsequenceEvaluator` before safe caller migration;
- NORMAL/EXPERT numeric policy profiles;
- a second possible-world solver;
- a shadow `GameState`.

## 10. SDE-0 contracts that must survive

Preserve:

- healthy `PUBLIC_GOOD_INFO` is not Storyteller confirmation;
- evil public speakers retain bluff worlds;
- healthy good public claims must match shown role and mechanically true clue in the current stress model;
- strict mechanically known `ShownRoleAt` remains exact;
- raw role-world cardinality is descriptive, not the primary product objective;
- possible Demon seats / evil-team topology / forced alignment / evil cover are primary structural evidence;
- whole-bundle interactions matter;
- calibration and holdout remain separated;
- no opaque global scalar becomes the decision authority.

## 11. Later stages

```text
SDE-2
    Drunk
    → Spy/Recluse per-interaction registration
    → Poisoner effective-state invalidation / re-planning

SDE-3
    cross-night impaired / registration decisions
    historical exact replay
    measured information pacing

SDE-4
    production cutover
    Experienced-mode manual override preservation
    ConsequenceEvaluator retirement
    stale heuristic-state cleanup
```

Do not pull later-stage complexity into SDE-1 merely because the eventual engine will support it.

## 12. ConsequenceEvaluator migration rule

`recommendation/dynamic/ConsequenceEvaluator` is migration-era legacy.

During SDE-1A:

- audit every caller;
- separate genuine context inputs from heuristic conclusions;
- preserve useful inputs for future `DecisionContext`;
- do not add new policy logic to `ConsequenceEvaluator`;
- do not delete it until the new seam covers its unique production obligations.

## 13. First implementation deliverables

The first SDE-1 PR should ideally contain only what is necessary to establish the seam cleanly:

1. fanout/ownership audit document;
2. typed orchestration boundary at the true package owner;
3. focused tests for that boundary;
4. one bounded vertical integration slice if the audit supports it;
5. no broad production cutover;
6. updated roadmap/handoff with measured follow-up work.

If the fanout audit shows the seam should be split into separate PRs, prefer smaller ownership-preserving PRs over one large migration.

## 14. Stable handoff

> **SDE-0 is merged. Begin SDE-1 by auditing live candidate/evaluator/session/flow fanout, then formalize the smallest thin `StorytellerDecisionEngine` orchestration seam that reuses canonical rules and exact epistemic consequences. Establish persistent/committed/planned ownership without implementing SDE-2 uncertainty yet. Do not create parallel state, rules, or world-solving authority, and do not perform global production cutover during the first SDE-1 slice.**
