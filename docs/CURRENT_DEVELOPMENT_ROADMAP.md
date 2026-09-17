# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence / D6 ownership decomposition                 COMPLETE
UI-R5 through DAY-UI-1                                  COMPLETE / merged
EPI-MQ capability + exact historical hypothetical seam   COMPLETE / merged
FN-BUNDLE-0 candidate-space audit + pair cleanup         COMPLETE / PR #139
FN-BUNDLE-1 proposition / shown-role semantics           COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle exact harness           COMPLETE / PR #142

CURRENT:
FN-BUNDLE-3 — BEGINNER human-review corpus / PR #143
  Stage 7A — real 7-player template robustness calibration
    -> Storyteller-controlled whole-bundle candidates
    -> Fortune Teller Red Herring included as a Storyteller-controlled factor
    -> every legal Fortune Teller target pair treated as a player-controlled robustness axis
    -> exact diagnostics; no scalar rank / no gate yet

NEXT:
finish one Fortune-Teller-containing real-template Stage 7A pilot
expand Stage 7A to all 11 healthy-compatible 7-player templates if pilot cost is viable
manual calibration labels
interpretable diagnostic separation
BEGINNER gate proposal only if calibration evidence supports it
sealed holdout evaluation only after gates are frozen
external Storyteller-data calibration where usable
then Drunk -> Spy/Recluse -> Poisoner staged expansion

DEFERRED:
production recommendation-provider cutover
unified scalar ranking
LLM / ML soft critic
later skill profiles
```

Live merged FN-BUNDLE-2 baseline:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

Current route decision:

`docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

Current active PR:

`#143 FN-BUNDLE-3: build BEGINNER review corpus`

Branch:

`fn-bundle-3-beginner-corpus`

Always query live `main`, PR head and CI before editing.

## 2. Stable completed bundle foundation

### FN-BUNDLE-0 — merged #139

Stable results:

- complete healthy Night-1 candidate-space census;
- representative 7-player raw Cartesian product `110,000`;
- represented public factor-product upper bound `100`;
- Fortune Teller **target selection** excluded because it is player-controlled;
- Fortune Teller **Red Herring designation** remains a Storyteller-controlled setup choice and is therefore part of the complete Storyteller candidate bundle when Fortune Teller is present;
- Drunk / Spy-Recluse / Poisoner explicitly staged for later;
- duplicate setup-owned pair-information semantics retired.

Canonical pair-information ownership remains:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical consumers
```

Canonical Red Herring legality remains owned by the existing setup candidate producer. Stage 7A must consume that producer; it must not implement a second Red Herring rules engine.

Do not repeat the FN-BUNDLE-0 census or pair-ownership audit.

### FN-BUNDLE-1 — merged #140

Stable results:

- thin first-night information -> epistemic proposition materialization;
- strict `InformationProposition.ShownRoleAt(seat, role)` semantics;
- JSON / exact / knowledge-boundary fanout complete;
- no hidden Drunk actual-role leakage through `ShownRoleAt`;
- exact healthy and Drunk semantic fixtures;
- T4 green before merge.

Important: strict `ShownRoleAt` remains a valid mechanical proposition. What changed in FN-BUNDLE-3 is **whether ordinary Day-1 player speech should be projected as that mechanically verified proposition**.

### FN-BUNDLE-2 — merged #142

Squash-merge baseline:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

Delivered:

```text
canonical legal factor producers
    ↓
complete-bundle factorized provenance
    ↓
PUBLIC_GOOD_INFO projection
    ↓
canonical projected signature
    ↓
lossless quotient
    ↓
exact diagnostics once per distinct signature
    ↓
leave-one-out diagnostics
```

Representative healthy 7-player contract:

```text
raw complete bundles                110,000
represented public combinations         100
distinct projected signatures           100
latent multiplicity / public signature 1,100
sampling                                none
```

Structural diagnostics are owned by `epistemic`, not Badness policy:

- exact BEFORE / AFTER world counts;
- possible demon seats / demon cover;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover;
- leave-one-out interaction evidence.

No final scalar score or rejection threshold exists.

The pristine Night-1 exact path uses lazy source enumeration so the unconstrained 7-player world family is not materialized in full. Historical replay remains on the existing historical exact path.

The healthy diagnostic counterworld domain deliberately excludes:

```text
Drunk
Spy
Recluse
Poisoner
```

Candidate legality still uses the canonical official Trouble Brewing producers.

## 3. FN-BUNDLE-3 — current PR #143

Purpose: establish a human-reviewed BEGINNER corpus before defining Badness gates.

Labels remain:

```text
UNREVIEWED
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

