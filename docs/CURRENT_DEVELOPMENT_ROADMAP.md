# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-20 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Program status

~~~text
D6 decomposition / ownership cleanup                  COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 / 1 / 2                                  COMPLETE / PR #139/#140/#142
SDE-0 BEGINNER strategic corpus                       COMPLETE / PR #143
SDE-1 orchestration / lifecycle / shadow              COMPLETE / PR #144
SDE-2D1 Drunk whole-bundle                            COMPLETE / PR #145
SDE-2D2 Demon bluff joint-output                      COMPLETE / PR #146
SDE-2D3 strategic-world quotient                      COMPLETE / PR #147
SDE-2D4 5–15 correctness/performance                  COMPLETE / PR #149
SDE-2D5 calibration / policy evidence                 CURRENT / PR #150 draft
~~~

Completed slice-level evidence is archived rather than loaded by default:

- [`archive/checkpoints/fn-bundle/`](archive/checkpoints/fn-bundle/README.md)
- [`archive/checkpoints/sde/`](archive/checkpoints/sde/README.md)

Long-term route authority entering SDE-3 remains:

[`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)

## 2. Current branch / PR

Branch:

`sde-2d5-calibration-policy-evidence`

PR:

**#150 — SDE-2D5: calibrate strategic policy evidence**

PR #150 remains **draft**. Do not merge unless the user explicitly says **“授权合并”**.

Latest accepted B3 functional head:

`74d8bb4cc07c5425bcb61fa821bc2287620f379f`

B3 documentation/cleanup followed on the same branch.

Documentation/archive consolidation baseline:

`77fe159d06f3e4c57144f8621cf6d50e46742b21`

This consolidation moved completed checkpoint evidence out of active `docs/` and rewrote the active README/roadmap/handoff; it did not change runtime behavior.

Always query live refs before executable edits.

## 3. SDE-2D5 status

~~~text
D5A–D5E                         COMPLETE
D5F-A                           COMPLETE
D5F-B manifest infrastructure  COMPLETE
D5F-B3 policy-model correction COMPLETE
D5F-B representative calibration MATERIALIZED / DIAGNOSTICS NEXT
D5F-C gate/band derivation     BLOCKED
sealed holdout                 CLOSED
SDE-3                           BLOCKED
~~~

Current policy-model authority:

[`SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`](SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md)

Current external-human evidence catalog:

[`SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`](SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv)

Representative healthy-information corpus authority:

[`SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md`](SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md)

## 4. D5F-B3 accepted model

### 4.1 Lifecycle rule

> Optimize only variables owned and still controllable at the current lifecycle stage.

Every review record now states:

- lifecycle stage;
- decision owner;
- controllable variables;
- diagnostic-only variables;
- persistence boundary.

### 4.2 Drunk review

One same-setup review surface carries:

- truthful candidate;
- mild false candidate;
- stronger false candidate when a strictly stronger legal false exists;
- HealthyCore;
- FullBundle;
- DrunkMarginal;
- normalized strategic diagnostics;
- `counterfactualHealthyTruthDanger`.

Semantic truth is descriptive only.

Drunk shown identity remains persistent setup input. SDE must not turn a healthy player into the Drunk after setup commit.

### 4.3 Healthy information ownership

Current real review provenance distinguishes:

- Chef / Empath → `RULE_DETERMINED`, diagnostic-only;
- Washerwoman / Investigator → `STORYTELLER_CONTROLLED`, SDE-controllable until shown.

General rule remains:

- Washerwoman / Librarian / Investigator may have legal Storyteller output choices;
- Fortune Teller target pair is player-controlled;
- Red Herring is setup-controlled before persistence;
- Poisoner target belongs to the Evil player.

Do not add automatic reroll merely because one healthy clue is strong.

### 4.4 Demon bluff review

Review evidence exposes separately:

- per-role support;
- individual-support floor;
- shared / union support;
- pairwise strategic coverage;
- distinct strategic patterns;
- categorical beginner execution burden;
- categorical claim burden / cadence;
- narrative route class / diversity;
- external-human observed triplet.

Shared/union is a coherence/fragility diagnostic, not a monotone quality objective.

Do not add numeric coverage-complementarity reward before human evidence supports it.

### 4.5 Bundle confirmation-chain review

Review evidence exposes:

- full-bundle normalized diagnostics;
- each leave-one-out diagnostic;
- worst evaluated good-recipient pressure;
- whether removal restores Demon cover;
- whether removal restores strategic topology;
- restoring-clue count;
- explicit multi-channel-collapse signal.

No opaque global confirmation score is authorized.

The beginner target is an information ecology band:

~~~text
too much joint healthy confirmation -> Evil collapses too early
middle                           -> Good can reason; Evil can survive
too little reliable information -> Good lacks meaningful traction
~~~

## 5. Human-review manifest

Canonical manifest:

`app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`

Version:

`d5f-b-calibration-v3`

Scope correction:

- the old v2 role-information and confirmation-chain records all came from one deliberately extreme 7-player D5E diagnostic fixture;
- that fixture is still useful for proving metric behavior and regression properties;
- it is **not representative enough to calibrate BEGINNER policy gates**;
- those records are now `DIAGNOSTIC_ONLY` and do not require human labels;
- historical v2 labels are preserved in `sde-2d5f-human-label-manifest-v2-obsolete-extreme-fixture.tsv`;
- canonical v3 retains only the three Demon-bluff judgments and the independent 6-player Drunk judgment.

The gate is intentionally still blocked even though all four v3 manifest entries are settled. Validation reports missing representative coverage for:

`HEALTHY_BUNDLE_INFORMATION`

This prevents accidental gate derivation before normal-template / external-human calibration evidence replaces the extreme fixture.

See:

[`SDE_2D5F_EXTREME_FIXTURE_CALIBRATION_SCOPE_CORRECTION_2026-09-20.md`](SDE_2D5F_EXTREME_FIXTURE_CALIBRATION_SCOPE_CORRECTION_2026-09-20.md)

## 6. B3 validation

Focused deterministic B3 calibration:

~~~text
run 35494283217  SUCCESS
~~~

It verified:

- generated v2 manifest equals persisted v2;
- 11 reviewable records;
- grouped Drunk contrast;
- multi-axis bluff records;
- confirmation-chain records;
- real role-information ownership split.

R2 on B3 final documentation/cleanup head before this archive pass:

~~~text
run 35494608592  SUCCESS
~~~

Ordinary CI runs around the final B3 heads were superseded/cancelled by subsequent same-branch pushes; the focused B3 T3 is the acceptance evidence for the corrected calibration surface. Query the live branch before the next executable change.

## 7. Compatibility code intentionally retained

Do not remove yet:

- impaired-information approximate 90/10 false-family compatibility bridge;
- legacy `MalfunctionPolicy`.

Tests that treated the approximate 90% false ratio as durable target policy were removed.

Generic shown-identity setup ownership remains:

~~~text
SetupShownIdentityPolicyResolver.resolveGenerated(...)
    -> setup-stage choice
    -> SetupShownIdentityCommitter
    -> PlayerState.shownRole
~~~

Do not restore old `SetupEvaluator` / recommendation-owned Drunk shown-role scoring.

## 8. NEXT — representative healthy-information diagnostics and review selection

Do **not** continue labeling the two remaining v2 Chef/Empath records.

Completed first representative materialization slice:

- audited all production 7–9 preset pools: 50 presets per player count;
- confirmed 36 / 38 / 36 presets respectively contain at least one Washerwoman/Librarian/Investigator control surface;
- selected seven `beginner` / `standard` production presets spanning 7–9 players and all three pair-information roles;
- committed deterministic seating through the production `TroubleBrewingSetupDealPlanner`;
- enumerated legal healthy clue outputs through `NaturalPairInformationCandidateGenerator`;
- fixed Poisoner targets only as explicit Evil-player-controlled context;
- retained Chef/Empath as fixed context; any registration-dependent healthy branch is exposed rather than optimized;
- added human-readable seating/clue rendering and registration provenance;
- kept the new materialization outside review membership and gate derivation.

Implementation checkpoint:

`cb1ffc454f87a7be8b166d8013746dee7f50bfda`

Validation:

- CI run `35514598766` — **SUCCESS**;
- R2 run `35514598790` — **SUCCESS**.

Next:

1. attach `TroubleBrewingTopologyHypotheticalBundleEvaluator` + `NormalizedStrategicDiagnosticsProjector` diagnostics to these exact scenario/candidate identities;
2. keep Demon-cover, Evil-topology, Evil-cover, forced-good and feasibility components separate — no opaque global score;
3. select a small human-review subset by semantic strata, not FIRST/MIDDLE/LAST ordering and not metric extrema;
4. include whole-table context so fixed Chef/Empath and cross-confirmation routes remain visible;
5. use ClockTracker / external-human evidence as ecology and guardrail anchors; do not invent direct healthy-pair labels where no equivalent observed choice exists;
6. only after the representative review set is stable, add it as `REVIEWABLE` so `HEALTHY_BUNDLE_INFORMATION` coverage can be satisfied;
7. only then resume D5F-C gate/band derivation.

The machine corpus may retain all legal alternatives; the human labeling workload should stay bounded by table-meaning strata.

## 9. Blocked until representative calibration coverage exists

Do not:

- enter D5F-C;
- derive or freeze numeric thresholds/bands;
- inspect sealed holdout diagnostics;
- tune against holdout;
- cut production recommendation policy;
- begin SDE-3;
- reopen obsolete v1 labeling.

## 10. SDE-3 resume gate

SDE-3 remains blocked until:

1. representative D5F-B calibration covers every SDE-owned policy variable, including HEALTHY_BUNDLE_INFORMATION;
2. D5F-C derives interpretable candidate gates/bands from representative labels;
3. gates are frozen before holdout inspection;
4. sealed holdout is evaluated once against frozen gates;
5. D5F final acceptance and roadmap/handoff explicitly advance the program.

## 11. Testing cadence

Follow root `AGENTS.md` and [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md).

For the next step, topology evaluation belongs in the explicit D5 calibration workload rather than FAST regression if it becomes expensive. Keep the semantic materializer covered by focused/FAST tests. Do not manufacture runtime tests for the later human label discussion itself.
