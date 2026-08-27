# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-27  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Current priority: **SNE-7.4 production typed-seam migration — 7.4A–E COMPLETE; next 7.4F Dawn planner authority closeout**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state              CURRENT / SNE-7 CLOSEOUT
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

Do not resume App-root decomposition, A3 setup-snapshot work, A4/B4 authority promotion, recommendation tuning, or generic custom-script Demon-death succession until the current same-night campaign is closed or explicitly paused.

## 2. Accepted same-night foundation

Accepted production before SNE-7 includes:

```text
SNE-1..6A      effective mechanical death / consumer foundations
SNE-6B1       current-role projection foundation
SNE-6B2.1     pure Demon succession semantics
SNE-6B2.2     legality separated from recommendations
SNE-6B2.3     confirmed successor transaction/checkpoint
SNE-6B2.4     confirmed successor RoleChanged production projection
SNE-6B2.5A    Poisoner source lifetime follows current role
SNE-6B2.5B    roleActor resolves from effective current role
SNE-6B2.5C    Fortune Teller detects current/new Demon role
SNE-6B2.5D    Spy/Recluse registration follows current role without alive gating
SNE-6B2.6     exact confirmed successor materializes at Dawn; no draft/fallback
Mayor closeout shared legality + Host/UI wiring COMPLETE
```

Important accepted checkpoints remain:

```text
5a94c63536c04382f59963843c2ac10544962b02
  SNE-6B2.5 A–D

51179ecca667d5450550375735ca49aae932c06d
  SNE-6B2.6 exact Dawn materialization

2e8cb6a6a4763f9926956e5407d1c465e112e2bd
  Mayor Demon-exclusion Host/UI production wiring
```

The attempted generic non-self Demon-death 6C direction remains intentionally deferred. Do not resurrect it in SNE-7.

## 3. Current SNE-7 live status

```text
SNE-7.1  behavior-first night transaction matrix
         ESTABLISHED

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED / typed pure seam

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED / typed pure seam

SNE-7.4  switch production Compose/App wiring to typed seams
         PARTIAL / CURRENT FUNCTIONAL FRONTIER

  SNE-7.4A  Poison
             COMPLETE / FOCUSED + BROAD GREEN / REMOTE DIFF AUDITED

  SNE-7.4B  Monk
             COMPLETE / FOCUSED + BROAD GREEN / REMOTE DIFF AUDITED

  SNE-7.4C  Demon attack
             COMPLETE / FOCUSED GREEN / REMOTE DIFF AUDITED

  SNE-7.4D  Mayor redirect
             COMPLETE / FOCUSED GREEN / REMOTE DIFF AUDITED

  SNE-7.4E  Demon successor
             COMPLETE / FOCUSED GREEN / REMOTE DIFF AUDITED

  SNE-7.4F  Dawn planner authority closeout
             NEXT

SNE-7.5  restore / process-death reconstruction matrix
         SCAFFOLD EXISTS / INCOMPLETE

SNE-7.6  limited Compose smoke/integration coverage
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS; CI #803 stale failures CLEANED UP

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

### Latest accepted SNE-7.4 checkpoints

```text
09bea7ffc028833d3c893d740a5e9b6f90919bf6
  SNE-7.4A RED: ClocktowerPoisonReducerProductionWiringTest

db2a3746cedc2b667b0e5abd20e722ba8866263b
  production: route Poison checkpoint transitions through NightCheckpointReducer

e34598d60c012b6cb7c60e0e19da22b4483c600b
  formatting-only follow-up

CI #814 + R2 #741
  broad SUCCESS

6deb9d42f1b8ce5dfa1ca999778c22a49f714a91
  SNE-7.4B RED: ClocktowerMonkReducerProductionWiringTest

b1679f1b648e0de1d1aabaadb59715e53f9843f9
  production: route Monk checkpoint transitions through NightCheckpointReducer

CI #817 + R2 #744
  broad SUCCESS

0ea9d0b4c46dd69a0672a0c3fdc600d6e52dbe3d
  SNE-7.4C RED: ClocktowerDemonAttackReducerProductionWiringTest

CI #818 at RED head
  888 tests
  exactly 2 intended RED failures
  4 skipped
  Real Clingo SUCCESS
  R2 #745 SUCCESS

