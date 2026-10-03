# Trouble Brewing Canonical Game Snapshot Integration Route — 2026-09-30

> Date: 2026-09-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ARCHITECTURE INTEGRATION ROUTE / TB ONLY / NO POLICY CHANGE AUTHORIZED**  
> Scope: integrate a standard immutable Trouble Brewing game-state snapshot between the canonical Game Engine/session boundary, Storyteller Recommendation Engine, and ClocktowerEvidenceLab historical replay. This route does not authorize a broad GameState rewrite or non-TB generalization.

## 1. Decision

Introduce a versioned, immutable, read-only Trouble Brewing snapshot contract, provisionally named:

```text
TroubleBrewingGameSnapshotV1
```

This snapshot is a **projection**, not a new mutable game-state authority.

Canonical ownership remains:

```text
setup lifecycle / committed setup
+ ClocktowerGameSession / ClocktowerSessionState
+ canonical action / epistemic / decision histories
                |
                | pure projection
                v
TroubleBrewingGameSnapshotV1
                |
        +-------+--------+
        |                |
        v                v
Game/rules consumers   Decision Context Builder
                              |
                              v
                   Storyteller Recommendation Engine
```

The snapshot contract is also the intended semantic interoperability boundary for EvidenceLab historical reconstruction.

## 2. Why the existing GameState is not the cross-project contract

The current `GameState` remains useful as a bounded mechanical runtime component:

- script;
- seated players;
- actual/shown identity after those facts are materialized;
- alive/dead;
- poison state;
- seed.

It should **not** be expanded into one universal God object merely to serve every decision surface.

In particular, the current DLB Drunk lifecycle proves that a valid decision-time game state can exist **before every seat has a committed actualRole**. The post-seating / pre-Drunk state has:

- complete shown-seat identities;
- `hasDrunk = true`;
- no committed Drunk seat yet.

Therefore the cross-boundary snapshot must represent committed and not-yet-committed facts explicitly rather than require a fully materialized final `GameState`.

## 3. Required state-value semantics

The TB snapshot contract must distinguish at least:

```text
KNOWN(value)
UNCOMMITTED
UNKNOWN
NOT_APPLICABLE
```

Meanings:

- `KNOWN(value)`: the fact is committed/known at this snapshot boundary;
- `UNCOMMITTED`: the decision that will create this fact has not happened yet;
- `UNKNOWN`: the historical fact may already exist, but the source/reconstruction cannot establish it;
- `NOT_APPLICABLE`: the field does not apply in this game/state.

This distinction is essential for EvidenceLab replay. `UNCOMMITTED` must never be collapsed into `UNKNOWN`.

Evidence-specific derivation/verification such as OBSERVED / RECONSTRUCTED / INFERRED / VERIFIED / DISPUTED remains outside this Host snapshot contract.

## 4. V1 content boundary

V1 is Trouble Brewing only and should remain intentionally small.

Conceptual shape:

```text
TroubleBrewingGameSnapshotV1
├── position
│   ├── stage
│   ├── phase
│   └── round
├── grimoire
│   └── seats[]
│       ├── seat
│       ├── participant reference / stable game-scoped identity where needed
│       ├── shownRole
│       ├── actualRole
│       ├── alive
│       └── poisoned
├── setupState
│   ├── hasDrunk
│   ├── drunkAssignment
│   ├── redHerring
│   └── demonBluffs
├── runtimeState
│   ├── protectedSeats
│   ├── pendingAttack
│   └── spentAbilities
└── historyPrefix
    ├── mechanical facts
    ├── delivered information
    └── Storyteller decisions
```

Not every conceptual field must be implemented in TBGS-0. Fields should be added only when the first bounded consumer needs them.

## 5. Explicit exclusions

Do not put recommendation-policy or external enrichment into the canonical TB snapshot merely because a recommendation may consume it.

Keep outside the snapshot:

- legal candidate lists;
- recommendation scores / quality tiers;
- `evilAdvantage` / `publicBalanceHint`;
- policy / selector / algorithm config versions;
- player experience;
- cross-game recent-role history;
- public-claim interpretation unless/until a stable game-scoped canonical claim history owner exists;
- EvidenceLab assertion IDs, timestamps, derivation or verification state;
- source provenance;
- UI-local state;
- recovery bookkeeping.

