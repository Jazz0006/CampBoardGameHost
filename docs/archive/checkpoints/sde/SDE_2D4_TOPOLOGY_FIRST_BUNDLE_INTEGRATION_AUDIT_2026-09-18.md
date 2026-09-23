# SDE-2D4 Topology-first Bundle Integration Audit

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-2d4-5-15-validation-performance`  
> Parent authority:
> - `SDE_2D4_5_15_CORRECTNESS_PERFORMANCE_NORMALIZED_METRICS_AUDIT_2026-09-18.md`
> - `SDE_2D4_TOPOLOGY_FIRST_FEASIBILITY_FANOUT_AUDIT_2026-09-18.md`
> Status: **IMPLEMENTATION CHECKPOINT — branch composition and parallel strategic bundle diagnostics implemented; final whole-bundle differential / D4E / T4 acceptance pending**

## 1. Current implemented topology-first layers

The D2D4 branch now contains:

```text
D4A normalized strategic ratios
D4B source-derived scale / bounded prefix evidence
D4C1 exact StrategicWorldKey topology domain
D4C2 setup role/type/shown-role witness existence
D4C3 Drunk / Poisoner AbilityState witness existence
D4C4a identity observations + Spy/Recluse registration witness semantics
D4C4b Chef / Empath / Fortune Teller topology-first observation semantics
D4C4c composable observation constraint branches
D4C5 parallel exact strategic topology whole-bundle evaluator
D4D bounded setup / observation differential tests against exhaustive mechanical worlds
D4E 5–15 topology-bundle performance harness
```

The new path still has no production policy cutover.

Current executable checkpoint after fixing the standard-profile Baron contradiction:

`f2ba4b8e29514ecc398626acf26526b1445d7eb6`

Validation at that checkpoint:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
Real Clingo cross-validation   SUCCESS
CI gate                        SUCCESS
Android testFull / APK         not selected at this FAST checkpoint
```

The Baron defect was an implementation bug in the standard-profile branch: `withForbiddenInPlay(Baron)` correctly returned `null` for a contradiction, but an Elvis fallback restored the previous `base` branch. The fix preserves the contradiction rather than silently discarding it.

## 2. Existing exact bundle contract cannot be reused dishonestly

`ExactHypotheticalObservationBundleDiagnostics` currently contains:

```text
before: WorldCardinality.Exact
after: WorldCardinality.Exact
beforeStructure
afterStructure
```

The cardinalities are exact **mechanical-world counts**.

Topology-first feasibility can exactly produce:

- feasible / infeasible;
- surviving `StrategicWorldKey` values;
- setup evil topology structure.

It does not count every mechanical role/shown-role/Poisoner/Red-Herring witness.

Therefore D4C5 must not write:

```text
after = 1
```

for a feasible topology or otherwise pretend a strategic witness count is the old mechanical cardinality.

That would silently change the meaning of an existing exact field.

## 3. Current production cardinality consumer

Production SDE audit finds a remaining mechanical-cardinality use in:

`DemonBluffJointOutputEvaluator`

It tests:

```text
diagnostic.after.value.signum() > 0
```

for feasibility.

The same consumer already uses precise `afterStructure.strategicWorldKeys` for topology composition.

This means the feasibility decision can later migrate to an explicit exact strategic-feasibility field, but the old mechanical cardinality field cannot simply be redefined.

Normalized strategic diagnostics already consume only BEFORE / AFTER structure.

## 4. Required parallel strategic diagnostics

Introduce a separate exact strategic contract, conceptually:

```text
ExactStrategicTopologyBundleDiagnostics(
    bundleId,
    recipientSeat,
    beforeStructure,
    afterStructure
)
```

where structure is derived only from exact feasible `StrategicWorldKey` values.

Useful derived properties:

```text
beforeFeasible
afterFeasible
distinctBeforeStrategicWorldCount
distinctAfterStrategicWorldCount
```

Do not add a fake mechanical-world cardinality.

The existing exhaustive exact evaluator remains the bounded mechanical oracle during migration.

## 5. Independent observation SAT is not sufficient

