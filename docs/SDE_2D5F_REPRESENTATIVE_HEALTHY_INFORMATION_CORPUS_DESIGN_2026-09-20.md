# SDE-2D5F Representative Healthy-Information Corpus Design — 2026-09-20

## 1. Decision

The replacement for the obsolete D5E extreme-fixture review must be a **committed-setup, choice-within-setup corpus**.

The calibration question is not "which clue removes the most worlds?" It is:

> Given a normal setup that the app can actually produce, and with non-SDE choices already fixed, which legal Storyteller-controlled healthy clue keeps the BEGINNER information ecology reasonable?

The first corpus therefore samples production presets first, then enumerates legal clue outputs inside those presets. Metric extremes are not the sampling frame.

## 2. Production sampling frame

The production Trouble Brewing dataset contains 50 presets at each of 7, 8, and 9 players.

Among those pools, presets containing at least one healthy Washerwoman / Librarian / Investigator control surface are:

| players | pool | with controllable pair clue | beginner | standard |
|---:|---:|---:|---:|---:|
| 7 | 50 | 36 | 14 | 19 |
| 8 | 50 | 38 | 6 | 21 |
| 9 | 50 | 36 | 4 | 11 |

These presets are genuine production candidates. `TroubleBrewingProductionSetupPreparer` adapts the validated player-count pool into the generic setup provider and `SetupDiversitySelector`; the selected preset is then dealt by `TroubleBrewingSetupDealPlanner`.

For calibration, selecting a known preset from that validated production pool and passing it through the production deal planner is preferable to inventing a synthetic seating topology.

## 3. Lifecycle ownership

For this corpus:

- Washerwoman / Librarian / Investigator healthy legal outputs are `STORYTELLER_CONTROLLED`.
- Healthy Chef / Empath results are `RULE_DETERMINED` and remain fixed context.
- Fortune Teller target choice is player-controlled and is not varied by this corpus.
- Poisoner target choice is Evil-player-controlled. If a Poisoner setup is sampled, the target is fixed as external context and is never treated as an SDE variable.
- Drunk shown identity is already committed setup input and is not selected by this corpus.
- Demon bluffs / Red Herring are separate policy variables and are not varied in the first healthy-information slice.

## 4. Legal output ownership

Do not create a second legality implementation.

Healthy pair-information alternatives must come directly from:

`NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(...)`

This already covers:

- natural Washerwoman / Librarian / Investigator truths;
- Librarian "no Outsiders" when applicable;
- Spy good-role registration for Washerwoman / Librarian;
- Recluse Minion registration for Investigator;
- explicit target/decoy seat identity and registration provenance.

Chef / Empath fixed values must come from:

`FirstNightNumericInformationSemantics.healthyTruthValues(...)`

## 5. Corpus shape

The corpus is stratified rather than extreme-selected.

### Representative middle-band majority

Use existing `beginner` or `standard` production presets, spanning:

- 7, 8, and 9 players;
- Washerwoman, Librarian, and Investigator;
- clean pair-information cases;
- ordinary Poisoner setups with the Poisoner target fixed away from the reviewed healthy source;
- real Spy/Recluse registration cases where the production pool naturally contains them;
- setups with fixed Chef/Empath context where such normal templates exist.

Do not require every player count to contain Chef/Empath. In the current 9-player pool, every preset that combines a controllable pair role with Chef/Empath is `advanced`; forcing such a condition would make the sample less representative.

### Guardrails

Add only a small number of clear pathologies after the representative layer exists.

Guardrails may come from:

- external-human postmortems showing information-poor ecology;
- supported setups in which cross-confirmation clearly collapses Evil;
- supported same-setup legal outputs that create an obvious confirmation chain.

Guardrails are not selected merely because they maximize a topology diagnostic.

## 6. First materialization batch

The first batch is intentionally small and interpretable:

| preset | players | complexity | active healthy control | important context |
|---|---:|---|---|---|
| TB2_7_015 | 7 | beginner | Washerwoman | clean Scarlet Woman setup |
| TB2_7_037 | 7 | beginner | Librarian | Butler + Saint + Baron |
| TB2_7_006 | 7 | standard | Investigator | Empath fixed; Recluse registration + Baron |
| TB2_8_010 | 8 | beginner | Washerwoman | Saint + Poisoner; Poisoner target fixed externally |
| TB2_8_012 | 8 | standard | Investigator | Chef + Empath fixed; Recluse registration + Scarlet Woman |
| TB2_9_008 | 9 | beginner | Librarian | Butler + Saint + Poisoner; Poisoner target fixed externally |
| TB2_9_031 | 9 | beginner | Washerwoman | Butler + Saint + Poisoner; Poisoner target fixed externally |

