# SDE-3C5 — Multi-Policy Replay Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: sde-3c-decision-trace-shadow-replay  
> Entry HEAD: 54ba922c7097d128d676c35e0d7ed595b0fa2d4b  
> Status: **CODE COMPLETE / REMOTE ACCEPTANCE PENDING**

## 1. Scope completed

SDE-3C5 now provides a policy-neutral offline replay path over one freshly recomputed structured
historical decision input.

Implemented:

- DecisionPolicyReplayRunner
  - explicit policyVersion;
  - consumes one recomputed DecisionFeatureEvaluation, stable decision ID, and canonical selection seed;
  - returns a policy-neutral DecisionTracePolicySnapshot plus optional PolicySelection.
- BeginnerConservativeV1ReplayRunner
  - delegates to the existing BeginnerConservativeV1Policy and BeginnerConservativeV1Selector;
  - therefore does not fork or reimplement V1 policy semantics.
- DecisionPolicyReplayRegistry
  - production registry contains only the real BEGINNER_CONSERVATIVE_V1;
  - duplicate runner versions fail closed;
  - unknown requested versions fail closed;
  - no implicit fallback to V1.
- MultiPolicyReplayInput
  - captures decision identity, lifecycle, source revision, one Global canonical history-prefix ref,
    complete ordered legal candidate IDs, freshly recomputed policy-neutral features, and the
    canonical selection seed;
  - fromStructuredShadow extracts this input directly from StructuredInformationShadowEvaluation.
- MultiPolicyReplayEngine
  - accepts an existing historical DecisionTrace only as the identity/actual-choice anchor;
  - requires exact decision/lifecycle/revision/prefix/legal-domain equality between the source trace
    and the recomputed replay input;
  - deliberately does not require old sourceTrace.featureEvaluation equality;
  - evaluates every explicitly requested policy version through the registry;
  - requires an exact evidence checkpoint mapping for the requested policy-version set;
  - returns one new DecisionTrace per requested version, preserving requested order;
  - copies authoritative actualChoice from the source historical trace unchanged;
  - does not mutate canonical history, the source trace, the replay input, or persistence.

StructuredInformationShadowEvaluation now exposes selectionSeed, which is the same canonical game
seed already used by the existing production V1 selector. No new selection authority was introduced.

## 2. Historical truth boundary

A persisted historical DecisionTrace feature snapshot is audit evidence describing what that capture
saw. It is not treated as historical truth for a new policy replay.

The implemented flow is:

~~~text
canonical committed prefix
    -> StructuredInformationProductionShadow / StructuredInformationShadowAdapter
    -> exact consequence evaluation
    -> current/versioned policy-neutral DecisionFeatureEvaluation
    -> MultiPolicyReplayInput
    -> explicit policy-version runners
~~~

The source trace contributes only:

- matching decision identity;
- matching lifecycle/revision/prefix/domain;
- authoritative actualChoice correlation.

The test contract explicitly uses an old source trace with Deferred features and a recomputed Ready
feature evaluation, then verifies every replayed trace uses the recomputed features.

## 3. Production version discipline

There is still only one real production policy version:

- BEGINNER_CONSERVATIVE_V1.

SDE-3C5 does not invent BEGINNER_CONSERVATIVE_V2.

Multi-version behavior is proven with injected test-only replay runners carrying distinct test policy
versions. This validates the generic orchestration and archive key behavior without creating fake
production policy semantics.

When a real V2 is later authorized, it can be added as another DecisionPolicyReplayRunner and
registered explicitly.

## 4. Archive compatibility

DecisionTraceArchive already keys traces by:

- game ID;
- decision ID;
- lifecycle;
- source revision;
- policy version.

Therefore replay outputs for distinct explicit policy versions can coexist for the same canonical
decision revision.

The replay engine itself does not auto-persist. Existing immutable archive/store contracts remain the
only persistence boundary. Same-version semantic drift remains fail-closed rather than silently
overwriting a historical trace.

## 5. Tests-first coverage

Added MultiPolicyReplayTest covering:

1. production registry exposes only the real V1 policy;
2. V1 replay runner preserves existing V1 policy and selector semantics;
3. duplicate registered policy versions fail closed;
4. two explicit injected policy versions replay the same canonical input;
5. requested policy-version order is preserved;
6. policy-specific evidence checkpoints are preserved;
7. replayed traces share decision/lifecycle/revision/prefix/legal domain;
8. replayed traces use recomputed features rather than persisted old source-trace features;
9. authoritative actualChoice is preserved across policy versions;
10. distinct policy-version traces can coexist in DecisionTraceArchive;
11. unknown requested versions fail closed;
12. duplicate requested versions fail closed;
13. missing or extra evidence-checkpoint mappings fail closed;
14. decision/revision/lifecycle/prefix/domain mismatches fail closed;
15. replay leaves source trace and replay input unchanged.

StructuredInformationProductionShadowTest additionally verifies:

- StructuredInformationShadowEvaluation exposes the canonical game seed as selectionSeed;
- MultiPolicyReplayInput.fromStructuredShadow preserves identity, revision, legal domain, recomputed
  features, seed, and Global prefix.

## 6. Local validation status

Mini MCP test:fast was invoked before production implementation and again after implementation.

Both attempts stopped during Gradle configuration before Kotlin compilation because the Oracle ARM64
workspace has no Android SDK configured:

~~~text
Could not determine the dependencies of task ':app:testFast'.
SDK location not found.
Define ANDROID_HOME or sdk.dir in /home/opc/repos/CampBoardGameHost/local.properties.
~~~

Therefore no local Kotlin GREEN claim is made. GitHub CI/R2 remains the required independent Android
acceptance surface.

## 7. Authority / fanout result

SDE-3C5 does not change:

- visible production recommendation;
- InformationDecisionContext confirmation authority;
- canonical observation/action commit ownership;
- ActionFactTimeline or EpistemicObservationLog;
- automatic DecisionTrace persistence;
- production policy calibration.

The new replay types currently have no UI or production commit caller. They are offline diagnostic
infrastructure.

## 8. Next gate

After commit/push and independent GitHub CI/R2 acceptance, SDE-3C5 can be marked COMPLETE.

At that point SDE-3C is structurally complete for the current route:

- deterministic shadow recommendation;
- typed/versioned DecisionTrace;
- durable diagnostic archive/codec;
- authoritative actual-choice correlation;
- explicit multi-policy replay.

SDE-3D calibrated policy freeze and SDE-3E automatic production cutover remain blocked by the
documented external evidence/calibration gaps rather than by missing replay architecture.
