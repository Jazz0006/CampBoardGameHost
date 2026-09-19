# SDE-2D4 — 5–15 Player Correctness / Performance / Normalized Strategic Metrics Audit

> Date: 2026-09-18 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Base: PR #147 merged to `main` as `2cab06efeee5692024e065c632deefe765ca1618`  
> Branch: `sde-2d4-5-15-validation-performance`  
> Status: **AUDIT COMPLETE — executable D2D4 slices not yet started**

## 1. Goal

SDE-2D4 must establish that the strategic-world model introduced by D2D3 is semantically correct and computationally viable across the full supported Trouble Brewing range:

```text
5–6
7–9
10–12
13–15
```

The target is not merely “the code accepts player counts 5–15”.

The gate requires:

- exact strategic semantics across the range;
- player-count-normalized diagnostics;
- a measured CPU / memory / latency envelope;
- no hidden requirement to exhaustively materialize raw mechanical worlds;
- a clear representation decision based on evidence rather than a guessed cutoff.

## 2. Existing setup semantics already cover 5–15

`TroubleBrewingSetupProfiles` is explicit for every non-Traveller count from 5 through 15.

Standard profiles are:

```text
5   3/0/1/1
6   3/1/1/1
7   5/0/1/1
8   5/1/1/1
9   5/2/1/1
10  7/0/2/1
11  7/1/2/1
12  7/2/2/1
13  9/0/3/1
14  9/1/3/1
15  9/2/3/1
```

The Baron profile is derived as:

```text
townsfolk - 2
outsiders + 2
same Minion / Demon count
```

and the exact enumerator enforces:

```text
hasBaron == isBaronProfile
```

Therefore D2D4 does not need four different setup algorithms.

It needs one algorithm whose semantic evidence and performance envelope span all four player-count regimes.

## 3. Critical representation finding

D2D3 reduced the **result identity** from raw role-world multiplicity to:

```text
StrategicWorldKey(
    setup Demon seat,
    setup Minion seats
)
```

but the current exact path still discovers that quotient by scanning raw mechanical worlds.

For pristine first night, `ExactHistoricalHypotheticalObservationBundleEvaluator` currently performs:

```text
recipient
    -> exact world stream
    -> BEFORE full scan
    -> one additional full world-stream scan per compatible query group
    -> project surviving worlds to StrategicWorldKey
```

The grouping introduced in D2D2 avoids one scan per role probe, which is important, but it does not remove the underlying mechanical-world generation cost.

Therefore:

> **D2D3 solved strategic representation multiplicity, not large-player witness-discovery complexity.**

## 4. Current ZDD does not remove the generation bottleneck

`ZddPlayerWorldSet.enumerateDirectMeasured` calls:

```text
TroubleBrewingWorldEnumerator.stream(...)
    -> filter exact mechanical worlds
    -> WorldZdd.createStreaming(...)
```

`WorldZdd.createStreaming` still iterates every exact mechanical world and only compresses after / while those worlds are generated.

The existing A4 device harness explicitly rejects any fixture other than 5 players:

```text
"The A4 device diagnostic currently supports only the validated 5-player fixture;
larger exact enumeration can exhaust the device heap before ZDD compression."
```

Therefore D2D4 must not treat ZDD as an already-existing solution to 10–15 player scaling.

ZDD remains useful for:

- compact retained representation;
- native restrictions after construction;
- differential correctness experiments.

It does **not** currently solve topology witness discovery.

## 5. Existing A3 benchmark is a constrained lower-bound workload

`A3EnumerationBenchmarkTest` covers 8, 10, 12 and 15 players, but deliberately pins every role except four.

That benchmark is useful for regression and per-world implementation cost.

It is not representative of the pristine SDE/player-visible baseline, where private storyteller role assignments are not available to the player's possible-world model.

Passing the current 15-player A3 benchmark therefore does not imply that an unpinned 15-player exact SDE scan is feasible.

## 6. Static search-space audit

The full Trouble Brewing catalog used by exact fixtures contains:

```text
13 Townsfolk
4 Outsiders
4 Minions
1 Demon
```

For one Demon and `M` Minions, the strategic seat-topology upper bound is:

```text
N × C(N - 1, M)
```

