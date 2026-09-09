# R3 — Transaction application viability audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / NO-GO  
> Branch: `codex/r3-transaction-application-viability-audit`  
> Audit baseline: `main@f64245573db246cfb3900d8f0a698e94158b9b1c`  
> Scope: read-only production audit; no production Kotlin or test behavior changes.

## Decision

**R3 is NO-GO. Do not implement a new App transaction controller/applier. End the D6 decomposition campaign after this audit.**

The difficult transaction semantics are already owned by narrow typed components:

```text
NightCheckpointHostTransaction
  -> checkpoint + revision intent
  -> no durable side effects

NightDawnResolutionPlanner
  -> pure checkpoint / continuation / DawnCommitIntent

NightDawnDurableMaterializationPlanner
  -> pure exactly-once materialization plan
  -> stable action/observation IDs
  -> independent repair of mechanical and durable-history partial commits

ClocktowerGameSession
  -> canonical writable mechanical/revision/history authority
  -> atomic individual action/observation commit primitives
```

What remains in `CampBoardGameHostApp.kt` is not one missing cohesive domain transaction. It is boundary choreography across those existing owners plus Compose-facing presentation projection, localized records/events, A4/public-observation durability preflight, game-outcome continuation and phase-specific UI flow.

Moving that choreography into a new class would mostly relocate fan-out. To own the ordering completely, the new object would need a broad context/callback surface equivalent to App; to stay narrow, it would leave the real ordering in App and therefore fail to become an authoritative owner.

## Audit question

The roadmap permits R3 implementation only if App's remaining transaction-application sequence can be represented by one or more narrow immutable apply contracts with:

1. small typed inputs/outputs;
2. clear single ownership of application ordering;
3. no duplicate `ClocktowerGameSession` authority;
4. no Compose dependency in session/domain;
5. typed behavioral evidence;
6. measurable maintainability gain beyond moving lines.

The current production shape does **not** meet those conditions.

## 1. Night checkpoint confirmation is already at the correct boundary

`NightCheckpointHostTransaction` is a deliberately small host adapter. It owns checkpoint-local confirmation/edit semantics and returns only a `NightCheckpointRevisionIntent` plus the reduced checkpoint. It explicitly does not own durable side effects.

The App then applies the returned revision intent and presentation mirrors. That is the intended transaction boundary, not an unextracted controller.

A second wrapper around these calls would either:

- duplicate the adapter's ownership; or
- merely bundle App assignments/revision calls into callbacks.

Neither creates a new cohesive responsibility.

**Result: NO-GO for further night-checkpoint controller extraction.**

## 2. Dawn planning and retry/idempotency already have a real typed owner

`NightDawnResolutionPlanner` owns pure Dawn/succession planning.

`NightDawnDurableMaterializationPlanner` owns the hard retry decision: from durable projection plus stable IDs it decides independently whether each death, role change, poison transition and phase advance still needs:

- mechanical state mutation;
- action history commit;
- public alive observation commit.

This is the meaningful reusable transaction abstraction. It allows a retry to repair partial persistence without duplicating durable history.

The SNE-7 restore/retry acceptance test independently materializes the returned plan and proves convergence/exactly-once semantics. This confirms that the stable contract is the **plan**, not a production executor object.

**Result: preserve the planner as the transaction semantic owner. Do not add a second durable-plan owner.**

## 3. Dawn death application is heterogeneous boundary choreography

The `onConfirmNight` death path must preserve a specific cross-owner order:

```text
resolve/plan death
-> build durable materialization projection
-> preflight public-alive observation when required
-> record redirect / poisoned / protected attack presentation events
-> record stable Death ActionFact when missing
-> synchronize canonical session death when missing
-> publish session view
-> update PlayerCard presentation mirror
-> add localized record/event
-> durably record the preflighted public alive observation when missing
-> branch into demon succession / Klutz / Ravenkeeper continuation
```

The preflight is deliberately before mutable/public projection. The ActionFact, session mechanical state, App card mirror, records/events and epistemic observation do not have the same owner or the same retry semantics.

A hypothetical `DawnDeathApplier` that truly owned this order would need ports for at least:

- session mechanical mutation/publication;
- action-history commit;
- epistemic preflight/commit and A4 durability;
- `PlayerCard` presentation mutation;
- localized records/events;
- event-sequence allocation;
- succession/Klutz/Ravenkeeper continuation.

That is already a broad transaction context in another name.

If those responsibilities stayed in App, the extracted class would only return instructions already supplied by `NightDawnDurableMaterializationPlanner`.

**Result: NO-GO for a Dawn death executor/controller.**

## 4. Succession/new-Demon application cannot share a uniform effect executor

`onConfirmNewDemon` consumes one typed Dawn plan, but its effect application remains intentionally specialized:

- role change uses role lookup, semantic role-change history, actual-role mutation, localized promotion record and role-change event;
- poison carry uses a poison ActionFact plus session poison-state synchronization and App poison mirrors;
- phase advance uses a PhaseAdvance ActionFact, App phase projection and the established revision cadence;
- checkpoint/pending-successor fields are cleared only after the continuation permits Dawn.

These effects do not share one mechanical apply shape. Their only honest commonality is that `NightDawnDurableMaterializationPlanner` decides which pieces are still missing.

A generic `apply(effect)` algebra would have to erase meaningful type differences or dispatch back into role/death/poison/phase-specific callbacks. That recreates the App fan-out behind an abstraction.

**Result: NO-GO for a generic Dawn effect executor.**

## 5. Day execution is a separate lifecycle, not part of the Dawn transaction owner

`onConfirmDay` has superficially similar operations—preflight, death synchronization, records/events and phase movement—but semantically it owns a different boundary:

```text
execution/no-execution confirmation
-> execution preflight/debug evidence
-> accepted day-boundary revision
-> execution death/public projection
-> Saint / Klutz / Demon succession / ordinary outcome branching
-> Mayor final-three no-execution outcome
-> Dusk poison expiry
-> next-round Night phase
```

It also contains `DebugFlightRecorder` evidence and Day/Dusk-specific outcome rules that are absent from Dawn.

Sharing a transaction owner with Dawn would therefore couple unrelated lifecycles merely because both can kill a player and advance phase.

A separate `DayExecutionApplier` would still need the same broad App presentation/outcome ports and would produce little net ownership simplification.

**Result: NO-GO for Day execution extraction in R3.**

## 6. Revision ownership blocks a single generic apply protocol

The current flows intentionally have different accepted revision boundaries:

- individual night confirmations can own their own revision intent;
- `onConfirmNight` advances the night-closing game-state revision before Dawn application;
- succession/new-Demon continuation applies its final Dawn materialization under a different continuation/revision path;
- Day confirmation advances the accepted Day boundary before execution materialization;
- some phase transitions subsequently advance revision at their established lifecycle point.

A generic applier would therefore need policy inputs telling it when revision already happened, when it must happen, and when it must not happen.

That policy is transaction semantics, not infrastructure. Passing it as flags would weaken the typed boundary; moving it into the applier would make that applier a second lifecycle coordinator.

**Result: no single narrow revision-aware apply contract exists today.**

## 7. Existing tests support the current boundary, not a missing executor

The relevant tests reinforce the current architecture:

### `NightTransactionHostIntegrationSmokeTest`

Its contract explicitly stops before App-owned durable side effects. It proves that checkpoint transaction, persistence/reconstruction and Dawn planner agree without introducing a second coordinator/state owner.

### `NightDawnRestoreRetryConvergenceAcceptanceTest`

It takes a `DawnDurableMaterializationPlan`, applies the typed pieces in a test-local materializer and proves that partial mechanical/history persistence converges exactly to uninterrupted state and remains idempotent on replay.

This is strong evidence for the existing planner/session boundary. It is not evidence that production needs a reusable executor.

Creating a new production applier would require a new broad wiring/integration test surface for an abstraction whose main benefit would be source relocation.

## Alternatives considered

### A. `DawnDurableMaterializationStateFactory`

Possible mechanically because App repeats construction of `DawnDurableMaterializationState`.

**NO-GO:** it would save a small amount of projection boilerplate but would not own transaction ordering or remove semantic fan-out. It is below the threshold for this high-risk second wave.

### B. `DawnDurableCommitApplier(plan, ports)`

Could be made testable only by defining multiple ports/callbacks for session, history, observation durability, presentation, events and continuation.

**NO-GO:** this is effectively `AppTransactionContext`/callback-bag architecture, explicitly forbidden by the R3 gate.

### C. `DayExecutionApplier`

**NO-GO:** Day execution is cohesive as a lifecycle callback but its dependencies are App presentation/outcome/Dusk concerns. Extraction would relocate the callback, not reduce ownership complexity.

### D. expand `ClocktowerGameSession` to own full Dawn transaction

**NO-GO:** this would pull presentation/localization/A4/flow concerns into the canonical session owner, violating domain/UI separation and risking duplicate/hidden persistence authority.

## GO-criteria evaluation

| Criterion | Result | Reason |
| --- | --- | --- |
| Small typed inputs/outputs | FAIL | Complete executor needs broad heterogeneous ports/context. |
| Single application-order owner | FAIL | Either App remains real owner or new controller must absorb multiple unrelated owners. |
| Preserve one session authority | PASS only if no extraction | Existing `ClocktowerGameSession` boundary is correct. |
| No Compose dependency in session/domain | PASS only if no extraction | Session expansion would violate this; App-side controller still needs presentation callbacks. |
| Typed behavioral evidence | PASS for current planner/session seam | Tests validate plan/retry semantics, not a production executor contract. |
| Maintainability gain beyond line movement | FAIL | Expected result is relocation of several hundred orchestration lines with equal or greater dependency fan-out. |

Overall: **NO-GO**.

## Frozen architecture after R3

Keep:

```text
Planner/reducer
  owns pure transition semantics and exactly-once intent/planning

ClocktowerGameSession
  owns canonical writable GameState, revisions and semantic chronology primitives

CampBoardGameHostApp
  owns the remaining cross-boundary application choreography and Compose-facing projection
```

This is an acceptable architectural endpoint. A large composition root is not automatically wrong when the remaining lines are exactly the wiring between narrower authoritative owners.

## Revisit trigger

Do not reopen R3 merely because `CampBoardGameHostApp.kt` remains large.

Re-audit only if a future product change naturally creates one of these seams:

- session gains a real typed atomic transaction API required by multiple non-UI callers;
- presentation projection moves behind a dedicated typed presenter for independent product reasons;
- a second production host surface needs the same Dawn commit protocol;
- persistence semantics change such that one explicit durable transaction object becomes necessary.

Absent such a trigger, another decomposition pass is expected to increase abstraction/callback cost without reducing semantic ownership.

## Validation / change scope

This R3 task is a read-only code audit plus documentation result.

```text
Production Kotlin changes: 0
Test Kotlin changes:       0
Workflow changes:          0
Behavior changes:          0
```

No Gradle/Android test execution is required for the audit conclusion because no executable source changed. Existing D6.2 FULL/T4 acceptance remains the production baseline.

## Next

Per `CURRENT_DEVELOPMENT_ROADMAP.md`, R3 NO-GO ends the D6 decomposition campaign.

Next product/engineering priority becomes:

```text
square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
