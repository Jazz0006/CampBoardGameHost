# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 TRIGGER DESIGN COMPLETE — final automated acceptance NEXT**

## Campaign

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 IN PROGRESS
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
```

## Checkpoint

```text
base main: ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
PR #112: open / draft / unmerged
latest validated production GREEN: 5926138d0557835f281ba15b4051f50fa3ae741e
```

Validation on the production GREEN:

```text
34178595756 — focused RecoveryLifecyclePersistenceTest + :app:testFast + git diff --check PASS
34178562642 — R2 PASS
```

All commits after `5926138...` through the PS5.2b audit checkpoint are validation-cleanup/docs-only. No production change was required for PS5.2b, so no artificial RED or redundant FAST run was created.

Full T4 is now the next automated step. Real-device process-loss/restart remains required before release-ready.

## Frozen boundary

Recovery is current-version-only, 4-hour emergency continuity. Archive remains separate. Unsupported/old Recovery fails closed. Failed writes must retain a future retry. A4 may not release rebuild before persistence succeeds.

## PS5.1 foundation

`RecoveryWriteGate` provides semantic ordinary-write suppression, real-change writes, forced writes, `retryRequired`, clear/reset, and A4 persistence ordering.

```text
retry GREEN 39229bfdddba5837a9368706946f62fd94915109
CI 34174011104 PASS
R2 34174011121 PASS

5736951007f66df042cb55d5a2b4122d064cf321 RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23 GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

PS5.1c found a real nested mutable-alias hazard. The gate now remembers timestamp-normalized persisted Recovery representation as immutable content identity. Suppressed ordinary attempts still pay snapshot + serialization identity cost.

## PS5.2a COMPLETE — lifecycle duplicate physical write reduction

Old policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

Behavior RED `192f031b67c9b4eb46bada4928425f9de332bb4a`; focused RED `34178392065` proved successful pause + unchanged stop wrote twice while retry and changed-content contracts already passed.

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Successful pause establishes freshness; unchanged stop deduplicates; failed pause is retried at stop through `retryRequired`; changed durable content still writes. A4 ordering is unchanged.

## PS5.2b COMPLETE — SideEffect ownership audit

### Audit result

**Retain `SideEffect` as the ordinary generic persistence-attempt trigger.**

This is an explicit architecture decision, not a deferred cleanup item.

Current topology is therefore the intended PS5 topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

### 1. Every Recovery input was enumerated

`RecoverySnapshot` contains the common envelope:

- compatibility token / saved timestamp;
- safe entry point;
- current deal index;
- round;
- cards;
- elimination records;
- outcome.

`UndercoverRecovery` additionally persists:

- undercover count;
- blank-role setting;
- last-words mode.

`WerewolfRecovery` additionally persists:

- role/setup settings;
- judge-step cursor;
- pending night death;
- seer target;
- witch used/current-night state;
- poison target;
- hunter target.

`ClocktowerRecovery` additionally persists:

- game/script identity and seed;
- Trouble Brewing setup-rotation record;
- resumable phase/night position;
- confirmed/pending mechanics and mandatory continuations;
- bluff/claim/use bookkeeping;
- ghost-vote/highest-vote authority;
- game/player revisions;
- semantic-history mode;
- action timeline/global sequence;
- semantic events;
- epistemic observations.

### 2. No existing single durable revision owns all of those inputs

Clocktower has strong revision discipline through `advanceClocktowerGameStateRevision()` and `advanceClocktowerPlayerInputRevision()`, and most semantic/night actions use those boundaries.

However those revisions are not a complete Recovery dirty token. A concrete example is `ghostVoteAuthority`, which is persisted in `ClocktowerRecoveryMechanics` but production wiring also permits direct replacement through:

```text
onGhostVoteAuthorityChange = { clocktowerGhostVoteAuthorityState.value = it }
```

without a paired game/player revision bump at that owner.

Therefore the existing Clocktower revision pair cannot safely replace `SideEffect` without first redesigning mutation ownership.

### 3. Non-Clocktower Recovery makes a Clocktower-only token insufficient

Undercover and Werewolf do not have an equivalent central durable revision.

Examples of durable direct mutation ownership include:

- Undercover elimination directly updates `cards`, `records`, and `gameOutcome`;
- identity reveal advances `currentDealIndex` directly;
- Werewolf judge callbacks directly update judge-step and night-role state;
- Werewolf dawn/day resolution directly updates cards, records, outcome, used-role flags, targets and round state.

Those mutations are currently covered generically by the later Compose `SideEffect` ordinary attempt.

### 4. Explicit persistence calls do not cover ordinary gameplay mutations

The production search found only a small number of explicit `persistActiveGameStateIfNeeded()` boundaries beyond the shared persistence function itself, notably initial Clocktower/setup materialization paths.

Ordinary Undercover/Werewolf gameplay and multiple Clocktower mechanics therefore intentionally rely on the generic trigger rather than scattered save calls.

Replacing `SideEffect` with explicit per-action persistence would increase coupling between UI/domain mutation code and storage ownership and would be a regression in architecture clarity.

### 5. Failure retry and A4 make change-only triggering insufficient

`RecoveryWriteGate` retains `retryRequired` after a failed physical write.

`A4ObservationDurabilityGate` also retains its pending observation until persistence succeeds:

```text
observation committed
-> markPending(recordId)
-> persistence attempt
-> release only if persistenceSucceeded == true
```

A pure “persist only when durable state changes” effect would have no new key/change to drive another foreground attempt after a failure. Lifecycle persistence would eventually retry when the app backgrounds, but that is not equivalent to the current foreground retry opportunity and can delay A4 release.

Preserving current retry semantics after removing `SideEffect` would therefore require a new retry scheduler/state machine or a new universal dirty/retry signal.

### 6. Cost/complexity decision

The remaining `SideEffect` cost is snapshot construction plus timestamp-normalized JSON identity construction on recomposition. The expensive synchronous physical `.commit()` has already been semantically deduplicated by PS5.1, and PS5.2a removed the normal duplicate pause/stop physical commit.

There is no measured evidence in this campaign that remaining identity construction is large enough to justify:

- instrumenting every Undercover/Werewolf/Clocktower durable mutation;
- repairing Clocktower revision gaps solely for persistence;
- adding a universal dirty token;
- adding foreground retry scheduling;
- or coupling individual game actions to storage calls.

That would optimize an unproven secondary cost by adding a new correctness surface. It is not persistence simplification.

### PS5.2b decision

```text
KEEP SideEffect
KEEP ON_PAUSE as forced freshness checkpoint
KEEP ON_STOP as ordinary retry/change fallback
KEEP RecoveryWriteGate semantic identity + retryRequired
KEEP A4 persistence-before-release ordering
```

Do not reopen SideEffect removal unless later profiling shows snapshot/identity construction is a real main-thread performance problem. If that happens, treat it as a measured performance campaign with a universal durable-mutation owner, not as opportunistic persistence cleanup.

## Final PS5 automated acceptance NEXT

Run the reserved final gate on the unchanged production checkpoint lineage:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle tests;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- `git diff --check` / changed-file audit against the accepted production checkpoint.

After automated acceptance, perform real-device process-loss/restart acceptance before declaring Persistence Simplification release-ready.

## Non-goals

No Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated UI work or DataStore modernization during PS5.
