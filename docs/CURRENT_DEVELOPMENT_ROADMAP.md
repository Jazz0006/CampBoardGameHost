# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline: 160f730594d76c294542cd22a5220baeb73d1bc9
main meaning: PR #55 merged — Dawn poison exactly-once materialization
post-merge main T4: CI #1031 SUCCESS

active branch: codex/hotfix-poison-expire-exactly-once
active PR: #56 — Hotfix next-night poison expiry exactly-once
PR state at this roadmap checkpoint: open / draft / unmerged / mergeable
current production GREEN head before this docs checkpoint: 6b022935618b3d00d5ef2b62a34bc88d8358e645
original RED checkpoint: 4b75bac46a9ef161e7b03e13308339daf56114a4
```

Detailed PR #56 handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR56_DUSK_POISON_EXPIRY_EXACTLY_ONCE.md
```

## 2. PR #55 — MERGED / ACCEPTED

PR #55 merged into `main` at:

```text
160f730594d76c294542cd22a5220baeb73d1bc9
```

It established exactly-once Dawn poison materialization for both ordinary Dawn and successor-Dawn paths through:

```text
NightDawnPoisonRecoveryAuthority
-> NightDawnResolutionPlanner
-> DawnPoisonCarryIntent
-> NightDawnDurableMaterializationPlanner
-> stable planner-owned ActionFact.Poison identity
```

Accepted convergence:

```text
state-first -> repair missing history only
history-first -> repair mechanical state only
fully durable -> no duplicate history/state mutation
unchanged carry -> no redundant Poison action
```

Post-merge main validation:

```text
CI #1031 / run 33244894925: SUCCESS
- Android :app:testFull: SUCCESS
- :app:assembleDebug: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo 5.8 cross-validation: SUCCESS
- CI gate: SUCCESS
```

PR #55 is no longer active work.

## 3. PR #56 — next-night poison expiry exactly-once — CURRENT P1

### Defect

Three Day -> Night paths previously owned separate retry-unsafe dynamic poison clear writers:

```text
1. Klutz continuation -> next Night
2. Virgin immediate execution -> next Night
3. normal Day confirmation -> next Night
```

Each generated:

```text
clocktowerActionId(kind = "poison-expire", ...)
```

and separately cleared mechanical poison state after entering Night.

This had two durability problems:

1. dynamic action identity could duplicate history after history-first partial persistence;
2. phase could become durable before poison expiry completed, losing the outgoing Day callback as retry owner.

### RED provenance

```text
4b75bac46a9ef161e7b03e13308339daf56114a4
test: expose next-night poison expiry ownership RED
```

Remote CI #1032 compiled production/tests and ran `:app:testFast`:

```text
924 tests completed, exactly 1 failed
DuskPoisonExpiryOwnershipTest > app root no longer owns dynamic poison-expire history identity
```

This is the preserved assertion-level RED for PR #56.

### Typed materialization owner

PR #56 now introduces:

```text
DuskPoisonExpiryRecoveryAuthority
-> DuskPoisonExpiryMaterializationPlanner
-> DuskPoisonExpiryMaterializationPlan
-> stable dusk-{game}-{outgoingRound}-poison-seat-{seat}-to-none action ID
```

The typed contract covers:

```text
initial expiry -> stable history + mechanical clear
state-first retry -> missing history only
history-first retry -> mechanical clear only
fully durable retry -> no-op
no previous poison -> no materialization responsibility
```

### First Night recovery

Dusk recovery is intentionally separate from Dawn recovery.

Poisoner acts on First Night, so `DuskPoisonExpiryRecoveryAuthority` accepts the latest Poison fact for the same outgoing round across First Night / ordinary Night / Day chronology. This preserves state-first recovery at the first Day -> Night boundary.

### Production GREEN

```text
6b022935618b3d00d5ef2b62a34bc88d8358e645
fix: materialize dusk poison expiry exactly once
```

This commit changed only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

App root now has one shared helper:

```text
materializeClocktowerPoisonExpiryAtDusk()
```

All three Day -> Night entry points execute:

```text
materializeClocktowerPoisonExpiryAtDusk()
-> recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
-> round = nextRound
-> clocktowerPhase = ClocktowerPhase.Night
```

Therefore poison expiry is materialized while phase/round still belong to the outgoing Day.

Legacy dynamic `kind = "poison-expire"` writers were removed from all three paths.

### Current validation

Remote production checkpoint:

```text
CI #1036 / run 33245684173: SUCCESS
- Android :app:testFast: SUCCESS
- CI gate: SUCCESS
- full Android route: correctly skipped at FAST checkpoint
- ASP / Real Clingo: correctly skipped at FAST checkpoint

R2 #961: SUCCESS
```

PR #56 is not yet merge-accepted. A dedicated `[full-ci]` docs checkpoint must run T4 before merge review.

## 4. PR #56 acceptance gates still required

Before PR #56 can be considered merge-ready:

```text
1. docs/handoff checkpoint with [full-ci]
2. Android :app:testFull
3. :app:assembleDebug
4. ASP contract tests
5. Real Clingo 5.8 cross-validation
6. CI gate
7. R2
8. final PR/global diff audit
9. verify no remaining P1/P2 blocker in authorized scope
```

Do not merge or mark ready without explicit user authorization.

## 5. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED / RESUME AFTER PR56 |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
| generic custom-script Demon succession | NOT AUTHORIZED |
| Mayor redirect to Demon with generic succession | DELIBERATELY CONSTRAINED |
| Host/A4/ZDD recommendation promotion | NOT AUTHORIZED |
| history UI / generic misinformation tuning | NOT CURRENT |

Each deferred item must be re-audited against live `main` before implementation.

## 6. Testing policy

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

```text
T0 focused typed RED
-> preserve assertion-level RED provenance when required
-> minimal GREEN
-> focused affected regressions
-> T1 :app:testFast
-> conservative escalation for persistence/history/transaction work
-> T4 full acceptance before merge-blocking closure
```

A skipped, cached-only or `UP-TO-DATE` route is not evidence that a required gate executed.

## 7. Source-string test policy

Gameplay/rules correctness belongs to typed tests.

Source inspection is allowed only as a coarse architecture/ownership guard. For PR #56 it may assert:

- no dynamic `kind = "poison-expire"` remains in App root;
- all three entry points share the same Dusk materialization helper;
- the helper call precedes the next-Night phase advance.

It must not substitute for typed convergence tests.

## 8. Branch / scope discipline

At this checkpoint:

- PR #56 remains draft and unmerged;
- do not rebase or force-push;
- do not fold unrelated Dawn/A3/Host/A4/ZDD work into PR #56;
- use GitHub connector for safe small/medium files;
- use Luna/Codex for huge protected App-root edits;
- keep exact changed-file audits at each production checkpoint.

## 9. Current next action

```text
1. Commit this roadmap + PR56 handoff as a docs-only [full-ci] checkpoint.
2. Verify T4 actually executes and passes.
3. Update acceptance evidence if necessary.
4. Perform final PR/global audit.
5. STOP before merge/ready unless explicitly authorized.
```

Do not resume A3 architecture hardening until PR #56 is integrated or explicitly paused.
