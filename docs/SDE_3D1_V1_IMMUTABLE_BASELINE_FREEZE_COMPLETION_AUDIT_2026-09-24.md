# SDE-3D1 — BEGINNER_CONSERVATIVE_V1 Immutable Baseline Freeze Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> RED HEAD: `7c7f9b0974b664b2df29b6345a948851ec15e919`  
> Accepted GREEN code HEAD: `f562887cf4e90d02d364eef5534a1f709922a0f8`  
> Status: **COMPLETE**

## 1. Completion conclusion

SDE-3D1 is complete.

`BEGINNER_CONSERVATIVE_V1` now has one immutable production release definition binding:

- policy version: `BEGINNER_CONSERVATIVE_V1`;
- evidence checkpoint: `sde-3b-merged-2026-09-24`;
- survivor selection method: `SEEDED_HASH_V1`.

This freezes policy identity/provenance without changing V1 candidate legality, rejection semantics,
survivor equivalence, selector algorithm, visible recommendation authority, or canonical game state.

Any future semantic candidate rejection, preference or survivor-refinement change must use a new
explicit policy version rather than silently mutating V1.

## 2. Implemented contract

Added `StorytellerPolicyDefinition` and the production-owned
`StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1`.

The definition is release metadata only. It contains no:

- weights;
- thresholds;
- policy score;
- rule logic;
- candidate-specific evidence labels.

`EvidenceCheckpointId` now lives beside the policy definition because the checkpoint identifies the
evidence basis of a policy release rather than the historical game being replayed.

## 3. Trace provenance freeze

`DecisionTraceFactory.fromStructuredShadow` no longer accepts an arbitrary caller-provided evidence
checkpoint for the current V1 structured-shadow path.

It now:

- resolves the frozen production V1 definition;
- verifies the shadow policy version matches that definition;
- verifies any shadow selection uses the definition's frozen selection method;
- writes the definition-owned evidence checkpoint into the trace.

Canonical history ownership remains unchanged. DecisionTrace remains diagnostic/replay state.

## 4. Replay provenance freeze

`DecisionPolicyReplayRunner` now exposes a complete `StorytellerPolicyDefinition`; its policy
version is derived from that definition.

`BeginnerConservativeV1ReplayRunner` binds the frozen production V1 definition.

`MultiPolicyReplayEngine` no longer accepts an arbitrary external
`Map<PolicyVersion, EvidenceCheckpointId>`. For each requested version it:

- resolves the explicit runner;
- evaluates the recomputed canonical feature input;
- checks output policy identity;
- checks the selection method against the runner definition;
- writes the runner definition's evidence checkpoint into the replayed DecisionTrace.

Test-only replay runners still inject explicit test policy definitions. Generic multi-version replay
therefore remains proven without inventing a production V2.

## 5. Selector baseline freeze

The V1 selector implementation itself was not changed.

The existing order-independence test now also freezes one deterministic multi-survivor golden:

- decision ID: `decision-1`;
- selection seed: `42`;
- survivors: `a`, `b`, `c`;
- frozen result under `SEEDED_HASH_V1`: `c`.

This protects the observable V1 tie-selection contract against silent changes to hash payload order,
hash algorithm, canonicalization or winner comparison.

## 6. Tests-first evidence

### RED

Test-only RED HEAD:

`7c7f9b0974b664b2df29b6345a948851ec15e919`

Remote evidence:

- CI #3445: **FAILURE**;
- Android FAST: **FAILURE**;
- R2 #3200: **SUCCESS**.

The Android job failed at `:app:compileDebugUnitTestKotlin` because the new tests referenced the
not-yet-implemented frozen policy-definition contract, including unresolved
`StorytellerPolicyDefinitions`. This is the expected meaningful RED.

Oracle `test:fast` was also attempted but remained blocked during Gradle configuration because the
Oracle ARM64 workspace has no Android SDK; no local Kotlin RED/GREEN claim is made.

### GREEN

Accepted code HEAD:

`f562887cf4e90d02d364eef5534a1f709922a0f8`

Remote acceptance:

- CI #3446: **SUCCESS**;
- Android FAST: **SUCCESS**;
- aggregate CI gate: **SUCCESS**;
- ASP contract tests: skipped by incremental routing;
- Real Clingo: skipped by incremental routing;
- R2 #3201: **SUCCESS**.

PR #154 remained open, draft and mergeable/clean at the accepted exact head.

## 7. Fanout result

Post-implementation fanout audit confirms:

- `DecisionTraceFactory.fromStructuredShadow` currently has only the intended test callers;
- `MultiPolicyReplayEngine.replay` currently has no production caller and is exercised through the
  replay contract tests;
- production replay registry still contains only real V1;
- no external production evidence-checkpoint map remains;
- direct historical trace fixtures may still construct explicit `EvidenceCheckpointId` values where
  they are testing archive/persistence semantics rather than the production policy-release path.

No UI, canonical confirmation, session commit, legality, exact consequence or feature-projection
authority moved.

## 8. V1 semantic invariants preserved

SDE-3D1 does not change the accepted V1 policy:

```text
legal candidates
    ->
exact no-credible-Evil-world rejection
    ->
survivor equivalence band
    ->
SEEDED_HASH_V1 survivor selection
```

Still unchanged:

- exact zero Evil-topology retention is the only generic hard rejection;
- non-zero retention is not rejected through an invented threshold;
- confirmation-chain, impaired-narrative, healthy-information and role-function-exposure projections
  remain diagnostic for V1 ordering;
- no new soft preference exists;
- no placeholder V2 exists;
- no production cutover is authorized.

## 9. Next boundary

The next slice is **SDE-3D2 — calibration-ready missing feature completion**.

Start with an architecture/evidence/fanout audit for:

- truth danger / credibility disruption;
- contextual Red-Herring downstream policy input.

The current cross-expert evidence is sufficient to justify a stable descriptive feature surface, but
not a candidate preference. SDE-3D2 must therefore keep V1 immutable and policy-neutral unless a
separate qualifying E3 gate is later established.
