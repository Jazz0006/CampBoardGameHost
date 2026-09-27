# SDE-3C5 — Multi-Policy Replay Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Entry HEAD: `54ba922c7097d128d676c35e0d7ed595b0fa2d4b`  
> Scope: replay multiple explicit policy versions over one recomputed canonical historical decision input.

## 1. Entry state

SDE-3C4 is COMPLETE and remotely accepted. DecisionTrace now has:

- a canonical Global history-prefix reference;
- complete legal candidate IDs;
- a policy-neutral feature snapshot;
- one explicit policy version/snapshot/selection;
- an authoritative actual choice when correlation has succeeded.

The archive key already includes `policyVersion`, so the same canonical decision/revision may hold
separate traces for distinct policy versions without creating a second game-history owner.

## 2. Critical replay rule: do not replay from persisted old features

A persisted DecisionTrace records what the policy saw at capture time. That is audit evidence, not
historical truth.

New policy replay must use:

```text
canonical committed prefix
    -> exact consequence replay
    -> current/versioned policy-neutral feature projection
    -> explicit Policy V1 / V2 / ...
```

It must not use:

```text
old DecisionTrace.featureEvaluation
    -> new policy
```

The existing `StructuredInformationProductionShadow.evaluateHistorical` plus
`StructuredInformationShadowAdapter` already own the canonical prefix -> exact consequences ->
feature projection path. SDE-3C5 should consume the freshly recomputed shadow feature result rather
than reconstruct feature semantics from the persisted trace.

## 3. Policy runner / registry boundary

There is currently only one real production policy version:

- `BEGINNER_CONSERVATIVE_V1`.

Do not invent a production V2 merely to demonstrate multi-policy replay.

Introduce a policy-neutral replay runner contract:

- explicit `policyVersion`;
- input: one recomputed `DecisionFeatureEvaluation`, stable decision ID, canonical selection seed;
- output: policy-neutral `DecisionTracePolicySnapshot` plus optional `PolicySelection`.

The production registry initially contains only the real V1 runner. It must:

- reject unknown versions;
- reject duplicate registered versions;
- preserve explicit requested-version order;
- never silently fall back to V1.

Tests may inject deterministic test-only runners with distinct test policy versions to prove genuine
multi-version fanout without creating fake production policy identities.

## 4. Canonical replay input

Replay needs a small immutable policy-neutral input extracted from a freshly recomputed structured
shadow:

- decision ID;
- interaction lifecycle;
- source revision;
- one Global canonical history-prefix ref;
- complete ordered legal candidate IDs;
- recomputed `DecisionFeatureEvaluation`;
- canonical game/selection seed.

The input must preserve candidate order exactly and reject mixed decision identity, lifecycle,
revision, prefix, or non-Global history.

`StructuredInformationShadowEvaluation` therefore needs to expose the canonical selection seed that
was already used for the production V1 shadow selector. This is read-only diagnostic input; it does
not move selection or game-state authority.

## 5. Historical trace correlation

A replay request also carries the source historical `DecisionTrace` so the report can preserve the
authoritative `actualChoice` already captured by SDE-3C4.

Before replay, require equality between source trace and recomputed replay input for:

- decision ID;
- lifecycle;
- source revision;
- Global history-prefix ref;
- complete legal candidate IDs.

Do **not** require equality of `featureEvaluation`: recomputation is expected and is the entire point
of replay under current/versioned projectors.

Every replayed trace copies the source trace's authoritative `actualChoice` unchanged.

## 6. Evidence checkpoint ownership

Each requested policy version must have one explicit `EvidenceCheckpointId`.

The replay request fails closed when the requested policy-version set and evidence-checkpoint key set
do not match exactly. Evidence provenance must not be inferred from the source trace because a new
policy version may rely on a different evidence corpus.

## 7. Persistence / mutation boundary

Multi-policy replay is pure:

- it does not mutate canonical GameSnapshot, ActionFactTimeline, or EpistemicObservationLog;
- it does not mutate the source DecisionTrace;
- it does not write DecisionTraceArchive automatically;
- it returns replayed DecisionTrace values.

A caller may append those traces through the existing immutable archive/store contract. Distinct
policy versions have distinct archive keys. Same-version semantic drift remains fail-closed rather
than silently overwriting historical diagnostics.

## 8. Tests-first acceptance

Add contract tests for:

1. production registry exposes only the real V1 version;
2. V1 replay runner returns the same policy snapshot/selection semantics as the existing V1 policy
   and selector;
3. two injected explicit test policy versions replay the same canonical input in requested order;
4. all replay outputs share decision/lifecycle/revision/prefix/legal domain/recomputed features and
   authoritative actual choice;
5. persisted old source-trace features are not reused;
6. unknown and duplicate requested policy versions fail closed;
7. missing/extra evidence checkpoint mappings fail closed;
8. mismatched decision/revision/domain/lifecycle/prefix fail closed;
9. source trace and canonical replay input remain unchanged;
10. distinct replay policy versions can coexist in the immutable archive.

Oracle Android execution remains subject to the known SDK limitation. GitHub CI/R2 remains the
independent Android acceptance surface.

## 9. Explicit non-goals

SDE-3C5 does not:

- create BEGINNER_CONSERVATIVE_V2;
- calibrate new policy thresholds;
- change production-visible recommendation;
- automatically select/confirm/commit a Storyteller choice;
- auto-persist replay results;
- mutate canonical historical truth;
- perform production cutover (SDE-3D / 3E remain blocked by calibration evidence).
