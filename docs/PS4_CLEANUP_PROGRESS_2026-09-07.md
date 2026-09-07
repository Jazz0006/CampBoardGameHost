# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.3a complete; stop before PS4.3b**

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
  PS4.3 Recovery wire cleanup       IN PROGRESS
    PS4.3a typed TB rotation owner  COMPLETE
    PS4.3b Recovery v2/wire cleanup NOT STARTED
PS5 trigger simplification          NOT STARTED
```

Live `main` remained:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains open, draft and unmerged.

PS3 authoritative production checkpoint remains:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

PS4.1 production checkpoint remains:

```text
721c7394115cdc839afb189f88eb9234cd5ea204
refactor: remove dead active snapshot
```

PS4.2 production checkpoint remains:

```text
9fa0e3f86832cd847fb71126e4de54524e0326d2
refactor: give Recovery independent compatibility token
```

PS4.2 validated docs checkpoint before PS4.3a:

```text
c7d476ad079716dd11d6e3f2911ae4ca94df948f
docs: record PS4.2 cleanup checkpoint
```

## 2. PS4.1 — completed

PS4.1 removed the proven-dead `activeGameSnapshotJson()` path and only helpers proven exclusive to it. It did not change Recovery format/token semantics or remove `LegacyRestoreCompatibility`.

## 3. PS4.2 — completed Recovery compatibility ownership

Recovery now owns its compatibility token through its own current format version:

```text
RecoveryCompatibilityToken.currentFor(gameKind)
-> "recovery-v${RecoverySnapshot.CURRENT_FORMAT_VERSION}:${gameKind.name}"
```

For current format v1 this produces, for example:

```text
recovery-v1:Undercover
recovery-v1:Clocktower
```

PS4.2 deliberately did not clean the legacy Recovery wire fields or bump the Recovery format.

## 4. PS4.3a — completed typed Trouble Brewing rotation ownership

### 4.1 Fresh ownership audit

Before implementation, the live writer/decoder/application/bookkeeping path was re-audited.

`TroubleBrewingSetupRotationRecord` is genuine durable current-game bookkeeping, not obsolete compatibility debris:

```text
TB preset selection
-> committedTroubleBrewingSetupRotationRecord
-> active Recovery persistence
-> strict Recovery decode
-> App Recovery apply
-> completed-game rotation history
```

Therefore it had to move before `LegacyRestoreCompatibility` could later be deleted.

The audit also established two safe cross-invariants for the typed model:

1. a Trouble Brewing rotation record may only be carried by `ClocktowerScript.TroubleBrewing` Recovery;
2. when present, `record.playerCount` must equal the recovered card count.

No runtime-role/external-preset role-ID equality check was added because those layers use different identifier authorities and such a check would introduce unnecessary coupling.

### 4.2 Tests-first contract checkpoint

Typed ownership test checkpoint:

```text
1e856f0fe7c246bbb5f977810eb95fb34d8d08d1
test: define typed Clocktower rotation recovery ownership
```

Added:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryClocktowerRotationOwnershipTest.kt
```

The test protects:

- typed `ClocktowerRecovery` ownership and v1 round-trip of the TB rotation record;
- rejection of a TB rotation record on No Greater Joy Recovery;
- rejection when rotation `playerCount` does not match recovered cards.

The branch advanced while the initial test-only CI run was active, so that CI run was cancelled by normal concurrency before an exact compiler diagnostic was retained. The checkpoint still precedes the production model change and references the then-missing typed field. Final focused and fast validation below are the authoritative GREEN evidence.

### 4.3 Typed model / writer / decoder implementation

Implementation lineage:

```text
c70058abe3401138995493c54f4b9ba73d7ca7af
refactor: move TB rotation fact into typed Clocktower recovery

2c2e9a6e645a8756dd0b6fb68c6d0cc9dbbf1514
refactor: encode TB rotation from typed recovery

e3179d48d1abcff02072da9868b0761f209ffc45
refactor: decode TB rotation into typed Clocktower recovery
```

The resulting ownership is:

```text
ClocktowerRecovery.troubleBrewingSetupRotationRecord
```

`LegacyRestoreCompatibility` remains present for PS4.3b, but it no longer owns the Trouble Brewing rotation record.

The v1 external wire key remains unchanged:

```text
TroubleBrewingSetupCompletionPersistence.ROOT_KEY
= "troubleBrewingSetupCompletion"
```

The writer now emits that key from the typed `ClocktowerRecovery` field. The strict decoder places the decoded record directly into typed `ClocktowerRecovery`.

### 4.4 Large-App wiring

`CampBoardGameHostApp.kt` was changed through the required fail-closed GitHub Actions + separate Python large-file workflow.

The exact App product commit is:

```text
8ed54f855d095f3f9c4473822ba32f06af4630db
refactor: wire typed TB rotation recovery in App
```

That commit changes only `CampBoardGameHostApp.kt`, with:

```text
2 additions
2 deletions
```

Its three semantic changes are exactly:

1. stop placing `committedTroubleBrewingSetupRotationRecord` into `LegacyRestoreCompatibility`;
2. place it into `ClocktowerRecovery.troubleBrewingSetupRotationRecord` when saving;
3. restore App bookkeeping from `game.troubleBrewingSetupRotationRecord`, not from the legacy shell.

The one-shot tooling self-removed in:

