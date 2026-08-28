# Next Development Handoff — SNE-7.9E Durable Dawn Exactly-Once Correction

> Date: 2026-08-28  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Latest broad accepted pre-7.9E checkpoint: `06f06fb5c0689983c9baaf0bfe1765a8c69a8d16`  
> Current intentional code RED checkpoint: `94adcd0febbfcedc7b523249be5cf784a8bc38f2`  
> Handoff status: **7.9A/B/C/D COMPLETE; major 7.9E seams accepted; final App durable materialization correction ACTIVE; PR #54 stays draft/unmerged**

## 1. Startup contract

Before changing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. re-query live `main` and PR #54 head/state/checks;
5. distinguish the last code checkpoint from any later docs-only synchronization commit.

Do not assume the branch SHA written here is still the live head. The code checkpoint that matters for the current RED is `94adcd0f`; the documentation synchronization commit created after it contains no production/test code change.

Never merge, mark PR ready, rebase, force-push, or broaden PR #54 without explicit authorization.

## 2. Campaign state

```text
SNE-7.9A  Mayor redirect dependency invalidation          COMPLETE / GREEN
SNE-7.9B  Chambermaid stale-target revalidation           COMPLETE / GREEN
SNE-7.9C  canonical night-death resolution                COMPLETE / BROAD GREEN
SNE-7.9D  succession legality / Dawn                      COMPLETE / BROAD GREEN
SNE-7.9E  restore + durable Dawn integration               ACTIVE / CORRECTION RED
```

Do **not** reopen 7.9A–D while finishing 7.9E unless a new typed counterexample directly demonstrates a regression in their accepted authority.

## 3. Accepted baseline you must preserve

### 3.1 7.9C canonical death

Accepted production checkpoint:

```text
b6185ccf6b23583c112b040f831e65f2724f1035
```

Accepted test/branch head:

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

Canonical death path:

```text
confirmed attack / poison / Monk facts
+ stable cards
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
```

No independent Soldier / Monk / Demon-poison / Mayor death gate may be reintroduced in App or Host.

### 3.2 7.9D succession / Dawn legality

7.9D is closed. Important provenance includes:

```text
8bf44fc7  RED — restored successor legality
ba777221  GREEN — reconstructor canonical legality

d396a80d  RED — planner Forced / None
f982e50c  GREEN — planner Forced / None / Choice

b1c1af78  later 7.9D RED slice
06f06fb5c0689983c9baaf0bfe1765a8c69a8d16  broad protective checkpoint
```

Accepted semantic contract:

```text
confirmed Imp self attack
→ canonical attack outcome
→ if attack really self-kills, reconstruct old Imp MechanicalDeath
→ canonical DemonSuccessionResolution
   None   → old Imp death remains; no RoleChanged
   Forced → canonical forced successor
   Choice → only confirmed canonically legal target
→ NightDawnResolutionPlanner owns pending-new-Demon / continuation / Dawn gating
```

A functioning Monk that prevents the self-kill means no reconstructed death and no succession.

## 4. 7.9E work already completed

Do not restart 7.9E from the old “restore does not call reconstructor” audit. That work has advanced substantially.

Earlier 7.9E chain:

```text
58ae...  RED — restored night transaction composition
82cb...  GREEN — restored transaction composition
0f516... test — forced successor restore composition
1a1e... test — expose restored mechanical events
9f5a9aba117a98d1bf18c0cfdb3b0bdffc3a8bb0
          refactor — expose reconstructed night events
40bac754b4e7c6e80e25b1e87b0bc9bf8d04346c
          RED — Host restored succession authority
b97e9486182f8d59820d7c61eaad3eaac5ae68e2
          GREEN — project canonical restored succession events
```

`b97e9486` was focused-GREEN in runner #17 (`33146544493`) and changed only `ClocktowerHostScreen.kt`. CI #923 / R2 #850 had no jobs because the production commit was pushed by the temporary runner token; this was provenance/trigger behavior, not a test failure.

## 5. Accepted pure Dawn durable planner

### RED

```text
674f8da229988a47031c33c3af7453bb6f31980a
test: require idempotent Dawn durable materialization
```

The typed RED introduced:

```text
DawnDurableMaterializationState
NightDawnDurableMaterializationPlanner.plan(...)
```

It required three central scenarios:

1. first materialization emits required state/action/observation/phase work;
2. fully durable replay suppresses all repeats;
3. partial retry where mechanical death is already applied but history is missing performs no remutation while still producing the stable missing Death action and AliveAt observation IDs.

### GREEN

```text
c2fdb820902d9c3bab6f33ebecd04ad90c62153f
feat: plan idempotent Dawn durable materialization
```

Accepted design:

```text
DawnDurableMaterializationState(
    aliveSeats,
    roleIdsBySeat,
    currentPhase,
    committedActionIds,
    committedObservationRecordIds,
)
```

The planner returns separate decisions for:

```text
death:
  stateMutationRequired
  actionIdToCommit
  publicAliveObservationIdToCommit

roleChanges:
  intent
  stateMutationRequired
  actionIdToCommit

phaseAdvance:
  targetPhase
  stateMutationRequired
  actionIdToCommit
```

Stable IDs are a function of `gameId + round + logical effect`, not mutable `clocktowerGameStateRevision`, event counter, callback count, or timeline cursor.

Validation:

```text
CI #925 GREEN — Android FAST + CI gate
R2 #852 SUCCESS
focused runner #18 GREEN
```

Treat this planner as accepted. The remaining defect is App materialization wiring.

## 6. App wiring RED → rejected candidate → current RED

### 6.1 Initial wiring RED

```text
c0075ca6e1a8b4734d2ba7bdc8ea28ffab4a23c4
test: require idempotent Dawn planner wiring
```

CI #926:

```text
production compiled
922 tests
exactly 2 failures — the two newly introduced Dawn planner wiring checks
```

Clean intended RED.

### 6.2 First GREEN candidate — NOT ACCEPTED

```text
af14b913b3b46c97c12b8e40cb8b304bc82f44b6
fix: materialize Dawn durable effects idempotently
```

Focused runner #20 (`33148959735`) succeeded and remote scope showed only `CampBoardGameHostApp.kt` changed. Nevertheless **do not use this as an accepted GREEN checkpoint**.

It introduced useful structure:

- imports `DawnDurableMaterializationState` / `NightDawnDurableMaterializationPlanner`;
- planner-driven Death, RoleChange and PhaseAdvance IDs;
- optional stable record ID in public-alive preflight;
- ability to suppress generic event semantic-history projection;
- separate role-change ActionFact commit from base-card mutation;
- planner-based state-mutation guards.

But a semantic audit found a real durability hole.

### 6.3 Why `af14b913` is wrong

`preflightClocktowerPublicAliveObservation(...)` is intentionally only a **validation seam**. It calls `ClocktowerGameSession.commitGlobalEpistemicObservation(...)` against the current timelines and checks that a candidate global commit can advance safely, but it does **not** assign the returned values back to:

```text
clocktowerEpistemicObservations
clocktowerNextTimelineGlobalSequence
clocktowerPlayerInputRevision
```

The actual durable App append is `recordEpistemicObservation(...)`, which writes back the committed observation log/cursor/revision and publishes durability state.

Before `af14b913`, `addClocktowerEvent(Death/Execution, ...)` performed generic semantic projection including:

```text
Death ActionFact
+ public AliveAt(false) observation
```

`af14b913` correctly tried to avoid duplicate dynamic/revision-based Death ActionFact identity by using `projectSemanticHistory=false` for planner-owned Dawn death. But it then only **preflighted** the planner-owned stable AliveAt observation and never did the real durable append.

Therefore:

```text
preflight SUCCESS ≠ durable AliveAt history commit
```

This is the core remaining correctness defect.

### 6.4 Current supplemental RED

```text
94adcd0febbfcedc7b523249be5cf784a8bc38f2
test: require durable Dawn alive observation commit
```

The new production wiring check explicitly requires a real:

```text
recordEpistemicObservation(...)
```

using:

```text
recordId = dawnDeathMaterialization.publicAliveObservationIdToCommit
```

after the fail-before-mutation validation path.

Normal CI #928 (`33149213389`) produced:

```text
922 tests
3 failures exactly:
1. ClocktowerDawnDurableMaterializationProductionWiringTest
2. ClocktowerGlobalObservationProductionWiringTest
3. ClocktowerHistoricalActionProductionWiringTest
```

This is the current intentional RED checkpoint.

