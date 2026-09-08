# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
branch: codex/persistence-simplification
PR #112: open / draft / unmerged
latest validated production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
```

Validation on `5926138...`:

```text
34178595756 — focused owning tests + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

PS5.2b was audit/docs-only; no production changes were required after `5926138...`.

## Current priority

> **Final PS5 automated acceptance.**

PS5 trigger design is complete. Run the reserved full persistence T4 gate on the current branch, then perform real-device process-loss/restart acceptance before calling the Persistence Simplification campaign release-ready.

Detailed handoff: `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`.

## Frozen Recovery contract

Recovery is current-version-only, 4-hour emergency continuity. Archive is separate. Unsupported/old Recovery fails closed. Restore the game, not the App. Failed writes retain a future retry. A4 cannot release rebuild before successful persistence.

## Campaign

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 IN PROGRESS — final acceptance pending
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
```

## PS5.1 safety foundation

`RecoveryWriteGate` supports semantic ordinary-write suppression, real-change writes, forced writes, `retryRequired`, clear/reset, and A4 persistence ordering.

```text
retry GREEN 39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS

alias GREEN e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
CI 34177323891 PASS
R2 34177323827 PASS
```

PS5.1c found a real nested mutable-alias hazard; the gate now remembers timestamp-normalized persisted Recovery representation as immutable content identity.

## PS5.2a COMPLETE

GREEN production checkpoint:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause refreshes freshness; unchanged stop deduplicates; failed pause is retried at stop via `retryRequired`; changed durable content still writes. A4 ordering remains unchanged.

## PS5.2b COMPLETE — SideEffect retained by design

The ownership audit rejected mechanical SideEffect removal.

Reasons:

1. `activeGameRecoverySnapshot()` spans common state plus Undercover, Werewolf and Clocktower durable state.
2. Undercover and Werewolf have direct durable mutations without a central recovery revision.
3. Clocktower revisions are broad but not universal; persisted mechanics such as ghost-vote authority can change through owners that do not bump the current revision pair.
4. Explicit per-action persistence calls cover only a small subset of transitions; many gameplay mutations intentionally rely on the generic ordinary trigger.
5. A failed ordinary/A4 persistence attempt must retain another foreground opportunity; change-only triggering would require a new retry scheduler/state machine.
6. Physical synchronous `.commit()` duplication is already addressed by `RecoveryWriteGate`, and duplicate lifecycle success writes were reduced in PS5.2a. The remaining snapshot/identity construction cost has not been measured as a performance problem.

Final intended topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

Do not reopen SideEffect removal without profiling evidence. A future measured performance campaign may introduce a universal durable mutation signal if justified, but it is not part of Persistence Simplification.

## Final PS5 automated acceptance

Run once at this logical campaign checkpoint:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle harness;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- `git diff --check` and changed-file audit.

Then perform real-device process-loss/restart acceptance covering at minimum:

- restore a recent active game after process kill/relaunch;
- reject stale/unsupported Recovery as designed;
- resume safe entry point rather than raw transient UI;
- verify already-published Clocktower history/information survives;
- verify mandatory continuation state survives where applicable;
- verify no duplicated lifecycle write behavior causes visible regression.

Only after automated T4 plus real-device acceptance should PS5 / Persistence Simplification be declared release-ready and PR #112 considered for merge.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, DataStore modernization or unrelated UI work during PS5. Re-plan D6 only after Persistence Simplification is complete and merged.
