# SDE-2D5F — Human Review, Interpretable Gate Freeze & Sealed Holdout Audit

> Date: 2026-09-19 Australia/Sydney  
> Repository: Jazz0006/CampBoardGameHost  
> Branch: sde-2d5-calibration-policy-evidence  
> Base D5 evidence: D5A–D5E complete  
> Status: **AUDIT COMPLETE — D5F-A is the first executable slice**

## 1. Purpose

SDE-2D5F converts the completed D5A–D5E descriptive calibration evidence into a reviewable BEGINNER-policy calibration process.

It does not cut production recommendation selection over to the new policy. Production cutover remains a later explicit phase.

Required lifecycle:

~~~text
CALIBRATION EVIDENCE
    ↓
deterministic human-review export
    ↓
human overall labels + named reasons
    ↓
interpretable candidate gates
    ↓
explicit frozen gate snapshot
    ↓
SEALED HOLDOUT — evaluate once
    ↓
false accept / false reject / uncertain report
    ↓
D5 T4 acceptance
~~~

No holdout data may influence gate construction.

## 2. Existing owners to preserve

### 2.1 Overall human label vocabulary

Reuse the existing FN-BUNDLE-3 label vocabulary:

~~~text
UNREVIEWED
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
~~~

These labels describe the overall beginner-table information ecology, not an individual metric.

Do not invent an automatic mapping from one metric to one label before human labels exist.

### 2.2 D5 evidence owners

D5F consumes but does not recompute:

- NormalizedStrategicDiagnosticsProjector;
- D5B cross-regime topology evidence;
- D5C HealthyCore / FullBundle / DrunkMarginal evidence;
- D5D Demon-bluff role-support and shared/union evidence;
- D5E mechanical-information vs strategic-topology contrasts.

### 2.3 Holdout owner

The existing FN-BUNDLE-3 calibration/holdout boundary remains authoritative.

D5F audit and pre-freeze implementation must not:

- inspect the sealed holdout setup;
- evaluate it;
- export its diagnostics;
- expose its scenario ID;
- use it to decide candidate gates.

Only the already-public sealed holdout count may appear before gate freeze.

## 3. Review model decision

A single scalar review score is rejected.

D5F should use two layers.

### Overall label

One of the existing whole-ecology labels.

### Named review reasons

Typed reasons explain why a reviewer assigned that label.

Initial reason families should cover:

~~~text
STRATEGIC_COLLAPSE
INSUFFICIENT_HEALTHY_INFORMATION
MECHANICALLY_USEFUL_TOPOLOGY_NEUTRAL
DRUNK_MARGINAL_PATHOLOGY
DRUNK_MARGINAL_ACCEPTABLE
BLUFF_SUPPORT_FRAGILE
BLUFF_SUPPORT_ROBUST
CROSS_REGIME_REFERENCE
OTHER_EXPLICIT_REVIEW_REASON
~~~

The exact stable names may be refined in D5F-A, but the separation is frozen:

~~~text
overall label != metric threshold
overall label != evidence kind
review reason != automatic gate
~~~

Baseline reference points are context and do not require an overall good/bad label.

## 4. One review surface, heterogeneous evidence

D5A–D5E evidence is intentionally heterogeneous.

D5F review export should therefore use a tagged review record rather than flattening everything into one numeric table.

Every record needs:

- stable review ID;
- evidence kind;
- player-count regime;
- setup profile;
- contrast/group ID where applicable;
- exact normalized ratios as numerator/denominator;
- optional bounded raw mechanical evidence;
- evidence-specific details;
- current overall label;
- review reasons / reviewer notes.

Evidence-specific details remain typed.

### Generic strategic point

- Demon-cover retention;
- evil-topology retention;
- evil-cover retention;
- forced-good fraction.

### Drunk

- HealthyCore;
- FullBundle;
- DrunkMarginal;
- semantic truth;
- bounded raw-world delta.

### Demon bluff

- supported-role count;
- per-role normalized support;
- strategic union/shared support;
- shared-to-union retention;
- distinct role strategic-pattern count.

### Role-information contrast

- mechanical-world reduction;
- topology-neutral flag;
- paired near-raw/different-topology evidence where selected.

Do not introduce placeholder zeros for inapplicable fields.

## 5. Deterministic review selection

The export must be reproducible.

Reuse the existing selection style:

- low/high boundary cases;
- structurally important contrasts;
- deterministic stable-ID tie breaking;
- no random sampling presented as exact evidence.

D5F should preserve at least:

- all four player-count regimes;
- STANDARD and BARON reference coverage;
- Drunk false-clue evidence;
- Demon-bluff low/high shared-support contrasts;
- topology-neutral mechanically informative evidence;
- near-raw but different strategic-topology evidence;
- at least one strategic-collapse case;
- at least one weak-information case.

The export may include reference context that is not itself labelable.

## 6. Human labels must be separate from generated evidence

Generated evidence must remain reproducible from code.

