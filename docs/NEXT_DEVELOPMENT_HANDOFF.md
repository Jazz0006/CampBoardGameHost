# NEXT DEVELOPMENT HANDOFF — FN-BUNDLE-2 Healthy Whole-Bundle Harness

> Updated: 2026-09-16 Australia/Sydney  
> Status: **CURRENT / FN-BUNDLE-2 T4 acceptance checkpoint**  
> Current route decision: `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`  
> Prior `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`: **superseded as execution authority**

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. query live `main`, PR #142 and current checks before editing.

Do not use archived dated handoffs as execution authority.

## 1. Stable completed stages

FN-BUNDLE-0 is complete and merged in PR #139.

Do not repeat:

- first-night candidate-space census;
- setup pair-information ownership audit;
- pair-route cleanup.

Canonical pair-information ownership remains:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical consumers
```

FN-BUNDLE-1 is complete and merged in PR #140.

Live FN-BUNDLE-1 squash-merge baseline:

`efedf87a7d2434ea7ddb75bfdc8516a27d125666`

Stable FN-BUNDLE-1 semantics include:

- thin first-night information proposition materialization;
- strict `InformationProposition.ShownRoleAt(seat, role)`;
- deterministic deduplicated PUBLIC_GOOD_INFO shown-role claims;
- no Drunk hidden-identity leakage;
- healthy confirmation-chain exact fixture;
- Drunk shown-role exact fixture;
- full T4 acceptance before merge.

## 2. Current active PR — #142

PR:

`FN-BUNDLE-2: evaluate complete healthy first-night bundles`

Branch:

`fn-bundle-2-healthy-harness`

Executable implementation immediately before this documentation-only T4 checkpoint:

```text
head: 6c6da72dc5c782002a08a16c0cb3cdc2cc0df9f0
main: efedf87a7d2434ea7ddb75bfdc8516a27d125666
```

This handoff update intentionally uses `[full-ci]` in the commit message so the current PR head must pass the complete T4 route before merge authorization.

Do not merge #142 without explicit user authorization.

Do not start FN-BUNDLE-3 implementation before #142 is merged.

## 3. FN-BUNDLE-2 implementation now present

### Complete healthy bundle composition

The harness consumes the canonical candidate producers rather than recreating legality.

Representative 7-player healthy Trouble Brewing factors remain the audited FN-BUNDLE-0 space:

```text
raw complete bundle product:              110,000
represented PUBLIC_GOOD_INFO factor product: 100
latent product per public combination:      1,100
```

The harness preserves the full complete-bundle space as factorized provenance families rather than allocating all `110,000` bundle objects.

### Lossless quotient-first evaluation

Current flow:

```text
canonical legal factor producers
    ↓
factorized complete-bundle provenance
    ↓
PUBLIC_GOOD_INFO projection
    ↓
canonical projected observation signature
    ↓
lossless grouping by identical signature
    ↓
exact evaluation once per distinct signature
    ↓
leave-one-out diagnostics
```

The representative fixture currently produces exactly `100` distinct projected signatures and applies no bounded sampling.

The sum of provenance multiplicities across quotient groups remains exactly `110,000`.

### Exact descriptive diagnostics

`epistemic` owns consequence diagnostics. Recommendation code consumes them and does not implement a second rules engine.

The exact bundle evaluator now exposes:

- exact BEFORE / AFTER world counts;
- possible demon seats / demon cover size;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover / evil cover size;
- leave-one-out interaction evidence through the harness.

No final Badness threshold or scalar quality score exists in FN-BUNDLE-2.

### Pristine Night-1 scalability route

The initial implementation exposed a real CI-memory limit when the unconstrained 7-player possible-world family was materialized. The existing prefix-trie ZDD construction also exceeded memory at this scale.

FN-BUNDLE-2 therefore uses a dedicated exact streaming route only for a pristine first night:

1. lazily enumerate and scan the BEFORE family without retaining it;
2. group queries by strict public `ShownRoleAt` identity claims;
3. re-enumerate lazily for each small identity group;
4. filter strict shown-role claims before retaining worlds;
5. retain only the much smaller identity-constrained family;
6. evaluate remaining clue conjunctions and leave-one-out variants over that retained family.

Historical multi-night evaluation keeps the existing `EnumeratedHistoricalExactBaseline` replay path.

No production A4/ZDD rollout decision is changed by this experiment harness.

The earlier exploratory ZDD modifications used while investigating the OOM were removed from the PR before this checkpoint.

## 4. Healthy experiment domain

The current experiment intentionally isolates the first staged uncertainty domain.

Candidate legality still uses the complete official Trouble Brewing catalog and canonical producers.

Only the diagnostic counterworld role domain excludes later-stage uncertainty sources:

```text
Drunk
Spy
Recluse
Poisoner
```

This means FN-BUNDLE-2 is genuinely a healthy baseline rather than silently mixing in:

- Drunk shown-role / false-information uncertainty;
- Spy/Recluse registration ambiguity;
- Poisoner target impairment.

Those are later staged expansions.

A setup that actually contains one of those staged roles is not flattened into the healthy experiment.

## 5. Retained acceptance coverage

Exploratory RED-only fixtures added early in FN-BUNDLE-2 were deliberately removed.

The retained acceptance fixture is:

`FirstNightBundleHealthyHarnessAcceptanceTest`

It exists because the behavior is now a stable merge contract, not to manufacture a RED step.

It proves the representative healthy 7-player harness actually executes and verifies:

```text
raw complete bundle count == 110,000
represented public factor combinations == 100
distinct projected signatures == 100
exact-evaluated signatures == 100
sum of quotient multiplicities == 110,000
excluded counterworld roles == Drunk/Spy/Recluse/Poisoner
samplingApplied == false
all evaluated representative signatures retain non-zero exact worlds
```

Ordinary current-head FAST CI passed with this acceptance fixture before the final documentation/T4 checkpoint.

## 6. Experimental testing rule

Do not add RED tests merely because an experimental/spike change exists.

Use this rule going forward:

```text
exploration / feasibility / measurement
    -> implementation-first is allowed
    -> do not manufacture RED

