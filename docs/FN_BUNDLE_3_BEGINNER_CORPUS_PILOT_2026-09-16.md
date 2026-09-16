# FN-BUNDLE-3 — BEGINNER pilot review corpus

> Date: 2026-09-16 Australia/Sydney  
> Base: FN-BUNDLE-2 merged at `690bc93b33b87fd54a911f4b9dbfc770f2a16a51`  
> Status: pilot corpus / human review preparation; **no Badness gates yet**

## Purpose

FN-BUNDLE-3 validates whether the exact diagnostics exposed by FN-BUNDLE-2 correspond to human judgments about first-night information quality for the initial `BEGINNER / PUBLIC_GOOD_INFO` profile.

This stage does **not** train a scalar score and does **not** invent rejection thresholds before review evidence exists.

## Pilot partitions

Partitioning is by complete setup + seating scenario, not by individual projected signature.

This prevents highly related signatures from one position from appearing in both calibration and holdout data.

Initial pilot:

### CALIBRATION — `cal-pair-rich-adjacent-evil`

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

This is the existing pair-rich FN-BUNDLE stress setup and retains the adjacent evil topology.

### HOLDOUT — `holdout-zero-outsider-librarian`

```text
1 Librarian
2 Chef
3 Empath
4 Monk
5 Investigator
6 Scarlet Woman
7 Imp
```

Anchor perspective: seat 1.

This deliberately introduces the legal zero-Outsider Librarian information path while keeping later staged Drunk / Spy / Recluse / Poisoner uncertainty out of the healthy experiment.

## Review-item selection

Every legal projected signature is still evaluated by the FN-BUNDLE-2 harness for the scenario anchor.

The pilot selects only a small deterministic review set, deduplicating overlaps among these reasons:

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

These are **sampling reasons**, not labels and not candidate-ranking rules.

The intent is to show the human reviewer both extremes and representative middle cases while explicitly including combination-collapse stress cases.

## Labels

Every generated item starts as:

```text
UNREVIEWED
```

Human review may later assign exactly one of:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

The label must be a judgment about the whole public first-night information ecology for a beginner table, not a mechanical translation of one metric.

## Evidence preserved per item

The review record retains:

- stable scenario and projected-signature identity;
- complete-bundle multiplicity represented by the signature;
- public observations;
- exact BEFORE / AFTER world counts;
- demon cover;
- distinct evil-team seat configurations;
- forced-good / forced-evil seats;
- evil cover;
- leave-one-out evidence for every public observation.

No posterior-probability language is used for unweighted exact-world counts.

## Cost boundary

The first exploratory implementation expanded every selected item to all good-player recipient perspectives across three scenarios. That was unnecessarily expensive for the first human-review loop.

The retained pilot therefore uses one explicit anchor perspective per scenario. Cross-recipient robustness remains a later validation question and should be added only if the first labels show that perspective dependence materially affects the Badness decision.

`FirstNightBundleBeginnerCorpusReviewTest` is a T3 review experiment and is excluded from `testFast`; it remains covered by full/T4 validation.

## Calibration / holdout discipline

Calibration data may be used to propose simple interpretable gates.

Holdout labels must not be used to choose those gates. After a candidate gate set is frozen, evaluate it against holdout and report at least:

```text
false accept: human-bad bundle passes the gates
false reject: human-acceptable bundle is rejected
uncertain cases: reported separately, not forced into either class
```

Do not silently move holdout cases into calibration because they are inconvenient.

## Next step

1. run the pilot corpus generator under full validation;
2. inspect and manually label the CALIBRATION review items;
3. inspect diagnostic separation without defining a scalar score;
4. expand calibration scenarios if the pilot is too narrow;
5. only after the calibration rule is frozen, inspect HOLDOUT performance;
6. derive BEGINNER gates only if the evidence is coherent enough to justify them.

Real Storyteller data such as ClockTracker remains an external calibration layer after the deterministic human-review baseline exists; historical choices are evidence, not unique ground-truth labels.
