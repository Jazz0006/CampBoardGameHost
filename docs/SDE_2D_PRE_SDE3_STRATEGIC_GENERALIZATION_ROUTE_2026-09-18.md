# SDE-2D — Pre-SDE-3 Strategic Generalization Route

> Date: 2026-09-18 Australia/Sydney  
> Status: **CURRENT / REQUIRED BEFORE SDE-3**  
> Base checkpoint: PR #144 merged to `main` as `89453c902741699b072d11320d85a5561172abe5`  
> Parent architecture: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 1. Purpose

SDE-1 and SDE-2 established the orchestration, lifecycle, exact-consequence, registration and replanning seams. Before cross-night expansion, three under-specified Night-1 algorithm questions were reviewed and are now resolved:

1. Drunk information-role clues must participate in whole-bundle strategic evaluation rather than remain a deferred reliability variant.
2. Demon bluff identities are Storyteller decision outputs while uncommitted, not fixed strategic inputs. Their legality remains rules/setup-owned; their strategic selection must migrate from the legacy setup heuristic into SDE. Once shown/committed, the chosen bluffs become persistent inputs for later planning.
3. Production strategy must not depend indefinitely on exhaustive raw role-world enumeration as player count grows. Strategic evil topology is the primary recommendation state; complete role assignments remain exact feasibility witnesses and rule-semantic evidence.

This corrective route is mandatory before SDE-3.

## 2. Frozen ownership decisions

### 2.1 Drunk

```text
Drunk actual identity / shown role
    -> PERSISTENT setup/session truth

unshown Drunk clue
    -> PLANNED / disposable Storyteller decision

shown Drunk clue
    -> COMMITTED / immutable observation history
```

SDE must never reselect the Drunk shown role. It consumes the committed shown identity and evaluates legal surface outputs for that shown ability.

### 2.2 Demon bluffs

```text
SetupCandidateGenerator
    -> legal bluff triplets only

StorytellerDecisionEngine / policy
    -> strategic selection among legal triplets

uncommitted bluff triplet
    -> OUTPUT / decision variable

shown + committed bluff triplet
    -> PERSISTENT setup commitment
    -> INPUT to all later uncommitted planning
```

The current `SetupRecommendationService` `demon-bluff-ease` / `bluffDifficulty` heuristic is migration-era selection logic, not the target strategic authority.

### 2.3 Strategic worlds

The epistemic layer remains the single consequence authority. Do **not** create a second recommendation-owned world solver.

A strategic world is a quotient/projection of mechanically legal worlds, initially keyed by:

```text
Demon seat
+ Minion seat set
```

Complete role assignments, Red Herring state, malfunction state and registration witnesses remain available as exact mechanical support for determining whether a strategic topology is feasible and how complex its narrative support is.

Raw role-world multiplicity is not a primary balance metric.

## 3. Target decision model

For an uncommitted Night-1 planning surface:

```text
persistent setup truth
+ committed history
+ legal Storyteller-controlled factors
    - healthy information choices
    - Drunk unreliable clue choices
    - Demon bluff triplets
    - Fortune Teller Red Herring
    - legal registration witness choices where applicable
+ player-controlled robustness cases
    - e.g. Fortune Teller target pairs
        ↓
exact/symbolic mechanical feasibility
        ↓
strategic-world quotient
        ↓
interpretable diagnostics
        ↓
BEGINNER policy gates
        ↓
select acceptable whole-bundle output
```

No opaque global optimum scalar is introduced.

## 4. SDE-2D1 — Drunk whole-bundle completion

### Objective

Make unreliable Drunk information a first-class whole-bundle Storyteller choice.

### Required semantics

For a Drunk shown as an information role, candidate generation produces every **surface-valid** output allowed by the shown ability. The output need not be mechanically true and may accidentally be true.

Public-claim semantics generalize from the healthy form:

```text
speaker is evil
OR
(shown role matches AND clue is mechanically true)
```

to the impaired-capable form:

```text
speaker is evil
OR
(shown role matches AND source ability is malfunctioning)
OR
(shown role matches AND source ability is functioning AND clue is mechanically true)
```

This must preserve hidden actual identity and hidden poison/drunk facts.

