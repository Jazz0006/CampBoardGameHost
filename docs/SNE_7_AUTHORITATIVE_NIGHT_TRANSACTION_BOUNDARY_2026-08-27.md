# SNE-7 — Authoritative Night Transaction Boundary

> Date: 2026-08-27  
> Scope: same-night correctness closeout  
> Status: **DESIGN ACCEPTED / IMPLEMENTATION NOT STARTED**  
> Branch: `codex/clocktower-same-night-effective-state-correctness`

## 1. Purpose

SNE-7 is not a generic architecture rewrite and does not reopen A3. Its purpose is to replace brittle source-string wiring verification with executable behavioral contracts at the real night-transaction boundary, while preserving the current persistence and timeline authorities.

The immediate task is **tests-first behavioral RED work**. Production extraction follows only after the RED contracts are established.

## 2. Authority model

The authoritative model is:

```text
Durable night UI/transaction state
    ClocktowerNightCheckpoint

Durable game-history authority
    GameState / existing ClocktowerGameSession timeline

Transient UI/application command
    NightResolutionEvent

Pure transition / planning
    NightCheckpointReducer
    ClocktowerEffectiveNightStateProjector
    NightDawnResolutionPlanner

Commit intent
    DawnCommitIntent

Durable commit authority
    ClocktowerGameSession / existing App transaction boundary
```

Hard rules:

1. `ClocktowerNightCheckpoint` remains the sole durable night transaction/checkpoint state.
2. `GameState` and the existing session/timeline remain the durable game-history authority.
3. No second persisted coordinator state is introduced.
4. `NightResolutionEvent` is a transient command only; it is not a durable event log and does not introduce event sourcing.
5. Restore reconstructs from checkpoint + game state + ruleset/canonical plan, not from replaying UI commands.

## 3. Navigation and identity

`ClocktowerNightCheckpoint` already owns `nightStepIndex`. Do not add another `interactionIndex` or navigation cursor as stored authority.

```text
checkpoint.nightStepIndex
+ canonicalInteractions
→ currentInteraction
→ effective cursor
```

The cursor and current interaction are derived values.

Stable seat and interaction identity must continue to come from canonical/stable ordering, never from re-indexing filtered views.

## 4. Draft, confirmation, navigation, and invalidation

These lifecycle stages are distinct:

```text
confirmed A
→ MovePrevious
→ confirmed A still authoritative

Edit draft to B
→ confirmed A still authoritative

MoveNext / Confirm B
→ confirmed upstream fact changes A → B
→ only then invalidate dependent downstream confirmed facts
```

Protected contract:

> Navigation does not invalidate mechanical facts. Draft editing does not invalidate mechanical facts. Dependent invalidation occurs only when an upstream confirmed fact is reconfirmed to a changed authoritative value.

Typed commands must therefore distinguish edit from confirm/navigation. Conceptually:

```text
EditPoisonDraft(target)
ConfirmPoison

EditDemonAttackDraft(target)
ConfirmDemonAttack

EditMayorRedirectDraft(target)
ConfirmMayorRedirect

EditDemonSuccessorDraft(target)
ConfirmDemonSuccessor

MovePrevious
MoveNext
```

Exact generic/event type design should reuse existing repository types where possible rather than proliferating role-specific wrappers unnecessarily.

## 5. Internal production seams

Do not create one large coordinator that reimplements all night rules.

Preferred internal responsibilities:

```text
NightCheckpointReducer
  draft edit
  confirm
  navigation
  dependent-fact invalidation

ClocktowerEffectiveNightStateProjector
  existing same-night derived mechanical state authority

NightDawnResolutionPlanner
  validated deaths
  succession consequences
  poison carry
  outcome gate
  DawnCommitIntent
```

A production facade may compose these seams, but it must stay thin and must not duplicate Mayor legality, Demon succession, poison functioning, effective role, effective alive, or other existing pure semantics.

Use existing domain types such as `GameState` / `GameSnapshot`; do not introduce a meaning-overlapping `ClocktowerGameState` model.

## 6. Dawn responsibility

The reducer/planner computes what should happen; it does not become a second persistence or timeline authority.

Conceptually:

```text
Reducer / projector / planner
→ next checkpoint
→ validated mechanical consequences
→ DawnCommitIntent

ClocktowerGameSession / App transaction boundary
→ sequence allocation
→ role-change commit
→ death commit
→ history/observation commit
→ phase transition
```

The exact commit order remains owned by the existing durable transaction boundary.

`NightTransition.mechanicalEvents`, if retained, must represent transition consequences/output rather than a second complete mechanical source of truth. If the complete set is fully derivable from checkpoint + projected state, do not persist or duplicate it.

