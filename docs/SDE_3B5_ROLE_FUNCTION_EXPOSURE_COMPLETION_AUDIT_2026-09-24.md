# SDE-3B5 — Contextual Role-Function Exposure Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE — FEATURE SEMANTICS ACCEPTED; POLICY SEVERITY DEFERRED**

## 1. Completion conclusion

SDE-3B5 is complete as the score-free semantic/projector slice for contextual role-function exposure.

The accepted implementation establishes one generic feature family for role/function targets whose usefulness depends on preserving ambiguity, with the first production-backed capability limited to **registration ambiguity**. It does not introduce a named-role penalty table, scalar exposure score, recommendation preference, or second history owner.

The accepted semantics distinguish:

- direct exposure;
- already exposed versus newly exposed;
- confirmation-amplified exposure;
- forced versus avoidable exposure across the complete legal candidate set;
- recipient-visible historical exposure only;
- unavailable history/confirmation inputs explicitly rather than treating them as neutral.

## 2. Accepted implementation slices

### 3B5A — pure typed exposure semantics

Accepted head:

`d80b8934103f8a16add09c2acb890e8b9be1da59`

Key owners:

- `RoleFunctionExposureCapability`;
- `RoleFunctionExposureTargetRef`;
- `RoleFunctionExposureCandidateEvidence`;
- `RoleFunctionExposureFeatures`;
- `RoleFunctionExposureFeaturesProjector`.

The projector is role-agnostic and score-free. Forced exposure is derived by intersection across the complete legal candidate set; avoidable exposure is direct exposure for which at least one legal alternative preserves ambiguity.

Acceptance evidence:

- CI #3409 / run `35869653795`: success;
- R2 #3165 / run `35869654046`: success.

### 3B5B — pair-information registration-ambiguity projection

Tests-first RED:

`8786390f0374d1ad3ffbe0be8fcb04fbc4bfe996`

CI #3410 failed at Android FAST for the expected unresolved `PairInformationRegistrationAmbiguityExposureProjector` symbol; R2 #3166 remained green.

Accepted GREEN head:

`acdb058f3b1cd0616c94f7d6ba0571abcaeae645`

Production owner:

`PairInformationRegistrationAmbiguityExposureProjector`

It reuses:

- `NaturalPairInformationCandidateGenerator` for natural truthful pair semantics;
- `PairInformationLegalDomain` for the selectable legal candidate set;
- `TroubleBrewingRegistrationDomain` for registration capability/legality;
- `RegistrationFact` for typed witness identity;
- `PairInformationExactConsequenceAdapter` output for the selected witness binding.

No role-name branch determines exposure. A direct exposure is projected only when the already-legal visible pair identifies the actual role of a currently functioning registration-capable subject rather than using a selected special-registration witness for that subject.

Acceptance evidence:

- CI #3411 / run `35872643774`: success;
- R2 #3167 / run `35872643773`: success.

### 3B5C — historical / confirmation integration

Tests-first RED:

`78bcaf35b5fc1968b5a1a5341cdfff470e479104`

CI #3412 failed at Android FAST for the expected unresolved `HistoricalRoleFunctionExposureFeatureProjector` symbol; R2 #3168 remained green.

Accepted GREEN head:

`473d3cb21e189355c2e730784db1f137333319c5`

Production owner:

`HistoricalRoleFunctionExposureFeatureProjector`

It reuses:

- canonical `ActionFactTimeline`;
- canonical `EpistemicObservationLog`;
- the 3B1 committed-prefix / no-hindsight boundary;
- typed `InformationProposition` role claims;
- 3B2 `HistoricalObservationConfirmationImpact.authenticatesDistinctSource`.

It does not persist exposure history and does not run another world evaluator. Historical exposure is recipient-visible and proposition-typed; confirmation amplification is accepted only from the existing confirmation-chain provenance.

Acceptance evidence:

- CI #3413 / run `35932784853`: success;
- R2 #3169 / run `35932784873`: success.

### 3B5D — bounded E1/E2 regression

Accepted evidence head:

`843455e8dac4f9bac0de516e875900a1c8b6c24c`