## 7. Runner #21 legacy guard inspection — COMPLETE

Temporary branch:

```text
codex/sne7-9c2c-dawn-focused-20260828
```

Read-only inspection workflow commit:

```text
2f47bad849f87d80c9f31c950fd813d212af0b2e
```

Runner:

```text
#21 / 33149908226 — SUCCESS
```

It inspected the exact current RED head and the two legacy guard sources. The conclusion is important: **these are genuine architecture contracts, not obsolete syntax checks to be deleted blindly.**

### 7.1 `ClocktowerGlobalObservationProductionWiringTest`

This guard requires:

- `recordEpistemicObservation(...)` to remain the shared production durable commit seam;
- GLOBAL_V1 exact duplicates to return before observable list mutation and durability publication;
- action/observation global sequence ownership to stay shared;
- public AliveAt observations to use `EpistemicObservationDraft` + `recordEpistemicObservation(...)`, not direct list mutation;
- public elimination preflight to happen before caller state publication;
- preflight itself to remain non-mutating.

Therefore the fix must preserve both sides:

```text
preflight before state mutation
AND
real durable append through recordEpistemicObservation
```

### 7.2 `ClocktowerHistoricalActionProductionWiringTest`

This guard requires:

- one App-owned `ActionFactTimeline`;
- actions and observations to share the global timeline cursor;
- save/restore to preserve the action timeline without upgrading legacy history;
- confirmed semantic actions to remain replayable, including Death / RoleChange / PhaseAdvance;
- `setClocktowerActualRole(...)` semantic history to precede base-card role mutation.

Do not “solve” the new planner wiring by removing these replayable action pathways or by allowing state mutation to precede the corresponding semantic history contract.

## 8. Exact corrective GREEN direction

The smallest correct Dawn death sequence should preserve this responsibility split:

```text
NightDawnDurableMaterializationPlanner.plan(...)
→ planner owns stable death action ID / stable AliveAt record ID / mutation requirement
→ preflight planner-owned AliveAt(false) against global observation authority
→ commit stable Death ActionFact if actionIdToCommit != null
→ mutate death state only if stateMutationRequired
→ create presentation/UI event without re-enabling generic duplicate semantic projection
→ if publicAliveObservationIdToCommit != null:
     recordEpistemicObservation(
       EpistemicObservationDraft(...,
         recordId = planner-owned stable observation ID
       )
     )
```

The exact sequencing must retain fail-before-public-mutation behavior while ensuring the planner-owned observation is eventually committed durably.

Do **not** simply change the Dawn death call back to generic semantic projection. That would again generate mutable revision/event-counter based Death action identity and defeat exactly-once ownership.

### Partial retry contract

The production wiring must support:

```text
mechanical death already applied
Death ActionFact missing
AliveAt(false) durable observation missing
```

On retry:

```text
stateMutationRequired = false
stable missing ActionFact is committed
stable missing AliveAt observation is committed
no card remutation
no duplicate durable history
```

The same separation applies to RoleChange and PhaseAdvance.

### RoleChange / phase details to preserve

- planner-owned stable `RoleChange` ActionFact must remain replayable;
- base role mutation only when planner says required;
- role semantic history remains before state mutation;
- planner-owned stable PhaseAdvance ActionFact is separate from phase mutation;
- Night→Dawn chronology must remain meaningful even during a partial retry where the base phase may already be Dawn but the durable phase action is missing.

## 9. Files / helpers involved

Primary production target:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

Accepted planner:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/NightDawnDurableMaterializationPlanner.kt
```

Current/new tests:

```text
app/src/test/java/com/codex/campboardgamehost/ClocktowerDawnDurableMaterializationProductionWiringTest.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightDawnDurableMaterializationPlannerTest.kt
```

Legacy guards that **must be included in next focused GREEN**:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerGlobalObservationProductionWiringTest.kt
app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerHistoricalActionProductionWiringTest.kt
```

Relevant App helpers already present after `af14b913` / current RED:

```text
preflightClocktowerPublicAliveObservation(...)
recordEpistemicObservation(...)
recordClocktowerAction(...)
recordClocktowerRoleChangeAction(...)
addClocktowerEvent(..., projectSemanticHistory = ...)
setClocktowerActualRole(..., recordSemanticHistory = ...)
```