Human judgments should therefore live in a separate stable label manifest keyed by review ID.

Required properties:

- generated evidence does not contain hand-edited labels;
- label manifest cannot reference unknown review IDs;
- duplicate labels are rejected;
- a gate freeze cannot proceed while required calibration items remain UNREVIEWED;
- UNCERTAIN is explicit and remains separate from ACCEPTABLE/bad cases;
- review reasons are stored with the label.

This prevents regeneration from erasing review work and prevents evidence generation from silently changing human labels.

## 7. Gate derivation decision

D5F may derive candidate gate hypotheses only after calibration labels exist.

Allowed shape:

~~~text
NamedGate(
    gateId,
    axis,
    direction,
    threshold,
    applicability,
    calibrationProvenance
)
~~~

Examples of eligible named axes:

- minimum Demon-cover retention;
- minimum evil-topology retention;
- maximum forced-good fraction;
- minimum healthy-core information floor;
- Drunk-marginal bounds;
- minimum supported bluff-role count;
- minimum bluff shared-to-union support;
- maximum / minimum diversity pattern bounds when supported by review.

These are examples of axis names, not frozen thresholds.

Prohibited shape: one weighted aggregate score combining all diagnostics.

No opaque weighted global score.

## 8. Gate provenance and freeze

A candidate gate must record which labeled calibration review items justify the proposed boundary.

Freeze is an explicit artifact/version transition:

~~~text
PROPOSED
    ↓ explicit review/freeze
FROZEN
~~~

A frozen snapshot must be immutable for the subsequent holdout run and include:

- gate IDs;
- exact thresholds;
- applicability;
- calibration review IDs used;
- calibration-label manifest version/hash.

Changing a threshold after holdout evaluation invalidates that holdout result and requires a new future holdout; it must not silently reuse the exposed holdout.

## 9. Holdout discipline

Before freeze:

~~~text
allowed:
    sealed holdout scenario count

forbidden:
    scenario ID
    seating
    setup
    selected signatures
    diagnostics
    labels
    outcomes
~~~

After freeze, evaluate the sealed holdout exactly once.

Report:

~~~text
false accept
    human-bad item passes frozen gates

false reject
    human-acceptable item rejected

uncertain
    human-uncertain case, reported separately
~~~

Do not retune the frozen gates from holdout failures.

## 10. D5F slices

### D5F-A — deterministic calibration review export

Implement a review-only model / renderer that consumes completed D5 evidence and emits calibration material only.

Requirements:

- no holdout evaluation;
- sealed holdout count only;
- stable IDs;
- exact rational diagnostics preserved;
- all generated reviewable items begin UNREVIEWED;
- baseline reference rows are explicitly non-labelable context.

### D5F-B — human label manifest

Add a separate label manifest keyed by stable review ID.

Validate:

- no unknown IDs;
- no duplicate IDs;
- reasons required for non-UNREVIEWED decisions;
- required review set fully settled before gate derivation;
- UNCERTAIN stays explicit.

D5F-B does not derive thresholds yet.

### D5F-C — interpretable candidate gate derivation

From labeled calibration only:

- derive named candidate gate ranges/boundaries;
- retain calibration provenance;
- no global score;
- no holdout access.

Gate candidates remain PROPOSED.

### D5F-D — explicit freeze

Create a frozen gate snapshot/version only after review.

Freeze validation must prove:

- all required calibration labels settled;
- no holdout evidence present;
- every threshold has calibration provenance;
- frozen artifact is deterministic.

### D5F-E — sealed holdout once

Only after D5F-D:

- evaluate holdout once;
- apply frozen gates unchanged;
- report false accept / false reject / uncertain;
- do not retune.

### D5F-F — D5 acceptance

Run logical T4:

- R2;
- Android testFull;
- debug APK;
- ASP;
- Real Clingo;
- CI gate;
- dedicated D5 calibration / review evidence where applicable.

Then advance roadmap/handoff explicitly.

## 11. Testing governance

- D5F-A pure review-model / renderer contracts may be FAST if cheap.
- Evidence generation remains T3 through :app:sde2D5Calibration.
- Human label manifest parsing/validation should be cheap T1.
- Holdout evaluation must have a dedicated explicit post-freeze task and must not run in ordinary FAST/FULL before freeze.
- Final D5 acceptance must use a full T4 checkpoint.

## 12. Non-goals

D5F does not authorize:

- production ranking cutover;
- changing setup legality;
- changing epistemic semantics;
- changing Drunk shown-role ownership;
- changing Demon-bluff persistence;
- opening holdout early;
- automatic human-label inference;
- a weighted global scalar;
- SDE-3 work.

## 13. Immediate next step

D5F-A only:

~~~text
completed D5 calibration evidence
        ↓
deterministic tagged review records
        ↓
markdown review export
        ↓
all reviewable records UNREVIEWED
+ baseline reference context
+ sealed holdout count only
~~~

Do not implement candidate thresholds, freeze artifacts, or holdout evaluation in D5F-A.