Across supported player counts:

| Players | Minions | Strategic topology upper bound |
| ---: | ---: | ---: |
| 5 | 1 | 20 |
| 6 | 1 | 30 |
| 7 | 1 | 42 |
| 8 | 1 | 56 |
| 9 | 1 | 72 |
| 10 | 2 | 360 |
| 11 | 2 | 495 |
| 12 | 2 | 660 |
| 13 | 3 | 2,860 |
| 14 | 3 | 4,004 |
| 15 | 3 | 5,460 |

This is the correct order of magnitude for the primary strategic state.

### 6.1 Current mechanical-stream magnitude

A static combinatorial expansion of the **current generator semantics** was performed for a recipient shown as Chef with only player-count knowledge, using the complete TB role catalog and both legal standard / Baron setup profiles.

The calculation follows the generator's current dimensions:

- unique role assignment;
- standard-vs-Baron profile legality;
- recipient may actually be the shown Townsfolk or the Drunk where legal;
- Fortune Teller red-herring alternatives;
- Poisoner target alternatives;
- hidden non-recipient Drunk shown-role alternatives.

It is a source-derived enumeration-size calculation, **not a measured runtime benchmark**.

| Players | Strategic topology upper bound | Current mechanical variants emitted by source semantics |
| ---: | ---: | ---: |
| 5 | 20 | 16,728 |
| 6 | 30 | 1,614,960 |
| 7 | 42 | 11,048,400 |
| 8 | 56 | 881,274,240 |
| 9 | 72 | 21,546,645,120 |
| 10 | 360 | 238,973,898,240 |
| 11 | 495 | 5,844,298,521,600 |
| 12 | 660 | 120,286,402,790,400 |
| 13 | 2,860 | 1,448,416,055,116,800 |
| 14 | 4,004 | 22,381,606,504,857,600 |
| 15 | 5,460 | 186,298,351,906,867,200 |

The exact number varies with recipient shown role and already-known observations, but the conclusion does not depend on the precise fixture:

> **Exhaustive raw mechanical enumeration cannot be the production discovery algorithm for unconstrained large-player strategic evaluation.**

The 15-player strategic state is thousands of topologies; the raw mechanical generator can expose roughly `10^17` witness variants under a minimally constrained player-visible baseline.

## 7. Normalized strategic diagnostics

D2D4 should introduce baseline-relative diagnostics without changing policy yet.

Required quantities:

```text
demonCoverRetention
    = after possible Demon seats / before possible Demon seats

evilTopologyRetention
    = after StrategicWorldKey count / before StrategicWorldKey count

evilCoverRetention
    = after evil-cover seats / before evil-cover seats

forcedGoodFraction
    = after forced-good seats / player count
```

### 7.1 Representation contract

Do not store only rounded `Double` values as the durable diagnostic identity.

Prefer an exact small ratio:

```text
StrategicRatio(
    numerator,
    denominator
)
```

with a derived floating-point view only for telemetry / display.

Reasons:

- deterministic equality in tests;
- no calibration drift from rounding;
- denominator remains visible for small-vs-large-player interpretation;
- metrics remain composable without turning them into one opaque global score.

### 7.2 Ownership

Raw exact sets remain epistemic-owned:

```text
ExactWorldStructureDiagnostics
```

The normalized comparison of BEFORE to AFTER is recommendation/SDE interpretation.

Target flow:

```text
exact BEFORE / AFTER structures
    -> pure SDE normalized diagnostic projection
    -> future D2D5 calibration / policy
```

Do not move recommendation policy into the epistemic evaluator.

## 8. Required large-player architecture

D2D4 needs an exact **topology-first feasibility** path.

Conceptually:

```text
candidate StrategicWorldKey
    -> exact constraint / witness-existence check
    -> survives iff at least one legal mechanical witness exists
```

This path must:

- remain in or directly beside the epistemic exact authority;
- reuse Trouble Brewing role/setup/observation semantics rather than recreate a recommendation-owned rules engine;
- stop after the first valid witness for a topology when only feasibility is required;
- preserve registration, Drunk, Poisoner, Red Herring and shown-role semantics as witness constraints;
- never use random sampling as proof of SAT / UNSAT.

The existing exhaustive enumerator remains the bounded correctness oracle.

