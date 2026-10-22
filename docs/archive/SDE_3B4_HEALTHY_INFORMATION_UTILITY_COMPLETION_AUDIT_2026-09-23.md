# SDE-3B4 — Healthy-Information Utility Completion Audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE**  
> Accepted code checkpoint: `1ac8d5c5251cafad4527d4ce3df569f71fe25693`

## 1. Completion conclusion

SDE-3B4 is complete.

The structured SDE shadow now derives score-free whole-table healthy-information utility from canonical history, authoritative ability state, upstream legal truth relation and existing exact/confirmation evidence. It does not create an information-quality state store, fixed misinformation budget, percentage floor, score, weight or policy ordering.

`BEGINNER_CONSERVATIVE_V1` still does not consume this dimension for candidate ranking or rejection.

## 2. Ownership map

Durable authority remains:

- `ActionFactTimeline` for mechanical chronology;
- `EpistemicObservationLog` for delivered player-visible information;
- the existing legal candidate owner for current `AbilityState` and `TruthRelation`;
- exact consequence evaluation for before/after world feasibility;
- confirmation-chain leave-one-out evidence for same-recipient historical contribution.

Derived SDE owners:

- `HistoricalInformationAbilityStateResolver` reconstructs historical source functioning state from canonical setup/action history;
- `HistoricalHealthyInformationUtilityFeatureProjector` assembles whole-table candidate evidence;
- `HealthyInformationUtilityFeaturesProjector` classifies route survival/loss.

None persists state or changes candidate legality.

## 3. Healthy-information semantics

A current candidate is healthy only when:

- its source ability is `FUNCTIONING`; and
- upstream legality marks it `TRUE_TO_ACTUAL_STATE` or `TRUE_TO_REGISTERED_STATE`.

Legal Spy/Recluse registration truth therefore remains healthy information. This does **not** redefine `DecisionFeatures.semanticTruth`: that feature remains actual-state semantic truth only.

A historical ability observation is eligible as a healthy route only when authoritative historical replay resolves its source as `FUNCTIONING`. Player-facing `ObservationReliability` is not used to infer hidden impairment truth.

Drunk and Poisoned observations are excluded from healthy routes by authoritative ability state, not by role-name policy branches.

## 4. Route feature semantics

Stable route identity is explicit:

- historical route = observation record ID + recipient seat;
- current route = candidate ID + recipient seat.

The feature exposes:

- usable healthy routes before the candidate;
- independently usable routes before the candidate;
- usable healthy routes after the candidate;
- independently usable routes after the candidate;
- historical routes made redundant by support;
- historical routes directly contradicted by the candidate;
- lost healthy routes;
- whether the candidate removes the last currently usable healthy route;
- the current candidate route when it is healthy.

No numeric amount of “information value” is assigned.

## 5. Collapsed-baseline guard

Exact BEFORE feasibility is now explicit input to the pure projector.

If the committed historical baseline already has zero exact worlds:

- no historical route is claimed usable before the candidate;
- no route is claimed lost because of the candidate;
- `removesLastUsableHealthyRoute` is false;
- the candidate is not blamed for pre-existing inconsistency.

This preserves the same no-false-attribution principle used by earlier confirmation/impaired-narrative features.

## 6. Confirmation reuse and performance boundary

3B4 reuses one additional descriptive fact from the existing confirmation leave-one-out scan:

`HistoricalObservationConfirmationImpact.wasIndependentlyConstrainingBefore`

This answers whether the historical observation removed exact worlds before the current candidate, avoiding a second same-recipient historical enumeration.

Different recipients remain separate whole-table information channels. A private current candidate cannot automatically make another recipient's healthy route redundant merely because both exist at the table.

## 7. Structured shadow

The structured path is now:

~~~text
legal candidate
    -> exact historical consequence
    -> strategic / actual-state semantic truth
    -> confirmation-chain
    -> impaired-narrative
    -> healthy-information utility
    -> BEGINNER_CONSERVATIVE_V1
~~~

`StructuredInformationShadowAdapter` passes the upstream legal `TruthRelation` separately to the healthy-information projector while preserving the existing actual-state `SemanticTruth` projection.