Rules derive legal candidates from the snapshot/current canonical state. Optional player/history context belongs in decision-specific enrichment.

## 6. Current repository audit

The current code already contains most required owners, but the read boundary is fragmented:

- `ClocktowerGameSession` / `ClocktowerSessionState` are the closest canonical mutable runtime aggregate;
- `GameSnapshot` is already an immutable ruleset-backed snapshot after final setup materialization;
- `TroubleBrewingIntermediateSetup` correctly represents post-seating / pre-Drunk truth;
- `CommittedClocktowerSetup` represents final actual/shown setup truth;
- `ActionFactTimeline`, `EpistemicObservationLog`, and decision history keep distinct semantic histories;
- the newer `StorytellerDecisionEngine` exact-consequence seam deliberately avoids the legacy `DynamicGameState`.

The main convergence debt is projection ownership:

- production has many `PlayerCard -> toClocktowerGameState()` reconstruction paths;
- `ClocktowerHostScreen.dynamicStorytellerState()` derives recommendation inputs from presentation/events and mixes mechanical state with policy-oriented summaries;
- legacy `DynamicGameState` contains derived pressure/balance/registration summaries and is not suitable as the canonical shared state contract.

No broad cleanup is authorized by this finding.

## 7. Integration with the current DLB route

This route does **not** invalidate DLB-0 through DLB-5H1.

Instead, split the former H2 dynamic recommendation-state projection into two stages.

### TBGS-0 — canonical TB snapshot contract — COMPLETE / ACCEPTED

Accepted executable checkpoint: `f367c0d3ec23ebf452c924ff7c0921cd978a800f`; CI #3618 / R2 #3343 GREEN, including Android FAST and Real Clingo.

Implemented boundary:

- V1 four-state field semantics preserve `UNCOMMITTED` separately from historical `UNKNOWN`;
- stable cross-project role semantics use canonical external role IDs rather than Host-internal `RoleId.value`;
- pure projectors cover setup precommit, committed setup, and a runtime decision boundary without moving canonical ownership;
- runtime position carries explicit phase/round plus canonical game-state/player-input revisions;
- deterministic V1 JSON encode/decode and the G10 precommit golden JSON establish the first interchange fixture;
- no recommendation ranking, policy authority, Host/UI migration, Recovery change, or non-TB generalization was introduced.

Original scope:

- define `TroubleBrewingGameSnapshotV1`;
- define the state-value semantics above;
- define stable TB semantic IDs and deterministic serialization/versioning as needed by tests/export;
- add bounded golden fixtures for at least:
  - Drunk assignment precommit;
  - finalized setup;
  - one runtime decision boundary;
- no recommendation ranking change;
- no canonical owner change;
- no broad consumer migration.

### TBGS-1 — Drunk decision vertical slice — COMPLETE / ACCEPTED

#### TBGS-1A — Host snapshot-backed decision vertical slice — COMPLETE / ACCEPTED

Accepted executable checkpoint: `ae4dc2400325d233da033d3c86d2863bde1bd485`; CI #3621 / R2 #3345 GREEN, including Android FAST.

Host now:

- derives policy-neutral Drunk candidate refs `(seat, shownRoleId)` from the canonical V1 snapshot through the rules-owned `TroubleBrewingDrunkCandidateDomain`;
- builds a typed `DrunkAssignmentDecisionContext` containing the snapshot, freshness revision, legal domain, decision identity and selection seed;
- routes the existing Drunk shadow adapter through that context while preserving the existing named candidate only as downstream presentation/hypothetical enrichment;
- consumes the G10 V1 interchange JSON directly to reproduce the legal candidate domain without Host setup objects;
- preserves existing candidate IDs/order, DecisionTrace/replay, frozen `BEGINNER_CONSERVATIVE_V1`, and deferral-only `DRUNK_ASSIGNMENT_SHADOW_V1` behavior.

#### TBGS-1B — EvidenceLab historical materializer / equivalence — COMPLETE

Observed EvidenceLab checkpoint: `970e7e6430f7088ac004cd1e5696759da4d52003`.