### Evaluation decomposition

Every Drunk candidate is evaluated with three related views:

```text
HealthyCore
    all relevant healthy/working information, excluding the Drunk clue

FullBundle
    HealthyCore + the candidate Drunk clue

DrunkMarginal
    strategic difference between HealthyCore and FullBundle
```

Policy requirements:

- HealthyCore must remain sufficiently informative on its own.
- FullBundle must not create catastrophic premature topology collapse or a misleading single-path lock.
- DrunkMarginal is evaluated as deliberate uncertainty/misdirection, not as a discounted healthy clue.
- Do not assign an arbitrary fixed scalar weight such as “Drunk clue = 0.5 normal clue”.

### Acceptance

- remove Drunk from the healthy-bundle “cannot evaluate” boundary for supported shown information roles;
- preserve shown-role persistence and existing revision invalidation;
- prove committed Drunk clues remain immutable;
- include representative Drunk pair, numeric and Fortune Teller-style fixtures as supported capability grows.

## 5. SDE-2D2 — Demon bluff joint-output migration

### Objective

Make bluff selection part of whole-bundle strategic planning.

### Required behavior

Before commitment, the three bluff identities are candidate outputs evaluated jointly with the other Storyteller-controlled Night-1 factors.

After commitment, they are immutable persistent setup inputs and must not be regenerated by later Poisoner/Fortune-Teller-driven replanning.

### Strategic diagnostics

Bluff evaluation should consume strategic support rather than raw bluff-role difficulty alone. Initial interpretable dimensions:

- bluff role support at the actual Demon seat;
- number/diversity of supported evil-team seat configurations;
- whether an information-role bluff has viable claim outputs compatible with the rest of the bundle;
- overlap/redundancy among the three bluff narratives;
- low-complexity versus exception-heavy support;
- interaction with healthy information, Drunk misinformation and Red Herring.

Three bluffs that all rely on one identical narrow counterworld are not equivalent to three independently supported narratives.

### Performance rule

Do not multiply full exact world enumeration by every bluff triplet when the bluff choice can be evaluated as a cheap strategic overlay over shared structural diagnostics.

### Migration target

`SetupCandidateGenerator` remains legality owner.

`SetupRecommendationService` may remain a compatibility caller during shadow migration, but `demon-bluff-ease` / `bluffDifficulty` must not remain the final selection authority after SDE cutover.

## 6. SDE-2D3 — Strategic-world quotient

### Objective

Replace raw mechanical-world multiplicity as the primary recommendation unit.

### Core representation

Initial strategic identity:

```text
StrategicWorldKey(
    demonSeat,
    minionSeats
)
```

For each strategic key the consequence layer should be able to report at least:

```text
feasible
minimal / bounded narrative support information
supporting bluff roles or claims when relevant
required registration witnesses when relevant
required malfunction explanation classes when relevant
```

The exact schema is deliberately not frozen until the fanout/audit identifies the narrowest durable epistemic owner.

### Exactness rule

A strategic world survives when **at least one** mechanically legal assignment/witness satisfies all current visible facts and hypothetical observations.

The production question becomes:

```text
“Does this evil topology have a legal supporting mechanical world?”
```

not:

```text
“How many full Townsfolk-role permutations realize this topology?”
```

### Role-information utility remains separate

Strategic topology alone is insufficient: a Washerwoman/Librarian clue can be useful without immediately changing evil seats.

Therefore retain two conceptually separate axes:

```text
Strategic pressure
    reduction / concentration of evil-team topology

Role-information utility
    useful good-role / identity information that does not necessarily change topology
```

This prevents the policy from protecting evil ambiguity by recommending nearly useless good information.

## 7. SDE-2D4 — 5–15 player generalization and performance envelope

### Semantic coverage

Trouble Brewing rules/world setup semantics already support 5–15 non-Traveller players. SDE-2D must separately prove recommendation semantics and performance.

Use four setup regimes for evidence:

```text
5–6
7–9
10–12
13–15
```

Do not create four different algorithms unless evidence later requires regime-specific policy.

### Normalized diagnostics

Prefer baseline-relative metrics over absolute raw counts:

