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
