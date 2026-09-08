# Next Development Handoff — PS4 Persistence Cleanup

> Date: 2026-09-07 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Purpose: authoritative next-chat route for **PS4 — Retire superseded active-save infrastructure**.

## 1. Entry state

At PS4 planning handoff creation, live GitHub state was:

```text
main/base:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

working branch before PS4 documentation commits:
8ab643bec947d818b12b29ad43cee3b02ccf1727
docs: trigger final PS3 checkpoint gates [full-ci]

PR #112:
open
draft
unmerged
mergeable
```

Always re-query live `main`, branch head, PR #112 state and latest checks before any production write. The documentation commits that create/update this handoff are expected to advance the branch head beyond `8ab643...`; treat that SHA as the audited PS4 planning baseline, not as a permanent expected head.

PS3 is complete. Authoritative PS3 production checkpoint:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

PS3 final cleanup/audit lineage also includes:

```text
28ad2f7cc734be80cd8aecce718e074f38c38082
```

and the final user-authored `[full-ci]` trigger at `8ab643...`.

Do not reopen PS3 behavior work unless PS4 exposes a real regression or contract defect.

## 2. Product and architecture boundary remains frozen

Recent Emergency Recovery remains intentionally narrow:

- one recent recovery only;
- maximum age 4 hours;
- process-loss/crash fallback, not normal Save/Load UX;
- no next-day continuation promise;
- no cross-version active-recovery migration framework;
- restore durable game facts and mandatory continuation, not exact App/UI state;
- malformed/incompatible recovery fails closed before partial App mutation;
- Preview and Restore share the typed preparation authority;
- supported typed-recovery product surface is **Undercover + Blood on the Clocktower**.

Werewolf recovery is not a PS4 functionality target. Current product direction is to remove the Werewolf module separately after Persistence Simplification. Do not expand, repair or redesign Werewolf recovery while cleaning persistence infrastructure.

Archive remains a separate long-lived product. Cleanup must not remove real archive compatibility merely because old active recovery is being retired.

## 3. PS4 entry audit — key findings

### 3.1 `activeGameSnapshotJson()` is proven dead active-save source

PS3.4 production-path audit established that current Recovery write, Preview and Restore no longer call `activeGameSnapshotJson()`.

Therefore PS4 may delete the function and helpers/constants/imports that become unreachable solely because of that deletion. Do not manufacture a RED test for this dead-code removal; use reference proof, existing owning behavior tests, compilation and exact diff audit.

### 3.2 `LegacyRestoreCompatibility` cannot be deleted atomically at the start

The legacy shell currently carries several categories together:

```text
activeGameStateVersion
persisted active-game identity
CommittedClocktowerSetup
TroubleBrewingSetupRotationRecord
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

Most are transitional active-restore debt and are no longer planner validity authority.

However `troubleBrewingSetupRotationRecord` still has a real post-recovery consumer: typed Clocktower App restore currently restores the in-memory committed rotation record from the legacy shell, and that record participates in completed-game setup-rotation bookkeeping.

Therefore the safe dependency order is:

```text
move durable TroubleBrewingSetupRotationRecord into typed ClocktowerRecovery
-> prove recovery/bookkeeping behavior
-> remove the remaining legacy shell
```

Do not delete setup-rotation bookkeeping.

### 3.3 Recovery compatibility ownership is still coupled to old ActiveGame versioning

Current token generation still depends on:

```text
ActiveGamePersistenceCoordinator.CURRENT_VERSION
```

This is obsolete ownership after PS3. Recovery should own its own format/token identity before `ActiveGamePersistenceCoordinator` is considered removable.

Target direction:

```text
Recovery format/version authority
+ game kind
-> current Recovery compatibility token
```

Do not rebuild the former long-lived content-identity/migration framework.

### 3.4 Typed model still emits a legacy-shaped Recovery wire payload

The current typed writer/strict decoder still carry old flat active-save metadata such as:

```text
version
PersistedActiveGameIdentity
CommittedClocktowerSetup
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

PS4 should retire this compatibility baggage rather than maintain two conceptual schemas indefinitely.

Because the product explicitly makes no cross-version active-recovery promise and Recovery expires after 4 hours, a deliberate Recovery format bump is preferable to retaining obsolete compatibility fields indefinitely.

### 3.5 `ClocktowerRulesetPersistenceBasis` itself is not cleanup debt

Do not delete `ClocktowerRulesetPersistenceBasis` merely because its name includes `Persistence`.

The current typed Recovery runtime resolution still legitimately derives/uses a role-set basis to resolve current Trouble Brewing ruleset identity. Keep the current basis and `TroubleBrewingRulesetPersistence.refFor()` semantics unless a separate proof shows they are unnecessary.

Legacy restore shims inside that area may be removed only after reference audit proves they are unreachable.

## 4. Approved PS4 execution route

Execute PS4 as small dependency-ordered checkpoints. Do not perform one broad grep-driven deletion.

### PS4.1 — Proven-dead active snapshot removal

Goal:

- delete `activeGameSnapshotJson()`;
- remove only helpers/constants/imports that become dead solely because of that deletion;
- do not change Recovery schema or save timing in this checkpoint.

Evidence:

- fresh repository-wide reference audit;
- affected existing persistence/recovery tests;
- compile / `:app:testFast` at the logical checkpoint;
- `git diff --check`;
- exact changed-file audit.

No manufactured RED is required for behavior-preserving dead-code deletion.

### PS4.2 — Give Recovery independent compatibility ownership

Goal:

- stop deriving Recovery compatibility from `ActiveGamePersistenceCoordinator.CURRENT_VERSION`;
- establish a small Recovery-owned current-format/current-contract token;
- keep the existing 4-hour and fail-closed semantics.

Stable typed behavior that must remain covered:

