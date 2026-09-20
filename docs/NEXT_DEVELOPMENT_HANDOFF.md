# NEXT DEVELOPMENT HANDOFF — SDE-2D Strategic Generalization

> Updated: 2026-09-19 Australia/Sydney  
> Status: **SDE-2D5 CURRENT — D5F-B human review pending; D5F-B2 external-human pilot ACTIVE**  
> Base checkpoint: PR #149 squash-merged to `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`; D2D5 branch `sde-2d5-calibration-policy-evidence`  
> Current route: `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_2D5F_HUMAN_REVIEW_GATE_HOLDOUT_AUDIT_2026-09-19.md`;
6. `docs/SDE_2D5F_EXTERNAL_HUMAN_EVIDENCE_PILOT_2026-09-20.md`;
7. `docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`;
8. `docs/SDE_2D5F_EXTERNAL_HUMAN_REPEAT_SIGNAL_AUDIT_2026-09-20.md`;
7. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
6. `docs/SDE_2D4_5_15_CORRECTNESS_PERFORMANCE_NORMALIZED_METRICS_AUDIT_2026-09-18.md`;
7. `docs/SDE_2D4_TOPOLOGY_FIRST_FEASIBILITY_FANOUT_AUDIT_2026-09-18.md`;
8. `docs/SDE_2D4_TOPOLOGY_FIRST_BUNDLE_INTEGRATION_AUDIT_2026-09-18.md`;
9. `docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`;
10. `docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
11. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
12. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
13. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
14. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
15. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
16. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
17. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
18. Storyteller Decision Engine route;
19. query live `main`, PR #149 head and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, SDE-1 or SDE-2A/B/C work.

D5F-B2 is deliberately parallel calibration evidence, not a replacement for the controlled eight-item D5F-B review. The first 14-player public Trouble Brewing pilot completed successfully and exposed a policy correction: Demon-bluff shared/union support is not monotone BEGINNER quality. Preserve practical bluffability, real-information anchoring, narrative-route diversity, complementary strategic coverage and a bounded Night-1 pressure band as separate hypotheses until further real cases are reviewed.

Immediate continuation:

1. continue explicit human judgment of the eight repaired D5F records;
2. reconstruct 2–3 more public Trouble Brewing Night-1 cases;
3. classify external cases as fully reconstructible executable fixtures or qualitative-only evidence;
4. compare repeated/new axes before D5F-C gate derivation;
5. do not open holdout, freeze thresholds, cut production policy, or begin SDE-3.

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

PR #147 was user-authorized and merged to live `main` as:

`2cab06efeee5692024e065c632deefe765ca1618`

PR #149 was user-authorized and squash-merged to live `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`. **SDE-2D5 calibration / policy evidence is now current** on fresh branch `sde-2d5-calibration-policy-evidence`; do not begin SDE-3.

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

## 7. COMPLETE — SDE-2D4 5–15 player validation

PR #147 is merged to live `main` as `2cab06efeee5692024e065c632deefe765ca1618`.

D2D4 branch:

`sde-2d4-5-15-validation-performance`

Draft PR:

**#149 — SDE-2D4: validate 5–15 strategic scaling**

Authority:

- `docs/SDE_2D4_5_15_CORRECTNESS_PERFORMANCE_NORMALIZED_METRICS_AUDIT_2026-09-18.md`;
- `docs/SDE_2D4_TOPOLOGY_FIRST_FEASIBILITY_FANOUT_AUDIT_2026-09-18.md`;
- `docs/SDE_2D4_TOPOLOGY_FIRST_BUNDLE_INTEGRATION_AUDIT_2026-09-18.md`.

Accepted implementation:

```text
D4A   normalized strategic diagnostics
D4B   source-derived scale matrix + bounded raw-stream evidence
D4C1  exact topology-domain enumeration for 5–15
D4C2  role/type/shown-role setup witness existence
D4C3  Drunk + Poisoner finite-resource AbilityState feasibility
D4C4a registration-aware identity observations
D4C4b Chef / Empath / Fortune Teller / Red Herring observations
D4C4c composable AnyOf / AllOf / supported Not constraint branches
D4C5  exact strategic topology whole-bundle evaluator with shared witness
D4D   same-world whole-bundle exhaustive differential parity
D4E   measured 5–15 topology-bundle performance matrix
```

Key frozen contracts:

- `TroubleBrewingStrategicTopologyDomain` enumerates Demon seat × Minion seat combinations only;
- `TroubleBrewingTopologySetupWitnessEvaluator` proves setup existence without complete mechanical-world enumeration;
- remaining seat assignment uses an exact type-quota → role → seat max-flow proof;
- `TroubleBrewingTopologyHypotheticalBundleEvaluator` composes all observations into one shared witness with unique Drunk, unique roles, one Poisoner target and one Red Herring identity;
- registration legality remains owned by `TroubleBrewingRegistrationDomain`;
- `WorldCardinality.Exact` still means exact mechanical-world cardinality;
- no production recommendation consumer has cut over to topology-first diagnostics.

D4D whole-bundle parity covers role-identity conflict, Poisoner shared target, Drunk shared resource, Spy/Recluse registration witnesses, Fortune Teller Red Herring, selected registration binding, contradictory bundles, and standard/Baron profiles.

D4E evidence through 15 players supports direct topology feasibility without adding memoization or compiled constraints at this stage. At 15 players, topology upper bound is 5,460 and the measured standard-profile single-bundle path retained 4,004 keys at about 766 ms on the CI JVM.

A test-governance issue was found during T4: `Sde2D4ScaleBenchmarkTest` is a raw-enumerator T3 evidence harness and exceeded a dedicated 900-second CI diagnostic cap. It is now explicitly runnable as `:app:sde2D4ScaleBenchmark` and is not part of bounded `testFull`. The topology performance harness and exact differential regression evidence remain in affected/full validation.

Final executable acceptance head:

`9d619370a68b97107957a2dde504778f436dae12`

Final validation:

```text
R2 main-thread boundary        SUCCESS   run 35410931214
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS   run 35410931127
Android full + APK build       BUILD SUCCESSFUL in 7m 56s
```

PR #149 was user-authorized and squash-merged to live `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`.

Do not cut production recommendation policy yet and do not start SDE-3.

## 8. CURRENT — SDE-2D5 calibration

Authority: `docs/SDE_2D5_CALIBRATION_POLICY_EVIDENCE_FANOUT_AUDIT_2026-09-19.md`.

Branch: `sde-2d5-calibration-policy-evidence`.

D5A–D5E are complete.

Implemented evidence now includes:

- normalized raw-exact and topology-first projection through one SDE owner;
- representative 6 / 9 / 12 / 15 topology-first baselines for STANDARD and BARON profiles;
- Drunk HealthyCore / FullBundle / DrunkMarginal evidence for numeric, pair and Fortune Teller families;
- Demon-bluff per-role support, shared/union strategic support and deterministic low/high review contrasts;
- bounded exact proof that mechanical information gain may be topology-neutral;
- bounded exact proof that equal raw cardinality may hide different strategic-topology retention;
- a real seven-player healthy-bundle leave-one-out corpus exhibiting topology-neutral marginal information and near-raw/different-topology marginal contrasts.

Latest accepted D5E evidence:

```text
D5 calibration T3  35421032892  SUCCESS
ordinary CI        35421032988  SUCCESS
cleanup head       9b47095a906cec5b78807d9ea9f5be6c8079714d
FAST / R2 / gate                  SUCCESS
```

The temporary D5 validation workflow has been removed.

### D5F — CURRENT

Authority: `docs/SDE_2D5F_HUMAN_REVIEW_GATE_HOLDOUT_AUDIT_2026-09-19.md`.

**D5F-A is COMPLETE.**

The calibration-only review export now has:

- stable deterministic review IDs;
- typed baseline / Drunk / Demon-bluff / role-information records;
- baseline references that are explicitly non-labelable;
- every reviewable generated item initially `UNREVIEWED`;
- exact rational diagnostics preserved;
- deterministic real review selection spanning all player-count regimes and STANDARD/BARON references;
- sealed holdout exposure limited to the already-public scenario count.

Accepted evidence:

~~~text
D5F-A model T1          91d8256c9b483d2f70887f8a526aba175a70f073
D5F-A calibration T3    35423966647  SUCCESS
full CI                 35423966654  SUCCESS
Android full + APK                     BUILD SUCCESSFUL in 7m 12s
clean head              c17db4fc4789a5d0b11156a22a88da7971a37ef7
clean full CI           35424382474  SUCCESS
~~~

**D5F-B manifest infrastructure is COMPLETE; HUMAN REVIEW is NEXT.**

Implemented D5F-B contracts:

1. labels live in a separate manifest keyed by stable review ID;
2. unknown IDs are rejected;
3. baseline/reference IDs are rejected;
4. duplicate IDs are rejected;
5. non-`UNREVIEWED` labels require explicit reasons;
6. `UNREVIEWED` cannot carry inferred reasons;
7. `UNCERTAIN` stays explicit;
8. review-set completeness blocks D5F-C while any required item remains UNREVIEWED;
9. an all-UNREVIEWED template is generated and round-trips through the manifest codec;
10. the real manifest seed is persisted at:
   `app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`.

A D5E review-selection bug was found and fixed before human review: `4/4` and `5/5` were structurally unequal objects but mathematically equal ratios. Strategic retention comparisons now use cross multiplication. The corrected equal-raw-removal contrast is `4/7` versus `3/6`.

The first human-review pass exposed a second calibration problem: `Sde2D5DemonBluffRealCalibrationBuilder` still used the bounded D2D2 role subset. That made legal BARON / Drunk counterworld families unreachable and incorrectly produced zero Butler support. The core world enumerator already handled STANDARD + BARON and Drunk shown-role branching; the defect was the calibration caller's incomplete `roleDefinitions`.

Repair now accepted:

- Demon-bluff real calibration uses `TroubleBrewingFixtures.fullRoleDefinitions()`;
- `Sde2D5CalibrationRoleDomainContract` distinguishes `BOUNDED_FIXTURE` from `FULL_SCRIPT_DOMAIN`;
- D5F review material rejects Demon-bluff evidence unless it is `FULL_SCRIPT_DOMAIN`;
- T3 regression requires Butler to retain legal full-domain support;
- Drunk and role-information calibration fixtures were audited and were already full-domain.

The two old bluff review IDs are obsolete. The user's earlier provisional judgments on those obsolete records were not persisted and must not be transferred to the repaired records.

Current reviewable IDs:

~~~text
d5f:bluff:13af34bc8befea2f:r1
d5f:bluff:fe702b4aac3ca49a:r1
d5f:drunk:value-1
d5f:role-info:sig-00002562710d4654:marginal-1
d5f:role-info:sig-44f30b2b21cd7682:marginal-0
d5f:role-info:sig-796b6039ad4bb4aa:marginal-0
d5f:role-info:sig-89a6d878c5516337:marginal-1
d5f:role-info:sig-aab5a4dfa48cfce0:marginal-2
~~~

All eight are currently `UNREVIEWED`.

Latest corrected validation:

~~~text
full-domain repair evidence head  b740a0abe93165dc6d35647ba793a5e26ca8e00b
D5 calibration T3                 35442885568  SUCCESS
ordinary CI                       35442885562  SUCCESS
R2                                35442885576  SUCCESS
Android full + Debug APK / ASP / Clingo / CI gate  SUCCESS
~~~

The current manifest is valid but intentionally incomplete:
`isCompleteForGateDerivation == false`.

Next conversation should restart the **human judgment** from the repaired full-domain bluff records, then continue through the remaining unchanged records. Record labels + reasons in the source-controlled manifest only after each explicit human judgment, then validate completeness. Do not automatically infer the labels from metrics.

Do not begin D5F-C, derive thresholds, inspect sealed holdout diagnostics, or alter production selection during this review.

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

> **PR #149 is merged to `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`. SDE-2D5 is CURRENT on `sde-2d5-calibration-policy-evidence`; D5A–D5E + D5F-A are COMPLETE and D5F-B manifest infrastructure is COMPLETE. The Demon-bluff calibration role-domain defect found during human review is repaired and guarded by a FULL_SCRIPT_DOMAIN contract. HUMAN REVIEW restarts on the repaired 8-record all-UNREVIEWED manifest. Record explicit labels + reasons, then validate completeness. Do not begin D5F-C until the manifest is fully settled; do not open the sealed holdout, derive/freeze thresholds early, introduce an opaque scalar, cut production policy, or begin SDE-3.**