A whole bundle means:

> one mechanically legal witness must satisfy every observation together.

It is unsound to do:

```text
observation A feasible on topology K
AND
observation B feasible on topology K
=> bundle feasible on K
```

because A and B may require incompatible latent assignments.

Examples:

- A requires the unique Minion to be Spy while B requires the same unique Minion to be Poisoner;
- two observations require different Poisoner targets in the same night;
- one branch requires Fortune Teller absent while another requires it in play;
- selected registration witnesses impose incompatible role identities;
- source malfunction branches conflict with a functioning-source branch.

Therefore D4C5 must consume **composable observation constraint alternatives**, not only a Boolean feasibility result.

## 6. Required D4C4c seam

Refactor the topology observation evaluator so its internal alternatives can be represented as a typed branch:

```text
TopologyObservationConstraintBranch(
    setupFacts,
    registrationWitness
)
```

The public feasibility API may remain as a convenience projection:

```text
branches.any { setupWitnessExists(base + branch.setupFacts) }
```

but bundle evaluation needs the branches themselves.

For a bundle:

1. obtain alternatives for each observation;
2. apply any selected registration-witness binding before composition;
3. cartesian-compose branch alternatives;
4. canonicalize / reject directly contradictory setup facts cheaply;
5. run `TroubleBrewingTopologySetupWitnessEvaluator` once per surviving composed branch;
6. stop at the first exact witness for that topology.

The branch product is over logical explanation alternatives, not mechanical worlds.

## 7. Composition must preserve finite-resource semantics

D4C3 already gives setup witness evaluation finite-resource meaning for:

- one actual Drunk role;
- one Poisoner role;
- one Poisoner target;
- unique role identities;
- setup type counts.

Bundle branch composition must pass all observation facts into the **same** setup witness call so those resources stay shared.

Do not evaluate Poisoner or Drunk separately per observation.

## 8. Composition propositions still needed

Before bundle integration, D4C4c must cover the current first-night SDE composition forms:

```text
AnyOf
AllOf
Not
```

especially PUBLIC_GOOD_INFO:

```text
speaker evil
OR
(
    shown-role claim
    AND
    (
        Drunk
        OR Poisoned
        OR (Functioning AND clue)
    )
)
```

The topology path must preserve those alternatives as branch structure.

## 9. Structure derivation does not need mechanical worlds

Given a surviving exact strategic-key set:

```text
possibleDemonSeats
evilTeamSeatConfigurations
strategicWorldKeys
forcedGoodSeats
forcedEvilSeats
evilCoverSeats
```

can all be derived directly.

For setup topology:

- Demon seat = key.demonSeat;
- evil seats = Demon + Minions;
- good seats = every other seat;
- forced sets are intersections over surviving keys.

No role permutation is needed.

## 10. Migration order

Current state:

```text
D4C4c composable observation branches                 IMPLEMENTED
    ↓
parallel ExactStrategicTopologyBundleDiagnostics      IMPLEMENTED
    ↓
pristine first-night topology bundle evaluator        IMPLEMENTED
    ↓
bounded setup / observation differential              IMPLEMENTED
    ↓
bounded whole-bundle shared-witness differential      NEXT ACCEPTANCE GAP
    ↓
D4E 5–15 CPU / memory / latency evidence              HARNESS READY / EVIDENCE PENDING
    ↓
final [full-ci] T4                                    PENDING
    ↓
migrate feasibility-only SDE consumers                LATER / NOT D2D4 ACCEPTED YET
```

Do not remove or reinterpret the old exact mechanical-cardinality API in D2D4. Do not migrate production consumers before whole-bundle parity and performance/T4 evidence are accepted.

## 11. Acceptance condition before cutover

A topology bundle key may be reported as surviving only when:

```text
there exists one shared exact setup/mechanical witness
that satisfies the complete observation bundle
and the selected registration witness bindings
```

Bounded fixtures must prove:

```text
topology bundle surviving keys
==
distinct StrategicWorldKey projection
of exhaustive exact whole-bundle surviving worlds
```

before any production SDE feasibility consumer migrates.