## 7. Restore and reconstruction contracts

A simple `derive(input) == derive(input)` determinism check is useful but insufficient.

SNE-7 restore testing must exercise the real reconstruction boundary:

```text
checkpoint encode/save
→ process-death / lifecycle boundary as applicable
→ checkpoint decode
→ ruleset + canonical interaction plan rebuild
→ derived effective state reconstruction
```

Required restore cases include:

- legacy draft-only successor data does not invent confirmation or `RoleChanged`;
- invalid confirmed successor fails closed / requires valid reconfirmation;
- missing interaction is handled safely;
- out-of-range `nightStepIndex` is handled safely;
- stale Mayor redirect to Demon fails closed;
- current effective role may differ from public/base role and reconstructs correctly;
- confirmed successor + Previous remains confirmed;
- draft edit without Next/Confirm leaves the old confirmed fact authoritative.

Additional persistence invariant:

> Restore must never promote draft state into confirmed mechanical fact.

## 8. Testing strategy

The goal is behavior-first verification, not wholesale deletion of all source tests.

### Keep

Fast pure semantics tests remain high-value, including examples such as:

- `ClocktowerEffectiveNightStateTest`
- `DemonSuccessionSemanticsTest`
- `PoisonEffectLifecycleTest`
- `MayorRedirectRecommenderTest`

### Add / migrate to behavioral contracts

Priority cross-layer behaviors:

- Imp self-kill → confirm successor → resolve → identity confirmation → Dawn intent;
- successor confirmation survives navigation to the identity-confirmation stage;
- Poisoner→Demon ends effective poison immediately and does not carry poison into day;
- next night uses the current living Imp rather than a dead old Imp;
- a new Imp can self-kill and produce the succession step again;
- Sage/other Demon-sensitive information uses current effective Demon identity;
- stale restored Mayor→Demon redirect fails closed at final resolution;
- Monk-protected Mayor cannot use an old redirect confirmation to kill another player;
- reconfirmed upstream Imp target invalidates stale dependent Chambermaid/other downstream confirmations as specified;
- same durable inputs reconstruct the same effective state.

### Compose/UI smoke tests

Keep only a small set proving UI-to-typed-seam wiring, for example:

- Fortune Teller two-target flow does not crash and displays the result;
- Previous → edit Imp target → Next uses the newly confirmed fact;
- Chambermaid only exposes current effective legal/alive targets;
- new Demon identity confirmation completes the handoff to Dawn.

### Source guards

Source-string tests are a last resort, not the primary correctness layer.

Prefer API/type constraints such as:

```text
Empath calculation consumes EffectiveNightState
Chambermaid legality consumes effectiveAliveSeats
Dawn resolution consumes validated resolved death / commit intent
```

Do not ban legitimate domain fields such as `eliminatedRound` across an entire file. Only retain narrowly scoped negative guards for a specific known-dangerous consumer path when the invariant cannot yet be expressed through types or APIs.

## 9. Safe implementation order

```text
SNE-7.1  behavioral REDs for real cross-layer failures
         no production changes yet

SNE-7.2  extract NightCheckpointReducer
         draft / confirm / navigation / invalidation GREEN

SNE-7.3  extract NightDawnResolutionPlanner
         validated resolution + DawnCommitIntent GREEN

SNE-7.4  switch production Compose/App wiring to typed seams
         Compose ceases to be transaction authority

SNE-7.5  restore / process-death reconstruction matrix

SNE-7.6  2–4 Compose smoke/integration tests

SNE-7.7  delete source-string assertions only when a real behavioral contract fully supersedes them

SNE-7.8  retain only minimal architecture guards, preferring types/APIs over source inspection
```

For every source-string test removed, identify the behavioral contract that replaces it.

## 10. Immediate execution boundary

**Current immediate task:** SNE-7.1 tests-first RED design and implementation.

Do not begin by refactoring `ClocktowerHostScreen.kt`, `CampBoardGameHostApp.kt`, `ClocktowerGameSession`, or other production files. The next production extraction is authorized only after the behavioral REDs establish the intended lifecycle contract.

This means the answer to “are we currently changing production code?” is:

```text
Immediate next slice: NO — tests/contracts first.
Whole SNE-7 stage: YES — production reducer/planner/seam extraction follows after RED is proven.
```

## 11. Non-goals

Do not:

- reopen A3 Architecture Hardening;
- resume App-root decomposition;
- introduce event sourcing;
- create a second durable night-state model;
- move sequence/timeline commit authority out of `ClocktowerGameSession`;
- broaden to generic arbitrary non-self Demon death / Scarlet Woman succession;
- use recommendations as legality authority;
- delete source-string tests before replacement behavioral coverage is GREEN.
