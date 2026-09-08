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

Latest validated PS5.2a production-code checkpoint:

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

Later validation-runner cleanup and documentation commits advance the branch head without changing the checkpointed production behavior. Always re-query live GitHub before implementation, validation or merge.

The old D6 branch `codex/d6-ownership-plan` and closed draft PR #111 are historical evidence only. Do not implement that old D6 sequence.

## 2. Current priority — PS5.2b ordinary-trigger / SideEffect ownership audit

PS0–PS4 are complete. PS5 remains **IN PROGRESS**.

Current next step:

> **PS5.2b — audit every durable mutation that can change `activeGameRecoverySnapshot()` and determine whether Compose `SideEffect` can safely be removed, narrowed, or guarded by explicit durable-change ownership.**

Do not begin with the assumption that `SideEffect` must be removed. A valid outcome is to retain it if explicit mutation instrumentation would be broader, more fragile or not meaningfully cheaper.

Detailed handoff:

- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## 3. Frozen product contract — Recent Emergency Recovery

Recovery is **short-horizon emergency continuity**, not a general Save Game product.

Required product behavior:

- one phone hosts the active game;
- in-memory state handles normal background/foreground movement while the process survives;
- disk Recovery exists for process loss, crash, accidental close or equivalent interruption;
- Recovery freshness window remains **4 hours** from the last successful persisted snapshot;
- no next-day continuation promise;
- no cross-version active-game migration framework;
- old persisted formats may be discarded;
- exact Compose/navigation restoration is not required.

Governing rule:

> **Restore the game, not the App.**

Recovery ownership categories remain:

| Category | Recovery policy |
|---|---|
| `DURABLE_GAME_FACT` | Persist |
| `RECOVERY_CONTINUATION` | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Recompute |
| `TRANSIENT_UI` | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Separate owner or retain only proven current-game bookkeeping |

Archive and Recovery remain separate products. Current-format rejection is a fail-closed safety boundary, not a compatibility promise.

## 4. Persistence Simplification campaign status

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

PS4 remains the frozen schema/content foundation:

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

Retained durable Clocktower ownership includes:

- Trouble Brewing setup completion/rotation bookkeeping;
- semantic history and events;
- action timeline;
- epistemic observations;
- current ruleset basis/ref behavior;
- `ClocktowerNightCheckpoint` state/writer;
- mandatory continuations such as pending Klutz/Demon flow;
- ghost-vote and highest-vote durable state.

PS4 final evidence remains:

- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`
- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- static audit `34169917902` — PASS
- stable PS4 `[full-ci]` head `3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0`
- full CI `34170266988` — PASS
- R2 `34170266998` — PASS

## 6. PS5.1 completed safety foundation

### PS5.1a — semantic Recovery write gate

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f  RED
22086dd984fb7992d00e3226d444affb76adb509  gate introduction
dbc4dcbd6b81d27524bb6f6688f49972ef17de4d  cutover
```

### PS5.1b — failed-write retry correctness

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130  RED
39229bfdddba5837a9368706946f62fd94915109  GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

`RecoveryWriteGate` contract now guarantees:

- first durable snapshot physically writes;
- unchanged ordinary attempts may skip physical writes;
- real content changes write;
- `force=true` physically writes;
- any failed physical write sets `retryRequired`;
- retry-required state disables ordinary duplicate suppression until success;
- clear/reset clears gate state;
- A4 cannot release its rebuild before persistence succeeds.

### PS5.1c — mutable-alias safety

Audit found a real nested alias risk: Recovery copies major outer collections, but some epistemic proposition collection fields do not recursively deep-freeze caller-owned collections. A shallow remembered Recovery object graph was therefore unsafe as an equality baseline.

