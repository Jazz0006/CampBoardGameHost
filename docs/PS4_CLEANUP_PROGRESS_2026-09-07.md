# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.3b complete; stop before PS4.4**

## 1. Current campaign position

Persistence Simplification status:

```text
PS0 contract freeze                 COMPLETE
PS1 Archive / Recovery separation   COMPLETE
PS2 typed Recovery writer           COMPLETE
PS3 typed safe Preview/Restore      COMPLETE
PS4 legacy cleanup                  IN PROGRESS
  PS4.1 dead active snapshot        COMPLETE
  PS4.2 Recovery token ownership    COMPLETE
  PS4.3 Recovery wire cleanup       COMPLETE
    PS4.3a typed TB rotation owner  COMPLETE
    PS4.3b Recovery v2/wire cleanup COMPLETE
  PS4.4 ActiveGame identity cleanup NOT STARTED
PS5 trigger simplification          NOT STARTED
```

Live `main` remained:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains open, draft and unmerged.

Key earlier production checkpoints:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery

721c7394115cdc839afb189f88eb9234cd5ea204
refactor: remove dead active snapshot

9fa0e3f86832cd847fb71126e4de54524e0326d2
refactor: give Recovery independent compatibility token

8ed54f855d095f3f9c4473822ba32f06af4630db
refactor: wire typed TB rotation recovery in App
```

PS4.3a final docs checkpoint before PS4.3b:

```text
13978ad1ef665c1364de4186fecb673e68dc8b59
docs: record PS4.3a typed rotation checkpoint [full-ci]
```

## 2. PS4.1 — complete

PS4.1 removed the proven-dead `activeGameSnapshotJson()` path and helpers proven exclusive to it. It did not change Recovery schema or timing.

## 3. PS4.2 — complete

Recovery compatibility identity is owned by Recovery itself:

```text
RecoveryCompatibilityToken.currentFor(gameKind)
-> "recovery-v${RecoverySnapshot.CURRENT_FORMAT_VERSION}:${gameKind.name}"
```

This removed compatibility-token ownership from `ActiveGamePersistenceCoordinator.CURRENT_VERSION` while retaining the 4-hour/fail-closed contract.

## 4. PS4.3a — complete typed Trouble Brewing rotation ownership

`TroubleBrewingSetupRotationRecord` was proven to be genuine durable current-game bookkeeping:

```text
TB preset selection
-> committedTroubleBrewingSetupRotationRecord
-> typed ClocktowerRecovery
-> strict Recovery decode
-> App Recovery apply
-> completed-game rotation history
```

It now belongs directly to:

```text
ClocktowerRecovery.troubleBrewingSetupRotationRecord
```

Typed invariants protect:

1. only Trouble Brewing Recovery may carry the record;
2. when present, `record.playerCount == recovered cards.size`.

The existing wire key remains:

```text
TroubleBrewingSetupCompletionPersistence.ROOT_KEY
= "troubleBrewingSetupCompletion"
```

## 5. PS4.3b — complete Recovery v2 schema cut

### 5.1 Tests-first RED

Contract checkpoint:

```text
e4cde20f8449eed49e36ef6e0162f696f3901096
test: define Recovery v2 schema contract
```

Added:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryV2SchemaContractTest.kt
```

The RED run was exact and behavior-focused:

```text
CI run 34110974863
:app:testFast
1191 tests completed, 3 failed
```

All three failures were only the new v2 contract assertions:

1. Recovery current format/token expected v2;
2. previous v1 Recovery expected `UnsupportedFormat` rather than migration;
3. current wire expected the obsolete ActiveGame metadata to be absent.

There were no compile failures or unrelated regressions. R2 at the RED checkpoint succeeded.

### 5.2 Fresh dependency audit

Before production changes, every remaining `LegacyRestoreCompatibility` field was re-audited:

