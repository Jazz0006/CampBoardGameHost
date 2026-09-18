# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-18 Australia/Sydney  
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
SDE-1A global fanout / orchestration seam audit        COMPLETE / PR #144 branch
SDE-1B thin bounded exact-consequence contracts        COMPLETE / PR #144 branch
SDE-1C exact-evaluator orchestration differential      COMPLETE / PR #144 branch
SDE-1D lifecycle ownership / planned freshness         COMPLETE / PR #144 branch
SDE-1E structured production shadow integration        COMPLETE / PR #144 branch
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

SDE-1 authority/evidence:

- `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`
- `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`
- `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`
- `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`

Final executable validation head for SDE-1E:

`e7bb31937db32863e5606044b443818011d16236`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

Live `main` at validation:

`4d6e90a7a268570d261048931a3557433ea01d83`

Always query live `main` and PR #144 before executable edits.

## 2. CURRENT

**SDE-2 — first-night uncertainty / replanning**

SDE-1 is complete through SDE-1E. The next stage is to model the first uncertainty-bearing decisions that cannot be treated as one fixed healthy information world:

```text
Drunk
→ Spy/Recluse registration branches
→ Poisoner-driven invalidation/replanning
```

The objective is not to add another solver or mutable recommendation state. SDE-2 must compose the SDE-1 exact consequence seam with the existing setup/session/rules/registration authorities and make uncertainty/replanning explicit.

## 3. NEXT

```text
SDE-3  — cross-night impaired / registration decisions
SDE-4  — production cutover + legacy heuristic retirement
```

## 4. SDE-1 completion summary

### SDE-1A — fanout / seam audit — COMPLETE

Frozen results:

- `ClocktowerGameSession` remains canonical actual-state/revision/history authority;
- rules/candidate domains remain legal-outcome owners;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` remains exact consequence authority;
- `InformationDecisionContext` remains structured-information freshness/confirmation boundary;
- flow remains interaction-ordering owner;
- host UI/coordinator are migration callers, not target authorities;
- SDE-0 healthy bundle harness remains experiment/evidence infrastructure, not runtime owner;
- `ConsequenceEvaluator` still has three production caller families that must all be migrated before retirement:
  - `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
  - `RegistrationPolicy.generateCandidates(...)`;
  - `DayRecommendationModule` malfunction path.

### SDE-1B/1C — exact consequence seam — COMPLETE

Implemented under `clocktower/recommendation/sde`:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

The exact evaluator remains the oracle. SDE does not select, commit, mutate session state, alter interaction ordering, or provide heuristic fallback.

### SDE-1D — lifecycle ownership — COMPLETE

Frozen lifecycle:

```text
PERSISTENT
    session/setup-owned durable commitments

COMMITTED
    session/history-owned executed/shown facts

PLANNED / UNCOMMITTED
    SDE-owned disposable identity/freshness metadata only
```

`PlannedDecisionRef` stores only stable decision/candidate identity plus existing revision/semantic freshness provenance.

### SDE-1E — structured production shadow integration — COMPLETE

Validated path:

```text
existing structured numeric legality/materialization
→ InformationDecisionContext
→ StructuredInformationShadowAdapter
→ StorytellerDecisionEngine
→ exact historical evaluator
→ PlannedDecisionRef shadow provenance
→ existing confirmation
→ explicit ClocktowerGameSession durable commit
```

Proof:

- visible choices/recommendations are unchanged;
- exact diagnostics remain shadow-only;
- historical baseline revision and current decision revision are explicitly separated;
- no shadow timeline allocation or observation append occurs;
- no shadow revision movement occurs;
- confirmation remains `InformationDecisionContext` ownership;
- durable observation/revision mutation remains `ClocktowerGameSession` ownership;
- UI/Compose does not become exact-evaluation authority.

Executable evidence: `e7bb31937db32863e5606044b443818011d16236`.

## 5. SDE-2 starting route

Start SDE-2 with a fresh audit before implementation.

### 5.1 Drunk

Determine exactly which first-night choices depend on the Drunk's shown role and which parts are already persistent setup commitments versus uncommitted clue decisions.

Required distinction:

```text
committed shown identity
vs
uncommitted information candidate
vs
later durable shown information
```

Do not let SDE own or rewrite the committed Drunk shown role.

### 5.2 Spy / Recluse

Model registration as interaction-scoped uncertainty using `TroubleBrewingRegistrationDomain`, not as a permanent alternate identity.

Audit how the exact evaluator should consume branch-local registration assumptions without duplicating registration legality.

### 5.3 Poisoner

Model Poisoner changes as invalidation/replanning of still-uncommitted planned decisions.

Do not rewrite already committed/shown observations. Replanning must use existing session revisions and planned freshness rather than a new lifecycle counter.

### 5.4 Acceptance target

By the end of SDE-2, one first-night decision chain should prove:

```text
persistent setup commitments
+ current durable history
+ interaction-scoped registration/impaired uncertainty
→ exact consequence evaluation
→ stale-plan invalidation when source facts change
→ fresh replanning
→ existing confirmation/commit authorities
```

## 6. SDE-2 non-goals

Do not during SDE-2:

- cut all production recommendation selection to SDE;
- introduce a second session/history/rules authority;
- turn Spy/Recluse into permanent identity mutations;
- rewrite committed player-visible information;
- add cross-night policy beyond what is needed to preserve first-night semantics;
- retire `ConsequenceEvaluator` before all remaining caller families are migrated;
- introduce opaque global scalar optimization.

## 7. Frozen architecture decisions

- rules own legal outcomes and registration legality;
- session/game state remains actual-state and revision authority;
- flow owns interaction ordering/projection;
- exact epistemic evaluation owns hypothetical world consequences;
- `InformationDecisionContext` remains structured-information confirmation boundary;
- strategic evil topology matters more than raw role-world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner may invalidate uncommitted decisions but not committed facts;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- lifecycle metadata references, never duplicates, durable session truth.

## 8. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE-2:

- tests-first for each uncertainty boundary;
- exact evaluator remains oracle where possible;
- prove stale-plan invalidation with existing revisions;
- prove no committed history rewrite;
- prove registration legality remains in the registration domain;
- keep expensive corpus/calibration work outside ordinary FAST regression;
- use the final executable SHA, not a later docs-only SHA, as validation evidence.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
8. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
9. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
10. query live `main`, PR #144 and current checks.

## 10. Stable rule

> **SDE-1A/B/C/D/E are complete on PR #144's branch with executable validation on `e7bb319...`. Current work is SDE-2: first-night uncertainty and replanning, beginning with Drunk, then interaction-scoped Spy/Recluse registration, then Poisoner-driven invalidation of uncommitted plans. Preserve all SDE-1 ownership boundaries and do not perform production selection cutover yet.**
