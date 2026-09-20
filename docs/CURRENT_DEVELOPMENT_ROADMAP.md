# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-19 Australia/Sydney  
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
SDE-2D1 Drunk whole-bundle completion                   COMPLETE / PR #145 merged
SDE-2D2 Demon bluff joint-output migration              COMPLETE / PR #146 merged
SDE-2D3 strategic-world quotient                        COMPLETE / PR #147 merged
SDE-2D4 5–15 player generalization / performance        COMPLETE / PR #149 merged
SDE-2D5 calibration / policy evidence                   CURRENT
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

### 2.1 SDE-2D1 — Drunk whole-bundle completion — COMPLETE ON MAIN

PR #145 was user-authorized and merged to `main` as `6fc0d99f1a250b91925280ed12ec0b199220b060`. Its final executable acceptance head remains:

`27b17d5e0eedea3367f5a1b69ed2093fb958f1af`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

Implemented result:

- supported Drunk pair/numeric/Fortune-Teller information is now a first-class whole-bundle candidate;
- public claims admit hidden Drunk/Poisoner malfunction explanations without leaking hidden state;
- exact worlds carry latent non-recipient Drunk shown roles;
- shared exact evaluation exposes `HealthyCore / FullBundle / DrunkMarginal`;
- Fortune Teller target pairs remain player-controlled robustness inputs;
- exact-world finite-resource tests protect one Drunk per world and Trouble Brewing Poisoner/Baron constraints;
- no production recommendation cutover occurred.

Frozen direction:

- the Drunk shown role remains PERSISTENT setup/session truth and is never reselected by SDE;
- the unshown clue is a PLANNED Storyteller decision;
- supported shown information roles generate all surface-valid outputs, including accidentally truthful outputs;
- unreliable clue choice is evaluated jointly with the rest of Night 1;
- evaluation separates `HealthyCore`, `FullBundle` and `DrunkMarginal`;
- impaired public-claim semantics must distinguish functioning truth from malfunctioning permissiveness without leaking hidden Drunk/Poisoner state.

### 2.2 SDE-2D2 — Demon bluff joint-output migration — COMPLETE ON MAIN

Authority:

- `docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
- PR #146 merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`;
- final executable acceptance head `6acb708bd734d36d024240f0dea213a882a7714b`.

Validation:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

Implemented result:

- `SetupCandidateGenerator` remains the sole Demon-bluff legality owner;
- already-legal triplets project losslessly into the SDE candidate shape;
- each distinct bluff role is evaluated as a strict hypothetical `ShownRoleAt(actualDemonSeat, role)` against the supplied public whole-bundle facts;
- legal triplets reuse shared per-role exact support rather than re-running a full exact evaluation per triplet;
- triplet diagnostics expose supported roles, evil-team topology union/intersection and role-topology diversity;
- the pristine exact evaluator now shares one mechanical-world generation pass across different strict shown-role values for the same constrained seat set while retaining exact per-query role checks;
- applied/recovered Demon bluffs remain persistent and locked bluffs are rejected from uncommitted replanning;
- the setup shadow path preserves the exact legacy visible recommendation object and records differential candidate IDs without changing selection;
- bluff-triplet comparison canonicalizes role ordering across the legal-candidate and legacy-plan producers;
- the real exact Demon-bluff contract is classified as affected T2/T3 and remains in `:app:testFull`.

Production cutover has **not** occurred. `SetupRecommendationService` and its `demon-bluff-ease / bluffDifficulty` compatibility heuristic remain unchanged and visible production behavior remains legacy-owned pending the later explicit cutover stage.

PR #146 was user-authorized and merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`.

### 2.3 SDE-2D3 — strategic-world quotient — COMPLETE ON MAIN

Authority:

- `docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`;
- branch `sde-2d3-strategic-world-quotient` from merged PR #146 main.

Representation/fanout audit and the first exact quotient migration are complete.

Final executable acceptance head:

`2d491663a899d048b4b69522e9cdde700effa85b`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
```

PR #147 was user-authorized and merged to live `main` as:

`2cab06efeee5692024e065c632deefe765ca1618`

Implemented result:

- existing `possibleDemonSeats` + `evilTeamSeatConfigurations` remain compatibility marginals/coarse sets;
- `StrategicWorldKey` preserves the joint setup Demon↔Minion topology that those fields lose;
- the key is projected from immutable `rolesBySeat` using `RoleDefinition.type`;
- `ExactWorldStructureDiagnostics.strategicWorldKeys` is accumulated in the existing exact world scan with no second enumeration;
- bounded semantics prove role-permutation collapse, Demon/Minion swap distinction, type-definition ownership and Imp-succession setup stability;
- Demon-bluff triplet diagnostics now add precise strategic-key union/intersection/pattern beside the existing coarse topology fields;
- production SDE fanout audit found no second coarse-topology consumer requiring migration;
- no recommendation-owned solver, `PlayerWorldSet`/ZDD widening, state mutation or production policy cutover was introduced;
- historical active-Demon state remains a separate cross-night dimension.

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

### 2.4 SDE-2D4 — 5–15 player generalization and cost — COMPLETE ON DRAFT PR #149

PR #147 was user-authorized and merged to live `main` as `2cab06efeee5692024e065c632deefe765ca1618`.

D2D4 is complete on branch `sde-2d4-5-15-validation-performance`, draft PR #149.

Authority:

- `docs/SDE_2D4_5_15_CORRECTNESS_PERFORMANCE_NORMALIZED_METRICS_AUDIT_2026-09-18.md`;
- `docs/SDE_2D4_TOPOLOGY_FIRST_FEASIBILITY_FANOUT_AUDIT_2026-09-18.md`;
- `docs/SDE_2D4_TOPOLOGY_FIRST_BUNDLE_INTEGRATION_AUDIT_2026-09-18.md`.

Implemented and accepted:

```text
D4A   normalized strategic diagnostics
D4B   reproducible source-derived / bounded-prefix scale evidence
D4C1  exact StrategicWorldKey topology domain
D4C2  setup role/type/shown-role witness existence
D4C3  Drunk / Poisoner finite-resource AbilityState feasibility
D4C4a identity observations + Spy/Recluse registration witness semantics
D4C4b Chef / Empath / Fortune Teller / Red Herring topology semantics
D4C4c composable AnyOf / AllOf / supported Not observation branches
D4C5  parallel exact strategic whole-bundle evaluator with shared witness resources
D4D   same-world whole-bundle exhaustive differential parity
D4E   measured 5–15 topology-bundle performance matrix
```

The setup witness tail uses an exact type-quota → role → seat max-flow existence proof rather than Townsfolk/Outsider seat-split enumeration. The bounded exhaustive oracle caught and fixed a real standard-profile Baron contradiction bug; do not restore the old Elvis fallback.

D4D proves one shared mechanical witness across complete bundles, including role identity conflict, one Poisoner target, one Drunk resource, Spy/Recluse registration witnesses, Fortune Teller Red Herring, selected registration binding, contradictory bundles, and standard/Baron profiles.

D4E measured direct topology feasibility through 15 players. The 15-player standard-profile run had a topology upper bound of 5,460, 4,004 retained strategic keys, and about 766 ms single-bundle CI-JVM latency. The evidence does not justify memoization or compiled constraints at this stage.

The raw mechanical enumerator remains a bounded correctness oracle, not the production discovery representation. `WorldCardinality.Exact` retains exact mechanical-world semantics. No production recommendation consumer has cut over.

A T4 governance issue was also resolved: `Sde2D4ScaleBenchmarkTest` is an explicit T3 raw-enumerator evidence harness, not a bounded regression contract. It remains runnable as `:app:sde2D4ScaleBenchmark` but is outside `testFull`; the bounded topology performance and exact differential evidence remain in full/affected validation.

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

PR #149 was user-authorized and squash-merged to `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`.

### 2.5 SDE-2D5 — calibration / policy evidence — CURRENT (D5A–E + D5F-A COMPLETE; D5F-B HUMAN REVIEW PENDING)

Authority: `docs/SDE_2D5_CALIBRATION_POLICY_EVIDENCE_FANOUT_AUDIT_2026-09-19.md`.

Branch: `sde-2d5-calibration-policy-evidence`.

Completed:

```text
D5A  normalized raw-exact / topology-first projection seam
D5B  5–15 cross-regime STANDARD/BARON calibration evidence
D5C  Drunk HealthyCore / FullBundle / DrunkMarginal calibration projection
D5D  Demon-bluff role support + shared/union contrast evidence
D5E  role-information utility vs strategic-topology contrast evidence
```

Key accepted D5E evidence:

- dedicated calibration T3 run `35421032892` SUCCESS;
- ordinary CI run `35421032988` SUCCESS;
- cleanup head `9b47095a906cec5b78807d9ea9f5be6c8079714d` FAST / R2 / CI gate SUCCESS.

D5F authority: `docs/SDE_2D5F_HUMAN_REVIEW_GATE_HOLDOUT_AUDIT_2026-09-19.md`.

D5F-A is COMPLETE.

D5F-B manifest infrastructure is also complete, but **human review is still pending**.

Current review state:

- source-controlled manifest:
  `app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`;
- exactly 8 reviewable records;
- all 8 remain `UNREVIEWED`;
- manifest is valid but `isCompleteForGateDerivation == false`;
- baseline reference records remain non-labelable;
- sealed holdout exposure remains scenario-count only.

The D5E review selector was corrected to compare strategic ratios by mathematical value, so equivalent ratios such as `4/4` and `5/5` are no longer treated as different. Corrected real calibration evidence includes an equal-raw-removal contrast with `4/7` versus `3/6` topology retention.

Human review then exposed a separate D5 Demon-bluff calibration-domain defect: the real calibration fixture used a bounded D2D2 role subset, which excluded legal BARON / Drunk counterworld families. Core exact world enumeration was not defective; it was exact inside an incomplete caller-supplied catalog. D5 policy calibration now requires `FULL_SCRIPT_DOMAIN`, the bluff fixture uses all Trouble Brewing roles, D5F rejects bounded bluff evidence, and a regression protects legal Butler support.

The repair invalidated the two old bluff review IDs. No old human judgment was persisted. Repaired bluff IDs are:

~~~text
d5f:bluff:13af34bc8befea2f:r1
d5f:bluff:fe702b4aac3ca49a:r1
~~~

Latest corrected checkpoint:

~~~text
full-domain repair evidence head  b740a0abe93165dc6d35647ba793a5e26ca8e00b
D5 calibration T3                35442885568  SUCCESS
ordinary CI                      35442885562  SUCCESS
R2                                35442885576  SUCCESS
Android full + Debug APK / ASP / Clingo / CI gate  SUCCESS
~~~

**D5F-B2 external-human evidence pilot is now ACTIVE in parallel with controlled human review.**

Authority:

- `docs/SDE_2D5F_EXTERNAL_HUMAN_EVIDENCE_PILOT_2026-09-20.md`.

First external Trouble Brewing pilot:

- public ClockTracker 14-player game `ffb40a93-3d7b-42c4-bba8-bc9c363dcd30`;
- external-human workflow `35446943045` SUCCESS;
- ordinary CI `35446945108` SUCCESS;
- R2 `35446945073` SUCCESS;
- all observed Night-1 decisions were expressible by current legal domains and retained feasible topology witnesses;
- observed Drunk false Empath `0` sat inside a non-maximal misinformation-pressure band;
- observed Chef / Investigator / Saint bluffs demonstrated that shared/union support is **not** a monotone BEGINNER-quality objective.

BEGINNER-policy hypothesis is now explicitly multi-axis:

- practical bluffability / novice execution burden;
- real-information anchoring;
- narrative-route diversity;
- strategic-world coverage complementarity after a coherence floor;
- bounded Night-1 strategic pressure rather than maximum good-team world collapse.

Do **not** freeze `shared/union` as "higher is better". Treat it, if calibration supports it, as a coherence / fragility floor only.

D5F-B2 remains positive-unlabeled external evidence. It must not auto-label unselected candidates, modify the eight-item manifest, expose the sealed holdout, derive/freeze thresholds, or alter production selection.

External evidence is now persisted in:
- docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv;
- docs/SDE_2D5F_EXTERNAL_HUMAN_REPEAT_SIGNAL_AUDIT_2026-09-20.md.
- docs/SDE_2D5F_BEGINNER_POLICY_CONTROL_SURFACE_AUDIT_2026-09-20.md.

The expanded sweep currently classifies healthy-truth danger / Evil-topology coupling, cross-channel narrative coherence, temporal consistency / impairment detectability, and minimum healthy-information floor / Night-1 pressure band as STRONG_REPEAT. Healthy-truth danger is now explicitly stage-bound: PRE-GAME uses it as `SetupTruthExposureRisk`; post-setup SDE may only use `CounterfactualHealthyTruthDanger` as context when the role is already legitimately impaired, and may not retroactively change Drunk identity/shown role. Red-Herring trajectory, future correction capacity, narrative-route diversity and confirmation-chain suppression remain REPEATED_EMERGING. Shared-support floor threshold and independent strategic-coverage-complementarity reward remain TENTATIVE.

Next external-evidence action: continue targeted exact-seat / exact-Night-1 recovery, especially cases that can discriminate the emerging/tentative axes; reuse the catalog later for cross-night Drunk/Poisoned selection.

**D5F-B3 policy-model correction is now REQUIRED before human review.**

Authority:

- `docs/SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`.

Audit conclusion:

- D2D1-D2D4 exact/topology architecture remains valid;
- D5A/B remain valid;
- D5C/D/E evidence remains useful but incomplete for policy calibration;
- the current eight-record v1 manifest must remain UNREVIEWED and must not feed D5F-C;
- fixed impaired false-family preference is a legacy policy contradiction;
- shared/union remains diagnostic/coherence evidence, not a monotone bluff-quality target;
- isolated strong healthy clues remain legitimate game variance; do not add automatic seat reroll for a lone Empath 2;
- human review resumes only after a corrected v2 review schema/manifest covers truth+false Drunk contrasts, bluff execution/narrative dimensions and bundle confirmation-chain evidence.

**NEXT: implement D5F-B3 policy-model correction, regenerate v2 review material/manifest, then resume human review.**

Do not automatically infer labels. Do not begin D5F-C until every required item is settled and the manifest validator reports complete.

Production recommendation selection remains unchanged.

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
CURRENT  SDE-2D5 — calibration / policy evidence
THEN     SDE-3   — cross-night impaired / registration decisions
THEN     SDE-4   — production cutover + legacy heuristic retirement
```

