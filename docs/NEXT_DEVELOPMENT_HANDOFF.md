# NEXT DEVELOPMENT HANDOFF — SDE-2D5F representative calibration correction

> Updated: 2026-09-20 Australia/Sydney  
> This is the **only active handoff**.

## 1. Read first

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`
5. `docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`
6. `docs/SDE_2D5F_EXTREME_FIXTURE_CALIBRATION_SCOPE_CORRECTION_2026-09-20.md`
7. `docs/SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md`
8. this handoff

## 2. Live branch

Branch: `sde-2d5-calibration-policy-evidence`

Latest representative implementation checkpoint before this documentation sync:

`cb1ffc454f87a7be8b166d8013746dee7f50bfda`

PR #150 remains **draft**.

Validation on that implementation checkpoint:

- CI run `35514598766` — **SUCCESS**
- R2 main-thread boundary run `35514598790` — **SUCCESS**
- FN-BUNDLE-3 calibration run `35514598760` — still independent/heavy; it is not acceptance-blocking for the representative materialization slice.

Documentation commits follow that implementation checkpoint, so always query the live branch before executable edits rather than treating the checkpoint SHA as current HEAD.

**Do not merge unless the user explicitly says “授权合并”.**

Always query the live branch before editing; do not assume the recorded HEAD is still current.

## 3. Current correction

The previous v2 role-information / confirmation-chain review set was derived from one fixed 7-player D5E diagnostic setup:

`Washerwoman / Chef / Empath / Fortune Teller / Investigator / Scarlet Woman / Imp`

Human review established that this setup is already pathological for the BEGINNER target. Chef=1 plus Empath=0 nearly collapses the game before Storyteller-controlled Washerwoman/Investigator choices are evaluated. Therefore labels from this setup have low value for calibrating the production choice policy.

Decision:

- retain the fixture and its metrics as diagnostic/regression evidence;
- mark its role-information and confirmation-chain review records `DIAGNOSTIC_ONLY`;
- exclude them from required human-label membership and gate derivation;
- preserve v2 labels only as historical/superseded evidence;
- do not continue labeling the remaining v2 Chef/Empath records.

## 4. Canonical manifest

Canonical:

`app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`

Version:

`d5f-b-calibration-v3`

It contains four retained judgments:

~~~text
d5f:bluff:13af34bc8befea2f:r1          BAD_TOO_WEAK
d5f:bluff:334926f04bd2c687:r1          ACCEPTABLE
d5f:bluff:fe702b4aac3ca49a:r1          ACCEPTABLE
d5f:drunk:baron-6-drunk-empath-seat-2  BAD_TOO_STRONG
~~~

Historical v2:

`app/src/test/resources/review/sde-2d5f-human-label-manifest-v2-obsolete-extreme-fixture.tsv`

Do not use v2 labels for gate derivation.

## 5. Gate state

D5F-C remains **BLOCKED**.

Manifest completeness alone is no longer sufficient. Validation also requires representative calibration coverage for SDE-owned policy variables.

Current deliberate gap:

`HEALTHY_BUNDLE_INFORMATION`

Therefore v3 can have zero `UNREVIEWED` entries while `isCompleteForGateDerivation == false`.

## 6. Why the ClockTracker work matters

The earlier ClockTracker real-game investigation was the correct direction because it samples real Storyteller choices and realistic information ecologies.

The next healthy-information calibration set should be anchored in:

- actual supported setup templates;
- ClockTracker / external-human cases where available;
- normal/playable 7–9 player setups;
- same-setup comparisons that vary only Storyteller/SDE-controlled output;
- table-level confirmation semantics, not raw-world count alone.

Avoid choosing cases merely because they maximize collapse, raw reduction, or another diagnostic extreme.

## 7. Frozen lessons that remain valid

- Optimize only variables still owned by the SDE at the current lifecycle stage.
- Chef / Empath are fixed context for this calibration, not optimization targets. If Spy/Recluse registration creates more than one legal healthy value, freeze and expose the registration branch rather than optimizing it as the reviewed variable.
- Washerwoman / Librarian / Investigator outputs can be Storyteller-controlled where legal.
- A topology-neutral clue can still be harmful through cross-confirmation.
- Player count matters for impaired information.
- Multi-night impaired information should follow a coherent false-world trajectory.
- Demon bluff quality must consider usability and narrative route diversity, not only coverage counts.
- No opaque global score is authorized.

## 8. Completed representative materialization slice

Implemented:

- design authority: `docs/SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md`;
- test-only corpus builder/renderer: `Sde2D5FRepresentativeHealthyInformationCorpus.kt`;
- contract tests: `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`;
- seven normal 7–9 player production presets spanning Washerwoman, Librarian, Investigator;
- production deal-planner seating rather than synthetic extreme seating;
- full legal pair-clue enumeration from the production generator;
- explicit Poisoner external context;
- fixed Chef/Empath context;
- actual / Spy-registration / Recluse-registration truth basis;
- complete human-readable table meaning.

The external-human audit found useful ecology/guardrail evidence but no clean 7–9 same-setup observed healthy pair-choice set. Do not manufacture direct labels from unlike external games.

No new record is `REVIEWABLE` yet. Canonical manifest remains `d5f-b-calibration-v3`, and the gate remains blocked on `HEALTHY_BUNDLE_INFORMATION`.

## 9. NEXT action

Attach representative strategic diagnostics and choose the bounded human-review subset.

1. evaluate these exact candidate identities through the existing topology-first 5–15 path;
2. expose Demon-cover, Evil-topology, Evil-cover, forced-good and feasibility separately;
3. keep full legal enumeration as the machine corpus;
4. choose human review items by semantic strata: good/Evil decoy, Demon/Minion inclusion, actual-Minon versus registration truth, and cross-confirmation with fixed context;
5. do not use FIRST/MIDDLE/LAST candidate ordering or diagnostic extrema as the sampling rule;
6. add a small number of evidence-backed guardrails only after the representative middle-band layer is stable;
7. only then add representative records to v3+ review membership and re-check `HEALTHY_BUNDLE_INFORMATION` coverage;
8. D5F-C remains blocked until that coverage is human-reviewed.

Do not open sealed holdout evidence, freeze thresholds, cut production policy, begin SDE-3, or merge PR #150 without explicit authorization.
