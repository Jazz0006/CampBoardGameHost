# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Persistence Simplification started from:

```text
main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge pull request #110 — Audit Poisoner execution dusk crash path
```

Active work:

```text
branch: codex/persistence-simplification
PR:     #112 — Persistence Simplification: recent emergency recovery
state:  open / draft / unmerged
```

Latest validated production-code checkpoint:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Validation:

```text
PS5 lifecycle GREEN one-shot 34178595756
  focused owning tests PASS
  :app:testFast       PASS
  git diff --check    PASS
R2 34178562642        PASS
```

Later commits are validation cleanup and documentation only. Always re-query live GitHub before implementation, validation or merge.

The old D6 branch `codex/d6-ownership-plan` and closed draft PR #111 are historical evidence only.

## 2. Current priority — PS5.2b SideEffect ownership audit

PS0–PS4 are complete. PS5 remains **IN PROGRESS**.

> **NEXT: map every durable mutation that can change `activeGameRecoverySnapshot()` and determine whether Compose `SideEffect` should be retained, narrowed behind a central dirty/retry signal, or replaced by explicit durable ownership.**

Do not assume `SideEffect` must be removed. Retaining it is valid if explicit mutation instrumentation is broader, more fragile or not meaningfully cheaper.

Detailed handoff:

- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## 3. Frozen Recent Emergency Recovery contract

Recovery is short-horizon emergency continuity, not general Save Game UX.

- one phone hosts the active game;
- disk Recovery exists for process loss/crash/accidental close;
- freshness remains **4 hours** from the latest successful persisted snapshot;
- no next-day continuation promise;
- no cross-version active-game migration framework;
- exact Compose/navigation restoration is not required;
- Archive remains separate.

Governing rule:

> **Restore the game, not the App.**

| Category | Recovery policy |
|---|---|
| `DURABLE_GAME_FACT` | Persist |
| `RECOVERY_CONTINUATION` | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Recompute |
| `TRANSIENT_UI` | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Separate owner or proven current-game bookkeeping only |

## 4. Campaign status

```text
PS0  product/recovery contract freeze                          COMPLETE
PS1  Archive / active Recovery separation                      COMPLETE
PS2  minimal typed RecoverySnapshot + writer                   COMPLETE
PS3  typed safe Preview/Restore + atomic apply                 COMPLETE
PS4  retire superseded active-save infrastructure              COMPLETE
PS5  simplify persistence triggers                             IN PROGRESS
  PS5.0  fresh trigger/ownership audit                         COMPLETE
  PS5.1a semantic Recovery write deduplication                 COMPLETE
  PS5.1b failed-write retry correctness                        COMPLETE
  PS5.1c RecoverySnapshot equality/object-graph safety audit   COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction        COMPLETE
  PS5.2b ordinary trigger / SideEffect ownership audit         NEXT
```

## 5. Stable persistence architecture

```text
live game state
-> typed RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> current-version recent Recovery

raw Recovery
-> exact current format + compatibility token + <=4h validity
-> strict decoder / RecoveryRestorePlanner
-> Preview or atomic Restore apply

completed game
-> independent GameArchiveRecord
```

PS4 remains the frozen schema/content foundation. Retained Clocktower durable ownership includes setup rotation bookkeeping, semantic history/events, action timeline, epistemic observations, ruleset basis, night checkpoint, mandatory continuations, ghost-vote and highest-vote state.

PS4 evidence:

- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`
- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- static audit `34169917902` PASS
- `[full-ci]` head `3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0`
- full CI `34170266988` PASS
- R2 `34170266998` PASS

## 6. PS5.1 completed safety foundation

### PS5.1a semantic gate

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f  RED
22086dd984fb7992d00e3226d444affb76adb509  gate introduction
dbc4dcbd6b81d27524bb6f6688f49972ef17de4d  cutover
```

### PS5.1b retry correctness

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130  RED
39229bfdddba5837a9368706946f62fd94915109  GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

Gate contract:

- first snapshot physically writes;
- unchanged ordinary attempt may suppress physical write;
- real durable change writes;
- `force=true` writes;
- failed write sets `retryRequired`;
- retry-required disables suppression until success;
- clear/reset resets the gate;
- A4 release remains persistence-gated.

### PS5.1c mutable-alias safety

A real nested alias hazard was found: major outer Recovery collections are copied, but nested epistemic proposition collections are not universally deep-frozen. The gate therefore must not retain a shallow Recovery object graph as equality baseline.

```text
5736951007f66df042cb55d5a2b4122d064cf321  RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23  GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

The gate now stores timestamp-normalized persisted Recovery representation as immutable content identity.

Carry-forward: suppressed ordinary attempts still pay snapshot + serialization identity cost.

## 7. PS5.2a lifecycle duplicate-write reduction — COMPLETE

Old policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
test: define lifecycle Recovery write deduplication
```

Focused RED harness `34178392065` proved exactly one expected failure: successful pause followed by unchanged stop physically wrote twice. Tests proving stop retry after pause failure and stop write after real content change were already green.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Current policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Consequences:

- successful pause refreshes freshness;
- unchanged stop is deduplicated;
- failed pause leaves `retryRequired`, so stop retries;
- durable content changed after pause still writes at stop.

A4 ordering is unchanged because both lifecycle events still use `persistAndReleaseA4ObservationRebuildIfDurable(force=...)`.

Validation:

```text
34178595756
  focused tests PASS
  :app:testFast PASS
  git diff --check PASS
R2 34178562642 PASS
```

PS5.2a net production/test files:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App edit is only lifecycle delegation to the typed helper. `SideEffect`, Recovery schema/content and A4 behavior remain.

## 8. Current live trigger topology

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

## 9. PS5.2b required audit

Do not delete `SideEffect` until all of these are answered:

1. enumerate every input to `activeGameRecoverySnapshot()`;
2. trace every production mutation owner;
3. map existing explicit persistence/A4 boundaries;
4. identify durable mutations relying only on later recomposition/SideEffect;
5. distinguish durable changes from transient UI recomposition;
6. evaluate a central dirty/revision signal without scattering save calls through UI callbacks;
7. prove future retry when `retryRequired` is true and no new mutation occurs;
8. preserve A4 synchronous persistence-before-release;
9. measure physical `.commit()` count and avoidable snapshot/serialization work.

Valid outcomes:

- retain SideEffect if safest and cheap enough;
- guard SideEffect with a proven central dirty/retry signal;
- replace it with explicit durable transactions only if coverage is complete.

Avoid moving persistence into dozens of UI callbacks; that is not simplification.

## 10. Validation strategy

For each behavioral PS5 slice:

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

## 11. Non-goals / deferred work

PS5 does not include Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work.

D1–D5 are already integrated through PR #106. A fresh D6 ownership plan comes only after Persistence Simplification is completed and merged.
