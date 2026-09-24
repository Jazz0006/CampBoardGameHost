# SDE-3D1 — BEGINNER_CONSERVATIVE_V1 Immutable Baseline Freeze Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Entry HEAD: `82fe839284d3244cbefc2e195c8ad778d1361b83`  
> Status: **ARCHITECTURE ACCEPTED — IMPLEMENTATION COMPLETE; SEE COMPLETION AUDIT**

## 1. Purpose

SDE-3D0 established that `BEGINNER_CONSERVATIVE_V1` is ready to become an immutable provisional
baseline before any new evidence-authorized preference is introduced.

SDE-3D1 is therefore a version-integrity slice, not a calibration slice.

It must freeze the identity/provenance of the already-accepted V1 behavior without:

- changing candidate legality;
- changing the exact zero-Evil-topology rejection;
- adding a new soft preference;
- changing survivor equivalence semantics;
- creating V2;
- cutting SDE over to visible production authority.

## 2. Current implementation strengths

The current V1 boundary already has several strong versioned contracts:

- one centralized `PolicyVersions.BEGINNER_CONSERVATIVE_V1`;
- all V1 candidate evaluations carry that version;
- structured shadow requires V1 explicitly;
- replay production registry contains only the real V1 runner;
- replay unknown/duplicate versions fail closed;
- DecisionTrace persists policy version separately from schema version;
- replay recomputes current policy-neutral features from canonical history;
- V1 policy tests already protect:
  - upstream-deferred behavior;
  - missing strategic projection deferral;
  - undefined strategic baseline deferral;
  - exact zero Evil-topology rejection;
  - non-zero retention survivor equivalence;
  - all-contradictory deferral;
  - explicit evidence/projector limitations;
- selector tests protect order independence, survivor-only selection and Deferred -> no selection.

These contracts are sufficient to freeze behavior without adding a new scoring architecture.

## 3. Version-integrity gaps found

### 3.1 Evidence checkpoint is caller-owned rather than policy-definition-owned

`EvidenceCheckpointId` is typed and persisted correctly, but no production V1 definition owns one
canonical checkpoint.

Current APIs allow:

```text
DecisionTraceFactory.fromStructuredShadow(
    shadow,
    evidenceCheckpoint = arbitrary caller value
)
```

and:

```text
MultiPolicyReplayEngine.replay(
    ...,
    policyVersions,
    evidenceCheckpoints = arbitrary map supplied by caller
)
```

That is valid generic infrastructure, but it is insufficient for an **immutable production policy
version**: the same `BEGINNER_CONSERVATIVE_V1` behavior can be labeled with unrelated evidence
checkpoint IDs.

The existing tests repeatedly use:

`sde-3b-merged-2026-09-24`

as the V1 checkpoint. This is already the de-facto accepted V1 evidence identity and should become the
single production definition rather than remain test-local text.

### 3.2 Selector behavior is named but not golden-frozen

The selector exposes:

`PolicySelectionMethod.SEEDED_HASH_V1`

and tests prove order independence.

However there is no fixed multi-survivor golden result. A future change could alter:

- hash payload field order;
- hash algorithm;
- canonicalization;
- winner comparison;

while retaining the same policy version and method enum, and the current tests might still pass.

Since seeded tie selection is part of observable V1 recommendation semantics, at least one stable
golden contract should pin the current algorithm.

## 4. Evidence checkpoint meaning

The checkpoint attached to a trace is the evidence/corpus basis of the **policy version**, not the
historical game being replayed.

Therefore:

- V1 should retain one frozen evidence checkpoint even when replayed over later game records;
- a later evidence corpus that changes policy behavior requires a new policy version;
- merely replaying V1 over new external games does not mutate V1 evidence provenance;
- replay-source provenance remains owned separately by the game/trace/evidence corpus that supplies
  the canonical historical case.

This matches SDE-3C5's rule that different policy versions may rely on different evidence checkpoints.

## 5. Target typed contract

Introduce one small immutable policy-definition type:

```text
StorytellerPolicyDefinition
    policyVersion
    evidenceCheckpoint
    selectionMethod
```

Production initially owns exactly one definition:

```text
BEGINNER_CONSERVATIVE_V1
    policyVersion = BEGINNER_CONSERVATIVE_V1
    evidenceCheckpoint = sde-3b-merged-2026-09-24
    selectionMethod = SEEDED_HASH_V1
```

