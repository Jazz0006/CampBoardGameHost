# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest accepted broad code checkpoint before 7.9E: `06f06fb5c0689983c9baaf0bfe1765a8c69a8d16`  
> Latest 7.9E production GREEN: `730c494f9972ec6425563d04a05c7b2984dda16e`  
> Latest 7.9E restore/retry acceptance test checkpoint: `61387b473ff18e174b211a80962eed6cf0228ed6`  
> T4 broad acceptance checkpoint: `70935644daf5c06985420f19833dbda3a160bbfa`  
> Current priority: **SNE-7 corrective campaign closed / next production scope not yet authorized**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state / SNE-7      CLOSED / BROAD GREEN
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

PR #54 remains **draft, open, and unmerged**. SNE-7 completion does not authorize merge/readiness work or automatic resumption of unrelated App-root decomposition, A3 setup snapshot, A4/B4 authority promotion, recommendation tuning, generic custom-script Demon succession, or unrelated Host/UI cleanup.

## 2. SNE-7 corrective route

```text
SNE-7.9A  Mayor redirect dependency invalidation          COMPLETE / GREEN
SNE-7.9B  Chambermaid stale-target revalidation           COMPLETE / GREEN
SNE-7.9C  canonical night-death resolution                COMPLETE / BROAD GREEN
SNE-7.9D  Demon succession legality / Dawn                COMPLETE / BROAD GREEN
SNE-7.9E  real restore + durable Dawn integration          COMPLETE / BROAD GREEN
```

The final 7.9E contract is satisfied:

```text
canonical restored night result
→ Dawn durable materialization
→ mechanical state mutation exactly once
→ Death ActionFact exactly once
→ public AliveAt(false) observation exactly once
→ RoleChange ActionFact exactly once
→ Night → Dawn PhaseAdvance ActionFact exactly once
→ crash / restore / retry produces the same final history and state
```

The production correction, typed restore/retry convergence acceptance proof, and full-strength T4 checkpoint are all GREEN.

## 3. Accepted 7.9C / 7.9D baseline

### 7.9C — canonical night-death resolution

**COMPLETE / BROAD GREEN**

Accepted production checkpoint:

```text
b6185ccf6b23583c112b040f831e65f2724f1035
```

Accepted branch/test head:

```text
bfdf42a083653475ed94607b9c0ad69a5685bcb8
```

Broad evidence:

```text
CI #897 SUCCESS
- Android full tests
- assembleDebug
- Real Clingo
- CI gate
R2 #824 SUCCESS
```

Canonical authority:

```text
confirmed attack / poison / Monk facts
+ stable cards
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
```

Do not recreate independent Soldier / Monk / Demon-poison / Mayor death gates in App or Host.

### 7.9D — succession legality / Dawn

**COMPLETE / BROAD GREEN — DO NOT REOPEN**

Key tests-first checkpoints:

```text
8bf44fc7  D1 RED
ba777221  D1 GREEN

d396a80d  D2A RED
f982e50c  D2A GREEN

b1c1af78  D2B RED
...later GREEN/checkpoint commits close 7.9D...
06f06fb5c0689983c9baaf0bfe1765a8c69a8d16  broad protective checkpoint
```

Accepted semantics remain:

```text
confirmed Imp self attack
→ canonical attack outcome
→ successful self-kill reconstructs old Imp MechanicalDeath independently of successor existence
→ canonical DemonSuccessionResolution
   None   → no RoleChanged, death still exists
   Forced → forced legal successor
   Choice → confirmed target must be canonically legal
→ NightDawnResolutionPlanner owns continuation / pending-new-Demon / Dawn gating
```

A functioning Monk that prevents the self-kill yields no death and no succession; stale successor fields cannot invent succession.

## 4. SNE-7.9E accepted foundations

### 4.1 Restore/reconstruction composition

Existing 7.9E chain before durable materialization:

```text
58ae...  RED — restored night transaction composition
82cb...  GREEN — compose restored transaction state
0f516... test — forced successor restore composition
1a1e... test — expose restored mechanical events
9f5a9aba117a98d1bf18c0cfdb3b0bdffc3a8bb0
          refactor — expose reconstructed night events
40bac754b4e7c6e80e25b1e87b0bc9bf8d04346c
          RED — Host restored succession authority
b97e9486182f8d59820d7c61eaad3eaac5ae68e2
          GREEN — project canonical restored succession events
```

`b97e9486` changed only `ClocktowerHostScreen.kt`; focused runner #17 (`33146544493`) succeeded. Bot-origin normal CI #923 / R2 #850 did not execute jobs; that absence is not treated as test evidence either way.