```text
5736951007f66df042cb55d5a2b4122d064cf321  RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23  GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

The gate now stores the timestamp-normalized persisted Recovery representation as immutable content identity instead of retaining a shallow `RecoverySnapshot` graph.

Important performance implication: an ordinary suppressed attempt still constructs the Recovery snapshot and serialized identity even when no `.commit()` occurs.

## 7. PS5.2a — lifecycle duplicate-write reduction — COMPLETE

The prior lifecycle policy was:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

A normal pause→stop transition could therefore issue two identical synchronous physical writes.

A typed lifecycle policy seam was extracted first without behavior change. Tests then fixed three behavioral requirements:

1. successful pause + unchanged stop => one physical write total;
2. failed pause write => stop must retry;
3. durable content changed between pause and stop => stop must write.

RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
test: define lifecycle Recovery write deduplication
```

Focused RED harness `34178392065` proved exactly one expected failure: successful pause followed by stop produced two physical writes instead of one. Retry and changed-content tests remained green.

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

This intentionally retains both lifecycle events:

- successful pause establishes freshness;
- unchanged stop is semantically deduplicated;
- failed pause sets `retryRequired`, so ordinary stop still physically retries;
- changed durable content between events still writes at stop.

Validation:

```text
34178595756
  focused owning tests PASS
  :app:testFast       PASS
  git diff --check    PASS

R2 34178562642        PASS
```

Exact PS5.2a net changed files from the pre-slice documentation head are only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App edit is only lifecycle delegation to the typed helper. `SideEffect`, Recovery schema/content, A4 ordering and domain semantics were not removed.

## 8. Current live trigger topology

After PS5.2a:

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

This is now the authoritative baseline for PS5.2b.

## 9. PS5.2b — SideEffect ownership audit — NEXT

Do **not** remove `SideEffect` until the durable mutation graph is mapped.

Required audit:

1. enumerate every input to `activeGameRecoverySnapshot()`;
2. trace every production mutation owner for those durable inputs;
3. identify which mutations already cross explicit persistence or A4 durability boundaries;
4. identify durable mutations currently guaranteed to persist only because recomposition later reaches `SideEffect`;
5. distinguish durable changes from transient UI recompositions;
6. determine whether a central durable dirty/revision marker can cover all mutation owners without spreading save calls throughout UI code;
7. prove how `retryRequired` receives a future retry when there is no subsequent durable mutation;
8. preserve A4 synchronous persistence-before-release;
9. measure both synchronous physical `.commit()` count and avoidable snapshot/serialization identity construction.

Possible outcomes:

- **retain SideEffect** if it remains the safest broad safety net and its non-commit overhead is acceptable;
- **guard SideEffect with a central dirty/retry signal** if a small complete owner can be proven;
- **replace SideEffect with explicit durable transactions** only if every durable mutation is covered and failure retry remains deterministic.

Avoid a design that simply moves persistence calls into dozens of UI callbacks; that would be more coupled, harder to audit and not a simplification.

## 10. Validation strategy

For each behavioral PS5 slice:

1. behavior-level RED;
2. focused owning tests first;
3. `:app:testFast` at logical GREEN;
4. R2;
5. `git diff --check`;
6. exact changed-file/reference audit;
7. remote-head race check.

Full PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle harness;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance before release-ready.

## 11. Explicit non-goals

PS5 does not include:

- Recovery schema/content redesign;
- cross-version migration;
- Archive redesign;
- Werewolf module removal;
- D6 App/Host decomposition;
- global ViewModel migration;
- DataStore migration merely for modernization;
- long-lived save slots/manual Save/Load UX;
- A4/ZDD production rollout;
- unrelated UI/recommendation redesign.

## 12. Deferred roadmap items

### D6 decomposition

D1–D5 are complete and integrated through PR #106. The old D6 plan assumed the former persistence architecture and is superseded. After Persistence Simplification is complete and merged, re-audit live `main` and write a new D6 ownership plan.

### Werewolf module removal

Still a separate future campaign. Do not mix it into PS5.

> Documentation checkpoint after PS5.2a: branch was advanced only by the two authoritative documentation updates after the validated production GREEN. Re-query live head before PS5.2b implementation.
