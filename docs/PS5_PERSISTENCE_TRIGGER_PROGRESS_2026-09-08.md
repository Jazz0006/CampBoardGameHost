# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IMPLEMENTATION + AUTOMATED ACCEPTANCE COMPLETE — real-device acceptance pending**

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
  final automated T4 COMPLETE
  real-device process-loss/restart acceptance PENDING
```

## Checkpoint

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
PR #112: open / draft / unmerged
latest production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
full-T4 checkpoint: 88249af2e68064b571da7cec5395c80940cbe021 (docs-only after production GREEN)
```

No production/test file changed after `5926138...` through the full-T4 checkpoint. Compare `5926138... -> 88249af...` contains only:

```text
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md
```

## Validation evidence

PS5.2a production GREEN:

```text
34178595756 — focused RecoveryLifecyclePersistenceTest PASS
              :app:testFast PASS
              git diff --check PASS
34178562642 — R2 PASS
```

Final reserved T4:

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

The literal production `git diff --check` evidence remains run `34178595756`; all later changes before T4 were docs-only. The final static connector audit additionally confirmed the accepted production slice and topology below. Do not describe the later docs-only head as having a separate `git diff --check` run.

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

## Current next step — real-device acceptance

Automated PS5 acceptance is complete. The campaign is **not release-ready yet** because real-device process-loss/restart behavior must still be exercised.

Minimum device matrix:

1. start a recent active game and create durable progress;
2. background normally, kill the Android process, relaunch, and restore;
3. verify safe Recovery entry rather than raw transient UI restoration;
4. verify cards/round/eliminations/outcome and game-specific durable mechanics survive;
5. for Clocktower, verify already-published semantic history/information survives;
6. verify at least one mandatory continuation (for example a supported pending night/Klutz/Demon continuation) resumes correctly;
7. verify stale/unsupported Recovery is rejected as designed;
8. exercise normal pause -> stop backgrounding and confirm no visible regression from the lifecycle deduplication policy;
9. if feasible, exercise a forced persistence-failure test build/path and confirm a later ordinary/stop attempt retries rather than falsely treating the state as durable.

Only after this device acceptance should Persistence Simplification be marked release-ready and PR #112 considered for merge. Do not merge without explicit authorization.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization during PS5.