```text
77ddfb0e0cbd058f37835d52cf3fccce0625cd94
chore: remove PS4.3a one-shot tooling
```

The temporary `.github/scripts/ps4_3a_typed_rotation_patch.py` and `.github/workflows/ps4-3a-typed-rotation-one-shot.yml` do not remain in the final branch tree.

## 5. PS4.3a validation evidence

One-shot run:

```text
PS4.3a typed rotation one-shot
run 34109004051
SUCCESS
```

The fail-closed workflow passed all stages:

```text
exact branch/parent/blob locks                  PASS
prepatch ownership/reference audit              PASS
large-App exact-anchor patch                    PASS
postpatch semantic + one-file diff audit        PASS
focused Recovery ownership tests                PASS
:app:compileDebugKotlin                          PASS
:app:testFast                                    PASS
git diff --check                                PASS
remote-head race audit                          PASS
App product commit/push                         PASS
temporary tooling self-removal                  PASS
```

Focused Recovery coverage included:

```text
RecoveryClocktowerRotationOwnershipTest
RecoverySnapshotJsonCodecTest
RecoveryRestorePlannerTest
RecoveryPreviewLoaderTest
RecoveryApplicationCoordinatorTest
```

The post-cleanup repository audit confirmed:

- `ClocktowerRecovery` directly owns the rotation record;
- `LegacyRestoreCompatibility` no longer contains that field;
- App save writes it into typed `ClocktowerRecovery`;
- App apply reads `game.troubleBrewingSetupRotationRecord`;
- no App reference remains to `plan.snapshot.legacyRestoreCompatibility.troubleBrewingSetupRotationRecord`.

## 6. Frozen boundaries after PS4.3a

PS4.3a deliberately did **not**:

- bump `RecoverySnapshot.CURRENT_FORMAT_VERSION`; it remains `1`;
- change current compatibility tokens; they remain `recovery-v1:*`;
- remove or rename the existing TB completion wire key;
- remove the remaining `LegacyRestoreCompatibility` shell;
- delete persisted ActiveGame identity metadata from Recovery;
- delete committed Clocktower setup metadata from Recovery;
- delete persisted Clocktower ruleset-role/ref metadata from Recovery;
- remove `ActiveGamePersistenceCoordinator.identityForSave()` validation behavior;
- change Recovery save timing or persistence triggers;
- change the 4-hour validity policy;
- add cross-version Recovery migration;
- change Archive compatibility;
- delete Werewolf;
- begin PS5;
- begin D6;
- merge PR #112.

Current Recovery format remains intentionally:

```text
RecoverySnapshot.CURRENT_FORMAT_VERSION = 1
```

`LegacyRestoreCompatibility` now retains only the still-unmigrated PS4.3b shell fields:

```text
activeGameStateVersion
identity
committedClocktowerSetup
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

## 7. Next approved slice — PS4.3b, not started

PS4.3b is the deliberate Recovery v2 / obsolete-wire cleanup slice.

Before production edits, re-audit the current writer, strict decoder, planner and App save/apply references. Then establish tests that explicitly prove the chosen current-format-only behavior.

Planned PS4.3b order:

1. add/strengthen tests for Recovery v2 and explicit v1 rejection;
2. bump `RecoverySnapshot.CURRENT_FORMAT_VERSION` from 1 to 2;
3. allow `RecoveryCompatibilityToken` to naturally become `recovery-v2:<GameKind>` through the existing Recovery-owned authority;
4. remove obsolete ActiveGame-shaped Recovery wire metadata:
   - legacy `version`;
   - persisted ActiveGame identity envelope;
   - committed Clocktower setup copy;
   - persisted Clocktower ruleset role IDs;
   - persisted Clocktower ruleset ref;
5. delete `LegacyRestoreCompatibility` once no genuine durable field remains;
6. keep the existing save-time identity/coordinator validation behavior unless separately re-homed and tested in PS4.4.

Do not add migration support for Recovery v1. Under the frozen product contract, previous-format Recovery must fail closed rather than be migrated.

Do not redesign the entire JSON hierarchy merely for aesthetics; PS4.3b should remove wrong ownership with the smallest safe wire change.

## 8. Current stop point

**Stop after PS4.3a. PS4.3b has not started.**

Before PS4.3b implementation:

1. re-query live `main`;
2. re-query PR #112 head/state/checks;
3. confirm this PS4.3a checkpoint is green;
4. keep PR #112 draft and unmerged;
5. re-audit all remaining `LegacyRestoreCompatibility` consumers;
6. establish explicit v2/current-format and old-v1 rejection tests before removing wire fields.

## 9. Retain boundaries throughout remaining PS4

Must remain intact unless separately re-audited:

- `GameArchiveRecord` / `GameArchiveJsonCodec`;
- real legacy archive-read compatibility;
- Trouble Brewing setup-rotation bookkeeping/history;
- durable Clocktower semantic history;
- action timeline / epistemic observations;
- current ruleset resolution behavior;
- 4-hour Recovery validity policy;
- fail-closed all-or-nothing preparation/application;
- atomic `RecoveryApplicationCoordinator` apply boundary.

## 10. Post-PS4 order

Preferred order remains:

```text
PS5 trigger audit/simplification if still warranted
-> final Persistence Simplification validation
-> explicit merge authorization for PR #112
-> separate Werewolf module removal
-> fresh D6 ownership/decomposition audit
```

PR #112 remains draft. Do not merge without explicit user authorization.
