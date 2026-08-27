# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Handoff status: **SNE-7 implementation active; 7.4 is the current functional frontier**

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

Then re-query live `main`, branch head, PR #54 state/head and checks. Do not assume the SHA below is still live after further work.

## 2. Stable baseline and PR state

`main` remained:

```text
c8985cb4991f6c7e5ea02adedb932d2d86452da1
```

Before this documentation refresh, PR #54 was verified as:

```text
open
draft
not merged
head = 2aa528dbb898313c51b1a7fb06d11a60c883b84f
commits = 105
```

That head was:

```text
2aa528dbb898313c51b1a7fb06d11a60c883b84f
  test: remove low-value same-night wiring assertions
```

Checks:

```text
R2 #730  SUCCESS
CI  #803  FAILURE
```

CI #803 completed 879 tests with 4 failures, all from source-inspection tests rather than compile/runtime crashes:

```text
ClocktowerSameNightEffectiveStateProductionWiringTest
  later normal actor eligibility must consume effective same-night state

ClocktowerProductionOtherNightWiringTest
  other-night planner input uses only currently waking roles including living Drunk shown role
  other-night planner receives existing resolved rule outcomes rather than deriving rules
  production other night is planner-first and lazily materialized by stable identity
```

Treat this as the immediate baseline-cleanup gate.

## 3. Important accepted pre-SNE-7 checkpoints

Keep these historical production checkpoints as evidence:

```text
5a94c63536c04382f59963843c2ac10544962b02
  SNE-6B2.5 A–D current-role consumer migration

51179ecca667d5450550375735ca49aae932c06d
  SNE-6B2.6 exact Dawn Demon successor materialization

2e8cb6a6a4763f9926956e5407d1c465e112e2bd
  Mayor Demon-exclusion Host/UI production wiring
```

Mayor product decision remains:

```text
Mayor redirect target cannot be the current Demon.
```

This is an intentional product/house-rule restriction. Generic non-self Demon death / Scarlet Woman succession remains deferred and must not be reopened in the current Trouble Brewing closeout.

## 4. Actual SNE-7 progress

The previous handoff section saying “immediate next work = SNE-7.1 behavioral REDs” is obsolete.

Live implementation has advanced to:

```text
SNE-7.1  behavior-first transaction matrix
         ESTABLISHED

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED

SNE-7.4  production Compose/App typed-seam cutover
         PARTIAL / CURRENT FRONTIER

SNE-7.5  NightTransactionReconstructor + contract scaffold
         EXISTS / INCOMPLETE

SNE-7.6  small Compose integration/smoke set
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

Existing behavioral coverage already proves important lifecycle semantics through typed seams, including:

- confirmed successor survives checkpoint restore + Previous;
- editing successor draft leaves old confirmation mechanically authoritative;
- changed upstream reconfirmation invalidates stale successor confirmation;
- Poisoner becoming Demon ends effective poison while raw confirmation remains durable;
- Fortune Teller sees the new Demon and the old dead Demon correctly for the night;
- Monk-protected Imp self-kill produces no succession;
- functioning Scarlet Woman at 5+ is forced on successful self-kill;
- nonfunctioning Scarlet Woman leaves ordinary living-Minion choice;
- same persisted checkpoint/canonical plan reconstructs deterministic projected state;
- canonical plan still contains exactly one normal Imp interaction.

`NightCheckpointReducer` also has direct typed tests for Poison/Monk/attack/Mayor/successor draft/confirm/invalidation semantics.

## 5. Immediate gate — finish CI #803 source-test cleanup

Do this before treating the current branch as a clean baseline.

Apply the project source-test rule:

```text
existing typed behavior covers the invariant
→ retire/narrow obsolete source-string assertion

