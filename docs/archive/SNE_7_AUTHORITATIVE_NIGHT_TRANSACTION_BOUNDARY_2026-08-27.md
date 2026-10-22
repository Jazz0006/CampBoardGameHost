# SNE-7 — Authoritative Night Transaction Boundary

> Date: 2026-08-27  
> Scope: same-night correctness closeout  
> Status: **IMPLEMENTATION COMPLETE — SNE-7.1–7.8 ESTABLISHED / final latest-head broad validation pending**
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
         COMPLETE / FOCUSED + BROAD GREEN / REMOTE AUDITED

  SNE-7.4A  Poison production reducer wiring
             COMPLETE / FOCUSED + BROAD GREEN / REMOTE AUDITED

  SNE-7.4B  Monk production reducer wiring
             COMPLETE / FOCUSED + BROAD GREEN / REMOTE AUDITED

  SNE-7.4C  Demon attack production reducer wiring
             COMPLETE / FOCUSED GREEN / REMOTE AUDITED

  SNE-7.4D  Mayor redirect production reducer wiring
             COMPLETE / FOCUSED GREEN / REMOTE AUDITED

  SNE-7.4E  Demon successor production reducer wiring
             COMPLETE / FOCUSED GREEN / REMOTE AUDITED

  SNE-7.4F  Dawn planner authority closeout
             COMPLETE / FOCUSED + BROAD GREEN / REMOTE AUDITED

SNE-7.5  restore / process-death reconstruction matrix
         COMPLETE / FOCUSED + BROAD GREEN / REMOTE AUDITED

SNE-7.6  limited integration smoke set
         COMPLETE / JVM-CALLABLE HOST LIFECYCLE GREEN

SNE-7.7  source-string retirement
         COMPLETE / SUPERSEDED TEMPORARY WIRING TESTS RETIRED

SNE-7.8  minimal architecture guards only
         COMPLETE / COARSE OWNERSHIP GUARD GREEN
```

### Accepted 7.4 evidence

```text
SNE-7.4A Poison
  RED      09bea7ffc028833d3c893d740a5e9b6f90919bf6
  GREEN    db2a3746cedc2b667b0e5abd20e722ba8866263b
  format   e34598d60c012b6cb7c60e0e19da22b4483c600b
  broad    CI #814 + R2 #741 SUCCESS

SNE-7.4B Monk
  RED      6deb9d42f1b8ce5dfa1ca999778c22a49f714a91
  GREEN    b1679f1b648e0de1d1aabaadb59715e53f9843f9
  broad    CI #817 + R2 #744 SUCCESS

SNE-7.4C Demon attack
  RED      0ea9d0b4c46dd69a0672a0c3fdc600d6e52dbe3d
  GREEN    062e000afad1c407ba17ad7cef915dae0c487b30

SNE-7.4D Mayor redirect
  RED      35659899745077f4f43cf914faa2bbf82eef3afa
  GREEN    21a7694ee340364485a283598cf7c2fa6fe2ae94

SNE-7.4E Demon successor
  RED      fac430f4b40a219fcd92d91a6f45dacc2e89cc2b
  GREEN    034b050c1656324766c1df3d2fbcd170af201389

SNE-7.4F Dawn planner authority closeout
  F-1 RED  c7b76ca4ca131da36f49634a081bbd9f47ab12bd
  F-1 GREEN 508b82a29054c2a89b402bce2605734bea307c7b
  broad    CI #835 + R2 #762 SUCCESS
  F-2 RED  ce15d84d819d400ae481d47c9a36c4cefac43962
  F-2 GREEN 6188978b96059d176fe1647f7bd8d068237a0d6f
  broad    CI #839 + R2 #766 SUCCESS
  poison RED 84643d5bb12583ad65f688cae3215a70df9efa2c
  poison GREEN b4bf9379db4de3f8fb7dc152fd93db088f857df0
  broad    CI #842 + R2 #769 SUCCESS
```

RED evidence for 7.4E:

```text
CI #828 at fac430f4
  894 tests
  exactly 2 failures
  both are intended ClocktowerDemonSuccessorReducerProductionWiringTest REDs
  4 skipped
  Real Clingo SUCCESS
  R2 #755 SUCCESS