```text
activeGameStateVersion
identity
committedClocktowerSetup
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

After PS4.3a none remained a genuine Recovery-owned durable fact.

In particular:

- typed Recovery already carries game kind and Clocktower identity;
- player `cards` carry actual/shown Clocktower roles;
- current Clocktower ruleset basis is reconstructed from recovered actual roles;
- current `RulesetRef` is resolved from the current rules asset, not trusted from Recovery wire;
- committed setup has no current Recovery-apply consumer;
- Trouble Brewing rotation bookkeeping had already moved to typed `ClocktowerRecovery` in PS4.3a.

### 5.3 GREEN product checkpoint

Authoritative PS4.3b product commit:

```text
58d687cc8c8082c040cf07eabcf5c1cfbd4dda15
refactor: cut Recovery over to v2 schema
```

Exact product diff from the pre-product one-shot head contains only 12 `app` source/test files:

```text
10 insertions
188 deletions
```

Production behavior/architecture changes are:

1. `RecoverySnapshot.CURRENT_FORMAT_VERSION` deliberately moved `1 -> 2`;
2. current compatibility tokens naturally became:
   - `recovery-v2:Undercover`
   - `recovery-v2:Clocktower`;
3. `LegacyRestoreCompatibility` was deleted completely;
4. `RecoverySnapshot` now directly contains only:
   - recovery format version;
   - compatibility token;
   - saved timestamp;
   - typed Recovery game;
5. current Recovery writer no longer emits:
   - legacy `version`;
   - `gameContentIdentity`;
   - `committedClocktowerSetup`;
   - `clocktowerRulesetRoleIds`;
   - `clocktowerRulesetRef`;
6. strict current Recovery decoder no longer reads or reconstructs those fields;
7. previous v1 Recovery is rejected by the format gate as `UnsupportedFormat`; no migration path was added;
8. `ActiveGamePersistenceCoordinator.identityForSave()` remains called once as a temporary save-time validation bridge, but its returned identity is no longer persisted by Recovery.

### 5.4 Fail-closed one-shot evidence

PS4.3b product workflow:

```text
PS4.3b Recovery v2 one-shot
run 34111655699
```

The product-critical stages all succeeded:

```text
exact RED/tooling lineage lock                  PASS
fail-closed schema/App patch                    PASS
exact post-patch reference/wire audit           PASS
:app:compileDebugKotlin                          PASS
:app:testFast                                    PASS
git diff --check                                PASS
remote-head race audit                          PASS
product commit/push                             PASS
```

The workflow job itself ended failed only in the final self-cleanup step because the runtime-repaired patch script had local tooling modifications and plain `git rm` refused to remove it. No product rollback occurred.

The three temporary PS4.3b tooling files were then removed through GitHub Contents API in cleanup lineage ending at:

```text
75d112d9ff068cc4950e7c1449e1e2930eeba4da
chore: remove PS4.3b one-shot workflow
```

No temporary PS4.3b script/workflow remains in the final branch tree.

## 6. Current Recovery architecture after PS4.3b

Current active Recovery boundary is now:

```text
App active state
-> typed RecoverySnapshot
-> RecoverySnapshotJsonCodec v2
-> recent active-recovery storage

Preview ----\
            -> prepareCurrentRecoveryPlan(raw)
Restore ----/
            -> format/token/time validity
            -> strict RecoverySnapshot decoder
            -> typed/game-specific validation
            -> current Clocktower ruleset resolution
            -> ValidatedRecoveryPlan
            -> one atomic RecoveryApplicationCoordinator apply boundary
```

Recovery v2 intentionally does **not** contain long-lived content-migration metadata.

Archive remains a separate long-lived product and its compatibility paths were not changed by PS4.3b.

## 7. Frozen boundaries after PS4.3b

PS4.3b deliberately did **not**:

- remove `ActiveGamePersistenceCoordinator`;
- remove `PersistedActiveGameIdentity*` classes/codecs globally;
- remove `CommittedClocktowerSetup` or `CommittedClocktowerSetupPersistence` globally;
- remove `ClocktowerRulesetPersistenceBasis` or current ruleset-resolution behavior;
- remove the single `identityForSave()` validation bridge;
- change Recovery save timing or persistence triggers;
- change the 4-hour Recovery validity policy;
- add v1/v2 migration support;
- change Archive compatibility;
- remove Trouble Brewing rotation bookkeeping;
- alter durable Clocktower semantic history/action timeline/epistemic observations;
- delete Werewolf;
- begin PS5;
- begin D6;
- merge PR #112.

## 8. Next slice — PS4.4, NOT STARTED

PS4.4 must begin with a fresh repository-wide reference and ownership audit before deleting any old ActiveGame identity/coordinator infrastructure.

Likely candidates, only where proven unreachable or safely re-homed:

```text
ActiveGamePersistenceCoordinator
ActiveGameIdentityEnvelope / related active-game offer types
PersistedActiveGameIdentityEnvelope
PersistedActiveGameIdentity
PersistedActiveGameIdentityJsonCodec
ClocktowerPersistenceIdentityFactory
legacy resolve-for-restore paths
```

Important boundary:

```text
activeGamePersistenceCoordinator.identityForSave(...)
```

still has current save-time validation behavior. Do not simply delete this call together with the now-unused persisted identity classes. PS4.4 must first decide which validations remain product invariants and where they should live.

Do not mix PS4.5 setup/ruleset hygiene, PS5 trigger simplification, Werewolf removal, or D6 into PS4.4.

## 9. Final PS4.3b stop point

**PS4.3b is complete. Stop before PS4.4.**

Before PS4.4 implementation:

1. re-query live `main` and PR #112 head/state/checks;
2. confirm this v2 checkpoint is green;
3. keep PR #112 draft and unmerged;
4. perform a fresh reference/behavior audit of `ActiveGamePersistenceCoordinator` and the identity chain;
5. preserve current save-time validation unless deliberately re-homed under tests.

## 10. Retained architecture/product contracts

Must remain intact unless separately audited:

- `GameArchiveRecord` / `GameArchiveJsonCodec` and real archive compatibility;
- Trouble Brewing setup-rotation bookkeeping/history;
- durable Clocktower semantic history;
- action timeline / epistemic observations;
- current Clocktower ruleset resolution;
- 4-hour Recovery validity policy;
- current-format-only Recovery contract;
- fail-closed all-or-nothing preparation/application;
- atomic `RecoveryApplicationCoordinator` apply boundary.

PR #112 remains draft. Do not merge without explicit user authorization.
