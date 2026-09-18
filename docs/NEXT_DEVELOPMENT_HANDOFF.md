# NEXT DEVELOPMENT HANDOFF — SDE-2D Strategic Generalization

> Updated: 2026-09-18 Australia/Sydney  
> Status: **SDE-2D3 COMPLETE / PR #147 FULL-CI GREEN — pending user-authorized merge**  
> Base checkpoint: PR #146 merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`; PR #147 is the validated D3 successor, not yet merged  
> Current route: `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
6. `docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`;
7. `docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
8. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
9. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
10. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
11. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
12. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
13. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
14. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
15. Storyteller Decision Engine route;
16. query live `main` and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, SDE-1 or SDE-2A/B/C work.

## 1. Live continuation point

PR #145 is merged to live `main`.

Merge commit:

`6fc0d99f1a250b91925280ed12ec0b199220b060`

SDE-2D2 is merged to live `main` via PR #146.

Merge commit:

`0aa488098f1284e26d8df03f4028cc263bcf9f8a`

Final executable acceptance head:

`6acb708bd734d36d024240f0dea213a882a7714b`

Validated:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

Authority:

`docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`

PR #146 is merged.

SDE-2D3 is fully implemented on draft PR #147.

Final executable acceptance head:

`2d491663a899d048b4b69522e9cdde700effa85b`

Validated:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

Authority:

`docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`

PR #147 remains draft and unmerged. Do not convert it to Ready or merge it without explicit user authorization.

After merge, the next executable stage is **SDE-2D4 5–15 player generalization/performance**, not SDE-3.

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
- PR #145 merged to `main` as `6fc0d99f1a250b91925280ed12ec0b199220b060`;
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

## 5. COMPLETE — SDE-2D2 Demon bluff joint-output migration

Authority:

- `docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
- PR #146 merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`;
- executable acceptance head `6acb708bd734d36d024240f0dea213a882a7714b`.

Implemented:

- `SetupCandidateGenerator.generateDemonBluffCandidates()` remains the legal triplet owner;
- SDE consumes already-legal triplets through a pure projection adapter;
- each distinct bluff role receives an exact strict-`ShownRoleAt` counterworld support diagnostic against the current public whole-bundle facts;
- legal triplets share the same role-support records instead of multiplying exact enumeration by triplet count;
- triplet overlay exposes supported roles, evil-team topology union/intersection and topology-pattern diversity;
- the exact evaluator shares pristine source-world scans for different shown-role values constrained to the same seat set while preserving exact per-query role checks;
- applied presentation wins over later pending recommendation changes;
- recovery preserves committed bluff roles;
- locked Demon bluffs are persistent inputs and are rejected from SDE replanning;
- `ClocktowerRecommendationCoordinator.evaluateSetupDemonBluffShadow()` supplies a shadow-only orchestration seam beside the existing setup recommendation result;
- `DemonBluffSetupShadowAdapter` returns the same visible result object and records legacy selected candidate IDs by style;
- bluff triplet identity is canonicalized across setup producers so list-order differences cannot create false domain mismatches;
- `DemonBluffJointOutputEvaluatorTest` is measured-expensive exact evidence and remains mandatory in full/affected validation rather than FAST.

Production selection has **not** cut over. `SetupRecommendationService` remains unchanged and `demon-bluff-ease / bluffDifficulty` remains the visible compatibility heuristic pending the later explicit production cutover.

Do not reopen D2A–D2D implementation on the next branch unless a concrete regression is found.

## 6. COMPLETE — SDE-2D3 Strategic-world quotient

Authority:

`docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`

The representation/fanout audit, first implementation and T4 acceptance are complete. Final executable acceptance head: `2d491663a899d048b4b69522e9cdde700effa85b`.

Implemented flow:

```text
EnumeratedWorld.rolesBySeat
    -> StrategicWorldKey(setup Demon seat, setup Minion seats)
    -> existing exact WorldStructureAccumulator pass
    -> ExactWorldStructureDiagnostics.strategicWorldKeys
    -> SDE precise strategic diagnostics
```

Key contracts:

- same setup evil topology collapses good-role / shown-role / impairment / explanation multiplicity;
- Demon/Minion swap within the same evil seat set remains distinct;
- projection uses role-definition CharacterType;
- Imp succession changes current active Demon state but does not rewrite the setup key.

Demon-bluff diagnostics now expose precise strategic-key union/intersection/pattern beside the old coarse evil-seat topology fields.

Do not reconstruct the joint topology from `possibleDemonSeats` and `evilTeamSeatConfigurations`. Do not add the key to generic `PlayerWorldSet` / ZDD in D2D3. No production selection cutover occurred.

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

## 7. NEXT AFTER PR #147 MERGE — SDE-2D4 5–15 player validation

Do not begin executable D2D4 work on the SDE-2D3 branch. After user-authorized merge of PR #147, create a fresh branch from live `main` and perform a focused performance/representation audit before implementation.

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

> **SDE-2D3 is fully implemented and full-ci GREEN on draft PR #147 at acceptance head `2d491663a899d048b4b69522e9cdde700effa85b`, but remains unmerged pending explicit user authorization. PR #146 is merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`. After PR #147 is merged, start SDE-2D4 on a fresh branch: validate the exact strategic quotient across 5–15 player regimes, measure CPU/memory/mobile latency, define normalized strategic metrics, and only then decide whether symbolic topology feasibility or another representation switch is required. Production Demon-bluff selection has not cut over; `demon-bluff-ease` remains compatibility behavior until the later explicit cutover. SDE-3 remains blocked until the complete SDE-2D gate is explicitly satisfied.**