### 4.2 Pure exactly-once Dawn planner

RED:

```text
674f8da229988a47031c33c3af7453bb6f31980a
test: require idempotent Dawn durable materialization
```

GREEN:

```text
c2fdb820902d9c3bab6f33ebecd04ad90c62153f
feat: plan idempotent Dawn durable materialization
```

`NightDawnDurableMaterializationPlanner` is an accepted pure/transient seam. Stable durable IDs depend only on `gameId + round + logical effect`; they do **not** depend on mutable `gameStateRevision`, event counter, callback count, or timeline cursor.

It separately plans:

- whether mechanical state mutation is still required;
- whether a Death `ActionFact` still needs committing;
- whether public `AliveAt(false)` still needs committing;
- whether a `RoleChange` action/mutation still needs committing/applying;
- whether the phase action/mutation still needs committing/applying.

This explicitly supports partial persistence repair: durable history can be filled in later without repeating an already-applied mechanical mutation.

Validation:

```text
CI #925 GREEN — Android FAST + CI gate
R2 #852 SUCCESS
focused runner #18 GREEN
```

The planner seam is accepted and should not be redesigned unless a typed counterexample proves it wrong.

## 5. App durable Dawn wiring — corrected GREEN

### 5.1 Intentional RED

Initial App wiring RED:

```text
c0075ca6e1a8b4734d2ba7bdc8ea28ffab4a23c4
test: require idempotent Dawn planner wiring
```

Supplemental durable-observation RED:

```text
94adcd0febbfcedc7b523249be5cf784a8bc38f2
test: require durable Dawn alive observation commit
```

Normal CI #928 (`33149213389`) ran 922 tests with exactly three failures:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
ClocktowerHistoricalActionProductionWiringTest
```

The latter two failures exposed stale source-level syntax proxies around real architecture contracts; they were not deleted or weakened.

### 5.2 Legacy guard semantic correction

Checkpoint:

```text
6f656387b8f4d44a5fd6f7aaae819779c4166d08
test: preserve semantic Dawn history guards
```

The corrected guards preserve the actual contracts:

- public elimination preflight must occur before the real card death mutation;
- `recordEpistemicObservation(...)` remains the durable shared observation authority;
- RoleChange history must still be produced by the App-owned role-change action helper;
- RoleChange history must occur before base-card role mutation;
- Death / RoleChange / PhaseAdvance remain replayable semantic actions on the shared global timeline.

The tests-first runner proved these corrected guards pass while the new durable AliveAt wiring test remained the sole intended RED.

### 5.3 Minimal App GREEN

Production checkpoint:

```text
730c494f9972ec6425563d04a05c7b2984dda16e
fix: durably commit Dawn alive observation
```

Parent:

```text
6f656387b8f4d44a5fd6f7aaae819779c4166d08
```

Exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
+17 / -0
```

Accepted Dawn death order is now:

```text
planner computes stable IDs + mutation requirements
→ preflight planner-owned AliveAt(false) before public state mutation
→ commit planner-owned stable Death ActionFact if missing
→ mutate death state only if stateMutationRequired
→ create presentation event with generic semantic projection disabled
→ if planner-owned public AliveAt record is missing:
     recordEpistemicObservation(... stable planner record ID ...)
→ continue Demon succession flow
```

The durable AliveAt append is outside `stateMutationRequired`, so a retry can repair missing durable history without remutating an already-dead card.

Expanded focused GREEN runner:

```text
33152530426 — SUCCESS
```

It included both corrected legacy production guards plus the Dawn materialization planner, session/global observation coverage, historical action capture, Demon succession, restore/night-transaction and Dawn resolution focused families.

Remote audit confirmed the production commit changed only the intended App file and passed `git diff --check` equivalent protection.

## 6. Restore / partial-commit equivalence — acceptance GREEN

Typed acceptance checkpoint:

```text
61387b473ff18e174b211a80962eed6cf0228ed6
test: prove Dawn restore retry convergence
```

Only added:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/session/
  NightDawnRestoreRetryConvergenceAcceptanceTest.kt
