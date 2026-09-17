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
SDE-1B thin bounded exact-consequence contracts        COMPLETE / PR #144 branch
SDE-1C exact-evaluator orchestration differential      COMPLETE / PR #144 branch
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

SDE-1A authority:

`docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`

SDE-1B/1C completion evidence:

`docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`

Executable validation head:

`a0c4fe2a2d1c1c94daedd976285cd29e40a48ee6`

with R2 success, Android FAST success and CI gate success on PR #144.

Always query live `main` before executable edits.

## 2. CURRENT

**SDE-1D — lifecycle ownership contract**

The exact-consequence seam now exists without production cutover. The next task is to represent the already-frozen decision lifecycle without creating another game-state/history owner.

Required distinction:

```text
PERSISTENT
    durable setup/session-owned commitments
    referenced by orchestration, never copied into a second authority

COMMITTED
    already executed/shown durable facts
    immutable and session/history owned

PLANNED / UNCOMMITTED
    disposable recommendation-local plan identity
    bound to source revisions
    may be invalidated/re-evaluated before commit
```

SDE-1D is ownership/lifecycle modeling only. It must not implement Poisoner replanning yet; Poisoner invalidation semantics remain SDE-2.

## 3. NEXT

```text
SDE-1E — integration boundary / one real structured caller shadow proof
SDE-2  — Drunk -> Spy/Recluse -> Poisoner first-night uncertainty
SDE-3  — cross-night impaired / registration decisions
SDE-4  — production cutover + legacy heuristic retirement
```

## 4. SDE-1 implementation order

### SDE-1A — fanout / seam audit — COMPLETE

Frozen results:

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

### SDE-1B — thin typed contracts — COMPLETE

Implemented under `clocktower/recommendation/sde`:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

These are deliberately bounded exact-evaluation envelopes, not a competing global state model.

The existing legacy `domain.StorytellerDecisionRequest` is intentionally not reused because it embeds `DynamicGameState`. The existing generic `DecisionCandidate<T>` remains a legal/recommendation-era model that a later adapter may project from rather than duplicate.

`ExactConsequenceCandidate` carries a non-empty observation bundle so whole-bundle semantics survive the orchestration boundary.

### SDE-1C — exact-evaluator orchestration — COMPLETE

The seam delegates directly to `ExactHistoricalHypotheticalObservationBundleEvaluator` and returns its exact structural diagnostics keyed by stable candidate identity.

Focused regression evidence proves:

- single-candidate exact differential equivalence;
- deterministic repeated evaluation;
- multi-observation bundle forwarding;
- multi-candidate evaluation from one immutable context;
- no timeline/observation-log mutation;
- capability deferral without heuristic fallback;
- duplicate candidate-ID fail-fast.

No selection, UI routing, commit or production caller changed.

### SDE-1D — lifecycle ownership — CURRENT

Represent and test the lifecycle distinction without shadowing session state:

```text
PERSISTENT
COMMITTED
PLANNED / UNCOMMITTED
```

The minimum useful planned record should contain stable decision/interaction identity, source `gameStateRevision` / `playerInputRevision`, candidate/snapshot identity where required, and lifecycle kind.

It must not contain a mutable copy of canonical game state or durable history.

Required SDE-1D proofs:

- planned records are revision-bound and disposable;
- committed/persistent references cannot be mutated through SDE lifecycle state;
- stale planned state can be detected from session revisions;
- lifecycle metadata does not commit observations or advance revisions;
- no Poisoner-specific invalidation/replanning policy is introduced yet.

### SDE-1E — integration boundary — NEXT

After lifecycle ownership is explicit, wire one existing healthy structured-information path through the SDE as a bounded shadow/integration proof before wider migration.

Prove that:

- rules still own legality;
- epistemic still owns world consequences;
- session still owns committed mutation/history;
- flow still owns ordering;
- `InformationDecisionContext` still owns confirmation/freshness;
- Experienced-mode manual override remains possible;
- the SDE does not create a second recommendation truth source;
- production-visible choice behavior remains unchanged until an explicit later cutover.

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
- Poisoner may invalidate uncommitted decisions;
- one engine continues beyond Night 1;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- `ConsequenceEvaluator` is migration-era and targeted for retirement only after safe caller migration;
- SDE orchestration belongs under recommendation/SDE rather than session, UI or epistemic;
- exact-consequence orchestration is evaluation-only until later integration/cutover work;
- lifecycle metadata must reference, not duplicate, session-owned durable state.

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

For SDE-1D:

- use focused owner-level tests for lifecycle state and revision freshness;
- do not require exact-world recomputation tests unless exact-evaluation behavior changes;
- do not add UI/integration breadth until SDE-1E;
- central runtime fanout changes later require broader T2/T4 validation;
- expensive calibration experiments stay outside ordinary FAST regression.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
8. query live `main`, PR #144 and current checks.

## 10. Stable rule

> **SDE-1A/B/C are complete on PR #144's branch. Current work is SDE-1D: model persistent/committed/planned lifecycle ownership as thin revision-bound metadata over session-owned truth. Do not implement Poisoner replanning, production selection cutover, or another state/history authority.**
