# FN-BUNDLE-0 — Live Seam Audit and Experiment Contract

> Date: 2026-09-16 Australia/Sydney  
> Scope: FN-BUNDLE-0 only  
> Route authority: `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

## 1. Exit decision

FN-BUNDLE-0 is implementation-complete when the code in PR #139 is merged.

The phase established the live ownership map, the experiment-only complete-bundle contract, exact mutation-free bundle evaluation, and a measurable candidate-space census. It deliberately does **not** define Badness thresholds or a production selector.

The next implementation phase is **FN-BUNDLE-1 — Experiment 0 evaluator correctness fixtures**.

## 2. Live ownership map

### Pair-information legality

Canonical owner:

`recommendation/NaturalPairInformationCandidateGenerator.kt`

Reuse it for healthy:

- Washerwoman;
- Librarian;
- Investigator.

Important live-source finding: `recommendation/setup/SetupCandidateGenerator.kt` currently routes setup pair candidates for Librarian and Investigator only. FN-BUNDLE composition must therefore not treat that setup route as the complete pair-information owner, or Washerwoman is lost.

### Fixed numeric information

Canonical owners:

- `rules/FirstNightNumericInformationSemantics.kt`;
- `rules/FixedInformationEvaluator.kt`.

Reuse these for Chef and Empath. Do not count evil neighbours or adjacent evil pairs again inside recommendation code.

### Setup choices

Canonical owner:

`recommendation/setup/SetupCandidateGenerator.kt`

Reuse it for:

- Fortune Teller Red Herring candidates;
- demon bluff candidates.

These remain part of complete bundle identity even when they have no immediate observation in the first PUBLIC_GOOD_INFO projection.

### Player-controlled choice

Fortune Teller nightly target selection is excluded from Storyteller bundle optimization. It is explicitly surfaced by the candidate-space audit as player-controlled when Fortune Teller is in play.

### Staged complexity

The first healthy harness does not pretend to own:

- Drunk shown-role / false-information choices;
- Spy/Recluse registration expansion;
- Poisoner first-night target / dynamic impairment.

The candidate-space audit reports these as deferred complexity. When any is present, `legalCompleteBundleCount` is deliberately unknown rather than a false exact count.

## 3. Experiment contract

`recommendation/FirstNightBundleExperimentContract.kt` defines the smallest experiment-only types needed to compose complete bundles without moving rules ownership:

- `FirstNightInformationBundleEntry`;
- `FirstNightInformationBundle`;
- `FirstNightBundleEntryControl`;
- `FirstNightBundleProfileExposure`;
- `FirstNightBundleExperimentEvaluation`.

The first profile is only:

`BEGINNER_PUBLIC_GOOD_INFO`.

`PUBLIC_GOOD_INFO` is a behavioral stress projection, not durable observation visibility and not a rules claim.

Latent choices may belong to the complete bundle while carrying no immediate public observation.

## 4. Exact bundle evaluation seam

The exact epistemic foundation now supports several hypothetical observations as one conjunction over the same historical baseline world set.

Required semantics already established before this audit:

- mutation-free hypothetical evaluation;
- same-baseline conjunction for multiple observations;
- observation-order invariance;
- supported contradiction returns exact zero worlds rather than `DEFERRED`;
- `DEFERRED` remains capability-unavailable, not UNSAT;
- the existing single-observation preview routes through the bundle seam.

`FirstNightBundleExperimentEvaluator` adapts the experiment contract to that neutral exact evaluator and creates an ephemeral public-share projection. It does not commit a session timeline or mutate durable history.

## 5. Candidate-space census

`recommendation/FirstNightBundleCandidateSpaceAudit.kt` composes option identities from the existing owners only. It does not regenerate rules legality.

For a representative healthy seven-player Trouble Brewing setup:

- Washerwoman: 20 legal pair options;
- Investigator: 5 legal pair options;
- Chef: 1 fixed result;
- Empath: 1 fixed result;
- Red Herring: 5 legal choices;
- demon bluffs: 220 legal choices.

Complete legal Cartesian product:

`20 × 5 × 1 × 1 × 5 × 220 = 110,000` complete bundle identities.

This is large enough that FN-BUNDLE-2 must not blindly invoke exact evaluation once for every raw bundle identity.

## 6. Deterministic bounded-enumeration plan

Do **not** start with arbitrary sampling.

The first reduction is an exact equivalence quotient under the selected experiment profile:

1. compose complete legal bundle identities;
2. materialize the `BEGINNER_PUBLIC_GOOD_INFO` observation projection;
3. canonicalize the projected observation set;
4. group complete bundles that have the same projected observation signature;
5. exact-evaluate each distinct projection once;
6. retain all complete bundle IDs / multiplicity as provenance for that equivalence class.

For the representative seven-player census above, Red Herring and demon bluffs are complete-bundle choices but have no immediate PUBLIC_GOOD_INFO observation. Before observation-level deduplication, the represented public projection Cartesian upper bound is therefore:

`20 × 5 × 1 × 1 = 100`.

So the raw 110,000 complete bundle identities collapse to at most 100 exact PUBLIC_GOOD_INFO evaluations for this setup. Actual distinct projections may be lower after canonical observation materialization.

This quotient is lossless **for the selected experiment profile's exact public consequence**; it does not delete the latent choices from complete bundle identity.

If a later fixture still has too many distinct projected signatures after this exact quotient, FN-BUNDLE-2 may introduce deterministic bounded sampling. Any such sampling must:

- sample complete projected bundles, never independently score/select per role;
- use a stable seed derived from fixed setup/profile identity;
- be reproducible;
- preserve factor coverage/strata rather than taking an arbitrary prefix;
- report raw bundle count, distinct projected-signature count, evaluated count and whether sampling was applied;
- never silently reinterpret a sample as exhaustive evidence.

No numeric sampling threshold is chosen in FN-BUNDLE-0. It must be derived from measured evaluator cost in FN-BUNDLE-1/2.

## 7. Materialization boundary for FN-BUNDLE-1

FN-BUNDLE-0 intentionally stops before inventing a second rules-to-proposition engine.

FN-BUNDLE-1 should add only the smallest structural adapters/fixtures required to materialize representative observations:

- Investigator;
- Washerwoman;
- Librarian, including legal zero-Outsider information;
- Chef;
- Empath;
- representative Drunk false information.

Rules truth/legality stays with the existing producers. Materialization converts already-legal typed outcomes into epistemic propositions; it must not recalculate who the legal target is or what numeric result should be true.

Where no canonical production materializer exists, add one thin adapter at the ownership boundary and prove it with typed fixtures rather than embedding conversions in the harness.

## 8. Explicit non-goals

FN-BUNDLE-0 does not:

- define BEGINNER Badness thresholds;
- rank candidate bundles with a scalar score;
- select a production recommendation;
- optimize Fortune Teller target choice;
- expand Drunk / Spy / Recluse / Poisoner state space;
- add an LLM critic;
- persist experiment projections to the session timeline.

## 9. Next phase

Proceed to FN-BUNDLE-1 with focused evaluator-correctness fixtures and structural observation materialization only.

The first correctness target is not recommendation quality. It is:

> the same already-legal typed Night 1 information, when materialized and composed, produces exact deterministic BEFORE/AFTER consequences without mutation, hidden-state shortcuts or double application.