The definition is metadata/version governance only. It must not contain:

- scores;
- thresholds;
- feature weights;
- rule logic;
- candidate ordering code;
- evidence labels for individual candidates.

## 6. Ownership / fanout plan

### Policy identity owner

The SDE policy package owns the immutable production definition.

The definition should reuse existing typed identities:

- `PolicyVersion`;
- `EvidenceCheckpointId`;
- `PolicySelectionMethod`.

Do not duplicate them as strings elsewhere.

### Shadow trace factory

Current production structured shadow is V1-only.

`DecisionTraceFactory.fromStructuredShadow` should therefore derive the evidence checkpoint from the
frozen V1 production definition instead of accepting a caller-provided checkpoint.

It must verify that the shadow policy version matches the definition before creating the trace.

### Replay runner / registry

Each replay runner should expose one complete policy definition rather than only a policy version.

The production V1 runner binds the frozen V1 definition.

The replay engine should derive the evidence checkpoint from the selected runner definition rather
than accepting an external checkpoint map.

Test-only runners remain free to inject explicit test policy definitions so generic multi-version
fanout remains testable without inventing production V2.

### Selector

The selector remains unchanged.

Add a golden contract proving one known multi-survivor input selects the currently expected
candidate under `SEEDED_HASH_V1`.

## 7. Architecture pre-flight

Architecture pre-flight:

- current owner: SDE policy/replay/trace metadata under
  `clocktower/recommendation/sde`;
- proposed responsibility: bind immutable policy version -> evidence checkpoint -> selection-method
  identity and prevent callers from relabeling production V1 provenance;
- authoritative state owner(s): policy definition owns release identity; canonical game history
  remains ActionFactTimeline/EpistemicObservationLog; DecisionTrace remains diagnostic only;
- narrow typed input/output seam: `StorytellerPolicyDefinition`, replay runner definition, trace
  factory using the frozen V1 definition;
- keep in current owner / extract: add one small policy-definition contract in the SDE package; do
  not move rules, feature projection or canonical history ownership;
- reason: version provenance is policy-release metadata shared by trace capture and replay, not
  gameplay state and not caller configuration.

## 8. Tests-first acceptance contract

Because this is a new durable version-integrity contract, tests should be written first.

Required RED expectations:

1. production V1 definition exposes exactly:
   - `BEGINNER_CONSERVATIVE_V1`;
   - `sde-3b-merged-2026-09-24`;
   - `SEEDED_HASH_V1`;
2. trace factory no longer accepts arbitrary production evidence checkpoint input and emits the
   frozen V1 checkpoint;
3. production replay emits the frozen V1 checkpoint without an externally supplied map;
4. injected test replay runners carry their own explicit test definitions and preserve requested
   order;
5. production registry still exposes only V1;
6. one fixed multi-survivor selector golden remains stable.

The test-only RED may fail to compile until the typed definition/API exists. Oracle cannot execute
Android JVM tests because the SDK is unavailable, so remote GitHub CI is the executable RED/GREEN
surface.

## 9. Validation scope

SDE-3D1 touches shared policy/replay/trace metadata and therefore requires:

- focused V1 policy/selector/replay/trace tests;
- T1 Android FAST on GitHub;
- exact diff/fanout review;
- no ASP/Real-Clingo semantic change expected from the metadata-only freeze itself unless CI
  classifier conservatively selects them;
- final checkpoint CI/R2 before marking 3D1 COMPLETE.

No new evidence-generation T3 harness is required because the policy behavior itself is unchanged.

## 10. Explicit non-goals

SDE-3D1 does not:

- add healthy-information preference;
- add impaired-narrative preference;
- add role-function-exposure preference;
- add truth-danger or Red-Herring features;
- add Demon-bluff ordering;
- change the zero-topology rule;
- create V2;
- modify production-visible recommendation authority;
- merge or mark PR #154 ready.

## 11. Completion condition

SDE-3D1 is complete when:

- V1 has one immutable production definition;
- trace/replay provenance is derived from that definition rather than arbitrary production caller
  input;
- seeded selector behavior has a stable golden;
- existing V1 policy behavior remains unchanged;
- focused/T1 remote validation is green;
- PR #154 remains draft.

After SDE-3D1, proceed to a separate SDE-3D2 architecture audit for
truth-danger / credibility-disruption and contextual Red-Herring projection.