Do not begin SDE-3 until D5F human review, frozen interpretable gate evidence, sealed-holdout acceptance and the final D2D5 T4 checkpoint are complete.

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
12. `docs/SDE_2D2_DEMON_BLUFF_JOINT_OUTPUT_FANOUT_OWNERSHIP_AUDIT_2026-09-18.md`;
13. `docs/SDE_2D3_STRATEGIC_WORLD_QUOTIENT_FANOUT_REPRESENTATION_AUDIT_2026-09-18.md`;
14. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
15. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
16. query live `main` and current checks.

## 10. Stable rule

> **PR #149 is merged to `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`. SDE-2D5 remains CURRENT on `sde-2d5-calibration-policy-evidence`; D5A–D5E and D5F-A are COMPLETE, and D5F-B manifest infrastructure is COMPLETE. Demon-bluff calibration now has a FULL_SCRIPT_DOMAIN gate after the bounded-domain defect found during human review. The repaired 8-record manifest is valid and fully UNREVIEWED; human review restarts from the repaired bluff evidence. D5F-C is blocked until the manifest is fully settled. Do not infer labels automatically, derive/freeze gates early, inspect sealed holdout diagnostics, use one opaque global scalar, cut production policy, or begin SDE-3 until D5F + final T4 are complete.**
