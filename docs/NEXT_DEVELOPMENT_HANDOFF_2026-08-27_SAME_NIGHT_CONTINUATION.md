# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Handoff status: **SNE-7.4 active — Poison + Monk + Demon attack cut-overs complete; Mayor redirect is next**

## 1. Startup contract

Before changing code, read:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, branch head, PR #54 state/head and checks. Do not assume any SHA below remains live after further work.

## 2. Stable baseline and active PR

`main` remained:

```text
c8985cb4991f6c7e5ea02adedb932d2d86452da1
```

PR #54 remains:

```text
open
draft
not merged
```

Do not merge, mark ready, rebase, force-push, or broaden it without explicit user authorization.

## 3. Current SNE-7 progress

```text
SNE-7.1  behavior-first transaction matrix
         ESTABLISHED

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED

SNE-7.4  production Compose/App typed-seam cutover
         PARTIAL / CURRENT FRONTIER

  7.4A Poison            COMPLETE / FOCUSED + BROAD GREEN
  7.4B Monk              COMPLETE / FOCUSED + BROAD GREEN
  7.4C Demon attack      COMPLETE / FOCUSED GREEN / REMOTE AUDITED
  7.4D Mayor redirect    NEXT
  7.4E Demon successor   NOT STARTED
  7.4F Dawn planner      NOT COMPLETE

SNE-7.5  NightTransactionReconstructor
         SCAFFOLD EXISTS / INCOMPLETE

SNE-7.6  small Compose integration/smoke set
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS; CI #803 stale failures cleaned up

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

## 4. Accepted SNE-7.4 checkpoints

### 4.1 Poison

```text
09bea7ffc028833d3c893d740a5e9b6f90919bf6
  RED: ClocktowerPoisonReducerProductionWiringTest

db2a3746cedc2b667b0e5abd20e722ba8866263b
  production Poison reducer cut-over

e34598d60c012b6cb7c60e0e19da22b4483c600b
  formatting-only follow-up

CI #814
  Android full tests + assembleDebug SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
  R2 #741 SUCCESS
```

### 4.2 Monk

```text
6deb9d42f1b8ce5dfa1ca999778c22a49f714a91
  RED: ClocktowerMonkReducerProductionWiringTest

CI #815 at RED head
  885 tests
  exactly 2 failures, both intended Monk wiring REDs
  4 skipped
  Real Clingo SUCCESS
  R2 #742 SUCCESS

b1679f1b648e0de1d1aabaadb59715e53f9843f9
  production Monk reducer cut-over

CI #817 after normal documentation checkpoint
  Android full tests + assembleDebug SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
  R2 #744 SUCCESS
```

### 4.3 Demon attack

```text
0ea9d0b4c46dd69a0672a0c3fdc600d6e52dbe3d
  RED: ClocktowerDemonAttackReducerProductionWiringTest

CI #818 at RED head
  888 tests
  exactly 2 failures, both intended Demon attack wiring REDs
  4 skipped
  Real Clingo SUCCESS
  R2 #745 SUCCESS

062e000afad1c407ba17ad7cef915dae0c487b30
  production: route Demon attack checkpoint transitions through reducer
```

The 7.4C RED→GREEN remote compare is exactly one commit and one production file:

```text
0ea9d0b4 → 062e000a
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
14 additions / 12 deletions
```

Focused 7.4C validation passed with `--rerun-tasks` for:

```text
ClocktowerDemonAttackReducerProductionWiringTest
ClocktowerMonkReducerProductionWiringTest
ClocktowerPoisonReducerProductionWiringTest
NightCheckpointReducerTest
SNE7NightTransactionBehaviorMatrixTest
```

It also passed exact target-head guard, exact patch preconditions, `git diff --check`, single-production-file scope audit, and remote-head recheck before push.

## 5. Accepted authority split through 7.4C

Production now follows the typed checkpoint seam for Poison, Monk and Demon attack:

```text
Edit draft callback
  → NightResolutionEvent.Edit...
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced draft field back to App state

Confirm callback
  → NightResolutionEvent.Confirm...
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced confirmed field
  → project any reducer-owned dependent confirmed-successor invalidation
```

`NightCheckpointReducer` owns checkpoint-local transition/invalidation semantics.

The App/session transaction boundary still owns:

```text
sequence allocation
ActionFactDraft.Poison / Protect / Attack durable recording
game-state revision
other durable timeline/history side effects
```

For Poison, Monk and Demon attack, draft editing does not invalidate confirmed mechanics. A changed upstream reconfirmation invalidates only the dependent confirmed Demon successor fact. The editable successor draft is preserved.

The old Demon attack draft callback also used to clear successor draft whenever the selected attack target was not the current Demon. That handwritten cross-mechanic draft cleanup was removed in 7.4C so production now follows the reducer contract consistently.

Reuse the existing `currentClocktowerNightCheckpoint()` helper. Do not add another durable/snapshot state owner.

## 6. Exact next functional slice — SNE-7.4D Mayor redirect

Continue tests-first with only Mayor redirect draft/confirmation production wiring.

Audit the live callbacks before editing. Expected target typed flow:

```text
onSelectMayorRedirectTarget
  → NightResolutionEvent.EditMayorRedirectDraft(target)
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced mayorRedirectDraftTarget