Visible recommendation, confirmation and canonical commit authority remain unchanged.

## 8. Evidence Lab E2 boundary

### R01

The bounded regression verifies the useful structural shape: a temporarily Poisoned information channel is not a healthy route while a parallel functioning truthful channel remains available.

### R04

The bounded regression verifies that a persistent Drunk information channel is not counted as healthy while a parallel functioning truthful channel can remain usable.

These are semantic-shape regressions, not claims of complete game replay. No R01/R04 ID or named-role evidence branch exists in production feature logic.

## 9. Registration-semantics audit correction

During final audit, an exploratory test at `0170799038ae9e62151e05cde759b0e52c8a764c` incorrectly assumed that a functioning result which depends on legal Recluse/Spy registration should not count as healthy actual truth.

That premise contradicted existing rules ownership: functioning `TRUE_TO_REGISTERED_STATE` is a legal healthy result.

The incorrect test was **not** accepted as policy truth and production was not changed to satisfy it. It was replaced by the correct contract: functioning registered truth creates a healthy route while actual-state `SemanticTruth` remains distinct.

This correction is intentionally recorded because it is exactly the kind of semantic drift the ownership audit is meant to catch.

## 10. Tests / validation evidence

Initial feature-contract RED:

- `fb5f9a40bb8961b38a0813fcf31048368efcda42`;
- CI #3385 failed at unit-test compilation because the typed 3B4 feature/projector contract did not yet exist;
- R2 #3141 succeeded.

Accepted bounded E2 checkpoint:

- `d86c6a08d880ad175c2e99df1c7a79e01a9d83ae`;
- CI #3396 succeeded;
- R2 #3152 succeeded.

Last-route RED/GREEN:

- `8cc1e68e07dfcd66cf2b0211957bc3818d765e8d`: CI #3398 failed on the newly required last-route-removal contract; R2 #3154 succeeded;
- `8aa94f3309ec8634b5b0c1457b37eeb4ad3e80bf`: CI #3399 succeeded; R2 #3155 succeeded.

Final accepted code checkpoint:

- `1ac8d5c5251cafad4527d4ce3df569f71fe25693`;
- CI #3404 / workflow `35860571824`: Android FAST **SUCCESS**, CI gate **SUCCESS**; full Android build, ASP contract and Real Clingo were skipped by the incremental classifier;
- R2 #3160 / workflow `35860571820`: **SUCCESS**.

Several rapid intermediate commits had superseded CI runs cancelled by branch concurrency; they are not treated as acceptance evidence.

T4 `[full-ci]` was not run; it remains reserved for overall SDE-3B acceptance.

## 11. Fanout / ownership audit

Relative to completed 3B3, production fanout is limited to:

- typed healthy-information feature replacement in `DecisionFeatures`;
- pure `HealthyInformationUtilityFeaturesProjector`;
- read-only `HistoricalHealthyInformationUtilityFeatureProjector`;
- shared historical ability-state resolver extracted from 3B3 logic;
- one descriptive pre-candidate-independence field on confirmation impact;
- structured-shadow attachment.

No UI, session mutation, persistence schema, setup ownership, legacy selector, visible recommendation authority or V1 policy reason moved.

## 12. Deliberate exclusions

3B4 did not add:

- a fixed healthy-information floor;
- a misinformation percentage/budget;
- player-count bands;
- score/weight/probability;
- named-role policy exceptions;
- a V1 soft preference or rejection reason;
- DecisionTrace persistence;
- automatic-selection cutover.

Those boundaries remain deliberate.

## 13. Next — SDE-3B5

SDE-3B5 is CURRENT.

Goal: derive contextual role-function exposure as a generic lifecycle-aware diagnostic. Reuse existing lifecycle, confirmation, registration and rules semantics; distinguish newly exposed versus already independently exposed mechanics and forced versus avoidable exposure.

Examples such as Spy/Ravenkeeper may justify the dimension as evidence, but production must not encode named-role penalties. V1 policy remains unchanged until 3B5 feature semantics are accepted.
