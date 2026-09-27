# SDE pre-RH-E repository cleanup checkpoint — 2026-09-27

> Status: **historical checkpoint / not current execution authority**
>
> Current execution authority remains `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and `docs/NEXT_DEVELOPMENT_HANDOFF.md`.

## 1. Purpose

This checkpoint records the repository-cleanup state immediately before RH-E runtime persistence/timing hardening and preserves details removed from the active roadmap/handoff during compaction.

## 2. Accepted engineering lineage

- SDE-3A: COMPLETE / PR #151 and #152 merged.
- SDE-3B / `BEGINNER_CONSERVATIVE_V1`: COMPLETE / PR #153 merged.
- SDE-3C DecisionTrace / shadow replay: COMPLETE historical checkpoint.
- CR-A / CR-B / CR-C: COMPLETE; combined validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0`, CI #3459 and R2 #3212 GREEN.
- C4 / SDE-3D2: COMPLETE; formal executable checkpoint `21206a8ca95896ffad83eaba5695a3a571c6cd74`, tree `a4701e0f4873ddd629dfa11163a94d2aa0cb9b8b`; validation-only PR #160 exact-tree evidence passed CI #3473 and R2 #3226.
- IF-D durable App replay capture/rebuild: COMPLETE; formal executable checkpoint `4d1b6d7f39529402eb9ec1e6032eb80ef9a14e86`, tree `181a1d80d449c2d443a678e10033acda13c777d1`; validation-only PR #161 exact-tree evidence passed CI #3476 and R2 #3229.
- No later production/test executable diff exists between the IF-D formal checkpoint and the pre-RH-E cleanup branch state; the later changes were documentation cleanup only.

Historical validation-only PRs #154, #156, #158, #160 and #161 were closed without merge after their evidence had been incorporated into the formal continuation lineage.

## 3. Recovery product boundary and PR #109 audit

Recovery is intentionally a **short-horizon emergency continuation mechanism**, not a durable game-save product and not a cross-version migration surface.

Current contract:

```text
live App state
-> current RecoverySnapshot format
-> exact current compatibility token
-> <= 4 hour eligibility window
-> prepareCurrentRecoveryPlan
-> validated atomic apply
```

`RecoveryValidityPolicy` explicitly rejects non-current format versions and mismatched compatibility tokens. It deliberately does not recreate older-format compatibility.

PR #109 reproduced an old/abnormal half-state in which a persisted `public-alive` observation had sequence 8 while the persisted event list ended at sequence 7. Current production no longer exposes a persistence window that can write that half-state:

1. execution/day action mutation, event insertion, action projection and public-alive observation projection execute synchronously on the UI thread within one event callback;
2. `activeGameRecoverySnapshot()` is not physically written from inside the intermediate event/observation operations;
3. ordinary persistence is triggered after composition by `SideEffect`, with lifecycle last-chance writes on `ON_PAUSE` / `ON_STOP`;
4. those lifecycle callbacks run on the main thread and cannot interleave inside an already-running UI callback;
5. each physical Recovery write stores one complete encoded `RecoverySnapshot` under one SharedPreferences key using synchronous `commit()`;
6. `RecoveryWriteGate` does not remember failed writes as durable and retries after failure.

Therefore the #109 reproduction is not a supported current-format state that production must migrate or repair. Adding tolerant legacy reconstruction would expand the product contract and increase persistence complexity without serving the intended emergency-recovery use case.

Disposition: close #109 as obsolete current-product evidence. Do not add legacy event/observation migration or tolerant repair. If a future current-version crash produces an inconsistent Recovery snapshot, treat that as a new current-format atomicity defect with fresh reproduction evidence.

## 4. Test-maintenance checkpoint

The 2026-09-27 test-cost retirement is complete:

- FAST and FULL both pass after retirement;
- FULL retains every intentional bounded Android JVM regression test;
- expensive healthy-bundle/topology/calibration evidence remains callable through named manual/T3 tasks;
- no ordinary regression was removed solely for runtime cost;
- no TODO/FIXME/HACK production markers were found in the pre-RH-E audit.

## 5. Branch / PR cleanup classification

Keep until deliberately resolved or archived:

- `codex/sde-history-prefix-route-closure` — active continuation / Draft PR #157;
- `sde-2d4-player-count-performance-audit` — closed PR #148 but contains unique historical metrics/audit commits;
- `docs/beginner-storyteller-mode-policy` — contains two unique historical product-strategy documents not present on the continuation.

Safe historical branch cleanup candidates include merged PR branches and obsolete validation/RED-only branches whose evidence is already preserved in merged history or archived checkpoints.

Branch deletion is repository hygiene only; it must not be confused with merge or acceptance.

## 6. Current continuation boundary

The continuation branch remains intentionally divergent from live `main` because live main contains the later merged M8G5 workflow/control-plane commit. Main synchronization/rebase/conflict resolution remains a separate integration action and is not required before RH-E.

## 7. Next executable slice

RH-E remains the only open short-horizon SDE integration-hardening slice:

- serialized background diagnostic persistence lane;
- atomic/idempotent append + post-commit correlation;
- evaluation / persistence / total latency accounting;
- slow-storage, backlog and cancellation coverage;
- bounded retention/storage growth;
- canonical session commit must remain first and independent of diagnostic persistence success.

C5 / policy V2 / production cutover remains blocked on genuinely qualifying E3/E4 evidence.