This is not yet the final review membership. It is the first representative materialization surface from which reviewable contrasts can be chosen without using a diagnostic extreme as the sampling rule.

### Implemented first slice

The test-only implementation now lives in:

- `Sde2D5FRepresentativeHealthyInformationCorpus.kt`
- `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`

It:

- parses and validates the production preset dataset;
- passes each selected preset through `TroubleBrewingSetupDealPlanner`;
- fixes Poisoner targets only as explicit Evil-player-controlled context;
- enumerates every legal healthy pair clue through `NaturalPairInformationCandidateGenerator`;
- exposes Chef/Empath as fixed context rather than an optimization target;
- distinguishes actual-role truth from Spy/Recluse registration truth;
- renders a complete seat table and human-readable clue meaning;
- does not add labels, policy gates, thresholds, or manifest membership.

Checkpoint validation on implementation head `cb1ffc454f87a7be8b166d8013746dee7f50bfda`:

- CI run `35514598766` — SUCCESS;
- R2 run `35514598790` — SUCCESS.

### Human-review subset selection

Full legal enumeration is the **machine corpus**, not the human labeling workload. A normal setup can expose tens of legal pair clues.

Human review should select a small semantic-strata subset from each setup, using table meaning rather than metric extrema. Useful strata include:

- decoy actual alignment/type, especially good versus Evil;
- whether the pair directly contains the Demon or Minion;
- Investigator actual-Minion truth versus Recluse-registration truth;
- Spy-registration truth where present;
- whether the candidate overlaps a fixed Chef/Empath confirmation route;
- whether two otherwise similar choices create materially different narrative routes.

Do not select review items by FIRST/MIDDLE/LAST candidate index or by lowest/highest global diagnostic.

## 7. Human-readable table meaning

Every candidate record must expose enough information for a Storyteller to judge the table, without decoding raw proposition/world objects:

1. preset ID, player count, complexity, source, style tags;
2. production-dealt seat -> actual role -> shown role;
3. fixed external context such as Poisoner target;
4. rule-determined Chef / Empath values;
5. active Storyteller-controlled source role and seat;
6. for every legal candidate:
   - shown role;
   - the two shown seats, or Librarian no-Outsider result;
   - true/registration anchor seat;
   - decoy seat;
   - truth basis: actual role, Spy registration, or Recluse registration;
   - stable candidate ID for traceability.

Strategic diagnostics are supplemental columns. They must not replace the table meaning.

## 8. Diagnostic integration

After semantic materialization is stable, evaluate the same candidates through the existing topology-first 5–15 path:

`TroubleBrewingTopologyHypotheticalBundleEvaluator`
-> `NormalizedStrategicDiagnosticsProjector`

The first integration must preserve explicit components:

- Demon-cover retention;
- Evil-topology retention;
- Evil-cover retention;
- forced-good fraction;
- candidate feasibility.

No aggregate score is introduced.

For multi-clue scenarios, fixed healthy context and leave-one-out comparisons may later expose cross-confirmation, but the first implementation varies one SDE-owned pair-information factor at a time.

## 9. External-human evidence use

ClockTracker / Reddit evidence is an anchor, not a direct label generator.

Use it to:

- justify ecology dimensions and guardrails;
- prefer realistic narrative/pressure patterns;
- identify information-floor and confirmation-chain pathologies;
- validate that the generated human-readable cases resemble real Storyteller decisions.

Do not map a qualitative external report onto a different setup as if it were an exact observed choice.

Current catalog audit: the external-human evidence contains strong ecology/guardrail evidence, but it does not provide a clean 7–9 player same-setup observed alternative set for healthy Washerwoman/Librarian/Investigator choices. The ClockTracker 14-player case is valuable real-choice evidence but includes impaired information and a different player-count regime. Therefore external evidence should constrain dimensions and guardrails here, not be converted into synthetic direct labels.

## 10. Gate discipline

The first materialization slice does **not** satisfy `HEALTHY_BUNDLE_INFORMATION` review coverage by itself.

Only after representative records contain:

- committed production setup context;
- legal same-setup alternatives;
- human-readable table meaning;
- supported topology diagnostics;
- deliberate review membership;

may they become `REVIEWABLE` and enter the canonical manifest.

Until then:

- D5F-C remains blocked;
- no thresholds are frozen;
- sealed holdout remains closed;
- SDE-3 remains blocked.
