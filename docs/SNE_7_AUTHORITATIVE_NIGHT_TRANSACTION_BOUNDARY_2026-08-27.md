# SNE-7 — Authoritative Night Transaction Boundary

> Date: 2026-08-27  
> Scope: same-night correctness closeout  
> Status: **IMPLEMENTATION IN PROGRESS — 7.1–7.3 ESTABLISHED / 7.4A COMPLETE / 7.4B NEXT**  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54

## 1. Purpose

SNE-7 is not a generic architecture rewrite and does not reopen A3. Its purpose is to replace brittle implementation-shaped wiring checks with executable behavioral contracts at the real night-transaction boundary while preserving the existing persistence and timeline authorities.

The design is accepted and implementation has advanced materially beyond the original handoff.

## 2. Live implementation status

```text
SNE-7.1  behavior-first transaction matrix
         ESTABLISHED / GREEN coverage exists

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED as a typed pure seam

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED as a typed pure seam

SNE-7.4  production Compose/App wiring consumes typed seams
         PARTIAL / CURRENT FUNCTIONAL FRONTIER

  SNE-7.4A  Poison production reducer wiring
             COMPLETE / FOCUSED GREEN / REMOTE AUDITED

  SNE-7.4B  Monk production reducer wiring
             NEXT

  SNE-7.4C  Demon attack
             NOT STARTED

  SNE-7.4D  Mayor redirect
             NOT STARTED

  SNE-7.4E  Demon successor
             NOT STARTED

  SNE-7.4F  Dawn planner authority closeout
             NOT COMPLETE

SNE-7.5  restore / process-death reconstruction matrix
         SCAFFOLD EXISTS, NOT GREEN / NOT COMPLETE

SNE-7.6  small Compose smoke/integration set
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS; CI #803 stale assertion failures cleaned up

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

### Accepted 7.4A evidence

```text
09bea7ffc028833d3c893d740a5e9b6f90919bf6
  RED: ClocktowerPoisonReducerProductionWiringTest

CI #809 at RED head
  882 tests
  exactly 2 expected SNE-7.4A failures
  previous CI #803 four stale source failures absent
  R2 #736 SUCCESS

db2a3746cedc2b667b0e5abd20e722ba8866263b
  production Poison reducer cut-over

e34598d60c012b6cb7c60e0e19da22b4483c600b
  formatting-only follow-up
```

The production and formatting commits each passed exact-head guard, single-file scope audit, `git diff --check`, focused `--rerun-tasks` validation, and remote-head recheck before push.

## 3. Authority model

The authoritative model remains:

```text
Durable unfinished-night state
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

1. `ClocktowerNightCheckpoint` remains the sole durable unfinished-night checkpoint state.
2. `GameState` and the existing `ClocktowerGameSession` timeline remain durable game-history authority.
3. No second persisted coordinator state is introduced.
4. `NightResolutionEvent` is a transient command only; it is not a durable event log and does not introduce event sourcing.
5. Restore reconstructs from checkpoint + game state + ruleset/canonical plan, not from replaying UI commands.
6. Compose callbacks may orchestrate durable side effects, but must stop duplicating checkpoint transition semantics once the corresponding reducer seam is cut over.
7. A shared App snapshot helper may project current App fields into `ClocktowerNightCheckpoint`; it is not a second state owner.

## 4. Navigation, draft, confirmation, and invalidation

`ClocktowerNightCheckpoint.nightStepIndex` remains the sole stored navigation position.

Protected lifecycle:

```text
confirmed A
→ MovePrevious
→ confirmed A remains authoritative

Edit draft to B
→ confirmed A remains authoritative

Confirm B
→ if B != A, authoritative upstream fact changes
→ only then invalidate dependent downstream confirmed facts
```

Therefore:

- navigation alone never invalidates confirmed mechanics;
- editing a draft alone never invalidates confirmed mechanics;
- changed reconfirmation is the invalidation boundary;
- restore never promotes draft state into confirmed mechanical fact;
- invalidating a dependent confirmed fact does not imply deleting its editable draft.

`NightCheckpointReducer` already owns typed semantics for Poison, Monk protection, Demon attack, Mayor redirect, Demon successor, and Previous navigation. Production callbacks must converge on those semantics instead of retaining parallel handwritten transition logic.

## 5. Established typed seams

### 5.1 `NightCheckpointReducer`

Current reducer responsibilities include:

```text
EditPoisonDraft / ConfirmPoison
EditMonkProtectionDraft / ConfirmMonkProtection
EditDemonAttackDraft / ConfirmDemonAttack
EditMayorRedirectDraft / ConfirmMayorRedirect
EditDemonSuccessorDraft / ConfirmDemonSuccessor
MovePrevious
```

The reducer returns a replacement `ClocktowerNightCheckpoint`. It does not allocate global sequence numbers and does not commit durable timeline/history facts.

### 5.2 `NightDawnResolutionPlanner`

The planner owns typed pure planning for:

- Demon succession continuation;
- new-Demon identity confirmation;
- Dawn role-change intent;
- validated night death / Mayor redirect resolution;
- poison carry/lifetime handling;
- outcome-evaluation gating.

It remains pure and returns checkpoint/continuation/commit intent rather than mutating durable game state.

### 5.3 `ClocktowerEffectiveNightStateProjector`

The projector remains the derived same-night mechanical authority for:

- effective alive state;
- effective current role;
- same-night chronology;
- projected mechanical death;
- projected `RoleChanged` facts.

A role change must not mutate public/base role state early merely to make later-night mechanics work.

## 6. SNE-7.4 production migration

