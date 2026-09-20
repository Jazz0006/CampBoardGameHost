# SDE-1D Lifecycle Ownership — Completion Note

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Status: **COMPLETE**

## 1. Scope

SDE-1D establishes the persistent / committed / planned ownership boundary without creating another state, history, revision, or lifecycle store.

No Poisoner replanning, uncertainty policy, production routing, UI behavior, or candidate selection ownership changed.

## 2. Existing ownership audit

### Canonical revisions and durable truth

`ClocktowerGameSession` / `ClocktowerSessionState` remain the only canonical owners of:

- `gameStateRevision`;
- `playerInputRevision`;
- canonical `GameState`;
- durable action / epistemic history;
- semantic-history mode and global chronology.

SDE lifecycle metadata may copy revision **values as provenance only**; it cannot advance them.

### Structured-information freshness

`InformationDecisionRevision` is the existing typed two-revision freshness value.

`InformationDecisionSnapshot` already owns the structured-information candidate-space identity through:

- `semanticIdentity`;
- `revision`;
- validated legal/recommended candidate membership.

SDE-1D therefore reuses those identities rather than creating another candidate-space snapshot.

### First-night ready/displayed lifecycle

`FirstNightInformationLifecycle` / `FirstNightInformationMigration` confirm the intended semantic split:

- ready/generated candidates are disposable and are not game facts;
- `invalidateUnshown()` discards unshown work;
- displayed information is retained;
- display is the existing migration commit boundary for the typed observation.

These types remain existing first-night migration ownership; SDE does not absorb or duplicate them.

### Durable decision/event stores

`DecisionEventStore` and `DynamicDecisionTransactionAggregate` are committed/durable transaction authorities. They are not suitable as a mutable registry for SDE plans.

Using either as the planned-decision store would incorrectly persist recommendation-local state and create competing lifecycle ownership.

## 3. Frozen lifecycle ownership

```text
PERSISTENT
    session/setup-owned durable commitments
    SDE may reference them as input
    SDE does not own writable copies

COMMITTED
    session/history-owned executed or shown facts
    immutable from SDE planning perspective
    SDE does not own writable copies

PLANNED / UNCOMMITTED
    recommendation-owned disposable identity/freshness metadata
    may become stale before execution
    never a game fact
```

The important design result is intentionally asymmetric: SDE gets an explicit planned reference, while PERSISTENT and COMMITTED remain negative ownership boundaries rather than new SDE wrapper stores.

## 4. Implemented contract

Added:

`clocktower/recommendation/sde/PlannedDecisionRef.kt`

The record contains only:

```text
decisionId
candidateId
sourceRevision: InformationDecisionRevision
optional sourceSemanticIdentity
```

For the current structured-information integration target, `fromInformationSnapshot(...)`:

1. requires the candidate ID to belong to the source snapshot's legal candidate IDs;
2. copies only stable decision/candidate identity, existing revision provenance and semantic snapshot identity;
3. does not retain the legal candidate pool, recommended pool, `GameState`, selected payload, observation history or any mutable session object.

Freshness is pure:

- matching source revision => current;
- changed `gameStateRevision` => stale;
- changed `playerInputRevision` => stale;
- when snapshot-bound, changed semantic candidate-space identity => stale;
- revision-only planned refs can be used by later decision families that do not have the structured-information snapshot contract.

No method commits, mutates, increments a revision, or appends semantic history.

## 5. Focused regression evidence

Added:

`PlannedDecisionRefTest.kt`

It proves:

1. a structured-information plan captures stable decision/candidate identity plus existing source freshness provenance;
2. matching source snapshot remains fresh;
3. changing `gameStateRevision` invalidates the plan;
4. changing `playerInputRevision` invalidates the plan;
5. changing candidate-space semantic identity invalidates a snapshot-bound plan even at the same revision;
6. revision-only plans do not accidentally depend on unrelated information candidate spaces;
7. `fromInformationSnapshot(...)` rejects a candidate absent from the validated legal source space.

The tested owner is a pure value object. There is no session handle or mutation API through which stale detection could commit state.

## 6. Executable validation

Executable SDE-1D head:

`c4e0967e416f0a259e50ab38ef2d832f6ab1b7c0`

Validation on PR #144:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

The Android FAST job executed successfully on the executable SHA; this is not a docs-only skip result.

Live `main` at validation remained:

`4d6e90a7a268570d261048931a3557433ea01d83`

PR branch was ahead of and not behind `main`.

## 7. Non-goals preserved

SDE-1D did not add:

- another `GameState` / `DynamicGameState` copy;
- another revision counter;
- another action/observation history store;
- a global mutable planned-decision registry;
- PERSISTENT/COMMITTED writable SDE mirrors;
- Poisoner invalidation/replanning;
- Drunk or Spy/Recluse uncertainty policy;
- badness thresholds;
- production selection cutover;
- UI changes;
- `ConsequenceEvaluator` migration/deletion.

## 8. Next — SDE-1E

The next bounded slice is one real healthy structured-information shadow/integration proof:

```text
rules-owned legal candidate
→ existing semantic materialization
→ StorytellerDecisionEngine exact consequence evaluation
→ PlannedDecisionRef freshness provenance
→ existing InformationDecisionContext confirmation
→ existing session commit
```

The integration must preserve visible behavior and keep SDE observational/evaluative only. `InformationDecisionContext` remains confirmation authority and `ClocktowerGameSession` remains the only durable commit authority.

> **SDE-1D is complete. SDE owns only disposable planned-decision identity/freshness metadata; persistent and committed truth remain session-owned. Continue with SDE-1E as a bounded shadow integration of one healthy structured-information path, with no production selection cutover.**