Remember: `preflightClocktowerPublicAliveObservation` does not persist returned global observation state. `recordEpistemicObservation` does.

## 10. Next tests-first microcycle

### Step A — keep current RED

Do not add more production scope until the three failures from CI #928 are understood as one coherent correction.

### Step B — minimal App GREEN

Patch current App content (same production content as `af14b913`) from the latest branch head, noting later docs-only commits do not change App/test semantics.

For the large App file use the complete-worktree-safe exact-head runner/edit path required by `AGENTS.md`; do not reconstruct the file from truncated connector content.

### Step C — focused runner must include at minimum

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
ClocktowerHistoricalActionProductionWiringTest
NightDawnDurableMaterializationPlannerTest
ClocktowerGameSessionTest
ClocktowerGlobalObservationCommitTest / equivalent global observation commit tests
ClocktowerHistoricalActionObservationCaptureTest
Demon succession production wiring tests
night restore ownership guard
night transaction architecture guard
NightDawnResolutionPlanner tests
NightTransactionReconstructor focused tests
```

The exact Gradle filter names may be taken from existing runner scripts/tests. The key change from runner #20 is that the two legacy production guards are mandatory.

### Step D — remote audit

After focused GREEN verify:

```text
parent is exact expected live docs/code head
production scope is only the intended App file unless a typed test correction is genuinely required
git diff --check equivalent passes
PR #54 remains draft/open/unmerged
```

Do not weaken legacy guards merely because their source assertion shape is inconvenient. If a guard truly conflicts with a better typed architecture, explain and establish replacement coverage first.

### Step E — close 7.9E only after broader acceptance

After App focused GREEN, still prove the end condition:

```text
uninterrupted night
versus
partially committed night → restore/retry
```

must converge to identical:

```text
mechanical death
current role / successor
Death ActionFact
RoleChange ActionFact
public AliveAt(false) observation
Night→Dawn phase ActionFact
final phase
```

with each durable logical effect present exactly once.

Then run the logical broad gate, including Android full suite/build and semantic/Clingo gates according to current CI policy. Only after that may SNE-7.9E and the wider night-action/information consistency bug campaign be declared fully repaired.

## 11. Existing session idempotence you should use, not replace

`ClocktowerGameSession` already provides the durable duplicate semantics the App planner needs:

- same stable `actionId` + matching action draft returns existing commit without consuming another global slot;
- same stable observation record ID + matching observation draft is idempotent;
- action timeline rejects duplicate action IDs;
- observation log rejects duplicate record IDs/global sequences.

The production gap is therefore not a missing storage model. It is making App Dawn producers use the stable identities consistently and complete both validation and durable publication.

## 12. Scope exclusions

Do not expand into:

```text
end-to-end unrelated Attack/Protect feature expansion
generic non-self Demon-death succession
custom-script Demon succession
A4/B4 authority promotion
recommendation work / ZDD
App-root decomposition
unrelated Host cleanup
PR merge/readiness
```

The currently accepted Trouble Brewing same-night route is the boundary.

## 13. Exact prompt-equivalent continuation instruction

A new conversation can continue with this instruction:

```text
读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md 和
 docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md。
重新确认 live main、draft PR #54 head/state/checks，并区分 docs-only live head 与最后代码 RED checkpoint
94adcd0febbfcedc7b523249be5cf784a8bc38f2。

7.9A/B/C/D 已完成；7.9E 的 restore/reconstructor、Host restored succession authority、以及
NightDawnDurableMaterializationPlanner 已建立并接受。
不要回到旧的 restore-first 计划。

从当前 7.9E App durable Dawn wiring RED 继续。runner #21 已成功检查两个 legacy guards；
必须同时保留 fail-before-mutation public AliveAt preflight、真正的 recordEpistemicObservation durable append、
以及 replayable Death/RoleChange/PhaseAdvance ActionFact ownership。

按 tests-first 做最小 CampBoardGameHostApp.kt GREEN，并把
ClocktowerDawnDurableMaterializationProductionWiringTest、
ClocktowerGlobalObservationProductionWiringTest、
ClocktowerHistoricalActionProductionWiringTest
以及 planner/session/restore/reconstructor focused tests 一起跑。
不要 merge PR，不要扩大到 generic succession、A4/B4/ZDD/recommendation 或无关 Host/UI。
```

That is the authoritative resume point.
