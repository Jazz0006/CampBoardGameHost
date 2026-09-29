# DLB-3B2 Dedicated Drunk-Assignment Feature Surface — Architecture Audit — 2026-09-29

> Status: **IMPLEMENTATION CONTRACT / TEST-FIRST**
>
> Baseline: `main@81b5dd2bd1c4c554bec631efe0254b5feec3fadc`
>
> Depends on: DLB-3B1 accepted score-free `DrunkSetupConsequenceEnvelope`.

## 1. Purpose

DLB-3B1 established candidate-local consequence facts without feeding them into the ordinary SDE `DecisionFeatures` contract.

DLB-3B2 adds a dedicated, typed Drunk-assignment feature surface over that accepted envelope.

This surface exists because Drunk-seat assignment is a setup choice, not a first-night information-output choice. Reusing ordinary `DecisionFeatures` would make frozen `BEGINNER_CONSERVATIVE_V1` appear to understand semantics it was never versioned to consume.

Therefore DLB-3B2 remains shadow-only and non-authoritative.

## 2. Evidence-qualified feature families

The current evidence supports two descriptive families.

### 2.1 Setup-topology consequence

G10 Game 2 explicitly ties the observed Drunk assignment to the candidate's adjacency to the Demon.

The feature surface may therefore expose exact candidate-local topology:

- previous / next seat;
- adjacent Evil seats;
- adjacent Demon seats;
- adjacent Minion seats.

This is factual state, not a preference.

### 2.2 First-night impaired-information opportunity

A Fond Farewell explicitly ties the observed Chef assignment to the ability to give a deliberately extreme misinformation result.

The accepted 3B1 envelope already exposes candidate-seat first-night public-good-info factors and their legal option-domain shape.

The feature surface may therefore expose, per candidate:

- stable factor ID;
- factor kind;
- control owner;
- legal option count;
- whether multiple legal outputs exist.

This remains descriptive. Multiple outputs are not a numeric quality score.

### 2.3 Longitudinal narrative opportunity

The current 3B1 owner is a first-night ecology census. It does not own multi-night narrative consequences.

Therefore DLB-3B2 must expose longitudinal narrative opportunity as explicitly unavailable with:

`FeatureUnavailableReason.MISSING_CAPABILITY`

Do not infer later-night flexibility from role name, first-night option count, or the fixed-Drunk whole-bundle evaluators.

## 3. Dedicated contract

Add:

`DrunkAssignmentFeatures`

with independent projections:

```text
topology
firstNightInformationOpportunity
longitudinalNarrativeOpportunity
limitations
```

Use the existing generic `FeatureProjection<T>` / `FeatureUnavailableReason` availability wrapper only.

Do **not** embed this object inside ordinary `DecisionFeatures`.

### Candidate alignment

Add:

`CandidateDrunkAssignmentFeatures(candidateId, features)`

and:

`DrunkAssignmentFeatureEvaluation(candidates)`

The evaluation must preserve the exact legal candidate order emitted by DLB-3A.

## 4. Feature semantics

### Topology

`FeatureProjection.Projected<DrunkAssignmentTopologyFeatures>`

is always available for a valid 3B1 envelope.

### First-night information opportunity

`FeatureProjection.Projected<DrunkAssignmentFirstNightInformationOpportunityFeatures>`

is also always available from 3B1.

An empty factor list means **known no candidate-seat first-night public-good-info factor**.

It must not be represented as unavailable.

### Longitudinal narrative opportunity

`FeatureProjection.Unavailable(MISSING_CAPABILITY)`

for every DLB-3B2 candidate.

This distinction is important:

```text
empty first-night route list = known none
longitudinal unavailable       = current owner cannot answer
```

## 5. Limitations

Carry the 3B1 limitations unchanged into a typed:

`DrunkAssignmentFeatureLimitations`

containing:

- excluded player-controlled element IDs;
- deferred first-night complexities.

These limitations are diagnostic facts, not rejection reasons.

## 6. Integration boundary

`DrunkSetupShadowEvaluation` gains:

`drunkAssignmentFeatureEvaluation`

The existing ordinary:

- `featureEvaluation`;
- frozen V1 `policyEvaluation`;
- `policySelection`;
- `DecisionTrace`;
- `MultiPolicyReplayInput`

remain unchanged.

DLB-3B2 therefore does **not** make the new surface policy-consumable.

## 7. Stable tests

Tests must prove:

1. G10 Empath topology feature projects adjacent Demon seat 2;
2. G10 Empath first-night opportunity projects the existing numeric factor with multiple legal outputs;
3. G10 deferred Spy/Recluse complexity survives in feature limitations;
4. Monk first-night opportunity is Projected with an empty factor list, not Unavailable;
5. longitudinal narrative opportunity is MISSING_CAPABILITY;
6. candidate IDs/order exactly match the legal/shadow domain;
7. ordinary `DecisionFeatureEvaluation` remains all-NOT_PROJECTED for this Drunk surface;
8. frozen V1 still defers and selector still returns null.

No test may assert that any candidate is preferred.

## 8. Allowlist

Production:

- new `DrunkAssignmentFeatures.kt`;
- `DrunkSetupShadowAdapter.kt` only to project/attach the dedicated evaluation.

Tests:

- new `DrunkAssignmentFeatureSurfaceTest.kt`;
- existing 3B1/3A tests only if a constructor change requires a mechanical adjustment.

Docs:

- this audit;
- roadmap/handoff only after acceptance.

## 9. Explicit NO-GO

- no `DecisionFeatures` modification;
- no `BEGINNER_CONSERVATIVE_V1` modification;
- no new policy version in this slice;
- no ranking / survivor / rejection semantics;
- no weights, thresholds or scalar score;
- no role-name preference;
- no output selection;
- no canonical Drunk commit;
- no UI;
- no persistence;
- no longitudinal inference from first-night-only evidence.

## 10. Next gate

After DLB-3B2 is accepted, DLB-3B3 must decide how the dedicated feature surface participates in shadow replay / policy-version experimentation without mutating frozen V1.

That later slice must keep descriptive projection separate from policy semantics.


## 11. Test-first evidence

### RED

The first RED attempt exposed a test-only Kotlin generic-cast defect and was corrected before accepting RED provenance.

Clean RED checkpoint:

`9e238043b2c3efe688913e872e5efe2fceaecd64`

CI #3551 failed at Android unit-test compilation only because the new DLB-3B2 contract was not yet implemented:

- unresolved `DrunkAssignmentFeaturesProjector`;
- unresolved `DrunkAssignmentTopologyFeatures`;
- unresolved `DrunkAssignmentFirstNightInformationOpportunityFeatures`;
- unresolved `DrunkSetupShadowEvaluation.drunkAssignmentFeatureEvaluation`.

No unrelated production failure or syntax failure remained.

### GREEN / T1

Production GREEN head:

`b9487beccf97f24467d733542357ca44736d16eb`

CI #3553:

- Android FAST unit tests: GREEN;
- aggregate CI gate: GREEN.

R2 #3290: GREEN.

## 12. Exact diff / fan-out audit

Executable production scope is exactly:

- new `DrunkAssignmentFeatures.kt`;
- narrow feature projection/attachment in `DrunkSetupShadowAdapter.kt`.

No change occurred in:

- `DecisionFeatures.kt`;
- `BeginnerConservativeV1Policy.kt`;
- selector code;
- `DecisionTrace`;
- replay engine/input;
- canonical setup/session;
- UI;
- persistence.

Production flow is:

```text
DLB-3B1 DrunkSetupConsequenceEnvelope
        ↓
DrunkAssignmentFeaturesProjector
        ↓
CandidateDrunkAssignmentFeatures
        ↓
DrunkAssignmentFeatureEvaluation
        ↓
DrunkSetupShadowEvaluation.drunkAssignmentFeatureEvaluation
```

The ordinary DLB-3A `DecisionFeatureEvaluation` remains a separate parallel surface and still carries NOT_PROJECTED_YET strategic features, so frozen V1 still defers with no selection.

T4 full-CI acceptance is requested for this new stable setup/recommendation feature contract.
