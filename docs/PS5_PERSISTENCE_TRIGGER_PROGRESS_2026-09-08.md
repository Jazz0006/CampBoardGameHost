# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IN PROGRESS — PS5.2a COMPLETE; PS5.2b SideEffect ownership audit NEXT**

## Campaign

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

## Checkpoint

```text
base main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

latest validated production GREEN:
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

PR #112 remains open / draft / unmerged. Later branch commits are validation cleanup and docs-only. Re-query live state before implementation.

Validation:

```text
34178595756 — focused RecoveryLifecyclePersistenceTest + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Full T4 is reserved for final PS5 acceptance.

## Frozen correctness boundary

Recovery remains current-version-only, 4-hour emergency continuity. Archive is separate. Unsupported/old Recovery fails closed. Failed physical writes must retain a future retry. A4 cannot release rebuild before successful persistence.

## PS5.1 safety foundation

`RecoveryWriteGate` provides semantic ordinary-write suppression, real-change writes, `force=true`, failed-write `retryRequired`, reset on clear, and A4 persistence ordering.

Retry checkpoint:

```text
39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS
```

PS5.1c found a real nested mutable-alias hazard in the reachable Recovery graph. The gate therefore stores timestamp-normalized persisted Recovery representation as immutable content identity instead of a shallow Recovery object graph.

```text
5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

Suppressed ordinary attempts still pay snapshot + serialization identity cost.

## PS5.2a COMPLETE

Old policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
focused RED 34178392065
```

Three contracts:

1. successful pause + unchanged stop => one physical write;
2. failed pause => stop retries;
3. changed durable content after pause => stop writes.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
```

Current policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause establishes freshness; unchanged stop deduplicates; failed pause sets `retryRequired` so stop retries; changed content still writes. A4 ordering is unchanged.

PS5.2a net production/test files:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App change is only delegation to the typed lifecycle helper. `SideEffect`, Recovery schema/content and A4 behavior remain.

## Current trigger topology

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

## PS5.2b NEXT

Do not remove `SideEffect` before completing the owner map:

1. enumerate every `activeGameRecoverySnapshot()` input;
2. trace every production mutation owner;
3. map explicit persistence/A4 boundaries;
4. identify durable mutations relying only on later SideEffect;
5. distinguish durable changes from transient recomposition;
6. assess a central dirty/revision signal without scattered UI save calls;
7. prove future retry after failure with no new mutation;
8. preserve A4 persistence-before-release;
9. measure physical `.commit()` and avoidable snapshot/serialization work.

Valid outcomes are retain, guard, or replace `SideEffect` according to proven correctness and simplicity.

## Validation route

```text
behavior RED -> focused tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance: Android `testFull`, `:app:assembleDebug`, ASP/oracle, real Clingo, R2, exact production-path audit, and real-device process-loss/restart acceptance.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work in PS5.