SNE-7.4 is complete only when production Compose/App wiring consumes the typed seams and stops independently owning the same transition semantics.

### 6.1 SNE-7.4A Poison — accepted

Production now follows:

```text
onSelectPoisonTarget
  → NightResolutionEvent.EditPoisonDraft
  → NightCheckpointReducer.reduce(...)
  → project reduced poisonDraftTarget

onConfirmPoisonTarget
  → NightResolutionEvent.ConfirmPoison
  → NightCheckpointReducer.reduce(...)
  → project reduced confirmedPoisonTarget
  → project reduced confirmedDemonSuccessorTarget
```

The reducer owns checkpoint-local Poison transition semantics.

The App/session boundary still owns:

```text
local/global sequence coordination
ActionFactDraft.Poison durable recording
game-state revision
other timeline/history side effects
```

The old production callback that manually set `confirmedDemonSuccessorTarget = null` and also erased the editable successor draft has been replaced by reducer output. Changed Poison reconfirmation invalidates only the dependent confirmed successor fact according to reducer semantics.

`currentClocktowerNightCheckpoint()` now provides a single reusable App snapshot projection for this and later 7.4 slices. It does not persist or independently own state.

### 6.2 SNE-7.4B Monk — next

Required target flow:

```text
onSelectMonkProtectedTarget
  → NightResolutionEvent.EditMonkProtectionDraft
  → NightCheckpointReducer.reduce(...)

onConfirmMonkProtectedTarget
  → NightResolutionEvent.ConfirmMonkProtection
  → NightCheckpointReducer.reduce(...)
```

Acceptance criteria:

1. RED first at the smallest practical application ownership boundary.
2. Monk draft edit does not alter confirmed Monk protection.
3. Monk draft edit does not invalidate confirmed successor mechanics.
4. Confirming unchanged Monk protection preserves dependent successor confirmation.
5. Confirming changed Monk protection commits the draft and invalidates the dependent confirmed successor.
6. Do not erase the editable successor draft merely because the upstream confirmed Monk fact changed.
7. Existing durable Monk event/timeline side effects remain exactly once outside `NightCheckpointReducer`.
8. Reuse `currentClocktowerNightCheckpoint()`; do not introduce another durable/snapshot owner.
9. Focused T0 GREEN and `git diff --check` before proceeding to Demon attack.

Expected follow-on slices:

```text
SNE-7.4B Monk
SNE-7.4C Demon attack
SNE-7.4D Mayor redirect
SNE-7.4E Demon successor
SNE-7.4F Dawn planner authority closeout
```

Do not combine these into a broad App rewrite unless a later audit proves a smaller cut unsafe.

## 7. Restore and reconstruction — SNE-7.5

`NightTransactionReconstructor` and its contract test exist, but the current implementation remains a scaffold. It must not be treated as completed merely because the types/files exist.

SNE-7.5 must exercise a real reconstruction boundary:

```text
checkpoint encode/save
→ process-death / lifecycle boundary as applicable
→ checkpoint decode
→ ruleset + canonical interaction plan rebuild
→ derived effective state reconstruction
```

Required restore cases remain:

- legacy draft-only successor data does not invent confirmation or `RoleChanged`;
- invalid confirmed successor fails closed / requires valid reconfirmation;
- missing interaction is handled safely;
- out-of-range `nightStepIndex` is handled safely;
- stale Mayor redirect to Demon fails closed;
- current effective role may differ from public/base role and reconstructs correctly;
- confirmed successor + Previous remains confirmed;
- draft edit without Confirm leaves the old confirmed fact authoritative;
- same durable inputs reconstruct the same effective state.

Do not activate the ignored reconstruction matrix by faking role changes or replaying UI commands. The reconstructor must derive from durable state plus canonical semantics.

## 8. Source-string retirement

Source inspection is not the primary correctness layer.

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal source ownership guard only when runtime proof is impractical
```

The four CI #803 implementation-shape failures have been cleaned up. They are no longer an active gate.

For every source-string assertion removed or narrowed, identify the behavioral or typed contract that replaces it. Retain coarse source guards only for genuine ownership boundaries, not local expression shape.

The SNE-7.4A App source ownership test is explicitly temporary until a directly callable production integration seam can supersede it.

## 9. Validation cadence

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`:

```text
micro-slice
  T0 RED
  exact production patch
  T0 GREEN --rerun-tasks
  git diff --check
  commit + push
  remote exact diff audit

logical checkpoint
  :app:testFast + triggered T2/T3
  latest-head GitHub CI/R2
```

Do not wait for old-head CI after every micro-slice. Do not treat a failing obsolete source assertion as a reason to preserve implementation shape.

## 10. Updated implementation order

```text
COMPLETED
  source-string cleanup exposed by CI #803
  SNE-7.4A Poison production reducer wiring

CURRENT NEXT
  SNE-7.4B Monk production reducer wiring

THEN
  SNE-7.4C Demon attack production reducer wiring
  SNE-7.4D Mayor redirect production reducer wiring
  SNE-7.4E Demon successor production reducer wiring
  SNE-7.4F Dawn planner production authority closeout

  SNE-7.5 restore/process-death reconstruction matrix GREEN
  SNE-7.6 2–4 Compose smoke/integration tests
  SNE-7.7 finish source-string retirement
  SNE-7.8 retain only minimal architecture guards

→ logical checkpoint validation
→ exact campaign audit
→ PR remains draft until explicit user authorization
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
- mutate public death/role early to simulate same-night mechanics;
- restore obsolete source-string tests merely to recover coverage count;
- merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