EvidenceLab now materializes the same G10 pre-Drunk V1 semantic payload from its reconstruction revision + historical prefix using a pure deterministic codec, with no persistence migration and no copied Host legality/policy. A fresh Host-side comparison of `g10-game2-precommit-tbgs-v1.json` from both repositories confirmed byte-for-byte equality: 2115 bytes on each side. This closes the first cross-project semantic-equivalence checkpoint.

The completion validates the snapshot boundary only. It does not authorize a Drunk candidate preference or production policy.

Use Drunk assignment as the first end-to-end consumer.

Target boundary:

```text
canonical setup lifecycle
    -> TB snapshot
    -> rules-owned Drunk legal candidate domain
    -> typed Drunk decision context
    -> shadow / later versioned production policy
```

The first production-capable Drunk request introduced after qualifying evidence should consume this standard snapshot boundary rather than create another recommendation-specific GameState variant.

TBGS-1 also defines the cross-project semantic fixture used by EvidenceLab. EvidenceLab remains responsible for event-sourced reconstruction/provenance and produces an equivalent V1 snapshot projection; it does not become a Host state owner.

### TBGS-2 — former H2 runtime migration

Do only after the Drunk cutover/fallback boundary and DLB-6/7 are stable enough.

Incrementally migrate recommendation consumers away from:

```text
PlayerCard / UI state
    -> ad-hoc toClocktowerGameState()
    -> DynamicGameState
```

toward:

```text
canonical owners
    -> TB snapshot projector
    -> typed decision context builder
```

Do not migrate all callers in one PR.

#### TBGS-2A — first-night natural pair recommendation context — COMPLETE / ACCEPTED

The 2026-10-03 focused audit selected the Trouble Brewing Washerwoman / Librarian / Investigator first-night natural-pair precompute as the first migration slice.

Current:

```text
PlayerCard
    -> toClocktowerGameState()
    -> first-night precompute
    -> natural pair candidate generator
```

Target:

```text
ClocktowerGameSession
    -> GameSnapshot
    -> TroubleBrewingGameSnapshotV1
    -> TroubleBrewingFirstNightPairDecisionContext
    -> first-night precompute
    -> natural pair candidate generator
```

V1 already contains the required mechanical facts for this consumer, so TBGS-2A must not expand the snapshot schema. Candidate IDs/order/outcomes/registration semantics and visible recommendation behavior are behavior-preserving constraints.

The typed context may temporarily carry a compatibility `GameState` derived purely from the immutable snapshot while the existing pair generator remains on its historical internal input type. That derived value is not authority and must never be reconstructed from `PlayerCard` inside the TB production path. Preserve the old natural-truth precompute boundary by retaining real poison on the snapshot but normalizing `poisoned=false` only in this compatibility candidate-space projection; reliability remains downstream.

Do not bundle pair manual/publication authority, setup coordination, DynamicGameState consumers, A3 presentation-catalog extraction, Recovery or A4 cleanup into TBGS-2A.

Acceptance: exact T4 head `9021ef26b65033b69911fa4a79124fdd2137284d`; CI #3647 / R2 #3364 GREEN, including Android full + assemble, ASP contracts and Real Clingo.

#### TBGS-2B — pair manual/publication snapshot context — COMPLETE / ACCEPTED

The focused post-2A re-audit confirmed that TB manual selectable-domain projection and pair publication still rebuild `GameState` from `PlayerCard`.

Target:

```text
TroubleBrewingFirstNightPairDecisionContext
    -> PairInformationLegalDomain
    -> manual legal options / selected AbilityObservation
```

Reuse the existing 2A snapshot-backed compatibility GameState because pair truth/display semantics do not read its poison flag; Drunk/Poisoned permission is carried by explicit `ReliabilityState`. Extend the typed context with role definitions derived from the validated TB character registry so this slice also stops sourcing rules input from the presentation catalog adapter.

Do not expand the TB snapshot schema or include setup coordination, DynamicGameState, numeric/categorical information families, A3, Recovery or broad Host decomposition. No Greater Joy Investigator remains on its existing non-TB compatibility adapter; TBGS-2B does not invent a cross-script snapshot abstraction.

Acceptance: exact T4 head `fa07e382fdad1b03aeadc27ce0d0d939f66f8b67`; CI #3654 / R2 #3370 GREEN, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo. TBGS-2B is COMPLETE / ACCEPTED.