062e000afad1c407ba17ad7cef915dae0c487b30
  production: route Demon attack checkpoint transitions through NightCheckpointReducer

35659899745077f4f43cf914faa2bbf82eef3afa
  SNE-7.4D RED: ClocktowerMayorRedirectReducerProductionWiringTest

CI #823 at RED head
  891 tests
  exactly 2 intended RED failures
  4 skipped
  Real Clingo SUCCESS
  R2 #750 SUCCESS

21a7694ee340364485a283598cf7c2fa6fe2ae94
  production: route Mayor redirect checkpoint transitions through NightCheckpointReducer

fac430f4b40a219fcd92d91a6f45dacc2e89cc2b
  SNE-7.4E RED: ClocktowerDemonSuccessorReducerProductionWiringTest

CI #828 at RED head
  894 tests
  exactly 2 intended RED failures
  4 skipped
  Real Clingo SUCCESS
  R2 #755 SUCCESS

034b050c1656324766c1df3d2fbcd170af201389
  production: route Demon successor checkpoint transitions through NightCheckpointReducer
```

The 7.4A–E production cut-overs were validated in complete GitHub worktrees with:

```text
exact target-head guard
exact patch preconditions
git diff --check
single-production-file scope audit
focused --rerun-tasks
remote-head recheck before push
```

SNE-7.4E focused validation included:

```text
ClocktowerDemonSuccessorReducerProductionWiringTest
ClocktowerDawnExactDemonSuccessorWiringTest
ClocktowerNewDemonPresentationOwnershipTest
ClocktowerDemonAttackReducerProductionWiringTest
NightCheckpointReducerTest
SNE7NightTransactionBehaviorMatrixTest
```

The remote RED→GREEN compare for SNE-7.4E is exactly:

```text
fac430f4 → 034b050c
one commit
one production file
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
14 additions / 5 deletions
```

## 4. Protected same-night architecture

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable canonical interaction plan
+ checkpoint.nightStepIndex
→ derived current interaction / cursor
→ ClocktowerEffectiveNightState
→ actor eligibility
→ ability functioning
→ persistent-effect lifetime
→ target legality
→ information truth
→ triggers
→ current role
```

Hard contracts:

- mechanical death and public death announcement remain distinct;
- never write `eliminatedRound` early merely to make later-night logic work;
- stable seat/interaction identity never comes from re-indexing filtered views;
- draft UI state is never mechanical authority;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- death-trigger/even-if-dead exceptions are explicit;
- one canonical interaction plan prevents a newly-created Demon from receiving a second normal Demon action;
- outcome is not evaluated mid-transaction while mandatory succession remains unresolved;
- recommendation ranking remains downstream of rules legality;
- `ClocktowerNightCheckpoint` is the sole durable unfinished-night checkpoint state;
- `nightStepIndex` is the sole stored navigation position;
- navigation alone does not invalidate confirmed mechanics;
- draft editing alone does not invalidate confirmed mechanics;
- changed reconfirmation is the dependent-invalidation boundary;
- invalidating a dependent confirmed fact does not imply erasing its editable draft;
- `NightResolutionEvent` is transient command input, not a durable event log;
- `NightCheckpointReducer` owns checkpoint-local transitions only;
- `NightDawnResolutionPlanner` owns pure validated consequences/intent only;
- `ClocktowerGameSession` / App boundary retains sequence, timeline and durable commit authority.

## 5. SNE-7.4A–E accepted result

Poison, Monk, Demon attack, Mayor redirect and Demon successor production callbacks now consume the typed checkpoint seam:

```text
Edit callback
  → NightResolutionEvent.Edit...
  → NightCheckpointReducer.reduce
  → project reduced draft field

Confirm callback
  → NightResolutionEvent.Confirm...
  → NightCheckpointReducer.reduce
  → project reduced confirmed field
  → project reducer-owned dependent confirmed-successor invalidation when applicable
```

Ownership split:

```text
NightCheckpointReducer
  owns checkpoint-local draft / confirmation / dependent invalidation semantics

App / ClocktowerGameSession transaction boundary
  still owns sequence allocation where applicable
  still owns ActionFactDraft.Poison / Protect / Attack durable recording
  still owns player/game-state revision and other durable side effects
```

