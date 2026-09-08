# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
branch: codex/persistence-simplification
PR #112: open / draft / unmerged
latest production GREEN: e622960a9c75c0b110d2c92e34b46828cb949e0a
renewed full-T4 checkpoint: 686b52a79cd4ea183c81884d34d54223fecae124
post-hotfix acceptance docs checkpoint: f1e482379143c671a72e3a4f13c08b7d6579a6ec
```

## Current priority

> **Persistence Simplification / PS5 is COMPLETE and release-ready.**

Automated acceptance and real-device acceptance have both passed. PR #112 is therefore ready for merge consideration, but **must not be merged without explicit user authorization**.

After PR #112 is merged, re-audit live `main` and create a fresh D6 ownership/decomposition plan. Do not start D6 against the unmerged persistence branch unless explicitly requested.

Detailed handoff: `docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`.

## Frozen Recovery contract

Recovery is current-version-only, 4-hour emergency continuity. Archive is separate. Unsupported/old Recovery fails closed. Restore the game, not the App. Failed writes retain a future retry. A4 cannot release rebuild before successful persistence.

Final persistence trigger topology remains:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

`SideEffect` is retained by explicit architecture decision. Do not reopen its removal without profiling evidence and a universal durable-mutation ownership design.

## Campaign

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
```

## Final production and validation

Latest production GREEN:

```text
e622960a9c75c0b110d2c92e34b46828cb949e0a
fix: restore seating owner after recovery
```

The device-acceptance hotfix repaired a Recovery ownership gap: a restored active game had valid cards/player identities/game kind but did not reconstruct `HostSeatingSetupFlow`. Recovery now rehydrates confirmed seating plus the selected active game while preserving the existing fail-closed `playerNamesFor()` production-start invariant.

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

Final docs-only verification after recording the hotfix acceptance:

```text
CI 34191972250 — PASS
R2 34191972340 — PASS
```

## Real-device acceptance — COMPLETE

On 2026-09-08 the user confirmed that the previously defined real-device acceptance tests **1–5 all passed**.

The additional regression path that exposed the recovery bug was also retested successfully after the hotfix:

```text
6-player No Greater Joy
-> process-loss / relaunch / Recovery restore
-> continue recovered game
-> Host Tools
-> same players / quick restart
-> new game starts without crash
```

This closes the real-device acceptance blocker. PS5 / Persistence Simplification is release-ready.

## Next development step

1. Keep PR #112 unmerged until explicit authorization.
2. After merge, re-read live `main` and verify the merged head/checks.
3. Re-audit architecture after Persistence Simplification.
4. Create a fresh D6 decomposition/ownership plan; the old D6 plan is superseded by the persistence architecture changes.

## Non-goals until PR #112 merge

No additional Recovery redesign, cross-version migration, Archive redesign, Werewolf removal, D6 implementation, A4/ZDD rollout, DataStore modernization or unrelated UI work on this branch unless explicitly requested.
