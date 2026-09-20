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
7. this handoff

## 2. Live branch

Branch: `sde-2d5-calibration-policy-evidence`

Latest verified HEAD at handoff:

`171a779ab21bf967929cd4c9bcd917f93d2998af`

PR #150 remains **draft**.

Latest validation on that HEAD:

- CI run `35513739303` — **SUCCESS**
- R2 main-thread boundary run `35513739295` — **SUCCESS**
- FN-BUNDLE-3 calibration run `35513739283` — pending at handoff time; it is an independent heavy workflow and is not acceptance-blocking for this scope correction.

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
- Chef / Empath healthy values are rule-determined; do not “fix” them after setup.
- Washerwoman / Librarian / Investigator outputs can be Storyteller-controlled where legal.
- A topology-neutral clue can still be harmful through cross-confirmation.
- Player count matters for impaired information.
- Multi-night impaired information should follow a coherent false-world trajectory.
- Demon bluff quality must consider usability and narrative route diversity, not only coverage counts.
- No opaque global score is authorized.

## 8. NEXT action

Design and materialize a representative healthy-information calibration corpus.

Recommended first slice:

1. select several existing 7–9 player templates that are already considered playable;
2. for each, enumerate legal Washerwoman/Librarian/Investigator output alternatives;
3. retain rule-determined Chef/Empath values as fixed context;
4. include external-human / ClockTracker cases where the equivalent choice is observable;
5. sample middle-band cases plus a small number of clear pathologies as guardrails;
6. expose human-readable clue bundles;
7. add only representative cases to v3+ review membership;
8. verify `HEALTHY_BUNDLE_INFORMATION` coverage is satisfied before D5F-C.

Do not open sealed holdout evidence, freeze thresholds, cut production policy, begin SDE-3, or merge PR #150 without explicit authorization.
