# DLB-3B3 Drunk-Assignment Shadow Replay / Policy Experiment — Architecture Audit — 2026-09-29

> Status: **IMPLEMENTATION CONTRACT / TEST-FIRST**
>
> Baseline: `main@329e28ace3e3dad736e4732ddd634be9217c5ce2`
>
> Depends on: DLB-3B1 consequence envelope + DLB-3B2 dedicated feature surface.

## 1. Purpose

DLB-3B2 created a dedicated Drunk-assignment feature surface that is intentionally separate from ordinary `DecisionFeatures`.

DLB-3B3 must make that dedicated surface replayable under explicitly versioned **shadow-only policy experiments** without modifying frozen `BEGINNER_CONSERVATIVE_V1` or the ordinary `MultiPolicyReplayEngine`.

The central invariant is:

```text
historical/source DecisionTrace
    contributes identity + legal domain + actual-choice metadata only

fresh DLB-3B2 feature evaluation
    contributes current Drunk-assignment evidence

explicit Drunk experiment policy version
    interprets only that dedicated feature surface

result
    is diagnostic replay output, never canonical setup authority
```

## 2. Why a parallel replay lane is required

The existing `MultiPolicyReplayInput` is typed around ordinary `DecisionFeatureEvaluation`.

Changing it to absorb Drunk-assignment features would:

- widen a frozen generic contract for one setup-specific surface;
- risk implying `BEGINNER_CONSERVATIVE_V1` can consume Drunk features;
- blur the distinction between first-night information decisions and setup-time Drunk assignment.

Therefore DLB-3B3 adds a dedicated Drunk-assignment replay input/engine.

The existing generic replay registry remains unchanged and production-facing.

## 3. Experimental policy identity

Add a stable experimental policy version:

`PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1`

This identity is **not** a production policy release and must not be inserted into `DecisionPolicyReplayRegistry.production()`.

Bind it to a dedicated definition:

`DrunkAssignmentExperimentPolicyDefinitions.SHADOW_V1`

with an explicit EvidenceLab/C1D evidence checkpoint.

No canonical commit authority is attached to this definition.

## 4. First experimental semantics

The initial `DRUNK_ASSIGNMENT_SHADOW_V1` must not rank candidates.

It evaluates the DLB-3B2 feature surface and returns Deferred.

Required deferral reasons:

1. `LONGITUDINAL_NARRATIVE_MISSING_CAPABILITY`
   - DLB-3B2 explicitly marks longitudinal narrative opportunity as `MISSING_CAPABILITY`.

2. `ORDERING_EVIDENCE_NOT_AUTHORIZED`
   - current evidence justifies typed descriptive dimensions, not a complete candidate-ordering policy.

The first experimental version therefore produces:

- policy version;
- complete candidate IDs in legal order;
- typed deferral reasons;
- no `PolicySelection`.

It must not turn G10 or A Fond Farewell into hard-coded winners.

## 5. Dedicated replay input

Add:

`DrunkAssignmentShadowReplayInput`

Fields:

- decisionId;
- lifecycleStage;
- sourceRevision;
- Global historyPrefixRef;
- legalCandidateIds;
- `DrunkAssignmentFeatureEvaluation`;
- selectionSeed.

A factory may build it from `DrunkSetupShadowEvaluation`.

Even though SHADOW_V1 is Deferred, retaining selectionSeed keeps later experimental versions reproducible without changing the replay identity contract.

## 6. Replay runner / registry

Add a dedicated runner interface accepting:

`DrunkAssignmentFeatureEvaluation`

The registry is explicitly experimental:

`DrunkAssignmentPolicyExperimentRegistry`

Default experimental registry contains only `DRUNK_ASSIGNMENT_SHADOW_V1`.

Unknown or duplicate requested versions fail closed.

The ordinary:

`DecisionPolicyReplayRegistry.production()`

must remain unchanged and continue exposing only `BEGINNER_CONSERVATIVE_V1`.

## 7. Replay output

Add:

`DrunkAssignmentShadowReplayRecord`

It carries:

- evidence checkpoint;
- decision identity/lifecycle/revision/prefix;
- complete legal candidate IDs;
- freshly recomputed dedicated feature evaluation;
- generic `DecisionTracePolicySnapshot`;
- optional `PolicySelection`;
- source `DecisionTraceActualChoice`.

The output is diagnostic only.

The source trace's persisted ordinary `featureEvaluation` and policy snapshot are **not** used as Drunk experiment inputs.

## 8. Replay engine

`DrunkAssignmentShadowReplayEngine.replay(...)` must:

1. require explicit non-empty unique policy-version requests;
2. cross-check source trace identity, lifecycle, revision, prefix and legal domain against the recomputed input;
3. run requested experiment versions in requested order;
4. preserve the source trace's `actualChoice`;
5. persist freshly recomputed dedicated features in each replay record;
6. never mutate source trace or replay input;
7. never commit a Drunk seat.

## 9. Historical-evidence semantics

A historical observed Storyteller choice may be carried as:

`DecisionTraceActualChoice.Committed`