For Poison, Monk and Demon attack, draft editing leaves confirmed mechanics authoritative. Changed reconfirmation invalidates only the dependent confirmed Demon successor fact; the editable successor draft is preserved.

Mayor redirect legality remains separate from transition mechanics. The existing Host/rules/UI seam still enforces the current Trouble Brewing restriction that Mayor redirect cannot target the current Demon.

Demon successor draft editing now leaves the old confirmed successor authoritative until explicit Confirm. The confirm callback no longer treats its transient `selectedTarget` argument as a second mechanical authority; `NightCheckpointReducer.ConfirmDemonSuccessor` commits the checkpoint's current successor draft. Existing exact-Dawn contracts remain intact: Dawn materialization uses only the confirmed successor and does not fall back to the editable draft.

Reuse `currentClocktowerNightCheckpoint()`; do not introduce another durable or snapshot state owner.

## 6. Immediate next slice — SNE-7.4F Dawn planner authority closeout

Do not jump directly to reconstruction. First audit the remaining production Dawn/new-Demon callbacks against `NightDawnResolutionPlanner` and `DawnCommitIntent`.

The 7.4F objective is to remove any remaining duplicate handwritten Dawn transaction semantics from App while preserving App/session ownership of actual durable commits.

Audit at minimum:

```text
onConfirmNewDemon
night outcome / unresolved-successor gating
Mayor redirect / night death Dawn resolution
role-change materialization
poison carry/lifetime handling
outcome-evaluation gating
```

Acceptance direction:

1. Establish the smallest behavior/ownership RED only after identifying a concrete remaining duplicate production seam.
2. `NightDawnResolutionPlanner` remains pure and returns validated checkpoint/continuation/commit intent.
3. App/session remains the sole durable commit authority for public death, public/base role materialization, timeline/history and sequence allocation.
4. Exact confirmed successor remains the only Dawn successor authority; no draft/fallback.
5. Mayor Demon-exclusion and existing target-legality seams remain unchanged.
6. Do not mutate public death/role early to simulate same-night state.
7. Reuse the existing checkpoint and effective-state projections; no second coordinator.
8. Focused RED/GREEN, `git diff --check`, remote exact diff audit.
9. Stop before SNE-7.5 until 7.4F is explicitly accepted.

## 7. Restore/reconstruction boundary

`NightTransactionReconstructor` exists but is not a completed authority. Do not treat a scaffold or ignored contract as GREEN.

Required reconstruction model:

```text
GameState
+ decoded ClocktowerNightCheckpoint
+ reconstructed ruleset/canonical plan
→ derived effective night state
```

Required cases include:

- legacy draft-only successor does not invent confirmation/`RoleChanged`;
- confirmed successor + Previous remains authoritative;
- draft edit without Confirm leaves prior confirmation authoritative;
- invalid confirmed successor fails closed;
- missing interaction and out-of-range navigation restore safely;
- stale Mayor redirect to Demon fails closed;
- current effective role may differ from public/base role;
- identical durable inputs reconstruct identical effective state.

Do not reconstruct by replaying transient UI commands.

## 8. Testing and source-inspection policy

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal architecture source guard only where runtime proof is impractical
```

The four CI #803 source-shape failures are no longer an active gate. They were replaced/narrowed without changing correct production behavior to satisfy obsolete strings.

The SNE-7.4 production source ownership tests are temporary until a directly callable integration seam supersedes them.

## 9. Development workflow authority

Current execution authority:

```text
AGENTS.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
docs/TESTING_STRATEGY.md
docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md
docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md
```

Cadence:

```text
micro-slice → exact T0 RED/GREEN
related slices → remote diff audit, do not wait for old-head CI
logical checkpoint → T1 + triggered T2/T3
latest checkpoint head → GitHub CI/R2
merge → full required gate + explicit user authorization
```

## 10. Deferred work after same-night correctness

After SNE-7 closes, re-audit rather than automatically resuming old work. Known candidates:

1. App-root S9.2 Active Game Persistence Boundary;
2. A3 immutable setup-snapshot ownership/persistence;
3. broader dynamic/custom-script generic Demon-death succession;
4. production recommendation-authority promotion only if explicitly authorized.

## 11. Startup order for the next conversation

Read in this order:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
8. `docs/TESTING_STRATEGY.md`;
9. re-query live `main`, PR #54 head/state and latest checks before editing.

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
