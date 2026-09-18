# SDE-2D4 Topology-first Exact Feasibility — Proposition / Fanout Audit

> Date: 2026-09-18 Australia/Sydney  
> Base: `main@2cab06efeee5692024e065c632deefe765ca1618`  
> Branch: `sde-2d4-5-15-validation-performance`  
> Parent authority: `docs/SDE_2D4_5_15_CORRECTNESS_PERFORMANCE_NORMALIZED_METRICS_AUDIT_2026-09-18.md`  
> Status: **AUDIT COMPLETE — implementation not yet started**

## 1. Scope

The topology-first path exists to answer one exact question efficiently:

```text
For this StrategicWorldKey and this pristine Night-1 player-visible knowledge / hypothetical bundle,
does at least one mechanically legal Trouble Brewing witness exist?
```

It is not a replacement for:

- historical B4 replay;
- the complete `PlayerWorldSet` API;
- the exhaustive enumerator as bounded oracle;
- rules legality;
- recommendation policy.

The large-player bottleneck is pristine first-night planning, so v1 feasibility must stay deliberately narrower than the generic historical exact engine.

## 2. Production fanout that needs scalable first-night exact evidence

Current first-night SDE / experiment consumers reach the exact hypothetical bundle evaluator through:

1. healthy whole-bundle evaluation;
2. Drunk pair whole-bundle evaluation;
3. Drunk Chef/Empath whole-bundle evaluation;
4. Drunk Fortune Teller whole-bundle evaluation;
5. Demon bluff strict shown-role probes;
6. SDE exact consequence adapters around those same first-night observations.

The PUBLIC_GOOD_INFO projection has a stable logical form:

```text
speaker is evil
OR
(
    speaker has claimed shown role
    AND
    (
        ability is Drunk-malfunctioning
        OR ability is Poisoned-malfunctioning
        OR (ability is functioning AND original clue proposition)
    )
)
```

Therefore topology-first feasibility must preserve latent malfunction explanations without exposing them to recommendation policy.

## 3. Proposition classification

### 3.1 Structural / setup-domain

`PlayerCount`

- exact from the candidate setup size;
- no role assignment search.

`SetupProfile`

- exact from the legal standard / Baron profile being tested;
- profile is an input alongside the strategic topology because `StrategicWorldKey` intentionally does not encode Townsfolk-vs-Outsider counts.

### 3.2 Topology-resolvable when natural registration is sufficient

`AlignmentAt`

- actual GOOD/EVIL is determined by whether the seat is in Demon/Minion topology;
- an observation may still admit Spy/Recluse registration alternatives, so the generic observation case is registration-sensitive.

`CharacterTypeAt(Demon|Minion)`

- natural actual type is topology-resolvable;
- optional Spy/Recluse registration again makes the observation form registration-sensitive.

`AliveAt`

- pristine Night 1 is structurally alive for every seat;
- dynamic alive/dead support remains historical/B4 scope.

### 3.3 Role-assignment-resolvable

`RoleAt`

- needs unique role identity assignment;
- observation semantics are registration-sensitive for Spy/Recluse.

`CharacterTypeAt(Townsfolk|Outsider)`

- good topology alone does not distinguish Townsfolk from Outsider;
- needs legal setup-profile type allocation.

`RoleInPlay`

- needs role-set existence, but not necessarily a full seat permutation;
- a topology-first solver should treat this as a set/count constraint.

### 3.4 Mechanical-variant-resolvable

`ShownRoleAt`

- ordinary characters show their actual role;
- the Drunk can show an unin-play Townsfolk role;
- therefore the constraint can require either role identity assignment or an existential Drunk shown-role variable.

`AbilityStateAt`

- functioning is default;
- Drunk malfunction is tied to Drunk identity;
- poisoned malfunction requires an in-play Poisoner and an existential legal Poisoner target;
- the current setup model permits the Poisoner targeting any seat, including the Drunk collapse represented by the existing ability-state semantics.

`BooleanResult(DEMON_OR_RED_HERRING_PRESENT)`

- Demon detection is topology-resolvable;
- Red Herring is an existential mechanical variable requiring Fortune Teller in play and one actual-good Red Herring seat;
- Recluse may optionally register as Demon while functioning.

### 3.5 Topology + registration

`NumericResult(ADJACENT_EVIL_PAIRS)`

- natural count is fully determined by evil seat topology;
- functioning Spy/Recluse at queried seats can alter registration choices;
- exact witness binding therefore needs role identity / malfunction state only for seats where optional registration matters.

`NumericResult(LIVING_EVIL_NEIGHBOURS)`

- on pristine Night 1 the subject-seat set is supplied by the materializer;
- natural count is topology-resolvable;
- Spy/Recluse registration makes it conditionally role-sensitive.

### 3.6 Composition

`AnyOf`, `AllOf`, `Not`

- no independent world dimension;
- recurse into child constraint classes;
- registration witness alternatives must remain separate when branches require them.

### 3.7 Explicitly outside v1 scalable first-night feasibility

`GrimoireState`

- exposes full Spy-visible mechanical state and reminder constraints;
- not PUBLIC_GOOD_INFO and not required by the current first-night SDE scalable path;
- remain on the existing exact world/historical authority.

