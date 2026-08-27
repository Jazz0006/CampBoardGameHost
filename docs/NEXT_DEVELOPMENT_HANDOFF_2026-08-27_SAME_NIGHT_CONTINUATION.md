# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Handoff status: **SNE-7.4 active — Poison + Monk + Demon attack + Mayor redirect + Demon successor cut-overs complete; Dawn planner authority closeout is next**

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

PR #54 remains open, draft and not merged. Do not merge, mark ready, rebase, force-push, or broaden it without explicit user authorization.

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
  7.4D Mayor redirect    COMPLETE / FOCUSED GREEN / REMOTE AUDITED
  7.4E Demon successor   COMPLETE / FOCUSED GREEN / REMOTE AUDITED
  7.4F Dawn planner      NEXT

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

### Poison / Monk

```text
09bea7ffc028833d3c893d740a5e9b6f90919bf6  Poison RED
db2a3746cedc2b667b0e5abd20e722ba8866263b  Poison production
e34598d60c012b6cb7c60e0e19da22b4483c600b  Poison formatting
CI #814 + R2 #741                            broad SUCCESS

6deb9d42f1b8ce5dfa1ca999778c22a49f714a91  Monk RED
b1679f1b648e0de1d1aabaadb59715e53f9843f9  Monk production
CI #817 + R2 #744                            broad SUCCESS
```

### Demon attack

```text
0ea9d0b4c46dd69a0672a0c3fdc600d6e52dbe3d  RED
CI #818: 888 tests, exactly 2 intended failures, 4 skipped
Real Clingo SUCCESS / R2 #745 SUCCESS
062e000afad1c407ba17ad7cef915dae0c487b30  production
```

### Mayor redirect

```text
35659899745077f4f43cf914faa2bbf82eef3afa  RED
CI #823: 891 tests, exactly 2 intended failures, 4 skipped
Real Clingo SUCCESS / R2 #750 SUCCESS
21a7694ee340364485a283598cf7c2fa6fe2ae94  production
```

### Demon successor

```text
fac430f4b40a219fcd92d91a6f45dacc2e89cc2b  RED: ClocktowerDemonSuccessorReducerProductionWiringTest

CI #828 at RED head
  894 tests
  exactly 2 failures, both intended successor wiring REDs
  4 skipped
  Real Clingo SUCCESS
  R2 #755 SUCCESS

034b050c1656324766c1df3d2fbcd170af201389
  production: route Demon successor checkpoint transitions through reducer
```

Remote 7.4E RED→GREEN compare:

```text
fac430f4 → 034b050c
one commit
one production file
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
14 additions / 5 deletions
```

Focused 7.4E validation passed with `--rerun-tasks` for:

```text
ClocktowerDemonSuccessorReducerProductionWiringTest
ClocktowerDawnExactDemonSuccessorWiringTest
ClocktowerNewDemonPresentationOwnershipTest
ClocktowerDemonAttackReducerProductionWiringTest
NightCheckpointReducerTest
SNE7NightTransactionBehaviorMatrixTest
```

It also passed exact target-head guard, exact patch preconditions, `git diff --check`, single-production-file scope audit, and remote-head recheck before push.

## 5. Accepted authority split through 7.4E

Production now follows the typed checkpoint seam for Poison, Monk, Demon attack, Mayor redirect and Demon successor.

```text
Edit draft callback
  → NightResolutionEvent.Edit...
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced draft field back to App state

Confirm callback
  → NightResolutionEvent.Confirm...
  → NightCheckpointReducer.reduce(currentClocktowerNightCheckpoint(), event)
  → project reduced confirmed field
```

For upstream Poison/Monk/Attack changes, reducer-owned dependent confirmed-successor invalidation is projected without erasing the editable successor draft.

The App/session transaction boundary still owns sequence allocation, `ActionFactDraft.Poison / Protect / Attack` durable recording, player/game-state revision, timeline/history and other durable side effects.

Mayor redirect legality remains in the existing Host/rules/UI seam and still excludes the current Demon.

Demon successor confirmation now commits the checkpoint's current successor draft through `NightResolutionEvent.ConfirmDemonSuccessor`; the transient `(String)` confirm callback argument is no longer a second mechanical authority. Existing exact-Dawn contracts remain unchanged: only `clocktowerConfirmedDemonSuccessorTarget` may materialize the new Demon at Dawn; the editable draft is not a fallback.

Reuse the existing `currentClocktowerNightCheckpoint()` helper. Do not add another durable/snapshot state owner.

## 6. Exact next functional slice — SNE-7.4F Dawn planner authority closeout

First audit the remaining production Dawn/new-Demon path against `NightDawnResolutionPlanner` and `DawnCommitIntent`; do not assume a single broad patch is needed.

Audit at minimum:

```text
onConfirmNewDemon
night outcome / unresolved-successor gating
Mayor redirect / night death Dawn resolution
role-change materialization
poison carry/lifetime handling
outcome-evaluation gating
```

### SNE-7.4F acceptance direction

1. Identify concrete duplicate handwritten production semantics before creating RED.
2. Establish the smallest behavior/ownership RED that demonstrates the missing planner authority.
3. `NightDawnResolutionPlanner` remains pure; it produces validated checkpoint/continuation/`DawnCommitIntent` only.
4. App/session remains the sole durable commit authority for public death, public/base role materialization, timeline/history and sequence allocation.
5. Exact confirmed successor remains the only Dawn successor authority; no draft fallback.
6. Preserve current Mayor Demon-exclusion and other legality seams.
7. Do not mutate public role/death early to fake same-night state.
8. Reuse existing checkpoint/effective-state projections; no second coordinator.
9. Focused RED/GREEN with `--rerun-tasks`, `git diff --check`, and remote exact diff audit.
10. Stop before SNE-7.5 until 7.4F is explicitly accepted.

## 7. Large-file writer constraint

`CampBoardGameHostApp.kt` remains a large/truncated production file. Chat/GitHub connector is appropriate for tests/docs/small files; large production edits require a complete worktree and exact diff validation.

The proven 7.4A–E pattern is:

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

`NightTransactionReconstructor` exists but remains a scaffold. Do not jump there before 7.4F is accepted.

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

This remains an intentional product/house-rule restriction. Generic non-self Demon death / Scarlet Woman succession remains deferred and must not be reopened during SNE-7.4.

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

Do not merge or mark PR #54 ready; do not rebase/force-push; do not reopen A3/App-root work; do not broaden to arbitrary custom-script Demon death; do not introduce event sourcing or a second night state owner; do not let recommendations define legality; do not mutate public role/death state early; do not activate reconstruction by replaying transient UI commands.

## 11. Exact next-start instruction

```text
1. re-query live PR #54 head/checks;
2. audit live onConfirmNewDemon / onConfirmNight / Dawn resolution path against NightDawnResolutionPlanner;
3. identify the smallest remaining duplicate production semantic;
4. establish SNE-7.4F RED only for that concrete seam;
5. cut planner authority without moving durable commit ownership out of App/session;
6. focused GREEN + diff check + remote parent/diff audit;
7. stop before SNE-7.5 until 7.4F is accepted.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
