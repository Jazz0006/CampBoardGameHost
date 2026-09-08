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

## Live checkpoint

```text
base main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

latest validated production GREEN:
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

PR #112 remains open / draft / unmerged. Later branch commits are validation cleanup and documentation only; re-query live head before implementation.

Validation on `5926138...` content:

```text
34178595756
  focused RecoveryLifecyclePersistenceTest PASS
  :app:testFast PASS
  git diff --check PASS
34178562642 R2 PASS
```

Full T4 remains reserved for final PS5 acceptance.

## Frozen Recovery boundary

Recovery remains current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. A4 may not release rebuild before persistence succeeds. Any failed physical write must retain a future retry path.

## PS5.1 foundation

`RecoveryWriteGate` guarantees first-write persistence, semantic duplicate suppression for ordinary attempts, real-change writes, forced writes, `retryRequired` after any failure, reset on clear, and persistence-gated A4 release.

Retry GREEN:

```text
39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS
```

PS5.1c found a real nested mutable-alias hazard in the reachable Recovery graph. The gate therefore stores timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow `RecoverySnapshot` graph.

```text
5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

Carry-forward: suppressed ordinary attempts still construct a Recovery snapshot and serialized identity.

## PS5.2a COMPLETE

Old lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
```

Focused RED run `34178392065` required three owning tests with exactly one failure: successful pause + unchanged stop generated 2 physical writes instead of 1. Retry-after-failed-pause and changed-content-after-pause already passed.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
```

Current lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Therefore successful pause establishes freshness; unchanged stop deduplicates; failed pause sets `retryRequired` so stop retries; changed content still writes at stop.

A4 ordering is unchanged because lifecycle events still use `persistAndReleaseA4ObservationRebuildIfDurable(force=...)`.

Exact PS5.2a production/test net files:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

`CampBoardGameHostApp.kt` only delegates lifecycle policy to the typed helper. `SideEffect`, Recovery schema/content and A4 behavior remain.

## Current trigger topology

```text
SideEffect
-> persistAndReleaseA4ObservationRebuildIfDurable(force=false)
-> RecoveryWriteGate

ON_PAUSE
-> persistAndReleaseA4ObservationRebuildIfDurable(force=true)
-> RecoveryWriteGate

ON_STOP
-> persistAndReleaseA4ObservationRebuildIfDurable(force=false)
-> RecoveryWriteGate
```

## PS5.2b NEXT

Do not delete `SideEffect` before completing this audit:

1. enumerate every input to `activeGameRecoverySnapshot()`;
2. trace every production mutation owner;
3. map existing explicit persistence/A4 boundaries;
4. identify durable mutations depending only on later recomposition/SideEffect;
5. distinguish durable changes from transient recomposition;
6. evaluate a central dirty/revision signal without scattering save calls across UI callbacks;
7. prove a future retry when `retryRequired` is true and no new mutation occurs;
8. preserve A4 persistence-before-release;
9. measure physical `.commit()` calls and avoidable snapshot/serialization work.

Valid outcomes: retain SideEffect, guard it with a proven central dirty/retry signal, or replace it only after complete explicit durable-transaction coverage. Do not optimize for trigger count alone.

## Validation route

For each behavioral slice:

```text
RED -> focused owning tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization in PS5.
