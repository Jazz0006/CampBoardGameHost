# FN-BUNDLE-3 — BEGINNER pilot review corpus

> Date: 2026-09-16 Australia/Sydney  
> Base: FN-BUNDLE-2 merged at `690bc93b33b87fd54a911f4b9dbfc770f2a16a51`  
> Status: **pilot calibration / public-claim semantic correction; no Badness gates yet**

## Purpose

FN-BUNDLE-3 tests whether FN-BUNDLE-2 exact diagnostics correspond to human judgments about first-night information quality for the initial:

```text
BEGINNER / PUBLIC_GOOD_INFO
```

This stage does not train a scalar score and does not invent rejection thresholds before review evidence exists.

## 1. Public-share behavioral model

The profile assumes healthy good players aggressively share their first-night role/information claims on Day 1.

This is a behavioral stress model, not a rule that public speech is trustworthy.

### Invalid first projection

The first implementation projected a statement such as:

```text
"I am Empath and my result is 0"
```

as two exact public facts:

```text
ShownRoleAt(speaker, Empath)
AND
NumericResult(0)
```

That accidentally made a public role claim equivalent to Storyteller confirmation. The first generated corpus consequently over-collapsed the world space around the actual seating.

That first corpus run is **invalid calibration evidence**:

- do not label it;
- do not derive Badness gates from it;
- do not use its world counts as reference thresholds.

### Correct current healthy-stage projection

One public statement is now modeled as:

```text
speaker is evil
OR
(
    speaker really has the claimed shown role
    AND
    the claimed clue is mechanically true
)
```

This encodes the intended first-stage behavior:

- healthy good speaker -> truthful public share;
- evil speaker -> may bluff the same claim;
- ordinary speech -> no oracle identity confirmation.

Strict `InformationProposition.ShownRoleAt` itself remains unchanged and exact when a shown role is genuinely known through an appropriate mechanical source.

Drunk false information, Poisoner impairment and Spy/Recluse registration remain later staged models. Do not infer their future public-claim semantics from this healthy-only formulation.

## 2. Exact-evaluator performance boundary

A public claim no longer exposes a top-level strict identity fact. Without a prefilter, pristine 7-player evaluation would retain too much of the source world family and reintroduce the earlier memory problem.

The pristine evaluator therefore uses a necessary-only identity envelope:

```text
speaker can satisfy evil branch
OR
shown role matches claimed role
```

The complete claim is still exact-evaluated afterwards.

The prefilter is therefore performance-only:

- it must never decide the final truth of the claim;
- it may conservatively retain extra worlds;
- historical replay remains unchanged;
- no A4/ZDD production rollout decision changes.

Recluse is conservatively retained by the prefilter for future registration safety, although the current healthy diagnostic domain excludes Recluse.

## 3. Pilot partitions

Partitioning is by complete setup + seating scenario, not by individual signature.

This prevents highly related signatures from the same position leaking between calibration and holdout.

### CALIBRATION A — pair-rich interaction stress

Scenario:

`cal-pair-rich-adjacent-evil`

```text
1 Washerwoman
2 Chef
3 Empath
4 Fortune Teller
5 Investigator
6 Scarlet Woman
7 Imp
```

Anchor perspective: seat 1.

This scenario deliberately has several mutually interacting public first-night claims and is intended to expose confirmation-chain collapse as well as acceptable middle cases.

### CALIBRATION B — low-information stress

Scenario:

`cal-low-information-two-sources`

```text
1 Washerwoman
2 Chef
3 Monk
4 Soldier
5 Virgin
6 Scarlet Woman
7 Imp
```

Anchor perspective: seat 1.

Only Washerwoman and Chef contribute first-night public information in this healthy-stage setup. The scenario exists specifically to give calibration evidence on the weak-information side rather than deriving `BAD_TOO_WEAK` ideas from a clue-rich setup alone.

Every projected signature in both calibration scenarios is evaluated. Only a small deterministic review subset is exported.

### HOLDOUT

The original holdout was exposed in the first CI report and is therefore retired as a validation holdout.

A replacement holdout exists in the corpus builder but is now **sealed** for human review.

Before gates are frozen, exported review material must reveal only:

```text
sealed holdout scenario count
```

It must not reveal:

- holdout scenario ID;
- seating/setup;
- selected signatures;
- diagnostics;
- labels.

## 4. Review-item selection

Every legal projected signature is still evaluated for the scenario anchor.

