# NEXT DEVELOPMENT HANDOFF — SDE-2D Strategic Generalization

> Updated: 2026-09-18 Australia/Sydney  
> Status: **SDE-2D1 COMPLETE / PR #145 FULL-CI GREEN — pending user-authorized merge**  
> Base checkpoint: PR #144 merged to `main` as `89453c902741699b072d11320d85a5561172abe5`; PR #145 is the validated D1 successor, not yet merged  
> Current route: `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
6. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
7. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
8. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
9. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
10. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
11. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
12. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
13. Storyteller Decision Engine route;
14. query live `main` and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, SDE-1 or SDE-2A/B/C work.

## 1. Live continuation point

PR #144 is merged. The old branch/draft status in the previous handoff is obsolete.

Merge commit:

`89453c902741699b072d11320d85a5561172abe5`

Final SDE-2C executable evidence before merge:

`fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`

Validated:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
CI gate                        SUCCESS
```

SDE-2D1 is now implemented on draft PR #145.

Final executable acceptance head:

`27b17d5e0eedea3367f5a1b69ed2093fb958f1af`

Validated:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

PR #145 remains draft and unmerged. Do not convert it to Ready or merge it without explicit user authorization.

After merge, the next executable stage is **SDE-2D2**, not SDE-3.

## 2. Frozen architecture entering SDE-2D

Existing ownership remains:

- `ClocktowerGameSession` owns canonical actual state, revisions and durable action/observation history.
- rules/candidate domains own legal outcomes.
- `TroubleBrewingRegistrationDomain` owns Spy/Recluse registration legality.
- the epistemic exact evaluator owns hypothetical consequence truth.
- `InformationDecisionContext` owns structured-information freshness/confirmation.
- flow owns interaction ordering.
- SDE owns orchestration/evaluation/planned decision metadata, not a second state or rules engine.

Lifecycle remains:

```text
PERSISTENT
    durable setup commitments

COMMITTED
    already executed/shown facts

PLANNED / UNCOMMITTED
    disposable current recommendation decisions
```

## 3. Newly frozen pre-SDE-3 decisions

### 3.1 Drunk information-role clues

The Drunk shown identity is already fixed before clue recommendation.

Therefore:

```text
shown role
    = PERSISTENT INPUT

unshown unreliable clue
    = SDE OUTPUT / PLANNED

shown clue
    = COMMITTED
```

Do not let SDE choose or replace the shown role.

The unreliable clue must become a first-class whole-bundle candidate. Candidate generation should produce all surface-valid outputs for the shown ability, including outputs that happen to be truthful.

Evaluate:

```text
HealthyCore
FullBundle = HealthyCore + Drunk clue
DrunkMarginal = FullBundle - HealthyCore
```

Do not model the Drunk clue as a healthy clue multiplied by an arbitrary numeric weight.

### 3.2 Demon bluffs

Current live ownership:

```text
SetupCandidateGenerator
    -> legal bluff triplets

SetupRecommendationService
    -> current heuristic selection
       via demon-bluff-ease / bluffDifficulty
```

Target ownership:

```text
SetupCandidateGenerator
    -> legality only

StorytellerDecisionEngine / policy
    -> strategic selection of uncommitted bluff triplets
```

Before the Demon is shown the three identities, bluff choice is an **OUTPUT decision variable** and belongs in the same strategic planning ecology as other Storyteller-controlled Night-1 choices.

After reveal/commit:

```text
chosen Demon bluffs
    -> PERSISTENT INPUT
```

Later Poisoner or player-controlled changes must not regenerate committed bluffs.

### 3.3 Large-player strategic representation

Do not plan long-term production around exhaustive materialization/counting of every complete role assignment.

Primary strategic representation moves toward evil topology:

```text
StrategicWorldKey(
    demonSeat,
    minionSeats
)
```

A topology survives when at least one legal mechanical assignment/witness supports it.

Mechanical worlds remain the exact correctness/feasibility authority. Recommendation must not create a second solver.

Raw mechanical-world count becomes secondary evidence.

Keep separate:

```text
Strategic pressure
    evil-topology concentration/collapse

Role-information utility
    useful identity/role information even when topology is unchanged
```

## 4. COMPLETE — SDE-2D1 Drunk whole-bundle completion

Authority:

- `docs/SDE_2D1_DRUNK_WHOLE_BUNDLE_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
- draft PR #145;
- final executable acceptance head `27b17d5e0eedea3367f5a1b69ed2093fb958f1af`.

Implemented:

- impairment-capable public information claims;
- exact latent shown-role state for non-recipient Drunk worlds;
- shared-world finite malfunction-resource semantics;
- Drunk Washerwoman/Librarian/Investigator pair whole-bundle evaluation;
- rules-owned numeric display domain plus Drunk Chef/Empath whole-bundle evaluation;
- Fortune Teller player-controlled target robustness plus Drunk Yes/No whole-bundle evaluation;
- shared `HealthyCore / FullBundle / DrunkMarginal` exact consequence seam;
- healthy harness/calibration remain healthy-only compatibility owners;
- no Host/UI production selection cutover.

Finite-resource regression evidence protects:

- at most one Drunk identity per exact mechanical world;
- Baron single-Minion profile excludes Poisoner and poison explanations;
- current Trouble Brewing Poisoner worlds have exactly one ordinary poison target.

Do not generalize the last rule into a cross-script global one-poisoned-player invariant.

## 5. NEXT — SDE-2D2 Demon bluff joint-output migration

**Start only after PR #145 is merged. Use a fresh branch.**

Read-only fanout precheck already reconfirmed:

- `SetupCandidateGenerator.generateDemonBluffCandidates()` remains the legal triplet owner;
- `SetupRecommendationService` currently supplies strategic preference through `demon-bluff-ease / bluffDifficulty`;
- Demon bluff presentation already distinguishes applied setup decisions from pending recommendation output, providing a useful committed/persistent boundary.

After PR #145 merge:

- audit `SetupRecommendationService` bluff fanout and commit boundary;
- keep `SetupCandidateGenerator.generateDemonBluffCandidates()` as legality owner;
- define the smallest SDE candidate/output shape for an uncommitted bluff triplet;
- evaluate bluff support against the same whole-bundle strategic consequences;
- prove committed bluff persistence;
- preserve existing visible production behavior initially through shadow/differential evidence;
- retire `demon-bluff-ease` as strategic authority only at an explicit cutover step.

Initial useful bluff diagnostics:

- role support at the actual Demon seat;
- supported evil-team topologies;
- information-role bluff claim affordance;
- overlap/redundancy across the three bluff narratives;
- narrative complexity / exception dependence;
- interaction with healthy information, Drunk clue and Red Herring.

Do not re-run complete raw exact enumeration for every bluff triplet when a shared structural diagnostic can support a cheaper overlay.

## 6. THEN — SDE-2D3 Strategic-world quotient

Perform a fresh fanout/representation audit before implementation.

Goal:

```text
mechanical exact/symbolic feasibility
        ↓
strategic topology quotient
        ↓
SDE diagnostics
```

Initial strategic identity:

```text
Demon seat + Minion seat set
```

The durable owner should live in or directly beside the epistemic consequence layer, not recommendation.

Prove differential correctness on bounded fixtures:

- every surviving strategic topology has at least one valid mechanical witness;
- every rejected topology has no valid witness under the same facts;
- raw role permutations that share one topology do not gain extra strategic weight merely by multiplicity;
- registration/malfunction branches remain semantically available as witness explanations.

Prefer exact quotient or constraint/symbolic feasibility over random sampling.

## 7. THEN — SDE-2D4 5–15 player validation

Evidence regimes:

```text
5–6
7–9
10–12
13–15
```

Validate separately:

- semantic correctness;
- candidate-space assumptions;
- normalized strategic metrics;
- CPU cost;
- memory cost;
- mobile-feasible latency.

Useful baseline-relative metrics include:

```text
demonCoverRetention
evilTopologyRetention
evilCoverRetention
forcedGoodFraction
```

For a 15-player one-Demon/three-Minion topology, the raw seat-topology upper bound is:

```text
15 × C(14,3) = 5,460
```

Do not freeze a production switch threshold until measured evidence exists.

## 8. THEN — SDE-2D5 calibration

Only after durable diagnostics exist, expand human-review/calibration evidence across:

- Drunk misinformation;
- bluff-supported and bluff-fragile bundles;
- role-information-useful but topology-neutral clues;
- strategic collapse cases;
- all player-count regimes.

Keep policy gate-based and interpretable.

No opaque global scalar.

## 9. SDE-3 resume gate

Do not begin SDE-3 until all are true:

1. Drunk whole-bundle semantics are implemented for supported first-night information roles;
2. Demon bluff selection has a validated SDE strategic path;
3. committed bluff persistence is proven;
4. strategic-world exact/symbolic feasibility has a durable epistemic owner;
5. player-count-normalized metrics exist where required;
6. representative 5–15 player semantic/performance evidence exists;
7. roadmap/handoff are explicitly advanced to SDE-3.

## 10. Non-goals

Do not during SDE-2D:

- create a second rules/world/state authority;
- reselect the Drunk shown role;
- replan committed Demon bluffs;
- rewrite committed observations;
- introduce unlabelled random sampling as if exact;
- make raw world count the primary policy objective;
- collapse all diagnostics into one global score;
- cut all production recommendation paths prematurely;
- retire `ConsequenceEvaluator` before its migration obligations are complete;
- implement cross-night SDE-3 behavior.

## 11. Testing cadence

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For SDE-2D:

- focused ownership/fanout audit before each slice;
- real RED/GREEN only for changed durable behavior;
- exact evaluator / existing rules semantics remain oracle evidence;
- use differential fixtures when introducing the strategic quotient;
- keep expensive calibration/performance workloads outside ordinary FAST tests;
- run T1/T2/T4 only at logical checkpoints required by the testing strategy;
- docs-only route changes need exact diff/source audit, not manufactured runtime tests.

## 12. Stable handoff

> **SDE-2D1 is fully implemented and full-ci GREEN on draft PR #145 at executable head `27b17d5e0eedea3367f5a1b69ed2093fb958f1af`, but remains unmerged pending explicit user authorization. After merge, start SDE-2D2 on a fresh branch: preserve SetupCandidateGenerator as Demon bluff legality owner, migrate still-uncommitted bluff strategic selection away from SetupRecommendationService bluffDifficulty heuristics into SDE joint-output evaluation, and prove committed bluff persistence. Then proceed to the epistemic-owned strategic evil-topology quotient, 5–15 player validation, and calibration. SDE-3 remains blocked until the complete SDE-2D gate is explicitly satisfied.**
