# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **This file is the single current project-status and execution-priority authority.**

## Live context

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
branch: codex/persistence-simplification
PR #112: open / draft / unmerged
```

Latest validated production checkpoint:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Validation:

```text
34178595756 — focused tests + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Later commits are validation cleanup and documentation only. Re-query live GitHub before implementation or merge.

## Current priority

> **PS5.2b — audit ordinary persistence ownership and decide the future of Compose `SideEffect`.**

Do not assume `SideEffect` must be removed. Map durable mutation coverage first.

Authoritative detailed handoff:

- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## Frozen Recovery contract

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Restore the game, not the App. A4 cannot release rebuild before successful persistence. Failed writes must retain a deterministic future retry.

## Campaign status

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 IN PROGRESS
  PS5.0  COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit NEXT
```

## Stable persistence foundation

```text
live state
-> RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> recent Recovery

raw Recovery
-> exact current format + token + <=4h freshness
-> strict planner
-> Preview / atomic Restore

completed game
-> separate Archive
```

PS4 remains frozen. Final PS4 evidence: static audit `34169917902`, full CI `34170266988`, R2 `34170266998`, stable `[full-ci]` head `3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0`.

## PS5.1 safety foundation

`RecoveryWriteGate` provides semantic ordinary-write suppression, real-change writes, `force=true`, failed-write `retryRequired`, reset-on-clear and A4 persistence ordering.

Retry GREEN:

```text
39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS
```

PS5.1c found a real nested alias hazard. The gate now remembers timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow Recovery object graph.

```text
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
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

RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
focused RED run 34178392065
```

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
```

Current:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause + unchanged stop now causes one physical write. Failed pause is retried at stop via `retryRequired`; changed durable content still writes at stop. A4 ordering is unchanged.

Net production/test files:

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

Before changing `SideEffect`:

1. enumerate every `activeGameRecoverySnapshot()` input;
2. trace every production mutation owner;
3. map existing explicit persistence and A4 boundaries;
4. identify durable mutations relying only on later SideEffect;
5. separate durable changes from transient recomposition;
6. evaluate a central dirty/revision signal without scattered UI save calls;
7. prove future retry when no new mutation follows a failed write;
8. preserve A4 persistence-before-release;
9. measure physical `.commit()` and avoidable snapshot/serialization work.

Valid outcomes: retain, guard, or replace `SideEffect` based on proven correctness and simplicity.

## Validation

Behavioral slice:

```text
RED -> focused tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance only: Android `testFull`, `:app:assembleDebug`, ASP/oracle, real Clingo, R2, exact production-path audit, then real-device process-loss/restart acceptance.

## Deferred / non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work in PS5. D1–D5 are already integrated through PR #106; re-plan D6 only after Persistence Simplification is completed and merged.
