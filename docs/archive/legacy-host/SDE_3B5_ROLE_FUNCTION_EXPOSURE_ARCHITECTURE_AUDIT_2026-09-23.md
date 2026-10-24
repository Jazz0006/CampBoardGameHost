# SDE-3B5 — Contextual Role-Function Exposure Architecture Audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE — implementation accepted; see `SDE_3B5_ROLE_FUNCTION_EXPOSURE_COMPLETION_AUDIT_2026-09-24.md`**

## 1. Audit conclusion

SDE-3B5 must not be implemented as another role-name score table.

The evidence supports **role-function exposure as a real policy dimension**, but it does not support a frozen severity ordering. Existing production also lacks one generic rules capability meaning “this role's function depends on remaining hidden/ambiguous”.

Therefore the correct route is staged:

1. define a role-agnostic, score-free exposure feature contract;
2. project only exposure mechanisms for which an existing rules owner can supply typed evidence;
3. add historical/confirmation context without inventing a second exposure state;
4. use bounded E1/E2 evidence to validate semantics;
5. leave policy preference/severity to 3B6/E3.

## 2. Evidence boundary

The current synthesis identifies two strong motivating examples:

- Librarian information that points at the actual Recluse can explain later registration anomalies too quickly;
- Investigator information that points at the actual Spy can undermine the Spy's concealment/information-assisted bluff function.

Both remain legal, and both can be forced when no healthy alternative exists.

The targeted evidence-gap contract explicitly says severity is still uncertain and should not be inferred from silent non-selection. That means 3B5 may establish **feature existence and forced/avoidable semantics**, but not “avoid” versus “strongly avoid” policy strength.

Ravenkeeper and similar hidden-trigger roles remain examples of the broader dimension. Current evidence/rules metadata does not justify encoding named-role penalties for them.

## 3. Existing owners that must be reused

### Candidate legality / registration

- `NaturalPairInformationCandidateGenerator` owns truthful pair-information candidates;
- `PairInformationLegalDomain` owns complete selectable pair-information semantics by reliability;
- `TroubleBrewingRegistrationDomain` owns Spy/Recluse registration legality and typed `RegistrationFact`;
- `PairInformationExactConsequenceAdapter` already binds a selected registration witness for exact consequence evaluation.

3B5 must not regenerate any of those domains.

### Canonical history / confirmation

- `ActionFactTimeline` and `EpistemicObservationLog` remain the only durable history owners;
- confirmation-chain projection already owns exact same-recipient support/contradiction/independence evidence;
- 3B5 may derive “already exposed” / “confirmation-amplified” facts from those owners when the pair-information SDE path is wired, but must not persist a second exposure ledger.

### Interaction lifecycle

`ClocktowerCharacterInteractionRegistry` owns event-triggered interaction eligibility such as Ravenkeeper death-trigger wakeup.

It does **not** say that an identity is strategically valuable to conceal. Trigger eligibility therefore cannot be reinterpreted as exposure sensitivity without a separately justified typed semantic.

## 4. Existing surfaces that are explicitly not authority

`TroubleBrewingRecommendationMetadata` contains legacy scalar fields such as:

- `exposureSensitivity`;
- `investigatorDisplaySuitability`.

These are compatibility-era ranking heuristics. They are not rules truth, not E2 evidence, and must not feed the SDE-3B5 feature projector.

Likewise, parsing natural-language `abilityText` for words such as “register”, “die” or “Demon” is not an acceptable semantic owner.

The catalog's optional `behaviorKey` is also not currently sufficient: the Trouble Brewing legacy registry does not populate a generic role-function-exposure capability.

## 5. Required feature contract

The pure SDE contract should operate on **typed exposure evidence already supplied by an upstream semantic owner**, not on role names.

At minimum it must be able to describe, per legal candidate:

- which role-function targets the candidate exposes to its information recipient;
- which of those targets were already exposed by committed information;
- which are newly exposed;
- which exposures are amplified/authenticated by an additional information channel;
- whether an exposure is forced because every legal candidate exposes the same target;
- whether it is avoidable because at least one legal candidate preserves that target's ambiguity.

