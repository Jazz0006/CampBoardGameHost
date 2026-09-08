# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

```text
base main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

branch: codex/persistence-simplification
PR: #112 — open / draft / unmerged
```

Latest validated production checkpoint:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Validation:

```text
34178595756 — focused owning tests + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Later commits are validation cleanup and documentation only. Re-query live GitHub before implementation or merge.

## 2. Current priority

> **PS5.2b — SideEffect ownership audit.**

Map every durable mutation that can change `activeGameRecoverySnapshot()`, identify which ones currently depend on a later Compose `SideEffect` attempt, and decide whether `SideEffect` should be retained, narrowed behind a central dirty/retry signal, or replaced by explicit durable ownership.

Do not assume it must be removed.

Detailed evidence and handoff:

- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## 3. Frozen Recovery product contract

Recovery is short-horizon emergency continuity, not general Save Game UX.

- process-loss/crash/accidental-close recovery;
- 4-hour freshness from latest successful persisted snapshot;
- no next-day continuation promise;
- no cross-version active-game migration framework;
- exact Compose/navigation restoration is unnecessary;
- Archive remains independent.

> **Restore the game, not the App.**

| Category | Policy |
|---|---|
| `DURABLE_GAME_FACT` | Persist |
| `RECOVERY_CONTINUATION` | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Recompute |
| `TRANSIENT_UI` | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Separate owner or proven current-game bookkeeping only |

## 4. Campaign status

```text
PS0  product/recovery contract freeze                          COMPLETE
PS1  Archive / Recovery separation                             COMPLETE
PS2  typed RecoverySnapshot + writer                           COMPLETE
PS3  typed Preview/Restore + atomic apply                      COMPLETE
PS4  retire superseded active-save infrastructure              COMPLETE
PS5  simplify persistence triggers                             IN PROGRESS
  PS5.0  fresh trigger/ownership audit                         COMPLETE
  PS5.1a semantic write deduplication                          COMPLETE
  PS5.1b failed-write retry correctness                        COMPLETE
  PS5.1c Recovery equality / mutable-alias safety              COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction        COMPLETE
  PS5.2b ordinary trigger / SideEffect ownership audit         NEXT
```

## 5. Stable persistence foundation

```text
live state
-> RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> recent current-version Recovery

raw Recovery
-> current format + compatibility token + <=4h validity
-> strict restore planner
-> Preview or atomic Restore apply

completed game
-> independent GameArchiveRecord
```

PS4 remains frozen. Retained durable Clocktower ownership includes setup rotation bookkeeping, semantic history/events, action timeline, epistemic observations, ruleset basis, night checkpoint, mandatory continuations, ghost-vote and highest-vote state.

PS4 final evidence:

```text
static audit 34169917902 PASS
full CI 34170266988 PASS
R2 34170266998 PASS
stable [full-ci] head 3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0
```

## 6. PS5.1 safety foundation

### Semantic gate + retry

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f RED
39229bfdddba5837a9368706946f62fd94915109 retry GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

Gate contract:

- first snapshot physically writes;
- unchanged ordinary attempt may suppress physical write;
- real durable change writes;
- `force=true` writes;
- failure sets `retryRequired`;
- retry-required disables suppression until success;
- clear/reset resets gate state;
- A4 release remains persistence-gated.

### Mutable-alias safety

A real reachable alias hazard was found in nested durable collections, so the gate no longer retains a shallow Recovery object graph.

```text
5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

The gate remembers timestamp-normalized persisted Recovery representation as immutable content identity.

Carry-forward: suppressed ordinary attempts still pay snapshot + serialization identity cost.

## 7. PS5.2a lifecycle reduction — COMPLETE

Old policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
```

Focused RED run `34178392065` proved exactly one failure: successful pause + unchanged stop wrote twice. Retry-after-failure and changed-content-between-events tests already passed.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
```

Current lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Thus successful pause refreshes freshness, unchanged stop deduplicates, failed pause is retried at stop through `retryRequired`, and real content changes still write.

A4 ordering is unchanged.

PS5.2a net production/test files:

```text
CampBoardGameHostApp.kt
persistence/RecoveryLifecyclePersistence.kt
persistence/RecoveryLifecyclePersistenceTest.kt
```

## 8. Current live trigger topology

```text
SideEffect
-> ordinary persistAndReleaseA4ObservationRebuildIfDurable(force=false)
-> RecoveryWriteGate

ON_PAUSE
-> force=true
-> RecoveryWriteGate

ON_STOP
-> force=false
-> RecoveryWriteGate
```

## 9. PS5.2b audit requirements

Before changing `SideEffect`:

1. enumerate every `activeGameRecoverySnapshot()` input;
2. trace its production mutation owner;
3. map explicit persistence and A4 boundaries;
4. identify durable changes relying only on later SideEffect;
5. separate durable changes from transient recomposition;
6. assess a central dirty/revision signal without scattered UI save calls;
7. prove future retry with no new mutation;
8. preserve A4 persistence-before-release;
9. measure physical `.commit()` and avoidable snapshot/serialization work.

Valid outcomes are retain, guard, or replace `SideEffect`; choose based on proven simplicity/correctness, not call-count aesthetics.

## 10. Validation

Each behavioral slice:

```text
RED -> focused tests -> :app:testFast -> R2 -> git diff --check -> exact audit -> remote-head race check
```

Final PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle;
- real Clingo cross-validation;
- R2;
- exact production-path audit;
- real-device process-loss/restart acceptance.

## 11. Non-goals / deferred work

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work during PS5.

D1–D5 are integrated through PR #106. Re-plan D6 only after Persistence Simplification is completed and merged.