No label is derived mechanically from one diagnostic.

### 3.1 Stage 7A ownership correction — Fortune Teller

Stage 7A evaluates the complete first-night **Storyteller decision surface**, not merely exposed Day-1 clue rows.

For Fortune Teller, two different decisions must not be conflated:

```text
Red Herring designation
    = Storyteller-controlled
    = candidate-bundle factor
    = algorithm output target

Fortune Teller chooses two players to inspect
    = player-controlled
    = NOT a Storyteller candidate factor
    = robustness / environment axis
```

Therefore a real-template Stage 7A candidate is conceptually:

```text
all Storyteller-controlled legal first-night choices
including Red Herring where Fortune Teller is in play
```

and its robustness evidence is:

```text
for each legal Fortune Teller target pair:
    derive the mechanically correct result under the chosen Red Herring
    add the resulting Fortune Teller public claim to the same complete information ecology
    exact-evaluate consequences
```

The Red Herring choice must come from canonical production legality. The Fortune Teller result must come from canonical production rules/semantics. Review code must not duplicate either rule.

A single unusually strong player-selected pair can be normal game variance. A Storyteller bundle is more concerning when **many legal Fortune Teller target pairs** repeatedly produce destructive confirmation-chain or evil-topology collapse. Stage 7A must preserve the per-query evidence needed to distinguish those cases.

### 3.2 Stage 7A enumeration policy

The old real-template pilot selected correlated `FIRST / MIDDLE / LAST` options from each public factor. That was useful only as a feasibility probe. It is **not production-calibration evidence** and must not be used to infer clue-quality gates.

Reworked Stage 7A policy:

1. start with one healthy real 7-player preset containing Fortune Teller;
2. enumerate the complete Storyteller-controlled candidate product for that fixed setup/seating, including every legal Red Herring;
3. for each candidate, enumerate every legal Fortune Teller target pair;
4. exact-evaluate every resulting public-information ecology;
5. retain interpretable per-query diagnostics plus a non-scalar robustness envelope;
6. measure execution cost and eliminate only redundant projected signatures, never approximate possible worlds;
7. if viable, expand the same method to all 11 healthy-compatible 7-player presets and the retained seating profiles.

Exactness is not weakened for performance. If experiment cost becomes excessive, bound the number of **real setup/seating scenarios** or split the experiment workload; do not sample possible worlds and do not silently return to FIRST/MIDDLE/LAST candidate sampling.

### 3.3 Calibration / holdout discipline

Partition by complete setup + seating scenario, never random signature rows from the same scenario.

The existing calibration corpus and bounded experiments remain useful historical evidence, but Stage 7A real-template robustness is now the production-relevant calibration path before gates.

Selection/display reasons are review aids only. They are not Badness rules.

The sealed holdout must remain unevaluated during calibration. Do not inspect, label or tune against holdout diagnostics until a candidate BEGINNER gate set has been frozen from calibration evidence.

## 4. Critical PUBLIC_GOOD_INFO semantic correction

The first FN-BUNDLE-3 corpus run exposed a modeling error in the earlier projection.

The old projection turned a Day-1 statement such as:

```text
"I am Investigator; my information is X"
```

into mechanically verified public facts equivalent to:

```text
ShownRoleAt(speaker, Investigator)
AND
X
```

That incorrectly made public bluff claims act like Storyteller-confirmed identity. The first pilot report therefore collapsed all sampled worlds around the actual good/evil seating and is **invalid for Badness calibration**. It must not be labeled or used to derive gates.

Current healthy-stage behavioral semantics are instead:

```text
speaker is evil
OR
(
    speaker really has the claimed shown role
    AND
    the claimed clue is mechanically true
)
```

Interpretation for this stage:

- healthy good speakers are assumed to share truthfully under the stress profile;
- evil speakers may make the same public statement as a bluff;
- public speech is not an oracle identity fact;
- `ShownRoleAt` itself remains a strict exact proposition when genuinely known through an appropriate mechanical source.

This public-claim model is explicitly scoped to the current healthy stage. Drunk false information, Poisoner impairment and Spy/Recluse registration are not silently folded into it.

