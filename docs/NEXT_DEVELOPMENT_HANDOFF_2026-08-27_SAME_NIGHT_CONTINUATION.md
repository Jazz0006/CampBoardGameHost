# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Handoff status: **SNE-7.4 active — Poison cut-over complete; Monk is next**

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

  7.4A Poison            COMPLETE / FOCUSED GREEN
  7.4B Monk              NEXT
  7.4C Demon attack      NOT STARTED
  7.4D Mayor redirect    NOT STARTED
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

## 4. SNE-7.4A Poison — completed checkpoint

The stale CI #803 source-wiring failures were first cleaned up without reshaping correct production code.

Checkpoint chain:

```text
70bd9fbe37ac0286428e34497b027661fd7dd511
  hardened remaining source ownership guards

09bea7ffc028833d3c893d740a5e9b6f90919bf6
  RED: ClocktowerPoisonReducerProductionWiringTest

CI #809 at RED head
  882 tests
  exactly 2 failures, both intended new Poison reducer REDs
  previous four CI #803 failures absent
  R2 #736 SUCCESS

db2a3746cedc2b667b0e5abd20e722ba8866263b
  refactor: route Poison checkpoint transitions through reducer

e34598d60c012b6cb7c60e0e19da22b4483c600b
  style: align Poison reducer callback wiring
```

The production and formatting work were executed in complete GitHub worktrees with exact-head guards. Both passed:

```text
git diff --check
single-production-file scope audit
ClocktowerPoisonReducerProductionWiringTest
NightCheckpointReducerTest
SNE7NightTransactionBehaviorMatrixTest
--rerun-tasks
remote-head recheck before push
```

### Accepted Poison authority split

Production now behaves as:

```text
onSelectPoisonTarget
  → NightResolutionEvent.EditPoisonDraft
  → NightCheckpointReducer.reduce
  → project reduced poisonDraftTarget back to App state

onConfirmPoisonTarget
  → NightResolutionEvent.ConfirmPoison
  → NightCheckpointReducer.reduce
  → project reduced confirmed poison and dependent confirmed successor back to App state
```

`NightCheckpointReducer` owns checkpoint-local transition semantics.

The App/session transaction boundary still owns:

```text
sequence allocation
ActionFactDraft.Poison durable recording
game-state revision
other durable timeline/history side effects
```

Changing a confirmed upstream Poison fact invalidates the dependent confirmed successor but no longer erases the editable successor draft.

A shared `currentClocktowerNightCheckpoint()` snapshot helper now exists in App and should be reused by later 7.4 slices rather than adding another durable/snapshot state owner.

## 5. Exact next functional slice — SNE-7.4B Monk

Continue tests-first with only Monk protection production wiring.

Current expected legacy shape to audit:

```text
onSelectMonkProtectedTarget
  directly edits Monk draft App state

onConfirmMonkProtectedTarget
  directly owns confirmed Monk transition and downstream successor invalidation
```

Do not assume exact spelling before re-reading the live callback block.

Target typed flow:

```text
onSelectMonkProtectedTarget
  → NightResolutionEvent.EditMonkProtectionDraft(target)
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced monkDraftTarget

onConfirmMonkProtectedTarget
  → NightResolutionEvent.ConfirmMonkProtection
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced confirmedMonkTarget
  → project reduced confirmedDemonSuccessorTarget
```

### SNE-7.4B acceptance criteria

1. Establish RED first at the smallest practical production ownership/application boundary.
2. Monk draft edit leaves confirmed Monk protection unchanged.
3. Monk draft edit leaves confirmed Demon successor unchanged.
4. Confirming unchanged Monk protection preserves confirmed successor.
5. Confirming changed Monk protection commits the new Monk confirmation and invalidates only dependent successor confirmation.
6. Do **not** clear `demonSuccessorDraftTarget` merely because Monk confirmation changed.
7. Any existing durable Monk action/timeline side effect remains exactly once outside `NightCheckpointReducer`.
8. Reuse `currentClocktowerNightCheckpoint()`.
9. No new durable coordinator/checkpoint/navigation state.
10. Focused T0 RED/GREEN with `--rerun-tasks`, `git diff --check`, then remote parent/diff audit.
11. Stop before 7.4C Demon attack until Monk is fully accepted.

## 6. Large-file writer constraint

`CampBoardGameHostApp.kt` remains a large/truncated production file.

Under root `AGENTS.md`:

- Chat/GitHub connector is appropriate for tests/docs/small files;
- large production edits require a complete worktree and exact diff validation.

The successful SNE-7.4A pattern is now proven:

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

A one-shot temporary GitHub Actions runner was used for 7.4A because the current container could not safely obtain/edit the complete App file. That runner branch is outside PR #54 and is not production authority. Reuse a complete-worktree method only if still necessary; do not put temporary runner workflow files into the PR branch.

## 7. SNE-7.5 remains future work

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

## 8. Mayor product restriction remains unchanged

For current Trouble Brewing automatic-host production:

```text
Mayor redirect target cannot be the current Demon.
```

This is an intentional product/house-rule restriction. Generic non-self Demon death / Scarlet Woman succession remains deferred and must not be reopened during SNE-7.4.

## 9. Validation cadence

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

## 10. Do not do

- do not merge or mark PR #54 ready;
- do not rebase or force-push;
- do not reopen A3;
- do not resume App-root decomposition;
- do not broaden to arbitrary custom-script Demon death;
- do not introduce event sourcing;
- do not create a second durable night/navigation state owner;
- do not move timeline/global-sequence ownership out of `ClocktowerGameSession`;
- do not let draft edits/navigation invalidate confirmed mechanics;
- do not let recommendations define legality;
- do not mutate public role/death state early to fake effective same-night state;
- do not preserve obsolete source strings by distorting production implementation;
- do not activate reconstruction tests with fake/replayed UI-command semantics.

## 11. Exact next-start instruction

At the next continuation point:

```text
1. re-query live PR #54 head and checks;
2. inspect live Monk select/confirm callbacks and durable side effects;
3. establish SNE-7.4B Monk reducer production-wiring RED;
4. prove the RED fails only on missing Monk reducer cut-over;
5. cut only Monk draft/confirm semantics over to NightCheckpointReducer;
6. preserve durable side-effect ownership and successor draft state;
7. focused GREEN + diff check + remote parent/diff audit;
8. stop before Demon attack unless Monk is fully accepted.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