```text
demonCoverRetention
    after demon-cover / before demon-cover

evilTopologyRetention
    after viable evil topologies / before viable evil topologies

evilCoverRetention
    after evil-cover / before evil-cover

forcedGoodFraction
    forced-good seats / player count
```

Raw BEFORE/AFTER role-world cardinality remains secondary diagnostic evidence.

### Performance study

Measure separately:

1. current raw exact enumeration cost;
2. streaming exact cost;
3. strategic quotient construction cost;
4. symbolic/constraint feasibility cost per strategic topology;
5. memory and latency on representative mobile-scale workloads.

No production player-count cutoff or approximation threshold is frozen before measurement.

### Search-space target

For a 15-player Trouble Brewing topology with one Demon and three Minions, the raw evil-seat topology upper bound is only:

```text
15 × C(14,3) = 5,460
```

This is the intended scale of the primary strategic state, rather than the much larger complete role-assignment space.

## 8. SDE-2D5 — calibration and policy evidence

Expand calibration only after the durable diagnostics above exist.

Required evidence:

- Drunk versus healthy-core contrasts;
- bluff-supported versus bluff-fragile bundles;
- identical raw-world-count cases with different evil topology;
- useful role-information cases with unchanged evil topology;
- representative setups from all four player-count regimes;
- performance reports kept outside ordinary FAST tests.

Policy remains interpretable gates/ordering, not one scalar score:

```text
hard legality
→ reject catastrophic strategic collapse
→ reject healthy-core information starvation
→ reject pathological Drunk misinformation
→ require viable evil narrative support
→ prefer robust/diverse bluff support among acceptable survivors
→ bounded tie-breaking / stable random selection
```

Exact thresholds remain calibration outputs, not route assumptions.

## 9. Implementation order

Execute in this order:

```text
SDE-2D1  Drunk whole-bundle semantics and HealthyCore/FullBundle/DrunkMarginal
    ↓
SDE-2D2  Demon bluff joint-output strategic evaluation
    ↓
SDE-2D3  Strategic-world quotient / feasibility seam
    ↓
SDE-2D4  5–15 player semantic + performance matrix
    ↓
SDE-2D5  cross-regime calibration / policy evidence
    ↓
SDE-3    cross-night impaired / registration decisions
```

A focused audit precedes each executable slice. Add a RED only when the slice introduces or changes a durable behavior contract not already protected.

## 10. Non-goals

SDE-2D must not:

- create a second rules engine or recommendation-owned possible-world solver;
- mutate committed history during planning;
- let SDE reselect the Drunk shown role;
- treat committed Demon bluffs as replannable;
- replace exactness with unlabelled random sampling;
- use raw world count as the primary strategic objective;
- collapse strategic pressure and role-information utility into one opaque scalar;
- cut all production selection to SDE before the corrective contracts are validated;
- retire `ConsequenceEvaluator` early;
- begin cross-night SDE-3 implementation before this route reaches its acceptance gate.

## 11. Acceptance gate for SDE-3

SDE-3 may begin only when all are true:

1. supported Drunk information-role clues are whole-bundle strategic candidates;
2. Demon bluff selection has a validated SDE strategic path and committed-bluff lifecycle behavior;
3. strategic-world topology has a durable exact/symbolic feasibility owner;
4. recommendation diagnostics are player-count-normalized where required;
5. representative 5–15 player semantic/performance evidence exists;
6. no unresolved requirement forces production to exhaustively materialize every raw role world;
7. roadmap and handoff are advanced explicitly to SDE-3.

## 12. Stable decision

> **Before SDE-3, complete SDE-2D. Drunk unreliable information is a whole-bundle decision evaluated through HealthyCore / FullBundle / DrunkMarginal. Demon bluffs are uncommitted Storyteller outputs selected jointly by SDE, while legality remains in SetupCandidateGenerator; after commitment they become persistent inputs. Large-player production evaluation moves toward exact strategic evil-topology equivalence/feasibility rather than exhaustive raw role-world multiplicity, while retaining mechanical assignments as correctness witnesses and role-information utility as a separate axis. No opaque global scalar and no second world solver are introduced.**