## 9. Differential correctness strategy

### 9.1 Bounded exact cross-check

For workloads where exhaustive enumeration remains tractable:

```text
topology-first exact result
==
distinct StrategicWorldKey projection of exhaustive mechanical worlds
```

Required cases include:

- standard setup;
- Baron setup;
- Drunk present/absent;
- Poisoner present/absent;
- Fortune Teller Red Herring;
- Spy/Recluse registration;
- strict shown-role constraints;
- contradictory / zero-world case.

### 9.2 Cross-regime structural evidence

For all 5–15 counts prove:

- every reported key has exactly one legal setup Demon seat;
- Minion seat count matches the legal profile;
- Demon is not in Minion seats;
- keys stay within the combinatorial topology bound;
- standard and Baron setup semantics remain legal;
- normalized ratios remain bounded and denominator-safe.

### 9.3 External oracle

Selected topology-feasibility cases should continue to use the existing real-Clingo oracle as independent exact evidence where the ASP coverage supports the queried semantics.

Clingo is validation evidence only; Android production remains offline/native.

## 10. Performance evidence

Measure separately:

1. exhaustive materialized enumeration;
2. exhaustive streaming enumeration;
3. D2D3 quotient accumulation after exhaustive generation;
4. topology-first feasibility;
5. retained representation / ZDD where useful;
6. memory and latency.

Do not compare only total wall-clock time.

For topology-first feasibility, record at minimum:

```text
playerCount
candidateTopologyCount
feasibleTopologyCount
topologiesChecked
witnessSearches
roleAssignmentsVisited
elapsedMs
coarseHeapDeltaBytes
```

This distinguishes “small final set” from “cheap proof of existence”.

## 11. Mobile performance gate

The existing A4 target values remain useful context:

```text
target operation                 15 ms
provisional maximum              50 ms
```

but D2D4 must not silently adopt them as the final SDE whole-bundle threshold.

A whole SDE evaluation may legitimately aggregate several exact feasibility operations.

Freeze production thresholds only after:

- JVM CI measurements;
- representative Android device measurements;
- especially the existing POCO X5/X8 device family gate where available.

## 12. D2D4 implementation slices

### D4A — normalized diagnostics

Tests first at the pure SDE seam:

- exact rational retention type;
- demon-cover retention;
- strategic-topology retention;
- evil-cover retention;
- forced-good fraction;
- zero-denominator behavior is explicit, never NaN/Infinity;
- no policy / ranking change.

### D4B — reproducible scale evidence

Add a T3-only scale/benchmark harness that records:

- setup regime;
- constrained exhaustive throughput;
- topology bound;
- mechanical-world generation lower/upper evidence;
- heap delta.

Do not add this workload to ordinary FAST.

### D4C — topology-first feasibility seam

Before implementation, perform a focused proposition/fanout audit and classify each exact proposition as:

```text
topology-resolvable
role-assignment-resolvable
mechanical-variant-resolvable
registration-sensitive
historical-only
```

Then introduce the smallest exact feasibility API beside the epistemic exact authority.

### D4D — differential matrix

Cross-check the new feasibility result against exhaustive enumeration on bounded fixtures and real Clingo where applicable.

Do not claim 10–15 correctness solely from performance tests.

### D4E — 5–15 performance matrix

Measure the new path across:

```text
5–6
7–9
10–12
13–15
```

Only after those measurements decide whether:

- direct topology feasibility is sufficient;
- a symbolic retained representation is still useful;
- additional memoization / compiled constraints are required.

## 13. Explicit non-goals

Do not during D2D4:

- cut production recommendation policy to new metrics;
- introduce one scalar “balance score”;
- reintroduce raw role-world count as the primary strategic target;
- use random sampling as exact evidence;
- create different semantic algorithms for each player-count regime without evidence;
- force ZDD into production merely because it compresses retained worlds;
- put Clingo into the offline Android runtime;
- start SDE-3.

## 14. Audit conclusion

The current architecture has a sharp and useful separation:

- D2D3 now owns the right strategic **identity**;
- the current enumerator still owns a trustworthy bounded mechanical **oracle**;
- the missing D2D4 piece is scalable exact **witness existence**.

