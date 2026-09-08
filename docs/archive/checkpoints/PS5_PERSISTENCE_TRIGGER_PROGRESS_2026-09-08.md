# PS5 Persistence Trigger Simplification — Final Handoff

> Date: 2026-09-08 Australia/Sydney
> Historical branch: `codex/persistence-simplification`
> PR: #112 — **MERGED / CLOSED**
> Merge commit: `1b502c75357a2de7c928c88668e6a9613521b4ac`
> Status: **PS5 COMPLETE — automated + real-device acceptance PASS — merged to main**

This document is now a **historical/final PS5 record**. Current execution priority is maintained in `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

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
  PR #112 MERGED
```

## Final checkpoints

```text
pre-campaign base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
recovery quick-restart RED: 3bc389349bba00a155d55ec01508d6b271125ff0
recovery seating-owner helper: d8e1123d1535672dc04271ab41e18ec979ccaa7f
latest production GREEN: e622960a9c75c0b110d2c92e34b46828cb949e0a
renewed full-T4 checkpoint: 686b52a79cd4ea183c81884d34d54223fecae124
final branch head before merge: 18e7be6b915d654ea6bbfa6a11ff5627fe467153
merge commit on main: 1b502c75357a2de7c928c88668e6a9613521b4ac
```

## Frozen Recovery boundary

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Failed writes retain a future retry. A4 may not release rebuild before persistence succeeds.

Final trigger topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

`SideEffect` remains by explicit PS5.2b architecture decision. It provides a generic ordinary persistence opportunity across Undercover, Werewolf and Clocktower where no universal durable revision exists, and preserves a foreground retry opportunity after failed persistence. Do not reopen removal without profiling evidence and a replacement ownership design.

## PS5.1 safety foundation

`RecoveryWriteGate` provides semantic ordinary-write suppression, real-change writes, forced writes, `retryRequired`, clear/reset, and A4 persistence ordering.

```text
retry GREEN 39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS

mutable-alias RED 5736951007f66df042cb55d5a2b4122d064cf321
alias GREEN       e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
CI 34177323891 PASS
R2 34177323827 PASS
```

PS5.1c found a real nested mutable-alias hazard. The gate remembers timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow mutable object graph.

## PS5.2a lifecycle persistence — COMPLETE

Final lifecycle policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause establishes freshness; unchanged stop deduplicates; failed pause is retried at stop through `retryRequired`; changed durable content still writes. A4 ordering is unchanged.

Production GREEN and validation:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
34178595756 — focused RecoveryLifecyclePersistenceTest PASS
              :app:testFast PASS
              git diff --check PASS
34178562642 — R2 PASS
```

## Device-acceptance hotfix — recovered seating ownership

Real-device process-loss testing exposed one bug after Recovery restore:

```text
process death / relaunch / Recovery restore
-> continue Clocktower game
-> Host Tools
-> same players / quick restart
-> crash
```

Crash bundle exception:

```text
java.lang.IllegalArgumentException:
Production start requires the currently selected game

HostSeatingSetupFlow.playerNamesFor(...)
-> startClocktowerGame(...)
-> archiveAndStartNewGame(...)
```

Root cause: `applyValidatedRecoveryPlan()` restored cards, player names, game kind and durable mechanics but did not reconstruct the separate `HostSeatingSetupFlow` owner. The active recovered game was valid while `confirmedSeating` / `selectedGame` remained empty.

The fix deliberately keeps `playerNamesFor()` fail-closed. Recovery reconstructs confirmed seating plus the selected recovered game through the game-independent recovery factory. This covers recovered Undercover, Werewolf and Clocktower sessions rather than adding an NGJ-only exception.

Tests-first evidence:

```text
RED commit 3bc389349bba00a155d55ec01508d6b271125ff0
CI 34191093216 — expected FAIL
  HostSeatingRosterTest.kt: unresolved reference recoveredActiveGame
R2 34191093300 — PASS

helper GREEN d8e1123d1535672dc04271ab41e18ec979ccaa7f
product GREEN e622960a9c75c0b110d2c92e34b46828cb949e0a
one-shot run 34191324182 — PASS
  exact branch/blob locks PASS
  helper contract focused test PASS
  exact App-only product diff audit PASS
  git diff --check PASS
  focused HostSeatingRosterTest PASS
  :app:testFast PASS
  temporary workflow/script cleanup PASS
```

## Renewed automated acceptance — COMPLETE

Post-hotfix reserved T4:

```text
checkpoint 686b52a79cd4ea183c81884d34d54223fecae124

CI 34191738571 — PASS
  Android testFull             PASS
  :app:assembleDebug           PASS
  ASP golden/contract tests    PASS
  Real Clingo 5.8 cross-check  PASS
  CI gate                      PASS

R2 34191738649 — PASS
```

Final pre-merge docs verification:

```text
CI 34192745929 — PASS
R2 34192745940 — PASS
```

## Real-device acceptance — COMPLETE

On 2026-09-08 the previously defined real-device tests **1–5 all passed**.

The regression path that exposed the seating-owner crash was also retested successfully on the fixed build:

```text
6-player No Greater Joy
-> process-loss / relaunch / Recovery restore
-> continue recovered game
-> same players / quick restart
-> PASS: no crash, new game starts correctly
```

This closed the final PS5 release-acceptance blocker.

## Merge closure

The user explicitly authorized merge after all automated and real-device gates passed.

PR #112 was marked ready and merged with the branch head locked to:

```text
18e7be6b915d654ea6bbfa6a11ff5627fe467153
```

GitHub created merge commit:

```text
1b502c75357a2de7c928c88668e6a9613521b4ac
Merge pull request #112 from Jazz0006/codex/persistence-simplification
```

`main` was then confirmed at that merge commit.

## Next development step

PS5 is closed. Do not continue production work under this campaign.

The next task is a **fresh post-persistence D6 architecture/ownership re-audit** against the merged `main`. Use:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

The previous D6 plan is historical reference only and is superseded where it conflicts with the merged persistence architecture.

## Historical non-goals

PS5 deliberately did not expand into Recovery cross-version migration, Archive redesign, Werewolf removal, D6 implementation, A4/ZDD feature rollout, unrelated UI work or DataStore modernization.
