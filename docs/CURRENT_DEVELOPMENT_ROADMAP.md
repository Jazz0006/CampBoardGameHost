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
SDE-1A global fanout / orchestration seam audit        COMPLETE / PR #144
SDE-1B thin bounded exact-consequence contracts        COMPLETE / PR #144
SDE-1C exact-evaluator orchestration differential      COMPLETE / PR #144
SDE-1D lifecycle ownership / planned freshness         COMPLETE / PR #144
SDE-1E structured production shadow integration        COMPLETE / PR #144
SDE-2A Drunk ownership / revision replanning contract  COMPLETE / PR #144
SDE-2B Spy/Recluse exact registration witness binding  COMPLETE / PR #144
SDE-2C Poisoner invalidation / broad replanning        COMPLETE / PR #144
SDE-2D pre-SDE-3 strategic generalization              CURRENT
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

PR #144 was merged to `main` as:

`89453c902741699b072d11320d85a5561172abe5`

Current corrective-route authority:

`docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`

Always query live `main` before executable edits.

## 2. CURRENT

**SDE-2D — PRE-SDE-3 STRATEGIC GENERALIZATION**

The pre-SDE-3 algorithm review is complete. The three previously open questions are now frozen into an executable corrective route:

`docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`

Do **not** begin SDE-3 until SDE-2D reaches its acceptance gate.

### 2.1 SDE-2D1 — Drunk whole-bundle completion

Frozen direction:

- the Drunk shown role remains PERSISTENT setup/session truth and is never reselected by SDE;
- the unshown clue is a PLANNED Storyteller decision;
- supported shown information roles generate all surface-valid outputs, including accidentally truthful outputs;
- unreliable clue choice is evaluated jointly with the rest of Night 1;
- evaluation separates `HealthyCore`, `FullBundle` and `DrunkMarginal`;
- impaired public-claim semantics must distinguish functioning truth from malfunctioning permissiveness without leaking hidden Drunk/Poisoner state.

### 2.2 SDE-2D2 — Demon bluff joint-output migration

Frozen ownership:

```text
SetupCandidateGenerator
    -> legal bluff triplets

StorytellerDecisionEngine / policy
    -> strategic choice among uncommitted legal bluff triplets

shown + committed bluffs
    -> PERSISTENT setup fact for later planning
```

The current `SetupRecommendationService` `demon-bluff-ease` / `bluffDifficulty` score is migration-era heuristic selection, not the target authority.

Before commitment, Demon bluffs are an SDE output variable and must be evaluated jointly with other Storyteller-controlled first-night decisions. After commitment they are immutable inputs to remaining planning.

### 2.3 SDE-2D3 — strategic-world quotient

Raw role-world cardinality is no longer the primary recommendation unit.

The target strategic identity starts with:

```text
Demon seat
+ Minion seat set
```

A strategic topology survives if at least one mechanically legal assignment/witness can explain the current visible facts and hypothetical bundle.

Mechanical role assignments remain exact correctness witnesses; do not create a second recommendation-owned world solver.

Retain two separate evaluation axes:

- strategic pressure / evil-topology concentration;
- role-information utility, so useful good-role information is not discarded merely because it leaves evil topology unchanged.

### 2.4 SDE-2D4 — 5–15 player generalization and cost

Rules/setup semantics already support 5–15 non-Traveller players, but recommendation correctness/calibration/performance are not yet proven across that range.

Evidence must cover:

```text
5–6
7–9
10–12
13–15
```

Prefer baseline-relative diagnostics such as Demon-cover retention and evil-topology retention. Raw BEFORE/AFTER role-world count becomes secondary evidence.

Do not freeze a production player-count cutoff or approximation threshold before measured cost evidence.

### 2.5 SDE-2D5 — calibration / policy evidence

After the durable diagnostics exist, expand the calibration corpus across:

- Drunk versus healthy-core cases;
- bluff-supported versus bluff-fragile cases;
- equal/similar raw world count but different evil topology;
- useful role-information with unchanged evil topology;
- representative player-count regimes.

Keep the policy interpretable and gate-based; do not introduce an opaque global scalar.

### 2.6 Resume gate

SDE-3 may begin only after:

1. Drunk whole-bundle semantics are implemented for the supported first-night information roles;
2. Demon bluff strategic selection has a validated SDE path and committed lifecycle proof;
3. strategic-world exact/symbolic feasibility has a durable epistemic owner;
4. player-count-normalized diagnostics are defined where required;
5. representative 5–15 player correctness/performance evidence exists;
6. roadmap/handoff are explicitly advanced to SDE-3.

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