The pristine exact evaluator uses a **necessary-only identity prefilter** for this claim form so the 7-player experiment remains memory-safe. The complete claim is still evaluated exactly afterwards; the prefilter is not a second semantics engine.

## 5. Holdout discipline

The first pilot CI report printed both calibration and holdout details. That original holdout is contaminated and retired.

A replacement holdout is sealed. Calibration work must not evaluate its diagnostics at all; human-facing output may report only the sealed holdout scenario count.

Do not inspect, label or tune against replacement holdout diagnostics until a candidate BEGINNER gate set has been frozen from calibration evidence.

## 6. Experimental testing rule

Do not manufacture RED tests merely to satisfy process for exploratory experiment/spike work.

```text
exploration / feasibility / measurement
    -> implementation-first allowed
    -> do not manufacture RED

stable retained behavior / architecture
    -> add only necessary durable contract or regression coverage
    -> run affected validation and required T4 checkpoint
```

Heavy Stage 7A enumeration belongs to the explicit/on-demand FN-BUNDLE calibration experiment, not routine `testFast` / `testFull`. Stable bounded contracts may verify report shape, ownership assumptions and deterministic enumeration boundaries without executing the full corpus on every ordinary CI run.

## 7. Product decision remains unchanged

Target behavior:

```text
rules-legal complete first-night bundles
    ↓
exact / capability-aware consequence diagnostics
    ↓
BEGINNER Badness rejection
    ↓
acceptable bundle pool
    ↓
uniform random selection
```

Do not introduce one opaque maximized scalar score.

Do not call unweighted world fractions posterior probabilities without an explicit prior / weighting model.

Before gates, continue to inspect at least:

- BEFORE / AFTER world counts;
- demon-seat diversity;
- distinct evil-team configurations;
- forced-good / forced-evil structure;
- evil and demon cover;
- leave-one-out / interaction recovery where applicable;
- Fortune Teller per-target-pair robustness where Fortune Teller is present;
- minimum useful information;
- structurally meaningful alternative worlds where tractable.

## 8. Current acceptance checkpoint

Stage 7A is currently an **experiment implementation checkpoint**, not a gate-tuning checkpoint.

Acceptance for the first Fortune-Teller-containing pilot requires:

```text
canonical Red Herring candidates are all represented
Storyteller-controlled candidate combinations are complete for the fixed scenario
Fortune Teller target pairs are all represented as player-controlled robustness cases
Fortune Teller results use canonical semantics
PUBLIC_GOOD_INFO projection remains defeasible speech
possible-world evaluation remains exact
per-query structural diagnostics are retained
no scalar score / Badness threshold is introduced
holdout remains unopened
```

Only after pilot correctness and cost are demonstrated should Stage 7A expand to all healthy real 7-player templates.

## 9. Next execution order

1. replace the old real-preset FIRST/MIDDLE/LAST Stage 7A experiment contract with the Fortune Teller-aware ownership model;
2. locate/reuse canonical Red Herring candidate generation and canonical Fortune Teller result semantics;
3. implement one real Fortune-Teller-containing 7-player preset pilot;
4. enumerate complete Storyteller-controlled candidates for the pilot, including every legal Red Herring;
5. enumerate every legal Fortune Teller target pair for each candidate and exact-evaluate the resulting information ecology;
6. emit interpretable per-query diagnostics and robustness ranges/counts, with no scalar ranking;
7. run the dedicated calibration experiment and inspect cost/output;
8. expand to all 11 healthy-compatible 7-player presets if the pilot is viable;
9. then resume human calibration labeling;
10. derive simple interpretable gate hypotheses only when evidence supports them;
11. freeze candidate gates;
12. only then evaluate sealed holdout;
13. audit external Storyteller data such as ClockTracker as ecological calibration evidence;
14. later widen Drunk -> Spy/Recluse -> Poisoner.

## 10. Stable architecture boundary

```text
rules          -> legality / registration / Fortune Teller result semantics
session        -> canonical actual state / timeline / commit
epistemic      -> exact recipient-visible consequence semantics
recommendation -> complete Storyteller bundle composition / future Badness / survivor selection
UI             -> presentation / confirmation
```

Badness must never become a second rules engine.

Fortune Teller target choice remains player-owned gameplay input even though Stage 7A enumerates it as an experiment robustness dimension. Red Herring remains Storyteller-owned and therefore belongs in the algorithm's candidate output surface.