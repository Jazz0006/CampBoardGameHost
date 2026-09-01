# MS-S7 / MS-S8 — PR #61 Closeout Checkpoint — 2026-09-01

## Status

This document is the final MS-SETUP acceptance checkpoint for PR #61.

- PR #61 must remain **DRAFT / OPEN / UNMERGED** until explicit user authorization.
- MS-S1 through MS-S6D remain accepted; the S6D full acceptance baseline is `a861c515a73834a4071c4a54bce953eba5c075a6`.
- MS-S7 is complete at the code level and has passed its FAST/R2 checkpoint.
- MS-S8 is complete as the deliberately minimal second-script/no-template architecture proof and has passed its FAST/R2 checkpoint.
- This commit intentionally contains `[full-ci]`; final PR-closeout acceptance requires the complete T4 gates for this exact head to pass.

## MS-S7 — Trouble Brewing controlled production cutover

The S7 audit found one concrete production authority divergence:

```text
TroubleBrewingProductionSetupPreparer
-> TroubleBrewingSetupPresetSelector
-> selected production composition
```

That meant the generic setup architecture existed, but TB production composition was still decided by the legacy TB-specific preset selector.

### Behavior RED

`5010420263c643c22d58d3cad6b38094596cf231`

Added a behavior-level test that finds a seed where the legacy selector and generic selector disagree, then requires production preparation to follow the generic authority. The RED failed in Android FAST as expected while R2 remained green.

### GREEN

`2ceb39729c54e97b8b6321483ea4decdc0acf98f`

TB curated preset data remains a valid data source, but production selection now follows:

```text
validated TB preset pool
-> SetupCandidate
-> TemplateRepository
-> ClocktowerSetupProvider
-> SetupDiversitySelector
-> selected candidate provenance
-> SetupShownIdentityPolicy / SetupShownIdentityCommitter
-> existing TB deal planner
-> CommittedClocktowerSetup boundary
```

The old `TroubleBrewingSetupPresetSelector` is no longer the production composition authority.

S7 intentionally did **not** expand legacy recommendation quality, shortlist/order parity, probability parity, `RecommendationStyle`, Productive Uncertainty, PlayerWorldSet production integration, or new role-specific recommendation heuristics.

Checkpoint evidence for `2ceb39729c54e97b8b6321483ea4decdc0acf98f`:

- CI #1334: SUCCESS
- R2 main-thread boundary #1251: SUCCESS
- Android FAST: SUCCESS

## MS-S8 — minimal No Greater Joy / no-template proof

S8 is deliberately an architecture acceptance proof, **not** an NGJ production-start cutover and not a new NGJ recommendation engine.

The existing NGJ App start path may remain legacy/random outside this minimal acceptance slice. S8 proves that the generic architecture itself supports a second script without TB-specific setup templates or recommendation assumptions.

### Acceptance proof

`NoGreaterJoyGenericArchitectureAcceptanceTest` uses the real built-in No Greater Joy ruleset and proves:

```text
No Greater Joy ruleset
-> GeneratedSetupCandidateSource
-> ClocktowerSetupProvider
-> generated SetupCandidate
-> SetupShownIdentityPolicyResolver
-> SetupShownIdentityCommitter
-> CommittedClocktowerSetup
-> typed NGJ GameState
-> legal Investigator information semantics
```

It also proves the generated second-script candidate pool can be consumed by generic `SetupDiversityHistory` / `SetupDiversitySelector` without TB templates.

No NGJ-specific legacy recommender was added. Unsupported recommendation remains allowed to fall back to the generic manual/semantic selection path under the current maintenance-only legacy recommendation policy.

Acceptance commits:

- `9001cecc03085362a7a9ddd157e771c43f2051c2` — initial tests-only NGJ proof
- `10268831399d96b52aceccd2ba8e454efc236002` — tests-only enum completeness correction; no production change

Checkpoint evidence for `10268831399d96b52aceccd2ba8e454efc236002`:

- CI #1336: SUCCESS
- R2 main-thread boundary #1253: SUCCESS
- Android FAST: SUCCESS

## Scope audit since accepted S6D baseline

From `a861c515a73834a4071c4a54bce953eba5c075a6` through the S8 code/test head, executable changes after the accepted S6D baseline are intentionally limited to:

1. `TroubleBrewingProductionSetupPreparer.kt` — S7 production authority cutover;
2. `TroubleBrewingGenericProductionAuthorityTest.kt` — S7 behavior contract;
3. `NoGreaterJoyGenericArchitectureAcceptanceTest.kt` — S8 second-script/no-template proof.

The intervening roadmap/handoff changes are documentation. No Productive Uncertainty, cognitive-consistency, A3/A4/ZDD expansion, broad NGJ production migration, or legacy recommendation quality work belongs to this closeout slice.

## T4 acceptance requirement for this exact checkpoint

Per `docs/TESTING_STRATEGY.md`, this `[full-ci]` checkpoint must run the full applicable repository acceptance gate. Acceptance requires this exact head to pass:

- Android `:app:testFull`;
- Android `:app:assembleDebug`;
- ASP contract validation;
- Real Clingo cross-validation;
- stable CI gate;
- R2 main-thread boundary.

Do not substitute FAST for this checkpoint, and do not push another micro-commit before the required T4 run concludes.

## Stop condition

After all T4 checks are green, stop PR #61 work and report the exact final head/diff/check evidence. Do not mark the PR Ready and do not merge it without explicit user authorization.

After a user-authorized merge, stop extending MS-SETUP and resume the primary cognitive-consistency / Productive Uncertainty campaign on a fresh branch:

```text
PlayerKnowledgeSnapshot
-> PlayerWorldSet
-> candidate hypothetical observation
-> beforeWorlds / afterWorlds
-> epistemic metrics
-> misinformation quality
-> Productive Uncertainty
-> cognitive-consistency recommendation
```

A3 exact enumeration remains the correctness baseline; A4/ZDD remain shadow/prototype until independently validated.
