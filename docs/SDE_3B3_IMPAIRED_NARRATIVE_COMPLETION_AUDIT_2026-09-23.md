# SDE-3B3 — Impaired-Narrative Feature Completion Audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE**  
> Accepted code checkpoint: `f3331c5fc914f960eb1a91cbc6a53135cb953242`

## 1. Completion conclusion

SDE-3B3 is complete.

The structured SDE shadow now derives score-free impaired-narrative coherence/detectability from canonical history and authoritative impairment state without creating a second perceived-world/narrative owner. `BEGINNER_CONSERVATIVE_V1` still does not use this dimension for candidate ranking or rejection.

## 2. Ownership map

Durable authority remains:

- committed setup replay baseline for actual/shown identity;
- `ActionFactTimeline` for poison/death/role-change chronology;
- `EpistemicObservationLog` for delivered player-visible information;
- existing legal candidate authority for the current interaction's `AbilityState`;
- exact/confirmation layers for historical compatibility and contradiction.

Derived SDE owners:

- `HistoricalImpairedNarrativeFeatureProjector` resolves impairment lifetime/episode and eligible prior observations;
- `ImpairedNarrativeFeaturesProjector` classifies that evidence.

Neither persists history.

## 3. Feature semantics

Generic impairment lifetime is explicit:

- `PERSISTENT_SETUP_BOUND`;
- `TEMPORARY_ACTION_BOUND`.

The feature describes:

- no prior impaired narrative;
- compatible continuation;
- break/contradiction with prior impaired observations;
- baseline already infeasible before the current candidate;
- forced versus avoidable transition;
- categorical detectability.

No score, probability, weight, player-count band, numeric coherence threshold or fixed misinformation budget exists.

## 4. Historical derivation

Historical impairment is not inferred from `ObservationReliability`.

The projector requires committed global chronology, replays actions strictly before each relevant observation from the immutable baseline, resolves impairment through rules semantics, preserves recipient visibility and same source seat/ability, and limits temporary impairment to the current authoritative action episode.

Thus Drunk-style persistence and Poisoner-style temporary corruption share one mechanism while retaining different lifetime semantics.

## 5. Death-trigger chronology seam

R06 exposed a valid interaction that may resolve after the source is already dead. `AbilityFunctioningSemantics.stateForEstablishedInteraction(...)` therefore separates interaction discovery from ability-state resolution:

- ordinary `stateFor(...)` still requires a living subject;
- once another authority established the death-trigger interaction, state resolution can still return FUNCTIONING / DRUNK / POISONED.

This is a rules-semantic seam, not an SDE role exception.

## 6. Structured shadow

The path is now:

~~~text
legal candidate
    -> exact historical consequence
    -> strategic / semantic truth
    -> confirmation-chain
    -> impaired-narrative episode + feature
    -> BEGINNER_CONSERVATIVE_V1
~~~

`StructuredInformationShadowAdapter` attaches the feature to `DecisionFeatures.impairedNarrative`. An integration regression proves that removing this projected dimension leaves current V1 policy evaluation unchanged.

Visible recommendation, confirmation and canonical commit authority remain unchanged.

## 7. Evidence Lab E2 boundary

### R04

R04 verifies persistent Drunk information across nights. Its direct replay package still has `DRUNK_SHOWN_ROLE = UNKNOWN`; the test uses a generic legal information channel to validate only the persistent lifecycle shape and does not guess the missing shown role.

### R06

R06 directly records Poisoner target → Ravenkeeper death → poisoned Ravenkeeper false death-trigger information. The regression validates the temporary episode/death-trigger chronology only; it does not claim a full-game R06 reconstruction.

No R04/R06 ID or named role pair exists in production policy logic.

## 8. Tests-first evidence

Primary RED:

- `b5cad66aaaa5b5f89039dec15ded472b47a73658`;
- CI #3377 failed at unit-test compilation because the typed 3B3 feature/projector did not yet exist;
- R2 #3133 succeeded.

Structured-shadow integration RED:

- `450864d61e67c435a7d0d9316546de82d7cc05b4`;
- CI #3381: 1466 FAST tests, exactly one expected failure at the new impaired-narrative attachment assertion;
- R2 #3137 succeeded.

Integration GREEN:

- `36758d563a2a06ab7201f12a96b5bc3341e6b7da`;
- CI #3382: Android FAST and CI gate succeeded;
- R2 #3138 succeeded.

Accepted E2 checkpoint:

- `f3331c5fc914f960eb1a91cbc6a53135cb953242`;
- CI #3383 / workflow `35849386381`: Android FAST **SUCCESS**, Real Clingo **SUCCESS**, CI gate **SUCCESS**;
- R2 #3139 / workflow `35849386371`: **SUCCESS**;
- ASP contract tests were skipped by the incremental classifier.

T4 `[full-ci]` was not run; it remains reserved for overall SDE-3B acceptance.

## 9. Fanout / ownership audit

Relative to completed 3B2, production changes are limited to:

- typed impaired-narrative feature replacement in `DecisionFeatures`;
- pure `ImpairedNarrativeFeaturesProjector`;
- read-only `HistoricalImpairedNarrativeFeatureProjector`;
- structured-shadow attachment;
- the rules-owned established-interaction state seam.

No UI, session mutation, persistence schema, setup ownership, legacy selector, visible recommendation authority or V1 policy reason moved.

## 10. Deliberate exclusions

3B3 did not persist a Drunk world, infer hidden impairment from observation reliability, add role-specific policy branches, add numeric weights/thresholds, add a V1 preference reason, persist DecisionTrace, change visible recommendation or cut over automatic selection.

## 11. Next — SDE-3B4

SDE-3B4 is CURRENT.

Goal: derive remaining independently usable healthy-information routes after each candidate without a fixed healthy-information percentage or misinformation budget.

Start with architecture/fanout audit. Reuse canonical exact/strategic history, authoritative ability state/semantic truth and confirmation provenance. Do not promote legacy score/probability/misinformation pressure into feature truth.

Initial descriptive output should distinguish preserved healthy routes, lost healthy routes, whether any independent healthy route remains, and whether the candidate removes the last currently usable healthy route.