Authority: `docs/TBGS_2_RUNTIME_RECOMMENDATION_STATE_MIGRATION_AUDIT_2026-10-03.md`.


#### TBGS-2C — setup recommendation mechanical/rules base context — COMPLETE / ACCEPTED

The focused post-2B audit found one remaining production reconstruction in the pre-first-night setup recommendation screen:

```text
ClocktowerJudgeScreen PlayerCard list
    -> recommendationCards.toClocktowerGameState(...)
    -> SetupCoordinationRequest(game, roles, lockedDecisions, history)
    -> SetupRecommendationService
```

The migration must separate the request into two categories rather than placing the whole coordination request in the TB snapshot:

```text
canonical committed/runtime state
    -> TroubleBrewingGameSnapshotV1
    -> TroubleBrewingSetupRecommendationDecisionContext
       - recommendation GameState compatibility projection
       - rules-owned RoleDefinition catalog

explicit coordination inputs kept outside snapshot
    - locked/committed StorytellerDecision values
    - CrossGameHistory enrichment
```

The existing setup recommender depends on actual role/alignment/type, shown identity, alive/poison state, seed and role definitions. TB snapshot V1 already carries the required mechanical facts. Unlike the first-night natural-pair context, the setup context must preserve the snapshot's real poisoned state because setup evaluation explicitly scores functioning Empath and impaired pair sources.

Player names are not recommendation semantics. Existing review coverage already proves setup recommendations are invariant under player renaming, so the compatibility projection may use deterministic seat labels without expanding the interchange snapshot.

The initial prewarm may project from the existing canonical committed setup via `TroubleBrewingGameSnapshotProjector.fromCommitted()`; the live pre-first-night UI may project from `ClocktowerGameSession.toGameSnapshot(...)` via `fromRuntime()`. The typed context must accept only finalized `SETUP_COMMITTED` or `RUNTIME/FIRST_NIGHT/round 1` snapshots and reject precommit/other runtime positions.

TBGS-2C may migrate only the Trouble Brewing mechanical/rules base. No Greater Joy remains on the legacy adapter. Do not change `SetupCoordinationRequest` lock/history semantics, setup recommendation policy/ranking, Red Herring or Demon-bluff commitment ownership, SDE shadow APIs, `DynamicGameState`, Recovery, A3, R3 or generic setup-effect ownership.

Acceptance passed at exact T4 head `a8116d8b333cc40e0a599ab208cf7a9a3ea80207`: CI #3663 and R2 #3378 GREEN, including Android `:app:testFull + :app:assembleDebug`, ASP contracts and Real Clingo. The compatibility context preserves the historical setup-recommendation role order while sourcing role definitions from the validated rules registry, preventing Demon-bluff presentation-order drift. TBGS-2C is COMPLETE / ACCEPTED.

Authority: `docs/TBGS_2C_SETUP_COORDINATION_SNAPSHOT_CONTEXT_AUDIT_2026-10-03.md`.

#### TBGS-2D — Demon succession typed recommendation context — AUDIT COMPLETE / IMPLEMENTATION READY

The post-2C `DynamicGameState` family audit selected Demon succession as the next bounded production migration.

Current recommendation flow duplicates the rules-owned legal domain:

```text
DemonSuccessionSemantics / resolveTroubleBrewingImpSelfKillSuccession(...)
    -> DemonSuccessionResolution.None / Choice / Forced
    -> Night Host legal target seats

DemonSuccessorRecommender
    -> independently rebuilds alive-Minions / Scarlet-Woman legality

Host UI
    -> filters selected recommendations back through legal target seats
```

TBGS-2D should converge this boundary to:

```text
ClocktowerGameSession
    -> GameSnapshot
    -> TroubleBrewingGameSnapshotV1
    -> TroubleBrewingDemonSuccessorDecisionContext
       + rules-owned DemonSuccessionResolution
       + explicit legacy-equivalent pressure / spent-ability / balance enrichment
    -> DemonSuccessorRecommender ranks only the already-legal domain
```

The TB snapshot already contains the required mechanical facts: actual role/alignment/type, alive/dead, poison, seat, phase/round, seed and revisions. No snapshot schema expansion is required.