```

7.4E focused GREEN passed with `--rerun-tasks` for the successor production ownership guard, `ClocktowerDawnExactDemonSuccessorWiringTest`, `ClocktowerNewDemonPresentationOwnershipTest`, the prior Demon attack ownership guard, `NightCheckpointReducerTest`, and `SNE7NightTransactionBehaviorMatrixTest`. The patch also passed exact-head guard, exact patch preconditions, `git diff --check`, single-production-file scope audit, and remote-head recheck before push.

Remote RED→GREEN compare for 7.4E is exactly one commit and one production file:

```text
fac430f4 → 034b050c
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
14 additions / 5 deletions
```

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

`NightCheckpointReducer` owns typed semantics for Poison, Monk protection, Demon attack, Mayor redirect, Demon successor, and Previous navigation. Production callbacks for all five decision families now consume those transitions.

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

The planner owns typed pure planning for Demon succession continuation, new-Demon identity confirmation, Dawn role-change intent, validated night death / Mayor redirect resolution, poison carry/lifetime handling, and outcome-evaluation gating.

It remains pure and returns checkpoint/continuation/commit intent rather than mutating durable game state.

### 5.3 `ClocktowerEffectiveNightStateProjector`

The projector remains the derived same-night mechanical authority for effective alive state, effective current role, chronology, projected mechanical death, and projected `RoleChanged` facts.

A role change must not mutate public/base role state early merely to make later-night mechanics work.

## 6. SNE-7.4 production migration

### 6.1 SNE-7.4A Poison — accepted

```text
onSelectPoisonTarget
  → EditPoisonDraft
  → NightCheckpointReducer.reduce(...)
  → project poisonDraftTarget

onConfirmPoisonTarget
  → ConfirmPoison
  → NightCheckpointReducer.reduce(...)
  → project confirmedPoisonTarget
  → project confirmedDemonSuccessorTarget
```

### 6.2 SNE-7.4B Monk — accepted

```text
onSelectMonkProtectedTarget
  → EditMonkProtectionDraft
  → NightCheckpointReducer.reduce(...)
  → project monkDraftTarget

onConfirmMonkProtectedTarget
  → ConfirmMonkProtection
  → NightCheckpointReducer.reduce(...)
  → project confirmedMonkTarget
  → project confirmedDemonSuccessorTarget
```

### 6.3 SNE-7.4C Demon attack — accepted

```text
onSelectNightDeath
  → EditDemonAttackDraft
  → NightCheckpointReducer.reduce(...)
  → project attackDraftTarget

onConfirmDemonAttack
  → ConfirmDemonAttack
  → NightCheckpointReducer.reduce(...)
  → project confirmedAttackTarget
  → project confirmedDemonSuccessorTarget
```

Draft editing no longer invalidates successor state, and changed attack reconfirmation invalidates only the dependent confirmed successor while preserving the editable successor draft.

### 6.4 SNE-7.4D Mayor redirect — accepted

```text
onSelectMayorRedirectTarget
  → EditMayorRedirectDraft
  → NightCheckpointReducer.reduce(...)
  → project mayorRedirectDraftTarget

onConfirmMayorRedirectTarget
  → ConfirmMayorRedirect
  → NightCheckpointReducer.reduce(...)
  → project confirmedMayorRedirectTarget
```

Mayor redirect legality is deliberately not moved into the reducer transition. The existing typed Host/rules/UI boundary still owns the current Trouble Brewing product restriction that the redirect target cannot be the current Demon.

### 6.5 SNE-7.4E Demon successor — accepted

```text
onSelectDemonSuccessor
  → EditDemonSuccessorDraft
  → NightCheckpointReducer.reduce(...)
  → project demonSuccessorDraftTarget

onConfirmDemonSuccessorTarget
  → ConfirmDemonSuccessor
  → NightCheckpointReducer.reduce(...)
  → project confirmedDemonSuccessorTarget
