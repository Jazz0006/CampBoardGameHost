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

Latest validated production-code checkpoint before the current documentation commits:

```text
39229bfdddba5837a9368706946f62fd94915109
fix: retry recovery write after forced failure
```

Validation on that code checkpoint:

```text
CI 34174011104  PASS — Android FAST + CI gate
R2 34174011121  PASS
```

The current documentation commits advance the branch head beyond that SHA without changing the checkpointed production behavior. Always re-query live GitHub state before implementation, validation or merge.

The previous D6 branch `codex/d6-ownership-plan` and closed draft PR #111 are historical evidence only. Do not implement the old D6 sequence.

## 2. Current priority — PS5.1c Recovery write-gate safety audit

Persistence Simplification PS0–PS4 is complete. PS5 is now **IN PROGRESS**.

Current next step:

> **PS5.1c — prove `RecoveryWriteGate` semantic equality is safe against mutable nested-state aliasing before reducing any more save triggers.**

Do not delete `SideEffect`, `ON_PAUSE`, or `ON_STOP` yet.

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
  PS5.1c RecoverySnapshot equality/object-graph safety audit  NEXT / IN PROGRESS
  PS5.2 further trigger reduction                             NOT STARTED
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

`RecoveryWriteGate` now means:

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

`RecoveryWriteGate` now carries `retryRequired`; any failed physical write disables duplicate suppression until a later physical write succeeds.

GREEN validation:

```text
CI 34174011104  PASS — Android FAST + CI gate
R2 34174011121  PASS
```

## 7. PS5.1c — current next step: semantic-equality safety audit

Before relying more heavily on the write gate or removing trigger points, prove the complete object graph reachable from `RecoverySnapshot` is safe for structural equality.

Risk to exclude:

```text
lastDurableContent and new snapshot share mutable nested state
-> nested object mutates in place
-> both snapshots observe the mutation
-> equality reports unchanged
-> required physical write is skipped
```

Audit at minimum:

- Clocktower night checkpoint state;
- action timeline / action facts;
- epistemic observations;
- semantic event/history collections;
- Trouble Brewing setup-rotation record;
- ghost-vote / highest-vote durable state;
- committed cards/records/outcome and other nested Recovery value objects.

Do not treat top-level `data class` equality as sufficient proof.

If mutable aliasing is found:

1. establish a behavioral RED that proves a missed physical write;
2. fix the gate identity/snapshotting strategy narrowly;
3. focused persistence tests;
4. `:app:testFast`;
5. R2 / `git diff --check` / exact reference audit;
6. remain in PS5.1 until green.

If the object graph is proven safe, record the proof in `PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`, then plan PS5.2 from the live trigger topology.

## 8. PS5.2 — further trigger reduction — NOT STARTED

Only after PS5.1c is complete should these questions be answered:

1. Should `SideEffect` remain the ordinary trigger, or should durable transactions explicitly mark/write Recovery?
2. Are both `ON_PAUSE` and `ON_STOP` necessary last-chance physical writes?
3. Can business-event persistence replace recomposition-driven attempts without missing durable transitions?
4. How is a failed write guaranteed a future retry if no further recomposition occurs?
5. Does any reduction preserve A4 persistence-before-release ordering?

Do **not** remove `SideEffect`, `ON_PAUSE`, or `ON_STOP` merely to reduce call count before these invariants are proven.

## 9. Validation strategy

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

## 10. Explicit non-goals

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

## 11. Deferred roadmap items

### D6 decomposition

Night Step decomposition D1–D5 is complete and integrated through PR #106. The old D6 plan assumed the former persistence architecture and is superseded. After Persistence Simplification is completed and merged, re-audit live `main` and write a fresh D6 ownership plan.

### Werewolf module removal

Still a separate future campaign. Do not mix it into PS5.

### UI-R5 real-device stabilization

Still open. Persistence process-loss/restart acceptance remains part of release readiness.

### EPI-MQ / Beginner Mode / UX-R6

Deferred until structural/recovery work is stable. Existing product direction remains unchanged.

### A4 / ZDD

Remain non-production. Persistence work protects existing A4 durability ordering but does not authorize production rollout.

## 12. Current references

Current execution authority:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — this file;
- `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` — current PS5 progress/handoff.

Persistence completion/history evidence:

- `docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md`;
- `docs/PS1_ARCHIVE_RECOVERY_SEPARATION_CHECKPOINT_2026-09-07.md`;
- `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`;
- `docs/PS3_TYPED_SAFE_RESTORE_CHECKPOINT_2026-09-07.md`;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md` — historical PS4 route;
- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md` — completed PS4 progress record;
- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md` — authoritative PS4 completion evidence.

Long-lived engineering authority:

- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Prior D1–D5/D6 planning remains historical support only; PR #111 is superseded evidence.

## 13. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution, architecture and test rules;
3. this roadmap controls current project state, product boundary and priority;
4. `PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` controls the detailed current PS5 checkpoint/next slice;
5. PS4 final/progress documents are frozen completion evidence;
6. umbrella Persistence Simplification docs control historical campaign intent where non-conflicting;
7. specialized design docs control their own semantic/product domain where non-conflicting;
8. old branches, superseded handoffs and historical PR records are evidence only.

## 14. Next conversation start point

The next conversation should:

1. read root `AGENTS.md`;
2. read `docs/TESTING_STRATEGY.md`;
3. read this roadmap;
4. read `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`;
5. re-query live `main`, PR #112, branch head and checks;
6. distinguish code checkpoint `39229bfdddba5837a9368706946f62fd94915109` from later docs-only commits;
7. continue **PS5.1c RecoverySnapshot value-semantics / mutable-alias safety audit first**;
8. if unsafe, fix the write gate tests-first; if safe, record proof and then design PS5.2 from live trigger topology;
9. do not merge PR #112 without explicit authorization;
10. do not start Werewolf removal, D6, A4/ZDD or unrelated UI work.
