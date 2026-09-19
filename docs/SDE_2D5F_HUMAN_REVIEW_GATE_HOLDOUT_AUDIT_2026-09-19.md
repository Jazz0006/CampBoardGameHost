# SDE-2D5F — Human Review, Interpretable Gate Freeze & Sealed Holdout Audit

> Date: 2026-09-19 Australia/Sydney  
> Repository: Jazz0006/CampBoardGameHost  
> Branch: sde-2d5-calibration-policy-evidence  
> Base D5 evidence: D5A–D5E complete  
> Status: **D5F-A COMPLETE — D5F-B infrastructure COMPLETE; HUMAN REVIEW is the next step**

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

### D5F-A — deterministic calibration review export — COMPLETE

Implement a review-only model / renderer that consumes completed D5 evidence and emits calibration material only.

Requirements:

- no holdout evaluation;
- sealed holdout count only;
- stable IDs;
- exact rational diagnostics preserved;
- all generated reviewable items begin UNREVIEWED;
- baseline reference rows are explicitly non-labelable context.

### D5F-B — human label manifest — INFRASTRUCTURE COMPLETE / HUMAN REVIEW PENDING

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

## 13. D5F-A implemented checkpoint

D5F-A now provides a review-only tagged export over the completed D5 evidence.

Implemented properties:

- stable deterministic review IDs and ordering;
- typed baseline / Drunk / Demon-bluff / role-information details;
- baseline rows are explicit non-labelable references;
- every reviewable generated record begins `UNREVIEWED`;
- exact strategic ratios remain numerator/denominator values;
- bounded raw mechanical evidence remains optional and evidence-specific;
- review reasons are a vocabulary only and are not inferred automatically;
- the real review corpus deterministically includes:
  - all four player-count regimes;
  - STANDARD and BARON baseline references;
  - a false Drunk numeric candidate;
  - Demon-bluff low/high shared-support contrasts;
  - topology-neutral mechanically useful role information;
  - near-raw/different-topology role-information contrasts;
  - the observed strongest strategic-collapse marginal;
  - the observed weakest positive mechanical-information marginal.

The real export is generated inside the explicit T3 task and writes:

`build/reports/sde-2d5f-calibration-review.md`

The pre-freeze holdout surface remains structurally sealed: the review builder accepts only the already-public sealed scenario **count**. It has no input for holdout scenario ID, seating, setup, diagnostics, labels or outcomes.

Acceptance evidence:

~~~text
D5F-A review-model T1 head  91d8256c9b483d2f70887f8a526aba175a70f073
Android FAST / R2 / CI gate  SUCCESS

D5F-A real review export T3  35423966647  SUCCESS
full CI run                  35423966654  SUCCESS
Android testFull + APK                      BUILD SUCCESSFUL in 7m 12s

clean validation head         c17db4fc4789a5d0b11156a22a88da7971a37ef7
clean full CI run             35424382474  SUCCESS
R2 / Android full / APK / ASP / Real Clingo / CI gate  SUCCESS
~~~

No human label was inferred, no candidate gate was derived, no threshold was frozen and no holdout diagnostic was inspected.

## 14. D5F-B infrastructure checkpoint

D5F-B now provides a separate human-label manifest with:

- the existing five-label vocabulary;
- stable review-ID keys;
- typed review reasons;
- deterministic line-oriented rendering/parsing;
- rejection of unknown IDs;
- rejection of baseline/reference IDs;
- rejection of duplicate IDs;
- reasons required for every non-`UNREVIEWED` decision;
- `UNREVIEWED` forbidden from carrying inferred reasons;
- explicit `UNCERTAIN`;
- review-set completeness through `isCompleteForGateDerivation`;
- an all-`UNREVIEWED` template builder;
- a source-controlled manifest seed at:
  `app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`.

A codec defect found by the template contract was fixed: parser trimming had removed the trailing TAB required to preserve an empty note field. Empty-note templates now round-trip exactly.

A second calibration defect was found while reviewing the generated evidence: structural ratio equality had treated `4/4` and `5/5` as different strategic-retention levels. D5E review selection now compares ratios by mathematical value using cross multiplication. The corrected real near-raw contrast includes equal raw removal (`6841`) with genuinely different strategic retention (`4/7` versus `3/6`).

Current source-controlled manifest contains exactly eight reviewable IDs, all `UNREVIEWED`:

~~~text
d5f:bluff:3ffbf7f823c8418f:r1
d5f:bluff:58e4a95e9ec27f82:r1
d5f:drunk:value-1
d5f:role-info:sig-00002562710d4654:marginal-1
d5f:role-info:sig-44f30b2b21cd7682:marginal-0
d5f:role-info:sig-796b6039ad4bb4aa:marginal-0
d5f:role-info:sig-89a6d878c5516337:marginal-1
d5f:role-info:sig-aab5a4dfa48cfce0:marginal-2
~~~

Validation evidence:

~~~text
manifest core GREEN                d1df3204b9514af15fe9281980a3ed5a50cba494
empty-note codec fix               624ac96813756d55b8bd8de1efd3a78a70984532
ratio-equivalence RED              d035c8683cb1489348463187ac46ccb8a5cf8f50
ratio-value fix                    900d185ff718054f0270c7e03fae433a4381e4ff
real-T3 assertion update           0b88a986a56e884e4b750ff9e7d4879a3f16c277
persisted manifest seed            9cfd0069038c095e6ee54eac04aa94bf1b638304
seed/live-material validator head  e88cf88801ceb7c8d28824eb793da98d5ae0b4cc

corrected D5 calibration T3        35430578290  SUCCESS
ordinary CI                       35430578286  SUCCESS
R2 / Android FAST / CI gate                     SUCCESS
~~~

The validator intentionally reports the current manifest as **valid but incomplete**:
`isCompleteForGateDerivation == false`.

No human label has yet been assigned, no gate has been derived and no holdout diagnostic has been inspected.

## 15. Immediate next step — HUMAN REVIEW

Review the eight source-controlled manifest records against the D5F-A calibration evidence and assign human judgments only:

~~~text
UNREVIEWED
    ↓ human review
ACCEPTABLE / BAD_TOO_STRONG / BAD_TOO_WEAK / UNCERTAIN
+ one or more explicit review reasons
~~~

Do not infer these labels automatically from metrics.

Only after all required review IDs are settled and the manifest validator reports `isCompleteForGateDerivation == true` may D5F-C begin.

Do not derive gates, freeze thresholds, inspect the sealed holdout, cut production policy or begin SDE-3 during the human-review step.
