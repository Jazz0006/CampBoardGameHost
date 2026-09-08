# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
branch: codex/persistence-simplification
PR #112: open / draft / unmerged
latest production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
full-T4 checkpoint: 88249af2e68064b571da7cec5395c80940cbe021
```

No production/test changes occurred after `5926138...` through the full-T4 checkpoint; later commits are acceptance documentation only.

## Current priority

> **Real-device process-loss/restart acceptance for Persistence Simplification.**

PS5 implementation/design and automated T4 acceptance are complete. Do not start another persistence refactor. Validate the current Recovery contract on a real Android device, then decide release readiness / PR #112 merge separately.

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
PS5 IN PROGRESS — real-device acceptance pending only
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
  final automated T4 COMPLETE
  real-device process-loss/restart acceptance PENDING
```

## Latest validation

PS5.2a production GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
34178595756 — focused owning tests + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

Final reserved T4, triggered by docs-only `[full-ci]` checkpoint `88249af...`:

```text
CI 34179926099 — PASS
  Android testFull + :app:assembleDebug PASS
  ASP contract/golden tests             PASS
  Real Clingo 5.8 cross-validation      PASS
  CI gate                               PASS

R2 34179926105 — PASS
```

The accepted production diff had literal `git diff --check` PASS in `34178595756`; the later full-T4 checkpoint changed documentation only.

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

Final lifecycle policy:

```text
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
5. Failed ordinary/A4 persistence needs another foreground opportunity; change-only triggering would require a new dirty/retry scheduler.
6. Duplicate physical synchronous `.commit()` is already suppressed by `RecoveryWriteGate`, and PS5.2a removes the normal duplicate successful pause/stop write. Remaining snapshot/identity construction is not a measured performance problem.

Final intended topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

Do not reopen SideEffect removal without profiling evidence.

## Real-device acceptance — required before release-ready

Exercise at minimum:

- recent active-game restore after process kill/relaunch;
- safe Recovery entry rather than raw transient UI restoration;
- cards/round/eliminations/outcome and game-specific durable mechanics;
- already-published Clocktower history/information;
- at least one supported mandatory continuation;
- stale/unsupported Recovery rejection;
- ordinary background pause -> stop path after the PS5.2a lifecycle change;
- retry behavior through a failure-injection path if available.

Only after this passes should PS5 / Persistence Simplification be marked release-ready and PR #112 considered for merge. Do not merge without explicit authorization.

## Deferred roadmap

After Persistence Simplification is complete and merged, re-audit live `main` and create a fresh D6 ownership/decomposition plan. The old D6 plan is superseded by the persistence architecture changes.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 implementation, A4/ZDD rollout, DataStore modernization or unrelated UI work during PS5.