for replay comparison.

That actual choice is **not** a policy label.

For G10 Game 2 the experiment should therefore be able to record:

```text
actual historical choice = seat-1 Empath
experimental policy       = Deferred
selection                 = null
```

This is a valid calibration result.

## 10. Stable tests

Tests must prove:

1. ordinary production replay registry still exposes only `BEGINNER_CONSERVATIVE_V1`;
2. Drunk experiment registry exposes only `DRUNK_ASSIGNMENT_SHADOW_V1`;
3. replay input from DLB-3 shadow preserves dedicated candidate order/features;
4. SHADOW_V1 defers for both required reasons and produces no selection;
5. G10 committed historical Empath choice survives replay unchanged while the policy remains Deferred;
6. replay uses freshly recomputed dedicated features rather than the source trace's ordinary feature snapshot;
7. multiple explicit test experiment versions replay in requested order;
8. duplicate/unknown policy versions fail closed;
9. identity/domain mismatch fails closed;
10. replay is pure.

## 11. Allowlist

Production:

- `PolicyEvaluation.kt` only to add the explicit experimental version ID;
- new `DrunkAssignmentPolicyExperiment.kt`;
- new `DrunkAssignmentShadowReplay.kt`;
- `DrunkSetupShadowAdapter.kt` only if a replay-input factory needs a narrow accessor; prefer no change if existing fields suffice.

Tests:

- new `DrunkAssignmentShadowReplayTest.kt`.

Docs:

- this audit;
- roadmap/handoff after acceptance.

## 12. Explicit NO-GO

- no modification to `BEGINNER_CONSERVATIVE_V1`;
- no modification to `DecisionPolicyReplayRegistry.production()`;
- no modification to ordinary `MultiPolicyReplayEngine`;
- no Drunk ranking in SHADOW_V1;
- no role-name rule;
- no weights / thresholds / scalar score;
- no seeded selection while experimental policy is Deferred;
- no canonical commit;
- no Beginner automatic authority;
- no UI/persistence expansion;
- no use of historical expert choice as a supervised winner label.

## 13. Next gate

After DLB-3B3 is accepted, the Drunk decision surface will have:

- legal candidates;
- hypothetical setups;
- consequence envelopes;
- dedicated typed features;
- replayable versioned shadow-policy experiments.

The next decision is then whether DLB-3B requires another evidence/capability slice before DLB-4 canonical commit work begins.

Importantly, DLB-4 canonical commit and Beginner automatic authority remain separate gates.


## 14. Test-first evidence

### RED

Clean RED checkpoint:

`5eb1d1db1a393aeea797ed00d71fe00d1f984201`

CI #3561 reached Android unit-test compilation and failed on the intentionally absent 3B3 contracts:

- `DRUNK_ASSIGNMENT_SHADOW_V1`;
- `DrunkAssignmentPolicyExperimentRegistry`;
- `DrunkAssignmentShadowReplayInput`;
- `DrunkAssignmentShadowReplayEngine`;
- `DrunkAssignmentShadowReplayRecord`;
- `DrunkAssignmentShadowV1ReplayRunner`.

No generic replay API change was required.

### GREEN / T1

The first production implementation reached executable tests at `23d192beb209c11a940d30f7c96d8ff9c4db606d`. One test fixture constructed an invalid non-SetupPrecommit input before entering `assertThrows`; the production input correctly failed fast.

The fixture was corrected without changing production semantics.

Accepted T1 head:

`a36bd089b446d3b02f31af1b63c6512593864359`

CI #3565:

- Android FAST unit tests: GREEN;
- 1,589 tests completed successfully;
- aggregate CI gate: GREEN.

R2 #3300: GREEN.

## 15. Exact diff / fan-out audit

Production scope is limited to:

- one explicit shadow-only version ID in `PolicyEvaluation.kt`;
- new `DrunkAssignmentPolicyExperiment.kt`;
- new `DrunkAssignmentShadowReplay.kt`.

The ordinary production surfaces remain unchanged:

- `BeginnerConservativeV1Policy.kt`;
- `DecisionPolicyReplayRegistry.production()`;
- `MultiPolicyReplayEngine`;
- ordinary `DecisionFeatureEvaluation`;
- `DecisionTrace` schema;
- canonical setup/session;
- UI;
- persistence.

The dedicated flow is:

```text
source DecisionTrace
    identity/domain/actual-choice metadata only
             +
fresh DrunkAssignmentFeatureEvaluation
             ↓
DrunkAssignmentShadowReplayInput
             ↓
explicit DrunkAssignmentPolicyExperimentRegistry
             ↓
DRUNK_ASSIGNMENT_SHADOW_V1
    Deferred:
      longitudinal capability missing
      ordering evidence not authorized
             ↓
DrunkAssignmentShadowReplayRecord
    diagnostic only / no commit
```

The default experimental registry contains exactly one version.

The ordinary production replay registry still contains exactly `BEGINNER_CONSERVATIVE_V1`.

T4 full-CI acceptance is requested for this stable replay/experiment boundary.
