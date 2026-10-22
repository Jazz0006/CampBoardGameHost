# DLB Drunk Recommendation Context Capability Contract Audit — 2026-09-30

> Status: **CURRENT CAPABILITY / EVIDENCE CONTRACT — NO PRODUCTION POLICY AUTHORIZED**
> Baseline: `main@c462cfd55a874cc767b41c658ff0dbd8e76124d2`
> Trigger: first Drunk-assignment production cutover audit completed **NOT PASSED**
> Production code changed by this audit: **none**

## 1. Decision

Do **not** add a generic `RecommendationContext`, a placeholder Drunk production request DTO, or a new mutable player-profile owner now.

The repository already has the two semantics needed to continue safely:

1. rules/setup owners provide the exact legal Drunk candidate domain and immutable candidate-specific hypothetical state/consequence evidence;
2. `FeatureProjection.Projected / Unavailable(reason)` already models “known value” versus “capability/input not available” without converting absence into a neutral score.

The missing work is therefore not “invent a context object”. It is to define, for the future Drunk production policy, which existing facts are mechanically required, which descriptive features are optional/enrichment, and which enrichment families still lack an authoritative producer.

This audit freezes that boundary without assigning ranking semantics.

## 2. Required context — correctness / identity

A future Drunk recommendation request must always be grounded in the following authoritative inputs.

### 2.1 Decision identity and freshness

Owned by the current setup/SDE lifecycle:

- game / setup decision identity;
- `InformationDecisionRevision` or equivalent current revision identity;
- SetupPrecommit lifecycle identity.

A recommendation computed for another revision is stale and cannot be committed.

### 2.2 Rules-owned legal candidate domain

Owned by `TroubleBrewingDrunkCandidateDomain`.

Required properties:

- complete current legal dealt-Townsfolk candidates;
- stable candidate identity / seat / shown role;
- no SDE regeneration of legality.

The recommendation engine may rank or defer among supplied legal candidates only.

### 2.3 Candidate-specific hypothetical setup projection

Owned by `TroubleBrewingDrunkHypotheticalProjector`.

For each legal candidate, the future request can consume an immutable candidate-specific projected setup/consequence envelope. The recommendation engine must not mutate canonical setup or reimplement the projector.

These three families are **required for request correctness**. Missing them means “no valid recommendation request”, not a lower-quality recommendation.

## 3. Available descriptive enrichment — already projected

The current dedicated `DrunkAssignmentFeatureEvaluation` is descriptive and score-free.

### 3.1 Topology

`DrunkAssignmentTopologyFeatures` is currently Projected for every legal candidate:

- previous/next seat;
- adjacent Evil seats;
- adjacent Demon seats;
- adjacent Minion seats.

Status: **AVAILABLE / OPTIONAL ENRICHMENT UNTIL A POLICY EXPLICITLY REQUIRES IT**.

The existence of this feature does not itself authorize “prefer adjacent to Demon”.

### 3.2 First-night information opportunity

`DrunkAssignmentFirstNightInformationOpportunityFeatures` is currently Projected from the accepted first-night ecology/consequence owner:

- known first-night information factors;
- Storyteller-controlled route presence;
- multi-output Storyteller-controlled route presence.

Status: **AVAILABLE / OPTIONAL ENRICHMENT UNTIL A POLICY EXPLICITLY REQUIRES IT**.

The current projection is exact descriptive structure, not a preference for “more” or “less” misinformation opportunity.

### 3.3 Limitations

`DrunkAssignmentFeatureLimitations` already records excluded player-controlled elements and deferred complexity.

Status: **REQUIRED DIAGNOSTIC METADATA FOR ANY POLICY THAT CONSUMES THE ASSOCIATED FEATURE SURFACE**.

A future policy must not silently score through an excluded/deferred capability.

## 4. Explicitly unavailable enrichment

### 4.1 Longitudinal impaired-narrative opportunity

Current projection:

`FeatureProjection.Unavailable(MISSING_CAPABILITY)`.

Reason:

- accepted DLB-3B consequence owner is first-night-only;
- no broader authoritative multi-night consequence owner has been introduced.

Status: **UNAVAILABLE / DO NOT DEFAULT TO ZERO**.

This field may become Projected only when a broader history-aware consequence owner exists and is evidence-validated.

### 4.2 Player-level context

EvidenceLab creator/experienced-Storyteller guidance supports player-first assignment and examples where player experience changes an otherwise similar setup decision.

Current Host Drunk-assignment implementation has no player-experience / games-played producer in this decision surface.

Status: **OPTIONAL ENRICHMENT FAMILY; FUTURE PER-PLAYER PRODUCER PLANNED**.

Do not create a player-profile subsystem solely to satisfy this audit. The 2026-10-04 product-owner decision now defines the future player-level vocabulary as per-player `BEGINNER / NORMAL / EXPERT` and authorizes **NORMAL as the current/default value for players without an explicit profile setting**. This supersedes the earlier provisional prohibition on treating missing player context as an average/default player.