stable behavior or architecture retained for merge
    -> add only necessary durable contract/regression coverage
    -> run affected validation + T4 acceptance
```

A RED test is useful when it proves a real uncovered stable behavior or reproduces a real defect. It is not a process goal by itself.

## 7. T4 acceptance checkpoint — current task

This current documentation commit intentionally uses `[full-ci]`.

The workflow must select:

```text
android=true
android_full=true
asp=true
oracle=true
```

Required current-head acceptance evidence:

- Android `:app:testFull` passes;
- `:app:assembleDebug` passes in the same full Android job;
- ASP golden corpus validation passes;
- ASP harness Python tests pass;
- Real Clingo frozen-oracle cross-validation passes;
- R2 main-thread boundary passes;
- aggregate CI gate passes.

Do not substitute the previous FAST-green run for this T4 checkpoint.

## 8. FN-BUNDLE-2 completion condition

Implementation requirements are satisfied:

```text
[x] complete healthy 7-player harness
[x] canonical producers reused; no second legality engine
[x] complete-bundle factorized provenance retained
[x] PUBLIC_GOOD_INFO materialized before quotient
[x] identical projected signatures evaluated once
[x] representative quotient 110,000 -> 100 proven
[x] exact structural diagnostics exposed by epistemic owner
[x] leave-one-out diagnostics produced
[x] no final Badness threshold / scalar ranking
[x] no bounded sampling needed for representative healthy slice
[x] pristine Night-1 exact path no longer materializes unconstrained 7-player baseline
[x] healthy diagnostic domain explicitly excludes Drunk/Spy/Recluse/Poisoner
[x] exploratory RED-only tests removed
[x] one retained stable acceptance fixture FAST-green
[x] superseded ZDD spike removed from PR diff
[ ] current #142 `[full-ci]` head has required T4 CI green
```

When the current-head T4 run is green, FN-BUNDLE-2 is complete and PR #142 is ready for explicit merge authorization.

## 9. Next stage after #142 merge

FN-BUNDLE-3 builds the BEGINNER Badness validation corpus.

Do not immediately invent numeric rejection thresholds.

First produce a human-reviewed corpus with labels such as:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Recommended validation structure:

- calibration set used to understand diagnostics and derive gates;
- holdout set not used to choose thresholds;
- adversarial fixtures where individually plausible clues collapse only in combination;
- explicit false-accept / false-reject review.

Real Storyteller records such as ClockTracker data should be audited and, where sufficiently detailed, used as external calibration/validation evidence. A historical Storyteller choice is not automatically a unique ground-truth label and should not make every unchosen legal bundle negative training data.

## 10. Later staged uncertainty

After the healthy corpus/gates are understood, widen the diagnostic model deliberately:

1. Drunk shown-role / false-information bundle effects;
2. Spy/Recluse registration ambiguity;
3. Poisoner target/dynamic impairment;
4. later skill-profile expansion;
5. optional soft-preference / ML / LLM research only after the deterministic baseline is validated.

## 11. Stable architecture boundary

```text
rules          -> legality / registration semantics
session        -> actual state / timeline / commit
epistemic      -> exact recipient-visible consequence semantics
recommendation -> complete-bundle composition / future Badness / survivor selection
UI             -> presentation / confirmation
```

Target product behavior remains:

```text
legal complete bundles
→ exact/capability-aware diagnostics
→ BEGINNER Badness rejection
→ uniform random among acceptable survivors
```

General-purpose LLM recommendation remains deferred.

## 12. Stable rule

> **FN-BUNDLE-0 and FN-BUNDLE-1 are finished and merged; do not repeat them. FN-BUNDLE-2 now has the complete healthy 7-player quotient/exact harness, scalable pristine-Night-1 evaluation, interpretable diagnostics and one retained acceptance contract. Treat the current `[full-ci]` run as the final T4 gate; if green, stop for explicit PR #142 merge authorization and do not start FN-BUNDLE-3 before merge.**