The next executable work should therefore be:

```text
D4A normalized diagnostics
    ↓
D4B reproducible scale evidence
    ↓
D4C topology-first exact feasibility
    ↓
D4D differential correctness
    ↓
D4E 5–15 performance matrix
```

Do not attempt to make 15-player exhaustive mechanical enumeration faster enough by micro-optimizing the current raw-world loop. Its search-space order of magnitude is the wrong primary representation.


## 15. Implementation checkpoint — 2026-09-19

D4A and D4B are no longer the current frontier. The D2D4 branch now contains the topology-first implementation through the whole-bundle strategic seam:

```text
D4A   normalized strategic diagnostics
D4B   reproducible scale / bounded-prefix evidence
D4C1  5–15 StrategicWorldKey topology domain
D4C2  setup identity/type/shown-role witness existence
D4C3  Drunk / Poisoner finite-resource AbilityState feasibility
D4C4a identity + Spy/Recluse registration observation semantics
D4C4b Chef / Empath / Fortune Teller / Red Herring semantics
D4C4c composable logical observation branches
D4C5  exact strategic topology whole-bundle evaluator
D4D   bounded setup / observation differential tests
D4E   5–15 topology-bundle performance harness
```

The setup existence tail was optimized from enumerating Townsfolk/Outsider seat splits to an exact type-quota → role → seat max-flow proof. This changes the search implementation, not the semantic contract.

A subsequent FAST run exposed three failures:

- one stale test still expected numeric observations to be Deferred after D4C4b;
- one Red Herring conflict fixture failed to exclude the Recluse registration explanation;
- one real standard-profile Baron contradiction was incorrectly accepted.

The Baron bug was caused by an Elvis fallback that swallowed a legitimate null contradiction:

```kotlin
baron?.let { base.withForbiddenInPlay(it.id) } ?: base
```

The standard-profile branch now propagates the contradiction instead of restoring the old base branch.

Current executable checkpoint:

`f2ba4b8e29514ecc398626acf26526b1445d7eb6`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Android testFull / APK         SKIPPED at this FAST checkpoint
```

### 15.1 D4B / D4E benchmark governance

Performance instrumentation is not ordinary regression coverage:

- `Sde2D4ScaleBenchmarkTest` is an explicit T3 raw-enumerator scale evidence harness;
- it remains directly runnable as `:app:sde2D4ScaleBenchmark`, but is excluded from the default `testDebugUnitTest` / `testFull` regression task;
- a temporary CI diagnostic gave the class a 900-second cap and it timed out with status 124, proving that keeping the raw-prefix harness inside T4 makes the acceptance gate unbounded without adding a stable regression contract;
- `Sde2D4TopologyBundlePerformanceTest` remains excluded from FAST but stays in `testFull`; its topology-first workload is bounded and measured through 15 players;
- raw-prefix D4B instrumentation still uses a 1,000-world cap per representative player count.

The scale harness was not deleted and no production algorithm was weakened. The governance change separates explicit T3 measurement from the bounded T4 regression suite.

### 15.2 D4D whole-bundle differential evidence

The final D4D gap is now closed by:

`TroubleBrewingTopologyBundleDifferentialTest`

Unlike the earlier setup-only and single-observation differential tests, this oracle filters every observation in a candidate bundle against the **same** exhaustive `EnumeratedWorld`, including selected registration-witness matching, and compares:

```text
topology-first BEFORE StrategicWorldKey set
==
exhaustive baseline projection

topology-first AFTER StrategicWorldKey set
==
exhaustive same-world whole-bundle projection
```

Bounded coverage includes:

- natural role-identity conflict;
- one shared Poisoner target;
- one shared Drunk identity/resource;
- Spy special-registration witness;
- Recluse Demon-type registration witness;
- selected empty registration binding rejecting a special-registration-only explanation;
- Fortune Teller Red Herring shared identity;
- contradictory bundle;
- standard and Baron setup profiles.

First executable test commit:

`b8a798ea36d5dbffbeaf765690851ff50ddfa5d3`

A fixture-only named-argument compile correction followed as:

`ed96ee639fc989183ea73fda7137f581ba79b58f`

PR CI run `35406446111` validated the corrected oracle:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
```

