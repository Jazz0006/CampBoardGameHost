# SDE-2D4 — 5–15 Player Correctness / Performance / Normalized Strategic Metrics Audit

> Date: 2026-09-18 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Base: PR #147 merged to `main` as `2cab06efeee5692024e065c632deefe765ca1618`  
> Branch: `sde-2d4-player-count-performance-audit`  
> Status: **INITIAL STATIC AUDIT COMPLETE / MEASURED PERFORMANCE EVIDENCE NEXT**

## 1. Goal

SDE-2D4 validates that the exact-consequence / strategic-quotient architecture generalizes from the bounded 7-player development fixtures to Trouble Brewing's full 5–15 non-Traveller range.

The audit keeps five questions separate:

1. **semantic correctness** — the same rules and strategic identity remain correct at every player count;
2. **candidate-space correctness** — all Storyteller-controlled factors remain legal and tractable to enumerate as candidates;
3. **normalized strategic diagnostics** — recommendation evidence is comparable across player counts without treating raw world multiplicity as strategic weight;
4. **performance** — CPU, memory and latency are measured on representative workloads rather than inferred from constrained benchmarks;
5. **representation choice** — only measured evidence may justify a symbolic topology-feasibility seam or another representation switch.

This stage does **not** cut production recommendation selection over to SDE and does not begin SDE-3.

## 2. Live-state correction

The roadmap and handoff checked at branch creation still described PR #147 as draft/unmerged. Live GitHub state is newer:

```text
PR #147       MERGED
main          2cab06efeee5692024e065c632deefe765ca1618
D2D3          COMPLETE ON MAIN
D2D4          CURRENT
```

The D2D4 branch was created directly from that live `main` commit.

## 3. Existing strategic representation remains semantically suitable

D2D3 introduced:

```text
StrategicWorldKey(
    demonSeat,
    minionSeats
)
```

projected from immutable setup `rolesBySeat` using `RoleDefinition.type`.

That identity generalizes naturally across player counts. The number of possible seat topologies is bounded by:

```text
N × C(N - 1, minionCount)
```

for Trouble Brewing's one-Demon setup.

| Players | Minions | Raw seat-topology upper bound |
|---:|---:|---:|
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

If one recipient is already known to be good, the corresponding topology ceiling is smaller:

```text
(N - 1) × C(N - 2, minionCount)
```

which is 4,004 rather than 5,460 at 15 players.

**Audit conclusion:** strategic topology itself is not the scaling problem. The risk is the cost of discovering which topology keys have at least one exact mechanical witness.

## 4. Current exact-world generation is the dominant scaling risk

`TroubleBrewingWorldEnumerator` recursively assigns distinct script roles seat-by-seat. When only player count is known, it evaluates both legal standard and Baron setup profiles. A Townsfolk recipient may be the perceived Townsfolk or the Drunk. Completed role assignments are then expanded by additional mechanical dimensions:

- Fortune Teller Red Herring seat;
- hidden Drunk shown-role alternatives;
- Poisoner target;
- later observation / registration witness semantics.

The following table is a **combinatorial role-assignment audit, not a runtime benchmark**. It assumes:

- full Trouble Brewing role catalog: 13 Townsfolk, 4 Outsiders, 4 Minions, 1 Demon;
- one recipient perceived as a specific Townsfolk;
- no other `RoleAt` pins;
- player count known but no explicit `SetupProfile`, so both standard and Baron profiles are admitted;
- Baron presence exactly matches the Baron setup profile;
- counts stop before Red Herring, hidden-Drunk shown-role and Poisoner-target expansion.

| Players | Role assignments before mechanical variants |
|---:|---:|
| 5 | 5,760 |
| 6 | 179,040 |
| 7 | 1,829,520 |
| 8 | 46,569,600 |
| 9 | 658,183,680 |
| 10 | 6,825,772,800 |
| 11 | 96,279,321,600 |
| 12 | 1,102,541,932,800 |
| 13 | 11,618,183,808,000 |
| 14 | 127,130,856,652,800 |
| 15 | 765,251,040,153,600 |

These are not `EnumeratedWorld` counts. Any assignment containing Fortune Teller, a hidden Drunk or Poisoner can fan out further.

**Audit conclusion:** exhaustive raw mechanical-world generation cannot be treated as a plausible full-range production strategy merely because the final strategic quotient contains at most 5,460 topology keys.

