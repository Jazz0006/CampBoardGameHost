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
production GREEN checkpoint: 6b022935618b3d00d5ef2b62a34bc88d8358e645
original RED checkpoint: 4b75bac46a9ef161e7b03e13308339daf56114a4
full T4 checkpoint: 840ba4eac8d1a0649e787737a8f21d89574e0ea7
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

## 3. PR #56 — next-night poison expiry exactly-once — ACCEPTED IN SCOPE

### Defect closed

The three Day -> Night paths previously owned separate retry-unsafe dynamic poison clear writers:

```text
1. Klutz continuation -> next Night
2. Virgin immediate execution -> next Night
3. normal Day confirmation -> next Night
```

Each generated `clocktowerActionId(kind = "poison-expire", ...)` and separately cleared mechanical poison after entering Night.

The hotfix closes both failure modes:

1. duplicate history identity after partial persistence;
2. loss of the outgoing Day callback as retry owner when phase advanced before poison expiry converged.

### Preserved RED

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

### Typed ownership chain

```text
DuskPoisonExpiryRecoveryAuthority
-> DuskPoisonExpiryMaterializationPlanner
-> DuskPoisonExpiryMaterializationPlan
-> stable dusk-{game}-{outgoingRound}-poison-seat-{seat}-to-none action ID
-> shared App helper materializeClocktowerPoisonExpiryAtDusk()
```

Typed convergence contract:

```text
initial expiry -> stable history + mechanical clear
state-first retry -> missing history only
history-first retry -> mechanical clear only
fully durable retry -> no-op
no previous poison -> no materialization responsibility
```

### First Night recovery

Dusk recovery is intentionally separate from Dawn recovery because Poisoner acts on First Night.

`DuskPoisonExpiryRecoveryAuthority` uses the latest Poison fact for the same outgoing round across First Night / ordinary Night / Day chronology. This preserves state-first recovery at the first Day -> Night boundary.

### Production GREEN

```text
6b022935618b3d00d5ef2b62a34bc88d8358e645
fix: materialize dusk poison expiry exactly once
```

This commit changed exactly one tracked file:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

All three Day -> Night entry points now execute:

```text
materializeClocktowerPoisonExpiryAtDusk()
-> recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
-> round = nextRound
-> clocktowerPhase = ClocktowerPhase.Night
```

Poison expiry therefore converges while phase/round still belong to the outgoing Day.

The three dynamic `kind = "poison-expire"` App writers are removed.

### T1 production validation

```text
CI #1036 / run 33245684173: SUCCESS
- Android :app:testFast: SUCCESS
- CI gate: SUCCESS

R2 #961: SUCCESS
```

### Full T4 acceptance

Docs-only full checkpoint:

```text
840ba4eac8d1a0649e787737a8f21d89574e0ea7
[full-ci] docs: record PR56 dusk poison closure
```

T4 evidence:

```text
CI #1037 / run 33245894921: SUCCESS
- Android :app:testFull + :app:assembleDebug: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo 5.8 cross-validation: SUCCESS
- CI gate: SUCCESS

R2 #962 / run 33245894916: SUCCESS
```

No known P1/P2 blocker remains inside the authorized PR #56 scope after this audit.

PR #56 is technically merge-ready in scope, but it remains draft/unmerged until explicit user authorization.

## 4. Final PR #56 audit summary

Relative to `main@160f7305...`, the acceptance checkpoint is:

```text
ahead: 6
behind: 0
merge base: 160f730594d76c294542cd22a5220baeb73d1bc9
```

Authorized changed-file classes only:

```text
1 App root production file
2 Dusk typed production seam files
4 focused/characterization test files
2 docs files
```

No Host/A3/workflow/unrelated production files are part of the PR.

## 5. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED / NEXT MAINLINE CANDIDATE AFTER PR56 |
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

Source inspection is allowed only as a coarse architecture/ownership guard. For PR #56 it protects:

- no dynamic `kind = "poison-expire"` remains in App root;
- all three entry points share the same Dusk materialization helper;
- the helper call precedes the next-Night phase advance.

It does not substitute for typed convergence tests.

## 8. Branch / scope discipline

At this checkpoint:

- PR #56 remains draft and unmerged;
- do not rebase or force-push;
- do not widen scope;
- do not mark ready or merge without explicit user authorization;
- use GitHub connector for safe small/medium files;
- use Luna/Codex for huge protected App-root edits.

## 9. Current next action

```text
1. Confirm final docs-only closeout CI/R2.
2. STOP before merge/ready.
3. If user explicitly authorizes merge, re-query live main + PR head/checks immediately before merge.
4. After merge, re-query new main and then decide whether to resume A3 Architecture Hardening or another explicitly chosen deferred item.
```
