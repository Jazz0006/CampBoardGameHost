# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline: 57b61a6a7d5be375612c2ec3590ff84518c9f277
main meaning: PR #54 merged — Same-night correctness and GCR hardening

active branch: codex/hotfix-dawn-poison-exactly-once
active PR: #55 — Hotfix Dawn poison exactly-once materialization
PR state at this roadmap checkpoint: open / draft / unmerged / mergeable
current PR head before this docs update: 5953afaac311bcfb404f71e99a539578513236a2
latest production checkpoint: 996201046e53de4395aaf555f50ae0f04a70b5ba
full T4 checkpoint: aa0d9cb8526cef2f96894505dee9fd173e8f8bbd
```

The dedicated PR #55 handoff remains the detailed acceptance record:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_PR55_DAWN_POISON_EXACTLY_ONCE.md
```

## 2. PR #54 — MERGED / ACCEPTED

PR #54 was merged into `main` at:

```text
57b61a6a7d5be375612c2ec3590ff84518c9f277
```

Accepted behavior includes the same-night correctness and GCR hardening work, including:

- First-night Fortune Teller uses base/current role authority instead of entering Other Night chronology projection.
- Pending Imp self-kill succession can reconstruct the current-night transaction safely while preserving the distinction between living-Demon UI authority and current-night reconstruction authority.
- GCR-1 current Demon authority is accepted.
- GCR-2 poisoned Spy fail-safe information policy is accepted.
- GCR-3 source-string retirement is accepted.
- SNE-7 remains broadly green.

Historical PR54 acceptance details remain in the existing PR54/GCR/SNE handoffs and Git history. They are no longer the active branch state.

## 3. PR #55 — Dawn poison exactly-once hotfix — FULLY ACCEPTED IN SCOPE

### Accepted defect contract

Dawn poison is now represented as an explicit semantic transition:

```text
DawnPoisonCarryIntent(
    previousTargetSeat,
    targetSeat,
)
```

Semantics:

- `previousTargetSeat == targetSeat` means unchanged carry;
- `previousTargetSeat != targetSeat` means a durable transition;
- `targetSeat == null` can represent an explicit clear;
- `poisonCarry == null` means no Dawn poison materialization responsibility, not an implicit clear.

### Durable ownership chain

Both successor Dawn and ordinary Night -> Dawn poison handling now use the typed chain:

```text
NightDawnPoisonRecoveryAuthority
-> NightDawnResolutionPlanner
-> DawnPoisonCarryIntent
-> NightDawnDurableMaterializationPlanner
-> planner-provided stable actionIdToCommit
-> ActionFactDraft.Poison
```

Mechanical state repair and history repair are planned independently.

Therefore the accepted convergence contract is:

```text
state-first partial persistence
-> repair missing history only

history-first partial persistence
-> repair mechanical state only

fully durable replay
-> no duplicate state mutation and no duplicate history

unchanged carry
-> no redundant Dawn Poison action
```

### Production checkpoint

```text
996201046e53de4395aaf555f50ae0f04a70b5ba
commit: fix: materialize ordinary Dawn poison exactly once
```

The obsolete ordinary Dawn writer using:

```text
poisonCarriedIntoTomorrow
clocktowerActionId(kind = "poison-after-night", ...)
```

has been removed from production.

### Full acceptance

Docs-only full checkpoint:

```text
aa0d9cb8526cef2f96894505dee9fd173e8f8bbd
```

T4 evidence:

```text
CI #1028 / run 33244041684: SUCCESS
- Android :app:testFull: SUCCESS
- :app:assembleDebug: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo cross-validation: SUCCESS
- CI gate: SUCCESS

R2 #954 / run 33244041658: SUCCESS
```

Final docs-only closeout head before this roadmap update:

```text
5953afaac311bcfb404f71e99a539578513236a2
CI #1029: SUCCESS
R2 #955: SUCCESS
```

No known P1/P2 runtime blocker remains **inside the authorized PR #55 Dawn scope**.

## 4. Newly promoted repository-global P1 — next-night poison expiry exactly-once

The final global PR55 audit found three remaining next-night poison expiry writers in `CampBoardGameHostApp.kt`.

Current owners:

```text
1. Klutz continuation -> next Night
2. Virgin immediate execution -> next Night
3. normal Day confirmation -> next Night
```

All three still generate poison clear history through the legacy shape:

```text
clocktowerActionId(
    kind = "poison-expire",
    actionRound = round,
    localSequence = ...,
)
```

and then separately clear:

```text
clocktowerPoisonTarget
clocktowerConfirmedPoisonTarget
```

This is now classified as:

```text
P1 — repository-global poison lifecycle durability gap
```

Reason: the transition can still be split across phase advancement, history materialization and mechanical poison state. The dynamic identity does not provide the same state-first/history-first convergence guarantee now established for Dawn.

This was pre-existing and deliberately excluded from PR #55. It does not invalidate PR #55 acceptance, but it is the immediate next correctness task after PR #55 integration.

### Required next hotfix scope

Create a separate branch from the post-PR55 `main` and tests-first unify all three next-night expiry entries behind one stable semantic materialization owner.

Minimum required RED/GREEN matrix:

```text
A. normal Day -> Night poison expiry
B. Virgin execution -> Night poison expiry
C. Klutz continuation -> Night poison expiry
D. state-first partial persistence repairs missing Poison(null) history
E. history-first partial persistence repairs mechanical poison state only
F. fully durable retry is a no-op
G. all three entry points share the same stable identity contract
H. legacy dynamic "poison-expire" App writers are removed
```

Do not fold this work back into PR #55 unless a new audit proves PR55 itself cannot safely merge without it.

## 5. P3 / future robustness

### No-current-Poisoner ordinary Dawn recovery

Ordinary Dawn currently discovers a card whose current role is `Poisoner` before invoking `planPoisonCarry`.

For current Trouble Brewing production flows this is not a known blocker:

- a Poisoner killed during the night retains the Poisoner role and is still discoverable;
- post-death effective state correctly marks the dead Poisoner mechanically inactive;
- Poisoner -> Imp succession is handled by the dedicated successor-Dawn path.

Re-audit this assumption before generic role-changing custom scripts are promoted.

Status:

```text
P3 / FUTURE CUSTOM-SCRIPT ROBUSTNESS
```

## 6. Deferred work registry

| Deferred area | Status |
|---|---|
| next-night `poison-expire` exactly-once hardening | **P1 — NEXT** |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED / RESUME AFTER HOTFIX |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
| generic custom-script Demon succession | NOT AUTHORIZED |
| Mayor redirect to Demon with generic succession | DELIBERATELY CONSTRAINED |
| Host/A4/ZDD recommendation promotion | NOT AUTHORIZED |
| history UI / generic misinformation tuning | NOT CURRENT |

Each deferred item must be re-audited against live `main` before implementation.

## 7. Testing policy

Follow `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Correctness work uses:

```text
T0 focused typed RED
-> preserve assertion-level RED provenance when required
-> minimal GREEN
-> focused affected regressions
-> T1 :app:testFast at logical checkpoint
-> T2/T3 when dependency/external scope requires it
-> T4 full acceptance before merge-blocking production closure
```

Persistence/schema, transaction boundaries, history identity and central orchestration justify conservative escalation.

A skipped, cached-only or `UP-TO-DATE` route is not evidence that a required gate executed.

## 8. Source-string test policy

Gameplay and rules correctness must be typed.

Source inspection is allowed only as a coarse architecture/ownership guard where there is no callable production seam. It must not freeze incidental callback-local variable names or substitute for behavioral tests.

For poison lifecycle hardening:

- typed tests own transition identity and convergence semantics;
- a coarse source guard may assert that legacy dynamic `poison-expire` writers are absent after migration.

## 9. Branch / scope discipline

At this roadmap checkpoint:

- PR #55 remains unmerged until its docs update CI is confirmed green;
- do not rebase or force-push PR #55;
- do not add the three `poison-expire` changes to PR #55 merely for convenience;
- after PR #55 merge, create a separate P1 branch from the new live `main`;
- preserve complete-worktree safety for `CampBoardGameHostApp.kt` and other huge protected files;
- use the GitHub connector for safe small/medium files and Luna/Codex for huge protected App-root edits.

## 10. Current next action

```text
1. Confirm this roadmap-only PR55 checkpoint CI/R2.
2. Merge PR #55 after the checkpoint is green.
3. Re-query live main and verify the PR55 merge commit.
4. Create a separate P1 poison-expire exactly-once hotfix branch from that main.
5. Start tests-first with a typed RED before changing production behavior.
```

Do not resume A3 architecture hardening until the new P1 poison-expire hotfix is either integrated or explicitly paused.
