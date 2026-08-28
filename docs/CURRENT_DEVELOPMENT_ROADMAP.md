# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest accepted broad code checkpoint before 7.9E: `06f06fb5c0689983c9baaf0bfe1765a8c69a8d16`  
> Current 7.9E intentional RED code checkpoint: `94adcd0febbfcedc7b523249be5cf784a8bc38f2`  
> Current priority: **SNE-7.9E exactly-once durable Dawn App wiring correction**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state / SNE-7      REOPENED / 7.9E ACTIVE
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

PR #54 remains **draft, open, and unmerged**. Do not resume unrelated App-root decomposition, A3 setup snapshot, A4/B4 authority promotion, recommendation tuning, generic custom-script Demon succession, or unrelated Host/UI cleanup until SNE-7.9 is closed or explicitly paused.

## 2. SNE-7 corrective route

```text
SNE-7.9A  Mayor redirect dependency invalidation          COMPLETE / GREEN
SNE-7.9B  Chambermaid stale-target revalidation           COMPLETE / GREEN
SNE-7.9C  canonical night-death resolution                COMPLETE / BROAD GREEN
SNE-7.9D  Demon succession legality / Dawn                COMPLETE / BROAD GREEN
SNE-7.9E  real restore + durable Dawn integration          ACTIVE / CORRECTION RED
```

The remaining work is no longer primarily “which role rule wins”. The active defect is the production materialization boundary:

```text
canonical restored night result
→ Dawn durable materialization
→ mechanical state mutation exactly once
→ ActionFact exactly once
→ public AliveAt(false) observation exactly once
→ RoleChange exactly once
→ Night → Dawn phase transition exactly once
→ crash / restore / retry produces the same final history and state
```

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

## 4. SNE-7.9E progress already accepted

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

`b97e9486` changed only `ClocktowerHostScreen.kt`; focused runner #17 (`33146544493`) succeeded. Bot-origin normal CI #923 / R2 #850 were `action_required` with no jobs, a trigger/provenance restriction rather than a test failure.

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

The planner seam is **accepted and must not be redesigned during the App wiring correction unless a typed counterexample proves it wrong**.

## 5. Current active RED — App durable Dawn wiring

Initial App wiring RED:

```text
c0075ca6e1a8b4734d2ba7bdc8ea28ffab4a23c4
test: require idempotent Dawn planner wiring
```

CI #926 compiled production and ran 922 tests with exactly the two new wiring failures. This was a clean intended RED.

First GREEN candidate:

```text
af14b913b3b46c97c12b8e40cb8b304bc82f44b6
fix: materialize Dawn durable effects idempotently
```

Focused runner #20 (`33148959735`) passed, but **this commit is rejected as an accepted checkpoint**. Later semantic audit found a real omission:

- `preflightClocktowerPublicAliveObservation(...)` only proves that a global observation commit would succeed before public mutation;
- it deliberately does **not** write returned `observationLog`, global cursor, or player-input revision back to App state;
- `af14b913` disabled generic Dawn death semantic projection with `projectSemanticHistory=false` to avoid a duplicate dynamic Death `ActionFact`;
- therefore the planner-owned stable public `AliveAt(false)` was only preflighted and was never durably appended.

Supplemental/current RED:

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

This is the current intentional code RED. Do not treat `af14b913` as GREEN and do not weaken the two legacy guards merely to make FAST green.

## 6. Legacy guard inspection — completed

Read-only temp runner #21 (`33149908226`) completed **SUCCESS** against exact code head `94adcd0f`.

The two legacy guards protect real architecture contracts:

### `ClocktowerGlobalObservationProductionWiringTest`

- `recordEpistemicObservation(...)` remains the real shared durable observation commit authority;
- exact duplicate GLOBAL_V1 commits must return before list mutation/durability publication;
- public `AliveAt` observations must flow through the same draft/commit authority;
- public elimination preflight must occur before caller state publication;
- preflight itself must remain non-mutating.

### `ClocktowerHistoricalActionProductionWiringTest`

- App owns one `ActionFactTimeline` and shares the global timeline cursor between actions and observations;
- Death, RoleChange and PhaseAdvance remain replayable semantic actions;
- RoleChange semantic history is recorded before the base-card state mutation;
- save/restore preserves the action timeline without upgrading legacy history.

Therefore the next GREEN must **preserve these contracts**, not delete or dilute them.

## 7. Exact next implementation direction

For Dawn death, the intended production order is now constrained to:

```text
planner computes stable IDs + mutation requirements
→ preflight planner-owned AliveAt(false) before public state mutation
→ commit planner-owned stable Death ActionFact if missing
→ mutate mechanical/public death state only if stateMutationRequired
→ add presentation event without generic duplicate semantic-history projection
→ perform a real recordEpistemicObservation(...) append using the same planner-owned stable record ID
```

Key rule: **preflight and durable append are two separate responsibilities**.

Do **not** simply re-enable generic `addClocktowerEvent` semantic projection for Dawn death: that would recreate revision/event-counter-based Death action identity and can duplicate historical action authority.

RoleChange and phase wiring must similarly preserve stable planner-owned IDs and separate “history missing” from “state mutation missing”.

Partial retry must work when, for example, the death is already mechanically applied but its durable Death action or public AliveAt observation is absent: the missing history is repaired without remutating the card.

## 8. Next focused verification set

The corrected GREEN runner must include at minimum:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
ClocktowerHistoricalActionProductionWiringTest
NightDawnDurableMaterializationPlannerTest
ClocktowerGameSessionTest / global observation commit coverage
ClocktowerHistoricalActionObservationCaptureTest
Demon succession production wiring / restore ownership guards
Night transaction architecture / reconstructor focused tests
Dawn resolution planner tests
```

Runner #20 is not sufficient evidence because it omitted the two legacy production guards now known to be relevant.

After focused GREEN:

```text
remote parent/diff/scope audit
→ normal broader CI checkpoint
→ restore / partial-commit equivalence validation
→ final SNE-7.9E broad acceptance gate
```

Only after that broad gate may the night-action/information consistency bug campaign be declared fully repaired.

## 9. Protected architecture / scope

Hard contracts:

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

Scope exclusions remain:

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

## 10. Startup order for next conversation

Read:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md` as needed;
5. re-query live `main`, PR #54 head/state/checks before editing.

Exact continuation instruction:

```text
1. latest code RED checkpoint = 94adcd0febbfcedc7b523249be5cf784a8bc38f2;
2. any commits after it may be docs-only handoff synchronization — distinguish docs head from code head;
3. runner #21 inspection is already complete and confirmed the two legacy guards are genuine contracts;
4. continue 7.9E by producing the minimal corrective CampBoardGameHostApp.kt GREEN;
5. keep preflight fail-before-mutation AND add the actual planner-owned durable AliveAt append;
6. preserve stable Death / RoleChange / PhaseAdvance action ownership and history-before-mutation ordering;
7. run the expanded focused set including both legacy guards;
8. then perform remote diff/scope audit and broader acceptance validation;
9. keep PR #54 draft/open/unmerged.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
