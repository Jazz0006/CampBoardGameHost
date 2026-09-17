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
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

SDE-1 authority/evidence:

- `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`
- `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`
- `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`

Latest executable validation head for SDE-1D:

`c4e0967e416f0a259e50ab38ef2d832f6ab1b7c0`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

Live `main` at validation:

`4d6e90a7a268570d261048931a3557433ea01d83`

Always query live `main` before executable edits.

## 2. CURRENT

**SDE-1E — integration boundary / one real structured caller shadow proof**

SDE-1A–D have established ownership, the bounded exact-consequence evaluator seam, and disposable planned-decision freshness metadata. The current task is to wire **one existing healthy structured-information production path** through the SDE as a shadow/integration proof while preserving all visible behavior and existing confirmation/commit ownership.

Target chain:

```text
rules-owned legal candidate
→ existing proposition / EpistemicObservation materialization
→ StorytellerDecisionEngine.evaluateExactConsequences(...)
→ PlannedDecisionRef freshness provenance
→ existing InformationDecisionContext confirmation
→ existing ClocktowerGameSession commit
```

The SDE result is shadow/evidence only in SDE-1E. It must not become the production selection truth source yet.

## 3. NEXT

```text
SDE-2  — first-night uncertainty: Drunk -> Spy/Recluse -> Poisoner replanning
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

The existing legacy `domain.StorytellerDecisionRequest` is intentionally not reused because it embeds `DynamicGameState`. `ExactConsequenceCandidate` carries a non-empty observation bundle so whole-bundle semantics survive the orchestration boundary.

### SDE-1C — exact-evaluator orchestration — COMPLETE

Focused regression evidence proves:

- exact differential equivalence with the existing evaluator;
- deterministic evaluation;
- multi-observation bundle forwarding;
- multi-candidate evaluation from one immutable baseline;
- no timeline/observation-log mutation;
- capability deferral without heuristic fallback;
- malformed/duplicate candidate identity fail-fast.

No selection, UI routing, durable commit or production caller changed.

### SDE-1D — lifecycle ownership — COMPLETE

Audit result:

```text
PERSISTENT
    session/setup-owned durable commitments

COMMITTED
    session/history-owned executed/shown facts

PLANNED / UNCOMMITTED
    SDE-owned disposable identity/freshness metadata only
```

`PlannedDecisionRef` contains only:

- stable `decisionId`;
- stable `candidateId`;
- existing `InformationDecisionRevision` provenance;
- optional structured-information semantic snapshot identity.

It does not contain canonical state, candidate pools, selected payloads, history, session mutation handles or a revision counter.

Focused tests prove stale detection on either revision and on candidate-space semantic identity, plus source candidate membership validation. Android FAST ran successfully on executable SHA `c4e0967e...`.

### SDE-1E — integration boundary — CURRENT

Wire one healthy structured-information path through SDE in shadow mode.

Required proof:

- rules still own legal candidates;
- existing adapters still own proposition/observation materialization;
- exact epistemic evaluator still owns world consequences;
- `PlannedDecisionRef` records only disposable provenance;
- `InformationDecisionContext` still owns confirmation/freshness;
- `ClocktowerGameSession` still owns durable observation commit and revision movement;
- flow still owns interaction ordering;
- Experienced-mode manual override remains possible;
- visible recommendation/selection behavior is unchanged;
- no second recommendation truth source is introduced.

Prefer the healthy structured numeric path because it already has stable candidate IDs, typed observation materialization, revision validation, and no legacy `ConsequenceEvaluator` call when `DynamicGenerationContext.state == null`.

## 5. SDE-1E audit before edit

Trace the real structured numeric production path and identify the narrowest shadow hook:

```text
ClocktowerStructuredInformationPreparation
→ StructuredNumericInformationAdapter
→ ClocktowerRecommendationCoordinator.resolveNumberInformation
→ DynamicCandidateGenerator.generateNumeric
→ InformationDecisionContext
→ StructuredNumberInformationUiModel
→ confirmation
→ session observation commit
```

The hook should receive an already-legal/materialized candidate set and current exact context without moving any existing selection/confirmation authority.

Do not route UI directly to SDE.

## 6. SDE-1 non-goals

Do **not** during SDE-1E:

- invent BEGINNER numeric Badness thresholds;
- alter visible selected/recommended candidates;
- implement Drunk uncertainty;
- implement Spy/Recluse uncertainty selection;
- implement Poisoner invalidation/replanning;
- implement cross-night pacing;
- migrate all production recommendation callers;
- delete `ConsequenceEvaluator`;
- create another rules engine, state store, history store or possible-world solver;
- move durable observation commit into SDE.

## 7. Frozen architecture decisions

- rules own legal outcomes and registration legality;
- session/game state remains actual-state and revision authority;
- flow owns interaction ordering/projection;
- exact epistemic evaluation owns hypothetical world consequences;
- `InformationDecisionContext` remains the current structured-information confirmation boundary;
- strategic evil topology matters more than raw role-world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner may invalidate uncommitted decisions, but implementation waits for SDE-2;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- `ConsequenceEvaluator` is migration-era and targeted for retirement only after safe caller migration;
- lifecycle metadata references, never duplicates, durable session truth.

## 8. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE-1E:

- use a focused integration/owner-level test proving the real structured path can invoke the SDE without changing its existing output;
- exact consequence comparison may reuse the existing evaluator as oracle rather than implementing another solver;
- explicitly assert confirmation/session commit authority remains unchanged;
- only broaden to T2/T4 if the implementation touches central runtime fanout rather than a bounded shadow adapter;
- keep expensive calibration corpus work outside ordinary FAST regression.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
8. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
9. query live `main`, PR #144 and current checks.

## 10. Stable rule

> **SDE-1A/B/C/D are complete on PR #144's branch. Current work is SDE-1E: wire one healthy structured-information production path through the SDE as a bounded shadow/integration proof, preserving existing visible behavior and leaving confirmation/commit authority with InformationDecisionContext and ClocktowerGameSession. Do not start SDE-2 uncertainty or production selection cutover yet.**