## 5. Existing performance evidence is not D2D4 evidence

### 5.1 A3 benchmark is strongly constrained

`A3EnumerationBenchmarkTest` covers 8/10/12/15 players, but it pins almost the entire setup with `RoleAt` and leaves only about four roles unpinned. It is valuable as a constrained exact-regression benchmark, but it does not model production-like incomplete player knowledge.

Therefore its current sub-10-second assertion must **not** be interpreted as evidence that unconstrained 8–15 player exact planning is mobile-feasible.

### 5.2 D2D2 shared scanning removes a multiplier, not the base explosion

The pristine exact evaluator currently:

1. creates one lazy exact world sequence per recipient;
2. scans the full sequence once for BEFORE;
3. groups compatible hypothetical queries;
4. regenerates and scans the sequence once per scan-compatible query group.

The D2D2 strict shown-role optimization correctly lets many bluff-role probes share one AFTER pass. That avoids one full enumeration per bluff role/triplet.

It does **not** reduce the cost of generating the underlying mechanical world family.

### 5.3 Current ZDD construction also starts from exact world generation

`ZddPlayerWorldSet.enumerateDirectMeasured()` consumes the same exact `TroubleBrewingWorldEnumerator.stream()` and inserts the resulting worlds into a ZDD.

This can reduce retained representation size after worlds are generated, but it does not avoid raw world-generation CPU cost. The current device benchmark intentionally hard-stops at 5 players because larger exact construction may exhaust device heap before compression.

**Audit conclusion:** neither streaming nor the current ZDD prototype is, by itself, the D2D4 large-player solution.

## 6. Candidate-space scaling is not the primary combinatorial blocker

Storyteller-controlled candidate producers remain much smaller than the raw mechanical world family.

### 6.1 Demon bluff triplets

Trouble Brewing has 17 good roles. For a legal setup, bluff roles are good roles not in play, and three are chosen.

| Players | Legal bluff triplets |
|---:|---:|
| 5–6 | 0 — rules do not give Demon bluffs |
| 7 | 220 |
| 8 | 165 |
| 9 | 120 |
| 10 | 120 |
| 11 | 84 |
| 12 | 56 |
| 13 | 56 |
| 14 | 35 |
| 15 | 20 |

D2D2 already evaluates distinct bluff roles once per recipient and composes triplets as a cheap overlay. Triplet count therefore does not multiply exact scans.

### 6.2 Pair information

Healthy Washerwoman/Librarian/Investigator candidates grow approximately with true-target count × available decoys, plus explicit Spy/Recluse registration alternatives where legal. This is polynomial in player count and remains rules-owned.

### 6.3 Other Night-1 factors

- Red Herring: at most one candidate per good seat.
- Chef/Empath numeric domains are small bounded value sets.
- Drunk Fortune Teller answer domain is two values; target choice remains player-controlled robustness input.

**Audit conclusion:** D2D4 should optimize exact feasibility/evaluation before considering candidate-generation approximation.

## 7. Normalized strategic metrics contract

Raw BEFORE/AFTER mechanical-world counts remain useful diagnostic evidence but are not the primary policy unit.

For one recipient and one candidate consequence, derive baseline-relative metrics from existing exact structure:

```text
demonCoverRetention
    = after.possibleDemonSeats.size
      / before.possibleDemonSeats.size

evilTopologyRetention
    = after.strategicWorldKeys.size
      / before.strategicWorldKeys.size

evilCoverRetention
    = after.evilCoverSeats.size
      / before.evilCoverSeats.size

forcedGoodFraction
    = after.forcedGoodSeats.size
      / playerCount
```

### Required representation rules

1. Preserve numerator and denominator as integers; a floating-point projection may be exposed for display/calibration but must not become the source of truth.
2. Metrics are **per recipient** because BEFORE knowledge differs by recipient.
3. `after == 0` is a hard UNSAT / legality failure before any quality metric is interpreted.
4. A zero BEFORE denominator is invalid/undefined evidence and must fail closed rather than divide by zero.
5. `forcedGoodFraction` is alignment certainty only. It is **not** a substitute for role-information utility.
6. Do not combine the metrics into one opaque global scalar.
7. At Night 1, `possibleDemonSeats` and setup `StrategicWorldKey.demonSeat` describe compatible dimensions. Cross-night SDE-3 must continue to distinguish current active Demon from immutable setup topology.