```

Successor draft editing leaves an existing confirmed successor mechanically authoritative until explicit Confirm. Production confirm no longer trusts the transient callback `selectedTarget` as independent authority; it commits the checkpoint's current draft through the reducer.

Existing Dawn contracts remain intact:

```text
exact confirmed successor only
no successor draft fallback
missing required confirmation fails closed
same-night RoleChanged remains projected before public/base-role materialization
```

For 7.4A–E, the App/session boundary still owns sequence allocation where applicable, durable `ActionFactDraft` recording, player/game-state revision, and other timeline/history side effects exactly once.

`currentClocktowerNightCheckpoint()` is the shared App snapshot projection for these slices. It does not persist or independently own state.

### 6.6 SNE-7.4F Dawn planner authority closeout — accepted

7.4F completed three narrow authority cuts without moving durable commit ownership out of App/session:

```text
onConfirmNight
  → NightDawnResolutionPlanner.planValidatedNightDeath(...)
  → planner-validated death / Mayor redirect intent
  → App materializes durable/public consequences

onConfirmNewDemon
  → currentClocktowerNightCheckpoint() canonical snapshot
  → NightDawnResolutionPlanner.confirmNewDemonIdentity(...)
  → DawnCommitIntent roleChanges + poisonCarry
  → App records/materializes durable consequences exactly once
```

Planner-backed succession no longer re-runs `PoisonEffectLifecycle.afterNight()` after consuming `DawnCommitIntent.poisonCarry`; that legacy lifecycle remains only on the non-planner path. The final cleanup audit found only idempotent successor-field clearing with no extra revision, timeline or mechanical side effect, so no further 7.4F production slice is warranted.

SNE-7.4 is therefore complete. Durable sequence allocation, action/history recording, public role/death materialization and phase/revision changes remain App/session-owned.

## 7. Restore and reconstruction — SNE-7.5 accepted

SNE-7.5 is complete and broad GREEN at code/test checkpoint `1136dbab` (CI #855 + R2 #782). Reconstruction derives effective same-night state from durable checkpoint + base `GameState` + canonical plan; it never replays transient UI commands and never mutates public/base role or alive state early.

Accepted reconstruction semantics:

- draft-only successor data does not invent confirmation or `RoleChanged`;
- missing successor interaction and out-of-range `nightStepIndex` fail closed;
- invalid/non-living/non-Minion confirmed successor fails closed;
- confirmed succession requires a confirmed old-Demon self attack;
- UI `nightStepIndex` remains navigation only; `MovePrevious` cannot roll back confirmed mechanics;
- editing a draft without Confirm leaves the prior confirmed fact authoritative;
- confirmed self-kill succession reconstructs old-Demon `MechanicalDeath` at successor `BEFORE` and successor `RoleChanged` at `AFTER`;
- current effective alive/role may differ from public/base state without early durable mutation;
- stale Mayor redirect to a reconstructed current Demon fails closed through the existing Dawn planner legality seam;
- identical durable inputs reconstruct identical effective state.

The mechanical projection cursor used to apply already-confirmed reconstructed facts is therefore not the same thing as the stored UI navigation cursor. This is required by the protected lifecycle in section 4.

Do not turn the reconstructor into a second attack-resolution engine. `confirmedAttackTarget` alone is not sufficient to infer ordinary death because Monk protection, Soldier immunity and Mayor redirect semantics remain validated by existing rule/planner seams.

## 7.1 SNE-7.6–7.8 accepted closeout

`NightCheckpointHostTransaction` is the minimal JVM-callable Host transaction adapter. It delegates to `NightCheckpointReducer`, returns only reduced checkpoint + revision intent, and owns no sequence/timeline/public materialization. Real App successor callbacks and all Host Previous navigation paths consume it.

The lifecycle integration smoke composes that adapter with checkpoint persistence/restoration, `NightTransactionReconstructor`, and `NightDawnResolutionPlanner`; no second coordinator was introduced. Fine-grained temporary SNE wiring tests were retired after the typed replacements and the minimal architecture guard passed focused validation. Implementation checkpoint `5686a5e4` was broad GREEN at CI #862 + R2 #789; retirement checkpoint `70ddd4f9` changes tests only.

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

The SNE-7.4 App source ownership tests are explicitly temporary until a directly callable production integration seam can supersede them.

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
  SNE-7.4B Monk production reducer wiring
  SNE-7.4C Demon attack production reducer wiring
  SNE-7.4D Mayor redirect production reducer wiring
  SNE-7.4E Demon successor production reducer wiring
  SNE-7.4F Dawn planner production authority closeout
  SNE-7.5A–G restore/reconstruction matrix

CURRENT NEXT
  SNE-7.6 2–4 JVM-callable Host/App integration smokes

THEN
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
