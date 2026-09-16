# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-16 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence / D6 ownership decomposition                 COMPLETE
UI-R5 through DAY-UI-1                                  COMPLETE / merged
EPI-MQ capability + exact historical hypothetical seam   COMPLETE / merged
FN-BUNDLE-0 candidate-space audit + pair cleanup         COMPLETE / PR #139
FN-BUNDLE-1 proposition / shown-role semantics            COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle exact harness             COMPLETE / PR #142

CURRENT:
FN-BUNDLE-3 — BEGINNER human-review corpus / PR #143

NEXT:
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
- Fortune Teller target selection excluded because it is player-controlled;
- Drunk / Spy-Recluse / Poisoner explicitly staged for later;
- duplicate setup-owned pair-information semantics retired.

Canonical pair-information ownership remains:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical consumers
```

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

### Pilot split

Partition by complete setup + seating scenario, never random signature rows from the same scenario.

Current pilot:

- one CALIBRATION scenario;
- one sealed HOLDOUT scenario;
- one explicit good-player anchor perspective per scenario;
- all projected signatures evaluated for selection;
- only a small deterministic set of extreme / representative signatures exposed for review.

Selection reasons include AFTER-count extremes, demon-cover minimum, evil-topology minimum, forced-good maximum, leave-one-out interaction maximum, and quartile representatives. These are review-sampling reasons, **not Badness rules**.

`FirstNightBundleBeginnerCorpusReviewTest` is T3/full coverage and is intentionally excluded from `testFast`.

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

The pristine exact evaluator now uses a **necessary-only identity prefilter** for this claim form so the 7-player experiment remains memory-safe. The complete claim is still evaluated exactly afterwards; the prefilter is not a second semantics engine.

## 5. Holdout discipline

The first pilot CI report printed both calibration and holdout details. That original holdout is therefore contaminated and retired.

A replacement holdout has been created. Human-review export now prints **CALIBRATION only** and reports only the number of sealed holdout scenarios.

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

The current public-claim change reused and updated retained contracts; no RED-only fixture was added.

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
- leave-one-out / interaction recovery;
- minimum useful information;
- structurally meaningful alternative worlds where tractable.

## 8. Current acceptance checkpoint

Current ordinary FAST validation after the public-claim correction must be green before the final checkpoint.

Then the current PR head must receive an explicit `[full-ci]` checkpoint covering:

```text
Android :app:testFull + :app:assembleDebug
ASP corpus + harness
Real Clingo cross-validation
R2 main-thread boundary
aggregate CI gate
```

After T4 is green, inspect **only the CALIBRATION report**.

The first empirical acceptance question is not a numeric Badness threshold. It is:

> Does defeasible public-claim modeling preserve plausible evil bluff worlds instead of automatically treating every claimed good role as mechanically confirmed?

Only after that is demonstrated should manual calibration labeling resume.

## 9. Next execution order

1. finish current FAST convergence;
2. update/checkpoint this documentation with `[full-ci]`;
3. require current-head T4 green;
4. read calibration-only report;
5. verify that forced-good/forced-evil and evil topology are no longer trivially fixed by role claims;
6. manually label calibration items;
7. add more calibration scenarios if one scenario is too narrow;
8. derive simple interpretable gate hypotheses only when evidence supports them;
9. freeze candidate gates;
10. only then evaluate sealed holdout;
11. audit external Storyteller data such as ClockTracker as ecological calibration evidence;
12. later widen Drunk -> Spy/Recluse -> Poisoner.

## 10. Stable architecture boundary

```text
rules          -> legality / registration semantics
session        -> canonical actual state / timeline / commit
epistemic      -> exact recipient-visible consequence semantics
recommendation -> bundle composition / future Badness / survivor selection
UI             -> presentation / confirmation
```

Badness must never become a second rules engine.
