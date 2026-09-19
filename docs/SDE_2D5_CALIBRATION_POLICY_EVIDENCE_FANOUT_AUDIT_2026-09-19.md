# SDE-2D5 — Calibration / Policy Evidence Fanout & Ownership Audit

> Date: 2026-09-19 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Base: SDE-2D4 squash-merged to `main` as `ce591be6f097db5a67a1d8028e8b98de38bdaf6f`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> Status: **AUDIT COMPLETE — D5A is the first executable slice**

## 1. Goal

SDE-2D5 calibrates interpretable BEGINNER policy evidence over the durable D2D1–D2D4 diagnostics.

It must not create a second solver or jump directly to production ranking.

Target decision evidence:

```text
hard legality
→ catastrophic strategic-collapse evidence
→ healthy-core information sufficiency
→ Drunk marginal/pathology evidence
→ evil narrative support
→ Demon-bluff robustness/diversity
→ bounded deterministic tie evidence
```

Thresholds are outputs of calibration and human review, not route assumptions.

## 2. Existing owners to preserve

### Epistemic exact / topology feasibility

- `ExactHistoricalHypotheticalObservationBundleEvaluator` remains the bounded mechanical-world oracle.
- `TroubleBrewingTopologyHypotheticalBundleEvaluator` owns scalable pristine-first-night strategic topology feasibility.
- `TroubleBrewingRegistrationDomain` remains Spy/Recluse registration legality authority.
- `WorldCardinality.Exact` continues to mean mechanical-world cardinality.

### SDE diagnostics

- `NormalizedStrategicDiagnosticsProjector` owns baseline-relative strategic ratios.
- `DemonBluffJointOutputEvaluator` owns descriptive uncommitted bluff support overlays only.
- Drunk whole-bundle evaluators own HealthyCore / FullBundle / DrunkMarginal descriptive evidence.

### Calibration / review

The existing FN-BUNDLE-3 review harness already owns:

- calibration-vs-sealed-holdout separation;
- human labels (`UNREVIEWED / BAD_TOO_STRONG / ACCEPTABLE / BAD_TOO_WEAK / UNCERTAIN`);
- deterministic evidence selection;
- markdown review output;
- explicit experiment execution outside ordinary regression.

D2D5 should extend this review/evidence family rather than invent a separate policy engine.

## 3. Current calibration limitations

The existing calibration infrastructure predates D2D4 and is still mostly seven-player/raw-exact shaped:

- the main rich corpus has one seven-player scenario;
- the Fortune Teller pilot is one real seven-player preset;
- the low-information probe is synthetic and bounded;
- review selection still emphasizes raw AFTER world count, absolute Demon cover and old evil-team configuration count;
- existing `FirstNightBeginnerDiagnostics` does not carry D2D4 normalized ratios;
- the existing builders call raw exact evaluation directly;
- there is no cross-regime 5–6 / 7–9 / 10–12 / 13–15 calibration matrix;
- Drunk, Demon bluff and topology-neutral role-information evidence are not unified into one review schema.

This is acceptable historical evidence but insufficient for D2D5.

## 4. Critical D5A seam gap

D2D4 created:

`ExactStrategicTopologyBundleDiagnostics`

for scalable topology-first evaluation, but:

`NormalizedStrategicDiagnosticsProjector`

currently accepts only:

`ExactHypotheticalObservationBundleDiagnostics`

from the raw exact evaluator.

Both contracts already expose the same semantic inputs:

```text
beforeStructure
afterStructure
playerCount
```

Therefore calibration cannot currently consume normalized D2D4 metrics from the scalable path without either:

1. returning to raw mechanical enumeration; or
2. duplicating normalized-metric logic in the review package.

Both are wrong.

### D5A decision

Normalize through one structure-pair projection seam and provide adapters for both raw-exact and topology-first diagnostics.

The SDE package remains the owner of normalized ratios. Review/calibration code consumes those ratios; it does not recompute them.

## 5. Evidence axes to keep distinct

Do not collapse these into one scalar.

