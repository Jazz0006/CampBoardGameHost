# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Persistence Simplification started from live `main`:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge pull request #110 — Audit Poisoner execution dusk crash path
```

Active branch / PR:

```text
branch: codex/persistence-simplification
PR:     #112 — Persistence Simplification: recent emergency recovery
state:  open / draft / unmerged
```

Latest validated production-code checkpoint:

```text
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
fix: freeze recovery write identity
```

Validation on that code checkpoint:

```text
CI 34177323891  PASS — Android FAST + CI gate
R2 34177323827  PASS
```

Later documentation-only commits may advance the branch head beyond that SHA without changing checkpointed production behavior. Always re-query live GitHub state before implementation, validation or merge.

The previous D6 branch `codex/d6-ownership-plan` and closed draft PR #111 are historical evidence only. Do not implement the old D6 sequence.

## 2. Current priority — PS5.2 trigger-topology planning

Persistence Simplification PS0–PS4 is complete. PS5 is **IN PROGRESS** and PS5.1 is now complete.

Current next step:

> **PS5.2 — re-audit the live `SideEffect` + lifecycle trigger topology and reduce physical persistence only where retry coverage, A4 durability ordering and actual synchronous `.commit()` reduction are proven.**

PS5.1c found a real mutable-alias risk in nested Recovery content and fixed the gate by remembering an immutable persisted-content identity rather than a shallow `RecoverySnapshot` object graph.

Do not delete `SideEffect`, `ON_PAUSE`, or `ON_STOP` merely to reduce invocation count.

Detailed current PS5 handoff/progress:

- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## 3. Frozen product contract — Recent Emergency Recovery

The app does **not** need a general-purpose long-lived Save Game system.

Supported need:

- one phone is actively hosting the game;
- ordinary background/foreground movement continues from in-memory state while the Android process survives;
- disk Recovery exists only for process loss, crash, accidental close or equivalent interruption;
- Recovery is short-horizon emergency continuity, not normal Save/Load UX;
- stale Recovery window is **4 hours** from the last successful persisted snapshot;
- there is no next-day continuation promise;
- there is no cross-version active-game migration framework;
- old persisted formats may be discarded;
- exact pre-crash App/Compose/UI restoration is not required.

Governing rule:

> **Restore the game, not the App.**

Recovery classification remains:

| Category | Recovery policy |
|---|---|
| `DURABLE_GAME_FACT` | Persist |
| `RECOVERY_CONTINUATION` | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Recompute |
| `TRANSIENT_UI` | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Separate owner or retain only proven current-game bookkeeping |

Clocktower already-published information/history remains durable. Unconfirmed UI selections/drafts are disposable unless a game rule requires a mandatory continuation.

Archive and Recovery remain separate products. Current-format/version rejection is a fail-closed safety boundary, not a cross-version compatibility promise.

## 4. Persistence Simplification campaign status

```text
PS0  product/recovery contract freeze                         COMPLETE
PS1  Archive / active Recovery separation                     COMPLETE
PS2  minimal typed RecoverySnapshot + writer                  COMPLETE
PS3  typed safe Preview/Restore + atomic apply                COMPLETE
PS4  retire superseded active-save infrastructure             COMPLETE
PS5  simplify persistence triggers                            IN PROGRESS
  PS5.0 fresh trigger/ownership audit                         COMPLETE
  PS5.1a semantic Recovery write deduplication                COMPLETE
  PS5.1b failed-write retry correctness                       COMPLETE
  PS5.1c RecoverySnapshot equality/object-graph safety audit  COMPLETE
  PS5.2 further trigger reduction                             NEXT / PLANNING
```

## 5. Stable persistence architecture after PS4

PS4 is complete and must remain the frozen content/schema foundation for PS5.

Current architecture:

```text
live game state
-> typed RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> current-version recent Recovery

raw Recovery
-> current format + exact compatibility token + <=4h validity
-> one strict decoder / RecoveryRestorePlanner
-> prepareCurrentRecoveryPlan(raw)
       ↙                    ↘
    Preview            atomic Restore apply
                         RecoveryApplicationCoordinator

