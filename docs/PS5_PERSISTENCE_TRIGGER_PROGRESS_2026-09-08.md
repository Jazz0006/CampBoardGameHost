# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IN PROGRESS — PS5.2a COMPLETE; PS5.2b ordinary-trigger audit is NEXT**

## 1. Campaign position

```text
PS0  Recovery product contract                         COMPLETE
PS1  Archive / Recovery separation                     COMPLETE
PS2  typed Recovery writer                             COMPLETE
PS3  typed safe Preview/Restore + atomic apply         COMPLETE
PS4  legacy persistence cleanup                        COMPLETE
PS5  persistence-trigger simplification                IN PROGRESS
  PS5.0  fresh trigger/ownership audit                 COMPLETE
  PS5.1a semantic Recovery write deduplication         COMPLETE
  PS5.1b failed-write retry correctness                COMPLETE
  PS5.1c RecoverySnapshot equality/object-graph audit  COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b ordinary trigger / SideEffect ownership audit NEXT
```

PS4 completion evidence remains frozen in `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md` and `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`.

## 2. Live repository / checkpoint state

```text
main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

PR #112:
open / draft / unmerged
base main @ ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

Latest validated production-code GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Validation:

```text
one-shot 34178595756
  focused RecoveryLifecyclePersistenceTest PASS
  :app:testFast PASS
  git diff --check PASS

R2 34178562642 PASS
```

Later commits are validation-runner cleanup and documentation only. Always re-query live branch head before implementation; do not treat a docs SHA as a new production checkpoint.

The ordinary PR CI on `5926138...` was cancelled only because the branch immediately advanced to the temporary validation commit. The one-shot runner executed full `:app:testFast` on the same GREEN content and passed. Bot cleanup commits may show `action_required` because GitHub does not recursively trigger workflows from bot pushes; that is not a test failure.

Full T4 remains reserved for final PS5 acceptance.

## 3. Frozen correctness boundary

PS5 changes when/how often Recovery is attempted or physically written. It does not redesign PS4 Recovery payload/schema.

Frozen constraints:

- current-version-only emergency continuity;
- 4-hour freshness;
- unsupported/old Recovery fails closed;
- Archive remains independent;
- failed physical writes retain a future retry path;
- A4 cannot release rebuild before persistence succeeds;
- trigger reduction must reduce real synchronous work, not merely relocate it.

## 4. PS5.0 — trigger audit — COMPLETE

At campaign start:

```text
Compose SideEffect -> ordinary persistence attempt
ON_PAUSE / ON_STOP -> force=true lifecycle attempt
```

A4 invariant:

```text
A4 durable observation
-> persistence succeeds
-> durability gate releases
-> rebuild may publish
```

## 5. PS5.1 — write-gate safety foundation — COMPLETE

### PS5.1a semantic duplicate suppression

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f RED
22086dd984fb7992d00e3226d444affb76adb509 gate introduction
dbc4dcbd6b81d27524bb6f6688f49972ef17de4d cutover
```

### PS5.1b failed-write retry

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130 RED
39229bfdddba5837a9368706946f62fd94915109 GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

Gate contract:

- first snapshot physically writes;
- unchanged ordinary attempt may suppress write;
- real durable change writes;
- `force=true` writes;
- failed write sets `retryRequired`;
- retry-required disables suppression until success;
- clear/reset resets gate state;
- A4 release remains persistence-gated.

### PS5.1c mutable-alias safety

Audit found a real reachable nested alias hazard: major outer Recovery collections are copied, but nested epistemic proposition collections are not universally deep-frozen. A shallow remembered `RecoverySnapshot` was unsafe as equality baseline.

```text
5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

The gate now stores timestamp-normalized persisted Recovery representation as immutable content identity. The audit also covered night checkpoint, action facts/timeline, semantic history/events, setup rotation, ghost vote/highest vote, cards, eliminations, outcome and other nested Recovery values.

Carry-forward: even a suppressed ordinary attempt still pays snapshot + serialization identity cost.

## 6. PS5.2a — pause/stop duplicate physical write — COMPLETE

Old lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

The lifecycle policy was first extracted behind a typed helper without changing behavior.

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
test: define lifecycle Recovery write deduplication
```

Owning contracts:

1. successful pause + unchanged stop => one physical write;
2. failed pause write => stop physically retries;
3. durable content changed after pause => stop physically writes.

Focused RED runner `34178392065` required exactly one failure: the first contract produced expected 1 / actual 2 writes. The retry and changed-content tests passed, proving the RED was specific.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Current lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Therefore:

- pause remains the lifecycle freshness write;
- unchanged stop is suppressed;
- failed pause leaves `retryRequired`, so stop retries;
- real durable change between pause and stop still writes.

A4 ordering remains unchanged because both events still pass through `persistAndReleaseA4ObservationRebuildIfDurable(force=...)`.

GREEN validation:

```text
34178595756
  exact allowlist PASS
  git diff --check PASS
  focused owning tests PASS
  :app:testFast PASS
R2 34178562642 PASS
```

PS5.2a net production/test files from the pre-slice head:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App change is only lifecycle delegation to the typed helper. `SideEffect`, Recovery schema/content and A4 behavior remain.

## 7. Current live trigger architecture

```text
Compose SideEffect
-> persistAndReleaseA4ObservationRebuildIfDurable(force = false)
-> RecoveryWriteGate

ON_PAUSE
-> persistAndReleaseA4ObservationRebuildIfDurable(force = true)
-> RecoveryWriteGate

ON_STOP
-> persistAndReleaseA4ObservationRebuildIfDurable(force = false)
-> RecoveryWriteGate
```

## 8. PS5.2b — SideEffect ownership audit — NEXT

Do not delete `SideEffect` yet.

Required audit:

1. enumerate every input to `activeGameRecoverySnapshot()`;
2. trace every production mutation owner;
3. map explicit persistence and A4 boundaries;
4. identify durable mutations relying only on later recomposition/SideEffect;
5. distinguish durable changes from transient UI recomposition;
6. evaluate a central durable dirty/revision signal without scattering save calls across UI callbacks;
7. prove future retry when `retryRequired` is true and no new mutation occurs;
8. preserve A4 synchronous persistence-before-release;
9. measure physical `.commit()` count and avoidable snapshot/serialization work.

Valid outcomes:

- retain SideEffect if it remains safest and cheap enough;
- guard it with a proven central dirty/retry signal;
- replace it with explicit durable transactions only if coverage is complete.

Avoid moving persistence into dozens of UI callbacks; that would increase coupling rather than simplify persistence.

## 9. Validation route

Each behavioral slice:

1. behavior RED;
2. focused owning tests;
3. `:app:testFast` at GREEN;
4. R2;
5. `git diff --check`;
6. exact changed-file/reference audit;
7. remote-head race check.

Final PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance.

## 10. Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization in PS5.

## 11. Next start point

Re-read the four authority files, re-query live GitHub, distinguish production GREEN `5926138...` from later docs commits, then continue PS5.2b SideEffect ownership audit. Do not merge PR #112 without explicit authorization.