Historical replay:

- current-role succession;
- deaths / alive transitions;
- cross-night Poisoner targets;
- night action chronology;
- other-night wake state.

These remain B4/exhaustive-historical scope.

Unsupported numeric enum members whose current exact setup evaluator does not implement must not gain accidental semantics merely because a new solver exists.

## 4. Required v1 semantic surface

The first scalable exact slice must cover the proposition surface actually emitted by current Night-1 SDE:

```text
PlayerCount
SetupProfile
RoleAt
ShownRoleAt
AlignmentAt
CharacterTypeAt
AbilityStateAt
RoleInPlay
AnyOf / AllOf / Not
NumericResult(ADJACENT_EVIL_PAIRS)
NumericResult(LIVING_EVIL_NEIGHBOURS)
BooleanResult(DEMON_OR_RED_HERRING_PRESENT)
```

plus current registration witness selection for Spy/Recluse.

Anything outside this surface must return an explicit unsupported/deferred result and fall back only where bounded exact evaluation is safe. It must never silently return UNSAT.

## 5. Minimal exact search state

A topology-first feasibility witness does **not** need to materialize every irrelevant seat permutation.

Inputs:

```text
player count
legal SetupProfile
StrategicWorldKey
recipient seat + perceived role
setup knowledge
hypothetical observations
role definitions
selected registration-witness bindings
```

Latent witness variables only as needed:

```text
specific role identities required by constraints
Townsfolk/Outsider allocation on good seats
Minion role identities on Minion seats
Drunk actual seat
Drunk shown role
Poisoner in-play / target
Fortune Teller in-play / Red Herring seat
Spy/Recluse role identity and functioning state where registration is used
```

The one Trouble Brewing Demon is Imp, so Demon role identity adds no search multiplicity once the Demon seat is fixed.

## 6. Search order

The solver should fail cheaply before role assignment:

1. validate topology shape against player count/profile;
2. apply topology-only alignment/Demon/Minion constraints;
3. reject numeric topology contradictions that need no registration;
4. bind explicit role/shown-role/type constraints on referenced seats;
5. assign required Minion identities;
6. assign only good role identities/types needed by propositions;
7. satisfy role-in-play / uniqueness / finite-count constraints for the unreferenced remainder combinatorially;
8. solve Drunk / Poisoner / Red Herring latent variables;
9. evaluate registration-sensitive branches;
10. stop at the first complete valid witness.

Do **not** assign every irrelevant good role to every irrelevant good seat just to prove existence.

## 7. Ownership

Target:

```text
clocktower/epistemic
    TroubleBrewing strategic topology feasibility
        -> exact witness-existence authority

recommendation/sde
    -> consumes resulting feasible StrategicWorldKeys
    -> owns normalized diagnostics / later policy
```

The recommendation package must not own role legality, registration mechanics or possible-world search.

## 8. Result contract

The first seam should distinguish:

```text
FEASIBLE
    at least one exact mechanical witness exists

INFEASIBLE
    exact search proves no witness exists

DEFERRED
    proposition/history capability is outside the v1 exact surface
```

A timeout or search budget exhaustion is **not** INFEASIBLE.

If bounded execution controls are later required, budget exhaustion must be an explicit incomplete/deferred outcome.

## 9. Correctness oracle

For every supported bounded fixture:

```text
topology-first feasible keys
==
distinct StrategicWorldKey projection of exhaustive exact worlds
```

Test both positive and negative keys.

Required differential dimensions:

- standard profile;
- Baron profile;
- healthy pair clue;
- Librarian zero;
- Chef;
- Empath;
- Fortune Teller / Red Herring;
- Drunk shown role;
- Poisoner malfunction;
- Spy registration;
- Recluse registration;
- strict Demon bluff shown-role probe;
- contradictory bundle.

## 10. Implementation slices

### D4C1 — topology domain + cheap structural predicates

Introduce exact topology enumeration from player count/profile:

```text
Demon seat × Minion seat combinations
```

with no role-world generation.

Cross-check key set shape/count against combinatorial bounds.

### D4C2 — referenced-seat role/type feasibility

Add:

- recipient perceived role/Drunk identity constraint;
- RoleAt;
- ShownRoleAt;
- CharacterTypeAt;
- RoleInPlay;
- uniqueness and setup type counts.

### D4C3 — malfunction / mechanical variables

Add:

- AbilityStateAt;
- Drunk shown role;
- Poisoner target;
- Fortune Teller Red Herring.

### D4C4 — numeric / registration semantics

Add:

- Chef;
- Empath;
- Fortune Teller yes/no registration path;
- Spy/Recluse registration witness bindings.

### D4C5 — exact bundle integration

Use the topology-first path for supported pristine first-night strategic diagnostics.

Keep existing exhaustive evaluation as bounded oracle / fallback during migration.

## 11. Non-goals

Do not:

- duplicate historical B4;
- build a generic SAT framework before TB constraints require it;
- use recommendation policy to prune exact feasibility;
- infer UNSAT from timeout;
- enumerate all raw role permutations inside each topology;
- retire the exhaustive oracle;
- start SDE-3.
