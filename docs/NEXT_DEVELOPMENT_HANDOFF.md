# NEXT DEVELOPMENT HANDOFF — SDE-2D5F expert-observed policy calibration

> Updated: 2026-09-21 Australia/Sydney  
> This is the **only active handoff**.

## 1. Read first

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`
5. `docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`
6. `docs/SDE_2D5F_EXTREME_FIXTURE_CALIBRATION_SCOPE_CORRECTION_2026-09-20.md`
7. `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`
8. this handoff

The older `SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md` is historical/superseded for active calibration.

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

## 6. Evidence direction

The earlier ClockTracker investigation was directionally correct because it moved calibration toward real Storyteller choices and realistic information ecologies.

The correction is that **real does not automatically mean expert**.

Evidence priority is now:

1. GOLD — reconstructable real games from trusted/experienced Storytellers, with explicit rationale when available;
2. SILVER — high-fidelity ClockTracker/equivalent logs whose Storyteller quality is not independently established;
3. QUALITATIVE — experienced Storyteller tutorials, community postmortems and repeated rules of thumb;
4. DIAGNOSTIC_ONLY — synthetic/extreme/counterfactual fixtures.

Use observed expert choices as anchors and compare them to the legal alternatives that existed at that moment. Do not label every unchosen alternative as bad, and do not use the eventual game winner as a Storyteller-quality label.

## 7. Frozen lessons that remain valid

- Optimize only variables still owned by the SDE at the current lifecycle stage.
- Chef / Empath are fixed only when exactly one healthy value remains after legal Spy/Recluse registration branches are considered. Multiple legal values mean the interaction-scoped registration ruling is Storyteller-controlled and belongs in the global bundle.
- Washerwoman / Librarian / Investigator outputs can be Storyteller-controlled where legal.
- A topology-neutral clue can still be harmful through cross-confirmation.
- Player count matters for impaired information.
- Multi-night impaired information should follow a coherent false-world trajectory.
- Demon bluff quality must consider usability and narrative route diversity, not only coverage counts.
- No opaque global score is authorized.

## 8. Superseded clean materialization slice

The seven 7–9 player test-only scenarios implemented at `cb1ffc454f87a7be8b166d8013746dee7f50bfda` are no longer an active calibration stratum.

They deliberately excluded Drunk and froze registration-dependent numeric context. That made them useful for proving a narrow materialization seam, but too artificial for the real first-night policy problem.

Do not spend further work attaching policy diagnostics or human labels to those scenarios.

Audit:

- `Sde2D5FRepresentativeHealthyInformationCorpus.kt`;
- `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`;
- `SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md`.

If their unique durable behavior is already protected by canonical legality/generator tests, delete the test-only corpus implementation and archive/remove the active design document reference. Do not retain it merely because it was already implemented.

## 9. NEXT action — expert-observed corpus

Build a small high-quality real-game corpus before any new policy labels.

1. discover expert/trusted Trouble Brewing Storyteller sources, prioritizing official/TPI-affiliated productions, established expert channels, and high-fidelity public logs;
2. select cases where committed setup, seating and Night 1 Storyteller decisions can be reconstructed;
3. record provenance/confidence, player experience, Drunk shown identity, Demon bluffs, red herring, relevant Spy/Recluse registrations, Poisoner context and first-night outputs;
4. reconstruct the exact legal alternatives at each Storyteller-controlled decision with existing production legality owners;
5. run whole-bundle/topology diagnostics for the observed choice and legal counterfactuals;
6. treat explicit rationale / explicit rejection / repeated comparable expert choices as strong preference evidence;
7. treat one observed choice without rationale as weaker evidence, not an automatic label for all alternatives;
8. use broader ClockTracker records as a generalization layer after GOLD patterns emerge;
9. use the project owner's review only for reconstruction checks, conflicting evidence and BEGINNER-specific adaptation;
10. keep D5F-C blocked until policy constraints are grounded in this expert-observed evidence.

Do not open sealed holdout evidence, freeze thresholds, cut production policy, begin SDE-3, or merge PR #150 without explicit authorization.
