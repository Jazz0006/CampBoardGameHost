# SDE-3D2 C4 — Truth danger / credibility disruption + contextual Red Herring architecture audit

> Date: 2026-09-26 Australia/Sydney  
> Branch inspected: `codex/sde-history-prefix-route-closure`  
> Live audit HEAD at start: `07563657b510a3ca027e163831dc8e60bbc0243a`  
> Status: **ARCHITECTURE / EVIDENCE / FANOUT AUDIT COMPLETE; PRODUCTION FEATURE EDITS BLOCKED ON CR-A / CR-B / CR-C**

## 1. Scope and gate

C4 / SDE-3D2 may define and audit the missing descriptive feature surface now, but the post-audit correctness route is authoritative for executable sequencing:

```text
CR-A impaired-narrative semantic correctness
-> CR-B typed game/request identity binding
-> CR-C strict nested replay decoding
-> accepted correctness-repair checkpoint
-> C4 production feature edits
```

This document therefore does not change production behavior.

It also does not authorize:

- a new policy version;
- a Red-Herring seat ordering;
- a role bonus;
- a neighbour bonus;
- a probability model for future Fortune Teller targets;
- a numeric weight or global scalar;
- automatic production cutover.

`BEGINNER_CONSERVATIVE_V1` remains immutable.

## 2. Current owner / fanout map

### 2.1 Red Herring legality

Canonical candidate legality remains in:

- `SetupCandidateGenerator.generateRedHerringCandidates(game)`.

The generator:

- emits a Red Herring candidate only when a Fortune Teller exists;
- restricts targets to actual-Good seats;
- owns stable candidate identity and reminder-effect semantics.

SDE must consume those already-legal candidates. It must not regenerate Red Herring legality.

### 2.2 Current Red Herring setup commitment

The currently reachable App commitment is external to SDE:

- `CampBoardGameHostApp.clocktowerRedHerring`;
- `onSelectRedHerring` / `onApplyRecommendation`;
- `ClocktowerRecoveryMechanics.redHerring` persistence / restore.

This is an existing setup commitment surface, not an SDE-owned state model.

SDE already has the correct generic reference vocabulary:

- `SdeCommittedDecisionInputKind.RED_HERRING`;
- `CommittedDecisionInputRef`;
- `SdeDecisionInputBindings`.

However, no current production caller constructs a `RED_HERRING` committed input binding. C4 should bind the existing commitment by reference rather than copy or relocate it.

### 2.3 Legacy Red Herring scoring is not C4 authority

The old setup authority still contains heuristic scoring:

- `SetupEvaluator` uses `red-herring-role-suitability`;
- `TroubleBrewingRecommendationMetadata.redHerringSuitability` is a named-role table;
- `RecommendationProfile` carries scalar penalties/weights.

These are legacy recommendation surfaces. C4 must not import their numbers or role ordering into SDE.

In particular, Evin's Chef case must not become a `Chef` bonus merely because the legacy table already assigns Chef a non-zero Red-Herring suitability.

### 2.4 Structured information legality

For dynamic information outputs, the semantic/legal owner remains:

- `InformationDecisionContext<T>`.

It owns:

- the already-validated legal candidate list;
- candidate IDs;
- revision binding;
- recommendation/manual confirmation over the same candidate space.

C4 must not duplicate this legality.

CR-B separately repairs the missing typed game/request identity boundary in this owner/replay path before new production feature edits.

### 2.5 Exact consequence authority

Exact consequence evaluation remains:

- `StorytellerDecisionEngine.evaluateExactConsequences`;
- `ExactHistoricalHypotheticalObservationBundleEvaluator`.

The exact layer owns world/cardinality/structure consequences. C4 may project descriptive features from this evidence but must not reimplement epistemic rules.

### 2.6 Existing confirmation and healthy-information projectors

Reusable production-owned descriptive seams already exist:

- `HistoricalConfirmationChainFeatureProjector`;
- `HistoricalHealthyInformationUtilityFeatureProjector`;
- `HealthyInformationUtilityFeaturesProjector`.

They already provide score-free provenance and structural facts such as:

- historical observation identity;
- source seat / source ability;
- exact independent constraint;
- support / contradiction;
- healthy route survival;
- current healthy route identity;
- exact before/after feasibility.

These are the preferred reusable inputs for truth/credibility projection at interaction lifecycle stages.

### 2.7 Night-1 setup product owner

For setup-precommit analysis, the existing whole-bundle census is:

- `TroubleBrewingFirstNightBundleCandidateSpaceAuditor`.

It already places the following in one candidate product:

- healthy pair information;
- fixed numeric information;
- Storyteller-controlled information;
- Red Herring;
- Demon bluffs.

Critically, Fortune Teller target selection is explicitly excluded because it is player-controlled.

This is the correct no-hindsight boundary for Red Herring C4 work.

### 2.8 Existing SDE feature slot

`DecisionFeatures` already contains:

```text
truthCredibility: FeatureProjection<TruthCredibilityFeatures>
```

but `TruthCredibilityFeatures` is currently only two string sets:

- `truthDangerReasonCodes`;
- `credibilityDisruptionReasonCodes`.

No production projector populates this field.

`BeginnerConservativeV1Policy` does not consume it for ordering. It only reports that unprojected preference dimensions remain limitations.

Therefore the slot is suitable for C4 evolution, but the current string-only model is not sufficient as the durable typed descriptive contract.

## 3. Evidence authority

### 3.1 Evin GOLD — E1 yes, E2 candidate, E3 no

Primary evidence explicitly records that Doug / Chef was chosen as Red Herring because Chef=1 was particularly damaging to Evil and a later Red-Herring hit could undermine confidence in Doug and the Chef information.

This supports at E1:

- truth danger as a real feature dimension;
- credibility disruption as a real downstream mechanism;
- Red Herring as a contextual setup precommit.

The reconstructed Evin game is suitable for bounded E2 semantic regression because:

- Red Herring legality is production-reconstructable;
- Doug's Chef information is functioning and rule-determined;
- Chef=1 is production-reconstructable.

It does **not** authorize at E3:

- always choose the strongest information role;
- prefer Chef;
- a scalar strength threshold;
- a deterministic suppression rule.

### 3.2 Ben — contextual target ecology, not a neighbour bonus

Ben's explicit rationale says Fortune Tellers often choose neighbours.

This supports a separate E1 conclusion:

- player-choice likelihood / target ecology can matter to Red Herring context.

It does not support:

- a fixed neighbour bonus;
- a general target probability;
- using the observed future Fortune Teller target as setup-time knowledge.

The material A Fond Farewell table remains Traveller-blocked for complete production counterfactual execution, so it is not the first C4 E2 regression.

### 3.3 SILVER

Current SILVER evidence supports:

- Red Herring has persistent downstream epistemic impact (`ct-04`);
- a real complete Night-1 bundle can contain a legal Red Herring (`ct-01`);
- healthy-information danger to Evil can matter on another Storyteller control surface (`ct-02`).

It does not establish candidate ordering.

## 4. No-hindsight invariant

The later observed Evin Fortune Teller target pair must not be used as an input to the earlier Red Herring precommit comparison.

The C4 model must distinguish:

1. a **potential credibility-disruption channel** created by the Red Herring mechanic; from
2. whether a player later chooses a target that activates that channel.

The first is a valid setup-precommit descriptive fact.

The second is player-controlled future behavior and is unavailable unless a separate, evidence-qualified target-ecology model exists.

Therefore C4 must not use:

- actual later FT target choices;
- a guessed hit probability;
- adjacency as a hidden proxy probability.

## 5. Proposed typed descriptive contract

### 5.1 Reuse source identity

Prefer reusing the existing generic source identity shape:

- `ConfirmationChannelRef.Source(sourceSeat, sourceAbility)`;

or extract an equivalent shared `InformationSourceRef` only if reuse would create an incorrect dependency.

Role identity is provenance, not a policy score.

### 5.2 Truth danger evidence

The smallest stable descriptive unit should answer:

> does this functioning healthy information source independently constrain the current Evil/world hypothesis, and by what exact structural consequence?

Recommended shape:

```text
TruthDangerSourceImpact
    source: typed information source ref
    exactWorldReduction: non-negative exact count
    strategicWorldKeysRemoved: set
    demonSeatsRemoved: set
    independentlyConstraining: derived boolean
```

This is descriptive. It does not define whether a given reduction is "large enough".

For setup-precommit projection, C4 should initially rely on information whose healthy output is already rule-determined from current canonical state. Storyteller-controlled information whose future value has not been committed must remain explicitly unresolved rather than silently neutral.

This generic rule covers Evin Chef without a Chef branch.

### 5.3 Credibility disruption evidence

Recommended shape:

```text
CredibilityDisruptionImpact
    committedInputRef: typed committed input reference
    affectedSource: typed information source ref
    mechanism: typed mechanism
```