The contract must contain no score, severity integer, probability, role-name branch or reject/accept policy.

## 6. First rules-backed mechanism

The first safe production mechanism is **registration ambiguity exposure**.

Why:

- Spy/Recluse registration legality already has a single typed rules owner;
- pair-information candidates already preserve actual truth versus registered truth and selected witness;
- the candidate set can distinguish a natural clue that points at the actual registration-capable role from an alternative clue that uses registration to preserve that role's ambiguity;
- forced versus avoidable exposure can therefore be derived from the complete legal candidate set instead of hard-coding “Librarian -> Recluse” or “Investigator -> Spy”.

The feature layer should receive the resulting typed target/capability evidence. It should not call `RoleId("Spy")` / `RoleId("Recluse")` itself.

## 7. Unsupported mechanism boundary

A broader “identity-dependent trigger” mechanism (for example, a role that benefits when Evil does not know its identity) is **not yet rules-owned generically**.

Do not infer it from:

- night order;
- interaction handler presence;
- ability text;
- legacy exposure score;
- a hard-coded Ravenkeeper/Sage/etc. list.

Until a generic semantic owner exists, those cases remain evidence for future feature coverage rather than production facts.

## 8. Pair-information integration boundary

The current production-facing `StructuredInformationShadowAdapter` is built around `InformationDecisionContext<T : DynamicInformationOutcome>` and the current accepted structured slice is numeric.

Pair information has a separate exact adapter with registration-witness binding. Do not force `PairInformationOutcome` through the numeric/dynamic transport merely to make 3B5 convenient.

3B5 should first establish the shared pure feature contract and a pair-information exposure evidence projector. Later integration may either:

- generalize the structured information envelope cleanly; or
- attach the shared feature through the existing pair exact-consequence path.

That decision requires a fanout check after the pair projector exists.

## 9. Implementation order

### SDE-3B5A — pure typed exposure semantics — COMPLETE

Role-agnostic score-free direct/new/already/confirmation/forced/avoidable semantics are accepted.

### SDE-3B5B — pair-information registration-ambiguity projection — COMPLETE

`PairInformationRegistrationAmbiguityExposureProjector` reuses `NaturalPairInformationCandidateGenerator`, `PairInformationLegalDomain`, `TroubleBrewingRegistrationDomain`, `RegistrationFact` and the selected witness bound by `PairInformationExactConsequenceAdapter`.

Production contains no Librarian/Recluse or Investigator/Spy policy branch.

### SDE-3B5C — historical / confirmation integration — COMPLETE

`HistoricalRoleFunctionExposureFeatureProjector` reuses canonical history, committed-prefix chronology, recipient visibility and existing confirmation provenance. No durable exposure state was added.

### SDE-3B5D — bounded E1/E2 regression and completion audit — COMPLETE

The existing `goldcand-ben-03` executable reconstruction validates the supported semantic shape without turning its observed choice into a preference label. Severity remains deferred to a qualifying E3 evidence gate.

Completion authority:

`SDE_3B5_ROLE_FUNCTION_EXPOSURE_COMPLETION_AUDIT_2026-09-24.md`

## 10. Non-goals

3B5 does not:

- make direct exposure illegal;
- assign “avoid” / “strongly avoid” weights;
- read legacy exposure scores;
- parse ability text;
- add a role-name exposure table;
- persist exposure history;
- change visible recommendation;
- change V1 policy ordering;
- run T4 before overall SDE-3B acceptance.

## 11. Completion boundary

SDE-3B5 is complete as a semantic/projector slice.

The current structured numeric shadow is not generalized merely to carry pair-information exposure. Pair information keeps its separate exact-consequence path until a clean production envelope is introduced.

Role-function exposure remains diagnostic: the current evidence validates feature existence and semantics, not policy severity. SDE-3B6 must not promote this feature to a V1 preference unless the targeted Gap C E3 evidence requirement is independently satisfied.
