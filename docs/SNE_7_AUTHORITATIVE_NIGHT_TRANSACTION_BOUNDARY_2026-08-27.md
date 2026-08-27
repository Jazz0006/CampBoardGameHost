# SNE-7 — Authoritative Night Transaction Boundary

> Date: 2026-08-27  
> Scope: same-night correctness closeout  
> Status: **IMPLEMENTATION IN PROGRESS — 7.1–7.3 ESTABLISHED / 7.4 CURRENT FRONTIER**  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54

## 1. Purpose

SNE-7 is not a generic architecture rewrite and does not reopen A3. Its purpose is to replace brittle implementation-shaped wiring checks with executable behavioral contracts at the real night-transaction boundary while preserving the existing persistence and timeline authorities.

The design is accepted and implementation has advanced materially beyond the original handoff. The current work is no longer SNE-7.1 design-only work.

## 2. Live implementation status

As of the live PR #54 audit on 2026-08-27:

```text
SNE-7.1  behavior-first transaction matrix
         ESTABLISHED / GREEN coverage exists

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED as a typed pure seam

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED as a typed pure seam

SNE-7.4  production Compose/App wiring consumes typed seams
         PARTIAL / CURRENT FUNCTIONAL FRONTIER

SNE-7.5  restore / process-death reconstruction matrix
         SCAFFOLD EXISTS, NOT GREEN / NOT COMPLETE

SNE-7.6  small Compose smoke/integration set
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS; cleanup started before 7.4 is fully closed

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

Known live head before the documentation refresh:

```text
2aa528dbb898313c51b1a7fb06d11a60c883b84f
  test: remove low-value same-night wiring assertions
```

At that head:

```text
R2 #730  SUCCESS
CI  #803  FAILURE
```

CI #803 completed 879 tests with 4 failures, all in legacy source-inspection tests:

- one assertion in `ClocktowerSameNightEffectiveStateProductionWiringTest`;
- three assertions in `ClocktowerProductionOtherNightWiringTest`.

These failures are a test-debt cleanup gate, not authorization to reshape correct production code around obsolete source strings. Apply `AGENTS.md` and `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`: retire or narrow a source assertion only after identifying the typed/behavioral contract that supersedes it.

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

## 4. Navigation, draft, confirmation, and invalidation

`ClocktowerNightCheckpoint.nightStepIndex` remains the sole stored navigation position. Do not add another interaction index.

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
- restore never promotes draft state into confirmed mechanical fact.

`NightCheckpointReducer` already owns the typed semantics for Poison, Monk protection, Demon attack, Mayor redirect, Demon successor, and Previous navigation. Production callbacks must converge on those semantics instead of retaining parallel handwritten transition logic.

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

The planner now owns typed pure planning for:

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

## 6. Current production migration frontier — SNE-7.4

SNE-7.4 is complete only when production Compose/App wiring consumes the typed seams and stops independently owning the same transition semantics.

The live audit shows partial migration:

- `NightDawnResolutionPlanner.confirmNewDemonIdentity(...)` is already consumed from App production wiring;
- other planner/reducer responsibilities remain partially duplicated in Compose/App callbacks;
- in particular Poison draft/confirm callbacks still directly mutate draft/confirmed poison and dependent successor state.

The next narrow functional slice is therefore:

```text
SNE-7.4A — Poison production reducer wiring
```

Required behavior:

```text
onSelectPoisonTarget
  → NightResolutionEvent.EditPoisonDraft
  → NightCheckpointReducer.reduce(...)

onConfirmPoisonTarget
  → NightResolutionEvent.ConfirmPoison
  → NightCheckpointReducer.reduce(...)
```

The reducer owns checkpoint-local transition semantics. Existing App/session code may still own durable side effects that are outside reducer scope, including sequence/timeline/history commitments and revision/phase orchestration.

### SNE-7.4A acceptance criteria

1. RED first at the smallest callable typed application boundary that can prove the migration.
2. Poison draft editing does not alter the confirmed poison fact.
3. Poison draft editing does not invalidate confirmed successor mechanics.
4. Confirming an unchanged poison fact preserves dependent successor confirmation.
5. Confirming a changed poison fact commits the draft and invalidates the dependent successor confirmation.
6. Existing raw event/timeline side effects are preserved exactly once and remain outside `NightCheckpointReducer`.
7. No second durable night-state owner is introduced.
8. Focused T0 GREEN and `git diff --check` before proceeding to Monk/attack/Mayor/successor migration.

Expected follow-on slices:

```text
SNE-7.4A Poison
SNE-7.4B Monk
SNE-7.4C Demon attack
SNE-7.4D Mayor redirect
SNE-7.4E Demon successor
SNE-7.4F Dawn planner authority closeout
```

Do not combine these into a broad App rewrite unless a later audit proves that a smaller cut is unsafe.

## 7. Restore and reconstruction — SNE-7.5

`NightTransactionReconstructor` and its contract test now exist, but the current implementation is still a scaffold. It must not be treated as completed merely because the types/files exist.

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

For every source-string assertion removed, identify the behavioral or typed contract that replaces it.

Current immediate cleanup gate from CI #803:

- do not modify production formatting/local variable spelling to satisfy the four failing assertions;
- remove or narrow only those assertions whose semantics are already covered elsewhere;
- if a unique behavior is not covered, add the smallest typed behavior test first.

Retain coarse source guards only for genuine ownership boundaries, not local expression shape.

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

The live sequence is now:

```text
CURRENT GATE
  finish source-string cleanup exposed by CI #803
  restore latest head to a trustworthy green baseline

SNE-7.4A  Poison production reducer wiring
SNE-7.4B  Monk production reducer wiring
SNE-7.4C  Demon attack production reducer wiring
SNE-7.4D  Mayor redirect production reducer wiring
SNE-7.4E  Demon successor production reducer wiring
SNE-7.4F  Dawn planner production authority closeout

SNE-7.5   restore/process-death reconstruction matrix GREEN
SNE-7.6   2–4 Compose smoke/integration tests
SNE-7.7   finish source-string retirement
SNE-7.8   retain only minimal architecture guards

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