onConfirmMayorRedirectTarget
  → NightResolutionEvent.ConfirmMayorRedirect
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced confirmedMayorRedirectTarget
```

### SNE-7.4D acceptance criteria

1. Establish RED first at the smallest practical production ownership/application boundary.
2. Mayor redirect draft editing leaves confirmed redirect unchanged.
3. Confirming the redirect commits exactly the reducer's current draft.
4. Preserve the existing product restriction that Mayor redirect cannot target the current Demon; do not reopen generic non-self Demon-death succession.
5. Do not move target-legality authority into recommendations or into the reducer transition itself if legality already belongs to the existing typed Host/rules seam.
6. Preserve any existing durable timeline/game-state side effects exactly once at App/session authority.
7. Reuse `currentClocktowerNightCheckpoint()`.
8. No new durable coordinator/checkpoint/navigation state.
9. Focused T0 RED/GREEN with `--rerun-tasks`, `git diff --check`, then remote parent/diff audit.
10. Stop before 7.4E Demon successor until Mayor redirect is fully accepted.

## 7. Large-file writer constraint

`CampBoardGameHostApp.kt` remains a large/truncated production file.

Under root `AGENTS.md`:

- Chat/GitHub connector is appropriate for tests/docs/small files;
- large production edits require a complete worktree and exact diff validation.

The proven 7.4A–C pattern is:

```text
Chat
  audit live code
  author RED / small-file contract
  define exact narrow production patch
  inspect remote parent/diff/scope

complete-worktree executor
  verify exact target head
  apply exact localized App patch
  git diff --check
  run focused --rerun-tasks tests
  recheck remote head
  commit + push only after GREEN
```

A one-shot temporary GitHub Actions runner branch outside PR #54 has been used because the current container cannot safely edit the complete App file. It is not production authority and must never be added to the PR branch.

## 8. SNE-7.5 remains future work

`NightTransactionReconstructor` exists but remains a scaffold. Do not jump there before production transaction authority is sufficiently cut over.

Required later reconstruction boundary:

```text
checkpoint encode/save
→ lifecycle/process boundary
→ checkpoint decode
→ ruleset + canonical plan rebuild
→ derived effective-state reconstruction
```

Still required:

- legacy draft-only successor invents no confirmation/`RoleChanged`;
- invalid confirmed successor fails closed;
- missing interaction handled safely;
- out-of-range `nightStepIndex` handled safely;
- stale Mayor→Demon redirect fails closed;
- effective role can differ from public/base role;
- Previous preserves confirmation;
- draft edit without Confirm preserves old mechanical fact;
- same durable inputs reconstruct same state.

Do not reconstruct by replaying transient `NightResolutionEvent` commands.

## 9. Mayor product restriction remains unchanged

For current Trouble Brewing automatic-host production:

```text
Mayor redirect target cannot be the current Demon.
```

This is an intentional product/house-rule restriction. Generic non-self Demon death / Scarlet Woman succession remains deferred and must not be reopened during SNE-7.4.

## 10. Validation cadence and do-not-do list

Follow `AGENTS.md`:

```text
micro-slice
  T0 RED
  exact production patch
  T0 GREEN --rerun-tasks
  git diff --check
  push
  Chat remote parent/diff/scope audit

logical checkpoint
  :app:testFast + triggered T2/T3
  latest-head GitHub CI/R2
```

Do not wait for an old-head CI after every related micro-slice.

Do not:

- merge or mark PR #54 ready;
- rebase or force-push;
- reopen A3;
- resume App-root decomposition;
- broaden to arbitrary custom-script Demon death;
- introduce event sourcing;
- create a second durable night/navigation state owner;
- move timeline/global-sequence ownership out of `ClocktowerGameSession`;
- let draft edits/navigation invalidate confirmed mechanics;
- let recommendations define legality;
- mutate public role/death state early to fake effective same-night state;
- preserve obsolete source strings by distorting production implementation;
- activate reconstruction tests with fake/replayed UI-command semantics.

## 11. Exact next-start instruction

At the next continuation point:

```text
1. re-query live PR #54 head and checks;
2. inspect live Mayor redirect select/confirm callbacks and existing legality seam;
3. establish SNE-7.4D Mayor redirect reducer production-wiring RED;
4. prove the RED fails only on missing Mayor redirect reducer cut-over;
5. cut only Mayor redirect draft/confirm transition ownership to NightCheckpointReducer;
6. preserve current Demon-exclusion legality and durable side-effect ownership;
7. focused GREEN + diff check + remote parent/diff audit;
8. stop before Demon successor unless Mayor redirect is fully accepted.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
