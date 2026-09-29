# DLB-4 Canonical Drunk Commit Producer / Consumer / Ownership Audit — 2026-09-29

> Status: **COMPLETE / ACCEPTED IMPLEMENTATION AUTHORITY**
> Baseline audited: `main@6adf0276103580118f6f3d7a8cf0fb9e6af836be`
> Accepted executable checkpoint: `5606371c68b97beb01418ede2bb2c19db7e69053`; CI #3580 and R2 #3313 GREEN.
> Live remote at audit start: no open PR; main CI #3572 GREEN.
> Scope: canonical Drunk commit before reveal only. DLB-4A UX, DLB-5 dependency barriers, DLB-6 compatibility retirement, Red Herring/Poisoner expansion, broad App/Host decomposition and Recovery compatibility are out of scope.

## 1. Architecture pre-flight

- current owner: `TroubleBrewingIntermediateSetup` owns the post-visible-roster/post-seat setup; `TroubleBrewingDrunkCandidateDomain` owns legal Drunk candidates; `TroubleBrewingDrunkHypotheticalProjector` owns mutation-free candidate projection; the current runtime still finalizes through `compatibilityDealPlan`.
- proposed responsibility: add one rules/setup-owned canonical commit seam that accepts the current intermediate setup plus an explicitly confirmed legal Drunk candidate and materializes the final immutable setup/GameState.
- authoritative state owner(s): rules/setup keep legality; the commit seam validates and materializes final setup truth; `ClocktowerGameSession` owns canonical mutable game truth after startup; App remains wiring/lifetime owner only.
- narrow typed seam:

```text
TroubleBrewingIntermediateSetup
+ explicitly confirmed TroubleBrewingDrunkCandidate (required iff hasDrunk)
+ canonical character registry
-> TroubleBrewingCommittedSetupResult
     - CommittedClocktowerSetup
     - canonical initial GameState
     - confirmed Drunk candidate (nullable only when hasDrunk=false)
```

- forbidden behavior: no candidate ranking, no shadow-policy selection, no `BEGINNER_CONSERVATIVE_V1` changes, no future player-choice inputs, no Recovery draft persistence.

## 2. Current producer chain

Current production preparation is:

```text
TroubleBrewingProductionSetupPreparer
-> TroubleBrewingIntermediateSetup
-> TroubleBrewingCompatibilityDealPlanAdapter
-> compatibilityDealPlan
```

The DLB-1 bridge still marks the transitional added Townsfolk as the fallback Drunk. This is explicitly not legal-domain or canonical commit authority.

DLB-2 already provides the correct legal source:

```text
TroubleBrewingIntermediateSetup
-> TroubleBrewingDrunkCandidateDomain.legalCandidates()
-> every dealt Townsfolk, ordered by seat
```

and a pure candidate projection:

```text
intermediate + legal candidate
-> TroubleBrewingDrunkHypotheticalProjector
-> GameState with exactly that seat actualRole=Drunk and shownRole unchanged
```

DLB-4 should reuse this legality/projector boundary rather than reimplementing Drunk role semantics.

## 3. Current consumers that still depend on compatibility finalization

### 3.1 App startup

`CampBoardGameHostApp.startTroubleBrewingGame()` currently:

1. prepares the intermediate setup;
2. resolves cards from `preparedSetup.compatibilityDealPlan`;
3. builds setup recommendation and first-night precompute requests from those cards;
4. enters the reveal callback;
5. only inside that callback writes `committedClocktowerSetup` through `TroubleBrewingCommittedSetupAdapter.fromDealPlan()`;
6. records rotation from `TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup()`.

This leaves the old deal plan acting as effective final truth.

DLB-4 must invert that dependency:

```text
prepare intermediate
-> obtain one explicitly confirmed legal candidate
-> canonical commit
-> build cards from committed result
-> create ClocktowerGameSession from committed GameState
-> publish finalized committed setup / completion fact
-> only then enter identity reveal
-> only then dispatch setup/first-night prewarm
```

### 3.2 Committed setup adapter

`TroubleBrewingCommittedSetupAdapter.fromDealPlan()` maps an already-finalized compatibility deal into `CommittedClocktowerSetup`.