### Ownership

The exact epistemic evaluator remains owner of raw exact structure. Normalization is a pure SDE diagnostic projection over BEFORE/AFTER structure plus player count; it must not enumerate worlds, decide legality, or own recommendation policy.

## 8. Correctness matrix required for 5–15 players

Evidence must cover all player counts, grouped for reporting as:

```text
5–6
7–9
10–12
13–15
```

The first typed correctness matrix should prove without broad raw enumeration:

- standard and Baron profile counts are legal at every N;
- strategic keys encode exactly one setup Demon plus the correct number of setup Minion seats;
- canonical key identity is unchanged by good-role permutations / shown-role / malfunction witness dimensions;
- Demon/Minion seat-role swaps remain distinct strategic worlds;
- 5–6 player setups do not produce Demon bluff candidates;
- 7–15 player bluff candidate legality remains complete and unique;
- normalized metric numerators/denominators are invariant under raw role-permutation multiplicity;
- UNSAT and zero-denominator cases cannot masquerade as healthy retention metrics.

This matrix should be FAST if it uses bounded typed fixtures rather than exhaustive full-catalog enumeration.

## 9. Performance evidence plan

D2D4 performance evidence must distinguish the following costs rather than report one aggregate time:

### P1 — raw exact generation

Measure production-like incomplete knowledge on the largest player counts that can complete safely. Record:

- generated mechanical worlds;
- elapsed time;
- coarse heap delta / peak where available;
- whether a bounded execution budget is exceeded.

Do not let CI hang merely to prove that a trillion-scale search is too large.

### P2 — streaming exact consequence scan

Measure BEFORE plus representative AFTER scan groups separately. Confirm the multiplier saved by grouped query fanout and the unchanged base-generation cost.

### P3 — strategic quotient accumulation

Measure the marginal cost of inserting `StrategicWorldKey` into the existing accumulator. This should remain cheap relative to world generation.

### P4 — existing ZDD

Measure construction and filtering separately. Report world-generation time independently from prefix insertion/canonicalization so compression is not credited for work it does not eliminate.

### P5 — topology-feasibility prototype, only if P1–P4 justify it

If raw exact generation is outside the mobile envelope, prototype an epistemic symbolic/constraint query of the form:

```text
for strategic topology K:
    does at least one mechanically legal witness exist under facts + hypothesis?
```

The prototype must:

- live in the epistemic consequence layer, never recommendation;
- reuse canonical rules semantics rather than invent a second rules engine;
- prove exact parity with raw enumeration on bounded fixtures;
- preserve registration/malfunction witnesses needed by explanation diagnostics;
- stay exact unless explicitly labelled otherwise.

## 10. Mobile-feasible latency policy is deliberately not frozen yet

D2D4 must measure before choosing:

- a player-count cutoff;
- a raw-enumeration cutoff;
- a topology-feasibility switch threshold;
- any approximation or sampling policy.

The current route explicitly forbids choosing those thresholds from intuition alone.

## 11. Implementation sequence from this audit

```text
D4A  normalized strategic-metric typed contract
     + bounded 5–15 correctness/candidate-space matrix

D4B  measurement harness that separates raw generation,
     streaming scan, quotient overhead and ZDD construction

D4C  collect representative 5–15 regime evidence
     + document exact non-completion/budget exceedance where applicable

D4D  representation decision from measured evidence
     -> keep current exact path where viable
     -> add exact symbolic topology-feasibility seam only if required

D4E  device/mobile validation of the chosen path
     + final D2D4 acceptance audit
```

No new RED is required merely for the static audit document. D4A introduces durable metric/correctness contracts and should use typed RED/GREEN evidence. Performance workloads belong in affected/T3 validation, not ordinary FAST.

## 12. Current decision

The first audit rejects two tempting shortcuts:

1. **Do not infer 5–15 feasibility from the existing constrained A3 benchmark.**
2. **Do not assume the D2D3 strategic quotient or current ZDD automatically removes exact-world generation cost.**

The strategic state space itself is small enough to remain attractive: at 15 players the raw setup topology ceiling is 5,460. The mechanical witness space is the scaling problem.

Therefore the next executable slice is **D4A: normalized strategic metrics + bounded 5–15 correctness/candidate-space evidence**, followed by a measurement harness. A symbolic topology-feasibility implementation remains a possible D4D outcome, not a pre-decided architecture change.