unique behavior not covered
→ add smallest typed behavior contract first
→ then retire/narrow source assertion
```

Do **not** modify production formatting, local variables or otherwise correct behavior merely to satisfy old source-text shape.

The retirement policy already records that same-night source wiring tests are temporary migration debt and should be replaced by typed behavior when available.

## 6. Next functional slice — SNE-7.4A Poison production reducer wiring

After the cleanup gate, continue tests-first with:

```text
SNE-7.4A — Poison production reducer wiring
```

Current issue:

Production App callbacks still directly own checkpoint semantics for Poison draft/confirmation and successor invalidation even though `NightCheckpointReducer` already owns these rules.

Target flow:

```text
onSelectPoisonTarget
  → NightResolutionEvent.EditPoisonDraft(target)
  → NightCheckpointReducer.reduce(checkpoint, event)
  → replacement checkpoint

onConfirmPoisonTarget
  → NightResolutionEvent.ConfirmPoison
  → NightCheckpointReducer.reduce(checkpoint, event)
  → replacement checkpoint
```

Important responsibility split:

```text
NightCheckpointReducer
  owns checkpoint-local draft/confirmed/invalidation semantics

existing App / ClocktowerGameSession transaction boundary
  keeps sequence allocation
  keeps raw timeline/history/event commit
  keeps game-state revision / phase orchestration
  keeps durable side effects outside the reducer
```

### SNE-7.4A acceptance criteria

1. Establish a real typed RED at the smallest callable application boundary.
2. Poison draft edit leaves confirmed poison unchanged.
3. Poison draft edit leaves confirmed successor unchanged.
4. Confirming unchanged poison preserves confirmed successor.
5. Confirming changed poison commits the new poison and invalidates confirmed successor.
6. Existing durable Poison action/timeline side effects still occur exactly once.
7. Reducer remains pure; no global sequence/timeline authority moves into it.
8. Focused T0 GREEN with `--rerun-tasks` and `git diff --check`.
9. Remote exact diff audit before proceeding.

Expected continuation:

```text
SNE-7.4B Monk
SNE-7.4C Demon attack
SNE-7.4D Mayor redirect
SNE-7.4E Demon successor
SNE-7.4F Dawn planner production authority closeout
```

Do not combine these into an uncontrolled App rewrite.

## 7. Large-file writer constraint

`CampBoardGameHostApp.kt` is a large/truncated source file. Under root `AGENTS.md`:

- tests/docs/small-medium files should be written by Chat through the GitHub connector;
- large/truncated production edits must use a complete worktree executor such as Codex/Luna when whole-file replacement through the connector is unsafe.

Therefore the safe SNE-7.4A execution split is:

```text
Chat
  live audit
  RED/test design and small-file edits
  exact deterministic production patch specification
  remote diff/checkpoint acceptance

Luna/Codex when needed
  exact localized CampBoardGameHostApp.kt edit
  exact focused local RED/GREEN command
  git diff --check
  commit + push
```

Do not ask Luna/Codex to redesign the slice.

## 8. SNE-7.5 reconstruction remains future work

`NightTransactionReconstructor` exists but is still a scaffold. Do not jump to it before production transaction authority is sufficiently cut over.

Required reconstruction boundary later:

```text
checkpoint encode/save
→ process/lifecycle boundary
→ checkpoint decode
→ ruleset + canonical interaction plan rebuild
→ effective-state reconstruction
```

Still required:

- legacy draft-only successor invents no confirmation/`RoleChanged`;
- invalid confirmed successor fails closed;
- missing interaction handled safely;
- out-of-range `nightStepIndex` handled safely;
- stale Mayor→Demon redirect fails closed;
- effective role can differ from public/base role;
- Previous preserves confirmation;
- draft edit without confirm preserves old mechanical fact;
- same durable inputs reconstruct same state.

Do not reconstruct by replaying transient `NightResolutionEvent` commands.

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

Do not wait for old-head CI after every related micro-slice.

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
2. verify whether CI #803 source-test failures have already been cleaned up;
3. if not, finish the source-test cleanup with typed replacement mapping;
4. establish SNE-7.4A Poison production-wiring RED;
5. cut only Poison draft/confirm semantics over to NightCheckpointReducer;
6. preserve durable side-effect ownership;
7. focused GREEN + diff check + remote audit;
8. stop before Monk unless the Poison slice is fully accepted.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