This is the wrong DLB-4 owner because it accepts a type that has already encoded the Drunk choice through the legacy compatibility bridge.

DLB-4 should create `CommittedClocktowerSetup` directly from the intermediate + confirmed legal candidate commit result. The old adapter may remain temporarily for compatibility/tests and is retired in DLB-6.

### 3.3 Rotation/completion

`TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup()` currently reads `preparedSetup.compatibilityDealPlan` and reconstructs completion truth from the preset plus the compatibility Drunk.

This is semantically incorrect once an original preset Townsfolk can be chosen as Drunk: the transitional added Townsfolk then remains an actual Townsfolk, so the final actual-role multiset is no longer the original preset multiset.

DLB-4 must derive these fields from the final committed result:

- `realNonDemonRoleIds`;
- `minionRoleIds`;
- `selectedDrunkShownRole`;
- `playerStartingIdentities`.

Preset metadata remains valid only for provenance/style fields.

`TroubleBrewingSetupCompletionPersistence` itself can remain unchanged in DLB-4 because it serializes the already-built completion fact. DLB-6 may later rename legacy field vocabulary if desired.

### 3.4 ClocktowerGameSession

`resetDealState()` currently creates `ClocktowerGameSession` by re-projecting `cards.toClocktowerGameState()`.

DLB-4 should allow Trouble Brewing startup to pass the already committed canonical initial `GameState` into session creation, avoiding a second setup-authority reconstruction. Other scripts may keep the existing card projection path.

### 3.5 Reveal and prewarm

The current reveal coordinator calls the reveal callback before dispatching setup-recommendation prewarm. First-night prewarm is also launched inside that callback.

The new caller ordering must ensure the canonical commit result exists before the coordinator is invoked at all. Then:

- cards/session/final setup are installed synchronously;
- active-game persistence sees finalized truth only;
- first-night prewarm starts from committed GameState;
- identity-reveal A4 prewarm can only become eligible after the finalized cards/session are published.

No DLB-5 sequencing changes are required here.

### 3.6 Recovery

Current Recovery captures SDE replay only when `committedClocktowerSetup`, `ClocktowerGameSession`, and `RulesetRef` all exist. Recovery does not persist `TroubleBrewingIntermediateSetup` or an unconfirmed Drunk candidate.

DLB-4 preserves this boundary. A process death before canonical confirmation remains a setup restart/re-entry case, not a recoverable half-game.

## 4. Transitional candidate confirmation before DLB-4A

DLB-4 is a commit-seam cutover, not the Storyteller-selection UX.

Until DLB-4A provides the Experienced assisted selector, current production behavior may preserve the old deterministic fallback **only as an upstream compatibility confirmation source**:

- identify the fallback Drunk represented by the existing compatibility bridge;
- resolve that seat back to the exact object returned by `TroubleBrewingDrunkCandidateDomain.legalCandidates(intermediate)`;
- pass that exact legal candidate into the new commit seam.

This temporary confirmation path:

- performs no ranking;
- does not read `DRUNK_ASSIGNMENT_SHADOW_V1`;
- does not grant Beginner automatic SDE authority;
- is not part of `TroubleBrewingIntermediateSetup`;
- must fail closed if the compatibility fallback cannot be resolved into the current legal domain;
- is replaced by DLB-4A / later automatic-authority cutover and removed with DLB-6 compatibility retirement.

For `hasDrunk=false`, the confirmed candidate must be null.

## 5. Canonical commit invariants

The DLB-4 commit owner must enforce:

1. `hasDrunk=true` requires exactly one non-null confirmed candidate.
2. That candidate must equal one member of the **current** intermediate setup's legal domain; stale seat/name/shown-role tuples fail closed.
3. `hasDrunk=false` requires a null candidate.
4. A Drunk commit produces exactly one actual Drunk.
5. The chosen seat keeps its dealt Townsfolk `shownRole`.
6. Every other seat has `actualRole == shownRole`.
7. Canonical seat order, player names, script and seed are preserved.
8. Commit does not mutate the intermediate setup or any session.
9. The commit owner never ranks candidates and never reads SDE policy output.