The first mechanism may be:

```text
RED_HERRING_FALSE_POSITIVE
```

This describes the rule-level ability of the committed Red Herring to create a future Demon-like signal involving the affected source.

It does not say the player will select that seat.

### 5.4 TruthCredibilityFeatures

Recommended role-agnostic container:

```text
TruthCredibilityFeatures
    truthDangerSources: set/list of TruthDangerSourceImpact
    credibilityDisruptions: set/list of CredibilityDisruptionImpact
    unresolvedSourceRefs: explicit set where the healthy output is not yet committed/rule-determined
```

No score, weight, threshold, preference rank, role suitability or expected-hit probability belongs here.

## 6. Setup-precommit adapter boundary

C4 should not force Red Herring through `StructuredInformationShadowAdapter`, which is currently an interaction-level `InformationDecisionContext<T>` adapter.

A narrow setup adapter should instead:

1. consume already-legal Red Herring candidates from `SetupCandidateGenerator`;
2. preserve lifecycle `SdeDecisionLifecycleStage.SetupPrecommit`;
3. bind the external setup choice through `CommittedDecisionInputRef(kind = RED_HERRING)`;
4. use existing Night-1 producer/factor semantics to identify functioning healthy source channels without parsing display strings;
5. reuse exact/topology consequence authority for descriptive impact;
6. emit generic `TruthCredibilityFeatures`;
7. never select a Red Herring or commit setup state.

This activates the currently unused setup-precommit SDE lifecycle without moving setup ownership.

## 7. Existing fanout gap that must remain explicit

Current production `StructuredInformationShadowAdapter` projects:

- strategic;
- semantic truth;
- confirmation-chain;
- impaired narrative;
- healthy-information utility.

It does not currently project:

- truth/credibility;
- role-function exposure;
- bluff narrative;
- relationships;
- future flexibility.

In particular, `HistoricalRoleFunctionExposureFeatureProjector` exists and is exercised by tests, but currently has no production caller.

C4 implementation must therefore audit actual caller fanout after every shared `DecisionFeatures` change rather than infer runtime reachability from completion documents.

## 8. Trace schema boundary

`DecisionTrace` is currently schema version 1 and `DecisionTraceArchiveJsonCodec` strictly serializes the current string-only `TruthCredibilityFeatures` shape.

A richer typed persisted feature cannot silently change that JSON contract.

Before C4 production integration, choose one explicit compatibility path:

- version the trace schema and provide bounded v1 -> current compatibility for historical traces; or
- preserve the v1 wire shape only if the new typed representation can be encoded without losing typed/provenance semantics.

Stringly encoding structured provenance merely to avoid a schema change is rejected.

Because production V1 currently leaves `truthCredibility` unavailable, migration can preserve historical V1 semantics without creating a new policy version. Trace schema evolution and policy-version evolution are separate concerns.

## 9. Tests-first acceptance plan after CR-A/B/C

### T0-A — pure feature owner

Add a durable typed projector test proving:

- a functioning, independently constraining healthy source produces truth-danger impact;
- zero independent contribution is represented descriptively rather than assigned a penalty/reward;
- a Red-Herring committed input can name an affected source through typed credibility-disruption evidence;
- unresolved Storyteller-controlled future information remains explicit;
- no output field defines candidate ordering.

### T0-B — setup adapter / fanout

Prove:

- only candidates from `SetupCandidateGenerator` are consumed;
- lifecycle is `SetupPrecommit`;
- Red Herring is represented through `CommittedDecisionInputRef`;
- Fortune Teller future target choice is not an input;
- setup/App ownership is not mutated.

### E2 — Evin semantic regression

Using the existing primary-verified Evin reconstruction:

- Doug / seat 1 is a legal Red Herring candidate;
- Doug's functioning Chef output is rule-determined;
- its healthy information is represented as an independently constraining source when exact evidence shows that consequence;
- assigning Red Herring to seat 1 creates typed credibility-disruption evidence for that source;
- the regression never reads Claire's later observed target pair to compute setup-time truth danger.

The expected assertion is feature semantics, not "Evin's chosen candidate must rank first".

### V1 invariance

Injecting/projecting the new descriptive feature must not change:

- V1 rejection reasons;
- V1 survivor equivalence;
- `SEEDED_HASH_V1` behavior;
- frozen V1 evidence checkpoint.

No placeholder V2.

### Replay / persistence

If the persisted feature shape changes:

- add exact trace-schema compatibility tests;
- replay recomputes the feature from canonical input rather than trusting old persisted feature values;
- historical truth remains immutable.

## 10. Implementation checkpoint — 2026-09-26

The first policy-neutral C4 implementation slices are now accepted:

- **T0-A COMPLETE:** `TruthCredibilityFeatures` has typed truth-danger, credibility-disruption and unresolved-source material. The legacy string reason-code fields remain only for schema-v1 read compatibility; current schema-v2 persistence serializes typed provenance explicitly.
- **T0-B COMPLETE:** `RedHerringSetupPrecommitAdapter` consumes only `SetupCandidateGenerator.generateRedHerringCandidates(game)`, emits `SetupPrecommit` SDE candidates plus future `RED_HERRING` commitment identities, keeps bindings uncommitted, separates rule-determined and unresolved healthy sources, and preserves the Night-1 audit exclusion of player-controlled Fortune Teller targets.
- **Exact truth-danger projection COMPLETE:** `ExactTruthDangerSourceProjector` delegates numeric truth to existing rules semantics, proposition construction to the existing first-night materializer, and consequence evaluation to `ExactHistoricalHypotheticalObservationBundleEvaluator`. It produces exact-world reduction, removed strategic-world keys and removed Demon seats with no score or ordering.
- **Evin E2 COMPLETE:** the primary-verified Evin reconstruction proves seat 1 / Doug as a legal Red Herring target, Chef 1 as the rule-determined strategic truth anchor, typed Red-Herring credibility disruption for that source, explicit unresolved Washerwoman material, and no setup-time consumption of Claire's later Fortune Teller target pair.

Initial feature/projection acceptance evidence:

- validation-only Draft PR #158 exact head `f181780bac2ff7759a880821ab664d7d23952131`;
- CI #3468 and R2 #3221 SUCCESS;
- formal implementation checkpoint `d39b336145b19ba29690a5b2dd14ed38273a6cc5`.

The persistence/invariance gate is also now accepted:

- **V1 invariance COMPLETE:** projecting typed truth/credibility does not alter V1 rejection reasons, survivor equivalence, `SEEDED_HASH_V1` selection, or the frozen evidence checkpoint. Limitation metadata may accurately shrink when a previously unprojected dimension becomes available, but policy behavior does not change.
- **DecisionTrace schema evolution COMPLETE:** `DecisionTrace.CURRENT_SCHEMA_VERSION == 2`; the archive envelope remains format v1. Schema-v2 encodes typed truth-danger source impacts, typed credibility-disruption provenance, unresolved source refs and legacy reason-code fields deterministically.
- **Bounded schema-v1 migration COMPLETE:** persisted trace schema v1 keeps its strict legacy truth/credibility shape, is accepted read-only, and migrates into the current in-memory schema with typed sets empty rather than fabricated.
- **Replay recomputation COMPLETE:** multi-policy replay continues to use freshly recomputed canonical feature input; the persisted historical feature snapshot is identity/diagnostic history, not policy input.

Latest acceptance evidence:

- validation-only Draft PR #158 exact head: `1299fba4fe9f884e468f85639e56ec0b0926794b`;
- CI #3470: Android FULL + debug APK, ASP contracts, Real Clingo and final CI gate SUCCESS;
- R2 #3223 SUCCESS;
- 6/6 checks successful, no unresolved review threads;
- formal schema/invariance checkpoint: `5e563b377df8988cca805d8e34aef7e5823e209f`.

The validation-only branch briefly contained one malformed large-file sync commit whose codec text included display line-number prefixes; Android compile rejected it. The next validation commit restored the exact clean source and passed the full gate. This was a transport/synchronization defect, not an accepted code state and not part of the formal branch.

The bounded production fanout audit is now complete for the setup-precommit lifecycle:

- `StructuredInformationShadowAdapter` remains interaction-only; Red Herring is not forced through that boundary.
- `ClocktowerRecommendationCoordinator.evaluateSetupRedHerringShadow` mirrors the existing Demon-bluff setup-shadow ownership pattern and is the narrow production seam for C4 setup-precommit diagnostics.
- The seam consumes `RedHerringSetupPrecommitAdapter` output, evaluates rule-determined healthy sources exactly once, and projects the resulting truth-danger impacts plus candidate-specific credibility disruptions into `DecisionFeatureEvaluation`.
- The exact existing `SetupRecommendationService.ConstrainedResult` is retained by identity; SDE cannot rewrite legacy plans, scores, selection, setup commitment or presentation.
- Visible legacy Red-Herring targets are correlated back to setup-owned legal candidate IDs and must be present in the SDE diagnostic domain.
- The App setup-prewarm path is intentionally not synchronously wired to exact C4 evaluation because it currently receives only `SetupCoordinationRequest` and adding an exact-evaluation dependency there would alter a latency/cutover boundary rather than remain a bounded diagnostic seam.
- Validation exact head `c9835a7dfdfe04e65e7b934784cbc91e1a0f9fd0` passed CI #3471 and R2 #3224, including Android FULL, ASP contracts, Real Clingo and final CI gate.
- Formal fanout code checkpoint: `77369a893bb457c8720df00b907e50a4e2550a45`.

### Committed-input ownership closure — 2026-09-27

C4's final ownership gap is now closed without adding a second setup owner or an artificial runtime consumer:

- `RedHerringCommittedInputBindingAdapter` resolves the App-owned persisted Red-Herring player name through the canonical `GameState` to one seat, then correlates that seat to the exact setup-precommit candidate's existing `CommittedDecisionInputRef`;
- the adapter never parses candidate IDs, never regenerates Red-Herring legality, never copies the persisted setup value, and never introduces player-controlled Fortune Teller target inputs;
- unknown names, duplicate canonical names, and targets outside the setup-owned legal candidate domain fail closed;
- the returned `SdeDecisionInputBindings.Captured` contains exactly the `RED_HERRING` committed reference and an empty player-controlled set;
- repository search found no real current production consumer of this binding. Therefore C4 deliberately adds **no App/UI/runtime wiring** for it. The seam remains available for the first real downstream SDE consumer rather than manufacturing dead state propagation.

Validation-only Draft PR #158 exact head `564b6d93ef3f08c9331512c186172505485b12bb` contains this exact adapter/test material and passed CI #3472 plus R2 #3225 with Android FULL, ASP contracts, Real Clingo and final CI gate all successful. Formal continuation commits `fd40ad1e966aa4ed499d9844641170baa7632101` and `d9dd50421f9b7b9d4be9f9173acef81e97320afe` persist the seam and its regressions on top of the accepted test-hygiene baseline.

**C4 / SDE-3D2 is therefore COMPLETE as a policy-neutral feature/composition slice.** It does not authorize V2, a Red-Herring preference score, automatic cutover, or broader runtime scope.

## 11. C4 completion conclusion

C4 now reuses the existing Red-Herring legality owner, setup-precommit lifecycle, exact consequence authority, typed committed-input reference, canonical history/provenance and DecisionTrace/replay surfaces without creating a parallel state owner.

The completed data flow is:

```text
existing legal Red Herring candidate
+ existing setup commitment reference
+ rule-determined / committed healthy information source
+ exact descriptive consequence
-> typed truth-danger / credibility-disruption feature
-> trace/replay
-> V1 remains policy-neutral
```

The App-owned committed Red Herring can be correlated to the exact precommit reference through a typed fail-closed seam, but there is deliberately no runtime consumer until one is required by a real SDE decision surface. No heuristic score, named-role preference, fixture-specific rule, Fortune Teller hindsight, V2 policy, or automatic cutover was introduced.

CR-A / CR-B / CR-C are accepted and **C4 / SDE-3D2 is COMPLETE**. IF-D durable App replay reconstruction and RH-E persistence/timing hardening remain separate integration follow-ups; evidence-backed policy change remains gated on qualifying E3/E4 evidence.

## 12. Combined exact-tree acceptance — 2026-09-27

The final C4 executable tree, including the accepted test-hygiene routing/retirement changes, is formal checkpoint `21206a8ca95896ffad83eaba5695a3a571c6cd74` with tree `a4701e0f4873ddd629dfa11163a94d2aa0cb9b8b`.

Validation-only Draft PR #160 exact head `20eeb85790e591fe66363cd1b509bf5dda225342` uses current `main` as its direct parent and carries that exact same tree. It passed:

- CI #3473 — Classify changes, Android FULL + debug APK, ASP contracts, Real Clingo cross-validation and final CI gate all SUCCESS;
- R2 main-thread boundary #3226 — SUCCESS;
- 6/6 checks successful with no unresolved review threads.

PR #160 is validation-only and must not be merged. This acceptance closes C4 engineering/validation. IF-D and RH-E remain separate integration-hardening follow-ups, and C5 remains evidence-gated.
