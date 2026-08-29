# PR #56 Handoff — Dusk Poison Expiry Exactly-Once

> Date: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/hotfix-poison-expire-exactly-once`  
> PR: #56 — `Hotfix next-night poison expiry exactly-once`  
> Base: `main@160f730594d76c294542cd22a5220baeb73d1bc9`

## 1. Scope

This P1 follow-up closes the three remaining Day -> Night poison expiry durability gaps found after PR #55.

Authorized owners only:

```text
1. Klutz continuation -> next Night
2. Virgin immediate execution -> next Night
3. normal Day confirmation -> next Night
```

Out of scope:

- Dawn poison behavior already accepted by PR #55;
- Demon succession changes;
- A3 historical replay;
- Host/A4/ZDD;
- generic custom-script work;
- unrelated App-root refactors.

## 2. Original defect

All three callbacks previously did:

```text
recordClocktowerPhaseAdvance(Night, nextRound)
-> round = nextRound
-> phase = Night
-> dynamic clocktowerActionId(kind = "poison-expire", ...)
-> mechanical poison clear
```

Two independent exactly-once failures existed:

### History-first

If `Poison(null)` history persisted but poison state remained stale, retry generated another dynamic action ID and could duplicate poison-clear history.

### Phase-first / state-first

If phase/round entered Night before poison expiry completed, restore no longer had the outgoing Day callback as the owner of the unfinished transition.

The correct transaction boundary is therefore not merely a stable action ID. Poison expiry must converge before next-Night phase state becomes durable.

## 3. Preserved RED

Commit:

```text
4b75bac46a9ef161e7b03e13308339daf56114a4
test: expose next-night poison expiry ownership RED
```

Remote CI #1032:

```text
:app:testFast executed
924 tests completed, exactly 1 failed
DuskPoisonExpiryOwnershipTest > app root no longer owns dynamic poison-expire history identity
```

This is the assertion-level RED provenance for PR #56.

## 4. Typed GREEN seams

### DuskPoisonExpiryMaterializationPlanner

Checkpoint:

```text
67abfa329f06b9a1aed5ee1296b9e82a64ece021
feat: add stable dusk poison expiry planner
```

Stable identity format:

```text
dusk-{stable game id}-{outgoing round}-poison-seat-{previous seat}-to-none
```

Planner input separates:

```text
semantic previousTargetSeat
current mechanical poison target
committed action IDs
```

Planner output separates:

```text
stateMutationRequired
actionIdToCommit
```

Typed tests cover initial materialization, state-first retry, history-first retry, fully durable retry, and no previous poison.

### DuskPoisonExpiryRecoveryAuthority

Checkpoint:

```text
a36b6bf50a7b7d57118ec8605323d52fcbc25881
feat: recover dusk poison expiry across first night
```

`NightDawnPoisonRecoveryAuthority` is not sufficient here because it filters to ordinary `NIGHT`, while Poisoner acts on `FIRST_NIGHT`.

Dusk recovery selects the latest `ActionFact.Poison` for the same outgoing round across First Night / ordinary Night / Day chronology.

This supports:

```text
First Night Poison(target)
-> First Day
-> mechanical clear persists first
-> restore
-> recover previous target from durable history
-> repair the same stable Poison(null) action
```

## 5. Production wiring

Production GREEN:

```text
6b022935618b3d00d5ef2b62a34bc88d8358e645
fix: materialize dusk poison expiry exactly once
```

Parent:

```text
e8b9b449a2564b8eca6696acf974c33a9af956dc
```

The commit changes exactly one tracked file:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

Added one shared App orchestration helper:

```text
materializeClocktowerPoisonExpiryAtDusk()
```

Helper semantics:

1. require current phase is outgoing `Day`;
2. derive current mechanical poison target seat;
3. recover durable previous target when mechanical state is already clear;
4. ask typed planner for state/history repair responsibilities;
5. commit planner-owned stable `ActionFactDraft.Poison(targetSeat = null)` while still Day/current round;
6. clear mechanical poison only when planner requests it.

All three entry points now execute:

```text
materializeClocktowerPoisonExpiryAtDusk()
-> recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
-> round = nextRound
-> clocktowerPhase = ClocktowerPhase.Night
```

The three legacy dynamic `kind = "poison-expire"` blocks were removed.

## 6. Source-wiring characterization

`ClocktowerHistoricalActionLifecycleProductionWiringTest` now protects the architectural contract rather than callback-local implementation details:

- Klutz, Virgin, and normal Day paths call the shared Dusk helper;
- each helper call appears before the next-Night phase advance;
- App root no longer owns dynamic poison-expire identity.

Typed tests remain the correctness authority for convergence semantics.

## 7. Validation

### T1 production checkpoint

```text
CI #1036 / run 33245684173: SUCCESS
- Android :app:testFast: SUCCESS
- CI gate: SUCCESS

R2 #961: SUCCESS
```

### T4 acceptance checkpoint

```text
840ba4eac8d1a0649e787737a8f21d89574e0ea7
[full-ci] docs: record PR56 dusk poison closure
```

Actual T4 evidence:

```text
CI #1037 / run 33245894921: SUCCESS
- Android :app:testFull + :app:assembleDebug: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo 5.8 cross-validation: SUCCESS
- CI gate: SUCCESS

R2 #962 / run 33245894916: SUCCESS
```

The acceptance checkpoint is therefore fully GREEN.

## 8. Final PR audit

Live `main` remained:

```text
160f730594d76c294542cd22a5220baeb73d1bc9
```

At T4 acceptance the branch is:

```text
ahead 6
behind 0
merge base = current main
```

Complete authorized changed-file set before this final docs-only closeout:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/DuskPoisonExpiryMaterializationPlanner.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/DuskPoisonExpiryRecoveryAuthority.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/DuskPoisonExpiryMaterializationPlannerTest.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/DuskPoisonExpiryOwnershipTest.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/DuskPoisonExpiryRecoveryAuthorityTest.kt
app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerHistoricalActionLifecycleProductionWiringTest.kt
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR56_DUSK_POISON_EXPIRY_EXACTLY_ONCE.md
```

No Host/A3/workflow/unrelated production files are included.

No known P1/P2 blocker remains inside authorized PR #56 scope.

## 9. Merge discipline

PR #56 is technically merge-ready in scope, but remains open/draft/unmerged.

Do not:

- merge PR #56;
- mark PR #56 ready;
- rebase;
- force-push;
- widen scope;

without explicit user authorization.

Immediately before any future merge, re-query live `main`, PR head/state/mergeability and checks.