A policy must still explicitly declare whether it consumes player experience. Policies that do not consume it remain invariant to the enrichment. Policies that do consume it must freeze the decision-time per-player values for replay/export and must not use player experience for rules legality.

### 4.3 Cross-game recent-role history

The long-horizon architecture identifies recent role history as possible enrichment.

Current Drunk-assignment selection surface does not need it for legality or mechanical correctness, and no evidence-backed production preference has been authorized.

Status: **PROPOSED OPTIONAL ENRICHMENT / NO CURRENT POLICY SEMANTICS**.

Do not add it to a production request until a policy/evidence slice needs it.

## 5. Why no new production DTO is added now

A production request type should encode a policy’s stable dependency contract.

At present, the missing policy has not yet answered:

- whether topology is required or optional;
- whether first-night misinformation opportunity is required or optional;
- whether player experience changes eligibility to recommend or only ranking sophistication;
- whether longitudinal narrative must defer the whole decision or may be absent under a bounded policy;
- what candidate-equivalence / tie semantics are evidence-authorized.

Encoding those choices now in a request DTO would turn an architecture proposal into accidental policy authority.

Therefore the stable implementation boundary remains:

```text
rules-owned legal candidates
+ immutable candidate projections
+ score-free DrunkAssignmentFeatureEvaluation
+ explicit FeatureProjection.Unavailable reasons
-> shadow/evidence analysis only
```

A versioned production selection contract should introduce its own typed required/enrichment request only after at least one concrete preference predicate is evidence-authorized. Before that production request is introduced, TBGS-0/1 establish the standard `TroubleBrewingGameSnapshotV1` read boundary so the request does not create another recommendation-specific game-state variant.

## 6. Targeted EvidenceLab handoff

The current downstream blocker is no longer “find more games with a Drunk”.

The requested evidence shape is **candidate-comparison / ordering evidence at assignment time**.

Highest-value evidence:

1. a qualified Storyteller explicitly compares two or more plausible Drunk candidates in the same already-fixed setup;
2. an explicit rejection such as “I would not make X the Drunk here because …” paired with the selected candidate;
3. a bounded creator/expert statement that establishes a conditional preference over alternatives, with the condition recoverable from assignment-time context;
4. player-context examples only when the relevant player-experience fact and the compared alternatives are explicit.

For each useful source, preserve:

- assignment-time historical/setup prefix;
- observed selected candidate;
- explicitly compared/rejected alternative(s);
- source-backed rationale for the comparison;
- which context variables the rationale actually used;
- provenance / Storyteller qualification;
- UNKNOWN for all unobserved alternatives.

Do not infer a full ranking from one observed winner.

## 7. Candidate evidence predicates to test — not yet policy rules

Current evidence suggests several **questions**, not authorized rules:

- Does adjacent-Demon topology ever produce an explicit preference over another legal candidate?
- Does a shown role with richer Storyteller-controlled first-night output become preferred over a plausible alternative, and under what table/player context?
- Does player experience reverse a candidate comparison in otherwise similar topology?
- When is preserving a believable multi-night narrative explicitly preferred over a stronger first-night effect?

Evidence collection should seek explicit comparative answers to these questions.

Do not code any answer until evidence actually supports it.

## 8. Re-entry criteria for production-policy work

The next Host production-policy slice may begin only when at least one bounded preference semantic can be stated with:

- a named evidence source / provenance;
- an assignment-time condition available to Host;
- a comparison or rejection over plausible alternatives;
- explicit behavior when the required/optional context is unavailable;
- no need to mutate `BEGINNER_CONSERVATIVE_V1`.

At that point:

1. consume the already-established `TroubleBrewingGameSnapshotV1` + typed Drunk decision boundary from TBGS-0/1;
2. define a new versioned Drunk production-selection contract;
3. define that version’s typed required/enrichment request without embedding another `DynamicGameState`-style owner;
4. keep unavailable enrichment explicit;
5. replay it against C1 cases without treating expert historical choice as an oracle;
6. re-run the production cutover gate.

## 9. Immediate route

```text
cutover gate audit COMPLETE / NOT PASSED
-> recommendation-context capability contract COMPLETE / policy-neutral
-> TBGS-0 canonical TB snapshot contract
-> TBGS-1 Drunk snapshot vertical slice / EvidenceLab interoperability
|| EvidenceLab targeted candidate-comparison acquisition continues
-> bounded preference evidence, if found
-> new versioned Drunk production policy/request over snapshot + typed request
-> shadow replay
-> re-run cutover gate
-> Beginner automatic authority only if PASS
```

Snapshot-route authority: `docs/TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md`.

Until then:

- Beginner stays on `CompatibilityImmediate`;
- Experienced keeps manual legal-domain selection;
- `DRUNK_ASSIGNMENT_SHADOW_V1` remains deferral-only;
- DLB-6 remains blocked;
- C5 / BEGINNER_CONSERVATIVE_V2 remains a separate gate.
