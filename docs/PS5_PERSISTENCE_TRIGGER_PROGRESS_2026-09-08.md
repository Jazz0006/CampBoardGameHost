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
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
PR #112: open / draft / unmerged
latest validated production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
```

Validation:

```text
34178595756 — focused RecoveryLifecyclePersistenceTest + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Later commits are validation cleanup/docs-only. Re-query live head before implementation. Full T4 remains reserved for final PS5 acceptance.

## Frozen boundary

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Failed writes must retain a future retry. A4 may not release rebuild before persistence succeeds.

## PS5.1 foundation

`RecoveryWriteGate` provides semantic ordinary-write suppression, real-change writes, `force=true`, `retryRequired`, reset on clear, and A4 persistence ordering.

```text
retry GREEN 39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS
```

PS5.1c found a real nested mutable-alias hazard. The gate now stores timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow Recovery graph.

```text
5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

Suppressed ordinary attempts still pay snapshot + serialization identity cost.

## PS5.2a COMPLETE

Old lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
focused RED run 34178392065
```

Three owning contracts:

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

Successful pause establishes freshness; unchanged stop deduplicates; failed pause sets `retryRequired` so stop retries; real changed content still writes. A4 ordering is unchanged.

PS5.2a net production/test files:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App edit is lifecycle delegation to the typed helper only. `SideEffect`, Recovery schema/content and A4 behavior remain.

## Current topology

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

## PS5.2b NEXT

Do not remove `SideEffect` before completing:

1. `activeGameRecoverySnapshot()` input inventory;
2. production mutation-owner map;
3. explicit persistence/A4 boundary map;
4. durable changes relying only on later SideEffect;
5. durable vs transient recomposition split;
6. central dirty/revision feasibility without scattered UI saves;
7. future retry after failure with no new mutation;
8. A4 persistence-before-release preservation;
9. physical `.commit()` plus snapshot/serialization cost measurement.

Valid outcomes are retain, guard, or replace `SideEffect` based on proven correctness and simplicity.

## Validation route

```text
behavior RED -> focused tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance: Android `testFull`, `:app:assembleDebug`, ASP/oracle, real Clingo, R2, exact production-path audit and real-device process-loss/restart acceptance.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization during PS5.