The bounded E2 regression reuses the existing executable `goldcand-ben-03` / Live and Imp-Person reconstruction rather than duplicating the real-game fixture.

The regression proves only the semantic shape supported by that evidence:

- the observed Librarian pair directly identifies the actual Recluse to that recipient;
- the same reconstructed subject later participates in a distinct Fortune Teller registration interaction;
- the complete production legal pair domain contains alternatives that preserve the subject's ambiguity;
- therefore the observed direct exposure is represented as **avoidable**, not forced;
- no preference label is inferred from the observed Storyteller choice.

Acceptance evidence:

- CI #3414 / run `35933589084`: success;
- R2 #3170 / run `35933588954`: success.

## 3. Ownership and fanout audit

### Rules / legality remain upstream

3B5 does not own:

- whether Spy/Recluse registration is legal;
- pair-information truth;
- pair-information display legality;
- exact registration witness selection;
- canonical historical facts;
- confirmation-world evaluation.

Those remain with their existing rules, recommendation-domain, epistemic and history owners.

### SDE owns only derived exposure facts

3B5 owns the typed diagnostic projection needed for later policy evaluation:

`upstream legal/rules/history/confirmation evidence -> RoleFunctionExposureFeatures`

This is a derived read-only projection.

### Current production envelope boundary

The accepted production-facing structured shadow is still primarily the structured numeric path. Pair information retains its separate exact-consequence adapter and is not forced through the numeric `InformationDecisionContext<T : DynamicInformationOutcome>` transport.

Accordingly, 3B5 completion does **not** claim a production pair-information recommendation cutover. The shared exposure projectors are production-owned semantic seams ready for a future clean pair-information SDE envelope. `DecisionFeatures.roleFunctionExposure` may remain unavailable on paths that have not captured/projected this feature.

This is intentional. Adding caller-specific or numeric-transport wiring merely to make the feature appear projected would violate the ownership audit.

## 4. Safety / architecture invariants verified

Production 3B5 code contains:

- no `if Spy`;
- no `if Recluse`;
- no Librarian/Investigator policy branch;
- no use of legacy `exposureSensitivity`;
- no natural-language `abilityText` parsing;
- no score, weight, threshold or severity;
- no exposure-history store;
- no V1 policy reason;
- no visible recommendation change.

Named roles appear only where existing rules owners necessarily encode Trouble Brewing registration legality, or in bounded test/evidence fixtures.

## 5. Evidence boundary after completion

The E1/E2 evidence is sufficient to accept the **feature semantics**.

It is not sufficient to choose policy strength.

The targeted Gap C evidence contract remains authoritative:

- current role-function exposure severity evidence is observation-only / weak for preference;
- legal direct exposure may be forced;
- silent non-selection must not be treated as rationale;
- a future E3 policy preference requires explicit qualified Storyteller rationale or sufficiently strong cross-expert qualitative support with considered legal alternatives.

Therefore SDE-3B5 does **not** establish:

- “avoid Librarian -> Recluse” as a general rule;
- “strongly avoid Investigator -> Spy” as a general rule;
- any universal exposure rejection;
- any relative exposure severity ordering.

Those older named-role policy statements are superseded by the generic contextual feature plus the current E3 evidence gate.

## 6. Validation cadence

No T4 full-ci was requested for 3B5.

Per the current testing strategy, T4 remains reserved for the logical overall SDE-3B acceptance checkpoint. Every 3B5 GREEN/evidence head used GitHub Android FAST plus R2 as the independent acceptance surface.

## 7. Next boundary — SDE-3B6

SDE-3B6 may now audit which completed feature families have sufficient E3 evidence for qualitative V1 soft priorities.

It must not assume that every completed projector is immediately eligible for policy use.

In particular:

- role-function exposure remains diagnostic until Gap C gains qualifying E3 evidence;
- numeric strength/tradeoffs remain SDE-3D;
- pair-information production-envelope generalization must be done cleanly when that path is introduced, not by contaminating the numeric structured transport;
- PR #153 remains draft until explicit project-owner authorization.
