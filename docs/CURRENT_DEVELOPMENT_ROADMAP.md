# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
branch: codex/persistence-simplification
PR #112: open / draft / unmerged
latest validated production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
```

Validation on `5926138...` content:

```text
34178595756 — focused owning tests + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Later commits are validation cleanup and documentation only. Re-query live GitHub before implementation or merge.

## Current priority

> **PS5.2b — SideEffect ownership audit.**

Map every durable mutation that can change `activeGameRecoverySnapshot()`, identify durable transitions currently relying only on later Compose `SideEffect`, then decide whether SideEffect should be retained, narrowed behind a central dirty/retry signal, or replaced by explicit durable ownership.

Do not assume removal is the goal.

Detailed handoff: `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`.

## Frozen Recovery contract

Recovery is current-version-only, 4-hour emergency continuity. Archive is separate. Unsupported/old Recovery fails closed. Restore the game, not the App. Failed writes retain a future retry. A4 cannot release rebuild before persistence succeeds.

## Campaign

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 IN PROGRESS
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit NEXT
```

## PS5.1 safety foundation

`RecoveryWriteGate` supports semantic ordinary-write suppression, real-change writes, forced writes, `retryRequired`, clear/reset, and A4 persistence ordering.

```text
retry GREEN 39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS
```

PS5.1c found a real nested mutable-alias hazard. The gate now remembers timestamp-normalized persisted Recovery representation as immutable content identity.

```text
alias GREEN e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
CI 34177323891 PASS
R2 34177323827 PASS
```

Suppressed ordinary attempts still pay snapshot + serialization identity cost.

## PS5.2a COMPLETE

Old:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

RED `192f031b67c9b4eb46bada4928425f9de332bb4a`; focused RED `34178392065` proved successful pause + unchanged stop wrote twice while retry and changed-content contracts already passed.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause refreshes freshness; unchanged stop deduplicates; failed pause is retried at stop via `retryRequired`; changed durable content still writes. A4 ordering remains unchanged.

PS5.2a net production/test files:

```text
CampBoardGameHostApp.kt
persistence/RecoveryLifecyclePersistence.kt
persistence/RecoveryLifecyclePersistenceTest.kt
```

## Current trigger topology

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

## PS5.2b audit requirements

Before changing SideEffect:

1. enumerate every `activeGameRecoverySnapshot()` input;
2. trace every production mutation owner;
3. map explicit persistence/A4 boundaries;
4. identify durable changes relying only on later SideEffect;
5. separate durable changes from transient recomposition;
6. assess a central dirty/revision signal without scattered UI save calls;
7. prove future retry after failure with no new mutation;
8. preserve A4 persistence-before-release;
9. measure physical `.commit()` and avoidable snapshot/serialization work.

Valid outcomes: retain, guard, or replace SideEffect based on proven correctness and simplicity.

## Validation

```text
behavior RED -> focused tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance only: Android `testFull`, `:app:assembleDebug`, ASP/oracle, real Clingo, R2, exact production-path audit, real-device process-loss/restart acceptance.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work during PS5. D1–D5 are integrated through PR #106; re-plan D6 only after Persistence Simplification is complete and merged.
