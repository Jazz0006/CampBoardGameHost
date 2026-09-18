# SDE-2D3 Strategic-world Quotient — Fanout / Representation Audit

> Date: 2026-09-18 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Base: PR #146 merged to `main` as `0aa488098f1284e26d8df03f4028cc263bcf9f8a`  
> Branch: `sde-2d3-strategic-world-quotient`  
> Status: **AUDIT COMPLETE — executable implementation not yet started**

## 1. Goal

SDE-2D3 changes the primary strategic identity from raw mechanical-role permutations toward a quotient that represents the evil setup topology:

```text
StrategicWorldKey(
    demonSeat,
    minionSeats
)
```

The quotient is descriptive exact evidence. It is not a recommendation score and must not become a second world solver.

A strategic world survives exactly when at least one mechanically legal exact world with that setup topology survives the same observations / witness constraints.

## 2. Existing exact structure is useful but insufficient

`ExactHistoricalHypotheticalObservationBundleEvaluator` already accumulates:

```text
ExactWorldStructureDiagnostics(
    possibleDemonSeats,
    evilTeamSeatConfigurations,
    forcedGoodSeats,
    forcedEvilSeats,
    evilCoverSeats
)
```

This is valuable D2D2-era structure, but it is **not** the target quotient.

Why:

```text
possibleDemonSeats = {5, 7}
evilTeamSeatConfigurations = {{2,5,7}}
```

cannot tell whether the surviving exact worlds include:

```text
Demon=5, Minions={2,7}
Demon=7, Minions={2,5}
or both.
```

The current accumulator stores the Demon marginal and the evil-seat set separately, so Demon/Minion pairing information is lost.

Therefore SDE-2D3 must introduce a joint topology key rather than reconstructing it from the existing marginals.

## 3. Current owners / fanout

### 3.1 Mechanical truth owner

`EnumeratedWorld` is the exact mechanical witness.

It intentionally contains both:

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  historical dynamic role state
```

Registration is evaluated per observation and is not persisted as identity.

### 3.2 Exact consequence owner

`ExactHistoricalHypotheticalObservationBundleEvaluator` owns exact BEFORE / AFTER hypothetical consequence truth.

Both pristine first-night streaming and historical replay already pass every surviving `EnumeratedWorld` through `WorldStructureAccumulator`.

This means the strategic quotient can be accumulated **in the same exact scan**. No second world enumeration is required.

### 3.3 Generic PlayerWorldSet boundary

`PlayerWorldSet` currently exposes only marginals such as:

```text
possibleDemonSeats()
possibleMinionSeats()
possibleRoles(seat)
```

It does not preserve joint Demon/Minion topology.

Do **not** add the D2D3 quotient to this generic interface in the first slice.

Reasons:

- D2D3 already has an exact hypothetical owner that scans the required worlds;
- forcing every world-set representation to implement the new joint API expands the change unnecessarily;
- the current ZDD prototype does not encode `currentRolesBySeat`;
- D2D3 correctness should be established before broad representation/API migration.

### 3.4 Existing SDE consumer

D2D2 Demon-bluff diagnostics currently use:

`afterStructure.evilTeamSeatConfigurations`

for union/intersection/topology-pattern diagnostics.

That remains compatible evidence but is coarser than the D2D3 target. D2D3 may add precise strategic-key diagnostics beside it; do not remove the coarse field until consumers are migrated and differential evidence is green.

## 4. Frozen semantic definition

### 4.1 V1 key uses setup identity

The D2D3 v1 key is based on immutable `rolesBySeat`, not `currentRolesBySeat`:

```text
demonSeat
    = the unique setup CharacterType.DEMON seat

minionSeats
    = all setup CharacterType.MINION seats, canonical sorted order
```

Rationale:

- SDE-2D3 is the pre-SDE3 first-night strategic quotient;
- this is the stable “which side / which evil setup topology” identity the current route is trying to measure;
- it is total for a legal supported setup;
- Imp / Scarlet Woman succession changes historical current roles but must not rewrite setup identity;
- good-role permutations, shown-role alternatives, poison state, Red Herring, registration witnesses and explanation clusters must not create extra strategic weight merely by multiplicity.

Dynamic **active Demon** state is a different cross-night dimension. If SDE-3 needs it, add it explicitly rather than silently changing the meaning of `StrategicWorldKey`.

### 4.2 Canonical shape

Target minimal type:

```text
StrategicWorldKey(
    demonSeat: Int,
    minionSeats: List<Int>
)
```

Contract:

- `demonSeat > 0`;
- `minionSeats` is sorted and distinct;
- Demon seat is not in Minion seats;
- key equality is independent of role names and all good-role assignments;
- role definitions remain the authority for CharacterType classification.

Do not store raw role IDs inside the strategic key.

## 5. Target epistemic ownership

The durable target belongs in the epistemic consequence layer, directly beside exact world structure:

```text
EnumeratedWorld
    ↓ exact projection
StrategicWorldKey
    ↓ same streaming accumulator
ExactWorldStructureDiagnostics.strategicWorldKeys
    ↓
