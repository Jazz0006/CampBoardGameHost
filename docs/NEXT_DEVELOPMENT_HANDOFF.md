# NEXT DEVELOPMENT HANDOFF — FN-BUNDLE-3 BEGINNER Corpus

> Updated: 2026-09-16 Australia/Sydney  
> Status: **CURRENT / PR #143 public-claim semantic correction + calibration checkpoint**  
> Route: `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`

## 0. Start here

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. `docs/FN_BUNDLE_3_BEGINNER_CORPUS_PILOT_2026-09-16.md`;
7. query live `main`, PR #143 and current checks.

Do not use archived dated handoffs as current execution authority.

## 1. Merged foundation

FN-BUNDLE-0: merged #139. Do not repeat candidate census or pair ownership audit.

FN-BUNDLE-1: merged #140. Strict `ShownRoleAt` remains canonical exact semantics when a shown role is mechanically known.

FN-BUNDLE-2: merged #142 at:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

FN-BUNDLE-2 stable output:

```text
110,000 complete legal bundle combinations
        ↓
100 distinct PUBLIC_GOOD_INFO projected signatures
        ↓
exact structural diagnostics once per signature
        ↓
leave-one-out evidence
```

No bounded sampling and no final Badness threshold.

Healthy counterworld stage excludes:

```text
Drunk / Spy / Recluse / Poisoner
```

## 2. Current PR

PR:

`#143 FN-BUNDLE-3: build BEGINNER review corpus`

Branch:

`fn-bundle-3-beginner-corpus`

Goal: create trustworthy human-review calibration evidence before any BEGINNER Badness gate is defined.

Do not merge #143 until current-head T4 is green and calibration semantics have been inspected.

## 3. Pilot corpus design

Every item begins `UNREVIEWED`.

Available later labels:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Partitioning is by full setup + seating scenario to prevent near-duplicate signature leakage.

Pilot currently contains:

- one CALIBRATION scenario;
- one sealed replacement HOLDOUT scenario;
- one explicit good-player anchor perspective per scenario.

Every projected signature in a scenario is evaluated. A small deterministic human-review subset is selected by AFTER-count extremes, demon-cover minimum, evil-configuration minimum, forced-good maximum, largest leave-one-out recovery and quartile representatives.

These selection reasons are **not labels and not gate rules**.

The review test is T3/full and excluded from `testFast`.

## 4. Critical finding from the first corpus run

The first exported calibration report was not suitable for labeling.

Old PUBLIC_GOOD_INFO projection converted player statements into exact public facts:

```text
ShownRoleAt(speaker, claimedRole)
AND
claimedClue
```

That meant a player saying “I am X” mechanically proved they were X. Sampled signatures consequently collapsed around the true seating, with the same large forced-good block and effectively fixed evil topology.

This was a behavioral-modeling error, not a Badness-threshold problem.

**The old report is invalid calibration evidence. Do not label it and do not derive gates from it.**

## 5. Current public-claim semantics

For the healthy `BEGINNER / PUBLIC_GOOD_INFO` stage, a shared statement is now modeled as:

```text
speaker is evil
OR
(
    ShownRoleAt(speaker, claimedRole)
    AND
    claimedClue is mechanically true
)
```

Meaning:

- healthy good speakers follow the profile and report truthfully;
- evil speakers may bluff the same role/information statement;
- public role claims are defeasible speech, not Storyteller-confirmed identity;
- strict `ShownRoleAt` semantics themselves are unchanged.

This model is deliberately healthy-stage only. Do not generalize it yet to Drunk false information, Poisoner impairment, or Spy/Recluse registration.

When those stages are opened, public-claim sincerity / registration / malfunction semantics must be explicitly revisited rather than inherited accidentally.

## 6. Pristine Night-1 performance route

The new claim form no longer contains a top-level strict shown-role fact, so naïvely retaining the entire 7-player baseline would reintroduce the prior memory problem.

The exact evaluator therefore recognizes a **necessary-only public-claim identity envelope** for prefiltering:

```text
speaker can satisfy evil branch
OR
world shown role matches claimed role
```

For future registration safety, Recluse is conservatively retained by this prefilter. The complete observation is still evaluated by `TroubleBrewingWorldObservationEvaluator` afterwards.

Therefore:

- the prefilter cannot create truth semantics;
- it may only keep extra worlds, never remove a world that could satisfy the claim;
- historical replay is unchanged;
- no production A4/ZDD rollout decision changes.

## 7. Retained coverage

No RED-only exploratory fixture was added.

Retained contracts now verify:

- projected public speech is one defeasible claim per exposed entry;
- no bare public `ShownRoleAt` oracle fact is emitted for ordinary speech;
- an evil speaker can satisfy the same claim without having the claimed shown role;
- strict `ShownRoleAt` itself still matches shown identity exactly;
- healthy-stage public claims do not silently make Drunk false information valid;
- original private observations remain private / unmutated;
- holdout IDs / diagnostics do not appear in review export.

An ordinary FAST run after aligning the old BUNDLE-1 fixtures is expected to be the immediate pre-checkpoint gate.

## 8. Holdout governance

The first report export exposed the original holdout, so that scenario is retired as validation holdout.

A replacement holdout is present in the deterministic corpus builder.

Human-facing Markdown export intentionally includes:

- calibration details;
- count of sealed holdout scenarios;
- **no holdout scenario ID, seating, signature, diagnostics or label**.

Do not inspect replacement holdout diagnostics until candidate BEGINNER gates have been frozen.

## 9. Experimental testing rule

User instruction:

> 非必要的，都不要加 Red.

Operational rule:

```text
exploration / feasibility / measurement
    -> implementation-first allowed
    -> no manufactured RED

stable retained behavior / regression contract
    -> necessary test coverage before acceptance
    -> required FAST/T4 validation
```

## 10. Immediate execution sequence

1. confirm ordinary FAST after fixture alignment is green;
2. update roadmap / handoff / pilot protocol;
3. make the final documentation checkpoint commit with `[full-ci]`;
4. require Android full + APK, ASP, Real Clingo, R2 and aggregate gate green on the same head;
5. read the generated **CALIBRATION-only** report;
6. confirm public bluff counterworlds materially survive;
7. manually review/label calibration items only;
8. determine whether one calibration scenario provides enough spread;
9. add calibration scenarios if needed;
10. only after evidence is adequate, propose simple interpretable BEGINNER gates;
11. freeze gates before opening holdout.

## 11. What not to do next

Do not:

- use the invalid old calibration report;
- inspect the replacement holdout;
- invent a numeric world-count cutoff;
- optimize a scalar score;
- connect the production selector;
- start Drunk / Spy-Recluse / Poisoner uncertainty expansion;
- train on historical Storyteller choices as if unchosen alternatives were negative labels.

ClockTracker / real Storyteller records remain planned external ecological calibration after the deterministic human-review baseline is trustworthy.

## 12. Acceptance question for current checkpoint

The immediate question is semantic, not threshold-based:

> Under aggressive public sharing, does the exact model treat good truth-telling as informative while still preserving coherent worlds where evil players bluff their claimed roles/information?

If the answer is yes under current-head T4 and the new calibration report, proceed to manual calibration labels. If not, repair the behavioral model before touching Badness gates.
