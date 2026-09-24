# SDE-3B1 Historical Lifecycle / Input-Binding Completion Audit — 2026-09-23

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: #153 (draft)  
> Status: **SDE-3B1 COMPLETE — SDE-3B2 NEXT/CURRENT**

## 1. Completion conclusion

SDE-3B1 is complete.

The SDE shadow is no longer structurally limited to first-night-only evaluation. It can evaluate a later structured information interaction against an immutable setup baseline plus the canonical committed historical prefix, while preserving existing legality, session, recommendation and commit ownership.

No second historical state owner was introduced.

## 2. Implemented structure

### Historical lifecycle-safe shadow

`StructuredInformationProductionShadow.evaluateHistorical(...)` now:

- accepts later lifecycle points;
- reconstructs from `CommittedClocktowerSetup`;
- consumes canonical `GameSnapshot.actionTimeline`;
- consumes canonical `GameSnapshot.epistemicObservationLog`;
- remains read-only;
- preserves the current session revision;
- leaves visible recommendation and commit authority unchanged.

`evaluateFirstNight(...)` remains as the original compatibility boundary and delegates to the historical path after enforcing FIRST_NIGHT / round 1.

### Canonical committed-prefix reference

Every SDE candidate can carry `SdeHistoricalPrefixRef.Global` containing only:

- game identity;
- action IDs + global timeline sequences;
- observation record IDs + global timeline sequences.

It does **not** copy GameState, ActionFact payloads, observation payloads, or a perceived-world state.

The canonical payload owners remain:

- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- `GameSnapshot` / session state.

### No-hindsight boundary

Historical shadow requires every referenced committed action/observation to be strictly before the decision lifecycle point.

A future fact at or after the decision point is rejected.

This is the required committed-prefix boundary for later Evidence Lab replay and prevents future information from leaking backward into recommendation evaluation.

## 3. Typed contextual input binding

`SdeDecisionInputBindings` now distinguishes:

- `NotCaptured`;
- `Captured`.

Captured refs carry:

- stable input ID;
- explicit external/canonical owner ID;
- typed ownership kind.

Current committed Storyteller-input kinds:

- `SETUP_SHOWN_IDENTITY`;
- `RED_HERRING`;
- `DEMON_BLUFFS`;
- `OTHER`.

Current player-controlled kinds:

- `TARGET_SELECTION`;
- `OTHER`.

The same stable input cannot be claimed as both Storyteller-committed and player-controlled.

This is intentionally a **reference seam**, not a copied state model.

## 4. Owner map after 3B1

| Contextual fact | Existing owner | SDE-3B1 representation | Status |
| --- | --- | --- | --- |
| prior delivered information | `EpistemicObservationLog` | historical observation refs | canonical / ready |
| Poisoner confirmed target / poison episode mechanics | `ActionFactTimeline` / session poison boundary | historical action ref + player-controlled TARGET_SELECTION ref when policy depends on the selected target | canonical / ready |
| deaths / executions | `ActionFactTimeline` | historical action refs | canonical / ready |
| phase/round chronology | `ActionFactTimeline` | historical action refs + candidate lifecycle stage | canonical / ready |
| Demon succession / role transition | `ActionFact.RoleChange` + session succession authority | historical action refs | canonical / ready |
| Drunk shown identity | `CommittedClocktowerSetup` / shown role | SETUP_SHOWN_IDENTITY ref when contextual policy needs explicit provenance; shown identity already present in replay baseline | canonical setup / ready |
| Fortune Teller selected targets | current structured interaction / outer player-selection owner | TARGET_SELECTION ref | bindable; consumer must capture before contextual policy use |
| Red Herring | current App/Recovery setup-mechanics owner | RED_HERRING ref | bindable; not yet part of GameSnapshot read model |
| revealed/locked Demon bluffs | current App/Recovery setup-mechanics owner | DEMON_BLUFFS ref | bindable; not yet part of GameSnapshot read model |

## 5. Important non-blocking ownership caveat

Red Herring and Demon bluffs are persisted/recoverable today, but their live ownership still sits outside the canonical `GameSnapshot` SDE read model.

SDE-3B1 does **not** move that ownership merely for convenience.

That is acceptable for 3B1 because:

1. the SDE binding now names the external owner explicitly;
2. policy must not use those contextual facts while the binding remains `NotCaptured`;
3. a future feature consumer can receive an explicit `Captured` ref without copying the fact into SDE state;
4. if later architecture shows these setup commitments need a more canonical domain owner, that migration can be done deliberately with a separate fanout audit.

Therefore “not yet in GameSnapshot” is not treated as “missing fact” or silently reconstructed.

## 6. Verified historical scenario

The historical structured shadow test now proves:

- a Night 2 structured information interaction can be evaluated;
- prior globally committed observation history is included;
- prior PhaseAdvance and Poison action facts are included;
- Poisoner target ownership is represented as player-controlled input;
- legal candidate IDs / policy result remain aligned;
- visible choices are unchanged;
- session state is unchanged by shadow evaluation;
- a subsequently committed future Protect fact is rejected as hindsight when replaying the earlier decision.

This is the minimum lifecycle proof required before confirmation-chain feature work.

## 7. What SDE-3B1 deliberately did not do

SDE-3B1 did not:

- add a new policy reason;
- rank FT targets or Poisoner targets;
- move player-controlled target ownership into SDE;
- persist a second narrative/perceived-world state;
- move Red Herring / Demon-bluff ownership merely to simplify SDE;
- persist DecisionTrace;
- introduce numeric thresholds;
- perform production recommendation cutover.

## 8. Validation

Live checkpoint before this completion-doc update:

- PR #153: draft / open / mergeable;
- code/document HEAD: `e7e07404fb418ef59a4edbd107a33001ac9daadb`;
- R2 main-thread boundary: **success**;
- CI: **success**.

No T4 milestone run is required to close this internal 3B1 slice. SDE-3B remains an open milestone on draft PR #153; T4 remains required at the SDE-3B acceptance checkpoint.

## 9. Next phase — SDE-3B2

SDE-3B2 is now CURRENT.

Goal:

**build a generic confirmation-chain feature projector over committed-prefix information, without role-pair special cases and without changing policy yet.**

Existing reusable research:

- `Sde2D5BundleConfirmationChainEvidenceProjector` already demonstrates the useful leave-one-out idea in review-only form:
  - remove one information channel;
  - compare exact strategic topology / Demon cover;
  - identify observations whose removal restores important ambiguity.

SDE-3B2 should extract the generic semantic idea into production-owned feature projection rather than promoting the review-only type directly.

First design questions:

1. what is the minimal production `ConfirmationChainFeatures` payload;
2. how to identify support / contradiction / authentication without named-role tables;
3. how to compare current candidate + committed-prefix observations using exact historical consequence primitives;
4. how to preserve recipient-visible knowledge boundaries;
5. how to guarantee no future observation can affect an earlier decision;
6. how Evidence Lab R02/R04 prefixes should be used as E2 semantic regression, not as hard-coded rules.

Do not add V1 soft preference until the projector has generic tests and evidence replay validation.