```

No production file changed in this checkpoint.

The acceptance test composes the real typed seams:

```text
persisted ClocktowerNightCheckpoint
+ partially materialized GameState
→ NightTransactionRestoreComposition.restore
→ reconstructed effective same-night state
→ NightDawnResolutionPlanner death + succession + identity confirmation
→ DawnCommitIntent
→ NightDawnDurableMaterializationPlanner
→ real ClocktowerGameSession GLOBAL_V1 action/observation commit APIs
```

It compares an uninterrupted Imp-self-kill/successor Dawn with a partial state where the old Imp is already mechanically dead but its durable Dawn history still needs repair. The two routes converge to identical:

```text
alive seats
current role / successor
final phase
ActionFactTimeline
EpistemicObservationLog
shared global timeline cursor
player-input revision
```

Exactly-once assertions require:

```text
1 × Death ActionFact
1 × RoleChange ActionFact
1 × PhaseAdvance ActionFact
1 × public AliveAt(false) observation
```

A further replay of the fully materialized final state is a no-op and remains exactly equal.

Normal validation on this checkpoint:

```text
CI #932 / 33153438146
- Classify changes: SUCCESS
- Android FAST: SUCCESS (actual execution)
- full Android/build: SKIPPED by ordinary test-only routing
- ASP: SKIPPED by ordinary test-only routing
- Real Clingo: SKIPPED by ordinary test-only routing
- CI gate: SUCCESS

R2 / 33153438147
- verify-boundary: SUCCESS
```

## 7. Final T4 broad acceptance — COMPLETE / GREEN

The docs checkpoint below intentionally carried `[full-ci]` and selected the repository’s full-strength T4 policy:

```text
70935644daf5c06985420f19833dbda3a160bbfa
[full-ci] docs: checkpoint SNE-7.9E acceptance
```

T4 evidence:

```text
CI #933 / 33153679896 — SUCCESS

Classify changes
- SUCCESS

Android tests
- FAST path: SKIPPED as expected under [full-ci]
- Run full Android unit tests and build debug APK: SUCCESS
  (actual full-strength execution; 08:01:45 → 08:04:47 UTC)

ASP contract tests
- golden corpus validation: SUCCESS
- Oracle harness unit tests: SUCCESS

Real Clingo cross-validation
- Clingo 5.8.0 install/verification: SUCCESS
- frozen botc-asp Oracle fetch: SUCCESS
- Oracle cross-validation: SUCCESS

CI gate
- SUCCESS

R2 main-thread boundary #860 / 33153679938
- SUCCESS
```

This satisfies the explicit 7.9E close condition. `730c494f...` remains the latest production code checkpoint, `61387b47...` remains the typed restore/retry acceptance checkpoint, and `70935644...` is the accepted full T4 checkpoint. This later roadmap synchronization is docs-only and does not change executable semantics.

## 8. Protected architecture / scope

Hard contracts retained after SNE-7 closure:

- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night state owner;
- `GameState` / action-observation timelines remain durable game-history authority;
- restore reconstructs from durable inputs, never transient event-command replay;
- mechanical death and public announcement remain distinct;
- same-night `RoleChanged` is derived before Dawn materialization;
- stable seat identity never comes from filtered-list reindexing;
- draft UI state is never mechanical authority;
- stable durable IDs cannot depend on mutable revision/event counters;
- rules determine legality; recommendation/UI never become mechanical authority;
- no second persisted coordinator and no event-sourcing rewrite.

Scope exclusions remain unless a future roadmap decision explicitly promotes them:

```text
generic non-self Demon-death succession
custom-script Demon succession
A4/B4 authority promotion
recommendation tuning
App-root decomposition
unrelated Host/UI cleanup
PR merge/readiness work
```

Large `CampBoardGameHostApp.kt` changes still require complete-worktree-safe / exact-head editing and exact remote scope audit. Never construct a whole-file replacement from truncated source content.

## 9. Startup order / next continuation

Read:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md` only as historical SNE-7.9E context;
4. re-query live `main`, PR #54 head/state/checks before any new edit.

Current continuation instruction:

```text
1. SNE-7.9A/B/C/D/E are complete; 7.9E T4 broad acceptance is GREEN.
2. production 7.9E GREEN = 730c494f9972ec6425563d04a05c7b2984dda16e.
3. restore/retry convergence acceptance = 61387b473ff18e174b211a80962eed6cf0228ed6.
4. full T4 acceptance = 70935644daf5c06985420f19833dbda3a160bbfa, CI #933 + R2 #860 GREEN.
5. any commit after 70935644 that only synchronizes docs is not a new executable checkpoint.
6. do not reopen SNE-7 without a typed counterexample.
7. do not automatically merge PR #54 or begin another deferred production scope; obtain/derive an explicit next roadmap decision first.
8. keep PR #54 draft/open/unmerged unless the user explicitly authorizes a state change.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