`DecisionHistoryRepository.project()` is the correct typed owner for future pressure/misinformation/registration projections, but production decision-event append/transition is not yet wired. TBGS-2D must therefore preserve current enrichment semantics rather than silently replacing UI-derived pressure/history with an incomplete session history.

Virgin / Slayer / Artist spent flags also remain outside the canonical action timeline. They may stay as explicit enrichment inputs for the existing balance calculation; TBGS-2D does not authorize a session/Recovery ownership rewrite.

Mayor redirection, Spy/Recluse registration, cross-cutting Artist/night-information balance consumers, decision-history producer cutover, A3, Recovery and R3 remain outside this slice.

Authority: `docs/TBGS_2D_DYNAMIC_STATE_CONSUMER_SELECTION_AUDIT_2026-10-03.md`.

## 8. Updated execution relationship

The current product/evidence sequence becomes:

```text
DLB-0..5H1 COMPLETE
-> Drunk cutover audit COMPLETE / NOT PASSED
-> recommendation-context capability audit COMPLETE
-> TBGS-0 canonical snapshot contract
-> TBGS-1A Host snapshot-backed Drunk decision context/shadow COMPLETE / ACCEPTED
-> TBGS-1B EvidenceLab G10 materializer + cross-project semantic equivalence COMPLETE
-> TBGS-1 overall COMPLETE / ACCEPTED
-> post-TBGS-1 cutover recheck COMPLETE / NOT PASSED
|| EvidenceLab C2 batch acquisition continues
|| EvidenceLab C3 comparison/rejection acquisition continues
-> first C3 Stage-1 VERIFIED ordering/rejection evidence
-> production-capable versioned Drunk policy over snapshot + typed request
-> re-run cutover gate
-> Beginner automatic Drunk authority only if PASS
-> DLB-6 old-contract retirement
-> DLB-7 acceptance
-> TBGS-2A / 2B / 2C incremental runtime migrations COMPLETE / ACCEPTED
-> TBGS-2D Demon succession typed recommendation context AUDIT COMPLETE / IMPLEMENTATION READY
```

TBGS-0/1 and EvidenceLab C3 may proceed in parallel. TBGS-0/1 must not invent ranking semantics while evidence is still missing.

## 9. Cross-project contract

EvidenceLab remains event-source authoritative for historical evidence:

```text
SetupCommitment + SemanticEvent prefix
        |
        | pure reconstruction projection
        v
TroubleBrewingGameSnapshotV1
        +
DecisionSlice
        +
observed expert choice / rationale / provenance
```

Host remains rules/policy authoritative:

```text
TroubleBrewingGameSnapshotV1
        +
DecisionPoint / typed request
        |
        +-> rules-owned legal candidate domain
        +-> required context
        +-> optional enrichment
        v
Storyteller Recommendation Engine
```

The two projects share **domain semantics and a versioned interchange contract**, not a mutable Kotlin/Python implementation type and not a database.

## 10. First acceptance fixture

Use the already accepted G10 Drunk-assignment replay as the first cross-project golden contract.

At the historical boundary immediately before Drunk assignment:

- all shown seats are known;
- setup requires one Drunk;
- Drunk seat/actual identity is UNCOMMITTED;
- later Red Herring, Demon bluffs and first-night information are not visible to the snapshot.

Acceptance:

1. EvidenceLab can materialize the V1 historical snapshot from the evidenced prefix;
2. Host can consume the same semantic snapshot;
3. Host rules independently derive the legal Drunk candidate domain;
4. the historical Empath choice maps into that domain;
5. no expert-choice label is turned into production policy;
6. no later history leaks backward.

## 11. Non-goals

This route does not authorize:

- non-TB generalized `CanonicalGameSnapshot`;
- replacing `ClocktowerGameSession`;
- making `GameState` partial/unknown-friendly throughout the runtime;
- storing EvidenceLab confidence/provenance inside Host game state;
- moving legality into recommendation code;
- moving enrichment into canonical game truth;
- migrating every `toClocktowerGameState()` caller now;
- rewriting Recovery;
- changing `BEGINNER_CONSERVATIVE_V1`;
- adding Drunk ranking before qualifying evidence exists.

Generalize beyond Trouble Brewing only after V1 has survived multiple real TB decision surfaces and an actual second-script requirement exists.