- current token accepted;
- wrong token rejected as compatibility mismatch;
- wrong format rejected;
- future timestamp rejected;
- exact 4-hour boundary accepted;
- older than 4 hours expired.

Do not introduce legacy migration support.

### PS4.3 — Recovery wire schema cleanup / format bump

Goal:

1. move the still-durable `TroubleBrewingSetupRotationRecord` from `LegacyRestoreCompatibility` into the typed Clocktower recovery model;
2. update typed writer/strict decoder/App apply accordingly;
3. bump Recovery format deliberately (expected direction: v1 -> v2) rather than carrying obsolete active-save wire compatibility;
4. remove obsolete active-save metadata from the Recovery wire payload;
5. delete `LegacyRestoreCompatibility` once no true durable field remains in it.

Expected obsolete wire fields/couplings to retire after proof:

```text
legacy active state version
PersistedActiveGameIdentity in Recovery
CommittedClocktowerSetup in Recovery
persisted clocktowerRulesetRoleIds in Recovery
persisted clocktowerRulesetRef in Recovery
```

The new current-format Recovery should remain strict and all-or-nothing.

Required behavior evidence includes:

- representative Undercover current-format recovery;
- representative Clocktower current-format recovery;
- setup-rotation record survives recovery and remains available to completed-game bookkeeping;
- Stable preview remains valid without generic `screen`;
- pending Klutz safe re-entry remains correct;
- malformed/current-format mismatches fail before App mutation;
- previous Recovery format is rejected cleanly rather than migrated.

### PS4.4 — Retire old ActiveGame identity/coordinator infrastructure

After PS4.2/PS4.3, perform a fresh reference audit before deletion.

Likely candidates include, only where proven unreachable:

```text
ActiveGamePersistenceCoordinator
ActiveGameIdentityEnvelope / related active-game offer types
PersistedActiveGameIdentityEnvelope
PersistedActiveGameIdentity
PersistedActiveGameIdentityJsonCodec
ClocktowerPersistenceIdentityFactory
legacy resolve-for-restore paths
```

Do not delete by filename/name alone. Compiler/reference proof controls the actual deletion set.

Tests that protect only removed implementation/schema details should be retired according to `AGENTS.md` test-retirement policy after confirming no unique product contract is lost.

### PS4.5 — Setup / ruleset / test hygiene

Audit remaining transitional persistence code after the main legacy chain has been cut.

Potential cleanup candidates:

- `CommittedClocktowerSetup` persistence if no post-start production consumer remains;
- legacy ruleset restore shims with no caller;
- duplicate Clocktower checkpoint mapping/writes that no longer have production ownership;
- tests protecting only removed active-save schema shape.

Must retain:

- `TroubleBrewingSetupRotationRecord` and rotation history bookkeeping;
- real archive compatibility and archive review behavior;
- durable semantic/history persistence tests;
- action timeline / epistemic observation durability;
- current `ClocktowerRulesetPersistenceBasis` if still required by typed current-rules resolution.

### PS4.6 — Final architecture and validation checkpoint

Target architecture assertions:

```text
NO activeGameSnapshotJson production/dead implementation
NO LegacyRestoreCompatibility
NO obsolete ActiveGame restore coordinator/identity ownership in Recovery
NO persisted old committed-setup authority in active Recovery
NO persisted legacy ruleset ref/role-id authority in Recovery wire
NO independent raw Preview parser
NO independent raw Restore parser
```

Retained architecture:

```text
one typed Recovery writer
one strict current-format Recovery decoder
one Recovery preparation/validation planner
Preview ----\
            -> prepareCurrentRecoveryPlan(raw)
Restore ----/
one atomic RecoveryApplicationCoordinator apply boundary
separate Archive compatibility
setup-rotation bookkeeping
durable semantic/history persistence
```

Final PS4 validation should use the project risk-based gates and include at least:

- focused current Recovery + Archive behavior tests;
- `:app:testFast`;
- `:app:testFull` for final campaign checkpoint;
- `:app:assembleDebug`;
- applicable ASP/Clingo gates;
- R2 main-thread boundary;
- `git diff --check` on the actual PS4 product diff/current commit as appropriate;
- exact deleted/changed-file and reference audit;
- remote-head race lock.

Do not merge PR #112 without explicit user authorization.

## 5. Explicit non-goals for PS4

Do not mix any of the following into PS4:

- PS5 persistence-trigger timing redesign;
- deleting the entire Werewolf module;
- D6 App/Host decomposition;
- general ViewModel/DataStore migration;
- long-lived save slots/manual Save/Load UX;
- cross-version Recovery migration;
- A4/ZDD production cutover;
- recommendation-quality redesign;
- unrelated Host UI changes.

If cleanup naturally reduces App size, accept it as a consequence, not as a decomposition acceptance metric.

## 6. Sequence after PS4

Preferred project order is:

```text
PS4 Cleanup
-> PS5 persistence-trigger simplification (if still justified by fresh audit)
-> complete/merge Persistence Simplification with explicit authorization
-> separately remove Werewolf module
-> fresh D6 ownership/decomposition audit against the reduced codebase
```

Do not reuse the old D6 sequence without a fresh ownership/hotspot audit.

## 7. Next-chat start instruction

In the next conversation:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this file;
4. read `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`;
5. re-query live `main`, PR #112/head/checks and distinguish the last PS3 production checkpoint from later docs-only PS4 planning commits;
6. start **PS4.1 only** with a fresh reference/dependency audit of `activeGameSnapshotJson()` and its exclusively-dead helpers;
7. use existing behavior evidence rather than manufacturing a RED for dead-code deletion;
8. stop before PS4.2 unless PS4.1 is cleanly validated and the user authorizes continuing in that conversation.

Do not merge PR #112.