The Android FAST build completed in `5m40s`. This is correctness evidence, but it is also direct evidence that the exhaustive whole-bundle differential should join the existing setup/observation differential classes outside ordinary T1 FAST and remain in affected/full validation.

### 15.3 D4E measured 5–15 topology-bundle matrix

The existing `Sde2D4TopologyBundlePerformanceTest` was executed unchanged on a temporary validation branch based on `ed96ee6...`.

It records a standard-profile, single-bundle topology-first pass with no guessed latency threshold:

| Players | Topology upper bound | Before keys | After keys | Elapsed ms | Coarse heap delta bytes |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 5 | 20 | 12 | 12 | 54 | 0 |
| 6 | 30 | 20 | 20 | 23 | 1,507,344 |
| 7 | 42 | 30 | 30 | 28 | 2,555,944 |
| 8 | 56 | 42 | 42 | 21 | 3,735,528 |
| 9 | 72 | 56 | 56 | 28 | 5,242,904 |
| 10 | 360 | 252 | 252 | 95 | 25,165,808 |
| 11 | 495 | 360 | 360 | 119 | 35,061,776 |
| 12 | 660 | 495 | 495 | 161 | 0 |
| 13 | 2,860 | 1,980 | 1,980 | 710 | 102,090,128 |
| 14 | 4,004 | 2,860 | 2,860 | 767 | 0 |
| 15 | 5,460 | 4,004 | 4,004 | 766 | 54,548,232 |

The heap number is deliberately labelled **coarse**. Zero deltas at 5, 12 and 14 demonstrate that GC timing makes before/after heap deltas unsuitable as peak-memory measurements. The non-zero values still provide useful evidence of allocation/heap pressure, especially in the 13–15 regime.

### 15.4 D4E representation decision

The measured result supports keeping direct topology feasibility as the D2D4 exact strategic discovery path:

- the 15-player strategic domain remains thousands of keys rather than the source-derived raw mechanical space on the order of `10^17`;
- the measured single-bundle JVM path remains sub-second through 15 players;
- 13–15 player latency is broadly flat rather than showing renewed combinatorial explosion;
- no evidence in this matrix justifies adding semantic complexity through compiled constraints before production cutover.

Therefore D2D4 does **not** add memoization or compiled constraints now.

This is not a production/mobile latency threshold. Before a future production cutover, multi-candidate batch scaling and representative Android-device evidence remain valid places to decide whether caching, memoized witness proofs or compiled constraints are needed.

The raw mechanical enumerator remains the bounded correctness oracle only. Do not return to raw-world enumeration as production discovery.

### 15.5 Final T4 acceptance — COMPLETE

SDE-2D4 is complete on draft PR #149.

The first full checkpoint exposed a test-governance problem rather than a production semantic failure: the raw-enumerator `Sde2D4ScaleBenchmarkTest` did not finish inside a dedicated 900-second diagnostic window. Because that class is T3 measurement evidence with no regression latency threshold, it was moved to the explicit `:app:sde2D4ScaleBenchmark` task and removed from the bounded Android regression full suite.

Final executable acceptance head:

`9d619370a68b97107957a2dde504778f436dae12`

Final validation:

```text
R2 main-thread boundary        SUCCESS   run 35410931214
Android testFull               SUCCESS
Debug APK assemble             SUCCESS
ASP contract tests             SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS   run 35410931127
```

The Android full-test + debug-APK step completed with `BUILD SUCCESSFUL in 7m 56s`.

D4D whole-bundle exhaustive parity, D4E 5–15 topology performance, normalized strategic metrics, setup/registration/shared-resource correctness and final T4 are therefore all accepted.

Frozen D2D4 result:

- topology-first strategic feasibility is the exact large-player discovery representation;
- raw mechanical worlds remain bounded oracle witnesses, not production discovery;
- no memoization or compiled-constraint layer is justified by the current 5–15 single-bundle measurements;
- no production recommendation consumer has cut over;
- `WorldCardinality.Exact` still means exact mechanical-world cardinality;
- no player-count approximation threshold was frozen.

PR #149 remains draft and unmerged until explicit user authorization.

The next development frontier is **SDE-2D5 calibration / policy evidence**. Do not begin SDE-3 yet.

