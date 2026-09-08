# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IMPLEMENTATION COMPLETE — recovery quick-restart hotfix automated GREEN; renewed T4 + real-device acceptance pending**

## Campaign

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 IN PROGRESS — release acceptance pending only
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
  original final automated T4 COMPLETE
  recovery quick-restart hotfix automated GREEN
  renewed post-hotfix T4 PENDING
  real-device process-loss/restart acceptance PENDING
```

## Checkpoint

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
PR #112: open / draft / unmerged
pre-device-acceptance production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
original full-T4 checkpoint: 88249af2e68064b571da7cec5395c80940cbe021
recovery quick-restart RED: 3bc389349bba00a155d55ec01508d6b271125ff0
recovery seating-owner helper: d8e1123d1535672dc04271ab41e18ec979ccaa7f
latest production GREEN: e622960a9c75c0b110d2c92e34b46828cb949e0a
one-shot cleanup head: b8b63cb62061ce29c471aa37fb036a4199be81ef
```

The original T4 was valid for the pre-device-acceptance production slice. Real-device process-loss/restart testing then exposed a separate Recovery ownership bug, so the production checkpoint advanced and a renewed full T4 is required before release acceptance.

## Validation evidence

PS5.2a production GREEN:

```text
34178595756 — focused RecoveryLifecyclePersistenceTest PASS
              :app:testFast PASS
              git diff --check PASS
34178562642 — R2 PASS
```

Original reserved T4:

```text
CI 34179926099 — PASS
  Classify changes             PASS / full checkpoint selected
  Android testFull             PASS
  :app:assembleDebug           PASS
  ASP golden/contract tests    PASS
  Real Clingo 5.8 cross-check  PASS
  CI gate                      PASS

R2 34179926105 — PASS
```

The literal pre-hotfix production `git diff --check` evidence was run `34178595756`; later pre-device-acceptance changes through the original T4 were docs-only.

## Frozen boundary

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Failed writes retain a future retry. A4 may not release rebuild before persistence succeeds.

## PS5.1 foundation

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

PS5.1c found a real nested mutable-alias hazard. The gate now remembers timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow mutable object graph.

## PS5.2a COMPLETE — lifecycle duplicate physical write reduction

Behavior RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
focused RED 34178392065
```

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause establishes freshness; unchanged stop deduplicates; failed pause is retried at stop through `retryRequired`; changed durable content still writes. A4 ordering is unchanged.

Exact PS5.2a production/test slice relative to the pre-slice docs head `e2ed5989b42dbd36c969ffad9ec6131f1a3bbdf0` is exactly:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

## PS5.2b COMPLETE — SideEffect ownership audit

**Decision: retain `SideEffect` as the ordinary generic persistence-attempt trigger.**

This is the final PS5 architecture decision, not deferred cleanup.

Why:

1. Recovery spans common state plus Undercover, Werewolf and Clocktower durable state.
2. Undercover/Werewolf contain direct durable mutations without a central recovery revision.
3. Clocktower revisions are broad but not universal; persisted mechanics such as ghost-vote authority can change through an owner without a paired current revision bump.
4. Explicit `persistActiveGameStateIfNeeded()` calls cover only a small subset of gameplay transitions; ordinary gameplay intentionally relies on the generic trigger rather than scattered storage calls.
5. `RecoveryWriteGate.retryRequired` and `A4ObservationDurabilityGate` require another foreground persistence opportunity after failure. A pure change-only trigger would need a new universal dirty token plus retry scheduler/state machine.
6. PS5.1 already suppresses duplicate synchronous physical `.commit()` calls, and PS5.2a removes the normal successful pause/stop double commit. Remaining snapshot/identity construction has not been measured as a performance problem worth a new correctness surface.

Final intended trigger topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

Static reference audit confirms one App `SideEffect` persistence block, lifecycle delegation through `persistRecoveryForLifecycleEvent`, and active Recovery physical write ownership remaining at `saveActiveGameState(...).commit()` behind `RecoveryWriteGate`.

Do not reopen SideEffect removal unless later profiling demonstrates a real main-thread snapshot/identity cost. If so, treat it as a measured performance campaign with universal durable-mutation ownership, not opportunistic persistence cleanup.

## Device-acceptance hotfix — recovered seating ownership

Real-device testing exposed a crash in the supported sequence:

```text
process death / relaunch / Recovery restore
-> continue Clocktower game
-> same players / quick restart
-> crash
```

The exported crash bundle recorded:

```text
java.lang.IllegalArgumentException: Production start requires the currently selected game
HostSeatingSetupFlow.playerNamesFor(...)
-> startClocktowerGame(...)
-> archiveAndStartNewGame(...)
```

Root cause: `applyValidatedRecoveryPlan()` correctly restored cards, player names, game kind and durable mechanics, but did not reconstruct the separate `HostSeatingSetupFlow` owner. After process restoration, the active game was valid while `confirmedSeating` / `selectedGame` remained empty. Quick restart therefore hit the existing fail-closed production-start contract.

The fix intentionally does **not** weaken `HostSeatingSetupFlow.playerNamesFor()`. Recovery now rehydrates the setup-flow owner from the already validated recovered roster and recovered game kind:

```text
Recovered cards/player identities
-> HostSeatingSetupFlow.recoveredActiveGame(...)
-> confirmed seating restored
-> selected active game restored
-> existing production-start invariant remains fail-closed
```

The helper is game-independent, so the ownership repair applies to recovered Undercover, Werewolf and Clocktower sessions rather than adding an NGJ-only exception.

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
  helper contract focused test PASS before App wiring
  exact App-only product diff audit PASS
  git diff --check PASS
  focused HostSeatingRosterTest PASS
  :app:testFast PASS
  product commit/push PASS
  temporary workflow/script cleanup PASS
```

The final bot-generated cleanup head produced `action_required` PR workflow states rather than test failures, so this docs checkpoint requests a fresh full T4 from a normal branch head.

## Current next step — renewed T4, then real-device acceptance

The recovery ownership bug is automated GREEN but the campaign is **not release-ready yet**.

First, this `[full-ci]` checkpoint must re-run the reserved full automated acceptance against the new production checkpoint. After that passes, repeat the real-device path that exposed the bug:

```text
6-player No Greater Joy
-> create durable progress
-> background / kill process
-> relaunch and restore
-> continue the recovered game
-> choose same players / quick restart
-> verify a new NGJ game starts without crash and preserves the recovered roster
```

Then continue/confirm the minimum device matrix:

1. start a recent active game and create durable progress;
2. background normally, kill the Android process, relaunch, and restore;
3. verify safe Recovery entry rather than raw transient UI restoration;
4. verify cards/round/eliminations/outcome and game-specific durable mechanics survive;
5. for Clocktower, verify already-published semantic history/information survives;
6. verify at least one mandatory continuation (for example a supported pending night/Klutz/Demon continuation) resumes correctly;
7. verify stale/unsupported Recovery is rejected as designed;
8. exercise normal pause -> stop backgrounding and confirm no visible regression from the lifecycle deduplication policy;
9. if feasible, exercise a forced persistence-failure test build/path and confirm a later ordinary/stop attempt retries rather than falsely treating the state as durable.

Only after renewed T4 and device acceptance should Persistence Simplification be marked release-ready and PR #112 considered for merge. Do not merge without explicit authorization.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization during PS5.