## 5. SDE-2A/B/C completion summary

SDE-2A/B/C are complete and provide the lifecycle/uncertainty foundation consumed by current SDE-2D.

### 5.1 Drunk — COMPLETE

Authority/evidence:

- `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`
- executable SHA `6c9d7fe8776fea4723004e22790ba11c1f140b8c`
- Android FAST and CI gate SUCCESS.

Frozen result:

```text
Drunk shown role      -> PERSISTENT setup/session truth
unshown Drunk clue    -> PLANNED / disposable
shown committed clue  -> COMMITTED / immutable history
Poisoner draft        -> playerInputRevision invalidation
Poisoner confirm      -> gameStateRevision invalidation
```

No SDE-specific dependency store or replanning revision counter was added.

### 5.2 Spy / Recluse — COMPLETE

Authority/evidence:

- `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
- core exact-registration semantics: `a977c01c0f4ae634dd60e4999759838f4005228c` with R2 / Android FAST / Real Clingo / CI gate SUCCESS;
- final SDE forwarding and pair projection: `ec970aaa302b7ea6ba5f869aec43cf8f3a82b950` with R2 / Android FAST / CI gate SUCCESS.

Frozen result:

```text
TroubleBrewingRegistrationDomain -> legality authority
WorldObservationResult           -> complete successful witness alternatives
ExactRegistrationWitnessBinding  -> one selected interaction-local witness
ExactConsequenceCandidate        -> forwards optional witness binding
PairInformationExactConsequenceAdapter
                                 -> pure projection from already-legal candidate
canonical player identity        -> unchanged
```

### 5.3 Poisoner — COMPLETE

Authority/evidence:

- `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
- executable SHA `fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`;
- R2 / Android FAST / CI gate SUCCESS.

Frozen result:

```text
Poisoner draft          -> playerInputRevision invalidation
Poisoner confirmation   -> durable hidden Poison action + gameStateRevision invalidation
uncommitted plans       -> stale across source revision changes
fresh planning          -> regenerate/re-evaluate through existing owners
committed observations  -> immutable
hidden poison target    -> excluded from recipient epistemic baseline
new lifecycle counter   -> none
```

Broad replanning was proven with both poisoned Empath and unaffected Chef. The unaffected role may keep the same legal IDs/diagnostics, but its old plan is still stale because its source revision is obsolete.

### 5.4 Acceptance target — COMPLETE

SDE-2 now proves:

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
- lifecycle metadata references, never duplicates, durable session truth;
- Drunk shown role is persistent input; Drunk clue choice is whole-bundle output until committed;
- Demon bluff legality remains setup-owned, while uncommitted bluff selection migrates to SDE strategic policy;
- committed Demon bluffs become persistent inputs and are not replanned;
- strategic evil topology, not raw role permutation multiplicity, is the primary large-player recommendation unit;
- mechanical worlds remain exact feasibility/correctness witnesses;
- role-information utility remains distinct from strategic pressure.

## 8. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE-2D:

- begin each executable slice with a focused fanout/ownership audit;
- add a RED only for a genuine new/changed durable contract;
- preserve exact rules/epistemic authority while changing strategic representation;
- prove no committed history rewrite and no second state/world authority;
- prove Drunk shown-role persistence and committed-bluff persistence;
- keep expensive corpus/performance work outside ordinary FAST regression;
- use exact/symbolic cross-check fixtures before reducing dependence on exhaustive enumeration;
- do not freeze player-count thresholds from 7-player evidence alone.

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
9. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
10. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
11. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
12. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
13. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
14. query live `main` and current checks.

## 10. Stable rule

> **PR #144 is merged at `89453c902741699b072d11320d85a5561172abe5`. The pre-SDE-3 review is resolved by SDE-2D. Execute SDE-2D1 Drunk whole-bundle completion, then SDE-2D2 Demon bluff joint-output migration, SDE-2D3 strategic-world quotient, SDE-2D4 5–15 player generalization/performance, and SDE-2D5 calibration. Demon bluffs are SDE outputs until committed and persistent inputs afterwards. Large-player strategy must move toward exact strategic evil-topology feasibility rather than exhaustive raw role-world multiplicity. Do not begin SDE-3 until the SDE-2D acceptance gate is explicitly satisfied.**