## 6. DLB-4 implementation slices

### DLB-4.1 — typed commit seam

Add the final commit result and commit owner beside the existing rules/setup DLB types.

Tests-first contract:

- legal candidate commits exactly one Drunk;
- original Townsfolk may be chosen and the transitional visible Townsfolk remains a real Townsfolk;
- stale/illegal candidate is rejected;
- missing candidate with `hasDrunk=true` is rejected;
- non-null candidate with `hasDrunk=false` is rejected;
- non-Drunk setup commits all seats actual==shown;
- intermediate input remains unchanged.

### DLB-4.2 — final-truth consumer migration

Add final-commit consumption for:

- App role resolution;
- `TroubleBrewingSetupRotationRecordFactory`;
- completion/rotation history handoff.

The critical regression test chooses an **original preset Townsfolk** as Drunk and proves the completion record reflects the final committed actual-role set rather than the preset/compatibility multiset.

### DLB-4.3 — startup ordering cutover

Update Trouble Brewing startup so:

```text
canonical commit
-> committed cards + canonical GameState
-> ClocktowerGameSession creation
-> committed setup / rotation fact publication
-> persistence
-> reveal
-> prewarm
```

Use typed seams and existing lifecycle tests; do not add a broad source-string App wiring test merely to assert line order. Exact diff review + compile/T1/T4 acceptance cover the App wiring after the owning typed contracts are tested.

## 7. Deferred boundaries

### DLB-4A

- Experienced `Choose the Drunk` interaction;
- manual selection among legal candidates;
- recommendation highlight/preselection;
- shared manual/automatic confirmation UX.

### DLB-5

- Demon bluff latest-safe commitment;
- Poisoner invalidation;
- Red Herring observation barriers;
- first-night dependency planner.

### Shadow/evidence cutover gate

- longitudinal narrative capability;
- evidence-backed candidate ordering;
- authoritative automatic-selection contract;
- Beginner automatic Drunk authority.

### DLB-6

- remove `compatibilityDealPlan`;
- remove old `TroubleBrewingCommittedSetupAdapter.fromDealPlan()` if unused;
- remove old deal-plan/runtime fields and legacy naming;
- re-run producer/consumer search and retire superseded compatibility tests.

## 8. Acceptance

DLB-4 is complete only when:

- production startup no longer uses `compatibilityDealPlan` as final setup/session/history truth;
- one tested commit seam owns intermediate + confirmed legal candidate -> final setup/GameState;
- rotation/completion derive from final committed truth;
- reveal and both setup/first-night prewarm occur only after final commit materialization;
- Recovery remains finalized-truth-only;
- frozen `BEGINNER_CONSERVATIVE_V1` and `DRUNK_ASSIGNMENT_SHADOW_V1` behavior are unchanged;
- exact-head CI/R2 are green and changed-file scope is clean.

## 9. Implementation outcome

DLB-4 is complete at executable checkpoint `5606371c68b97beb01418ede2bb2c19db7e69053`:

- DLB-4.1 added `TroubleBrewingSetupCommitter`, which accepts only the current rules-legal confirmed candidate and materializes one final `CommittedClocktowerSetup` plus canonical initial `GameState`;
- DLB-4.2 added final-truth rotation/completion projection, including the regression where an original preset Townsfolk becomes Drunk and the transitional added Townsfolk remains an actual Townsfolk;
- DLB-4.3 moved production startup to the canonical committed result for cards, setup recommendation input, first-night precompute input and `ClocktowerGameSession` initialization;
- the compatibility fallback is now resolved back into the current legal candidate domain only as a temporary upstream confirmation source; it has no ranking or policy authority and remains scheduled for DLB-4A/DLB-6 replacement;
- initial Recovery persistence is deferred for this startup path until the committed setup and final rotation fact are published, so no half-committed DLB setup is written;
- CI #3580 and R2 #3313 are GREEN for the accepted executable checkpoint.

Next product slice: **DLB-4A — Experienced assisted "Choose the Drunk" UX**. Beginner automatic Drunk authority remains blocked behind the later shadow/evidence cutover gate.