### Strategic pressure

- Demon-cover retention
- strategic-topology retention
- evil-cover retention
- forced-good fraction

### Healthy information utility

- whole-bundle strategic effect;
- leave-one-out / marginal contribution where useful;
- role information may be useful even when evil topology is unchanged.

### Drunk misinformation

- HealthyCore;
- FullBundle;
- DrunkMarginal;
- semantic truthfulness is descriptive evidence, not a fixed score multiplier.

### Demon bluff support

- supported bluff roles;
- union/shared strategic topology support;
- distinct role strategic-pattern count;
- fragile versus robust support.

## 6. Required D5 corpus dimensions

Calibration evidence must include all four player-count regimes:

```text
5–6
7–9
10–12
13–15
```

and at least the following semantic contrasts:

1. Drunk clue versus HealthyCore;
2. bluff-supported versus bluff-fragile bundles;
3. similar/equal raw-world evidence with different strategic topology;
4. useful role-information with unchanged evil topology;
5. strategic-collapse cases;
6. representative standard and Baron-compatible setups where relevant.

No random sampling may be presented as exact evidence.

## 7. Calibration / holdout discipline

Preserve the existing two-phase rule:

```text
CALIBRATION
    inspect evidence
    assign/review labels
    derive candidate interpretable gates

FREEZE gates

HOLDOUT
    evaluate once after freeze
    do not tune on holdout
```

D5 may expand the sealed holdout set, but calibration generation must not reveal holdout diagnostics before gate freeze.

## 8. Implementation slices

### D5A — normalized topology diagnostic seam

- add topology-first projection support to `NormalizedStrategicDiagnosticsProjector`;
- preserve the raw-exact overload;
- one shared structure-pair implementation;
- no ranking or thresholds.

### D5B — cross-regime calibration evidence contract

Introduce a review-owned evidence shape that records:

- player-count regime;
- scenario/profile identity;
- normalized strategic diagnostics;
- optional raw mechanical evidence when bounded;
- explicit evidence kind/contrast;
- no scalar score.

Build deterministic representative fixtures across 5–15 using topology-first evaluation.

### D5C — Drunk / HealthyCore contrasts

Project existing D2D1 outputs into the calibration evidence schema.

Do not reselect Drunk shown role or duplicate clue legality.

### D5D — Demon bluff support contrasts

Project D2D2 bluff diagnostics into the same review schema.

Legality remains setup-owned; calibration consumes already-legal triplets.

### D5E — strategic-vs-role-information contrast corpus

Add explicit cases where:

- raw/role-world evidence looks similar but strategic topology differs;
- role information is useful while strategic topology is unchanged.

This protects the frozen architectural decision that strategic pressure and role-information utility are separate axes.

### D5F — human labels, interpretable gate candidates, sealed holdout

- expand deterministic review export;
- record labels/reasons;
- derive candidate gate ranges from reviewed calibration evidence;
- freeze before holdout;
- run holdout once;
- no opaque global score.

Production selection cutover remains out of scope.

## 9. Testing cadence

- D5A is ordinary focused/T1 code and should remain cheap.
- Cross-regime calibration generation belongs to explicit T3 evidence tasks when expensive.
- Keep large human-review reports outside FAST.
- Use bounded exact differential evidence only where it adds a distinct invariant.
- Final D5 acceptance requires a logical T4 checkpoint, but not every calibration report regeneration.

## 10. Non-goals

Do not during D2D5:

- cut production recommendation selection to the new gates;
- create one weighted/global score;
- duplicate rules legality;
- create a recommendation-owned world solver;
- return 10–15 player calibration to raw mechanical enumeration;
- tune on holdout;
- start SDE-3.

## 11. Immediate next step

Implement D5A test-first:

```text
ExactStrategicTopologyBundleDiagnostics
        ↓
NormalizedStrategicDiagnosticsProjector
        ↓
same ratios as structure-pair / raw-exact projection
```

Only after this seam is green should D5B cross-regime corpus work begin.