SDE diagnostics / policy consumers
```

Recommendation must consume this result; recommendation must not enumerate role assignments to recreate it.

Recommended compatibility extension:

```text
ExactWorldStructureDiagnostics(
    existing fields...,
    strategicWorldKeys: Set<StrategicWorldKey>
) {
    distinctStrategicWorldCount = strategicWorldKeys.size
}
```

Existing fields remain until downstream migration is proven.

## 6. Important historical-state finding

Historical worlds can contain dynamic role duplication.

Existing regression evidence demonstrates:

- immutable setup Imp remains in `rolesBySeat`;
- after succession, `currentRolesBySeat` may contain a dead former Imp and a living successor Imp;
- `possibleDemonSeats()` intentionally reports the living current Demon.

Therefore setup strategic topology and active-current Demon topology are **not the same concept** after succession.

D2D3 must not conflate them.

A useful regression is:

```text
before succession:
    setup key = Demon 10, Minions {8,9}

after Scarlet Woman succeeds:
    setup key = Demon 10, Minions {8,9}   // unchanged
    current active Demon = 9              // separate historical fact
```

## 7. ZDD boundary

Current `WorldZdd` atoms encode setup role, shown role, Red Herring, alive/dead state, ability state and explanation clusters.

They do not encode `currentRolesBySeat`.

For the **setup** strategic key this is not a correctness blocker because setup `rolesBySeat` is encoded. However D2D3 should not modify ZDD or `PlayerWorldSet` merely to expose the quotient in the first slice.

D2D4 owns measured 5–15 player representation/performance work and can decide whether a native symbolic topology projection is useful.

## 8. Required differential evidence

Before any SDE policy consumes the new quotient, tests must prove:

### D3A — key semantics

1. Two mechanical worlds with identical setup Demon/Minion seats but different good-role permutations map to the same `StrategicWorldKey`.
2. Two worlds with the same total evil-seat set but Demon and Minion roles swapped map to different keys.
3. Poison / Drunk / Red Herring / shown-role / explanation-cluster differences do not split the strategic key when setup evil topology is unchanged.
4. The key uses role-definition CharacterType, not role-name string heuristics.

### D3B — exact accumulator parity

For bounded fixtures:

```text
ExactWorldStructureDiagnostics.strategicWorldKeys
==
manual distinct projection of every surviving mechanical exact world
```

Prove this for both BEFORE and AFTER hypothetical bundles.

### D3C — historical stability

Use existing dynamic-role/succession fixtures to prove setup strategic key remains stable when `currentRolesBySeat` changes.

### D3D — SDE consumer migration

When D2D2 / whole-bundle diagnostics consume the precise key:

- same evil seats with Demon/Minion swap must remain distinct;
- raw role permutations within one key must not gain additional strategic weight;
- existing visible recommendation behavior remains unchanged until explicit policy cutover.

## 9. Implementation slices

### Slice 1 — epistemic key + accumulator

Tests first:

- add `StrategicWorldKey`;
- add exact projector from one `EnumeratedWorld` + role definitions;
- extend `ExactWorldStructureDiagnostics` with exact distinct key set;
- populate it inside existing `WorldStructureAccumulator`;
- no new enumeration pass.

### Slice 2 — exact differential fixtures

Add bounded manual-projection parity and historical setup-stability fixtures.

Do not add broad corpus/performance work here.

### Slice 3 — SDE precise projection

Expose precise strategic keys through existing exact consequence diagnostics to SDE consumers.

Keep role-information utility separate from strategic-topology pressure.

Do not introduce a scalar “strategic score”.

### Slice 4 — migration audit

Audit every consumer of coarse `evilTeamSeatConfigurations`.

Migrate only consumers that semantically need Demon/Minion distinction. Retain coarse compatibility diagnostics until no caller requires them.

## 10. Performance boundary

D2D3 reduces **strategic representation multiplicity**; it does not by itself solve the cost of producing exact mechanical witnesses.

For a 15-player one-Demon / three-Minion setup, the seat-topology upper bound is:

```text
15 × C(14,3) = 5,460
```

This is small relative to complete role permutations, but the current exact engine may still spend substantial time discovering which topologies have witnesses.

Therefore:

- D2D3 establishes exact quotient semantics and ownership;
- D2D4 measures 5–15 player CPU/memory cost;
- D2D4 may introduce symbolic/constraint feasibility for topology existence if needed;
- do not claim D2D3 alone makes 15-player exact planning mobile-feasible.

## 11. Non-goals

Do not during the initial D2D3 implementation:

- add a second recommendation-owned world solver;
- change setup legality;
- change committed history;
- change `SetupRecommendationService` selection;
- retire `demon-bluff-ease`;
- change PlayerWorldSet/ZDD representation without measured need;
- redefine setup strategic topology after Imp succession;
- fold role-information utility into topology count;
- add random sampling as exact evidence;
- start SDE-3 cross-night policy.

## 12. Audit conclusion

The repository is already close to the desired architecture:

- exact mechanical worlds are the correct witness authority;
- the exact hypothetical evaluator already has the correct streaming accumulation point;
- SDE already consumes structure diagnostics without owning worlds.

The missing piece is **joint Demon/Minion topology identity**.

The smallest correct D2D3 implementation is therefore an epistemic `StrategicWorldKey` projected from immutable setup role identity and accumulated during the existing exact pass. This preserves exactness, removes role-permutation multiplicity from the strategic unit, and avoids creating a second solver or prematurely widening the PlayerWorldSet/ZDD API.
