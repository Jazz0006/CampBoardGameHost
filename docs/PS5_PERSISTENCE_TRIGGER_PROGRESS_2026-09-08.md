# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 COMPLETE — automated + real-device acceptance PASS — release-ready, unmerged**

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

## Final checkpoint

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
PR #112: open / draft / unmerged
pre-device-acceptance production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
recovery quick-restart RED: 3bc389349bba00a155d55ec01508d6b271125ff0
recovery seating-owner helper: d8e1123d1535672dc04271ab41e18ec979ccaa7f
latest production GREEN: e622960a9c75c0b110d2c92e34b46828cb949e0a
renewed full-T4 checkpoint: 686b52a79cd4ea183c81884d34d54223fecae124
```

## Frozen Recovery boundary

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Failed writes retain a future retry. A4 may not release rebuild before persistence succeeds.

Final trigger topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

`SideEffect` remains by explicit PS5.2b architecture decision. It provides a generic ordinary persistence opportunity across Undercover, Werewolf and Clocktower where no universal durable revision exists, and also preserves foreground retry opportunity after failed persistence. Do not reopen removal without profiling evidence and a replacement ownership design.

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

The fix deliberately keeps `playerNamesFor()` fail-closed. Recovery now reconstructs confirmed seating plus the selected recovered game through the new game-independent recovery factory. This covers recovered Undercover, Werewolf and Clocktower sessions rather than adding an NGJ-only exception.

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

Final docs-only verification after recording the hotfix acceptance:

```text
CI 34191972250 — PASS
R2 34191972340 — PASS
```

## Real-device acceptance — COMPLETE

On 2026-09-08 the user confirmed that the previously defined real-device tests **1–5 all passed**.

The regression path that exposed the seating-owner crash was also retested successfully on the fixed build:

```text
6-player No Greater Joy
-> process-loss / relaunch / Recovery restore
-> continue recovered game
-> same players / quick restart
-> PASS: no crash, new game starts correctly
```

This closes the final PS5 release-acceptance blocker.

## Release status

**Persistence Simplification / PS5 is release-ready.**

PR #112 remains open / draft / unmerged only because merge requires explicit user authorization. Do not make further production changes on this branch merely to extend PS5.

## Next step after merge

After PR #112 is explicitly authorized and merged:

1. re-confirm live `main`, merged head and checks;
2. re-audit the post-persistence architecture;
3. create a fresh D6 ownership/decomposition plan;
4. treat the previous D6 plan as superseded by the persistence architecture changes.

## Non-goals before merge

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 implementation, A4/ZDD rollout, unrelated UI work or DataStore modernization unless explicitly requested.
