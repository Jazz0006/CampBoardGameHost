# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
live main: 1b502c75357a2de7c928c88668e6a9613521b4ac
merged PR: #112 Persistence Simplification: recent emergency recovery
merged branch head: 18e7be6b915d654ea6bbfa6a11ff5627fe467153
latest production GREEN inside merged history: e622960a9c75c0b110d2c92e34b46828cb949e0a
renewed post-hotfix full-T4 checkpoint: 686b52a79cd4ea183c81884d34d54223fecae124
```

PR #112 is **merged / closed**. Persistence Simplification / PS5 is complete on `main`.

## Current priority

> **D6 post-persistence architecture re-audit and decomposition planning.**

Do **not** resume an old D6 implementation plan mechanically. Persistence Simplification changed ownership boundaries around Recovery, lifecycle persistence, restart/recovery seating ownership, Archive separation and A4 durability. The next development step is therefore a fresh audit of the merged `main`, followed by a new D6 ownership/decomposition plan.

New-conversation handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

Historical PS5 handoff:

`docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## Persistence Simplification / PS5 — COMPLETE

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 COMPLETE
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
  original final automated T4 COMPLETE
  recovery quick-restart hotfix COMPLETE
  renewed post-hotfix T4 COMPLETE
  real-device process-loss/restart acceptance COMPLETE
  PR #112 MERGED
```

Final Recovery contract remains frozen unless a future task deliberately reopens it:

- current-version-only, 4-hour emergency continuity;
- Archive remains a separate contract;
- unsupported/old Recovery fails closed;
- failed writes retain a future retry opportunity;
- A4 cannot release rebuild before persistence succeeds;
- restore the game, not raw transient UI state.

Final persistence trigger topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

`SideEffect` is retained by explicit architecture decision. Do not reopen its removal without profiling evidence and a universal durable-mutation ownership design.

## Final PS5 production / validation evidence

Latest production GREEN:

```text
e622960a9c75c0b110d2c92e34b46828cb949e0a
fix: restore seating owner after recovery
```

The final real-device-discovered bug was a Recovery ownership gap: a restored active game had valid cards/player identities/game kind but did not reconstruct `HostSeatingSetupFlow`. Recovery now rehydrates confirmed seating plus the selected active game while preserving the existing fail-closed `playerNamesFor()` production-start invariant.

Tests-first hotfix evidence:

```text
RED 3bc389349bba00a155d55ec01508d6b271125ff0
CI 34191093216 — expected FAIL
R2 34191093300 — PASS

helper GREEN d8e1123d1535672dc04271ab41e18ec979ccaa7f
product GREEN e622960a9c75c0b110d2c92e34b46828cb949e0a
one-shot 34191324182 — PASS
  focused HostSeatingRosterTest PASS
  :app:testFast PASS
  exact App-only product diff audit PASS
  git diff --check PASS
```

Renewed post-hotfix reserved T4:

```text
checkpoint 686b52a79cd4ea183c81884d34d54223fecae124
CI 34191738571 — PASS
  Android testFull + :app:assembleDebug PASS
  ASP contract/golden tests             PASS
  Real Clingo 5.8 cross-validation      PASS
  CI gate                               PASS
R2 34191738649 — PASS
```

Final pre-merge docs verification:

```text
CI 34192745929 — PASS
R2 34192745940 — PASS
```

## Real-device acceptance — COMPLETE

On 2026-09-08 the previously defined real-device acceptance tests **1–5 all passed**.

The additional regression path that exposed the recovery bug was also retested successfully after the hotfix:

```text
6-player No Greater Joy
-> process-loss / relaunch / Recovery restore
-> continue recovered game
-> Host Tools
-> same players / quick restart
-> new game starts without crash
```

## D6 entry criteria / next actions

Start from live `main` `1b502c75357a2de7c928c88668e6a9613521b4ac` or its verified descendant.

1. Re-read root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.
2. Reconfirm live `main` before any branch/write.
3. Audit current large-file and ownership boundaries after the persistence merge, especially `CampBoardGameHostApp.kt` and the extracted persistence/recovery owners.
4. Re-evaluate which remaining responsibilities genuinely belong together; do not optimize only for file size.
5. Produce a fresh D6 decomposition sequence with dependency order, invariants, tests-first boundaries and rollback-safe checkpoints.
6. Only after the audit/plan is accepted should D6 implementation begin on a fresh branch.

The old D6 plan is historical reference only and is superseded where it conflicts with the merged architecture.

## Current non-goals

Until the D6 re-audit defines otherwise, do not mix D6 with new Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, A4/ZDD feature rollout, unrelated UI work or DataStore modernization.