The calibration export selects a small deterministic set per calibration scenario, deduplicating overlaps among:

```text
LOWEST_AFTER_WORLD_COUNT
HIGHEST_AFTER_WORLD_COUNT
SMALLEST_DEMON_COVER
FEWEST_EVIL_CONFIGURATIONS
MOST_FORCED_GOOD
LARGEST_LEAVE_ONE_OUT_RECOVERY
LOWER_QUARTILE_AFTER
UPPER_QUARTILE_AFTER
```

These are review-sampling reasons, not quality labels and not candidate-ranking rules.

## 5. Labels

Every generated item begins:

```text
UNREVIEWED
```

Manual review may assign:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

The judgment concerns the whole public first-night information ecology for a beginner table. No single metric maps mechanically to a label.

Assistant analysis may propose provisional review suggestions, but those suggestions must remain explicitly distinct from final human-reviewed labels.

## 6. Evidence preserved

Each internal corpus item retains:

- stable scenario/signature identity;
- represented complete-bundle multiplicity;
- public claims;
- exact BEFORE / AFTER world counts;
- demon cover;
- distinct evil-team configurations;
- forced-good / forced-evil seats;
- evil cover;
- leave-one-out evidence.

Do not describe unweighted exact-world fractions as posterior probabilities.

## 7. Cost boundary

An exploratory implementation evaluated several scenarios and expanded selected items across every good-player perspective. That was unnecessarily expensive for the first human-review loop.

The retained pilot uses one explicit anchor perspective per scenario.

Cross-recipient robustness remains a later experiment and should be added only if the first calibration evidence shows perspective dependence matters materially.

`FirstNightBundleBeginnerCorpusReviewTest` is T3/full coverage and is excluded from `testFast`.

The low-information calibration scenario was added only after the corrected pair-rich report showed that one clue-rich setup did not span the weak-information region well enough for calibration.

## 8. Testing rule

Do not manufacture RED for experiment ceremony.

```text
exploratory spike / measurement
    -> implementation-first allowed

stable retained contract
    -> only necessary regression/contract coverage
    -> required FAST/T4 acceptance
```

The public-claim correction updated retained contracts; it did not add a RED-only test stage.

The low-information calibration expansion likewise reuses the retained corpus contract instead of adding a ceremonial failing test.

## 9. Current acceptance evidence and checkpoint

The corrected public-claim model already passed one complete T4 checkpoint at head `130a109349d8bbec8fbdd920227581298b8dac2b`:

- Android full + debug APK: green;
- ASP contract/golden validation: green;
- Real Clingo cross-validation: green;
- R2 main-thread boundary: green;
- aggregate CI gate: green;
- calibration-only report export: green;
- sealed holdout details omitted.

That report demonstrated that public claims no longer automatically lock the actual evil team: sampled pair-rich signatures ranged from broad evil-bluff counterworlds to genuine combination-collapse cases.

After adding the second low-information calibration scenario, ordinary FAST and R2 are green at `04f43f2609c2b94d5563979e696b78738b544572`.

This documentation commit intentionally uses `[full-ci]` to require one new current-head full/T4 checkpoint that includes the low-information calibration scenario before its diagnostics are used for review.

No numeric Badness threshold is part of this checkpoint.

## 10. Calibration / holdout discipline

Calibration evidence may be used to propose simple interpretable gate hypotheses.

Holdout must not be used to choose those gates.

After candidate gates are frozen, evaluate against holdout and report at least:

```text
false accept: human-bad bundle passes
false reject: human-acceptable bundle is rejected
uncertain: reported separately
```

Do not move inconvenient holdout cases into calibration.

## 11. Next step

After this current-head T4:

1. inspect the calibration-only report for both calibration scenarios;
2. compare the pair-rich and low-information diagnostic ranges;
3. produce provisional review suggestions without pretending they are final human labels;
4. manually settle calibration labels;
5. inspect whether diagnostics meaningfully separate those labels;
6. add more calibration scenarios only if the two-scenario pilot still leaves an important structural gap;
7. propose simple gates only when evidence is sufficient;
8. freeze those gates;
9. only then open sealed holdout evaluation.

Real Storyteller records such as ClockTracker remain an external ecological calibration layer after this deterministic baseline is trustworthy. Historical Storyteller choices are evidence, not unique ground-truth labels, and unchosen alternatives must not automatically become negative training examples.
