# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

Completed foundation relevant to the current program:

```text
D6 decomposition / ownership cleanup                  COMPLETE
Beginner / Experienced host modes                     COMPLETE
Same-night effective-state / transaction foundation   COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 candidate-space + pair ownership           COMPLETE / PR #139
FN-BUNDLE-1 proposition / ShownRoleAt semantics        COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle exact harness         COMPLETE / PR #142
SDE-0 BEGINNER strategic-robustness corpus             COMPLETE / PR #143
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

PR #143 final validated head before merge:

`7f5b0574b25fff52612cd8cbe74a26933987dae1`

Observed final validation on that head:

- R2 main-thread boundary — SUCCESS;
- CI / Android FAST / CI gate — SUCCESS;
- FN-BUNDLE-3 calibration experiment — SUCCESS;
- full Android + debug APK was not selected because the final cleanup diff was test-only, not because of failure.

Always query live `main` before executable edits.

## 2. CURRENT

**SDE-1 — unified `StorytellerDecisionEngine` orchestration seam**

The next engineering objective is to establish one thin shared orchestration owner that composes existing authorities without recreating them.

Target flow:

```text
canonical GameState / interaction-time effective state
        ↓
rules-owned legal candidates
        ↓
InformationProposition / hypothetical effects
        ↓
existing exact epistemic evaluator
        ↓
strategic structural diagnostics
        ↓
profile / phase policy boundary
        ↓
selection result
        ↓
existing session / flow commit ownership
```

`StorytellerDecisionEngine` must be orchestration, not a second rules engine, second state model, second flow owner, or second possible-world solver.

## 3. NEXT

```text
SDE-2 — Drunk -> Spy/Recluse -> Poisoner first-night uncertainty
SDE-3 — cross-night impaired / registration decisions
SDE-4 — production cutover + legacy heuristic retirement
```

## 4. SDE-1 implementation order

### SDE-1A — fanout / seam audit

Before adding the engine class, map the current real ownership and call graph for:

- canonical first-night and dynamic legal candidate generators;
- `InformationProposition` / observation materialization;
- exact hypothetical evaluator entry points;
- current recommendation coordinator / policy callers;
- session commit / revision identity;
- flow ordering / host UI invocation;
- existing `ConsequenceEvaluator` callers and context inputs.

The audit must identify reusable owners and duplicate responsibilities. It must not reopen completed FN-BUNDLE-0/1/2 candidate-space audits unless a concrete regression is found.

### SDE-1B — thin typed contracts

Define the smallest durable orchestration contracts required to express a decision without copying canonical state.

Expected concepts include, subject to the audit:

```text
StorytellerDecisionEngine
DecisionContext
DecisionRequest / DecisionKind
LegalDecisionCandidate
CandidateConsequence / exact diagnostics
StorytellerPolicyResult
DecisionLifecycle = PERSISTENT | COMMITTED | PLANNED
```

Names may change if existing types already own the concept. Prefer reuse over parallel models.

`DecisionContext` should reference or project canonical state; it must not become a shadow `GameState`.

### SDE-1C — exact-evaluator orchestration

Route a bounded existing decision surface through the new seam using:

- canonical legal candidates;
- existing exact hypothetical evaluation;
- existing strategic structural diagnostics;
- no guessed new numeric thresholds;
- no new heuristic authority.

The first vertical slice should prove orchestration ownership, not production cutover.

### SDE-1D — lifecycle ownership

Represent and test the lifecycle distinction required by later Poisoner work:

```text
PERSISTENT
COMMITTED
PLANNED / UNCOMMITTED
```

SDE-1 establishes the ownership contract only. Poisoner invalidation/re-planning semantics belong to SDE-2.

### SDE-1E — integration boundary

Prove that:

- rules still own legality;
- epistemic still owns world consequences;
- session still owns committed mutation/history;
- flow still owns ordering;
- Experienced-mode manual override remains possible;
- the new engine does not create a second recommendation truth source.

## 5. SDE-1 non-goals

Do **not** during SDE-1:

- invent BEGINNER numeric Badness thresholds without reviewed evidence;
- open/retrain on the sealed SDE-0 holdout casually;
- implement full Drunk uncertainty;
- implement Spy/Recluse per-interaction uncertainty selection;
- implement Poisoner invalidation/re-planning;
- implement cross-night pacing;
- cut all production recommendation callers to the new engine;
- delete `ConsequenceEvaluator` before caller/fanout migration proves it has no unique contract;
- create another rules engine or possible-world solver.

Those belong to later stages.

## 6. Frozen architecture decisions

- rules own legal outcomes and registration legality;
- canonical session/game state remains actual-state authority;
- flow owns interaction ordering/projection;
- exact epistemic evaluation owns hypothetical world consequences;
- strategic evil topology matters more than raw full-role world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner may invalidate uncommitted plans;
- one engine continues beyond Night 1;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- `ConsequenceEvaluator` is migration-era and targeted for retirement after safe cutover.

## 7. Deliberately unfrozen

- exact Demon-seat / evil-team thresholds;
- forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT numeric profiles;
- exhaustive vs beam search;
- optional bounded soft preference;
- production cutover timing.

## 8. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE-1:

- audit-only work may be documentation-first;
- stable orchestration contracts require typed regression tests at the true owner;
- exact semantic changes require relevant epistemic/oracle validation;
- central orchestration integration requires broader T2/T4 validation when executable fanout changes;
- do not bring expensive calibration experiments back into ordinary bounded regression.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
6. query live `main` and open PRs / checks.

Where the older route document still describes SDE-0 / PR #143 as active, this roadmap and the current handoff supersede those **status-only** statements. Its architecture decisions remain authoritative unless explicitly superseded here.

## 10. Stable rule

> **SDE-0 is complete and merged. The current program is SDE-1: establish a thin `StorytellerDecisionEngine` orchestration seam over existing rules, session, flow, and exact epistemic authorities. Reuse canonical legal candidates and exact diagnostics; do not create parallel state or rules; do not perform SDE-2 uncertainty or SDE-4 production cutover early.**
