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
SDE-1A global fanout / orchestration seam audit        COMPLETE
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

The SDE-1A audit is recorded in:

`docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`

Always query live `main` before executable edits.

## 2. CURRENT

**SDE-1B / SDE-1C — thin typed contracts + bounded exact-consequence orchestration seam**

SDE-1A established that the engine must compose existing owners rather than recreate them.

Current target flow:

```text
existing healthy typed legal candidate/effect
        ↓
recommendation-owned SDE request/context
        ↓
existing proposition / hypothetical observation adapter
        ↓
ExactHistoricalHypotheticalObservationBundleEvaluator
        ↓
typed CandidateConsequence
        ↓
policy boundary shell
```

The first executable slice is evaluation-only. It must not move production selection, UI routing, confirmation, durable commit, flow ordering, or uncertainty ownership.

Preferred initial fixture: an already-supported healthy structured-information interaction, with structured numeric as the lowest-coupling path because its current `DynamicGenerationContext` does not supply `state` and therefore does not invoke legacy `ConsequenceEvaluator`.

## 3. NEXT

```text
SDE-1D — lifecycle ownership contract
SDE-1E — integration boundary proof
SDE-2  — Drunk -> Spy/Recluse -> Poisoner first-night uncertainty
SDE-3  — cross-night impaired / registration decisions
SDE-4  — production cutover + legacy heuristic retirement
```

## 4. SDE-1 implementation order

### SDE-1A — fanout / seam audit — COMPLETE

Completed findings:

- `ClocktowerGameSession` remains canonical actual-state/revision/history authority;
- rules/candidate domains remain legal-outcome owners;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` remains exact consequence authority;
- `InformationDecisionContext` remains structured-information freshness/confirmation boundary;
- flow remains interaction-ordering owner;
- host UI/coordinator are migration callers, not target authorities;
- SDE-0 healthy bundle harness remains experiment/evidence infrastructure, not runtime owner;
- `ConsequenceEvaluator` has three production caller families that must all be migrated before retirement:
  - `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
  - `RegistrationPolicy.generateCandidates(...)`;
  - `DayRecommendationModule` malfunction path.

### SDE-1B — thin typed contracts — CURRENT

Define only the smallest durable contracts required for exact consequence orchestration.

Expected concepts, subject to implementation evidence:

```text
StorytellerDecisionEngine
DecisionContext
DecisionRequest / DecisionKind
LegalDecisionCandidate
CandidateConsequence
StorytellerPolicyResult
```

Reuse existing revision/snapshot/evaluator types where possible. Do not create a shadow `GameState`, second semantic history, second solver, or duplicate lifecycle store.

The first contract may expose evaluation without selection.

### SDE-1C — exact-evaluator orchestration — CURRENT

Route one healthy structured-information fixture through the new seam using:

- an existing typed legal candidate/effect;
- existing proposition/observation materialization;
- `ExactHistoricalHypotheticalObservationBundleEvaluator`;
- exact structural diagnostics;
- no guessed new numeric thresholds;
- no new heuristic authority.

Required focused evidence:

```text
SDE consequence output
==
direct exact-evaluator output
```

for the same hypothetical observation/context.

This is a differential orchestration contract, not a new epistemic oracle.

### SDE-1D — lifecycle ownership

Represent and test:

```text
PERSISTENT
COMMITTED
PLANNED / UNCOMMITTED
```

Planned state remains orchestration-local and disposable. Persistent/committed truth remains session-owned. Poisoner invalidation/replanning semantics remain SDE-2.

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
- implement Poisoner invalidation/replanning;
- implement cross-night pacing;
- cut all production recommendation callers to the new engine;
- delete `ConsequenceEvaluator` before all three caller families are safely migrated;
- create another rules engine or possible-world solver;
- move durable observation commit into SDE.

## 6. Frozen architecture decisions

- rules own legal outcomes and registration legality;
- canonical session/game state remains actual-state authority;
- session revisions remain freshness authority;
- flow owns interaction ordering/projection;
- exact epistemic evaluation owns hypothetical world consequences;
- strategic evil topology matters more than raw full-role world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner may invalidate uncommitted plans;
- one engine continues beyond Night 1;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- `ConsequenceEvaluator` is migration-era and targeted for retirement only after safe caller migration;
- SDE orchestration belongs under recommendation/SDE rather than session, UI or epistemic;
- the first SDE slice is evaluation-only and does not own production selection.

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

For current SDE-1B/1C work:

- add focused typed regression tests at the new orchestration owner;
- compare the orchestration result with the existing exact evaluator for the same request/context;
- exact evaluator semantics themselves should not change for the first seam;
- no UI/integration breadth is required until an executable production caller is changed;
- expensive calibration experiments remain outside ordinary FAST regression.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
7. query live `main` and open PRs/checks.

## 10. Stable rule

> **SDE-1A is complete. Current work is SDE-1B/1C: add the smallest recommendation-owned typed orchestration seam that evaluates an existing healthy structured-information candidate through the existing exact epistemic authority and returns typed consequence diagnostics. No production cutover, no second state/rules/solver owner, and no SDE-2 uncertainty yet.**