completed game
-> GameArchiveRecord
-> current GameArchiveJsonCodec
```

Retired legacy ownership includes:

```text
activeGameSnapshotJson
LegacyRestoreCompatibility
ActiveGamePersistenceCoordinator / legacy active identity schema
legacy Archive "snapshot" reader
Archive id -> archivedAtMillis fallback
obsolete committed-setup persistence codec
legacy ruleset JSON/restore helpers
```

Retained durable Clocktower ownership includes:

- Trouble Brewing setup completion/rotation bookkeeping;
- semantic history;
- action timeline;
- epistemic observations;
- current ruleset basis/ref behavior;
- `ClocktowerNightCheckpoint` state/writer;
- mandatory continuation state such as pending Klutz/Demon flow.

PS4 final evidence:

- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`
- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- static audit run `34169917902` — PASS
- stable PS4 `[full-ci]` head `3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0`
- full CI `34170266988` — PASS
- R2 `34170266998` — PASS

## 6. PS5 current architecture and completed work

### PS5.0 — fresh trigger audit — COMPLETE

The fresh audit showed the important persistence trigger path is concentrated around:

```text
persistActiveGameStateIfNeeded()
-> persistAndReleaseA4ObservationRebuildIfDurable()
```

Current trigger classes remain:

```text
Compose SideEffect
-> ordinary persistence attempt

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence attempt
```

The important inefficiency was repeated synchronous physical writes during recomposition even when durable game content had not changed.

The audit also confirmed the A4 ordering invariant:

```text
A4 observation becomes durable
-> persistence succeeds
-> durability gate releases
-> cache rebuild may publish
```

Therefore PS5 does not start by deleting triggers. It first suppresses duplicate physical writes while preserving the persistence result consumed by the A4 gate.

### PS5.1a — semantic Recovery write gate — COMPLETE

Tests-first contract:

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f
test: define semantic Recovery write deduplication
```

Implementation lineage:

```text
22086dd984fb7992d00e3226d444affb76adb509
feat: add Recovery semantic write gate

dbc4dcbd6b81d27524bb6f6688f49972ef17de4d
refactor: dedupe semantic Recovery writes
```

`RecoveryWriteGate` means:

- first snapshot physically writes;
- ordinary attempts whose only difference is `savedAtMillis` are deduplicated after a successful durable write;
- real Recovery content changes physically write;
- clearing saved-game state clears the gate;
- lifecycle calls still use `force=true` and physically refresh Recovery;
- A4 still waits for a successful persistence result before release.

`SideEffect` remains in place as the ordinary attempt trigger. PS5.1 reduces physical I/O without yet redesigning trigger ownership.

### PS5.1b — failed forced-write retry correctness — COMPLETE

Safety review found this required behavior:

```text
A successfully persisted
-> forced lifecycle write of A fails
-> next ordinary save of A
-> MUST physically retry
```

RED:

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130
test: pin recovery retry after forced failure
CI 34173747359 — expected Android FAST failure
```

GREEN:

```text
39229bfdddba5837a9368706946f62fd94915109
fix: retry recovery write after forced failure
```

`RecoveryWriteGate` carries `retryRequired`; any failed physical write disables duplicate suppression until a later physical write succeeds.

GREEN validation:

```text
CI 34174011104  PASS — Android FAST + CI gate
R2 34174011121  PASS
```

### PS5.1c — semantic-equality / mutable-alias safety — COMPLETE

The audit found a real reachable alias hazard rather than proving the entire graph deeply immutable.

Key finding:

```text
Recovery outer observation list is copied
-> RecordedEpistemicObservation retains proposition
-> several InformationProposition variants retain caller-supplied Lists
-> shallow gate baseline can share mutable nested content with live/caller state
-> in-place mutation can back-mutate lastDurableContent
-> required write can be suppressed
```

Behavior RED:

```text
5736951007f66df042cb55d5a2b4122d064cf321
test: expose mutable recovery alias suppression
```

Minimal GREEN:

```text
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
fix: freeze recovery write identity
```

The gate now remembers the timestamp-normalized persisted Recovery representation as an immutable String rather than retaining a shallow `RecoverySnapshot` graph. This removes mutable aliasing at the gate boundary while leaving `force`, retry, clear/reset and A4 ordering unchanged.

The broader audit also confirmed:

- `ClocktowerNightCheckpoint` is scalar/value-semantic;
- `ActionFact` is scalar/value-semantic;
- `ActionFactTimeline` defensively snapshots and returns replacement timelines;
- Recovery copies major outer collections but does not recursively deep-freeze them;
- setup-rotation Sets are factory-snapshotted but the record type itself does not guarantee defensive copying;
- ghost-vote authority uses replacement/copy semantics;
- highest-vote state is scalar replacement;
- cards, elimination records and outcome are value objects.

Validation:

```text
CI 34177323891  PASS — Android FAST + CI gate
R2 34177323827  PASS
```

Important PS5.2 carry-forward: duplicate suppression now avoids physical `.commit()` but still constructs a serialized durable identity on each ordinary attempt. Trigger simplification must therefore consider both physical-write count and avoidable main-thread snapshot/serialization work.

## 7. PS5.2 — current next step: further trigger reduction planning

Start from the still-live topology:

```text
Compose SideEffect
-> ordinary persistence attempt
-> RecoveryWriteGate

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence attempt
-> RecoveryWriteGate
```

Do not begin from a predetermined deletion.

Required PS5.2 decisions:

1. **`SideEffect`** — determine whether it should remain the generic ordinary trigger or be replaced/guarded by explicit durable-change ownership. It currently supplies broad coverage and a future retry opportunity, but recomposition can still cause unnecessary snapshot/identity construction.
2. **Durable transaction / dirty marker** — evaluate a small explicit durable revision/dirty boundary so committed game facts, rather than Compose recomposition, drive ordinary persistence. This is acceptable only after every durable mutation owner is covered.
3. **`ON_PAUSE` vs `ON_STOP`** — determine whether two unconditional forced writes are necessary. A candidate worth testing is successful `ON_PAUSE` as the normal lifecycle freshness write with `ON_STOP` retained only as a failed-write retry fallback, rather than a second unconditional successful `.commit()`.
4. **Failure retry** — removing any generic trigger is unsafe unless `retryRequired` is guaranteed another physical attempt even when no further recomposition or game mutation occurs.
5. **A4 ordering** — `persistAndReleaseA4ObservationRebuildIfDurable()` remains a hard durability boundary. Batching/dirty state may not publish A4 rebuild before persistence succeeds.
6. **Actual cost reduction** — acceptance must count physical write callbacks / synchronous `.commit()` calls and also consider snapshot/serialization attempt frequency. Merely moving code or reducing public method calls is not a simplification.

Do **not** remove `SideEffect`, `ON_PAUSE`, or `ON_STOP` merely to reduce call count before these invariants are proven.

## 8. Validation strategy

Persistence is a durability boundary, so high-value behavior/integration tests are justified. Do not use source-string RED ceremony where behavior is unchanged or code is proven dead.

For PS5 behavioral slices:

1. behavioral RED when changing a contract or fixing a correctness bug;
2. focused owning tests;
3. `:app:testFast` at each logical GREEN checkpoint;
4. R2 structural/main-thread boundary gate;
5. `git diff --check`;
6. exact changed-file/reference audit;
7. remote-head race check before/after writes.

Do not run full CI after every small PS5 substep. At the final logical PS5 acceptance checkpoint, run the full persistence T4 gate:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle harness;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit.

Real-device process-loss/restart acceptance remains required before the overall Persistence Simplification campaign is considered release-ready.

## 9. Explicit non-goals

PS5 does **not** include:

- Recovery schema/content redesign;
- cross-version active-game migration;
- Archive redesign;
- deleting the entire Werewolf module;
- D6 App/Host large-file decomposition;
- global ViewModel migration;
- DataStore migration merely for modernization;
- long-lived save slots/manual Save/Load UX;
- recommendation-quality redesign;
- A4/ZDD production rollout;
- unrelated Host/UI redesign.

## 10. Deferred roadmap items

### D6 decomposition

Night Step decomposition D1–D5 is complete and integrated through PR #106. The old D6 plan assumed the former persistence architecture and is superseded. After Persistence Simplification is completed and merged, re-audit live `main` and write a fresh D6 ownership plan.

### Werewolf module removal

Still a separate future campaign. Do not mix it into PS5.
